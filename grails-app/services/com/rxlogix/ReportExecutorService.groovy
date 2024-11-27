package com.rxlogix

import com.rxlogix.audit.AuditTrail
import com.rxlogix.cache.CacheService
import com.rxlogix.cache.HazelcastService
import com.rxlogix.config.*
import com.rxlogix.customException.ExecutionStatusException
import com.rxlogix.dto.CumThreadInfoDTO
import com.rxlogix.dto.SuperQueryDTO
import com.rxlogix.enums.*
import com.rxlogix.helper.LinkHelper
import com.rxlogix.json.JsonOutput
import com.rxlogix.pvdictionary.DictionaryGroupCmd
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.signal.EmergingIssue
import com.rxlogix.signal.GroupedAlertInfo
import com.rxlogix.signal.ProductTypeConfiguration
import com.rxlogix.signal.ProductViewAssignment
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.UserViewAssignment
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.*
import grails.async.Promise
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.lang.Tuple2
import groovy.sql.GroovyResultSet
import groovy.sql.Sql
import groovy.time.TimeCategory
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import net.bytebuddy.implementation.bytecode.Throw
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.commons.lang3.time.DateUtils
import org.apache.http.util.TextUtils
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.grails.web.json.JSONObject
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.engine.jdbc.internal.BasicFormatterImpl
import org.springframework.jdbc.datasource.SimpleDriverDataSource
import org.springframework.transaction.annotation.Propagation
import org.springframework.util.StringUtils

import java.sql.Clob
import java.sql.ResultSet
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.time.LocalDateTime

import static grails.async.Promises.task

class ReportExecutorService implements LinkHelper, AlertUtil {
    static final String RUNNING_SIGNAL_CFG = 'running_signal_cfg'
    static final String SINGLE_CASE_ALERT = 'Single Case Alert'
    static transactional = false

    def sessionFactory
    def configurationService
    def sqlGenerationService
    def CRUDService
    def emailService
    def queryService
    def templateService
    def messageSource
    def singleCaseAlertService
    def aggregateCaseAlertService
    def reportService
    def springSecurityService
    def userService
    def businessConfigurationService
    def dataSource_pva
    def signalDataSourceService
    def evdasAlertService
    def reportIntegrationService
    def userGroupService
    def spotfireService
    def productBasedSecurityService
    def pvsProductDictionaryService
    def aggregateOnDemandAlertService
    def singleOnDemandAlertService
    private SimpleDriverDataSource reportDataSource
    def alertService
    def statisticsService
    def notificationHelper
    def emailNotificationService
    def dictionaryGroupService

    def dataSource

    def cacheService
    def dataSource_faers
    def dataSource_eudra
    def dataSource_vaers
    def dataSource_vigibase
    def dataSource_jader
    def evdasAlertExecutionService
    def grailsApplication
    def archiveService
    def productAssignmentService
    def dataObjectService
    def validatedSignalService
    def alertAdministrationService
    def skippedAlertService
    def appAlertProgressStatusService
    def alertFieldService
    def dataSheetService
    HazelcastService hazelcastService
    def signalAuditLogService
    def jaderExecutorService
    def aggregateExecutorService
    def importConfigurationService

    List<Long> currentlyRunning = []
    List<Long> currentlyQuantRunning = []
    List<Long> currentlyFaersRunning = []
    List<Long> currentlyVaersRunning = []
    List<Long> currentlyVigibaseRunning = []
    List<Long> currentlyAlertBursting = []

    SimpleDateFormat dateWriteFormat = new SimpleDateFormat('dd-MMM-yyyy')

    static Tuple2<Long, LocalDateTime> currentFaersAlert

    /**
     * Look for and execute source alerts which need to be run
     * @return
     */
    void runConfigurations(String alertType, List<Long> runningIdsList, int threadPoolSize) throws Exception {
        Configuration scheduledConfiguration = Configuration.getNextConfigurationToExecute(runningIdsList,
                Constants.DataSource.PVA, alertType)
        if (scheduledConfiguration && alertService.isPreCheckVerified(AlertType.SINGLE_CASE_ALERT,scheduledConfiguration)) {
            alertAdministrationService.initializePreChecks()
            Map<String, Object> resultMap = alertAdministrationService.isConfigurationReadyForExecution(scheduledConfiguration, true)
            Boolean isReadyForExecution = resultMap.get(Constants.AlertUtils.IS_READY_FOR_EXECUTION)
            ExecutionStatus executionStatus
            Long configId = scheduledConfiguration.id
            if (scheduledConfiguration.isResume) {
                runningIdsList.add(configId)
                Long exConfigId = ExecutionStatus.createCriteria().get(){
                    eq("configId", scheduledConfiguration.id)
                    or{
                        eq("executionStatus",ReportExecutionStatus.SCHEDULED)
                        eq("reportExecutionStatus",ReportExecutionStatus.SCHEDULED)
                        eq("spotfireExecutionStatus",ReportExecutionStatus.SCHEDULED)
                    }
                    order("id", "desc")
                    maxResults(1)
                }.executedConfigId
                executionStatus = ExecutionStatus.findByConfigIdAndExecutedConfigIdAndType(scheduledConfiguration.id,
                        exConfigId, alertType)

                scheduledConfiguration.executing = true
                if (executionStatus?.executionStatus != ReportExecutionStatus.COMPLETED) {
                    executionStatus?.executionStatus = ReportExecutionStatus.GENERATING
                    if (scheduledConfiguration.templateQueries.size()) {
                        executionStatus?.reportExecutionStatus = ReportExecutionStatus.SCHEDULED
                    }
                    if (scheduledConfiguration.spotfireSettings) {
                        executionStatus?.spotfireExecutionStatus = ReportExecutionStatus.SCHEDULED
                    }
                } else {
                    setNextRunDateForConfiguration(scheduledConfiguration)
                }
                CRUDService.updateWithoutAuditLog(executionStatus)
            } else {
                if (isReadyForExecution && (getExecutionQueueSize() < threadPoolSize)) {
                    runningIdsList.add(configId)
                    scheduledConfiguration.executing = true
                    CRUDService.updateWithoutAuditLog(scheduledConfiguration)
                    executionStatus = createExecutionStatus(scheduledConfiguration)
                } else {
                    alertAdministrationService.autoPauseConfigurationAndTriggerNotification(scheduledConfiguration, Constants.AlertConfigType.SINGLE_CASE_ALERT, resultMap.get(Constants.AlertUtils.ALERT_DISABLE_REASON))
                }
            }
            if (isReadyForExecution && (getExecutionQueueSize() <= threadPoolSize)) {
                log.info("Found ${scheduledConfiguration.name} [${configId}] to execute.")
                try {
                    if (scheduledConfiguration.type == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
                        startQualAlertExecutionByLevel(executionStatus)
                    }
                } catch (Throwable th) {
                    if (th.getClass().getName() != ExecutionStatusException.getName()) {
                        th = new ExecutionStatusException(alertService.exceptionString(th))
                    }
                    log.error("Exception in runConfigurations", th)
                    interruptCumulativeThread(configId)
                    handleFailedExecution(th, configId, executionStatus.executedConfigId, alertType, executionStatus.id)
                } finally {
                    runningIdsList.remove(configId)
                }
            }

        }
    }

    void startQualAlertExecutionByLevel(ExecutionStatus executionStatus) throws Exception {
        try {
            Configuration lockedConfiguration = Configuration.get(executionStatus.configId)
            ExecutedConfiguration executedConfiguration
            Configuration configuration = lockedConfiguration
            Boolean caseSeriesGenerated = false
            boolean isCumCaseSeries = false
            boolean isFreshRun = false
            if (executionStatus.executedConfigId) {
                executedConfiguration = ExecutedConfiguration.get(executionStatus.executedConfigId)
                if (executedConfiguration?.adjustmentTypeEnum) {
                    skippedAlertService.updateSkippedAlertsState(executedConfiguration.id, SkippedAlertStateEnum.EXECUTING)
                }
                isCumCaseSeries = isGenerateCumCaseSeries(executedConfiguration, configuration)
            }
            switch (executionStatus.executionLevel) {
                case 0: executedConfiguration = createExecutedConfigurationForAlert(lockedConfiguration)
                    if (configuration.selectedDatasource.contains(Constants.DataSource.PVA) && lockedConfiguration?.adjustmentTypeEnum) {
                        skippedAlertService.updateSkippedAlertsPostExecutedConfigInitialization(lockedConfiguration, executedConfiguration.id)
                        alertAdministrationService.updateConfigPostExecutedConfigInitialization(lockedConfiguration)
                        CRUDService.saveWithoutAuditLog(lockedConfiguration)
                    }
                    updateExecutionStatus(executionStatus, executedConfiguration)
                    isCumCaseSeries = isGenerateCumCaseSeries(executedConfiguration, configuration)
                case 1:
                    caseSeriesGenerated = singleCaseAlertService.generateCaseSeriesForQualAlert(executedConfiguration, configuration, isCumCaseSeries)
                    if (!caseSeriesGenerated) {
                        throw new Exception("Connection to PV Reports failed.")
                    }
                    updateExecutionStatusLevel(executionStatus)
                case 2:
                    if (lockedConfiguration.isResume) {
                        isFreshRun = isEtlSuccessCheck(executionStatus, lockedConfiguration.selectedDatasource)
                        if (isFreshRun) {
                            clearDataMiningTablesOnResume(executedConfiguration.id, lockedConfiguration.selectedDatasource)
                        }
                    }

                    if (isCumCaseSeries) {
                        isCumCaseSeries = false
                        generateCummCaseSeriesInBackground(configuration, executedConfiguration)
                    }
                    fetchSCADataFromMart(lockedConfiguration, executedConfiguration, executionStatus.alertFilePath, executionStatus)
                    updateExecutionStatusLevel(executionStatus)
                case 3:
                    if (lockedConfiguration.isResume && isCumCaseSeries && isFreshRun) {
                        //check is it required to stop clear datamining or not
                        generateCummCaseSeriesInBackground(configuration, executedConfiguration)
                    }
                    saveSCAData(lockedConfiguration, executedConfiguration, executionStatus.alertFilePath, executionStatus)
                    updateExecutionStatusLevel(executionStatus)
                    setValuesForConfiguration(configuration.id, executedConfiguration.id, executionStatus.id)
                    break
                case Constants.Commons.RESUME_REPORT:
                case Constants.Commons.RESUME_SPOTFIRE:
                    try {
                        boolean isReporting = true
                        if (isCumCaseSeries) {
                            isReporting = singleCaseAlertService.generateCummCaseSeries(configuration.id, executedConfiguration.id)
                        }
                        if (isReporting) {
                            if (executionStatus.executionLevel == Constants.Commons.RESUME_REPORT)
                                alertService.generateReport(executedConfiguration)
                            alertService.generateSpotfireReport(executedConfiguration)
                        }
                    } catch (Throwable throwable) {
                        singleCaseAlertService.updateReportStatusForError(executedConfiguration)
                        throw throwable
                    }  finally {
                        lockedConfiguration?.isResume = false
                        lockedConfiguration?.executing = false
                        lockedConfiguration?.save(flush:true)
                    }
            }
            updateLinkedConfigurationForAttachedAlert(lockedConfiguration.id)
        } catch (Throwable throwable) {
            log.error("Exception while running the qualitative alert", throwable)
            ExecutionStatusException ese = new ExecutionStatusException(alertService.exceptionString(throwable))
            throw ese
        }
    }

    boolean isGenerateCumCaseSeries(ExecutedConfiguration executedConfiguration, Configuration configuration) {
        alertService.isCumCaseSeriesReport(executedConfiguration) || alertService.isCumCaseSeriesSpotfire(executedConfiguration) ||
                alertService.isCumForLimitCaseSeries(executedConfiguration, configuration.alertCaseSeriesId != null) ||
                alertService.isCumForLimitCaseSeriesSpotfire(executedConfiguration, configuration.alertCaseSeriesId != null)
    }

    List<Map> getCaseVersionList(ExecutedConfiguration executedConfiguration) {
        SingleCaseAlert.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("caseNumber", "caseNumber")
                property("caseVersion", "versionNumber")
            }
            eq('executedAlertConfiguration.id', executedConfiguration.id)
        } as List<Map>
    }

    void startQuantAlertExecutionByLevel(ExecutionStatus executionStatus) throws Exception {

        Configuration lockedConfiguration = Configuration.get(executionStatus.configId)
        Map<String, Long> otherDataSourcesExecIds = [:]
        Configuration configurationFaers
        Configuration configurationVaers
        Configuration configurationVigibase
        ExecutedEvdasConfiguration executedEvdasConfiguration
        ExecutedConfiguration executedConfiguration
        ExecutedConfiguration executedFaersConfiguration
        ExecutedConfiguration executedVaersConfiguration

        ExecutedConfiguration executedVigibaseConfiguration
        Configuration configuration
        EvdasConfiguration evdasConfiguration
        boolean isExecuteEvdasAlert = true
        boolean isFreshRun = false
        boolean isException = false

        if (executionStatus.executedConfigId) {
            executedConfiguration = ExecutedConfiguration.get(executionStatus.executedConfigId)
            if (executedConfiguration.adjustmentTypeEnum) {
                skippedAlertService.updateSkippedAlertsState(executedConfiguration.id, SkippedAlertStateEnum.EXECUTING)
            }
        }
        try {
            lockedConfiguration.selectedDatasource.split(',').each {

                if (it == Constants.DataSource.EUDRA) {
                    Long prevEvdasConfigObject = EvdasConfiguration.findByIntegratedConfigurationId(lockedConfiguration?.id)?.id

                    if (prevEvdasConfigObject) {
                        evdasConfiguration = EvdasConfiguration.get(prevEvdasConfigObject)
                        evdasConfiguration.dateRangeInformation = evdasAlertExecutionService.createDateRangeForIntegratedReview(lockedConfiguration.productGroupList, DateRangeEnum.CUSTOM, evdasConfiguration?.dateRangeInformation?.id)
                    } else {
                        evdasConfiguration = new EvdasConfiguration()
                        try {
                            evdasConfiguration = evdasAlertExecutionService.createConfigForIntegratedReview(lockedConfiguration, evdasConfiguration)
                        } catch (Exception e) {
                            isExecuteEvdasAlert = false
                            log.error("Custom Date Range not present for EVDAS")
                            e.printStackTrace()
                        }
                    }
                }else{
                    if (it == Constants.DataSource.PVA || (it == Constants.DataSource.FAERS && lockedConfiguration.selectedDatasource.startsWith(Constants.DataSource.FAERS)) || (it == Constants.DataSource.VAERS && lockedConfiguration.selectedDatasource.startsWith(Constants.DataSource.VAERS))
                            || (it == Constants.DataSource.VIGIBASE && lockedConfiguration.selectedDatasource.startsWith(Constants.DataSource.VIGIBASE))) {
                        configuration = lockedConfiguration
                    }else if(it == Constants.DataSource.FAERS){
                        while (getFaersExecutionQueueSize() > 0) {
                            log.info("Waiting for Faers alert to complete")
                            Thread.sleep(960000)
                        }
                        Long prevFaersConfigObject = Configuration.findByIntegratedConfigurationIdAndSelectedDatasource(lockedConfiguration.id, Constants.DataSource.FAERS)?.id
                        if (prevFaersConfigObject) {
                            configurationFaers = Configuration.get(prevFaersConfigObject)
                            Date endDate = configurationFaers.alertDateRangeInformation?.getReportStartAndEndDate()[1] ?: new Date()
                            AlertDateRangeInformation alertDateRangeInformation = configurationFaers.alertDateRangeInformation?.id ? AlertDateRangeInformation.get(configurationFaers.alertDateRangeInformation?.id) : new AlertDateRangeInformation()
                            alertDateRangeInformation.dateRangeEnum = configurationFaers.alertDateRangeInformation?.dateRangeEnum
                            if (!alertDateRangeInformation.alertConfiguration) {
                                alertDateRangeInformation.alertConfiguration = configurationFaers
                            }
                            if (configuration.alertDateRangeInformation?.dateRangeEnum == DateRangeEnum.CUSTOM) {
                                Map dateRange = getFaersDateRange()
                                alertDateRangeInformation.relativeDateRangeValue = configuration.alertDateRangeInformation?.relativeDateRangeValue
                                alertDateRangeInformation.dateRangeStartAbsolute = new Date().parse(DateUtil.DATETIME_FMT, dateRange.startDate+" 00:00:01")
                                alertDateRangeInformation.dateRangeEndAbsolute = new Date().parse(DateUtil.DATETIME_FMT, dateRange.endDate+" 00:00:01")
                            } else {
                                alertDateRangeInformation.relativeDateRangeValue = configuration.alertDateRangeInformation?.relativeDateRangeValue
                                alertDateRangeInformation.dateRangeEndAbsolute = endDate
                                alertDateRangeInformation.dateRangeStartAbsolute = new Date().parse(DateUtil.DATETIME_FMT, "01-01-1900 00:00:01")
                            }
                            configurationFaers.alertDateRangeInformation = alertDateRangeInformation
                        } else {
                            configurationFaers = new Configuration()
                            configurationFaers = createConfigForIntegratedReview(lockedConfiguration, configurationFaers)
                        }
                        currentlyFaersRunning.add(configurationFaers.id)
                        configuration = (it == Constants.DataSource.FAERS && lockedConfiguration.selectedDatasource.startsWith(Constants.DataSource.FAERS)) ? configurationFaers : lockedConfiguration
                    } else if(it == Constants.DataSource.VAERS) {
                        Long prevVaersConfigObject = Configuration.findByIntegratedConfigurationIdAndSelectedDatasource(lockedConfiguration.id, Constants.DataSource.VAERS)?.id
                        if (prevVaersConfigObject) {
                            configurationVaers = Configuration.get(prevVaersConfigObject)
                        } else {
                            configurationVaers = new Configuration()
                            configurationVaers = createConfigForIntegratedReviewVaers(lockedConfiguration, configurationVaers)
                        }
                        currentlyVaersRunning.add(configurationVaers.id)
                        configuration = (it == Constants.DataSource.VAERS && lockedConfiguration.selectedDatasource.startsWith(Constants.DataSource.VAERS)) ? configurationVaers : lockedConfiguration
                    } else if(it == Constants.DataSource.VIGIBASE) {
                        Long prevVigibaseConfigObject = Configuration.findByIntegratedConfigurationIdAndSelectedDatasource(lockedConfiguration.id, Constants.DataSource.VIGIBASE)?.id
                        if (prevVigibaseConfigObject) {
                            configurationVigibase = Configuration.get(prevVigibaseConfigObject)
                        } else {
                            configurationVigibase = new Configuration()
                            configurationVigibase = createConfigForIntegratedReviewVigibase(lockedConfiguration, configurationVigibase)
                        }
                        currentlyVigibaseRunning.add(configurationVigibase.id)
                        configuration = (it == Constants.DataSource.VIGIBASE && lockedConfiguration.selectedDatasource.startsWith(Constants.DataSource.VIGIBASE)) ? configurationVigibase : lockedConfiguration
                    }
                }

            }

            switch (executionStatus.executionLevel) {
                case 0:
                    lockedConfiguration.selectedDatasource.split(',').each {
                        if (it == Constants.DataSource.EUDRA) {
                            try {
                                if (isExecuteEvdasAlert) {
                                    executedEvdasConfiguration = evdasAlertExecutionService.createExecutedConfiguration(evdasConfiguration)
                                    otherDataSourcesExecIds.put(it, executedEvdasConfiguration.id)
                                    List<Date> dateRange = executedEvdasConfiguration?.dateRangeInformation?.getReportStartAndEndDate()
                                    if (executedEvdasConfiguration.dateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE) {
                                        Date dateRangeEnd = executedEvdasConfiguration?.dateCreated
                                        String evdasDateRange = DateUtil.toDateString1(dateRange[0]) + " - " + DateUtil.toDateString1(dateRangeEnd)
                                        executedConfiguration.evdasDateRange = evdasDateRange
                                    } else {
                                        String evdasDateRange = DateUtil.toDateString1(dateRange[0]) + " - " + DateUtil.toDateString1(dateRange[1])
                                        executedConfiguration.evdasDateRange = evdasDateRange
                                    }
                                }
                            } catch (Exception e) {
                                isExecuteEvdasAlert = false
                                log.error("Custom Date Range not present for EVDAS")
                                e.printStackTrace()
                            }

                        } else {
                            if (it == Constants.DataSource.PVA || (it == Constants.DataSource.FAERS && lockedConfiguration.selectedDatasource.startsWith(Constants.DataSource.FAERS)) || (it == Constants.DataSource.VAERS && lockedConfiguration.selectedDatasource.startsWith(Constants.DataSource.VAERS))
                                    || (it == Constants.DataSource.VIGIBASE && lockedConfiguration.selectedDatasource.startsWith(Constants.DataSource.VIGIBASE))) {
                                executedConfiguration = createExecutedConfigurationForAlert(configuration)
                                if (configuration.selectedDatasource.contains(Constants.DataSource.PVA) && lockedConfiguration.adjustmentTypeEnum) {
                                    skippedAlertService.updateSkippedAlertsPostExecutedConfigInitialization(lockedConfiguration, executedConfiguration.id)
                                    alertAdministrationService.updateConfigPostExecutedConfigInitialization(lockedConfiguration)
                                    CRUDService.saveWithoutAuditLog(lockedConfiguration)
                                }
                                otherDataSourcesExecIds.put(it, executedConfiguration.id)
                            }else if(it == Constants.DataSource.FAERS){
                                executedFaersConfiguration = createExecutedConfigurationForAlert(configurationFaers)
                                otherDataSourcesExecIds.put(it, executedFaersConfiguration.id)
                            } else if(it == Constants.DataSource.VAERS) {
                                executedVaersConfiguration = createExecutedConfigurationForAlert(configurationVaers)
                                otherDataSourcesExecIds.put(it, executedVaersConfiguration.id)
                            } else if(it == Constants.DataSource.VIGIBASE) {
                                executedVigibaseConfiguration = createExecutedConfigurationForAlert(configurationVigibase)
                                otherDataSourcesExecIds.put(it, executedVigibaseConfiguration.id)
                            }
                        }
                    }
                    updateExecutionStatus(executionStatus, executedConfiguration)
                case 1:
                    lockedConfiguration.selectedDatasource.split(',').each {
                        List alertData = []
                        if (it == Constants.DataSource.EUDRA) {
                            if (isExecuteEvdasAlert) {
                                appAlertProgressStatusService.createAppAlertProgressStatus(executionStatus, executedEvdasConfiguration?.id, it, 2, 2, System.currentTimeMillis(), AlertProgressExecutionType.DATASOURCE)
                                alertData = evdasAlertExecutionService.fetchEvdasAlertDataFromMart(evdasConfiguration, executedEvdasConfiguration, executionStatus.alertFilePath, executedEvdasConfiguration?.id)
                                String fileName = "${grailsApplication.config.signal.alert.file}/${executedConfiguration.id}_${executedConfiguration.type}_${it}"
                                alertService.saveAlertDataInFile(alertData, fileName)
                                appAlertProgressStatusService.updateAppAlertProgressStatus(executionStatus.id, executedEvdasConfiguration?.id, it, 3, 3, System.currentTimeMillis())
                            }
                        } else {
                            if (it == Constants.DataSource.PVA || (it == Constants.DataSource.FAERS && lockedConfiguration.selectedDatasource.startsWith(Constants.DataSource.FAERS)) || (it == Constants.DataSource.VAERS && lockedConfiguration.selectedDatasource.startsWith(Constants.DataSource.VAERS))
                                    || (it == Constants.DataSource.VIGIBASE && lockedConfiguration.selectedDatasource.startsWith(Constants.DataSource.VIGIBASE))) {
                                if(configuration.isResume) {
                                    if (isDataSourceRequiredForFreshRun(executionStatus, it)) {
                                        clearDataMiningTablesOnResume(executedConfiguration.id, it)
                                    }
                                }
                                alertData = fetchQuantAlertDataFromMart(executionStatus, configuration, executedConfiguration, it)
                            }else {
                                if(!executedFaersConfiguration && it != Constants.DataSource.VAERS && it != Constants.DataSource.VIGIBASE) {
                                    Configuration prevFaersConfig = Configuration.findByIntegratedConfigurationIdAndSelectedDatasource(lockedConfiguration.id, Constants.DataSource.FAERS)
                                    Long exFaersConfigId = ExecutedConfiguration.createCriteria().get {
                                        eq('configId' , prevFaersConfig?.id)
                                        projections { max "id" }
                                    } as Long
                                    if (exFaersConfigId) {
                                        executedFaersConfiguration = ExecutedConfiguration.get(exFaersConfigId)
                                    }
                                }
                                if(!executedVaersConfiguration && it != Constants.DataSource.FAERS && it != Constants.DataSource.VIGIBASE) {
                                    Configuration prevVaersConfig = Configuration.findByIntegratedConfigurationIdAndSelectedDatasource(lockedConfiguration.id, Constants.DataSource.VAERS)
                                    Long exVaersConfigId = ExecutedConfiguration.createCriteria().get {
                                        eq('configId' , prevVaersConfig?.id)
                                        projections { max "id" }
                                    } as Long
                                    if (exVaersConfigId) {
                                        executedVaersConfiguration = ExecutedConfiguration.get(exVaersConfigId)
                                    }
                                }
                                if(!executedVigibaseConfiguration && it != Constants.DataSource.VAERS && it != Constants.DataSource.FAERS) {
                                    Configuration prevVigibaseConfig = Configuration.findByIntegratedConfigurationIdAndSelectedDatasource(lockedConfiguration.id, Constants.DataSource.VIGIBASE)
                                    Long exVigibaseConfigId = ExecutedConfiguration.createCriteria().get {
                                        eq('configId' , prevVigibaseConfig?.id)
                                        projections { max "id" }
                                    } as Long
                                    if (exVigibaseConfigId) {
                                        executedVigibaseConfiguration = ExecutedConfiguration.get(exVigibaseConfigId)
                                    }
                                }
                                if(executedFaersConfiguration && !executedVaersConfiguration && it == Constants.DataSource.FAERS) {
                                    if (configuration.isResume) {
                                        if (isDataSourceRequiredForFreshRun(executionStatus, it)) {
                                            clearDataMiningTablesOnResume(executedFaersConfiguration.id, it)
                                        }
                                    }
                                    alertData = fetchQuantAlertDataFromMart(executionStatus, configurationFaers, executedFaersConfiguration, it)
                                }
                                if(executedVaersConfiguration && !executedFaersConfiguration && it == Constants.DataSource.VAERS) {
                                    if (configuration.isResume) {
                                        if (isDataSourceRequiredForFreshRun(executionStatus, it)) {
                                            clearDataMiningTablesOnResume(executedVaersConfiguration.id, it)
                                        }
                                    }
                                    alertData = fetchQuantAlertDataFromMart(executionStatus, configurationVaers, executedVaersConfiguration, it, true)
                                }
                                if(executedVigibaseConfiguration && it == Constants.DataSource.VIGIBASE) {
                                    if (configuration.isResume) {
                                        if (isDataSourceRequiredForFreshRun(executionStatus, it)) {
                                            clearDataMiningTablesOnResume(executedVigibaseConfiguration.id, it)
                                        }
                                    }
                                    alertData = fetchQuantAlertDataFromMart(executionStatus, configurationVigibase, executedVigibaseConfiguration, it, false, true)
                                }
                            }
                            String fileName = "${grailsApplication.config.signal.alert.file}/${executedConfiguration.id}_${executedConfiguration.type}"
                            if(!lockedConfiguration.adhocRun)
                                fileName = fileName + "_${it}"
                            alertService.saveAlertDataInFile(alertData, fileName)
                            log.info("after finishing alert data in file.")
                        }
                    }
                    log.info("before updating log level "+executionStatus.executionLevel)
                    updateExecutionStatusLevel(executionStatus)
                case 2:
                    lockedConfiguration.selectedDatasource.split(',').each {
                        if (it == Constants.DataSource.EUDRA) {
                            if (isExecuteEvdasAlert) {
                                Long exEvdasConfigId
                                if (!executedEvdasConfiguration) {
                                    exEvdasConfigId = ExecutedEvdasConfiguration.createCriteria().get {
                                        eq('configId', evdasConfiguration?.id)
                                        projections { max "id" }
                                    } as Long
                                }
                                if (exEvdasConfigId) {
                                    otherDataSourcesExecIds.put(it, exEvdasConfigId)
                                }
                            }
                        } else {
                            if (it == Constants.DataSource.PVA || (it == Constants.DataSource.FAERS && lockedConfiguration.selectedDatasource.startsWith(Constants.DataSource.FAERS)) || (it == Constants.DataSource.VAERS && lockedConfiguration.selectedDatasource.startsWith(Constants.DataSource.VAERS))
                                    || (it == Constants.DataSource.VIGIBASE && lockedConfiguration.selectedDatasource.startsWith(Constants.DataSource.VIGIBASE))) {
                                if (!executedConfiguration) {
                                    executedConfiguration = ExecutedConfiguration.get(executionStatus.executedConfigId)
                                }
                                if (executedConfiguration)
                                    otherDataSourcesExecIds.put(it, executedConfiguration.id)
                            }else{
                                if(!executedFaersConfiguration && it != Constants.DataSource.VAERS && it != Constants.DataSource.VIGIBASE) {
                                    Configuration prevFaersConfig = Configuration.findByIntegratedConfigurationIdAndSelectedDatasource(lockedConfiguration.id, Constants.DataSource.FAERS)
                                    Long exFaersConfigId = ExecutedConfiguration.createCriteria().get {
                                        eq('configId' , prevFaersConfig?.id)
                                        projections { max "id" }
                                    } as Long
                                    if (exFaersConfigId) {
                                        executedFaersConfiguration = ExecutedConfiguration.get(exFaersConfigId)
                                    }
                                }
                                if(!executedVaersConfiguration && it != Constants.DataSource.FAERS && it != Constants.DataSource.VIGIBASE) {
                                    Configuration prevVaersConfig = Configuration.findByIntegratedConfigurationIdAndSelectedDatasource(lockedConfiguration.id, Constants.DataSource.VAERS)
                                    Long exVaersConfigId = ExecutedConfiguration.createCriteria().get {
                                        eq('configId' , prevVaersConfig?.id)
                                        projections { max "id" }
                                    } as Long
                                    if (exVaersConfigId) {
                                        executedVaersConfiguration = ExecutedConfiguration.get(exVaersConfigId)
                                    }
                                }
                                if(!executedVigibaseConfiguration && it != Constants.DataSource.FAERS && it != Constants.DataSource.VAERS) {
                                    Configuration prevVigibaseConfig = Configuration.findByIntegratedConfigurationIdAndSelectedDatasource(lockedConfiguration.id, Constants.DataSource.VIGIBASE)
                                    Long exVigibaseConfigId = ExecutedConfiguration.createCriteria().get {
                                        eq('configId' , prevVigibaseConfig?.id)
                                        projections { max "id" }
                                    } as Long
                                    if (exVigibaseConfigId) {
                                        executedVigibaseConfiguration = ExecutedConfiguration.get(exVigibaseConfigId)
                                    }
                                }
                                if(executedFaersConfiguration && it == Constants.DataSource.FAERS)
                                    otherDataSourcesExecIds.put(it, executedFaersConfiguration.id)
                                if(executedVaersConfiguration && it == Constants.DataSource.VAERS)
                                    otherDataSourcesExecIds.put(it, executedVaersConfiguration.id)
                                if(executedVigibaseConfiguration && it == Constants.DataSource.VIGIBASE)
                                    otherDataSourcesExecIds.put(it, executedVigibaseConfiguration.id)

                            }
                        }
                    }
            }

            switch (executionStatus.executionLevel) {
                case 2:
                    saveQuantData(lockedConfiguration, executedConfiguration, executionStatus.alertFilePath, otherDataSourcesExecIds, executionStatus)
                    setValuesForConfiguration(configuration.id, executedConfiguration.id, executionStatus.id)
                    alertService.persistShareWithToExecutedConfig(executedConfiguration)
                    break
                case Constants.Commons.RESUME_REPORT:
                case Constants.Commons.RESUME_SPOTFIRE:
                    try {
                        boolean isReportingCompleted
                        if (executedConfiguration.pvrCaseSeriesId) {
                            isReportingCompleted = aggregateCaseAlertService.generateCaseSeriesForQuantAlert(executedConfiguration, false)
                        }
                        if (executedConfiguration.pvrCumulativeCaseSeriesId && executedConfiguration.pvrCaseSeriesId != executedConfiguration.pvrCumulativeCaseSeriesId) {
                            isReportingCompleted = aggregateCaseAlertService.generateCaseSeriesForQuantAlert(executedConfiguration, true)
                        }

                        if (executedConfiguration.faersCaseSeriesId) {
                            isReportingCompleted = aggregateCaseAlertService.generateCaseSeriesForFaersAlert(executedConfiguration, executedFaersConfiguration?.id, false)
                        }

                        if (executedConfiguration.faersCumCaseSeriesId) {
                            isReportingCompleted = aggregateCaseAlertService.generateCaseSeriesForFaersAlert(executedConfiguration, executedFaersConfiguration?.id, true)
                        }

                        if (executedConfiguration.vigibaseCaseSeriesId) {
                            isReportingCompleted = aggregateCaseAlertService.generateCaseSeriesForVigibaseAlert(executedConfiguration, executedVigibaseConfiguration?.id, false)
                        }

                        if (executedConfiguration.vigibaseCumCaseSeriesId) {
                            isReportingCompleted = aggregateCaseAlertService.generateCaseSeriesForVigibaseAlert(executedConfiguration, executedVigibaseConfiguration?.id, true)
                        }

                        if (executedConfiguration.vaersCaseSeriesId) {
                            isReportingCompleted = aggregateCaseAlertService.generateCaseSeriesForVaersAlert(executedConfiguration, executedVaersConfiguration?.id, false)
                        }

                        if (executedConfiguration.vaersCumCaseSeriesId) {
                            isReportingCompleted = aggregateCaseAlertService.generateCaseSeriesForVaersAlert(executedConfiguration, executedVaersConfiguration?.id, true)
                        }
                        if (isReportingCompleted) {
                            if (executionStatus.executionLevel == Constants.Commons.RESUME_REPORT)
                                alertService.generateReport(executedConfiguration)
                            alertService.generateSpotfireReport(executedConfiguration, executedFaersConfiguration)
                        }
                    } catch (Throwable throwable) {
                        singleCaseAlertService.updateReportStatusForError(executedConfiguration)
                        throw throwable
                    } finally {
                        lockedConfiguration?.isResume = false
                        lockedConfiguration?.executing = false
                        lockedConfiguration?.save(flush:true)
                    }
            }
        } catch (Throwable throwable) {
            log.error("Exception while running the quantitative alert", throwable)
            isException = true
            throw throwable
        } finally {
            if (configurationFaers && (currentlyFaersRunning.contains(configurationFaers.id) || executedFaersConfiguration)) {
                currentlyFaersRunning.remove(configurationFaers.id)
                if (!isException) {
                    clearDataMiningTables(configurationFaers.id, executedFaersConfiguration.id, Constants.DataSource.FAERS)
                }
            }
            if(configurationVaers && (currentlyVaersRunning.contains(configurationVaers.id) || executedVaersConfiguration)){
                currentlyVaersRunning.remove(configurationVaers.id)
                if (!isException) {
                    clearDataMiningTables(configurationVaers.id, executedVaersConfiguration.id, Constants.DataSource.VAERS)
                }
           }
           if(configurationVigibase && (currentlyVigibaseRunning.contains(configurationVigibase.id) || executedVigibaseConfiguration)){
                currentlyVigibaseRunning.remove(configurationVigibase.id)
               if (!isException) {
                   clearDataMiningTables(configurationVigibase.id, executedVigibaseConfiguration.id, Constants.DataSource.VIGIBASE)
               }
           }
        }
    }

    void startAlertBurstingExecutionByLevels(ExecutionStatus executionStatus) throws Exception {
        Configuration lockedConfiguration = Configuration.get(executionStatus.configId)
        ExecutedConfiguration executedConfiguration = null
        Configuration configuration = lockedConfiguration
        try {
            switch (executionStatus.executionLevel) {
                case 0:
                    executedConfiguration = createExecutedConfigurationForAlert(configuration)
                    if (configuration.selectedDatasource.contains(Constants.DataSource.PVA) && lockedConfiguration.adjustmentTypeEnum) {
                        skippedAlertService.updateSkippedAlertsPostExecutedConfigInitialization(lockedConfiguration, executedConfiguration.id)
                        alertAdministrationService.updateConfigPostExecutedConfigInitialization(lockedConfiguration)
                        CRUDService.saveWithoutAuditLog(lockedConfiguration)
                    }
                    updateExecutionStatus(executionStatus, executedConfiguration)
                case 1:
                    createConfigurationsAndExConfigurationsBasedOnPVA(configuration, executedConfiguration)
                    updateExecutionStatus(executionStatus, executedConfiguration)
            }
            switch (executionStatus.executionLevel) {
                case 2:
                    aggregateExecutorService.processAlertExecutionAndData(executedConfiguration)
                    setValuesForConfiguration(configuration.id, executedConfiguration.id, executionStatus.id)
                    break
            }
        } catch (Throwable throwable) {
            log.error("Exception while Alert Bursting.", throwable)
            throw throwable
        } finally {

        }
    }


    void runConfigurationsIntegratedReview(String alertType, String dataSource, List<Long> runningIdsList,int threadPoolSize) throws Exception {
        Configuration scheduledConfiguration = null
        if (dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM)) {
//            Long configId = aggregateExecutorService.configurationQueue.poll()
//            scheduledConfiguration = Configuration.get(configId)
            scheduledConfiguration = Configuration.getNextConfigurationToExecute(runningIdsList, dataSource, alertType)
        } else {
            scheduledConfiguration = Configuration.getNextConfigurationToExecute(runningIdsList, dataSource, alertType)
        }
        if (scheduledConfiguration && alertService.isPreCheckVerified(AlertType.AGGREGATE_CASE_ALERT,scheduledConfiguration)) {
            boolean isSafetyAlert = (dataSource == Constants.DataSource.PVA) ? true : false
            alertAdministrationService.initializePreChecks()
            Map<String, Object> resultMap = alertAdministrationService.isConfigurationReadyForExecution(scheduledConfiguration, isSafetyAlert)
            Boolean isReadyForExecution = resultMap.get(Constants.AlertUtils.IS_READY_FOR_EXECUTION)
            if (isReadyForExecution && (runningIdsList.size() < threadPoolSize)) {
                startIntegratedReviewExecution(alertType, scheduledConfiguration, runningIdsList)
            } else {
                alertAdministrationService.autoPauseConfigurationAndTriggerNotification(scheduledConfiguration, Constants.AlertConfigType.AGGREGATE_CASE_ALERT, resultMap.get(Constants.AlertUtils.ALERT_DISABLE_REASON))
            }
            updateLinkedConfigurationForAttachedAlert(scheduledConfiguration.id)
        }
    }
    void runConfigurationsAlertBursting(String alertType, String dataSource, List<Long> runningIdsList,int threadPoolSize) throws Exception {
        Configuration scheduledConfiguration = Configuration.getNextAlertBurstingConfigurationToExecute(runningIdsList, dataSource, alertType)
        if (scheduledConfiguration && alertService.isPreCheckVerified(AlertType.AGGREGATE_CASE_ALERT,scheduledConfiguration)) {
            boolean isSafetyAlert = (dataSource == Constants.DataSource.PVA) ? true : false
            alertAdministrationService.initializePreChecks()
            Map<String, Object> resultMap = alertAdministrationService.isConfigurationReadyForExecution(scheduledConfiguration, isSafetyAlert)
            Boolean isReadyForExecution = resultMap.get(Constants.AlertUtils.IS_READY_FOR_EXECUTION)
            if (isReadyForExecution && (runningIdsList.size() < threadPoolSize)) {
                startAlertBurstingExecution(alertType, scheduledConfiguration, runningIdsList)
            } else {
                alertAdministrationService.autoPauseConfigurationAndTriggerNotification(scheduledConfiguration, Constants.AlertConfigType.AGGREGATE_CASE_ALERT, resultMap.get(Constants.AlertUtils.ALERT_DISABLE_REASON))
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    def setValuesForConfiguration(Long configId, Long execConfigId, Long executionStatusId) {
        Configuration lockedConfiguration
        ExecutedConfiguration executedConfiguration
        ExecutionStatus executionStatus
        try {
            lockedConfiguration = Configuration.lock(configId)
            executedConfiguration = ExecutedConfiguration.get(execConfigId)
            executionStatus = ExecutionStatus.get(executionStatusId)
            if (executionStatus) {

                executionStatus.endTime = System.currentTimeMillis()
                setTotalExecutionTimeForConfiguration(lockedConfiguration, (executionStatus.endTime - executionStatus.startTime))
                setNextRunDateForConfiguration(lockedConfiguration)
                lockedConfiguration.executing = false


                if (executionStatus?.executionStatus != ReportExecutionStatus.ERROR) {
                    executionStatus?.executionStatus = ReportExecutionStatus.COMPLETED
                    lockedConfiguration.isResume = false
                }
                Date currentAsOfVersionDate = lockedConfiguration.asOfVersionDate?.clone()
                if (!lockedConfiguration.adhocRun) {
                    adjustCustomDateRanges(lockedConfiguration)
                    adjustAsOfVersion(lockedConfiguration, executedConfiguration)
                }
                if (lockedConfiguration.selectedDatasource.contains(Constants.DataSource.PVA) && executedConfiguration?.adjustmentTypeEnum) {
                    alertAdministrationService.updateConfigPostAlertExecution(lockedConfiguration, executedConfiguration)
                }

                if (lockedConfiguration?.futureScheduleDateJSON) {
                    if (lockedConfiguration.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF) {
                        lockedConfiguration.setAsOfVersionDate(getAdjustAsOfVersionForFutureScheduleAlert(lockedConfiguration, currentAsOfVersionDate))
                    }
                    lockedConfiguration.scheduleDateJSON = lockedConfiguration.futureScheduleDateJSON
                    lockedConfiguration.setNextRunDate(configurationService.getNextDate(lockedConfiguration))
                    lockedConfiguration.setFutureScheduleDateJSON(null)
                }

                if (executedConfiguration) {
                    String alertConfigType = executedConfiguration.type
                    boolean isAdhoc = executedConfiguration.adhocRun
                    if (!(alertConfigType == Constants.AlertConfigType.SINGLE_CASE_ALERT && !isAdhoc && alertService.getTotalCountsForExecConfig(executedConfiguration.dispCounts) == 0)) {
                        addNotification(executedConfiguration, executionStatus, executedConfiguration.assignedTo, executedConfiguration.assignedToGroup)
                        emailNotificationService.emailHanlderAtAlertLevel(executedConfiguration, executionStatus)
                    }else if(alertService.getTotalCountsForExecConfig(executedConfiguration.dispCounts) == 0){
                        createAuditForExecution(executedConfiguration, "app.notification.completed", executedConfiguration.type == Constants.AlertConfigType.SINGLE_CASE_ALERT ? Constants.AlertConfigType.INDIVIDUAL_CASE_CONFIGURATIONS : Constants.AlertConfigType.AGGREGATE_CASE_CONFIGURATIONS)
                    }
                }
            }
        } catch (Throwable th) {
            log.error("Error happened when handling Successful Configurations [${lockedConfiguration.id}]", th)
            throw th
        } finally {
            if (lockedConfiguration) {
                CRUDService.updateWithAuditLog(lockedConfiguration)
            }
            if (executedConfiguration) {
                executedConfiguration.save(flush: true)
            }
            if (executionStatus) {
                executionStatus.save(flush: true)
            }
        }
    }

    def setValuesForChildConfiguration(Configuration lockedConfiguration, ExecutedConfiguration executedConfiguration) {
        try {
            if (executedConfiguration) {
                setNextRunDateForConfiguration(lockedConfiguration)
                lockedConfiguration.executing = false
                Date currentAsOfVersionDate = lockedConfiguration.asOfVersionDate?.clone()
                if (!lockedConfiguration.adhocRun) {
                    adjustCustomDateRanges(lockedConfiguration)
                    adjustAsOfVersion(lockedConfiguration, executedConfiguration)
                }
                if (lockedConfiguration.selectedDatasource.contains(Constants.DataSource.PVA) && executedConfiguration?.adjustmentTypeEnum) {
                    alertAdministrationService.updateConfigPostAlertExecution(lockedConfiguration, executedConfiguration)
                }

                if (lockedConfiguration?.futureScheduleDateJSON) {
                    if (lockedConfiguration.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF) {
                        lockedConfiguration.setAsOfVersionDate(getAdjustAsOfVersionForFutureScheduleAlert(lockedConfiguration, currentAsOfVersionDate))
                    }
                    lockedConfiguration.scheduleDateJSON = lockedConfiguration.futureScheduleDateJSON
                    lockedConfiguration.setNextRunDate(configurationService.getNextDate(lockedConfiguration))
                    lockedConfiguration.setFutureScheduleDateJSON(null)
                }
            }
        } catch (Throwable th) {
            log.error("Error happened when handling Successful Configurations [${lockedConfiguration.id}]", th)
            throw th
        } finally {
//            if (lockedConfiguration) {
//                CRUDService.updateWithAuditLog(lockedConfiguration)
//            }
//            if (executedConfiguration) {
//                executedConfiguration.save(flush: true)
//            }
        }
    }

    ExecutionStatus createExecutionStatus(Configuration scheduledConfiguration) {
        log.info("Creating the execution status.")
        Configuration lockedConfiguration = Configuration.lock(scheduledConfiguration.id)
        ExecutionStatus executionStatus = new ExecutionStatus(
                configId: lockedConfiguration.id,
                reportVersion: lockedConfiguration.numOfExecutions + 1,
                startTime: System.currentTimeMillis(),
                nextRunDate: lockedConfiguration.nextRunDate,
                owner: lockedConfiguration.owner,
                name: lockedConfiguration.name,
                executionStatus: ReportExecutionStatus.GENERATING,
                type: lockedConfiguration.type,
                nodeName: hazelcastService.getName())
        executionStatus.frequency = configurationService.calculateFrequency(lockedConfiguration)
        if (lockedConfiguration.templateQueries.size()) {
            executionStatus.reportExecutionStatus = ReportExecutionStatus.SCHEDULED
        }
        if (lockedConfiguration.spotfireSettings) {
            executionStatus.spotfireExecutionStatus = ReportExecutionStatus.SCHEDULED
        }
        log.info("Saving the execution status.")
        executionStatus.save(flush: true)
        log.info("Execution status related flow complete.")
        executionStatus
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    def handleFailedExecution(ExecutionStatusException ese,
                              Long configId,
                              Long executedConfigId, String alertType, Long execStatusId = null) {
        try {
            Configuration config = Configuration.get(configId)
            ExecutedConfiguration executedConfiguration = null
            if (executedConfigId) {
                executedConfiguration = ExecutedConfiguration.get(executedConfigId)
                executedConfiguration.isEnabled = false

                executedConfiguration.save(flush: true)
                alertService.dataCleanUpForFailedExecution(executedConfiguration,config)
            }
            ExecutionStatus executionStatus = null
            if (executedConfigId) {
                executionStatus = ExecutionStatus.findByExecutedConfigIdAndType(executedConfigId, alertType)
            } else if (execStatusId) {
                executionStatus = ExecutionStatus.get(execStatusId)
            }
            if (executionStatus) {
                executionStatus.stackTrace = ese?.errorCause?.length() > 32000 ? ese?.errorCause?.substring(0,32000) : ese?.errorCause
                if (executionStatus.executionStatus != ReportExecutionStatus.COMPLETED)
                    executionStatus.executionStatus = ReportExecutionStatus.ERROR
                if (config.templateQueries && executionStatus.reportExecutionStatus != ReportExecutionStatus.COMPLETED) {
                    executionStatus.reportExecutionStatus = ReportExecutionStatus.ERROR
                }
                if (config.spotfireSettings && executionStatus.spotfireExecutionStatus != ReportExecutionStatus.COMPLETED) {
                    executionStatus.spotfireExecutionStatus = ReportExecutionStatus.ERROR
                }
                /*if (alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT && executionStatus.executionLevel == 1) {
                    executionStatus.executionLevel = 0
                }*/
                executionStatus.save(flush: true)

                setNextRunDateForConfiguration(config)

                if (config.adhocRun) {
                    config.isEnabled = false
                    config.setNextRunDate(null)
                }

                if (executedConfiguration && executedConfiguration?.adjustmentTypeEnum) {
                    log.info("Handling failed auto-adjusted execution...")
                    alertAdministrationService.handleFailedSkippedConfiguration(config, executedConfiguration)
                }

                config.isResume = false
                config.executing = false
                CRUDService.updateWithoutAuditLog(config)

                if (executedConfiguration) {
                    //After that set the notification.
                    addNotification(executedConfiguration, executionStatus, executedConfiguration.assignedTo, executedConfiguration.assignedToGroup)
                    emailNotificationService.emailHanlderAtAlertLevel(executedConfiguration, executionStatus)
                }
            } else {
                log.error("Cannot find the execution status. [handleFailedExecution]")
            }
        } catch (Throwable throwable) {
            log.error("Error happened when handling failed Configurations [${executedConfigId}]", throwable)
        }
    }

    int getExecutionQueueSize() {
        return currentlyRunning.size()
    }

    int getFaersExecutionQueueSize() {
        return currentlyFaersRunning.size()
    }

    int getVaersExecutionQueueSize() {
        return currentlyVaersRunning.size()
    }

    int getVigibaseExecutionQueueSize() {
        return currentlyVigibaseRunning.size()
    }

    int getQuantExecutionSize() {
        return currentlyQuantRunning.size()
    }

    int getAlertBurstingQueueSize(){
        return currentlyAlertBursting.size()
    }

    void executeReportJob(Configuration configuration, Closure successCallback, Closure failureCallback) {
        if (configuration.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            executeAlertJobQuantitative(configuration, successCallback, failureCallback)
        } else {
            executeAlertJobQualitative(configuration, true, successCallback, failureCallback)
        }
    }

    void executeAlertJobQualitative(Configuration configuration, boolean isAutoTrigger,
                                    Closure successCallback, Closure failureCallback) {

        ExecutedConfiguration executedConfiguration = null

        String sectionName = ""
        configuration.refresh()
        Configuration lockedConfiguration = Configuration.lock(configuration.id)
        Long templateId = lockedConfiguration.template.id
        Long queryId = lockedConfiguration.alertQueryId

        if (lockedConfiguration.isEnabled) {
            log.info("Now Executing the alert : " + lockedConfiguration.name)
            //Executed Configuration object is created.
            try {
                executedConfiguration = createExecutedConfiguration(lockedConfiguration)
            } catch (Throwable exception) {
                log.error("Error happened when creating executed configuration " +
                        "for ${lockedConfiguration.name}. Skipping", exception)
                CRUDService.updateWithAuditLog(lockedConfiguration)
                ExecutionStatusException ese = new ExecutionStatusException(alertService.exceptionString(exception))
                failureCallback(lockedConfiguration, executedConfiguration, ese)
                return
            }
            List<Map> alertData = []
            //retrieve alert data from dataSource
            SuperQueryDTO superQueryDTO = queryService.queryDetail(configuration.alertQueryId)

            try {

                Long timeBeforeDB = System.currentTimeMillis()
                alertData = generateAlertResultQualitative(configuration, executedConfiguration, superQueryDTO, false)
                def timeAfterDB = System.currentTimeMillis()
                log.info(((timeAfterDB - timeBeforeDB) / 1000) +
                        " Secs were taken in fetching the data from database for configuration " +
                        executedConfiguration.name)
            } catch (Throwable exception) {
                log.error("Error when getPersistedReportResult", exception)
                CRUDService.updateWithAuditLog(lockedConfiguration)
                clearDataMiningTables(lockedConfiguration.id, executedConfiguration.id)
                ExecutionStatusException ese = new ExecutionStatusException(alertService.exceptionString(exception))
                failureCallback(lockedConfiguration.id, executedConfiguration.id, ese)
                return
            }

            List caseVersionList = []

            // For each of the result, we generate the corresponding alert if the template is for alert.
            List<Map> filteredAlertData = singleCaseAlertService.filterCases(lockedConfiguration, alertData)
            alertData = null
            try {
                CRUDService.updateWithAuditLog(lockedConfiguration) // unlock
                if (configuration.adhocRun) {
                    caseVersionList = singleOnDemandAlertService.createAlert(lockedConfiguration, executedConfiguration, filteredAlertData)
                } else {
                    caseVersionList = singleCaseAlertService.createAlert(lockedConfiguration, executedConfiguration,
                            filteredAlertData, Constants.Commons.BLANK_STRING, false, false)
                }
                filteredAlertData = null
                if (isAutoTrigger) {
                    if (caseVersionList.size() > 0) {
                        triggerAlertAtThreshold(configuration, executedConfiguration, caseVersionList)
                    }
                }
                caseVersionList = null
                successCallback(lockedConfiguration.id, executedConfiguration.id)
            } catch (Throwable exception) {
                exception.printStackTrace()
                ExecutionStatusException ese = new ExecutionStatusException(alertService.exceptionString(exception))
                failureCallback(lockedConfiguration.id, executedConfiguration.id, ese)
            }

        } else {
            log.info("${lockedConfiguration.name} is not enabled. Skipping")
        }
    }

    /**
     * Execute the report. For now we're setting a fake result and setting the next runRunDate to the next day.
     * If the scheduleDateJSON was working we would look at it to determine if another nextRunDate needs to be generated
     *
     * @param Configuration
     */
    void executeAlertJobQuantitative(Configuration configuration, Closure successCallback, Closure failureCallback) {
        ExecutedConfiguration executedConfiguration = null

        String sectionName = ""
        configuration.refresh()
        Configuration lockedConfiguration = Configuration.lock(configuration.id)
        Long templateId = lockedConfiguration.template.id
        Long queryId = lockedConfiguration.alertQueryId

        if (lockedConfiguration.isEnabled) {
            log.info("Now Executing the alert : " + lockedConfiguration.name)
            //Executed Configuration object is created.
            try {
                executedConfiguration = createExecutedConfiguration(lockedConfiguration)
                lockedConfiguration.templateQueries.each {
                    getPersistedReportResult(it.id, executedConfiguration)
                }
            } catch (Throwable exception) {
                log.error("Error happened when creating executed configuration " +
                        "for ${lockedConfiguration.name}. Skipping", exception)
                CRUDService.updateWithAuditLog(lockedConfiguration)
                ExecutionStatusException ese = new ExecutionStatusException(alertService.exceptionString(exception))
                failureCallback(lockedConfiguration, executedConfiguration, ese)
                return
            }

            //retrieve alert data from dataSource
            SuperQueryDTO superQueryDTO = queryService.queryDetail(configuration.alertQueryId)

            //GTT's and version/report sqls are prepared.
            try {
                Long timeBeforeDB = System.currentTimeMillis()
                generateAlertResultQuantitative(configuration, executedConfiguration, superQueryDTO, true, false)
                def timeAfterDB = System.currentTimeMillis()
                log.info(((timeAfterDB - timeBeforeDB) / 1000) +
                        " Secs were taken in fetching the data from database for configuration " +
                        executedConfiguration.name)
            } catch (Throwable exception) {
                log.error("Error in getPersistedReportResult", exception)
                CRUDService.updateWithAuditLog(lockedConfiguration)
                clearDataMiningTables(lockedConfiguration.id, executedConfiguration.id)
                ExecutionStatusException ese = new ExecutionStatusException(alertService.exceptionString(exception))
                failureCallback(lockedConfiguration.id, executedConfiguration.id, ese)
                return
            }


            try {
                def fileNameStr = (Holders.config.statistics.inputfile.path as String) + executedConfiguration.id

                if (Holders.config.statistics.enable.ebgm) {
                    log.info("Now calling the statistics block.")
                    def status = statisticsService.calculateStatisticalScores(lockedConfiguration.selectedDatasource,
                            executedConfiguration.id, false)
                    if (!status.status && Holders.config.statistics.enable.ebgm) {
                        throw new Exception("EBGM score calculation failed! " + status.error)
                    }
                }
            } catch (Throwable th) {
                log.error("Error when preparing and saving EBGM data", th)
                CRUDService.updateWithAuditLog(lockedConfiguration)
                clearDataMiningTables(lockedConfiguration.id, executedConfiguration.id)
                ExecutionStatusException ese = new ExecutionStatusException(alertService.exceptionString(th))
                failureCallback(lockedConfiguration.id, executedConfiguration.id, ese)
                return
            }

            //Once stats scores are calculated then view is polled to fetch the data.
            int alertExecutionTryCount = 0
            String dataStatusMessage = checkDataStatus(lockedConfiguration.selectedDatasource, executedConfiguration, alertExecutionTryCount)

            log.info(dataStatusMessage)

            if (dataStatusMessage != Holders.config.alertExecution.status.success.message) {
                CRUDService.updateWithAuditLog(lockedConfiguration)
                clearDataMiningTables(lockedConfiguration.id, executedConfiguration.id)
                Throwable th = new Throwable(dataStatusMessage)
                ExecutionStatusException ese = new ExecutionStatusException(th.message)
                failureCallback(lockedConfiguration.id, executedConfiguration.id, ese)
                return
            }

            boolean callPrrRorFlow = false

            if ((lockedConfiguration.selectedDatasource == Constants.DataSource.PVA) &&
                    Holders.config.statistics.enable.prr && Holders.config.statistics.enable.ror) {
                callPrrRorFlow = true
            } else if ((lockedConfiguration.selectedDatasource == Constants.DataSource.FAERS) &&
                    Holders.config.statistics.faers.enable.prr && Holders.config.statistics.faers.enable.ror) {
                callPrrRorFlow = true
            } else if ((lockedConfiguration.selectedDatasource == Constants.DataSource.VAERS) &&
                    Holders.config.statistics.vaers.enable.prr && Holders.config.statistics.vaers.enable.ror) {
                callPrrRorFlow = true
            }
            else if ((lockedConfiguration.selectedDatasource == Constants.DataSource.VIGIBASE) &&
                    Holders.config.statistics.vigibase.enable.prr && Holders.config.statistics.vigibase.enable.ror) {
                callPrrRorFlow = true
            }

            if (callPrrRorFlow) {
                Sql sql = null
                try {
                    sql = new Sql(signalDataSourceService.getReportConnection(configuration.selectedDatasource))
                    //Fetch the PRR values
                    statisticsService.getStatsPRRData(sql, executedConfiguration.id, configuration)
                    sql.close()
                } catch (Throwable th) {
                    sql?.close()
                    log.error("Error while fetching and preparing PRR data", th)
                    CRUDService.updateWithAuditLog(lockedConfiguration)
                    clearDataMiningTables(lockedConfiguration.id, executedConfiguration.id)
                    ExecutionStatusException ese = new ExecutionStatusException(alertService.exceptionString(th))
                    failureCallback(lockedConfiguration.id, executedConfiguration.id, ese)
                    return
                }finally{
                    sql?.close()
                }
            }

            Sql sql = null
            List alertData = []
            try {
                sql = new Sql(signalDataSourceService.getReportConnection(configuration.selectedDatasource))
                alertData = prepareAlertData(sql, true, executedConfiguration, configuration)
                sql?.close()
            } catch (Throwable th) {
                sql?.close()
                log.error("Error while fetching and preparing count and alert data sql", th)
                CRUDService.updateWithAuditLog(lockedConfiguration)
                clearDataMiningTables(lockedConfiguration.id, executedConfiguration.id)
                ExecutionStatusException ese = new ExecutionStatusException(alertService.exceptionString(th))
                failureCallback(lockedConfiguration.id, executedConfiguration.id, ese)
                return
            }finally{
                sql?.close()
            }

            CRUDService.updateWithAuditLog(lockedConfiguration) //unlock

            Closure successCallBack = { Long scheduledConfigId, Long executedConfigId ->
                successCallback(scheduledConfigId, executedConfigId)
                clearDataMiningTables(scheduledConfigId, executedConfigId)
            }
            Closure failureCallBack = { Long scId, Long ecId, Throwable exception ->
                ExecutionStatusException ese = new ExecutionStatusException(exception.message)
                failureCallback(scId, ecId, ese)
            }
            if (lockedConfiguration.adhocRun) {
                aggregateOnDemandAlertService.createAlert(lockedConfiguration.id, executedConfiguration.id,
                        alertData, successCallBack, failureCallBack)
            } else {
                aggregateCaseAlertService.createAlert(lockedConfiguration.id, executedConfiguration.id,
                        alertData, successCallBack, failureCallBack)
            }

        } else {
            log.info("${lockedConfiguration.name} is not enabled. Skipping")
        }
    }

    String checkDataStatus(String datasource, ExecutedConfiguration executedConfiguration, int alertExecutionTryCount, Map param = [:], Boolean isAWSfailed = false) {
        Sql sql = null
        int status = 0
        try {
            Long t1 = System.currentTimeMillis()
            sql = new Sql(signalDataSourceService.getReportConnection(datasource))
            //Open the select result data based on the status query.
            String query = SignalQueryHelper.alert_status_sql(executedConfiguration.id)
            sql.eachRow(query) { ResultSet resultSetObj ->
                status = resultSetObj.getInt("CURRENT_STATUS")
            }
            Long t2 = System.currentTimeMillis()
            log.info("The time taken to read is " + (t2 - t1) / 1000 + " Secs and status is ${status}.")

        } catch (Throwable th) {
            th.printStackTrace()
            status = Holders.config.alertExecution.status.failure.code
        } finally {
            sql?.close()
        }
        if (status == Holders.config.alertExecution.status.in_progress.code) {
            if (Holders.config.pvsignal.alert.execution.retry < alertExecutionTryCount) {
                return Holders.config.alertExecution.status.timeout.message
            }
            Thread.sleep(120000)
            alertExecutionTryCount = alertExecutionTryCount + 1
            checkDataStatus(datasource, executedConfiguration, alertExecutionTryCount, param, isAWSfailed)
        } else if (status == Holders.config.alertExecution.status.failure.code) {
            return Holders.config.alertExecution.status.failure.message
        } else if (status == Holders.config.alertExecution.status.success.code) {
            pUpdateAlertMetadataMaster(executedConfiguration.id, datasource)
            return Holders.config.alertExecution.status.success.message
        } else if (status == Holders.config.alertExecution.status.aws.failure.code) {
            log.info("Alert got failed on AWS execution now recalling gttInsertionAndDataMining for normal DB flow")
            isAWSfailed=true
            if (!param.isEmpty()) {
                clearDataMiningTablesOnResume(executedConfiguration.id,executedConfiguration.selectedDatasource)
                gttInsertionAndDataMining(param?.configuration, param.executedConfiguration, param.superQueryDTO, param.dataSource, param.isIntegratedVaers, param.isIntegratedVigibase,
                        param.dsAlertProgressStatus, param.executionStatus, true)
                checkDataStatus(datasource, executedConfiguration, alertExecutionTryCount, param, isAWSfailed)
            } else {
                return Holders.config.alertExecution.status.failure.message
            }
        } else if (status == Holders.config.alertExecution.status.awsdb.api.code && datasource in Holders.config.awsdb.supported.dbs) {
            if (isAWSfailed == false && !checkApiForAWSDb(executedConfiguration, datasource)) {
                isAWSfailed=true
                if (!param.isEmpty()) {
                    log.info("AWS api for alert execution gives false respons now recalling gttInsertionAndDataMining for normal DB flow")
                    clearDataMiningTablesOnResume(executedConfiguration.id,executedConfiguration.selectedDatasource)
                    gttInsertionAndDataMining(param?.configuration, param.executedConfiguration, param.superQueryDTO, param.dataSource, param.isIntegratedVaers, param.isIntegratedVigibase,
                            param.dsAlertProgressStatus, param.executionStatus, true)
                    checkDataStatus(datasource, executedConfiguration, alertExecutionTryCount, param, isAWSfailed)
                } else {
                    return Holders.config.alertExecution.status.failure.message
                }
            } else {
                checkDataStatus(datasource, executedConfiguration, alertExecutionTryCount, param, isAWSfailed)
            }
        } else {
            return Holders.config.alertExecution.status.failure.message
        }
    }

    void triggerAlertAtThreshold(Configuration configuration, ExecutedConfiguration execConfig, List fetchedCases) {
        Integer triggerCasesCount = configuration.alertTriggerCases
        if (triggerCasesCount && fetchedCases) {
            Integer triggerCasesDays = configuration.alertTriggerDays
            if (triggerCasesDays == 0) {
                if (fetchedCases.size() >= triggerCasesCount) {
                    if (emailNotificationService.emailNotificationWithBulkUpdate(null, Constants.EmailNotificationModuleKeys.SCA_ALERT)) {
                        sentAlertThresholdEmail(execConfig.id, configuration, fetchedCases)
                        sentAlertThresholdEmail(execConfig, configuration, fetchedCases)
                    }
                }
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss zz yyyy")
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"))
                Date currentDate = new Date(sdf.format(new Date()))
                Date previousDate = currentDate - triggerCasesDays

                List<SingleCaseAlert> singleCaseAlerts = SingleCaseAlert.createCriteria().list {
                    between('dateCreated', previousDate, currentDate)
                    eq("alertConfiguration", configuration)
                    eq("executedAlertConfiguration", execConfig)
                } as List<SingleCaseAlert>
                if (singleCaseAlerts.size() > triggerCasesCount) {
                    if (emailNotificationService.emailNotificationWithBulkUpdate(null, Constants.EmailNotificationModuleKeys.SCA_ALERT)) {
                        sentAlertThresholdEmail(execConfig, configuration, fetchedCases)
                    }
                }
            }
        }
    }

    private updateCapturedCases(fetchedCases) {
        def cases = ""
        fetchedCases.each {
            cases = cases + "'" + it + "'" + ","
        }
        //Remove the last comma.
        cases = cases.substring(0, cases.length() - 1)
        log.info("Cases captured :  " + cases)
        def updateQry = SignalQueryHelper.update_auto_alert_sql(cases)

        final Sql sqlObj = new Sql(dataSource_pva)
        try {
            sqlObj.executeUpdate(updateQry)
        } catch (Exception e) {
            log.error(e.printStackTrace())
        } finally {
            sqlObj.close()
        }
    }

    private void sentAlertThresholdEmail(ExecutedConfiguration execConfig, Configuration configuration, List fetchedCases) {
        def alertLink = createHref("singleCaseAlert", execConfig?.adhocRun ? "adhocDetails" : "details", ["configId": execConfig?.id, "callingScreen": "review"])
        def locale = userService?.user?.preference?.locale ?: Locale.ENGLISH
        emailService.sendNotificationEmail([
                'toAddress': userService.getRecipientsList(configuration),
                'inboxType': "Triggered Alert",
                'title'    : messageSource.getMessage("app.signal.case.subject",
                        [configuration.name, configuration.getProductNameList()].toArray(), locale),
                'map'      : ["map"         : ['Alert Name '                        : configuration.name,
                                               "Product Name "                      : configuration.getProductNameList(),
                                               "Event "                             : configuration.getEventSelectionList() ?: "",
                                               "Query "                             : configuration.templateQueries[0]?.queryName,
                                               "Case Numbers which Triggered Alert ": fetchedCases?.join(','),
                ],
                              "emailMessage": messageSource.getMessage('app.signal.alert.trigger.message', null, locale),
                              "alertLink"   : alertLink]])
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception)
    ExecutedConfiguration getPersistedReportResult(Long templateQueryId, ExecutedConfiguration executedConfiguration) throws Exception {
        addExecutedTemplateQueryToExecutedConfiguration(templateQueryId, executedConfiguration)
        executedConfiguration.save(flush: true)
    }

    private setTotalExecutionTimeForConfiguration(Configuration configuration, long executionTime) throws Exception {
        configuration.totalExecutionTime = executionTime
    }

    /**
     * Creation of Executed configuration is cruicial in alert execution flow. As we are trying to maintain one execution configuration for the
     * single case alert all the time for each logged in user.
     * @param configuration
     * @return
     * @throws Exception
     */
    ExecutedConfiguration createExecutedConfiguration(Configuration configuration) throws Exception {
        ExecutedConfiguration executedConfiguration = saveExecutedConfiguration(configuration)
        return executedConfiguration
    }

    ExecutedConfiguration saveExecutedConfiguration(Configuration configuration, MasterExecutedConfiguration masterExecutedConfiguration = null, Configuration integratedConfiguration = null) {
        Date reviewDate = null

        if(!configuration.isAdhocRun()) {
            use(TimeCategory) {
                reviewDate = new Date() + (configuration.reviewPeriod).day
            }
        }
        String dataSource = integratedConfiguration ? integratedConfiguration.selectedDatasource : configuration.selectedDatasource
        Date startDate = configuration.alertDateRangeInformation.getReportStartAndEndDate()[0]
        Date endDate = configuration.alertDateRangeInformation.getReportStartAndEndDate()[1]
        updateLatestProductGroupName(configuration,masterExecutedConfiguration)
        ExecutedAlertDateRangeInformation executedAlertDateRangeInformation = new ExecutedAlertDateRangeInformation(
                dateRangeEnum: configuration.alertDateRangeInformation.dateRangeEnum,
                relativeDateRangeValue: configuration.alertDateRangeInformation.relativeDateRangeValue,
                dateRangeEndAbsolute: endDate, dateRangeStartAbsolute: startDate,
                executedAsOfVersionDate: configuration.getAsOfVersionDateCustom() ?: endDate
        )
        String productRuleNames = ""
        def keyToNameMap = [
                'DRUG_SUSPECT_FAERS': 'Drug(F)-S',
                'DRUG_SUSPECT_CONCOMITANT_FAERS': 'Drug(F)-S+C',
                'VACCINE_SUSPECT_VAERS': 'Vaccine(VA)-S',
                'DRUG_SUSPECT_VIGIBASE': 'Drug(VB)-S',
                'DRUG_SUSPECT_CONCOMITANT_VIGIBASE': 'Drug(VB)-S+C',
                'VACCINE_SUSPECT_VIGIBASE': 'Vaccine(VB)-S',
                'DRUG_SUSPECT_JADER': 'Drug(J)-S',
                'DRUG_SUSPECT_CONCOMITANT_JADER': 'Drug(J)-S+C',
                'VACCINE_SUSPECT_JADER': 'Vaccine(J)-S',
                'SUSPECT' : 'Suspect',
                'SUSPECT_AND_CONCOMITANT' : 'Suspect and Concomitant',
                'VACCINE' : 'Vaccine'
        ]
        configuration?.drugType?.split(',')?.each {
            if(it.isInteger()){
                productRuleNames+=(ProductTypeConfiguration.get(it) ? (ProductTypeConfiguration.get(it)?.name+",") : "")
            }else{
                productRuleNames+=(keyToNameMap[it]+",")
            }
        }
        if(productRuleNames.size()>0){
            productRuleNames=productRuleNames.substring(0,productRuleNames.size()-1)
        }
        ExecutedConfiguration executedConfiguration = new ExecutedConfiguration(name: configuration.name, workflowGroup: configuration.workflowGroup,
                owner: User.get(configuration.owner.id), scheduleDateJSON: configuration.scheduleDateJSON, nextRunDate: configuration.nextRunDate ?: new Date(),
                description: configuration.description, dateCreated: configuration.dateCreated, lastUpdated: configuration.lastUpdated,
                isPublic: configuration.isPublic, isDeleted: configuration.isDeleted, isEnabled: configuration.isEnabled,
                dateRangeType: configuration.dateRangeType, productGroups: configuration.productGroups, spotfireSettings: configuration.spotfireSettings,
                productSelection: configuration.productSelection, eventSelection: configuration.eventSelection, studySelection: configuration.studySelection,
                configSelectedTimeZone: configuration.configSelectedTimeZone, evaluateDateAs: configuration.evaluateDateAs,
                productDictionarySelection: configuration.productDictionarySelection, limitPrimaryPath: configuration.limitPrimaryPath,
                includeMedicallyConfirmedCases: configuration.includeMedicallyConfirmedCases, excludeFollowUp: configuration.excludeFollowUp,
                includeLockedVersion: configuration.includeLockedVersion, adjustPerScheduleFrequency: configuration.adjustPerScheduleFrequency,
                isAutoTrigger: configuration.isAutoTrigger, adhocRun: configuration.adhocRun, reviewDueDate: reviewDate,
                createdBy: configuration.getOwner().username, modifiedBy: configuration.modifiedBy, selectedDatasource: dataSource,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: configuration.numOfExecutions + 1,
                excludeNonValidCases: configuration.excludeNonValidCases,
                alertQueryName: configuration.alertQueryName, executedAlertQueryId: configuration.alertQueryId,
                executedAlertDateRangeInformation: executedAlertDateRangeInformation,
                alertForegroundQueryName: configuration.alertForegroundQueryName, executedAlertForegroundQueryId: configuration.alertForegroundQueryId,
                foregroundSearch:configuration.foregroundSearch,foregroundSearchAttr:configuration.foregroundSearchAttr,
                priority: configuration.priority, alertTriggerCases: configuration.alertTriggerCases, alertTriggerDays: configuration.alertTriggerDays,
                alertRmpRemsRef: configuration.alertRmpRemsRef, onOrAfterDate: configuration.onOrAfterDate, asOfVersionDate: configuration.asOfVersionDate,
                applyAlertStopList: configuration.applyAlertStopList, suspectProduct: configuration.suspectProduct, missedCases: configuration.missedCases,
                productGroupSelection: configuration.productGroupSelection, eventGroupSelection: configuration.eventGroupSelection, configId: configuration.id,
                drugType: configuration.drugType, isAutoAssignedTo: configuration.isAutoAssignedTo,drugTypeName: productRuleNames, drugClassification: configuration.drugClassification,
                alertCaseSeriesName: configuration.alertCaseSeriesName, isTemplateAlert: configuration.isTemplateAlert, isProductMining: configuration.isProductMining,
                dataMiningVariable: configuration.dataMiningVariable, dataMiningVariableValue: configuration.dataMiningVariableValue, isStandalone: configuration.isStandalone,
                isMultiIngredient: configuration.isMultiIngredient, includeWHODrugs: configuration.includeWHODrugs, studyCases: configuration.studyCases, onlyPoi: configuration.onlyPoi,
                ignoreStudyType: configuration.ignoreStudyType, drugRecordNumbers: configuration.drugRecordNumbers)

        if (configuration.selectedDatasource.contains(Constants.DataSource.PVA) && configuration?.adjustmentTypeEnum) {
            log.info("Now setting values for auto adjusted ex config...")
            executedConfiguration.skippedAlertGroupCode = configuration.skippedAlertGroupCode
            executedConfiguration.adjustmentTypeEnum = configuration.adjustmentTypeEnum

            if (configuration.adjustmentTypeEnum == AdjustmentTypeEnum.ALERT_PER_SKIPPED_EXECUTION) {
                executedConfiguration.skippedAlertId = configuration.skippedAlertId
            } else if (configuration.adjustmentTypeEnum == AdjustmentTypeEnum.SINGLE_ALERT_FOR_ALL_SKIPPED_EXECUTION) {
                alertAdministrationService.adjustExecutedConfigurationForSingleTypeAutoAdjustment(configuration, executedConfiguration)
            }
        }

        if (configuration.adjustmentTypeEnum != AdjustmentTypeEnum.SINGLE_ALERT_FOR_ALL_SKIPPED_EXECUTION) {
            if (executedConfiguration.evaluateDateAs == EvaluateCaseDateEnum.LATEST_VERSION && !DateUtil.matchDate(executedConfiguration.nextRunDate, new Date())) {
                executedConfiguration.evaluateDateAs = EvaluateCaseDateEnum.VERSION_ASOF
                executedConfiguration.asOfVersionDate = executedConfiguration.nextRunDate - 1
            }
        }

        // drugType fields added according to requirement

        if (configuration.alertCaseSeriesId) {
            populateLimitToCaseSeriesData(executedConfiguration, configuration.alertCaseSeriesId)
        }
        if (configuration.selectedDatasource.contains(Constants.DataSource.FAERS) && !configuration.selectedDatasource.startsWith(Constants.DataSource.FAERS)) {
            Map faersDateRange = getFaersDateRange()
            executedConfiguration.faersDateRange = faersDateRange.faersDate
        }
        if (configuration.selectedDatasource.contains(Constants.DataSource.VAERS) && !configuration.selectedDatasource.startsWith(Constants.DataSource.VAERS)) {
            Map vaersDateRange = getVaersDateRange()
            executedConfiguration.vaersDateRange = vaersDateRange.vaersDate
        }
        // check required for agg alert
        if(configuration.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            if (executedConfiguration.dataMiningVariable && executedConfiguration.dataMiningVariable != "null" && executedConfiguration.adhocRun) {
                executedConfiguration.stratificationColumnsDataMining = new JsonBuilder(aggregateCaseAlertService.getStratificationValuesDataMiningVariables(executedConfiguration.selectedDatasource, executedConfiguration.dataMiningVariable)).toPrettyString()
            } else {
                executedConfiguration.stratificationColumns = new JsonBuilder(aggregateCaseAlertService.getStratificationValues(executedConfiguration.selectedDatasource)).toPrettyString()
            }
        }
        if(configuration.selectedDatasource.contains(Constants.DataSource.VIGIBASE)&& !configuration.selectedDatasource.startsWith(Constants.DataSource.VIGIBASE)) {
            Map vigibaseDateRange = getVigibaseDateRange()
            executedConfiguration.vigibaseDateRange = vigibaseDateRange.vigibaseDate
        }

        executedConfiguration.allProducts = getAllProductsWithHierarchy(executedConfiguration)

        configuration.alertQueryValueLists.each {
            ExecutedQueryValueList executedQVL = new ExecutedQueryValueList(query: it.query, queryName: it.queryName)
            it.parameterValues.each {
                ParameterValue executedValue
                if (it.hasProperty('reportField')) {
                    ReportField rp=ReportField.get(it.reportField.id)
                    executedValue = new ExecutedQueryExpressionValue(key: it.key,
                            reportField: rp, operator: it.operator, value: it.value,
                            operatorValue: messageSource.getMessage( "app.queryOperator.$it.operator", null, Locale.ENGLISH ) )
                } else {
                    executedValue = new ExecutedCustomSQLValue(key: it.key, value: it.value)
                }
                executedQVL.addToParameterValues(executedValue)
            }
            executedConfiguration.addToExecutedAlertQueryValueLists(executedQVL)
        }
        configuration.alertForegroundQueryValueLists.each {
            ExecutedQueryValueList executedQVL = new ExecutedQueryValueList(query: it.query, queryName: it.queryName)
            it.parameterValues.each {
                ParameterValue executedValue
                if (it.hasProperty('reportField')) {
                    ReportField rp=ReportField.get(it.reportField.id)
                    executedValue = new ExecutedQueryExpressionValue(key: it.key,
                            reportField: rp, operator: it.operator, value: it.value, operatorValue: messageSource.getMessage( "app.queryOperator.$it.operator", null, Locale.ENGLISH ) )
                } else {
                    executedValue = new ExecutedCustomSQLValue(key: it.key, value: it.value)
                }
                executedQVL.addToParameterValues(executedValue)
            }
            executedConfiguration.addToExecutedAlertForegroundQueryValueLists(executedQVL)
        }

        executedConfiguration.type = configuration.type
        executedConfiguration.groupBySmq = configuration.groupBySmq
        List dicList = PVDictionaryConfig.ProductConfig?.views?.collect {
            messageSource.getMessage(it.code, null, Locale.default)
        }
        int userAssignmentIndex = -1
        dicList.eachWithIndex { value, index ->
            if (value.toString() == "User Assignment") {
                userAssignmentIndex = index + 1
            }
        }
        Boolean userViewExists = true
        if (configuration.productSelection) {
            Map productSelectionMap = JSON.parse(configuration.productSelection)
            if (userAssignmentIndex > 0 && productSelectionMap.get(userAssignmentIndex as String)) {
                List usersList = productSelectionMap.get(userAssignmentIndex as String)
                usersList.each {
                    Long userId = it.id as Long
                    List userViewsList = UserViewAssignment.createCriteria().list {
                        or {
                            eq("userAssigned", userId)
                            eq("groupAssigned", userId)
                        }
                    }
                    if (userViewsList.size() == 0) {
                        userViewExists = false
                    }
                }
            }
        }
        ProductViewAssignment matchedAssignment = null
        if (configuration.autoShareWithUser || configuration.autoShareWithGroup || configuration.isAutoAssignedTo) {
            matchedAssignment = getMatchingProductViewAssignment(configuration)
        }
        if (configuration.autoShareWithUser || configuration.autoShareWithGroup) {
            List<User> foundUsers = []
            List<Group> foundGroups = []
            if (matchedAssignment) {
                matchedAssignment.usersAssigned.each {
                    foundUsers.add(User.get(it))
                }
                matchedAssignment.groupsAssigned.each {
                    foundGroups.add(Group.get(it))
                }
                executedConfiguration.autoShareWithUser = foundUsers.unique()
                executedConfiguration.autoShareWithGroup = foundGroups.unique()
                configuration.autoShareWithUser = foundUsers.unique()
                configuration.autoShareWithGroup = foundGroups.unique()
                if(!masterExecutedConfiguration) {
                    CRUDService.updateWithAuditLog(configuration)
                }
            }
        }
        if (!configuration.isAutoAssignedTo) {
            executedConfiguration.assignedTo = configuration.assignedTo
            executedConfiguration.assignedToGroup = configuration.assignedToGroup
        } else {
            if (matchedAssignment) {
                String primaryUserOrGroupId = matchedAssignment.primaryUserOrGroupId
                if (primaryUserOrGroupId.contains(Constants.USER_GROUP_TOKEN)) {
                    Long groupId = primaryUserOrGroupId.replace(Constants.USER_GROUP_TOKEN, "") as Long
                    Group group = Group.get(groupId)
                    if (group) {
                        executedConfiguration.assignedToGroup = group
                        executedConfiguration.isAutoAssignedTo = true
                    }
                } else {
                    Long userId = primaryUserOrGroupId.replace(Constants.USER_TOKEN, "") as Long
                    User user = User.get(userId)
                    if (user) {
                        executedConfiguration.assignedTo = user
                        executedConfiguration.isAutoAssignedTo = true
                    }
                }
            }

        }
        executedConfiguration.isEnabled = false
        configuration.templateQueries.each {
            addExecutedTemplateQueryToExecutedConfiguration(it.id, executedConfiguration)
        }
        if (!userViewExists || (!executedConfiguration.assignedTo && !executedConfiguration.assignedToGroup)) {
            log.info("No assignment found for ${configuration.name} [${configuration.id}].")
            throw new Exception("No assignment found")
        }
        if (!configuration.isMasterTemplateAlert)
            executedConfiguration.productName = generateProductName(executedConfiguration)
        if (masterExecutedConfiguration) {
            executedConfiguration.masterExConfigId = masterExecutedConfiguration.id
            if (integratedConfiguration) {
                executedConfiguration.configId = integratedConfiguration.id
            }
        }
        if(configuration.isDatasheetChecked && !TextUtils.isEmpty(configuration.selectedDataSheet)){
            executedConfiguration.isDatasheetChecked = configuration.isDatasheetChecked
            executedConfiguration.datasheetType = configuration.datasheetType
            executedConfiguration.selectedDataSheet = alertService.getOnlyActiveDataSheets(configuration,false) as JSON
        }
        if(configuration.isMasterTemplateAlert){
            executedConfiguration.isMasterTemplateAlert = true
            executedConfiguration.eventHierarchyMasterTmplt = configuration.eventHierarchyMasterTmplt
            executedConfiguration.productHierarchyMasterTmplt = configuration.productHierarchyMasterTmplt
        }
        executedConfiguration.studyDrugs = sqlGenerationService.getIncludedAllStudyDrugs(configuration)

        executedConfiguration.save(flush: true)
        cacheService.setUpperHierarchyProductDictionaryCache(executedConfiguration)
        executedConfiguration
    }

    void updateLatestProductGroupName(Configuration configuration,MasterExecutedConfiguration masterExecutedConfiguration = null) {
        if(configuration!=null && !StringUtils.isEmpty(configuration.productGroupSelection)) {
            String jsonString = configuration.productGroupSelection
            Map productGroupsMap = new HashMap()
            def jsonObj = null
            boolean isProductGroupNameChanged=false
            if (jsonString) {
                jsonObj = parseJsonString(jsonString)
                if (jsonObj) {
                    String oldProductGroupName = ""
                    String newProductGroupName = ""
                    String currentUserName = (springSecurityService.principal?.username!=null)?(springSecurityService.principal?.username):(configuration?.owner?.username)
                    jsonObj.each{ it ->
                        oldProductGroupName = it.name.substring(0,it.name.lastIndexOf('(') - 1)
                        newProductGroupName = getNewProductGroupNameByProductGroupId(currentUserName, productGroupsMap, String.valueOf(it.id), oldProductGroupName)
                        if(!oldProductGroupName.equals(newProductGroupName)) {
                            log.info("ProductGroupName is changed from "+oldProductGroupName+" to "+newProductGroupName+" for product group id "+String.valueOf(it.id))
                            jsonString = jsonString.replace(oldProductGroupName,newProductGroupName)
                            isProductGroupNameChanged=true
                        }
                    }
                }
            }
            if(isProductGroupNameChanged==true) {
                configuration.productGroupSelection = jsonString
                log.info("configuration productGroupSelection is updated in DB for "+configuration.id)
            }
        }
    }
    String getAllProductsWithHierarchy(def executedConfiguration){
        JsonSlurper jsonSlurper = new JsonSlurper()
        Map allProductList = [:]
        List IndexList = ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10"]
        if (executedConfiguration.productGroupSelection) {
            def productGroupList = jsonSlurper.parseText(executedConfiguration.productGroupSelection)
            productGroupList.each { pg ->
                List<String> productList = []
                String currentUserName = (springSecurityService.principal?.username != null) ? (springSecurityService.principal?.username) : (executedConfiguration?.owner?.username)
                DictionaryGroupCmd response = validatedSignalService.fetchGroupDetails(Long.valueOf(pg.id), currentUserName, true)
                Map resultProductGroup = response?.properties
                if (resultProductGroup) {
                    List dataSourcesName = resultProductGroup.dataSourceNames as List
                    def finalOutput
                    if(resultProductGroup?.data){
                        finalOutput = jsonSlurper.parseText(resultProductGroup?.data as String)
                    }
                    dataSourcesName?.each { it ->
                        IndexList.each { ind ->
                            finalOutput?.get(it)?.get(ind)?.each { product ->
                                String productHierachyComb = "'" + ind + "-" + product.name + "'"
                                productList.add(productHierachyComb)
                            }
                        }
                    }
                }
                productList = productList?.unique()
                allProductList.put(pg.id, productList)
            }
        } else {
            def resultProducts = executedConfiguration?.productSelection?jsonSlurper.parseText(executedConfiguration?.productSelection):""
            if(resultProducts) {
                IndexList.each { ind ->
                    resultProducts?.get(ind)?.each { product ->
                        String productHierarchyComb = "'" + ind + "-" + product.name + "'"
                        List tempProduct = []
                        tempProduct.add(productHierarchyComb)
                        allProductList.put(product.id, tempProduct)

                    }
                }
            }
        }
        return allProductList as JSON
    }


    String getAllProductsWithHierarchyConfiguration(String productSelection, String productGroupSelection, ExecutedConfiguration executedConfiguration) {
        JsonSlurper jsonSlurper = new JsonSlurper()
        Map allProductList = [:]
        List IndexList = ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10"]
        if (productGroupSelection) {
            def productGroupList = jsonSlurper.parseText(productGroupSelection)
            productGroupList.each { pg ->
                List<String> productList = []
                String currentUserName = (springSecurityService.principal?.username != null) ? (springSecurityService.principal?.username) : (executedConfiguration?.owner?.username)
                DictionaryGroupCmd response = validatedSignalService.fetchGroupDetails(Long.valueOf(pg.id), currentUserName, true)
                Map resultProductGroup = response?.properties
                if (resultProductGroup) {
                    List dataSourcesName = resultProductGroup.dataSourceNames as List
                    def finalOutput
                    if (resultProductGroup?.data) {
                        finalOutput = jsonSlurper.parseText(resultProductGroup?.data as String)
                    }
                    dataSourcesName?.each { it ->
                        IndexList.each { ind ->
                            finalOutput?.get(it)?.get(ind)?.each { product ->
                                String productHierachyComb = "'" + ind + "-" + product.name + "'"
                                productList.add(productHierachyComb)
                            }
                        }
                    }
                }
                productList = productList?.unique()
                allProductList.put(pg.id, productList)
            }
        } else {
            def resultProducts = productSelection ? jsonSlurper.parseText(productSelection) : ""
            if (resultProducts) {
                IndexList.each { ind ->
                    resultProducts?.get(ind)?.each { product ->
                        String productHierarchyComb = "'" + ind + "-" + product.name + "'"
                        List tempProduct = []
                        tempProduct.add(productHierarchyComb)
                        allProductList.put(product.id, tempProduct)

                    }
                }
            }
        }
        return allProductList as JSON
    }

    String getNewProductGroupNameByProductGroupId(String currentUserName, Map productGroupsMap, String productGroupId, String productGroupName) {
        String latestProductGroupName = productGroupName
        if(productGroupsMap.get(productGroupId)==null) {
            try {
                DictionaryGroupCmd dictionaryGroupCmd = dictionaryGroupService.groupDetails(Long.valueOf(productGroupId), currentUserName, true)
                productGroupsMap.put(String.valueOf(dictionaryGroupCmd.id), dictionaryGroupCmd.groupName)
                log.info("dictionaryGroupService groupDetails is called to get product group detail from PVR....")
            } catch (Exception ex) {
                ex.printStackTrace()
                throw new Exception("Group not found for id: ${productGroupId}" + '\n' + ExceptionUtils.getStackTrace(ex)) //Added for fix PVS-55102
            }
        } else {
            log.info "${productGroupId} productGroupId already exists in productGroupId map: ${productGroupId}"
        }
        if(productGroupsMap.get(productGroupId)!=null) {
            log.info "${productGroupId} is found in productGroupsMap ${productGroupsMap}"
            latestProductGroupName = productGroupsMap.get(productGroupId)
        }
        latestProductGroupName
    }
    void populateLimitToCaseSeriesData(ExecutedConfiguration executedConfiguration, Long configuredCaseSeriesId) {
        String url = Holders.config.pvreports.url
        String path = Holders.config.pvreports.api.executed.caseSeries
        Map query = [id: configuredCaseSeriesId]
        def response = reportIntegrationService.get(url, path, query)
        if (response.status == 200 && response.data && response.data?.id) {
            executedConfiguration.alertCaseSeriesId = response.data?.id
            if (response?.data?.spotfireFileName) {
                executedConfiguration.caseSeriesSpotfireFile = response.data.spotfireFileName + "@@@@DateRange" + response.data.dateRange
            }
        } else if (response?.status == 200 && !response?.data) {
            executedConfiguration.alertCaseSeriesId = -1L
        }
    }

    ProductViewAssignment getMatchingProductViewAssignment(Configuration configuration) {
        String hierarchy = ""
        Map productMap = [:]
        if (configuration.productSelection) {
            Map product = JSON.parse(configuration.productSelection)
            product.each {
                if (it.value) {
                    hierarchy = productAssignmentService.getProductHierarchyWithoutDicMap(it.key as Integer)
                    productMap = it.value[0]
                }
            }
        } else {
            hierarchy = "Product Group"
            productMap = JSON.parse(configuration.productGroupSelection)[0]
        }

        BigInteger productId = productMap.id as BigInteger

        ProductViewAssignment matchedAssignment = ProductViewAssignment.createCriteria().get {
            eq("workflowGroup", configuration.workflowGroup.id)
            eq("hierarchy", hierarchy)
            sqlRestriction("JSON_VALUE(product,'\$.id') = ${productId}")
            maxResults(1)
        }
        if (!matchedAssignment) {
            matchedAssignment = ProductViewAssignment.createCriteria().get {
                isNull('workflowGroup')
                eq("hierarchy", hierarchy)
                sqlRestriction("JSON_VALUE(product,'\$.id') = ${productId}")
                maxResults(1)
            }
        }
        return matchedAssignment
    }

    /**
     * Creates an executedTemplateQueries from a TemplateQuery and adds it to an ExecutedConfiguration
     * @param executedConfig
     * @param templateQuery
     */
    private addExecutedTemplateQueryToExecutedConfiguration(Long templateQueryId, ExecutedConfiguration executedConfiguration) throws Exception {
        TemplateQuery.withTransaction {
            TemplateQuery templateQuery = TemplateQuery.get(templateQueryId)
            //ExecutedTemplate
            def startDate = templateQuery?.dateRangeInformationForTemplateQuery?.getReportStartAndEndDate()[0] ?: new Date()
            def endDate = templateQuery?.dateRangeInformationForTemplateQuery?.getReportStartAndEndDate()[1] ?: new Date()
            ExecutedDateRangeInformation executedDateRangeInfoTemplateQuery = new ExecutedDateRangeInformation(
                    dateRangeEnum: templateQuery?.dateRangeInformationForTemplateQuery?.dateRangeEnum,
                    relativeDateRangeValue: templateQuery?.dateRangeInformationForTemplateQuery?.relativeDateRangeValue,
                    dateRangeEndAbsolute: endDate, dateRangeStartAbsolute: startDate,
                    executedAsOfVersionDate: templateQuery.report.getAsOfVersionDateCustom() ?: endDate
            )

            ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(executedTemplate: templateQuery.template,
                    executedTemplateName: templateQuery.templateName,
                    executedQuery: templateQuery.query,
                    executedQueryName: templateQuery.queryName,
                    executedDateRangeInformationForTemplateQuery: executedDateRangeInfoTemplateQuery,
                    queryLevel: templateQuery.queryLevel, title: templateQuery?.title ?: templateQuery.report.name,
                    header: templateQuery?.header, footer: templateQuery?.footer,
                    createdBy: templateQuery.createdBy, modifiedBy: templateQuery.modifiedBy,
                    headerDateRange: templateQuery?.headerDateRange,
                    blindProtected: templateQuery?.blindProtected, privacyProtected: templateQuery?.privacyProtected)


            templateQuery.templateValueLists?.each {
                ExecutedTemplateValueList executedTVL = new ExecutedTemplateValueList(template: it.template)
                it.parameterValues.each {
                    ParameterValue executedValue = new ExecutedCustomSQLValue(key: it.key, value: it.value)
                    executedTVL.addToParameterValues(executedValue)
                }
                executedTemplateQuery.addToExecutedTemplateValueLists(executedTVL)
            }

            templateQuery.queryValueLists?.each {
                ExecutedQueryValueList executedQVL = new ExecutedQueryValueList(query: it.query, queryName: it.queryName)
                it.parameterValues.each {
                    ParameterValue executedValue
                    if (it.hasProperty('reportField')) {
                        executedValue = new ExecutedQueryExpressionValue(key: it.key,
                                reportField: it.reportField, operator: it.operator, value: it.value,
                                operatorValue: messageSource.getMessage("app.queryOperator.$it.operator", null, Locale.ENGLISH))
                    } else {
                        executedValue = new ExecutedCustomSQLValue(key: it.key, value: it.value)
                    }
                    executedQVL.addToParameterValues(executedValue)
                }
                executedTemplateQuery.addToExecutedQueryValueLists(executedQVL)
            }
            executedConfiguration.addToExecutedTemplateQueries(executedTemplateQuery)

            return executedTemplateQuery
        }
    }

    private void setNextRunDateForConfiguration(Configuration configuration) throws Exception {
        configuration.numOfExecutions = configuration.isResume ? (configuration.numOfExecutions ?: 1) : configuration.numOfExecutions + 1
        if (configuration.adhocRun) {
            configuration.setNextRunDate(null)
            configuration.isEnabled = false
        } else {
            configuration.setNextRunDate(configurationService.getNextDate(configuration))
        }
    }

    void adjustCustomDateRanges(Configuration configuration) {
        def runOnce = null
        def hourly = null
        def nextRunDate = configuration.nextRunDate

        if (JSON.parse(configuration.scheduleDateJSON).recurrencePattern) {
            runOnce = JSON.parse(configuration.scheduleDateJSON)?.recurrencePattern?.contains(Constants.Scheduler.RUN_ONCE)
            hourly = JSON.parse(configuration.scheduleDateJSON)?.recurrencePattern?.contains(Constants.Scheduler.HOURLY)
        }
        if (!runOnce && !hourly) {
            //Start and End check to make sure that date range is custom date range
            if (configuration.alertDateRangeInformation.dateRangeStartAbsolute &&
                    configuration.alertDateRangeInformation.dateRangeEndAbsolute) {
                List<Date> dateRange = configurationService.getUpdatedStartandEndDate(configuration)
                if (dateRange) {
                    configuration.alertDateRangeInformation.dateRangeStartAbsolute = dateRange[0]
                    configuration.alertDateRangeInformation.dateRangeEndAbsolute = dateRange[1]
                }
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    private void addNotification(ExecutedConfiguration executedConfiguration,
                                 ExecutionStatus executionStatus,
                                 User user, Group group = null, String statusMsg = null) {
        List<User> notificationRecipients = group ? userGroupService.fetchUserListForGroup(group) : [user]
        try {
            NotificationLevel status = NotificationLevel.INFO
            String message = statusMsg
            String messageArgs = "$executedConfiguration.name"
            def url = alertService.getDetailsUrlMap(executedConfiguration.type, executedConfiguration.adhocRun)
            if (executionStatus.executionStatus == ReportExecutionStatus.COMPLETED) {
                status = NotificationLevel.INFO
                message = "app.notification.completed"
            } else if (executionStatus.executionStatus == ReportExecutionStatus.WARN) {
                status = NotificationLevel.WARN
                message = "app.notification.needsReview"
            } else if (executionStatus.executionStatus != ReportExecutionStatus.GENERATING) {
                status = NotificationLevel.ERROR
                message = "app.notification.failed"
                url = Constants.ERROR_URL
            }


            def type = executedConfiguration.type
            def inboxType = "Alert Execution"
            if (type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
                inboxType = "Quantitative Alert Execution"
            } else if (type == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
                inboxType = "Qualitative Alert Execution"
            }
            InboxLog inboxLog
            notificationRecipients.each { User notificationRecipient ->
                inboxLog = new InboxLog(notificationUserId: notificationRecipient.id, level: status, message: message,
                        messageArgs: messageArgs, type: inboxType, subject: messageSource.getMessage(message, [messageArgs].toArray(),
                        notificationRecipient.preference.locale), content: "", createdOn: new Date(), inboxUserId: notificationRecipient.id, isNotification: true,
                        executedConfigId: executedConfiguration.id, detailUrl: url)
                inboxLog.save(flush: true, failOnError: true)
                notificationHelper.pushNotification(inboxLog)

            }
            if(message in ["app.notification.completed","app.notification.failed"]) {
                createAuditForExecution(executedConfiguration, message, executedConfiguration.type == Constants.AlertConfigType.SINGLE_CASE_ALERT ? Constants.AlertConfigType.INDIVIDUAL_CASE_CONFIGURATIONS : Constants.AlertConfigType.AGGREGATE_CASE_CONFIGURATIONS)
            }
        } catch (Throwable e) {
            log.error("Error creating Notification: ${e.message}", e)
        }
    }
    void createAuditForExecution(def executedConfiguration,def message,def alertType){

            String auditMessage = ""
            if (message == "app.notification.completed") {
                auditMessage = Constants.ActionStatus.EXECUTION_COMPLETED
            } else {
                auditMessage = Constants.ActionStatus.EXECUTION_FAILED
            }
            List<Map> auditChildMap = []
            def childEntry = [:]
            childEntry = [
                    propertyName: "Alert Name",
                    newValue    : executedConfiguration.name]
            auditChildMap << childEntry
            childEntry = [
                    propertyName: "Owner",
                    newValue    : executedConfiguration.owner.fullName]
            auditChildMap << childEntry
            String userName = executedConfiguration.modifiedBy
            String fullName = User.findByUsername(executedConfiguration.modifiedBy)?.fullName ?: executedConfiguration.modifiedBy
            if (alertAdministrationService.isConfigurationScheduledGeneric(executedConfiguration)){
                userName= "SYSTEM"
                fullName=""
            }
        if (executedConfiguration.class == ExecutedConfiguration.class && executedConfiguration.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            def externalDsDateRange = configurationService.getDateRangeForPublicDS(executedConfiguration)
            childEntry = [
                    propertyName: "Date Range",
                    newValue    :  externalDsDateRange.size() == 1 ? externalDsDateRange["Date Range"] : externalDsDateRange?.toString()]
            auditChildMap << childEntry
        }
        signalAuditLogService.createAuditLog([
                    entityName : executedConfiguration.getClass().getSimpleName(),
                    moduleName : "${alertType}: Execution",
                    category   : AuditTrail.Category.INSERT.toString(),
                    entityValue: executedConfiguration.getInstanceIdentifierForAuditLog()+"(${auditMessage})",
                    description: "Executed ${alertType}:${executedConfiguration.name}",
                    username   : userName,//need o change this as modified by can be username or userfullname
                    fullname   : fullName,
            ] as Map, auditChildMap)


    }

    /**
     * For debug purposes return a list of String representing the SQL that is used to generate a report given a Configuration
     * @param configuration
     * @return List < Map >  with the innermost Map containing the SQL string for the following keys [templateQueryId, versionSql, querySql, reportSql, headerSql]
     */
    List debugReportSQL(Configuration configuration, ExecutedConfiguration executedConfiguration, boolean isAggregateCase = false) throws Exception {

        BasicFormatterImpl formatter = new BasicFormatterImpl()
        List sqlList = []

        long sectionStart = System.currentTimeMillis()

        try {
            SuperQueryDTO superQueryDTO = queryService.queryDetail(configuration.alertQueryId)
            Map sqlMap = [configurationId: "${configuration.id}"]

            boolean hasQuery = true
            alertService.fetchFirstExecutionDate(executedConfiguration, configuration)
            def selectedDatasource = configuration.selectedDatasource
            String gttInsertForProductTypeConfiguration = ""
            if (isAggregateCase && executedConfiguration) {
                selectedDatasource.split(',').each { dataSource ->
                    Sql sql = new Sql(signalDataSourceService.getReportConnection(dataSource))
                    def roleMap = [:]
                    def productTypeMap = [:]
                    if (dataSource != Constants.DataSource.PVA && dataSource != Constants.DataSource.EUDRA) {
                        sql.eachRow("select * from vw_product_type where lang_id = '1'", []) { row ->
                            productTypeMap.put(row.product_type, row.product_type_id)
                        }
                        sql.eachRow("select * from VW_CLP_DRUG_TYPE_DSP where therapy_type is not null and lang_id = '1'", []) { row ->
                            roleMap.put(row.therapy_type, row.id)
                        }
                    }
                    gttInsertForProductTypeConfiguration += sqlGenerationService.gttInsertForProductTypeConfigurationSql(dataSource, executedConfiguration, roleMap, productTypeMap)
                }
            }
            boolean isEligibleForAWS = aggregateExecutorService.isConfigurationReadyForAWS(configuration)
            String initialParamsInsert = sqlGenerationService.initializeAlertGtts(configuration, executedConfiguration?.id, hasQuery, isAggregateCase, true, false, selectedDatasource?selectedDatasource.split(',')[0]: null, isEligibleForAWS)
            initialParamsInsert += gttInsertForProductTypeConfiguration
            sqlMap.gttInsertSql = formatter.format((initialParamsInsert ?: "") as String)

            String initializeMissedCasesSql
            if (executedConfiguration.missedCases) {
                initializeMissedCasesSql = sqlGenerationService.initializeGTTForMissedCases(configuration, executedConfiguration)
            }

            if(dataSource == Constants.DataSource.PVA){
                String insertEmergingIssuesStatement = sqlGenerationService.getEmergingIssueTextQuery(EmergingIssue.list())
                sqlMap.insertEmergingIssuesStatement  = insertEmergingIssuesStatement
            }

            sqlMap.gttInitializeMissedCasesSql = formatter.format((initializeMissedCasesSql ?: "") as String)
            //Passing null for the sql parameter to avoid the actual execution of sql.
            List gttInitializeSql = initializeGttTables(configuration, executedConfiguration, superQueryDTO, null, sectionStart, hasQuery, isAggregateCase)

            String gttSql = Constants.Commons.BLANK_STRING

            gttInitializeSql.each {
                gttSql = gttSql + formatter.format((it ?: "") as String) + "\n"
            }

            sqlMap.gttInitializeSql = gttSql


            if (!isAggregateCase) {
                String stopListSql = stopListWithProc(null)
                sqlMap.stopListSql = stopListSql
                sqlMap.sqlResultSql = "{? = call pkg_create_report_sql.p_main()}"
            } else {
                String insertStatementListed =''
                if(executedConfiguration.isDatasheetChecked && executedConfiguration?.selectedDataSheet && !TextUtils.isEmpty(executedConfiguration?.selectedDataSheet)){
                        insertStatementListed =  "Begin execute immediate('delete from gtt_pvs_ds_input_data');"
                        Map dataSheetsMap = JSON.parse(executedConfiguration.selectedDataSheet)
                        def dsId = null
                        def baseId = null
                        dataSheetsMap?.each { key, value ->
                            dsId = key?.toString()?.split('_')[0]
                            baseId = key?.toString()?.split('_')[1]
                            insertStatementListed += "INSERT INTO GTT_PVS_DS_INPUT_DATA (Base_Id,Datasheet_Id, Datasheet_Name, Execution_Id) VALUES ('${baseId}','${dsId}','${value}', '${executedConfiguration.id}');"+"\n"
                        }
                        insertStatementListed += " END;"+"\n"
                    String listednessProc = insertStatementListed  + """ 
                                              begin PKG_SAFETY_AGG_LISTEDNESS.p_mains(${executedConfiguration.id}); end;"""
                    sqlMap.listednessProc = formatter.format(listednessProc)
                }
                String caseSeriesProc = sqlGenerationService.persistCaseSeriesExecutionData(executedConfiguration.id)
                sqlMap.caseSeriesSql = caseSeriesProc
                String popAggDataProc = initiateAggregateDataMining(executedConfiguration, configuration, configuration.selectedDatasource, true, true)
                sqlMap.aggDataMiningSql = popAggDataProc
                List sqlResult = processTemplate(null, isAggregateCase, 0L, configuration?.selectedDatasource, configuration.eventGroupSelection && configuration.groupBySmq, configuration.adhocRun, null, isEligibleForAWS)
                sqlMap.sqlResultSql = sqlResult[0]
            }
            sqlList.add(sqlMap)
        } catch (Throwable t) {
            t.printStackTrace()
            log.error(t.getMessage())
        }
        return sqlList
    }

    def generateAlertResultQualitative(Configuration configuration, ExecutedConfiguration executedConfiguration,
                                       SuperQueryDTO superQueryDTO, boolean isAggregateCase = false,
                                       boolean isReportCumulative = false, Long caseSeriesId = null, boolean isSpotfire = false) throws Exception {

        Long sectionStart = System.currentTimeMillis()
        boolean hasQuery = true

        def selectedDatasource = configuration.selectedDatasource
        log.info("selectedDatasource is : " + selectedDatasource)

        final Sql sql = new Sql(signalDataSourceService.getReportConnection(selectedDatasource))
        if (isReportCumulative) {
            CumThreadInfoDTO cumThread = dataObjectService.getCumCaseSeriesThreadFromMap(configuration.id)
            if (cumThread) {
                dataObjectService.getCumCaseSeriesThreadFromMap(configuration.id).sql = sql
            } else {
                Long cumExecConfigId = validatedSignalService.getNext(false)
                dataObjectService.setCumCaseSeriesThreadMap(configuration.id, new CumThreadInfoDTO(cumExecConfigId: cumExecConfigId))
                dataObjectService.getCumCaseSeriesThreadFromMap(configuration.id).sql = sql
            }
        }
        log.info("Generating the sql queries.")
        List alertData = []
        try {
            if (org.apache.commons.lang.StringUtils.equalsIgnoreCase('pva', selectedDatasource)) {
                //added code for db schema previlage lock
                DbUtil.executePIIProcCall(sql, Constants.PII_OWNER, Constants.PII_ENCRYPTION_KEY)
                // piencryption key will be changed according to pvcm team
            }
            prepareGttsAndVersionSqls(configuration, executedConfiguration, hasQuery, isAggregateCase, sql, superQueryDTO, sectionStart, selectedDatasource, false, false, isReportCumulative, caseSeriesId, isSpotfire)
            //The alert stop list flow to remove the cases which are not in use.
            if (configuration.applyAlertStopList) {
                stopListWithProc(sql)
            }

            if (caseSeriesId) {
                alertData = prepareBusinessConfigurationCaseList(sql, executedConfiguration.id)
            } else if (isReportCumulative) {
                return prepareCumulativeReportCaseSeriesList(executedConfiguration)
            } else {
                alertData = prepareAlertDataFromMart(sql, false, executedConfiguration, configuration)
            }

        } catch (Exception ex) {
            log.error("Exception came in executing the mining run. ", ex)
            throw ex
        } finally {
            sql.close()
        }
        return alertData
    }

    boolean prepareGttsAndVersionSqls(Configuration configuration, ExecutedConfiguration executedConfiguration, boolean hasQuery, boolean isAggregateCase, Sql sql, SuperQueryDTO superQueryDTO, long sectionStart, String dataSource, Boolean isIntegratedVaers = false,
                                   boolean isIntegratedVigibase = false, boolean isReportCumulative = false, Long caseSeriesId = null, boolean isSpotfire = false,
                                   MasterConfiguration masterConfiguration = null, MasterExecutedConfiguration masterExecutedConfiguration = null, List exConfigs = null, List dataSheetMap=[],Boolean isAWSfailed=false) {
        Sql pvaSql = null
        boolean isEligibleForAWS = false
        try {
            //The initialize the gtt flow. In this flow all the alert configuration related parameters are considered and are inserted to the gtt tables.
            alertService.fetchFirstExecutionDate(executedConfiguration, configuration)
            def roleMap = [:]
            def productTypeMap = [:]
            if(dataSource != Constants.DataSource.PVA && dataSource != Constants.DataSource.EUDRA){
                sql.eachRow("select * from vw_product_type where lang_id = '1'" , []) { row ->
                    productTypeMap.put(row.product_type, row.product_type_id)
                }
                sql.eachRow("select * from VW_CLP_DRUG_TYPE_DSP where therapy_type is not null and lang_id = '1'" , []) { row ->
                    roleMap.put(row.therapy_type, row.id)
                }
            }

            isEligibleForAWS = dataSource == Constants.DataSource.PVA ? aggregateExecutorService.isConfigurationReadyForAWS(configuration) : false

            String initialParamsInsert = sqlGenerationService.initializeAlertGtts(configuration, executedConfiguration.id, hasQuery, isAggregateCase, isSpotfire, isReportCumulative, dataSource, caseSeriesId, isIntegratedVaers,
                    isIntegratedVigibase, masterConfiguration, masterExecutedConfiguration, exConfigs, roleMap, productTypeMap, isAWSfailed, isEligibleForAWS)


            Map prevData = prevMasterExConfigs(masterConfiguration, masterExecutedConfiguration)
            List<ExecutedConfiguration> prevExConfigs = prevData.prevExConfigs
            Long prevExConfigId = prevData.prevExConfigId

            String insertPrevDssSql
            if(exConfigs) {
                insertPrevDssSql = sqlGenerationService.initializeGTTForPrevDss(exConfigs, masterExecutedConfiguration?.id, prevExConfigs, prevExConfigId)
                if(insertPrevDssSql) {
                    sql?.execute(insertPrevDssSql)
                }
            }



            if (initialParamsInsert) {
                sql?.execute(initialParamsInsert)
            }

            EmergingIssue.list().collate(1000).each { List<EmergingIssue> it ->
                // insert emerging issues
                if(dataSource == Constants.DataSource.PVA){
                    String insertEmergingIssuesStatement = sqlGenerationService.getEmergingIssueTextQuery(it)
                    sql?.execute(insertEmergingIssuesStatement)
                }
            }


            String insertMissedCasesSql
            if (executedConfiguration.missedCases && !isReportCumulative) {
                //if(true){
                insertMissedCasesSql = sqlGenerationService.initializeGTTForMissedCases(configuration, executedConfiguration, prevExConfigs, prevExConfigId)

                if (insertMissedCasesSql) {
                    sql.execute(insertMissedCasesSql)
                }
            }
            def dsId = null
            def baseId = null
            Long exId = null
            Map dataSheetsMap = null
            String insertStatement = "Begin execute immediate('delete from gtt_pvs_ds_input_data');"
                if(configuration.isDatasheetChecked ){
                    if (masterExecutedConfiguration && dataSheetMap) {
                        pvaSql = new Sql(dataSource_pva)
                        exId = masterExecutedConfiguration.id
                        dataSheetMap?.each {
                            dataSheetsMap = JSON.parse(it["selectedDatasheet"])
                            dataSheetsMap?.each { key, value ->
                                dsId = key?.toString()?.split('_')[0]
                                baseId = key?.toString()?.split('_')[1]
                                insertStatement += "INSERT INTO GTT_PVS_DS_INPUT_DATA (Base_Id,Datasheet_Id, Datasheet_Name, Execution_Id) VALUES ('${baseId}','${dsId}','${value}', '${it['id']}');"
                            }
                        }
                    } else {
                        if (executedConfiguration.isDatasheetChecked && executedConfiguration?.selectedDataSheet && !TextUtils.isEmpty(executedConfiguration?.selectedDataSheet)) {
                            pvaSql = new Sql(dataSource_pva)
                            exId = executedConfiguration.id
                            dataSheetsMap = JSON.parse(executedConfiguration.selectedDataSheet)
                            dataSheetsMap?.each { key, value ->
                                dsId = key?.toString()?.split('_')[0]
                                baseId = key?.toString()?.split('_')[1]
                                insertStatement += "INSERT INTO GTT_PVS_DS_INPUT_DATA (Base_Id,Datasheet_Id, Datasheet_Name, Execution_Id) VALUES ('${baseId}','${dsId}','${value}', '${executedConfiguration.id}');"
                            }
                        }
                    }
                    insertStatement += " END;"
                    if(dataSheetsMap){
                        pvaSql.execute(insertStatement)
                        log.info("Calling PKG_SAFETY_AGG_LISTEDNESS.p_mains(?)")
                        String procedure = "call  PKG_SAFETY_AGG_LISTEDNESS.p_mains(?)"
                        pvaSql.call("{${procedure}", [exId])
                    }
                }

        } catch (Exception e) {
            log.error("Alert got failed while populating gtt table. "+ ExceptionUtils.getStackTrace(e))
            throw new Exception("Alert got failed while populating gtt table. "+ '\n' + e.toString())
        }
        finally {
            pvaSql?.close()
        }
        //Now initiallize the gtt tables.

        try {
            initializeGttTables(configuration, executedConfiguration, superQueryDTO, sql, sectionStart, hasQuery, isAggregateCase, isReportCumulative, caseSeriesId)
        } catch(Exception ex) {
            log.error("Alert got failed while generating case series. "+ ExceptionUtils.getStackTrace(ex))
            throw new Exception("Alert got failed while generating case series. "+ '\n' + ex.toString())
        }
        return isEligibleForAWS
    }

    List prepareAlertDataFromMart(Sql sql, boolean isAggregateCase, ExecutedConfiguration executedConfiguration, Configuration configuration, String dataSource=null, boolean isEligibleForAwsExecution = false) {
        //Sql is fetched from the template.
        List resultData = []
        List sqlResult = []
        String reportSql = ''
        if (dataSource != Constants.DataSource.PVA || !isEligibleForAwsExecution) {
            sqlResult = processTemplate(sql, isAggregateCase, executedConfiguration.id, configuration?.selectedDatasource, configuration.eventGroupSelection && configuration.groupBySmq, configuration.adhocRun, dataSource)
            reportSql = sqlResult[0]
            List fieldNameWithIndex = []

            ReportTemplate templateObj = configuration.template
            templateObj= GrailsHibernateUtil.unwrapIfProxy(templateObj)

            if (templateObj.templateType == TemplateTypeEnum.CASE_LINE) {
                CaseLineListingTemplate template = templateObj
                fieldNameWithIndex = template.getFieldNameWithIndex()
            }
            sql.eachRow(reportSql) { GroovyResultSet resultSet ->
                Map map = [:]
                resultSet.toRowResult().eachWithIndex { it, i ->
                    def value = ""
                    if (it.value instanceof Clob) {
                        //Handle Clob data
                        value = it.value.asciiStream.text
                    } else {
                        value = it.value
                    }

                    if (templateObj.templateType == TemplateTypeEnum.CASE_LINE) {
                        map.put(fieldNameWithIndex[i], value)
                    } else {
                        map.put(it.key, value)
                    }
                }
                resultData.add(map)
            }
        } else {
            sqlResult = processTemplate(sql, isAggregateCase, executedConfiguration.id, configuration?.selectedDatasource, configuration.eventGroupSelection && configuration.groupBySmq, configuration.adhocRun, dataSource, true)
            reportSql = sqlResult[0]
            List resultList = aggregateExecutorService.fetchDataFromDataLake(reportSql)
            resultList.each { row ->
                HashMap dataMap = jsonToMap(row) as HashMap
                resultData.add(dataMap)
            }
        }
        return resultData
    }

    List<Long> prepareBusinessConfigurationCaseList(Sql sql, Long execConfigId) {
        List<Long> caseNumberList = []
        sql.eachRow("Select * from ALRT_QRY_CASELST_QLL_$execConfigId", []) { row ->
            caseNumberList.add(row.case_id as Long)
        }
        caseNumberList
    }

    boolean prepareCumulativeReportCaseSeriesList(ExecutedConfiguration executedConfiguration) {
        Sql caseSeriesSql
        def dataSourceObj = signalDataSourceService.getReportConnection(Constants.DataSource.PVA)
        boolean isCaseSeriesSaved = false
        try {
            caseSeriesSql = new Sql(dataSourceObj)
            dataObjectService.getCumCaseSeriesThreadFromMap(executedConfiguration.configId).caseSeriesSql = caseSeriesSql
            Long execConfigId = dataObjectService.getCumCaseSeriesIdFromThread(executedConfiguration.configId)
            caseSeriesSql.execute(sqlGenerationService.delPrevGTTSForCaseSeries())
            log.info("Insertion in gtt starts for cumulative case series")
            caseSeriesSql.execute(SignalQueryHelper.insert_gtt_cumulative_case_series(execConfigId), [])
            log.info("Insertion in gtt completed for cumulative case series")
            isCaseSeriesSaved = saveCaseSeriesInMartForCumulativeReports(caseSeriesSql, executedConfiguration)
        } catch (SQLException e) {
            e.printStackTrace()
        } finally {
            caseSeriesSql?.close()
        }
        isCaseSeriesSaved
    }

    boolean saveCaseSeriesInMartForCumulativeReports(Sql sql, ExecutedConfiguration executedConfiguration) {
        Set<String> warnings = []
        Integer result = 0
        Long startTime = System.currentTimeMillis()
        String endDate = new Date().format(SqlGenerationService.DATE_FMT)
        String dateRangeType = executedConfiguration.dateRangeType.value()
        String evaluateDateAs = executedConfiguration.evaluateDateAs
        String versionAsOfDate = executedConfiguration.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF ? executedConfiguration?.asOfVersionDate?.format(SqlGenerationService.DATE_FMT) : null
        Integer includeLockedVersion = executedConfiguration.includeLockedVersion ? 1 : 0
        String owner = Constants.PVS_CASE_SERIES_OWNER
        sql.call("{?= call PKG_QUERY_HANDLER.f_save_cstm_case_series(?,?,?,?,?,?,?,?,?,?,?,?)}",
                [Sql.NUMERIC, executedConfiguration.name, executedConfiguration.version, executedConfiguration.owner.id, executedConfiguration.pvrCumulativeCaseSeriesId, null, 1, endDate, dateRangeType, evaluateDateAs, versionAsOfDate, includeLockedVersion, owner]) { res ->
            result = res
        }
        if (result != 0) {
            warnings = sql.rows("SELECT * from GTT_REPORT_INPUT_PARAMS").collect { it.PARAM_KEY }
        }
        Long endTime = System.currentTimeMillis()
        log.info("Time taken to save the Case Series \'${executedConfiguration.name}\' in DB : " + ((endTime - startTime) / 1000) + "secs")
        log.info("Cases not saved in DB : ${warnings}")
        warnings.size() == 0
    }

    def initiateAggregateDataMining(def executedConfiguration, Configuration configuration, String dataSource, boolean isIntegratedVaers = false, boolean isViewSql = false, boolean isIntegratedVigibase = false) {
        //Values to be passed to the stored proc.
        List<Date> minMaxDate = isViewSql ? executedConfiguration?.executedAlertDateRangeInformation?.getReportStartAndEndDate() : configuration?.alertDateRangeInformation?.getReportStartAndEndDate()
        Date startDate = minMaxDate.get(0)
        Date endDate = minMaxDate.get(1)
        Date asOfVersionDate = configuration.asOfVersionDate
        Long runId = executedConfiguration.id
        String productGroupIds
        if (configuration.productGroupSelection) {
            productGroupIds = configuration.productGroupList
        }
        def productList = configuration.productCodeList
        def eventList = configuration.eventCodeList
        Map eventSelection = configuration.eventSelection ? JSON.parse(configuration.eventSelection) : null

        Map smqNarrowAndBroadGrouped = [:]
        if (eventSelection?.containsKey('7')) {
            smqNarrowAndBroadGrouped = eventSelection.get('7').groupBy {
                it.name.contains('Narrow')
            }
        }

        String smqBroadList = ""
        String smqNarrowList = ""

        if (smqNarrowAndBroadGrouped) {
            smqBroadList = smqNarrowAndBroadGrouped[false] ? smqNarrowAndBroadGrouped[false]*.id.join(',').replaceAll('\\(B\\)', '') : ''
            smqNarrowList = smqNarrowAndBroadGrouped[true] ? smqNarrowAndBroadGrouped[true]*.id.join(',').replaceAll('\\(N\\)', '') : ''
        }

        def prodFamilyFlag = 0
        if (dataSource == Constants.DataSource.FAERS) {
            prodFamilyFlag = configuration.productDictionarySelection
        }

        def dateType = configuration.dateRangeType.value()
        def caseDate = configuration.evaluateDateAs.value().equals(EvaluateCaseDateEnum.LATEST_VERSION.value()) ? 1 : 0
        def drugOrVaccine = configuration.drugType == DrugTypeEnum.VACCINE.value() ? 0 : 1
        def drugType = configuration.drugType == DrugTypeEnum.SUSPECT.value() ? 0 : 2

        /* lilydemo changes (commented code below) */
        //def drugType = configuration.drugType == DrugTypeEnum.SUSPECT.value() ? 0 : configuration.drugType == DrugTypeEnum.DEVICES.value() ? 3 : 4

        def runType = businessConfigurationService.getConfigurationType(dataSource)
        int smq = 0
        if (configuration.groupBySmq && configuration.eventGroupSelection) {
            smq = 0
        } else if (configuration.groupBySmq) {
            smq = 1
        }
        def followUpExists = configuration.excludeFollowUp ? 1 : 0
        String asOfVersionDateString = asOfVersionDate ? """TO_DATE('${
            asOfVersionDate?.format(SqlGenerationService.DATE_FMT)
        }', '${
            SqlGenerationService.DATETIME_FMT_ORA
        }')""" : null

        String popAggDataProc
        // passing product as null in parameter, db picking products from gtt
        if (dataSource == Constants.DataSource.FAERS &&
                configuration.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            String className = configuration.drugClassification ?: ''

            popAggDataProc = """
                       begin pkg_ebgm_data_mining.p_mains(
                           ${runId},
                           TO_DATE('${startDate?.format(SqlGenerationService.DATE_FMT)}', '${
                SqlGenerationService.DATETIME_FMT_ORA
            }'),
                           TO_DATE('${endDate?.format(SqlGenerationService.DATE_FMT)}', '${
                SqlGenerationService.DATETIME_FMT_ORA
            }'),
                            ${asOfVersionDateString},
                            ${null},
                            ${runType},
                            ${drugType},
                            ${drugOrVaccine},
                            ${prodFamilyFlag},
                            '${dateType}',
                            ${caseDate},
                            ${followUpExists}, 
                            ${smq},
                            '${className}',
                            '${smqNarrowList}',
                            '${smqBroadList}'
                            );
                       end;
                        """

        } else if (dataSource == Constants.DataSource.VAERS &&
                configuration.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {

            def includeLockedCases = 0
            if (isIntegratedVaers) {
                Map dateRange = getVaersDateRange()
                if (configuration.alertDateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE) {
                    startDate = new Date().parse("dd-MM-yyyy", "01-01-1900")
                } else {
                    startDate = DateUtil.parseDate(dateRange.startDate, Constants.DateFormat.DISPLAY_NEW_DATE)
                }
                endDate = DateUtil.parseDate(dateRange.endDate, Constants.DateFormat.DISPLAY_NEW_DATE)
            }
            popAggDataProc = """
                       begin  pkg_ebgm_data_mining.p_mains(
                           ${runId},
                           TO_DATE('${startDate?.format(SqlGenerationService.DATE_FMT)}', '${
                SqlGenerationService.DATETIME_FMT_ORA
            }'),
                           TO_DATE('${endDate?.format(SqlGenerationService.DATE_FMT)}', '${
                SqlGenerationService.DATETIME_FMT_ORA
            }'),
                            ${asOfVersionDateString},
                           '${null}',
                            ${runType},
                            ${drugType},
                            ${drugOrVaccine},
                            ${prodFamilyFlag},
                            '${dateType}',
                            ${caseDate},
                            ${followUpExists}, 
                            ${smq},
                            ${includeLockedCases},
                            '${smqNarrowList}',
                            '${smqBroadList}'
                            );
                       end;
                        """

        } else if (dataSource == Constants.DataSource.VIGIBASE &&
                configuration.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {

            def includeLockedCases = 0
            if(isIntegratedVigibase){
                Map dateRange = getVigibaseDateRange()
                if(configuration.alertDateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE){
                    startDate = new Date().parse("dd-MM-yyyy", "01-01-1900")
                } else {
                    startDate = DateUtil.parseDate(dateRange.startDate, Constants.DateFormat.DISPLAY_NEW_DATE)
                }
                endDate = DateUtil.parseDate(dateRange.endDate, Constants.DateFormat.DISPLAY_NEW_DATE)
            }
            popAggDataProc = """
                       begin  pkg_ebgm_data_mining.p_mains(
                           ${runId},
                           TO_DATE('${startDate?.format(SqlGenerationService.DATE_FMT)}', '${
                SqlGenerationService.DATETIME_FMT_ORA
            }'),
                           TO_DATE('${endDate?.format(SqlGenerationService.DATE_FMT)}', '${
                SqlGenerationService.DATETIME_FMT_ORA
            }'),
                            ${asOfVersionDateString},
                           '${null}',
                            ${runType},
                            ${drugType},
                            ${drugOrVaccine},
                            ${prodFamilyFlag},
                            '${dateType}',
                            ${caseDate},
                            ${followUpExists}, 
                            ${smq},
                            ${includeLockedCases},
                            '${smqNarrowList}',
                            '${smqBroadList}'
                            );
                       end;
                        """

        } else {
            def includeLockedCases = configuration.includeLockedVersion ? 1 : 0
            popAggDataProc = """
                       begin pkg_ebgm_data_mining.p_mains(
                           ${runId},
                           TO_DATE('${startDate?.format(SqlGenerationService.DATE_FMT)}', '${
                SqlGenerationService.DATETIME_FMT_ORA
            }'),
                           TO_DATE('${endDate?.format(SqlGenerationService.DATE_FMT)}', '${
                SqlGenerationService.DATETIME_FMT_ORA
            }'),
                            ${asOfVersionDateString},
                            ${null},
                            ${runType},
                            ${drugType},
                            ${drugOrVaccine},
                            ${prodFamilyFlag},
                            '${dateType}',
                            ${caseDate},
                            ${followUpExists}, 
                            ${smq},
                            ${includeLockedCases},
                            '${smqNarrowList}',
                            '${smqBroadList}'
                            );
                       end;
                        """
        }
        log.info(popAggDataProc)
        popAggDataProc
    }


    private initializeGttTables(Configuration configuration, ExecutedConfiguration executedConfiguration, SuperQueryDTO superQueryDTO, Sql sql, long sectionStart, boolean hasQuery, Boolean isAggregate, boolean isReportCumulative = false, Long caseSeriesId = null) {

        log.info("Now executing Version SQL")

        def gttSqlList = []

        sectionStart = System.currentTimeMillis()

        // Add query reasses...
        List<String> reassessPopulateQuerySql = sqlGenerationService.setReassessContextForQuery(configuration, superQueryDTO)
        reassessPopulateQuerySql.each {
            if (it.length() > 0) {
                sql?.call(it)
                gttSqlList.add(it)
            }
        }

        String customFieldPreQueryProcs = sqlGenerationService.selectedFieldsCustomProcedures(configuration.template, superQueryDTO, 1)
        if (customFieldPreQueryProcs) {
            sql?.execute(customFieldPreQueryProcs)
            gttSqlList.add(customFieldPreQueryProcs)
        }

        String insertQueryData = sqlGenerationService.getInsertStatementsToInsert(configuration, superQueryDTO, configuration?.excludeNonValidCases, caseSeriesId && !superQueryDTO.id, true)
        if (insertQueryData) {
            sql?.execute(insertQueryData)
            gttSqlList.add(insertQueryData)
            if (!isAggregate) {
                String createReportSql
                if (caseSeriesId) {
                    createReportSql = "{call PKG_PVS_ALERT_EXECUTION.P_CASE_SERIES_DATA_QL_BR(${executedConfiguration.id})}"
                } else if (isReportCumulative) {
                    createReportSql = "{call PKG_PVS_ALERT_EXECUTION.P_CASE_SERIES_DATA_QL_CUM_RPT(${dataObjectService.getCumCaseSeriesIdFromThread(configuration.id)})}"
                } else {
                    createReportSql = "{call PKG_PVS_ALERT_EXECUTION.P_PERSIST_CASE_SERIES_DATA_QL(${executedConfiguration.id})}"
                }
                sql?.call(createReportSql)
                gttSqlList.add(createReportSql)
            }
        }

        //Additional query insertions added
        if (configuration?.type == SINGLE_CASE_ALERT && !(isReportCumulative || caseSeriesId)) {
            String primarySuspectSql = SignalQueryHelper.primary_suspect_sql()
            sql?.call(primarySuspectSql)
            gttSqlList.add(primarySuspectSql)
        }

        String customFieldPostQueryProcs = sqlGenerationService.selectedFieldsCustomProcedures(configuration.template, superQueryDTO, 2)
        if (customFieldPostQueryProcs) {
            sql?.execute(customFieldPostQueryProcs)
            gttSqlList.add(customFieldPostQueryProcs)
        }

        String reassessPopulateSql = sqlGenerationService.setReassessContextForTemplate(configuration, executedConfiguration, hasQuery, true)
        //==========================================================================================================
        if (reassessPopulateSql?.length() > 0) {
            sql?.call(reassessPopulateSql)
            gttSqlList.add(reassessPopulateSql)
        }

        String customFieldPreReportProcs = sqlGenerationService.selectedFieldsCustomProcedures(configuration.template, superQueryDTO, 3)
        if (customFieldPreReportProcs) {
            sql?.execute(customFieldPreReportProcs)
            gttSqlList.add(customFieldPreReportProcs)
        }
        gttSqlList
    }

    def stopListWithProc(sql) {
        def asl = AlertStopList.list()
        def proc = ""
        for (def alertStopList : asl) {
            if (alertStopList.activated) {
                def productList = []
                def familyList = []
                def ptList = []
                def jsonSlurper = new JsonSlurper()
                def productSelectionObj = jsonSlurper.parseText(alertStopList.productName)

                productSelectionObj.find { k, v ->
                    if (k == "2") {
                        v.each {
                            familyList.add(it.id)
                        }
                    }
                    if (k == "3") {
                        v.each {
                            productList.add(it.id)
                        }
                    }
                }

                def eventSelectionObj = jsonSlurper.parseText(alertStopList.eventName)

                eventSelectionObj.find { k, v ->
                    if (k == "4") {
                        v.each {
                            ptList.add(it.id)
                        }
                    }
                }

                proc = """
                    begin
                    p_single_alert_filter('${productList.flatten().size() > 0 ? productList.flatten().join(",") : ''}',
                                          '${ptList.flatten().size() > 0 ? ptList.flatten().join(",") : ''}',
                                          '${familyList.flatten().size() > 0 ? familyList.flatten().join(",") : ''}');
                    end;
                """

                try {
                    sql?.call(proc)
                } catch (Exception ex) {
                    log.error("Call to p_single_alert_filter failed.")
                    ex.printStackTrace()
                }
            }
        }
        proc
    }

    private List processTemplate(Sql sql, boolean isAggregateCase = false, executedConfigId, String selectedDatasource, boolean isEventGroup = false, boolean isAdhocRun = false, String dataSource = null, boolean isAws = false) {
        List result = []
        if (isAggregateCase) {//For Agg alert
            String customReportSQl = sqlGenerationService.generateCustomReportSQL(executedConfigId, selectedDatasource, isEventGroup, dataSource, isAws)
            List<Map> newFields = []
            if(isAdhocRun){
                newFields = alertFieldService.getAggOnDemandColumnList(Constants.DataSource.PVA,isEventGroup,true)
            }else {
                newFields = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', true)
            }
            String tempSql = ''
            newFields?.each {
                if (it.isNewColumn && it.keyId && it.enabled) {
                    String key = it.keyId.replace("REPORT_","").replace("_FLG","")
                    tempSql = tempSql + "," + "NEW_" + key + "_COUNT" + " AS " + "NEW_" + key + "_COUNT" + " "
                    tempSql = tempSql + "," + "CUMM_" +key+ "_COUNT" + " AS " + "CUMM_" + key + "_COUNT" + " "
                }
            }
            if (newFields != null) {
                customReportSQl = customReportSQl.replaceAll(",#NEW_DYNAMIC_COUNT", tempSql)
            } else {
                customReportSQl = customReportSQl.replaceAll(",#NEW_DYNAMIC_COUNT", ' ')
            }
            result.add(customReportSQl)
        } else {// For single case alert.
            String reportSql = sqlGenerationService.generateCaseLineListingSql(sql, executedConfigId)
            result.add(reportSql)
        }
        return result
    }

    def generateCaseSeries(String seriesName, String caseData, boolean isTemporary = false) {
        User user = userService.getUser()
        try {
            String url = Holders.config.pvreports.url
            String path = Holders.config.pvreports.saveCaseSeriesForSpotfire.uri
            Map query = [user       : user.username,
                         seriesName : seriesName,
                         caseNumbers: caseData,
                         isTemporary: isTemporary]
            return postData(url, path, query, Method.POST)
        } catch (Exception e) {
            e.printStackTrace()
            return null
        }
    }


    private Map postData(String baseUrl, String path, Map query, method) {
        //To do
        //log.info("Requested Parameters : ${baseUrl}, ${path}, ${query}, ${method}")
        Map ret = [:]
        HTTPBuilder http = new HTTPBuilder(baseUrl)
        Map publicToken = reportIntegrationService.fetchPublicToken()

        // perform a POST request, expecting JSON response
        http.request(method, ContentType.JSON) {
            uri.path = path
            requestContentType = ContentType.URLENC
            body = query

            if (publicToken)
                headers = publicToken

            // response handlers
            response.success = { resp, reader -> ret = [status: resp.status, result: reader] }
            response.failure = { resp -> ret = [status: resp.status] }
        }
        log.info("Response : ${ret}")
        return ret
    }


    def clearDataMiningTables(Long scheduledConfigurationId, Long executedConfigId, String dataSource = null) {
        Sql sql = null
        try {
            def beforeTime = System.currentTimeMillis()
            Configuration.withTransaction {
                Configuration config = Configuration.get(scheduledConfigurationId)
                sql = new Sql(signalDataSourceService?.getReportConnection(dataSource ?: config?.selectedDatasource))
                def cleanUpProc = """
                       begin
                            p_drop_objects_alert(${executedConfigId});
                       end;
                        """
                sql?.call(cleanUpProc)
                def afterTime = System.currentTimeMillis()
                log.info("It took " + (afterTime - beforeTime) / 1000 + " seconds to delete the meta tables for execution Id ${executedConfigId}")
            }
        } catch (Throwable throwable) {
            log.error("Error happened when clear data mining table. Not able to automactically recover from this",
                    throwable)
        } finally {
            sql?.close()
        }
    }

    Boolean isFaers(Configuration config) {
        config.selectedDatasource == Constants.DataSource.FAERS
    }

    @Transactional(rollbackFor = Exception)
    ExecutedConfiguration createExecutedConfigurationForAlert(Configuration lockedConfiguration) throws Exception {
        ExecutedConfiguration executedConfiguration = null
        if (lockedConfiguration.isEnabled) {
            log.info("Now Executing the alert : " + lockedConfiguration.name)
            try {
                executedConfiguration = createExecutedConfiguration(lockedConfiguration)
            } catch (Throwable exception) {
                log.error("Error happened when creating executed configuration " +
                        "for ${lockedConfiguration.name}. Skipping", exception)
                throw exception
            }
        }
        executedConfiguration
    }

    void fetchSCADataFromMart(Configuration configuration, ExecutedConfiguration executedConfiguration, String fileName, ExecutionStatus executionStatus) throws Exception {
        ArrayList<Map> alertData = []
        Long sectionStart = System.currentTimeMillis()
        String selectedDatasource = configuration.selectedDatasource
        log.info("selectedDatasource is : " + selectedDatasource)
        Sql sql
        log.info("Generating the sql queries.")
        try {
            SuperQueryDTO superQueryDTO = queryService.queryDetail(configuration.alertQueryId)
            sql = new Sql(signalDataSourceService.getReportConnection(selectedDatasource))
            //Added change for pii policy
            DbUtil.executePIIProcCall(sql, Constants.PII_OWNER, Constants.PII_ENCRYPTION_KEY)
            appAlertProgressStatusService.createAppAlertProgressStatus(executionStatus, executedConfiguration.id, selectedDatasource, 2, 2, System.currentTimeMillis(), AlertProgressExecutionType.DATASOURCE)
            prepareGttsAndVersionSqls(configuration, executedConfiguration, true, false, sql, superQueryDTO, sectionStart, selectedDatasource)
            //The alert stop list flow to remove the cases which are not in use.
            if (configuration.applyAlertStopList) {
                stopListWithProc(sql)
            }
            log.info("Preparing alert data from the mart")
            if (executedConfiguration.alertCaseSeriesId != -1) {
                try {
                    alertData = prepareAlertDataFromMart(sql, false, executedConfiguration, configuration)
                } catch (Exception ex) {
                    log.error("Alert got failed while generating case line listing. "+ ExceptionUtils.getStackTrace(ex))
                    throw new Exception("Alert got failed while generating case line listing. "+ '\n' + ex.toString())
                }
            }
            log.info("Start putting data to file")
            try {
                alertService.saveAlertDataInFile(alertData, fileName)
            } catch (Exception saveFileEx) {
                log.error("Alert got failed while saving alert data in file. "+ ExceptionUtils.getStackTrace(saveFileEx))
                throw new Exception("Alert got failed while saving alert data in file. "+ '\n' + saveFileEx.toString())
            }
            log.info("Data is saved into the file.")
            appAlertProgressStatusService.updateAppAlertProgressStatus(executionStatus.id, executedConfiguration.id, selectedDatasource, 3, 3, System.currentTimeMillis())
        } catch (Throwable throwable) {
            log.error("Alert got failed while fetching single case alert data from mart. "+ ExceptionUtils.getStackTrace(throwable))
            appAlertProgressStatusService.updateAppAlertProgressStatus(executionStatus.id, executedConfiguration.id, selectedDatasource, 2, 1, System.currentTimeMillis())
            List<String> errorMessages = Arrays.asList("file", "case line listing", "gtt", "case series")
            if (org.apache.commons.lang3.StringUtils.isBlank(throwable.getMessage()) || !isMatchedFromException(throwable, errorMessages)) {
                log.error("Alert got failed while fetching single case alert data from mart. ", throwable)
                throw new Exception("Alert got failed while fetching single case alert data from mart. "+ '\n' + throwable.toString())
            }
            throw throwable
        } finally {
            sql?.close()
        }
    }

    void saveSCAData(Configuration configuration, ExecutedConfiguration executedConfiguration, String filePath, ExecutionStatus executionStatus = null) throws Exception {
        List caseVersionList = []
        try {
            log.info("Getting datat list from the file.")
            List<Map> alertDataList = alertService.loadAlertDataFromFile(filePath)
            // For each of the result, we generate the corresponding alert if the template is for alert.
            Collection filteredAlertData = singleCaseAlertService.filterCases(configuration, alertDataList)
            if (configuration.adhocRun) {
                caseVersionList = singleOnDemandAlertService.createAlert(configuration, executedConfiguration, filteredAlertData, executionStatus)
            } else {
                caseVersionList = singleCaseAlertService.createAlert(configuration, executedConfiguration,
                        filteredAlertData, Constants.Commons.BLANK_STRING, false, false, null, executionStatus)
            }
            if (caseVersionList.size() > 0) {
                triggerAlertAtThreshold(configuration, executedConfiguration, caseVersionList)
            }
            File file = new File(filePath)
            if (file.exists()) {
                log.info("Alert is created now deleteing the file.")
                file.delete()
            }
            System.gc()
        } catch (Throwable throwable) {
            if (org.apache.commons.lang3.StringUtils.isBlank(throwable.getMessage())) {
                log.error("Alert got failed while saving single case alert. "+ ExceptionUtils.getStackTrace(throwable))
                throw new Exception("Alert got failed while saving single case alert. "+ '\n' +throwable.toString())
            }
            log.error("Alert got failed while saving single case alert. "+ ExceptionUtils.getStackTrace(throwable))
            throw throwable
        }
    }

    List fetchPrevExConfigs(Long configurationId, Long executedConfigurationId) {
        List<ExecutedConfiguration> prevExConfigs = []
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(executedConfigurationId)
        if (executedConfiguration) {
            List<ExecutedConfiguration> executedConfigurationList = ExecutedConfiguration.findAllByConfigId(configurationId)
            prevExConfigs = executedConfigurationList*.id - executedConfiguration.id
        }
        prevExConfigs
    }

    boolean checkPECsInDB(Long executedConfigurationId, String dataSource){
        Integer numOfCases = 0
        Sql sql = null
        try {
            sql = new Sql(signalDataSourceService.getReportConnection(dataSource))
            String ebgmQuery = "select * from pvs_ebgm_ml_info_${executedConfigurationId}"
            sql.eachRow(ebgmQuery, []) { row ->
                numOfCases = row.TOTAL_PEC_COUNT as Integer
            }
        } catch (Exception ex) {
            throw ex
        } finally {
            sql?.close()
        }
        numOfCases > 0
    }
    boolean checkPecsInDbPrrRor(Long executedConfigurationId, String dataSource){
        Integer numOfCases = 0
        Sql sql = new Sql(signalDataSourceService.getReportConnection(dataSource))
        String ebgmQuery = SignalQueryHelper.check_prr_count_db(executedConfigurationId)
        try {
            sql.eachRow(ebgmQuery, []) { row ->
                numOfCases = row.TOTAL_PEC_COUNT as Integer
            }
            return numOfCases > 0
        }catch(Exception ex){
            throw ex
        }finally{
            sql?.close()
        }
    }

    boolean checkAggCountInDB(Long executedConfigurationId, String dataSource){
        Integer numOfCases = 0
        Sql sql = null
        try {
            sql = new Sql(signalDataSourceService.getReportConnection(dataSource))
            String aggCountQuery = "SELECT count(1) FROM pvs_app_agg_counts_${executedConfigurationId} agg_counts WHERE agg_counts.cumm_count > 0 AND agg_counts.pt_name IS NOT NULL AND ROWNUM = 1"
            sql.eachRow(aggCountQuery, []) { row ->
                numOfCases = row."count(1)" as Integer
            }
        } catch (Exception ex) {
            throw ex
        } finally {
            sql?.close()
        }
        numOfCases > 0
    }


    @Transactional(rollbackFor = Exception)
    List fetchQuantAlertDataFromMart(ExecutionStatus executionStatus, Configuration configuration, ExecutedConfiguration executedConfiguration, String dataSource, boolean isIntegratedVaers = false, boolean isIntegratedVigibase = false) throws Exception {
        if (configuration.isEnabled) {
            Sql sql
            ArrayList<Map> alertData = []
            AppAlertProgressStatus dsAlertProgressStatus = null
            boolean isEligibleForAwsExecution = false // This flag will be used to trigger LAMBDA at AWS for alert execution on AWS
            try {
                SuperQueryDTO superQueryDTO = queryService.queryDetail(configuration.alertQueryId)
                long timeBeforeDB = System.currentTimeMillis()
                isEligibleForAwsExecution = gttInsertionAndDataMining(configuration, executedConfiguration, superQueryDTO, dataSource, isIntegratedVaers, isIntegratedVigibase, dsAlertProgressStatus, executionStatus, false)
                //if any change in function call parameter for gtts done here then it is required to do same in AWS failure scenario too
                Map requireParamForMining = [configuration    : configuration, executedConfiguration: executedConfiguration, superQueryDTO: superQueryDTO, dataSource: dataSource,
                                             isIntegratedVaers: isIntegratedVaers, isIntegratedVigibase: isIntegratedVigibase, dsAlertProgressStatus: dsAlertProgressStatus, executionStatus: executionStatus]

                long timeAfterDB = System.currentTimeMillis()
                log.info(((timeAfterDB - timeBeforeDB) / 1000) + " Secs were taken in fetching the data from database for configuration " + executedConfiguration.name)
                log.info("Checking cases in DB")
                Thread.sleep(1000)
                int alertExecutionTryCount = 0
                String dataStatusMessage = checkDataStatus(dataSource, executedConfiguration, alertExecutionTryCount, requireParamForMining, false)
                log.info("dataStatusMessage "+dataStatusMessage)
                if (dataStatusMessage != Holders.config.alertExecution.status.success.message) {
                    Exception exception = new Exception(dataStatusMessage)
                    throw exception
                }
                boolean isCasesAvail = false
                if (isEligibleForAwsExecution) {
                    log.info("Triggering Lambda now for ${executedConfiguration.name}.")
                    Map body = ['execution_id': executedConfiguration.id, 'alert_name': executedConfiguration.name]
                    aggregateExecutorService.triggerLambdaForExecution(body, Holders.config.aws.aggregate.lambda.arn as String)
                    aggregateExecutorService.isLambdaFlowCompleted(executedConfiguration.id, 0)
                    String query = "select * from glue_catalog.${Holders.config.aws.aggregate.datalake}.pvs_ebgm_ml_info_${executedConfiguration.id}"
                    isCasesAvail = aggregateExecutorService.checkPecsInAWS(query)
                } else {
                    isCasesAvail = checkPECsInDB(executedConfiguration.id, dataSource)
                }

                AppAlertProgressStatus ebgmAppAlertProgressStatus = null
                if(isCasesAvail){
                    log.info("Now calling the EBGM statistics block.")
                    //ebgm progress level check
                    ebgmAppAlertProgressStatus = appAlertProgressStatusService.createAppAlertProgressStatus(executionStatus, executedConfiguration.id, "EBGM", 2, 2, System.currentTimeMillis(), AlertProgressExecutionType.EBGM)
                    def status = statisticsService.calculateStatisticalScores(dataSource, executedConfiguration.id, false)
                    if (!status.status && Holders.config.statistics.enable.ebgm) {
                        appAlertProgressStatusService.updateAppAlertProgressStatus(executionStatus.id, executedConfiguration.id, "EBGM", 2, 1, System.currentTimeMillis())
                        throw new Exception("Alert got failed while EBGM score calculating. "+ '\n' + status.error)
                    }
                } else {
                    log.info("No call on EBGM Stats due to zero cases in DB.")
                }
                boolean isCaseAvailPrrRor = false
                if(dataSource == Constants.DataSource.PVA) {
                    if (isEligibleForAwsExecution) {
                        String query = SignalQueryHelper.check_prr_count_db(executedConfiguration.id, true)
                        isCaseAvailPrrRor = aggregateExecutorService.checkPecsInAWS(query)
                    } else {
                        isCaseAvailPrrRor = checkPecsInDbPrrRor(executedConfiguration.id, dataSource)
                    }
                    Boolean isRor = cacheService.getRorCache()
                    if (isCaseAvailPrrRor && !executedConfiguration?.dataMiningVariable) {
                        boolean prrSubGrpEnabled = cacheService.getSubGroupsEnabled("prrSubGrpEnabled") ?: false
                        if(prrSubGrpEnabled) {
                            def prrStatus = statisticsService.calculateStatisticalScoresPRR(dataSource, executedConfiguration.id, false)
                            if (!prrStatus.status && Holders.config.statistics.enable.prr) {
                                //appAlertProgressStatusService.updateAppAlertProgressStatus(executionStatus.id, executedConfiguration.id, "EBGM", 2, 1, System.currentTimeMillis())
                                throw new Exception("Alert got failed while PRR score calculating. " + '\n' + prrStatus.error)
                            }
                            if(isRor) {
                                def rorStatus = statisticsService.calculateStatisticalScoresROR(dataSource, executedConfiguration.id, false)
                                if (!rorStatus.status && Holders.config.statistics.enable.ror) {
                                    //appAlertProgressStatusService.updateAppAlertProgressStatus(executionStatus.id, executedConfiguration.id, "EBGM", 2, 1, System.currentTimeMillis())
                                    throw new Exception("Alert got failed while ROR score calculating. " + '\n' + rorStatus.error)
                                }
                            }
                        }else{
                            log.info("No call on Safety PRR/ROR Stats due to Sub-Groups Disabled")
                        }
                    } else {
                        log.info("No call on Safety PRR/ROR Stats due to zero cases in DB or DMV alert.")
                    }
                }
                //Once stats scores are calculated then view is polled to fetch the data.
                //prr scores level check
                generatePRRScores(configuration, executedConfiguration, dataSource, executionStatus,isCaseAvailPrrRor, isEligibleForAwsExecution)
                if (isCasesAvail) {
                    generateEbgmData(executedConfiguration, dataSource, executionStatus)
                    boolean isCountPresentInDB = isEligibleForAwsExecution ? aggregateExecutorService.checkAggCountInAWS(executedConfiguration.id) : checkAggCountInDB(executedConfiguration.id, dataSource)
                    if (isCountPresentInDB) {
                        generateDssData(executedConfiguration, dataSource, fetchPrevExConfigs(configuration.id, executedConfiguration.id), executionStatus)
                    }
                    generateABCDData(configuration, executedConfiguration, dataSource, executionStatus)
                }
                populateCriteriaSheetCount(executedConfiguration,dataSource, isEligibleForAwsExecution)
                log.info("fetchQuantAlertDataFromMart executedConfiguration.criteriaCounts :"+executedConfiguration.criteriaCounts)
                sql = new Sql(signalDataSourceService.getReportConnection(dataSource))
                alertData = prepareAlertDataFromMart(sql, true, executedConfiguration, configuration,dataSource, isEligibleForAwsExecution)
            } catch (Throwable throwable) {
                if (Objects.nonNull(throwable) && !isMatchedStringFromException(throwable)) {
                    appAlertProgressStatusService.updateAppAlertProgressStatus(executionStatus.id, executedConfiguration.id, dataSource, null, 1, System.currentTimeMillis())
                }
                throw throwable
            } finally {
                sql?.close()
            }
            alertData
        }
    }

    void saveQuantData(Configuration configuration, ExecutedConfiguration executedConfiguration, String filePath, Map<String, Long> otherDataSourcesExecIds, ExecutionStatus executionStatus) throws Exception {
        try {
            List<String> allFiles = [filePath]
            if (configuration.adhocRun) {
                List<Map> alertDataList = alertService.loadAlertDataFromFile(filePath)
                aggregateOnDemandAlertService.createAlert(configuration.id, executedConfiguration.id, alertDataList)
            } else {
                Map<String, List<Map>> alertDataMap = [:]
                configuration.selectedDatasource.split(',').each {
                    if (otherDataSourcesExecIds.containsKey(it)) {
                        String integratedFilePath = "${grailsApplication.config.signal.alert.file}/${executedConfiguration.id}_${executedConfiguration.type}_${it}"
                        allFiles.add(integratedFilePath)
                        List<Map> alertDataList = alertService.loadAlertDataFromFile(integratedFilePath)
                        alertDataMap.put(it, alertDataList)
                    }
                }
                aggregateCaseAlertService.createAlert(configuration.id, executedConfiguration.id, alertDataMap, otherDataSourcesExecIds, null, null, null, executionStatus)
            }
            for (String fileName : allFiles) {
                File file = new File(fileName)
                if (file.exists()) {
                    file.delete()
                }
            }
            System.gc()
        } catch (Throwable throwable) {
            log.error("Exception came in saving the Quantitative alerts. ", throwable)
            throw throwable
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    void updateExecutionStatus(ExecutionStatus executionStatus, def executedConfiguration, Boolean isMaster = false) {
        executionStatus.executedConfigId = executedConfiguration.id
        if (isMaster)
            executionStatus.alertFilePath = "${grailsApplication.config.signal.alert.file}/${executedConfiguration.id}"
        else
            executionStatus.alertFilePath = "${grailsApplication.config.signal.alert.file}/${executedConfiguration.id}_${executedConfiguration.type}"
        executionStatus.executionLevel = executionStatus.executionLevel + 1
        updateTimeStampJson(executionStatus)
        executionStatus.save(flush: true)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    void updateExecutionStatusLevel(ExecutionStatus executionStatus, MasterExecutedConfiguration executedConfiguration = null, Boolean isSuccess=false) {
        try {
            JsonSlurper jsonSlurper = new JsonSlurper()
            Map timeStamp = new HashMap<String, Long>()
            int level = executionStatus.executionLevel + 1
            if (executionStatus.timeStampJSON)
                timeStamp = jsonSlurper.parseText(executionStatus.timeStampJSON)
            timeStamp.put(level as String, System.currentTimeMillis())
            String timeStampJSON = timeStamp as JSON


            if (!executionStatus.isAttached()) {
                ExecutionStatus.withTransaction {
                    String updateQuery = "update ex_status set EXECUTION_LEVEL = " + level + " , TIME_STAMPJSON = '" + timeStampJSON + "'"
                    if (executedConfiguration) {
                        updateQuery = updateQuery + ", executed_config_id = " + executedConfiguration.id + ", alert_file_path = '" + "${grailsApplication.config.signal.alert.file}/${executedConfiguration.id}" + "'"
                    }
                    if (isSuccess) {
                        updateQuery = updateQuery + ", ex_status = '" + ReportExecutionStatus.COMPLETED + "', end_time = " + System.currentTimeMillis() + ""
                    }
                    updateQuery = updateQuery + " where id = " + executionStatus.id
                    log.info(updateQuery)
                    SQLQuery sql = null
                    Session session = sessionFactory.currentSession
                    sql = session.createSQLQuery(updateQuery)
                    sql.executeUpdate()
                    session.flush()
                    session.clear()
                }
            } else {
                executionStatus.executionLevel = level
                executionStatus.timeStampJSON = timeStampJSON
                if (isSuccess) {
                    executionStatus.executionStatus = ReportExecutionStatus.COMPLETED
                    executionStatus.endTime = System.currentTimeMillis()
                }
                if (executedConfiguration) {
                    executionStatus.executedConfigId = executedConfiguration.id
                    executionStatus.alertFilePath = "${grailsApplication.config.signal.alert.file}/${executedConfiguration.id}"
                }
                executionStatus.save(flush: true)
            }
        }catch(Exception ex){
            ex.printStackTrace()
        }
    }

        void updateTimeStampJson(ExecutionStatus executionStatus) {
            JsonSlurper jsonSlurper = new JsonSlurper()
            Map timeStamp = new HashMap<String, Long>()
            if (executionStatus.timeStampJSON)
                timeStamp = jsonSlurper.parseText(executionStatus.timeStampJSON)
            timeStamp.put(executionStatus.executionLevel?.toString(), System.currentTimeMillis())
            executionStatus.timeStampJSON = timeStamp as JSON
        }

        void startIntegratedReviewExecution(String alertType, Configuration lockedConfiguration, List<Long> runningIdsList) {
            boolean isException = false
            runningIdsList.add(lockedConfiguration.id)
            log.info("Found ${lockedConfiguration.name} [${lockedConfiguration.id}] to execute.")

            ExecutionStatus executionStatus
            if (lockedConfiguration.isResume) {
                Long exConfigId = ExecutionStatus.createCriteria().get(){
                    eq("configId", lockedConfiguration.id)
                    or{
                        eq("executionStatus",ReportExecutionStatus.SCHEDULED)
                        eq("reportExecutionStatus",ReportExecutionStatus.SCHEDULED)
                        eq("spotfireExecutionStatus",ReportExecutionStatus.SCHEDULED)
                    }
                    order("id", "desc")
                    maxResults(1)
                }.executedConfigId

                executionStatus = ExecutionStatus.findByConfigIdAndExecutedConfigIdAndType(lockedConfiguration.id,
                        exConfigId, alertType)

                lockedConfiguration.executing = true
                if (executionStatus?.executionStatus != ReportExecutionStatus.COMPLETED) {
                    executionStatus?.executionStatus = ReportExecutionStatus.GENERATING
                    if (lockedConfiguration.templateQueries.size()) {
                        executionStatus?.reportExecutionStatus = ReportExecutionStatus.SCHEDULED
                    }
                    if (lockedConfiguration.spotfireSettings) {
                        executionStatus?.spotfireExecutionStatus = ReportExecutionStatus.SCHEDULED
                    }
                } else {
                    setNextRunDateForConfiguration(lockedConfiguration)
                }
                if (executionStatus)
                    executionStatus.save(flush: true)
            } else {
                lockedConfiguration.executing = true
                CRUDService.updateWithAuditLog(lockedConfiguration)
                executionStatus = createExecutionStatus(lockedConfiguration)
            }
            try {
                if(lockedConfiguration.selectedDatasource == Constants.DataSource.JADER){
                    jaderExecutorService.startJaderAlertExecutionByLevel(executionStatus)
                }else {
                    startQuantAlertExecutionByLevel(executionStatus)
                }

            } catch (Throwable throwable) {
                log.error("Exception in runConfigurations", throwable)
                ExecutionStatusException ese = new ExecutionStatusException(alertService.exceptionString(throwable))
                handleFailedExecution(ese, lockedConfiguration.id, executionStatus.executedConfigId, alertType, executionStatus.id)
                isException = true
            } finally {
                runningIdsList.remove(lockedConfiguration.id)
                if (!isException) {
                    clearDataMiningTables(lockedConfiguration.id, executionStatus?.executedConfigId, lockedConfiguration.selectedDatasource.split(',')[0])
                }
            }
        }
    void startAlertBurstingExecution(String alertType, Configuration lockedConfiguration, List<Long> runningIdsList) {
        boolean isException = false
        runningIdsList.add(lockedConfiguration.id)
        log.info("Found Master Template Alert: ${lockedConfiguration.name} [${lockedConfiguration.id}] to execute.")

        ExecutionStatus executionStatus
        lockedConfiguration.executing = true
        CRUDService.updateWithAuditLog(lockedConfiguration)
        executionStatus = createExecutionStatus(lockedConfiguration)
        try {
            startAlertBurstingExecutionByLevels(executionStatus)
        } catch (Throwable throwable) {
            log.error("Exception in runConfigurations", throwable)
            ExecutionStatusException ese = new ExecutionStatusException(alertService.exceptionString(throwable))
            handleFailedExecution(ese, lockedConfiguration.id, executionStatus.executedConfigId, alertType, executionStatus.id)
            isException = true
        } finally {
            runningIdsList.remove(lockedConfiguration.id)
            if (!isException) {
                clearDataMiningTables(lockedConfiguration.id, executionStatus?.executedConfigId, lockedConfiguration.selectedDatasource.split(',')[0])
            }
        }
    }
    // Added for PVS-55617
    void updateLinkedConfigurationForAttachedAlert(Long lockedConfigurationId){
        def signalId = cacheService.getSignalId(lockedConfigurationId)
        if(signalId && signalId != "null") {
            validatedSignalService.addConfigurationToSignal(signalId,lockedConfigurationId)
            cacheService.clearSignalIdFromCache(lockedConfigurationId)
        }
    }

        @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception)
        def saveEvdasDateRange(String dateRange, Long id) {
            ExecutedConfiguration.executeUpdate("Update ExecutedConfiguration set evdasDateRange = :evdasDateRange where id = :id", [evdasDateRange: dateRange, id: id])
        }

        boolean gttInsertionAndDataMining(Configuration configuration, ExecutedConfiguration executedConfiguration, SuperQueryDTO superQueryDTO, String dataSource,
                                boolean isIntegratedVaers = false, boolean isIntegratedVigibase = false, AppAlertProgressStatus dsAlertProgressStatus,
                                ExecutionStatus executionStatus, Boolean isAWSfailed = false) throws Exception {

            if (configuration && executedConfiguration) {
                Long sectionStart = System.currentTimeMillis()
                Sql sql
                boolean isEligibleForAWS = false
                if (!configuration.isResume && !isAWSfailed) {
                    dsAlertProgressStatus = appAlertProgressStatusService.createAppAlertProgressStatus(executionStatus, executedConfiguration.id, dataSource, 0, 0, System.currentTimeMillis(), AlertProgressExecutionType.DATASOURCE)
                    log.info("dsAlertProgressStatus"+dsAlertProgressStatus)
                }
                try {
                    log.info("selectedDatasource is : $dataSource")
                    sql = new Sql(signalDataSourceService.getReportConnection(dataSource))
                    log.info("Generating the sql queries.")
                    isEligibleForAWS = prepareGttsAndVersionSqls(configuration, executedConfiguration, true, true, sql, superQueryDTO, sectionStart, dataSource, isIntegratedVaers, isIntegratedVigibase, false, null, false, null, null, null, [], isAWSfailed)
                    try {
                        log.info("Call to p_persist_case_series_data is made.")
                        String caseSeriesProc = sqlGenerationService.persistCaseSeriesExecutionData(executedConfiguration.id)
                        sql.call(caseSeriesProc)
                        log.info("Call to p_persist_case_series_data is ended.")
                    } catch(Throwable ex) {
                        log.error("Alert got failed while generating case series. "+ ExceptionUtils.getStackTrace(ex))
                        throw new Exception("Alert got failed while generating case series. "+ '\n' + ex.toString())
                    }
                    try {
                        log.info("pkg_ebgm_data_mining package calling started.")
                        log.info(new Date().toString())
                        String popAggDataProc = initiateAggregateDataMining(executedConfiguration, configuration, dataSource, isIntegratedVaers, false, isIntegratedVigibase)
                        sql.call(popAggDataProc)
                        log.info(new Date().toString())
                        log.info("pkg_ebgm_data_mining package calling ended.")
                    } catch (Throwable dataMiningEx) {
                        log.error("Alert got failed while executing the mining run. "+ ExceptionUtils.getStackTrace(dataMiningEx))
                        throw new Exception("Alert got failed while executing the mining run. "+ '\n' + dataMiningEx.toString())
                    }
                    return isEligibleForAWS

                } catch (Throwable th) {
                    appAlertProgressStatusService.updateAppAlertProgressStatus(executionStatus.id, executedConfiguration.id, dataSource, 2, 1, System.currentTimeMillis())
                    if (org.apache.commons.lang3.StringUtils.isBlank(th.getMessage())) {
                        log.error("Alert got failed while executing the mining run. ", th)
                        throw new Exception("Alert got failed while executing the mining run. " + '\n' + th.toString())
                    }
                    log.error("Alert got failed with specific message for executing mining run", th)
                    throw th
                } finally {
                    sql?.close()
                }
            }
        }

    void generatePRRScores(Configuration configuration, ExecutedConfiguration executedConfiguration, String dataSource, ExecutionStatus executionStatus,boolean isCaseAvailPrrRor, boolean isEligibleForAwsExecution = false) {
            Boolean callPrrRorFlow
            if ((dataSource == Constants.DataSource.PVA) &&
                    (Holders.config.statistics.enable.prr || Holders.config.statistics.enable.ror)) {
                callPrrRorFlow = true
            } else if ((dataSource == Constants.DataSource.FAERS) &&
                    (Holders.config.statistics.faers.enable.prr || Holders.config.statistics.faers.enable.ror)) {
                callPrrRorFlow = true
            } else if ((dataSource == Constants.DataSource.VAERS) &&
                    (Holders.config.statistics.vaers.enable.prr || Holders.config.statistics.vaers.enable.ror)) {
                callPrrRorFlow = true
            } else if ((dataSource == Constants.DataSource.VIGIBASE) &&
                    (Holders.config.statistics.vigibase.enable.prr || Holders.config.statistics.vigibase.enable.ror)) {
                callPrrRorFlow = true
            } else if ((dataSource == Constants.DataSource.JADER) &&
                    (Holders.config.statistics.jader.enable.prr || Holders.config.statistics.jader.enable.ror)) {
                callPrrRorFlow = true
            }

            if (callPrrRorFlow) {
                AppAlertProgressStatus prrAppAlertProgressStatus = appAlertProgressStatusService.createAppAlertProgressStatus(executionStatus, executedConfiguration.id, "PRR", 2, 2, System.currentTimeMillis(), AlertProgressExecutionType.PRR)
                Sql sql = null
                try {
                    sql = new Sql(signalDataSourceService.getReportConnection(dataSource))
                    //Fetch the PRR values
                    if(dataSource == Constants.DataSource.PVA && isCaseAvailPrrRor) {
                        statisticsService.getStatsPRRData(sql, executedConfiguration.id, configuration, isEligibleForAwsExecution)
                        statisticsService.getStatsRORData(sql, executedConfiguration.id, configuration, isEligibleForAwsExecution)
                    }else if(dataSource != Constants.DataSource.PVA){
                        statisticsService.getStatsPrrRorDataForOtherDataSource(sql, executedConfiguration.id, configuration)
                    }
                    appAlertProgressStatusService.updateAppAlertProgressStatus(executionStatus.id, executedConfiguration.id, "PRR", 3, 3, System.currentTimeMillis())
                } catch (Throwable th) {
                    appAlertProgressStatusService.updateAppAlertProgressStatus(executionStatus.id, executedConfiguration.id, "PRR", 2, 1, System.currentTimeMillis())
                    throw new Exception("Alert got failed while fetching PRR values. "+  '\n'  + th.toString())
                } finally {
                    sql?.close()
                }
            }
        }

    void generateABCDData(Configuration configuration, ExecutedConfiguration executedConfiguration, String dataSource, ExecutionStatus executionStatus){
        Boolean callABCDFlow
        if ((dataSource == Constants.DataSource.PVA) &&
                Holders.config.statistics.enable.ebgm) {
            callABCDFlow = true
        } else if ((dataSource == Constants.DataSource.FAERS) &&
                Holders.config.statistics.faers.enable.ebgm) {
            callABCDFlow = true
        } else if ((dataSource == Constants.DataSource.VAERS) &&
                Holders.config.statistics.vaers.enable.ebgm) {
            callABCDFlow = true
        } else if ((dataSource == Constants.DataSource.VIGIBASE) &&
                Holders.config.statistics.vigibase.enable.ebgm) {
            callABCDFlow = true
        } else if ((dataSource == Constants.DataSource.JADER) &&
                Holders.config.statistics.jader.enable.ebgm) {
            callABCDFlow = true
        }

        if (callABCDFlow) {
            AppAlertProgressStatus abcdAppAlertProgressStatus = appAlertProgressStatusService.createAppAlertProgressStatus(executionStatus, executedConfiguration.id, "ABCD", 2, 2, System.currentTimeMillis(), AlertProgressExecutionType.ABCD)
            Sql sql = null
            try {
                sql = new Sql(signalDataSourceService.getReportConnection(dataSource))
                //Fetch the PRR values
                statisticsService.getStatsABCDData(sql, executedConfiguration.id, configuration)
                appAlertProgressStatusService.updateAppAlertProgressStatus(executionStatus.id, executedConfiguration.id, "ABCD", 3, 3, System.currentTimeMillis())
            } catch (Throwable th) {
                appAlertProgressStatusService.updateAppAlertProgressStatus(executionStatus.id, executedConfiguration.id, "ABCD", 2, 1, System.currentTimeMillis())
                throw new Exception("Alert got failed while fetching ABCD values. "+  '\n'  + th.toString())
            } finally {
                sql?.close()
            }
        }
    }

    void generateEbgmData(ExecutedConfiguration executedConfiguration, String dataSource, ExecutionStatus executionStatus = null) {
        Boolean ebgmFlow
        if ((dataSource == Constants.DataSource.PVA) &&
                Holders.config.statistics.enable.ebgm) {
            ebgmFlow = true
        } else if ((dataSource == Constants.DataSource.FAERS) &&
                Holders.config.statistics.faers.enable.ebgm) {
            ebgmFlow = true
        } else if ((dataSource == Constants.DataSource.VAERS) &&
                Holders.config.statistics.vaers.enable.ebgm) {
            ebgmFlow = true
        } else if ((dataSource == Constants.DataSource.JADER) &&
                Holders.config.statistics.jader.enable.ebgm) {
            ebgmFlow = true
        }

        if (ebgmFlow) {
            Sql sql = null
            try {
                sql = new Sql(signalDataSourceService.getReportConnection(dataSource))
                //Fetch the ebgm values
                statisticsService.getStatsEbgmData(sql, executedConfiguration.id, dataSource)
                appAlertProgressStatusService.updateAppAlertProgressStatus(executionStatus.id, executedConfiguration.id, "EBGM", 3, 3, System.currentTimeMillis())
            } catch (Throwable th) {
                appAlertProgressStatusService.updateAppAlertProgressStatus(executionStatus.id, executedConfiguration.id, "EBGM", 2, 1, System.currentTimeMillis())
                throw new Exception("Alert got failed while fetching EBGM values. "+ '\n' + th.toString())
            } finally {
                sql?.close()
            }
        }
    }

    void generateDssData(ExecutedConfiguration executedConfiguration, String dataSource, List prevExConfigs, ExecutionStatus executionStatus) {
        Boolean dssFlow
        if ((dataSource == Constants.DataSource.PVA) &&
                Holders.config.statistics.enable.dss) {
            dssFlow = true
        }

        if (dssFlow) {
            AppAlertProgressStatus dssAppAlertProgressStatus = appAlertProgressStatusService.createAppAlertProgressStatus(executionStatus, executedConfiguration.id, "DSS", 2, 2, System.currentTimeMillis(), AlertProgressExecutionType.DSS)
            Sql sql = null
            try {
                statisticsService.sendApiCallForDssScores2(executedConfiguration.id, prevExConfigs)
                sql = new Sql(signalDataSourceService.getReportConnection(dataSource))
                //Fetch the dss values
                statisticsService.getStatsDssData(sql, executedConfiguration.id, dataSource)
                appAlertProgressStatusService.updateAppAlertProgressStatus(executionStatus.id, executedConfiguration.id, "DSS", 3, 3, System.currentTimeMillis())
            } catch (Exception ex) {
                appAlertProgressStatusService.updateAppAlertProgressStatus(executionStatus.id, executedConfiguration.id, "DSS", 2, 1, System.currentTimeMillis())
                throw new Exception("Alert got failed while fetching DSS values. "+ '\n' + ex.toString())
            } finally {
                sql?.close()
            }
        }
    }



    def getFaersDateRange() {
            String date
            String startDate
            String endDate
            String faersDate
            Sql faersSql
            try {
                faersSql = new Sql(dataSource_faers)
                String faers_statement = SignalQueryHelper.faers_date_range()
                faersSql.eachRow(faers_statement, []) { resultSetObj ->
                    date = resultSetObj
                }
                int lenght = date?.size() ?: 0
                String year = date?.substring(lenght - 5, lenght - 1)
                if (date?.contains('MAR')) {
                    startDate = '01-01-' + year
                    endDate = '31-03-' + year
                    faersDate = '01-Jan-' + year + ' - 31-Mar-' + year
                } else if (date?.contains('JUN')) {
                    startDate = '01-04-' + year
                    endDate = '30-06-' + year
                    faersDate = '01-Apr-' + year + ' - 30-Jun-' + year
                } else if (date?.contains('SEP')) {
                    startDate = '01-07-' + year
                    endDate = '30-09-' + year
                    faersDate = '01-Jul-' + year + ' - 30-Sep-' + year
                } else if (date?.contains('DEC')) {
                    startDate = '01-10-' + year
                    endDate = '31-12-' + year
                    faersDate = '01-Oct-' + year + ' - 31-Dec-' + year
                }
            } catch (Exception ex) {
                ex.printStackTrace()
            } finally {
                faersSql.close()
            }


            return [startDate: startDate, endDate: endDate, faersDate: faersDate]
        }

    String getEvdasDateRange(List productGroupIds) {
        String evdasDataJson
        String evdasDateRange
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        EvdasFileProcessLog evdasFileProcessLog
        Sql evdasSql
        try {
            evdasSql = new Sql(dataSource_eudra)
            evdasSql.eachRow("select GRP_DATA from VW_DICT_GRP_DATA where DICT_GRP_ID IN (${productGroupIds.join(",")})") { ResultSet resultSetObj ->
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
                evdasDateRange = sdf.format(new Date()) + " - " + sdf.format(new Date() + 10)
            }else{
                evdasDateRange = sdf.format(evdasFileProcessLog.recordStartDate) + " - " + sdf.format(evdasFileProcessLog.recordEndDate)
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        } finally {
            evdasSql?.close()
        }
        return evdasDateRange
    }
    /**
     * Retrieves the VAERS date range.
     *
     * This method queries the VAERS database to fetch the latest uploaded date range, parses the retrieved date range, and formats it for display.
     *
     * The date range is expected to be in the format "LATEST_UPLOADED_DATE_RANGE:dd-MM-yyyy/dd-MM-yyyy".
     *
     * The method extracts the start and end dates from this range, formats them appropriately, and returns a map containing these values.
     *
     * @return A map containing the following key-value pairs:
     *         - startDate: The start date of the VAERS date range in "dd-MM-yyyy" format.
     *         - endDate: The end date of the VAERS date range in "dd-MM-yyyy" format.
     *         - vaersDate: The formatted VAERS date range in "dd-MM-yyyy - dd-MM-yyyy" format.
     */

    def getVaersDateRange() {
        String date
        String startDate
        String endDate
        String vaersDate
        Sql vaersSql
        Date vaersEndDate
        Date vaersStartDate
        try {
            vaersSql = new Sql(dataSource_vaers)
            String vaers_statement = SignalQueryHelper.vaers_date_range()
            vaersSql.eachRow(vaers_statement, []) { resultSetObj ->
                date = resultSetObj
            }
            //Updated Vaers Date Range for Integrated Alert PVS-67898
            int lastChar = date.lastIndexOf(':')
            String vaersDateString = lastChar != Constants.Commons.UNDEFINED_NUM_INT_REVIEW ? date.substring(lastChar + 1, date.length() - 1) : ""
            String[] vaersDateRange = vaersDateString.split("/")
            String vaersStart = ""
            String vaersEnd = ""
            if (vaersDateRange?.size() == 2) {
                vaersStart = vaersDateRange[0].trim()
                vaersEnd = vaersDateRange[1].trim()
            }
            if (vaersEnd != "" || vaersEnd != "") {
                vaersEndDate = DateUtil.parseDate(vaersEnd, Constants.DateFormat.DISPLAY_NEW_DATE)
                vaersStartDate = DateUtil.parseDate(vaersStart, Constants.DateFormat.DISPLAY_NEW_DATE)
                endDate = DateUtil.toDateStringWithoutTimezoneForAnyFormat(vaersEndDate, Constants.DateFormat.DISPLAY_NEW_DATE)
                startDate = DateUtil.toDateStringWithoutTimezoneForAnyFormat(vaersStartDate, Constants.DateFormat.DISPLAY_NEW_DATE)
                vaersDate = DateUtil.toDateStringWithoutTimezone(vaersStartDate) + ' - ' + DateUtil.toDateStringWithoutTimezone(vaersEndDate)
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        } finally {
            vaersSql?.close()
        }
        return [startDate: startDate, endDate: endDate, vaersDate: vaersDate]
    }

        def getVigibaseDateRange() {
            String date
            String startDate
            String endDate
            String vigibaseDate
            Sql vigibaseSql
            try {
                vigibaseSql = new Sql(dataSource_vigibase)
                String vigibase_statement = SignalQueryHelper.vigibase_date_range_display()
                vigibaseSql.eachRow(vigibase_statement, []) { resultSetObj ->
                    date = resultSetObj
                }
                int lastChar = date?.lastIndexOf(':')
                String vigibaseDateString = date?.substring(lastChar + 1, date?.length() - 1)
                Date vigibaseEndDate = DateUtil.parseDate(vigibaseDateString, Constants.DateFormat.DISPLAY_NEW_DATE)
                Date vigibaseStartDate = DateUtil.parseDate(vigibaseDateString, Constants.DateFormat.DISPLAY_NEW_DATE) - 90
                endDate = DateUtil.toDateStringWithoutTimezoneForAnyFormat(vigibaseEndDate, Constants.DateFormat.DISPLAY_NEW_DATE)
                startDate = DateUtil.toDateStringWithoutTimezoneForAnyFormat(vigibaseStartDate, Constants.DateFormat.DISPLAY_NEW_DATE)
                vigibaseDate = DateUtil.toDateStringWithoutTimezone(vigibaseStartDate) + ' - ' + DateUtil.toDateStringWithoutTimezone(vigibaseEndDate)
            } catch (Exception ex) {
                ex.printStackTrace()
            } finally {
                vigibaseSql?.close()
            }


            return [startDate: startDate, endDate: endDate, vigibaseDate: vigibaseDate]
        }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception)
        Configuration createConfigForIntegratedReview(Configuration configuration, Configuration configurationFaers) {
            Date reviewDate = null

        if(!configuration.isAdhocRun()) {
            use(TimeCategory) {
                reviewDate = new Date() + (configuration.reviewPeriod).day
            }
        }
            Date startDate = configuration.alertDateRangeInformation?.getReportStartAndEndDate()[0] ?: new Date()
            Date endDate = configuration.alertDateRangeInformation?.getReportStartAndEndDate()[1] ?: new Date()
            AlertDateRangeInformation alertDateRangeInformation = configurationFaers?.alertDateRangeInformation
            if (!alertDateRangeInformation) {
                alertDateRangeInformation = new AlertDateRangeInformation(
                        dateRangeEnum: configuration.alertDateRangeInformation?.dateRangeEnum,
                        executedAsOfVersionDate: configuration.getAsOfVersionDateCustom() ?: endDate)
            }
            alertDateRangeInformation.dateRangeEnum = configuration.alertDateRangeInformation?.dateRangeEnum
            if (configuration.alertDateRangeInformation?.dateRangeEnum == DateRangeEnum.CUSTOM) {
                Map dateRange = getFaersDateRange()
                alertDateRangeInformation.relativeDateRangeValue = configuration.alertDateRangeInformation?.relativeDateRangeValue
                alertDateRangeInformation.dateRangeStartAbsolute = new Date().parse(DateUtil.DATETIME_FMT, dateRange.startDate + " 00:00:01")
                alertDateRangeInformation.dateRangeEndAbsolute = new Date().parse(DateUtil.DATETIME_FMT, dateRange.endDate + " 00:00:01")
            } else {
                alertDateRangeInformation.relativeDateRangeValue = configuration.alertDateRangeInformation?.relativeDateRangeValue
                alertDateRangeInformation.dateRangeEndAbsolute = endDate
                alertDateRangeInformation.dateRangeStartAbsolute = new Date().parse(DateUtil.DATETIME_FMT, "01-01-1900 00:00:01")
            }

            configurationFaers.setProperties([name                          : configuration.name, workflowGroup: configuration.workflowGroup,
                                              owner                         : User.get(configuration.owner.id), scheduleDateJSON: configuration.scheduleDateJSON, nextRunDate: null,
                                              description                   : configuration.description, dateCreated: configuration.dateCreated, lastUpdated: configuration.lastUpdated,
                                              isPublic                      : configuration.isPublic, isDeleted: configuration.isDeleted, isEnabled: configuration.isEnabled,
                                              dateRangeType                 : configuration.dateRangeType, productGroups: configuration.productGroups, spotfireSettings: null,
                                              productGroupSelection         : configuration.productGroupSelection, eventSelection: configuration.eventSelection, studySelection: configuration.studySelection,
                                              configSelectedTimeZone        : configuration.configSelectedTimeZone, evaluateDateAs: configuration.evaluateDateAs,
                                              productDictionarySelection    : configuration.productDictionarySelection, limitPrimaryPath: configuration.limitPrimaryPath,
                                              includeMedicallyConfirmedCases: configuration.includeMedicallyConfirmedCases, excludeFollowUp: configuration.excludeFollowUp,
                                              includeLockedVersion          : configuration.includeLockedVersion, adjustPerScheduleFrequency: configuration.adjustPerScheduleFrequency,
                                              isAutoTrigger                 : configuration.isAutoTrigger, adhocRun: configuration.adhocRun, reviewDueDate: reviewDate ?: new Date(),
                                              createdBy                     : configuration.getOwner().username, modifiedBy: configuration.modifiedBy, selectedDatasource: Constants.DataSource.FAERS,
                                              executionStatus               : ReportExecutionStatus.COMPLETED, numOfExecutions: configuration.numOfExecutions + 1, excludeNonValidCases: configuration.excludeNonValidCases,
                                              alertQueryName                : configuration.alertQueryName, executedAlertQueryId: configuration.alertQueryId, alertDateRangeInformation: alertDateRangeInformation,
                                              priority                      : configuration.priority, alertTriggerCases: configuration.alertTriggerCases, alertTriggerDays: configuration.alertTriggerDays,
                                              alertRmpRemsRef               : configuration.alertRmpRemsRef, onOrAfterDate: configuration.onOrAfterDate, asOfVersionDate: configuration.asOfVersionDate,
                                              applyAlertStopList            : configuration.applyAlertStopList, suspectProduct: configuration.suspectProduct, missedCases: false, eventGroupSelection: configuration.eventGroupSelection, integratedConfigurationId: configuration?.id])

            configurationFaers.type = configuration.type
            configurationFaers.groupBySmq = configuration.groupBySmq
            configurationFaers.assignedTo = configuration.assignedTo
            configurationFaers.assignedToGroup = configuration.assignedToGroup
            configurationFaers.isEnabled = true
            configurationFaers.isDeleted = true
            configurationFaers.template = configuration.template
            configurationFaers.drugType = configuration.drugType
            configurationFaers.shareWithUser?.clear()
            configurationFaers.shareWithGroup?.clear()
            configuration.shareWithUser.each { User usr ->
                configurationFaers.addToShareWithUser(usr)
            }
            configuration.shareWithGroup.each { Group grp ->
                configurationFaers.addToShareWithGroup(grp)
            }
            configurationFaers.skipAudit=true
            CRUDService.saveWithAuditLog(configurationFaers)
            configurationFaers

        }

        @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception)
        Configuration createConfigForIntegratedReviewVaers(Configuration configuration, Configuration configurationVaers) {
            Date reviewDate = null
            if(!configuration.isAdhocRun()){
                use(TimeCategory) {
                    reviewDate = new Date() + (configuration.reviewPeriod).day
                }
            }
            Date startDate = configuration.alertDateRangeInformation?.getReportStartAndEndDate()[0] ?: new Date()
            Date endDate = configuration.alertDateRangeInformation?.getReportStartAndEndDate()[1] ?: new Date()
            AlertDateRangeInformation alertDateRangeInformation = configurationVaers?.alertDateRangeInformation
            if (!alertDateRangeInformation) {
                alertDateRangeInformation = new AlertDateRangeInformation(
                        dateRangeEnum: configuration.alertDateRangeInformation?.dateRangeEnum,
                        executedAsOfVersionDate: configuration.getAsOfVersionDateCustom() ?: endDate)
            }
            alertDateRangeInformation.dateRangeEnum = configuration.alertDateRangeInformation?.dateRangeEnum
            alertDateRangeInformation.relativeDateRangeValue = configuration.alertDateRangeInformation?.relativeDateRangeValue
            Map dateRange = getVaersDateRange()
            if (configuration.alertDateRangeInformation?.dateRangeEnum == DateRangeEnum.CUMULATIVE) {
                alertDateRangeInformation.dateRangeEndAbsolute = new Date().parse(DateUtil.DATETIME_FMT, dateRange.endDate + " 00:00:01")
                alertDateRangeInformation.dateRangeStartAbsolute = new Date().parse(DateUtil.DATETIME_FMT, "01-01-1900 00:00:01")
            } else if (configuration.alertDateRangeInformation?.dateRangeEnum == DateRangeEnum.CUSTOM) {
                alertDateRangeInformation.dateRangeEndAbsolute = endDate
                alertDateRangeInformation.dateRangeStartAbsolute = startDate
            } else {
                alertDateRangeInformation.dateRangeStartAbsolute = new Date().parse(DateUtil.DATETIME_FMT, dateRange.startDate + " 00:00:01")
                alertDateRangeInformation.dateRangeEndAbsolute = new Date().parse(DateUtil.DATETIME_FMT, dateRange.endDate + " 00:00:01")
            }

            configurationVaers.setProperties([name                          : configuration.name, workflowGroup: configuration.workflowGroup,
                                              owner                         : User.get(configuration.owner.id), scheduleDateJSON: configuration.scheduleDateJSON, nextRunDate: null,
                                              description                   : configuration.description, dateCreated: configuration.dateCreated, lastUpdated: configuration.lastUpdated,
                                              isPublic                      : configuration.isPublic, isDeleted: configuration.isDeleted, isEnabled: configuration.isEnabled,
                                              dateRangeType                 : configuration.dateRangeType, productGroups: configuration.productGroups, spotfireSettings: null,
                                              productGroupSelection         : configuration.productGroupSelection, eventSelection: configuration.eventSelection, studySelection: configuration.studySelection,
                                              configSelectedTimeZone        : configuration.configSelectedTimeZone, evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION,
                                              productDictionarySelection    : configuration.productDictionarySelection, limitPrimaryPath: configuration.limitPrimaryPath,
                                              includeMedicallyConfirmedCases: configuration.includeMedicallyConfirmedCases, excludeFollowUp: configuration.excludeFollowUp,
                                              includeLockedVersion          : configuration.includeLockedVersion, adjustPerScheduleFrequency: configuration.adjustPerScheduleFrequency,
                                              isAutoTrigger                 : configuration.isAutoTrigger, adhocRun: configuration.adhocRun, reviewDueDate: reviewDate ?: new Date(),
                                              createdBy                     : configuration.getOwner().username, modifiedBy: configuration.modifiedBy, selectedDatasource: Constants.DataSource.VAERS,
                                              executionStatus               : ReportExecutionStatus.COMPLETED, numOfExecutions: configuration.numOfExecutions + 1, excludeNonValidCases: configuration.excludeNonValidCases,
                                              alertQueryName                : configuration.alertQueryName, executedAlertQueryId: configuration.alertQueryId, alertDateRangeInformation: alertDateRangeInformation,
                                              priority                      : configuration.priority, alertTriggerCases: configuration.alertTriggerCases, alertTriggerDays: configuration.alertTriggerDays,
                                              alertRmpRemsRef               : configuration.alertRmpRemsRef, onOrAfterDate: configuration.onOrAfterDate, asOfVersionDate: configuration.asOfVersionDate,
                                              applyAlertStopList            : configuration.applyAlertStopList, suspectProduct: configuration.suspectProduct, missedCases: false, eventGroupSelection: configuration.eventGroupSelection, integratedConfigurationId: configuration?.id])

            configurationVaers.type = configuration.type
            configurationVaers.groupBySmq = configuration.groupBySmq
            configurationVaers.assignedTo = configuration.assignedTo
            configurationVaers.assignedToGroup = configuration.assignedToGroup
            configurationVaers.isEnabled = true
            configurationVaers.isDeleted = true
            configurationVaers.template = configuration.template
            configurationVaers.drugType = configuration.drugType
            configurationVaers.shareWithUser?.clear()
            configurationVaers.shareWithGroup?.clear()
            configuration.shareWithUser.each { User usr ->
                configurationVaers.addToShareWithUser(usr)
            }
            configuration.shareWithGroup.each { Group grp ->
                configurationVaers.addToShareWithGroup(grp)
            }
            configurationVaers.skipAudit=true
            CRUDService.saveWithAuditLog(configurationVaers)
            configurationVaers

        }

        @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception)
        Configuration createConfigForIntegratedReviewVigibase(Configuration configuration , Configuration configurationVigibase){
            Date reviewDate = null

            if(!configuration.isAdhocRun()){
                use(TimeCategory) {
                    reviewDate = new Date() + (configuration.reviewPeriod).day
                }
            }

            Date startDate = configuration.alertDateRangeInformation?.getReportStartAndEndDate()[0] ?: new Date()
            Date endDate = configuration.alertDateRangeInformation?.getReportStartAndEndDate()[1] ?: new Date()
            AlertDateRangeInformation alertDateRangeInformation = configurationVigibase?.alertDateRangeInformation
            if(!alertDateRangeInformation) {
                alertDateRangeInformation = new AlertDateRangeInformation(
                        dateRangeEnum: configuration.alertDateRangeInformation?.dateRangeEnum,
                        executedAsOfVersionDate: configuration.getAsOfVersionDateCustom() ?: endDate)
            }
            alertDateRangeInformation.dateRangeEnum = configuration.alertDateRangeInformation?.dateRangeEnum
            if (configuration.alertDateRangeInformation?.dateRangeEnum == DateRangeEnum.CUSTOM) {
                Map dateRange = getVigibaseDateRange()
                alertDateRangeInformation.relativeDateRangeValue = configuration.alertDateRangeInformation?.relativeDateRangeValue
                alertDateRangeInformation.dateRangeStartAbsolute = new Date().parse(DateUtil.DATETIME_FMT, dateRange.startDate + " 00:00:01")
                alertDateRangeInformation.dateRangeEndAbsolute = new Date().parse(DateUtil.DATETIME_FMT, dateRange.endDate + " 00:00:01")
            } else {
                alertDateRangeInformation.relativeDateRangeValue = configuration.alertDateRangeInformation?.relativeDateRangeValue
                alertDateRangeInformation.dateRangeEndAbsolute = endDate
                alertDateRangeInformation.dateRangeStartAbsolute = new Date().parse(DateUtil.DATETIME_FMT, "01-01-1900 00:00:01")
            }

            configurationVigibase.setProperties([name                       : configuration.name, workflowGroup: configuration.workflowGroup,
                                                 owner                         : User.get(configuration.owner.id), scheduleDateJSON: configuration.scheduleDateJSON, nextRunDate: null,
                                                 description                   : configuration.description, dateCreated: configuration.dateCreated, lastUpdated: configuration.lastUpdated,
                                                 isPublic                      : configuration.isPublic, isDeleted: configuration.isDeleted, isEnabled: configuration.isEnabled,
                                                 dateRangeType                 : configuration.dateRangeType, productGroups: configuration.productGroups, spotfireSettings: null,
                                                 productGroupSelection         : configuration.productGroupSelection, eventSelection: configuration.eventSelection, studySelection: configuration.studySelection,
                                                 configSelectedTimeZone        : configuration.configSelectedTimeZone, evaluateDateAs: configuration.evaluateDateAs,
                                                 productDictionarySelection    : configuration.productDictionarySelection, limitPrimaryPath: configuration.limitPrimaryPath,
                                                 includeMedicallyConfirmedCases: configuration.includeMedicallyConfirmedCases, excludeFollowUp: configuration.excludeFollowUp,
                                                 includeLockedVersion          : configuration.includeLockedVersion, adjustPerScheduleFrequency: configuration.adjustPerScheduleFrequency,
                                                 isAutoTrigger                 : configuration.isAutoTrigger, adhocRun: configuration.adhocRun, reviewDueDate: reviewDate ?: new Date(),
                                                 createdBy                     : configuration.getOwner().username, modifiedBy: configuration.modifiedBy, selectedDatasource: Constants.DataSource.VIGIBASE,
                                                 executionStatus               : ReportExecutionStatus.COMPLETED, numOfExecutions: configuration.numOfExecutions + 1, excludeNonValidCases: false,
                                                 alertQueryName                : configuration.alertQueryName, executedAlertQueryId: configuration.alertQueryId, alertDateRangeInformation: alertDateRangeInformation,
                                                 priority                      : configuration.priority, alertTriggerCases: configuration.alertTriggerCases, alertTriggerDays: configuration.alertTriggerDays,
                                                 alertRmpRemsRef               : configuration.alertRmpRemsRef, onOrAfterDate: configuration.onOrAfterDate, asOfVersionDate: configuration.asOfVersionDate,
                                                 applyAlertStopList            : configuration.applyAlertStopList, suspectProduct: configuration.suspectProduct, missedCases: false, eventGroupSelection: configuration.eventGroupSelection, integratedConfigurationId: configuration?.id])

            configurationVigibase.type = configuration.type
            configurationVigibase.groupBySmq = configuration.groupBySmq
            configurationVigibase.assignedTo = configuration.assignedTo
            configurationVigibase.assignedToGroup = configuration.assignedToGroup
            configurationVigibase.isEnabled = true
            configurationVigibase.isDeleted = true
            configurationVigibase.template = configuration.template
            configurationVigibase.drugType = configuration.drugType
            configurationVigibase.shareWithUser?.clear()
            configurationVigibase.shareWithGroup?.clear()
            configuration.shareWithUser.each { User usr ->
                configurationVigibase.addToShareWithUser(usr)
            }
            configuration.shareWithGroup.each { Group grp ->
                configurationVigibase.addToShareWithGroup(grp)
            }
            configurationVigibase.skipAudit=true
            CRUDService.saveWithAuditLog(configurationVigibase)
            configurationVigibase

        }

        def clearDataMiningTablesOnResume(Long executedConfigId, String dataSource) {
            Sql sql = null
            try {
                def beforeTime = System.currentTimeMillis()
                Configuration.withTransaction {
                    sql = new Sql(signalDataSourceService.getReportConnection(dataSource))
                    def cleanUpProc = """
                       begin
                            p_drop_objects_alert_all_data(${executedConfigId});
                       end;
                        """
                    sql?.call(cleanUpProc)
                    def afterTime = System.currentTimeMillis()
                    log.info("It took " + (afterTime - beforeTime) / 1000 + " seconds to delete the meta tables for resume execution")
                }
            } catch (Throwable throwable) {
                log.error("Error happened when clear data mining table. Not able to automactically recover from this",
                        throwable)
            } finally {
                sql?.close()
            }
        }


        String generateProductName(ExecutedConfiguration executedConfiguration) {
            String products = ''
            StringJoiner joinProducts = new StringJoiner(",")
            Configuration configuration = Configuration.get(executedConfiguration.configId)
            String productSelection = ViewHelper.getDictionaryValues(configuration, DictionaryTypeEnum.PRODUCT)
            String productGroupSelection = ViewHelper.getDictionaryValues(configuration, DictionaryTypeEnum.PRODUCT_GROUP)
            if (pvsProductDictionaryService.isLevelGreaterThanProductLevel(executedConfiguration)) {
                products = pvsProductDictionaryService.fetchProductNamesfromUpperHierarchy(executedConfiguration.productSelection)
            } else {
                productSelection ? joinProducts.add(productSelection) : ''
                productGroupSelection ? joinProducts.add(productGroupSelection) : ''
                products = joinProducts.toString()
            }
            products
        }

        void generateCummCaseSeriesInBackground(Configuration configuration, ExecutedConfiguration executedConfiguration) {
            Long cumExecConfigId = validatedSignalService.getNext(false)
            Promise promise = task {
                dataObjectService.setCumCaseSeriesThreadMap(configuration.id, new CumThreadInfoDTO(cumExecConfigId: cumExecConfigId))
                singleCaseAlertService.generateCummCaseSeries(configuration.id, executedConfiguration.id)
            }
            promise.onError { Throwable th ->
                dataObjectService.setCummCaseSeriesGeneratedMap(configuration.id, false)
                clearDataMiningTables(configuration.id, cumExecConfigId)
            }
            promise.onComplete { boolean isReportingCompleted ->
                dataObjectService.setCummCaseSeriesGeneratedMap(configuration.id, isReportingCompleted)
                clearDataMiningTables(configuration.id, cumExecConfigId)
            }
        }

        void interruptCumulativeThread(Long configId) {
            CumThreadInfoDTO cumThreadInfoDTO = dataObjectService.getCumCaseSeriesThreadFromMap(configId)
            if (cumThreadInfoDTO) {
                dataObjectService.clearCummCaseSeriesGeneratedMap(configId)
                dataObjectService.removeCumCaseSeriesThread(configId)
                cumThreadInfoDTO.sql?.close()
                cumThreadInfoDTO.caseSeriesSql?.close()
            }
        }

        private adjustAsOfVersion(Configuration configuration, ExecutedConfiguration executedConfiguration) {
            def runOnce = null
            def hourly = null


            if (JSON.parse(configuration.scheduleDateJSON).recurrencePattern) {
                runOnce = JSON.parse(configuration.scheduleDateJSON)?.recurrencePattern?.contains(Constants.Scheduler.RUN_ONCE)
                hourly = JSON.parse(configuration.scheduleDateJSON)?.recurrencePattern?.contains(Constants.Scheduler.HOURLY)
            }
            if (!runOnce && !hourly) {
                if (configuration.asOfVersionDate && configuration.nextRunDate != null) {
                    configuration.asOfVersionDate = configurationService.getUpdatedAsOfVersionDate(configuration)
                }
            }
        }

    private Date getAdjustAsOfVersionForFutureScheduleAlert(Configuration configuration, Date currentAsOfVersionDate) {
        Date adjustedAsOfVersionDate = null
        JSONObject oldtimeObject = JSON.parse(configuration.scheduleDateJSON)
        JSONObject newtimeObject = JSON.parse(configuration.futureScheduleDateJSON)
        if (oldtimeObject.startDateTime && oldtimeObject.timeZone && oldtimeObject.recurrencePattern) {
            String dateFormat = "yyyy-MM-dd'T'HH:mmXXX"
            Date oldStartDate = Date.parse(dateFormat, oldtimeObject.startDateTime)
            int differenceDays = DateUtil.getDifferenceDays(oldStartDate, currentAsOfVersionDate)
            Date newStartDate = Date.parse(dateFormat, newtimeObject.startDateTime)
            adjustedAsOfVersionDate = newStartDate + differenceDays
        }
        return adjustedAsOfVersionDate
    }

        Map prevMasterExConfigs(MasterConfiguration masterConfiguration,MasterExecutedConfiguration masterExecutedConfiguration) {
            List<ExecutedConfiguration> prevExConfigs = []
            Long prevExConfigId
            if (masterExecutedConfiguration) {
                List<MasterExecutedConfiguration> masterExecutedConfigurationList = MasterExecutedConfiguration.findAllByMasterConfigId(masterConfiguration.id)
                List<Long> prevMasterConfigs = masterExecutedConfigurationList*.id - masterExecutedConfiguration.id
                if(prevMasterConfigs) {
                    prevExConfigId = prevMasterConfigs.max()
                    prevExConfigs = ExecutedConfiguration.findAllByMasterExConfigId(prevExConfigId)
                }
            }

            [prevExConfigs: prevExConfigs, prevExConfigId: prevExConfigId]
        }

    List fetchEbgmData(Long executedConfigId, String selectedDatasource) {
        List ebgmData = []
        String ebgmSql = "select * FROM pvs_ebgm_output_${executedConfigId}"
        //String ebgmSql = "select * FROM pvs_ebgm_output_9990997"
        Sql sql = null
        try {
            sql = new Sql(signalDataSourceService.getReportConnection(selectedDatasource))

            sql.eachRow(ebgmSql) { GroovyResultSet resultSet ->
                Map map = [:]
                resultSet.toRowResult().eachWithIndex { it, i ->
                    def value = ""
                    if (it.value instanceof Clob) {
                        //Handle Clob data
                        value = it.value.asciiStream.text
                    } else {
                        value = it.value
                    }
                    map.put(it.key, value)
                }
                ebgmData.add(map)
            }
        } catch (Exception ex) {
            log.error(ex.printStackTrace())
        } finally {
            sql?.close()
        }
        return ebgmData
    }

    boolean checkProductGroupUpdateStatus(){
        PvsAppConfiguration pvsAppConfiguration = PvsAppConfiguration.findByKey(Constants.PRODUCT_GROUP_UPDATE)
        return pvsAppConfiguration?.booleanValue
    }

    boolean isClearDataMiningRequired(Long executedConfigurationId, String selectedDatasource) {
        boolean isClearDataMining = false
        int keyValue = 0
        Sql sql = null
        ExecutedConfiguration executedConfiguration = null
        Date lastUpdated
        if (Objects.nonNull(executedConfigurationId)) {
            executedConfiguration = ExecutedConfiguration.findById(executedConfigurationId)
        }
        if (executedConfiguration) {
            lastUpdated = executedConfiguration?.lastUpdated
        } else {
            MasterExecutedConfiguration masterExecutedConfiguration = MasterExecutedConfiguration.findById(executedConfigurationId)
            lastUpdated = masterExecutedConfiguration?.lastUpdated
        }
        if (Objects.nonNull(lastUpdated) && org.apache.commons.lang3.StringUtils.isNotBlank(selectedDatasource)) {
            try {
                sql = new Sql(signalDataSourceService.getReportConnection(selectedDatasource))
                String query = SignalQueryHelper.alert_resumption_limit_hours()
                sql.eachRow(query) { ResultSet resultSetObj ->
                    keyValue = resultSetObj.getInt("KEY_VALUE")
                }

                Date runLimitDate = DateUtils.addHours(lastUpdated, keyValue)
                int compareResult = new Date().compareTo(runLimitDate)
                if (compareResult > 0) {
                    isClearDataMining = true
                    clearDataMiningTablesOnErrorJob(executedConfigurationId, selectedDatasource, isClearDataMining)
                }
            } catch (Throwable th) {
                th.printStackTrace()
            } finally {
                sql?.close()
            }
        }
        return isClearDataMining
    }

    boolean isDataSourceRequiredForFreshRun(ExecutionStatus executionStatus, String selectedDataSource) {
        boolean isFreshRun = false
        if (Objects.nonNull(executionStatus) && org.apache.commons.lang3.StringUtils.isNotBlank(selectedDataSource)) {
            AppAlertProgressStatus appAlertProgressStatus = AppAlertProgressStatus.findByExStatusIdAndName(executionStatus?.id, selectedDataSource)
            if (Objects.nonNull(appAlertProgressStatus)) {
                if (appAlertProgressStatus.finalStatus == 1) {
                    isFreshRun = isEtlSuccessCheck(executionStatus, selectedDataSource)
                }
            } else {
                isFreshRun = true
            }
        }
        return isFreshRun
    }

    boolean isEtlSuccessCheck(ExecutionStatus executionStatus, String selectedDatasource) {
        boolean isFreshRun = false
        Date lastSuccessETL
        if (Objects.nonNull(executionStatus) && org.apache.commons.lang3.StringUtils.isNotBlank(selectedDatasource)) {
            Sql sql = null
            try {
                sql = new Sql(signalDataSourceService.getReportConnection(selectedDatasource))
                String query = SignalQueryHelper.select_etl_alert_sql()
                sql.eachRow(query) { ResultSet resultSetObj ->
                    lastSuccessETL = resultSetObj.getDate("FINISH_DATETIME")
                }

                if (lastSuccessETL) {
                    Date alertFailureDate = executionStatus.lastUpdated
                    if (alertFailureDate <= lastSuccessETL) {
                        isFreshRun = true
                    }
                }
            } catch (Exception e) {
                log.error(e.printStackTrace())
            } finally {
                sql?.close()
            }
        }
        return  isFreshRun
    }

    void clearErrorAlertData() {
        Boolean isClearDataMining = false
        int batchSize = 10000 // Define the batch size

        List<ExecutionStatus> executionStatuses = ExecutionStatus.findAllByExecutionStatusAndIsClearDataMining(
                ReportExecutionStatus.ERROR,
                isClearDataMining,
                [max: batchSize]  // Limit the query to 10,000 results
        )
        List<Long> exStatusIdsBatch = new ArrayList<>()
        executionStatuses.each { exStatus ->
                callClearDataMining(exStatus.id, exStatus.executedConfigId, exStatusIdsBatch)
        }

        // Update the execution status list for the current batch
        if (exStatusIdsBatch.size() > 0) {
                updateExecutionStatusList(exStatusIdsBatch)
        }
    }

    void callClearDataMining(Long exStatusId, Long exConfigId, List<Long> exStatusIds) {
        boolean isClearDataMining = false
        if (Objects.nonNull(exStatusId)) {
            List<AppAlertProgressStatus> appAlertProgressStatusList = AppAlertProgressStatus.findAllByExStatusIdAndType(exStatusId, AlertProgressExecutionType.DATASOURCE)
            // All the DB Temp tables should be deleted after defined time interval even if alert fails at application side
            if (appAlertProgressStatusList) {
                appAlertProgressStatusList.each { appAlertProgressStatus ->
                    isClearDataMining = isClearDataMiningRequired(appAlertProgressStatus.executedConfigId, appAlertProgressStatus.name)
                }
            } else {
                ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(exConfigId)
                if (executedConfiguration) {
                    String selectedDataSource = executedConfiguration.selectedDatasource
                    List<String> dataSourcesList = selectedDataSource?.split(',')
                    dataSourcesList.each { dataSource ->
                        isClearDataMining = isClearDataMiningRequired(exConfigId, dataSource)

                    }
                } else {
                    MasterExecutedConfiguration masterExecutedConfiguration = MasterExecutedConfiguration.get(exConfigId)
                    String selectedDataSource = (masterExecutedConfiguration?.datasource) ?: "pva"
                    isClearDataMining = isClearDataMiningRequired(exConfigId, selectedDataSource)
                }
            }
            if (isClearDataMining) {
                exStatusIds.add(exStatusId)
            }
        }
    }

    def pUpdateAlertMetadataMaster(Long executedConfigId, String dataSource) {
        Sql sql = null
        try {
            def beforeTime = System.currentTimeMillis()
            Configuration.withTransaction {
                sql = new Sql(signalDataSourceService.getReportConnection(dataSource))
                def updateMetaDataProc = """
                       begin
                            p_update_alert_metadata_master(${executedConfigId});
                       end;
                        """
                sql?.call(updateMetaDataProc)
                def afterTime = System.currentTimeMillis()
                log.info("It took " + (afterTime - beforeTime) / 1000 + " seconds to update the meta tables")
            }
        } catch (Throwable throwable) {
            log.error("Error happened when calling p_update_alert_metadata_master after getting db success final status",
                    throwable)
        } finally {
            sql?.close()
        }
    }

    void fetchUpdateAlertDBProgress() {
        List<ExecutionStatus> executionStatuses = ExecutionStatus.findAllByExecutionStatusAndTypeNotEqual(ReportExecutionStatus.GENERATING, SINGLE_CASE_ALERT)
        List<Long> exeStatusIds = null
        if (!executionStatuses.isEmpty()) {
            exeStatusIds = new ArrayList<>()
            executionStatuses.forEach{exStatus -> exeStatusIds.add(exStatus.id)}
            List<String> dataSources = grailsApplication.config.pvsignal.supported.datasource.call()
            for (String dataSource : dataSources) {
                if (!Constants.DataSource.EUDRA.equals(dataSource)) {
                    List<AppAlertProgressStatus> appAlertProgressStatusList = null
                    if (Objects.nonNull(exeStatusIds) && exeStatusIds.size() < 1000) {
                        appAlertProgressStatusList = AppAlertProgressStatus.findAllByExStatusIdInListAndName(exeStatusIds, dataSource)
                    } else {
                        appAlertProgressStatusList = AppAlertProgressStatus.createCriteria().list {
                            if (exeStatusIds) {
                                exeStatusIds.collate(1000).each {
                                    'in'("exStatusId", it)
                                }
                            }
                            and {
                                eq('name', dataSource)
                            }

                        }
                    }
                    if (!appAlertProgressStatusList.isEmpty()) {
                        def exConfigIds = (appAlertProgressStatusList?.collect { it.executedConfigId }).join(",")
                        checkDBMartStatus(dataSource, exConfigIds)
                    }
                }
            }
        }
    }

    void checkDBMartStatus(String datasource, def exConfigIds) {
        Sql sql = null
        int status = 0
        int progressPct = 0
        long executionId = 0l
        boolean dataSourceEnabled = "Holders.config.signal."+datasource+".enabled"
        if (dataSourceEnabled) {
            try {
                sql = new Sql(signalDataSourceService.getReportConnection(datasource))
                String query = SignalQueryHelper.multiple_alert_db_progress_status_sql(exConfigIds)
                sql.eachRow(query) { ResultSet resultSetObj ->
                    executionId = resultSetObj.getLong("EXECUTION_ID")
                    status = resultSetObj.getInt("CURRENT_STATUS")
                    progressPct = resultSetObj.getInt("PROGRESS_PCT")
                    appAlertProgressStatusService.updateAppAlertProgressStatusWithOutExStatusId(executionId, datasource, progressPct, status, System.currentTimeMillis())
                }
            } catch (Throwable th) {
                th.printStackTrace()
            } finally {
                sql?.close()
            }
        }

    }

    private boolean isMatchedStringFromException(Throwable throwable) {
        List<String> errorMessages = Arrays.asList("EBGM", "PRR", "DSS", "ABCD")
        if (Objects.nonNull(throwable) ) {
            for (String error : errorMessages) {
                if (throwable.getMessage() != null && throwable.getMessage().contains(error)) {
                    return true
                }
            }
        }
        return false
    }

    private boolean isMatchedFromException(Throwable throwable, List<String> errorMessages) {
        if (Objects.nonNull(throwable) ) {
            for (String error : errorMessages) {
                if (throwable.getMessage() != null && throwable.getMessage().contains(error)) {
                    return true
                }
            }
        }
        return false
    }

    void clearCompletedAlertProgressData() {
        Sql sql = null
        try {
                Long exStatusId
                sql = new Sql(dataSource)
                String query = SignalQueryHelper.execution_completed_status_detail()
                sql.eachRow(query) { ResultSet resultSetObj ->
                    exStatusId = resultSetObj.getLong("ID")
                    callClearAppAlertProgressStatus(exStatusId)
                }

        } catch (Throwable throwable) {
            log.error("Exception occured while deleting completed Alert Progress Data :"+ExceptionUtils.getStackTrace(throwable))
        } finally {
            sql?.close()
        }
    }

    private void callClearAppAlertProgressStatus(Long exStatusId) {
        if (Objects.nonNull(exStatusId)) {
            appAlertProgressStatusService.deleteAppAlertProgressStatus(exStatusId)
        }
    }

    def clearDataMiningTablesOnErrorJob(Long executedConfigId, String dataSource, Boolean isClearDataMining = true) {
        Sql sql = null
        try {
            def beforeTime = System.currentTimeMillis()
            Configuration.withTransaction {
                sql = new Sql(signalDataSourceService.getReportConnection(dataSource))
                def cleanUpProc = """
                       begin
                            p_drop_objects_alert(${executedConfigId});
                       end;
                        """
                sql?.call(cleanUpProc)
                def afterTime = System.currentTimeMillis()
                log.info("It took " + (afterTime - beforeTime) / 1000 + " seconds to delete the meta tables for clear error alert execution job")
            }
        } catch (Throwable throwable) {
            log.error("Error happened when clear data mining table. Not able to automactically recover from this",
                    throwable)
            isClearDataMining = false
        } finally {
            sql?.close()
        }
    }
    void populateCriteriaSheetCount(ExecutedConfiguration ec, String dataSource, boolean isEligibleForAWS = false){
        String criteriaCountJson
        List<Long> exConfigIds = []
        exConfigIds.add(ec.id as Long)
        if(dataSource == Constants.DataSource.PVA){
            if (isEligibleForAWS) {
                String criteriaCountQuery = SignalQueryHelper.criteria_sheet_count(ec.id, true)
                criteriaCountJson = aggregateExecutorService.fetchCriteriaCountFromAWS(criteriaCountQuery)
            } else {
                criteriaCountJson = fetchCriteriaCount(exConfigIds, dataSource)
            }
        } else if (dataSource == Constants.DataSource.FAERS){
            criteriaCountJson = fetchCriteriaCountFaers(exConfigIds, dataSource)
        } else if (dataSource == Constants.DataSource.VAERS){
            criteriaCountJson = fetchCriteriaCountVaers(exConfigIds, dataSource)
        } else if (dataSource == Constants.DataSource.VIGIBASE){
            criteriaCountJson = fetchCriteriaCountVigiBase(exConfigIds,dataSource)
        } else {
            criteriaCountJson = fetchCriteriaCountJader(exConfigIds,dataSource)
        }
        ec.criteriaCounts = criteriaCountJson
    }

    String fetchCriteriaCount(List<Long> executedConfigurationId, String dataSource){
        Map countMap = [:]
        Sql sql = new Sql(signalDataSourceService.getReportConnection(dataSource))
        String criteriaCountQuery = SignalQueryHelper.criteria_sheet_count(executedConfigurationId)
        try {
            sql.eachRow(criteriaCountQuery, []) { row ->
                countMap['Alert Level New Count'] = row.NEW_TOTAL_COUNT ?:0
                countMap['Alert Level Cumulative Count'] = row.CUMM_TOTAL_COUNT ?:0
                countMap['Alert Level New Study Count'] = row.STUDY_TOTAL_NEW_COUNT ?:0
                countMap['Alert Level Cumulative Study Count'] = row.STUDY_TOTAL_CUMM_COUNT ?: 0
            }
            String jsonString = countMap? JsonOutput.toJson(countMap): null
            return jsonString
        } catch (Exception ex){
            ex.printStackTrace()
        } finally{
            sql?.close()
        }
    }
    String fetchCriteriaCountFaers(List<Long> executedConfigurationId,String dataSource){
        Map countMap = [:]
        Sql sql = new Sql(signalDataSourceService.getReportConnection(dataSource))
        String criteriaCountQuery = SignalQueryHelper.criteria_sheet_count(executedConfigurationId)
        try {
            sql.eachRow(criteriaCountQuery, []) { row ->
                countMap['Alert Level New Count (F)'] = row.NEW_TOTAL_COUNT ?:0
                countMap['Alert Level Cumulative Count (F)'] = row.CUMM_TOTAL_COUNT ?:0
            }
            String jsonString = countMap? JsonOutput.toJson(countMap): null
            return jsonString
        } catch (Exception ex){
            ex.printStackTrace()
        } finally{
            sql?.close()
        }
    }
    String fetchCriteriaCountVaers(List<Long> executedConfigurationId,String dataSource){
        Map countMap = [:]
        Sql sql = new Sql(signalDataSourceService.getReportConnection(dataSource))
        String criteriaCountQuery = SignalQueryHelper.criteria_sheet_count(executedConfigurationId)
        try {
            sql.eachRow(criteriaCountQuery, []) { row ->
                countMap['Alert Level New Count (VA)'] = row.NEW_TOTAL_COUNT ?:0
                countMap['Alert Level Cumulative Count (VA)'] = row.CUMM_TOTAL_COUNT ?:0
            }
            String jsonString = countMap? JsonOutput.toJson(countMap): null
            return jsonString
        } catch (Exception ex){
            ex.printStackTrace()
        } finally{
            sql?.close()
        }
    }
    String fetchCriteriaCountVigiBase(List<Long> executedConfigurationId,String dataSource){
        Map countMap = [:]
        Sql sql = new Sql(signalDataSourceService.getReportConnection(dataSource))
        String criteriaCountQuery = SignalQueryHelper.criteria_sheet_count(executedConfigurationId)
        try {
            sql.eachRow(criteriaCountQuery, []) { row ->
                countMap['Alert Level New Count (VB)'] = row.NEW_TOTAL_COUNT ?: 0
                countMap['Alert Level Cumulative Count (VB)'] = row.CUMM_TOTAL_COUNT ?:0
            }
            String jsonString = countMap? JsonOutput.toJson(countMap): null
            return jsonString
        } catch (Exception ex){
            ex.printStackTrace()
        } finally{
            sql?.close()
        }
    }
    String fetchCriteriaCountJader(List<Long> executedConfigurationId,String dataSource){
        Map countMap = [:]
        Sql sql = new Sql(signalDataSourceService.getReportConnection(dataSource))
        String criteriaCountQuery = SignalQueryHelper.criteria_sheet_count(executedConfigurationId)
        try {
            sql.eachRow(criteriaCountQuery, []) { row ->
                countMap['alertLevelNewCountJader'] = row.NEW_TOTAL_COUNT ?: 0
                countMap['alertLevelCumCountJader'] = row.CUMM_TOTAL_COUNT ?:0
            }
            String jsonString = countMap? JsonOutput.toJson(countMap): null
            return jsonString
        } catch (Exception ex){
            ex.printStackTrace()
        } finally{
            sql?.close()
        }
    }

    Map fetchCriteriaCountChangeLogPVA(List<Long> executedConfigurationId, String dataSource){
        Map countMap = [:]
        Sql sql = new Sql(signalDataSourceService.getReportConnection(dataSource))
        String criteriaCountQuery = SignalQueryHelper.criteria_sheet_count(executedConfigurationId)
        Map cumulativeCountMap = [:]
        try {
            sql.eachRow(criteriaCountQuery, []) { row ->
                countMap = [:]
                countMap['Alert Level New Count'] = row.NEW_TOTAL_COUNT ?:0
                countMap['Alert Level Cumulative Count'] = row.CUMM_TOTAL_COUNT ?:0
                countMap['Alert Level New Study Count'] = row.STUDY_TOTAL_NEW_COUNT ?:0
                countMap['Alert Level Cumulative Study Count'] = row.STUDY_TOTAL_CUMM_COUNT ?: 0
                cumulativeCountMap[row.EXECUTION_ID] = countMap
            }
            return cumulativeCountMap ?: null
        } catch (Exception ex){
            ex.printStackTrace()
        } finally{
            sql?.close()
        }
    }

    Map fetchCriteriaCountChangelogFaers(List<Long> executedConfigurationId, String dataSource){
        Map countMap = [:]
        Sql sql = new Sql(signalDataSourceService.getReportConnection(dataSource))
        String criteriaCountQuery = SignalQueryHelper.criteria_sheet_count(executedConfigurationId)
        Map cumulativeCountMap = [:]
        try {
            sql.eachRow(criteriaCountQuery, []) { row ->
                countMap = [:]
                countMap['Alert Level New Count (F)'] = row.NEW_TOTAL_COUNT ?:0
                countMap['Alert Level Cumulative Count (F)'] = row.CUMM_TOTAL_COUNT ?:0
                cumulativeCountMap[row.EXECUTION_ID] = countMap
            }
            return cumulativeCountMap ?: null
        } catch (Exception ex){
            ex.printStackTrace()
        } finally{
            sql?.close()
        }
    }
    Map fetchCriteriaCountVaersChangelog(List<Long> executedConfigurationId,String dataSource){
        Map countMap = [:]
        Sql sql = new Sql(signalDataSourceService.getReportConnection(dataSource))
        String criteriaCountQuery = SignalQueryHelper.criteria_sheet_count(executedConfigurationId)
        Map cumulativeCountMap = [:]
        try {
            sql.eachRow(criteriaCountQuery, []) { row ->
                countMap = [:]
                countMap['Alert Level New Count (VA)'] = row.NEW_TOTAL_COUNT ?:0
                countMap['Alert Level Cumulative Count (VA)'] = row.CUMM_TOTAL_COUNT ?:0
                cumulativeCountMap[row.EXECUTION_ID] = countMap
            }
            return cumulativeCountMap ?: null
        } catch (Exception ex){
            ex.printStackTrace()
        } finally{
            sql?.close()
        }
    }
    Map fetchCriteriaCountVigiBaseChangelog(List<Long> executedConfigurationId,String dataSource){
        Map countMap = [:]
        Sql sql = new Sql(signalDataSourceService.getReportConnection(dataSource))
        String criteriaCountQuery = SignalQueryHelper.criteria_sheet_count(executedConfigurationId)
        Map cumulativeCountMap = [:]
        try {
            sql.eachRow(criteriaCountQuery, []) { row ->
                countMap = [:]
                countMap['Alert Level New Count (VB)'] = row.NEW_TOTAL_COUNT ?: 0
                countMap['Alert Level Cumulative Count (VB)'] = row.CUMM_TOTAL_COUNT ?:0
                cumulativeCountMap[row.EXECUTION_ID] = countMap
            }
            return cumulativeCountMap ?: null
        } catch (Exception ex){
            ex.printStackTrace()
        } finally{
            sql?.close()
        }
    }

    Boolean checkApiForAWSDb(ExecutedConfiguration executedConfiguration, String db) {
        Boolean success = false
        try {
            def url = Holders.config.awsdb.api.url
            def path = Holders.config.awsdb.api.path
            Integer prr_ror = (Holders.config.statistics."$db".enable.prr || Holders.config.statistics."$db".enable.ror) ? 1 : 0
            Integer ebgm = (Holders.config.statistics."$db".enable.ebgm) ? 1 : 0
            def query = [run_id: executedConfiguration.id, prr_ror: prr_ror, ebgm: ebgm, alert_name: executedConfiguration.name, dataSource: executedConfiguration?.selectedDatasource]
            log.info("Call to API for aws db started with ${query}.")
            Map result = reportIntegrationService.get(url, path, query)

            log.info("Response of API for aws call : ${result.toString()}")

            if (result?.get('data')?.get('ResponseMetadata')?.get('HTTPStatusCode') == 200) {
                success = true
            }
            log.info("Call to API for aws db ended.")
        } catch (Exception exception) {
            exception.printStackTrace()
        }
        return success
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception)
    def updateExecutionStatusList(List<Long> executionStatusIds) {
        int batchSize = 1000  // Define the batch size
        List<List<Long>> batches = executionStatusIds.collate(batchSize)  // Split the list into batches

        Session session = sessionFactory.currentSession

        try {
            for (List<Long> batch : batches) {
                // Construct the update query for the current batch
                String updateQuery = "UPDATE ex_status SET IS_CLEAR_DATA_MINING = true WHERE id IN (:batchIds)"
                log.info("Update Query for exstatus with isClearDataMining: " + updateQuery)
                SQLQuery sql = session.createSQLQuery(updateQuery)
                sql.setParameterList("batchIds", batch)
                sql.executeUpdate()
            }
        } catch (Exception ex) {
            log.error("Error updating execution status list: $ex.message")
        } finally {
            if (session != null && session.isOpen()) {
                session.flush()
                session.clear()
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception)
    def createConfigurationsAndExConfigurationsBasedOnPVA(Configuration masterTemplateConfig = null, ExecutedConfiguration exMasterTemplateConfig = null) {
        try {
            log.info("Fetching Respective Product Assignments.")
            List productViewAssignments
            Map productAssignmentDataGroupedByAlertGroup
            String hierarchyName = JSON.parse(masterTemplateConfig.productHierarchyMasterTmplt).values().first()
            productViewAssignments = fetchAllProductViewAssignments(hierarchyName.trim(), masterTemplateConfig)
            productAssignmentDataGroupedByAlertGroup = groupProductViewAssignmentDataAccordingToAlertGroup(productViewAssignments)
            log.info("Respective Product Assignments Fetched.")
            if (!productAssignmentDataGroupedByAlertGroup)
                throw new Exception("No Assignments Found for Alert Bursting.")
            log.info("Child Configuration generation Started.")
            productAssignmentDataGroupedByAlertGroup.each { alertGroup, assignments ->
                createConfigurationAndExecutedConfigurationBasedOnAssignments(assignments, masterTemplateConfig, exMasterTemplateConfig, alertGroup)
            }
            log.info("Child Configuration generation Ended.")

        } catch (Exception ex) {
            ex.printStackTrace()
            throw new Exception("Exception occured while creating Config and Ex Config for Master Template CONFIG_ID: ${masterTemplateConfig.id} EX_CONFIG_ID: ${exMasterTemplateConfig.id}")
        }

    }

    def fetchAllProductViewAssignments(String hierarchy, Configuration masterTemplateConfig) {
        Sql sql = new Sql(dataSource_pva)
        List productViewAssignments
        try {
            String sqlString = """
                SELECT 
                    pva.Alert_Group, 
                    pva.Product, 
                    pva.Workflow_group, 
                    pva.Primary_User_OR_Group_ID, 
                    pva.HIERARCHY,
                (SELECT LISTAGG(pau.USER_ID, ',') WITHIN GROUP (ORDER BY pau.USER_ID) 
                     FROM PRODUCT_ASSIGNMENT_USERS pau 
                     WHERE pau.PRODUCT_ASSIGNMENT_ID = pva.ID) AS User_IDs,
                (SELECT LISTAGG(pag.GROUP_ID, ',') WITHIN GROUP (ORDER BY pag.GROUP_ID) 
                     FROM PRODUCT_ASSIGNMENT_GROUPS pag 
                     WHERE pag.PRODUCT_ASSIGNMENT_ID = pva.ID) AS Group_IDs
                FROM 
                    product_view_assignment pva where hierarchy = '${hierarchy}' and alert_Group is not null and Primary_User_OR_Group_ID is not null
            """
            if (masterTemplateConfig.getExcludedAlertGroups() != null) {
                sqlString += " AND alert_group NOT IN (${masterTemplateConfig.excludedAlertGroups})"
            }
            productViewAssignments = sql.rows(sqlString)
        } catch (Exception ex) {
            ex.printStackTrace()
        } finally {
            sql?.close()
        }
        return productViewAssignments
    }

    void createConfigurationAndExecutedConfigurationBasedOnAssignments(Map productViewAssignments, Configuration masterTemplateConfig, ExecutedConfiguration exMasterTemplateConfig, String alertGroup) {
        String key = JSON.parse(masterTemplateConfig?.productHierarchyMasterTmplt).keySet().first()
        String hierarchyName = JSON.parse(masterTemplateConfig?.productHierarchyMasterTmplt).values().first()
        String eventHierarchyName = masterTemplateConfig.groupBySmq ? "SMQ/EventGroup" : "PT"
        List drugTypes = ProductTypeConfiguration.getAll(masterTemplateConfig.drugType.split(",").collect { it as Long })
        String productTypeName = drugTypes ? drugTypes*.name.join(" ") : ""
        Set masterTemplateShareWithUser = masterTemplateConfig.shareWithUsers
        Set masterTemplateShareWithGroup = masterTemplateConfig.shareWithGroups
        Map assignedUserAlertMapping = [:]
        Set usersToAdd = []
        productViewAssignments.each { product, userInfo ->
            String productOrGroupName
            String assignedUserOrGroupName
            String productJson
            String productGroupSelection
            Configuration config
            Configuration existingConfig
            Map productMap
            if (hierarchyName != "Product Group") {
                productMap = JSON.parse(product)
                productJson = SignalUtil.generateProductDictionaryJson(productMap['id'] as String, productMap['name'], key, false)
                productOrGroupName = productMap['name']
                if (masterTemplateConfig.eventGroupSelection) {
                    existingConfig = Configuration.findByEventGroupSelectionAndMasterTemplateIdAndAlertGroupIdAndProductSelection(masterTemplateConfig.eventGroupSelection, masterTemplateConfig.id, alertGroup, productJson)
                } else {
                    existingConfig = Configuration.findByEventSelectionAndMasterTemplateIdAndAlertGroupIdAndProductSelection(masterTemplateConfig.eventSelection, masterTemplateConfig.id, alertGroup, productJson)
                }
                if (existingConfig) {
                    config = existingConfig
                }
            } else {
                def jsonSlurper = new JsonSlurper()
                Map jsonMap = jsonSlurper.parseText(product)
                productOrGroupName = jsonMap.name
                jsonMap.name = "${jsonMap.name} (${jsonMap.id})"
                def updatedJsonString = JsonOutput.toJson(jsonMap)
                productGroupSelection = "[${updatedJsonString}]"
                if (masterTemplateConfig.eventGroupSelection) {
                    existingConfig = Configuration.findByEventGroupSelectionAndMasterTemplateIdAndAlertGroupIdAndProductGroupSelection(masterTemplateConfig.eventGroupSelection, masterTemplateConfig.id, alertGroup, productGroupSelection as JSON)
                } else {
                    existingConfig = Configuration.findByEventSelectionAndMasterTemplateIdAndAlertGroupIdAndProductGroupSelection(masterTemplateConfig.eventSelection, masterTemplateConfig.id, alertGroup, productGroupSelection as JSON)
                }
                if (existingConfig) {
                    config = existingConfig
                }
            }
            if (!config) {
                config = importConfigurationService.createAlertFromTemplate(masterTemplateConfig.type, masterTemplateConfig, masterTemplateConfig.owner)
                config.masterTemplateId = masterTemplateConfig.id
                if (hierarchyName != "Product Group") {
                    config.productSelection = productJson
                    config.productDictionarySelection = key
                } else {
                    config.productGroupSelection = productGroupSelection
                }
                config.createdBy = config.modifiedBy = masterTemplateConfig.createdBy
                config.alertGroupId = alertGroup as Long
            }

            Set assignedToUserToShared = new HashSet<>()
            Set assignedToGroupToShared = new HashSet<>()
            userInfo.each { type, value ->
                if (type == "Assigned_To_ID" && value) {
                    def firstEntry = value[0]
                    if (firstEntry.startsWith(Constants.USER_GROUP_TOKEN) && Group.get(Long.valueOf(firstEntry.replaceAll(Constants.USER_GROUP_TOKEN, '')))) {
                        Group assignedToGroup = Group.get(Long.valueOf(firstEntry.replaceAll(Constants.USER_GROUP_TOKEN, '')))
                        config.assignedToGroup = assignedToGroup
                        config.assignedTo = null
                        assignedUserOrGroupName = assignedToGroup.name
                    } else if (firstEntry.startsWith(Constants.USER_TOKEN) && User.get(Long.valueOf(firstEntry.replaceAll(Constants.USER_TOKEN, '')))) {
                        User assignedToUser = User.get(Long.valueOf(firstEntry.replaceAll(Constants.USER_TOKEN, '')))
                        config.assignedTo = assignedToUser
                        config.assignedToGroup = null
                        assignedUserOrGroupName = assignedToUser.username
                    }
                    value.each {
                        if (it.startsWith(Constants.USER_GROUP_TOKEN) && Group.get(Long.valueOf(it.replaceAll(Constants.USER_GROUP_TOKEN, '')))) {
                            Group sharedGroup = Group.get(Long.valueOf(it.replaceAll(Constants.USER_GROUP_TOKEN, '')))
                            config.addToShareWithGroup(sharedGroup)
                            assignedToGroupToShared.add(sharedGroup)
                        } else if (it.startsWith(Constants.USER_TOKEN) && User.get(Long.valueOf(it.replaceAll(Constants.USER_TOKEN, '')))) {
                            User sharedUser = User.get(Long.valueOf(it.replaceAll(Constants.USER_TOKEN, '')))
                            config.addToShareWithUser(sharedUser)
                            assignedToUserToShared.add(sharedUser)
                        }
                    }
                }
            }
            userInfo.each { type, value ->
                if (type == "SharedWithUserId" && value) {
                    Set newSharedUserList = User.getAll(value)
                    newSharedUserList = newSharedUserList - assignedToUserToShared - masterTemplateShareWithUser
                    Set existingSharedUserList = config.shareWithUsers ?: []
                    existingSharedUserList = existingSharedUserList - assignedToUserToShared - masterTemplateShareWithUser
                    Set sharedUsersToAdd = newSharedUserList - existingSharedUserList
                    Set usersToDelete = existingSharedUserList - newSharedUserList
                    sharedUsersToAdd?.each { user ->
                        config.addToShareWithUser(user)
                    }
                    usersToDelete.each { user ->
                        config.removeFromShareWithUser(user)
                    }
                }
                if (type == "SharedWithGroupID" && value) {
                    Set newSharedGroupList = Group.getAll(value)
                    newSharedGroupList = newSharedGroupList - assignedToGroupToShared - masterTemplateShareWithGroup
                    Set existingSharedGroupList = config.shareWithGroups ?: []
                    existingSharedGroupList = existingSharedGroupList - assignedToGroupToShared - masterTemplateShareWithGroup
                    Set groupsToAdd = newSharedGroupList - existingSharedGroupList
                    Set groupsToDelete = existingSharedGroupList - newSharedGroupList
                    groupsToAdd?.each {
                        config.addToShareWithGroup(it)
                    }
                    groupsToDelete.each {
                        config.removeFromShareWithGroup(it)
                    }
                }
            }
            String expectedName = replacePlaceholders(masterTemplateConfig.name, assignedUserOrGroupName, productOrGroupName, hierarchyName, productTypeName, eventHierarchyName, alertGroup)
            if (config.name != expectedName) {
                alertService.renameExecConfig(config.id, config.name, expectedName, config.owner.id, Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
            }
            config.name = expectedName
            config.skipAudit = true
            config.skipNameValidation = true
            config.save(flush: true)
            ExecutedConfiguration exConfig = createExecutedConfigurationForAlert(config)
            exConfig.alertGroupId = config.alertGroupId
            exConfig.exMasterTemplateId = exMasterTemplateConfig.id
            exConfig.save(flush: true)
            userInfo.each { type, value ->

                if (type == "Assigned_To_ID" && value) {
                    value?.each {
                        if (it.startsWith(Constants.USER_GROUP_TOKEN) && Group.get(Long.valueOf(it.replaceAll(Constants.USER_GROUP_TOKEN, '')))) {
                            usersToAdd.addAll(cacheService.getAllUsersFromCacheByGroup(Long.valueOf(it.replaceAll(Constants.USER_GROUP_TOKEN, '')))*.id)

                        } else if (it.startsWith(Constants.USER_TOKEN) && User.get(Long.valueOf(it.replaceAll(Constants.USER_TOKEN, '')))) {
                            usersToAdd.add(Long.valueOf(it.replaceAll(Constants.USER_TOKEN, '')))
                        }
                    }
                }
                //Add FDA or EMA condition here.............................
                if (Holders.config.is.secondary.assignee) {
                    if (type == "SharedWithUserId" && value) {
                        usersToAdd.addAll(value)
                    }
                    if (type == "SharedWithGroupID" && value) {
                        value.each { groupId ->
                            usersToAdd.addAll(cacheService.getAllUsersFromCacheByGroup(groupId as Long)*.id)
                        }
                    }
                }
            }

            usersToAdd.each { user ->
                Set exConfigList = assignedUserAlertMapping.get(user) ?: []
                exConfigList.add(exConfig)
                assignedUserAlertMapping.put(user, exConfigList)
            }
        log.info("Configuration generated for ${expectedName}")
        }
        generateGroupAlertInfo(assignedUserAlertMapping, masterTemplateConfig, exMasterTemplateConfig, hierarchyName, productTypeName, eventHierarchyName, alertGroup)
    }

    void generateGroupAlertInfo(Map assignedUserAlertMapping, Configuration masterTemplateConfig, ExecutedConfiguration exMasterTemplateConfig, String hierarchyName, String productTypeName, String eventHierarchyName, String alertGroup) {
        assignedUserAlertMapping.each { userId, alertList ->
            GroupedAlertInfo groupedAlertInfo = new GroupedAlertInfo()
            groupedAlertInfo.productDictionarySelection = masterTemplateConfig.productDictionarySelection
            groupedAlertInfo.assignedToId = userId
            alertList.each {
                groupedAlertInfo.addToExecConfigList(it)
            }
            User user = User.get(userId)
            groupedAlertInfo.name = replacePlaceholders(masterTemplateConfig.name, user.username, user.username, hierarchyName, productTypeName, eventHierarchyName, alertGroup)
            groupedAlertInfo.masterTemplateConfigId = masterTemplateConfig.id
            groupedAlertInfo.exMasterTemplateConfigId = exMasterTemplateConfig.id
            groupedAlertInfo.alertGroupId = alertGroup as Long
            if (hierarchyName != "Product Group") {
                groupedAlertInfo.productSelection = setProductSelectionForHierarchyAlertInfo(alertList)
            } else {
                groupedAlertInfo.productGroupSelection = setProductGroupSelectionForHierarchyAlertInfo(alertList)
            }
            groupedAlertInfo.dateCreated = new Date()
            groupedAlertInfo.lastUpdated = new Date()
            groupedAlertInfo.save()
            log.info("Grouped Config Generated for ${groupedAlertInfo.name}")
        }

    }
    String setProductSelectionForHierarchyAlertInfo(def execConfigList) {
        Map helperProductSelectionMap = [:]
        JsonSlurper jsonSlurper = new JsonSlurper()
        execConfigList.each { obj ->
            Map productSelectionMap = obj.productSelection ? jsonSlurper.parseText(obj.productSelection) : null
            helperProductSelectionMap = createCumulativeProductJson(productSelectionMap, helperProductSelectionMap)
        }
        return JsonOutput.toJson(helperProductSelectionMap)
    }

    String setProductGroupSelectionForHierarchyAlertInfo(def execConfigList) {
        List helperProductGroupSelection = []
        JsonSlurper jsonSlurper = new JsonSlurper()
        execConfigList.each { obj ->
            List productGroupSelection = obj.productGroupSelection ? jsonSlurper.parseText(obj.productGroupSelection) : []
            helperProductGroupSelection = createCumulativeProductGroupJson(productGroupSelection, helperProductGroupSelection)
        }
        return JsonOutput.toJson(helperProductGroupSelection)
    }

    String replacePlaceholders(String alertName, String assignedUserOrGroupName, String productOrGroupName,
                               String hierarchyName, String productTypeName, String eventHierarchyName,
                               String alertGroup) {
        String alertGroupName = AlertGroup.get(alertGroup as Long)?.name
        // Max Limit Set As per Requirement in the Story.
        assignedUserOrGroupName = assignedUserOrGroupName?.take(80)
        productOrGroupName = productOrGroupName?.take(80)
        hierarchyName = hierarchyName?.take(20)
        productTypeName = productTypeName?.take(20)
        eventHierarchyName = eventHierarchyName?.take(20)
        alertGroupName = alertGroupName?.take(20)

        alertName = alertName.replaceAll("(?i)\\[Assigned To\\]", assignedUserOrGroupName ?: "")
                .replaceAll("(?i)\\[Product Name\\]", productOrGroupName ?: "")
                .replaceAll("(?i)\\[Product Hierarchy\\]", hierarchyName ?: "")
                .replaceAll("(?i)\\[Product Type\\]", productTypeName ?: "")
                .replaceAll("(?i)\\[Event Hierarchy\\]", eventHierarchyName ?: "")
                .replaceAll("(?i)\\[Alert Group\\]", alertGroupName ?: "")

        return alertName
    }

    def createCumulativeProductJson(Map productSelectionMap, Map productSelectionHelperMap) {
        if (productSelectionHelperMap && productSelectionMap) {
            Map combinedMap = [:]
            combinedMap = productSelectionHelperMap.collectEntries { key, value ->
                [key, (value + productSelectionMap[key])]
            }
            productSelectionHelperMap = combinedMap
        } else if (productSelectionMap) {
            productSelectionHelperMap = productSelectionMap
        }
        productSelectionHelperMap
    }

    def createCumulativeProductGroupJson(List productSelectionList = [], List productGroupSelectionHelper = []) {
        productGroupSelectionHelper = productGroupSelectionHelper + productSelectionList
        productGroupSelectionHelper
    }

    def groupProductViewAssignmentDataAccordingToAlertGroup(List assignmentList) {
        def structuredMap = [:]
        assignmentList.each { row ->
            def alertGroup = row.Alert_Group
            def productName = row.Product
            def assignedToId = row.Primary_User_OR_Group_ID

            structuredMap[alertGroup] = structuredMap.get(alertGroup, [:])
            structuredMap[alertGroup][productName] = structuredMap[alertGroup].get(productName, [:])

            def assignedToIdList = structuredMap[alertGroup][productName].get('Assigned_To_ID', [])
            if (!assignedToIdList.contains(assignedToId)) {
                assignedToIdList << assignedToId
            }
            structuredMap[alertGroup][productName]['Assigned_To_ID'] = assignedToIdList

            def sharedWithUserIdList = (structuredMap[alertGroup][productName].get('SharedWithUserId', [])
                    + (row.User_IDs?.split(',') ?: []).collect { it as Long }).flatten().unique()
            structuredMap[alertGroup][productName]['SharedWithUserId'] = sharedWithUserIdList

            def sharedWithGroupIdList = (structuredMap[alertGroup][productName].get('SharedWithGroupID', [])
                    + (row.Group_IDs?.split(',') ?: []).collect { it as Long }).flatten().unique()
            structuredMap[alertGroup][productName]['SharedWithGroupID'] = sharedWithGroupIdList
        }
        return structuredMap

    }
}

