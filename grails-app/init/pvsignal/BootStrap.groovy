package pvsignal

import com.rxlogix.Constants
import com.rxlogix.EmailNotification
import com.rxlogix.PostgreSecurityUtil
import com.rxlogix.QueryService
import com.rxlogix.UserDashboardCounts
import com.rxlogix.attachments.Attachment
import com.rxlogix.attachments.Attachmentable
import com.rxlogix.attachments.AttachmentableService
import com.rxlogix.attachments.ajax.ProgressDescriptor
import com.rxlogix.attachments.exceptions.AttachmentableException
import com.rxlogix.config.*
import com.rxlogix.dto.BlindedUserGroupsDTO
import com.rxlogix.enums.EvdasFileProcessState
import com.rxlogix.enums.GroupType
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.json.JsonOutput
import com.rxlogix.mapping.MedDraPT
import com.rxlogix.pvdictionary.ProductDictionaryMetadata
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.security.jwt.JwtAuthenticationFilter
import com.rxlogix.signal.*
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.AttachmentableUtil
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.core.GrailsApplication
import grails.plugin.springsecurity.SecurityFilterPosition
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.GrailsClassUtils
import grails.util.Holders
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.sql.GroovyResultSet
import groovy.sql.Sql
import org.springframework.web.context.support.WebApplicationContextUtils
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.commons.CommonsMultipartFile
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest
import com.rxlogix.FieldManagementNotificationService
import javax.servlet.ServletContext
import javax.sql.DataSource
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import com.rxlogix.config.AlertGroup

class BootStrap {
    def messageSource
    def CRUDService
    def seedDataService
    def alertAttributesService
    def graphFaersDataService
    def attachmentableService
    GrailsApplication grailsApplication
    PostgreSecurityUtil postgreSecurityUtil
    def reportFieldService
    def PVDMSBootStrapService
    def cacheService
    def springSecurityService
    def aggregateCaseAlertService
    def evdasAlertService
    def applicationSettingsService
    def dataObjectService
    QueryService queryService
    def alertService
    def dataSource
    def productAssignmentImportService
    def importConfigurationService
    boolean isFreshBuild
    def userService
    def sessionFactory
    def validatedSignalService
    def pvsGlobalTagService
    def preCheckService
    def alertFieldService
    def userRoleService
    def applicationConfigService
    def refreshConfigurationService
    ConfigManagementService configManagementService
    def viewInstanceService
    def refreshDbConfigService
    def emergingIssueService
    def reportIntegrationService
    def userGroupService

    DataSource dataSource_pva
    DataSource dataSource_faers
    DataSource dataSource_vaers
    DataSource dataSource_vigibase
    DataSource dataSource_jader
    DataSource dataSource_eudra
    FieldManagementNotificationService fieldManagementNotificationService



    def init = { servletContext ->
        SpringSecurityUtils.clientRegisterFilter( 'securityHeadersFilter', SecurityFilterPosition.LAST.order + 20)
        log.info("=========================== Booting the PVS application =========================")

        isFreshBuild = Justification.count() == 0 && Disposition.count() == 0 && DispositionRule.count() == 0 && BusinessConfiguration.count() == 0 ? true : false
        boolean precheckEnabled = Holders.config.system.precheck.configuration.enable
        applicationConfigService.updateAppConfigurations("PVS", dataSource_pva, false)
        refreshConfigurationService.updateOnetimeDependentConfigurations()
        updateDbConfigurations()

        def selectedDataSource = "dataSource"
        log.info "User home is : ${Holders.getConfig().grails.pvsignal.config.root}"
        if (precheckEnabled) {
            truncatePrecChecks()
            initializeSystemConfigurationPrecheck()
            new Thread(new Runnable() {
                public void run() {
                    preCheckService.executeSystemConfigurationPrecheck()
                }
            }).start();
        }
        updateArgusScoreConfiguration()
        updateScoreConfiguration()
        initTempSpace()
        setDefaultTimeZone()
        injectMessageSource()
        registerMarshallers(servletContext)
        reOrderFilters()
        createAllUsersGroup()
        setRoleHierarchy()
        initializeSystemConfig()
        changeMultipartResolver(servletContext)
        createProductGroups()
        generateDisposition()
        createWorkflowGroups()
        createSignalWorkflow()
        createBusinessRules()
        createEmergingIssues()
        createSafetyLead()
        readDataFromExcel()
        saveRorStatus()
        saveSocAbbreviation()
        refreshConfigurationService.syncGlobalCaseComments()
        initializeUserDashboardCounts()
        updateQueryExpression()
        updateUserPreferenceDashboardJson()
        updateForceAuthNForSSO()
        if (Holders.config.pvsignal.enable.data.seeding == true) {
            log.info("Seeding Started.")
            seedDataService.seed()
            log.info("Seeding Completed.")
        } else {
            log.info("Seeding flag is false thus seeding will be skipped.")
        }

        if (Holders.config.pvsignal.multiple.datasource.toBoolean() == true &&
                Holders.config.pvsignal.supported.datasource.call().contains(Constants.DataSource.FAERS) &&
                Holders.config.signal.faers.graphDB.seedData == true) {
            log.info("Faers DB Seeding Started.")
            graphFaersDataService.seed()
            log.info("Faers DB Seeding Completed.")
        } else {
            log.info("Seeding on FAERS flag is false thus seeding will be skipped.")
        }
        if (Holders.config.signal.evdas.enabled == true) {
            def ctx = Holders.grailsApplication.mainContext
            def evdasDataImportService = ctx.getBean("evdasDataImportService")
            evdasDataImportService.checkAndCreateBaseDirs()
            def evdasCaseListingImportService = ctx.getBean("evdasCaseListingImportService")
            evdasCaseListingImportService.checkAndCreateBaseDirs()
        }


        if (Holders.config.validatedSignal.shareWith.enabled) {
            validatedSignalService.shareWithAllUsers()
        }
        //TODO: Need to uncomment it.
        alertService.createAlertFilesFolder()
        productAssignmentImportService.checkAndCreateBaseDirs()
        importConfigurationService.checkAndCreateBaseDirs()
        importConfigurationService.failScheduledImportConfiguration()
        importConfigurationService.clearUploadDirectory()
        setApplicationSettingsInDB()
        cacheAdHocAlertAttributes()
        assignDefaultWorkflowGroupToExistingUsers()
        saveApplicationData()
        createMedicalConcepts()
        createJustifications()
        createTopicCategory()
        createSignalOutcome()
        createActionConfigs()
        updateSubstanceFrequency()
        mapSignalOutcomeAndDisposition()
        dataObjectService.prepareProductDictValues()
        dataObjectService.setIdLabelMap()
        dataObjectService.setLabelIdMap()
        generateEmailNotification()
        alertFieldService.initiateDefaultAggregateAlertFields()
        if (Holders.config.signal.faers.enabled) {
            alertFieldService.updateFlgToFlag()
        }
        cacheService.initCache()
        alertFieldService.updateAlertFields()
        alertFieldService.updateOnDemandAlertFields()
        alertFieldService.updatePreviousCommentTemplate()
        alertFieldService.updateOldDisplayValue()
        userRoleService.updateRoleAuthorityDisplay()
        initiateDynamicTables()
        alertService.executeIncompleteJobs()
        alertService.executeMasterIncompleteJobs()
        updateUserGroupFieldMapping()
        createViewInstances()
        scheduleJobs()
        initDictionaryPlugin()
        productAssignmentImportService.resumeInProgressFile()
        getAllReportFields()
        getReportFields()
        refreshConfigurationService.populateValueInDataSourceMap()
        syncGloablCategoriesAndAggAlerts()
        enableEtlPvrConnectivity()
        initializeAlertPreChecks()
        refreshConfigurationService.initializeAlertAutoAdjustmentRules()
        setHostnameForMultiNodeSensitiveJobs()
        setPvsAppConfigurationForSignalCharts()
        setPvsAppConfigurationForProductGroupUpdate()
        initiateProductTypeConfiguration()
        cachePreCheckValue()
        setAutoAlertJobConfigurationsInWebAppSchema()
        updateEvdasFileStatus()
        //fieldManagementNotificationService.subscribeHazelcastTopic()
        updateReportHistory()
        disableApprovalRequiredInWorkFlow()
        setValueForPVAMigration()
        populateDisplayConfigForDynamicFields()
        postgreSecurityUtil.updateColumnsAccess()
        populateAlertGroup()
        showStartup()
        Holders.config.signal.boot.status = true
        log.info("=========================== Booting the PVS application completed =========================")
        log.info "Database home is : ${Holders.getConfig().dataSources."$selectedDataSource".url}"
    }

    void updateDbConfigurations() {
        refreshDbConfigService.updateConfigurableAggFields("PVA-DB")
        if (Holders.config.signal.faers.enabled) {
            refreshDbConfigService.updateAppConfigurationsFromMart("FAERS-DB")
            refreshDbConfigService.updateConfigurableAggFields("FAERS-DB")
        }
        if (Holders.config.signal.vaers.enabled) {
            refreshDbConfigService.updateAppConfigurationsFromMart("VAERS-DB")
            refreshDbConfigService.updateConfigurableAggFields("VAERS-DB")

        }
        if (Holders.config.signal.vigibase.enabled) {
            refreshDbConfigService.updateAppConfigurationsFromMart("VIGIBASE-DB")
            refreshDbConfigService.updateConfigurableAggFields("VIGIBASE-DB")

        }
        if (Holders.config.signal.evdas.enabled) {
            refreshDbConfigService.updateAppConfigurationsFromMart("EUDRA-DB")
            refreshDbConfigService.updateConfigurableAggFields("EUDRA-DB")
        }
        if (Holders.config.signal.jader.enabled) {
            refreshDbConfigService.updateAppConfigurationsFromMart("JADER-DB")
            refreshDbConfigService.updateConfigurableAggFields("JADER-DB")

        }

    }

    void truncatePrecChecks() {
        try {
            SystemPreConfig.deleteAll(SystemPreConfig.findAll())
        } catch (Exception ex) {
            ex.printStackTrace()
        }

        try {
            SystemPrecheckEmail.deleteAll(SystemPrecheckEmail.findAll())
        } catch (Exception ex) {
            ex.printStackTrace()
        }
    }

    private void readDataFromExcel() {
        String baseDirectoryPath = Holders.config.pvadmin.api.import.read.directory
        try {
            File baseFolder = new File(configManagementService.USER_HOME_DIR + baseDirectoryPath)
            if (!baseFolder.exists()) {
                baseFolder.mkdirs()
            }
            File sourcePath = new File(configManagementService.USER_HOME_DIR + "/configurations_folder/BUSINESS_CONFIG_PVS.xlsx")
            log.info("Business configurations excel source path - ${sourcePath.getAbsolutePath()}")
            if (sourcePath.exists()) {
                File destDir = new File(configManagementService.USER_HOME_DIR + baseDirectoryPath + Constants.ConfigManagement.BUSINESS_CONFIG_EXCEL)
                Files.copy(sourcePath.toPath(), destDir.toPath(), StandardCopyOption.REPLACE_EXISTING);
                if (destDir.exists()) {
                    configManagementService.parseExcelAndPopulateDB(baseDirectoryPath + Constants.ConfigManagement.BUSINESS_CONFIG_EXCEL, true)
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace()
        }
    }

    def mapSignalOutcomeAndDisposition() {
        List<SignalOutcome> outcomes = SignalOutcome.findAll();
        outcomes.each {
            it.dispositionId = null;
        }
        SignalOutcome.saveAll(outcomes);
        if (Holders.config.disposition.signal.outcome.mapping.enabled) {
            Map mapping = grailsApplication.config.freshBuild.disposition.mapped.signalOutcome
            mapping.each { key, val ->
                Disposition disposition = Disposition.findByValue(key);
                val.value.each {
                    SignalOutcome signalOutcome = SignalOutcome.findByName(it)
                    signalOutcome?.dispositionId = disposition?.id
                    signalOutcome?.save(flush: true);
                }
            }
        }
    }

    void updateArgusScoreConfiguration() {
        def config = Holders.getConfig()
        final String sqlToExecute = """
            select case when CONFIG_KEY = 'PQR_Ebgm' then 'EBGM' WHEN CONFIG_KEY = 'PQR_PRR' THEN 'PRR' END as key,JSON_VALUE(config_value,'\$.HIDE') as hide from VW_ADMIN_APP_CONFIG where CONFIG_KEY in ('PQR_Ebgm', 'PQR_PRR') and application_name = 'PVA-DB'
        """
        Sql sql
        try {
            sql = new Sql(dataSource_pva)
            sql.eachRow(sqlToExecute, []) { sqlRow ->
                if(sqlRow.key == "EBGM") {
                    config.statistics.enable.ebgm = sqlRow.hide == "0"? true:false
                }
                if(sqlRow.key == "PRR") {
                    config.statistics.enable.prr = sqlRow.hide == "0"? true:false
                    config.statistics.enable.ror = sqlRow.hide == "0"? true:false
                }
            }

        } catch(Exception ex) {
            ex.printStackTrace()
        } finally {
            sql?.close()
        }
    }

    void updateScoreConfiguration() {
        def config = Holders.getConfig()
        Map<String, DataSource> dataSourceMap = new HashMap<>();
        if (config.signal.faers.enabled) {
            dataSourceMap.put(Constants.DataSource.FAERS, dataSource_faers)
        }
        if (config.signal.vaers.enabled) {
            dataSourceMap.put(Constants.DataSource.VAERS, dataSource_vaers)
        }
        if (config.signal.vigibase.enabled) {
            dataSourceMap.put(Constants.DataSource.VIGIBASE, dataSource_vigibase)
        }
        if (config.signal.jader.enabled) {
            dataSourceMap.put(Constants.DataSource.JADER, dataSource_jader)
        }


        Sql sql
        try {
            dataSourceMap.each { key, value ->
                String sqlToExecute = """
            select case when CONFIG_KEY = 'PQR_Ebgm' then 'EBGM' WHEN CONFIG_KEY = 'PQR_PRR' THEN 'PRR' END as key,JSON_VALUE(config_value,'\$.HIDE') as hide from VW_ADMIN_APP_CONFIG where CONFIG_KEY in ('PQR_Ebgm', 'PQR_PRR')
            """

                String dataSource = ""
                sql = new Sql(value)
                if (Constants.DataSource.FAERS.equals(key)) {
                    dataSource += Constants.FAERS_DB
                }
                if (Constants.DataSource.VAERS.equals(key)) {
                    dataSource += Constants.VAERS_DB
                }
                if (Constants.DataSource.VIGIBASE.equals(key)) {
                    dataSource += Constants.VIGIBASE_DB
                }
                if (Constants.DataSource.JADER.equals(key)) {
                    dataSource += Constants.JADER_DB
                }
                sqlToExecute +=  " and application_name = '${dataSource}'"
                sql.eachRow(sqlToExecute, []) { sqlRow ->
                    if (sqlRow.key == "EBGM") {
                        config.statistics."${key}".enable.ebgm = sqlRow.hide == "0"? true:false

                    }
                    if (sqlRow.key == "PRR") {
                        config.statistics."${key}".enable.prr = sqlRow.hide == "0"? true:false
                        config.statistics."${key}".enable.ror = sqlRow.hide == "0"? true:false
                    }
                }
                sql?.close()
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        } finally {
            sql?.close()
        }
    }

    def updateSubstanceFrequency() {
        Map frequencyMap = ['Fortnight': '15 Days', 'Monthly': '1 Month', 'Quarterly': '3 Months', 'HalfYearly': '6 Months', 'Yearly': '12 Months']
        List frequencyMapList = frequencyMap.keySet() as List;
        List<SubstanceFrequency> frequencies = SubstanceFrequency.findAllByUploadFrequencyInListOrMiningFrequencyInList(frequencyMapList, frequencyMapList)
        frequencies.each {
            if (frequencyMap.keySet().contains(it.uploadFrequency) || frequencyMap.keySet().contains(it.miningFrequency)) {
                it.uploadFrequency = frequencyMap.get(it.uploadFrequency)
                it.miningFrequency = frequencyMap.get(it.miningFrequency)
            }
            it.save();
        }
    }

    def updateQueryExpression(){
        List queryExpresionList=QueryExpressionValue.list()
        queryExpresionList.each {
            if(it.operatorValue==null){
                it.operatorValue=messageSource.getMessage("app.queryOperator.$it.operator", null, Locale.ENGLISH)
                it.save()
            }
        }

    }

    // This method is to ensure that user dashboard couts are prepared in case of upgrade scenario.
    def initializeUserDashboardCounts() {
        try {
            User.list().each { User user ->
                Long userId = user.id
                UserDashboardCounts userDashboardCounts = UserDashboardCounts.get(userId)
                if (!userDashboardCounts) {
                    new UserDashboardCounts(userId: userId).save(flush: true)
                    userService.updateUserGroupCountsInBackground(user, [])
                }
            }
        } catch (Exception ex) {
            log.error("Unable to refresh the user dashboard counts")
            ex.printStackTrace()
        }
    }

    def setApplicationSettingsInDB() {
        if (!EvdasApplicationSettings.count) {
            new EvdasApplicationSettings().save(flush: true)
        } else {
            applicationSettingsService.releaseEvdasErmrUploadLock()
            applicationSettingsService.releaseEvdasCaseListingUploadLock()
        }
        //commented this as dashaboard counts are handled by real time query as part of PVS-16312
        //setCountSyncJobConfigurationsInWebAppSchema()
    }

    void assignDefaultWorkflowGroupToExistingUsers() {
        Group workflowGroup = Group.findAllByGroupType(GroupType.WORKFLOW_GROUP)?.first()
        User.all.collect {
            if (!it.workflowGroup && workflowGroup) {
                it.addToGroups(workflowGroup)
                it.save(flush: true)
            }
        }
    }

    void createProductGroups() {
        if (isFreshBuild) {
            grailsApplication.config.configurations.freshBuild.productGroups.collect {
                new Group(name: it.name, description: it.description, isActive: true, createdBy: "System", modifiedBy: "System", groupType: GroupType.USER_GROUP).save(flush: true)
            }
        } else {
            if (!Group.countByGroupType(GroupType.USER_GROUP) && Group.count() == 0) {
                grailsApplication.config.configurations.productGroups.collect {
                    new Group(name: it.name, description: it.description, isActive: true, createdBy: "System", modifiedBy: "System", groupType: GroupType.USER_GROUP).save(flush: true)
                }
            }
        }
    }

    void createWorkflowGroups() {
        if (!Group.countByGroupType(GroupType.WORKFLOW_GROUP)) {
            grailsApplication.config.configurations.workflowGroups.collect {
                new Group(name: it.name, description: it.description, isActive: true, createdBy: "System", modifiedBy: "System", groupType: GroupType.WORKFLOW_GROUP,
                        defaultQualiDisposition: Disposition.findByValue(Constants.Commons.DISPOSITION_REQUIRES_REVIEW),
                        defaultQuantDisposition: Disposition.findByValue(Constants.Commons.DISPOSITION_THRESHOLD_NOT_MET),
                        defaultAdhocDisposition: Disposition.findByValue(Constants.Commons.DISPOSITION_REQUIRES_REVIEW),
                        defaultEvdasDisposition: Disposition.findByValue(Constants.Commons.DISPOSITION_THRESHOLD_NOT_MET),
                        defaultLitDisposition: Disposition.findByValue(Constants.Commons.DISPOSITION_REQUIRES_REVIEW),
                        defaultSignalDisposition: Disposition.findByValue(Constants.Commons.SAFETY_TOPIC),
                        isDefault: it.isDefault == "true").save(flush: true)
            }
        }
    }

    def createActionConfigs() {
        if (ActionType.count() <= 0)
            grailsApplication.config.configurations.actionTypes.each {
                def at = new ActionType(value: it.value, displayName: it.displayName, description: it.description)
                at.save(flush: true, failOnError: true)
            }

        if (ActionConfiguration.count() <= 0) {
            grailsApplication.config.configurations.actionConfiguration.each {
                def ac = new ActionConfiguration(value: it.action, displayName: it.action,
                        isEmailEnabled: true, description: it.description)
                ac.save(flush: true, failOnError: true)
            }
        }
    }

    void createBusinessRules() {
        if (isFreshBuild) {
            log.debug "Creating Business Rules"
            if (BusinessConfiguration.count() == 0) {
                def configList = grailsApplication.config.configurations.freshBuild.businessConfiguration
                BusinessConfiguration businessConfiguration
                configList.each {
                    businessConfiguration = new BusinessConfiguration(ruleName: it.ruleName, description: it.description, dataSource: it.source, isGlobalRule: true, createdBy: "System", modifiedBy: "System", enabled: true)
                    businessConfiguration.save(failOnError: true, flush: true)
                    log.debug "Business Configuration created- ${it.ruleName}"
                }
            }

            if (RuleInformation.count() == 0 && BusinessConfiguration.count() >= 0) {
                RuleInformation ruleInformation
                def ruleList = grailsApplication.config.configurations.freshBuild.businessRule
                ruleList.each {
                    BusinessConfiguration businessConfiguration = BusinessConfiguration.findByRuleName(it.businessConfig)
                    if (businessConfiguration != null) {
                        Disposition disposition = Disposition.findByDisplayName(it.disposition)
                        println "rule>>>>> "  + it.ruleName
                        println "rule>>>>> "  + it.rule
                        println "###########################RULE end"
                        ruleInformation = new RuleInformation(ruleName: it.ruleName, businessConfiguration: businessConfiguration, enabled: it.enable, ruleJSON: it.rule, disposition: disposition, justificationText: it.justificationText, createdBy: "System", modifiedBy: "System", isBreakAfterRule: false, isFirstTimeRule: false, isSingleCaseAlertType: false)
                        if (it.tags) {
                            List tagsList = it.tags
                            String tagJson = JsonOutput.toJson(tagsList)
                            ruleInformation.tags = ([tags: tagJson] as JSON)
                        }
                        ruleInformation.save(failOnError: true, flush: true)
                        log.debug "Business Rule created- ${it.ruleName}"
                    }
                }
            }
        }
    }

    void createEmergingIssues() {
        if (isFreshBuild) {
            log.debug "Creating Emerging Issues"
            if (EmergingIssue.count() == 0) {
                ((seedDataService.readPvsExtConfig()).grails.pvsignal.importantEvents as List).each {
                    EmergingIssue emergingIssue
                    Long id = null
                    String eventName = it.eventname
                    MedDraPT."pva".withTransaction {
                        id = MedDraPT."pva".findByNameAndLangId(eventName, 1)?.id
                    }
                    if (id) {
                        String eventSelection = '{"1":[],"2":[],"3":[],"4":[{"name":"' + eventName + '","id":"' + id + '"}],"5":[],"6":[]}'
                        List events = []
                        Map eventMap = emergingIssueService.prepareEventMap(eventSelection, [])
                        events += eventMap.keySet()
                        events += eventMap.values().flatten()
                        emergingIssue = new EmergingIssue(eventName: eventSelection, ime: it.ime, dme: it.dme, emergingIssue: false, specialMonitoring: false, createdBy: "System", modifiedBy: "System", events: events.join(','))
                        emergingIssue.save(failOnError: true, flush: true)
                    }
                }
            }
            log.debug "Emerging Issues Creation Completed"
        }
    }

    void createSafetyLead() {
        if (isFreshBuild) {
            log.debug "Creating Safety Lead "
            if (SafetyGroup.count() == 0) {
                def configList = grailsApplication.config.configurations.freshBuild.safetyLead
                SafetyGroup safetyGroup
                configList.each {
                    safetyGroup = new SafetyGroup(name: it.name, allowedProd: it.allowedProducts, createdBy: "System", modifiedBy: "System",)
                    safetyGroup.save(failOnError: true, flush: true)
                }
            }
            log.debug "Safety Lead Creation Completed"
        }
    }

    void createSignalWorkflow() {
        if (SignalWorkflowRule.count() <= 0)
            grailsApplication.config.configurations.signalWorkflowRule.each {
                List<Group> allowedUserGroups
                if (it.allowedGroups != [])
                    allowedUserGroups = Group.findAllByNameInList(it.allowedGroups)

                def signalWorkflowRule = new SignalWorkflowRule(ruleName: it.ruleName, fromState: it.fromState, toState: it.toState, description: it.description, display: it.display)

                allowedUserGroups?.each { Group allowedGroup ->
                    signalWorkflowRule.addToAllowedGroups(allowedGroup)
                }
                signalWorkflowRule.dateCreated = new Date()
                signalWorkflowRule.save(flush: true, failOnError: true)
            }

        if (SignalWorkflowState.count() <= 0)
            grailsApplication.config.configurations.signalWorkflowState.each {
                List<Disposition> allowedDispositions
                if (it.allowedDispositions != [])
                    allowedDispositions = Disposition.findAllByDisplayNameInList(it.allowedDispositions)

                def signalWorkflowState = new SignalWorkflowState(value: it.value, displayName: it.displayName, description: it.description, defaultDisplay: it.defaultDisplay, dueInDisplay: it.dueInDisplay)

                allowedDispositions?.each { Disposition allowedDisposition ->
                    signalWorkflowState.addToAllowedDispositions(allowedDisposition)
                }
                signalWorkflowState.save(flush: true, failOnError: true)
            }
    }

    private generateDisposition() {
        if (isFreshBuild) {
            List dispositions = grailsApplication.config.configurations.freshBuild.dispositions
            dispositions.each {
                new Disposition(value: it.value, display: it.display, displayName: it.displayName,
                        description: it.description, validatedConfirmed: it.validatedConfirmed, reviewCompleted: it.reviewCompleted, closed: it.closed, notify: it.notify, abbreviation: it.abbreviation, colorCode: it.colorCode, resetReviewProcess: it.resetReview).save(flush: true, failOnError: true)

            }
        } else {
            //Create dispositions if not present.
            if (Disposition.count() == 0) {
                List dispositions = grailsApplication.config.configurations.dispositions
                dispositions.each {
                    new Disposition(value: it.value, display: it.display, displayName: it.displayName,
                            description: it.description, validatedConfirmed: it.validatedConfirmed, reviewCompleted: it.reviewCompleted, closed: it.closed, notify: it.notify, abbreviation: it.abbreviation, colorCode: it.colorCode).save(flush: true, failOnError: true)
                }
            } else if (Disposition.first().abbreviation == "-") {
                Disposition disposition
                grailsApplication.config.configurations.dispositions.each {
                    disposition = Disposition.findByValue(it.value)
                    disposition.colorCode = it.colorCode
                    disposition.abbreviation = it.abbreviation
                    disposition.save(flush: true, failOnError: true)
                }
                //Create dispositions if not present.
                if (Disposition.count() == 0) {
                    List dispositions = grailsApplication.config.configurations.dispositions
                    dispositions.each {
                        new Disposition(value: it.value, display: it.display, displayName: it.displayName,
                                description: it.description, validatedConfirmed: it.validatedConfirmed, reviewCompleted: it.reviewCompleted, closed: it.closed, notify: it.notify, abbreviation: it.abbreviation, colorCode: it.colorCode).save(flush: true, failOnError: true)
                    }
                } else if (Disposition.first().abbreviation == "-") {
                    grailsApplication.config.configurations.dispositions.each {
                        disposition = Disposition.findByValue(it.value)
                        disposition.colorCode = it.colorCode
                        disposition.abbreviation = it.abbreviation
                        disposition.save(flush: true, failOnError: true)
                    }
                }

            }
        }
    }


    def createMedicalConcepts() {
        def medicalConcepts = MedicalConcepts.count()
        if (medicalConcepts == 0) {
            def list = grailsApplication.config.configurations.medicalConcepts
            list.each {
                MedicalConcepts medicalConcepts1 = new MedicalConcepts(name: it)
                medicalConcepts1.save(failOnError: true)
            }
        }
    }

    def createJustifications() {
        def justifications = Justification.count()
        if (justifications == 0) {
            def list = grailsApplication.config.configurations.justifications
            Justification justification
            Map justificationFeatures = [:]
            list.each {
                justificationFeatures.alertWorkflow = it.alertWorkflow ? "on" : ""
                justificationFeatures.signalWorkflow = it.signalWorkflow ? "on" : ""
                justificationFeatures.topicWorkflow = it.topicWorkflow ? "on" : ""
                justificationFeatures.alertPriority = it.alertPriority ? "on" : ""
                justificationFeatures.signalPriority = it.signalPriority ? "on" : ""
                justificationFeatures.topicPriority = it.topicPriority ? "on" : ""
                justificationFeatures.caseAddition = it.caseAddition ? "on" : ""
                justification = new Justification(name: it.templateName, justification: it.justificationText, feature: new JsonBuilder(justificationFeatures).toPrettyString())
                if (it.allowedDispositions != []) {
                    it.allowedDispositions.each {
                        Disposition justificationDisposition = Disposition.findByDisplayName(it)
                        justification.addToDispositions(justificationDisposition)
                    }
                }
                justification.save(failOnError: true)
            }
        }
    }

    void createTopicCategory() {
        List<String> list = grailsApplication.config.pvsignal.freshBuild.topicCategory
        Sql sql
        try {
            List namesOfTopicCategoriesConfig = list?.collect { it as String } ?: []
            List currentTopicCategories = TopicCategory.findAll()
            if(currentTopicCategories){
                List<TopicCategory> topicCategoriesToDelete = currentTopicCategories.findAll { !namesOfTopicCategoriesConfig.contains(it.name) }
                if (topicCategoriesToDelete) {
                    String whereClauseForMapping = ""
                    String whereClause = ""
                    topicCategoriesToDelete*.id.collate(999).each {
                        if (whereClauseForMapping == "") {
                            whereClauseForMapping += "WHERE TOPIC_CATEGORY_ID IN (${it.join(',')})"
                        } else {
                            whereClauseForMapping += " OR TOPIC_CATEGORY_ID IN  (${it.join(',')})"
                        }
                        if (whereClause == "") {
                            whereClause += "WHERE ID IN (${it.join(',')})"
                        } else {
                            whereClause += " OR ID IN  (${it.join(',')})"
                        }
                    }
                    sql = new Sql(dataSource)
                    String sqlString = "DELETE FROM VAL_SIGNAL_TOPIC_CATEGORY " + whereClauseForMapping
                    sql.execute(sqlString)
                    sqlString = "DELETE FROM TOPIC_CATEGORY " + whereClause
                    sql.execute(sqlString)
                }
            }
        }catch (Exception ex){
            ex.printStackTrace()
        }finally {
            sql?.close()
        }
        TopicCategory newTopicCategory
        list?.each {
            newTopicCategory = TopicCategory.findByName(it)
            if (!newTopicCategory) {
                newTopicCategory = new TopicCategory(name: it)
                newTopicCategory.save()
            }
        }
    }

    void createSignalOutcome() {
        List list = grailsApplication.config.pvsignal.freshBuild.signalOutcome
        List namesOfSignalOutcomeConfig = list?.collect { it.value as String } ?: []
        List currentOutcomes = SignalOutcome.findAll()
        if(currentOutcomes){
            List<SignalOutcome> outcomesToDelete = currentOutcomes.findAll { !namesOfSignalOutcomeConfig.contains(it.name) }
            outcomesToDelete.each { outcome ->
                outcome.isDeleted = true
                outcome.isDisabled = true
                outcome.save(flush: true)
            }
        }
        list?.each { def outcomeMap ->
            SignalOutcome signalOutcome = SignalOutcome.findByName(outcomeMap.value as String)
            Boolean isDisabled = outcomeMap instanceof String ? false : outcomeMap.isDisabled
            Boolean isDeleted = outcomeMap instanceof String ? false : outcomeMap.isDeleted
            if (!signalOutcome) {
                signalOutcome = new SignalOutcome(name: outcomeMap.value, isDisabled: isDisabled, isDeleted: isDeleted)
            } else if (signalOutcome.isDisabled != isDisabled || signalOutcome.isDeleted != isDeleted) {
                signalOutcome.isDisabled = isDisabled
                signalOutcome.isDeleted = isDeleted
            }
            signalOutcome.save()
        }
    }

    void saveApplicationData() {
        generateSCATemplate()
        generateAGATemplate()
        generateWorkflowManagementData()
    }

    // create a data tabulation template for dev testing
    void generateSCATemplate() {
        log.debug("Generate SCA Template")
        CaseLineListingTemplate caseLineTemplate = CaseLineListingTemplate.findByName('Case_num')
        def seedingUser = Holders.config.pvsignal.seeding.user
        ReportFieldInfoList reportFileList = getReportFieldList(caseLineTemplate)
        if (caseLineTemplate) {
            caseLineTemplate.columnList = reportFileList
        } else {
            caseLineTemplate = new CaseLineListingTemplate(templateType: TemplateTypeEnum.CASE_LINE,
                    name: 'Case_num', createdBy: "normalUser", modifiedBy: "normalUser",
                    columnList: reportFileList, originalTemplateId: 1,
                    owner: User.findByUsername(seedingUser), isPublic: true)
        }
        caseLineTemplate.save(failOnError: true)
    }

    private getReportFieldList(CaseLineListingTemplate caseLineTemplate) {

        ReportFieldInfoList reportFileList
        if (caseLineTemplate) {
            reportFileList = caseLineTemplate.columnList
        } else {
            reportFileList = new ReportFieldInfoList()
        }

        //This list contains the name of the report field parameter name. In some cases stack id is required thus is appended as :1, :2 or :3
        def reportFieldParamList = [
                'masterCaseNum:1', 'masterVersionNum:1', 'reportersHcpFlag:1', 'masterRptTypeId:1', 'masterInitReptDate:2',
                'masterFollowupDate:2', 'masterPrefTermAll:1', 'assessOutcome:1', 'eventConserCoreListedness:3', 'productFamilyId:1',
                'masterFupNum:1', 'masterFlagSt:1', 'productProductStudyDrug:1', 'masterPrimEvtPrefTerm:1', 'masterPrimProdName:1',
                'eventEvtOutcomeId:1', 'assessSeriousness:1', 'masterSuspProdAgg:1', 'masterConcomitProdList:1', 'masterCountryId:1',
                'patInfoAgeGroupId:1', 'patInfoGenderId:1', 'prodDrugsPosRechallenge:1', 'masterDateLocked:1', 'cfDeathFlag:1',
                'eventPrefTerm:1', 'cpiProdIdResolved:1', 'productProductId:1', 'masterCaseId:1', 'narrativeNarrative:1',
                'ccconcomitProductAll:1', 'ccAePt:1', 'assessListedness:1', 'ccconcomitProd2All:1', 'cmadlflagEligibleLocalExpdtd:1',
                'caseMasterPvrUdText12:1', 'vwcpai1FlagCompounded:1', 'casePatInfoPvrUdNumber2:1', 'cdrClobAeAllUdUdClob1:1',
                'caseProdDrugsPvrUdText20:1', 'caseProdDrugsPvrUdNumber10:1', 'caseProdDrugsPvrUdNumber11:1',
                'csisender_organization:1', 'flagMasterCase:1', 'malfunctionDevices:1', 'deviceComboProduct:1', 'patInfoPatientAgeYears:1',
                'productIndCoddorReptd:1', 'ccCoddRptdCauseDeathAll:1', 'cprmConditionAll:1',
                'ccMedHistDrugAll:1', 'productLotNoAllcs:1', 'masterPrefTermSurAll:1', 'dvProdEventTimeOnsetDays:1', 'masterCharactersticAllcs:1',
                'ciTxtDateReceiptInitial:1', 'cifTxtDateLocked:1', 'vwstudyProtocolNum:1',
                'masterSusar:1', 'productStartStopDateAllcs:1', 'productDoseDetailAllcs:1', 'ceSerUnlRel:1', 'PreAndastudyStudyNum:1', 'CsHcpFlag:1', 'productInterestId:1', 'eventDescReptd:1',
                'ciPrimSrccountryId:1', 'caseChangesConcat:1', 'masterCloseDate:1','ciFlagUnblind:1','studyCodeUnBroken:1',
                'analytic.caseInformation.cmflagcopackproduct:1'
        ]

        if (Holders.config.custom.qualitative.fields.enabled) {
            reportFieldParamList.add('vwcsiSponsorStudyNumber:1')
            reportFieldParamList.add('vwstudyClassificationId:1')
            reportFieldParamList.add('CrossRefIndstudyStudyNum:1')
        } else {
            reportFieldParamList.add('crepoud_text_11:1')
            reportFieldParamList.add('crepoud_text_12:1')
        }
        reportFieldParamList.add('cifInitialDateLocked:1')
        if (Holders.config.custom.qualitative.fields.enabled) {
            reportFieldParamList.add('dciPrimSuspectPai:1')
            reportFieldParamList.add('dciPaiAll:1')
        }
        reportFieldParamList.add('masterSuspProdList:1')
        if (Holders.config.custom.qualitative.fields.enabled) {
            reportFieldParamList.add('csiderived_country:1')
        }
        reportFieldParamList.addAll(['masterCreateTime:1', 'patInfoPatDobPartial:1', 'eventPrimaryOnsetDatePartial:1', 'masterPregnancyFlag:1', 'masterHcpFlag:1', 'masterPrefTermList:1'])
        reportFieldParamList.add('masterCloseNotes:1')
        reportFieldParamList.add('masterFatalFlag:1')
        reportFieldParamList.add('productProductName:1')
        reportFieldParamList.add('reportersReporterType:1')
        reportFieldParamList.add('riskCategory:1')
        // To add custom expression use this map
        Map customExpressionMap = ["masterSuspProdList":"rtrim(ltrim(REGEXP_REPLACE (REPLACE(REPLACE( ccpa.SUSP_PROD_2_ALL ,'!@##@!',', '),'!@_@!','-'),'[0-9]'||'!@.@!',''),', '),', ')",
                                    "masterPregnancyFlag":"(SELECT a.state_ynu FROM vw_clp_state_ynu a WHERE a.id=cf.flag_pregnancy AND a.tenant_id=cf.tenant_id AND a.lang_id = '1')",
                                   "eventPrimaryOnsetDatePartial":"SUBSTR(ce.TXT_DATE_AE_START,1,11)",
                                   "patInfoPatDobPartial":"SUBSTR(cpi.TXT_DATE_PATIENT_BIRTH,1,11)"]
        List existingReportFieldInfoList = reportFileList?.reportFieldInfoList?.collect { it.argusName } ?: []
        List reportFieldNameList = reportFieldParamList.collect { it.split(":")[0] }
        List rptFieldInfoToBeDeleted = existingReportFieldInfoList.minus(reportFieldNameList)
        List rptFieldInfoToBeAdded = reportFieldNameList.minus(existingReportFieldInfoList)
        boolean isDirty = false
        customExpressionMap.each{ key,value ->
            ReportFieldInfo reportFieldInfo = reportFileList?.reportFieldInfoList?.find { it.argusName == key && it.customExpression != value }
            if(reportFieldInfo){
                reportFieldInfo.customExpression = value
                reportFieldInfo.save(flush:true)
            }
        }
        if (rptFieldInfoToBeAdded || rptFieldInfoToBeDeleted) {
            // Note: On some server, ReportField entity didn't retun newly added fields after adding in Mart table rpt_field.
            // To overcome this, clearing cache for ReportField entityReportField so that fresh data be retrived from table itself.
            sessionFactory.getCache().evictEntityRegion(ReportField.class)
            reportFieldParamList.each {

                def tokens = (it)?.split(":")
                def fieldName = tokens[0]
                String customExpression = customExpressionMap.get(fieldName)
                def stackId = null
                if (tokens.length > 1) {
                    stackId = tokens[1]
                }
                if (!reportFileList?.reportFieldInfoList?.count { it.argusName == fieldName }) {

                    def reportField = ReportField.findByName(fieldName)
                    if (reportField) {
                        isDirty = true
                        ReportFieldInfo column1 = new ReportFieldInfo(reportField: reportField, argusName: fieldName, stackId: stackId,
                                customExpression: customExpression)
                        reportFileList.addToReportFieldInfoList(column1)
                    }
                }

            }
            rptFieldInfoToBeDeleted.each { String fieldName ->
                if (reportFileList?.reportFieldInfoList?.count { it.argusName.equals(fieldName) }) {
                    ReportField reportField = ReportField.findByName(fieldName)
                    if (reportField) {
                        ReportFieldInfo reportFieldInfo = ReportFieldInfo.createCriteria().get {
                            eq("reportField", reportField)
                            maxResults(1)
                        }
                        if (reportFieldInfo) {
                            isDirty = true
                            reportFileList.removeFromReportFieldInfoList(reportFieldInfo)
                        }
                    }
                }
            }

            try {
                if (isDirty)
                    reportFileList.save(failOnError: true)
            } catch (Throwable t) {
                t.printStackTrace()
                log.error("" + reportFileList.hasErrors())
                log.error("Not able to save ReportFieldInfoList.")
            }
        }
        reportFileList
    }

    def generateAGATemplate() {

        CustomSQLTemplate cust_agg_temp = CustomSQLTemplate.findByName('cust_agg_temp')
        def adminUser = Holders.config.pvsignal.seeding.user
        if (!cust_agg_temp) {
            cust_agg_temp = new CustomSQLTemplate(templateType: TemplateTypeEnum.CUSTOM_SQL,
                    name: 'cust_agg_temp', createdBy: adminUser, modifiedBy: adminUser,
                    columnNamesList: """[PRODUCT_NAME,
                    SOC, LLT, PT, HLT, HLGT, NEW_FATAL_COUNT, CUMM_FATAL_COUNT, NEW_SERIOUS_COUNT,
                    CUMM_SERIOUS_COUNT, NEW_STUDY_COUNT, CUMM_STUDY_COUNT, NEW_SPON_COUNT,
                    CUMM_SPON_COUNT, PRR_VALUE, NEW_FATAL_CASELIST, CUMM_FATAL_CASELIST,
                    NEW_SERIOUS_CASELIST, CUMM_SERIOUS_CASELIST, NEW_STUDY_CASELIST,
                    CUMM_STUDY_CASELIST, NEW_SPON_CASELIST, CUMM_SPON_CASELIST]""",
                    customSQLTemplateSelectFrom: """
            SELECT gccd.PRODUCT_NAME
            AS PRODUCT_NAME,
            gccd.MEDDRA_SOC            AS SOC,
            gccd.MEDDRA_LLT            AS LLT,
            gccd.MEDDRA_PT             AS PT,
            gccd.MEDDRA_HLT            AS HLT,
            gccd.MEDDRA_HLGT           AS HLGT,
            gccd.NEW_FATAL_COUNT       AS NEW_FATAL_COUNT,
            gccd.CUMM_FATAL_COUNT      AS CUMM_FATAL_COUNT,
            gccd.NEW_SERIOUS_COUNT     AS NEW_SERIOUS_COUNT,
            gccd.CUMM_SERIOUS_COUNT    AS CUMM_SERIOUS_COUNT,
            gccd.NEW_STUDY_COUNT       AS NEW_STUDY_COUNT,
            gccd.CUMM_STUDY_COUNT      AS CUMM_STUDY_COUNT,
            gccd.NEW_SPON_COUNT        AS NEW_SPON_COUNT,
            gccd.CUMM_SPON_COUNT       AS CUMM_SPON_COUNT,
            gapd.PRR_VALUE             AS PRR_VALUE,
            gccl.NEW_FATAL_CASELIST    AS NEW_FATAL_CASELIST,
            gccl.CUMM_FATAL_CASELIST   AS CUMM_FATAL_CASELIST,
            gccl.NEW_SERIOUS_CASELIST  AS NEW_SERIOUS_CASELIST,
            gccl.CUMM_SERIOUS_CASELIST AS CUMM_SERIOUS_CASELIST,
            gccl.NEW_STUDY_CASELIST    AS NEW_STUDY_CASELIST,
            gccl.CUMM_STUDY_CASELIST   AS CUMM_STUDY_CASELIST,
            gccl.NEW_SPON_CASELIST     AS NEW_SPON_CASELIST,
            gccl.CUMM_SPON_CASELIST    AS CUMM_SPON_CASELIST
            FROM  GTT_AGG_NEW_CUMM_CASELIST gccl
            JOIN GTT_AGG_NEW_CUMM_COUNT_DATA gccd
            ON (gccl.ID       = gccd.ID
            AND gccl.tenant_id = gccd.tenant_id )
            JOIN GTT_AGG_PRR_DATA gapd
            ON (gccl.ID       = gapd.ID
            AND gccl.tenant_id = gapd.tenant_id )""", isPublic: true, owner: User.findByUsername(adminUser))
            cust_agg_temp.save(failOnError: true)
        }

    }

    def initTempSpace() {
        def tempSpace = new File(Holders.config.tempDirectory)
        log.info("Initializing application temp space in ${tempSpace.absolutePath}")
        try {
            tempSpace.mkdirs()
        } catch (SecurityException se) {
            log.error(se.message)
        }
    }

    //This timeZone is for TimeCategory which  will now use UTC as default.
    def setDefaultTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    def injectMessageSource() {

        ViewHelper.class.metaClass.getMessageSource = {
            messageSource
        }
        ViewHelper.class.metaClass.static.getMessageSource = {
            messageSource
        }
    }

    def destroy = {
        showShutdown()
    }

    def registerMarshallers(servletContext) {
        def springContext = WebApplicationContextUtils.getWebApplicationContext(servletContext)
        springContext.getBean("customMarshallerRegistry").register()
    }


    def setRoleHierarchy() {
        grailsApplication.mainContext.roleHierarchy.hierarchy = """
         ROLE_DEV > ROLE_ADMIN
         ROLE_ADMIN > ROLE_CONFIGURATION_CRUD
         ROLE_ADMIN > ROLE_MANAGE_PRODUCT_ASSIGNMENTS
         ROLE_CONFIGURE_TEMPLATE_ALERT < ROLE_CONFIGURATION_CRUD
         ROLE_CONFIGURE_TEMPLATE_ALERT < ROLE_SHARE_GROUP
         ROLE_CONFIGURE_TEMPLATE_ALERT < ROLE_SHARE_ALL
         ROLE_ADMIN > ROLE_CONFIGURE_TEMPLATE_ALERT
         ROLE_CONFIGURATION_CRUD > ROLE_CONFIGURATION_VIEW
         ROLE_ADMIN > ROLE_QUERY_ADVANCED
         ROLE_QUERY_ADVANCED > ROLE_QUERY_CRUD
         ROLE_QUERY_CRUD > ROLE_QUERY_VIEW
         ROLE_ADMIN > ROLE_TEMPLATE_ADVANCED
         ROLE_TEMPLATE_ADVANCED > ROLE_TEMPLATE_CRUD
         ROLE_TEMPLATE_CRUD > ROLE_TEMPLATE_VIEW
         ROLE_ADMIN > ROLE_SINGLE_CASE_CONFIGURATION
         ROLE_SINGLE_CASE_CONFIGURATION > ROLE_SINGLE_CASE_REVIEWER
         ROLE_SINGLE_CASE_REVIEWER > ROLE_SINGLE_CASE_VIEWER
         ROLE_ADMIN > ROLE_AGGREGATE_CASE_CONFIGURATION
         ROLE_AGGREGATE_CASE_CONFIGURATION > ROLE_AGGREGATE_CASE_REVIEWER
         ROLE_AGGREGATE_CASE_REVIEWER > ROLE_AGGREGATE_CASE_VIEWER
         ROLE_ADMIN > ROLE_LITERATURE_CASE_CONFIGURATION
         ROLE_LITERATURE_CASE_CONFIGURATION > ROLE_LITERATURE_CASE_REVIEWER
         ROLE_LITERATURE_CASE_REVIEWER > ROLE_LITERATURE_CASE_VIEWER
         ROLE_ADMIN > ROLE_SIGNAL_MANAGEMENT_CONFIGURATION
         ROLE_SIGNAL_MANAGEMENT_CONFIGURATION > ROLE_SIGNAL_MANAGEMENT_REVIEWER
         ROLE_SIGNAL_MANAGEMENT_REVIEWER > ROLE_SIGNAL_MANAGEMENT_VIEWER
         
         ${Holders.config.signal.evdas.enabled ? '''ROLE_ADMIN > ROLE_EVDAS_CASE_CONFIGURATION
                ROLE_EVDAS_CASE_CONFIGURATION > ROLE_EVDAS_CASE_REVIEWER
                ROLE_EVDAS_CASE_REVIEWER > ROLE_EVDAS_CASE_VIEWER      
                ''' : ''}
         ${Holders.config.signal.faers.enabled ? '''
                ROLE_ADMIN > ROLE_FAERS_CONFIGURATION
                ''': ''}

         ${Holders.config.signal.vaers.enabled ? '''
                ROLE_ADMIN > ROLE_VAERS_CONFIGURATION
                ''': ''}
         
         ${Holders.config.signal.vigibase.enabled ? '''
                ROLE_ADMIN > ROLE_VIGIBASE_CONFIGURATION
                ''': ''}
                ''' : ''}
         ${Holders.config.signal.jader.enabled ? '''
                ROLE_ADMIN > ROLE_JADER_CONFIGURATION
                ''': ''}
                ''' : ''}

        ${Holders.config.system.precheck.configuration.enable ? '''
                    ROLE_HEALTH_CONFIGURATION
                ''': ''}
                ''' : ''}

         ROLE_ADMIN > ROLE_AD_HOC_CRUD
         ${Holders.config.signal.spotfire.enabled ? 'ROLE_ADMIN > ROLE_DATA_ANALYSIS' : ''}
         ${Holders.config.signal.spotfire.enabled ? 'ROLE_ADMIN > ROLE_PRODUCTIVITY_AND_COMPLIANCE' : ''}
         ROLE_ADMIN > ROLE_DATA_MINING
         ROLE_ADMIN > ROLE_TASK_CRUD
         ROLE_TASK_CRUD > ROLE_TASK_VIEW
         ROLE_ADMIN > ROLE_COGNOS_CRUD
         ROLE_COGNOS_CRUD > ROLE_COGNOS_VIEW
         ROLE_ADMIN > ROLE_SHARE_GROUP
         ROLE_ADMIN > ROLE_SHARE_ALL
         ROLE_ADMIN > ROLE_CATEGORY_SUBCATEGORY_MANAGEMENT
         ROLE_ADMIN > ROLE_OPERATIONAL_METRICS
         ROLE_ADMIN > ROLE_EXECUTE_SHARED_ALERTS
         ROLE_ADMIN > ROLE_REPORTING
         ROLE_ADMIN > ROLE_EVDAS_AUTO_DOWNLOAD
         ROLE_ADMIN > ROLE_DATA_MINING_ADVANCED_RUNS
       """
    }

    private generateWorkflowManagementData() {

        def workflowList
        //Create priority if not present.
        if (Priority.count() == 0) {
            grailsApplication.config.configurations.priorities.each {
                Priority pr = new Priority(value: it.value, display: true, priorityOrder: it.order,
                        displayName: it.value, reviewPeriod: it.reviewPeriod,
                        defaultPriority: it.defaultPriority, iconClass: it.iconClass)
                pr.save(flush: true)
            }
        }

        //Create Work flow rules if not present.
        ActivityTypeValue.values().each {
            ActivityType.findOrSaveByValue(it)
        }

        if (isFreshBuild) {
            workflowList = grailsApplication.config.configurations.freshBuild.workflows

        } else {
            workflowList = grailsApplication.config.configurations.workflows
        }
        //Create Work flow rules if not present.
        if (DispositionRule.count() == 0) {
            workflowList.each {
                Disposition incomingDisposition = Disposition.findByDisplayName(it.incomingDisposition)
                Disposition targetDisposition = Disposition.findByDisplayName(it.targetDisposition)
                List<Group> userGroups = Group.findAllByNameInList(it.allowedUserGroups)
                List<Group> workflowGroups = Group.findAllByNameInList(it.allowedWorkflowGroups)

                DispositionRule dispositionRule = new DispositionRule(name: it.ruleName, incomingDisposition: incomingDisposition, targetDisposition: targetDisposition)
                userGroups?.each { Group userGroup ->
                    dispositionRule.addToAllowedUserGroups(userGroup)
                }
                workflowGroups?.each { Group workflowGroup ->
                    dispositionRule.addToWorkflowGroups(workflowGroup)
                }
                dispositionRule.validate()
                if (!dispositionRule.hasErrors()) {
                    dispositionRule.save()
                }
            }
        }
    }



    def cachePreCheckValue(){
        try{
            def preCheckList = [:]
            Map keyMap = [:]
            preCheckList = preCheckService.preCheckList().keySet().each{
                List keys = it.split('_')
                keyMap[keys[0]] = [keys[1],keys[2]]
            }
            cacheService.savePreCheckCache(preCheckList as String)
        } catch (Exception c) {
            log.warn('Error in Pre Check caching')
        }

    }

    def cacheAdHocAlertAttributes() {
        alertAttributesService.slurp(null)
        alertAttributesService.attributesMap
        List evaluationMethods = grailsApplication.config.configurations.evaluationMethods
        List datasources = grailsApplication.config.configurations.datasources
        List modifiedEvaluationMethods = []
        List modifiedDataSourceMethods = []
        evaluationMethods.each {
            modifiedEvaluationMethods << [value: it]
        }
        datasources.each {
            modifiedDataSourceMethods << [value: it.dataSource]
        }
        alertAttributesService.attributesMap.evaluationMethods = alertAttributesService.attributesMap["evaluationMethods"] ?: modifiedEvaluationMethods
        alertAttributesService.attributesMap.initialDataSource = alertAttributesService.attributesMap["initialDataSource"] ?: modifiedDataSourceMethods
    }


    def scheduleJobs() {
        def schedules = Holders.config.pvsignal.task.schedules
        schedules.each {
            try {
                def jobInstance = Class.forName(it.get("job") + "Job")
                if (it.get('cronExpression'))
                    jobInstance.schedule(it.get('cronExpression'))
                else
                    jobInstance?.schedule(it.get('repeatInterval'), it.get("repeatCount") ?: -1)
            } catch (Exception e) {
                log.error("Job Scheduler failed for " + it.get("job") + ":", e)
            }

        }
    }

    def changeMultipartResolver(ServletContext servletContext) {
        // get application
        def application = Holders.findApplication()

        def config = Holders.config

        // upload dir
        fixUploadDir config

        // enhance domain classes
        application.domainClasses?.each { d ->
            if (Attachmentable.class.isAssignableFrom(d.clazz) || getAttachmentableProperty(d)) {
                addDomainMethods config, d.clazz.metaClass, attachmentableService
            }
        }
    }

    public static final String ATTACHMENTABLE_PROPERTY_NAME = "attachmentable";

    private getAttachmentableProperty(domainClass) {
        GrailsClassUtils.getStaticPropertyValue(domainClass.clazz, ATTACHMENTABLE_PROPERTY_NAME)
    }

    private void fixUploadDir(config) {
        def dir = config.grails.attachmentable?.uploadDir
        if (!dir) {
            String userHome = System.properties.'user.home'
            String appName = application.metadata['app.name']
            dir = new File(userHome, appName).canonicalPath
            config.grails.attachmentable.uploadDir = dir
        }
        log.debug "Attachmentable config(upload dir): '$dir'"
    }

    private generateEmailNotification() {
            List modules = grailsApplication.config.mail.notification.modules
            modules.each {
                if (!EmailNotification.findByKey(it.key)){
                    new EmailNotification(key: it.key, moduleName: it.moduleName, isEnabled: it.defaultValue, defaultValue: it.defaultValue).save(flush: true, failOnError: true)

                }
            }

    }

    private void addControllerMethods(def config,
                                      MetaClass mc,
                                      AttachmentableService service) {
        mc.uploadStatus = {
            def controllerInstance = delegate
            def request = controllerInstance.request

            ProgressDescriptor pd = request.session[AMR.progressAttrName(request)]
            controllerInstance.render(pd ?: '')
        }

        mc.attachUploadedFilesTo = { reference, List inputNames = [] ->
            def controllerInstance = delegate
            def request = controllerInstance.request

            if (AttachmentableUtil.isAttachmentable(reference)) {
                // user
                def evaluator = config.grails.attachmentable.poster.evaluator
                def user = null

                if (evaluator instanceof Closure) {
                    evaluator.delegate = controllerInstance
                    evaluator.resolveStrategy = Closure.DELEGATE_ONLY
                    user = evaluator.call()
                }

                if (!user) {
                    throw new AttachmentableException(
                            "No [grails.attachmentable.poster.evaluator] setting defined or the evaluator doesn't evaluate to an entity or string. Please define the evaluator correctly in grails-app/conf/Config.groovy or ensure attachmenting is secured via your security rules.")
                }

                if (!(user instanceof String) && !user.id) {
                    throw new AttachmentableException(
                            "The evaluated Attachment poster is not a persistent instance.")
                }

                // files
                List<MultipartFile> filesToUpload = []
                List<MultipartFile> uploadedFiles = []

                if (request instanceof DefaultMultipartHttpServletRequest) {
                    request.multipartFiles.each { k, v ->
                        if (!inputNames || inputNames.contains(k)) {
                            if (v instanceof List) {
                                v.each { MultipartFile file ->
                                    filesToUpload << file
                                }
                            } else {
                                filesToUpload << v
                            }
                        }
                    }

                    // upload
                    uploadedFiles = service.upload(user, reference, filesToUpload)
                }

                // result
                [filesToUpload: filesToUpload, uploadedFiles: uploadedFiles]
            }
        }

        mc.existAttachments = {
            def controllerInstance = delegate
            def request = controllerInstance.request
            def result = false

            if (request instanceof DefaultMultipartHttpServletRequest) {
                request.multipartFiles.each { k, v ->
                    if (v instanceof List) {
                        v.each { MultipartFile file ->
                            if (file.size)
                                result = true
                        }
                    } else {
                        if (v.size)
                            result = true
                    }
                }
            }

            return result
        }
    }


    def initiateDynamicTables() {
        // Initialize a temporary list for dynamic field configurations
        List<Map> tempDynamicFieldConfigs = []

        List<Map> dynamicFieldConfigs = Holders.config.signal.summary.dynamic.fields

        // Process each field configuration
        if(dynamicFieldConfigs) {
            dynamicFieldConfigs.each { fieldConfig ->
                fieldConfig.enabled = fieldConfig.enabled?.toBoolean() ?: false
                fieldConfig.largeText = fieldConfig.largeText?.toBoolean() ?: false
                tempDynamicFieldConfigs << fieldConfig
            }
        }
        Holders.config.signal.summary.dynamic.fields = tempDynamicFieldConfigs
        def parsedConfig = [:]
        def dropdownConfig = Holders.config.signal.summary.dynamic.dropdown.values

        if(dropdownConfig) {
            dropdownConfig.each { key, value ->
                try {
                    def list = Eval.me(value)
                    def map = list.collectEntries { [(it.key): it.value] }
                    parsedConfig[key] = map
                } catch (Exception e) {
                    log.error("Error parsing config for ${key}: ${e.message}")
                }
            }
        }
        Holders.config.signal.summary.dynamic.dropdown.values = parsedConfig
    }

    /**
     * Populates dynamic label configuration for audit purposes based on provided configuration data.
     */
    void populateDisplayConfigForDynamicFields() {
        AuditLogConfigUtil.populateLabelConfigForAudit()
    }

    def addSignalToAlert() {
        aggregateCaseAlertService.savePreviousExecutedAggAlertWithSignal()
        evdasAlertService.savePreviousExecutedEvdasAlertWithSignal()
    }

    List<ReportField> getAllReportFields() {
        return reportFieldService.getAllReportFields();
    }

    List<ReportFieldGroup> getReportFields() {
        return queryService.getReportFields();
    }

    private void addDomainMethods(def config,
                                  MetaClass mc,
                                  AttachmentableService service) {
        // add

        mc.addAttachment = { def poster,
                             CommonsMultipartFile file ->
            service.addAttachment(poster, delegate, file)
        }

        // get

        mc.getAttachments = { String inputName, params = [:] ->
            getAttachments(inputName ? [inputName] : [], params)
        }

        mc.getAttachments = { List inputNames = [], params = [:] ->
            service.findAttachmentsByReference(delegate, inputNames, params)
        }

        mc.getReferences = { List referenceLinks = [], params = [:] ->
            service.findReferenceLinkByReference(delegate, referenceLinks, params)
        }

        // count

        mc.getTotalAttachments = { String inputName ->
            getTotalAttachments(inputName ? [inputName] : [])
        }

        mc.getTotalAttachments = { List inputNames = [] ->
            service.countAttachmentsByReference(delegate, inputNames)
        }

        // remove

        mc.removeAttachments = { ->
            service.removeAttachments(delegate)
        }

        mc.removeAttachments = { List inputNames ->
            service.removeAttachments(delegate, inputNames)
        }

        mc.removeAttachment = { Attachment attachment ->
            service.removeAttachment(attachment)
        }

        mc.removeAttachment = { Long attachmentId ->
            service.removeAttachment(attachmentId)
        }

        mc.removeAttachment = { String inputName ->
            removeAttachments([inputName])
        }
    }

    private initDictionaryPlugin() {
        PVDictionaryConfig.initialize(ProductDictionaryMetadata.findAllByDisplay(true))
    }

    private showStartup() {
        def startup = """
            uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu  
            ██████╗ ██╗   ██╗    ███████╗██╗ ██████╗ ███╗   ██╗ █████╗ ██╗         ███████╗████████╗ █████╗ ██████╗ ████████╗███████╗██████╗
            ██╔══██╗██║   ██║    ██╔════╝██║██╔════╝ ████╗  ██║██╔══██╗██║         ██╔════╝╚══██╔══╝██╔══██╗██╔══██╗╚══██╔══╝██╔════╝██╔══██╗
            ██████╔╝██║   ██║    ███████╗██║██║  ███╗██╔██╗ ██║███████║██║         ███████╗   ██║   ███████║██████╔╝   ██║   █████╗  ██║  ██║
            ██╔═══╝ ╚██╗ ██╔╝    ╚════██║██║██║   ██║██║╚██╗██║██╔══██║██║         ╚════██║   ██║   ██╔══██║██╔══██╗   ██║   ██╔══╝  ██║  ██║
            ██║      ╚████╔╝     ███████║██║╚██████╔╝██║ ╚████║██║  ██║███████╗    ███████║   ██║   ██║  ██║██║  ██║   ██║   ███████╗██████╔╝
            ╚═╝       ╚═══╝      ╚══════╝╚═╝ ╚═════╝ ╚═╝  ╚═══╝╚═╝  ╚═╝╚══════╝    ╚══════╝   ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═╝   ╚═╝   ╚══════╝╚═════╝
            zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz
                        """
        log.info(startup)
    }

    private showShutdown() {
        def shutdown = """
            ██████╗ ██╗   ██╗    ███████╗██╗ ██████╗ ███╗   ██╗ █████╗ ██╗         ███████╗██╗  ██╗██╗   ██╗████████╗██████╗  ██████╗ ██╗    ██╗███╗   ██╗
            ██╔══██╗██║   ██║    ██╔════╝██║██╔════╝ ████╗  ██║██╔══██╗██║         ██╔════╝██║  ██║██║   ██║╚══██╔══╝██╔══██╗██╔═══██╗██║    ██║████╗  ██║
            ██████╔╝██║   ██║    ███████╗██║██║  ███╗██╔██╗ ██║███████║██║         ███████╗███████║██║   ██║   ██║   ██║  ██║██║   ██║██║ █╗ ██║██╔██╗ ██║
            ██╔═══╝ ╚██╗ ██╔╝    ╚════██║██║██║   ██║██║╚██╗██║██╔══██║██║         ╚════██║██╔══██║██║   ██║   ██║   ██║  ██║██║   ██║██║███╗██║██║╚██╗██║
            ██║      ╚████╔╝     ███████║██║╚██████╔╝██║ ╚████║██║  ██║███████╗    ███████║██║  ██║╚██████╔╝   ██║   ██████╔╝╚██████╔╝╚███╔███╔╝██║ ╚████║
            ╚═╝       ╚═══╝      ╚══════╝╚═╝ ╚═════╝ ╚═╝  ╚═══╝╚═╝  ╚═╝╚══════╝    ╚══════╝╚═╝  ╚═╝ ╚═════╝    ╚═╝   ╚═════╝  ╚═════╝  ╚══╝╚══╝ ╚═╝  ╚═══╝
            """
        log.info(shutdown)
        def selectedDataSource = "dataSource"

        log.info "Database home is : ${Holders.getConfig().dataSources."$selectedDataSource".url}"

    }

    //Method to create 'All Users' Group by default
    void createAllUsersGroup() {
        Group existingGroup = Group.findByName('All Users')
        if (!existingGroup) {
            new Group(name: 'All Users', description: 'Default User group created by the system. This user group is automatically assigned to all users. When Sharing signals feature is enabled then by default All Users group will be associated to signals.', isActive: true, createdBy: "System", modifiedBy: "System", groupType: 'USER_GROUP', dateCreated: new Date(), lastUpdated: new Date(),scimId: null).save(flush: true)
            Group newCreated = Group.findByName('All Users')
            User.all.collect {
                it.addToGroups(newCreated)
                it.save(flush: true)
            }
        }
    }

    private saveRorStatus() {
        String rorStatement = "select JSON_VALUE(CONFIG_VALUE,'\$.KEY_VALUE') as KEY_VALUE from VW_ADMIN_APP_CONFIG where CONFIG_KEY='ROR_INTERVAL' and APPLICATION_NAME = 'PVA-DB'"
        String rorColumn = Holders.config.signal.ror.flag.column.name
        boolean isRor = true
        Sql sqlObj = null
        try {
            sqlObj = new Sql(dataSource_pva)
            sqlObj.eachRow(rorStatement) { GroovyResultSet resultSet ->
                String rorColumnFlagVal = resultSet.getString(rorColumn)
                if (rorColumnFlagVal == '1') {
                    isRor = false
                }
            }
        } catch (Exception e) {
            log.error(e.printStackTrace())
        } finally {
            sqlObj?.close()
        }
        cacheService.saveRorCache(isRor)
    }

    private saveSocAbbreviation() {
        String rorStatement = "SELECT pm.soc_abbrev, pm.soc_name FROM pvr_md_soc pm WHERE pm.meddra_dict_id = ( SELECT " +
                "vmtm.meddra_dict_id FROM vw_meddra_tenant_mapping vmtm WHERE vmtm.tenant_id = pkg_pvs_app_util.f_get_active_tenant_mt) and meddra_language_id = 1"

        Sql sqlObj = null
        Map socAbbreviationMap = [:]
        try {
            sqlObj = new Sql(dataSource_pva)

            sqlObj.eachRow(rorStatement) { GroovyResultSet resultSet ->
                socAbbreviationMap.put(resultSet.getString('SOC_ABBREV'), resultSet.getString('SOC_NAME'))
            }


        } catch (Exception e) {
            log.error(e.printStackTrace())
        } finally {
            sqlObj?.close()
        }
        dataObjectService.saveSocAbbreviationCache(socAbbreviationMap)
    }

    void initializeSystemConfig() {
        List<SystemConfig> systemConfigList = SystemConfig.findAll()
        SystemConfig systemConfigObj
        if (!systemConfigList.size()) {
            systemConfigObj = new SystemConfig()
            systemConfigObj.enableSignalWorkflow = false
        }else{
            systemConfigObj = SystemConfig.first()
        }
        if(!systemConfigObj.firstTime){
            systemConfigObj.displayDueIn = Holders.config.validatedSignal.show.dueIn
            systemConfigObj.enableEndOfMilestone = false
            def x = Holders.config.validatedSignal.end.of.review ? JSON.parse(Holders.config.validatedSignal.end.of.review) : [Constants.DATE_CLOSED]
            def str = ""
            x.eachWithIndex{def entry, int i ->
                if(i>0){
                    str+=","
                }
                str+=entry
            }
            systemConfigObj.selectedEndPoints = str ?: "Date Closed"
            systemConfigObj.firstTime=true
        }
        systemConfigObj.save(flush: true)
    }

    void syncGloablCategoriesAndAggAlerts() {
        if (Holders.config.signal.categories.migration.enabled == true) {
            pvsGlobalTagService.runCategoriesMigrationInDB()
            pvsGlobalTagService.importAggregateGlobalTagsWithKeyId()
            pvsGlobalTagService.importSingleGlobalTagsWithKeyId()
        }
    }


    void enableEtlPvrConnectivity(){
        if (!PvsAppConfiguration.findByKey(Constants.ENABLE_ALERT_EXECUTION)){
            PvsAppConfiguration pvsAppConfiguration = new PvsAppConfiguration()
            pvsAppConfiguration.key = Constants.ENABLE_ALERT_EXECUTION
            pvsAppConfiguration.skipAudit=true
            pvsAppConfiguration.booleanValue = true
            CRUDService.saveWithAuditLog(pvsAppConfiguration)
        }
    }
    void setPvsAppConfigurationForSignalCharts() {
        if (!PvsAppConfiguration.findByKey(Constants.ENABLE_SIGNAL_CHARTS)) {
            PvsAppConfiguration pvsAppConfigurationObj = new PvsAppConfiguration()
            pvsAppConfigurationObj.booleanValue = false
            pvsAppConfigurationObj.key = Constants.ENABLE_SIGNAL_CHARTS
            CRUDService.saveWithAuditLog(pvsAppConfigurationObj)
        }
    }
    void setPvsAppConfigurationForProductGroupUpdate() {
        if (!PvsAppConfiguration.findByKey(Constants.PRODUCT_GROUP_UPDATE)) {
            PvsAppConfiguration pvsAppConfigurationObj = new PvsAppConfiguration()
            pvsAppConfigurationObj.booleanValue = true
            pvsAppConfigurationObj.key = Constants.PRODUCT_GROUP_UPDATE
            CRUDService.saveWithAuditLog(pvsAppConfigurationObj)
        }
    }

    void reOrderFilters() {
        if (Holders.config.pvsignal.csrfProtection.enabled) {
            SpringSecurityUtils.clientRegisterFilter('csrfFilter', SecurityFilterPosition.LAST.order + 10)
        }
        if (!Holders.config.singleUserSession.enabled) {
            SpringSecurityUtils.clientRegisterFilter('concurrentSessionFilter', SecurityFilterPosition.CONCURRENT_SESSION_FILTER)
        }
    }

    void initializeAlertPreChecks() {
        try {
            if (!AlertPreExecutionCheck.first()) {
                AlertPreExecutionCheck alertPreExecutionCheck = new AlertPreExecutionCheck(true, true, true, false, true, Constants.SYSTEM_USER, Constants.SYSTEM_USER)
                alertPreExecutionCheck.save(flush: true)
            }
        } catch (Exception ex) {
            log.error("Error occurred while initializing alert pre-execution checks.", ex.printStackTrace())
        }
    }

    void initializeSystemConfigurationPrecheck() {
        boolean precheckEnabled = Holders.config.system.precheck.configuration.enable
        try {
            if (precheckEnabled) {
                List<SystemPreConfig> systemPreConfigList = new ArrayList<SystemPreConfig>()
                try {
                    //Checking precheck status at mart end for SAFETY
                    log.debug("Fetching system Pre-Checks from safety db...")
                    preCheckService.initializeMartPrechecks([dataSource: dataSource_pva, type: Constants.SystemPrecheck.SAFETY, bootType: true])
                } catch (Exception ex) {
                    log.error("Exception occurred while fetching system pre-check", ex)
                }
                try {
                    //Checking precheck status at mart end for FAERS
                    if (Holders.config.signal.faers.enabled) {
                        log.debug("Fetching system Pre-Checks from faers db...")
                        preCheckService.initializeMartPrechecks([dataSource: dataSource_faers, type: Constants.SystemPrecheck.FAERS, bootType: true])
                    }
                } catch (Exception ex) {
                    log.error("Exception occurred while fetching system pre-check", ex)
                }
                try {
                    //Checking precheck status at mart end for VIGIBASE
                    if (Holders.config.signal.vigibase.enabled) {
                        log.debug("Fetching system Pre-Checks from vigibase db...")
                        preCheckService.initializeMartPrechecks([dataSource: dataSource_vigibase, type: Constants.SystemPrecheck.VIGIBASE, bootType: true])
                    }
                } catch (Exception ex) {
                    log.error("Exception occurred while fetching system pre-check", ex)
                }
                try {
                    //Checking precheck status at mart end for JADER
                    if (Holders.config.signal.jader.enabled) {
                        log.debug("Fetching system Pre-Checks from vigibase db...")
                        preCheckService.initializeMartPrechecks([dataSource: dataSource_jader, type: Constants.SystemPrecheck.JADER, bootType: true])
                    }
                } catch (Exception ex) {
                    log.error("Exception occurred while fetching system pre-check", ex)
                }
                try {
                    //Checking precheck status at mart end for VAERS
                    if (Holders.config.signal.vaers.enabled) {
                        log.debug("Fetching system Pre-Checks from vaers db...")
                        preCheckService.initializeMartPrechecks([dataSource: dataSource_vaers, type: Constants.SystemPrecheck.VAERS, bootType: true])
                    }
                } catch (Exception ex) {
                    log.error("Exception occurred while fetching system pre-check", ex)
                }
                try {
                    //Checking precheck status at mart end for EVDAS
                    if (Holders.config.signal.evdas.enabled) {
                        log.debug("Fetching system Pre-Checks from evdas db...")
                        preCheckService.initializeMartPrechecks([dataSource: dataSource_eudra, type: Constants.SystemPrecheck.EVDAS, bootType: true])
                    }
                } catch (Exception ex) {
                    log.error("Exception occurred while fetching system pre-check", ex)
                }
            }
        } catch (Exception ex) {
            log.error("Exception occurred while fetching system pre-check", ex)

        }
    }

    void initiateProductTypeConfiguration(){
        // this should be added only when product_type_configuration is empty
        // this will happen in case of PVD data corruption

        if(ProductTypeConfiguration.count()==0) {
            def startup = "Creating Product Type Configurations Using Mart Values"
            log.info(startup)
            Sql sql, sql_pva
            sql_pva = new Sql(dataSource_pva)
            sql = new Sql(dataSource)
            // default product and role combinations to create
            List productList = ['DRUG', 'DEVICE', 'VACCINE', 'BIOLOGIC']
            List roleList = ['Suspect', 'Concomitant']
            List<Map> productTypeMap = []
            Map suspectMap = [:]
            Map concomitantMap = [:]
            try {
                sql_pva.eachRow("select * from vw_product_type where lang_id = '1'", []) { row ->
                    Map rowData = ['id': row.product_type_id, 'text': row.product_type]
                    if (productList.contains(rowData.text.toUpperCase())) {
                        productTypeMap << rowData
                    }
                }
                sql_pva.eachRow("select * from VW_CLP_DRUG_TYPE_DSP where therapy_type is not null and lang_id ='1'", []) { row ->
                    Map rowData = ['id': row.id, 'text': row.therapy_type]
                    if (rowData.text == 'Suspect') {
                        suspectMap = rowData
                    }
                    if (rowData.text == 'Concomitant') {
                        concomitantMap = rowData
                    }
                }
                // saving default suspect product role configuration
                productTypeMap.each {
                    ProductTypeConfiguration productTypeConfiguration = new ProductTypeConfiguration()
                    productTypeConfiguration.name = it.text + "-S"
                    productTypeConfiguration.productType = it.text
                    productTypeConfiguration.productTypeId = it.id as Long
                    productTypeConfiguration.roleType = suspectMap.text
                    productTypeConfiguration.roleTypeId = suspectMap.id as Long
                    productTypeConfiguration.dateCreated = new Date()
                    productTypeConfiguration.lastUpdated = new Date()
                    if (it?.text?.toUpperCase() == 'DRUG') {
                        productTypeConfiguration.isDefault = true
                    }
                    productTypeConfiguration.save(failOnError: true, flush: true)
                }
                // saving default suspect, concomitant product role configuration
                if (concomitantMap != [:]) {
                    productTypeMap.each {
                        //suspect
                        ProductTypeConfiguration productTypeConfiguration = new ProductTypeConfiguration()
                        productTypeConfiguration.name = it.text + "-S+C"
                        productTypeConfiguration.productType = it.text
                        productTypeConfiguration.productTypeId = it.id as Long
                        productTypeConfiguration.roleType = suspectMap.text
                        productTypeConfiguration.roleTypeId = suspectMap.id as Long
                        productTypeConfiguration.dateCreated = new Date()
                        productTypeConfiguration.lastUpdated = new Date()
                        productTypeConfiguration.save(failOnError: true, flush: true)
                        //concomitant
                        ProductTypeConfiguration productTypeConfiguration1 = new ProductTypeConfiguration()
                        productTypeConfiguration1.name = it.text + "-S+C"
                        productTypeConfiguration1.productType = it.text
                        productTypeConfiguration1.productTypeId = it.id as Long
                        productTypeConfiguration1.roleType = concomitantMap.text
                        productTypeConfiguration1.roleTypeId = concomitantMap.id as Long
                        productTypeConfiguration1.dateCreated = new Date()
                        productTypeConfiguration1.lastUpdated = new Date()
                        productTypeConfiguration1.save(failOnError: true, flush: true)
                    }
                }

            } catch (Exception e) {
                println("########## Some error occurred while populating default values in ProductTypeConfiguration #############")
                e.printStackTrace()
            } finally {
                sql?.close()
                sql_pva?.close()
            }
            // Below configurations will only run after Prosuct type Config has values
            if(ProductTypeConfiguration.count()!=0) {
                /*
                    Migration for configurations
                */
                startup = "Migration for Product Type Configuration Started"
                log.info(startup)
                Map dsProductRoleMap = [
                        'pva'     : [
                                'DEFAULT'                : ProductTypeConfiguration.findByIsDefault(true).id,
                                'SUSPECT'                : ProductTypeConfiguration.findByIsDefault(true).id,
                                'SUSPECT_AND_CONCOMITANT': ProductTypeConfiguration.findByNameIlike('Drug-S+C').id,
                                'VACCINE'                : ProductTypeConfiguration.findByProductTypeIlikeAndRoleTypeIlike('Vaccine', 'Suspect').id
                        ],
                        'vaers'   : [
                                'DEFAULT': 'VACCINE_SUSPECT_VAERS',
                                'VACCINE': 'VACCINE_SUSPECT_VAERS'
                        ],
                        'faers'   : [
                                'DEFAULT'                : 'DRUG_SUSPECT_FAERS',
                                'SUSPECT'                : 'DRUG_SUSPECT_FAERS',
                                'SUSPECT_AND_CONCOMITANT': 'DRUG_SUSPECT_CONCOMITANT_FAERS'
                        ],
                        'vigibase': [
                                'DEFAULT'                : 'DRUG_SUSPECT_VIGIBASE',
                                'SUSPECT'                : 'DRUG_SUSPECT_VIGIBASE',
                                'SUSPECT_AND_CONCOMITANT': 'DRUG_SUSPECT_CONCOMITANT_VIGIBASE',
                                'VACCINE'                : 'VACCINE_SUSPECT_VIGIBASE'
                        ]
                ]
                def configList = Configuration.list().findAll {
                    (it.type == 'Aggregate Case Alert')
                }
                List<Map> configurationProductTypeList = []
                String productType = ""
                configList.each { configuration ->
                    productType = ""
                    configuration.selectedDatasource.split(',').each { dataSource ->
                        if (dataSource != 'eudra') {
                            productType = productType + (dsProductRoleMap[dataSource][configuration.drugType] ?: dsProductRoleMap[dataSource]['DEFAULT']) + ","
                        }
                    }
                    if (productType.length() > 0) {
                        productType = (productType.substring(0, productType.length() - 1))
                    }
                    configurationProductTypeList.add(id: configuration.id, drugType: productType)
                }
                try {
                    sql = new Sql(dataSource)
                    sql.withBatch(100, "UPDATE RCONFIG SET DRUG_TYPE = :drugType WHERE ID = :id", { preparedStatement ->
                        configurationProductTypeList.each {
                            preparedStatement.addBatch(id: it.id, drugType: it.drugType)
                        }
                    })
                } catch (Exception e) {
                    println("########## Some error occurred in Migrating ProductTypeConfiguration for Configuration #############")
                    e.printStackTrace()
                } finally {
                    sql?.close()
                }
                /*
                    Migration for executed configurations
                */
                startup = "Migration for Product Type Configuration Started for Executed Configurations"
                log.info(startup)
                dsProductRoleMap = [
                        'pva'     : [
                                'DEFAULT'                : 'Drug-S',
                                'SUSPECT'                : 'Drug-S',
                                'SUSPECT_AND_CONCOMITANT': 'Drug-S+C',
                                'VACCINE'                : 'Vaccine-S'
                        ],
                        'vaers'   : [
                                'DEFAULT'              : 'Vaccine(VA)-S',
                                'VACCINE'              : 'Vaccine(VA)-S',
                                'VACCINE_SUSPECT_VAERS': 'Vaccine(VA)-S'
                        ],
                        'faers'   : [
                                'DEFAULT'                       : 'Drug(F)-S',
                                'SUSPECT'                       : 'Drug(F)-S',
                                'SUSPECT_AND_CONCOMITANT'       : 'Drug(F)-S+C',
                                'DRUG_SUSPECT_FAERS'            : 'Drug(F)-S',
                                'DRUG_SUSPECT_CONCOMITANT_FAERS': 'Drug(F)-S+C'
                        ],
                        'vigibase': [
                                'DEFAULT'                          : 'Drug(VB)-S',
                                'SUSPECT'                          : 'Drug(VB)-S',
                                'SUSPECT_AND_CONCOMITANT'          : 'Drug(VB)-S+C',
                                'VACCINE'                          : 'Vaccine(VB)-S',
                                'DRUG_SUSPECT_VIGIBASE'            : 'Drug(VB)-S',
                                'DRUG_SUSPECT_CONCOMITANT_VIGIBASE': 'Drug(VB)-S+C',
                                'VACCINE_SUSPECT_VIGIBASE'         : 'Vaccine(VB)-S'
                        ]
                ]

                List<Map> executedProductNameList = []
                sql = new Sql(dataSource)
                sql.eachRow("select id,name from product_type_configuration") { row ->
                    dsProductRoleMap['pva'][row[0] as String] = row[1]
                }
                String productName = ""
                sql.eachRow("select id,drug_type, selected_data_source from ex_rconfig where  drug_type is not null") { row ->
                    productName = ""
                    row[2].split(',').each { dataSource ->
                        if (dataSource == 'pva') {
                            row[1].split(',').each { pt ->
                                if (dsProductRoleMap[dataSource][pt]) {
                                    productName = productName + dsProductRoleMap[dataSource][pt] + ","
                                }
                            }
                        } else if (dataSource != 'eudra') {
                            row[1].split(',').each { pt ->
                                if (dsProductRoleMap[dataSource][pt]) {
                                    productName = productName + (dsProductRoleMap[dataSource][pt]) + ","
                                }
                            }
                        }
                    }
                    if (productName.length() > 0) {
                        productName = (productName.substring(0, productName.length() - 1))
                    }
                    executedProductNameList.add(id: row[0], drugTypeName: productName)
                }

                try {
                    sql.withBatch(100, "UPDATE EX_RCONFIG SET DRUG_TYPE_NAME = :drugTypeName WHERE ID = :id", { preparedStatement ->
                        executedProductNameList.each {
                            preparedStatement.addBatch(id: it.id, drugTypeName: it.drugTypeName)
                        }
                    })
                } catch (Exception e) {
                    println("########## Some error occurred in Migrating ProductTypeConfiguration for Executed Configuration #############")
                    e.printStackTrace()
                } finally {
                    sql?.close()
                }
            }
        }
    }

    void updateUserPreferenceDashboardJson() {
        try {
            Map defaultWidgetConfig = [:]
            List<String> namesToRemove = [] //this isrequired for disbling widfet in PVS-57996
            defaultWidgetConfig = Holders.config.signal.dashboard.widgets.config.clone()
            JsonSlurper slurper = new JsonSlurper()
            User.findAllByEnabled(true).each { User it ->
                String userdashboardConfigJson = it.preference?.dashboardConfig
                Map unionMap = [:]
                Map dashboardWidgetConfig = userdashboardConfigJson!=null ? slurper.parseText(userdashboardConfigJson) : [:]
                unionMap.putAll(dashboardWidgetConfig)
                defaultWidgetConfig.each { k, v ->
                    if (!unionMap.containsKey(k)) {
                        unionMap[k] = v
                        //Adding the keys that are missed in user dashboardconfig may be due to roles config or new widgets addition
                    }
                }
                namesToRemove.each { name ->
                    unionMap.remove(name) //removed the disabled widgets
                }
                if ((unionMap.keySet() - dashboardWidgetConfig.keySet()).size() != 0 || (dashboardWidgetConfig.keySet() - unionMap.keySet()).size() != 0)  {
                    it.preference.dashboardConfig = new JsonBuilder(unionMap).toPrettyString()
                    it.preference.save(flush: true)
                }
            }
        } catch (Exception ex) {
            log.error("Exception occurred while updating user preference dashboard JSON", ex)
        }
    }


    def createViewInstances() {
        if (ViewInstance.count() <= 0) {
            grailsApplication.config.configurations.viewInstances.each { Map viDetailMap ->
                Map columnOrderMap = viewInstanceService.addOrUpdateColumnMap(viDetailMap)
                ViewInstance vi = new ViewInstance(name: viDetailMap.name, alertType: viDetailMap.alertType, filters: viDetailMap.filters, columnSeq: JsonOutput.toJson(columnOrderMap), sorting: viDetailMap.sorting, defaultValue: viDetailMap.defaultValue)
                vi.save(failOnError: true)
            }
            Map pvaMiningVariables = cacheService.getMiningVariables(Constants.DataSource.PVA)
            Map faersMiningVariale = cacheService.getMiningVariables(Constants.DataSource.FAERS)
            pvaMiningVariables.each { keyId, miningVariable ->
                try {
                    grailsApplication.config.configurations.viewInstance.adhoc.dataMining.pva.each { Map viDetailMap ->
                        Map columnOrderMap = viewInstanceService.addOrUpdateColumnMap(viDetailMap)
                        ViewInstance vi = new ViewInstance(name: viDetailMap.name, alertType: viDetailMap.alertType, filters: viDetailMap.filters, columnSeq: JsonOutput.toJson(columnOrderMap), sorting: viDetailMap.sorting, defaultValue: viDetailMap.defaultValue)
                        if (viDetailMap.isDataMining) {
                            vi.sorting = '{"3":"asc"}'
                            vi.alertType = vi.alertType + "-" + miningVariable?.label
                            vi.keyId = keyId as Long
                        }
                        vi.save(failOnError: true)
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace()
                }

            }
            faersMiningVariale.each { keyId, miningVariable ->
                try {
                    grailsApplication.config.configurations.viewInstance.adhoc.dataMining.faers.each { Map viDetailMap ->
                        Map columnOrderMap = viewInstanceService.addOrUpdateColumnMap(viDetailMap)
                        ViewInstance vi = new ViewInstance(name: viDetailMap.name, alertType: viDetailMap.alertType, filters: viDetailMap.filters, columnSeq: JsonOutput.toJson(columnOrderMap), sorting: viDetailMap.sorting, defaultValue: viDetailMap.defaultValue)
                        if (viDetailMap.isDataMining) {
                            vi.sorting = '{"3":"asc"}'
                            vi.alertType = vi.alertType + "-" + miningVariable?.label
                            vi.keyId = keyId as Long
                        }
                        vi.save(failOnError: true)
                    }
                }
                catch (Exception ex) {
                    println("######## Some error occure while updating the View Instances  ##########")
                    ex.printStackTrace()
                }

            }
        } else {
                viewInstanceService.updateAllViewInstances()
                viewInstanceService.updateICRandEvdasViews()
        }
    }

    void setHostnameForMultiNodeSensitiveJobs() {
        try {
            PvsAppConfiguration pvsAppConfigurationServerHostname = PvsAppConfiguration.findOrCreateByKey('serverHostname')
            String serverHostname = "hostname".execute()?.text?.trim()
            pvsAppConfigurationServerHostname.stringValue = serverHostname
            pvsAppConfigurationServerHostname.skipAudit=true
            pvsAppConfigurationServerHostname.save(flush:true)
        } catch (Exception ex) {
            log.error("Error occurred while creating or updating config for Hostname", ex)
        }
    }


    void updateEvdasFileStatus(){
        try{
            List <EvdasFileProcessLog> evFileLogListToUpdate = EvdasFileProcessLog.findAllByStatus(EvdasFileProcessState.IN_PROCESS)
            evFileLogListToUpdate.each{
                it.status = EvdasFileProcessState.FAILED
                it.save(flush:true)
            }
            log.debug("Evdas File Process Log Status updated to Error for In Process files due to restart. ${evFileLogListToUpdate.size()} Logs updated.")
        } catch (Exception ex){
            ex.printStackTrace()
        }

    }

    void updateReportHistory() {
        try {
            List<ReportHistory> reportHistories = ReportHistory.findAllByIsReportGenerated(false)
            reportHistories.each {
                it.isReportGenerated = null
                it.save(flush: true)
            }
        } catch (Exception ex) {
            log.error("Report history updation failed.", ex)
            ex.printStackTrace()
        }
    }

    void setAutoAlertJobConfigurationsInWebAppSchema() {
        try {
            PvsAppConfiguration pvsAppConfiguration = PvsAppConfiguration.findOrCreateByKey('autoAlertJobRunning')
            pvsAppConfiguration.booleanValue = false
            pvsAppConfiguration.skipAudit = true
            pvsAppConfiguration.save(flush: true)
        } catch (Exception ex) {
            log.error("Error occurred while updating config for Auto Alert job.", ex.printStackTrace())
        }
    }
    void disableApprovalRequiredInWorkFlow(){
        Sql sql
        try{
            sql = new Sql(dataSource)
            sql.execute("UPDATE DISPOSITION_RULES SET APPROVAL_REQUIRED =false")
        }catch(Exception ex){
            log.error("Error Occured while disabling approval required functionality.", ex)
        } finally {
            sql?.close()
        }
    }

    /**
     * update information of user group field mapping by fetching
     * information from PVR API
     */
    void updateUserGroupFieldMapping(){
        try{
            List groupList = Group.createCriteria().list {
                projections {
                    ('name')
                }
                'eq'('groupType',GroupType.USER_GROUP)
            }
            String url = Holders.config.pvreports.url
            String fieldAccessURI = Constants.PVRApiPath.UPDATE_BLINDED_DATA
            Map requestMap = [usergroupnames : groupList.join(',')]
            Map res = reportIntegrationService.get(url, fieldAccessURI, requestMap)
            if(res.data?.status){
                // save group field mapping
                List<BlindedUserGroupsDTO> userGroupInfo = res.data.data
                List blindedGroups = []
                List blindedUsers = []
                userGroupInfo.each {
                    Group group = Group.findByName(it['userGroupName'])
                    if(group){
                        if (it['blindedFieldIds']) {
                            blindedGroups.add(group.id)
                            blindedUsers.addAll(userGroupService.fetchUserListIdForGroup(group))
                        }
                        UserGroupFieldMapping obj = UserGroupFieldMapping.findByGroupId(group.id as Long)
                        if(it['isDeleted']) {
                            obj?.delete(flush: true)
                        } else if(it['fieldIds']) {
                            obj = obj ?: new UserGroupFieldMapping()
                            obj.groupId = group.id as Long
                            obj.availableFields = it['fieldIds'].join(',')
                            obj.blindedFields = it['blindedFieldIds']?.join(',')?: null
                            obj.redactedFields = it['protectedFieldIds']?.join(',')?: null

                            obj.save(flush:true)
                        } else if(it['fieldIds'] == []) {
                            obj = obj?: new UserGroupFieldMapping()
                            obj.groupId = group.id as Long
                            obj.blindedFields = null
                            obj.redactedFields = null
                            obj.availableFields = null

                            obj.save(flush:true)
                        }
                    }
                }
                log.info("User Group Mapping table updated")
                cacheService.prepareUserGroupBlindingCache(blindedGroups)
                cacheService.prepareUserBlindingCache(blindedUsers.unique())
            }
        } catch(Exception ex){
            throw ex
            log.error(ex.printStackTrace())
        }

    }

    void setValueForPVAMigration() {
        try {
            int numOfAssignmentToBeMigrated = ProductViewAssignment.createCriteria().count {
                isNull("alertGroup")
            }
            Holders.config.product.view.assignment.migration.required = numOfAssignmentToBeMigrated ? true : false
        } catch(Exception ex){
            ex.printStackTrace()
        }
    }

    void updateForceAuthNForSSO() {
        try {
            if (grailsApplication.config.grails.plugin.springsecurity.saml.active) {
                grailsApplication.mainContext.webProfileOptions.forceAuthn = grailsApplication.config.grails.plugin.springsecurity.saml.forceAuthN
                grailsApplication.mainContext.samlEntryPoint.defaultOptions.forceAuthn = grailsApplication.config.grails.plugin.springsecurity.saml.forceAuthN
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        }
    }
    void populateAlertGroup() {
        try {
            grailsApplication.config.product.assignment.alert.group.each{ entry ->
                String alertGroupName = entry.name
                String grouping = entry.grouping
                AlertGroup alertGroup = AlertGroup.findByNameIlike(alertGroupName)
                if (!alertGroup) {
                    alertGroup = new AlertGroup(name: alertGroupName, grouping: grouping.toBoolean())
                    alertGroup.save(flush: true)
                }
            }
        } catch (Exception ex){
            ex.printStackTrace()
        }
    }
}
