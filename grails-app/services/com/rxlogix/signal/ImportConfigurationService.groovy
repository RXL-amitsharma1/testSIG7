package com.rxlogix.signal

import com.monitorjbl.xlsx.StreamingReader
import com.rxlogix.Constants
import com.rxlogix.audit.AuditTrailChild
import com.rxlogix.config.AlertDateRangeInformation
import com.rxlogix.config.Configuration
import com.rxlogix.config.CustomSQLValue
import com.rxlogix.config.DateRangeInformation
import com.rxlogix.config.DateRangeValue
import com.rxlogix.config.MasterConfiguration
import com.rxlogix.config.ParameterValue
import com.rxlogix.config.ProductGroup
import com.rxlogix.config.QueryExpressionValue
import com.rxlogix.config.QueryValueList
import com.rxlogix.config.ReportField
import com.rxlogix.config.TemplateQuery
import com.rxlogix.config.TemplateValueList
import com.rxlogix.enums.AdjustmentTypeEnum
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.enums.ImportConfigurationProcessState
import com.rxlogix.json.JsonOutput
import com.rxlogix.pvdictionary.product.view.LmProdDic200
import com.rxlogix.pvdictionary.product.view.LmProdDic201
import com.rxlogix.pvdictionary.product.view.LmProdDic202
import com.rxlogix.pvdictionary.product.view.LmProdDic203
import com.rxlogix.pvdictionary.product.view.LmProdDic204
import com.rxlogix.pvdictionary.product.view.LmProdDic205
import com.rxlogix.pvdictionary.product.view.LmProdDic206
import com.rxlogix.pvdictionary.product.view.LmProdDic207
import com.rxlogix.pvdictionary.product.view.LmProdDic208
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.SignalQueryHelper
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.io.FileType
import groovy.json.JsonSlurper
import groovy.sql.Sql
import org.apache.commons.io.FilenameUtils
import org.apache.http.util.TextUtils
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.type.LongType
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.springframework.context.MessageSource
import org.hibernate.sql.JoinType
import com.rxlogix.enums.EvaluateCaseDateEnum
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.pvdictionary.config.ViewConfig
import java.sql.ResultSet

import java.time.ZoneId
import org.apache.poi.ss.usermodel.WorkbookFactory;



@Transactional
class ImportConfigurationService {
    def userService
    def CRUDService
    def configurationService
    def dataObjectService
    def dynamicReportService
    def masterExecutorService
    def alertService
    MessageSource messageSource
    def hazelcastService
    def pvsProductDictionaryService
    def dataSheetService
    def cacheService
    def dataSource
    def signalDataSourceService

    File readFolder = new File(Holders.config.signal.configuration.import.folder.read as String)
    File uploadFolder = new File(Holders.config.signal.configuration.import.folder.upload as String)
    File failFolder = new File(Holders.config.signal.configuration.import.folder.fail as String)
    File logsDir = new File(Holders.config.signal.configuration.import.folder.logs as String)
    def sessionFactory

    def checkAndCreateBaseDirs() {
        File baseFolder = new File(Holders.config.signal.configuration.import.folder.base as String)
        if (!baseFolder.exists()) {
            log.debug("Base folder not found, creating it.")
            baseFolder.mkdir()
        }
        if (!readFolder.exists()) {
            log.debug("Source folder not found, creating it.")
            readFolder.mkdir()
        }

        if (!uploadFolder.exists()) {
            log.debug("Upload folder not found, creating it.")
            uploadFolder.mkdir()
        }

        if (!failFolder.exists()) {
            log.debug("Upload folder not found, creating it.")
            failFolder.mkdir()
        }

        if (!logsDir.exists()) {
            log.debug("Success folder not found, creating it.")
            logsDir.mkdir()
        }
    }


    def fetchAlertListByType(Map resultMap, String alertType,def params) {
        def startTime=System.currentTimeSeconds()
        List<User> allMatchedUsers =[]
        List<Group> allMatchedGroups =[]
        def max=params.length
        def offset=params.start
        def searchVal = params."search[value]"
        if(searchVal && !searchVal.contains("_")) {
            def userSearchAble = Arrays.asList(searchVal.split("\\s*,\\s*"))
            userSearchAble.each { def user ->
                allMatchedUsers += User.findAllByFullNameIlike('%' + user + '%')*.id
                allMatchedGroups += Group.findAllByNameIlike('%' + user + '%')*.id
            }
        }
        Map orderMap = alertService.prepareOrderColumnMap(params)
        List<Configuration> alertsResultList
        try {

            User currentUser = userService.getUser()
            Long workflowGroupId = currentUser.workflowGroup.id
            List<Long> groupIds = currentUser.groups?.collect { it.id }
            List<Configuration> configList
            def configurations
            Boolean isAggOrSingle = alertType in [Constants.AlertConfigType.SINGLE_CASE_ALERT, Constants.AlertConfigType.AGGREGATE_CASE_ALERT]

            Closure criteria = {
                sqlRestriction("IS_DELETED=false")
                sqlRestriction("""{alias}.id  in
               (${SignalQueryHelper.user_configuration_sql(getUserService().getCurrentUserId(), workflowGroupId,
                        groupIds.join(","), alertType)}
               )""")
                sqlRestriction("IS_CASE_SERIES=false")
                sqlRestriction("ADHOC_RUN=false")

                if (searchVal) {
                    String esc_char = ""
                    if (searchVal.contains('_')) {
                        searchVal = searchVal.replaceAll("\\_", "!_")
                        esc_char = "escape '!'"
                    } else if (searchVal.contains('%')) {
                        searchVal = searchVal.replaceAll("\\%", "!%%")
                        esc_char = "escape '!'"
                    }
                    List allConfigurationTemplateiDs = Configuration.createCriteria().list() {
                        sqlRestriction("UPPER(NAME) LIKE UPPER('%${searchVal}%') ${esc_char}")
                    }*.id
                    List masterConfigIds = MasterConfiguration.createCriteria().list() {
                        sqlRestriction("UPPER(NAME) LIKE UPPER('%${searchVal}%') ${esc_char}")
                    }*.id
                    'or' {
                        sqlRestriction("UPPER(NAME) LIKE UPPER('%${searchVal}%') ${esc_char}")
                        sqlRestriction("UPPER(SCHEDULE_DATE) LIKE UPPER('%${searchVal}%') ${esc_char}")
                        sqlRestriction("UPPER(PRODUCT_SELECTION) LIKE UPPER('%${searchVal}%') ${esc_char}") //sorting will not work on multiple products
                        if(allConfigurationTemplateiDs.size()>0){
                           'or' {
                               allConfigurationTemplateiDs.collate(999).each {
                                   sqlRestriction("CONFIGURATION_TEMPLATE_ID IN (${it.join(",")})")
                               }
                           }
                       }

                        if(allMatchedUsers.size()>0){
                            'or' {
                                allMatchedUsers.collate(999).each {
                                    sqlRestriction("ASSIGNED_TO_ID IN (${it.join(",")})")
                                }
                            }
                            'or' {
                                allMatchedUsers.collate(999).each {
                                    sqlRestriction("exists(SELECT 1 from SHARE_WITH_USER_CONFIG where config_id = {alias}.id and SHARE_WITH_USERID in (${it.join(",")}))")
                                }
                            }

                        }

                        if(allMatchedGroups.size()>0){
                            'or' {
                                allMatchedGroups.collate(999).each {
                                    sqlRestriction("ASSIGNED_TO_GROUP_ID IN (${it.join(",")})")
                                }
                            }
                            'or' {
                                allMatchedGroups.collate(999).each {
                                    sqlRestriction("exists (SELECT 1 from SHARE_WITH_GROUP_CONFIG where config_id = {alias}.id and SHARE_WITH_GROUPID in (${it.join(",")}))")
                                }
                            }
                            'or' {
                                allMatchedGroups.collate(999).each {
                                    sqlRestriction("exists (SELECT 1 from AUTO_SHARE_WITH_GROUP_CONFIG where config_id = {alias}.id and AUTO_SHARE_WITH_GROUPID in (${it.join(",")}))")
                                }
                            }
                        }

                        if (alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
                            if(masterConfigIds.size()>0){
                                masterConfigIds.collate(1000).each {
                                    'in'('masterConfigId', it)
                                }
                            }
                            sqlRestriction("UPPER(SELECTED_DATASHEET) LIKE UPPER('%${searchVal}%') ${esc_char}")
                        }
                    }
                }

                if (orderMap?.name != null) {
                    if (orderMap.name == "name") {
                        sqlRestriction(" 1=1 ORDER BY UPPER(NAME) ${orderMap.dir}")
                    } else if (orderMap.name == "nextRunDate") {
                        sqlRestriction(" 1=1 ORDER BY NEXT_RUN_DATE ${orderMap.dir}")
                    } else if (orderMap.name == "lastUpdated") {
                        sqlRestriction(" 1=1 ORDER BY LAST_UPDATED ${orderMap.dir}")
                    } else if (orderMap.name == "scheduleDateJSON") {
                        sqlRestriction(" 1=1 ORDER BY SCHEDULE_DATE ${orderMap.dir}")
                    } else if (orderMap.name == "products") {
                        sqlRestriction("1=1 ORDER BY dbms_lob.substr(product_selection, dbms_lob.getlength(product_selection), 1)  ${orderMap.dir}")
                    } else if (alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT && orderMap.name == "masterConfiguration") {
                        sqlRestriction(orderByMasterConfigurationName(orderMap?.name,orderMap?.dir))
                    }
                } else {
                        sqlRestriction(" 1=1 ORDER BY LAST_UPDATED DESC")
                    }
            }
            int totalCount =0;
            int filteredCount =0;
            if (alertType.equals(Constants.AlertConfigType.SINGLE_CASE_ALERT) || alertType.equals(Constants.AlertConfigType.AGGREGATE_CASE_ALERT)) {
                String searchValEscChar =  searchVal
                alertsResultList = Configuration.createCriteria().list([max: max, offset: offset], criteria) as List<Configuration>
                searchVal = searchValEscChar
                filteredCount = Configuration.createCriteria().list([:], criteria).size()
                searchVal = null;
                totalCount = Configuration.createCriteria().list([:], criteria).size()
            }

            List ressultList = resultMapData(alertsResultList, alertType)
            resultMap.aaData = ressultList
            resultMap.recordsTotal = totalCount
            resultMap.recordsFiltered = filteredCount
        } catch (Exception ex) {
            ex.printStackTrace()
        }
        def endTime=System.currentTimeSeconds()
        log.info("It took ${endTime-startTime} seconds to fetch all import config list")
        return resultMap
    }

    List<Map> resultMapData(List<Configuration> alertsResultList, def alertType = '') {
        String userTimezone = userService.getCurrentUserPreference()?.timeZone ?: Constants.UTC
        List<Map> finalMap = []
        Map datasheetMap = [:]
        Map templateConfigMap = linkedTemplateConfigMapping(alertsResultList*.id)
        Map alertDateRangeInfoConfigMap = alertDateRangeInfoMap(alertsResultList*.id)
        Boolean ifAdmin = SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")
        Boolean ifSharedAlertRole = SpringSecurityUtils.ifAnyGranted("ROLE_EXECUTE_SHARED_ALERTS")
        Long currentUserId = userService.getCurrentUserId()
        alertsResultList.each {
            Boolean autoShareWith = it.autoShareWithUser || it.autoShareWithGroup
            String selectedDataSheet = ""
            List dataSheetMap = dataSheetService.formatDatasheetMap(it)
            String dataSheetData =''
            if (!alertType?.equals(Constants.AlertConfigType.SINGLE_CASE_ALERT && it.selectedDataSheet && !TextUtils.isEmpty(it.selectedDataSheet))) {
                dataSheetData = dataSheetMap as JSON
                selectedDataSheet = dataSheetMap?.text?.join(',')
            }
            def config = [
                    id                       : it.id,
                    name                     : it.name,
                    isTemplateAlert          : it.isTemplateAlert,
                    configurationTemplate    : it.configurationTemplateId,
                    configurationTemplateName: templateConfigMap[it.id as Long],
                    products                 : getProductSelectionWithType(it.productSelection, it.productGroupSelection, true),
                    selectedDataSheet        : selectedDataSheet?:'',
                    dataSheetData            : dataSheetData?:'',
                    datasheetType            : it.datasheetType?:Constants.DatasheetOptions.CORE_SHEET,
                    selectedDataSources      : it?.selectedDatasource?.join(','),
                    alertDateRangeInformation: alertDateRangeInfoConfigMap[it.id as Long],
                    dateRangeType            : getDateRangeString(it, alertDateRangeInfoConfigMap[it.id as Long]),
                    assignedTo               : it.assignedTo,
                    assignedToGroup          : it.assignedToGroup,
                    sharedWith               : (it.shareWithUser?.fullName + it?.shareWithGroup?.name).join(', '),
                    scheduleDateJSON         : it.scheduleDateJSON,
                    nextRunDate              : it.nextRunDate? DateUtil.toDateStringWithTime(it.nextRunDate, userService.getUser()?.preference?.timeZone): "",
                    status                   : (it.isEnabled && it.nextRunDate),
                    type                     : it.type,
                    selectedDatasource       : it?.selectedDatasource,
                    productSelection         : it?.productSelection,
                    productGroupSelection    : it?.productGroupSelection,
                    shareWithMap             : JsonOutput.toJson(it?.shareWithGroup?.collect { [id: Constants.USER_GROUP_TOKEN + it?.id, name: it?.name] } + it?.shareWithUser?.collect { [id: Constants.USER_TOKEN + it?.id, name: it?.fullName] } + (autoShareWith ? [id: "AUTO_ASSIGN", name: "Auto Assign"] : [])),
                    isAutoAssignedTo         : it.isAutoAssignedTo,
                    isAutoSharedWith         : autoShareWith ? true : false,
                    isEditableAlert          : ifAdmin || currentUserId == it.owner.id || ifSharedAlertRole,
                    lastUpdated              : new Date(DateUtil.toDateStringWithTime(it.lastUpdated, userTimezone)).format(DateUtil.DATEPICKER_FORMAT_AM_PM).toString(),
                    asOfVersionDate          : it.asOfVersionDate,
                    evaluateDateAs           : it.evaluateDateAs.name(),
                    masterConfiguration      : it.type == "Aggregate Case Alert" && it.masterConfigId ? MasterConfiguration.get(it.masterConfigId)?.name:"",
                    masterConfigId           : it.masterConfigId,
                    unscheduled              : it.type == "Aggregate Case Alert" ? it.isLatestMaster && !it.masterConfigId : false,
                    isRunnable               : it.masterConfigId == null || it.nextRunDate == null,
                    isMultiIngredient        : it.isMultiIngredient != null ? it.isMultiIngredient : false

            ]

            finalMap.add(config)
        }
        return finalMap

    }

    Map linkedTemplateConfigMapping(List<Long> configIdList) {
        Sql sql = new Sql(dataSource)
        String whereClause = ""

        configIdList.collate(999).each{
            if(whereClause == ""){
                whereClause += "WHERE c1.id IN (${it.join(',')})"
            }
            else{
                whereClause += " OR c1.id IN  (${it.join(',')})"
            }
        }
        String query = """
           SELECT c1.id AS id,
           c2.NAME AS name
           FROM rconfig c1
           JOIN rconfig c2 ON c1.configuration_Template_Id = c2.id 
        """+whereClause
        Map result = sql.rows(query).collectEntries {
            [(it.id as Long): it.name]
        }
        sql?.close()
        return result
    }

    Map alertDateRangeInfoMap(List<Long> configIdList){
        Sql sql = new Sql(dataSource)
        String whereClause = ""

        configIdList.collate(999).each{
            if(whereClause == ""){
                whereClause += "WHERE r.id IN (${it.join(',')})"
            }
            else{
                whereClause += " OR r.id IN  (${it.join(',')})"
            }
        }

        String query = """
           SELECT
           r.id AS ID,
           adr.DATE_RNG_ENUM as dateRangeEnum,
           adr.RELATIVE_DATE_RNG_VALUE as relativeDateRangeValue,
           adr.DATE_RNG_END_ABSOLUTE as dateRangeEnd,
           adr.DATE_RNG_START_ABSOLUTE as dateRangeStart
           FROM
           rconfig r
           JOIN
           alert_date_range adr ON r.alert_data_range_id = adr.ID
        """ + whereClause

        Map result = sql.rows(query).collectEntries {
            [(it.id as Long): [dateRangeEnum: it.dateRangeEnum as DateRangeEnum, relativeDateRangeValue: it.relativeDateRangeValue, dateRangeEnd: (it.dateRangeEnd ? Date.from(it.dateRangeEnd.toLocalDateTime().atZone(ZoneId.systemDefault()).toInstant()) : null ), dateRangeStart: (it.dateRangeStart ? Date.from(it.dateRangeStart.toLocalDateTime().atZone(ZoneId.systemDefault()).toInstant()): null)]]
        }
        sql?.close()
        return result
    }

    String orderByMasterConfigurationName(String ordering_property, String direction) {
        String masterConfigurationSql = ""
        if(ordering_property == "masterConfiguration") {
            masterConfigurationSql = "select * from rconfig left join master_configuration  on rconfig.master_config_id = master_configuration.id order by master_configuration.name ${direction?direction.toLowerCase():'asc'}"
        }
        List<Long> configurationsIds = []
        try {
            Session session = sessionFactory.currentSession
            SQLQuery sqlQuery = session.createSQLQuery(masterConfigurationSql)
            sqlQuery.addScalar("id", new LongType())
            configurationsIds = sqlQuery.list()
            session.flush()
            session.clear()
        } catch(Exception ex) {
            ex.printStackTrace()
        }
        if(configurationsIds) {
            return ("1=1 order by decode(id, ${configurationsIds.collect { [it, configurationsIds.indexOf(it)] }?.flatten()?.join(',')})")
        } else {
            return ""
        }
    }

    List getProductSelectionWithType(String productSelection, String productGroupSelection, Boolean showDrug = false) {
        def jsonObj = null
        Map productTypeMap
        if(dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM)){
            productTypeMap = Holders.config.pvsignal.pvcm.dictionary.list
        }else{
            productTypeMap = Holders.config.custom.caseInfoMap.Enabled ? Holders.config.custom.dictionary.list : Holders.config.pvsignal.pva.dictionary.list
        }
        productTypeMap[Constants.DRUG_RECORD_NUMBER_LEVEL] = Constants.DRUG_RECORD_NUMBER
        List allProductsListWithType = []

        if (productSelection) {
            jsonObj = parseString(productSelection)
            if (jsonObj) {
                jsonObj.each { x ->
                    if (x.key == Constants.DRUG_RECORD_NUMBER_LEVEL && showDrug) {
                        allProductsListWithType << x.value.collect { it.id + ' (' + it.name + ') (' + productTypeMap[x.key] + ') ' }
                    } else if (x.key != Constants.DRUG_RECORD_NUMBER_LEVEL) {
                        allProductsListWithType << x.value.collect { it.name + ' (' + productTypeMap[x.key] + ') ' }
                    }
                }
            }
        }
        def jsonProductGroupObj = null


        if (productGroupSelection) {
            jsonProductGroupObj = parseString(productGroupSelection)
            if (jsonProductGroupObj) {
                jsonProductGroupObj.each { x ->
                    allProductsListWithType << x['name'] + ' (Product Group) '
                }
            }
        }

        allProductsListWithType.flatten()
    }

    def parseString(str) {
        try {
            def jsonSlurper = new JsonSlurper()
            jsonSlurper.parseText(str)
        } catch (all) {
            null
        }
    }

    Configuration createAlertFromTemplate(String alertType, Configuration alertTemplate, User user) {

        try {

            if (alertTemplate) {
                List<User> shareWithUsersList = []
                List shareWithUsersId = fetchShareWithUserConfig(alertTemplate.id)
                shareWithUsersId.each {
                    shareWithUsersList.add(User.get(it))
                }
                String newAlertName = getUniqueName(alertTemplate)
                Configuration configuration = new Configuration(name: newAlertName,
                        owner: user ? user : userService.getUser(),
                        description: alertTemplate?.description,
                        isPublic: alertTemplate?.isPublic,
                        isDeleted: alertTemplate?.isDeleted,
                        isEnabled: true,
                        isResume: false,
                        dateRangeType: alertTemplate?.dateRangeType,
                        productSelection: alertTemplate?.productSelection,
                        eventSelection: alertTemplate?.eventSelection,
                        studySelection: alertTemplate?.studySelection,
                        configSelectedTimeZone: alertTemplate?.configSelectedTimeZone,
                        evaluateDateAs: alertTemplate?.evaluateDateAs,
                        productDictionarySelection: alertTemplate?.productDictionarySelection,
                        limitPrimaryPath: alertTemplate?.limitPrimaryPath,
                        includeMedicallyConfirmedCases: alertTemplate?.includeMedicallyConfirmedCases,
                        excludeFollowUp: alertTemplate?.excludeFollowUp,
                        includeLockedVersion: alertTemplate?.includeLockedVersion,
                        adjustPerScheduleFrequency: alertTemplate?.adjustPerScheduleFrequency,
                        assignedToGroup: alertTemplate?.assignedToGroup,
                        isAutoTrigger: alertTemplate?.isAutoTrigger,
                        adhocRun: alertTemplate?.adhocRun,
                        frequency: null,
                        productGroupSelection: alertTemplate?.productGroupSelection,
                        selectedDatasource: alertTemplate?.selectedDatasource,
                        workflowGroup: alertTemplate?.workflowGroup,
                        productGroups: alertTemplate?.productGroups,
                        eventGroupSelection: alertTemplate?.eventGroupSelection,
                        asOfVersionDate: alertTemplate?.asOfVersionDate,
                        excludeNonValidCases: alertTemplate?.excludeNonValidCases,
                        groupBySmq: alertTemplate?.groupBySmq,
                        missedCases: alertTemplate?.missedCases,
                        blankValuesJSON: alertTemplate?.blankValuesJSON,
                        type: alertType == 'Aggregate Case Alert' ? Constants.AlertConfigType.AGGREGATE_CASE_ALERT : Constants.AlertConfigType.SINGLE_CASE_ALERT,
                        assignedTo: alertTemplate?.assignedTo,
                        repeatExecution: alertTemplate?.repeatExecution,
                        drugType: alertTemplate?.drugType,
                        drugClassification: alertTemplate?.drugClassification,
                        referenceNumber: alertTemplate?.referenceNumber,
                        suspectProduct: alertTemplate?.suspectProduct,
                        applyAlertStopList: alertTemplate?.applyAlertStopList,
                        spotfireSettings: alertTemplate?.spotfireSettings,
                        alertRmpRemsRef: alertTemplate?.alertRmpRemsRef,
                        alertQueryName: alertTemplate?.alertQueryName,
                        alertForegroundQueryName: alertTemplate?.alertForegroundQueryName,
                        alertTriggerCases: alertTemplate?.alertTriggerCases,
                        alertTriggerDays: alertTemplate?.alertTriggerDays,
                        asOfVersionDateDelta: alertTemplate?.asOfVersionDateDelta,
                        priority: alertTemplate?.priority,
                        reviewPeriod: alertTemplate?.reviewPeriod,
                        dispositions: alertTemplate?.dispositions,
                        alertQueryId: alertTemplate?.alertQueryId,
                        alertForegroundQueryId: alertTemplate?.alertForegroundQueryId,
                        foregroundSearch: alertTemplate?.foregroundSearch,
                        foregroundSearchAttr: alertTemplate?.foregroundSearchAttr,
                        template: alertTemplate?.template,
                        shareWithUser: shareWithUsersList,
                        shareWithGroup: alertTemplate?.shareWithGroup,
                        isAutoAssignedTo: alertTemplate?.isAutoAssignedTo,
                        autoShareWithUser: alertTemplate?.autoShareWithUser,
                        autoShareWithGroup: alertTemplate?.autoShareWithGroup,
                        integratedConfigurationId: alertTemplate?.integratedConfigurationId,
                        nextRunDate: null,
                        isTemplateAlert: false,
                        configurationTemplateId: alertTemplate?.id,
                        alertCaseSeriesId: alertTemplate?.alertCaseSeriesId,
                        alertCaseSeriesName: alertTemplate?.alertCaseSeriesName,
                        isProductMining: alertTemplate?.isProductMining,
                        isMultiIngredient: alertTemplate?.isMultiIngredient,
                        includeWHODrugs: alertTemplate?.includeWHODrugs,
                        studyCases: alertTemplate?.studyCases,
                        onlyPoi: alertTemplate?.onlyPoi,
                        ignoreStudyType: alertTemplate?.ignoreStudyType)

                AlertDateRangeInformation alertDateRangeInformation = new AlertDateRangeInformation()
                alertDateRangeInformation.relativeDateRangeValue = alertTemplate?.alertDateRangeInformation?.relativeDateRangeValue
                alertDateRangeInformation.dateRangeStartAbsolute = alertTemplate?.alertDateRangeInformation?.dateRangeStartAbsolute
                alertDateRangeInformation.dateRangeEndAbsolute = alertTemplate?.alertDateRangeInformation?.dateRangeEndAbsolute
                alertDateRangeInformation.dateRangeEnum = alertTemplate?.alertDateRangeInformation?.dateRangeEnum
                configuration.alertDateRangeInformation = alertDateRangeInformation
                configuration.scheduleDateJSON = getDefaultScheduleJSON(user)
                alertTemplate?.alertQueryValueLists.each {
                    QueryValueList queryValueList = new QueryValueList(query: it.query, queryName: it.queryName)

                    it.parameterValues.each { pv ->

                        ParameterValue executedValue
                        if (pv.hasProperty('reportField')) {
                            ReportField rp = ReportField.get(pv.reportField?.id)
                            executedValue = new QueryExpressionValue(key: pv.key,
                                    reportField: rp, operator: pv.operator, value: pv.value,
                                    operatorValue: messageSource.getMessage("app.queryOperator.$pv.operator", null, Locale.ENGLISH))
                        } else {
                            executedValue = new CustomSQLValue(key: pv.key, value: pv.value)
                        }
                        queryValueList.addToParameterValues(executedValue)

                    }
                    configuration.addToAlertQueryValueLists(queryValueList)
                }
                alertTemplate?.alertForegroundQueryValueLists.each {
                    QueryValueList queryValueList = new QueryValueList(query: it.query, queryName: it.queryName)

                    it.parameterValues.each { pv ->

                        ParameterValue executedValue
                        if (pv.hasProperty('reportField')) {
                            ReportField rp = ReportField.get(pv.reportField?.id)
                            executedValue = new QueryExpressionValue(key: pv.key,
                                    reportField: rp, operator: pv.operator, value: pv.value,
                                    operatorValue: messageSource.getMessage("app.queryOperator.$pv.operator", null, Locale.ENGLISH))
                        } else {
                            executedValue = new CustomSQLValue(key: pv.key, value: pv.value)
                        }
                        queryValueList.addToParameterValues(executedValue)

                    }
                    configuration.addToAlertForegroundQueryValueLists(queryValueList)
                }

                copyTemplateQuery(configuration,alertTemplate,user)
                return configuration

            }
        } catch (Throwable th) {
            th.printStackTrace()
        }
    }

    String getDateRangeString(Configuration config, Map alertDateRangeInformation) {
        StringBuilder dateString = new StringBuilder()
        if (config) {

            dateString.append(alertDateRangeInformation.dateRangeEnum.name())
            if (alertDateRangeInformation.dateRangeStart && alertDateRangeInformation.dateRangeEnd) {
                dateString.append(' (' + DateUtil.fromDateToString(alertDateRangeInformation.dateRangeStart as Date, DateUtil.DEFAULT_DATE_FORMAT) + ' - ' + DateUtil.fromDateToString(alertDateRangeInformation.dateRangeEnd as Date, DateUtil.DEFAULT_DATE_FORMAT) + ')')
            }
            Boolean lastX = alertDateRangeInformation.dateRangeEnum in [DateRangeEnum.LAST_X_DAYS, DateRangeEnum.LAST_X_MONTHS, DateRangeEnum.LAST_X_WEEKS, DateRangeEnum.LAST_X_YEARS]
            if (alertDateRangeInformation.relativeDateRangeValue && lastX) {
                dateString.append(' , X =' + alertDateRangeInformation.relativeDateRangeValue + ' ')
            }
            if (config.asOfVersionDate) {
                dateString.append(', Version As Of ( ' + DateUtil.fromDateToString(config.asOfVersionDate, DateUtil.DEFAULT_DATE_FORMAT) + ')')
            }
        }
        return dateString
    }

    String getUniqueName(Configuration config) {
        String newAlertName = 'Copy of ' + config?.name
        if(newAlertName.length()>=195){
            newAlertName = newAlertName.substring( 0,190)
        }
        List<Configuration> alertConfig = Configuration.findAllByNameIlikeAndWorkflowGroupAndIsDeleted(newAlertName + '%', userService?.getUser()?.workflowGroup, false)
        if (alertConfig.size() >= 1) {
            newAlertName = newAlertName + '(' + alertConfig.size() + ')'
        }
        return newAlertName
    }

    @Transactional
    def updateDateRange(Map params, Configuration config) {
        try {

            def timezone = Holders.config.server.timezone
            if (params.dateRangeEnum) {
                config?.alertDateRangeInformation.dateRangeEnum = params.dateRangeEnum
                if (config?.alertDateRangeInformation?.dateRangeEnum?.dateRangeType == DateRangeValue.RELATIVE) {
                    int relativeDateRangeX = params.relativeDateRangeValue ? params.relativeDateRangeValue as Integer : 1
                    config?.alertDateRangeInformation?.relativeDateRangeValue = relativeDateRangeX
                    config?.alertDateRangeInformation?.dateRangeStartAbsolute = null
                    config?.alertDateRangeInformation?.dateRangeEndAbsolute = null
                } else if (config?.alertDateRangeInformation?.dateRangeEnum?.dateRangeType == DateRangeValue.CUMULATIVE) {
                    config?.alertDateRangeInformation?.relativeDateRangeValue = 1
                    config?.alertDateRangeInformation?.dateRangeStartAbsolute = null
                    config?.alertDateRangeInformation?.dateRangeEndAbsolute = null
                } else if (config?.alertDateRangeInformation?.dateRangeEnum?.dateRangeType == DateRangeValue.CUSTOM) {
                    config?.alertDateRangeInformation?.relativeDateRangeValue = 1
                    config?.alertDateRangeInformation?.dateRangeStartAbsolute = DateUtil.stringToDate(params.dateRangeStartAbsolute, 'dd-MMM-yyyy', timezone)
                    config?.alertDateRangeInformation?.dateRangeEndAbsolute = DateUtil.stringToDate(params.dateRangeEndAbsolute, 'dd-MMM-yyyy', timezone)
                }
            }
            if(params.evaluateDateAs == 'LATEST_VERSION'){
                config.evaluateDateAs=EvaluateCaseDateEnum.LATEST_VERSION
                config.asOfVersionDate=null
            }else{
                config.evaluateDateAs=EvaluateCaseDateEnum.VERSION_ASOF
                config.asOfVersionDate=DateUtil.stringToDate(params.asOfVersionDate,"MM/dd/yyyy", timezone)
            }
            config.lastUpdated = new Date()
            CRUDService.updateWithAuditLog(config)

        } catch (Throwable th) {
            throw th
        }

    }

    def updateScheduleDateAndNextRunDate(Map params, Configuration config) {
        try {
            config.scheduleDateJSON = params.scheduleDateJSON
            if (Boolean.parseBoolean(params.repeatExecution)) {
                config.setIsEnabled(true)
                setNextRunDateAndScheduleDateJSON(config)
            } else {
                config.setIsEnabled(false)
                config.nextRunDate = null
                config.masterConfigId = null
            }
            CRUDService.updateWithAuditLog(config)

        } catch (Throwable th) {
            throw th
        }
    }

    private setNextRunDateAndScheduleDateJSON(Configuration configurationInstance) {
        if (configurationInstance.scheduleDateJSON && configurationInstance.isEnabled) {
            configurationInstance.nextRunDate = configurationService.getNextDate(configurationInstance)
        } else {
            configurationInstance.nextRunDate = null
        }
    }

    String getDefaultScheduleJSON(User user = null) {
        String userTimeZone = user ? user.preference?.timeZone : userService.getUser()?.preference?.timeZone
        String StartDate = DateUtil.stringFromDate(new Date(), "yyyy-MM-dd'T'HH:mm", userTimeZone)
        String scheduleJson = '{"startDateTime":"' + StartDate + 'Z","timeZone":{"name":"' + userTimeZone + '","offset":"' + DateUtil?.getOffsetString(userTimeZone) + '"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1"}'

        return scheduleJson
    }

    Map getSortedListData(Map params) {
        List<Configuration> alertsResultList
        Map columnMap = ['1': 'name', '2': 'configurationTemplate', '3': 'productSelection', '5': 'assignedTo', '6': 'shareWith', '8': 'nextRunDate']
        List resultList = []
        List masterList = []
        try {
            User currentUser = userService.getUser()
            Long workflowGroupId = currentUser.workflowGroup.id
            List<Long> groupIds = currentUser.groups?.collect { it.id }
            List<Configuration> configList
            def configurations
            String alertType = params?.alertType
            Boolean isAggOrSingle = alertType in [Constants.AlertConfigType.SINGLE_CASE_ALERT, Constants.AlertConfigType.AGGREGATE_CASE_ALERT]

            Closure criteria = {
                eq("isDeleted", false)
                'or' {
                    and {
                        eq("workflowGroup.id", workflowGroupId)
                        or {
                            sqlRestriction("{alias}.id in (SELECT CONFIG_ID from SHARE_WITH_USER_CONFIG where SHARE_WITH_USERID = ${currentUser.id})")
                            if (isAggOrSingle) {
                                sqlRestriction("{alias}.id in (SELECT CONFIG_ID from AUTO_SHARE_WITH_USER_CONFIG where AUTO_SHARE_WITH_USERID = ${currentUser.id})")
                            }
                            if (groupIds) {
                                or {
                                    groupIds.collate(1000).each {
                                        sqlRestriction("{alias}.id in (SELECT CONFIG_ID from SHARE_WITH_GROUP_CONFIG where SHARE_WITH_GROUPID in (${it.join(",")}))")
                                    }
                                }
                                if (isAggOrSingle) {
                                    or {
                                        groupIds.collate(1000).each {
                                            sqlRestriction("{alias}.id in (SELECT CONFIG_ID from AUTO_SHARE_WITH_GROUP_CONFIG where AUTO_SHARE_WITH_GROUPID in (${it.join(",")}))")
                                        }
                                    }
                                }
                            }
                        }
                    }
                    eq('owner.id', currentUser.id)
                }
                eq("type", alertType)
                eq("isCaseSeries", false)
                eq("adhocRun", false)
                order("lastUpdated", "desc")
            }

            if (alertType.equals(Constants.AlertConfigType.SINGLE_CASE_ALERT) || alertType.equals(Constants.AlertConfigType.AGGREGATE_CASE_ALERT)) {
                alertsResultList = Configuration.createCriteria().list(criteria) as List<Configuration>
            }
            List<MasterConfiguration> masterConfigurationList = MasterConfiguration.createCriteria().list {
                if(alertsResultList) {
                    'or'{
                        alertsResultList.collate(1000).each {
                            'in'("id", it.masterConfigId)
                        }
                    }
                }
            } as List<MasterConfiguration>

            Map alertDateRangeInformationList
            Map templateConfigMap
            if (alertsResultList) {
                alertDateRangeInformationList = alertDateRangeInfoMap(alertsResultList*.id)
                templateConfigMap = linkedTemplateConfigMapping(alertsResultList*.id)
            }
            alertsResultList.each { it ->
                Map alertDateRangeInformationMap = alertDateRangeInformationList[it.id as Long]
                Boolean autoShared = it.autoShareWithUser || it.autoShareWithGroup
                String confTemplateName = templateConfigMap[it.id as Long]
                String products = getProductSelectionWithType(it?.productSelection, it?.productGroupSelection).join(", ")
                String startDate = alertDateRangeInformationMap.dateRangeStart ? DateUtil.fromDateToString(alertDateRangeInformationMap.dateRangeStart, DateUtil.DEFAULT_DATE_FORMAT) : ''
                String endDate = alertDateRangeInformationMap.dateRangeEnd ? DateUtil.fromDateToString(alertDateRangeInformationMap.dateRangeEnd, DateUtil.DEFAULT_DATE_FORMAT) : ''
                String versionAsOfDate = it?.asOfVersionDate ? DateUtil.fromDateToString(it?.asOfVersionDate, DateUtil.DEFAULT_DATE_FORMAT) : ''
                String assignedToValue = it?.isAutoAssignedTo ? "Auto Assign" : (it?.assignedTo ? it?.assignedTo?.username : it?.assignedToGroup?.name)
                List shareWithList = it.shareWithUser?.username + it?.shareWithGroup?.name
                List  selectedDatasheets =[]
                if(it.selectedDataSheet && !TextUtils.isEmpty(it.selectedDataSheet)){
                    selectedDatasheets =dataSheetService.formatDatasheetMap(it)?.text
                }
                String typeOfAlert = it.type == "Single Case Alert" ? "Individual Case Review" : "Aggregate Review"
                if (autoShared) {
                    shareWithList << "Auto Assign"
                }
                String shareWith = (shareWithList).join(", ")
                MasterConfiguration masterConfig = masterConfigurationList.find {it2->it2.id == it.masterConfigId }
                resultList.add([it?.name, typeOfAlert, it?.owner?.username, it?.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT && masterConfig ? masterConfig.name:Constants.importConfigSheet.IMPORT_VALUE_EMPTY,
                                confTemplateName, products, it?.includeWHODrugs? Constants.importConfigSheet.IMPORT_VALUE_ONE : Constants.importConfigSheet.IMPORT_VALUE_EMPTY, it?.isMultiIngredient? Constants.importConfigSheet.IMPORT_VALUE_ONE : Constants.importConfigSheet.IMPORT_VALUE_EMPTY, selectedDatasheets?.join(',')?:Constants.importConfigSheet.IMPORT_VALUE_EMPTY, alertDateRangeInformationMap?.dateRangeEnum, alertDateRangeInformationMap?.relativeDateRangeValue,
                                startDate, endDate, versionAsOfDate, assignedToValue, shareWith, it?.scheduleDateJSON
                ])
            }

            masterConfigurationList.each {it->
                masterList.add([it.name, it.configTemplate, it.productHierarchy, it.includeWHODrugs? Constants.importConfigSheet.IMPORT_VALUE_ONE : Constants.importConfigSheet.IMPORT_VALUE_EMPTY, it.isMultiIngredient? Constants.importConfigSheet.IMPORT_VALUE_ONE : Constants.importConfigSheet.IMPORT_VALUE_EMPTY, it.dateRangeType, it.lastX as String,
                                it.startDate?new SimpleDateFormat(DateUtil.DEFAULT_DATE_FORMAT).format(it.startDate):"",
                                it.endDate?new SimpleDateFormat(DateUtil.DEFAULT_DATE_FORMAT).format(it.endDate):"",
                                it.asOfVersionDate, it.scheduler])
            }

        } catch (Exception ex) {
            log.error(ex.message, ex)
        }
        return [resultList: resultList, masterList: masterList]
    }

    boolean checkFileFormat(File fileToBeProcessed, String importType = null) {
        Map baseColumnTypeMap = Constants.importConfigSheet.BASE_COLUMN_MAP.clone() //Added for PVS-71197
        if (importType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            baseColumnTypeMap['unschedule'] = 'String'
        }
        boolean isCorrectFormat = false
        switch (FilenameUtils.getExtension(fileToBeProcessed.name)) {
            case 'xls':
            case 'xlsx':
            case 'xlx':
            case 'xlsm':
            case 'xlm':
                isCorrectFormat = checkExcelFileFormat(fileToBeProcessed, baseColumnTypeMap)
                break
            default:
                log.error("Not supported")
        }
        isCorrectFormat
    }

    boolean checkExcelFileFormat(File file, Map<String, String> baseColumnTypeMap) {
        InputStream is = new FileInputStream(file)
        Workbook workbook = WorkbookFactory.create(is);

        Sheet sheet = workbook.getSheetAt(0)
        Row headerRow = sheet.rowIterator().next()

        List<String> headerNamesList = []
        headerRow.each { Cell cell ->
            headerNamesList.add(cell.getStringCellValue().trim().toLowerCase())
        }
        workbook.close()

        int index = headerNamesList.findLastIndexOf { it }
        if ((index + 1) != headerNamesList.size()) {
            headerNamesList = headerNamesList.take(index + 1)
        }
        headerNamesList == baseColumnTypeMap.keySet() as List<String>
    }

    ImportConfigurationLog saveImportConfigurationLog(String fileName, User user, String importType = null) {
        ImportConfigurationLog importConfigurationLog = new ImportConfigurationLog(importFileName: fileName, importedBy: user, importedDate: new Date(), status: ImportConfigurationProcessState.IN_READ, importConfType: importType)
        importConfigurationLog.nodeUuid = hazelcastService.hazelcastInstance.cluster.localMember.uuid
        importConfigurationLog.save(flush: true)

    }

    void createDir(String logsFilePath) {
        File logsFileDir = new File(logsFilePath)
        if (!logsFileDir.exists()) {
            logsFileDir.mkdir()
        }
    }

    void copyFile(File file, String destination) {
        String updatedPath = "$destination/${file.name}"
        File newDestination = new File(updatedPath)
        Files.copy(file.toPath(), newDestination.toPath(), StandardCopyOption.REPLACE_EXISTING)
    }

    void startProcessingUploadedFile() {
        boolean isFileUploading = checkIfFileExistsInUploadFolder()
        ImportConfigurationLog importConfigurationLog = getNextImportFile()
        if (isFileUploading || !importConfigurationLog)
            return
        File fileToUpload = getNextFileToUpload(importConfigurationLog, readFolder)
        try {
            log.info("Job for Importing Configurations Started")
            updateImportConfigurationLog(importConfigurationLog, ImportConfigurationProcessState.IN_PROCESS)
            boolean result = false
            if(fileToUpload) {
                fileToUpload = moveFile(fileToUpload, uploadFolder as String)
                result = persistImportConfigurationFromExcel(fileToUpload, importConfigurationLog)
            }
            if(result)
                updateImportConfigurationLog(importConfigurationLog, ImportConfigurationProcessState.SUCCESS)
            else
                updateImportConfigurationLog(importConfigurationLog, ImportConfigurationProcessState.FAILED)

            log.info("Job for Importing Configurations Ended")
        } catch (Throwable th) {
            log.error(th.getMessage(), th)
            if(fileToUpload) {
                moveFile(fileToUpload, failFolder as String)
            }
            updateImportConfigurationLog(importConfigurationLog, ImportConfigurationProcessState.FAILED)
        }
    }

    boolean checkIfFileExistsInUploadFolder() {
        if (uploadFolder.exists() && uploadFolder.isDirectory()) {
            return uploadFolder.listFiles().size()
        }
        return false
    }

    ImportConfigurationLog getNextImportFile() {
        ImportConfigurationLog.createCriteria().get {
            eq("status", ImportConfigurationProcessState.IN_READ)
            eq("nodeUuid", hazelcastService.hazelcastInstance.cluster.localMember.uuid)
            order("importedDate", "asc")
            maxResults(1)
        } as ImportConfigurationLog
    }

    File getNextFileToUpload(ImportConfigurationLog importConfigurationLog, File folder) {
        List<File> fileListToProcess = []
        folder.eachFileRecurse(FileType.FILES) { file ->
            fileListToProcess << file
        }
        fileListToProcess.find { it.name == importConfigurationLog.importFileName }
    }


    void updateImportConfigurationLog(ImportConfigurationLog importConfigurationLog, ImportConfigurationProcessState importConfigurationProcessState) {
        importConfigurationLog.status = importConfigurationProcessState
            ImportConfigurationLog.executeUpdate("Update ImportConfigurationLog set status = :status where id = ${importConfigurationLog.id}",
                    [status: importConfigurationProcessState])
    }


    File moveFile(File file, String destination, String newFileName = null) {
        String updatedPath = "$destination/${newFileName ?: file.name}"
        File newDestination = new File(updatedPath)
        Files.move(file.toPath(), newDestination.toPath(), StandardCopyOption.REPLACE_EXISTING)
        new File(updatedPath)
    }

    boolean persistImportConfigurationFromExcel(File fileToUpload, ImportConfigurationLog importConfigurationLog) {
        try {
            Map processedRecordsStatus = processingOfImportedRecords(fileToUpload, importConfigurationLog?.importConfType, importConfigurationLog?.importedBy)
            fileToUpload.delete()
            updateImportConfigurationLog(importConfigurationLog, ImportConfigurationProcessState.SUCCESS)
            byte[] file = generateImportConfigurationLogFile(processedRecordsStatus, importConfigurationLog?.importedBy, importConfigurationLog?.importConfType)
            String directory = Holders.config.signal.configuration.import.folder.logs + "/" + importConfigurationLog?.id
            String originalFileName = importConfigurationLog?.importFileName
            String logFileName = FilenameUtils.removeExtension(originalFileName) + '_Log.xlsx'
            new File(directory, logFileName).bytes = file
            return true

        } catch (Exception e) {
            log.error("error occurred while persisting record", e)
            moveFile(fileToUpload, Holders.config.configuration.import.folder.fail as String)
            updateImportConfigurationLog(importConfigurationLog, ImportConfigurationProcessState.FAILED)
        }
        return false

    }

    Map processingOfImportedRecords(File file, String importAlertType, User user) {
        try {
            Map importedData = processFile(file, importAlertType)
            List<Map> importedRecords = importedData.alertConfigs
            List<Map> masterRecords = importedData.masterConfigs
            Map statusForAllConfigs = [:]
            List recordStatusCreated = []
            List recordStatusUpdated = []
            Map<String, Map> masterConfigs = [:]
            List<Configuration> childConfigs = []
            Map checkDuplicateRecord = [:]
            Map checkDuplicateProduct = [:]
            Map checkDuplicateMaster = [:]
            Boolean duplicatePass = true
            if(masterRecords) {
                Locale locale = user?.preference?.locale ?: Locale.ENGLISH
                cacheService.masterUploadRunningStatus(true)
                validateMasterConfigRecords(masterRecords, importedRecords, locale)
                masterRecords.each { masterRecord ->
                    try {
                        List masterError = []
                        MasterConfiguration masterConfiguration = createUpdateMasterConfigFromRecord(masterRecord, masterError, user)
                        masterConfigs.put(masterRecord["master configuration"]?.toLowerCase(), [name: masterRecord["master configuration"]?.toLowerCase(),
                                                                                                masterError: masterError, masterConfig: masterConfiguration])
                    } catch (Throwable th) {
                        log.error(alertService.exceptionString(th))
                        List error = ['Fail', th.message]
                        masterConfigs.put(masterRecord["master configuration"]?.toLowerCase(), [name: masterRecord["master configuration"]?.toLowerCase(),
                                                                                                masterError: error, masterConfig: null])
                    }
                }
            }
            importedRecords.each { record ->
                duplicatePass = true
                String selectedType = importAlertType == "Single Case Alert" ? "Individual Case Review" : "Aggregate Review"
                if (record['alert type'].trim() == selectedType) {
                    String alertName = record[Constants.ImportConfiguration.ALERT_NAME]
                    if(selectedType == "Aggregate Review" && record['master configuration'] && alertName) {
                        //MasterConfiguration masterConfiguration = createUpdateMasterConfigFromRecord(record, masterRecords, recordStatusCreated, user, masterConfigs)
                        MasterConfiguration masterConfiguration = masterConfigs.get(record['master configuration'].toLowerCase())?.masterConfig
                        if(masterConfiguration) {
                            String productName = ""
                            if(masterConfiguration.productHierarchy == "Ingredient" || masterConfiguration.productHierarchy == "Substance") {
                                productName = record["products"].toLowerCase() + record["multi-ingredient"].toLowerCase()
                            } else {
                                productName = record["products"].toLowerCase()
                            }
                            //Added changes for PVS-66680 FOR ALERT NAME SPECIAL CHARACTER VALIDATION
                            Object res = MiscUtil.validator(alertName, Constants.ImportConfiguration.NAME, Constants.SpecialCharacters.DEFAULT_CHARS as String[])

                            if (res instanceof List) {
                                duplicatePass = false
                                recordStatusCreated << record.values() + ['Fail', res[Constants.Commons.ONE]]
                            } else if(checkDuplicateRecord.get(alertName)) {
                                duplicatePass = false
                                recordStatusCreated << record.values() + ['Fail', 'Alert Name is more than once.']
                            } else if(checkDuplicateProduct.get(productName)) {
                                duplicatePass = false
                                recordStatusCreated << record.values() + ['Fail', 'Products field is more than once.']
                            } else {
                                checkDuplicateRecord.put(record["alert name"].toLowerCase(), record["alert name"].toLowerCase())
                                checkDuplicateProduct.put(productName, record["products"].toLowerCase())
                            }
                            String masterConfigData = record["configuration template"] + "-" + record["date range type"] + "-" + record["x"]
                            masterConfigData = masterConfigData + "-" +record["start date"] + "-" + record["end date"] + "-" + record["version as of date"] + "-" + record["scheduler"]
                            String existingData = checkDuplicateMaster.get(record["master configuration"].toLowerCase())
                            if(existingData) {
                                if(existingData != masterConfigData && duplicatePass) {
                                    duplicatePass = false
                                    recordStatusCreated << record.values() + ['Fail', 'Master Configuration data is mismatched for other record.']
                                }
                            } else {
                                checkDuplicateMaster.put(record["master configuration"].toLowerCase(), masterConfigData)
                            }
                            if(duplicatePass) {
                                if (record['unschedule'] && record['unschedule'].toLowerCase() == Constants.Commons.YES_LOWERCASE) {
                                    unscheduleAndDelinkChild(record, user, recordStatusUpdated, childConfigs, masterConfiguration)
                                } else {
                                    createChildConfigForMaster(record, masterConfiguration, recordStatusCreated, user, childConfigs)
                                }
                            }
                        } else if(!masterConfigs.get(record['master configuration'].toLowerCase())) {
                            recordStatusCreated << record.values() + ['Fail', 'Master Configuration is not available.']
                        } else {
                            recordStatusCreated << record.values() + masterConfigs.get(record['master configuration'].toLowerCase())?.masterError
                        }
                    } else {
                        if (record['alert name']) {
                            Configuration config
                            List<Configuration> conf = getConfig(record['alert name']?.trim(), user, importAlertType)
                            Boolean configExists = false
                            if (conf.size() > 1) {
                                configExists = true
                                if (record['owner'].trim()) {
                                    User owner = User.findByUsername(record['owner'].trim())
                                    if (owner) {
                                        config = conf.find { it.owner == owner }
                                        if (!config) {
                                            configExists = false
                                        }
                                    } else {
                                        recordStatusCreated << record.values() + ['Fail', "Owner with given name doesn't exist"]
                                    }
                                } else {
                                    recordStatusCreated << record.values() + ['Fail', "Multiple alerts present with this name, Owner is required"]
                                }
                            } else if (conf.size() == 1) {
                                config = conf[0]
                                configExists = true
                            }
                            if (config) {
                                Map recordStatusCreatedMap = updateConfigurationFromRecord(record, config, user, null)
                                if (recordStatusCreatedMap?.status) {
                                    recordStatusCreated << record.values() + ['Success', recordStatusCreatedMap?.message]
                                } else {
                                    recordStatusCreated << record.values() + ['Fail', recordStatusCreatedMap?.message]
                                }
                            } else if (!configExists) {
                                if (record['configuration template']) {
                                    Map recordStatusCreatedMap = updateConfigurationFromTemplate(record, user, null)
                                    if (recordStatusCreatedMap?.status) {
                                        recordStatusCreated << record.values() + ['Success', recordStatusCreatedMap?.message]
                                    } else {
                                        recordStatusCreated << record.values() + ['Fail', recordStatusCreatedMap?.message]
                                    }
                                } else {
                                    recordStatusCreated << record.values() + ['Fail', 'Configuration Template name should be present for New configurations.']
                                }
                            }
                        } else {
                            recordStatusCreated << record.values() + ['Fail', 'Alert name is required.']
                        }
                    }
                } else {
                    recordStatusCreated << record.values() + ['Fail', 'Alert type mismatch, Selected alert type is: ' + selectedType]
                }
            }
            if(childConfigs) {
                alertService.updateProdSelectionForDrugNumber(childConfigs as List)
                alertService.batchPersistForDomain(childConfigs as List, Configuration.class.name)
            }
            statusForAllConfigs.put('configurationsCreated', recordStatusCreated)
            // Refreshing the existing child alerts based on the uploaded template.
            if (masterRecords && childConfigs) {
                refreshChildLinkedWithMaster(masterRecords, masterConfigs, childConfigs*.id, user, recordStatusUpdated)
            }
            statusForAllConfigs.put('configurationsUpdated', recordStatusUpdated)
            log.info("Processing of the File Ended!")
            return statusForAllConfigs
        } catch (Exception ex) {
            log.error(ex.message, ex)
            throw ex
        } finally {
            cacheService.masterUploadRunningStatus(false)
        }
    }

    String checkForMultiIngredient(List<Map> records, Map masterRecord) {
        if(masterRecord["products hierarchy"] == "Ingredient" || masterRecord["products hierarchy"] == "Substance" || masterRecord["products hierarchy"] == "Product Group") {
            if(masterRecord["multi-ingredient"] == "1" || masterRecord["multi-ingredient"] == "") {
                if(isMasterChildInSync(records, masterRecord)) {
                    return ""
                } else {
                    return "One of the value(Multi-Ingredient/Product Hierarchy) mismatch in Master and Child"
                }
            } else {
                return "Master Multi-Ingredient value is not correct"
            }
        } else {
            if(masterRecord["multi-ingredient"] == "") {
                if(isMasterChildInSync(records, masterRecord)) {
                    return ""
                } else {
                    return "One of the value(Multi-Ingredient/Product Hierarchy) mismatch in Master and Child"
                }
            } else {
                return "Multi-Ingredient value is not correct"
            }
        }
    }
    /**
     *
     * @param masterRecord Map containing who enabled information
     * @param locale Locale for language
     * @return String text message which containing detailed error information
     */
    String checkForIncludeWHODrugs(Map masterRecord, Locale locale) {
        String includeWhoDrugs = masterRecord[Constants.importConfigSheet.INCLUDE_WHO_COLUMN]
        String multiIngredient = masterRecord[Constants.importConfigSheet.MULTI_INGREDIENT_COLUMN]
        String result = null
        if(includeWhoDrugs in [Constants.importConfigSheet.IMPORT_VALUE_ONE, Constants.importConfigSheet.IMPORT_VALUE_EMPTY]) {
            if(includeWhoDrugs == Constants.importConfigSheet.IMPORT_VALUE_ONE && multiIngredient != Constants.importConfigSheet.IMPORT_VALUE_ONE) {
                result =  messageSource.getMessage('app.import.configuration.message.multiIngredientIncorrect', null, locale)
            }
        } else {
            result =  messageSource.getMessage('app.import.configuration.message.includeWhoDrugsIncorrect', null, locale)
        }
        return result
    }

    Boolean isMasterChildInSync(List<Map> records, Map masterRecord) {
        !records.any {it["master configuration"] == masterRecord["master configuration"] && !it["products"].contains(masterRecord["products hierarchy"])}
    }

    def updateConfigurationFromRecord(Map record, Configuration config1, User user, MasterConfiguration masterConfiguration=null) {
        Configuration config
        try {
            String recordConfigTemplate = masterConfiguration?.configTemplate?: record['configuration template']
            config = Configuration.findById(config1.id)
            String selectedAlertType = getImportAlertType(record)
            Configuration template
            Locale locale = user?.preference?.locale ?: Locale.ENGLISH
            if (recordConfigTemplate) {
                List<Configuration> templateConfigs = getTemplateConfig(recordConfigTemplate.trim(), user, selectedAlertType)
                if (templateConfigs.size() > 1) {
                    if (record['owner'].trim()) {
                        User owner = User.findByUsername(record['owner'].trim())
                        if (owner) {

                            template = templateConfigs.find { it.owner == owner }
                            if (!template) {
                                return [status: false, message: "No templates exist for given owner, Owner is required- Multiple Template Alerts present with this name"]
                            }
                        } else {
                            return [status: false, message: "Owner with given name doesn't exist, Owner is required- Multiple Template Alerts present with this name"]
                        }
                    } else {
                        return [status: false, message: "Multiple Template Alerts present with this name, Owner is required"]
                    }
                } else if (templateConfigs.size() == 1) {
                    template = templateConfigs[0]
                }
                if (template) {
                    User configOwner = User.findByUsername(record['owner'].trim())
                    updateAlertFromTemplate(template.type, template, user, config, configOwner)
                    config.configurationTemplateId = template?.id
                } else {
                    return [status: false, message: 'No Configuration Template exist with given name']
                }
            }

            String recordDateRangeType = record['date range type']
            String recordStartDate = record['start date']
            String recordEndDate = record['end date']
            String recordLastX = record['x']
            String recordVersionAsOfDate = record['version as of date']
            String recordScheduler = record['scheduler']
            String recordMultiIngredient = record['multi-ingredient']
            Boolean recordIncludeWHODrugs = false
            if(masterConfiguration) {
                config.masterConfigId = masterConfiguration.id
                config.isLatestMaster = true
                config.isMultiIngredient = recordMultiIngredient == "1"? true:false
                recordDateRangeType = recordDateRangeType?:masterConfiguration.dateRangeType
                recordStartDate = recordStartDate?:masterConfiguration.startDate?new SimpleDateFormat(DateUtil.DEFAULT_DATE_FORMAT).format(masterConfiguration.startDate):""
                recordEndDate = recordEndDate?:masterConfiguration.endDate?new SimpleDateFormat(DateUtil.DEFAULT_DATE_FORMAT).format(masterConfiguration.endDate):""
                recordLastX = recordLastX?:masterConfiguration.lastX
                recordVersionAsOfDate = recordVersionAsOfDate?:masterConfiguration.asOfVersionDate
                recordScheduler = recordScheduler?:masterConfiguration.scheduler
            }


            if (selectedAlertType in [Constants.AlertConfigType.SINGLE_CASE_ALERT, Constants.AlertConfigType.AGGREGATE_CASE_ALERT]) {
                String includeWhoDrugs = record[Constants.importConfigSheet.INCLUDE_WHO_COLUMN]
                String multiIngredient = record[Constants.importConfigSheet.MULTI_INGREDIENT_COLUMN]
                if (includeWhoDrugs in [Constants.importConfigSheet.IMPORT_VALUE_ONE, Constants.importConfigSheet.IMPORT_VALUE_EMPTY]) {
                    if (includeWhoDrugs == Constants.importConfigSheet.IMPORT_VALUE_ONE && multiIngredient != Constants.importConfigSheet.IMPORT_VALUE_ONE) {
                        return [status: false, message: messageSource.getMessage('app.import.configuration.message.multiIngredientIncorrect', null, locale)]
                    } else if (includeWhoDrugs == Constants.importConfigSheet.IMPORT_VALUE_EMPTY) {
                        if (!multiIngredient in [Constants.importConfigSheet.IMPORT_VALUE_ONE, Constants.importConfigSheet.IMPORT_VALUE_EMPTY]) {
                            return [status: false, message: messageSource.getMessage('app.import.configuration.message.multiIngredientIncorrect', null, locale)]
                        }
                    }
                } else {
                    return [status: false, message: messageSource.getMessage('app.import.configuration.message.includeWhoDrugsIncorrect', null, locale)]
                }
                if (masterConfiguration) {
                    config.includeWHODrugs = masterConfiguration.includeWHODrugs
                } else {
                    config.includeWHODrugs = (includeWhoDrugs == Constants.importConfigSheet.IMPORT_VALUE_ONE)
                }
            }

            if (masterConfiguration) {
                config.isMultiIngredient = masterConfiguration.isMultiIngredient
            } else {
                config.isMultiIngredient = (recordMultiIngredient == Constants.importConfigSheet.IMPORT_VALUE_ONE)
            }

            if (record['products']) {
                String dataSource = config?.selectedDatasource
                List productsListFromRecord = record['products']?.trim().split(' , ')
                List<String> productGroupsList = []
                productsListFromRecord.collect { it ->
                    if (it?.trim().contains('(Product Group)')) {
                        productGroupsList.add(it.replace('(Product Group)', '').trim())
                    }
                }
                if (productGroupsList) {
                    // product group update
                    List<?> productGroupMapList = []
                    log.info("Product Group(s) List: " + productGroupsList)
                    productGroupsList.each { productGroupName ->
                        productGroupName = productGroupName.substring(0, productGroupName.lastIndexOf("(") ).trim()
                        Map productGroup = pvsProductDictionaryService.fetchProductGroup(PVDictionaryConfig.PRODUCT_GRP_TYPE, productGroupName, dataSource, 1, 30, user?.username,true, config.isMultiIngredient)
                        productGroupMapList.add(productGroup)
                    }
                    productGroupMapList.removeAll([null])
                    if (productGroupMapList) {
                        //save Product Group
                        config.productGroupSelection = productGroupMapList.collect { it ->
                            [name: it.name, id: it.id, isMultiIngredient: config.isMultiIngredient, includeWHODrugs: config.includeWHODrugs]
                        } as JSON
                        config.productSelection = null
                    } else {
                        return [status: false, message: "Couldn't find product Group(s) with given name for this DataSource and User."]
                    }
                } else {
                    if (dataSource == 'pva' || dataSource == 'faers' || dataSource == 'eudra' || dataSource == 'vaers' || dataSource == 'vigibase') {
                        Map product = [:]
                        Boolean isPvcm = dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM)
                        product = Holders.config.custom.caseInfoMap.Enabled ? getProductSelectionMapCustom(record['products'], config) : getProductSelectionMap(record['products'], config, isPvcm)
                        if (product.values().flatten()) {
                            config.productSelection = product as JSON
                            config.productGroupSelection = null
                        } else {
                            return [status: false, message: "Couldn't find product(s) with given name."]
                        }
                    } else {
                        return [status: false, message: "Can not update Products for Configs with multiple DataSources."]
                    }
                }
            }
            if (!masterConfiguration) {
                alertService.updateProdSelectionForDrugNumber([config])
            }
            config.isDatasheetChecked = false
            config.selectedDataSheet = ''
            Boolean isProductGroup = false
            if(record['datasheets'] && !TextUtils.isEmpty(record['datasheets']) && record['alert type'].trim() != "Individual Case Review" ) {
                String sheet = ''
                List importedDatasheet =  record['datasheets']?.split(',')
                List dataSheetNames = record['datasheets']?.toLowerCase()?.split(',')
                List finalDataSheets = []
                Map dataSheetMap = [:]
                String[] dataSheetArr = []
                List datasheetList =[]
                String productDictionarySelection = ''
                if (config.productGroupSelection) {
                    productDictionarySelection = config.productGroupSelection
                    isProductGroup = true
                } else
                    productDictionarySelection = config.productSelection
                if(config.selectedDatasource?.contains(Constants.DataSource.PVA)){
                    datasheetList = dataSheetService.fetchDataSheets(productDictionarySelection, Constants.DatasheetOptions.ALL_SHEET, isProductGroup, config.isMultiIngredient)
                }else{
                    datasheetList = dataSheetService.getAllActiveDatasheetsList("",Constants.DatasheetOptions.ALL_SHEET)
                }
                List validDatasheetList = datasheetList?.dispName*.toLowerCase()?.intersect(dataSheetNames*.trim()*.toLowerCase())
                List invalidDatasheet = []
                if (dataSheetNames?.size() > validDatasheetList?.size()) {
                    importedDatasheet?.each {
                        if(!(it?.trim()?.toLowerCase() in validDatasheetList)){
                            invalidDatasheet?.add(it)
                        }
                    }
                    return [status: false, message: "Import failed due to invalid datasheet(s) - ${invalidDatasheet?.join(',')}"]
                }else{
                    validDatasheetList?.each{ datasheet ->
                        Map dataSheet =  datasheetList?.find{ map ->
                            map.dispName?.toString()?.toLowerCase() == datasheet
                        }
                        sheet = dataSheet.dispName+"__"+dataSheet.id
                        if (sheet) {
                            finalDataSheets?.add(sheet)
                            sheet =''
                        }
                    }
                    if (!finalDataSheets?.empty) {
                        finalDataSheets.removeAll(['null'])
                        finalDataSheets?.each { dataSheet ->
                            dataSheetArr = dataSheet.split(Constants.DatasheetOptions.SEPARATOR)
                            dataSheetMap.put(dataSheetArr[1], dataSheetArr[0])
                        }
                        String finalDatasheetList = dataSheetMap as JSON
                        config.selectedDataSheet = finalDatasheetList
                        config.isDatasheetChecked = true
                        config.datasheetType = Constants.DatasheetOptions.ALL_SHEET
                    } else
                        return [status: false, message: "Couldn't find datasheet(s) with given name."]
                }
            }

            if (recordVersionAsOfDate) {
                String dataSource = config?.selectedDatasource
                if(dataSource == 'vaers' || dataSource == 'faers' || dataSource == 'vigibase'){
                    return [status: false, message: 'As of Version Date is not applicable for this DataSource.']
                }else{
                    try {
                        DateFormat df = new SimpleDateFormat(DateUtil.DEFAULT_DATE_FORMAT)
                        Date asOfversionDate = df.parse(recordVersionAsOfDate.trim())

                        config?.asOfVersionDate = asOfversionDate
                        config.evaluateDateAs=EvaluateCaseDateEnum.VERSION_ASOF


                    } catch (ParseException pe) {
                        // If it comes here, then its not a valid date of this format.
                        return [status: false, message: 'As of Version Date has Incorrect format.']
                    }
                }

            }
            if (record['assigned to']) {
                String assignedToValue
                User assignToUser = User.findByUsername(record['assigned to'].trim())
                Group assignToGroup = Group.findByNameAndGroupType(record['assigned to'].trim(), GroupType.USER_GROUP)
                if (assignToUser) {
                    assignedToValue = Constants.USER_TOKEN + assignToUser?.id
                } else if (assignToGroup) {
                    assignedToValue = Constants.USER_GROUP_TOKEN + assignToGroup?.id
                } else if (record['assigned to'].trim() == "Auto Assign") {
                    assignedToValue = "AUTO_ASSIGN"
                } else {
                    return [status: false, message: 'Assigned to user/ group does not exist']
                }
                Map productMap = [product: config.productSelection, productGroup: config.productGroupSelection]
                Map autoAssignStatus = [status: true]
                config = userService.assignGroupOrAssignTo(assignedToValue, config, productMap, user, autoAssignStatus)
                if(assignedToValue == "AUTO_ASSIGN" && autoAssignStatus.status == false) {
                    return [status: false, message: 'Auto assign user not mapped with product']
                }

            }
            if (record['assigned to'].trim() == "Auto Assign") {
                List<String> sharedWithList = []
                sharedWithList << ["AUTO_ASSIGN"]
                Map productMap = [:]
                userService.bindSharedWithConfiguration(config, sharedWithList, true, false, productMap, user)
            } else {
                if (record['share with']) {
                    List usernamesFromRecord = record['share with'].split(',')
                    List<String> sharedWithList = []
                    usernamesFromRecord.each {
                        User sharedWithUser = User.findByUsername(it.trim())
                        if (sharedWithUser) {
                            sharedWithList << [Constants.USER_TOKEN + sharedWithUser?.id]
                        }
                        Group shareWithGroup = Group.findByNameAndGroupType(it.trim(), GroupType.USER_GROUP)
                        if (shareWithGroup) {
                            sharedWithList << [Constants.USER_GROUP_TOKEN + shareWithGroup?.id]
                        }
                        if (it.trim() == "Auto Assign") {
                            sharedWithList << ["AUTO_ASSIGN"]
                        }
                    }
                    if(!sharedWithList) {
                        return [status: false, message: 'Share with user/group does not exist']
                    }
                    Map productMap = [:]
                    Map autoShareStatus = [status:true]
                    userService.bindSharedWithConfiguration(config, sharedWithList, true, false, productMap, user, false, autoShareStatus)
                    if(usernamesFromRecord.contains("Auto Assign") && autoShareStatus.status == false) {
                        return [status: false, message: 'Auto share user not mapped with product']
                    }
                }
            }

            Boolean runOnce = false
            if (recordScheduler && config.scheduleDateJSON != recordScheduler) {
                Map schedulerMap = new JsonSlurper().parseText(recordScheduler)
                if(schedulerMap){
                    Date startDate = DateUtil.parseDateWithTimeZone(schedulerMap.startDateTime,"yyyy-MM-dd'T'HH:mmXXX",schedulerMap?.timeZone?.name)
                    Date systemDate = new DateTime().withZone(DateTimeZone.forID(schedulerMap?.timeZone?.name)).toDate()
                    if(startDate < systemDate  && schedulerMap['recurrencePattern'] == 'FREQ=DAILY;INTERVAL=1;COUNT=1'){
                        return [status: false,message: "Start Date given as Past date in Scheduler."]
                    }else{
                        config.scheduleDateJSON = recordScheduler
                        Boolean repeatExecution = schedulerMap['recurrencePattern'] != 'FREQ=DAILY;INTERVAL=1;COUNT=1'
                        if (repeatExecution) {
                            config.setIsEnabled(true)
                            setNextRunDateAndScheduleDateJSON(config)
                            if(!TextUtils.isEmpty(config.scheduleDateJSON) && config.nextRunDate == null){
                                return [status: false,message: "Start Date given as Past date in Scheduler."]
                            }
                        } else {
                            config.setIsEnabled(false)
                            config.nextRunDate = null
                            runOnce = true
                        }
                    }

                }
            }

            if (recordDateRangeType) {
                List lastXList = ['LAST_X_DAYS', 'LAST_X_WEEKS', 'LAST_X_MONTHS', 'LAST_X_YEARS']
                if (recordDateRangeType == 'CUSTOM') {
                    if (recordStartDate && recordEndDate) {
                        String strDate = recordStartDate
                        String endDate = recordEndDate
                        DateFormat df = new SimpleDateFormat(DateUtil.DEFAULT_DATE_FORMAT)
                        try {
                            Date dateStart = df.parse(strDate);
                            Date dateEnd = df.parse(endDate);
                            config?.alertDateRangeInformation?.dateRangeEnum = recordDateRangeType
                            config?.alertDateRangeInformation?.relativeDateRangeValue = 1
                            config?.alertDateRangeInformation?.dateRangeStartAbsolute = dateStart
                            config?.alertDateRangeInformation?.dateRangeEndAbsolute = dateEnd

                        } catch (ParseException pe) {
                            // If it comes here, then its not a valid date of this format.
                            return [status: false, message: 'Start Date or End Date has Incorrect format.']
                        }
                    } else {
                        return [status: false, message: 'Start Date or End Date Missing.']
                    }

                } else if (recordDateRangeType in lastXList) {
                    if (recordLastX) {
                        String number = recordLastX
                        if (number.isInteger() && number != '0') {
                            config?.alertDateRangeInformation?.dateRangeEnum = recordDateRangeType
                            config?.alertDateRangeInformation?.relativeDateRangeValue = number as Integer
                            config?.alertDateRangeInformation?.dateRangeStartAbsolute = null
                            config?.alertDateRangeInformation?.dateRangeEndAbsolute = null
                        } else {
                            return [status: false, message: 'X field does not contain a valid number.']
                        }
                    } else {
                        return [status: false, message: 'X field missing ']
                    }
                } else {
                    config?.alertDateRangeInformation?.dateRangeEnum = recordDateRangeType
                    config?.alertDateRangeInformation?.relativeDateRangeValue = 1
                    config?.alertDateRangeInformation?.dateRangeStartAbsolute = null
                    config?.alertDateRangeInformation?.dateRangeEndAbsolute = null
                }
            } else if(template) {
                AlertDateRangeInformation alertDateRangeInformation = new AlertDateRangeInformation()
                alertDateRangeInformation.relativeDateRangeValue = template?.alertDateRangeInformation?.relativeDateRangeValue
                alertDateRangeInformation.dateRangeStartAbsolute = template?.alertDateRangeInformation?.dateRangeStartAbsolute
                alertDateRangeInformation.dateRangeEndAbsolute = template?.alertDateRangeInformation?.dateRangeEndAbsolute
                alertDateRangeInformation.dateRangeEnum = template?.alertDateRangeInformation?.dateRangeEnum
                config.alertDateRangeInformation = alertDateRangeInformation
            }
            // If aggregate template alert does not linked with active
            // product type configuration then fail child alert
            if(record['alert type'].trim() ==  "Aggregate Review" && template?.selectedDatasource?.contains('pva')){
                Boolean validProductIdFound = false

                template?.drugType?.split(',')?.each {
                    if(it.isInteger() && ProductTypeConfiguration.get(it)!=null){
                        validProductIdFound = true
                    }
                }
                if (!validProductIdFound) {
                    return [status: false, message: 'Please enter a value for Product Type']
                }
            }

            if(masterConfiguration && runOnce == false) {
                config.nextRunDate = masterConfiguration.nextRunDate
            }
            config.modifiedBy = user
//            CRUDService.updateWithAuditLog(config)
        } catch (Exception ex) {
            ex.printStackTrace()
            return [status: false, message: 'An Error occurred while saving Configuration.']
        }
        return [status: true, message: 'Successfully Updated Configuration.', configuration: config]

    }

    Map processFile(File fileToBeProcessed, String importAlertType = null) {
        log.info('Processing of the File started!');
        Map baseColumnTypeMap = Constants.importConfigSheet.BASE_COLUMN_MAP.clone() //Added for PVS-71197
        if (importAlertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            baseColumnTypeMap['unschedule'] = 'String'
        }
        Map masterColumnTypeMap = Constants.importConfigSheet.MASTER_COLUMN_MAP
        List<Map> alertConfigs = []
        List<Map> masterConfigs = []
        switch (FilenameUtils.getExtension(fileToBeProcessed.name)) {
            case 'xls':
            case 'xlsx':
            case 'xlx':
            case 'xlsm':
            case 'xlm':
                alertConfigs = processExcelFile(fileToBeProcessed, baseColumnTypeMap, 0)
                if (importAlertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT){
                    masterConfigs = processExcelFile(fileToBeProcessed, masterColumnTypeMap, 1)
                }
                break
            default:
                log.error("Not supported")
        }
        [alertConfigs: alertConfigs, masterConfigs: masterConfigs]
    }

    List<Map> processExcelFile(File file, Map<String, String> baseColumnTypeMap, Integer tabId=0) {
        InputStream is = new FileInputStream(file)
        Workbook workbook = WorkbookFactory.create(is);

        Sheet sheet = workbook.getSheetAt(tabId)
        List dataList = []
        Iterator<Row> rowIterator = sheet.rowIterator()
        rowIterator.next()
        rowIterator.each { row ->
            Cell cell
            Map map = [:]
            baseColumnTypeMap.eachWithIndex { String name, String type, index ->
                cell = row.getCell(index)
                map.put(name, cell ? cell.getStringCellValue().trim() : '')
            }
            if (Collections.frequency(map.values(), '') != map.size()) {
                dataList.add(map)
            }
        }
        workbook.close()
        dataList
    }


    byte[] generateImportConfigurationLogFile(Map processedRecordsStatus, User importedBy, String importConfType) {
        def locale = importedBy?.preference?.locale ?: Locale.ENGLISH

        Map metadata = [sheetName: "Configurations created",
                        columns  : [
                                [title: messageSource.getMessage('import.configuraton.label.alertName', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.label.alertType', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.label.owner', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.label.configuration.master', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.label.configuration.template', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.label.products', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.label.includeWHODrugs', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.label.multiIngredient', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.label.dataSheets', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.label.date.range.type', null, locale), width: 25],
                                [title: ' X ', width: 25],
                                [title: messageSource.getMessage('import.configuration.alert.start.date', null, locale), width: 25],
                                [title: messageSource.getMessage('import.cofiguration.alert.end.date', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.alert.versionAsOf.date', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.alert.assigned.to', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.alert.share.with', null, locale), width: 25],
                                [title: messageSource.getMessage('import.configuration.alert.scheduler', null, locale), width: 25],
                                [title: "Import Result", width: 25],
                                [title: "Info", width: 25]
                        ]]
        Map metaDataForUpdatedRecords = [sheetName: "Configurations updated",
                                         columns  : [
                                                 [title: messageSource.getMessage('import.configuraton.label.alertName', null, locale), width: 25],
                                                 [title: messageSource.getMessage('import.configuration.label.alertType', null, locale), width: 25],
                                                 [title: messageSource.getMessage('import.configuration.label.owner', null, locale), width: 25],
                                                 [title: messageSource.getMessage('import.configuration.label.configuration.master', null, locale), width: 25],
                                                 [title: messageSource.getMessage('import.configuration.label.configuration.template', null, locale), width: 25],
                                                 [title: messageSource.getMessage('import.configuration.label.products', null, locale), width: 25],
                                                 [title: messageSource.getMessage('next.run.date', null, locale), width: 25],
                                                 [title: "Info", width: 25]
                                         ]]
        byte[] file = dynamicReportService.createLogFileForImportConfiguration(processedRecordsStatus, metadata, metaDataForUpdatedRecords, importedBy, importConfType)
        return file
    }

    Map updateConfigurationFromTemplate(Map record, User user, MasterConfiguration masterConfiguration=null) {
        try {
            Locale locale = user?.preference?.locale ?: Locale.ENGLISH
            String recordConfigTemplate = masterConfiguration?.configTemplate?: record['configuration template']
            if (recordConfigTemplate) {
                Configuration template
                String selectedAlertType = getImportAlertType(record)
                List<Configuration> templateConfigs = getTemplateConfig(recordConfigTemplate.trim(), user, selectedAlertType)
                //Added changes for PVS-66680 FOR ALERT NAME SPECIAL CHARACTER VALIDATION
                String alertName = record[Constants.ImportConfiguration.ALERT_NAME]
                Object res = MiscUtil.validator(alertName, Constants.ImportConfiguration.NAME, Constants.SpecialCharacters.DEFAULT_CHARS as String[])

                if (res instanceof List) {
                    return [status: false, message: res[Constants.Commons.ONE]]
                }
                if (templateConfigs.size() > 1) {
                    if (record['owner'].trim()) {
                        User owner = User.findByUsername(record['owner'].trim())
                        if (owner) {
                            template = templateConfigs.find { it.owner == owner }
                            if (!template) {
                                return [status: false, message: "No templates exist for given owner, Owner is required- Multiple Template Alerts present with this name"]
                            }
                        } else {
                            return [status: false, message: "Owner with given name doesn't exist, Owner is required- Multiple Template Alerts present with this name"]
                        }
                    } else {
                        return [status: false, message: "Multiple Template Alerts present with this name, Owner is required"]
                    }
                } else if (templateConfigs.size() == 1) {
                    template = templateConfigs[0]
                }
                if (template) {
                    if (template.type == selectedAlertType) {
                        Configuration configuration
                        configuration = createAlertFromTemplate(template.type, template, user)
                        configuration.name = record['alert name'].trim()
                        configuration.configurationTemplateId = template?.id
                        // check for master config column, if exists pick configs from master
                        String recordProducts = record['products']
                        String recordDateRangeType = record['date range type']
                        String recordStartDate = record['start date']
                        String recordEndDate = record['end date']
                        String recordLastX = record['x']
                        String recordVersionAsOfDate = record['version as of date']
                        String recordScheduler = record['scheduler']
                        String recordMultiIngredient = record['multi-ingredient']
                        Boolean recordIncludeWHODrugs = false

                        if(masterConfiguration) {
                            recordDateRangeType = recordDateRangeType?:masterConfiguration.dateRangeType
                            recordStartDate = recordStartDate?:masterConfiguration.startDate?new SimpleDateFormat(DateUtil.DEFAULT_DATE_FORMAT).format(masterConfiguration.startDate):""
                            recordEndDate = recordEndDate?:masterConfiguration.endDate?new SimpleDateFormat(DateUtil.DEFAULT_DATE_FORMAT).format(masterConfiguration.endDate):""
                            recordLastX = recordLastX?:masterConfiguration.lastX
                            recordVersionAsOfDate = recordVersionAsOfDate?:masterConfiguration.asOfVersionDate
                            recordScheduler = recordScheduler?:masterConfiguration.scheduler
                        }

                        if (selectedAlertType in [Constants.AlertConfigType.SINGLE_CASE_ALERT, Constants.AlertConfigType.AGGREGATE_CASE_ALERT]) {
                            String includeWhoDrugs = record[Constants.importConfigSheet.INCLUDE_WHO_COLUMN]
                            String multiIngredient = record[Constants.importConfigSheet.MULTI_INGREDIENT_COLUMN]
                            if (includeWhoDrugs in [Constants.importConfigSheet.IMPORT_VALUE_ONE, Constants.importConfigSheet.IMPORT_VALUE_EMPTY]) {
                                if (includeWhoDrugs == Constants.importConfigSheet.IMPORT_VALUE_ONE && multiIngredient != Constants.importConfigSheet.IMPORT_VALUE_ONE) {
                                    return [status: false, message: messageSource.getMessage('app.import.configuration.message.multiIngredientIncorrect', null, locale)]
                                } else if (includeWhoDrugs == Constants.importConfigSheet.IMPORT_VALUE_EMPTY) {
                                    if (!multiIngredient in [Constants.importConfigSheet.IMPORT_VALUE_ONE, Constants.importConfigSheet.IMPORT_VALUE_EMPTY]) {
                                        return [status: false, message: messageSource.getMessage('app.import.configuration.message.multiIngredientIncorrect', null, locale)]
                                    }
                                }
                            } else {
                                return [status: false, message: messageSource.getMessage('app.import.configuration.message.includeWhoDrugsIncorrect', null, locale)]
                            }
                            if (masterConfiguration) {
                                configuration.includeWHODrugs = masterConfiguration.includeWHODrugs
                            } else {
                                configuration.includeWHODrugs = (includeWhoDrugs == Constants.importConfigSheet.IMPORT_VALUE_ONE)
                            }
                        }

                        if (masterConfiguration) {
                            configuration.isMultiIngredient = masterConfiguration.isMultiIngredient
                        } else {
                            configuration.isMultiIngredient = (recordMultiIngredient == Constants.importConfigSheet.IMPORT_VALUE_ONE)
                        }

                        if (recordProducts) {
                            String dataSource = configuration?.selectedDatasource
                            List productsListFromRecord = recordProducts?.trim().split(' , ')
                            List<String> productGroupsList = []
                            productsListFromRecord.collect { it ->
                                if (it?.trim().contains('(Product Group)')) {
                                    productGroupsList.add(it.replace('(Product Group)', '').trim())
                                }
                            }
                            if (productGroupsList) {
                                // product group update
                                List<?> productGroupMapList = []
                                log.info("Product Group(s) List: " + productGroupsList)
                                productGroupsList.each { productGroupName ->
                                    productGroupName = productGroupName.substring(0, productGroupName.lastIndexOf("(") ).trim()
                                    Map productGroup = pvsProductDictionaryService.fetchProductGroup(PVDictionaryConfig.PRODUCT_GRP_TYPE, productGroupName, dataSource, 1, 30, user?.username, true, configuration.isMultiIngredient)
                                    productGroupMapList.add(productGroup)
                                }
                                productGroupMapList.removeAll([null])
                                if (productGroupMapList) {
                                    //save Product Group
                                    configuration.productGroupSelection = productGroupMapList.collect { it ->
                                        [name: it.name, id: it.id, isMultiIngredient: configuration.isMultiIngredient, includeWHODrugs: configuration.includeWHODrugs]
                                    } as JSON
                                    configuration.productSelection = null
                                } else {
                                    return [status: false, message: "Couldn't find product Group(s) with given name for this DataSource and User."]
                                }
                            } else {
                                if (dataSource == 'pva' || dataSource == 'faers' || dataSource == 'eudra' || dataSource == 'vaers' || dataSource == 'vigibase') {
                                    Map product = [:]
                                    Boolean isPvcm = dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM)
                                    product = Holders.config.custom.caseInfoMap.Enabled ? getProductSelectionMapCustom(recordProducts, configuration) : getProductSelectionMap(recordProducts, configuration, isPvcm)
                                    if (product.values().flatten()) {
                                        configuration.productSelection = product as JSON
                                        configuration.productGroupSelection = null
                                    } else {
                                        return [status: false, message: "Couldn't find product(s) with given name."]
                                    }
                                } else {
                                    return [status: false, message: "Can not update Products for Configs with multiple DataSources."]
                                }
                            }
                        }
                        configuration.isDatasheetChecked = false
                        Boolean isProductGroup = false
                        if(record['datasheets'] && !TextUtils.isEmpty(record['datasheets'])  && record['alert type'].trim() != "Individual Case Review") {
                            String sheet = ''
                            List dataSheetNames = record['datasheets']?.toLowerCase()?.split(',')
                            List finalDataSheets = []
                            Map dataSheetMap = [:]
                            String[] dataSheetArr = []
                            List datasheetList = []
                            String productDictionarySelection = ''
                            if (configuration.productGroupSelection) {
                                productDictionarySelection = configuration.productGroupSelection
                                isProductGroup = true
                            } else
                                productDictionarySelection = configuration.productSelection
                            if(configuration.selectedDatasource?.contains(Constants.DataSource.PVA)){
                                datasheetList = dataSheetService.fetchDataSheets(productDictionarySelection, Constants.DatasheetOptions.ALL_SHEET, isProductGroup, configuration.isMultiIngredient)
                            }else{
                                datasheetList = dataSheetService.getAllActiveDatasheetsList("",Constants.DatasheetOptions.ALL_SHEET)
                            }
                            List validDatasheetList = datasheetList?.dispName*.toLowerCase()?.intersect(dataSheetNames*.trim()*.toLowerCase())
                            List importedDatasheet =  record['datasheets']?.split(',')
                            List invalidDatasheet = []
                            if (dataSheetNames?.size() > validDatasheetList?.size()) {
                                importedDatasheet?.each {
                                    if(!(it?.trim()?.toLowerCase() in validDatasheetList)){
                                        invalidDatasheet?.add(it);
                                    }
                                }
                                return [status: false, message: "Import failed due to invalid datasheet(s) - ${invalidDatasheet?.join(',')}"]
                            }else{
                                validDatasheetList?.each{ datasheet ->
                                    Map dataSheet =  datasheetList?.find{ map ->
                                        map.dispName?.toString()?.toLowerCase() == datasheet
                                    }
                                    sheet = dataSheet?.dispName+"__"+dataSheet.id
                                    if (sheet) {
                                        finalDataSheets?.add(sheet)
                                        sheet =''
                                    }
                                }
                                if (!finalDataSheets?.empty) {
                                    finalDataSheets.removeAll(['null'])
                                    finalDataSheets?.each { dataSheet ->
                                        dataSheetArr = dataSheet.split(Constants.DatasheetOptions.SEPARATOR)
                                        dataSheetMap.put(dataSheetArr[1], dataSheetArr[0])
                                    }
                                    String finalDatasheetList = dataSheetMap as JSON
                                    configuration.selectedDataSheet = finalDatasheetList
                                    configuration.isDatasheetChecked = true
                                    configuration.datasheetType = Constants.DatasheetOptions.ALL_SHEET
                                } else
                                    return [status: false, message: "Couldn't find datasheet(s) with given name."]
                            }
                        }
                        List lastXList = ['LAST_X_DAYS', 'LAST_X_WEEKS', 'LAST_X_MONTHS', 'LAST_X_YEARS']
                        if (recordDateRangeType && (configuration?.alertDateRangeInformation?.dateRangeEnum?.name() != recordDateRangeType ||
                                (recordDateRangeType == DateRangeEnum.CUSTOM.name() && recordStartDate && recordEndDate) ||
                                        (recordDateRangeType in lastXList && recordLastX))) {
                            recordDateRangeType = recordDateRangeType.toUpperCase() //Added for PVS-71144
                            if (recordDateRangeType == 'CUSTOM') {
                                if (recordStartDate && recordEndDate) {
                                    String strDate = recordStartDate
                                    String endDate = recordEndDate
                                    DateFormat df = new SimpleDateFormat(DateUtil.DEFAULT_DATE_FORMAT)
                                    try {
                                        Date dateStart = df.parse(strDate);
                                        Date dateEnd = df.parse(endDate);
                                        configuration?.alertDateRangeInformation?.dateRangeEnum = recordDateRangeType
                                        configuration?.alertDateRangeInformation?.relativeDateRangeValue = 1
                                        configuration?.alertDateRangeInformation?.dateRangeStartAbsolute = dateStart
                                        configuration?.alertDateRangeInformation?.dateRangeEndAbsolute = dateEnd

                                    } catch (ParseException pe) {
                                        // If it comes here, then its not a valid date of this format.
                                        return [status: false, message: 'Start Date or End Date has Incorrect format.']
                                    }
                                } else {
                                    return [status: false, message: 'Start Date or End Date Missing.']
                                }

                            } else if (recordDateRangeType in lastXList) {
                                if (recordLastX) {
                                    String number = recordLastX
                                    if (number.isInteger() && number != '0') {
                                        configuration?.alertDateRangeInformation?.dateRangeEnum = recordDateRangeType
                                        configuration?.alertDateRangeInformation?.relativeDateRangeValue = number as Integer
                                        configuration?.alertDateRangeInformation?.dateRangeStartAbsolute = null
                                        configuration?.alertDateRangeInformation?.dateRangeEndAbsolute = null
                                    } else {
                                        return [status: false, message: 'X field does not contain a valid number.']
                                    }
                                } else {
                                    return [status: false, message: 'X field missing ']
                                }
                            } else {
                                configuration?.alertDateRangeInformation?.dateRangeEnum = recordDateRangeType
                                configuration?.alertDateRangeInformation?.relativeDateRangeValue = 1
                                configuration?.alertDateRangeInformation?.dateRangeStartAbsolute = null
                                configuration?.alertDateRangeInformation?.dateRangeEndAbsolute = null
                            }
                        }

                        if (recordVersionAsOfDate) {
                            String dataSource = configuration?.selectedDatasource
                            if(dataSource == 'vaers' || dataSource == 'faers' || dataSource == 'vigibase'){
                                return [status: false, message: 'As of Version Date is not applicable for this DataSource.']
                            } else{
                                try {
                                    DateFormat df = new SimpleDateFormat(DateUtil.DEFAULT_DATE_FORMAT)
                                    Date asOfversionDate = df.parse(recordVersionAsOfDate.trim())
                                    configuration?.asOfVersionDate = asOfversionDate
                                    configuration.evaluateDateAs=EvaluateCaseDateEnum.VERSION_ASOF

                                } catch (ParseException pe) {
                                    // If it comes here, then its not a valid date of this format.
                                    return [status: false, message: 'As of Version Date has Incorrect format.']
                                }
                            }
                        }
                        if (record['assigned to']) {
                            String assignedToValue
                            User assignToUser = User.findByUsername(record['assigned to'].trim())
                            Group assignToGroup = Group.findByNameAndGroupType(record['assigned to'].trim(), GroupType.USER_GROUP)
                            if (assignToUser) {
                                assignedToValue = Constants.USER_TOKEN + assignToUser?.id
                            } else if (assignToGroup) {
                                assignedToValue = Constants.USER_GROUP_TOKEN + assignToGroup?.id
                            } else if (record['assigned to'].trim() == "Auto Assign") {
                                assignedToValue = "AUTO_ASSIGN"
                            } else {
                                return [status: false, message: 'Assigned to user/ group does not exist']
                            }
                            Map productMap = [product: configuration.productSelection, productGroup: configuration.productGroupSelection]
                            Map autoAssignStatus = [status: true]
                            configuration = userService.assignGroupOrAssignTo(assignedToValue, configuration, productMap, user, autoAssignStatus)
                            if(assignedToValue == "AUTO_ASSIGN" && autoAssignStatus.status == false) {
                                return [status: false, message: 'Auto assign user not mapped with product']
                            }
                        }
                        if (record['assigned to'].trim() == "Auto Assign") {
                            List<String> sharedWithList = []
                            sharedWithList << ["AUTO_ASSIGN"]
                            Map productMap = [:]
                            userService.bindSharedWithConfiguration(configuration, sharedWithList, true, false, productMap, user)
                        } else {
                            if (record['share with']) {
                                List usernamesFromRecord = record['share with'].split(',')
                                List<String> sharedWithList = []
                                usernamesFromRecord.each {
                                    User sharedWithUser = User.findByUsername(it.trim())
                                    if (sharedWithUser) {
                                        sharedWithList << [Constants.USER_TOKEN + sharedWithUser?.id]
                                    }
                                    Group shareWithGroup = Group.findByNameAndGroupType(it.trim(), GroupType.USER_GROUP)
                                    if (shareWithGroup) {
                                        sharedWithList << [Constants.USER_GROUP_TOKEN + shareWithGroup?.id]
                                    }
                                    if (it.trim() == "Auto Assign") {
                                        sharedWithList << ["AUTO_ASSIGN"]
                                    }
                                }
                                if(!sharedWithList) {
                                    return [status: false, message: 'Share with user/group does not exist']
                                }
                                Map productMap = [:]
                                Map autoShareStatus = [status:true]
                                userService.bindSharedWithConfiguration(configuration, sharedWithList, true, false, productMap, user, false, autoShareStatus)
                                if(usernamesFromRecord.contains("Auto Assign") && autoShareStatus.status == false) {
                                    return [status: false, message: 'Auto share user not mapped with product']
                                }
                            }
                        }
                        Boolean runOnce = false
                        if (recordScheduler && configuration.scheduleDateJSON != recordScheduler) {
                            Map schedulerMap = new JsonSlurper().parseText(recordScheduler)
                            if(schedulerMap){
                                Date startDate = DateUtil.parseDateWithTimeZone(schedulerMap.startDateTime,"yyyy-MM-dd'T'HH:mmXXX",schedulerMap?.timeZone?.name)
                                Date systemDate = new DateTime().withZone(DateTimeZone.forID(schedulerMap?.timeZone?.name)).toDate()
                                if(startDate < systemDate && schedulerMap['recurrencePattern'] == 'FREQ=DAILY;INTERVAL=1;COUNT=1'){
                                    return [status: false,message: "Start Date given as Past date in Scheduler."]
                                }else{
                                    configuration.scheduleDateJSON = recordScheduler
                                    Boolean repeatExecution = schedulerMap['recurrencePattern'] != 'FREQ=DAILY;INTERVAL=1;COUNT=1'
                                    if (repeatExecution) {
                                        configuration.setIsEnabled(true)
                                        setNextRunDateAndScheduleDateJSON(configuration)
                                        if(!TextUtils.isEmpty(configuration.scheduleDateJSON) && configuration.nextRunDate == null){
                                            return [status: false,message: "Start Date given as Past date in Scheduler."]
                                        }
                                    } else {
                                        configuration.setIsEnabled(false)
                                        configuration.nextRunDate = null
                                        runOnce = true
                                    }
                                }
                            }
                        }
                        configuration.owner = user
                        configuration.createdBy = user
                        configuration.modifiedBy = user
                        if(masterConfiguration) {
                            configuration.masterConfigId = masterConfiguration.id
                            configuration.isLatestMaster = true
                            configuration.numOfExecutions = 0
                            if(runOnce == false)
                                configuration.nextRunDate = masterConfiguration.nextRunDate
                        }
                        if(record['alert type'].trim() ==  "Aggregate Review" && template?.selectedDatasource?.contains('pva')){
                            Boolean validProductIdFound = false
                            template?.drugType?.split(',')?.each {
                                if(it.isInteger() && ProductTypeConfiguration.get(it)!=null){
                                    validProductIdFound = true
                                }
                            }
                            if (!validProductIdFound) {
                                return [status: false, message: 'Please enter a value for Product Type']
                            }
                        }
                        List<AuditTrailChild> auditChildList = []
                        if (!masterConfiguration) {
                            alertService.updateProdSelectionForDrugNumber([configuration])
                            CRUDService.saveWithAuditLog(configuration, auditChildList, true)
                        }
                        if (auditChildList) {
                            alertService.batchPersistForDomain(auditChildList, AuditTrailChild.class.name)
                        }
                        return [status: true, message: 'Configuration Created Successfully', configuration: configuration]

                    } else {
                        return [status: false, message: 'Alert type mismatch - Mentioned Configuration Template has type: ' + template.type]
                    }
                } else {
                    return [status: false, message: 'No Configuration Template exist with given name']
                }
            }

        } catch (Exception ex) {
            log.error("Error occured on saving config", ex)
            return [status: false, message: 'An Error occurred while saving Configuration.']
        }

    }

    def getProductSelectionMapPvcm(String productRecord, Configuration config) {

        Map productMap = ["Substance": [], "Product Name": [], "Product - Dosage Forms": [], "Trade Name": []]
        def product = ["1": [], "2": [], "3": [], "4": []]
        String dataSource = config?.selectedDatasource
        List productsListFromRecord = productRecord?.split(', ')
        productsListFromRecord.each {
            if (it?.trim().contains('(Substance)')) {
                productMap["Substance"] << it?.trim().replace(' (Substance)', '').toUpperCase()

            } else if (it?.trim().contains('(Product Name)')) {
                productMap["Product Name"] << it?.trim().replace(' (Product Name)', '').toUpperCase()

            } else if (it?.trim().contains('(Product - Dosage Forms)')) {
                productMap["Product - Dosage Forms"] << it?.trim().replace(' (Product - Dosage Forms)', '').toUpperCase()
            } else {
                productMap["Trade Name"] << it?.trim().replace(' (Trade Name)', '').toUpperCase()
            }
        }

        if (productMap["Substance"]) {
            String list= productMap["Substance"].join("','")
            product["1"] = LmProdDic200."${dataSource}".createCriteria().list() {
                sqlRestriction("upper(COL_2) in ('${list}')")
                eq('isMultiIngredient', config?.isMultiIngredient)
                projections {
                    distinct('viewId')
                    property('name')
                    property('isMultiIngredient')
                }
                maxResults(1)
            }.collect { [name: it[1], id: it[0], isMultiIngredient: it[2]] }
        }
        if (productMap["Product Name"]) {
            String list= productMap["Product Name"].join("','")
            product["2"] = LmProdDic201."${dataSource}".createCriteria().list() {
                sqlRestriction("upper(COL_2) in ('${list}')")
                projections {
                    distinct('viewId')
                    property('name')
                }
                maxResults(1)
            }.collect { [name: it[1], id: it[0]] }

        }
        if (productMap["Product - Dosage Forms"]) {
            String list= productMap["Product - Dosage Forms"].join("','")
            product["3"] = LmProdDic202."${dataSource}".createCriteria().list() {
                sqlRestriction("upper(COL_2) in ('${list}')")
                projections {
                    distinct('viewId')
                    property('name')
                }
                maxResults(1)
            }.collect { [name: it[1], id: it[0]] }

        }
        if (productMap["Trade Name"]) {
            String list= productMap["Trade Name"].join("','")
            product["4"] = LmProdDic203."${dataSource}".createCriteria().list() {
                sqlRestriction("upper(COL_2) in ('${list}')")
                projections {
                    distinct('viewId')
                    property('name')
                }
                maxResults(1)
            }.collect { [name: it[1], id: it[0]] }
        }

        return product
    }


    def getProductSelectionMap(String productRecord, Configuration config, Boolean isPvcm = false) {
        if (isPvcm){
            return getProductSelectionMapPvcm(productRecord, config)
        }
        Map productMap = ["Ingredient": [], "Family": [], "Product Name": [], "Trade Name": []]
        def product = ["1": [], "2": [], "3": [], "4": []]
        String dataSource = config?.selectedDatasource
        List productsListFromRecord = productRecord?.split(/(?<=\))\s*,/)
        productsListFromRecord.each {
            if (it?.trim().contains('(Ingredient)')) {
                productMap["Ingredient"] << it?.trim().replace(' (Ingredient)', '').replaceAll("'","''").toUpperCase()

            } else if (it?.trim().contains('(Family)')) {
                productMap["Family"] << it?.trim().replace(' (Family)', '').replaceAll("'","''").toUpperCase()

            } else if (it?.trim().contains('(Product Name)')) {
                productMap["Product Name"] << it?.trim().replace(' (Product Name)', '').replaceAll("'","''").toUpperCase()
            } else {
                productMap["Trade Name"] << it?.trim().replace(' (Trade Name)', '').replaceAll("'","''").toUpperCase()
            }
        }

        if (productMap["Ingredient"]) {
            String list= productMap["Ingredient"].join("','")
            product["1"] = LmProdDic200."${dataSource}".createCriteria().list() {
                sqlRestriction("upper(COL_2) in ('${list}')")
                eq('isMultiIngredient', config?.isMultiIngredient)
                projections {
                    distinct('viewId')
                    property('name')
                    property('isMultiIngredient')
                }
                maxResults(1)
            }.collect { [name: it[1], id: it[0], isMultiIngredient: it[2]] }
        }
        if (productMap["Family"]) {
            String list= productMap["Family"].join("','")
            product["2"] = LmProdDic201."${dataSource}".createCriteria().list() {
                sqlRestriction("upper(COL_2) in ('${list}')")
                projections {
                    distinct('viewId')
                    property('name')
                }
                maxResults(1)
            }.collect { [name: it[1], id: it[0]] }

        }
        if (productMap["Product Name"]) {
            String list= productMap["Product Name"].join("','")
            product["3"] = LmProdDic202."${dataSource}".createCriteria().list() {
                sqlRestriction("upper(COL_2) in ('${list}')")
                projections {
                    distinct('viewId')
                    property('name')
                }
                maxResults(1)
            }.collect { [name: it[1], id: it[0]] }

        }
        if (productMap["Trade Name"]) {
            String list= productMap["Trade Name"].join("','")
            product["4"] = LmProdDic203."${dataSource}".createCriteria().list() {
                sqlRestriction("upper(COL_2) in ('${list}')")
                projections {
                    distinct('viewId')
                    property('name')
                }
                maxResults(1)
            }.collect { [name: it[1], id: it[0]] }
        }
        return product

    }

    List<Configuration> getConfig(String configName, User user, String alertType) {
        User currentUser = user
        Long workflowGroupId = currentUser.workflowGroup.id
        List<Long> groupIds = currentUser.groups?.collect { it.id }
        List<Configuration> alertConfig = []
        Boolean hasRole = ["ROLE_ADMIN", "ROLE_DEV","ROLE_EXECUTE_SHARED_ALERTS"].any { currentUser.getAuthorities().authority?.contains(it) }
        Boolean isAggOrSingle = alertType in [Constants.AlertConfigType.SINGLE_CASE_ALERT, Constants.AlertConfigType.AGGREGATE_CASE_ALERT]

        Closure criteria = {
            createAlias("shareWithUser", "shareWithUser", JoinType.LEFT_OUTER_JOIN)
            createAlias("shareWithGroup", "shareWithGroup", JoinType.LEFT_OUTER_JOIN)
            if (isAggOrSingle) {
                createAlias("autoShareWithUser", "autoShareWithUser", JoinType.LEFT_OUTER_JOIN)
                createAlias("autoShareWithGroup", "autoShareWithGroup", JoinType.LEFT_OUTER_JOIN)
            }
            eq("isDeleted", false)
            eq("workflowGroup.id", workflowGroupId)
            eq("isCaseSeries", false)
            'or' {
                if (!hasRole) {
                    eq('owner.id', currentUser.id)
                }else{
                    'or' {
                        eq('owner.id', currentUser.id)
                        eq("shareWithUser.id", currentUser.id)
                        if (isAggOrSingle) {
                            eq("autoShareWithUser.id", currentUser.id)
                        }
                        if (groupIds) {
                            or {
                                groupIds.collate(1000).each {
                                    'in'("shareWithGroup.id", groupIds)
                                }
                            }
                            if (isAggOrSingle) {
                                or {
                                    groupIds.collate(1000).each {
                                        'in'("autoShareWithGroup.id", groupIds)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            eq("name", configName)
            eq("type", alertType)
        }

            try {
                alertConfig = Configuration.createCriteria().list(criteria).unique { it.id } as List<Configuration>
            } catch (Throwable th) {
                th.printStackTrace()
            }
            alertConfig
        }

    List<Configuration> getTemplateConfig(String templateName, User user, String alertType) {
        User currentUser = user
        Long workflowGroupId = currentUser.workflowGroup.id
        List<Long> groupIds = currentUser.groups?.collect { it.id }
        List<Configuration> alertConfig
        Boolean hasRole = ["ROLE_ADMIN", "ROLE_DEV","ROLE_EXECUTE_SHARED_ALERTS"].any { currentUser.getAuthorities().authority?.contains(it) }
        Boolean isAggOrSingle = alertType in [Constants.AlertConfigType.SINGLE_CASE_ALERT, Constants.AlertConfigType.AGGREGATE_CASE_ALERT]

        Closure criteria = {
            createAlias("shareWithUser", "shareWithUser", JoinType.LEFT_OUTER_JOIN)
            createAlias("shareWithGroup", "shareWithGroup", JoinType.LEFT_OUTER_JOIN)
            if (isAggOrSingle) {
                createAlias("autoShareWithUser", "autoShareWithUser", JoinType.LEFT_OUTER_JOIN)
                createAlias("autoShareWithGroup", "autoShareWithGroup", JoinType.LEFT_OUTER_JOIN)
            }
            eq("isDeleted", false)
            eq("isTemplateAlert", true)
            eq("workflowGroup.id", workflowGroupId)
            'or' {
                if (!hasRole) {
                    eq('owner.id', currentUser.id)
                }else{
                    'or' {
                        eq('owner.id', currentUser.id)
                        eq("shareWithUser.id", currentUser.id)
                        if (isAggOrSingle) {
                            eq("autoShareWithUser.id", currentUser.id)
                        }
                        if (groupIds) {
                            or {
                                groupIds.collate(1000).each {
                                    'in'("shareWithGroup.id", groupIds)
                                }
                            }
                            if (isAggOrSingle) {
                                or {
                                    groupIds.collate(1000).each {
                                        'in'("autoShareWithGroup.id", groupIds)
                                    }
                                }
                            }
                        }
                    }

                }
            }
            eq("name", templateName)
            eq("type", alertType)
        }

        alertConfig = Configuration.createCriteria().list(criteria).unique{it.id} as List<Configuration>


        alertConfig
    }

    def copyTemplateQuery(Configuration configuration,Configuration templateAlert, User user = null){
        if(!user){
            user =userService.getUser()
        }

        configuration.templateQueries.clear()

        List<TemplateQuery> templateQueries = templateAlert.templateQueries
        templateQueries.each{it->

            def bindingMap = [
                    template               : it.template,
                    templateName           : it.templateName,
                    query                  : it.query,
                    queryName              : it.queryName,
                    queryLevel             : it.queryLevel,
                    dynamicFormEntryDeleted: (it.dynamicFormEntryDeleted) ?: false,
                    header                 : (it.header) ?: null,
                    footer                 : (it.footer) ?: null,
                    title                  : (it.title) ?: null,
                    headerProductSelection : (it.headerProductSelection) ?: false,
                    headerDateRange        : (it.headerDateRange) ?: false,
                    blindProtected         : (it.blindProtected) ?: false,
                    privacyProtected       : (it.privacyProtected) ?: false
            ]
            TemplateQuery templateQueryInstance = new TemplateQuery(bindingMap)
            templateQueryInstance.createdBy=user.username
            templateQueryInstance.modifiedBy=user.username
            templateQueryInstance.dateRangeInformationForTemplateQuery.dateRangeEnum = null
            DateRangeInformation dateRangeInformationForTemplateQuery = templateQueryInstance.dateRangeInformationForTemplateQuery
            def dateRangeEnum = it.dateRangeInformationForTemplateQuery.dateRangeEnum
            dateRangeInformationForTemplateQuery.dateRangeEnum=dateRangeEnum

            if (dateRangeEnum == DateRangeEnum.CUSTOM.name()) {
                Locale locale = user?.preference?.locale
                dateRangeInformationForTemplateQuery.dateRangeStartAbsolute = DateUtil.getStartDate(it.dateRangeInformationForTemplateQuery.dateRangeStartAbsolute, locale)
                dateRangeInformationForTemplateQuery.dateRangeEndAbsolute = DateUtil.getEndDate(it.dateRangeInformationForTemplateQuery.dateRangeEndAbsolute, locale)
            } else {
                dateRangeInformationForTemplateQuery?.dateRangeStartAbsolute = null
                dateRangeInformationForTemplateQuery?.dateRangeEndAbsolute = null
            }
            dateRangeInformationForTemplateQuery.templateQuery = templateQueryInstance
            dateRangeInformationForTemplateQuery.relativeDateRangeValue = it.dateRangeInformationForTemplateQuery.relativeDateRangeValue
            assignParameterValuesToTemplateQuery( templateQueryInstance, it)
            configuration.addToTemplateQueries(templateQueryInstance)
        }

        configuration
    }


    private void assignParameterValuesToTemplateQuery( TemplateQuery templateQuery, TemplateQuery exisitinTemplateQuery) {
        def queryVLists=exisitinTemplateQuery.queryValueLists?.id
        def templateList=exisitinTemplateQuery.templateValueLists?.id
        queryVLists.each{
            QueryValueList queryValueList= QueryValueList.get(it)
            QueryValueList newQueryValueList =new QueryValueList()
            newQueryValueList.query=queryValueList.query
            newQueryValueList.queryName=queryValueList.queryName
            if(queryValueList.parameterValues){
                queryValueList.parameterValues.each{
                    def tempValue
                    def ParameterValuetype= it.getClass().getSimpleName()
                    if(ParameterValuetype=='QueryExpressionValue'){
                        tempValue = new QueryExpressionValue(key: it.key, value: it.value,
                                reportField: it.reportField,
                                operator: it.operator,
                                operatorValue: messageSource.getMessage("app.queryOperator.$it.operator", null, Locale.ENGLISH))
                    }else{
                        tempValue = new CustomSQLValue(key: it.key, value: it.value)
                    }
                    newQueryValueList.addToParameterValues(tempValue)
                }

            }
            templateQuery.addToQueryValueLists(queryValueList)
        }
        templateList.each {
            TemplateValueList templateValueList = TemplateValueList.get(it)
            TemplateValueList newTemplateValueList = new TemplateValueList()
            newTemplateValueList.template = templateValueList.template
            if(templateValueList.parameterValues){
                templateValueList.parameterValues.each {
                    ParameterValue tempValue
                    tempValue = new CustomSQLValue(key:it.key,
                            value: it.value)
                    newTemplateValueList.addToParameterValues(tempValue)
                }

            }


            templateQuery.addToTemplateValueLists(templateValueList)
        }
    }

    def getProductSelectionMapCustom(String productRecord, Configuration config) {
        Map productMap = [:]
        def product = [:]

        def customDicList = Holders.config.custom.dictionary.list
        customDicList.keySet().each {
            product.put(it, [])
            productMap.put(customDicList[it], [])
        }


        String dataSource = config?.selectedDatasource
        List productsListFromRecord = productRecord?.split(', ')

        productsListFromRecord.each {
            if (it?.trim().contains('(Therapeutic drug class)')) {
                productMap["Therapeutic drug class"] << it?.trim().replace(' (Therapeutic drug class)', '').toUpperCase()

            } else if (it?.trim().contains('(Active Moiety)')) {
                productMap["Active Moiety"] << it?.trim().replace(' (Active Moiety)', '').toUpperCase()

            } else if (it?.trim().contains('(Active Ingredient)')) {
                productMap["Active Ingredient"] << it?.trim().replace(' (Active Ingredient)', '').toUpperCase()
            } else if (it?.trim().contains('(Product Active Ingredient)')) {
                productMap["Product Active Ingredient"] << it?.trim().replace(' (Product Active Ingredient)', '').toUpperCase()
            } else if (it?.trim().contains('(Product Name)')) {
                productMap["Product Name"] << it?.trim().replace(' (Product Name)', '').toUpperCase()
            } else if (it?.trim().contains('(User Assignment)')) {
                productMap["User Assignment"] << it?.trim().replace(' (User Assignment)', '').toUpperCase()
            } else if (it?.trim().contains('(Application Number)')) {
                productMap["Application Number"] << it?.trim().replace(' (Application Number)', '').toUpperCase()
            } else if (it?.trim().contains('(Synonym)')) {
                productMap["Synonym"] << it?.trim().replace(' (Synonym)', '').toUpperCase()
            } else if (it?.trim().contains('(IND Reviewer)')) {
                productMap["IND Reviewer"] << it?.trim().replace(' (IND Reviewer)', '').toUpperCase()
            }
        }


        productMap.eachWithIndex { key, val, i ->
            if (productMap[key]) {

                def viewClass = null

                switch (i + 1) {
                    case "1":
                        viewClass = LmProdDic200
                        break
                    case "2":
                        viewClass = LmProdDic201
                        break
                    case "3":
                        viewClass = LmProdDic202
                        break
                    case "4":
                        viewClass = LmProdDic203
                        break
                    case "5":
                        viewClass = LmProdDic204
                        break
                    case "6":
                        viewClass = LmProdDic205
                        break
                    case "7":
                        viewClass = LmProdDic206
                        break
                    case "8":
                        viewClass = LmProdDic207
                        break
                    case "9":
                        viewClass = LmProdDic208
                        break
                }
                String list= productMap[key].join("','")
                product["${i + 1}"] = viewClass."${dataSource}".createCriteria().list() {
                    sqlRestriction("upper(COL_2) in ('${list}')")
                    projections {
                        distinct('viewId')
                        property('name')
                    }
                    maxResults(1)
                }.collect { [name: it[1], id: it[0]] }

            }
        }

        return product

    }

    void validateMasterConfigRecords(List masterRecords, List importedRecords, Locale locale) {
        // validate master config data
        List<String> uniqueMaster = []
        masterRecords.each {it ->
            String masterConfigName = it["master configuration"]?.toLowerCase()
            if(masterConfigName.length() > 255) {
                it.put("Fail", "Master Configuration length is more than 255")
            } else if(uniqueMaster.contains(masterConfigName)) {
                it.put("Fail", "Master Configuration is duplicate")
            } else { // code for include drug validation
                String errorMessage = ""
                errorMessage = checkForIncludeWHODrugs(it, locale) ?: checkForMultiIngredient(importedRecords, it)
                if(errorMessage){
                    it.put("Fail", errorMessage)
                }
            }
            uniqueMaster << masterConfigName
        }
    }

    MasterConfiguration createUpdateMasterConfigFromRecord(Map masterRecord, List masterError, User user) {
        // master config exists in 2nd tab
        String masterConfiguration = masterRecord['master configuration']

        // check duplicate master entries using masterRecords
        if(masterConfiguration.length() > 255 || masterRecord["Fail"] != null) {
            masterError << 'Fail'
            masterError << masterRecord["Fail"]
            return null
        }

        List<MasterConfiguration> masterConfigList = MasterConfiguration.createCriteria().list {
            eq('name',masterConfiguration,[ignoreCase: true])
        }
        MasterConfiguration masterConfig = null
        if(masterConfigList){
           masterConfig = masterConfigList.sort{it.id}[0]
        }
        List runningMasterIds = masterExecutorService.manageMasterExecutionQueue(true)
        if(masterConfig && runningMasterIds.contains(masterConfig?.id)) {
            masterError << 'Fail'
            masterError << "This master alert already executing."
            return null
        }

        // update master record and child record if already exists
        if(masterConfig){
            boolean isAddOn = false
            Date newRunDate = masterExecutorService.getMasterNextDate(masterRecord["scheduler"], null)
            if(masterConfig.nextRunDate && newRunDate.equals(masterConfig.nextRunDate)) {
                isAddOn = true
            } else if (masterConfig.nextRunDate && newRunDate.before(masterConfig.nextRunDate)){
                masterError << 'Fail'
                masterError << "The master configuration is already scheduled, the start date of the master cannot be earlier than the scheduled next run date."
                return null
            }
            if (masterConfig.configTemplate != masterRecord["configuration template"]) {
                masterError << 'Fail'
                masterError << "New configuration template can't be selected for existing master configuration."
                return null
            }
            if(!isAddOn) {
                DateFormat df = new SimpleDateFormat(DateUtil.DEFAULT_DATE_FORMAT)
                masterConfig.configTemplate = masterRecord["configuration template"]
                masterConfig.asOfVersionDate = masterRecord["version as of date"]
                masterConfig.dateRangeType = masterRecord["date range type"]
                masterConfig.startDate = masterRecord["start date"]? df.parse(masterRecord["start date"]):null
                masterConfig.endDate = masterRecord["end date"]? df.parse(masterRecord["end date"]):null
                masterConfig.lastX = masterRecord["x"] as Integer
                masterConfig.productHierarchy = masterRecord["products hierarchy"]
                masterConfig.isEnabled = true
                masterConfig.executing = false
                masterConfig.owner = user
                masterConfig.scheduler = masterRecord["scheduler"]
                masterConfig.isMultiIngredient = (masterRecord["multi-ingredient"] == Constants.importConfigSheet.IMPORT_VALUE_ONE)
                masterConfig.includeWHODrugs = (masterRecord["include who drugs"] == Constants.importConfigSheet.IMPORT_VALUE_ONE)
                Map schedulerMap = new JsonSlurper().parseText(masterConfig.scheduler)
                Boolean repeatExecution = schedulerMap['recurrencePattern'] != 'FREQ=DAILY;INTERVAL=1;COUNT=1'
                if (repeatExecution) {
                    masterConfig.setIsEnabled(true)
                    masterConfig.nextRunDate = null //resetting the next run for new scheduler info
                    masterConfig.nextRunDate = masterExecutorService.getMasterNextDate(masterConfig.scheduler, masterConfig.nextRunDate)
                    if(!TextUtils.isEmpty(masterConfig.scheduler) && masterConfig.nextRunDate == null){
                        masterError << 'Fail'
                        masterError << "Start Date given as Past date in Scheduler."
                        return null
                    }
                } else {
                    masterConfig.setIsEnabled(false)
                    masterConfig.nextRunDate = null
                }
                masterConfig.save()
            } else {
                if(masterConfig.configTemplate != masterRecord["configuration template"] ||
                        masterConfig.dateRangeType != masterRecord["date range type"] ||
                        masterConfig.lastX != masterRecord["x"] as Integer ||
                        masterConfig.productHierarchy != masterRecord["products hierarchy"]
                ) {
                    masterError << 'Fail'
                    masterError << "Master config details are not same."
                    return null
                }
            }
        } else {
            // create master record and child record if not exists
            masterConfig = createMasterConfig(masterRecord, user, masterError)
        }
        return masterConfig
    }

    MasterConfiguration createMasterConfig(Map linkedMasterConfig, User user, List masterError) {

        DateFormat df = new SimpleDateFormat(DateUtil.DEFAULT_DATE_FORMAT)
        MasterConfiguration masterConfiguration = new MasterConfiguration(
                name: linkedMasterConfig["master configuration"],
                productHierarchy: linkedMasterConfig["products hierarchy"],
                lastX: linkedMasterConfig["x"] as Integer,
                dateRangeType: linkedMasterConfig["date range type"] as String,
                asOfVersionDate: linkedMasterConfig["version as of date"]?:null,
                startDate: linkedMasterConfig["start date"]? df.parse(linkedMasterConfig["start date"]):null,
                endDate: linkedMasterConfig["end date"]?df.parse(linkedMasterConfig["end date"]):null,
                scheduler: linkedMasterConfig["scheduler"],
                configTemplate: linkedMasterConfig["configuration template"],
                isEnabled: true, executing: false, numOfExecutions: 0,
                isMultiIngredient: (linkedMasterConfig["multi-ingredient"] == Constants.importConfigSheet.IMPORT_VALUE_ONE),
                includeWHODrugs: (linkedMasterConfig["include who drugs"] == Constants.importConfigSheet.IMPORT_VALUE_ONE),
                owner: user
        )
        Map schedulerMap = new JsonSlurper().parseText(masterConfiguration.scheduler)
        Boolean repeatExecution = schedulerMap['recurrencePattern'] != 'FREQ=DAILY;INTERVAL=1;COUNT=1'
        if (repeatExecution) {
            masterConfiguration.setIsEnabled(true)
            masterConfiguration.nextRunDate = masterExecutorService.getMasterNextDate(masterConfiguration.scheduler, null)
            if(!TextUtils.isEmpty(masterConfiguration.scheduler) && masterConfiguration.nextRunDate == null){
                masterError << 'Fail'
                masterError << "Start Date given as Past date in Scheduler."
                return null
            }
        } else {
            masterConfiguration.setIsEnabled(false)
            masterConfiguration.nextRunDate = null
        }
        masterConfiguration.save()
    }

    void createChildConfigForMaster(Map record, MasterConfiguration masterConfiguration, List recordStatusCreated, User user, List childConfigs){
        String importAlertType = "Aggregate Case Alert"
        if(masterConfiguration.configTemplate) {
            List<Configuration> conf = getConfig(record['alert name']?.trim(), user, importAlertType)

            Boolean configExists = false
            Configuration config
            if (conf.size() > 1) {
                configExists = true
                if (record['owner'].trim()) {
                    User owner = User.findByUsername(record['owner'].trim())
                    if (owner) {
                        config = conf.find { it.owner == owner }
                        if (!config) {
                            configExists = false
                        }
                    } else {
                        recordStatusCreated << record.values() + ['Fail', "Owner with given name doesn't exist"]
                    }
                } else {
                    recordStatusCreated << record.values() + ['Fail', "Multiple alerts present with this name, Owner is required"]
                }
            } else if (conf.size() == 1) {
                config = conf[0]
                configExists = true
            }
            if(config) {
                // exists update from record

                Map recordStatusCreatedMap = updateConfigurationFromRecord(record, config, user, masterConfiguration)
                if (recordStatusCreatedMap?.status) {
                    if (recordStatusCreatedMap.configuration)
                        childConfigs << recordStatusCreatedMap.configuration
                    recordStatusCreated << record.values() + ['Success', recordStatusCreatedMap?.message]
                } else {
                    recordStatusCreated << record.values() + ['Fail', recordStatusCreatedMap?.message]
                }

            } else {
                // if configuraton template create/update config from template
                // to do update masterConfiguration
                Map recordStatusCreatedMap = updateConfigurationFromTemplate(record, user, masterConfiguration)

                if (recordStatusCreatedMap?.status) {
                    if (recordStatusCreatedMap.configuration)
                        childConfigs << recordStatusCreatedMap.configuration
                    recordStatusCreated << record.values() + ['Success', recordStatusCreatedMap?.message]
                } else {
                    recordStatusCreated << record.values() + ['Fail', recordStatusCreatedMap?.message]
                }
            }

        } else {
            // create/update config from record
            Map recordStatusCreatedMap = createUpdateConfigFromRecord(record, masterConfiguration, user)
            if (recordStatusCreatedMap?.status) {
                recordStatusCreated << record.values() + ['Success', recordStatusCreatedMap?.message]
            } else {
                recordStatusCreated << record.values() + ['Fail', recordStatusCreatedMap?.message]
            }

        }
    }

    Map createUpdateConfigFromRecord(Map record, MasterConfiguration masterConfiguration, User user) {
        String configName = record["alert name"]
        Configuration alertConfig = Configuration.findByNameAndMasterConfigId(configName, masterConfiguration.id)
        // check if config exists for master, update config
        if(alertConfig) {

        } else {
            // create config if not exists
            // create only when config template exists
            return [status: false, message: "Configuration Template is not available."]
        }
    }

    Configuration updateAlertFromTemplate(String alertType, Configuration alertTemplate, User user , Configuration configuration, User owner = null, boolean isRefreshConfig = false) {

        try {
            User configOwner = owner ? owner : (user ? user : userService.getUser())
            if (alertTemplate) {
                configuration.isProductMining = alertTemplate?.isProductMining
                configuration.description= alertTemplate?.description
                configuration.isPublic= alertTemplate?.isPublic
                configuration.isDeleted= alertTemplate?.isDeleted
                configuration.isEnabled= true
                configuration.isResume= false
                configuration.studySelection= alertTemplate?.studySelection
                configuration.configSelectedTimeZone= alertTemplate?.configSelectedTimeZone
                configuration.evaluateDateAs= alertTemplate?.evaluateDateAs
                configuration.productDictionarySelection= alertTemplate?.productDictionarySelection
                configuration.limitPrimaryPath= alertTemplate?.limitPrimaryPath
                configuration.includeMedicallyConfirmedCases= alertTemplate?.includeMedicallyConfirmedCases
                configuration.excludeFollowUp= alertTemplate?.excludeFollowUp
                configuration.includeLockedVersion= alertTemplate?.includeLockedVersion
                configuration.adjustPerScheduleFrequency= alertTemplate?.adjustPerScheduleFrequency
                configuration.isAutoTrigger= alertTemplate?.isAutoTrigger
                configuration.adhocRun= alertTemplate?.adhocRun
                configuration.workflowGroup= alertTemplate?.workflowGroup
                configuration.excludeNonValidCases= alertTemplate?.excludeNonValidCases
                configuration.groupBySmq= alertTemplate?.groupBySmq
                configuration.missedCases= alertTemplate?.missedCases
                configuration.blankValuesJSON= alertTemplate?.blankValuesJSON
                configuration.type= alertType == 'Aggregate Case Alert' ? Constants.AlertConfigType.AGGREGATE_CASE_ALERT : Constants.AlertConfigType.SINGLE_CASE_ALERT
                configuration.repeatExecution= alertTemplate?.repeatExecution
                configuration.drugType= alertTemplate?.drugType
                configuration.drugClassification= alertTemplate?.drugClassification
                configuration.referenceNumber= alertTemplate?.referenceNumber
                configuration.suspectProduct= alertTemplate?.suspectProduct
                configuration.applyAlertStopList= alertTemplate?.applyAlertStopList
                configuration.spotfireSettings= alertTemplate?.spotfireSettings
                configuration.alertRmpRemsRef= alertTemplate?.alertRmpRemsRef
                configuration.alertQueryName= alertTemplate?.alertQueryName
                configuration.alertTriggerCases= alertTemplate?.alertTriggerCases
                configuration.alertTriggerDays= alertTemplate?.alertTriggerDays
                configuration.priority= alertTemplate?.priority
                configuration.reviewPeriod= alertTemplate?.reviewPeriod
                //configuration.dispositions= alertTemplate?.dispositions
                configuration.alertQueryId= alertTemplate?.alertQueryId

                configuration.alertQueryValueLists?.each {
                    List<ParameterValue> parameterValuesList = it.parameterValues
                    it.parameterValues?.clear()

                    parameterValuesList?.each {
                        ParameterValue.get(it.id)?.delete()
                    }

                }

                configuration.alertQueryValueLists?.clear()


                alertTemplate?.alertQueryValueLists.each {
                    QueryValueList queryValueList = new QueryValueList(query: it.query, queryName: it.queryName)

                    it.parameterValues.each { pv ->

                        ParameterValue executedValue
                        if (pv.hasProperty('reportField')) {
                            ReportField rp = ReportField.get(pv.reportField?.id)
                            executedValue = new QueryExpressionValue(key: pv.key,
                                    reportField: rp, operator: pv.operator, value: pv.value,
                                    operatorValue: messageSource.getMessage("app.queryOperator.$pv.operator", null, Locale.ENGLISH))
                        } else {
                            executedValue = new CustomSQLValue(key: pv.key, value: pv.value)
                        }
                        queryValueList.addToParameterValues(executedValue)

                    }
                    configuration.addToAlertQueryValueLists(queryValueList)
                }


                configuration.alertForegroundQueryName= alertTemplate?.alertForegroundQueryName
                configuration.alertForegroundQueryId= alertTemplate?.alertForegroundQueryId
                configuration.alertForegroundQueryValueLists?.each {
                    List<ParameterValue> parameterValuesList = it.parameterValues
                    it.parameterValues?.clear()
                    parameterValuesList?.each {
                        ParameterValue.get(it.id)?.delete()
                    }
                }

                configuration.alertForegroundQueryValueLists?.clear()

                alertTemplate?.alertForegroundQueryValueLists.each {
                    QueryValueList queryValueList = new QueryValueList(query: it.query, queryName: it.queryName)
                    it.parameterValues.each { pv ->
                        ParameterValue executedValue
                        if (pv.hasProperty('reportField')) {
                            ReportField rp = ReportField.get(pv.reportField?.id)
                            executedValue = new QueryExpressionValue(key: pv.key,
                                    reportField: rp, operator: pv.operator, value: pv.value,
                                    operatorValue: messageSource.getMessage("app.queryOperator.$pv.operator", null, Locale.ENGLISH))
                        } else {
                            executedValue = new CustomSQLValue(key: pv.key, value: pv.value)
                        }
                        queryValueList.addToParameterValues(executedValue)
                    }
                    configuration.addToAlertForegroundQueryValueLists(queryValueList)
                }

                configuration.foregroundSearch= alertTemplate?.foregroundSearch
                configuration.foregroundSearchAttr= alertTemplate?.foregroundSearchAttr
                configuration.template= alertTemplate?.template
                configuration.integratedConfigurationId= alertTemplate?.integratedConfigurationId
                configuration.nextRunDate= null
                configuration.isTemplateAlert= false
                configuration.configurationTemplateId= alertTemplate?.id
                configuration.alertCaseSeriesId= alertTemplate?.alertCaseSeriesId
                configuration.alertCaseSeriesName= alertTemplate?.alertCaseSeriesName
                if (!isRefreshConfig) {
                    configuration.owner= configOwner
                    configuration.isAutoAssignedTo= alertTemplate?.isAutoAssignedTo
                    configuration.scheduleDateJSON = getDefaultScheduleJSON(user)
                    configuration.asOfVersionDateDelta= alertTemplate?.asOfVersionDateDelta
                    configuration.assignedTo= alertTemplate?.assignedTo
                    configuration.eventGroupSelection= alertTemplate?.eventGroupSelection
                    configuration.asOfVersionDate= alertTemplate?.asOfVersionDate
                    configuration.productGroupSelection= alertTemplate?.productGroupSelection
                    configuration.selectedDatasource= alertTemplate?.selectedDatasource
                    configuration.assignedToGroup= alertTemplate?.assignedToGroup
                    configuration.dateRangeType= alertTemplate?.dateRangeType
                    configuration.productSelection= alertTemplate?.productSelection
                    configuration.eventSelection= alertTemplate?.eventSelection
                }
                copyTemplateQuery(configuration,alertTemplate,user)
                return configuration

            }
        } catch (Throwable th) {
            th.printStackTrace()
        }
    }

    def failScheduledImportConfiguration() {
        // all import configs in scheduled state
        // should be in failed state since application is restarting
        ImportConfigurationLog.executeUpdate("Update ImportConfigurationLog set status = 'FAILED' where status in ('IN_READ', 'IN_PROCESS')")
    }

    def clearUploadDirectory() {
        // clearing upload directory if import config was running
        // on application restart
        File fileToDelete = null
        uploadFolder.eachFileRecurse(FileType.FILES) { file ->
            fileToDelete = file
        }
        if (fileToDelete!=null){
            fileToDelete.delete()
        }
    }

    /**
     * This method is responsible for refreshing the existing child that are linked with the uploaded master
     * @param masterRecords - This contains all uploaded master records
     * @param masterConfigs - This contains all the valid and created/updated master records
     * @param newChildConfigs - This contains newly added batch of child alerts
     * @param user
     */
    void refreshChildLinkedWithMaster(List masterRecords, Map masterConfigs, List newChildConfigs, User user, List recordStatusUpdated) {
        masterRecords.each { masterRecord ->
            MasterConfiguration masterConfiguration = masterConfigs.get(masterRecord["master configuration"]?.toLowerCase())?.masterConfig
            boolean isMasterRecordValid = masterConfiguration ? true : false
            if (isMasterRecordValid) {
                List<Configuration> existingChildAlerts
                existingChildAlerts = Configuration.createCriteria().list {
                    eq('masterConfigId', masterConfiguration.id)
                    eq('isEnabled', true)
                    eq('executing', false)
                    eq('isDeleted', false)
                    if (newChildConfigs) {
                        not {
                            'in'('id', newChildConfigs)
                        }
                    }
                    ne("isAutoPaused", true)
                    ne("isManuallyPaused", true)
                    or {
                        isNull("adjustmentTypeEnum")
                        not {
                            'in'('adjustmentTypeEnum', [AdjustmentTypeEnum.FAILED_ALERT_PER_SKIPPED_EXECUTION, AdjustmentTypeEnum.FAILED_SINGLE_ALERT_FOR_ALL_SKIPPED_EXECUTION])
                        }
                    }
                }
                if (existingChildAlerts) {
                    log.info("Refreshing the existing alert configuration linked with master: ${masterConfiguration.name}")
                    List<Configuration> templateConfigs = getTemplateConfig(masterConfiguration.configTemplate, user, Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
                    Configuration template
                    if (templateConfigs.size() > 1) {
                        template = templateConfigs.find { it.owner == user }
                    } else {
                        template = templateConfigs[0]
                    }
                    // updating the existing alert configurations as per the selected template of master
                    if (template) {
                        updateAlertFieldsFromTemplate(existingChildAlerts, masterConfiguration, user, template, recordStatusUpdated)
                    }
                }
                int allLinkedChildCount = Configuration.countByMasterConfigId(masterConfiguration.id)
                if (allLinkedChildCount < 1) {
                    // Unscheduling the master configuration as well since no child alert is present
                    log.info("Unscheduling the master configuration ${masterConfiguration.name} as well since no more child alert is present")
                    masterConfiguration.isEnabled = false
                    masterConfiguration.nextRunDate = null
                    masterConfiguration.save(flush: true)
                }
            }
        }
    }

    /**
     * This method is responsible for updating the alert fields on the basis of alert template
     * @param childAlertList - This contains the list of child alerts to be updated
     * @param masterConfiguration - This is the master alert configuration
     * @param user
     * @param template - This is the selected template of master alert
     */
    void updateAlertFieldsFromTemplate(List childAlertList, MasterConfiguration masterConfiguration, User user, Configuration template, List recordStatusUpdated) {
        childAlertList.each { configuration ->
            updateAlertFromTemplate(template.type, template, user, configuration, configuration.owner, true)
            configuration.isLatestMaster = true
            configuration.scheduleDateJSON = masterConfiguration.scheduler
            configuration.nextRunDate = masterConfiguration.nextRunDate
            configuration.modifiedBy = user
            String runDate = DateUtil.StringFromDate(configuration.nextRunDate, DateUtil.DATEPICKER_FORMAT_AM_PM_2.toString(), user.preference.timeZone) + userService.getGmtOffset(user.preference.timeZone)
            String selectedProduct = getProductSelectionWithType(configuration.productSelection, configuration.productGroupSelection).join(", ")
            recordStatusUpdated << [configuration.name, configuration.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT ? 'Aggregate Review': configuration.type, configuration.owner.username, masterConfiguration.name, masterConfiguration.configTemplate, selectedProduct, runDate, 'Configuration Updated Successfully']
        }
        alertService.batchPersistForDomain(childAlertList as List, Configuration.class.name)
    }

    void unscheduleAndDelinkChild(Map childRecord, User user, List recordStatusUpdated, List childConfigs, MasterConfiguration masterConfiguration) {
        List<Configuration> conf = getConfig(childRecord['alert name']?.trim(), user, Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
        List statusList = [childRecord['alert name']?.trim(), 'Aggregate Review', childRecord['owner']?.trim(), masterConfiguration.name, masterConfiguration.configTemplate, childRecord['products']?.trim(), null]
        Configuration config
        if (conf.size() > 1) {
            if (childRecord['owner'].trim()) {
                User owner = User.findByUsername(childRecord['owner'].trim())
                if (owner) {
                    config = conf.find { it.owner == owner }
                } else {
                    statusList += ["Owner with given name doesn't exist"]
                }
            } else {
                statusList += ["Multiple alerts present with this name, Owner is required"]
            }
        } else if (conf.size() == 1) {
            config = conf[0]
        }
        if (config) {
            config.isEnabled = false
            config.nextRunDate = null
            config.masterConfigId = null
            config.scheduleDateJSON = getDefaultScheduleJSON(user)
            statusList += ['Configuration Unscheduled Successfully']
            childConfigs << config
        } else {
            statusList += ["Alert does not exist with this name"]
        }
        recordStatusUpdated << statusList
    }

    List fetchShareWithUserConfig(Long id) {
        Sql sql = null
        List userList = []
        try {
            if (id) {
                sql = new Sql(dataSource)
                String query = "SELECT SHARE_WITH_USERID FROM SHARE_WITH_USER_CONFIG WHERE CONFIG_ID = ${id}"
                sql.rows(query).collect{
                    userList.add(it['SHARE_WITH_USERID'])
                }
            }
        } catch(Exception ex) {
            log.error("Error encountered while fetching share with user config.", ex)
        } finally {
            sql?.close()
        }
        return userList
    }

    String getImportAlertType(Map record) {
        record['alert type'].trim() == Constants.AlertConfigType.INDIVIDUAL_CASE_REVIEW ? Constants.AlertConfigType.SINGLE_CASE_ALERT : Constants.AlertConfigType.AGGREGATE_CASE_ALERT
    }
}
