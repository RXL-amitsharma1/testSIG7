package com.rxlogix.signal

import com.fasterxml.jackson.databind.ObjectMapper
import com.rxlogix.Constants
import com.rxlogix.DataObjectService
import com.rxlogix.StatisticsService
import com.rxlogix.config.Configuration
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.dto.AlertDataDTO
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.SignalQueryHelper
import grails.async.Promise
import grails.async.PromiseList
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.sql.Sql
import org.apache.commons.lang3.StringUtils
import org.apache.http.util.TextUtils
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.Transaction
import org.springframework.transaction.annotation.Propagation

import java.text.DecimalFormat
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

import static com.rxlogix.Constants.AlertConfigType.AGGREGATE_CASE_ALERT
import static grails.async.Promises.task
import static grails.async.Promises.waitAll

class AggregateOnDemandAlertService implements AlertUtil{

    StatisticsService statisticsService
    SessionFactory sessionFactory
    DataObjectService dataObjectService
    def alertService
    def aggregateCaseAlertService
    def grailsApplication
    def cacheService
    def productEventHistoryService
    def pvsGlobalTagService
    def pvsAlertTagService
    def dataSource
    def signalDataSourceService
    def emergingIssueService
    def reportExecutorService
    def dataSource_pva
    def CRUDService
    def alertFieldService

    @Transactional
    void createAlert(Long scheduledConfigId, Long executedConfigId, List alertData)throws Exception
    {
        log.info("Data mining in PV Datahub gave " + alertData.size() + " PE combinations.")
        try {
            def status = [:]
            Configuration config = Configuration.get(scheduledConfigId)
            ExecutedConfiguration executedConfig = ExecutedConfiguration.get(executedConfigId)
            List<Date> dateRangeStartEndDate = config.alertDateRangeInformation.getReportStartAndEndDate()
            List<AggregateOnDemandAlert> resultData = []
            String fileNameStr = (Holders.config.statistics.inputfile.path as String) + executedConfig.id
            List<Map> pec = aggregateCaseAlertService.fetchPECfromdata(['pva':alertData])
            List<Map> newProductEvents = aggregateCaseAlertService.fetchNewProductEvent(pec)
            pvsGlobalTagService.batchPersistGlobalProductEvents(newProductEvents)
            List<GlobalProductEvent> globalProductEventList = aggregateCaseAlertService.fetchGlobalProductEvent(pec)
            dataObjectService.setGlobalProductEventList(executedConfigId, globalProductEventList)

            boolean isProductGroup = config.productGroupSelection != null
            boolean isEventGroup = config.eventGroupSelection && config.groupBySmq
            log.info("Now calling the statistics block.")
            if(alertData && config.selectedDatasource != Constants.DataSource.PVA){
                status = statisticsService.mergeStatsScores(fileNameStr, config.selectedDatasource, executedConfig.id , alertData, isProductGroup, isEventGroup, config.dataMiningVariable?true:false)
            }else{
                status.status = 1
            }
            if(!status.status && Holders.config.statistics.enable.ebgm && alertData){
                throw new Exception("EBGM score calculation failed! " + status.error)
            }
            Integer workerCnt = Holders.config.signal.worker.count as Integer
            ExecutorService executorService = Executors.newFixedThreadPool(workerCnt)
            List eiList = alertService.getEmergingIssueList()
            log.info("Thread Starts")
            List listednessData = []
            if (executedConfig?.selectedDataSheet && !TextUtils.isEmpty(executedConfig?.selectedDataSheet)) {
                listednessData = aggregateCaseAlertService.evaluateListedness(executedConfig.id, null,false)
            }
            List<Future<AggregateOnDemandAlert>> futureList = alertData.collect { Map data ->
                executorService.submit({ ->
                    parallellyCreateAODAlert(data, config, executedConfig, dateRangeStartEndDate, eiList, listednessData)
                } as Callable)
            }
            futureList.each {
                resultData.add(it.get())
            }
            executorService.shutdown()
            log.info("Thread Ends")

            batchPersistData(resultData, executedConfig)
            alertService.updateOldExecutedConfigurations(config, executedConfigId,ExecutedConfiguration)
            aggregateCaseAlertService.invokeReportingForQuantAlert(executedConfig,null, null, null)
            printExecutionMessage(config, executedConfig, resultData)

        } catch (Throwable throwable) {
            throw throwable
        }
    }

    AggregateOnDemandAlert parallellyCreateAODAlert(Map data, Configuration config, ExecutedConfiguration executedConfig, List<Date> dateRangeStartEndDate, List eiList, List listednessData = []) {

        String[] ptCodeWithSMQ = data["PT_CODE"] ? String.valueOf(data["PT_CODE"]).split(Constants.Commons.DASH_STRING, 2) : ["0"]
        String ptCode = ptCodeWithSMQ ? ptCodeWithSMQ[0] ? ptCodeWithSMQ[0] : 0 : Constants.Commons.UNDEFINED_NUM
        Boolean isIngredient = executedConfig.productSelection ? isIngredient(executedConfig.productSelection,data["PRODUCT_NAME"]) : false
        String impEvents = alertService.setImpEventValue(data["PT"], ptCode, data["PRODUCT_NAME"] as String, data["PRODUCT_ID"],  eiList, false, executedConfig?.isMultiIngredient,isIngredient)
        Map newCountData = new HashMap()
        List<Map> newFields = alertFieldService.getAggOnDemandColumnList(Constants.DataSource.PVA,config.groupBySmq,true)
        for (int k = 0; k < newFields?.size(); k++) {
            String name = newFields?.get(k).name
            String secondaryName = newFields?.get(k).secondaryName
            String type = newFields?.get(k).type
            String keyId =  newFields?.get(k).dbKey
            if (type == "countStacked") {
                String newCount = keyId
                String cumCount = keyId.replace("NEW","CUMM")
                if (data.containsKey(newCount)) {
                    newCountData.put(name, data.get(newCount) ?: 0)
                    newCountData.put(secondaryName, data.get(cumCount) ?: 0)
                }
            }else{
                newCountData.put(name, data.get(keyId) ?: null)
            }
        }
        String newCountJsonString = new ObjectMapper().writeValueAsString(newCountData)
        AggregateOnDemandAlert aggregateOnDemandAlert = new AggregateOnDemandAlert(
                alertConfiguration: config, executedAlertConfiguration: executedConfig,
                name: executedConfig.name,
                productName: data["PRODUCT_NAME"] ?: Constants.Commons.UNDEFINED, productId: data["PRODUCT_ID"] ?: Constants.Commons.UNDEFINED_NUM,
                soc: data["SOC"] ?: Constants.Commons.UNDEFINED, pt: data["PT"] ?: Constants.Commons.UNDEFINED,
                ptCode: ptCode,
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
                listed:(executedConfig?.selectedDataSheet && !TextUtils.isEmpty(executedConfig?.selectedDataSheet))?Constants.Commons.NO: Constants.Commons.BLANK_STRING, pregnancy: data["PREGENENCY"] ?: Constants.Commons.BLANK_STRING,
                related: data["RELATEDNESS"] ?: Constants.Commons.BLANK_STRING,
                impEvents: impEvents,
                pecImpNumHigh: 0, pecImpNumLow: 0,
                //These statistics related dummy values would be overridden from statistics module.
                ebgm: 0.0, eb95: 0.0, eb05: 0.0, rorValue: 0, rorLCI: 0, rorUCI: 0, rorStr: 0, rorStrLCI: 0,
                rorStr95: 0, rorMh: 0, prrValue: 0, prrLCI: 0, prrUCI: 0, prrStr: 0, prrStrLCI: 0, prrStrUCI: 0,
                prrMh: 0, pecImpHigh: 0, pecImpLow: 0,
                aValue: 0, bValue: -1, cValue: -1, dValue: -1, eValue: -1, rrValue: -1,
                //These are freq and flags
                newCount: data["NEW_COUNT"] ?: 0, cummCount: data["CUMM_COUNT"] ?: 0,
                newPediatricCount: data["NEW_PEDIA_COUNT"] ?: 0, cummPediatricCount: data["CUMM_PEDIA_COUNT"] ?: 0,
                newInteractingCount: data["NEW_INTERACTING_COUNT"] ?: 0,
                cummInteractingCount: data["CUMM_INTERACTING_COUNT"] ?: 0,
                newGeriatricCount: data["NEW_GERIA_COUNT"] ?: 0,
                cumGeriatricCount: data["CUMM_GERIA_COUNT"] ?: 0,
                newNonSerious: data["NEW_NON_SERIOUS_COUNT"] ?: 0,
                cumNonSerious: data["CUMM_NON_SERIOUS_COUNT"] ?: 0,
                chiSquare: 0, smqCode: ptCodeWithSMQ.size() > 1 ? ptCodeWithSMQ[1] : null,
                prodHierarchyId: data["PROD_HIERARCHY_ID"]?:Constants.Commons.UNDEFINED_NUM_INT_REVIEW,
                eventHierarchyId: data["EVENT_HIERARCHY_ID"]?:Constants.Commons.UNDEFINED_NUM_INT_REVIEW,
                newCountsJSON: newCountJsonString
        )
        statisticsService.setDefaultSubGroupValues(aggregateOnDemandAlert,executedConfig?.dataMiningVariable as boolean)
        if (aggregateOnDemandAlert.impEvents && aggregateOnDemandAlert.impEvents != Constants.Commons.BLANK_STRING) {
            String[] impEventList = aggregateOnDemandAlert.impEvents?.split(',')
            aggregateOnDemandAlert.aggImpEventList = []
            impEventList.each {
                if (it) {
                    aggregateOnDemandAlert.aggImpEventList.add(it.trim())
                }
            }
        }
        if (executedConfig?.selectedDataSheet && !TextUtils.isEmpty(executedConfig?.selectedDataSheet)) {
            listednessData?.each{
                if(it["ptCode"] == ptCode) {
                    aggregateOnDemandAlert.listed = it["listed"]?:Constants.Commons.NO
                }
            }
            if( aggregateOnDemandAlert.listed == null || TextUtils.isEmpty(aggregateOnDemandAlert.listed) ){
                aggregateOnDemandAlert.listed = Constants.Commons.NO
            }
        }
        String peComb = aggregateOnDemandAlert.smqCode ? aggregateOnDemandAlert.productId + "-" + aggregateOnDemandAlert.ptCode + "-" + aggregateOnDemandAlert.smqCode : aggregateOnDemandAlert.productId + "-" + aggregateOnDemandAlert.ptCode + "-" + 'null'
        aggregateOnDemandAlert.globalIdentity = dataObjectService.getGlobalProductEventList(executedConfig.id, peComb)
        aggregateOnDemandAlert
    }

    @Transactional
    void batchPersistData(List<AggregateOnDemandAlert> alertList, ExecutedConfiguration executedConfig) {
        Long time1 = System.currentTimeMillis()
        log.info("Now persisting the execution related data in a batch.")
        boolean isEvntGroup = executedConfig.eventGroupSelection && executedConfig.groupBySmq
        //Persist the alerts
        AggregateOnDemandAlert.withTransaction {
            List<AggregateOnDemandAlert> batch = []
            for (AggregateOnDemandAlert alert : alertList) {
                batch += alert
                if (batch.size() > Holders.config.signal.batch.size) {
                    Session session = sessionFactory.currentSession
                    for (AggregateOnDemandAlert alertIntance in batch) {
                        setStatisticsScoresValues(alertIntance, executedConfig.productGroupSelection, isEvntGroup)
                        setPrrRorScoresValues(alertIntance, executedConfig.productGroupSelection, isEvntGroup)
                        setStatsSubgroupingScoresValues(alertIntance, executedConfig.productGroupSelection, isEvntGroup)
                        alertIntance.save(validate: false)
                    }
                    session.flush()
                    session.clear()
                    batch.clear()
                }
            }
            if (batch) {
                Session session = sessionFactory.currentSession
                for (AggregateOnDemandAlert alertIntance in batch) {
                    setStatisticsScoresValues(alertIntance, executedConfig.productGroupSelection, isEvntGroup)
                    setPrrRorScoresValues(alertIntance, executedConfig.productGroupSelection, isEvntGroup)
                    setStatsSubgroupingScoresValues(alertIntance, executedConfig.productGroupSelection, isEvntGroup)
                    alertIntance.save(validate: false)
                }
                session.flush()
                session.clear()
                batch.clear()

            }
            log.info("Alert data is batch persisted.")
        }

        log.info("Persistance of execution related data in a batch is done.")
        Long time2 = System.currentTimeMillis()
        log.info(((time2 - time1) / 1000) + " Secs were taken in the persistance of data for configuration "
                + executedConfig.id)
    }

    private setStatisticsScoresValues(AggregateOnDemandAlert aggregateOnDemandAlert, String productGroupSelection, boolean isEventGroup) {
        Map statsData
        String ptCodeString = isEventGroup ? aggregateOnDemandAlert.ptCode + "-EG" : aggregateOnDemandAlert.ptCode
        String productIdString = (productGroupSelection && !aggregateOnDemandAlert?.executedAlertConfiguration?.dataMiningVariable) ? aggregateOnDemandAlert.productId + "-PG" : aggregateOnDemandAlert.productId


        if (aggregateOnDemandAlert.smqCode)
            statsData = dataObjectService.getStatsDataMap(aggregateOnDemandAlert.executedAlertConfiguration.id, productIdString,
                    ptCodeString + Constants.Commons.DASH_STRING + aggregateOnDemandAlert.smqCode)
        else
            statsData = dataObjectService.getStatsDataMap(aggregateOnDemandAlert.executedAlertConfiguration.id, productIdString, ptCodeString)
        if (statsData) {
            aggregateOnDemandAlert.ebgm = statsData.ebgm as Double
            aggregateOnDemandAlert.eb05 = statsData.eb05 as Double
            aggregateOnDemandAlert.eb95 = statsData.eb95 as Double
        } else {
            aggregateOnDemandAlert.ebgm = 0 as Double
            aggregateOnDemandAlert.eb05 = 0 as Double
            aggregateOnDemandAlert.eb95 = 0 as Double
        }
    }

    private setPrrRorScoresValues(AggregateOnDemandAlert aggregateOnDemandAlert, String productGroupSelection, boolean isEventGroup) {
        Map statsData
        Map rorStatsData
        Map prrSubGroupStatsData
        Map rorSubGroupStatsData
        ExecutedConfiguration executedConfiguration = aggregateOnDemandAlert.executedAlertConfiguration
        boolean isSafetyAlert = false
        if (Objects.nonNull(executedConfiguration)) {
            isSafetyAlert = StringUtils.contains(executedConfiguration.selectedDatasource, Constants.DataSource.PVA)
        }
        String ptCodeString = ""
        if (isEventGroup) {
            ptCodeString = aggregateOnDemandAlert.ptCode + "-EG"
        } else {
            ptCodeString = aggregateOnDemandAlert.ptCode
        }

        String productIdString = ""

        if (productGroupSelection && !aggregateOnDemandAlert?.executedAlertConfiguration?.dataMiningVariable) {
            productIdString = "${aggregateOnDemandAlert.productId}-PG"
        } else {
            productIdString = aggregateOnDemandAlert.productId
        }

        if (aggregateOnDemandAlert.smqCode) {
            statsData = dataObjectService.getProbDataMap(executedConfiguration.id, productIdString,
                    ptCodeString + Constants.Commons.DASH_STRING + aggregateOnDemandAlert.smqCode)
            rorStatsData = dataObjectService.getRorProbDataMap(executedConfiguration.id, productIdString,
                    ptCodeString + Constants.Commons.DASH_STRING + aggregateOnDemandAlert.smqCode)
            prrSubGroupStatsData = dataObjectService.getPrrSubGroupDataMap(executedConfiguration.id, productIdString,
                    ptCodeString + Constants.Commons.DASH_STRING + aggregateOnDemandAlert.smqCode)
            rorSubGroupStatsData = dataObjectService.getRorSubGroupDataMap(executedConfiguration.id, productIdString,
                    ptCodeString + Constants.Commons.DASH_STRING + aggregateOnDemandAlert.smqCode)
        }else {
            statsData = dataObjectService.getProbDataMap(executedConfiguration.id, productIdString, ptCodeString)
            rorStatsData = dataObjectService.getRorProbDataMap(executedConfiguration.id, productIdString,ptCodeString)
            prrSubGroupStatsData = dataObjectService.getPrrSubGroupDataMap(executedConfiguration.id, productIdString,ptCodeString)
            rorSubGroupStatsData = dataObjectService.getRorSubGroupDataMap(executedConfiguration.id, productIdString,ptCodeString)
        }

        if (statsData) {
            aggregateOnDemandAlert.prrValue = statsData.prrValue!=null ? statsData.prrValue as Double : 0
            aggregateOnDemandAlert.prrUCI = statsData.prrUCI!=null ? statsData.prrUCI as Double : 0
            aggregateOnDemandAlert.prrLCI = statsData.prrLCI!=null ? statsData.prrLCI as Double : 0
            aggregateOnDemandAlert.aValue = statsData.aValue as Double
            aggregateOnDemandAlert.bValue = statsData.bValue as Double
            aggregateOnDemandAlert.cValue = statsData.cValue as Double
            aggregateOnDemandAlert.dValue = statsData.dValue as Double
            aggregateOnDemandAlert.eValue = statsData.eValue as Double
            aggregateOnDemandAlert.rrValue = statsData.rrValue as Double
            if (!isSafetyAlert) {
                aggregateOnDemandAlert.rorValue = statsData.rorValue!=null ? statsData.rorValue as Double : 0
                aggregateOnDemandAlert.rorLCI = statsData.rorLCI!=null ? statsData.rorLCI as Double : 0
                aggregateOnDemandAlert.rorUCI = statsData.rorUCI!=null ? statsData.rorUCI as Double : 0
                aggregateOnDemandAlert.chiSquare = statsData.chiSquare!=null ? statsData.chiSquare as Double : 0
            }
        }
        if(rorStatsData && isSafetyAlert){ //Changed this as the data for PVA only comes in rorStatsData for other datasource ROR data stored in  statsData
            aggregateOnDemandAlert.rorValue = rorStatsData.rorValue!=null ? rorStatsData.rorValue as Double : 0
            aggregateOnDemandAlert.rorLCI = rorStatsData.rorLCI!=null ? rorStatsData.rorLCI as Double : 0
            aggregateOnDemandAlert.rorUCI = rorStatsData.rorUCI!=null ? rorStatsData.rorUCI as Double : 0
            aggregateOnDemandAlert.chiSquare = rorStatsData.chiSquare!=null ? rorStatsData.chiSquare as Double : 0
        }
        if(prrSubGroupStatsData){
            aggregateOnDemandAlert.prrSubGroup = prrSubGroupStatsData.prrSubGroup
            aggregateOnDemandAlert.prrLciSubGroup = prrSubGroupStatsData.prrLciSubGroup
            aggregateOnDemandAlert.prrUciSubGroup = prrSubGroupStatsData.prrUciSubGroup
        }
        if(rorSubGroupStatsData){
            aggregateOnDemandAlert.rorSubGroup = rorSubGroupStatsData.rorSubGroup
            aggregateOnDemandAlert.rorLciSubGroup = rorSubGroupStatsData.rorLciSubGroup
            aggregateOnDemandAlert.rorUciSubGroup = rorSubGroupStatsData.rorUciSubGroup
            aggregateOnDemandAlert.chiSquareSubGroup = rorSubGroupStatsData.chiSquareSubGroup
            aggregateOnDemandAlert.rorRelSubGroup = rorSubGroupStatsData.rorRelSubGroup
            aggregateOnDemandAlert.rorLciRelSubGroup = rorSubGroupStatsData.rorLciRelSubGroup
            aggregateOnDemandAlert.rorUciRelSubGroup = rorSubGroupStatsData.rorUciRelSubGroup
        }
    }

    private setStatsSubgroupingScoresValues(AggregateOnDemandAlert aggregateOnDemandAlert, String productGroupSelection, boolean isEventGroup) {
        Map statsData

        String ptCodeString = isEventGroup ? aggregateOnDemandAlert.ptCode + "-EG" : aggregateOnDemandAlert.ptCode
        String productIdString = (productGroupSelection && !aggregateOnDemandAlert?.executedAlertConfiguration?.dataMiningVariable) ? aggregateOnDemandAlert.productId + "-PG" : aggregateOnDemandAlert.productId

        Map ebgmSubGroupStatsData
        if (aggregateOnDemandAlert.smqCode) {
            statsData = dataObjectService.getStatsDataMapSubgrouping(aggregateOnDemandAlert.executedAlertConfiguration.id, productIdString,
                    ptCodeString + Constants.Commons.DASH_STRING + aggregateOnDemandAlert.smqCode)
            ebgmSubGroupStatsData = dataObjectService.getEbgmStatsDataMapSubgrouping(aggregateOnDemandAlert.executedAlertConfigurationId, productIdString,
                    ptCodeString + Constants.Commons.DASH_STRING + aggregateOnDemandAlert.smqCode)
        }else {
            statsData = dataObjectService.getStatsDataMapSubgrouping(aggregateOnDemandAlert.executedAlertConfiguration.id, productIdString, ptCodeString)
            ebgmSubGroupStatsData = dataObjectService.getEbgmStatsDataMapSubgrouping(aggregateOnDemandAlert.executedAlertConfigurationId, productIdString, ptCodeString)
        }

        if (statsData) {
            aggregateOnDemandAlert.ebgmAge = statsData.ebgmAge
            aggregateOnDemandAlert.eb05Age = statsData.eb05Age
            aggregateOnDemandAlert.eb95Age = statsData.eb95Age
            aggregateOnDemandAlert.ebgmGender = statsData.ebgmGender
            aggregateOnDemandAlert.eb05Gender = statsData.eb05Gender
            aggregateOnDemandAlert.eb95Gender = statsData.eb95Gender
        }
        if(ebgmSubGroupStatsData){
            aggregateOnDemandAlert.ebgmSubGroup = ebgmSubGroupStatsData.ebgmSubGroup
            aggregateOnDemandAlert.eb05SubGroup = ebgmSubGroupStatsData.eb05SubGroup
            aggregateOnDemandAlert.eb95SubGroup = ebgmSubGroupStatsData.eb95SubGroup
        }
    }

    void printExecutionMessage(Configuration config,
                               ExecutedConfiguration executedConfig,
                               alertData) {
        String executionMessage = "Execution of Configuration took ${executedConfig.totalExecutionTime}ms " +
                "for configuration ${config.name} [C:${config.id}, EC: ${executedConfig.id}]. " +
                "It gave ${alertData ? alertData.size() : 0} PE combinations"
        log.info(executionMessage)
        log.info("Alert data save flow is complete.")
    }

    List<Map> fetchResultAlertList(List<AggregateOnDemandAlert> agaList, AlertDataDTO alertDataDTO, Boolean isExport = false) {
        Map params = alertDataDTO.params
        Boolean showSpecialPE = Boolean.parseBoolean(params.specialPE)
        fetchValuesForAggregatedReport(agaList, showSpecialPE, isExport)
    }

    List fetchValuesForAggregatedReport(List<AggregateOnDemandAlert> agaList,
                                        Boolean showSpecialPE, Boolean isExport = false) {

        List list = []
        Boolean ime = false, dme = false, ei = false, sm = false
        List<Map> returnValue = []
        List<Long> alertIdList = agaList.collect {it.id}
        List<Map> alertTagNameList = pvsAlertTagService.getAllAlertSpecificTags(alertIdList, Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND)
        List<Map> globalTagNameList = pvsGlobalTagService.getAllGlobalTags(agaList.collect {it.globalIdentityId}, Constants.AlertConfigType.AGGREGATE_CASE_ALERT, Constants.Commons.REVIEW)

        ExecutorService executorService = Executors.newFixedThreadPool(50)
        List<Future> futureList = agaList.collect { aga ->
            executorService.submit({ ->
                List imgList = aga?.impEvents?.split(',')
                List<Map> peAlertTags = alertTagNameList.findAll{it.alertId == aga.id}
                List<Map> globalTags = globalTagNameList.findAll{it.globalId == aga.globalIdentityId }
                globalTags = globalTags.unique(false) { a, b ->
                    a.tagText <=> b.tagText
                }
                List<Map> allTags = peAlertTags + globalTags
                List<Map> tagNameList = allTags.sort{tag1 , tag2 -> tag1.priority <=> tag2.priority}
                Map ptMap = [isIme: imgList?.contains('ime') ? "true" : "false",
                             isDme: imgList?.contains('dme') ? "true" : "false",
                             isEi : imgList?.contains('ei') ? "true" : "false",
                             isSm : imgList?.contains('sm') ? "true" : "false"]
                aga.toDto(showSpecialPE, ptMap, tagNameList, isExport)
            } as Callable)
        }
        futureList.each {
            list.add(it.get())
        }
        executorService.shutdown()
        list
    }

    List<Map> fieldListAdvanceFilter(Boolean isFaersEnabled, Boolean isPvaEnabled,Boolean isVaersEnabled, Boolean isVigibaseEnabled, Boolean isJaderEnabled, String dataMiningVariable = null, Boolean groupBySmq) {
        List fieldList = alertFieldService.getAggOnDemandColumnList(Constants.DataSource.PVA,groupBySmq)
        if(isVaersEnabled){
            fieldList  = alertFieldService.getAggOnDemandColumnList(Constants.DataSource.VAERS,groupBySmq)
        } else if (isVigibaseEnabled) {
            fieldList  = alertFieldService.getAggOnDemandColumnList(Constants.DataSource.VIGIBASE,groupBySmq)
        } else if (isFaersEnabled) {
            fieldList  = alertFieldService.getAggOnDemandColumnList(Constants.DataSource.FAERS,groupBySmq)
        } else if (isJaderEnabled) {
            fieldList  = alertFieldService.getAggOnDemandColumnList(Constants.DataSource.JADER,groupBySmq)
        }
        Map subGroupColumnInfo = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT').findAll{it.type=="subGroup" && it.enabled== true}.collectEntries {
            b -> [b.name, b.display]
        }

        Boolean isRor = cacheService.getRorCache()
        List newFieldList = []
        fieldList.each {
            if (isRor == false && !isVigibaseEnabled) {
                if (it.display.contains('ROR') && !it.display.contains('iROR') && !it.display.contains('(E)') && !it.display.contains('(F)')) {
                    it.display = it.display.replaceAll('ROR', 'iROR')
                }
            }
            String[] displayName = it.display?.split("/")
            if(it.secondaryName && it.secondaryName !="" && displayName.size() > 1){
                newFieldList.add(["name":it.name,"display":displayName[0],"dataType":it.dataType])
                newFieldList.add(["name":it.secondaryName,"display":displayName[1],"dataType":it.dataType])
            }else{
                if(it.isAutocomplete) {
                    newFieldList.add(["name": it.name, "display": it.display, "dataType": it.dataType,"isAutocomplete":true])
                }else{
                    if(it.name?.toString().equals("alertTags")){
                        newFieldList.add(["name": "tags", "display": it.display, "dataType": it.dataType])
                    }else {
                        newFieldList.add(["name": it.name, "display": it.display, "dataType": it.dataType])
                    }
                }
            }
        }
        fieldList = newFieldList
        fieldList.add([name: "subTags", display: "Sub Categories", dataType: "java.lang.String", 'isAutocomplete': false,enabled:true])
        fieldList.removeAll{it?.name?.toString().equals("impEvents")}
        fieldList.add( [ name: "aggImpEventList", display: "IMP Events" , dataType: "java.lang.String" ] )
        if(dataMiningVariable){
            fieldList.removeAll {it.name == "productName"}
            fieldList.add(
                    [name: "productName", display: "${dataMiningVariable}", dataType: 'java.lang.String']
            )
        }
        if (isVigibaseEnabled && groupBySmq) {
            fieldList.remove(
                    [name: "listed", display: "Listed", dataType: "java.lang.String"]
            )
        }
        List subGrpFieldList = []
        List subGroup = cacheService.getSubGroupColumns()?.flatten()
        List subGroupFaers = cacheService.getSubGroupColumnFaers()?.flatten()
        List subGroupValue = grailsApplication.config.businessConfiguration.attributes.aggregate.subGroup.clone()
        if (!dataMiningVariable) {
            subGroupValue.each { def sub ->
                if (isPvaEnabled) {
                    subGroup.each {
                        String key = sub.value.toLowerCase() + it.value
                        if(key in subGroupColumnInfo?.keySet()) {
                            String value = sub.value + ':' + it.value
                            String text = subGroupColumnInfo.get(key)
                            subGrpFieldList.add([name: value, display: text, dataType: 'java.lang.Number'])
                        }
                    }
                }
                if (isFaersEnabled) {
                    subGroupFaers.each {
                        String key = sub.value.toLowerCase() + it.value + "Faers"
                        if(key in subGroupColumnInfo?.keySet()) {
                            String valueFaers = sub.value + ':' + it.value
                            String textFaers = subGroupColumnInfo.get(key)
                            subGrpFieldList.add([name: valueFaers, display: textFaers, dataType: 'java.lang.Number'])
                        }
                    }
                }
            }
            if (isPvaEnabled) {
                Map<String, List<String>> getAllOtherSubGroupColumnsListMap = cacheService.getAllOtherSubGroupColumnsListMap(Constants.DataSource.PVA)
                Map<String, List<String>> relativeSubGroupMap = cacheService.getRelativeSubGroupColumnsListMap(Constants.DataSource.PVA)
                getAllOtherSubGroupColumnsListMap?.each { subGrp, subGroupList ->
                    subGroupList?.each {
                        String key = cacheService.toCamelCase(subGrp as String) + it.value
                        if(key in subGroupColumnInfo?.keySet()) {
                            String value = subGrp + ':' + it.value
                            String text =  subGroupColumnInfo.get(key)
                            subGrpFieldList.add([name: value, display: text, dataType: 'java.lang.Number'])
                        }
                    }
                }
                relativeSubGroupMap?.each { sub, subGroupList ->
                    subGroupList?.each {
                        String key = cacheService.toCamelCase(sub as String) +"el" +  it.value
                        if(key in subGroupColumnInfo?.keySet()) {
                            String value = sub + ':' + it.value
                            String text =  subGroupColumnInfo.get(key)
                            subGrpFieldList.add([name: value, display: text, dataType: 'java.lang.Number'])
                        }
                    }
                }
            }
            // Removed for PVS-66698
        }
        subGrpFieldList.addAll(fieldList)
        subGrpFieldList
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void deleteOnDemandAlert(ExecutedConfiguration executedConfiguration) {
        /**
         * Commented code due to creating new connection not taking from pool
        Sql sql = new Sql(signalDataSourceService.getReportConnection("dataSource"))
         */
        Sql sql = null
        try {
            sql = new Sql(dataSource)
            executedConfiguration.setIsEnabled(false)
            executedConfiguration.setIsDeleted(true)
            CRUDService.updateWithAuditLog(executedConfiguration)

            Configuration configuration = Configuration.get(executedConfiguration.configId)
            configuration.setIsEnabled(false)
            configuration.setIsDeleted(true)
            CRUDService.updateWithAuditLog(configuration)
        } catch (Exception ex) {
            log.error(ex.printStackTrace())
        } finally {
            sql?.close()
        }
    }

    void bulkUpdateProdIdAggregateOnDemandAlert(Integer prod_hierarchy_id, List<Long> executedConfigurationIds) {
        Sql sql = new Sql(dataSource)
        try {
            sql.withTransaction {
                executedConfigurationIds.collate(999).each { list ->
                    String query = "UPDATE AGG_ON_DEMAND_ALERT SET prod_hierarchy_id = ${prod_hierarchy_id} WHERE EXEC_CONFIGURATION_ID IN (" + list.join(",").replace("'", "") + ")"
                    sql.execute(query.replace("'", ""))
                }
            }
        }
        catch (Exception ex) {
            println(ex)
            println("######## Exception while Updating the prod_hierarchy_id for Aggregate On Demand Alert. ###########")
        }
        finally {
            sql.close()
        }
    }

    /**
     * For pva dataSource only
     */
    @Transactional
    void migrateProdIdAggregateOnDemandAlerts() {
        /**
         * Commented code due to creating new connection not taking from pool
        Sql sql = new Sql(signalDataSourceService.getReportConnection("pva"))
         */
        Sql sql = null
        try {
            sql = new Sql(dataSource_pva)
            log.info("executing migrateProdIdAggregateOnDemandAlerts")
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
                bulkUpdateProdIdAggregateOnDemandAlert(it.key as Integer, it.value)
            }

            def afterTime = System.currentTimeMillis()
            log.info("It took " + (afterTime - beforeTime) / 1000 + " seconds while migrating the Aggregate On Demand Alert for Product Hirearchy Id")

        }
        catch (Exception ex) {
            println(ex)
            println("##########Exception came while migrating the Aggregate On Demand Alert for Product Hirearchy Id################")
        }
        finally {
            if (Objects.nonNull(sql)) {
                sql.close()
            }
        }
    }
    void migrateSmqForAggregateOnDemandAlerts() {
        Sql sql = new Sql(dataSource)
        try {
            log.info("executing migrateSmqForAggregateOnDemandAlerts")
            def beforeTime = System.currentTimeMillis()
            sql.withTransaction {
                sql.execute(SignalQueryHelper.update_agg_on_demand_alert_for_smq())
            }
            def afterTime = System.currentTimeMillis()
            log.info("It took " + (afterTime - beforeTime) / 1000 + " seconds for migrating the Aggregate On Demand alert for Smq Id#")
        }
        catch (Exception ex) {
            println(ex)
            println("######## Exception while Updating the Smq Hirearchy in Aggregate On Demand alert table. ###########")
        }
        finally {
            sql.close()
        }
    }

}
