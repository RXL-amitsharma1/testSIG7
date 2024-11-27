package com.rxlogix

import com.rxlogix.attachments.Attachment
import com.rxlogix.audit.AuditTrail
import com.rxlogix.config.*
import com.rxlogix.controllers.AlertController
import com.rxlogix.dto.AlertDataDTO
import com.rxlogix.dto.AlertLevelDispositionDTO
import com.rxlogix.dto.DashboardCountDTO
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.JustificationFeatureEnum
import com.rxlogix.enums.ReportFormat
import com.rxlogix.mart.MartTags
import com.rxlogix.signal.*
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.FileNameCleaner
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.SignalQueryHelper
import grails.converters.JSON
import grails.events.EventPublisher
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import grails.validation.ValidationException
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.time.DateUtils
import org.apache.http.util.TextUtils
import org.grails.web.json.JSONObject
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import javax.servlet.http.HttpSession
import java.text.ParseException

import static com.rxlogix.Constants.AlertConfigType.SINGLE_CASE_ALERT
import static com.rxlogix.util.DateUtil.toDateString
import static com.rxlogix.util.DateUtil.toDateString1
import static com.rxlogix.util.MiscUtil.calcDueDate
import static org.springframework.http.HttpStatus.*

@Secured(["isAuthenticated()"])
class SingleCaseAlertController implements AlertController, EventPublisher {
    def emailService
    def configurationService
    def singleCaseAlertService
    def SpringSecurityService
    def CRUDService
    def reportExecutorService
    def activityService
    def dynamicReportService
    def queryService
    def caseHistoryService
    def productBasedSecurityService
    def validatedSignalService
    def medicalConceptsService
    def actionTemplateService
    def userService
    def attachmentableService
    def caseInfoService
    def viewInstanceService
    def aggregateCaseAlertService
    def reportIntegrationService
    def cacheService
    def productGroupService
    def userGroupService
    def workflowRuleService
    def priorityService
    def safetyLeadSecurityService
    def reportFieldService
    def alertService
    def dispositionService
    def dataObjectService
    def spotfireService
    def actionService
    def alertCommentService
    def advancedFilterService
    def singleAlertTagService
    EmailNotificationService emailNotificationService
    def pvsGlobalTagService
    def caseFormService
    def alertAdministrationService
    def messageSource
    def signalAuditLogService
    public final static String JSON_DATE = "yyyy-MM-dd'T'HH:mmXXX"
    def caseNarrativeConfigurationService
    ImportConfigurationService importConfigurationService

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION'])
    def create() {
        Configuration configurationInstance = new Configuration(type: SINGLE_CASE_ALERT, adhocRun: true)
        String action = Constants.AlertActions.CREATE
        Map model = modelData(configurationInstance, action)
        model << [clone:false]
        model << [isPVCM: dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM)]
        render(view: "create", model: model)
    }

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_SINGLE_CASE_REVIEWER', 'ROLE_SINGLE_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def index() {
        render(view: 'index')
    }

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION'])
    def view(Configuration configuration) {

        if (!configuration) {
            notFound()
            return
        }

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
        // map for reportField Values
        def map = [:]
        configuration?.alertQueryValueLists?.each{
            it?.parameterValues?.each{
                // change method incase of japan
                map.put(it?.reportField?.name, cacheService.getRptToUiLabelInfoPvrEn(it?.reportField?.name))
            }
        }
        render(view: "view", model: [configurationInstance: configuration, currentUser: currentUser, isExecuted: false,
                                     templateQueries      : configuration.templateQueries,
                                     viewSql              : params.getBoolean("viewSql") ? reportExecutorService.debugReportSQL(configuration,executedConfiguration, false) : null,
                                     isEdit               : SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN") || userService.getCurrentUserId() == configuration.owner.id,
                                     selectedCaseSeries   : configuration.alertCaseSeriesName,
                                     rptUiLabelMap        : map, isPVCM: dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM)])
    }

    @Secured(['ROLE_SINGLE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION','ROLE_AGGREGATE_CASE_VIEWER'])
    def viewExecutedConfig(ExecutedConfiguration executedConfiguration) {
        if (!executedConfiguration) {
            notFound()
            return
        }
        User currentUser = userService.getUser()
        render(view: "view", model: [isExecuted     : true, configurationInstance: executedConfiguration, currentUser: currentUser,
                                     templateQueries: executedConfiguration.executedTemplateQueries,selectedCaseSeries:executedConfiguration.alertCaseSeriesName, isPVCM: dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM)])
    }

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION'])
    def copy(Configuration originalConfig) {
        if (!originalConfig) {
            notFound()
            return
        }
        String action = Constants.AlertActions.COPY
        Map model = modelData(originalConfig, action)
        model << [clone:true, currentUser: userService.getUser()]
        render(view: "create", model: model)
    }

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION'])
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

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION'])
    def edit(Configuration configurationInstance) {

        if (!configurationInstance) {
            notFound()
            return
        }

        def firstExecutionDate = '-'
        def lastExecutionDate = '-'
        Date configNextRunDate = configurationInstance.nextRunDate
        User currentUser = userService.getUser()
        String timeZone = currentUser.preference.timeZone
        List<ExecutedConfiguration> executedConfiguration = ExecutedConfiguration.findAllByNameAndOwner(configurationInstance.name, configurationInstance.owner)

        try {
            if (executedConfiguration) {
                def length = executedConfiguration.size()
                def firstExecutionObject = executedConfiguration[0]
                def lastExecutionObject = executedConfiguration[length - 1]
                def fExecutionStartDate = firstExecutionObject.executedAlertDateRangeInformation.dateRangeStartAbsolute
                def fExecutionEndDate = firstExecutionObject.executedAlertDateRangeInformation.dateRangeEndAbsolute
                def lExecutionStartDate = lastExecutionObject.executedAlertDateRangeInformation.dateRangeStartAbsolute
                def lExecutionEndDate = lastExecutionObject.executedAlertDateRangeInformation.dateRangeEndAbsolute
                def firstExecutionStartDate = DateUtil.toDateStringWithoutTimezone(fExecutionStartDate)
                def firstExecutionEndDate = DateUtil.toDateStringWithoutTimezone(fExecutionEndDate)
                def lastExecutionStartDate = DateUtil.toDateStringWithoutTimezone(lExecutionStartDate)
                def lastExecutionEndDate = DateUtil.toDateStringWithoutTimezone(lExecutionEndDate)
                firstExecutionDate = firstExecutionStartDate + "-" + firstExecutionEndDate
                lastExecutionDate = lastExecutionStartDate + "-" + lastExecutionEndDate
            }
        } catch (Throwable th) {
            flash.warn = message(code: "app.alert.view")
            log.error(th.getMessage())
        }

        if (reportExecutorService.currentlyRunning.contains(configurationInstance.id)) {
            flash.warn = message(code: "app.configuration.running.fail", args: [configurationInstance.name])
            redirect(action: "index")
        }

        if (!(configurationInstance?.isEditableBy(currentUser) ||
                SpringSecurityUtils.ifAllGranted("ROLE_SINGLE_CASE_CONFIGURATION, ROLE_EXECUTE_SHARED_ALERTS"))) {
            flash.warn = message(code: "app.configuration.edit.permission", args: [configurationInstance.name])
            redirect(controller: "configuration", action: "index")
        } else {
            String action = Constants.AlertActions.EDIT
            Map model = modelData(configurationInstance, action)
            model << [configSelectedTimeZone: params?.configSelectedTimeZone, templateId: 32,
                      configNextRunDate     : configNextRunDate, firstExecutionDate: firstExecutionDate, lastExecutionDate: lastExecutionDate,
                      onlyShareAccess       : configurationInstance.owner.workflowGroup != currentUser.workflowGroup, isPVCM: dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM)]
            render(view: "edit", model: model)
        }
    }

    def getAllEmailsUnique(Configuration configurationInstance) {
        userService.getAllEmails(userService.getUser())
    }

    def validateTriggerParams(triggerParams) {
        try {
            Integer.parseInt(triggerParams)
        } catch (NumberFormatException nfe) {
            return false
        } catch (Exception ex) {
            return false
        }
        return true
    }

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION'])
    def save() {

        Configuration configurationInstance = new Configuration(type: SINGLE_CASE_ALERT)

        if (!validateTriggerParams(params.alertTriggerDays)) {
            flash.error = message(code: "app.label.threshold.trigger.days.invalid")
            renderOnErrorScenario(configurationInstance)
            return
        }

        if (!validateTriggerParams(params.alertTriggerCases)) {
            flash.error = message(code: "app.label.threshold.trigger.cases.invalid")
            renderOnErrorScenario(configurationInstance)
            return
        }
        try {
            configurationInstance.isResume = false
            configurationInstance.setIsEnabled(false)

            populateModel(configurationInstance)
            Map productMap = [product: params.productSelection, productGroup: params.productGroupSelection]
            configurationInstance = userService.assignGroupOrAssignTo(params.assignedToValue, configurationInstance, productMap)
            userService.bindSharedWithConfiguration(configurationInstance, params.sharedWith, false, false, productMap)
            setTemplateToAlert(configurationInstance)

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
            if (!isProductOrFamilyOrIngredient) {
                flash.error = message(code: "app.label.product.family.error.message")
                configurationInstance.nextRunDate = null
                renderOnErrorScenario(configurationInstance)
                return
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

            //Set the workflow group from the logged in user.
            configurationInstance.workflowGroup = userService.getUser().workflowGroup

            configurationInstance = (Configuration) CRUDService.saveWithAuditLog(configurationInstance)
            if (!TextUtils.isEmpty(params.signalId)) {
                cacheService.setSignalAndConfigurationId(params.signalId,configurationInstance.id)
            }
        } catch (ValidationException ve) {
            ve.printStackTrace()
            configurationInstance.errors = ve.errors
            log.error(ve.printStackTrace())
            renderOnErrorScenario(configurationInstance)
            return
        } catch (Exception exception) {
            exception.printStackTrace()
            log.error(exception.printStackTrace())
            flash.error = message(code: "app.label.alert.error")
            renderOnErrorScenario(configurationInstance)
            return
        }

        request.withFormat {
            form {
                flash.message = message(code: 'default.created.message', args: [message(code: 'app.label.qualitative.alert.configuration'), configurationInstance.name])
                redirect(action: "view", id: configurationInstance.id)
            }
            '*' { respond configurationInstance, [status: CREATED] }
        }
    }

    private setNextRunDateAndScheduleDateJSON(Configuration configurationInstance) {
        try {
            if (configurationInstance.scheduleDateJSON && configurationInstance.isEnabled) {
                configurationInstance.nextRunDate = configurationService.getNextDate(configurationInstance)
            } else {
                configurationInstance.nextRunDate = null
            }
        }catch(Exception e){
            configurationInstance.scheduleDateJSON = null
        }
    }

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION'])
    def run() {

        Configuration configurationInstance
        def exiting_config = false
        if (params.id && params.previousAction != Constants.AlertActions.COPY) {
            configurationInstance = Configuration.get(params.id)
            exiting_config = true
        } else {
            configurationInstance = new Configuration(type: SINGLE_CASE_ALERT)
        }

        configurationInstance.isResume = false
        configurationInstance.setIsEnabled(true)

        if(!params.excludeNonValidCases)
            configurationInstance.excludeNonValidCases = false

        if (params.repeatExecution) {
            configurationInstance.repeatExecution = params.repeatExecution
        }

        if(!params.containsKey("name")) {
            flash.error = message(code: "app.label.alert.error")
            renderOnErrorScenario(configurationInstance)
            return
        }

        if (!validateTriggerParams(params.alertTriggerDays)) {
            flash.error = message(code: "app.label.threshold.trigger.days.invalid")
            renderOnErrorScenario(configurationInstance)
            return
        }

        if (!validateTriggerParams(params.alertTriggerCases)) {
            flash.error = message(code: "app.label.threshold.trigger.cases.invalid")
            renderOnErrorScenario(configurationInstance)
            return
        }
        try {

            populateModel(configurationInstance)
            Map productMap = [product: params.productSelection, productGroup: params.productGroupSelection]
            configurationInstance = userService.assignGroupOrAssignTo(params.assignedToValue, configurationInstance, productMap)
            userService.bindSharedWithConfiguration(configurationInstance, params.sharedWith, exiting_config, false, productMap)
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

            def isProductOrFamily = allowedDictionarySelection(configurationInstance)

            if (!isProductOrFamily) {
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
            }catch(Exception e){
                configurationInstance.scheduleDateJSON = null
            }
            //Set the workflow group from the logged in user.
            configurationInstance.workflowGroup = userService.getUser().workflowGroup

            if(configurationInstance.adhocRun){
                configurationInstance.nextRunDate = new Date()
            }
            if (exiting_config) {
                configurationInstance = (Configuration) CRUDService.updateWithAuditLog(configurationInstance)
                def exConfig = ExecutedConfiguration.findByConfigId(configurationInstance?.id)
                if (params.name && !exConfig?.name.equals(params.name)) {
                    alertService.renameExecConfig(configurationInstance.id, configurationInstance.name, params.name, configurationInstance.owner.id, SINGLE_CASE_ALERT)
                }
            } else {
                configurationInstance = (Configuration) CRUDService.saveWithAuditLog(configurationInstance)
            }
            if (!TextUtils.isEmpty(params.signalId)) {
                cacheService.setSignalAndConfigurationId(params.signalId,configurationInstance.id)
            }
        } catch (ValidationException ve) {
            ve.printStackTrace()
            log.error(ve.printStackTrace())
            if (Boolean.parseBoolean(params.repeatExecution)) {
                configurationInstance.setIsEnabled(true)
            } else {
                configurationInstance.setIsEnabled(false)
            }
            configurationInstance.errors = ve.errors
            renderOnErrorScenario(configurationInstance)
            return
        } catch (Exception exception) {
            exception.printStackTrace()
            log.error("Some error occurred", exception.printStackTrace())
            flash.error = message(code: "app.label.alert.error")
            renderOnErrorScenario(configurationInstance)
            return
        }

        boolean precheckEnabled = Holders.config.system.precheck.configuration.enable
        if(precheckEnabled && !alertService.isPreCheckVerified(AlertType.SINGLE_CASE_ALERT,configurationInstance)){
            flash.message = message(code: 'default.precheck.scheduled.message', args: [message(code: 'app.label.qualitative.alert.configuration'), configurationInstance.name])
        }else{
            flash.message = message(code: 'app.Configuration.RunningMessage',
                    args: [message(code: 'configuration.label'), configurationInstance.name])
        }
        request.withFormat {
            form {
                flash.message = flash.message
                redirect(controller: "configuration", action: "executionStatus",params: [alertType: AlertType.SINGLE_CASE_ALERT])
            }
            '*' { respond configurationInstance, [status: CREATED] }
        }
    }

    private void renderOnErrorScenario(Configuration configuration) {
        String action = params.previousAction
        Map model = modelData(configuration, action)
        model.signalId = params.signalId ?: null
        if (params.id && params.previousAction != Constants.AlertActions.COPY) {
            render view: "edit", model: model
        } else {
            render view: "create", model: model
        }
    }

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_EXECUTE_SHARED_ALERTS'])
    def runOnce() {
        Configuration configurationInstance = Configuration.get(params.id)
        if (!configurationInstance) {
            notFound()
            return
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
                }catch(Exception e) {
                    configurationInstance.scheduleDateJSON = null
                    configurationInstance = (Configuration) CRUDService.updateWithAuditLog(configurationInstance)
                }
                setWhoDrug(configurationInstance)
            } catch (ValidationException ve) {
                configurationInstance.errors = ve.errors
                String action = params.previousAction
                Map model = modelData(configurationInstance, action)
                render view: "create", model: model
                return
            } catch (Exception exception) {
                flash.error = message(code: "app.label.alert.error")
                renderOnErrorScenario(configurationInstance)
                return
            }

            boolean precheckEnabled = Holders.config.system.precheck.configuration.enable
            if (precheckEnabled && !alertService.isPreCheckVerified(AlertType.SINGLE_CASE_ALERT, configurationInstance)) {
                flash.message = message(code: 'default.precheck.scheduled.message', args: [message(code: 'app.label.qualitative.alert.configuration'), configurationInstance.name])
            } else {
                flash.message = message(code: 'app.Configuration.RunningMessage')
            }

            request.withFormat {
                form {
                    flash.message =  flash.message
                    redirect(controller: "configuration", action: "executionStatus",params: [alertType: AlertType.SINGLE_CASE_ALERT])
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

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION'])
    def update() {
        Configuration configurationInstance = Configuration.lock(params.id)
        if (!configurationInstance) {
            notFound()
            return
        }

        if (!validateTriggerParams(params.alertTriggerDays)) {
            flash.error = message(code: "app.label.threshold.trigger.days.invalid")
            renderOnErrorScenario(configurationInstance)
            return
        }

        if (!validateTriggerParams(params.alertTriggerCases)) {
            flash.error = message(code: "app.label.threshold.trigger.cases.invalid")
            renderOnErrorScenario(configurationInstance)
            return
        }
        try {
            populateModel(configurationInstance)
            Map productMap = [product: params.productSelection, productGroup: params.productGroupSelection]
            configurationInstance = userService.assignGroupOrAssignTo(params.assignedToValue, configurationInstance, productMap)
            userService.bindSharedWithConfiguration(configurationInstance, params.sharedWith, true, false, productMap)
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

            def isProductOrFamily = allowedDictionarySelection(configurationInstance)

            if (!isProductOrFamily) {
                flash.error = message(code: "app.label.product.family.error.message")
                configurationInstance.nextRunDate = null
                renderOnErrorScenario(configurationInstance)
                return
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
            configurationInstance = (Configuration) CRUDService.updateWithAuditLog(configurationInstance)
            def exConfig = ExecutedConfiguration.findByConfigId(configurationInstance?.id)
            if (params.name && !exConfig?.name.equals(params.name)) {
                alertService.renameExecConfig(configurationInstance.id, configurationInstance.name, params.name, configurationInstance.owner.id, SINGLE_CASE_ALERT)
            }
        } catch (ValidationException ve) {
            configurationInstance.errors = ve.errors
            log.error(ve.printStackTrace())
            renderOnErrorScenario(configurationInstance)
            return
        } catch (Exception exception) {
            flash.error = message(code: "app.label.alert.error")
            log.error(exception.printStackTrace())
            renderOnErrorScenario(configurationInstance)
            return
        }
        request.withFormat {
            form {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'app.label.qualitative.alert.configuration'), configurationInstance.name])
                redirect(action: "view", id: configurationInstance.id)
            }
            '*' { respond configurationInstance, [status: OK] }
        }
    }

    def updateAndExecuteAlertBulk(){
        List configIdList = params.get("configIdList")?.split(",")
        def list = []
        List<Configuration> currentlyRunningConfigs = []
        def configToRemove = []
        configIdList.each{ configId ->
            List runningAlerts = configurationService.fetchRunningAlertList(Constants.ConfigurationType.QUAL_TYPE)
            if (runningAlerts?.contains(configId)) {
                currentlyRunningConfigs.add(Configuration.get(configId as Long))
            }
        }
        configIdList.removeAll(currentlyRunningConfigs*.id)
        configIdList.each {
            params.configId = it
            Configuration configurationInstance = Configuration.get(params.configId)
            if (!configurationInstance) {
                notFound()
                return
            }

            configurationInstance.setIsEnabled(true)
            configurationInstance.setIsResume(false)
            configurationInstance.setNextRunDate(null)
            setTemplateToAlert(configurationInstance)

            alertAdministrationService.nullifyAutoAdjustmentRelatedFlow(configurationInstance, configurationInstance.type)

            configurationInstance.evaluateDateAs = params.evaluateDateAs ?: EvaluateCaseDateEnum.LATEST_VERSION
            if (params.asOfVersionDate) {
                bindAsOfVersionDateOfSingleCase(configurationInstance)
            }

            AlertDateRangeInformation alertDateRangeInformation = configurationInstance.alertDateRangeInformation
            if (params.alertDateRangeInformation) {
                bindData(alertDateRangeInformation, params.alertDateRangeInformation, [exclude: ['dateRangeEndAbsolute', 'dateRangeStartAbsolute']])
                alertDateRangeInformation.alertConfiguration = configurationInstance
                setAlertDateRange(alertDateRangeInformation)
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
                configurationInstance = (Configuration) CRUDService.updateWithAuditLog(configurationInstance)
            } catch (ValidationException ve) {
                render ve.errors
                log.error(ve.printStackTrace())
                return
            }
        }
        if(currentlyRunningConfigs.size()>0){
            flash.warn = message(code: 'app.configuration.alert.running', args: [currentlyRunningConfigs*.name.join(",")])
            redirect(controller: "alertAdministration", action: "index")
        } else{
            redirect(controller: "configuration", action: "executionStatus",params: [alertType: AlertType.SINGLE_CASE_ALERT])
        }
    }

    def updateAndExecuteAlert(){
        Configuration configurationInstance = Configuration.get(params.configId)
        if (!configurationInstance) {
            notFound()
            return
        }
        List runningAlerts = configurationService.fetchRunningAlertList(Constants.ConfigurationType.QUAL_TYPE)
        if (runningAlerts?.contains(configurationInstance.id)) {
            // found alert running on given configID, return
            flash.error = message(code: 'app.configuration.alert.running', args: [configurationInstance.name])
            redirect(controller: "alertAdministration", action: "index")
            return
        }
        if(params.futureSchedule != null && params.scheduleDateJSON){
            Map scheduleDateJSONMap = JSON.parse(params.scheduleDateJSON) as Map
            Date startDate = Date.parse(JSON_DATE, scheduleDateJSONMap.startDateTime as String)
            if(startDate < (DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH))){
                flash.error = message(code: "app.label.past.date.error", args: ["Start Date"])
                redirect(controller: "alertAdministration", action: "index")
                return
            }
        }

        configurationInstance.setIsEnabled(true)
        configurationInstance.setIsResume(false)
        configurationInstance.setNextRunDate(null)
        setTemplateToAlert(configurationInstance)

        alertAdministrationService.nullifyAutoAdjustmentRelatedFlow(configurationInstance, configurationInstance.type)

        configurationInstance.evaluateDateAs = params.evaluateDateAs ?: EvaluateCaseDateEnum.LATEST_VERSION
        if (params.asOfVersionDate) {
            bindAsOfVersionDateOfSingleCase(configurationInstance)
        }

        AlertDateRangeInformation alertDateRangeInformation = configurationInstance.alertDateRangeInformation
        if (params.alertDateRangeInformation) {
            bindData(alertDateRangeInformation, params.alertDateRangeInformation, [exclude: ['dateRangeEndAbsolute', 'dateRangeStartAbsolute']])
            alertDateRangeInformation.alertConfiguration = configurationInstance
            setAlertDateRange(alertDateRangeInformation)
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
            configurationInstance = (Configuration) CRUDService.updateWithAuditLog(configurationInstance)
        } catch (ValidationException ve) {
            log.error(ve.printStackTrace())
            flash.error = ve?.errors?.fieldError?.defaultMessage
            redirect(controller: "alertAdministration", action: "index")
            return
        }

        redirect(controller: "configuration", action: "executionStatus",params: [alertType: AlertType.SINGLE_CASE_ALERT])
    }

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_SINGLE_CASE_REVIEWER', 'ROLE_SINGLE_CASE_VIEWER', 'ROLE_VIEW_ALL'])
    def review() {
        //If the cumulative alert check box is checked then flow will show the bulk cumulative screen with all the record set.
        Boolean showCumulative = userService.getUser()?.preference?.isCumulativeAlertEnabled
        Map  singleReviewHelpMap =Holders.config.single.review.helpMap

        if (showCumulative) {
            params.callingScreen = Constants.Commons.TRIGGERED_ALERTS
            params.cumulative = true
            details()
        } else {
            render (view: "index",model:[singleReviewHelpMap:singleReviewHelpMap])
        }
    }

    def listAllResults() {
        User currentUser = userService.getUser()
        render(view: "executionStatus", model: [related: "listAllResultsPage", isAdmin: currentUser?.isAdmin()])
    }

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_SINGLE_CASE_REVIEWER', 'ROLE_SINGLE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_EXECUTE_SHARED_ALERTS'])
    def viewConfig() {
        if (params.id) {
            if (params.from == "result") {
                def configurationInstance = ExecutedConfiguration.get(params.id)
                redirect(controller: "configuration", action: "viewExecutedConfig", id: configurationInstance.id)
            } else {
                redirect(controller: "configuration", action: "view", id: params.id)
            }
        } else {
            flash.error = message(code: "app.configuration.id.null")
            redirect(controller: "configuration", action: "listAllResults")
        }
    }


    // This is needed to properly re-read the data being created/edited after a transaction rollback.
    private populateModel(Configuration configurationInstance) {
        params?.name = params?.name?.trim()?.replaceAll("\\s{2,}", " ")
        //Do not bind in any other way because of the clone contained in the params
        bindData(configurationInstance, params, [exclude: ["productGroupSelection","eventGroupSelection","templateQueries", "tags", "isEnabled", "asOfVersionDate", "onOrAfterDate", "template", "alertQueryValueLists"]])
        List<DateRangeEnum> dateRangeEnums = params.spotfireDaterange instanceof String ? [params.spotfireDaterange] : params.spotfireDaterange
        if(params.limitToCaseSeries){
            Map<String,String> caseSeriesMap = JSON.parse(params.limitToCaseSeries)
            configurationInstance.alertCaseSeriesId = caseSeriesMap.id as Long
            configurationInstance.alertCaseSeriesName = caseSeriesMap.name
        } else {
            configurationInstance.alertCaseSeriesId = null
            configurationInstance.alertCaseSeriesName = null
        }
        if(params.productGroupSelection != '[]'){
            configurationInstance.productGroupSelection=params.productGroupSelection
        } else {
            configurationInstance.productGroupSelection=null
        }

        if(params.eventGroupSelection != '[]'){
            configurationInstance.eventGroupSelection = params.eventGroupSelection
        } else {
            configurationInstance.eventGroupSelection = null
        }
        String productOrStudySelection =  params.productSelection?:params.studySelection
        bindSpotfireSettings(configurationInstance, params.boolean("enableSpotfire"), productOrStudySelection, params.spotfireType, dateRangeEnums)
        if (params.onOrAfterDate != "") {
            configurationInstance.setOnOrAfterDate(DateUtil.displayStringToDate(params.onOrAfterDate))
        }
        bindAsOfVersionDateOfSingleCase(configurationInstance)
        bindExistingTemplateQueryEdits(configurationInstance)
        bindNewTemplateQueries(configurationInstance)
        if (params.alertDateRangeInformation) {
            assignParameterValuesToAlertQuery(configurationInstance)
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

        updateTemplateQuerySequence(configurationInstance)
        setNextRunDateAndScheduleDateJSON(configurationInstance)
    }

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION'])
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
            configurationInstance = (Configuration) CRUDService.updateWithAuditLog(configurationInstance)
        } catch (ValidationException ve) {
            Configuration originalConfiguration = Configuration.get(params.id)
            populateModel(originalConfiguration)
            originalConfiguration.errors = ve.errors
            String action = params.previousAction
            Map model = modelData(originalConfiguration, action)
            render view: "edit", model: model
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

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION'])
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

        try {
            configurationInstance = (Configuration) CRUDService.updateWithAuditLog(configurationInstance)
        } catch (ValidationException ve) {
            Configuration originalConfiguration = Configuration.get(params.id)
            populateModel(originalConfiguration)
            originalConfiguration.errors = ve.errors
            String action = params.previousAction
            Map model = modelData(originalConfiguration, action)
            render view: "edit", model: model
            return
        }
        def exConfig = ExecutedConfiguration.findByConfigId(configurationInstance?.id)
        if (params.name && !exConfig?.name.equals(params.name)) {
            alertService.renameExecConfig(configurationInstance.id, configurationInstance.name, params.name, configurationInstance.owner.id, SINGLE_CASE_ALERT)
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
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'configuration.label'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    protected void noAlerts() {
        request.withFormat {
            form {
                flash.message = message(code: "default.no.alerts.selected")
                redirect uri: request.getHeader('referer')
            }
            '*' { render status: NO_CONTENT }
        }
    }

    def list() {
        def timezone = grailsApplication.config.server.timezone

        def scaList = singleCaseAlertService.listAll().collect { SingleCaseAlert sca ->
            [
                    id                         : sca.id,
                    alertName                  : sca.alertConfiguration.name,
                    caseNumber                 : sca.caseNumber,
                    description                : sca.alertConfiguration.description,
                    detectedDate               : toDateString(sca.detectedDate, timezone),
                    dueDate                    : toDateString(sca.dueDate, timezone),
                    productName                : sca.getAttr('masterProdTypeList_5'),
                    frequency                  : sca.alertConfiguration.scheduleDateJSON,
                    flagged                    : sca.flagged,
                    followupDate               : toDateString1(sca.getAttr(cacheService.getRptFieldIndexCache('masterFollowupDate'))),
                    caseReportType             : sca.getAttr(cacheService.getRptFieldIndexCache('masterRptTypeId')),
                    caseInitReceiptDate        : sca.getAttr(cacheService.getRptFieldIndexCache('masterInitReptDate')),
                    caseSignificantFollowupDate: toDateString1(sca.getAttr(cacheService.getRptFieldIndexCache('masterFollowupDate'))),
                    reportersHcpFlag           : sca.getAttr(cacheService.getRptFieldIndexCache('CsHcpFlag')),
                    masterProdTypeList         : sca.getAttr('masterProdTypeList_5'),
                    masterPrefTermAll          : sca.getAttr(cacheService.getRptFieldIndexCache('masterPrefTermSurAll')),
                    assessOutcome              : sca.getAttr(cacheService.getRptFieldIndexCache('assessOutcome')),
                    assessListedness           : sca.getAttr(cacheService.getRptFieldIndexCache('assessListedness')),
                    causality                  : sca.getAttr('assessAgentSuspect_9'),
                    allPt                      : sca.getAttr(cacheService.getRptFieldIndexCache('masterPrefTermAll')),
            ]
        }
        respond scaList, [formats: ['json']]
    }

    @Secured(['ROLE_SINGLE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def previousCaseState(String caseNumber, Integer caseVersion, Integer followUpNumber, Long alertConfigId) {
        def productFamily = params.productFamily
        Configuration configuration = Configuration.get(alertConfigId)
        List<SingleCaseAlert> alertList = SingleCaseAlert.findAllByCaseNumberAndAlertConfiguration(caseNumber, configuration)
        CaseHistory previousCaseHistory = caseHistoryService.getSecondLatestCaseHistory(caseNumber, productFamily)

        //Now restore the alert's workflow state.
        alertList.each { alert ->
            alert.disposition = previousCaseHistory.currentDisposition
            alert.followUpNumber = followUpNumber
            CRUDService.update(alert)
        }

        //Create the caseHistory.
        def caseHistoryMap = [
                "configId"          : Configuration.findByIdAndIsEnabled(alertConfigId, true)?.id,
                "currentDisposition": previousCaseHistory.currentDisposition,
                "justification"     : message(code: 'app.signal.case.restored.workflow'),
                "caseNumber"        : caseNumber,
                "caseVersion"       : caseVersion,
                "change"            : Constants.HistoryType.DISPOSITION,
                "productFamily"     : productFamily,
                "modifiedBy"        : userService.getUser().fullName,
                "followUpNumber"    : Integer.parseInt(params.followUpNumber),
        ]
        caseHistoryService.saveCaseHistory(caseHistoryMap)

        render(contentType: "application/json", status: OK.value()) {
            [success: 'true', previousDisposition: previousCaseHistory.currentDisposition.displayName]
        }
    }

    @Secured(['ROLE_SINGLE_CASE_REVIEWER', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def changePriorityOfAlert(String selectedRows, Priority newPriority, String justification) {
        boolean isArchived = params.boolean('isArchived')
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true, data: [])
        try {
            List<Long> singleCaseAlertIdList = JSON.parse(selectedRows).collect{ Map<String, Long> selectedRow ->
                selectedRow["alert.id"] as Long
            }
            singleCaseAlertService.changePriorityOfAlerts(singleCaseAlertIdList, justification, newPriority, responseDTO, isArchived)
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


    @Secured(['ROLE_SINGLE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def exportReport() {
        List scaL = []
        List exportList = []
        List unOrderedRecord = []
        ExecutedConfiguration ec = null
        boolean hasOver1000Records = false
        Boolean cumulative = params.boolean('cumulativeExport')
        def exportType = cumulative ? "Cumulative Export" : "Alert Details"
        Boolean isDashboard = (params.callingScreen != Constants.Commons.DASHBOARD && params.callingScreen != Constants.Commons.TAGS) ? false : true
        Boolean exportCaseNarrative = ("true".equals(params.isSafety)) && checkCaseNarrativeConfiguration(params.outputFormat?.equalsIgnoreCase(ReportFormat.XLSX.name()), "true".equals(params.promptUser))
        String timeZone = userService.getCurrentUserPreference()?.timeZone
        User user = userService.getUser()
        AlertDataDTO alertDataDTO = new AlertDataDTO()
        alertDataDTO.params = params
        alertDataDTO.timeZone = timeZone
        alertDataDTO.userId = userService.getCurrentUserId()
        alertDataDTO.cumulative = cumulative
        alertDataDTO.isFromExport = true
        alertDataDTO.domainName = alertService.generateDomainName(params.boolean('isArchived'))
        alertDataDTO.exportCaseNarrative=exportCaseNarrative
        alertDataDTO.orderColumnMap = [name: params.column, dir: params.sorting]
        if(params.tempViewId)  {
            alertDataDTO.clipBoardCases = ClipboardCases.get(params.tempViewId as Long)?.caseIds?.tokenize(',')
        }
        params.isFaers = params.boolean("isFaers")
        alertDataDTO.isFaers = params.boolean("isFaers")
        if (params.callingScreen != Constants.Commons.DASHBOARD && params.callingScreen != Constants.Commons.TAGS) {
            ec = ExecutedConfiguration.findByIdAndIsEnabled(params.id, true)
        }
        if (params.selectedCases && !cumulative) {
            exportList = alertService.getAlertFilterCountAndListICR(alertDataDTO, params.callingScreen, params.boolean('isArchived'), params.selectedCases,true).resultList
            exportList.each {
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
                alertDataDTO.uniqueDispositions.add(it.currentDisposition)
            }
        } else {
            Map filterMap = [:]
            if (params.filterList && params.filterList != "{}") {
                def jsonSlurper = new JsonSlurper()
                filterMap = jsonSlurper.parseText(params.filterList)
            }
            filterMap.each { def k, def v ->
                if(k == 'assessSeriousness'){
                    filterMap.put('serious', filterMap.remove('assessSeriousness'))
                }
            }
            List dispositionFilters = getFiltersFromParams(params.isFilterRequest?.toBoolean(), params)
            alertDataDTO.filterMap = filterMap
            alertDataDTO.executedConfiguration = ec
            alertDataDTO.execConfigId = ec?.id
            alertDataDTO.workflowGroupId = user.workflowGroup.id
            alertDataDTO.groupIdList = user.groups.collect { it.id }
            alertDataDTO.dispositionFilters = dispositionFilters
            alertDataDTO.start = 0
            alertDataDTO.length = 5000
            if(params.isFaers){
                alertDataDTO.isFaers = params.getBoolean("isFaers", false)
            }
            if(params.isJader){
                alertDataDTO.isJader = params.getBoolean("isJader", false)
            }
            if (cumulative) {
                Configuration config = Configuration.findByNameAndOwnerAndIsDeleted(ec.name, ec.owner, false)
                alertDataDTO.execConfigIdList = alertService.fetchPrevExecConfigId(ec, config, false, true)
                alertDataDTO.execConfigIdList.add(ec.id)
                alertDataDTO.dispositionFilters = []
            }
            if(isDashboard) {
                alertDataDTO.length = 5000
            }
            log.info("Generating the data for Export")
             String alertType = Constants.AlertConfigType.SINGLE_CASE_ALERT
            if(ec?.adhocRun && ec?.isCaseSeries){
                alertType = Constants.AlertConfigType.SINGLE_CASE_ALERT_DRILL_DOWN_ADHOC
            }
            List<Map> columnList = viewInstanceService.fetchVisibleColumnList(alertType, params.viewId ? params.viewId as Long : 0)
            if (!Holders.config.icr.comments.enable) {
                columnList.removeIf { it.name == Constants.AdvancedFilter.COMMENTS }
            }
            alertDataDTO.visibleColumnsList = columnList.collect { it.name }
            Map filterCountAndList = alertService.getExportedAlertData(alertDataDTO, params.callingScreen, params.boolean('isArchived'))
            exportList = filterCountAndList.resultList
            log.info("Data has been fetched")
            if(exportList?.size() > 1000){
                hasOver1000Records = true
            }
            /* To get the Archive Alert Data for Cumulative export report. */
            def archivedAlertList
            if (alertDataDTO.execConfigIdList.size() > 1 && cumulative && !hasOver1000Records) {
                log.info("Fetching the data for Archived Alert")
                alertDataDTO.start = 0
                alertDataDTO.length = 5000
                alertDataDTO.domainName = alertService.generateDomainName(true)
                filterCountAndList = alertService.getExportedAlertData(alertDataDTO, params.callingScreen, params.boolean('isArchived'))
                archivedAlertList = filterCountAndList.resultList
                exportList = exportList + archivedAlertList
                log.info("Data has been fetched for Archived Alert")
            }
        }
        if((null == cumulative || !cumulative) && alertDataDTO.dispositionFilters?.isEmpty()) { //Fix for bug PVS-55057
            exportList = []
        }
        if(exportList?.size() > 1000) {
            def  caseNarrativeSublist = exportList.subList(0, 1000);
            exportList = new ArrayList<>(caseNarrativeSublist);
        }
        params.quickFilterDisposition = alertDataDTO.uniqueDispositions.join(", ")
        //for alert review screen only
        List criteriaSheetList=[]
        if (params.callingScreen != Constants.Commons.DASHBOARD && params.callingScreen != Constants.Commons.TAGS) {
            //criteriaSheet data for export reports
            params.totalCount = exportList?.size?.toString()
            boolean isJader = params?.isJader == "true" ? true : false
            if(isJader){
                criteriaSheetList = singleCaseAlertService.getSingleCaseAlertCriteriaData(ec, params,null,false,true)
            }
            else{
                criteriaSheetList = singleCaseAlertService.getSingleCaseAlertCriteriaData(ec, params)
            }
            if (params.isCaseSeries?.toBoolean()) {
                criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.SOC, 'value': params.soc ?: ""])
                criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.PT, 'value': params.eventName ?: ""])
                if(ec.selectedDatasource == "vigibase"){
                    criteriaSheetList.remove(['label': Constants.CriteriaSheetLabels.DISPOSITIONS, 'value': params.quickFilterDisposition?:Constants.Commons.BLANK_STRING])
                } else if(ec.adhocRun && ec.selectedDatasource == "pva"){
                    criteriaSheetList.remove(['label': Constants.CriteriaSheetLabels.DISPOSITIONS, 'value': params.quickFilterDisposition?:Constants.Commons.BLANK_STRING])
                }
            }

            params.criteriaSheetList = criteriaSheetList
        }

        Integer maxSize = 1
        if (exportCaseNarrative) {
            exportList.each { map ->
                def caseN = map.find { it.key == 'caseNarrative' }?.value
                if (caseN) {
                    map.remove('caseNarrative')
                    List caseNarrativeList = splitCellContent(caseN as String)
                    maxSize = caseNarrativeList.size() > maxSize ? caseNarrativeList.size() : maxSize
                    Integer i = 1
                    caseNarrativeList.each { it ->
                        String key = "caseNarrative" + i
                        map.put(key, it)
                        i++
                    }
                }
            }
        }
        params << [
                exportCaseNarrative: exportCaseNarrative,
                maxSize            : maxSize
        ]
        Boolean isLongComment = exportList?.any({x -> x.comments?.size() > 100})
        params.isLongComment = isLongComment?: false
        def reportFile = dynamicReportService.createAlertsReport(new JRMapCollectionDataSource(exportList), params)
        renderReportOutputType(reportFile, params)
        signalAuditLogService.createAuditForExport(criteriaSheetList,  isDashboard == true ? "Individual Case Review Dashboard" :ec.getInstanceIdentifierForAuditLog()+" : ${exportType}", isDashboard == true ? "Individual Case Review DashBoard" :(ec.isLatest ? Constants.AuditLog.SINGLE_REVIEW : "Individual Case Review: Archived Alert"),params,reportFile.name)

    }

    @Secured(['ROLE_SINGLE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def exportDetailReport() {
        if (!params.selectedCases) {
            noAlerts()
            return
        }
        def domain = params.boolean('isArchived') ? ArchivedSingleCaseAlert : SingleCaseAlert
        def scadList = singleCaseAlertService.listSelectedAlerts(params.selectedCases,domain)
        def reportFile = dynamicReportService.createAlertsDetailReport(new JRMapCollectionDataSource(scadList), params,
                scadList ? scadList.first().alertName : "")

        renderReportOutputType(reportFile, params)
    }

    /**
     * Send the output type (PDF/Excel/Word) to the browser which will save it to the user's local file system
     * @param reportFile
     * @return
     */
    private renderReportOutputType(File reportFile,def params) {
        String reportName = "Single Case Alert" + DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss").print(new DateTime())
        params.reportName=reportName.replaceAll(" ","+")
        response.contentType = "${dynamicReportService.getContentType(params.outputFormat)}; charset=UTF-8"
        response.contentLength = reportFile.size()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" +
                "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.$params.outputFormat" + "\"")
        response.getOutputStream().write(reportFile.bytes)
        response.outputStream.flush()
    }

    @Secured(['ROLE_SINGLE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION', 'ROLE_SIGNAL_MANAGEMENT_VIEWER','ROLE_JADER_CONFIGURATION'])
    def details(Boolean isCaseDetailView) {
        log.info("Details Started")
        // Initiate Ui labels from mart-- currently only added for safety
        cacheService.prepareUiLabelCacheForSafety()
        Boolean alertDeletionObject = false
        Long id = params.long("configId")
        if(params.callingScreen != Constants.Commons.DASHBOARD && id == -1){
            alertDeletionInProgress()
            return
        }
        def name = Constants.Commons.BLANK_STRING
        def dateRange = Constants.Commons.BLANK_STRING
        def cumulative = params.cumulative ?: false
        def isCaseSeries = false
        def isCaseSeriesAlert = false
        def justificationList = []
        def backUrl = request.getHeader('referer')
        boolean isArchived = false
        def singleHelpMap=Holders.config.single.helpMap
        String alertType = SINGLE_CASE_ALERT
        isCaseSeriesAlert = params.boolean("isCaseSeriesAlert")
        Boolean isCaseVersion = dataObjectService.getDataSourceMap(Constants.DbDataSource.IS_ARISG_PVIP)
        Boolean showDob=Holders.config.enable.show.dob

        String timezone = userService.getCurrentUserPreference()?.timeZone
        Boolean isPriorityEnabled = Holders.config.alert.priority.enable
        Boolean isDashboardScreen = false
        Boolean customFieldsEnabled = Holders.config.custom.qualitative.fields.enabled
        boolean calculateIndexes = true
        String viewAlertType = SINGLE_CASE_ALERT
        ExecutedConfiguration exConfig = ExecutedConfiguration.findById(id)
        if(exConfig) {
            alertDeletionObject = alertService.isDeleteInProgress(exConfig.configId as Long, exConfig?.type) ?: false
        }
        String selectedDatasource = exConfig ? exConfig.selectedDatasource : null
        User user=userService.getUser()
        Boolean isAuthorized = isAuthorized(params.boolean("isCaseSeries"),selectedDatasource) // Added for PVS-60158
        ExecutedConfiguration exAggConfig = null
        if(isAuthorized && params.primExecutionId) {
            exAggConfig = ExecutedConfiguration.findById(params.primExecutionId as Long)
        }
        if (params.callingScreen == Constants.Commons.REVIEW && ((alertService.checkAlertSharedToCurrentUser(exConfig) && alertService.checkAlertSharedToCurrentUser(exAggConfig)) || !isAuthorized)) {
            forward(controller: 'errors', action: 'permissionsError')
            log.info("${user?.username} does not have access to alert")
            return
        }
        if (params.callingScreen != Constants.Commons.REVIEW) {
            isDashboardScreen = true
            alertType = Constants.AlertConfigType.SINGLE_CASE_ALERT_DASHBOARD
            viewAlertType = Constants.AlertConfigType.SINGLE_CASE_ALERT_DASHBOARD
        }
        List<Map> fieldList = []
        Map rptToSignalFieldMap = Holders.config.icrFields.rptMap
        Map rptToUiLabelMap = cacheService.getRptToUiLabelMapForSafety() as Map
        if(params.boolean("isVaers")){
            alertType = Constants.AlertConfigType.SINGLE_CASE_ALERT_VAERS
            viewAlertType = Constants.AlertConfigType.SINGLE_CASE_ALERT_VAERS
            fieldList = grailsApplication.config.signal.scaVaersColumnList.clone() as List<Map>
            calculateIndexes = false
        } else if(params.boolean("isVigibase")){
            alertType = Constants.AlertConfigType.SINGLE_CASE_ALERT_VIGIBASE
            viewAlertType = Constants.AlertConfigType.SINGLE_CASE_ALERT_VIGIBASE
            fieldList = grailsApplication.config.signal.scaVigibaseColumnList.clone() as List<Map>
            calculateIndexes = false
        } else if(params.boolean("isJader")){
            alertType = Constants.AlertConfigType.SINGLE_CASE_ALERT_JADER
            viewAlertType = Constants.AlertConfigType.SINGLE_CASE_ALERT_JADER
            fieldList = grailsApplication.config.signal.scaJaderColumnList.clone() as List<Map>
            calculateIndexes = false
        }else if(params.boolean("isFaers")){
            alertType = Constants.AlertConfigType.SINGLE_CASE_ALERT_FAERS
            viewAlertType = Constants.AlertConfigType.SINGLE_CASE_ALERT_FAERS
            fieldList = grailsApplication.config.signal.scaFaersColumnList.clone() as List<Map>
            calculateIndexes = false
        } else if (params.boolean("isCaseSeries") && params.boolean("isAggregateAdhoc")){
            alertType = Constants.AlertConfigType.SINGLE_CASE_ALERT_DRILL_DOWN
            viewAlertType = Constants.AlertConfigType.SINGLE_CASE_ALERT_DRILL_DOWN_ADHOC
            fieldList = grailsApplication.config.signal.scaColumnListAdhoc.clone() as List<Map>
        } else if (params.boolean("isCaseSeries")){
            alertType = Constants.AlertConfigType.SINGLE_CASE_ALERT_DRILL_DOWN
            viewAlertType = Constants.AlertConfigType.SINGLE_CASE_ALERT_DRILL_DOWN
            fieldList = grailsApplication.config.signal.scaColumnList.clone() as List<Map>
        }
        else{
            fieldList = grailsApplication.config.signal.scaColumnList.clone() as List<Map>
        }
        fieldList = removeFDAColumns(Holders.config.custom.qualitative.fields.enabled, fieldList)
        if (!Holders.config.icr.comments.enable) {
            fieldList.removeIf { it.name == Constants.AdvancedFilter.COMMENTS }
        }
        if(showDob==false){
           fieldList.removeAll {it.name=="dateOfBirth"}
        }
        if(isCaseVersion && !params.boolean("isFaers")){
            fieldList.each{ it->
                if(it.name=='caseNumber'){
                    it.display = Constants.Commons.CASE_VERSION_NO
                }
            }
        }
        if(params.callingScreen == Constants.Commons.REVIEW) {
            fieldList.remove([name: "name", display: "Alert Name", dataType: 'java.lang.String'])
        }
        if(params.boolean("isCaseSeries") && params.boolean("isVigibase")){
            fieldList.each{ it->
                if(it.name=='conComit'){
                    it.name = 'conComitList'
                }
            }
        }
        // change advance filter info according to Ui label, currently only change for SafetyDB
        if(!params.boolean("isVigibase") && !params.boolean("isJader") && !params.boolean("isVaers") && !params.boolean("isFaers")){
            fieldList.each {
                if(rptToSignalFieldMap.get(it.name)!=null && rptToUiLabelMap.get(rptToSignalFieldMap.get(it.name))!=null){
                    it.display = rptToUiLabelMap.get(rptToSignalFieldMap.get(it.name))?:it.display
                }
                if(it.get("name")=='suspectProductList' && rptToUiLabelMap.get('masterSuspProdAgg')!=null){
                    it.display = rptToUiLabelMap.get('masterSuspProdAgg')?:it.display
                }
                else if(it.get("name")=='allPtList' && rptToUiLabelMap.get('masterPrefTermAll')!=null){
                    it.display = rptToUiLabelMap.get('masterPrefTermAll')?:it.display
                }
                else if(it.get("name")=='ptList' && rptToUiLabelMap.get('masterPrefTermSurAll')!=null){
                    it.display = rptToUiLabelMap.get('masterPrefTermSurAll')?:it.display
                }
                else if(it.get("name")=='primSuspProdList' && rptToUiLabelMap.get('masterPrimProdName')!=null){
                    it.display = rptToUiLabelMap.get('masterPrimProdName')?:it.display
                }
                else if(it.get("name")=='conComitList' && rptToUiLabelMap.get('masterConcomitProdList')!=null){
                    it.display = rptToUiLabelMap.get('masterConcomitProdList')?:it.display
                }
            }
        }
        ViewInstance viewInstance = viewInstanceService.fetchSelectedViewInstance(viewAlertType,params.viewId as Long)

        List actionConfigList = validatedSignalService.getActionConfigurationList(SINGLE_CASE_ALERT)
        Configuration configuration
        Map<String , Map> statusMap = [:]
        //If flow is coming from the dashboard and its not cumulative then we need to show name on the ui as well.
        if (params.callingScreen != Constants.Commons.DASHBOARD && !cumulative && params.callingScreen != Constants.Commons.TAGS) {
            if (exConfig) {
                name = exConfig?.name
                if(!exConfig.isCaseSeries && !exConfig.isLatest){
                    isArchived = true
                }
                ExecutedAlertDateRangeInformation executedAlertDateRangeInformation = exConfig?.executedAlertDateRangeInformation
                dateRange = DateUtil.toDateString(executedAlertDateRangeInformation?.dateRangeStartAbsolute) +
                        " - " +
                        DateUtil.toDateString(executedAlertDateRangeInformation?.dateRangeEndAbsolute)
                isCaseSeries = exConfig.isCaseSeries ?: false
            }else{
                notFound()
                return
            }
            if(alertDeletionObject){
                alertDeletionInProgress()
                return
            }

            def justificationObjList = Justification.list()
            justificationObjList.each {
                if (it.getAttr("caseAddition") == "on") {
                    justificationList.add(it)
                }
            }
            justificationList = justificationList.collect { [id: it.id, name: it.name, text: it.justification] }
            statusMap = spotfireService.fetchAnalysisFileUrlCounts(exConfig)

        } else if (params.callingScreen != Constants.Commons.TAGS) {
            backUrl = createLink(controller: 'dashboard', action: 'index')
        }
        Map indexMap = [fixedColumns:0,indexList:[]]
        if(calculateIndexes) {
            indexMap = alertService.getSingleCaseFilterIndexes(isPriorityEnabled, isDashboardScreen,
                    customFieldsEnabled, isCaseSeries, exConfig?.adhocRun)
        }
        User currentUser = cacheService.getUserByUserNameIlike(userService.getCurrentUserName())
        Map dispositionIncomingOutgoingMap = workflowRuleService.fetchDispositionIncomingOutgoingMap()
        List<Map> availableAlertPriorityJustifications = isPriorityEnabled? Justification.fetchByAnyFeatureOn([JustificationFeatureEnum.alertPriority], false)*.toDto(timezone) : []
        Group workflowGroup = currentUser?.workflowGroup
        Boolean forceJustification = cacheService.getGroupByGroupId(workflowGroup?.id)?.forceJustification
        List<Map> availablePriorities = isPriorityEnabled? priorityService.listPriorityOrder() : []
        List allowedProductsAsSafetyLead = alertService.isProductSecurity() ? safetyLeadSecurityService.allAllowedProductsForUser(userService.getCurrentUserId()) : []
        List alertDispositionList = dispositionService.listAlertDispositions()
        Map actionTypeAndActionMap = alertService.getActionTypeAndActionMap()
        List<String> reviewCompletedDispostionList = cacheService.getDispositionByReviewCompleted().collect{it.displayName}
        Long meetingId = ActionConfiguration.findByDisplayName("Meeting")?.id
        Boolean isShareFilterViewAllowed = currentUser.isAdmin()
        Boolean isViewUpdateAllowed = viewInstance?.isViewUpdateAllowed(currentUser)
        String pECaseSeries = ""
        String safetyProductName = ""
        if(params.boolean("isCaseSeriesAlert")){
            // if case series is saved from case series this flag becomes true
            pECaseSeries = ""
        } else if(params.boolean("isCaseSeries")) {
            pECaseSeries = params.productName + ": " + params.eventName
            safetyProductName = params?.productName
        } else if(params.boolean("isCaseDetailView") && (params.containsKey("isFaers") || params.containsKey("isVaers") || params.containsKey("isVigibase") ||  params.containsKey("isJader"))) {
            pECaseSeries = exConfig.name.split('_')[-4] + ":" + exConfig.name.split('_')[-3]
        }
        else if( params.containsKey("isVaers") || (params.containsKey("isFaers")) || (params.containsKey("isVigibase")) || (params.containsKey("isJader"))){
            pECaseSeries = exConfig.name.split('_')[-4] + ":" + exConfig.name.split('_')[-3]
        }
        ClipboardCases clipboardCase = ClipboardCases.createCriteria().get{
            eq('user.id' , currentUser?.id)
            'or'{
                eq('isFirstUse' , true)
                eq('isUpdated', true)
            }

        }
        Map dispositionData = workflowRuleService.fetchDispositionData()
        String tempViewPresent = clipboardCase?.id && !isArchived ? (clipboardCase?.id) : (params.containsKey('tempViewId') && !isArchived ? params.tempViewId:"false")
        Boolean isTempViewSelected = false
        if (params.containsKey("tempViewId")) {
            isTempViewSelected = true
        }
        Boolean hasSingleReviewerAccess = hasReviewerAccess(Constants.AlertConfigType.SINGLE_CASE_ALERT, isCaseSeries)
        String buttonClass = hasSingleReviewerAccess?"":"hidden"
        def latestVersion
        if (params.callingScreen != Constants.Commons.DASHBOARD){
            configuration = Configuration.findByName(exConfig?.name)
            latestVersion = ExecutionStatus.findAllByConfigIdAndExecutionStatusAndType(configuration?.id, ReportExecutionStatus.COMPLETED, alertType)?.size()
        }
        // create Map of signal Field to Ui label by mart, currently only if datasource is safety
        Map columnLabelMap = [:]
        if(!params.boolean("isVigibase") && !params.boolean("isJader") && !params.boolean("isVaers") && !params.boolean("isFaers")) {
            rptToSignalFieldMap.each {
                if (rptToUiLabelMap.get(it.value) != null) {
                    columnLabelMap.put(it.key, rptToUiLabelMap.get(it.value))
                }
            }
        }
        String detailedAdvanceFilterName =  "null"
        if(params?.detailedAdvancedFilterId != "null" && params?.detailedAdvancedFilterId !="undefined") {
            AdvancedFilter advancedFilter = AdvancedFilter.findById(params?.detailedAdvancedFilterId)
             detailedAdvanceFilterName = advancedFilter?.name
        }
        Boolean isAdhocCaseSeries = false
        if(isCaseSeries){
            isAdhocCaseSeries = exConfig.adhocRun
        }
           render view: 'details', model: [executedConfigId                    : id,
                                        reportUrl                           : reportIntegrationService.fetchReportUrl(exConfig),
                                        analysisFileUrl                     : spotfireService.fetchAnalysisFileUrl(exConfig),
                                        backUrl                             : backUrl, reportName: exConfig?.name,
                                        callingScreen                       : params.callingScreen,
                                        fixedColumnScaCount                 : indexMap.fixedColumns,
                                        indexListSca                        : JsonOutput.toJson(indexMap.indexList),
                                        name                                : name,
                                        cumulative                          : cumulative,
                                        dateRange                           : dateRange,
                                        viewInstance                        : viewInstance ?: null,
                                        isShareFilterViewAllowed            : isShareFilterViewAllowed,
                                        isViewUpdateAllowed                 : isViewUpdateAllowed,
                                        filterMap                           : viewInstance ? viewInstance.filters : "",
                                        columnIndex                         : "",
                                        sortedColumn                        : viewInstance ? viewInstance.sorting : "",
                                        viewId                              : viewInstance ? viewInstance.id : "",
                                        advancedFilterView                  : viewInstance?.advancedFilter ? alertService.fetchAdvancedFilterDetails(viewInstance.advancedFilter) : "",                                        justification                       : justificationList,
                                        isCaseSeries                        : isCaseSeries,
                                        isCaseSeriesAlert                   : isCaseSeriesAlert,
                                        justificationJSON                   : JsonOutput.toJson(justificationList),
                                        tagName                             : params.tagName,
                                        isFaers                             : params.boolean("isFaers"),
                                        isVaers                             : params.boolean("isVaers"),
                                        isVigibase                          : params.boolean("isVigibase"),
                                        isJader                             : params.boolean("isJader"),
                                        isCaseDetailView                    : isCaseDetailView,
                                        isLatest                            : exConfig?.isLatest,
                                        actionConfigList                    : actionConfigList,
                                        dashboardFilter                     : params.dashboardFilter ? params.dashboardFilter : '',
                                        dispositionIncomingOutgoingMap      : dispositionIncomingOutgoingMap as JSON,
                                        dispositionData                     : dispositionData as JSON,
                                        forceJustification                  : forceJustification,
                                        availableAlertPriorityJustifications: availableAlertPriorityJustifications,
                                        availablePriorities                 : availablePriorities,
                                        allowedProductsAsSafetyLead         : allowedProductsAsSafetyLead?.join(","),
                                        appType                             : Constants.AlertConfigType.SINGLE_CASE_ALERT,
                                        fieldList                           : fieldList.sort({
                                            it.display.toUpperCase()
                                        }),
                                        actionTypeList                      : actionTypeAndActionMap.actionTypeList,
                                        meetingId                           : meetingId,
                                        customFieldsEnabled                 : customFieldsEnabled,
                                        reviewCompletedDispostionList       : JsonOutput.toJson(reviewCompletedDispostionList),
                                        actionPropertiesMap                 : JsonOutput.toJson(actionTypeAndActionMap.actionPropertiesMap),
                                        alertDispositionList                : alertDispositionList,
                                        isProductSecurity                   : alertService.isProductSecurity(),
                                        isArchived                          : isArchived,
                                        alertType                           : alertType,
                                        icrAlertType                        : viewAlertType,
                                        caseSeriesId                        : exConfig?.pvrCaseSeriesId,
                                        isCaseSeriesGenerating              : params.isCaseSeriesGenerating,
                                        pECaseSeries                        : pECaseSeries,
                                        isPriorityEnabled                   : isPriorityEnabled,
                                        tempViewPresent                     : tempViewPresent,
                                        productName                         : params.productName ?: "",
                                        eventName                           : params.eventName ?: "",
                                        soc                                 : params.soc ?: "",
                                        analysisStatus                      : statusMap,
                                        isTempViewSelected                  : isTempViewSelected,
                                        clipboardInterval                   : Holders.config.pvs.details.copy.clipboard.interval,
                                        isCommentsEditEnable                : Holders.config.icr.comments.enable, // added comments flag for PVS-63960
                                        saveCategoryAccess                  : checkAccessForCategorySave(Constants.AlertConfigType.SINGLE_CASE_ALERT),
                                        hasSingleReviewerAccess             : hasSingleReviewerAccess,
                                        hasSignalCreationAccessAccess       : hasSignalCreationAccessAccess(),
                                        hasSignalViewAccessAccess           : hasSignalViewAccessAccess(),
                                        buttonClass                         : buttonClass,
                                        aggExecutionId                      : params.aggExecutionId,
                                        isAggregateAdhoc                    : params.isAggregateAdhoc,
                                        isCaseVersion                       : isCaseVersion,
                                        type                                : params.type ?: null,
                                        typeFlag                            : params.typeFlag ?: null,
                                        version                             : params.version ?: latestVersion ?: "null",
                                        alertId                             : params.aggAlertId ?: null,
                                        showDob                             : showDob,
                                        currUserName                        : currentUser.fullName,
                                        columnLabelMap                      : columnLabelMap,
                                        singleHelpMap                           : singleHelpMap,
                                           detailedAdvancedFilterId                 : params.detailedAdvancedFilterId ?:"null",
                                           detailedAdvanceFilterName         : detailedAdvanceFilterName ?:"null",
                                           detailedViewInstanceId            : params?.viewId  ?: "null",
                                        isAdhocCaseSeries                 : isAdhocCaseSeries?:false,
                                        exportAlways                      : caseNarrativeConfigurationService.isExportAlwaysEnabled(),
                                        promptUser                        : caseNarrativeConfigurationService.isPromptUserEnabled(),
                                           safetyProductName               :safetyProductName
           ]
    }
    void alertDeletionInProgress() {
        redirect(action: "alertInProgressError", controller: 'errors')
    }
    //Added for PVS-60158
    Boolean isAuthorized(Boolean isCaseSeries,String selectedDatasource) {
         Boolean isSingleCaseAlertRole = alertService.roleAuthorised(Constants.AlertConfigType.SINGLE_CASE_ALERT,selectedDatasource)
         Boolean isCaseSeriesRole = alertService.roleAuthorised(Constants.AlertConfigType.AGGREGATE_CASE_ALERT,selectedDatasource)
        if(isSingleCaseAlertRole || (isCaseSeries && isCaseSeriesRole))
            return true
        else
            return false
    }

    @Secured(['ROLE_SINGLE_CASE_REVIEWER', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_REVIEWER','ROLE_JADER_CONFIGURATION'])
    def changeAssignedTo() {
        def alertList = params.alertDetails
        Configuration configuration
        JsonSlurper jsonSlurper = new JsonSlurper()
        String userGroupName
        String oldUserGroupName
        Map userGroupMap = userGroupService.fetchUserGroupMap(params.newValue)
        Boolean isAssignmentTypeGroup = userGroupService.checkAssignmentGroupType(params.newValue)
        try {
            def alertListJson = jsonSlurper.parseText(alertList)
            List<Long> alertIdsList = []

            alertListJson.each {
                alertIdsList.add(Long.parseLong(it.alertId))
            }
            List<SingleCaseAlert> alertListObj = SingleCaseAlert.findAllByIdInList(alertIdsList)
            String oldUserName
            List<User> oldUserList
            String newUserName
            List<User> newUserList
            alertListObj.each { SingleCaseAlert alert ->
                Long alertId = alert.id
                configuration = alert.alertConfiguration
                String eventName = alert.pt
                if (alertId) {
                    oldUserName = userService.getAssignedToName(alert)
                    oldUserList = userService.getUserListFromAssignToGroup(alert)
                    Map productMap = [product: params.productSelection, productGroup: params.productGroupSelection]
                    alert = userService.assignGroupOrAssignTo(params.newValue, alert, productMap)
                    newUserName = userService.getAssignedToName(alert)
                    newUserList = userService.getUserListFromAssignToGroup(alert)
                    userGroupName = isAssignmentTypeGroup ? userGroupMap.group.name : userGroupMap.user.fullName
                    oldUserGroupName = alert.assignedTo ? alert.assignedTo.fullName : alert.assignedToGroup.name
                    User currUser = userService.getUser()

                    if (userGroupMap) {
                        CaseHistory caseHistoryObj = caseHistoryService.getLatestCaseHistory(alert.caseNumber, configuration?.id)
                        Map assignedToChangeMap = getAssignedToChangeMap(alert)
                        //Send the email of assigned to.
                        sendMailOfAssignedToAction(oldUserList, newUserList, alert, caseHistoryObj)

                        singleCaseAlertService.updateSingleCaseAlertStates(alert, assignedToChangeMap)

                        if (params.executedConfigId) {
                            Long executedConfigId = Long.parseLong(params.executedConfigId)
                            ExecutedConfiguration executedConfig = singleCaseAlertService.getExecConfigurationById(executedConfigId)

                            activityService.createActivity(executedConfig, singleCaseAlertService.getActivityByType(ActivityTypeValue.AssignedToChange),
                                    currUser, "Assigned To changed from '${oldUserName}' to '${newUserName}'", null, ['For Case Number': alert.caseNumber],
                                    alert.productName, eventName, alert.assignedTo, alert.caseNumber, alert.assignedToGroup)
                        }
                    }
                }
            }
            render(contentType: "application/json", status: OK.value()) {
                [success: 'true', newValue: userGroupName,
                 newId  : isAssignmentTypeGroup ? userGroupMap.group.id : userGroupMap.user.id]
            }
        } catch (Exception ex) {
            ex.printStackTrace()
            render(status: BAD_REQUEST)
        }
    }

    @Secured(['ROLE_SINGLE_CASE_REVIEWER', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_REVIEWER','ROLE_JADER_CONFIGURATION'])
    def changeAssignedToGroup(String selectedId, String assignedToValue) {
        ResponseDTO responseDTO = new ResponseDTO(status: true, message: message(code: 'app.assignedTo.changed.success'))
        List<Long> singleCaseAlertIdList = JSON.parse(selectedId).collect {
            it as Long
        }
        boolean bulkUpdate = singleCaseAlertIdList.size() > 1
        try {
            boolean isArchived = params.boolean('isArchived')
            List<Long> existingCaseHistoryList = []
            List<CaseHistory> caseHistoryList = []
            Map<Long, String> tagsNameMap = singleCaseAlertService.getTagsNameListBulkOperations(singleCaseAlertIdList)
            AlertLevelDispositionDTO alertLevelDispositionDTO = new AlertLevelDispositionDTO()
            alertLevelDispositionDTO.changeType = Constants.HistoryType.ASSIGNED_TO
            alertLevelDispositionDTO.userName = userService.getCurrentUserName()
            alertLevelDispositionDTO.existingCaseHistoryList = caseHistoryService.existingCaseHistoryMap(singleCaseAlertIdList)
            def domain = isArchived ? ArchivedSingleCaseAlert : SingleCaseAlert
            List singleCaseAlertList = singleCaseAlertService.fetchSingleCaseAlertsForAssignedTo(singleCaseAlertIdList,domain)
            List<Map> activityList = []
            User currUser = cacheService.getUserByUserId(userService.getCurrentUserId())
            Activity activity
            Long userId = null
            Long groupId = null
            List<User> newUserList = []
            def assignedObject
            if (assignedToValue.startsWith(Constants.USER_GROUP_TOKEN)) {
                groupId = Long.valueOf(assignedToValue.replaceAll(Constants.USER_GROUP_TOKEN, ''))
                assignedObject = Group.get(groupId)
                newUserList = userGroupService.fetchUserListForGroup(assignedObject)
            } else {
                userId = Long.valueOf(assignedToValue.replaceAll(Constants.USER_TOKEN, ''))
                assignedObject = User.get(userId)
                newUserList = [assignedObject]
            }
            String newUserName = userService.getAssignedToNameFromCache(userId, groupId)
            DashboardCountDTO dashboardCountDTO = alertService.prepareDashboardCountDTO(true)
            def isCaseSeries = false
            singleCaseAlertList.each { singleCaseAlert ->
                if (singleCaseAlert.isCaseSeries) {
                    isCaseSeries = true
                }
                if (assignedToValue) {
                    if (emailNotificationService.emailNotificationWithBulkUpdate(bulkUpdate, Constants.EmailNotificationModuleKeys.ASSIGNEE_UPDATE)) {
                        List<User> oldUserList = userService.getUserListFromAssignToGroup(singleCaseAlert)
                        sendMailOfAssignedToAction(oldUserList, newUserList, singleCaseAlert, null, isArchived, newUserName)
                    }
                    List<Map> bulkUpdateDueDateDataList = []
                    Map caseHistoryAndExistingCaseHistoryMap = singleCaseAlertService.createCaseAndExistingCaseHistory(singleCaseAlert, tagsNameMap, alertLevelDispositionDTO, isArchived, bulkUpdateDueDateDataList, false, false)
                    CaseHistory caseHistory = caseHistoryAndExistingCaseHistoryMap.caseHistory
                    List<Map> auditChildMap = []
                    if (assignedToValue.startsWith(Constants.USER_GROUP_TOKEN)) {
                        def childEntry = [
                                propertyName: "Assigned To Group",
                                oldValue    : caseHistory?.currentAssignedToGroup?.name,
                                newValue    : assignedObject?.name]
                        auditChildMap << childEntry

                        if (caseHistory?.currentAssignedTo?.name != null && caseHistory?.currentAssignedTo?.name != "") {
                            childEntry = [
                                    propertyName: "Assigned To User",
                                    oldValue    : caseHistory?.currentAssignedTo?.name,
                                    newValue    : ""]
                            auditChildMap << childEntry
                        }
                        caseHistory.currentAssignedToGroup = assignedObject
                        caseHistory.currentAssignedTo = null
                    } else {
                        def childEntry = [
                                propertyName: "Assigned To User",
                                oldValue    : caseHistory?.currentAssignedTo?.name,
                                newValue    : assignedObject?.name]
                        auditChildMap << childEntry

                        if(caseHistory?.currentAssignedToGroup?.name!=null && caseHistory?.currentAssignedToGroup?.name!="")
                        {
                            childEntry = [
                                propertyName: "Assigned To Group",
                                oldValue: caseHistory?.currentAssignedToGroup?.name,
                                newValue    : ""]
                        auditChildMap << childEntry
                        }
                        caseHistory.currentAssignedTo = assignedObject
                        caseHistory.currentAssignedToGroup = null
                    }
                    signalAuditLogService.createAuditLog([
                            entityName: "Individual Case Review",
                            moduleName: "Individual Case Review",
                            category: AuditTrail.Category.UPDATE.toString(),
                            entityValue: singleCaseAlert.getInstanceIdentifierForAuditLog(),
                            username: userService.getUser().username,
                            fullname: userService.getUser().fullName
                    ] as Map, auditChildMap)
                    caseHistoryList.add(caseHistory)
                    Long existingCaseHistoryId = caseHistoryAndExistingCaseHistoryMap.existingCaseHistory
                    if (existingCaseHistoryId) {
                        existingCaseHistoryList.add(existingCaseHistoryId)
                    }
                    activity = activityService.createActivityBulkUpdate(singleCaseAlertService.getActivityByType(ActivityTypeValue.AssignedToChange),
                            currUser, "Assigned To changed from '${userService.getAssignedToName(singleCaseAlert)}' to '${newUserName}'",
                            null, ['For Case Number': singleCaseAlert.caseNumber],
                            singleCaseAlert.productName, singleCaseAlert.pt, userId?cacheService.getUserByUserId(userId):null,
                            singleCaseAlert.caseNumber, groupId?cacheService.getGroupByGroupId(groupId):null)

                    activityList.add([execConfigId: singleCaseAlert.executedAlertConfigurationId, activity: activity])
                    //For Dashboard Counts
                    if (alertService.isUpdateDashboardCount(isArchived, singleCaseAlert) && singleCaseAlert.assignedToId) {
                        alertService.updateDashboardCountMaps(dashboardCountDTO.userDispCountsMap, singleCaseAlert.assignedToId, singleCaseAlert.dispositionId.toString())
                        if (singleCaseAlert.dueDate) {
                            alertService.updateDashboardCountMaps(dashboardCountDTO.userDueDateCountsMap, singleCaseAlert.assignedToId, DateUtil.stringFromDate(singleCaseAlert.dueDate, "dd-MM-yyyy", "UTC"))
                        }

                    } else if (alertService.isUpdateDashboardCount(isArchived, singleCaseAlert)) {
                        alertService.updateDashboardCountMaps(dashboardCountDTO.groupDispCountsMap, singleCaseAlert.assignedToGroupId, singleCaseAlert.dispositionId.toString())
                        if (singleCaseAlert.dueDate) {
                            alertService.updateDashboardCountMaps(dashboardCountDTO.groupDueDateCountsMap, singleCaseAlert.assignedToGroupId, DateUtil.stringFromDate(singleCaseAlert.dueDate, "dd-MM-yyyy", "UTC"))
                        }
                    }
                }
            }
            if (singleCaseAlertIdList) {
                if (!isCaseSeries) {
                    alertService.updateAssignedToDashboardCounts(dashboardCountDTO, userId, groupId)
                }
                alertService.bulkUpdateAssignedTo(assignedToValue, singleCaseAlertIdList,userId,groupId,domain)
                notify 'activity.case.history.event.published', [existingCaseHistoryList: existingCaseHistoryList, caseHistoryList: caseHistoryList,
                                                                 activityList           : activityList, isBulkUpdate: true]
            }

        } catch (grails.validation.ValidationException vx) {
            responseDTO.message = MiscUtil.getCustomErrorMessage(vx)
            responseDTO.status = false
            log.error("Exception is : ${vx}")
        }catch (Exception e) {
            log.error(e.getMessage(), e)
            responseDTO.status = false
            responseDTO.message = message(code: 'app.assignedTo.changed.fail')
        }
        render(responseDTO as JSON)
    }

    private void sendMailOfAssignedToAction(List<User> oldUserList, List<User> newUserList, def alert, CaseHistory caseHistoryObj, Boolean isArchived, String newUserName) {
        List sentEmailList = []
        String alertLink = createHref("singleCaseAlert", "details", ["configId": alert.executedAlertConfiguration.id, "callingScreen":"review" ,isArchived: isArchived])
        String caseLink = createHref('caseInfo',"caseDetail",["caseNumber":alert.caseNumber,"version":alert.caseVersion,"followUpNumber":alert.followUpNumber,"alertId":alert.id,"isArchived":isArchived,isFaers:alert?.executedAlertConfiguration?.selectedDatasource?.contains("faers"),isVaers:alert?.executedAlertConfiguration?.selectedDatasource?.contains("Vaers"),isVigibase:alert?.executedAlertConfiguration?.selectedDatasource?.contains("Vigibase"),isSingleAlertScreen:true,isVersion:true,isCaseSeries:alert?.executedAlertConfiguration?.isCaseSeries,isAggregateAdhoc:false])
        //Send email to assigned User
        String newMessage = message(code: 'app.email.case.assignment.message.newUser')
        String oldMessage = message(code: 'app.email.case.assignment.message.oldUser')
        List emailDataList = userService.generateEmailDataForAssignedToChange(newMessage, newUserList, oldMessage, oldUserList)
        emailDataList.each { Map emailMap ->
            if (!sentEmailList.count { it == emailMap.user.email }) {
                sendAssignedToEmail(emailMap.user, alert, caseHistoryObj, alertLink, caseLink, emailMap.emailMessage, newUserName)
                sentEmailList << emailMap.user.email
            }
        }

    }


    private sendAssignedToEmail(User newUser, def alert, CaseHistory caseHistoryObj, String alertLink, String caseLink, String messageToUser, String newUserName) {
        String alertName=''
        if(alert.name){
            alertName=alert.name
        }
        String p="CUMM_"
        String indexOfCumCount=alertName.indexOf(p)
        if(indexOfCumCount){
            alertName=alertName.replace(p,"CUM_")
        }
        emailService.sendNotificationEmail([
                'toAddress': [newUser.email],
                'title'    : message(code: "app.email.case.assignment.qualitative.message", args: [alert.productName]),
                "inboxType": "Assigned To Change",
                'map'      : ["map": ['Alert Name ' : alertName,
                                      "Case Number" : alert?.followUpNumber ? alert?.caseNumber + "(" + alert?.followUpNumber + ")" : alert?.caseNumber + "(0)",
                                      "Product Name": alert.productName,
                                      "Priority"    : cacheService.getPriorityByValue(alert.priorityId)?.displayName,
                                      "Disposition" : cacheService.getDispositionByValue(alert.dispositionId)?.displayName,
                                      "Assigned To" : newUserName
                ], "emailMessage" : messageToUser, "alertLink": alertLink, "caseLink":caseLink]])
    }

    @Secured(['ROLE_SINGLE_CASE_REVIEWER', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def toggleFlag(Long id) {
        if (id) {
            Boolean flagged = alertService.toggleAlertFlag(id,SingleCaseAlert)
            render([success: 'ok', flagged: flagged] as JSON)
        } else {
            response.status = 404
        }
    }

    /**
     * This method is responsible to render the review screen for the single case alerts(qualitative alerts).
     * @return
     */
    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_SINGLE_CASE_REVIEWER', 'ROLE_SINGLE_CASE_VIEWER','ROLE_VIEW_ALL'])
    def listConfig() {
        Map resultMap = [recordsFiltered: 0, recordsTotal: 0, aaData: []]
        try {
            HttpSession session = request.getSession()
            session.setAttribute("icr",params["selectedAlertsFilter"])
            resultMap = generateResultMapForSingleCaseReview(resultMap, SingleCaseAlert)
        } catch (Throwable th) {
            th.printStackTrace()
        }
        render(resultMap as JSON)
    }

    List<ExecutedConfiguration> getExecutedConfigurationsWithAllowedProducts() {
        User user = userService.getUser()
        List<ExecutedConfiguration> executedConfigurationList = ExecutedConfiguration.list()
        List<String> allowedProductsToUser = productBasedSecurityService.allAllowedProductForUser(user)
        executedConfigurationList.each { ExecutedConfiguration c ->
            def productNameList = c.getProductNameList()
            boolean isAllowed = false
            if (productNameList instanceof String) {
                isAllowed = allowedProductsToUser.contains(productNameList)
            } else {
                isAllowed = allowedProductsToUser?.intersect(productNameList).size() > 0
            }
        }

    }

    /**
     * This method is responsible for showing the single case alert execution data in a grid.
     * for cumulative its going to show whole data otherwise its going to show the the executed configuration data only.
     * @return
     */
    @Secured(['ROLE_SINGLE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_SIGNAL_MANAGEMENT_VIEWER','ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def listByExecutedConfig(Long id, Boolean isFilterRequest) {
        def startTime=System.currentTimeSeconds()
        Map resultMap = [recordsTotal: 0, recordsFiltered: 0, aaData: [], filters: [], configId: id, advancedFilterDispName: [],visibleIdList: []]
        try {
            AlertDataDTO alertDataDTO = singleCaseAlertService.generateAlertDataDTO(params, isFilterRequest)
            alertDataDTO.length = params.int("length")
            alertDataDTO.start = params.int("start")
            String viewInstanceCheck = params["isViewInstance"]
            if(params.tempViewId)  {
                ClipboardCases instance = ClipboardCases.get(params.tempViewId as Long)
                if(instance?.isFirstUse || instance?.isUpdated) {
                    alertDataDTO.params.advancedFilterId = null
                    alertDataDTO.params.queryJSON = null
                    alertDataDTO.params.filterMap = [:]
                }
                instance?.isFirstUse = false
                instance?.isUpdated = false
                if(instance?.tempCaseIds){
                    instance?.caseIds = instance?.tempCaseIds
                    instance?.tempCaseIds = null
                }
                alertDataDTO.clipBoardCases = instance?.caseIds?.tokenize(',')
                instance?.save()
            }
            ViewInstance viewInstance = viewInstanceService.fetchSelectedViewInstance(params.alertType,params.viewId as Long)
            Map orderColumnMap = alertService.prepareOrderColumnMap(params)
            Map sort =  JSON.parse(viewInstance.sorting)
            def viewOrderColumnMap
            if(sort && viewInstanceCheck == "1"){
                orderColumnMap = [name: params["columns[${sort.keySet()[0]}][data]"], dir: sort.values()[0]]

            }
            alertDataDTO.orderColumnMap = orderColumnMap
            if (params.isFaers) {
                alertDataDTO.isFaers = params.getBoolean("isFaers", false)
            }
            if(params.isVaers){
                alertDataDTO.isVaers = params.getBoolean("isVaers", false)
            }
            if(params.isVigibase){
                alertDataDTO.isVigibase= params.getBoolean("isVigibase", false)
            }
            if(params.isJader){
                alertDataDTO.isJader= params.getBoolean("isJader", false)
            }
            Set dispositionSet = []
            if (params.callingScreen == Constants.Commons.DASHBOARD) {
                long time1 = System.currentTimeMillis()
                log.info("Fetching possible dispositions")
                dispositionSet = alertService.getDispositionSetDashboard(isFilterRequest,alertDataDTO)
                long time2 = System.currentTimeMillis()
                log.info(((time2 - time1) / 1000) + " Secs were taken in fetching possible dispositions ")
            } else {
                dispositionSet = alertService.getDispositionSet(alertDataDTO.executedConfiguration,alertService.generateDomainName(Boolean.parseBoolean(params.isArchived)), isFilterRequest,params)
            }

            long time1 = System.currentTimeMillis()
            log.info("Fetching grid data")
            Map filterCountAndList = alertService.getAlertFilterCountAndListICR(alertDataDTO, params.callingScreen, params.boolean('isArchived'))
            long time2 = System.currentTimeMillis()
            log.info(((time2 - time1) / 1000) + " Secs were taken in fetching grid data")
            if(!alertDataDTO.dispositionFilters?.isEmpty() || alertDataDTO.advancedFilterDispositions || params.isVaers) {
                List fullCaseList = filterCountAndList.fullCaseList
                List resultList = filterCountAndList.resultList.unique()
                List visibleIdList = resultList*.id
                resultMap = [recordsTotal: filterCountAndList.totalCount, recordsFiltered: filterCountAndList.totalFilteredCount, aaData: resultList, filters: dispositionSet, configId: id, fullCaseList: fullCaseList,visibleIdList: visibleIdList]}
            else {
                resultMap = [recordsTotal: filterCountAndList.totalCount, recordsFiltered: 0, aaData: [], filters: dispositionSet, configId: id,visibleIdList: []]
            }
            resultMap .put("advancedFilterDispName",alertDataDTO.advancedFilterDispName)
            resultMap.put("orderColumnMap", alertDataDTO.orderColumnMap)
        } catch (Throwable th) {
            th.printStackTrace()
        }
        def endTime=System.currentTimeSeconds()
        log.info("Total time taken to load list by executed config is ${endTime-startTime} seconds")
        render(resultMap as JSON)
    }


    boolean getAttachmentList(Long singleAlertId){
        SingleCaseAlert singleCaseAlert = null
        if(singleAlertId)
            singleCaseAlert = SingleCaseAlert.findById(singleAlertId)
        singleCaseAlert?.attachments?.count{it.id} as Boolean
    }

    def alertDetailListHeaders() {
        render([headers: [
                [title: "Checked", dataType: "string"],
                [title: "Flag", dataType: "strng"],
                [title: "P", dataType: "string"],
                [title: "Case(f/u#)", dataType: "string"],
                [title: "Receipt Date", dataType: "string"],
                [title: "Preferred Term", dataType: "string"],
                [title: "Listedness<br>Outcome", dataType: "string"],
                [title: "Workflow State", dataType: "string"],
                [title: "Signal / Topic", dataType: "string"],
                [title: "Disposition", dataType: "string"],
                [title: "Assigned To", dataType: "string"],
                [title: "Similar Events", dataType: "string"],
                [title: "Due In", dataType: "string"],
                [title: "History", dataType: "string"],
                [title: "Comments", dataType: "string"]

        ]] as JSON)
    }

    def exportSignalSummaryReport(Long id, Boolean cumulative, String callingScreen, String outputFormat) {
        List validatedSignalList = []
        List notStartedReviewSignalList = []
        List pendingReviewList = []
        List closedReviewList = []
        Map signalData = [:]
        ExecutedConfiguration ec = null
        List criteriaSheetList = []
        Group workflowGroup = userService.getUser()?.getWorkflowGroup()
        String defaultDispositionValue = workflowGroup?.defaultQualiDisposition?.value
        def scaList
        String timeZone = userService.getCurrentUserPreference()?.timeZone
        Boolean isDashboard = callingScreen == Constants.Commons.DASHBOARD ? true : false
        User user = userService.getUser()
        AlertDataDTO alertDataDTO = new AlertDataDTO()
        alertDataDTO.params = params
        alertDataDTO.timeZone = timeZone
        alertDataDTO.userId = userService.getCurrentUserId()
        alertDataDTO.cumulative = cumulative
        alertDataDTO.isFromExport = true
        def domainName = alertService.generateDomainName(params.boolean('isArchived'))

        if (cumulative || callingScreen == Constants.Commons.DASHBOARD) {
            List<ExecutedConfiguration> executedConfigurationList = ExecutedConfiguration.createCriteria().list {
                eq('workflowGroup', workflowGroup)
                eq("adhocRun", false)
                eq('type', Constants.AlertConfigType.SINGLE_CASE_ALERT)
            }
            List singleCaseAlertList = []
            singleCaseAlertList = domainName.createCriteria().list {
                if(executedConfigurationList) {
                    or {
                        executedConfigurationList.collate(1000).each {
                            'in'("executedAlertConfiguration", it)
                        }
                    }
                }
            }
            if(singleCaseAlertList) {
                singleCaseAlertList.each { SingleCaseAlert sca ->
                    if (sca.disposition.isValidatedConfirmed()) {
                        validatedSignalList << sca
                    } else if (sca.disposition.isClosed()) {
                        closedReviewList << sca
                    } else if (sca.disposition.value == defaultDispositionValue) {
                        notStartedReviewSignalList << sca
                    } else {
                        pendingReviewList << sca
                    }
                }
            }
            signalData = [alertName    : "", productName: "", dateRange: "",
                          otherCriteria: "", referenceNumber: "", cumulative: true]
        } else {
            ec = ExecutedConfiguration.findByIdAndIsEnabled(params.id, true)
            if (params.selectedCases) {
                String[] alertList = params.selectedCases.split(",")
                scaList = alertList.collect { it as Long }
            } else {
                Map filterMap = [:]
                if (params.filterList && params.filterList != "{}") {
                    def jsonSlurper = new JsonSlurper()
                    filterMap = jsonSlurper.parseText(params.filterList)
                }
                filterMap.each { def k, def v ->
                    if (k == 'assessSeriousness') {
                        filterMap.put('serious', filterMap.remove('assessSeriousness'))
                    }
                }
                List dispositionFilters = getFiltersFromParams(params.isFilterRequest?.toBoolean(), params)
                alertDataDTO.filterMap = filterMap
                alertDataDTO.executedConfiguration = ec
                alertDataDTO.execConfigId = ec?.id
                alertDataDTO.workflowGroupId = user.workflowGroup.id
                alertDataDTO.groupIdList = user.groups.collect { it.id }
                alertDataDTO.orderColumnMap = [name: params.column, dir: params.sorting]
                alertDataDTO.domainName = singleCaseAlertService.getDomainObject(params.boolean('isArchived'))
                alertDataDTO.dispositionFilters = dispositionFilters
                alertDataDTO.start = 0
                alertDataDTO.length = 5000

                scaList = alertService.getAlertFilterIdList(alertDataDTO)
            }

            List currentDispositionList = []
            List singleCaseAlertList = []
            if(scaList) {
                singleCaseAlertList = domainName.createCriteria().list {
                    or {
                        scaList.collate(1000).each {
                            'in'("id", it)
                        }
                    }
                }
            }
            if(singleCaseAlertList) {
                singleCaseAlertList.each { def sca ->
                    currentDispositionList.add(sca?.disposition.displayName)
                    if (sca.disposition.isValidatedConfirmed()) {
                        validatedSignalList << sca
                    } else if (sca.disposition.isClosed()) {
                        closedReviewList << sca
                    } else if (sca.disposition.value == defaultDispositionValue) {
                        notStartedReviewSignalList << sca
                    } else {
                        pendingReviewList << sca
                    }
                }
            }
            params.totalCount = scaList?.size()?.toString()
            def uniqueDispositions = currentDispositionList.toSet()
            String quickFilterDisposition = uniqueDispositions?.join(", ")
            params.quickFilterDisposition = quickFilterDisposition

            Configuration config = Configuration.findById(ec.configId)
            List dateRange = ec?.executedAlertDateRangeInformation?.getReportStartAndEndDate()
            def reportDateRange = (dateRange ? DateUtil.toDateString1(dateRange[0]) : "-") + " to " + (dateRange ? DateUtil.toDateString1(dateRange[1]) : "-")
            def referenceNumber = ec?.referenceNumber ?: "-"
            String otherCriteria = ec?.alertQueryName ?: '-'
            def productName = ec.getProductNameList().size() < 1 ? getGroupNameFieldFromJson(config.getProductGroupSelection()) : ec.getProductNameList()
            signalData = [alertName    : ec.name, productName: productName, dateRange: reportDateRange,
                          otherCriteria: otherCriteria, referenceNumber: referenceNumber, cumulative: false]
            //criteriaSheetList for criteria sheet data in export reports
            criteriaSheetList = singleCaseAlertService.getSingleCaseAlertCriteriaData(ec, params)
            if (params?.isCaseSeries?.toBoolean()) {
                criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.SOC, 'value': params.soc ?: ""])
                criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.PT, 'value': params.eventName ?: ""])
            }
            params.criteriaSheetList = criteriaSheetList
        }
        validatedSignalList = singleCaseAlertService.getSignalDetectionSummaryMap(validatedSignalList)
        notStartedReviewSignalList = singleCaseAlertService.getSignalDetectionSummaryMap(notStartedReviewSignalList)
        pendingReviewList = singleCaseAlertService.getSignalDetectionSummaryMap(pendingReviewList)
        closedReviewList = singleCaseAlertService.getSignalDetectionSummaryMap(closedReviewList)
        Map reportParamsMap = ["showCompanyLogo"  : true,
                               "showLogo"         : true,
                               "isSingleCaseAlert": true,
                               "header"           : "Signal Detection Summary for Individual Case Alert",
                               "outputFormat"     : outputFormat,
                               'criteriaSheetList': criteriaSheetList]
        File reportFile = dynamicReportService.createSignalDetectionReport(validatedSignalList ? new JRMapCollectionDataSource(validatedSignalList) : null,
                notStartedReviewSignalList ? new JRMapCollectionDataSource(notStartedReviewSignalList) : null,
                pendingReviewList ? new JRMapCollectionDataSource(pendingReviewList) : null,
                closedReviewList ? new JRMapCollectionDataSource(closedReviewList) : null,
                signalData, reportParamsMap)
        renderReportOutputType(reportFile, params)
        signalAuditLogService.createAuditForExport(criteriaSheetList, isDashboard == true ? Constants.Commons.DASHBOARD : ec.getInstanceIdentifierForAuditLog()+" : Detection Summary", isDashboard == true ? "Single Case Review Dashboard" : Constants.AuditLog.SINGLE_REVIEW + "${!ec.isLatest?": Archived Alert":""}",params,reportFile.name)
    }

    /**
     * Show the list of cases when similar events count is clicked.
     * @return
     */
    @Secured(['ROLE_SINGLE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_VIEWER','ROLE_JADER_CONFIGURATION'])
    def listCaseInfo(String executedConfigId, String eventType, String eventVal) {

        List listObj = []
        try {
            Boolean cumulative = userService.getCurrentUserPreference().isCumulativeAlertEnabled
            List<String> allowedProductsToUser = productBasedSecurityService.allAllowedProductForUser(userService.getUserFromCacheByUsername(userService.getCurrentUserName()))
            listObj = singleCaseAlertService.listCaseInfo(executedConfigId, eventType, eventVal, allowedProductsToUser, cumulative)
        } catch(Throwable th) {
            log.error(th.getMessage())
        }
        respond listObj, [formats: ['json']]
    }

    def generateCaseSeries() {
        def seriesName = params.seriesName
        def ec = ExecutedConfiguration.findById(params.id)
        def singleCaseAlertList = SingleCaseAlert.findAllByExecutedAlertConfiguration(ec)
        def caseData = []

        def caseSeriesDelimiter = Holders.config.caseSeries.bulk.addCase.delimiter ?: ":"
        singleCaseAlertList.each { sca ->
            caseData.add(sca.caseNumber + caseSeriesDelimiter + sca.caseVersion)
        }
        caseData = caseData.join(",")
        def response = reportExecutorService.generateCaseSeries(seriesName, caseData)
        response.status = response.status
        render response as JSON
    }

    def addCaseToSingleCaseAlert() {
        if (TextUtils.isEmpty(params.caseNumber) && !params.file || (params.caseNumber != null && TextUtils.isEmpty(params.justification)) || (params.file != null && TextUtils.isEmpty(params.justification))) {
            render([success: false, message: message(code: "app.error.fill.all.required")] as JSON)
            return
        }
        try {
            List<String> caseNumber = []
            List singleCaseAlertList = []
            ExecutedConfiguration executedConfig = ExecutedConfiguration.findById(params.executedConfigId)
            Configuration config = Configuration.findByNameAndOwner(executedConfig.name,executedConfig.owner)

            if (params.file) {
                List<String> caseNumberList = singleCaseAlertService.processExcelFile(params.file)
                if (caseNumberList) {
                    Map map = singleCaseAlertService.saveCaseSeriesInDB(caseNumberList, executedConfig, params.justification)
                    singleCaseAlertList = map.singleAlertCaseList
                    Map caseNumbersMap = map.caseNumbersMap
                    List singleAlertCaseList = singleCaseAlertService.removeDuplicateCases(singleCaseAlertList,executedConfig)
                    if (caseNumbersMap.validCaseNumber || caseNumbersMap.invalidCaseNumber) {
                        caseNumber = caseNumbersMap.validCaseNumber
                        String caseNumberRpt=cacheService.getRptFieldIndexCache('masterCaseNum')
                        def casesInsingleAlertCaseList = singleAlertCaseList?.collect { it."${caseNumberRpt}" }
                        if (singleAlertCaseList) {
                            singleCaseAlertService.createAlert(config, executedConfig, singleAlertCaseList, params.justification, true, true)
                            if (caseNumbersMap.invalidCaseNumber.size() > 0) {
                                createAuditForAddCase(executedConfig, casesInsingleAlertCaseList as List<String>, params.justification)
                                render([success: true, message: message(code: "singleCaseAlert.import.caseNumber.success", args: [caseNumberList.size(), singleAlertCaseList?.size(),
                                                                                                                                  caseNumbersMap.invalidCaseNumber.toString().replaceAll("[\\[\\]]", "")])] as JSON)
                            } else {
                                createAuditForAddCase(executedConfig,casesInsingleAlertCaseList as List<String>,params.justification)
                                render([success: true, message: message(code: "singleCaseAlert.import.caseNumber.successWithoutRejection", args: [caseNumberList.size(), singleAlertCaseList?.size()])] as JSON)
                            }
                        } else {
                            render([success: false, message: message(code: "singleCaseAlert.import.caseNumber.all.Duplicated", args: [caseNumberList.size()])] as JSON)
                        }
                    }
                } else {
                    render([success: false, message: message(code: "app.label.no.data.excel.error")] as JSON)
                }
            } else {
                if (!SingleCaseAlert.findByExecutedAlertConfigurationAndCaseNumber(executedConfig, params.caseNumber)) {
                    caseNumber.add(params.caseNumber)
                    Map map = singleCaseAlertService.saveCaseSeriesInDB(caseNumber as List<String>, executedConfig, params.justification)
                    singleCaseAlertList = map?.singleAlertCaseList
                    if (!singleCaseAlertList) {
                        render([success: false, message: message(code: "singleCaseAlert.invalid.caseNumber.data")] as JSON)
                    } else {
                        singleCaseAlertService.createAlert(config, executedConfig, singleCaseAlertList, params.justification, true, true)
                        createAuditForAddCase(executedConfig,caseNumber as List<String>,params.justification)
                        render([success: true, message: message(code: "singleCaseAlert.add.caseNumber.success")] as JSON)
                    }
                } else {
                    render([success: false, message: message(code: "singleCaseAlert.duplicate.caseNumber.data")] as JSON)
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace()
            render([success: false, message: 'Something unexpected happened at server'] as JSON)
        }
    }

    def createAuditForAddCase(def execConfig,def casesList,def justification){
        Map auditTrail = [:]
        auditTrail.category = AuditTrail.Category.INSERT.toString()
        auditTrail.entityValue = execConfig.getInstanceIdentifierForAuditLog()
        auditTrail.entityName = "singleCaseAlert"
        auditTrail.entityId = execConfig.id
        auditTrail.moduleName = Constants.AuditLog.SINGLE_REVIEW + (execConfig.isLatest ? "" : ": Archived Alert")
        auditTrail.description = "Added Case ${casesList.toString()}"
        List<Map> auditChildMap = []

        def childEntry = [:]
        childEntry = [
                propertyName: "Added Case Numbers",
                newValue    : casesList.toString()?.replaceAll("'","")]
        auditChildMap << childEntry
        childEntry = [
                propertyName: "Justification",
                newValue    : justification]
        auditChildMap << childEntry
        signalAuditLogService.createAuditLog(auditTrail, auditChildMap)
    }

    def upload() {
        def alertId = params?.alertId
        def domain = alertService.generateDomainName(params.boolean('isArchived'))
        def singleCaseAlert = domain.findById(alertId.toInteger())
        def fileName = params?.attachments.filename
        if (fileName instanceof String) {
            fileName = [fileName]
        }
        params?.isAlertDomain=true
        User currentUser = userService.getUser()
        Map filesStatusMap = attachmentableService.attachUploadFileTo(currentUser, fileName, singleCaseAlert, request)
        String fileDescription = params.description
        List<Attachment> attachments = singleCaseAlert.getAttachments().sort { it.dateCreated }
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
            User loggedInUser = cacheService.getUserByUserId(userService.getCurrentUserId())
            def filenames = filesStatusMap?.uploadedFiles*.originalFilename.join(', ')
            Activity activity = activityService.createActivityBulkUpdate(cacheService.getActivityTypeByValue(ActivityTypeValue.AttachmentAdded.value),
                    loggedInUser, "Attachment ${filenames} is added with description '${fileDescription}'", null, ['For Case Number': singleCaseAlert.caseNumber],
                    singleCaseAlert.productName, singleCaseAlert.pt, singleCaseAlert.assignedTo, singleCaseAlert.caseNumber, singleCaseAlert.assignedToGroup)
            List<Map> activityExecConfIdsMap = activityService.batchPersistBulkUpdateActivity([[execConfigId: singleCaseAlert.executedAlertConfigurationId, activity: activity]])
            String insertExConfigActivityQuery = SignalQueryHelper.add_activity_for_exec_config()
            alertService.batchPersistBulkUpdateExecConfigActivityMapping(activityExecConfIdsMap, insertExConfigActivityQuery)
        }
        render(['success': true] as JSON)
    }

    def fetchAttachment(final Long alertId) {
        def attachments = []
        List<Long> scaAlertList = singleCaseAlertService.getAlertIdsForAttachments(alertId, params.boolean('isArchived'))
        String timezone = userService.user.preference.timeZone
        scaAlertList.each { Long scaAlertId ->
            def scaAlert = ArchivedSingleCaseAlert.get(scaAlertId) ?: SingleCaseAlert.get(scaAlertId)
            attachments += scaAlert.attachments.collect {
                [
                        id         : it.id,
                        name       : it.name,
                        description: AttachmentDescription.findByAttachment(it)?.description,
                        timeStamp  : DateUtil.stringFromDate(it.dateCreated, DateUtil.DEFAULT_DATE_TIME_FORMAT, timezone),
                        modifiedBy : AttachmentDescription.findByAttachment(it)?.createdBy
                ]
            }
        }

        respond attachments.unique(), [formats: ['json']]
    }

    def deleteAttachment(Long attachmentId, Long alertId) {
        try {
            def domain = alertService.generateDomainName(params.boolean('isArchived'))
            def singleCaseAlert = domain.get(alertId)
            Attachment attachment = Attachment.findById(attachmentId)
            String fileName = attachment.getFilename()
            if (attachment) {
                if (AttachmentDescription.findByAttachment(attachment)) {
                    AttachmentDescription.findByAttachment(attachment).delete()
                }
                attachmentableService.removeAttachment(attachmentId)
                activityService.createActivity(singleCaseAlert.executedAlertConfiguration, ActivityType.findByValue(ActivityTypeValue.AttachmentRemoved),
                        userService.getUser(), "Attachment " + fileName + " is removed", null,
                        [product: getNameFieldFromJson(singleCaseAlert.executedAlertConfiguration.productSelection), event: getNameFieldFromJson(singleCaseAlert.executedAlertConfiguration.eventSelection)],
                        singleCaseAlert.productName, singleCaseAlert.pt, singleCaseAlert.assignedTo, singleCaseAlert.caseNumber, singleCaseAlert.assignedToGroup)

            }
            render(['success': true] as JSON)
        }catch (Exception e) {
            render(['success': false] as JSON)
        }
    }

    def fetchRelatedCaseSeries() {
        def caseNumber = params.caseNumber
        int offset = params.start as int
        int max = params.length as int
        String searchKey = params.searchString
        def relatedCaseSeriesList = []
        int recordsTotal = 0
        int filteredCount = 0
        def orderColumn = params["order[0][column]"]
        Map orderColumnMap = [name: params["columns[${orderColumn}][data]"], dir: params["order[0][dir]"]]
        def resultMap = [recordsTotal: 0, recordsFiltered: 0, aaData: []]
        if (!TextUtils.isEmpty(caseNumber)) {
            if (searchKey) {
                relatedCaseSeriesList = singleCaseAlertService.getRelatedCaseSeries(caseNumber, offset, max, searchKey, orderColumnMap)
                filteredCount = singleCaseAlertService.getRelatedCaseSeriesTotalCount(caseNumber, searchKey)
            } else {
                relatedCaseSeriesList = singleCaseAlertService.getRelatedCaseSeries(caseNumber, offset, max, null, orderColumnMap)
                filteredCount = singleCaseAlertService.getRelatedCaseSeriesTotalCount(caseNumber)
            }
            recordsTotal = singleCaseAlertService.getRelatedCaseSeriesTotalCount(caseNumber)
            resultMap = [recordsTotal: recordsTotal, recordsFiltered: filteredCount, aaData: relatedCaseSeriesList]
        }
        render(resultMap as JSON)
    }


    def alertTagDetails() {
        List<MartTags> martTagsList
        MartTags.withTransaction {
            martTagsList = MartTags.list()
        }
        List alertTagList = martTagsList.findAll { it.isCaseSeriesTag }?.collect { it.name }
        List globalTagList = martTagsList.findAll { !it.isCaseSeriesTag }?.collect { it.name }
        render(["globalTagList": globalTagList, alertTagList: alertTagList] as JSON)

    }

    def saveAlertTags(Long alertId, Long execConfigId, String alertTags, String globalTags, String deletedCaseSeriesTags, String deletedGlobalTags, String addedGlobalTags, String addedCaseSeriesTags) {
        log.info("params : ${params}")
        def domain = params.boolean('isArchived') ? ArchivedSingleCaseAlert : SingleCaseAlert
        JsonSlurper jsonSlurper = new JsonSlurper()
        List caseSeriesTagList = jsonSlurper.parseText(alertTags) ?: []
        List globalTagList = jsonSlurper.parseText(globalTags) ?: []
        List deletedCaseSeriesTagList = jsonSlurper.parseText(deletedCaseSeriesTags) ?: []
        List deletedGlobalTagList = jsonSlurper.parseText(deletedGlobalTags) ?: []
        List addedCaseSeriesTagsList = jsonSlurper.parseText(addedCaseSeriesTags) ?: []
        List addedGlobalTagsList = jsonSlurper.parseText(addedGlobalTags) ?: []
        ResponseDTO responseDTO = new ResponseDTO(status: true, message: "Tags Saved Successfully.")
        String justification = params.justification
        List tagList = []
        User currentUser = userService.getUser()

        ExecutedConfiguration executedConfig = ExecutedConfiguration.findById(execConfigId)
        def singleCaseAlert = domain.findById(alertId)
        Configuration configuration = singleCaseAlert.alertConfiguration
        CaseHistory caseHistoryObj = caseHistoryService.getLatestCaseHistory(singleCaseAlert.caseNumber, configuration?.id)
        Map caseHistoryMap = getCaseHistoryMap(configuration.id, execConfigId, caseHistoryObj, singleCaseAlert, Constants.HistoryType.ALERT_TAGS, justification)
        singleCaseAlertService.saveTags(caseSeriesTagList?.join(','), globalTagList?.join(','), singleCaseAlert.getAttr(cacheService.getRptFieldIndexCache('masterCaseId')) as Long, executedConfig.pvrCaseSeriesId, Constants.CASE_SERIES_OWNER,null)
        if(singleCaseAlert.caseId) {
            log.info("reload tag started for case id: "+ singleCaseAlert.caseId)
            singleAlertTagService.reloadTags(executedConfig.pvrCaseSeriesId, singleCaseAlert.caseId)
            log.info("reload tag completed for case id: "+ singleCaseAlert.caseId)
        }
        ActivityType activityType = ActivityType.findByValue(ActivityTypeValue.CategoryAdded)
        (addedCaseSeriesTagsList + addedGlobalTagsList).each {
            activityService.createActivity(executedConfig, activityType,
                    userService.getUser(), "Category Added " + it, justification,
                    ['For Case Number': singleCaseAlert.caseNumber], singleCaseAlert.productName, singleCaseAlert.pt, singleCaseAlert.assignedTo, singleCaseAlert.caseNumber,
                    singleCaseAlert.assignedToGroup)
        }
        activityType = ActivityType.findByValue(ActivityTypeValue.CategoryRemoved)
        (deletedCaseSeriesTagList + deletedGlobalTagList).each {
            activityService.createActivity(executedConfig, activityType,
                    userService.getUser(), "Category Removed " + it, justification,
                    ['For Case Number': singleCaseAlert.caseNumber], singleCaseAlert.productName, singleCaseAlert.pt, singleCaseAlert.assignedTo, singleCaseAlert.caseNumber,
                    singleCaseAlert.assignedToGroup)
        }
        caseSeriesTagList.each { tagName ->
            tagList.add(["name": tagName, "type": Constants.Commons.CASE_SERIES_TAG] as JSON)
        }
        globalTagList.each { tagName ->
            tagList.add(["name": tagName, "type": Constants.Commons.GLOBAL_TAG] as JSON)
        }
        caseHistoryMap['tagName'] = tagList.toString()
        caseHistoryService.saveCaseHistory(caseHistoryMap)
        render(responseDTO as JSON)
    }

    def caseSeriesDetails(Long aggExecutionId, Long aggAlertId, String aggCountType, Integer ptCode, String type, String typeFlag, String domainName) {
        Boolean isFaers = false
        Boolean isVaers = false
        Boolean isVigibase = false
        Boolean isJader = false
        Boolean isEventGroup
        BigInteger productId = params.productId as BigInteger
        int noOfCases = Holders.config.caseSeries.limitNoOfCases
        Boolean isCaseSeriesGenerating = false
        Long execId = 0L
        Long caseSeriesId = 0L
        def domain = Boolean.parseBoolean(params.isArchived) ? ArchivedAggregateCaseAlert : AggregateCaseAlert
        ExecutedConfiguration executedConfigurationInstance = ExecutedConfiguration.findById(aggExecutionId)
        if (executedConfigurationInstance?.adhocRun)
            domain = AggregateOnDemandAlert
        def aggAlert = domain.findById(aggAlertId)
        String fullSoc = dataObjectService.getAbbreviationMap(aggAlert?.soc)
        try {
            //check if cases already exist
            ExecutedConfiguration newCaseSeriesExConfig = ExecutedConfiguration.findByAggExecutionIdAndAggAlertIdAndAggCountTypeAndIsDeleted(aggExecutionId, aggAlertId, aggCountType, false)
            ExecutedConfiguration aggExConfig = ExecutedConfiguration.findById(aggExecutionId)

            if (!newCaseSeriesExConfig) {
                String selectedDatasource = aggExConfig.selectedDatasource.split(',')[0]
                String aggCountTypeLabel = aggCountType?.contains(Constants.CountsLabel.CUMM_LABEL) ? aggCountType?.replace(Constants.CountsLabel.CUMM_LABEL, Constants.CountsLabel.CUM_LABEL): aggCountType
                String newConfigName = aggExConfig.name + '_' + aggAlert.productName.replaceAll("/","_") + '_' + aggAlert.pt + '_' + aggCountTypeLabel + '(Case Series)' + "${System.currentTimeMillis()}"
                newConfigName = newConfigName.replaceAll("#","")
                Configuration newCaseSeriesConfig = singleCaseAlertService.saveCaseSeriesConfiguration(aggExConfig.configId, newConfigName, aggExecutionId, aggAlertId, aggCountType, selectedDatasource)

                newCaseSeriesExConfig = singleCaseAlertService.saveCaseSeriesExecutedConfig(aggExecutionId, newCaseSeriesConfig, selectedDatasource)

                if (aggExConfig.eventGroupSelection) {
                    isEventGroup = true
                } else {
                    isEventGroup = false
                }
                def caseInfo = aggregateCaseAlertService.caseDrillDown(type, typeFlag, aggExecutionId, productId, ptCode, selectedDatasource, aggExConfig.groupBySmq, aggAlert, isEventGroup, noOfCases, newCaseSeriesExConfig)

                Map caseInfoMap = [type              : type,
                                   typeFlag          : typeFlag,
                                   aggExecutionId    : aggExecutionId,
                                   productId         : productId,
                                   ptCode            : ptCode,
                                   selectedDataSource: selectedDatasource,
                                   groupBySmq        : aggExConfig.groupBySmq,
                                   aggAlert          : aggAlert,
                                   isEventGroup      : isEventGroup,
                                   configId          : newCaseSeriesConfig,
                                   exeConfigId       : newCaseSeriesExConfig,
                                   prevData          : caseInfo]
                int caseListSize

                if (caseInfo) {
                    singleCaseAlertService.createAlert(newCaseSeriesConfig, newCaseSeriesExConfig, caseInfo, Constants.Commons.DASH_STRING, false, true, type)
                    singleCaseAlertService.generateExecutedCaseSeriesAsync(newCaseSeriesExConfig, true)
                    caseListSize = caseInfo.size()
                    if (caseListSize == noOfCases) {
                        isCaseSeriesGenerating = true
                        caseInfoMap.put('isArchived',params.boolean('isArchived'))
                        singleCaseAlertService.parallelyCreateSCAlerts(caseInfoMap)
                    }
                }

                    //Manual audit
                    singleCaseAlertService.createAuditForCaseSeries(executedConfigurationInstance, aggAlert, aggCountTypeLabel, newCaseSeriesExConfig.selectedDatasource, params.numberOfCount ?: caseListSize)

            }
            else {
                singleCaseAlertService.syncAssignedAndShareWithFieldsParentAgg(newCaseSeriesExConfig,aggExConfig)
            }
            execId = newCaseSeriesExConfig.id
            if (newCaseSeriesExConfig.selectedDatasource == Constants.DataSource.FAERS) {
                isFaers = true
                caseSeriesId = newCaseSeriesExConfig.faersCaseSeriesId
            } else if (newCaseSeriesExConfig.selectedDatasource == Constants.DataSource.VAERS) {
                isVaers = true
            } else if (newCaseSeriesExConfig.selectedDatasource == Constants.DataSource.VIGIBASE) {
                isVigibase = true
            } else if (newCaseSeriesExConfig.selectedDatasource == Constants.DataSource.JADER) {
                isJader = true
            }else {
                caseSeriesId = newCaseSeriesExConfig.pvrCaseSeriesId
            }
        } catch (Throwable ex) {
            ex.printStackTrace()
            log.error(ex.getMessage())
        }
        if (execId == 0) {
            // Execution ID not set means error in DB connection
            redirect(action: "databaseError", controller: 'errors')
            return
        }
        log.info("The executed configuration id for the generated case series is : " + execId)
        redirect(action: "details", params: [callingScreen: Constants.Commons.REVIEW, configId: execId, isFaers: isFaers, isVaers: isVaers, isVigibase: isVigibase,isJader:isJader, isCaseSeries: true, isCaseSeriesGenerating: isCaseSeriesGenerating, productName: aggAlert.productName, eventName: aggAlert.pt, soc: fullSoc,isAggregateAdhoc:params.isAggregateAdhoc, aggExecutionId: aggExecutionId,type: type, typeFlag: typeFlag, version: params.version, aggAlertId: aggAlertId, primExecutionId: aggAlert.executedAlertConfiguration?.id])
    }

    @Transactional
    def saveCaseSeries(Long executedConfigId, String configName) {
        Map response = [success: true, message: message(code: "singleCaseAlert.add.caseNumber.success")]
        try {
            ExecutedConfiguration executedConfig = ExecutedConfiguration.findById(executedConfigId)
            Configuration config = Configuration.findByNameAndOwner(executedConfig.name, executedConfig.owner)
            List scaList = SingleCaseAlert.findAllByExecutedAlertConfiguration(executedConfig)
            Map execConfigCountsMap = scaList.countBy {it.dispositionId as String}
            List requiresReviewDispList = cacheService.getNotReviewCompletedDisposition().collect {it.id as String}
            int requiresReviewCount = 0
            Map requiresReviewCountMap = execConfigCountsMap?.findAll { it.key.toString() in requiresReviewDispList  }
            DashboardCountDTO dashboardCountDTO = alertService.prepareDashboardCountDTO(true)
            if(requiresReviewCountMap) {
                requiresReviewCount = requiresReviewCountMap.values().sum()
            }
            executedConfig.productName = reportExecutorService.generateProductName(executedConfig)
            executedConfig.requiresReviewCount = requiresReviewCount
            executedConfig.dispCounts = execConfigCountsMap ? new JsonBuilder(execConfigCountsMap).toPrettyString() : null
            executedConfig.isCaseSeries = false
            executedConfig.name = configName
            executedConfig.save(failOnError: true)
            config.numOfExecutions = config.numOfExecutions + 1
            config.isCaseSeries = false
            config.name = configName
            config.save(flush: true, failOnError: true)
            scaList.each {
                it.isCaseSeries = false
                it.name = configName
                it.save(failOnError: true)
            }
            alertService.generateCountsMap(scaList, dashboardCountDTO)
            activityService.createActivity(executedConfig, singleCaseAlertService.getActivityByType(ActivityTypeValue.CaseSeriesSaved),
                    userService.getUser(), "Case Series '$configName' saved", '',
                    [], params.productName, params.eventName, userService.getUser(), params.caseNumber)
            singleCaseAlertService.generateTempCaseSeries(configName, executedConfigId)
            alertService.updateDashboardCounts(dashboardCountDTO, alertService.mergeCountMaps)
        } catch (Exception ex) {
            ex.printStackTrace()
            log.error(ex.message)
            response = [success: false]
        }

        render(response as JSON)
    }

    private Map modelData(Configuration configurationInstance, String action) {
        Long cioms1Id = reportIntegrationService.getCioms1Id()
        boolean hasNormalAlertExecutionAccess = userService.hasNormalAlertExecutionAccess(Constants.AlertConfigType.SINGLE_CASE_ALERT)
        List<User> userList = User.findAllByEnabled(true).sort {
            it.fullName?.toUpperCase()
        }
        //If signal id comes, indicating that flow is coming from signal screen then product selection
        //will be filled from the data coming from signal.
        if (params.signalId) {
            ValidatedSignal validatedSignal = ValidatedSignal.get(Long.parseLong(params.signalId))
            configurationInstance.productSelection = validatedSignal.products
            configurationInstance.productGroupSelection = validatedSignal.productGroupSelection
            configurationInstance.isMultiIngredient = validatedSignal.isMultiIngredient
        }

        Set<String> sMQList = cacheService.getSMQList()
        List<Priority> priorityList = Priority.findAllByDisplay(true)
        Priority priority = Priority.findByDefaultPriority(true)
        Long byDefaultPriority = priority ? priority?.id : null
        List<Map> templateList = []
        if (grailsApplication.config.show.pvreports.templates) {
            templateList = reportIntegrationService.getTemplateList().collect { [id: it.id, name: it.name] }
        }
        [configurationInstance: configurationInstance, priorityList: priorityList,byDefaultPriority:byDefaultPriority, spotfireEnabled: Holders.config.signal.spotfire.enabled,
         userList             : userList, action: action, templateList: templateList, sMQList: sMQList, cioms1Id: cioms1Id,appType: Constants.AlertConfigType.SINGLE_CASE_ALERT,
         signalId             : params.signalId, productGroupList: productGroupService.fetchProductGroupsListByDisplay(true),
         isAutoAssignedTo     :configurationInstance.isAutoAssignedTo, isAutoSharedWith: configurationInstance.autoShareWithUser || configurationInstance.autoShareWithGroup?true:false,
         selectedCaseSeriesId:configurationInstance.alertCaseSeriesId?"{'id':'${configurationInstance.alertCaseSeriesId}','name':'${configurationInstance.alertCaseSeriesName}'}":"",selectedCaseSeriesText:configurationInstance.alertCaseSeriesName,
         hasNormalAlertExecutionAccess: hasNormalAlertExecutionAccess]
    }

    def fetchCaseSeries(String term, Integer page, Integer max) {
        Map items = [:]
        try{
            if (!max) {
                max = 30
            }
            if (!page) {
                page = 1
            }
            if (term) {
                term = term?.trim()
            }
            int offset = Math.max(page - 1, 0) * max
            List caseSeriesList = []
            int caseSeriesSize = 0
            String url = Holders.config.pvreports.url
            String path = Holders.config.pvreports.api.configure.caseSeries
            Map query = [offset: offset, max: max, term: term, username: userService.getCurrentUserName()]
            def response = reportIntegrationService.get(url, path, query)
            if(response.status == 200 && response?.data){
                List responseList = response.data.result
                caseSeriesSize = response.data.totalCount
                responseList.each{
                    caseSeriesList.add([id: "{'id':'${it.id}','name':'${it.text.replaceAll("'", "\\\\'")}'}",text: it.text])
                }
            }
            items = ["list": caseSeriesList,totalCount: caseSeriesSize]
        }catch (Exception e){
            e.printStackTrace()
            log.error("Some error occurred",e)
        }
        render(items as JSON)
    }

    @Secured(['ROLE_SINGLE_CASE_REVIEWER', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_REVIEWER','ROLE_JADER_CONFIGURATION'])
    def changeDisposition(String selectedRows, Disposition targetDisposition, String incomingDisposition,
                          String justification, String validatedSignalName, String productJson,Long signalId) {
        boolean isArchived = params.boolean('isArchived')
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        List<Long> multipleExecutedConfigIdList = JSON.parse(selectedRows).collect { Map<String, Long> selectedRow ->
            selectedRow["executedConfigObj.id"] != "" ? selectedRow["executedConfigObj.id"] as Long : null
        }.unique()
        if (multipleExecutedConfigIdList.size() > 1) {
            responseDTO.status = false
            responseDTO.message = message(code: "multiple.alert.disposition.change.error.cases")
            render(responseDTO as JSON)
            return
        }
        try {
            validatedSignalName = validatedSignalName? org.apache.commons.lang.StringEscapeUtils.unescapeHtml(validatedSignalName) : ''
            List<Long> singleCaseAlertIdList = JSON.parse(selectedRows).collect { Map<String, Long> selectedRow ->
                selectedRow["alert.id"]!=""?selectedRow["alert.id"] as Long:null
            }
            Map responseMap = singleCaseAlertService.changeDisposition(singleCaseAlertIdList, targetDisposition, incomingDisposition, justification, validatedSignalName, productJson,isArchived,signalId)
            if (targetDisposition?.id) {
                singleCaseAlertService.persistDispositionDueDate(responseMap.alertDueDateList, responseMap.domain)
            }
            responseDTO.data =[:]
            if (responseMap.attachedSignalData) {
                responseDTO.data << [attachedSignalData: responseMap.attachedSignalData,signal:responseMap.signal]
            }
            def domain = alertService.generateDomainName(isArchived)
            Long configId = domain?.get(singleCaseAlertIdList[0])?.executedAlertConfiguration?.id
            Long countOfPreviousDisposition
            if(params.callingScreen != Constants.Commons.DASHBOARD){
                countOfPreviousDisposition = alertService.fetchPreviousDispositionCount(configId, incomingDisposition, domain)
            }else{
                countOfPreviousDisposition = alertService.fetchPreviousDispositionCountDashboard(incomingDisposition, domain)
            }
            responseDTO.data << [incomingDisposition:incomingDisposition, countOfPreviousDisposition:countOfPreviousDisposition]
            if(!responseMap.dispositionChanged && !responseMap.attachedSignalData){
                responseDTO.status = false
                responseDTO.message = message(code: "app.label.disposition.change.error.refresh")
            }
        }catch (grails.validation.ValidationException vx) {
            responseDTO.message = MiscUtil.getCustomErrorMessageList(vx)[0]
            responseDTO.status = false
            log.error("Exception is : ${vx}")
        }  catch (Exception e) {
            e.printStackTrace()
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.disposition.change.error")
        }
        render(responseDTO as JSON)
    }
    def revertDisposition(Long id, String justification) {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            responseDTO.data = singleCaseAlertService.revertDisposition(id, justification)
            Long configId = SingleCaseAlert.get(id)?.executedAlertConfiguration?.id
            singleCaseAlertService.persistDispositionDueDate(responseDTO.data.alertDueDateList, responseDTO.data.domain)
            Long countOfPreviousDisposition
            if (params.callingScreen != Constants.Commons.DASHBOARD) {
                countOfPreviousDisposition = alertService.fetchPreviousDispositionCount(configId, responseDTO?.data?.oldDispName, SingleCaseAlert)
            } else {
                countOfPreviousDisposition = alertService.fetchPreviousDispositionCountDashboard(responseDTO?.data?.oldDispName, SingleCaseAlert)
            }
            responseDTO.data << [incomingDisposition: responseDTO?.data?.oldDispName, targetDisposition: responseDTO?.data?.newDispName, countOfPreviousDisposition: countOfPreviousDisposition]
            if(!responseDTO.data.dispositionReverted){
                responseDTO.status = false
                responseDTO.message = message(code: "app.label.undo.disposition.change.error.refresh")
            }
        }catch (grails.validation.ValidationException vx) {
            responseDTO.message = MiscUtil.getCustomErrorMessage(vx)
            responseDTO.status = false
            log.error("Exception is : ${vx}")
        }  catch (Exception e) {
            e.printStackTrace()
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.undo.disposition.change.error")
        }
        render(responseDTO as JSON)
    }

    def searchTagsList(String term, Integer page, Integer max, Boolean isCaseSeriesTag) {
        List items = []
        Integer totalCount = 0
        try {
            if (!max) {
                max = 30
            }
            if (!page) {
                page = 1
            }
            if (!term || term == 'null') {
                term = ""
            } else {
                term = term?.trim()
            }
            int offset = Math.max(page - 1, 0) * max

            List<MartTags> martTagsList
            MartTags.withTransaction {
                martTagsList = MartTags.findAllByIsCaseSeriesTagAndNameIlike(isCaseSeriesTag, "%${term}%", [max: max, offset: offset, sort: "name", order: "desc"])
            }
            List alertTagList = martTagsList.collect { it.name }
            items = alertTagList.collect {
                [id: it, text: it]
            }
            MartTags.withTransaction {
                totalCount = MartTags.countByIsCaseSeriesTagAndNameIlike(isCaseSeriesTag, "%${term}%")
            }
        } catch (Exception e) {
            e.printStackTrace()
            log.error("Some error occurred", e)
        }
        render([list: items, totalCount: totalCount] as JSON)
    }

    @Secured(['ROLE_SINGLE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_AGGREGATE_CASE_VIEWER','ROLE_JADER_CONFIGURATION'])
    def archivedAlert(Long id) {
        Map executedConfigurationList = archivedAlertList(id, params, false)
        render(executedConfigurationList as JSON)
    }

    def fetchPossibleValues(Long executedConfigId) {
        Map<String, List> possibleValuesMap = [:]
        ExecutedConfiguration exConfig = ExecutedConfiguration.get(executedConfigId)
        Map possibleValuesMapFromConfig = Holders.config.advancedFilter.possible.values.map
        List blindingValuesList = [Constants.BlindingStatus.BLINDED,Constants.BlindingStatus.REDACTED]
        Map cachedReportFields = [:]
        if(exConfig?.selectedDatasource != Constants.DataSource.JADER ) {
            alertService.preparePossibleValuesMap(SingleCaseAlert, possibleValuesMap, executedConfigId)
            String lang = "en"
            cachedReportFields = reportFieldService.getSelectableValuesForFields(lang)
            possibleValuesMap.put("listedness", cachedReportFields.get("assessListedness")?:[] + blindingValuesList)
            possibleValuesMap.put("caseInitReceiptDate", "")
            possibleValuesMap.put("lockedDate", "")
            possibleValuesMap.put("serious", cachedReportFields.get("assessSeriousness")?:[] + blindingValuesList)
            possibleValuesMap.put("caseReportType", cachedReportFields.get("masterRptTypeId")?:[] + blindingValuesList)
            possibleValuesMap.put("reportersHcpFlag", cachedReportFields.get("CsHcpFlag")?:[] + blindingValuesList)
            possibleValuesMap.put("death", cachedReportFields.get("masterFatalFlag")?:[] + blindingValuesList)
            possibleValuesMap.put("rechallenge", [Constants.Commons.POSITIVE, Constants.Commons.NEGATIVE] + blindingValuesList)
            possibleValuesMap.put("caseType", cachedReportFields.get("cmadlflagEligibleLocalExpdtd")?:[] + blindingValuesList)
            possibleValuesMap.put("compoundingFlag", cachedReportFields.get("vwcpai1FlagCompounded")?:[] + blindingValuesList)
            possibleValuesMap.put("dueIn", "")
            possibleValuesMap.put("currentRun", ["Yes", "No"] + blindingValuesList)
            possibleValuesMap.put("malfunction", ["Yes", "No"] + blindingValuesList)
            possibleValuesMap.put("comboFlag", ["Yes", "No"] + blindingValuesList)
            possibleValuesMap.put("isSusar", ["Yes", "No"] + blindingValuesList)
            possibleValuesMapFromConfig.each { key, value ->
                possibleValuesMap.put(key, cacheService.getPossibleValuesByKey(key)?:[] + blindingValuesList)
            }
        }
        if(exConfig?.selectedDatasource ==  Constants.DataSource.JADER){
            List<String> ageList = singleCaseAlertService.getDistinctValues(SingleCaseAlert, executedConfigId, "age")
            List<String> deathList = singleCaseAlertService.getDistinctValues(SingleCaseAlert, executedConfigId, "death")
            List<String> hcpList = singleCaseAlertService.getDistinctValues(SingleCaseAlert, executedConfigId, "reportersHcpFlag")
            List<String> rechallengeList = singleCaseAlertService.getDistinctValues(SingleCaseAlert, executedConfigId, "rechallenge")
            List<String> reportTypeList = singleCaseAlertService.getDistinctValues(SingleCaseAlert, executedConfigId, "caseReportType")
            possibleValuesMap.put("age", ageList.unique{it.toUpperCase()})
            possibleValuesMap.put("death", deathList.unique{it.toUpperCase()})
            possibleValuesMap.put("reportersHcpFlag", hcpList.unique{it.toUpperCase()})
            possibleValuesMap.put("rechallenge", rechallengeList.unique{it.toUpperCase()})
            possibleValuesMap.put("caseReportType", reportTypeList.unique{it.toUpperCase()})
        }else if(exConfig?.selectedDatasource ==  Constants.DataSource.VIGIBASE){
            List<String> ageList = singleCaseAlertService.getDistinctValues(SingleCaseAlert, executedConfigId, "age")
            possibleValuesMap.put("age", ageList.unique{it.toUpperCase()})
        } else{
            possibleValuesMap.put("age", cachedReportFields.get("patInfoAgeGroupId")?:[] + blindingValuesList)
        }
        List<String>genders=[]
        List<String> outcomes =[]
        List<String> regions = []
        List<String> deaths = []
        if(executedConfigId>0){
            genders = singleCaseAlertService.getDistinctValues(SingleCaseAlert, executedConfigId, "gender")
            outcomes= singleCaseAlertService.getDistinctValues(SingleCaseAlert, executedConfigId, "outcome")
            regions = singleCaseAlertService.getDistinctValues(SingleCaseAlert, executedConfigId, "region")
            deaths = singleCaseAlertService.getDistinctValues(SingleCaseAlert, executedConfigId, "death")

            possibleValuesMap.put("gender", genders.unique{it.toUpperCase()})
            possibleValuesMap.put("outcome", outcomes.unique{it.toUpperCase()})
            possibleValuesMap.put("region", regions.unique{it.toUpperCase()})
            possibleValuesMap.put("death", deaths.unique{it.toUpperCase()})
        }else{
            genders=singleCaseAlertService.getDistinctOutcomeValuesSql("gender")
            outcomes=singleCaseAlertService.getDistinctOutcomeValuesSql("outcome")
            regions=singleCaseAlertService.getDistinctOutcomeValuesSql("region")
            deaths =singleCaseAlertService.getDistinctOutcomeValuesSql("death")

            possibleValuesMap.put("gender", genders)
            possibleValuesMap.put("region", regions)
            possibleValuesMap.put("outcome", outcomes)
            possibleValuesMap.put("death", deaths)
        }

        List<Map> codeValues = pvsGlobalTagService.fetchTagsAndSubtags()
        codeValues.each{
            it.id = it.text
        }
        List<Map> tags = pvsGlobalTagService.fetchTagsfromMart(codeValues)
        List<Map> subTags = pvsGlobalTagService.fetchSubTagsFromMart(codeValues)
        possibleValuesMap.put("tags" , tags.unique{it.text.toUpperCase()})
        possibleValuesMap.put("subTags" , subTags.unique{it.text.toUpperCase()})
        List customFields = [Constants.CustomQualitativeFields.APP_TYPE_AND_NUM,
                             Constants.CustomQualitativeFields.IND_NUM,
                             Constants.CustomQualitativeFields.CASE_TYPE,
                             Constants.CustomQualitativeFields.COMPLETENESS_SCORE,
                             Constants.CustomQualitativeFields.COMPOUNDING_FLAG,
                             Constants.CustomQualitativeFields.MED_ERR_PT_LIST,
                             Constants.CustomQualitativeFields.SUBMITTER,
                             Constants.CustomQualitativeFields.CROSS_REFERENCE_IND]

        if(!Holders.config.custom.qualitative.fields.enabled){
            customFields.each{customValue ->
                possibleValuesMap.remove{it.key == customValue}
            }
        }
        render possibleValuesMap as JSON
    }

    def fetchAllFieldValues(boolean isFaers, boolean isVaers, boolean isVigibase, boolean isJader) {
        List<Map> fieldValues = []
        if(isFaers){
            fieldValues = grailsApplication.config.signal.scaFaersColumnList.clone() as List<Map>
        }else if(isJader){
            fieldValues = grailsApplication.config.signal.scaJaderColumnList.clone() as List<Map>
        }
        else{
            fieldValues = grailsApplication.config.signal.scaColumnList.clone() as List<Map>
        }
        List customFields = [Constants.CustomQualitativeFields.APP_TYPE_AND_NUM,
                             Constants.CustomQualitativeFields.IND_NUM,
                             Constants.CustomQualitativeFields.CASE_TYPE,
                             Constants.CustomQualitativeFields.COMPLETENESS_SCORE,
                             Constants.CustomQualitativeFields.COMPOUNDING_FLAG,
                             Constants.CustomQualitativeFields.SUBMITTER,
                             Constants.CustomQualitativeFields.PRE_ANDA,
                             Constants.CustomQualitativeFields.MED_ERR_PT_LIST,
                             Constants.CustomQualitativeFields.PAI_ALL_LIST,
                             Constants.CustomQualitativeFields.PRIM_SUSP_PAI_LIST,
                             Constants.CustomQualitativeFields.CROSS_REFERENCE_IND,
                             Constants.CustomQualitativeFields.MED_ERRS_PT
                            ]
        if(!Holders.config.custom.qualitative.fields.enabled){
            customFields.each{customValue ->
                fieldValues.removeAll{it.name == customValue}}
        }
        Map rptToSignalFieldMap = Holders.config.icrFields.rptMap
        Map rptToUiLabelMap = cacheService.getRptToUiLabelMapForSafety() as Map
        // check if this is used in advance filter label fetching
        // change is only for safety
            fieldValues.each {
                if(!isFaers && !isVaers && !isVigibase && !isJader){
                    if(rptToSignalFieldMap.get(it.get("name"))!=null && rptToUiLabelMap.get(rptToSignalFieldMap.get(it.get("name")))!=null){
                        it.display = rptToUiLabelMap.get(rptToSignalFieldMap.get(it.get("name")))?:it.display
                    }
                    if(it.get("name")=='suspectProductList' && rptToUiLabelMap.get('masterSuspProdAgg')!=null){
                        it.display = rptToUiLabelMap.get('masterSuspProdAgg')?:it.display
                    }
                    else if(it.get("name")=='allPtList' && rptToUiLabelMap.get('masterPrefTermAll')!=null){
                        it.display = rptToUiLabelMap.get('masterPrefTermAll')?:it.display
                    }
                    else if(it.get("name")=='ptList' && rptToUiLabelMap.get('masterPrefTermSurAll')!=null){
                        it.display = rptToUiLabelMap.get('masterPrefTermSurAll')?:it.display
                    }
                    else if(it.get("name")=='primSuspProdList' && rptToUiLabelMap.get('masterPrimProdName')!=null){
                        it.display = rptToUiLabelMap.get('masterPrimProdName')?:it.display
                    }
                    else if(it.get("name")=='conComitList' && rptToUiLabelMap.get('masterConcomitProdList')!=null){
                        it.display = rptToUiLabelMap.get('masterConcomitProdList')?:it.display
                    }
                }
                if(it.get("name") == 'rechallenge'){
                    it["isAutocomplete"] = true
                }
        }
        render fieldValues as JSON
    }

    Map getAssignedToChangeMap(SingleCaseAlert singleCaseAlert){
      [
                "currentAssignedTo"     : singleCaseAlert.assignedTo,
                "currentAssignedToGroup": singleCaseAlert.assignedToGroup,
                "change"                : Constants.HistoryType.ASSIGNED_TO
      ]
    }

    Map getCaseHistoryMap(Long configId, Long exConfigId, CaseHistory caseHistoryObj, SingleCaseAlert singleCaseAlert, String historyType, String justification){
        [
                "configId"              : configId,
                "singleAlertId"         : singleCaseAlert?.id,
                "currentDisposition"    : caseHistoryObj ? caseHistoryObj.currentDisposition : singleCaseAlert.disposition,
                "currentPriority"       : singleCaseAlert.priority,
                "caseNumber"            : singleCaseAlert.caseNumber,
                "caseVersion"           : singleCaseAlert.caseVersion,
                "productFamily"         : singleCaseAlert.productFamily,
                "currentAssignedTo"     : caseHistoryObj ? caseHistoryObj.currentAssignedTo : singleCaseAlert.assignedTo,
                "currentAssignedToGroup": singleCaseAlert.assignedToGroup,
                "execConfigId"          : exConfigId,
                "followUpNumber"        : singleCaseAlert.followUpNumber,
                "justification"         : justification ?: Constants.Commons.BLANK_STRING,
                "tagName"               : singleCaseAlertService.getAlertTagNames(singleCaseAlert),
                "change"                : historyType
        ]
    }

    def changeAlertLevelDisposition(Disposition targetDisposition,String justificationText,ExecutedConfiguration execConfig, Boolean isArchived){
        def domain = alertService.generateDomainName(isArchived)
        String alertName = execConfig?.name
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true, message: message(code: "alert.level.disposition.successfully.updated"))
        try {
            AlertLevelDispositionDTO alertLevelDispositionDTO = dispositionService.populateAlertLevelDispositionDTO(targetDisposition, justificationText, domain, execConfig)
            Integer updatedRowsCount = singleCaseAlertService.changeAlertLevelDisposition(alertLevelDispositionDTO, isArchived)
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
        } catch (grails.validation.ValidationException vx) {
            responseDTO.message = MiscUtil.getCustomErrorMessage(vx)
            responseDTO.status = false
            log.error("Exception is : ${vx}")
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex)
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.disposition.change.error")
        }
        render(responseDTO as JSON)
    }

    def isWarningMessageInAutoRouteDisposition(Long id, Boolean isArchived){
        def singleCaseAlert = isArchived ? ArchivedSingleCaseAlert.get(id) : SingleCaseAlert.get(id)
        ResponseDTO responseDTO = new ResponseDTO(status: false)
        Group workflowGroup = singleCaseAlert.executedAlertConfiguration?.owner?.workflowGroup
        responseDTO.status = workflowGroup?.autoRouteDisposition && (singleCaseAlert.disposition == workflowGroup.defaultQualiDisposition)
        render(responseDTO as JSON)
    }

    def updateAutoRouteDisposition(Long id, Boolean isArchived) {
        def singleCaseAlert = isArchived ? ArchivedSingleCaseAlert.get(id) : SingleCaseAlert.get(id)
        User currentUser = userService.getUser()
        ResponseDTO responseDTO = new ResponseDTO(status: false)
        if (singleCaseAlert) {
            Group workflowGroup = singleCaseAlert.executedAlertConfiguration?.owner?.workflowGroup
            Boolean isAutoRouteDisposition = workflowGroup.autoRouteDisposition && (singleCaseAlert.disposition == workflowGroup.defaultQualiDisposition)
            Boolean isStandalone = singleCaseAlert.executedAlertConfiguration?.isStandalone?:false
            Boolean isJader = params?.isJader == 'true' ? true : false
            if (isAutoRouteDisposition && !isStandalone && !isJader) {
                try {
                    DashboardCountDTO dashboardCountDTO = alertService.prepareDashboardCountDTO(true)
                    boolean isTargetDispReviewed = workflowGroup.autoRouteDisposition.reviewCompleted
                    Disposition newDisposition = workflowGroup.autoRouteDisposition
                    String prevDueDate = singleCaseAlert.dueDate ? DateUtil.stringFromDate(singleCaseAlert.dueDate, "dd-MM-yyyy", "UTC") : null
                    Disposition previousDisposition = singleCaseAlert.disposition
                    ExecutedConfiguration ec = singleCaseAlert.executedAlertConfiguration
                    singleCaseAlert.disposition = workflowGroup.autoRouteDisposition
                    calcDueDate(singleCaseAlert, singleCaseAlert.priority, singleCaseAlert.disposition,true,
                            cacheService.getDispositionConfigsByPriority(singleCaseAlert.priority.id))
                    log.info("Update Auto Route Disposition from ${previousDisposition?.displayName} to ${singleCaseAlert.disposition?.displayName}")
                    String justification = workflowGroup.forceJustification ? workflowGroup.justificationText : ""
                    singleCaseAlertService.saveAlertCaseHistory(singleCaseAlert, justification)
                    CRUDService.save(singleCaseAlert)
                    Date lastDispChange = singleCaseAlert.dispLastChange
                    singleCaseAlertService.createActivityForDispositionChange(singleCaseAlert, previousDisposition, singleCaseAlert.disposition, justification, ec, currentUser,null, lastDispChange)
                    singleCaseAlertService.createCaseHistoryForDispositionChange(singleCaseAlert, justification, ec, singleCaseAlert.alertConfiguration, null, isArchived)
                    if(emailNotificationService.emailNotificationWithBulkUpdate(null, Constants.EmailNotificationModuleKeys.DISPOSITION_AUTO_ROUTE_SCA)) {
                        emailNotificationService.mailHandlerForAutoRouteDispSCA(singleCaseAlert, previousDisposition, ec,isArchived)
                    }

                    //For Dashboard Counts
                    if(!isArchived && singleCaseAlert.assignedToId) {
                        alertService.updateDashboardCountMaps(dashboardCountDTO.prevDispCountMap, singleCaseAlert.assignedToId, previousDisposition.id.toString())
                        alertService.updateDashboardCountMaps(dashboardCountDTO.userDispCountsMap, singleCaseAlert.assignedToId, newDisposition.id.toString())
                        if(prevDueDate && isTargetDispReviewed ) {
                            alertService.updateDashboardCountMaps(dashboardCountDTO.userDueDateCountsMap, singleCaseAlert.assignedToId, prevDueDate)
                        }
                    } else if(!isArchived) {
                        alertService.updateDashboardCountMaps(dashboardCountDTO.prevGroupDispCountMap, singleCaseAlert.assignedToGroupId, previousDisposition.id.toString())
                        alertService.updateDashboardCountMaps(dashboardCountDTO.groupDispCountsMap, singleCaseAlert.assignedToGroupId, newDisposition.id.toString())
                        if (prevDueDate && isTargetDispReviewed) {
                            alertService.updateDashboardCountMaps(dashboardCountDTO.groupDueDateCountsMap, singleCaseAlert.assignedToGroupId, prevDueDate)
                        }
                    }
                    //For Top 50 alert widget
                    alertService.updateDashboardCountMaps(dashboardCountDTO.execDispCountMap, singleCaseAlert.executedAlertConfigurationId, newDisposition.id.toString())

                    alertService.updateDashboardCountsForDispChange(dashboardCountDTO, isTargetDispReviewed)
                    alertService.updateDashboardGroupCountsForDispChange(dashboardCountDTO, isTargetDispReviewed)
                    alertService.updateDispCountsForExecutedConfiguration(dashboardCountDTO.execDispCountMap, dashboardCountDTO.prevDispCountMap)

                    responseDTO.status = true
                    responseDTO.data = singleCaseAlert.disposition?.displayName
                } catch (Throwable th) {
                    log.error(th.getMessage(),th)
                    responseDTO.message = "Some exception occured while updating Auto Route Disposition"
                }
            } else if(isStandalone) {
                responseDTO.status = true
                responseDTO.data = "isStandalone"
            } else if(isJader){
                responseDTO.status = true
                responseDTO.data = "isJader"
            }
        }
        render(responseDTO as JSON)
    }

    @Secured(['ROLE_SHARE_GROUP','ROLE_SHARE_ALL'])
    def editShare() {
        Boolean sharedWithoutException = true
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(Long.parseLong(params.executedConfigId))
        try {
            editShareWith()
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

    def bindAsOfVersionDateOfSingleCase(Configuration configuration) {
        if (configuration.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF) {
            configuration.asOfVersionDate = DateUtil.getAsOfVersion(params.asOfVersionDate, "UTC")
        } else {
            configuration.asOfVersionDate = null
        }
    }

    private Integer getDefaultReviewPeriod(Configuration config){
        Disposition defaultDisposition = userService.getUser().workflowGroup.defaultQualiDisposition
        List<PriorityDispositionConfig> dispositionConfigs = cacheService.getDispositionConfigsByPriority(config.priority.id)
        Integer reviewPeriod = dispositionConfigs?.find{it.disposition == defaultDisposition}?.reviewPeriod
        reviewPeriod?: config.priority.reviewPeriod
    }

    def exportCaseForm() {
        caseFormService.initializeGeneration(params, userService.getUser())
        render([status:200] as JSON)
    }

    def fetchCaseForms(String execConfigId) {
        List caseForms = caseFormService.listCaseForms(execConfigId as Long)
        render(caseForms as JSON)
    }

    def fetchCaseFormNames(String execConfigId) {
        List<String> caseForms = caseFormService.listCaseFormNames(execConfigId as Long)
        render([names:caseForms] as JSON)
    }

    def downloadCaseForm(Long id) {
        Long idx = params.id as Long
        String outputFormat = params.outputFormat as String
        Map formMap = caseFormService.fetchFile(idx,outputFormat)
        String reportName = formMap.filename
        File file = formMap.file
        String fileExtension = FilenameUtils.getExtension(file.name)
        response.contentType = "$fileExtension charset=UTF-8"
        response.contentLength = file.length()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}" + "\"")
        response.getOutputStream().write(file.bytes)
        response.outputStream.flush()
        signalAuditLogService.createAuditForExport(params?.containsKey('criteriaSheetList') ? params.criteriaSheetList : null, formMap?.alertName + " : " + formMap?.filename, formMap.isAdhoc == "true" ? "Adhoc Individual Case Review: Case Form" : "Individual Case Review${!formMap.isLatest ? ": Archived Alert" : ""}: Case Form", params, file.name)
    }

    def changeFilterAttributes(){
        HttpSession session = request.getSession()
        session.removeAttribute("selectedAlertsFilter")
        session.setAttribute("icr",params["selectedAlertsFilter"])
        render([status:200] as JSON)
    }
}
