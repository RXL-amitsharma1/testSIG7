package com.rxlogix

import com.fasterxml.jackson.databind.ObjectMapper
import com.rxlogix.audit.AuditTrail
import com.rxlogix.config.*
import com.rxlogix.dto.AlertDataDTO
import com.rxlogix.dto.AlertLevelDispositionDTO
import com.rxlogix.dto.DashboardCountDTO
import com.rxlogix.dto.LastReviewDurationDTO
import com.rxlogix.dto.SpotfireSettingsDTO
import com.rxlogix.dto.SuperQueryDTO
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.ReportFormat
import com.rxlogix.helper.LinkHelper
import com.rxlogix.json.JsonOutput
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.pvdictionary.config.ViewConfig
import com.rxlogix.signal.*
import com.rxlogix.user.User
import com.rxlogix.user.Group
import com.rxlogix.util.AlertAsyncUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.DbUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.SignalQueryHelper
import com.rxlogix.util.SignalUtil
import com.rxlogix.util.ViewHelper
import grails.async.Promise
import grails.converters.JSON
import grails.events.EventPublisher
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.sql.GroovyResultSet
import groovy.sql.Sql
import oracle.jdbc.driver.OracleTypes
import org.apache.commons.lang.StringUtils
import org.apache.http.util.TextUtils
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.jdbc.Work
import org.hibernate.sql.JoinType
import org.springframework.transaction.annotation.Propagation

import java.sql.Clob
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.sql.Timestamp
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.regex.Matcher

import static com.rxlogix.Constants.AlertConfigType.AGGREGATE_CASE_ALERT
import static com.rxlogix.Constants.AlertConfigType.AGGREGATE_CASE_ALERT_JADER
import static com.rxlogix.Constants.AlertConfigType.AGGREGATE_CASE_ALERT_VAERS
import static com.rxlogix.Constants.AlertConfigType.AGGREGATE_CASE_ALERT_VIGIBASE
import static com.rxlogix.util.MiscUtil.calcDueDate
import static grails.async.Promises.task

class AggregateCaseAlertService implements Alertbililty, LinkHelper, AlertAsyncUtil, EventPublisher {

    def CRUDService
    def statisticsService
    def productEventHistoryService
    def emailService
    def messageSource
    def userService
    def activityService
    def validatedSignalService
    def actionTemplateService
    def medicalConceptsService
    def businessConfigurationService
    def grailsApplication
    def signalDataSourceService
    def productBasedSecurityService
    def sessionFactory
    def dataObjectService
    def cacheService
    def dataSource
    def evdasAlertService
    def productDictionaryCacheService
    def alertService
    def alertTagService
    def actionService
    def alertCommentService
    EmailNotificationService emailNotificationService
    def signalExecutorService
    def dataSource_pva
    def dataSource_faers
    def singleCaseAlertService
    def reportIntegrationService
    def spotfireService
    def pvsGlobalTagService
    def pvsAlertTagService
    def archiveService
    ViewInstanceService viewInstanceService
    def evdasAlertExecutionService
    def reportExecutorService
    CustomMessageService customMessageService
    def emergingIssueService
    def queryService
    def dataSource_vaers
    def dataSource_vigibase
    def dataSource_jader
    def dataSheetService
    def undoableDispositionService
    def appAlertProgressStatusService
    def signalAuditLogService
    def alertFieldService
    def jaderExecutorService

    List<Long> getExecConfigIdForTrendFlag(ExecutedConfiguration execConfig) {
        boolean isProductGroup = execConfig.productGroupSelection ? true : false
        List<Long> execIds = []
        List<ExecutedConfiguration> allExecConfigs = ExecutedConfiguration.findAllByConfigId(execConfig.configId)
        if (isProductGroup) {
            execIds = allExecConfigs.findAll { ExecutedConfiguration config -> config.productGroupSelection && config.id != execConfig.id && config.isEnabled}*.id
        } else {
            String productSelection = execConfig.productSelection
            Map<String, Map> productMap = JSON.parse(productSelection)
            String hierarchy = ""
            for (Map.Entry<String, Map> ele : productMap.entrySet()) {
                if (ele.getValue()) {
                    hierarchy = ele.getKey()
                    break
                }
            }
            execIds = allExecConfigs.findAll { ExecutedConfiguration config ->
                if (config.productSelection) {
                    Map<String, Map> tmpProductMap = JSON.parse(config.productSelection)
                    return tmpProductMap."$hierarchy" && config.id != execConfig.id && config.isEnabled
                } else {
                    return false
                }
            }*.id
        }
        execIds
    }

    @Transactional
    void createAlert(Long scheduledConfigId, Long executedConfigId, Map<String, List<Map>> alertData,Map<String,Long> otherDataSourcesExecIds, Long integratedFaersMasterId = null, List<String> allFiles = [], Long integratedVigibaseMasterId = null, ExecutionStatus executionStatus = null) throws Exception {


        log.info("Data mining in PV Datahub completed for " + alertData.size() + " data sources.")
        if (alertData) {
            ExecutedConfiguration executedConfig = null
            try {
                String disabledDssNodes
                Configuration config = Configuration.get(scheduledConfigId)
                executedConfig = ExecutedConfiguration.get(executedConfigId)
                List<Date> dateRangeStartEndDate = config.alertDateRangeInformation.getReportStartAndEndDate()
                Disposition defaultDisposition = config?.getWorkflowGroup()?.defaultQuantDisposition
                if(executionStatus) {
                    appAlertProgressStatusService.createAppAlertProgressStatus(executionStatus, executedConfig
                            .id, "BR", 2, 2, System.currentTimeMillis(), AlertProgressExecutionType.BR)
                }
                List<Map> pec = fetchPECfromdata(alertData)
                List<Map> newProductEvents = fetchNewProductEvent(pec)
                pvsGlobalTagService.batchPersistGlobalProductEvents(newProductEvents)
                List<GlobalProductEvent> globalProductEventList = fetchGlobalProductEvent(pec)
                dataObjectService.setGlobalProductEventList(executedConfigId, globalProductEventList)
                config.selectedDatasource.split(',').each {
                    dataObjectService.setEnabledBusinessConfigList(it, BusinessConfiguration.findAllByDataSourceAndIsGlobalRuleAndEnabled(it, true, true))
                }

                boolean isProductGroup = config.productGroupSelection != null
                boolean isEventGroup = config.eventGroupSelection && config.groupBySmq

                if (isProductGroup && config.productGroupList && config.productGroupList != Constants.EMPTY_STRING) {
                    List<BusinessConfiguration> prodGrpBusinessConfigList = BusinessConfiguration.createCriteria().list {
                        eq('enabled', true)
                        sqlRestriction("""Id in (SELECT id 
                            FROM business_configuration, 
                            LATERAL (
                                SELECT jsonb_array_elements(
                                CASE 
                                    WHEN PRODUCT_GROUP_SELECTION = '' THEN '[]'::jsonb
                                    ELSE PRODUCT_GROUP_SELECTION::jsonb 
                                END
                                ) AS element
                            ) t2
                            WHERE (t2.element->>'id')::numeric IN (${config.productGroupList}) -- Replace with actual list or handle case with an empty list
                            AND PRODUCT_GROUP_SELECTION IS NOT NULL
                            )""")
                    } as List<BusinessConfiguration>

                    dataObjectService.setEnabledBusinessConfigProductGrpList(executedConfigId, prodGrpBusinessConfigList)
                }

                List eiList = alertService.getEmergingIssueList()
                Long configId = config.id
                log.info("Start prepare previous alert data")

                //Fetch the previous aggregate alerts.

                def prevAlertData = null
                Map prevAlertDataFaers = [:]
                Map prevAlertDataVaers = [:]
                Map prevAlertDataVigibase = [:]
                List<ExecutedConfiguration> prevExecutions = []
                synchronized (AggregateCaseAlertService.class) {
                    prevExecutions = fetchPreviousExecConfigs(executedConfig,configId)
                }
                List<Long> prevExecIds = prevExecutions*.id
                List<Map> prevAggAlertList = fetchPreviousAlerts(prevExecIds, AggregateCaseAlert)
                List<Map> allPrevAlerts = prevAggAlertList + fetchPreviousAlerts(prevExecIds, ArchivedAggregateCaseAlert)
                List<Long> reviewedDispositions = cacheService.getDispositionByReviewCompleted()*.id
                prevAlertData = preparePreviousData(alertData.get(Constants.DataSource.PVA) as List<Map>, prevExecutions, allPrevAlerts,executedConfigId,reviewedDispositions, Constants.DataSource.PVA)
                prevAlertDataFaers = preparePreviousData(alertData.get(Constants.DataSource.FAERS) as List<Map>, prevExecutions, allPrevAlerts,executedConfigId,reviewedDispositions, Constants.DataSource.FAERS)
                prevAlertDataVaers = preparePreviousData(alertData.get(Constants.DataSource.VAERS) as List<Map>, prevExecutions, allPrevAlerts,executedConfigId,reviewedDispositions, Constants.DataSource.VAERS)
                prevAlertDataVigibase = preparePreviousData(alertData.get(Constants.DataSource.VIGIBASE) as List<Map>, prevExecutions, allPrevAlerts,executedConfigId,reviewedDispositions, Constants.DataSource.VIGIBASE)

                List<Long> prevExecIdsForTrendFlag = getExecConfigIdForTrendFlag(executedConfig)
                Boolean enabledTrendFlag = Holders.config.signal.agg.calculate.trend.flag as Boolean
                List<Map> allPrevAlertsForTrendFlag = []
                if(enabledTrendFlag) {
                    allPrevAlertsForTrendFlag = fetchPreviousAlertsForTrendFlag(prevExecIdsForTrendFlag, AggregateCaseAlert, reviewedDispositions, false) + fetchPreviousAlertsForTrendFlag(prevExecIdsForTrendFlag, ArchivedAggregateCaseAlert, reviewedDispositions, false)
                }

                log.info("Previous alert data prepared ready")

                log.info("Fetching existing Product Event History List")

                List<ProductEventHistory> existingPEHistoryList = ProductEventHistory.createCriteria().list {
                    eq("configId", config.id)
                    order("lastUpdated", "desc")
                    order("id", "desc")
                } as List<ProductEventHistory>

                dataObjectService.setExistingPEHistoryList(config.id, existingPEHistoryList)
                log.info("Existing Product Event History List fetched")

                log.info("Fetching Previous Executed Config Ids")
                List<Long> prevExConfigIds = prevExecIds.sort {-it}
                log.info("Fetched All Previous Executed Config Ids")

                if (prevExConfigIds) {
                    log.info("Fetching LastReviewDurationDTOs")
                    Session session = sessionFactory.currentSession
                    String sql = SignalQueryHelper.quant_last_review_sql(prevExConfigIds)
                    List<LastReviewDurationDTO> lastReviewDurationDTOList = alertService.getTransformedResultList(LastReviewDurationDTO.class.name, sql, session)
                    log.info("Fetched LastReviewDurationDTOs")

                    dataObjectService.setLastReviewDurationMap(executedConfigId, lastReviewDurationDTOList)
                    dataObjectService.setCurrentEndDateMap(executedConfigId, executedConfig.executedAlertDateRangeInformation.dateRangeEndAbsolute)
                }

                Integer workerCnt = Holders.config.signal.worker.count as Integer
                List<AggregateCaseAlert> resultData = []
                ExecutorService executorService = Executors.newFixedThreadPool(workerCnt)
                String fileNameStr = (Holders.config.statistics.inputfile.path as String) + executedConfig.id
                List<Map> faersList = alertData.faers
                List<Map> evdasList = alertData.eudra
                List<Map> pvaList = alertData.pva
                List<Map> vaersList = alertData.vaers
                List<Map> vigibaseList = alertData.vigibase
                List<Map> jaderList = alertData.jader

                log.info("PE combinations- " + "PVA: " + pvaList?.size()?:0)
                log.info("PE combinations- " + "FAERS: " + faersList?.size()?:0)
                log.info("PE combinations- " + "EVDAS: " + evdasList?.size()?:0)
                log.info("PE combinations- " + "VAERS: " + vaersList?.size()?:0)
                log.info("PE combinations- " + "VIGIBASE: " + vigibaseList?.size()?:0)
                log.info("PE combinations- " + "JADER: " + jaderList?.size()?:0)

                def status = [:]

                if (pvaList) {
                    log.info("Now calling the statistics block for pva dataSource.")
                    String productId = pvaList[0]["PRODUCT_ID"]
                    if (executedConfig.masterExConfigId) {
                        reportExecutorService.populateCriteriaSheetCount(executedConfig, Constants.DataSource.PVA)
                        log.info("createAlert ec.criteriaCounts : "+executedConfig.criteriaCounts)
                        status = statisticsService.mergeStatsScoresForMaster(executedConfig.masterExConfigId, executedConfig.id, Constants.DataSource.PVA, allFiles)
                    }
                    else {
                        status.status = 1
                    }
                }

                if (faersList) {
                    Long masterId = pvaList? integratedFaersMasterId : executedConfig.masterExConfigId
                    String productId = faersList[0]["PRODUCT_ID"]
                    Long faersExecConfigId = otherDataSourcesExecIds.get(Constants.DataSource.FAERS)
                    ExecutedConfiguration executedConfigurationFaers = ExecutedConfiguration.get(faersExecConfigId)
                    String fileNameStrFaers = (Holders.config.statistics.inputfile.path as String) + faersExecConfigId
                    log.info("Now calling the statistics block for other dataSource")
                    if (executedConfig?.masterExConfigId) {
                        reportExecutorService.populateCriteriaSheetCount(executedConfigurationFaers,Constants.DataSource.FAERS)
                        status = statisticsService.mergeStatsScoresForMaster(executedConfig.masterExConfigId, faersExecConfigId, Constants.DataSource.FAERS, allFiles)
                    } else {
                        status = statisticsService.mergeStatsScores(fileNameStrFaers, Constants.DataSource.FAERS, faersExecConfigId, alertData.faers, isProductGroup, isEventGroup, false, masterId, productId)
                    }
                }

                if (vaersList) {
                    Long vaersExecConfigId = otherDataSourcesExecIds.get(Constants.DataSource.VAERS)
                    String fileNameStrVaers = (Holders.config.statistics.inputfile.path as String) + vaersExecConfigId
                    ExecutedConfiguration executedConfigurationVaers = ExecutedConfiguration.get(vaersExecConfigId)
                    log.info("Now calling the statistics block for other dataSource")
                    if(executedConfig.masterExConfigId){
                        reportExecutorService.populateCriteriaSheetCount(executedConfigurationVaers, Constants.DataSource.VAERS)
                        status = statisticsService.mergeStatsScoresForMaster(executedConfig.masterExConfigId, vaersExecConfigId, Constants.DataSource.VAERS, allFiles)}
                    else{
                        status = statisticsService.mergeStatsScores(fileNameStrVaers, Constants.DataSource.VAERS, vaersExecConfigId, alertData.vaers, isProductGroup,isEventGroup)
                    }
                }

                if (vigibaseList) {
                    Long masterId = pvaList? integratedVigibaseMasterId : executedConfig.masterExConfigId
                    String productId = vigibaseList[0]["PRODUCT_ID"]
                    Long vigibaseExecConfigId = otherDataSourcesExecIds.get(Constants.DataSource.VIGIBASE)
                    ExecutedConfiguration executedConfigurationVigibase = ExecutedConfiguration.get(vigibaseExecConfigId);
                    String fileNameStrVigibase = (Holders.config.statistics.inputfile.path as String) + vigibaseExecConfigId
                    log.info("Now calling the statistics block for other dataSource")
                    if(executedConfig.masterExConfigId) {
                        reportExecutorService.populateCriteriaSheetCount(executedConfigurationVigibase, Constants.DataSource.VIGIBASE)
                        status = statisticsService.mergeStatsScoresForMaster(executedConfig.masterExConfigId, vigibaseExecConfigId, Constants.DataSource.VIGIBASE, allFiles)
                    }else{
                        status = statisticsService.mergeStatsScores(fileNameStrVigibase, Constants.DataSource.VIGIBASE, vigibaseExecConfigId, alertData.vigibase, isProductGroup,isEventGroup, false, masterId, productId)
                    }
                }
                if (jaderList) {
                    Long masterId = executedConfig.masterExConfigId
                    String productId = jaderList[0]["PRODUCT_ID"]
                    Long jaderExecConfigId = otherDataSourcesExecIds.get(Constants.DataSource.JADER)
                    String fileNameStrJader = (Holders.config.statistics.inputfile.path as String) + jaderExecConfigId
                    log.info("Now calling the statistics block for other dataSource")
                    if(executedConfig.masterExConfigId)
                        status = statisticsService.mergeStatsScoresForMaster(executedConfig.masterExConfigId, jaderExecConfigId, Constants.DataSource.VIGIBASE, allFiles)
                    else
                        status = statisticsService.mergeStatsScores(fileNameStrJader, Constants.DataSource.JADER, jaderExecConfigId, alertData.vigibase, isProductGroup,isEventGroup, false, masterId, productId)
                }

                if(!status.status && Holders.config.statistics.enable.ebgm && (pvaList || faersList || vaersList || vigibaseList || jaderList)){
                    if(executionStatus){
                        appAlertProgressStatusService.updateAppAlertProgressStatus(executionStatus.id, executedConfig.id, "BR", 2, 1, System.currentTimeMillis())
                    }
                    throw new Exception("Alert got failed while statistics scores merging. " + status.error)
                }

                log.info("Thread Starts")
                List listednessData = []
                if(executedConfig?.masterExConfigId && executedConfig?.selectedDataSheet && !TextUtils.isEmpty(executedConfig?.selectedDataSheet)){
                    listednessData = evaluateListedness(executedConfig.masterExConfigId,executedConfig.id,true)
                }else if (executedConfig?.masterExConfigId == null && executedConfig?.selectedDataSheet && !TextUtils.isEmpty(executedConfig?.selectedDataSheet)) {
                    listednessData = evaluateListedness(executedConfig.id,null, false)
                }
                List<Future<AggregateCaseAlert>> futureList = pvaList.collect { Map data ->
                    Map faersData = faersList.find {
                        it["PT"] == data["PT"]
                    }
                    if(faersData){
                        faersList.remove(faersData)
                    }
                    Map vaersData = vaersList.find {
                        it["PT"] == data["PT"]
                    }
                    if(vaersData){
                        vaersList.remove(vaersData)
                    }
                    Map vigibaseData = vigibaseList.find {
                        it["PT"] == data["PT"]
                    }
                    if(vigibaseData){
                        vigibaseList.remove(vigibaseData)
                    }
                    Map evdasData = evdasList.find {
                        it["pt"] == data["PT"]
                    }
                    if(evdasList){
                        evdasList.remove(evdasData)
                    }
                    executorService.submit({ ->
                        createAggregateAlert(data, config, executedConfig, dateRangeStartEndDate, prevAlertData,prevAlertDataFaers, defaultDisposition, eiList, faersData,
                                evdasData, vaersData, vigibaseData, otherDataSourcesExecIds, allPrevAlertsForTrendFlag, prevExecIdsForTrendFlag, reviewedDispositions, true, listednessData)
                    } as Callable)
                }
                futureList.each {
                    resultData?.add(it.get())
                }

                if (faersList) {
                    List<Future<AggregateCaseAlert>> faersFutureList = faersList.collect { Map data ->
                        Map evdasData = evdasList.find {
                            it["pt"] == data["PT"]
                        }
                        if (evdasList) {
                            evdasList.remove(evdasData)
                        }
                        Map vigibaseData = vigibaseList.find {
                            it["PT"] == data["PT"]
                        }
                        if(vigibaseData){
                            vigibaseList.remove(vigibaseData)
                        }
                        executorService.submit({ ->
                            createAggAlertForOtherDataSources(data, config, executedConfig, allPrevAlerts, prevAlertDataFaers, reviewedDispositions,
                                    dateRangeStartEndDate, defaultDisposition, otherDataSourcesExecIds, evdasData, vigibaseData, eiList, Constants.DataSource.FAERS, listednessData)

                        } as Callable)
                    }
                    faersFutureList.each {
                        resultData.add(it.get())
                    }
                }

                if (vaersList) {
                    List<Future<AggregateCaseAlert>> vaersFutureList = vaersList.collect { Map data ->
                        Map evdasData = evdasList.find {
                            it["pt"] == data["PT"]
                        }
                        if (evdasList) {
                            evdasList.remove(evdasData)
                        }
                        Map vigibaseData = vigibaseList.find {
                            it["PT"] == data["PT"]
                        }
                        if(vigibaseData){
                            vigibaseList.remove(vigibaseData)
                        }
                        executorService.submit({ ->
                            createAggAlertForOtherDataSources(data, config, executedConfig, allPrevAlerts, prevAlertDataFaers, reviewedDispositions,
                                    dateRangeStartEndDate, defaultDisposition, otherDataSourcesExecIds, evdasData, vigibaseData, eiList, Constants.DataSource.VAERS)

                        } as Callable)
                    }
                    vaersFutureList.each {
                        resultData.add(it.get())
                    }
                }

                if (vigibaseList) {
                    List<Future<AggregateCaseAlert>> vigibaseFutureList = vigibaseList.collect { Map data ->
                        Map evdasData = evdasList.find {
                            it["pt"] == data["PT"]
                        }
                        if (evdasList) {
                            evdasList.remove(evdasData)
                        }
                        executorService.submit({ ->
                            createAggAlertForOtherDataSources(data, config, executedConfig, allPrevAlerts, prevAlertDataFaers, reviewedDispositions,
                                    dateRangeStartEndDate, defaultDisposition, otherDataSourcesExecIds, evdasData, [:], eiList, Constants.DataSource.VIGIBASE, listednessData)

                        } as Callable)
                    }
                    vigibaseFutureList.each {
                        resultData.add(it.get())
                    }
                }
                if (jaderList) {
                    List<Future<AggregateCaseAlert>> jaderFutureList = jaderList.collect { Map data ->
                        executorService.submit({ ->
                            createAggAlertForOtherDataSources(data, config, executedConfig, allPrevAlerts, prevAlertDataFaers, reviewedDispositions,
                                    dateRangeStartEndDate, defaultDisposition, otherDataSourcesExecIds, [:], [:], eiList, Constants.DataSource.JADER, listednessData)

                        } as Callable)
                    }
                    jaderFutureList.each {
                        resultData.add(it.get())
                    }
                }

                if (evdasList){
                    List<Future<AggregateCaseAlert>> evdasFutureList = evdasList.collect { Map data ->
                        executorService.submit({ ->
                            createAggAlertForOtherDataSources(data, config, executedConfig, allPrevAlerts,[:], reviewedDispositions,
                                    dateRangeStartEndDate, defaultDisposition,otherDataSourcesExecIds,[:],[:],eiList,Constants.DataSource.EUDRA,listednessData)
                        } as Callable)
                    }
                    evdasFutureList.each {
                        resultData.add(it.get())
                    }
                }

                executorService.shutdown()
                log.info("Thread Ends")
                Long prevExecId = prevExConfigIds?.size() ? prevExConfigIds.first() : 0
                log.info "Out of ${resultData ? resultData.size() : 0} aggregate alerts, ${pvaList ? pvaList.size() : 0} are qualified for DSS."
                disabledDssNodes = dataObjectService.getDssMetaDataMap(executedConfig.id)
                if(Holders.config.statistics.enable.dssScores && pvaList) {
                    if(executedConfig.masterExConfigId) {
                        log.info("Now calling DSS Module block.")
                        //change for disabledDssNodes
                        statisticsService.mergeDSSScores2(executedConfig.id, executedConfig.masterExConfigId, Constants.DataSource.PVA, allFiles)
                        log.info("Now Ending DSS Module block." + disabledDssNodes)
                    }
                }
                synchronized (com.rxlogix.AggregateCaseAlertService.class) {
                    Thread.sleep(1000)
                }
                batchPersistData(resultData, config, executedConfig, executionStatus, prevExConfigIds[0])
                //Update Dashboard Count Map
                DashboardCountDTO dashboardCountDTO = alertService.prepareDashboardCountDTO(false)
                log.info("generateCountsMap")
                alertService.generateCountsMap(resultData, dashboardCountDTO)
                List<Map> prevAggAlert = fetchPreviousAlerts((prevExecIds?[prevExecIds[0]]:[]), AggregateCaseAlert)
                alertService.updateDashboardCountsForPrevAlert(prevAggAlert, dashboardCountDTO)
                log.info("updateDashboardCounts")
                alertService.updateDashboardCounts(dashboardCountDTO, alertService.mergeCountMaps)
                if(executionStatus){
                    AppAlertProgressStatus archieveAppAlertProgressStatus = appAlertProgressStatusService.createAppAlertProgressStatus(executionStatus, executedConfig.id, "ARCHIEVE", 2, 2, System.currentTimeMillis(), AlertProgressExecutionType.ARCHIEVE)

                }
                if (!config.adhocRun) {
                    synchronized (com.rxlogix.AggregateCaseAlertService.class) {
                        Thread.sleep(1000)
                    }
                    try {
                        archiveService.moveDatatoArchive(executedConfig, AggregateCaseAlert, prevExConfigIds)
                        if(executionStatus){
                            appAlertProgressStatusService.updateAppAlertProgressStatus(executionStatus.id, executedConfig.id, "ARCHIEVE", 3, 3, System.currentTimeMillis())
                        }
                    } catch(Exception archieveExcep) {
                        if (executionStatus){
                            appAlertProgressStatusService.updateAppAlertProgressStatus(executionStatus.id, executedConfig.id, "ARCHIEVE", 2, 1, System.currentTimeMillis())
                        }
                        log.error("Alert got failed while archiving the alert :" + alertService.exceptionString(archieveExcep))
                        throw  archieveExcep
                    }
                }
                alertService.updateOldExecutedConfigurationWithDispCounts(config, executedConfigId,ExecutedConfiguration, resultData.countBy {it.dispositionId}, 0, disabledDssNodes)
                printExecutionMessage(config, executedConfig, resultData)
                Long faersId
                if(executedConfig.selectedDatasource.contains(Constants.DataSource.FAERS)){
                    faersId = otherDataSourcesExecIds.get(Constants.DataSource.FAERS)
                }
                Long vigibaseId
                if(executedConfig.selectedDatasource.contains(Constants.DataSource.VIGIBASE)){
                    vigibaseId = otherDataSourcesExecIds.get(Constants.DataSource.VIGIBASE)
                }
                Long vaersId
                if(executedConfig.selectedDatasource.contains(Constants.DataSource.VAERS)){
                    vaersId = otherDataSourcesExecIds.get(Constants.DataSource.VAERS)
                }
                invokeReportingForQuantAlert(executedConfig, faersId, vigibaseId, vaersId)
            } catch (Throwable throwable) {
                if (Objects.nonNull(throwable) && !isMatchedStringFromException(throwable)) {
                    if(executionStatus){
                        appAlertProgressStatusService.updateAppAlertProgressStatus(executionStatus.id, executedConfig.id, "BR", 2, 1, System.currentTimeMillis())
                    }
                    log.error("Alert got failed while creating aggregate alert in application: " + alertService.exceptionString(throwable))
                }
                throw throwable
            } finally {
                dataObjectService.clearExistingPEHistoryList(scheduledConfigId)
                dataObjectService.clearGlobalProductEventMap(executedConfigId)
                dataObjectService.clearEnabledBusinessConfigProductGrpList(executedConfigId)
            }
        }
    }

    List<String> getDistinctProductName(Long execConfigId) {

        List<String> productName  = AggregateCaseAlert.createCriteria().list {
            projections {
                distinct('productName')
            }
            if(execConfigId){
                eq("executedAlertConfiguration.id", execConfigId)
            }
            isNotNull('productName')
        } as List<String>
        productName
    }

    String prepareMapforDSSCall(List<AggregateCaseAlert> aggregateCaseAlertList, ExecutedConfiguration ec, Long prevExConfig) {

        JSONArray peList = new JSONArray()
        List<Long> alertList = [ec.id]
        JSONArray alertIds = alertList
        JSONArray prevAlertIds = prevExConfig? [prevExConfig]: []
        boolean smqFlag = ec.isGroupBySmq()

        JSONObject productAlertMapping = new JSONObject()
        List<String> productNameList = getDistinctProductName(ec.id)
        if(aggregateCaseAlertList){
            productNameList.each {
                productAlertMapping.put(it as String, ec.id as String)
            }
        }

        for(AggregateCaseAlert alert : aggregateCaseAlertList){
            JSONObject peMap = new JSONObject()
            peMap.put("product_name", alert.getProductName())
            peMap.put("pid", alert.getProductId())
            peMap.put("ptid", alert.getPtCode())
            peMap.put("pt", alert.getPt())
            peMap.put("ebgm_score", alert.getEbgm())
            peMap.put("eb05_score", alert.getEb05())
            peMap.put("eb95_score", alert.getEb95())
            peMap.put("rorLCI_score", alert.getRorLCI())
            peMap.put("rorUCI_score", alert.getRorUCI())
            peMap.put("ror_value", alert.getRorValue())
            peMap.put("listedness", alert.getListed() == null? "no" : alert.getListed().toLowerCase())
            peMap.put("smq_code", alert.getSmqCode())
            peMap.put("new_count", alert.getNewCount())
            peMap.put("new_fatal_count", alert.getNewFatalCount())
            peMap.put("new_serious_count", alert.getNewSeriousCount())
            peMap.put("prr_value", alert.getPrrValue())
            peMap.put("prrLCI_score", alert.getPrrLCI())
            peMap.put("prrUCI_score", alert.getPrrUCI())

            peList.add(peMap)
        }

        JSONObject jsonObject = new JSONObject()
        jsonObject.put("product_to_alert_mapping",productAlertMapping)
        jsonObject.put("alert_ids",alertIds)
        jsonObject.put("prev_alert_ids",prevAlertIds)
        jsonObject.put("smq_flag",smqFlag)
        jsonObject.put("is_master",false)
        jsonObject.put("peMap",peList)
        String fileNameStr = (Holders.config.statistics.inputfile.path as String)

        String alertFileName = fileNameStr +"DSS_REQUEST_${ec.id}.txt"


        Writer writer = new FileWriter(alertFileName)
        jsonObject.write(writer)
        writer.close()

        return alertFileName
    }
    private printExecutionMessage(Configuration config,
                                  ExecutedConfiguration executedConfig,
                                  alertData) {
        def executionMessage = "Execution of Configuration took ${executedConfig.totalExecutionTime}ms " +
                "for configuration ${config.name} [C:${config.id}, EC: ${executedConfig.id}]. " +
                "It gave ${alertData ? alertData.size() : 0} PE combinations"
        log.info(executionMessage)
        log.info("Alert data save flow is complete.")
    }

    private setStatisticsScoresValues(def aca, String productGroupSelection,boolean isEventGroup, String selectedDatasource = "pva") {
        Map statsData

        String ptCodeString = isEventGroup ? aca.ptCode + "-EG" : aca.ptCode
        String productIdString = productGroupSelection ? aca.productId + "-PG" : aca.productId

        if (aca.smqCode)
            statsData = dataObjectService.getStatsDataMap(aca.executedAlertConfigurationId, productIdString,
                    ptCodeString + Constants.Commons.DASH_STRING + aca.smqCode)
        else
            statsData = dataObjectService.getStatsDataMap(aca.executedAlertConfigurationId, productIdString, ptCodeString)

        if (statsData) {
            if(selectedDatasource.equals(Constants.DataSource.VAERS)){
                aca.ebgmVaers = statsData.ebgm as Double
                aca.eb05Vaers = statsData.eb05 as Double
                aca.eb95Vaers = statsData.eb95 as Double
            } else if(selectedDatasource.equals(Constants.DataSource.VIGIBASE)){
                aca.ebgmVigibase = statsData.ebgm as Double
                aca.eb05Vigibase = statsData.eb05 as Double
                aca.eb95Vigibase = statsData.eb95 as Double
            }else if(selectedDatasource.equals(Constants.DataSource.JADER)){
                aca.ebgmJader = statsData.ebgm as Double
                aca.eb05Jader = statsData.eb05 as Double
                aca.eb95Jader = statsData.eb95 as Double
            } else if (aca instanceof Map) {
                aca.ebgmFaers = statsData.ebgm as Double
                aca.eb05Faers = statsData.eb05 as Double
                aca.eb95Faers = statsData.eb95 as Double
            } else {
                aca.ebgm = statsData.ebgm as Double
                aca.eb05 = statsData.eb05 as Double
                aca.eb95 = statsData.eb95 as Double
            }
        }
    }

    private setStatsSubgroupingScoresValues(AggregateCaseAlert aca,String productGroupSelection,boolean isEventGroup) {
        Map statsData
        String ptCodeString = isEventGroup ? aca.ptCode + "-EG" : aca.ptCode

        String productIdString = productGroupSelection ? aca.productId + "-PG" : aca.productId

        Map ebgmSubGroupStatsData
        if (aca.smqCode) {
            statsData = dataObjectService.getStatsDataMapSubgrouping(aca.executedAlertConfigurationId, productIdString,
                    ptCodeString + Constants.Commons.DASH_STRING + aca.smqCode)
            ebgmSubGroupStatsData = dataObjectService.getEbgmStatsDataMapSubgrouping(aca.executedAlertConfigurationId, productIdString,
                    ptCodeString + Constants.Commons.DASH_STRING + aca.smqCode)
        }else {
            statsData = dataObjectService.getStatsDataMapSubgrouping(aca.executedAlertConfigurationId, productIdString, ptCodeString)
            ebgmSubGroupStatsData = dataObjectService.getEbgmStatsDataMapSubgrouping(aca.executedAlertConfigurationId, productIdString, ptCodeString)
        }
        if (statsData) {
            aca.ebgmAge = statsData.ebgmAge
            aca.eb05Age = statsData.eb05Age
            aca.eb95Age = statsData.eb95Age
            aca.ebgmGender = statsData.ebgmGender
            aca.eb05Gender = statsData.eb05Gender
            aca.eb95Gender = statsData.eb95Gender
        }
        if(ebgmSubGroupStatsData){
            aca.ebgmSubGroup = ebgmSubGroupStatsData.ebgmSubGroup
            aca.eb05SubGroup = ebgmSubGroupStatsData.eb05SubGroup
            aca.eb95SubGroup = ebgmSubGroupStatsData.eb95SubGroup
        }
    }

    private setStatsSubgroupingScoresValuesFaers(Map faersColumnMap,AggregateCaseAlert aca,String productGroupSelection,boolean isEventGroup) {
        Map statsData

        String ptCodeString = isEventGroup ? faersColumnMap.ptCode + "-EG" : faersColumnMap.ptCode
        String productIdString = productGroupSelection ? faersColumnMap.productId + "-PG" : faersColumnMap.productId

        if (faersColumnMap.smqCode)
            statsData = dataObjectService.getStatsDataMapSubgrouping(faersColumnMap.executedAlertConfigurationId, productIdString,
                    ptCodeString + Constants.Commons.DASH_STRING + faersColumnMap.smqCode)
        else
            statsData = dataObjectService.getStatsDataMapSubgrouping(faersColumnMap.executedAlertConfigurationId, productIdString, ptCodeString)

        if (statsData) {
            aca.ebgmAgeFaers = statsData.ebgmAge
            aca.eb05AgeFaers= statsData.eb05Age
            aca.eb95AgeFaers = statsData.eb95Age
            aca.ebgmGenderFaers = statsData.ebgmGender
            aca.eb05GenderFaers = statsData.eb05Gender
            aca.eb95GenderFaers = statsData.eb95Gender
        }
    }

    private setPrrRorScoresValues(AggregateCaseAlert aca, String productGroupSelection, boolean isEventGroup, Long execConfigId, Map faersColumnMap = null, Map vaersColumnMap = null, Map vigibaseColumnMap = null,Map jaderColumnMap=null) {
        Map statsData
        Map rorStatsData
        Map prrSubGroupStatsData
        Map rorSubGroupStatsData
        String ptCodeString = ""
        String productIdString = ''
        def aggAlert = faersColumnMap ?: aca
        if (isEventGroup) {
            ptCodeString = aggAlert.ptCode + "-EG"
        } else {
            ptCodeString = aggAlert.ptCode
        }

        if (productGroupSelection) {
            productIdString = "${aggAlert.productId}-PG"
        } else {
            productIdString = aggAlert.productId
        }

        if (aggAlert.smqCode) {
            statsData = dataObjectService.getProbDataMap(execConfigId, productIdString,
                    ptCodeString + Constants.Commons.DASH_STRING + aggAlert.smqCode)
            rorStatsData = dataObjectService.getRorProbDataMap(execConfigId, productIdString,
                    ptCodeString + Constants.Commons.DASH_STRING + aggAlert.smqCode)
            prrSubGroupStatsData = dataObjectService.getPrrSubGroupDataMap(execConfigId, productIdString,
                    ptCodeString + Constants.Commons.DASH_STRING + aggAlert.smqCode)
            rorSubGroupStatsData = dataObjectService.getRorSubGroupDataMap(execConfigId, productIdString,
                    ptCodeString + Constants.Commons.DASH_STRING + aggAlert.smqCode)
        }else {
            statsData = dataObjectService.getProbDataMap(execConfigId, productIdString, ptCodeString)
            rorStatsData = dataObjectService.getRorProbDataMap(execConfigId, productIdString,ptCodeString)
            prrSubGroupStatsData = dataObjectService.getPrrSubGroupDataMap(execConfigId, productIdString,ptCodeString)
            rorSubGroupStatsData = dataObjectService.getRorSubGroupDataMap(execConfigId, productIdString,ptCodeString)
        }
        if (statsData) {
            if(vaersColumnMap){
                vaersColumnMap.prrValueVaers = statsData.prrValue
                vaersColumnMap.prrUCIVaers = statsData.prrUCI
                vaersColumnMap.prrLCIVaers = statsData.prrLCI
                vaersColumnMap.rorValueVaers = statsData.rorValue
                vaersColumnMap.rorLCIVaers = statsData.rorLCI
                vaersColumnMap.rorUCIVaers = statsData.rorUCI
                vaersColumnMap.chiSquareVaers = statsData.chiSquare
                vaersColumnMap.aValueVaers = statsData.aValue
                vaersColumnMap.bValueVaers = statsData.bValue
                vaersColumnMap.cValueVaers = statsData.cValue
                vaersColumnMap.dValueVaers = statsData.dValue
                vaersColumnMap.eValueVaers = statsData.eValue
                vaersColumnMap.rrValueVaers = statsData.rrValue
            } else if(vigibaseColumnMap){
                vigibaseColumnMap.prrValueVigibase = statsData.prrValue
                vigibaseColumnMap.prrUCIVigibase = statsData.prrUCI
                vigibaseColumnMap.prrLCIVigibase = statsData.prrLCI
                vigibaseColumnMap.rorValueVigibase = statsData.rorValue
                vigibaseColumnMap.rorLCIVigibase = statsData.rorLCI
                vigibaseColumnMap.rorUCIVigibase = statsData.rorUCI
                vigibaseColumnMap.chiSquareVigibase = statsData.chiSquare
                vigibaseColumnMap.aValueVigibase = statsData.aValue
                vigibaseColumnMap.bValueVigibase = statsData.bValue
                vigibaseColumnMap.cValueVigibase = statsData.cValue
                vigibaseColumnMap.dValueVigibase = statsData.dValue
                vigibaseColumnMap.eValueVigibase = statsData.eValue
                vigibaseColumnMap.rrValueVigibase = statsData.rrValue
            } else if(jaderColumnMap){
                jaderColumnMap.prrValueJader = statsData.prrValue
                jaderColumnMap.prrUCIJader = statsData.prrUCI
                jaderColumnMap.prrLCIJader = statsData.prrLCI
                jaderColumnMap.rorValueJader = statsData.rorValue
                jaderColumnMap.rorLCIJader = statsData.rorLCI
                jaderColumnMap.rorUCIJader = statsData.rorUCI
                jaderColumnMap.chiSquareJader = statsData.chiSquare
                jaderColumnMap.aValueJader = statsData.aValue
                jaderColumnMap.bValueJader = statsData.bValue
                jaderColumnMap.cValueJader = statsData.cValue
                jaderColumnMap.dValueJader = statsData.dValue
                jaderColumnMap.eValueJader = statsData.eValue
                jaderColumnMap.rrValueJader = statsData.rrValue
            } else if (faersColumnMap) {
                faersColumnMap.prrValueFaers = statsData.prrValue
                faersColumnMap.prrUCIFaers = statsData.prrUCI
                faersColumnMap.prrLCIFaers = statsData.prrLCI
                faersColumnMap.prrMhFaers = statsData.prrMh
                faersColumnMap.rorValueFaers = statsData.rorValue
                faersColumnMap.rorLCIFaers = statsData.rorLCI
                faersColumnMap.rorUCIFaers = statsData.rorUCI
                faersColumnMap.rorMhFaers = statsData.rorMh
                faersColumnMap.chiSquareFaers = statsData.chiSquare
                faersColumnMap.aValueFaers = statsData.aValue
                faersColumnMap.bValueFaers = statsData.bValue
                faersColumnMap.cValueFaers = statsData.cValue
                faersColumnMap.dValueFaers = statsData.dValue
                faersColumnMap.eValueFaers = statsData.eValue
                faersColumnMap.rrValueFaers = statsData.rrValue
                aca.prrStrFaers = statsData.prrStr
                aca.prrStrLCIFaers = statsData.prrStrLCI
                aca.prrStrUCIFaers = statsData.prrStrUCI
                aca.rorStrFaers = statsData.rorStr
                aca.rorStrLCIFaers = statsData.rorStrLCI
                aca.rorStrUCIFaers = statsData.rorStrUCI
            } else {
                aca.prrValue = statsData.prrValue ?: aca.prrValue
                aca.prrUCI = statsData.prrUCI
                aca.prrLCI = statsData.prrLCI
                aca.aValue = statsData.aValue
                aca.bValue = statsData.bValue
                aca.cValue = statsData.cValue
                aca.dValue = statsData.dValue
                aca.eValue = statsData.eValue
                aca.rrValue = statsData.rrValue
            }
        }
        if(rorStatsData){
            aca.rorValue = rorStatsData.rorValue ?: aca.rorValue
            def isRor = cacheService.getRorCache();
            String disproportionalityScoresQueryName = Holders.config.pvreports.disproportionality.scores.query.name
            SuperQueryDTO disproportionalityScoresQuery = queryService.queryDetailByName(disproportionalityScoresQueryName)
            if (!isRor && !aca.alertConfiguration.alertQueryId && disproportionalityScoresQuery) {
                aca.rorValue = null
            }
            aca.rorLCI = rorStatsData.rorLCI
            aca.rorUCI = rorStatsData.rorUCI
            aca.chiSquare = rorStatsData.chiSquare ?: aca.chiSquare
        }
        if(prrSubGroupStatsData){
            aca.prrSubGroup = prrSubGroupStatsData.prrSubGroup
            aca.prrLciSubGroup = prrSubGroupStatsData.prrLciSubGroup
            aca.prrUciSubGroup = prrSubGroupStatsData.prrUciSubGroup
        }
        if(rorSubGroupStatsData){
            aca.rorSubGroup = rorSubGroupStatsData.rorSubGroup
            aca.rorLciSubGroup = rorSubGroupStatsData.rorLciSubGroup
            aca.rorUciSubGroup = rorSubGroupStatsData.rorUciSubGroup
            aca.chiSquareSubGroup = rorSubGroupStatsData.chiSquareSubGroup
            aca.rorRelSubGroup = rorSubGroupStatsData.rorRelSubGroup
            aca.rorLciRelSubGroup = rorSubGroupStatsData.rorLciRelSubGroup
            aca.rorUciRelSubGroup = rorSubGroupStatsData.rorUciRelSubGroup
        }
    }


    private preparePreviousData(List<Map> alertData,List<ExecutedConfiguration> prevExecutions, List<Map> allPrevAlerts,
                                Long execConfigId, List<Long> reviewedDispositions, String selectedDatasource) {

        Map orgMap = [:]
        Map trendAlertMap = [:]
        Map leftOver = [:]
        String sieveAnalysisQueryName = Holders.config.pvsignal.sieveAnalysisQueryName
        SuperQueryDTO sieveAnalysisQuery = queryService.queryDetailByName(sieveAnalysisQueryName)
        Boolean isBackendQuery = ((selectedDatasource.startsWith(Constants.DataSource.PVA) && Holders.config.signal.sieveAnalysis.safetyDB) ||(selectedDatasource.startsWith(Constants.DataSource.FAERS) && Holders.config.signal.sieveAnalysis.faers) || (selectedDatasource.startsWith(Constants.DataSource.VAERS) && Holders.config.signal.sieveAnalysis.vaers)
        || (selectedDatasource.startsWith(Constants.DataSource.VIGIBASE) && Holders.config.signal.sieveAnalysis.vigibase)) && sieveAnalysisQuery
        for (Map aggregateCaseAlert : allPrevAlerts) {
            String periodEndDate = DateUtil.stringFromDate(aggregateCaseAlert.periodEndDate, DateUtil.DATEPICKER_FORMAT, Constants.UTC)
            trendAlertMap.put(execConfigId + "-" + aggregateCaseAlert.productId + "-" + aggregateCaseAlert.ptCode + "${aggregateCaseAlert.smqCode ? '-' + aggregateCaseAlert.smqCode : ''}" + '-' + periodEndDate, aggregateCaseAlert)
            orgMap.put(execConfigId + "-" + aggregateCaseAlert.productId + "-" + aggregateCaseAlert.ptCode, aggregateCaseAlert)
        }

        List<String> endDateRangeList = []
        for (ExecutedConfiguration prevExConfig : prevExecutions) {
            String periodEndDate = DateUtil.stringFromDate(prevExConfig.executedAlertDateRangeInformation.dateRangeEndAbsolute, DateUtil.DATEPICKER_FORMAT, Constants.UTC)
            //trend type calculated only when previously executed alerts have distinct date ranges
            endDateRangeList.push(periodEndDate)
        }
        String[] ptCodeWithSMQ = []
        String ptCode
        String key
        for (Map data : alertData) {
            String freqPriority = ''
            String trendType = ''
            String flag = ''

            //trend map
            Integer totalCount = 0
            Integer maxCount = 0
            Integer prevAlertsCount = 0
            Integer avgAlertCount = 0
            String prevPriority = ''
            String prevAlertKey = ''
            Map prevAggAlert = null
            ptCodeWithSMQ = data["PT_CODE"] ? String.valueOf(data["PT_CODE"]).split(Constants.Commons.DASH_STRING, 2) : ["0"]
            ptCode = ptCodeWithSMQ ? ptCodeWithSMQ[0] ? ptCodeWithSMQ[0] : 0 : Constants.Commons.UNDEFINED_NUM
            endDateRangeList.each { String endDate ->
                if(prevAlertsCount<Holders.config.signal.sieveAnalysis.period){
                    prevAlertKey = execConfigId + "-" + data["PRODUCT_ID"] + "-" + "${ptCodeWithSMQ.size() > 1 ? data["PT_CODE"] : ptCode}" + "-" + endDate
                    Map prevAlert = trendAlertMap.get(prevAlertKey)
                    String prevPriorityKey = execConfigId + "-" + data["PRODUCT_ID"] + "-" + "${ptCodeWithSMQ.size() > 1 ? data["PT_CODE"] : ptCode}" + "-" + endDateRangeList.first()
                    Map prevPriorityMap = trendAlertMap.get(prevPriorityKey)
                    if (prevAlert) {
                        Integer newCount =-1;
                        if(isBackendQuery) {
                            newCount = prevAlert.newCountFreqCalc?:0
                        } else if( prevAlert.newCount == -1 && selectedDatasource.equals(Constants.DataSource.FAERS)){
                            JsonSlurper jsonSlurper = new JsonSlurper()
                            def faersColumns = prevAlert?.faersColumns ? jsonSlurper.parseText(prevAlert?.faersColumns) : [:]
                            if(faersColumns != [:]){
                                newCount = faersColumns.newCountFaers ? faersColumns?.newCountFaers as Integer : -1
                            }
                        } else{
                            newCount = prevAlert.newCount
                        }

                        if (!prevPriority) {
                            if( prevAlert.newCount == -1 && selectedDatasource.equals(Constants.DataSource.FAERS)){
                                JsonSlurper jsonSlurper = new JsonSlurper()
                                def prevFaersColumns = prevPriorityMap?.faersColumns ? jsonSlurper.parseText(prevPriorityMap?.faersColumns) : [:]
                                if(prevFaersColumns != [:]){
                                    prevPriority = prevFaersColumns?.freqPriorityFaers
                                }
                            }
                            else {
                                prevPriority = prevPriorityMap?.freqPriority
                            }
                        }
                        prevAlertsCount++
                        totalCount += newCount
                        if (maxCount < newCount) {
                            maxCount = newCount
                        }
                        if (!prevAggAlert) {
                            prevAggAlert = prevAlert
                        }
                    }
                }
            }
            if (prevAlertsCount != 0) {
                avgAlertCount = totalCount / prevAlertsCount
            }
            if (prevAggAlert) {
                if (prevAggAlert.dispositionId in reviewedDispositions) {
                    flag = "Previously Reviewed"
                }
            } else {
                flag = "New"
            }

            String currentPriority = ''
            if((selectedDatasource.startsWith(Constants.DataSource.PVA) && Holders.config.signal.sieveAnalysis.safetyDB) ||(selectedDatasource.startsWith(Constants.DataSource.FAERS) && Holders.config.signal.sieveAnalysis.faers) || (selectedDatasource.startsWith(Constants.DataSource.VAERS) && Holders.config.signal.sieveAnalysis.vaers)
                    || (selectedDatasource.startsWith(Constants.DataSource.VIGIBASE) && Holders.config.signal.sieveAnalysis.vigibase)) {
                if (sieveAnalysisQuery) {
                    if(data["NEW_COUNT_FREQ_CALC"] != null && data["CUMM_COUNT_FREQ_CALC"] != null)
                        currentPriority = calcFreqPriority(data["NEW_COUNT_FREQ_CALC"].intValueExact(), data["CUMM_COUNT_FREQ_CALC"].intValueExact(), maxCount, avgAlertCount)
                } else{
                    currentPriority = calcFreqPriority(data["NEW_COUNT"].intValueExact(), data["CUMM_COUNT"].intValueExact(), maxCount, avgAlertCount)
                }
                if(currentPriority != '') {
                    freqPriority = currentPriority
                    trendType = setTrendType(currentPriority, prevPriority)
                }
            }
            key = execConfigId + '-' + data["PRODUCT_ID"] + '-' + "${ptCodeWithSMQ.size() > 1 ? data["PT_CODE"] : ptCode}"

            if (orgMap.containsKey(key)) {
                leftOver.put(key, ['prevAlert': orgMap.get(key), 'trendType': trendType, 'freqPriority': freqPriority, 'flags': flag])
            } else {
                leftOver.put(key, ['prevAlert': '', 'trendType': trendType, 'freqPriority': freqPriority, 'flags': flag])
            }
        }
        leftOver
    }

    private List<Map> fetchPreviousAlerts(List<Long> prevExecIds, def domain) {
        List<Map> fetchPrevAlerts = []
        if (prevExecIds) {
            fetchPrevAlerts = domain.createCriteria().list(order: 'asc') {
                resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                projections {
                    property("periodEndDate", "periodEndDate")
                    property("productId", "productId")
                    property("ptCode", "ptCode")
                    property("smqCode", "smqCode")
                    property("newSponCount", "newSponCount")
                    property("newCount", "newCount")
                    property("newCountFreqCalc", "newCountFreqCalc")
                    property("freqPriority", "freqPriority")
                    property("faersColumns", "faersColumns")
                    property("dueDate", "dueDate")
                    disposition {
                        property("id", "dispositionId")
                    }
                    property("assignedTo.id", 'assignedToId')
                    property("assignedToGroup.id", 'assignedToGroupId')
                }
                or {
                    prevExecIds.collate(1000).each{
                        'in'('executedAlertConfiguration.id', it)
                    }
                }
            } as List<Map>

        }
        fetchPrevAlerts
    }

    private List<Map> fetchPreviousAlertsForTrendFlag(List<Long> prevExecIds, def domain, List<Long> reviewedDispositions,
                                                      boolean isThresholdMet, AggregateCaseAlert aggAlert = null) {
        String sysUserFullName = cacheService.getUserByUserNameIlike( Constants.SYSTEM_USER)?.fullName
        List<Map> fetchPrevAlerts = []
        if (prevExecIds) {
            fetchPrevAlerts = domain.createCriteria().list() {
                resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                projections {
                    property("productId", "productId")
                    property("ptCode", "ptCode")
                    property("pt", "pt")
                    property("freqPeriod", "freqPeriod")
                    property("dispPerformedBy", "dispPerformedBy")
                    disposition {
                        property("id", "dispositionId")
                    }
                    property("executedAlertConfiguration.id", 'execConfigId')
                    property("freqPeriod", 'freqPeriod')
                    property("cumFreqPeriod", 'cumFreqPeriod')
                }
                or {
                    prevExecIds.collate(1000).each{
                        'in'('executedAlertConfiguration.id', it)
                    }
                }
                isNotNull("dispPerformedBy")
                not{
                    ilike "dispPerformedBy", "${sysUserFullName}"
                }
                geProperty("dispLastChange", "dateCreated")
                disposition {
                    'in'("id", reviewedDispositions)
                }
                if (isThresholdMet) {
                    eq("productId", aggAlert.productId)
                    eq("ptCode", aggAlert.ptCode)
                    eq("pt", aggAlert.pt)
                } else {
                    order("executedAlertConfiguration.id", "desc")
                }
            } as List<Map>

        }
        fetchPrevAlerts
    }

    public List<ExecutedConfiguration> fetchPreviousExecConfigs(ExecutedConfiguration executedConfiguration, Long configId) {
        Date exEndDate = executedConfiguration.executedAlertDateRangeInformation.dateRangeEndAbsolute

        List result = ExecutedConfiguration.createCriteria().list() {
            eq("configId", executedConfiguration.configId)
            eq("type", executedConfiguration.type)
            eq("adhocRun", executedConfiguration.adhocRun)
            eq("isDeleted", false)
            eq("isEnabled", true)
            ne("id", executedConfiguration.id)
            order("id", "desc")
        }
        result
    }

    private getPreviousAlertMap(ExecutedConfiguration previousExecutedConfig) {
        Map previousAlertMap = [:]
        List<AggregateCaseAlert> prevEvdasAlertList = AggregateCaseAlert.findAllByExecutedAlertConfiguration(previousExecutedConfig)
        for (AggregateCaseAlert alert : prevEvdasAlertList) {
            previousAlertMap.put(alert.productName + "-" + alert.pt, alert)
        }
        previousAlertMap
    }

    private Map getPreviousAlertTags(ExecutedConfiguration previousExecutedConfig) {
        def data = [:]
        Session session = sessionFactory.currentSession
        try {
            String sql_statement = SignalQueryHelper.aca_previousAlertTags_sql(previousExecutedConfig.id)
            SQLQuery sqlQuery = session.createSQLQuery(sql_statement)

            sqlQuery.list().each { row ->
                String key = row[0].toString() + "-" + row[1].toString() + "-" + row[2].toString()
                Set<String> tags
                if(data.get(key)){
                    tags = data.get(key)
                }else{
                    tags = new HashSet<>()
                }
                tags.add(row[3])
                data.put(key,tags)
            }
        }catch(Exception ex){
            ex.printStackTrace()
        } finally {
            session.flush()
            session.clear()
        }
        data
    }

    private Map getAllPreviousTags(ExecutedConfiguration executedConfig, String configIds) {
        def data = [:]
        Session session = sessionFactory.currentSession
        try {
            String sql_statement = SignalQueryHelper.aca_allGlobalTags_sql(configIds)
            SQLQuery sqlQuery = session.createSQLQuery(sql_statement)

            sqlQuery.list().each { row ->
                String key = row[0].toString() + "-" + row[1].toString() + "-" + row[2].toString()
                Set<String> tags
                if(data.get(key)){
                    tags = data.get(key)
                }else{
                    tags = new HashSet<>()
                }
                tags.add(row[3])
                data.put(key,tags)
            }

            sql_statement = SignalQueryHelper.aca_allAlertTags_sql(configIds)
            sqlQuery = session.createSQLQuery(sql_statement)

            sqlQuery.list().each { row ->
                String key = row[0].toString() + "-" + row[1].toString() + "-" + row[2].toString()
                Set<String> tags
                if(data.get(key)){
                    tags = data.get(key)
                }else{
                    tags = new HashSet<>()
                }
                tags.add(row[3])
                data.put(key,tags)
            }
        }catch(Exception ex){
            ex.printStackTrace()
        } finally {
            session.flush()
            session.clear()
        }
        data
    }

    /**
     * Method to batch persist the aggregate alerts. Here alerts are persisted into database based on the config defined size.
     * Before persistance for each alert the stats scores values are set and then business configuration run is executed(order is important).
     * @param alertList : The list of alerts to be persisted.
     * @param config : The alert configuration object.
     * @param groupBySmq : The flag which tells if group by smq is enabled or not. The stats scores are not calculated when its on.
     */
    void batchPersistAggregateAlert(alertList, Configuration config, ExecutedConfiguration executedConfig, ExecutionStatus executionStatus = null) throws Exception {
        String defaultDispositionValue = config?.getWorkflowGroup()?.defaultQuantDisposition?.value
        ExecutedConfiguration previousExecutedConfig = null
        previousExecutedConfig = fetchLastExecutionOfAlert(executedConfig)
        List<ExecutedConfiguration> allPrevExecutedConfig = fetchAllPrevExecutionOfAlert(executedConfig)
        Map<String, AggregateCaseAlert> previousAlertMap = getPreviousAlertMap(previousExecutedConfig)
        List<Long> prevExecList = alertService.fetchPrevExecConfigId(executedConfig, config)
        prevExecList = prevExecList?.takeRight(6)
        List prevAlertMapList = []
        if(prevExecList != null || prevExecList.size() != 0) {
            for (Long prevExecId : prevExecList) {
                ExecutedConfiguration prevConfig = ExecutedConfiguration.get(prevExecId)
                prevAlertMapList.add(getPreviousAlertMap(prevConfig))
            }
        }
        Map<String, Set<String>> previousAlertTags = [:]
        Map<String, Set<String>> allPreviousTags = [:]
        if(allPrevExecutedConfig) {
            previousAlertTags = getPreviousAlertTags(allPrevExecutedConfig[0])
            allPreviousTags = getAllPreviousTags(allPrevExecutedConfig[0], allPrevExecutedConfig*.id.join(","))
        }
        List impEventList = businessConfigurationService.generateImpEventList()
        List aggAlertList = []
        boolean dssPvaFlag = executedConfig.selectedDatasource.contains(Constants.DataSource.PVA)? true:false
        log.info("Applying business rule for ${executedConfig.id}")
        def startTime = System.currentTimeSeconds()
        ExecutorService executorService = signalExecutorService.threadPoolForQuantAlertExec()
        StringBuffer logStringBuilder = new StringBuffer()
        logStringBuilder.append("\nApplying business rule for ${executedConfig.id}")
        try {
            alertList.collate(5000).each { List<AggregateCaseAlert> aggregateCaseAlertList ->
                List<Future<AggregateCaseAlert>> futureList = aggregateCaseAlertList.collect { AggregateCaseAlert aggregateCaseAlert ->
                    executorService.submit({ ->
                        Disposition initialDisp = aggregateCaseAlert.disposition
                        singleCaseAlertService.calculateDssScore(aggregateCaseAlert, dssPvaFlag)
                        if (executedConfig.selectedDatasource.contains(Constants.DataSource.PVA)) {
                            businessConfigurationService.executeRulesForAggregateAlert(aggregateCaseAlert, config.productDictionarySelection, executedConfig.productSelection,
                                    defaultDispositionValue, previousAlertMap, previousAlertTags, allPreviousTags, impEventList, initialDisp,Constants.DataSource.PVA,logStringBuilder)
                        }
                        if (aggregateCaseAlert.evdasColumns) {
                            businessConfigurationService.executeRulesForEvdasAlertIntegratedReview(aggregateCaseAlert, executedConfig.productSelection, defaultDispositionValue, previousAlertMap, previousAlertTags, allPreviousTags, impEventList, initialDisp,logStringBuilder)
                        }
                        if (aggregateCaseAlert.vaersColumns) {
                            businessConfigurationService.executeRulesForVaersIntegratedReview(aggregateCaseAlert, config.productDictionarySelection, executedConfig.productSelection, defaultDispositionValue, previousAlertMap, previousAlertTags, allPreviousTags, initialDisp,logStringBuilder)
                        }
                        if (aggregateCaseAlert.faersColumns) {
                            businessConfigurationService.executeRulesForFaersIntegratedReview(aggregateCaseAlert, config.productDictionarySelection, executedConfig.productSelection, defaultDispositionValue, previousAlertMap, previousAlertTags, allPreviousTags, impEventList, initialDisp,logStringBuilder)
                        }
                        if (aggregateCaseAlert.vigibaseColumns) {
                            businessConfigurationService.executeRulesForVigibaseIntegratedReview(aggregateCaseAlert, config.productDictionarySelection, executedConfig.productSelection, defaultDispositionValue, previousAlertMap, previousAlertTags, allPreviousTags, initialDisp,logStringBuilder)
                        }
                        if (aggregateCaseAlert.jaderColumns) {
                            businessConfigurationService.executeRulesForJaderReview(aggregateCaseAlert, config.productDictionarySelection, executedConfig.productSelection, defaultDispositionValue, previousAlertMap, previousAlertTags, allPreviousTags,initialDisp,logStringBuilder)
                        }
                        aggregateCaseAlert
                    } as Callable)
                }
                futureList.each {
                    aggAlertList.add(it.get())
                }
                if(executionStatus){
                    appAlertProgressStatusService.updateAppAlertProgressStatus(executionStatus.id, executedConfig.id, "BR", 3, 3, System.currentTimeMillis())
                }
            }
            Promise promise = task {
                alertService.saveLogsInFile(logStringBuilder,"${config.id}_${executedConfig.type}")
            }
            promise.onError {
                log.error("BR Log File Processing Failed.")
            }
            promise.onComplete {
                log.info("BR Log File Processing Finished.")
            }
        }catch (Exception brexcep) {
            if(executionStatus){
                appAlertProgressStatusService.updateAppAlertProgressStatus(executionStatus.id, executedConfig.id, "BR", 2, 1, System.currentTimeMillis())
            }
            log.error("Alert got failed while applying business rules: " + alertService.exceptionString(brexcep))
            throw brexcep
        }
        log.info("Total time taken in applying business rule is ${System.currentTimeSeconds() - startTime} seconds.")
        List<ProductEventHistory> productEventHistoryList = []
        List<ValidatedSignal> qualifiedSignalList = []
        boolean isAutoProposed = grailsApplication.config.dss.enable.autoProposed
        dssPvaFlag = dssPvaFlag && isAutoProposed
        if(dssPvaFlag && aggAlertList) {
            final String DISPOSITION = "DISPOSITION";
            productEventHistoryList = ProductEventHistory.createCriteria().list {
                eq("change", DISPOSITION)
                eq("configId", aggAlertList[0].alertConfiguration.id)
                order("id", "desc")
                ne("modifiedBy", Constants.Commons.SYSTEM)
            } as List<ProductEventHistory>

            //// -- todo -- add product name/ pg filter here
            if(productEventHistoryList) {
                qualifiedSignalList = ValidatedSignal.createCriteria().list {
                    order("lastUpdated", "desc")
                    ne("modifiedBy", Constants.Commons.SYSTEM)
                } as List<ValidatedSignal>
            }
        }
        log.info "${executedConfig.id}: Product Event History available for this PE combination are : ${productEventHistoryList ? productEventHistoryList.size() : 0}"
        try {
            if(executionStatus){
                AppAlertProgressStatus persistAppAlertProgressStatus = appAlertProgressStatusService.createAppAlertProgressStatus(executionStatus, executedConfig
                        .id, "PERSIST", 2, 2, System.currentTimeMillis(), AlertProgressExecutionType.PERSIST)
            }
            singleCaseAlertService.batchPersistForAlert(aggAlertList, Constants.AlertConfigType.AGGREGATE_CASE_ALERT, dssPvaFlag, productEventHistoryList, qualifiedSignalList)
            if(executionStatus){
                appAlertProgressStatusService.updateAppAlertProgressStatus(executionStatus.id, executedConfig.id, "PERSIST", 3, 3, System.currentTimeMillis())
            }
        } catch (Exception persistExcep) {
            if(executionStatus){
                appAlertProgressStatusService.updateAppAlertProgressStatus(executionStatus.id, executedConfig.id, "PERSIST", 2, 1, System.currentTimeMillis())
            }
            log.error("Alert got failed while persisting data in app schema: " + alertService.exceptionString(persistExcep))
            throw persistExcep
        }
        productEventHistoryList = null
        qualifiedSignalList = null
    }

    /**
     * The calculation of priority is based on the counts of alerts.
     * based on the algorithm the priority tag is determined.
     * @param newSponCount
     * @param cumSponCount
     * @param max_count
     * @param avg_count
     * @return
     */
    private calcFreqPriority(int newCount, int cumCount, int max_count, avg_count) {
        def priority_tag = "Priority 1"
        if (newCount <= 0) {
            priority_tag = "Priority 7"
        } else if (newCount <= avg_count) {
            priority_tag = "Priority 6"
        } else if ((max_count != null) && (newCount <= max_count)) {
            priority_tag = "Priority 5"
        } else if (newCount <= (0.25) * (cumCount)) {
            priority_tag = "Priority 4"
        } else if ((max_count != null) && (newCount <= (2) * (max_count))) {
            priority_tag = "Priority 3"
        } else if (newCount < cumCount) {
            priority_tag = "Priority 2"
        }
        priority_tag
    }

    /**
     * Trend is determined based on the current and previous priority values.
     * Rules :-
     *
     * Current priority is of type 'Priority 1' or 'Priority 2' or 'Priority 3' and Previous priority is of type
     * 'Priority 1' or 'Priority 2' or 'Priority 3' then we say the trend type as 'Continuing Trend'.
     *
     * Current priority is of type 'Priority 1' or 'Priority 2' or 'Priority 3' and Previous priority is of type
     * 'Priority 4' or 'Priority 5' or 'Priority 6' or 'Priority 7' then we say the trend type as 'Emerging Trend'.
     *
     * Else its 'No Trend'
     *
     * @param currentPriority
     * @param prevPriority
     * @return
     */
    private setTrendType(currentPriority, prevPriority) {
        def trendType = "No Trend"
        if (currentPriority in ['Priority 1', 'Priority 2', 'Priority 3'] && prevPriority in ['Priority 1', 'Priority 2', 'Priority 3']) {
            trendType = "Continuing Trend"
        } else if (currentPriority in ['Priority 1', 'Priority 2', 'Priority 3'] && prevPriority in ['Priority 4', 'Priority 5', 'Priority 6', 'Priority 7']) {
            trendType = "Emerging Trend"
        }
        trendType
    }
    /**
     * Method to batch persis the aggregate alert data and its history, activity and updates the other agg alerts.
     * @param data
     */
    @Transactional
    void batchPersistData(List<AggregateCaseAlert> alertList, Configuration scheduledConfig, ExecutedConfiguration executedConfig, ExecutionStatus executionStatus, Long prevExecConfigId) {
        def time1 = System.currentTimeMillis()
        log.info("Now persisting the execution related data in a batch.")

        //Persist the alerts
        batchPersistAggregateAlert(alertList, scheduledConfig, executedConfig, executionStatus)

        List<Map> productEventAlertMapping = getProductEventAlertMapping(alertList)
        dataObjectService.setGlobalProductAlertMap(executedConfig?.id, productEventAlertMapping)
        productEventAlertMapping = null

        dataObjectService.clearStatsDataMap(executedConfig.id)

        //Update the aggregate alerts states

        //Persist the history
        List<Map> peHistoryMapList = dataObjectService.getBusinessConfigPropertiesMapList(executedConfig.id)
        productEventHistoryService.batchPersistHistory(peHistoryMapList)
        peHistoryMapList=null
        dataObjectService.clearBusinessConfigPropertiesMap(executedConfig.id)

        //Persist the activities
        activityService.setActivities(executedConfig.id)
        signalAuditLogService.saveAuditTrailForBusinessRuleActions(executedConfig.id)
        dataObjectService.setDefaultDispositionMap(scheduledConfig.id, scheduledConfig.getWorkflowGroup()?.defaultQualiDisposition)
        //Persist signal with Aggregate Case Alert
        if (!scheduledConfig.adhocRun) {
            persistValidatedSignalWithAggCaseAlert(executedConfig.id, scheduledConfig.name, scheduledConfig.id)
        }
        saveTagsForBusinessConfig(executedConfig.id)

        dataObjectService.clearGlobalProductAlertMap(executedConfig?.id)


        //Clearing last review maps
        dataObjectService.clearCurrentEndDateMap(executedConfig.id)
        dataObjectService.clearLastReviewDurationMap(executedConfig.id)
        if (prevExecConfigId) {
            log.info("Now Saving the Actions")
            Session session = sessionFactory.currentSession
            insertAggAlertActions(session, executedConfig.id, prevExecConfigId)
        }
        if (alertService.isProductSecurity()) {
            addProductInCache(executedConfig.id)
        }

        log.info("Persistance of execution related data in a batch is done.")
        def time2 = System.currentTimeMillis()
        log.info(((time2 - time1) / 1000) + " Secs were taken in the persistance of data for configuration "+ executedConfig.id)
    }



    void insertAggAlertActions(Session session, Long executedConfigId, Long prevExecConfigId) {
        // Retrieve the alert actions using a helper method
        String sqlStatement = SignalQueryHelper.agg_alert_actions(executedConfigId, prevExecConfigId)
        List<Map<String, String>> alertIdAndActionIdList = session.createSQLQuery(sqlStatement)
                .list()
                .collect { row -> [col1: row[0].toString(), col2: row[1].toString(), col3: '1'] }
        // Log the start of the batch execution
        log.info("Batch execution of TABLE AGG_ALERT_ACTIONS started")

        // Insert the alert actions into the database
        String insertActionSingleAlertQuery = SignalQueryHelper.insert_agg_actions_sql()
        alertService.batchPersistForStringParameters(session, alertIdAndActionIdList, insertActionSingleAlertQuery, 3)

        // Log the end of the batch execution
        log.info("Batch execution of TABLE AGG_ALERT_ACTIONS ended")

        // Update the actions count in the alert
        log.info("Now updating the Actions Count in Alert")
        String actionCountSql = SignalQueryHelper.agg_alert_actions_count(executedConfigId)
        session.createSQLQuery(actionCountSql).executeUpdate()
        log.info("Action Count For Alert updated")
    }


    void addProductInCache(Long execConfigId) {
        List<String> productNameList = []
        productNameList = AggregateCaseAlert.createCriteria().list {
            projections {
                distinct("productName")
            }
            'executedAlertConfiguration' {
                eq("id", execConfigId)
                'eq'('selectedDatasource', 'pva')
            }
        } as List<String>
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.findByIdAndSelectedDatasource(execConfigId, Constants.DataSource.PVA)

        if (executedConfiguration?.getProductNameList()) {
            productNameList.addAll(executedConfiguration.getProductNameList())
        }
        if (productNameList.size() > 0) {
            cacheService.addInProductsUsedCache(productNameList as Set)
        }
    }

    void proposedDispositionRule(Configuration config, ExecutedConfiguration executedConfiguration, AggregateCaseAlert aggregateCaseAlert, List prevAlertList  ){

        List<ProductEventHistory> productEventHistoryList = dataObjectService.getPEHistoryListByConfigId(aggregateCaseAlert.getProductName(), aggregateCaseAlert.getPt(), config.getId())
        aggregateCaseAlert.proposedDisposition = aggregateCaseAlert.getPecImpNumHigh() < grailsApplication.config.dss.threshold ? "Non-Validated Observation" : "Validated Observation"
        aggregateCaseAlert.rationale = "New Event"
        //if no prev executions set based on threshold
        if (prevAlertList == null || prevAlertList.size() == 0) {

        } else {
            if (aggregateCaseAlert.getDisposition().getAbbreviation() == "NVO") {
                aggregateCaseAlert.proposedDisposition = "Non-Validated Observation"
                aggregateCaseAlert.rationale = "No Signal"
            }else if (aggregateCaseAlert.getDisposition().getAbbreviation() == "VO") {

                List<Long> validAlertIds = []
                for (Map<String, AggregateCaseAlert> alertMap : prevAlertList) {
                    validAlertIds.add((alertMap.get(aggregateCaseAlert.getProductName() + "-" + aggregateCaseAlert.getPt())).getId())
                }
                List<ValidatedSignalAggregateAlertMapping> validatedSignalMappings = AggregateCaseAlert.createCriteria().list {
                    or {
                        validAlertIds.collate(1000).each{
                            'in'("aggregateCaseAlert.id", it)
                        }
                    }
                } as List<AggregateCaseAlert>

                List<Disposition> validatedSignalDispositions = []
                for (ValidatedSignalAggregateAlertMapping alertMapping : validatedSignalMappings) {
                    validatedSignalDispositions.add(alertMapping.getValidatedSignal().getDisposition().getAbbreviation().toUpperCase())
                }
                if (validatedSignalDispositions.contains("CS") && validatedSignalDispositions.contains("RS")) {
                    aggregateCaseAlert.proposedDisposition = "Validated Observation"
                    aggregateCaseAlert.rationale = "Non-Validated Observation"
                }else {
                    if (validatedSignalDispositions.contains("RS")) {
                        aggregateCaseAlert.proposedDisposition = "Non-Validated Observation"
                        aggregateCaseAlert.rationale = "Refuted Signal"
                    }
                    if (validatedSignalDispositions.contains("CS")) {
                        aggregateCaseAlert.proposedDisposition = "Validated Observation"
                        aggregateCaseAlert.rationale = "Confirmed Signal"
                    }
                }
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = Exception)
    def persistValidatedSignalWithAggCaseAlert(Long executedConfigId, String name, Long configId) {
        List<AggregateCaseAlert> attachSignalAlertList = AggregateCaseAlert.createCriteria().list {
            eq("executedAlertConfiguration.id", executedConfigId)
            'disposition' {
                eq("validatedConfirmed", true)
            }
            createAlias("validatedSignals", "vs", JoinType.LEFT_OUTER_JOIN)
            isNull('vs.id')
        } as List<AggregateCaseAlert>

        List<Long> prevExecConfigIdList = ExecutedConfiguration.createCriteria().list {
            projections {
                property('id')
            }
            eq("name", name)
            'not' {
                'eq'("id", executedConfigId)
            }
            order("id", "desc")
            maxResults(1)
        }



        log.info("Now saving the signal across the PE.")

        if(attachSignalAlertList) {
            List<Map<String, String>> alertIdAndSignalIdList = getAggAlertIdAndSignalIdForBusinessConfig(executedConfigId, configId, attachSignalAlertList)
            List<Map<String, String>> autoAlertIdAndSignalIdList = getAutoAggAlertIdAndSignalIdForBusinessConfig(executedConfigId, attachSignalAlertList)

            List<String> productIdAndPtCodeList = attachSignalAlertList.collect {
                "(" + it.productId + "," + it.ptCode + ",'" + it.soc + "','" + it.pt?.replace("'", "''") + "')"
            }
            Session session = sessionFactory.currentSession

            if (prevExecConfigIdList.size() > 0) {
                log.info("Executing SQL query for signals for previous executions")
                def sql_statement = SignalQueryHelper.signal_alert_ids(productIdAndPtCodeList.join(","), executedConfigId, prevExecConfigIdList[0])
                log.info(sql_statement)
                def sqlQuery = session.createSQLQuery(sql_statement)
                sqlQuery.list().each { row -> alertIdAndSignalIdList.add([col2: row[0].toString(), col1: row[1].toString(), col3: '1', autoRouted: '0'])
                }
            }
            if(autoAlertIdAndSignalIdList){
                autoAlertIdAndSignalIdList.each{
                    if(it.col1 != null && it.col2 != null) {
                        it.col2?.each { signal ->
                            if(alertIdAndSignalIdList.contains([col2: it.col1, col1: signal.toString(), col3: '1', autoRouted: '0'])){
                                alertIdAndSignalIdList.remove([col2: it.col1, col1: signal.toString(), col3: '1', autoRouted: '0'])
                            }
                            alertIdAndSignalIdList.add([col2: it.col1, col1: signal.toString(), col3: '0', autoRouted: '1'])
                        }
                    }
                }
            }
            alertIdAndSignalIdList = alertIdAndSignalIdList?.unique {
                [it.col2, it.col1]
            }
            log.info("Batch execution of TABLE validated_agg_alerts started")
            def insertValidatedAggAlertQuery = "INSERT INTO validated_agg_alerts(VALIDATED_SIGNAL_ID,AGG_ALERT_ID,IS_CARRY_FORWARD, DATE_CREATED,AUTO_ROUTED) VALUES(?,?,?,?,?)"
            session.doWork(new Work() {
                public void execute(Connection connection) throws SQLException {
                    PreparedStatement preparedStatement = connection.prepareStatement(insertValidatedAggAlertQuery)
                    def batchSize = Holders.config.signal.batch.size
                    int count = 0
                    try {
                        alertIdAndSignalIdList?.each {
                            if (it.col1 != null && it.col2 != null) {
                                preparedStatement.setLong(1, it.col1.toLong())
                                preparedStatement.setLong(2, it.col2.toLong())
                                preparedStatement.setBoolean(3, it.col3.toBoolean())
                                preparedStatement.setTimestamp(4, new Timestamp(System.currentTimeMillis()))
                                preparedStatement.setBoolean(5, it.autoRouted.toBoolean())
                                preparedStatement.addBatch()
                                count += 1
                                if (count == batchSize) {
                                    preparedStatement.executeBatch()
                                    count = 0
                                }
                            }
                        }
                        preparedStatement.executeBatch()
                    } catch (Exception e) {
                        e.printStackTrace()
                    } finally {
                        log.info("Batch execution of TABLE validated_agg_alerts ended")
                        preparedStatement.close()
                        session.flush()
                        session.clear()
                    }
                }
            })
        }
        log.info("Signal are saved across the system.")
    }


    List<Map<String, String>> getAggAlertIdAndSignalIdForBusinessConfig(Long executedConfigId, Long configId, List<AggregateCaseAlert> attachSignalAlertList) {
        List<Map> signalAlertMap = dataObjectService.getAggSignalAlertMap(executedConfigId)
        List<Map<String, String>> alertIdAndSignalIdList = []
        List<Activity> signalActivityList = []
        if (signalAlertMap) {
            Disposition defaultSignalDisposition = attachSignalAlertList[0].alertConfiguration.getWorkflowGroup()?.defaultSignalDisposition
            List<String> validatedDateDispositions=Holders.config.alert.validatedDateDispositions
            boolean isValidatedDate=validatedDateDispositions.contains(defaultSignalDisposition.value)
            ValidatedSignal validatedSignal = validatedSignalService.createSignalForBusinessConfiguration(signalAlertMap[0].signalName, signalAlertMap[0].alert, Constants.AlertConfigType.AGGREGATE_CASE_ALERT, defaultSignalDisposition, signalAlertMap[0].signalId ? signalAlertMap[0].signalId as Long : null, isValidatedDate)
            log.info("validatedSignal-:"+validatedSignal)
            signalAlertMap.each { Map map ->
                Long alertId = attachSignalAlertList.find {
                    map.alert?.id == it?.id
                }?.id
                if (alertId) {
                    alertIdAndSignalIdList.add([col2: alertId.toString(), col1: validatedSignal.id.toString(), col3: '0', autoRouted: '0'])
                    signalActivityList.add(activityService.createActivityForSignalBusinessConfigAgg(map.alert, validatedSignal.name, map.alert.name))

                }
            }
            dataObjectService.clearAggSignalAlertMap(executedConfigId)
            alertService.persistActivitesForSignal(validatedSignal.id, signalActivityList)
        }
        alertIdAndSignalIdList
    }

    List<Map<String, String>> getAutoAggAlertIdAndSignalIdForBusinessConfig(Long executedConfigId, List<AggregateCaseAlert> attachSignalAlertList) {
        Map<Long,List<Map>> autoAlertIdAndSignalIdList = dataObjectService.getAlertIdSignalListMap(executedConfigId)
        List<Map> autoAlertSignalIdList = []
        Map<Long,List[]> signalActivityMap = [:]
        if (autoAlertIdAndSignalIdList) {
            autoAlertIdAndSignalIdList.each { ruleId, idMap ->
                boolean associateMultipleSignal
                String ruleJSON = RuleInformation.get(ruleId)?.ruleJSON
                JsonSlurper jsonSlurper = new JsonSlurper()
                Map ruleMap = jsonSlurper.parseText(ruleJSON)
                List expressionList = ruleMap?.all?.containerGroups
                associateMultipleSignal = Boolean.parseBoolean(expressionList[0]?.expressions[0]?.assMultSignal)
                Map<Long, List> alertIdAllSignalListMap = [:]
                idMap.each { map ->
                    Long alertId = attachSignalAlertList.find {
                        map.alert.id == it.id
                    }?.id
                    if(alertId) {
                        if (alertIdAllSignalListMap.get(alertId)) {
                            List tempList = alertIdAllSignalListMap.get(alertId)
                            tempList += map.signalList
                            alertIdAllSignalListMap.put(alertId, tempList)
                        } else {
                            alertIdAllSignalListMap.put(alertId, map.signalList)
                        }
                    }
                }
                alertIdAllSignalListMap.each { alertId, signalList ->
                    if (!associateMultipleSignal && signalList.size() > 1) {
                        List signalListLastDisp = signalList.sort { a, b -> ValidatedSignal.get(b).lastDispChange <=> ValidatedSignal.get(a).lastDispChange }
                        signalList = signalListLastDisp[0]
                    }
                    autoAlertSignalIdList.add([col1: alertId.toString(), col2: signalList])
                    AggregateCaseAlert aggregateCaseAlert = AggregateCaseAlert.get(alertId)
                    signalList?.each { signalId ->
                        ValidatedSignal validatedSignal = ValidatedSignal.get(signalId)
                        if(validatedSignal) {
                            Activity signalActivity = activityService.createActivityForSignalBusinessConfigAgg(aggregateCaseAlert, validatedSignal.name, aggregateCaseAlert.name)
                            if (signalActivityMap.get(signalId)) {
                                signalActivityMap.get(signalId)?.add(signalActivity)
                            } else {
                                signalActivityMap.put(signalId, [])
                                signalActivityMap.get(signalId)?.add(signalActivity)
                            }
                        }
                    }
                }
            }
            dataObjectService.clearAggSignalAlertMapForSignalState(executedConfigId)
            dataObjectService.clearAlertIdSignalListMap(executedConfigId)
            alertService.persistActivitiesForAutoRouteSignal(signalActivityMap)
        }
        autoAlertSignalIdList
    }
    /**
     * Updates the aggregate alert objects based on the pec history.
     * @param executedConfig
     * @return
     */
    def persistAggregateAlerts(Long executedConfigId) {
        log.info("Now saving the alert states across the system.")
        ExecutedConfiguration executedConfig = ExecutedConfiguration.get(executedConfigId)
        def propertiesMapList = dataObjectService.getBusinessConfigPropertiesMapList(executedConfig?.id)
        def size = Holders.config.signal.batch.size
        Sql sql = null
        try {
            sql = new Sql(dataSource)
            sql.withBatch(size, "update AGG_ALERT set priority_id = :val0, assigned_to_id = :val3, assigned_to_group_id = :val7 " +
                    "where product_name = :val4 and pt = :val5 and exec_configuration_id = :val6".toString(), { preparedStatement ->
                propertiesMapList.each { def obj ->
                    preparedStatement.addBatch(val0: obj.priority.id, val3: obj.assignedTo?.id, val4: obj.productName, val5: obj.eventName, val6: executedConfig.id, val7: obj.assignedToGroup?.id)
                }
            })
        } catch (Exception e) {
            log.error(e.printStackTrace())
        } finally {
            sql?.close()
        }
        //Fetch all the alerts.
        log.info("Alert states across the system are saved.")
    }

    void saveDispositionAlertCaseHistory(AggregateCaseAlert alert, ProductEventHistory caseHistory){
        alert.justification = caseHistory.justification
        alert.dispLastChange = Objects.nonNull(caseHistory.createdTimestamp) ? caseHistory.createdTimestamp : caseHistory.lastUpdated
        String userName = caseHistory.modifiedBy? (caseHistory.modifiedBy == Constants.Commons.SYSTEM ? Constants.SYSTEM_USER: caseHistory.modifiedBy)
                : caseHistory.createdBy == Constants.Commons.SYSTEM ? Constants.SYSTEM_USER: caseHistory.createdBy
        alert.dispPerformedBy = cacheService.getUserByUserNameIlike(userName)?.fullName?:userName
    }

    @Transactional
    def setWorkflowMgmtStates(AggregateCaseAlert aca, Configuration config, ExecutedConfiguration execConfig, AggregateCaseAlert prevAca) {

        ProductEventHistory.withTransaction {
            ProductEventHistory existingProductEventHistory = dataObjectService.getPEHistoryByConfigId(aca.productName, aca.productId as Long,aca.ptCode as Long, aca.pt, config.id)
            ProductEventHistory existingLatestDispositionPEHistory = dataObjectService.getLatestDispositionPEHistory(aca.productName, aca.productId as Long,aca.ptCode as Long, aca.pt, config.id)
            try {
                if(existingLatestDispositionPEHistory){
                    saveDispositionAlertCaseHistory(aca,existingLatestDispositionPEHistory)
                }
                if (existingProductEventHistory && (existingProductEventHistory.alertName == config.name)) {
                    //In order to pass the lazy initialization exception we are handling exception
                    //TODO: Need to have a fix for this.
                    aca.priority = existingProductEventHistory.priority ?: config.priority
                    aca.disposition = existingProductEventHistory.disposition
                    if(aca.disposition.reviewCompleted || aca.disposition.closed){
                        if(execConfig.isAutoAssignedTo){
                            if (execConfig.assignedTo) {
                                aca.assignedTo = execConfig.assignedTo
                            } else {
                                aca.assignedToGroup = execConfig.assignedToGroup
                            }
                        } else {
                            if (config.assignedTo) {
                                aca.assignedTo = config.assignedTo
                            } else {
                                aca.assignedToGroup = config.assignedToGroup
                            }
                        }
                    } else {
                        if(existingProductEventHistory.assignedTo){
                            aca.assignedTo = existingProductEventHistory.assignedTo
                        } else if(existingProductEventHistory.assignedToGroup){
                            aca.assignedToGroup = existingProductEventHistory.assignedToGroup
                        } else {
                            if (config.assignedTo) {
                                aca.assignedTo = config.assignedTo
                            } else {
                                aca.assignedToGroup =  config.assignedToGroup
                            }
                        }
                    }
                    aca.dueDate = existingProductEventHistory.dueDate
                } else {
                    setDefaultWorkflowStates(aca, config, execConfig , prevAca)
                }
            } catch (Throwable th) {
                log.error("")
                log.error("The exception occured which fetching the data from the history")
                log.error("Setting up default values.")
                log.error("")
                log.error(th.getMessage())
                setDefaultWorkflowStates(aca, config, execConfig, prevAca)
            }
        }
    }

    private setDefaultWorkflowStates(AggregateCaseAlert aca, Configuration config, ExecutedConfiguration execConfig, AggregateCaseAlert prevAca) {
        if (aca && config) {
            aca.priority = config.priority
            if(execConfig.isAutoAssignedTo){
                if (execConfig.assignedTo) {
                    aca.assignedTo = execConfig.assignedTo
                } else {
                    aca.assignedToGroup = execConfig.assignedToGroup
                }
            } else {
                if (config.assignedTo) {
                    aca.assignedTo = config.assignedTo
                } else {
                    aca.assignedToGroup = config.assignedToGroup
                }
            }
            if (aca.disposition.reviewCompleted || aca.disposition.closed) {
                aca.dueDate = null
            } else if (prevAca && aca.disposition == config?.getWorkflowGroup()?.defaultQuantDisposition) {
                aca.dueDate = prevAca.dueDate
            } else {
                calcDueDate(aca, aca.priority, aca.disposition, false,
                        cacheService.getDispositionConfigsByPriority(aca.priority.id))
            }
        }
    }

    //This will update the state At Alert Level
    def updateAggregateAlertStates(def alert, Map map) {
        switch (map.change) {
            case Constants.HistoryType.DISPOSITION:
                alert.disposition = map.disposition
                break
            case Constants.HistoryType.ASSIGNED_TO:
                alert.assignedTo = map.assignedTo
                alert.assignedToGroup = map.assignedToGroup
                break
            case Constants.HistoryType.PRIORITY:
                alert.priority = map.priority
                calcDueDate(alert, alert.priority, alert.disposition, false,
                        cacheService.getDispositionConfigsByPriority(alert.priority.id))
                map.dueDate = alert.dueDate
                break
        }
        alert.save(flush: true)
    }

    def list() {
        AggregateCaseAlert.findAll()
    }

    //Fetching Configuration Object from ExecutedConfiguration Id
    Long getAlertConfigObject(ExecutedConfiguration executedConfiguration) {
        executedConfiguration.configId
    }

    //Return List of JSON Objects with Tag Names
    def getAlertTagNames(def alert) {
        List tagNames = alert?.alertTags?.name
        List tagList = []
        tagNames.each { tagName ->
            Map tagObj = ["name": tagName]
            tagList.add(tagObj)
        }
        JsonOutput.toJson(tagList)
    }

    def caseDrillDown(String type, String typeFlag, Long id, BigInteger productId, Integer ptCode, String selectedDataSource, Boolean groupBySMQ = false,
                      def aggregateCaseAlert = null, Boolean isEventGroup = false,Integer noOfCases = null, ExecutedConfiguration  newCaseSeriesExConfig = null) {

        Sql sql = null
        Boolean isAddCase = 0
        String pt = aggregateCaseAlert == null ? "" : aggregateCaseAlert.pt
        log.info("Case Drill down flow")
        log.info "id is : " + id
        log.info "type is : " + type
        log.info "typeflag is : " + typeFlag
        log.info "selectedDataSource is : " + selectedDataSource
        log.info "groupBySMQ is : " + groupBySMQ
        log.info "pt is : " + pt

        List<Map> singleAlertCaseList = []
        Map attributeMap = Holders.config.pvsignal.attributeMap.clone() as Map
        if (Holders.config.custom.qualitative.fields.enabled) {
            attributeMap.putAll(Holders.config.pvsignal.attributeMapCustom as Map)
            attributeMap.remove("patInfoPatientAgeYears")
        } else {
            attributeMap.remove("casePatInfoPvrUdNumber2")
        }

        try {
            if (id && type && typeFlag) {
                sql = new Sql(signalDataSourceService.getReportConnection(selectedDataSource))
                //Added code for pii policy
                if (StringUtils.isNotBlank(selectedDataSource) && StringUtils.equalsIgnoreCase('pva', selectedDataSource)) {
                    DbUtil.executePIIProcCall(sql, Constants.PII_OWNER, Constants.PII_ENCRYPTION_KEY)
                }
                //end code for pii policy
                int termCodeNum = 0
                String termCode = (groupBySMQ && !isEventGroup) ? SignalUtil.getTermScopeFromSMQ(pt) : null
                if (termCode && termCode == Constants.SMQType.BROAD) {
                    termCodeNum = 1
                } else if (termCode && termCode == Constants.SMQType.NARROW) {
                    termCodeNum = 2
                }
                log.info("TermCode is : " + termCodeNum)

                int isSmq = 0
                if (groupBySMQ) {
                    isSmq = 1
                }

                def termCodeSmq = null
                if (groupBySMQ && isEventGroup) {
                    isSmq = 0
                    termCodeSmq = null
                } else if (termCodeNum == 0) {
                    termCodeSmq = ''
                } else if (termCodeNum > 0) {
                    termCodeSmq = termCodeNum
                }

                log.info "Start Db call to get case drill down details"
                if( selectedDataSource == "pva" && Holders.config.custom.qualitative.fields.enabled) {
                    String reportSql = 'SELECT CASE_ID  "masterCaseId",CASE_NUM  "masterCaseNum",VERSION_NUM "masterVersionNum",' +
                            'TEXT_1  "CsHcpFlag",TEXT_2 "masterRptTypeId",DATE_1 "masterInitReptDate",TEXT_3 "ciTxtDateReceiptInitial",' +
                            'TEXT_44 "cifInitialDateLocked",DATE_2 "masterFollowupDate",CLOB_1  "masterPrefTermAll",TEXT_4  "assessListedness",' +
                            'CLOB_2 "masterSuspProdAgg",CLOB_3 "ccconcomitProd2All",TEXT_5 "assessOutcome",TEXT_6 "productFamilyId",' +
                            'NUMBER_1 "masterFupNum",TEXT_7 "masterFlagSt",TEXT_8 "productProductId",TEXT_9 "productGenericName",' +
                            'TEXT_10 "masterPrimEvtPrefTerm",TEXT_11 "masterPrimProdName",TEXT_12  "eventEvtOutcomeId",TEXT_13 "assessSeriousness",' +
                            'TEXT_14 "csiderived_country",TEXT_15 "patInfoAgeGroupId",TEXT_16 "patInfoGenderId",TEXT_17 "prodDrugsPosRechallenge",' +
                            'DATE_3 "masterDateLocked",TEXT_18 "cifTxtDateLocked",TEXT_19 "masterFatalFlag",TEXT_20  "eventPrefTerm",' +
                            'NUMBER_2  "cpiProdIdResolved",CLOB_4 "narrativeNarrative",CLOB_5  "ccAePt",TEXT_21 "ciFlagEligibleLocalExpdtd",' +
                            'NUMBER_3 "caseMasterPvrUdText12",TEXT_22 "vwcsiSponsorStudyNumber",TEXT_23 "vwstudyClassificationId",TEXT_24 "vwcpai1FlagCompounded",' +
                            'TEXT_25 "csisender_organization",CLOB_6 "cdrClobAeAllUdUdClob1",TEXT_26 "deviceComboProduct",TEXT_27 "malfunctionDevices",' +
                            'NUMBER_4 "casePatInfoPvrUdNumber2",TEXT_28 "caseProdDrugsPvrUdText20",TEXT_29 "caseProdDrugsPvrUdNumber10",' +
                            'TEXT_30  "caseProdDrugsPvrUdNumber11",NUMBER_5 "flagMasterCase",TEXT_31 "vwstudyProtocolNum",TEXT_32 "PreAndastudyStudyNum",' +
                            'NUMBER_6 "dvProdEventTimeOnsetDays",TEXT_33 "masterSusar",TEXT_34 "ceSerUnlRel",TEXT_35 "productIndCoddorReptd",' +
                            'CLOB_7 "productLotNoAllcs",CLOB_8 "masterPrefTermSurAll",CLOB_9 "productStartStopDateAllcs",' +
                            'CLOB_10 "productDoseDetailAllcs",TEXT_36 "masterCharactersticAllcs",CLOB_11 "cprmConditionAll",CLOB_12 "ccMedHistDrugAll",' +
                            'TEXT_37  "ccCoddRptdCauseDeathAll",TEXT_38 "caseChangesConcat",CLOB_13 "masterConcomitProdList",' +
                            'CLOB_14 "dciPaiAll",CLOB_15 "masterSuspProdList",CLOB_16 "dciPrimSuspectPai",TEXT_39 "masterCreateTime", TEXT_40  "patInfoPatDobPartial",' +
                            'TEXT_41 "eventPrimaryOnsetDatePartial", TEXT_42 "masterPregnancyFlag",TEXT_43 "masterHcpFlag",CLOB_17  "masterPrefTermList", TEXT_45 "ciFlagUnblind", TEXT_46 "studyCodeBroken",  TEXT_47 "productProductName", NUMBER_6 "studyCodeUnBroken" FROM GTT_CLL_REPORT_DATA'
                    sql.call("{call pkg_pvs_case_drill_down.p_case_drill_down(?,?,?,?,?,?,?,?,?,?)}",
                            [id, productId, ptCode, termCodeSmq, (type.toUpperCase() == "NEW") ? 1 : 0, typeFlag, isSmq,noOfCases,isAddCase,
                             Sql.resultSet(oracle.jdbc.driver.OracleTypes.CURSOR)]) { case_info ->
                        log.info("Data is present in cursor , start mapping with object")

                    }
                    sql.eachRow(reportSql) { GroovyResultSet resultSet ->

                        Map map = [:]
                        resultSet.toRowResult().eachWithIndex { it, i ->

                            def value = ""
                            if (it.value instanceof Clob) {
                                //Handle Clob data
                                value = it.value.characterStream.text
                            } else {
                                value = it.value
                            }

                            map.put(cacheService.getRptFieldIndexCache(it.key), value)

                        }
                        singleAlertCaseList.add(map)
                    }

                }
                else if(selectedDataSource == "pva"){
                    String reportSql = 'SELECT CASE_ID "masterCaseId",CASE_NUM "masterCaseNum",VERSION_NUM "masterVersionNum",' +
                            'TEXT_1 "CsHcpFlag",TEXT_2 "masterRptTypeId",DATE_1 "masterInitReptDate",TEXT_3 "ciTxtDateReceiptInitial",' +
                            'DATE_2 "masterFollowupDate",CLOB_1 "masterPrefTermAll",TEXT_4 "assessListedness",CLOB_2 "masterSuspProdAgg",' +
                            'CLOB_3 "masterConcomitProdList",TEXT_5 "assessOutcome",TEXT_6 "productFamilyId",NUMBER_1 "masterFupNum",' +
                            'TEXT_7 "masterFlagSt",TEXT_8 "productProductId",TEXT_9 "masterPrimEvtPrefTerm",TEXT_10 "masterPrimProdName",' +
                            'TEXT_11 "eventEvtOutcomeId",TEXT_12 "assessSeriousness",TEXT_13 "masterCountryId",TEXT_14 "patInfoAgeGroupId",' +
                            'TEXT_15 "patInfoGenderId",TEXT_16 "prodDrugsPosRechallenge",DATE_3 "masterDateLocked",TEXT_17 "cifTxtDateLocked",' +
                            'TEXT_18 "masterFatalFlag",TEXT_19 "deviceComboProduct",TEXT_20 "malfunctionDevices",TEXT_21 "eventPrefTerm",' +
                            'NUMBER_2 "cpiProdIdResolved",CLOB_4 "narrativeNarrative",CLOB_5 "ccAePt",TEXT_22 "ciFlagEligibleLocalExpdtd",' +
                            'TEXT_23 "caseMasterPvrUdText12",TEXT_24 "crepoud_text_11",TEXT_25 "crepoud_text_12",TEXT_26 "vwcpai1FlagCompounded",' +
                            'TEXT_27 "csisender_organization",TEXT_28 "cdrClobAeAllUdUdClob1",NUMBER_3 "patInfoPatientAgeYears",TEXT_29 "caseProdDrugsPvrUdText20",' +
                            'TEXT_30 "caseProdDrugsPvrUdNumber10",TEXT_31 "caseProdDrugsPvrUdNumber11",NUMBER_4 "flagMasterCase",' +
                            'TEXT_32 "vwstudyProtocolNum",TEXT_33 "PreAndastudyStudyNum",NUMBER_5 "dvProdEventTimeOnsetDays",TEXT_34 "masterSusar",' +
                            'TEXT_35 "ceSerUnlRel",TEXT_36 "productIndCoddorReptd",CLOB_6 "productLotNoAllcs",CLOB_7 "masterPrefTermSurAll",' +
                            'CLOB_8 "productStartStopDateAllcs",CLOB_9 "productDoseDetailAllcs",CLOB_10 "masterCharactersticAllcs",' +
                            'CLOB_11 "cprmConditionAll",CLOB_12 "ccMedHistDrugAll",TEXT_37 "ccCoddRptdCauseDeathAll",TEXT_38 "caseChangesConcat",' +
                            'TEXT_39 "masterCreateTime",TEXT_40 "patInfoPatDobPartial",TEXT_41 "eventPrimaryOnsetDatePartial",' +
                            'TEXT_42 "masterPregnancyFlag",TEXT_43 "masterHcpFlag",CLOB_13 "masterSuspProdList",CLOB_14 "masterPrefTermList", TEXT_44 "cifInitialDateLocked" , TEXT_45 "ciFlagUnblind", TEXT_46 "studyCodeBroken", TEXT_47 "productProductName", NUMBER_6 "studyCodeUnBroken", TEXT_48 "analytic.caseInformation.cmflagcopackproduct"  FROM GTT_CLL_REPORT_DATA'
                    sql.call("{call pkg_pvs_case_drill_down.p_case_drill_down(?,?,?,?,?,?,?,?,?,?)}",
                            [id, productId, ptCode, termCodeSmq, (type.toUpperCase() == "NEW") ? 1 : 0, typeFlag, isSmq,noOfCases,isAddCase,
                             Sql.resultSet(oracle.jdbc.driver.OracleTypes.CURSOR)]) { case_info ->
                        log.info("Data is present in cursor , start mapping with object")

                    }
                    sql.eachRow(reportSql) { GroovyResultSet resultSet ->
                        Map map = [:]
                        resultSet.toRowResult().eachWithIndex { it, i ->

                            def value = ""
                            if (it.value instanceof Clob) {
                                //Handle Clob data
                                value = it.value.characterStream.text
                            } else {
                                value = it.value
                            }

                            map.put(cacheService.getRptFieldIndexCache(it.key), value)

                        }
                        singleAlertCaseList.add(map)
                    }
                } else {
                    String reportSql
                    if(selectedDataSource == "jader"){
                         reportSql = 'SELECT CASE_ID "masterCaseId", CASE_NUM "masterCaseNum", VERSION_NUM "masterVersionNum", TEXT_1 "CsHcpFlag", ' +
                                'TEXT_2 "masterRptTypeId", DATE_1 "masterInitReptDate", TEXT_3 "ciTxtDateReceiptInitial", DATE_2 "masterFollowupDate", ' +
                                'CLOB_1 "masterPrefTermAll", CLOB_2 "masterSuspProdAgg",CLOB_3 "masterConcomitProdList" ,CLOB_4 "ccconcomitProd2All", TEXT_4 "assessOutcome", TEXT_5 "productFamilyId" ,NUMBER_1 "masterFupNum",' +
                                'TEXT_6 "masterFlagSt", TEXT_7 "productProductId" ,TEXT_8 "masterPrimEvtPrefTerm" ,TEXT_9 "masterPrimProdName",TEXT_10 "eventEvtOutcomeId", TEXT_11 "assessSeriousness", TEXT_12 "masterCountryId", TEXT_13 "patInfoAgeGroupId", TEXT_14 "patInfoGenderId", TEXT_15 "prodDrugsPosRechallenge", DATE_3 "masterDateLocked", TEXT_16 "cifTxtDateLocked", TEXT_17 "masterFatalFlag", TEXT_18 "eventPrefTerm", ' +
                                'NUMBER_2 "cpiProdIdResolved", CLOB_5 "narrativeNarrative", CLOB_6 "ccAePt", CLOB_8 "masterSuspProdList", CLOB_9 "cprmConditionAll", NUMBER_3 "patInfoPatientAgeYears", NUMBER_4 "flagMasterCase", TEXT_19 "masterSusar", TEXT_20 "masterCloseNotes", TEXT_21 "ceSerUnlRel", TEXT_22 "productIndCoddorReptd", TEXT_23 "productLotNoAllcs", CLOB_7 "masterPrefTermSurAll", TEXT_24 "productStartStopDateAllcs", TEXT_25 "productDoseDetailAllcs", TEXT_26 "caseChangesConcat" ,TEXT_27 "cifInitialDateLocked", ' +
                                'TEXT_28 "riskCategory", TEXT_29 "reportersReporterType", TEXT_30 "eventPrimaryOnsetDatePartial" FROM GTT_CLL_REPORT_DATA'
                    }else{
                        String tableName = ' FROM GTT_CLL_REPORT_DATA '
                        reportSql = 'SELECT CASE_ID "masterCaseId", CASE_NUM "masterCaseNum", VERSION_NUM "masterVersionNum", TEXT_1 "CsHcpFlag", ' +
                                'TEXT_2 "masterRptTypeId", DATE_1 "masterInitReptDate", TEXT_3 "ciTxtDateReceiptInitial", DATE_2 "masterFollowupDate", ' +
                                'CLOB_1 "masterPrefTermAll", CLOB_2 "masterSuspProdAgg",CLOB_3 "masterConcomitProdList" ,CLOB_4 "ccconcomitProd2All", TEXT_4 "assessOutcome", TEXT_5 "productFamilyId" ,NUMBER_1 "masterFupNum",' +
                                'TEXT_6 "masterFlagSt", TEXT_7 "productProductId" ,TEXT_8 "masterPrimEvtPrefTerm" ,TEXT_9 "masterPrimProdName",TEXT_10 "eventEvtOutcomeId", TEXT_11 "assessSeriousness", TEXT_12 "masterCountryId", TEXT_13 "patInfoAgeGroupId", TEXT_14 "patInfoGenderId", TEXT_15 "prodDrugsPosRechallenge", DATE_3 "masterDateLocked", TEXT_16 "cifTxtDateLocked", TEXT_17 "masterFatalFlag", TEXT_18 "eventPrefTerm", ' +
                                'NUMBER_2 "cpiProdIdResolved", CLOB_5 "narrativeNarrative", CLOB_6 "ccAePt", NUMBER_3 "patInfoPatientAgeYears", NUMBER_4 "flagMasterCase", TEXT_19 "masterSusar", TEXT_20 "masterCloseNotes", TEXT_21 "ceSerUnlRel", TEXT_22 "productIndCoddorReptd", TEXT_23 "productLotNoAllcs", CLOB_7 "masterPrefTermSurAll", TEXT_24 "productStartStopDateAllcs", TEXT_25 "productDoseDetailAllcs", TEXT_26 "caseChangesConcat" ,TEXT_27 "cifInitialDateLocked" '
                        if(selectedDataSource == "vaers"){
                            reportSql += ',NUMBER_5 "dvProdEventTimeOnsetDays"'
                        }
                        reportSql += tableName
                    }

                    sql.call("{call pkg_pvs_case_drill_down.p_case_drill_down(?,?,?,?,?,?,?,?,?)}",
                            [id, productId, ptCode, termCodeSmq, (type.toUpperCase() == "NEW") ? 1 : 0, typeFlag, isSmq,noOfCases,
                             Sql.resultSet(oracle.jdbc.driver.OracleTypes.CURSOR)]) { case_info ->
                        log.info("Data is present in cursor , start mapping with object")

                    }
                    sql.eachRow(reportSql) { GroovyResultSet resultSet ->

                        Map map = [:]
                        resultSet.toRowResult().eachWithIndex { it, i ->

                            def value = ""
                            if (it.value instanceof Clob) {
                                //Handle Clob data
                                value = it.value.characterStream.text
                            } else {
                                value = it.value
                            }

                            map.put(cacheService.getRptFieldIndexCache(it.key), value)

                        }
                        singleAlertCaseList.add(map)
                    }
                }

                    log.info("End of Db call and data in mapped to object")
                }
        } catch (Exception e) {
            newCaseSeriesExConfig.isDeleted = true
            newCaseSeriesExConfig.save(flush:true, failOnError:true)
            log.error(e.printStackTrace())
        } finally {
            sql?.close()
        }

        singleAlertCaseList
    }

    def listSelectedAlerts(String alerts, def domainName) {
        String[] alertList = alerts.split(",")
        alertList.collect {
            domainName.findById(Integer.valueOf(it))
        }
    }

    def getPreviousPeriodAggregateAlertListForProductSummary(Integer productId, periodStartDate, periodEndDate, dispositionValue, dataSource, eventList) {
        def aggregateAlertList = AggregateCaseAlert.withCriteria {
            eq('productId', productId)
            eq('periodStartDate', periodStartDate)
            eq('periodEndDate', periodEndDate)
            'alertConfiguration' {
                eq('selectedDatasource', dataSource)
            }
            if (dispositionValue.size() > 0) {
                'in'('disposition', dispositionValue)
            }
            or {
                eventList.collate(1000).each {
                    'in'("pt", it)
                }
            }
            order('lastUpdated', 'desc')
        }
        aggregateAlertList = aggregateAlertList?.unique {
            [it.productId, it.ptCode, it.alertConfiguration]
        }
        aggregateAlertList
    }

    def saveRequestByForAggCaseAlert(params) {
        AggregateCaseAlert.findAllByProductNameAndPt(params.productName, params.eventName).each {
            it.requestedBy = params.requestedBy
            CRUDService.saveWithoutAuditLog(it)
        }
    }

    def savePreviousExecutedAggAlertWithSignal() {

        Session session = sessionFactory.currentSession

        List signalIdProductIdPtCodeList = []
        String sql_statement = SignalQueryHelper.aggregateCaseAlert_attached_signals()
        SQLQuery sqlQuery = session.createSQLQuery(sql_statement)
        sqlQuery.list().each { row ->
            signalIdProductIdPtCodeList.add([signalId: row[0].toString(), productId: row[1].toString(), ptCode: row[2].toString()])
        }

        session.flush()
        session.clear()

        if (signalIdProductIdPtCodeList.size() > 0) {

            def productIdAndPtCodeList = signalIdProductIdPtCodeList.collect {
                "(" + it.productId + "," + it.ptCode + ")"
            }

            List alertSignalToAddList = []


            sql_statement = SignalQueryHelper.aggregateCaseAlert_signals_to_add(productIdAndPtCodeList.join(","))
            sqlQuery = session.createSQLQuery(sql_statement)
            sqlQuery.list().each { row ->
                alertSignalToAddList.add([alertId: row[0].toString(), productId: row[1].toString(), ptCode: row[2].toString()])
            }
            session.flush()
            session.clear()

            if (alertSignalToAddList.size() > 0) {
                List alertIdAndSignalIdList = []
                signalIdProductIdPtCodeList.each { signalIdProductIdPtCode ->
                    List<String> alertIdList = alertSignalToAddList.findAll {
                        signalIdProductIdPtCode.productId == it.productId && signalIdProductIdPtCode.ptCode == it.ptCode
                    }?.collect {
                        it.alertId
                    }
                    alertIdList.unique { it }
                    if (alertIdList.size() > 0) {
                        alertIdList.each {
                            alertIdAndSignalIdList.add([alertId: it, signalId: signalIdProductIdPtCode.signalId])
                        }
                    }
                }

                def insertValidatedAggAlertQuery = "INSERT INTO validated_agg_alerts(VALIDATED_SIGNAL_ID,AGG_ALERT_ID) VALUES(?,?)"
                session.doWork(new Work() {
                    public void execute(Connection connection) throws SQLException {
                        PreparedStatement preparedStatement = connection.prepareStatement(insertValidatedAggAlertQuery)
                        def batchSize = Holders.config.signal.batch.size
                        int count = 0
                        try {
                            alertIdAndSignalIdList.each {
                                preparedStatement.setString(1, it.signalId)
                                preparedStatement.setString(2, it.alertId)
                                preparedStatement.addBatch()
                                count += 1
                                if (count == batchSize) {
                                    preparedStatement.executeBatch()
                                    count = 0
                                }
                            }
                            preparedStatement.executeBatch()
                        } catch (Exception e) {
                            e.printStackTrace()
                        } finally {
                            preparedStatement.close()
                            session.flush()
                            session.clear()
                        }
                    }
                })
            }
        }
    }

    def checkProductNameListExistsForFAERS(String prodDictSelection, def productNameList) {

        if (prodDictSelection != "1" && prodDictSelection != "3") {
            return false
        } else if (prodDictSelection == "3") {
            return productBasedSecurityService.checkProductExistsForFAERS(productNameList)
        } else {
            return productBasedSecurityService.checkIngredientExistsForFAERS(productNameList)
        }
        return false
    }

    Map createPEHistoryMapForAssignedToChange(def aggregateCaseAlert, Long configId,Boolean isArchived = false) {
        //Create the peHistory.
        Map peHistoryMap = [
                "justification"   : '',
                "change"          : Constants.HistoryType.ASSIGNED_TO,
        ]
        peHistoryMap = createBasePEHistoryMap(aggregateCaseAlert,peHistoryMap, isArchived)
        peHistoryMap
    }

    void sendMailForAssignedToChange(List emailDataList, def aggregateCaseAlert, Boolean isArchived = false) {
        List sentEmailList = []
        String alertLink = createHref("aggregateCaseAlert", "details", ["callingScreen": Constants.Commons.REVIEW, "configId": aggregateCaseAlert.executedAlertConfiguration.id, "isArchived": isArchived])
        emailDataList.each { Map emailMap ->
            if (!sentEmailList.count { it == emailMap.user.email }) {
                emailNotificationService.mailHandlerForAssignedToACA(emailMap.user, aggregateCaseAlert, alertLink, emailMap.emailMessage)
                sentEmailList << emailMap.user.email
            }
        }

    }

    @Transactional
    Map changeDisposition(List<Long> aggCaseAlertIdList, Disposition newDisposition,
                          String justification, String validatedSignalName, Map productJson,
                          Boolean isArchived, Long signalId, String incomingDisposition) {
        // Initializing variables
        boolean attachedSignalData = false
        boolean dispositionChanged = false
        boolean isChangeAllowed = true
        List<Map> alertDueDateList = []
        def domain = getDomainObject(isArchived)
        List<Map> peHistoryList = []
        List<Map> activityList = []
        ValidatedSignal validatedSignal
        String productJsonS = productJson ? JsonOutput.toJson(productJson) : null
        List aggCaseAlertList = fetchAggCaseAlertsForBulkOperations(domain, aggCaseAlertIdList)

        // Checking if disposition is allowed to change
        for (def aggCaseAlert : aggCaseAlertList) {
            if (!incomingDisposition.equals(aggCaseAlert.disposition.displayName)) {
                isChangeAllowed = false
                break
            }
        }

        // Filtering aggCaseAlertList if signalId is provided
        if (signalId) {
            validatedSignal = ValidatedSignal.findById(signalId)
            if (validatedSignal) {
                aggCaseAlertList = aggCaseAlertList.findAll { !it.validatedSignals.contains(validatedSignal) }
            }
        }

        if (!aggCaseAlertList.isEmpty() && isChangeAllowed) {
            boolean bulkUpdate = aggCaseAlertList.size() > 1
            Disposition previousDisposition = aggCaseAlertList[0].disposition
            List<Map<String, String>> alertIdAndSignalIdList = []
            List<Activity> signalActivityList = []
            String productGroupJson = null

            List<String> validatedDateDispositions = Holders.config.alert.validatedDateDispositions
            if (validatedSignalName) {
                String eventJson
                String dictionarySelectionType = SignalUtil.getDictionarySelectionType(
                        aggCaseAlertList[0].parseJsonString(aggCaseAlertList[0].executedAlertConfiguration.productSelection))
                if (dictionarySelectionType) {
                    productJsonS = SignalUtil.generateProductDictionaryJson(
                            aggCaseAlertList[0].productId as String,
                            aggCaseAlertList[0].productName, dictionarySelectionType,
                            aggCaseAlertList[0].executedAlertConfiguration?.isMultiIngredient
                    )
                } else if (productJson?.get("3") && productJson["3"] instanceof List && productJson["3"].size() > 0) {
                    def item = productJson["3"][0]
                    String name = item?.get("name")
                    String id = item?.get("id")
                    if (name && id) {
                        item["name"] = "${name} (${id})"
                    }
                    productGroupJson = name ? new JsonBuilder(productJson["3"])
                            : aggCaseAlertList[0].executedAlertConfiguration.productGroupSelection
                    productJsonS = null
                } else {
                    productGroupJson = aggCaseAlertList[0].executedAlertConfiguration.productGroupSelection
                }
                Disposition defaultSignalDisposition = aggCaseAlertList[0].alertConfiguration.getWorkflowGroup()?.defaultSignalDisposition
                boolean isValidatedDate = validatedDateDispositions.contains(defaultSignalDisposition.value)

                if (aggCaseAlertList[0].alertConfiguration.groupBySmq && aggCaseAlertList[0].alertConfiguration.eventSelection) {
                    eventJson = alertService.generateBulkEventJSON(aggCaseAlertList*.pt, aggCaseAlertList*.ptCode, "")
                } else {
                    eventJson = alertService.generateBulkEventJSON(aggCaseAlertList*.pt, aggCaseAlertList*.ptCode, "4")
                }

                validatedSignal = validatedSignalService.createSignalForBusinessConfiguration(validatedSignalName,
                        aggCaseAlertList[0], Constants.AlertConfigType.AGGREGATE_CASE_ALERT, defaultSignalDisposition,
                        productJsonS, eventJson, productGroupJson, signalId, isValidatedDate)
                activityService.createActivityForSignal(validatedSignal, '', "Signal '${validatedSignal?.name}' created", ActivityType.findByValue(ActivityTypeValue.SignalCreated), validatedSignal.assignedTo,
                        userService.getUser(), [signal: validatedSignal.name], validatedSignal.assignedToGroup)
            }
            String selectedDataSource = aggCaseAlertList[0].executedAlertConfiguration.selectedDatasource
            DashboardCountDTO dashboardCountDTO = alertService.prepareDashboardCountDTO(false)
            boolean isTargetDispReviewed = newDisposition.reviewCompleted
            List<UndoableDisposition> undoableDispositionList = []
            aggCaseAlertList.each { aggregateCaseAlert ->
                String prevDueDate = aggregateCaseAlert.dueDate
                        ? DateUtil.stringFromDate(aggregateCaseAlert.dueDate, "dd-MM-yyyy", "UTC") : null
                aggregateCaseAlert.customAuditProperties = ["justification": justification]

                // Handle when the disposition is not validated
                if (!aggregateCaseAlert.disposition.isValidatedConfirmed()) {
                    handleNonValidatedDisposition(aggregateCaseAlert, newDisposition, undoableDispositionList,
                            alertDueDateList, selectedDataSource, previousDisposition, bulkUpdate, isArchived,
                            peHistoryList, justification)
                }
                // Handle when the previous disposition is validated
                else if (previousDisposition.isValidatedConfirmed()) {
                    handleValidatedDisposition(aggregateCaseAlert, newDisposition, alertDueDateList,
                            justification, validatedSignalName, peHistoryList, isArchived)
                }

                // Create activity for bulk update
                Activity activity = createActivityForBulkUpdate(aggregateCaseAlert, newDisposition,
                        previousDisposition, justification, validatedSignalName)
                activityList.add([execConfigId: aggregateCaseAlert.executedAlertConfigurationId, activity: activity])

                // Update dashboard counts
                updateDashboardCounts(aggregateCaseAlert, dashboardCountDTO, previousDisposition, newDisposition,
                        prevDueDate, isTargetDispReviewed)

                // For Top 50 alert widget
                updateTop50AlertWidget(aggregateCaseAlert, dashboardCountDTO, newDisposition)

                // Add details for signal
                if (validatedSignalName) {
                    alertIdAndSignalIdList.add([col2: aggregateCaseAlert.id.toString(), col1: validatedSignal.id.toString()])
                    signalActivityList.add(activityService.createActivityForSignalBusinessConfigEvdas(aggregateCaseAlert.productName,
                            aggregateCaseAlert.pt, validatedSignal.name, aggregateCaseAlert.name))
                    signalAuditLogService.createAuditLog([
                            entityName : "Signal: Aggregate Review Observations",
                            moduleName : "Signal: Aggregate Review Observations",
                            category   : AuditTrail.Category.INSERT.toString(),
                            entityValue: "${validatedSignalName}: PEC associated",
                            username   : userService.getUser().username,
                            fullname   : userService.getUser().fullName] as Map,
                            [[propertyName: "PEC associated", oldValue: "",
                              newValue: "${aggregateCaseAlert.productName}-${aggregateCaseAlert.pt}"]] as List)
                    attachedSignalData = true
                }
            }

            if (aggCaseAlertIdList) {
                if (!isArchived) {
//                    alertService.updateDashboardCountsForDispChange(dashboardCountDTO, isTargetDispReviewed)
//                    alertService.updateDashboardGroupCountsForDispChange(dashboardCountDTO, isTargetDispReviewed )
                    notify 'push.disposition.changes', [undoableDispositionList:undoableDispositionList]
                }
                def totalPrevDispCount = dashboardCountDTO.prevDispCountMap ?: [:]
                totalPrevDispCount = totalPrevDispCount + dashboardCountDTO.prevGroupDispCountMap ?: [:]
                //Creating peHistories and activities in background
                Map peHistoryActivityMap = [activityList: activityList, peHistoryList: peHistoryList, isBulkUpdate: true,
                                            execDispCountMap: dashboardCountDTO.execDispCountMap, prevDispCountMap: totalPrevDispCount]
                notify 'activity.product.event.history.published', peHistoryActivityMap
            }

            if (validatedSignal) {
                notify 'activity.add.signal.event.published', [signalId: validatedSignal.id, signalActivityList: signalActivityList]
                String insertValidatedQuery = SignalQueryHelper.add_signal_for_agg_alert(isArchived)
                alertService.batchPersistForMapping(sessionFactory.currentSession, alertIdAndSignalIdList, insertValidatedQuery, !isArchived)
            }
            dispositionChanged = true
            if(domain == AggregateCaseAlert && !previousDisposition.isValidatedConfirmed()){
                alertService.batchPersistForDomain(aggCaseAlertList,domain.name)
            }
        }
        Map signal = validatedSignalService.fetchSignalDataFromSignal(validatedSignal, productJsonS,null)
        [attachedSignalData: attachedSignalData.toString(), alertDueDateList: alertDueDateList, signal:signal, dispositionChanged : dispositionChanged]
    }

    def undonePEHistory(AggregateCaseAlert aggregateCaseAlert) {
        log.info("Marking previous PE history as Undone")
        ExecutedConfiguration ec = ExecutedConfiguration.get(aggregateCaseAlert.executedAlertConfigurationId as Long)
        ProductEventHistory productEventHistory = ProductEventHistory.createCriteria().get {
            eq("productName", aggregateCaseAlert.productName)
            eq("eventName", aggregateCaseAlert.pt)
            eq("execConfigId", ec.id)
            eq("configId", ec.configId)
            eq("change", Constants.HistoryType.DISPOSITION)
            order("lastUpdated", "desc")
            maxResults(1)
        }
        if (productEventHistory) {
            productEventHistory.isUndo = true
            productEventHistory.save(flush:true, failOnError:true)
            log.info("Successfully marked previous PE History as Undone for aggregateCaseAlert alert: ${aggregateCaseAlert?.id}")
        }
    }

    @Transactional
    Map revertDisposition(Long id, String justification) {
        log.info("Reverting Disposition Started")
        Boolean dispositionReverted = false
        List<Map> activityList = []
        List<Map> alertDueDateList = []
        List<Map> peHistoryList = []
        String oldDispName = ""
        String newDispName = ""
        AggregateCaseAlert aggregateCaseAlert = AggregateCaseAlert.get(id)
        UndoableDisposition undoableDisposition = UndoableDisposition.createCriteria().get {
            eq('objectId', id as Long)
            eq('objectType', Constants.AlertType.AGGREGATE_NEW)
            order('dateCreated', 'desc')
            maxResults(1)
        }

        if (aggregateCaseAlert && undoableDisposition?.isEnabled) {
            try {
                DashboardCountDTO dashboardCountDTO = alertService.prepareDashboardCountDTO(false)

                Disposition oldDisp = cacheService.getDispositionByValue(aggregateCaseAlert.disposition?.id)
                oldDispName = oldDisp?.displayName
                Disposition newDisposition = cacheService.getDispositionByValue(undoableDisposition.prevDispositionId)
                newDispName = newDisposition?.displayName

                String prevDueDate = aggregateCaseAlert.dueDate ? DateUtil.stringFromDate(aggregateCaseAlert.dueDate, "dd-MM-yyyy", "UTC") : null

                boolean isTargetDispReviewed = newDisposition.reviewCompleted

                undoableDisposition.isUsed = true
                // saving state before undo for activity: 60067
                def prevUndoDisposition = aggregateCaseAlert.disposition
                def prevUndoJustification = aggregateCaseAlert.justification
                def prevUndoDispPerformedBy = aggregateCaseAlert.dispPerformedBy
                def prevUndoDispChangeDate = aggregateCaseAlert.dispLastChange
                def prevUndoDueDate = aggregateCaseAlert.dueDate

                aggregateCaseAlert.disposition = newDisposition
                aggregateCaseAlert.justification = undoableDisposition.prevJustification
                aggregateCaseAlert.dispPerformedBy = undoableDisposition.prevDispPerformedBy
                aggregateCaseAlert.dispLastChange = undoableDisposition.prevDispChangeDate
                aggregateCaseAlert.dueDate = undoableDisposition.prevDueDate
                aggregateCaseAlert.previousDueDate = undoableDisposition.pastPrevDueDate
                aggregateCaseAlert.proposedDisposition = undoableDisposition.prevProposedDisposition
                aggregateCaseAlert.undoJustification = justification

                def activityMap = [
                        'Disposition': [
                                'previous': prevUndoDisposition ?: "",
                                'current': aggregateCaseAlert.disposition ?: ""
                        ],
                        'Justification': [
                                'previous': prevUndoJustification ?: "",
                                'current': aggregateCaseAlert.justification ?: ""
                        ],
                        'Performed By': [
                                'previous': prevUndoDispPerformedBy ?: "",
                                'current': aggregateCaseAlert.dispPerformedBy ?: ""
                        ],
                        'Last Disposition Date': [
                                'previous': prevUndoDispChangeDate ? new SimpleDateFormat("yyyy-MM-dd").format(prevUndoDispChangeDate) : "",
                                'current': aggregateCaseAlert.dispLastChange ? new SimpleDateFormat("yyyy-MM-dd").format(aggregateCaseAlert.dispLastChange) : ""
                        ],
                        'Due Date': [
                                'previous': prevUndoDueDate ? new SimpleDateFormat("yyyy-MM-dd").format(prevUndoDueDate) : "",
                                'current': aggregateCaseAlert.dueDate ? new SimpleDateFormat("yyyy-MM-dd").format(aggregateCaseAlert.dueDate) : ""
                        ]
                ]

                String activityChanges = activityMap.collect { k, v ->
                    def previous = v['previous'] ?: ""
                    def current = v['current'] ?: ""
                    if (previous != current) {
                        "$k changed from \'$previous\' to \'$current\'"
                    } else {
                        null
                    }
                }.findAll().join(', ')

                CRUDService.update(aggregateCaseAlert)
                CRUDService.update(undoableDisposition)

                UndoableDisposition.executeUpdate("Update UndoableDisposition set isEnabled=:isEnabled where objectId=:id and objectType=:type", [isEnabled: false, id: id, type: Constants.AlertType.AGGREGATE_NEW])

                alertDueDateList << [id        : aggregateCaseAlert.id, dueDate: aggregateCaseAlert.dueDate ? new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(aggregateCaseAlert.dueDate) : null, dispositionId: aggregateCaseAlert.disposition.id,
                                     reviewDate: aggregateCaseAlert.reviewDate ? new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(aggregateCaseAlert.reviewDate) : null]
                undonePEHistory(aggregateCaseAlert)
                peHistoryList << createProductEventHistoryForDispositionChange(aggregateCaseAlert, justification, false, true)

                Activity activity = createActivityForUndoAction(aggregateCaseAlert, justification, activityChanges)
                activityList.add([execConfigId: aggregateCaseAlert.executedAlertConfigurationId, activity: activity])

                if (aggregateCaseAlert.assignedToId) {
                    dashboardCountDTO.prevDispCountMap.put(oldDisp.id.toString(), (dashboardCountDTO.prevDispCountMap.get(oldDisp.id.toString()) ?: 0) + 1)
                    alertService.updateDashboardCountMaps(dashboardCountDTO.prevDispCountMap, aggregateCaseAlert.assignedToId, oldDisp.id.toString())
                    alertService.updateDashboardCountMaps(dashboardCountDTO.userDispCountsMap, aggregateCaseAlert.assignedToId, newDisposition.id.toString())
                    if (prevDueDate && isTargetDispReviewed) {
                        alertService.updateDashboardCountMaps(dashboardCountDTO.userDueDateCountsMap, aggregateCaseAlert.assignedToId, prevDueDate)
                    }
                } else {
                    dashboardCountDTO.prevGroupDispCountMap.put(oldDisp.id.toString(), (dashboardCountDTO.prevGroupDispCountMap.get(oldDisp.id.toString()) ?: 0) + 1)
                    alertService.updateDashboardCountMaps(dashboardCountDTO.prevGroupDispCountMap, aggregateCaseAlert.assignedToGroupId, oldDisp.id.toString())
                    alertService.updateDashboardCountMaps(dashboardCountDTO.groupDispCountsMap, aggregateCaseAlert.assignedToGroupId, newDisposition.id.toString())
                    if (prevDueDate && isTargetDispReviewed) {
                        alertService.updateDashboardCountMaps(dashboardCountDTO.groupDueDateCountsMap, aggregateCaseAlert.assignedToGroupId, prevDueDate)
                    }
                }
                //for top 50 alert widget
                alertService.updateDashboardCountMaps(dashboardCountDTO.execDispCountMap, aggregateCaseAlert.executedAlertConfigurationId, newDisposition.id.toString())

                alertService.updateDashboardCountsForDispChange(dashboardCountDTO, isTargetDispReviewed)

                alertService.updateDashboardGroupCountsForDispChange(dashboardCountDTO, isTargetDispReviewed)

                def totalPrevDispCount = dashboardCountDTO.prevDispCountMap ?: [:]
                totalPrevDispCount = totalPrevDispCount + dashboardCountDTO.prevGroupDispCountMap ?: [:]

                Map peHistoryActivityMap = [activityList    : activityList, peHistoryList: peHistoryList, isBulkUpdate: true,
                                            execDispCountMap: dashboardCountDTO.execDispCountMap, prevDispCountMap: totalPrevDispCount]
                notify 'activity.product.event.history.published', peHistoryActivityMap
                dispositionReverted = true
                log.info("Disposition reverted successfully for alert Id: " + id)
            } catch (Exception ex) {
                ex.printStackTrace()
                log.error("some error occurred while reverting disposition")
            }
        }
        [alertDueDateList: alertDueDateList, oldDispName: oldDispName, newDispName: newDispName, dispositionReverted: dispositionReverted]
    }

    Activity createActivityForUndoAction(def alert, String justification, String activityChanges) {
        log.info("Creating Activity for reverting disposition for alert: "+alert.id)
        ActivityType activityType = cacheService.getActivityTypeByValue(ActivityTypeValue.UndoAction.value)
        String changeDetails = Constants.ChangeDetailsUndo.UNDO_DISPOSITION_CHANGE + " with " + activityChanges
        User loggedInUser = cacheService.getUserByUserId(User.findByUsername('signaldev').id)

        Activity activity = activityService.createActivityBulkUpdate(activityType, loggedInUser, changeDetails, justification,
                null, alert.productName, alert.pt, alert.assignedToId ? cacheService.getUserByUserId(alert.assignedToId) : null, null,
                alert.assignedToGroupId ? cacheService.getGroupByGroupId(alert.assignedToGroupId) : null)

        activity
    }

    void saveAlertCaseHistory(def alert, String justification, String fullName){
        alert.justification = justification
        alert.dispLastChange = new Date()
        alert.dispPerformedBy = fullName
        alert.isDispChanged = true
    }

    Map createProductEventHistoryForDispositionChange(def alert, String justification, Boolean isArchived= false, Boolean isUndo=false) {
        Map peHistoryMap = [
                "justification"   : justification,
                "change"          : isUndo ? Constants.HistoryType.UNDO_ACTION : Constants.HistoryType.DISPOSITION,
                "isLatest"        : alert.flagged,
                "isUndo"          : isUndo
        ]
        peHistoryMap = createBasePEHistoryMap(alert,peHistoryMap,isArchived)
        peHistoryMap
    }

    List fetchResultAlertList(List agaList, AlertDataDTO alertDataDTO, String callingScreen = null ) {
        Map params = alertDataDTO.params
        int prevColCount = Holders.config.signal.quantitative.number.prev.columns
        List list = []
        boolean showSpecialPE = Boolean.parseBoolean(params.specialPE)
        List<ExecutedConfiguration> prevExecs = []
        List<Map> prevAggCaseAlertMap = []
        Disposition defaultQuantDisposition = userService.getUser().workflowGroup.defaultQuantDisposition
        cacheService.setDefaultDisp(Constants.AlertType.AGGREGATE_NEW, defaultQuantDisposition.id as Long)
        list = fetchValuesForAggregatedReport(agaList, showSpecialPE, alertDataDTO.domainName,
                callingScreen, alertDataDTO.isFromExport, params.boolean('isArchived'))
        boolean isPrevExecutions = !alertDataDTO.cumulative && !(params.adhocRun.toBoolean()) && params.callingScreen != Constants.Commons.DASHBOARD && params.callingScreen != Constants.Commons.TAGS
        if (isPrevExecutions) {
            prevExecs = fetchPrevPeriodExecConfig(alertDataDTO.executedConfiguration.configId as Long, alertDataDTO.execConfigId as Long)
            if (prevExecs.size() > 0) {
                Set prevExecutionsId = prevExecs.collect {
                    it.id
                }
                Set<Integer> ptCodeList = agaList.collect {
                    it.ptCode
                }

                Set<Integer> productIdList = agaList.collect {
                    it.productId
                }
                prevAggCaseAlertMap = ArchivedAggregateCaseAlert.createCriteria().list {
                    resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                    projections {
                        property("id", "id")
                        property("executedAlertConfiguration.id", "executedAlertConfigurationId")
                        property("productId", "productId")
                        property("ptCode", "ptCode")
                        property("newCount","newCount")
                        property("cummCount","cummCount")
                        property("newPediatricCount","newPediatricCount")
                        property("cummPediatricCount","cummPediatricCount")
                        property("newInteractingCount","newInteractingCount")
                        property("cummInteractingCount","cummInteractingCount")
                        property("newStudyCount", "newStudyCount")
                        property("cumStudyCount", "cumStudyCount")
                        property("newSponCount", "newSponCount")
                        property("cumSponCount", "cumSponCount")
                        property("newSeriousCount", "newSeriousCount")
                        property("cumSeriousCount", "cumSeriousCount")
                        property("newGeriatricCount", "newGeriatricCount")
                        property("cumGeriatricCount", "cumGeriatricCount")
                        property("newNonSerious", "newNonSerious")
                        property("cumNonSerious", "cumNonSerious")
                        property("newFatalCount", "newFatalCount")
                        property("cumFatalCount", "cumFatalCount")
                        property("prrValue", "prrValue")
                        property("prrLCI", "prrLCI")
                        property("prrUCI", "prrUCI")
                        property("rorValue", "rorValue")
                        property("rorLCI", "rorLCI")
                        property("rorUCI", "rorUCI")
                        property("eb95", "eb95")
                        property("eb05", "eb05")
                        property("ebgm", "ebgm")
                        property("chiSquare", "chiSquare")
                        property("ebgmAge", "ebgmAge")
                        property("eb05Age", "eb05Age")
                        property("eb95Age", "eb95Age")
                        property("ebgmGender", "ebgmGender")
                        property("eb05Gender", "eb05Gender")
                        property("eb95Gender", "eb95Gender")
                        property("ebgmSubGroup", "ebgmSubGroup")
                        property("eb05SubGroup", "eb05SubGroup")
                        property("eb95SubGroup", "eb95SubGroup")
                        property("rorSubGroup", "rorSubGroup")
                        property("rorLciSubGroup", "rorLciSubGroup")
                        property("rorUciSubGroup", "rorUciSubGroup")
                        property("prrSubGroup", "prrSubGroup")
                        property("prrLciSubGroup", "prrLciSubGroup")
                        property("prrUciSubGroup", "prrUciSubGroup")
                        property("chiSquareSubGroup","chiSquareSubGroup")
                        property("rorRelSubGroup", "rorRelSubGroup")
                        property("rorLciRelSubGroup", "rorLciRelSubGroup")
                        property("rorUciRelSubGroup", "rorUciRelSubGroup")
                        property("evdasColumns", "evdasColumns")
                        property("faersColumns", "faersColumns")
                        property("vaersColumns", "vaersColumns")
                        property("vigibaseColumns", "vigibaseColumns")
                        property("jaderColumns", "jaderColumns")
                        property("eb05AgeFaers", "eb05AgeFaers")
                        property("eb95AgeFaers", "eb95AgeFaers")
                        property("ebgmAgeFaers", "ebgmAgeFaers")
                        property("eb05GenderFaers", "eb05GenderFaers")
                        property("eb95GenderFaers", "eb95GenderFaers")
                        property("ebgmGenderFaers", "ebgmGenderFaers")
                        property("aValue", "aValue")
                        property("bValue", "bValue")
                        property("cValue", "cValue")
                        property("dValue", "dValue")
                        property("eValue", "eValue")
                        property("rrValue", "rrValue")
                        property("newCountsJson", "newCountsJson")
                    }
                    or {
                        prevExecutionsId.collate(1000).each{
                            'in'("executedAlertConfiguration.id", it)
                        }
                    }
                    'or' {
                        ptCodeList.collate(1000).each {
                            'in'('ptCode', it)
                        }
                    }
                    'or' {
                        productIdList.collate(1000).each {
                            'in'('productId', it)
                        }
                    }
                } as List<Map>
            }
        }
        Map dynamicCountsDashMap = getDynamicCountDashMap()
        ExecutorService executorService = Executors.newFixedThreadPool(6)
        agaList.each { def aga ->
            //If the flow is not cummulative and not the adhoc run then we need to show the previous executions as well.
            executorService.submit({
                if (isPrevExecutions) {
                    (0..<prevColCount).each { index ->
                        def exeName = "exe" + (index)
                        ExecutedConfiguration pec = prevExecs[index]
                        Map countMap = [:]
                        if (pec) {
                            Map prevAlert = prevAggCaseAlertMap.find {
                                it.executedAlertConfigurationId == pec.id && it.productId == aga.productId && it.ptCode == aga.ptCode
                            }
                            //Pushed counts map in function to improve code readability
                            countMap = getCountMapForPrevPEC(prevAlert)

                            if (prevAlert?.newCountsJson) {
                                Map data = JSON.parse(prevAlert?.newCountsJson)
                                for (String k : data.keySet()) {
                                    String key = k
                                    String value = data.get(k)
                                    if(key.contains("new")){
                                        Map fieldDataMap = JSON.parse(value) as Map
                                        countMap[key] = fieldDataMap.new?.toString() ?: ''
                                        countMap[key.replace("new", "cum")] = fieldDataMap.cum?.toString() ?: ''
                                    }else{
                                        countMap.put(key, value)
                                    }
                                }
                            } else {
                                countMap.putAll(dynamicCountsDashMap)
                            }
                        } else {
                            countMap = getDashStringCountMap()
                            countMap.putAll(dynamicCountsDashMap)
                        }
                        list.find { it.id == aga.id }[exeName] = countMap
                    }
                }
            })

        }
        executorService.shutdown()
        cacheService.removeDefaultDisp(Constants.AlertType.AGGREGATE_NEW)
        list
    }

    //TODO combine two method in one for accept a list of filtering result fields
    List fetchResultAlertListForRest(List agaList, AlertDataDTO alertDataDTO, String callingScreen = null, List requiredFields, List execConfigIdList = []) {
        Map params = alertDataDTO.params
        int prevColCount = Holders.config.signal.quantitative.number.prev.columns
        List list = []
        boolean showSpecialPE = Boolean.parseBoolean(params.specialPE)
        List<ExecutedConfiguration> prevExecs = []
        List<Map> prevAggCaseAlertMap = []
        Disposition defaultQuantDisposition = userService.getUser().workflowGroup.defaultQuantDisposition
        cacheService.setDefaultDisp(Constants.AlertType.AGGREGATE_NEW, defaultQuantDisposition.id as Long)
        messageSource = MiscUtil.getBean("messageSource")
        String dataAnalysisLabel = messageSource.getMessage("app.label.dataAnalysis", null, Locale.ENGLISH)
        list = fetchValuesForAggregatedReport(agaList, showSpecialPE, alertDataDTO.domainName,
                callingScreen, alertDataDTO.isFromExport, params.isArchived, dataAnalysisLabel, alertDataDTO.analysisStatusJson, requiredFields)
        boolean isPrevExecutions = !alertDataDTO.cumulative && !(params.adhocRun.toBoolean()) && params.callingScreen != Constants.Commons.DASHBOARD && params.callingScreen != Constants.Commons.TAGS
        if (isPrevExecutions && execConfigIdList.size() == 0) {
            prevExecs = fetchPrevPeriodExecConfig(alertDataDTO.executedConfiguration.configId as Long, alertDataDTO.execConfigId as Long)
            if (prevExecs.size() > 0) {
                Set prevExecutionsId = prevExecs.collect {
                    it.id
                }
                Set<Integer> ptCodeList = agaList.collect {
                    it.ptCode
                }

                Set<Integer> productIdList = agaList.collect {
                    it.productId
                }
                prevAggCaseAlertMap = ArchivedAggregateCaseAlert.createCriteria().list {
                    resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                    projections {
                        property("id", "alertId")
                        property("executedAlertConfiguration.id", "executedAlertConfigurationId")
                        property("productId", "productId")
                        property("ptCode", "ptCode")
                        property("newCount","newCount")
                        property("cummCount","cummCount")
                        property("newPediatricCount","newPediatricCount")
                        property("cummPediatricCount","cummPediatricCount")
                        property("newInteractingCount","newInteractingCount")
                        property("cummInteractingCount","cummInteractingCount")
                        property("newStudyCount", "newStudyCount")
                        property("cumStudyCount", "cumStudyCount")
                        property("newSponCount", "newSponCount")
                        property("cumSponCount", "cumSponCount")
                        property("newSeriousCount", "newSeriousCount")
                        property("cumSeriousCount", "cumSeriousCount")
                        property("newGeriatricCount", "newGeriatricCount")
                        property("cumGeriatricCount", "cumGeriatricCount")
                        property("newNonSerious", "newNonSerious")
                        property("cumNonSerious", "cumNonSerious")
                        property("newFatalCount", "newFatalCount")
                        property("cumFatalCount", "cumFatalCount")
                        property("prrValue", "prrValue")
                        property("prrLCI", "prrLCI")
                        property("prrUCI", "prrUCI")
                        property("rorValue", "rorValue")
                        property("rorLCI", "rorLCI")
                        property("rorUCI", "rorUCI")
                        property("eb95", "eb95")
                        property("eb05", "eb05")
                        property("ebgm", "ebgm")
                        property("chiSquare", "chiSquare")
                        property("ebgmAge", "ebgmAge")
                        property("eb05Age", "eb05Age")
                        property("eb95Age", "eb95Age")
                        property("ebgmGender", "ebgmGender")
                        property("eb05Gender", "eb05Gender")
                        property("eb95Gender", "eb95Gender")
                        property("ebgmSubGroup", "ebgmSubGroup")
                        property("eb05SubGroup", "eb05SubGroup")
                        property("eb95SubGroup", "eb95SubGroup")
                        property("rorSubGroup", "rorSubGroup")
                        property("rorLciSubGroup", "rorLciSubGroup")
                        property("rorUciSubGroup", "rorUciSubGroup")
                        property("prrSubGroup", "prrSubGroup")
                        property("prrLciSubGroup", "prrLciSubGroup")
                        property("prrUciSubGroup", "prrUciSubGroup")
                        property("chiSquareSubGroup","chiSquareSubGroup")
                        property("rorRelSubGroup", "rorRelSubGroup")
                        property("rorLciRelSubGroup", "rorLciRelSubGroup")
                        property("rorUciRelSubGroup", "rorUciRelSubGroup")
                        property("evdasColumns", "evdasColumns")
                        property("faersColumns", "faersColumns")
                        property("vaersColumns", "vaersColumns")
                        property("vigibaseColumns", "vigibaseColumns")
                        property("jaderColumns", "jaderColumns")
                        property("eb05AgeFaers", "eb05AgeFaers")
                        property("eb95AgeFaers", "eb95AgeFaers")
                        property("ebgmAgeFaers", "ebgmAgeFaers")
                        property("eb05GenderFaers", "eb05GenderFaers")
                        property("eb95GenderFaers", "eb95GenderFaers")
                        property("ebgmGenderFaers", "ebgmGenderFaers")
                        property("aValue", "aValue")
                        property("bValue", "bValue")
                        property("cValue", "cValue")
                        property("dValue", "dValue")
                        property("eValue", "eValue")
                        property("rrValue", "rrValue")
                        property("newCountsJson", "newCountsJson")
                    }
                    or {
                        prevExecutionsId.collate(1000).each{
                            'in'("executedAlertConfiguration.id", it)
                        }
                    }
                    'or' {
                        ptCodeList.collate(1000).each {
                            'in'('ptCode', it)
                        }
                    }
                    'or' {
                        productIdList.collate(1000).each {
                            'in'('productId', it)
                        }
                    }
                } as List<Map>
            }
        }
        Map dynamicCountsDashMap = getDynamicCountDashMap()
        ExecutorService executorService = Executors.newFixedThreadPool(6)
        agaList.each { def aga ->
            //If the flow is not cummulative and not the adhoc run then we need to show the previous executions as well.
            executorService.submit({
                if (isPrevExecutions) {
                    (0..<prevColCount).each { index ->
                        def exeName = "exe" + (index)
                        ExecutedConfiguration pec = prevExecs[index]
                        Map countMap = [:]
                        if (pec) {
                            Map prevAlert = prevAggCaseAlertMap.find {
                                it.executedAlertConfigurationId == pec.id && it.productId == aga.productId && it.ptCode == aga.ptCode
                            }
                            //Pushed counts map in function to improve code readability
                            countMap = getCountMapForPrevPEC(prevAlert)

                            if (prevAlert?.newCountsJson) {
                                Map data = JSON.parse(prevAlert?.newCountsJson)
                                for (String k : data.keySet()) {
                                    String key = k
                                    String value = data.get(k)
                                    if(key.contains("new")){
                                        Map fieldDataMap = JSON.parse(value) as Map
                                        countMap[key] = fieldDataMap.new?.toString() ?: ''
                                        countMap[key.replace("new", "cum")] = fieldDataMap.cum?.toString() ?: ''
                                    }else{
                                        countMap.put(key, value)
                                    }
                                }
                            } else {
                                countMap.putAll(dynamicCountsDashMap)
                            }
                        } else {
                            countMap = getDashStringCountMap()
                            countMap.putAll(dynamicCountsDashMap)
                        }
                        Map filteredCountMap = [:]
                        requiredFields.each { field ->
                            def matcher = field =~ /(exe\d+)(.*)/
                            if (matcher) {
                                def prefix = matcher[0][1]
                                def fieldName = matcher[0][2]
                                if (prefix == exeName) {
                                    if (countMap.containsKey(fieldName)) {
                                        filteredCountMap[fieldName] = countMap[fieldName]
                                    }
                                }
                            }
                        }

                        if (filteredCountMap) {
                            list.find { it.alertId == aga.id }[exeName] = filteredCountMap
                        }
                    }
                }
            })

        }
        executorService.shutdown()
        cacheService.removeDefaultDisp(Constants.AlertType.AGGREGATE_NEW)
        list
    }

    Closure saveActivityAndHistory = { AlertLevelDispositionDTO alertLevelDispositionDTO, Boolean isArchived , List<Map> bulkUpdateDueDateDataList = []->
        List<Activity> activityList = []
        List<ProductEventHistory> peHistoryList = []
        List<UndoableDisposition> undoableDispositionList = []
        DashboardCountDTO dashboardCountDTO = alertService.prepareDashboardCountDTO(false)
        dashboardCountDTO.prevDispCountMap = alertLevelDispositionDTO.alertList.findAll { it.assignedToId }.countBy { it.dispositionId as String }
        dashboardCountDTO.prevGroupDispCountMap = alertLevelDispositionDTO.alertList.findAll { !it.assignedToId }.countBy { it.dispositionId as String }
        dashboardCountDTO.execDispCountMap = [(alertLevelDispositionDTO.execConfigId) : [(alertLevelDispositionDTO.targetDisposition.id.toString()):alertLevelDispositionDTO.alertList.size()]]
        boolean isTargetDispReviewed = alertLevelDispositionDTO.targetDisposition.reviewCompleted
        alertLevelDispositionDTO.activityType = ActivityType.findByValue(ActivityTypeValue.DispositionChange)
        alertLevelDispositionDTO.loggedInUser = userService.getUser()
        Map<Long, String> tagsNameMap = getTagsNameList(alertLevelDispositionDTO.execConfigId, alertLevelDispositionDTO.reviewCompletedDispIdList)
        ExecutorService executorService = Executors.newFixedThreadPool(20)
        List<Future> futureList = alertLevelDispositionDTO.alertList.collect { alertMap ->

            //For Dashboard Counts
            String prevDueDate = alertMap.dueDate ? DateUtil.stringFromDate(alertMap.dueDate, "dd-MM-yyyy", "UTC") : null
            if (alertMap.assignedToId) {
                alertService.updateDashboardCountMaps(dashboardCountDTO.prevDispCountMap, alertMap.assignedToId, alertMap.disposition?.id.toString())
                alertService.updateDashboardCountMaps(dashboardCountDTO.userDispCountsMap, alertMap.assignedToId, alertLevelDispositionDTO.targetDisposition.id.toString())
                if (prevDueDate && isTargetDispReviewed) {
                    alertService.updateDashboardCountMaps(dashboardCountDTO.userDueDateCountsMap, alertMap.assignedToId, prevDueDate)
                }
            } else {
                alertService.updateDashboardCountMaps(dashboardCountDTO.prevGroupDispCountMap, alertMap.assignedToGroupId, alertMap.disposition?.id.toString())
                alertService.updateDashboardCountMaps(dashboardCountDTO.groupDispCountsMap, alertMap.assignedToGroupId, alertLevelDispositionDTO.targetDisposition.id.toString())
                if (prevDueDate && isTargetDispReviewed) {
                    alertService.updateDashboardCountMaps(dashboardCountDTO.groupDueDateCountsMap, alertMap.assignedToGroupId, prevDueDate)
                }
            }

            executorService.submit({ ->
                UndoableDisposition undoableDisposition = null
                Activity activity = alertService.createActivityForBulkDisposition(alertMap, alertLevelDispositionDTO)
                ProductEventHistory productEventHistory = createProductEventHistoryForBulkDisposition(alertMap, tagsNameMap, alertLevelDispositionDTO, bulkUpdateDueDateDataList)
                if(!isArchived){
                    Map dispDataMap = [objectId: alertMap.id, objectType: Constants.AlertType.AGGREGATE_NEW, prevDispositionId: alertMap.disposition.id, prevProposedDisposition: alertMap.proposedDisposition,
                                       currDispositionId: alertLevelDispositionDTO.targetDisposition.id, prevJustification: alertMap.justification, prevDispPerformedBy: alertMap.dispPerformedBy,
                                       prevDueDate: alertMap.dueDate, prevDispChangeDate: alertMap.dispLastChange, pastPrevDueDate:alertMap.previousDueDate]
                    undoableDisposition = undoableDispositionService.createUndoableObject(dispDataMap)
                }
                [activity: activity, productEventHistory: productEventHistory, undoableDisposition: undoableDisposition?:null]

            } as Callable)
        }
        futureList.each {
            activityList.add(it.get()['activity'])
            peHistoryList.add(it.get()['productEventHistory'])
            undoableDispositionList.add(it.get()['undoableDisposition'])
        }
        executorService.shutdown()
        def totalPrevDispCount = dashboardCountDTO.prevDispCountMap ?: [:]
        totalPrevDispCount = totalPrevDispCount + dashboardCountDTO.prevGroupDispCountMap ?: [:]
        Map peHistoryActivityMap = [activityList: activityList, peHistoryList: peHistoryList, id: alertLevelDispositionDTO.execConfigId,
                                    execDispCountMap: dashboardCountDTO.execDispCountMap, prevDispCountMap: totalPrevDispCount]
        notify 'activity.product.event.history.published', peHistoryActivityMap
        undoableDispositionList.removeAll([null])
        if(undoableDispositionList.size()>0){
            notify 'push.disposition.changes', [undoableDispositionList:undoableDispositionList]
        }

    }

    Integer changeAlertLevelDisposition(AlertLevelDispositionDTO alertLevelDispositionDTO, Boolean isArchived= false) {
        alertService.changeAlertLevelDisposition(saveActivityAndHistory, alertLevelDispositionDTO,isArchived)
    }

    Map<Long, String> getTagsNameList(Long execConfigId, List<Long> reviewCompletedDispIdList) {
        Map<Long, String> tagsNameMap = [:]
        if (reviewCompletedDispIdList) {
            Session session = sessionFactory.currentSession
            String sql_statement = SignalQueryHelper.list_agg_tag_name(execConfigId, reviewCompletedDispIdList)
            SQLQuery sqlQuery = session.createSQLQuery(sql_statement)
            sqlQuery.list()?.each { row ->
                tagsNameMap.put(row[0] as Long, row[1])
            }
        }
        tagsNameMap
    }


    ProductEventHistory createProductEventHistoryForBulkDisposition(Map alertMap, Map<Long, String> tagsNameMap, AlertLevelDispositionDTO alertLevelDispositionDTO, List <Map> bulkUpdateDueDateDataList = []) {
//        ToDo Need to check that how we can use createBasePEHistoryMap() here.
        Map peHistoryMap = [
                "eventId"       : alertMap.eventId,
                "productId"     : alertMap.productId,
                "productName"   : alertMap.productName,
                "eventName"     : alertMap.pt,
                "justification" : alertLevelDispositionDTO.justificationText,
                "disposition"   : alertLevelDispositionDTO.targetDisposition,
                "change"        : Constants.HistoryType.DISPOSITION,
                "assignedTo"    : alertMap.assignedTo,
                "priority"      : alertMap.priority,
                "prrValue"      : alertMap.prrValue,
                "rorValue"      : alertMap.rorValue,
                "ebgm"          : alertMap.ebgm,
                "eb05"          : alertMap.eb05,
                "eb95"          : alertMap.eb95,
                "isLatest"      : true,
                "asOfDate"      : alertMap.periodEndDate,
                "createdBy"     : alertLevelDispositionDTO.loggedInUser.fullName,
                "modifiedBy"    : alertLevelDispositionDTO.loggedInUser.fullName,
                "configId"      : alertMap.alertConfigurationId,
                "execConfigId"  : alertLevelDispositionDTO.execConfigId,
                "aggCaseAlertId": alertMap.id,
                "tagName"       : alertService.getAlertTagNames(alertMap.id, tagsNameMap)
        ]
        if (bulkUpdateDueDateDataList) {
            Map dueDateMap = bulkUpdateDueDateDataList.find { it.priorityId == alertMap?.priority?.id }
            if (dueDateMap) {
                peHistoryMap.dueDate = !dueDateMap.isClosed ? (dueDateMap.dueDateChange ? dueDateMap.dueDate : alertMap.dueDate) : null
            }
        }
        ProductEventHistory productEventHistory = new ProductEventHistory(peHistoryMap)
        productEventHistory.tagName = peHistoryMap.tagName as String
        productEventHistory.dateCreated = new Date()
        productEventHistory.lastUpdated = new Date()
        productEventHistory.executionDate = new Date()
        productEventHistory
    }

    Map createBasePEHistoryMap(def aggregateCaseAlert, Map additionalInfoMap = [:], Boolean isArchived = false, Date lastUpdatedDate = null) {
        User user = userService.getUser()
        Map peHistoryMap = [
                cumFatalCount           : aggregateCaseAlert.cumFatalCount,
                cumSeriousCount         : aggregateCaseAlert.cumSeriousCount,
                cumSponCount            : aggregateCaseAlert.cumSponCount,
                cumStudyCount           : aggregateCaseAlert.cumStudyCount,
                newFatalCount           : aggregateCaseAlert.newFatalCount,
                newSeriousCount         : aggregateCaseAlert.newSeriousCount,
                newSponCount            : aggregateCaseAlert.newSponCount,
                newStudyCount           : aggregateCaseAlert.newStudyCount,
                positiveRechallenge     : aggregateCaseAlert.positiveRechallenge,
                "productName"           : aggregateCaseAlert.productName,
                "eventName"             : aggregateCaseAlert.pt,
                "productId"             : aggregateCaseAlert.productId,
                "eventId"               : aggregateCaseAlert.ptCode,
                "prrValue"              : aggregateCaseAlert.prrValue,
                "rorValue"              : aggregateCaseAlert.rorValue,
                "ebgm"                  : aggregateCaseAlert.ebgm,
                "eb05"                  : aggregateCaseAlert.eb05,
                "asOfDate"              : aggregateCaseAlert.periodEndDate,
                "assignedTo"            : aggregateCaseAlert.assignedTo,
                "assignedToGroup"       : aggregateCaseAlert.assignedToGroup,
                "disposition"           : aggregateCaseAlert.disposition,
                "eb95"                  : aggregateCaseAlert.eb95,
                "executionDate"         : aggregateCaseAlert.dateCreated,
                "createdBy"             : user?.fullName,
                "modifiedBy"            : user?.fullName,
                "aggCaseAlertObj"       : !isArchived ? aggregateCaseAlert.id : null,
                "archivedAggCaseAlertId": isArchived ? aggregateCaseAlert.id : null,
                "aggCaseAlertId"        : aggregateCaseAlert.id,
                "execConfigId"          : aggregateCaseAlert.executedAlertConfigurationId,
                "configId"              : aggregateCaseAlert.alertConfigurationId,
                "priority"              : aggregateCaseAlert.priority,
                "isLatest"              : true,
                "dueDate"               : aggregateCaseAlert.dueDate,
                "createdTimestamp"      : lastUpdatedDate
        ]
        peHistoryMap << additionalInfoMap
        peHistoryMap
    }

    ExecutedConfiguration fetchLastExecutionOfAlert(ExecutedConfiguration ec) {
        ExecutedConfiguration lastExecutedConfiguration
        if (ec && ec.configId) {
            lastExecutedConfiguration = ExecutedConfiguration.createCriteria().get() {
                eq("configId", ec.configId)
                if (ec.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
                    eq("selectedDatasource", ec.selectedDatasource)
                }
                eq("type", ec.type)
                eq("isLatest", true)
                eq("adhocRun", ec.adhocRun)
                eq("isDeleted", false)
                eq("isEnabled", true)
                order("id", "desc")
                maxResults(1)
            }
        }
        lastExecutedConfiguration
    }

    List<ExecutedConfiguration> fetchAllPrevExecutionOfAlert(ExecutedConfiguration ec) {
        List<ExecutedConfiguration> prevExecutedConfiguration
        if (ec && ec.configId) {
            prevExecutedConfiguration = ExecutedConfiguration.createCriteria().list() {
                eq("configId", ec.configId)
                if (ec.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
                    eq("selectedDatasource", ec.selectedDatasource)
                }
                'executedAlertDateRangeInformation'{
                    'or' {
                        'ne'('dateRangeStartAbsolute', ec.executedAlertDateRangeInformation.dateRangeStartAbsolute)
                        'ne'('dateRangeEndAbsolute', ec.executedAlertDateRangeInformation.dateRangeEndAbsolute)
                    }
                }
                eq("type", ec.type)
                eq("adhocRun", ec.adhocRun)
                eq("isDeleted", false)
                eq("isEnabled", true)
                order("id", "desc")
            }
        }
        prevExecutedConfiguration
    }


    List<Map> getSignalDetectionSummaryMap(List aggregateCaseAlertList) {
        List<Map> signalList = []
        Integer workerCnt = Holders.config.signal.worker.count as Integer
        if (aggregateCaseAlertList) {
            List<Map> peHistoryMap = []
            List<String> productNamePtConfigIds = aggregateCaseAlertList.collect {
                "('" + it.productName?.replaceAll( "'", "''" ) + "','" + it.pt?.replaceAll( "'", "''" ) + "','" + it.alertConfigurationId + "')"
            }
            Sql sql = new Sql(dataSource)
            String sql_statement = SignalQueryHelper.product_event_history_change(productNamePtConfigIds.join(","))
            sql.eachRow(sql_statement) { row ->
                peHistoryMap.add([productName: row[0].toString(), pt: row[1],configId: row[2],justification:row[3].toString(),change: row[5].toString()])
            }
            sql?.close()
            ExecutorService executorService = Executors.newFixedThreadPool(workerCnt)
            List<Future<Map<String, String>>> futureList = aggregateCaseAlertList.collect { def aggregateCaseAlert ->
                executorService.submit({ ->

                    String dispositionJustification = peHistoryMap.find {
                        it.productName == aggregateCaseAlert.productName && it.pt == aggregateCaseAlert.pt && it.configId == aggregateCaseAlert.alertConfigurationId && it.change == Constants.HistoryType.DISPOSITION
                    }?.justification

                    String priorityJustification = peHistoryMap.find {
                        it.productName == aggregateCaseAlert.productName && it.pt == aggregateCaseAlert.pt && it.configId == aggregateCaseAlert.alertConfigurationId && it.change == Constants.HistoryType.PRIORITY
                    }?.justification


                    [
                            "product"              : aggregateCaseAlert.productName,
                            "event"                : aggregateCaseAlert.pt,
                            "justification"        : (dispositionJustification != null && dispositionJustification != "" && dispositionJustification != 'null') ? dispositionJustification : '-',
                            "currentDisposition"   : aggregateCaseAlert.disposition.displayName,
                            "priority"             : cacheService.getPriorityByValue(aggregateCaseAlert.priorityId).displayName,
                            "priorityJustification": priorityJustification ?: '-'
                    ]
                } as Callable)
            }
            futureList.each {
                signalList.add(it.get())
            }
            executorService.shutdown()
        }
        signalList
    }

    List fetchNewProductEvent(pec) {
        List oldProductEvents = []
        pec.collate(1000).each { List pecList ->
            Long productKeyId = pecList[0].productKeyId
            def eventKeyIdList = pecList.collect{ it.eventKeyId as Long}.unique()
            eventKeyIdList.each { eventKeyId ->
                def productEventCombList=[]
                pecList.each { it ->
                    if(it.eventKeyId == eventKeyId){
                        productEventCombList.add(it.productEventComb)
                    }
                }
                oldProductEvents += GlobalProductEvent.createCriteria().list {
                    resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                    projections {
                        property("productEventComb", "productEventComb")
                        property("productKeyId", "productKeyId")
                        property("eventKeyId", "eventKeyId")
                    }
                    'eq'("productKeyId", productKeyId)
                    'eq'("eventKeyId", eventKeyId)
                    or {
                        productEventCombList.collate(1000).each{
                            'in'("productEventComb", it)
                        }
                    }
                }
            }
        }
        return pec - oldProductEvents

    }

    List fetchGlobalProductEvent(pec) {
        List globalPEList = []
        pec.collate(1000).each { List pecList ->
            // get all unique productKeyId
            def productKeyIdList = pecList.collect{ it.productKeyId as Long}.unique()
            // for each of unique productKeyId, get unique eventKeyId
            productKeyIdList.each{ productKeyId ->
                def eventKeyIdList = []
                pecList.each{
                    if(it.productKeyId == productKeyId && !eventKeyIdList.contains(it.eventKeyId as Long))
                        eventKeyIdList.add(it.eventKeyId as Long)
                }
                eventKeyIdList.each { eventKeyId ->
                    def productEventCombList=[]
                    pecList.each { it ->
                        if(it.eventKeyId == eventKeyId){
                            productEventCombList.add(it.productEventComb)
                        }
                    }
                    // after grouping them, get all existing GlobalProductEvents
                    globalPEList += GlobalProductEvent.createCriteria().list {
                        'eq'("productKeyId", productKeyId)
                        'eq'("eventKeyId", eventKeyId)
                        or {
                            productEventCombList.collate(1000).each{
                                'in'("productEventComb", it)
                            }
                        }
                    }
                }
            }
        }
        return globalPEList
    }

    List<String> fetchPECfromdata(Map<String, List<Map>> alertData) {
        List<Map> pec = new ArrayList()
        alertData.each { it->
            if(it.key.equals(Constants.DataSource.EUDRA)) {
                it.value.each { data ->
                    Map globalProductMap =[:]
                    String peCombination = data.substanceId + "-" + data.ptCode + "-" + "null"
                    globalProductMap.put('productEventComb', peCombination)
                    globalProductMap.put('productKeyId', data["PROD_HIERARCHY_ID"]?:Constants.Commons.UNDEFINED_NUM_INT_REVIEW)
                    globalProductMap.put('eventKeyId', data["EVENT_HIERARCHY_ID"]?:Constants.Commons.UNDEFINED_NUM_INT_REVIEW)
                    pec.add(globalProductMap)
                }
            } else {
                List<Map> alertDataList = it.value
                Integer workerCnt = Holders.config.signal.worker.count as Integer
                ExecutorService executorService = Executors.newFixedThreadPool(workerCnt)
                List<Future<Map>> futureList = alertDataList.collect { Map data ->
                    executorService.submit({ ->
                        bindGlobalProductMap(data)
                    } as Callable)
                }
                futureList.each {
                    pec.add(it.get())
                }
                executorService.shutdown()
            }

        }

        return pec
    }

    void saveTagsForBusinessConfig(Long execConfigId) {
        List<String> pEComList = dataObjectService.getPEComList(execConfigId)
        List tags = []
        List<Map> tagsAndSubTags = pvsGlobalTagService.fetchTagsAndSubtags()
        List<PvsGlobalTag> pvsGlobalTagList = []
        List<PvsAlertTag> pvsAlertTagList = []
        List<CategoryDTO> categoryDtoList = []
        List tagsList = []
        JsonSlurper jsonSlurper = new JsonSlurper()
        String dataSource = ''
        if(ExecutedConfiguration.get(execConfigId)?.selectedDatasource.toUpperCase().contains(',')){
            dataSource = ExecutedConfiguration.get(execConfigId)?.selectedDatasource.toUpperCase()
        }
        else {
            dataSource = (ExecutedConfiguration.get(execConfigId)?.selectedDatasource.split(',')[0]).toUpperCase()
        }
        log.info("Now Saving the Tags")
        prepareCategoryDTO(categoryDtoList,pEComList,tagsAndSubTags,dataSource,execConfigId)
        categoryDtoList.collate(20000).each{
            CategoryUtil.saveCategories(it)
        }
        cacheService.prepareCommonTagCache()
        List<Map> tagsAndSubTagsList = pvsGlobalTagService.fetchTagsAndSubtags()
        pEComList.each { peComb ->
            Map keyIdMap = dataObjectService.getKeyIdMap("$peComb-$execConfigId")
            dataObjectService.clearKeyIdMap("$peComb-$execConfigId")
            tags = dataObjectService.getTagsList("$peComb-$execConfigId")
            dataObjectService.removeTagForPEC("$peComb-$execConfigId")

            if (tags) {
                tags.each {tag ->
                    tagsList.add(JSON.parse((jsonSlurper.parseText(tag) as Map).tags))
                }
            }
            tagsList = tagsList.flatten()

            tagsList = tagsList.unique{[it.alert,it.tagText,it.subTags]}

            tagsList.each { tag->
                buildTagList(tag, tagsAndSubTagsList, pvsGlobalTagList, pvsAlertTagList, dataSource, execConfigId, peComb, keyIdMap )
            }
            tagsList.clear()
        }

        categoryDtoList = []
        if(pvsGlobalTagList) {
            log.info("Global tags are present")
            List<Map> tagCaseMap = pvsGlobalTagService.batchPersistGlobalTags(pvsGlobalTagList)
            pvsGlobalTagList = []
            Session session = sessionFactory.currentSession
            String insertValidatedQuery = "INSERT INTO AGG_GLOBAL_TAGS(PVS_GLOBAL_TAG_ID,GLOBAL_PRODUCT_EVENT_ID) VALUES(?,?)"
            alertService.batchPersistForMapping(session, tagCaseMap, insertValidatedQuery)
        }
        if(pvsAlertTagList) {
            log.info("Alert tags are present")
            List<Map> tagAlertMap = pvsAlertTagService.batchPersistAlertTags(pvsAlertTagList)
            pvsAlertTagList = []
            Session session = sessionFactory.currentSession
            String insertValidatedQuery = "INSERT INTO AGG_CASE_ALERT_TAGS(PVS_ALERT_TAG_ID,AGG_ALERT_ID) VALUES(?,?)"
            alertService.batchPersistForMapping(session, tagAlertMap, insertValidatedQuery)
        }
        log.info("Tags are saved across the system.")

        if (pEComList) {
            dataObjectService.clearTagsFromMap(execConfigId)
            dataObjectService.clearPEComList(execConfigId)
        }
    }

    List<Map> fieldListAdvanceFilter(String selectedDatasource=null, boolean groupBySmq = false, def callingScreen, String alertType) {
        List<Map> fieldList = alertFieldService.getAlertFields(alertType, null, true, null).findAll {
            it.enabled == true && !(it.name in ["actions"]) && !it.type.equals("subGroup") &&   !it.name.equals("currentDisposition")
        }
        List newCountList = ['newPediatricCount', 'newPediatricCountVaers', 'newPediatricCountVigibase', 'newPediatricCountFaers', 'newCount', 'newCountFaers', 'newCountVaers'
                              , 'newCountVigibase', 'newInteractingCount', 'newInteractingCountFaers', 'newInteractingCountVaers', 'newInteractingCountVigibase']
        if (groupBySmq == true) {
            fieldList.removeAll {
                (it.name.toString().equals("hlt") || it.name.toString().equals("hlgt") || it.name.toString().equals("smqNarrow"))
            }
        }

        Map subgroupDisplayNames = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', null, null, null).findAll {
            it.type == "subGroup"
        }.collectEntries {
            b -> [b.name, b.display]
        }
        Map labelEnabled = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', null, null, null).collectEntries {
            b -> [b.name, b.enabled]
        }

        Boolean isRor = cacheService.getRorCache()
        Boolean showDss = Holders.config.statistics.enable.dss
        boolean labelCondition = groupBySmq ? true : viewInstanceService.isLabelChangeRequired(selectedDatasource)
        List<Map> fieldList1 = []
        for (int i = 0; i < fieldList.size(); i++) {
            if (fieldList.get(i).name.equals("pt")) {
                if(groupBySmq){
                    fieldList1.add([name: fieldList.get(i).name, display: fieldList.get(i).display.split("#OR")[1], dataType: fieldList.get(i).dataType , 'isAutocomplete':true,enabled: fieldList.get(i).enabled])
                }else{
                    fieldList1.add([name: fieldList.get(i).name, display: fieldList.get(i).display.split("#OR")[0], dataType: fieldList.get(i).dataType,'isAutocomplete':true,enabled: fieldList.get(i).enabled])
                }

            } else if (fieldList.get(i).display.contains("#OR")) {
                if(fieldList.get(i).name.contains("rorLCI")){
                    if (isRor) {
                        fieldList1.add([name: fieldList.get(i).name, display: fieldList.get(i).display.split("#OR")[0].split("/")[0], dataType: fieldList.get(i).dataType,enabled: fieldList.get(i).enabled])
                        fieldList1.add([name: fieldList.get(i).name, display: fieldList.get(i).display.split("#OR")[0].split("/")[1], dataType: fieldList.get(i).dataType,enabled: fieldList.get(i).enabled])
                    } else {
                        fieldList1.add([name: fieldList.get(i).name, display: fieldList.get(i).display.split("#OR")[1].split("/")[0], dataType: fieldList.get(i).dataType,enabled: fieldList.get(i).enabled])
                        fieldList1.add([name: fieldList.get(i).name, display: fieldList.get(i).display.split("#OR")[1].split("/")[1], dataType: fieldList.get(i).dataType,enabled: fieldList.get(i).enabled])
                    }

                } else if(fieldList.get(i).name.contains("currentDisposition")){
                    if(labelCondition){
                        fieldList1.add([name: fieldList.get(i).name, display: fieldList.get(i).display.split("#OR")[0], dataType: fieldList.get(i).dataType,enabled: fieldList.get(i).enabled])
                    }else{
                        fieldList1.add([name: fieldList.get(i).name, display: fieldList.get(i).display.split("#OR")[1], dataType: fieldList.get(i).dataType,enabled: fieldList.get(i).enabled])
                    }
                } else if(fieldList.get(i).name.contains("rorValue")){
                    if (isRor) {
                        fieldList1.add([name: fieldList.get(i).name, display: fieldList.get(i).display.contains("#OR")? fieldList.get(i).display.split("#OR")[0]:fieldList.get(i).display, dataType: fieldList.get(i).dataType,enabled: fieldList.get(i).enabled])
                    }else{
                        fieldList1.add([name: fieldList.get(i).name, display: fieldList.get(i).display.contains("#OR")? fieldList.get(i).display.split("#OR")[1]:fieldList.get(i).display, dataType: fieldList.get(i).dataType,enabled: fieldList.get(i).enabled])
                    }
                } else if(fieldList.get(i).name.contains("rationale")){
                    if (callingScreen.equals("")) {
                        if (Holders.config.dss.enable.autoProposed) {
                            fieldList1.add([name: fieldList.get(i).name, display: fieldList.get(i).display.split("#OR")[0] + fieldList.get(i).display.split("#OR")[1], dataType: fieldList.get(i).dataType,enabled: fieldList.get(i).enabled])
                        } else {
                            fieldList1.add([name: fieldList.get(i).name, display: fieldList.get(i).display.split("#OR")[2], dataType: fieldList.get(i).dataType,enabled: fieldList.get(i).enabled])
                        }
                    } else {
                        if (Holders.config.dss.enable.autoProposed) {
                            fieldList1.add([name: fieldList.get(i).name, display: fieldList.get(i).display.split("#OR")[0], dataType: fieldList.get(i).dataType,enabled: fieldList.get(i).enabled])
                        } else {
                            fieldList1.add([name: fieldList.get(i).name, display: fieldList.get(i).display.split("#OR")[2], dataType: fieldList.get(i).dataType,enabled: fieldList.get(i).enabled])
                        }
                    }
                }
            }else if(fieldList.get(i).name.contains("rorValue")){
                if (isRor) {
                    fieldList1.add([name: fieldList.get(i).name, display: fieldList.get(i).display.contains("#OR")? fieldList.get(i).display.split("#OR")[0]:fieldList.get(i).display, dataType: fieldList.get(i).dataType,enabled: fieldList.get(i).enabled])
                }else{
                    fieldList1.add([name: fieldList.get(i).name, display: fieldList.get(i).display.contains("#OR")? fieldList.get(i).display.split("#OR")[1]:fieldList.get(i).display, dataType: fieldList.get(i).dataType,enabled: fieldList.get(i).enabled])
                }
            } else if( !fieldList.get( i ).name.equals( "pt" ) && fieldList.get( i ).display.contains( "/" ) ) {
                if( fieldList.get( i ).name.equals( "dmeImeEvdas" ) ) {
                    fieldList1.add( [ name: "dmeImeEvdas", display: fieldList.get( i ).display, dataType: 'java.lang.String' ,enabled: fieldList.get(i).enabled] )
                } else {
                    fieldList1.add( [ name: fieldList.get( i ).name, display: fieldList.get( i ).display.split( "/" )[ 0 ], dataType: fieldList.get( i ).dataType ,enabled: fieldList.get(i).enabled] )
                    if( fieldList.get( i ).isNewColumn ) {
                        fieldList1.add( [ name: fieldList.get( i ).name.toString() + "_cum", display: fieldList.get( i ).display.split( "/" )[ 1 ], dataType: fieldList.get( i ).dataType,enabled: fieldList.get(i).enabled ] )
                    } else {
                        if( fieldList.get( i ).name.toString() in newCountList ) {
                            fieldList1.add( [ name: fieldList.get( i ).name?.toString().replace( "new", "cumm" ), display: fieldList.get( i ).display.split( "/" )[ 1 ], dataType: fieldList.get( i ).dataType ,enabled: fieldList.get(i).enabled] )
                        } else {
                            if( fieldList.get( i ).name?.toString().contains( "Evdas" ) && !fieldList.get( i ).name?.toString().contains( "tot" ) ) {
                                if( fieldList.get( i ).name?.toString() in [ "newEvEvdas", "newSeriousEvdas", "newSeriousEvdas", "newLitEvdas" ] ) {
                                    fieldList1.add( [ name: fieldList.get( i ).name?.toString().replace( "new", "total" ), display: fieldList.get( i ).display.split( "/" )[ 1 ], dataType: fieldList.get( i ).dataType,enabled: fieldList.get(i).enabled ] )
                                } else {
                                    fieldList1.add( [ name: fieldList.get( i ).name?.toString().replace( "new", "tot" ), display: fieldList.get( i ).display.split( "/" )[ 1 ], dataType: fieldList.get( i ).dataType ,enabled: fieldList.get(i).enabled] )
                                }
                            } else {
                                if (fieldList.get( i ).name?.toString().contains( "eb05" ) && fieldList.get(i).display.contains("/")) {
                                    fieldList1.add([name: fieldList.get(i).name?.toString().replace("eb05", "eb95"), display: fieldList.get(i).display.split("/")[1], dataType: fieldList.get(i).dataType, enabled: fieldList.get(i).enabled])
                                }else {
                                    fieldList1.add([name: fieldList.get(i).name?.toString().replace("new", "cum"), display: fieldList.get(i).display.split("/")[1], dataType: fieldList.get(i).dataType, enabled: fieldList.get(i).enabled])
                                }
                            }

                        }
                    }
                }

            } else if (fieldList.get(i).name.equals("rorLci") && fieldList.get(i).display.contains("/")) {
                fieldList1.add([name: fieldList.get(i).name, display: fieldList.get(i).display.split("/")[0], dataType: fieldList.get(i).dataType,enabled: fieldList.get(i).enabled])
                fieldList1.add([name: fieldList.get(i).name, display: fieldList.get(i).display.split("/")[1], dataType: fieldList.get(i).dataType,enabled: fieldList.get(i).enabled])
            } else if( fieldList.get( i ).name.equals( "disposition" ) ) {
                fieldList1.add( [ name: "disposition.id", display: fieldList.get( i ).display, dataType: "java.lang.String",enabled: fieldList.get(i).enabled ] )
            } else if( fieldList.get( i ).name.equals( "priority" ) ) {
                fieldList1.add( [ name: "priority.id", display: "Priority", dataType: fieldList.get( i ).dataType ,enabled: fieldList.get(i).enabled] )
            } else if( fieldList.get( i ).name.equals( "alertTags" ) ) {
                fieldList1.add( [ name: "tags", display: fieldList.get( i ).display, dataType: fieldList.get( i ).dataType ,enabled: fieldList.get(i).enabled] )
            } else if( fieldList.get( i ).name.equals( "impEvents" ) ) {
                fieldList1.add( [ name: "aggImpEventList", display: fieldList.get( i ).display, dataType: fieldList.get( i ).dataType ,enabled: fieldList.get(i).enabled] )
            } else if( fieldList.get( i ).name.equals( "signalsAndTopics" ) ) {
                fieldList1.add( [ name: "signal", display: fieldList.get( i ).display, dataType: fieldList.get( i ).dataType ,enabled: fieldList.get(i).enabled] )
            } else if( fieldList.get( i ).name.equals( "dispPerformedBy" ) ) {
                fieldList1.add( [ name: fieldList.get(i).name, display: fieldList.get( i ).display, dataType: 'java.lang.String','isAutocomplete':true ,enabled: fieldList.get(i).enabled] )
            }else if( fieldList.get( i ).name.equals( "justification" ) ) {
                fieldList1.add( [ name: fieldList.get(i).name, display: fieldList.get( i ).display, dataType: 'java.lang.String','isAutocomplete':true ,enabled: fieldList.get(i).enabled] )
            }
            else{
                if(fieldList.get(i).isAutocomplete){
                    fieldList1.add([name: fieldList.get(i).name, display: fieldList.get(i).display, dataType: fieldList.get(i).dataType,'isAutocomplete':true,enabled: fieldList.get(i).enabled])
                }else{
                    fieldList1.add([name: fieldList.get(i).name, display: fieldList.get(i).display, dataType: fieldList.get(i).dataType,enabled: fieldList.get(i).enabled])
                }
            }
        }


        if (callingScreen != Constants.Commons.DASHBOARD) {
            fieldList1.removeAll { it ->
                it.name.toString().equals('name')
            }
        }
        fieldList1.removeAll {
            (it.name.toString().equals("assignedTo"))
        }
        fieldList1.add([name: "assignedTo.id", display: "Assigned To(User)", dataType: "java.lang.String",'isAutocomplete':true,enabled:true])
        fieldList1.add([name: "assignedToGroup.id", display: "Assigned To(Group)", dataType: "java.lang.String",'isAutocomplete':true,enabled:true])

         //Changes for bug PVS-53972 starts from here.
        fieldList1.add([name: "flags", display: "Flags", dataType: "java.lang.String", 'isAutocomplete': false,enabled:true])
        fieldList1.add([name: "subTags", display: "Sub Categories", dataType: "java.lang.String", 'isAutocomplete': false,enabled:true])
        fieldList1.add([name: "currentRun", display: "Current Period Category", dataType: "java.lang.Boolean", 'isAutocomplete': false,enabled:true])
        //Changes for bug PVS-53972 ends here.

        List subGrpFieldList = []
        List subGroup = cacheService.getSubGroupColumns()?.flatten()
        List subGroupFaers = cacheService.getSubGroupColumnFaers()?.flatten()
        List subGroupValue = grailsApplication.config.businessConfiguration.attributes.aggregate.subGroup as List
        Map<String,List<String>> getAllOtherSubGroupColumnsListMap = cacheService.getAllOtherSubGroupColumnsListMap(Constants.DataSource.PVA)
        Map<String,List<String>> relativeSubGroupMap = cacheService.getRelativeSubGroupColumnsListMap(Constants.DataSource.PVA)
        subGroupValue.each { def sub ->
            subGroup.each {
                String key = sub.key.toLowerCase() + it
                String value = sub.value + ':' + it
                String text = subgroupDisplayNames.get(key)
                if(labelEnabled.get(key))
                {
                    subGrpFieldList.add([name: value, display: text, dataType: 'java.lang.Number',enabled:true])
                }

            }
                subGroupFaers.each {
                    String key = sub.key.toLowerCase() + it.value + "Faers"
                    String valueFaers = sub.value + "FAERS" + ':' + it.value
                    String textFaers =subgroupDisplayNames.get(key)
                    if(labelEnabled.get(key))
                    {
                        subGrpFieldList.add([name: valueFaers, display: textFaers, dataType: 'java.lang.Number',enabled:true])
                    }
                }
            }
        getAllOtherSubGroupColumnsListMap?.each{ sub , subGroupList ->
            subGroupList?.each {
                String key = cacheService.toCamelCase(sub.toLowerCase()) + it
                String value = sub?.toString() + ':' + it?.toString()
                String text = subgroupDisplayNames.get(key)
                if(labelEnabled.get(key))
                {
                    subGrpFieldList.add([name: value, display: text, dataType: 'java.lang.Number',enabled:true])
                }
            }
        }
        relativeSubGroupMap?.each{ sub , subGroupList ->
            subGroupList?.each {
                String key = cacheService.toCamelCase(sub.toLowerCase()) +"el" + it
                String value = sub?.toString() + ':' + it?.toString()
                String text = subgroupDisplayNames.get(key)
                if(labelEnabled.get(key))
                {
                    subGrpFieldList.add([name: value, display: text, dataType: 'java.lang.Number',enabled:true])
                }
            }
        }

        subGrpFieldList.addAll(fieldList1)
        subGrpFieldList.sort { a, b -> a.display?.trim() <=> b.display?.trim() }
    }
    List<Map> fieldListAdvanceFilterJader(Boolean groupBySmq) {
        List fieldList = alertFieldService.getJaderColumnList(Constants.DataSource.JADER,groupBySmq)
        Boolean isRor = cacheService.getRorCache()
        List newFieldList = []
        fieldList.each {
            String[] displayName = it.display?.split("/")
            if(it.secondaryName && it.secondaryName !="" && displayName.size() > 1){
                newFieldList.add(["name":it.name,"display":displayName[0],"dataType":it.dataType])
                newFieldList.add(["name":it.secondaryName,"display":displayName[1],"dataType":it.dataType])
            }else{
                if(it.isAutocomplete) {
                    newFieldList.add(["name": it.name, "display": it.display, "dataType": it.dataType,"isAutocomplete":true])
                }else{
                    if( it.name.equals( "alertTags" ) ) {
                        newFieldList.add( [ name: "tags", display: it.display, dataType: it.dataType ] )
                    }else if( it.name.equals( "signalsAndTopics" ) ) {
                        newFieldList.add( [ name: "signal", display: it.display, dataType: it.dataType ] )
                    } else if( it.name.equals( "disposition" ) ) {
                        newFieldList.add( [ name: "disposition.id", display: it.display, dataType: "java.lang.String" ] )
                    } else {
                        newFieldList.add(["name": it.name, "display": it.display, "dataType": it.dataType])
                    }
                }
            }
        }
        newFieldList.add([name: "subTags", display: "Sub Categories", dataType: "java.lang.String", 'isAutocomplete': false])
        fieldList = newFieldList
        fieldList.removeAll {
            (it.name.toString().equals("assignedTo"))
        }
        fieldList.add([name: "assignedTo.id", display: "Assigned To(User)", dataType: "java.lang.String",'isAutocomplete':true])
        fieldList.add([name: "assignedToGroup.id", display: "Assigned To(Group)", dataType: "java.lang.String",'isAutocomplete':true])
        fieldList.removeAll{it?.name?.toString()?.equals("actions")}
        fieldList.removeAll{it?.name?.toString()?.equals("currentDisposition")}
        return fieldList
    }




    List fetchAggCaseAlertsForBulkOperations(def domain, List<Long> aggCaseAlertIdList) {
        List aggCaseAlertList = domain.createCriteria().list {
            'or' {
                aggCaseAlertIdList.collate(1000).each {
                    'in'("id", it)
                }
            }
        } as List
        aggCaseAlertList
    }

    private Activity createActivityForBulkUpdate(def aggregateCaseAlert, Disposition newDisposition, Disposition previousDisposition,
                                                 String justification, String validatedSignalName) {
        ActivityType activityType
        Date lastDispChange = null
        String changeDetails
        User loggedInUser = cacheService.getUserByUserId(userService.getCurrentUserId())
        if (previousDisposition.isValidatedConfirmed()) {
            activityType = cacheService.getActivityTypeByValue(ActivityTypeValue.SignalAdded.value)
            changeDetails = "PEC attached with Signal '$validatedSignalName'"
        } else {
            activityType = cacheService.getActivityTypeByValue(ActivityTypeValue.DispositionChange.value)
            if (validatedSignalName) {
                changeDetails = "Disposition changed from '$previousDisposition.displayName' to '$newDisposition.displayName' and attached with signal '$validatedSignalName'"
            } else {
                changeDetails = "Disposition changed from '$previousDisposition.displayName' to '$newDisposition.displayName'"
            }
        }
        lastDispChange = aggregateCaseAlert.dispLastChange
        Activity activity = activityService.createActivityBulkUpdate(activityType, loggedInUser, changeDetails, justification,
                null, aggregateCaseAlert.productName, aggregateCaseAlert.pt, aggregateCaseAlert.assignedToId ? cacheService.getUserByUserId(aggregateCaseAlert.assignedToId) : null, null,
                aggregateCaseAlert.assignedToGroupId ? cacheService.getGroupByGroupId(aggregateCaseAlert.assignedToGroupId) : null, lastDispChange)
        activity
    }

    void invokeReportingForQuantAlert(ExecutedConfiguration executedConfiguration, Long faersId, Long vigibaseId, Long vaersId) {
        boolean isGenerateReport = executedConfiguration.executedTemplateQueries?.size() > 0
        boolean isSpotfire = false
        boolean isFaersSpotfire = false
        boolean isVigibaseSpotfire = false
        boolean isVaersSpotfire = false
        if(executedConfiguration.spotfireSettings){
            SpotfireSettingsDTO settings = SpotfireSettingsDTO.fromJson(executedConfiguration.spotfireSettings)
            isSpotfire = settings.dataSource.contains('pva')
            isFaersSpotfire = settings.dataSource.contains('faers')
            isVigibaseSpotfire = settings.dataSource.contains('vigibase')
            isVaersSpotfire = settings.dataSource.contains('vaers')
        }

        boolean isCaseSeriesGenerated = generateCaseSeriesForReporting(executedConfiguration, isGenerateReport, isSpotfire, isFaersSpotfire, isVigibaseSpotfire, isVaersSpotfire)
        if ((isGenerateReport || isSpotfire || isFaersSpotfire || isVigibaseSpotfire || isVaersSpotfire) && isCaseSeriesGenerated) {
            Promise promise = task {
                generateReportsInBackground(executedConfiguration.id, faersId, vigibaseId, vaersId)
            }
            promise.onComplete { boolean isReportingCompleted ->
                if (!isReportingCompleted) {
                    alertService.sendReportingErrorNotifications(executedConfiguration, isGenerateReport, isSpotfire)
                    singleCaseAlertService.updateReportStatusForError(executedConfiguration)
                }
            }
            promise.onError { Throwable err ->
                err.printStackTrace()
                throw err
            }

        } else if (!isCaseSeriesGenerated) {
            alertService.sendReportingErrorNotifications(executedConfiguration, isGenerateReport, isSpotfire)
            singleCaseAlertService.updateReportStatusForError(executedConfiguration)
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    boolean generateCaseSeriesForReporting(ExecutedConfiguration executedConfiguration, boolean isGenerateReport, boolean isSpotfire,boolean isFaersSpotfire, boolean isVigibaseSpotfire, boolean isVaersSpotfire) {
        boolean isCaseSeriesGenerated = true
        if (isGenerateReport || isSpotfire) {
            boolean isIntervalCaseSeriesRequired = executedConfiguration.executedAlertDateRangeInformation.dateRangeEnum != DateRangeEnum.CUMULATIVE
            if (isIntervalCaseSeriesRequired) {
                Long seriesId = singleCaseAlertService.generateExecutedCaseSeries(executedConfiguration, true)
                if (seriesId) {
                    String updateCaseSeriesHql = prepareUpdateCaseSeriesHql(false)
                    ExecutedConfiguration.executeUpdate(updateCaseSeriesHql, [pvrCaseSeriesId: seriesId, id: executedConfiguration.id])
                } else {
                    isCaseSeriesGenerated = false
                }
            }

            boolean isCummCaseSeriesRequired = !isIntervalCaseSeriesRequired || alertService.isCumCaseSeriesReport(executedConfiguration) || alertService.isCumCaseSeriesSpotfire(executedConfiguration)
            if (isCummCaseSeriesRequired) {
                Long seriesId = singleCaseAlertService.generateExecutedCaseSeries(executedConfiguration, true)
                if (seriesId) {
                    String updateCaseSeriesHql = prepareUpdateCaseSeriesHql(true)
                    ExecutedConfiguration.executeUpdate(updateCaseSeriesHql, [pvrCaseSeriesId: seriesId, id: executedConfiguration.id])
                    if (!isIntervalCaseSeriesRequired) {
                        ExecutedConfiguration.executeUpdate(prepareUpdateCaseSeriesHql(false), [pvrCaseSeriesId: seriesId, id: executedConfiguration.id])
                    }
                } else {
                    isCaseSeriesGenerated = false
                }
            }
        }

        if (isFaersSpotfire) {
            List<DateRangeEnum> dateRangeEnumList = alertService.dateRangeFaersSpotfire(executedConfiguration)
            if (dateRangeEnumList.contains(DateRangeEnum.PR_DATE_RANGE_FAERS)) {
                Long seriesId = singleCaseAlertService.generateExecutedCaseSeries(executedConfiguration, true, false, Constants.DataSource.FAERS)
                if (seriesId) {
                    ExecutedConfiguration.executeUpdate("Update ExecutedConfiguration set faersCaseSeriesId = :faersCaseSeriesId where id = :id", [faersCaseSeriesId: seriesId, id: executedConfiguration.id])
                } else {
                    isCaseSeriesGenerated = false
                }
            }

            if (dateRangeEnumList.contains(DateRangeEnum.CUMULATIVE_FAERS)) {
                Long seriesId = singleCaseAlertService.generateExecutedCaseSeries(executedConfiguration, true, false, Constants.DataSource.FAERS)
                if (seriesId) {
                    ExecutedConfiguration.executeUpdate("Update ExecutedConfiguration set faersCumCaseSeriesId = :faersCumCaseSeriesId where id = :id", [faersCumCaseSeriesId: seriesId, id: executedConfiguration.id])
                } else {
                    isCaseSeriesGenerated = false
                }
            }
        }

        if (isVigibaseSpotfire) {
            List<DateRangeEnum> dateRangeEnumList = alertService.dateRangeVigibaseSpotfire(executedConfiguration)
            if (dateRangeEnumList.contains(DateRangeEnum.PR_DATE_RANGE_VIGIBASE)) {
                Long seriesId = singleCaseAlertService.generateExecutedCaseSeries(executedConfiguration, true, false, Constants.DataSource.VIGIBASE)
                if (seriesId) {
                    ExecutedConfiguration.executeUpdate("Update ExecutedConfiguration set vigibaseCaseSeriesId = :vigibaseCaseSeriesId where id = :id", [vigibaseCaseSeriesId: seriesId, id: executedConfiguration.id])
                } else {
                    isCaseSeriesGenerated = false
                }
            }

            if (dateRangeEnumList.contains(DateRangeEnum.CUMULATIVE_VIGIBASE)) {
                Long seriesId = singleCaseAlertService.generateExecutedCaseSeries(executedConfiguration, true, false, Constants.DataSource.VIGIBASE)
                if (seriesId) {
                    ExecutedConfiguration.executeUpdate("Update ExecutedConfiguration set vigibaseCumCaseSeriesId = :vigibaseCumCaseSeriesId where id = :id", [vigibaseCumCaseSeriesId: seriesId, id: executedConfiguration.id])
                } else {
                    isCaseSeriesGenerated = false
                }
            }
        }

        if (isVaersSpotfire) {
            List<DateRangeEnum> dateRangeEnumList = alertService.dateRangeVaersSpotfire(executedConfiguration)
            if (dateRangeEnumList.contains(DateRangeEnum.PR_DATE_RANGE_VAERS)) {
                Long seriesId = singleCaseAlertService.generateExecutedCaseSeries(executedConfiguration, true,false ,Constants.DataSource.VAERS)
                if (seriesId) {
                    ExecutedConfiguration.executeUpdate("Update ExecutedConfiguration set vaersCaseSeriesId = :vaersCaseSeriesId where id = :id", [vaersCaseSeriesId: seriesId, id: executedConfiguration.id])
                } else {
                    isCaseSeriesGenerated = false
                }
            }

            if (dateRangeEnumList.contains(DateRangeEnum.CUMULATIVE_VAERS)) {
                Long seriesId = singleCaseAlertService.generateExecutedCaseSeries(executedConfiguration, true,false, Constants.DataSource.VAERS)
                if (seriesId) {
                    ExecutedConfiguration.executeUpdate("Update ExecutedConfiguration set vaersCumCaseSeriesId = :vaersCumCaseSeriesId where id = :id", [vaersCumCaseSeriesId: seriesId, id: executedConfiguration.id])
                } else {
                    isCaseSeriesGenerated = false
                }
            }
        }
        Session session = sessionFactory.currentSession
        session.flush()
        session.clear()
        isCaseSeriesGenerated
    }

    @Transactional
    boolean generateReportsInBackground(Long execConfigId, Long faersId, Long vigibaseId, Long vaersId) {
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(execConfigId)
        ExecutedConfiguration executedConfigurationFaers = faersId ? ExecutedConfiguration.get(faersId) : null
        boolean isReportingCompleted
        log.info("Generating report in background")
        if (executedConfiguration.pvrCaseSeriesId) {
            isReportingCompleted = generateCaseSeriesForQuantAlert(executedConfiguration, false)
        }
        if (executedConfiguration.pvrCumulativeCaseSeriesId && executedConfiguration.pvrCaseSeriesId != executedConfiguration.pvrCumulativeCaseSeriesId) {
            isReportingCompleted = generateCaseSeriesForQuantAlert(executedConfiguration, true)
        }

        if (executedConfiguration.faersCaseSeriesId) {
            isReportingCompleted = generateCaseSeriesForFaersAlert(executedConfiguration, faersId, false)
        }

        if (executedConfiguration.faersCumCaseSeriesId) {
            isReportingCompleted = generateCaseSeriesForFaersAlert(executedConfiguration, faersId, true)
        }

        if (executedConfiguration.vigibaseCaseSeriesId) {
            isReportingCompleted = generateCaseSeriesForVigibaseAlert(executedConfiguration, vigibaseId, false)
        }

        if (executedConfiguration.vigibaseCumCaseSeriesId) {
            isReportingCompleted = generateCaseSeriesForVigibaseAlert(executedConfiguration, vigibaseId, true)
        }

        if (executedConfiguration.vaersCaseSeriesId) {
            isReportingCompleted = generateCaseSeriesForVaersAlert(executedConfiguration, vaersId, false)
        }

        if (executedConfiguration.vaersCumCaseSeriesId) {
            isReportingCompleted = generateCaseSeriesForVaersAlert(executedConfiguration, vaersId, true)
        }

        if (isReportingCompleted) {
            alertService.generateReport(executedConfiguration)
            alertService.generateSpotfireReport(executedConfiguration, executedConfigurationFaers)
        }
        isReportingCompleted
    }

    boolean generateCaseSeriesForQuantAlert(ExecutedConfiguration executedConfiguration, boolean isCumulative) {
        List<Map> caseVersionList = generateCaseVersionList(executedConfiguration.id, Constants.DataSource.PVA, isCumulative)
        Long seriesId = isCumulative ? executedConfiguration.pvrCumulativeCaseSeriesId : executedConfiguration.pvrCaseSeriesId
        alertService.saveCaseSeriesInMart(caseVersionList, executedConfiguration, seriesId, isCumulative, Constants.DataSource.PVA)
    }

    boolean generateCaseSeriesForFaersAlert(ExecutedConfiguration executedConfiguration, Long faersId, boolean isCumulative) {
        List<Map> caseVersionList = generateCaseVersionList(faersId ?: executedConfiguration.id, Constants.DataSource.FAERS, isCumulative)
        Long seriesId = isCumulative ? executedConfiguration.faersCumCaseSeriesId : executedConfiguration.faersCaseSeriesId
        alertService.saveCaseSeriesInMart(caseVersionList, executedConfiguration, seriesId, isCumulative,Constants.DataSource.FAERS)
    }

    boolean generateCaseSeriesForVigibaseAlert(ExecutedConfiguration executedConfiguration, Long vigibaseId, boolean isCumulative) {
        List<Map> caseVersionList = generateCaseVersionList(vigibaseId ?: executedConfiguration.id, Constants.DataSource.VIGIBASE, isCumulative)
        Long seriesId = isCumulative ? executedConfiguration.vigibaseCumCaseSeriesId : executedConfiguration.vigibaseCaseSeriesId
        alertService.saveCaseSeriesInMart(caseVersionList, executedConfiguration, seriesId, isCumulative,Constants.DataSource.VIGIBASE)
    }

    boolean generateCaseSeriesForVaersAlert(ExecutedConfiguration executedConfiguration, Long vaersId, boolean isCumulative) {
        List<Map> caseVersionList = generateCaseVersionList(vaersId ?: executedConfiguration.id, Constants.DataSource.VAERS, isCumulative)
        Long seriesId = isCumulative ? executedConfiguration.vaersCumCaseSeriesId : executedConfiguration.vaersCaseSeriesId
        alertService.saveCaseSeriesInMart(caseVersionList, executedConfiguration, seriesId, isCumulative,Constants.DataSource.VAERS)
    }

    String prepareUpdateCaseSeriesHql(boolean isCumulativeCaseSeries) {
        StringBuilder updateCaseSeriesHql = new StringBuilder()
        if (isCumulativeCaseSeries) {
            updateCaseSeriesHql.append("Update ExecutedConfiguration set pvrCumulativeCaseSeriesId = :pvrCaseSeriesId ")
        } else {
            updateCaseSeriesHql.append("Update ExecutedConfiguration set pvrCaseSeriesId = :pvrCaseSeriesId ")
        }
        updateCaseSeriesHql.append(" where id = :id").toString()
    }

    List generateCaseVersionList(Long execConfigId, String selectedDataSource, boolean isCumulative) {
        Sql sqlObj = null
        List<Map> caseVersionList = []
        def dataSourceObj = signalDataSourceService.getDataSource(selectedDataSource)
        try {
            sqlObj = new Sql(dataSourceObj)
            sqlObj.call(SignalQueryHelper.quant_case_series_proc(isCumulative),
                    [execConfigId,
                     Sql.resultSet(OracleTypes.CURSOR)
                    ]
            ) { case_series ->
                if (case_series != null) {
                    while (case_series.next()) {
                        caseVersionList.add([caseNumber: case_series.getAt('CASE_NUM'), versionNumber: case_series.getAt("VERSION_NUM")])
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.printStackTrace())
        } finally {
            sqlObj?.close()
        }
        caseVersionList
    }

    @Transactional
    void persistAlertDueDate(List alertDueDateList) {
        def size = Holders.config.signal.batch.size
        Sql sql = null
        try {
            sql = new Sql(dataSource)
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            sql.withBatch(size, "update AGG_ALERT set disposition_id = :val0, due_date = :val1, review_date = :val2 " +
                    "where id = :val3".toString(), { preparedStatement ->
                alertDueDateList.each { def obj ->
                    preparedStatement.addBatch(val0: obj.dispositionId, val1: obj.dueDate ? new Timestamp(sdf.parse(obj.dueDate).getTime()) : null,
                            val2: obj.reviewDate ? new Timestamp(sdf.parse(obj.reviewDate).getTime()) : null, val3: obj.id)
                }
            })
        } catch (Exception e) {
            log.error(e.printStackTrace())
        } finally {
            sql?.close()
        }
    }

    def getDomainObject(Boolean isArchived) {
        isArchived ? ArchivedAggregateCaseAlert : AggregateCaseAlert
    }

    List getColumnListForExcelExport(String alertType, Long viewId,Boolean adhocRun = false,Boolean groupBySmq = false,Boolean isJader = false) {
        ViewInstance viewInstance = viewInstanceService.fetchSelectedViewInstance(alertType, viewId)
        JsonSlurper js = new JsonSlurper()
        Map caseListCols = js.parseText(viewInstance?.tempColumnSeq ?: viewInstance?.columnSeq)
        List columnList = []
        Map labelConfig = alertFieldService.getColumnInfoExport(adhocRun,groupBySmq,isJader)
        caseListCols.each{ k, v ->
            if(v.containerView == 1){
                if(v?.name == "rorLCI") {
                    columnList?.addAll(getLabelNameList(v?.label.split("/")[0], v?.name, labelConfig))
                    columnList?.addAll(getLabelNameList(v?.label.split("/")[1], 'rorUCI', labelConfig))
                } else{
                    columnList?.addAll(getLabelNameList(v?.label, v?.name, labelConfig))
                }
            }
        }
        Map disposition=[label:'Disposition', name:'currentDisposition']
        Map proposedDisposition = [label:'Disposition (*-Proposed)', name:'currentDisposition']
        Map currDisposition=[label:'Current Disposition', name:'disposition']
        if(columnList.contains(disposition) && columnList.contains(currDisposition))
        {
            columnList=columnList-currDisposition
        } else if(columnList.contains(proposedDisposition) && columnList.contains(currDisposition)){
            columnList=columnList-proposedDisposition
        }
        columnList
    }

    List getLabelNameList (String currentLabel, String currentName, Map labelConfig) {
        List tempCoulmnList = []
        String withoutPrevPeriod
        String prevPerioidString = "Prev Period"
        Integer period
        String intvCumString = currentLabel
        String[] intvCumArray
        String label


        if(currentLabel.contains(prevPerioidString)){
            withoutPrevPeriod = currentLabel.substring(prevPerioidString.length() + 1)
            period = Integer.parseInt(withoutPrevPeriod.substring(0,1))
            intvCumString = withoutPrevPeriod.substring(1).trim()
        }
        if(intvCumString.contains("/") && !intvCumString.contains("IME/DME") && !intvCumString.contains("Prior Review/DSS")){
            intvCumArray = intvCumString.split("/")
            intvCumArray[0] = intvCumArray[0]
        }
        if (intvCumArray) {
            if (!intvCumArray){
                intvCumArray = intvCumString.split(" ")
            }
            intvCumArray.each {
                label = period ? prevPerioidString + " " + period + " " + it   : it
                if (labelConfig.containsKey(it)) {
                    String name = period?"exe"+(period-1)+labelConfig.get(it):labelConfig.get(it)
                    tempCoulmnList << [label: label, name: name]
                } else {
                    tempCoulmnList << [label: label, name: currentName]
                }
            }
        }else {
            tempCoulmnList << [label: currentLabel, name: currentName]
        }

        tempCoulmnList
    }


    AggregateCaseAlert createAggregateAlert(Map data, Configuration config, ExecutedConfiguration executedConfig,
                                            List<Date> dateRangeStartEndDate, Map prevAlertData,Map prevAlertDataFaers, Disposition defaultDisposition, List eiList, Map faersData, Map evdasData, Map vaersData, Map vigibaseData,
                                            Map<String,Long> otherDataSourcesExecIds, List<Map> allPrevAlertsForTrendFlag,
                                            List<Long> prevExecIdsForTrendFlag, List<Long> reviewedDispositions, boolean isSafety = true, List listednessData=[]) {

        AggregateCaseAlert prevAggAlert
        Boolean enabledTrendFlag = Holders.config.signal.agg.calculate.trend.flag as Boolean

        String[] ptCodeWithSMQ = data["PT_CODE"] ? String.valueOf(data["PT_CODE"]).split(Constants.Commons.DASH_STRING, 2) : ["0"]
        String ptCode = ptCodeWithSMQ ? ptCodeWithSMQ[0] ? ptCodeWithSMQ[0] : 0 : Constants.Commons.UNDEFINED_NUM
        String key = executedConfig.id + '-' + data["PRODUCT_ID"] + '-' + "${ptCodeWithSMQ.size() > 1 ? data["PT_CODE"] : ptCode}"
        prevAggAlert = prevAlertData.getAt(key)?.getAt('prevAlert') ?: null
        String trendType = prevAlertData.getAt(key)?.getAt('trendType') ?: Constants.Commons.BLANK_STRING
        String freqPriority = prevAlertData.getAt(key)?.getAt('freqPriority') ?: Constants.Commons.BLANK_STRING
        String flags = prevAlertData.getAt(key)?.getAt('flags') ?: Constants.Commons.BLANK_STRING
        Boolean isIngredient = executedConfig.productSelection ? isIngredient(executedConfig.productSelection,data["PRODUCT_NAME"]) : false
        String impEvents = alertService.setImpEventValue(data["PT"], ptCode, data["PRODUCT_NAME"], data["PRODUCT_ID"], eiList,false,executedConfig?.isMultiIngredient,isIngredient)
        Map prevPec = [:]
        if(enabledTrendFlag) {
            for (Map it : allPrevAlertsForTrendFlag) {
                if (it.productId == data["PRODUCT_ID"] && it.ptCode == Integer.valueOf(ptCode) && it.pt == data["PT"]) {
                    prevPec = it
                    break
                }
            }
        }
        DecimalFormat df = new DecimalFormat('#.#####')
        Double freqPeriod = data["NEW_COUNT"] && data["PROD_N_PERIOD"] ? data["NEW_COUNT"] / data["PROD_N_PERIOD"] : 0d
        Double cumFreqPeriod = data["CUMM_COUNT"] && data["PROD_N_CUMUL"] ? data["CUMM_COUNT"] / data["PROD_N_CUMUL"] : 0d


        Map newCountData = new HashMap()
        def newFields = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', true)

        for (int k = 0; k < newFields?.size(); k++) {
            String name = newFields?.get(k).name
            String keyId = newFields?.get(k).keyId

            Map tempMap1 = new HashMap()
            if (keyId != null) {
                keyId = keyId.replace("REPORT_", "").replace("_FLG", "")
                String newCount = "NEW_" + keyId + "_COUNT"
                String cumCount = "CUMM_" + keyId + "_COUNT"
                if (data.containsKey(newCount)) {
                    tempMap1.put("name", name)
                    tempMap1.put("new", data.get(newCount))
                    tempMap1.put("cum", data.get(cumCount))
                    newCountData.put(name, tempMap1)
                }
            }else {
                if(name.equals("smqNarrow")){
                    newCountData.put(name, data.get("SMQ_NARROW_NAME"))
                }else{
                    newCountData.put(name, data.get(name?.toUpperCase()+"_NAME"))
                }
            }
        }
        String newCountJsonString = new ObjectMapper().writeValueAsString(newCountData)

       AggregateCaseAlert aca = new AggregateCaseAlert(
                alertConfiguration: config, executedAlertConfiguration: executedConfig,
                name: executedConfig.name, detectedDate: executedConfig.dateCreated,
                productName: data["PRODUCT_NAME"] ?: Constants.Commons.UNDEFINED, productId: data["PRODUCT_ID"] ?: Constants.Commons.UNDEFINED_NUM,
                soc: data["SOC"] ?: Constants.Commons.UNDEFINED, pt: data["PT"] ?: Constants.Commons.UNDEFINED,
                ptCode: ptCode, isSafety: isSafety,
                newStudyCount: data["NEW_STUDY_COUNT"] ?: 0, cumStudyCount: data["CUMM_STUDY_COUNT"] ?: 0,
                newSponCount: data["NEW_SPON_COUNT"] ?: 0, cumSponCount: data["CUMM_SPON_COUNT"] ?: 0,
                newSeriousCount: data["NEW_SERIOUS_COUNT"] ?: 0, cumSeriousCount: data["CUMM_SERIOUS_COUNT"] ?: 0,
                newFatalCount: data["NEW_FATAL_COUNT"] ?: 0, cumFatalCount: data["CUMM_FATAL_COUNT"] ?: 0,
                createdBy: config.createdBy, modifiedBy: config.modifiedBy,
                dateCreated: executedConfig.dateCreated, lastUpdated: executedConfig.dateCreated,
                positiveRechallenge: data["POSITIVE_RECHALLENGE"] ?: Constants.Commons.BLANK_STRING,
                periodStartDate: dateRangeStartEndDate[0],
                periodEndDate: dateRangeStartEndDate[1],
                positiveDechallenge: data["POSITIVE_DECHALLENGE"] ?: Constants.Commons.BLANK_STRING,
                pregenency: data["PREGENENCY"] ?: Constants.Commons.BLANK_STRING,
                related: data["RELATEDNESS"] ?: Constants.Commons.BLANK_STRING, adhocRun: config.adhocRun,
                isNew: prevAggAlert ? false : true,
                disposition: defaultDisposition,
                impEvents: impEvents,
                pecImpNumHigh: 0, pecImpNumLow: 0,
                //These statistics related dummy values would be overridden from statistics module.
                ebgm: 0.0, eb95: 0.0, eb05: 0.0, rorValue: 0, rorLCI: 0, rorUCI: 0, rorStr: 0, rorStrLCI: 0,
                rorStrUCI: 0, rorMh: 0, prrValue: 0, prrLCI: 0, prrUCI: 0, prrStr: 0, prrStrLCI: 0, prrStrUCI: 0,
                prrMh: 0, pecImpHigh: 0, pecImpLow: 0,
                aValue: 0, bValue: -1, cValue: -1, dValue: -1,
                eValue: -1, rrValue: -1,
                //These are freq and flags
                freqPriority: freqPriority, trendType: trendType, flags: flags,
                newCount: data["NEW_COUNT"] ?: 0, cummCount: data["CUMM_COUNT"] ?: 0,
                newPediatricCount: data["NEW_PEDIA_COUNT"] ?: 0, cummPediatricCount: data["CUMM_PEDIA_COUNT"] ?: 0,
                newInteractingCount: data["NEW_INTERACTING_COUNT"] ?: 0,
                cummInteractingCount: data["CUMM_INTERACTING_COUNT"] ?: 0,
                newGeriatricCount: data["NEW_GERIA_COUNT"] ?: 0,
                cumGeriatricCount: data["CUMM_GERIA_COUNT"] ?: 0,
                newNonSerious: data["NEW_NON_SERIOUS_COUNT"] ?: 0,
                cumNonSerious: data["CUMM_NON_SERIOUS_COUNT"] ?: 0,
                chiSquare: 0, smqCode: ptCodeWithSMQ.size() > 1 ? ptCodeWithSMQ[1] : null,
                newCountFreqCalc: data["NEW_COUNT_FREQ_CALC"],
                prodHierarchyId: data["PROD_HIERARCHY_ID"]?:Constants.Commons.UNDEFINED_NUM_INT_REVIEW,
                eventHierarchyId: data["EVENT_HIERARCHY_ID"]?:Constants.Commons.UNDEFINED_NUM_INT_REVIEW,
                newCountsJson: newCountJsonString
        )
        statisticsService.setDefaultSubGroupValues(aca, executedConfig?.dataMiningVariable as boolean)
        if (executedConfig?.selectedDataSheet && !TextUtils.isEmpty(executedConfig?.selectedDataSheet)) {
            listednessData?.each{
                if(it["ptCode"] == ptCode) {
                    aca.listed = it["listed"]?:Constants.Commons.NO
                }
            }
            if( aca.listed == null || TextUtils.isEmpty(aca.listed)){
                aca.listed = Constants.Commons.NO
            }
        } else {
            aca.listed = data["LISTED"] ?: Constants.Commons.NA_LISTED
        }

        if(enabledTrendFlag){
            aca.newProdCount = data["PROD_N_PERIOD"] ?: 0
            aca.cumProdCount = data["PROD_N_CUMUL"] ?: 0
            aca.reviewedFreqPeriod = prevPec["freqPeriod"]
            aca.reviewedCumFreqPeriod = prevPec["cumFreqPeriod"]
            aca.freqPeriod = Double.valueOf(df.format(freqPeriod))
            aca.cumFreqPeriod = Double.valueOf(df.format(cumFreqPeriod))
        }
        if (aca.impEvents && aca.impEvents != Constants.Commons.BLANK_STRING) {
            String[] impEventList = aca.impEvents?.split(',')
            aca.aggImpEventList = []
            impEventList.each {
                if (it) {
                    aca.aggImpEventList.add(it.trim())
                }
            }
        }
        String peComb = aca.smqCode ? aca.productId + "-" + aca.ptCode + "-" + aca.smqCode : aca.productId + "-" + aca.ptCode + "-" + 'null'
        aca.globalIdentity = dataObjectService.getGlobalProductEventList(executedConfig.id, peComb)

        if (faersData) {
            aca.faersColumns = new JsonBuilder(prepareFaersColumnsMap(otherDataSourcesExecIds.get(Constants.DataSource.FAERS),executedConfig, faersData,eiList,prevAlertDataFaers,aca)).toPrettyString()
        }
        if (evdasData) {
            aca.evdasColumns = new JsonBuilder(prepareEvdasColumnsMap(otherDataSourcesExecIds.get(Constants.DataSource.EUDRA), evdasData)).toPrettyString()
        }
        if (vaersData) {
            aca.vaersColumns = new JsonBuilder(prepareVaersColumnsMap(otherDataSourcesExecIds.get(Constants.DataSource.VAERS), executedConfig, vaersData, aca)).toPrettyString()
        }
        if (vigibaseData) {
            aca.vigibaseColumns = new JsonBuilder(prepareVigibaseColumnsMap(otherDataSourcesExecIds.get(Constants.DataSource.VIGIBASE), executedConfig, vigibaseData, aca)).toPrettyString()
        }

        boolean isEvntGroup = executedConfig.eventGroupSelection && executedConfig.groupBySmq
        setStatisticsScoresValues(aca, executedConfig.productGroupSelection,isEvntGroup)
        setPrrRorScoresValues(aca, executedConfig.productGroupSelection,isEvntGroup, aca.executedAlertConfigurationId, [:], [:], [:])
        setStatsSubgroupingScoresValues(aca, executedConfig.productGroupSelection,isEvntGroup)
        synchronized (AggregateCaseAlertService.class) {
            setWorkflowMgmtStates(aca, config, executedConfig,prevAggAlert)
        }

        aca.initialDueDate = aca.dueDate
        aca.initialDisposition = aca.disposition
        if(enabledTrendFlag) {
            setTrendFlag(aca, prevExecIdsForTrendFlag, reviewedDispositions)
        }
        aca
    }


    def setTrendFlag(AggregateCaseAlert aggAlert, List<Long> prevExecIdsForTrendFlag, List<Long> reviewedDispositions) {
        List totalThresholdMet = fetchPreviousAlertsForTrendFlag(prevExecIdsForTrendFlag, AggregateCaseAlert, reviewedDispositions, true, aggAlert) +
                fetchPreviousAlertsForTrendFlag(prevExecIdsForTrendFlag, ArchivedAggregateCaseAlert, reviewedDispositions, true, aggAlert)
        boolean prevThresholdMet = totalThresholdMet.size() > 0
        List<String> persistentList = Holders.config.signal.agg.trend.flag.fields
        Map alertMap = persistentList.collectEntries { [it, aggAlert."${it}"] }
        String logic = Holders.config.signal.agg.trend.flag.logic
        String finalLogic = """
                            ${Constants.OracleFunctions.NVL}
                            boolean firstEncounter = ${!prevThresholdMet}
                            Map alert = ${alertMap}
                            ${logic}
                            """
        GroovyShell groovyShell = new GroovyShell()
        String retVal = groovyShell.evaluate(finalLogic)
        aggAlert.trendFlag = retVal
    }

    String generateRorValue(Map evdasData){
        String rorValue = "0.000"
        if (evdasData.rorValue) {
            if (evdasData.rorValue[0] == ".") {
                rorValue = "0" + evdasData.rorValue
            } else {
                rorValue = evdasData.rorValue
            }
        }
        rorValue
    }


    AggregateCaseAlert createAggAlertForOtherDataSources(Map data, Configuration config, ExecutedConfiguration executedConfig, List<Map> allPrevAlerts,Map prevAlertDataFaers, List<Long> reviewedDispositions,
                                              List<Date> dateRangeStartEndDate, Disposition defaultDisposition, Map<String, Long> otherDataSourcesExecIds,Map evdasData, Map vigibaseData, List eiList,String dataSourceType,List listednessData=[]) {

        boolean prevAggAlert
        String ptCode
        Integer productId
        String pt
        String productName
        String soc
        String smqCode = null
        String flags
        AggregateCaseAlert prevAca = null
        if (dataSourceType in [Constants.DataSource.FAERS, Constants.DataSource.VAERS, Constants.DataSource.VIGIBASE,Constants.DataSource.JADER]) {
            productName = data["PRODUCT_NAME"]
            productId = data["PRODUCT_ID"]
            String[] ptCodeWithSMQ = data["PT_CODE"] ? String.valueOf(data["PT_CODE"]).split(Constants.Commons.DASH_STRING, 2) : ["0"]
            ptCode = ptCodeWithSMQ ? ptCodeWithSMQ[0] ? ptCodeWithSMQ[0] : 0 : Constants.Commons.UNDEFINED_NUM
            pt = data.PT
            soc = data.SOC
            smqCode = ptCodeWithSMQ.size() > 1 ? ptCodeWithSMQ[1] : null
            String key = executedConfig.id + '-' + data["PRODUCT_ID"] + '-' + "${ptCodeWithSMQ.size() > 1 ? data["PT_CODE"] : ptCode}"
            flags = prevAlertDataFaers.getAt(key)?.getAt('flags') ?: Constants.Commons.BLANK_STRING
            prevAggAlert = prevAlertDataFaers.getAt(key)?.getAt('prevAlert') ? true: false
            prevAca = prevAlertDataFaers.getAt(key)?.getAt('prevAlert') ? prevAlertDataFaers.getAt(key)?.getAt('prevAlert') : null

        } else {
            productName = data.substance
            productId = data.substanceId as Integer
            ptCode = data.ptCode
            pt = data.pt
            soc = data.soc
        }
        if (!prevAggAlert) {
            prevAggAlert = allPrevAlerts.any {
                it.productId == productId && it.ptCode.toString() == ptCode
            }
        }
        if (dataSourceType != Constants.DataSource.FAERS) {
            flags = prevAggAlert ? "Previously Reviewed" : "New"
        }

        boolean isEvdas = ((dataSourceType == Constants.DataSource.EVDAS) || (dataSourceType == Constants.DataSource.EUDRA))
        String impEvents = alertService.setImpEventValue(pt, ptCode, productName, productId, eiList, isEvdas)
        AggregateCaseAlert aca = new AggregateCaseAlert(
                alertConfiguration: config, executedAlertConfiguration: executedConfig,
                name: executedConfig.name, detectedDate: executedConfig.dateCreated,
                productName: productName ?: Constants.Commons.DASH_STRING, productId: productId ,
                soc: soc ?: Constants.Commons.UNDEFINED, pt: pt ?: Constants.Commons.UNDEFINED,
                ptCode: ptCode, isSafety: false,
                newStudyCount: Constants.Commons.UNDEFINED_NUM_INT_REVIEW, cumStudyCount: Constants.Commons.UNDEFINED_NUM_INT_REVIEW,
                newSponCount: Constants.Commons.UNDEFINED_NUM_INT_REVIEW, cumSponCount: Constants.Commons.UNDEFINED_NUM_INT_REVIEW,
                newSeriousCount: Constants.Commons.UNDEFINED_NUM_INT_REVIEW, cumSeriousCount: Constants.Commons.UNDEFINED_NUM_INT_REVIEW,
                newFatalCount: Constants.Commons.UNDEFINED_NUM_INT_REVIEW, cumFatalCount: Constants.Commons.UNDEFINED_NUM_INT_REVIEW,
                createdBy: config.createdBy, modifiedBy: config.modifiedBy,
                dateCreated: executedConfig.dateCreated, lastUpdated: executedConfig.dateCreated,
                positiveRechallenge: Constants.Commons.DASH_STRING,
                periodStartDate: dateRangeStartEndDate[0],
                periodEndDate: dateRangeStartEndDate[1],
                positiveDechallenge: Constants.Commons.DASH_STRING,
                listed:(executedConfig?.selectedDataSheet && !TextUtils.isEmpty(executedConfig?.selectedDataSheet))?Constants.Commons.NO:Constants.Commons.DASH_STRING, pregenency: Constants.Commons.DASH_STRING,
                related: Constants.Commons.DASH_STRING, adhocRun: config.adhocRun,
                isNew: !prevAggAlert,
                disposition: defaultDisposition,
                impEvents: impEvents,
                //These statistics related dummy values would be overridden from statistics module.
                ebgm: Constants.Commons.UNDEFINED_NUM_INT_REVIEW, eb95: Constants.Commons.UNDEFINED_NUM_INT_REVIEW, eb05: Constants.Commons.UNDEFINED_NUM_INT_REVIEW,
                rorValue: Constants.Commons.UNDEFINED_NUM_INT_REVIEW, rorLCI: Constants.Commons.UNDEFINED_NUM_INT_REVIEW, rorUCI: Constants.Commons.UNDEFINED_NUM_INT_REVIEW,
                rorStr: Constants.Commons.UNDEFINED_NUM_INT_REVIEW, rorStrLCI: Constants.Commons.UNDEFINED_NUM_INT_REVIEW,
                rorStrUCI: Constants.Commons.UNDEFINED_NUM_INT_REVIEW, rorMh: Constants.Commons.UNDEFINED_NUM_INT_REVIEW, prrValue: Constants.Commons.UNDEFINED_NUM_INT_REVIEW,
                prrLCI: Constants.Commons.UNDEFINED_NUM_INT_REVIEW, prrUCI: Constants.Commons.UNDEFINED_NUM_INT_REVIEW, prrStr: Constants.Commons.UNDEFINED_NUM_INT_REVIEW,
                prrStrLCI: Constants.Commons.UNDEFINED_NUM_INT_REVIEW, prrStrUCI: Constants.Commons.UNDEFINED_NUM_INT_REVIEW,
                prrMh: Constants.Commons.UNDEFINED_NUM_INT_REVIEW, pecImpHigh: Constants.Commons.UNDEFINED_NUM_INT_REVIEW, pecImpLow: Constants.Commons.UNDEFINED_NUM_INT_REVIEW,
                aValue: 0,bValue: -1,cValue: -1,dValue: -1,eValue: -1,rrValue: -1,
                //These are freq and flags
                flags: flags,
                newCount: Constants.Commons.UNDEFINED_NUM_INT_REVIEW, cummCount: Constants.Commons.UNDEFINED_NUM_INT_REVIEW,
                newPediatricCount: Constants.Commons.UNDEFINED_NUM_INT_REVIEW, cummPediatricCount: Constants.Commons.UNDEFINED_NUM_INT_REVIEW,
                newInteractingCount: Constants.Commons.UNDEFINED_NUM_INT_REVIEW, cummInteractingCount: Constants.Commons.UNDEFINED_NUM_INT_REVIEW,
                chiSquare: Constants.Commons.UNDEFINED_NUM_INT_REVIEW, smqCode: smqCode,
                newGeriatricCount: Constants.Commons.UNDEFINED_NUM_INT_REVIEW,
                cumGeriatricCount: Constants.Commons.UNDEFINED_NUM_INT_REVIEW,
                newNonSerious: Constants.Commons.UNDEFINED_NUM_INT_REVIEW,
                cumNonSerious: Constants.Commons.UNDEFINED_NUM_INT_REVIEW,
                prodHierarchyId: data["PROD_HIERARCHY_ID"]?:Constants.Commons.UNDEFINED_NUM_INT_REVIEW,
                eventHierarchyId: data["EVENT_HIERARCHY_ID"]?:Constants.Commons.UNDEFINED_NUM_INT_REVIEW
        )
        if (executedConfig?.selectedDataSheet && !TextUtils.isEmpty(executedConfig?.selectedDataSheet)) {
            listednessData?.each{
                if(it["ptCode"] == ptCode) {
                    aca.listed = it["listed"]?:Constants.Commons.NO
                }
            }
            if( aca.listed == null || TextUtils.isEmpty(aca.listed) ){
                aca.listed = Constants.Commons.NO
            }
        }
        if (aca.impEvents && aca.impEvents != Constants.Commons.BLANK_STRING) {
            String[] impEventList = aca.impEvents?.split(',')
            aca.aggImpEventList = []
            impEventList.each {
                if (it) {
                    aca.aggImpEventList.add(it.trim())
                }
            }
        }

        String peComb = aca.smqCode ? aca.productId + "-" + aca.ptCode + "-" + aca.smqCode : aca.productId + "-" + aca.ptCode + "-" + 'null'
        aca.globalIdentity = dataObjectService.getGlobalProductEventList(executedConfig.id, peComb)

        if (dataSourceType == Constants.DataSource.FAERS) {
            aca.faersColumns = new JsonBuilder(prepareFaersColumnsMap(otherDataSourcesExecIds.get(dataSourceType),executedConfig, data,eiList,prevAlertDataFaers,aca)).toPrettyString()
        }

        if (evdasData || dataSourceType == Constants.DataSource.EUDRA) {
            aca.evdasColumns = new JsonBuilder(prepareEvdasColumnsMap(otherDataSourcesExecIds.get(Constants.DataSource.EUDRA), evdasData ?: data)).toPrettyString()
            if(TextUtils.isEmpty(executedConfig?.selectedDataSheet)){
                if(aca.getEvdasColumnValue("listedEvdas") == null){
                    aca.listed = Constants.Commons.NA_LISTED
                }else{
                    aca.listed = getEvdasListednessValue(aca.getEvdasColumnValue("listedEvdas") as Boolean)
                }
            }
        }
        if(dataSourceType == Constants.DataSource.VAERS){
            aca.vaersColumns = new JsonBuilder(prepareVaersColumnsMap(otherDataSourcesExecIds.get(Constants.DataSource.VAERS),executedConfig, data,aca)).toPrettyString()
        }
        if(vigibaseData || dataSourceType == Constants.DataSource.VIGIBASE){
            aca.vigibaseColumns = new JsonBuilder(prepareVigibaseColumnsMap(otherDataSourcesExecIds.get(Constants.DataSource.VIGIBASE),executedConfig, vigibaseData ?: data,aca)).toPrettyString()
        }
        if(dataSourceType == Constants.DataSource.JADER){
            aca.jaderColumns = new JsonBuilder(prepareJaderColumnsMap(otherDataSourcesExecIds.get(Constants.DataSource.JADER),executedConfig, data,aca)).toPrettyString()
        }

        synchronized (AggregateCaseAlertService.class) {
            setWorkflowMgmtStates(aca, config, executedConfig, prevAca)
        }
        aca.initialDueDate = aca.dueDate
        aca.initialDisposition = aca.disposition
        aca
    }
    String getEvdasListednessValue(Boolean listed) {
        switch (listed) {
            case true:
                return "Yes"
            case false:
                return "No"
            default:
                return "N/A"
        }
    }
    Map prepareFaersColumnsMap(Long execConfigId,ExecutedConfiguration executedConfiguration, Map faersData, List eiList, Map prevAlertDataFaers, AggregateCaseAlert aca) {
        String[] ptCodeWithSMQIntegratedReview = faersData["PT_CODE"] ? String.valueOf(faersData["PT_CODE"]).split(Constants.Commons.DASH_STRING, 2) : ["0"]
        String ptCodeIntegratedReview = ptCodeWithSMQIntegratedReview ? ptCodeWithSMQIntegratedReview[0] ? ptCodeWithSMQIntegratedReview[0] : 0 : Constants.Commons.UNDEFINED_NUM
        String key = execConfigId + '-' + faersData["PRODUCT_ID"] + '-' + "${ptCodeWithSMQIntegratedReview.size() > 1 ? faersData["PT_CODE"] : ptCodeIntegratedReview}"

        Map faersColumnMap = [executedAlertConfigurationId: execConfigId, productId: faersData["PRODUCT_ID"], ptCode: ptCodeIntegratedReview,
                              smqCode                     : ptCodeWithSMQIntegratedReview.size() > 1 ? ptCodeWithSMQIntegratedReview[1] : null,
                              newCountFaers               : faersData["NEW_COUNT"] ?: 0, cummCountFaers: faersData["CUMM_COUNT"] ?: 0, newSeriousCountFaers: faersData["NEW_SERIOUS_COUNT"] ?: 0,
                              cumSeriousCountFaers        : faersData["CUMM_SERIOUS_COUNT"] ?: 0, ebgmFaers: 0, eb95Faers: 0, eb05Faers: 0,
                              newSponCountFaers           : faersData["NEW_SPON_COUNT"], cumSponCountFaers: faersData["CUMM_SPON_COUNT"],
                              newStudyCountFaers          : faersData["NEW_STUDY_COUNT"], cumStudyCountFaers: faersData["CUMM_STUDY_COUNT"],
                              newInteractingCountFaers    : faersData["NEW_INTERACTING_COUNT"], cummInteractingCountFaers: faersData["CUMM_INTERACTING_COUNT"],
                              newGeriatricCountFaers      : faersData["NEW_GERIA_COUNT"],
                              cumGeriatricCountFaers      : faersData["CUMM_GERIA_COUNT"],
                              newNonSeriousFaers          : faersData["NEW_NON_SERIOUS_COUNT"],
                              cumNonSeriousFaers          : faersData["CUMM_NON_SERIOUS_COUNT"],
                              newFatalCountFaers          : faersData["NEW_FATAL_COUNT"], cumFatalCountFaers: faersData["CUMM_FATAL_COUNT"],
                              listedFaers                 : faersData["LISTED"],
                              impEventsFaers              : alertService.setImpEventValue(faersData["PT"], ptCodeIntegratedReview, aca.productName, faersData["PRODUCT_ID"],  eiList),
                              positiveDechallengeFaers    : faersData["POSITIVE_DECHALLENGE"] ?: Constants.Commons.BLANK_STRING,
                              positiveRechallengeFaers    : faersData["POSITIVE_RECHALLENGE"] ?: Constants.Commons.BLANK_STRING,
                              pregenencyFaers             : faersData["PREGENENCY"] ?: Constants.Commons.BLANK_STRING,
                              relatedFaers                : faersData["RELATEDNESS"] ?: Constants.Commons.BLANK_STRING,
                              trendTypeFaers              : prevAlertDataFaers.getAt(key)?.getAt('trendType') ?:  Constants.Commons.BLANK_STRING,
                              freqPriorityFaers           : prevAlertDataFaers.getAt(key)?.getAt('freqPriority') ?:  Constants.Commons.BLANK_STRING,
                              newPediatricCountFaers      : faersData["NEW_PEDIA_COUNT"], cummPediatricCountFaers: faersData["CUMM_PEDIA_COUNT"]

        ]

        boolean isEvntGroup = executedConfiguration.eventGroupSelection && executedConfiguration.groupBySmq

        setStatisticsScoresValues(faersColumnMap, executedConfiguration.productGroupSelection,isEvntGroup)
        setPrrRorScoresValues(aca, executedConfiguration.productGroupSelection,isEvntGroup, execConfigId, faersColumnMap, [:])
        setStatsSubgroupingScoresValuesFaers(faersColumnMap, aca, executedConfiguration.productGroupSelection,isEvntGroup)
        faersColumnMap
    }

    Map prepareVaersColumnsMap(Long execConfigId, ExecutedConfiguration executedConfiguration, Map vaersData, AggregateCaseAlert aca) {
        String[] ptCodeWithSMQIntegratedReview = vaersData["PT_CODE"] ? String.valueOf(vaersData["PT_CODE"]).split(Constants.Commons.DASH_STRING, 2) : ["0"]
        String ptCodeIntegratedReview = ptCodeWithSMQIntegratedReview ? ptCodeWithSMQIntegratedReview[0] ? ptCodeWithSMQIntegratedReview[0] : 0 : Constants.Commons.UNDEFINED_NUM
        Map vaersColumnMap = [executedAlertConfigurationId: execConfigId,
                              productId                   : vaersData["PRODUCT_ID"],
                              ptCode                      : ptCodeIntegratedReview,
                              smqCode                     : ptCodeWithSMQIntegratedReview.size() > 1 ? ptCodeWithSMQIntegratedReview[1] : null,
                              newCountVaers               : vaersData["NEW_COUNT"] ?: 0,
                              cummCountVaers              : vaersData["CUMM_COUNT"] ?: 0,
                              newSeriousCountVaers        : vaersData["NEW_SERIOUS_COUNT"] ?: 0,
                              cumSeriousCountVaers        : vaersData["CUMM_SERIOUS_COUNT"] ?: 0,
                              ebgmVaers                   : 0,
                              eb95Vaers                   : 0,
                              eb05Vaers                   : 0,
                              newGeriatricCountVaers      : vaersData["NEW_GERIA_COUNT"],
                              cumGeriatricCountVaers      : vaersData["CUMM_GERIA_COUNT"],
                              newFatalCountVaers          : vaersData["NEW_FATAL_COUNT"],
                              cumFatalCountVaers          : vaersData["CUMM_FATAL_COUNT"],
                              newPediatricCountVaers      : vaersData["NEW_PEDIA_COUNT"],
                              cummPediatricCountVaers     : vaersData["CUMM_PEDIA_COUNT"]
                ]
        boolean isEvntGroup = executedConfiguration.eventGroupSelection && executedConfiguration.groupBySmq
        setStatisticsScoresValues(vaersColumnMap, executedConfiguration.productGroupSelection,isEvntGroup, Constants.DataSource.VAERS)
        setPrrRorScoresValues(aca, executedConfiguration.productGroupSelection, isEvntGroup, execConfigId, [:],vaersColumnMap)
        vaersColumnMap
    }

    Map prepareVigibaseColumnsMap(Long execConfigId, ExecutedConfiguration executedConfiguration, Map vigibaseData, AggregateCaseAlert aca) {
        String[] ptCodeWithSMQIntegratedReview = vigibaseData["PT_CODE"] ? String.valueOf(vigibaseData["PT_CODE"]).split(Constants.Commons.DASH_STRING, 2) : ["0"]
        String ptCodeIntegratedReview = ptCodeWithSMQIntegratedReview ? ptCodeWithSMQIntegratedReview[0] ? ptCodeWithSMQIntegratedReview[0] : 0 : Constants.Commons.UNDEFINED_NUM
        Map vigibaseColumnMap = [executedAlertConfigurationId: execConfigId,
                                 productId                   : vigibaseData["PRODUCT_ID"],
                                 ptCode                      : ptCodeIntegratedReview,
                                 smqCode                     : ptCodeWithSMQIntegratedReview.size() > 1 ? ptCodeWithSMQIntegratedReview[1] : null,
                                 newCountVigibase               : vigibaseData["NEW_COUNT"] ?: 0,
                                 cummCountVigibase              : vigibaseData["CUMM_COUNT"] ?: 0,
                                 newSeriousCountVigibase        : vigibaseData["NEW_SERIOUS_COUNT"] ?: 0,
                                 cumSeriousCountVigibase        : vigibaseData["CUMM_SERIOUS_COUNT"] ?: 0,
                                 ebgmVigibase                   : 0,
                                 eb95Vigibase                   : 0,
                                 eb05Vigibase                   : 0,
                                 newGeriatricCountVigibase      : vigibaseData["NEW_GERIA_COUNT"],
                                 cumGeriatricCountVigibase      : vigibaseData["CUMM_GERIA_COUNT"],
                                 newFatalCountVigibase          : vigibaseData["NEW_FATAL_COUNT"],
                                 cumFatalCountVigibase          : vigibaseData["CUMM_FATAL_COUNT"],
                                 newPediatricCountVigibase      : vigibaseData["NEW_PEDIA_COUNT"],
                                 cummPediatricCountVigibase     : vigibaseData["CUMM_PEDIA_COUNT"]
        ]
        boolean isEvntGroup = executedConfiguration.eventGroupSelection && executedConfiguration.groupBySmq
        setStatisticsScoresValues(vigibaseColumnMap, executedConfiguration.productGroupSelection,isEvntGroup, Constants.DataSource.VIGIBASE)
        setPrrRorScoresValues(aca, executedConfiguration.productGroupSelection, isEvntGroup, execConfigId, [:], [:], vigibaseColumnMap)
        vigibaseColumnMap
    }

    Map prepareJaderColumnsMap(Long execConfigId, ExecutedConfiguration executedConfiguration, Map jaderData, AggregateCaseAlert aca) {
        String[] ptCodeWithSMQIntegratedReview = jaderData["PT_CODE"] ? String.valueOf(jaderData["PT_CODE"]).split(Constants.Commons.DASH_STRING, 2) : ["0"]
        String ptCodeIntegratedReview = ptCodeWithSMQIntegratedReview ? ptCodeWithSMQIntegratedReview[0] ? ptCodeWithSMQIntegratedReview[0] : 0 : Constants.Commons.UNDEFINED_NUM
        Map jaderColumnMap = [executedAlertConfigurationId: execConfigId,
                              productId                   : jaderData["PRODUCT_ID"],
                              ptCode                      : ptCodeIntegratedReview,
                              smqCode                     : ptCodeWithSMQIntegratedReview.size() > 1 ? ptCodeWithSMQIntegratedReview[1] : null,
                              newCountJader              : jaderData["NEW_COUNT"] ?: 0,
                              cumCountJader             : jaderData["CUMM_COUNT"] ?: 0,
                              newSeriousCountJader       : jaderData["NEW_SERIOUS_COUNT"] ?: 0,
                              cumSeriousCountJader       : jaderData["CUMM_SERIOUS_COUNT"] ?: 0,
                              ebgmJader                  : 0,
                              eb95Jader                  : 0,
                              eb05Jader                  : 0,
                              newGeriatricCountJader     : jaderData["NEW_GERIA_COUNT"],
                              cumGeriatricCountJader     : jaderData["CUMM_GERIA_COUNT"],
                              newFatalCountJader         : jaderData["NEW_FATAL_COUNT"],
                              cumFatalCountJader         : jaderData["CUMM_FATAL_COUNT"],
                              newPediatricCountJader     : jaderData["NEW_PEDIA_COUNT"],
                              cumPediatricCountJader    : jaderData["CUMM_PEDIA_COUNT"]
        ]
        boolean isEvntGroup = executedConfiguration.eventGroupSelection && executedConfiguration.groupBySmq
        setStatisticsScoresValues(jaderColumnMap, executedConfiguration.productGroupSelection,isEvntGroup, Constants.DataSource.JADER)
        setPrrRorScoresValues(aca, executedConfiguration.productGroupSelection, isEvntGroup, execConfigId, [:], [:],[:],jaderColumnMap)
        jaderColumnMap
    }

    Map prepareEvdasColumnsMap(Long execConfigId, Map evdasData) {
        [execConfigId                : execConfigId, newEvEvdas: evdasData["newEv"], totalEvEvdas: evdasData["totalEv"], dmeImeEvdas: evdasData["dmeIme"],
         sdrEvdas                    : evdasData["sdr"], rorValueEvdas: generateRorValue(evdasData), newPaedEvdas: evdasData.newPaed, newEvLink: evdasData.newEvLink,
         totPaedEvdas                : evdasData.totPaed, totalFatalEvdas: evdasData.totalFatal, newFatalEvdas: evdasData.newFatal, totalEvLink: evdasData.totalEvLink,
         sdrPaedEvdas                : evdasData.sdrPaed, changesEvdas: evdasData.changes ? evdasData.changes : '-',
         hlgtEvdas                   : evdasData.hlgt, hltEvdas: evdasData.hlt, smqNarrowEvdas: evdasData.smqNarrow,
         impEventsEvdas              : evdasData.impEvents, newEeaEvdas: evdasData.newEea, totEeaEvdas: evdasData.totEea, newHcpEvdas: evdasData.newHcp,
         totHcpEvdas                 : evdasData.totHcp, newSeriousEvdas: evdasData.newSerious, totalSeriousEvdas: evdasData.totalSerious, newMedErrEvdas: evdasData.newMedErr,
         totMedErrEvdas              : evdasData.totMedErr, newObsEvdas: evdasData.newObs, totObsEvdas: evdasData.totObs,
         newRcEvdas                  : evdasData.newRc, totRcEvdas: evdasData.totRc, newLitEvdas: evdasData.newLit, totalLitEvdas: evdasData.totalLit,
         ratioRorPaedVsOthersEvdas   : evdasData.ratioRorPaedVsOthers, newGeriaEvdas: evdasData.newGeria, totGeriaEvdas: evdasData.totGeria,
         ratioRorGeriatrVsOthersEvdas: evdasData.ratioRorGeriatrVsOthers, sdrGeratrEvdas: evdasData.sdrGeratr,
         newSpontEvdas               : evdasData.newSpont, totSpontEvdas: evdasData.totSpont, totSpontEuropeEvdas: evdasData.totSpontEurope,
         totSpontNAmericaEvdas       : evdasData.totSpontNAmerica, totSpontJapanEvdas: evdasData.totSpontJapan,
         totSpontAsiaEvdas           : evdasData.totSpontAsia, totSpontRestEvdas: evdasData.totSpontRest,
         europeRorEvdas              : evdasData.europeRor, northAmericaRorEvdas: evdasData.northAmericaRor,
         japanRorEvdas               : evdasData.japanRor, asiaRorEvdas: evdasData.asiaRor, restRorEvdas: evdasData.restRor,
         listedEvdas                 : evdasData.listedness,allRorEvdas: generateRorValue(evdasData)]
    }

    def getAlertConfigObjectFaers(String name, User owner) {
        try {
            Long configId = Configuration.createCriteria().get {
                projections {
                    property("id")
                }
                eq('owner', owner)
                eq('name', name)
                eq('selectedDatasource', Constants.DataSource.FAERS)
                maxResults(1)
            } as Long
            return configId
        } catch (Throwable th) {
            log.error(th.getMessage())
            return 0L
        }
    }

    void updateIntegratedConfiguration(Configuration configurationInstance) {
        if(configurationInstance?.selectedDatasource?.contains(Constants.DataSource.EUDRA)){
            EvdasConfiguration evdasConfiguration = EvdasConfiguration.findByIntegratedConfigurationId(configurationInstance?.id)
            if(!evdasConfiguration) {
               evdasConfiguration = new EvdasConfiguration()
            }
            evdasAlertExecutionService.createConfigForIntegratedReview(configurationInstance , evdasConfiguration)
        }
        if(configurationInstance?.selectedDatasource?.contains(Constants.DataSource.FAERS) && (!configurationInstance.selectedDatasource.startsWith(Constants.DataSource.FAERS) )) {
            Configuration configurationFaers = Configuration.findByIntegratedConfigurationId(configurationInstance?.id)
            if(!configurationFaers) {
                configurationFaers = new Configuration()
            }
            reportExecutorService.createConfigForIntegratedReview(configurationInstance , configurationFaers)
        }
        if(configurationInstance?.selectedDatasource?.contains(Constants.DataSource.VAERS) && (!configurationInstance.selectedDatasource.startsWith(Constants.DataSource.VAERS) )) {
            Configuration configurationVaers = Configuration.findByIntegratedConfigurationId(configurationInstance?.id)
            if(!configurationVaers) {
                configurationVaers = new Configuration()
            }
            reportExecutorService.createConfigForIntegratedReviewVaers(configurationInstance , configurationVaers)
        }
        if(configurationInstance?.selectedDatasource?.contains(Constants.DataSource.VIGIBASE) && (!configurationInstance.selectedDatasource.startsWith(Constants.DataSource.VIGIBASE) )) {
            Configuration configurationVigibase = Configuration.findByIntegratedConfigurationId(configurationInstance?.id)
            if(!configurationVigibase) {
                configurationVigibase = new Configuration()
            }
            reportExecutorService.createConfigForIntegratedReviewVigibase(configurationInstance , configurationVigibase)
        }
    }

    void buildTagList(def tag, List tagsAndSubTags, List pvsGlobalTagList, List pvsAlertTagList, String dataSource, Long execConfigId, String peComb, Map keyIdMap=null){
        Boolean isSubTagPresent = false
        Long tagId = tagsAndSubTags.find { it.text == tag.tagText}?.id
        String tagName = tag.tagText
        String alertId = peComb
        Long globalId = dataObjectService.getGlobalProductEventList(execConfigId, peComb)?.id
        if(globalId) {
            if (tag.alert) {
                alertId = (dataObjectService.getGlobalProductAlertMap(execConfigId, peComb)?.id)?.toString()
            }
            tag.subTags.each { subTagName ->
                isSubTagPresent = true
                Map subTag = tagsAndSubTags.find { it.text == subTagName && it.parentId == tagId }
                Long subTagId = subTag?.id
                if (tag.alert) {
                    PvsAlertTag pvsAlertTag = pvsAlertTagService.fetchPvsAlertTagObject(tagName, tagId, dataObjectService.getGlobalProductAlertMap(execConfigId, peComb)?.id, Constants.AlertConfigType.AGGREGATE_CASE_ALERT, subTagName,
                            subTagId, true, false, execConfigId)
                    pvsAlertTagList.add(pvsAlertTag)
                } else {
                    PvsGlobalTag pvsGlobalTag = pvsGlobalTagService.fetchPvsGlobalTagObject(tagName, tagId, globalId, Constants.AlertConfigType.AGGREGATE_CASE_ALERT, subTagName,
                            subTagId, true, false, execConfigId)
                    pvsGlobalTagList.add(pvsGlobalTag)
                }
            }
            if (!isSubTagPresent) {
                if (tag.alert) {
                    PvsAlertTag pvsAlertTag = pvsAlertTagService.fetchPvsAlertTagObject(tagName, tagId, dataObjectService.getGlobalProductAlertMap(execConfigId, peComb)?.id, Constants.AlertConfigType.AGGREGATE_CASE_ALERT, null,
                            null, true, false, execConfigId)
                    pvsAlertTagList.add(pvsAlertTag)
                } else {
                    PvsGlobalTag pvsGlobalTag = pvsGlobalTagService.fetchPvsGlobalTagObject(tagName, tagId, dataObjectService.getGlobalProductEventList(execConfigId, peComb)?.id, Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
                            null, null, true, false, execConfigId)
                    pvsGlobalTagList.add(pvsGlobalTag)
                }
            }
        }
    }
    List getProductEventAlertMapping(List alertData){
        List<Map> productEventAlertMapping = []
        alertData.each {
            if(it.globalIdentity) {
                productEventAlertMapping.add([id: it.id, productEventComb: it.globalIdentity.productEventComb])
            }
        }
        productEventAlertMapping
    }
    Map getStratificationValues(String selectedDatasource){
        Sql pvaSql
        Sql faersSql
        Sql vaersSql
        Sql vigibaseSql
        Sql jaderSql
        String checkStatement
        String stratificationStatement
        Map sqlMap = [:]

        ConcurrentHashMap<String, ConcurrentHashMap> map_EBGM =["pva"     :[age:[], gender:[], receiptYear:[], ageSubGroup:[], genderSubGroup:[],countrySubGroup:[],regionSubGroup:[], "isEBGM": false],
                                                                "faers"   :[age:[],gender:[],receiptYear:[],ageSubGroup:[],genderSubGroup:[], "isEBGM": false],
                                                                "vaers"   :[age:[],gender:[],receiptYear:[],ageSubGroup:[],genderSubGroup:[], "isEBGM": false],
                                                                "vigibase":[age:[],gender:[],receiptYear:[],ageSubGroup:[],genderSubGroup:[], "isEBGM": false],
                                                                "jader":[age:[],gender:[],receiptYear:[],ageSubGroup:[],genderSubGroup:[], "isEBGM": false]]
        ConcurrentHashMap<String, ConcurrentHashMap> map_PRR =["pva":[age:[],gender:[],receiptYear:[],ageSubGroup:[],genderSubGroup:[], "isPRR": false],
                       "faers":[age:[],gender:[],receiptYear:[],ageSubGroup:[],genderSubGroup:[], "isPRR": false],
                       "vaers":[age:[],gender:[],receiptYear:[],ageSubGroup:[],genderSubGroup:[], "isPRR": false],
                       "jader":[age:[],gender:[],receiptYear:[],ageSubGroup:[],genderSubGroup:[], "isPRR": false],
                        "vigibase":[age:[],gender:[],receiptYear:[],ageSubGroup:[],genderSubGroup:[], "isPRR": false]]

        try {
            selectedDatasource.split(",").minus("eudra").each { dataSource ->
                String queryDataSource = ""
                if(dataSource == Constants.DataSource.PVA)
                    queryDataSource += "PVA"
                else if(dataSource == Constants.DataSource.EUDRA || dataSource == Constants.DataSource.EVDAS)
                    queryDataSource += "EVDAS"
                else if(dataSource == Constants.DataSource.FAERS)
                    queryDataSource += "FAERS"
                else if(dataSource == Constants.DataSource.VAERS)
                    queryDataSource += "VAERS"
                else if(dataSource == Constants.DataSource.VIGIBASE)
                    queryDataSource += "VIGIBASE"
                else if(dataSource == Constants.DataSource.JADER)
                    queryDataSource += "JADER"
                else
                    queryDataSource = dataSource

                queryDataSource = queryDataSource + '-DB'

                if (dataSource == "pva") {
                    pvaSql = new Sql(dataSource_pva)
                    sqlMap.put(dataSource,pvaSql)
                } else if(dataSource == "faers"){
                    faersSql = new Sql(dataSource_faers)
                    sqlMap.put(dataSource,faersSql)
                } else if(dataSource == "vaers"){
                    vaersSql = new Sql(dataSource_vaers)
                    sqlMap.put(dataSource,vaersSql)
                } else if(dataSource == "vigibase"){
                    vigibaseSql = new Sql(dataSource_vigibase)
                    sqlMap.put(dataSource,vigibaseSql)
                } else if(dataSource == "jader"){
                    jaderSql = new Sql(dataSource_jader)
                    sqlMap.put(dataSource,jaderSql)
                }

                ExecutorService executorService = Executors.newFixedThreadPool(3)
                List<Callable<Boolean>> callables = new ArrayList<Callable<Boolean>>()

                    callables.add(new Callable<Boolean>() {
                        @Override
                        Boolean call() throws Exception {
                            Integer count = 0
                            checkStatement = SignalQueryHelper.statification_enabled_ebgm()
                            sqlMap.get(dataSource).eachRow(checkStatement, []) { resultSetObj ->
                                count = resultSetObj."count(1)"
                            }
                            if (count > 0) {
                                map_EBGM."${dataSource}".isEBGM = true
                                if(dataSource == "jader"){
                                    stratificationStatement = SignalQueryHelper.stratification_values_ebgm_jader(queryDataSource)
                                }else {
                                    stratificationStatement = SignalQueryHelper.stratification_values_ebgm(queryDataSource)
                                }
                                sqlMap.get(dataSource).eachRow(stratificationStatement, []) { resultSetObj ->

                                    if (resultSetObj."PARAM" == "age") {
                                        map_EBGM."${dataSource}".age.add(resultSetObj."PARAM_VALUE")
                                    } else if (resultSetObj."PARAM" == "gender") {
                                        map_EBGM."${dataSource}".gender.add(resultSetObj."PARAM_VALUE")
                                    } else if (resultSetObj."PARAM" == "receipt_years") {
                                        map_EBGM."${dataSource}".receiptYear.add(resultSetObj."PARAM_VALUE")
                                    }
                                }
                            }
                            return true
                        }
                    })

                callables.add(new Callable<Boolean>() {
                    @Override
                    Boolean call() throws Exception {
                        Integer count = 0
                        checkStatement = SignalQueryHelper.statification_enabled(queryDataSource)
                        sqlMap.get(dataSource).eachRow(checkStatement, []) { resultSetObj ->
                            count = resultSetObj."count(1)"
                        }
                        if (count > 0) {
                            map_PRR."${dataSource}".isPRR = true
                            if(dataSource == "jader"){
                                stratificationStatement = SignalQueryHelper.stratification_values_jader(queryDataSource)
                            }else {
                                stratificationStatement = SignalQueryHelper.stratification_values(queryDataSource)
                            }
                            sqlMap.get(dataSource).eachRow(stratificationStatement, []) { resultSetObj ->

                                if (resultSetObj."PARAM" == "age") {
                                    map_PRR."${dataSource}".age.add(resultSetObj."PARAM_VALUE")
                                } else if (resultSetObj."PARAM" == "gender") {
                                    map_PRR."${dataSource}".gender.add(resultSetObj."PARAM_VALUE")
                                } else if (resultSetObj."PARAM" == "receipt_years") {
                                    map_PRR."${dataSource}".receiptYear.add(resultSetObj."PARAM_VALUE")
                                }
                            }
                        }
                        return true
                    }
                })

                callables.add(new Callable<Boolean>() {
                    @Override
                    Boolean call() throws Exception {
                        Integer count = 0
                        checkStatement = SignalQueryHelper.stratification_subgroup_enabled(queryDataSource)
                        sqlMap.get(dataSource).eachRow(checkStatement, []) { resultSetObj ->
                            count = resultSetObj."count(1)"
                        }
                        map_EBGM."${dataSource}".isSubGroup = false
                        if (count > 0) {
                            map_EBGM."${dataSource}".isSubGroup = true
                            if(dataSource == Constants.DataSource.PVA){
                                stratificationStatement = SignalQueryHelper.stratification_subgroup_values(queryDataSource)
                            }else{
                                stratificationStatement = SignalQueryHelper.stratification_subgroup_values_faers(queryDataSource)
                            }
                            sqlMap.get(dataSource).eachRow(stratificationStatement, []) { resultSetObj ->

                                if (resultSetObj."PARAM" == "age_subgroup") {
                                    map_EBGM."${dataSource}".ageSubGroup.add(resultSetObj."PARAM_VALUE")
                                } else if (resultSetObj."PARAM" == "gender_subgroup") {
                                    map_EBGM."${dataSource}".genderSubGroup.add(resultSetObj."PARAM_VALUE")
                                } else if (resultSetObj."PARAM" == "region_subgroup") {
                                    map_EBGM."${dataSource}".regionSubGroup.add(resultSetObj."PARAM_VALUE")
                                } else if (resultSetObj."PARAM" == "country_subgroup") {
                                    map_EBGM."${dataSource}".countrySubGroup.add(resultSetObj."PARAM_VALUE")
                                }
                            }
                        }
                        return true
                    }
                })

                List<Future<Boolean>> futures = executorService.invokeAll(callables)
                futures.each {
                    try {
                            it.get()
                    } catch (Exception e) {

                        e.printStackTrace()
                    }
                }
                executorService.shutdown()

            }
        } catch (Exception ex) {
            ex.printStackTrace()
        } finally {
            if (selectedDatasource.contains("pva")) {
                pvaSql?.close()
            }
            if (selectedDatasource.contains("faers")) {
                faersSql?.close()
            }
            if (selectedDatasource.contains("vaers")) {
                vaersSql?.close()
            }
            if (selectedDatasource.contains("vigibase")) {
                vigibaseSql?.close()
            }
            if (selectedDatasource.contains("jader")) {
                jaderSql?.close()
            }
        }

        return [map_EBGM: map_EBGM, map_PRR: map_PRR]
    }
    Map getStratificationValuesDataMiningVariables(String selectedDatasource, String dataMiningVariable){
        Sql pvaSql
        Sql faersSql
        String checkStatement
        String stratificationStatement
        Map sqlMap = [:]
        int count

        Map map_EBGM =["pva": [age: [], gender: [], receiptYear: [], ageSubGroup: [], genderSubGroup: [], "isEBGM": false],
                       "faers":[age:[],gender:[],receiptYear:[],ageSubGroup:[],genderSubGroup:[], "isEBGM": false]]
        Map map_PRR = ["pva": [age: [], gender: [], receiptYear: [], ageSubGroup: [], genderSubGroup: [], "isPRR": false],
                       "faers":[age:[],gender:[],receiptYear:[],ageSubGroup:[],genderSubGroup:[], "isPRR": false]]
        try {
            selectedDatasource.split(",").minus("eudra").each { dataSource ->
                String queryDataSource = ""
                queryDataSource = dataSource?.toUpperCase() + '-DB'
                if (dataSource == "pva") {
                    pvaSql = new Sql(dataSource_pva)
                    sqlMap.put(dataSource,pvaSql)
                } else if(dataSource == "faers"){
                    faersSql = new Sql(dataSource_faers)
                    sqlMap.put(dataSource,faersSql)
                }

                Integer keyId = 0
                Map miningVariables = cacheService.getMiningVariables(dataSource)
                miningVariables.each { key, value ->
                    if (value?.label.equalsIgnoreCase(dataMiningVariable)) {
                        keyId = key as Long
                    }
                }
                count = 0
                checkStatement = SignalQueryHelper.mining_variable_statification_enabled_ebgm(keyId,queryDataSource)
                log.info("checkStatement")
                log.info(""+checkStatement)
                sqlMap.get(dataSource).eachRow(checkStatement, []) { resultSetObj ->
                    count = resultSetObj."count(1)"
                }
                log.info("count")
                log.info(""+count)
                if (count > 0) {
                    map_EBGM."${dataSource}".isEBGM = true
                    stratificationStatement = SignalQueryHelper.stratification_values_ebgm_data_Mining(keyId,queryDataSource)
                    sqlMap.get(dataSource).eachRow(stratificationStatement, []) { resultSetObj ->
                        if (resultSetObj."PARAM" == "age") {
                            map_EBGM."${dataSource}".age.add(resultSetObj."PARAM_VALUE")
                        } else if (resultSetObj."PARAM" == "gender") {
                            map_EBGM."${dataSource}".gender.add(resultSetObj."PARAM_VALUE")
                        } else if (resultSetObj."PARAM" == "receipt_years") {
                            map_EBGM."${dataSource}".receiptYear.add(resultSetObj."PARAM_VALUE")
                        }
                    }
                }

                count = 0
                checkStatement = SignalQueryHelper.statification_enabled_data_Mining(keyId,queryDataSource)
                log.info("checkStatement=========================")
                log.info(""+checkStatement)
                sqlMap.get(dataSource).eachRow(checkStatement, []) { resultSetObj ->
                    count = resultSetObj."count(1)"
                }
                log.info("count")
                log.info(""+count)
                sqlMap.get(dataSource).eachRow(checkStatement, []) { resultSetObj ->
                    count = resultSetObj."count(1)"
                }
                if (count > 0) {
                    map_PRR."${dataSource}".isPRR = true
                    stratificationStatement = SignalQueryHelper.stratification_values_data_Mining(keyId,queryDataSource)
                    sqlMap.get(dataSource).eachRow(stratificationStatement, []) { resultSetObj ->
                        if (resultSetObj."PARAM" == "age") {
                            map_PRR."${dataSource}".age.add(resultSetObj."PARAM_VALUE")
                        } else if (resultSetObj."PARAM" == "gender") {
                            map_PRR."${dataSource}".gender.add(resultSetObj."PARAM_VALUE")
                        } else if (resultSetObj."PARAM" == "receipt_years") {
                            map_PRR."${dataSource}".receiptYear.add(resultSetObj."PARAM_VALUE")
                        }
                    }
                }
                map_EBGM."${dataSource}".isSubGroup = false
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        } finally {
            if (selectedDatasource.contains("pva")) {
                pvaSql?.close()
            }
            if (selectedDatasource.contains("faers")) {
                faersSql?.close()
            }
        }
        return [map_EBGM: map_EBGM, map_PRR: map_PRR]
    }

    //For dissociating aggregateCaseAlert from Signal and Creating history for it's disposition change
    void dissociateAggregateCaseAlertFromSignal(def alert, Disposition targetDisposition, String justification, ValidatedSignal signal,
                                                Boolean isArchived) {
        try {
            // set alert disposition to initial disposition and create history for aggregate case alert
            Disposition previousDisposition = alert.disposition
            DashboardCountDTO dashboardCountDTO = alertService.prepareDashboardCountDTO(false)
            String prevDueDate = alert.dueDate ? DateUtil.stringFromDate(alert.dueDate, Constants.DateFormat.STANDARD_DATE, "UTC") : null
            boolean isTargetDispReviewed = targetDisposition.reviewCompleted

            validatedSignalService.changeToInitialDisposition(alert, signal, targetDisposition)
            saveAlertCaseHistory(alert, justification, userService.getUser()?.fullName)
            ProductEventHistory peHistory = createProductEventHistoryForDispositionChange(alert, justification, isArchived)
            peHistory.save(flush: true)
            String changeDetails = "Disposition changed from '$previousDisposition' to '$targetDisposition' and dissociated from signal '$signal.name'"
            def attr = [product: getNameFieldFromJson(alert.alertConfiguration.productSelection), event: getNameFieldFromJson(alert.alertConfiguration.eventSelection)]
            activityService.createActivity(alert.executedAlertConfiguration, ActivityType.findByValue(ActivityTypeValue.DispositionChange),
                    userService.getUser(), changeDetails, justification, attr,
                    alert.productName, alert.pt, alert.assignedTo, null, alert.assignedToGroup)

            //For Dashboard Counts
            if (!isArchived && alert.assignedToId) {
                alertService.updateDashboardCountMaps(dashboardCountDTO.prevDispCountMap, alert.assignedToId, previousDisposition.id.toString())
                alertService.updateDashboardCountMaps(dashboardCountDTO.userDispCountsMap, alert.assignedToId, targetDisposition.id.toString())
                if (prevDueDate && isTargetDispReviewed) {
                    alertService.updateDashboardCountMaps(dashboardCountDTO.userDueDateCountsMap, alert.assignedToId, prevDueDate)
                }
            } else if (!isArchived) {
                alertService.updateDashboardCountMaps(dashboardCountDTO.prevGroupDispCountMap, alert.assignedToGroupId, previousDisposition.id.toString())
                alertService.updateDashboardCountMaps(dashboardCountDTO.groupDispCountsMap, alert.assignedToGroupId, targetDisposition.id.toString())
                if (prevDueDate && isTargetDispReviewed) {
                    alertService.updateDashboardCountMaps(dashboardCountDTO.groupDueDateCountsMap, alert.assignedToGroupId, prevDueDate)
                }
            }
            //For Top 50 alert widget
            alertService.updateDashboardCountMaps(dashboardCountDTO.execDispCountMap, alert.executedAlertConfigurationId, targetDisposition.id.toString())

            if (!isArchived) {
                alertService.updateDashboardCountsForDispChange(dashboardCountDTO, isTargetDispReviewed)
                alertService.updateDashboardGroupCountsForDispChange(dashboardCountDTO, isTargetDispReviewed)
                def totalPrevDispCount = dashboardCountDTO.prevDispCountMap ?: [:]
                totalPrevDispCount = totalPrevDispCount + dashboardCountDTO.prevGroupDispCountMap ?: [:]
                alertService.updateDispCountsForExecutedConfiguration(dashboardCountDTO.execDispCountMap, totalPrevDispCount)
            }
        } catch (Exception e) {
            e.printStackTrace()
        }

    }
    def getIdFieldFromJson(jsonString) {
        def prdId = ""
        def jsonObj = null
        if (jsonString) {
            jsonObj = parseJsonString(jsonString)
            if (!jsonObj)
                prdId = jsonString
            else {
                def prdVal = jsonObj.find { k,v->
                    v.find {
                        it.containsKey('id')
                    }
                }?.value.findAll {
                    it.containsKey('id')
                }.collect {
                    it.id
                }
                prdId = prdVal ? prdVal.sort().join(',') : ""
            }
        }
        prdId
    }

    List getAggregateCaseAlertCriteriaData(ExecutedConfiguration ec, Map params, String alertType = null, Boolean checkPecCount = false) {
        String timeZone = userService.getCurrentUserPreference()?.timeZone
        if (ec?.selectedDatasource == Constants.DataSource.VAERS) {
            alertType = alertType ? alertType : AGGREGATE_CASE_ALERT_VAERS
        }
        if (ec?.selectedDatasource == Constants.DataSource.VIGIBASE) {
            alertType = alertType ? alertType : AGGREGATE_CASE_ALERT_VIGIBASE
        }
        Long viewId
        def alertFields = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT').collectEntries {
            b -> [b.name, b.display]
        }
        if (!params?.viewId.equals("")) {
            viewId=params?.viewId as Long
        }
        ViewInstance viewInstance = viewInstanceService.fetchSelectedViewInstance(alertType ? alertType : AGGREGATE_CASE_ALERT, viewId)
        //current advandedFilter in view
        AdvancedFilter advancedFilter = AdvancedFilter.findById(params?.advancedFilterId)

        String dateRange = DateUtil.toDateString(ec?.executedAlertDateRangeInformation?.dateRangeStartAbsolute) +
                " - " + DateUtil.toDateString(ec?.executedAlertDateRangeInformation?.dateRangeEndAbsolute)

        Map stratificationMap =[:]

        Map stratificationMapEBGM = [:]
        Map stratificationMapPRR = [:]

        String stratificationParameterSafetyEBGM = ""
        String stratificationParameterSafetyPRR = ""

        String subGroupParameterSafety = ""
        String stratificationParameterFaersEBGM = ""
        String stratificationParameterFaersPRR = ""

        String subGroupParameterFaers = ""
        String stratificationParameterVaersEBGM = ""
        String stratificationParameterVaersPRR = ""
        String stratificationParameterVigibaseEBGM = ""
        String stratificationParameterVigibasePRR = ""
        def stratificationTime=System.currentTimeSeconds()
        if (ec?.dataMiningVariable && ec?.dataMiningVariable != "null" && ec?.adhocRun) {
            stratificationMap = getStratificationValuesDataMiningVariables(ec?.selectedDatasource, ec?.dataMiningVariable)
            stratificationMapEBGM = stratificationMap.map_EBGM
            stratificationMapPRR = stratificationMap.map_PRR

            if (ec?.getDataSource(ec?.selectedDatasource).contains("Safety")) {
                stratificationParameterSafetyEBGM = "Age: " + stratificationMapEBGM?.pva?.age.join(", ") + "\n" + "\n" +
                        "Gender: " + stratificationMapEBGM?.pva?.gender.join(", ") + "\n" + "\n" +
                        "Receipt Year: " + stratificationMapEBGM?.pva?.receiptYear.join(", ") + "\n"

                stratificationParameterSafetyPRR = "Age: " + stratificationMapPRR?.pva?.age.join(", ") + "\n" + "\n" +
                        "Gender: " + stratificationMapPRR?.pva?.gender.join(", ") + "\n" + "\n" +
                        "Receipt Year: " + stratificationMapPRR?.pva?.receiptYear.join(", ") + "\n"

            }
        } else {
            stratificationMap = getStratificationValues(ec.selectedDatasource)
            stratificationMapEBGM = stratificationMap.map_EBGM
            stratificationMapPRR = stratificationMap.map_PRR
            if (ec?.getDataSource(ec?.selectedDatasource).contains("Safety")) {
                stratificationParameterSafetyEBGM = "Age: " + stratificationMapEBGM?.pva?.age.join(", ") + "\n" + "\n" +
                        "Gender: " + stratificationMapEBGM?.pva?.gender.join(", ") + "\n" + "\n" +
                        "Receipt Year: " + stratificationMapEBGM?.pva?.receiptYear.join(", ") + "\n"

                stratificationParameterSafetyPRR = "Age: " + stratificationMapPRR?.pva?.age.join(", ") + "\n" + "\n" +
                        "Gender: " + stratificationMapPRR?.pva?.gender.join(", ") + "\n" + "\n" +
                        "Receipt Year: " + stratificationMapPRR?.pva?.receiptYear.join(", ") + "\n"

                subGroupParameterSafety = "Age Group: " + stratificationMapEBGM?.pva?.ageSubGroup?.join(", ") + "\n" + "\n" +
                        "Gender : " + stratificationMapEBGM?.pva?.genderSubGroup?.join(", ") + "\n" + "\n" +
                        "Country : " + stratificationMapEBGM?.pva?.countrySubGroup?.join(", ") + "\n" + "\n" +
                        "Region : " + stratificationMapEBGM?.pva?.regionSubGroup?.join(", ") + "\n"

            }

            if (ec?.getDataSource(ec?.selectedDatasource).contains("FAERS")) {
                stratificationParameterFaersEBGM = "Age: " + stratificationMapEBGM?.faers?.age.join(", ") + "\n" + "\n" +
                        "Gender: " + stratificationMapEBGM?.faers?.gender.join(", ") + "\n" + "\n" +
                        "Receipt Year: " + stratificationMapEBGM?.faers?.receiptYear.join(", ") + "\n"

                stratificationParameterFaersPRR = "Age: " + stratificationMapPRR?.faers?.age.join(", ") + "\n" + "\n" +
                        "Gender: " + stratificationMapPRR?.faers?.gender.join(", ") + "\n" + "\n" +
                        "Receipt Year: " + stratificationMapPRR?.faers?.receiptYear.join(", ") + "\n"

                subGroupParameterFaers = "Age Group: " + stratificationMapEBGM?.faers?.ageSubGroup.join(", ") + "\n" + "\n" +
                        "Gender : " + stratificationMapEBGM?.faers?.genderSubGroup.join(", ") + "\n"

            }
            if (ec?.getDataSource(ec?.selectedDatasource).contains("VAERS")) {
                stratificationParameterVaersEBGM = "Age: " + stratificationMapEBGM?.vaers?.age.join(", ") + "\n" + "\n" +
                        "Gender: " + stratificationMapEBGM?.vaers?.gender.join(", ") + "\n" + "\n" +
                        "Receipt Year: " + stratificationMapEBGM?.vaers?.receiptYear.join(", ") + "\n"

                stratificationParameterVaersPRR = "Age: " + stratificationMapPRR?.vaers?.age.join(", ") + "\n" + "\n" +
                        "Gender: " + stratificationMapPRR?.vaers?.gender.join(", ") + "\n" + "\n" +
                        "Receipt Year: " + stratificationMapPRR?.vaers?.receiptYear.join(", ") + "\n"

            }

            if (ec?.getDataSource(ec?.selectedDatasource).contains("VigiBase")) {
                stratificationParameterVigibaseEBGM = "Age: " + stratificationMapEBGM?.vigibase?.age.join(", ") + "\n" + "\n" +
                        "Gender: " + stratificationMapEBGM?.vigibase?.gender.join(", ") + "\n" + "\n" +
                        "Receipt Year: " + stratificationMapEBGM?.vigibase?.receiptYear.join(", ") + "\n"

                stratificationParameterVigibasePRR = "Age: " + stratificationMapPRR?.vigibase?.age.join(", ") + "\n" + "\n" +
                        "Gender: " + stratificationMapPRR?.vigibase?.gender.join(", ") + "\n" + "\n" +
                        "Receipt Year: " + stratificationMapPRR?.vigibase?.receiptYear.join(", ") + "\n"

            }
        }

        List<String> queryParameters = []
        List<String> foregroundQueryParameters = []
        String queryName = ''
        String foregroundQueryName = ''
        String all = Constants.Commons.ALL.toLowerCase().substring(0, 1).toUpperCase() + Constants.Commons.ALL.toLowerCase().substring(1)
        String none = Constants.Commons.NONE.toLowerCase().substring(0, 1).toUpperCase() + Constants.Commons.NONE.toLowerCase().substring(1)

        if (ec.executedAlertQueryId && ec.alertQueryName) {
            queryName = ec.alertQueryName
            if (ec.executedAlertQueryValueLists?.isEmpty() == false) {
                processQueryParameters(ec.executedAlertQueryValueLists, queryName, queryParameters)
            }
        }

        if (ec.executedAlertForegroundQueryId && ec.alertForegroundQueryName && !ec.dataMiningVariable) {
            foregroundQueryName = ec.alertForegroundQueryName
            if (ec.executedAlertForegroundQueryValueLists?.isEmpty() == false) {
                processQueryParameters(ec.executedAlertForegroundQueryValueLists, foregroundQueryName, foregroundQueryParameters)
            }
        }

        String dateRangeType = ""
        ViewHelper.getDateRangeTypeI18n().each {
            if (it.name == ec.dateRangeType ) {
                dateRangeType = it.display
            }
        }

        JsonSlurper js = new JsonSlurper()
        def caseListCols = viewInstance.columnSeq ? js.parseText(viewInstance.columnSeq) : []
        def filterList = viewInstance.filters ? js.parseText(viewInstance.filters) : []

        def sorting = viewInstance.sorting ? js.parseText(viewInstance.sorting) : []
        String sortColoum = ""
        String coloumLevelFilter = ""
        Map fixedColSeq = [
                5: "PT",
                4: "SOC",
                3: "Product Name",
                2: "A"
        ]
        sorting.each { filter ->
            caseListCols.each{
                if(filter.key == it.value.seq.toString()){
                    sortColoum = it.value.label + ": " + filter.value
                }
            }
        }
        if(sortColoum == ""){
            sorting.each{ filter ->
                fixedColSeq.each{
                    if(filter.key == it.key.toString()){
                        sortColoum = it.value + ": " + filter.value
                    }
                }
            }
        }
        if(params.column != "undefined"){
            alertFields.each{key,label ->
                if (key == params.column) {
                    if(key == "pt" && label.split("#OR").size() > 1){
                        label = ec.groupBySmq ?  label.split("#OR")[1] : label.split("#OR")[0]
                    }
                    sortColoum = label + ": " + params.sorting
                }
            }
        }
        filterList.each{ filter ->
            caseListCols.each{
                if(filter.key == it.value.seq.toString()){
                    coloumLevelFilter = it.value.label + ": " + filter.value
                }
            }
        }

        Map filterMap = [:]
        if (params.filterList) {
            def jsonSlurper = new JsonSlurper()
            filterMap = jsonSlurper.parseText(params.filterList)
            if(filterMap.productName=="-1")
                filterMap.remove("productName")
        }

        List filtersList = []
        String filter = ""

        if(filterMap.get("productName")!=null && filterMap.get("productName")!=-1) {
            def label =  params.miningVariable ?: alertFields?.get("productName")
            String productLabel = label ?: "Product Name"
            String productName = ""
            if(ec && ec?.masterExConfigId != null){
                productName = ec?.productName
            }else{
                productName = filterMap.get("productName")
            }
            filtersList.add(productLabel + ": " + productName)
        }

        Map fixedColsMap = ["0":["label":"SOC","name":"soc"],
                            "1":["label":"PT","name":"pt"]]
        //Added for SMQ Event group
        String ptAndSMQLabel = ""
        if(alertFields) {
            def label = alertFields?.get("pt")
            if (label && label?.split("#OR")?.size() > 1) {
                ptAndSMQLabel = ec?.groupBySmq ? label.split("#OR")[1] : label.split("#OR")[0]
            }
        }
        filterMap.each{object ->
            fixedColsMap.each{
                if(object.key == it.value.name && object.key == "pt"){
                    filtersList.add(ptAndSMQLabel + ": " +object.value)
                } else if(object.key == it.value.name) {
                    filtersList.add(it.value.label + ": " +object.value)
                }
            }
        }

        filterMap.each{object ->
            caseListCols.each{
                if(object.key == it.value.name){
                    filtersList.add(it.value.label + ": " +object.value)
                }
            }
        }
        Map criteriaCountsMap = populateCriteriaCountMap(ec)
        filter = filtersList.join("\n")
        String productSelection = ec?.productSelection ? ViewHelper.getDictionaryValues(ec, DictionaryTypeEnum.PRODUCT) : ViewHelper.getDictionaryValues(ec, DictionaryTypeEnum.PRODUCT_GROUP)
        String drugRecordSelection = ec?.productSelection ? ViewHelper.getDictionaryValues(ec, DictionaryTypeEnum.PRODUCT, true) : ""
        String datasheets = ""
        String enabledSheet = Constants.DatasheetOptions.CORE_SHEET
        if(!TextUtils.isEmpty(ec?.selectedDataSheet)){
            datasheets = dataSheetService.formatDatasheetMap(ec).text?.join(',')
        }else if (ec?.selectedDatasource?.contains(Constants.DataSource.PVA)) {
            Boolean isProductGroup = !TextUtils.isEmpty(ec.productGroupSelection)
            String products = ec.productGroupSelection?:ec.productSelection
            datasheets =  dataSheetService.fetchDataSheets(products,enabledSheet, isProductGroup, ec.isMultiIngredient)?.dispName.join(',')
        }

        String substanceLabel = Constants.CriteriaSheetLabels.MULTI_SUBSTANCE
        if (!dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM)) {
            substanceLabel = Constants.CriteriaSheetLabels.MULTI_INGREDIENT
        }

        List criteriaSheetList = [
                ['label': Constants.CriteriaSheetLabels.ALERT_NAME, 'value': ec?.name],
                ['label': Constants.CriteriaSheetLabels.DESCRIPTION, 'value': ec?.description ? ec?.description : Constants.Commons.BLANK_STRING],
                ['label': Constants.CriteriaSheetLabels.PRODUCT, 'value': productSelection],
                ['label': substanceLabel, 'value': ec.isMultiIngredient ? Constants.Commons.YES : Constants.Commons.NO]

        ]

        if (ec && ec.selectedDatasource?.contains(Constants.DataSource.PVA)) {
            criteriaSheetList += [
                    ['label': Constants.CriteriaSheetLabels.INCLUDE_WHO_DRUGS, 'value': ec.includeWHODrugs ? Constants.Commons.YES : Constants.Commons.NO]
            ]
            if (ec.includeWHODrugs && ec.productSelection) {
                criteriaSheetList += [
                        ['label': Constants.CriteriaSheetLabels.DRUG_RECORD_NUMBER, 'value': drugRecordSelection] // drug record change
                ]
            }
        }

        criteriaSheetList += [
                ['label': Constants.CriteriaSheetLabels.DATASHEETS, 'value': datasheets?:""],
                ['label': Constants.CriteriaSheetLabels.EVENT_SELECTION, 'value': ec.eventSelection ? getAllEventNameFieldFromJson(ec.eventSelection) : (getGroupNameFieldFromJson(ec.eventGroupSelection) ?: Constants.Commons.BLANK_STRING)], //Added for PVS-55056
                ['label': Constants.CriteriaSheetLabels.QUERY_NAME, 'value': ec.alertQueryName ? queryName : none],
                ['label': Constants.CriteriaSheetLabels.QUERY_PARAMETERS, 'value': ec.executedAlertQueryValueLists ? queryParameters.join(', ') : none],
                ['label': Constants.CriteriaSheetLabels.FOREFROUND_QUERY_NAME, 'value': ec.alertForegroundQueryName ? foregroundQueryName: none]
        ]

        String foregroundQueryParams = ec.executedAlertForegroundQueryValueLists ? foregroundQueryParameters?.join(', ') : Constants.Commons.BLANK_STRING
        criteriaSheetList += [
                ['label': Constants.CriteriaSheetLabels.FOREFROUND_QUERY_PARAMETERS, 'value': getForegroundQueryParameters(ec, foregroundQueryParams)]
        ]
        criteriaSheetList += [
                ['label': Constants.CriteriaSheetLabels.DATE_RANGE_TYPE, 'value': ec.dateRangeType ? dateRangeType : Constants.Commons.BLANK_STRING]
        ]
        String faersDateRange = ec?.faersDateRange
        String vaersDateRange = ec?.vaersDateRange
        String vigibaseDateRange = ec?.vigibaseDateRange
        String evdasDateRange = ec?.evdasDateRange
        if(ec.executedAlertDateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE && ec?.selectedDatasource.contains(Constants.DataSource.FAERS)){
            faersDateRange = DateUtil.toDateString(ec.executedAlertDateRangeInformation.dateRangeStartAbsolute) +
                    " - " + (reportExecutorService.getFaersDateRange().faersDate).substring(13)
        } else if (ec.executedAlertDateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE && ec?.selectedDatasource.contains(Constants.DataSource.VAERS)) {
            //Updated Vaers Date Range for Integrated Alert PVS-67898
            vaersDateRange = DateUtil.toDateString(ec.executedAlertDateRangeInformation.dateRangeStartAbsolute) +
                    " - " + (reportExecutorService.getVaersDateRange().vaersDate).substring(Constants.Commons.VAERS_END_DATE_INDEX)
        } else if(ec.executedAlertDateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE && ec?.selectedDatasource.contains(Constants.DataSource.VIGIBASE)){
            vigibaseDateRange = DateUtil.toDateString(ec.executedAlertDateRangeInformation.dateRangeStartAbsolute) +
                    " - " + (reportExecutorService.getVigibaseDateRange().vigibaseDate).substring(13)
        }


        if(ec?.getDataSource(ec?.selectedDatasource).contains("Safety")){
            criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.DATE_RANGE, 'value': dateRange?dateRange:Constants.Commons.BLANK_STRING])
        }
        if(ec?.getDataSource(ec?.selectedDatasource).contains("FAERS")){
            criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.FAERS_DATE_RANGE, 'value': faersDateRange?:dateRange?:Constants.Commons.BLANK_STRING])
        }
        if(ec?.getDataSource(ec?.selectedDatasource).contains("EVDAS")){
            List idList = []
            def jsonObj = parseJsonString(ec.productGroupSelection)
            if (jsonObj) {
                jsonObj?.each {
                    idList?.add(it?.id)
                }
            }
            if(!evdasDateRange){
                evdasDateRange=reportExecutorService.getEvdasDateRange(idList)
            }
            criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.EVDAS_DATE_RANGE, 'value': evdasDateRange?:evdasDateRange?:Constants.Commons.BLANK_STRING])
        }
        if(ec?.getDataSource(ec?.selectedDatasource).contains("VAERS")){
            criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.VAERS_DATE_RANGE, 'value': vaersDateRange?:dateRange?:Constants.Commons.BLANK_STRING])
        }
        if(ec?.getDataSource(ec?.selectedDatasource).contains(Constants.DataSource.VIGIBASE_CAMEL_CASE)){
            criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.VIGIBASE_DATE_RANGE, 'value': vigibaseDateRange?:dateRange?:Constants.Commons.BLANK_STRING])
        }
        String dataMiningVariable = ec?.dataMiningVariable
        String dataMiningVariableValue = ec?.dataMiningVariableValue ? JSON.parse(ec?.dataMiningVariableValue).value : Constants.Commons.BLANK_STRING
        String dataMiningVariableOperator = ec?.dataMiningVariableValue ? JSON.parse(ec?.dataMiningVariableValue).operatorDisplay : Constants.Commons.BLANK_STRING
        dataMiningVariableValue = getDmvData(dataMiningVariable, dataMiningVariableValue, dataMiningVariableOperator)
        criteriaSheetList += [
                ['label': Constants.CriteriaSheetLabels.EVAULATE_ON, 'value': ec.evaluateDateAs == EvaluateCaseDateEnum.LATEST_VERSION ? "Latest Version" : "Version As Of"],
                ['label': Constants.CriteriaSheetLabels.AS_OF_DATE, 'value': ec.evaluateDateAs == EvaluateCaseDateEnum.LATEST_VERSION ? "" : DateUtil.fromDateToString(ec.asOfVersionDate, DateUtil.DATEPICKER_FORMAT)],
                ['label': Constants.CriteriaSheetLabels.DRUG_TYPE, 'value': ec.drugTypeName ?: Constants.Commons.BLANK_STRING],
                ['label': Constants.CriteriaSheetLabels.GROUP_BY_SMQ, 'value': ec?.groupBySmq ? "Yes" : "No"],
                ['label': Constants.CriteriaSheetLabels.INCLUDE_LOCKED_VERSION, 'value': ec?.includeLockedVersion ? "Yes" : "No"],
                ['label': Constants.CriteriaSheetLabels.EXCLUDE_NON_VALID_CASE, 'value': ec?.excludeNonValidCases ? "Yes" : "No"],
                ['label': Constants.CriteriaSheetLabels.EXCLUDE_FOLLOW_UP, 'value': ec?.excludeFollowUp ? "Yes" : "No"],
                ['label': Constants.CriteriaSheetLabels.MISSED_CASE, 'value': ec?.missedCases ? "Yes" : "No"],
                ['label': Constants.CriteriaSheetLabels.INCLUDE_STUDY_CASES, 'value': (ec.studyCases ? (ec?.studyDrugs?:Constants.Commons.BLANK_STRING) : 'No')],
                ['label': Constants.CriteriaSheetLabels.CONSIDER_ONLY_POI, 'value': ec.onlyPoi? "Yes": "No"],
                ['label': Constants.CriteriaSheetLabels.IGNORE_STUDY_MEDICATION, 'value': ec?.ignoreStudyType ? "Yes": "No"],
                ['label': Constants.CriteriaSheetLabels.DATASOURCE, 'value': ec?.getDataSource(ec?.selectedDatasource)]
        ]
        if (dataMiningVariable!=null && !dataMiningVariable.equals(Constants.Commons.BLANK_STRING)) {
            criteriaSheetList += [
                    ['label': Constants.CriteriaSheetLabels.MINING_VARIABLE, 'value': dataMiningVariable],
                    ['label': Constants.CriteriaSheetLabels.MINING_VARIABLE_PARAM, 'value': dataMiningVariableValue]
            ]
        }

        if(ec?.getDataSource(ec?.selectedDatasource).contains("Safety")) {
                criteriaSheetList.addAll(
                        ['label': Constants.CriteriaSheetLabels.STRATIFICATION_PARAMETER_EBGM_SAFETY, 'value': stratificationParameterSafetyEBGM ?: Constants.Commons.BLANK_STRING],
                        ['label': Constants.CriteriaSheetLabels.STRATIFICATION_PARAMETER_PRR_SAFETY, 'value': stratificationParameterSafetyPRR ?: Constants.Commons.BLANK_STRING],
                        ['label': Constants.CriteriaSheetLabels.SUB_GROUP_PARAMETER, 'value': subGroupParameterSafety ?:Constants.Commons.BLANK_STRING]
                )
        }

        if(ec?.getDataSource(ec?.selectedDatasource).contains("FAERS")){
            criteriaSheetList.addAll(
                    ['label': Constants.CriteriaSheetLabels.STRATIFICATION_PARAMETER_EBGM_FAERS, 'value': stratificationParameterFaersEBGM ?: ""],
                    ['label': Constants.CriteriaSheetLabels.STRATIFICATION_PARAMETER_PRR_FAERS, 'value': stratificationParameterFaersPRR ?: ""],
                    ['label': Constants.CriteriaSheetLabels.SUB_GROUP_PARAMETER_FAERS, 'value': subGroupParameterFaers ?: ""]
            )
        }
        if(ec.adhocRun){
            criteriaSheetList.addAll(
                    ['label': Constants.CriteriaSheetLabels.PRODUCT_AS_DATA_MINING, 'value': ec?.isProductMining ? "Yes" : "No"]
            )
        }

        criteriaSheetList.addAll(
                    ['label': Constants.CriteriaSheetLabels.FG_DATA_MINING_RUN, 'value': (ec?.foregroundSearch==null || ec?.foregroundSearch==false) ? "No" : "Yes"]
        )

        if(ec?.getDataSource(ec?.selectedDatasource).contains("VAERS")){
            criteriaSheetList.addAll(
                    ['label': Constants.CriteriaSheetLabels.STRATIFICATION_PARAMETER_EBGM_VAERS, 'value': stratificationParameterVaersEBGM ?: ""],
                    ['label': Constants.CriteriaSheetLabels.STRATIFICATION_PARAMETER_PRR_VAERS, 'value': stratificationParameterVaersPRR ?: ""],
            )
        }

        if(ec?.getDataSource(ec?.selectedDatasource).toLowerCase().contains("vigibase")){
            criteriaSheetList.addAll(
                    ['label': Constants.CriteriaSheetLabels.STRATIFICATION_PARAMETER_EBGM_VIGIBASE, 'value': stratificationParameterVigibaseEBGM ?: ""],
                    ['label': Constants.CriteriaSheetLabels.STRATIFICATION_PARAMETER_PRR_VIGIBASE, 'value': stratificationParameterVigibasePRR ?: ""],
            )
        }

        criteriaSheetList += [
                ['label': Constants.CriteriaSheetLabels.VIEW, 'value': viewInstance ?viewInstance.name: Constants.Commons.BLANK_STRING],
                ['label': Constants.CriteriaSheetLabels.FILTER, 'value': advancedFilter ? advancedFilter?.name : Constants.Commons.NA_LISTED],
                ['label': Constants.CriteriaSheetLabels.COLUMN_LEVEL_FILTER, value: filter?:""],
                ]

        if(ec.selectedDatasource.contains(Constants.DataSource.PVA)){
            criteriaSheetList += [
                    ['label' : Constants.CriteriaSheetLabels.ALERT_LEVEL_NEW_COUNT_LABEL, 'value': criteriaCountsMap[Constants.CriteriaSheetLabels.ALERT_LEVEL_NEW_COUNT]?criteriaCountsMap[Constants.CriteriaSheetLabels.ALERT_LEVEL_NEW_COUNT].toString():'0'],
                    ['label' : Constants.CriteriaSheetLabels.ALERT_LEVEL_CUM_COUNT_LABEL, 'value': criteriaCountsMap[Constants.CriteriaSheetLabels.ALERT_LEVEL_CUM_COUNT]?criteriaCountsMap[Constants.CriteriaSheetLabels.ALERT_LEVEL_CUM_COUNT].toString():'0'],
                    ['label' : Constants.CriteriaSheetLabels.ALERT_LEVEL_STUDY_COUNT_LABEL, 'value': criteriaCountsMap[Constants.CriteriaSheetLabels.ALERT_LEVEL_STUDY_COUNT]?criteriaCountsMap[Constants.CriteriaSheetLabels.ALERT_LEVEL_STUDY_COUNT].toString():'0'],
                    ['label' : Constants.CriteriaSheetLabels.ALERT_LEVEL_STUDY_CUM_COUNT_LABEL, 'value': criteriaCountsMap[Constants.CriteriaSheetLabels.ALERT_LEVEL_STUDY_CUM_COUNT]?criteriaCountsMap[Constants.CriteriaSheetLabels.ALERT_LEVEL_STUDY_CUM_COUNT].toString():'0']
            ]
        }
        if(ec.selectedDatasource.contains(Constants.DataSource.FAERS)){
            criteriaSheetList += [
                    ['label' : Constants.CriteriaSheetLabels.ALERT_LEVEL_NEW_COUNT_FAERS_LABEL, 'value': criteriaCountsMap[Constants.CriteriaSheetLabels.ALERT_LEVEL_NEW_COUNT_FAERS]?criteriaCountsMap[Constants.CriteriaSheetLabels.ALERT_LEVEL_NEW_COUNT_FAERS].toString():'0'],
                    ['label' : Constants.CriteriaSheetLabels.ALERT_LEVEL_CUM_COUNT_FAERS_LABEL, 'value': criteriaCountsMap[Constants.CriteriaSheetLabels.ALERT_LEVEL_CUM_COUNT_FAERS]?criteriaCountsMap[Constants.CriteriaSheetLabels.ALERT_LEVEL_CUM_COUNT_FAERS].toString():'0']
            ]
        }
        if(ec.selectedDatasource.contains(Constants.DataSource.VAERS)){
            criteriaSheetList += [
                    ['label' : Constants.CriteriaSheetLabels.ALERT_LEVEL_NEW_COUNT_VAERS_LABEL, 'value': criteriaCountsMap[Constants.CriteriaSheetLabels.ALERT_LEVEL_NEW_COUNT_VAERS]?criteriaCountsMap[Constants.CriteriaSheetLabels.ALERT_LEVEL_NEW_COUNT_VAERS].toString():'0'],
                    ['label' : Constants.CriteriaSheetLabels.ALERT_LEVEL_CUM_COUNT_VAERS_LABEL, 'value': criteriaCountsMap[Constants.CriteriaSheetLabels.ALERT_LEVEL_CUM_COUNT_VAERS]?criteriaCountsMap[Constants.CriteriaSheetLabels.ALERT_LEVEL_CUM_COUNT_VAERS].toString():'0']
            ]
        }
        if(ec.selectedDatasource.contains(Constants.DataSource.VIGIBASE)){
            criteriaSheetList += [
                    ['label' : Constants.CriteriaSheetLabels.ALERT_LEVEL_NEW_COUNT_VIGIBASE_LABEL, 'value': criteriaCountsMap[Constants.CriteriaSheetLabels.ALERT_LEVEL_NEW_COUNT_VIGIBASE]?criteriaCountsMap[Constants.CriteriaSheetLabels.ALERT_LEVEL_NEW_COUNT_VIGIBASE].toString():'0'],
                    ['label' : Constants.CriteriaSheetLabels.ALERT_LEVEL_CUM_COUNT_VIGIBASE_LABEL, 'value': criteriaCountsMap[Constants.CriteriaSheetLabels.ALERT_LEVEL_CUM_COUNT_VIGIBASE]?criteriaCountsMap[Constants.CriteriaSheetLabels.ALERT_LEVEL_CUM_COUNT_VIGIBASE].toString():'0']
            ]
        }
        criteriaSheetList += [
                ['label': Constants.CriteriaSheetLabels.DISPOSITIONS, 'value': params.quickFilterDisposition?:Constants.Commons.BLANK_STRING],
                ['label': Constants.CriteriaSheetLabels.SORT_ORDER, 'value': sortColoum?:Constants.Commons.BLANK_STRING],
                (!checkPecCount ? ['label': ec.dataMiningVariable? Constants.CriteriaSheetLabels.ROW_COUNT : Constants.CriteriaSheetLabels.PEC_COUNT, 'value': params.totalCount? params.totalCount : Constants.Commons.BLANK_STRING] : null),
                ['label': Constants.CriteriaSheetLabels.DATE_CREATED, 'value': DateUtil.stringFromDate(ec?.dateCreated, DateUtil.DATEPICKER_FORMAT_AM_PM, timeZone)],
                ['label': Constants.CriteriaSheetLabels.REPORT_GENERATED_BY, 'value': userService.getUser().fullName?:""],
                ['label': Constants.CriteriaSheetLabels.DATE_EXPORTED, 'value': (DateUtil.stringFromDate(new Date(), DateUtil.DATEPICKER_FORMAT_AM_PM, timeZone) + userService.getGmtOffset(timeZone))]
        ].findAll { it != null }

        return criteriaSheetList
    }

    private void processQueryParameters(List queryValueLists, String queryName, List<String> parametersList) {
        queryValueLists.each { eaqvl ->
            eaqvl.parameterValues.each { parameter ->
                StringBuilder queryParameter = new StringBuilder()

                if (parameter.hasProperty('reportField')) {
                    queryParameter.append(messageSource.getMessage("app.reportField.${parameter.reportField.name}", null, Locale.default))
                    queryParameter.append(" ")
                    queryParameter.append(messageSource.getMessage("${parameter.operator.getI18nKey()}", null, Locale.default))
                    queryParameter.append(" ")

                    if (parameter.value) {
                        queryParameter.append(parameter.value)
                    } else {
                        queryParameter.append(queryName ? all : none)
                    }

                    parametersList.add(queryParameter.toString())
                } else {
                    parametersList.add("${parameter.key} : ${parameter.value}")
                }
                queryParameter.setLength(0)
            }
        }
    }

    List getJaderAggregateCaseAlertCriteriaData(ExecutedConfiguration ec, Map params, String alertType = null){
        String timeZone = userService.getCurrentUserPreference()?.timeZone
        Long viewId
        Map alertFields = alertFieldService.getJaderColumnList(ec?.selectedDatasource,ec.groupBySmq).collectEntries {
            b -> [b.name, b.display]
        }
        if(!params?.viewId.equals("")){
            viewId=params?.viewId as Long
        }
        ViewInstance viewInstance = viewInstanceService.fetchSelectedViewInstance(alertType ? alertType : AGGREGATE_CASE_ALERT_JADER, viewId)
        //current advandedFilter in view
        AdvancedFilter advancedFilter = AdvancedFilter.findById(params?.advancedFilterId)

        String dateRange = DateUtil.toDateString(ec.executedAlertDateRangeInformation.dateRangeStartAbsolute) +
                " - " + DateUtil.toDateString(ec.executedAlertDateRangeInformation.dateRangeEndAbsolute)

        Map stratificationMap =[:]

        Map stratificationMapEBGM = [:]
        Map stratificationMapPRR = [:]

        String stratificationParameterEBGM = ""
        String stratificationParameterPRR = ""

        if (ec.dataMiningVariable && ec.dataMiningVariable != "null" && ec.adhocRun) {
            stratificationMap = getStratificationValuesDataMiningVariables(ec.selectedDatasource, ec.dataMiningVariable)
            stratificationMapEBGM = stratificationMap.map_EBGM
            stratificationMapPRR = stratificationMap.map_PRR
            stratificationParameterEBGM = "Age: " + stratificationMapEBGM?.jader?.age.join(", ") + "\n" + "\n" +
                    "Gender: " + stratificationMapEBGM?.jader?.gender.join(", ") + "\n" + "\n" +
                    "Receipt Year: " + stratificationMapEBGM?.jader?.receiptYear.join(", ") + "\n"

            stratificationParameterPRR = "Age: " + stratificationMapPRR?.jader?.age.join(", ") + "\n" + "\n" +
                    "Gender: " + stratificationMapPRR?.jader?.gender.join(", ") + "\n" + "\n" +
                    "Receipt Year: " + stratificationMapPRR?.jader?.receiptYear.join(", ") + "\n"

        } else {
            stratificationMap = getStratificationValues(ec.selectedDatasource)
            stratificationMapEBGM = stratificationMap.map_EBGM
            stratificationMapPRR = stratificationMap.map_PRR
                stratificationParameterEBGM = "Age: " + stratificationMapEBGM?.jader?.age.join(", ") + "\n" + "\n" +
                        "Gender: " + stratificationMapEBGM?.jader?.gender.join(", ") + "\n" + "\n" +
                        "Receipt Year: " + stratificationMapEBGM?.jader?.receiptYear.join(", ") + "\n"

                stratificationParameterPRR = "Age: " + stratificationMapPRR?.jader?.age.join(", ") + "\n" + "\n" +
                        "Gender: " + stratificationMapPRR?.jader?.gender.join(", ") + "\n" + "\n" +
                        "Receipt Year: " + stratificationMapPRR?.jader?.receiptYear.join(", ") + "\n"

        }
        String dateRangeType = ""
        ViewHelper.getDateRangeTypeI18n().each{
            if(it.name == ec.dateRangeType ){
                dateRangeType = it.display
            }
        }
        JsonSlurper js = new JsonSlurper()
        def caseListCols = js.parseText(viewInstance.columnSeq)
        def filterList = js.parseText(viewInstance.filters)

        def sorting = js.parseText(viewInstance.sorting)

        String sortColoum = ""
        String coloumLevelFilter = ""
        Map fixedColSeq = [
                5: "PT",
                4: "SOC",
                3: "Product Name",
                2: "A"
        ]
        sorting.each { filter ->
            caseListCols.each{
                if(filter.key == it.value.seq.toString()){
                    sortColoum = it.value.label + ": " + filter.value
                }
            }
        }
        if(sortColoum == ""){
            sorting.each{ filter ->
                fixedColSeq.each{
                    if(filter.key == it.key.toString()){
                        sortColoum = it.value + ": " + filter.value
                    }
                }
            }
        }
        if(params.column != "undefined"){
            alertFields.each{key,label ->
                if (key == params.column) {
                    if(key == "pt" && label.split("#OR").size() > 1){
                        label = ec.groupBySmq ?  label.split("#OR")[1] : label.split("#OR")[0]
                    }
                    sortColoum = label + ": " + params.sorting
                }
            }
        }
        filterList.each{ filter ->
            caseListCols.each{
                if(filter.key == it.value.seq.toString()){
                    coloumLevelFilter = it.value.label + ": " + filter.value
                }
            }
        }

        Map filterMap = [:]
        if (params.filterList) {
            def jsonSlurper = new JsonSlurper()
            filterMap = jsonSlurper.parseText(params.filterList)
            if(filterMap.productName=="-1")
                filterMap.remove("productName")
        }

        List filtersList = []
        String filter = ""

        if(filterMap.get("productName")!=null && filterMap.get("productName")!=-1) {
            def label = alertFields?.get("productName") ?: "Product Name"
            filtersList.add( label +": " + filterMap.get("productName"))
        }

        Map fixedColsMap = ["0":["label":"SOC","name":"soc"],
                            "1":["label":"PT","name":"pt"]]


        filterMap.each{object ->
            fixedColsMap.each{
                if(object.key == it.value.name && object.key == "pt" && ec?.groupBySmq){
                    filtersList.add("SMQ/Event Group: " + object.value)
                } else if(object.key == it.value.name) {
                    filtersList.add(it.value.label + ": " +object.value)
                }
            }
        }

        filterMap.each{object ->
            caseListCols.each{
                if(object.key == it.value.name){
                    filtersList.add(it.value.label + ": " +object.value)
                }
            }
        }
        Map criteriaCountsMap = populateCriteriaCountMap(ec)
        filter = filtersList.join("\n")
        String productSelection = ec?.productSelection ? ViewHelper.getDictionaryValues(ec, DictionaryTypeEnum.PRODUCT) : ViewHelper.getDictionaryValues(ec, DictionaryTypeEnum.PRODUCT_GROUP)
        String datasheets = ""
        String enabledSheet = Constants.DatasheetOptions.CORE_SHEET
        if(!TextUtils.isEmpty(ec?.selectedDataSheet)){
            datasheets = dataSheetService.formatDatasheetMap(ec).text?.join(',')
        }else if (ec?.selectedDatasource?.contains(Constants.DataSource.PVA)) {
            Boolean isProductGroup = !TextUtils.isEmpty(ec.productGroupSelection)
            String products = ec.productGroupSelection?:ec.productSelection
            datasheets =  dataSheetService.fetchDataSheets(products,enabledSheet, isProductGroup, ec.isMultiIngredient)?.dispName?.join(',')
        }

        String substanceLabel = Constants.CriteriaSheetLabels.MULTI_SUBSTANCE
        if (!dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM)) {
            substanceLabel = Constants.CriteriaSheetLabels.MULTI_INGREDIENT
        }
        List criteriaSheetList = [
                ['label': Constants.CriteriaSheetLabels.ALERT_NAME, 'value': ec?.name],
                ['label': Constants.CriteriaSheetLabels.DESCRIPTION, 'value': ec?.description ? ec?.description : Constants.Commons.BLANK_STRING],
                ['label': Constants.CriteriaSheetLabels.PRODUCT, 'value': productSelection],
                ['label': substanceLabel, 'value': ec.isMultiIngredient ? "Yes" : "No"],
                ['label': Constants.CriteriaSheetLabels.DATASHEETS, 'value': datasheets?:""],
                ['label': Constants.CriteriaSheetLabels.EVENT_SELECTION, 'value': ec.eventSelection ? getAllEventNameFieldFromJson(ec.eventSelection) : (getGroupNameFieldFromJson(ec.eventGroupSelection) ?: Constants.Commons.BLANK_STRING)],
        ]
        criteriaSheetList += [
                ['label': Constants.CriteriaSheetLabels.DATE_RANGE_TYPE, 'value': ec.dateRangeType ? dateRangeType : Constants.Commons.BLANK_STRING]
        ]
        String jaderDateRange = dateRange
        if(ec.executedAlertDateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE && ec?.selectedDatasource?.contains(Constants.DataSource.JADER)){
            jaderDateRange = DateUtil.toDateString(ec.executedAlertDateRangeInformation.dateRangeStartAbsolute) +
                    " - " + (jaderExecutorService.getJaderDateRange().jaderDate).substring(13)
        }
        criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.JADER_DATE_RANGE, 'value': jaderDateRange?:dateRange?:Constants.Commons.BLANK_STRING])
        criteriaSheetList += [
                ['label': Constants.CriteriaSheetLabels.EVAULATE_ON, 'value': ec.evaluateDateAs == EvaluateCaseDateEnum.LATEST_VERSION ? "Latest Version" : "Version As Of"],
                ['label': Constants.CriteriaSheetLabels.AS_OF_DATE, 'value': ec.evaluateDateAs == EvaluateCaseDateEnum.LATEST_VERSION ? "" : DateUtil.fromDateToString(ec.asOfVersionDate, DateUtil.DATEPICKER_FORMAT)],
                ['label': Constants.CriteriaSheetLabels.DRUG_TYPE, 'value': ec.drugTypeName ?: Constants.Commons.BLANK_STRING],
                ['label': Constants.CriteriaSheetLabels.GROUP_BY_SMQ, 'value': ec?.groupBySmq ? "Yes" : "No"],
                ['label': Constants.CriteriaSheetLabels.INCLUDE_LOCKED_VERSION, 'value': ec?.includeLockedVersion ? "Yes" : "No"],
                ['label': Constants.CriteriaSheetLabels.EXCLUDE_NON_VALID_CASE, 'value': ec?.excludeNonValidCases ? "Yes" : "No"],
                ['label': Constants.CriteriaSheetLabels.EXCLUDE_FOLLOW_UP, 'value': ec?.excludeFollowUp ? "Yes" : "No"],
                ['label': Constants.CriteriaSheetLabels.MISSED_CASE, 'value': ec?.missedCases ? "Yes" : "No"],
                ['label': Constants.CriteriaSheetLabels.DATASOURCE, 'value': ec?.getDataSource(ec?.selectedDatasource)]
        ]

        if(ec?.getDataSource(ec?.selectedDatasource)?.toLowerCase().contains("jader")){
            criteriaSheetList.addAll(
                    ['label': Constants.CriteriaSheetLabels.STRATIFICATION_PARAMETER_EBGM_JADER, 'value': stratificationParameterEBGM ?: ""],
                    ['label': Constants.CriteriaSheetLabels.STRATIFICATION_PARAMETER_PRR_JADER, 'value': stratificationParameterPRR ?: ""],
            )
        }

        criteriaSheetList += [
                ['label': Constants.CriteriaSheetLabels.VIEW, 'value': viewInstance ?viewInstance.name: Constants.Commons.BLANK_STRING],
                ['label': Constants.CriteriaSheetLabels.FILTER, 'value': advancedFilter ? advancedFilter?.name : Constants.Commons.NA_LISTED],
                ['label': Constants.CriteriaSheetLabels.COLUMN_LEVEL_FILTER, value: filter?:""],
        ]
        criteriaSheetList += [
                ['label' : Constants.CriteriaSheetLabels.ALERT_LEVEL_NEW_COUNT_JADER, 'value': criteriaCountsMap["alertLevelNewCountJader"]? criteriaCountsMap["alertLevelNewCountJader"].toString():'0'],
                ['label' : Constants.CriteriaSheetLabels.ALERT_LEVEL_CUM_COUNT_JADER, 'value': criteriaCountsMap["alertLevelCumCountJader"]? criteriaCountsMap["alertLevelCumCountJader"].toString():'0']
        ]

        criteriaSheetList += [
                ['label': Constants.CriteriaSheetLabels.DISPOSITIONS, 'value': params.quickFilterDisposition?:Constants.Commons.BLANK_STRING],
                ['label': Constants.CriteriaSheetLabels.SORT_ORDER, 'value': sortColoum?:Constants.Commons.BLANK_STRING],
                ['label': ec.dataMiningVariable? Constants.CriteriaSheetLabels.ROW_COUNT : Constants.CriteriaSheetLabels.PEC_COUNT, 'value': params.totalCount? params.totalCount : Constants.Commons.BLANK_STRING],
                ['label': Constants.CriteriaSheetLabels.DATE_CREATED, 'value': DateUtil.stringFromDate(ec?.dateCreated, DateUtil.DATEPICKER_FORMAT_AM_PM, timeZone)],
                ['label': Constants.CriteriaSheetLabels.REPORT_GENERATED_BY, 'value': userService.getUser().fullName?:""],
                ['label': Constants.CriteriaSheetLabels.DATE_EXPORTED, 'value': (DateUtil.stringFromDate(new Date(), DateUtil.DATEPICKER_FORMAT_AM_PM, timeZone) + userService.getGmtOffset(timeZone))]
        ]

        return criteriaSheetList
    }
    def parseJsonString(str) {
        try {
            def jsonSlurper = new JsonSlurper()
            jsonSlurper.parseText(str)
        } catch (all) {
            null
        }
    }
    String getForegroundQueryParameters(ExecutedConfiguration ec, String foregroundQueryParams) {
        String equals = Constants.AdvancedFilter.EQUALS.toLowerCase().substring(0, 1).toUpperCase() + Constants.AdvancedFilter.EQUALS.toLowerCase().substring(1)
        String all = Constants.Commons.ALL.toLowerCase().substring(0, 1).toUpperCase() + Constants.Commons.ALL.toLowerCase().substring(1)
        String none = Constants.Commons.NONE.toLowerCase().substring(0, 1).toUpperCase() + Constants.Commons.NONE.toLowerCase().substring(1)
        if (ec.foregroundSearch) {
            JSON.parse(ec.foregroundSearchAttr).each {
                if (it.val) {
                    if (foregroundQueryParams.equals(Constants.Commons.BLANK_STRING)) {
                        foregroundQueryParams = it.label + Constants.Commons.SPACE + equals + Constants.Commons.SPACE + it.text
                    } else {
                        foregroundQueryParams = foregroundQueryParams + "," + Constants.Commons.SPACE + it.label + Constants.Commons.SPACE + equals + Constants.Commons.SPACE + it.text
                    }
                }
            }
        }
        if (!foregroundQueryParams && (ec.dataMiningVariable || !ec.alertForegroundQueryName)) {
            foregroundQueryParams = none
        } else if (!ec.dataMiningVariable && !foregroundQueryParams) {
            foregroundQueryParams = all
        }
        return foregroundQueryParams;
    }

    boolean detailsAccessPermission(def selectedDataSources){
        boolean hasAccess = false
        if(SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_VIEWER, ROLE_VIEW_ALL")){
            hasAccess = true
        } else if(Constants.DataSource.FAERS in selectedDataSources && SpringSecurityUtils.ifAnyGranted("ROLE_FAERS_CONFIGURATION")){
            hasAccess = true
        } else if(Constants.DataSource.VAERS in selectedDataSources && SpringSecurityUtils.ifAnyGranted("ROLE_VAERS_CONFIGURATION")){
            hasAccess = true
        } else if(Constants.DataSource.VIGIBASE in selectedDataSources && SpringSecurityUtils.ifAnyGranted("ROLE_VIGIBASE_CONFIGURATION")){
            hasAccess = true
        } else if(Constants.DataSource.JADER in selectedDataSources && SpringSecurityUtils.ifAnyGranted("ROLE_JADER_CONFIGURATION")){
            hasAccess = true
        }
        return hasAccess
    }

    Map reviewRoles(){
        Map roles = ["AllDatasources":SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_VIEWER, ROLE_VIEW_ALL"), "faers": SpringSecurityUtils.ifAnyGranted("ROLE_FAERS_CONFIGURATION"),  "vaers": SpringSecurityUtils.ifAnyGranted("ROLE_VAERS_CONFIGURATION"), "vigibase": SpringSecurityUtils.ifAnyGranted("ROLE_VIGIBASE_CONFIGURATION"),"jader": SpringSecurityUtils.ifAnyGranted("ROLE_JADER_CONFIGURATION")]
        return roles
    }

    Map getEnabledOptions(){
        Map map = [enabledOptions:[], defaultSelected:""]
        if(SpringSecurityUtils.ifAnyGranted('ROLE_AGGREGATE_CASE_CONFIGURATION')){
            map.enabledOptions.add(Constants.DataSource.PVA)
        }
        User user = userService.getUser();
        Set<String> roles=user?.getEnabledAuthorities()?.collect{it?.authority}
        if(roles?.contains('ROLE_FAERS_CONFIGURATION') || roles?.contains('ROLE_ADMIN')){
            map.enabledOptions.add(Constants.DataSource.FAERS)
        }
        if (roles?.contains('ROLE_EVDAS_CASE_CONFIGURATION') || roles?.contains('ROLE_ADMIN')) {
            map.enabledOptions.add(Constants.DataSource.EUDRA)
        }
        if (roles?.contains('ROLE_VAERS_CONFIGURATION') || roles?.contains('ROLE_ADMIN')) {
            map.enabledOptions.add(Constants.DataSource.VAERS)
        }
        if (roles?.contains('ROLE_VIGIBASE_CONFIGURATION') || roles?.contains('ROLE_ADMIN')) {
            map.enabledOptions.add(Constants.DataSource.VIGIBASE)
        }
        if (roles?.contains('ROLE_JADER_CONFIGURATION') || roles?.contains('ROLE_ADMIN')) {
            map.enabledOptions.add(Constants.DataSource.JADER)
        }
        if(Constants.DataSource.PVA in map.enabledOptions){
            map.defaultSelected = Constants.DataSource.PVA
        } else if(Constants.DataSource.FAERS in map.enabledOptions){
            map.defaultSelected = Constants.DataSource.FAERS
        } else if(Constants.DataSource.VAERS in map.enabledOptions){
            map.defaultSelected = Constants.DataSource.VAERS
        } else if(Constants.DataSource.VIGIBASE in map.enabledOptions){
            map.defaultSelected = Constants.DataSource.VIGIBASE
        } else if(Constants.DataSource.JADER in map.enabledOptions){
            map.defaultSelected = Constants.DataSource.JADER
        }

        return map
    }

    void bulkUpdateProdIdAggregateCaseAlert(Integer prod_hierarchy_id, List<Long> executedConfigurationIds) {
        Sql sql = new Sql(dataSource)
        try {
            log.info("executing bulkUpdateProdIdAggregateCaseAlert")
            sql.withTransaction {
                executedConfigurationIds.collate(999).each { list ->
                    String query1 = "UPDATE AGG_ALERT SET prod_hierarchy_id = ${prod_hierarchy_id} WHERE EXEC_CONFIGURATION_ID IN (" + list.join(",").replace("'", "") + ")"
                    String query2 = "UPDATE archived_agg_alert SET prod_hierarchy_id = ${prod_hierarchy_id} WHERE EXEC_CONFIGURATION_ID IN (" + list.join(",").replace("'", "") + ")"
                    sql.execute(query1.replace("'", ""))
                    sql.execute(query2.replace("'", ""))
                }
            }
        }
        catch (Exception ex) {
            println(ex)
            println("######## Exception while Updating the prod_hierarchy_id in Agg Alert table and Archived Agg Alert table. ###########")
        }
        finally {
            sql.close()
        }
    }

    /**
     * For pva dataSource only
     */
    @Transactional
    void migrateProdIdAggregateCaseAlerts() {
        Sql sql = new Sql(signalDataSourceService.getReportConnection("pva"))
        try {
            log.info("executing migrateProdIdAggregateCaseAlerts")
            def beforeTime = System.currentTimeMillis()
            sql.call("{call P_CAT_WEBAPP_PROD_MIG()}")
            String sql_statement1 = "select EXECUTION_ID, KEY_ID from cat_prod_hierarchy_mappping"
            def rows1 = sql.rows(sql_statement1)

            Map<Integer, List> keyIdMapping = new HashMap<>()
            rows1.each { it ->
                if (keyIdMapping.get(it.KEY_ID)) {
                    keyIdMapping.put(it.KEY_ID, keyIdMapping.get(it.KEY_ID) + [it.EXECUTION_ID as Long])
                } else {
                    keyIdMapping.put(it.KEY_ID, [it.EXECUTION_ID as Long])
                }
            }

            keyIdMapping.each { it ->
                bulkUpdateProdIdAggregateCaseAlert(it.key as Integer, it.value as List)
            }

            def afterTime = System.currentTimeMillis()
            log.info("It took " + (afterTime - beforeTime) / 1000 + " seconds for migrating the Aggregate alert and Archived Agg Alerts for Product Hirearchy Id#")
        }
        catch (Exception ex) {
            println(ex)
            println("##########Exception came while migrating the Aggregate alert and Archived Agg Alerts for Product Hirearchy Id################")
        }
        finally {
            sql.close()
        }
    }

    void migrateEventIdAggregateCaseAlerts() {
        Sql sql = new Sql(dataSource)
        try {
            log.info("executing migrateEventIdAggregateCaseAlerts")
            def beforeTime = System.currentTimeMillis()
            sql.withTransaction {
                sql.execute(SignalQueryHelper.update_agg_alert_for_smq())
            }
            def afterTime = System.currentTimeMillis()
            log.info("It took " + (afterTime - beforeTime) / 1000 + " seconds for migrating the Aggregate alert for Event Hirearchy Id#")
        }
        catch (Exception ex) {
            println(ex)
            println("######## Exception while Updating the Event Hirearchy in Agg Alert table table. ###########")
        }
        finally {
            sql.close()
        }
    }

    void migrateSmqArchivedAggregateCaseAlerts() {
        Sql sql = new Sql(dataSource)
        try {
            log.info("executing migrateSmqArchivedAggregateCaseAlerts")
            def beforeTime = System.currentTimeMillis()
            sql.withTransaction {
                sql.execute(SignalQueryHelper.update_archived_agg_alert_for_smq())
            }
            def afterTime = System.currentTimeMillis()
            log.info("It took " + (afterTime - beforeTime) / 1000 + " seconds for migrating the Archived Aggregate alert for Event Hirearchy Id#")
        }
        catch (Exception ex) {
            println(ex)
            println("######## Exception while Updating the Event Hirearchy in Archived Agg Alert table. ###########")
        }
        finally {
            sql.close()
        }
    }

    Map bindGlobalProductMap(Map data) {
        Map globalProductMap = [:]
        String[] ptCodeWithSMQ = data["PT_CODE"] ? String.valueOf(data["PT_CODE"]).split(Constants.Commons.DASH_STRING, 2) : ["0"]
        String ptCode = ptCodeWithSMQ ? ptCodeWithSMQ[0] ? ptCodeWithSMQ[0] : 0 : Constants.Commons.UNDEFINED_NUM
        String peCombination = ptCodeWithSMQ.size() > 1 ? data["PRODUCT_ID"] + "-" + ptCode + "-" + ptCodeWithSMQ[1] : data["PRODUCT_ID"] + "-" + ptCode + "-" + "null"
        globalProductMap.put('productEventComb', peCombination)
        globalProductMap.put('productKeyId', data["PROD_HIERARCHY_ID"]?:Constants.Commons.UNDEFINED_NUM_INT_REVIEW)
        globalProductMap.put('eventKeyId', data["EVENT_HIERARCHY_ID"]?:Constants.Commons.UNDEFINED_NUM_INT_REVIEW)
        globalProductMap
    }
    void prepareCategoryDTOList(def tag, List tagsAndSubTags, List categoryDtoList, String dataSource, Long execConfigId, String peComb, Map keyIdMap=null){
        Boolean isSubTagPresent = false
        Long tagId = tagsAndSubTags.find { it.text == tag.tagText}?.id
        String tagName = tag.tagText
        Integer tagIndex = tag.alert? Holders.config.category.aggregateCase.alertSpecific: Holders.config.category.aggregateCase.global
        String alertId = peComb
        Long globalId = dataObjectService.getGlobalProductEventList(execConfigId, peComb)?.id
        if(globalId) {
            if (tag.alert) {
                alertId = (dataObjectService.getGlobalProductAlertMap(execConfigId, peComb)?.id)?.toString()
            }
            tag.subTags.each { subTagName ->
                isSubTagPresent = true
                Map subTag = tagsAndSubTags.find { it.text == subTagName && it.parentId == tagId }
                Long subTagId = subTag?.id
                CategoryDTO category = pvsGlobalTagService.fetchCategoryDto(dataSource, tagName, tagId, alertId, tagIndex, subTagName, subTagId,
                        0, true, false, execConfigId)
                if(tag.alert){
                    category.setFactGrpCol3(execConfigId as String)
                } else {
                    category.setFactGrpCol5(keyIdMap?.prodHierarchyId as String)
                    category.setFactGrpCol6(keyIdMap?.eventHierarchyId as String)
                }
                categoryDtoList.add(category)
            }
            if (!isSubTagPresent) {
                CategoryDTO category = pvsGlobalTagService.fetchCategoryDto(dataSource, tagName, tagId, alertId, tagIndex, null, null, 0, true, false, execConfigId)
                if(tag.alert){
                    category.setFactGrpCol3(execConfigId as String)
                } else {
                    category.setFactGrpCol5(keyIdMap?.prodHierarchyId as String)
                    category.setFactGrpCol6(keyIdMap?.eventHierarchyId as String)
                }
                categoryDtoList.add(category)
            }
        }
    }

    void prepareCategoryDTO(List<CategoryDTO> categoryDtoList,List<String> pEComList,List<Map> tagsAndSubTags,String dataSource,Long execConfigId){
        List tags = []
        List tagsList = []
        JsonSlurper jsonSlurper = new JsonSlurper()
        pEComList.each { peComb ->
            Map keyIdMap = dataObjectService.getKeyIdMap("$peComb-$execConfigId")
            tags = dataObjectService.getTagsList("$peComb-$execConfigId")

            if (tags) {
                tags.each {tag ->
                    tagsList.add(JSON.parse((jsonSlurper.parseText(tag) as Map).tags))
                }
            }
            tagsList = tagsList.flatten()

            tagsList = tagsList.unique{[it.alert,it.tagText,it.subTags]}

            tagsList.each { tag->
                prepareCategoryDTOList(tag, tagsAndSubTags, categoryDtoList, dataSource, execConfigId, peComb, keyIdMap )
            }
            tagsList.clear()
        }

    }

    List<Long> getAlertIdsForAttachments(Long alertId, boolean isArchived = false){
        def domain = getDomainObject(isArchived)
        List<Long> aggList = []
        List<Long> archivedAlertIds = []
        def aggAlert
        ArchivedAggregateCaseAlert.withTransaction {
            aggAlert = domain.findById(alertId.toInteger())
            archivedAlertIds = ExecutedConfiguration.findAllByConfigId(aggAlert.alertConfiguration.id).collect {
                it.id
            }
            aggList = ArchivedAggregateCaseAlert.createCriteria().list {
                projections {
                    property('id')
                }
                eq('productId', aggAlert?.productId)
                eq('ptCode', aggAlert?.ptCode)
                if (archivedAlertIds){
                    or {
                        archivedAlertIds.collate(1000).each{
                            'in'('executedAlertConfiguration.id', it)
                        }
                    }
                }
            } as List<Long>

            archivedAlertIds = aggList.findAll {
                ArchivedAggregateCaseAlert.get(it).executedAlertConfiguration.id < aggAlert.executedAlertConfiguration.id
            }
        }
        archivedAlertIds + aggAlert.id
    }

    boolean checkAttachmentsForAlert(List<Long> alertIds){
        boolean  isAttached = false
        ArchivedAggregateCaseAlert.withTransaction {
            alertIds.each { Long aggAlertId ->
                def aggAlert = ArchivedAggregateCaseAlert.get(aggAlertId) ?: AggregateCaseAlert.get(aggAlertId)
                if (aggAlert.attachments)
                    isAttached = true
            }
        }
        isAttached
    }

    def fetchPreviousAlertsList(List aggAlertsList,List prevExecConfigIdList){
        def prevAlertsList
        List ptCodeList = aggAlertsList*.ptCode
        List productIdList = aggAlertsList*.productId
        ArchivedAggregateCaseAlert.withTransaction {
                prevAlertsList = ArchivedAggregateCaseAlert.createCriteria().list {
                    resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                    projections {
                        property('id','id')
                        property('pt','pt')
                        property('ptCode','ptCode')
                        property('productId','productId')
                    }
                    if (prevExecConfigIdList){
                        or {
                            prevExecConfigIdList.collate(1000).each{
                                'in'('executedAlertConfiguration.id', it)
                            }
                        }
                        or {
                            ptCodeList.collate(1000).each{
                                'in'('ptCode', it)
                            }
                        }
                        or {
                            productIdList.collate(1000).each{
                                'in'('productId', it)
                            }
                        }
                    }

                } as List<Long>
        }
        return prevAlertsList
    }

    void bindDatasheetData(Configuration configuration, List dataSheets) {
        Map dataSheetMap = [:]
        String [] dataSheetArr = []
        if (dataSheets && dataSheets?.size()>0) {
            dataSheets?.each { dataSheet ->
                dataSheetArr = dataSheet.split (Constants.DatasheetOptions.SEPARATOR)
                dataSheetMap.put(dataSheetArr[1], dataSheetArr[0])
            }
            configuration.selectedDataSheet = dataSheetMap as JSON
        }else{
            configuration.selectedDataSheet = ""
            configuration.isDatasheetChecked = false
            configuration.datasheetType = Constants.DatasheetOptions.CORE_SHEET

        }

    }

    List evaluateListedness (Long executedId,Long childId =null, Boolean isMasterConfig = false) {
        Sql sql = null
        List listedNess = []
        String listedSql = null
        listedSql = SignalQueryHelper.getListedNess(executedId)
        if(isMasterConfig){
            listedSql += " where execution_id = ${childId}"
        }
        try {
                sql = new Sql(dataSource_pva)
                def rows = sql.rows(listedSql)
                if(rows){
                        rows.each{ row ->
                            listedNess?.add([ptCode: row.Meddra_pt_code,listed:row.Listedness_data])
                        }
                }
        } catch (Exception e) {
            log.error(e.printStackTrace())
        } finally {
            sql?.close()
        }
        return listedNess
    }

    private boolean isMatchedStringFromException(Throwable throwable) {
        List<String> errorMessages = Arrays.asList("statistics", "ARCHIEVE", "business", "PERSIST")
        if (Objects.nonNull(throwable) ) {
            for (String error : errorMessages) {
                if (throwable.getMessage() != null && throwable.getMessage().contains(error)) {
                    return true
                }
            }
        }
        return false
    }
    Map populateCriteriaCountMap(ExecutedConfiguration ec){
        Map criteriaCountsMap = [:]
        ec.selectedDatasource.split(',').each {
            if (it == Constants.DataSource.PVA || (it == Constants.DataSource.FAERS && ec.selectedDatasource.startsWith(Constants.DataSource.FAERS)) || (it == Constants.DataSource.VAERS && ec.selectedDatasource.startsWith(Constants.DataSource.VAERS))
                    || (it == Constants.DataSource.VIGIBASE && ec.selectedDatasource.startsWith(Constants.DataSource.VIGIBASE))){
                if(ec.criteriaCounts) {
                    criteriaCountsMap.putAll(JSON.parse(ec.criteriaCounts) as Map)
                }
            } else if (it == Constants.DataSource.FAERS){
                AggregateCaseAlert aggAlert = AggregateCaseAlert.findByExecutedAlertConfigurationAndFaersColumnsIsNotNull(ec)
                Map faersColumns = aggAlert ? new JsonSlurper().parseText(aggAlert.faersColumns) : [:]
                ExecutedConfiguration faersExecutedConfiguration = faersColumns ? ExecutedConfiguration.get(faersColumns.executedAlertConfigurationId as Long) : null
                if(faersExecutedConfiguration && faersExecutedConfiguration.criteriaCounts){
                    criteriaCountsMap.putAll(JSON.parse(faersExecutedConfiguration.criteriaCounts) as Map)
                }
            } else if (it == Constants.DataSource.VAERS){
                AggregateCaseAlert aggAlert = AggregateCaseAlert.findByExecutedAlertConfigurationAndVaersColumnsIsNotNull(ec)
                Map vaersColumns = aggAlert ? new JsonSlurper().parseText(aggAlert.vaersColumns) : [:]
                ExecutedConfiguration vaersExecutedConfiguration = vaersColumns ? ExecutedConfiguration.get(vaersColumns.executedAlertConfigurationId as Long) : null
                if(vaersExecutedConfiguration && vaersExecutedConfiguration.criteriaCounts){
                    criteriaCountsMap.putAll(JSON.parse(vaersExecutedConfiguration.criteriaCounts) as Map)
                }
            } else if (it == Constants.DataSource.VIGIBASE){
                AggregateCaseAlert aggAlert = AggregateCaseAlert.findByExecutedAlertConfigurationAndVigibaseColumnsIsNotNull(ec)
                Map vigibaseColumns = aggAlert ? new JsonSlurper().parseText(aggAlert.vigibaseColumns) : [:]
                ExecutedConfiguration vigibaseExecutedConfiguration = vigibaseColumns ? ExecutedConfiguration.get(vigibaseColumns.executedAlertConfigurationId as Long) : null
                if(vigibaseExecutedConfiguration && vigibaseExecutedConfiguration.criteriaCounts){
                    criteriaCountsMap.putAll(JSON.parse(vigibaseExecutedConfiguration.criteriaCounts) as Map)
                }
            } else if (it == Constants.DataSource.JADER){
                if(ec && ec.criteriaCounts){
                    criteriaCountsMap.putAll(JSON.parse(ec.criteriaCounts) as Map)
                }
            }
        }
        return criteriaCountsMap
    }

    List fetchPrevPeriodExecConfig(Long configId, Long executedConfigId) {
        List<ExecutedConfiguration> prevExecs = ExecutedConfiguration.createCriteria().list {
            eq("configId", configId)
            eq("isEnabled", true)
            lt("id", executedConfigId)
            order("dateCreated", "desc")
            maxResults(Holders.config.signal.quantitative.number.prev.columns)
        }
        return prevExecs
    }

    void notifyUndoableDisposition(List undoableDispositionIdList) {
        notify 'push.disposition.changes', [undoableDispositionList:undoableDispositionIdList]

    }

    // handling when previous disposition is not validated confirmed
    def handleNonValidatedDisposition(def aggregateCaseAlert, Disposition newDisposition, List undoableDispositionList, List alertDueDateList, String selectedDataSource, Disposition previousDisposition, boolean bulkUpdate, boolean isArchived, List peHistoryList, String justification) {
        Disposition oldDisp = aggregateCaseAlert?.disposition
        Map dispDataMap = [
                objectId               : aggregateCaseAlert.id,
                objectType             : Constants.AlertType.AGGREGATE_NEW,
                prevDispositionId      : oldDisp.id,
                prevProposedDisposition: aggregateCaseAlert.proposedDisposition,
                currDispositionId      : newDisposition.id,
                prevJustification      : aggregateCaseAlert.justification,
                prevDispPerformedBy    : aggregateCaseAlert.dispPerformedBy,
                prevDueDate            : aggregateCaseAlert.dueDate,
                prevDispChangeDate     : aggregateCaseAlert.dispLastChange,
                pastPrevDueDate        : aggregateCaseAlert.previousDueDate
        ]
        UndoableDisposition undoableDisposition = undoableDispositionService.createUndoableObject(dispDataMap)

        undoableDispositionList.add(undoableDisposition)

        aggregateCaseAlert.disposition = newDisposition
        if ((newDisposition.closed || newDisposition.reviewCompleted) && aggregateCaseAlert.dueDate != null) {
            aggregateCaseAlert.previousDueDate = aggregateCaseAlert.dueDate
        }
        calcDueDate(aggregateCaseAlert, aggregateCaseAlert.priority, aggregateCaseAlert.disposition, true,
                cacheService.getDispositionConfigsByPriority(aggregateCaseAlert.priority.id), true)
        def customReviewPeriod = cacheService.getDispositionConfigsByPriority(aggregateCaseAlert.priority.id)?.find { it -> it.disposition == newDisposition }?.reviewPeriod
        if (newDisposition.closed == false && newDisposition.reviewCompleted == false && aggregateCaseAlert.previousDueDate != null && (oldDisp.closed || oldDisp.reviewCompleted) && !customReviewPeriod) {
            aggregateCaseAlert.dueDate = aggregateCaseAlert.previousDueDate
        }
        alertDueDateList << [id        : aggregateCaseAlert.id, dueDate: aggregateCaseAlert.dueDate, dispositionId: aggregateCaseAlert.disposition.id,
                             reviewDate: aggregateCaseAlert.reviewDate]
        emailNotificationService.mailHandlerForDispChangeACA(aggregateCaseAlert, selectedDataSource, previousDisposition, newDisposition, bulkUpdate, isArchived)
        peHistoryList << createProductEventHistoryForDispositionChange(aggregateCaseAlert, justification, isArchived)
    }

// Handle when the previous disposition is validated confirmed
    def handleValidatedDisposition(def aggregateCaseAlert, Disposition newDisposition, List alertDueDateList, String justification, String validatedSignalName, List peHistoryList, boolean isArchived) {
        calcDueDate(aggregateCaseAlert, aggregateCaseAlert.priority, aggregateCaseAlert.disposition, true,
                cacheService.getDispositionConfigsByPriority(aggregateCaseAlert.priority.id), true)

        if (newDisposition.closed == false && newDisposition.reviewCompleted == false && aggregateCaseAlert.previousDueDate != null) {
            aggregateCaseAlert.dueDate = aggregateCaseAlert.previousDueDate
        }
        alertDueDateList << [id        : aggregateCaseAlert.id, dueDate: aggregateCaseAlert.dueDate, dispositionId: aggregateCaseAlert.disposition.id,
                             reviewDate: aggregateCaseAlert.reviewDate]
        if (justification) {
            justification = justification.replace('.', ' ') + " " + customMessageService.getMessage("validatedObservation.justification.pec", "${validatedSignalName}")
        } else {
            justification = customMessageService.getMessage("validatedObservation.justification.pec", "${validatedSignalName}")
        }
        peHistoryList << createProductEventHistoryForDispositionChange(aggregateCaseAlert, justification, isArchived)
    }

// Update dashboard counts
    def updateDashboardCounts(def aggregateCaseAlert, DashboardCountDTO dashboardCountDTO, Disposition previousDisposition, Disposition newDisposition, String prevDueDate, boolean isTargetDispReviewed) {
        String prevDispCountKey = aggregateCaseAlert.assignedToId ? previousDisposition.id.toString() : previousDisposition.id.toString()
        if (aggregateCaseAlert.assignedToId) {
            dashboardCountDTO.prevDispCountMap.put(prevDispCountKey, (dashboardCountDTO.prevDispCountMap.get(prevDispCountKey) ?: 0) + 1)
            alertService.updateDashboardCountMaps(dashboardCountDTO.prevDispCountMap, aggregateCaseAlert.assignedToId, previousDisposition.id.toString())
            alertService.updateDashboardCountMaps(dashboardCountDTO.userDispCountsMap, aggregateCaseAlert.assignedToId, newDisposition.id.toString())
            if (prevDueDate && isTargetDispReviewed) {
                alertService.updateDashboardCountMaps(dashboardCountDTO.userDueDateCountsMap, aggregateCaseAlert.assignedToId, prevDueDate)
            }
        } else {
            dashboardCountDTO.prevGroupDispCountMap.put(prevDispCountKey, (dashboardCountDTO.prevGroupDispCountMap.get(prevDispCountKey) ?: 0) + 1)
            alertService.updateDashboardCountMaps(dashboardCountDTO.prevGroupDispCountMap, aggregateCaseAlert.assignedToGroupId, previousDisposition.id.toString())
            alertService.updateDashboardCountMaps(dashboardCountDTO.groupDispCountsMap, aggregateCaseAlert.assignedToGroupId, newDisposition.id.toString())
            if (prevDueDate && isTargetDispReviewed) {
                alertService.updateDashboardCountMaps(dashboardCountDTO.groupDueDateCountsMap, aggregateCaseAlert.assignedToGroupId, prevDueDate)
            }
        }
    }

// For Top 50 alert widget
    def updateTop50AlertWidget(def aggregateCaseAlert, DashboardCountDTO dashboardCountDTO, Disposition newDisposition) {
        alertService.updateDashboardCountMaps(dashboardCountDTO.execDispCountMap, aggregateCaseAlert.executedAlertConfigurationId, newDisposition.id.toString())
    }

    Map getCountMapForPrevPEC(Map prevAlert){
        Map prevEvdasCols
        if(prevAlert?.evdasColumns){
            prevEvdasCols=JSON.parse(prevAlert.evdasColumns)
        }
        Map prevFaersCols
        if(prevAlert?.faersColumns){
            prevFaersCols=JSON.parse(prevAlert.faersColumns)
        }
        Map prevVaersCols
        if(prevAlert?.vaersColumns){
            prevVaersCols=JSON.parse(prevAlert.vaersColumns)
        }
        Map prevVigibaseCols
        if(prevAlert?.vigibaseColumns){
            prevVigibaseCols=JSON.parse(prevAlert.vigibaseColumns)
        }

        return [
                newCount            : (prevAlert?.newCount != null && prevAlert?.newCount != Constants.Commons.UNDEFINED_NUM_INT_REVIEW) ? prevAlert.newCount : Constants.Commons.DASH_STRING,
                cummCount           : (prevAlert?.cummCount != null && prevAlert?.newCount != Constants.Commons.UNDEFINED_NUM_INT_REVIEW) ? prevAlert.cummCount : Constants.Commons.DASH_STRING,
                newPediatricCount   : (prevAlert?.newPediatricCount != null && prevAlert?.newCount != Constants.Commons.UNDEFINED_NUM_INT_REVIEW) ? prevAlert.newPediatricCount : Constants.Commons.DASH_STRING,
                cummPediatricCount  : (prevAlert?.cummPediatricCount != null && prevAlert?.newCount != Constants.Commons.UNDEFINED_NUM_INT_REVIEW) ? prevAlert.cummPediatricCount : Constants.Commons.DASH_STRING,
                newInteractingCount : (prevAlert?.newInteractingCount != null && prevAlert?.newCount != Constants.Commons.UNDEFINED_NUM_INT_REVIEW) ? prevAlert.newInteractingCount : Constants.Commons.DASH_STRING,
                cummInteractingCount: (prevAlert?.cummInteractingCount != null && prevAlert?.newCount != Constants.Commons.UNDEFINED_NUM_INT_REVIEW) ? prevAlert.cummInteractingCount : Constants.Commons.DASH_STRING,
                newStudyCount       : (prevAlert?.newStudyCount != null && prevAlert?.newCount != Constants.Commons.UNDEFINED_NUM_INT_REVIEW) ? prevAlert.newStudyCount : Constants.Commons.DASH_STRING,
                cumStudyCount       : (prevAlert?.cumStudyCount != null && prevAlert?.newCount != Constants.Commons.UNDEFINED_NUM_INT_REVIEW) ? prevAlert.cumStudyCount : Constants.Commons.DASH_STRING,
                newSponCount        : (prevAlert?.newSponCount != null && prevAlert?.newCount != Constants.Commons.UNDEFINED_NUM_INT_REVIEW) ? prevAlert.newSponCount : Constants.Commons.DASH_STRING,
                cumSponCount        : (prevAlert?.newSponCount != null && prevAlert?.newCount != Constants.Commons.UNDEFINED_NUM_INT_REVIEW) ? prevAlert.cumSponCount : Constants.Commons.DASH_STRING,
                newSeriousCount     : (prevAlert?.newSeriousCount != null && prevAlert?.newCount != Constants.Commons.UNDEFINED_NUM_INT_REVIEW) ? prevAlert.newSeriousCount : Constants.Commons.DASH_STRING,
                cumSeriousCount     : (prevAlert?.cumSeriousCount != null && prevAlert?.newCount != Constants.Commons.UNDEFINED_NUM_INT_REVIEW) ? prevAlert.cumSeriousCount : Constants.Commons.DASH_STRING,
                newGeriatricCount   : (prevAlert?.newGeriatricCount != null && prevAlert?.newCount != Constants.Commons.UNDEFINED_NUM_INT_REVIEW) ? prevAlert.newGeriatricCount : Constants.Commons.DASH_STRING,
                cumGeriatricCount   : (prevAlert?.cumGeriatricCount != null && prevAlert?.newCount != Constants.Commons.UNDEFINED_NUM_INT_REVIEW) ? prevAlert.cumGeriatricCount : Constants.Commons.DASH_STRING,
                newNonSerious       : (prevAlert?.newNonSerious != null && prevAlert?.newCount != Constants.Commons.UNDEFINED_NUM_INT_REVIEW) ? prevAlert.newNonSerious : Constants.Commons.DASH_STRING,
                cumNonSerious       : (prevAlert?.cumNonSerious != null && prevAlert?.newCount != Constants.Commons.UNDEFINED_NUM_INT_REVIEW) ? prevAlert.cumNonSerious : Constants.Commons.DASH_STRING,
                newFatalCount       : (prevAlert?.newFatalCount != null && prevAlert?.newCount != Constants.Commons.UNDEFINED_NUM_INT_REVIEW) ? prevAlert.newFatalCount : Constants.Commons.DASH_STRING,
                cumFatalCount       : (prevAlert?.cumFatalCount != null && prevAlert?.newCount != Constants.Commons.UNDEFINED_NUM_INT_REVIEW) ? prevAlert.cumFatalCount : Constants.Commons.DASH_STRING,
                prrValue            : (prevAlert?.prrValue != null && prevAlert?.newCount != Constants.Commons.UNDEFINED_NUM_INT_REVIEW) ? prevAlert.prrValue : Constants.Commons.DASH_STRING,
                prrLCI              : (prevAlert?.prrLCI != null && prevAlert?.newCount != Constants.Commons.UNDEFINED_NUM_INT_REVIEW) ? prevAlert.prrLCI : Constants.Commons.DASH_STRING,
                prrUCI              : (prevAlert?.prrUCI != null && prevAlert?.newCount != Constants.Commons.UNDEFINED_NUM_INT_REVIEW) ? prevAlert.prrUCI : Constants.Commons.DASH_STRING,
                rorValue            : (prevAlert?.rorValue != null && prevAlert?.newCount != Constants.Commons.UNDEFINED_NUM_INT_REVIEW) ? prevAlert.rorValue : Constants.Commons.DASH_STRING,
                rorLCI              : (prevAlert?.rorLCI != null && prevAlert?.newCount != Constants.Commons.UNDEFINED_NUM_INT_REVIEW) ? prevAlert.rorLCI : Constants.Commons.DASH_STRING,
                rorUCI              : (prevAlert?.rorUCI != null && prevAlert?.newCount != Constants.Commons.UNDEFINED_NUM_INT_REVIEW) ? prevAlert.rorUCI : Constants.Commons.DASH_STRING,
                eb95                : (prevAlert?.eb95 != null && prevAlert?.newCount != Constants.Commons.UNDEFINED_NUM_INT_REVIEW) ? prevAlert.eb95 : Constants.Commons.DASH_STRING,
                eb05                : (prevAlert?.eb05 != null && prevAlert?.newCount != Constants.Commons.UNDEFINED_NUM_INT_REVIEW) ? prevAlert.eb05 : Constants.Commons.DASH_STRING,
                ebgm                : (prevAlert?.ebgm != null && prevAlert?.newCount != Constants.Commons.UNDEFINED_NUM_INT_REVIEW) ? prevAlert.ebgm : Constants.Commons.DASH_STRING,
                eb95Age             : (prevAlert?.eb95Age) ? prevAlert.eb95Age : Constants.Commons.DASH_STRING,
                eb05Age             : (prevAlert?.eb05Age) ? prevAlert.eb05Age : Constants.Commons.DASH_STRING,
                ebgmAge             : (prevAlert?.ebgmAge) ? prevAlert.ebgmAge : Constants.Commons.DASH_STRING,
                eb95Gender          : (prevAlert?.eb95Gender) ? prevAlert.eb95Gender : Constants.Commons.DASH_STRING,
                eb05Gender          : (prevAlert?.eb05Gender) ? prevAlert.eb05Gender : Constants.Commons.DASH_STRING,
                ebgmGender          : (prevAlert?.ebgmGender) ? prevAlert.ebgmGender : Constants.Commons.DASH_STRING,
                ebgmSubGroup        : (prevAlert?.ebgmSubGroup) ? prevAlert.ebgmSubGroup : Constants.Commons.DASH_STRING,
                eb05SubGroup        : (prevAlert?.eb05SubGroup) ? prevAlert.eb05SubGroup : Constants.Commons.DASH_STRING,
                eb95SubGroup        : (prevAlert?.eb95SubGroup) ? prevAlert.eb95SubGroup : Constants.Commons.DASH_STRING,
                rorSubGroup         : (prevAlert?.rorSubGroup) ? prevAlert.rorSubGroup : Constants.Commons.DASH_STRING,
                rorLciSubGroup      : (prevAlert?.rorLciSubGroup) ? prevAlert.rorLciSubGroup : Constants.Commons.DASH_STRING,
                rorUciSubGroup      : (prevAlert?.rorUciSubGroup) ? prevAlert.rorUciSubGroup : Constants.Commons.DASH_STRING,
                prrSubGroup         : (prevAlert?.prrSubGroup) ? prevAlert.prrSubGroup : Constants.Commons.DASH_STRING,
                prrLciSubGroup      : (prevAlert?.prrLciSubGroup) ? prevAlert.prrLciSubGroup : Constants.Commons.DASH_STRING,
                prrUciSubGroup      : (prevAlert?.prrUciSubGroup) ? prevAlert.prrUciSubGroup : Constants.Commons.DASH_STRING,
                chiSquareSubGroup   : (prevAlert?.chiSquareSubGroup) ? prevAlert.chiSquareSubGroup : Constants.Commons.DASH_STRING,
                rorRelSubGroup     : (prevAlert?.rorRelSubGroup != null) ? prevAlert.rorRelSubGroup : Constants.Commons.DASH_STRING,
                rorLciRelSubGroup    : (prevAlert?.rorLciRelSubGroup != null) ? prevAlert.rorLciRelSubGroup : Constants.Commons.DASH_STRING,
                rorUciRelSubGroup  : (prevAlert?.rorUciRelSubGroup != null) ? prevAlert.rorUciRelSubGroup : Constants.Commons.DASH_STRING,
                aValue              : (prevAlert?.aValue != null) ? prevAlert.aValue : Constants.Commons.UNDEFINED_NUM,
                bValue              : (prevAlert?.bValue != null) ? prevAlert.bValue : Constants.Commons.UNDEFINED_NUM,
                cValue              : (prevAlert?.cValue != null) ? prevAlert.cValue : Constants.Commons.UNDEFINED_NUM,
                dValue              : (prevAlert?.dValue != null) ? prevAlert.dValue : Constants.Commons.UNDEFINED_NUM,
                eValue              : (prevAlert?.eValue != null) ? prevAlert.eValue : Constants.Commons.UNDEFINED_NUM,
                rrValue             : (prevAlert?.rrValue != null) ? prevAlert.rrValue : Constants.Commons.UNDEFINED_NUM,
                newEvEvdas          : (prevEvdasCols?.newEvEvdas != null) ? prevEvdasCols.newEvEvdas : Constants.Commons.DASH_STRING,
                totalEvEvdas        : (prevEvdasCols?.totalEvEvdas != null) ? prevEvdasCols.totalEvEvdas : Constants.Commons.DASH_STRING,
                newSeriousEvdas     : (prevEvdasCols?.newSeriousEvdas != null) ? prevEvdasCols.newSeriousEvdas : Constants.Commons.DASH_STRING,
                totalSeriousEvdas   : (prevEvdasCols?.totalSeriousEvdas != null) ? prevEvdasCols.totalSeriousEvdas : Constants.Commons.DASH_STRING,
                newFatalEvdas       : (prevEvdasCols?.newFatalEvdas != null) ? prevEvdasCols.newFatalEvdas : Constants.Commons.DASH_STRING,
                totalFatalEvdas     : (prevEvdasCols?.totalFatalEvdas != null) ? prevEvdasCols.totalFatalEvdas : Constants.Commons.DASH_STRING,
                newLitEvdas         : (prevEvdasCols?.newLitEvdas != null) ? prevEvdasCols.newLitEvdas : Constants.Commons.DASH_STRING,
                totalLitEvdas       : (prevEvdasCols?.totalLitEvdas != null) ? prevEvdasCols.totalLitEvdas : Constants.Commons.DASH_STRING,
                allRorEvdas         : (prevEvdasCols?.allRorEvdas != null) ? prevEvdasCols.allRorEvdas : Constants.Commons.DASH_STRING,
                newFatalCountFaers  : (prevFaersCols?.newFatalCountFaers != null) ? prevFaersCols.newFatalCountFaers : Constants.Commons.DASH_STRING,
                cumFatalCountFaers  : (prevFaersCols?.cumFatalCountFaers != null) ? prevFaersCols.cumFatalCountFaers : Constants.Commons.DASH_STRING,
                newSeriousCountFaers: (prevFaersCols?.newSeriousCountFaers != null) ? prevFaersCols.newSeriousCountFaers : Constants.Commons.DASH_STRING,
                cumSeriousCountFaers: (prevFaersCols?.cumSeriousCountFaers != null) ? prevFaersCols.cumSeriousCountFaers : Constants.Commons.DASH_STRING,
                newSponCountFaers   : (prevFaersCols?.newSponCountFaers != null) ? prevFaersCols.newSponCountFaers : Constants.Commons.DASH_STRING,
                cumSponCountFaers   : (prevFaersCols?.cumSponCountFaers != null) ? prevFaersCols.cumSponCountFaers : Constants.Commons.DASH_STRING,
                newStudyCountFaers  : (prevFaersCols?.newStudyCountFaers != null) ? prevFaersCols.newStudyCountFaers : Constants.Commons.DASH_STRING,
                cumStudyCountFaers  : (prevFaersCols?.cumStudyCountFaers != null) ? prevFaersCols.cumStudyCountFaers : Constants.Commons.DASH_STRING,
                newCountFaers            : (prevFaersCols?.newCountFaers != null) ? prevFaersCols.newCountFaers : Constants.Commons.UNDEFINED_NUM,
                newCountVaers            : (prevVaersCols?.newCountVaers != null) ? prevVaersCols.newCountVaers : Constants.Commons.UNDEFINED_NUM,
                newCountVigibase            : (prevVigibaseCols?.newCountVigibase != null) ? prevVigibaseCols.newCountVigibase : Constants.Commons.UNDEFINED_NUM,
                cummCountFaers           : (prevFaersCols?.cummCountFaers != null) ? prevFaersCols.cummCountFaers : Constants.Commons.UNDEFINED_NUM,
                cummCountVaers           : (prevVaersCols?.cummCountVaers != null) ? prevVaersCols.cummCountVaers : Constants.Commons.UNDEFINED_NUM,
                newPediatricCountVaers           : (prevVaersCols?.newPediatricCountVaers != null) ? prevVaersCols.newPediatricCountVaers : Constants.Commons.UNDEFINED_NUM,
                newGeriatricCountVaers           : (prevVaersCols?.newGeriatricCountVaers != null) ? prevVaersCols.newGeriatricCountVaers : Constants.Commons.UNDEFINED_NUM,
                cummPediatricCountVaers           : (prevVaersCols?.cummPediatricCountVaers != null) ? prevVaersCols.cummPediatricCountVaers : Constants.Commons.UNDEFINED_NUM,
                cumGeriatricCountVaers           : (prevVaersCols?.cumGeriatricCountVaers != null) ? prevVaersCols.cumGeriatricCountVaers : Constants.Commons.UNDEFINED_NUM,
                newSeriousCountVaers           : (prevVaersCols?.newSeriousCountVaers != null) ? prevVaersCols.newSeriousCountVaers : Constants.Commons.UNDEFINED_NUM,
                cumSeriousCountVaers           : (prevVaersCols?.cumSeriousCountVaers != null) ? prevVaersCols.cumSeriousCountVaers : Constants.Commons.UNDEFINED_NUM,
                newFatalCountVaers           : (prevVaersCols?.newFatalCountVaers != null) ? prevVaersCols.newFatalCountVaers : Constants.Commons.UNDEFINED_NUM,
                cumFatalCountVaers           : (prevVaersCols?.cumFatalCountVaers != null) ? prevVaersCols.cumFatalCountVaers : Constants.Commons.UNDEFINED_NUM,
                chiSquareVaers           : (prevVaersCols?.chiSquareVaers != null) ? prevVaersCols.chiSquareVaers : Constants.Commons.UNDEFINED_NUM,
                eb05Vaers           : (prevVaersCols?.eb05Vaers != null) ? prevVaersCols.eb05Vaers : Constants.Commons.UNDEFINED_NUM,
                eb95Vaers           : (prevVaersCols?.eb95Vaers != null) ? prevVaersCols.eb95Vaers : Constants.Commons.UNDEFINED_NUM,
                prrLCIVaers           : (prevVaersCols?.prrLCIVaers != null) ? prevVaersCols.prrLCIVaers : Constants.Commons.UNDEFINED_NUM,
                prrUCIVaers           : (prevVaersCols?.prrUCIVaers != null) ? prevVaersCols.prrUCIVaers : Constants.Commons.UNDEFINED_NUM,
                rorLCIVaers           : (prevVaersCols?.rorLCIVaers != null) ? prevVaersCols.rorLCIVaers : Constants.Commons.UNDEFINED_NUM,
                rorUCIVaers           : (prevVaersCols?.rorUCIVaers != null) ? prevVaersCols.rorUCIVaers : Constants.Commons.UNDEFINED_NUM,
                prrValueVaers           : (prevVaersCols?.prrValueVaers != null) ? prevVaersCols.prrValueVaers : Constants.Commons.UNDEFINED_NUM,
                rorValueVaers           : (prevVaersCols?.rorValueVaers != null) ? prevVaersCols.rorValueVaers : Constants.Commons.UNDEFINED_NUM,
                ebgmVaers           : (prevVaersCols?.ebgmVaers != null) ? prevVaersCols.ebgmVaers : Constants.Commons.UNDEFINED_NUM,
                cummCountVigibase           : (prevVigibaseCols?.cummCountVigibase != null) ? prevVigibaseCols.cummCountVigibase : Constants.Commons.UNDEFINED_NUM,
                newPediatricCountVigibase           : (prevVigibaseCols?.newPediatricCountVigibase != null) ? prevVigibaseCols.newPediatricCountVigibase : Constants.Commons.UNDEFINED_NUM,
                newGeriatricCountVigibase           : (prevVigibaseCols?.newGeriatricCountVigibase != null) ? prevVigibaseCols.newGeriatricCountVigibase : Constants.Commons.UNDEFINED_NUM,
                cummPediatricCountVigibase           : (prevVigibaseCols?.cummPediatricCountVigibase != null) ? prevVigibaseCols.cummPediatricCountVigibase : Constants.Commons.UNDEFINED_NUM,
                cumGeriatricCountVigibase           : (prevVigibaseCols?.cumGeriatricCountVigibase != null) ? prevVigibaseCols.cumGeriatricCountVigibase : Constants.Commons.UNDEFINED_NUM,
                newSeriousCountVigibase           : (prevVigibaseCols?.newSeriousCountVigibase != null) ? prevVigibaseCols.newSeriousCountVigibase : Constants.Commons.UNDEFINED_NUM,
                cumSeriousCountVigibase           : (prevVigibaseCols?.cumSeriousCountVigibase != null) ? prevVigibaseCols.cumSeriousCountVigibase : Constants.Commons.UNDEFINED_NUM,
                newFatalCountVigibase           : (prevVigibaseCols?.newFatalCountVigibase != null) ? prevVigibaseCols.newFatalCountVigibase : Constants.Commons.UNDEFINED_NUM,
                cumFatalCountVigibase           : (prevVigibaseCols?.cumFatalCountVigibase != null) ? prevVigibaseCols.cumFatalCountVigibase : Constants.Commons.UNDEFINED_NUM,
                chiSquareVigibase           : (prevVigibaseCols?.chiSquareVigibase != null) ? prevVigibaseCols.chiSquareVigibase : Constants.Commons.UNDEFINED_NUM,
                eb05Vigibase           : (prevVigibaseCols?.eb05Vigibase != null) ? prevVigibaseCols.eb05Vigibase : Constants.Commons.UNDEFINED_NUM,
                eb95Vigibase           : (prevVigibaseCols?.eb95Vigibase != null) ? prevVigibaseCols.eb95Vigibase : Constants.Commons.UNDEFINED_NUM,
                prrLCIVigibase           : (prevVigibaseCols?.prrLCIVigibase != null) ? prevVigibaseCols.prrLCIVigibase : Constants.Commons.UNDEFINED_NUM,
                prrUCIVigibase           : (prevVigibaseCols?.prrUCIVigibase != null) ? prevVigibaseCols.prrUCIVigibase : Constants.Commons.UNDEFINED_NUM,
                rorLCIVigibase           : (prevVigibaseCols?.rorLCIVigibase != null) ? prevVigibaseCols.rorLCIVigibase : Constants.Commons.UNDEFINED_NUM,
                rorUCIVigibase           : (prevVigibaseCols?.rorUCIVigibase != null) ? prevVigibaseCols.rorUCIVigibase : Constants.Commons.UNDEFINED_NUM,
                prrValueVigibase           : (prevVigibaseCols?.prrValueVigibase != null) ? prevVigibaseCols.prrValueVigibase : Constants.Commons.UNDEFINED_NUM,
                rorValueVigibase           : (prevVigibaseCols?.rorValueVigibase != null) ? prevVigibaseCols.rorValueVigibase : Constants.Commons.UNDEFINED_NUM,
                ebgmVigibase           : (prevVigibaseCols?.ebgmVigibase != null) ? prevVigibaseCols.ebgmVigibase : Constants.Commons.UNDEFINED_NUM,
                newPediatricCountFaers   : (prevFaersCols?.newPediatricCountFaers != null) ? prevFaersCols.newPediatricCountFaers : Constants.Commons.UNDEFINED_NUM,
                cummPediatricCountFaers  : (prevFaersCols?.cummPediatricCountFaers != null) ? prevFaersCols.cummPediatricCountFaers : Constants.Commons.UNDEFINED_NUM,
                newInteractingCountFaers : (prevFaersCols?.newInteractingCountFaers != null) ? prevFaersCols.newInteractingCountFaers : Constants.Commons.UNDEFINED_NUM,
                cummInteractingCountFaers: (prevFaersCols?.cummInteractingCountFaers != null) ? prevFaersCols.cummInteractingCountFaers : Constants.Commons.UNDEFINED_NUM,
                newGeriatricCountFaers: (prevFaersCols?.newGeriatricCountFaers != null) ? prevFaersCols.newGeriatricCountFaers  : Constants.Commons.UNDEFINED_NUM,
                cumGeriatricCountFaers: (prevFaersCols?.cumGeriatricCountFaers != null) ? prevFaersCols.cumGeriatricCountFaers : Constants.Commons.UNDEFINED_NUM,
                newNonSeriousFaers  : (prevFaersCols?.newNonSeriousFaers != null) ? prevFaersCols.newNonSeriousFaers : Constants.Commons.UNDEFINED_NUM,
                cumNonSeriousFaers  : (prevFaersCols?.cumNonSeriousFaers != null) ? prevFaersCols.cumNonSeriousFaers : Constants.Commons.UNDEFINED_NUM,
                prrValueFaers       : (prevFaersCols?.prrValueFaers != null) ? prevFaersCols.prrValueFaers : Constants.Commons.DASH_STRING,
                prrLCIFaers          : (prevFaersCols?.prrLCIFaers != null) ? prevFaersCols.prrLCIFaers : Constants.Commons.DASH_STRING,
                prrUCIFaers          : (prevFaersCols?.prrUCIFaers != null) ? prevFaersCols.prrUCIFaers : Constants.Commons.DASH_STRING,
                rorValueFaers       : (prevFaersCols?.rorValueFaers != null) ? prevFaersCols.rorValueFaers : Constants.Commons.DASH_STRING,
                rorLCIFaers          : (prevFaersCols?.rorLCIFaers != null) ? prevFaersCols.rorLCIFaers : Constants.Commons.DASH_STRING,
                rorUCIFaers          : (prevFaersCols?.rorUCIFaers != null) ? prevFaersCols.rorUCIFaers : Constants.Commons.DASH_STRING,
                eb05Faers           : (prevFaersCols?.eb05Faers != null) ? prevFaersCols.eb05Faers : Constants.Commons.DASH_STRING,
                eb95Faers           : (prevFaersCols?.eb95Faers != null) ? prevFaersCols.eb95Faers : Constants.Commons.DASH_STRING,
                ebgmFaers           : (prevFaersCols?.ebgmFaers != null) ? prevFaersCols.ebgmFaers : Constants.Commons.DASH_STRING,
                eb05AgeFaers        : (prevAlert?.eb05AgeFaers) ? prevAlert.eb05AgeFaers : Constants.Commons.DASH_STRING,
                eb95AgeFaers        : (prevAlert?.eb95AgeFaers) ? prevAlert.eb95AgeFaers : Constants.Commons.DASH_STRING,
                ebgmAgeFaers        : (prevAlert?.ebgmAgeFaers) ? prevAlert.ebgmAgeFaers : Constants.Commons.DASH_STRING,
                eb05GenderFaers     : (prevAlert?.eb05GenderFaers) ? prevAlert.eb05GenderFaers : Constants.Commons.DASH_STRING,
                eb95GenderFaers     : (prevAlert?.eb95GenderFaers) ? prevAlert.eb95GenderFaers : Constants.Commons.DASH_STRING,
                ebgmGenderFaers     : (prevAlert?.ebgmGenderFaers) ? prevAlert.ebgmGenderFaers : Constants.Commons.DASH_STRING,
                chiSquare           : (prevAlert?.chiSquare != null) ? prevAlert.chiSquare : Constants.Commons.UNDEFINED_NUM,
                chiSquareFaers      : (prevFaersCols?.chiSquareFaers != null) ? prevFaersCols.chiSquareFaers : Constants.Commons.DASH_STRING,
        ]
    }

    Map getDashStringCountMap() {
        return [
                newCount                  : Constants.Commons.DASH_STRING,
                cummCount                 : Constants.Commons.DASH_STRING,
                newPediatricCount         : Constants.Commons.DASH_STRING,
                cummPediatricCount        : Constants.Commons.DASH_STRING,
                newInteractingCount       : Constants.Commons.DASH_STRING,
                cummInteractingCount      : Constants.Commons.DASH_STRING,
                newStudyCount             : Constants.Commons.DASH_STRING,
                cumStudyCount             : Constants.Commons.DASH_STRING,
                newSponCount              : Constants.Commons.DASH_STRING,
                cumSponCount              : Constants.Commons.DASH_STRING,
                newSeriousCount           : Constants.Commons.DASH_STRING,
                cumSeriousCount           : Constants.Commons.DASH_STRING,
                newGeriatricCount         : Constants.Commons.DASH_STRING,
                cumGeriatricCount         : Constants.Commons.DASH_STRING,
                newNonSerious             : Constants.Commons.DASH_STRING,
                cumNonSerious             : Constants.Commons.DASH_STRING,
                newFatalCount             : Constants.Commons.DASH_STRING,
                cumFatalCount             : Constants.Commons.DASH_STRING,
                prrValue                  : Constants.Commons.DASH_STRING,
                prrLCI                    : Constants.Commons.DASH_STRING,
                prrUCI                    : Constants.Commons.DASH_STRING,
                rorValue                  : Constants.Commons.DASH_STRING,
                rorLCI                    : Constants.Commons.DASH_STRING,
                rorUCI                    : Constants.Commons.DASH_STRING,
                eb95                      : Constants.Commons.DASH_STRING,
                eb05                      : Constants.Commons.DASH_STRING,
                ebgm                      : Constants.Commons.DASH_STRING,
                aValue                    : Constants.Commons.DASH_STRING,
                bValue                    : Constants.Commons.DASH_STRING,
                cValue                    : Constants.Commons.DASH_STRING,
                dValue                    : Constants.Commons.DASH_STRING,
                eValue                    : Constants.Commons.DASH_STRING,
                rrValue                   : Constants.Commons.DASH_STRING,
                eb95Age                   : Constants.Commons.DASH_STRING,
                eb05Age                   : Constants.Commons.DASH_STRING,
                ebgmAge                   : Constants.Commons.DASH_STRING,
                eb95Gender                : Constants.Commons.DASH_STRING,
                eb05Gender                : Constants.Commons.DASH_STRING,
                ebgmGender                : Constants.Commons.DASH_STRING,
                ebgmSubGroup              : Constants.Commons.DASH_STRING,
                eb05SubGroup              : Constants.Commons.DASH_STRING,
                eb95SubGroup              : Constants.Commons.DASH_STRING,
                rorSubGroup               : Constants.Commons.DASH_STRING,
                rorLciSubGroup            : Constants.Commons.DASH_STRING,
                rorUciSubGroup            : Constants.Commons.DASH_STRING,
                prrSubGroup               : Constants.Commons.DASH_STRING,
                prrLciSubGroup            : Constants.Commons.DASH_STRING,
                prrUciSubGroup            : Constants.Commons.DASH_STRING,
                chiSquareSubGroup         : Constants.Commons.DASH_STRING,
                rorRelSubGroup            : Constants.Commons.DASH_STRING,
                rorLciRelSubGroup         : Constants.Commons.DASH_STRING,
                rorUciRelSubGroup         : Constants.Commons.DASH_STRING,
                newEvEvdas                : Constants.Commons.DASH_STRING,
                newSeriousEvdas           : Constants.Commons.DASH_STRING,
                newFatalEvdas             : Constants.Commons.DASH_STRING,
                newLitEvdas               : Constants.Commons.DASH_STRING,
                allRorEvdas               : Constants.Commons.DASH_STRING,
                newFatalCountFaers        : Constants.Commons.DASH_STRING,
                cumFatalCountFaers        : Constants.Commons.DASH_STRING,
                newSeriousCountFaers      : Constants.Commons.DASH_STRING,
                cumSeriousCountFaers      : Constants.Commons.DASH_STRING,
                newSponCountFaers         : Constants.Commons.DASH_STRING,
                cumSponCountFaers         : Constants.Commons.DASH_STRING,
                newStudyCountFaers        : Constants.Commons.DASH_STRING,
                cumStudyCountFaers        : Constants.Commons.DASH_STRING,
                newCountFaers             : Constants.Commons.DASH_STRING,
                cummCountFaers            : Constants.Commons.DASH_STRING,
                newPediatricCountFaers    : Constants.Commons.DASH_STRING,
                cummPediatricCountFaers   : Constants.Commons.DASH_STRING,
                newInteractingCountFaers  : Constants.Commons.DASH_STRING,
                cummInteractingCountFaers : Constants.Commons.DASH_STRING,
                newGeriatricCountFaers    : Constants.Commons.DASH_STRING,
                cumGeriatricCountFaers    : Constants.Commons.DASH_STRING,
                newNonSeriousFaers        : Constants.Commons.DASH_STRING,
                cumNonSeriousFaers        : Constants.Commons.DASH_STRING,
                prrValueFaers             : Constants.Commons.DASH_STRING,
                prrLCIFaers               : Constants.Commons.DASH_STRING,
                prrUCIFaers               : Constants.Commons.DASH_STRING,
                rorValueFaers             : Constants.Commons.DASH_STRING,
                rorLCIFaers               : Constants.Commons.DASH_STRING,
                rorUCIFaers               : Constants.Commons.DASH_STRING,
                eb05Faers                 : Constants.Commons.DASH_STRING,
                eb95Faers                 : Constants.Commons.DASH_STRING,
                ebgmFaers                 : Constants.Commons.DASH_STRING,
                eb05AgeFaers              : Constants.Commons.DASH_STRING,
                eb95AgeFaers              : Constants.Commons.DASH_STRING,
                ebgmAgeFaers              : Constants.Commons.DASH_STRING,
                eb05GenderFaers           : Constants.Commons.DASH_STRING,
                eb95GenderFaers           : Constants.Commons.DASH_STRING,
                ebgmGenderFaers           : Constants.Commons.DASH_STRING,
                chiSquare                 : Constants.Commons.DASH_STRING,
                chiSquareFaers            : Constants.Commons.DASH_STRING,
                newCountVaers             : Constants.Commons.DASH_STRING,
                cummCountVaers            : Constants.Commons.DASH_STRING,
                newPediatricCountVaers    : Constants.Commons.DASH_STRING,
                newGeriatricCountVaers    : Constants.Commons.DASH_STRING,
                cummPediatricCountVaers   : Constants.Commons.DASH_STRING,
                cumGeriatricCountVaers    : Constants.Commons.DASH_STRING,
                newSeriousCountVaers      : Constants.Commons.DASH_STRING,
                cumSeriousCountVaers      : Constants.Commons.DASH_STRING,
                newFatalCountVaers        : Constants.Commons.DASH_STRING,
                cumFatalCountVaers        : Constants.Commons.DASH_STRING,
                chiSquareVaers            : Constants.Commons.DASH_STRING,
                eb05Vaers                 : Constants.Commons.DASH_STRING,
                eb95Vaers                 : Constants.Commons.DASH_STRING,
                prrLCIVaers               : Constants.Commons.DASH_STRING,
                prrUCIVaers               : Constants.Commons.DASH_STRING,
                rorLCIVaers               : Constants.Commons.DASH_STRING,
                rorUCIVaers               : Constants.Commons.DASH_STRING,
                prrValueVaers             : Constants.Commons.DASH_STRING,
                rorValueVaers             : Constants.Commons.DASH_STRING,
                newCountVigibase          : Constants.Commons.DASH_STRING,
                cummCountVigibase         : Constants.Commons.DASH_STRING,
                newPediatricCountVigibase : Constants.Commons.DASH_STRING,
                newGeriatricCountVigibase : Constants.Commons.DASH_STRING,
                cummPediatricCountVigibase: Constants.Commons.DASH_STRING,
                cumGeriatricCountVigibase : Constants.Commons.DASH_STRING,
                newSeriousCountVigibase   : Constants.Commons.DASH_STRING,
                cumSeriousCountVigibase   : Constants.Commons.DASH_STRING,
                newFatalCountVigibase     : Constants.Commons.DASH_STRING,
                cumFatalCountVigibase     : Constants.Commons.DASH_STRING,
                chiSquareVigibase         : Constants.Commons.DASH_STRING,
                eb05Vigibase              : Constants.Commons.DASH_STRING,
                eb95Vigibase              : Constants.Commons.DASH_STRING,
                prrLCIVigibase            : Constants.Commons.DASH_STRING,
                prrUCIVigibase            : Constants.Commons.DASH_STRING,
                rorLCIVigibase            : Constants.Commons.DASH_STRING,
                rorUCIVigibase            : Constants.Commons.DASH_STRING,
                prrValueVigibase          : Constants.Commons.DASH_STRING,
                rorValueVigibase          : Constants.Commons.DASH_STRING,
        ]
    }

    Map getDynamicCountDashMap() {
        List newFields = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', true).collect { it.name }
        Map outCountMap = [:]
        if (newFields) {
            newFields.each {
                outCountMap.put(it, Constants.Commons.DASH_STRING)
                if (it.contains("new")) {
                    outCountMap.put(it.replace("new", "cum"), Constants.Commons.DASH_STRING)
                }
            }
        }

        return outCountMap
    }

    List<Map> getColumnListForAggregateExport(Map params) {
        String alertType
        List<Map> columnList = []
        Boolean isJader = params.isJader ? true : false
        Long viewId = params.viewId ? Long.valueOf(params.viewId) : null
        if (params.viewId && params.adhocRun) {
            alertType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND
            if (params.groupBySmq) {
                alertType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT_SMQ_DEMAND
            }
            if (params.miningVariable) {
                alertType = alertType + "-" + params.miningVariable
            }
        } else {
            alertType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT
            if (params.groupBySmq) {
                alertType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT_SMQ
            }
        }
        if (params.outputFormat == ReportFormat.XLSX.name()) {
            columnList = getColumnListForExcelExport(alertType, viewId, params.adhocRun as Boolean, params.groupBySmq as Boolean, isJader) ?: columnList
        } else {
            columnList = viewInstanceService.fetchVisibleColumnList(alertType, viewId)

        }
        return columnList

    }
    /*
    This function return the max number of executed config for which the field is available in view to restrict previous period data fetch
     */

    Integer getTheMaxExeNumber(Map params) {
        try {
            List<Map> columnList = getColumnListForAggregateExport(params)
            List exeNumbers = columnList.collect { it.name }
                    .findAll { it.startsWith("exe") }  // Filter names that start with "exe"
                    .collect {
                        Matcher matcher = (it =~ /exe(\d+)/)
                        matcher ? matcher[0][1] : ''
                    }
                    .findAll { it.isNumber() }  // Keep only numeric parts
                    .collect { it as Integer }  // Convert to integers

            Integer maxExeNumber = exeNumbers ? (exeNumbers.max() == 0 ? 0 : exeNumbers.max()) : -1
            return maxExeNumber
        } catch (Exception exception) {
            log.info("Error occured while fetching last exec number for export")
            return Holders.config.signal.quantitative.number.prev.columns as Integer;
        }
    }

    Map showCharts(def aga) {
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
            frequency.equals("HOURLY") ? it.executedAlertDateRangeInformation.dateRangeEndAbsolute : it.executedAlertDateRangeInformation.dateRangeEndAbsolute.clearTime()
        }?.sort {
            frequency.equals("HOURLY") ? it.executedAlertDateRangeInformation.dateRangeEndAbsolute : it.executedAlertDateRangeInformation.dateRangeEndAbsolute.clearTime()
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
                //Updated Vaers Date Range for Integrated Alert PVS-67898
                dateRange = (reportExecutorService.getVaersDateRange().vaersDate).substring(Constants.Commons.VAERS_END_DATE_INDEX)
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
        return model
    }

    void notifyAlertConfigSyncToMart(Long configId, String productHierarchy, String eventHierarchy) {
        notify 'sync.alert.config.to.mart', [configId: configId, productHierarchy: productHierarchy, eventHierarchy: eventHierarchy]
    }

    @Transactional
    void syncConfigToMartCacheConfig(Map data) {
        Configuration configuration = Configuration.read(data.configId as long)
        if (!configuration) return

        Map alertConfigMap = buildAlertConfigMap(data, configuration)
        Map eventHierarchyJson = buildEventHierarchyJson(data)

        String alertConfig = new JsonBuilder(alertConfigMap).toPrettyString()
        String eventJson = new JsonBuilder(eventHierarchyJson).toPrettyString()

        Sql martConnection = null
        Sql webSchema = null
        boolean isNewAlertConfig = true
        def masterCachedConfigId, countsConfigId, scoresConfigId
        boolean mappingUpdateRequired = false

        try {
            martConnection = new Sql(signalDataSourceService.getReportConnection('pva'))

            martConnection.eachRow('SELECT ALERT_CONFIG, JSON_EVENT_HIERARCHY, MASTER_CACHED_CONFIG_ID, COUNTS_CONFIG_ID, SCORES_CONFIG_ID FROM PVS_CACHED_CONFIG') { row ->
                String clobString = row['ALERT_CONFIG']?.getSubString(1, (int) row['ALERT_CONFIG'].length())
                if (clobString == alertConfig) {
                    isNewAlertConfig = false
                    clobString = row['JSON_EVENT_HIERARCHY']?.getSubString(1, (int) row['JSON_EVENT_HIERARCHY'].length())
                    if (eventJson && clobString != eventJson) {
                        mappingUpdateRequired = true
                    }
                    masterCachedConfigId = row['MASTER_CACHED_CONFIG_ID']
                    countsConfigId = row['COUNTS_CONFIG_ID']
                    scoresConfigId = row['SCORES_CONFIG_ID']
                    return false // Exit the loop early
                }
            }

            if (isNewAlertConfig) {
                webSchema = new Sql(signalDataSourceService.getReportConnection('dataSource'))
                masterCachedConfigId = getSequenceNextVal(webSchema, 'config_sequence')
                scoresConfigId = getSequenceNextVal(webSchema, 'config_sequence')
                countsConfigId = getSequenceNextVal(webSchema, 'config_sequence')

                martConnection.execute(
                        'INSERT INTO PVS_CACHED_CONFIG (ALERT_CONFIG, MASTER_CACHED_CONFIG_ID, SCORES_CONFIG_ID, COUNTS_CONFIG_ID, INIT_FLAG, JSON_EVENT_HIERARCHY, IS_ENABLED) ' +
                                'VALUES (?, ?, ?, ?, 1, ?, 1)',
                        [alertConfig, masterCachedConfigId, scoresConfigId, countsConfigId, eventJson]
                )
                mapAlertConfigWithMasterCachedConfig(configuration.id, [masterCachedConfigId: masterCachedConfigId, scoresConfigId: scoresConfigId, countsConfigId: countsConfigId])

            } else if (mappingUpdateRequired && masterCachedConfigId) {
                martConnection.execute(
                        'UPDATE PVS_CACHED_CONFIG SET JSON_EVENT_HIERARCHY = ?, INIT_FLAG = 1, LAST_UPDATED = ? WHERE MASTER_CACHED_CONFIG_ID = ?',
                        [eventJson, new Date(), masterCachedConfigId]
                )
                mapAlertConfigWithMasterCachedConfig(configuration.id, [masterCachedConfigId: masterCachedConfigId, scoresConfigId: scoresConfigId, countsConfigId: countsConfigId])
            } else {
                mapAlertConfigWithMasterCachedConfig(configuration.id, [masterCachedConfigId: masterCachedConfigId, scoresConfigId: scoresConfigId, countsConfigId: countsConfigId])
            }

        } catch (Exception ex) {
            log.error("Error occurred while syncing alert config to mart", ex)
            ex.printStackTrace()
        } finally {
            webSchema?.close()
            martConnection?.close()
        }
    }

    Map buildAlertConfigMap(Map data, Configuration configuration) {
        List<ViewConfig> productViewsList = PVDictionaryConfig.ProductConfig.views
        int index = ViewHelper.getHiearchyValuesWithIndex(data.productHierarchy as String)
        int productHierarchyCode
        if (index != -1) {
            productHierarchyCode =  productViewsList.find { it.index == index }.keyId
        } else {
            productHierarchyCode = -1
        }
        int isEventGroup = data.eventHierarchy == "EVENT_GROUP" ? 1 : 0
        return [
                "PRODUCT_DICTIONARY"                : productHierarchyCode,
                "IS_MULTI_INGREDIENT"               : configuration.isMultiIngredient ? 1 : 0,
                "IS_EVENT_GROUP"                    : isEventGroup,
                "DATE_RANGE_TYPE"                   : configuration.dateRangeType.value(),
                "EVALUATE_DATA_ASOF"                : configuration.evaluateDateAs.value(),
                "EXCLUDE_FOLLOWUP"                  : configuration.excludeFollowUp ? 1 : 0,
                "IS_SMQ"                            : configuration.isGroupBySmq() ? 1 : 0,
                "EXCLUDE_NONVALID_CASES"            : configuration.excludeNonValidCases ? 1 : 0,
                "INCLUDE_LOCKED_VERSION"            : configuration.includeLockedVersion ? 1 : 0,
                "INCLUDE_PREVIOUSLY_MISSED_CASES_PVS": configuration.missedCases ? 1 : 0,
                "PRODUCT_TYPE"                      : configuration.drugType ? fetchProductTypeWithCode(configuration.drugType) : null,
                "SELECTED_DATE_RANGE"               : configuration.alertDateRangeInformation.dateRangeEnum,
                "RELATIVE_DATE_RNG_VALUE"           : configuration.alertDateRangeInformation.relativeDateRangeValue,
                "BACKGROUND_QUERY_ID"               : configuration.alertQueryId,
                "BACKGROUND_QUERY_NAME"             : configuration.alertQueryValueLists ?: null,
                "SUSPECT_PRODUCT"                   : configuration.suspectProduct ? 1 : 0,
                "NEXT_RUN_DATE"                     : configuration.nextRunDate ?: null,
                "REPORT_START_DATE"                 : configuration.alertDateRangeInformation.getReportStartAndEndDate()[0],
                "REPORT_END_DATE"                   : configuration.alertDateRangeInformation.getReportStartAndEndDate()[1]
        ]
    }

    Map buildEventHierarchyJson(Map data) {
        return [
                "PT"          : (data.eventHierarchy == "PT") ? 1 : 0,
                "SOC"         : (data.eventHierarchy == "SOC") ? 1 : 0,
                "EVENT_GROUP" : (data.eventHierarchy == "EVENT_GROUP") ? 1 : 0,
                "HLT"         : (data.eventHierarchy == "HLT") ? 1 : 0,
                "HLGT"         : (data.eventHierarchy == "HLGT") ? 1 : 0,
                "SMQ"         : (data.eventHierarchy == "SMQ") ? 1 : 0
        ]
    }


    List fetchProductTypeWithCode(String drugType) {
        ProductTypeConfiguration productTypeConfiguration
        List productTypeList = []
        drugType?.replace('[', '')?.replace(']', '')?.split(',')?.each {
            if (it.isInteger()) {
                productTypeConfiguration = ProductTypeConfiguration.get(it)
                if (productTypeConfiguration != null) {
                    productTypeList.addAll(["TEXT": "${productTypeConfiguration.productType} (${productTypeConfiguration.roleType})", "CODE": "${productTypeConfiguration.productTypeId}.${productTypeConfiguration.roleTypeId}"])
                }
            }
        }
        return productTypeList
    }

    def getSequenceNextVal(Sql sql, String sequenceName) {
        def nextVal
        sql.rows("SELECT nextval('${sequenceName}')").collect {
            nextVal = it["nextval"]
        }
        return nextVal
    }

    void mapAlertConfigWithMasterCachedConfig(Long configId, Map dataMap) {
        Configuration configuration = Configuration.get(configId)
        JsonBuilder jsonBuilder = new JsonBuilder(dataMap)
        configuration.masterConfigMapping = jsonBuilder.toPrettyString()
        configuration.save(flush: true)
    }

    /**
     * This fetches read Only users/groups for a configuration from Params and save it.
     * @param shareWith
     * @param configId
     */
    void persistUserReadOnlyMapping(def shareWith, Long configId) {
        List readOnlyUser = []
        List readOnlyGroup = []
        if (shareWith) {
            def shareWithValues = []
            shareWithValues << shareWith
            shareWithValues.flatten().each { String shared ->
                if (shared.startsWith(Constants.USER_GROUP_TOKEN)) {
                    if(shared.endsWith("_readOnly")){
                        Long groupId = Long.valueOf(shared.replaceAll(Constants.USER_GROUP_TOKEN, '').replaceAll("_readOnly", ""))
                        readOnlyGroup.add(groupId)
                    }
                } else if (shared.startsWith(Constants.USER_TOKEN)) {
                    if(shared.endsWith("_readOnly")){
                        Long userId = Long.valueOf(shared.replaceAll(Constants.USER_TOKEN, '').replaceAll("_readOnly", ""))
                        readOnlyUser.add(userId)
                    }
                }
            }
        }
        saveReadOnlyShareWithToConfig(configId, readOnlyGroup, readOnlyUser)
    }

    /**
     * This persists read only property for the user/group shared to a Configuration.
     * @param configId
     * @param readOnlyGroup
     * @param readOnlyUser
     */
    void saveReadOnlyShareWithToConfig(Long configId,List readOnlyGroup, List readOnlyUser){
        Sql sql
        try{
            sql = new Sql(dataSource)
            sql.execute("UPDATE SHARE_WITH_USER_CONFIG SET READ_ONLY = false WHERE CONFIG_ID = ?", [configId])
            sql.execute("UPDATE SHARE_WITH_GROUP_CONFIG SET READ_ONLY = false WHERE CONFIG_ID = ?", [configId])
            if (readOnlyGroup) {
                String groupIds = readOnlyGroup.collect { it.toString() }.join(',')
                sql.execute("UPDATE SHARE_WITH_GROUP_CONFIG SET READ_ONLY = true WHERE CONFIG_ID = ? AND SHARE_WITH_GROUPID IN (${groupIds})", [configId])
            }
            if (readOnlyUser) {
                String userIds = readOnlyUser.collect { it.toString() }.join(',')
                sql.execute("UPDATE SHARE_WITH_USER_CONFIG SET READ_ONLY = true WHERE CONFIG_ID = ? AND SHARE_WITH_USERID IN (${userIds})", [configId])
            }
        }catch(Exception ex){
            log.error("Error Occured while persisting read only property for user.", ex)
        } finally {
            sql?.close()
        }
    }

    /**
     * This share with data for an executed Configuration.
     * @param params
     */
    void persistDataToShareWithExconfig(def params){
        def shareWithValues = []
        shareWithValues << params.sharedWith
        Long exconfigId = params.executedConfigId as Long
        String oldValue = (alertService.getReadOnlyUsersForEx(ExecutedConfiguration.get(exconfigId))?.collect{ it -> return User.get(it.id).fullName + "${it.readOnly? ' (R)':''}"} + alertService.getReadOnlyGroupsForEx(ExecutedConfiguration.get(exconfigId))?.collect{ return Group.get(it.id).name + "${it.readOnly? ' (R)':''}"}) as String
        def groupList = []
        def userList = []
        shareWithValues.flatten().each { String shared ->
            if (shared.startsWith(Constants.USER_GROUP_TOKEN)) {
                Long groupId
                Boolean readOnly
                if(shared.contains("_readOnly")){
                    groupId = Long.valueOf(shared.replaceAll(Constants.USER_GROUP_TOKEN, '').replaceAll("_readOnly", ""))
                    readOnly = true
                } else {
                    groupId = Long.valueOf(shared.replaceAll(Constants.USER_GROUP_TOKEN, ''))
                    readOnly = false
                }
                groupList.add([id:groupId,readOnly:readOnly])
            } else if (shared.startsWith(Constants.USER_TOKEN)) {
                Long userId
                Boolean readOnly
                if(shared.contains("_readOnly")){
                    userId = Long.valueOf(shared.replaceAll(Constants.USER_TOKEN, '').replaceAll("_readOnly", ""))
                    readOnly = true
                } else {
                    userId = Long.valueOf(shared.replaceAll(Constants.USER_TOKEN, ''))
                    readOnly = false
                }
                userList.add([id:userId,readOnly:readOnly])
            }
        }
        saveShareWithToExConfigWithAuditLog(exconfigId,groupList,userList,oldValue)
    }

    /**
     * This saves the sharewith user and group for an executed configuration and creates a manual auditlog for it.
     * @param exconfigId
     * @param groupList
     * @param userList
     * @param oldValue
     */
    void saveShareWithToExConfigWithAuditLog(Long exconfigId, List groupList, List userList,String oldValue){
        Sql sql
        try{
            sql = new Sql(dataSource)
            sql.execute("DELETE FROM SHARE_WITH_USER_EXCONFIG WHERE EXCONFIG_ID = ?", [exconfigId])
            sql.execute("DELETE FROM SHARE_WITH_GROUP_EXCONFIG WHERE EXCONFIG_ID = ?", [exconfigId])
            groupList.each { group ->
                sql.execute("INSERT INTO SHARE_WITH_GROUP_EXCONFIG (EXCONFIG_ID, SHARE_WITH_GROUPID, READ_ONLY) VALUES (?, ?, ?)",
                        [exconfigId, group.id, group.readOnly])
            }
            userList.each { user ->
                sql.execute("INSERT INTO SHARE_WITH_USER_EXCONFIG (EXCONFIG_ID, SHARE_WITH_USERID, READ_ONLY) VALUES (?, ?, ?)",
                        [exconfigId, user.id, user.readOnly])
            }
        }catch(Exception ex){
            log.error("Error Occured while persisting read only property for user.", ex)
        } finally {
            sql?.close()
        }
        String newValue = (alertService.getReadOnlyUsersForEx(ExecutedConfiguration.get(exconfigId))?.collect{ it -> return User.get(it.id).fullName + "${it.readOnly? ' (R)':''}"} + alertService.getReadOnlyGroupsForEx(ExecutedConfiguration.get(exconfigId))?.collect{ return Group.get(it.id).name + "${it.readOnly? ' (R)':''}"}) as String
        signalAuditLogService.createAuditLog([
                entityName: "Aggregate Review: Share With",
                moduleName: "Aggregate Review",
                category: AuditTrail.Category.UPDATE.toString(),
                entityValue: ExecutedConfiguration.get(exconfigId).name,
                username: userService.getUser().username,
                fullname: userService.getUser().fullName] as Map, [[propertyName: "shareWithExConfig", oldValue: oldValue?:"", newValue: newValue]] as List)
    }
    void notifyUpdateDispCountsForExConfigAndGroupedConfig(List<Long> exConfigList){
        notify 'update.disp.count', [exConfigList: exConfigList]
    }

    void updateDispCountsForExConfigAndGroupedConfig (Map data) {
        log.info("Disp Count Updated Started.")
        List<ExecutedConfiguration>  executedConfigList =  ExecutedConfiguration.getAll(data.exConfigList)
        executedConfigList.each { ExecutedConfiguration execConfig ->
            String oldCountValue = execConfig.dispCounts
            def domain = (execConfig.type == 'Single Case Alert')? SingleCaseAlert: AggregateCaseAlert
            def caseCountMap= domain.findAllByExecutedAlertConfiguration(execConfig).countBy {it.dispositionId as String}
            String caseCountMapJson = new JsonBuilder(caseCountMap).toPrettyString()
            log.info( "Disp Count Updated For Alert with ExConfig ID : ${execConfig.id}")
            log.info( "Existing case/PEC counts : ${oldCountValue}")
            log.info( "Updated case/PEC counts : ${caseCountMapJson}")
            execConfig.dispCounts = caseCountMapJson
            execConfig.save(flush:true)
        }
        List <Long> groupedAlertInfoIds = fetchAllGroupedInfoConfigsId(data.exConfigList)
        if(groupedAlertInfoIds){
            updateDispCountOfGroupedAlertInfos(groupedAlertInfoIds)
        }
        log.info("Disp Count Updated Ended.")
    }

    List fetchAllGroupedInfoConfigsId(List exConfigList) {
        Sql sql
        List<Long> groupedAlertInfoIds = []
        try{
            sql = new Sql(dataSource)
            String query = """
            SELECT DISTINCT ALERT_GROUP_INFO_ID
            FROM GROUP_ALERT_EXEC_CONFIG
            WHERE EXEC_CONFIG_ID IN (${exConfigList.collect { '?' }.join(', ')})
            """

            sql.eachRow(query, exConfigList) { row ->
                groupedAlertInfoIds << row.ALERT_GROUP_INFO_ID
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        } finally {
            sql?.close()
        }
        return groupedAlertInfoIds

    }

    void updateDispCountOfGroupedAlertInfos(List <Long> groupedAlertInfoIds) {
        List <GroupedAlertInfo> groupedAlertInfoList = GroupedAlertInfo.getAll(groupedAlertInfoIds)
        groupedAlertInfoList.each { GroupedAlertInfo groupedAlertInfo ->
            groupedAlertInfo.dispCounts = getDispCountsForGroupedAlertInfo(groupedAlertInfo)
            groupedAlertInfo.save(flush:true)
        }
    }

    String getDispCountsForGroupedAlertInfo(GroupedAlertInfo groupedAlertInfo) {
        List<ExecutedConfiguration> execConfigList = groupedAlertInfo.execConfigList
        Map helperMap = [:]
        JsonSlurper jsonSlurper = new JsonSlurper()
        execConfigList.each {
            Map dispCountMap = it.dispCounts ? jsonSlurper.parseText(it.dispCounts) : [:]
            helperMap = createCumulativeDispCount(dispCountMap, helperMap)
        }
        return JsonOutput.toJson(helperMap)

    }

    Map createCumulativeDispCount(Map dispCountMap, Map helperMap) {
        (dispCountMap.keySet() + helperMap.keySet()).each { key ->
            helperMap[key] = (dispCountMap.get(key) ?: 0) + (helperMap.get(key) ?: 0)
        }
        helperMap
    }

}
