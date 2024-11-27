package com.rxlogix

import com.rxlogix.config.Keyword
import com.rxlogix.config.ReportField
import com.rxlogix.enums.QueryOperatorEnum
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import groovy.sql.Sql
import org.grails.web.json.JSONObject
import org.springframework.web.multipart.MultipartFile

import javax.sql.DataSource

@Secured(["isAuthenticated()"])
class QueryController {

    def queryService
    def reportIntegrationService
    def reportFieldService
    def userService
    def importService
    def advancedFilterService
    def cacheService
    SqlGenerationService sqlGenerationService
    DataSource dataSource_pva
    DataSource dataSource_faers

    def view(Long id) {
        String url = Holders.config.pvreports.query.view.uri + "/" + id
        redirect(url: url)
    }

    def list() {
        String url = Holders.config.pvreports.query.list.uri
        redirect(url: url)
    }

    def create() {
        String url = Holders.config.pvreports.query.create.uri
        redirect(url: url)
    }

    def load() {
        String url = Holders.config.pvreports.query.load.uri
        redirect(url: url)
    }

    def queryDetails(Long queryId) {
        def detail = queryService.queryDetail(queryId)
        render(detail as JSON)
    }

    def queryExpressionValuesForQuery(Long queryId) {
        Set<Map> result = queryService.queryExpressionValuesForQuery(queryId) ?: []
        render(result as JSON)
    }

    def queryExpressionValuesForQuerySet(Long queryId) {
        Set<List<Map>> result = queryService.queryExpressionValuesForQuerySet(queryId) ?: []
        render(result as JSON)
    }

    def customSQLValuesForQuery() {
        List<Map> result = []
        if (params.queryId) {
            result = queryService.customSQLValuesForQuery(params.long("queryId"))
        }
        render result as JSON
    }

    def getAllFields() {
        render reportFieldService.getAllReportFields() as JSON
    }

    def possibleValues(String lang, String field) {
        if (field != null) {
            render((reportFieldService.getSelectableValuesForFields("en").get(field) ?: []) as JSON)
            return
        }
        render reportFieldService.getSelectableValuesForFields("en") as JSON
    }

    def possiblePaginatedValues(String lang, String field, String term, Integer max, Integer page) {
        def jsonData = []
        max = max ?: 30
        page = page ?: 1
        Integer offset = Math.max(page - 1, 0) * max
        term = term?.trim() ?: ""
        Integer totalCount = 0

        if (field != null) {
           Map results = reportFieldService.getNonCacheSelectableValuesForFields(field, lang, term, max, offset)
            totalCount=results?.totalCount
            results?.result.each {
                // Select2 needs both an id and text. Both fields are used in the UI.
                jsonData << new JSONObject(id: it, text: it)
            }
        }
        render([list:jsonData,totalCount: totalCount] as JSON)
    }

    def extraValues() {
        render reportFieldService.getExtraValuesForFields() as JSON
    }

    def getStringOperators() {
        render operatorsI18n(QueryOperatorEnum.stringOperators) as JSON
    }

    def getBooleanOperators() {
        render operatorsI18n(QueryOperatorEnum.booleanOperators) as JSON
    }

    def getNumOperators() {
        render operatorsI18n(QueryOperatorEnum.numericOperators) as JSON
    }

    def getDateOperators() {
        render operatorsI18n(QueryOperatorEnum.dateOperators) as JSON
    }

    def getValuelessOperators() {
        render operatorsI18n(QueryOperatorEnum.valuelessOperators) as JSON
    }

    def getAllKeywords() {
        render getKeyWords() as JSON
    }

    private def getKeyWords() {
        Keyword.values().collect { Keyword keyword ->
            return [value: keyword.name(), display: message(code: keyword.getI18nKey())]
        }
    }

    private def operatorsI18n(QueryOperatorEnum[] operators) {
        operators.collect { QueryOperatorEnum operator ->
            return [value: operator.name(), display: message(code: operator.getI18nKey())]
        }
    }

    //TODO: Need to check the usage of this action - PS
    /**
     * AJAX call used by autocomplete textfields.
     */
    def ajaxReportFieldSearch() {
        def jsonData = []

        if (params.term?.length() > 1) {
            List results = reportFieldService.retrieveValuesFromDatabaseSingle(ReportField.findByNameAndIsDeleted(params.field, false), params.term, params.lang ?: userService.user?.preference?.locale?.language)

            results.each {
                // Select2 needs both an id and text. Both fields are used in the UI.
                jsonData << new JSONObject(id: it, text: it)
            }
        }

        render jsonData as JSON
    }

    def queryList(String dataSource, String term, int page, int max) {
        Boolean isFaersQuery = false
        Boolean isVigibaseQuery = false
        Boolean isEmbaseQuery = false
        Boolean isSafetyQuery = true

        max = max ?: 30
        page = page ?: 1
        term = term?.trim()

        switch (dataSource) {
            case 'faers':
                isSafetyQuery = false
                isFaersQuery = true
                break
            case 'vigibase':
                isVigibaseQuery = true
                //Currently this features not implemented from pvr side so, just adding code similar as isFaersQuery below please remove after implementation in pvr.
                isFaersQuery = isVigibaseQuery
                isSafetyQuery = false
                break
            case 'embase':
                // Все флаги, кроме isEmbaseQuery, должны быть false
                isFaersQuery = false
                isVigibaseQuery = false
                isEmbaseQuery = true
                isSafetyQuery = false
                break
        }
        if (dataSource.equals('embase')) {
            isSafetyQuery = false
            isEmbaseQuery = true
        }
        Map resp = reportIntegrationService.getQueryList(term, Math.max(page - 1, 0) * max, max, false, isFaersQuery, false, isSafetyQuery, isEmbaseQuery)
        Integer totalCount =resp?resp.totalCount:0
        render([list: resp?.queryList?.unique()?.collect {
            [id: it.id, text: it.name + " " + (it?.description ? "(" + it.description + ")" : Constants.Commons.BLANK_STRING) + (it.owner ? " - Owner: " + it.owner : Constants.Commons.BLANK_STRING), name: it.name]
        }, totalCount: totalCount] as JSON)
    }

    def evdasQueryList(String term, int page, int max) {
        max = max ?: 30
        page = page ?: 1
        term = term?.trim()
        //Uncomment below line once EVDAS flag is added in the PVR - PS
        Map resp = reportIntegrationService.getQueryList(term, Math.max(page - 1, 0) * max, max, true, false, true, false, false)
        Integer totalCount = resp.totalCount

        render([list  : resp?.queryList?.unique()?.collect {
            [id: it.id, text: it.name + " " + (it?.description ? "(" + it.description + ")" : Constants.Commons.BLANK_STRING) + (it.owner ? " - Owner: " + it.owner : Constants.Commons.BLANK_STRING), name: it.name]
        }, totalCount: totalCount] as JSON)
    }

    def queryIdNameList() {
        Map resp = [:]
        if(params.queryIdList && params.list("queryIdList")) {
            List<String> queryIdList = params.list("queryIdList")
            resp = reportIntegrationService.getQueryNameIdList(queryIdList)
        }else{
            resp.queryIdNameList = []
        }
        render([queryIdNameList: resp.queryIdNameList] as JSON)
    }

    def reportFieldsForQueryValue() {
        // get name from cache where it was stored from job
        // change displayText , fetch it by cache, not massageSource
        ReportField field = ReportField.findByName(params.name)
        def displayText = cacheService.getRptToUiLabelInfoPvrEn(field?.name)
        render([name       : field?.name, dictionary: field?.dictionaryType?.toString() ?: '', level: field?.dictionaryLevel ?: '',validatable: reportFieldService.isImportValidatable(field), isAutocomplete: field?.isAutocomplete, dataType: field?.dataType, isText: field?.isText,
                description: message(code: "app.reportField." + field?.name + ".label.description", default: ''), displayText: displayText?:message(code: "app.reportField.${field?.name}"),
                isNonCacheSelectable:field?.nonCacheSelectable] as JSON)
    }

    def validateValue() {
        Map map = [uploadedValues: "", message: "", success: false]
        String selectedField = params.selectedField
        if(selectedField.contains(";")){
            selectedField=selectedField.split(";")[0]
        }
        String qevId = params.qevId
        List<String> list = params.values.split(";").collect { it.trim() }.findAll { it }
        if (list) {
            Map<String, List> validationResult = importService.getValidInvalidValues(reportFieldService,list, selectedField, userService.user?.preference?.locale?.toString())
            String template = g.render(template: '/advancedFilters/includes/importValueModal', model: [qevId: qevId, validValues: validationResult.validValues, invalidValues: validationResult.invalidValues, duplicateValues: advancedFilterService.getDuplicates(list)])
            map.uploadedValues = template
            map.success = true
        }
        render map as JSON
    }

    def importExcel() {
        Map map = [uploadedValues: "", message: "", success: false]
        MultipartFile file = request.getFile('file')
        List list = importService.readFromExcel(file)
        if (list) {
            map.uploadedValues = list.join(';')
            map.success = true
        } else {
            map.message = "${message(code: 'app.label.no.data.excel.error')}"
        }
        render map as JSON
    }

    def getDmvData(String term, int page, int max) {
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }
        int offset = Math.max(page - 1, 0) * max
        def selectorMap=[]
        List finalList = []
        Integer totalCount = 0
        Sql sql = null
        try {
            def dmvInfoMap = cacheService.getMiningVariables(params.dataSource).findAll { it.getValue()["use_case"] == params.useCase }
            String column_name = dmvInfoMap ? dmvInfoMap.values()['column_name'][0] : null
            String table_name = dmvInfoMap ? dmvInfoMap.values()['table_name'][0] : null
            String query = sqlGenerationService.dmvPossibleValueQuery(table_name, column_name)
            String countQuery = sqlGenerationService.dmvTotalCountQuery(table_name, column_name)
            if (params.dataSource == "pva") {
                sql = new Sql(dataSource_pva)
            } else {
                sql = new Sql(dataSource_faers)
            }
            finalList = sql.rows(query, [max: max, offset: offset, TENANT_ID: Holders.config.signal.default.tenant.id, SEARCH_TERM: (term ? "%${term}%" : '%').toUpperCase()]).collect {
                it[0]
            }
            totalCount = sql.rows(countQuery, [TENANT_ID: Holders.config.signal.default.tenant.id, SEARCH_TERM: (term ? "%${term}%" : '%').toUpperCase()])?.get(0)?.get('COUNT')
        } catch (Exception ex) {
            ex.printStackTrace()
        } finally {
            sql?.close()
        }
        finalList.each {
            selectorMap.add(["id": it, "text": it])
        }

        render([list: selectorMap, totalCount: totalCount as Integer] as JSON)
    }

}
