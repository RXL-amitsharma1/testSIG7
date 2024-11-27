package com.rxlogix.signal

import com.rxlogix.*
import com.rxlogix.cache.CacheService
import com.rxlogix.config.AlertProgressExecutionType
import com.rxlogix.config.AppAlertProgressStatus
import com.rxlogix.config.Configuration
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutionStatus
import com.rxlogix.dto.AlertDataDTO
import grails.async.Promise
import grails.gorm.transactions.Transactional
import grails.util.Holders
import grails.validation.ValidationException
import groovy.json.JsonBuilder
import groovy.sql.Sql
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import org.springframework.transaction.annotation.Propagation

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

import static grails.async.Promises.task

class SingleOnDemandAlertService {

    def userService
    def alertService
    def dataObjectService
    def singleCaseAlertService
    def cacheService
    def pvsAlertTagService
    def pvsGlobalTagService
    def dataSource
    def grailsApplication
    def signalDataSourceService
    def appAlertProgressStatusService
    def CRUDService

    /**
     * Method to create the alert based on passed alert data.
     * @param config
     * @param executedConfig
     * @param alertData
     * @param justification
     * @param isAddCase
     * @return
     */
    List<String> createAlert(Configuration config, ExecutedConfiguration executedConfig, List<Map> alertData, ExecutionStatus executionStatus = null) {
        List<String> caseMapperList = []
        List<Map> caseVersionList = []
        ExecutedConfiguration.withNewSession {
            executedConfig = ExecutedConfiguration.get(executedConfig.id)
        }
        try {
            List<Date> dateRangeStartEndDate = executedConfig.executedAlertDateRangeInformation?.getReportStartAndEndDate()
            List<Long> prevExecConfigId = alertService.fetchPrevExecConfigId(executedConfig, config)
            Map advancedFilterMap = [:]
            Map<String, String> advancedFilterRptMap = [:]
            grailsApplication.config.advancedFilter.rpt.field.map.each{key, val ->
                advancedFilterRptMap.put(key,cacheService.getRptFieldIndexCache(val))
            }
            List<Map> prevSingleCaseAlertList = alertService.fetchPrevPeriodSCAlerts(SingleOnDemandAlert, prevExecConfigId)
            log.info("Previous Single Case Alert List Size : ${prevSingleCaseAlertList.size()}")

            alertData.each {
                Integer followUpNumber = (it[cacheService.getRptFieldIndexCache('masterFupNum')]) as Integer
                followUpNumber = followUpNumber ? (followUpNumber - 1) : followUpNumber

                Map prevalertMap = prevSingleCaseAlertList.find { map ->
                    map.caseNumber == it[cacheService.getRptFieldIndexCache('masterCaseNum')]
                }

                if (prevalertMap && prevalertMap.followUpNumber != followUpNumber) {
                    it.put("isFollowUpExists", true)
                } else if (!prevalertMap) {
                    it.put("isNew", true)
                }
                advancedFilterMap = [:]
                advancedFilterRptMap.each { String key, String value ->
                    if (it[value]) {
                        Set<String> list = advancedFilterMap.get(key) as Set
                        if (!list) {
                            list = new HashSet<>()
                        }
                        list.add(it[value])
                        advancedFilterMap.put(key, list as List<String>)
                    }
                }

                caseMapperList.add(it[cacheService.getRptFieldIndexCache('masterCaseNum')] as String)
                caseVersionList.add([caseNumber: it[cacheService.getRptFieldIndexCache('masterCaseNum')], versionNumber: it[cacheService.getRptFieldIndexCache('masterVersionNum')]])
            }
            advancedFilterMap.each { String key, List<String> value ->
                cacheService.saveAdvancedFilterPossibleValues(key, value)
            }

            alertData.collate(5000).each { List<Map> alertDataMapList ->
                List<SingleOnDemandAlert> singleOnDemandAlerList =
                        processAlertData(alertDataMapList, config, executedConfig,
                                dateRangeStartEndDate)

                singleOnDemandAlerList.removeAll([null])
                log.info("Save Single Case Alert")
                //persist start
                try {
                    appAlertProgressStatusService.createAppAlertProgressStatus(executionStatus, executedConfig
                            .id, Constants.AlertProgress.PERSIST, 2, 2, System.currentTimeMillis(), AlertProgressExecutionType.PERSIST)
                        alertService.batchPersistForDomain(singleOnDemandAlerList, SingleOnDemandAlert.class.name)
                } catch(Throwable persistEx) {
                    log.error("Alert got failed while persisting data. " + ExceptionUtils.getStackTrace(persistEx))
                    throw new Exception("Alert got failed while persisting data in app schema. "+ persistEx.toString())
                }

            }
            log.info("Now saving the case series in mart for reporting purposes.")
            try {
                boolean isCaseSeriesGenerated = singleCaseAlertService.invokeReportingForQualAlert(executedConfig, caseVersionList, config)
                if (isCaseSeriesGenerated) {
                    alertService.updateOldExecutedConfigurations(config, executedConfig.id, ExecutedConfiguration)
                    boolean isCumCaseSeries = alertService.isCumCaseSeriesReport(executedConfig) || alertService.isCumCaseSeriesSpotfire(executedConfig)
                    boolean isGenerateReport = executedConfig.executedTemplateQueries?.size() > 0
                    boolean isSpotfire = executedConfig.spotfireSettings != null
                    Promise promise = task {
                        singleCaseAlertService.reportingInBackground(executedConfig.id, isCumCaseSeries)
                    }
                    promise.onError { Throwable th ->
                        singleCaseAlertService.updateReportStatusForError(executedConfig)
                        alertService.sendReportingErrorNotifications(executedConfig, isGenerateReport, isSpotfire)
                        dataObjectService.clearfirstVersionExecMap(executedConfig.id)
                        if (isCumCaseSeries) {
                            dataObjectService.clearCummCaseSeriesGeneratedMap(config.id)
                            dataObjectService.removeCumCaseSeriesThread(config.id)
                        }
                    }
                    promise.onComplete { boolean isReportingCompleted ->
                        if (!isReportingCompleted) {
                            singleCaseAlertService.updateReportStatusForError(executedConfig)
                            alertService.sendReportingErrorNotifications(executedConfig, isGenerateReport, isSpotfire)
                        }
                        dataObjectService.clearfirstVersionExecMap(executedConfig.id)
                        if (isCumCaseSeries) {
                            dataObjectService.clearCummCaseSeriesGeneratedMap(config.id)
                            dataObjectService.removeCumCaseSeriesThread(config.id)
                        }
                    }
                }
                appAlertProgressStatusService.updateAppAlertProgressStatus(executionStatus.id, executedConfig.id, Constants.AlertProgress.PERSIST, 3, 3, System.currentTimeMillis())
                singleCaseAlertService.updateExecutionStatus(config, isCaseSeriesGenerated)
                log.info("Case Series Saved.")
            } catch (Throwable caseSeriesEx) {
                log.error("Alert got failed while saving the case series in mart for reporting purposes. " + ExceptionUtils.getStackTrace(caseSeriesEx))
                throw new Exception("Alert got failed while saving the case series in mart for reporting purposes. "+ caseSeriesEx.toString())
            }
            String executionMessage = "Execution of Configuration took ${executedConfig.totalExecutionTime} ms for configuration ${config.name} [C:${config.id}, EC: ${executedConfig.id}]. It gave ${alertData ? alertData.size() : 0} Cases"
            log.info(executionMessage)
            log.info("Alert data save is complete.")
            if (alertService.isProductSecurity()) {
                alertService.addProductInCacheForSingleAlerts(executedConfig.id, SingleOnDemandAlert)
            }
        } catch (ValidationException vex) {
            vex.printStackTrace()
            log.error(vex.getMessage())
            throw vex
        } catch (Throwable ex) {
            log.error("Alert got failed while creating single on demand alert. "+ ExceptionUtils.getStackTrace(ex))
            if (StringUtils.isBlank(ex.getMessage())) {
                log.error("Alert got failed while creating single on demand alert. "+ ExceptionUtils.getStackTrace(ex))
                throw new Exception("Alert got failed while creating single on demand alert. "+ex.toString())
            }
            throw ex
        }
        return caseMapperList
    }


    private List<SingleOnDemandAlert> processAlertData(List<Map> alertData, Configuration config, ExecutedConfiguration executedConfig,
                                                       List dateRangeStartEndDate) {
        List<String> newCaseIds = singleCaseAlertService.fetchNewCases(alertData)
        pvsGlobalTagService.batchPersistGlobalCase(newCaseIds)
        List<GlobalCase> globalCaseList = singleCaseAlertService.fetchGlobalCases(alertData)
        List<GlobalCase> existingGlobalCaseList = dataObjectService.getGlobalCaseList(executedConfig.id)
        dataObjectService.setGlobalCaseList(executedConfig.id, globalCaseList + existingGlobalCaseList)
        List<SingleOnDemandAlert> allAlerts = []
        Integer workerCnt = Holders.config.signal.worker.count as Integer
        ExecutorService executorService = Executors.newFixedThreadPool(workerCnt)
        log.info("Thread Starts")
        List<Future<SingleOnDemandAlert>> futureList = alertData.collect { Map data ->
            executorService.submit({ ->
                createSingleOnDemandAlert(data, config, executedConfig, dateRangeStartEndDate)
            } as Callable)
        }
        futureList.each {
            allAlerts.add(it.get())
        }
        executorService.shutdown()
        log.info("Thread Ends")
        allAlerts
    }

    protected SingleOnDemandAlert createSingleOnDemandAlert(Map data, Configuration config, ExecutedConfiguration executedConfig,
                                                            List dateRangeStartEndDate) {
        boolean isPVCM = dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM)
        String caseNumber = data[cacheService.getRptFieldIndexCache('masterCaseNum')]
        String versionNumber = data[cacheService.getRptFieldIndexCache('masterVersionNum')]
        String productFamily = data[cacheService.getRptFieldIndexCache('productFamilyId')] ?: Constants.Commons.UNDEFINED
        Integer followUpNumber = (data[cacheService.getRptFieldIndexCache('masterFupNum')]) as Integer
        String productName
        productName = data[cacheService.getRptFieldIndexCache('productProductId')] ?: (data[cacheService.getRptFieldIndexCache('productProductName')] ?: Constants.Commons.UNCODED)
        BigInteger productId = data[cacheService.getRptFieldIndexCache('cpiProdIdResolved')] ?: Constants.Commons.UNDEFINED_NUM as BigInteger
        String pt = data[cacheService.getRptFieldIndexCache('masterPrimEvtPrefTerm')] ?: (data[cacheService.getRptFieldIndexCache('eventDescReptd')] ?: Constants.Commons.UNDEFINED)
        String listedness = data[cacheService.getRptFieldIndexCache('assessListedness')] ?: Constants.Commons.UNDEFINED
        String outcome = data[cacheService.getRptFieldIndexCache('assessOutcome')] ?: Constants.Commons.BLANK_STRING
        String appTypeAndNum = singleCaseAlertService.getAppTypeAndNumValue(data)
        Double patientAge

        if (Holders.config.custom.qualitative.fields.enabled && data[cacheService.getRptFieldIndexCache('casePatInfoPvrUdNumber2')] != null) {
            patientAge = data[cacheService.getRptFieldIndexCache('casePatInfoPvrUdNumber2')] as Double
        } else if (data[cacheService.getRptFieldIndexCache('patInfoPatientAgeYears')] != null) {
            patientAge = data[cacheService.getRptFieldIndexCache('patInfoPatientAgeYears')] as Double
        }


        Map scaData = [caseNumber           : caseNumber, versionNumber: versionNumber, productFamily: productFamily,
                       productName          : productName, productId: productId, pt: pt, followUpNumber: followUpNumber,
                       dateRangeStartEndDate: dateRangeStartEndDate, listedness : listedness, outcome: outcome,
                       patientAge: patientAge, appTypeAndNum: appTypeAndNum, isNew: data.containsKey("isNew")]
        Map SCAJsonFieldMap = grailsApplication.config.advancedFilter.singleCaseAlert.jsonFieldMap
        Map jsonField = [:]
        SCAJsonFieldMap.each { k, v ->
            jsonField.put("${v}", [])
        }

        SingleOnDemandAlert singleOnDemandAlert = saveFieldsInSingleCaseAlert( config, executedConfig, scaData, data)
        singleCaseAlertService.splitDataForAdvanceFilter(singleOnDemandAlert, jsonField)
        singleOnDemandAlert.patientMedHist = singleCaseAlertService.addNewLineInField(singleOnDemandAlert.patientMedHist)

        if (singleOnDemandAlert.conComit != Constants.Commons.BLANK_STRING) {
            String[] conComitList = singleOnDemandAlert.conComit?.split('\r\n')
            singleOnDemandAlert.conComitList = []
            conComitList.each {
                if (it && it.length() > 2) {
                    singleOnDemandAlert.conComitList.add(it.substring(3, it.length()).trim())
                }
            }
            jsonField.put("${SCAJsonFieldMap.get("conComit")}", singleOnDemandAlert.conComitList)
        }
        if (singleOnDemandAlert.medErrorsPt != Constants.Commons.BLANK_STRING) {
            String[] medErrorPtList = singleOnDemandAlert.medErrorsPt?.split('\r\n')
            singleOnDemandAlert.medErrorPtList = []
            medErrorPtList.each {
                if (it && it.length() > 2) {
                    singleOnDemandAlert.medErrorPtList.add(it.substring(3, it.length()).trim())
                }
            }
            jsonField.put("${SCAJsonFieldMap.get("medErrorsPt")}", singleOnDemandAlert.medErrorPtList)
        }
        if (singleOnDemandAlert.suspProd != Constants.Commons.BLANK_STRING) {
            String[] suspProdList = singleOnDemandAlert.suspProd?.split('\r\n')
            singleOnDemandAlert.suspectProductList = []
            suspProdList.each {
                if (it && it.length() > 2) {
                    singleOnDemandAlert.suspectProductList.add(it.trim())
                }
            }
            jsonField.put("${SCAJsonFieldMap.get("suspProd")}", singleOnDemandAlert.suspectProductList)
        }
        if (singleOnDemandAlert.allPt != Constants.Commons.BLANK_STRING) {
            String[] allPtList = singleOnDemandAlert.allPt?.split('\r\n')
            singleOnDemandAlert.allPtList = []
            allPtList.each {
                if (it && it.length() > 2) {
                    singleOnDemandAlert.allPtList.add(it.substring(3, it.length()).trim())
                }
            }
            jsonField.put("${SCAJsonFieldMap.get("allPt")}", singleOnDemandAlert.allPtList)
        }
        if (singleOnDemandAlert.masterPrefTermAll != Constants.Commons.BLANK_STRING) {
            String[] allSuspPtList = singleOnDemandAlert.masterPrefTermAll?.split('\r\n')
            singleOnDemandAlert.ptList = []
            allSuspPtList.each {
                if (it && it.length() > 2) {
                    singleOnDemandAlert.ptList.add(it.substring(3, it.length()).trim())
                }
            }
            jsonField.put("${SCAJsonFieldMap.get("masterPrefTermAll")}", singleOnDemandAlert.ptList)
        }
        if (singleOnDemandAlert.primSuspProd != Constants.Commons.BLANK_STRING) {
            String[] primSuspProdRows = singleOnDemandAlert.primSuspProd?.split('\r\n')
            singleOnDemandAlert.primSuspProdList = []
            primSuspProdRows.each {
                singleOnDemandAlert.primSuspProdList.add(it.trim())
            }
            jsonField.put("${SCAJsonFieldMap.get("primSuspProd")}", singleOnDemandAlert.primSuspProdList)
        }
        if (singleOnDemandAlert.primSuspPai != Constants.Commons.BLANK_STRING) {
            String[] primSuspPaiRows = singleOnDemandAlert.primSuspPai?.split('\r\n')
            singleOnDemandAlert.primSuspPaiList = []
            primSuspPaiRows.each {
                singleOnDemandAlert.primSuspPaiList.add(it.trim())
            }
            jsonField.put("${SCAJsonFieldMap.get("primSuspPai")}", singleOnDemandAlert.primSuspPaiList)
        }
        if (singleOnDemandAlert.paiAll != Constants.Commons.BLANK_STRING) {
            String[] paiAllRows = singleOnDemandAlert.paiAll?.split('\r\n')
            singleOnDemandAlert.paiAllList = []
            paiAllRows.each {
                if (it && it.length() > 2) {
                    singleOnDemandAlert.paiAllList.add(it.substring(3, it.length()).trim())
                }
            }
            jsonField.put("${SCAJsonFieldMap.get("paiAll")}", singleOnDemandAlert.paiAllList)
        }
        if (singleOnDemandAlert.allPTsOutcome != Constants.Commons.BLANK_STRING) {
            String[] allPTsOutcomeRows = singleOnDemandAlert.allPTsOutcome?.split('\r\n')
            singleOnDemandAlert.allPTsOutcomeList = []
            allPTsOutcomeRows.each {
                if (it && it.length() > 2) {
                    singleOnDemandAlert.allPTsOutcomeList.add(it.substring(3, it.length()).trim())
                }
            }
            jsonField.put("${SCAJsonFieldMap.get("allPTsOutcome")}", singleOnDemandAlert.allPTsOutcomeList)
        }
        if (singleOnDemandAlert.batchLotNo != Constants.Commons.BLANK_STRING) {
            String[] batchLotNoList = singleOnDemandAlert.batchLotNo?.split('\r\n')
            singleOnDemandAlert.batchLotNoList = []
            batchLotNoList.each {
                if (it && it.length() > 2) {
                    singleOnDemandAlert.batchLotNoList.add(it.substring(3, it.length()).trim())
                }
            }
            jsonField.put("${SCAJsonFieldMap.get("batchLotNo")}", singleOnDemandAlert.batchLotNoList)
        }
        if (singleOnDemandAlert.crossReferenceInd != Constants.Commons.BLANK_STRING) {
            String[] crossReferenceIndRows = singleOnDemandAlert.crossReferenceInd?.split('\r\n')
            singleOnDemandAlert.crossReferenceIndList = []
            crossReferenceIndRows.each {
                if (it && it.length() > 2) {
                    singleOnDemandAlert.crossReferenceIndList.add(it.substring(3, it.length()).trim())
                }
            }
            jsonField.put("${SCAJsonFieldMap.get("crossReferenceInd")}", singleOnDemandAlert.crossReferenceIndList)
        }
        singleOnDemandAlert.jsonField = new JsonBuilder(jsonField).toPrettyString()
        singleOnDemandAlert.globalIdentity = dataObjectService.getGlobalCase(executedConfig.id, singleOnDemandAlert.caseId, singleOnDemandAlert.caseVersion)
        alertService.setBadgeValueForSCA(singleOnDemandAlert, false)
        singleOnDemandAlert
    }

    List<Map> getSingleCaseAlertList(List<SingleOnDemandAlert> list, AlertDataDTO alertDataDTO) {
        List scaList = []
        if (list) {
            List<Long> alertIdList = list.collect { it.id }
            List<Map> allAlertTagsList = pvsAlertTagService.getAllAlertSpecificTags(alertIdList, Constants.AlertConfigType.SINGLE_CASE_ALERT_DEMAND)
            List<Map> allGlobalTagsList = pvsGlobalTagService.getAllGlobalTags(list.collect { it.globalIdentityId }, Constants.AlertConfigType.SINGLE_CASE_ALERT, Constants.Commons.REVIEW)
            list.each { SingleOnDemandAlert singleOnDemandAlert ->
                Map caseData = singleOnDemandAlert.composeAlert(alertDataDTO.timeZone, alertDataDTO.isFromExport)
                if(Holders.config.enable.show.dob == false){
                    caseData.dateOfBirth="#########"
                }
                List<Map> caseSeriesTags = allAlertTagsList.findAll { it.alertId == caseData.id }
                List<Map> globalTags = allGlobalTagsList.findAll { it.globalId == caseData.globalId }
                globalTags = globalTags.unique(false) { a, b ->
                    a.tagText <=> b.tagText
                }
                List<Map> caseSeriesAndGlobalTags = caseSeriesTags + globalTags
                caseData.alertTags = caseSeriesAndGlobalTags.sort{ it.tagText?.toLowerCase()}
                scaList.add(caseData)
            }
        }
        scaList
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
        } catch(Exception ex){
            log.error(ex.printStackTrace())
        } finally {
            sql?.close()
        }
    }

    SingleOnDemandAlert saveFieldsInSingleCaseAlert(Configuration config,ExecutedConfiguration executedConfig,Map scaData,Map data){
        Integer timeToOnset = data[cacheService.getRptFieldIndexCache('dvProdEventTimeOnsetDays')]
        String csiSponsorStudyNumber = Holders.config.custom.qualitative.fields.enabled ? cacheService.getRptFieldIndexCache('vwcsiSponsorStudyNumber') :  cacheService.getRptFieldIndexCache('crepoud_text_11')
        String countryColumn = Holders.config.custom.qualitative.fields.enabled ? cacheService.getRptFieldIndexCache('csiderived_country') :  cacheService.getRptFieldIndexCache('masterCountryId')
        String parsedAttributes = new JsonBuilder(data)
        if (parsedAttributes.size() == 8000) {
            data << [extraField: ""]
            parsedAttributes = new JsonBuilder(data)
        }
        SingleOnDemandAlert singleCaseAlert = new SingleOnDemandAlert(
                [alertConfiguration        : config,
                 executedAlertConfiguration: executedConfig,
                 name                      : executedConfig.name,
                 caseNumber                : scaData.caseNumber,
                 caseVersion               : scaData.versionNumber,
                 productFamily             : scaData.productFamily,
                 productName               : scaData.productName,
                 productId                 : scaData.productId,
                 pt                        : scaData.pt,
                 detectedDate              : executedConfig.dateCreated,
                 attributes                : parsedAttributes,
                 createdBy                 : config.createdBy,
                 modifiedBy                : config.modifiedBy,
                 dateCreated               : executedConfig.dateCreated,
                 lastUpdated               : executedConfig.dateCreated,
                 followUpNumber            : scaData.followUpNumber ? (scaData.followUpNumber - 1) : scaData.followUpNumber,
                 adhocRun                  : config.adhocRun,
                 periodStartDate           : scaData.dateRangeStartEndDate?scaData.dateRangeStartEndDate.get(0):null,
                 periodEndDate             : scaData.dateRangeStartEndDate?scaData.dateRangeStartEndDate.get(1):null,
                 listedness                : scaData.listedness,
                 outcome                   : scaData.outcome,
                 lockedDate                : data[cacheService.getRptFieldIndexCache('masterDateLocked')] ?:  data[cacheService.getRptFieldIndexCache('masterCloseDate')],
                 caseInitReceiptDate       : data[cacheService.getRptFieldIndexCache('masterInitReptDate')],
                 caseReportType            : data[cacheService.getRptFieldIndexCache('masterRptTypeId')],
                 reportersHcpFlag          : data[cacheService.getRptFieldIndexCache('CsHcpFlag')] ?: Constants.Commons.BLANK_STRING,
                 death                     : data[cacheService.getRptFieldIndexCache('masterFatalFlag')] ?: Constants.Commons.BLANK_STRING,
                 rechallenge               : data[cacheService.getRptFieldIndexCache('prodDrugsPosRechallenge')] ?: Constants.Commons.BLANK_STRING,
                 gender                    : data[cacheService.getRptFieldIndexCache('patInfoGenderId')] ?: Constants.Commons.BLANK_STRING,
                 age                       : data[cacheService.getRptFieldIndexCache('patInfoAgeGroupId')] ?: Constants.Commons.BLANK_STRING,
                 country                   : data[countryColumn] ?: (data[cacheService.getRptFieldIndexCache('ciPrimSrccountryId')] ?: Constants.Commons.BLANK_STRING),
                 masterPrefTermAll         : data[cacheService.getRptFieldIndexCache('masterPrefTermSurAll')] ?: Constants.Commons.BLANK_STRING,
                 conComit                  : data[cacheService.getRptFieldIndexCache('masterConcomitProdList')] ?: Constants.Commons.BLANK_STRING,
                 suspProd                  : data[cacheService.getRptFieldIndexCache('masterSuspProdAgg')] ?: Constants.Commons.BLANK_STRING,
                 serious                   : data[cacheService.getRptFieldIndexCache('assessSeriousness')] ?: Constants.Commons.BLANK_STRING,
                 caseNarrative             : data[cacheService.getRptFieldIndexCache('narrativeNarrative')] ?: Constants.Commons.BLANK_STRING,
                 conMeds                   : data[cacheService.getRptFieldIndexCache('masterConcomitProdList')] ?: Constants.Commons.BLANK_STRING,
                 caseType                  : data[cacheService.getRptFieldIndexCache('cmadlflagEligibleLocalExpdtd')] ?: Constants.Commons.BLANK_STRING,
                 completenessScore         : singleCaseAlertService.generateCompletenessScore(data),
                 indNumber                 : data[csiSponsorStudyNumber] ?: Constants.Commons.BLANK_STRING,
                 compoundingFlag           : data[cacheService.getRptFieldIndexCache('vwcpai1FlagCompounded')] ?: Constants.Commons.BLANK_STRING,
                 patientAge                : scaData.patientAge,
                 medErrorsPt               : data[cacheService.getRptFieldIndexCache('cdrClobAeAllUdUdClob1')] ?: Constants.Commons.BLANK_STRING,
                 submitter                 : data[cacheService.getRptFieldIndexCache('csisender_organization')] ?: Constants.Commons.BLANK_STRING,
                 appTypeAndNum             : scaData.appTypeAndNum,
                 isNew                     : scaData.isNew,
                 followUpExists            : false,
                 aggExecutionId            : executedConfig.aggExecutionId,
                 aggAlertId                : executedConfig.aggAlertId,
                 aggCountType              : executedConfig.aggCountType,
                 isCaseSeries              : executedConfig.isCaseSeries,
                 caseId                    : data[cacheService.getRptFieldIndexCache('masterCaseId')] ? data[cacheService.getRptFieldIndexCache('masterCaseId')] as Long : 0L,
                 isDuplicate               : data[cacheService.getRptFieldIndexCache('flagMasterCase')],
                 malfunction               : data[cacheService.getRptFieldIndexCache('malfunctionDevices')] == 'Yes' ? 'Yes' : 'No',
                 comboFlag                 : data[cacheService.getRptFieldIndexCache('deviceComboProduct')] == 'Yes' ? 'Yes' : 'No',
                 indication                : data[cacheService.getRptFieldIndexCache('productIndCoddorReptd')] ?: Constants.Commons.BLANK_STRING,
                 eventOutcome              : data[cacheService.getRptFieldIndexCache('eventEvtOutcomeId')] ?: Constants.Commons.BLANK_STRING,
                 causeOfDeath              : replaceTagsToChar(data[cacheService.getRptFieldIndexCache('ccCoddRptdCauseDeathAll')] as String) ?: Constants.Commons.BLANK_STRING,
                 seriousUnlistedRelated    : data[cacheService.getRptFieldIndexCache('ceSerUnlRel')] ?: Constants.Commons.BLANK_STRING,
                 patientMedHist            : data[cacheService.getRptFieldIndexCache('cprmConditionAll')] ?: Constants.Commons.BLANK_STRING,
                 patientHistDrugs          : data[cacheService.getRptFieldIndexCache('ccMedHistDrugAll')] ?: Constants.Commons.BLANK_STRING,
                 batchLotNo                : data[cacheService.getRptFieldIndexCache('productLotNoAllcs')] ?: Constants.Commons.BLANK_STRING,
                 timeToOnset               : (timeToOnset || timeToOnset == 0) ? timeToOnset: Constants.Commons.BLANK_STRING,
                 caseClassification        : data[cacheService.getRptFieldIndexCache('masterCharactersticAllcs')]?: Constants.Commons.BLANK_STRING,
                 initialFu                 : singleCaseAlertService.checkIfInitialOrFu(data,false,scaData),
                 protocolNo                : Holders.config.custom.qualitative.fields.enabled? (data[cacheService.getRptFieldIndexCache('vwstudyProtocolNum')] ?: Constants.Commons.BLANK_STRING) : singleCaseAlertService.getProtocolNo(data),
                 isSusar                   : data[cacheService.getRptFieldIndexCache('masterSusar')] ?: Constants.Commons.BLANK_STRING,
                 therapyDates              : data[cacheService.getRptFieldIndexCache('productStartStopDateAllcs')] ?: Constants.Commons.BLANK_STRING,
                 doseDetails               : data[cacheService.getRptFieldIndexCache('productDoseDetailAllcs')] ?: Constants.Commons.BLANK_STRING,
                 preAnda                   : data[cacheService.getRptFieldIndexCache('PreAndastudyStudyNum')] ?: Constants.Commons.BLANK_STRING,
                 primSuspProd              : data[cacheService.getRptFieldIndexCache('masterPrimProdName')] ?: Constants.Commons.BLANK_STRING,
                 primSuspPai               : data[cacheService.getRptFieldIndexCache('dciPrimSuspectPai')] ?: Constants.Commons.BLANK_STRING,
                 paiAll                    : data[cacheService.getRptFieldIndexCache('dciPaiAll')] ?: Constants.Commons.BLANK_STRING,
                 allPt                     : data[cacheService.getRptFieldIndexCache('masterPrefTermAll')] ?: Constants.Commons.BLANK_STRING,
                 genericName               : data[cacheService.getRptFieldIndexCache('masterSuspProdList')] ?: Constants.Commons.BLANK_STRING,
                 caseCreationDate          : data[cacheService.getRptFieldIndexCache('masterCreateTime')] ?: Constants.Commons.BLANK_STRING,
                 dateOfBirth               : data[cacheService.getRptFieldIndexCache('patInfoPatDobPartial')] ?: Constants.Commons.BLANK_STRING,
                 eventOnsetDate            : data[cacheService.getRptFieldIndexCache('eventPrimaryOnsetDatePartial')] ?: Constants.Commons.BLANK_STRING,
                 pregnancy                 : data[cacheService.getRptFieldIndexCache('masterPregnancyFlag')] ?: Constants.Commons.BLANK_STRING,
                 medicallyConfirmed        : data[cacheService.getRptFieldIndexCache('masterHcpFlag')] ?: Constants.Commons.BLANK_STRING,
                 allPTsOutcome             : data[cacheService.getRptFieldIndexCache('masterPrefTermList')] ?: Constants.Commons.BLANK_STRING,
                 crossReferenceInd         : data[cacheService.getRptFieldIndexCache('CrossRefIndstudyStudyNum')] ?: Constants.Commons.BLANK_STRING,
                 coPackagedProduct         : data[cacheService.getRptFieldIndexCache('analytic.caseInformation.cmflagcopackproduct')] ?: Constants.Commons.BLANK_STRING,
                ]
        )

        return singleCaseAlert
    }
    def replaceTagsToChar(String data){
        String result = Constants.Commons.BLANK_STRING
        if(data){
            result = data.replace("&amp;apos;","'")
        }
        return result
    }

    List<Map> fetchNextXFullCaseList(Map params, Boolean isFaers = false, Boolean isVaers = false, Boolean isVigibase = false) {
        Integer totalColumns = 28
        Map filterMap = alertService.prepareFilterMap(params, totalColumns)
        Map orderColumnMap = alertService.prepareOrderColumnMap(params)
        AlertDataDTO alertDataDTO = createAlertDataDTOadhoc(filterMap, orderColumnMap, SingleOnDemandAlert, true,params)
        alertDataDTO.length = params.length
        alertDataDTO.start = params.start
        alertDataDTO.isCaseFormProjection = true
        alertDataDTO.isFaers = isFaers
        alertDataDTO.isVaers = isVaers
        alertDataDTO.isVigibase = isVigibase
        if(Objects.isNull(alertDataDTO.orderColumnMap)){
            alertDataDTO.orderColumnMap = [:]
        }
        Closure advancedFilterClosure
        List<Map> fullCaseList = []
        advancedFilterClosure = alertService.generateAdvancedFilterClosure(alertDataDTO, advancedFilterClosure)
        fullCaseList = alertService.generateAlertListForOnDemandRuns(advancedFilterClosure, alertDataDTO)
        fullCaseList
    }


    AlertDataDTO createAlertDataDTOadhoc(Map filterMap, Map orderColumnMap,
                                         def domain, Boolean isFullCaseList = false, def params) {
        List<String> allowedProductsToUser = []
        if (getAlertService().isProductSecurity()) {
            allowedProductsToUser = getAlertService().fetchAllowedProductsForConfiguration()
        }
        params.isFaers = false
        ExecutedConfiguration executedConfig = ExecutedConfiguration.get(params.id)
        String selectedDataSource = executedConfig?.selectedDatasource ?: Constants.DataSource.PVA
        if (selectedDataSource == Constants.DataSource.FAERS) {
            params.isFaers = true
        }

        String timeZone = getUserService().getCurrentUserPreference()?.timeZone
        AlertDataDTO alertDataDTO = new AlertDataDTO()
        alertDataDTO.params = params
        alertDataDTO.allowedProductsToUser = allowedProductsToUser
        alertDataDTO.domainName = domain
        alertDataDTO.executedConfiguration = executedConfig
        alertDataDTO.execConfigId = executedConfig?.id
        alertDataDTO.configId = executedConfig?.id
        alertDataDTO.filterMap = filterMap
        alertDataDTO.timeZone = timeZone
        alertDataDTO.orderColumnMap = orderColumnMap
        alertDataDTO.userId = getUserService().getUser().id
        alertDataDTO.isFullCaseList = isFullCaseList
        alertDataDTO
    }
}
