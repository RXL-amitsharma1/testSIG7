package com.rxlogix

import com.rxlogix.attachments.Attachment
import com.rxlogix.config.*
import com.rxlogix.controllers.AlertController
import com.rxlogix.dto.AlertDataDTO
import com.rxlogix.dto.AlertLevelDispositionDTO
import com.rxlogix.dto.DashboardCountDTO
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.enums.*
import com.rxlogix.signal.*
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.*
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import grails.validation.ValidationException
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.sql.Sql
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import org.apache.commons.lang3.time.DateUtils
import org.apache.http.util.TextUtils
import com.fasterxml.jackson.databind.ObjectMapper
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import javax.servlet.http.HttpSession
import java.sql.Clob
import java.sql.ResultSet
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.concurrent.*

import static org.springframework.http.HttpStatus.*
import org.springframework.security.core.context.SecurityContextHolder

@Secured(["isAuthenticated()"])
class AggregateCaseAlertController implements AlertController, AlertAsyncUtil {

    def configurationService
    def aggregateCaseAlertService
    def emailService
    def SpringSecurityService
    def CRUDService
    def reportExecutorService
    def jaderExecutorService
    def activityService
    def queryService
    def dynamicReportService
    def productEventHistoryService
    def productBasedSecurityService
    def validatedSignalService
    def emergingIssueService
    def specialPEService
    def medicalConceptsService
    def actionTemplateService
    def userService
    def attachmentableService
    def evdasAlertService
    def viewInstanceService
    def dataSource_pva
    def cacheService
    def reportIntegrationService
    def productGroupService
    def workflowRuleService
    def priorityService
    def safetyLeadSecurityService
    def alertService
    def reportFieldService
    def dispositionService
    def spotfireService
    def actionService
    def alertCommentService
    def alertTagService
    EmailNotificationService emailNotificationService
    def pvsGlobalTagService
    AdvancedFilterService advancedFilterService
    def dataSource_eudra
    def dataSource_faers
    def dataSource_jader
    def dataSource_vaers
    def dataSource_vigibase
    def dataObjectService
    def signalExecutorService
    def dataSheetService
    def signalAuditLogService
    def alertFieldService
    def jaderAlertService
    ImportConfigurationService importConfigurationService
    public final static String JSON_DATE = "yyyy-MM-dd'T'HH:mmXXX"
    public final static List <String> eventHierarchy = ["SOC", "HLGT","HLT","PT","LLT","Synonyms","SMQ Broad","SMQ Narrow","EVENT_GROUP", "SMQ"]


    @Secured(['ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def create() {
        Configuration configurationInstance = new Configuration(type: Constants.AlertConfigType.AGGREGATE_CASE_ALERT, adhocRun: true, excludeFollowUp: true,applyAlertStopList: false)
        String action = Constants.AlertActions.CREATE
        Map model = modelData(configurationInstance, action)
        model << [productTypeMap : fetchProductTypeForDataSources()]
        model << [clone:false, dataSheetList:params.dataSheetList?:'', isPVCM: dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM), productHierarchy: ViewHelper.getHiearchyValues(), eventHierarchy: eventHierarchy]
        render(view: "create", model: model)
    }

    def list() {
        response.status = 200
        List<Map> acaList = aggregateCaseAlertService.list().collect { AggregateCaseAlert aca ->
            [
                    id          : aca.id,
                    alertName   : aca.alertConfiguration.name,
                    priority    : aca.priority?.value,
                    description : aca.alertConfiguration.description,
                    assignedTo  : aca.assignedTo ? aca.assignedTo?.fullName : aca.assignedToGroup?.name,
                    detectedDate: aca.detectedDate,
                    dueDate     : aca.dueDate,
                    disposition : aca.disposition?.value,
                    type        : 'test'
            ]
        }
        respond acaList, [formats: ['json']]
    }

    @Secured(['ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def view(Configuration configuration) {
        if (!configuration) {
            notFound()
            return
        }

        List<TemplateQuery> templateQueries = configuration.templateQueries
        User currentUser = userService.getUser()
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.createCriteria().get {
            eq('name',configuration.name)
            'owner'{
                eq("id",configuration.owner.id)
            }
            eq("isDeleted",false)
            maxResults(1)
            order("id","desc")
        }

        List<Map> selectDataSourceMap = []
        dataSourceMap.each {
            if (it.key != Constants.DataSource.EUDRA)
                selectDataSourceMap.add([name: it.key, display: it.value])
        }

        List activeDataSheetsTexts = []
        String enabledSheet = Constants.DatasheetOptions.CORE_SHEET

        if(!TextUtils.isEmpty(configuration?.selectedDataSheet)){
            Map activeDataSheets = alertService.getOnlyActiveDataSheets(configuration, false)
            activeDataSheetsTexts = dataSheetService.formatActiveDatasheetMap(activeDataSheets).text
        }
        if(!activeDataSheetsTexts.size()) {
            Boolean isProductGroup = !TextUtils.isEmpty(configuration.productGroupSelection)
            String productSelection = configuration.productGroupSelection ?: configuration.productSelection
            activeDataSheetsTexts = dataSheetService.fetchDataSheets(productSelection, enabledSheet, isProductGroup, configuration.isMultiIngredient)?.dispName
        }

        String productRuleNames = ""
        def keyToNameMap = [
                'DRUG_SUSPECT_FAERS': 'Drug(F)-S',
                'DRUG_SUSPECT_CONCOMITANT_FAERS': 'Drug(F)-S+C',
                'VACCINE_SUSPECT_VAERS': 'Vaccine(VA)-S',
                'DRUG_SUSPECT_VIGIBASE': 'Drug(VB)-S',
                'DRUG_SUSPECT_CONCOMITANT_VIGIBASE': 'Drug(VB)-S+C',
                'VACCINE_SUSPECT_VIGIBASE': 'Vaccine(VB)-S',
                'SUSPECT' : 'Suspect',
                'SUSPECT_AND_CONCOMITANT' : 'Suspect and Concomitant',
                'VACCINE' : 'Vaccine',
                'DRUG_SUSPECT_JADER': 'Drug(J)-S',
                'DRUG_SUSPECT_CONCOMITANT_JADER': 'Drug(J)-S+C',
        ]
        configuration.drugType.split(',').each {
            if(it.isInteger()){
                productRuleNames+=(ProductTypeConfiguration.get(it) ? (ProductTypeConfiguration.get(it)?.name+",") : "")
            }else{
                productRuleNames+=(keyToNameMap[it]+",")
            }
        }
        if(productRuleNames && productRuleNames.size() > 1) {
            productRuleNames = productRuleNames.substring(0, productRuleNames.size() - 1)
        }
        Boolean isEdit = configuration.isMasterTemplateAlert ? SecurityContextHolder.context.authentication.authorities*.authority.contains('ROLE_DATA_MINING_ADVANCED_RUNS') : (SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN") || userService.getCurrentUserId() == configuration.owner.id)
        render(view: "view", model: [configurationInstance: configuration, templateQueries: templateQueries,selectDataSourceMap : selectDataSourceMap,productRuleNames:productRuleNames,
                                     currentUser          : currentUser, isExecuted: false,dataSource : getDataSource(configuration?.selectedDatasource),
                                     viewSql              : params.getBoolean("viewSql") ? reportExecutorService.debugReportSQL(configuration, executedConfiguration, true) : null,
                                     isEdit               : isEdit,
                                     masterConfig: configuration.masterConfigId? MasterConfiguration.findById(configuration.masterConfigId)?.name :"",
                                     selectedDatasheets    : activeDataSheetsTexts?.join(',')?:'', isPVCM: dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM),
                                     productHierarchy: ViewHelper.getHiearchyValues(), eventHierarchy: eventHierarchy
        ])
    }

    @Secured(['ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def viewExecutedConfig(String id) {
        Long decodedId = configurationService.decodeId(id)
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(decodedId)
        if (!executedConfiguration) {
            notFound()
            return
        }
        User currentUser = userService.getUser()
        String faersDateRange = executedConfiguration?.faersDateRange
        String vaersDateRange = executedConfiguration?.vaersDateRange
        String vigibaseDateRange = executedConfiguration?.vigibaseDateRange
        Map stratificationMap = [:]
        if (executedConfiguration.dataMiningVariable && executedConfiguration.dataMiningVariable != "null" && executedConfiguration.adhocRun) {
            if (executedConfiguration.stratificationColumnsDataMining) {
                JsonSlurper jsonSlurper = new JsonSlurper()
                stratificationMap = jsonSlurper.parseText(executedConfiguration.stratificationColumnsDataMining)
            } else {
                stratificationMap = aggregateCaseAlertService.getStratificationValuesDataMiningVariables(executedConfiguration.selectedDatasource, executedConfiguration.dataMiningVariable)
            }
        } else {
            if (executedConfiguration.stratificationColumns) {
                JsonSlurper jsonSlurper = new JsonSlurper()
                stratificationMap = jsonSlurper.parseText(executedConfiguration.stratificationColumns)
            } else {
                stratificationMap = aggregateCaseAlertService.getStratificationValues(executedConfiguration.selectedDatasource)
            }
        }
        if(executedConfiguration.executedAlertDateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE && executedConfiguration?.selectedDatasource.contains(Constants.DataSource.FAERS)){
            faersDateRange = DateUtil.toDateString(executedConfiguration.executedAlertDateRangeInformation.dateRangeStartAbsolute) +
                    " - " + (reportExecutorService.getFaersDateRange().faersDate).substring(13)
        } else if (executedConfiguration.executedAlertDateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE && executedConfiguration?.selectedDatasource.contains(Constants.DataSource.VAERS)) {
            //Updated Vaers Date Range for Integrated Alert PVS-67898
            vaersDateRange = DateUtil.toDateString(executedConfiguration.executedAlertDateRangeInformation.dateRangeStartAbsolute) +
                    " - " + (reportExecutorService.getVaersDateRange().vaersDate).substring(Constants.Commons.VAERS_END_DATE_INDEX)
        }
        if (executedConfiguration.executedAlertDateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE && executedConfiguration?.selectedDatasource.contains(Constants.DataSource.VIGIBASE)){
            vigibaseDateRange = DateUtil.toDateString(executedConfiguration.executedAlertDateRangeInformation.dateRangeStartAbsolute) +
                    " - " + (reportExecutorService.getVigibaseDateRange().vigibaseDate).substring(13)
        }
        List selectedDatasheets = []
            if(executedConfiguration?.selectedDataSheet){
                selectedDatasheets = dataSheetService.formatDatasheetMap(executedConfiguration)?.text
            }else if(TextUtils.isEmpty(executedConfiguration?.selectedDataSheet) && executedConfiguration?.selectedDatasource?.contains(Constants.DataSource.PVA)){
                if(!TextUtils.isEmpty(executedConfiguration.productGroupSelection)  || !TextUtils.isEmpty(executedConfiguration.productSelection)){
                    Boolean isProductGroup = !TextUtils.isEmpty(executedConfiguration.productGroupSelection)
                    String enabledSheet = Constants.DatasheetOptions.CORE_SHEET
                    selectedDatasheets =  dataSheetService.fetchDataSheets(executedConfiguration.productGroupSelection?:executedConfiguration.productSelection,enabledSheet,isProductGroup, executedConfiguration.isMultiIngredient)?.dispName
                }
            }


        render(view: "view", model: [configurationInstance: executedConfiguration, templateQueries: executedConfiguration.executedTemplateQueries, currentUser: currentUser, isExecuted: true, dataSource: getDataSource(executedConfiguration.selectedDatasource),productRuleNames: executedConfiguration.drugTypeName,
                                     isEBGM     : stratificationMap.map_EBGM.pva.isEBGM, stratificationMapEBGM: stratificationMap.map_EBGM, isFaersEBGM: stratificationMap.map_EBGM.faers.isEBGM, isVaersEBGM: stratificationMap.map_EBGM?.vaers?.isEBGM,
                                     isPRR      : stratificationMap.map_PRR.pva.isPRR, stratificationMapPRR: stratificationMap.map_PRR, isFaersPRR: stratificationMap.map_PRR.faers?.isPRR, isVaersPRR: stratificationMap.map_PRR?.vaers?.isPRR,faersDateRange: faersDateRange,vaersDateRange: vaersDateRange,
                                     isVigibaseEBGM: stratificationMap.map_EBGM?.vigibase?.isEBGM, isVigibasePRR: stratificationMap.map_PRR?.vigibase?.isPRR, vigibaseDateRange: vigibaseDateRange,
                                     isJaderEBGM: stratificationMap.map_EBGM?.jader?.isEBGM, isJaderPRR: stratificationMap.map_PRR?.jader?.isPRR,
                                     masterConfig: executedConfiguration.masterExConfigId? MasterExecutedConfiguration.findById(executedConfiguration.masterExConfigId)?.name :"",selectedDatasheets:selectedDatasheets?.join(',')?:"", isPVCM: dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM)
        ])
    }

    @Secured(['ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def copy(Configuration originalConfig) {
        String startDateAbsoluteCustomFreq = ""
        String endDateAbsoluteCustomFreq = ""
        if (!originalConfig) {
            notFound()
            return
        }
        String action = Constants.AlertActions.COPY
        String exeVaersDateRange
        String exeVigibaseDateRange
        Map model = modelData(originalConfig, action)
        model << [clone:true, currentUser: userService.getUser()]
        model << [productTypeMap : fetchProductTypeForDataSources()]
        if (originalConfig?.alertDateRangeInformation?.dateRangeEnum == DateRangeEnum.CUSTOM) {
            startDateAbsoluteCustomFreq = DateUtil.toDateString1(originalConfig?.alertDateRangeInformation?.dateRangeStartAbsolute)
            endDateAbsoluteCustomFreq = DateUtil.toDateString1(originalConfig?.alertDateRangeInformation?.dateRangeEndAbsolute)
            if (Holders.config.signal.vaers.enabled) {
                //Updated Vaers Date Range for Integrated Alert PVS-67898
                exeVaersDateRange = reportExecutorService.getVaersDateRange().vaersDate
            }
            exeVigibaseDateRange = reportExecutorService.getVigibaseDateRange().vigibaseDate
        }
        if(originalConfig.isMasterTemplateAlert){
            JsonSlurper jsonSlurper = new JsonSlurper()
            if(originalConfig.productHierarchyMasterTmplt){
                model << [configurationInstancePrdHierarchy: jsonSlurper.parseText(originalConfig.productHierarchyMasterTmplt).values().first()]
            }
            if(originalConfig.eventHierarchyMasterTmplt){
                model << [configurationInstanceEventHierarchy: jsonSlurper.parseText(originalConfig.eventHierarchyMasterTmplt).values().first()]
            }
        }
        model << [startDateAbsoluteCustomFreq: startDateAbsoluteCustomFreq,
                  endDateAbsoluteCustomFreq  : endDateAbsoluteCustomFreq, exeVaersDateRange: exeVaersDateRange,
                  exeVigibaseDateRange: exeVigibaseDateRange, dataSheetList :originalConfig?.selectedDataSheet?dataSheetService.formatDatasheetMap(originalConfig) as JSON :'',
                  isPVCM: dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM),
                  productHierarchy: ViewHelper.getHiearchyValues(), eventHierarchy: eventHierarchy
        ]
        render(view: "create", model: model)
    }

    @Secured(['ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def delete(Configuration config) {

        if (!config) {
            notFound()
            return
        }
        User currentUser = userService.getUser()
        if (config.isEditableBy(currentUser)) {
            def deletedConfig = configurationService.deleteConfig(config)
            if (deletedConfig.hasErrors()) {
                chain(action: "index", model: [error: deletedConfig])
            }
            flash.message = message(code: "app.configuration.delete.success", args: [config.name])
        } else {
            flash.warn = message(code: "app.configuration.delete.fail", args: [config.name])
        }
        redirect(controller: "configuration", action: "index")
    }

    @Secured(['ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def edit(Configuration configurationInstance) {

        if (!configurationInstance) {
            notFound()
            return
        }

        String startDateAbsoluteCustomFreq = ""
        String endDateAbsoluteCustomFreq = ""
        def firstExecutionDate = '-'
        def lastExecutionDate = '-'
        def configNextRunDate = configurationInstance.nextRunDate
        User currentUser = userService.getUser()
        String timeZone = currentUser.preference.timeZone
        def executedConfiguration = ExecutedConfiguration.findAllByConfigId(configurationInstance.id).sort {it.id}
        String exeEvdasDateRange
        String exeFaersDateRange
        String exeVaersDateRange
        String exeVigibaseDateRange

        try {
            if (executedConfiguration) {
                def length = executedConfiguration.size()
                def firstExecutionObject = executedConfiguration[0]
                def lastExecutionObject = executedConfiguration[length - 1]
                exeEvdasDateRange = lastExecutionObject.evdasDateRange
                exeFaersDateRange = lastExecutionObject.faersDateRange
                exeVaersDateRange = lastExecutionObject.vaersDateRange
                exeVigibaseDateRange = lastExecutionObject.vigibaseDateRange
                def fExecutionStartDate = firstExecutionObject.executedAlertDateRangeInformation.dateRangeStartAbsolute
                def fExecutionEndDate = firstExecutionObject.executedAlertDateRangeInformation.dateRangeEndAbsolute
                def lExecutionStartDate = lastExecutionObject.executedAlertDateRangeInformation.dateRangeStartAbsolute
                def lExecutionEndDate = lastExecutionObject.executedAlertDateRangeInformation.dateRangeEndAbsolute
                def firstExecutionStartDate = DateUtil.toDateStringWithoutTimezone(fExecutionStartDate)
                def firstExecutionEndDate = DateUtil.toDateStringWithoutTimezone(fExecutionEndDate)
                def lastExecutionStartDate = DateUtil.toDateStringWithoutTimezone(lExecutionStartDate)
                def lastExecutionEndDate = DateUtil.toDateStringWithoutTimezone(lExecutionEndDate)
                firstExecutionDate = firstExecutionStartDate + Constants.Commons.SPACE_HYPHEN_SPACE + firstExecutionEndDate
                lastExecutionDate = lastExecutionStartDate + Constants.Commons.SPACE_HYPHEN_SPACE + lastExecutionEndDate
            }
        } catch (Throwable th) {
            flash.warn = message(code: "app.alert.view")
            log.error(th.getMessage(), th)
        }

        if (reportExecutorService.currentlyQuantRunning.contains(configurationInstance.id)) {
            flash.warn = message(code: "app.configuration.running.fail", args: [configurationInstance.name])
            redirect(controller: "configuration", action: "index")
        }
        if (!(configurationInstance?.isEditableBy(currentUser) ||
                (SpringSecurityUtils.ifAllGranted("ROLE_AGGREGATE_CASE_CONFIGURATION, ROLE_EXECUTE_SHARED_ALERTS")) && !alertService.ifReadOnlyUserForConfig(currentUser,configurationInstance))) {
            flash.warn = message(code: "app.configuration.edit.permission", args: [configurationInstance.name])
            redirect(controller: "configuration", action: "index")
        } else {
            String action = Constants.AlertActions.EDIT
            if (configurationInstance?.alertDateRangeInformation?.dateRangeEnum == DateRangeEnum.CUSTOM) {
                startDateAbsoluteCustomFreq = DateUtil.toDateString1(configurationInstance?.alertDateRangeInformation?.dateRangeStartAbsolute)
                endDateAbsoluteCustomFreq = DateUtil.toDateString1(configurationInstance?.alertDateRangeInformation?.dateRangeEndAbsolute)
            }
            Map activeDataSheets = alertService.getOnlyActiveDataSheets(configurationInstance,false)
            boolean isReadOnlyUser = alertService.ifReadOnlyUserForConfig(currentUser,configurationInstance)
            Map model = modelData(configurationInstance, action)
            if(configurationInstance.isMasterTemplateAlert){
                JsonSlurper jsonSlurper = new JsonSlurper()
                if(configurationInstance.productHierarchyMasterTmplt){
                    model << [configurationInstancePrdHierarchy: jsonSlurper.parseText(configurationInstance.productHierarchyMasterTmplt).values().first()]
                }
                if(configurationInstance.eventHierarchyMasterTmplt){
                    model << [configurationInstanceEventHierarchy: jsonSlurper.parseText(configurationInstance.eventHierarchyMasterTmplt).values().first()]
                }
            }
            model << [startDateAbsoluteCustomFreq: startDateAbsoluteCustomFreq, endDateAbsoluteCustomFreq: endDateAbsoluteCustomFreq,
                      configSelectedTimeZone     : params?.configSelectedTimeZone,
                      configNextRunDate          : configNextRunDate, firstExecutionDate: firstExecutionDate, lastExecutionDate: lastExecutionDate,
                      exeEvdasDateRange : exeEvdasDateRange, exeFaersDateRange : exeFaersDateRange,exeVaersDateRange: exeVaersDateRange,
                      exeVigibaseDateRange : exeVigibaseDateRange,
                      prevDataSourcesList: JsonOutput.toJson(configurationInstance.selectedDatasource.split(",").minus("eudra")),
                      dataSheetList : configurationInstance.selectedDataSheet?dataSheetService.formatActiveDatasheetMap(activeDataSheets) as JSON:'',
                      isPVCM: dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM),productTypeMap : fetchProductTypeForDataSources(),
                      productHierarchy: ViewHelper.getHiearchyValues(), eventHierarchy: eventHierarchy, isReadOnlyUser: isReadOnlyUser
            ]
            render(view: "edit", model: model)
        }
    }

    def getAllEmailsUnique(Configuration configurationInstance) {
        userService.getAllEmails(userService.getUser())
    }

    @Secured(['ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def save() {
        List<String> alertGroupList = []

        if (params.alertGroup instanceof String) {
            alertGroupList = [params.alertGroup]
        } else if (params.alertGroup instanceof String[]) {
            alertGroupList = params.alertGroup.toList()
        }
        Configuration configurationInstance = new Configuration(type: Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
        if (params.isMasterTemplateAlert && params.productHierarchy) {
            def key = ViewHelper.getHiearchyValuesWithIndex(params.productHierarchy)
            configurationInstance.productHierarchyMasterTmplt = JsonOutput.toJson([(key+1):params.productHierarchy])
        }
      
        if(params.groupBySmq != "PT" && params.eventGroupSelection != '[]'){
            params.eventHierarchy = "EVENT_GROUP"
        } else if (params.groupBySmq != "PT" && params.eventGroupSelection == '[]') {
            params.eventHierarchy = "SMQ"
        } else {
            params.eventHierarchy = params.groupBySmq
        }
        if (params.isMasterTemplateAlert && params.eventHierarchy) {
            def key = eventHierarchy.indexOf(params.eventHierarchy)
            configurationInstance.eventHierarchyMasterTmplt = JsonOutput.toJson([(key+1): params.eventHierarchy])
        }
        try {
            getDataMiningVariableLabel(params)
            configurationInstance.isResume = false
            configurationInstance.isDatasheetChecked = false
            configurationInstance.setIsEnabled(false)
            configurationInstance = populateModel(configurationInstance)
            configurationInstance.excludedAlertGroups = params.isMasterTemplateAlert ? (alertGroupList ? alertGroupList.join(',') : null) : null
            setTemplateToAlert(configurationInstance)
            println "excludedAlertGroups = " + configurationInstance.excludedAlertGroups

            if (Boolean.parseBoolean(params.repeatExecution)) {
                configurationInstance.setIsEnabled(true)
                setNextRunDateAndScheduleDateJSON(configurationInstance)
            } else {
                configurationInstance.setIsEnabled(false)
                configurationInstance.nextRunDate = null
            }
            setWhoDrug(configurationInstance)
            if (configurationInstance.priority) {
                if (params.reviewPeriod) {
                    configurationInstance.reviewPeriod = Integer.parseInt(params.reviewPeriod)
                } else {
                    configurationInstance.reviewPeriod = getDefaultReviewPeriod(configurationInstance)
                }
            } else {
                configurationInstance.reviewPeriod = 0
            }

            def isProductOrFamilyOrIngredient = allowedDictionarySelection(configurationInstance)
            boolean isProductGroupOnly = configurationInstance.productSelection && configurationInstance.productGroupSelection
            if (!isProductOrFamilyOrIngredient || isProductGroupOnly) {
                flash.error = message(code: "app.label.product.family.error.message")
                configurationInstance.nextRunDate = null
                renderOnErrorScenario(configurationInstance)
                return
            }
            try {
                if (!configurationInstance.adhocRun && configurationInstance.scheduleDateJSON) {
                    JSONObject timeObject = JSON.parse(configurationInstance.scheduleDateJSON) as JSONObject
                    Date startDate = Date.parse(JSON_DATE, timeObject.startDateTime as String)
                    if (startDate < (DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH))) {
                        flash.error = message(code: "app.label.past.date.error", args: ["Start Date"])
                        configurationInstance.nextRunDate = null
                        renderOnErrorScenario(configurationInstance)
                        return
                    }
                }
            }catch(Exception e){
                configurationInstance.scheduleDateJSON = null
            }
            //Set the workflow group from the logged in user.
            configurationInstance.workflowGroup = userService.getUser().workflowGroup
            if (configurationInstance.groupBySmq && configurationInstance.eventSelection && configurationInstance.eventGroupSelection) {
                flash.error = message(code: "app.label.smq.event.group.error.message")
                configurationInstance.nextRunDate = null
                renderOnErrorScenario(configurationInstance)
                return
            } else if (configurationInstance.groupBySmq && configurationInstance.eventSelection) {
                List<Map> eventDetails = MiscUtil?.getEventDictionaryValues(configurationInstance.eventSelection)
                if (eventDetails.take(6).any { it.size() } && (eventDetails[6] || eventDetails[7])) {
                    flash.error = message(code: "app.label.smq.error.message")
                    configurationInstance.nextRunDate = null
                    renderOnErrorScenario(configurationInstance)
                    return
                }
            }

            configurationInstance = (Configuration) CRUDService.saveWithAuditLog(configurationInstance)
            aggregateCaseAlertService.persistUserReadOnlyMapping(params.sharedWith,configurationInstance.id)
            if (params.isMasterTemplateAlert && params.productHierarchy) {
                aggregateCaseAlertService.notifyAlertConfigSyncToMart(configurationInstance.id, params.productHierarchy, params.eventHierarchy)
            }
            if (!TextUtils.isEmpty(params.signalId)) {
                cacheService.setSignalAndConfigurationId(params.signalId,configurationInstance.id)
            }
        } catch (ValidationException ve) {
            ve.printStackTrace()
            log.error(ve.getMessage(), ve)
            configurationInstance.errors = ve.errors
            configurationInstance.nextRunDate = null
            renderOnErrorScenario(configurationInstance)
            return
        } catch (Exception ex) {
            ex.printStackTrace()
            log.error(ex.printStackTrace())
            flash.error = message(code: "app.label.alert.error")
            renderOnErrorScenario(configurationInstance)
            return
        }

        request.withFormat {
            form {
                flash.message = message(code: 'default.created.message', args: [message(code: 'app.label.quantitative.alert.configuration'), configurationInstance.name])
                redirect(action: "view", id: configurationInstance.id)
            }
            '*' { respond configurationInstance, [status: CREATED] }
        }
    }

    @Secured(['ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def details() {
        // Initialize variables
        Boolean alertDeletionObject = false
        Integer prevColCount = Holders.config.signal.quantitative.number.prev.columns
        Long executedConfigId = params.long("configId")
        Boolean isTempViewSelected = params.containsKey("tempViewId")

        if (params.masterConfigProduct && params.masterConfigProduct != null) {
            flash.message = message(code: "master.config.product.success.message", args: [params.masterConfigProduct])
        }

        if (params.callingScreen != Constants.Commons.DASHBOARD && executedConfigId == -1) {
            alertDeletionInProgress()
            return
        }

        ExecutedConfiguration executedConfiguration = executedConfigId ? ExecutedConfiguration.findById(executedConfigId) : null

        // Check if deletion is in progress for executed configuration
        if (executedConfiguration) {
            alertDeletionObject = alertService.isDeleteInProgress(executedConfiguration.configId as Long, executedConfiguration?.type) ?: false
        }

        // Initialize variables
        String evdasDateRange = executedConfiguration?.evdasDateRange
        String faersDateRange = executedConfiguration?.faersDateRange
        String vaersDateRange = executedConfiguration?.vaersDateRange
        String vigibaseDateRange = executedConfiguration?.vigibaseDateRange
        Long configId = 0L

        if (executedConfiguration || params.callingScreen == Constants.Commons.DASHBOARD) {
            if (params.callingScreen != Constants.Commons.DASHBOARD && !aggregateCaseAlertService.detailsAccessPermission(executedConfiguration.selectedDatasource.split(","))) {
                notFound()
                return
            }
            configId = executedConfiguration ? aggregateCaseAlertService.getAlertConfigObject(executedConfiguration) : 0L
        } else {
            notFound()
            return
        }

        if (params.callingScreen != Constants.Commons.DASHBOARD && alertDeletionObject) {
            alertDeletionInProgress()
            return
        }

        Boolean cumulative = userService.getCurrentUserPreference().isCumulativeAlertEnabled ?: false
        String name = Constants.Commons.BLANK_STRING
        String dateRange = Constants.Commons.BLANK_STRING
        String dssNetworkUrl = [grailsLinkGenerator.serverBaseURL, "aggregateCaseAlert", "dssScores"].join("/")
        String backUrl = request.getHeader('referer')
        List freqNames = []
        Boolean groupBySmq = false
        List listDateRange = []
        List dssDateRange = []
        String currentDateRangeDss = ""
        List actionConfigList = validatedSignalService.getActionConfigurationList(Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
        List alertDispositionList = dispositionService.listAlertDispositions()
        String dr = Constants.Commons.BLANK_STRING
        String alertType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT
        String timezone = userService.getCurrentUserPreference()?.timeZone
        List <String> prevFaersDate = []
        List <String> prevEvdasDate = []
        List <String> prevVaersDate = []
        List <String> prevVigibaseDate = []
        Map<String, Map> statusMap = [:]
        User user = userService.getUser()

        // Check permission for accessing alert
        if (params.callingScreen == Constants.Commons.REVIEW && (alertService.checkAlertSharedToCurrentUser(executedConfiguration) || !alertService.roleAuthorised(Constants.AlertConfigType.AGGREGATE_CASE_ALERT,executedConfiguration?.selectedDatasource))) {
            forward(controller: 'errors', action: 'permissionsError')
            log.info("${user?.username} does not have access to alert")
            return
        }

        if (params.callingScreen != Constants.Commons.DASHBOARD && !cumulative && params.callingScreen != Constants.Commons.TAGS) {
            statusMap = spotfireService.fetchAnalysisFileUrlCounts(executedConfiguration)
            name = executedConfiguration?.name
            if (name) {
                List<ExecutedConfiguration> prevExecs = aggregateCaseAlertService.fetchPrevPeriodExecConfig(executedConfiguration.configId, executedConfigId)
                prevExecs.each {
                    prevFaersDate.add(getEndDate(it.faersDateRange))
                    prevEvdasDate.add(getEndDate(it.evdasDateRange))
                    prevVaersDate.add(getEndDate(it.vaersDateRange))
                    prevVigibaseDate.add(getEndDate(it.vigibaseDateRange))
                }
                prevExecs.removeIf{it.id >= executedConfiguration.id}
                prevExecs.each { it ->
                    Date sdEx = it.executedAlertDateRangeInformation.dateRangeEndAbsolute
                    if (sdEx) {
                        listDateRange.add(Date.parse("dd-MMM-yyyy", DateUtil.toDateString1(sdEx)).format("dd/MMM/yy"))
                    }
                }
            }
            List<Long> prevExecList = alertService.fetchPrevExecConfigId(executedConfiguration, Configuration.get(configId))
            prevExecList.add(executedConfigId)
            List<ExecutedConfiguration> latestPrevExecList = prevExecList.sort { it ->
                ExecutedConfiguration prevExec = ExecutedConfiguration.get(it)
                prevExec?.dateCreated
            }.reverse()?.unique {
                [ExecutedConfiguration.get(it)?.executedAlertDateRangeInformation?.dateRangeStartAbsolute, ExecutedConfiguration.get(it)?.executedAlertDateRangeInformation?.dateRangeEndAbsolute]
            }.take(5)
            latestPrevExecList.each {
                String exDateRange = DateUtil.toDateString(ExecutedConfiguration.get(it).executedAlertDateRangeInformation.dateRangeStartAbsolute) +
                        " - " + DateUtil.toDateString(ExecutedConfiguration.get(it).executedAlertDateRangeInformation.dateRangeEndAbsolute)
                if (!dssDateRange.contains(exDateRange))
                    dssDateRange.add(exDateRange)
            }

            dateRange = DateUtil.toDateString(executedConfiguration.executedAlertDateRangeInformation.dateRangeStartAbsolute) +
                    " - " + DateUtil.toDateString(executedConfiguration.executedAlertDateRangeInformation.dateRangeEndAbsolute)
            String dateRangeStart = DateUtil.toDateString(executedConfiguration.executedAlertDateRangeInformation.dateRangeStartAbsolute);
            if (executedConfiguration.executedAlertDateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE) {
                String dateRangeEnd;
                if (executedConfiguration?.selectedDatasource == Constants.DataSource.FAERS) {
                    dateRangeEnd = reportExecutorService.getFaersDateRange().faersDate.substring(13);
                    dateRange = dateRangeStart + " - " + dateRangeEnd;
                    faersDateRange = dateRange
                } else if (executedConfiguration?.selectedDatasource == Constants.DataSource.VAERS) {
                    //Updated Vaers Date Range for Integrated Alert PVS-67898
                    dateRangeEnd = reportExecutorService.getVaersDateRange().vaersDate.substring(Constants.Commons.VAERS_END_DATE_INDEX);
                    dateRange = dateRangeStart + " - " + dateRangeEnd;
                    vaersDateRange = dateRange
                } else if (executedConfiguration?.selectedDatasource.contains(Constants.DataSource.FAERS)) {
                    faersDateRange = dateRangeStart + " - " + reportExecutorService.getFaersDateRange().faersDate.substring(13);
                } else if (executedConfiguration?.selectedDatasource.contains(Constants.DataSource.VAERS)) {
                    //Updated Vaers Date Range for Integrated Alert PVS-67898
                    vaersDateRange = dateRangeStart + " - " + reportExecutorService.getVaersDateRange().vaersDate.substring(Constants.Commons.VAERS_END_DATE_INDEX);
                } else {
                    dateRangeEnd = DateUtil.toDateString(executedConfiguration.executedAlertDateRangeInformation.dateRangeEndAbsolute);
                    dateRange = dateRangeStart + " - " + dateRangeEnd;
                }
                if(executedConfiguration?.selectedDatasource == Constants.DataSource.VIGIBASE){
                    dateRange = dateRangeStart + " - " + (reportExecutorService.getVigibaseDateRange().vigibaseDate).substring(13)
                    vigibaseDateRange = dateRange
                } else if(executedConfiguration?.selectedDatasource.contains(Constants.DataSource.VIGIBASE)){
                    vigibaseDateRange = dateRangeStart + " - " + (reportExecutorService.getVigibaseDateRange().vigibaseDate).substring(13)
                } else if(executedConfiguration?.selectedDatasource.contains(Constants.DataSource.JADER)){
                    dateRange = dateRangeStart + " - " + (jaderExecutorService.getJaderDateRange().jaderDate).substring(13)
                }
            }
            Date sd = executedConfiguration.executedAlertDateRangeInformation.dateRangeEndAbsolute
            if (sd) {
                dr = Date.parse("dd-MMM-yyyy", DateUtil.toDateString1(sd)).format("dd/MMM/yy")
            }
            groupBySmq = executedConfiguration.groupBySmq
            if (groupBySmq) {
                alertType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT_SMQ
            }
        } else if (params.callingScreen != Constants.Commons.TAGS) {
            backUrl = createLink(controller: 'dashboard', action: 'index')
        }

        if (params.callingScreen == Constants.Commons.DASHBOARD || params.callingScreen == Constants.Commons.TAGS) {
            alertType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DASHBOARD
        } else if (executedConfiguration) {
            String selectedDatasource = executedConfiguration.selectedDatasource
            if (selectedDatasource && !selectedDatasource.contains(Constants.DataSource.EUDRA)) {
                if (selectedDatasource.startsWith(Constants.DataSource.FAERS) && !selectedDatasource.contains(Constants.DataSource.VIGIBASE)) {
                    alertType = groupBySmq ? Constants.AlertConfigType.AGGREGATE_CASE_ALERT_SMQ_FAERS : Constants.AlertConfigType.AGGREGATE_CASE_ALERT_FAERS
                } else if (selectedDatasource.startsWith(Constants.DataSource.VAERS) && !selectedDatasource.contains(Constants.DataSource.VIGIBASE)) {
                    alertType = groupBySmq ? Constants.AlertConfigType.AGGREGATE_CASE_ALERT_SMQ_VAERS : Constants.AlertConfigType.AGGREGATE_CASE_ALERT_VAERS
                } else if (selectedDatasource.startsWith(Constants.DataSource.VIGIBASE)) {
                    alertType = groupBySmq ? Constants.AlertConfigType.AGGREGATE_CASE_ALERT_SMQ_VIGIBASE : Constants.AlertConfigType.AGGREGATE_CASE_ALERT_VIGIBASE
                } else if (selectedDatasource.startsWith(Constants.DataSource.JADER)) {
                    alertType = groupBySmq ? Constants.AlertConfigType.AGGREGATE_CASE_ALERT_SMQ_JADER : Constants.AlertConfigType.AGGREGATE_CASE_ALERT_JADER
                }
            }
        }

        ViewInstance viewInstance = viewInstanceService.fetchSelectedViewInstance(alertType,params.viewId as Long)

        String dssUrl = grailsApplication.config.dss.url
        Configuration configurationInstance = Configuration.get(configId)
        String selectedDatasource = configurationInstance?.selectedDatasource
        List<String> availableDataSources = grailsApplication.config.pvsignal.supported.datasource.call()
        boolean isFaersEnabled = availableDataSources.contains(Constants.DataSource.FAERS)
        boolean isEvdasEnabled = availableDataSources.contains(Constants.DataSource.EUDRA)
        boolean isPVAEnabled = selectedDatasource ? availableDataSources.contains(Constants.DataSource.PVA) && selectedDatasource.contains(Constants.DataSource.PVA) : false
        boolean isFaersAvailable = selectedDatasource ? availableDataSources.contains(Constants.DataSource.FAERS) && selectedDatasource.contains(Constants.DataSource.FAERS) : false
        boolean isVaersAvailable = selectedDatasource ? availableDataSources.contains(Constants.DataSource.VAERS) && selectedDatasource.contains(Constants.DataSource.VAERS) : false
        boolean isJaderAvailable = selectedDatasource ? availableDataSources.contains(Constants.DataSource.JADER) && selectedDatasource.contains(Constants.DataSource.JADER) : false
        boolean isVaersEnabled = availableDataSources.contains(Constants.DataSource.VAERS)
        boolean isVigibaseEnabled = availableDataSources.contains(Constants.DataSource.VIGIBASE)
        boolean isJaderEnabled = availableDataSources.contains(Constants.DataSource.JADER)
        List<Map> availableAlertPriorityJustifications = Justification.fetchByAnyFeatureOn([JustificationFeatureEnum.alertPriority], false)*.toDto(timezone)
        Map dispositionIncomingOutgoingMap = workflowRuleService.fetchDispositionIncomingOutgoingMap()
        Boolean forceJustification = userService.user.workflowGroup?.forceJustification
        List<Map> availablePriorities = priorityService.listPriorityOrder()
        List allowedProductsAsSafetyLead = alertService.isProductSecurity() ? safetyLeadSecurityService.allAllowedProductsForUser(userService.getCurrentUserId()) : []
        Map actionTypeAndActionMap = alertService.getActionTypeAndActionMap()
        List<String> reviewCompletedDispostionList = dispositionService.getReviewCompletedDispositionList()
        User currentUser = userService.getUser()
        List subGroupsColumnsList = []
        List faersSubGroupsColumnsList = []
        Map subGroupMap = cacheService.getSubGroupMap()
        subGroupMap[Holders.config.subgrouping.ageGroup.name]?.each { id, value ->
            subGroupsColumnsList.add(value)
        }
        subGroupMap[Holders.config.subgrouping.gender.name]?.each { id, value ->
            subGroupsColumnsList.add(value)
        }
        Map<String,List<String>> prrRorSubGroupMap =  cacheService.allOtherSubGroupColumnUIList(Constants.DataSource.PVA)
        Map<String,List<String>> relativeSubGroupMap = cacheService.relativeSubGroupColumnUIList(Constants.DataSource.PVA)
        Boolean isShareFilterViewAllowed = currentUser.isAdmin()
        Boolean isViewUpdateAllowed = viewInstance?.isViewUpdateAllowed(currentUser)
        subGroupMap[Holders.config.subgrouping.faers.ageGroup.name]?.each { id, value ->
            faersSubGroupsColumnsList.add(value)
        }
        subGroupMap[Holders.config.subgrouping.faers.gender.name]?.each { id, value ->
            faersSubGroupsColumnsList.add(value)
        }
        def prevColMap = Holders.config.signal.evdas.data.previous.columns.clone()
        def prevColumns = groovy.json.JsonOutput.toJson(prevColMap)
        Boolean hasAggReviewerAccess = hasReviewerAccess(Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
        Boolean isReadOnly = false
        List readOnlyUsers = alertService.getReadOnlyUsersForEx(executedConfiguration)
        List readOnlyGroups = alertService.getReadOnlyGroupsForEx(executedConfiguration)
        if (readOnlyUsers.find { it.id == currentUser.id && it.readOnly }) {
            hasAggReviewerAccess = false
            isReadOnly = true
        } else if (readOnlyGroups.find { currentUser.groups*.id.contains(it.id) && it.readOnly }) {
            hasAggReviewerAccess = false
            isReadOnly = true
        }
        if(currentUser.isAdmin() || executedConfiguration.owner.id==currentUser.id){
            isReadOnly = false
        }
        String buttonClass = hasAggReviewerAccess?"":"hidden"
        if(dssDateRange){
            currentDateRangeDss=dssDateRange.first()
        }
        def latestVersion
        if (params.callingScreen != Constants.Commons.DASHBOARD) {
            Configuration configuration = Configuration.get(executedConfiguration?.configId)
            latestVersion = ExecutionStatus.findAllByConfigIdAndExecutionStatusAndType(configuration?.id, ReportExecutionStatus.COMPLETED, Constants.AlertConfigType.AGGREGATE_CASE_ALERT).size()
        }
        Map dispositionData = workflowRuleService.fetchDispositionData();
        boolean labelCondition = executedConfiguration?.groupBySmq ? true : viewInstanceService.isLabelChangeRequired(selectedDatasource)
        if (params.callingScreen == Constants.Commons.DASHBOARD){
            labelCondition = !(Holders.config.dss.enable.autoProposed && Holders.config.statistics.enable.dss)
        }
        def aggregateHelpMap = Holders.config.aggregate.helpMap

        // Fetch label configuration data once
        Map labelConfig = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', null, null, null).collectEntries {
            b -> [b.name, [display: b.display, enabled: b.enabled, isHyperLink: b.isHyperLink, keyId: b.keyId]]
        }

        Map labelConfigDisplayNames = new HashMap()
        Map labelConfigCopy = new HashMap()
        List jaderColumnList = []
        // Process label configuration data
        labelConfig.each { key, value ->
            String displayName = value.display
            Boolean enabled = value.enabled
            labelConfigDisplayNames.put(key, displayName)
            labelConfigCopy.put(key, enabled)

            // Process for previous period count
            for (int j = 0; j < prevColCount; j++) {
                String prevKey = "exe${j}${key}"
                labelConfigCopy.put(prevKey, enabled)
                labelConfigDisplayNames.put(prevKey, "Prev Period ${j + 1} ${displayName}")
            }
        }

        List<Map> labelWithPreviousPeriod = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', true)
        List<Map> labelWithPreviousPeriodSubGroup = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT').findAll { it.type == "subGroup" }

        List<Map> labelWithPreviousPeriod1 = []

        for (int j = 0; j < prevColCount; j++) {
            labelWithPreviousPeriod.each { label ->
                if (!(label.name in ['hlt', 'hlgt', 'smqNarrow'])) {
                    labelWithPreviousPeriod1.add([
                            name                  : "exe${j}${label.name}",
                            display               : "Prev ${j + 1} ${label.display}",
                            enabled               : label.enabled,
                            visible               : labelConfigCopy.get(label.name),
                            previousPeriodCounter: j
                    ])
                }
            }

            labelWithPreviousPeriodSubGroup.each { subGroup ->
                labelWithPreviousPeriod1.add([
                        name                  : "exe${j}${subGroup.name}",
                        display               : "Prev ${j + 1} ${subGroup.display}",
                        enabled               : subGroup.enabled,
                        visible               : labelConfigCopy.get(subGroup.name),
                        previousPeriodCounter: j
                ])
            }
        }

        List searchableColumnList = []
        List<Map> fieldList = []
        if(isJaderAvailable){
            jaderColumnList = alertFieldService.getJaderColumnList(selectedDatasource,groupBySmq)
            searchableColumnList = alertService.generateSearchableColumnsJader(groupBySmq)
            fieldList = aggregateCaseAlertService.fieldListAdvanceFilterJader(groupBySmq)
        }else {
            searchableColumnList = alertService.generateSearchableColumns(groupBySmq, params.callingScreen, selectedDatasource)
            fieldList = aggregateCaseAlertService.fieldListAdvanceFilter(selectedDatasource,groupBySmq,params.callingScreen,'AGGREGATE_CASE_ALERT').sort()

            if (groupBySmq) {
                fieldList.removeAll {
                    (it.name.toString().equals("smqNarrow") || it.name.toString().equals("hlt") || it.name.toString().equals("hlgt"))
                }
            }
        }

        labelWithPreviousPeriod1 = labelWithPreviousPeriod1 + labelWithPreviousPeriod


        render(view: 'details', model: [
                                        executedConfigId                    : executedConfigId,
                                        labelCondition                      : labelCondition,
                                        selectedDatasource                  : selectedDatasource,
                                        isSaftyDb                           : executedConfiguration ? executedConfiguration.selectedDatasource==Constants.DataSource.PVA:false,
                                        evdasDateRange                      : evdasDateRange,
                                        faersDateRange                      : faersDateRange,
                                        vaersDateRange                      : vaersDateRange,
                                        vigibaseDateRange                   : vigibaseDateRange,
                                        evdasEndDate                        : getEndDate(evdasDateRange),
                                        faersEndDate                        : getEndDate(faersDateRange),
                                        prevFaersDate                       : prevFaersDate,
                                        prevVaersDate                       : prevVaersDate,
                                        prevVigibaseDate                    : prevVigibaseDate,
                                        prevEvdasDate                       : prevEvdasDate,
                                        configId                            : configId,
                                        prevColumns                         : prevColMap,
                                        prevColumnsJson                     : prevColumns,
                                        callingScreen                       : params.callingScreen,
                                        name                                : name,
                                        showPrr                             : grailsApplication.config.statistics.enable.prr,
                                        showRor                             : grailsApplication.config.statistics.enable.ror,
                                        showEbgm                            : grailsApplication.config.statistics.enable.ebgm,
                                        showPrrFaers                        : isFaersEnabled && grailsApplication.config.statistics.faers.enable.prr,
                                        showRorFaers                        : isFaersEnabled && grailsApplication.config.statistics.faers.enable.ror,
                                        showEbgmFaers                       : isFaersEnabled && grailsApplication.config.statistics.faers.enable.ebgm,
                                        showPrrVaers                        : isVaersEnabled && grailsApplication.config.statistics.vaers.enable.prr,
                                        showRorVaers                        : isVaersEnabled && grailsApplication.config.statistics.vaers.enable.ror,
                                        showEbgmVaers                       : isVaersEnabled && grailsApplication.config.statistics.vaers.enable.ebgm,
                                        showPrrVigibase                     : isVigibaseEnabled && grailsApplication.config.statistics.vigibase.enable.prr,
                                        showRorVigibase                     : isVigibaseEnabled && grailsApplication.config.statistics.vigibase.enable.ror,
                                        showEbgmVigibase                    : isVigibaseEnabled && grailsApplication.config.statistics.vigibase.enable.ebgm,
                                        showPrrJader                        : isJaderEnabled && grailsApplication.config.statistics.jader.enable.prr,
                                        showRorJader                        : isJaderEnabled && grailsApplication.config.statistics.jader.enable.ror,
                                        showEbgmJader                       : isJaderEnabled && grailsApplication.config.statistics.jader.enable.ebgm,
                                        showDss                             : grailsApplication.config.statistics.enable.dss,
                                        showDssScores                       : grailsApplication.config.statistics.enable.dssScores,
                                        isAutoProposed                      : grailsApplication.config.dss.enable.autoProposed,
                                        aggregateRules                      : getAggregateRuleJson(),
                                        groupBySmq                          : groupBySmq,
                                        backUrl                             : backUrl,
                                        dr                                  : dr,
                                        listDr                              : listDateRange,
                                        dssDateRange                        : dssDateRange,
                                        currentDateRangeDss                 : currentDateRangeDss,
                                        dateRange                           : dateRange,
                                        dssUrl                              : dssUrl,
                                        cumulative                          : cumulative,
                                        freqNames                           : freqNames,
                                        viewInstance                        : viewInstance ?: null,
                                        isShareFilterViewAllowed            : isShareFilterViewAllowed,
                                        isViewUpdateAllowed                 : isViewUpdateAllowed,
                                        filterMap                           : viewInstance ? viewInstance.filters : "",
                                        columnIndex                         : "",
                                        sortedColumn                        : viewInstance ? viewInstance.sorting : "",
                                        advancedFilterView                  : viewInstance?.advancedFilter ? alertService.fetchAdvancedFilterDetails(viewInstance.advancedFilter) : "",
                                        viewId                              : viewInstance ? viewInstance.id : "",
                                        tagName                             : params.tagName,
                                        actionConfigList                    : actionConfigList,
                                        isFaers                             : (executedConfiguration && executedConfiguration?.selectedDatasource == Constants.DataSource.FAERS) ?: false,
                                        isVaers                             : (executedConfiguration && executedConfiguration?.selectedDatasource == Constants.DataSource.VAERS) ?: false,
                                        isVigibase                          : (executedConfiguration && executedConfiguration?.selectedDatasource == Constants.DataSource.VIGIBASE) ?: false,
                                        isJader                             : (executedConfiguration && executedConfiguration?.selectedDatasource == Constants.DataSource.JADER) ?: false,
                                        dashboardFilter                     : params.dashboardFilter ? params.dashboardFilter : '',
                                        reportUrl                           : reportIntegrationService.fetchReportUrl(executedConfiguration),
                                        reportName                          : executedConfiguration?.name,
                                        analysisFileUrl                     : spotfireService.fetchAnalysisFileUrlIntegratedReview(executedConfiguration),
                                        dispositionIncomingOutgoingMap      : dispositionIncomingOutgoingMap as JSON,
                                        dispositionData                     : dispositionData as JSON,
                                        forceJustification                  : forceJustification,
                                        availableAlertPriorityJustifications: availableAlertPriorityJustifications,
                                        availablePriorities                 : availablePriorities,
                                        allowedProductsAsSafetyLead         : allowedProductsAsSafetyLead?.join(","),
                                        prevColCount                        : prevColCount,
                                        isLatest                            : executedConfiguration?.isLatest,
                                        appType                             : Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
                                        fieldList                           : fieldList.sort({it?.display?.toUpperCase()}),
                                        reviewCompletedDispostionList       : JsonOutput.toJson(reviewCompletedDispostionList),
                                        actionPropertiesMap                 : JsonOutput.toJson(actionTypeAndActionMap.actionPropertiesMap),
                                        actionTypeList                      : actionTypeAndActionMap.actionTypeList,
                                        alertDispositionList                : alertDispositionList,
                                        isProductSecurity                   : alertService.isProductSecurity(),
                                        subGroupsColumnList                 : subGroupsColumnsList,
                                        faersSubGroupsColumnList            : faersSubGroupsColumnsList,
                                        relativeSubGroupMap                 : relativeSubGroupMap,
                                        prrRorSubGroupMap                   : prrRorSubGroupMap,
                                        filterIndex                         : JsonOutput.toJson(searchableColumnList[0]),
                                        filterIndexMap                      : JsonOutput.toJson(searchableColumnList[1]),
                                        isCaseSeriesAccess                  : SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_VIEWER, ROLE_VIEW_ALL, ROLE_FAERS_CONFIGURATION, ROLE_VAERS_CONFIGURATION, ROLE_VIGIBASE_CONFIGURATION,ROLE_JADER_CONFIGURATION"),
                                        alertType                           : alertType,
                                        isPVAEnabled                        : isPVAEnabled,
                                        isJaderAvailable                    : isJaderAvailable,
                                        isFaersEnabled                      : isFaersEnabled,
                                        isVaersEnabled                      : isVaersEnabled,
                                        isVigibaseEnabled                   : isVigibaseEnabled,
                                        isJaderEnabled                      : isJaderEnabled,
                                        isEvdasEnabled                      : isEvdasEnabled,
                                        allFourEnabled                      : isVigibaseEnabled && isVaersEnabled && isFaersEnabled && isEvdasEnabled,
                                        allThreeEnabled                     : (isVaersEnabled && isFaersEnabled && isEvdasEnabled && !isVigibaseEnabled) || (isVigibaseEnabled && isFaersEnabled && isEvdasEnabled && !isVaersEnabled) || (isVigibaseEnabled && isVaersEnabled && isEvdasEnabled && !isFaersEnabled) || (isVigibaseEnabled && isFaersEnabled && isVaersEnabled && !isEvdasEnabled),
                                        anyTwoEnabled                       : (isVaersEnabled && isFaersEnabled && !isEvdasEnabled && !isVigibaseEnabled) || (isEvdasEnabled && isVaersEnabled && !isFaersEnabled && !isVigibaseEnabled) || (isEvdasEnabled && isFaersEnabled && !isVaersEnabled && !isVigibaseEnabled) || (isVaersEnabled && !isFaersEnabled && !isEvdasEnabled && isVigibaseEnabled) || (isEvdasEnabled && !isVaersEnabled && !isFaersEnabled && isVigibaseEnabled) || (isEvdasEnabled && !isFaersEnabled && !isVaersEnabled && isVigibaseEnabled),
                                        anyOneEnabled                       : (isVaersEnabled && !isFaersEnabled && !isEvdasEnabled && !isVigibaseEnabled) || (isEvdasEnabled && !isVaersEnabled && !isFaersEnabled && !isVigibaseEnabled) || (isFaersEnabled && !isEvdasEnabled && !isVaersEnabled && !isVigibaseEnabled) || (isVigibaseEnabled && !isFaersEnabled && !isEvdasEnabled && !isVaersEnabled),
                                        isArchived                          : params.boolean('archived')?:false,
                                        isRor                               : cacheService.getRorCache(),
                                        isPriorityEnabled                   : grailsApplication.config.alert.priority.enable,
                                        analysisStatus                      : statusMap,
                                        analysisStatusJson                  : statusMap as JSON,
                                        saveCategoryAccess                  : checkAccessForCategorySave(Constants.AlertConfigType.AGGREGATE_CASE_ALERT),
                                        hasAggReviewerAccess                : hasAggReviewerAccess,
                                        hasSignalCreationAccessAccess       : hasSignalCreationAccessAccess(),
                                        hasSignalViewAccessAccess           : hasSignalViewAccessAccess(),
                                        buttonClass                         : buttonClass,
                                        isTempViewSelected                  : isTempViewSelected,
                                        dssNetworkUrl                       : dssNetworkUrl,
                                        isFaersAvailable                    : isFaersAvailable,
                                        isVaersAvailable                    : isVaersAvailable,
                                        version                             : params.version?:latestVersion?:"null",
                                        currUserName                        : currentUser.fullName,
                                        aggregateHelpMap                    : aggregateHelpMap,
                                        labelConfig    : labelConfigDisplayNames,
                                        labelConfigCopy: labelConfigCopy,
                                        labelConfigKeyId: labelConfig.collectEntries { key, value -> [key, value.keyId] },
                                        hyperlinkConfiguration: labelConfig.collectEntries { key, value -> [key, value.isHyperLink] },
                                        jaderColumnList: jaderColumnList,
                                        labelConfigJson: alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', true) as JSON,
                                        labelConfigNew : labelWithPreviousPeriod1,
                                        isReadOnly : isReadOnly

        ])
    }

    String getEndDate(String dateRange){
        if(dateRange){
            String date = dateRange.substring(dateRange.indexOf(" - ")+" - ".size(),dateRange.length()).replace('-','/')
            date.replace(date[-4..-1],date[-2..-1])
        } else {
            null
        }
    }
    void alertDeletionInProgress() {
        redirect(action: "alertInProgressError", controller: 'errors')
    }

    private setNextRunDateAndScheduleDateJSON(Configuration configurationInstance) {
        try {

            if (configurationInstance.scheduleDateJSON && configurationInstance.isEnabled) {
                configurationInstance.nextRunDate = configurationService.getNextDate(configurationInstance)
            } else {
                configurationInstance.nextRunDate = null
            }
        }catch(Exception e){
            configurationInstance.nextRunDate = null
        }
    }

    def getDssHistoryDetails(Long executedConfigId, Long configId, String pt, Boolean isArchived){

        List dssDateRange = []
        Map<String, Map> somethng = new HashMap<>()
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(executedConfigId)
        List<ExecutedConfiguration> prevExecList = alertService.fetchPrevExecConfigId(executedConfiguration, Configuration.get(configId))

        prevExecList.add(executedConfigId)
        List<ExecutedConfiguration> latestPrevExecList = prevExecList.sort { it ->
            ExecutedConfiguration prevExec = ExecutedConfiguration.get(it)
            prevExec?.dateCreated
        }.reverse()?.unique {
            [ExecutedConfiguration.get(it)?.executedAlertDateRangeInformation?.dateRangeStartAbsolute, ExecutedConfiguration.get(it)?.executedAlertDateRangeInformation?.dateRangeEndAbsolute]
        }.take(5)
        latestPrevExecList.each { it ->
            ExecutedConfiguration prevConf = ExecutedConfiguration.get(it)
            def domain = aggregateCaseAlertService.getDomainObject(isArchived)
            def aggregateCaseAlert = domain.createCriteria().get {
                'eq'("executedAlertConfiguration.id", executedConfigId)
                'eq'("pt", pt)
            }

            String dateRange = DateUtil.toDateString(prevConf.executedAlertDateRangeInformation.dateRangeStartAbsolute) +
                    " - " + DateUtil.toDateString(prevConf.executedAlertDateRangeInformation.dateRangeEndAbsolute)

            ObjectMapper objectMapper = new ObjectMapper()
            String disabledNodesString = ExecutedConfiguration.get(executedConfigId)?.disabledDssNodes
            List disabledNodes = []
            if(disabledNodesString){
                disabledNodes = objectMapper.readValue(disabledNodesString, List.class)
            }
            List<Map<String, String>> rationaleList =[]
            if(aggregateCaseAlert?.getDssScore() != null){
                Map<String, String> alertDssScoreMap = objectMapper.readValue(aggregateCaseAlert?.getDssScore(), Map.class)
                rationaleList = alertDssScoreMap?.get("rationale_details")
                rationaleList.removeIf{disabledNodes?.contains(it?.pv_concept)}
            }

            rationaleList?.each {  def rationale ->
                if (somethng.containsKey(rationale.get("pv_concept"))) {
                    Map tempMap = somethng.get(rationale.get("pv_concept"))
                    tempMap.put(dateRange, rationale.get("potential_signal"))
                    tempMap.put("parent", rationale.get("parent"))
                    somethng.put(rationale.get("pv_concept"), tempMap)

                } else {
                    Map tempMap = [:]
                    tempMap.put("pv_concept", rationale.get("pv_concept"))
                    tempMap.put(dateRange, rationale.get("potential_signal"))
                    tempMap.put("parent", rationale.get("parent"))
                    somethng.put(rationale.get("pv_concept"), tempMap)
                }
            }

        }
        Map finalMap = [aaData: somethng.values()?.sort{it?.pv_concept}]
        render finalMap as JSON
    }

    def getRationaleDetails(Long executedConfigId, Long configId, String pt, Boolean isArchived,String productDss,String socDss){
        List dssDateRange = []
        def domain = aggregateCaseAlertService.getDomainObject(isArchived)
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(executedConfigId)
        List<ExecutedConfiguration> prevExecList = alertService.fetchPrevExecConfigId(executedConfiguration, Configuration.get(configId))
        prevExecList.add(executedConfigId)
        List<ExecutedConfiguration> latestPrevExecList = prevExecList.sort { it ->
            ExecutedConfiguration prevExec = ExecutedConfiguration.get(it)
            prevExec?.dateCreated
        }.reverse()?.unique {
            [ExecutedConfiguration.get(it)?.executedAlertDateRangeInformation?.dateRangeStartAbsolute, ExecutedConfiguration.get(it)?.executedAlertDateRangeInformation?.dateRangeEndAbsolute]
        }.take(5)
        latestPrevExecList.each { it ->
            String exDateRange = DateUtil.toDateString(ExecutedConfiguration.get(it).executedAlertDateRangeInformation.dateRangeStartAbsolute) +
                    " - " + DateUtil.toDateString(ExecutedConfiguration.get(it).executedAlertDateRangeInformation.dateRangeEndAbsolute)
            if (!dssDateRange.contains(exDateRange))
                dssDateRange.add(exDateRange)
        }
        def aggregateCaseAlert = domain.createCriteria().get {
                'eq'("executedAlertConfiguration.id", executedConfigId)
                'eq'("pt", pt)
                'eq'("productName", productDss)
                'eq'("soc", socDss)

            }
        ObjectMapper objectMapper = new ObjectMapper()
        String disabledNodesString = ExecutedConfiguration.get(executedConfigId)?.disabledDssNodes
        List disabledNodes = []
        if(disabledNodesString){
            disabledNodes = objectMapper.readValue(disabledNodesString, List.class)
        }
        Map<String, String> alertDssScoreMap =[:]
        List<Map<String, String>> rationaleList =[]
        if(aggregateCaseAlert?.getDssScore()!=null){
            alertDssScoreMap=objectMapper.readValue(aggregateCaseAlert.getDssScore(), Map.class)
            rationaleList = alertDssScoreMap.get("rationale_details")
            rationaleList.removeIf{disabledNodes?.contains(it?.pv_concept)}
        }

        Map finalMap = [aaData: rationaleList?.sort{it?.pv_concept},dssDateRange:dssDateRange]
        render finalMap as JSON
    }
    @Secured(['ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def run() {
        Configuration configurationInstance
        getDataMiningVariableLabel(params)
        def existing_config = false
        if (params.id && params.previousAction != Constants.AlertActions.COPY) {
            configurationInstance = Configuration.get(params.id)
            existing_config = true
            if (alertService.ifReadOnlyUserForConfig(userService.getUser(),configurationInstance) && (!userService.getUser().isAdmin() && userService.getUser()!=configurationInstance.owner)){
                flash.warn = message(code: "app.configuration.run.permission", args: [configurationInstance.name])
                redirect(controller: "configuration", action: "index")
            }
        } else {
            configurationInstance = new Configuration(type: Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
        }
        try {
            configurationInstance.isResume = false
            configurationInstance.setIsEnabled(true)
            configurationInstance.repeatExecution = params.repeatExecution
            configurationInstance = populateModel(configurationInstance)
            setTemplateToAlert(configurationInstance)
            if (configurationInstance.priority) {
                if (params.reviewPeriod) {
                    configurationInstance.reviewPeriod = Integer.parseInt(params.reviewPeriod)
                } else {
                    configurationInstance.reviewPeriod = getDefaultReviewPeriod(configurationInstance)
                }
            } else {
                configurationInstance.reviewPeriod = 0
            }
            if (configurationInstance.masterConfigId) {
                configurationInstance.masterConfigId = null
            }

            def isProductOrFamily = allowedDictionarySelection(configurationInstance)
            boolean isProductGroupOnly = configurationInstance.productSelection && configurationInstance.productGroupSelection
            if (!isProductOrFamily || isProductGroupOnly) {
                flash.error = message(code: "app.label.product.family.error.message")
                if (Boolean.parseBoolean(params.repeatExecution)) {
                    configurationInstance.setIsEnabled(true)
                } else {
                    configurationInstance.setIsEnabled(false)
                }
                configurationInstance.nextRunDate = null
                renderOnErrorScenario(configurationInstance)
                return
            }
            setWhoDrug(configurationInstance)

            try {
                if (!configurationInstance.adhocRun && configurationInstance.scheduleDateJSON) {
                    JSONObject timeObject = JSON.parse(configurationInstance.scheduleDateJSON)
                    Date startDate = Date.parse(JSON_DATE, timeObject.startDateTime)
                    if (startDate < (DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH))) {
                        flash.error = message(code: "app.label.past.date.error", args: ["Start Date"])
                        configurationInstance.nextRunDate = null
                        renderOnErrorScenario(configurationInstance)
                        return
                    }
                }
            } catch(Exception e){
                configurationInstance.scheduleDateJSON = null
            }
            //Set the workflow group from the logged in user.
            configurationInstance.workflowGroup = userService.getUser().workflowGroup

            if(configurationInstance.adhocRun){
                configurationInstance.nextRunDate = new Date()
            }

            if (configurationInstance.groupBySmq && configurationInstance.eventSelection && configurationInstance.eventGroupSelection) {
                flash.error = message(code: "app.label.smq.event.group.error.message")
                configurationInstance.nextRunDate = null
                renderOnErrorScenario(configurationInstance)
                return
            } else if (configurationInstance.groupBySmq && configurationInstance.eventSelection) {
                List<Map> eventDetails = MiscUtil?.getEventDictionaryValues(configurationInstance.eventSelection)
                if (eventDetails.take(6).any { it.size() } && (eventDetails[6] || eventDetails[7])) {
                    flash.error = message(code: "app.label.smq.error.message")
                    configurationInstance.nextRunDate = null
                    renderOnErrorScenario(configurationInstance)
                    return
                }
            }
            if (existing_config) {
                configurationInstance = (Configuration) CRUDService.updateWithAuditLog(configurationInstance)
                aggregateCaseAlertService.updateIntegratedConfiguration(configurationInstance)
                def exConfig = ExecutedConfiguration.findByConfigId(configurationInstance?.id)
                if (params.name && !exConfig?.name.equals(params.name)) {
                    alertService.renameExecConfig(configurationInstance.id, configurationInstance.name, params.name, configurationInstance.owner.id, Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
                }

            } else {
                configurationInstance = (Configuration) CRUDService.saveWithAuditLog(configurationInstance)
                if (!TextUtils.isEmpty(params.signalId)) {
                    cacheService.setSignalAndConfigurationId(params.signalId,configurationInstance.id)
                }
            }
            aggregateCaseAlertService.persistUserReadOnlyMapping(params.sharedWith,configurationInstance.id)
        } catch (ValidationException ve) {
            ve.printStackTrace()
            log.error(ve.getMessage(), ve)
            if (Boolean.parseBoolean(params.repeatExecution)) {
                configurationInstance.setIsEnabled(true)
            } else {
                configurationInstance.setIsEnabled(false)
            }
            configurationInstance.errors = ve.errors
            configurationInstance.nextRunDate = null
            renderOnErrorScenario(configurationInstance)
            return
        } catch (Exception exception) {
            exception.printStackTrace()
            log.error("Some error occurred while saving aggregate configuration.", exception)
            flash.error = message(code: "app.label.alert.error")
            renderOnErrorScenario(configurationInstance)
            return
        }

        boolean precheckEnabled = Holders.config.system.precheck.configuration.enable
        if(precheckEnabled && !alertService.isPreCheckVerified(AlertType.AGGREGATE_CASE_ALERT,configurationInstance)){
            flash.message = message(code: 'default.precheck.scheduled.message', args: [message(code: 'app.role.ROLE_AGGREGATE_CASE_CONFIGURATION'), configurationInstance.name])
        }else{
            flash.message = message(code: 'app.Configuration.RunningMessage', args: [message(code: 'configuration.label'), configurationInstance.name])
        }

        request.withFormat {
            form {
                flash.message =flash.message
                redirect(controller: "configuration", action: "executionStatus",params: [alertType: AlertType.AGGREGATE_CASE_ALERT])
            }
            '*' { respond configurationInstance, [status: CREATED] }
        }
    }

    private void renderOnErrorScenario(Configuration configuration) {
        String startDateAbsoluteCustomFreq = ""
        String endDateAbsoluteCustomFreq = ""
        List listOfSelectedDataSource = []
        String selectedDataSource = ""

        String action = params.previousAction

        if (configuration?.alertDateRangeInformation?.dateRangeEnum == DateRangeEnum.CUSTOM) {
            startDateAbsoluteCustomFreq = DateUtil.toDateString1(configuration?.alertDateRangeInformation?.dateRangeStartAbsolute)
            endDateAbsoluteCustomFreq = DateUtil.toDateString1(configuration?.alertDateRangeInformation?.dateRangeEndAbsolute)
        }
        if (configuration?.selectedDatasource) {
            selectedDataSource = configuration.selectedDatasource
            listOfSelectedDataSource = Arrays.asList(selectedDataSource?.toString().split(","))
        }
        Map model = modelData(configuration, action)
        model.signalId = params.signalId ?: null
        model << [startDateAbsoluteCustomFreq: startDateAbsoluteCustomFreq, endDateAbsoluteCustomFreq: endDateAbsoluteCustomFreq]
        model << [productTypeMap : fetchProductTypeForDataSources()]
        model << [isPVCM: dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM)]
        model << [validationError: true]
        model << [prevDataSourcesList: JsonOutput.toJson(configuration.selectedDatasource.split(",").minus("eudra"))]
        if(!model.dataSheetList){
            model.dataSheetList = dataSheetService.formatDatasheetMap(configuration) as JSON ?: ''
        }
        model << [listOfSelectedDataSource: listOfSelectedDataSource]
        JsonSlurper jsonSlurper = new JsonSlurper()
        model << [productHierarchy: ViewHelper.getHiearchyValues(), eventHierarchy: eventHierarchy, configurationInstancePrdHierarchy: configuration?.productHierarchyMasterTmplt ? jsonSlurper.parseText(configuration?.productHierarchyMasterTmplt)?.values()?.first() : '']

        if (params.id && action != Constants.AlertActions.COPY) {
            render view: "edit", model: model
        } else {
            render view: "create", model: model
        }
    }

    @Secured(['ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_EXECUTE_SHARED_ALERTS', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def runOnce() {
        Configuration configurationInstance = Configuration.get(params.id)
        if (alertService.ifReadOnlyUserForConfig(userService.getUser(),configurationInstance) && (!userService.getUser().isAdmin() && userService.getUser()!=configurationInstance.owner)){
            flash.warn = message(code: "app.configuration.run.permission", args: [configurationInstance.name])
            redirect(controller: "configuration", action: "index")
        }
        getDataMiningVariableLabel(params)
        if (!configurationInstance) {
            notFound()
            return
        }
        try {
            String drugs = Configuration.findById(configurationInstance.id)?.drugType
            List drugList = drugs ? drugs.split(",").collect { it as Long } : []
            List foundDrugList = ProductTypeConfiguration.findAllByIdInList(drugList)
            if (!foundDrugList) {
                flash.warn = message(code: "app.configuration.drugtype.deleted")
                redirect(action: "index")
            }
        } catch(Exception ex) {
            log.error("Drug type is not integer")
        }
        if (configurationInstance.nextRunDate != null && configurationInstance.isEnabled == true) {
            flash.warn = message(code: 'app.configuration.run.exists')
            redirect(action: "index")

        } else {
            try {
                configurationInstance.setIsEnabled(true)
                try {
                    configurationInstance.setScheduleDateJSON(getRunOnceScheduledDateJson())
                    configurationInstance.setNextRunDate(configurationService.getNextDate(configurationInstance))
                    setTemplateToAlert(configurationInstance)
                    configurationInstance = (Configuration) CRUDService.updateWithAuditLog(configurationInstance)
                }catch(Exception e){
                    configurationInstance.scheduleDateJSON = null
                }
                setWhoDrug(configurationInstance)
            } catch (ValidationException ve) {
                log.error(ve.getMessage(), ve)
                configurationInstance.errors = ve.errors
                renderOnErrorScenario(configurationInstance)
                return
            } catch (Exception exception) {
                flash.error = message(code: "app.label.alert.error")
                renderOnErrorScenario(configurationInstance)
                return
            }

            boolean precheckEnabled = Holders.config.system.precheck.configuration.enable
            if (precheckEnabled && !alertService.isPreCheckVerified(AlertType.AGGREGATE_CASE_ALERT, configurationInstance)) {
                flash.message = message(code: 'default.precheck.scheduled.message', args: [message(code: 'app.role.ROLE_AGGREGATE_CASE_CONFIGURATION'), configurationInstance.name])
            } else {
                flash.message = message(code: 'app.Configuration.RunningMessage')
            }
            request.withFormat {
                form {
                    flash.message = flash.message
                    redirect(controller: "configuration", action: "executionStatus",params: [alertType: AlertType.AGGREGATE_CASE_ALERT])
                }
                '*' { respond configurationInstance, [status: CREATED] }
            }
        }
    }

    private getRunOnceScheduledDateJson() {
        def startupTime = (new Date()).format(ConfigurationService.JSON_DATE)
        def timeZone = DateUtil.getTimezoneForRunOnce(userService.getUser())
        return """{"startDateTime":"${
            startupTime
        }","timeZone":{"${timeZone}"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}"""

    }

    @Secured(['ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def update() {
        Configuration configurationInstance = Configuration.lock(params.id)
        getDataMiningVariableLabel(params)
        if (!configurationInstance) {
            notFound()
            return
        }
        try {
            populateModel(configurationInstance)
            setTemplateToAlert(configurationInstance)
            configurationInstance.isResume = false
            configurationInstance.setIsEnabled(false)
            configurationInstance.repeatExecution = params.repeatExecution
            if (Boolean.parseBoolean(params.repeatExecution)) {
                configurationInstance.setIsEnabled(true)
                setNextRunDateAndScheduleDateJSON(configurationInstance)
            } else {
                configurationInstance.setIsEnabled(false)
                configurationInstance.nextRunDate = null
            }
            if (configurationInstance.masterConfigId) {
                configurationInstance.masterConfigId = null
            }

            def isProductOrFamily = allowedDictionarySelection(configurationInstance)
            boolean isProductGroupOnly = configurationInstance.productSelection && configurationInstance.productGroupSelection
            if (!isProductOrFamily || isProductGroupOnly) {
                flash.error = message(code: "app.label.product.family.error.message")
                renderOnErrorScenario(configurationInstance)
                return
            } else if (configurationInstance.groupBySmq && configurationInstance.eventSelection && configurationInstance.eventGroupSelection) {
                flash.error = message(code: "app.label.smq.event.group.error.message")
                configurationInstance.nextRunDate = null
                renderOnErrorScenario(configurationInstance)
                return
            } else if (configurationInstance.groupBySmq && configurationInstance.eventSelection) {
                List<Map> eventDetails = MiscUtil?.getEventDictionaryValues(configurationInstance.eventSelection)
                if (eventDetails.take(6).any { it.size() } && (eventDetails[6] || eventDetails[7])) {
                    flash.error = message(code: "app.label.smq.error.message")
                    configurationInstance.nextRunDate = null
                    renderOnErrorScenario(configurationInstance)
                    return
                }
            }
            try {
                if (!configurationInstance.adhocRun && configurationInstance.scheduleDateJSON) {
                    JSONObject timeObject = JSON.parse(configurationInstance.scheduleDateJSON)
                    Date startDate = Date.parse(JSON_DATE, timeObject.startDateTime)
                    if (startDate < (DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH))) {
                        flash.error = message(code: "app.label.past.date.error", args: ["Start Date"])
                        configurationInstance.nextRunDate = null
                        renderOnErrorScenario(configurationInstance)
                        return
                    }
                }
            }catch(Exception e){
                configurationInstance.scheduleDateJSON = null
            }
            setWhoDrug(configurationInstance)
            def exConfig = ExecutedConfiguration.findByConfigId(configurationInstance?.id)
            if (params.name && !exConfig?.name.equals(params.name)) {
                alertService.renameExecConfig(configurationInstance.id, configurationInstance.name, params.name, configurationInstance.owner.id, Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
            }

            configurationInstance = (Configuration) CRUDService.updateWithAuditLog(configurationInstance)
            aggregateCaseAlertService.persistUserReadOnlyMapping(params.sharedWith,configurationInstance.id)
            if (configurationInstance.isMasterTemplateAlert && params.productHierarchy) {
                aggregateCaseAlertService.notifyAlertConfigSyncToMart(configurationInstance.id, params.productHierarchy, params.eventHierarchy)
            }
            aggregateCaseAlertService.updateIntegratedConfiguration(configurationInstance)
        } catch (ValidationException ve) {
            log.error(ve.getMessage(), ve)
            configurationInstance.errors = ve.errors
            configurationInstance.nextRunDate = null
            renderOnErrorScenario(configurationInstance)
            return
        } catch (Exception exception) {
            flash.error = message(code: "app.label.alert.error")
            renderOnErrorScenario(configurationInstance)
        }

        request.withFormat {
            form {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'app.label.quantitative.alert.configuration'), configurationInstance.name])
                redirect(action: "view", id: configurationInstance.id)
            }
            '*' { respond configurationInstance, [status: OK] }
        }
    }


    void getDataMiningVariableLabel(params){
        if(params.dataMiningVariable?.contains(";")){
            String dataMiningVariable=params.dataMiningVariable.split(";")[0]
            List<Map> miningVariableMap
            if(params.selectedDatasource==(Constants.DataSource.PVA)){
                miningVariableMap=cacheService.getMiningVariables(Constants.DataSource.PVA)?.collect{it.value}
            }else if(params.selectedDatasource==(Constants.DataSource.FAERS)){
                miningVariableMap=cacheService.getMiningVariables(Constants.DataSource.FAERS)?.collect{it.value}
            }else{
                miningVariableMap=cacheService.getMiningVariables(Constants.DataSource.PVA)?.collect{it.value}
            }
            miningVariableMap.each {
                if(it.use_case==dataMiningVariable){
                    params.dataMiningVariable=it.label
                }
            }
        }
    }

    @Secured(['ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def review() {
        //If the cumulative alert check box is checked then flow will show the bulk cumulative screen with all the record set.
        Boolean showCumulative = userService.getUser()?.preference?.isCumulativeAlertEnabled
        def aggReviewHelpMap = Holders.config.aggregate.review.helpMap
        String angularAlertUrl = ""
        if (showCumulative) {
            params.callingScreen = Constants.Commons.TRIGGERED_ALERTS
            details()
        } else {
            String angularBaseURL = Holders.config.signal.angular.app.base.url
            if (angularBaseURL && angularBaseURL != "") {
                angularAlertUrl = angularBaseURL + Holders.config.signal.angular.app.alert.details
            }
            render(view: "review", model: [roles: aggregateCaseAlertService.reviewRoles(), aggReviewHelpMap: aggReviewHelpMap, angularAlertUrl: angularAlertUrl])
        }
    }

    def listAllResults() {
        User currentUser = userService.getUser()
        render(view: "executionStatus", model: [related: "listAllResultsPage", isAdmin: currentUser?.isAdmin()])
    }

    @Secured(['ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def viewConfig() {
        if (params.id) {
            if (params.from == "result") {
                def configurationInstance = aggregateCaseAlertService.getExecConfigurationById(params.id)
                redirect(controller: "configuration", action: "viewExecutedConfig", id: configurationInstance.id)
            } else {
                redirect(controller: "configuration", action: "view", id: params.id)
            }
        } else {
            flash.error = message(code: "app.configuration.id.null")
            redirect(controller: "configuration", action: "listAllResults")
        }

    }

    public getPublicForExecutedConfig() {
        return ExecutedConfiguration.get(params.id).isPublic ? message(code: "app.label.public") : message(code: "app.label.private")
    }

    // This is needed to properly re-read the data being created/edited after a transaction rollback.
    private Configuration populateModel(Configuration configurationInstance) {
        params?.name = params?.name?.trim()?.replaceAll("\\s{2,}", " ")
        //Do not bind in any other way because of the clone contained in the params
        bindData(configurationInstance, params, [exclude: ["productGroupSelection",'eventGroupSelection','selectedDatasource',"templateQueries", "tags", "isEnabled", "asOfVersionDate", "id", "template", "alertQueryValueLists", "sharedWith", "dataMiningVariable", "drugType","isProductMining","groupBySmq"]])
        bindSelectedDatasource(params.selectedDatasource, configurationInstance)
        boolean groupBySmq = (params.groupBySmq != 'PT')
        configurationInstance.groupBySmq = groupBySmq
        List<DateRangeEnum> dateRangeEnums = params.spotfireDaterange instanceof String ? [params.spotfireDaterange] : params.spotfireDaterange
        // Option values saved as list
        configurationInstance.drugType = params.drugType.toString().replace(', ',',').replace('[','').replace(']','')
        if(params.productGroupSelection != '[]'){
            configurationInstance.productGroupSelection = params.productGroupSelection
        } else {
            configurationInstance.productGroupSelection=null
        }
        if(params.dataMiningVariable == null || params.dataMiningVariable == "null"){
            configurationInstance.dataMiningVariable = null
        } else {
            configurationInstance.isProductMining = (params.isProductMining=='on') as Boolean
            configurationInstance.dataMiningVariable = params.dataMiningVariable
            if(params.productSelection =="" && params.productGroupSelection == '[]'){
                configurationInstance.isProductMining = false
            }
        }
        if(params.eventGroupSelection != '[]'){
            configurationInstance.eventGroupSelection = params.eventGroupSelection
        } else {
            configurationInstance.eventGroupSelection=null
        }
        bindSpotfireSettings(configurationInstance, params.boolean("enableSpotfire"), params.productSelection, params.spotfireType, dateRangeEnums)
        bindAsOfVersionDate(configurationInstance)
        if(configurationInstance.selectedDatasource != Constants.DataSource.FAERS){
            bindExistingTemplateQueryEdits(configurationInstance)
            bindNewTemplateQueries(configurationInstance)
        }
        if (params.alertDateRangeInformation) {
            assignParameterValuesToAlertQuery(configurationInstance)
        }
        updateTemplateQuerySequence(configurationInstance)
        Map productMap = [product: params.productSelection, productGroup: params.productGroupSelection]
        if(!configurationInstance.isMasterTemplateAlert){
            configurationInstance = userService.assignGroupOrAssignTo(params.assignedToValue, configurationInstance, productMap)
        } else {
            configurationInstance = userService.assignGroupOrAssignToForMasterTemplate(params.assignedToValue, configurationInstance, productMap)
        }
        setNextRunDateAndScheduleDateJSON(configurationInstance)
        userService.bindSharedWithConfiguration(configurationInstance, params.sharedWith, true, false, productMap, null, false, [:], configurationInstance.isMasterTemplateAlert)
        aggregateCaseAlertService.bindDatasheetData(configurationInstance, params.dataSheet ? [params.dataSheet]?.flatten() : null)
        if (params.selectedDatasheet && params.selectedDatasheet == 'on') {
            configurationInstance.datasheetType = params.allSheets ?: Constants.DatasheetOptions.CORE_SHEET
            configurationInstance.isDatasheetChecked = true
        } else {
            configurationInstance.isDatasheetChecked = false
        }
        if(configurationInstance.selectedDatasource == Constants.DataSource.JADER){
            configurationInstance.includeLockedVersion = false
        }
        if(params.studyDrugCases){
            configurationInstance.studyCases = true
            configurationInstance.onlyPoi = params.considerPOI ? true: false
            configurationInstance.ignoreStudyType = params.ignoreMedicationType ? true : false
        } else{
            configurationInstance.studyCases = false
            configurationInstance.onlyPoi = false
            configurationInstance.ignoreStudyType = false

        }
        if(!params.missedCases)
            configurationInstance.missedCases = false
        configurationInstance
    }

    void bindSelectedDatasource(def selectedDatasource, Configuration configurationInstance) {
        if (selectedDatasource instanceof String) {
            configurationInstance.selectedDatasource = selectedDatasource
        } else if (selectedDatasource instanceof String[]) {
                configurationInstance.selectedDatasource = selectedDatasource.join(",")
        }
    }

    @Secured(['ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    public enable() {

        Configuration configurationInstance = Configuration.get(params.id)
        if (!configurationInstance) {
            notFound()
            return
        }
        configurationInstance.setIsEnabled(true)
        populateModel(configurationInstance)
        setTemplateToAlert(configurationInstance)

        try {
            configurationInstance = (Configuration) CRUDService.update(configurationInstance)
        } catch (ValidationException ve) {
            Configuration originalConfiguration = Configuration.get(params.id)
            populateModel(originalConfiguration)
            originalConfiguration.errors = ve.errors
            renderOnErrorScenario(originalConfiguration)
            return
        }

        request.withFormat {
            form {
                flash.message = message(code: 'default.enabled.message', args: [message(code: 'configuration.label'), configurationInstance.name])
                redirect(action: "view", id: configurationInstance.id)
            }
            '*' { respond configurationInstance, [status: OK] }
        }

    }

    @Secured(['ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    public disable() {

        Configuration configurationInstance = Configuration.get(params.id)
        if (!configurationInstance) {
            notFound()
            return
        }
        configurationInstance.setIsEnabled(false)
        params.scheduleDateJSON = importConfigurationService.getDefaultScheduleJSON() //Done to make this in sync with import config behaviour PVS-61035
        configurationService.pushCustomAuditForUnscheduling(configurationInstance,"configuration")
        populateModel(configurationInstance)
        setTemplateToAlert(configurationInstance)
        configurationInstance.nextRunDate = null
        def isProductOrFamily = allowedDictionarySelection(configurationInstance)

        if (!isProductOrFamily) {
            flash.error = message(code: "app.label.product.family.error.message")
            renderOnErrorScenario(configurationInstance)
            return
        }

        try {
            configurationInstance = (Configuration) CRUDService.updateWithAuditLog(configurationInstance)
        } catch (ValidationException ve) {
            Configuration originalConfiguration = Configuration.get(params.id)
            populateModel(originalConfiguration)
            originalConfiguration.errors = ve.errors
            renderOnErrorScenario(originalConfiguration)
            return
        }
        def exConfig = ExecutedConfiguration.findByConfigId(configurationInstance?.id) //this is added to fetched old name of exconfig instead of comparing it with configuration name which is already updated hence the below condition is never satisfying
        if (params.name && !exConfig?.name.equals(params.name)) {
            alertService.renameExecConfig(configurationInstance.id, configurationInstance.name, params.name, configurationInstance.owner.id, Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
        }


        request.withFormat {
            form {
                flash.message = message(code: 'default.disabled.message', args: [message(code: 'configuration.label'), configurationInstance.name])
                redirect(action: "view", id: configurationInstance.id)
            }
            '*' { respond configurationInstance, [status: OK] }
        }

    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message',
                        args: [message(code: 'configuration.label'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    @Secured(['ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def changePriority(Long executedConfigId, String newValue, String justification, String alertList) {
        try {
            Date lastUpdated = null
            //Create the peHistory.
            Map peHistoryMap = [
                    "justification"   : justification,
                    "priority"        : Priority.findByDisplayName(newValue),
                    "change"          : Constants.HistoryType.PRIORITY,
            ]
            User loggedInUser = userService.getUser()
            JsonSlurper jsonSlurper = new JsonSlurper()
            ExecutedConfiguration executedConfig = aggregateCaseAlertService.getExecConfigurationById(executedConfigId)
            Long configId =  aggregateCaseAlertService.getAlertConfigObject(executedConfig)
            def alertListObj = jsonSlurper.parseText(alertList)
            List alertIdsList = []
            alertListObj.each {
                alertIdsList.add(it.alertId)
            }
            List alertListFromDB = AggregateCaseAlert.findAllByIdInList(alertIdsList)

            List peHistoryMapList = []

            alertListFromDB.each { AggregateCaseAlert alert ->

                User currUser = alert.assignedTo

                if (alert) {
                    def orgPriority = alert.priority?.value?.toString()

                    if (orgPriority != newValue) {
                        peHistoryMap = aggregateCaseAlertService.createBasePEHistoryMap(alert,peHistoryMap)
                        //Update the alert instance.
                        aggregateCaseAlertService.updateAggregateAlertStates(alert, peHistoryMap)
                        lastUpdated = alert.lastUpdated
                        peHistoryMap.put("createdTimestamp", lastUpdated)
                        peHistoryMapList.add(peHistoryMap)

                        activityService.createActivity(executedConfig, aggregateCaseAlertService.getActivityByType(ActivityTypeValue.PriorityChange),
                                userService.getUser(), "Priority changed from '$orgPriority' to '$newValue'",
                                justification, ['For Aggregate Alert'], alert.productName, alert.pt, currUser, null, alert.assignedToGroup, null, null, lastUpdated)
                    }
                }
            }
            //Batch persist the product event history.
            productEventHistoryService.batchPersistHistory(peHistoryMapList)

            render(contentType: "application/json", status: OK.value()) {
                [success: 'true', newValue: newValue]
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex)
            render(status: BAD_REQUEST)
        }
    }

    def changePriorityOfAlert(String selectedRows, Priority newPriority, String justification, Boolean isArchived) {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true, data:[])
        def domain = aggregateCaseAlertService.getDomainObject(isArchived)
        Date lastUpdated = null
        List<UndoableDisposition> undoableDispositionIdList = []

        try {
            //Create the peHistory.
            Map peHistoryMap = [
                    "priority"        : newPriority,
                    "justification"   : justification,
                    "change"          : Constants.HistoryType.PRIORITY,
            ]
            User loggedInUser = userService.getUser()
            List<Map> peHistoryMapList = []

            JSON.parse(selectedRows).each { Map<String, Long> selectedRow ->
                def alert = domain.get(selectedRow["alert.id"])
                ExecutedConfiguration executedConfigObj = ExecutedConfiguration.get(selectedRow["executedConfigObj.id"])
                Configuration configObj = Configuration.get(selectedRow["configObj.id"])
                if (alert) {
                    User currUser = alert.assignedTo
                    if (alert.priority != newPriority) {
                        Priority currentPriority = alert.priority
                        peHistoryMap = aggregateCaseAlertService.createBasePEHistoryMap(alert,peHistoryMap, isArchived)
                        peHistoryMapList << peHistoryMap
                        //Update the alert instance.
                        aggregateCaseAlertService.updateAggregateAlertStates(alert, peHistoryMap)
                        lastUpdated = alert.lastUpdated
                        peHistoryMap.put("createdTimestamp", lastUpdated)
                        ExecutedConfiguration executedConfig = executedConfigObj
                        activityService.createActivity(executedConfig, aggregateCaseAlertService.getActivityByType(ActivityTypeValue.PriorityChange),
                                userService.getUser(), "Priority changed from '$currentPriority' to '$newPriority'",
                                justification, ['For Aggregate Alert'], alert.productName, alert.pt, currUser, null, alert.assignedToGroup,null, null, lastUpdated)

                        // updating the due date of undoable disposition object with latest due date
                        alertService.updateUndoDispDueDate(Constants.AlertType.AGGREGATE_NEW, alert.id as Long, undoableDispositionIdList, alert.dueDate)

                        responseDTO.data << [id: alert.id, dueIn: alert.dueIn()]
                    }
                }
                peHistoryMap = [
                        "priority"        : newPriority,
                        "justification"   : justification,
                        "change"          : Constants.HistoryType.PRIORITY,
                ]
            }
            productEventHistoryService.batchPersistHistory(peHistoryMapList)
            if (undoableDispositionIdList) {
                aggregateCaseAlertService.notifyUndoableDisposition(undoableDispositionIdList)
            }
        } catch (Exception e) {
            e.printStackTrace()
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.priority.change.error")
        }
        render(responseDTO as JSON)
    }

    @Secured(['ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def exportReport() {
        def startTime=System.currentTimeSeconds()

        List agaList = []
        List newCountAgaList = []
        List listDateRange = []
        Map dataSourceMap = [:]
        List newFields = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', true).collect { it.name }
        Boolean cumulative = userService.getUser()?.preference?.isCumulativeAlertEnabled ?: false
        boolean isExcelExport = isExcelExportFormat(params.outputFormat)
        params.adhocRun = false
        def adhocRun = params.adhocRun
        Integer previousExecutionsToConsider = Holders.config.signal.quantitative.number.prev.columns
        Boolean groupBySmq = false
        ExecutedConfiguration executedConfiguration = null
        Map criteriaData
        params.isFaers = params.boolean("isFaers")
        params.isVaers = params.boolean("isVaers")
        params.isVigibase = params.boolean("isVigibase")
        Boolean isDashboard = params.callingScreen == Constants.Commons.DASHBOARD ? true : false

        if (!cumulative && !adhocRun?.toBoolean() && params.callingScreen != Constants.Commons.DASHBOARD && params.callingScreen != Constants.Commons.TAGS) {
            executedConfiguration = ExecutedConfiguration.findByIdAndIsEnabled(params.id, true)
            groupBySmq = executedConfiguration.groupBySmq
            def prevExecs = ExecutedConfiguration.findAllByName(executedConfiguration.name, [max: previousExecutionsToConsider])
            prevExecs.remove(executedConfiguration)
            prevExecs.each { it ->
                Date sdEx = it.executedAlertDateRangeInformation.dateRangeEndAbsolute
                listDateRange.add(Date.parse("dd-MMM-yyyy", DateUtil.toDateString1(sdEx)).format("dd/MMM/yy"))
            }
            def selectedDataSource = executedConfiguration?.selectedDatasource ?: Constants.DataSource.PVA
            if (selectedDataSource == Constants.DataSource.FAERS) {
                params.isFaers = true
            }
            if (selectedDataSource == Constants.DataSource.VAERS) {
                params.isVaers = true
            }
            if (selectedDataSource == Constants.DataSource.VIGIBASE) {
                params.isVigibase = true
            }
            if (selectedDataSource == Constants.DataSource.JADER) {
                params.isJader = true
            }
        } else if (cumulative && !adhocRun.toBoolean()) {
            Map freqDateRange = fetchDateRangeMap(params.frequency)
            freqDateRange.each { key, value ->
                if (key != 'exeRecent') {
                    listDateRange.add(value['startDate'].replaceAll("-", "/") + "-" + value['endDate'].replaceAll("-", "/"))
                }
            }
        }
        User user = userService.getUser()
        String timeZone = user?.preference?.timeZone
        params['listDateRange'] = listDateRange
        params['groupBySmq'] = groupBySmq
        AlertDataDTO alertDataDTO = new AlertDataDTO()
        alertDataDTO.params = params
        alertDataDTO.userId = user.id
        alertDataDTO.executedConfiguration = executedConfiguration
        alertDataDTO.execConfigId = executedConfiguration?.id
        alertDataDTO.cumulative = cumulative
        alertDataDTO.timeZone = timeZone
        alertDataDTO.isFromExport = true
        alertDataDTO.isJader = params.isJader
        alertDataDTO.workflowGroupId = user.workflowGroup.id
        alertDataDTO.groupIdList = user.groups.collect { it.id }

        alertDataDTO.domainName = aggregateCaseAlertService.getDomainObject(params.boolean('isArchived'))

        if (params.selectedCases) {
            List resultList = aggregateCaseAlertService.listSelectedAlerts(params.selectedCases, alertDataDTO.domainName)
            if(params.isJader){
                agaList = jaderAlertService.fetchResultAlertListJader(resultList, alertDataDTO, params.callingScreen)
            }else{
                agaList = aggregateCaseAlertService.fetchResultAlertList(resultList, alertDataDTO, params.callingScreen)
            }
          } else {
            Map filterMap = [:]
            if (params.filterList) {
                def jsonSlurper = new JsonSlurper()
                filterMap = jsonSlurper.parseText(params.filterList)
                if(filterMap.productName=="-1")
                    filterMap.remove("productName")
            }
            if(executedConfiguration?.masterExConfigId) {
                filterMap.remove("productName")
            }
            List dispositionFilters = getFiltersFromParams(params.isFilterRequest?.toBoolean(), params)

            if ((!cumulative && params.callingScreen != Constants.Commons.DASHBOARD) || params.adhocRun.toBoolean()) {
                executedConfiguration = ExecutedConfiguration.findByIdAndIsEnabled(params.id, true)
            }

            List<String> allowedProductsToUser = []
            if(alertService.isProductSecurity()){
                allowedProductsToUser = alertService.fetchAllowedProductsForConfiguration()
            }
            alertDataDTO.allowedProductsToUser = allowedProductsToUser
            alertDataDTO.filterMap = filterMap
            alertDataDTO.executedConfiguration = executedConfiguration
            alertDataDTO.execConfigId = executedConfiguration?.id
            alertDataDTO.configId = executedConfiguration?.configId
            alertDataDTO.orderColumnMap = [name: params.column, dir: params.sorting]
            alertDataDTO.userId = userService.getUser().id
            alertDataDTO.cumulative = cumulative
            alertDataDTO.dispositionFilters = dispositionFilters
            alertDataDTO.length = isDashboard ? 5000 : 10000  //As per PO suggestion applied max limit as 10000 for export from details screen
            Map filterCountAndList = alertService.getAlertFilterCountAndList(alertDataDTO, params.callingScreen)
            agaList = filterCountAndList.resultList

        }
        if(alertDataDTO.dispositionFilters?.isEmpty()) {
            agaList = []
        }
        ConcurrentLinkedQueue currentDispositionQueue = new ConcurrentLinkedQueue()
        ExecutorService executorService = Executors.newFixedThreadPool(6)
        List subGroupingFields = Holders.config.subgrouping.pva.subGroupColumnsList?.keySet() as List
        List ebgmSubGroupingFields = ["ebgm","eb05","eb95"]
        List relativeSubGroupingFields = ["ror","rorLci","rorUci"]
        Integer maxPrevExeFieldNum = aggregateCaseAlertService.getTheMaxExeNumber(params)
        maxPrevExeFieldNum = Math.min(maxPrevExeFieldNum, previousExecutionsToConsider)
        List<Future> futureList = []
        if(executedConfiguration?.selectedDatasource == Constants.DataSource.JADER){
            futureList = jaderAlertService.prepareJaderAlertExportData(agaList,isExcelExport,previousExecutionsToConsider,executorService)
        }else {
            boolean selectedDatasourceContainsFaers = executedConfiguration?.selectedDatasource?.contains("faers")
            boolean statisticsEbgmEnabled = grailsApplication.config.statistics.enable.ebgm
            boolean statisticsFaersEbgmEnabled = grailsApplication.config.statistics.faers.enable.ebgm
            boolean statisticsRrrOrRorEnabled = grailsApplication.config.statistics.enable.prr || grailsApplication.config.statistics.enable.ror
            Map subGroupColumnsCamelCaseMap = cacheService.getAllOtherSubGroupColumnsCamelCase(Constants.DataSource.PVA)
            Map subGroupMap = cacheService.getSubGroupMap()
            futureList = agaList.collect { it ->
                executorService.submit({ ->
                    it.name = it?.alertName
                    it.pt = it.preferredTerm
                    it.newStudyCount = isExcelExport ? "" + it.newStudyCount : "    " + it.newStudyCount + "\n    " + it.cumStudyCount
                    it.newCount = isExcelExport ? "" + it.newCount : "    " + it.newCount + "\n    " + it.cummCount
                    it.newPediatricCount = isExcelExport ? "" + it.newPediatricCount : "    " + it.newPediatricCount + "\n    " + it.cummPediatricCount
                    it.newInteractingCount = isExcelExport ? "" + it.newInteractingCount : "    " + it.newInteractingCount + "\n    " + it.cummInteractingCount
                    it.newSponCount = isExcelExport ? "" + it.newSponCount : "    " + it.newSponCount + "\n   " + it.cumSponCount
                    it.newSeriousCount = isExcelExport ? "" + it.newSeriousCount : "    " + it.newSeriousCount + "\n    " + it.cumSeriousCount
                    it.newFatalCount = isExcelExport ? "" + it.newFatalCount : "    " + it.newFatalCount + "\n    " + it.cumFatalCount
                    it.newGeriatricCount = isExcelExport ? "" + it.newGeriatricCount : "    " + it.newGeriatricCount + "\n    " + it.cumGeriatricCount
                    it.newNonSerious = isExcelExport ? "" + it.newNonSerious : "    " + it.newNonSerious + "\n    " + it.cumNonSerious
                    it.prrLCI = isExcelExport ? "" + it.prrLCI : "    " + it.prrLCI + "\n    " + it.prrUCI
                    it.rorLCI = isExcelExport ? "" + it.rorLCI : "    " + it.rorLCI + "\n    " + it.rorUCI
                    it.eb05 = isExcelExport ? "" + it.eb05 + "" : "    " + it.eb05 + "" + "\n    " + it.eb95 + ""
                    it.newProdCount = isExcelExport ? "" + it.newProdCount + "" : "    " + it.newProdCount + "" + "\n    " + it.cumProdCount + ""
                    it.freqPeriod = isExcelExport ? "" + it.freqPeriod + "" : "    " + it.freqPeriod + "" + "\n    " + it.cumFreqPeriod + ""
                    it.reviewedFreqPeriod = isExcelExport ? "" + it.reviewedFreqPeriod + "" : "    " + it.reviewedFreqPeriod + "" + "\n    " + it.reviewedCumFreqPeriod + ""
                    it.trendFlag = "" + it.trendFlag

                    it.ebgm = "" + it.ebgm
                    it.chiSquare = (it.chiSquare as String != '-' && it.chiSquare != -1) ? ("" + (it.chiSquare)) : it.chiSquare != -1 ? it.chiSquare : '-'
                    it.aValue = (it.aValue as String != '-' && it.aValue != -1) ? ("" + (it.aValue as Integer)) : it.aValue != -1 ? it.aValue : '-'
                    it.bValue = (it.bValue as String != '-' && it.bValue != -1) ? ("" + (it.bValue as Integer)) : it.bValue != -1 ? it.bValue : '-'
                    it.cValue = (it.cValue as String != '-' && it.cValue != -1) ? ("" + (it.cValue as Integer)) : it.cValue != -1 ? it.cValue : '-'
                    it.dValue = (it.dValue as String != '-' && it.dValue != -1) ? ("" + (it.dValue as Integer)) : it.dValue != -1 ? it.dValue : '-'
                    it.eValue = "" + it.eValue
                    it.rrValue = "" + it.rrValue

                    it.pecImpHigh = "" + it.highPecImp
                    it.lowPecImp = "" + it.pecImpLow
                    it.dueDate = it.dueIn + ""
                    it.soc = dataObjectService.getAbbreviationMap(it.soc) + ""

                    it.newCountFaers = isExcelExport ? "" + it.newCountFaers : "    " + it.newCountFaers + "\n    " + it.cummCountFaers
                    it.newSeriousCountFaers = isExcelExport ? "" + it.newSeriousCountFaers : "    " + it.newSeriousCountFaers + "\n    " + it.cumSeriousCountFaers
                    it.newStudyCountFaers = isExcelExport ? "" + it.newStudyCountFaers : "    " + it.newStudyCountFaers + "\n    " + it.cumStudyCountFaers
                    it.newPediatricCountFaers = isExcelExport ? "" + it.newPediatricCountFaers : "    " + it.newPediatricCountFaers + "\n    " + it.cummPediatricCountFaers
                    it.newInteractingCountFaers = isExcelExport ? "" + it.newInteractingCountFaers : "    " + it.newInteractingCountFaers + "\n    " + it.cummInteractingCountFaers
                    it.newSponCountFaers = isExcelExport ? "" + it.newSponCountFaers : "    " + it.newSponCountFaers + "\n    " + it.cumSponCountFaers
                    it.newGeriatricCountFaers = isExcelExport ? "" + it.newGeriatricCountFaers : "    " + it.newGeriatricCountFaers + "\n    " + it.cumGeriatricCountFaers
                    it.newNonSeriousFaers = isExcelExport ? "" + it.newNonSeriousFaers : "    " + it.newNonSeriousFaers + "\n    " + it.cumNonSeriousFaers
                    it.newFatalCountFaers = isExcelExport ? "" + it.newFatalCountFaers : "    " + it.newFatalCountFaers + "\n    " + it.cumFatalCountFaers
                    it.prrLCIFaers = isExcelExport ? "" + it.prrLCIFaers : "    " + it.prrLCIFaers + "\n    " + it.prrUCIFaers
                    it.prrValueFaers = "" + it.prrValueFaers
                    it.rorValueFaers = "" + it.rorValueFaers
                    it.rorLCIFaers = isExcelExport ? "" + it.rorLCIFaers : "    " + it.rorLCIFaers + "\n    " + it.rorUCIFaers
                    it.eb05Faers = isExcelExport ? "" + it.eb05Faers : "    " + it.eb05Faers + "" + "\n    " + it.eb95Faers + ""
                    it.ebgmFaers = "" + it.ebgmFaers
                    it.chiSquareFaers = "" + it.chiSquareFaers

                    it.newCountVaers = isExcelExport ? "" + it.newCountVaers : "    " + it.newCountVaers + "\n    " + it.cummCountVaers
                    it.newSeriousCountVaers = isExcelExport ? "" + it.newSeriousCountVaers : "    " + it.newSeriousCountVaers + "\n    " + it.cumSeriousCountVaers
                    it.newPediatricCountVaers = isExcelExport ? "" + it.newPediatricCountVaers : "    " + it.newPediatricCountVaers + "\n    " + it.cummPediatricCountVaers
                    it.newGeriatricCountVaers = isExcelExport ? "" + it.newGeriatricCountVaers : "    " + it.newGeriatricCountVaers + "\n    " + it.cumGeriatricCountVaers
                    it.newFatalCountVaers = isExcelExport ? "" + it.newFatalCountVaers : "    " + it.newFatalCountVaers + "\n    " + it.cumFatalCountVaers
                    it.prrLCIVaers = isExcelExport ? "" + it.prrLCIVaers : "    " + it.prrLCIVaers + "\n    " + it.prrUCIVaers
                    it.prrValueVaers = "" + it.prrValueVaers
                    it.rorValueVaers = "" + it.rorValueVaers
                    it.rorLCIVaers = isExcelExport ? "" + it.rorLCIVaers : "    " + it.rorLCIVaers + "\n    " + it.rorUCIVaers
                    it.eb05Vaers = isExcelExport ? "" + it.eb05Vaers : "    " + it.eb05Vaers + "" + "\n    " + it.eb95Vaers + ""
                    it.ebgmVaers = "" + it.ebgmVaers
                    it.chiSquareVaers = "" + it.chiSquareVaers
                    it.newCountVigibase = isExcelExport ? "" + it.newCountVigibase : "    " + it.newCountVigibase + "\n    " + it.cummCountVigibase
                    it.newSeriousCountVigibase = isExcelExport ? "" + it.newSeriousCountVigibase : "    " + it.newSeriousCountVigibase + "\n    " + it.cumSeriousCountVigibase
                    it.newPediatricCountVigibase = isExcelExport ? "" + it.newPediatricCountVigibase : "    " + it.newPediatricCountVigibase + "\n    " + it.cummPediatricCountVigibase
                    it.newGeriatricCountVigibase = isExcelExport ? "" + it.newGeriatricCountVigibase : "    " + it.newGeriatricCountVigibase + "\n    " + it.cumGeriatricCountVigibase
                    it.newFatalCountVigibase = isExcelExport ? "" + it.newFatalCountVigibase : "    " + it.newFatalCountVigibase + "\n    " + it.cumFatalCountVigibase
                    it.prrLCIVigibase = isExcelExport ? "" + it.prrLCIVigibase : "    " + it.prrLCIVigibase + "\n    " + it.prrUCIVigibase
                    it.prrValueVigibase = "" + it.prrValueVigibase
                    it.rorValueVigibase = "" + it.rorValueVigibase
                    it.rorLCIVigibase = isExcelExport ? "" + it.rorLCIVigibase : "    " + it.rorLCIVigibase + "\n    " + it.rorUCIVigibase
                    it.eb05Vigibase = isExcelExport ? "" + it.eb05Vigibase : "    " + it.eb05Vigibase + "" + "\n    " + it.eb95Vigibase + ""
                    it.ebgmVigibase = "" + it.ebgmVigibase
                    it.chiSquareVigibase = "" + it.chiSquareVigibase
                    it.newFatalEvdas = isExcelExport ? "" + it.newFatalEvdas : "    " + it.newFatalEvdas + "\n    " + it.totalFatalEvdas
                    it.newSeriousEvdas = isExcelExport ? "" + it.newSeriousEvdas : "    " + it.newSeriousEvdas + "\n    " + it.totalSeriousEvdas
                    it.newEvEvdas = isExcelExport ? "" + it.newEvEvdas : "    " + it.newEvEvdas + "\n    " + it.totalEvEvdas
                    it.newLitEvdas = isExcelExport ? "" + it.newLitEvdas : "    " + it.newLitEvdas + "\n    " + it.totalLitEvdas
                    it.newEeaEvdas = isExcelExport ? "" + it.newEeaEvdas : "    " + it.newEeaEvdas + "\n    " + it.totEeaEvdas
                    it.newHcpEvdas = isExcelExport ? "" + it.newHcpEvdas : "    " + it.newHcpEvdas + "\n    " + it.totHcpEvdas
                    it.newMedErrEvdas = isExcelExport ? "" + it.newMedErrEvdas : "    " + it.newMedErrEvdas + "\n    " + it.totMedErrEvdas
                    it.newObsEvdas = isExcelExport ? "" + it.newObsEvdas : "    " + it.newObsEvdas + "\n    " + it.totObsEvdas
                    it.newRcEvdas = isExcelExport ? "" + it.newRcEvdas : "    " + it.newRcEvdas + "\n    " + it.totRcEvdas
                    it.newPaedEvdas = isExcelExport ? "" + it.newPaedEvdas : "    " + it.newPaedEvdas + "\n    " + it.totPaedEvdas
                    it.newGeriaEvdas = isExcelExport ? "" + it.newGeriaEvdas : "    " + it.newGeriaEvdas + "\n    " + it.totGeriaEvdas
                    it.newSpontEvdas = isExcelExport ? "" + it.newSpontEvdas : "    " + it.newSpontEvdas + "\n    " + it.totSpontEvdas
                    Map countMap = new HashMap()
                    for (int i = 0; i < newFields.size(); i++) {
                        String newCount = '', cumCount = ''
                        String fieldName = newFields.get(i)
                        String fieldData = it[fieldName]

                        if (fieldName in ['hlt', 'hlgt', 'smqNarrow']) {
                            newCount = fieldData?.toString()?.replaceAll(/<br>|<BR>/, "\n") ?: Constants.Commons.DASH_STRING
                            countMap.put(fieldName, newCount)
                        } else {
                            if (fieldData&& !fieldData.equals(Constants.Commons.DASH_STRING)) {
                                Map fieldDataMap = JSON.parse(fieldData) as Map
                                newCount = fieldDataMap.new
                                cumCount = fieldDataMap.cum
                            } else {
                                newCount = Constants.Commons.DASH_STRING
                                cumCount = Constants.Commons.DASH_STRING
                            }
                            String newCountString = fieldName.toString()
                            String cumCountString = newCountString.replace("new", "cum")

                            countMap.put(newCountString, isExcelExport ? "" + newCount : "    " + newCount + "\n    " + cumCount)
                            if(isExcelExport){
                                countMap.put(cumCountString, isExcelExport ? "" + cumCount : "    " + newCount + "\n    " + cumCount)
                            }
                        }

                    }
                    it << countMap

                    if (isExcelExport) {
                        it.cumGeriatricCount = "" + it.cumGeriatricCount
                        it.cumNonSerious = "" + it.cumNonSerious
                        it.cumStudyCount = "" + it.cumStudyCount
                        it.cummCount = "" + it.cummCount
                        it.cummPediatricCount = "" + it.cummPediatricCount
                        it.cummInteractingCount = "" + it.cummInteractingCount
                        it.cumSponCount = "" + it.cumSponCount
                        it.cumSeriousCount = "" + it.cumSeriousCount
                        it.cumFatalCount = "" + it.cumFatalCount
                        it.prrUCI = "" + it.prrUCI
                        it.rorUCI = "" + it.rorUCI
                        it.eb95 = "" + it.eb95
                        it.cumProdCount = "" + it.cumProdCount
                        it.cumFreqPeriod = "" + it.cumFreqPeriod
                        it.reviewedCumFreqPeriod = "" + it.reviewedCumFreqPeriod
                        it.cummCountFaers = "" + it.cummCountFaers
                        it.cumSeriousCountFaers = "" + it.cumSeriousCountFaers
                        it.cumStudyCountFaers = "" + it.cumStudyCountFaers
                        it.cummPediatricCountFaers = "" + it.cummPediatricCountFaers
                        it.cummInteractingCountFaers = "" + it.cummInteractingCountFaers
                        it.cumSponCountFaers = "" + it.cumSponCountFaers
                        it.cumFatalCountFaers = "" + it.cumFatalCountFaers
                        it.cumGeriatricCountFaers = "" + it.cumGeriatricCountFaers
                        it.cumNonSeriousFaers = "" + it.cumNonSeriousFaers
                        it.prrUCIFaers = "" + it.prrUCIFaers
                        it.rorUCIFaers = "" + it.rorUCIFaers
                        it.eb95Faers = "" + it.eb95Faers
                        it.cummCountVaers = "" + it.cummCountVaers
                        it.cumSeriousCountVaers = "" + it.cumSeriousCountVaers
                        it.cummPediatricCountVaers = "" + it.cummPediatricCountVaers
                        it.cumGeriatricCountVaers = "" + it.cumGeriatricCountVaers
                        it.cumFatalCountVaers = "" + it.cumFatalCountVaers
                        it.prrUCIVaers = "" + it.prrUCIVaers
                        it.rorUCIVaers = "" + it.rorUCIVaers
                        it.eb95Vaers = "" + it.eb95Vaers
                        it.cummCountVigibase = "" + it.cummCountVigibase
                        it.cumSeriousCountVigibase = "" + it.cumSeriousCountVigibase
                        it.cummPediatricCountVigibase = "" + it.cummPediatricCountVigibase
                        it.cumGeriatricCountVigibase = "" + it.cumGeriatricCountVigibase
                        it.cumFatalCountVigibase = "" + it.cumFatalCountVigibase
                        it.prrUCIVigibase = "" + it.prrUCIVigibase
                        it.rorUCIVigibase = "" + it.rorUCIVigibase
                        it.eb95Vigibase = "" + it.eb95Vigibase
                        it.totalFatalEvdas = "" + it.totalFatalEvdas
                        it.totalSeriousEvdas = "" + it.totalSeriousEvdas
                        it.totalEvEvdas = "" + it.totalEvEvdas
                        it.totalLitEvdas = "" + it.totalLitEvdas
                        it.totEeaEvdas = "" + it.totEeaEvdas
                        it.totHcpEvdas = "" + it.totHcpEvdas
                        it.totMedErrEvdas = "" + it.totMedErrEvdas
                        it.totObsEvdas = "" + it.totObsEvdas
                        it.totRcEvdas = "" + it.totRcEvdas
                        it.totPaedEvdas = "" + it.totPaedEvdas
                        it.totGeriaEvdas = "" + it.totGeriaEvdas
                        it.totSpontEvdas = "" + it.totSpontEvdas
                    }
                    if (it.pecImpNumHigh && it.pecImpNumHigh != '-') {
                        if (it.rationale && it.rationale != '-') {
                            it.rationale = it.rationale + " " + it.pecImpNumHigh
                        } else {
                            it.rationale = it.pecImpNumHigh
                        }
                    } else {
                        it.rationale = Constants.Commons.BLANK_STRING
                    }

                    if (groupBySmq) {
                        it.rationale = ""
                    }
                    String signalTopics = ""
                    signalTopics = it.signalsAndTopics.collect { it.name }?.join(",")
                    it.signalsAndTopics = signalTopics
                    it.assignedTo = userService.getAssignedToName(it)
                    List<String> tagsList = []
                    it.alertTags.each { tag ->
                        String tagString = ""
                        if (tag.subTagText == null) {
                            tagString = tagString + tag.tagText + tag.privateUser + tag.tagType
                        } else {
                            String subTags = tag.subTagText.split(";").join("(S);")
                            tagString = tagString + tag.tagText + tag.privateUser + tag.tagType + " : " + subTags + "(S)"
                        }
                        tagsList.add(tagString)

                    }
                    it.alertTags = tagsList.join(", ")
                    it.currentDisposition = it.disposition
                    if (statisticsEbgmEnabled || statisticsFaersEbgmEnabled) {
                        if (selectedDatasourceContainsFaers) {
                            it << fetchEachSubCategory(subGroupMap, Holders.config.subgrouping.faers.ageGroup.name, '', it, "Age")
                            it << fetchEachSubCategory(subGroupMap, Holders.config.subgrouping.faers.gender.name, '', it, "Gender")
                            if (executedConfiguration?.selectedDatasource?.contains("pva")) {
                                it << fetchEachSubCategory(subGroupMap, Holders.config.subgrouping.ageGroup.name, '', it, "Age")
                                it << fetchEachSubCategory(subGroupMap, Holders.config.subgrouping.gender.name, '', it, "Gender")
                                ebgmSubGroupingFields?.each { category ->
                                    it << fetchAllSubCategoryColumn(subGroupColumnsCamelCaseMap, category, null, '', it)
                                }
                            }
                        } else {
                            it << fetchEachSubCategory(subGroupMap, Holders.config.subgrouping.ageGroup.name, '', it, "Age")
                            it << fetchEachSubCategory(subGroupMap, Holders.config.subgrouping.gender.name, '', it, "Gender")
                            ebgmSubGroupingFields?.each { category ->
                                it << fetchAllSubCategoryColumn(subGroupColumnsCamelCaseMap as Map, category, null, '', it)
                            }
                        }
                    }
                    if ((statisticsRrrOrRorEnabled)) {
                        subGroupingFields?.each { category ->
                            it << fetchAllSubCategoryColumn(subGroupColumnsCamelCaseMap, category, null, '', it)
                        }
                        relativeSubGroupingFields?.each { category ->
                            it << fetchAllSubCategoryColumn(subGroupColumnsCamelCaseMap, category, null, "Rel", it)
                        }
                    }
                    currentDispositionQueue.add(it?.currentDisposition)


                    if (maxPrevExeFieldNum >= 0) {
                        (0..maxPrevExeFieldNum).each { exeNum ->
                            String exeName = 'exe' + exeNum
                            it.put(exeName + 'newSponCount', isExcelExport ? "" + it[exeName]?.newSponCount : "    " + it[exeName]?.newSponCount + "\n    " + it[exeName]?.cumSponCount)
                            it.put(exeName + 'newSeriousCount', isExcelExport ? "" + it[exeName]?.newSeriousCount : "    " + it[exeName]?.newSeriousCount + "\n    " + it[exeName]?.cumSeriousCount)
                            it.put(exeName + 'newFatalCount', isExcelExport ? "" + it[exeName]?.newFatalCount : "    " + it[exeName]?.newFatalCount + "\n    " + it[exeName]?.cumFatalCount)
                            it.put(exeName + 'newStudyCount', isExcelExport ? "" + it[exeName]?.newStudyCount : "    " + it[exeName]?.newStudyCount + "\n    " + it[exeName]?.cumStudyCount)
                            it.put(exeName + 'newCount', isExcelExport ? "" + it[exeName]?.newCount : "    " + it[exeName]?.newCount + "\n    " + it[exeName]?.cummCount)
                            it.put(exeName + 'newInteractingCount', isExcelExport ? "" + it[exeName]?.newInteractingCount : "    " + it[exeName]?.newInteractingCount + "\n    " + it[exeName]?.cummInteractingCount)
                            it.put(exeName + 'newPediatricCount', isExcelExport ? "" + it[exeName]?.newPediatricCount : "    " + it[exeName]?.newPediatricCount + "\n    " + it[exeName]?.cummPediatricCount)
                            it.put(exeName + 'newNonSerious', isExcelExport ? "" + it[exeName]?.newNonSerious : "    " + it[exeName]?.newNonSerious + "\n    " + it[exeName]?.cumNonSerious)
                            it.put(exeName + 'newGeriatricCount', isExcelExport ? "" + it[exeName]?.newGeriatricCount : "    " + it[exeName]?.newGeriatricCount + "\n    " + it[exeName]?.cumGeriatricCount)
                            it.put(exeName + 'prrLCI', isExcelExport ? "" + it[exeName]?.prrLCI : "    " + it[exeName]?.prrLCI + "\n    " + it[exeName]?.prrUCI)
                            it.put(exeName + 'prrValue', "" + it[exeName]?.prrValue)
                            it.put(exeName + 'rorLCI', isExcelExport ? "" + it[exeName]?.rorLCI : "    " + it[exeName]?.rorLCI + "\n    " + it[exeName]?.rorUCI)
                            it.put(exeName + 'rorValue', "" + it[exeName]?.rorValue)
                            it.put(exeName + 'ebgm', "" + it[exeName]?.ebgm)
                            it.put(exeName + 'eb05', isExcelExport ? "" + it[exeName]?.eb05 : "    " + it[exeName]?.eb05 + "\n    " + it[exeName]?.eb95)
                            it.put(exeName + 'eb05Age', "    " + it[exeName]?.eb05Age)
                            it.put(exeName + 'eb05Gender', "    " + it[exeName]?.eb05Gender)
                            it.put(exeName + 'rrValue', "    " + it[exeName]?.rrValue)
                            it.put(exeName + 'newCountFaers', isExcelExport ? "" + it[exeName]?.newCountFaers : "    " + it[exeName]?.newCountFaers + "\n    " + it[exeName]?.cummCountFaers)
                            it.put(exeName + 'newSeriousCountFaers', isExcelExport ? "" + it[exeName]?.newSeriousCountFaers : "    " + it[exeName]?.newSeriousCountFaers + "\n    " + it[exeName]?.cumSeriousCountFaers)
                            it.put(exeName + 'eb05Faers', isExcelExport ? "" + it[exeName]?.eb05Faers : "    " + it[exeName]?.eb05Faers + "\n    " + it[exeName]?.eb95Faers)
                            it.put(exeName + 'newSponCountFaers', isExcelExport ? "" + it[exeName]?.newSponCountFaers : "    " + it[exeName]?.newSponCountFaers + "\n    " + it[exeName]?.cumSponCountFaers)
                            it.put(exeName + 'newStudyCountFaers', isExcelExport ? "" + it[exeName]?.newStudyCountFaers : "    " + it[exeName]?.newStudyCountFaers + "\n    " + it[exeName]?.cumStudyCountFaers)
                            it.put(exeName + 'prrLCIFaers', isExcelExport ? "" + it[exeName]?.prrLCIFaers : "    " + it[exeName]?.prrLCIFaers + "\n    " + it[exeName]?.prrUCIFaers)
                            it.put(exeName + 'newPediatricCountFaers', isExcelExport ? "" + it[exeName]?.newPediatricCountFaers : "    " + it[exeName]?.newPediatricCountFaers + "\n    " + it[exeName]?.cummPediatricCountFaers)
                            it.put(exeName + 'newInteractingCountFaers', isExcelExport ? "" + it[exeName]?.newInteractingCountFaers : "    " + it[exeName]?.newInteractingCountFaers + "\n    " + it[exeName]?.cummInteractingCountFaers)
                            it.put(exeName + 'newFatalCountFaers', isExcelExport ? "" + it[exeName]?.newFatalCountFaers : "    " + it[exeName]?.newFatalCountFaers + "\n    " + it[exeName]?.cumFatalCountFaers)
                            it.put(exeName + 'newNonSeriousFaers', isExcelExport ? "" + it[exeName]?.newNonSeriousFaers : "    " + it[exeName]?.newNonSeriousFaers + "\n    " + it[exeName]?.cumNonSeriousFaers)
                            it.put(exeName + 'newGeriatricCountFaers', isExcelExport ? "" + it[exeName]?.newGeriatricCountFaers : "    " + it[exeName]?.newGeriatricCountFaers + "\n    " + it[exeName]?.cumGeriatricCountFaers)
                            it.put(exeName + 'rorLCIFaers', isExcelExport ? "" + it[exeName]?.rorLCIFaers : "    " + it[exeName]?.rorLCIFaers + "\n    " + it[exeName]?.rorUCIFaers)
                            it.put(exeName + 'newCountVaers', isExcelExport ? "" + it[exeName]?.newCountVaers : "    " + it[exeName]?.newCountVaers + "\n    " + it[exeName]?.cummCountVaers)
                            it.put(exeName + 'newSeriousCountVaers', isExcelExport ? "" + it[exeName]?.newSeriousCountVaers : "    " + it[exeName]?.newSeriousCountVaers + "\n    " + it[exeName]?.cumSeriousCountVaers)
                            it.put(exeName + 'eb05Vaers', isExcelExport ? "" + it[exeName]?.eb05Vaers : "    " + it[exeName]?.eb05Vaers + "\n    " + it[exeName]?.eb95Vaers)
                            it.put(exeName + 'prrLCIVaers', isExcelExport ? "" + it[exeName]?.prrLCIVaers : "    " + it[exeName]?.prrLCIVaers + "\n    " + it[exeName]?.prrUCIVaers)
                            it.put(exeName + 'newPediatricCountVaers', isExcelExport ? "" + it[exeName]?.newPediatricCountVaers : "    " + it[exeName]?.newPediatricCountVaers + "\n    " + it[exeName]?.cummPediatricCountVaers)
                            it.put(exeName + 'newFatalCountVaers', isExcelExport ? "" + it[exeName]?.newFatalCountVaers : "    " + it[exeName]?.newFatalCountVaers + "\n    " + it[exeName]?.cumFatalCountVaers)
                            it.put(exeName + 'newGeriatricCountVaers', isExcelExport ? "" + it[exeName]?.newGeriatricCountVaers : "    " + it[exeName]?.newGeriatricCountVaers + "\n    " + it[exeName]?.cumGeriatricCountVaers)
                            it.put(exeName + 'rorLCIVaers', isExcelExport ? "" + it[exeName]?.rorLCIVaers : "    " + it[exeName]?.rorLCIVaers + "\n    " + it[exeName]?.rorUCIVaers)
                            it.put(exeName + 'newCountVigibase', isExcelExport ? "" + it[exeName]?.newCountVigibase : "    " + it[exeName]?.newCountVigibase + "\n    " + it[exeName]?.cummCountVigibase)
                            it.put(exeName + 'newSeriousCountVigibase', isExcelExport ? "" + it[exeName]?.newSeriousCountVigibase : "    " + it[exeName]?.newSeriousCountVigibase + "\n    " + it[exeName]?.cumSeriousCountVigibase)
                            it.put(exeName + 'eb05Vigibase', isExcelExport ? "" + it[exeName]?.eb05Vigibase : "    " + it[exeName]?.eb05Vigibase + "\n    " + it[exeName]?.eb95Vigibase)
                            it.put(exeName + 'prrLCIVigibase', isExcelExport ? "" + it[exeName]?.prrLCIVigibase : "    " + it[exeName]?.prrLCIVigibase + "\n    " + it[exeName]?.prrUCIVigibase)
                            it.put(exeName + 'newPediatricCountVigibase', isExcelExport ? "" + it[exeName]?.newPediatricCountVigibase : "    " + it[exeName]?.newPediatricCountVigibase + "\n    " + it[exeName]?.cummPediatricCountVigibase)
                            it.put(exeName + 'newFatalCountVigibase', isExcelExport ? "" + it[exeName]?.newFatalCountVigibase : "    " + it[exeName]?.newFatalCountVigibase + "\n    " + it[exeName]?.cumFatalCountVigibase)
                            it.put(exeName + 'newGeriatricCountVigibase', isExcelExport ? "" + it[exeName]?.newGeriatricCountVigibase : "    " + it[exeName]?.newGeriatricCountVigibase + "\n    " + it[exeName]?.cumGeriatricCountVigibase)
                            it.put(exeName + 'rorLCIVigibase', isExcelExport ? "" + it[exeName]?.rorLCIVigibase : "    " + it[exeName]?.rorLCIVigibase + "\n    " + it[exeName]?.rorUCIVigibase)
                            it.put(exeName + 'newEeaEvdas', isExcelExport ? "" + it[exeName]?.newEeaEvdas : "    " + it[exeName]?.newEeaEvdas + "\n    " + it[exeName]?.totEeaEvdas)
                            it.put(exeName + 'newHcpEvdas', isExcelExport ? "" + it[exeName]?.newHcpEvdas : "    " + it[exeName]?.newHcpEvdas + "\n    " + it[exeName]?.totHcpEvdas)
                            it.put(exeName + 'newSeriousEvdas', isExcelExport ? "" + it[exeName]?.newSeriousEvdas : "    " + it[exeName]?.newSeriousEvdas + "\n    " + it[exeName]?.totalSeriousEvdas)
                            it.put(exeName + 'newMedErrEvdas', isExcelExport ? "" + it[exeName]?.newMedErrEvdas : "    " + it[exeName]?.newMedErrEvdas + "\n    " + it[exeName]?.totMedErrEvdas)
                            it.put(exeName + 'newObsEvdas', isExcelExport ? "" + it[exeName]?.newObsEvdas : "    " + it[exeName]?.newObsEvdas + "\n    " + it[exeName]?.totObsEvdas)
                            it.put(exeName + 'newFatalEvdas', isExcelExport ? "" + it[exeName]?.newFatalEvdas : "    " + it[exeName]?.newFatalEvdas + "\n    " + it[exeName]?.totalFatalEvdas)
                            it.put(exeName + 'newRcEvdas', isExcelExport ? "" + it[exeName]?.newRcEvdas : "    " + it[exeName]?.newRcEvdas + "\n    " + it[exeName]?.totRcEvdas)
                            it.put(exeName + 'newLitEvdas', isExcelExport ? "" + it[exeName]?.newLitEvdas : "    " + it[exeName]?.newLitEvdas + "\n    " + it[exeName]?.totalLitEvdas)
                            it.put(exeName + 'newPaedEvdas', isExcelExport ? "" + it[exeName]?.newPaedEvdas : "    " + it[exeName]?.newPaedEvdas + "\n    " + it[exeName]?.totPaedEvdas)
                            it.put(exeName + 'newGeriaEvdas', isExcelExport ? "" + it[exeName]?.newGeriaEvdas : "    " + it[exeName]?.newGeriaEvdas + "\n    " + it[exeName]?.totGeriaEvdas)
                            it.put(exeName + 'newSpontEvdas', isExcelExport ? "" + it[exeName]?.newSpontEvdas : "    " + it[exeName]?.newSpontEvdas + "\n    " + it[exeName]?.totSpontEvdas)
                            it.put(exeName + 'newEvEvdas', isExcelExport ? "" + it[exeName]?.newEvEvdas : "    " + it[exeName]?.newEvEvdas + "\n    " + it[exeName]?.totalEvEvdas)
                            it.put(exeName + 'ebgmFaers', "" + it[exeName]?.ebgmFaers)
                            it.put(exeName + 'ebgmVaers', "" + it[exeName]?.ebgmVaers)
                            it.put(exeName + 'prrValueVaers', "" + it[exeName]?.prrValueVaers)
                            it.put(exeName + 'rorValueVaers', "" + it[exeName]?.rorValueVaers)
                            it.put(exeName + 'chiSquareVaers', "" + it[exeName]?.chiSquareVaers)
                            it.put(exeName + 'chiSquare', "" + it[exeName]?.chiSquare)
                            it.put(exeName + 'chiSquareFaers', "" + it[exeName]?.chiSquareFaers)
                            it.put(exeName + 'ebgmVigibase', "" + it[exeName]?.ebgmVigibase)
                            it.put(exeName + 'prrValueVigibase', "" + it[exeName]?.prrValueVigibase)
                            it.put(exeName + 'rorValueVigibase', "" + it[exeName]?.rorValueVigibase)
                            it.put(exeName + 'chiSquareVigibase', "" + it[exeName]?.chiSquareVigibase)
                            it.put(exeName + 'prrValueFaers', "" + it[exeName]?.prrValueFaers)
                            it.put(exeName + 'rorValueFaers', "" + it[exeName]?.rorValueFaers)

                            if (it[exeName]) {
                                Map m2 = new HashMap()
                                for (int i = 0; i < newFields.size(); i++) {
                                    String newCount = '', cumCount = ''

                                    if (it[newFields.get(i)]) {
                                        if (newFields.get(i) in ['hlt', 'hlgt', 'smqNarrow']) {
                                            newCount = it[exeName][newFields.get(i)]
                                            if (!newCount) {
                                                newCount = Constants.Commons.DASH_STRING
                                            }
                                        } else {
                                            if (it[exeName][newFields.get(i)] && !it[exeName][newFields.get(i)].equals(Constants.Commons.DASH_STRING)) {

                                                newCount = it[exeName][newFields.get(i)]
                                                cumCount = it[exeName][newFields.get(i).replace("new", "cum")]
                                            }

                                        }
                                    } else {
                                        newCount = Constants.Commons.DASH_STRING
                                        cumCount = Constants.Commons.DASH_STRING
                                    }

                                    if (newFields.get(i) in ['hlt', 'hlgt', 'smqNarrow']) {
                                        m2.put(exeName + newFields.get(i), newCount)
                                    } else {
                                        m2.put(exeName + newFields.get(i), isExcelExport ? "" + newCount : "    " + newCount + "\n    " + cumCount)
                                        m2.put(exeName + newFields.get(i).replace("new", "cum"), cumCount)
                                    }

                                }
                                it << m2
                            }


                            if (isExcelExport) {
                                it.put(exeName + 'cummCount', "" + it[exeName]?.cummCount)
                                it.put(exeName + 'cummPediatricCount', "" + it[exeName]?.cummPediatricCount)
                                it.put(exeName + 'cummInteractingCount', "" + it[exeName]?.cummInteractingCount)
                                it.put(exeName + 'cumNonSerious', "" + it[exeName]?.cumNonSerious)
                                it.put(exeName + 'cumGeriatricCount', "" + it[exeName]?.cumGeriatricCount)
                                it.put(exeName + 'cumSponCount', "" + it[exeName]?.cumSponCount)
                                it.put(exeName + 'cumSeriousCount', "" + it[exeName]?.cumSeriousCount)
                                it.put(exeName + 'cumFatalCount', "" + it[exeName]?.cumFatalCount)
                                it.put(exeName + 'cumStudyCount', "" + it[exeName]?.cumStudyCount)
                                it.put(exeName + 'prrUCI', "" + it[exeName]?.prrUCI)
                                it.put(exeName + 'rorUCI', "" + it[exeName]?.rorUCI)
                                it.put(exeName + 'eb95', "" + it[exeName]?.eb95)
                                it.put(exeName + 'cummCountFaers', "" + it[exeName]?.cummCountFaers)
                                it.put(exeName + 'cumSeriousCountFaers', "" + it[exeName]?.cumSeriousCountFaers)
                                it.put(exeName + 'eb95Faers', "" + it[exeName]?.eb95Faers)
                                it.put(exeName + 'cumSponCountFaers', "" + it[exeName]?.cumSponCountFaers)
                                it.put(exeName + 'cumStudyCountFaers', "" + it[exeName]?.cumStudyCountFaers)
                                it.put(exeName + 'prrUCIFaers', "" + it[exeName]?.prrUCIFaers)
                                it.put(exeName + 'cummPediatricCountFaers', "" + it[exeName]?.cummPediatricCountFaers)
                                it.put(exeName + 'cummInteractingCountFaers', "" + it[exeName]?.cummInteractingCountFaers)
                                it.put(exeName + 'cumFatalCountFaers', "" + it[exeName]?.cumFatalCountFaers)
                                it.put(exeName + 'cumGeriatricCountFaers', "" + it[exeName]?.cumGeriatricCountFaers)
                                it.put(exeName + 'cumNonSeriousFaers', "" + it[exeName]?.cumNonSeriousFaers)
                                it.put(exeName + 'rorUCIFaers', "" + it[exeName]?.rorUCIFaers)
                                it.put(exeName + 'cummCountVaers', "" + it[exeName]?.cummCountVaers)
                                it.put(exeName + 'cumSeriousCountVaers', "" + it[exeName]?.cumSeriousCountVaers)
                                it.put(exeName + 'eb95Vaers', "" + it[exeName]?.eb95Vaers)
                                it.put(exeName + 'prrUCIVaers', "" + it[exeName]?.prrUCIVaers)
                                it.put(exeName + 'cummPediatricCountVaers', "" + it[exeName]?.cummPediatricCountVaers)
                                it.put(exeName + 'cumFatalCountVaers', "" + it[exeName]?.cumFatalCountVaers)
                                it.put(exeName + 'cumGeriatricCountVaers', "" + it[exeName]?.cumGeriatricCountVaers)
                                it.put(exeName + 'rorUCIVaers', "" + it[exeName]?.rorUCIVaers)
                                it.put(exeName + 'cummCountVigibase', "" + it[exeName]?.cummCountVigibase)
                                it.put(exeName + 'cumSeriousCountVigibase', "" + it[exeName]?.cumSeriousCountVigibase)
                                it.put(exeName + 'eb95Vigibase', "" + it[exeName]?.eb95Vigibase)
                                it.put(exeName + 'prrUCIVigibase', "" + it[exeName]?.prrUCIVigibase)
                                it.put(exeName + 'cummPediatricCountVigibase', "" + it[exeName]?.cummPediatricCountVigibase)
                                it.put(exeName + 'cumFatalCountVigibase', "" + it[exeName]?.cumFatalCountVigibase)
                                it.put(exeName + 'cumGeriatricCountVigibase', "" + it[exeName]?.cumGeriatricCountVigibase)
                                it.put(exeName + 'rorUCIVigibase', "" + it[exeName]?.rorUCIVigibase)
                                it.put(exeName + 'totEeaEvdas', "" + it[exeName]?.totEeaEvdas)
                                it.put(exeName + 'totHcpEvdas', "" + it[exeName]?.totHcpEvdas)
                                it.put(exeName + 'totalSeriousEvdas', "" + it[exeName]?.totalSeriousEvdas)
                                it.put(exeName + 'totMedErrEvdas', "" + it[exeName]?.totMedErrEvdas)
                                it.put(exeName + 'totObsEvdas', "" + it[exeName]?.totObsEvdas)
                                it.put(exeName + 'totalFatalEvdas', "" + it[exeName]?.totalFatalEvdas)
                                it.put(exeName + 'totRcEvdas', "" + it[exeName]?.totRcEvdas)
                                it.put(exeName + 'totalLitEvdas', "" + it[exeName]?.totalLitEvdas)
                                it.put(exeName + 'totPaedEvdas', "" + it[exeName]?.totPaedEvdas)
                                it.put(exeName + 'totGeriaEvdas', "" + it[exeName]?.totGeriaEvdas)
                                it.put(exeName + 'totSpontEvdas', "" + it[exeName]?.totSpontEvdas)
                                it.put(exeName + 'totalEvEvdas', "" + it[exeName]?.totalEvEvdas)

                                if (it[exeName]) {
                                    Map m1 = new HashMap()
                                    for (int i = 0; i < newFields.size(); i++) {
                                        String newCountString = newFields.get(i).toString()
                                        String cumCountString = newCountString.replace("new", "cum")
                                        if (it[exeName][cumCountString]) {
                                            m1.put(exeName + cumCountString, it[exeName][cumCountString])
                                        } else {
                                            m1.put(exeName + cumCountString, Constants.Commons.DASH_STRING)
                                        }
                                    }
                                    it << m1
                                }


                            }
                            if (statisticsEbgmEnabled || statisticsFaersEbgmEnabled) {
                                if (selectedDatasourceContainsFaers) {
                                    it << fetchEachSubCategory(subGroupMap, Holders.config.subgrouping.faers.ageGroup.name, exeName, it, "Age")
                                    it << fetchEachSubCategory(subGroupMap, Holders.config.subgrouping.faers.gender.name, exeName, it, "Gender")
                                    if (executedConfiguration?.selectedDatasource?.contains("pva")) {
                                        it << fetchEachSubCategory(subGroupMap, Holders.config.subgrouping.ageGroup.name, '', it, "Age")
                                        it << fetchEachSubCategory(subGroupMap, Holders.config.subgrouping.gender.name, '', it, "Gender")
                                        ebgmSubGroupingFields?.each { category ->
                                            it << fetchAllSubCategoryColumn(subGroupColumnsCamelCaseMap, category, exeName, '', it)
                                        }
                                    }
                                } else {
                                    it << fetchEachSubCategory(subGroupMap, Holders.config.subgrouping.ageGroup.name, exeName, it, "Age")
                                    it << fetchEachSubCategory(subGroupMap, Holders.config.subgrouping.gender.name, exeName, it, "Gender")
                                    ebgmSubGroupingFields?.each { category ->
                                        it << fetchAllSubCategoryColumn(subGroupColumnsCamelCaseMap, category, exeName, '', it)
                                    }
                                }
                            }
                            if ((grailsApplication.config.statistics.enable.prr || grailsApplication.config.statistics.enable.ror)) {
                                subGroupingFields?.each { category ->
                                    it << fetchAllSubCategoryColumn(subGroupColumnsCamelCaseMap, category, exeName, '', it)
                                }
                                relativeSubGroupingFields?.each { category ->
                                    it << fetchAllSubCategoryColumn(subGroupColumnsCamelCaseMap, category, exeName, Constants.SubGroups.REL, it)
                                }
                            }
                        }
                    }
                } as Runnable)
            }
        }
        futureList?.each {
            it.get()
        }
        executorService.shutdown()
        Map reportParamsMap
        String datasheets = ""
        String enabledSheet = Constants.DatasheetOptions.CORE_SHEET
        if (!TextUtils.isEmpty(executedConfiguration?.selectedDataSheet)) {
            datasheets = dataSheetService.formatDatasheetMap(executedConfiguration)?.text?.join(',')
        } else if (executedConfiguration?.selectedDatasource?.contains(Constants.DataSource.PVA)) {
            if(!TextUtils.isEmpty(executedConfiguration.productGroupSelection)  || !TextUtils.isEmpty(executedConfiguration.productSelection)) {
                Boolean isProductGroup = !TextUtils.isEmpty(executedConfiguration.productGroupSelection)
                String products = executedConfiguration.productGroupSelection ?: executedConfiguration.productSelection
                datasheets = dataSheetService.fetchDataSheets(products, enabledSheet, isProductGroup, executedConfiguration.isMultiIngredient)?.dispName?.join(',')
            }
        }
        if (isExcelExport) {
            String reportDateRange = DateUtil.toDateString1(executedConfiguration?.executedAlertDateRangeInformation?.dateRangeStartAbsolute) + " - " + DateUtil.toDateString1(executedConfiguration?.executedAlertDateRangeInformation?.dateRangeEndAbsolute)
            criteriaData = [alertName                     : executedConfiguration?.name,
                            dataSource                    : executedConfiguration?.getDataSource(executedConfiguration?.selectedDatasource),
                            productName                   : executedConfiguration?.productSelection ? ViewHelper.getDictionaryValues(executedConfiguration, DictionaryTypeEnum.PRODUCT) : ViewHelper.getDictionaryValues(executedConfiguration, DictionaryTypeEnum.PRODUCT_GROUP),
                            selectedDatasheets            : datasheets?:"",
                            dateRange                     : reportDateRange,
                            cumulative                    : false,
                            advncedFilter                 : params.advancedFilterId ? advancedFilterService.getAvdFilterCriteriaExcelExport(params.advancedFilterId as Long) : '-',
                            excludeFollowUp               : executedConfiguration?.excludeFollowUp ? "Yes" : "No",
                            missedcase                    : executedConfiguration?.missedCases ? "Yes" : "No",
                            groupBySmq                    : executedConfiguration?.groupBySmq ? "Yes" : "No",
                            includeMedicallyConfirmedCases: executedConfiguration?.includeMedicallyConfirmedCases ? "Yes" : "No",
                            limitToPrimaryPath            : executedConfiguration?.limitPrimaryPath ? "Yes" : "No",
                            rmpReference                  : executedConfiguration?.alertRmpRemsRef ?: "-",
                            scheduledBy                   : executedConfiguration?.owner?.fullName ?: "-",
                            evdasDateRange                : executedConfiguration?.evdasDateRange ?: "-",
                            faersDateRange                : executedConfiguration?.faersDateRange ?: "-",
                            onAndAfterDate                : executedConfiguration?.onOrAfterDate ? DateUtil.toDateString1(executedConfiguration?.onOrAfterDate) : "-"
            ]
            reportParamsMap = ["showCompanyLogo": true, "showLogo": true, "header": "Quantitative Review Report"]
            params << reportParamsMap
            params.criteriaData = criteriaData
        }
        def uniqueDispositions = currentDispositionQueue.toSet()
        String quickFilterDisposition = uniqueDispositions?.join(", ")
        params.quickFilterDisposition = quickFilterDisposition
        List criteriaSheetList
        if ( !cumulative && !adhocRun?.toBoolean() && params.callingScreen != Constants.Commons.DASHBOARD && params.callingScreen != Constants.Commons.TAGS) {
            params.totalCount = agaList?.size()?.toString()
            if(executedConfiguration.selectedDatasource == Constants.DataSource.JADER){
                criteriaSheetList = aggregateCaseAlertService.getJaderAggregateCaseAlertCriteriaData(executedConfiguration, params,Constants.AlertConfigType.AGGREGATE_CASE_ALERT_JADER)
            }else {
                criteriaSheetList = aggregateCaseAlertService.getAggregateCaseAlertCriteriaData(executedConfiguration, params, Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
            }
            params.criteriaSheetList = criteriaSheetList
        }
        Boolean isLongComment = agaList?.any({x -> x.comment?.size() > 100})
        params.isLongComment = isLongComment?: false
        def reportFile = dynamicReportService.createAggregateAlertsReport(new JRMapCollectionDataSource(agaList), params)
        println("total time taken in output report generation is ${System.currentTimeSeconds()-startTime}")
        renderReportOutputType(reportFile,params)
        signalAuditLogService.createAuditForExport(criteriaSheetList, isDashboard == true ? Constants.AuditLog.AGGREGATE_REVIEW_DASHBOARD : executedConfiguration.getInstanceIdentifierForAuditLog() + ": Alert Details", isDashboard == true ? Constants.AuditLog.AGGREGATE_REVIEW_DASHBOARD : (Constants.AuditLog.AGGREGATE_REVIEW + (executedConfiguration.isLatest ? "" : ": Archived Alert")), params, reportFile.name)
    }

    private renderReportOutputType(File reportFile,params) {
        String reportName = "Aggregate Case Alert" + DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss").print(new DateTime())
        response.contentType = "${dynamicReportService.getContentType(params.outputFormat)}; charset=UTF-8"
        response.contentLength = reportFile.size()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.$params.outputFormat" + "\"")
        response.getOutputStream().write(reportFile.bytes)
        response.outputStream.flush()
        params?.reportName = "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.$params.outputFormat"
        }

    @Secured(['ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def changeAssignedToGroup(String selectedId, String assignedToValue, Boolean isArchived) {
        Map labelConfig = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', null, null, null).collectEntries {
            b -> [b.name, b.display]
        }
        List<Map> peHistoryList = []
        def domain = aggregateCaseAlertService.getDomainObject(isArchived)
        String newMessage = ''
        if (params.controller.equals("aggregateCaseAlert")) {
            newMessage = labelConfig.get("assignedTo")+" changed successfully."
        } else {
            newMessage = message(code: 'app.assignedTo.changed.success')
        }
        ResponseDTO responseDTO = new ResponseDTO(status: true, message: newMessage)

        try {
            List<Long> selectedIds = JSON.parse(selectedId).collect {
                it as Long
            }
            boolean bulkUpdate = selectedIds.size() > 1
            DashboardCountDTO dashboardCountDTO = alertService.prepareDashboardCountDTO(false)
            selectedIds.each { Long id ->
                User loggedInUser = userService.getUser()
                def aggregateCaseAlert = domain.get(id)
                String eventName = aggregateCaseAlert.pt
                ExecutedConfiguration executedConfiguration = aggregateCaseAlert.executedAlertConfiguration
                Long configId = aggregateCaseAlertService.getAlertConfigObject(executedConfiguration)
                List peHistoryMapList = []
                if (assignedToValue != userService.getAssignToValue(aggregateCaseAlert)) {
                    List<User> oldUserList = userService.getUserListFromAssignToGroup(aggregateCaseAlert)
                    String oldUserName = userService.getAssignedToName(aggregateCaseAlert)
                    //For Dashboard Counts
                    if (alertService.isUpdateDashboardCount(isArchived, aggregateCaseAlert) && aggregateCaseAlert.assignedToId) {
                        alertService.updateDashboardCountMaps(dashboardCountDTO.userDispCountsMap, aggregateCaseAlert.assignedToId, aggregateCaseAlert.dispositionId.toString())
                        if (aggregateCaseAlert.dueDate) {
                            alertService.updateDashboardCountMaps(dashboardCountDTO.userDueDateCountsMap, aggregateCaseAlert.assignedToId, DateUtil.stringFromDate(aggregateCaseAlert.dueDate, "dd-MM-yyyy", "UTC"))
                        }

                    } else if (alertService.isUpdateDashboardCount(isArchived, aggregateCaseAlert)) {
                        alertService.updateDashboardCountMaps(dashboardCountDTO.groupDispCountsMap, aggregateCaseAlert.assignedToGroupId, aggregateCaseAlert.dispositionId.toString())
                        if (aggregateCaseAlert.dueDate) {
                            alertService.updateDashboardCountMaps(dashboardCountDTO.groupDueDateCountsMap, aggregateCaseAlert.assignedToGroupId, DateUtil.stringFromDate(aggregateCaseAlert.dueDate, "dd-MM-yyyy", "UTC"))
                        }
                    }
                    aggregateCaseAlert = userService.assignGroupOrAssignTo(assignedToValue, aggregateCaseAlert)
                    String newUserName = userService.getAssignedToName(aggregateCaseAlert)
                    List<User> newUserList = userService.getUserListFromAssignToGroup(aggregateCaseAlert)
                    Map peHistoryMap = aggregateCaseAlertService.createPEHistoryMapForAssignedToChange(aggregateCaseAlert, configId, isArchived)
                    peHistoryMapList.add(peHistoryMap)
                    aggregateCaseAlertService.updateAggregateAlertStates(aggregateCaseAlert, peHistoryMap)
                    if (executedConfiguration) {
                        ActivityType activityType = aggregateCaseAlertService.getActivityByType(ActivityTypeValue.AssignedToChange)
                        String detailsText = labelConfig.get("assignedTo")+" changed from '${oldUserName}' to '${newUserName}'"
                        if (emailNotificationService.emailNotificationWithBulkUpdate(bulkUpdate, Constants.EmailNotificationModuleKeys.ASSIGNEE_UPDATE)) {
                            String newEmailMessage = message(code: 'app.email.case.assignment.agg.message.newUser')
                            String oldEmailMessage = message(code: 'app.email.case.assignment.agg.message.oldUser')
                            List emailDataList = userService.generateEmailDataForAssignedToChange(newEmailMessage, newUserList, oldEmailMessage, oldUserList)
                            aggregateCaseAlertService.sendMailForAssignedToChange(emailDataList, aggregateCaseAlert, isArchived)
                        }
                        Map pEHistoryMap = aggregateCaseAlertService.createProductEventHistoryForDispositionChange(aggregateCaseAlert, null, isArchived)
                        pEHistoryMap?.change = Constants.HistoryType.ASSIGNED_TO
                        peHistoryList << pEHistoryMap
                        activityService.createActivity(executedConfiguration, activityType,
                                loggedInUser, detailsText, null, ['For Aggregate Alert'],
                                aggregateCaseAlert.productName, eventName, aggregateCaseAlert.assignedTo, null, aggregateCaseAlert.assignedToGroup)
                    }
                }
            }
            Long userId = null
            Long groupId = null
            if (assignedToValue.startsWith(Constants.USER_GROUP_TOKEN)) {
                groupId = Long.valueOf(assignedToValue.replaceAll(Constants.USER_GROUP_TOKEN, ''))
            } else {
                userId = Long.valueOf(assignedToValue.replaceAll(Constants.USER_TOKEN, ''))
            }
            alertService.updateAssignedToDashboardCounts(dashboardCountDTO, userId, groupId)
            productEventHistoryService.batchPersistHistory(peHistoryList)
        }catch (grails.validation.ValidationException vx) {
            responseDTO.message = MiscUtil.getCustomErrorMessage(vx)
            responseDTO.status = false
            log.error("Exception is : ${vx}")
        } catch (Exception ex) {
            responseDTO.status = false
            responseDTO.message = message(code: 'app.assignedTo.changed.fail')
            log.error(ex.getMessage(), ex)
        }
        render(responseDTO as JSON)
    }

    @Secured(['ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def listConfig() {
        Map resultMap = [aaData: [], recordsTotal: 0, recordsFiltered: 0]
        try {
            HttpSession session = request.getSession()
            session.setAttribute("agg",params["selectedAlertsFilter"])
            resultMap = generateResultMapAgg(resultMap,AggregateCaseAlert)
        } catch (Throwable th) {
            th.printStackTrace()
        }
        render(resultMap as JSON)
    }

    @Secured(['ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def listByExecutedConfig(Boolean cumulative, Long id, Boolean isFilterRequest) {
        def startTime=System.currentTimeSeconds()
        String viewInstanceCheck = params["isViewInstance"]

        ConcurrentHashMap finalMap = [recordsTotal: 0, recordsFiltered: 0, aaData: [], filters: [], configId: id,visibleIdList: []]

        try {
            List filters = getFiltersFromParams(isFilterRequest, params)
            Integer totalColumns = 67
            Map filterMap = alertService.prepareFilterMap(params, totalColumns)
            if(ExecutedConfiguration.get(id)?.masterExConfigId){
                filterMap.remove("productName")
            }
            ViewInstance viewInstance = viewInstanceService.fetchSelectedViewInstance(params.alertType,params.viewId as Long)
            Map sort =  JSON.parse(viewInstance.sorting)
            Map orderColumnMap = alertService.prepareOrderColumnMap(params)
            if(sort && viewInstanceCheck == "1"){
               orderColumnMap = [name: params["columns[${sort.keySet()[0]}][data]"], dir: sort.values()[0]]
            }
            if(filterMap.assignedTo != null){
                String name = filterMap.remove("assignedTo")
                filterMap.put("assignedToUser", name)
            }
            List<String> allowedProductsToUser = []
            if(alertService.isProductSecurity()){
                allowedProductsToUser = alertService.fetchAllowedProductsForConfiguration()
            }
            ExecutedConfiguration executedConfig = null

            //Faers related check added to by-pass the product security.
            params.isFaers = false
            if (!cumulative && params.callingScreen != Constants.Commons.DASHBOARD && params.callingScreen != Constants.Commons.TAGS) {
                executedConfig = ExecutedConfiguration.get(id)
                def selectedDataSource = executedConfig?.selectedDatasource ?: Constants.DataSource.PVA
                if (selectedDataSource == Constants.DataSource.FAERS) {
                    params.isFaers = true
                }
                if (selectedDataSource == Constants.DataSource.JADER) {
                    params.isJader = true
                }
            }

            User user = userService.getUser()
            String timeZone = userService.getCurrentUserPreference()?.timeZone
            AlertDataDTO alertDataDTO = new AlertDataDTO()
            alertDataDTO.params = params
            alertDataDTO.allowedProductsToUser = allowedProductsToUser
            alertDataDTO.domainName = params.boolean('isArchived') ? ArchivedAggregateCaseAlert : AggregateCaseAlert
            alertDataDTO.executedConfiguration = executedConfig
            alertDataDTO.execConfigId = executedConfig?.id
            alertDataDTO.masterExConfigId = executedConfig?.masterExConfigId
            alertDataDTO.configId = executedConfig?.configId
            alertDataDTO.filterMap = filterMap
            alertDataDTO.timeZone = timeZone
            alertDataDTO.orderColumnMap = orderColumnMap
            alertDataDTO.userId = user.id
            alertDataDTO.workflowGroupId = user.workflowGroup.id
            alertDataDTO.groupIdList = user.groups.collect { it.id }
            alertDataDTO.cumulative = cumulative
            alertDataDTO.dispositionFilters = filters
            alertDataDTO.isJader = params.isJader
            alertDataDTO.length = params.int("length")
            alertDataDTO.start = params.int("start")

            Set dispositionSet
            if (params.callingScreen == Constants.Commons.DASHBOARD) {
                dispositionSet = alertService.getDispositionSetDashboard(isFilterRequest,alertDataDTO)
            } else {
                dispositionSet = alertService.getDispositionSet(alertDataDTO.executedConfiguration, alertDataDTO.domainName, isFilterRequest,params)
            }
            long time1 = System.currentTimeMillis()
            log.info("Fetching grid data")
            Map filterCountAndList = alertService.getAlertFilterCountAndList(alertDataDTO, params.callingScreen)
            long time2 = System.currentTimeMillis()
            log.info(((time2 - time1) / 1000) + " Secs were taken in getAlertFilterCountAndList method")
            List<String> productNames = alertService.getDistinctProductName(alertDataDTO.domainName, alertDataDTO,params.callingScreen)
            boolean isMasterConfig = params.callingScreen != Constants.Commons.DASHBOARD ? alertDataDTO.masterExConfigId && productNames.size() >= 1 : false
            List<Map> productIdList = []
            if(isMasterConfig && params.callingScreen != Constants.Commons.DASHBOARD){
                productIdList = alertService.getProductIdMapForMasterConfig(alertDataDTO,params.callingScreen)
            }
            if(alertService.isProductSecurity()) {
                List productList = productIdList ? productIdList.collect { it[0] } : []
                List commonList = allowedProductsToUser.intersect(productList)
                productIdList = productIdList.findAll { it[0] in commonList }
            }
            String currentProduct = productIdList.find { it[1] == executedConfig?.id } ? productIdList.find { it[1] == executedConfig?.id }[0] : productIdList != [] ? productIdList.find { it[1] }[0] : ''
            if(!filters?.isEmpty() || alertDataDTO.advancedFilterDispositions) {
                List visibleIdList = filterCountAndList?.resultList*.id
                if(currentProduct?:executedConfig?.productName){
                    finalMap = [recordsTotal: filterCountAndList.totalCount, recordsFiltered: filterCountAndList.totalFilteredCount, aaData: filterCountAndList.resultList,
                                filters: dispositionSet, configId: id,productNameList :productNames.sort({it?.toUpperCase()}), productIdList: productIdList.sort { it[0]?.toUpperCase() },
                                isMasterConfig: isMasterConfig, currentProduct: currentProduct?:executedConfig?.productName,visibleIdList: visibleIdList]
                } else {
                    finalMap = [recordsTotal: filterCountAndList.totalCount, recordsFiltered: filterCountAndList.totalFilteredCount, aaData: filterCountAndList.resultList,
                                filters: dispositionSet, configId: id,productNameList :productNames.sort({it?.toUpperCase()}), productIdList: productIdList.sort { it[0]?.toUpperCase() },
                                isMasterConfig: isMasterConfig,visibleIdList: visibleIdList]
                }
            } else {
                finalMap = [recordsTotal: filterCountAndList.totalCount, recordsFiltered: 0, aaData: [], filters: dispositionSet, configId: id, visibleIdList : []]
            }
            finalMap .put("advancedFilterDispName",alertDataDTO.advancedFilterDispName)
            finalMap.put("orderColumnMap", alertDataDTO.orderColumnMap)
        } catch (Throwable th) {
            th.printStackTrace()
        }
        def endTime=System.currentTimeSeconds()
        log.info("it took ${endTime-startTime} to load listByExecutedConfig")
        render finalMap as JSON
    }

    def getListWithFilter(ExecutedConfiguration ec) {
        def validFilters = params.findAll { k, v -> (k ==~ /.*Filter/) && (v) }.collectEntries { k, v ->
            [k.replace('Filter', ''), v]
        }
        def filterMap = ["workflowState"             : validFilters["workflowState"] ? PVSState.findById(Long.parseLong(validFilters["workflowState"])) : null,
                         "disposition"               : validFilters["disposition"] ? Disposition.findById(Long.parseLong(validFilters["disposition"])) : null,
                         "priority"                  : validFilters["priority"] ? Priority.findById(Long.parseLong(validFilters["priority"])) : null,
                         "assignedTo"                : validFilters["assignedTo"] ? User.findById(Long.parseLong(validFilters["assignedTo"])) : null,
                         "pt"                        : validFilters["eventSelection"],
                         "productName"               : validFilters["productSelection"],
                         "executedAlertConfiguration": ec]

        def list = listWithFilter(filterMap, AggregateCaseAlert)
        list
    }


    def getListWithoutFilter(ExecutedConfiguration ec, showClosed) {
        List agaL = []
        if (params.callingScreen == Constants.Commons.DASHBOARD) {
            if (showClosed) {
                agaL = AggregateCaseAlert.findAllByAssignedTo(userService.getUser(), [sort: "lastUpdated", order: "desc"])
            } else {
                agaL = (AggregateCaseAlert.where {
                    assignedTo == userService.getUser() && disposition.closed == false
                })?.order('lastUpdated', 'desc')?.list()
            }
        } else if (params.callingScreen == Constants.Commons.REVIEW) {
            if (showClosed) {
                agaL = AggregateCaseAlert.findAllByExecutedAlertConfiguration(ec, [sort: "lastUpdated", order: "desc"])
            } else {
                agaL = (AggregateCaseAlert.where {
                    disposition.closed == false
                    executedAlertConfiguration == ec
                })?.order('lastUpdated', 'desc')?.list()
            }
        } else if (params.callingScreen == Constants.Commons.TRIGGERED_ALERTS) {
            if (showClosed) {
                agaL = AggregateCaseAlert.findAll([sort: "lastUpdated", order: "desc"])
            } else {
                agaL = (AggregateCaseAlert.where {
                    disposition.closed == false
                })?.order('lastUpdated', 'desc')?.list()
            }
        } else {
            if (showClosed) {
                if (ec) {
                    agaL = AggregateCaseAlert.findAllByExecutedAlertConfiguration(ec, [sort: "lastUpdated", order: "desc"])
                } else {
                    agaL = AggregateCaseAlert.findAll([sort: "lastUpdated", order: "desc"])
                }

            } else {
                agaL = (AggregateCaseAlert.where {
                    disposition.closed == false
                    if (ec) {
                        executedAlertConfiguration == ec
                    }
                }).order('lastUpdated', 'desc')?.list()
            }
        }
        agaL
    }

    @Secured(['ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def toggleFlag() {
        def id = params.id
        if (id) {
            def flagged = aggregateCaseAlertService.toggleFlag(id)
            render([success: 'ok', flagged: flagged] as JSON)
        } else {
            response.status = 404
        }
    }

    @Secured(['ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def caseDrillDown(Long id, String type, String typeFlag, Long executedConfigId, BigInteger productId, Integer ptCode) {
        def result = [:]
        if (type && typeFlag) {
            def data = aggregateCaseAlertService.caseDrillDown(type, typeFlag, executedConfigId, productId, ptCode, 'pva')
            result = data
        }
        render result as JSON
    }

    @Secured(['ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def emergingIssues() {
        //TODO: Need to integrate the business configuration backend impl.
        render(view: "emergingIssues", model: [showPrr: true, showRor: true, showEbgm: true, groupBySmq: false, aggregateRules: getAggregateRuleJson()])
    }

    def statisticalComparison() {
        //TODO: Need to integrate the business configuration backend impl.
        render(view: "/statisticalComparison/statisticalComparison", model: [executedConfigId: params.id, showPrr: true, showRor: true, showEbgm: true])
    }

    @Secured(['ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def listEmergingIssues() {

        def list
        def timeZone = "UTC"
        def emergingIssues = emergingIssueService.getEmergingIssues()

        if (!params.filterApplied) {
            if (emergingIssues) {
                list = AggregateCaseAlert.where {
                    pt in emergingIssues
                }
            }
            timeZone = userService.getUser().getPreference().timeZone
        } else {
            def validFilters = params.findAll { k, v -> (k ==~ /.*Filter/) && (v) }.collectEntries { k, v ->
                [k.replace('Filter', ''), v]
            }
            def filterMap = ["workflowState": validFilters["workflowState"] ? PVSState.findById(Long.parseLong(validFilters["workflowState"])) : null,
                             "disposition"  : validFilters["disposition"] ? Disposition.findById(Long.parseLong(validFilters["disposition"])) : null,
                             "priority"     : validFilters["priority"] ? Priority.findById(Long.parseLong(validFilters["priority"])) : null,
                             "assignedTo"   : validFilters["assignedTo"] ? User.findById(Long.parseLong(validFilters["assignedTo"])) : null,
                             "pt"           : validFilters["eventSelection"],
                             "productName"  : validFilters["productSelection"]]

            list = AggregateCaseAlert.createCriteria().list {

                filterMap.each { key, value ->
                    if (value) {
                        and {
                            eq(key, filterMap[key])
                        }
                    }
                }
                if (emergingIssues) {
                    and {
                        "in"("pt", emergingIssues)
                    }
                }
            }
        }

        List agaList = getFilteredAggAlerts(list, timeZone)
        respond agaList, [formats: ['json']]
    }

    private List getFilteredAggAlerts(list, timeZone) {
        def enableSpecialPE = params.specialPE
        def agaList = []
        list.each { AggregateCaseAlert aga ->
            def isSpecialPE = specialPEService.isSpecialPE(aga.productName, aga.pt)
            def trend = productEventHistoryService.getProductEventHistoryTrend(aga.productName, aga.pt)
            if (enableSpecialPE && Boolean.parseBoolean(enableSpecialPE)) {
                if (isSpecialPE) {
                    agaList.add(aga.toDto(timeZone, isSpecialPE, trend))
                }
            } else {
                agaList.add(aga.toDto(timeZone, isSpecialPE, trend))
            }
        }
        agaList
    }

    def generateCaseSeries() {
        def seriesName = params.seriesName
        def alertMetaInfo = params.metaInfo
        def selectedDataSource=params.selectedDatasource
        def metaArray = alertMetaInfo.split(",")
        def type, typeFlag, executedConfigId, productId, ptCode, id, alert
        metaArray.each {
            def mataObj = it.split(":")
            if (mataObj[0] == "typeFlag") {
                typeFlag = mataObj[1]
            }
            if (mataObj[0] == "type") {
                type = mataObj[1]
            }
            if (mataObj[0] == "executedConfigId") {
                executedConfigId = mataObj[1]
            }
            if (mataObj[0] == "productId") {
                productId = mataObj[1]
            }
            if (mataObj[0] == "ptCode") {
                ptCode = mataObj[1]
            }
            if (mataObj[0] == "id") {
                id = mataObj[1]
            }
            if (mataObj[0] == "keyId") {
                typeFlag = mataObj[1]
            }
        }
        if(!selectedDataSource)
        {
            selectedDataSource="pva"
        }
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(executedConfigId)
        if(executedConfiguration?.adhocRun){
            alert = AggregateOnDemandAlert.get(id)
        } else {
            alert = executedConfiguration.isLatest ? AggregateCaseAlert.get(id) : ArchivedAggregateCaseAlert.get(id)
        }
        boolean isEventGroup = false
        if(executedConfiguration.eventGroupSelection){
            isEventGroup = true
        }
        def data =aggregateCaseAlertService.caseDrillDown(type, typeFlag, executedConfigId as Long, productId as BigInteger, alert.ptCode, selectedDataSource, executedConfiguration.groupBySmq, alert, isEventGroup)

        def caseData = []

        def caseSeriesDelimiter = Holders.config.caseSeries.bulk.addCase.delimiter ?: ":"
        data.each {
            caseData.add(it."${cacheService.getRptFieldIndexCache('masterCaseNum')}" + caseSeriesDelimiter + it."${cacheService.getRptFieldIndexCache('masterVersionNum')}")
        }
        caseData = caseData.join(",")
        def response = reportExecutorService.generateCaseSeries(seriesName, caseData)
        response.status = response?.status
        render response as JSON
    }

    def pecTree() {}

    private getAggregateRuleJson() {
        def aggregateRules = grailsApplication.config.aggregateRules
        def aggregateArray = new JSONArray()
        aggregateRules.each {
            def eudraRulesObj = new JSONObject()
            eudraRulesObj.put("parameterName", it.parameterName)
            eudraRulesObj.put("value", it.value)
            eudraRulesObj.put("change", it.change)
            eudraRulesObj.put("color", it.color)
            aggregateArray.add(eudraRulesObj)
        }
        aggregateArray.toString()
    }

    def pecTreeJson() {
        def json = """
           {  
    "name":"PEC Importance(50.4, 49.6)",
    "children":[  
      {  
         "name":"BH Criteria(51.0, 49.0)",
         "children":[  
            {  
               "name":"Temporality",
               "children":[  
                  {  
                     "name":"Time of onset"
                  }
               ]
            },
            {  
               "name":"Dose : Strength of association(56.2, 43.8)",
               "children":[  
                  {  
                     "name":"Biological Gradience",
                     "children":[  
                        {  
                           "name":"Dechallenge"
                        },
                        {  
                           "name":"Rechallenge"
                        }
                     ]
                  },
                  {  
                     "name":"Strength(62.5, 37.5)",
                     "title" : "Strength",
                     "matrix" : "High:62.5, Low:37.5",
                     "children":[  
                        {  
                           "name":"Trend"
                        },
                        {  
                           "name":"SDR(75.0, 25.0)",
                           "children":[  
                              {  
                                 "name":"IC"
                              },
                              {  
                                 "name":"PRR(100, 0)"
                              }
                           ]
                        }
                     ]
                  }
               ]
            },
            {  
               "name":"Specificity - Consistency",
               "children":[  
                  {  
                     "name":"Specificity",
                     "children":[  
                        {  
                           "name":"DME"
                        }
                     ]
                  },
                  {  
                     "name":"Consistancy",
                     "children":[  
                        {  
                           "name":"Evidence of multiple countries"
                        },
                        {  
                           "name":"Evidence of multiple case types"
                        }
                     ]
                  }
               ]
            },
            {  
               "name":"Coherence",
               "children":[  
                  {  
                     "name":"Cofounding by indication"
                  },
                  {  
                     "name":"Alternate Explaination",
                     "children":[  
                        {  
                           "name":"Concomitant Drugs"
                        }
                     ]
                  }
               ]
            }
         ]
      },
      {  
         "name":"Special Population",
         "children":[  
            {  
               "name":"Elderly"
            },
            {  
               "name":"children"
            }
         ]
      },
      {  
         "name":"Other Factors(50.0, 50.0)",
         "children":[  
            {  
               "name":"IME(50.0, 50.0)"
            },
            {  
               "name":"Other Factors(50.0, 50.0)"
            }
         ]
      },
      {  
         "name":"Listed(50.0, 50.0)"
      }
   ]
}
        """
        render json
    }

    def showTrendAnalysis(Boolean isFaers) {
        def alertId = params.id
        redirect(controller: "trendAnalysis", action: 'showTrendAnalysis', params: [id: alertId, type: Constants.AlertConfigType.AGGREGATE_CASE_ALERT, isFaers: isFaers])
    }

    def fetchStratifiedScores(Long alertId, String prrType, Boolean isArchived) {
        def domain
        if(AggregateOnDemandAlert.get(alertId)){
            domain = AggregateOnDemandAlert
        } else {
            domain = aggregateCaseAlertService.getDomainObject(isArchived)
        }
        def aggregateCaseAlert = domain.read(alertId)
        String jsonString = null
        def object = [:]

        switch (prrType) {
            case Constants.Stratification_Fields.PRR:
                jsonString = aggregateCaseAlert.prrStr
                object.PRR = aggregateCaseAlert.prrValue
                object["PRR(MH)"] = aggregateCaseAlert.prrMh
                break;
            case Constants.Stratification_Fields.ROR:
                jsonString = aggregateCaseAlert.rorStr
                object.ROR = aggregateCaseAlert.rorValue
                object["ROR(MH)"] = aggregateCaseAlert.rorMh
                break;
            case Constants.Stratification_Fields.PRRLCI:
                jsonString = aggregateCaseAlert.prrStrLCI
                object.PRRLCI = aggregateCaseAlert.prrLCI
                object["PRR(MH)"] = aggregateCaseAlert.prrMh
                break;
            case Constants.Stratification_Fields.PRRUCI:
                jsonString = aggregateCaseAlert.prrStrUCI
                object.PRRUCI = aggregateCaseAlert.prrUCI
                object["PRR(MH)"] = aggregateCaseAlert.prrMh
                break;
            case Constants.Stratification_Fields.RORLCI:
                jsonString = aggregateCaseAlert.rorStrLCI
                object.RORLCI = aggregateCaseAlert.rorLCI
                object["ROR(MH)"] = aggregateCaseAlert.rorMh
                break;
            case Constants.Stratification_Fields.RORUCI:
                jsonString = aggregateCaseAlert.rorStrUCI
                object.RORUCI = aggregateCaseAlert.rorUCI
                object["ROR(MH)"] = aggregateCaseAlert.rorMh
                break;

        }
        try {
            if (jsonString && jsonString != "0") {//Check to make sure that stratification is enabled.
                def jsonStringLength = jsonString.length() - 1
                jsonString = jsonString.charAt(0) == '"' && jsonString.charAt(jsonStringLength) ? jsonString?.substring(1, jsonStringLength) : jsonString
                def splitJson = jsonString?.split(",")
                splitJson.each {
                    String []keyValue = it.split(":")
                    if(keyValue.length == 2) {
                        String key = cacheService.getStratificationColumnName(keyValue[0])?:keyValue[0]
                        object[key] = keyValue[1]
                    }
                }
                object.ErrorMessage = Constants.Commons.BLANK_STRING
            } else {
                object.ErrorMessage = message(code: "app.label.stratification.values.error")
            }
        } catch (Throwable t) {
            t.printStackTrace()
        }
        render(object as JSON)
    }

    def exportSignalSummaryReport(Long id, Boolean cumulative, String outputFormat, String callingScreen) {
        List validatedSignalList = []
        List notStartedReviewSignalList = []
        List pendingReviewList = []
        List closedReviewList = []
        Map signalData = [:]
        ExecutedConfiguration ec = null
        List criteriaSheetList = []
        Boolean isDashboard=false
        def entityValueForExport = ""
        def domainName = aggregateCaseAlertService.getDomainObject(params.boolean('isArchived'))

        Group workflowGroup = userService.getUser()?.getWorkflowGroup()
        String defaultDispositionValue = workflowGroup?.defaultQuantDisposition?.value
        if (cumulative || callingScreen == Constants.Commons.DASHBOARD && callingScreen == Constants.Commons.TAGS) {
            isDashboard=true
            List<ExecutedConfiguration> executedConfigurationList = ExecutedConfiguration.createCriteria().list {
                eq('workflowGroup', workflowGroup)
                eq("adhocRun", false)
                eq('type', Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
            }
            List aggregateCaseAlertList = domainName.createCriteria().list {
                or {
                    executedConfigurationList.collate(1000).each {
                        'in'("executedAlertConfiguration", it)
                    }
                }
            }
            entityValueForExport = Constants.Commons.DASHBOARD
            aggregateCaseAlertList.each { def aga ->
                if (aga.disposition.isValidatedConfirmed()) {
                    validatedSignalList << aga
                } else if (aga.disposition.isClosed()) {
                    closedReviewList << aga
                } else if (aga.disposition.value == defaultDispositionValue) {
                    notStartedReviewSignalList << aga
                } else {
                    pendingReviewList << aga
                }
            }
            signalData = [alertName      : "", productName: "", dateRange: "",
                          referenceNumber: "", cumulative: true]
        } else {
            List agaList = []
            ec = ExecutedConfiguration.findByIdAndIsEnabled(params.id, true)
            entityValueForExport=ec.getInstanceIdentifierForAuditLog()
            User user = userService.getUser()
            params.adhocRun = false
            String timeZone = userService.getUser()?.preference?.timeZone
            AlertDataDTO alertDataDTO = new AlertDataDTO()
            alertDataDTO.params = params
            alertDataDTO.userId = user.id
            alertDataDTO.executedConfiguration = ec
            alertDataDTO.execConfigId = ec?.id
            alertDataDTO.cumulative = cumulative
            alertDataDTO.timeZone = timeZone
            alertDataDTO.isFromExport = true
            alertDataDTO.workflowGroupId = user.workflowGroup.id
            alertDataDTO.groupIdList = user.groups.collect { it.id }
            alertDataDTO.domainName = aggregateCaseAlertService.getDomainObject(params.boolean('isArchived'))

            if (params.selectedCases) {
                List resultList = aggregateCaseAlertService.listSelectedAlerts(params.selectedCases, alertDataDTO.domainName)
                agaList = resultList*.id
            } else {
                Map filterMap = [:]
                if (params.filterList) {
                    def jsonSlurper = new JsonSlurper()
                    filterMap = jsonSlurper.parseText(params.filterList)
                    if (filterMap.productName == "-1") filterMap.remove("productName")
                }
                if (ec?.masterExConfigId) {
                    filterMap.remove("productName")
                }
                List dispositionFilters = getFiltersFromParams(params.isFilterRequest?.toBoolean(), params)

                List<String> allowedProductsToUser = []
                if (alertService.isProductSecurity()) {
                    allowedProductsToUser = alertService.fetchAllowedProductsForConfiguration()
                }
                alertDataDTO.allowedProductsToUser = allowedProductsToUser
                alertDataDTO.filterMap = filterMap
                alertDataDTO.executedConfiguration = ec
                alertDataDTO.execConfigId = ec?.id
                alertDataDTO.configId = ec?.configId
                alertDataDTO.orderColumnMap = [name: params.column, dir: params.sorting]
                alertDataDTO.userId = userService.getUser().id
                alertDataDTO.cumulative = cumulative
                alertDataDTO.dispositionFilters = dispositionFilters

                agaList = alertService.getAlertFilterIdList(alertDataDTO)
            }

            List currentDispositionList = []
            List aggregateCaseAlertList = []
            if(agaList) {
                aggregateCaseAlertList = domainName.createCriteria().list {
                    or {
                        agaList.collate(1000).each {
                            'in'("id", it)
                        }
                    }
                }
            }
            if(aggregateCaseAlertList) {
                aggregateCaseAlertList.each { def aga ->
                    currentDispositionList.add(aga?.disposition.displayName)
                    if (aga.disposition.isValidatedConfirmed()) {
                        validatedSignalList << aga
                    } else if (aga.disposition.isClosed()) {
                        closedReviewList << aga
                    } else if (aga.disposition.value == defaultDispositionValue) {
                        notStartedReviewSignalList << aga
                    } else {
                        pendingReviewList << aga
                    }
                }
            }
            params.totalCount = agaList?.size()?.toString()
            def uniqueDispositions = currentDispositionList.toSet()
            String quickFilterDisposition = uniqueDispositions?.join(", ")
            params.quickFilterDisposition = quickFilterDisposition

            Configuration config = Configuration.findByName(ec.name)
            def productName = ec.getProductNameList().size() < 1 ? getGroupNameFieldFromJson(config.getProductGroupSelection()) : ec.getProductNameList()
            List<Date> dateRange = ec?.executedAlertDateRangeInformation?.getReportStartAndEndDate()
            def reportDateRange = (dateRange ? DateUtil.toDateString1(dateRange[0]) : "-") + " to " + (dateRange ? DateUtil.toDateString1(dateRange[1]) : "-")
            def referenceNumber = ec?.referenceNumber ?: "-"
            signalData = [alertName      : ec.name, productName: productName, dateRange: reportDateRange,
                          referenceNumber: referenceNumber, cumulative: false]
        }

        validatedSignalList = aggregateCaseAlertService.getSignalDetectionSummaryMap(validatedSignalList)
        notStartedReviewSignalList = aggregateCaseAlertService.getSignalDetectionSummaryMap(notStartedReviewSignalList)
        pendingReviewList = aggregateCaseAlertService.getSignalDetectionSummaryMap(pendingReviewList)
        closedReviewList = aggregateCaseAlertService.getSignalDetectionSummaryMap(closedReviewList)
        if(ec.selectedDatasource == Constants.DataSource.JADER){
            criteriaSheetList = aggregateCaseAlertService.getJaderAggregateCaseAlertCriteriaData(ec, params,Constants.AlertConfigType.AGGREGATE_CASE_ALERT_JADER)
        }else {
            criteriaSheetList = aggregateCaseAlertService.getAggregateCaseAlertCriteriaData(ec, params)
        }
        params.criteriaSheetList = criteriaSheetList
        Map reportParamsMap = ["showCompanyLogo"  : true,
                               "showLogo"         : true,
                               "header"           : "Signal Detection Summary for Aggregate Alert",
                               "outputFormat"     : outputFormat,
                               'criteriaSheetList': criteriaSheetList]

        File reportFile = dynamicReportService.createSignalDetectionReport(validatedSignalList ? new JRMapCollectionDataSource(validatedSignalList) : null,
                notStartedReviewSignalList ? new JRMapCollectionDataSource(notStartedReviewSignalList) : null,
                pendingReviewList ? new JRMapCollectionDataSource(pendingReviewList) : null,
                closedReviewList ? new JRMapCollectionDataSource(closedReviewList) : null,
                signalData, reportParamsMap)
        renderReportOutputType(reportFile, params)
        signalAuditLogService.createAuditForExport(criteriaSheetList, entityValueForExport + ": Detection Summary", isDashboard == true ? "Aggregate Review Dashboard" : (Constants.AuditLog.AGGREGATE_REVIEW + (ec.isLatest ? "" : ": Archived Alert")), params, reportFile.name)


    }

    def upload() {
        def alertId = params?.alertId
        def domain = params.boolean('isArchived') ? ArchivedAggregateCaseAlert : AggregateCaseAlert

        def aggCaseAlert = domain.findById(alertId.toInteger())
        def fileName = params?.attachments.filename
        if (fileName instanceof String) {
            fileName = [fileName]
        }
        params?.isAlertDomain = true   //this is added to check whether attachment is added from alert details screen PVS-49054
        User currentUser = userService.getUser()
        Map filesStatusMap = attachmentableService.attachUploadFileTo(currentUser, fileName, aggCaseAlert, request)
        String fileDescription = params.description
        List<Attachment> attachments = aggCaseAlert.getAttachments().sort { it.dateCreated }
        if (attachments) {
            List<Integer> bulkAttachmentIndex = 1..filesStatusMap?.uploadedFiles?.size()
            bulkAttachmentIndex.each {
                Attachment attachment = attachments[-it]
                AttachmentDescription attachmentDescription = new AttachmentDescription()
                attachmentDescription.attachment = attachment
                attachmentDescription.createdBy = currentUser.fullName
                attachmentDescription.description = fileDescription
                attachmentDescription.skipAudit = true
                attachmentDescription.save(flush: true)
            }
        }
        if (filesStatusMap?.uploadedFiles) {
            def filenames = filesStatusMap?.uploadedFiles*.originalFilename.join(', ')
            activityService.createActivity(aggCaseAlert.executedAlertConfiguration, ActivityType.findByValue(ActivityTypeValue.AttachmentAdded),
                    userService.getUser(), "Attachment ${filenames} is added with description '${fileDescription}'", null,
                    [product: getNameFieldFromJson(aggCaseAlert.alertConfiguration.productSelection), event: getNameFieldFromJson(aggCaseAlert.alertConfiguration.eventSelection)],
                    aggCaseAlert.productName, aggCaseAlert.pt, aggCaseAlert.assignedTo, null, aggCaseAlert.assignedToGroup)
        }
        render(['success': true] as JSON)
    }

    def showCharts() {
        def studyCount = []
        def newCount = []
        def seriousCount = []
        def fatalCount = []
        def prrValue = []
        def rorValue = []
        def eb05 = []
        def eb95 = []
        def ebgmValue = []
        def xAxisTitle = []
        def frequency = Constants.Commons.BLANK_STRING
        def domain = params.boolean('isArchived') ? ArchivedAggregateCaseAlert : AggregateCaseAlert
        def aga = domain.findById(params.alertId)
        if (aga) {
            def configuration = aga.alertConfiguration
            def productId = aga.productId
            def ptCode = aga.ptCode
            def smqCode = aga.smqCode

            def prevExecs = ExecutedConfiguration.findAllByConfigIdAndIsEnabled(configuration.id, true, [sort: "numOfExecutions", order: "desc"])

            SubstanceFrequency substanceFrequency = SubstanceFrequency.findByNameIlike(aga.productName)
            if (substanceFrequency) {
                frequency = substanceFrequency.uploadFrequency
            } else {
                frequency = getRecurrencePattern(prevExecs[0].scheduleDateJSON)
            }

            def exeIdList = prevExecs*.id
            exeIdList.each {
                if(ExecutionStatus.findByExecutedConfigId(it)?.executionStatus==ReportExecutionStatus.ERROR){
                    prevExecs.remove(ExecutedConfiguration.get(it))
                }
            }

            prevExecs = prevExecs?.unique {
                frequency.equals("HOURLY") ? it.executedAlertDateRangeInformation.dateRangeEndAbsolute : (DateUtils.truncate(it.executedAlertDateRangeInformation.dateRangeEndAbsolute, Calendar.DAY_OF_MONTH))
            }?.sort {
                frequency.equals("HOURLY") ? it.executedAlertDateRangeInformation.dateRangeEndAbsolute : (DateUtils.truncate(it.executedAlertDateRangeInformation.dateRangeEndAbsolute, Calendar.DAY_OF_MONTH))
            }?.takeRight(6)
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy")
            SimpleDateFormat hour = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss")
            List prevAlertList = []
            ExecutedConfiguration latest = prevExecs.first()
            prevExecs.eachWithIndex{ ExecutedConfiguration exe, int i ->
                if(latest.id<exe.id && ExecutionStatus.findByExecutedConfigId(exe.id).executionStatus==ReportExecutionStatus.COMPLETED){
                    latest=exe
                }
            }
            prevAlertList.add(ArchivedAggregateCaseAlert.findAllByExecutedAlertConfigurationInListAndProductIdAndPtCodeAndSmqCode(prevExecs, productId, ptCode,smqCode))
            prevAlertList.add(AggregateCaseAlert.findByExecutedAlertConfigurationAndProductIdAndPtCodeAndSmqCode(latest, productId, ptCode,smqCode))
            DateFormat hourFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss")
            DateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy")
            prevAlertList?.flatten().sort { it?.periodEndDate }.sort { it?.periodEndDate }.each { prevAlert ->
                if(prevAlert?.newStudyCount != -1)
                    studyCount.add(prevAlert?.newStudyCount ?: 0)
                else if(prevAlert?.newStudyCount == -1 && prevAlert.faersColumns && prevAlert.getFaersColumnValue("newStudyCountFaers")!=null)
                    studyCount.add(prevAlert.getFaersColumnValue("newStudyCountFaers") as Integer)
                else
                    studyCount.add(0)
                if(prevAlert?.newCount != -1)
                    newCount.add(prevAlert?.newCount ?: 0)
                else if(prevAlert?.newCount == -1 && prevAlert.faersColumns && prevAlert.getFaersColumnValue("newCountFaers")!=null)
                    newCount.add(prevAlert.getFaersColumnValue("newCountFaers") as Integer)
                else if(prevAlert?.newCount == -1 && prevAlert.vaersColumns && prevAlert.getVaersColumnValue("newCountVaers")!=null)
                    newCount.add(prevAlert.getVaersColumnValue("newCountVaers") as Integer)
                else if(prevAlert?.newCount == -1 && prevAlert.vigibaseColumns && prevAlert.getVigibaseColumnValue("newCountVigibase")!=null)
                    newCount.add(prevAlert.getVigibaseColumnValue("newCountVigibase") as Integer)
                else if(prevAlert?.newCount == -1 && prevAlert.jaderColumns && prevAlert.getJaderColumnValue("newCountJader")!=null)
                    newCount.add(prevAlert.getJaderColumnValue("newCountJader") as Integer)
                else
                    newCount.add(' - ')
                if(prevAlert?.newSeriousCount != -1)
                    seriousCount.add(prevAlert?.newSeriousCount ?: 0)
                else if(prevAlert?.newSeriousCount == -1 && prevAlert.faersColumns && prevAlert.getFaersColumnValue("newSeriousCountFaers")!=null)
                    seriousCount.add(prevAlert.getFaersColumnValue("newSeriousCountFaers") as Integer)
                else if(prevAlert?.newSeriousCount == -1 && prevAlert.vaersColumns && prevAlert.getVaersColumnValue("newSeriousCountVaers")!=null)
                    seriousCount.add(prevAlert.getVaersColumnValue("newSeriousCountVaers") as Integer)
                else if(prevAlert?.newSeriousCount == -1 && prevAlert.vigibaseColumns && prevAlert.getVigibaseColumnValue("newSeriousCountVigibase")!=null)
                    seriousCount.add(prevAlert.getVigibaseColumnValue("newSeriousCountVigibase") as Integer)
                else if(prevAlert?.newSeriousCount == -1 && prevAlert.jaderColumns && prevAlert.getJaderColumnValue("newSeriousCountJader")!=null)
                    seriousCount.add(prevAlert.getJaderColumnValue("newSeriousCountJader") as Integer)
                else
                    seriousCount.add(' - ')
                if(prevAlert?.newFatalCount != -1)
                    fatalCount.add(prevAlert?.newFatalCount ?: 0)
                else if(prevAlert?.newFatalCount == -1 && prevAlert.faersColumns && prevAlert.getFaersColumnValue("newFatalCountFaers")!=null)
                    fatalCount.add(prevAlert.getFaersColumnValue("newFatalCountFaers") as Integer)
                else if(prevAlert?.newFatalCount == -1 && prevAlert.vaersColumns && prevAlert.getVaersColumnValue("newFatalCountVaers")!=null)
                    fatalCount.add(prevAlert.getVaersColumnValue("newFatalCountVaers") as Integer)
                else if(prevAlert?.newFatalCount == -1 && prevAlert.vigibaseColumns && prevAlert.getVigibaseColumnValue("newFatalCountVigibase")!=null)
                    fatalCount.add(prevAlert.getVigibaseColumnValue("newFatalCountVigibase") as Integer)
                else if(prevAlert?.newFatalCount == -1 && prevAlert.jaderColumns && prevAlert.getJaderColumnValue("newFatalCountJader")!=null)
                    fatalCount.add(prevAlert.getJaderColumnValue("newFatalCountJader") as Integer)
                else
                    fatalCount.add(' - ')
                if(Double.valueOf(prevAlert?.prrValue?:0)!=-1)
                    prrValue.add(prevAlert?.prrValue ?: 0)
                else if(prevAlert.faersColumns)
                    prrValue.add(prevAlert.getFaersColumnValue("prrValueFaers") ?: 0)
                else if(prevAlert.vaersColumns)
                    prrValue.add(prevAlert.getVaersColumnValue("prrValueVaers") ?: 0)
                else if(prevAlert.vigibaseColumns)
                    prrValue.add(prevAlert.getVigibaseColumnValue("prrValueVigibase") ?: 0)
                else if(prevAlert.jaderColumns)
                    prrValue.add(prevAlert.getJaderColumnValue("prrValueJader") ?: 0)
                else
                    prrValue.add(' - ')
                if(Double.valueOf(prevAlert?.rorValue?:0)!=-1)
                    rorValue.add(prevAlert?.rorValue ?: 0)
                else if(prevAlert.faersColumns)
                    rorValue.add(prevAlert.getFaersColumnValue("rorValueFaers") ?: 0)
                else if(prevAlert.vaersColumns)
                    rorValue.add(prevAlert.getVaersColumnValue("rorValueVaers") ?: 0)
                else if(prevAlert.vigibaseColumns)
                    rorValue.add(prevAlert.getVigibaseColumnValue("rorValueVigibase") ?: 0)
                else if(prevAlert.jaderColumns)
                    rorValue.add(prevAlert.getJaderColumnValue("rorValueJader") ?: 0)
                else
                    rorValue.add(' - ')
                if(Double.valueOf(prevAlert?.eb05?:0)!=-1)
                    eb05.add(prevAlert?.eb05 ?: 0)
                else if(prevAlert.faersColumns)
                    eb05.add(prevAlert.getFaersColumnValue("eb05Faers") ?: 0)
                else if(prevAlert.vaersColumns)
                    eb05.add(prevAlert.getVaersColumnValue("eb05Vaers") ?: 0)
                else if(prevAlert.vigibaseColumns)
                    eb05.add(prevAlert.getVigibaseColumnValue("eb05Vigibase") ?: 0)
                else if(prevAlert.jaderColumns)
                    eb05.add(prevAlert.getJaderColumnValue("eb05Jader") ?: 0)
                else
                    eb05.add(' - ')
                if(Double.valueOf(prevAlert?.eb95?:0)!=-1)
                    eb95.add(prevAlert?.eb95 ?: 0)
                else if(prevAlert.faersColumns)
                    eb95.add(prevAlert.getFaersColumnValue("eb95Faers") ?: 0)
                else if(prevAlert.vaersColumns)
                    eb95.add(prevAlert.getVaersColumnValue("eb95Vaers") ?: 0)
                else if(prevAlert.vigibaseColumns)
                    eb95.add(prevAlert.getVigibaseColumnValue("eb95Vigibase") ?: 0)
                else if(prevAlert.jaderColumns)
                    eb95.add(prevAlert.getJaderColumnValue("eb95Jader") ?: 0)
                else
                    eb95.add(' - ')
                if(prevAlert?.ebgm != -1)
                    ebgmValue.add(prevAlert?.ebgm ?: 0)
                else if(prevAlert?.prrValue == -1 && prevAlert.faersColumns)
                    ebgmValue.add(prevAlert.getFaersColumnValue("ebgmFaers"))
                else if(prevAlert?.prrValue == -1 && prevAlert.vaersColumns)
                    ebgmValue.add(prevAlert.getVaersColumnValue("ebgmVaers"))
                else if(prevAlert?.prrValue == -1 && prevAlert.vigibaseColumns)
                    ebgmValue.add(prevAlert.getVigibaseColumnValue("ebgmVigibase"))
                else if(prevAlert?.prrValue == -1 && prevAlert.jaderColumns)
                    ebgmValue.add(prevAlert.getJaderColumnValue("ebgmJader"))
                else
                    ebgmValue.add(' - ')
                def dateRange = prevAlert.executedAlertConfiguration.executedAlertDateRangeInformation.dateRangeEndAbsolute
                boolean isSafetyOnly = true
                if(prevAlert.executedAlertConfiguration.executedAlertDateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE && prevAlert.executedAlertConfiguration?.selectedDatasource == Constants.DataSource.FAERS){
                    dateRange = (reportExecutorService.getFaersDateRange().faersDate).substring(13)
                    isSafetyOnly=false
                } else if(prevAlert.executedAlertConfiguration.executedAlertDateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE && prevAlert.executedAlertConfiguration?.selectedDatasource == Constants.DataSource.VAERS){
                    dateRange = (reportExecutorService.getVaersDateRange(1).vaersDate).substring(13)
                    isSafetyOnly=false
                } else if(prevAlert.executedAlertConfiguration.executedAlertDateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE && prevAlert.executedAlertConfiguration?.selectedDatasource == Constants.DataSource.VIGIBASE) {
                    dateRange = (reportExecutorService.getVigibaseDateRange().vigibaseDate).substring(13)
                    isSafetyOnly=false
                } else if(prevAlert.executedAlertConfiguration.executedAlertDateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE && prevAlert.executedAlertConfiguration?.selectedDatasource == Constants.DataSource.JADER) {
                    dateRange = (jaderExecutorService.getJaderDateRange().jaderDate).substring(13)
                    isSafetyOnly=false
                }
                if(!isSafetyOnly){
                    xAxisTitle.add(dateRange)
                }else{
                    xAxisTitle.add(sdf.format(prevAlert?.periodEndDate))
                }
            }
        }
        def model = ["studyCount": studyCount, "newCount": newCount, "seriousCount": seriousCount, "fatalCount": fatalCount,
                     "xAxisTitle": xAxisTitle, "frequency" : frequency , "isRor" : cacheService.getRorCache()]

        boolean isEnabledPRR = (grailsApplication.config.statistics.enable.prr || grailsApplication.config.statistics.faers.enable.prr || grailsApplication.config.statistics.vaers.enable.prr || grailsApplication.config.statistics.vigibase.enable.prr || grailsApplication.config.statistics.jader.enable.prr)
        boolean isEnabledROR = (grailsApplication.config.statistics.enable.ror || grailsApplication.config.statistics.faers.enable.ror || grailsApplication.config.statistics.vaers.enable.ror || grailsApplication.config.statistics.vigibase.enable.ror || grailsApplication.config.statistics.jader.enable.ror)
        boolean isEnabledEBGM = (grailsApplication.config.statistics.enable.ebgm || grailsApplication.config.statistics.faers.enable.ebgm || grailsApplication.config.statistics.vaers.enable.ebgm || grailsApplication.config.statistics.vigibase.enable.ebgm || grailsApplication.config.statistics.jader.enable.ebgm)

        if (isEnabledPRR) {
            model << ["prrValue": prrValue]
        }
        if (isEnabledROR) {
            model << ["rorValue": rorValue]
        }
        if (isEnabledEBGM) {
            model << ["ebgmValue": ebgmValue]
            model << ["eb05Value": eb05]
            model << ["eb95Value": eb95]
        }
        render(model as JSON)
    }

    def fetchAttachment(final Long alertId, Boolean isArchived) {
        def attachments = []
        List<Long> aggAlertList = aggregateCaseAlertService.getAlertIdsForAttachments(alertId, isArchived)
        String timezone = userService.user.preference.timeZone
        aggAlertList.each { Long aggAlertId ->
            def aggAlert = ArchivedAggregateCaseAlert.get(aggAlertId) ?: AggregateCaseAlert.get(aggAlertId)
            attachments += aggAlert.attachments.collect {
                [
                        id         : it.id,
                        name       :  it.inputName ?: it.name,
                        description: AttachmentDescription.findByAttachment(it)?.description,
                        timeStamp  : DateUtil.stringFromDate(it.dateCreated, DateUtil.DEFAULT_DATE_TIME_FORMAT, timezone),
                        modifiedBy : AttachmentDescription.findByAttachment(it)?.createdBy
                ]
            }
        }

        respond attachments.unique(), [formats: ['json']]
    }

    def deleteAttachment(Long alertId, Long attachmentId, Boolean isArchived) {
        try {
            def domain = aggregateCaseAlertService.getDomainObject(isArchived)
            Attachment attachment = Attachment.findById(attachmentId)
            String fileName = attachment.getFilename()
            def aggCaseAlert = domain.get(alertId)
            if (attachment) {
                if (AttachmentDescription.findByAttachment(attachment)) {
                    AttachmentDescription.findByAttachment(attachment).delete()
                }
                attachmentableService.removeAttachment(attachmentId)
                activityService.createActivity(aggCaseAlert.executedAlertConfiguration, ActivityType.findByValue(ActivityTypeValue.AttachmentRemoved),
                        userService.getUser(), "Attachment " + fileName + " is removed", null,
                        [product: getNameFieldFromJson(aggCaseAlert.alertConfiguration.productSelection), event: getNameFieldFromJson(aggCaseAlert.alertConfiguration.eventSelection)],
                        aggCaseAlert.productName, aggCaseAlert.pt, aggCaseAlert.assignedTo, null, aggCaseAlert.assignedToGroup)

            }
            render(['success': true] as JSON)
        }catch (Exception e) {
            render(['success': false] as JSON)
        }
    }


    def getSubstanceFrequency(String productName) {
        def alertType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT
        if (params.dataSource == 'faers') {
            alertType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT_FAERS
        }
        SubstanceFrequency frequency = SubstanceFrequency.findByNameAndAlertType(productName, alertType)

        Map properties = [:]
        if (frequency) {
            properties.miningFrequency = frequency.frequencyName
            List probableDateRange = evdasAlertService.populatePossibleDateRanges(frequency.startDate, frequency.miningFrequency)
            properties.probableStartDate = probableDateRange.collect { it[0] }
            properties.probableEndDate = probableDateRange.collect { it[1] }

        }
        render(properties as JSON)
    }


    def fetchDateRangeList(String frequency) {
        LinkedHashMap dateRangeMap = fetchDateRangeMap(frequency)
        render(dateRangeMap as JSON)
    }

    def saveAlertTags(Long execConfigId, Long alertId, Boolean isArchived) {
        def domain = aggregateCaseAlertService.getDomainObject(isArchived)
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        String justification = params.justification
        User loggedInUser = userService.getUser()

        ExecutedConfiguration executedConfig = ExecutedConfiguration.findById(execConfigId)
        Long configId = aggregateCaseAlertService.getAlertConfigObject(executedConfig)
        def aggCaseAlert = domain.findById(alertId)
        List peHistoryMapList = []
        List tagList = []
        Map peHistoryMap = [
                "justification"   : '',
                "change"          : Constants.HistoryType.ALERT_TAGS
        ]
        peHistoryMap = aggregateCaseAlertService.createBasePEHistoryMap(aggCaseAlert,peHistoryMap, isArchived)
        if (params?.alertTags) {
            List attachedTags = aggCaseAlert.alertTags.collect { it.name }
            def updatedTags = params.alertTags == 'null' ? [] : JSON.parse(params.alertTags)
            List addedTags = updatedTags - attachedTags
            List removedTags = attachedTags - updatedTags

            addedTags.each { tagName ->
                AlertTag addAlertTag = AlertTag.findByName(tagName)
                if (!addAlertTag) {
                    addAlertTag = new AlertTag(name: tagName, createdBy: userService.getUser(), dateCreated: new Date())
                    addAlertTag.save(flush: true)
                }
                aggCaseAlert.addToAlertTags(addAlertTag)
                CRUDService.update(aggCaseAlert)
                activityService.createActivity(executedConfig, aggregateCaseAlertService.getActivityByType(ActivityTypeValue.CategoryAdded),
                        userService.getUser(), "Category Added " + tagName,
                        justification, ['For Aggregate Alert'], aggCaseAlert.productName, aggCaseAlert.pt
                        , aggCaseAlert.assignedTo, null, aggCaseAlert.assignedToGroup)
            }
            removedTags.each { tagName ->
                AlertTag removeAlertTag = AlertTag.findByName(tagName)
                aggCaseAlert.removeFromAlertTags(removeAlertTag)
                CRUDService.update(aggCaseAlert)
                activityService.createActivity(executedConfig, aggregateCaseAlertService.getActivityByType(ActivityTypeValue.CategoryRemoved),
                        userService.getUser(), "Category Removed " + tagName,
                        justification, ['For Aggregate Removed'], aggCaseAlert.productName, aggCaseAlert.pt
                        , aggCaseAlert.assignedTo, null, aggCaseAlert.assignedToGroup)
            }
            updatedTags.each { tagName ->
                Map tagObj = ["name": tagName]
                tagList.add(tagObj)
                peHistoryMap['tagName'] = JsonOutput.toJson(tagList)
            }
        }
        peHistoryMapList.add(peHistoryMap)
        productEventHistoryService.batchPersistHistory(peHistoryMapList)
        render(responseDTO as JSON)
    }

    def alertTagDetails(Long alertId, Boolean isArchived) {
        def domain = aggregateCaseAlertService.getDomainObject(isArchived)
        def aggCaseAlert = domain.findById(alertId)
        List alertTagList = aggCaseAlert.alertTags?.collect { it.name }
        List tagList = AlertTag.list()?.collect { it.name }
        tagList += alertTagService.getMartTagsName()
        render(["tagList": tagList.unique(), alertTagList: alertTagList] as JSON)
    }

    def fetchFreqName(String freq) {
        SubstanceFrequency substanceFrequency = SubstanceFrequency.findByFrequencyNameAndAlertType(freq, "Aggregate Case Alert")
        render(["frequency": substanceFrequency?.miningFrequency] as JSON)

    }

    private Map modelData(Configuration configurationInstance, String action) {
        Long cioms1Id = reportIntegrationService.getCioms1Id()
        boolean hasNormalAlertExecutionAccess = userService.hasNormalAlertExecutionAccess(Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
        if(!hasNormalAlertExecutionAccess){
             hasNormalAlertExecutionAccess = userService.hasNormalAlertExecutionAccess(Constants.AlertConfigType.AGGREGATE_CASE_ALERT_FAERS)
        }
        if(!hasNormalAlertExecutionAccess){
            hasNormalAlertExecutionAccess = userService.hasNormalAlertExecutionAccess(Constants.AlertConfigType.AGGREGATE_CASE_ALERT_VAERS)
        }
        if(!hasNormalAlertExecutionAccess){
            hasNormalAlertExecutionAccess = userService.hasNormalAlertExecutionAccess(Constants.AlertConfigType.AGGREGATE_CASE_ALERT_VIGIBASE)
        }
        if(!hasNormalAlertExecutionAccess){
            hasNormalAlertExecutionAccess = userService.hasNormalAlertExecutionAccess(Constants.AlertConfigType.AGGREGATE_CASE_ALERT_JADER)
        }

        List<User> userList = User.findAllByEnabled(true).sort {
            it.fullName?.toLowerCase()
        }
        Set<String> sMQList = cacheService.getSMQList()
        List<Priority> priorityList = Priority.findAllByDisplay(true)
        Priority priority = Priority.findByDefaultPriority(true)
        Long byDefaultPriority = priority ? priority?.id : null
        List<Map> templateList = []
        if (grailsApplication.config.show.pvreports.templates) {
            templateList = reportIntegrationService.getTemplateList().collect { [id: it.id, name: it.name] }
        }
        def dataSourceMap = getDataSourceMap()

        List<Map> selectDataSourceMap = []
        dataSourceMap.each {
            if (it.key != Constants.DataSource.EUDRA)
                selectDataSourceMap.add([name: it.key, display: it.value])
        }
        if (params.signalId) {
            ValidatedSignal validatedSignal = ValidatedSignal.get(Long.parseLong(params.signalId))
            boolean productExists = true
            if (params.dataSource) {
                productExists = aggregateCaseAlertService.checkProductNameListExistsForFAERS(validatedSignal.productDictionarySelection?:Constants.Commons.DASH_STRING , validatedSignal.productNameList)
                configurationInstance.selectedDatasource = params.dataSource
            }
            if (productExists) {
                configurationInstance.productSelection = validatedSignal.products
                configurationInstance.productGroupSelection = validatedSignal.productGroupSelection
                configurationInstance.isMultiIngredient = validatedSignal.isMultiIngredient
            }
        }
        if (params.selectedDataSource) {
            configurationInstance.selectedDatasource = params.selectedDataSource
        }
        String dataSheetList = ''
        if(params.selectedDataSheet){
            dataSheetList = dataSheetService.formatDatasheetMap(configurationInstance) as JSON
        }
        Map dataSourceEnabledMap = aggregateCaseAlertService.getEnabledOptions()
        if (!(action in [Constants.AlertActions.COPY,Constants.AlertActions.EDIT]) && params.dataSource== null && params.selectedDatasource == null && dataSourceEnabledMap.defaultSelected) {
            // Default Value should be picked from assigned roles while creating alert
            configurationInstance.selectedDatasource = dataSourceEnabledMap.defaultSelected
        }
        List safetyMiningVariables = cacheService.getMiningVariables(Constants.DataSource.PVA)?.collect{it.value.label}
        List faersMiningVariables = cacheService.getMiningVariables(Constants.DataSource.FAERS)?.collect{it.value.label}
        boolean isMultipleDatasource = Holders.config.pvsignal.multiple.datasource.toBoolean()
        Map map = [configurationInstance: configurationInstance, priorityList: priorityList,byDefaultPriority:byDefaultPriority, userList: userList, action: action, templateList: templateList, cioms1Id: cioms1Id, selectDataSourceMap:selectDataSourceMap,appType: Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
                   dataSourceMap        : dataSourceMap, sMQList: sMQList, signalId: params.signalId, productGroupList: productGroupService.fetchProductGroupsListByDisplay(true), spotfireEnabled: Holders.config.signal.spotfire.enabled, isQuant:true,
                   isAutoAssignedTo     : configurationInstance.isMasterTemplateAlert ?: configurationInstance.isAutoAssignedTo, isAutoSharedWith: configurationInstance.autoShareWithUser || configurationInstance.autoShareWithGroup?true:false,
                   enabledOptions       : dataSourceEnabledMap.enabledOptions, defaultSelected:dataSourceEnabledMap.defaultSelected, safetyMiningVariables:safetyMiningVariables, faersMiningVariables:faersMiningVariables,
                   hasNormalAlertExecutionAccess: hasNormalAlertExecutionAccess, isMultipleDatasource: isMultipleDatasource, dataSheetList:dataSheetList
        ]
        return map
    }

    @Secured(['ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def changeDisposition(String selectedRows, Disposition targetDisposition,
                          String justification, String validatedSignalName, String productJson,Long signalId, String incomingDisposition) {
        boolean isArchived = params.boolean('isArchived')
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        List<Long> multipleExecutedConfigIdList = JSON.parse(selectedRows).collect { Map<String, Long> selectedRow ->
            selectedRow["executedConfigObj.id"] != "" ? selectedRow["executedConfigObj.id"] as Long : null
        }.unique()
        if (multipleExecutedConfigIdList.size() > 1) {
            responseDTO.status = false
            responseDTO.message = message(code: "multiple.alert.disposition.change.error.PECs")
            render(responseDTO as JSON)
            return
        }
        try {
            validatedSignalName = validatedSignalName? org.apache.commons.lang.StringEscapeUtils.unescapeHtml(validatedSignalName) : ''
            List<Long> aggCaseAlertIdList = JSON.parse(selectedRows).collect{ Map<String, Long> selectedRow ->
                selectedRow["alert.id"]!=""?selectedRow["alert.id"] as Long:null
            }
            def jsonSlurper = new JsonSlurper()
            def productJsonMap = [:]
            if (productJson) {
                productJsonMap = jsonSlurper.parseText(productJson) as Map<String, Object>
            }
            responseDTO.data = aggregateCaseAlertService.changeDisposition(aggCaseAlertIdList, targetDisposition,
                    justification, validatedSignalName, productJsonMap, isArchived,signalId, incomingDisposition)
            def domain = aggregateCaseAlertService.getDomainObject(isArchived)
            Long configId = domain?.get(aggCaseAlertIdList[0])?.executedAlertConfiguration?.id
            Long countOfPreviousDisposition
            if(params.callingScreen != Constants.Commons.DASHBOARD){
                countOfPreviousDisposition = alertService.fetchPreviousDispositionCount(configId, params.incomingDisposition, domain)
            }else{
                countOfPreviousDisposition = alertService.fetchPreviousDispositionCountDashboard(params.incomingDisposition, domain)
            }
            responseDTO.data << [incomingDisposition:params.incomingDisposition, countOfPreviousDisposition:countOfPreviousDisposition]
            if (targetDisposition?.id) {
                aggregateCaseAlertService.persistAlertDueDate(responseDTO?.data?.alertDueDateList)
            }
            if(!responseDTO.data.dispositionChanged){
                responseDTO.status = false
                responseDTO.message = message(code: "app.label.disposition.change.error.refresh")
            }
        } catch (grails.validation.ValidationException vx) {
            vx.printStackTrace()
            responseDTO.status = false
            responseDTO.message = MiscUtil.getCustomErrorMessageList(vx)[0]
        }
        catch (Exception e) {
            e.printStackTrace()
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.disposition.change.error")
        }
        render(responseDTO as JSON)
    }

    def changeAlertLevelDisposition(Disposition targetDisposition,String justificationText,ExecutedConfiguration execConfig, Boolean isArchived){
        def domain = aggregateCaseAlertService.getDomainObject(isArchived)
        String alertName = execConfig?.name
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true, message: message(code: "alert.level.disposition.successfully.updated"))
        try {
            AlertLevelDispositionDTO alertLevelDispositionDTO = dispositionService.populateAlertLevelDispositionDTO(targetDisposition, justificationText, domain, execConfig)
            Integer updatedRowsCount = aggregateCaseAlertService.changeAlertLevelDisposition(alertLevelDispositionDTO, isArchived)
            List removedDispositionNames = []
            removedDispositionNames = Disposition.findAllByReviewCompleted(false).collect {
                it.displayName
            }
            responseDTO.data = [removedDispositionNames:removedDispositionNames]
            if(updatedRowsCount <= 0){
                responseDTO.status = true
                responseDTO.message = message(code: "alert.level.review.completed")
            } else {
                dispositionService.sendDispChangeNotification(targetDisposition, alertName)
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex)
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.disposition.change.error")
        }
        render(responseDTO as JSON)
    }

    @Secured(['ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def revertDisposition(Long id, String justification) {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            responseDTO.data = aggregateCaseAlertService.revertDisposition(id, justification)
            Long configId = AggregateCaseAlert.get(id)?.executedAlertConfiguration?.id
            Long countOfPreviousDisposition
            if (params.callingScreen != Constants.Commons.DASHBOARD) {
                countOfPreviousDisposition = alertService.fetchPreviousDispositionCount(configId, responseDTO?.data?.oldDispName, AggregateCaseAlert)
            } else {
                countOfPreviousDisposition = alertService.fetchPreviousDispositionCountDashboard(responseDTO?.data?.oldDispName, AggregateCaseAlert)
            }
            responseDTO.data << [incomingDisposition: responseDTO?.data?.oldDispName, targetDisposition: responseDTO?.data?.newDispName, countOfPreviousDisposition: countOfPreviousDisposition]
            aggregateCaseAlertService.persistAlertDueDate(responseDTO?.data?.alertDueDateList)
            if(!responseDTO.data.dispositionReverted){
                responseDTO.status = false
                responseDTO.message = message(code: "app.label.undo.disposition.change.error.refresh")
            }
        } catch (Exception e) {
            e.printStackTrace()
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.undo.disposition.change.error")
        }
        render(responseDTO as JSON)
    }

    @Secured(['ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def archivedAlert(Long id) {
        Map executedConfigurationList = archivedAlertList(id, params, false)
        render(executedConfigurationList as JSON)
    }

    def fetchPossibleValues(Long executedConfigId) {
        Map<String, List> possibleValuesMap = [:]
        possibleValuesMap.put("alertTags", AlertTag.list()?.collect { [id: it.name, text: it.name] })
        alertService.preparePossibleValuesMap(AggregateCaseAlert, possibleValuesMap, executedConfigId)
        String fullName = ""
        for(int i=0; i<(possibleValuesMap?.soc)?.size(); i++) {
            if(possibleValuesMap.soc[i].id == Constants.Commons.SMQ_ADVANCEDFILTER_LABEL){
                fullName = Constants.Commons.SMQ_ADVANCEDFILTER_LABEL
            }else {
                fullName = dataObjectService.getAbbreviationMap(possibleValuesMap.soc[i].id)
                if(!fullName){
                    fullName =possibleValuesMap.soc[i].text
                }
            }
            possibleValuesMap.soc[i].text=fullName
        }
        possibleValuesMap.soc = possibleValuesMap?.soc?.sort{it?.text}
        List<String> yesNoFieldsList = ["listed","positiveRechallenge","positiveDechallenge", "related", "pregenency",
                                        "listedFaers","positiveRechallengeFaers", "positiveDechallengeFaers","relatedFaers", "pregenencyFaers",
                                        "sdrEvdas","sdrPaedEvdas", "sdrGeratrEvdas", "currentRun"]
        List<Map<String, String>> yesNoMapList = [[id: "Yes", text: "Yes"], [id: "No", text: "No"]]
        possibleValuesMap.put(Constants.AggregateAlertFields.TREND_FLAG,[Constants.Commons.NEW_UPPERCASE, Constants.Commons.NO_UPPERCASE, Constants.Commons.YES_UPPERCASE])
        yesNoFieldsList.each {
            possibleValuesMap.put(it, yesNoMapList)
        }
        List<Map> codeValues = pvsGlobalTagService.fetchTagsAndSubtags()
        codeValues.each{
            it.id = it.text
        }
        List<Map> tags = pvsGlobalTagService.fetchTagsfromMart(codeValues)
        List<Map> subTags = pvsGlobalTagService.fetchSubTagsFromMart(codeValues)
        possibleValuesMap.put("tags" , tags.unique{it.text.toUpperCase()})
        possibleValuesMap.put("subTags" , subTags.unique{it.text.toUpperCase()})
        render possibleValuesMap as JSON
    }

    def fetchAllFieldValues() {
        ExecutedConfiguration executedConfiguration
        if(params){
            executedConfiguration = ExecutedConfiguration.get(params.executedConfigId as Long)
        }
        List<Map> fieldList = []
        if(executedConfiguration?.selectedDatasource == Constants.DataSource.JADER){
            fieldList  =  aggregateCaseAlertService.fieldListAdvanceFilterJader(executedConfiguration?.groupBySmq as boolean)
        }else{
            fieldList = aggregateCaseAlertService.fieldListAdvanceFilter(executedConfiguration?.selectedDatasource, executedConfiguration?.groupBySmq as boolean,
                    params.callingScreen,'AGGREGATE_CASE_ALERT')
        }
        render fieldList as JSON
    }

    def fetchFaersDisabledColumnsIndexes(){
        render( ["disabledIndexValues": Holders.config.pvsignal.faers.disabled.columns.indexes] as JSON)
    }

    @Secured(['ROLE_SHARE_GROUP','ROLE_SHARE_ALL'])
    def editShare() {
        Boolean sharedWithoutException = true
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(Long.parseLong(params.executedConfigId))
        try {
            aggregateCaseAlertService.persistDataToShareWithExconfig(params)
        } catch (Exception e) {
            sharedWithoutException = false
            e.printStackTrace()
        }
        if(sharedWithoutException){
            flash.message = message(code: "app.configuration.share.success", args: [executedConfiguration?.name])
        }else{
            flash.warn = message(code: "app.configuration.share.warning", args: [executedConfiguration?.name])
        }
        redirect action: "review"
    }

    def fetchSubGroupsMap() {
        Map<String, Map> fullMap = cacheService.getSubGroupMap()
        Boolean isAgeEnabled
        Boolean isGenderEnabled
        Map subGroupsMap = new LinkedHashMap()
        String ageGroupField = params.dataSource == "faers" ? Holders.config.subgrouping.faers.ageGroup.name : Holders.config.subgrouping.ageGroup.name
        String gender = params.dataSource == "faers" ? Holders.config.subgrouping.faers.gender.name : Holders.config.subgrouping.gender.name
        fullMap.each { key, value ->
            fullMap.containsKey(ageGroupField) ? subGroupsMap.put(Holders.config.subgrouping.ageGroup.name, fullMap[ageGroupField].collect {
                it.value
            }) : null
            fullMap.containsKey(gender) ? subGroupsMap.put(Holders.config.subgrouping.gender.name, fullMap[gender].collect {
                it.value
            }) : null
        }
        isAgeEnabled = fullMap.containsKey(ageGroupField) ? true : false
        isGenderEnabled = fullMap.containsKey(gender) ? true : false
        Map finalMap = [subGroupsMap: subGroupsMap, isAgeEnabled: isAgeEnabled, isGenderEnabled: isGenderEnabled]
        render finalMap as JSON
    }

    def fetchSubGroupsMapIntegratedReview() {
        Map<String, Map> fullMap = cacheService.getSubGroupMap()
        Boolean isAgeEnabled
        Boolean isGenderEnabled
        Map<String,List<String>> getAllOtherSubGroupColumnsListMap = cacheService.getAllOtherSubGroupColumnsCamelCase(Constants.DataSource.PVA)
        List subGroupColumnInfo = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT').findAll{it.type=="subGroup" && it.enabled== true}.collect {it.name}
        Map subGroupsMap = new LinkedHashMap()
        fullMap.containsKey(Holders.config.subgrouping.ageGroup.name) ? subGroupsMap.put(Holders.config.subgrouping.ageGroup.name, fullMap[Holders.config.subgrouping.ageGroup.name].collect {
            it.value
        }) : null
        fullMap.containsKey(Holders.config.subgrouping.gender.name) ? subGroupsMap.put(Holders.config.subgrouping.gender.name, fullMap[Holders.config.subgrouping.gender.name].collect {
            it.value
        }) : null

        fullMap.containsKey(Holders.config.subgrouping.faers.ageGroup.name) ? subGroupsMap.put(Holders.config.subgrouping.faers.ageGroup.name, fullMap[Holders.config.subgrouping.faers.ageGroup.name].collect {
            it.value
        }) : null
        fullMap.containsKey(Holders.config.subgrouping.faers.gender.name) ? subGroupsMap.put(Holders.config.subgrouping.faers.gender.name, fullMap[Holders.config.subgrouping.faers.gender.name].collect {
            it.value
        }) : null

        isAgeEnabled = fullMap.containsKey(Holders.config.subgrouping.ageGroup.name)
        isGenderEnabled = fullMap.containsKey(Holders.config.subgrouping.gender.name)
        Boolean rorRelSubGrpEnabled = cacheService.getSubGroupsEnabled("rorRelSubGrpEnabled") ?: false
        Map finalMap = [subGroupsMap: subGroupsMap, isAgeEnabled: isAgeEnabled, isGenderEnabled: isGenderEnabled,allSubGroupMap:getAllOtherSubGroupColumnsListMap,rorRelSubGrpEnabled:rorRelSubGrpEnabled,subGroupColumnInfo:subGroupColumnInfo]
        render finalMap as JSON
    }

    private Integer getDefaultReviewPeriod(Configuration config){
        Disposition defaultDisposition = userService.getUser().workflowGroup.defaultQuantDisposition
        List<PriorityDispositionConfig> dispositionConfigs = cacheService.getDispositionConfigsByPriority(config.priority.id)
        Integer reviewPeriod = dispositionConfigs?.find{it.disposition == defaultDisposition}?.reviewPeriod
        reviewPeriod?: config.priority.reviewPeriod
    }

    private boolean isExcelExportFormat(String outputFormat) {
        return outputFormat == ReportFormat.XLSX.name() ? true : false
    }

    String getEvdasAndFaersDateRange() {
        String productGroupId = params.productGroupId
        Sql evdasSql
        String evdasDataJson
        EvdasFileProcessLog evdasFileProcessLog
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        String faersDate
        Sql faersSql
        Map dateRange =[:]
        try {
            if (params.selectedDatasource.contains("faers")) {
                faersSql = new Sql(dataSource_faers)
                String faers_statement = SignalQueryHelper.faers_date_range()
                faersSql.eachRow(faers_statement, []) { resultSetObj ->
                    faersDate = resultSetObj
                }
                int lenght = faersDate.size()
                String year = faersDate.substring(lenght - 5, lenght - 1)
                if (faersDate.contains('MAR')) {
                    faersDate = '01-Jan-' + year + ' to 31-Mar-' + year
                } else if (faersDate.contains('JUN')) {
                    faersDate = '01-Apr-' + year + ' to 30-Jun-' + year
                } else if (faersDate.contains('SEP')) {
                    faersDate = '01-Jul-' + year + ' to 30-Sep-' + year
                } else if (faersDate.contains('DEC')) {
                    faersDate = '01-Oct-' + year + ' to 31-Dec-' + year
                }
                dateRange.faersDate=faersDate
            }

            if (params.selectedDatasource.contains("eudra") && productGroupId) {
                evdasSql =new Sql(dataSource_eudra)
                evdasSql.eachRow("select GRP_DATA from VW_DICT_GRP_DATA where DICT_GRP_ID IN ("+productGroupId+")") { ResultSet resultSetObj ->
                    Clob clob = resultSetObj.getClob("GRP_DATA")
                    if (clob) {
                        evdasDataJson = clob.getSubString(1, (int) clob.length())
                    }
                }
                String substance = getNameFieldFromJson(evdasDataJson)
                evdasFileProcessLog = EvdasFileProcessLog.createCriteria().get {
                    eq('substances', substance, [ignoreCase: true])
                    eq('dataType', 'eRMR')
                    eq('status', EvdasFileProcessState.SUCCESS)
                    order('recordEndDate', 'desc')
                    maxResults(1)
                } as EvdasFileProcessLog
                if (!evdasFileProcessLog) {
                    render([evdasStartDate: sdf.format(new Date()), evdasEndDate: sdf.format(new Date() + 10),faersDate: faersDate, status: "success"] as JSON)
                    return
                }
                dateRange.evdasStartDate = sdf.format(evdasFileProcessLog.recordStartDate)
                dateRange.evdasEndDate = sdf.format(evdasFileProcessLog.recordEndDate)
            }



        } catch (Exception ex) {
            ex.printStackTrace()
        } finally {
            if (params.selectedDatasource.contains("eudra")) {
                evdasSql?.close()
            }
            if (params.selectedDatasource.contains("faers")) {
                faersSql?.close()
            }
        }

        render(dateRange as JSON)
    }
    def fetchMiningVariables(String selectedDatasource){
        List miningVariables
        if (selectedDatasource.equals("pva")) {
            miningVariables = cacheService.getMiningVariables("pva")?.collect {
                ['label'         : it.value.label, 'use_case': it.value.use_case, "isMeddra": it.value.isMeddra,
                 "isOob"         : it.value.isOob,
                 'isautocomplete': it.value.isautocomplete
                 , 'dic_level'   : it.value.dic_level, 'dic_type': it.value.dic_type
                 , 'validatable' : it.value.validatable]
            }
        } else if (selectedDatasource.equals("faers")) {
            miningVariables = cacheService.getMiningVariables("faers")?.collect {
                ['label'         : it.value.label, 'use_case': it.value.use_case, "isMeddra": it.value.isMeddra,
                 "isOob"         : it.value.isOob,
                 'isautocomplete': it.value.isautocomplete
                 , 'dic_level'   : it.value.dic_level, 'dic_type': it.value.dic_type
                 , 'validatable' : it.value.validatable]
            }
        }
        render(miningVariables as JSON)
    }

    String getVaersDateRange() {
        String vaersDate = ''
        Date vaersDateEnd
        Date startDateEnd
        Sql vaersSql = new Sql(dataSource_vaers)
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            String vaers_statement = SignalQueryHelper.vaers_date_range()
            vaersSql.eachRow(vaers_statement, []) { resultSetObj ->
                vaersDate = resultSetObj
            }
            int lastChar = vaersDate.lastIndexOf(':')
            String vaersDateString = lastChar != Constants.Commons.UNDEFINED_NUM_INT_REVIEW ? vaersDate.substring(lastChar + 1, vaersDate.length() - 1) : ""
            //Updated Vaers Date Range for Integrated Alert PVS-67898
            String[] vaersDateRange = vaersDateString.split("/")
            String vaersStartDate = ""
            String vaersEndDate = ""
            if (vaersDateRange?.size() == 2) {
                vaersStartDate = vaersDateRange[0].trim()
                vaersEndDate = vaersDateRange[1].trim()
            }
            vaersDateEnd = vaersEndDate != "" ? DateUtil.StringToDate(vaersEndDate, Constants.DateFormat.DISPLAY_NEW_DATE) : null
            startDateEnd = vaersStartDate != "" ? DateUtil.StringToDate(vaersStartDate, Constants.DateFormat.DISPLAY_NEW_DATE) : null
            vaersDate = DateUtil.toDateStringWithoutTimezone(startDateEnd) + ' to ' + DateUtil.toDateStringWithoutTimezone(vaersDateEnd)
            responseDTO.data = vaersDate
        } catch (Exception ex) {
            log.error(ex.printStackTrace())
            responseDTO.status = false
        } finally {
            vaersSql?.close()
        }
        render(responseDTO as JSON)
    }

    String getFaersLatestQuarter() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        String faersDate
        Sql faersSql
        Map dateRange = [:]
        try {
            if (params.selectedDatasource.contains("faers")) {
                faersSql = new Sql(dataSource_faers)
                String faers_statement = SignalQueryHelper.faers_date_range()
                faersSql.eachRow(faers_statement, []) { resultSetObj ->
                    faersDate = resultSetObj
                }
                int length = faersDate.size()
                String year = faersDate.substring(length - 5, length - 1)
                if (faersDate.contains('MAR')) {
                    faersDate = '31-Mar-' + year
                } else if (faersDate.contains('JUN')) {
                    faersDate = '30-Jun-' + year
                } else if (faersDate.contains('SEP')) {
                    faersDate = '30-Sep-' + year
                } else if (faersDate.contains('DEC')) {
                    faersDate = '31-Dec-' + year
                }
                dateRange.faersDate = faersDate
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        } finally {
            if (params.selectedDatasource.contains("faers")) {
                faersSql?.close()
            }
        }

        render(dateRange as JSON)
    }

    String getVigibaseLatestQuarter() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        String vigibaseDate
        Sql vigibaseSql
        Map dateRange = [:]
        try {
            if (params.selectedDatasource.contains("vigibase")) {
                vigibaseSql = new Sql(dataSource_vigibase)
                String vigibase_statement = SignalQueryHelper.vigibase_date_range_display()
                vigibaseSql.eachRow(vigibase_statement, []) { resultSetObj ->
                    vigibaseDate = resultSetObj
                }
                int lastChar = vigibaseDate?.lastIndexOf(':')
                String vigibaseDateString = vigibaseDate?.substring(lastChar + 1, vigibaseDate.length() - 1)
                Date vigibaseEndDate = DateUtil.StringToDate(vigibaseDateString, Constants.DateFormat.DISPLAY_NEW_DATE)
                vigibaseDate = DateUtil.toDateStringWithoutTimezone(vigibaseEndDate)
                dateRange.vigibaseDate = vigibaseDate
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        } finally {
            if (params.selectedDatasource.contains("vigibase") && Objects.nonNull(vigibaseSql)) {
                vigibaseSql?.close()
            }
        }

        render(dateRange as JSON)
    }
    String getJaderLatestQuarter() {
        Map dateRange = jaderAlertService.getJaderLatestQuarter()
        render(dateRange as JSON)
    }

    String getEvdasAndVigibaseDateRange() {
        String productGroupId = params.productGroupId
        Sql evdasSql
        String evdasDataJson
        EvdasFileProcessLog evdasFileProcessLog
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        String vigibaseDate
        Sql vigibaseSql
        Map dateRange =[:]
        try {
            if (params.selectedDatasource.contains("vigibase")) {
                vigibaseSql = new Sql(dataSource_vigibase)
                String vigibase_statement = SignalQueryHelper.vigibase_date_range_display()
                vigibaseSql.eachRow(vigibase_statement, []) { resultSetObj ->
                    vigibaseDate = resultSetObj
                }
                int lenght = vigibaseDate.size()
                String year = vigibaseDate.substring(lenght - 5, lenght - 1)
                int lastChar = vigibaseDate.lastIndexOf(':')
                String vigibaseDateString = vigibaseDate.substring(lastChar + 1, vigibaseDate.length() - 1)
                Date vigibaseEndDate = DateUtil.StringToDate(vigibaseDateString, Constants.DateFormat.DISPLAY_NEW_DATE)
                Date vigibaseStartDate = vigibaseEndDate?.minus(90)
                dateRange.vigibaseDate = (params.dateRange == DateRangeEnum.CUMULATIVE) ? "" : (DateUtil.toDateStringWithoutTimezone(vigibaseStartDate) + ' to ' + DateUtil.toDateStringWithoutTimezone(vigibaseEndDate))
            }

            if (params.selectedDatasource.contains("eudra")) {
                evdasSql =new Sql(dataSource_eudra)
                evdasSql.eachRow("select GRP_DATA from VW_DICT_GRP_DATA where DICT_GRP_ID IN ("+productGroupId+")") { ResultSet resultSetObj ->
                    Clob clob = resultSetObj.getClob("GRP_DATA")
                    if (clob) {
                        evdasDataJson = clob.getSubString(1, (int) clob.length())
                    }
                }
                String substance = getNameFieldFromJson(evdasDataJson)
                evdasFileProcessLog = EvdasFileProcessLog.createCriteria().get {
                    eq('substances', substance, [ignoreCase: true])
                    eq('dataType', 'eRMR')
                    eq('status', EvdasFileProcessState.SUCCESS)
                    order('recordEndDate', 'desc')
                    maxResults(1)
                } as EvdasFileProcessLog
                if (!evdasFileProcessLog) {
                    render([evdasStartDate: sdf.format(new Date()), evdasEndDate: sdf.format(new Date() + 10),vigibaseDate: dateRange.vigibaseDate, status: "success"] as JSON)
                    return
                }
                dateRange.evdasStartDate = sdf.format(evdasFileProcessLog.recordStartDate)
                dateRange.evdasEndDate = sdf.format(evdasFileProcessLog.recordEndDate)
            }



        } catch (Exception ex) {
            ex.printStackTrace()
        } finally {
            if (params.selectedDatasource.contains("eudra") && Objects.nonNull(evdasSql)) {
                evdasSql?.close()
            }
            if (params.selectedDatasource.contains("vigibase") && Objects.nonNull(vigibaseSql)) {
                vigibaseSql?.close()
            }
        }

        render(dateRange as JSON)
    }

    String getVigibaseEvdasAndFaersDateRange() {
        String productGroupId = params.productGroupId
        Sql evdasSql
        String evdasDataJson
        EvdasFileProcessLog evdasFileProcessLog
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        String faersDate
        Sql faersSql
        String vigibaseDate
        Sql vigibaseSql
        Map dateRange = [:]
        try {
            if (params.selectedDatasource.contains("faers")) {
                faersSql = new Sql(dataSource_faers)
                String faers_statement = SignalQueryHelper.faers_date_range()
                faersSql.eachRow(faers_statement, []) { resultSetObj -> faersDate = resultSetObj
                }
                int lenght = faersDate.size()
                String year = faersDate.substring(lenght - 5, lenght - 1)
                if (faersDate.contains('MAR')) {
                    faersDate = '01-Jan-' + year + ' to 31-Mar-' + year
                } else if (faersDate.contains('JUN')) {
                    faersDate = '01-Apr-' + year + ' to 30-Jun-' + year
                } else if (faersDate.contains('SEP')) {
                    faersDate = '01-Jul-' + year + ' to 30-Sep-' + year
                } else if (faersDate.contains('DEC')) {
                    faersDate = '01-Oct-' + year + ' to 31-Dec-' + year
                }
                dateRange.faersDate = faersDate
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        } finally {
            if (params.selectedDatasource.contains("faers") && Objects.nonNull(faersSql)) {
                faersSql?.close()
            }
        }

        try {
            if (params.selectedDatasource.contains("vigibase")) {
                vigibaseSql = new Sql(dataSource_vigibase)
                String vigibase_statement = SignalQueryHelper.vigibase_date_range_display()
                vigibaseSql.eachRow(vigibase_statement, []) { resultSetObj -> vigibaseDate = resultSetObj
                }
                int lastChar = vigibaseDate.lastIndexOf(':')
                String vigibaseDateString = vigibaseDate.substring(lastChar + 1, vigibaseDate.length() - 1)
                Date vigibaseEndDate = DateUtil.StringToDate(vigibaseDateString, Constants.DateFormat.DISPLAY_NEW_DATE)
                Date vigibaseStartDate = vigibaseEndDate?.minus(90)
                String dateRangeVigibase = DateUtil.toDateStringWithoutTimezone(vigibaseStartDate) + ' to ' + DateUtil.toDateStringWithoutTimezone(vigibaseEndDate)
                dateRange.vigibaseDate = (params.dateRange == DateRangeEnum.CUMULATIVE) ? "" : dateRangeVigibase

            }
        } catch (Exception ex) {
            ex.printStackTrace()
        } finally {
            if (params.selectedDatasource.contains("vigibase") && Objects.nonNull(vigibaseSql)) {
                vigibaseSql?.close()
            }
        }


        try {


            if (params.selectedDatasource.contains("eudra")) {
                evdasSql = new Sql(dataSource_eudra)
                evdasSql.eachRow("select GRP_DATA from VW_DICT_GRP_DATA where DICT_GRP_ID IN (" + productGroupId + ")") { ResultSet resultSetObj ->
                    Clob clob = resultSetObj.getClob("GRP_DATA")
                    if (clob) {
                        evdasDataJson = clob.getSubString(1, (int) clob.length())
                    }
                }
                String substance = getNameFieldFromJson(evdasDataJson)
                evdasFileProcessLog = EvdasFileProcessLog.createCriteria().get {
                    eq('substances', substance, [ignoreCase: true])
                    eq('dataType', 'eRMR')
                    eq('status', EvdasFileProcessState.SUCCESS)
                    order('recordEndDate', 'desc')
                    maxResults(1)
                } as EvdasFileProcessLog
                if (!evdasFileProcessLog) {
                    render([evdasStartDate: sdf.format(new Date()), evdasEndDate: sdf.format(new Date() + 10), vigibaseDate: dateRange.vigibaseDate, faersDate: faersDate, status: "success"] as JSON)
                    return
                }
                dateRange.evdasStartDate = sdf.format(evdasFileProcessLog.recordStartDate)
                dateRange.evdasEndDate = sdf.format(evdasFileProcessLog.recordEndDate)
            }

        } catch (Exception ex) {
            ex.printStackTrace()
        } finally {
            if (params.selectedDatasource.contains("eudra") && Objects.nonNull(evdasSql)) {
                evdasSql?.close()
            }
        }

        render(dateRange as JSON)
    }

    def fetchProductTypeForDataSources(){
        List<Map> mapList = ProductTypeConfiguration.list().collect {
            [id: it.id, name: it.name, dataSource: 'pva', default: it.isDefault, productType: it.productType, roleType: it.roleType]
        }
        /*
             grouping by name of configuration and
             saving product and role combination info
             in keys, default should be || of all
             objects
         */
        // Added for safety dataSource product type
        Long defaultVaersId = Long.MAX_VALUE
        for (Map<String, Object> m : mapList) {
            String productType =  m?.get("productType")
            String roleType = m?.get("roleType")
            if (productType?.toUpperCase()?.contains("VACCINE") && roleType?.toUpperCase()?.contains("SUSPECT")) {
                Long id = m.get("id")
                if(id < defaultVaersId){
                    defaultVaersId = id
                }
            }
        }
        mapList = mapList.groupBy { it.name.toLowerCase() }.collect { k, v ->
            [
                    id           : v.sort { it.name }[0]?.id,
                    name         : v.name.sort()[0],
                    dataSource   : 'pva',
                    default      : (v.default - false).size() == 1,
                    productType  : v.productType.unique().join(", "),
                    isDefaultVaers  : v.sort { it.name }[0]?.id == defaultVaersId ? true : false,
                    roleType     : v.roleType.unique().join(", "),
                    displaySource: 'Safety DB',
                    isManual     : false
            ]
        }
        /*
            default options for other datasource,
            we need to fetch ID's for productType and
            roleType for inserting in GTTs
         */
        mapList.addAll(
                [id: 'DRUG_SUSPECT_FAERS', name: "Drug(F)-S", dataSource: 'faers', default: true, productType: "Drug", roleType: "Suspect", displaySource: 'FAERS', isManual: false],
                [id: 'DRUG_SUSPECT_CONCOMITANT_FAERS', name: "Drug(F)-S+C", dataSource: 'faers', default: false, productType: "Drug", roleType: "Suspect,Concomitant", displaySource: 'FAERS', isManual: false],
                [id: 'VACCINE_SUSPECT_VAERS', name: "Vaccine(VA)-S", dataSource: 'vaers', default: true, productType: "Vaccine", roleType: "Suspect", displaySource: 'VAERS', isManual: false],
                [id: 'DRUG_SUSPECT_VIGIBASE', name: "Drug(VB)-S", dataSource: 'vigibase', default: true, productType: "Drug", roleType: "Suspect", displaySource: 'VigiBase', isManual: false],
                [id: 'DRUG_SUSPECT_CONCOMITANT_VIGIBASE', name: "Drug(VB)-S+C", dataSource: 'vigibase', default: false, productType: "Drug", roleType: "Suspect,Concomitant", displaySource: 'VigiBase', isManual: false],
                [id: 'VACCINE_SUSPECT_VIGIBASE', name: "Vaccine(VB)-S", dataSource: 'vigibase', default: false, productType: "Vaccine", roleType: "Suspect", displaySource: 'VigiBase', isManual: false],
                [id: 'DRUG_SUSPECT_JADER', name: "Drug(J)-S", dataSource: 'jader', default: true, productType: "Drug", roleType: "Suspect", displaySource: 'JADER', isManual: false],
                [id: 'DRUG_SUSPECT_CONCOMITANT_JADER', name: "Drug(J)-S+C", dataSource: 'jader', default: false, productType: "Drug", roleType: "Suspect,Concomitant", displaySource: 'JADER', isManual: false],

        )
        mapList.sort {it?.name?.toLowerCase() }
        mapList
    }

    def changeFilterAttributes(){
        HttpSession session = request.getSession()
        session.removeAttribute("selectedAlertsFilter")
        session.setAttribute("agg",params["selectedAlertsFilter"])
        render([status:200] as JSON)
    }

    @Secured(['ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def dssScores(Long configId, Long rowId, boolean isArchived) {
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.findById(params.configId)
        if ((alertService.checkAlertSharedToCurrentUser(executedConfiguration) || !alertService.roleAuthorised(Constants.AlertConfigType.AGGREGATE_CASE_ALERT,executedConfiguration?.selectedDatasource))) {
            forward(controller: 'errors', action: 'permissionsError')
            return
        }
        String dssNetworkUrl = grailsApplication.config.dss.scores.url + grailsApplication.config.dss.network.path
        String fullUserName = userService.getUserFromCacheByUsername(userService.getCurrentUserName())?.fullName ?:""
        String finalUrl = dssNetworkUrl + "?alert_id=${configId}&row_id=${rowId}&archived=${isArchived}&username=${fullUserName}"
        return [finalUrl: finalUrl]
    }
}
