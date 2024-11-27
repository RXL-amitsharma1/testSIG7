package com.rxlogix.controllers

import com.rxlogix.Constants
import com.rxlogix.config.*
import com.rxlogix.dto.AlertDataDTO
import com.rxlogix.dto.AlertReviewDTO
import com.rxlogix.dto.SpotfireSettingsDTO
import com.rxlogix.enums.DataSourceEnum
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.enums.QueryOperatorEnum
import com.rxlogix.enums.TimeZoneEnum
import com.rxlogix.helper.LinkHelper
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.ArchivedAggregateCaseAlert
import com.rxlogix.signal.ArchivedSingleCaseAlert
import com.rxlogix.signal.GroupedAlertInfo
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.SignalQueryHelper
import grails.converters.JSON
import grails.util.Holders
import groovy.json.JsonSlurper
import org.apache.commons.lang3.StringUtils
import org.apache.http.util.TextUtils
import org.grails.datastore.mapping.query.Query

import static com.rxlogix.Constants.AlertConfigType.AGGREGATE_CASE_ALERT
import static com.rxlogix.Constants.AlertConfigType.SINGLE_CASE_ALERT

trait AlertController implements AlertUtil, LinkHelper {
    def alertService
    def messageSource
    def userService
    def queryService
    def reportIntegrationService
    def spotfireService
    def pvsProductDictionaryService
    def cacheService
    def CRUDService
    def reportExecutorService
    def jaderExecutorService

    def getAlertByExecutedConfigId() {
        def execConfigId = params.id
        alertService.findByExecutedConfiguration(ExecutedConfiguration.findById(execConfigId))
    }

    def setTemplateToAlert(configurationInstance) {
        ReportTemplate template = null

        //Need to set template only when the configuration type.
        if (configurationInstance.type == AGGREGATE_CASE_ALERT) {
            template = ReportTemplate.findByName("cust_agg_temp")
        } else if (configurationInstance.type == SINGLE_CASE_ALERT) {
            template = ReportTemplate.findByName("Case_num")
        }

        if (template) {
            configurationInstance.template = template
        }
    }
    /**
     * includeWHODrugs value is updated in configurationInstance object from product information
     * @param ConfigurationInstance object
     */
    void setWhoDrug(configurationInstance){
        if(configurationInstance.isMasterTemplateAlert)
            return
        JsonSlurper jsonSlurper = new JsonSlurper()
        if (configurationInstance.productSelection) {
            Map jsonMap = jsonSlurper.parseText(configurationInstance.productSelection) as Map
            configurationInstance.includeWHODrugs = jsonMap && jsonMap[Constants.DRUG_RECORD_NUMBER_LEVEL] != []
            if (configurationInstance.includeWHODrugs) {
                configurationInstance.drugRecordNumbers = jsonMap[Constants.DRUG_RECORD_NUMBER_LEVEL].collect { it.drugRecordNumber + " (" + it.name + ")" }.join(", ")
            }
        } else if(configurationInstance.productGroupSelection){
            Map jsonMapPg = jsonSlurper.parseText(configurationInstance.productGroupSelection) as Map
            configurationInstance.includeWHODrugs = jsonMapPg && jsonMapPg[0].includeWHODrugs
        }
    }

    List<String> setSubFreQuencyDates() {
        List<String> startDateAbsoluteArray = params.list("dateRangeStart")
        List<String> endDateAbsoluteArray = params.list("dateRangeEnd")
        if (startDateAbsoluteArray && endDateAbsoluteArray) {
            return [startDateAbsoluteArray[0], endDateAbsoluteArray[0]]
        }
        return ["", ""]
    }

    def bindNewTemplateQueries(Configuration configurationInstance) {
        //bind new Template Queries as appropriate
        for (int i = 0; params.containsKey("templateQueries[" + i + "].id"); i++) {
            boolean isTemplateQuery = isCreateTemplateQuery(i)
            if (isTemplateQuery && params.get("templateQueries[" + i + "].new").equals("true") &&
                    params.get("templateQueries[" + i + "].dynamicFormEntryDeleted").equals("false")) {
                LinkedHashMap bindingMap = getBindingMap(i)
                TemplateQuery templateQueryInstance = new TemplateQuery(bindingMap)
                templateQueryInstance = (TemplateQuery) getUserService().setOwnershipAndModifier(templateQueryInstance)
                templateQueryInstance.dateRangeInformationForTemplateQuery.dateRangeEnum = null

                //Set the back reference on DateRangeInformationForTemplateQuery object to TemplateQuery; binding via bindingMap won't do this
                DateRangeInformation dateRangeInformationForTemplateQuery = templateQueryInstance.dateRangeInformationForTemplateQuery
                setDateRangeInformation(i, dateRangeInformationForTemplateQuery, configurationInstance)
                dateRangeInformationForTemplateQuery.templateQuery = templateQueryInstance
                dateRangeInformationForTemplateQuery.relativeDateRangeValue = params.int("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.relativeDateRangeValue")
                assignParameterValuesToTemplateQuery(configurationInstance, templateQueryInstance, i)
                templateQueryInstance.parameterValueAuditString = templateQueryInstance?.queryValueLists ? templateQueryInstance.queryValueLists*.toLogParameterString()?.join(',') : ""
                configurationInstance.addToTemplateQueries(templateQueryInstance)
            }
        }
    }

    boolean isCreateTemplateQuery(Integer i) {
        return params.("templateQueries[" + i + "].query") || !TextUtils.isEmpty(params.("templateQueries[" + i + "].queryLevel")) || !TextUtils.isEmpty(params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.dateRangeEnum")) || params.("templateQueries[" + i + "].template")

    }

    def bindExistingTemplateQueryEdits(Configuration configurationInstance) {
        configurationInstance?.templateQueries?.eachWithIndex() { templateQuery, i ->
            LinkedHashMap bindingMap = getBindingMap(i)
            templateQuery.properties = bindingMap
            templateQuery = (TemplateQuery) getUserService().setOwnershipAndModifier(templateQuery)
            //Set the back reference on DateRangeInformationForTemplateQuery object to TemplateQuery; binding via bindingMap won't do this
            DateRangeInformation dateRangeInformationForTemplateQuery = templateQuery.dateRangeInformationForTemplateQuery

            setDateRangeInformation(i, dateRangeInformationForTemplateQuery, configurationInstance)
            dateRangeInformationForTemplateQuery.templateQuery = templateQuery
            if (params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.relativeDateRangeValue") && params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.relativeDateRangeValue") =~ "-?\\d+") {
                dateRangeInformationForTemplateQuery.relativeDateRangeValue = (params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.relativeDateRangeValue")) as Integer
            }
            assignParameterValuesToTemplateQuery(configurationInstance, templateQuery, i)
            templateQuery.parameterValueAuditString = templateQuery?.queryValueLists ? templateQuery.queryValueLists*.toLogParameterString()?.join(',') : ""
        }
        configurationInstance
    }

    void bindSpotfireSettings(Configuration configuration, Boolean enableSpotfire, String productSelection, String spotfireType, List<DateRangeEnum> dateRangeEnums) {
        if (enableSpotfire && (productSelection || configuration.productGroupSelection || (configuration.adhocRun && configuration.dataMiningVariable))) {
            SpotfireSettingsDTO settings = new SpotfireSettingsDTO()
            settings.type = spotfireType
            settings.rangeType = dateRangeEnums
            if(configuration.type != Constants.AlertConfigType.SINGLE_CASE_ALERT) {
                settings.dataSource = configuration.selectedDatasource.split(",")
            } else {
                settings.dataSource = [Constants.DataSource.PVA]
            }
            configuration.spotfireSettings = settings.toJsonString()
        } else {
            configuration.spotfireSettings = null
        }
    }

    private void setDateRangeInformation(int i, DateRangeInformation dateRangeInformationForTemplateQuery, Configuration configurationInstance) {
        def dateRangeEnum = params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.dateRangeEnum")
        if (!TextUtils.isEmpty(dateRangeEnum)) {
            dateRangeInformationForTemplateQuery?.dateRangeEnum = dateRangeEnum
            if (dateRangeEnum == DateRangeEnum.CUSTOM.name()) {
                dateRangeInformationForTemplateQuery?.dateRangeEnum = dateRangeEnum
                Locale locale = userService.currentUser?.preference?.locale
                dateRangeInformationForTemplateQuery.dateRangeStartAbsolute = DateUtil.getStartDate(params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute"), locale)
                dateRangeInformationForTemplateQuery.dateRangeEndAbsolute = DateUtil.getEndDate(params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute"), locale)
            } else {
                dateRangeInformationForTemplateQuery?.dateRangeStartAbsolute = null
                dateRangeInformationForTemplateQuery?.dateRangeEndAbsolute = null
            }
        } else {
            dateRangeInformationForTemplateQuery.dateRangeEnum = DateRangeEnum.CUMULATIVE
        }
    }

    private void assignParameterValuesToTemplateQuery(Configuration configurationInstance, TemplateQuery templateQuery, int i) {
        templateQuery.queryValueLists?.each {
            List<ParameterValue> parameterValues = it.parameterValues
            it.parameterValues?.clear()
            parameterValues?.each {
                ParameterValue.get(it.id)?.delete()
            }
        }
        templateQuery.templateValueLists?.each {
            List<ParameterValue> parameterValuesList = it.parameterValues
            it.parameterValues?.clear()
            parameterValuesList?.each {
                CustomSQLValue.get(it.id)?.delete() // CustomSQLTemplateValue?
            }
        }
        templateQuery.templateValueLists?.clear()
        templateQuery.queryValueLists?.clear()

        if (params.containsKey("templateQuery" + i + ".qev[0].key")) {
            // for each single query
            int start = 0
            List<String> queryIdList = params.("templateQueries[" + i + "].validQueries").split(",")
            if (queryIdList) {
                Map queryIdNameListMap = [:]
                queryIdNameListMap = getReportIntegrationService().getQueryNameIdList(queryIdList)
                queryIdNameListMap.queryIdNameList.each {
                    QueryValueList queryValueList = new QueryValueList(query: it.id, queryName: it.name)
                    int size = getQueryService().getParameterSize(it.id as Long)

                    // if query set, iterate each query in query set
                    for (int j = start; params.containsKey("templateQuery" + i + ".qev[" + j + "].key") && j < (start + size); j++) {
                        ParameterValue tempValue
                        String key = params.("templateQuery" + i + ".qev[" + j + "].key")
                        String value = params.("templateQuery" + i + ".qev[" + j + "].value")

                        ReportField reportField = ReportField.findByName(params.("templateQuery" + i + ".qev[" + j + "].field"))
                        if (params.containsKey("templateQuery" + i + ".qev[" + j + "].field")) {
                            messageSource= MiscUtil.getBean("messageSource")
                            def operatorString=QueryOperatorEnum.valueOf(params.("templateQuery" + i + ".qev[" + j + "].operator"))
                            tempValue = new QueryExpressionValue(key: key, value: value,
                                    reportField: reportField,
                                    operator: operatorString,
                                    operatorValue:messageSource.getMessage("app.queryOperator.$operatorString", null, Locale.ENGLISH) )
                        } else {
                            tempValue = new CustomSQLValue(key: key, value: value)
                        }
                        queryValueList.addToParameterValues(tempValue)
                    }

                    start += size
                    templateQuery.addToQueryValueLists(queryValueList)
                }
            }
        }

        if (params.containsKey("templateQuery" + i + ".tv[0].key")) {
            TemplateValueList templateValueList = new TemplateValueList(template: params.("templateQueries[" + i + "].template"))
            for (int j = 0; params.containsKey("templateQuery" + i + ".tv[" + j + "].key"); j++) {
                ParameterValue tempValue
                tempValue = new CustomSQLValue(key: params.("templateQuery" + i + ".tv[" + j + "].key"),
                        value: params.("templateQuery" + i + ".tv[" + j + "].value"))
                templateValueList.addToParameterValues(tempValue)
            }
            templateQuery.addToTemplateValueLists(templateValueList)
        }
    }

    def bindAsOfVersionDate(Configuration configuration) {
        if (configuration.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF) {
            configuration.asOfVersionDate = DateUtil.getAsOfVersion(params.asOfVersionDate, "UTC")
        } else {
            configuration.asOfVersionDate = null
        }
    }

    private getBindingMap(int i) {
        def bindingMap = [
                template               : params.("templateQueries[" + i + "].template"),
                templateName           : params.("templateQueries[" + i + "].templateName"),
                query                  : params.("templateQueries[" + i + "].query"),
                queryName              : params.("templateQueries[" + i + "].queryName"),
                operator               : params.("templateQueries[" + i + "].operator"),
                queryLevel             : params.("templateQueries[" + i + "].queryLevel"),
                dynamicFormEntryDeleted: params.("templateQueries[" + i + "].dynamicFormEntryDeleted") ?: false,
                header                 : params.("templateQueries[" + i + "].header") ?: null,
                footer                 : params.("templateQueries[" + i + "].footer") ?: null,
                title                  : params.("templateQueries[" + i + "].title") ?: null,
                headerProductSelection : params.("templateQueries[" + i + "].headerProductSelection") ?: false,
                headerDateRange        : params.("templateQueries[" + i + "].headerDateRange") ?: false,
                blindProtected         : params.("templateQueries[" + i + "].blindProtected") ?: false,
                privacyProtected       : params.("templateQueries[" + i + "].privacyProtected") ?: false
        ]
        bindingMap
    }

    def isPrefferedTerm(configuration) {
        def eventSelection = configuration.eventSelection
        def jsonSlurper = new JsonSlurper()
        def soc, hlgt, hlt, pt, llt, countEvent = 0
        if (eventSelection) {
            def eventSelectionObj = jsonSlurper.parseText(eventSelection)
            eventSelectionObj.each { k, v ->
                if (k == "1") {
                    soc = v
                }
                if (k == "2") {
                    hlgt = v
                }
                if (k == "3") {
                    hlt = v
                }
                if (k == "4") {
                    pt = v
                }
                if (k == "5") {
                    llt = v
                }
            }
        } else {
            return true
        }
        if (!soc && !hlgt && !hlt && pt && !llt) {
            return true
        }
    }

    def listWithFilter(filterMap, domainName) {
        def filteredList = domainName.createCriteria().list {

            filterMap.each { key, value ->
                if (value) {
                    and {
                        eq(key, filterMap[key])
                    }
                }
            }
        }
        filteredList
    }

    private getOpenDispositions(dispositionFilters) {
        def openDispositions = []
        if (dispositionFilters['closed'].toBoolean()) {
            openDispositions = Disposition.findAllByClosed(true)
        }
        if (dispositionFilters['validated'].toBoolean()) {
            openDispositions.addAll(Disposition.findAllByValidatedConfirmed(true))
        }

        if (dispositionFilters['new'].toBoolean()) {
            openDispositions.addAll(Disposition.findAllByValue("New Potential Signal"))
        }
        if (dispositionFilters['underReview'].toBoolean()) {
            openDispositions.addAll(Disposition.findAllByValidatedConfirmedAndClosedAndValueNotEqual(false, false, "New Potential Signal"))
        }
        openDispositions
    }

    private List<Disposition> getDispositionsForName(dispositionFilters) {
        List dispositionList = []
        if (dispositionFilters) {
            dispositionList = Disposition.findAllByDisplayNameInList(dispositionFilters)
        }
        dispositionList
    }

    List getFiltersFromParams(Boolean isFilterRequest, def params) {
        List filters = []
        def escapedFilters = null
        if (params.filters) {
            def slurper = new JsonSlurper()
            escapedFilters = slurper.parseText(params.filters)
        }
        if(escapedFilters) {
            filters = new ArrayList(escapedFilters)
        }

        if (params.dashboardFilter && (params.dashboardFilter == 'total' || params.dashboardFilter == 'new') && !isFilterRequest) {
            filters = Disposition.list().collect { it.displayName }
        } else if (params.dashboardFilter && params.dashboardFilter == 'underReview' && !isFilterRequest) {
            filters = Disposition.findAllByClosedNotEqualAndValidatedConfirmedNotEqual(true, true).collect {
                it.displayName
            }
        } else if (!isFilterRequest) {
            filters = Disposition.findAllByClosedAndReviewCompleted(false , false).collect { it.displayName }
        }
        filters
    }

    void setAlertDateRange(AlertDateRangeInformation alertDateRangeInformation) {
        String timezone = Holders.config.server.timezone
        String dateRangeEnum = params.("alertDateRangeInformation.dateRangeEnum")
        if (dateRangeEnum) {
            alertDateRangeInformation?.dateRangeEnum = dateRangeEnum
            String startDateAbsolute = params.("alertDateRangeInformation.dateRangeStartAbsolute")
            String endDateAbsolute = params.("alertDateRangeInformation.dateRangeEndAbsolute")
            if (dateRangeEnum == DateRangeEnum.CUSTOM.name()) {
                //check for blank values in custom date
                try {
                    alertDateRangeInformation.dateRangeStartAbsolute = DateUtil.stringToDate(startDateAbsolute, 'dd-MMM-yyyy', timezone)
                    alertDateRangeInformation.dateRangeEndAbsolute = DateUtil.stringToDate(endDateAbsolute, 'dd-MMM-yyyy', timezone)
                } catch (Exception e) {
                    alertDateRangeInformation?.dateRangeStartAbsolute = null
                    alertDateRangeInformation?.dateRangeEndAbsolute = null
                }
            } else {
                alertDateRangeInformation?.dateRangeStartAbsolute = null
                alertDateRangeInformation?.dateRangeEndAbsolute = null
            }
        }
    }

    void assignParameterValuesToAlertQuery(Configuration configurationInstance) {
        AlertDateRangeInformation alertDateRangeInformation = configurationInstance.alertDateRangeInformation
        bindData(alertDateRangeInformation, params.alertDateRangeInformation, [exclude: ['dateRangeEndAbsolute', 'dateRangeStartAbsolute']])
        alertDateRangeInformation.alertConfiguration = configurationInstance
        setAlertDateRange(alertDateRangeInformation)
        configurationInstance.alertQueryValueLists?.each {
            List<ParameterValue> parameterValuesList = it.parameterValues
            it.parameterValues?.clear()
            parameterValuesList?.each {
                ParameterValue.get(it.id)?.delete()
            }
        }
        configurationInstance.alertQueryValueLists?.clear()
        configurationInstance.alertQueryId = params.long("alertQuery")
        configurationInstance.alertQueryName = params.alertQueryName
        configurationInstance.alertForegroundQueryValueLists?.clear()
        configurationInstance.alertForegroundQueryId = !configurationInstance.dataMiningVariable? params.long("alertForegroundQuery"):null
        configurationInstance.alertForegroundQueryName = !configurationInstance.dataMiningVariable? params.alertForegroundQueryName:null
        if (params.containsKey("qev[0].key")) {
            // for each single query
            int start = 0
            List<String> queryIdList = params.("validQueries")?.split(",")
            if (queryIdList) {
                List<Map> queryIdNameListMap = []
                queryIdNameListMap = getReportIntegrationService().getQueryNameIdList(queryIdList)?.queryIdNameList
                queryIdNameListMap.each {
                    QueryValueList queryValueList = new QueryValueList(query: it.id, queryName: it.name)
                    int size = getQueryService().getParameterSize(it.id)
                    // if query set, iterate each query in query set
                    for (int j = start; params.containsKey("qev[" + j + "].key") && j < (start + size); j++) {
                        ParameterValue tempValue
                        String key = params.("qev[" + j + "].key")
                        String value = params.("qev[" + j + "].value")
                        String copyPasteValue = params.("qev[" + j + "].copyPasteValue")
                        if(value=="" || value==null){
                            if(params.selectValue!=null && j<params.selectValue.size() && params.selectValue[j]!=''){
                                value=params.selectValue[j]
                            }
                            if(value=="" || value==null){
                                value=copyPasteValue
                            }
                        }
                        ReportField reportField =  ReportField.findByName(params.("qev[" + j + "].field"))
                        if (params.containsKey("qev[" + j + "].field")) {
                            def operatorString=QueryOperatorEnum.valueOf(params.("qev[" + j + "].operator"))
                            messageSource=MiscUtil.getBean("messageSource")
                            tempValue = new QueryExpressionValue(key: key, value: value,
                                    reportField: reportField,
                                    operator:operatorString,
                                    operatorValue:messageSource.getMessage("app.queryOperator.$operatorString", null, Locale.ENGLISH) )
                        } else {
                            tempValue = new CustomSQLValue(key: key, value: value)
                        }
                        queryValueList.addToParameterValues(tempValue)
                    }

                    start += size
                    configurationInstance.addToAlertQueryValueLists(queryValueList)
                }
            }
        }
        if (!configurationInstance.dataMiningVariable) {
            // for each single query
            int start = 0
            List<String> queryIdList = params.("foregroundValidQueries")?.split(",")
            if (queryIdList) {
                List<Map> queryIdNameListMap = []
                queryIdNameListMap = getReportIntegrationService().getQueryNameIdList(queryIdList)?.queryIdNameList
                queryIdNameListMap.each {
                    QueryValueList queryValueList = new QueryValueList(query: it.id, queryName: it.name)
                    int size = getQueryService().getParameterSize(it.id)
                    // if query set, iterate each query in query set
                    for (int j = start;j < (start + size); j++) {
                        ParameterValue tempValue
                        String key = params.("fev[" + j + "].key")
                        if(key==null){
                            key=j
                        }
                        String value = params.("fev[" + j + "].value")
                        String copyPasteValue = params.("fev[" + j + "].copyPasteValue")
                        if (value == "" || value == null) {
                            if (value == "" || value == null) {
                                value = copyPasteValue
                                if (value == "" || value == null) {
                                    String fgData = params.fgData as String
                                    if(StringUtils.isNotBlank(fgData))
                                    {
                                        value = getStringValue(j, fgData)
                                    }

                                }
                            }
                        }
                        ReportField reportField = ReportField.findByName(params.("fev[" + j + "].field"))
                        if (params.containsKey("fev[" + j + "].field")) {
                            def operatorString = QueryOperatorEnum.valueOf(params.("fev[" + j + "].operator"))
                            messageSource = MiscUtil.getBean("messageSource")
                            tempValue = new QueryExpressionValue(key: key, value: value,
                                    reportField: reportField,
                                    operator: operatorString,
                                    operatorValue: messageSource.getMessage("app.queryOperator.$operatorString", null, Locale.ENGLISH))
                        } else {
                            tempValue = new CustomSQLValue(key: key, value: value)
                        }
                        queryValueList.addToParameterValues(tempValue)
                    }

                    start += size
                    configurationInstance.addToAlertForegroundQueryValueLists(queryValueList)
                }
            }
        }
    }

    String getStringValue(int indexNum,String data){
        String tempData=JSON.parse(data)[indexNum].toString().replaceAll("[\\[\\]\"]","")
        int inDex=Integer.parseInt(tempData.split(",")[0]);
        if(inDex==indexNum)
        {
            return tempData.substring(2).replaceAll(",",";");
        }
    }

    Map archivedAlertList(Long id, Map params, Boolean isRestCall) {
        ExecutedConfiguration ec = ExecutedConfiguration.get(id)
        List<Map> archivedAlertList = []
        int offset = params.start as int
        int max = params.length as int
        String searchVal = params.search?: params["search[value]"]
        def orderColumn = params["order[0][column]"]
        String orderDirection = params.order ?: params["order[0][dir]"] ?: Constants.DESCENDING_ORDER
        List<Disposition> dispositions = Disposition.list()
        List<Integer> allDispIds = dispositions.collect { it.id }
        List<Integer> closedIds = dispositions.findAll { it -> it.reviewCompleted == true }.collect { it.id }
        String orderColumnName = params.columns?: params["columns[${orderColumn}][data]"]
        if (isRestCall) {
            searchVal = params.search.value
            Map orderMap = params.order[0]
            orderColumnName = orderMap.name
            orderDirection = orderMap.dir
        }
        int totalCount
        if (ec) {
            if (ec.configId) {
                //Fetching only completed execution status objects for alert. -PS
                List<Integer> reportVersions = ExecutionStatus.findAllByConfigIdAndExecutionStatusInListAndType(ec.configId, [ReportExecutionStatus.COMPLETED, ReportExecutionStatus.DELIVERING], ec.type).reportVersion.collect {
                    (int) it
                }
                List<ExecutedConfiguration> executedConfigurationList = ExecutedConfiguration.createCriteria().list(offset: offset, max: max) {
                    eq("configId", ec.configId)
                    owner {
                        eq("id", ec.owner?.id)
                    }
                    eq("type", ec.type)
                    eq("isLatest", false)
                    eq("adhocRun", ec.adhocRun)
                    eq("isEnabled", true)
                    eq("isDeleted", false)
                    if(searchVal) {
                        'or' {
                            ilike("name", "%${searchVal}%")
                            ilike("description", "%${searchVal}%")
                            ilike("productSelection", "%${searchVal}%")
                            ilike("productGroupSelection", "%${searchVal}%")
                        }
                    }
                    //This check is used to fetch only completed Executed Configurations
                    if (reportVersions.size() > 0) {
                        or {
                            reportVersions.collate(999).each {
                                'in'('numOfExecutions', it)
                            }
                        }
                    }
                    if (orderColumnName == "alertName") {
                        sqlRestriction("1=1 ORDER BY name ${orderDirection}")
                    } else if (orderColumnName == "description") {
                        sqlRestriction("1=1 ORDER BY description ${orderDirection}")
                    } else if (orderColumnName == "caseCount") {
                        List<String> orderByListCaseCount = []
                        allDispIds.each {
                            orderByListCaseCount <<  "COALESCE((disp_counts::jsonb)->>'${it}', '0')::integer"
                        }
                        String orderByStr = orderByListCaseCount.join("+")
                        sqlRestriction(" 1=1 ORDER BY ${orderByStr} ${orderDirection == 'asc' ? Query.Order.Direction.ASC : Query.Order.Direction.DESC}")
                    } else if (orderColumnName == "reviewedCases") {
                        List<String> orderByListReviewedPEC = []
                        closedIds.each {
                            orderByListReviewedPEC << "COALESCE((disp_counts::jsonb)->>'${it}', '0')::integer"
                        }
                        String orderByStr = orderByListReviewedPEC.join("+")
                        sqlRestriction(" 1=1 ORDER BY ${orderByStr} ${orderDirection == 'asc' ? Query.Order.Direction.ASC : Query.Order.Direction.DESC}")
                    } else if (orderColumnName == "lastModified") {
                        order("lastUpdated", "${orderDirection}")
                    } else {
                        order("id", "${orderDirection}")
                    }
                }
                // Fetching total configuration list to show correct version
                List<ExecutedConfiguration> totalExecutedConfigurationList = ExecutedConfiguration.createCriteria().list() {
                    eq("configId", ec.configId)
                    owner {
                        eq("id", ec.owner?.id)
                    }
                    eq("type", ec.type)
                    eq("isLatest", false)
                    eq("adhocRun", ec.adhocRun)
                    eq("isEnabled", true)
                    eq("isDeleted", false)
                    //This check is used to fetch only completed Executed Configurations
                    if (reportVersions.size() > 0) {
                        or {
                            reportVersions.collate(999).each {
                                'in'('numOfExecutions', it)
                            }
                        }
                    }
                    order("id", "asc")
                }
                totalCount = ExecutedConfiguration.createCriteria().count() {
                    eq("configId", ec.configId)
                    owner {
                        eq("id", ec.owner?.id)
                    }
                    eq("type", ec.type)
                    eq("isLatest", false)
                    eq("adhocRun", ec.adhocRun)
                    eq("isEnabled", true)
                    eq("isDeleted", false)
                    'or' {
                        ilike("name", "%${searchVal}%")
                        ilike("description", "%${searchVal}%")
                        ilike("productSelection", "%${searchVal}%")
                        ilike("productGroupSelection", "%${searchVal}%")
                    }
                    //This check is used to fetch only completed Executed Configurations
                    if (reportVersions.size() > 0) {
                        or {
                            reportVersions.collate(999).each {
                                'in'('numOfExecutions', it)
                            }
                        }
                    }
                }

                archivedAlertList = executedConfigurationList.collect {
                    archivedAlertMap(it, totalExecutedConfigurationList.indexOf(it) + 1)
                }
            }
        }
        [aaData: archivedAlertList, recordsFiltered: totalCount, recordsTotal: totalCount]

    }

    Map literatureArchivedAlertList(Long id, Map params) {
        ExecutedLiteratureConfiguration ec = ExecutedLiteratureConfiguration.get(id)
        int offset = params.start as int
        int max = params.length as int
        def orderColumn = params["order[0][column]"]
        def orderColumnMap = [name: params["columns[${orderColumn}][data]"], dir: params["order[0][dir]"]]
        String orderDirection = orderColumnMap?.dir ?: Constants.DESCENDING_ORDER
        List<Map> archivedAlertList = []
        def totalCount =0
        if (ec) {
            if (ec.configId) {
                //Fetching only completed executed objects for alert. -PS
                List<Integer> reportVersions = ExecutionStatus.findAllByConfigIdAndExecutionStatusInListAndType(ec.configId, [ReportExecutionStatus.COMPLETED, ReportExecutionStatus.DELIVERING], Constants.AlertConfigType.LITERATURE_SEARCH_ALERT ).reportVersion.collect {
                    (int) it
                }
                List<ExecutedLiteratureConfiguration> executedConfigurationList = ExecutedLiteratureConfiguration.createCriteria().list(offset: offset, max:max) {
                    eq('configId', ec.configId)
                    eq("isLatest", false)
                    eq("isDeleted", false)
                    if (reportVersions.size() > 0) {
                        or {
                            reportVersions.collate(999).each {
                                'in'('numOfExecutions', it)
                            }
                        }
                    }
                    order("id","${orderDirection}")
                }
                List<ExecutedLiteratureConfiguration> totalExecutedConfigurationList = ExecutedLiteratureConfiguration.createCriteria().list() {
                    eq('configId', ec.configId)
                    eq("isLatest", false)
                    eq("isDeleted", false)
                    if (reportVersions.size() > 0) {
                        or {
                            reportVersions.collate(999).each {
                                'in'('numOfExecutions', it)
                            }
                        }
                    }
                    order("id","asc")
                }
                totalCount = ExecutedLiteratureConfiguration.createCriteria().list() {
                    projections {
                        rowCount()
                    }

                    eq('configId', ec.configId)
                    eq("isLatest", false)
                    eq("isDeleted", false)
                    if (reportVersions.size() > 0) {
                        or {
                            reportVersions.collate(999).each {
                                'in'('numOfExecutions', it)
                            }
                        }
                    }
                }

                archivedAlertList = executedConfigurationList.collect { it -> literatureArchivedAlertMap(it, totalExecutedConfigurationList.indexOf(it)+1) }
            }
        }
        [aaData:  archivedAlertList, recordsFiltered: totalCount, recordsTotal: totalCount]
    }

    Map literatureArchivedAlertMap(ExecutedLiteratureConfiguration ec, Integer startingVersion) {
        String timezone = getUserService().getUser().preference.timeZone
        [exConfigId        : ec.id,
         selectedDatasource: ec?.selectedDatasource?.equalsIgnoreCase(Constants.DataSource.PUB_MED) ? Constants.DataSource.PUB_MED : Constants.LiteratureFields.EMBASE,
         alertName         : ec.name,
         version           : startingVersion,
         dateRange         : DateUtil.toDateString(ec.dateRangeInformation.dateRangeStartAbsolute) +
                 " - " + DateUtil.toDateString(ec.dateRangeInformation.dateRangeEndAbsolute),
         lastModified      : DateUtil.toDateString(ec.lastUpdated, timezone)
        ]
    }


    Map archivedAlertMap(ExecutedConfiguration ec, Integer startingVersion) {
        List<Disposition> reviewedList = Disposition.findAllByValidatedConfirmedOrClosed(true, true)
        String timezone = getUserService().getUser().preference.timeZone
        int caseCount = ec.type == Constants.AlertConfigType.SINGLE_CASE_ALERT ? ArchivedSingleCaseAlert.countByExecutedAlertConfiguration(ec) : ArchivedAggregateCaseAlert.countByExecutedAlertConfiguration(ec)
        int reviewedCases = ec.type == Constants.AlertConfigType.SINGLE_CASE_ALERT ? ArchivedSingleCaseAlert.countByExecutedAlertConfigurationAndDispositionInList(ec, reviewedList) : ArchivedAggregateCaseAlert.countByExecutedAlertConfigurationAndDispositionInList(ec, reviewedList)
        String aggDataUrl = Holders.config.signal.angular.app.base.url + Holders.config.signal.angular.app.alert.details
        String alertNameUrl = createCustomHref(aggDataUrl, [callingScreen: "review",configId: ec.id, archived: true,version:startingVersion])
        [exConfigId        : ec.id,
         selectedDatasource: ec.selectedDatasource,
         alertName         : ec.name,
         alertNameUrl      : alertNameUrl,
         version           : startingVersion,
         product           : getAlertService().productSelectionValue(ec , true),
         description       : ec.description,
         caseCount         : caseCount,
         reviewedCases     : reviewedCases,
         dateRange         : DateUtil.toDateString(ec.executedAlertDateRangeInformation.dateRangeStartAbsolute) +
                 " - " + DateUtil.toDateString(ec.executedAlertDateRangeInformation.dateRangeEndAbsolute),
         lastModified      : DateUtil.toDateString(ec.lastUpdated, timezone)
        ]
    }

    AlertReviewDTO createAlertReviewDTO(String alertType) {
        User currentUser = getUserService().getUserFromCacheByUsername(getUserService().getCurrentUserName())
        Long workflowGroupId = currentUser.workflowGroup.id
        AlertReviewDTO alertReviewDTO = new AlertReviewDTO()
        alertReviewDTO.workflowGrpId = workflowGroupId
        alertReviewDTO.alertType = alertType
        alertReviewDTO.adhocRun = params.adhocRun.toBoolean()
        alertReviewDTO.max = params.int("length")
        alertReviewDTO.offset = params.int("start")
        alertReviewDTO.searchValue = params["search[value]"]
        alertReviewDTO.filterWithUsersAndGroups = (params["selectedAlertsFilter"] == "null") ? [] : params["selectedAlertsFilter"]?.substring(1,params["selectedAlertsFilter"].length()-1).replaceAll("\"", "").split(",");
        def orderColumn = params["order[0][column]"]
        Map orderColumnMap = [name: params["columns[${orderColumn}][data]"], dir: params["order[0][dir]"]]
        alertReviewDTO.orderProperty = orderColumnMap.name ?: "id"

        if (alertReviewDTO.orderProperty.equals("lastExecuted")) {
            alertReviewDTO.orderProperty = "dateCreated"
        } else if (alertReviewDTO.orderProperty.equals("lastModified")) {
            alertReviewDTO.orderProperty = "lastUpdated"
        }
        String groupIds = currentUser.groups.findAll{it.groupType != GroupType.WORKFLOW_GROUP}.collect { it.id }.join(",")
        alertReviewDTO.groupIds = groupIds
        alertReviewDTO.direction = orderColumnMap.dir ?: "desc"
        alertReviewDTO
    }

    Closure executedConfigForAggTypeAlert = { AlertReviewDTO alertReviewDTO ->
        eq("type", alertReviewDTO.alertType)
        eq("isLatest", true)
        eq("isDeleted", false)
        eq("adhocRun", alertReviewDTO.adhocRun)
        eq("isEnabled", true)
        if (alertReviewDTO.alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            sqlRestriction("""id IN 
           (${
                SignalQueryHelper.user_exconfiguration_sql(getUserService().getCurrentUserId(), alertReviewDTO.workflowGrpId,
                        alertReviewDTO.groupIds, alertReviewDTO.alertType, alertReviewDTO.filterWithUsersAndGroups)}
           )""")
        } else {
            sqlRestriction("""CONFIG_ID IN 
           (${
                SignalQueryHelper.user_configuration_sql(getUserService().getCurrentUserId(), alertReviewDTO.workflowGrpId,
                        alertReviewDTO.groupIds, alertReviewDTO.alertType, alertReviewDTO.filterWithUsersAndGroups)}
           )""")
        }
    }

    Closure executedConfigForSingleCaseTypeAlert = { AlertReviewDTO alertReviewDTO ->
        eq("isLatest", true)
        eq("type", alertReviewDTO.alertType)
        eq("adhocRun", alertReviewDTO.adhocRun)
        eq("isCaseSeries", false)
        eq("isDeleted", false)
        eq("isEnabled", true)
        sqlRestriction("""CONFIG_ID IN 
           (${SignalQueryHelper.user_configuration_sql(getUserService().getCurrentUserId(), alertReviewDTO.workflowGrpId,
                                                       alertReviewDTO.groupIds, alertReviewDTO.alertType, alertReviewDTO.filterWithUsersAndGroups)}
           )""")
        isNotNull('pvrCaseSeriesId')
    }
    Map generateResultMap(Map resultMap, def domain) {
        def startTime = System.currentTimeSeconds()
        List list = []
        AlertReviewDTO alertReviewDTO = createAlertReviewDTO(Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
        Map configList = getAlertService().generateAlertReviewMap(alertReviewDTO, executedConfigForAggTypeAlert)

        String timeZone = getUserService().getCurrentUserPreference()?.timeZone
        boolean isOnDemandAlert = domain != AggregateCaseAlert

        if (configList?.configurationsList?.size() > 0) {
            List<String> closedDispositionList = Disposition.findAllByReviewCompleted(true).collect {it.id as String}
            List<Map> totalCountList = []
            List<Long> autoSharedWithUserIds = getAlertService().fetchAutoSharedIds(configList.configurationsList*.id)
            List dateRangeInfoMapList = getAlertService().fetchDateRangeInformationMapListId(configList.configurationsList*.executedAlertDateRangeInformationId)
            if (isOnDemandAlert) {
                totalCountList = getAlertService().getTotalCountList(domain, configList.configurationsList*.id)
            }
            String drFaers = ''
            String drVaers = ''
            String drVigibase = ''
            String drJader = ''

            try {
                if (Holders.config.signal.faers.enabled) {
                    drFaers = (getReportExecutorService().getFaersDateRange().faersDate)?.substring(13)
                }
            } catch (Throwable th) {
                log.error("Fetching Date Range failed on FAERS DB.")
                th.printStackTrace()
            }

            try {
                if (Holders.config.signal.vaers.enabled) {
                    //Updated Vaers Date Range for Integrated Alert PVS-67898
                    drVaers = (getReportExecutorService().getVaersDateRange().vaersDate)?.substring(Constants.Commons.VAERS_END_DATE_INDEX)
                }
            } catch (Throwable th) {
                log.error("Fetching Date Range failed on VAERS DB.")
                th.printStackTrace()
            }

            try {
                if (Holders.config.signal.vigibase.enabled) {
                    drVigibase = (getReportExecutorService().getVigibaseDateRange().vigibaseDate)?.substring(13)
                }
            } catch (Throwable th) {
                log.error("Fetching Date Range failed on VIGIBASE DB.")
                th.printStackTrace()
            }
            try {
                if (Holders.config.signal.jader.enabled) {
                    drJader = (getJaderExecutorService().getJaderDateRange().jaderDate)?.substring(13)
                }
            } catch (Throwable th) {
                log.error("Fetching Date Range failed on JADER DB.")
                th.printStackTrace()
            }

            configList.configurationsList?.each { ExecutedConfiguration executedConfiguration ->

                try {
                    String productSelection = ""
                    Map dateRangeInfoMap=dateRangeInfoMapList.find{it.id==executedConfiguration.executedAlertDateRangeInformationId}
                    String endDate = getEndDateInCaseOfCumulative(executedConfiguration.selectedDatasource, executedConfiguration.executedAlertDateRangeInformation.dateRangeEnum, drFaers, drVaers, drVigibase, drJader)
                    String dr = getDateRangeFromExecutedConfiguration(dateRangeInfoMap, endDate)
                    Integer totalCount = totalCountList.find { it.id == executedConfiguration.id }?.cnt ?: 0
                    Map spotfireName = getSpotfireService().getSpotfireNames(executedConfiguration)
                    if (Holders.config.custom.qualitative.fields.enabled) {
                        productSelection = (getPvsProductDictionaryService().isLevelGreaterThanProductLevel(executedConfiguration) ? getCacheService().getUpperHierarchyProductDictionaryCache(executedConfiguration.id) : getNameFieldFromJson(executedConfiguration.productSelection)) ?: getGroupNameFieldFromJson(executedConfiguration.productGroupSelection)
                    } else {
                        productSelection = getNameFieldFromJson(executedConfiguration.productSelection) ?: getGroupNameFieldFromJson(executedConfiguration.productGroupSelection)
                        String drnSelection = getAllDrugRecNumberFromJson(executedConfiguration.productSelection)
                        productSelection += drnSelection ? (', ' + drnSelection) : ""
                    }
                    String product
                    if(isOnDemandAlert && executedConfiguration.dataMiningVariable){
                        String miningVariable = executedConfiguration.dataMiningVariable
                        if(productSelection){
                            product = miningVariable + "(" + productSelection + ")"
                        } else{
                            product = miningVariable
                        }
                    } else {
                        product = productSelection
                    }
                    Boolean readOnly =false
                    User currentUser = getUserService().getUser()
                    if(executedConfiguration.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT){
                        List readOnlyUsers = getAlertService().getReadOnlyUsersForEx(executedConfiguration)
                        List readOnlyGroups = getAlertService().getReadOnlyGroupsForEx(executedConfiguration)
                        if (readOnlyUsers.find { it.id == currentUser.id && it.readOnly } != null) {
                            readOnly = true
                        } else if (readOnlyGroups.find { group -> currentUser.groups.any { it.id == group.id } && group.readOnly} != null) {
                            readOnly = true
                        }
                    }
                    Map va = [
                            id                   : executedConfiguration.id,
                            name                 : executedConfiguration.name,
                            version              : executedConfiguration.numOfExecutions,
                            description          : executedConfiguration.description,
                            frequency            : getRecurrencePattern(executedConfiguration.scheduleDateJSON),
                            productSelection     : product,
                            pecCount             : !isOnDemandAlert ? getTotalCountsForExecConfig(executedConfiguration.dispCounts) : totalCount,
                            closedPecCount       : getDispCountsForExecConfig(executedConfiguration.dispCounts, closedDispositionList),
                            alertPriority        : getCacheService().getPriorityByValue(executedConfiguration.priorityId)?.displayName,
                            dateRagne            : dr,
                            type                 : Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
                            lastExecuted         : DateUtil.stringFromDate(executedConfiguration.dateCreated, DateUtil.DATEPICKER_FORMAT, timeZone),
                            lastModified         : DateUtil.stringFromDate(executedConfiguration.lastUpdated, DateUtil.DATEPICKER_FORMAT, timeZone),
                            sporfireCumulativeUrl: spotfireName.get(DateRangeEnum.CUMULATIVE),
                            sporfireRangeUrl     : spotfireName.get(DateRangeEnum.PR_DATE_RANGE),
                            IsShareWithAccess    : (getUserService().hasAccessShareWith() && !readOnly) || currentUser.id == executedConfiguration.owner.id || currentUser.isAdmin(),
                            dataSource           : getDataSource(executedConfiguration.selectedDatasource) ,
                            isAutoSharedWith     : autoSharedWithUserIds.any {it == executedConfiguration.id},
                            isAlertBursting      : false
                    ]
                        list.add(va)
                } catch (Throwable th) {
                    log.error(" Some error occured with executedConfiguration ---> " + executedConfiguration.name)
                    th.printStackTrace()
                }
            }
            resultMap = [aaData: list as Set, recordsTotal: configList.totalCount, recordsFiltered: configList.filteredCount]
        }
        def endTime = System.currentTimeSeconds() - startTime
        log.info("It took ${endTime}s to fetch all Aggregate review list")
        resultMap
    }

    Map generateResultMapForAlertBursting(Map resultMap, def domain) {
        def startTime = System.currentTimeSeconds()
        List list = []
        AlertReviewDTO alertReviewDTO = createAlertReviewDTO(Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
        Map configList
        if(Holders.grailsApplication.config.ifFDAorEMA){
            configList = getAlertService().generateAlertReviewMapForAlertBursting(alertReviewDTO, executedConfigForAlertBurstingAggTypeAlert)
        } else {
            configList = getAlertService().generateAlertReviewMap(alertReviewDTO, executedConfigForAggTypeAlert)
        }

        String timeZone = getUserService().getCurrentUserPreference()?.timeZone
        boolean isOnDemandAlert = domain != AggregateCaseAlert

        if (configList?.configurationsList?.size() > 0) {
            List<String> closedDispositionList = Disposition.findAllByReviewCompleted(true).collect {it.id as String}
            List<Map> totalCountList = []
            List<Long> autoSharedWithUserIds = getAlertService().fetchAutoSharedIds(configList.configurationsList*.id)
            List abcLinkedExConfig = []
            configList.configurationsList.each{
                abcLinkedExConfig.add(it.executedConfigurations[0])
            }
            List dateRangeInfoMapList = getAlertService().fetchDateRangeInformationMapListId(abcLinkedExConfig*.executedAlertDateRangeInformationId)

            configList.configurationsList?.each { def abc ->

                try {
                    ExecutedConfiguration executedConfiguration = abc.executedConfigurations[0]
                    String productSelection = ""
                    Map dateRangeInfoMap=dateRangeInfoMapList.find{it.id==executedConfiguration.executedAlertDateRangeInformationId}
                    String endDate = getEndDateInCaseOfCumulative(executedConfiguration.selectedDatasource, executedConfiguration.executedAlertDateRangeInformation.dateRangeEnum)
                    String dr = getDateRangeFromExecutedConfiguration(dateRangeInfoMap, endDate)
                    Integer totalCount = totalCountList.find { it.id == abc.id }?.cnt ?: 0
                    Map spotfireName = getSpotfireService().getSpotfireNames(executedConfiguration)
                    productSelection = getNameFieldFromJson(abc.productSelection) ?: getGroupNameFieldFromJson(abc.productGroupSelection)
                    String product = productSelection
                    Map va = [
                            id                   : abc.id,
                            name                 : abc.name,
                            version              : abc.numOfExecutions,
                            description          : executedConfiguration.description,
                            frequency            : getRecurrencePattern(abc.scheduleDateJSON),
                            productSelection     : product,
                            pecCount             : !isOnDemandAlert ? getTotalCountsForExecConfig(executedConfiguration.dispCounts) : totalCount,
                            closedPecCount       : getDispCountsForExecConfig(abc.dispCounts, closedDispositionList),
                            alertPriority        : getCacheService().getPriorityByValue(abc.priorityId)?.displayName,
                            dateRagne            : dr,
                            type                 : Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
                            lastExecuted         : DateUtil.stringFromDate(executedConfiguration.dateCreated, DateUtil.DATEPICKER_FORMAT, timeZone),
                            lastModified         : DateUtil.stringFromDate(executedConfiguration.lastUpdated, DateUtil.DATEPICKER_FORMAT, timeZone),
                            IsShareWithAccess    : getUserService().hasAccessShareWith(),
                            dataSource           : getDataSource(executedConfiguration.selectedDatasource) ,
                            isAutoSharedWith     : false,
                            isAlertBursting      : true
                    ]
                    list.add(va)
                } catch (Throwable th) {
                    log.error(" Some error occured with executedConfiguration ---> " + executedConfiguration.name)
                    th.printStackTrace()
                }
            }
            resultMap = [aaData: list as Set, recordsTotal: configList.totalCount, recordsFiltered: configList.filteredCount]
        }
        def endTime = System.currentTimeSeconds() - startTime
        log.info("It took ${endTime}s to fetch all Aggregate review list")
        resultMap
    }


    Map generateResultMapAgg(Map resultMap, def domain) {
        def startTime = System.currentTimeSeconds()
        List list = []
        AlertReviewDTO alertReviewDTO = createAlertReviewDTO(Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
        Map finalConfigurationMap = getAlertService().generateAlertReveiwMapAgg(alertReviewDTO)

        String timeZone = getUserService().getCurrentUserPreference()?.timeZone
        println finalConfigurationMap.resultList
        if (finalConfigurationMap?.resultList?.size() > 0) {
            List<String> closedDispositionList = Disposition.findAllByReviewCompleted(true).collect {it.id as String}
            List nonGroupedEntries = finalConfigurationMap.resultList.findAll { it.grouped_entry == false }
            List groupedEntries = finalConfigurationMap.resultList - nonGroupedEntries
            println nonGroupedEntries
            println groupedEntries
            List<Long> autoSharedWithUserIds = getAlertService().fetchAutoSharedIds(nonGroupedEntries*.id)
            println autoSharedWithUserIds
            List dateRangeInfoMapList = getAlertService().fetchDateRangeInformationMapListId(finalConfigurationMap.resultList*.ex_alert_date_range_id)
            String drFaers = ''
            String drVaers = ''
            String drVigibase = ''
            String drJader = ''

            try {
                if (Holders.config.signal.faers.enabled) {
                    drFaers = (getReportExecutorService().getFaersDateRange().faersDate)?.substring(13)
                }
            } catch (Throwable th) {
                log.error("Fetching Date Range failed on FAERS DB.")
                th.printStackTrace()
            }

            try {
                if (Holders.config.signal.vaers.enabled) {
                    //Updated Vaers Date Range for Integrated Alert PVS-67898
                    drVaers = (getReportExecutorService().getVaersDateRange().vaersDate)?.substring(Constants.Commons.VAERS_END_DATE_INDEX)
                }
            } catch (Throwable th) {
                log.error("Fetching Date Range failed on VAERS DB.")
                th.printStackTrace()
            }

            try {
                if (Holders.config.signal.vigibase.enabled) {
                    drVigibase = (getReportExecutorService().getVigibaseDateRange().vigibaseDate)?.substring(13)
                }
            } catch (Throwable th) {
                log.error("Fetching Date Range failed on VIGIBASE DB.")
                th.printStackTrace()
            }
            try {
                if (Holders.config.signal.jader.enabled) {
                    drJader = (getJaderExecutorService().getJaderDateRange().jaderDate)?.substring(13)
                }
            } catch (Throwable th) {
                log.error("Fetching Date Range failed on JADER DB.")
                th.printStackTrace()
            }

            finalConfigurationMap.resultList?.each { execConfig ->

                try {
                    String productSelection = ""
                    GroupedAlertInfo groupedAlertInfo = null
                    if(execConfig.grouped_entry){
                        groupedAlertInfo = GroupedAlertInfo.get(execConfig.id as Long)
                    }
                    Map dateRangeInfoMap=dateRangeInfoMapList.find{it.id==execConfig.ex_alert_date_range_id}
                    String endDate = getEndDateInCaseOfCumulative(execConfig.selected_data_source, ExecutedAlertDateRangeInformation.get(execConfig.ex_alert_date_range_id)?.dateRangeEnum, drFaers, drVaers, drVigibase, drJader)
                    String dr = getDateRangeFromExecutedConfiguration(dateRangeInfoMap, endDate)
                    Integer totalCount = finalConfigurationMap.totalCount ?: 0
                    Map spotfireName = [:]
                    if(groupedAlertInfo == null){
                        spotfireName = getSpotfireService().getSpotfireNames(ExecutedConfiguration.get(execConfig.id as Long))
                    }
                    if (Holders.config.custom.qualitative.fields.enabled) {
                        productSelection = (getPvsProductDictionaryService().isLevelGreaterThanProductLevelAgg(execConfig.product_dictionary_selection) && groupedAlertInfo == null? getCacheService().getUpperHierarchyProductDictionaryCache(execConfig.id) : getNameFieldFromJson(execConfig.product_selection)) ?: getGroupNameFieldFromJson(execConfig.product_group_selection)
                    } else {
                        productSelection = getNameFieldFromJson(execConfig.product_selection) ?: getGroupNameFieldFromJson(execConfig.product_group_selection)
                        String drnSelection = getAllDrugRecNumberFromJson(execConfig.product_selection)
                        productSelection += drnSelection ? (', ' + drnSelection) : ""
                    }
                    Map va = [
                            id                   : execConfig.id,
                            name                 : execConfig.name,
                            version              : execConfig.num_of_execution,
                            description          : execConfig.description,
                            frequency            : getRecurrencePattern(execConfig.schedule_date),
                            productSelection     : productSelection,
                            pecCount             : getTotalCountsForExecConfig(execConfig.disp_count),
                            closedPecCount       : getDispCountsForExecConfig(execConfig.disp_count, closedDispositionList),
                            alertPriority        : getCacheService().getPriorityByValue(execConfig.priority_id as Long)?.displayName,
                            dateRagne            : dr,
                            type                 : Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
                            lastExecuted         : DateUtil.stringFromDate(execConfig.date_created, DateUtil.DATEPICKER_FORMAT, timeZone),
                            lastModified         : DateUtil.stringFromDate(execConfig.last_updated, DateUtil.DATEPICKER_FORMAT, timeZone),
                            sporfireCumulativeUrl: spotfireName.get(DateRangeEnum.CUMULATIVE),
                            sporfireRangeUrl     : spotfireName.get(DateRangeEnum.PR_DATE_RANGE),
                            IsShareWithAccess    : getUserService().hasAccessShareWith(),
                            dataSource           : getDataSource(execConfig.selected_data_source) ,
                            isAutoSharedWith     : autoSharedWithUserIds.any {it == execConfig.id},
                            isAlertBursting      : groupedAlertInfo != null
                    ]
                        list.add(va)
                } catch (Throwable th) {
                    log.error(" Some error occured with executedConfiguration ---> " + execConfig.name)
                    th.printStackTrace()
                }
            }
            resultMap = [aaData: list as Set, recordsTotal: finalConfigurationMap.totalCount, recordsFiltered: finalConfigurationMap.filteredCount]
        }
        def endTime = System.currentTimeSeconds() - startTime
        log.info("It took ${endTime}s to fetch all Aggregate review list")
        println resultMap
        resultMap
    }

    String getEndDateInCaseOfCumulative(String dataSource, DateRangeEnum dateRangeEnum, String drFaers=null, String drVaers=null, String drVigibase=null, String drJader = null){
        String dr = ''
        if(dataSource == Constants.DataSource.FAERS && dateRangeEnum == DateRangeEnum.CUMULATIVE){
            dr = drFaers
        } else if(dataSource == Constants.DataSource.VAERS && dateRangeEnum == DateRangeEnum.CUMULATIVE){
            dr = drVaers
        } else if(dataSource == Constants.DataSource.VIGIBASE && dateRangeEnum == DateRangeEnum.CUMULATIVE){
            dr = drVigibase
        } else if(dataSource == Constants.DataSource.JADER && dateRangeEnum == DateRangeEnum.CUMULATIVE){
            dr = drJader
        }
        dr
    }

    Map generateResultMapForSingleCaseReview(Map resultMap, def domain) {
        def startTime=System.currentTimeSeconds()
        AlertReviewDTO alertReviewDTO = createAlertReviewDTO(Constants.AlertConfigType.SINGLE_CASE_ALERT)
        Map configList = getAlertService().generateAlertReviewMapICR(alertReviewDTO, executedConfigForSingleCaseTypeAlert)
        String timeZone = getUserService().getCurrentUserPreference()?.timeZone
        boolean isOnDemandAlert = domain != SingleCaseAlert
        List list = []
        if (configList?.configurationsList?.size() > 0) {
            List<String> closedDispositionList = Disposition.findAllByReviewCompleted(true).collect {it.id as String}
            List<Map> totalCountList = []
            List<Map> newCountList = []
            List<Map>dateRangeInfoMapList = getAlertService().fetchDateRangeInformationMapListId(configList.configurationsList*.executedAlertDateRangeInformationId)
            List<Long> autoSharedWithUserIds = getAlertService().fetchAutoSharedIds(configList.configurationsList*.id)
            if (isOnDemandAlert) {
                totalCountList = getAlertService().getTotalCountList(domain, configList.configurationsList*.id)
                newCountList = totalCountList
            }
            def safetyDataSourceEnumValue = DataSourceEnum."PVA"?.value()
            boolean isShareWithAccess = getUserService().hasAccessShareWith()
            configList?.configurationsList?.each { Map executedConfiguration ->
                try {
                    Map dateRangeInfoMap=dateRangeInfoMapList.find{it.id==executedConfiguration.executedAlertDateRangeInformationId}
                    String dr = getDateRangeFromExecutedConfiguration(dateRangeInfoMap,"")
                    Integer totalCount = totalCountList.find { it.id == executedConfiguration.id }?.cnt ?: 0
                    Integer totalCountNew = newCountList.find { it.id == executedConfiguration.id }?.cnt ?: 0
                    Map value = [
                            id                   : executedConfiguration.id,
                            name                 : executedConfiguration.name,
                            dataSource           : safetyDataSourceEnumValue?: getDataSource(executedConfiguration.selectedDatasource),
                            version              : executedConfiguration.numOfExecutions,
                            description          : executedConfiguration.description,
                            productSelection     : getProductsList(executedConfiguration),
                            studySelection       : getStudyList(executedConfiguration),
                            singleDateRange      : dr,
                            caseCount            : !isOnDemandAlert ? getTotalCountsForExecConfig(executedConfiguration.dispCounts) : totalCount,
                            closedCaseCount      : getDispCountsForExecConfig(executedConfiguration.dispCounts, closedDispositionList),
                            newCases             : !isOnDemandAlert ? executedConfiguration.newCounts :totalCountNew,
                            alertPriority        : getCacheService().getPriorityByValue(executedConfiguration.priorityId)?.displayName,
                            type                 : Constants.AlertConfigType.SINGLE_CASE_ALERT,
                            lastExecuted         : DateUtil.stringFromDate(executedConfiguration.dateCreated, DateUtil.DATEPICKER_FORMAT, timeZone),
                            lastModified         : DateUtil.stringFromDate(executedConfiguration.lastUpdated, DateUtil.DATEPICKER_FORMAT, timeZone),
                            pvrCaseSeriesId      : executedConfiguration.pvrCaseSeriesId,
                            IsShareWithAccess    : isShareWithAccess,
                            isAutoSharedWith     : autoSharedWithUserIds.any {it == executedConfiguration.id},
                            isAlertBursting      : false
                    ]
                    list.add(value)
                } catch (Throwable th) {
                    log.error(" Some error occured with executedConfiguration ---> " + executedConfiguration.name)
                    th.printStackTrace()
                }
            }
            resultMap = [recordsFiltered: configList.filteredCount, recordsTotal: configList.totalCount, aaData: list]
        }
        def endTime = System.currentTimeSeconds() - startTime
        log.info("It took ${endTime}s to fetch all single case review list")
        resultMap
    }

    private getDateRangeFromExecutedConfiguration(Map dateRangeInfoMap, String endDate) {
    Date startDate=dateRangeInfoMap.dateRangeStartAbsolute
    Date EndDate=dateRangeInfoMap.dateRangeEndAbsolute
        String dr = DateUtil.toDateString1(startDate) + " to " + DateUtil.toDateString1(EndDate)
        if(endDate != ''){
            dr = DateUtil.toDateString1(dateRangeInfoMap.dateRangeStartAbsolute) + " to " + endDate
        }
        dr
    }

    AlertDataDTO createAlertDataDTO(Map filterMap, Map orderColumnMap,
                                            def domain, Boolean isFullCaseList = false) {
        List<String> allowedProductsToUser = []
        if (getAlertService().isProductSecurity()) {
            allowedProductsToUser = getAlertService().fetchAllowedProductsForConfiguration()
        }
        params.isFaers = false
        ExecutedConfiguration executedConfig = ExecutedConfiguration.get(params.id)
        String selectedDataSource = executedConfig?.selectedDatasource ?: Constants.DataSource.PVA
        if (selectedDataSource == Constants.DataSource.FAERS) {
            params.isFaers = true
        }

        String timeZone = getUserService().getCurrentUserPreference()?.timeZone
        AlertDataDTO alertDataDTO = new AlertDataDTO()
        alertDataDTO.params = params
        alertDataDTO.allowedProductsToUser = allowedProductsToUser
        alertDataDTO.domainName = domain
        alertDataDTO.executedConfiguration = executedConfig
        alertDataDTO.execConfigId = executedConfig?.id
        alertDataDTO.configId = executedConfig?.id
        alertDataDTO.filterMap = filterMap
        alertDataDTO.timeZone = timeZone
        alertDataDTO.orderColumnMap = orderColumnMap
        alertDataDTO.userId = getUserService().getUser().id
        alertDataDTO.isFullCaseList = isFullCaseList
        alertDataDTO.length = params.int("length")
        alertDataDTO.start = params.int("start")
        alertDataDTO
    }

    void editShareWith() {
        if (params.sharedWith && params.executedConfigId) {
            ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(Long.parseLong(params.executedConfigId))
            Configuration config = Configuration.findByIdAndIsDeleted(executedConfiguration.configId, false)
            getUserService().bindSharedWithConfiguration(config, params.sharedWith, true)
            getCRUDService().updateWithAuditLog(config)
            if (executedConfiguration?.reportId) {
                getAlertService().updateSharedWithReport(params)
            }
            if(executedConfiguration.pvrCaseSeriesId) {
                getAlertService().updateIsTempCaseSeries(null,false,config,executedConfiguration.pvrCaseSeriesId)
            }
        } else {
            log.info("No valid executed config id")
        }
    }

    void updateTemplateQuerySequence(Configuration configurationInstance) {

        configurationInstance?.templateQueries?.removeAll { !it || it.dynamicFormEntryDeleted }

        configurationInstance?.templateQueries?.eachWithIndex() { templateQuery, i ->
            if (templateQuery) {
                templateQuery.index = i
            }
        }
    }

    private String getProductsList(Map executedConfiguration) {
        List data = []
        List drnData = []
        if (executedConfiguration.productGroupSelection) {
            data.addAll(getGroupNameFieldFromJson(executedConfiguration.productGroupSelection).split(","))
        }
        if (executedConfiguration.productSelection) {
            drnData.addAll(getAllDrugRecNumberFromJson(executedConfiguration.productSelection).split(", "))
            if (getPvsProductDictionaryService().isLevelGreaterThanProductLevel(executedConfiguration) && Holders.config.custom.qualitative.fields.enabled) {
                data.addAll(getCacheService().getUpperHierarchyProductDictionaryCache(executedConfiguration.id)?.split(","))
            } else {
                data.addAll(getAllProductNameFieldFromJson(executedConfiguration.productSelection).split(","))
            }
        }
        String drnString = drnData.sort().join(", ")
        data.sort().join(", ") + (drnString ? (', ' + drnString) : "")
    }

    private String getStudyList(Map executedConfiguration) {
        List data = []
        if (executedConfiguration.studySelection) {
            if (getPvsProductDictionaryService().isLevelGreaterThanProductLevel(executedConfiguration)) {
                data.addAll(getCacheService().getUpperHierarchyProductDictionaryCache(executedConfiguration.id)?.split(","))
            } else {
                data.addAll(getAllProductNameFieldFromJson(executedConfiguration.studySelection).split(","))
            }
        }
        data.sort().join(", ")
    }

    String getProductsListByConfig(Configuration configuration) {
        List data = []
        List drnData = []
        drnData.addAll(getAllDrugRecNumberFromJson(configuration.productSelection).split(","))
        if (configuration.productGroupSelection) {
            data.addAll(getGroupNameFieldFromJson(configuration.productGroupSelection).split(","))
        }
        if (configuration.productSelection) {
            data.addAll(getAllProductNameFieldFromJson(configuration.productSelection).split(","))
        }
        String drnString = drnData.sort().join(", ")
        data.sort().join(", ") + (drnString ? (' ,' + drnString) : "")
    }

    private Integer getTotalCountsForExecConfig(String execDispCounts){
        execDispCounts ? new JsonSlurper().parseText(execDispCounts).values().sum() : 0
    }

    private Integer getDispCountsForExecConfig(String execDispCounts, List<String> dispositionList){
        execDispCounts ? new JsonSlurper().parseText(execDispCounts).findAll { it.key in dispositionList  }.values().sum() ?: 0 : 0
    }

    List removeFDAColumns(Boolean isCustomEnabled, List fieldList){
        List customFields = [Constants.CustomQualitativeFields.APP_TYPE_AND_NUM,
                             Constants.CustomQualitativeFields.IND_NUM,
                             Constants.CustomQualitativeFields.CASE_TYPE,
                             Constants.CustomQualitativeFields.COMPLETENESS_SCORE,
                             Constants.CustomQualitativeFields.COMPOUNDING_FLAG,
                             Constants.CustomQualitativeFields.SUBMITTER,
                             Constants.CustomQualitativeFields.PRE_ANDA,
                             Constants.CustomQualitativeFields.MED_ERR_PT_LIST,
                             Constants.CustomQualitativeFields.MED_ERRS_PT,
                             Constants.CustomQualitativeFields.PAI_ALL_LIST,
                             Constants.CustomQualitativeFields.PRIM_SUSP_PAI_LIST,
                             Constants.CustomQualitativeFields.CROSS_REFERENCE_IND
        ]

        if(!isCustomEnabled){
            customFields.each{customValue ->
                fieldList.removeAll{it.name == customValue}}
        }
        return fieldList
    }

    String getAlertStatus(def configuration, def executedConfiguration = null) {
        String status = Constants.Commons.DASH_STRING
        if (configuration.isExecuting()) {
            status = Constants.AlertStatus.IN_PROGRESS
        } else if (configuration instanceof Configuration && configuration.isAutoPaused) {
            status = Constants.AlertStatus.AUTO_DISABLED
        } else if (configuration.hasProperty("deletionInProgress") && configuration?.deletionInProgress) {
            status = Constants.AlertStatus.DELETION_IN_PROGRESS
        } else if (configuration.isManuallyPaused) {
            status = Constants.AlertStatus.USER_DISABLED
        } else if (configuration instanceof Configuration && executedConfiguration) {
            if (configuration.numOfExecutions > 0) {
                status = Constants.AlertStatus.COMPLETED
            }
            ExecutionStatus executionStatus = ExecutionStatus.findByConfigIdAndExecutedConfigIdAndType(configuration.id, executedConfiguration.id, configuration.type)
            if (configuration.templateQueries && executionStatus && (executionStatus.reportExecutionStatus == ReportExecutionStatus.SCHEDULED || executionStatus.reportExecutionStatus == ReportExecutionStatus.GENERATING)) {
                status = Constants.AlertStatus.IN_PROGRESS
            }
            if (configuration.spotfireSettings && executionStatus && (executionStatus.spotfireExecutionStatus == ReportExecutionStatus.SCHEDULED || executionStatus.spotfireExecutionStatus == ReportExecutionStatus.GENERATING)) {
                status = Constants.AlertStatus.IN_PROGRESS
            }
        } else if (configuration.numOfExecutions > 0) {
            status = Constants.AlertStatus.COMPLETED
        }
        return status
    }

    def getConfigurationDomainByAlertType(String alertType) {
        def domain = null
        if (alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT || alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            domain = Configuration
        } else if (alertType == Constants.AlertConfigType.EVDAS_ALERT) {
            domain = EvdasConfiguration
        } else if (alertType == Constants.AlertConfigType.LITERATURE_SEARCH_ALERT) {
            domain = LiteratureConfiguration
        }
        return domain
    }
    /**
     *
     * @param isExcel
     * @param promptUser
     * @return whether to include case narrative in export report or not
     */
    Boolean checkCaseNarrativeConfiguration(Boolean isExcel, Boolean promptUser = false) {
        CaseNarrativeConfiguration cnc = CaseNarrativeConfiguration.getInstance()
        if (!isExcel) return false
        if (cnc.exportAlways) {
            return true
        } else if (cnc.promptUser && promptUser) {
            return true
        }
        return false
    }
    /**
     *
     * @param content
     * @return list of splitted case narrative data, item on each index to be added in respective column
     * of exported excel
     */
    List<String> splitCellContent(String content) {
        List<String> stringList = []
        if (content.length() <= Constants.ExcelConstants.MAX_CELL_CONTENT_LENGTH) {
            stringList.add(content)
        } else {
            stringList.add(content.substring(0, Constants.ExcelConstants.MAX_CELL_CONTENT_LENGTH))
            stringList.addAll(splitCellContent(content.substring(Constants.ExcelConstants.MAX_CELL_CONTENT_LENGTH)))
        }
        return stringList
    }

}
