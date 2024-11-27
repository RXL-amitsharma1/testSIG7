package com.rxlogix

import com.rxlogix.audit.AuditTrail
import com.rxlogix.cache.CacheService
import com.rxlogix.cache.HazelcastService
import com.rxlogix.config.*
import com.rxlogix.customException.ExecutionStatusException
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.EvdasFileProcessState
import com.rxlogix.helper.LinkHelper
import com.rxlogix.mapping.ERMRConfig
import com.rxlogix.signal.SubstanceFrequency
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.RelativeDateConverter
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonSlurper
import groovy.sql.Sql
import oracle.jdbc.driver.OracleTypes
import org.apache.http.util.TextUtils
import org.hibernate.engine.jdbc.internal.BasicFormatterImpl
import org.joda.time.DateTime
import org.springframework.context.MessageSource
import org.springframework.transaction.annotation.Propagation

import java.sql.Clob
import java.sql.ResultSet
import java.text.SimpleDateFormat

class EvdasAlertExecutionService implements LinkHelper, AlertUtil {
    static final String RUNNING_EVDAS_CONFIG_CFG = 'running_evdas_signal_cfg'

    static transactional = false
    ConfigurationService configurationService
    CRUDService CRUDService
    MessageSource messageSource
    EvdasAlertService evdasAlertService
    EvdasOnDemandAlertService evdasOnDemandAlertService
    EvdasSqlGenerationService evdasSqlGenerationService
    def signalDataSourceService
    UserGroupService userGroupService
    CacheService cacheService
    ReportExecutorService reportExecutorService
    EmailService emailService
    def notificationHelper
    EmailNotificationService emailNotificationService
    def dataSource_eudra
    def alertService
    def grailsApplication
    def dataSource
    HazelcastService hazelcastService
    def alertAdministrationService

    def currentlyRunning = []
    SimpleDateFormat dateWriteFormat = new SimpleDateFormat('dd-MMM-yyyy')

    def detailUrlMap = ["EVDAS_ALERT": [adhocRun: "evdas_adhoc_reportRedirectURL", dataMiningRun: "evdas_reportRedirectURL"]]
    def signalAuditLogService
    def dataSource_pva

    def runConfigurations() throws Exception {

        EvdasConfiguration scheduledConfiguration = EvdasConfiguration.getNextConfigurationToExecute(currentlyRunning)

        if (scheduledConfiguration) {
            alertAdministrationService.initializePreChecks()
            Map<String, Object> resultMap = alertAdministrationService.isConfigurationReadyForExecution(scheduledConfiguration)
            Boolean isReadyForExecution = resultMap.get(Constants.AlertUtils.IS_READY_FOR_EXECUTION)
            if (isReadyForExecution) {
                scheduledConfiguration.executing = true
            CRUDService.updateWithoutAuditLog(scheduledConfiguration)
            ExecutionStatus executionStatus = null
            if (scheduledConfiguration.isResume) {
                Long exConfigId = ExecutedEvdasConfiguration.createCriteria().get {
                    eq('configId' , scheduledConfiguration?.id)
                    projections { max "id" }
                } as Long
                executionStatus = ExecutionStatus.findByConfigIdAndExecutedConfigIdAndType(scheduledConfiguration.id,
                        exConfigId, Constants.AlertConfigType.EVDAS_ALERT)

                executionStatus.executionStatus = ReportExecutionStatus.GENERATING
                CRUDService.updateWithoutAuditLog(executionStatus)
            } else {
                executionStatus = createExecutionStatus(scheduledConfiguration)
            }
            log.info("Found ${scheduledConfiguration.name} (${scheduledConfiguration.id}) to execute.")
            try {
                startEvdasAlertExecutionByLevel(executionStatus)
            } catch (Throwable e) {
                handleFailedExecution(scheduledConfiguration.id, executionStatus.executedConfigId, e)

            } finally {
                currentlyRunning.remove(scheduledConfiguration.id)
            }
            //executeAlertJob(scheduledConfiguration)
            log.info("Execution is Done")
            } else {
                alertAdministrationService.autoPauseConfigurationAndTriggerNotification(scheduledConfiguration, Constants.AlertConfigType.EVDAS_ALERT, resultMap.get(Constants.AlertUtils.ALERT_DISABLE_REASON))
            }
        }
    }

    int getExecutionQueueSize() {
        return currentlyRunning.size()
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    def handleFailedExecution(Long configId, Long executedConfigId, ExecutionStatusException ese) {
        try {
            EvdasConfiguration config = EvdasConfiguration.get(configId)
            ExecutedEvdasConfiguration executedConfiguration = ExecutedEvdasConfiguration.get(executedConfigId)
            ExecutionStatus executionStatus =
                    ExecutionStatus.findByConfigIdAndReportVersionAndType(configId,
                            config.isResume ? config.numOfExecutions : config.numOfExecutions + 1, Constants.AlertConfigType.EVDAS_ALERT)
            if (executionStatus) {
                executionStatus.stackTrace = ese?.errorCause?.length() > 32000 ? ese?.errorCause?.substring(0,32000) : ese?.errorCause
                executionStatus.executionStatus = ReportExecutionStatus.ERROR
                executionStatus.save(flush:true)

                setNextRunDateForConfiguration(config)

                //Adjust the date range frequency for the custom dates.
                if (!config.adhocRun) {
                    adjustCustomDateRanges(config)
                }

                if (config.adhocRun) {
                    config.isEnabled = false
                    config.setNextRunDate(null)
                } else {
                    config.isEnabled = true
                }

                config.isResume = false
                config.executing = false
                CRUDService.updateWithoutAuditLog(config)

                if (executedConfiguration) {
                    executedConfiguration.isEnabled = false
                    executedConfiguration.save()
                }

                //After that set the notification.
                addNotification(executedConfiguration, config, executionStatus, config.assignedTo, config.assignedToGroup)
                emailNotificationService.emailHanlderAtAlertLevel(executedConfiguration, executionStatus)
            } else {
                log.error("Cannot find the execution status. [handleFailedExecution]")
            }
        } catch (Throwable th) {
            log.error("Error happened when handling failed Configurations [${executedConfigId}]", th)
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    def setValuesForConfiguration(Long configId, Long execConfigId) {
        EvdasConfiguration lockedConfiguration
        ExecutedEvdasConfiguration executedConfiguration
        ExecutionStatus executionStatus
        try {
            lockedConfiguration = EvdasConfiguration.lock(configId)
            executedConfiguration = ExecutedEvdasConfiguration.get(execConfigId)
            executionStatus = ExecutionStatus.findByConfigIdAndExecutedConfigIdAndType(lockedConfiguration.id,execConfigId,
                    Constants.AlertConfigType.EVDAS_ALERT)
            if (executionStatus) {
                executionStatus.endTime = System.currentTimeMillis()
                setTotalExecutionTimeForConfiguration(lockedConfiguration, (executionStatus.endTime - executionStatus.startTime))
                setNextRunDateForConfiguration(lockedConfiguration)
                lockedConfiguration.executing = false

                //Set the status completed if the status is delivering.
                if (executionStatus?.executionStatus != ReportExecutionStatus.ERROR) {
                    executionStatus?.executionStatus = ReportExecutionStatus.COMPLETED
                    lockedConfiguration.isResume = false
                }
                //Adjust the date range frequency for the custom dates.
                if (!lockedConfiguration.adhocRun) {
                    adjustCustomDateRanges(lockedConfiguration)
                }

                if (lockedConfiguration.futureScheduleDateJSON) {
                    lockedConfiguration.scheduleDateJSON = lockedConfiguration.futureScheduleDateJSON
                    lockedConfiguration.setNextRunDate(configurationService.getNextDate(lockedConfiguration))
                    lockedConfiguration.setFutureScheduleDateJSON(null)
                }

                if (executedConfiguration) {
                    //After that set the notification.
                    executedConfiguration.isEnabled = true
                    addNotification(executedConfiguration, lockedConfiguration, executionStatus, executedConfiguration.assignedTo, executedConfiguration.assignedToGroup)
                    emailNotificationService.emailHanlderAtAlertLevel(executedConfiguration, executionStatus)
                }
            }
        } catch (Throwable th) {
            log.error("Error happened when handling Successful Configurations [${lockedConfiguration.id}]", th)
        } finally {
            if (lockedConfiguration) {
                CRUDService.updateWithAuditLog(lockedConfiguration)
            }
            if (executionStatus) {
                executionStatus.save(flush:true)
            }
            if (executedConfiguration) {
                executedConfiguration.save()
            }
        }
    }

    LinkedHashMap debugReportSQL(EvdasConfiguration configuration, ExecutedEvdasConfiguration executedConfiguration=null) throws Exception {
        final Sql sql = new Sql(signalDataSourceService.getReportConnection(Constants.DataSource.EUDRA))
        LinkedHashMap sqlMap = [:]
        try {
            def formatter = new BasicFormatterImpl()
            def list = []
            String gttInserts = evdasSqlGenerationService.initializeInsertGtts(configuration)
            String insertStatementListed =''
            if(executedConfiguration.isDatasheetChecked && executedConfiguration?.selectedDataSheet && !TextUtils.isEmpty(executedConfiguration?.selectedDataSheet)){
                insertStatementListed =  "Begin execute immediate('delete from gtt_pvs_ds_input_data');"
                Map dataSheetsMap = JSON.parse(executedConfiguration.selectedDataSheet)
                def dsId = null
                def baseId = null
                dataSheetsMap?.each { key, value ->
                    dsId = key?.toString()?.split('_')[0]
                    baseId = key?.toString()?.split('_')[1]
                    insertStatementListed += "INSERT INTO GTT_PVS_DS_INPUT_DATA (Base_Id,Datasheet_Id, Datasheet_Name, Execution_Id) VALUES ('${baseId}','${dsId}','${value}', '${executedConfiguration?.id}');"+"\n"
                }
                insertStatementListed += " END;"+"\n"
                String listednessProc = insertStatementListed  + """ 
                                              begin PKG_SAFETY_AGG_LISTEDNESS.p_mains(${executedConfiguration?.id}); end;"""
                sqlMap.listednessProc = formatter.format(listednessProc)
            }
            String queryInserts = evdasSqlGenerationService.initializeQuerySql(configuration)
            String evdasSql = evdasSqlGenerationService.getEvdasQuerySql()
            sqlMap = [gttInserts: formatter.format(gttInserts), queryInserts: queryInserts, evdasSql: formatter.format(evdasSql), listednessProc: sqlMap.listednessProc]
        } catch (Exception e) {
            log.error(e.printStackTrace())
        } finally {
            sql.close()
        }
        return sqlMap
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    private void addNotification(ExecutedEvdasConfiguration executedEvdasConfiguration, EvdasConfiguration configuration,
                                 ExecutionStatus executionStatus, User user, Group group = null) {

        List<User> notificationRecipients = group ? userGroupService.fetchUserListForGroup(group) : [user]

        try {
            def url = getDetailsUrlMap(configuration.adhocRun)
            NotificationLevel status
            String message
            String messageArgs = "$configuration.name"
            if (executionStatus.executionStatus == ReportExecutionStatus.COMPLETED) {
                status = NotificationLevel.INFO
                message = "app.notification.completed"
            } else if (executionStatus.executionStatus == ReportExecutionStatus.WARN) {
                status = NotificationLevel.WARN
                message = "app.notification.needsReview"
            } else {
                status = NotificationLevel.ERROR
                message = "app.notification.failed"
                url = Constants.ERROR_URL
            }

            InboxLog inboxLog = null
            notificationRecipients.each { User notificationRecipient ->

                inboxLog = new InboxLog(notificationUserId: notificationRecipient.id, level: status, message: message,
                        messageArgs: messageArgs, type: "EVDAS Alert Execution", subject: messageSource.getMessage(message, [messageArgs].toArray(),
                        notificationRecipient.preference.locale), content: "", createdOn: new Date(), inboxUserId: notificationRecipient.id, isNotification: true,
                        executedConfigId: executedEvdasConfiguration?.id ?: 0, detailUrl: url)
                inboxLog.save(flush:true,failOnError: true)
                notificationHelper.pushNotification(inboxLog)
            }
            if(message in ["app.notification.completed","app.notification.failed"]) {
                reportExecutorService.createAuditForExecution(executedEvdasConfiguration, message, Constants.AlertConfigType.EVDAS_ALERT_CONFIGURATIONS)
            }
        }
        catch (Exception e) {
            log.info("""Error creating Notification: ${e.message}""")
        }
    }

    private getDetailsUrlMap(adhocRun) {
        def urlMap = detailUrlMap.get("EVDAS_ALERT")
        if (adhocRun) {
            urlMap.adhocRun
        } else {
            urlMap.dataMiningRun
        }
    }

    private setTotalExecutionTimeForConfiguration(configuration, long executionTime) throws Exception {
        configuration.totalExecutionTime = executionTime
        CRUDService.updateWithAuditLog(configuration)
    }

    private void setNextRunDateForConfiguration(EvdasConfiguration configuration) throws Exception {
        configuration.numOfExecutions = configuration.isResume ? (configuration.numOfExecutions?:1) : configuration.numOfExecutions + 1
        if (configuration.adhocRun) {
            configuration.setNextRunDate(null)
            configuration.isEnabled = false
        } else {
            configuration.setNextRunDate(configurationService.getNextDate(configuration))
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    ExecutionStatus createExecutionStatus(EvdasConfiguration scheduledConfiguration) {
        EvdasConfiguration lockedConfiguration = EvdasConfiguration.lock(scheduledConfiguration.id)
        ExecutionStatus executionStatus = new ExecutionStatus(configId: lockedConfiguration.id,
                reportVersion: lockedConfiguration.numOfExecutions + 1,
                startTime: System.currentTimeMillis(), nextRunDate: lockedConfiguration.nextRunDate,
                owner: lockedConfiguration.owner, name: lockedConfiguration.name,
                attachmentFormats: null, sharedWith: null, type: Constants.AlertConfigType.EVDAS_ALERT,
                nodeName: hazelcastService.getName())
        executionStatus.frequency = configurationService.calculateFrequency(lockedConfiguration)
        CRUDService.saveWithoutAuditLog(executionStatus)
        executionStatus
    }


    void startEvdasAlertExecutionByLevel(ExecutionStatus executionStatus) throws Exception {
        try {
            EvdasConfiguration lockedConfiguration = EvdasConfiguration.get(executionStatus.configId)
            ExecutedEvdasConfiguration executedEvdasConfiguration
            if (executionStatus.executedConfigId) {
                executedEvdasConfiguration = ExecutedEvdasConfiguration.get(executionStatus.executedConfigId)
            }
            switch (executionStatus.executionLevel) {
                case 0: executedEvdasConfiguration = createExecutedConfiguration(lockedConfiguration)
                    updateExecutionStatus(executionStatus, executedEvdasConfiguration)
                case 1: fetchEvdasAlertDataFromMart(lockedConfiguration, executedEvdasConfiguration, executionStatus.alertFilePath,executedEvdasConfiguration?.id)
                    updateExecutionStatusLevel(executionStatus)
                case 2: saveEvdasAlertData(lockedConfiguration, executedEvdasConfiguration, executionStatus.alertFilePath)
            }
        } catch (Throwable throwable) {
            log.error("Exception while running the evdas alert", throwable)
            ExecutionStatusException ese = new ExecutionStatusException(alertService.exceptionString(throwable))
            throw ese
        }
    }

    List fetchEvdasDataFromMart(EvdasConfiguration lockedConfiguration) throws Exception {
        def data = []
        Sql sql = null
        try {
            sql = new Sql(signalDataSourceService.getReportConnection(Constants.DataSource.EUDRA))

            String gttInserts = evdasSqlGenerationService.initializeInsertGtts(lockedConfiguration)
            log.info(gttInserts)
            if (gttInserts) {
                sql.execute(gttInserts)
                log.info("GTT Inserts executed.")
            }

            String queryInserts = evdasSqlGenerationService.initializeQuerySql(lockedConfiguration)
            log.info(queryInserts)
            if (queryInserts) {
                sql.execute(queryInserts)
                log.info("Query Inserts executed.")
            }
            List<ERMRConfig> eRMRConfigMapping
            def dataSourceValue = "pva"

            if (Holders.config.signal.evdas.enabled) {
                dataSourceValue = "eudra"
            }

            ERMRConfig."$dataSourceValue".withTransaction {
                eRMRConfigMapping = ERMRConfig.findAllByExcluded('N')
            }
            String evdasSql = evdasSqlGenerationService.getEvdasQuerySql()
            log.info(evdasSql)
            if (evdasSql) {
                sql.call(evdasSql, [lockedConfiguration.dateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE ? 1 : 0, Sql.resultSet(OracleTypes.CURSOR)]) { evdas_info ->
                    if (evdas_info) {
                        while (evdas_info.next()) {
                            try {
                                data << readEvdasData(evdas_info, eRMRConfigMapping)
                            } catch (Throwable t) {
                                log.error("Read data error", t)
                            }
                        }
                    }
                }
                log.info("Evdas SQLs executed.")
            }

            log.info("EVDAS Data mining in PV Datahub gave " + data.size() + " PE combinations.")
        } catch (Throwable throwable) {
            throw throwable
        } finally {
            sql?.close()
        }
        data
    }

    private adjustCustomDateRanges(EvdasConfiguration configuration) {

        def runOnce = JSON.parse(configuration.scheduleDateJSON).recurrencePattern.contains(Constants.Scheduler.RUN_ONCE)
        def hourly = JSON.parse(configuration.scheduleDateJSON).recurrencePattern.contains(Constants.Scheduler.HOURLY)
        def nextRunDate = configuration.nextRunDate
        if (nextRunDate && configuration.frequency && !runOnce && !hourly) {
            if (configuration.dateRangeInformation.dateRangeStartAbsolute && configuration.dateRangeInformation.dateRangeEndAbsolute) {
                calculateNextDateRangeFrequency(configuration)
            }
        } else if (!runOnce && !hourly && !configuration.frequency) {
            //Start and End check to make sure that date range is custom date range
            if (configuration.dateRangeInformation.dateRangeStartAbsolute &&
                    configuration.dateRangeInformation.dateRangeEndAbsolute) {

                def delta = configurationService.getDelta(configuration)
                def dateRangeStartAbsolute = configuration.dateRangeInformation.dateRangeStartAbsolute

                configuration.dateRangeInformation.dateRangeStartAbsolute =
                        RelativeDateConverter.getUpdatedDate(dateRangeStartAbsolute, delta)

                def dateRangeEndAbsolute = configuration.dateRangeInformation.dateRangeEndAbsolute
                configuration.dateRangeInformation.dateRangeEndAbsolute =
                        RelativeDateConverter.getUpdatedDate(dateRangeEndAbsolute, delta)
            }
        }
    }

    private void calculateNextDateRangeFrequency(EvdasConfiguration config) {
        def startDate = config.dateRangeInformation.dateRangeStartAbsolute
        def endDate = config.dateRangeInformation.dateRangeEndAbsolute
        SubstanceFrequency substanceFrequency = SubstanceFrequency.findByFrequencyNameAndAlertType(config.frequency, Constants.AlertConfigType.EVDAS_ALERT)
        def dateList = evdasAlertService.populatePossibleDateRanges(substanceFrequency.startDate, substanceFrequency.uploadFrequency).reverse(true)
        def newStartDate
        def newEndDate
        dateList.find {
            if (Date.parse(dateWriteFormat.toPattern(), it[0]) == startDate) {
                return true
            } else {
                newStartDate = it[0] ? Date.parse(dateWriteFormat.toPattern(), it[0]) : startDate
                newEndDate = it[1] ? Date.parse(dateWriteFormat.toPattern(), it[1]) : endDate
                return false
            }
        }
        config.dateRangeInformation.dateRangeStartAbsolute = newStartDate
        config.dateRangeInformation.dateRangeEndAbsolute = newEndDate
    }


    private Map readEvdasData(resultSet, List<ERMRConfig> eRMRConfigMapping, Boolean isMaster = false) {
        def v =
                [
                        substanceId            : resultSet.getString('ACTIVE_SUBSTANCES_ID'),
                        substance              : resultSet.getString('ACTIVE_SUBSTANCES'),
                        soc                    : resultSet.getString('SOCS'),
                        hlgt                   : resultSet.getString('HLGTS'),
                        hlt                    : resultSet.getString('HLTS'),
                        smqNarrow              : resultSet.getString('SMQ_NARROW'),
                        pt                     : resultSet.getString('PTS'),
                        ptCode                 : resultSet.getString('PT_CODE'),
                        dmeIme                 : resultSet.getString('IME_DME'),
                        newEv                  : resultSet.getString('NEW_EV'),
                        totalEv                : resultSet.getString('TOT_EV'),
                        newEvLink              : resultSet.getString('NEW_EVPM_LINK'),
                        totalEvLink            : resultSet.getString('TOT_EVPM_LINK'),
                        newEea                 : resultSet.getString('NEW_EEA'),
                        totEea                 : resultSet.getString('TOT_EEA'),
                        newHcp                 : resultSet.getString('NEW_HCP'),
                        totHcp                 : resultSet.getString('TOT_HCP'),
                        totalSerious           : resultSet.getString('TOT_SERIOUS'),
                        newSerious             : resultSet.getString('NEW_SERIOUS'),
                        newMedErr              : resultSet.getString('NEW_MED_ERR'),
                        totMedErr              : resultSet.getString('TOT_MED_ERR'),
                        totalFatal             : resultSet.getString('TOT_FATAL'),
                        newFatal               : resultSet.getString('NEW_FATAL'),
                        newLit                 : resultSet.getString('NEW_LIT'),
                        totalLit               : resultSet.getString('TOT_LIT'),
                        newPaed                : resultSet.getString('NEW_PAED'),
                        totPaed                : resultSet.getString('TOT_PAED'),
                        newObs                 : resultSet.getString('NEW_OBS'),
                        totObs                 : resultSet.getString('TOT_OBS'),
                        newRc                  : resultSet.getString('NEW_RC'),
                        totRc                  : resultSet.getString('TOT_RC'),
                        newGeria               : resultSet.getString('NEW_GERIATR'),
                        totGeria               : resultSet.getString('TOT_GERIATR'),
                        europeRor              : resultSet.getString('ROR_EUROPE'),
                        northAmericaRor        : resultSet.getString('ROR_NORTH_AMERICA'),
                        japanRor               : resultSet.getString('ROR_JAPAN'),
                        asiaRor                : resultSet.getString('ROR_ASIA'),
                        restRor                : resultSet.getString('ROR_REST'),
                        rorValue               : resultSet.getString('ROR_ALL'),
                        sdr                    : resultSet.getString('SDR'),
                        ratioRorPaedVsOthers   : resultSet.getString('RATIO_ROR_PAED_VS_OTHERS'),
                        ratioRorGeriatrVsOthers: resultSet.getString('RATIO_ROR_GERIATR_VS_OTHERS'),
                        changes                : resultSet.getString('CHANGES'),
                        sdrPaed                : resultSet.getString('SDR_PAED'),
                        sdrGeratr              : resultSet.getString('SDR_GERIATR'),
                        newSpont               : resultSet.getString('NEW_SPONT'),
                        totSpont               : resultSet.getString('TOT_SPONT'),
                        totSpontEurope         : resultSet.getString('TOT_SPONT_EUROPE'),
                        totSpontNAmerica       : resultSet.getString('TOT_SPONT_N_AMERICA'),
                        totSpontJapan          : resultSet.getString('TOT_SPONT_JAPAN'),
                        totSpontAsia           : resultSet.getString('TOT_SPONT_ASIA'),
                        totSpontRest           : resultSet.getString('TOT_SPONT_REST'),
                        listedness             : resultSet.getObject('LISTEDNESS') != -1 ? resultSet.getBoolean('LISTEDNESS') : null,
                        childExecutionId       : isMaster? resultSet.getString('CHILD_EXECUTION_ID') : ""
                ]
        v.attributes = eRMRConfigMapping.collectEntries {
            [(it.targetColumnName): resultSet.getString(it.targetColumnName)]
        }
        v.attributes = [:]
        v
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception)
    ExecutedEvdasConfiguration createExecutedConfiguration(EvdasConfiguration configuration) throws Exception {
        try {
            ExecutedEvdasConfiguration executedConfiguration = saveExecutedConfiguration(configuration)
            return executedConfiguration
        } catch (Throwable throwable) {
            log.error("Error happened when creating executed configuration " +
                    "for ${configuration?.name}. Skipping", throwable)
            throw throwable
        }
    }

    ExecutedEvdasConfiguration saveExecutedConfiguration(EvdasConfiguration configuration) {
        String frequency
        if (configuration?.dateRangeInformation.dateRangeEnum != DateRangeEnum.CUMULATIVE) {
            frequency = configuration.frequency
        }

        ExecutedEVDASDateRangeInformation executedEVDASDateRangeInformation = getExecutedDateRangeInformation(configuration.dateRangeInformation)

        ExecutedEvdasConfiguration executedConfiguration = new ExecutedEvdasConfiguration(name: configuration.name,
                owner: User.get(configuration.owner.id), scheduleDateJSON: configuration.scheduleDateJSON, nextRunDate: configuration.nextRunDate ?: new Date(),
                description: configuration.description, dateCreated: configuration.dateCreated, lastUpdated: configuration.lastUpdated, isDeleted: configuration.isDeleted, workflowGroup: configuration.workflowGroup,
                isEnabled: configuration.isEnabled, productSelection: configuration.productSelection, eventSelection: configuration.eventSelection,
                configSelectedTimeZone: configuration.configSelectedTimeZone, createdBy: configuration.getOwner().username, modifiedBy: configuration.modifiedBy,
                dateRangeInformation: executedEVDASDateRangeInformation, executionStatus: ReportExecutionStatus.COMPLETED,
                numOfExecutions: configuration.numOfExecutions + 1, adhocRun: configuration.adhocRun, frequency: frequency,
                assignedTo: configuration.assignedTo, assignedToGroup: configuration.assignedToGroup, productGroupSelection: configuration.productGroupSelection,
                executedQueryName: configuration.queryName, executedQuery: configuration.query, priority: configuration.priority, configId: configuration.id, eventGroupSelection: configuration.eventGroupSelection,masterExConfigId: configuration?.masterConfigId)
        executedConfiguration.isEnabled = false
        executedConfiguration.isLatest = true
        if(configuration.isDatasheetChecked && !TextUtils.isEmpty(configuration.selectedDataSheet)){
            executedConfiguration.isDatasheetChecked = configuration.isDatasheetChecked
            executedConfiguration.datasheetType = configuration.datasheetType
            executedConfiguration.selectedDataSheet = alertService.getOnlyActiveDataSheets(configuration,true) as JSON
        }
        executedConfiguration.allProducts = reportExecutorService.getAllProductsWithHierarchy(executedConfiguration)
        generateReviewDueDate(executedConfiguration)
        generateProductName(executedConfiguration)
        executedConfiguration.save()
        executedConfiguration
    }

    private getExecutedDateRangeInformation(EVDASDateRangeInformation dateRangeInformation) {
        ExecutedEVDASDateRangeInformation executedEVDASDateRangeInformation = new ExecutedEVDASDateRangeInformation(dateRangeInformation.properties)
        executedEVDASDateRangeInformation
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    EvdasConfiguration createConfigForIntegratedReview(Configuration configuration , EvdasConfiguration evdasConfiguration, Long masterConfigId = null) {
        evdasConfiguration.setProperties([name: configuration.name,
                owner: User.get(configuration.owner.id), scheduleDateJSON: configuration.scheduleDateJSON, nextRunDate: null,
                description: configuration.description, dateCreated: configuration.dateCreated, lastUpdated: configuration.lastUpdated,
                isDeleted: configuration.isDeleted, workflowGroup: configuration.workflowGroup,
                isEnabled: configuration.isEnabled, productGroupSelection: configuration.productGroupSelection, eventSelection: configuration.eventSelection,
                configSelectedTimeZone: configuration.configSelectedTimeZone, createdBy: configuration.getOwner().username, modifiedBy: configuration.modifiedBy,
                dateRangeInformation: evdasConfiguration.dateRangeInformation ?: createDateRangeForIntegratedReview(configuration.productGroupList, DateRangeEnum.CUSTOM),
                executionStatus: ReportExecutionStatus.COMPLETED,
                numOfExecutions: 0, adhocRun: configuration.adhocRun, frequency: null,
                assignedTo: configuration.assignedTo, assignedToGroup: configuration.assignedToGroup,
                priority: configuration.priority, eventGroupSelection: configuration.eventGroupSelection, integratedConfigurationId : configuration?.id, masterConfigId: masterConfigId])
        evdasConfiguration.isEnabled = false
        evdasConfiguration.skipAudit = true
        if(!evdasConfiguration.dateRangeInformation.dateRangeStartAbsolute && !evdasConfiguration.dateRangeInformation.dateRangeEndAbsolute ){
            return null
        }else{
            println evdasConfiguration
            evdasConfiguration.save(flush: true)
            println EvdasConfiguration.findByName(evdasConfiguration.name)
            println EvdasConfiguration.get(evdasConfiguration.id)
        }
        evdasConfiguration

    }

    EVDASDateRangeInformation createDateRangeForIntegratedReview(String productGroupId, DateRangeEnum dateRangeEnum, Long id = null) {
        EVDASDateRangeInformation dateRangeInformation = id ? EVDASDateRangeInformation.get(id) : new EVDASDateRangeInformation()

        dateRangeInformation.dateRangeEnum = dateRangeEnum
            Sql evdasSql = new Sql(signalDataSourceService.getReportConnection(Constants.DataSource.EUDRA))
            String evdasDataJson
            try {
                evdasSql.eachRow("select GRP_DATA from VW_DICT_GRP_DATA where DICT_GRP_ID IN ("+productGroupId+")") { ResultSet resultSetObj ->
                    Clob clob = resultSetObj.getClob("GRP_DATA")
                    if (clob) {
                        evdasDataJson = clob.getSubString(1, (int) clob.length())
                    }
                }
                String substance = getNameFieldFromJson(evdasDataJson)
                EvdasFileProcessLog evdasFileProcessLog = EvdasFileProcessLog.createCriteria().get {
                    eq('substances', substance, [ignoreCase: true])
                    eq('dataType', 'eRMR')
                    eq('status', EvdasFileProcessState.SUCCESS)
                    order('recordEndDate', 'desc')
                    maxResults(1)
                } as EvdasFileProcessLog

                dateRangeInformation.dateRangeStartAbsolute = evdasFileProcessLog?.recordStartDate
                dateRangeInformation.dateRangeEndAbsolute = evdasFileProcessLog?.recordEndDate
            } catch (Throwable th) {
                log.error("Error occurred while fetching EVDAS date range for substance. ", th)
                throw th
            } finally {
                evdasSql.close()
            }
        dateRangeInformation
    }

    List fetchEvdasAlertDataFromMart(EvdasConfiguration evdasConfiguration, ExecutedEvdasConfiguration executedEvdasConfiguration, String filePath,Long exEvdasConfigId=0L) throws Exception {
        ArrayList<Map> alertData = []
        Sql sql
        try {
            sql = new Sql(signalDataSourceService.getReportConnection(Constants.DataSource.EUDRA))
            String gttInserts = evdasSqlGenerationService.initializeInsertGtts(evdasConfiguration)
            log.info(gttInserts)
            sql.execute(gttInserts)
            log.info("GTT Inserts executed.")
            if(executedEvdasConfiguration?.isDatasheetChecked && executedEvdasConfiguration?.selectedDataSheet && !TextUtils.isEmpty(executedEvdasConfiguration?.selectedDataSheet)){
                Sql pvaSql =null
                try{
                    pvaSql = new Sql(dataSource_pva)
                    String insertStatement =  "Begin execute immediate('delete from gtt_pvs_ds_input_data');"
                    Map dataSheetsMap = JSON.parse(executedEvdasConfiguration?.selectedDataSheet)
                    def dsId = null
                    def baseId = null
                    dataSheetsMap?.each { key, value ->
                        dsId = key?.toString()?.split('_')[0]
                        baseId = key?.toString()?.split('_')[1]
                        insertStatement += "INSERT INTO GTT_PVS_DS_INPUT_DATA (Base_Id,Datasheet_Id, Datasheet_Name, Execution_Id) VALUES ('${baseId}','${dsId}','${value}', '${exEvdasConfigId}');"
                    }
                    insertStatement += " END;"
                    pvaSql.execute(insertStatement)
                    log.info("Calling PKG_SAFETY_AGG_LISTEDNESS.p_mains(?)")
                    String procedure = "call  PKG_SAFETY_AGG_LISTEDNESS.p_mains(?)"
                    pvaSql.call("{${procedure}", [exEvdasConfigId])
                }catch(Exception e){
                    e.printStackTrace()
                }
                finally{
                    pvaSql?.close()
                }

            }

            String queryInserts = evdasSqlGenerationService.initializeQuerySql(evdasConfiguration)
            log.info(queryInserts)
            sql.execute(queryInserts)
            log.info("Query Inserts executed.")
            alertData = prepareAlertDataFromMart(sql, evdasConfiguration)
            alertService.saveAlertDataInFile(alertData, filePath)
        } catch (Throwable throwable) {
            log.error("Exception came in executing the mining run. ", throwable)
            throw throwable
        } finally {
            sql?.close()
        }
        alertData
    }

    List prepareAlertDataFromMart(Sql sql, EvdasConfiguration evdasConfiguration, Boolean isMaster=false, def dateRangeEnum = null) {
        List<ERMRConfig> eRMRConfigMapping
        String dataSourceValue = "pva"

        if (Holders.config.signal.evdas.enabled) {
            dataSourceValue = "eudra"
        }

        ERMRConfig."$dataSourceValue".withTransaction {
            eRMRConfigMapping = ERMRConfig.findAllByExcluded('N')
        }
        String evdasSql = evdasSqlGenerationService.getEvdasQuerySql()
        log.info(evdasSql)
        List data = []
        def isDateRangeCumulative
        if (!evdasConfiguration) {
            isDateRangeCumulative = dateRangeEnum == 'CUMULATIVE' ? 1 : 0
        }
        if (evdasConfiguration) {
            isDateRangeCumulative = evdasConfiguration.dateRangeInformation?.dateRangeEnum == DateRangeEnum.CUMULATIVE ? 1: 0
        }
        if (evdasSql) {
            sql.call(evdasSql, [isDateRangeCumulative, Sql.resultSet(OracleTypes.CURSOR)]) { evdas_info ->
                if (evdas_info) {
                    while (evdas_info.next()) {
                        try {
                            data << readEvdasData(evdas_info, eRMRConfigMapping, isMaster)
                        } catch (Throwable t) {
                            log.error("Read data error", t)
                        }
                    }
                }
            }
            log.info("Evdas SQLs executed.")
        }

        log.info("EVDAS Data mining in PV Datahub gave " + data.size() + " PE combinations.")
        data
    }

    void saveEvdasAlertData(EvdasConfiguration evdasConfiguration, ExecutedEvdasConfiguration executedEvdasConfiguration, String filePath) throws Exception {
        try {
            List<Map> alertDataList = alertService.loadAlertDataFromFile(filePath)
            if (evdasConfiguration.adhocRun) {
                evdasOnDemandAlertService.createOnDemandEvdasAlert(evdasConfiguration.id, executedEvdasConfiguration.id, alertDataList)
            } else {
                evdasAlertService.createEvdasAlert(evdasConfiguration.id, executedEvdasConfiguration.id, alertDataList)
            }
            setValuesForConfiguration(evdasConfiguration.id, executedEvdasConfiguration.id)
            File file = new File(filePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (Throwable throwable) {
            log.error("Exception came in saving the Evdas alerts. ", throwable)
            throw throwable
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    void updateExecutionStatus(ExecutionStatus executionStatus, ExecutedEvdasConfiguration executedConfiguration) {
        executionStatus.executedConfigId = executedConfiguration.id
        executionStatus.alertFilePath = "${grailsApplication.config.signal.alert.file}/${executedConfiguration.id}_${Constants.AlertType.EVDAS_ALERT}"
        executionStatus.executionLevel = executionStatus.executionLevel + 1
        updateTimeStampJson(executionStatus)
        executionStatus.save(flush:true)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    void updateExecutionStatusLevel(ExecutionStatus executionStatus) {
        executionStatus.executionLevel = executionStatus.executionLevel + 1
        updateTimeStampJson(executionStatus)
        executionStatus.save(flush:true)
    }

    void updateTimeStampJson(ExecutionStatus executionStatus) {
        JsonSlurper jsonSlurper = new JsonSlurper()
        Map timeStamp = new HashMap<String, Long>()
        if (executionStatus.timeStampJSON)
            timeStamp = jsonSlurper.parseText(executionStatus.timeStampJSON)
        timeStamp.put(executionStatus.executionLevel?.toString(), System.currentTimeMillis())
        executionStatus.timeStampJSON = timeStamp as JSON
    }

    void generateReviewDueDate(ExecutedEvdasConfiguration executedConfiguration) {
        if (executedConfiguration?.isAdhocRun()) {
            executedConfiguration.reviewDueDate = new Date()
            return
        }
        Disposition defaultEvdasDisposition = executedConfiguration.owner.workflowGroup.defaultEvdasDisposition
        List<PriorityDispositionConfig> dispositionConfigs = cacheService.getDispositionConfigsByPriority(executedConfiguration.priority.id)
        Integer reviewPeriod = dispositionConfigs?.find{it.disposition == defaultEvdasDisposition}?.reviewPeriod
        reviewPeriod = reviewPeriod ?: executedConfiguration.priority.reviewPeriod
        DateTime theDueDate = reviewPeriod ? new DateTime(executedConfiguration.dateCreated).plusDays(reviewPeriod) : new DateTime(new Date())
        executedConfiguration.reviewDueDate = theDueDate.toDate()
    }

    void generateProductName(ExecutedEvdasConfiguration executedConfiguration) {
        if (executedConfiguration.productGroupSelection) {
            executedConfiguration.productName = getGroupNameFieldFromJson(executedConfiguration.productGroupSelection)
        } else if (executedConfiguration.productSelection) {
            executedConfiguration.productName = getNameFieldFromJson(executedConfiguration.productSelection)
        }
    }

}
