package com.rxlogix

import com.rxlogix.config.EvdasConfiguration
import com.rxlogix.config.MasterEvdasConfiguration
import com.rxlogix.config.ParameterValue
import com.rxlogix.config.ReportField
import com.rxlogix.dto.SuperQueryDTO
import com.rxlogix.enums.QueryOperatorEnum
import com.rxlogix.enums.QueryTypeEnum
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.SignalQueryHelper
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonSlurper
import grails.util.Holders

import java.text.SimpleDateFormat
import java.time.ZoneId

@Transactional
class EvdasSqlGenerationService {

    def queryService

    private static final String DATETIME_FMT = "dd-MM-yyyy HH:mm:ss"
    private static final String DATE_FMT = "dd-MM-yyyy"

    public String initializeInsertGtts(EvdasConfiguration config, List evdasConfigs=[], MasterEvdasConfiguration masterEvdasConfiguration=null, Map evdasDataMap = [:]) {


        String insertStatement = "Begin execute immediate('delete from gtt_report_input_params'); " +
                "execute immediate('delete from gtt_report_input_fields'); " +
                "execute immediate ('delete from gtt_filter_key_values'); " +
                "execute immediate ('delete from GTT_AGG_MASTER_CHILD_DTLS'); " +
                "commit;"

        Map resultMap = [:]
        String startDate
        String endDate
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FMT)
        if (!config) {
            resultMap = [id: evdasDataMap.id, productSelection: evdasDataMap.productSelection, productGroupSelection: evdasDataMap.productGroupSelection, eventSelection: evdasDataMap.eventSelection, eventGroupSelection: evdasDataMap.eventGroupSelection, name: evdasDataMap.name, query: evdasDataMap.query]
            startDate = sdf.format(Date.from(evdasDataMap.dateRangeStart.timestampValue().toLocalDateTime().atZone(ZoneId.systemDefault()).toInstant()))?.toString()
            endDate = sdf.format(Date.from(evdasDataMap.dateRangeEnd.timestampValue().toLocalDateTime().atZone(ZoneId.systemDefault()).toInstant()))?.toString()?.toString()
        }

        if (config) {
            def startEndDate = config.dateRangeInformation.getReportStartAndEndDate()
            resultMap = [id: config.id, productSelection: config.productSelection, productGroupSelection: config.productGroupSelection, eventSelection: config.eventSelection, eventGroupSelection: config.eventGroupSelection, name: config.name, query: config.query]
            startDate = startEndDate[0]?sdf.format(startEndDate[0]):''?.toString()
            endDate = startEndDate[1]?sdf.format(startEndDate[1]):''?.toString()
        }

        int queryExists = resultMap.query ? 1 : 0

        String inputSeparator = "" // feature not available in UI , comma added by default
        String reportName = resultMap?.name?.replaceAll("(?i)'", "''")



        int productFilterFlag = (resultMap?.productSelection || resultMap?.productGroupSelection) ? 1 : 0

        int eventFilterFlag = (resultMap?.eventSelection || resultMap?.eventGroupSelection) ? 1 : 0

        // Comma separated lists of all filters
        List<Map> productDetails = MiscUtil?.getProductDictionaryValues(resultMap?.productSelection, false)
        Map ingredient = productDetails[0]
        List<Map> eventDetails = MiscUtil?.getEventDictionaryValues(resultMap?.eventSelection)
        Map soc = eventDetails[0]
        Map hlgt = eventDetails[1]
        Map hlt = eventDetails[2]
        Map pt = eventDetails[3]
        Map llt = eventDetails[4]
        Map synonyms = eventDetails[5]
        Map smqsBroad = eventDetails[6]
        Map smqsNarrow = eventDetails[7]

        if(masterEvdasConfiguration) {
            insertStatement += "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('MASTER_EXECUTION_ID','${masterEvdasConfiguration.id}');"
        }
        // report Level parameters
        insertStatement +=" Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('HAS_QUERY','${queryExists}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('INPUT_SEPARATOR','${inputSeparator}');"+  //Akash
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('REPORT_START_DATE','${startDate}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('REPORT_END_DATE','${endDate}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('PRODUCT_FILTER_FLAG','${productFilterFlag}');"+
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('EVENT_FILTER_FLAG','${eventFilterFlag}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('REPORT_NAME','${reportName}');" +
                "Insert into GTT_REPORT_INPUT_PARAMS (param_key , param_value) VALUES ('REPORT_ID','${resultMap.id}');" +
                "INSERT INTO GTT_REPORT_INPUT_PARAMS (PARAM_KEY,  PARAM_VALUE) VALUES ('TENANT_ID','${Holders.config.signal.default.tenant.id}');"

        if(evdasConfigs) {
            // insert child executed config gtts
            //List<ExecutedConfiguration> executedConfigurationList = ExecutedConfiguration.findByMasterExConfigId(masterExecutedConfiguration.id)
            evdasConfigs.each {EvdasConfiguration it->
                List groups = []
                if(it.productGroupSelection) {
                    groups = JSON.parse(it.productGroupSelection)
                }
                String prodName = groups? groups[0].name.substring(0,groups[0].name.lastIndexOf('(') - 1) : it.getNameFieldFromJson(it.productSelection)
                String prodId = groups? groups[0].id :it.getIdFieldFromJson(it.productSelection)
                Integer hierarchyId = 199
                insertStatement += " Insert into GTT_AGG_MASTER_CHILD_DTLS (MASTER_EXECUTION_ID, CHILD_EXECUTION_ID, ALERT_NAME, HIERARCHY_ID, BASE_ID, BASE_NAME) " +
                        "VALUES (${masterEvdasConfiguration.id}, ${it.id},'${it.name}', '${hierarchyId}', '${prodId}', '${prodName}'); "
            }
        }

        if (resultMap.productGroupSelection) {
            JSON.parse(resultMap.productGroupSelection).each {
                it.name = it.name?.replaceAll( '"', '\"' )?.replaceAll( "'", "''" )
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (199,${it.id},'${it.name.substring(0,it.name.lastIndexOf('(') - 1)}'); "
            }
        }

        if(!resultMap.productGroupSelection && productFilterFlag == 1) {// Ids used in product filter
            ingredient.each { k, v ->
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (1,$k,'${v?.replaceAll("(?i)'", "''")}'); "
            }
        }

        if (resultMap.eventGroupSelection) {
            JSON.parse(resultMap.eventGroupSelection).each {
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (600,${it.id},'${it.name.substring(0, it.name.lastIndexOf('(') - 1)}'); "
            }
        }

        if (eventFilterFlag == 1) {// Ids used in event filter

            soc.each {k,v ->
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (8,$k,'${v?.replaceAll("(?i)'", "''")}'); "} // KEY_ID for soc = 8
            hlgt.each {k,v ->
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (9,$k,'${v?.replaceAll("(?i)'", "''")}'); "} // KEY_ID for hlgt = 9
            hlt.each {k,v ->
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (10,$k,'${v?.replaceAll("(?i)'", "''")}'); "} // KEY_ID for hlt = 10
            pt.each {k,v ->
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (11,$k,'${v?.replaceAll("(?i)'", "''")}'); "} // KEY_ID for pt = 11
            llt.each {k,v ->
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (12,$k,'${v?.replaceAll("(?i)'", "''")}'); "} // KEY_ID for llt = 12
            synonyms.each {k,v ->
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (13,$k,'${v?.replaceAll("(?i)'", "''")}'); "} // KEY_ID for synonym = 13
            smqsNarrow.each { k, v ->
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (18,$k,'${v?.replaceAll("(?i)'", "''")}'); "} // KEY_ID for Narrow SMQ = 18
            smqsBroad.each { k, v ->
                insertStatement += " Insert into GTT_FILTER_KEY_VALUES (KEY_ID,CODE,TEXT) VALUES (19,$k,'${v?.replaceAll("(?i)'", "''")}'); "} // KEY_ID for Narrow SMQ = 19
        }

        insertStatement += " END;"

        return insertStatement
    }

    String initializeQuerySql(EvdasConfiguration configuration, def evdasDataMap = null) {

        String queryString = """ BEGIN
                            delete from GTT_QUERY_DETAILS;
                            delete from GTT_QUERY_SETS;
                            delete from GTT_REPORT_VAR_INPUT;
                       """
        Locale locale
        Long queryId
        if (!configuration) {
        locale = new Locale(evdasDataMap.locale as String)
            queryId = evdasDataMap?.query as Long
        }
        if (configuration) {
            locale = configuration.owner.preference.locale
            queryId = configuration.query
        }
        SuperQueryDTO superQuery = queryService.queryDetail(queryId)

        SqlGenIDDTO sqlGenIDDTO = new SqlGenIDDTO()
        queryString = queryString + insertQueriesDataToTempTable(superQuery, sqlGenIDDTO, null, 0, [], [],null, locale, false)

        queryString = queryString + "\n END; \n"
        return queryString
    }

    /**
     * Method to insert the query data to the temp table. Currently it supports only query builder. Support for set builder is not yet there.
     * @param superQueryDTO
     * @param sqlGenIDDTO
     * @param joinOperator
     * @param parent
     * @param blanks
     * @param customSqlBlanks
     * @param poiInputParams
     * @param locale
     * @param nonValidCases
     * @return
     */
    private String insertQueriesDataToTempTable(SuperQueryDTO superQuery, SqlGenIDDTO sqlGenIDDTO, String joinOperator, int parent, List<ParameterValue> blanks, List<ParameterValue> customSqlBlanks, Set<ParameterValue> poiInputParams, Locale locale, boolean nonValidCases) {
        String insertQuery = ""
        if (superQuery?.queryType == QueryTypeEnum.QUERY_BUILDER) {
            insertQuery = insertQuery + buildQueryFromJSONQuery(superQuery.JSONQuery, sqlGenIDDTO, parent, joinOperator, superQuery.hasBlanks, blanks, poiInputParams, locale, nonValidCases)
        }
        insertQuery += ""
        return insertQuery
    }

    String buildQueryFromJSONQuery(String JSONQuery, SqlGenIDDTO sqlGenIDDTO, int parent, String joinOperator, boolean hasBlanks, List blanks, Set<ParameterValue> poiInputParams, Locale locale, boolean nonValidCases, boolean isTemplateQuery = false){
        Map dataMap = (new JsonSlurper()).parseText(JSONQuery)
        Map allMap = dataMap.all
        List containerGroupsList = allMap.containerGroups
        String query = "INSERT INTO GTT_QUERY_SETS (SET_ID,CUSTOM_SQL,SET_OPERATOR,GROUP_ID,QUERY_FLAG,PARENT_GROUP_ID) values ($sqlGenIDDTO.value,null,${joinOperator ? "'${joinOperator.toUpperCase()}'" : null},${parent}, ${nonValidCases ? 1 : (isTemplateQuery ? 2 : null)},${parent}); \n"
        query = query + insertSqlStatementFromQueryBuilderStatement(containerGroupsList, sqlGenIDDTO.value, new SqlGenIDDTO(), 0, 0, null, hasBlanks, blanks,poiInputParams, locale)
        return query
    }

    private String insertSqlStatementFromQueryBuilderStatement(
            def data, Integer setId, SqlGenIDDTO sqlGenIDDTO, int parent, Integer index, String joinOperator, boolean hasBlanks, List<ParameterValue> blanks, Set<ParameterValue> poiInputParams, Locale locale) {
        if (data instanceof Map && data.expressions) {
            sqlGenIDDTO.value = sqlGenIDDTO.value + 1
            String groupInsert = "INSERT INTO GTT_QUERY_DETAILS (SET_ID,FIELD_ID,JAVA_VARIABLE,FIELD_OPERATOR,FIELD_VALUES,GROUP_ID,GROUP_OPERATOR,CUSTOM_INPUT,PARENT_GROUP_ID) values ($setId,null,null,null, null,${sqlGenIDDTO.value},${data.keyword ? ("'${data.keyword.toUpperCase()}'") : null},null,${parent}) ;\n"
            return groupInsert + insertSqlStatementFromQueryBuilderStatement(data.expressions, setId, sqlGenIDDTO, sqlGenIDDTO.value, 0, data.keyword, hasBlanks, blanks,poiInputParams, locale)
        } else {
            if (data instanceof List) {
                String query = ""
                data.eachWithIndex { val, i ->
                    query = query + insertSqlStatementFromQueryBuilderStatement(val, setId, sqlGenIDDTO, parent, i, joinOperator, hasBlanks, blanks,poiInputParams, locale)
                }
                return query
            } else {
                //TODO CUSTOM_INPUT LOGIC
                String customInput = null
                if (hasBlanks && !data.value || data.value.toString().matches(Constants.POI_INPUT_PATTERN_REGEX)) {
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
                    datasheetValues = "'" + data.RLDS.split(";").join("'~!@#@!~'") + "'"
                }
                return "INSERT INTO GTT_QUERY_DETAILS (SET_ID,FIELD_ID,JAVA_VARIABLE,FIELD_OPERATOR,FIELD_VALUES,GROUP_ID,GROUP_OPERATOR,CUSTOM_INPUT,PARENT_GROUP_ID,ADDL_PARAMS) values ($setId,$index,'${data.field}','${data.op?.toUpperCase()}', ${data.value ? "'${data.value}'" : null},${parent},null,${customInput ? "'${customInput}'" : null},${parent},${datasheetValues ? "'${datasheetValues.replaceAll("'", "''")}'" : null}); \n"
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
                String fieldDateFormat = reportField.getDateFormat(locale.toString())
                value = !value.contains(':') && fieldDateFormat.contains(':') ? (value + " 00:00:00") : value
                return DateUtil.parseDate(value, fieldDateFormat)?.format(DATETIME_FMT)
            }
            if (op in [QueryOperatorEnum.EQUALS.name(), QueryOperatorEnum.NOT_EQUAL.name()] && value.indexOf(';') != -1) {
                value = "'" + value.split(";").join("'~!@#@!~'") + "'"
            } else if (reportField.isString()) {
                value = "'" + value + "'"
            }
            return value.replaceAll("'", "''")
        }
        return value
    }

    String getEvdasQuerySql() {
        return SignalQueryHelper.evdas_query_sql()
    }
}
