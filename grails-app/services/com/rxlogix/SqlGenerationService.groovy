package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.config.metadata.CaseColumnJoinMapping
import com.rxlogix.config.metadata.SourceColumnMaster
import com.rxlogix.config.metadata.SourceTableMaster
import com.rxlogix.dto.AlertDataDTO
import com.rxlogix.dto.AlertReviewDTO
import com.rxlogix.dto.SignalChartsDTO
import com.rxlogix.dto.SuperQueryDTO
import com.rxlogix.enums.*
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.pvdictionary.config.ViewConfig
import com.rxlogix.reportTemplate.PercentageOptionEnum
import com.rxlogix.reportTemplate.ReassessListednessEnum
import com.rxlogix.signal.EmergingIssue
import com.rxlogix.signal.ProductTypeConfiguration
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.RelativeDateConverter
import com.rxlogix.util.SignalQueryHelper
import grails.converters.JSON
import grails.gorm.transactions.NotTransactional
import grails.util.Holders
import groovy.json.JsonSlurper
import groovy.sql.Sql
import oracle.jdbc.OracleTypes
import org.apache.http.util.TextUtils
import org.grails.datastore.mapping.query.Query
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.grails.web.json.JSONObject

import java.text.SimpleDateFormat
import java.util.regex.Pattern

import static com.rxlogix.Constants.AlertConfigType.AGGREGATE_CASE_ALERT
import static com.rxlogix.Constants.AlertConfigType.SINGLE_CASE_ALERT

class SqlGenerationService {

    static transactional = false

    public static final String VERSION_TABLE_NAME = "gtt_versions"
    public static final String VERSION_BASE_TABLE_NAME = "gtt_versions_base"
    public static final String CASE_LIST_TABLE_NAME = "gtt_query_case_list"
    public static final String DATE_FMT = "dd-MM-yyyy"
    private static final String DATE_FORMAT = "MM/dd/yyyy"
    private static final String DATETIME_FMT = "dd-MM-yyyy HH:mm:ss"
    private static final String DATETIME_FMT_ORA = "dd-MM-yyyy HH24:MI:SS"
    private static final String NEGATIVE_INFINITY = "01-01-0001 00:00:00"
    private static final String POSITIVE_INFINITY = "31-12-9999 23:59:59"
    private static final QUERY_LEVEL_SUBMISSION_CMR_TABLE_ALIAS = "cmr"

    public static final String SET_TABLE_NAME = "set_table"

    public static final Pattern PARTIAL_DATE_YEAR_ONLY = Pattern.compile("\\?{2}-\\?{3}-\\d{4}")
    public static final Pattern PARTIAL_DATE_MONTH_AND_YEAR = Pattern.compile("\\?{2}-[a-zA-Z]{3}-\\d{4}")
    public static final Pattern PARTIAL_DATE_FULL = Pattern.compile("\\d{2}-[a-zA-Z]{3}-\\d{4}")
    public static final String PARTIAL_DATE_FMT = "dd-MMM-yyyy"
    public static final String EXECUTION_START_DATE = "01-01-1900"
    public static final String SIGNIFICANT_FOLLOUP_NUMBER  = "analytic.caseInformation.significantFollowUpNumber"

    // Re-assess Listedness
    private static final RLDS = "RLDS"

    // No value is selected in configuration
    private static final NO_VALUE_SELECTED = "No Value Selected"

    def grailsApplication
    def queryService
    def dataObjectService
    def alertService
    def signalDataSourceService
    def reportExecutorService
    def cacheService
    def dataSource_pva
    def userService


    // Generate the SQL necessary to build the query to generate a "version temp table".
    // This is the initial step taken for the generation of a report. The "version temp
    // table" will become an input into the next step ( generateQuerySQL ).

    private boolean checkCustumTabFlag(TemplateQuery templateQuery) {
        boolean boolRet = false
        if (templateQuery.template.templateType == TemplateTypeEnum.CUSTOM_SQL &&
                templateQuery.dateRangeInformationForTemplateQuery.dateRangeEnum == DateRangeEnum.CUSTOM) {
            boolRet = true
        }
        return boolRet;
    }

    public String processDictionariesWithNoDLPRev(Configuration config, String versionSql, boolean isAggregateCase = false) {
        return processDictionaries(config, versionSql, false, isAggregateCase)
    }

    public String processDictionariesWithDLPRev(TemplateQuery templateQuery, boolean isAggregateCase = false) {
        Configuration config = templateQuery.report
        Boolean excludeFollowUp = config.excludeFollowUp
        Date startDate = templateQuery?.dateRangeInformationForTemplateQuery?.getReportStartAndEndDate()[0]
        Date endDate = templateQuery?.dateRangeInformationForTemplateQuery?.getReportStartAndEndDate()[1]
        String initialDateFilterCol = SourceColumnMaster.findByReportItem(config.dateRangeType.value()).columnName
        String dateCheckSql = ""
        String excludeFollowupCheck = ""
        String includeMedicallyConfirmedCases = ""
        boolean hasWhere = false
        boolean medicallyConfirmedCasesFlag = config.includeMedicallyConfirmedCases

        if (checkCustumTabFlag(templateQuery)) {
            startDate = new Date().parse(DATETIME_FMT, NEGATIVE_INFINITY)
        }
        if (excludeFollowUp) {
            excludeFollowupCheck = """ and dc.seq_num =0 """
        }
        //PVDB-526 date check should not be present for date range = submission_date or case_locked_date
        if ((config.dateRangeType.value() != DateRangeTypeCaseEnum.SUBMISSION_DATE.value()) &&
                config.dateRangeType.value() != DateRangeTypeCaseEnum.CASE_LOCKED_DATE.value()) {
            dateCheckSql = """where exists ( select 1 from dv_case_dates_list dc where dc.TENANT_ID = t2.TENANT_ID and
                    dc.VERSION_NUM = t2.DLP_REVISION_NUMBER and dc.case_id = t2.case_id and dc.significant =1 ${excludeFollowupCheck}
                     and  trunc(dc.${initialDateFilterCol})  >= trunc(TO_DATE( '${new SimpleDateFormat(DATE_FMT).format(startDate)}', '${DATETIME_FMT_ORA}'))
                     and  trunc(dc.${initialDateFilterCol})  <  trunc(TO_DATE( '${new SimpleDateFormat(DATE_FMT).format(endDate)}', '${DATETIME_FMT_ORA}'))+1)"""
            hasWhere = true
        } else if (config.dateRangeType..value() == DateRangeTypeCaseEnum.CASE_LOCKED_DATE.value()) {
            if (!hasWhere) {
                dateCheckSql = " WHERE"
            } else {
                dateCheckSql = " AND"
            }

            dateCheckSql += " EXISTS (SELECT 1 FROM C_IDENTIFICATION ci WHERE NVL(LOCKED_DATE,ARCHIEVE_DATE) IS NOT NULL" +
                    " AND ci.TENANT_ID = t2.TENANT_ID AND ci.CASE_ID = t2.CASE_ID AND ci.VERSION_NUM = t2.VERSION_NUM)"

            hasWhere = true
        }

        // Adding sql for including only medically confirmed cases (PVR-1743)
        if (medicallyConfirmedCasesFlag) {
            if (!hasWhere) {
                includeMedicallyConfirmedCases = " WHERE"
            } else {
                includeMedicallyConfirmedCases = " AND"
            }
            includeMedicallyConfirmedCases += " EXISTS (SELECT 1 FROM CASE_FLAGS cf WHERE " +
                    " cf.TENANT_ID = t2.TENANT_ID " +
                    " AND cf.CASE_ID = t2.CASE_ID " +
                    " AND cf.VERSION_NUM = t2.dlp_revision_number AND cf.${Holders.config.pvsignal.includeMedConfCases ?: 'HCP_FLAG'} = 1 )"
        }

        String versionSql = " Select t2.TENANT_ID, t2.CASE_ID, t2.DLP_REVISION_NUMBER from $VERSION_BASE_TABLE_NAME t2 ${dateCheckSql} ${includeMedicallyConfirmedCases}"

        return processDictionaries(config, versionSql, true, isAggregateCase)
    }

    private String processDictionaries(Configuration config, String versionSql, boolean joinDLPVersion, boolean isAggregateCase = false) {
        String result = versionSql
        if (!isAggregateCase) {
            if (config.eventSelection || config.productSelection || config.studySelection) {
                List dictionaryFilter = getDictionaryFilter(config, joinDLPVersion)
                result = """${dictionaryFilter[0]} select TENANT_ID, case_id, dlp_revision_number
                from (${versionSql}) t1 ${dictionaryFilter[1]}"""

            }
        }
        return result
    }

    public String generateVersionTableInsert(String versionSQL) {
        return " INSERT INTO $VERSION_BASE_TABLE_NAME (TENANT_ID, case_id, dlp_revision_number) ${versionSQL}"
    }

    public String generateFinalVersionTableInsert(String versionSQL) {
        return " INSERT INTO $VERSION_TABLE_NAME (TENANT_ID, case_id, dlp_revision_number) ${versionSQL}"
    }

    public String getQueryProductDictFilter(Configuration configuration) {
        String whereClause = ""

        if (configuration.productSelection) {
            whereClause = getCaseListFromProductForQuery(configuration)
        }

        return whereClause
    }

    public List getQueryEventDictFilter(Configuration configuration) {
        return getCaseListFromEvent(configuration, true, true) // Assuming DLPJoin = true
    }

    public List getQueryStudyDictFilter(Configuration configuration) {
        return getCaseListFromStudy(configuration, true, true) // Assuming DLPJoin = true
    }

    private List getDictionaryFilter(Configuration configuration, boolean DLPJoin) {
        String withClause = ""
        String whereClause = ""

        if (configuration.productSelection) {
            String productFilter = getCaseListFromProduct(configuration, DLPJoin)

            if (whereClause != "") {
                whereClause += " and "
            }

            whereClause += "case_id in (${productFilter})"
        }

        if (configuration.studySelection) {
            def studyFilter = getCaseListFromStudy(configuration, DLPJoin, false)

            if (whereClause != "") {
                whereClause += " and "
            }

            withClause += studyFilter[0]
            whereClause += "case_id in (${studyFilter[1]})"
        }

        if (configuration.eventSelection) {
            def eventFilter = getCaseListFromEvent(configuration, DLPJoin, false)

            if (whereClause != "") {
                whereClause += " and "
                if (withClause != "" && eventFilter[0] != "") {
                    withClause += ", "
                }
            }

            withClause += eventFilter[0]
            whereClause += "case_id in (${eventFilter[1]})"
        }

        if (whereClause) {
            if (withClause != "") {
                withClause = " with " + withClause
            }
            return [withClause, "where " + whereClause]
        } else {
            return ["", ""]
        }
    }

    private String getCaseListFromProductForQuery(Configuration configuration) {
        String result = """SELECT cp.case_id, cp.prod_rec_num, cp.version_num FROM c_prod_identification cp
                        WHERE (PROD_ID_RESOLVED IN ( SELECT product_id FROM ("""
        return appendProductFilterInfo(configuration, result) + ")))"
    }

    private String getCaseListFromProduct(Configuration configuration, boolean DLPJoin) {
        String DLPRevJoinSQL = ""
        if (DLPJoin) {
            DLPRevJoinSQL = "And cp.version_num = t1.version_num "
        }

        String result = """SELECT cp.case_id FROM C_PROD_IDENTIFICATION cp
                        WHERE cp.TENANT_ID = t1.TENANT_ID  and cp.case_id = t1.case_id ${DLPRevJoinSQL}"""

        if (configuration.studySelection) {
            return result
        }
        result += """AND
            (cp.PROD_ID_RESOLVED IN ( SELECT product_id FROM ("""
        return appendProductFilterInfo(configuration, result) + ")))"
    }

    private String appendProductFilterInfo(Configuration configuration, String result) {
        Map dictionaryMap = dataObjectService.getLabelIdMap()
        List ingredient = configuration.getProductDictionaryValues()[dictionaryMap.get('Ingredient')]
        List family = configuration.getProductDictionaryValues()[dictionaryMap.get('Family')]
        List product = configuration.getProductDictionaryValues()[dictionaryMap.get('Product Name')]
        List trade = configuration.getProductDictionaryValues()[dictionaryMap.get('Trade Name')]

        String selectDicLevel = ""

        if (ingredient) {
            String ingredientCodes = ingredient?.toString()
            ingredientCodes = ingredientCodes.substring(1, ingredientCodes.length() - 1) // remove "[" and "]"
            if (selectDicLevel != "") {
                selectDicLevel += " UNION "
            }
            selectDicLevel += """(SELECT lpc.product_id FROM VW_PROD_INGRED_LINK lpc
                            JOIN (SELECT * FROM VW_INGREDIENT_DSP where lang_id = '1') li ON (lpc.ingredient_id = li.ingredient_id and lpc.TENANT_ID = li.TENANT_ID)
                            WHERE li.ingredient_id IN (${ingredientCodes}))"""
        }

        if (family) {
            String familyCodes = family?.toString()
            familyCodes = familyCodes.substring(1, familyCodes.length() - 1) // remove "[" and "]"
            if (selectDicLevel != "") {
                selectDicLevel += " UNION "
            }
            selectDicLevel += """(SELECT lp.product_id FROM VW_PRODUCT_DSP lp
                            JOIN (Select * from VW_FAMILY_NAME_DSP where lang_id = '1') lpf ON (lpf.prod_family_id = lp.prod_family_id and lpf.TENANT_ID = lp.TENANT_ID  )
                            WHERE lpf.prod_family_id IN (${familyCodes}) and lp.lang_id = '1')"""
        }

        if (product) {
            String productCodes = product?.toString()
            productCodes = productCodes.substring(1, productCodes.length() - 1) // remove "[" and "]"
            if (selectDicLevel != "") {
                selectDicLevel += " UNION "
            }
            selectDicLevel += "(SELECT lp.product_id FROM VW_PRODUCT_DSP lp WHERE lp.product_id IN (${productCodes}) and lp.lang_id = '1')"
        }

        if (trade) {
            String tradeCodes = trade?.toString()
            tradeCodes = tradeCodes.substring(1, tradeCodes.length() - 1) // remove "[" and "]"
            if (selectDicLevel != "") {
                selectDicLevel += " UNION "
            }
            selectDicLevel += """(SELECT llp.product_id FROM VW_PROD_LICENSE_LINK_DSP llp
                            JOIN VW_TRADE_NAME_DSP ll ON (ll.license_id = llp.license_id and ll.TENANT_ID = llp.TENANT_ID and ll.lang_id = '1')
                            WHERE ll.license_id IN (${tradeCodes}))"""
        }

        result += selectDicLevel

        return result
    }

    private List getCaseListFromStudy(Configuration configuration, boolean DLPJoin, boolean querySQLSelection) {
        List protocol = configuration.getStudyDictionaryValues()[0]
        List study = configuration.getStudyDictionaryValues()[1]
        List center = configuration.getStudyDictionaryValues()[2]
        String DLPRevJoinSQL = ""
        if (DLPJoin) {
            if (querySQLSelection) {
                DLPRevJoinSQL = "and cs.version_num = ver.version_num "
            } else {
                DLPRevJoinSQL = "And cs.version_num = t1.dlp_revision_number "
            }
        }

        String withClause = ""
        String selectDicLevel = ""

        if (study) {
            String studyCodes = study.toString()
            studyCodes = studyCodes.substring(1, studyCodes.length() - 1) // remove "[" and "]"
            if (withClause != "") {
                withClause += " UNION "
            }
            withClause += "(SELECT ls.study_key FROM VW_STUDY_NUM ls WHERE ls.study_key IN (${studyCodes}) and ls.lang_id = '1')"

            if (selectDicLevel != "") {
                selectDicLevel += " OR "
            }
            selectDicLevel += "cs.study_id IN (${studyCodes})"
        }

        if (protocol) {
            String protocolCodes = protocol.toString()
            protocolCodes = protocolCodes.substring(1, protocolCodes.length() - 1) // remove "[" and "]"
            if (withClause != "") {
                withClause += " UNION "
            }
            withClause += "(SELECT ls.study_key FROM VW_STUDY_NUM ls WHERE ls.id_protocol IN (${protocolCodes}) and ls.lang_id = '1')"

            if (selectDicLevel != "") {
                selectDicLevel += " OR "
            }
            selectDicLevel += " UPPER(cs.study_project_id) IN (" +
                    " SELECT UPPER(lpt.PROTOCOL_DESCRIPTION) FROM VW_PROTOCOL lpt WHERE lpt.PROTOCOL_ID IN (${protocolCodes}))"
        }

        if (center) {
            String centerCodes = center.toString()
            centerCodes = centerCodes.substring(1, centerCodes.length() - 1) // remove "[" and "]"
            if (withClause != "") {
                withClause += " UNION "
            }
            withClause += """(SELECT lsc.study_key FROM VW_STUDY_CENTER_LINK lsc INNER JOIN (Select * from VW_LCE_CENTER_NAME_dsp where lang_id = '1') lc
                               ON (lsc.center_id = lc.center_id and lsc.TENANT_ID = lc.TENANT_ID) WHERE lc.center_id IN (${
                centerCodes
            }))"""

            if (selectDicLevel != "") {
                selectDicLevel += " OR "
            }
            selectDicLevel += " UPPER(cs.study_center_name) IN (SELECT UPPER(center_name) from VW_LCE_CENTER_NAME where id in (${centerCodes}) and lang_id = '1')"
        }

        String whereClauseResult
        if (querySQLSelection) {
            whereClauseResult = """SELECT DISTINCT cs.case_id FROM case_study_info cs, gtt_versions ver WHERE cs.TENANT_ID = ver.TENANT_ID and cs.case_id = ver.case_id ${
                DLPRevJoinSQL
            } AND cs.version_num = ver.dlp_revision_number
                AND (${selectDicLevel} OR EXISTS (SELECT 1 FROM key_filter kf WHERE cs.study_id = kf.study_key))"""
        } else {
            whereClauseResult = """SELECT DISTINCT cs.case_id FROM case_study_info cs WHERE cs.TENANT_ID = t1.TENANT_ID and cs.case_id = t1.case_id ${
                DLPRevJoinSQL
            } AND cs.version_num = t1.dlp_revision_number
                AND (${selectDicLevel} OR EXISTS (SELECT 1 FROM key_filter kf WHERE cs.study_id = kf.study_key))"""
        }

        return [" key_filter AS (SELECT study_key FROM (${withClause})) ", whereClauseResult]
    }

    private List getCaseListFromEvent(Configuration configuration, boolean DLPJoin, boolean querySQLSelection) {
        List soc = configuration.getEventDictionaryValues()[0]
        List hlgt = configuration.getEventDictionaryValues()[1]
        List hlt = configuration.getEventDictionaryValues()[2]
        List pt = configuration.getEventDictionaryValues()[3]
        List llt = configuration.getEventDictionaryValues()[4]
        List synonyms = configuration.getEventDictionaryValues()[5]
        String DLPRevJoinSQL = ""
        if (DLPJoin) {
            if (querySQLSelection) {
                DLPRevJoinSQL = "and ce.version_num = ver.version_num "
            } else {
                DLPRevJoinSQL = "And ce.version_num = t1.dlp_revision_number "
            }
        }

        String ptFilter = "pt_filter AS (SELECT mmh.pt_code FROM PVR_MD_HIERARCHY_dsp mmh"

        if (soc || hlgt || hlt || pt) {
            String whereClause = ""

            if (soc) {
                String socCodes = soc.toString()
                socCodes = socCodes.substring(1, socCodes.length() - 1) // remove "[" and "]"
                if (whereClause != "") {
                    whereClause += " OR "
                }
                whereClause += "mmh.soc_code IN (${socCodes})"
            }

            if (hlgt) {
                String hlgtCodes = hlgt.toString()
                hlgtCodes = hlgtCodes.substring(1, hlgtCodes.length() - 1) // remove "[" and "]"
                if (whereClause != "") {
                    whereClause += " OR "
                }
                whereClause += "mmh.hlgt_code IN (${hlgtCodes})"
            }

            if (hlt) {
                String hltCodes = hlt.toString()
                hltCodes = hltCodes.substring(1, hltCodes.length() - 1) // remove "[" and "]"
                if (whereClause != "") {
                    whereClause += " OR "
                }
                whereClause += "mmh.hlt_code IN (${hltCodes})"
            }

            if (pt) {
                while (pt.size() > 1000) {
                    String ptCodes = pt.take(1000).toString()
                    ptCodes = ptCodes.substring(1, ptCodes.length() - 1) // remove "[" and "]"
                    pt = pt.drop(1000)
                }
                String ptCodes = pt.toString()
                ptCodes = ptCodes.substring(1, ptCodes.length() - 1) // remove "[" and "]"

                if (whereClause != "") {
                    whereClause += " OR "
                }
                whereClause += "mmh.pt_code IN (${ptCodes})"
            }

            ptFilter += " WHERE ${whereClause}"
        }
        ptFilter += ")"

        String lltFilter = """, llt_filter AS (SELECT llt_code FROM (SELECT mptl.llt_code FROM PVR_MD_pref_term_llt_dsp mptl
                            WHERE EXISTS (SELECT 1 FROM pt_filter t1 WHERE t1.pt_code = mptl.pt_code and mptl.lang_id = 1)))"""

        String withClause = ""
        String whereClause = ""

        if (llt) {
            while (llt.size() > 1000) {
                String lltCodes = llt.take(1000).toString()
                lltCodes = lltCodes.substring(1, lltCodes.length() - 1) // remove "[" and "]"
                if (whereClause != "") {
                    whereClause += " or "
                }
                whereClause += "t1.pt_code in (${lltCodes})"
                llt = llt.drop(1000)
            }
            String lltCodes = llt.toString()
            lltCodes = lltCodes.substring(1, lltCodes.length() - 1) // remove "[" and "]"
            if (whereClause != "") {
                whereClause += " or "
            }
            whereClause += "ce.meddra_llt_code IN (${lltCodes})"
        }

        if (synonyms) {
            if (whereClause != "") {
                whereClause += " or "
            }
            String synonymsCodes = synonyms.keySet()?.toString()
            synonymsCodes = synonymsCodes.substring(1, synonymsCodes.length() - 1) // remove "[" and "]"
            whereClause = " ce.mdr_ae_llt_code IN ( SELECT ms.llt_code FROM PVR_MD_synonyms_dsp ms WHERE ms.syn_id IN (${synonymsCodes}) AND LANG_ID = 1)"
        }

        if (soc || hlgt || hlt || pt || synonyms) {
            if (whereClause != "") {
                whereClause += " OR "
            }
            if (querySQLSelection) {
                whereClause += "EXISTS (SELECT 1 FROM llt_filter t2, pt_filter t1 WHERE t2.llt_code = ce.mdr_ae_llt_code)"
            } else {
                whereClause += "EXISTS (SELECT 1 FROM llt_filter t2 WHERE t2.llt_code = ce.mdr_ae_llt_code)"
            }
            withClause = ptFilter + lltFilter
        }

        String whereClauseResult
        if (querySQLSelection) {
            whereClauseResult = """SELECT ce.case_id, ce.AE_REC_NUM FROM C_AE_IDENTIFICATION ce, gtt_versions ver WHERE ce.tenant_id = ver.TENANT_ID
                and ce.case_id = ver.case_id ${DLPRevJoinSQL} AND (${whereClause})"""
        } else {
            whereClauseResult = """SELECT ce.case_id FROM C_AE_IDENTIFICATION ce WHERE ce.tenant_id = t1.TENANT_ID
                and ce.case_id = t1.case_id ${DLPRevJoinSQL} AND (${whereClause})"""
        }

        //A check is introduced
        if (configuration.limitPrimaryPath) {
            whereClauseResult = whereClauseResult + " and ce.primary_path_coded_flag =1"
        }

        return [withClause, whereClauseResult]
    }

    public String generateEmptyQuerySQL(TemplateQuery templateQuery, boolean isAggregateCase = false) {

        QueryLevelEnum queryLevel = templateQuery.queryLevel

        String result = "select cm.TENANT_ID, cm.case_id, cm.version_num "

        if (!isAggregateCase) {
            if (queryLevel == QueryLevelEnum.PRODUCT || queryLevel == QueryLevelEnum.PRODUCT_EVENT || templateQuery.report.productSelection) {
                result += ", cp.prod_rec_num prod_rec_num, cp.version_num prod_version_num"
            }
            if (queryLevel == QueryLevelEnum.PRODUCT_EVENT || templateQuery.report.eventSelection) {
                result += ", ce.AE_REC_NUM"
            }
            if (queryLevel == QueryLevelEnum.SUBMISSION) {
                result += ", cmr.PROCESSED_REPORT_ID"
            }
        }

        result += " from V_C_IDENTIFICATION cm"

        if (!isAggregateCase) {
            if (queryLevel == QueryLevelEnum.PRODUCT || queryLevel == QueryLevelEnum.PRODUCT_EVENT || templateQuery.report.productSelection) {
                String productFilter = ""
                if (templateQuery.report.productSelection) {
                    String str = """(cp.PROD_ID_RESOLVED IN ( SELECT product_id FROM ("""
                    productFilter = " AND " + appendProductFilterInfo(templateQuery.report, str) + ")))"
                }
                result += """ join C_PROD_IDENTIFICATION cp on (cm.CASE_ID = cp.CASE_ID and cm.version_num = cp.version_num
                    AND cm.TENANT_ID = cp.TENANT_ID ${productFilter})"""
            }
            if (queryLevel == QueryLevelEnum.PRODUCT_EVENT || templateQuery.report.eventSelection) {
                result += " join C_AE_IDENTIFICATION ce on (cm.CASE_ID = ce.CASE_ID and cm.version_num = ce.version_num AND cm.TENANT_ID = ce.TENANT_ID)"
            }
            if (queryLevel == QueryLevelEnum.SUBMISSION) {
                result += " left join V_C_SUBMISSIONS cmr on (cm.CASE_ID = cmr.CASE_ID AND cm.TENANT_ID = cmr.TENANT_ID)"
            }
        }
        return """$result join $VERSION_TABLE_NAME ver
                    on (cm.case_id = ver.case_id and cm.version_num = ver.version_num)"""
    }

    private String replaceMapInString(String sqlQuery, Map<String, String> parameterMap) {
        String result = sqlQuery
        parameterMap.each {
            result = result.replaceAll(it.key, it.value)
        }
        return result
    }

    public String generateCaseListInsert(String querySQL, TemplateQuery templateQuery, boolean isAggregateCase = false) {

        if (!isAggregateCase) {
            if (templateQuery.queryLevel == QueryLevelEnum.PRODUCT) {
                return " INSERT INTO $CASE_LIST_TABLE_NAME (TENANT_ID, case_id, version_num, prod_rec_num) ${querySQL} "
            } else if (templateQuery.queryLevel == QueryLevelEnum.PRODUCT_EVENT) {
                return " INSERT INTO $CASE_LIST_TABLE_NAME (TENANT_ID, case_id, version_num, prod_rec_num, event_seq_num) ${querySQL} "
            } else if (templateQuery.queryLevel == QueryLevelEnum.SUBMISSION) {
                return " INSERT INTO $CASE_LIST_TABLE_NAME (TENANT_ID, case_id, version_num, PROCESSED_REPORT_ID) ${querySQL} "
            }
        }
        return " INSERT INTO $CASE_LIST_TABLE_NAME (TENANT_ID, case_id, version_num) ${querySQL} "
    }

    private int selectedPercentOption(PercentageOptionEnum percentageOption) {
        int selectedOption = 0

        if (percentageOption == PercentageOptionEnum.NO_PERCENTAGE) {
            selectedOption = 0
        } else if (percentageOption == PercentageOptionEnum.BY_SUBTOTAL) {
            selectedOption = 1
        } else {
            selectedOption = 2
        }
        return selectedOption
    }

    // function to generat join clause of custom/interval date range in tabulation
    private String generateCustomDateFilterJoinSQl(boolean submissionReportFlag, String filterTableAlias, boolean reportCountFlag) {
        String filterSql = ""

        if (submissionReportFlag) {

            filterSql = " on (cmr.case_id = " + filterTableAlias + ".case_id and cmr.TENANT_ID = " + filterTableAlias + ".TENANT_ID "
            if (reportCountFlag) {
                filterSql += "and cmr.PROCESSED_REPORT_ID = " + filterTableAlias + ".PROCESSED_REPORT_ID "
            }
            filterSql += ")"
        } else {
            filterSql += " on (cm.case_id = " + filterTableAlias + ".case_id and cm.TENANT_ID = " +
                    filterTableAlias + ".TENANT_ID and cm.version_num = " + filterTableAlias + ".version_num )"
        }
        return filterSql
    }

    // function to generate WithClause of the of custom/interval date range in tabulation
    private String generateCustomDateFilterWithView(boolean submissionReportFlag, boolean checkExcludeFupFlag, String filterColumn, Date startDate, Date endDate, boolean reportCountFlag) {
        String filterSql = ""
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FMT)

        if (submissionReportFlag) {
            filterSql = "( select /*+ MATERIALIZE */ distinct case_id "

            if (reportCountFlag) {
                filterSql += ", PROCESSED_REPORT_ID "
            }
            filterSql += ", TENANT_ID  from V_CASE_SUBMISSION_INFO where deleted is null " +
                    " and trunc(SUBMISSION_DATE)  >= trunc(TO_DATE( '${sdf.format(startDate)}', '${DATETIME_FMT_ORA}')) " +
                    " and trunc(SUBMISSION_DATE)  <  trunc(TO_DATE( '${sdf.format(endDate)}', '${DATETIME_FMT_ORA}'))+1 )"
        } else {
            if (!reportCountFlag) {
                filterSql = "( select /*+ MATERIALIZE */ distinct case_id , TENANT_ID, version_num from dv_case_dates_list where "
            } else {
                filterSql = "(SELECT /*+ MATERIALIZE */ DISTINCT dcdl.case_id, dcdl.TENANT_ID, dcdl.version_num, cmr.PROCESSED_REPORT_ID " +
                        " FROM V_CASE_SUBMISSION_INFO cmr, dv_case_dates_list dcdl where cmr.casE_id = dcdl.case_id and cmr.deleted is null and "
            }
            if (checkExcludeFupFlag) {
                filterSql += "   initial_fu_flag =0  and "
            } else {
                filterSql += "   SIGNIFICANT =1  and "
            }
            filterSql += " trunc(${filterColumn})  >= trunc(TO_DATE( '${sdf.format(startDate)}', '${DATETIME_FMT_ORA}')) and " +
                    " trunc(${filterColumn})  <  trunc(TO_DATE(   '${sdf.format(endDate)}', '${DATETIME_FMT_ORA}'))+1 )"

        }
        return filterSql
    }

    String getInsertStatementsToInsert(Configuration configuration, SuperQueryDTO superQuery, boolean nonValidCases, boolean isBusinessConfig = false, boolean deletedCases = true) {
        String additionalQueryBuilderBlock = ""
        //TODO; Need to make it in sync with the Reports its not relevent in the pv signal.
        Set<ParameterValue> poiInputParams = []//templateQuery.report?.poiInputsParameterValues ?: []
        if (configuration.template instanceof CaseLineListingTemplate && configuration.template?.JSONQuery) {
            additionalQueryBuilderBlock = configuration.template?.JSONQuery
        }
        SuperQueryDTO superForegroundQueryDTO = queryService.queryDetail(configuration.alertForegroundQueryId)
        return getInsertStatementsToInsert(configuration,superForegroundQueryDTO,configuration.adhocRun,configuration.dataMiningVariableValue,superQuery, additionalQueryBuilderBlock, configuration.alertQueryValueLists, configuration.alertForegroundQueryValueLists, poiInputParams, nonValidCases, configuration?.type, configuration?.selectedDatasource, isBusinessConfig, deletedCases)
    }

    String getInsertStatementsToInsert(Configuration configuration,SuperQueryDTO superForegroundQueryDTO,boolean adhocRun,String dataMiningVariableValue,SuperQueryDTO superQuery, String additionalJSONQuery = "", def parameters,def foregroundParameters, def poiInputParams, boolean nonValidCases, String configType, String selectedDatasource, boolean isBusinessConfig, boolean deletedCases = true) {

        //TODO: Need to set the locale from the previously passed locale.
        Locale locale = new Locale("en", "US");

        List<ParameterValue> blanks = []
        List<ParameterValue> foregroundBlanks = []
        List<ParameterValue> customSqlBlanks = []
        List<ParameterValue> foregroundCustomSqlBlanks = []
        parameters?.each { globalQueryList ->
            globalQueryList.parameterValues.each { parmeterValue ->
                if (parmeterValue.hasProperty('reportField')) {
                    blanks.add(parmeterValue)
                } else {
                    customSqlBlanks.add(parmeterValue)
                }
            }
        }
        foregroundParameters?.each { globalQueryList ->
            globalQueryList.parameterValues.each { parmeterValue ->
                if (parmeterValue.hasProperty('reportField')) {
                    foregroundBlanks.add(parmeterValue)
                } else {
                    foregroundCustomSqlBlanks.add(parmeterValue)
                }
            }
        }

        String queryString = """
                            DECLARE lastInsertedRow ROWID;
                            BEGIN
                                delete from GTT_QUERY_DETAILS;
                                delete from GTT_QUERY_SETS;
                                delete from GTT_REPORT_VAR_INPUT;
                       """

         if (configType == AGGREGATE_CASE_ALERT && adhocRun){
            try{
                Map dmvJson = JSON.parse(dataMiningVariableValue)
                String delimiter= dmvJson.get("delimiter")
                String value= dmvJson.get("value")
                String operator= dmvJson.get("operator")
                String[] valueArray=value.split(delimiter)
                log.info("Inserting data mining variable values.")
                valueArray.each {
                    queryString +="INSERT INTO GTT_QUERY_DETAILS (SET_ID,FIELD_ID,JAVA_VARIABLE,FIELD_OPERATOR,FIELD_VALUES,GROUP_ID,GROUP_OPERATOR,CUSTOM_INPUT,PARENT_GROUP_ID) values (2000,null,null,'${operator}', '${it}',null,null,null,null);"
                }
                log.info("Data mining variable values inserted correctly.")
            }catch(NullPointerException ex){
                log.info("Data mining variable values are null")
            }
        }
        SqlGenIDDTO sqlGenIDDTO = new SqlGenIDDTO()
        if (isBusinessConfig) {
            if (configuration.alertForegroundQueryName) {
                sqlGenIDDTO.value = 3000
                queryString = queryString + insertQueriesDataToTempTableForBusinessConfig(superForegroundQueryDTO, sqlGenIDDTO, null, 0, foregroundBlanks,foregroundCustomSqlBlanks,poiInputParams, locale, false)
            }else{
                queryString = queryString + insertQueriesDataToTempTableForBusinessConfig(superQuery, sqlGenIDDTO, null, 0, blanks,customSqlBlanks,poiInputParams, locale, false)
            }
        } else {
            queryString = queryString + insertQueriesDataToTempTable(superQuery, sqlGenIDDTO, null, 0,  blanks, customSqlBlanks, poiInputParams, locale, false)
            if (configuration.alertForegroundQueryName) {
                sqlGenIDDTO.value = 3000
                queryString = queryString + insertQueriesDataToTempTable(superForegroundQueryDTO,  sqlGenIDDTO, null, 0, foregroundBlanks, foregroundCustomSqlBlanks, poiInputParams, locale, false)
            }
        }

        if (additionalJSONQuery) {
            sqlGenIDDTO.value = sqlGenIDDTO.value + 1
            queryString = queryString + buildQueryFromJSONQuery(additionalJSONQuery, sqlGenIDDTO, 0, null, false, [], poiInputParams, locale, false, true)
        }
        def isSieveAnalysisQuery = false
        if (configType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT && ((selectedDatasource.startsWith(Constants.DataSource.PVA) && Holders.config.signal.sieveAnalysis.safetyDB) || (selectedDatasource.startsWith(Constants.DataSource.FAERS) && Holders.config.signal.sieveAnalysis.faers) || (selectedDatasource.startsWith(Constants.DataSource.VAERS) && Holders.config.signal.sieveAnalysis.vaers) || (selectedDatasource.startsWith(Constants.DataSource.VIGIBASE) && Holders.config.signal.sieveAnalysis.vigibase))) {

            String sieveAnalysisQueryName = Holders.config.pvsignal.sieveAnalysisQueryName
            int incrementSetId = Holders.config.signal.incrementSetId
            //when sieve analysis query is selected, need to increment the set_id with 1000
            SuperQueryDTO sieveAnalysisQuery = queryService.queryDetailByName(sieveAnalysisQueryName)
            if (sieveAnalysisQuery) {
                isSieveAnalysisQuery = true
                if (configuration.alertForegroundQueryName) {
                    sqlGenIDDTO.value = sqlGenIDDTO.value-3000 + incrementSetId
                }else{
                    sqlGenIDDTO.value = sqlGenIDDTO.value + incrementSetId
                }
                queryString = queryString + insertQueriesDataToTempTable(sieveAnalysisQuery, sqlGenIDDTO, null, 0, blanks, customSqlBlanks, poiInputParams, locale, false)
            }
        }
        if (nonValidCases && selectedDatasource.startsWith(Constants.DataSource.PVA)) {
            String nonValidQueryName = Holders.config.pvreports.nonValidQueryName.quan
            if (configType == SINGLE_CASE_ALERT) {
                nonValidQueryName = Holders.config.pvreports.nonValidQueryName.qual
            }
            SuperQueryDTO nonValidQuery = queryService.queryDetailByName(nonValidQueryName)
            if (nonValidQuery) {
                //When we have non valid cases value then value should be 2.
                sqlGenIDDTO.value = Constants.Commons.TWO
                queryString = queryString + insertQueriesDataToTempTable(nonValidQuery, sqlGenIDDTO, null, 0, [], [],  poiInputParams, locale, true)
            }
        }
        if (!configuration.alertQueryId && configType == AGGREGATE_CASE_ALERT) {
            String disproportionalityScoresQueryName = Holders.config.pvreports.disproportionality.scores.query.name
            if (disproportionalityScoresQueryName) {
                SuperQueryDTO disproportionalityScoresQuery = queryService.queryDetailByName(disproportionalityScoresQueryName)
                if (disproportionalityScoresQuery) {
                    if (isSieveAnalysisQuery) {
                        int incrementSetId = Holders.config.signal.incrementSetId
                        sqlGenIDDTO.value = sqlGenIDDTO.value - incrementSetId + 4000
                    } else if (configuration.alertForegroundQueryName) {
                        sqlGenIDDTO.value = sqlGenIDDTO.value - 3000 + 4000
                    } else {
                        sqlGenIDDTO.value = sqlGenIDDTO.value + 4000
                    }
                    queryString = queryString + insertQueriesDataToTempTable(disproportionalityScoresQuery, sqlGenIDDTO, null, 0, [], [], poiInputParams, locale, false)
                } else {
                    throw new Exception(Holders.config.pvreports.disproportionality.scores.query.name + " is not configured in PV Reports.")
                }
            }
        }
        if (poiInputParams) {
            poiInputParams.each {
                queryString = queryString + "INSERT INTO GTT_REPORT_VAR_INPUT (INPUT_KEY, INPUT_VALUES) values ('${it.key}',${it.value ? "'${it.value}'" : null});\n"
            }
        }

        queryString = queryString + "\n END; \n"
        return queryString
    }

    private String insertQueriesDataToTempTable(boolean isForeground = false, SuperQueryDTO superQuery,
                                                SqlGenIDDTO sqlGenIDDTO, String joinOperator, int parent, List<ParameterValue> blanks,
                                                List<ParameterValue> customSqlBlanks, Set<ParameterValue> poiInputParams, Locale locale, boolean nonValidCases, boolean deletedCases = false) {
        String insertQuery = ""
        if (superQuery?.queryType == QueryTypeEnum.QUERY_BUILDER) {
            insertQuery = insertQuery + buildQueryFromJSONQuery(isForeground,superQuery.JSONQuery, sqlGenIDDTO, parent, joinOperator, superQuery.hasBlanks, blanks, poiInputParams, locale, nonValidCases, false, deletedCases)
        } else if (superQuery?.queryType == QueryTypeEnum.CUSTOM_SQL) {
            String sqlQuery = superQuery.customSQLQuery
            if (superQuery.hasBlanks) {
                sqlQuery = replaceMapInString(sqlQuery, customSqlBlanks.collectEntries { [it.key, it.value] })
            }
            insertQuery += "INSERT INTO GTT_QUERY_SETS (SET_ID,CUSTOM_SQL,SET_OPERATOR,GROUP_ID,QUERY_FLAG,PARENT_GROUP_ID) values (${sqlGenIDDTO.value},${sqlQuery ? "'${sqlQuery.replaceAll("'", "''")}'" : null},${joinOperator ? "'${joinOperator.toUpperCase()}'" : null},${parent ?: 0},null,${parent ?: 0}); \n"
        } else if (superQuery?.queryType == QueryTypeEnum.SET_BUILDER) {
            Map dataMap = (new JsonSlurper()).parseText(superQuery.JSONQuery)
            Map allMap = dataMap.all
            List containerGroupsList = allMap.containerGroups
            insertQuery = insertQuery + insertSqlStatementFromQuerySetStatement(isForeground,containerGroupsList, sqlGenIDDTO, new SqlGenIDDTO(), 0, 0, joinOperator, blanks, customSqlBlanks, poiInputParams, locale)
        }
        insertQuery += ""
        return insertQuery
    }

    private String insertSqlStatementFromQuerySetStatement(
           boolean isForeground=false, def data, SqlGenIDDTO sqlGenIDDTO, SqlGenIDDTO parentSqlGenIDTO, Integer parent, Integer index, String joinOperator, List<ParameterValue> blanks, List<ParameterValue> customSqlBlanks, Set<ParameterValue> poiInputParams, Locale locale) {
        if (data instanceof Map && data.expressions) {
            parentSqlGenIDTO.value = parentSqlGenIDTO.value + 1
            String groupInsert = "INSERT INTO GTT_QUERY_SETS (SET_ID,CUSTOM_SQL,SET_OPERATOR,GROUP_ID,QUERY_FLAG,PARENT_GROUP_ID) values (${sqlGenIDDTO.value},null,${data.keyword ? "'${data.keyword.toUpperCase()}'" : null},${parentSqlGenIDTO.value},null,${parent}) ;\n"
            sqlGenIDDTO.value = sqlGenIDDTO.value + 1
            return groupInsert + insertSqlStatementFromQuerySetStatement(isForeground,data.expressions, sqlGenIDDTO, parentSqlGenIDTO, parentSqlGenIDTO.value, 0, data.keyword, blanks, customSqlBlanks, poiInputParams, locale)
        } else {
            if (data instanceof List) {
                String query = ""
                data.eachWithIndex { val, i ->
                     sqlGenIDDTO.value = sqlGenIDDTO.value + 1
                    query = query + insertSqlStatementFromQuerySetStatement(isForeground,val, sqlGenIDDTO, parentSqlGenIDTO, parent, i, joinOperator, blanks, customSqlBlanks, poiInputParams, locale)
                }
                return query
            } else {
                sqlGenIDDTO.value = sqlGenIDDTO.value + 1
                return insertQueriesDataToTempTable(isForeground,queryService.queryDetail(data.query as Long), sqlGenIDDTO, joinOperator, parent, blanks, customSqlBlanks, poiInputParams, locale, false)
            }
        }
    }

    String buildQueryFromJSONQuery(boolean isForeground=false,String JSONQuery, SqlGenIDDTO sqlGenIDDTO, int parent, String joinOperator,
 boolean hasBlanks, List blanks, Set<ParameterValue> poiInputParams, Locale locale, boolean nonValidCases, boolean isTemplateQuery = false, boolean deletedCases = false) {
        Map dataMap = (new JsonSlurper()).parseText(JSONQuery)
        Map allMap = dataMap.all
        List containerGroupsList = allMap.containerGroups
        String query = "INSERT INTO GTT_QUERY_SETS (SET_ID,CUSTOM_SQL,SET_OPERATOR,GROUP_ID,QUERY_FLAG,PARENT_GROUP_ID) values ( ${sqlGenIDDTO.value},null,${joinOperator ? "'${joinOperator.toUpperCase()}'" : null},${parent}, ${nonValidCases ? (deletedCases ? 6 : 1) : (isTemplateQuery ? 2 : null)},${parent}); \n"
        query = query + insertSqlStatementFromQueryBuilderStatement(isForeground,containerGroupsList, sqlGenIDDTO.value, new SqlGenIDDTO(), 0, 0, null, hasBlanks, blanks, poiInputParams, locale)
        return query
    }

    private String insertSqlStatementFromQueryBuilderStatement(boolean isForeground = false,
                                                               def data, Integer setId, SqlGenIDDTO sqlGenIDDTO, int parent, Integer index, String joinOperator, boolean hasBlanks,
                                                               List<ParameterValue> blanks, Set<ParameterValue> poiInputParams, Locale locale) {
        if (data instanceof Map && data.expressions) {
            sqlGenIDDTO.value = sqlGenIDDTO.value + 1
            String groupInsert = "INSERT INTO GTT_QUERY_DETAILS (SET_ID,FIELD_ID,JAVA_VARIABLE,FIELD_OPERATOR,FIELD_VALUES,GROUP_ID,GROUP_OPERATOR,CUSTOM_INPUT,PARENT_GROUP_ID) values ($setId,null,null,null, null,${sqlGenIDDTO.value},${data.keyword ? ("'${data.keyword.toUpperCase()}'") : null},null,${parent}) ;\n"
            return groupInsert + insertSqlStatementFromQueryBuilderStatement(isForeground,data.expressions, setId, sqlGenIDDTO, sqlGenIDDTO.value, 0, data.keyword, hasBlanks, blanks, poiInputParams, locale)
        } else {
            if (data instanceof List) {
                String query = ""
                data.eachWithIndex { val, i ->
                    query = query + insertSqlStatementFromQueryBuilderStatement(isForeground,val, setId, sqlGenIDDTO, parent, i, joinOperator, hasBlanks, blanks, poiInputParams, locale)
                }
                return query
            } else {
                //TODO CUSTOM_INPUT LOGIC
                String customInput = null
                if (hasBlanks && (TextUtils.isEmpty(data.value) || data.value == "null") || data.value.toString().matches(Constants.POI_INPUT_PATTERN_REGEX)) {
                    customInput = data.value
                    ParameterValue parameterValue = blanks ? blanks?.get(0) : null
                    if (parameterValue) {
                        if (parameterValue.value) {
                            data.value = parameterValue.value
                        }
                        blanks.remove(parameterValue)
                    }
                }
                data.value = normalizeValue(data.field, data.op, data.value, locale)
                if (customInput && data.value && poiInputParams*.key.contains(customInput)) {
                    ParameterValue parameterValue = poiInputParams.find { it.key == customInput }
                    parameterValue.value = data.value
                }
                String datasheetValues = null
                if (data.field == "dvListednessReassessQuery" && data.RLDS) {
                    datasheetValues = "'" + data.RLDS.split(";")*.replaceAll("'", "''").join("'~!@#@!~'") + "'"
                }
                String outSql = ""
                String apostrophe = "";
                if (data.value?.size() > 3999) {
                    data.value.split("(?<=\\G.{3999})").eachWithIndex { String part, int i ->
                        String entry
                        if (apostrophe == "'")
                            entry = part.substring(1)
                        else
                            entry = part

                        if (part[part.length() - 1] == "'" && part[part.length() - 2] != "'") {
                            apostrophe = "'"
                            entry = entry + "'"
                        } else {
                            apostrophe = ""
                        }


                        if (i == 0)
                            outSql = "INSERT INTO GTT_QUERY_DETAILS (SET_ID,FIELD_ID,JAVA_VARIABLE,FIELD_OPERATOR,FIELD_VALUES,GROUP_ID,GROUP_OPERATOR,CUSTOM_INPUT,PARENT_GROUP_ID,ADDL_PARAMS) values (${setId},$index,'${data.field}','${data.op?.toUpperCase()}','${entry}',${parent},null,${customInput ? "'${customInput}'" : null},${parent},${datasheetValues ? "'${datasheetValues.replaceAll("'", "''")}'" : null}) returning ROWID into lastInsertedRow; \n"
                        else
                            outSql += "UPDATE GTT_QUERY_DETAILS SET FIELD_VALUES=FIELD_VALUES||'${entry}' WHERE ROWID=lastInsertedRow;\n "
                    }
                } else {
                    outSql = "INSERT INTO GTT_QUERY_DETAILS (SET_ID,FIELD_ID,JAVA_VARIABLE,FIELD_OPERATOR,FIELD_VALUES,GROUP_ID,GROUP_OPERATOR,CUSTOM_INPUT,PARENT_GROUP_ID,ADDL_PARAMS) values (${setId},$index,'${data.field}','${data.op?.toUpperCase()}', ${data.value ? "'${data.value}'" : null},${parent},null,${customInput ? "'${customInput}'" : null},${parent},${datasheetValues ? "'${datasheetValues.replaceAll("'", "''")}'" : null}); \n"
                }
                return outSql
            }
        }
    }

    private String normalizeValue(String field, String op, String value, Locale locale) {
        if (op in QueryOperatorEnum.valuelessOperators*.name()) {
            value = null //fix for valueless operator contatining operator as value
        }
        if (value) {
            ReportField reportField = ReportField.findByNameAndIsDeleted(field, false)
            if (reportField && reportField.isDate()) {
                if (op in QueryOperatorEnum.numericValueDateOperators*.name()) {
                    return value
                }
                if(value?.contains('<') && value?.contains('>') ){
                    return "''" + value + "''"
                }
                String fieldDateFormat = reportField.getDateFormat('en')
                value = !value.contains(':') && fieldDateFormat.contains(':') ? (value + " 00:00:00") : value
                String dateFormat = value.contains(":") ? DATETIME_FMT : DATE_FMT
                String parseDateFormat = value.contains("/") ? DATE_FORMAT : fieldDateFormat
                return DateUtil.parseDate(value, parseDateFormat)?.format(dateFormat)
            }
            if (op in [QueryOperatorEnum.EQUALS.name(), QueryOperatorEnum.NOT_EQUAL.name()] && value.indexOf(';') != -1) {
                value = "'" + value.split(";")*.replaceAll("'", "''").join("'~!@#@!~'") + "'"
            } else if (reportField?.isString()) {
                value = "'" + value + "'"
            }
            return value.replaceAll("'", "''")
        }
        return value
    }


    public String selectedFieldsCustomProcedures(ReportTemplate reportTemplate, SuperQueryDTO query, int execPosition) {
        String returnString = ""
        if (execPosition == 1 || execPosition == 2) {
            String proc = ""
            List queryFields = []
            Map dataMap = query?.JSONQuery ? (new JsonSlurper()).parseText(query.JSONQuery) : [:]
            Map allMap = dataMap?.all
            List containerGroupsList = allMap?.containerGroups
            for (int i = 0; i < containerGroupsList?.size(); i++) {
                Map groupMap = containerGroupsList[i]
                returnJsonField(groupMap, queryFields)
            }

            if (execPosition == 1) {
                queryFields.each { field ->
                    proc = ReportField?.findByName(field)?.preQueryProcedure
                    if ((proc ? 1 : 0) == 1 && !returnString?.toLowerCase().contains(proc.toLowerCase())) {
                        returnString += proc + ";"
                    }
                }
            }

            if (execPosition == 2) {
                queryFields.each { field ->
                    proc = ReportField?.findByName(field)?.postQueryProcedure
                    if ((proc ? 1 : 0) == 1 && !returnString?.toLowerCase().contains(proc.toLowerCase())) {
                        returnString += proc + ";"
                    }
                }
            }

        }

        if (execPosition == 3) {
            reportTemplate = GrailsHibernateUtil.unwrapIfProxy(reportTemplate)
            reportTemplate?.getAllSelectedFieldsInfo()?.each { ReportFieldInfo rf ->
                if ((rf?.reportField?.preReportProcedure ? 1 : 0) == 1 && !returnString?.toLowerCase().contains(rf?.reportField?.preReportProcedure.toLowerCase())) {
                    returnString += rf.reportField.preReportProcedure + ";"
                }
            }
        }
        if (returnString) {
            returnString = "Begin " + returnString + " End;"
        }
        return returnString
    }

    String setReassessContextForTemplate(Configuration configuration, ExecutedConfiguration executedConfiguration, boolean hasQuery, boolean isViewSql) {
        String procedureCall = "";
        ReassessListednessEnum reassessListednessEnum = configuration.template.reassessListedness
        String reassessDate = ""
        String queryTableFlag = "1"
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FMT)
        Date endDate = configuration.alertDateRangeInformation.dateRangeEndAbsolute
        List<Date> minMaxDate = isViewSql ? executedConfiguration?.executedAlertDateRangeInformation?.getReportStartAndEndDate() : configuration?.alertDateRangeInformation?.getReportStartAndEndDate()
        String minStartDate = minMaxDate.first()?.format(DATE_FMT)

        if (reassessListednessEnum == ReassessListednessEnum.BEGINNING_OF_THE_REPORTING) {
            reassessDate = minStartDate
        } else if (reassessListednessEnum == ReassessListednessEnum.END_OF_THE_REPORTING_PERIOD) {
            reassessDate = sdf.format(endDate)
        }

        String datasheet = ""
        def configTemplate = configuration.template
        configTemplate = GrailsHibernateUtil.unwrapIfProxy(configTemplate) //https://github.com/grails/grails-data-mapping/issues/1468
        List<ReportFieldInfo> templateFields = configTemplate.getAllSelectedFieldsInfo()
        if (templateFields) {
            templateFields.datasheet.each {
                if (it) {
                    datasheet += it + ","
                }
            }
        }
        if (!hasQuery) {
            queryTableFlag = "0"
        }
        if (datasheet.length() > 0) {
            datasheet = datasheet.substring(0, datasheet.length() - 1)
            procedureCall = """{call pkg_reassess_listedness.p_report('${datasheet}','${reassessDate}',${queryTableFlag})}"""
        }

        return procedureCall
    }

    private def returnJsonField(Map groupMap, def fieldList) {
        def expressionElements = groupMap?.expressions

        if (expressionElements) {
            for (int i = 0; i < expressionElements?.size(); i++) {
                def expElement = expressionElements[i]

                if (expElement.field) {
                    fieldList.add(expElement.field)
                } else {
                    returnJsonField(expElement, fieldList)
                }
            }
        }
    }

    // Reassess Listedness for query
    public List<String> setReassessContextForQuery(Configuration configuration, SuperQueryDTO superQueryDTO) {
        List<String> procedureCall = [];
        if (superQueryDTO) {
            if (superQueryDTO?.queryType == QueryTypeEnum.QUERY_BUILDER) {
                procedureCall.add(getSingleQueryProcedure(superQueryDTO, configuration, 0))
            } else if (superQueryDTO?.queryType == QueryTypeEnum.SET_BUILDER) {
                int reassessIndex = 0
                superQueryDTO.queries.each {
                    if (it.queryType == QueryTypeEnum.QUERY_BUILDER) {
                        procedureCall.add(getSingleQueryProcedure(it, configuration, reassessIndex))
                        reassessIndex++
                    }
                }
            }
        }
        return procedureCall
    }

    private String getSingleQueryProcedure(def query, Configuration configuration, int reassessIndex) {
        String procedureCall = ""
        ReassessListednessEnum reassessListednessEnum = query.reassessListedness
        String reassessDate = ""
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FMT)
        Date startDate = configuration?.alertDateRangeInformation?.getReportStartAndEndDate()[0]
        Date endDate = configuration?.alertDateRangeInformation?.getReportStartAndEndDate()[1]

        if (reassessListednessEnum == ReassessListednessEnum.BEGINNING_OF_THE_REPORTING) {
            reassessDate = sdf.format(startDate)
        } else if (reassessListednessEnum == ReassessListednessEnum.END_OF_THE_REPORTING_PERIOD) {
            reassessDate = sdf.format(endDate)
        }

        String datasheet = ""
        JSONObject queryJSON = JSON.parse(query.JSONQuery)
        queryJSON.all.containerGroups.expressions.flatten().each { expression ->
            if (expression.field == "dvListednessReassess") {
                expression.RLDS.split(";").each {
                    datasheet += it + ","
                }
            }
        }

        if (datasheet.length() > 0) {
            datasheet = datasheet.substring(0, datasheet.length() - 1)
            procedureCall = """{call pkg_reassess_listedness.p_query(${reassessIndex},'${datasheet}','${reassessDate}')}"""
        }

        return procedureCall
    }

    // Generate the SQL which represents the transformation and retrieval of data based on the Configuration.reportTemplate
    public String generateReportSQL(TemplateQuery templateQuery, boolean hasQuery, ReportTemplate template) {

        List<ReportField> selectedFieldsOrg = null
        template = GrailsHibernateUtil.unwrapIfProxy(template)
        if (template.templateType == TemplateTypeEnum.CASE_LINE) {
            CaseLineListingTemplate cllTemplate = (CaseLineListingTemplate) template
            def selectedFieldInfoList = cllTemplate.getAllSelectedFieldsInfo()
            selectedFieldsOrg = selectedFieldInfoList?.reportField
        } else {
            selectedFieldsOrg = template.getAllSelectedFieldsInfo()?.reportField
        }

        List<ReportField> selectedFields = new ArrayList<ReportField>()
        int dsReAssessCount = 1

        selectedFieldsOrg.each { ReportField rf ->
            if (rf.sourceColumn.reportItem == "DCEAL_REASSESS_LISTEDNESS") {
                String key = "GDR_LISTEDNESS_DS_" + dsReAssessCount
                selectedFields.add(ReportField.findBySourceColumn(SourceColumnMaster.findByReportItem(key)))
                dsReAssessCount++;
            } else {
                selectedFields.add(rf)
            }
        }

        Configuration config = templateQuery.report
        def selectClause = ""
        def String lmTableJoins = ""
        boolean addedQuerying = false
        boolean isClobSelected = false
        String orderBy = ""
        def colNameList = []
        def commaTabList = []
        def tabJoin = []
        def tabJoinColList = [:]
        String partitionByStr = ""
        int iloop = 0
        Boolean excludeFollowUp = config.excludeFollowUp
        Boolean includeLockedVersion = config.includeLockedVersion
        def boolean useDistinct = false

        // get comma-separated value in CLL
        def flagSequence = []
        if (template.templateType == TemplateTypeEnum.CASE_LINE) {
            CaseLineListingTemplate cllTemplate = (CaseLineListingTemplate) template

            cllTemplate.getAllSelectedFieldsInfo().eachWithIndex { reportFieldInfo, i ->
                if (reportFieldInfo?.commaSeparatedValue) {
                    flagSequence.add(i)
                }
            }
        }
        // get custom expression list
        def customExpressionObject = template.getAllSelectedFieldsInfo().customExpression
        def customExpressionSequence = []
        template.getAllSelectedFieldsInfo().eachWithIndex { rf, index ->
            if (rf?.customExpression) {
                customExpressionSequence.add(index)
            }
        }

        //select clause  and LM_ table join creation

        //TODO: Manually added these if we need case_event and case_product. Removed for now.
        def product_event_seq = ""
        def productEventSelectedFields = selectedFields
        if (templateQuery.queryLevel == QueryLevelEnum.PRODUCT) {
            productEventSelectedFields += [ReportField.findByName("productProductName")]
            product_event_seq = " and cp.prod_seq_num = caseList.prod_seq_num"
        } else if (templateQuery.queryLevel == QueryLevelEnum.PRODUCT_EVENT) {
            productEventSelectedFields += [ReportField.findByName("eventPrefTerm"), ReportField.findByName("productProductName")]
            product_event_seq = " and ce.ae_seq_num = caseList.event_seq_num and cp.prod_seq_num = caseList.prod_seq_num"
        } else if (templateQuery.queryLevel == QueryLevelEnum.SUBMISSION) {
            product_event_seq = " and caseList.PROCESSED_REPORT_ID = cmr.PROCESSED_REPORT_ID "
        }

        selectedFields.each { ReportField rf ->
            if (flagSequence.contains(iloop)) {
                commaTabList.add(rf.sourceColumn.tableName.tableName)
                useDistinct = true
            }
            if (!isClobSelected && rf.sourceColumn.columnType == "C") {
                isClobSelected = true
            }
            iloop++
        }

        // from clause creation
        def tableName
        def tempTableNames = []
        String caseTableFromClause = ""

        //find table list
        // QueryLevel
        productEventSelectedFields.each { ReportField rf ->
            if (tableName != rf.sourceColumn.tableName.tableName) {
                tempTableNames.add(rf.sourceColumn.tableName.tableName)
                tableName = rf.sourceColumn.tableName.tableName
            }
        }

        if (!tempTableNames.contains("V_CASE_INFO")) {
            tempTableNames.add("V_CASE_INFO")
        }

        if (templateQuery.queryLevel == QueryLevelEnum.SUBMISSION) {
            tempTableNames.add(SourceTableMaster.findByTableAlias(QUERY_LEVEL_SUBMISSION_CMR_TABLE_ALIAS).tableName)
        }

        // construct SQL after finding relation between case tables
        def Integer loopCounter = 0
        boolean recursiveFlag = true

        while (recursiveFlag && loopCounter < 5) {
            def relTableJoinMapping = CaseColumnJoinMapping.createCriteria()
            def relCaseTableRelation = relTableJoinMapping.list {
                inList("tableName.tableName", tempTableNames)
                order("mapTableName.tableName", "asc")
            }
            recursiveFlag = false
            relCaseTableRelation.each { CaseColumnJoinMapping rf ->

                if (tableName == rf.mapTableName.tableName) {
                    if (!tempTableNames.contains(rf.mapTableName.tableName)) {
                        tempTableNames.add(rf.mapTableName.tableName)
                        recursiveFlag = true
                    }
                }
                tableName = rf.mapTableName.tableName

            }
            loopCounter++
        }

        loopCounter = 0
        // sort tables in join order
        def tableJoinOrder = SourceTableMaster.createCriteria()
        def tableList = tableJoinOrder.list {
            inList("tableName", tempTableNames)
            order("caseJoinOrder", "asc")
        }
        tableList.each { SourceTableMaster tabRec ->
            def tableJoinMapping = CaseColumnJoinMapping.createCriteria()
            def caseTableRelation = tableJoinMapping.list {
                inList("mapTableName.tableName", tempTableNames)
                eq("tableName.tableName", tabRec.tableName)
                order("mapColumnName", "asc")
            }
            if (loopCounter > 0) {
                caseTableFromClause += (tabRec.caseJoinType == "O" ? " Left " : "") + " join "
            }
            caseTableFromClause += tabRec.tableName + " " + tabRec.tableAlias
            if (caseTableRelation.size() > 0) {
                caseTableFromClause += " on ("
            }
            def int iterations = 0
            partitionByStr = ""
            caseTableRelation.each { CaseColumnJoinMapping rf ->
                if (!commaTabList.contains(rf.mapTableName.tableName) || rf.mapTableName.tableName == "V_CASE_INFO") {
                    if (iterations > 0) {
                        caseTableFromClause += " AND "
                    }
                    iterations++
                    caseTableFromClause += rf.mapTableName.tableAlias + "." + rf.mapColumnName + " = " + rf.tableName.tableAlias + "." + rf.columnName
                    if (rf.tableName.versionedData == "V" && rf.mapTableName.versionedData == "V") {
                        caseTableFromClause += " and " + rf.mapTableName.tableAlias + ".version_num = " + rf.tableName.tableAlias + ".version_num"
                    }
                    if (rf.mapTableName.hasEnterpriseId == 1) {
                        caseTableFromClause += " AND " + rf.mapTableName.tableAlias + ".TENANT_ID = " + rf.tableName.tableAlias + ".TENANT_ID"
                    }
                    if (partitionByStr != "") partitionByStr += ","

                    partitionByStr += rf.tableName.tableAlias + "." + rf.columnName;
                }
            }
            if (caseTableRelation.size() > 0) {
                caseTableFromClause += " ) "
            }
            tabJoinColList.put(tabRec.tableName, partitionByStr)
            loopCounter++
        }

        def tempString
        loopCounter = 0
        def lmTableAlias = ""

        selectedFields.each { ReportField rf ->
            if (rf?.sourceColumn?.lmDecodeColumn) {
                if (rf?.sourceColumn?.lmTableName?.tableName && rf?.sourceColumn?.tableName?.tableAlias) {
                    lmTableAlias = rf.sourceColumn.tableName.tableAlias + loopCounter // unique alias for LM table
                    lmTableJoins += (rf.sourceColumn.lmJoinType == "O" ? " Left " : "") + " Join " + rf.sourceColumn.lmTableName.tableName + " "
                    lmTableJoins += lmTableAlias + " on (" + rf.sourceColumn.tableName.tableAlias + "."
                    lmTableJoins += rf.sourceColumn.columnName + " = " + lmTableAlias + "." + rf.sourceColumn.lmJoinColumn
                    if (rf.sourceColumn.lmTableName.hasEnterpriseId == 1) {
                        lmTableJoins += " AND " + lmTableAlias + ".TENANT_ID = " + rf.sourceColumn.tableName.tableAlias + ".TENANT_ID"
                    }
                    lmTableJoins += " ) "
                    tempString = lmTableAlias + "." + rf.sourceColumn.lmDecodeColumn
                    colNameList.add(lmTableAlias + "." + rf.sourceColumn.lmDecodeColumn)
                }
            } else {
                tempString = rf.sourceColumn.tableName.tableAlias + "." + rf.sourceColumn.columnName
                colNameList.add(tempString)
            }

            // for comma-separated values
            if (flagSequence.contains(loopCounter)) {
                String partitioningClauseStr = tabJoinColList[rf.sourceColumn.tableName.tableName]
                if (partitioningClauseStr == "") {
                    partitioningClauseStr = " cm.Case_id "
                }
                if (rf.sourceColumn.columnType == "N") {
                    selectClause += " replace( regexp_replace( regexp_replace((LISTAGG(to_char(" + tempString + "), ',') WITHIN GROUP (ORDER BY " + tempString + ") OVER (PARTITION BY " +
                            partitioningClauseStr + ")),',\\s*',',') ,'([^,]+)(,\\1)+', '\\1'),',',', ')  AS " + rf.sourceColumn.columnName + loopCounter + ","

                } else if (rf.sourceColumn.columnType == "D") {
                    selectClause += " replace( regexp_replace( regexp_replace((LISTAGG(to_char(" + tempString + ", 'dd-MMM-yyyy'), ',') WITHIN GROUP (ORDER BY " + tempString + ") OVER (PARTITION BY " +
                            partitioningClauseStr + ")),',\\s*',',') ,'([^,]+)(,\\1)+', '\\1'),',',', ')  AS " + rf.sourceColumn.columnName + loopCounter + ","

                } else {
                    selectClause += " replace( regexp_replace( regexp_replace((LISTAGG(" + tempString + ", ',') WITHIN GROUP (ORDER BY " + tempString + ") OVER (PARTITION BY " +
                            partitioningClauseStr + ")),',\\s*',',') ,'([^,]+)(,\\1)+', '\\1'),',',', ')  AS " + rf.sourceColumn.columnName + loopCounter + ","

                }
            } else if (customExpressionSequence.contains(loopCounter)) {
                selectClause += customExpressionObject[loopCounter] + ","
            } else {
                if (rf.sourceColumn.concatField == "1") {
                    selectClause += "REPLACE_SEPARATOR(" + tempString + ",',\n')" + " AS " + rf.sourceColumn.columnName + loopCounter + ","
                } else {
                    selectClause += tempString + " AS " + rf.sourceColumn.columnName + loopCounter + ","
                }

            }
            loopCounter++
        }

        orderBy = "order by "
        template.sortInfo().each {
            orderBy += colNameList.get(it[0]) + " " + it[1] + ","
        }
        if (orderBy == "order by ") { // if no sort order
            orderBy = ""
        } else {
            orderBy = orderBy.substring(0, orderBy.length() - 1) // remove the last comma
        }

        if (useDistinct && !isClobSelected) {
            tempString = " distinct "
        } else {
            tempString = " "
        }

        String result = "select ${tempString} ${selectClause.getAt(0..selectClause.length() - 2)} from ${caseTableFromClause} ${lmTableJoins}"

        if (hasQuery) {
            result += """ where exists
                        (select 1 from $CASE_LIST_TABLE_NAME caseList
                            where cm.case_id = caseList.case_id and
                                  cm.version_num = caseList.dlp_revision_number
                                  and caseList.TENANT_ID = cm.TENANT_ID
                        $product_event_seq)"""

        } else {
            result += """ where exists
                        (select 1 from $VERSION_TABLE_NAME ver
                            where cm.case_id = ver.case_id and
                                  cm.version_num = ver.dlp_revision_number
                                  and ver.TENANT_ID = cm.TENANT_ID )"""
        }

        return "$result ${orderBy}"
    }

    String generateCaseLineListingSql(sql, Long execConfigId) {
        log.info("generateCaseLineListingSql called.")
        String reportSql
        sql?.call("{? = call pkg_create_report_sql.p_main()}", [Sql.VARCHAR]) { String sqlValue ->
            reportSql = sqlValue
        }
        reportSql
    }

    String generateCustomReportSQL(Long executedConfigId, String selectedDatasource, boolean isEventGroup, String dataSource = null, boolean isAws = false, boolean isHierarchyAlert = false, String eventHierarchy = "", String productId = "") {
        String sieveAnalysisQueryName = Holders.config.pvsignal.sieveAnalysisQueryName
        SuperQueryDTO sieveAnalysisQuery = queryService.queryDetailByName(sieveAnalysisQueryName)
        if (sieveAnalysisQuery && ((selectedDatasource.startsWith(Constants.DataSource.PVA) && Holders.config.signal.sieveAnalysis.safetyDB) || (selectedDatasource.startsWith(Constants.DataSource.FAERS) && Holders.config.signal.sieveAnalysis.faers) || (selectedDatasource.startsWith(Constants.DataSource.VAERS) && Holders.config.signal.sieveAnalysis.vaers) || (selectedDatasource.startsWith(Constants.DataSource.VIGIBASE) && Holders.config.signal.sieveAnalysis.vigibase))) {
            return SignalQueryHelper.agg_count_sql_sv(executedConfigId, isEventGroup, [], dataSource, isAws, isHierarchyAlert, eventHierarchy)
        }
        return SignalQueryHelper.agg_count_sql(executedConfigId, isEventGroup, [], dataSource, isAws, isHierarchyAlert, eventHierarchy, productId)
    }

    // For QuerySet
    private String convertQueryToWhereClause(String index, TemplateQuery templateQuery) {
        def selectFields = ""
        QueryLevelEnum queryLevel = templateQuery.queryLevel
        if (queryLevel == QueryLevelEnum.PRODUCT || queryLevel == QueryLevelEnum.PRODUCT_EVENT || templateQuery.report.productSelection) {
            selectFields += " prod_seq_num, prod_version_num,"
        }
        if (queryLevel == QueryLevelEnum.PRODUCT_EVENT || templateQuery.report.eventSelection) {
            // query must pass case id, product sequence number and product event sequence number (PVR-117)
            selectFields += " AE_REC_NUM,"
        }
        if (queryLevel == QueryLevelEnum.SUBMISSION) {
            selectFields += " PROCESSED_REPORT_ID,"
        }
        return "SELECT DISTINCT TENANT_ID, CASE_ID,${selectFields} VERSION_NUM FROM ${SET_TABLE_NAME}_${index}"
    }

    // helper method, don't call this
    private def buildCriteriaFromGroup(
            Map groupMap, // A group has at most 2 objects: 1 is a list of expressions and 2 is the keyword, if we have one
            Date nextRunDate, List<ReportField> fields, String timezone, LinkedHashMap lmTables, LinkedHashMap usedLMTables,
            Set<QueryExpressionValue> blanks, int reassessIndex, EvaluateCaseDateEnum evaluateCaseDateEnum, Date asOfCaseVersionDate) {
        String result = ""
        List<ReportField> reportFields = fields;
        boolean hasKeyword = groupMap.keyword;
        if (groupMap.expressions) {
            List expressionsList = groupMap.expressions;
            for (int i = 0; i < expressionsList.size(); i++) {
                if (hasKeyword && i > 0) {
                    if (result) {
                        result += " ${groupMap.keyword} "
                    }
                }
                def executed = buildCriteriaFromGroup(expressionsList[i],
                        nextRunDate, fields, timezone, lmTables, usedLMTables, blanks, reassessIndex, evaluateCaseDateEnum, asOfCaseVersionDate)

                if (executed.result == '1=1' || executed.result == '(1=1)') {
                    def logicalAppender = "${groupMap.keyword}"
                    if (logicalAppender != "or") {
                        result += "(${executed.result})";
                    } else {

                        def orClauseKeyword = " or "

                        //A check to determine of result has 'or' and this 'or' keyword is in the last.
                        if (result.indexOf(orClauseKeyword) != -1 && result?.trim().split(orClauseKeyword)?.size() == 1) {
                            result = result.split("or")[0]
                        }
                    }
                } else {
                    result += "(${executed.result})";
                }

                executed.reportFields.each() {
                    if (!reportFields.contains(it)) {
                        reportFields.add(it)
                    }
                }
            }
        } else {
            if (groupMap.keyword) {
                result += " ${groupMap.keyword} "
            }

            ReportField reportField = ReportField.findByName(groupMap.field)
            // Extra Values
            HashMap extraValues = [:]
            // Re-assess Listedness
            if (groupMap.containsKey(RLDS)) {
                extraValues.put(RLDS, groupMap.get(RLDS))
            }
            result += convertExpressionToWhereClause(
                    new Expression(reportField: reportField, value: groupMap.value,
                            operator: groupMap.op as QueryOperatorEnum),
                    nextRunDate, timezone, lmTables, usedLMTables, blanks, extraValues, reassessIndex, evaluateCaseDateEnum, asOfCaseVersionDate);
            if (!reportFields.contains(reportField)) {
                reportFields.add(reportField)
            }
        }
        return [result: result, reportFields: reportFields, lmTables: lmTables]
    }

    private String convertExpressionToWhereClause(Expression e, Date nextRunDate, String timezone, LinkedHashMap lmTables,
                                                  LinkedHashMap usedLMTables, Set<QueryExpressionValue> blanks,
                                                  HashMap extraValues, int reassessIndex, EvaluateCaseDateEnum evaluateCaseDateEnum, Date asOfCaseVersionDate) {
        String result = ""
        String columnName = ""

        if (e.value == null || e.value.equals("")) {
            ParameterValue qev = blanks.find { qev ->
                e.reportField == qev.reportField && e.operator == qev.operator
            }

            if (qev) {
                if (qev.value) {
                    e.value = qev.value
                } else {
                    // PVR-1628: If no value is selected for parameter in parametrized query in report configuration then all values should be returned
                    e.value = NO_VALUE_SELECTED
                }
                blanks.remove(qev)
            } else {
                //If no parameter value is selected then we set value NO_VALUE_SELECTED
                e.value = NO_VALUE_SELECTED
            }
        }

        if (e.reportField.dataType == PartialDate.class) {
            result = generatePartialDateWhereClause(e, nextRunDate, timezone)
            return result
        } else if (e.reportField.sourceColumn.lmDecodeColumn) {
            int dynamicAlias = 0
            if (lmTables.containsKey(e.reportField.sourceColumn.lmTableName.tableAlias)) {
                dynamicAlias = (int) lmTables.getAt(e.reportField.sourceColumn.lmTableName.tableAlias)
            }
            if (!usedLMTables.containsKey(e.reportField.sourceColumn.reportItem)) {
                dynamicAlias++
                usedLMTables[(e.reportField.sourceColumn.reportItem)] = dynamicAlias
                lmTables[(e.reportField.sourceColumn.lmTableName.tableAlias)] = dynamicAlias
            }
            columnName = "${e.reportField.sourceColumn.lmTableName.tableAlias}_${dynamicAlias}.${e.reportField.sourceColumn.lmDecodeColumn}"
        } else {
            columnName = "${e.reportField.sourceColumn.tableName.tableAlias}.${e.reportField.sourceColumn.columnName}"
        }

        boolean isClobColumn = false
        if (e.reportField.sourceColumn.columnType == 'C') {
//            columnName = "dbms_lob.substr(${columnName},4000,1)"
            isClobColumn = true
        }

        // PVR-1355 Akash
        // Re-assess Listedness needs to be completed in this method.
        if (!extraValues.isEmpty()) {
            String RLDSValue = extraValues.get(RLDS)
            if (RLDSValue) {
                /*
                    Add code here to process Re-assess Listedness Datasheet.

                    e.value is the value of the listedness (i.e. Listed, Unlisted, Unknown).

                    RLDSValue is the value of the datasheet (i.e. JPN, CCDS, C1).

                    RLDSValue cannot be blank.

                    Store the string in result, which is returned.
                 */

                if (e.operator == QueryOperatorEnum.IS_EMPTY) {
                    result = "${columnName} IS NULL"
                } else if (e.operator == QueryOperatorEnum.IS_NOT_EMPTY) {
                    result = "${columnName} IS NOT NULL"
                } else if (e.reportField.isString()) {
                    //First, check our custom operator. String comparisons are case insensitive.
                    String listednessList = ""
                    List tokens1 = e.value.split(/;/) as List
                    tokens1.eachWithIndex { it, index ->
                        listednessList += "UPPER(\'${it}\'),"
                    }

                    String datasheetList = ""
                    List tokens2 = RLDSValue.split(/;/) as List
                    tokens2.eachWithIndex { it, index ->
                        datasheetList += "UPPER(\'${it}\'),"
                    }

                    if (e.operator == QueryOperatorEnum.EQUALS) {
                        result = "UPPER(${columnName}) in (${listednessList.substring(0, listednessList.length() - 1)})"
                        // check listedness
                        result += " and UPPER(gdrq.datasheet_name) in (${datasheetList.substring(0, datasheetList.length() - 1)})"
                        // check datasheet
                        result += " and gdrq.row_num = ${reassessIndex}" // check sequence number

                    } else if (e.operator == QueryOperatorEnum.NOT_EQUAL) {
                        result = "UPPER(${columnName}) not in (${listednessList.substring(0, listednessList.length() - 1)})"
                        // check listedness
                        result += " and UPPER(gdrq.datasheet_name) not in (${datasheetList.substring(0, datasheetList.length() - 1)})"
                        // check datasheet
                        result += " and gdrq.row_num = ${reassessIndex}" // check sequence number

                    } else if (e.operator == QueryOperatorEnum.CONTAINS) {
                        result = "UPPER(${columnName}) LIKE UPPER('%${e.value}%') AND ${columnName} IS NOT NULL"
                    } else if (e.operator == QueryOperatorEnum.DOES_NOT_CONTAIN) {
                        result = "UPPER(${columnName}) NOT LIKE UPPER('%${e.value}%') AND ${columnName} IS NOT NULL"
                    } else if (e.operator == QueryOperatorEnum.START_WITH) {
                        result = "UPPER(${columnName}) LIKE UPPER('${e.value}%') AND ${columnName} IS NOT NULL"
                    } else if (e.operator == QueryOperatorEnum.DOES_NOT_START) {
                        result = "UPPER(${columnName}) NOT LIKE UPPER('${e.value}%') AND ${columnName} IS NOT NULL"
                    } else if (e.operator == QueryOperatorEnum.ENDS_WITH) {
                        result = "UPPER(${columnName}) LIKE UPPER('%${e.value}') AND ${columnName} IS NOT NULL"
                    } else if (e.operator == QueryOperatorEnum.DOES_NOT_END) {
                        result = "UPPER(${columnName}) NOT LIKE UPPER('%${e.value}') AND ${columnName} IS NOT NULL"
                    }
                }

                return result
            }
        }

        if (e.value == NO_VALUE_SELECTED) {
            result = "1=1" // ignore that bit of query
        } else if (e.operator == QueryOperatorEnum.IS_EMPTY) {
            result = "${columnName} IS NULL"
        } else if (e.operator == QueryOperatorEnum.IS_NOT_EMPTY) {
            result = "${columnName} IS NOT NULL"
        } else if (e.reportField.isString()) {
            //First, check our custom operator. String comparisons are case insensitive.
            if (e.operator == QueryOperatorEnum.EQUALS) {
                //Second, check if we have multiselect
                if (e?.value?.indexOf(";") == -1) {
                    if (isClobColumn) {
                        result = "dbms_lob.getlength(${columnName}) = dbms_lob.getlength('${e.value}') and dbms_lob.instr(UPPER(${columnName}),UPPER('${e.value}'),1,1) > 0"
                    } else {
                        result = "UPPER(${columnName}) = UPPER('${e.value}')"
                        if (e.reportField.name == 'masterStateId' && e.value.toLowerCase() == "deleted") {
                            result += excludeDeletedCases(evaluateCaseDateEnum, asOfCaseVersionDate)
                        }
                    }
                } else {
//                    String inFromTable = ""
//                    String inColumnName = ""
//                    if (e.reportField.argusColumn.lmDecodeColumn) {
//                        inFromTable = e.reportField.argusColumn.lmTableName.tableName
//                        inColumnName = e.reportField.argusColumn.lmDecodeColumn
//                    } else {
//                        inFromTable = e.reportField.argusColumn.tableName.tableName
//                        inColumnName = e.reportField.argusColumn.columnName
//                    }

                    //Multiselect Select2
                    List tokens = e?.value?.split(/;/) as List
                    String values = ""

                    if (isClobColumn) {
                        tokens.eachWithIndex { it, index ->
                            if (index > 0) {
//                                values += " OR UPPER(${inColumnName})=UPPER('${it}')"
                                values += " OR dbms_lob.instr(UPPER(${columnName}),UPPER('${it}'),1,1) > 0"
                            } else {
                                values += " dbms_lob.instr(UPPER(${columnName}),UPPER('${it}'),1,1) > 0"
                            }
                        }

//                        inColumnName = "dbms_lob.substr(${inColumnName},4000,1)"
                    } else {
                        tokens.eachWithIndex { it, index ->
                            if (index > 0) {
                                values += " OR UPPER(${columnName}) = UPPER('${it}')"
                            } else {
                                values += "UPPER(${columnName}) = UPPER('${it}')"
                            }
                        }
                    }

                    result += values
//                    result = """UPPER(${columnName}) IN (SELECT DISTINCT UPPER(${inColumnName}) FROM ${
//                        inFromTable
//                    } WHERE ${values})"""
                }
            } else if (e.operator == QueryOperatorEnum.NOT_EQUAL) {
                //Second, check if we have multiselect
                if (e.value.indexOf(";") == -1) {
                    if (isClobColumn) {
                        result = "dbms_lob.instr(UPPER(${columnName}),UPPER('${e.value}'),1,1) = 0"
                    } else {
                        result = "UPPER(${columnName}) <> UPPER('${e.value}')"
                    }
                } else {
//                    String inFromTable = ""
//                    String inColumnName = ""
//                    if (e.reportField.argusColumn.lmDecodeColumn) {
//                        inFromTable = e.reportField.argusColumn.lmTableName.tableName
//                        inColumnName = e.reportField.argusColumn.lmDecodeColumn
//                    } else {
//                        inFromTable = e.reportField.argusColumn.tableName.tableName
//                        inColumnName = e.reportField.argusColumn.columnName
//                    }

//                    if (isClobColumn) {
//                        inColumnName = "dbms_lob.substr(${inColumnName},4000,1)"
//                    }

                    //Multiselect Select2
                    String[] tokens = e?.value?.split(/;/)
                    String values = ""

                    if (isClobColumn) {
                        tokens.eachWithIndex { it, index ->
                            if (index > 0) {
//                                values += " OR UPPER(${inColumnName})=UPPER('${it}')"
                                values += " AND dbms_lob.instr(UPPER(${columnName}),UPPER('${it}'),1,1) = 0"
                            } else {
                                values += " dbms_lob.instr(UPPER(${columnName}),UPPER('${it}'),1,1) = 0"
                            }
                        }

//                        inColumnName = "dbms_lob.substr(${inColumnName},4000,1)"
                    } else {
                        tokens.eachWithIndex { it, index ->
                            if (index > 0) {
                                values += " AND UPPER(${columnName}) <> UPPER('${it}')"
                            } else {
                                values += "UPPER(${columnName}) <> UPPER('${it}')"
                            }
                        }
                    }

                    result += values
//                    result = """UPPER(${columnName}) NOT IN (SELECT DISTINCT ${inColumnName} FROM ${
//                        inFromTable
//                    } WHERE ${values})"""
                }
            } else if (e.operator == QueryOperatorEnum.CONTAINS) {
                result = "UPPER(${columnName}) LIKE UPPER('%${e.value}%') AND ${columnName} IS NOT NULL"
            } else if (e.operator == QueryOperatorEnum.DOES_NOT_CONTAIN) {
                result = "UPPER(${columnName}) NOT LIKE UPPER('%${e.value}%') AND ${columnName} IS NOT NULL"
            } else if (e.operator == QueryOperatorEnum.START_WITH) {
                result = "UPPER(${columnName}) LIKE UPPER('${e.value}%') AND ${columnName} IS NOT NULL"
            } else if (e.operator == QueryOperatorEnum.DOES_NOT_START) {
                result = "UPPER(${columnName}) NOT LIKE UPPER('${e.value}%') AND ${columnName} IS NOT NULL"
            } else if (e.operator == QueryOperatorEnum.ENDS_WITH) {
                result = "UPPER(${columnName}) LIKE UPPER('%${e.value}') AND ${columnName} IS NOT NULL"
            } else if (e.operator == QueryOperatorEnum.DOES_NOT_END) {
                result = "UPPER(${columnName}) NOT LIKE UPPER('%${e.value}') AND ${columnName} IS NOT NULL"
            }
        } else if (e.reportField.isNumber()) {
            // TODO: Numbers can have multiple values?
            List operatorIgnoreList = [QueryOperatorEnum.IS_EMPTY, QueryOperatorEnum.IS_NOT_EMPTY, QueryOperatorEnum.EQUALS, QueryOperatorEnum.NOT_EQUAL]
            result = "${columnName} ${e.operator.value()} ${e.value} ${!(e.operator in operatorIgnoreList) ? 'AND ' + columnName + ' IS NOT NULL' : ''}"
        } else if (e.reportField.isDate()) {
            result = generateDateWhereClause(e, nextRunDate, timezone, columnName)
        }
        return result
    }

    Boolean isDeletedCasesFlagEnabled(String selectedDatasource) {
        String dataSource = ""
        if(selectedDatasource == Constants.DataSource.EUDRA || selectedDatasource == Constants.DataSource.EVDAS)
            dataSource += "EUDRA"
        else
            dataSource = selectedDatasource
        dataSource = dataSource?.toUpperCase() + '-DB'
        Sql sql = null
        String deletedCasesSql = SignalQueryHelper.deleted_cases_sql(dataSource)
        log.info("deletedCasesSql==========================================")
        log.info(deletedCasesSql)
        try {
            sql = new Sql(signalDataSourceService.getReportConnection(selectedDatasource))
            int isDeleted = sql.rows(deletedCasesSql)?.get(0)['KEY_VALUE'] ?: 0 as int
            log.info("isDeletec==========================")
            log.info(""+isDeleted)
            return isDeleted == 1
        } catch (Exception e) {
            log.error(e.printStackTrace())
        } finally {
            sql?.close()
        }
        return false
    }

    String generatePartialDateWhereClause(Expression e, Date nextRunDate, String timezone) {
        String result = ""
        String columnName = "$e.reportField.sourceColumn.tableName.tableAlias.$e.reportField.sourceColumn.columnName"
        if (e.value.matches(PARTIAL_DATE_YEAR_ONLY)) { //??-???-yyyy
            String monthAndYear = e.value.substring(6)
            String startDate = "01-JAN${monthAndYear}"
            def startDates
            def endDates

            SimpleDateFormat dateFormat = new SimpleDateFormat(PARTIAL_DATE_FMT)
            Date convertedStartDate = dateFormat.parse(startDate)
            Calendar c = Calendar.getInstance();
            c.setTime(convertedStartDate)
            c.set(Calendar.MONTH, Calendar.DECEMBER)
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
            Date convertedEndDate = c.time

            if (e.operator == QueryOperatorEnum.EQUALS) {
                startDates = RelativeDateConverter.findDay(convertedStartDate, timezone)
                endDates = RelativeDateConverter.findDay(convertedEndDate, timezone)
                result = """  ${columnName} >= TO_DATE('${convertDateToSQLDateTime(startDates[0])}', '$DATETIME_FMT_ORA') AND
                              ${columnName} <= TO_DATE('${convertDateToSQLDateTime(endDates[1])}', '$DATETIME_FMT_ORA') """
            } else if (e.operator == QueryOperatorEnum.NOT_EQUAL) {
                startDates = RelativeDateConverter.findDay(convertedStartDate, timezone)
                endDates = RelativeDateConverter.findDay(convertedEndDate, timezone)
                result = """ NOT (${columnName} >= TO_DATE('${convertDateToSQLDateTime(startDates[0])}', '$DATETIME_FMT_ORA') AND
                                  ${columnName} <= TO_DATE('${convertDateToSQLDateTime(endDates[1])}', '$DATETIME_FMT_ORA')) """
            } else if (e.operator == QueryOperatorEnum.LESS_THAN) {
                startDates = RelativeDateConverter.findDay(convertedStartDate, timezone)
                result = "${columnName} < TO_DATE('${convertDateToSQLDateTime(startDates[0])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"
            } else if (e.operator == QueryOperatorEnum.LESS_THAN_OR_EQUAL) {
                endDates = RelativeDateConverter.findDay(convertedEndDate, timezone)
                result = "${columnName} <= TO_DATE('${convertDateToSQLDateTime(endDates[1])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"
            } else if (e.operator == QueryOperatorEnum.GREATER_THAN) {
                endDates = RelativeDateConverter.findDay(convertedEndDate, timezone)
                result = "${columnName} > TO_DATE('${convertDateToSQLDateTime(endDates[1])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"
            } else if (e.operator == QueryOperatorEnum.GREATER_THAN_OR_EQUAL) {
                startDates = RelativeDateConverter.findDay(convertedStartDate, timezone)
                result = "${columnName} >= TO_DATE('${convertDateToSQLDateTime(startDates[0])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"
            } else if (e.operator == QueryOperatorEnum.YESTERDAY || e.operator == QueryOperatorEnum.LAST_MONTH ||
                    e.operator == QueryOperatorEnum.LAST_WEEK || e.operator == QueryOperatorEnum.LAST_YEAR) {
                startDates = RelativeDateConverter.(e.operator.value())(nextRunDate, 1, timezone)
                result = """ ${columnName} >= TO_DATE('${convertDateToSQLDateTime(startDates[0])}', '$DATETIME_FMT_ORA') AND
                             ${columnName} <= TO_DATE('${convertDateToSQLDateTime(startDates[1])}', '$DATETIME_FMT_ORA') AND ${columnName} IS NOT NULL"""
            } else if (e.operator == QueryOperatorEnum.LAST_X_DAYS || e.operator == QueryOperatorEnum.LAST_X_MONTHS ||
                    e.operator == QueryOperatorEnum.LAST_X_WEEKS || e.operator == QueryOperatorEnum.LAST_X_YEARS) {
                startDates = RelativeDateConverter.(e.operator.value())(nextRunDate, Integer.parseInt(e.value), timezone)
                result = """ ${columnName} >= TO_DATE('${convertDateToSQLDateTime(startDates[0])}', '$DATETIME_FMT_ORA') AND
                             ${columnName} <= TO_DATE('${convertDateToSQLDateTime(startDates[1])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"""
            }
        } else if (e.value.matches(PARTIAL_DATE_MONTH_AND_YEAR)) { //??-MMM-yyyy
            String monthAndYear = e.value.substring(2)
            String startDate = "01${monthAndYear}"
            def startDates
            def endDates

            SimpleDateFormat dateFormat = new SimpleDateFormat(PARTIAL_DATE_FMT)
            Date convertedStartDate = dateFormat.parse(startDate)
            Calendar c = Calendar.getInstance();
            c.setTime(convertedStartDate)
            c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH))
            Date convertedEndDate = c.time

            if (e.operator == QueryOperatorEnum.EQUALS) {
                startDates = RelativeDateConverter.findDay(convertedStartDate, timezone)
                endDates = RelativeDateConverter.findDay(convertedEndDate, timezone)
                result = """ ${columnName} >= TO_DATE('${convertDateToSQLDateTime(startDates[0])}', '$DATETIME_FMT_ORA') AND
                             ${columnName} <= TO_DATE('${convertDateToSQLDateTime(endDates[1])}', '$DATETIME_FMT_ORA') """
            } else if (e.operator == QueryOperatorEnum.NOT_EQUAL) {
                startDates = RelativeDateConverter.findDay(convertedStartDate, timezone)
                endDates = RelativeDateConverter.findDay(convertedEndDate, timezone)
                result = """NOT (${columnName} >= TO_DATE('${convertDateToSQLDateTime(startDates[0])}', '$DATETIME_FMT_ORA') AND
                                 ${columnName} <= TO_DATE('${convertDateToSQLDateTime(endDates[1])}', '$DATETIME_FMT_ORA'))"""
            } else if (e.operator == QueryOperatorEnum.LESS_THAN) {
                startDates = RelativeDateConverter.findDay(convertedStartDate, timezone)
                result = "${columnName} < TO_DATE('${convertDateToSQLDateTime(startDates[0])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"
            } else if (e.operator == QueryOperatorEnum.LESS_THAN_OR_EQUAL) {
                endDates = RelativeDateConverter.findDay(convertedEndDate, timezone)
                result = "${columnName} <= TO_DATE('${convertDateToSQLDateTime(endDates[1])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"
            } else if (e.operator == QueryOperatorEnum.GREATER_THAN) {
                endDates = RelativeDateConverter.findDay(convertedEndDate, timezone)
                result = "${columnName} > TO_DATE('${convertDateToSQLDateTime(endDates[1])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"
            } else if (e.operator == QueryOperatorEnum.GREATER_THAN_OR_EQUAL) {
                startDates = RelativeDateConverter.findDay(convertedStartDate, timezone)
                result = "${columnName} >= TO_DATE('${convertDateToSQLDateTime(startDates[0])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"
            } else if (e.operator == QueryOperatorEnum.YESTERDAY || e.operator == QueryOperatorEnum.LAST_MONTH ||
                    e.operator == QueryOperatorEnum.LAST_WEEK || e.operator == QueryOperatorEnum.LAST_YEAR) {
                startDates = RelativeDateConverter.(e.operator.value())(nextRunDate, 1, timezone)
                result = """${columnName} >= TO_DATE('${convertDateToSQLDateTime(startDates[0])}', '$DATETIME_FMT_ORA') AND
                            ${columnName} <= TO_DATE('${convertDateToSQLDateTime(startDates[1])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"""
            } else if (e.operator == QueryOperatorEnum.LAST_X_DAYS || e.operator == QueryOperatorEnum.LAST_X_MONTHS ||
                    e.operator == QueryOperatorEnum.LAST_X_WEEKS || e.operator == QueryOperatorEnum.LAST_X_YEARS) {
                startDates = RelativeDateConverter.(e.operator.value())(nextRunDate, Integer.parseInt(e.value), timezone)
                result = """${columnName} >= TO_DATE('${convertDateToSQLDateTime(startDates[0])}', '$DATETIME_FMT_ORA') AND
                            ${columnName} <= TO_DATE('${convertDateToSQLDateTime(startDates[1])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"""
            }
        } else if (e.value.matches(PARTIAL_DATE_FULL)) { //dd-MMM-yyyy
            result = generateDateWhereClause(e, nextRunDate, timezone, columnName)
        }
        return result
    }

    String generateDateWhereClause(Expression e, Date nextRunDate, String timezone, String columnName) {
        String result = ""
        def dates
        if (e.operator == QueryOperatorEnum.EQUALS) {
            dates = RelativeDateConverter.findDay(new Date(e.value), timezone, true)
            result = """${columnName} >= TO_DATE('${convertDateToSQLDateTime(dates[0])}', '$DATETIME_FMT_ORA') AND
                        ${columnName} <= TO_DATE('${convertDateToSQLDateTime(dates[1])}', '$DATETIME_FMT_ORA')"""
        } else if (e.operator == QueryOperatorEnum.NOT_EQUAL) {
            dates = RelativeDateConverter.findDay(new Date(e.value), timezone, true)
            result = """NOT (${columnName} >= TO_DATE('${convertDateToSQLDateTime(dates[0])}', '$DATETIME_FMT_ORA') AND
                             ${columnName} <= TO_DATE('${convertDateToSQLDateTime(dates[1])}', '$DATETIME_FMT_ORA'))"""
        } else if (e.operator == QueryOperatorEnum.LESS_THAN) {
            dates = RelativeDateConverter.findDay(new Date(e.value), timezone, true)
            result = "${columnName} < TO_DATE('${convertDateToSQLDateTime(dates[0])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"
        } else if (e.operator == QueryOperatorEnum.LESS_THAN_OR_EQUAL) {
            dates = RelativeDateConverter.findDay(new Date(e.value), timezone, true)
            result = "${columnName} <= TO_DATE('${convertDateToSQLDateTime(dates[1])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"
        } else if (e.operator == QueryOperatorEnum.GREATER_THAN) {
            dates = RelativeDateConverter.findDay(new Date(e.value), timezone, true)
            result = "${columnName} > TO_DATE('${convertDateToSQLDateTime(dates[0])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"
        } else if (e.operator == QueryOperatorEnum.GREATER_THAN_OR_EQUAL) {
            dates = RelativeDateConverter.findDay(new Date(e.value), timezone, true)
            result = "${columnName} >= TO_DATE('${convertDateToSQLDateTime(dates[0])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"
        } else if (e.operator == QueryOperatorEnum.YESTERDAY || e.operator == QueryOperatorEnum.LAST_MONTH ||
                e.operator == QueryOperatorEnum.LAST_WEEK || e.operator == QueryOperatorEnum.LAST_YEAR) {
            dates = RelativeDateConverter.(e.operator.value())(nextRunDate, 1, timezone)
            result = """${columnName} >= TO_DATE('${convertDateToSQLDateTime(dates[0])}', '$DATETIME_FMT_ORA') AND
                        ${columnName} <= TO_DATE('${convertDateToSQLDateTime(dates[1])}', '$DATETIME_FMT_ORA')  AND ${
                columnName
            } IS NOT NULL"""
        } else if (e.operator == QueryOperatorEnum.YESTERDAY || e.operator == QueryOperatorEnum.LAST_X_DAYS) {
            def days = 1
            if (e.operator == QueryOperatorEnum.LAST_X_DAYS) {
                days = Integer.parseInt(e.value)
            }
            dates = RelativeDateConverter.lastXDaysDates(nextRunDate, days, timezone)
            result = """${columnName} >= TO_DATE('${convertDateToSQLDateTime(dates[0])}', '$DATETIME_FMT_ORA') AND
                        ${columnName} <= TO_DATE('${convertDateToSQLDateTime(dates[1])}', '$DATETIME_FMT_ORA')  AND ${
                columnName
            } IS NOT NULL"""
        } else if (e.operator == QueryOperatorEnum.LAST_X_MONTHS ||
                e.operator == QueryOperatorEnum.LAST_X_WEEKS || e.operator == QueryOperatorEnum.LAST_X_YEARS) {
            dates = RelativeDateConverter.(e.operator.value())(nextRunDate, Integer.parseInt(e.value), timezone)
            result = """${columnName} >= TO_DATE('${convertDateToSQLDateTime(dates[0])}', '$DATETIME_FMT_ORA') AND
                        ${columnName} <= TO_DATE('${convertDateToSQLDateTime(dates[1])}', '$DATETIME_FMT_ORA')  AND ${columnName} IS NOT NULL"""
        }
        return result
    }

    String convertDateToSQLDateTime(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FMT)
        return sdf.format(date)
    }

    public boolean isVoidedFlagOn(ReportTemplate template) {
        if (!(template?.templateType in [TemplateTypeEnum.CASE_LINE, TemplateTypeEnum.DATA_TAB])) {
            return false
        }
        return template.getAllSelectedFieldsInfo()*.reportField?.any {
            it.sourceColumn?.reportItem == "CMR_VOIDED"
        }
    }

    public String replaceVoidedTable(reportSql) {
        String inputSql = reportSql
        inputSql = inputSql?.replaceAll("(?i)V_CASE_SUBMISSION_INFO", "V_CASE_SUBMISSION_INFO_ALL")
        return inputSql
    }

    String excludeDeletedCases(EvaluateCaseDateEnum evaluateCaseDateEnum, Date asOfCaseVersionDate) {
        String deletedCasesCheckSql = ""
        SimpleDateFormat sdf = new SimpleDateFormat(DATETIME_FMT)
        if (evaluateCaseDateEnum == EvaluateCaseDateEnum.VERSION_ASOF && asOfCaseVersionDate) {
            deletedCasesCheckSql = " and exists ( select 1 from case_deleted_info cdi where cdi.case_id = cm.case_id " +
                    "and TO_DATE('${sdf.format(asOfCaseVersionDate)}','${DATETIME_FMT_ORA}')  >= cdi.deleted_start_date " +
                    "and TO_DATE('${sdf.format(asOfCaseVersionDate)}','${DATETIME_FMT_ORA}')  < cdi.deleted_end_date)  "
        }
        return deletedCasesCheckSql
    }

    String persistCaseSeriesExecutionData(Long executedConfigurationId) {
        String proc = """
                begin PKG_PVS_ALERT_EXECUTION.p_persist_case_series_data(${executedConfigurationId}); end;
        """
        proc
    }

    /**
     * This is the core method responsible to create the initial alert configuration parameters gtts.
     * @param hasQuery
     * @param templateSetId
     * @return
     */
    public String initializeAlertGtts(Configuration config, Long executedConfigId, boolean hasQuery, boolean isAggregateCase = false, boolean isViewSql = false,
                                      boolean isReportCumulative = false, String dataSource = null, Long caseSeriesId = null, boolean isIntegratedVaers = false, boolean isIntegratedVigibase = false,
                                      MasterConfiguration masterConfiguration = null, MasterExecutedConfiguration masterExecutedConfiguration = null, List exConfigs = null, def roleMap = null, def productMap = null, Boolean isAWSfailed = false, boolean isEligibleForAWS) {
            //Configuration config = templateQuery.report
            int isCumulativeTemplate = 0
            int colID = 0
            ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(executedConfigId)
            println "roleType: = " +roleMap
            println "productMap:= " + productMap
            //The gtt tables are truncated
            String insertStatement = "Begin " +
                    "execute immediate('delete from gtt_report_input_params'); " +
                    "execute immediate('delete from gtt_report_input_fields'); " +
                    "execute immediate('delete from GTT_AGG_MASTER_CHILD_DTLS'); " +
                    "execute immediate('delete from gtt_filter_key_values'); "


            int queryExists = hasQuery ? 1 : 0
            //In order to make sure that case related data should come, query level is set as case.
            String queryLevel = QueryLevelEnum.CASE
            if (config?.type == SINGLE_CASE_ALERT) {
                queryLevel = QueryLevelEnum.PRODUCT
            }
            int showDistinct

            ReportTemplate reportTemplate = config.template
            reportTemplate = GrailsHibernateUtil.unwrapIfProxy(reportTemplate)

            if (reportTemplate instanceof CaseLineListingTemplate) { // this parameter only available for line listings
                showDistinct = reportTemplate?.columnShowDistinct ? 1 : 0
            }

            String inputSeparator = "" // feature not available in UI , comma added by default
            int reassessIndex = 0
            int templateSetFlag = 0
            List<Date> minMaxDate = isViewSql ? executedConfiguration?.executedAlertDateRangeInformation?.getReportStartAndEndDate() : config.alertDateRangeInformation.getReportStartAndEndDate(isReportCumulative)
            String minStartDate = isReportCumulative ? EXECUTION_START_DATE : minMaxDate.first()?.format(DATE_FMT)?.toString()
            String maxEndDate = minMaxDate ? minMaxDate.last()?.format(DATE_FMT)?.toString() : new Date().format(DATE_FMT).toString()
            String reportName = config?.name?.replaceAll("(?i)'", "''''") //bug/PVS-64009- ' in report name escaped with 4' as per pvr logic
            if (reportTemplate.templateType == TemplateTypeEnum.TEMPLATE_SET) {
                templateSetFlag = 1
            }

            String startDate = minStartDate
            String endDate = maxEndDate

        if(isIntegratedVaers){
            //Updated Vaers Date Range for Integrated Alert PVS-67898
            Map dateRange = reportExecutorService.getVaersDateRange()
            startDate = isReportCumulative ? EXECUTION_START_DATE : dateRange.startDate
            endDate = dateRange.endDate
            maxEndDate = endDate
        }

        if(isIntegratedVigibase){
            Map dateRange = reportExecutorService.getVigibaseDateRange()
            if (executedConfiguration?.executedAlertDateRangeInformation?.dateRangeEnum == DateRangeEnum.CUMULATIVE) {
                startDate = executedConfiguration?.executedAlertDateRangeInformation?.getReportStartAndEndDate()[0]?.format('dd-MM-yyyy')
                endDate = executedConfiguration?.executedAlertDateRangeInformation?.getReportStartAndEndDate()[1]?.format('dd-MM-yyyy')
                maxEndDate = endDate
            } else {
                startDate = isReportCumulative ? EXECUTION_START_DATE : dateRange.startDate
                endDate = dateRange.endDate
                maxEndDate = endDate
            }
        }
        // Dates is cumulative period is selected in report template or any of the date range associated with measures in Tabulation template is a cumulative date range.
        if ((config.alertDateRangeInformation?.dateRangeEnum == DateRangeEnum.CUMULATIVE) ||
                (reportTemplate instanceof DataTabulationTemplate && reportTemplate.hasCumulativeOrCustomPeriod())){
            isCumulativeTemplate = 1
        }

            int includeLockedVersion = config?.includeLockedVersion ? 1 : 0

            //PV reports centric parameters.
            int includeAllStudyDrugsCases = config?.studyCases ? 1 : 0
            int onlyPoi = config?.onlyPoi ? 1 : 0
            int ignoreStudyType = config?.ignoreStudyType ? 1 : 0
            int supectProductCheck = 0
            if (config?.type == Constants.AlertConfigType.SINGLE_CASE_ALERT && config?.suspectProduct) {
                supectProductCheck = 1
            }
            int includeOpenCases = 0
            int includePrevMissedCase = 0
            Integer prCaseSeriesID = 0
            Integer cumCaseSeriesID = 0
            int limitToCaseSeriesID = 0
            if (caseSeriesId) {
                limitToCaseSeriesID = caseSeriesId
            } else if (!isReportCumulative) {
                if (executedConfiguration?.alertCaseSeriesId) {
                    limitToCaseSeriesID = executedConfiguration.alertCaseSeriesId
                }
            }
            //PV reports centric parameters ends.

            int excludeFu = config?.excludeFollowUp ? 1 : 0

            String asOfVersionDate = ""
            if (config?.asOfVersionDate) {
                asOfVersionDate = config?.asOfVersionDate?.format(DATE_FMT)?.toString()
            }
            String dateRangeType = config?.dateRangeType?.value()
            String evaluateCaseDataAs = config?.evaluateDateAs?.value()
            int isDatasheetChecked = config?.isDatasheetChecked ? 1: 0
            int medicallyConfirmedCasesFlag = 0 // passed it alwasys 0 as part of Point#3 PVS-54684
            int productFilterFlag = (config?.productSelection || config?.productGroups || config?.productGroupSelection) ? 1 : 0
            int studyFilterFlag = config?.studySelection ? 1 : 0
            int eventFilterFlag = config?.eventSelection || config?.eventGroupSelection ? 1 : 0

            int includeCleanupVersion = Holders.config.signal.includeDataCleanupVersion ? 1 : 0
            // configurable in application

            int limitPrimaryPath = config?.limitPrimaryPath ? 1 : 0

            //TODO: Need to clarify with DB team to determine its relevence for the PVS.
            int bVoidedFlag = 0

            //In pvsginal application we don't want non-valid cases thus passing value as 1
            int excludeNonValidCases = config.excludeNonValidCases ? 1 : 0
            excludeNonValidCases = excludeNonValidCases ? updateNonValidForQuery(config, dataSource) : 0
            int excludeIncludeDuplicateCases = config.type == SINGLE_CASE_ALERT ? 0 : 1

            String selectedDateRange = config.alertDateRangeInformation?.dateRangeEnum?.name()
            Long prevExecId = fetchPreviousExConfigId(executedConfigId, config.name)

            int periodicReportFlag = 0
            int missedCases = config.missedCases ? 1 : 0
            String missedCaseStartDate = dataObjectService.getfirstVersionExecMap(executedConfigId)?.format(DATE_FMT)?.toString()
            String periodicReportType = ''
            String primaryDestination = ''

            // Comma separated lists of all filters
            //ToDo remove this PVA check
            Boolean isPVA = true
            List<Map> productDetails = isPVA ? PVDictionaryConfig.ProductConfig.columns.collect {
                [:]
            } : [[:], [:], [:], [:]]
            List<Map> productDrugDetails = []
            if (config?.productSelection) {
                productDetails = MiscUtil?.getProductDictionaryValues(config?.productSelection, isPVA)
                productDrugDetails = MiscUtil?.getProductDrugValues(config?.productSelection, isPVA)?: []
            } else if (config?.productGroups) {
                productDetails = MiscUtil?.getProductDictionaryValues(config?.productGroups, isPVA)
            }
            List<Map> studyDetails = MiscUtil?.getStudyDictionaryValues(config?.studySelection, isPVA)
            List<Map> eventDetails = MiscUtil?.getEventDictionaryValues(config?.eventSelection)
            Map soc = eventDetails[0]
            Map hlgt = eventDetails[1]
            Map hlt = eventDetails[2]
            Map pt = eventDetails[3]
            Map llt = eventDetails[4]
            Map synonyms = eventDetails[5]
            Map smqsBroad = eventDetails[6]
            Map smqsNarrow = eventDetails[7]

            log.info("isAggregateCase -: " + isAggregateCase)
            String reportStartDate = startDate
            log.info("The report start date -: " + reportStartDate)

            // Template field level parameters are configured
            //Primarily used for the single case alert.
            if (reportTemplate instanceof CaseLineListingTemplate) {
                reportTemplate.getAllSelectedFieldsInfo()?.each { ReportFieldInfo rf ->
                    int csvFlag = 0
                    int redactedFlag = 0
                    int blindedFlag = 0
                    int groupColumnFlag = 0
                    String sortAscDesc = ""
                    colID += 1
                    String javaVariable = rf?.reportField?.name
                    int sortLevel = rf?.sortLevel ?: 0
                    sortAscDesc = rf?.sort?.value()
                    int setId = 0 //This is set as 0 as default
                    if (rf.commaSeparatedValue) {
                        csvFlag = 1
                    }
                    if (rf.blindedValue) {
                        blindedFlag = 1
                    }
                    String customExpression = rf?.customExpression?.replaceAll("(?i)'", "''")
                    String advSortExpression = rf?.advancedSorting?.replaceAll("(?i)'", "''")
                    // this parameter only available for line listings
                    if (reportTemplate instanceof CaseLineListingTemplate) {
                        groupColumnFlag = reportTemplate.groupingList?.reportFieldInfoList*.reportField.find {
                            it.id == rf.reportField.id
                        } ? 1 : 0
                    }
                    // incremental id generated for every re-assess column added in the template in the relative order of columns in template
                    if (rf?.reportField?.name == "dvListednessReassess") {
                        reassessIndex += 1
                    }
                    if (!javaVariable.equals("eventConserCoreListedness")) {
                        insertStatement += " Insert into GTT_REPORT_INPUT_FIELDS (ID,JAVA_VARIABLE,SORT_LEVEL, SORT_TYPE,CSV_FLAG,BLINDED_FLAG,REDACTED_FLAG," +
                                "CUSTOM_EXPRESSION,GROUP_COLUMN_FLAG,REASSESS_INDEX,ADVANCED_SORT_EXPRESSION,SET_ID,TEMP_SET_ID) VALUES (${colID},'${javaVariable}',${sortLevel}" +
                                ",'${sortAscDesc}',${csvFlag},${blindedFlag},${redactedFlag},'${customExpression}',${groupColumnFlag},${reassessIndex},'${advSortExpression}',${setId},0); "?.replaceAll("(?i)'null'", "null")

                    } else {
                        insertStatement += " Insert into GTT_REPORT_INPUT_FIELDS (ID,JAVA_VARIABLE,SORT_LEVEL, SORT_TYPE,CSV_FLAG,BLINDED_FLAG,REDACTED_FLAG," +
                                "CUSTOM_EXPRESSION,GROUP_COLUMN_FLAG,REASSESS_INDEX,ADVANCED_SORT_EXPRESSION,SET_ID,TEMP_SET_ID) VALUES (${colID},'assessListedness',${sortLevel}" +
                                ",'${sortAscDesc}',${csvFlag},${blindedFlag},${redactedFlag},'${customExpression}',${groupColumnFlag},${reassessIndex},'${advSortExpression}',${setId},0); "?.replaceAll("(?i)'null'", "null")

                }
            }
        }
        if(config.type == Constants.AlertConfigType.SINGLE_CASE_ALERT){
            insertStatement += "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('EXECUTION_ID','${executedConfigId}');"
        } else if (masterExecutedConfiguration) {
            insertStatement += "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('MASTER_EXECUTION_ID','${masterExecutedConfiguration.id}');"
        } else if (config.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            insertStatement += "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('CURRENT_EXECUTION_ID','${executedConfigId}');"
        }

        Map miningVariables
        if(config?.selectedDatasource.equals(Constants.DataSource.FAERS)){
            miningVariables = cacheService.getMiningVariables(Constants.DataSource.FAERS)
        } else {
            miningVariables = cacheService.getMiningVariables(Constants.DataSource.PVA)
        }
        Integer keyId = 0
        miningVariables.each{ key, value ->
            if(value?.label.equalsIgnoreCase(config?.dataMiningVariable)){
                keyId = key as Integer
            }
        }
        Integer isBatchRestrict = config.isProductMining ? 1 : 0

            if (dataSource == Constants.DataSource.VAERS || dataSource == Constants.DataSource.VIGIBASE || dataSource == Constants.DataSource.JADER) {
                includeLockedVersion = 0
                evaluateCaseDataAs = EvaluateCaseDateEnum.LATEST_VERSION
                asOfVersionDate = ""
            }

            // report Level parameters
            insertStatement += " Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('HAS_QUERY','${queryExists}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('QUERY_LEVEL','${queryLevel}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('DATASHEET_SELECTED','${isDatasheetChecked}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('SHOW_DISTINCT','${showDistinct}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('INPUT_SEPARATOR','${inputSeparator}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('REPORT_START_DATE','${reportStartDate}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('REPORT_END_DATE','${endDate}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('INCLUDE_LOCKED_VERSION','${includeLockedVersion}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('INCLUDE_STUDY_DRUGS','${includeAllStudyDrugsCases}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('IGNORE_STUDY_MED_TYPE','${ignoreStudyType}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('CONSIDER_POI_FOR_UNBLINDED','${onlyPoi}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('VERSION_ASOF_DATE','${asOfVersionDate}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('DATE_RANGE_TYPE','${dateRangeType}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('EVALUATE_DATA_ASOF','${evaluateCaseDataAs}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('MEDICALLY_CONFIRMED_FLAG','${medicallyConfirmedCasesFlag}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('PRODUCT_FILTER_FLAG','${productFilterFlag}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('STUDY_FILTER_FLAG','${studyFilterFlag}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('EVENT_FILTER_FLAG','${eventFilterFlag}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('EXCLUDE_FOLLOWUP','${excludeFu}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('INCLUDE_CLEANUP_VERSION','${includeCleanupVersion}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('SUSPECT_PRODUCT_CHECK','${supectProductCheck}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('LIMIT_PRIMARY_PATH','${limitPrimaryPath}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('EXCLUDE_NONVALID_CASES','${excludeNonValidCases}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('LIMIT_TO_CASE_SERIES_ID','${limitToCaseSeriesID}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('VOIDED_FLAG','${bVoidedFlag}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('PERIODIC_REPORT_FLAG','${periodicReportFlag}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('PR_CASE_SERIES_ID','${prCaseSeriesID}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('CUM_CASE_SERIES_ID','${cumCaseSeriesID}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('SELECTED_DATE_RANGE','${selectedDateRange}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('PERIODIC_REPORT_TYPE','${periodicReportType}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('PRIMARY_DESTINATION_NAME','${primaryDestination}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('INCLUDE_OPEN_CASES_IN_DRAFT','${includeOpenCases}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('MIN_REPORT_START_DATE','${reportStartDate}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('MAX_REPORT_END_DATE','${maxEndDate}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('REPORT_NAME','${reportName}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ANY_CUMULATIVE_TEMP_FLAG','0');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('IS_CUMULATIVE_TEMPLATE','${isCumulativeTemplate}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('TEMPLATE_SET_FLAG','${templateSetFlag}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('INCLUDE_PREVIOUSLY_MISSED_CASES','${includePrevMissedCase}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('SOURCE_TYPE','CENTRAL');" +
                    "INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,  PARAM_VALUE) VALUES ('EXCLUDE_DUPLICATES','${excludeIncludeDuplicateCases}');" +
                    "INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,  PARAM_VALUE) VALUES ('INCLUDE_PREVIOUSLY_MISSED_CASES_PVS','${missedCases}');" +
                    "INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,  PARAM_VALUE) VALUES ('PREV_INCLUDE_START_DATE','${missedCaseStartDate}');" +
                    "INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,  PARAM_VALUE) VALUES ('HAS_QUERY_PVS','${config?.alertQueryId ? 1 : 0}');" +
                    "INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,  PARAM_VALUE) VALUES ('IS_EVENT_GROUP','${config?.eventGroupSelection && config?.groupBySmq ? 1 : 0}');" +
                    "INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,  PARAM_VALUE) VALUES ('DSS_PREVIOUS_EXECUTION_ID','${prevExecId ?: executedConfigId}');" +
                    "INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,  PARAM_VALUE) VALUES ('TENANT_ID','${Holders.config.signal.default.tenant.id}');" +
                    "INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,  PARAM_VALUE) VALUES ('BATCH_RESTRICT_TO_SEL_PRODUCT','${isBatchRestrict}');" +
                    "INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,  PARAM_VALUE) VALUES ('BATCH_SIGNAL_ON','${keyId}');"+
                    "INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,  PARAM_VALUE) VALUES ('IS_MULTI_INGREDIENT','${config?.isMultiIngredient?1:0}');" +
                    "INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,  PARAM_VALUE) VALUES ('AWS_LOGGING','${isEligibleForAWS?1:0}');"

        if (executedConfiguration.selectedDatasource in Holders.config.awsdb.supported.dbs) {
            insertStatement += "INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,  PARAM_VALUE) VALUES ('AWS_OPTIMIZATION_FLAG','${isAWSfailed ? 0 : 1}');"
        }

        if (dataSource == Constants.DataSource.PVA) {
            insertStatement += "INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,  PARAM_VALUE) VALUES ('INCLUDE_WHO_DRUGS','${config?.includeWHODrugs ? 1 : 0}');"
        }

        SuperQueryDTO superForegroundQueryDTO = queryService.queryDetail(config?.alertForegroundQueryId)

        if (superForegroundQueryDTO) {
            insertStatement += "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('HAS_FOREGROUND_QUERY_PVS',1);"
        } else {
            insertStatement += "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('HAS_FOREGROUND_QUERY_PVS',0);"
        }
        if (config.foregroundSearch) {
            insertStatement += "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('HAS_FOREGROUND_SEARCH_ATTRIBUTES',1);"
            if (config?.foregroundSearchAttr) {
                JSON.parse(config?.foregroundSearchAttr).each {
                    if (it.val) {
                        insertStatement += "Insert into GTT_FILTER_KEY_VALUES (key_id , code , text) VALUES ('${it.keyId}','${it.val}','${it.text}');"
                    }
                }
            }
        } else {
            insertStatement += "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('HAS_FOREGROUND_SEARCH_ATTRIBUTES','0');"
        }


        if (exConfigs) {
                // insert child executed config gtts
                //List<ExecutedConfiguration> executedConfigurationList = ExecutedConfiguration.findByMasterExConfigId(masterExecutedConfiguration.id)
                exConfigs.each { ExecutedConfiguration it ->
                    List groups = []
                    if (it.productGroupSelection) {
                        groups = JSON.parse(it.productGroupSelection)
                    }
                    String prodName = groups ? groups[0].name.substring(0, groups[0].name.lastIndexOf('(') - 1) : it.getNameFieldFromJson(it.productSelection)
                    String prodId = groups ? groups[0].id : it.getIdFieldFromJson(it.productSelection)
                    Integer hierarchyId = it.productGroupSelection ? 199 : (it.productDictionarySelection as Integer) + 199
                    insertStatement += " Insert into GTT_AGG_MASTER_CHILD_DTLS (MASTER_EXECUTION_ID, CHILD_EXECUTION_ID, ALERT_NAME, HIERARCHY_ID, BASE_ID, BASE_NAME) " +
                            "VALUES (${masterExecutedConfiguration.id}, ${it.id},'${it.name?.replaceAll("(?i)'", "''''")}', '${hierarchyId}', '${prodId}', '${prodName?.replaceAll("(?i)'", "''")}'); "

                    // added code for drug record gtt entries
                    if(it.includeWHODrugs && !it.productGroupSelection) {
                        String drugRecordName = it.getDrugNameFieldFromJson(it.productSelection)
                        String drugRecordId = it.getDrugIdFieldFromJson(it.productSelection)
                        if(drugRecordId) {
                            insertStatement += " Insert into GTT_AGG_MASTER_CHILD_DTLS (MASTER_EXECUTION_ID, CHILD_EXECUTION_ID, ALERT_NAME, HIERARCHY_ID, BASE_ID, BASE_NAME) " +
                                    "VALUES (${masterExecutedConfiguration.id}, ${it.id},'${it.name?.replaceAll("(?i)'", "''''")}', '${Constants.DRUG_PROD_HIERARCHY}', '${drugRecordId}', '${drugRecordName?.replaceAll("(?i)'", "''")}'); "
                        }

                    }
                }
            }

            if (config.productGroupSelection && !exConfigs) {
                JSON.parse(config.productGroupSelection).each {
                    it.name = it.name?.replaceAll( '"', '\"' )?.replaceAll( "'", "''" )
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (199,${it.id},'${it.name.substring(0, it.name.lastIndexOf('(') - 1)}'); "
                }
            }
        if(dataSource == Constants.DataSource.PVA){
            ProductTypeConfiguration productTypeConfiguration
            Map<String, Integer> productConfigMap = [:]
            executedConfiguration.drugType?.replace('[', '')?.replace(']', '')?.split(',')?.each {
                if(it.isInteger()){
                    productTypeConfiguration = ProductTypeConfiguration.get(it)
                    if(productTypeConfiguration!=null) {
                        if (!productConfigMap.containsKey("${productTypeConfiguration.productTypeId}.${productTypeConfiguration.roleTypeId}")) {
                            productConfigMap.put("${productTypeConfiguration.productTypeId}.${productTypeConfiguration.roleTypeId}", 1)
                            insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (9999,${productTypeConfiguration.productTypeId}.${productTypeConfiguration.roleTypeId},'${productTypeConfiguration.productType} (${productTypeConfiguration.roleType})'); "
                        }
                        ProductTypeConfiguration.findAllByNameIlike(productTypeConfiguration.name).each { productRuleByName ->
                            if (productTypeConfiguration.id != productRuleByName.id && !productConfigMap.containsKey("${productRuleByName.productTypeId}.${productRuleByName.roleTypeId}")) {
                                productConfigMap.put("${productRuleByName.productTypeId}.${productRuleByName.roleTypeId}", 1)
                                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (9999,${productRuleByName.productTypeId}.${productRuleByName.roleTypeId},'${productRuleByName.productType} (${productRuleByName.roleType})'); "
                            }
                        }
                    }
                }
            }
        }else if(dataSource == Constants.DataSource.FAERS && roleMap && productMap){
            if(executedConfiguration.drugType.contains('DRUG_SUSPECT_CONCOMITANT_FAERS')){
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (9999,${productMap['Drug']}.${roleMap['Suspect']},'Drug (Suspect)'); "
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (9999,${productMap['Drug']}.${roleMap['Concomitant']},'Drug (Concomitant)'); "
            }else if(executedConfiguration.drugType.contains('DRUG_SUSPECT_FAERS')){
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (9999,${productMap['Drug']}.${roleMap['Suspect']},'Drug (Suspect)'); "
            }
        }else if(dataSource == Constants.DataSource.VAERS && roleMap && productMap){
            insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (9999,${productMap['Vaccine']}.${roleMap['Suspect']},'Vaccine (Suspect)'); "
        }else if(dataSource == Constants.DataSource.VIGIBASE && roleMap && productMap){
            if (executedConfiguration.drugType.contains('DRUG_SUSPECT_CONCOMITANT_VIGIBASE')){
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (9999,${productMap['Drug']}.${roleMap['Suspect']},'Drug (Suspect)'); "
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (9999,${productMap['Drug']}.${roleMap['Concomitant']},'Drug (Concomitant)'); "
            } else if(executedConfiguration.drugType.contains('DRUG_SUSPECT_VIGIBASE')){
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (9999,${productMap['Drug']}.${roleMap['Suspect']},'Drug (Suspect)'); "
            }
            if(executedConfiguration.drugType.contains('VACCINE_SUSPECT_VIGIBASE')){
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (9999,${productMap['Vaccine']}.${roleMap['Suspect']},'Vaccine (Suspect)'); "
            }
        } else if(dataSource == Constants.DataSource.JADER && roleMap && productMap){
            if(executedConfiguration.drugType.contains('DRUG_SUSPECT_CONCOMITANT_JADER')){
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (9999,${productMap['Drug']}.${roleMap['Suspect']},'Drug (Suspect)'); "
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (9999,${productMap['Drug']}.${roleMap['Concomitant']},'Drug (Concomitant)'); "
            }else if(executedConfiguration.drugType.contains('DRUG_SUSPECT_JADER')){
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (9999,${productMap['Drug']}.${roleMap['Suspect']},'Drug (Suspect)'); "
            }
            println executedConfiguration.drugType
        }
            int trendCalc = Holders.config.signal.agg.calculate.trend.flag == true ? 1 : 0

            insertStatement += "INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,  PARAM_VALUE) VALUES ('TREND_FLAG_CALC','${trendCalc}');"


            if (config.productSelection && !exConfigs) { // Ids used in product filter
                if (isPVA) {

                    List<ViewConfig> productViewsList = PVDictionaryConfig.ProductConfig.views
                    productDetails.eachWithIndex { Map entry, int i ->
                        keyId = productViewsList.get(i)?.keyId as int
                        if (keyId) {
                            entry.each { k, v ->
                                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES ($keyId,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                            }
                        }
                    }
                    if (config?.includeWHODrugs) {
                        productDrugDetails.each { map ->
                            insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (${Constants.DRUG_PROD_HIERARCHY},'${map.id}','${map.name?.replaceAll("(?i)'", "''")}'); "
                        }
                    }
                } else {

                    Map ingredient = productDetails[0]
                    Map family = productDetails[1]
                    Map product = productDetails[2]
                    Map trade = productDetails[3]
                    ingredient.each { k, v ->
                        insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (1,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                    } // KEY_ID for ingredient = 1
                    family.each { k, v ->
                        insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (2,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                    } // KEY_ID for family = 2
                    product.each { k, v ->
                        insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (3,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                    } // KEY_ID for product = 3
                    trade.each { k, v ->
                        insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (4,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                    } // KEY_ID for trade = 4

                }
            }

            if (studyFilterFlag == 1) // Ids used in study filter
            {
                if (isPVA) {
                    studyDetails.eachWithIndex { Map entry, int i ->
                        entry.each { k, v ->
                            insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (${(i + 5)},$k,'${v?.replaceAll("(?i)'", "''")}'); "
                        }
                    }
                } else {
                    Map protocol = studyDetails[0]
                    Map study = studyDetails[1]
                    Map center = studyDetails[2]
                    protocol.each { k, v ->
                        insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (5,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                    } // KEY_ID for protocol = 5
                    study.each { k, v ->
                        insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (6,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                    } // KEY_ID for study = 6
                    center.each { k, v ->
                        insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (7,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                    } // KEY_ID for center = 7
                }
            }

            if (config.eventGroupSelection) {
                JSON.parse(config.eventGroupSelection).each {
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (600,${it.id},'${it.name.substring(0, it.name.lastIndexOf('(') - 1).replaceAll("(?i)'", "''")}'); "
                }
            }

            if (eventFilterFlag == 1) {// Ids used in event filter

                soc.each { k, v ->
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (8,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                } // KEY_ID for soc = 8
                hlgt.each { k, v ->
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (9,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                } // KEY_ID for hlgt = 9
                hlt.each { k, v ->
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (10,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                } // KEY_ID for hlt = 10
                pt.each { k, v ->
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (11,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                } // KEY_ID for pt = 11
                llt.each { k, v ->
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (12,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                } // KEY_ID for llt = 12
                synonyms.each { k, v ->
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (13,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                } // KEY_ID for synonym = 13
                smqsNarrow.each { k, v ->
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (18,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                } // KEY_ID for Narrow SMQ = 18
                smqsBroad.each { k, v ->
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (19,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                } // KEY_ID for Narrow SMQ = 19

            }

            insertStatement += " END;"
            return insertStatement
        }

        String getEmergingIssueTextQuery(List<EmergingIssue> emergingIssueList) {
            int variableCount=0
            int variableCount2=0
            StringBuilder commentTextDeclare = new StringBuilder()
            StringBuilder sqlStatement = new StringBuilder()
            String dateFormat = getStringColumn('DD-MM-YYYY HH:MI:SS AM')
            if (!emergingIssueList)
                return ""
            emergingIssueList.each { EmergingIssue emergingIssue ->
                String eventGroupSelection = "{\"1\":" + emergingIssue.eventGroupSelection?.replaceAll("(?i)'", "''")?.replaceAll('"', "\"")+"}"
                String productGroupSelection = "{\"1\":" + emergingIssue.productGroupSelection?.replaceAll("(?i)'", "''")?.replaceAll('"', "\"")+"}"

                def productSelection = emergingIssue.productSelection? "${emergingIssue.productSelection?.replaceAll("(?i)'", "''")}":null
                def dataSourceDict = (emergingIssue.dataSourceDict)? "${emergingIssue.dataSourceDict?.replaceAll("(?i)'", "''")}":null
                def products = emergingIssue.products? "${emergingIssue.products?.replaceAll("(?i)'", "''")}":null
                def eventSelection = emergingIssue.eventName? "${emergingIssue.eventName?.replaceAll("(?i)'", "''")}": null

                List thisCommentVariable = []
                List thisCommentVariable2 = []
                List<String> eventSelectionParts = eventSelection ? splitStringByByteSize(eventSelection, 32000) : []
                List<String> eventParts = emergingIssue.events ? splitStringByByteSize(emergingIssue.events?.replaceAll("(?i)'", "''"), 32000) : []

                for (int i = 0; i < eventSelectionParts.size(); i++) {
                    if (variableCount == 0) {
                        commentTextDeclare.append("declare\n ");
                    }
                    commentTextDeclare.append("lclb_cmt").append(variableCount).append(" clob := ");
                    commentTextDeclare.append(getStringColumn(eventSelectionParts.get(i))).append(";");
                    thisCommentVariable.add("lclb_cmt"+variableCount)
                    variableCount++;
                }

                for (int i = 0; i < eventParts.size(); i++) {
                    commentTextDeclare.append("lclb_evt").append(variableCount2).append(" clob := ");
                    commentTextDeclare.append(getStringColumn(eventParts.get(i))).append(";");
                    thisCommentVariable2.add("lclb_evt"+variableCount2)
                    variableCount2++;
                }

                sqlStatement.append("Insert into gtt_emerging_issue (ID, VERSION, CREATED_BY, DATE_CREATED, DME, EMERGING_ISSUE, " +
                        "EVENT_SELECTION, IME, LAST_UPDATED, MODIFIED_BY, SPECIAL_MONITORING, " +
                        "EVENT_GROUP_SELECTION, PRODUCT_GROUP_SELECTION, PRODUCT_SELECTION, " +
                        "DATA_SOURCE_DICT, PRODUCTS, EVENTS, IS_MULTI_INGREDIENT) ")

                sqlStatement.append("VALUES (")
                sqlStatement.append(emergingIssue.id).append(",")
                sqlStatement.append(emergingIssue.version).append(",")
                sqlStatement.append(getStringColumn(emergingIssue.createdBy?.replaceAll("(?i)'", "''"))).append(",")
                sqlStatement.append(("TO_TIMESTAMP(${getStringColumn(DateUtil.stringFromDate(emergingIssue.dateCreated, 'dd-MM-YYYY hh:mm:ss a', 'UTC'))}, ${dateFormat})")).append(",")
                sqlStatement.append(emergingIssue.dme ? 1 : 0).append(",")
                sqlStatement.append(emergingIssue.emergingIssue ? 1 : 0).append(",")


                if(thisCommentVariable == []){
                    sqlStatement.append("NULL");
                }else {
                    for (int i = 0; i < thisCommentVariable.size(); i++) {
                        if (i > 0) {
                            sqlStatement.append(" || ");
                        }
                        sqlStatement.append(thisCommentVariable[i]);
                    }
                }


                sqlStatement.append(",").append(emergingIssue.ime ? 1 : 0).append(",")
                sqlStatement.append(("TO_TIMESTAMP(${getStringColumn(DateUtil.stringFromDate(emergingIssue.lastUpdated, 'dd-MM-YYYY hh:mm:ss a', 'UTC'))}, ${dateFormat})")).append(",")
                sqlStatement.append(getStringColumn(emergingIssue.modifiedBy?.replaceAll("(?i)'", "''"))).append(",")
                sqlStatement.append(emergingIssue.specialMonitoring ? 1 : 0).append(",")
                sqlStatement.append(getStringColumn(eventGroupSelection)).append(",")
                sqlStatement.append(getStringColumn(productGroupSelection)).append(",")
                sqlStatement.append(getStringColumn(productSelection)).append(",")
                sqlStatement.append(getStringColumn(dataSourceDict)).append(",")
                sqlStatement.append(getStringColumn(products)).append(",")

                if(thisCommentVariable2 == []){
                    sqlStatement.append("NULL");
                }else {
                    for (int i = 0; i < thisCommentVariable2.size(); i++) {
                        if (i > 0) {
                            sqlStatement.append(" || ");
                        }
                        sqlStatement.append(thisCommentVariable2[i]);
                    }
                }

                sqlStatement.append(",").append(emergingIssue.isMultiIngredient ? 1 : 0).append(");")


            }

            String insertStatement = "";
            insertStatement += commentTextDeclare.toString();
            insertStatement+= "Begin " +
                    "execute immediate('delete from gtt_emerging_issue'); ";
            insertStatement+= sqlStatement.toString();
            insertStatement += " END;"
            return insertStatement


        }

        private static String getStringColumn(String columnVal) {
            if(columnVal == null)
                return "NULL";
            else{
            }
            return "\'"+columnVal+"\'";
        }

        public static List<String> splitStringByByteSize(String input, int maxByteSize) throws UnsupportedEncodingException {
            List<String> parts = new ArrayList<>();

            byte[] bytes = input.getBytes("UTF-8");
            int index = 0;

            while (index < bytes.length) {
                int endIndex = Math.min(index + maxByteSize, bytes.length);
                while (endIndex < bytes.length && (bytes[endIndex] & 0xC0) == 0x80) {
                    endIndex--;
                }

                parts.add(new String(bytes, index, endIndex - index, "UTF-8"));
                index = endIndex;
            }

            return parts;
        }

        String initializeAssessmentGtts(SignalChartsDTO signalChartsDTO) {
            String insertStatement = "Begin " +
                    "execute immediate('delete from gtt_filter_key_values'); " +
                    "execute immediate('delete from signal_report_cases'); " +
                    "execute immediate('delete from gtt_report_input_params'); " +
                    "execute immediate('delete from GTT_INP_CASE_DETAILS'); "

            List<ViewConfig> productViewsList = PVDictionaryConfig.ProductConfig.views
            List<Map> productDetails = PVDictionaryConfig.ProductConfig.columns.collect { [:] }
            if (signalChartsDTO.productSelection) {
                productDetails = MiscUtil?.getProductDictionaryValues(signalChartsDTO.productSelection)
            }
            productDetails.eachWithIndex { Map entry, int i ->
                int keyId = productViewsList.get(i).keyId
                entry.each { k, v ->
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES ($keyId,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                }
            }

            if (signalChartsDTO.productGroupSelection) {
                JSON.parse(signalChartsDTO.productGroupSelection).each {
                    it.name = it.name?.replaceAll( '"', '\"' )?.replaceAll( "'", "''" )
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (199,${it.id},'${it.name.substring(0, it.name.lastIndexOf('(') - 1)}'); "
                }
            }

            if (signalChartsDTO.eventSelection) {
                List<Map> eventDetails = MiscUtil?.getEventDictionaryValues(signalChartsDTO.eventSelection)
                Map soc = eventDetails[0]
                Map hlgt = eventDetails[1]
                Map hlt = eventDetails[2]
                Map pt = eventDetails[3]
                Map llt = eventDetails[4]
                Map synonyms = eventDetails[5]
                Map smqsBroad = eventDetails[6]
                Map smqsNarrow = eventDetails[7]

                soc.each { k, v ->
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (8,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                } // KEY_ID for soc = 8
                hlgt.each { k, v ->
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (9,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                } // KEY_ID for hlgt = 9
                hlt.each { k, v ->
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (10,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                } // KEY_ID for hlt = 10
                pt.each { k, v ->
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (11,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                } // KEY_ID for pt = 11
                llt.each { k, v ->
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (12,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                } // KEY_ID for llt = 12
                synonyms.each { k, v ->
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (13,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                } // KEY_ID for synonym = 13
                smqsNarrow.each { k, v ->
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (18,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                } // KEY_ID for Narrow SMQ = 18
                smqsBroad.each { k, v ->
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (19,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                } // KEY_ID for Narrow SMQ = 19
            }

            if (signalChartsDTO.eventGroupSelection) {
                JSON.parse(signalChartsDTO.eventGroupSelection).each {
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (600,${it.id},'${it.name.substring(0, it.name.lastIndexOf('(') - 1)}'); "
                }
            }
            insertStatement += "INSERT INTO GTT_INP_CASE_DETAILS (ID,  TENANT_ID) VALUES ('100','${Holders.config.signal.default.tenant.id}'); "
            signalChartsDTO.caseList?.replaceAll("'", "")?.split(",")?.each {
                insertStatement += "INSERT INTO GTT_INP_CASE_DETAILS (ID,  CASE_NUM) VALUES ('0','${it}'); "
            }
            insertStatement += "INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('IS_MULTI_INGREDIENT', '${signalChartsDTO?.isMultiIngredient?1:0}');"

            insertStatement += " END;"
            insertStatement
        }

        /*
      This method will initialize GTT_FILTER_KEY_VALUES in which KEY_ID will be VIEW_ID,CODE will be the id of value selected in product dictionary.
     */
        String initializeGTTForSpotfire(String productSelection, Boolean isMultiIngredient=false) {
            String insertStatement = ''
            List<ViewConfig> productViewsList = PVDictionaryConfig.ProductConfig.views
            List<Map> productDetails = PVDictionaryConfig.ProductConfig.columns.collect { [:] }
            if (productSelection) {
                productDetails = MiscUtil?.getProductDictionaryValues(productSelection)
            }
            productDetails.eachWithIndex { Map entry, int i ->
                int keyId = productViewsList.get(i).keyId
                entry.each { k, v ->
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES ($keyId,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                }
            }
            insertStatement
        }

    String initializeGTTForDatsheets(String productSelection, Boolean isProductGroup = false) {
        String insertStatement = ''
        if (isProductGroup) {
            String productMap = productSelection?.replace('[','')?.replace(']','')
            def productGroup = JSON.parse(productMap)
            productGroup.name = productGroup.name?.replaceAll( '"', '\"' )?.replaceAll( "'", "''" )
            insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (199,${productGroup?.id},'${productGroup.name?.substring(0, productGroup.name?.lastIndexOf('(') - 1)}'); "
        } else {
        List<ViewConfig> productViewsList = PVDictionaryConfig.ProductConfig.views
        List<Map> productDetails = PVDictionaryConfig.ProductConfig.columns.collect { [:] }
        if (productSelection) {
            productDetails = MiscUtil?.getProductDictionaryValues(productSelection)
        }
        productDetails?.eachWithIndex { Map entry, int i ->
            int keyId = productViewsList.get(i).keyId
            entry.each { k, v ->
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES ($keyId,$k,'${v?.replaceAll("(?i)'", "''")}'); "
            }
        }
        }
        insertStatement
    }

        String initializeGTTForMissedCases(Configuration config, ExecutedConfiguration executedConfiguration, List prevExConfigs = null, Long prevExConfigId = null) {
            StringBuilder insertStatement = new StringBuilder()
            if (prevExConfigs) {
                insertStatement.append("Begin execute immediate('delete from GTT_PREV_CASES_ALERT_DTLS'); ")
                prevExConfigs.each { ExecutedConfiguration ec ->
                    insertStatement.append(" Insert into GTT_PREV_CASES_ALERT_DTLS (CONFIG_ID,EXECUTION_ID,TENANT_ID) VALUES (${prevExConfigId},${ec.id},1); ")
                }
                insertStatement.append(" END;")
            } else {
                List prevExecAndCaseSeriesId = alertService.fetchPrevExecConfigId(executedConfiguration, config, true, false)
                log.info("Prev Exec Case Series Id Size : " + prevExecAndCaseSeriesId.size())
                if (prevExecAndCaseSeriesId) {
                    insertStatement.append("Begin execute immediate('delete from GTT_PREV_CASES_ALERT_DTLS'); ")
                    prevExecAndCaseSeriesId.each {
                        if (executedConfiguration.type == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
                            insertStatement.append(" Insert into GTT_PREV_CASES_ALERT_DTLS (CONFIG_ID,EXECUTION_ID,CASE_SERIES_ID,TENANT_ID) VALUES (${config.id},${it[0]},${it[1]},1); ")
                        } else {
                            insertStatement.append(" Insert into GTT_PREV_CASES_ALERT_DTLS (CONFIG_ID,EXECUTION_ID,TENANT_ID) VALUES (${config.id},${it},1); ")
                        }
                    }
                    insertStatement.append(" END;")
                }
            }

            insertStatement.toString()
        }

    String initializeGTTForPrevDss(List<ExecutedConfiguration> exConfigs, Long masterExId, List<ExecutedConfiguration> prevExConfigs = null, Long prevMasterExId=null) {
        StringBuilder insertStatement = new StringBuilder()
        ExecutedConfiguration currentCheck  = null
        if(prevExConfigs){
            prevExConfigs.each { ExecutedConfiguration ec ->
                currentCheck = exConfigs.find { it.configId == ec.configId }
            }
        }
        if(prevExConfigs && currentCheck) {
            insertStatement.append("Begin execute immediate('delete from gtt_dss_prev_exec_id_dtls'); ")
            prevExConfigs.each { ExecutedConfiguration ec ->
                List groups = []
                if(ec.productGroupSelection) {
                    groups = JSON.parse(ec.productGroupSelection)
                }
                String prodId = groups? groups[0].id :ec.getIdFieldFromJson(ec.productSelection)

                ExecutedConfiguration currentEc = exConfigs.find {it.configId == ec.configId}
                if(currentEc){
                    insertStatement.append(" Insert into gtt_dss_prev_exec_id_dtls (tenant_id,master_execution_id,execution_id,base_id,prev_master_execution_id,prev_execution_id,prev_base_id) VALUES " +
                            "(1,${masterExId},${currentEc.id},${prodId},${prevMasterExId?:masterExId}, ${ec.id}, ${prodId}); ")
                }
            }
            insertStatement.append(" END;")
        } else {
            insertStatement.append("Begin execute immediate('delete from gtt_dss_prev_exec_id_dtls'); ")
            exConfigs.each { ExecutedConfiguration ec ->
                List groups = []
                if(ec.productGroupSelection) {
                    groups = JSON.parse(ec.productGroupSelection)
                }
                String prodId = groups? groups[0].id :ec.getIdFieldFromJson(ec.productSelection)
                insertStatement.append(" Insert into gtt_dss_prev_exec_id_dtls (tenant_id,master_execution_id,execution_id,base_id,prev_master_execution_id,prev_execution_id,prev_base_id) VALUES " +
                        "(1,${masterExId},${ec.id},${prodId},${masterExId}, ${ec.id}, ${prodId}); ")
            }
            insertStatement.append(" END;")
        }
        insertStatement.toString()
    }


    String getFetchGttStatement(Integer grpId) {
        String insertStatement = "Begin " +
                "execute immediate('delete from GTT_CAT_FETCH'); "
        insertStatement += " Insert into GTT_CAT_FETCH (fact_grp_id , col_nm, col_value) VALUES ("+ grpId +",'CAT_ID',NULL);" +
                "Insert into GTT_CAT_FETCH (fact_grp_id , col_nm, col_value) VALUES ("+ grpId +",'SUB_CAT_ID',NULL);" +
                "Insert into GTT_CAT_FETCH (fact_grp_id , col_nm, col_value) VALUES ("+ grpId +",'CAT_NM',NULL);" +
                "Insert into GTT_CAT_FETCH (fact_grp_id , col_nm, col_value) VALUES ("+ grpId +",'SUB_CAT_NM',NULL);" +
                "Insert into GTT_CAT_FETCH (fact_grp_id , col_nm, col_value) VALUES ("+ grpId +",'PRIVATE_USER_ID',NULL);"

        insertStatement += " END;"
        return insertStatement
    }

   String getFetchGttStatementTab(Integer grpId) {
            String insertStatement = "Begin " +
                    "execute immediate('delete from GTT_CAT_FETCH_TAB'); ";

            insertStatement += "INSERT INTO GTT_CAT_FETCH_TAB " +
                    "(fact_grp_id, cat_id, sub_cat_id, cat_nm, sub_cat_nm, module, private_user_id," +
                    "fact_grp_col_1, fact_grp_col_2, fact_grp_col_3, fact_grp_col_4, fact_grp_col_5," +
                    "fact_grp_col_6, fact_grp_col_7, fact_grp_col_8, fact_grp_col_9, fact_grp_col_10) ";

            insertStatement += "VALUES (" + grpId + ", NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);\n"

            insertStatement += " END;";
            return insertStatement
        }

        String getInputStatement() {
            String insertStatement = "Begin " +
                    "execute immediate('delete from gtt_cat_input_params'); ";

            insertStatement += "INSERT INTO gtt_cat_input_params (key,value) VALUES ('CAT_FACT_FETCH_TAB_SAVE',1);"

            insertStatement += " END;";
            return insertStatement
        }

        String delPrevGTTSForCaseSeries() {
            """
           Begin 
              execute immediate('delete from GTT_FILTER_KEY_VALUES'); 
              execute immediate('delete from gtt_query_case_list');
            END;
        """
        }

        private Long fetchPreviousExConfigId(Long executedConfigId, String name) {

            List<Long> prevExecConfigIdList = ExecutedConfiguration.createCriteria().list {
                projections {
                    property('id')
                }
                eq("name", name)
                'not' {
                    'eq'("id", executedConfigId)
                }
                order("id", "desc")
                maxResults(1)
            } as List<Long>

            return prevExecConfigIdList != null && prevExecConfigIdList.size() > 0 ? prevExecConfigIdList.get(0) : null
        }

        int updateNonValidForQuery(Configuration config, String dataSource) {
            Locale locale = new Locale("en", "US")
            String nonValidQueryName = Holders.config.pvreports.nonValidQueryName.quan
            if (config.type == SINGLE_CASE_ALERT) {
                nonValidQueryName = Holders.config.pvreports.nonValidQueryName.qual
            }
            boolean result = false
            SuperQueryDTO nonValidQuery = queryService.queryDetailByName(nonValidQueryName)
            if (nonValidQuery) {
                Map dataMap = (new JsonSlurper()).parseText(nonValidQuery.JSONQuery)
                Map allMap = dataMap.all
                def containerGroupsList = allMap.containerGroups
                if (nonValidQuery?.queryType == com.rxlogix.enums.QueryTypeEnum.QUERY_BUILDER
                        && dataSource == Constants.DataSource.PVA) {
                    Map nonValidQueryMap = getConfiguredNonQueryString()
                    result = nonValidQueryList(containerGroupsList, result, locale, nonValidQueryMap)
                }
            } else {
                log.info("Query be name ${nonValidQueryName} doesn't exist in PVR, so setting the result to 0")
            }
            return result ? 1 : 0
        }

        boolean nonValidQueryList(def data, Boolean result, def locale, Map nonValidQueryMap) {
            if (data instanceof Map && data.expressions) {
                return result || nonValidQueryList(data.expressions, false, locale, nonValidQueryMap)
            } else {
                if (data instanceof List) {
                    data.eachWithIndex { val, i ->
                        result = result || nonValidQueryList(val, false, locale, nonValidQueryMap)
                    }
                    return result
                } else {
                    if (data.field == nonValidQueryMap.field && data.op == nonValidQueryMap.op
                            && data.value == nonValidQueryMap.value) {
                        return true
                    } else {
                        return false
                    }
                }
            }
        }

        Map getConfiguredNonQueryString() {
            Sql sql = null
            Map result = [:]
            try {
                sql = new Sql(signalDataSourceService.getReportConnection("pva"))
                sql.eachRow("""select key_id, key_value from pvs_app_constants 
                    where key_id in ('PVS_INVALID_JAVA_VARIABLE','PVS_INVALID_OPERATOR','PVS_INVALID_VALUE')""") { row ->
                    if (row.key_id == "PVS_INVALID_JAVA_VARIABLE")
                        result["field"] = row.key_value
                    else if (row.key_id == "PVS_INVALID_OPERATOR")
                        result["op"] = row.key_value
                    else if (row.key_id == "PVS_INVALID_VALUE")
                        result["value"] = row.key_value
                }
            } catch (Throwable th) {
                th.printStackTrace()
                log.error(th.getMessage())
            } finally {
                sql?.close()
            }
            result
        }

    private String insertQueriesDataToTempTableForBusinessConfig(SuperQueryDTO superQuery, SqlGenIDDTO sqlGenIDDTO,
                                                                 String joinOperator, int parent, List<ParameterValue> blanks, List<ParameterValue> customSqlBlanks,
                                                                 Set<ParameterValue> poiInputParams, Locale locale, boolean nonValidCases) {
            String insertQuery = ""
            if (superQuery?.queryType == QueryTypeEnum.QUERY_BUILDER) {
                insertQuery = insertQuery + buildQueryFromJSONQuery(superQuery.JSONQuery, sqlGenIDDTO, parent, joinOperator, superQuery.hasBlanks, blanks, poiInputParams, locale, nonValidCases)
            } else if (superQuery?.queryType == QueryTypeEnum.SET_BUILDER) {
                Map dataMap = (new JsonSlurper()).parseText(superQuery.JSONQuery)
                Map allMap = dataMap.all
                List containerGroupsList = allMap.containerGroups
                insertQuery = insertQuery + insertSqlStatementFromQuerySetStatementForBusinessConfig(containerGroupsList, sqlGenIDDTO, new SqlGenIDDTO(), 0, joinOperator, blanks, customSqlBlanks, poiInputParams, locale)
            }
            insertQuery += ""
            return insertQuery
        }

        private String insertSqlStatementFromQuerySetStatementForBusinessConfig(boolean isForeground=false,
                def data, SqlGenIDDTO sqlGenIDDTO, SqlGenIDDTO parentSqlGenIDTO, Integer parent, String joinOperator, List<ParameterValue> blanks,
List<ParameterValue> customSqlBlanks, Set<ParameterValue> poiInputParams, Locale locale) {
            if (data instanceof Map && data.expressions) {
                parentSqlGenIDTO.value = parentSqlGenIDTO.value + 1
                String keyword = generateKeywordForBusinessConfig(data.keyword)
                String groupInsert = "INSERT INTO GTT_QUERY_SETS (SET_ID,CUSTOM_SQL,SET_OPERATOR,GROUP_ID,QUERY_FLAG,PARENT_GROUP_ID) values (${sqlGenIDDTO.value},null,'$keyword',${parentSqlGenIDTO.value},null,${parent}) ;\n"
                sqlGenIDDTO.value = sqlGenIDDTO.value + 1
                return groupInsert + insertSqlStatementFromQuerySetStatementForBusinessConfig(isForeground,data.expressions, sqlGenIDDTO, parentSqlGenIDTO, parentSqlGenIDTO.value, keyword, blanks, customSqlBlanks, poiInputParams, locale)
            } else {
                if (data instanceof List) {
                    String query = ""
                    data.each { val ->
                        sqlGenIDDTO.value = sqlGenIDDTO.value + 1
                        query = query + insertSqlStatementFromQuerySetStatementForBusinessConfig(isForeground,val, sqlGenIDDTO, parentSqlGenIDTO, parent, joinOperator, blanks, customSqlBlanks, poiInputParams, locale)
                    }
                    return query
                } else if (data.category && data.category.equals("QUERY_CRITERIA")) {
                    sqlGenIDDTO.value = sqlGenIDDTO.value + 1
                    return insertQueriesDataToTempTableForBusinessConfig(queryService.queryDetail(data.attribute as Long), sqlGenIDDTO, joinOperator, parent, blanks, customSqlBlanks, poiInputParams, locale, false)
                }
                return ""
            }
        }

        SuperQueryDTO prepareSuperQueryDTO(String ruleJSON, QueryTypeEnum queryTypeEnum) {
            new SuperQueryDTO(ruleJSON, queryTypeEnum)
        }

        String generateKeywordForBusinessConfig(String keyword) {
            if (keyword) {
                if (keyword.equals("and"))
                    return "INTERSECT"
                else
                    return "UNION"
            }
            return null
        }

        String initializeAlertGttsSignal(Map params, List<String> dateRangeList) {

            //Configuration config = templateQuery.report
            int isCumulativeTemplate = 0

            int colID = 0

            //The gtt tables are truncated
            String insertStatement = "Begin " +
                    "execute immediate('delete from gtt_report_input_params'); " +
                    "execute immediate('delete from gtt_report_input_fields'); " +
                    "execute immediate('delete from gtt_filter_key_values'); "

            int queryExists = 1
            //In order to make sure that case related data should come, query level is set as case.
            String queryLevel = QueryLevelEnum.PRODUCT
            int showDistinct

            ReportTemplate reportTemplate = ReportTemplate.findByName("Case_num")

            if (reportTemplate instanceof CaseLineListingTemplate) { // this parameter only available for line listings
                showDistinct = reportTemplate?.columnShowDistinct ? 1 : 0
            }

            String inputSeparator = "" // feature not available in UI , comma added by default
            int reassessIndex = 0
            int templateSetFlag = 0
            String minStartDate = dateRangeList[0]
            String maxEndDate = dateRangeList[1]
            String reportName = params.name?.replaceAll("(?i)'", "''")
            if (reportTemplate.templateType == TemplateTypeEnum.TEMPLATE_SET) {
                templateSetFlag = 1
            }

            String startDate = minStartDate
            String endDate = maxEndDate

            int includeLockedVersion = 1

            //PV reports centric parameters.
            int includeAllStudyDrugsCases = 0
            int onlyPoi = 0
            int ignoreStudyType = 0
            int supectProductCheck = 1
            int includeOpenCases = 0
            int includePrevMissedCase = 0
            Integer prCaseSeriesID = 0
            Integer cumCaseSeriesID = 0
            int limitToCaseSeriesID = 0
            //PV reports centric parameters ends.

            int excludeFu = 0

            String asOfVersionDate = ""

            String dateRangeType = DateRangeTypeCaseEnum.CASE_RECEIPT_DATE.value()
            String evaluateCaseDataAs = EvaluateCaseDateEnum.LATEST_VERSION.value()
            int medicallyConfirmedCasesFlag = 0
            int productFilterFlag = 1
            int studyFilterFlag = 0
            int eventFilterFlag = 1

            int includeCleanupVersion = Holders.config.signal.includeDataCleanupVersion ? 1 : 0
            // configurable in application

            int limitPrimaryPath = 0

            //TODO: Need to clarify with DB team to determine its relevence for the PVS.
            int bVoidedFlag = 0

            //In pvsginal application we don't want non-valid cases thus passing value as 1
            int excludeNonValidCases = 1
            int excludeIncludeDuplicateCases = 0

            String selectedDateRange = DateRangeEnum.CUSTOM.name()

            int periodicReportFlag = 0
            int missedCases = 0
            String missedCaseStartDate = ''
            String periodicReportType = ''
            String primaryDestination = ''

            // Comma separated lists of all filters
            //ToDo remove this PVA check
            Boolean isPVA = true
            List<Map> productDetails = isPVA ? PVDictionaryConfig.ProductConfig.columns.collect {
                [:]
            } : [[:], [:], [:], [:]]
            if (params.productSelection) {
                productDetails = MiscUtil?.getProductDictionaryValues(params?.productSelection, isPVA)
            }
            List<Map> eventDetails = MiscUtil?.getEventDictionaryValues(params?.eventSelection)
            Map soc = eventDetails[0]
            Map hlgt = eventDetails[1]
            Map hlt = eventDetails[2]
            Map pt = eventDetails[3]
            Map llt = eventDetails[4]
            Map synonyms = eventDetails[5]
            Map smqsBroad = eventDetails[6]
            Map smqsNarrow = eventDetails[7]

            String reportStartDate = startDate
            log.info("The report start date -: " + reportStartDate)

            // Template field level parameters are configured
            //Primarily used for the single case alert.
            if (reportTemplate instanceof CaseLineListingTemplate) {
                reportTemplate.getAllSelectedFieldsInfo()?.each { ReportFieldInfo rf ->
                    int csvFlag = 0
                    int redactedFlag = 0
                    int blindedFlag = 0
                    int groupColumnFlag = 0
                    String sortAscDesc = ""
                    colID += 1
                    String javaVariable = rf?.reportField?.name
                    int sortLevel = rf?.sortLevel
                    sortAscDesc = rf?.sort?.value()
                    int setId = 0 //This is set as 0 as default
                    if (rf.commaSeparatedValue) {
                        csvFlag = 1
                    }
                    if (rf.blindedValue) {
                        blindedFlag = 1
                    }
                    String customExpression = rf?.customExpression?.replaceAll("(?i)'", "''")
                    String advSortExpression = rf?.advancedSorting?.replaceAll("(?i)'", "''")
                    // this parameter only available for line listings
                    if (reportTemplate instanceof CaseLineListingTemplate) {
                        groupColumnFlag = reportTemplate.groupingList?.reportFieldInfoList*.reportField.find {
                            it.id == rf.reportField.id
                        } ? 1 : 0
                    }
                    // incremental id generated for every re-assess column added in the template in the relative order of columns in template
                    if (rf?.reportField?.name == "dvListednessReassess") {
                        reassessIndex += 1
                    }

                    insertStatement += " Insert into GTT_REPORT_INPUT_FIELDS (ID,JAVA_VARIABLE,SORT_LEVEL, SORT_TYPE,CSV_FLAG,BLINDED_FLAG,REDACTED_FLAG," +
                            "CUSTOM_EXPRESSION,GROUP_COLUMN_FLAG,REASSESS_INDEX,ADVANCED_SORT_EXPRESSION,SET_ID,TEMP_SET_ID) VALUES (${colID},'${javaVariable}',${sortLevel}" +
                            ",'${sortAscDesc}',${csvFlag},${blindedFlag},${redactedFlag},'${customExpression}',${groupColumnFlag},${reassessIndex},'${advSortExpression}',${setId},0); "?.replaceAll("(?i)'null'", "null")

                }
            }

            // report Level parameters
            insertStatement += " Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('HAS_QUERY','${queryExists}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('QUERY_LEVEL','${queryLevel}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('SHOW_DISTINCT','${showDistinct}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('INPUT_SEPARATOR','${inputSeparator}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('REPORT_START_DATE','${reportStartDate}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('REPORT_END_DATE','${endDate}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('INCLUDE_LOCKED_VERSION','${includeLockedVersion}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('INCLUDE_STUDY_DRUGS','${includeAllStudyDrugsCases}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('IGNORE_STUDY_MED_TYPE','${ignoreStudyType}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('CONSIDER_POI_FOR_UNBLINDED','${onlyPoi}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('VERSION_ASOF_DATE','${asOfVersionDate}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('DATE_RANGE_TYPE','${dateRangeType}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('EVALUATE_DATA_ASOF','${evaluateCaseDataAs}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('MEDICALLY_CONFIRMED_FLAG','${medicallyConfirmedCasesFlag}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('PRODUCT_FILTER_FLAG','${productFilterFlag}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('STUDY_FILTER_FLAG','${studyFilterFlag}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('EVENT_FILTER_FLAG','${eventFilterFlag}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('EXCLUDE_FOLLOWUP','${excludeFu}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('INCLUDE_CLEANUP_VERSION','${includeCleanupVersion}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('SUSPECT_PRODUCT_CHECK','${supectProductCheck}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('LIMIT_PRIMARY_PATH','${limitPrimaryPath}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('EXCLUDE_NONVALID_CASES','${excludeNonValidCases}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('LIMIT_TO_CASE_SERIES_ID','${limitToCaseSeriesID}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('VOIDED_FLAG','${bVoidedFlag}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('PERIODIC_REPORT_FLAG','${periodicReportFlag}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('PR_CASE_SERIES_ID','${prCaseSeriesID}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('CUM_CASE_SERIES_ID','${cumCaseSeriesID}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('SELECTED_DATE_RANGE','${selectedDateRange}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('PERIODIC_REPORT_TYPE','${periodicReportType}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('PRIMARY_DESTINATION_NAME','${primaryDestination}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('INCLUDE_OPEN_CASES_IN_DRAFT','${includeOpenCases}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('MIN_REPORT_START_DATE','${reportStartDate}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('MAX_REPORT_END_DATE','${maxEndDate}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('REPORT_NAME','${reportName}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('ANY_CUMULATIVE_TEMP_FLAG','0');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('IS_CUMULATIVE_TEMPLATE','${isCumulativeTemplate}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('TEMPLATE_SET_FLAG','${templateSetFlag}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('INCLUDE_PREVIOUSLY_MISSED_CASES','${includePrevMissedCase}');" +
                    "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('SOURCE_TYPE','CENTRAL');" +
                    "INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,  PARAM_VALUE) VALUES ('EXCLUDE_DUPLICATES','${excludeIncludeDuplicateCases}');" +
                    "INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,  PARAM_VALUE) VALUES ('INCLUDE_PREVIOUSLY_MISSED_CASES_PVS','${missedCases}');" +
                    "INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,  PARAM_VALUE) VALUES ('PREV_INCLUDE_START_DATE','${missedCaseStartDate}');" +
                    "INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,  PARAM_VALUE) VALUES ('HAS_QUERY_PVS','0');" +
                    "INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,  PARAM_VALUE) VALUES ('IS_BATCH','0');" +
                    "INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,  PARAM_VALUE) VALUES ('IS_EVENT_GROUP','0');" +
                    "INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,  PARAM_VALUE) VALUES ('DSS_PREVIOUS_EXECUTION_ID','0');"

            if (params.productGroupSelection && params.productGroupSelection != "[]") {
                JSON.parse(params.productGroupSelection).each {
                    it.name = it.name?.replaceAll( '"', '\"' )?.replaceAll( "'", "''" )
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (199,${it.id},'${it.name.substring(0, it.name.lastIndexOf('(') - 1)}'); "
                }
            }

            if ((params.productGroupSelection == "[]" || !params.productGroupSelection) && productFilterFlag == 1) { // Ids used in product filter
                if (isPVA) {

                    List<ViewConfig> productViewsList = PVDictionaryConfig.ProductConfig.views
                    productDetails.eachWithIndex { Map entry, int i ->
                        int keyId = productViewsList.get(i).keyId
                        entry.each { k, v ->
                            insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES ($keyId,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                        }
                    }
                } else {

                    Map ingredient = productDetails[0]
                    Map family = productDetails[1]
                    Map product = productDetails[2]
                    Map trade = productDetails[3]
                    ingredient.each { k, v ->
                        insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (1,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                    } // KEY_ID for ingredient = 1
                    family.each { k, v ->
                        insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (2,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                    } // KEY_ID for family = 2
                    product.each { k, v ->
                        insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (3,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                    } // KEY_ID for product = 3
                    trade.each { k, v ->
                        insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (4,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                    } // KEY_ID for trade = 4

                }
            }

            if (params.eventGroupSelection) {
                JSON.parse(params.eventGroupSelection).each {
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (600,${it.id},'${it.name.substring(0, it.name.lastIndexOf('(') - 1)}'); "
                }
            }

            if (eventFilterFlag == 1) {// Ids used in event filter

                soc.each { k, v ->
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (8,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                } // KEY_ID for soc = 8
                hlgt.each { k, v ->
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (9,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                } // KEY_ID for hlgt = 9
                hlt.each { k, v ->
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (10,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                } // KEY_ID for hlt = 10
                pt.each { k, v ->
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (11,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                } // KEY_ID for pt = 11
                llt.each { k, v ->
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (12,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                } // KEY_ID for llt = 12
                synonyms.each { k, v ->
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (13,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                } // KEY_ID for synonym = 13
                smqsNarrow.each { k, v ->
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (18,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                } // KEY_ID for Narrow SMQ = 18
                smqsBroad.each { k, v ->
                    insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (19,$k,'${v?.replaceAll("(?i)'", "''")}'); "
                } // KEY_ID for Narrow SMQ = 19

            }
            insertStatement += " END;"

            return insertStatement
        }

    String gttInsertForProductTypeConfigurationSql(String dataSource, ExecutedConfiguration executedConfiguration, def roleMap=null, def productMap=null){
        String insertStatement = ""
        if(dataSource == Constants.DataSource.PVA){
            ProductTypeConfiguration productTypeConfiguration
            Map<String, Integer> productConfigMap = [:]
            executedConfiguration.drugType?.replace('[', '')?.replace(']', '')?.split(',')?.each {
                if(it.isInteger()){
                    productTypeConfiguration = ProductTypeConfiguration.get(it)
                    if(productTypeConfiguration!=null) {
                        if (!productConfigMap.containsKey("${productTypeConfiguration.productTypeId}.${productTypeConfiguration.roleTypeId}")) {
                            productConfigMap.put("${productTypeConfiguration.productTypeId}.${productTypeConfiguration.roleTypeId}", 1)
                            insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (9999,${productTypeConfiguration.productTypeId}.${productTypeConfiguration.roleTypeId},'${productTypeConfiguration.productType} (${productTypeConfiguration.roleType})'); "
                        }
                        ProductTypeConfiguration.findAllByNameIlike(productTypeConfiguration.name).each { productRuleByName ->
                            if (productTypeConfiguration.id != productRuleByName.id && !productConfigMap.containsKey("${productRuleByName.productTypeId}.${productRuleByName.roleTypeId}")) {
                                productConfigMap.put("${productRuleByName.productTypeId}.${productRuleByName.roleTypeId}", 1)
                                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (9999,${productRuleByName.productTypeId}.${productRuleByName.roleTypeId},'${productRuleByName.productType} (${productRuleByName.roleType})'); "
                            }
                        }
                    }
                }
            }
        }else if(dataSource == Constants.DataSource.FAERS && roleMap && productMap){
            if(executedConfiguration.drugType.contains('DRUG_SUSPECT_CONCOMITANT_FAERS')){
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (9999,${productMap['Drug']}.${roleMap['Suspect']},'Drug (Suspect)'); "
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (9999,${productMap['Drug']}.${roleMap['Concomitant']},'Drug (Concomitant)'); "
            }else if(executedConfiguration.drugType.contains('DRUG_SUSPECT_FAERS')){
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (9999,${productMap['Drug']}.${roleMap['Suspect']},'Drug (Suspect)'); "
            }
        }else if(dataSource == Constants.DataSource.VAERS && roleMap && productMap){
            insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (9999,${productMap['Vaccine']}.${roleMap['Suspect']},'Vaccine (Suspect)'); "
        }else if(dataSource == Constants.DataSource.VIGIBASE && roleMap && productMap){
            if (executedConfiguration.drugType.contains('DRUG_SUSPECT_CONCOMITANT_VIGIBASE')){
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (9999,${productMap['Drug']}.${roleMap['Suspect']},'Drug (Suspect)'); "
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (9999,${productMap['Drug']}.${roleMap['Concomitant']},'Drug (Concomitant)'); "
            } else if(executedConfiguration.drugType.contains('DRUG_SUSPECT_VIGIBASE')){
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (9999,${productMap['Drug']}.${roleMap['Suspect']},'Drug (Suspect)'); "
            }
            if(executedConfiguration.drugType.contains('VACCINE_SUSPECT_VIGIBASE')){
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (9999,${productMap['Vaccine']}.${roleMap['Suspect']},'Vaccine (Suspect)'); "
            }
        }
        insertStatement
    }

    String initializeEvdasChartGtt(Boolean isMultiIngredient){
        String insertStatement = "BEGIN " + "execute immediate('delete from gtt_report_input_params');"
        insertStatement += " INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,PARAM_VALUE) VALUES ('IS_MULTI_INGREDIENT', '${isMultiIngredient?1:0}');"
        insertStatement +=" END;"
        insertStatement
    }

    String dmvPossibleValueQuery(String table_name, String column_name) {
        """
                SELECT ${column_name} FROM ${table_name} WHERE UPPER(${column_name}) LIKE :SEARCH_TERM ORDER BY ${column_name} ASC OFFSET :offset ROWS FETCH NEXT :max ROWS ONLY
        """
    }

    String dmvTotalCountQuery(String table_name, String column_name) {
        """
                SELECT COUNT(*) AS COUNT FROM ${table_name} WHERE UPPER(${column_name}) LIKE :SEARCH_TERM
        """
    }

    /**
     * This method is responsible to provide query to insert GTT with required parameters
     * for the case details data coming from the PVS DB
     * @return - Insert statement for GTT related to blinded data will be returned
     */
    String insertGTTForBlindedDataForCaseDetail(User currentUser) {
        Map blindingFieldsForGroup = userService.getBlindingFieldsForGroup(currentUser.groups*.id)
        Map blindingFieldsForUser = userService.getBlindingFieldsForUser(blindingFieldsForGroup, false)
        boolean isAllFieldsRedacted = blindingFieldsForGroup.isAllFieldsRedacted == 1
        boolean isUserMappingAvailable = blindingFieldsForGroup.isUserMappingAvailable
        isAllFieldsRedacted = !(!isUserMappingAvailable && !isAllFieldsRedacted)
        StringBuilder blindingFieldsInsertStatement = new StringBuilder()
        blindingFieldsInsertStatement.append("BEGIN EXECUTE IMMEDIATE('DELETE FROM GTT_PVS_COMMON');\n")
        String initialFuUniqueField = cacheService.getCacheUniqueFieldId()?.find {
            it.value == 'ciTxtDateReceiptInitial'
        }?.key

        for (String field : blindingFieldsForUser.keySet()) {
            if (field && blindingFieldsForUser.get(field) == Constants.BlindingStatus.BLINDED) {
                isAllFieldsRedacted = false
                blindingFieldsInsertStatement.append(SignalQueryHelper.insert_blinding_fields_into_gtt(field, 0))
            } else if (field && blindingFieldsForUser.get(field) == Constants.BlindingStatus.AVAILABLE) {
                isAllFieldsRedacted = false
                blindingFieldsInsertStatement.append(SignalQueryHelper.insert_blinding_fields_into_gtt(field, 1))
            }
        }

        if (isAllFieldsRedacted) {
            blindingFieldsInsertStatement.append(SignalQueryHelper.insert_blinding_fields_into_gtt('ALL_FIELDS_REDACTED', 3))
        }
        blindingFieldsInsertStatement.append(SignalQueryHelper.insert_blinding_fields_into_gtt(initialFuUniqueField, 1))

        blindingFieldsInsertStatement.append(SignalQueryHelper.insert_blinding_fields_into_gtt(SIGNIFICANT_FOLLOUP_NUMBER, 1)) //Added to fix bugs PVS-76785

        blindingFieldsInsertStatement.append('END;\n')
        log.info("GTT insert for Blinding Data - " + blindingFieldsInsertStatement.toString())
        return blindingFieldsInsertStatement.toString()
    }


    /**
     * prepare select sql string for feching data for
     * ICR alert
     * @param columns
     * @param selectedCasesId
     * @param callingScreenQuery
     * @return sql string
     */
    String prepareBlindingSelectSql(List<String> columns, List<Long> selectedCasesId, String callingScreenQuery, String baseTable) {
        String sqlQuery = ""
        String joinSql = ""
        sqlQuery += """
                     SELECT * FROM ( SELECT b.*, COUNT(*) OVER() AS total_count FROM ( SELECT   
                     ${columns.join(', ')} , sca.case_number as actualCaseNumber, sca.product_name as actualProductName, sca.pt as actualPt, sca.name as alertName, att.reference_id as attachments FROM
                       ${baseTable} sca LEFT JOIN ATTACHMENT_LINK att on sca.id = att.reference_id LEFT JOIN EX_RCONFIG ex on sca.exec_config_id = ex.id LEFT JOIN PVUSER users on sca.assigned_to_id = users.id
                         LEFT JOIN GROUPS groups on sca.assigned_to_group_id = groups.id LEFT JOIN DISPOSITION disposition on sca.disposition_id = disposition.id  LEFT JOIN VALIDATED_SINGLE_ALERTS v on sca.id = v.SINGLE_ALERT_ID
                      LEFT JOIN VALIDATED_SIGNAL vs on v.VALIDATED_SIGNAL_ID = vs.id \n
                        """
        sqlQuery += selectedCasesId ? " )b " : " WHERE ${callingScreenQuery} )b "
        joinSql += """
                      LEFT JOIN DISPOSITION disp on disposition::NUMERIC = disp.id         
                      LEFT JOIN PVUSER u on assignedTo::NUMERIC = u.id
                      LEFT JOIN GROUPS g on assignedToGroup::NUMERIC = g.id
                      LEFT JOIN VALIDATED_SINGLE_ALERTS v on ids::NUMERIC = v.SINGLE_ALERT_ID
                      LEFT JOIN VALIDATED_SIGNAL vs on v.VALIDATED_SIGNAL_ID = vs.id         
                      LEFT JOIN PRIORITY pr on priority::NUMERIC = pr.id  
                                """
        sqlQuery += selectedCasesId ? '' : joinSql
        return sqlQuery

    }

    String getIncludedAllStudyDrugs(Configuration configuration) {
        List result = []
        if (configuration.studyCases && (configuration.productSelection || configuration.productGroupSelection)) {
            def products = [:]
            if (configuration.productSelection) {
                JSON.parse(configuration.productSelection).each { key, val ->
                    products[key] = val.collect {
                        it.id
                    }.join(",")
                }
            }

            String productGroups = null
            if (configuration.productGroupSelection) {
                List ids = []
                JSON.parse(configuration.productGroupSelection).each {
                    if (it.id) {
                        ids.add(it.id)
                    }
                }
                productGroups = ids.join(',')
            }

            Sql sql = new Sql(signalDataSourceService.getReportConnection('pva'))
            int includeAllStudyDrugsCases = configuration?.studyCases ? 1 : 0
            int onlyPoi = configuration?.onlyPoi ? 1 : 0
            int ignoreStudyType = configuration?.ignoreStudyType ? 1 : 0
            String insertStatement = "Begin " +
                    "execute immediate('delete from gtt_report_input_params'); "
            try {
                insertStatement += "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('INCLUDE_STUDY_DRUGS','${includeAllStudyDrugsCases}');" +
                        "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('IGNORE_STUDY_MED_TYPE','${ignoreStudyType}');" +
                        "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('CONSIDER_POI_FOR_UNBLINDED','${onlyPoi}');"
                insertStatement += " END;"
                sql.execute(insertStatement)
                sql.call("{?= call pkg_pvr_app_util.f_include_study_drug(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}", [Sql.resultSet(OracleTypes.CURSOR), productGroups, products["1"], products["2"], products["3"], products["4"], products["5"], products["6"], products["7"], products["8"], products["9"], products["10"], products["11"], products["12"], products["13"], products["14"], products["15"], products["16"], products["17"], products["18"], products["19"], products["20"]]) { cursorResults ->
                    MiscUtil.resultSetToList(cursorResults).each { res ->
                        result.push(res.STUDY_NUM)
                    }
                }
                log.info("Get study drug data : ${result}")
            }catch (Exception ex) {
                log.error("Error occurred while getting study drug data",ex)
            } finally {
                sql?.close()
            }
        }
        return result.join(", ")
    }

        Map generateReviewQuery(AlertReviewDTO alertReviewDTO){
        Long userId = getUserService().getCurrentUserId()
        Long workflowGroupId = alertReviewDTO.workflowGrpId
        String groupIds = alertReviewDTO.groupIds
        List<String> filterWithUsersAndGroups = alertReviewDTO.filterWithUsersAndGroups
        def ungroupedAlertGroup = AlertGroup.findAllByIsGrouping(false)
        String idList = ungroupedAlertGroup.collect { it.id }.join(',')
        String searchString = alertReviewDTO.searchValue?.toLowerCase()
        List<Disposition> dispositions = Disposition.list()
        List<Integer> allDispIds = dispositions.collect {it.id}
        List<Integer> closedIds = dispositions.findAll {it-> it.reviewCompleted == true}.collect {it.id}
        String esc_char = ""
        String query = ''
        query = "select * from (select * from (select id as id,type as type, is_latest as is_latest, is_deleted as is_deleted,adhoc_run as adhoc_run, is_enabled as is_enabled, config_id as config_id, name as name, product_name as product_name, description as description, selected_data_source as selected_data_source, data_mining_variable as data_mining_variable, disp_counts as disp_count, num_of_executions as num_of_execution, schedule_date as schedule_date, product_selection as product_selection, product_group_selection as product_group_selection, date_created as date_created, last_updated as last_updated,priority_id as priority_id, false as grouped_entry,alert_group_id as alert_group_id, assigned_to_id as assigned_to_id, EX_ALERT_DATE_RANGE_ID as ex_alert_date_range_id,product_dictionary_selection as product_dictionary_selection from ex_rconfig\n" +
                "where type = 'Aggregate Case Alert' and id not in (select exec_config_id from GROUP_ALERT_EXEC_CONFIG where ALERT_GROUP_INFO_ID in (select id from grouped_alert_info where assigned_to_id = $userId ${idList.size()>0 ? "and alert_group_id not in ($idList)": "" }))\n" +
                "and is_enabled = true and is_latest = true and is_deleted = false and adhoc_run = false and is_master_template_alert = false\n" +
                "UNION ALL\n" +
                "\n" +
                "select id as id,'Aggregate Case Alert' as type, is_latest as is_latest, is_deleted as is_deleted,false as adhoc_run, is_enabled as is_enabled, master_template_config_id as config_id, name as name, null as product_name, null as description, 'pva' as selected_data_source, (SELECT t3.data_mining_variable from ex_rconfig t3 JOIN GROUP_ALERT_EXEC_CONFIG t4 ON t4.EXEC_CONFIG_ID = t3.id WHERE t4.ALERT_GROUP_INFO_ID = t1.id LIMIT 1) as data_mining_variable, disp_counts as disp_count, (SELECT t5.num_of_executions from ex_rconfig t5 JOIN GROUP_ALERT_EXEC_CONFIG t6 ON t6.EXEC_CONFIG_ID = t5.id WHERE t6.ALERT_GROUP_INFO_ID = t1.id LIMIT 1) as num_of_execution, (SELECT t7.schedule_date from ex_rconfig t7 JOIN GROUP_ALERT_EXEC_CONFIG t8 ON t8.EXEC_CONFIG_ID = t7.id WHERE t8.ALERT_GROUP_INFO_ID = t1.id LIMIT 1) as schedule_date, product_selection as product_selection, product_group_selection as product_group_selection, date_created as date_created, last_updated as last_updated,(SELECT t9.priority_id from ex_rconfig t9 JOIN GROUP_ALERT_EXEC_CONFIG t10 ON t10.EXEC_CONFIG_ID = t9.id WHERE t10.ALERT_GROUP_INFO_ID = t1.id LIMIT 1) as priority_id, true as grouped_entry, alert_group_id as alert_group_id, assigned_to_id as assigned_to_id, (SELECT t2.EX_ALERT_DATE_RANGE_ID \n" +
                "     FROM ex_rconfig t2\n" +
                "     JOIN GROUP_ALERT_EXEC_CONFIG mt ON mt.EXEC_CONFIG_ID = t2.id\n" +
                "     WHERE mt.ALERT_GROUP_INFO_ID = t1.id\n" +
                "     LIMIT 1) AS ex_alert_date_range_id,product_dictionary_selection as product_dictionary_selection from grouped_alert_info t1 where is_enabled = true and is_latest = true and is_deleted = false) as a  where (CASE WHEN grouped_entry = false THEN ( config_id in (\n" +
                "    select\n" +
                "        this_.id\n" +
                "    from\n" +
                "        RCONFIG this_ \n" +
                "\n" +
                "    left outer join\n" +
                "        AUTO_SHARE_WITH_GROUP_CONFIG autosharew6_ \n" +
                "            on this_.id=autosharew6_.CONFIG_ID \n" +
                "    left outer join\n" +
                "        GROUPS autosharew4_ \n" +
                "            on autosharew6_.AUTO_SHARE_WITH_GROUPID=autosharew4_.id \n" +
                "    left outer join\n" +
                "        AUTO_SHARE_WITH_USER_CONFIG autosharew8_ \n" +
                "            on this_.id=autosharew8_.CONFIG_ID \n" +
                "    left outer join\n" +
                "        PVUSER autosharew3_ \n" +
                "            on autosharew8_.AUTO_SHARE_WITH_USERID=autosharew3_.id \n" +
                "    left outer join\n" +
                "        SHARE_WITH_GROUP_CONFIG sharewithg10_ \n" +
                "            on this_.id=sharewithg10_.CONFIG_ID \n" +
                "    left outer join\n" +
                "        GROUPS sharewithg2_ \n" +
                "            on sharewithg10_.SHARE_WITH_GROUPID=sharewithg2_.id \n" +
                "    left outer join\n" +
                "        SHARE_WITH_USER_CONFIG sharewithu12_ \n" +
                "            on this_.id=sharewithu12_.CONFIG_ID \n" +
                "    left outer join\n" +
                "        PVUSER sharewithu1_ \n" +
                "            on sharewithu12_.SHARE_WITH_USERID=sharewithu1_.id \n" +
                "    where(\n" +
                "          ( sharewithu1_.id=$userId \n" +
                "            or autosharew3_.id=$userId\n" +
                "            or this_.PVUSER_ID=$userId\n" +
                "            ${groupSql(groupIds)}\n" +
                "            ${autoGroupSql(groupIds)} )\n" +
                "            ${createFilterQuery(filterWithUsersAndGroups)}\n" +
                "        ) \n" +
                "        and this_.WORKFLOW_GROUP=$workflowGroupId\n" +
                "        \n" +
                "        )) \n" +
                "   ELSE assigned_to_id = $userId \n and ${idList?"alert_group_id not in ($idList)":""}" +
                " END)) as foo"
        if (searchString && searchString.contains('_')) {
            searchString = searchString.replaceAll("\\_", "!_")
            esc_char = "!"
        } else if (searchString && searchString.contains('%')) {
            searchString = searchString.replaceAll("\\%", "!%%")
            esc_char = "!"
        }
        String searchStringSql = ""
        if(searchString){
            if(esc_char){
                searchStringSql = "where lower(name) like '%${searchString.replaceAll( "'", "''")}%' escape '${esc_char}' \n" +
                        " OR EXISTS (SELECT 1\n" +
                        "    FROM jsonb_each(product_selection :: jsonb) AS elements(key, value)\n" +
                        "    CROSS JOIN LATERAL jsonb_array_elements(value) AS items\n" +
                        "    WHERE items->>'name' ILIKE '%${searchString.replaceAll("'","''")}%' escape '${esc_char}') \n" +
                        " OR EXISTS (SELECT 1\n" +
                        "    FROM jsonb_array_elements(product_group_selection :: jsonb) AS item\n" +
                        "    WHERE item->>'name' ILIKE '%${searchString.replaceAll("'","''")}%' escape '${esc_char}') \n" +
                        " OR REPLACE(REPLACE(SELECTED_DATA_SOURCE, 'pva', 'safety db'), 'eudra', 'evdas') like '%${searchString.replaceAll("'", "''")}%' \n"

            } else {
                searchStringSql = "where lower(name) like '%${searchString.replaceAll( "'", "''")}%' \n" +
                        " OR EXISTS (SELECT 1\n" +
                        "    FROM jsonb_each(product_selection :: jsonb) AS elements(key, value)\n" +
                        "    CROSS JOIN LATERAL jsonb_array_elements(value) AS items\n" +
                        "    WHERE items->>'name' ILIKE '%${searchString.replaceAll("'","''")}%') \n" +
                        " OR EXISTS (SELECT 1\n" +
                        "    FROM jsonb_array_elements(product_group_selection :: jsonb) AS item\n" +
                        "    WHERE item->>'name' ILIKE '%${searchString.replaceAll("'","''")}%') \n" +
                        " OR REPLACE(REPLACE(SELECTED_DATA_SOURCE, 'pva', 'safety db'), 'eudra', 'evdas') like '%${searchString.replaceAll("'", "''")}%' \n"
            }
        }
        if(alertReviewDTO.orderProperty == "lastUpdated"){
            alertReviewDTO.orderProperty = "last_updated"
        } else if(alertReviewDTO.orderProperty == "dateCreated"){
            alertReviewDTO.orderProperty = "date_created"
        }
        String sortSql = ""
        if(alertReviewDTO.orderProperty == 'closedPecCount') {
            List<String> orderByList2 = []
            closedIds.each {
                orderByList2 <<  "COALESCE((disp_count::jsonb)->>'${it}', '0')::integer"
            }
            String orderByStr2 = orderByList2.join("+")
            sortSql +=" ORDER BY ${orderByStr2} ${alertReviewDTO.direction == 'asc' ? Query.Order.Direction.ASC : Query.Order.Direction.DESC} , id ${Query.Order.Direction.ASC} "
        } else if(alertReviewDTO.orderProperty == 'pecCount') {
            List<String> orderByList3 = []
            allDispIds.each {
                orderByList3 <<  "COALESCE((disp_count::jsonb)->>'${it}', '0')::integer"
            }
            String orderByStr3 = orderByList3.join("+")
            sortSql += " ORDER BY ${orderByStr3} ${alertReviewDTO.direction == 'asc' ? Query.Order.Direction.ASC : Query.Order.Direction.DESC} , id ${Query.Order.Direction.ASC} "
        } else if(alertReviewDTO.orderProperty == 'selectedDataSource'){
            sortSql += " ORDER BY lower(selected_data_source) ${alertReviewDTO.direction == 'asc' ? Query.Order.Direction.ASC : Query.Order.Direction.DESC}"
        } else {
            sortSql += " ORDER BY $alertReviewDTO.orderProperty ${alertReviewDTO.direction == 'asc' ? Query.Order.Direction.ASC : Query.Order.Direction.DESC}"
        }
        String totalCountSql = "select * from (select id as id,type as type, is_latest as is_latest, is_deleted as is_deleted,adhoc_run as adhoc_run, is_enabled as is_enabled, config_id as config_id, name as name, product_name as product_name, description as description, selected_data_source as selected_data_source, data_mining_variable as data_mining_variable, disp_counts as disp_count, num_of_executions as num_of_execution, schedule_date as scheduled_date, product_selection as product_selection, product_group_selection as product_group_selection, date_created as date_created, last_updated as last_updated,priority_id as priority_id, false as grouped_entry,alert_group_id as alert_group_id, assigned_to_id as assigned_to_id,EX_ALERT_DATE_RANGE_ID as ex_alert_date_range_id from ex_rconfig\n" +
        "where type = 'Aggregate Case Alert' and id not in (select exec_config_id from GROUP_ALERT_EXEC_CONFIG where ALERT_GROUP_INFO_ID in (select id from grouped_alert_info where assigned_to_id = $userId ${idList.size() > 0 ? "and alert_group_id not in ($idList)": "" }))\n" +
                "and is_enabled = true and is_latest = true and is_deleted = false and adhoc_run = false and is_master_template_alert = false\n" +
                "UNION ALL\n" +
                "\n" +
                "select id as id,'Aggregate Case Alert' as type, is_latest as is_latest, is_deleted as is_deleted,false as adhoc_run, is_enabled as is_enabled, master_template_config_id as config_id, name as name, null as product_name, null as description, 'pva' as selected_data_source, (SELECT t3.data_mining_variable from ex_rconfig t3 JOIN GROUP_ALERT_EXEC_CONFIG t4 ON t4.EXEC_CONFIG_ID = t3.id WHERE t4.ALERT_GROUP_INFO_ID = t1.id LIMIT 1) as data_mining_variable, null as disp_count, null as num_of_execution, null as schedule_date, product_selection as product_selection, product_group_selection as product_group_selection, date_created as date_created, last_updated as last_updated,null as priority_id, true as grouped_entry, alert_group_id as alert_group_id, assigned_to_id as assigned_to_id, (SELECT t2.EX_ALERT_DATE_RANGE_ID \n" +
                "     FROM ex_rconfig t2\n" +
                "     JOIN GROUP_ALERT_EXEC_CONFIG mt ON mt.EXEC_CONFIG_ID = t2.id\n" +
                "     WHERE mt.ALERT_GROUP_INFO_ID = t1.id\n" +
                "     LIMIT 1) AS ex_alert_date_range_id from grouped_alert_info t1 where is_enabled = true and is_latest = true and is_deleted = false) as a  where (CASE WHEN grouped_entry = false THEN ( config_id in (\n" +
                "    select\n" +
                "        this_.id\n" +
                "    from\n" +
                "        RCONFIG this_ \n" +
                "\n" +
                "    left outer join\n" +
                "        AUTO_SHARE_WITH_GROUP_CONFIG autosharew6_ \n" +
                "            on this_.id=autosharew6_.CONFIG_ID \n" +
                "    left outer join\n" +
                "        GROUPS autosharew4_ \n" +
                "            on autosharew6_.AUTO_SHARE_WITH_GROUPID=autosharew4_.id \n" +
                "    left outer join\n" +
                "        AUTO_SHARE_WITH_USER_CONFIG autosharew8_ \n" +
                "            on this_.id=autosharew8_.CONFIG_ID \n" +
                "    left outer join\n" +
                "        PVUSER autosharew3_ \n" +
                "            on autosharew8_.AUTO_SHARE_WITH_USERID=autosharew3_.id \n" +
                "    left outer join\n" +
                "        SHARE_WITH_GROUP_CONFIG sharewithg10_ \n" +
                "            on this_.id=sharewithg10_.CONFIG_ID \n" +
                "    left outer join\n" +
                "        GROUPS sharewithg2_ \n" +
                "            on sharewithg10_.SHARE_WITH_GROUPID=sharewithg2_.id \n" +
                "    left outer join\n" +
                "        SHARE_WITH_USER_CONFIG sharewithu12_ \n" +
                "            on this_.id=sharewithu12_.CONFIG_ID \n" +
                "    left outer join\n" +
                "        PVUSER sharewithu1_ \n" +
                "            on sharewithu12_.SHARE_WITH_USERID=sharewithu1_.id \n" +
                "    where(\n" +
                "          ( sharewithu1_.id=$userId \n" +
                "            or autosharew3_.id=$userId\n" +
                "            or this_.PVUSER_ID=$userId\n" +
                "            ${groupSql(groupIds)}\n" +
                "            ${autoGroupSql(groupIds)} )\n" +
                "            ${createFilterQuery(filterWithUsersAndGroups)}\n" +
                "        ) \n" +
                "        and this_.WORKFLOW_GROUP=$workflowGroupId\n" +
                "        \n" +
                "        )) \n" +
                "   ELSE assigned_to_id = $userId \n ${idList.size() > 0 ? "and alert_group_id not in ($idList)": "" }" +
                " END)"
        String filteredCountSql = query + " " + searchStringSql
        String finalSql = "SELECT * FROM ( " + query + " " + searchStringSql + sortSql + ") as subquery LIMIT ${alertReviewDTO.max} OFFSET ${alertReviewDTO.offset}"
        return [finalSql : finalSql, totalCountSql : totalCountSql, filteredCountSql: filteredCountSql]
    }

    static String groupSql(String groupIds){
        if(groupIds) {
            return """
            or (
                sharewithg2_.id in (
                    $groupIds
                )
            ) 
            """
        }
        ""
    }
    static String autoGroupSql(String groupIds){
        if(groupIds) {
            return """
            or (
                autosharew4_.id in (
                    $groupIds
                )
            ) 
            """
        }
        ""
    }
    static String createFilterQuery(List<String> filterWithUsersAndGroups ) {
        String filterAlertsQuery = ""
        if(filterWithUsersAndGroups.size()) {
            filterAlertsQuery = " and ( "
            filterWithUsersAndGroups.each {it ->
                if(it.contains("User_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "this_.ASSIGNED_TO_ID=${extractedId} or "
                }
                else if(it.contains("UserGroup_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "this_.ASSIGNED_TO_GROUP_ID=${extractedId} or "
                }
                else if(it.contains("Mine_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "this_.PVUSER_ID=${extractedId} or "
                }
                else if(it.contains("AssignToMe_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "this_.ASSIGNED_TO_ID=${extractedId} or "
                    User loggedInUser = User.findById(extractedId as Long)
                    Set<Group> allGroupsContainsUser = Group.findAllUserGroupByUser(loggedInUser)
                    allGroupsContainsUser.each {
                        filterAlertsQuery += "this_.ASSIGNED_TO_GROUP_ID=${it.id} or "
                    }
                }
                else if(it.contains("SharedWithMe_")) {
                    String extractedId = it.substring(it.indexOf('_')+1)
                    filterAlertsQuery += "sharewithu1_.id=${extractedId} or autosharew3_.id=${extractedId} or "
                    User loggedInUser = User.findById(extractedId as Long)
                    Set<Group> allGroupsContainsUser = Group.findAllUserGroupByUser(loggedInUser)
                    allGroupsContainsUser.each {
                        filterAlertsQuery += "sharewithg2_.id=${it.id} or autosharew4_.id=${it.id} or "
                    }
                }
            }
            filterAlertsQuery = filterAlertsQuery.substring(0,filterAlertsQuery.length()-3)
            filterAlertsQuery += " )"
        }
        filterAlertsQuery
    }
}
