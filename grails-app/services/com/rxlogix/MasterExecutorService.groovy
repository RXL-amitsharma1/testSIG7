package com.rxlogix

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.Member
import com.rxlogix.config.AlertDateRangeInformation
import com.rxlogix.config.Configuration
import com.rxlogix.config.EvdasConfiguration
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.config.ExecutionStatus
import com.rxlogix.config.MasterChildRunNode
import com.rxlogix.config.MasterConfigStatus
import com.rxlogix.config.MasterConfiguration
import com.rxlogix.config.MasterEvdasConfiguration
import com.rxlogix.config.MasterEvdasExecutedConfig
import com.rxlogix.config.MasterExecutedConfiguration
import com.rxlogix.config.ReportExecutionStatus
import com.rxlogix.customException.ExecutionStatusException
import com.rxlogix.dto.SuperQueryDTO
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.DrugTypeEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.SignalQueryHelper
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonSlurper
import groovy.sql.GroovyResultSet
import groovy.sql.Sql
import groovy.time.TimeCategory
import net.fortuna.ical4j.model.DateTime
import net.fortuna.ical4j.model.Period
import net.fortuna.ical4j.model.PeriodList
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.property.RRule
import org.apache.http.util.TextUtils
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.Transaction
import org.hibernate.resource.transaction.spi.TransactionStatus
import org.springframework.transaction.annotation.Propagation

import java.sql.Clob
import java.sql.ResultSet
import java.text.SimpleDateFormat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import static org.hibernate.resource.transaction.spi.TransactionStatus.*

@Transactional
class MasterExecutorService {

    def CRUDService
    def configurationService
    def reportExecutorService
    def signalDataSourceService
    def sqlGenerationService
    def queryService
    def businessConfigurationService
    def alertService
    def aggregateCaseAlertService
    def evdasAlertExecutionService
    def dataSource_eudra
    def evdasSqlGenerationService
    def grailsApplication
    def statisticsService
    def sessionFactory
    def dataObjectService
    def singleCaseAlertService
    def userService
    def alertAdministrationService
    def hazelcastService
    def cacheService
    def alertFieldService
    def dataSource

    HazelcastInstance hazelcastInstance

    public static String HAZELCAST_CURRENTLY_RUNNING_MASTER = "hazelcastCurrentlyRunningMaster"

    List<Long> currentlyMasterRunning = []


    void runConfigurations(String alertType, List<Long> runningIdsList) throws Exception {

        MasterConfiguration scheduledConfiguration = MasterConfiguration.getNextConfigurationToExecute(runningIdsList)

        Date nextRunDate = null
        Boolean isPartialAlert = scheduledConfiguration ? cacheService.getPartialAlertCache(scheduledConfiguration.id):false
        if (scheduledConfiguration && !isPartialAlert) {
            String serverHostname = "hostname".execute()?.text?.trim()
            log.info('Hostname of the server : ' + serverHostname)
            nextRunDate = scheduledConfiguration.nextRunDate
            if (scheduledConfiguration.isResume) {
                updateMasterConfig(scheduledConfiguration, true, true)
            } else{
                updateMasterConfig(scheduledConfiguration, true, false)
            }
            runningIdsList.add(scheduledConfiguration.id)
            List<Configuration> configurations = Configuration.findAllByMasterConfigIdAndNextRunDate(scheduledConfiguration.id, nextRunDate)
            Configuration childConfiguration = configurations ? configurations[0] : null
            if (!childConfiguration) return
            boolean isSafetyAlert = alertAdministrationService.isSafetyAlert(childConfiguration)
            alertAdministrationService.initializePreChecks()
            Map<String, Object> resultMap = alertAdministrationService.isMasterConfigurationReadyForExecution(scheduledConfiguration, childConfiguration, isSafetyAlert)
            Boolean isReadyForExecution = resultMap.get(Constants.AlertUtils.IS_READY_FOR_EXECUTION)
            if (isReadyForExecution) {
                log.info("Master alert found ${scheduledConfiguration.name} [${scheduledConfiguration.id}] to execute.")
                ExecutionStatus executionStatus
                Boolean isResume = false
                if (scheduledConfiguration.isResume) {
                    isResume = true
                    Long exConfigId = ExecutionStatus.findByConfigIdAndExecutionStatusAndIsMaster(scheduledConfiguration?.id, ReportExecutionStatus.SCHEDULED, true)?.executedConfigId
                    if (!exConfigId) {
                        ExecutionStatus availExecutionStatus = ExecutionStatus.findByConfigIdAndReportExecutionStatusAndIsMaster(scheduledConfiguration?.id, ReportExecutionStatus.SCHEDULED, true) ?: ExecutionStatus.findByConfigIdAndSpotfireExecutionStatusAndIsMaster(scheduledConfiguration?.id, ReportExecutionStatus.SCHEDULED, true)
                        exConfigId = availExecutionStatus?.executedConfigId
                    }
                    executionStatus = ExecutionStatus.findByConfigIdAndExecutedConfigIdAndTypeAndIsMaster(scheduledConfiguration.id,
                            exConfigId, alertType, true)

                } else {
                    MasterExecutedConfiguration masterExecutedConfiguration = createMasterExecutedConfiguration(scheduledConfiguration.id, executionStatus, null)
                    executionStatus = createExecutionStatus(scheduledConfiguration.id, true, masterExecutedConfiguration)
                }
                try {
                    if (executionStatus)
                        startMasterAlertExecutionByLevel(executionStatus, isResume, nextRunDate)

                } catch (Throwable throwable) {
                    ExecutionStatusException ese = new ExecutionStatusException(alertService.exceptionString(throwable))
                    handleFailedMasterExecution(ese, scheduledConfiguration, executionStatus.executedConfigId, alertType, executionStatus.id, true)
                    log.error("Exception in runConfigurations", throwable)
                } finally {
                    runningIdsList.remove(scheduledConfiguration.id)
                }
            } else {
                alertAdministrationService.autoPauseMasterConfigurationAndTriggerNotification(scheduledConfiguration, childConfiguration, alertType, resultMap.get(Constants.AlertUtils.ALERT_DISABLE_REASON))
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception)
    createExecutionStatus(def scheduledConfigurationId, boolean isMaster, def executedConfiguration=null) {
        log.info("Creating the execution status.")
        def scheduledConfiguration
        def lockedConfiguration = isMaster?MasterConfiguration.get(scheduledConfigurationId):Configuration.get(scheduledConfigurationId)
        ExecutionStatus executionStatus = new ExecutionStatus(
                configId: lockedConfiguration.id,
                reportVersion: lockedConfiguration.numOfExecutions?lockedConfiguration.numOfExecutions+1:1,
                startTime: System.currentTimeMillis(),
                nextRunDate: new Date(),// to do master config date
                owner: lockedConfiguration.owner,
                name: lockedConfiguration.name,
                executionStatus: ReportExecutionStatus.GENERATING,
                isMaster: isMaster, executionLevel: isMaster?0:1,
                type: "Aggregate Case Alert")
        if(executedConfiguration)
            executionStatus.executedConfigId = executedConfiguration.id
        executionStatus.frequency = configurationService.calculateFrequency(lockedConfiguration)
        // schedule reports/spotfire
        if(!isMaster) {
            if (lockedConfiguration.templateQueries.size()) {
                executionStatus.reportExecutionStatus = ReportExecutionStatus.SCHEDULED
            }
            if (lockedConfiguration.spotfireSettings) {
                executionStatus.spotfireExecutionStatus = ReportExecutionStatus.SCHEDULED
            }
        }

        log.info("Saving the execution status.")
        executionStatus.save(flush:true)
        log.info("Execution status related flow complete.")
        executionStatus
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception)
    void updateExecutionStatus(List<ExecutedConfiguration> allExConfigs, ExecutionStatus executionStatus){
        List<ExecutionStatus> executionStatusListNew = ExecutionStatus.findAllByExecutedConfigIdInListAndExecutionLevel(allExConfigs*.id, executionStatus.executionLevel) + executionStatus
        String updateQuery
        if(executionStatus.executionLevel == Constants.Commons.RESUME_REPORT){
            updateQuery = "update ex_status set REPORT_EXECUTION_STATUS = '" + ReportExecutionStatus.GENERATING + "' where id in (" + executionStatusListNew*.id.join(",") + ")"
        } else if(executionStatus.executionLevel == Constants.Commons.RESUME_SPOTFIRE){
            updateQuery = "update ex_status set SPOTFIRE_EXECUTION_STATUS = '" + ReportExecutionStatus.GENERATING + "' where id in (" + executionStatusListNew*.id.join(",") + ")"
        } else {
            updateQuery = "update ex_status set EX_STATUS = '" + ReportExecutionStatus.GENERATING + "' where id in (" + executionStatusListNew*.id.join(",") + ")"
        }
        log.info(updateQuery)
        try {
            SQLQuery sql = null
            Session session = sessionFactory.currentSession
            sql = session.createSQLQuery(updateQuery)
            sql.executeUpdate()
            session.flush()
            session.clear()
        } catch(Exception ex) {
            log.error(ex.printStackTrace())
        }
    }

    void startMasterAlertExecutionByLevel(ExecutionStatus executionStatus, boolean isResume, Date nextRunDate) throws Exception {
        MasterConfiguration lockedConfiguration = MasterConfiguration.get(executionStatus.configId)
        MasterConfiguration masterFaersConfiguration
        MasterConfiguration masterVaersConfiguration
        MasterConfiguration masterVigibaseConfiguration
        MasterEvdasConfiguration masterEvdasConfiguration
        // create master executed configurtion
        MasterExecutedConfiguration masterExecutedConfiguration
        MasterExecutedConfiguration masterFaersExecutedConfiguration
        MasterExecutedConfiguration masterVaersExecutedConfiguration
        MasterExecutedConfiguration masterVigibaseExecutedConfiguration

        if(isResume && executionStatus.executedConfigId && executionStatus.executionLevel > 1) {
            masterExecutedConfiguration = MasterExecutedConfiguration.get(executionStatus.executedConfigId)
            masterFaersExecutedConfiguration = MasterExecutedConfiguration.findByIntegratedExIdAndDatasource(masterExecutedConfiguration?.id, "faers")
            masterEvdasConfiguration = MasterEvdasConfiguration.findByMasterConfigId(lockedConfiguration.id)
        }


        List<Configuration> configurations = Configuration.findAllByMasterConfigIdAndNextRunDate(lockedConfiguration.id, nextRunDate)
        List<Configuration> faersConfigurations = []
        List<EvdasConfiguration> evdasConfigurations = []
        List<Configuration> vaersConfigurations = []
        List<Configuration> vigibaseConfigurations = []

        Configuration configuration1 = configurations?configurations[0]:null
        try{
            if(configuration1 && !isResume) {
                updateMasterConfigurationDataSource(lockedConfiguration.id, configuration1.selectedDatasource)
                lockedConfiguration.refresh()
            } else if(!isResume)
                return

            if(configuration1) {
                updateConfigurationStatus(configurations)
            }
        } catch (Throwable th){
            log.error(th.message)
            throw th
        }
        List<ExecutedConfiguration> exConfigs = []
        List<ExecutedConfiguration> exFaersConfigs = []
        List<ExecutedEvdasConfiguration> exEvdasConfigs = []
        List<ExecutedConfiguration> exVaersConfigs = []
        List<ExecutedConfiguration> exVigibaseConfigs = []
        List<ExecutionStatus> executionStatusList = []


        if(isResume) {
            if (executionStatus?.executionStatus != ReportExecutionStatus.COMPLETED || executionStatus?.reportExecutionStatus != ReportExecutionStatus.COMPLETED || executionStatus?.spotfireExecutionStatus != ReportExecutionStatus.COMPLETED) {
                List<ExecutedConfiguration> allExConfigs = ExecutedConfiguration.findAllByMasterExConfigIdAndConfigIdInList(executionStatus.executedConfigId, configurations*.id)
                updateExecutionStatus(allExConfigs, executionStatus)
                exConfigs = allExConfigs

                faersConfigurations = Configuration.findAllByIntegratedConfigurationIdInList(configurations*.id)
                configurations.each { childConfig ->
                    Configuration faersConfiguration = faersConfigurations?.find {it.integratedConfigurationId == childConfig.id}
                    if(faersConfiguration) {
                        ExecutedConfiguration exFaersConfig = ExecutedConfiguration.findByMasterExConfigIdAndConfigId(masterFaersExecutedConfiguration?.id, faersConfiguration.id)
                        exFaersConfigs << exFaersConfig
                    }
                }
            }
        }

        try{
            List dataSources = configuration1.selectedDatasource.split(',')

            switch(executionStatus.executionLevel) {
                case 0: // create executed config

                    // create configs for integrated review
                    if(dataSources.size() >= 1) {
                        dataSources.each { it ->
                            if (it == Constants.DataSource.EUDRA) {
                                masterEvdasConfiguration = MasterEvdasConfiguration.findByMasterConfigId(lockedConfiguration.id)
                                if (!masterEvdasConfiguration) {
                                    masterEvdasConfiguration = createEvdasConfiguration(lockedConfiguration.id)
                                }
                            } else {
                                if (it == Constants.DataSource.PVA || (it == Constants.DataSource.FAERS && configuration1.selectedDatasource.startsWith(Constants.DataSource.FAERS)) ||
                                        (it == Constants.DataSource.VAERS && configuration1.selectedDatasource.startsWith(Constants.DataSource.VAERS)) ||
                                        (it == Constants.DataSource.VIGIBASE && configuration1.selectedDatasource.startsWith(Constants.DataSource.VIGIBASE))) {
                                    // configs exists
                                    if(executionStatus.executedConfigId) {
                                        masterExecutedConfiguration = MasterExecutedConfiguration.get(executionStatus.executedConfigId)
                                    } else {
                                        masterExecutedConfiguration = createMasterExecutedConfiguration(lockedConfiguration.id, executionStatus, it)
                                    }
                                } else if(it == Constants.DataSource.FAERS){
                                    // can check in master job as well
                                    while (reportExecutorService.getFaersExecutionQueueSize() > 0) {
                                        log.info("Waiting for Faers alert to complete")
                                        Thread.sleep(600000)
                                    }
                                    masterFaersConfiguration = MasterConfiguration.findByIntegratedIdAndDatasource(lockedConfiguration.id, Constants.DataSource.FAERS)
                                    if (!masterFaersConfiguration) {
                                        masterFaersConfiguration = createMasterConfig(lockedConfiguration.id, Constants.DataSource.FAERS)
                                        masterFaersConfiguration.save(flush: true)
                                    }
                                    masterFaersExecutedConfiguration = MasterExecutedConfiguration.findByIntegratedExIdAndDatasource(masterExecutedConfiguration.id, Constants.DataSource.FAERS)
                                    if (!masterFaersExecutedConfiguration) {
                                        masterFaersExecutedConfiguration = createMasterExecutedConfiguration(lockedConfiguration.id, executionStatus, Constants.DataSource.FAERS)
                                    }

                                } else if(it == Constants.DataSource.VAERS){

                                    masterVaersConfiguration = MasterConfiguration.findByIntegratedIdAndDatasource(lockedConfiguration.id, Constants.DataSource.VAERS)
                                    if (!masterVaersConfiguration) {
                                        masterVaersConfiguration = createMasterConfig(lockedConfiguration.id, Constants.DataSource.VAERS)
                                        masterVaersConfiguration.save(flush: true)
                                    }
                                    masterVaersExecutedConfiguration = MasterExecutedConfiguration.findByIntegratedExIdAndDatasource(masterExecutedConfiguration.id, Constants.DataSource.VAERS)
                                    if (!masterVaersExecutedConfiguration) {
                                        masterVaersExecutedConfiguration = createMasterExecutedConfiguration(lockedConfiguration.id, executionStatus, Constants.DataSource.VAERS)
                                    }

                                } else if(it == Constants.DataSource.VIGIBASE){

                                    masterVigibaseConfiguration = MasterConfiguration.findByIntegratedIdAndDatasource(lockedConfiguration.id, Constants.DataSource.VIGIBASE)
                                    if (!masterVigibaseConfiguration) {
                                        masterVigibaseConfiguration = createMasterConfig(lockedConfiguration.id, Constants.DataSource.VIGIBASE)
                                        masterVigibaseConfiguration.save(flush: true)
                                    }
                                    masterVigibaseExecutedConfiguration = MasterExecutedConfiguration.findByIntegratedExIdAndDatasource(masterExecutedConfiguration.id, Constants.DataSource.VIGIBASE)
                                    if (!masterVigibaseExecutedConfiguration) {
                                        masterVigibaseExecutedConfiguration = createMasterExecutedConfiguration(lockedConfiguration.id, executionStatus, Constants.DataSource.VIGIBASE)
                                    }

                                }
                            }
                        }
                    }

                    dataSources.each { it ->
                        if (it == Constants.DataSource.EUDRA) {
                            createEvdasConfigurations(configurations, evdasConfigurations, exEvdasConfigs,masterEvdasConfiguration?.id)
                        } else {
                            if (it == Constants.DataSource.PVA || (it == Constants.DataSource.FAERS && configuration1.selectedDatasource.startsWith(Constants.DataSource.FAERS)) ||
                                    (it == Constants.DataSource.VAERS && configuration1.selectedDatasource.startsWith(Constants.DataSource.VAERS)) ||
                                    (it == Constants.DataSource.VIGIBASE && configuration1.selectedDatasource.startsWith(Constants.DataSource.VIGIBASE))) {
                                createExecConfigsForMaster(configurations*.id, masterExecutedConfiguration, dataSources, exConfigs, executionStatusList)
                                for (Configuration config : configurations) {
                                    config.refresh()
                                }
                            } else if (it == Constants.DataSource.FAERS) {
                                createIntegratedConfigurations(configurations, faersConfigurations, exFaersConfigs, masterFaersExecutedConfiguration, masterFaersConfiguration, Constants.DataSource.FAERS)
                            } else if (it == Constants.DataSource.VAERS) {
                                createIntegratedConfigurations(configurations, vaersConfigurations, exVaersConfigs, masterVaersExecutedConfiguration, masterVaersConfiguration, Constants.DataSource.VAERS)
                            } else if (it == Constants.DataSource.VIGIBASE) {
                                createIntegratedConfigurations(configurations, vigibaseConfigurations, exVigibaseConfigs, masterVigibaseExecutedConfiguration, masterVigibaseConfiguration, Constants.DataSource.VIGIBASE)
                            }
                        }
                    }

                    // exconfigs for faers/evdas in integrated review

                    reportExecutorService.updateExecutionStatusLevel(executionStatus, masterExecutedConfiguration)
                case 1: // fetch data and persist in files
                    // insert gtt tables
                    dataSources.each { it ->

                        if (it == Constants.DataSource.EUDRA) {
                            exEvdasConfigs.each{executedEvdasConfiguration ->
                                List<Date> dateRange = executedEvdasConfiguration?.dateRangeInformation?.getReportStartAndEndDate()
                                EvdasConfiguration evdasConfiguration = EvdasConfiguration.findById(executedEvdasConfiguration?.configId as Long)
                                ExecutedConfiguration executedConfiguration = ExecutedConfiguration.findByConfigIdAndMasterExConfigId(Configuration.findById(evdasConfiguration?.integratedConfigurationId as Long)?.id, masterExecutedConfiguration?.id as Long)
                                if (executedEvdasConfiguration.dateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE) {
                                    Date dateRangeEnd = executedEvdasConfiguration?.dateCreated
                                    String evdasDateRange = DateUtil.toDateString1(dateRange[0]) + " - " + DateUtil.toDateString1(dateRangeEnd)
                                    executedConfiguration.evdasDateRange = evdasDateRange
                                } else {
                                    String evdasDateRange = DateUtil.toDateString1(dateRange[0]) + " - " + DateUtil.toDateString1(dateRange[1])
                                    executedConfiguration.evdasDateRange = evdasDateRange
                                }
                                executedConfiguration.save(flush:true)
                            }
                        } else {
                            if (it == Constants.DataSource.PVA || (it == Constants.DataSource.FAERS && configuration1.selectedDatasource.startsWith(Constants.DataSource.FAERS)) ||
                                    (it == Constants.DataSource.VAERS && configuration1.selectedDatasource.startsWith(Constants.DataSource.VAERS)) ||
                                    (it == Constants.DataSource.VIGIBASE && configuration1.selectedDatasource.startsWith(Constants.DataSource.VIGIBASE))) {
                                initiateMasterRun(lockedConfiguration.id, masterExecutedConfiguration, configurations, exConfigs, it)
                            } else if (it == Constants.DataSource.FAERS) {
                                initiateMasterRun(masterFaersConfiguration?.id, masterFaersExecutedConfiguration, faersConfigurations, exFaersConfigs, it)
                            } else if (it == Constants.DataSource.VAERS) {
                                initiateMasterRun(masterVaersConfiguration?.id, masterVaersExecutedConfiguration, vaersConfigurations, exVaersConfigs, it)
                            } else if (it == Constants.DataSource.VIGIBASE) {
                                initiateMasterRun(masterVigibaseConfiguration?.id, masterVigibaseExecutedConfiguration, vigibaseConfigurations, exVigibaseConfigs, it)
                            }
                        }

                    }

                    // check data status for completion
                    List<MasterConfigStatus> objs=[]
                    AtomicInteger dbRun = new AtomicInteger(0)
                    Integer totalSize = dataSources.size()


                    ExecutorService executorService = Executors.newFixedThreadPool(4)
                    try {
                        List<Future> futureList = dataSources.collect { db ->
                            executorService.submit({
                                // d) check db data activity done

                                if([Constants.DataSource.PVA,Constants.DataSource.FAERS, Constants.DataSource.VAERS, Constants.DataSource.VIGIBASE].contains(db)) {
                                    List exDbConfigs = exConfigs
                                    Long exDbConfigId = masterExecutedConfiguration.id
                                    if(dataSources.contains("vigibase") || dataSources.contains("faers") || dataSources.contains("vaers")) {
                                        if(db == "faers" && !configuration1.selectedDatasource.startsWith(Constants.DataSource.FAERS)) {
                                            exDbConfigId = masterFaersExecutedConfiguration.id
                                            exDbConfigs = exFaersConfigs
                                        } else if(db == "vaers" && !configuration1.selectedDatasource.startsWith(Constants.DataSource.VAERS)) {
                                            exDbConfigId = masterVaersExecutedConfiguration.id
                                            exDbConfigs = exVaersConfigs
                                        } else if(db == "vigibase" && !configuration1.selectedDatasource.startsWith(Constants.DataSource.VIGIBASE)) {
                                            exDbConfigId = masterVigibaseExecutedConfiguration.id
                                            exDbConfigs = exVigibaseConfigs
                                        }
                                    }

                                    Map result = [countsCompleted: false, ebgmCompleted: false, prrCompleted: false, dssCompleted: false]
                                    Integer tryCountInit = 1
                                    Integer tryCountMax = Holders.config.master.safety.timeout.minutes?:300
                                    Integer tryCountMaxFaers = Holders.config.master.faers.timeout.minutes?:600
                                    if(db == "faers" || db == "vigibase")
                                        tryCountMax = tryCountMaxFaers
                                    log.info("Timeout set to ${tryCountMax} minutes.")
                                    Boolean countsInProgress = false
                                    Boolean ebgmInProgress = false
                                    Boolean prrInProgress = false
                                    Boolean dssInProgress = false
                                    boolean dssFlag = db == "pva" && Holders.config.statistics.enable.dss
                                    boolean ebgmFlag = db == "pva" ? Holders.config.statistics.enable.ebgm : Holders.config.statistics."${db}".enable.ebgm
                                    boolean prrFlag = db == "pva" ? Holders.config.statistics.enable.prr : Holders.config.statistics."${db}".enable.prr
                                    Map flagMap = [dssFlag: dssFlag, ebgmFlag: ebgmFlag, prrFlag: prrFlag]
                                    if(!dssFlag)
                                        result.dssCompleted = true
                                    if(!ebgmFlag)
                                        result.ebgmCompleted = true
                                    if(!prrFlag)
                                        result.prrCompleted = true

                                    log.info("result:${db}:${result}")


                                    while (!(result.countsCompleted == true && result.ebgmCompleted == true && result.prrCompleted == true && result.dssCompleted == true)) {
                                        Thread.sleep(60000)
                                        checkDbActivityStatus(exDbConfigId, db, result, flagMap)

                                        Boolean isEventGroup = configurations[0].eventGroupSelection && configurations[0].groupBySmq
                                        String alertType = configurations[0].type
                                        Boolean isCaseAvailPrrRor = false
                                        if (result.ebgmCompleted && ebgmInProgress == false) {
                                            ebgmInProgress = true
                                            // update master config status ebgm done
                                            if(ebgmFlag) {
                                                try {
                                                    log.info("Checking cases in DB")
                                                    boolean isCasesAvail = reportExecutorService.checkPECsInDB(exDbConfigId, db)
                                                    if (isCasesAvail) {
                                                        log.info("Now calling the statistics block.")
                                                        def status = statisticsService.calculateStatisticalScores(db, exDbConfigId, true)
                                                        if (!status.status && Holders.config.statistics.enable.ebgm) {
                                                            throw new Exception("EBGM score calculation failed! " + status.error)
                                                        }
                                                        if(status.status){
                                                            execABCDData(db, exDbConfigId)
                                                        }
                                                    } else {
                                                        log.info("No call on Stats due to zero cases in DB.")
                                                    }

                                                } catch (Throwable th) {
                                                    log.error("Error occurred while calculating EBGM scores.", th)
                                                    throw th
                                                }
                                            }
                                        }
                                        if (result.prrCompleted && prrInProgress == false) {
                                            prrInProgress = true
                                            // update master config status prr done
                                            if(prrFlag) {
                                                if(db == Constants.DataSource.PVA){
                                                    log.info("Checking PRR cases in DB")
                                                    isCaseAvailPrrRor = reportExecutorService.checkPecsInDbPrrRor(exDbConfigId, db)
                                                    def isRor = cacheService.getRorCache()
                                                    if (isCaseAvailPrrRor && !configurations[0]?.dataMiningVariable) {
                                                        boolean prrSubGrpEnabled = cacheService.getSubGroupsEnabled("prrSubGrpEnabled") ?: false
                                                        if(prrSubGrpEnabled) {
                                                            def prrStatus = statisticsService.calculateStatisticalScoresPRR(db, exDbConfigId, true)
                                                            if (!prrStatus.status && Holders.config.statistics.enable.prr) {
                                                                throw new Exception("Alert got failed while PRR score calculating. " + '\n' + prrStatus.prrStatus)
                                                            }
                                                            if(isRor) {
                                                                def rorStatus = statisticsService.calculateStatisticalScoresROR(db, exDbConfigId, true)
                                                                if (!rorStatus.status && Holders.config.statistics.enable.ror) {
                                                                    throw new Exception("Alert got failed while ROR score calculating. " + '\n' + rorStatus.error)
                                                                }
                                                            }
                                                        }else{
                                                            log.info("No call on Safety PRR/ROR Stats due to Sub-Groups Disabled")
                                                        }
                                                    }else {
                                                        log.info("No call on Safety PRR/ROR Stats due to zero cases in DB or DMV alert.")
                                                    }
                                                }else {
                                                    log.info("calling PRR method in DB")
                                                    try {
                                                        execPRRData(db, exDbConfigId)
                                                    } catch (Throwable th) {
                                                        log.error("Error occurred while calculating PRR/ROR scores.", th)
                                                        throw th
                                                    }
                                                }
                                            }
                                        }
                                        if (result.ebgmCompleted && result.countsCompleted && result.prrCompleted &&
                                                result.dssCompleted && dssInProgress == false && db == "pva" ) {
                                            dssInProgress = true
                                            // update master config status dss done
                                            try {
                                                if(dssFlag == true) {
                                                    log.info("Checking cases in DB")
                                                    boolean isCasesAvail = reportExecutorService.checkPECsInDB(exDbConfigId, db)
                                                    if(isCasesAvail){
                                                        Map prevData = prevMasterExConfigs(lockedConfiguration.id, masterExecutedConfiguration.id)
                                                        List<ExecutedConfiguration> prevExConfigs = prevData.prevExConfigs
                                                        JSONArray prevAlertIds = prevExConfigs*.id
                                                        if(reportExecutorService.checkAggCountInDB(exDbConfigId, db))
                                                        {
                                                            statisticsService.sendApiCallMasterDssScores(masterExecutedConfiguration?.id, prevAlertIds, exConfigs[0].isGroupBySmq())
                                                        }
                                                    }  else {
                                                        log.info("No call on DSS due to zero cases in DB.")
                                                    }
                                                }

                                            } catch (Throwable th) {
                                                log.error("Error occurred while calculating DSS scores.", th)
                                                throw th
                                            }
                                        }

                                        if(tryCountInit > tryCountMax){
                                            throw new Exception(Holders.config.alertExecution.status.timeout.message)
                                        }
                                        tryCountInit++
                                    }



                                }
                                int runCount = dbRun.incrementAndGet()

                                if(runCount == totalSize) {
                                    objs = createMasterConfigStatus(masterExecutedConfiguration.id, dataSources[0], [:], exConfigs, masterFaersExecutedConfiguration?.id,
                                            exFaersConfigs, masterEvdasConfiguration?.id, evdasConfigurations, exEvdasConfigs, masterVaersExecutedConfiguration?.id, exVaersConfigs,
                                            masterVigibaseExecutedConfiguration?.id, exVigibaseConfigs)
                                }
                            })
                        } as Runnable
                        futureList.each {
                            //it.get()
                            try {
                                it.get()
                            } catch (Throwable th) {
                                log.error(alertService.exceptionString(th))
                                futureList.each { fut->
                                    fut.cancel(true)
                                }
                                throw th
                            }

                        }

                        Boolean dbActivityDone = false
                        int tryCountInit = 1
                        int tryCountMax = 120

                        while (!dbActivityDone) {

                            Thread.sleep(60000)
                            // error check
                            Boolean allDoneFlag = true
                            String errorMsg = ""
                            Boolean isDbError = false
                            MasterConfigStatus.withTransaction {
                                objs.each {
                                    allDoneFlag = allDoneFlag && allDone(it.masterExecId, it.nodeUuid)
                                    MasterConfigStatus masterConfigStatus = MasterConfigStatus.findByNodeUuidAndMasterExecId(it.nodeUuid, it.masterExecId)
                                    if (masterConfigStatus && masterConfigStatus.isDbError) {
                                        isDbError = true
                                        errorMsg = masterConfigStatus.errorMsg
                                    }

                                }
                            }

                            log.info("allDoneFlag:${allDoneFlag}")

                            if (allDoneFlag) {
                                dbActivityDone = true
                                objs.each {
                                    updateMasterConfigStatus("all_db_done", [it.masterExecId], it.nodeUuid)
                                    dataObjectService.setMasterDbDoneMap(it.nodeUuid, it.masterExecId)
                                }
                                //allDbDone
                            } else if (isDbError) {
                                throw new Exception(errorMsg)
                            }

                            if (tryCountInit > tryCountMax) { // can be at individual node check
                                throw new Exception(Holders.config.alertExecution.status.timeout.message)
                            }
                            tryCountInit++
                        }


                        executionStatus = ExecutionStatus.get(executionStatus.id)
                        reportExecutorService.updateExecutionStatusLevel(executionStatus)
                        updateBulkExecutionStatusLevel(executionStatus.id, executionStatusList)
                    } catch (Throwable th) {
                        log.error(alertService.exceptionString(th))
                        throw th
                    } finally {
                        executorService.shutdown()
                        String primeDb = dataSources[0]

                        clearDataMiningTables(masterExecutedConfiguration?.id, primeDb)
                        if(masterFaersExecutedConfiguration?.id)
                            clearDataMiningTables(masterFaersExecutedConfiguration?.id, "faers")
                        if(masterVaersExecutedConfiguration?.id)
                            clearDataMiningTables(masterVaersExecutedConfiguration?.id, "vaers")
                        if(masterVigibaseExecutedConfiguration?.id)
                            clearDataMiningTables(masterVigibaseExecutedConfiguration?.id, "vigibase")
                    }
                    break
                case 2:
                    // read files and persist in tables, code moved to dss executor job
                    break
                case Constants.Commons.RESUME_REPORT:
                case Constants.Commons.RESUME_SPOTFIRE:
                    List<ExecutedConfiguration> allExConfigs = ExecutedConfiguration.findAllByMasterExConfigId(masterExecutedConfiguration?.id)
                    executionStatusList = ExecutionStatus.findAllByExecutedConfigIdInListAndExecutionLevel(allExConfigs*.id, executionStatus.executionLevel)
                    exConfigs = ExecutedConfiguration.findAllByIdInList(executionStatusList*.executedConfigId)
                    exConfigs.collate(20).each { List<ExecutedConfiguration> executedConfigurationList ->
                        executedConfigurationList.each { ExecutedConfiguration executedConfiguration ->
                            ExecutedConfiguration executedFaersConfiguration = ExecutedConfiguration.findByConfigIdAndSelectedDatasource(executedConfiguration.configId, Constants.DataSource.FAERS)
                            aggregateCaseAlertService.invokeReportingForQuantAlert(executedConfiguration, executedFaersConfiguration?.id)
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

                                if (isReportingCompleted) {
                                    if (executionStatus.executionLevel == Constants.Commons.RESUME_REPORT)
                                        alertService.generateReport(executedConfiguration)
                                    alertService.generateSpotfireReport(executedConfiguration, executedFaersConfiguration)
                                }
                                updateConfigurationList(configurations, lockedConfiguration)
                            } catch (Throwable throwable) {
                                singleCaseAlertService.updateReportStatusForError(executedConfiguration)
                                throw throwable
                            }
                        }
                    }
                    break
            }
        } catch (Throwable th) {
            log.error(alertService.exceptionString(th))
            updateBulkExecutionStatusForError(executionStatus.id, executionStatusList, th.message)
            updateConfigurationList(configurations, lockedConfiguration)
            updateMasterConfig(lockedConfiguration, false)
            manageMasterExecutionQueue()
            throw  th
        }

    }

    void level2(ExecutionStatus executionStatus, Long lockedConfigurationId, MasterExecutedConfiguration masterExecutedConfiguration, MasterExecutedConfiguration masterFaersExecutedConfiguration=null, List exFaersConfigs=[], List evdasConfigurations=[], MasterEvdasConfiguration masterEvdasConfiguration=null, Boolean isResume= false) {

        try {
            MasterConfiguration masterConfiguration = MasterConfiguration.findById(lockedConfigurationId)
            String masterFilePath = "${grailsApplication.config.signal.alert.file}/${masterExecutedConfiguration.id}"
            saveQuantData(executionStatus, masterConfiguration, masterExecutedConfiguration, masterFilePath, masterFaersExecutedConfiguration, exFaersConfigs, evdasConfigurations, masterEvdasConfiguration, isResume)
        } catch (Throwable th) {
            log.error(th.printStackTrace())
            throw th
        }


    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception)
    void saveQuantData(ExecutionStatus masterExecutionStatus, MasterConfiguration masterConfiguration, MasterExecutedConfiguration masterExecutedConfiguration, String filePath, MasterExecutedConfiguration masterFaersExecutedConfiguration=null, List exFaersConfigs = [], List evdasConfigurations=[], MasterEvdasConfiguration masterEvdasConfiguration=null, Boolean isResume= false) throws Exception {
        try {

            List<ExecutedConfiguration> exConfigs = ExecutedConfiguration.findAllByMasterExConfigIdAndRunCount(masterExecutedConfiguration.id, masterExecutedConfiguration.runCount?:0)
            //List<ExecutedConfiguration> exFaersConfigs = []

            if(masterFaersExecutedConfiguration && !exFaersConfigs)
                exFaersConfigs = ExecutedConfiguration.findAllByMasterExConfigId(masterFaersExecutedConfiguration.id)

            if(masterEvdasConfiguration && !evdasConfigurations)
                evdasConfigurations = EvdasConfiguration.findAllByMasterConfigId(masterEvdasConfiguration.id)


            List<ExecutionStatus> executionStatusList = ExecutionStatus.findAllByExecutedConfigIdInList(exConfigs*.id)

            exConfigs.collate(25).each { List<ExecutedConfiguration> executedConfigurationList ->
                ExecutorService executorService = Executors.newFixedThreadPool(8)

                List<Future> futureList = executedConfigurationList.collect { ExecutedConfiguration executedConfig ->
                    executorService.submit({ ->

                        //Map otherDataSourcesExecIds = ["pva": executedConfig.id]
                        //if()
                        Map<String, List<Map>> alertDataMap = [:]
                        List<String> allFiles = []
                        if (false) {//(configuration.adhocRun) {
                            //List<Map> alertDataList = alertService.loadAlertDataFromFile(filePath)
                            //aggregateOnDemandAlertService.createAlert(configuration.id, executedConfiguration.id, alertDataList)
                        } else {
                            Map otherDataSourcesExecIds = [:]
                            executedConfig.selectedDatasource.split(',').each {
                                //if (otherDataSourcesExecIds.containsKey(it)) {
                                Long dbExConfigId = executedConfig.id
                                ExecutedConfiguration dbExConfig = executedConfig
                                if(it == "faers" && executedConfig.selectedDatasource.contains("pva")) {
                                    dbExConfig = exFaersConfigs.find {it.name == executedConfig.name}
                                    dbExConfigId = dbExConfig?.id
                                }
                                if(it == "eudra") {
                                    def dbEvConfig = evdasConfigurations.find {it.name == executedConfig.name}
                                    dbExConfigId = dbEvConfig?.id

                                }
                                String integratedFilePath = "${filePath}/${dbExConfigId}_${executedConfig.type}_${it}"
                                List<Map> alertDataList = []
                                try {
                                    allFiles << integratedFilePath
                                    alertDataList = alertService.loadAlertDataFromFile(integratedFilePath)
                                } catch (Exception ex) {
                                    log.error("File not found: " + ex.printStackTrace())
                                }

                                if(it != "eudra") {
                                    List<Map> prrList = []
                                    List<Map> abcdList = []
                                    List<Map> rorList = []
                                    String prrFilePath = filePath + "/prr"
                                    String abcdFilePath = filePath + "/abcd"
                                    String rorFilePath = filePath + "/ror"
                                    try {
                                        if(it == "pva"){
                                            prrList = getAlertDataFromFile(dbExConfig, prrFilePath, it, allFiles)
                                            rorList = getAlertDataFromFile(dbExConfig, rorFilePath, it, allFiles)
                                            setEcPrrData(prrList)
                                            setEcRorData(rorList)
                                        }else{
                                            prrList = getAlertDataFromFile(dbExConfig, prrFilePath, it, allFiles)
                                            setEcPrrRorData(prrList)
                                        }
                                        abcdList = getAlertDataFromFile(dbExConfig, abcdFilePath, it, allFiles)
                                        setEcAbcdData(abcdList)
                                        abcdList=[]
                                        prrList = []
                                    } catch (Throwable th) {
                                        log.error("Error in saving prr data: " + th.printStackTrace())
                                    }
                                }
                                alertDataMap.put(it, alertDataList)
                                alertDataList = []
                                otherDataSourcesExecIds.put(it, dbExConfigId)
                                //}
                            }
                            ExecutionStatus executionStatus = executionStatusList.find { it -> it.executedConfigId == executedConfig.id }
                            try {
                                log.info(otherDataSourcesExecIds)
                                Long integratedFaersMasterId = masterFaersExecutedConfiguration?.id
                                aggregateCaseAlertService.createAlert(executedConfig.configId, executedConfig.id, alertDataMap, otherDataSourcesExecIds, integratedFaersMasterId)
                                synchronized (MasterExecutorService.class){
                                    setSuccessForConfiguration(executedConfig.configId, executedConfig.id, executionStatus.id, isResume, masterConfiguration)
                                    reportExecutorService.updateExecutionStatusLevel(executionStatus, null, true)
                                }
                                deleteSuccessFiles(allFiles)
                                deleteDssSuccessFile(executedConfig.id, masterExecutedConfiguration?.id)

                            }
                            catch (Throwable th) {
                                ExecutionStatusException ese = new ExecutionStatusException(alertService.exceptionString(th))
                                synchronized (MasterExecutorService.class) {
                                    handleFailedExecution(ese, executedConfig.configId, executedConfig.id, executedConfig.type, executionStatus.id, isResume, masterConfiguration)
                                }
                            }
                        }
                    } as Runnable)
                }
                futureList.each {
                    try {
                        it.get(600000, TimeUnit.MILLISECONDS)
                    } catch(Throwable th) {
                        log.error(th.printStackTrace())
                    }
                }
                executorService.shutdown()
            }


        } catch (Throwable throwable) {
            log.error("Exception came in saving the Quantitative alerts. ", throwable)
            throw throwable
        }
    }

    def getMasterPrrData(List<ExecutedConfiguration> executedConfigurations, String dataSource, MasterExecutedConfiguration masterExecutedConfiguration) {
        Long executedConfigId = masterExecutedConfiguration.id
        List executedConfigIds = executedConfigurations*.id
        Sql sql = new Sql(signalDataSourceService.getReportConnection(dataSource))
        try{
            String getPrrQuery = "select * FROM pvs_prr_full_data_$executedConfigId where child_execution_id in (" + executedConfigIds.join(",")+ ")"
            sql.eachRow(getPrrQuery, []) { row->
                Map statsProperties = [:]
                Long childExecutedConfigid = row.CHILD_EXECUTION_ID as Long
                statsProperties.prrValue = row.NORMAL_PRR as Double
                statsProperties.prrUCI = row.NORMAL_UPPER_CI_PRR as Double
                statsProperties.prrLCI = row.NORMAL_LOWER_CI_PRR as Double
                statsProperties.prrStr = row.PRR
                statsProperties.prrStrLCI = row.LCI_PRR
                statsProperties.prrStrUCI = row.UCI_PRR
                statsProperties.prrMh = row.MH_PRR as Double
                statsProperties.rorValue = row.NORMAL_ROR as Double
                statsProperties.rorLCI = row.NORMAL_LOWER_95_CI_ROR as Double
                statsProperties.rorUCI = row.NORMAL_UPPER_95_CI_ROR as Double
                statsProperties.rorStr = row.ROR
                statsProperties.rorStrLCI = row.LCI_ROR
                statsProperties.rorStrUCI = row.UCI_ROR
                statsProperties.rorMh = row.MH_ROR as Double
                statsProperties.chiSquare = row.NORMAL_CHI_SQ as Double
                String productId = row.PRODUCT_NAME
                String eventId = row.PT_NAME ? String.valueOf(row.PT_NAME).replace('"', '') : ""
                dataObjectService.setProbDataMap(childExecutedConfigid, productId, eventId, statsProperties)
            }
        }catch (Throwable throwable) {
            log.error("Exception came in executing the mining run. ", throwable)
            throw throwable
        } finally {
            sql?.close()
        }

    }

    List fetchMasterEvdasAlertData(EvdasConfiguration evdasConfiguration, List evdasConfigs, MasterEvdasConfiguration masterEvdasConfiguration) throws Exception {
        ArrayList<Map> alertData = []
        Sql sql
        try {
            sql = new Sql(dataSource_eudra)
            String gttInserts = evdasSqlGenerationService.initializeInsertGtts(evdasConfiguration, evdasConfigs, masterEvdasConfiguration)
            log.info(gttInserts)
            sql.execute(gttInserts)
            log.info("GTT Inserts executed.")

            String queryInserts = evdasSqlGenerationService.initializeQuerySql(evdasConfiguration)
            log.info(queryInserts)
            sql.execute(queryInserts)
            log.info("Query Inserts executed.")
            alertData = evdasAlertExecutionService.prepareAlertDataFromMart(sql, evdasConfiguration, true)
        } catch (Throwable throwable) {
            log.error("Exception came in executing the mining run. ", throwable)
            throw throwable
        } finally {
            sql?.close()
        }
        alertData
    }

    List fetchMasterCountData(List exConfigs, Long executedConfigId, String selectedDatasource, boolean isEventGroup) {
        List resultData = []
        String reportSql = generateCustomReportSQL(executedConfigId, selectedDatasource, isEventGroup, exConfigs)
        def newFields = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', true)
        String tempSql = ''
        newFields?.each {
            if (it.isNewColumn && it.keyId && it.enabled) {
                String key = it.keyId.replace("REPORT_","").replace("_FLG","")
                tempSql = tempSql + "," + "NEW_" + key + "_COUNT" + " AS " + "NEW_" + key + "_COUNT" + " "
                tempSql = tempSql + "," + "CUMM_" +key+ "_COUNT" + " AS " + "CUMM_" + key + "_COUNT" + " "
            }
        }
        if (newFields != null) {
            reportSql = reportSql.replaceAll(",#NEW_DYNAMIC_COUNT", tempSql)
        } else {
            reportSql = reportSql.replaceAll(",#NEW_DYNAMIC_COUNT", ' ')
        }
        Sql sql = new Sql(signalDataSourceService.getReportConnection(selectedDatasource))

        try{
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
                    map.put(it.key, value)
                }
                resultData.add(map)
            }
            return resultData
        }catch(Exception ex){
            throw ex
        }finally{
            sql?.close()
        }
    }

    String generateCustomReportSQL(Long executedConfigId, String selectedDatasource, boolean isEventGroup, List exConfigs) {
        String sieveAnalysisQueryName = Holders.config.pvsignal.sieveAnalysisQueryName
        SuperQueryDTO sieveAnalysisQuery = queryService.queryDetailByName(sieveAnalysisQueryName)
        if(sieveAnalysisQuery && ((selectedDatasource.startsWith(Constants.DataSource.PVA) && Holders.config.signal.sieveAnalysis.safetyDB) ||(selectedDatasource.startsWith(Constants.DataSource.FAERS) && Holders.config.signal.sieveAnalysis.faers))) {
            return SignalQueryHelper.agg_count_sql_sv(executedConfigId, isEventGroup, exConfigs,selectedDatasource)
        }
        return SignalQueryHelper.agg_count_sql(executedConfigId, isEventGroup, exConfigs,selectedDatasource)
    }

    void checkDbActivityStatus(Long executedConfigId, String datasource, Map result, Map flagMap) {
        int countStatus = 0, ebgmStatus = 0, prrStatus = 0, dssStatus = 0
        Sql sql
        try {
            sql = new Sql(signalDataSourceService.getReportConnection(datasource))
            // check ebgm/statistics data status
            if (result.countsCompleted == false) {
                //Open the select result data based on the status query.
                // check count data done
                String countQuery = SignalQueryHelper.master_count_status_sql(executedConfigId)
                sql.eachRow(countQuery) { ResultSet resultSetObj ->
                    countStatus = resultSetObj.getInt("CURRENT_STATUS")
                }
                if (countStatus == Holders.config.alertExecution.status.success.code)
                    result.countsCompleted = true
            }

            // check count data status
            if (result.ebgmCompleted == false && flagMap.ebgmFlag == true) {
                String ebgmQuery = SignalQueryHelper.master_ebgm_status_sql(executedConfigId)
                sql.eachRow(ebgmQuery) { ResultSet resultSetObj ->
                    ebgmStatus = resultSetObj.getInt("CURRENT_STATUS")
                }
                if (ebgmStatus == Holders.config.alertExecution.status.success.code)
                    result.ebgmCompleted = true
            }

            // check prr/ror data status
            if (result.prrCompleted == false && flagMap.prrFlag == true) {
                String prrQuery = SignalQueryHelper.master_prr_status_sql(executedConfigId)
                sql.eachRow(prrQuery) { ResultSet resultSetObj ->
                    prrStatus = resultSetObj.getInt("CURRENT_STATUS")
                }
                if (prrStatus == Holders.config.alertExecution.status.success.code)
                    result.prrCompleted = true
            }

            // check prr/ror data status
            if (result.dssCompleted == false && flagMap.dssFlag == true) {
                String dssQuery = SignalQueryHelper.master_dss_status_sql(executedConfigId)
                sql.eachRow(dssQuery) { ResultSet resultSetObj ->
                    dssStatus = resultSetObj.getInt("CURRENT_STATUS")
                }
                if (dssStatus == Holders.config.alertExecution.status.success.code)
                    result.dssCompleted = true
            }

            if (countStatus == Holders.config.alertExecution.status.failure.code ||
                    ebgmStatus == Holders.config.alertExecution.status.failure.code ||
                    prrStatus == Holders.config.alertExecution.status.failure.code ||
                    dssStatus == Holders.config.alertExecution.status.failure.code) {
                throw new Exception("DB activity failed!")
            }
        } catch(Throwable th) {
            log.error("Error occurred while fetching DB activity status.", th)
            throw th
        } finally {
            sql?.close()
        }


    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception)
    MasterExecutedConfiguration createMasterExecutedConfiguration(Long lockedConfigurationId, ExecutionStatus executionStatus = null, String datasource = '') {
        MasterConfiguration lockedConfiguration = MasterConfiguration.findById(lockedConfigurationId)
        MasterExecutedConfiguration executedConfiguration = new MasterExecutedConfiguration(name: lockedConfiguration.name, productHierarchy: lockedConfiguration.productHierarchy,
                lastX: lockedConfiguration.lastX, asOfVersionDate: lockedConfiguration.asOfVersionDate, scheduler: lockedConfiguration.scheduler,
                dateRangeType: lockedConfiguration.dateRangeType, configTemplate: lockedConfiguration.configTemplate,
                startDate: lockedConfiguration.startDate, endDate: lockedConfiguration.endDate,
                executing: lockedConfiguration.executing, masterConfigId: lockedConfiguration.id)
        if(datasource){
            executedConfiguration.isEnabled = false
            executedConfiguration.datasource = datasource
            executedConfiguration.integratedExId = executionStatus.executedConfigId
        } else {
            executedConfiguration.isEnabled = lockedConfiguration.isEnabled
        }
        executedConfiguration.save(flush:true)

    }

    ExecutedConfiguration createExecutedConfigurationForAlert(Configuration lockedConfiguration, MasterExecutedConfiguration masterExecutedConfiguration, Configuration integratedConfiguration=null) throws Exception {
        ExecutedConfiguration executedConfiguration = null
        log.info("Now creating the alert : " + lockedConfiguration.name)
        try {
            executedConfiguration = reportExecutorService.saveExecutedConfiguration(lockedConfiguration, masterExecutedConfiguration, integratedConfiguration)
            //executedConfiguration.masterExConfigId = masterExecutedConfiguration.id
            //executedConfiguration.save()
        } catch (Throwable exception) {
            log.error("Error happened when creating executed configuration " +
                    "for ${lockedConfiguration.name}. Skipping", exception)
            throw exception
        }

        executedConfiguration
    }

    void initiateMasterRun(Long lockedConfigurationId, MasterExecutedConfiguration masterExecutedConfiguration,
                           List configurations, List exConfigs, String dataSource) {
        Sql sql
        try {
            MasterConfiguration lockedConfiguration = MasterConfiguration.findById(lockedConfigurationId)
            Boolean isIntegratedVaers = (dataSource == Constants.DataSource.VAERS)
            log.info("selectedDatasource is : $dataSource")
            sql = new Sql(signalDataSourceService.getReportConnection(dataSource))
            log.info("Generating the sql queries.")
            Long sectionStart = System.currentTimeMillis()
            Configuration configuration = configurations[0]
            ExecutedConfiguration executedConfiguration = exConfigs[0]
            SuperQueryDTO superQueryDTO = queryService.queryDetail(configuration.alertQueryId)
            List dataSheetsWithId=[]
            def datasheetMap=[:]
            exConfigs?.each{
                if(it.selectedDataSheet && !TextUtils.isEmpty(it.selectedDataSheet)){
                    datasheetMap =[id:it.id ,selectedDatasheet:it.selectedDataSheet]
                    dataSheetsWithId.add(datasheetMap)
                }
            }
            reportExecutorService.prepareGttsAndVersionSqls(configuration, executedConfiguration, true, true, sql, superQueryDTO,
                    sectionStart, dataSource, isIntegratedVaers, false, false, null, false, lockedConfiguration, masterExecutedConfiguration, exConfigs,dataSheetsWithId)
            log.info("Call to p_persist_case_series_data is made.")
            String caseSeriesProc = sqlGenerationService.persistCaseSeriesExecutionData(masterExecutedConfiguration.id)
            sql.call(caseSeriesProc)
            log.info("Call to p_persist_case_series_data is ended.")
            log.info("pkg_ebgm_data_mining package calling started.")
            log.info(new Date().toString())
            String popAggDataProc = initiateMasterAggregateDataMining(executedConfiguration.id, configuration, masterExecutedConfiguration, dataSource)
            sql.call(popAggDataProc)
            log.info(new Date().toString())
            log.info("pkg_ebgm_data_mining package calling ended.")

            Thread.sleep(20000)
        } catch(Throwable th) {
            log.error("Error happened while calling DB packages.", th)
            throw th
        }finally{
            sql?.close()
        }
    }

    def initiateMasterAggregateDataMining(def executedConfigurationId, Configuration configuration, MasterExecutedConfiguration masterExecutedConfiguration, String dataSource) {
        //Values to be passed to the stored proc.
        Date startDate = configuration?.alertDateRangeInformation?.reportStartAndEndDate?.get(0)
        Date endDate = configuration?.alertDateRangeInformation?.reportStartAndEndDate?.get(1)
        Date asOfVersionDate = configuration.asOfVersionDate
        def runId = masterExecutedConfiguration.id
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
        def runType = businessConfigurationService.getConfigurationType(dataSource)
        int smq = 0
        if(configuration.groupBySmq && configuration.eventGroupSelection){
            smq = 0
        } else if(configuration.groupBySmq){
            smq = 1
        }
        def followUpExists = configuration.excludeFollowUp ? 1 : 0
        String asOfVersionDateString = asOfVersionDate ? """TO_DATE('${
            new SimpleDateFormat(SqlGenerationService.DATE_FMT).format(asOfVersionDate)
        }', '${
            SqlGenerationService.DATETIME_FMT_ORA
        }')""" : null

        String popAggDataProc

        if (dataSource == Constants.DataSource.FAERS &&
                configuration.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            String className = configuration.drugClassification ?: ''

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
                             null,
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

        } else {
            def includeLockedCases = configuration.includeLockedVersion ? 1 : 0
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
                             null,
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

    void saveMasterQuantData(Configuration configuration, ExecutedConfiguration executedConfiguration, String filePath, Map<String, Long> otherDataSourcesExecIds) throws Exception {
        try {
            List<String> allFiles = [filePath]
            Map<String, List<Map>> alertDataMap = [:]
            configuration.selectedDatasource.split(',').each {
                if (otherDataSourcesExecIds.containsKey(it)) {
                    String integratedFilePath = "${grailsApplication.config.signal.alert.file}/${executedConfiguration.id}_${executedConfiguration.type}_${it}"
                    allFiles.add(integratedFilePath)
                    List<Map> alertDataList = alertService.loadAlertDataFromFile(integratedFilePath)
                    alertDataMap.put(it, alertDataList)
                }
            }
            //aggregateCaseAlertService.createAlert(configuration.id, executedConfiguration.id, alertDataMap, otherDataSourcesExecIds)
            for(String fileName: allFiles){
                File file = new File(fileName)
                if (file.exists()) {
                    file.delete()
                }
            }
        } catch (Throwable throwable) {
            log.error("Exception came in saving the Quantitative alerts. ", throwable)
            throw throwable
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    def setMasterErrorForConfiguration(MasterConfiguration lockedConfiguration, Long execConfigId, Long executionStatusId) {

        MasterExecutedConfiguration executedConfiguration
        ExecutionStatus executionStatus
        try {
            //lockedConfiguration = MasterConfiguration.get(configId)
            executedConfiguration = MasterExecutedConfiguration.get(execConfigId)
            executionStatus = ExecutionStatus.get(executionStatusId)
            if (executionStatus) {

                executionStatus.endTime = System.currentTimeMillis()
                //setTotalExecutionTimeForConfiguration(lockedConfiguration, (executionStatus.endTime - executionStatus.startTime))
                //setNextRunDateForConfiguration(lockedConfiguration)
                lockedConfiguration.executing = false
                lockedConfiguration.isEnabled = false
                lockedConfiguration.isResume = false


                if (executionStatus?.executionStatus != ReportExecutionStatus.ERROR) {
                    executionStatus?.executionStatus = ReportExecutionStatus.COMPLETED
                }



            }
        } catch (Throwable th) {
            log.error("Error happened when handling Successful Configurations [${lockedConfiguration?.id}]", th)
            throw th
        } finally {
            if (lockedConfiguration) {
                lockedConfiguration.save(flush:true)
            }
            if (executionStatus) {
                executionStatus.save(flush:true)
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    def setSuccessForConfiguration(Long configId, Long execConfigId, Long executionStatusId, Boolean isResume = false, MasterConfiguration masterConfiguration) {

        Configuration lockedConfiguration
        ExecutedConfiguration executedConfiguration
        ExecutionStatus executionStatus
        try {
            lockedConfiguration = Configuration.get(configId)
            executedConfiguration = ExecutedConfiguration.get(execConfigId)
            executionStatus = ExecutionStatus.get(executionStatusId)
            if (executionStatus) {
                lockedConfiguration.executing = false
                lockedConfiguration.isResume = false
                lockedConfiguration.numOfExecutions = isResume?lockedConfiguration.numOfExecutions:  lockedConfiguration.numOfExecutions + 1
                lockedConfiguration.nextRunDate = isResume ? getMasterNextDate(masterConfiguration.scheduler, masterConfiguration.nextRunDate) : masterConfiguration.nextRunDate
                reportExecutorService.adjustCustomDateRanges(lockedConfiguration) // Added custom date range for master child alert
                reportExecutorService.adjustAsOfVersion(lockedConfiguration, executedConfiguration)

                if (executedConfiguration) {
                    //After that set the notification.
                    reportExecutorService.addNotification(executedConfiguration, executionStatus, executedConfiguration.assignedTo, executedConfiguration.assignedToGroup, "app.notification.completed")
                    reportExecutorService.emailNotificationService.emailHanlderAtAlertLevel(executedConfiguration, executionStatus)
                }
            }
        } catch (Throwable th) {
            log.error("Error happened when handling Successful Configurations [${lockedConfiguration.id}]", th)
            throw th
        } finally {
            if (lockedConfiguration) {
                lockedConfiguration.save(flush:true)
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    def setSuccessForMasterConfiguration(Long configId, Long execConfigId, Long executionStatusId) {

        MasterConfiguration lockedConfiguration
        MasterExecutedConfiguration executedConfiguration
        ExecutionStatus executionStatus
        try {
            lockedConfiguration = MasterConfiguration.get(configId)
            executedConfiguration = MasterExecutedConfiguration.get(execConfigId)
            if (lockedConfiguration) {

                lockedConfiguration.numOfExecutions = lockedConfiguration.isResume ? (lockedConfiguration.numOfExecutions ?: 1) : configuration.numOfExecutions + 1
                lockedConfiguration.nextRunDate = getMasterNextDate(lockedConfiguration.scheduler, lockedConfiguration.nextRunDate)
                lockedConfiguration.executing = false
                lockedConfiguration.isEnabled = false
                lockedConfiguration.isResume = false

            }
        } catch (Throwable th) {
            log.error("Error happened when handling Successful Configurations [${lockedConfiguration.id}]", th)
            throw th
        } finally {
            if (lockedConfiguration) {
                lockedConfiguration.save()
            }

        }
    }

    MasterConfiguration createMasterConfig(Long configId, String datasource) {
        MasterConfiguration config = MasterConfiguration.findById(configId)
        MasterConfiguration masterConfiguration = new MasterConfiguration(
                name: config.name,
                productHierarchy: config.productHierarchy,
                laxtX: config.lastX,
                dateRangeType: config.dateRangeType,
                asOfVersionDate: config.asOfVersionDate,
                startDate: config.startDate,
                endDate: config.endDate,
                scheduler: config.scheduler,
                configTemplate: config.configTemplate,
                isEnabled: false, executing: false,
                owner: config.owner,
                datasource: datasource,
                integratedId: config.id
                //owner
        )
        masterConfiguration.save()
    }

    void createDir(String logsFilePath) {
        File logsFileDir = new File(logsFilePath)
        if (!logsFileDir.exists()) {
            logsFileDir.mkdir()
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    void updateBulkExecutionStatusLevel(Long executionStatusId, List<ExecutionStatus> executionStatusList, Integer status = null) {
        try {
            ExecutionStatus executionStatus = ExecutionStatus.get(executionStatusId)
            executionStatus.refresh()
            int executionLevel = status ? status : executionStatus.executionLevel
            String timeStampJSON = executionStatus.timeStampJSON
            List exStatusIds = executionStatusList*.id
            String updateQuery = "update ex_status set EXECUTION_LEVEL = " + executionLevel + " , TIME_STAMPJSON = '" + timeStampJSON + "' where id in (" + exStatusIds.join(",") + ")"
            log.info(updateQuery)
            SQLQuery sql = null
            Session session = sessionFactory.currentSession
            sql = session.createSQLQuery(updateQuery)
            sql.executeUpdate()
            session.flush()
            session.clear()
        } catch (Throwable th) {
            log.error("Error occurred while updating the execution status.", th)
            throw th
        }

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    void updateBulkExecutionStatusForError(Long executionStatusId, List<ExecutionStatus> executionStatusList, String message) {
        Transaction tx = null
        Session session = null
        try {
            SQLQuery sql = null
            session = sessionFactory.openSession()
            tx = session.beginTransaction()
            ExecutionStatus executionStatus = ExecutionStatus.get(executionStatusId)
            String timeStampJSON = executionStatus.timeStampJSON
            long endTime = System.currentTimeMillis()
            List exStatusIds = executionStatusList*.id
            exStatusIds << executionStatusId
            String errorCauseString = message?.length() > 32000 ? message?.substring(0,32000) : message
            String updateQuery = "update ex_status set TIME_STAMPJSON = '" + timeStampJSON + "', end_time = " + endTime + ", STACK_TRACE = '" + errorCauseString?.replaceAll("'", "''") + "',  EX_STATUS = '" + ReportExecutionStatus.ERROR + "'"

            if (executionStatusList) {
                ExecutionStatus executionStatus1 = executionStatusList[0]
                if (executionStatus1.reportExecutionStatus == ReportExecutionStatus.SCHEDULED ||
                        executionStatus1.reportExecutionStatus == ReportExecutionStatus.GENERATING) {
                    updateQuery = updateQuery + ", REPORT_EXECUTION_STATUS = '" + ReportExecutionStatus.ERROR + "'"
                }
                if (executionStatus1.spotfireExecutionStatus == ReportExecutionStatus.SCHEDULED ||
                        executionStatus1.spotfireExecutionStatus == ReportExecutionStatus.GENERATING) {
                    updateQuery = updateQuery + ", SPOTFIRE_EXECUTION_STATUS = '" + ReportExecutionStatus.ERROR + "'"
                }
            }
            updateQuery = updateQuery + " where"
            updateQuery = collateInQueryLimit(updateQuery, exStatusIds)
            log.info(updateQuery)
            sql = session.createSQLQuery(updateQuery)
            sql.executeUpdate()
            tx.commit()
            session.clear()

            executionStatusList.each { execStatus -> // add notification and audit log for alert failure
                ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(execStatus.executedConfigId)
                if (executedConfiguration)
                    reportExecutorService.addNotification(executedConfiguration, execStatus, executedConfiguration.assignedTo, executedConfiguration.assignedToGroup, "app.notification.failed")

            }
        } catch (Throwable th) {
            if (tx != null && tx.getStatus() == ACTIVE) {
                tx.rollback()
            }
            log.error("Error occurred while updating the execution status. : ${alertService.exceptionString(th)}")
        } finally {
            session?.close()
        }
    }

    String collateInQueryLimit(String query, List inList) {
        inList.collate(999).each { List subList ->
            query = query + " ID in (" + String.join(", ", subList.collect { it.toString() }) + ") or"
        }
        query.substring(0, query.length() - 2)
    }

    void execPRRData(String db, Long executedConfigId) {
        //Call the stats data stored proc with run id and batch size.
        Sql sql = null
        try {
            sql = new Sql(signalDataSourceService.getReportConnection(db))
            String queryCalling = "{call p_send_stats_data_prr_all(?)}"
            sql.call(queryCalling, [executedConfigId])
        } catch (Exception ex) {
            throw ex
        } finally {
            sql?.close()
        }
    }

    void execABCDData(String db, Long executedConfigId){
        //Call the stats data stored proc with run id and batch size.
        Sql sql = null
        try {
            sql = new Sql(signalDataSourceService.getReportConnection(db))
            String queryCalling = "{call p_create_ebgm_final_table(?)}"
            sql.call(queryCalling, [executedConfigId])
        } catch (Exception ex) {
            throw ex
        } finally {
            sql?.close()
        }
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception)
    void createExecConfigsForMaster(List configurations, MasterExecutedConfiguration masterExecutedConfiguration, List dataSources, List exConfigs, List executionStatusList ) {
        configurations.each { childConfigId ->
            Configuration childConfig = Configuration.get(childConfigId)
            ExecutedConfiguration executedConfiguration = createExecutedConfigurationForAlert(childConfig, masterExecutedConfiguration)
            childConfig.save(flush: true)
            executionStatusList << createExecutionStatus(childConfig.id, false, executedConfiguration)
            exConfigs << executedConfiguration

        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    def handleFailedMasterExecution(ExecutionStatusException ese, MasterConfiguration config,
                                    Long executedConfigId, String alertType, Long execStatusId = null, Boolean isMaster = false) {
        try {

            def executedConfiguration = null
            if (executedConfigId) {
                executedConfiguration = MasterExecutedConfiguration.get(executedConfigId)
                executedConfiguration.isEnabled = false
                //executedConfiguration.executing = false

                executedConfiguration.save(flush: true)
            }
            ExecutionStatus executionStatus = null
            if (execStatusId) {
                executionStatus = ExecutionStatus.get(execStatusId)
            } else if (executedConfigId) {
                executionStatus = ExecutionStatus.findByExecutedConfigIdAndType(executedConfigId, alertType)
            }
            if (executionStatus) {
                executionStatus.stackTrace = ese?.errorCause
                if(executionStatus.executionStatus != ReportExecutionStatus.COMPLETED)
                    executionStatus.executionStatus = ReportExecutionStatus.ERROR

                if (alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT && executionStatus.executionLevel == 1) {
                    executionStatus.executionLevel = 0
                }
                executionStatus.save(flush:true)


                if(!config.isAttached()) {
                    String updateQuery = "update master_configuration set executing = false, is_resume = false where id = " + config.id
                    SQLQuery sql = null
                    Session session = sessionFactory.currentSession
                    sql = session.createSQLQuery(updateQuery)
                    sql.executeUpdate()
                    session.flush()
                    session.clear()
                } else {
                    config.executing = false
                    config.isResume = false
                    config.save()
                }

            } else {
                log.error("Cannot find the execution status. [handleFailedExecution]")
            }
        } catch (Throwable throwable) {
            log.error("Error happened when handling failed Configurations [${executedConfigId}]", throwable)
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    def handleFailedExecution(ExecutionStatusException ese, Long configId,
                              Long executedConfigId, String alertType, Long execStatusId = null, Boolean isResume= false, MasterConfiguration masterConfiguration) {
        try {
            Configuration config = Configuration.get(configId)
            def executedConfiguration = null
            if (executedConfigId) {
                executedConfiguration = ExecutedConfiguration.get(executedConfigId)
                executedConfiguration.isEnabled = false

                executedConfiguration.save(flush: true)
            }
            ExecutionStatus executionStatus = null
            if (executedConfigId) {
                executionStatus = ExecutionStatus.findByExecutedConfigIdAndType(executedConfigId, alertType)
            } else if (execStatusId) {
                executionStatus = ExecutionStatus.get(execStatusId)
            }
            String errorCauseString = ese?.errorCause?.length() > 32000 ? ese?.errorCause?.substring(0,32000) : ese?.errorCause
            if (executionStatus) {
                String exStatusUpdateQuery = "update ex_status set END_TIME =" + System.currentTimeMillis() + " , STACK_TRACE = '" + errorCauseString + "'"

                if(executionStatus.executionStatus != ReportExecutionStatus.COMPLETED)
                    exStatusUpdateQuery += ", EX_STATUS = '" + ReportExecutionStatus.ERROR + "'"

                if (config.templateQueries && executionStatus.reportExecutionStatus != ReportExecutionStatus.COMPLETED)
                    exStatusUpdateQuery += ", REPORT_EXECUTION_STATUS = '" + ReportExecutionStatus.ERROR + "'"

                if (config.spotfireSettings && executionStatus.spotfireExecutionStatus != ReportExecutionStatus.COMPLETED) {
                    exStatusUpdateQuery += ", SPOTFIRE_EXECUTION_STATUS = '" + ReportExecutionStatus.ERROR + "'"
                }

                if (alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT && executionStatus.executionLevel == 1) {
                    exStatusUpdateQuery += ", EXECUTION_LEVEL =" + 0 + ""
                }

                exStatusUpdateQuery += " WHERE id =" + executionStatus?.id
                log.info("exStatusUpdate query......"+exStatusUpdateQuery)
                final Sql sqlObj = new Sql(dataSource)
                try {
                    sqlObj.executeUpdate(exStatusUpdateQuery)
                    log.info("exStatus table updated......"+executionStatus?.id)
                } catch (Exception e) {
                    log.error("exStatus table updation error"+e.printStackTrace())
                } finally {
                    sqlObj.close()
                }

                Integer numOfExec = isResume? config.numOfExecutions : (config.numOfExecutions?:0)+1
                Date nextRunDate = masterConfiguration.nextRunDate

                if(!config.isAttached()) {
                    String updateQuery = "update rconfig set num_of_executions = "+numOfExec+", executing = false"
                    if(nextRunDate){
                        updateQuery = updateQuery + ", next_run_date = " + "TO_TIMESTAMP('${nextRunDate?.format(SqlGenerationService.DATETIME_FMT)}', '${SqlGenerationService.DATETIME_FMT_ORA}')" + ""
                    } else {
                        updateQuery = updateQuery + ", next_run_date = " + null + ""
                    }
                    updateQuery = updateQuery + " where id = " + config.id
                    log.info("update failed config")
                    log.info(updateQuery)
                    SQLQuery sql = null
                    Session session = sessionFactory.currentSession
                    sql = session.createSQLQuery(updateQuery)
                    sql.executeUpdate()
                    session.flush()
                    session.clear()
                } else {
                    config.executing = false
                    config.numOfExecutions = numOfExec
                    config.nextRunDate = nextRunDate
                    config.save()
                }

                reportExecutorService.addNotification(executedConfiguration, executionStatus, executedConfiguration.assignedTo, executedConfiguration.assignedToGroup, "app.notification.failed") // add notification and audit log for failed alert
            } else {
                log.error("Cannot find the execution status. [handleFailedExecution]")
            }
        } catch (Throwable throwable) {
            log.error("Error happened when handling failed Configurations [${executedConfigId}]", throwable)
        }
    }

    String prepareMapforMasterDSS(List<ExecutedConfiguration> alertList, MasterExecutedConfiguration masterExecutedConfiguration, Long masterConfigurationId) {

        MasterConfiguration masterConfiguration = MasterConfiguration.findById(masterConfigurationId)
        JSONArray peList = new JSONArray()
        String masterFilePath = "${grailsApplication.config.signal.alert.file}/${masterExecutedConfiguration.id}"
        String prrFilePath = masterFilePath + "/prr"

        JSONObject productAlertMapping = new JSONObject()

        JSONArray alertIds = alertList*.id
        Map prevData = reportExecutorService.prevMasterExConfigs(masterConfiguration, masterExecutedConfiguration)
        List<ExecutedConfiguration> prevExConfigs = prevData.prevExConfigs
        JSONArray prevAlertIds = prevExConfigs*.id
        boolean smqFlag = alertList? alertList[0].isGroupBySmq():false

        for (ExecutedConfiguration ec: alertList){

            List<Map> aggregateAlertList = []
            try {
                aggregateAlertList = getAlertDataFromFile(ec, masterFilePath)
            } catch (Throwable th) {

            }
            List<Map> ebgmList = []
            Map status = [:]
            if(aggregateAlertList && aggregateAlertList[0]) {
                Boolean isProductGroup = ec.productGroupSelection != null
                Boolean isEventGroup = ec.eventGroupSelection != null
                String fileNameStr = (Holders.config.statistics.inputfile.path as String) + ec.id
                String productId = aggregateAlertList[0]["PRODUCT_ID"] as String
                status = statisticsService.mergeStatsScores(fileNameStr, Constants.DataSource.PVA, ec.id, aggregateAlertList, isProductGroup, isEventGroup, false, ec.masterExConfigId, productId)

            }
            List<Map> prrList = []
            try {
                prrList = getAlertDataFromFile(ec, prrFilePath)
            } catch (Throwable th) {

            }


            if(aggregateAlertList) {
                productAlertMapping.put(aggregateAlertList[0]["PRODUCT_NAME"] as String, ec.id as String)
            }

            for(Map alert : aggregateAlertList){
                Map prrMap = prrList.find {it.pt_code == alert.pt_code}
                Map statsData = [:]
                if(status.status == 1) {
                    if (alert["SMQ_CODE"])
                        statsData = dataObjectService.getStatsDataMapSubgrouping(ec.id, alert["PRODUCT_ID"],
                                alert["PT_CODE"] + Constants.Commons.DASH_STRING + alert["SMQ_CODE"])
                    else
                        statsData = dataObjectService.getStatsDataMapSubgrouping(ec.id, alert["PRODUCT_ID"], alert["PT_CODE"])

                }
                JSONObject peMap = new JSONObject()
                peMap.put("product_name", alert["PRODUCT_NAME"])
                peMap.put("pid", alert["PRODUCT_ID"])
                peMap.put("ptid", alert["PT_CODE"])
                peMap.put("pt", alert["PT"])
                peMap.put("ebgm_score", statsData?.ebgm?: 0.0)
                peMap.put("eb05_score", statsData?.eb05?: 0.0)
                peMap.put("eb95_score", statsData?.eb95?: 0.0)
                peMap.put("rorLCI_score", prrMap ? prrMap["NORMAL_LOWER_95_CI_ROR"] as Double: 0)
                peMap.put("rorUCI_score", prrMap ? prrMap["NORMAL_UPPER_95_CI_ROR"] as Double: 0)
                peMap.put("ror_value", prrMap? prrMap["NORMAL_ROR"] as Double: 0)
                peMap.put("listedness", alert["LISTED"]? "no" : "yes")
                peMap.put("smq_code", alert["SMQ_CODE"])
                peMap.put("new_count", alert["NEW_COUNT"])
                peMap.put("new_fatal_count", alert["NEW_FATAL_COUNT"])
                peMap.put("new_serious_count", alert["NEW_SERIOUS_COUNT"])
                peMap.put("prr_value", prrMap ? prrMap["NORMAL_PRR"] as Double: 0)
                peMap.put("prrLCI_score", prrMap ? prrMap["NORMAL_LOWER_CI_PRR"] as Double : 0.0)
                peMap.put("prrUCI_score", prrMap ? prrMap["NORMAL_UPPER_CI_PRR"] as Double: 0.0)

                peList.add(peMap)
            }

        }
        JSONObject jsonObject = new JSONObject()
        jsonObject.put("product_to_alert_mapping",productAlertMapping)
        jsonObject.put("alert_ids",alertIds)
        jsonObject.put("prev_alert_ids",prevAlertIds)
        jsonObject.put("smq_flag",smqFlag)
        jsonObject.put("peMap",peList)
        String fileNameStr = (Holders.config.statistics.inputfile.path as String)

        String alertFileName = fileNameStr +"DSS_REQUEST_${masterExecutedConfiguration.id}.txt"

        Writer writer = new FileWriter(alertFileName)
        jsonObject.write(writer)
        writer.close()

        return alertFileName
    }

    List getAlertDataFromFile(ExecutedConfiguration executedConfig, String filePath, String db="pva", List allFiles = []) {
        String integratedFilePath = "${filePath}/${executedConfig.id}_${executedConfig.type}_${db}"
        if(allFiles) {
            allFiles << integratedFilePath
        }
        List<Map> alertDataList = []
        try {
            alertDataList = alertService.loadAlertDataFromFile(integratedFilePath)
        } catch (Exception ex) {
            log.error("File not found: " + ex.printStackTrace())
        }
        alertDataList
    }
    List fetchMasterPrrDataForOtherDataSource(List exConfigs, Long executedConfigId, String selectedDatasource){
        List prrData = []
        List<Long> executedConfigIds = exConfigs
        String prrSql = "select * FROM pvs_prr_full_data_${executedConfigId} where child_execution_id in (" + executedConfigIds.join(",")+ ")"

        Sql sql = new Sql(signalDataSourceService.getReportConnection(selectedDatasource))
        try{
            sql.eachRow(prrSql) { GroovyResultSet resultSet ->
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
                prrData.add(map)
            }
            return prrData
        }catch(Exception ex){
            throw ex
        }finally{
            sql?.close()
        }
    }

    List fetchMasterPrrData(List exConfigs, Long executedConfigId, String selectedDatasource) {
        List prrData = []
        List<Long> executedConfigIds = exConfigs
        boolean prrSubGrpEnabled = cacheService.getSubGroupsEnabled("prrSubGrpEnabled") ?: false
        String prrSql = "select * FROM prr_full_data_${executedConfigId} where child_execution_id in (" + executedConfigIds.join(",")+ ")"
        if(prrSubGrpEnabled ){
            prrSql = "select * FROM pvs_prr_output_${executedConfigId} where child_execution_id in (" + executedConfigIds.join(",")+ ")"
        }
        Sql sql = new Sql(signalDataSourceService.getReportConnection(selectedDatasource))
        try{
            sql.eachRow(prrSql) { GroovyResultSet resultSet ->
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
                prrData.add(map)
            }
            return prrData
        }catch(Exception ex){
            throw ex
        }finally{
            sql?.close()
        }
    }

    List fetchMasterRorData(List exConfigs, Long executedConfigId, String selectedDatasource) {
        List rorData = []
        List<Long> executedConfigIds = exConfigs
        boolean prrSubGrpEnabled = cacheService.getSubGroupsEnabled("prrSubGrpEnabled") ?: false
        Boolean isRor = cacheService.getRorCache()
        String prrSql = "select * FROM ror_full_data_${executedConfigId} where child_execution_id in (" + executedConfigIds.join(",")+ ")"
        if(prrSubGrpEnabled && isRor){
            prrSql = "select * FROM pvs_ror_output_${executedConfigId} where child_execution_id in (" + executedConfigIds.join(",")+ ")"
        }
        Sql sql = new Sql(signalDataSourceService.getReportConnection(selectedDatasource))
        try{
            sql.eachRow(prrSql) { GroovyResultSet resultSet ->
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
                rorData.add(map)
            }
            return rorData
        }catch(Exception ex){
            throw ex
        }finally{
            sql?.close()
        }
    }

    List fetchMasterEbgmData(List exConfigs, Long executedConfigId, String selectedDatasource) {
        List ebgmData = []
        List<Long> executedConfigIds = exConfigs
        String ebgmSql = "select * FROM pvs_ebgm_output_${executedConfigId} where child_execution_id in (" + executedConfigIds.join(",")+ ")"
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
            throw ex
        } finally {
            sql?.close()
        }
        return ebgmData
    }

    List fetchMasterAbcdData(List exConfigs, Long executedConfigId, String selectedDatasource){
        List abcdData = []
        List<Long> executedConfigIds = exConfigs
        Sql sql = null
        try {
            sql = new Sql(signalDataSourceService.getReportConnection(selectedDatasource))

            String abcdSql = "select * FROM pvs_ebgm_final_${executedConfigId} where child_execution_id in (" + executedConfigIds.join(",") + ")"
            sql.eachRow(abcdSql) { GroovyResultSet resultSet ->
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
                abcdData.add(map)
            }
        } catch (Exception ex) {
            throw ex
        } finally {
            sql?.close()
        }
        return abcdData
    }

    List fetchMasterDssData(List exConfigs, Long executedConfigId, String selectedDatasource) {
        List dssData = []
        List<Long> executedConfigIds = exConfigs
        String dssSql = "select * FROM pvs_dss_results_${executedConfigId} where child_execution_id in (" + executedConfigIds.join(",")+ ")"

        Sql sql = null
        try {
            sql = new Sql(signalDataSourceService.getReportConnection(selectedDatasource))

            sql.eachRow(dssSql) { GroovyResultSet resultSet ->
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
                dssData.add(map)
            }
        } catch (Exception ex) {
            throw ex
        } finally {
            sql?.close()
        }
        return dssData
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    def updateMasterConfigurationDataSource(Long lockedConfigurationId, String selectedDatasource) {
        String updateQuery = "update master_configuration set datasource = '" + selectedDatasource + "'"
        updateQuery = updateQuery + " where id = " + lockedConfigurationId
        log.info("update master config datasource")
        log.info(updateQuery)
        SQLQuery sql = null
        Session session = sessionFactory.openSession()
        sql = session.createSQLQuery(updateQuery)
        sql.executeUpdate()
        session.flush()
        session.clear()
        session.close()
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    MasterEvdasConfiguration createEvdasConfiguration(Long lockedConfigurationId) {
        MasterConfiguration lockedConfiguration = MasterConfiguration.findById(lockedConfigurationId)
        MasterEvdasConfiguration masterEvdasConfiguration = new MasterEvdasConfiguration(name: lockedConfiguration.name,
                masterConfigId: lockedConfiguration.id)
        masterEvdasConfiguration.save(flush: true)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    void createEvdasConfigurations(List configurations, List evdasConfigurations, List exEvdasConfigs, Long masterEvdasExecutedId) {
        configurations.each { Configuration childConfig ->

            // ---------------------------------------------------------------------
            // create or get masterEvdasExecutedConfiguration
            EvdasConfiguration evdasConfiguration
            evdasConfiguration = createEvdasConfiguration(childConfig, evdasConfiguration, masterEvdasExecutedId)
            if (evdasConfiguration) {
                evdasConfigurations << evdasConfiguration
                //----------------------------------------------------------------------
                ExecutedEvdasConfiguration executedEvdasConfiguration
                executedEvdasConfiguration = evdasAlertExecutionService.createExecutedConfiguration(evdasConfiguration)
                exEvdasConfigs << executedEvdasConfiguration
            }
        }
    }

    @Transactional
    EvdasConfiguration createEvdasConfiguration(Configuration childConfig, EvdasConfiguration evdasConfiguration, Long masterConfigId = null) {
        EvdasConfiguration prevEvdasConfigObject = EvdasConfiguration.findByIntegratedConfigurationId(childConfig?.id)
        if (prevEvdasConfigObject) {
            prevEvdasConfigObject.masterConfigId = masterConfigId
            prevEvdasConfigObject.dateRangeInformation = evdasAlertExecutionService.createDateRangeForIntegratedReview(childConfig.productGroupList, DateRangeEnum.CUSTOM, prevEvdasConfigObject?.dateRangeInformation?.id)
            prevEvdasConfigObject.save(flush:true)
            evdasConfiguration = prevEvdasConfigObject
        } else {
            evdasConfiguration = new EvdasConfiguration()
            try {
                evdasConfiguration = evdasAlertExecutionService.createConfigForIntegratedReview(childConfig, evdasConfiguration,masterConfigId)
            } catch (Throwable th) {
                log.error("Error occurred while creating EVDAS configuration.", th)
                throw th
            }
        }
        evdasConfiguration
    }

    def getMasterNextDate(String scheduleDateJSON, Date currentRunDate) {
        if(scheduleDateJSON) {
            Map schedulerMap = new JsonSlurper().parseText(scheduleDateJSON)
            Boolean repeatExecution = schedulerMap['recurrencePattern'] != 'FREQ=DAILY;INTERVAL=1;COUNT=1'
            if(!repeatExecution)
                return null
        }
        if (scheduleDateJSON) {
            JSONObject timeObject = JSON.parse(scheduleDateJSON)
            if (timeObject.startDateTime && timeObject.timeZone && timeObject.recurrencePattern) {
                Date now = new Date()
                Date startDate = Date.parse("yyyy-MM-dd'T'HH:mmXXX", timeObject.startDateTime)
                DateTime from = new DateTime(startDate)
                DateTime to = new DateTime(Long.MAX_VALUE)
                Date lastRunDate = currentRunDate

                RRule recurRule = new RRule(timeObject.recurrencePattern)
                //Check if the scheduler will never end
                if (recurRule.recur.count == -1 && !recurRule.recur.until) {
                    //We temporarily set the recurrence count to 2 because we only need the next recur date
                    recurRule.recur.setCount(2)
                    if (lastRunDate) {
                        from = new DateTime(lastRunDate)
                    }
                }

                //Check if the recurrence is run once/now
                if (recurRule?.recur?.count == 1 && startDate.before(now)) {
                    //Do not return a nextRunDate if we have already run this configuration once
                    if (lastRunDate) {
                        return null
                    }

                    //Run once anytime in the past is generated with today's date
                    from = new DateTime(now)
                }

                VEvent event = new VEvent(from, "event")
                event.getProperties().add(recurRule)
                Period period = new Period(from, to)
                PeriodList periodList = event.calculateRecurrenceSet(period)

                //Check if the start date matches recurrence pattern
                boolean excludeStartDate = configurationService.checkStartDate(startDate, recurRule)

                def futureRunDates = []
                if (periodList) {
                    if (excludeStartDate) {
                        periodList.remove(periodList.first())
                    }
                    if (!lastRunDate) {
                        lastRunDate = startDate - 1
                    }
                    futureRunDates = periodList.findAll {
                        new DateTime(it.toString().split("/").first()).after(lastRunDate)
                    }
                }
                if (futureRunDates) {
                    DateTime finalRunDate = new DateTime(futureRunDates?.last()?.toString()?.split("/")?.first())
                    if (finalRunDate && finalRunDate < new DateTime(now)) {
                        return null
                    }
                    DateTime nextRun = new DateTime(futureRunDates?.first()?.toString()?.split("/")?.first())
                    Date nextRunDate = new Date(nextRun.time)
                    return nextRunDate
                }
            }
        }
        return null
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    void updateMasterExecutionStatus(ExecutionStatus executionStatus) {
        JsonSlurper jsonSlurper = new JsonSlurper()
        Map timeStamp = new HashMap<String, Long>()
        if (executionStatus.timeStampJSON)
            timeStamp = jsonSlurper.parseText(executionStatus.timeStampJSON)
        timeStamp.put(executionStatus.executionLevel?.toString(), System.currentTimeMillis())
        String timeStampJSON = timeStamp as JSON
        int level = executionStatus.executionLevel + 1
        long endTime = System.currentTimeMillis()

        ExecutionStatus.withTransaction {
            String updateQuery = "update ex_status set EXECUTION_LEVEL = " + level + " , TIME_STAMPJSON = '" + timeStampJSON + "'"
            if (executionStatus?.executionStatus != ReportExecutionStatus.ERROR) {
                updateQuery = updateQuery + ", ex_status = '" + ReportExecutionStatus.COMPLETED + "', end_time = " + endTime+ ""
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
    }

    def createIntegratedConfigurations(List configurations, List integratedConfigurations, List execIntegratedConfigs,
                                  MasterExecutedConfiguration masterIntegratedExecutedConfiguration, MasterConfiguration masterIntegratedConfiguration, String db) {
        List<Configuration> integratedNewConfigs = []
        configurations.each { childConfig ->
            Configuration integratedConfiguration
            Long prevIntegratedConfigObject = Configuration.findByIntegratedConfigurationIdAndSelectedDatasource(childConfig.id, db)?.id
            if (prevIntegratedConfigObject) {
                integratedConfiguration = Configuration.get(prevIntegratedConfigObject)
            } else {
                integratedConfiguration = new Configuration()
                integratedConfiguration = createConfigForIntegratedReview(childConfig, integratedConfiguration, db)
                integratedConfiguration.masterConfigId = masterIntegratedConfiguration.id
                integratedNewConfigs << integratedConfiguration
            }
            if (integratedConfiguration)
                integratedConfigurations << integratedConfiguration

        }

        alertService.batchPersistForDomain(integratedNewConfigs, Configuration.class.name)

        configurations.each { childConfig ->
            Configuration integratedConfiguration = integratedConfigurations.find {it.integratedConfigurationId == childConfig.id}
            ExecutedConfiguration executedIntegratedConfiguration
            executedIntegratedConfiguration = createExecutedConfigurationForAlert(childConfig, masterIntegratedExecutedConfiguration, integratedConfiguration)
            execIntegratedConfigs << executedIntegratedConfiguration
        }

    }


    Configuration createConfigForIntegratedReview(Configuration configuration, Configuration configurationFaers, String db) {
        Date reviewDate = null

        if(!configuration.isAdhocRun()){
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
            Map dateRange = reportExecutorService.getFaersDateRange()
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
                                          createdBy                     : configuration.getOwner().username, modifiedBy: configuration.modifiedBy, selectedDatasource: db,
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
        configurationFaers.skipAudit //Done to prevent audit of all configuration in master alert PVS-46911
        configurationFaers

    }
    void setEcPrrRorData(List prrData) {
        prrData.each { Map row ->
            Map statsProperties = [:]
            Long childExecutedConfigid = row.CHILD_EXECUTION_ID as Long
            statsProperties.prrValue = row.NORMAL_PRR as Double
            statsProperties.prrUCI = row.NORMAL_UPPER_CI_PRR as Double
            statsProperties.prrLCI = row.NORMAL_LOWER_CI_PRR as Double
            statsProperties.prrStr = row.PRR
            statsProperties.prrStrLCI = row.LCI_PRR
            statsProperties.prrStrUCI = row.UCI_PRR
            statsProperties.prrMh = row.MH_PRR as Double
            statsProperties.rorValue = row.NORMAL_ROR as Double
            statsProperties.rorLCI = row.NORMAL_LOWER_95_CI_ROR as Double
            statsProperties.rorUCI = row.NORMAL_UPPER_95_CI_ROR as Double
            statsProperties.rorStr = row.ROR
            statsProperties.rorStrLCI = row.LCI_ROR
            statsProperties.rorStrUCI = row.UCI_ROR
            statsProperties.rorMh = row.MH_ROR as Double
            statsProperties.chiSquare = row.NORMAL_CHI_SQ as Double
            statsProperties.aValue = row.PRR_A as Double
            statsProperties.bValue = row.PRR_B as Double
            statsProperties.cValue = row.PRR_C as Double
            statsProperties.dValue = row.PRR_D as Double
            String productId = row.PRODUCT_NAME
            String eventId = row.PT_NAME ? String.valueOf(row.PT_NAME).replace('"', '') : ""
            dataObjectService.setProbDataMap(childExecutedConfigid, productId, eventId, statsProperties)
        }
    }
    void setEcRorData(List rorData) {
        boolean prrSubGrpEnabled = cacheService.getSubGroupsEnabled("prrSubGrpEnabled") ?: false
        rorData.each { Map row ->
            Map statsProperties = [:]
            Long childExecutedConfigid = row.CHILD_EXECUTION_ID as Long
            statsProperties.rorValue = row.ROR as Double
            statsProperties.rorUCI = row.ROR_UCI as Double
            statsProperties.rorLCI = row.ROR_LCI as Double
            statsProperties.chiSquare = row.CHI_SQUARE as Double
            String productId = row.BASE_ID ? String.valueOf(row.BASE_ID) : ""
            String eventId = row.MEDDRA_PT_CODE ?String.valueOf(String.valueOf(row.MEDDRA_PT_CODE)): ""
            dataObjectService.setRorProbDataMap(childExecutedConfigid, productId, eventId, statsProperties)
            if(prrSubGrpEnabled) {
                Map prrRorStatsSubGroupDataMap = [:]
                boolean rorRelSubGrpEnabled = cacheService.getSubGroupsEnabled("rorRelSubGrpEnabled") ?: false
                prrRorStatsSubGroupDataMap.rorSubGroup = statisticsService.prepareSubGroupDataMap(row, "ROR")
                prrRorStatsSubGroupDataMap.rorLciSubGroup = statisticsService.prepareSubGroupDataMap(row, "ROR_LCI")
                prrRorStatsSubGroupDataMap.rorUciSubGroup = statisticsService.prepareSubGroupDataMap(row, "ROR_UCI")
                prrRorStatsSubGroupDataMap.chiSquareSubGroup = statisticsService.prepareSubGroupDataMap(row, "Chi-Square")
                if(rorRelSubGrpEnabled) {
                    prrRorStatsSubGroupDataMap.rorRelSubGroup = statisticsService.prepareSubGroupDataMap(row, "ROR_REL")
                    prrRorStatsSubGroupDataMap.rorLciRelSubGroup = statisticsService.prepareSubGroupDataMap(row, "ROR_LCI_REL")
                    prrRorStatsSubGroupDataMap.rorUciRelSubGroup = statisticsService.prepareSubGroupDataMap(row, "ROR_UCI_REL")
                }
                dataObjectService.setRorSubGroupDataMap(childExecutedConfigid, productId, eventId, prrRorStatsSubGroupDataMap)
            }
        }
    }
    void setEcPrrData(List prrData) {
        boolean prrSubGrpEnabled = cacheService.getSubGroupsEnabled("prrSubGrpEnabled") ?: false
        prrData.each { Map row ->
            Map statsProperties = [:]
            Long childExecutedConfigid = row.CHILD_EXECUTION_ID as Long
            statsProperties.prrValue = row.PRR as Double
            statsProperties.prrUCI = row.PRR_UCI as Double
            statsProperties.prrLCI = row.PRR_LCI as Double
            statsProperties.aValue = row.PRR_A as Double
            statsProperties.bValue = row.PRR_B as Double
            statsProperties.cValue = row.PRR_C as Double
            statsProperties.dValue = row.PRR_D as Double
            String productId = row.BASE_ID ? String.valueOf(row.BASE_ID) : ""
            String eventId = row.MEDDRA_PT_CODE ?String.valueOf(String.valueOf(row.MEDDRA_PT_CODE)): ""
            dataObjectService.setProbDataMap(childExecutedConfigid, productId, eventId, statsProperties)
            if(prrSubGrpEnabled) {
                Map prrRorStatsSubGroupDataMap = [:]
                prrRorStatsSubGroupDataMap.prrSubGroup = statisticsService.prepareSubGroupDataMap(row, "PRR")
                prrRorStatsSubGroupDataMap.prrLciSubGroup = statisticsService.prepareSubGroupDataMap(row, "PRR_LCI")
                prrRorStatsSubGroupDataMap.prrUciSubGroup = statisticsService.prepareSubGroupDataMap(row, "PRR_UCI")
                dataObjectService.setRorSubGroupDataMap(childExecutedConfigid, productId, eventId, prrRorStatsSubGroupDataMap)
            }
        }
    }

    void setEcAbcdData(List prrData) {
        prrData.each { Map row ->
            Long childExecutedConfigid = row.CHILD_EXECUTION_ID as Long
            String productId = row.BASE_ID ? String.valueOf(row.BASE_ID) : ""
            String eventId = row.MEDDRA_PT_CODE ?String.valueOf(String.valueOf(row.MEDDRA_PT_CODE)): ""
            Map statsProperties = dataObjectService.getProbDataMap(childExecutedConfigid, productId, eventId)
            if(statsProperties!=null){
                statsProperties.aValue = row.EBGM_A as Double
                statsProperties.bValue = row.EBGM_B as Double
                statsProperties.cValue = row.EBGM_C as Double
                statsProperties.dValue = row.EBGM_D as Double
                statsProperties.eValue = row.E as Double
                statsProperties.rrValue = row.RR as Double
                dataObjectService.setProbDataMap(childExecutedConfigid, productId, eventId, statsProperties)
            }else{
                statsProperties = [:]
                statsProperties.aValue = row.EBGM_A as Double
                statsProperties.bValue = row.EBGM_B as Double
                statsProperties.cValue = row.EBGM_C as Double
                statsProperties.dValue = row.EBGM_D as Double
                statsProperties.eValue = row.E as Double
                statsProperties.rrValue = row.RR as Double
                dataObjectService.setProbDataMap(childExecutedConfigid, productId, eventId, statsProperties)
            }
            dataObjectService.setProbDataMap(childExecutedConfigid, productId, eventId, statsProperties)
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    void updateMasterConfig(MasterConfiguration config, boolean isCreate = false, boolean isResume = false) {
        String executing = isCreate ? "true" : "false"
        String resume = isResume ? "true" : "false"
        Transaction tx = null
        Session session = null
        try {
            session = sessionFactory.openSession()
            tx = session.beginTransaction()
            String updateQuery = "update master_configuration set executing = " + executing + ", is_resume = " + resume + ""
            if (isCreate || isResume) {
                Date nextRunDate = getMasterNextDate(config.scheduler, config.nextRunDate)
                if (nextRunDate) {
                    updateQuery = updateQuery + ", next_run_date = " + "TO_TIMESTAMP('${nextRunDate?.format(SqlGenerationService.DATETIME_FMT)}', '${SqlGenerationService.DATETIME_FMT_ORA}')" + ""
                } else {
                    updateQuery = updateQuery + ", next_run_date = " + null + ""
                }
                Integer numOfExec = isResume ? config.numOfExecutions : config.numOfExecutions + 1
                updateQuery = updateQuery + ", num_of_executions = " + numOfExec + ""
            }
            updateQuery = updateQuery + " where id = " + config.id
            log.info(updateQuery)
            SQLQuery sql = null
            sql = session.createSQLQuery(updateQuery)
            sql.executeUpdate()
            tx.commit()
            config.refresh()
        } catch (Throwable th) {
            if (tx != null && tx.getStatus() == TransactionStatus.ACTIVE) {
                tx.rollback()
            }
            log.error("Error occurred while updating master config status.", th)
            th.printStackTrace()
        } finally {
            session.close()
        }
    }

    void deleteSuccessFiles(List allFiles) {
        for (String fileName : allFiles) {
            try {
                File file = new File(fileName)
                if (file.exists()) {
                    file.delete()
                }
            } catch(Exception ex) {
                log.error("Error deleting file: ", ex)
            }
        }
    }

    def clearDataMiningTables(Long executedConfigId, String dataSource = "pva") {
        Sql sql = null
        try {
            def beforeTime = System.currentTimeMillis()
            if(executedConfigId) {
                Configuration.withTransaction {
                    sql = new Sql(signalDataSourceService.getReportConnection(dataSource))
                    def cleanUpProc = """
                       begin
                            p_drop_objects_alert(${executedConfigId});
                       end;
                        """
                    sql?.call(cleanUpProc)
                    def afterTime = System.currentTimeMillis()
                    log.info("It took " + (afterTime - beforeTime) / 1000 + " seconds to delete the meta tables for execution")
                }
            }
        } catch (Throwable throwable) {
            log.error("Error happened when clear data mining table. Not able to automactically recover from this",
                    throwable)
        } finally {
            sql?.close()
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception)
    def updateConfigurationList(List<Configuration> configs, MasterConfiguration masterConfiguration){
        try {
            Date nextRunDate = masterConfiguration.nextRunDate
            String updateQuery = "update rconfig set executing = false, "
            if(nextRunDate){
                updateQuery = updateQuery + " next_run_date = " + "TO_TIMESTAMP('${nextRunDate?.format(SqlGenerationService.DATETIME_FMT)}', '${SqlGenerationService.DATETIME_FMT_ORA}')" + ""
            } else {
                updateQuery = updateQuery + " next_run_date = " + null + ""
            }
            updateQuery = updateQuery + " where id in (" + configs*.id.join(",") + ")"
            log.info(updateQuery)
            SQLQuery sql = null
            Session session = sessionFactory.currentSession
            sql = session.createSQLQuery(updateQuery)
            sql.executeUpdate()
            session.flush()
            session.clear()
        } catch(Exception ex) {
            log.error(alertService.exceptionString(ex))
        }
    }

    def deleteDssMasterFile(Long masterId) {
        try {
            String path = (Holders.config.statistics.inputfile.path as String)
            String dssFileName = path +"DSS_REQUEST_${masterId}.txt"
            String fileNameMetaJson = path + "${masterId}/" + "meta.json"
            List inputDssFiles = [dssFileName, fileNameMetaJson]
            log.info("Now cleaning dss input file.")
            log.info(inputDssFiles)
            inputDssFiles.each { String inputDssFile->
                try {
                    File alertFile = new File(inputDssFile)
                    if (alertFile.exists()) {
                        alertFile.delete()
                        log.info("Deleted file "+inputDssFile)
                    }
                } catch(Exception e){
                    log.error("Some error occurred while deleting file "+ inputDssFile)
                }
            }
        } catch(Exception ex) {
            log.error("Error deleting dss master file: ", ex)
        }
    }

    void deleteDssSuccessFile(Long exConfigId, Long masterId) {
        try {
            String fileNameOut = (Holders.config.statistics.inputfile.path as String) + "${masterId}/" + exConfigId
            String outputFileName = "${fileNameOut}_DSS_SCORE.csv"
            File file = new File(outputFileName)
            if (file.exists()) {
                file.delete()
            }
        } catch(Exception ex) {
            log.error("Error deleting file: ", ex)
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception)
    def updateConfigurationStatus(List<Configuration> configs){
        String updateQuery = "update rconfig set executing= true"
        updateQuery = updateQuery + " where id in (" + configs*.id.join(",") + ")"
        log.info(updateQuery)
        SQLQuery sql = null
        Session session = sessionFactory.currentSession
        sql = session.createSQLQuery(updateQuery)
        sql.executeUpdate()
        session.flush()
        session.clear()
    }

    List<MasterConfigStatus> createMasterConfigStatus(Long masterExecutionId, String datasource, Map flagMap, List exDbConfigs, Long faersMasterExId=null,
                                                      List faersExConfigs=[], Long evdasMasterId=null, List evdasConfigs=[], List evdasExConfigs=[], Long vaersMasterExId = null,
                                                        List vaersExConfigs=[], Long vigibaseMasterExId=null, List vigibaseExConfigs=[]) {
        List<MasterConfigStatus> configs = []
        Set<Member> nodes = hazelcastService.hazelcastInstance.cluster.members
        Integer availableNodes = nodes.size()
        Integer batchSize = (exDbConfigs.size()/availableNodes) + 1
        Integer i = 0
        exDbConfigs.collate(batchSize).each { exConfigs->
            List configNames = exConfigs*.name
            List batchFaers = faersExConfigs ? faersExConfigs.findAll {it.masterExConfigId == faersMasterExId && configNames.contains(it.name)} : []
            List batchEvdas = evdasConfigs ? evdasConfigs.findAll {it.masterConfigId == evdasMasterId && configNames.contains(it.name)} : []
            List batchExEvdas = evdasExConfigs ? evdasExConfigs.findAll {it.masterExConfigId == evdasMasterId && configNames.contains(it.name)} : []
            List batchVaers = vaersExConfigs ? vaersExConfigs.findAll {it.masterExConfigId == vaersMasterExId && configNames.contains(it.name)} : []
            List batchVigibase = vigibaseExConfigs ? vigibaseExConfigs.findAll {it.masterExConfigId == vigibaseMasterExId && configNames.contains(it.name)} : []
            Member node = nodes[i++]
            MasterConfigStatus obj = new MasterConfigStatus()
            obj.masterExecId = masterExecutionId
            obj.nodeUuid = node.uuid
            obj.nodeName = hazelcastService.getName()
            obj.dataSource = datasource
            obj.dssFlag = false
            obj.ebgmFlag = false
            obj.prrFlag = false
            obj.faersMasterExId = faersMasterExId
            obj.vaersMasterExId = vaersMasterExId
            obj.vigibaseMasterExId = vigibaseMasterExId
            obj.evdasMasterExId = evdasMasterId
            obj.faersExIds = batchFaers?batchFaers*.id.join(","):""
            obj.evdasExIds = batchEvdas?batchEvdas*.id.join(","):""
            obj.vaersExIds = batchVaers?batchVaers*.id.join(","):""
            obj.vigibaseExIds = batchVigibase?batchVigibase*.id.join(","):""
            obj.allChildDone = false
            obj.save()
            insertChildNodes(exConfigs, node.uuid, masterExecutionId, batchFaers, batchEvdas, batchExEvdas, batchVaers, batchVigibase)
            configs<< obj
        }
        configs
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception)
    def updateMasterConfigStatus(String field, List masterExecs, String uuid="", String errorMsg=""){
        Session session = sessionFactory.openSession()
        Transaction tx = session.beginTransaction()
        try {
            String updateQuery = "update master_config_status set " + field + "= true"
            if (errorMsg) {
                errorMsg = errorMsg.length() > 255 ? errorMsg.substring(0, 254) : errorMsg
                updateQuery = updateQuery + ", error_msg='" + errorMsg.replaceAll("'", "''") + "'"
            }
            updateQuery = updateQuery + " where master_exec_id in (" + masterExecs?.join(",") + ")"
            if (uuid) {
                updateQuery = updateQuery + " and node_uuid= '" + uuid + "'"
            }
            log.info(updateQuery)
            SQLQuery sql = null
            sql = session.createSQLQuery(updateQuery)
            sql.executeUpdate()
        } catch(Exception ex) {
            log.error(ex.message)
        } finally {
            session.flush()
            session.clear()
            tx.commit()
            session.close()
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception)
    def insertChildNodes(List childExecs, String nodeUuid, Long masterExecId, List faersConfigs, List evdasConfigs, List evdasExConfigs, List vaersConfigs, List vigibaseConfigs){
        childExecs.each {child->
            MasterChildRunNode obj = new MasterChildRunNode()
            obj.nodeName = hazelcastService.getName()
            obj.nodeUuid = nodeUuid
            obj.masterExecId = masterExecId
            obj.childExecId = child.id
            obj.faersId = faersConfigs.find {it.name ==  child.name}?.id
            obj.evdasId = evdasConfigs.find {it.name ==  child.name}?.id
            obj.exEvdasId = evdasExConfigs.find {it.name ==  child.name}?.id
            obj.vaersId = vaersConfigs.find {it.name ==  child.name}?.id
            obj.vigibaseId = vigibaseConfigs.find {it.name ==  child.name}?.id
            obj.save()
        }
    }


    Boolean allChildDone(Long masterExecId) {
        String query = "select case when exists (select 1 from master_config_status where master_exec_id = ${masterExecId} and all_child_done = false)" +
                " then 0 else 1 end as CURRENT_STATUS"
        Sql sql = null
        int status = 0
        try {
            sql = new Sql(dataSource)
            sql.eachRow(query) { ResultSet resultSetObj ->
                status = resultSetObj.getInt("CURRENT_STATUS")
            }
        } catch (Exception ex) {
            log.error(ex.message)
        } finally {
            sql?.close()
        }
        return status == 1
    }


    Boolean allDone(Long masterExecId, String nodeUuid) {
        String query = "select is_count_done AND is_prr_done AND is_ebgm_done  AND is_dss_done as CURRENT_STATUS " +
                "from  master_config_status " +
                "where master_exec_id  = " + masterExecId + " and node_uuid='" +nodeUuid+ "'"


        Sql sql = null
        boolean status = false
        try {
            sql = new Sql(dataSource)
            sql.eachRow(query) { ResultSet resultSetObj ->
                status = resultSetObj.getBoolean("CURRENT_STATUS")
            }
        } catch (Exception ex) {
            log.error(ex.message)
        } finally {
            sql?.close()
        }
        log.info("status:"+status)
        return status //|| status == 1110
    }

    Map prevMasterExConfigs(Long masterConfigurationId,Long masterExecutedConfigurationId) {
        List<ExecutedConfiguration> prevExConfigs = []
        Long prevExConfigId
        MasterExecutedConfiguration masterExecutedConfiguration = MasterExecutedConfiguration.get(masterExecutedConfigurationId)
        if (masterExecutedConfiguration) {
            List<MasterExecutedConfiguration> masterExecutedConfigurationList = MasterExecutedConfiguration.findAllByMasterConfigId(masterConfigurationId)
            List<Long> prevMasterConfigs = masterExecutedConfigurationList*.id - masterExecutedConfiguration.id
            if(prevMasterConfigs) {
                prevExConfigId = prevMasterConfigs.max()
                prevExConfigs = ExecutedConfiguration.findAllByMasterExConfigId(prevExConfigId)
            }
        }

        [prevExConfigs: prevExConfigs]
    }

    void updateChildDoneStatus(String nodeUuid, Long masterExecId) {

        updateMasterConfigStatus("all_child_done", [masterExecId], nodeUuid)
        MasterExecutedConfiguration mec = MasterExecutedConfiguration.get(masterExecId)
        MasterConfiguration masterConfiguration = MasterConfiguration.findById(mec.masterConfigId)
        updateMasterConfig(masterConfiguration, false)

    }

    def manageMasterExecutionQueue(boolean getList = false, boolean getSize = false, boolean clearList = true) {
        if (getList){
            def listData = (grailsApplication.config.hazelcast.enabled && grailsApplication.config.hazelcast.network.nodes.size() >1) ? hazelcastInstance.getList(HAZELCAST_CURRENTLY_RUNNING_MASTER) : currentlyMasterRunning
            return listData
        } else if(getSize) {
            def listSize = (grailsApplication.config.hazelcast.enabled && grailsApplication.config.hazelcast.network.nodes.size() >1) ? hazelcastInstance.getList(HAZELCAST_CURRENTLY_RUNNING_MASTER)?.size() : currentlyMasterRunning?.size()
            return listSize
        } else {
            (grailsApplication.config.hazelcast.enabled && grailsApplication.config.hazelcast.network.nodes.size() >1) ? hazelcastInstance.getList(HAZELCAST_CURRENTLY_RUNNING_MASTER)?.clear() : currentlyMasterRunning?.clear()
        }
    }


}
