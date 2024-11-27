package com.rxlogix

import com.rxlogix.config.AdvancedFilter
import com.rxlogix.config.EvdasAlert
import com.rxlogix.config.EvdasOnDemandAlert
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.AlertComment
import com.rxlogix.signal.GlobalCaseCommentMapping
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.config.Configuration
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.SingleOnDemandAlert
import com.rxlogix.signal.CaseHistory
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.ViewInstance
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.SignalQueryHelper
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import org.grails.web.json.JSONObject
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.sql.JoinType
import com.rxlogix.signal.AggregateOnDemandAlert
import com.rxlogix.pvdictionary.config.PVDictionaryConfig

@Transactional
class AdvancedFilterService {
    def userService
    def viewInstanceService
    def sessionFactory
    def grailsApplication
    def dictionaryGroupService
    def springSecurityService
    def alertFieldService
    def cacheService

    private
    static List<String> countColumn = ["newSponCount", "cumSponCount", "newStudyCount", "cumStudyCount", "newSeriousCount",
                                       "cumSeriousCount", "newGeriatricCount", "cumGeriatricCount", "newNonSerious",
                                       "cumNonSerious", "newFatalCount", "cumFatalCount", "eb05", "eb95", "ebgm", "prrValue",
                                       "prrLCI", "prrUCI", "rorValue", "rorLCI", "rorUCI", "newEv", "totalEv", "newSerious",
                                       "totalSerious", "newFatal", "totalFatal", "newRc", "totRc", "newLit", "totalLit",
                                       "newPaed", "totPaed", "newGeria", "totGeria", "newEea", "totEea", "newMedErr", "totMedErr",
                                       "newHcp", "totHcp", "newSpont", "totSpont", "newObs", "totObs", "ratioRorPaedVsOthers",
                                       "ratioRorGeriatrVsOthers", "totSpontRest", "totSpontAsia", "totSpontJapan",
                                       "totSpontNAmerica", "totSpontEurope", "asiaRor", "restRor", "japanRor", "europeRor",
                                       "northAmericaRor", "newCount", "cummCount", "newPediatricCount", "cummPediatricCount",
                                       "newInteractingCount", "cummInteractingCount", "chiSquare", "completenessScore",
                                       "patientAge", "medErrorPtCount","timeToOnset","newProdCount","cumProdCount",
                                       "freqPeriod", "cumFreqPeriod","reviewedFreqPeriod","reviewedCumFreqPeriod","pecImpNumHigh",
                                       "aValue", "bValue", "cValue", "dValue", "eValue", "rrValue"]
    private
    static List<String> clobColumn = ["caseNarrative", "conComitList", "ptList", "suspectProductList", "medErrorPtList",
                                      "aggImpEventList", "evImpEventList","indication","causeOfDeath","patientMedHist","patientHistDrugs",
                                      "batchLotNo","caseClassification","therapyDates","doseDetails","primSuspProdList","primSuspPaiList","paiAllList","allPtList", "genericName","allPTsOutcome","crossReferenceInd"]

    private
    static List<String> newClobColumn = ["smqNarrow"]

    private static Map<String, List<String>> fieldMap = ['ptList'                : ['SCA_PT', 'SINGLE_ALERT_PT'],
                                                         'conComitList'          : ['ALERT_CON_COMIT', 'SINGLE_ALERT_CON_COMIT'],
                                                         'medErrorPtList'        : ['SCA_MED_ERROR', 'SINGLE_ALERT_MED_ERR_PT_LIST'],
                                                         'suspectProductList'    : ['SCA_PRODUCT_NAME', 'SINGLE_ALERT_SUSP_PROD'],
                                                         'indication'            : ['SCA_INDICATION', 'SINGLE_ALERT_INDICATION_LIST'],
                                                         'causeOfDeath'          : ['SCA_CAUSE_OF_DEATH', 'SINGLE_ALERT_CAUSE_OF_DEATH'],
                                                         'patientMedHist'        : ['SCA_PAT_MED_HIST', 'SINGLE_ALERT_PAT_MED_HIST'],
                                                         'patientHistDrugs'      : ['SCA_PAT_HIST_DRUGS', 'SINGLE_ALERT_PAT_HIST_DRUGS'],
                                                         'batchLotNo'            : ['SCA_BATCH_LOT_NO', 'SINGLE_ALERT_BATCH_LOT_NO'],
                                                         'caseClassification'    : ['SCA_CASE_CLASSIFICATION', 'SINGLE_ALERT_CASE_CLASSIFI'],
                                                         'therapyDates'          : ['SCA_THERAPY_DATES', 'SINGLE_ALERT_THERAPY_DATES'],
                                                         'doseDetails'           : ['SCA_DOSE_DETAILS', 'SINGLE_ALERT_DOSE_DETAILS'],
                                                         'primSuspProdList'      : ['SCA_PRIM_SUSP', 'SINGLE_ALERT_PRIM_SUSP'],
                                                         'primSuspPaiList'       : ['SCA_PRIM_PAI', 'SINGLE_ALERT_PRIM_PAI'],
                                                         'paiAllList'            : ['SCA_ALL_PAI', 'SINGLE_ALERT_ALL_PAI'],
                                                         'allPtList'             : ['SCA_ALL_PT', 'SINGLE_ALERT_ALL_PT'],
                                                         'genericName'           : ['GENERIC_NAME','SINGLE_ALERT_GENERIC_NAME'],
                                                         'allPTsOutcome'         : ['ALLPTS_OUTCOME','SINGLE_ALERT_ALLPT_OUT_COME'],
                                                         'crossReferenceInd'     : ['CROSS_REFERENCE_IND','SINGLE_ALERT_CROSS_REFERENCE_IND']]

    private static Map<String, List<String>> fieldOnDemandMap = ['ptList'    : ['PT', 'SINGLE_ALERT_OD_PT'],
                                                                 'conComitList'      : ['CON_COMIT', 'SINGLE_ALERT_OD_CON_COMIT'],
                                                                 'medErrorPtList'    : ['MED_ERROR', 'SINGLE_ALERT_OD_MED_ERR'],
                                                                 'suspectProductList': ['PRODUCT_NAME', 'SINGLE_ALERT_OD_SUSP_PROD'],
                                                                 'indication'            : ['SCA_INDICATION', 'SINGLE_DEMAND_INDICATION_LIST'],
                                                                 'causeOfDeath'          : ['SCA_CAUSE_OF_DEATH', 'SINGLE_DEMAND_CAUSE_OF_DEATH'],
                                                                 'patientMedHist'        : ['SCA_PAT_MED_HIST', 'SINGLE_DEMAND_PAT_MED_HIST'],
                                                                 'patientHistDrugs'      : ['SCA_PAT_HIST_DRUGS', 'SINGLE_DEMAND_PAT_HIST_DRUGS'],
                                                                 'batchLotNo'            : ['SCA_BATCH_LOT_NO', 'SINGLE_DEMAND_BATCH_LOT_NO'],
                                                                 'caseClassification'    : ['SCA_CASE_CLASSIFICATION', 'SINGLE_DEMAND_CASE_CLASSIFI'],
                                                                 'therapyDates'          : ['SCA_THERAPY_DATES', 'SINGLE_DEMAND_THERAPY_DATES'],
                                                                 'doseDetails'           : ['SCA_DOSE_DETAILS', 'SINGLE_DEMAND_DOSE_DETAILS'],
                                                                 'primSuspProdList'      : ['SCA_PRIM_SUSP', 'SINGLE_DEMAND_PRIM_SUSP'],
                                                                 'primSuspPaiList'      : ['SCA_PRIM_PAI', 'SINGLE_DEMAND_PRIM_PAI'],
                                                                 'paiAllList'            : ['SCA_ALL_PAI', 'SINGLE_DEMAND_ALL_PAI'],
                                                                 'allPtList'             : ['SCA_ALL_PT', 'SINGLE_DEMAND_ALL_PT'],
                                                                 'genericName'           : ['GENERIC_NAME', 'SIG_DMD_ALRT_GENERIC_NAME'],
                                                                 'allPTsOutcome'         : ['ALLPTS_OUTCOME', 'SIG_DMD_ALRT_ALLPT_OUT_COME'],
                                                                 'crossReferenceInd'     : ['CROSS_REFERENCE_IND','SIG_DMD_ALRT_CROSS_REFERENCE_IND']]

    private static List<String> integratedReviewColumn = ["newCountFaers", "cummCountFaers", "newPediatricCountFaers", "cummPediatricCountFaers",
                                                          "newInteractingCountFaers", "cummInteractingCountFaers", "eb05Faers", "eb95Faers", "ebgmFaers", "prrValueFaers",
                                                          "prrLCIFaers", "prrUCIFaers", "rorValueFaers", "rorLCIFaers", "rorUCIFaers","newSeriousCountFaers",
                                                          "cumSeriousCountFaers","newSponCountFaers","cumSponCountFaers","newStudyCountFaers",
                                                          "cumStudyCountFaers","newFatalCountFaers","cumFatalCountFaers","chiSquareFaers",
                                                          "impEventsFaers","freqPriorityFaers","positiveRechallengeFaers","positiveDechallengeFaers",
                                                          "listedFaers","relatedFaers","pregenencyFaers","trendTypeFaers",
                                                          "newEeaEvdas","totEeaEvdas", "newHcpEvdas","totHcpEvdas", "newSeriousEvdas","totalSeriousEvdas", "newMedErrEvdas","totMedErrEvdas",
                                                          "newObsEvdas","totObsEvdas", "newFatalEvdas", "totalFatalEvdas","newRcEvdas","totRcEvdas", "newLitEvdas","totalLitEvdas", "newPaedEvdas", "totPaedEvdas","ratioRorPaedVsOthersEvdas", "newGeriaEvdas","totGeriaEvdas", "ratioRorGeriatrVsOthersEvdas",
                                                          "sdrGeratrEvdas", "newSpontEvdas","totSpontEvdas", "totSpontEuropeEvdas", "totSpontNAmericaEvdas", "totSpontJapanEvdas", "totSpontAsiaEvdas", "totSpontRestEvdas",
                                                          "sdrPaedEvdas", "europeRorEvdas", "northAmericaRorEvdas", "japanRorEvdas", "asiaRorEvdas", "restRorEvdas", "newEvEvdas","totalEvEvdas",
                                                          "dmeImeEvdas","sdrEvdas","hlgtEvdas","hltEvdas","smqNarrowEvdas","impEventsEvdas",
                                                          "changesEvdas","listedEvdas" , "allRorEvdas", "newCountVaers", "cummCountVaers", "newFatalCountVaers", "cumFatalCountVaers", "newSeriousCountVaers", "cumSeriousCountVaers",
                                                          "newGeriatricCountVaers", "cumGeriatricCountVaers", "newPediatricCountVaers", "cummPediatricCountVaers", "eb05Vaers", "eb95Vaers", "ebgmVaers", "prrValueVaers", "prrLCIVaers",
                                                          "prrUCIVaers", "rorValueVaers", "rorLCIVaers", "rorUCIVaers", "chiSquareVaers",
                                                           "newCountVigibase", "cummCountVigibase", "newFatalCountVigibase", "cumFatalCountVigibase", "newSeriousCountVigibase", "cumSeriousCountVigibase",
                                                          "newGeriatricCountVigibase", "cumGeriatricCountVigibase", "newPediatricCountVigibase", "cummPediatricCountVigibase", "eb05Vigibase", "eb95Vigibase", "ebgmVigibase", "prrValueVigibase", "prrLCIVigibase",
                                                          "prrUCIVigibase", "rorValueVigibase", "rorLCIVigibase", "rorUCIVigibase", "chiSquareVigibase",
                                                          "newCountJader", "cumCountJader", "newFatalCountJader", "cumFatalCountJader", "newSeriousCountJader", "cumSeriousCountJader",
                                                          "newGeriatricCountJader", "cumGeriatricCountJader", "newPediatricCountJader", "cumPediatricCountJader", "eb05Jader", "eb95Jader", "ebgmJader", "prrValueJader", "prrLCIJader",
                                                          "prrUCIJader", "rorValueJader", "rorLCIJader", "rorUCIJader", "chiSquareJader",
                                                          "aValueJader", "bValueJader", "cValueJader", "dValueJader", "eValueJader", "rrValueJader"]

    List maxSeqCommentList(def domainName, Long configId, Boolean isFaers){
        List finalCommentList = [ ]
        List commentList = []
        switch (domainName) {
            case SingleCaseAlert:
                def domain = isFaers?GlobalCaseCommentMapping."faers":GlobalCaseCommentMapping."pva"
                commentList = domain.createCriteria().list {
                    resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                    projections {
                        property("caseId", "caseId")
                        property("commentSeqNum", "commentSeqNum")
                        property("comments", "comments")

                    }
                    order('commentSeqNum', 'desc')
                }

                commentList.groupBy { it.caseId }.each { key, value ->
                    finalCommentList.add(value.find { it.commentSeqNum == value.commentSeqNum.max() })
                }
                break
            case AggregateCaseAlert:
                finalCommentList = AlertComment.createCriteria().list {
                    resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                    projections {
                        property("comments", "comments")

                    }
                    isNotNull("productId")
                    isNotNull("eventName")
                    isNotNull("configId")
                    eq('alertType', Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
                    if(configId) {
                        eq("configId", configId)
                    }
                } as List
                break
            case EvdasAlert:
                finalCommentList = AlertComment.createCriteria().list {
                    resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                    projections {
                        property("comments", "comments")

                    }
                    isNotNull("productName")
                    isNotNull("eventName")
                    isNotNull("configId")
                    eq('alertType', Constants.AlertConfigType.EVDAS_ALERT)
                    if(configId) {
                        eq("configId", configId)
                    }
                } as List
        }
        finalCommentList
    }

    Map getAjaxFilterData(String term, int offset, int max, Long executedConfigId, String field, def domainName, Map filterMap) {
        Boolean isFaers = filterMap.isFaers
        List jsonData = []
        int listSize = 0
        List<String> possibleValuesList = []
        def pgMap = [:]
        Boolean isPg = term.contains('PG_')
        Boolean isEg = term.contains('EG_')
        def termList = term.split('_')
        if ((isPg || isEg) && termList[1]) {
            pgMap = dictionaryGroupService.fetchList(isPg ? PVDictionaryConfig.PRODUCT_GRP_TYPE : PVDictionaryConfig.EVENT_GRP_TYPE, termList[1]?.trim(), "pva", 1, 30, springSecurityService.principal?.username)?.items.collect {
                [id: termList[0] + '_' + it.id, text: it.name]
            }
            pgMap += dictionaryGroupService.fetchList(isPg ? PVDictionaryConfig.PRODUCT_GRP_TYPE : PVDictionaryConfig.EVENT_GRP_TYPE, termList[1]?.trim(), "pva", 1, 30, springSecurityService.principal?.username, false, true)?.items.collect {
                [id: termList[0] + '_' + it.id, text: it.name]
            }
        } else {
            if (field in [Constants.AdvancedFilter.COMMENTS, Constants.AdvancedFilter.COMMENT]) {
                Long configId = 0L
                if (executedConfigId) {
                    configId = ExecutedConfiguration.get(executedConfigId)?.configId
                }
                List finalCommentList = maxSeqCommentList(domainName, configId, isFaers)
                possibleValuesList = finalCommentList.findAll { it?.comments?.toUpperCase()?.contains(term?.toUpperCase()) }?.
                        comments?.collect { it?.trim() }?.unique()?.sort { val1, val2 -> val1?.toUpperCase() <=> val2?.toUpperCase() } as List<String>

                listSize = possibleValuesList.size()
                int startIdx = offset
                int maxIdx = offset + max
                int endIdx = Math.min(maxIdx, listSize)
                if (listSize > 0) {
                    possibleValuesList = possibleValuesList.subList(startIdx, endIdx)
                }
            } else if (field in clobColumn || field in newClobColumn) {
                getDistinctValuesClobFields(term, executedConfigId, field, offset, max, possibleValuesList, domainName)
            } else {
                possibleValuesList = domainName.createCriteria().list {
                    projections {
                        distinct(field)
                    }
                    ilike(field, '%' + term + '%')
                    if (executedConfigId > 0) {
                        eq("executedAlertConfiguration.id", executedConfigId)
                    } else {
                        'executedAlertConfiguration' {
                            eq('adhocRun', false)
                        }
                    }
                    maxResults(max)
                    firstResult(offset)
                } as List<String>
            }
        }
        if(field in [Constants.AdvancedFilter.COMMENTS, Constants.AdvancedFilter.COMMENT]){
            possibleValuesList.each {
                // Select2 needs both an id and text. Both fields are used in the UI.
                jsonData << new JSONObject(id: it, text: it)
            }
        } else {
            if (isEg || isPg) {
                pgMap.each {
                    jsonData << new JSONObject(id: it.id, text: it.text)
                }
            } else {
                possibleValuesList.unique().sort().each {
                    // Select2 needs both an id and text. Both fields are used in the UI.
                    jsonData << new JSONObject(id: it, text: it)
                }
            }
        }
        [jsonData:jsonData, possibleValuesListSize:listSize ]
    }


    Long getAjaxFilterDataTotalCount(String term, Long executedConfigId, String field, def domainName) {
        if (field in clobColumn) {
            return getCountOfClobFields(term, executedConfigId, field, domainName)
        } else {
            List<Long> totalCountList = domainName.createCriteria().list {
                projections {
                    countDistinct(field)
                }
                if (executedConfigId > 0) {
                    eq("executedAlertConfiguration.id", executedConfigId)
                } else {
                    'executedAlertConfiguration' {
                        eq('adhocRun', false)
                    }
                }
                ilike(field, '%' + term + '%')
            } as List<Long>
            return totalCountList[0]
        }
    }

    void getDistinctValuesClobFields(String term, Long executedConfigId, String field, int offset, int max, List<String> distinctValuesList, def domainName) {
        String sql
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.findById(executedConfigId)
        Session session = sessionFactory.currentSession
        if (domainName == SingleCaseAlert) {
            if(executedConfiguration?.adhocRun) {
                sql = SignalQueryHelper.distinct_values_for_clob_columns_adhoc_alert(fieldMap.get(field)[0], executedConfigId, fieldMap.get(field)[1], term, offset, max)
            }
            else{
                sql = SignalQueryHelper.distinct_values_for_clob_columns(fieldMap.get(field)[0], executedConfigId, fieldMap.get(field)[1], term, offset, max)
            }
        } else {
            if(field in newClobColumn){
                String tableName = ''
                if (domainName == AggregateCaseAlert) {
                    tableName = "AGG_ALERT"
                } else {
                    tableName = "AGG_ON_DEMAND_ALERT"
                }
                sql = SignalQueryHelper.distinct_advance_filter_new_column_clob_list("NEW_COUNTS_JSON",field,tableName,executedConfigId,term,offset,max)
            }else {
                sql = SignalQueryHelper.distinct_values_for_clob_on_demand_columns(fieldOnDemandMap.get(field)[0], executedConfigId, fieldOnDemandMap.get(field)[1], term, offset, max)
            }
        }
        println sql
        SQLQuery sqlQuery = session.createSQLQuery(sql)
        sqlQuery.list().each { row ->
            if(field in newClobColumn){
                String[] allSmqList = row?.split('<br>')
                allSmqList.each { it ->
                    if (it && it.length() > 2) {
                       String newSmqTerm =  it.substring(3, it.length()).trim()
                        if(newSmqTerm?.toUpperCase()?.contains(term?.toUpperCase())) {
                            distinctValuesList.add(newSmqTerm)
                        }
                    }
                }
            }else {
                distinctValuesList.add(row)
            }
        }
    }

    Integer getCountOfClobFields(String term, Long executedConfigId, String field, def domainName) {
        String sql
        Session session = sessionFactory.currentSession
        if (domainName == SingleCaseAlert) {
            sql = SignalQueryHelper.distinct_count_for_clob_columns(fieldMap.get(field)[0], executedConfigId, fieldMap.get(field)[1], term)
        } else {
            sql = SignalQueryHelper.distinct_count_for_clob_on_demand_columns(fieldOnDemandMap.get(field)[0], executedConfigId, fieldOnDemandMap.get(field)[1], term)
        }
        SQLQuery sqlQuery = session.createSQLQuery(sql)
        sqlQuery.list()[0]
    }

    List<Map> getAjaxFilterUserData(String term, int offset, int max) {
        List<Map> userList = User.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("id", "id")
                property("fullName", "text")
            }
            eq("enabled", true)
            ilike('fullName', '%' + term + '%')
            maxResults(max)
            firstResult(offset)
        } as List<Map>
        userList
    }

    Integer ajaxFilterUserCount(String term) {
        User.countByEnabledAndFullNameIlike(true, "%${term}%")
    }

    List<Map> getAjaxFilterGroupData(String term, int offset, int max) {
        List<Map> groupList = Group.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("id", "id")
                property("name", "text")
            }
            ilike('name', '%' + term + '%')
            maxResults(max)
            firstResult(offset)
        } as List<Map>
        groupList
    }

    Integer ajaxFilterGroupCount(String term) {
        Group.countByNameIlike("%${term}%")
    }

    String createAdvancedFilterCriteria(String JSONQuery, Long exConfigId = null,def domainName = null) {
        JsonSlurper jsonSlurper = new JsonSlurper()
        Map object = jsonSlurper.parseText(JSONQuery)
        Map expressionObj = object.all.containerGroups[0]
        StringBuilder criteria = new StringBuilder()
        criteria.append('{ ->\n')
        generateCriteria(expressionObj, criteria, exConfigId,domainName)
        criteria.append('}')
        criteria.toString()
    }

    private void generateCriteria(Map expressionObj, StringBuilder criteria, Long exConfigId, def domainName) {
        boolean isCumCount=false
        if (expressionObj.containsKey('keyword')) {
            criteria.append(expressionObj.keyword + " { \n")
            for (int i = 0; i < expressionObj.expressions.size(); i++) {
                generateCriteria(expressionObj.expressions[i], criteria, exConfigId, domainName)
            }
            criteria.append("}\n")
        } else {
            if (expressionObj.containsKey('expressions')) {
                for (int i = 0; i < expressionObj.expressions.size(); i++) {
                    generateCriteria(expressionObj.expressions[i], criteria, exConfigId, domainName)
                }
            } else {
                List newFields = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT',true).collect{
                    it.name
                }
                List newAdhocColumnList = alertFieldService.getAggOnDemandColumnList(Constants.DataSource.PVA,false,true).findAll{it.secondaryName != null}.collect{it.secondaryName}
                newAdhocColumnList += alertFieldService.getAggOnDemandColumnList(Constants.DataSource.PVA,false,true).collect{it.name}
                if (expressionObj.field in countColumn) {
                    criteria.append("criteriaConditionsCount('${expressionObj.field}','${expressionObj.op}','${expressionObj.value}')\n")
                } else if (expressionObj.field == 'caseInitReceiptDate' || expressionObj.field == 'lockedDate' || expressionObj.field == 'dueDate' || expressionObj.field == 'dispLastChange' || expressionObj.field == 'caseCreationDate') {
                    criteria.append("criteriaConditionsDate('${expressionObj.field}','${expressionObj.op}','${expressionObj.value}')\n")
                } else if (expressionObj.field in clobColumn) {
                    expressionObj.value = (expressionObj.field == 'caseClassification') ? expressionObj.value?.replaceAll('\n', '\\\\n') : expressionObj.value
                    String escapedValue = escapeSpecialCharactersGroovyCriteria(expressionObj.value)
                    criteria.append("criteriaConditionsForCLOB('${expressionObj.field}','${expressionObj.op}',\"${escapedValue}\",${exConfigId?:"exConfigId"})\n")
                } else if (expressionObj.field == 'tags' || expressionObj.field == 'subTags' || expressionObj.field == 'currentRun') {
                    criteria.append("criteriaConditionsTags('${expressionObj.field}','${expressionObj.op}',\"${expressionObj.value}\",${exConfigId}, groupedExConfig)\n")
                } else if (expressionObj.field.toString().startsWith("EBGM:") || expressionObj.field.toString().startsWith("EB95:") || expressionObj.field.toString().startsWith("EB05:") ||
                        expressionObj.field.toString().startsWith("ROR:") || expressionObj.field.toString().startsWith("ROR_LCI:") || expressionObj.field.toString().startsWith("ROR_UCI:") ||
                        expressionObj.field.toString().startsWith("PRR:") || expressionObj.field.toString().startsWith("PRR_LCI:") || expressionObj.field.toString().startsWith("PRR_UCI:") || expressionObj.field.toString().startsWith("Chi-Square:")) {
                    criteria.append("criteriaConditionsForSubGroup('${expressionObj.field}','${expressionObj.op}',\"${expressionObj.value}\")\n")
                }else if (expressionObj.field.toString().startsWith("ROR-R:") || expressionObj.field.toString().startsWith("ROR_LCI-R:") || expressionObj.field.toString().startsWith("ROR_UCI-R:")) {
                    criteria.append("criteriaConditionsForRelSubGroup('${expressionObj.field}','${expressionObj.op}',\"${expressionObj.value}\")\n")
                } else if (expressionObj.field.toString().startsWith("EBGMFAERS:") || expressionObj.field.toString().startsWith("EB95FAERS:") || expressionObj.field.toString().startsWith("EB05FAERS:")) {
                    criteria.append("criteriaConditionsForSubGroupFaers('${expressionObj.field}','${expressionObj.op}',\"${expressionObj.value}\")\n")
                } else if (expressionObj.field in integratedReviewColumn) {
                    criteria.append("criteriaConditionsForIntegratedReview('${expressionObj.field}','${expressionObj.op}',\"${expressionObj.value}\")\n")
                }else if((expressionObj.field in newAdhocColumnList && domainName == AggregateOnDemandAlert) || (  expressionObj.field in ['hlt','hlgt','smqNarrow'] && domainName!=EvdasAlert)){
                    criteria.append("criteriaConditionsForNewColumnsAdhoc('${expressionObj.field}','${expressionObj.op}',\"${expressionObj.value}\")\n")
                } else if((expressionObj.field in newFields || expressionObj.field.toString().replace("_cum","") in newFields) && domainName!=EvdasAlert) {
                    criteria.append("criteriaConditionsForNewColumns(${isCumCount},'${expressionObj.field}','${expressionObj.op}',\"${expressionObj.value}\")\n")
                } else {
                    if (expressionObj.field in ["dispPerformedBy"] && expressionObj.value?.contains(Constants.Commons.SYSTEM)) {
                        expressionObj.value = expressionObj.value.replaceAll(Constants.Commons.SYSTEM, Constants.SYSTEM_USER)
                    }
                    String escapedValue = escapeSpecialCharactersGroovyCriteria(expressionObj.value)
                    criteria.append("criteriaConditions('${expressionObj.field}','${expressionObj.op}',\"${escapedValue}\")\n")
                }
            }
        }
    }

    String escapeSpecialCharactersGroovyCriteria(String name) {
        // Define the regular expression pattern to match special characters
        def specialCharacters = /["'$]/
        // Replace special characters and newline characters with escaped versions
        return name.replaceAll(specialCharacters) { match -> '\\' + match}.replaceAll(/\n/, /\\r\\n/)
    }


    def getAjaxAdvFilter(String alertType, String term, int offset, int max, Boolean isDashboard) {
        List filterData = []
        def alertFields = cacheService.getAlertFields('AGGREGATE_CASE_ALERT').collectEntries {
            b -> [b.name, b.enabled]
        }
        Long currentUserId = userService.getCurrentUserId()
        if(alertType== Constants.AlertConfigType.SINGLE_CASE_ALERT && isDashboard==true){
            alertType=Constants.AlertConfigType.SINGLE_CASE_ALERT_DASHBOARD
        }
        String groupIds = userService.user.groups?.collect { it.id }.join(",")
        List<AdvancedFilter> possibleValuesList = AdvancedFilter.createCriteria().list(max: max, offset: offset, sort: "name") {
            resultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
            eq("alertType", alertType)
            iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(term)}%")
            sqlRestriction(""" {alias}.id in
                            (
                                select id from ADVANCED_FILTER afa
                                left join SHARE_WITH_USER_FILTER swua ON (afa.id =swua.ADVANCED_FILTER_ID)
                                left join SHARE_WITH_GROUP_FILTER swga ON (afa.id =swga.ADVANCED_FILTER_ID)
                                where afa.user_id=${currentUserId} OR swua.USER_ID = ${currentUserId} ${
                groupIds ? 'OR swga.GROUP_ID in (' + groupIds + ')' : ''
            }
                            )
                        """)
            if (alertType.startsWith("Agg")) {
                def alertFieldsWithGetValueFalse = []
                for (Map.Entry<String, Object> entry : alertFields.entrySet()) {
                    if (entry.getValue() == false) {
                        alertFieldsWithGetValueFalse.add(entry.getKey());
                    }
                }
                if(alertFieldsWithGetValueFalse.size()>0) {
                    not {
                        for (Map.Entry<String, Object> entry : alertFields.entrySet()) {
                            if (entry.getValue() == false) {
                                ilike('criteria', "%'" + entry.getKey() + "'%")
                            }
                        }
                    }
                }
            }
            if (!isDashboard) {
                not {
                    ilike('criteria', "%'name'%")
                }
            }
        }

        String shared = ''

            possibleValuesList.each {
                shared = (currentUserId == it.userId) ? '' : Constants.Commons.SHARED
                filterData << new JSONObject(id: it.id, name: it.name + shared)
            }

        [list : filterData, totalCount : possibleValuesList.totalCount]
    }

    String getAvdFilterCriteriaExcelExport(Long filterId) {
        AdvancedFilter advancedFilter = AdvancedFilter.findById(filterId)
        "Name : ${advancedFilter?.name ?: '-' } , Description : ${advancedFilter?.description ?: '-'}"
    }

    void deleteAdvancedFilter(AdvancedFilter advancedFilter) {
        log.info("deleting advance filter with users ${advancedFilter?.shareWithUsers?.toString()} and groups ${advancedFilter?.shareWithGroups?.toString()}")
        //dont remove this log info as this was used as corrective action to fetch lazyproperties in predelete event in audit
        if (advancedFilter) {
            advancedFilter.delete(failOnError: true, flush: true)
        }
    }

    Map<String, List> getValidInvalidValues(List<Object> values, String fieldName, Long exConfigId, String lang , String alertType) {
        log.debug("Validating for field name: ${fieldName}")
        Set validValues = []
        Set invalidValues = values
        def domain = SingleCaseAlert
        def executedConfigurationDomain = ExecutedConfiguration
        if(alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT_DEMAND){
            domain = SingleOnDemandAlert
        } else if(alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            domain = AggregateCaseAlert
        }else if(alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND) {
            domain = AggregateOnDemandAlert
        }else if( alertType == Constants.AlertConfigType.EVDAS_ALERT_DEMAND) {
            domain = EvdasOnDemandAlert
            executedConfigurationDomain = ExecutedEvdasConfiguration
        } else if( alertType == Constants.AlertConfigType.EVDAS_ALERT){
            domain = EvdasAlert
            executedConfigurationDomain = ExecutedEvdasConfiguration
        }
        if (values && fieldName) {
            if (fieldName == Constants.AdvancedFilter.CASE_NUMBER) {
                values.collate(500).each { list ->
                    if(exConfigId > 0) {
                        validValues += (domain.findAllByCaseNumberInListAndExecutedAlertConfiguration(list, ExecutedConfiguration.get(exConfigId))*.caseNumber)
                    } else{
                        validValues += (domain.findAllByCaseNumberInList(list)*.caseNumber)
                    }
                }
                invalidValues = values - validValues
            } else if(fieldName == Constants.AdvancedFilter.PT_FIELD) {
                values.collate(500).each { list ->
                    if(exConfigId > 0) {
                        validValues += (domain.findAllByPtInListAndExecutedAlertConfiguration(list, executedConfigurationDomain.get(exConfigId))*.pt)
                    } else{
                        validValues += (domain.findAllByPtInList(list)*.pt)
                    }
                }
                invalidValues = values - validValues
            }
        }
        return [validValues: validValues, invalidValues: invalidValues]
    }

    Set<String> getDuplicates(List<String> list) {
        Set<String> lump = new HashSet<String>()
        Set<String> dupl = new HashSet<String>()
        list?.each {
            if (lump.contains(it))
            {
                dupl.add(it)
            }
            else
            {
                lump.add(it)
            }
        }
        return dupl
    }
}
