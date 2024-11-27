package com.rxlogix

import com.rxlogix.attachments.Attachment
import com.rxlogix.audit.AuditTrail
import com.rxlogix.config.*
import com.rxlogix.dto.AlertDataDTO
import com.rxlogix.dto.AlertLevelDispositionDTO
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.JustificationFeatureEnum
import com.rxlogix.enums.ReportFormat
import com.rxlogix.helper.LinkHelper
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
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import oracle.sql.DATE
import org.apache.commons.lang3.time.DateUtils
import org.apache.http.util.TextUtils
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.hibernate.sql.JoinType
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import javax.servlet.http.HttpSession
import java.text.ParseException
import java.util.concurrent.ConcurrentHashMap

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND

@Secured(["isAuthenticated()"])
class EvdasAlertController implements AlertUtil, LinkHelper, AlertAsyncUtil {

    def CRUDService
    def userService
    def queryService
    def emailService
    def activityService
    def evdasAlertService
    def evdasHistoryService
    def configurationService
    def actionTemplateService
    def validatedSignalService
    def medicalConceptsService
    def productBasedSecurityService
    def productEventHistoryService
    def reportExecutorService
    def evdasAlertExecutionService
    def dynamicReportService
    def attachmentableService
    def viewInstanceService
    def workflowRuleService
    def priorityService
    def alertService
    def dispositionService
    def actionService
    def alertCommentService
    def aggregateCaseAlertService
    EmailNotificationService emailNotificationService
    AdvancedFilterService advancedFilterService
    def cacheService
    def dataSheetService
    def signalAuditLogService
    def dataObjectService
    ImportConfigurationService importConfigurationService
    public final static String JSON_DATE = "yyyy-MM-dd'T'HH:mmXXX"

    def index() {
        render view: "review"
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION'])
    def create() {
        EvdasConfiguration evdasConfig = new EvdasConfiguration(adhocRun: true)

        if (params.signalId) {
            ValidatedSignal validatedSignal = ValidatedSignal.get(Long.parseLong(params.signalId))
            if(validatedSignal.products) {
                boolean productExists = evdasAlertService.checkProductExistsForEvdas(validatedSignal.productDictionarySelection, validatedSignal.productNameList)
                if (productExists) {
                    evdasConfig.productSelection = validatedSignal.products
                }
            }
            if(validatedSignal.productGroupSelection) {
                evdasConfig.productGroupSelection = validatedSignal.productGroupSelection
            }
        }
        Map model = modelData(evdasConfig, Constants.AlertActions.CREATE)
        model << [signalId: params.signalId]
        render(view: "create", model: model)
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION'])
    def save() {
        params?.name = params?.name?.trim()?.replaceAll("\\s{2,}", " ")
        EvdasConfiguration configurationInstance = EvdasConfiguration.get(params.configurationInstanceId) ?: new EvdasConfiguration()
        bindData(configurationInstance, params, [exclude: ["eventGroupSelection","owner", "tags","sharedWith","productGroupSelection"]])
        if(params.eventGroupSelection!="[]"){
            configurationInstance.eventGroupSelection=params.eventGroupSelection
        }
        try {
            configurationInstance.owner = userService.getUser()
            configurationInstance.isResume = false
            configurationInstance.isDatasheetChecked = false
            configurationInstance.isEnabled = params.isEnabled ?: false
            if(params.productGroupSelection != '[]'){
                configurationInstance.productGroupSelection=params.productGroupSelection
            }
            //Set the workflow group from the logged in user.
            configurationInstance.workflowGroup = userService.getUser().workflowGroup

            setNextRunDateAndScheduleDateJSON(configurationInstance)

            configurationInstance.dateRangeInformation = setDateRange(configurationInstance)
            configurationInstance = userService.assignGroupOrAssignTo(params[Constants.ASSIGN_TO_PARAM], configurationInstance)
            userService.bindSharedWithConfiguration(configurationInstance, params.sharedWith, true)
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
            if(params.selectedDatasheet && params.selectedDatasheet == 'on'){
                configurationInstance.datasheetType = params.allSheets?:Constants.DatasheetOptions.CORE_SHEET
                configurationInstance.isDatasheetChecked = true
            }else{
                configurationInstance.isDatasheetChecked = false
            }
            evdasAlertService.bindDatasheetData(configurationInstance, params.dataSheet ? [params.dataSheet]?.flatten() : null)
            configurationInstance = (EvdasConfiguration) CRUDService.saveWithAuditLog(configurationInstance)
            //Related to linked alert configurations. If params are passed then it will be attached to the signal.
            if (!TextUtils.isEmpty(params.signalId)) {
                validatedSignalService.addEvdasConfigurationToSignal(params.signalId, configurationInstance)
            }
        } catch (ValidationException ve) {
            if (Boolean.parseBoolean(params.repeatExecution)) {
                configurationInstance.setIsEnabled(true)
            } else {
                configurationInstance.setIsEnabled(false)
            }
            configurationInstance.errors = ve.errors
            renderOnErrorScenario(configurationInstance)
            return
        }catch (Exception exception) {
            flash.error = message(code: "app.label.alert.error")
            renderOnErrorScenario(configurationInstance)
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'app.label.evdas.alert.configuration'), configurationInstance.name])
        redirect(controller: "evdasAlert", action: "view", id: configurationInstance.id)
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION'])
    def update() {
        params?.name = params?.name?.trim()?.replaceAll("\\s{2,}", " ")
        EvdasConfiguration configurationInstance = EvdasConfiguration.lock(params.configurationInstanceId)
        bindData(configurationInstance, params, [exclude: ["owner", "tags","productGroupSelection","eventGroupSelection"]])
        try {
            //Set the workflow group from the logged in user.
            configurationInstance.workflowGroup = userService.getUser().workflowGroup
            if(!configurationInstance.priority){
                configurationInstance.priority = Priority.findByDefaultPriority(true)?:null
            }
            if(params.productGroupSelection != '[]'){
                configurationInstance.productGroupSelection = params.productGroupSelection
            } else {
                configurationInstance.productGroupSelection=null
            }
            if(params.eventGroupSelection!="[]"){
                configurationInstance.eventGroupSelection=params.eventGroupSelection
            } else {
                configurationInstance.eventGroupSelection=null
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
            configurationInstance.isResume = false

            configurationInstance.isEnabled = params.isEnabled ?: false
            setNextRunDateAndScheduleDateJSON(configurationInstance)
            configurationInstance.dateRangeInformation = setDateRange(configurationInstance)
            configurationInstance = userService.assignGroupOrAssignTo(params[Constants.ASSIGN_TO_PARAM], configurationInstance)
            userService.bindSharedWithConfiguration(configurationInstance, params.sharedWith, true)
            if(params.selectedDatasheet && params.selectedDatasheet == 'on'){
                configurationInstance.datasheetType = params.allSheets?:Constants.DatasheetOptions.CORE_SHEET
                configurationInstance.isDatasheetChecked = true
            }else{
                configurationInstance.isDatasheetChecked = false
            }
            evdasAlertService.bindDatasheetData(configurationInstance, params.dataSheet ? [params.dataSheet]?.flatten() : null)
            configurationInstance = (EvdasConfiguration) CRUDService.updateWithAuditLog(configurationInstance)
            def exConfig = ExecutedEvdasConfiguration.findByConfigId(configurationInstance?.id)
            if (params.name && !exConfig?.name.equals(params.name)) {
                alertService.renameExecConfig(configurationInstance.id, configurationInstance.name, params.name, configurationInstance.owner.id, Constants.AlertConfigType.EVDAS_ALERT)
            }
        } catch (ValidationException ve) {
            configurationInstance.errors = ve.errors
            renderOnErrorScenario(configurationInstance)
        }catch (Exception exception) {
            flash.error = message(code: "app.label.alert.error")
            renderOnErrorScenario(configurationInstance)
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'app.label.evdas.alert.configuration'), configurationInstance.name])
        redirect(controller: "evdasAlert", action: "view", id: configurationInstance.id)
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION'])
    def run() {
        params?.name = params?.name?.trim()?.replaceAll("\\s{2,}", " ")
        EvdasConfiguration configurationInstance
        def existingConfig = false
        if (params.id && params.action != Constants.AlertActions.COPY) {
            configurationInstance = EvdasConfiguration.get(params.id)
            existingConfig = true
        } else {
            configurationInstance = new EvdasConfiguration()
        }
        bindData(configurationInstance, params, [exclude: ["owner", "id", "tags","sharedWith","productGroupSelection","eventGroupSelection"]])
        try {
            if(params.productGroupSelection != '[]'){
                configurationInstance.productGroupSelection = params.productGroupSelection
            } else {
                configurationInstance.productGroupSelection=null
            }
            if(params.eventGroupSelection!="[]"){
                configurationInstance.eventGroupSelection=params.eventGroupSelection
            } else {
                configurationInstance.eventGroupSelection=null
            }
            configurationInstance.owner = configurationInstance?.owner ?: userService.getUser()
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
            //Set the workflow group from the logged in user.
            configurationInstance.workflowGroup = userService.getUser().workflowGroup
            configurationInstance.isResume = false
            configurationInstance.isEnabled = true
            setNextRunDateAndScheduleDateJSON(configurationInstance)
            configurationInstance.dateRangeInformation = setDateRange(configurationInstance)
            configurationInstance = userService.assignGroupOrAssignTo(params[Constants.ASSIGN_TO_PARAM], configurationInstance)
            userService.bindSharedWithConfiguration(configurationInstance, params.sharedWith,  existingConfig)
            if(params.selectedDatasheet && params.selectedDatasheet == 'on'){
                configurationInstance.datasheetType = params.allSheets?:Constants.DatasheetOptions.CORE_SHEET
                configurationInstance.isDatasheetChecked = true
            }else{
                configurationInstance.isDatasheetChecked = false
            }
            evdasAlertService.bindDatasheetData(configurationInstance, params.dataSheet ? [params.dataSheet]?.flatten() : null)
            if(configurationInstance.adhocRun){
                configurationInstance.nextRunDate = new Date()
            }
            if(existingConfig){
                configurationInstance = (EvdasConfiguration) CRUDService.updateWithAuditLog(configurationInstance)
                def exConfig = ExecutedEvdasConfiguration.findByConfigId(configurationInstance?.id)
                if (params.name && !exConfig?.name.equals(params.name)) {
                    alertService.renameExecConfig(configurationInstance.id, configurationInstance.name, params.name, configurationInstance.owner.id, Constants.AlertConfigType.EVDAS_ALERT)
                }
            } else {
                configurationInstance = (EvdasConfiguration) CRUDService.saveWithAuditLog(configurationInstance)
            }
            //Related to linked alert configurations. If params are passed then it will be attached to the signal.
            if (!TextUtils.isEmpty(params.signalId)) {
                validatedSignalService.addEvdasConfigurationToSignal(params.signalId, configurationInstance)
            }
        } catch (ValidationException ve) {
            ve.printStackTrace()
            if (Boolean.parseBoolean(params.repeatExecution)) {
                configurationInstance.setIsEnabled(true)
            } else {
                configurationInstance.setIsEnabled(false)
            }
            configurationInstance.errors = ve.errors
            renderOnErrorScenario(configurationInstance)
            return
        }catch (Exception exception) {
            exception.printStackTrace()
            flash.error = message(code: "app.label.alert.error")
            renderOnErrorScenario(configurationInstance)
            return
        }
        request.withFormat {
            form {
                flash.message = message(code: 'app.Configuration.RunningMessage',
                        args: [message(code: 'configuration.label'), configurationInstance.name])
                redirect(controller: "configuration", action: "executionStatus",params: [alertType:AlertType.EVDAS_ALERT])
            }
            '*' { respond configurationInstance, [status: CREATED] }
        }
    }

    private void renderOnErrorScenario(EvdasConfiguration configuration) {
        String action = params.action
        String startDateAbsoluteCustomFreq = params?.dateRangeStartAbsolute
        String endDateAbsoluteCustomFreq = params?.dateRangeEndAbsolute
        Map model = modelData(configuration, action)
        model.signalId = params.signalId ?: null
        model << [startDateAbsoluteCustomFreq: startDateAbsoluteCustomFreq, endDateAbsoluteCustomFreq: endDateAbsoluteCustomFreq]
        if (params.id && params.action != Constants.AlertActions.COPY) {
            render view: "edit", model: model
        } else {
            render view: "create", model: model
        }
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION'])
    def view(EvdasConfiguration configuration) {

        if (!configuration) {
            notFound()
            return
        }

        User currentUser = userService.getUser()
        boolean isConfigExecuted = false
        def executedConfiuguration = null
        def exConfig = null
        try {
            executedConfiuguration = ExecutedEvdasConfiguration.findAllByName(configuration.name)?.sort {
                -it.id
            }
            if (executedConfiuguration && executedConfiuguration.size() > 0) {
                isConfigExecuted = true
                exConfig = executedConfiuguration.first()
            }
        } catch (Throwable th) {
            flash.warn = message(code: "app.alert.view")
            log.error(th.getMessage())
        }
        List activeDataSheetsTexts = []
        String enabledSheet = Constants.DatasheetOptions.CORE_SHEET
        if(!TextUtils.isEmpty(configuration?.selectedDataSheet)){
            Map activeDataSheets = alertService.getOnlyActiveDataSheets(configuration, true)
            activeDataSheetsTexts = dataSheetService.formatActiveDatasheetMap(activeDataSheets).text
        }
        if(!activeDataSheetsTexts.size()) {
            Boolean isProductGroup = !TextUtils.isEmpty(configuration.productGroupSelection)
            String productSelection = configuration.productGroupSelection ?: configuration.productSelection
            activeDataSheetsTexts = dataSheetService.fetchDataSheets(productSelection, enabledSheet, isProductGroup, false)?.dispName
        }

        render(view: "view", model: [configurationInstance: configuration, currentUser: currentUser,
                                     assignedToName       : userService.getAssignedToName(configuration),
                                     isConfigExecuted     : isConfigExecuted, isExecuted: false,
                                     viewSql              : params.getBoolean("viewSql") ? evdasAlertExecutionService.debugReportSQL(configuration, exConfig) : null,
                                     isEdit               : SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN") || userService.getCurrentUserId() == configuration.owner.id,
                                     selectedDatasheets    : activeDataSheetsTexts?.join(',')?:''])
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def viewExecutedConfig(ExecutedEvdasConfiguration executedConfiguration) {
        if (!executedConfiguration) {
            notFound()
            return
        }
        User currentUser = userService.getUser()
        List selectedDatasheets = []
        String enabledSheet = Constants.DatasheetOptions.CORE_SHEET
        if(!TextUtils.isEmpty(executedConfiguration?.selectedDataSheet)){
            selectedDatasheets = dataSheetService.formatDatasheetMap(executedConfiguration)?.text
        }else {
            Boolean isProductGroup = !TextUtils.isEmpty(executedConfiguration.productGroupSelection)
            selectedDatasheets =  dataSheetService.fetchDataSheets(executedConfiguration.productGroupSelection?:executedConfiguration.productSelection,enabledSheet, isProductGroup, false)?.dispName
        }
        render(view: "view", model: [configurationInstance: executedConfiguration, currentUser: currentUser, isExecuted: true, selectedDatasheets:selectedDatasheets?.join(',') ])
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION'])
    def edit(EvdasConfiguration configurationInstance) {

        if (!configurationInstance) {
            notFound()
            return
        }

        def startDateAbsoluteCustomFreq = ""
        def endDateAbsoluteCustomFreq = ""
        String firstExecutionDate = '-'
        String lastExecutionDate = '-'
        def nextRunDate = configurationInstance.nextRunDate
        User currentUser = userService.getUser()
        String timeZone = currentUser.preference.timeZone
        def executedConfig = ExecutedEvdasConfiguration.findAllByName(configurationInstance.name).sort {it.id}

        try {
            if (executedConfig) {
                def length = executedConfig.size()
                def firstExecutionObject = executedConfig[0]
                def lastExecutionObject = executedConfig[length - 1]
                def fExecutionStartDate = firstExecutionObject.dateRangeInformation.dateRangeStartAbsolute
                def fExecutionEndDate = firstExecutionObject.dateRangeInformation.dateRangeEndAbsolute
                def lExecutionStartDate = lastExecutionObject.dateRangeInformation.dateRangeStartAbsolute
                def lExecutionEndDate = lastExecutionObject.dateRangeInformation.dateRangeEndAbsolute
                String firstExecutionStartDate = fExecutionStartDate?DateUtil.toDateStringWithoutTimezone(fExecutionStartDate):""
                String firstExecutionEndDate = fExecutionEndDate?DateUtil.toDateStringWithoutTimezone(fExecutionEndDate):""
                String lastExecutionStartDate = lExecutionStartDate?DateUtil.toDateStringWithoutTimezone(lExecutionStartDate):""
                String lastExecutionEndDate = lExecutionEndDate?DateUtil.toDateStringWithoutTimezone(lExecutionEndDate):""
                firstExecutionDate = firstExecutionStartDate + "-" + firstExecutionEndDate
                lastExecutionDate = lastExecutionStartDate + "-" + lastExecutionEndDate
            }
        } catch (Throwable th) {
            flash.warn = message(code: "app.alert.view")
            log.error(th.getMessage())
        }

        if (evdasAlertExecutionService.currentlyRunning.contains(configurationInstance.id)) {
            flash.warn = message(code: "app.configuration.running.fail", args: [configurationInstance.name])
            redirect(action: "index")
        }
        if (!(configurationInstance?.isEditableBy(currentUser) ||
                SpringSecurityUtils.ifAllGranted("ROLE_EVDAS_CASE_CONFIGURATION, ROLE_EXECUTE_SHARED_ALERTS"))) {
            flash.warn = message(code: "app.configuration.edit.permission", args: [configurationInstance.name])
            redirect(controller: "configuration", action: "index")
        } else {
            String action = Constants.AlertActions.EDIT
            if (configurationInstance?.dateRangeInformation.dateRangeEnum == DateRangeEnum.CUSTOM) {
                startDateAbsoluteCustomFreq = DateUtil.toDateString1(configurationInstance?.dateRangeInformation?.dateRangeStartAbsolute)
                endDateAbsoluteCustomFreq = DateUtil.toDateString1(configurationInstance?.dateRangeInformation?.dateRangeEndAbsolute)
            }
            Map activeDataSheets = alertService.getOnlyActiveDataSheets(configurationInstance,true)
            Map model = modelData(configurationInstance, action)
            model << [startDateAbsoluteCustomFreq: startDateAbsoluteCustomFreq,
                      endDateAbsoluteCustomFreq  : endDateAbsoluteCustomFreq,
                      configSelectedTimeZone     : params?.configSelectedTimeZone,
                      nextRunDate                : nextRunDate,
                      firstExecutionDate         : firstExecutionDate, lastExecutionDate: lastExecutionDate,
                      dataSheetList : activeDataSheets?dataSheetService.formatActiveDatasheetMap(activeDataSheets) as JSON:'']
            render(view: "edit", model: model)
        }
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION'])
    def copy(EvdasConfiguration originalConfig) {
        def startDateAbsoluteCustomFreq = ""
        def endDateAbsoluteCustomFreq = ""
        if (!originalConfig) {
            notFound()
            return
        }
        if (originalConfig.dateRangeInformation?.dateRangeEnum == DateRangeEnum.CUSTOM) {
            startDateAbsoluteCustomFreq = DateUtil.toDateString1(originalConfig?.dateRangeInformation?.dateRangeStartAbsolute)
            endDateAbsoluteCustomFreq = DateUtil.toDateString1(originalConfig?.dateRangeInformation?.dateRangeEndAbsolute)
        }
        List dataSheetList = dataSheetService.formatDatasheetMap(originalConfig)
        Map model = modelData(originalConfig, Constants.AlertActions.COPY)
        model << [clone:true, currentUser: userService.getUser()]
        model << [startDateAbsoluteCustomFreq: startDateAbsoluteCustomFreq, endDateAbsoluteCustomFreq: endDateAbsoluteCustomFreq, dataSheetList : dataSheetList?dataSheetList as JSON:'']
        render(view: "create", model: model)
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EXECUTE_SHARED_ALERTS'])
    def runOnce() {
        params?.name = params?.name?.trim()?.replaceAll("\\s{2,}", " ")
        EvdasConfiguration configurationInstance = EvdasConfiguration.get(params.id)
        if (!configurationInstance) {
            notFound()
            return
        }
        if (configurationInstance.nextRunDate != null && configurationInstance.isEnabled == true) {
            flash.warn = message(code: 'app.configuration.run.exists')
            redirect(controller: 'configuration', action: "index")

        } else {
            try {
                configurationInstance.setIsEnabled(true)
                try {
                    configurationInstance.setScheduleDateJSON(getRunOnceScheduledDateJson())
                    configurationInstance.setNextRunDate(configurationService.getNextDate(configurationInstance))
                    configurationInstance = (EvdasConfiguration) CRUDService.updateWithAuditLog(configurationInstance)
                }catch(Exception e){
                    configurationInstance.scheduleDateJSON = null
                }
            } catch (ValidationException ve) {
                configurationInstance.errors = ve.errors
                render view: "create", model: [configurationInstance: configurationInstance]
                return
            } catch (Exception exception) {
                flash.error = message(code: "app.label.alert.error")
                renderOnErrorScenario(configurationInstance)
            }
            flash.message = message(code: 'app.Configuration.RunningMessage',
                    args: [message(code: 'configuration.label'), configurationInstance.name])
            redirect(controller: "configuration", action: "executionStatus",params: [alertType: AlertType.EVDAS_ALERT])
        }
    }

    def updateAndExecuteEvdasAlert(){
        EvdasConfiguration configurationInstance = EvdasConfiguration.get(params.configId)
        if (!configurationInstance) {
            notFound()
            return
        }
        List runningAlerts = configurationService.fetchRunningAlertList(Constants.ConfigurationType.EVDAS_TYPE)
        if (runningAlerts?.contains(configurationInstance.id)) {
            // found alert running on given configID, return
            flash.error = message(code: 'app.configuration.alert.running', args: [configurationInstance.name])
            redirect(controller: "alertAdministration", action: "index")
            return
        }

        configurationInstance.setIsEnabled(true)
        configurationInstance.setIsResume(false)
        configurationInstance.setNextRunDate(null)
        if (configurationInstance.isAutoPaused) {
            configurationInstance.isAutoPaused = false
            configurationInstance.autoPausedEmailTriggered = null
            configurationInstance.alertDisableReason = null
        }

        if (params.alertDateRangeInformation) {
            configurationInstance.dateRangeInformation = setDateRange(configurationInstance)
        }
        if (params.runNow != null) {
            // if run now is selected then newxt run date is now
            configurationInstance.setScheduleDateJSON(getRunOnceScheduledDateJson())
            configurationInstance.setNextRunDate(configurationService.getNextDate(configurationInstance))
            if (params.futureSchedule != null && params.scheduleDateJSON) {
                configurationInstance.futureScheduleDateJSON = params.scheduleDateJSON
            }
        } else if (params.futureSchedule != null && params.scheduleDateJSON) {
            configurationInstance.setScheduleDateJSON(params.scheduleDateJSON)
            configurationInstance.setNextRunDate(configurationService.getNextDate(configurationInstance))
        }

        try {
            if(params.futureSchedule != null && params.scheduleDateJSON){
                Map scheduleDateJSONMap = JSON.parse(params.scheduleDateJSON) as Map
                Date startDate = Date.parse(JSON_DATE, scheduleDateJSONMap.startDateTime as String)
                if(startDate < (DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH))){
                    flash.error = message(code: "app.label.past.date.error", args: ["Start Date"])
                    redirect(controller: "alertAdministration", action: "index")
                    return
                }
            }
            configurationInstance = (EvdasConfiguration) CRUDService.updateWithAuditLog(configurationInstance)
        } catch (ValidationException ve) {
            render ve.errors
            log.error(ve.printStackTrace())
            return
        }

        redirect(controller: "configuration", action: "executionStatus",params: [alertType: AlertType.EVDAS_ALERT])
    }

    def updateAndExecuteEvdasAlertBulk(){
        List configIdList = params.get("configIdList")?.split(",")
        List<EvdasConfiguration> currentlyRunningConfigs = []
        configIdList.each{ configId ->
            List runningAlerts = configurationService.fetchRunningAlertList(Constants.ConfigurationType.EVDAS_TYPE)
            if (runningAlerts?.contains(configId)) {
                currentlyRunningConfigs.add(EvdasConfiguration.get(configId as Long))
            }
        }
        configIdList.removeAll(currentlyRunningConfigs)
        configIdList.each {
            params.configId = it
            EvdasConfiguration configurationInstance = EvdasConfiguration.get(params.configId)
            if (!configurationInstance) {
                notFound()
                return
            }

            configurationInstance.setIsEnabled(true)
            configurationInstance.setIsResume(false)
            configurationInstance.setNextRunDate(null)
            if (configurationInstance.isAutoPaused) {
                configurationInstance.isAutoPaused = false
                configurationInstance.autoPausedEmailTriggered = null
                configurationInstance.alertDisableReason = null
            }

            if (params.alertDateRangeInformation) {
                configurationInstance.dateRangeInformation = setDateRange(configurationInstance)
            }
            if (params.runNow != null) {
                // if run now is selected then next run date is now
                configurationInstance.setScheduleDateJSON(getRunOnceScheduledDateJson())
                configurationInstance.setNextRunDate(configurationService.getNextDate(configurationInstance))
                if (params.futureSchedule != null && params.scheduleDateJSON) {
                    configurationInstance.futureScheduleDateJSON = params.scheduleDateJSON
                }
            } else if (params.futureSchedule != null && params.scheduleDateJSON) {
                configurationInstance.setScheduleDateJSON(params.scheduleDateJSON)
                configurationInstance.setNextRunDate(configurationService.getNextDate(configurationInstance))
            }

            try {
                if(params.futureSchedule != null && params.scheduleDateJSON){
                    Map scheduleDateJSONMap = JSON.parse(params.scheduleDateJSON) as Map
                    Date startDate = Date.parse(JSON_DATE, scheduleDateJSONMap.startDateTime as String)
                    if(startDate < (DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH))){
                        flash.error = message(code: "app.label.past.date.error", args: ["Start Date"])
                        redirect(controller: "alertAdministration", action: "index")
                        return
                    }
                }
                configurationInstance = (EvdasConfiguration) CRUDService.updateWithAuditLog(configurationInstance)
            } catch (ValidationException ve) {
                render ve.errors
                log.error(ve.printStackTrace())
                return
            }
        }
        if(currentlyRunningConfigs.size()>0){
            flash.warn = message(code: 'app.configuration.alert.running', args: [currentlyRunningConfigs.join(",")])
            redirect(controller: "alertAdministration", action: "index")
        } else{
            redirect(controller: "configuration", action: "executionStatus",params: [alertType: AlertType.EVDAS_ALERT])
        }
    }

    private getRunOnceScheduledDateJson() {
        def startupTime = (new Date()).format(ConfigurationService.JSON_DATE)
        def timeZone = DateUtil.getTimezoneForRunOnce(userService.getUser())
        return """{"startDateTime":"${
            startupTime
        }","timeZone":{"${timeZone}"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}"""

    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION'])
    def setNextRunDateAndScheduleDateJSON(configurationInstance) {
        try{
        if (configurationInstance.scheduleDateJSON && configurationInstance.isEnabled) {
            configurationInstance.nextRunDate = configurationService.getNextDate(configurationInstance)
        } else {
            configurationInstance.nextRunDate = null
        }
        }catch(Exception e){
            configurationInstance.scheduleDateJSON = null
        }
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION'])
    def setDateRange(configurationInstance) {
        def dateRangeEnum = params.dateRangeEnumEvdas ?: params.dateRangeEnum
        if (dateRangeEnum) {
            EVDASDateRangeInformation dateRangeInformation
            if (configurationInstance.dateRangeInformation?.id) {
                dateRangeInformation = EVDASDateRangeInformation.get(configurationInstance.dateRangeInformation.id)
            } else {
                dateRangeInformation = new EVDASDateRangeInformation()
            }
            dateRangeInformation.evdasConfiguration = configurationInstance
            dateRangeInformation.dateRangeEnum = dateRangeEnum
            def startDateAbsolute = params.dateRangeStartAbsolute
            def endDateAbsolute = params.dateRangeEndAbsolute
            if (dateRangeEnum == DateRangeEnum.CUSTOM.name() && startDateAbsolute != "null" && endDateAbsolute != "null") {
                def timezone = Holders.config.server.timezone
                dateRangeInformation.dateRangeStartAbsolute = DateUtil.stringToDate(startDateAbsolute, 'dd-MMM-yyyy', timezone)
                dateRangeInformation.dateRangeEndAbsolute = DateUtil.stringToDate(endDateAbsolute, 'dd-MMM-yyyy', timezone)
            } else if(dateRangeEnum == DateRangeEnum.CUMULATIVE.name()){
                dateRangeInformation.dateRangeStartAbsolute =new Date(dateRangeInformation.MIN_DATE)
                dateRangeInformation.dateRangeEndAbsolute =new Date()
            }
            return dateRangeInformation
        }
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION'])
    def delete(EvdasConfiguration config) {
        if (!config) {
            notFound()
            return
        }
        User currentUser = userService.getUser()
        if (config.isEditableBy(currentUser) ||
                SpringSecurityUtils.ifAllGranted("ROLE_EVDAS_CASE_CONFIGURATION, ROLE_EXECUTE_SHARED_ALERTS")) {
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

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'configuration.label'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def review() {
        Boolean showCumulative = userService.getUser()?.preference?.isCumulativeAlertEnabled
        def evdasReviewHelpMap =Holders.config.evdas.review.helpMap
        if (showCumulative) {
            params.callingScreen = Constants.Commons.TRIGGERED_ALERTS
            details()
        } else {
            render (view: "review",model:[evdasReviewHelpMap:evdasReviewHelpMap])
        }
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def listConfig() {

        Map resultMap = [aaData: [], recordsFiltered: 0, recordsTotal: 0]

        try {
            HttpSession session = request.getSession()
            session.setAttribute("evdas",params["selectedAlertsFilter"])
            def orderColumn = params["order[0][column]"]
            Map orderColumnMap = [name: params["columns[${orderColumn}][data]"], dir: params["order[0][dir]"]]

            def configList = evdasAlertService.getListOfExecutedEvdasAlerts(params.int("length"), params.int("start"), orderColumnMap, params["search[value]"], params["selectedAlertsFilter"])
            List list = []

            User user = userService.getUser()
            Disposition defaultDisposition = user?.getWorkflowGroup()?.defaultEvdasDisposition
            String timeZone = userService.getCurrentUserPreference()?.timeZone

            if (configList?.resultList?.size() > 0) {
                List<Disposition> reviewCompletedDispositionList = Disposition.findAllByReviewCompleted(true)
                List<Map> totalCountList = EvdasAlert.executeQuery(evdasAlertService.prepareHQLForTotalCount(), [execConfigList: configList.resultList*.id])
                List<Map> closedCountList = EvdasAlert.executeQuery(evdasAlertService.prepareHQLForClosedCount(), [execConfigList: configList.resultList*.id, dispositionList: reviewCompletedDispositionList*.id])
                List<Map> newCountList = EvdasAlert.executeQuery(evdasAlertService.prepareHQLForNewCount(), [execConfigList: configList.resultList*.id])

                configList?.resultList?.each { ExecutedEvdasConfiguration c ->

                    try {
                        List<Date> dateRange = c.dateRangeInformation?.getReportStartAndEndDate() ?: []
                        if (c.dateRangeInformation?.dateRangeEnum == DateRangeEnum.CUMULATIVE) {
                                dateRange.add(null)
                                dateRange.add(c?.dateCreated)
                        }
                        def va = [
                                    id              : c.id,
                                    name            : c.name,
                                    version         : c.numOfExecutions,
                                    description     : c.description,
                                    frequency       : getRecurrencePattern(c.scheduleDateJSON),
                                    productSelection: (getNameFieldFromJson(c.productSelection))?:getGroupNameFieldFromJson(c.productGroupSelection),
                                    priority        : c.priority.displayName,
                                    dateRagne       : (dateRange ? DateUtil.toDateString1(dateRange[0]) : "-") + " to " + (dateRange ? DateUtil.toDateString1(dateRange[1]) : "-"),
                                    caseCount       : totalCountList.find { it.id == c.id }?.cnt ?: 0,
                                    closedCaseCount : closedCountList.find { it.id == c.id }?.cnt ?: 0,
                                    newCases        : newCountList.find { it.id == c.id }?.cnt ?: 0,
                                    lastExecuted    : DateUtil.stringFromDate(c.dateCreated, DateUtil.DATEPICKER_FORMAT, timeZone),
                                    lastModified    : DateUtil.stringFromDate(c.lastUpdated, DateUtil.DATEPICKER_FORMAT, timeZone),
                                    IsShareWithAccess : userService.hasAccessShareWith()
                        ]
                        list.add(va)

                    } catch(Throwable th) {
                        log.error(" Some error occured with executedConfiguration ---> "+c.name )
                        th.printStackTrace()
                    }
                }
            }
            resultMap = [aaData: list as Set, recordsFiltered: configList.filteredCount, recordsTotal: configList.totalCount]
        } catch(Throwable th) {
            th.printStackTrace()
        }
        render(resultMap as JSON)
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def listAdhocConfig() {
        Group workflowGroup = userService.getUser()?.getWorkflowGroup()
        List<ExecutedEvdasConfiguration> configList = ExecutedEvdasConfiguration.createCriteria().list {
            eq("adhocRun", true)
            eq("workflowGroup", workflowGroup)
            order("lastUpdated", "desc")
        }

        List list = []
        configList.each { ExecutedEvdasConfiguration c ->
            EvdasConfiguration configuration = EvdasConfiguration.findByNameAndAdhocRun(c.name, true)
            List dateRange = configuration.dateRangeInformation.getReportStartAndEndDate()
            Map va = [
                    id              : c.id,
                    name            : c.name,
                    version         : c.numOfExecutions,
                    description     : c.description,
                    productSelection: getNameFieldFromJson(c.productSelection),
                    dateRagne       : (dateRange ? DateUtil.toDateString1(dateRange[0]) : "-") + " to " + (dateRange ? DateUtil.toDateString1(c?.dateCreated) : "-"),
                    caseCount       : (EvdasAlert.where {
                        (disposition.closed == false && executedAlertConfiguration == c)
                    }).size(),
                    closedCaseCount : (EvdasAlert.where {
                        (disposition.closed == true && executedAlertConfiguration == c)
                    }).size(),
                    newCases        : EvdasAlert.countByExecutedAlertConfigurationAndDisposition(c, workflowGroup?.defaultEvdasDisposition)
            ]
            list.add(va)
        }
        Set executedSet = list as Set
        render(executedSet as JSON)
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def getRecurrencePattern(jsonString) {
        def prdName = ""
        def jsonObj = null
        if (jsonString) {
            jsonObj = parseJsonString(jsonString)
            if (!jsonObj)
                prdName = jsonString
            else {
                def prdVal = jsonObj.find { k, v ->
                    if (k == "recurrencePattern") {
                        prdName = (v.split(";")[0]).split("=")[1]
                    }
                }
            }
        }
        prdName
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER'])
    def changePriorityOfAlert(String selectedRows, Priority newPriority, String justification, Boolean isArchived) {

        def domain = evdasAlertService.getDomainObject(isArchived)
        Date lastUpdated = null
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true, data: [])
        try {
            User loggedInUser = userService.getUser()
            List<UndoableDisposition> undoableDispositionIdList = []
            List<Map> evdasHistoryMapList = []
            JSON.parse(selectedRows).each { Map<String, Long> selectedRow ->
                def alert = domain.get(selectedRow["alert.id"])

                Priority orgPriority = alert.priority

                if (alert) {

                    Map evdasHistoryMap = evdasAlertService.createEvdasHistoryMap(alert, Constants.HistoryType.PRIORITY, justification, isArchived)
                    evdasHistoryMap.priority = newPriority
                    //Update the states for all the evdas alerts where this substance(productName) and event is same.
                    evdasAlertService.updateEvdasAlertStates(alert, evdasHistoryMap)
                    lastUpdated = alert.lastUpdated
                    evdasHistoryMap.put("createdTimestamp", lastUpdated)
                    evdasHistoryMapList << evdasHistoryMap

                    responseDTO.data << [id: alert.id, dueIn: alert.dueIn()]
                    def activityType = ActivityType.findByValue(ActivityTypeValue.PriorityChange)

                    // updating the due date of undoable disposition object with latest due date
                    alertService.updateUndoDispDueDate(Constants.AlertType.EVDAS, alert.id as Long, undoableDispositionIdList, alert.dueDate)

                    activityService.createActivityForEvdas(alert.executedAlertConfiguration, activityType,
                            userService.getUser(), "Priority changed from '$orgPriority.displayName' to '$newPriority.displayName'".toString(), justification,
                            ['For EVDAS Alert'], alert.substance, alert.pt, alert.assignedTo, null, alert.assignedToGroup, null, lastUpdated)
                }
            }
            evdasHistoryService.batchPersistHistory(evdasHistoryMapList)
            if (undoableDispositionIdList) {
                aggregateCaseAlertService.notifyUndoableDisposition(undoableDispositionIdList)
            }
        } catch (grails.validation.ValidationException vx) {
            responseDTO.message = MiscUtil.getCustomErrorMessage(vx)
            responseDTO.status = false
            log.error("Exception is : ${vx}")
        } catch (Exception e) {
            e.printStackTrace()
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.priority.change.error")
        }
        render(responseDTO as JSON)
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION'])
    public disable() {

        EvdasConfiguration configurationInstance = EvdasConfiguration.get(params.id)
        if (!configurationInstance) {
            notFound()
            return
        }
        params.scheduleDateJSON = importConfigurationService.getDefaultScheduleJSON() //Done to make this in sync with import config behaviour PVS-61035
        configurationService.pushCustomAuditForUnscheduling(configurationInstance,"evdasConfiguration")
        bindData(configurationInstance, params, [exclude: ["owner"]])
        try {
            configurationInstance.owner = userService.getUser()
            configurationInstance.isEnabled = false
            configurationInstance.nextRunDate = null
            def dateRangeInformation = setDateRange(configurationInstance)
            configurationInstance.dateRangeInformation = dateRangeInformation
            configurationInstance = (EvdasConfiguration) CRUDService.updateWithAuditLog(configurationInstance)
            def exConfig = ExecutedEvdasConfiguration.findByConfigId(configurationInstance?.id)
            if (params.name && !exConfig?.name.equals(params.name)) {
                alertService.renameExecConfig(configurationInstance.id, configurationInstance.name, params.name, configurationInstance.owner.id, Constants.AlertConfigType.EVDAS_ALERT)
            }
        } catch (ValidationException ve) {
            configurationInstance.errors = ve.errors
            renderOnErrorScenario(configurationInstance)
        } catch (Exception e) {
            renderOnErrorScenario(configurationInstance)
        }

        flash.message = message(code: 'default.disabled.message', args: [message(code: 'configuration.label'), configurationInstance.name])
        redirect(controller: "evdasAlert", action: "view", id: configurationInstance.id)

    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER'])
    def changeAssignedToGroup(String selectedId, String assignedToValue, Boolean isArchived) {

        def domain = evdasAlertService.getDomainObject(isArchived)
        Date lastUpdated = null
        ResponseDTO responseDTO = new ResponseDTO(status: true, message: message(code: 'app.assignedTo.changed.success'))
        List<Long> selectedRowsList = JSON.parse(selectedId).collect {
            it as Long
        }
        boolean bulkUpdate = selectedRowsList.size() > 1
        try {
            selectedRowsList.each { Long id ->
                def evdasAlert = domain.get(id)
                List peHistoryMapList = []
                String oldUserName = userService.getAssignedToName(evdasAlert)
                List<User> oldUserList = userService.getUserListFromAssignToGroup(evdasAlert)
                evdasAlert = userService.assignGroupOrAssignTo(assignedToValue, evdasAlert)
                String newUserName = userService.getAssignedToName(evdasAlert)
                List<User> newUserList = userService.getUserListFromAssignToGroup(evdasAlert)
                String eventName = evdasAlert.pt
                String productName = evdasAlert.substance

                if (evdasAlert) {
                    User currUser = userService.getUser()
                    if (newUserName && (oldUserName != newUserName)) {

                        //Update the alert instance.
                        if (evdasAlert.executedAlertConfiguration.id) {
                            //Create the Evdas history.
                            Map evdasHistoryMap = evdasAlertService.createEvdasHistoryMap(evdasAlert, Constants.HistoryType.ASSIGNED_TO,
                                    Constants.Commons.BLANK_STRING, isArchived)
                            peHistoryMapList.add(evdasHistoryMap)
                            //Update the states for all the evdas alerts where this substance(productName) and event is same.
                            evdasAlertService.updateEvdasAlertStates(evdasAlert, evdasHistoryMap)
                            lastUpdated = evdasAlert.lastUpdated
                            evdasHistoryMap.put("createdTimestamp", lastUpdated)
                            if(emailNotificationService.emailNotificationWithBulkUpdate(bulkUpdate, Constants.EmailNotificationModuleKeys.ASSIGNEE_UPDATE)) {
                                String newEmailMessage = message(code: 'app.email.case.assignment.evdas.message.newUser')
                                String oldEmailMessage = message(code: 'app.email.case.assignment.evdas.message.oldUser')
                                List emailDataList = userService.generateEmailDataForAssignedToChange(newEmailMessage, newUserList, oldEmailMessage, oldUserList)
                                String emailTitle = message(code: "app.email.case.assignment.evdas.message", args: [evdasAlert.substance])
                                evdasAlertService.sendMailForAssignedToChange(emailDataList, evdasAlert, emailTitle, isArchived)
                            }
                            ActivityType activityType = ActivityType.findByValue(ActivityTypeValue.AssignedToChange)
                            activityService.createActivityForEvdas(evdasAlert.executedAlertConfiguration, activityType, currUser,
                                    "Assigned To changed from '${oldUserName}' to '${newUserName}'", null,
                                    ['For EVDAS Alert'], productName, eventName, evdasAlert.assignedTo, null, evdasAlert.assignedToGroup, null, lastUpdated)
                        }

                    }
                }
                evdasHistoryService.batchPersistHistory(peHistoryMapList)
            }
        }catch (grails.validation.ValidationException vx) {
            responseDTO.message = MiscUtil.getCustomErrorMessage(vx)
            responseDTO.status = false
            log.error("Exception is : ${vx}")
        } catch (Throwable e) {
            log.error(e.getMessage(), e)
            responseDTO.status = false
            responseDTO.message = message(code: 'app.assignedTo.changed.fail')
        }
        render(responseDTO as JSON)
    }

    private sendAssignedToEmail(User user, EvdasAlert alert, String alertLink, messageToUser, currentAssignedUser) {
        String emailTitle = message(code: "app.email.case.assignment.evdas.message", args: [alert.substance])
        evdasAlertService.sendAssignedToEmail(user, alert, alertLink, messageToUser, currentAssignedUser, emailTitle)
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def details() {

        User currentUser = userService.getUser()
        Boolean alertDeletionObject = false
        def domain = params.boolean('isArchived') ? ArchivedEvdasAlert : EvdasAlert
        Integer prevColCount = Holders.config.signal.evdas.number.previous.columns
        Boolean isTempViewSelected = false
        if (params.containsKey("tempViewId")) {
            isTempViewSelected = true
        }
        Long id = params.long("configId")
        if(params.callingScreen != Constants.Commons.DASHBOARD && id == -1){
            alertDeletionInProgress()
            return
        }
        ExecutedEvdasConfiguration exEvdasConfiguration = ExecutedEvdasConfiguration.get(id)
        if(exEvdasConfiguration) {
            alertDeletionObject = alertService.isDeleteInProgress(exEvdasConfiguration.configId as Long, Constants.AlertConfigType.EVDAS_ALERT) ?: false
        }
        Long configId = 0L
        if(exEvdasConfiguration || params.callingScreen == Constants.Commons.DASHBOARD) {
            configId = exEvdasConfiguration?evdasAlertService.getAlertConfigObject(exEvdasConfiguration):0L
        }else{
            notFound()
            return
        }
        if(params.callingScreen != Constants.Commons.DASHBOARD && alertDeletionObject){
            alertDeletionInProgress()
            return
        }
        Boolean cumulative = userService.getCurrentUserPreference()?.isCumulativeAlertEnabled ?: false
        String name = Constants.Commons.BLANK_STRING
        String dr = Constants.Commons.BLANK_STRING
        String startDate = Constants.Commons.BLANK_STRING
        String endDate = Constants.Commons.BLANK_STRING
        List listDateRange = []
        List freqNames = []
        def prevColMap = Holders.config.signal.evdas.data.previous.columns.clone()
        def prevColumns = groovy.json.JsonOutput.toJson(prevColMap)
        String backUrl = request.getHeader('referer')
        List actionConfigList = validatedSignalService.getActionConfigurationList(Constants.AlertConfigType.EVDAS_ALERT)
        List alertDispositionList = dispositionService.listAlertDispositions()
        String alertType = Constants.AlertConfigType.EVDAS_ALERT

        if (params.callingScreen == Constants.Commons.REVIEW && alertService.checkAlertSharedToCurrentUserEvdas(exEvdasConfiguration)) {
            forward(controller: 'errors', action: 'permissionsError')
            log.info("${userService.getUser()?.username} does not have access to alert")
            return
        }

        if (params.callingScreen != Constants.Commons.DASHBOARD && !cumulative) {
            name = exEvdasConfiguration?.name
            EvdasConfiguration config = EvdasConfiguration.findById(configId)
            List<Date> dateRange = exEvdasConfiguration?.dateRangeInformation?.getReportStartAndEndDate()
            if (exEvdasConfiguration?.dateRangeInformation?.dateRangeEnum == config.dateRangeInformation?.dateRangeEnum.CUMULATIVE) {
                Date dateRangeEnd = exEvdasConfiguration?.dateCreated
                dr = (dateRange ? DateUtil.toDateString1(dateRange[0]) : "-") + " to " + (DateUtil.toDateString1(dateRangeEnd))
                startDate = Date.parse("dd-MMM-yyyy", DateUtil.toDateString1(dateRange[0])).format("dd/MMM/yy")
                endDate = Date.parse("dd-MMM-yyyy", DateUtil.toDateString1(dateRangeEnd)).format("dd/MMM/yy")
            } else {
                dr = (dateRange ? DateUtil.toDateString1(dateRange[0]) : "-") + " to " + (dateRange ? DateUtil.toDateString1(dateRange[1]) : "-")
                startDate = Date.parse("dd-MMM-yyyy", DateUtil.toDateString1(dateRange[0])).format("dd/MMM/yy")
                endDate = Date.parse("dd-MMM-yyyy", dateRange ? DateUtil.toDateString1(dateRange[1]) : "-").format("dd/MMM/yy")
            }
            List<ExecutedEvdasConfiguration> prevExecs = evdasAlertService.fetchPrevPeriodExecConfig(configId as Long, exEvdasConfiguration.id as Long)
            prevExecs.each { ec ->
                List prevDateRange = ec.dateRangeInformation?.getReportStartAndEndDate()
                if (config.dateRangeInformation.dateRangeEnum == config.dateRangeInformation?.dateRangeEnum.CUMULATIVE) {
                    listDateRange.add(Date.parse("dd-MMM-yyyy", DateUtil.toDateString1(ec.dateCreated)).format("dd/MMM/yy"))
                } else {
                    listDateRange.add(Date.parse("dd-MMM-yyyy", dateRange ? DateUtil.toDateString1(prevDateRange[1]) : "-").format("dd/MMM/yy"))
                }
            }
        } else {
            backUrl = createLink(controller: 'dashboard', action: 'index')
        }
        if (params.callingScreen == Constants.Commons.DASHBOARD) {
            alertType = Constants.AlertConfigType.EVDAS_ALERT_DASHBOARD
        }
        ViewInstance viewInstance = viewInstanceService.fetchSelectedViewInstance(alertType,params.viewId as Long)
        Map dispositionData = workflowRuleService.fetchDispositionData();

        String timezone = userService.getCurrentUserPreference()?.timeZone
        List<Map> availableAlertPriorityJustifications = Justification.fetchByAnyFeatureOn([JustificationFeatureEnum.alertPriority], false)*.toDto(timezone)
        Map dispositionIncomingOutgoingMap = workflowRuleService.fetchDispositionIncomingOutgoingMap()
        Boolean forceJustification = currentUser.workflowGroup?.forceJustification
        List<Map> availablePriorities = priorityService.listPriorityOrder()
        List<Map> fieldList = grailsApplication.config.signal.evdasColumnList.clone() as List<Map>
        if (params.callingScreen == Constants.Commons.REVIEW) {
            fieldList.remove([name: "name", display: "Alert Name", dataType: 'java.lang.String'])
        }
        Map actionTypeAndActionMap = alertService.getActionTypeAndActionMap()
        List<String> reviewCompletedDispostionList = dispositionService.getReviewCompletedDispositionList()
        Boolean isShareFilterViewAllowed = currentUser.isAdmin()
        Boolean isViewUpdateAllowed = viewInstance?.isViewUpdateAllowed(currentUser)
        Boolean hasEvdasReviewerAccess = hasReviewerAccess(Constants.AlertConfigType.EVDAS_ALERT)
        String buttonClass = hasEvdasReviewerAccess?"":"hidden"

        def evdasHelpMap = Holders.config.evdas.helpMap
        render view: 'details', model: [executedConfigId                    : id,
                                        configId                            : configId,
                                        backUrl                             : backUrl,
                                        eudraRules                          : getEudraRuleJson(),
                                        appName                             : Constants.AlertConfigType.EVDAS_ALERT,
                                        callingScreen                       : params.callingScreen,
                                        name                                : name,
                                        dr                                  : dr,
                                        dateRange                           : endDate,
                                        startDate                           : startDate,
                                        listDr                              : listDateRange,
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
                                        prevColumns                         : prevColumns,
                                        actionConfigList                    : actionConfigList,
                                        dispositionIncomingOutgoingMap      : dispositionIncomingOutgoingMap as JSON,
                                        dispositionData                     : dispositionData as JSON,
                                        forceJustification                  : forceJustification,
                                        availableAlertPriorityJustifications: availableAlertPriorityJustifications,
                                        availablePriorities                 : availablePriorities,
                                        isLatest                            : exEvdasConfiguration?.isLatest,
                                        prevColCount                        : prevColCount,
                                        appType                             : Constants.AlertConfigType.EVDAS_ALERT,
                                        fieldList                           : fieldList.sort({it.display.toUpperCase()}),
                                        actionTypeList                      : actionTypeAndActionMap.actionTypeList,
                                        reviewCompletedDispostionList       : JsonOutput.toJson(reviewCompletedDispostionList),
                                        actionPropertiesMap                 : JsonOutput.toJson(actionTypeAndActionMap.actionPropertiesMap),
                                        alertDispositionList                : alertDispositionList,
                                        isArchived                          : params.boolean('archived')?:false,
                                        alertType                           : alertType,
                                        isPriorityEnabled                   : grailsApplication.config.alert.priority.enable,
                                        hasEvdasReviewerAccess              : hasEvdasReviewerAccess,
                                        hasSignalCreationAccessAccess       : hasSignalCreationAccessAccess(),
                                        hasSignalViewAccessAccess           : hasSignalViewAccessAccess(),
                                        buttonClass                         : buttonClass,
                                        hasReportingAccess                  : SpringSecurityUtils.ifAnyGranted("ROLE_REPORTING"),
                                        isTempViewSelected                  : isTempViewSelected,
                                        currUserName                        : currentUser.fullName,
                                        evdasHelpMap                        : evdasHelpMap
        ]
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER'])
    def toggleFlag() {
        def id = params.id

        if (id) {
            def flagged = evdasAlertService.toggleEvdasAlertFlag(id)
            render([success: 'ok', flagged: flagged] as JSON)
        } else {
            response.status = 404
        }
    }
    void alertDeletionInProgress() {
        redirect(action: "alertInProgressError", controller: 'errors')
    }

    private getEudraRuleJson() {
        def eudraRules = grailsApplication.config.eudraRules
        def eudraRulesArray = new JSONArray()
        eudraRules.each {
            def eudraRulesObj = new JSONObject()
            eudraRulesObj.put("parameterName", it.parameterName)
            eudraRulesObj.put("value", it.value)
            eudraRulesObj.put("change", it.change)
            eudraRulesObj.put("color", it.color)
            eudraRulesArray.add(eudraRulesObj)
        }
        eudraRulesArray.toString()
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def listByExecutedConfig(Boolean cumulative, Long id, Boolean isFilterRequest) {
        def startTime=System.currentTimeSeconds()
        ConcurrentHashMap finalMap = [recordsTotal: 0, recordsFiltered: 0, aaData: [], filters: [], configId: id, visibleIdList: []]
        try {
            List filters = getFiltersFromParams(isFilterRequest, params)
            Integer totalColumns = 65
            Map filterMap = alertService.prepareFilterMap(params, totalColumns)
            Map orderColumnMap = alertService.prepareOrderColumnMap(params)
            ExecutedEvdasConfiguration executedConfig = null
            if(filterMap.assignedTo != null){
                String name = filterMap.remove("assignedTo")
                filterMap.put("assignedToUser", name)
            }
            if ((!cumulative && params.callingScreen != Constants.Commons.DASHBOARD) || params.adhocRun.toBoolean()) {
                executedConfig = ExecutedEvdasConfiguration.findByIdAndIsEnabled(id, true)
            }

            User user = userService.getUser()
            AlertDataDTO alertDataDTO = new AlertDataDTO()
            alertDataDTO.params = params
            alertDataDTO.domainName = params.boolean('isArchived') ? ArchivedEvdasAlert : EvdasAlert
            alertDataDTO.executedConfiguration = executedConfig
            alertDataDTO.execConfigId = executedConfig?.id
            alertDataDTO.filterMap = filterMap
            alertDataDTO.orderColumnMap = orderColumnMap
            alertDataDTO.userId = user.id
            alertDataDTO.workflowGroupId = user.workflowGroup.id
            alertDataDTO.groupIdList = user.groups.collect { it.id }
            alertDataDTO.cumulative = cumulative
            alertDataDTO.dispositionFilters = filters
            alertDataDTO.length = params.int("length")
            alertDataDTO.start = params.int("start")
            alertDataDTO.configId = executedConfig?.configId

            Set dispositionSet = []
            if (params.callingScreen != Constants.Commons.DASHBOARD) {
                dispositionSet = alertService.getDispositionSet(alertDataDTO.executedConfiguration, alertDataDTO.domainName, isFilterRequest,params)
            } else {
                dispositionSet = alertService.getDispositionSetDashboard(isFilterRequest,alertDataDTO)
            }
            Map filterCountAndList = alertService.getAlertFilterCountAndList(alertDataDTO)
            if(!filters?.isEmpty() || alertDataDTO.advancedFilterDispositions){
                List visibleIdList = filterCountAndList?.resultList*.id
                finalMap = [recordsTotal: filterCountAndList.totalCount, recordsFiltered: filterCountAndList.totalFilteredCount, aaData: filterCountAndList.resultList, filters: dispositionSet, configId: id,visibleIdList: visibleIdList]
            }
            else {
                finalMap = [recordsTotal: filterCountAndList.totalCount, recordsFiltered: 0, aaData: [], filters: dispositionSet, configId: id,  visibleIdList : []]
            }
            finalMap .put("advancedFilterDispName",alertDataDTO.advancedFilterDispName)
        } catch(Throwable th) {
            th.printStackTrace()
        }
        def endTime=System.currentTimeSeconds()
        log.info("it took ${endTime-startTime} to fetch all evdas alerts")
        ConcurrentHashMap renderedFinalMap = finalMap
        render renderedFinalMap as JSON
    }

    private List<Disposition> getDispositionsForName(dispositionFilters) {
        List dispositionList = []
        if (dispositionFilters) {
            dispositionList = Disposition.findAllByDisplayNameInList(dispositionFilters)
        }
        dispositionList
    }

    private getTotalCaseCount(boolean cumulative, filterMap, dispositionFilters, params, alertList) {
        def totalCountCriteria = EvdasAlert.createCriteria()
        def totalFilteredCountCriteria = EvdasAlert.createCriteria()
        def totalCount = 0
        def totalFilteredCount = 0
        def ec = null
        def openDispositions = getDispositionsForName(dispositionFilters)
        def currentDate_a = new Date()
        def currentDate = (DateUtils.truncate(currentDate_a, Calendar.DAY_OF_MONTH))
        Integer integerMinValue = Integer.MIN_VALUE
        def user = userService.getUser()

        //Show the list only when atleast one disposition is checked. If its not then list will be null.
        if (openDispositions) {
            if (params.callingScreen == Constants.Commons.TRIGGERED_ALERTS && !params.adhocRun.toBoolean() && !alertList) {
            } else {
                totalCount = totalCountCriteria.count {
                    'in'("disposition", openDispositions)
                    if (params.callingScreen == Constants.Commons.DASHBOARD) {
                        and {
                            eq("assignedTo", user)
                            if (alertList.size() > 0) {
                                or {
                                    alertList.collate(1000).each {
                                        'in'("id", it)
                                    }
                                }
                            }
                        }
                    } else if (params.callingScreen != Constants.Commons.TRIGGERED_ALERTS && params.callingScreen != Constants.Commons.DASHBOARD) {
                        ec = ExecutedEvdasConfiguration.findByIdAndIsEnabled(params.id, true)
                        and {
                            eq("executedAlertConfiguration", ec)
                        }
                    }
                    if (params.callingScreen == Constants.Commons.DASHBOARD || params.callingScreen == Constants.Commons.TRIGGERED_ALERTS) {
                        'executedAlertConfiguration' {
                            eq('adhocRun', false)
                        }
                    }
                    if (params.callingScreen == Constants.Commons.TRIGGERED_ALERTS && !params.adhocRun.toBoolean()) {
                        or {
                            alertList.collate(1000).each{
                                'in'('id', it)
                            }
                        }
                    }
                }
                totalFilteredCount = totalFilteredCountCriteria.count {
                    'in'("disposition", openDispositions)
                    //Calling screen is review
                    if (params.callingScreen != Constants.Commons.DASHBOARD && params.callingScreen != Constants.Commons.TRIGGERED_ALERTS) {
                        eq("executedAlertConfiguration", ec)
                    } else if (params.callingScreen == Constants.Commons.DASHBOARD) {
                        eq("assignedTo", user)
                        if (alertList.size() > 0) {
                            or {
                                alertList.collate(1000).each {
                                    'in'("id", it)
                                }
                            }
                        }
                    }
                    if (params.callingScreen == Constants.Commons.DASHBOARD || params.callingScreen == Constants.Commons.TRIGGERED_ALERTS) {
                        'executedAlertConfiguration' {
                            eq('adhocRun', false)
                        }
                    }
                    if (params.callingScreen == Constants.Commons.TRIGGERED_ALERTS && !params.adhocRun.toBoolean()) {
                        or {
                            alertList.collate(1000).each{
                                'in'('id', it)
                            }
                        }
                    }
                    //If filter maps are coming then we prepare the filter map.
                    and {
                        filterMap.each { k, v ->
                            if (k == 'workflowState') {
                                'workflowState' {
                                    ilike('displayName', '%' + v + '%')
                                }
                            } else if (k == 'disposition') {
                                'disposition' {
                                    ilike('displayName', '%' + v + '%')
                                }
                            } else if (k == 'assignedTo') {
                                assignedTo {
                                    ilike('fullName', '%' + v + '%')
                                }
                            } else if (k == 'newEv') {
                                or {
                                    eq(k, v.isInteger() ? v.toInteger() : integerMinValue)
                                    eq('totalEv', v.isInteger() ? v.toInteger() : integerMinValue)
                                }
                            } else if (k == 'newSerious') {
                                or {
                                    eq(k, v.isInteger() ? v.toInteger() : integerMinValue)
                                    eq('totalSerious', v.isInteger() ? v.toInteger() : integerMinValue)
                                }
                            } else if (k == 'newFatal') {
                                or {
                                    eq(k, v.isInteger() ? v.toInteger() : integerMinValue)
                                    eq('totalFatal', v.isInteger() ? v.toInteger() : integerMinValue)
                                }
                            } else if (k == 'rorValue') {
                                ilike(k, '%' + v.toString() + '%')
                            } else if (k == 'newEea') {
                                or {
                                    ilike(k, '%' + v + '%')
                                    ilike('totEea', '%' + v + '%')
                                }
                            } else if (k == 'newHcp') {
                                or {
                                    ilike(k, '%' + v + '%')
                                    ilike('totHcp', '%' + v + '%')
                                }
                            } else if (k == 'newMedErr') {
                                or {
                                    ilike(k, '%' + v + '%')
                                    ilike('totMedErr', '%' + v + '%')
                                }
                            } else if (k == 'newAbuse') {
                                or {
                                    ilike(k, '%' + v + '%')
                                    ilike('totAbuse', '%' + v + '%')
                                }
                            } else if (k == 'newObs') {
                                or {
                                    ilike(k, '%' + v + '%')
                                    ilike('totObs', '%' + v + '%')
                                }
                            } else if (k == 'newOccup') {
                                or {
                                    ilike(k, '%' + v + '%')
                                    ilike('totOccup', '%' + v + '%')
                                }
                            } else if (k == 'newCt') {
                                or {
                                    ilike(k, '%' + v + '%')
                                    ilike('totCt', '%' + v + '%')
                                }
                            } else if (k == 'newRc') {
                                or {
                                    ilike(k, '%' + v + '%')
                                    ilike('totRc', '%' + v + '%')
                                }
                            } else if (k == 'newLit') {
                                or {
                                    ilike(k, '%' + v + '%')
                                    ilike('totalLit', '%' + v + '%')
                                }
                            } else if (k == 'newPaed') {
                                or {
                                    ilike(k, '%' + v + '%')
                                    ilike('totPaed', '%' + v + '%')
                                }
                            } else if (k == 'newGeria') {
                                or {
                                    ilike(k, '%' + v + '%')
                                    ilike('totGeria', '%' + v + '%')
                                }
                            } else if (k == 'newSpon') {
                                or {
                                    ilike(k, '%' + v + '%')
                                    ilike('totSpon', '%' + v + '%')
                                }
                            } else if (k == 'signalsAndTopics') {
                                createAlias("validatedSignals", "vs", JoinType.LEFT_OUTER_JOIN)
                                ilike('vs.name', '%' + v + '%')
                            } else if (k == 'dueDate') {
                                if ((v.isInteger() ? v.toInteger() : 0) < 0) {
                                    between('dueDate', currentDate + (v.isInteger() ? v.toInteger() + 1 : 0), currentDate)
                                } else {
                                    between('dueDate', currentDate, (currentDate + (v.isInteger() ? v.toInteger() + 1 : 0)))
                                }
                            } else {
                                ilike(k, '%' + v + '%')
                            }
                        }
                    }
                }
            }
        }
        [totalCount: totalCount, totalFilteredCount: totalFilteredCount]
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def getAllowedAlerts(executedConfig, domainName, dispositionFilters, params, filterMap, orderColumnMap, alertList) {
        def listColName = ["substance", "name", "pt", "soc", "rorValue", "newFatal", "newSerious", "newEv", "newLit", "sdr",
                           "smqNarrow", "hlgt", "hlt", "newEea", "newHcp", "newMedErr", "newAbuse", "newOccup", "newObs", "newCt",
                           "newRc", "newPaed", "newGeria", "newSpon", "roa1", "newRoa1", "totRoa1", "europeRor", "northAmericaRor",
                           "japanRor", "asiaRor", "restRor", "allRor", "dueDate", "flags"]
        def length = params.length.isInteger() ? params.length.toInteger() : 0
        def start = params.start.isInteger() ? params.start.toInteger() : 0
        def list = []
        Object openDispositions = getDispositionsForName(dispositionFilters)
        def currentDate_a = new Date()
        def currentDate = DateUtils.truncate(currentDate_a, Calendar.DAY_OF_MONTH)
        Integer integerMinValue = Integer.MIN_VALUE

        if (openDispositions) {
            if (params.callingScreen == Constants.Commons.TRIGGERED_ALERTS && !params.adhocRun.toBoolean() && !alertList) {
            } else {
                list = domainName.withCriteria {
                    'in'("disposition", openDispositions)
                    if (params.callingScreen != Constants.Commons.DASHBOARD && params.callingScreen != Constants.Commons.TRIGGERED_ALERTS) {
                        eq("executedAlertConfiguration", executedConfig)
                    } else if (params.callingScreen == Constants.Commons.DASHBOARD) {
                        eq("assignedTo", userService.getUser())
                        if (alertList.size() > 0) {
                            or {
                                alertList.collate(1000).each {
                                    'in'("id", it)
                                }
                            }
                        }
                    }
                    if (params.callingScreen == Constants.Commons.DASHBOARD || params.callingScreen == Constants.Commons.TRIGGERED_ALERTS) {
                        'executedAlertConfiguration' {
                            eq('adhocRun', false)
                        }
                    }
                    if (params.callingScreen == Constants.Commons.TRIGGERED_ALERTS && !params.adhocRun.toBoolean()) {
                        or {
                            alertList.collate(1000).each{
                                'in'('id', it)
                            }
                        }
                    }
                    and {
                        filterMap.each { k, v ->
                            if (k == 'workflowState') {
                                'workflowState' {
                                    ilike('displayName', '%' + v + '%')
                                }
                            } else if (k == 'disposition') {
                                'disposition' {
                                    ilike('displayName', '%' + v + '%')
                                }
                            } else if (k == 'assignedTo') {
                                assignedTo {
                                    ilike('fullName', '%' + v + '%')
                                }
                            } else if (k == 'newEv') {
                                or {
                                    eq(k, v.isInteger() ? v.toInteger() : integerMinValue)
                                    eq('totalEv', v.isInteger() ? v.toInteger() : integerMinValue)
                                }
                            } else if (k == 'newSerious') {
                                or {
                                    eq(k, v.isInteger() ? v.toInteger() : integerMinValue)
                                    eq('totalSerious', v.isInteger() ? v.toInteger() : integerMinValue)
                                }
                            } else if (k == 'newFatal') {
                                or {
                                    eq(k, v.isInteger() ? v.toInteger() : integerMinValue)
                                    eq('totalFatal', v.isInteger() ? v.toInteger() : integerMinValue)
                                }
                            } else if (k == 'rorValue') {
                                ilike(k, '%' + v.toString() + '%')
                            } else if (k == 'newEea') {
                                or {
                                    ilike(k, '%' + v + '%')
                                    ilike('totEea', '%' + v + '%')
                                }
                            } else if (k == 'newHcp') {
                                or {
                                    ilike(k, '%' + v + '%')
                                    ilike('totHcp', '%' + v + '%')
                                }
                            } else if (k == 'newMedErr') {
                                or {
                                    ilike(k, '%' + v + '%')
                                    ilike('totMedErr', '%' + v + '%')
                                }
                            } else if (k == 'newAbuse') {
                                or {
                                    ilike(k, '%' + v + '%')
                                    ilike('totAbuse', '%' + v + '%')
                                }
                            } else if (k == 'newObs') {
                                or {
                                    ilike(k, '%' + v + '%')
                                    ilike('totObs', '%' + v + '%')
                                }
                            } else if (k == 'newOccup') {
                                or {
                                    ilike(k, '%' + v + '%')
                                    ilike('totOccup', '%' + v + '%')
                                }
                            } else if (k == 'newCt') {
                                or {
                                    ilike(k, '%' + v + '%')
                                    ilike('totCt', '%' + v + '%')
                                }
                            } else if (k == 'newRc') {
                                or {
                                    ilike(k, '%' + v + '%')
                                    ilike('totRc', '%' + v + '%')
                                }
                            } else if (k == 'newLit') {
                                or {
                                    ilike(k, '%' + v + '%')
                                    ilike('totalLit', '%' + v + '%')
                                }
                            } else if (k == 'newPaed') {
                                or {
                                    ilike(k, '%' + v + '%')
                                    ilike('totPaed', '%' + v + '%')
                                }
                            } else if (k == 'newGeria') {
                                or {
                                    ilike(k, '%' + v + '%')
                                    ilike('totGeria', '%' + v + '%')
                                }
                            } else if (k == 'newSpon') {
                                or {
                                    ilike(k, '%' + v + '%')
                                    ilike('totSpon', '%' + v + '%')
                                }
                            } else if (k == 'signalsAndTopics') {
                                createAlias("validatedSignals", "vs", JoinType.LEFT_OUTER_JOIN)
                                ilike('vs.name', '%' + v + '%')
                            } else if (k == 'dueDate') {
                                if ((v.isInteger() ? v.toInteger() : 0) < 0) {
                                    between('dueDate', currentDate + (v.isInteger() ? v.toInteger() + 1 : 0), currentDate)
                                } else {
                                    between('dueDate', currentDate, (currentDate + (v.isInteger() ? v.toInteger() + 1 : 0)))
                                }
                            } else {
                                ilike(k, '%' + v + '%')
                            }
                        }
                    }
                    if (orderColumnMap.name == "workflowState") {
                        'workflowState' {
                            order('displayName', orderColumnMap.dir)
                        }
                    } else if (orderColumnMap.name == "disposition") {
                        'disposition' {
                            order('displayName', orderColumnMap.dir)
                        }
                    } else if (orderColumnMap.name == 'assignedTo') {
                        'assignedTo' {
                            order('username', orderColumnMap.dir)
                        }
                    } else if (orderColumnMap.name == 'priority') {
                        order('dueDate', orderColumnMap.dir)
                    } else if (orderColumnMap.name == "actions") {
                        order('actionCount', orderColumnMap.dir)
                    } else if (orderColumnMap.name in listColName) {
                        order(orderColumnMap.name, orderColumnMap.dir)
                    } else {
                        order("lastUpdated", "desc")
                    }
                    maxResults(length)
                    firstResult(start)
                    order("lastUpdated", "desc")
                }
            }
        }
        list
    }

    private Object getOpenDispositions(dispositionFilters) {
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

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def getEvdasData() {
        //TODO: Need to integrate it with the domain.
        def documents = [
                [
                        "documentName"   : "eRMR-Substance1-2016-Q4",
                        "uploadTimeStamp": "31-Dec-2016 12:30:35",
                        "substanceName"  : "Substance1",
                        "dataRange"      : "1-Oct-2016 to 31-Dev-2016",
                        "uploadStatus"   : "SUCCESS"
                ],
                [
                        "documentName"   : "eRMR-Substance1-2017-Q1",
                        "uploadTimeStamp": "31-Mar-2017 12:30:35",
                        "substanceName"  : "Substance1",
                        "dataRange"      : "1-Jan-2017 to 31-Mar-2017",
                        "uploadStatus"   : "FAIL"
                ],
                [
                        "documentName"   : "eRMR-Substance1-2017-Q2",
                        "uploadTimeStamp": "30-Jun-2017 12:30:35",
                        "substanceName"  : "Substance1",
                        "dataRange"      : "1-Apr-2017 to 30-Jun-2017",
                        "uploadStatus"   : "SUCCESS"
                ]
        ]

        respond documents, [formats: ['json']]
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def downloadDocument() {
        def fileRef = grailsApplication.config.signal.evdas.file
        def file = new File(fileRef)
        String reportName = file.name
        response.contentType = "xlsm; charset=UTF-8"
        response.contentLength = file.length()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}" + "\"")
        response.getOutputStream().write(file.bytes)
        response.outputStream.flush()
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def showTrendAnalysis() {
        def alertId = params.id
        redirect(controller: "trendAnalysis", action: 'showTrendAnalysis', params: [id: alertId, type: Constants.AlertConfigType.EVDAS_ALERT])
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def fetchStratifiedScores(Long alertId, Boolean isPRR) {
        //ToDo This is dummy implementation change it.
        AggregateCaseAlert aggregateCaseAlert = AggregateCaseAlert.first()
        String jsonString = null
        def object = [:]
        if (isPRR) {
            jsonString = aggregateCaseAlert.prrStr
            object.total = aggregateCaseAlert.prrValue
        } else {
            jsonString = aggregateCaseAlert.rorStr
            object.total = aggregateCaseAlert.rorValue
        }

        try {
            if (jsonString) {
                def jsonSlurper = new JsonSlurper()
                object = object + jsonSlurper.parseText(jsonString)
            } else {
                //TODO: This is not right. Need to correct it.
                object.male = 12.7
                object.female = 14.8
                object.others = 20.8
            }
        } catch (Throwable t) {
            t.printStackTrace()
        }
        render(object as JSON)
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def exportReport() {
        List listDateRange = []
        ExecutedEvdasConfiguration executedConfig = null
        Boolean isDashboard = params.callingScreen == Constants.Commons.DASHBOARD ? true : false
        Boolean cumulative = userService.getUser()?.preference?.isCumulativeAlertEnabled ?: false
        Boolean isExcelExport = isExcelExportFormat(params.outputFormat)
        if (params.adhocRun.toBoolean()) {
            cumulative = false
        }
        List evdasList
        if (!cumulative && !params.adhocRun.toBoolean() && params.callingScreen != Constants.Commons.DASHBOARD) {
            ExecutedEvdasConfiguration executedEvdasConfiguration = ExecutedEvdasConfiguration.findById(params.id)
            def config = EvdasConfiguration.findByName(executedEvdasConfiguration.name)
            def dateRange = config.dateRangeInformation?.getReportStartAndEndDate()
            List<ExecutedEvdasConfiguration> prevExecs = evdasAlertService.fetchPrevPeriodExecConfig(config?.id as Long, executedEvdasConfiguration.id as Long)
            prevExecs.each { ec ->
                if (config.dateRangeInformation.dateRangeEnum == config.dateRangeInformation?.dateRangeEnum.CUMULATIVE) {
                    listDateRange.add(Date.parse("dd-MMM-yyyy", DateUtil.toDateString1(ec.dateCreated)).format("dd/MMM/yy"))
                } else {
                    listDateRange.add(Date.parse("dd-MMM-yyyy", dateRange ? DateUtil.toDateString1(dateRange[1]) : "-").format("dd/MMM/yy"))
                }
            }
        } else if (cumulative) {
            Map freqDateRange = fetchDateRangeMap(params.frequency)
            freqDateRange.each { key, value ->
                if (key != 'exeRecent') {
                    listDateRange.add(value['startDate'].replaceAll("-", "/") + "-" + value['endDate'].replaceAll("-", "/"))
                }
            }
        }
        params['listDateRange'] = listDateRange
        AlertDataDTO alertDataDTO = new AlertDataDTO()
        alertDataDTO.params = params
        alertDataDTO.userId = userService.getUser().id
        alertDataDTO.cumulative = cumulative
        alertDataDTO.domainName = params.boolean('isArchived') ? ArchivedEvdasAlert : EvdasAlert
        alertDataDTO.isFromExport = true
        if(isDashboard) {
            alertDataDTO.length = 5000
        }


        if (params.selectedCases) {
            def eaList = evdasAlertService.listSelectedAlerts(params.selectedCases, alertDataDTO.domainName)
            if (!cumulative) {
                executedConfig = ExecutedEvdasConfiguration.findByIdAndIsEnabled(params.id, true)
            }
            alertDataDTO.executedConfiguration = executedConfig
            alertDataDTO.execConfigId = executedConfig?.id
            alertDataDTO.configId = executedConfig?.configId
            evdasList = evdasAlertService.fetchEvdasAlertList(eaList, alertDataDTO)
        } else {
            Map filterMap = [:]
            if (params.filterList && params.filterList != "{}") {
                def jsonSlurper = new JsonSlurper()
                filterMap = jsonSlurper.parseText(params.filterList)
            }

            List dispositionFilters = getFiltersFromParams(params.isFilterRequest?.toBoolean(), params)

            if ((!cumulative && params.callingScreen != Constants.Commons.DASHBOARD) || params.adhocRun?.toBoolean()) {
                executedConfig = ExecutedEvdasConfiguration.findByIdAndIsEnabled(params.id, true)
            }

            User user = userService.getUser()
            alertDataDTO.executedConfiguration = executedConfig
            alertDataDTO.execConfigId = executedConfig?.id
            alertDataDTO.filterMap = filterMap
            alertDataDTO.orderColumnMap = [name: params.column, dir: params.sorting]
            alertDataDTO.workflowGroupId = user.workflowGroup.id
            alertDataDTO.groupIdList = user.groups.collect { it.id }
            alertDataDTO.dispositionFilters = dispositionFilters
            alertDataDTO.configId = executedConfig?.configId

            Map filterCountAndList = alertService.getAlertFilterCountAndList(alertDataDTO)
            evdasList = filterCountAndList.resultList
        }
        if(alertDataDTO.dispositionFilters?.isEmpty()) {
            evdasList = []
        }
        def prevColMap = Holders.config.signal.evdas.data.previous.columns.clone()
        if (params.outputFormat == ReportFormat.XLSX.name()) {
            prevColMap = prevColMap +  ["totalEv" : "Total EV", "totalSerious": "Total Ser", "totalFatal": "Total Fatal", "totalLit": "Total Lit"]
        }
        evdasList.each {
            it.name = it?.alertName
            it.substance = it?.productName
            it.pt = it?.preferredTerm
            it.newFatal     =  isExcelExport ? "" + it.newFatal     : "    " + it.newFatal   + "\n    " + it.totalFatal
            it.newSerious   =  isExcelExport ? "" + it.newSerious   : "    " + it.newSerious + "\n    " + it.totalSerious
            it.newEv        =  isExcelExport ? "" + it.newEv        : "    " + it.newEv      + "\n    " + it.totalEv
            it.newLit       =  isExcelExport ? "" + it.newLit       : "    " + it.newLit     + "\n    " + it.totalLit
            it.newEea       =  isExcelExport ? "" + it.newEea       : "    " + it.newEea     + "\n    " + it.totEea
            it.newHcp       =  isExcelExport ? "" + it.newHcp       : "    " + it.newHcp     + "\n    " + it.totHcp
            it.newMedErr    =  isExcelExport ? "" + it.newMedErr    : "    " + it.newMedErr  + "\n    " + it.totMedErr
            it.newAbuse     =  isExcelExport ? "" + it.newAbuse     : "    " + it.newAbuse   + "\n    " + it.totAbuse
            it.newOccup     =  isExcelExport ? "" + it.newOccup     : "    " + it.newOccup   + "\n    " + it.totOccup
            it.newObs       =  isExcelExport ? "" + it.newObs       : "    " + it.newObs     + "\n    " + it.totObs
            it.newCt        =  isExcelExport ? "" + it.newCt        : "    " + it.newCt      + "\n    " + it.totCt
            it.newRc        =  isExcelExport ? "" + it.newRc        : "    " + it.newRc      + "\n    " + it.totRc
            it.newPaed      =  isExcelExport ? "" + it.newPaed      : "    " + it.newPaed    + "\n    " + it.totPaed
            it.newGeria     =  isExcelExport ? "" + it.newGeria     : "    " + it.newGeria   + "\n    " + it.totGeria
            it.newSpont      =  isExcelExport ? "" + it.newSpont      : "    " + it.newSpont    + "\n    " + it.totSpont

            if(isExcelExport) {
                it.totalFatal   =  "" + it.totalFatal
                it.totalSerious =  "" + it.totalSerious
                it.totalEv      =  "" + it.totalEv
                it.totalLit     =  "" + it.totalLit
                it.totEea       =  "" + it.totEea
                it.totHcp       =  "" + it.totHcp
                it.totMedErr    =  "" + it.totMedErr
                it.totAbuse     =  "" + it.totAbuse
                it.totOccup     =  "" + it.totOccup
                it.totObs       =  "" + it.totObs
                it.totCt        =  "" + it.totCt
                it.totRc        =  "" + it.totRc
                it.totPaed      =  "" + it.totPaed
                it.totGeria     =  "" + it.totGeria
                it.totSpon      =  "" + it.totSpon
            }
            it.dueDate      =  it.dueIn + ""
            it.currentDisposition = it.disposition
            String signalTopics = ""
            signalTopics = it.signalsAndTopics.collect { it.name }?.join(",")
            it.soc = dataObjectService.getAbbreviationMap(it.soc) + ""
            it.signalsAndTopics = signalTopics
            if (listDateRange.size() > 0) {
                (0..listDateRange.size() - 1).each { exeNum ->
                    def exeName = 'exe' + exeNum
                    prevColMap.each { fn, cn ->
                        if(it[exeName]){
                            it.put(exeName + fn, it[exeName][fn])
                        }
                    }
                }
            }
        }
        if(isExcelExport) {
            EvdasConfiguration config  =  EvdasConfiguration.findByName(executedConfig?.name)
            List<Date> dateRange = executedConfig?.dateRangeInformation?.getReportStartAndEndDate()
            String reportDateRange
            if (config?.dateRangeInformation?.dateRangeEnum == config?.dateRangeInformation?.dateRangeEnum?.CUMULATIVE) {
                Date dateRangeEnd = executedConfig?.dateCreated
                reportDateRange = (dateRange ? DateUtil.toDateString1(dateRange[0]) : "-") + " to " + (DateUtil.toDateString1(dateRangeEnd))
            }else {
                reportDateRange = (dateRange ? DateUtil.toDateString1(dateRange[0]) : "-") + " to " + (dateRange ? DateUtil.toDateString1(dateRange[1]) : "-")
            }
            String datasheets = ""
            String enabledSheet = Constants.DatasheetOptions.CORE_SHEET
            if(params.callingScreen != Constants.Commons.DASHBOARD){
                if(!TextUtils.isEmpty(executedConfig?.selectedDataSheet)){
                    datasheets = dataSheetService.formatDatasheetMap(executedConfig)?.text?.join(',')
                }else {
                    Boolean isProductGroup = !TextUtils.isEmpty(executedConfig.productGroupSelection)
                    String products = executedConfig.productGroupSelection?:executedConfig.productSelection
                    datasheets =  dataSheetService.fetchDataSheets(products,enabledSheet, isProductGroup, false)?.dispName?.join(',')
                }
            }
            Map criteriaData      =  [alertName      : executedConfig?.name, productName    : (getNameFieldFromJson(executedConfig?.productSelection))?:getGroupNameFieldFromJson(executedConfig?.productGroupSelection),
                                      datasheets: datasheets?:"" ,dateRange      : reportDateRange, cumulative: false, advncedFilter : params.advancedFilterId ? advancedFilterService.getAvdFilterCriteriaExcelExport(params.advancedFilterId as Long) : '-']
            Map reportParamsMap   =  ["showCompanyLogo" : true, "showLogo" : true, "header" : "EVDAS Review Report"]
            params << reportParamsMap
            params.criteriaData = criteriaData
        }
        List currentDispositionList = []
        evdasList.each{
            currentDispositionList.add(it?.currentDisposition)
        }
        def uniqueDispositions = currentDispositionList.toSet()
        String quickFilterDisposition = uniqueDispositions?.join(", ")
        params.quickFilterDisposition = quickFilterDisposition
        List criteriaSheetList
        params.totalCount = evdasList?.size()?.toString()
        if ((!cumulative && params.callingScreen != Constants.Commons.DASHBOARD) || params.adhocRun.toBoolean()) {
            criteriaSheetList = evdasAlertService.getEvdasAlertCriteriaData(executedConfig, params)
            params.criteriaSheetList = criteriaSheetList
        }
        Boolean isLongComment = evdasList?.any({x -> x.comment?.size() > 100})
        params.isLongComment = isLongComment?: false
        def reportFile = dynamicReportService.createEvdasAlertsReport(new JRMapCollectionDataSource(evdasList), params)
        renderReportOutputType(reportFile)
        signalAuditLogService.createAuditForExport(criteriaSheetList, isDashboard == true ? Constants.Commons.DASHBOARD : executedConfig.getInstanceIdentifierForAuditLog()+ ": Alert Details", isDashboard == true ? "EVDAS Review Dashboard" : (Constants.AuditLog.EVDAS_REVIEW + (executedConfig.isLatest ? "" : ": Archived Alert")),params,reportFile.name)

    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def exportSignalSummaryReport(Long id, Boolean cumulative, String callingScreen, String outputFormat) {
        List validatedSignalList = []
        List notStartedReviewSignalList = []
        List pendingReviewList = []
        List closedReviewList = []
        Map signalData = [:]
        List eaList = []
        ExecutedEvdasConfiguration ec = null
        Group workflowGroup = userService?.getUser()?.getWorkflowGroup()
        String defaultDispositionValue = workflowGroup?.defaultEvdasDisposition?.value
        def domainName = evdasAlertService.getDomainObject(params.boolean('isArchived'))

        AlertDataDTO alertDataDTO = new AlertDataDTO()
        alertDataDTO.params = params
        alertDataDTO.userId = userService.getUser().id
        alertDataDTO.cumulative = cumulative
        alertDataDTO.domainName = params.boolean('isArchived') ? ArchivedEvdasAlert : EvdasAlert
        alertDataDTO.isFromExport = true

        ec = ExecutedEvdasConfiguration.findByIdAndIsEnabled(params.id, true)
        if (params.selectedCases) {
            String[] alertList = params.selectedCases.split(",")
            eaList = alertList.collect { it as Long }
        } else {
            Map filterMap = [:]
            if (params.filterList && params.filterList != "{}") {
                def jsonSlurper = new JsonSlurper()
                filterMap = jsonSlurper.parseText(params.filterList)
            }

            List dispositionFilters = getFiltersFromParams(params.isFilterRequest.toBoolean(), params)

            User user = userService.getUser()
            alertDataDTO.executedConfiguration = ec
            alertDataDTO.execConfigId = ec?.id
            alertDataDTO.filterMap = filterMap
            alertDataDTO.orderColumnMap = [name: params.column, dir: params.sorting]
            alertDataDTO.workflowGroupId = user.workflowGroup.id
            alertDataDTO.groupIdList = user.groups.collect { it.id }
            alertDataDTO.dispositionFilters = dispositionFilters
            alertDataDTO.configId = ec?.configId

            eaList = alertService.getAlertFilterIdList(alertDataDTO)
        }

        List currentDispositionList = []
        List evdasCaseAlertList = []
        if(eaList) {
            evdasCaseAlertList = domainName.createCriteria().list {
                or {
                    eaList.collate(1000).each {
                        'in'("id", it)
                    }
                }
            }
        }
        if(evdasCaseAlertList) {
            evdasCaseAlertList.each { evdas ->
                currentDispositionList.add(evdas?.disposition.displayName)
                if (evdas.disposition.isValidatedConfirmed()) {
                    validatedSignalList << evdas
                } else if (evdas.disposition.isClosed()) {
                    closedReviewList << evdas
                } else if (evdas.disposition.value == defaultDispositionValue) {
                    notStartedReviewSignalList << evdas
                } else {
                    pendingReviewList << evdas
                }
            }
        }

        def uniqueDispositions = currentDispositionList.toSet()
        String quickFilterDisposition = uniqueDispositions?.join(", ")
        params.quickFilterDisposition = quickFilterDisposition
        EvdasConfiguration config = EvdasConfiguration.findByNameAndOwner(ec.name, ec.owner)
        def dateRange = config?.dateRangeInformation?.getReportStartAndEndDate()
        def reportDateRange = (dateRange ? DateUtil.toDateString1(dateRange[0]) : "-") + " to " + (dateRange ? DateUtil.toDateString1(dateRange[1]) : "-")
        def referenceNumber = config?.referenceNumber ?: "-"
        def otherCriteria = config.queryName
        //to check for product name list method
        signalData = [alertName    : config.name, productName: config.getProductNameList() ?: "-", dateRange: reportDateRange,
                      otherCriteria: otherCriteria?.size() > 0 ? otherCriteria?.join(",") : '-', referenceNumber: referenceNumber]


        List<Map> evdasHistoryMap = evdasAlertService.generateEvdasHistoryMap(config.id)
        validatedSignalList = evdasAlertService.getSignalDetectionSummaryMap(validatedSignalList, evdasHistoryMap)
        notStartedReviewSignalList = evdasAlertService.getSignalDetectionSummaryMap(notStartedReviewSignalList, evdasHistoryMap)
        pendingReviewList = evdasAlertService.getSignalDetectionSummaryMap(pendingReviewList, evdasHistoryMap)
        closedReviewList = evdasAlertService.getSignalDetectionSummaryMap(closedReviewList, evdasHistoryMap)
        params.totalCount = eaList?.size()?.toString()
        List criteriaSheetList = evdasAlertService.getEvdasAlertCriteriaData(ec, params)
        Map reportParamsMap = ["showCompanyLogo"  : true,
                               "showLogo"         : true,
                               "header"           : message(code: 'app.signal.detection.evdas.alert'),
                               "isEvdasCaseAlert" : true,
                               "outputFormat"     : outputFormat,
                               'criteriaSheetList': criteriaSheetList]
        File reportFile = dynamicReportService.createSignalDetectionReport(validatedSignalList ? new JRMapCollectionDataSource(validatedSignalList) : null,
                notStartedReviewSignalList ? new JRMapCollectionDataSource(notStartedReviewSignalList) : null,
                pendingReviewList ? new JRMapCollectionDataSource(pendingReviewList) : null,
                closedReviewList ? new JRMapCollectionDataSource(closedReviewList) : null,
                signalData, reportParamsMap)
        renderReportOutputType(reportFile)
        signalAuditLogService.createAuditForExport(criteriaSheetList,ec.getInstanceIdentifierForAuditLog()+": Detection Summary", Constants.AuditLog.EVDAS_REVIEW + (ec.isLatest ? "" : ": Archived Alert"),params,reportFile.name)

    }

    private renderReportOutputType(File reportFile, String fileName = null) {
        String reportName = fileName ?: "EVDAS Alert" + DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss").print(new DateTime())
        response.contentType = "${dynamicReportService.getContentType(params.outputFormat)}; charset=UTF-8"
        response.contentLength = reportFile.size()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.$params.outputFormat" + "\"")
        response.getOutputStream().write(reportFile.bytes)
        response.outputStream.flush()
        params?.reportName = "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.$params.outputFormat"
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def upload() {
        def alertId = params?.alertId
        def domain = params.boolean('isArchived') ? ArchivedEvdasAlert : EvdasAlert
        def evdasAlert = domain.findById(alertId.toInteger())
        def fileName = params?.attachments.filename
        if (fileName instanceof String) {
            fileName = [fileName]
        }
        params?.isAlertDomain=true
        User currentUser = userService.getUser()
        Map filesStatusMap = attachmentableService.attachUploadFileTo(currentUser, fileName, evdasAlert, request)
        String fileDescription = params.description
        List<Attachment> attachments = evdasAlert.getAttachments().sort { it.dateCreated }
        if (attachments) {
            List<Integer> bulkAttachmentIndex = 1..filesStatusMap?.uploadedFiles?.size()
            bulkAttachmentIndex.each {
                Attachment attachment = attachments[-it]
                AttachmentDescription attachmentDescription = new AttachmentDescription()
                attachmentDescription.attachment = attachment
                attachmentDescription.createdBy = currentUser.fullName
                attachmentDescription.description = fileDescription
                attachmentDescription.skipAudit=true
                attachmentDescription.save(flush: true)
            }
        }
        if (filesStatusMap?.uploadedFiles) {
            activityService.createActivityForEvdas(evdasAlert.executedAlertConfiguration, ActivityType.findByValue(ActivityTypeValue.AttachmentAdded),
                    userService.getUser(), "Attachment " + filesStatusMap?.uploadedFiles*.originalFilename + " is added", null,
                    [product: getNameFieldFromJson(evdasAlert.alertConfiguration.productSelection), event: getNameFieldFromJson(evdasAlert.alertConfiguration.eventSelection)],
                    evdasAlert.productName, evdasAlert.pt, evdasAlert.assignedTo, null, evdasAlert.assignedToGroup)
        }
        render(['success': true] as JSON)
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def showCharts(Long alertId, Boolean isArchived) {
        int prevCounts = Holders.config.previous.alerts.count.evdas.charts

        List evCount = []
        List litCount = []
        List seriousCount = []
        List fatalCount = []
        List prrValue = []
        List rorValue = []
        List xAxisTitle = []
        String frequency = Constants.Commons.BLANK_STRING
        def domain = evdasAlertService.getDomainObject(isArchived)
        def ea = domain.get(alertId)
        if (ea) {
            EvdasConfiguration configuration = ea.alertConfiguration
            String substance = ea.substance
            String pt = ea.pt
            List<ExecutedEvdasConfiguration> prevExecs = evdasAlertService.getValidPrevExecConfigurations(configuration)
            SubstanceFrequency substanceFrequency = SubstanceFrequency.findByNameIlike(substance)
            if (substanceFrequency) {
                frequency = substanceFrequency.uploadFrequency
            } else {
                frequency = getRecurrencePattern(prevExecs[0].scheduleDateJSON)
            }

            prevExecs = prevExecs?.unique {
                frequency.equals("HOURLY") ? it.dateRangeInformation?.getReportStartAndEndDate()[1] :  DateUtils.truncate(it.dateRangeInformation?.getReportStartAndEndDate()[1], Calendar.DAY_OF_MONTH)
            }?.sort {
                frequency.equals("HOURLY") ? it.dateRangeInformation?.getReportStartAndEndDate()[1] :  DateUtils.truncate(it.dateRangeInformation?.getReportStartAndEndDate()[1], Calendar.DAY_OF_MONTH)
            }?.takeRight(6)

            //TODO check for previous alert to get from archive
            List prevAlertList = []
            prevAlertList.add(ArchivedEvdasAlert.findAllByExecutedAlertConfigurationInListAndSubstanceAndPt(prevExecs, substance, pt))
            prevAlertList.add(EvdasAlert.findByExecutedAlertConfigurationAndSubstanceAndPt(prevExecs?.first(), substance, pt))
            prevAlertList?.flatten().sort { it?.periodEndDate }.each { prevAlert ->
                evCount.add(prevAlert?.newEv ?: 0)
                litCount.add(prevAlert?.newLit ?: '0')
                seriousCount.add(prevAlert?.newSerious ?: 0)
                fatalCount.add(prevAlert?.newFatal ?: 0)
                rorValue.add(prevAlert ? prevAlert.rorValue : 0)
                if (frequency.equals("HOURLY"))
                    xAxisTitle.add(DateUtil.toDateStringPattern(prevAlert?.periodEndDate, "dd-MMM-yy HH:mm:ss"))
                else
                    xAxisTitle.add(DateUtil.toDateStringPattern(prevAlert?.periodEndDate, "dd-MMM-yy"))
            }
        }
        litCount = litCount.collect {
            Integer.parseInt(it)
        }
        render(["evCount" : evCount, "litCount": litCount, "seriousCount": seriousCount, "fatalCount": fatalCount,
                "prrValue": prrValue, "rorValue": rorValue, "xAxisTitle": xAxisTitle, "frequency": frequency] as JSON)
    }


    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def fetchAttachment(final Long alertId, Boolean isArchived) {
        def attachments = []
        List<Long> evdAlertList = evdasAlertService.getAlertIdsForAttachments(alertId, isArchived)
        String timezone = userService.user.preference.timeZone
        evdAlertList.each { Long evdAlertId ->
            def evdAlert = ArchivedEvdasAlert.get(evdAlertId) ?: EvdasAlert.get(evdAlertId)
            attachments += evdAlert.attachments.collect {
                [
                        id         : it.id,
                        name       : it.inputName ?: it.name,
                        description: AttachmentDescription.findByAttachment(it)?.description,
                        timeStamp  : DateUtil.stringFromDate(it.dateCreated, DateUtil.DEFAULT_DATE_TIME_FORMAT, timezone),
                        modifiedBy : AttachmentDescription.findByAttachment(it)?.createdBy
                ]
            }
        }
        respond attachments.unique(), [formats: ['json']]
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def deleteAttachment(Long attachmentId, Long alertId, Boolean isArchived) {
        def domain = evdasAlertService.getDomainObject(isArchived)
        Attachment attachment = Attachment.findById(attachmentId)
        String fileName = attachment.inputName
        def extIndex=attachment.getFilename().lastIndexOf(".")
        String extension=attachment.getFilename().substring(extIndex)
        if(!fileName.contains(extension)){
            fileName=fileName+extension
        }
        if (fileName instanceof String) {
            fileName = [fileName]
        }
        def evdasAlert = domain.get(alertId)
        if (attachment) {
            if (AttachmentDescription.findByAttachment(attachment)) {
                AttachmentDescription.findByAttachment(attachment).delete()
            }
            attachmentableService.removeAttachment(attachmentId)
            activityService.createActivityForEvdas(evdasAlert.executedAlertConfiguration, ActivityType.findByValue(ActivityTypeValue.AttachmentRemoved),
                    userService.getUser(), "Attachment " + fileName + " is removed", null,
                    [product: getNameFieldFromJson(evdasAlert.alertConfiguration.productSelection), event: getNameFieldFromJson(evdasAlert.alertConfiguration.eventSelection)],
                    evdasAlert.productName, evdasAlert.pt, evdasAlert.assignedTo, null, evdasAlert.assignedToGroup)
        }
        render(['success': true] as JSON)
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def fetchSubstanceFrequencyProperties(String substanceName, Boolean isAdhocRun) {
        SubstanceFrequency frequency = SubstanceFrequency.findByNameIlikeAndAlertType(substanceName, Constants.AlertConfigType.EVDAS_ALERT)
        Map properties = [:]
        if (frequency) {
            properties.miningFrequency = frequency.miningFrequency
            properties.frequencyName = frequency.frequencyName
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

    def fetchDateRangeMap(String frequency) {
        SubstanceFrequency substanceFrequency = SubstanceFrequency.findByFrequencyName(frequency)
        def dateRangeMap = [:]
        if (substanceFrequency) {
            List probableDateRange = evdasAlertService.populatePossibleDateRanges(substanceFrequency.startDate, substanceFrequency.miningFrequency)
            probableDateRange.reverse(true)
            def dateRangeList = []
            def timezone = Holders.config.server.timezone
            probableDateRange.each {
                dateRangeList << Date.parse('dd-MMM-yyyy', it[1])
            }
            def endDateList = ExecutedEvdasConfiguration.findAllByFrequencyAndAdhocRun(frequency, false).collect {
                it.dateRangeInformation.dateRangeEndAbsolute
            }
            def isLatestExec = 0
            dateRangeList.eachWithIndex { Date date, int i ->
                if (endDateList.contains(date)) {
                    isLatestExec++
                    if (isLatestExec == 1) {
                        def recentDateRange = [
                                "startDate": probableDateRange[i][0],
                                "endDate"  : probableDateRange[i][1]
                        ]
                        def exeRecent = "exeRecent"
                        dateRangeMap[exeRecent] = recentDateRange
                    } else if (isLatestExec < 7) {
                        def dateRangeInfo = [
                                "startDate": probableDateRange[i][0],
                                "endDate"  : probableDateRange[i][1]
                        ]
                        def exeName = "exe" + (isLatestExec - 2)
                        dateRangeMap[exeName] = dateRangeInfo
                    }
                }
            }
        }
        dateRangeMap
    }

    def fetchSubstanceFreqNames() {
        def ecFreqList = ExecutedEvdasConfiguration.findAllByFrequencyIsNotNull().collect { it.frequency }
        def freqNames = ecFreqList.unique()
        def frequencyList = SubstanceFrequency.findAllByAlertType("EVDAS Alert")
        frequencyList.each {
            if (!freqNames.contains(it.frequencyName)) {
                freqNames.add(it.frequencyName)
            }
        }
        freqNames
    }


    def fetchAlertIdFreq(String frequency) {
        def alertList = []
        def substancePtList = []
        def evdasList = EvdasAlert.findAllByFrequencyAndAdhocRun(frequency, false)
        evdasList.each {
            def substancePtMap = [
                    'id'               : it.id,
                    'ACTIVE_SUBSTANCES': it.getAttr('ACTIVE_SUBSTANCES'),
                    'PTS'              : it.getAttr('PTS')
            ]
            substancePtList << substancePtMap
        }
        def finalMap = substancePtList.groupBy({ it['ACTIVE_SUBSTANCES'] }, { it['PTS'] })
        finalMap.each { substance, substanceMap ->
            substanceMap.each { pt, ptMap ->
                ptMap.eachWithIndex { idMap, index ->
                    if (index == 0) {
                        alertList << idMap['id']
                    }
                }
            }
        }
        alertList
    }

    def fetchDateRangeListOfExec(String frequency) {
        SubstanceFrequency substanceFrequency = SubstanceFrequency.findByFrequencyName(frequency)
        def dateRangeMap = [:]
        if (substanceFrequency) {
            List probableDateRange = evdasAlertService.populatePossibleDateRanges(substanceFrequency.startDate, substanceFrequency.miningFrequency)
            def dateRangeList = []
            probableDateRange.reverse(true)
            probableDateRange.each {
                dateRangeList << Date.parse('dd-MMM-yyyy', it[1])
            }
            def endDateList = ExecutedEvdasConfiguration.findAllByFrequency(frequency).collect {
                it.dateRangeInformation.dateRangeEndAbsolute
            }
            def isLatestExec = 0
            dateRangeList.eachWithIndex { Date date, int i ->
                if (endDateList.contains(date)) {
                    isLatestExec++
                    if (isLatestExec == 1) {
                        def recentDateRange = [
                                "startDate": Date.parse('dd-MMM-yyyy', probableDateRange[i][0]),
                                "endDate"  : Date.parse('dd-MMM-yyyy', probableDateRange[i][1])
                        ]
                        def exeRecent = "exeRecent"
                        dateRangeMap[exeRecent] = recentDateRange
                    } else if (isLatestExec < 7) {
                        def dateRangeInfo = [
                                "startDate": Date.parse('dd-MMM-yyyy', probableDateRange[i][0]),
                                "endDate"  : Date.parse('dd-MMM-yyyy', probableDateRange[i][1])
                        ]
                        def exeName = "exe" + (isLatestExec - 2)
                        dateRangeMap[exeName] = dateRangeInfo
                    }
                }
            }
        }
        dateRangeMap
    }

    private List getFiltersFromParams(Boolean isFilterRequest, def params) {
        List filters = []
        def escapedFilters = null
        if (params.filters) {
            def slurper = new JsonSlurper()
            escapedFilters = slurper.parseText(params.filters)
        }
        if(escapedFilters) {
            filters = new ArrayList(escapedFilters)
        }
        if (!isFilterRequest) {
            filters = Disposition.findAllByClosedAndReviewCompleted(false , false).collect { it.displayName }
        }
        filters
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def fetchCaseDrillDownData(String substance, String pt, Integer flagVar, Boolean isStartDate, Integer alertId, Integer numberOfCount,Boolean async) {
        Date startDate
        Date endDate
        String sessionId = request.getSession().getId()
        ExecutedEvdasConfiguration exEvdasConfig = ExecutedEvdasConfiguration.get(params.id)
        EvdasConfiguration evdasConfig = EvdasConfiguration.findByName(exEvdasConfig?.name)
        cacheService.clearEvdasDrillDownData(sessionId)
        cacheService.clearEvdasAlertId(sessionId)
        def dateRange = exEvdasConfig?.dateRangeInformation?.getReportStartAndEndDate()
        if (evdasConfig.dateRangeInformation.dateRangeEnum == evdasConfig.dateRangeInformation.dateRangeEnum.CUMULATIVE) {
            def dateRangeEnd = exEvdasConfig?.dateCreated
            endDate = dateRangeEnd
            startDate = dateRange[0]
        } else {
            startDate = dateRange[0]
            endDate = dateRange[1]
        }

        String productGroupId = evdasConfig.getProductGroupList()
        def data
        if(async){
            data = evdasAlertService.caseDrillDownAsync(startDate, endDate, substance, pt, flagVar, isStartDate, evdasConfig.name, alertId,productGroupId ? productGroupId as Long:null, numberOfCount)
        }else{
            data = evdasAlertService.caseDrillDown(startDate, endDate, substance, pt, flagVar, isStartDate, evdasConfig.name, alertId,productGroupId ? productGroupId as Long:null)
        }
        cacheService.setEvdasDrillDownData(data, sessionId)
        cacheService.setEvdasAlertId(alertId as Long, sessionId)
        render data as JSON
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def exportEVDASCaseListColumns(){
        String sessionId = request.getSession().getId()
        List<Map> filteredDrillDownData = cacheService.getEvdasDrillDownData(sessionId)
        Long evdasAlertId = cacheService.getEvdasAlertId(sessionId)
        EvdasAlert evdasAlert=EvdasAlert.get(evdasAlertId)
        filteredDrillDownData = evdasAlertService.getFinalDrillDownData(filteredDrillDownData, params)
        StringBuilder evdasEntityValue = new StringBuilder()
        params.criteriaSheetList = evdasAlertService.createCriteriaSheetListDrillDown(params,evdasAlertId,false, evdasEntityValue)
        def reportFile = dynamicReportService.createEVDASDrillDownReport(new JRMapCollectionDataSource(filteredDrillDownData), params)
        renderReportOutputType(reportFile, evdasAlertService.getCaseListFileName(evdasAlertId, params.boolean('isArchived'),params.alertType))
        signalAuditLogService.createAuditForExport(params.criteriaSheetList,evdasEntityValue.toString(), Constants.AuditLog.EVDAS_REVIEW,params,reportFile.name)
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def attachCaseListingFile(){
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try{
            String sessionId = request.getSession().getId()
            params.alertId = cacheService.getEvdasAlertId(sessionId)
            List<Map> drillDownData = cacheService.getEvdasDrillDownData(sessionId)
            drillDownData = evdasAlertService.getFinalDrillDownData(drillDownData, params)
            evdasAlertService.attachCaseListingFile(drillDownData, params, request)
        } catch(Exception ex){
            log.error(ex.printStackTrace())
            responseDTO.status = false
            responseDTO.message = message(code: "evdas.attachment.save.failed")
        }
        render(responseDTO as JSON)
    }

    def fetchFreqName(String freq) {
        SubstanceFrequency substanceFrequency = SubstanceFrequency.findByFrequencyNameAndAlertType(freq, Constants.AlertConfigType.EVDAS_ALERT)
        render(["frequency": substanceFrequency?.miningFrequency] as JSON)
    }


    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER'])
    def changeDisposition(String selectedRows, Disposition targetDisposition, String justification, String validatedSignalName, String productJson, Boolean isArchived,Long signalId) {
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
            validatedSignalName = validatedSignalName ? org.apache.commons.lang.StringEscapeUtils.unescapeHtml(validatedSignalName) : Constants.Commons.BLANK_STRING
            List<Long> evdasAlertIdList = JSON.parse(selectedRows).collect { Map<String, Long> selectedRow ->
                selectedRow["alert.id"] as Long
            }
            responseDTO.data =[:]
            responseDTO.data << evdasAlertService.changeDisposition(evdasAlertIdList, targetDisposition, justification, validatedSignalName, productJson, isArchived,signalId)
            def domain = evdasAlertService.getDomainObject(isArchived)
            Long configId = domain?.get(evdasAlertIdList[0])?.executedAlertConfiguration?.id
            Long countOfPreviousDisposition
            if(params.callingScreen != Constants.Commons.DASHBOARD){
                countOfPreviousDisposition = alertService.fetchPreviousDispositionCount(configId, params.incomingDisposition, domain)
            }else{
                countOfPreviousDisposition = alertService.fetchPreviousDispositionCountDashboard(params.incomingDisposition, domain)
            }
            responseDTO.data << [incomingDisposition:params.incomingDisposition, countOfPreviousDisposition:countOfPreviousDisposition]
            if (targetDisposition?.id) {
                evdasAlertService.persistEvdasDueDate(responseDTO?.data?.alertDueDateList)
            }
        } catch (grails.validation.ValidationException vx) {
            responseDTO.message = MiscUtil.getCustomErrorMessageList(vx)[0]
            responseDTO.status = false
            log.error("Exception is : ${vx}")
        } catch (Exception e) {
            e.printStackTrace()
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.disposition.change.error")
        }
        render(responseDTO as JSON)
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER'])
    def revertDisposition(Long id, String justification) {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            responseDTO.data = evdasAlertService.revertDisposition(id, justification)
            Long configId = EvdasAlert.get(id)?.executedAlertConfiguration?.id
            Long countOfPreviousDisposition
            if (params.callingScreen != Constants.Commons.DASHBOARD) {
                countOfPreviousDisposition = alertService.fetchPreviousDispositionCount(configId, responseDTO?.data?.prevDisposition, EvdasAlert)
            } else {
                countOfPreviousDisposition = alertService.fetchPreviousDispositionCountDashboard(responseDTO?.data?.prevDisposition, EvdasAlert)
            }
            responseDTO.data << [incomingDisposition: responseDTO?.data?.prevDisposition, targetDisposition: responseDTO?.data?.targetDisposition, countOfPreviousDisposition: countOfPreviousDisposition]
            evdasAlertService.persistEvdasDueDate(responseDTO?.data?.alertDueDateList)
            if(!responseDTO.data.dispositionReverted){
                responseDTO.status = false
                responseDTO.message = message(code: "app.label.undo.disposition.change.error.refresh")
            }
        }catch (grails.validation.ValidationException vx) {
            responseDTO.message = MiscUtil.getCustomErrorMessage(vx)
            responseDTO.status = false
            log.error("Exception is : ${vx}")
        }
        catch (Exception e) {
            e.printStackTrace()
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.undo.disposition.change.error")
        }
        render(responseDTO as JSON)
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def archivedAlert(Long id) {
        Map archivedAlertList = evdasAlertService.archivedAlertsList(id,params)
        def orderColumn = params["order[0][column]"]
        def orderColumnMap = [name: params["columns[${orderColumn}][data]"], dir: params["order[0][dir]"]]
        String sortedColumn = orderColumnMap?.name
        if (orderColumn != null && sortedColumn != "version") {
            archivedAlertList.aaData?.sort { Map config1, Map config2 ->
                def val1 = config1[sortedColumn]
                def val2 = config2[sortedColumn]
                if (sortedColumn in ["alertName", "product","description","dateRange","lastModified"]) {
                    if (orderColumnMap?.dir == Constants.DESCENDING_ORDER) {
                        return val2?.toLowerCase() <=> val1?.toLowerCase()
                    } else {
                        return val1?.toLowerCase() <=> val2?.toLowerCase()
                    }
                } else {
                    if (orderColumnMap?.dir == Constants.DESCENDING_ORDER) {
                        return val2 <=> val1
                    } else {
                        return val1 <=> val2
                    }
                }

            }
        }
        render(archivedAlertList as JSON)
    }

    Map modelData(EvdasConfiguration evdasConfig, String action) {
        List<Priority> priorityList = Priority.findAllByDisplay(true)
        def byDefaultPriority = Priority.findByDefaultPriority(true)?.id ?: null
        boolean hasNormalAlertExecutionAccess = userService.hasNormalAlertExecutionAccess(Constants.AlertConfigType.EVDAS_ALERT)
        List<User> userList = User.findAllByEnabled(true).sort {
            it.fullName?.toLowerCase()
        }
        [action: action, userList: userList, priorityList: priorityList, configurationInstance: evdasConfig, byDefaultPriority: byDefaultPriority, hasNormalAlertExecutionAccess: hasNormalAlertExecutionAccess]
    }

    def fetchPossibleValues(Long executedConfigId) {
        Map<String, List> possibleValuesMap = [:]
        alertService.preparePossibleValuesMap(EvdasAlert, possibleValuesMap, executedConfigId)
        List<String> yesNoFieldsList = ["sdr", "sdrPaed", "sdrGeratr"]
        List<Map<String, String>> yesNoMapList = [[id: "Yes", text: "Yes"], [id: "No", text: "No"]]
        yesNoFieldsList.each {
            possibleValuesMap.put(it, yesNoMapList)
        }
        possibleValuesMap.put("listedness", [[id: "true", text: "Yes"], [id: "false", text: "No"]])

        render possibleValuesMap as JSON
    }

    def fetchAllFieldValues() {
        List<Map> fieldList = grailsApplication.config.signal.evdasColumnList as List<Map>
        render fieldList as JSON
    }

    def changeAlertLevelDisposition(Disposition targetDisposition,String justificationText,ExecutedEvdasConfiguration execConfig, EvdasConfiguration config, Boolean isArchived){
        def domain = evdasAlertService.getDomainObject(isArchived)
        String alertName = execConfig?.name
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true, message: message(code: "alert.level.disposition.successfully.updated"))
        try {
            AlertLevelDispositionDTO alertLevelDispositionDTO = dispositionService.populateAlertLevelDispositionDTO(targetDisposition, justificationText, domain, execConfig, config)
            Integer updatedRowsCount = evdasAlertService.changeAlertLevelDisposition(alertLevelDispositionDTO, isArchived)
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
        }catch (grails.validation.ValidationException vx) {
            responseDTO.message = MiscUtil.getCustomErrorMessage(vx)
            responseDTO.status = false
            log.error("Exception is : ${vx}")
        }  catch (Exception ex) {
            log.error(ex.getMessage(), ex)
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.disposition.change.error")
        }
        render(responseDTO as JSON)
    }

    def getSharedWithUserAndGroups() {
        if (params.id) {
            ExecutedEvdasConfiguration executedConfiguration = ExecutedEvdasConfiguration.get(params.id)
            if (executedConfiguration) {
                EvdasConfiguration config = EvdasConfiguration.findByName(executedConfiguration.name)
                List<Map> users = config.getShareWithUsers()?.collect{[id: Constants.USER_TOKEN + it.id, name: it.fullName]}
                List<Map>  groups= config.getShareWithGroups()?.collect{[id: Constants.USER_GROUP_TOKEN + it.id, name: it.name]}
                Map result =[users: users, groups: groups, all: users + groups]
                render result as JSON
            } else {
                log.info("Not valid config id.")
            }
        }
    }

    @Secured(['ROLE_SHARE_GROUP','ROLE_SHARE_ALL'])
    def editShare() {
        Boolean sharedWithoutException = true
        ExecutedEvdasConfiguration executedConfiguration
        if (params.sharedWith && params.executedConfigId) {
            executedConfiguration = ExecutedEvdasConfiguration.get(Long.parseLong(params.executedConfigId))
            EvdasConfiguration config = EvdasConfiguration.findByName(executedConfiguration.name)
            userService.bindSharedWithConfiguration(config, params.sharedWith, true)
            CRUDService.update(config)
        } else {
            sharedWithoutException = false
            log.info("No valid executed config id")
        }
        if(sharedWithoutException){
            flash.message = message(code: "app.configuration.share.success", args: [executedConfiguration?.name])
        }else{
            flash.warn = message(code: "app.configuration.share.warning", args: [executedConfiguration?.name])
        }
        redirect action: "review"
    }

    private boolean isExcelExportFormat(String outputFormat) {
        return outputFormat == ReportFormat.XLSX.name() ? true : false
    }

    def changeFilterAttributes(){
        HttpSession session = request.getSession()
        session.removeAttribute("selectedAlertsFilter")
        session.setAttribute("evdas",params["selectedAlertsFilter"])
        render([status:200] as JSON)
    }
}
