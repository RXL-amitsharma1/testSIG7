package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.FrequencyEnum
import com.rxlogix.enums.QueryOperatorEnum
import com.rxlogix.signal.ProductTypeConfiguration
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.RelativeDateConverter
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import grails.validation.ValidationException
import groovy.json.JsonSlurper
import org.apache.commons.lang3.time.DateUtils
import org.hibernate.SQLQuery
import org.hibernate.Session

import static org.springframework.http.HttpStatus.*

@Secured(["isAuthenticated()"])
class ConfigurationController {

    def configurationService
    def UserService
    def SpringSecurityService
    def messageSource
    def CRUDService
    def reportExecutorService
    def evdasAlertService
    def literatureAlertService
    def masterExecutorService
    def sessionFactory
    def alertService
    def appAlertProgressStatusService

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_CONFIGURATION',
            'ROLE_LITERATURE_CASE_CONFIGURATION', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def index() {}

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def copy(Configuration originalConfig) {
        if (!originalConfig) {
            notFound()
            return
        }
        User currentUser = userService.getUser()
        def savedConfig = configurationService.copyConfig(originalConfig, currentUser)
        if (savedConfig.hasErrors()) {
            chain(action: "index", model: [error: savedConfig])
        } else {
            flash.message = message(code: "app.copy.success", args: [originalConfig.name])
            redirect(action: "view", id: savedConfig.id)
        }
    }

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def delete(Configuration config) {

        if (!config) {
            notFound()
            return
        }
        User currentUser = userService.getUser()
        if (config?.isEditableBy(currentUser) ||
                (config.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT && SpringSecurityUtils.ifAllGranted("ROLE_AGGREGATE_CASE_CONFIGURATION, ROLE_EXECUTE_SHARED_ALERTS")) ||
                (config.type == Constants.AlertConfigType.SINGLE_CASE_ALERT && SpringSecurityUtils.ifAllGranted("ROLE_SINGLE_CASE_CONFIGURATION, ROLE_EXECUTE_SHARED_ALERTS"))) {
            def deletedConfig = configurationService.deleteConfig(config)
            if (deletedConfig.hasErrors()) {
                chain(action: "index", model: [error: deletedConfig])
            }
            flash.message = message(code: "app.configuration.alert.delete.success", args: [config.name])
        } else {
            flash.warn = message(code: "app.configuration.delete.fail", args: [config.name])
        }
        redirect(action: "index")
    }

    @Secured(['ROLE_EVDAS_CASE_CONFIGURATION'])
    def deleteEvdas(EvdasConfiguration config) {
        if (!config) {
            notFound()
            return
        }
        User currentUser = userService.getUser()
        if (config.isEditableBy(currentUser) ||
                SpringSecurityUtils.ifAllGranted("ROLE_EVDAS_CASE_CONFIGURATION, ROLE_EXECUTE_SHARED_ALERTS")) {
            def deletedConfig = evdasAlertService.deleteEvdasConfig(config)
            if (deletedConfig.hasErrors()) {
                chain(action: "index", model: [error: deletedConfig])
            }
            flash.message = message(code: "app.configuration.delete.success", args: [config.name])
        } else {
            flash.warn = message(code: "app.configuration.delete.fail", args: [config.name])
        }
        redirect(action: "index")
    }

    @Secured(['ROLE_LITERATURE_CASE_CONFIGURATION'])
    def deleteLiterature(LiteratureConfiguration config) {
        if (!config) {
            notFound()
            return
        }
        User currentUser = userService.getUser()
        if (config.isEditableBy(currentUser) ||
                SpringSecurityUtils.ifAllGranted("ROLE_LITERATURE_CASE_CONFIGURATION, ROLE_EXECUTE_SHARED_ALERTS")) {
            LiteratureConfiguration deletedConfig = literatureAlertService.delete(config)
            if (deletedConfig.hasErrors()) {
                chain(action: "index", model: [error: deletedConfig])
            }
            flash.message = message(code: "app.configuration.delete.success", args: [config.name])
        } else {
            flash.warn = message(code: "app.configuration.delete.fail", args: [config.name])
        }
        redirect(action: "index")
    }

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def edit(Configuration configurationInstance) {
        if (!configurationInstance) {
            notFound()
            return
        }
        User currentUser = userService.getUser()

        if (reportExecutorService.currentlyRunning.contains(configurationInstance.id)) {
            flash.warn = message(code: "app.configuration.running.fail", args: [configurationInstance.name])
            redirect(action: "index")
        }

        if (!(configurationInstance?.isEditableBy(currentUser) ||
                ((configurationInstance.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT && SpringSecurityUtils.ifAllGranted("ROLE_AGGREGATE_CASE_CONFIGURATION, ROLE_EXECUTE_SHARED_ALERTS")) ||
                (configurationInstance.type == Constants.AlertConfigType.SINGLE_CASE_ALERT && SpringSecurityUtils.ifAllGranted("ROLE_SINGLE_CASE_CONFIGURATION, ROLE_EXECUTE_SHARED_ALERTS"))))
        ) {
            flash.warn = message(code: "app.configuration.edit.permission", args: [configurationInstance.name])
            redirect(action: "index")
        } else {
            render(view: "edit", model: [configurationInstance : configurationInstance,
                                         configSelectedTimeZone: params?.configSelectedTimeZone])
        }
    }

    def getAllEmailsUnique(Configuration configurationInstance) {
        userService.getAllEmails(userService.getUser())
    }


    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def save() {
        Configuration configurationInstance = new Configuration()
        configurationInstance.setIsEnabled(false)
        populateModel(configurationInstance)

        try {
            configurationInstance = (Configuration) CRUDService.save(configurationInstance)
        } catch (ValidationException ve) {
            configurationInstance.errors = ve.errors
            render view: "create", model: [configurationInstance: configurationInstance]
            return
        }

        request.withFormat {
            form {
                flash.message = message(code: 'default.created.message', args: [message(code: 'configuration.label'), configurationInstance.name])
                redirect(action: "view", id: configurationInstance.id)
            }
            '*' { respond configurationInstance, [status: CREATED] }
        }
    }

    private setNextRunDateAndScheduleDateJSON(Configuration configurationInstance) {
        if (configurationInstance.scheduleDateJSON && configurationInstance.isEnabled) {
            configurationInstance.nextRunDate = configurationService.getNextDate(configurationInstance)
        } else {
            configurationInstance.nextRunDate = null
        }
    }

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def run() {
        Configuration configurationInstance
        if (params.id) {
            configurationInstance = Configuration.get(params.id)
        } else {
            configurationInstance = new Configuration()
        }
        configurationInstance.setIsEnabled(true)
        populateModel(configurationInstance)

        try {
            configurationInstance = (Configuration) CRUDService.save(configurationInstance)
        } catch (ValidationException ve) {
            configurationInstance.errors = ve.errors
            if (params.id) {
                render view: "edit", model: [configurationInstance: configurationInstance]
            } else {
                render view: "create", model: [configurationInstance: configurationInstance]
            }
            return
        }
        AlertType alertType
        if(configurationInstance?.type?.equals("Single Case Alert")){
            alertType= AlertType.SINGLE_CASE_ALERT
        }else if(configurationInstance?.type?.equals("Aggregate Case Alert")){
            alertType= AlertType.AGGREGATE_CASE_ALERT
        }

        boolean precheckEnabled = Holders.config.system.precheck.configuration.enable
        if(precheckEnabled && !alertService.isPreCheckVerified(alertType,configurationInstance)){
            String alertTypeVal;
            if(alertType == AlertType.SINGLE_CASE_ALERT)
                alertTypeVal = message(code:'app.label.qualitative.alert.configuration');
            else if(alertType == AlertType.AGGREGATE_CASE_ALERT)
                alertTypeVal = message(code: 'app.role.ROLE_AGGREGATE_CASE_CONFIGURATION');
            flash.message = message(code: 'default.precheck.scheduled.message', args: [alertTypeVal, configurationInstance.name])
        }else{
            flash.message = message(code: 'app.Configuration.RunningMessage', args: [message(code: 'configuration.label'), configurationInstance.name])
        }


        request.withFormat {
            form {
                flash.message = flash.message
                redirect(action: "executionStatus")
            }
            '*' { respond configurationInstance, [status: CREATED] }
        }
    }

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EXECUTE_SHARED_ALERTS', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def runOnce() {
        Configuration configurationInstance = Configuration.get(params.id)
        if (!configurationInstance) {
            notFound()
            return
        }
        if (configurationInstance.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            try {
                String drugs = Configuration.findById(configurationInstance.id)?.drugType
                List drugList = drugs ? drugs.split(",").collect { it as Long } : []
                List foundDrugList = ProductTypeConfiguration.findAllByIdInList(drugList)
                if (!foundDrugList) {
                    flash.warn = message(code: "app.configuration.drugtype.deleted")
                    redirect(action: "index")
                    return
                }
            } catch (Exception ex) {
                log.error("Drug type is not integer")
            }
        }
        User currentUser = userService.getUser()
        if (!(configurationInstance?.isEditableBy(currentUser) ||
                ((configurationInstance.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT && SpringSecurityUtils.ifAllGranted("ROLE_AGGREGATE_CASE_CONFIGURATION, ROLE_EXECUTE_SHARED_ALERTS")) ||
                        (configurationInstance.type == Constants.AlertConfigType.SINGLE_CASE_ALERT && SpringSecurityUtils.ifAllGranted("ROLE_SINGLE_CASE_CONFIGURATION, ROLE_EXECUTE_SHARED_ALERTS"))))
        ) {
            flash.warn = message(code: "app.configuration.run.permission", args: [configurationInstance.name])
            redirect(action: "index")
            return
        }
        String masterConfigId = params.masterConfigId
        if(masterConfigId) {
            MasterConfiguration masterConfiguration = MasterConfiguration.findById(Long.parseLong(masterConfigId))
            Map schedulerMap = new JsonSlurper().parseText(masterConfiguration.scheduler)
            Boolean repeatExecution = schedulerMap['recurrencePattern'] != 'FREQ=DAILY;INTERVAL=1;COUNT=1'
            if(!repeatExecution) {
                List<Configuration> configList = Configuration.findAllByMasterConfigIdAndNextRunDateIsNull(masterConfiguration.id)
                if(configList && masterConfiguration.nextRunDate == null) {
                    masterConfiguration.setIsEnabled(true)
                    masterConfiguration.nextRunDate = DateUtils.truncate(new Date(), Calendar.SECOND)
                    masterConfiguration.save()
                    masterExecutorService.updateConfigurationList(configList, masterConfiguration)
                }
                else {
                    flash.warn = message(code: 'app.configuration.run.exists')
                    redirect(action: "index")
                    return
                }
            } else {
                flash.warn = message(code: 'app.configuration.run.exists')
                redirect(action: "index")
                return
            }

        } else {
            if (configurationInstance.nextRunDate != null && configurationInstance.isEnabled == true) {
                flash.warn = message(code: 'app.configuration.run.exists')
                redirect(action: "index")
                return
            }
            configurationInstance.setIsEnabled(true)
            configurationInstance.setScheduleDateJSON(getRunOnceScheduledDateJson())
            setNextRunDateAndScheduleDateJSON(configurationInstance)
            List runningAlerts = configurationService.fetchRunningAlertList(params.type)
            if (runningAlerts?.contains(configurationInstance.id)) {
                flash.warn = message(code: "app.configuration.running.fail", args: [configurationInstance.name])
                render view: "index"
                return
            }
            try {
//                configurationInstance = (Configuration) CRUDService.save(configurationInstance)
                String updateQuery = "update rconfig set next_run_date = " + "TO_DATE('${configurationInstance.nextRunDate?.format(SqlGenerationService.DATETIME_FMT)}', '${SqlGenerationService.DATETIME_FMT_ORA}'), is_enabled = true" + " where id = " + configurationInstance.id
                SQLQuery sql = null
                Session session = sessionFactory.currentSession
                sql = session.createSQLQuery(updateQuery)
                sql.executeUpdate()
                session.flush()
                session.clear()
            } catch (ValidationException ve) {
                configurationInstance.errors = ve.errors
                render view: "create", model: [configurationInstance: configurationInstance]
                return
            }
        }
        AlertType alertType
        if(configurationInstance?.type?.equals("Single Case Alert")){
            alertType= AlertType.SINGLE_CASE_ALERT
        }else if(configurationInstance?.type?.equals("Aggregate Case Alert")){
            alertType= AlertType.AGGREGATE_CASE_ALERT
        }
        boolean precheckEnabled = Holders.config.system.precheck.configuration.enable
        if(precheckEnabled && !alertService.isPreCheckVerified(alertType,configurationInstance)){
            String alertTypeVal;
            if(alertType == AlertType.SINGLE_CASE_ALERT)
                alertTypeVal = message(code:'app.label.qualitative.alert.configuration');
            else if(alertType == AlertType.AGGREGATE_CASE_ALERT)
                alertTypeVal = message(code: 'app.role.ROLE_AGGREGATE_CASE_CONFIGURATION');
            flash.message = message(code: 'default.precheck.scheduled.message', args: [alertTypeVal, configurationInstance.name])
        }else{
            flash.message = message(code: 'app.Configuration.RunningMessage')
        }
        redirect(action: "executionStatus", model: [configurationInstance: configurationInstance, status: CREATED], params: [alertType: alertType])
    }

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_CONFIGURATION','ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def update() {

        Configuration configurationInstance = Configuration.lock(params.id)
        if (!configurationInstance) {
            notFound()
            return
        }
        populateModel(configurationInstance)
        try {
            configurationInstance = (Configuration) CRUDService.update(configurationInstance)
        } catch (ValidationException ve) {
            Configuration originalConfiguration = Configuration.get(params.id)
            populateModel(originalConfiguration)
            originalConfiguration.errors = ve.errors
            render view: "edit", model: [configurationInstance: originalConfiguration]
            return
        }

        request.withFormat {
            form {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'configuration.label'), configurationInstance.name])
                redirect(action: "view", id: configurationInstance.id)
            }
            '*' { respond configurationInstance, [status: OK] }
        }
    }

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_CONFIGURATION',
            'ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_LITERATURE_CASE_CONFIGURATION', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def executionStatus() {
        User currentUser = userService.getUser()
        List<Map> alertTypeList = getAlertTypeList(currentUser)
        String alertType = params.alertType
        render(view: "executionStatus", model: [related: "executionStatusPage", isAdmin: currentUser?.isAdmin(), alertTypeList: alertTypeList,
        alertType:alertType, roleAccessMap:getAlertsConfigurationAccess()])
    }

    private Map getAlertsConfigurationAccess(){
        Map accessMap = [(AlertType.SINGLE_CASE_ALERT.key): SpringSecurityUtils.ifAnyGranted("ROLE_SINGLE_CASE_CONFIGURATION"),
                         (AlertType.AGGREGATE_CASE_ALERT.key): SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_CONFIGURATION, ROLE_FAERS_CONFIGURATION, ROLE_VAERS_CONFIGURATION, ROLE_VIGIBASE_CONFIGURATION,ROLE_JADER_CONFIGURATION"),
                         (AlertType.EVDAS_ALERT.key): SpringSecurityUtils.ifAnyGranted("ROLE_EVDAS_CASE_CONFIGURATION"),
                         (AlertType.LITERATURE_SEARCH_ALERT.key): SpringSecurityUtils.ifAnyGranted("ROLE_LITERATURE_CASE_CONFIGURATION")
        ]
        return accessMap
    }

    private List<Map> getAlertTypeList(User currentUser) {
        List<Map> alertTypeList = []
        if (currentUser.isAdmin()) {
            alertTypeList = AlertType.getAlertTypeList().collect {
                [name: it.key, display: message(code: it.getI18nValueForExecutionStatusDropDown())]
            }
        } else {
            if (SpringSecurityUtils.ifAnyGranted("ROLE_SINGLE_CASE_CONFIGURATION")) {
                alertTypeList.add([name: AlertType.SINGLE_CASE_ALERT.key, display: message(code: AlertType.SINGLE_CASE_ALERT.getI18nValueForExecutionStatusDropDown())])
            }
            if (SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_CONFIGURATION, ROLE_FAERS_CONFIGURATION, ROLE_VAERS_CONFIGURATION, ROLE_VIGIBASE_CONFIGURATION,ROLE_JADER_CONFIGURATION")) {
                alertTypeList.add([name: AlertType.AGGREGATE_CASE_ALERT.key, display: message(code: AlertType.AGGREGATE_CASE_ALERT.getI18nValueForExecutionStatusDropDown())])
            }
            if (SpringSecurityUtils.ifAnyGranted("ROLE_EVDAS_CASE_CONFIGURATION")) {
                alertTypeList.add([name: AlertType.EVDAS_ALERT.key, display: message(code: AlertType.EVDAS_ALERT.getI18nValueForExecutionStatusDropDown())])
            }
            if (SpringSecurityUtils.ifAnyGranted("ROLE_LITERATURE_CASE_CONFIGURATION")) {
                alertTypeList.add([name: AlertType.LITERATURE_SEARCH_ALERT.key, display: message(code: AlertType.LITERATURE_SEARCH_ALERT.getI18nValueForExecutionStatusDropDown())])
            }

        }
        alertTypeList
    }


    def listAllResults() {
        User currentUser = userService.getUser()
        render(view: "executionStatus", model: [related: "listAllResultsPage", isAdmin: currentUser?.isAdmin()])
    }

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_SINGLE_CASE_REVIEWER', 'ROLE_SINGLE_CASE_VIEWER',
            'ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_AGGREGATE_CASE_VIEWER',
            'ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER',
            'ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
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
    
    public getPublicForExecutedConfig() {
        return ExecutedConfiguration.get(params.id).isPublic ? message(code: "app.label.public") : message(code: "app.label.private")
    }

    // This is needed to properly re-read the data being created/edited after a transaction rollback
    private populateModel(Configuration configurationInstance) {
        params?.name = params?.name?.trim()?.replaceAll("\\s{2,}", " ")
        //Do not bind in any other way because of the clone contained in the params
        bindData(configurationInstance, params, [exclude: ["templateQueries", "tags", "isEnabled", "asOfVersionDate"]])
        bindAsOfVersionDate(configurationInstance)
        setNextRunDateAndScheduleDateJSON(configurationInstance)
        bindNewTemplateQueries(configurationInstance)
        bindExistingTemplateQueryEdits(configurationInstance)

        def _toBeRemoved = configurationInstance?.templateQueries?.findAll {
            (it?.dynamicFormEntryDeleted || (it == null))
        }

        // if there are Template Queries to be removed
        if (_toBeRemoved) {
            configurationInstance?.templateQueries?.removeAll(_toBeRemoved)
        }
//        _toBeRemoved.each {
//            configurationInstance?.removeFromTemplateQueries(it)
//            it.delete()
//        }

        //update the indexes
        configurationInstance?.templateQueries?.eachWithIndex() { templateQuery, i ->
            if (templateQuery) {
                templateQuery.index = i
            }
        }
    }

    private bindAsOfVersionDate(Configuration configuration) {
        if (configuration.evaluateDateAs == EvaluateCaseDate.VersionAsOf) {
            configuration.asOfVersionDate = DateUtil.getAsOfVersion(params.asOfVersionDateValue, configuration.configSelectedTimeZone)
        } else {
            configuration.asOfVersionDate = null
        }

    }

    private bindNewTemplateQueries(Configuration configurationInstance) {
        //bind new Template Queries as appropriate
        for (int i = 0; params.containsKey("templateQueries[" + i + "].id"); i++) {
            if (params.get("templateQueries[" + i + "].new").equals("true")) {
                LinkedHashMap bindingMap = getBindingMap(i)
                TemplateQuery templateQueryInstance = new TemplateQuery(bindingMap)

                templateQueryInstance = (TemplateQuery) userService.setOwnershipAndModifier(templateQueryInstance)
                //Set the back reference on DateRangeInformationForTemplateQuery object to TemplateQuery; binding via bindingMap won't do this
                DateRangeInformation dateRangeInformationForTemplateQuery = templateQueryInstance.dateRangeInformationForTemplateQuery
                def dateRangeEnum = params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.dateRangeEnum")
                if (dateRangeEnum) {
                    dateRangeInformationForTemplateQuery?.dateRangeEnum = dateRangeEnum
                    dateRangeInformationForTemplateQuery.dateRangeStartAbsolute = DateUtil.getStartDate(params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute"), configurationInstance?.configSelectedTimeZone)
                    dateRangeInformationForTemplateQuery.dateRangeEndAbsolute = DateUtil.getEndDate(params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute"), configurationInstance?.configSelectedTimeZone)
                    dateRangeInformationForTemplateQuery?.dateRangeStartAbsoluteDelta = RelativeDateConverter.getDaysDifference(configurationInstance?.nextRunDate, dateRangeInformationForTemplateQuery.dateRangeStartAbsolute)
                    dateRangeInformationForTemplateQuery?.dateRangeEndAbsoluteDelta = RelativeDateConverter.getDaysDifference(configurationInstance?.nextRunDate, dateRangeInformationForTemplateQuery.dateRangeEndAbsolute)
                }
                dateRangeInformationForTemplateQuery.templateQuery = templateQueryInstance

                dateRangeInformationForTemplateQuery.relativeDateRangeValue = (params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.relativeDateRangeValue")) as Integer

                assignParameterValuesToTemplateQuery(templateQueryInstance, i)

                configurationInstance.addToTemplateQueries(templateQueryInstance)
            }

        }
    }

    private bindExistingTemplateQueryEdits(Configuration configurationInstance) {
        //handle edits to the existing Template Queries
        configurationInstance?.templateQueries?.eachWithIndex() { templateQuery, i ->
            LinkedHashMap bindingMap = getBindingMap(i)
            templateQuery.properties = bindingMap
            templateQuery = (TemplateQuery) userService.setOwnershipAndModifier(templateQuery)
            //Set the back reference on DateRangeInformationForTemplateQuery object to TemplateQuery; binding via bindingMap won't do this
            DateRangeInformation dateRangeInformationForTemplateQuery = templateQuery.dateRangeInformationForTemplateQuery
            def dateRangeEnum = params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.dateRangeEnum")
            if (dateRangeEnum) {
                dateRangeInformationForTemplateQuery?.dateRangeEnum = dateRangeEnum
//                    if(dateRangeEnum.name() == DateRangeEnum.CUSTOM.name()) {
                dateRangeInformationForTemplateQuery.dateRangeStartAbsolute = DateUtil.getStartDate(params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.dateRangeStartAbsolute"), configurationInstance?.configSelectedTimeZone)
                dateRangeInformationForTemplateQuery.dateRangeEndAbsolute = DateUtil.getEndDate(params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.dateRangeEndAbsolute"), configurationInstance?.configSelectedTimeZone)
                dateRangeInformationForTemplateQuery?.dateRangeEndAbsoluteDelta = RelativeDateConverter.getDaysDifference(configurationInstance?.nextRunDate, dateRangeInformationForTemplateQuery.dateRangeEndAbsolute)
                dateRangeInformationForTemplateQuery?.dateRangeStartAbsoluteDelta = RelativeDateConverter.getDaysDifference(configurationInstance?.nextRunDate, dateRangeInformationForTemplateQuery.dateRangeStartAbsolute)
//                    }
            }
            dateRangeInformationForTemplateQuery.templateQuery = templateQuery

            if (params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.relativeDateRangeValue") && params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.relativeDateRangeValue") =~ "-?\\d+") {
                dateRangeInformationForTemplateQuery.relativeDateRangeValue = (params.("templateQueries[" + i + "].dateRangeInformationForTemplateQuery.relativeDateRangeValue")) as Integer
            }


            assignParameterValuesToTemplateQuery(templateQuery, i)
        }
        configurationInstance
    }

    private void assignParameterValuesToTemplateQuery(TemplateQuery templateQuery, int i) {
        templateQuery.queryValueLists?.each {
            it.parameterValues?.each {
                if (it.hasProperty('reportField')) {
                    QueryExpressionValue.get(it.id)?.delete()
                } else {
                    CustomSQLValue.get(it.id)?.delete()
                }
            }
            it.parameterValues?.clear()
        }
        templateQuery.queryValueLists = []
        templateQuery.templateValueLists?.each {
            it.parameterValues?.each {
                CustomSQLValue.get(it.id)?.delete()
            }
            it.parameterValues?.clear()
        }
        templateQuery.templateValueLists?.clear()

        if (params.containsKey("templateQuery" + i + ".qev[0].key")) {
            QueryValueList queryValueList = new QueryValueList(query: params.("templateQueries[" + i + "].query"))

            for (int j = 0; params.containsKey("templateQuery" + i + ".qev[" + j + "].key"); j++) {
                ParameterValue tempValue
                if (params.containsKey("templateQuery" + i + ".qev[" + j + "].field")) {
                    def operatorString=QueryOperatorEnum.valueOf(params.("templateQuery" + i + ".qev[" + j + "].operator"))
                    tempValue = new QueryExpressionValue(key: params.("templateQuery" + i + ".qev[" + j + "].key"),
                            reportField: ReportField.findByName(params.("templateQuery" + i + ".qev[" + j + "].field")),
                            operator: operatorString,
                            value: params.("templateQuery" + i + ".qev[" + j + "].value"),
                            operatorValue: messageSource.getMessage("app.queryOperator.$operatorString", null, Locale.ENGLISH))
                } else {
                    tempValue = new CustomSQLValue(key: params.("templateQuery" + i + ".qev[" + j + "].key"),
                            value: params.("templateQuery" + i + ".qev[" + j + "].value"))
                }
                queryValueList.addToParameterValues(tempValue)
            }
            templateQuery.addToQueryValueLists(queryValueList)
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

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    public enable() {

        Configuration configurationInstance = Configuration.get(params.id)
        if (!configurationInstance) {
            notFound()
            return
        }
        // configurationService.setBlankValues(configurationInstance, Query.get(params.query), params.JSONExpressionValues)
        configurationInstance.setIsEnabled(true)
        populateModel(configurationInstance)

        try {
            configurationInstance = (Configuration) CRUDService.update(configurationInstance)
        } catch (ValidationException ve) {
            Configuration originalConfiguration = Configuration.get(params.id)
            populateModel(originalConfiguration)
            originalConfiguration.errors = ve.errors
            render view: "edit", model: [configurationInstance: originalConfiguration]
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

    @Secured(['ROLE_SINGLE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    public disable() {

        Configuration configurationInstance = Configuration.get(params.id)
        if (!configurationInstance) {
            notFound()
            return
        }
        // configurationService.setBlankValues(configurationInstance, Query.get(params.query), params.JSONExpressionValues)
        configurationInstance.setIsEnabled(false)
        populateModel(configurationInstance)

        try {
            configurationInstance = (Configuration) CRUDService.save(configurationInstance)
        } catch (ValidationException ve) {
            Configuration originalConfiguration = Configuration.get(params.id)
            populateModel(originalConfiguration)
            originalConfiguration.errors = ve.errors
            render view: "edit", model: [configurationInstance: originalConfiguration]
            return
        }

        request.withFormat {
            form {
                flash.message = message(code: 'default.disabled.message', args: [message(code: 'configuration.label'), configurationInstance.name])
                redirect(action: "view", id: configurationInstance.id)
            }
            '*' { respond configurationInstance, [status: OK] }
        }

    }

    @Secured(['ROLE_ADMIN'])
    def signalExecutionError(Long id) {
        ExecutionStatus exStatus = ExecutionStatus.read(id)
        Map progressTrackerMap = [:]
        int highestExecutionLevel=1
        Map milestonesCompleted = configurationService.getCompletedMilestonesMap(exStatus)
        if (!exStatus) {
            notFound()
            return
        }
        if (exStatus.executionLevel == 1) {
            progressTrackerMap.put(1, appAlertProgressStatusService.calculateAlertProgress(exStatus.id, 1));
            highestExecutionLevel=1
        } else if (exStatus.executionLevel == 2) {
            progressTrackerMap.put(1, appAlertProgressStatusService.calculateAlertProgress(exStatus.id, 1));
            progressTrackerMap.put(2, appAlertProgressStatusService.calculateAlertProgress(exStatus.id, 2));
            highestExecutionLevel=2
        } else if (exStatus.executionLevel == 3) {
            progressTrackerMap.put(1, appAlertProgressStatusService.calculateAlertProgress(exStatus.id, 1));
            progressTrackerMap.put(2, appAlertProgressStatusService.calculateAlertProgress(exStatus.id, 2));
            progressTrackerMap.put(3, appAlertProgressStatusService.calculateAlertProgress(exStatus.id, 3));
            highestExecutionLevel=3

        }
        render(view: "/configuration/signalExecutionError", model: [highestExecutionLevel: highestExecutionLevel ,progressTrackerMap: progressTrackerMap as JSON, exStatus: exStatus, milestonesCompleted: milestonesCompleted as JSON])
    }

    private getBindingMap(int i) {
        def bindingMap = [
                template               : params.("templateQueries[" + i + "].template"),
                query                  : params.("templateQueries[" + i + "].query"),
                operator               : params.("templateQueries[" + i + "].operator"),
                queryLevel             : params.("templateQueries[" + i + "].queryLevel"),
                dynamicFormEntryDeleted: params.("templateQueries[" + i + "].dynamicFormEntryDeleted") ?: false,
                header                 : params.("templateQueries[" + i + "].header") ?: null,
                footer                 : params.("templateQueries[" + i + "].footer") ?: null,
                title                  : params.("templateQueries[" + i + "].title") ?: null,
                headerDateRange        : params.("templateQueries[" + i + "].headerDateRange") ?: false
        ]
        bindingMap
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

    private getRunOnceScheduledDateJson() {
        def startupTime = (new Date()).format(ConfigurationService.JSON_DATE)
        println(startupTime)
        def timeZone = DateUtil.getTimezoneForRunOnce(userService.getUser())
        return """{"startDateTime":"${
            startupTime
        }","timeZone":{"${timeZone}"},"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}"""

    }
}

