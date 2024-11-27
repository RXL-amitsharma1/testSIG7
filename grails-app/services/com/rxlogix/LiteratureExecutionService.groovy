package com.rxlogix


import com.rxlogix.cache.CacheService
import com.rxlogix.cache.HazelcastService
import com.rxlogix.config.*
import com.rxlogix.customException.ExecutionStatusException
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.time.TimeCategory
import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil
import org.apache.commons.lang3.StringUtils
import org.joda.time.DateTimeZone
import org.springframework.context.MessageSource
import org.springframework.transaction.annotation.Propagation

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields

class LiteratureExecutionService implements AlertUtil {

    static transactional = false
    def detailUrl = "LITERATURE_reportRedirectURL"
    static final String RUNNING_LIT_CONFIG = 'running_literature_cfg'
    ConfigurationService configurationService
    LiteratureAlertService literatureAlertService
    CacheService cacheService
    def notificationHelper
    EmailNotificationService emailNotificationService
    MessageSource messageSource
    CRUDService CRUDService
    ReportExecutorService reportExecutorService
    EmailService emailService
    def grailsApplication
    def alertService
    def userGroupService
    HazelcastService hazelcastService

    List<Long> currentlyRunning = []
    def signalAuditLogService


    void runConfigurations() throws Exception {
        LiteratureConfiguration scheduledConfiguration = LiteratureConfiguration.getNextConfigurationToExecute(currentlyRunning)
        if (scheduledConfiguration) {
            currentlyRunning.add(scheduledConfiguration.id)
            log.info("currentlyRunning size " + currentlyRunning.size())
            scheduledConfiguration.executing = true
            getStartAndEndDateRangeForLiteratureAlert(scheduledConfiguration)
            CRUDService.updateWithoutAuditLog(scheduledConfiguration)
            log.info("Found ${scheduledConfiguration.name}(${scheduledConfiguration.id}) to execute.")
            ExecutionStatus executionStatus = null
            if (scheduledConfiguration.isResume) {
                Long exConfigId = ExecutedLiteratureConfiguration.createCriteria().get {
                    eq('configId' , scheduledConfiguration?.id)
                    projections { max "id" }
                } as Long
                executionStatus = ExecutionStatus.findByConfigIdAndExecutedConfigIdAndType(scheduledConfiguration.id,
                        exConfigId, Constants.AlertConfigType.LITERATURE_SEARCH_ALERT)

                executionStatus.executionStatus = ReportExecutionStatus.GENERATING
                CRUDService.updateWithoutAuditLog(executionStatus)
            } else {
                executionStatus = createExecutionStatus(scheduledConfiguration)
                getStartAndEndDateRangeForLiteratureAlert(scheduledConfiguration)
            }
            try {
                startLiteratureAlertExecutionByLevel(executionStatus)
            } catch (Throwable e) {
                handleFailedExecution(e, scheduledConfiguration.id, executionStatus.executedConfigId)
                log.error("Exception in Executor Service" + e, e)
            } finally {
                currentlyRunning.remove(scheduledConfiguration.id)
            }
            log.info("Execution is Done")
        }
    }

    void startLiteratureAlertExecutionByLevel(ExecutionStatus executionStatus) throws Exception {
        try {
            LiteratureConfiguration lockedConfiguration = LiteratureConfiguration.get(executionStatus.configId)
            ExecutedLiteratureConfiguration executedLiteratureConfiguration
            if (executionStatus.executedConfigId) {
                executedLiteratureConfiguration = ExecutedLiteratureConfiguration.get(executionStatus.executedConfigId)
            }
            switch (executionStatus.executionLevel) {
                case 0: executedLiteratureConfiguration = createExecutedConfiguration(lockedConfiguration)
                    updateExecutionStatus(executionStatus, executedLiteratureConfiguration)
                case 1: fetchLiteratureAlertData(lockedConfiguration, executionStatus.alertFilePath)
                    updateExecutionStatusLevel(executionStatus)
                case 2: saveLiteratureAlertData(lockedConfiguration, executedLiteratureConfiguration, executionStatus.alertFilePath)
            }
        } catch (Throwable throwable) {
            log.error("Exception while running the literature alert", throwable)
            ExecutionStatusException ese = new ExecutionStatusException(alertService.exceptionString(throwable))
            throw ese
        }
    }

    void fetchLiteratureAlertDataFromPubmed(LiteratureConfiguration literatureConfiguration, String filePath) throws Exception {
        try {
            //This call returns the Map of ids, webenv and querykey from the selected literature database.
            Map idListAndWebEnv = literatureAlertService.fetchIdListForLiteratureData(literatureConfiguration)

            //This call returns the xml string based on the passed Id list, webenv, querykey.
            String alertData = literatureAlertService.fetchDataForIds(idListAndWebEnv)
            if (StringUtils.isNotBlank(alertData) && StringUtils.equalsIgnoreCase(Constants.API_CALL_FAILED_MESSAGE, alertData)) {
                throw new Exception(Constants.API_CALL_FAILED_MESSAGE)
            }
            alertService.saveLiteratureDataInFile(alertData, filePath)
        } catch (Throwable throwable) {
            log.error("Exception came in fetching data from pubmed. ", throwable)
            throw throwable
        }
    }

    void fetchLiteratureAlertData(LiteratureConfiguration literatureConfiguration, String filePath) throws Exception {
        def dataSource = literatureConfiguration.selectedDatasource
        if (Constants.LiteratureFields.PUBMED.equalsIgnoreCase(dataSource)) {
            fetchLiteratureAlertDataFromPubmed(literatureConfiguration, filePath)
        }
        else if (Constants.LiteratureFields.EMBASE.equalsIgnoreCase(dataSource)) {
            fetchLiteratureAlertDataFromEmbase(literatureConfiguration, filePath)
        }
    }

    void fetchLiteratureAlertDataFromEmbase(LiteratureConfiguration literatureConfiguration, String filePath) throws Exception {
        try {
            literatureAlertService.fetchEmbaseLiteratureData(literatureConfiguration, filePath)
        } catch (Throwable throwable) {
            log.error("Exception came in fetching data from Embase. ", throwable)
            File file = new File(filePath)
            if (file.exists()) {
                file.delete()
            }
            throw throwable
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    def handleFailedExecution(ExecutionStatusException ese, Long configId, Long executedConfigId) {
        LiteratureConfiguration config
        try {
            config = LiteratureConfiguration.get(configId)
            ExecutedLiteratureConfiguration executedConfiguration = ExecutedLiteratureConfiguration.get(executedConfigId)
            ExecutionStatus executionStatus = ExecutionStatus.findByConfigIdAndReportVersionAndType(configId,
                            config.isResume ? config.numOfExecutions : config.numOfExecutions + 1, Constants.AlertConfigType.LITERATURE_SEARCH_ALERT)
            if (executionStatus) {
                executionStatus.stackTrace = ese?.errorCause
                executionStatus.executionStatus = ReportExecutionStatus.ERROR
                executionStatus.save(flush:true)

                executedConfiguration.isEnabled = false
                executedConfiguration.save()

                if (executedConfiguration) {
                    //After that set the notification.
                    addNotification(executedConfiguration, executionStatus, executedConfiguration.assignedTo, executedConfiguration.assignedToGroup)
                    emailNotificationService.emailHanlderAtAlertLevel(executedConfiguration, executionStatus)
                }
            } else {
                log.error("Cannot find the execution status. [handleFailedExecution]")
            }
        } catch (Throwable th) {
            log.error("Error happened when handling failed Configurations [${executedConfigId}]", th)
        }finally{
            if(config){
                setNextRunDateForConfiguration(config)
                config.isEnabled = true
                config.isResume = false
                config.executing = false
                config.save()
            }
        }
    }

    private setTotalExecutionTimeForConfiguration(LiteratureConfiguration configuration, long executionTime) throws Exception {
        configuration.totalExecutionTime = executionTime
    }

    private void setNextRunDateForConfiguration(LiteratureConfiguration configuration) throws Exception {
        configuration.numOfExecutions = configuration.isResume ? (configuration.numOfExecutions?:1) : configuration.numOfExecutions + 1
        configuration.setNextRunDate(configurationService.getNextDate(configuration))
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    def setValuesForConfiguration(LiteratureConfiguration scheduledConfiguration,
                                  ExecutedLiteratureConfiguration executedConfiguration) {

        LiteratureConfiguration lockedConfiguration
        ExecutionStatus executionStatus
        try {
            lockedConfiguration = LiteratureConfiguration.lock(scheduledConfiguration.id)
            executionStatus = ExecutionStatus.findByConfigIdAndExecutedConfigIdAndType(lockedConfiguration.id,executedConfiguration.id,
                    Constants.AlertConfigType.LITERATURE_SEARCH_ALERT)
            if (executionStatus) {

                executionStatus.endTime = System.currentTimeMillis()
                setTotalExecutionTimeForConfiguration(lockedConfiguration, (executionStatus.endTime - executionStatus.startTime))

                lockedConfiguration.executing = false

                if (executionStatus?.executionStatus != ReportExecutionStatus.ERROR) {
                    executionStatus?.executionStatus = ReportExecutionStatus.COMPLETED
                    lockedConfiguration?.isResume = false
                }
                if (executedConfiguration) {
                    //After that set the notification.
                    updateIsLatestForOldExecutedConfigurations(lockedConfiguration, executedConfiguration.id)
                    addNotification(executedConfiguration, executionStatus, executedConfiguration.assignedTo, executedConfiguration.assignedToGroup)
                    emailNotificationService.emailHanlderAtAlertLevel(executedConfiguration, executionStatus)
                }
                setNextRunDateForConfiguration(lockedConfiguration)

                if (lockedConfiguration?.futureScheduleDateJSON) {
                    lockedConfiguration.scheduleDateJSON = lockedConfiguration.futureScheduleDateJSON
                    lockedConfiguration.setNextRunDate(configurationService.getNextDate(lockedConfiguration))
                    lockedConfiguration.setFutureScheduleDateJSON(null)
                }

            }
        } catch (Throwable th) {
            log.error("Error happened when handling Successful Configurations [${scheduledConfiguration.id}]", th)
        } finally {
            if (lockedConfiguration) {
                lockedConfiguration.save()
            }
            if (executionStatus) {
                executionStatus.save(flush:true)
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    private void addNotification(ExecutedLiteratureConfiguration executedConfiguration,
                                 ExecutionStatus executionStatus,
                                 User user, Group group = null) {
        List<User> notificationRecipients = group ? userGroupService.fetchUserListForGroup(group) : [user]
        if (notificationRecipients) {
            try {
                NotificationLevel status
                String message
                String messageArgs = "$executedConfiguration.name"
                def url = detailUrl
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

                notificationRecipients.each { User notificationRecipient ->
                    InboxLog inboxLog = new InboxLog(notificationUserId: notificationRecipient?.id, level: status, message: message,
                            messageArgs: messageArgs, type: "Literature Alert Execution", subject: messageSource.getMessage(message, [messageArgs].toArray(),
                            notificationRecipient?.preference?.locale),
                            content: "", createdOn: new Date(), inboxUserId: notificationRecipient?.id, isNotification: true,
                            executedConfigId: executedConfiguration?.id, detailUrl: url)
                    inboxLog.save(flush: true)
                    notificationHelper.pushNotification(inboxLog)
                }
                    if(message in ["app.notification.completed","app.notification.failed"]) {
                        reportExecutorService.createAuditForExecution(executedConfiguration, message, Constants.AlertConfigType.LITERATURE_ALERT_CONFIGURATIONS)
                    }
            } catch (Throwable e) {
                log.error("Error creating Notification: ${e.message}", e)
            }
        }
    }

    int getExecutionQueueSize() {
        return currentlyRunning.size()
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    ExecutionStatus createExecutionStatus(LiteratureConfiguration scheduledConfiguration) {
        LiteratureConfiguration lockedConfiguration = LiteratureConfiguration.get(scheduledConfiguration.id)
        ExecutionStatus executionStatus = new ExecutionStatus(
                configId: lockedConfiguration.id,
                reportVersion: lockedConfiguration.numOfExecutions + 1,
                startTime: System.currentTimeMillis(),
                nextRunDate: lockedConfiguration.nextRunDate,
                owner: lockedConfiguration.owner,
                name: lockedConfiguration.name,
                attachmentFormats: null,
                executionStatus : ReportExecutionStatus.GENERATING,
                sharedWith: null,
                type: Constants.AlertConfigType.LITERATURE_SEARCH_ALERT,
                nodeName: hazelcastService.getName())
        executionStatus.frequency = configurationService.calculateFrequency(lockedConfiguration)
        CRUDService.saveWithoutAuditLog(executionStatus)
        executionStatus
    }

    /**
     * Creation of Executed configuration is cruicial in alert execution flow. As we are trying to maintain one execution configuration for the
     * single case alert all the time for each logged in user.
     * @param configuration
     * @return
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception)
    ExecutedLiteratureConfiguration createExecutedConfiguration(LiteratureConfiguration configuration) throws Exception {
        ExecutedLiteratureConfiguration executedConfiguration = saveExecutedConfiguration(configuration)
        return executedConfiguration
    }

    private saveExecutedConfiguration(LiteratureConfiguration configuration) {


        ExecutedLiteratureDateRangeInformation executedLiteratureDateRangeInformation = getExecutedDateRangeInformation(configuration.dateRangeInformation)

        ExecutedLiteratureConfiguration executedConfiguration = new ExecutedLiteratureConfiguration(name: configuration.name,
                scheduleDateJSON: configuration.scheduleDateJSON, assignedTo: configuration?.assignedTo, assignedToGroup: configuration?.assignedToGroup,
                dateCreated: configuration.dateCreated, lastUpdated: configuration.lastUpdated,
                isDeleted: configuration.isDeleted, isEnabled: configuration.isEnabled,
                searchString: configuration.searchString, dateRangeInformation: executedLiteratureDateRangeInformation,
                productSelection: configuration.productSelection, eventSelection: configuration.eventSelection,
                configSelectedTimeZone: configuration.configSelectedTimeZone,
                workflowGroup: configuration.workflowGroup,
                createdBy: configuration.getOwner().username, modifiedBy: configuration.modifiedBy, owner: User.get(configuration.owner.id),
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: configuration.numOfExecutions + 1,selectedDatasource: configuration.selectedDatasource, configId: configuration.id,
                productGroupSelection: configuration.productGroupSelection, eventGroupSelection: configuration.eventGroupSelection)
        generateProductName(executedConfiguration)
        executedConfiguration.save()
        executedConfiguration
    }

    private getExecutedDateRangeInformation(LiteratureDateRangeInformation dateRangeInformation) {
        ExecutedLiteratureDateRangeInformation executedLiteratureDateRangeInformation = new ExecutedLiteratureDateRangeInformation(dateRangeInformation.properties)
        executedLiteratureDateRangeInformation
    }

    /**
     * this method is used to set isLatest to false for all previous executed configurations and 
     * to set isLatest to true for the current executed configuration
     * @param literatureConfiguration The literature configuration whose executed configurations are to be updated.
     * @param executedConfigId The ID of the executed configuration to be marked as the latest.
    */
    @Transactional
    private updateIsLatestForOldExecutedConfigurations(LiteratureConfiguration literatureConfiguration,Long executedConfigId) {

        // Bug/PVS-59022: findAllByNameAndOwner has been deprecated in favor of findAllByConfigIdAndOwner to accommodate changes when user edits the alert name.
        List<ExecutedLiteratureConfiguration> list = ExecutedLiteratureConfiguration.findAllByConfigIdAndOwner(literatureConfiguration.id, literatureConfiguration.owner)
        list.each { ExecutedLiteratureConfiguration executedConfiguration ->
            executedConfiguration.isLatest = false
            executedConfiguration.save()
        }
        ExecutedLiteratureConfiguration executedConfiguration = ExecutedLiteratureConfiguration.get(executedConfigId)
        executedConfiguration.isLatest = true
        executedConfiguration.save()
    }

    Boolean isConfigurationLocked(Long configId) {
        com.rxlogix.util.Tuple2 runningConfig = cacheService.getCache(RUNNING_LIT_CONFIG).get(configId as String) as Tuple2
        if (runningConfig)
            true
        else
            false
    }

    com.rxlogix.util.Tuple2 lockConfiguration(Long configurationId) {
        com.rxlogix.util.Tuple2 cachedData = new Tuple2(configurationId, System.currentTimeMillis())
        cacheService.getCache(RUNNING_LIT_CONFIG).put(configurationId as String, cachedData)
        cachedData
    }

    void unlockConfiguration(Long configurationId) {
        cacheService.getCache(RUNNING_LIT_CONFIG).remove(configurationId as String)
    }

    void getStartAndEndDateRangeForLiteratureAlert(LiteratureConfiguration config) {
        DateRangeEnum dateRange = config.dateRangeInformation.dateRangeEnum
        int dateRangeValue = config.dateRangeInformation.relativeDateRangeValue
        ZoneId zoneId = ZoneId.of(DateTimeZone.forID(Holders.config.server.timezone).ID)
        if (DateRangeEnum.YESTERDAY.equals(dateRange) || DateRangeEnum.LAST_X_DAYS.equals(dateRange)) {
            setStartAndEndDateInConfig(config, Date.from(LocalDate.now().minusDays(dateRangeValue).atStartOfDay(zoneId).toInstant()))
        } else if (DateRangeEnum.LAST_WEEK.equals(dateRange) || DateRangeEnum.LAST_X_WEEKS.equals(dateRange)) {
            DayOfWeek firstDayOfWeek = WeekFields.of(Locale.getDefault()).getFirstDayOfWeek();
            DayOfWeek lastDayOfWeek = firstDayOfWeek.plus(6);
            config.dateRangeInformation.dateRangeStartAbsolute = Date.from(LocalDate.now().minusWeeks(dateRangeValue).with(TemporalAdjusters.previousOrSame(firstDayOfWeek)).atStartOfDay(zoneId).toInstant())
            config.dateRangeInformation.dateRangeEndAbsolute = Date.from((LocalDate.now().with(TemporalAdjusters.previousOrSame(lastDayOfWeek)).atStartOfDay(zoneId)).toInstant())
        } else if (DateRangeEnum.LAST_MONTH.equals(dateRange) || DateRangeEnum.LAST_X_MONTHS.equals(dateRange)) {
            config.dateRangeInformation.dateRangeStartAbsolute = Date.from(LocalDate.now().minusMonths(dateRangeValue).with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay(zoneId).toInstant())
            config.dateRangeInformation.dateRangeEndAbsolute = Date.from((LocalDate.now().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth()).atStartOfDay(zoneId)).toInstant())
        } else if (DateRangeEnum.LAST_YEAR.equals(dateRange) || DateRangeEnum.LAST_X_YEARS.equals(dateRange)) {
            config.dateRangeInformation.dateRangeStartAbsolute = Date.from(LocalDate.now().minusYears(dateRangeValue).with(TemporalAdjusters.firstDayOfYear()).atStartOfDay(zoneId).toInstant())
            config.dateRangeInformation.dateRangeEndAbsolute = Date.from(LocalDate.now().minusYears(1).with(TemporalAdjusters.lastDayOfYear()).atStartOfDay(zoneId).toInstant())
        }
    }

    void setStartAndEndDateInConfig(LiteratureConfiguration config, Date startDate) {
        use(TimeCategory) {
            config.dateRangeInformation.dateRangeEndAbsolute = DateUtil.endOfDay(new Date() -1)
        }
        config.dateRangeInformation.dateRangeStartAbsolute = startDate
    }

    void saveLiteratureAlertData(LiteratureConfiguration literatureConfiguration,
                                 ExecutedLiteratureConfiguration executedLiteratureConfiguration, String filePath) throws Exception {
        def dataSource = literatureConfiguration.selectedDatasource

        if (Constants.LiteratureFields.PUBMED.equalsIgnoreCase(dataSource)) {
            savePubmedLiteratureAlertData(literatureConfiguration, executedLiteratureConfiguration, filePath)
        }
        else if (Constants.LiteratureFields.EMBASE.equalsIgnoreCase(dataSource)) {
            saveEmbaseLiteratureAlertData(literatureConfiguration, executedLiteratureConfiguration, filePath)
        }
    }

    void savePubmedLiteratureAlertData(LiteratureConfiguration literatureConfiguration,
                                 ExecutedLiteratureConfiguration executedLiteratureConfiguration, String filePath) throws Exception {
        try {
            List<Map<String, Object>> alertDataList = alertService.parseXmlWithStax(filePath)
            literatureAlertService.createAlert(literatureConfiguration.id, executedLiteratureConfiguration.id, alertDataList)
            alertDataList = null
            File file = new File(filePath)
            if (file.exists()) {
                file.delete()
            }
            setValuesForConfiguration(literatureConfiguration, executedLiteratureConfiguration)
        } catch (Throwable throwable) {
            log.error("Exception came in saving the Literature alerts. ", throwable)

            throw throwable
        } finally {
            System.gc()
        }
    }

    void saveEmbaseLiteratureAlertData(LiteratureConfiguration literatureConfiguration,
                                 ExecutedLiteratureConfiguration executedLiteratureConfiguration, String filePath) throws Exception {
        try {
            literatureAlertService.createEmbaseAlerts(literatureConfiguration, executedLiteratureConfiguration, filePath)

            File file = new File(filePath)
            if (file.exists()) {
                file.delete()
            }
            setValuesForConfiguration(literatureConfiguration, executedLiteratureConfiguration)
        } catch (Throwable throwable) {
            log.error("Exception came in saving the Literature alerts. ", throwable)

            throw throwable
        } finally {
            System.gc()
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    void updateExecutionStatus(ExecutionStatus executionStatus, ExecutedLiteratureConfiguration executedConfiguration) {
        executionStatus.executedConfigId = executedConfiguration.id
        executionStatus.alertFilePath = "${grailsApplication.config.signal.alert.file}/${executedConfiguration.id}_${Constants.AlertType.LITERATURE_ALERT}"
        executionStatus.executionLevel = executionStatus.executionLevel + 1
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

    void generateProductName(ExecutedLiteratureConfiguration executedConfiguration) {
        LiteratureConfiguration configuration = LiteratureConfiguration.get(executedConfiguration.configId)
        if (executedConfiguration.productGroupSelection && executedConfiguration.productSelection && executedConfiguration.searchString) {
            executedConfiguration.productName = "${ViewHelper.getDictionaryValues(configuration, DictionaryTypeEnum.PRODUCT_GROUP)}, ${ViewHelper.getDictionaryValues(configuration, DictionaryTypeEnum.PRODUCT)}: ${executedConfiguration.searchString}"
        } else if (executedConfiguration.productGroupSelection && executedConfiguration.productSelection) {
            executedConfiguration.productName = "${ViewHelper.getDictionaryValues(configuration, DictionaryTypeEnum.PRODUCT_GROUP)}, ${ViewHelper.getDictionaryValues(configuration, DictionaryTypeEnum.PRODUCT)}"
        } else if (executedConfiguration.productGroupSelection && executedConfiguration.searchString) {
            executedConfiguration.productName = "${ViewHelper.getDictionaryValues(configuration, DictionaryTypeEnum.PRODUCT_GROUP)}: ${executedConfiguration.searchString}"
        } else if (executedConfiguration.productSelection && executedConfiguration.searchString) {
            executedConfiguration.productName = "${ViewHelper.getDictionaryValues(configuration, DictionaryTypeEnum.PRODUCT)}: ${executedConfiguration.searchString}"
        } else if (executedConfiguration.productGroupSelection) {
            executedConfiguration.productName = ViewHelper.getDictionaryValues(configuration, DictionaryTypeEnum.PRODUCT_GROUP)
        } else if (executedConfiguration.productSelection) {
            executedConfiguration.productName = ViewHelper.getDictionaryValues(configuration, DictionaryTypeEnum.PRODUCT)
        } else if (executedConfiguration.searchString) {
            executedConfiguration.productName = executedConfiguration.searchString
        }
    }

}
