package com.rxlogix


import com.rxlogix.Constants.AlertConfigType
import com.rxlogix.audit.AuditTrail
import com.rxlogix.config.*
import com.rxlogix.dto.ActivityDTO
import com.rxlogix.dto.AuditTrailChildDTO
import com.rxlogix.dto.AuditTrailDTO
import com.rxlogix.dto.NewCountScoreDTO
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.dto.RuleDataDTO
import com.rxlogix.dto.ScaLastReviewDurationDTO
import com.rxlogix.dto.StratificationScoreDTO
import com.rxlogix.dto.LastReviewDurationDTO
import com.rxlogix.signal.*
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.SignalQueryHelper
import com.rxlogix.util.SignalUtil
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.time.TimeCategory
import groovyx.net.http.Method
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang3.time.DateFormatUtils
import org.apache.commons.lang3.time.DateUtils
import org.hibernate.SQLQuery
import org.hibernate.Session
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import org.springframework.transaction.annotation.Propagation
import com.rxlogix.json.JsonOutput
import groovy.sql.Sql

import java.util.stream.Collectors

import static com.rxlogix.util.MiscUtil.calcDueDate

@Transactional
class BusinessConfigurationService implements AlertUtil {

    def CRUDService
    def emailService
    def queryService
    def actionTemplateService
    def grailsApplication
    def productEventHistoryService
    def evdasHistoryService
    def activityService
    def userService
    def caseHistoryService
    def alertCommentService
    def validatedSignalService
    def medicalConceptsService
    def productBasedSecurityService
    def aggregateCaseAlertService
    def evdasAlertService
    def cacheService
    def dataObjectService
    def singleCaseAlertService
    def messageSource
    def alertTagService
    def pvsGlobalTagService
    def reportIntegrationService
    def dataSource
    def sessionFactory
    def emergingIssueService
    def alertFieldService


    Map generateRenderModelForRules(RuleInformation ruleInformation, BusinessConfiguration businessConfiguration) {
        List actionList = ActionTemplate.list().collect { [id: it.id, name: it.name] }
        List dispositionList = Disposition.list().collect {
            [id: it.id, name: it.displayName, isValidationConfirmed: it.validatedConfirmed]
        }
        List justificationList = Justification.list().collect { [id: it.id, name: it.name, text: it.justification] }
        List medicalConceptList = MedicalConcepts.list().collect { [name: it.name] }
        List medicalConceptValues = []
        JsonSlurper jsonSlurper = new JsonSlurper()
        if (ruleInformation.medicalConcepts) {
            Map medicalConceptMap = jsonSlurper.parseText(ruleInformation.medicalConcepts)
            medicalConceptValues = medicalConceptMap.medicalConcepts as List
        }
        def datasourceMap = getDataSourceMap()
        Justification selectedJustification = Justification.findByJustification(ruleInformation.justificationText)
        List<ValidatedSignal> signalList = []
        List<Map> signalNameIdList = []
        if (ruleInformation?.signal) {
            signalList = ValidatedSignal.getAll(ruleInformation?.signal as Long)
            signalNameIdList = signalList.collect { [id: it.id, name: it.name] }
        }

        [businessConfiguration: businessConfiguration, actionList: actionList,
         justificationJSON    : JsonOutput.toJson(justificationList), dispositionList: dispositionList,
         datasourceMap        : datasourceMap, justificationList: justificationList, ruleInformation: ruleInformation,
         medicalConceptList: medicalConceptList, medicalConceptValues: medicalConceptValues, signalList: signalNameIdList,
         selectedJustification: selectedJustification]
    }

    def populateRuleInformation(def params, RuleInformation ruleInformation) {
        Boolean isFirstTimeRule = params.isFirstTimeRule as Boolean
        Boolean isBreakAfterRule = params.isBreakAfterRule as Boolean
        def toIntOrNull = { it?.isInteger() ? it.toInteger() : null }
        ruleInformation.customSqlQuery = params.customSQLQuery?.trim()
        ruleInformation.ruleJSON = params.JSONQuery
        ruleInformation.ruleName = params.ruleName
        ruleInformation.justificationText = params.justificationText
        ruleInformation.format = params.formatInfo
        ruleInformation.isFirstTimeRule = isFirstTimeRule ?: false
        ruleInformation.isBreakAfterRule = isBreakAfterRule ?: false
        ruleInformation.action = params.customAction ? ActionTemplate.read(params.customAction as Long) : null
        ruleInformation.disposition = params.disposition ? Disposition.read(params.disposition as Long) : null
        ruleInformation.signal = params.signal ? params.signal : null
        ruleInformation.medicalConcepts = params.medicalConcepts ? ([medicalConcepts: params.medicalConcepts] as JSON) : null
        if (params.allTags && params.allTags != "[]") {
            List tagsList = JSON.parse(params.allTags)
            tagsList.each { value ->
                Long id = null
                Integer priority = value.priority as Integer
                value.priority = priority < 9999 ? priority + 9998 : priority  //business configuration tags should be displayed at the end in the Pop-up modal // removed un-neccessary priority increment
                def url = Holders.config.commonComponent.url.codeValue
                def query = ["codeValue": value.tagText, "parentId": null, "modifiedBy": userService.getUser().username, "lastUpdated": new Date().format("yyyy-MM-dd\'T\'HH:mm:ss.SSS"), "createdBy": userService.getUser().username, "dateCreated": new Date().format("yyyy-MM-dd\'T\'HH:mm:ss.SSS")]
                def response = reportIntegrationService.postDataWithoutPath(url, query, Method.POST)
                id = response?.result?.data?.id

                value.subTags.each { subTag ->
                    query = ["codeValue": subTag, "parentId": id, "modifiedBy": userService.getUser().username, "lastUpdated": new Date().format("yyyy-MM-dd\'T\'HH:mm:ss.SSS"), "createdBy": userService.getUser().username, "dateCreated": new Date().format("yyyy-MM-dd\'T\'HH:mm:ss.SSS")]
                    response = reportIntegrationService.postDataWithoutPath(url, query, Method.POST)
                }
            }
            String tagJson = JsonOutput.toJson(tagsList)
            ruleInformation.tags = ([tags: tagJson] as JSON)
        }
        else {
            ruleInformation.tags = null
        }
        ruleInformation
    }

    List prepareTags(def params) {
        List tags = []
        if (params instanceof String) {
            tags << params
        } else {
            tags = params
        }
        tags
    }

    List<ProductEventHistory> getProductEventHistoryList(AggregateCaseAlert aggregateCaseAlert) {
        dataObjectService.getPEHistoryListByConfigId(aggregateCaseAlert.productName, aggregateCaseAlert.pt, aggregateCaseAlert.alertConfigurationId)
    }

    int calculateLastReviewDuration(Date otherDate) {
        Date today = new Date()
        int days = 0
        use(TimeCategory) {
            def duration = today - otherDate
            log.info("days: ${duration.days}")
            days = duration.days
        }
        days
    }

    int calculateLastReviewDurationForSingleCaseAlert(SingleCaseAlert singleCaseAlert) {
        int lastReviewDuration
        Date currentReviewPeriodEndDate
        ScaLastReviewDurationDTO lastReviewDurationDTO

        if (dataObjectService.getLastReviewDurationMap(singleCaseAlert.executedAlertConfigurationId)) {
            lastReviewDurationDTO =
                    dataObjectService.getQualLastReviewDurationDTO(singleCaseAlert.caseNumber, singleCaseAlert.executedAlertConfigurationId)
            currentReviewPeriodEndDate = dataObjectService.getCurrentEndDateMap(singleCaseAlert.executedAlertConfigurationId)
        }

        lastReviewDuration = lastReviewDurationDTO ? DateUtils.truncate(currentReviewPeriodEndDate, Calendar.DAY_OF_MONTH) - DateUtils.truncate( lastReviewDurationDTO.lastEndDate, Calendar.DAY_OF_MONTH) : Integer.MAX_VALUE

        lastReviewDuration
    }

    int calculateLastReviewDurationForAggregateAlert(AggregateCaseAlert aggregateCaseAlert) {
        int lastReviewDuration
        Date currentReviewPeriodEndDate
        LastReviewDurationDTO lastReviewDurationDTO

        if (dataObjectService.getLastReviewDurationMap(aggregateCaseAlert.executedAlertConfigurationId)) {
            lastReviewDurationDTO =
                    dataObjectService.getLastReviewDurationDTO(aggregateCaseAlert.productName, aggregateCaseAlert.pt, aggregateCaseAlert.executedAlertConfigurationId)
            currentReviewPeriodEndDate = dataObjectService.getCurrentEndDateMap(aggregateCaseAlert.executedAlertConfigurationId)
        }

        lastReviewDuration = lastReviewDurationDTO ? DateUtils.truncate(currentReviewPeriodEndDate, Calendar.DAY_OF_MONTH) - DateUtils.truncate(lastReviewDurationDTO.lastEndDate, Calendar.DAY_OF_MONTH) : Integer.MAX_VALUE
        lastReviewDuration
    }

    int calculateLastReviewDurationForEvdasAlert(EvdasAlert evdasAlert) {
        int lastReviewDuration
        Date currentReviewPeriodEndDate
        LastReviewDurationDTO lastReviewDurationDTO

        if (dataObjectService.getLastReviewDurationMap(evdasAlert.executedAlertConfigurationId)) {
            lastReviewDurationDTO =
                    dataObjectService.getLastReviewDurationDTO(evdasAlert.productName, evdasAlert.pt, evdasAlert.executedAlertConfigurationId)
            currentReviewPeriodEndDate = dataObjectService.getCurrentEndDateMap(evdasAlert.executedAlertConfigurationId)
        }

        lastReviewDuration = lastReviewDurationDTO ? DateUtils.truncate(currentReviewPeriodEndDate, Calendar.DAY_OF_MONTH) - DateUtils.truncate(lastReviewDurationDTO.lastEndDate, Calendar.DAY_OF_MONTH): Integer.MAX_VALUE

        lastReviewDuration
    }

    def executeRulesForSingleCaseAlert(SingleCaseAlert singleCaseAlert,
                                       List<Map> queryCaseMetadataMaps, String productDictionarySelection, Long defaultDispositionId, String productAlertSelection,StringBuffer allPECLogs) {
        StringBuilder logStringBuilder = new StringBuilder()
        logStringBuilder.append("\n********** Rule Processing Started for Single Case Alert : ${singleCaseAlert.name} **********")

        RuleDataDTO ruleDataDTO = new RuleDataDTO()

        String familySelectionLevel = Constants.ProductSelectionTypeValue.FAMILY

        ruleDataDTO.with {
            id = singleCaseAlert?.id
            execConfigId = singleCaseAlert.executedAlertConfigurationId
            disposition = singleCaseAlert.disposition
            products = (singleCaseAlert?.productDictionarySelection == familySelectionLevel) ? singleCaseAlert?.productFamily : singleCaseAlert?.productName
            productSelection = productAlertSelection
            dataSource = Constants.DataSource.PVA
            alertType = AlertConfigType.SINGLE_CASE_ALERT
            isPEDetectedFirstTime = (singleCaseAlert?.dispositionId == defaultDispositionId) as Boolean
            lastReviewDuration = calculateLastReviewDurationForSingleCaseAlert(singleCaseAlert)
            isLastReviewDuration = lastReviewDuration == Integer.MAX_VALUE ? true : false
            queryCaseMaps = queryCaseMetadataMaps
            caseId = singleCaseAlert?.caseId
            configProductSelection = productDictionarySelection
            dataSource = Constants.DataSource.PVA
            logString = logStringBuilder
            productId = singleCaseAlert?.productId
        }
        executeRules(ruleDataDTO, singleCaseAlert)
        allPECLogs.append(logStringBuilder)
    }

    List generateImpEventList() {
        List importantEventList = []
        importantEventList = EmergingIssue.getAll()?.collect { ei ->
            [
                    eventName                                            : emergingIssueService.getEventNameFieldJson(ei.eventName),
                    eventGroup                                           : getGroupNameFieldFromJson(ei.eventGroupSelection)?.tokenize(","),
                    DME                                                  : ei.dme,
                    STOP_LIST                                            : ei.emergingIssue,
                    IME                                                  : ei.ime,
                    SPECIAL_MONITORING                                   : ei.specialMonitoring,
                    dataSourceDict                                       : ei.dataSourceDict,
                    products                                             : emergingIssueService.fetchProductNameForMatching(ei.productSelection) + emergingIssueService.fetchPGNameForMatching(ei.productGroupSelection),
                    isMultiIngredient                                    : ei.isMultiIngredient
            ]
        }
        importantEventList
    }

    Map fetchImpEventMap(def alert, String eventName, String productName, productId, List impEventList, Boolean isProduct) {
        Map result = [isIME: null, isDME: null, isStopList: null, isSpecialMonitoring: null, isDMEProd: null, isIMEProd: null, isStopListProd: null, isSpecialMonitoringProd: null]
        try {
            List eventList = impEventList.findAll { importantEvent -> (importantEvent.eventName && importantEvent.eventName*.toLowerCase().contains(eventName.toLowerCase())) || (importantEvent.eventGroup && importantEvent.eventGroup*.toLowerCase().contains(eventName.toLowerCase())) }
            if (isProduct) {
                List importantEventMapProd = eventList.findAll { importantEvent -> importantEvent.products.find { it.name.toLowerCase().contains(productName.toLowerCase()) && it.id == productId as String } }
                if (importantEventMapProd) {
                    result.isDMEProd = importantEventMapProd.any { it[Constants.BusinessConfigAttributes.DME_AGG_ALERT] == true }
                    result.isIMEProd = importantEventMapProd.any { it[Constants.BusinessConfigAttributes.IME_AGG_ALERT] == true }
                    result.isStopListProd = importantEventMapProd.any { it[Constants.BusinessConfigAttributes.STOP_LIST] == true }
                    result.isSpecialMonitoringProd = importantEventMapProd.any { it[Constants.BusinessConfigAttributes.SPECIAL_MONITORING] == true }
                }
            }
            List importantEventMap = eventList.findAll { importantEvent -> importantEvent.products.size() == 0 }
            if (importantEventMap) {
                result.isIME = importantEventMap.any { it[Constants.BusinessConfigAttributes.IME_AGG_ALERT] == true }
                result.isDME = importantEventMap.any { it[Constants.BusinessConfigAttributes.DME_AGG_ALERT] == true }
                result.isStopList = importantEventMap.any { it[Constants.BusinessConfigAttributes.STOP_LIST] == true }
                result.isSpecialMonitoring = importantEventMap.any { it[Constants.BusinessConfigAttributes.SPECIAL_MONITORING] == true }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e)
        }
        result

    }

    Map fetchImpEventMapEvdas(def alert, String eventName, String productName,  List impEventList, Boolean isProduct) {
        Map result = [isIME: null, isDME: null, isStopList: null, isSpecialMonitoring: null, isDMEProd: null, isIMEProd: null, isStopListProd: null, isSpecialMonitoringProd: null]
        try {
            List eventList = impEventList.findAll { importantEvent -> (importantEvent.eventName && importantEvent.eventName*.toLowerCase().contains(eventName.toLowerCase())) || (importantEvent.eventGroup && importantEvent.eventGroup*.toLowerCase().contains(eventName.toLowerCase())) }
            if (isProduct) {
                Map importantEventMapProd = eventList.find { importantEvent -> importantEvent.products.find { it.name.toLowerCase().contains(productName.toLowerCase())} && (importantEvent.dataSourceDict?.contains(Constants.DataSource.EUDRA) || importantEvent.dataSourceDict?.contains(Constants.DataSource.DATASOURCE_EUDRA))}
                if (importantEventMapProd) {
                    result.isDMEProd = importantEventMapProd[Constants.BusinessConfigAttributes.DME_AGG_ALERT]
                    result.isIMEProd = importantEventMapProd[Constants.BusinessConfigAttributes.IME_AGG_ALERT]
                    result.isStopListProd = importantEventMapProd[Constants.BusinessConfigAttributes.STOP_LIST]
                    result.isSpecialMonitoringProd = importantEventMapProd[Constants.BusinessConfigAttributes.SPECIAL_MONITORING]
                }
            }
            Map importantEventMap = eventList.find { importantEvent -> importantEvent.products.size() == 0 }
            if (importantEventMap) {
                result.isIME = importantEventMap[Constants.BusinessConfigAttributes.IME_AGG_ALERT]
                result.isDME = importantEventMap[Constants.BusinessConfigAttributes.DME_AGG_ALERT]
                result.isStopList = importantEventMap[Constants.BusinessConfigAttributes.STOP_LIST]
                result.isSpecialMonitoring = importantEventMap[Constants.BusinessConfigAttributes.SPECIAL_MONITORING]
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e)
        }
        result

    }

    void executeRulesForAggregateAlert(AggregateCaseAlert aggregateCaseAlert, String productDictionarySelection, String execProductSelection,
                                       String defaultDispositionValue, Map<String, AggregateCaseAlert> previousAlertMap,
                                       Map previousAlertTags, Map allPreviousTags, List impEventList, Disposition initialDisp,String db=null,StringBuffer allPECLogs) {


        StringBuilder logStringBuilder = new StringBuilder()
        logStringBuilder.append("\n********** Rule Processing Started for Aggregate Case Alert : ${aggregateCaseAlert?.name}**********")
        RuleDataDTO ruleDataDTO = new RuleDataDTO()
        List<ProductEventHistory> productEventHistoryList = getProductEventHistoryList(aggregateCaseAlert)
        ruleDataDTO.prevAggData = getPreviousPeriodDataOfAggAlert(aggregateCaseAlert, previousAlertMap)
        if (ruleDataDTO.prevAggData) {
            ruleDataDTO.isFirstExecution = false
        }
        int prevQuarter = DateUtil.quarter - 1
        Date startDateOfQuarter = DateUtil.getFirstDateOfQuarter(prevQuarter)
        Date endDateOfQuarter = DateUtil.getEndDateOfQuarter(prevQuarter)
        ruleDataDTO.previousAggAlert = previousAlertMap.get(aggregateCaseAlert.productName +"-"+aggregateCaseAlert.pt)
        if (ruleDataDTO.prevAggData) {
            ruleDataDTO.previousTags = previousAlertTags.get(aggregateCaseAlert.productId + "-" + aggregateCaseAlert.ptCode + "-" + aggregateCaseAlert.smqCode) ?: []
            ruleDataDTO.allPrevTags = allPreviousTags.get(aggregateCaseAlert.productId + "-" + aggregateCaseAlert.ptCode + "-" + aggregateCaseAlert.smqCode) ?: []
        }
        String pec = aggregateCaseAlert.productName + "-" + aggregateCaseAlert.pt
        Map impEventMap = fetchImpEventMap(aggregateCaseAlert, aggregateCaseAlert.pt, aggregateCaseAlert.productName, aggregateCaseAlert.productId , impEventList, true)
        ruleDataDTO.with {
            execConfigId = aggregateCaseAlert.executedAlertConfigurationId
            disposition = initialDisp
            eb95 = aggregateCaseAlert.eb95
            eb05 = aggregateCaseAlert.eb05
            ebgm = aggregateCaseAlert.ebgm
            newCount = aggregateCaseAlert.newCount
            cumCount = aggregateCaseAlert.cummCount
            newStudyCount = aggregateCaseAlert.newStudyCount
            cumStudyCount = aggregateCaseAlert.cumStudyCount
            newSponCount = aggregateCaseAlert.newSponCount
            cumSponCount = aggregateCaseAlert.cumSponCount
            newSeriousCount = aggregateCaseAlert.newSeriousCount
            cumSeriousCount = aggregateCaseAlert.cumSeriousCount
            newFatalCount = aggregateCaseAlert.newFatalCount
            cumFatalCount = aggregateCaseAlert.cumFatalCount
            newGeriatricCount = aggregateCaseAlert.newGeriatricCount
            cumGeriatricCount = aggregateCaseAlert.cumGeriatricCount
            newNonSerious = aggregateCaseAlert.newNonSerious
            cumNonSerious = aggregateCaseAlert.cumNonSerious
            newInteractingCount = aggregateCaseAlert.newInteractingCount
            cumInteractingCount = aggregateCaseAlert.cummInteractingCount
            products = aggregateCaseAlert.productName
            productSelection = execProductSelection
            prr = aggregateCaseAlert.prrValue as Double
            prrLCI = aggregateCaseAlert.prrLCI as Double
            prrUCI = aggregateCaseAlert.prrUCI as Double
            ror = aggregateCaseAlert.rorValue as Double
            rorLCI = aggregateCaseAlert.rorLCI as Double
            rorUCI = aggregateCaseAlert.rorUCI as Double
            ebgm = aggregateCaseAlert.ebgm as Double
            eb05 = aggregateCaseAlert.eb05 as Double
            eb95 = aggregateCaseAlert.eb95 as Double
            dataSource = Constants.DataSource.PVA
            trendType = aggregateCaseAlert.trendType
            prevEB95 = getPreviousPeriodEB95Value(productEventHistoryList, startDateOfQuarter, endDateOfQuarter)
            alertType = AlertConfigType.AGGREGATE_CASE_ALERT
            isPEDetectedFirstTime = (cacheService.getDispositionByValue(aggregateCaseAlert.dispositionId).value == defaultDispositionValue) as Boolean
            lastReviewDuration = calculateLastReviewDurationForAggregateAlert(aggregateCaseAlert)
            isLastReviewDuration = lastReviewDuration == Integer.MAX_VALUE
            configProductSelection = productDictionarySelection
            listedness = aggregateCaseAlert.listed
            positiveRechallenge = aggregateCaseAlert.positiveRechallenge
            isDME = impEventMap.isDME
            isIME = impEventMap.isIME
            isStopList = impEventMap.isStopList
            isSpecialMonitoring = impEventMap.isSpecialMonitoring
            isDMEProd = impEventMap.isDMEProd
            isIMEProd = impEventMap.isIMEProd
            isStopListProd = impEventMap.isStopListProd
            isSpecialMonitoringProd = impEventMap.isSpecialMonitoringProd
            chiSquare = aggregateCaseAlert.chiSquare as Double
            isNewEvent = aggregateCaseAlert.isNew
            logString = logStringBuilder
            productId = aggregateCaseAlert.productId
            newPediatricCount = aggregateCaseAlert.newPediatricCount
            cumPediatricCount = aggregateCaseAlert.cummPediatricCount
            trendFlag = aggregateCaseAlert.trendFlag
            newProdCount = aggregateCaseAlert.newProdCount
            cumProdCount = aggregateCaseAlert.cumProdCount
            freqPeriod = aggregateCaseAlert.freqPeriod
            cumFreqPeriod = aggregateCaseAlert.cumFreqPeriod
            pecImpNumHigh = aggregateCaseAlert.pecImpNumHigh
            eValue = aggregateCaseAlert.eValue
            rrValue = aggregateCaseAlert.rrValue
        }
        ruleDataDTO = newCountScore(aggregateCaseAlert,ruleDataDTO)
        ruleDataDTO = populateStratificationScore(aggregateCaseAlert, ruleDataDTO)
        executeRules(ruleDataDTO, aggregateCaseAlert, initialDisp,db)
        allPECLogs.append(logStringBuilder)
    }

    RuleDataDTO newCountScore(AggregateCaseAlert aggregateCaseAlert,RuleDataDTO ruleDataDTO) {
        if(aggregateCaseAlert.newCountsJson) {
            NewCountScoreDTO newCountScoreDTO = new NewCountScoreDTO()
            ruleDataDTO.newCountScoreDTO = newCountScoreDTO.addNewCountData(aggregateCaseAlert.newCountsJson)
        }
        ruleDataDTO
    }

    RuleDataDTO populateStratificationScore(AggregateCaseAlert aggregateCaseAlert, RuleDataDTO ruleDataDTO) {
        StratificationScoreDTO stratificationScoreDTO = new StratificationScoreDTO()
        ruleDataDTO.stratificationScoreDTO = stratificationScoreDTO.addStratificationScore(aggregateCaseAlert.eb05Gender?.toString(), 'eb05')
        ruleDataDTO.stratificationScoreDTO = stratificationScoreDTO.addStratificationScore(aggregateCaseAlert.eb95Gender?.toString(), 'eb95')
        ruleDataDTO.stratificationScoreDTO = stratificationScoreDTO.addStratificationScore(aggregateCaseAlert.eb05Age?.toString(), 'eb05')
        ruleDataDTO.stratificationScoreDTO = stratificationScoreDTO.addStratificationScore(aggregateCaseAlert.eb95Age?.toString(), 'eb95')
        ruleDataDTO.stratificationScoreDTO = stratificationScoreDTO.addStratificationScore(aggregateCaseAlert.ebgmAge?.toString(), 'ebgm')
        ruleDataDTO.stratificationScoreDTO = stratificationScoreDTO.addStratificationScore(aggregateCaseAlert.ebgmGender?.toString(), 'ebgm')

        ruleDataDTO.stratificationScoreDTO = stratificationScoreDTO.addAllSubGroupScore(aggregateCaseAlert.ebgmSubGroup?.toString(), 'EBGM')
        ruleDataDTO.stratificationScoreDTO = stratificationScoreDTO.addAllSubGroupScore(aggregateCaseAlert.eb95SubGroup?.toString(), 'EB95')
        ruleDataDTO.stratificationScoreDTO = stratificationScoreDTO.addAllSubGroupScore(aggregateCaseAlert.eb05SubGroup?.toString(), 'EB05')
        ruleDataDTO.stratificationScoreDTO = stratificationScoreDTO.addAllSubGroupScore(aggregateCaseAlert.rorSubGroup.toString(), 'ROR')
        ruleDataDTO.stratificationScoreDTO = stratificationScoreDTO.addAllSubGroupScore(aggregateCaseAlert.rorLciSubGroup?.toString(), 'ROR_LCI')
        ruleDataDTO.stratificationScoreDTO = stratificationScoreDTO.addAllSubGroupScore(aggregateCaseAlert.rorUciSubGroup?.toString(), 'ROR_UCI')
        ruleDataDTO.stratificationScoreDTO = stratificationScoreDTO.addAllSubGroupScore(aggregateCaseAlert.prrSubGroup.toString(), 'PRR')
        ruleDataDTO.stratificationScoreDTO = stratificationScoreDTO.addAllSubGroupScore(aggregateCaseAlert.prrLciSubGroup?.toString(), 'PRR_LCI')
        ruleDataDTO.stratificationScoreDTO = stratificationScoreDTO.addAllSubGroupScore(aggregateCaseAlert.prrUciSubGroup?.toString(), 'PRR_UCI')
        ruleDataDTO.stratificationScoreDTO = stratificationScoreDTO.addAllSubGroupScore(aggregateCaseAlert.chiSquareSubGroup?.toString(), 'CHI_SQUARE')
        ruleDataDTO.stratificationScoreDTO  = stratificationScoreDTO.addAllSubGroupScore(aggregateCaseAlert.rorRelSubGroup.toString(), 'ROR_R')
        ruleDataDTO.stratificationScoreDTO  = stratificationScoreDTO.addAllSubGroupScore(aggregateCaseAlert.rorLciRelSubGroup?.toString(), 'ROR_LCI_R')
        ruleDataDTO.stratificationScoreDTO  = stratificationScoreDTO.addAllSubGroupScore(aggregateCaseAlert.rorUciRelSubGroup?.toString(), 'ROR_UCI_R')
        ruleDataDTO
    }

    Double getPreviousPeriodEB95Value(List<ProductEventHistory> productEventHistoryList,
                                      Date startDateOfQuarter,
                                      Date endDateOfQuarter) {

        List<ProductEventHistory> filteredProductEventHistoryList = productEventHistoryList.findAll {
            it.dateCreated > startDateOfQuarter && it.dateCreated < endDateOfQuarter
        }
        ProductEventHistory productEventHistory = null
        if (filteredProductEventHistoryList) {
            productEventHistory = filteredProductEventHistoryList?.sort { it.dateCreated }?.last()
        }

        productEventHistory ? productEventHistory.eb95 : 0.0
    }

    EvdasAlert executeRulesForEvdasAlert(EvdasAlert evdasAlert, String productDictionarySelection, String alertProductSelection,
                                         String defaultDispositionValue, Map<String, EvdasAlert> previousAlertMap, List impEventList, StringBuffer allPECLogs) {

        StringBuilder logStringBuilder = new StringBuilder()
        logStringBuilder.append("\n********** Rule Processing Started for Evdas Alert : ${evdasAlert.name} ${evdasAlert.id}**********")
        RuleDataDTO ruleDataDTO = new RuleDataDTO()
        ruleDataDTO.prevEvdasData = getPreviousPeriodDataOfEvdasAlert(evdasAlert, previousAlertMap)
        if(ruleDataDTO.prevEvdasData){
            ruleDataDTO.isFirstExecution = false
        }
        def previousEvdasData = previousAlertMap.get(evdasAlert.substance +"-"+ evdasAlert.pt)
        ruleDataDTO.previousEvdasAlert = previousEvdasData
        Map impEventMap = fetchImpEventMapEvdas(evdasAlert, evdasAlert.pt, evdasAlert.substance, impEventList, true)
        ruleDataDTO.with {
            execConfigId = evdasAlert.executedAlertConfigurationId
            disposition = evdasAlert.disposition
            id = evdasAlert.id
            ror = evdasAlert.rorValue as Double
            newEvEvdas = evdasAlert.newEv
            totalEvEvdas = evdasAlert.totalEv
            newFatalEvdas = evdasAlert.newFatal
            totalFatalEvdas = evdasAlert.totalFatal
            newSeriousEvdas = evdasAlert.newSerious
            totalSeriousEvdas = evdasAlert.totalSerious
            newLitEvdas = evdasAlert.newLit as Integer
            totalLitEvdas = evdasAlert.totalLit as Integer
            newPaedEvdas = evdasAlert.newPaed as Integer
            totalPaedEvdas = evdasAlert.totPaed as Integer
            newGeriatEvdas = evdasAlert.newGeria as Integer
            totalGeriatEvdas = evdasAlert.totGeria as Integer
            newEEAEvdas = evdasAlert.newEea as Integer
            totEEAEvdas = evdasAlert.totEea as Integer
            newHCPEvdas = evdasAlert.newHcp as Integer
            totHCPEvdas = evdasAlert.totHcp as Integer
            newObsEvdas = evdasAlert.newObs as Integer
            totObsEvdas = evdasAlert.totObs as Integer
            newMedErrEvdas = evdasAlert.newMedErr as Integer
            totMedErrEvdas = evdasAlert.totMedErr as Integer
            newPlusRCEvdas = evdasAlert.newRc as Integer
            totPlusRCEvdas = evdasAlert.totRc as Integer
            newSponEvdas = evdasAlert.newSpont as Integer
            totSponEvdas = evdasAlert.totSpont as Integer

            rorEuropeEvdas = evdasAlert.europeRor as Double
            rorNAmericaEvdas = evdasAlert.northAmericaRor as Double
            rorJapanEvdas = evdasAlert.japanRor as Double
            rorAsiaEvdas = evdasAlert.asiaRor as Double
            rorRestEvdas = evdasAlert.restRor as Double
            rorAllEvdas = evdasAlert.allRor as Double
            dataSource = Constants.DataSource.EUDRA
            alertType = AlertConfigType.EVDAS_ALERT
            isPEDetectedFirstTime = (evdasAlert.disposition?.value == defaultDispositionValue)
            lastReviewDuration = calculateLastReviewDurationForEvdasAlert(evdasAlert)
            isLastReviewDuration = lastReviewDuration == Integer.MAX_VALUE ? true : false
            products = evdasAlert.substance
            configProductSelection = "1"
            sdr = evdasAlert.sdr
            changes = evdasAlert.changes
            productSelection = alertProductSelection
            evdasListedness = evdasAlert.listedness?"Yes":(evdasAlert.listedness==false)?'No':'-'
            totSpontEurope = evdasAlert.totSpontEurope as Integer
            totSpontNAmerica = evdasAlert.totSpontNAmerica as Integer
            totSpontJapan = evdasAlert.totSpontJapan as Integer
            totSpontAsia = evdasAlert.totSpontAsia as Integer
            totSpontRest = evdasAlert.totSpontRest as Integer
            dmeIme = evdasAlert.dmeIme
            reltRorPadVsOhtr = evdasAlert.ratioRorPaedVsOthers as Double
            sdrPaed = evdasAlert.sdrPaed
            reltRorGrtVsOthr = evdasAlert.ratioRorGeriatrVsOthers as Double
            sdrGeratr = evdasAlert.sdrGeratr
            isNewEvent = evdasAlert.isNew
            logString = logStringBuilder
            isDME = impEventMap.isDME
            isIME = impEventMap.isIME
            isStopList = impEventMap.isStopList
            isSpecialMonitoring = impEventMap.isSpecialMonitoring
            isDMEProd = impEventMap.isDMEProd
            isIMEProd = impEventMap.isIMEProd
            isStopListProd = impEventMap.isStopListProd
            isSpecialMonitoringProd = impEventMap.isSpecialMonitoringProd
            productId = evdasAlert.substanceId as BigInteger
        }
        executeRules(ruleDataDTO, evdasAlert)
        allPECLogs.append(logStringBuilder)
        evdasAlert
    }

    def fetchBusinessConfigurationFromProducts(String alertProductSelectionType, String productSelection, ExecutedConfiguration executedConfiguration = null) {
        def selectedBusinessConfigurations = []
        List productIds = SignalUtil.getProductIdsFromProductSelection(productSelection)
        List<BusinessConfiguration> businessConfigurationList = cacheService.getBusinessConfigByProdDictSelection(alertProductSelectionType)
        businessConfigurationList?.each { BusinessConfiguration businessConfiguration ->
            List bcProductIdList = businessConfiguration?.productIdList
            if (!bcProductIdList?.disjoint(productIds) && (businessConfiguration?.isMultiIngredient ?: false) == (executedConfiguration?.isMultiIngredient ?: false)) {
                selectedBusinessConfigurations.add(businessConfiguration)
            }
        }
        selectedBusinessConfigurations
    }

    def fetchBusinessConfigurations(String alertProductSelectionType, String dataSource,
                                    String productSelection, Long execConfigId, StringBuilder logString = null) {

        //Fetch the global business configurations based on datasource.
        List<BusinessConfiguration> businessConfigurationList = dataObjectService.getEnabledBusinessConfigList(dataSource)

        //If global rules are null then it will check for product rules.
        if (!businessConfigurationList) {
            if (logString) {
                logString.append("\nfetchBusinessConfigurationFromProducts called with param : ${alertProductSelectionType} | ${productSelection}")
            } else {
                log.info("fetchBusinessConfigurationFromProducts called with param : ${alertProductSelectionType} | ${productSelection}")
            }
            if(productSelection){
                ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(execConfigId)
                businessConfigurationList = fetchBusinessConfigurationFromProducts(alertProductSelectionType, productSelection,executedConfiguration)
            } else {
                businessConfigurationList = dataObjectService.getEnabledBusinessConfigProductGrpList(execConfigId).findAll {
                    it.dataSource == dataSource
                }
            }
        } else {
            if (logString) {
                logString.append("\nGlobal Rule found for datasource : ${dataSource} : ${businessConfigurationList}")
                logString.append("\nSkipping product rules")
            } else {
                log.info("Global Rule found for datasource : ${dataSource} : ${businessConfigurationList}")
                log.info("Skipping product rules")
            }
        }
        businessConfigurationList
    }

    Map getQueryFromRules(Long execConfigId, Configuration config) {
        List<BusinessConfiguration> businessConfigurationList = fetchBusinessConfigurations(config.productDictionarySelection, config.selectedDatasource, config.productSelection,execConfigId)
        List<Long> queryList = []
        List<RuleInformation> ruleList = []
        if (businessConfigurationList.size() > 0) {
            List<RuleInformation> ruleInfo = []
            businessConfigurationList.each {
                ruleInfo = cacheService.getRuleInformationList(it.id)
                ruleInfo.findAll { it.isSingleCaseAlertType && it.enabled }.each { ruleInformation ->
                    int noOfMatches = StringUtils.countMatches(ruleInformation.ruleJSON, "QUERY_CRITERIA")
                    if(noOfMatches == 1) {
                        def ruleJSON = ruleInformation.ruleJSON
                        JsonSlurper jsonSlurper = new JsonSlurper()
                        def ruleMap = jsonSlurper.parseText(ruleJSON)
                        def expressionList = ruleMap.all.containerGroups
                        expressionList.each {
                            def expressions = it.expressions
                            expressions.each { def eachExpression ->
                                def expressionCategory = eachExpression["category"]
                                if (expressionCategory == "QUERY_CRITERIA") {
                                    Long queryId = eachExpression["attribute"] as Long
                                    queryList.add(queryId)
                                }
                            }
                        }
                    } else if(noOfMatches > 1){
                        ruleList.add(ruleInformation)
                    }
                }
            }
        }
        [queryList: queryList, ruleList: ruleList]
    }

    def executeRules(RuleDataDTO ruleDataDTO, def alert = null,Disposition initialDisp = null,String db=null) {


        //If there is no alert then return from here.
        if (alert == null) {
            return null
        }

        ruleDataDTO.logString.append("\nApplying Business Configuration for product : ${ruleDataDTO?.products} and Event : ${alert?.pt}")
        Boolean result
        def returnValue = null
        List<RuleInformation> filteredRuleList = []
        List<RuleInformation> matchedRuleList = []
        JsonSlurper jsonSlurper = new JsonSlurper()

        Map node = ruleDataDTO.productSelection ? jsonSlurper.parseText(ruleDataDTO.productSelection) : [:]
        List<Map> items = node.get(ruleDataDTO.configProductSelection)
        List<Map> actualItems = items.findAll{ it.id == ruleDataDTO?.productId.toString() || it.name == ruleDataDTO?.products}
        if(actualItems)
            node.put(ruleDataDTO.configProductSelection, actualItems)
        String alertProductSelection = node ? JsonOutput.toJson(node) : null
        List<BusinessConfiguration> businessConfigurationList = fetchBusinessConfigurations(ruleDataDTO.configProductSelection, ruleDataDTO.dataSource, alertProductSelection,ruleDataDTO.execConfigId,ruleDataDTO.logString)

        if (businessConfigurationList.size() > 0) {

            Set<RuleInformation> ruleInformationList
            ruleDataDTO.logString.append("\nAlert Type : ${ruleDataDTO.alertType}")
            businessConfigurationList.each {
                List<RuleInformation> ruleInformationLists= cacheService.getRuleInformationList(it.id)
                if (ruleInformationLists != null && !ruleInformationLists.isEmpty()) {
                    filteredRuleList.addAll(ruleInformationLists)
                }
            }
            filteredRuleList = filteredRuleList.flatten()
            ruleDataDTO.logString.append("\nRule List : ${filteredRuleList*.id}")

            //First of all the rules list are prepared where type is single case alert.
            if (ruleDataDTO.alertType == AlertConfigType.SINGLE_CASE_ALERT) {
                ruleInformationList = filteredRuleList.findAll {
                    it.isSingleCaseAlertType && it.enabled
                } as Set<RuleInformation>
            } else {
                ruleInformationList = filteredRuleList.findAll {
                    !it.isSingleCaseAlertType && it.enabled
                } as Set<RuleInformation>
            }

            ruleDataDTO.logString.append("\nFinal Rule List : ${ruleInformationList*.id}")

            //Now rules are iterated and rule matching flow is initiated
            ruleInformationList.eachWithIndex { RuleInformation ruleInformation, int i ->
                ruleDataDTO.ruleId = ruleInformation.id
                if (ruleInformation.isFirstTimeRule) {
                    result = false
                    if (ruleDataDTO.isPEDetectedFirstTime) {
                        result = matchRule(ruleDataDTO, ruleInformation, jsonSlurper, alert,db)
                    }
                } else {
                    result = matchRule(ruleDataDTO, ruleInformation, jsonSlurper, alert,db)
                }
                if (result) {
                    matchedRuleList << ruleInformation
                }
            }
            if (matchedRuleList) {
                ruleDataDTO.logString.append("\n*************** Matched Rules : ${matchedRuleList.collect { it.id }.join(',')} **************")
                matchedRuleList = matchedRuleList.sort { it.ruleRank }
                ruleDataDTO.logString.append("\n*************** Rule Order :  ${matchedRuleList.collect { it.id }.join(',')}   **************")
                returnValue =  applyOutcomeOfRules(matchedRuleList, ruleDataDTO.alertType, ruleDataDTO.logString, alert, initialDisp)

            } else {
                returnValue = alert
                ruleDataDTO.logString.append("\n******* No Rule Matched *******")
            }
            ruleDataDTO.logString.append("\n********** Rule Processed **********")
        } else {
            returnValue = alert
            ruleDataDTO.logString.append("\n******* No Rule Found *******")
        }
        return returnValue
    }

    def applyOutcomeOfRules(List<RuleInformation> ruleInformationList, String alertType, StringBuilder logString,
                            def alert = null, Disposition initialDisp = null) {
        Boolean isBreakAfterRule = false
        List<Map> outcomeMapList = []
        Map outcomeMap = [:]

        for (RuleInformation ruleInformation : ruleInformationList) {
            if (!isBreakAfterRule) {
                outcomeMap = [format: '', action: '', disposition: '', justificationText: '', signal: '', medicalConcepts: '', tags: '', subTags: '']
                if (ruleInformation.format) {
                    outcomeMap.format = ruleInformation.format
                }

                if (ruleInformation.actionId) {
                    if (!outcomeMapList.any { it.action instanceof ActionTemplate &&  it.action?.id == ruleInformation.actionId })
                        outcomeMap.action = cacheService.getActionTemplateCache(ruleInformation.actionId)
                }

                if (ruleInformation.dispositionId) {
                    outcomeMap.disposition = cacheService.getDispositionByValue(ruleInformation.dispositionId)
                    outcomeMap.signal = ruleInformation.signal
                    outcomeMap.medicalConcepts = ruleInformation.medicalConcepts
                    outcomeMap.signalId = ruleInformation.signal ? fetchIdFromSignalName(ruleInformation.signal) : ''
                }
                if (ruleInformation.tags) {
                    outcomeMap.tags = ruleInformation.tags
                }
                isBreakAfterRule = ruleInformation.isBreakAfterRule
                if (isBreakAfterRule) {
                    logString.append("\n******* Rule execution breaked after rule : ${ruleInformation.id} *******")
                }
                outcomeMap.justificationText = ruleInformation.justificationText
                outcomeMap.ruleId = ruleInformation.id
                logString.append("\nOutcome - Format : ${outcomeMap.format} | Action : ${outcomeMap.action} | Disposition : ${outcomeMap.disposition} | Signal : ${outcomeMap.signal} | Medical Concepts : ${outcomeMap.medicalConcepts} | Tags : ${outcomeMap.tags} ")
                outcomeMapList.add(outcomeMap)
            }
        }
        logString.append("\nFinal Outcome - Format : ${outcomeMap.format} | Action : ${outcomeMap.action} | Disposition : ${outcomeMap.disposition} | Signal : ${outcomeMap.signal} | Medical Concepts : ${outcomeMap.medicalConcepts} | Tags : ${outcomeMap.tags} ")
        switch (alertType) {
            case AlertConfigType.AGGREGATE_CASE_ALERT:
                return processOutcomeOnAggregateAlerts(alert, outcomeMapList, initialDisp)
                break
            case AlertConfigType.EVDAS_ALERT:
                processOutcomeOnEvdasAlerts(alert, outcomeMapList)
                break
            case AlertConfigType.SINGLE_CASE_ALERT:
                processOutcomeOnSingleCaseAlerts(alert, logString, outcomeMapList)
                break
            default:
                return processOutcomeOnAggregateAlerts(alert, outcomeMapList, initialDisp)
        }
    }
    void saveAlertCaseHistory(def alert, String justification, String fullName){
        alert.justification = justification
        alert.dispLastChange = new Date()
        alert.dispPerformedBy = fullName
    }

    def processOutcomeOnSingleCaseAlerts(SingleCaseAlert singleCaseAlert, StringBuilder logString, List<Map> outcomeMapList) {
        boolean isRuleApplied
        ActivityType activityType = cacheService.getActivityTypeByValue(ActivityTypeValue.DispositionChange.value)
        ActivityType actionCreatedActivity = cacheService.getActivityTypeByValue(ActivityTypeValue.ActionCreated.value)
        ActivityType tagAddedActivity = cacheService.getActivityTypeByValue(ActivityTypeValue.CategoryAdded.value)
        Disposition currentDisposition
        logString.append("\nExecuting Single Case Alert : ${singleCaseAlert?.id}")
        User user = cacheService.getUserByUserNameIlike(Constants.SYSTEM_USER)
        JsonSlurper jsonSlurper = new JsonSlurper()
        Map caseHistoryMap = [:]
        List tags = []
        Long caseId = singleCaseAlert.getAttr(cacheService.getRptFieldIndexCache('masterCaseId')) as Long

        outcomeMapList.each { outcomeMap ->
            AuditTrailDTO auditTrail = createBusinessRuleAudit("singleCaseAlert", "Individual Case Review",singleCaseAlert)
            caseHistoryMap = getCaseHistoryMap(singleCaseAlert, outcomeMap.justificationText)
            if (outcomeMap.disposition) {
                isRuleApplied = true
                currentDisposition = singleCaseAlert.disposition
                singleCaseAlert.disposition = outcomeMap.disposition
                calcDueDate(singleCaseAlert, singleCaseAlert.priority, singleCaseAlert.disposition,
                            true, cacheService.getDispositionConfigsByPriority(singleCaseAlert.priority.id))
                caseHistoryMap.dueDate = singleCaseAlert.dueDate
                caseHistoryMap.currentDisposition = singleCaseAlert.disposition
                caseHistoryMap.change = Constants.HistoryType.DISPOSITION

                if (singleCaseAlert.disposition.isValidatedConfirmed()) {
                    dataObjectService.setSignalAlertMap(singleCaseAlert.executedAlertConfigurationId, outcomeMap.signal, singleCaseAlert, outcomeMap.signalId)
                    if (outcomeMap.medicalConcepts) {
                        String medicalConceptsString = outcomeMap.medicalConcepts
                        Map medicalConceptsMap = jsonSlurper.parseText(medicalConceptsString) as Map
                        medicalConceptsService.addMedicalConcepts(singleCaseAlert, medicalConceptsMap.medicalConcepts.join(','))
                    }
                    AuditTrailChildDTO auditTrailChildSignalDTO  = createBusinessRuleChildAudit("",outcomeMap.signal,"signalName")
                    auditTrail.setAuditTrailChildDTOList(auditTrailChildSignalDTO)
                }
                caseHistoryService.saveCaseHistoryForBusinessConfig(caseHistoryMap)
                saveAlertCaseHistory(singleCaseAlert, outcomeMap.justificationText as String, user?.fullName)
                String signalMsg = outcomeMap.signal ? " and attached with signal '${outcomeMap.signal}'" : ""
                Activity activity = activityService.createActivityForSingleCaseAlert(activityType, user,
                        "Disposition changed from ${currentDisposition} to ${singleCaseAlert.disposition}${signalMsg}",
                        outcomeMap.justificationText, null,
                        singleCaseAlert.productName, singleCaseAlert.pt, singleCaseAlert.assignedTo, singleCaseAlert.caseNumber, singleCaseAlert.assignedToGroup)
                dataObjectService.setSCAActivityToMap(singleCaseAlert.executedAlertConfigurationId, activity)
                AuditTrailChildDTO auditTrailChildDispositionDTO  = createBusinessRuleChildAudit(currentDisposition.displayName,singleCaseAlert.disposition.displayName,"disposition")
                auditTrail.setAuditTrailChildDTOList(auditTrailChildDispositionDTO)
                AuditTrailChildDTO auditTrailChildJustificationDTO  = createBusinessRuleChildAudit("",outcomeMap.justificationText,"justification")
                auditTrail.setAuditTrailChildDTOList(auditTrailChildJustificationDTO)
            }
            if (outcomeMap.action) {
                isRuleApplied = true
                actionTemplateService.addActionsFromTemplate(outcomeMap.action?.id, Constants.AlertConfigType.SINGLE_CASE_ALERT, singleCaseAlert)
                Activity activity = activityService.createActivityForSingleCaseAlert(actionCreatedActivity,
                        user, "Action ${outcomeMap.action?.name} is added to alert", outcomeMap.justificationText,
                        null, singleCaseAlert.productName, singleCaseAlert.pt, singleCaseAlert.action.last().assignedTo,
                        singleCaseAlert.caseNumber, singleCaseAlert.action.last().assignedToGroup, singleCaseAlert.action.last().guestAttendeeEmail)
                dataObjectService.setSCAActivityToMap(singleCaseAlert.executedAlertConfigurationId, activity)
                AuditTrailChildDTO auditTrailChildActionDTO  = createBusinessRuleChildAudit("",outcomeMap.action?.name,"action")
                auditTrail.setAuditTrailChildDTOList(auditTrailChildActionDTO)
            }
            List tagsToUpdate = []
            //checking if outcome.tags contains any value
            if (outcomeMap.tags != null && outcomeMap.tags != "" && JSON.parse((jsonSlurper.parseText(outcomeMap.tags) as Map).tags).size()>0) {
                isRuleApplied = true
                tags.add(outcomeMap.tags)
                Map tagsMap = jsonSlurper.parseText(outcomeMap.tags) as Map

                String tagsToUpdateJson = tagsMap.tags
                tagsToUpdate = JSON.parse(tagsToUpdateJson)
                List tagList = []
                List tagListAct = []
                List subTagList = []
                List subTagListAct = []
                tagsToUpdate.each { tag ->
                    String tagType = tag.alert? Constants.Commons.CASE_SERIES_TAG: Constants.Commons.GLOBAL_TAG
                    tagList.add(["name": tag.tagText, "type": tagType] as JSON)
                    tagListAct.add(tag.tagText)
                    tag.subTags.each { subTag ->
                        subTagList.add(["name": subTag, "type": tagType] as JSON)
                        subTagListAct.add(subTag)
                    }
                }
                caseHistoryMap.tagName = tagList.toString()
                caseHistoryMap.subTagName = subTagList.toString()
                caseHistoryMap.change = Constants.HistoryType.ALERT_TAGS
                caseHistoryService.saveCaseHistoryForBusinessConfig(caseHistoryMap)
                dataObjectService.setTags(singleCaseAlert.executedAlertConfigurationId, outcomeMap.tags)
                dataObjectService.addCaseIdToMap(singleCaseAlert.executedAlertConfigurationId, caseId, singleCaseAlert?.caseVersion)
                String description = subTagListAct ? "Categories ${tagListAct?.join(',')} and Sub-Category(ies) ${subTagListAct?.join(',')} are added to alert" : "Categories ${tagListAct?.join(',')} are added to alert"

                Activity activity = activityService.createActivityForSingleCaseAlert(tagAddedActivity,
                        user, description, "",
                        null, singleCaseAlert.productName, singleCaseAlert.pt, singleCaseAlert.assignedTo,
                        singleCaseAlert.caseNumber, singleCaseAlert.assignedToGroup)
                dataObjectService.setSCAActivityToMap(singleCaseAlert.executedAlertConfigurationId, activity)
                List auditLogTagsList = []
                tagsToUpdate.each { tag ->
                    String catName = tag?.tagText
                    String subCatName = tag?.subTags ? ("["+tag?.subTags?.join(',')+"]") : ""
                    String privateStr = tag?.private ? '(P)' : ''
                    String alertStr = tag?.alert ? '(A)' : ''
                    if(subCatName.length()>0){
                        subCatName =  subCatName.replaceAll("\"", "")
                    }
                    auditLogTagsList.add(catName+subCatName+privateStr+alertStr)
                }

                AuditTrailChildDTO auditTrailChildCategoryDTO  = createBusinessRuleChildAudit("",auditLogTagsList?.join(','),"Categories")
                auditTrail.setAuditTrailChildDTOList(auditTrailChildCategoryDTO)
            }
            dataObjectService.setBusinessRuleAuditTrail(singleCaseAlert.executedAlertConfigurationId,auditTrail)
        }

        if (isRuleApplied) {
            singleCaseAlert.badge = setAutoFlagForAlert(singleCaseAlert.badge)

        }
         if(tags) {
            dataObjectService.setCaseTags(singleCaseAlert.executedAlertConfigurationId, caseId, tags)
        }
    }

    def processOutcomeOnEvdasAlerts(EvdasAlert evdasAlert, List<Map> outcomeMapList) {
        Disposition currentDisposition
        ActivityType activityType = cacheService.getActivityTypeByValue(ActivityTypeValue.DispositionChange.value)
        ActivityType actionCreatedActivity = cacheService.getActivityTypeByValue(ActivityTypeValue.ActionCreated.value)
        Long executedConfigId = evdasAlert.executedAlertConfigurationId
        Boolean isAutoRouteRuleDispo=false

        User user = cacheService.getUserByUserNameIlike(Constants.SYSTEM_USER)

        Boolean isDirty = false
        List<String> formats = []

        outcomeMapList.each {outcomeMap ->
            AuditTrailDTO auditTrail = createBusinessRuleAudit("evdasAlert","EVDAS Review",evdasAlert)
            if (outcomeMap.disposition) {
                isDirty = true
                currentDisposition = evdasAlert.disposition
                evdasAlert.disposition = outcomeMap.disposition
                isAutoRouteRuleDispo = evdasAlert.disposition.isValidatedConfirmed() && !outcomeMap.signal
                calcDueDate(evdasAlert, evdasAlert.priority, evdasAlert.disposition,
                            true, cacheService.getDispositionConfigsByPriority(evdasAlert.priority.id))
                List<Map> signalIdListForRule = dataObjectService.getAlertIdSignalListMapEvdasAlert(executedConfigId)?.get(outcomeMap.ruleId)
                if(signalIdListForRule && isAutoRouteRuleDispo){
                    createEvdasHistoryForAutoRoutePec(evdasAlert,signalIdListForRule,user,outcomeMap.justificationText,outcomeMap.ruleId,auditTrail)
                }

                if(!isAutoRouteRuleDispo){
                    //Create the Evdas history.
                    def evdasHistoryMap = [
                            "productName"    : evdasAlert.substance,
                            "eventName"      : evdasAlert.pt,
                            "disposition"    : evdasAlert.disposition,
                            "change"         : Constants.HistoryType.DISPOSITION,
                            "justification"  : outcomeMap.justificationText,
                            "asOfDate"       : evdasAlert.periodEndDate,
                            "executionDate"  : new Date(),
                            "assignedTo"     : evdasAlert.assignedTo,
                            "assignedToGroup": evdasAlert.assignedToGroup,
                            "priority"       : evdasAlert.priority,
                            "createdBy"      : Constants.Commons.SYSTEM,
                            "modifiedBy"     : Constants.Commons.SYSTEM,
                            "lastUpdated"    : new Date(),
                            "dateCreated"    : new Date(),
                            "evdasAlertId"   : evdasAlert.id,
                            "configId"       : evdasAlert.alertConfigurationId,
                            "dueDate"        : evdasAlert.dueDate,
                            "evdasAlert"     : evdasAlert,
                            "execConfigId"   : evdasAlert.executedAlertConfiguration
                    ]
                    saveAlertCaseHistory(evdasAlert, outcomeMap.justificationText as String, user?.fullName)
                    evdasHistoryMap << evdasAlertService.createCommonEvdasHistoryMap(evdasAlert)
                    dataObjectService.setEvdasBusinessConfigPropertiesMap(evdasAlert.executedAlertConfiguration.id, evdasHistoryMap)
                }

                if (evdasAlert.disposition.isValidatedConfirmed()  && outcomeMap.signal) {
                    dataObjectService.setSignalAlertMapForEvdasAlert(evdasAlert.executedAlertConfigurationId, outcomeMap.signal, evdasAlert, outcomeMap.signalId)
                    if (outcomeMap.medicalConcepts) {
                        String medicalConceptsString = outcomeMap.medicalConcepts
                        JsonSlurper jsonSlurper = new JsonSlurper()
                        Map medicalConceptsMap = jsonSlurper.parseText(medicalConceptsString) as Map
                        medicalConceptsService.addMedicalConcepts(evdasAlert, medicalConceptsMap.medicalConcepts.join(','))
                    }
                    AuditTrailChildDTO auditTrailChildSignalDTO  = createBusinessRuleChildAudit("",outcomeMap.signal,"signalName")
                    auditTrail.setAuditTrailChildDTOList(auditTrailChildSignalDTO)
                }

                if(!isAutoRouteRuleDispo){
                    String signalMsg = outcomeMap.signal ? " and attached with signal '${outcomeMap.signal}'" : ""
                    ActivityDTO activityDTO = activityService.createActivityDto(
                            evdasAlert.executedAlertConfiguration, activityType, user,
                            "Disposition changed from ${currentDisposition} to ${evdasAlert.disposition}${signalMsg}",
                            outcomeMap.justificationText, [product: getNameFieldFromJson(evdasAlert.alertConfiguration.productSelection),
                                                           event  : getNameFieldFromJson(evdasAlert.alertConfiguration.eventSelection)],
                            evdasAlert.productName, evdasAlert.pt, evdasAlert.assignedTo, null, evdasAlert.assignedToGroup)

                    dataObjectService.setEvdasActivityToMap(evdasAlert.executedAlertConfiguration.id, activityDTO)
                    AuditTrailChildDTO auditTrailChildDispositionDTO  = createBusinessRuleChildAudit(currentDisposition.displayName,evdasAlert.disposition.displayName,"disposition")
                    auditTrail.setAuditTrailChildDTOList(auditTrailChildDispositionDTO)
                }

                // create activity for auto route disposition Business Rule
                if (signalIdListForRule && isAutoRouteRuleDispo) {
                    createEvdasActivityForAutoRoutePec(evdasAlert,signalIdListForRule,currentDisposition,user,outcomeMap.justificationText,outcomeMap.ruleId)
                }
                AuditTrailChildDTO auditTrailChildJustificationDTO  = createBusinessRuleChildAudit("",outcomeMap.justificationText,"justification")
                auditTrail.setAuditTrailChildDTOList(auditTrailChildJustificationDTO)
            }


            if (outcomeMap.format) {
                isDirty = true
                formats.add(outcomeMap.format)
            }
            if (outcomeMap.action) {
                isDirty = true
                actionTemplateService.addActionsFromTemplate(outcomeMap.action?.id, Constants.AlertConfigType.EVDAS_ALERT, evdasAlert, true)
                ActivityDTO activityDTO = activityService.createActivityDto(evdasAlert.executedAlertConfiguration, actionCreatedActivity, user, "Action ${outcomeMap.action?.name} is added to alert",
                        outcomeMap.justificationText, [product: getNameFieldFromJson(evdasAlert.alertConfiguration.productSelection),
                                                       event  : getNameFieldFromJson(evdasAlert.alertConfiguration.eventSelection)],
                        evdasAlert.productName, evdasAlert.pt, evdasAlert.action.last().assignedTo, null, evdasAlert.action.last().assignedToGroup, evdasAlert.action.last().guestAttendeeEmail)
                dataObjectService.setEvdasActivityToMap(evdasAlert.executedAlertConfiguration.id, activityDTO)
                AuditTrailChildDTO auditTrailChildActionDTO  = createBusinessRuleChildAudit("",outcomeMap.action?.name,"action")
                auditTrail.setAuditTrailChildDTOList(auditTrailChildActionDTO)
            }
            if (isDirty) {
                evdasAlert.flags = setAutoFlagForAlert(evdasAlert?.flags)
            }
            isDirty = false
            dataObjectService.setBusinessRuleAuditTrail(evdasAlert.executedAlertConfiguration.id,auditTrail)
        }

        if (formats) {
            evdasAlert.format = formats.toString()
        }
        evdasAlert
    }

    String setAutoFlagForAlert(String currentBadge) {
        String newFlag = Constants.Badges.AUTO_FLAGGED
        if (currentBadge == Constants.Badges.NEW) {
            newFlag = "${Constants.Badges.AUTO_FLAGGED},${Constants.Badges.NEW}"
        } else if (currentBadge == Constants.Badges.PREVIOUSLY_REVIEWED) {
            newFlag = Constants.Badges.FLAGGED_PREVIOUSLY_REVIEWED
        } else if(currentBadge == Constants.Badges.PENDING_REVIEW){
            newFlag = "${Constants.Badges.AUTO_FLAGGED},${Constants.Badges.PENDING_REVIEW}"
        }
        newFlag
    }

    AggregateCaseAlert processOutcomeOnAggregateAlerts(AggregateCaseAlert aggregateCaseAlert, List<Map> outcomeMapList, Disposition initialDisp = null) {
        ActivityType activityType = cacheService.getActivityTypeByValue(ActivityTypeValue.DispositionChange.value)
        ActivityType actionCreatedActivity = cacheService.getActivityTypeByValue(ActivityTypeValue.ActionCreated.value)
        ActivityType tagAddedActivity = cacheService.getActivityTypeByValue(ActivityTypeValue.CategoryAdded.value)
        Boolean isDirty = false
        Long executedConfigId = aggregateCaseAlert.executedAlertConfigurationId
        User user = cacheService.getUserByUserNameIlike(Constants.SYSTEM_USER)
        Boolean isAutoRouteRuleDispo=false

        JsonSlurper jsonSlurper = new JsonSlurper()
        AlertTag alertTag
        List formats = []
        if (aggregateCaseAlert.format) {
            jsonSlurper.parseText(aggregateCaseAlert.format).each {
                formats.add(new JsonBuilder(it))
            }
        }
        List tagsList = []
        String peCom = aggregateCaseAlert.productId + '-' + aggregateCaseAlert.ptCode
        peCom = aggregateCaseAlert.smqCode ? peCom + '-' + aggregateCaseAlert.smqCode : peCom + '-null'

        outcomeMapList.each { outcomeMap ->
            AuditTrailDTO auditTrail = createBusinessRuleAudit("aggregateCaseAlert","Aggregate Review",aggregateCaseAlert)
            if (outcomeMap.disposition) {
                isDirty = true
                Disposition currentDisposition = initialDisp ?: aggregateCaseAlert.disposition
                aggregateCaseAlert.disposition = outcomeMap.disposition
                isAutoRouteRuleDispo = aggregateCaseAlert.disposition.isValidatedConfirmed() && !outcomeMap.signal
                // calculation of due date after priority change PVS-23971
                if (currentDisposition.reviewCompleted && executedConfigId) {
                    calcDueDate(aggregateCaseAlert, ExecutedConfiguration.get(executedConfigId)?.priority, aggregateCaseAlert.disposition,
                            true, cacheService.getDispositionConfigsByPriority(aggregateCaseAlert.priority.id))
                } else {
                    calcDueDate(aggregateCaseAlert, aggregateCaseAlert.priority, aggregateCaseAlert.disposition,
                            true, cacheService.getDispositionConfigsByPriority(aggregateCaseAlert.priority.id))
                }
                List<Map> signalIdListForRule = dataObjectService.getAlertIdSignalListMap(executedConfigId)?.get(outcomeMap.ruleId)
                if(signalIdListForRule  && isAutoRouteRuleDispo){
                  createHistoryForAutoRoutePec(aggregateCaseAlert,user,outcomeMap.justificationText,signalIdListForRule,outcomeMap.ruleId,auditTrail)
                }

                if(!isAutoRouteRuleDispo){
                    Map peHistoryMap = [
                            "justification": outcomeMap.justificationText,
                            "change"       : Constants.HistoryType.DISPOSITION,
                            "isLatest"     : aggregateCaseAlert.flagged,
                            "createdBy"    : Constants.Commons.SYSTEM,
                            "modifiedBy"   : Constants.Commons.SYSTEM,
                    ]
                    peHistoryMap = aggregateCaseAlertService.createBasePEHistoryMap(aggregateCaseAlert, peHistoryMap)
                    saveAlertCaseHistory(aggregateCaseAlert, outcomeMap?.justificationText as String, user?.fullName)
                    //Set the business config map.
                    dataObjectService.setBusinessConfigPropertiesMap(aggregateCaseAlert.executedAlertConfiguration.id, peHistoryMap)
                }

                if (aggregateCaseAlert.disposition.isValidatedConfirmed() && outcomeMap.signal) {
                    dataObjectService.setAggSignalAlertMap(aggregateCaseAlert.executedAlertConfigurationId, outcomeMap.signal, aggregateCaseAlert, outcomeMap.signalId)
                    if (outcomeMap.medicalConcepts) {
                        String medicalConceptsString = outcomeMap.medicalConcepts
                        Map medicalConceptsMap = jsonSlurper.parseText(medicalConceptsString) as Map
                        medicalConceptsService.addMedicalConcepts(aggregateCaseAlert,
                                medicalConceptsMap.medicalConcepts.join(','))
                    }
                    AuditTrailChildDTO auditTrailChildSignalDTO = createBusinessRuleChildAudit("", outcomeMap.signal?.take(outcomeMap.signal.lastIndexOf('(')), "signalName")
                    // last index of "(" is never null as data comes in signal:signalName(signalId) frmat
                    auditTrail.setAuditTrailChildDTOList(auditTrailChildSignalDTO)
                }


                if(!isAutoRouteRuleDispo){
                    String signalMsg = outcomeMap.signal ? " and attached with signal '${outcomeMap.signal?.take(outcomeMap.signal.lastIndexOf('('))}'" : ""
                    ActivityDTO activityDTO = activityService.createActivityDto(
                            aggregateCaseAlert.executedAlertConfiguration, activityType, user,
                            "Disposition changed from ${currentDisposition} to ${aggregateCaseAlert.disposition}${signalMsg}",
                            outcomeMap.justificationText, [product: getNameFieldFromJson(aggregateCaseAlert.alertConfiguration.productSelection),
                                                           event  : getNameFieldFromJson(aggregateCaseAlert.alertConfiguration.eventSelection)],
                            aggregateCaseAlert.productName, aggregateCaseAlert.pt, aggregateCaseAlert.assignedTo, null, aggregateCaseAlert.assignedToGroup)

                    dataObjectService.setActivityToMap(aggregateCaseAlert.executedAlertConfigurationId, activityDTO)
                }

                // create activity for auto route disposition Business Rule
               if (signalIdListForRule  && isAutoRouteRuleDispo) {
                    createActivityForAutoRoutePec(aggregateCaseAlert,currentDisposition,user,outcomeMap.justificationText,signalIdListForRule,outcomeMap.ruleId)
                }
                AuditTrailChildDTO auditTrailChildDispositionDTO  = createBusinessRuleChildAudit(currentDisposition.displayName,aggregateCaseAlert.disposition.displayName,"disposition")
                auditTrail.setAuditTrailChildDTOList(auditTrailChildDispositionDTO)
                AuditTrailChildDTO auditTrailChildJustificationDTO  = createBusinessRuleChildAudit("",outcomeMap.justificationText,"justification")
                auditTrail.setAuditTrailChildDTOList(auditTrailChildJustificationDTO)
            }
            if (outcomeMap.format) {
                isDirty = true
                formats.add(outcomeMap.format)
            }
            if (outcomeMap.action) {
                isDirty = true
                actionTemplateService.addActionsFromTemplate(outcomeMap.action?.id, Constants.AlertConfigType.AGGREGATE_CASE_ALERT, aggregateCaseAlert)
                ActivityDTO activityDTO = activityService.createActivityDto(aggregateCaseAlert.executedAlertConfiguration, actionCreatedActivity,
                        user, "Action ${outcomeMap.action?.name} is added to alert", outcomeMap.justificationText,
                        [product: getNameFieldFromJson(aggregateCaseAlert.alertConfiguration.productSelection),
                         event  : getNameFieldFromJson(aggregateCaseAlert.alertConfiguration.eventSelection)], aggregateCaseAlert.productName, aggregateCaseAlert.pt, aggregateCaseAlert.action.last().assignedTo,
                        null, aggregateCaseAlert.action.last().assignedToGroup, aggregateCaseAlert.action.last().guestAttendeeEmail)
                dataObjectService.setActivityToMap(aggregateCaseAlert.executedAlertConfiguration.id, activityDTO)
                AuditTrailChildDTO auditTrailChildActionDTO  = createBusinessRuleChildAudit("",outcomeMap.action?.name,"action")
                auditTrail.setAuditTrailChildDTOList(auditTrailChildActionDTO)
            }
            List tagsToUpdate
            if (outcomeMap.tags && jsonSlurper.parseText(outcomeMap.tags)["tags"]!="[]") {
                isDirty = true
                tagsList.add(outcomeMap.tags)
                Map tagsMap = jsonSlurper.parseText(outcomeMap.tags) as Map
                String tagsToUpdateJson = tagsMap.tags
                tagsToUpdate = JSON.parse(tagsToUpdateJson)
                List tagList = []
                List tagListAct = []
                List subTagList = []
                List subTagListAct = []
                tagsToUpdate.each { tag ->
                    String tagType = tag.alert? Constants.Commons.CASE_SERIES_TAG: Constants.Commons.GLOBAL_TAG
                    tagList.add(["name": tag.tagText, "type": tagType] as JSON)
                    tagListAct.add(tag.tagText)
                    tag.subTags.each{subTag->
                        subTagList.add(["name": subTag, "type": tagType] as JSON)
                        subTagListAct.add(subTag)
                    }
                }
                Map peHistoryMap = [
                        "justification": "",
                        "change"       : Constants.HistoryType.DISPOSITION,
                        "isLatest"     : aggregateCaseAlert.flagged,
                        "createdBy"    : Constants.Commons.SYSTEM,
                        "modifiedBy"   : Constants.Commons.SYSTEM,
                ]
                peHistoryMap = aggregateCaseAlertService.createBasePEHistoryMap(aggregateCaseAlert, peHistoryMap)
                peHistoryMap.tagName = tagList.toString()
                peHistoryMap.subTagName = subTagList.toString()
                peHistoryMap.change = Constants.HistoryType.ALERT_TAGS
                dataObjectService.setBusinessConfigPropertiesMap(aggregateCaseAlert.executedAlertConfigurationId, peHistoryMap)
                dataObjectService.setTags(aggregateCaseAlert.executedAlertConfigurationId, outcomeMap.tags)
                String description = subTagListAct ? "Categories ${tagListAct?.join(',')} and Sub-Category(ies) ${subTagListAct?.join(',')} are added to alert" : "Categories ${tagListAct?.join(',')} are added to alert"
                ActivityDTO activityDTO = activityService.createActivityDto(aggregateCaseAlert.executedAlertConfiguration, tagAddedActivity,
                        user, description, "",
                        [product: getNameFieldFromJson(aggregateCaseAlert.alertConfiguration.productSelection),
                         event  : getNameFieldFromJson(aggregateCaseAlert.alertConfiguration.eventSelection)], aggregateCaseAlert.productName, aggregateCaseAlert.pt, aggregateCaseAlert.assignedTo,
                        null, aggregateCaseAlert.assignedToGroup)
                dataObjectService.setActivityToMap(aggregateCaseAlert.executedAlertConfigurationId, activityDTO)
                List auditLogTagsList = []
                 tagsToUpdate.each { tag ->
                     String catName = tag?.tagText
                     String subCatName = tag?.subTags ? ("["+tag?.subTags?.join(',')+"]") : ""
                     String privateStr = tag?.private ? '(P)' : ''
                     String alertStr = tag?.alert ? '(A)' : ''
                     if(subCatName.length()>0){
                         subCatName =  subCatName.replaceAll("\"", "")
                     }
                     auditLogTagsList.add(catName+subCatName+privateStr+alertStr)
                 }
                AuditTrailChildDTO auditTrailChildCategoryDTO  = createBusinessRuleChildAudit("",auditLogTagsList?.join(','),"Categories")
                auditTrail.setAuditTrailChildDTOList(auditTrailChildCategoryDTO)

            }

            if (isDirty) {
                //auto flagging aggregate alerts
                aggregateCaseAlert.flags = setAutoFlagForAlert(aggregateCaseAlert?.flags)
            }

            isDirty = false
            dataObjectService.setBusinessRuleAuditTrail(aggregateCaseAlert.executedAlertConfigurationId,auditTrail)
        }

        if (tagsList) {
            dataObjectService.setTagsList("$peCom-${aggregateCaseAlert.executedAlertConfigurationId}", tagsList)
            dataObjectService.setKeyIdMap("$peCom-${aggregateCaseAlert.executedAlertConfigurationId}", aggregateCaseAlert.prodHierarchyId, aggregateCaseAlert.eventHierarchyId)
            dataObjectService.addPEComToMap(aggregateCaseAlert.executedAlertConfigurationId, peCom)
        }

        if(formats) {
            aggregateCaseAlert.format = formats.toString()
        }
        return aggregateCaseAlert
    }

    void createActivityForAutoRoutePec(AggregateCaseAlert aggregateCaseAlert, Disposition currDisposition, User user,String justification,List<Map> signalIdListForRule,Long ruleId) {
        ActivityType dispoChangeActivityType = cacheService.getActivityTypeByValue(ActivityTypeValue.DispositionChange.value)

        if (signalIdListForRule) {
            boolean associateMultipleSignal
            String ruleJSON = RuleInformation.get(ruleId)?.ruleJSON
            JsonSlurper jsonSlurper = new JsonSlurper()
            Map ruleMap = jsonSlurper.parseText(ruleJSON)
            List expressionList = ruleMap?.all?.containerGroups
            associateMultipleSignal = Boolean.parseBoolean(expressionList[0]?.expressions[0]?.assMultSignal)
            List signalIdList = []
            signalIdListForRule.each {
                AggregateCaseAlert aggAlert = it.alert
                if (aggAlert.pt == aggregateCaseAlert.pt && aggAlert.productName == aggregateCaseAlert.productName) {
                    signalIdList += it.signalList
                }
            }
            if(!associateMultipleSignal && signalIdList.size() > 1){
                List signalListLastDisp = signalIdList.sort { a, b -> ValidatedSignal.get(b).lastDispChange <=> ValidatedSignal.get(a).lastDispChange }
                signalIdList = [signalListLastDisp[0]]
            }
            signalIdList = signalIdList?.unique()
            signalIdList.eachWithIndex { it, i ->
                ValidatedSignal validatedSignal = ValidatedSignal.get(it as Long)
                String details = i == 0 ? "${currDisposition} to ${aggregateCaseAlert.disposition}" : "${aggregateCaseAlert.disposition} to ${aggregateCaseAlert.disposition}"

                ActivityDTO activityDTO = activityService.createActivityDto(
                        aggregateCaseAlert.executedAlertConfiguration, dispoChangeActivityType, user,
                        "Disposition changed from $details and attached with signal '$validatedSignal.name'",
                        "${justification}", [product: getNameFieldFromJson(aggregateCaseAlert.alertConfiguration.productSelection),
                                          event  : getNameFieldFromJson(aggregateCaseAlert.alertConfiguration.eventSelection)],
                        aggregateCaseAlert.productName, aggregateCaseAlert.pt, aggregateCaseAlert.assignedTo, null, aggregateCaseAlert.assignedToGroup)

                dataObjectService.setActivityToMap(aggregateCaseAlert.executedAlertConfigurationId, activityDTO)
            }
        }
    }

    void createHistoryForAutoRoutePec(AggregateCaseAlert aggregateCaseAlert, User user, String justification,List<Map> signalIdListForRule,Long ruleId,AuditTrailDTO auditTrail) {
        if (signalIdListForRule) {
            boolean associateMultipleSignal
            String ruleJSON = RuleInformation.get(ruleId)?.ruleJSON
            JsonSlurper jsonSlurper = new JsonSlurper()
            Map ruleMap = jsonSlurper.parseText(ruleJSON)
            List expressionList = ruleMap?.all?.containerGroups
            associateMultipleSignal = Boolean.parseBoolean(expressionList[0]?.expressions[0]?.assMultSignal)
            List signalIdList = []
            signalIdListForRule.each {
                AggregateCaseAlert aggAlert = it.alert
                if (aggAlert.pt == aggregateCaseAlert.pt && aggAlert.productName == aggregateCaseAlert.productName) {
                    signalIdList += it.signalList
                }
            }
            if(!associateMultipleSignal && signalIdList.size() > 1){
                List signalListLastDisp = signalIdList.sort { a, b -> ValidatedSignal.get(b).lastDispChange <=> ValidatedSignal.get(a).lastDispChange }
                signalIdList = [signalListLastDisp[0]]
            }
            signalIdList = signalIdList.unique()
            signalIdList.eachWithIndex { it, i ->
                Map peHistoryMap = [
                        "justification": justification,
                        "change"       : Constants.HistoryType.DISPOSITION,
                        "isLatest"     : aggregateCaseAlert.flagged,
                        "createdBy"    : Constants.Commons.SYSTEM,
                        "modifiedBy"   : Constants.Commons.SYSTEM,
                ]
                peHistoryMap = aggregateCaseAlertService.createBasePEHistoryMap(aggregateCaseAlert, peHistoryMap)
                //Set the business config map.
                dataObjectService.setBusinessConfigPropertiesMap(aggregateCaseAlert.executedAlertConfiguration.id, peHistoryMap)
            }
            saveAlertCaseHistory(aggregateCaseAlert, justification, user?.fullName)
            String signalList = signalIdList as String
            if (signalList && signalList.length() > 0) {
                signalList = signalList.substring(1, signalList.length() - 1)
            }
            Session session = sessionFactory.currentSession
            try {
                String signalNameSql = SignalQueryHelper.auto_routing_signal_name_for_audit_log(signalList)
                SQLQuery sqlSignalQuery = session.createSQLQuery(signalNameSql)
                List finalMatchingSignalList = sqlSignalQuery.list().unique()
                String finalSignalString = finalMatchingSignalList ? finalMatchingSignalList.join(",") : ""
                AuditTrailChildDTO auditTrailChildSignalDTO  = createBusinessRuleChildAudit("",finalSignalString,"signalName")
                auditTrail.setAuditTrailChildDTOList(auditTrailChildSignalDTO)
            }catch(Exception ex){
                log.info("Error occurred while saving audit log for auto routing signals: " + ex.printStackTrace())
            }finally{
                session.flush()
                session.clear()
            }
        }
    }
    AuditTrailDTO createBusinessRuleAudit(String entityName,String moduleName,def alert) {
        AuditTrailDTO auditTrail = new AuditTrailDTO()
        auditTrail.username = "SYSTEM"
        auditTrail.fullname = ""
        auditTrail.category = AuditTrail.Category.UPDATE
        auditTrail.applicationName = "PV Signal"
        auditTrail.entityName = entityName
        auditTrail.entityId = 12345 as Long
        auditTrail.description = "Disposition changed from BR"
        auditTrail.moduleName = moduleName
        auditTrail.entityValue = alert.getInstanceIdentifierForAuditLog()
        auditTrail.transactionId = sessionFactory?.currentSession?.getSessionIdentifier()
        return auditTrail
    }
    AuditTrailChildDTO createBusinessRuleChildAudit(String oldValue,String newValue,String propertyName){
        AuditTrailChildDTO auditTrailChildSignalDTO = new AuditTrailChildDTO()
        auditTrailChildSignalDTO.oldValue = oldValue
        auditTrailChildSignalDTO.newValue = newValue
        auditTrailChildSignalDTO.propertyName = propertyName
        return auditTrailChildSignalDTO
    }

    void createEvdasActivityForAutoRoutePec(EvdasAlert evdasAlert, List<Map> signalIdListForRule, Disposition currDisposition, User user,String justification,Long ruleId) {
        ActivityType dispoChangeActivityType = cacheService.getActivityTypeByValue(ActivityTypeValue.DispositionChange.value)

        if (signalIdListForRule) {
            boolean associateMultipleSignal
            String ruleJSON = RuleInformation.get(ruleId)?.ruleJSON
            JsonSlurper jsonSlurper = new JsonSlurper()
            Map ruleMap = jsonSlurper.parseText(ruleJSON)
            List expressionList = ruleMap?.all?.containerGroups
            associateMultipleSignal = Boolean.parseBoolean(expressionList[0]?.expressions[0]?.assMultSignal)
            List signalIdList = []
            signalIdListForRule.each {
                EvdasAlert evAlert = it.alert
                if (evAlert.pt == evdasAlert.pt && evAlert.productName == evdasAlert.productName) {
                    signalIdList += it.signalList
                }
            }
            if(!associateMultipleSignal && signalIdList.size() > 1){
                List signalListLastDisp = signalIdList.sort { a, b -> ValidatedSignal.get(b).lastDispChange <=> ValidatedSignal.get(a).lastDispChange }
                signalIdList = [signalListLastDisp[0]]
            }
            signalIdList = signalIdList?.unique()
            signalIdList.eachWithIndex { it, i ->
                ValidatedSignal validatedSignal = ValidatedSignal.get(it as Long)
                String details = i == 0 ? "${currDisposition} to ${evdasAlert.disposition}" : "${evdasAlert.disposition} to ${evdasAlert.disposition}"

                ActivityDTO activityDTO = activityService.createActivityDto(
                        evdasAlert.executedAlertConfiguration, dispoChangeActivityType, user,
                        "Disposition changed from $details and attached with signal '$validatedSignal.name'",
                        "${justification}", [product: getNameFieldFromJson(evdasAlert.alertConfiguration.productSelection),
                                             event  : getNameFieldFromJson(evdasAlert.alertConfiguration.eventSelection)],
                        evdasAlert.productName, evdasAlert.pt, evdasAlert.assignedTo, null, evdasAlert.assignedToGroup)

                dataObjectService.setEvdasActivityToMap(evdasAlert.executedAlertConfigurationId, activityDTO)
            }
        }
    }

    void createEvdasHistoryForAutoRoutePec(EvdasAlert evdasAlert, List<Map> signalIdListForRule, User user, String justification,Long ruleId,AuditTrailDTO auditTrail) {
        if (signalIdListForRule) {
            boolean associateMultipleSignal
            String ruleJSON = RuleInformation.get(ruleId)?.ruleJSON
            JsonSlurper jsonSlurper = new JsonSlurper()
            Map ruleMap = jsonSlurper.parseText(ruleJSON)
            List expressionList = ruleMap?.all?.containerGroups
            associateMultipleSignal = Boolean.parseBoolean(expressionList[0]?.expressions[0]?.assMultSignal)
            List signalIdList = []
            signalIdListForRule.each {
                EvdasAlert evAlert = it.alert
                if (evAlert.pt == evdasAlert.pt && evAlert.productName == evdasAlert.productName) {
                    signalIdList += it.signalList
                }
            }
            if(!associateMultipleSignal && signalIdList.size() > 1){
                List signalListLastDisp = signalIdList.sort { a, b -> ValidatedSignal.get(b).lastDispChange <=> ValidatedSignal.get(a).lastDispChange }
                signalIdList = [signalListLastDisp[0]]
            }
            signalIdList = signalIdList.unique()
            signalIdList.eachWithIndex { it, i ->
                def evdasHistoryMap = [
                        "productName"    : evdasAlert.substance,
                        "eventName"      : evdasAlert.pt,
                        "disposition"    : evdasAlert.disposition,
                        "change"         : Constants.HistoryType.DISPOSITION,
                        "justification"  : justification,
                        "asOfDate"       : evdasAlert.periodEndDate,
                        "executionDate"  : new Date(),
                        "assignedTo"     : evdasAlert.assignedTo,
                        "assignedToGroup": evdasAlert.assignedToGroup,
                        "priority"       : evdasAlert.priority,
                        "createdBy"      : Constants.Commons.SYSTEM,
                        "modifiedBy"     : Constants.Commons.SYSTEM,
                        "lastUpdated"    : new Date(),
                        "dateCreated"    : new Date(),
                        "evdasAlertId"   : evdasAlert.id,
                        "configId"       : evdasAlert.alertConfigurationId,
                        "dueDate"        : evdasAlert.dueDate,
                        "execConfigId"   : evdasAlert.executedAlertConfiguration
                ]
                evdasHistoryMap << evdasAlertService.createCommonEvdasHistoryMap(evdasAlert)
                dataObjectService.setEvdasBusinessConfigPropertiesMap(evdasAlert.executedAlertConfiguration.id, evdasHistoryMap)
            }
            saveAlertCaseHistory(evdasAlert, justification as String, user?.fullName)
            String signalList = signalIdList as String
            if (signalList && signalList.length() > 0) {
                signalList = signalList.substring(1, signalList.length() - 1)
            }
            Session session = sessionFactory.currentSession
            try {
                String signalNameSql = SignalQueryHelper.auto_routing_signal_name_for_audit_log(signalList)
                SQLQuery sqlSignalQuery = session.createSQLQuery(signalNameSql)
                List finalMatchingSignalList = sqlSignalQuery.list().unique()
                String finalSignalString = finalMatchingSignalList ? finalMatchingSignalList.join(",") : ""
                AuditTrailChildDTO auditTrailChildSignalDTO  = createBusinessRuleChildAudit("",finalSignalString,"signalName")
                auditTrail.setAuditTrailChildDTOList(auditTrailChildSignalDTO)
            }catch(Exception ex){
                log.info("Error occurred while saving audit log for auto routing signals: " + ex.printStackTrace())
            }finally{
                session.flush()
                session.clear()
            }
        }
    }

    Map getCaseHistoryMap(def singleCaseAlert, String justificationText) {
        [
                "configId"              : singleCaseAlert.alertConfigurationId,
                "currentDisposition"    : singleCaseAlert.disposition,
                "currentPriority"       : singleCaseAlert.priority,
                "productFamily"         : singleCaseAlert.productFamily,
                "caseNumber"            : singleCaseAlert.caseNumber,
                "caseVersion"           : singleCaseAlert.caseVersion,
                "currentAssignedTo"     : singleCaseAlert.assignedTo,
                "currentAssignedToGroup": singleCaseAlert.assignedToGroup,
                "justification"         : justificationText,
                "createdBy"             : Constants.Commons.SYSTEM,
                "modifiedBy"            : Constants.Commons.SYSTEM,
                "dateCreated"           : singleCaseAlert.dateCreated,
                "lastUpdated"           : singleCaseAlert.dateCreated,
                "followUpNumber"        : singleCaseAlert.followUpNumber,
                "execConfigId"          : singleCaseAlert.executedAlertConfigurationId,
                "dueDate"               : singleCaseAlert.dueDate
        ]
    }

    Boolean matchRule(RuleDataDTO ruleDataDTO, RuleInformation ruleInfo, JsonSlurper jsonSlurper,def alert = null,String db=null) {
        Map object = jsonSlurper.parseText(ruleInfo.getRuleJSON())
        Map expressionObj = [:]
        if(object.all.containerGroups.size() > 1 && object.all.keyword) {
            expressionObj = [expressions: [object.all.containerGroups[0], object.all.containerGroups[1]],
                                keyword    : object.all.keyword]
        } else{
            expressionObj = object.all.containerGroups[0]
        }
        ruleDataDTO.logString.append("\nRule : ${expressionObj}")
        Boolean result = generateRule(ruleDataDTO, expressionObj, alert,"" ,ruleInfo,db)
        ruleDataDTO.logString.append("\n**********************************************")
        ruleDataDTO.logString.append("\n              Rule Matched : ${result}        ")
        ruleDataDTO.logString.append("\n**********************************************")
        result
    }

    Boolean generateRule(RuleDataDTO ruleDataDTO, Map expressionObj ,def alert = null , String prefix = "",RuleInformation ruleInfo,String db=null) {
        if (expressionObj.containsKey('keyword')) {
            if (expressionObj.keyword == 'and') {
                ruleDataDTO.logString.append("\n${prefix}├── AND")
                prefix = getPrefix(prefix)
                Boolean andResult = true
                for (int i = 0; i < expressionObj.expressions.size(); i++) {
                    andResult = andResult && generateRule(ruleDataDTO, expressionObj.expressions[i], alert, prefix ,ruleInfo,db)
                }
                return andResult
            } else {
                ruleDataDTO.logString.append("\n${prefix}├── OR")
                prefix = getPrefix(prefix)
                Boolean orResult = false
                for (int i = 0; i < expressionObj.expressions.size(); i++) {
                    Boolean generateResult = generateRule(ruleDataDTO, expressionObj.expressions[i], alert, prefix, ruleInfo,db)
                    orResult = orResult || generateResult
                }
                return orResult
            }
        } else {
            if (expressionObj.containsKey('expressions')) {
                Boolean result = true
                for (int i = 0; i < expressionObj.expressions.size(); i++) {
                    result = result && generateRule(ruleDataDTO, expressionObj.expressions[i], alert, prefix ,ruleInfo,db)
                }
                return result
            } else {
                return generateExpression(ruleDataDTO, expressionObj, prefix, alert,ruleInfo,db)
            }
        }
    }

    Boolean generateExpression(RuleDataDTO ruleDataDTO, Map expressionObj, String prefix, def alert = null, RuleInformation ruleInfo, String db = null) {
        Boolean result = false
        try {
            def getValueFromRuleData = { attr ->
                ruleDataDTO.mapData()["${attr}"]
            }

            Map operatorMap = fetchOperatorMap(prefix, ruleDataDTO)
            Map thresholdData = getAlgoThresholdValue(ruleDataDTO, expressionObj)


            String thresholdValue = thresholdData.get('thresholdValue')
            boolean isContainThresholdValue = thresholdData.get('isContainThresholdValue')
            boolean isCurrentAlertRule = thresholdData.get('isCurrentAlertRule')
            boolean subGroupNotExist = thresholdData.get('subGroupNotExist')

            Map categoryMap = [
                    'ALGORITHM'           : {
                        ruleDataDTO.logString.append("\n${prefix}├── ${expressionObj.attribute}")
                        if (isContainThresholdValue) {
                            if (subGroupNotExist) {
                                // to handle scenerio when subgroup was persent during business rule configuration but removed after
                                return false
                            } else if (expressionObj.percent != null && expressionObj.percent != "" && expressionObj.percent != "undefined") {
                                if ((!isCurrentAlertRule && !ruleDataDTO.previousAggAlert && !ruleDataDTO.previousEvdasAlert)) {
                                    return false
                                }
                                Double firstAttr = getValueFromRuleData(expressionObj.attribute)
                                Double secondAttr = getThresholdValue(isCurrentAlertRule, alert, thresholdValue, ruleDataDTO)
                                if (firstAttr == null) {
                                    firstAttr = 0
                                }
                                if (secondAttr == null) {
                                    secondAttr = 0
                                }
                                if (firstAttr < 0 || secondAttr < 0) {
                                    return false
                                }
                                if ((secondAttr == 0 || secondAttr == 0.0) && (firstAttr == 0 || firstAttr == 0.0) && expressionObj.operator in [Constants.Operators.EQUAL_TO,
                                                                                                                                                 Constants.Operators.GREATER_THAN_OR_EQAUL_TO, Constants.Operators.LESS_THAN_OR_EQUAL_TO]) {
                                    return false
                                }
                                Integer percent = Integer.parseInt(expressionObj.percent)
                                if (expressionObj.operator in [Constants.Operators.EQUAL_TO, Constants.Operators.NOT_EQUAL_TO]) {
                                    secondAttr = (secondAttr * percent) / 100
                                    operatorMap["${expressionObj.operator}"](firstAttr, secondAttr, getPrefix(prefix))
                                } else if (expressionObj.operator in [Constants.Operators.LESS_THAN_OR_EQUAL_TO, Constants.Operators.LESS_THAN]) {
                                    secondAttr = secondAttr - ((secondAttr * percent) / 100)
                                    operatorMap["${expressionObj.operator}"](firstAttr, secondAttr, getPrefix(prefix))
                                } else if (expressionObj.operator in [Constants.Operators.GREATER_THAN, Constants.Operators.GREATER_THAN_OR_EQAUL_TO]) {
                                    secondAttr = secondAttr + ((secondAttr * percent) / 100)
                                    operatorMap["${expressionObj.operator}"](firstAttr, secondAttr, getPrefix(prefix))
                                } else {
                                    false
                                }
                            } else if (ruleDataDTO.previousAggAlert || ruleDataDTO.previousEvdasAlert || isCurrentAlertRule) {
                                Double firstAttr = getValueFromRuleData(expressionObj.attribute)
                                Double threshold = getThresholdValue(isCurrentAlertRule, alert, thresholdValue, ruleDataDTO)
                                if(firstAttr == null && threshold == null){
                                    return false
                                }
                                operatorMap["${expressionObj.operator}"](firstAttr, threshold, getPrefix(prefix))
                            } else {
                                false
                            }
                        } else {
                            if (expressionObj.attribute in [Constants.BusinessConfigAttributes.PREVIOUS_CATEGORY, Constants.BusinessConfigAttributes.ALL_CATEGORY]) {
                                String ruleDataAttr = (getValueFromRuleData(expressionObj.attribute) as List)?.join(",")
                                if (expressionObj.operator in [Constants.Operators.IS_EMPTY, Constants.Operators.IS_NOT_EMPTY]) {
                                    operatorMap["${expressionObj.operator}"](ruleDataAttr, null, getPrefix(prefix))
                                } else {
                                    List attrTagList = (getValueFromRuleData(expressionObj.attribute) as List)
                                    List thresholdValues = expressionObj.threshold?.split(",")
                                    Boolean res = false
                                    if (expressionObj.operator in [Constants.Operators.NOT_EQUAL_TO, Constants.Operators.DOES_NOT_CONTAIN,
                                                                   Constants.Operators.DOES_NOT_END_WITH, Constants.Operators.DOES_NOT_START_WITH]) {
                                        res = true
                                    }

                                    for (String tag : attrTagList) {
                                        for (String value : thresholdValues) {
                                            if (tag && value)
                                                if (expressionObj.operator in [Constants.Operators.NOT_EQUAL_TO, Constants.Operators.DOES_NOT_CONTAIN,
                                                                               Constants.Operators.DOES_NOT_END_WITH, Constants.Operators.DOES_NOT_START_WITH]) {
                                                    res = res && (operatorMap["${expressionObj.operator}"](tag, value, getPrefix(prefix), true))
                                                } else {
                                                    res = res || (operatorMap["${expressionObj.operator}"](tag, value, getPrefix(prefix), true))
                                                }
                                        }
                                    }
                                    res
                                }
                            } else if (expressionObj.attribute in [Constants.BusinessConfigAttributesEvdas.SDR_EVDAS, Constants.BusinessConfigAttributesEvdas.CHANGES_EVDAS, Constants.BusinessConfigAttributes.LISTEDNESS, Constants.BusinessConfigAttributesEvdas.EVDAS_LISTEDNESS, Constants.BusinessConfigAttributesEvdas.NEW_EVENT,
                                                                   Constants.BusinessConfigAttributesEvdas.EVDAS_IME_DME, Constants.BusinessConfigAttributesEvdas.EVDAS_SDR_PAED, Constants.BusinessConfigAttributesEvdas.EVDAS_SDR_GERTR]) {
                                operatorMap["${expressionObj.operator}"](getValueFromRuleData(expressionObj.attribute)?.toLowerCase()?.trim(), expressionObj.threshold?.toLowerCase()?.trim(), getPrefix(prefix))
                            } else if (expressionObj.attribute in [Constants.BusinessConfigAttributesEvdas.DME_AGG_ALERT, Constants.BusinessConfigAttributesEvdas.IME_AGG_ALERT, Constants.BusinessConfigAttributesEvdas.SPECIAL_MONITORING, Constants.BusinessConfigAttributesEvdas.STOP_LIST]) {
                                if (expressionObj.operator == Constants.Operators.NOT_EQUAL_TO) {
                                    expressionObj.operator = Constants.Operators.EQUAL_TO
                                    if (expressionObj.threshold?.toLowerCase()?.trim() == Constants.Commons.NO_LOWERCASE.toLowerCase()) {
                                        expressionObj.threshold = Constants.Commons.YES_LOWERCASE
                                    } else if (expressionObj.threshold?.toLowerCase()?.trim() == Constants.Commons.YES_LOWERCASE.toLowerCase()) {
                                        expressionObj.threshold = Constants.Commons.NO_LOWERCASE
                                    }
                                }
                                if (expressionObj.isProductSpecific && expressionObj.isProductSpecific == "true") {
                                    operatorMap["${expressionObj.operator}"](getValueFromRuleData(expressionObj.attribute + "_PROD")?.toLowerCase()?.trim(), expressionObj.threshold?.toLowerCase()?.trim(), getPrefix(prefix))
                                } else {
                                    operatorMap["${expressionObj.operator}"](getValueFromRuleData(expressionObj.attribute)?.toLowerCase()?.trim(), expressionObj.threshold?.toLowerCase()?.trim(), getPrefix(prefix))
                                }
                            } else if (expressionObj.attribute == 'trendType' || expressionObj.attribute == Constants.BusinessConfigAttributes.TREND_FLAG) {
                                String ruleDataAttr = getValueFromRuleData(expressionObj.attribute)
                                if (expressionObj.operator in [Constants.Operators.IS_EMPTY, Constants.Operators.IS_NOT_EMPTY]) {
                                    operatorMap["${expressionObj.operator}"](ruleDataAttr, null, getPrefix(prefix))
                                } else {
                                    operatorMap["${expressionObj.operator}"](ruleDataAttr?.toLowerCase()?.trim(), expressionObj.threshold?.toLowerCase()?.trim(), getPrefix(prefix))
                                }
                            } else {
                                Double firstAttr = getValueFromRuleData(expressionObj.attribute)
                                def secondAttr = expressionObj.operator in [Constants.Operators.IS_EMPTY, Constants.Operators.IS_NOT_EMPTY] ? null : expressionObj.threshold
                                if(firstAttr == null && secondAttr == null){
                                    return false
                                }
                                operatorMap["${expressionObj.operator}"](firstAttr, secondAttr ? secondAttr as Double : null, getPrefix(prefix))
                            }
                        }
                    },
                    'QUERY_CRITERIA'      : {
                        ruleDataDTO.logString.append("\n${prefix}├── ${expressionObj.attribute}")
                        Long queryId = expressionObj.attribute as Long
                        //Sample output : [[query:19980, caseList:[16US001165]]]
                        List<Map> queryCaseMaps = ruleDataDTO.queryCaseMaps
                        boolean queryValid = queryCaseMaps.any { (it.query == queryId || it.rule == ruleDataDTO.ruleId) && it.caseList.contains(ruleDataDTO.caseId) }
                        ruleDataDTO.logString.append("\n${getPrefix(prefix)}└──${queryValid}")
                        ruleDataDTO.logString.append("\nQuery applied : " + queryValid)
                        //Execute the query
                        queryValid
                    },
                    'COUNTS'              : {
                        ruleDataDTO.logString.append("\n${prefix}├── ${expressionObj.attribute}")
                        if (isContainThresholdValue) {
                            if (expressionObj.percent != null && expressionObj.percent != "" && expressionObj.percent != "undefined") {
                                if ((!isCurrentAlertRule && !ruleDataDTO.previousAggAlert && !ruleDataDTO.previousEvdasAlert)) {
                                    return false
                                }
                                Double firstAttr = getValueFromRuleData(expressionObj.attribute)
                                Double secondAttr = getThresholdValue(isCurrentAlertRule, alert, thresholdValue, ruleDataDTO)

                                if (firstAttr == null) {
                                    firstAttr = 0
                                }
                                if (secondAttr == null) {
                                    secondAttr = 0
                                }
                                if (firstAttr < 0 || secondAttr < 0) {
                                    return false
                                }
                                if (secondAttr == 0 && firstAttr == 0 && expressionObj.operator in [Constants.Operators.EQUAL_TO,
                                                                                                    Constants.Operators.GREATER_THAN_OR_EQAUL_TO, Constants.Operators.LESS_THAN_OR_EQUAL_TO]) {
                                    return false
                                }
                                Integer percent = Integer.parseInt(expressionObj.percent)
                                if (expressionObj.operator in [Constants.Operators.EQUAL_TO, Constants.Operators.NOT_EQUAL_TO]) {
                                    secondAttr = (secondAttr * percent) / 100
                                    operatorMap["${expressionObj.operator}"](firstAttr, secondAttr, getPrefix(prefix))
                                } else if (expressionObj.operator in [Constants.Operators.LESS_THAN_OR_EQUAL_TO, Constants.Operators.LESS_THAN]) {
                                    secondAttr = secondAttr - ((secondAttr * percent) / 100)
                                    operatorMap["${expressionObj.operator}"](firstAttr, secondAttr, getPrefix(prefix))
                                } else if (expressionObj.operator in [Constants.Operators.GREATER_THAN, Constants.Operators.GREATER_THAN_OR_EQAUL_TO]) {
                                    secondAttr = secondAttr + ((secondAttr * percent) / 100)
                                    operatorMap["${expressionObj.operator}"](firstAttr, secondAttr, getPrefix(prefix))
                                } else {
                                    false
                                }
                            } else if (ruleDataDTO.previousAggAlert || ruleDataDTO.previousEvdasAlert || isCurrentAlertRule){
                                Integer firstAttr = getValueFromRuleData(expressionObj.attribute)
                                Integer threshold = getAlertThesholdValue(ruleDataDTO, alert, thresholdValue, isCurrentAlertRule) as Integer
                                if(firstAttr == null && threshold == null){
                                    return false
                                }
                                operatorMap["${expressionObj.operator}"](firstAttr, threshold, getPrefix(prefix))
                            } else {
                                return false
                            }
                        } else {
                            if (expressionObj.attribute in [Constants.BusinessConfigAttributes.POSITIVE_RE_CHALLENGE]) {
                                operatorMap["Content Match"](getValueFromRuleData(expressionObj.attribute)?.toLowerCase(), expressionObj.threshold?.toLowerCase(), getPrefix(prefix))
                            } else {
                                Integer firstAttr = getValueFromRuleData(expressionObj.attribute)
                                def secondAttr = expressionObj.operator in [Constants.Operators.IS_EMPTY, Constants.Operators.IS_NOT_EMPTY] ? null : expressionObj.threshold
                                if (expressionObj.operator in [Constants.Operators.IS_EMPTY]) {
                                    operatorMap[Constants.Operators.IS_EMPTY_COUNTS](firstAttr, secondAttr ? secondAttr as Integer : null, getPrefix(prefix))
                                } else if (expressionObj.operator in [Constants.Operators.IS_NOT_EMPTY]) {
                                    operatorMap[Constants.Operators.IS_NOT_EMPTY_COUNTS](firstAttr, secondAttr ? secondAttr as Integer : null, getPrefix(prefix))
                                } else {
                                    operatorMap["${expressionObj.operator}"](firstAttr, secondAttr ? secondAttr as Integer : null, getPrefix(prefix))
                                }
                            }
                        }
                    },
                    'REVIEW_STATE'        : {
                        ruleDataDTO.logString.append("\n${prefix}├── ${expressionObj.attribute}")
                        result = ruleDataDTO?.disposition?.displayName == expressionObj.attribute
                        ruleDataDTO.logString.append("\n${getPrefix(prefix)}└──${result}")
                        result
                    },
                    'LAST_REVIEW_DURATION': {
                        ruleDataDTO.logString.append("\n${prefix}├── ${expressionObj.category}")
                        if (ruleDataDTO.isLastReviewDuration) {
                            ruleDataDTO.isLastReviewDuration
                        } else {
                            operatorMap["${expressionObj.operator}"](ruleDataDTO.lastReviewDuration, expressionObj.threshold as Integer, getPrefix(prefix))
                        }
                    },
                    'SIGNAL_REVIEW_STATE' : {
                        ruleDataDTO.logString.append("\n${prefix}├── ${expressionObj.attribute}")
                        result = fetchMatchingSignalList(alert, expressionObj, ruleInfo, ruleDataDTO.alertType, ruleDataDTO)
                        ruleDataDTO.logString.append("\n${getPrefix(prefix)}└──${result}")
                        return result
                    }
            ]
            result = categoryMap["${expressionObj.category}"]()
            return result
        }catch(Exception ex){
            ex.printStackTrace()
            throw new Exception("Alert got failed while generating rule expression while applying Business rules " + ex.getStackTrace())
        }
    }

    def fetchMatchingSignalList(def alert, Map expressionObj,RuleInformation ruleInformation,String alertType,RuleDataDTO ruleDataDTO){
        boolean isSplitToPtLevel = Boolean.parseBoolean(expressionObj.splitSignalToPt as String)
        boolean isAssociateClosedSignal = Boolean.parseBoolean(expressionObj.assClosedSignal as String)
        boolean isAssociateMultipleSignal = Boolean.parseBoolean(expressionObj.assMultSignal as String)
        boolean result = false
        Disposition qualifiedSignalDisposition = Disposition.findByDisplayName(expressionObj.attribute as String)
        Long dispositionId = qualifiedSignalDisposition.id as Long
        def executedConfiguration
        def domain
        Long productId
        if(alertType == AlertConfigType.EVDAS_ALERT){
            executedConfiguration = ExecutedEvdasConfiguration.get(alert.executedAlertConfigurationId)
            productId = alert.substanceId
            domain = ExecutedEvdasConfiguration
        }else{
            executedConfiguration = ExecutedConfiguration.get(alert.executedAlertConfigurationId)
            productId = alert.productId
            domain = ExecutedConfiguration
        }
        Long exConfigId = executedConfiguration.id as Long
        def getMatchingList = dataObjectService.getAggSignalAlertMapForSignalState(exConfigId)
        if(!(getMatchingList?.get(productId))) {
            matchingSignalsOnProductLevel(executedConfiguration,productId,isAssociateClosedSignal)
        }
        def alertMatchingList  = dataObjectService.getAggSignalAlertMapForSignalState(exConfigId)?.get(productId)
        List<Long> prevExecConfigIdList = domain.createCriteria().list {
            projections {
                property('id')
            }
            eq("name", executedConfiguration.name)
            'not' {
                'eq'("id", exConfigId)
            }
            order("id", "desc")
            maxResults(1)
        }
        if (prevExecConfigIdList.size() > 0 && !isAssociateMultipleSignal) {
            Session session = sessionFactory.currentSession
            SQLQuery sqlQuery
            try {
                ruleDataDTO.logString.append("\n ----- Fetching Previous run signals as Associate Multiple checkbox is disabled")
                String sql_statement
                List<String> productIdAndPtCodeList
                if(alertType == AlertConfigType.EVDAS_ALERT){
                    productIdAndPtCodeList = ["(" + alert.substanceId + "," + alert.ptCode + ")"]
                    sql_statement = SignalQueryHelper.signal_ids_evdas_auto_routing(productIdAndPtCodeList.join(","), exConfigId, prevExecConfigIdList[0])
                }else{
                    productIdAndPtCodeList = ["(" + alert.productId + "," + alert.ptCode + ",'" + alert.soc + "','" + alert.pt?.replace("'", "''") + "')"]
                    sql_statement = SignalQueryHelper.signal_ids_auto_routing(productIdAndPtCodeList.join(","), exConfigId, prevExecConfigIdList[0])
                }
                sqlQuery = session.createSQLQuery(sql_statement)
            }catch(Exception ex){
                log.error(ex)
            }finally{
                session.flush()
                session.clear()
            }
            if(sqlQuery?.list()?.size() > 0){
                ruleDataDTO.logString.append("\n └──Associate Multiple signal checkbox is disabled and signals are found in previous run hence business rule not applying for alert: ${alert.name} and pt: ${alert.pt}")
                return false
            }
        }
        ruleDataDTO.logString.append("\nsignal matching list on product basis for product ${alert.productName} is └──${alertMatchingList}")
        if(alertMatchingList) {
            result = matchingSignalsOnEventLevel(executedConfiguration,alert,alertMatchingList,result,isAssociateMultipleSignal,isSplitToPtLevel,ruleInformation,dispositionId,alertType,ruleDataDTO)
            return result
        }
    }

    def matchingSignalsOnProductLevel(def executedConfiguration,Long productId,boolean isAssociateClosedSignal){
        Long exConfigId = executedConfiguration.id as Long
        Session session = sessionFactory.currentSession
        Map matchingSignalMap = [:]
        String pecProducts = ""
        Map productList = JSON.parse(executedConfiguration?.allProducts) as Map
        List pecProductList = productList.get(productId as String)
        pecProducts = pecProductList as String
        if (pecProducts && pecProducts.length() > 0) {
            pecProducts = pecProducts.substring(1, pecProducts.length() - 1)
        }
        String sql_statement = ""
        if (executedConfiguration.productGroupSelection) {
            def productGroupSelection = JSON.parse(executedConfiguration.productGroupSelection)
            String productGroup = []
            productGroupSelection?.each{ it ->
                if((it.id as Long) == (productId as Long)){
                    productGroup =  it.name
                }
            }
            sql_statement = SignalQueryHelper.fetch_signal_list_query_product_group(pecProducts,isAssociateClosedSignal,productGroup)
        } else {
            sql_statement = SignalQueryHelper.fetch_signal_list_query_product(pecProducts,isAssociateClosedSignal)
        }
        try {
            SQLQuery sqlQuery = session.createSQLQuery(sql_statement)
            List resultList = sqlQuery.list().unique()
            matchingSignalMap.put(productId, resultList)
            dataObjectService.setAggSignalAlertMapForSignalState(exConfigId, matchingSignalMap)
        }catch(Exception e){
            log.error("Error Occurred while Matching signals for auto route disposition on product level", e)
            throw e
        }finally {
            session.flush()
            session.clear()
        }
    }

    def matchingSignalsOnEventLevel(def executedConfiguration, def alert, List alertMatchingList,boolean result,boolean isAssociateMultipleSignal,boolean isSplitToPtLevel,RuleInformation ruleInformation,Long dispositionId,String alertType,RuleDataDTO ruleDataDTO){
        Long exConfigId = executedConfiguration.id as Long
        String pt = alert.pt
        Session session = sessionFactory.currentSession
        String alertPt = ""
        String sql_signal = ""
        String signalListQuery = " "
        if(alertMatchingList.size() >1000) {
            signalListQuery = " ("
            alertMatchingList.collate(1000).eachWithIndex {signalIds, index ->
                if(index == 0) {
                    signalListQuery += " v1.id in (${signalIds.join(',')}) "
                } else {
                    signalListQuery += " or v1.id in (${signalIds.join(',')}) "
                }
            }
            signalListQuery += " ) "
        } else if(alertMatchingList.size() >= 1) {
            signalListQuery = " v1.id in (${alertMatchingList.join(',')}) "
        }
        if(alertType!= AlertConfigType.EVDAS_ALERT && executedConfiguration.groupBySmq){
            if(executedConfiguration.eventGroupSelection){
                String eventGroupPt = pt + " ("
                Long eventGroupId
                def eventsGroup = JSON.parse(executedConfiguration.eventGroupSelection)
                eventsGroup.each{ it ->
                    if(it.name.startsWith(eventGroupPt)){
                        eventGroupId =  it.id as Long
                    }
                }
                if(eventGroupId) {
                    alertPt = pt + " (" + eventGroupId + ")"
                    sql_signal = SignalQueryHelper.final_signal_list_query_product_event_smq_alert_event_group(alertPt,signalListQuery,dispositionId)
                }

            }else{
                alertPt = "'" + pt + "'"
                    sql_signal = SignalQueryHelper.final_signal_list_query_product_event_smq_alert(alertPt, signalListQuery, dispositionId, isSplitToPtLevel)
            }
        }else{
            if(isSplitToPtLevel){
                alertPt = "'" + pt + "'"
                sql_signal = SignalQueryHelper.final_signal_list_query_product_event_all_pt_checked(alertPt,signalListQuery,dispositionId)
            }else {
                alertPt = "'" + "4-" + pt + "'"
                sql_signal = SignalQueryHelper.final_signal_list_query_product_event(alertPt, signalListQuery,dispositionId)
            }
        }
        try {
            log.info(sql_signal)
            SQLQuery sqlSignalQuery = session.createSQLQuery(sql_signal)
            List finalMatchingSignalList = sqlSignalQuery.list().unique()
            ruleDataDTO.logString.append("\nfinal signal matching list for pt : ${alert.pt} is └──${finalMatchingSignalList}")
            if(finalMatchingSignalList){
                Disposition ruleDisposition = cacheService.getDispositionByValue(ruleInformation.dispositionId)
                if(ruleDisposition && ruleDisposition.isValidatedConfirmed() && !ruleInformation.signal) {
                    if(alertType == AlertConfigType.EVDAS_ALERT){
                        if (isAssociateMultipleSignal) {
                            dataObjectService.setAlertIdSignalListMapEvdasAlert(exConfigId, alert, finalMatchingSignalList,ruleInformation.id)
                        } else {
                            List onlyOneMatchingSignal = [finalMatchingSignalList[0]]
                            dataObjectService.setAlertIdSignalListMapEvdasAlert(exConfigId, alert, onlyOneMatchingSignal,ruleInformation.id)
                        }
                    }else{
                        if (isAssociateMultipleSignal) {
                            dataObjectService.setAlertIdSignalListMap(exConfigId, alert, finalMatchingSignalList,ruleInformation.id)
                        } else {
                            List onlyOneMatchingSignal = [finalMatchingSignalList[0]]
                            dataObjectService.setAlertIdSignalListMap(exConfigId, alert, onlyOneMatchingSignal,ruleInformation.id)
                        }
                    }
                }
                result = true
            }
        }catch(Exception e){
            log.error("Error Occurred while Matching signals for auto route event", e)
            throw e
        }finally {
            session.flush()
            session.clear()
        }
        return result
    }


    private Double getThresholdValue(boolean isCurrentAlertRule, def alert, String thresholdValue, RuleDataDTO ruleDataDTO) throws Exception {
        def threshold
        boolean subGroupCheck
        if (ruleDataDTO.dataSource == Constants.DataSource.FAERS) {
            threshold = getAlertThesholdValue(ruleDataDTO, alert, thresholdValue, isCurrentAlertRule)
        } else if (ruleDataDTO?.dataSource == Constants.DataSource.PVA) {
                cacheService.getAllOtherSubGroupKeyValues(Constants.DataSource.PVA).each { key, value ->
                    if (thresholdValue == key) {
                        subGroupCheck = true
                        threshold = allSubGroupThresholdValue(ruleDataDTO, alert, thresholdValue, value, isCurrentAlertRule)
                    }
                }

            if(!subGroupCheck){
                cacheService.getRelSubGroupKeyValues(Constants.DataSource.PVA).each { key, value ->
                    if (thresholdValue == key) {
                        subGroupCheck = true
                        threshold = allSubGroupThresholdValue(ruleDataDTO, alert, thresholdValue, value, isCurrentAlertRule)
                    }
                }
            }
            if (!subGroupCheck) {
                threshold = getAlertThesholdValue(ruleDataDTO, alert, thresholdValue, isCurrentAlertRule)
            }
        } else {
            threshold = getAlertThesholdValue(ruleDataDTO, alert, thresholdValue, isCurrentAlertRule)
        }
        threshold
    }

    def getAlertThesholdValue(RuleDataDTO ruleDataDTO, def alert, String thresholdValue, boolean isCurrentAlertRule) throws Exception {
        def threshold
        def currentValue = 0.0
            Map alertData = ruleDataDTO.mapData()
            currentValue = alertData.get(thresholdValue)
            def value = null
            if (ruleDataDTO.previousAggAlert) {
                value = ruleDataDTO.prevAggData[thresholdValue]
            } else if (ruleDataDTO.previousEvdasAlert) {
                value = ruleDataDTO.prevEvdasData[thresholdValue]
            } else {
                value = 0
            }
            def previousValue = (value || value == 0 || value == 0.0) ? value as Double : null
            threshold = isCurrentAlertRule ? (currentValue || currentValue == 0 ? currentValue as Double : null) : previousValue
            threshold
    }

    def subGroupThresholdValue(RuleDataDTO ruleDataDTO, def alert, String thresholdValue, String key, String value, boolean isCurrentAlertRule) {
        def threshold
        String property = thresholdValue.replace(key, "") + value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase()
        if (ruleDataDTO.dataSource == Constants.DataSource.FAERS) {
            property = property + Constants.ViewsDataSourceLabels.FAERS
        }
        String groupValue = isCurrentAlertRule ? alert.getProperty(property) : ruleDataDTO.previousAggAlert?.getProperty(property)
        if (StringUtils.isNotBlank(groupValue)) {
            Integer jsonStringLength = groupValue?.length() - 1
            String jsonString = groupValue.charAt(0) == '"' && groupValue.charAt(jsonStringLength) ? groupValue?.substring(1, jsonStringLength) : groupValue
            List<String> splitJson = jsonString?.split(",")
            splitJson.each {
                List<String> keyValue = it?.trim()?.split(":")
                if (keyValue[0]?.toString()?.trim().equalsIgnoreCase(key)) {
                    threshold = SignalUtil.getDoubleFromString(keyValue[1])
                }
            }
        } else {
            threshold = null
        }
        threshold
    }
    def allSubGroupThresholdValue(RuleDataDTO ruleDataDTO, def alert, String thresholdValue, String value, boolean isCurrentAlertRule) {
        def threshold
        String groupValue = isCurrentAlertRule ? alert.getProperty(value) : ruleDataDTO.previousAggAlert?.getProperty(value)
        if (StringUtils.isNotBlank(groupValue)) {
            Map keyValueMap = JSON.parse(groupValue)
            keyValueMap?.each { subGroup, columnDataMap ->
                columnDataMap.each { key, val ->
                    threshold = SignalUtil.getDoubleFromString(val.toString())
                }
            }
        } else {
            threshold = null
        }
        threshold
    }

    /**
     * Method return the Map of the threshold values for the given expressionObj
     * @param ruleDataDTO ,expressionObj
     * @return Map of the threshold values
     */
    private Map<Object, Object> getAlgoThresholdValue(RuleDataDTO ruleDataDTO, Map expressionObj) {

        boolean isContainThresholdValue = false
        String thresholdValue = null
        boolean isCurrentAlertRule = true
        boolean subGroupNotExist = false
        List thresholds  = expressionObj.threshold.toString().split('-')
        List nonThresholdFields = ["LISTEDNESS","DME","IME","SPECIAL_MONITORING","STOP_LIST","NEW_EVENT","PREVIOUS_CATEGORY","ALL_CATEGORY","EVDAS_LISTEDNESS","sdrEvdas","changesEvdas","dmeImeEvdas","sdrPaedEvdas","sdrGeratrEvdas","trendType","trendFlag","positiveRechallenge"]
        if(thresholds[0].isDouble() || expressionObj.attribute in nonThresholdFields || expressionObj.threshold == "null" || expressionObj.threshold == null){
            // If Threshold Attribute is Double or Specific String Fields which require direct matching and do not need to fetch further value wil come here
            isCurrentAlertRule = false
            isContainThresholdValue = false
        }else {
            if (ruleDataDTO.dataSource != Constants.DataSource.PVA) {
                // Threshold attribute is configured in rule such that value can be directly fetched from ruleDataDTO using key thresholds[0]
                if (thresholds.size() == 2 && thresholds[1].equals('Previous')) {
                    isCurrentAlertRule = false
                }
                isContainThresholdValue = true
                thresholdValue = thresholds[0]
            } else {
                if (thresholds.size() == 2 && thresholds[1].equals('Previous')) {
                    isCurrentAlertRule = false
                }
                if (!isContainThresholdValue && !thresholds[0]?.isDouble()) {
                    isContainThresholdValue = true
                    thresholdValue = thresholds[0]
                }

            }
        }
        return [isContainThresholdValue : isContainThresholdValue ,thresholdValue :thresholdValue,isCurrentAlertRule:isCurrentAlertRule,subGroupNotExist:subGroupNotExist]
    }

    String getPrefix(String prefix) {
        prefix + "│   "
    }

    /**
     * This determines the statistics calculation flag. The criteria for that should be
     * EBGM  + PRR - 3
     * EBGM = 1
     * PRR  = 2
     * @return
     */
    def getConfigurationType(String dataSource) {

        def calculateFlag = 2
        if (dataSource == Constants.DataSource.FAERS) {

            def prr = Holders.config.statistics.faers.enable.prr
            def ror = Holders.config.statistics.faers.enable.ror
            def ebgm = Holders.config.statistics.faers.enable.ebgm

            if (ebgm && (prr || ror)) {
                calculateFlag = 3
            }

            if (ebgm && !prr && !ror) {
                calculateFlag = 1
            }
        } else if (dataSource == Constants.DataSource.VAERS) {

            def prr = Holders.config.statistics.vaers.enable.prr
            def ror = Holders.config.statistics.vaers.enable.ror
            def ebgm = Holders.config.statistics.vaers.enable.ebgm

            if (ebgm && (prr || ror)) {
                calculateFlag = 3
            }

            if (ebgm && !prr && !ror) {
                calculateFlag = 1
            }
        } else if (dataSource == Constants.DataSource.VIGIBASE) {

            def prr = Holders.config.statistics.vigibase.enable.prr
            def ror = Holders.config.statistics.vigibase.enable.ror
            def ebgm = Holders.config.statistics.vigibase.enable.ebgm

            if (ebgm && (prr || ror)) {
                calculateFlag = 3
            }

            if (ebgm && !prr && !ror) {
                calculateFlag = 1
            }
        } else if (dataSource == Constants.DataSource.JADER) {

            def prr = Holders.config.statistics.jader.enable.prr
            def ror = Holders.config.statistics.jader.enable.ror
            def ebgm = Holders.config.statistics.jader.enable.ebgm

            if (ebgm && (prr || ror)) {
                calculateFlag = 3
            }

            if (ebgm && !prr && !ror) {
                calculateFlag = 1
            }
        } else {

            def prr = Holders.config.statistics.enable.prr
            def ror = Holders.config.statistics.enable.ror
            def ebgm = Holders.config.statistics.enable.ebgm

            if (ebgm && (prr || ror)) {
                calculateFlag = 3
            }

            if (ebgm && !prr && !ror) {
                calculateFlag = 1
            }
        }
        return calculateFlag
    }

    ResponseDTO saveGlobalRule(BusinessConfiguration businessConfiguration, String ruleName, String dataSource, String description) {
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        Boolean isEditMode = businessConfiguration.id ? true : false
        Boolean isGlobalRule = true
        if(!isOtherRuleEnabled(false, dataSource)) {
            int dataSourceCount
            dataSourceCount = BusinessConfiguration.countByDataSourceAndIsGlobalRule(dataSource, isGlobalRule)

            try {
                //This check is for updating the Global Rule
                if (isEditMode) {
                    dataSourceCount--
                }
                businessConfiguration.ruleName = ruleName
                businessConfiguration.isGlobalRule = isGlobalRule
                businessConfiguration.description = description
                businessConfiguration.enabled = true
                if (!isEditMode && dataSource) {
                    businessConfiguration.dataSource = dataSource
                }

                //This check will test whether the ruleName already exists or not.
                if (businessConfigurationNameCheck(isEditMode, businessConfiguration?.id, ruleName)) {
                    responseDTO.message = messageSource.getMessage("app.label.business.configuration.name.error", null, Locale.default)
                    responseDTO.status = false
                } else {
                    if (!dataSourceCount) {
                        if (isEditMode) {
                            CRUDService.update(businessConfiguration)
                            responseDTO.message = messageSource.getMessage("app.label.business.configuration.update.sucess", null, Locale.default)
                        } else {
                            CRUDService.save(businessConfiguration)
                            responseDTO.message = messageSource.getMessage("app.label.business.configuration.create.sucess", null, Locale.default)
                        }
                        cacheService.prepareBusinessConfigCacheForSelectionType()
                    } else {
                        responseDTO.message = messageSource.getMessage("app.label.business.configuration.dataSource.error", null, Locale.default)
                        responseDTO.status = false
                    }
                }
            }
            catch (Exception ve) {
                ve.printStackTrace()
                responseDTO.status = false
            }
        }else{
            responseDTO.status = false
            responseDTO.message = messageSource.getMessage("app.business.configuration.enabled.warming.message",
                    [Constants.RuleType.PRODUCT ,Constants.RuleType.GLOBAL] as Object[], Locale.default)
        }
        responseDTO
    }

    Boolean businessConfigurationNameCheck(Boolean isEditMode, Long id, String ruleName) {
        int businessConfigCount
        if (isEditMode) {
            businessConfigCount = BusinessConfiguration.countByRuleNameAndIdNotEqual(ruleName, id)
        } else {
            businessConfigCount = BusinessConfiguration.countByRuleName(ruleName)
        }
        Boolean ruleExist = false
        if (businessConfigCount > 0) {
            ruleExist = true
        }
        ruleExist
    }

    Map fetchOperatorMap(String prefix, RuleDataDTO ruleDataDTO) {

        String text
        String pre = prefix
        def printOperator = { attr1, attr2, op, preText ->
            preText = getPrefix(preText)
            ruleDataDTO.logString.append("\n${preText}├── ${op}")
            pre = getPrefix(preText)
            ruleDataDTO.logString.append("\n${pre}├── ${attr1}")
            ruleDataDTO.logString.append("\n${pre}├──  ${attr2}")
            getPrefix(pre)
        }
        Map stringOperators = getStringOperatorsMap(ruleDataDTO, printOperator)
        return ([
                (Constants.Operators.EQUAL_TO)                                               : { attr1, attr2, preText, caseInSensitive=false ->
                    text = printOperator(attr1, attr2, "==", preText)
                    ruleDataDTO.logString.append("\n${text}└──${(attr1 == attr2)}")
                    caseInSensitive? attr1.equalsIgnoreCase(attr2):(attr1 == attr2)
                },
                (Constants.Operators.CONTENT_MATCH)                                          : { attr1, attr2, preText ->
                    text = printOperator(attr1, attr2, "==", preText)
                    ruleDataDTO.logString.append("\n${text}└──${(attr1 == attr2)}")
                    (attr1 == attr2)
                },
                (Constants.Operators.GREATER_THAN_OR_EQAUL_TO)                               : { attr1, attr2, preText ->
                    text = printOperator(attr1, attr2, ">=", preText)
                    ruleDataDTO.logString.append("\n${text}└──${(attr1 >= attr2)}")
                    ((attr1 >= attr2) && (attr1 != Integer.MAX_VALUE))
                },
                (Constants.Operators.GREATER_THAN)                                           : { attr1, attr2, preText ->
                    text = printOperator(attr1, attr2, ">", preText)
                    ruleDataDTO.logString.append("\n${text}└──${(attr1 > attr2)}")
                    ((attr1 > attr2) && (attr1 != Integer.MAX_VALUE))
                },
                (Constants.Operators.LESS_THAN_OR_EQUAL_TO)                                  : { attr1, attr2, preText ->
                    text = printOperator(attr1, attr2, "<=", preText)
                    ruleDataDTO.logString.append("\n${text}└──${(attr1 <= attr2)}")
                    (attr1 <= attr2)
                },
                (Constants.Operators.LESS_THAN)                                              : { attr1, attr2, preText ->
                    text = printOperator(attr1, attr2, "<", preText)
                    ruleDataDTO.logString.append("\n${text}└──${(attr1 < attr2)}")
                    (attr1 < attr2)
                },
                (Constants.Operators.IS_EMPTY)                                               : { attr1, attr2, preText ->
                    text = printOperator(attr1, attr2, Constants.Operators.IS_EMPTY, preText)
                    ruleDataDTO.logString.append("\n${text}└──${(attr1 == null || attr1 == "") || attr1 == '-'}")
                    (attr1 == null || attr1 == "" || attr1 == "-")
                },
                (Constants.Operators.IS_NOT_EMPTY)                                           : { attr1, attr2, preText ->
                    text = printOperator(attr1, attr2, Constants.Operators.IS_NOT_EMPTY, preText)
                    ruleDataDTO.logString.append("\n${text}└──${(attr1 != null && attr1 != "" && attr1 != '-')}")
                    (attr1 != null && attr1 != "" && attr1 != "-")
                },
                (Constants.Operators.IS_EMPTY_COUNTS)                                               : { attr1, attr2, preText ->
                    text = printOperator(attr1, attr2, Constants.Operators.IS_EMPTY, preText)
                    ruleDataDTO.logString.append("\n${text}└──${(attr1 == null || attr1 == "") || attr1 == '-' || attr1 == -1}")
                    (attr1 == null || attr1 == "" || attr1 == "-" || attr1 == -1)
                },
                (Constants.Operators.IS_NOT_EMPTY_COUNTS)                                           : { attr1, attr2, preText ->
                    text = printOperator(attr1, attr2, Constants.Operators.IS_NOT_EMPTY, preText)
                    ruleDataDTO.logString.append("\n${text}└──${(attr1 != null && attr1 != "" && attr1 != '-' && attr1 != -1)}")
                    (attr1 != null && attr1 != "" && attr1 != "-"  && attr1 != -1)
                },
                (Constants.Operators.NOT_EQUAL_TO)                                               : { attr1, attr2, preText, caseInSensitive=false ->
                    text = printOperator(attr1, attr2, "!=", preText)
                    ruleDataDTO.logString.append("\n${text}└──${(attr1 != attr2)}")
                    caseInSensitive? (!attr1.equalsIgnoreCase(attr2)):(attr1 != attr2)
                }
        ] + stringOperators)

    }

    Map getStringOperatorsMap(RuleDataDTO ruleDataDTO, Closure printOperator) {
        String text
        return [
                (Constants.Operators.END_WITH)           : { attr1, attr2, preText, caseInSensitive=false ->
                    text = printOperator(attr1, attr2, Constants.Operators.END_WITH, preText)
                    ruleDataDTO.logString.append("\n${text}└──${(attr1?.endsWith(attr2))}")
                    caseInSensitive? (attr1?.toLowerCase().endsWith(attr2?.toLowerCase())):(attr1?.endsWith(attr2))
                },
                (Constants.Operators.CONTAINS)           : { attr1, attr2, preText, caseInSensitive=false ->
                    text = printOperator(attr1, attr2, Constants.Operators.CONTAINS, preText)
                    ruleDataDTO.logString.append("\n${text}└──${(attr1?.contains(attr2))}")
                    caseInSensitive? (attr1?.toLowerCase().contains(attr2?.toLowerCase())):(attr1?.contains(attr2))
                },
                (Constants.Operators.DOES_NOT_CONTAIN)   : { attr1, attr2, preText, caseInSensitive=false ->
                    text = printOperator(attr1, attr2, Constants.Operators.DOES_NOT_CONTAIN, preText)
                    ruleDataDTO.logString.append("\n${text}└──${!(attr1?.contains(attr2))}")
                    caseInSensitive? !(attr1?.toLowerCase().contains(attr2?.toLowerCase())):(!attr1?.contains(attr2))
                },
                (Constants.Operators.DOES_NOT_START_WITH): { attr1, attr2, preText, caseInSensitive=false ->
                    text = printOperator(attr1, attr2, Constants.Operators.DOES_NOT_START_WITH, preText)
                    ruleDataDTO.logString.append("\n${text}└──${!(attr1?.startsWith(attr2))}")
                    caseInSensitive? !(attr1?.toLowerCase().startsWith(attr2?.toLowerCase())):(!attr1?.startsWith(attr2))
                },
                (Constants.Operators.DOES_NOT_END_WITH)  : { attr1, attr2, preText, caseInSensitive=false ->
                    text = printOperator(attr1, attr2, Constants.Operators.DOES_NOT_END_WITH, preText)
                    ruleDataDTO.logString.append("\n${text}└──${!(attr1?.endsWith(attr2))}")
                    caseInSensitive? !(attr1?.toLowerCase().endsWith(attr2?.toLowerCase())):(!attr1?.endsWith(attr2))
                }

        ]
    }

    Map getPreviousPeriodDataOfAggAlert(AggregateCaseAlert aggregateCaseAlert, Map<String, AggregateCaseAlert> previousAlertMap) {
        Map result = [:]

        AggregateCaseAlert prevAggregateCaseAlert = previousAlertMap.get(aggregateCaseAlert.productName + "-" + aggregateCaseAlert.pt)

        if (prevAggregateCaseAlert) {
            result = [
                    id                                                        : prevAggregateCaseAlert?.id,
                    (Constants.BusinessConfigAttributes.PRR_SCORE)        : prevAggregateCaseAlert.prrValue,
                    (Constants.BusinessConfigAttributes.PRRLCI_SCORE)      : prevAggregateCaseAlert.prrLCI,
                    (Constants.BusinessConfigAttributes.PRRUCI_SCORE)      : prevAggregateCaseAlert.prrUCI,
                    (Constants.BusinessConfigAttributes.ROR_SCORE)        : prevAggregateCaseAlert.rorValue,
                    (Constants.BusinessConfigAttributes.RORLCI_SCORE)      : prevAggregateCaseAlert.rorLCI,
                    (Constants.BusinessConfigAttributes.RORUCI_SCORE)      : prevAggregateCaseAlert.rorUCI,
                    (Constants.BusinessConfigAttributes.EBGM_SCORE)       : prevAggregateCaseAlert.ebgm,
                    (Constants.BusinessConfigAttributes.EB05_SCORE)       : prevAggregateCaseAlert.eb05,
                    (Constants.BusinessConfigAttributes.EB95_SCORE)       : prevAggregateCaseAlert.eb95,
                    (Constants.BusinessConfigAttributes.CHI_SQUARE)       : prevAggregateCaseAlert.chiSquare,


                    (Constants.BusinessConfigAttributes.NEW_COUNT)        : prevAggregateCaseAlert.newCount,
                    (Constants.BusinessConfigAttributes.CUMM_COUNT)        : prevAggregateCaseAlert.cummCount,
                    (Constants.BusinessConfigAttributes.NEW_SPON_COUNT)     : prevAggregateCaseAlert.newSponCount,
                    (Constants.BusinessConfigAttributes.CUM_SPON_COUNT)     : prevAggregateCaseAlert.cumSponCount,
                    (Constants.BusinessConfigAttributes.NEW_SERIOUS_COUNT)  : prevAggregateCaseAlert.newSeriousCount,
                    (Constants.BusinessConfigAttributes.CUM_SERIOUS_COUNT)  : prevAggregateCaseAlert.cumSeriousCount,
                    (Constants.BusinessConfigAttributes.NEW_FATAL_COUNT)    : prevAggregateCaseAlert.newFatalCount,
                    (Constants.BusinessConfigAttributes.CUM_FATAL_COUNT)    : prevAggregateCaseAlert.cumFatalCount,
                    (Constants.BusinessConfigAttributes.NEW_STUDY_COUNT)    : prevAggregateCaseAlert.newStudyCount,
                    (Constants.BusinessConfigAttributes.CUM_STUDY_COUNT)    : prevAggregateCaseAlert.cumStudyCount,
                    (Constants.BusinessConfigAttributes.E_VALUE)            : prevAggregateCaseAlert.eValue,
                    (Constants.BusinessConfigAttributes.RR_VALUE)           : prevAggregateCaseAlert.rrValue,
                    (Constants.BusinessConfigAttributes.NEW_GERIATRIC_COUNT): prevAggregateCaseAlert.newGeriatricCount,
                    (Constants.BusinessConfigAttributes.CUM_GERIATRIC_COUNT): prevAggregateCaseAlert.cumGeriatricCount,
                    (Constants.BusinessConfigAttributes.NEW_PEDIATRIC_COUNT) : prevAggregateCaseAlert.newPediatricCount,
                    (Constants.BusinessConfigAttributes.CUM_PEDIATRIC_COUNT) : prevAggregateCaseAlert.cummPediatricCount,
                    (Constants.BusinessConfigAttributes.NEW_NON_SERIOUS_COUNT) : prevAggregateCaseAlert.newNonSerious,
                    (Constants.BusinessConfigAttributes.CUM_NON_SERIOUS_COUNT) : prevAggregateCaseAlert.cumNonSerious,
                    (Constants.BusinessConfigAttributes.NEW_INTERACTING_COUNT) : prevAggregateCaseAlert.newInteractingCount,
                    (Constants.BusinessConfigAttributes.CUMM_INTERACTING_COUNT) : prevAggregateCaseAlert.cummInteractingCount,
                    (Constants.BusinessConfigAttributes.FREQ_PERIOD)                 : prevAggregateCaseAlert.freqPeriod,
                    (Constants.BusinessConfigAttributes.CUM_FREQ_PERIOD)             : prevAggregateCaseAlert.cumFreqPeriod,
                    (Constants.BusinessConfigAttributes.TREND_TYPE)                                               : prevAggregateCaseAlert.trendType
            ]
            StratificationScoreDTO stratificationScoreDTO = new StratificationScoreDTO()
            stratificationScoreDTO = stratificationScoreDTO.addStratificationScore(prevAggregateCaseAlert.eb05Gender?.toString(),'eb05')
            stratificationScoreDTO = stratificationScoreDTO.addStratificationScore(prevAggregateCaseAlert.eb95Gender?.toString(),'eb95')
            stratificationScoreDTO = stratificationScoreDTO.addStratificationScore(prevAggregateCaseAlert.eb05Age?.toString(),'eb05')
            stratificationScoreDTO = stratificationScoreDTO.addStratificationScore(prevAggregateCaseAlert.eb95Age?.toString(),'eb95')
            stratificationScoreDTO = stratificationScoreDTO.addStratificationScore(prevAggregateCaseAlert.ebgmAge?.toString(),'ebgm')
            stratificationScoreDTO = stratificationScoreDTO.addStratificationScore(prevAggregateCaseAlert.ebgmGender?.toString(),'ebgm')
            stratificationScoreDTO = stratificationScoreDTO.addAllSubGroupScore(aggregateCaseAlert.ebgmSubGroup?.toString(), 'EBGM')
            stratificationScoreDTO = stratificationScoreDTO.addAllSubGroupScore(aggregateCaseAlert.eb95SubGroup?.toString(), 'EB95')
            stratificationScoreDTO = stratificationScoreDTO.addAllSubGroupScore(aggregateCaseAlert.eb05SubGroup?.toString(), 'EB05')
            stratificationScoreDTO = stratificationScoreDTO.addAllSubGroupScore(aggregateCaseAlert.rorSubGroup.toString(), 'ROR')
            stratificationScoreDTO = stratificationScoreDTO.addAllSubGroupScore(aggregateCaseAlert.rorLciSubGroup?.toString(), 'ROR_LCI')
            stratificationScoreDTO = stratificationScoreDTO.addAllSubGroupScore(aggregateCaseAlert.rorUciSubGroup?.toString(), 'ROR_UCI')
            stratificationScoreDTO = stratificationScoreDTO.addAllSubGroupScore(aggregateCaseAlert.prrSubGroup.toString(), 'PRR')
            stratificationScoreDTO = stratificationScoreDTO.addAllSubGroupScore(aggregateCaseAlert.prrLciSubGroup?.toString(), 'PRR_LCI')
            stratificationScoreDTO = stratificationScoreDTO.addAllSubGroupScore(aggregateCaseAlert.prrUciSubGroup?.toString(), 'PRR_UCI')
            stratificationScoreDTO = stratificationScoreDTO.addAllSubGroupScore(aggregateCaseAlert.chiSquareSubGroup?.toString(), 'CHI_SQUARE')
            stratificationScoreDTO = stratificationScoreDTO.addAllSubGroupScore(aggregateCaseAlert.rorRelSubGroup.toString(), 'ROR_R')
            stratificationScoreDTO = stratificationScoreDTO.addAllSubGroupScore(aggregateCaseAlert.rorLciRelSubGroup?.toString(), 'ROR_LCI_R')
            stratificationScoreDTO = stratificationScoreDTO.addAllSubGroupScore(aggregateCaseAlert.rorUciRelSubGroup?.toString(), 'ROR_UCI_R')

            result << stratificationScoreDTO.mapData()
            if(prevAggregateCaseAlert.newCountsJson) {
                NewCountScoreDTO newCountScoreDTO = new NewCountScoreDTO()
                newCountScoreDTO = newCountScoreDTO.addNewCountData(prevAggregateCaseAlert.newCountsJson)
                result << newCountScoreDTO.mapData()
            }
        }
        result
    }

    Map getPreviousPeriodDataOfEvdasAlert(def evdasAlert, Map<String, EvdasAlert> previousAlertMap) {
        Map result = [:]
        EvdasAlert prevEvdasAlert = previousAlertMap.get(evdasAlert.substance + "-" + evdasAlert.pt)
        if (prevEvdasAlert) {
            result = [
                    id                                                          : prevEvdasAlert?.id,
                    (Constants.BusinessConfigAttributesEvdas.ROR_EUROPE_EVDAS)       : SignalUtil.getDoubleFromString(prevEvdasAlert.europeRor),
                    (Constants.BusinessConfigAttributesEvdas.ROR_N_AMERICA_EVDAS)    : SignalUtil.getDoubleFromString(prevEvdasAlert.northAmericaRor),
                    (Constants.BusinessConfigAttributesEvdas.ROR_JAPAN_EVDAS)        : SignalUtil.getDoubleFromString(prevEvdasAlert.japanRor),
                    (Constants.BusinessConfigAttributesEvdas.ROR_ASIA_EVDAS)         : SignalUtil.getDoubleFromString(prevEvdasAlert.asiaRor),
                    (Constants.BusinessConfigAttributesEvdas.ROR_REST_EVDAS)         : SignalUtil.getDoubleFromString(prevEvdasAlert.restRor),
                    (Constants.BusinessConfigAttributesEvdas.ROR_ALL_EVDAS)          : SignalUtil.getDoubleFromString(prevEvdasAlert.allRor),
                    (Constants.BusinessConfigAttributesEvdas.RELTV_ROR_PAED_VS_OTHR) : SignalUtil.getDoubleFromString(prevEvdasAlert.ratioRorPaedVsOthers),
                    (Constants.BusinessConfigAttributesEvdas.EVDAS_SDR_PAED)         : SignalUtil.getDoubleFromString(prevEvdasAlert.sdrPaed),
                    (Constants.BusinessConfigAttributesEvdas.RELTV_ROR_GERTR_VS_OTHR): SignalUtil.getDoubleFromString(prevEvdasAlert.ratioRorGeriatrVsOthers),
                    (Constants.BusinessConfigAttributesEvdas.EVDAS_SDR_GERTR)        : SignalUtil.getDoubleFromString(prevEvdasAlert.sdrGeratr),

                    (Constants.BusinessConfigAttributesEvdas.NEW_EV_EVDAS)           : prevEvdasAlert.newEv,
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_EV_EVDAS)         : prevEvdasAlert.totalEv,
                    (Constants.BusinessConfigAttributesEvdas.NEW_EEA_EVDAS)          : SignalUtil.getIntegerFromString(prevEvdasAlert.newEea),
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_EEA_EVDAS)        : SignalUtil.getIntegerFromString(prevEvdasAlert.totEea),
                    (Constants.BusinessConfigAttributesEvdas.NEW_HCP_EVDAS)          : SignalUtil.getIntegerFromString(prevEvdasAlert.newHcp),
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_HCP_EVDAS)        : SignalUtil.getIntegerFromString(prevEvdasAlert.totHcp),
                    (Constants.BusinessConfigAttributesEvdas.NEW_SERIOUS_EVDAS)      : prevEvdasAlert.newSerious,
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_SERIOUS_EVDAS)    : prevEvdasAlert.totalSerious,
                    (Constants.BusinessConfigAttributesEvdas.NEW_OBS_EVDAS)          : SignalUtil.getIntegerFromString(prevEvdasAlert.newObs),
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_OBS_EVDAS)        : SignalUtil.getIntegerFromString(prevEvdasAlert.totObs),
                    (Constants.BusinessConfigAttributesEvdas.NEW_FATAL_EVDAS)        : prevEvdasAlert.newFatal,
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_FATAL_EVDAS)      : prevEvdasAlert.totalFatal,
                    (Constants.BusinessConfigAttributesEvdas.NEW_MED_ERR_EVDAS)      : SignalUtil.getIntegerFromString(prevEvdasAlert.newMedErr),
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_MED_ERR_EVDAS)    : SignalUtil.getIntegerFromString(prevEvdasAlert.totMedErr),
                    (Constants.BusinessConfigAttributesEvdas.NEW_PLUS_RC_EVDAS)      : SignalUtil.getIntegerFromString(prevEvdasAlert.newRc),
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_PLUS_RC_EVDAS)    : SignalUtil.getIntegerFromString(prevEvdasAlert.totRc),
                    (Constants.BusinessConfigAttributesEvdas.NEW_LITERATURE_EVDAS)   : SignalUtil.getIntegerFromString(prevEvdasAlert.newLit),
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_LITERATURE_EVDAS) : SignalUtil.getIntegerFromString(prevEvdasAlert.totalLit),
                    (Constants.BusinessConfigAttributesEvdas.NEW_PAED_EVDAS)         : SignalUtil.getIntegerFromString(prevEvdasAlert.newPaed),
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_PAED_EVDAS)       : SignalUtil.getIntegerFromString(prevEvdasAlert.totPaed),
                    (Constants.BusinessConfigAttributesEvdas.NEW_GERIAT_EVDAS)       : SignalUtil.getIntegerFromString(prevEvdasAlert.newGeria),
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_GERIAT_EVDAS)     : SignalUtil.getIntegerFromString(prevEvdasAlert.totGeria),
                    (Constants.BusinessConfigAttributesEvdas.NEW_SPON_EVDAS)         : SignalUtil.getIntegerFromString(prevEvdasAlert.newSpont),
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_SPON_EVDAS)       : SignalUtil.getIntegerFromString(prevEvdasAlert.totSpont),
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_SPON_EUROPE)      : SignalUtil.getIntegerFromString(prevEvdasAlert.totSpontEurope),
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_SPON_N_AMERICA)   : SignalUtil.getIntegerFromString(prevEvdasAlert.totSpontNAmerica),
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_SPON_JAPAN)       : SignalUtil.getIntegerFromString(prevEvdasAlert.totSpontJapan),
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_SPON_ASIA)        : SignalUtil.getIntegerFromString(prevEvdasAlert.totSpontAsia),
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_SPON_REST)        : SignalUtil.getIntegerFromString(prevEvdasAlert.totSpontRest)
            ]
        }
        result
    }

    @Transactional(propagation = Propagation.NEVER)
    List<LinkedHashMap<String, Object>> changeModifiedByUserNameToFullName(List<LinkedHashMap<String, Object>> ruleInformationDtos) {
        for (Map businessConfigMap : ruleInformationDtos) {
            LinkedHashSet<RuleInformation> ruleInformation = businessConfigMap.get("ruleInformations")
            if(!ruleInformation.isEmpty()) {
                for (RuleInformation ruleInfo : ruleInformation) {
                    User user = cacheService.getUserByUserNameIlike(ruleInfo.modifiedBy)
                    ruleInfo.setModifiedBy(user ? user.fullName : ruleInfo.modifiedBy)
                }
            }
        }
    }

    List addSubGroupFields(String dataSource) {
        List fields = []
        List subGroup
        if (dataSource.equalsIgnoreCase(com.rxlogix.Constants.DataSource.PVA)) {
            subGroup = cacheService.getSubGroupColumns()?.flatten()
        } else if (dataSource.equalsIgnoreCase(com.rxlogix.Constants.DataSource.FAERS)) {
            subGroup = cacheService.getSubGroupColumnFaers()?.flatten()
        }
        Map labelConfig = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', null, null, null).collectEntries {
            b -> [b.name, b.display]
        }
        List subGroupValue = grailsApplication.config.businessConfiguration.attributes.aggregate.subGroup as List
        subGroupValue.each { def sub ->
            subGroup.each {
                String value = sub.value + '_' + it.value
                String key
                if (dataSource.equalsIgnoreCase(com.rxlogix.Constants.DataSource.FAERS)) {
                    key = sub.key + it.value + 'Faers'
                }else{
                    key = sub.key + it.value
                }
                String text = labelConfig.get(key)
                fields << [type: 'ALGORITHM', text: text, value: StringUtils.upperCase(value)]
             }
        }
        if (dataSource.equalsIgnoreCase(com.rxlogix.Constants.DataSource.PVA)) {
            Map<String, List<String>> getAllOtherSubGroupColumnsListMap = cacheService.getAllOtherSubGroupColumnsListMap(Constants.DataSource.PVA)
            Map<String,List<String>> relativeSubGroupMap = cacheService.getRelativeSubGroupColumnsListMap(Constants.DataSource.PVA)
            getAllOtherSubGroupColumnsListMap?.each { subGrp, columns ->
                columns?.each { it ->
                    String value = subGrp?.replaceAll('-', '_') + '_' + it
                    String key = cacheService.toCamelCase(subGrp.toLowerCase())  + it.value
                    String text = labelConfig.get(key)


                    fields << [type: 'ALGORITHM', text: text, value: StringUtils.upperCase(value)]
                }
            }
            relativeSubGroupMap?.each { subGrp, columns ->
                columns?.each { it ->
                    String key = cacheService.toCamelCase(subGrp.toLowerCase()) +"el" + it.value
                    String value = subGrp?.replaceAll('-', '_') + '_' + it
                    String text = labelConfig.get(key)

                    fields << [type: 'ALGORITHM', text: text, value: StringUtils.upperCase(value)]
                }
            }

        }
        fields
    }

    /**
     * Method return the Map of the SelectBoxValues for the given business config id
     * @param business config id
     * @return Map of the SelectBoxValues
     */
    Map getSelectBoxValues(BusinessConfiguration businessConfiguration) {
        def keyValueMap = alertFieldService.fetchLabelConfigMap(Constants.SystemPrecheck.SAFETY, "")
        def attributeMap
        List obj = []

        switch (businessConfiguration?.dataSource) {
            case Constants.DataSource.EUDRA:
                attributeMap = grailsApplication.config.businessConfiguration.attributes.evdas.clone() as Map
                break
            case Constants.DataSource.VAERS:
                attributeMap = grailsApplication.config.businessConfiguration.attributes.vaers.clone()
                keyValueMap = alertFieldService.fetchLabelConfigMap(Constants.SystemPrecheck.VAERS, "Vaers")
                break
            case Constants.DataSource.VIGIBASE:
                attributeMap = grailsApplication.config.businessConfiguration.attributes.vigibase.clone()
                keyValueMap = alertFieldService.fetchLabelConfigMap(Constants.SystemPrecheck.VIGIBASE, "Vigibase")
                break
            case Constants.DataSource.FAERS:
                attributeMap = grailsApplication.config.businessConfiguration.attributes.faers.clone()
                keyValueMap = alertFieldService.fetchLabelConfigMap(Constants.SystemPrecheck.FAERS, "Faers")
                break
            default:
                attributeMap = grailsApplication.config.businessConfiguration.attributes.aggregate.clone()
        }

        List fixedLabelFields = ["LISTEDNESS", "DME", "IME", "SPECIAL_MONITORING", "STOP_LIST", "NEW_EVENT", "PREVIOUS_CATEGORY", "ALL_CATEGORY"]
        List algoList = attributeMap.algorithm.clone() as List
        List countList = attributeMap.counts.clone() as List
        List newCountList = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', true, true, null).findAll{it.type == "count"}.collect{it.name}
        if (!Holders?.config?.statistics?.enable?.dss) {
            algoList.removeIf { it.key == "pecImpNumHigh" }
        }
        if (businessConfiguration?.dataSource == Constants.DataSource.EUDRA) {
            algoList.each { algo ->
                obj << [type: 'ALGORITHM', text: algo.value, value: algo.key]
            }
            addEvdasThresholdFieldValues(attributeMap, obj)
            countList.each { count ->
                obj << [type: 'COUNTS', text: count.value, value: count.key]
            }
        } else {
            algoList.each { algo ->
                def label = (algo.key in fixedLabelFields) ? algo.value : keyValueMap.get(algo.key)
                if (label) {
                    obj << [type: 'ALGORITHM', text: label, value: algo.key]
                }
            }
            obj.addAll(addSubGroupFields(businessConfiguration?.dataSource))
            if (businessConfiguration?.dataSource == Constants.DataSource.PVA) {
                queryService.getQueryListForBusinessConfiguration().each { superQuery ->
                    obj << [type: 'QUERY_CRITERIA', text: superQuery.name, value: superQuery.id]
                }
            }
            addThresholdFieldValues(keyValueMap, attributeMap, obj, businessConfiguration?.dataSource,newCountList)
            countList.each { count ->
                String label = keyValueMap.get(count.key)
                if (label) {
                    obj << [type: 'COUNTS', text: label, value: count.key]
                }
            }
            newCountList.each{ key ->
                String secondaryKey = key.replace("new", "cum")
                obj << [type: 'COUNTS', text: keyValueMap.get(key), value: key]
                obj << [type: 'COUNTS', text: keyValueMap.get(secondaryKey), value: secondaryKey]
            }
        }
        // Adding review states
        Disposition.list()*.displayName.each { reviewState ->
            obj << [type: 'REVIEW_STATE', text: reviewState, value: reviewState]
            obj << [type: 'SIGNAL_REVIEW_STATE', text: reviewState, value: reviewState]
        }
        // Remove null text entries
        obj.removeAll { it?.text == null }
        // Sorting and returning the result
        [categories: obj.sort { a, b -> a.text?.trim() <=> b.text?.trim() }, formatOptions: attributeMap.formatOptions, textFields: attributeMap.textType, booleanFields: attributeMap.booleanType]
    }

    Map fetchSelectBoxValuesJader(BusinessConfiguration businessConfiguration){
        String selectedDatasource = Constants.DataSource.JADER
        List jaderColumnList = alertFieldService.getJaderColumnList(selectedDatasource,false).findAll{it.enabled == true}
        Map jaderConfigDisplay = [:]
        jaderColumnList.each{ it ->
            if(it.secondaryName != null &&  it.display.split("/").size() > 1){
                jaderConfigDisplay.put(it.name, it.display.split("/")[0])
                jaderConfigDisplay.put(it.secondaryName, it.display.split("/")[1])
            }else{
                jaderConfigDisplay.put(it.name, it.display)
            }
        }
        List jaderEnabledFields = jaderColumnList.collect{it.name}
        Map attributeMap = grailsApplication.config.businessConfiguration.attributes.jader.clone()
        List algoList = attributeMap.algorithm.clone() as List
        List reviewStateList = Disposition.list()*.displayName
        List formatOptions = attributeMap.formatOptions
        List textFields = attributeMap.textType
        List booleanFields = attributeMap.booleanType
        List obj = []
        algoList.each {
            if(it.key in [Constants.BusinessConfigAttributes.PREVIOUS_CATEGORY, Constants.BusinessConfigAttributes.ALL_CATEGORY, Constants.BusinessConfigAttributes.LISTEDNESS]){
                obj << [type: 'ALGORITHM', text: it.value, value: it.key]
            }else {
                obj << [type: 'ALGORITHM', text: jaderConfigDisplay.get(it.key), value: it.key]
            }
        }
        addThresholdFieldValuesJader(jaderConfigDisplay,attributeMap, obj, businessConfiguration?.dataSource)
        List countList = attributeMap.counts as List
        countList.each {
            obj << [type: 'COUNTS', text:jaderConfigDisplay.get(it.key), value: it.key]
        }
        reviewStateList.each {
            obj << [type: 'REVIEW_STATE', text: it, value: it]
        }
        reviewStateList.each {
            obj << [type: 'SIGNAL_REVIEW_STATE', text: it, value: it]
        }
        obj.removeAll{it?.text == null}
        [categories: obj.sort { a, b -> a.text?.trim() <=> b.text?.trim() }, formatOptions: formatOptions, textFields: textFields, booleanFields: booleanFields]
    }

    private void addThresholdFieldValues(Map labelConfig, Map attributeMap, List obj, String dataSource,List newCountList) {

        List prrGroup = attributeMap.prrGroupValues as List
        List rorGroup = attributeMap.rorGroupValues as List
        List ebgmGroup = attributeMap.ebgmGroupValues as List
        List countList = attributeMap.counts as List
        prrGroup.each {
            obj << [type: 'prrType', text: labelConfig.get(it.key), value: it.key]
            obj << [type: 'prrType', text: Constants.BusinessConfigType.PREVIOUS_PERIOD + labelConfig.get(it.key), value: it.key + Constants.Commons.PREVIOUS]
        }
        rorGroup.each {
            obj << [type: 'rorType', text: labelConfig.get(it.key), value: it.key]
            obj << [type: 'rorType', text: Constants.BusinessConfigType.PREVIOUS_PERIOD + labelConfig.get(it.key), value: it.key + Constants.Commons.PREVIOUS]
        }
        ebgmGroup.each {
            obj << [type: 'ebgmType', text: labelConfig.get(it.key), value: it.key]
            obj << [type: 'ebgmType', text: Constants.BusinessConfigType.PREVIOUS_PERIOD + labelConfig.get(it.key), value: it.key + Constants.Commons.PREVIOUS]
        }
        List<Map> codeValues = pvsGlobalTagService.fetchTagsAndSubtags(true)
        List<Map> tags = pvsGlobalTagService.fetchTagsfromMart(codeValues)
        List tagTypes = tags.unique { it.text.toUpperCase() }
        tagTypes.each { obj << [type: 'tagType', text: it.text, value: it.text] }

        List subGrpFields = subGroupThresholdvalue(labelConfig,dataSource)
        obj.addAll(subGrpFields)

        obj << [type: 'rrvalueType', text: Constants.BusinessConfigType.PREVIOUS_PERIOD + labelConfig.get(Constants.BusinessConfigAttributes.RR_VALUE), value: Constants.BusinessConfigAttributes.RR_VALUE + Constants.Commons.PREVIOUS]
        obj << [type: 'evalueType', text: Constants.BusinessConfigType.PREVIOUS_PERIOD + labelConfig.get(Constants.BusinessConfigAttributes.E_VALUE), value: Constants.BusinessConfigAttributes.E_VALUE + Constants.Commons.PREVIOUS]
        List chiSquare = attributeMap.chiSquareValue as List
        chiSquare.each {
            obj << [type: com.rxlogix.Constants.BusinessConfigAttributes.CHI_SQUARE, text: labelConfig.get(it.key), value: it.key]
            obj << [type: com.rxlogix.Constants.BusinessConfigAttributes.CHI_SQUARE, text: Constants.BusinessConfigType.PREVIOUS_PERIOD + labelConfig.get(it.key), value: it.key + Constants.Commons.PREVIOUS]
        }

        List trendType = attributeMap.trendTypeValues as List
        trendType.each {
            obj << [type: 'trendType', text: it.value, value: it.key]
        }
        if (dataSource == Constants.DataSource.PVA) {
            List trendFlagValue = attributeMap.trendFlagValue as List
            trendFlagValue.each {
                obj << [type: Constants.BusinessConfigAttributes.TREND_FLAG, text: it.value, value: it.key]
            }

            List freqPeriodValue = attributeMap.freqPeriod as List
            freqPeriodValue.each {
                obj << [type: Constants.BusinessConfigAttributes.FREQ_PERIOD, text: it.value, value: it.key]
                obj << [type: Constants.BusinessConfigAttributes.FREQ_PERIOD, text: Constants.BusinessConfigType.PREVIOUS_PERIOD + it.value, value: it.key + Constants.Commons.PREVIOUS]
            }
            newCountList.each { key ->
                String secondaryKey = key.replace("new", "cum")
                String label = labelConfig.get(key)
                String secondaryLabel = labelConfig.get(secondaryKey)
                if(label) {
                    obj << [type: 'countValues', text: label, value: key]
                    obj << [type: 'countValues', text: secondaryLabel, value: secondaryKey]
                    obj << [type: 'countValues', text: Constants.BusinessConfigType.PREVIOUS_PERIOD + label, value: key + Constants.Commons.PREVIOUS]
                    obj << [type: 'countValues', text: Constants.BusinessConfigType.PREVIOUS_PERIOD + secondaryLabel, value: secondaryKey + Constants.Commons.PREVIOUS]
                }
            }

        }
        countList.each {
            String label = labelConfig.get(it.key)
            if(label && it.key != Constants.BusinessConfigAttributes.POSITIVE_RE_CHALLENGE) {
                obj << [type: 'countValues', text: label, value: it.key]
                obj << [type: 'countValues', text: Constants.BusinessConfigType.PREVIOUS_PERIOD + label, value: it.key + Constants.Commons.PREVIOUS]
            }
        }
    }

    private void addThresholdFieldValuesJader(Map jaderConfigDisplay, Map attributeMap, List obj, String dataSource) {

        List prrGroup = attributeMap.prrGroupValues as List
        List rorGroup = attributeMap.rorGroupValues as List
        List ebgmGroup = attributeMap.ebgmGroupValues as List
        List countList = attributeMap.counts as List
        prrGroup.each {
            if (it.key.toString().equals(Constants.BusinessConfigAttributes.PRR_SCORE_JADER)) {
                obj << [type: 'prrType', text: jaderConfigDisplay.get(it.key), value: it.value]
            } else if (it.key.equals(Constants.BusinessConfigAttributes.PRRLCI_SCORE_JADER)) {
                obj << [type: 'prrType', text: jaderConfigDisplay.get(Constants.BusinessConfigAttributes.PRRLCI_SCORE_JADER), value: it.key]
            } else if (it.key.equals(Constants.BusinessConfigAttributes.PRRUCI_SCORE_JADER)) {
                obj << [type: 'prrType', text: jaderConfigDisplay.get(Constants.BusinessConfigAttributes.PRRUCI_SCORE_JADER), value: it.key]
            }
        }

        rorGroup.each {
                if (it.key.toString().equals(Constants.BusinessConfigAttributes.ROR_SCORE_JADER)) {
                    obj << [type: 'rorType', text: jaderConfigDisplay.get(it.key), value: it.value]
                } else if (it.key.equals(Constants.BusinessConfigAttributes.RORLCI_SCORE_JADER)) {
                    obj << [type: 'rorType', text: jaderConfigDisplay.get(Constants.BusinessConfigAttributes.RORLCI_SCORE_JADER), value: it.key]
                } else if (it.key.equals(Constants.BusinessConfigAttributes.RORUCI_SCORE_JADER)) {
                    obj << [type: 'rorType', text: jaderConfigDisplay.get(Constants.BusinessConfigAttributes.RORUCI_SCORE_JADER), value: it.key]
                }
        }


        ebgmGroup.each {
            if (it.key.toString().equals(Constants.BusinessConfigAttributes.EBGM_SCORE_JADER)) {
                obj << [type: 'ebgmType', text: jaderConfigDisplay.get(it.key), value: StringUtils.upperCase(it.key)]
            } else if (it.key.equals(Constants.BusinessConfigAttributes.EB05_SCORE_JADER)) {
                obj << [type: 'ebgmType', text: jaderConfigDisplay.get(Constants.BusinessConfigAttributes.EB05_SCORE_JADER), value: it.key]
            } else if (it.key.equals(Constants.BusinessConfigAttributes.EB95_SCORE_JADER)) {
                obj << [type: 'ebgmType', text: jaderConfigDisplay.get(Constants.BusinessConfigAttributes.EB95_SCORE_JADER), value: it.key]
            }
        }

        List<Map> codeValues = pvsGlobalTagService.fetchTagsAndSubtags(true)
        List<Map> tags = pvsGlobalTagService.fetchTagsfromMart(codeValues)
        List tagTypes = tags.unique { it.text.toUpperCase() }
        tagTypes.each { obj << [type: 'tagType', text: it.text, value: it.text] }

        //Values with Previous Property
        prrGroup.each {
            if (it.key.toString().equals(Constants.BusinessConfigAttributes.PRR_SCORE_JADER)) {
                obj << [type: 'prrType', text: Constants.BusinessConfigType.PREVIOUS_PERIOD + jaderConfigDisplay.get(it.key), value: it.value + Constants.Commons.PREVIOUS]
            } else if (it.key.equals(Constants.BusinessConfigAttributes.PRRLCI_SCORE_JADER)) {
                obj << [type: 'prrType', text: Constants.BusinessConfigType.PREVIOUS_PERIOD + jaderConfigDisplay.get(Constants.BusinessConfigAttributes.PRRLCI_SCORE_JADER), value: it.key + Constants.Commons.PREVIOUS]

            } else if (it.key.equals(Constants.BusinessConfigAttributes.PRRUCI_SCORE_JADER)) {
                obj << [type: 'prrType', text: Constants.BusinessConfigType.PREVIOUS_PERIOD + jaderConfigDisplay.get(Constants.BusinessConfigAttributes.PRRUCI_SCORE_JADER), value: it.key + Constants.Commons.PREVIOUS]
            }
        }

        rorGroup.each {
                if (it.key.toString().equals(Constants.BusinessConfigAttributes.ROR_SCORE_JADER)) {
                    obj << [type: 'rorType', text: Constants.BusinessConfigType.PREVIOUS_PERIOD + jaderConfigDisplay.get(it.key), value: it.value + Constants.Commons.PREVIOUS]
                } else if (it.key.equals(Constants.BusinessConfigAttributes.RORLCI_SCORE_JADER)) {
                    obj << [type: 'rorType', text: Constants.BusinessConfigType.PREVIOUS_PERIOD + jaderConfigDisplay.get(Constants.BusinessConfigAttributes.RORLCI_SCORE_JADER), value: it.key + Constants.Commons.PREVIOUS]
                } else if (it.key.equals(Constants.BusinessConfigAttributes.RORUCI_SCORE_JADER)) {
                    obj << [type: 'rorType', text: Constants.BusinessConfigType.PREVIOUS_PERIOD + jaderConfigDisplay.get(Constants.BusinessConfigAttributes.RORUCI_SCORE_JADER), value: it.key + Constants.Commons.PREVIOUS]
                }
        }

        ebgmGroup.each {
            if (it.key.toString().equals(Constants.BusinessConfigAttributes.EBGM_SCORE_JADER)) {
                obj << [type: 'ebgmType', text: Constants.BusinessConfigType.PREVIOUS_PERIOD + jaderConfigDisplay.get(it.key), value: StringUtils.upperCase(it.key) + Constants.Commons.PREVIOUS]
            } else if (it.key.equals(Constants.BusinessConfigAttributes.EB05_SCORE_JADER)) {
                obj << [type: 'ebgmType', text: Constants.BusinessConfigType.PREVIOUS_PERIOD + jaderConfigDisplay.get(Constants.BusinessConfigAttributes.EB05_SCORE_JADER), value: it.key + Constants.Commons.PREVIOUS]
            } else if (it.key.equals(Constants.BusinessConfigAttributes.EB95_SCORE_JADER)) {
                obj << [type: 'ebgmType', text: Constants.BusinessConfigType.PREVIOUS_PERIOD + jaderConfigDisplay.get(Constants.BusinessConfigAttributes.EB95_SCORE_JADER), value: it.key + Constants.Commons.PREVIOUS]
            }
        }
        obj << [type: 'rrvalueType', text: Constants.BusinessConfigType.PREVIOUS_PERIOD + jaderConfigDisplay.get("rrValueJader"), value: Constants.BusinessConfigAttributes.RR_VALUE_JADER + Constants.Commons.PREVIOUS]
        obj << [type: 'evalueType', text: Constants.BusinessConfigType.PREVIOUS_PERIOD + jaderConfigDisplay.get("eValueJader"), value: Constants.BusinessConfigAttributes.E_VALUE_JADER + Constants.Commons.PREVIOUS]

        List chiSquare = attributeMap.chiSquareValue as List
        chiSquare.each {
            obj << [type: Constants.BusinessConfigAttributes.CHI_SQUARE_JADER, text: jaderConfigDisplay.get(it.key), value: it.key]
            obj << [type: Constants.BusinessConfigAttributes.CHI_SQUARE_JADER, text: Constants.BusinessConfigType.PREVIOUS_PERIOD + jaderConfigDisplay.get(it.key), value: it.key + Constants.Commons.PREVIOUS]
        }

        List trendType = attributeMap.trendTypeValues as List
        trendType.each {
            obj << [type: 'trendType', text: it.value, value: it.key]
        }
        countList.each {
            obj << [type: 'countValues', text: it.value, value: it.key]
            obj << [type: 'countValues', text: Constants.BusinessConfigType.PREVIOUS_PERIOD + it.value, value: it.key + Constants.Commons.PREVIOUS]

        }
    }

    List subGroupThresholdvalue(Map labelConfig,String dataSource) {
        List fields = []
        List subGroup
        List subGroupValue = grailsApplication.config.businessConfiguration.attributes.aggregate.subGroup as List
        if (dataSource.equalsIgnoreCase(Constants.DataSource.PVA)) {
            subGroup = cacheService.getSubGroupColumns()?.flatten()
        } else if (dataSource.equalsIgnoreCase(Constants.DataSource.FAERS)) {
            subGroup = cacheService.getSubGroupColumnFaers()?.flatten()
        } else if (dataSource.equalsIgnoreCase(Constants.DataSource.VAERS)) {
            subGroup = cacheService.getSubGroupColumnVaers()?.flatten()
        } else if (dataSource.equalsIgnoreCase(Constants.DataSource.VIGIBASE)) {
            subGroup = cacheService.getSubGroupColumnVigibase()?.flatten()
        }

        subGroupValue.each { def sub ->
            subGroup.each {
                String value = sub.key + '_' + it.value.toString().trim()
                String key
                if (dataSource.equalsIgnoreCase(Constants.DataSource.FAERS)) {
                    key = sub.key + it.value + 'Faers'
                }else{
                    key = sub.key + it.value
                }
                if(labelConfig.get(key)) {
                    fields << [type: 'ebgmType', text: labelConfig.get(key), value: StringUtils.upperCase(value)]
                }
            }
        }
        subGroupValue.each { def sub ->
            subGroup.each {
                String value = sub.key + '_' + it.value.toString().trim()
                String key
                if (dataSource.equalsIgnoreCase(Constants.DataSource.FAERS)) {
                    key = sub.key + it.value + 'Faers'
                }else{
                    key = sub.key + it.value
                }
                if(labelConfig.get(key)) {
                    fields << [type: 'ebgmType', text: Constants.BusinessConfigType.PREVIOUS_PERIOD + labelConfig.get(key), value: StringUtils.upperCase(value) + Constants.Commons.PREVIOUS]
                }
            }
        }
        if (dataSource.equalsIgnoreCase(Constants.DataSource.PVA)) {
            Map<String, List<String>> getAllOtherSubGroupColumnsListMap = cacheService.getAllOtherSubGroupColumnsListMap(Constants.DataSource.PVA)
            Map<String,List<String>> relativeSubGroupMap = cacheService.getRelativeSubGroupColumnsListMap(Constants.DataSource.PVA)
            getAllOtherSubGroupColumnsListMap?.each { subGrp, columns ->
                columns?.each { it ->
                    String value = subGrp?.replaceAll('-', '_')?.replaceAll(' ', '') + '_' + it
                    String type = cacheService.toCamelCase(subGrp?.toString()) + "Type"
                    String key = cacheService.toCamelCase(subGrp.toLowerCase()) + it.value
                    if(labelConfig.get(key)) {
                        if(type == "eb05Type" || type == "eb95Type"){
                            type = "ebgmType"
                        }
                        fields << [type: type, text: labelConfig.get(key), value: StringUtils.upperCase(value)]
                    }
                }
            }
            getAllOtherSubGroupColumnsListMap?.each { subGrp, columns ->
                columns?.each { it ->
                    String value = subGrp.replaceAll('-', '_')?.replaceAll(' ', '') + '_' + it
                    String type = cacheService.toCamelCase(subGrp?.toString()) + "Type"
                    String key = cacheService.toCamelCase(subGrp.toLowerCase()) + it.value
                    if(labelConfig.get(key)) {
                        if(type == "eb05Type" || type == "eb95Type"){
                            type = "ebgmType"
                        }
                    fields << [type: type, text: Constants.BusinessConfigType.PREVIOUS_PERIOD + labelConfig.get(key), value: StringUtils.upperCase(value) + Constants.Commons.PREVIOUS]
                    }
                }
            }
            relativeSubGroupMap?.each { subGrp, columns ->
                columns?.each { it ->
                    String value = subGrp?.replaceAll('-', '_')?.replaceAll(' ', '') + '_' + it
                    String type = cacheService.toCamelCase(subGrp?.toString()) + "Type"
                    String key = cacheService.toCamelCase(subGrp.toLowerCase()) +"el" + it.value
                    if(labelConfig.get(key)) {
                        fields << [type: type, text: labelConfig.get(key), value: StringUtils.upperCase(value)]
                    }
                }
            }
            relativeSubGroupMap?.each { subGrp, columns ->
                columns?.each { it ->
                    String value = subGrp.replaceAll('-', '_')?.replaceAll(' ', '') + '_' + it
                    String type = cacheService.toCamelCase(subGrp?.toString()) + "Type"
                    String key = cacheService.toCamelCase(subGrp.toLowerCase()) +"el" + it.value
                    if(labelConfig.get(key)) {
                        fields << [type: type, text: Constants.BusinessConfigType.PREVIOUS_PERIOD + labelConfig.get(key), value: StringUtils.upperCase(value) + Constants.Commons.PREVIOUS]
                    }
                }
            }
        }
        fields
    }

    private void addEvdasThresholdFieldValues(Map attributeMap, List obj) {
        List threshold = attributeMap.algorithmThreshold.threshold as List
        List countThreshold = attributeMap.counts as List
        List<Map> codeValues = pvsGlobalTagService.fetchTagsAndSubtags(true)
        List<Map> tags = pvsGlobalTagService.fetchTagsfromMart(codeValues)
        List tagTypes = tags.unique{it.text.toUpperCase()}
        tagTypes.each { obj << [type: 'tagType', text:  it.text, value: it.text] }

        threshold.each { obj << [type: 'rorType', text: it.value, value: it.key] }
        threshold.each {
            obj << [type: 'rorType', text: Constants.BusinessConfigType.PREVIOUS_PERIOD + it.value, value: it.key + Constants.Commons.PREVIOUS]
        }
        countThreshold.each { obj << [type: 'countValues', text: it.value, value: it.key] }
        countThreshold.each {
            obj << [type: 'countValues', text: Constants.BusinessConfigType.PREVIOUS_PERIOD + it.value, value: it.key + Constants.Commons.PREVIOUS]
        }
    }

    /**
     * Method return the boolean of the BusinessConfiguration for the given ruleTypeCheck
     * @param isGlobalRule
     * @return boolean of the BusinessConfiguration
     */
    boolean isOtherRuleEnabled(boolean ruleTypeCheck, String dataSource ) {
        boolean  result
        List businessConfiguration = BusinessConfiguration.findAllByIsGlobalRule(ruleTypeCheck)
        businessConfiguration.each {
            if (it.enabled && it?.dataSource?.equalsIgnoreCase(dataSource)) {
                result = true
            }
        }
        result
    }

    BusinessConfiguration getBusinessConfiguration(Long id){
       return BusinessConfiguration.get(id)
    }

    void migrateRuleInformationTags() {
        Sql sql = new Sql(dataSource)
        try {
            List ruleInformation = RuleInformation.createCriteria().list {
                projections {
                    property("tags")
                    property("id")
                }
                isNotNull("tags")
            }
            String updateQuery = ''
            ruleInformation.each { value ->
                List list = []
                Map tagsMap = JSON.parse(value[0])
                tagsMap.tags.each {
                    list.add([tagText: it, subTags: [], priority: 9999])
                }
                String tagJson = JsonOutput.toJson(list)
                String x = [tags: tagJson] as JSON
                updateQuery += 'UPDATE RULE_INFORMATION SET TAGS' + ' = \'' + x + '\' WHERE ID = ' + value[1] + ';'

            }
            String begin = "BEGIN "
            updateQuery = begin + updateQuery
            updateQuery += ' END;'
            sql.executeUpdate(updateQuery)
        }
        catch(Exception e){
            log.error(e.getMessage(), e)
        }
        finally{
            sql?.close()
        }

    }

    def getAllBussinessConfigurationsList() {
        String timezone = userService?.getCurrentUserPreference()?.timeZone
        BusinessConfiguration.list().collect {
            [
                    id              : it.id,
                    ruleName        : it.ruleName?.trim()?.replaceAll("\\s{2,}", " "),
                    ruleRank        : 1,
                    lastModified    : DateUtil.toDateStringWithTime(it.lastUpdated, timezone),
                    modifiedBy      : User.findByUsername(it.modifiedBy)?.fullName,
                    products        : it.isGlobalRule ? 'Global Rule' : getProductSelectionWithType(it)?: getGroupNameFieldFromJson(it.productGroupSelection),
                    enabled         : it.enabled,
                    description     : it.description ?: Constants.Commons.DASH_STRING,
                    isGlobalRule    : it.isGlobalRule,
                    dataSource      : it.dataSource,
                    ruleInformations: it.ruleInformations
            ]
        }

    }

    List getProductSelectionWithType(BusinessConfiguration businessConfig) {
        def jsonObj = null
        List dicList = PVDictionaryConfig.ProductConfig.views.collect {
            messageSource.getMessage(it.code, null, Locale.default)
        }
        Map productTypeMap = [:]
        dicList.eachWithIndex { value, index ->
            productTypeMap[index+1] = value
        }
        List allProductsListWithType = []
        if (businessConfig) {
            jsonObj = parseJsonString(businessConfig.productSelection)
            if (jsonObj) {
                jsonObj.each { x ->
                    allProductsListWithType << x.value.collect { it.name + ' (' + productTypeMap[x.key as Integer] + ') ' }
                }
            }
        }
        allProductsListWithType?.flatten()
    }

    void executeRulesForEvdasAlertIntegratedReview(AggregateCaseAlert aca, String alertProductSelection,
                                         String defaultDispositionValue, Map<String, AggregateCaseAlert> previousAlertMap,
                                                   Map previousAlertTags, Map allPreviousTags, List impEventList, Disposition initialDisp,StringBuffer allPECLogs) {

        StringBuilder logStringBuilder = new StringBuilder()
        logStringBuilder.append("\n********** Rule Processing Started for Evdas Alert : ${aca.name} ${aca.id}**********")
        RuleDataDTO ruleDataDTO = new RuleDataDTO()
        def previousEvdasAlert = previousAlertMap.get(aca.productName + "-" + aca.pt)
        ruleDataDTO.previousEvdasAlert = previousEvdasAlert?.populateEvdasColumnMap()
        ruleDataDTO.prevEvdasData = getPreviousPeriodDataIntegratedReview(ruleDataDTO.previousEvdasAlert, aca.id)
        if (ruleDataDTO.prevEvdasData) {
            ruleDataDTO.isFirstExecution = false
        }
        if (ruleDataDTO.prevEvdasData) {
            ruleDataDTO.previousTags = previousAlertTags.get(aca.productId + "-" + aca.ptCode + "-" + aca.smqCode) ?: []
            ruleDataDTO.allPrevTags = allPreviousTags.get(aca.productId + "-" + aca.ptCode + "-" + aca.smqCode) ?: []
        }
        Map impEventMap = fetchImpEventMap(aca, aca.pt, aca.productName, aca.productId , impEventList, true)
        ruleDataDTO.with {
            execConfigId = aca.executedAlertConfigurationId
            disposition = initialDisp?:aca.disposition
            id = aca.id
            ror = aca.getEvdasColumnValue("rorValueEvdas") as Double
            newEvEvdas = aca.getEvdasColumnValue("newEvEvdas") as Integer
            totalEvEvdas = aca.getEvdasColumnValue("totalEvEvdas") as Integer
            newFatalEvdas = aca.getEvdasColumnValue("newFatalEvdas") as Integer
            totalFatalEvdas = aca.getEvdasColumnValue("totalFatalEvdas") as Integer
            newSeriousEvdas = aca.getEvdasColumnValue("newSeriousEvdas") as Integer
            totalSeriousEvdas = aca.getEvdasColumnValue("totalSeriousEvdas") as Integer
            newLitEvdas = aca.getEvdasColumnValue("newLitEvdas") as Integer
            totalLitEvdas = aca.getEvdasColumnValue("totalLitEvdas") as Integer
            newPaedEvdas = aca.getEvdasColumnValue("newPaedEvdas") as Integer
            totalPaedEvdas = aca.getEvdasColumnValue("totPaedEvdas") as Integer
            newGeriatEvdas = aca.getEvdasColumnValue("newGeriaEvdas") as Integer
            totalGeriatEvdas = aca.getEvdasColumnValue("totGeriaEvdas") as Integer
            newEEAEvdas = aca.getEvdasColumnValue("newEeaEvdas") as Integer
            totEEAEvdas = aca.getEvdasColumnValue("totEeaEvdas") as Integer
            newHCPEvdas = aca.getEvdasColumnValue("newHcpEvdas") as Integer
            totHCPEvdas = aca.getEvdasColumnValue("totHcpEvdas") as Integer
            newObsEvdas = aca.getEvdasColumnValue("newObsEvdas") as Integer
            totObsEvdas = aca.getEvdasColumnValue("totObsEvdas") as Integer
            newMedErrEvdas = aca.getEvdasColumnValue("newMedErrEvdas") as Integer
            totMedErrEvdas = aca.getEvdasColumnValue("totMedErrEvdas") as Integer
            newPlusRCEvdas = aca.getEvdasColumnValue("newRcEvdas") as Integer
            totPlusRCEvdas = aca.getEvdasColumnValue("totRcEvdas") as Integer
            newSponEvdas = aca.getEvdasColumnValue("newSpontEvdas") as Integer
            totSponEvdas = aca.getEvdasColumnValue("totSpontEvdas") as Integer
            rorEuropeEvdas = aca.getEvdasColumnValue("europeRorEvdas") as Double
            rorNAmericaEvdas = aca.getEvdasColumnValue("northAmericaRorEvdas") as Double
            rorJapanEvdas = aca.getEvdasColumnValue("japanRorEvdas") as Double
            rorAsiaEvdas = aca.getEvdasColumnValue("asiaRorEvdas") as Double
            rorRestEvdas = aca.getEvdasColumnValue("restRorEvdas") as Double
            rorAllEvdas = aca.getEvdasColumnValue("allRorEvdas") as Double
            dataSource = Constants.DataSource.EUDRA
            alertType = AlertConfigType.AGGREGATE_CASE_ALERT
            isPEDetectedFirstTime = (aca.disposition?.value == defaultDispositionValue)
            lastReviewDuration = calculateLastReviewDurationForAggregateAlert(aca)
            products = aca.productName
            configProductSelection = "1"
            sdr = aca.getEvdasColumnValue("sdrEvdas")
            changes = aca.getEvdasColumnValue("changesEvdas")
            productSelection = alertProductSelection
            evdasListedness = aca.listed
            totSpontEurope = aca.getEvdasColumnValue("totSpontEuropeEvdas") as Integer
            totSpontNAmerica = aca.getEvdasColumnValue("totSpontNAmericaEvdas") as Integer
            totSpontJapan = aca.getEvdasColumnValue("totSpontJapanEvdas") as Integer
            totSpontAsia = aca.getEvdasColumnValue("totSpontAsiaEvdas") as Integer
            totSpontRest = aca.getEvdasColumnValue("totSpontRestEvdas") as Integer
            dmeIme = aca.getEvdasColumnValue("dmeImeEvdas")
            reltRorPadVsOhtr = aca.getEvdasColumnValue("ratioRorPaedVsOthersEvdas") as Double
            sdrPaed = aca.getEvdasColumnValue("sdrPaedEvdas")
            reltRorGrtVsOthr = aca.getEvdasColumnValue("ratioRorGeriatrVsOthersEvdas") as Double
            sdrGeratr = aca.getEvdasColumnValue("sdrGeratrEvdas")
            isNewEvent = aca.isNew
            isEvdasIntegratedReview = true
            logString = logStringBuilder
            productId = aca.productId
            isDME = impEventMap.isDME
            isIME = impEventMap.isIME
            isStopList = impEventMap.isStopList
            isSpecialMonitoring = impEventMap.isSpecialMonitoring
            isDMEProd = impEventMap.isDMEProd
            isIMEProd = impEventMap.isIMEProd
            isStopListProd = impEventMap.isStopListProd
            isSpecialMonitoringProd = impEventMap.isSpecialMonitoringProd
        }
        executeRules(ruleDataDTO, aca, initialDisp)
        allPECLogs.append(logStringBuilder)

    }
    Map getPreviousPeriodDataIntegratedReview(Map prevEvdasAlert,Long prevAlertId) {
        Map result = [:]
        if (prevEvdasAlert) {
            result = [
                    id                                                          : prevAlertId,
                    (Constants.BusinessConfigAttributesEvdas.ROR_EUROPE_EVDAS)       : SignalUtil.getDoubleFromString(prevEvdasAlert.europeRorEvdas),
                    (Constants.BusinessConfigAttributesEvdas.ROR_N_AMERICA_EVDAS)    : SignalUtil.getDoubleFromString(prevEvdasAlert.northAmericaRorEvdas),
                    (Constants.BusinessConfigAttributesEvdas.ROR_JAPAN_EVDAS)        : SignalUtil.getDoubleFromString(prevEvdasAlert.japanRorEvdas),
                    (Constants.BusinessConfigAttributesEvdas.ROR_ASIA_EVDAS)         : SignalUtil.getDoubleFromString(prevEvdasAlert.asiaRorEvdas),
                    (Constants.BusinessConfigAttributesEvdas.ROR_REST_EVDAS)         : SignalUtil.getDoubleFromString(prevEvdasAlert.restRorEvdas),
                    (Constants.BusinessConfigAttributesEvdas.ROR_ALL_EVDAS)          : SignalUtil.getDoubleFromString(prevEvdasAlert.allRorEvdas),
                    (Constants.BusinessConfigAttributesEvdas.RELTV_ROR_PAED_VS_OTHR) : SignalUtil.getDoubleFromString(prevEvdasAlert.ratioRorPaedVsOthersEvdas),
                    (Constants.BusinessConfigAttributesEvdas.EVDAS_SDR_PAED)         : SignalUtil.getDoubleFromString(prevEvdasAlert.sdrPaedEvdas),
                    (Constants.BusinessConfigAttributesEvdas.RELTV_ROR_GERTR_VS_OTHR): SignalUtil.getDoubleFromString(prevEvdasAlert.ratioRorGeriatrVsOthersEvdas),
                    (Constants.BusinessConfigAttributesEvdas.EVDAS_SDR_GERTR)        : SignalUtil.getDoubleFromString(prevEvdasAlert.sdrGeratrEvdas),

                    (Constants.BusinessConfigAttributesEvdas.NEW_EV_EVDAS)           : prevEvdasAlert.newEvEvdas,
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_EV_EVDAS)         : prevEvdasAlert.totalEvEvdas,
                    (Constants.BusinessConfigAttributesEvdas.NEW_EEA_EVDAS)          : SignalUtil.getIntegerFromString(prevEvdasAlert.newEeaEvdas),
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_EEA_EVDAS)        : SignalUtil.getIntegerFromString(prevEvdasAlert.totEeaEvdas),
                    (Constants.BusinessConfigAttributesEvdas.NEW_HCP_EVDAS)          : SignalUtil.getIntegerFromString(prevEvdasAlert.newHcpEvdas),
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_HCP_EVDAS)        : SignalUtil.getIntegerFromString(prevEvdasAlert.totHcpEvdas),
                    (Constants.BusinessConfigAttributesEvdas.NEW_SERIOUS_EVDAS)      : prevEvdasAlert.newSeriousEvdas,
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_SERIOUS_EVDAS)    : prevEvdasAlert.totalSeriousEvdas,
                    (Constants.BusinessConfigAttributesEvdas.NEW_OBS_EVDAS)          : SignalUtil.getIntegerFromString(prevEvdasAlert.newObsEvdas),
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_OBS_EVDAS)        : SignalUtil.getIntegerFromString(prevEvdasAlert.totObsEvdas),
                    (Constants.BusinessConfigAttributesEvdas.NEW_FATAL_EVDAS)        : prevEvdasAlert.newFatalEvdas,
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_FATAL_EVDAS)      : prevEvdasAlert.totalFatalEvdas,
                    (Constants.BusinessConfigAttributesEvdas.NEW_MED_ERR_EVDAS)      : SignalUtil.getIntegerFromString(prevEvdasAlert.newMedErrEvdas),
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_MED_ERR_EVDAS)    : SignalUtil.getIntegerFromString(prevEvdasAlert.totMedErrEvdas),
                    (Constants.BusinessConfigAttributesEvdas.NEW_PLUS_RC_EVDAS)      : SignalUtil.getIntegerFromString(prevEvdasAlert.newRcEvdas),
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_PLUS_RC_EVDAS)    : SignalUtil.getIntegerFromString(prevEvdasAlert.totRcEvdas),
                    (Constants.BusinessConfigAttributesEvdas.NEW_LITERATURE_EVDAS)   : SignalUtil.getIntegerFromString(prevEvdasAlert.newLitEvdas),
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_LITERATURE_EVDAS) : SignalUtil.getIntegerFromString(prevEvdasAlert.totalLitEvdas),
                    (Constants.BusinessConfigAttributesEvdas.NEW_PAED_EVDAS)         : SignalUtil.getIntegerFromString(prevEvdasAlert.newPaedEvdas),
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_PAED_EVDAS)       : SignalUtil.getIntegerFromString(prevEvdasAlert.totPaedEvdas),
                    (Constants.BusinessConfigAttributesEvdas.NEW_GERIAT_EVDAS)       : SignalUtil.getIntegerFromString(prevEvdasAlert.newGeriaEvdas),
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_GERIAT_EVDAS)     : SignalUtil.getIntegerFromString(prevEvdasAlert.totGeriaEvdas),
                    (Constants.BusinessConfigAttributesEvdas.NEW_SPON_EVDAS)         : SignalUtil.getIntegerFromString(prevEvdasAlert.newSpontEvdas),
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_SPON_EVDAS)       : SignalUtil.getIntegerFromString(prevEvdasAlert.totSpontEvdas),
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_SPON_EUROPE)      : SignalUtil.getIntegerFromString(prevEvdasAlert.totSpontEuropeEvdas),
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_SPON_N_AMERICA)   : SignalUtil.getIntegerFromString(prevEvdasAlert.totSpontNAmericaEvdas),
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_SPON_JAPAN)       : SignalUtil.getIntegerFromString(prevEvdasAlert.totSpontJapanEvdas),
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_SPON_ASIA)        : SignalUtil.getIntegerFromString(prevEvdasAlert.totSpontAsiaEvdas),
                    (Constants.BusinessConfigAttributesEvdas.TOTAL_SPON_REST)        : SignalUtil.getIntegerFromString(prevEvdasAlert.totSpontRestEvdas)
            ]
        }
        result
    }

    void executeRulesForFaersIntegratedReview(AggregateCaseAlert aggregateCaseAlert, String productDictionarySelection, String execProductSelection,
                                                 String defaultDispositionValue, Map<String, AggregateCaseAlert> previousAlertMap,
                                              Map previousAlertTags, Map allPreviousTags, List impEventList,Disposition initialDisp,StringBuffer allPECLogs) {

        StringBuilder logStringBuilder = new StringBuilder()
        logStringBuilder.append("\n********** Rule Processing Started for Integrated Review Faers Alert : ${aggregateCaseAlert?.name}**********")
        RuleDataDTO ruleDataDTO = new RuleDataDTO()
        List<ProductEventHistory> productEventHistoryList = getProductEventHistoryList(aggregateCaseAlert)
        def previousAggAlert = previousAlertMap.get(aggregateCaseAlert.productName + "-" + aggregateCaseAlert.pt)
        ruleDataDTO.previousAggAlert = previousAggAlert
        ruleDataDTO.prevAggData = getPreviousDataOfFaersIntegratedReview(ruleDataDTO.previousAggAlert, aggregateCaseAlert.id)
        if (ruleDataDTO.prevAggData) {
            ruleDataDTO.isFirstExecution = false
        }
        if (ruleDataDTO.prevAggData) {
            ruleDataDTO.previousTags = previousAlertTags.get(aggregateCaseAlert.productId + "-" + aggregateCaseAlert.ptCode + "-" + aggregateCaseAlert.smqCode) ?: []
            ruleDataDTO.allPrevTags = allPreviousTags.get(aggregateCaseAlert.productId + "-" + aggregateCaseAlert.ptCode + "-" + aggregateCaseAlert.smqCode) ?: []
        }

        int prevQuarter = DateUtil.quarter - 1
        Date startDateOfQuarter = DateUtil.getFirstDateOfQuarter(prevQuarter)
        Date endDateOfQuarter = DateUtil.getEndDateOfQuarter(prevQuarter)
        Map faersColumnMap = aggregateCaseAlert.populateFaersColumnMap()
        Map impEventMap = fetchImpEventMap(aggregateCaseAlert, aggregateCaseAlert.pt, aggregateCaseAlert.productName, aggregateCaseAlert.productId , impEventList, true)
        ruleDataDTO.with {
            execConfigId = aggregateCaseAlert.executedAlertConfigurationId
            disposition = initialDisp
            eb95Faers = faersColumnMap.eb95Faers
            eb05Faers = faersColumnMap.eb05Faers
            ebgmFaers = faersColumnMap.ebgmFaers
            newCountFaers = faersColumnMap.newCountFaers
            cumCountFaers = faersColumnMap.cummCountFaers
            newStudyCountFaers = faersColumnMap.newStudyCountFaers
            cumStudyCountFaers = faersColumnMap.cumStudyCountFaers
            newSponCountFaers = faersColumnMap.newSponCountFaers
            cumSponCountFaers = faersColumnMap.cumSponCountFaers
            newSeriousCountFaers = faersColumnMap.newSeriousCountFaers
            cumSeriousCountFaers = faersColumnMap.cumSeriousCountFaers
            newFatalCountFaers = faersColumnMap.newFatalCountFaers
            cumFatalCountFaers = faersColumnMap.cumFatalCountFaers
            newInteractingCountFaers = faersColumnMap.newInteractingCountFaers
            cumInteractingCountFaers = faersColumnMap.cummInteractingCountFaers
            newPediatricCountFaers = faersColumnMap.newPediatricCountFaers
            cumPediatricCountFaers = faersColumnMap.cummPediatricCountFaers
            newGeriatricCountFaers = faersColumnMap.newGeriatricCountFaers
            cumGeriatricCountFaers = faersColumnMap.cumGeriatricCountFaers
            products = aggregateCaseAlert.productName
            productSelection = execProductSelection
            prrValueFaers = faersColumnMap.prrValueFaers as Double
            prrLCIFaers = faersColumnMap.prrLCIFaers as Double
            prrUCIFaers = faersColumnMap.prrUCIFaers as Double
            rorValueFaers = faersColumnMap.rorValueFaers as Double
            rorLCIFaers = faersColumnMap.rorLCIFaers as Double
            rorUCIFaers = faersColumnMap.rorUCIFaers as Double
            ebgmFaers = faersColumnMap.ebgmFaers as Double
            eb05Faers = faersColumnMap.eb05Faers as Double
            eb95Faers = faersColumnMap.eb95Faers as Double
            eValueFaers = faersColumnMap.eValueFaers as Double
            rrValueFaers = faersColumnMap.rrValueFaers as Double
            dataSource = Constants.DataSource.FAERS
            prevEB95 = getPreviousPeriodEB95Value(productEventHistoryList, startDateOfQuarter, endDateOfQuarter)
            alertType = AlertConfigType.AGGREGATE_CASE_ALERT
            isPEDetectedFirstTime = (cacheService.getDispositionByValue(aggregateCaseAlert.dispositionId).value == defaultDispositionValue) as Boolean
            lastReviewDuration = calculateLastReviewDurationForAggregateAlert(aggregateCaseAlert)
            configProductSelection = productDictionarySelection
            listedness = aggregateCaseAlert.listed
            positiveRechallenge = faersColumnMap.positiveRechallengeFaers
            chiSquare = faersColumnMap.chiSquareFaers as Double
            isNewEvent = aggregateCaseAlert.isNew
            logString = logStringBuilder
            productId = aggregateCaseAlert.productId
            isDME = impEventMap.isDME
            isIME = impEventMap.isIME
            isStopList = impEventMap.isStopList
            isSpecialMonitoring = impEventMap.isSpecialMonitoring
            isDMEProd = impEventMap.isDMEProd
            isIMEProd = impEventMap.isIMEProd
            isStopListProd = impEventMap.isStopListProd
            isSpecialMonitoringProd = impEventMap.isSpecialMonitoringProd
            trendType = faersColumnMap.trendTypeFaers
        }
        ruleDataDTO = populateStratificationScoreFaers(aggregateCaseAlert, ruleDataDTO)
        executeRules(ruleDataDTO, aggregateCaseAlert, initialDisp)
        allPECLogs.append(logStringBuilder)
    }

    void executeRulesForVaersIntegratedReview(AggregateCaseAlert aggregateCaseAlert, String productDictionarySelection, String execProductSelection,
                                                 String defaultDispositionValue, Map<String, AggregateCaseAlert> previousAlertMap,
                                              Map previousAlertTags, Map allPreviousTags, Disposition initialDisp,StringBuffer allPECLogs) {

        StringBuilder logStringBuilder = new StringBuilder()
        logStringBuilder.append("\n********** Rule Processing Started for Integrated Review Vaers Alert : ${aggregateCaseAlert?.name}**********")
        RuleDataDTO ruleDataDTO = new RuleDataDTO()
        List<ProductEventHistory> productEventHistoryList = getProductEventHistoryList(aggregateCaseAlert)
        def previousAggAlert = previousAlertMap.get(aggregateCaseAlert.productName + "-" + aggregateCaseAlert.pt)
        ruleDataDTO.previousAggAlert = previousAggAlert
        ruleDataDTO.prevAggData = getPreviousDataOfVaersIntegratedReview(ruleDataDTO.previousAggAlert, aggregateCaseAlert.id)
        if (ruleDataDTO.prevAggData) {
            ruleDataDTO.isFirstExecution = false
        }
        if (ruleDataDTO.prevAggData) {
            ruleDataDTO.previousTags = previousAlertTags.get(aggregateCaseAlert.productId + "-" + aggregateCaseAlert.ptCode + "-" + aggregateCaseAlert.smqCode) ?: []
            ruleDataDTO.allPrevTags = allPreviousTags.get(aggregateCaseAlert.productId + "-" + aggregateCaseAlert.ptCode + "-" + aggregateCaseAlert.smqCode) ?: []
        }

        int prevQuarter = DateUtil.quarter - 1
        Date startDateOfQuarter = DateUtil.getFirstDateOfQuarter(prevQuarter)
        Date endDateOfQuarter = DateUtil.getEndDateOfQuarter(prevQuarter)
        Map vaersColumnMap = aggregateCaseAlert.populateVaersColumnMap()
        ruleDataDTO.with {
            execConfigId = aggregateCaseAlert.executedAlertConfigurationId
            disposition = initialDisp
            newCountVaers = vaersColumnMap.newCountVaers
            cumCountVaers = vaersColumnMap.cummCountVaers
            newFatalCountVaers = vaersColumnMap.newFatalCountVaers
            cumFatalCountVaers = vaersColumnMap.cumFatalCountVaers
            newSeriousCountVaers = vaersColumnMap.newSeriousCountVaers
            cumSeriousCountVaers = vaersColumnMap.cumSeriousCountVaers
            newGeriatricCountVaers = vaersColumnMap.newGeriatricCountVaers
            cumGeriatricCountVaers = vaersColumnMap.cumGeriatricCountVaers
            newPediatricCountVaers = vaersColumnMap.newPediatricCountVaers
            cumPediatricCountVaers = vaersColumnMap.cummPediatricCountVaers
            prrValueVaers = vaersColumnMap.prrValueVaers as Double
            prrLCIVaers = vaersColumnMap.prrLCIVaers as Double
            prrUCIVaers = vaersColumnMap.prrUCIVaers as Double
            rorValueVaers = vaersColumnMap.rorValueVaers as Double
            rorLCIVaers = vaersColumnMap.rorLCIVaers as Double
            rorUCIVaers = vaersColumnMap.rorUCIVaers as Double
            ebgmVaers = vaersColumnMap.ebgmVaers as Double
            eb05Vaers = vaersColumnMap.eb05Vaers as Double
            eb95Vaers = vaersColumnMap.eb95Vaers as Double
            chiSquareVaers = vaersColumnMap.chiSquareVaers as Double
            eValueVaers = vaersColumnMap.eValueVaers as Double
            rrValueVaers = vaersColumnMap.rrValueVaers as Double
            listedness = aggregateCaseAlert.listed
            products = aggregateCaseAlert.productName
            productSelection = execProductSelection
            dataSource = Constants.DataSource.VAERS
            prevEB95 = getPreviousPeriodEB95Value(productEventHistoryList, startDateOfQuarter, endDateOfQuarter)
            alertType = AlertConfigType.AGGREGATE_CASE_ALERT
            isPEDetectedFirstTime = (cacheService.getDispositionByValue(aggregateCaseAlert.dispositionId).value == defaultDispositionValue) as Boolean
            lastReviewDuration = calculateLastReviewDurationForAggregateAlert(aggregateCaseAlert)
            configProductSelection = productDictionarySelection
            isNewEvent = aggregateCaseAlert.isNew
            logString = logStringBuilder
            productId = aggregateCaseAlert.productId
        }
        executeRules(ruleDataDTO, aggregateCaseAlert, initialDisp)
        allPECLogs.append(logStringBuilder)
    }


    void executeRulesForVigibaseIntegratedReview(AggregateCaseAlert aggregateCaseAlert, String productDictionarySelection, String execProductSelection,
                                              String defaultDispositionValue, Map<String, AggregateCaseAlert> previousAlertMap,
                                              Map previousAlertTags, Map allPreviousTags, Disposition initialDisp, StringBuffer allPECLogs) {

        StringBuilder logStringBuilder = new StringBuilder()
        logStringBuilder.append("\n********** Rule Processing Started for Integrated Review Vigibase Alert : ${aggregateCaseAlert?.name}**********")
        RuleDataDTO ruleDataDTO = new RuleDataDTO()
        List<ProductEventHistory> productEventHistoryList = getProductEventHistoryList(aggregateCaseAlert)
        def previousAggAlert = previousAlertMap.get(aggregateCaseAlert.productName + "-" + aggregateCaseAlert.pt)
        ruleDataDTO.previousAggAlert = previousAggAlert
        ruleDataDTO.prevAggData = getPreviousDataOfVigibaseIntegratedReview(ruleDataDTO.previousAggAlert, aggregateCaseAlert.id)
        if (ruleDataDTO.prevAggData) {
            ruleDataDTO.isFirstExecution = false
        }
        if (ruleDataDTO.prevAggData) {
            ruleDataDTO.previousTags = previousAlertTags.get(aggregateCaseAlert.productId + "-" + aggregateCaseAlert.ptCode + "-" + aggregateCaseAlert.smqCode) ?: []
            ruleDataDTO.allPrevTags = allPreviousTags.get(aggregateCaseAlert.productId + "-" + aggregateCaseAlert.ptCode + "-" + aggregateCaseAlert.smqCode) ?: []
        }

        int prevQuarter = DateUtil.quarter - 1
        Date startDateOfQuarter = DateUtil.getFirstDateOfQuarter(prevQuarter)
        Date endDateOfQuarter = DateUtil.getEndDateOfQuarter(prevQuarter)
        Map vigibaseColumnMap = aggregateCaseAlert.populateVigibaseColumnMap()
        ruleDataDTO.with {
            execConfigId = aggregateCaseAlert.executedAlertConfigurationId
            disposition = initialDisp
            newCountVigibase = vigibaseColumnMap.newCountVigibase
            cumCountVigibase = vigibaseColumnMap.cummCountVigibase
            newFatalCountVigibase = vigibaseColumnMap.newFatalCountVigibase
            cumFatalCountVigibase = vigibaseColumnMap.cumFatalCountVigibase
            newSeriousCountVigibase = vigibaseColumnMap.newSeriousCountVigibase
            cumSeriousCountVigibase = vigibaseColumnMap.cumSeriousCountVigibase
            newGeriatricCountVigibase = vigibaseColumnMap.newGeriatricCountVigibase
            cumGeriatricCountVigibase = vigibaseColumnMap.cumGeriatricCountVigibase
            newPediatricCountVigibase = vigibaseColumnMap.newPediatricCountVigibase
            cumPediatricCountVigibase = vigibaseColumnMap.cummPediatricCountVigibase
            prrValueVigibase = vigibaseColumnMap.prrValueVigibase as Double
            prrLCIVigibase = vigibaseColumnMap.prrLCIVigibase as Double
            prrUCIVigibase = vigibaseColumnMap.prrUCIVigibase as Double
            rorValueVigibase = vigibaseColumnMap.rorValueVigibase as Double
            rorLCIVigibase= vigibaseColumnMap.rorLCIVigibase as Double
            rorUCIVigibase = vigibaseColumnMap.rorUCIVigibase as Double
            ebgmVigibase = vigibaseColumnMap.ebgmVigibase as Double
            eb05Vigibase = vigibaseColumnMap.eb05Vigibase as Double
            eb95Vigibase = vigibaseColumnMap.eb95Vigibase as Double
            chiSquareVigibase = vigibaseColumnMap.chiSquareVigibase as Double
            eValueVigibase = vigibaseColumnMap.eValueVigibase as Double
            rrValueVigibase = vigibaseColumnMap.rrValueVigibase as Double
            listedness = aggregateCaseAlert.listed
            products = aggregateCaseAlert.productName
            productSelection = execProductSelection
            dataSource = Constants.DataSource.VIGIBASE
            prevEB95 = getPreviousPeriodEB95Value(productEventHistoryList, startDateOfQuarter, endDateOfQuarter)
            alertType = AlertConfigType.AGGREGATE_CASE_ALERT
            isPEDetectedFirstTime = (cacheService.getDispositionByValue(aggregateCaseAlert.dispositionId).value == defaultDispositionValue) as Boolean
            lastReviewDuration = calculateLastReviewDurationForAggregateAlert(aggregateCaseAlert)
            configProductSelection = productDictionarySelection
            isNewEvent = aggregateCaseAlert.isNew
            logString = logStringBuilder
            productId = aggregateCaseAlert.productId
        }
        executeRules(ruleDataDTO, aggregateCaseAlert, initialDisp)
        allPECLogs.append(logStringBuilder)
    }
    void executeRulesForJaderReview(AggregateCaseAlert aggregateCaseAlert, String productDictionarySelection, String execProductSelection,
                                    String defaultDispositionValue, Map<String, AggregateCaseAlert> previousAlertMap,
                                    Map previousAlertTags, Map allPreviousTags, Disposition initialDisp, StringBuffer allPECLogs) {

        StringBuilder logStringBuilder = new StringBuilder()
        logStringBuilder.append("\n********** Rule Processing Started for Integrated Review Jader Alert : ${aggregateCaseAlert?.name}**********")
        RuleDataDTO ruleDataDTO = new RuleDataDTO()
        List<ProductEventHistory> productEventHistoryList = getProductEventHistoryList(aggregateCaseAlert)
        def previousAggAlert = previousAlertMap.get(aggregateCaseAlert.productName + "-" + aggregateCaseAlert.pt)
        ruleDataDTO.previousAggAlert = previousAggAlert
        ruleDataDTO.prevAggData = getPreviousDataOfJaderIntegratedReview(ruleDataDTO.previousAggAlert, aggregateCaseAlert.id)
        if (ruleDataDTO.prevAggData) {
            ruleDataDTO.isFirstExecution = false
        }
        if (ruleDataDTO.prevAggData) {
            ruleDataDTO.previousTags = previousAlertTags.get(aggregateCaseAlert.productId + "-" + aggregateCaseAlert.ptCode + "-" + aggregateCaseAlert.smqCode) ?: []
            ruleDataDTO.allPrevTags = allPreviousTags.get(aggregateCaseAlert.productId + "-" + aggregateCaseAlert.ptCode + "-" + aggregateCaseAlert.smqCode) ?: []
        }

        int prevQuarter = DateUtil.quarter - 1
        Date startDateOfQuarter = DateUtil.getFirstDateOfQuarter(prevQuarter)
        Date endDateOfQuarter = DateUtil.getEndDateOfQuarter(prevQuarter)
        Map jaderColumnMap = aggregateCaseAlert.populateJaderColumnMap()
        ruleDataDTO.with {
            execConfigId = aggregateCaseAlert.executedAlertConfigurationId
            disposition = initialDisp
            newCountJader = jaderColumnMap.newCountJader
            cumCountJader = jaderColumnMap.cumCountJader
            newFatalCountJader = jaderColumnMap.newFatalCountJader
            cumFatalCountJader = jaderColumnMap.cumFatalCountJader
            newSeriousCountJader = jaderColumnMap.newSeriousCountJader
            cumSeriousCountJader = jaderColumnMap.cumSeriousCountJader
            newGeriatricCountJader = jaderColumnMap.newGeriatricCountJader
            cumGeriatricCountJader = jaderColumnMap.cumGeriatricCountJader
            newPediatricCountJader = jaderColumnMap.newPediatricCountJader
            cumPediatricCountJader = jaderColumnMap.cumPediatricCountJader
            prrValueJader = jaderColumnMap.prrValueJader as Double
            prrLCIJader = jaderColumnMap.prrLCIJader as Double
            prrUCIJader = jaderColumnMap.prrUCIJader as Double
            rorValueJader = jaderColumnMap.rorValueJader as Double
            rorLCIJader= jaderColumnMap.rorLCIJader as Double
            rorUCIJader = jaderColumnMap.rorUCIJader as Double
            ebgmJader = jaderColumnMap.ebgmJader as Double
            eb05Jader = jaderColumnMap.eb05Jader as Double
            eb95Jader = jaderColumnMap.eb95Jader as Double
            chiSquareJader = jaderColumnMap.chiSquareJader as Double
            eValueJader = jaderColumnMap.eValueJader as Double
            rrValueJader = jaderColumnMap.rrValueJader as Double
            listedness = aggregateCaseAlert.listed
            products = aggregateCaseAlert.productName
            productSelection = execProductSelection
            dataSource = Constants.DataSource.JADER
            prevEB95 = getPreviousPeriodEB95Value(productEventHistoryList, startDateOfQuarter, endDateOfQuarter)
            alertType = AlertConfigType.AGGREGATE_CASE_ALERT
            isPEDetectedFirstTime = (cacheService.getDispositionByValue(aggregateCaseAlert.dispositionId).value == defaultDispositionValue) as Boolean
            lastReviewDuration = calculateLastReviewDurationForAggregateAlert(aggregateCaseAlert)
            configProductSelection = productDictionarySelection
            isNewEvent = aggregateCaseAlert.isNew
            logString = logStringBuilder
            productId = aggregateCaseAlert.productId
        }
        executeRules(ruleDataDTO, aggregateCaseAlert,initialDisp,Constants.DataSource.JADER)
        allPECLogs.append(logStringBuilder)
    }

    Map getPreviousDataOfFaersIntegratedReview(AggregateCaseAlert prevAggregateCaseAlert, Long prevAlertId) {
        Map result = [:]
        if (prevAggregateCaseAlert) {
            Map prevFaersAlertMap = prevAggregateCaseAlert.populateFaersColumnMap()
            result = [
                    id                              : prevAlertId,
                    (Constants.BusinessConfigAttributes.PRR_SCORE_FAERS)                        : prevFaersAlertMap.prrValueFaers,
                    (Constants.BusinessConfigAttributes.PRRLCI_SCORE_FAERS)                         : prevFaersAlertMap.prrLCIFaers,
                    (Constants.BusinessConfigAttributes.PRRUCI_SCORE_FAERS)                           : prevFaersAlertMap.prrUCIFaers,
                    (Constants.BusinessConfigAttributes.ROR_SCORE_FAERS)                        : prevFaersAlertMap.rorValueFaers,
                    (Constants.BusinessConfigAttributes.RORLCI_SCORE_FAERS)                           : prevFaersAlertMap.rorLCIFaers,
                    (Constants.BusinessConfigAttributes.RORUCI_SCORE_FAERS)                           : prevFaersAlertMap.rorUCIFaers,
                    (Constants.BusinessConfigAttributes.EBGM_SCORE_FAERS)                            : prevFaersAlertMap.ebgmFaers,
                    (Constants.BusinessConfigAttributes.EB05_SCORE_FAERS)                            : prevFaersAlertMap.eb05Faers,
                    (Constants.BusinessConfigAttributes.EB95_SCORE_FAERS)                            : prevFaersAlertMap.eb95Faers,
                    (Constants.BusinessConfigAttributes.CHI_SQUARE_FAERS)                       : prevFaersAlertMap.chiSquareFaers,
                    (Constants.BusinessConfigAttributes.E_VALUE_FAERS)                          : prevFaersAlertMap.eValueFaers,
                    (Constants.BusinessConfigAttributes.RR_VALUE_FAERS)                         : prevFaersAlertMap.rrValueFaers,

                    (Constants.BusinessConfigAttributes.NEW_COUNT_FAERS)           : prevFaersAlertMap.newCountFaers,
                    (Constants.BusinessConfigAttributes.CUMM_COUNT_FAERS)          : prevFaersAlertMap.cummCountFaers,
                    (Constants.BusinessConfigAttributes.NEW_SPON_COUNT_FAERS)       : prevFaersAlertMap.newSponCountFaers,
                    (Constants.BusinessConfigAttributes.CUM_SPON_COUNT_FAERS)       : prevFaersAlertMap.cumSponCountFaers,
                    (Constants.BusinessConfigAttributes.NEW_SERIOUS_COUNT_FAERS)    : prevFaersAlertMap.newSeriousCountFaers,
                    (Constants.BusinessConfigAttributes.CUM_SERIOUS_COUNT_FAERS)     : prevFaersAlertMap.cumSeriousCountFaers,
                    (Constants.BusinessConfigAttributes.NEW_FATAL_COUNT_FAERS)      : prevFaersAlertMap.newFatalCountFaers,
                    (Constants.BusinessConfigAttributes.CUM_FATAL_COUNT_FAERS)      : prevFaersAlertMap.cumFatalCountFaers,
                    (Constants.BusinessConfigAttributes.NEW_STUDY_COUNT_FAERS)      : prevFaersAlertMap.newStudyCountFaers,
                    (Constants.BusinessConfigAttributes.CUM_STUDY_COUNT_FAERS)      : prevFaersAlertMap.cumStudyCountFaers,
                    (Constants.BusinessConfigAttributes.NEW_GERIA_FAERS)   : prevFaersAlertMap.newGeriatricCountFaers,
                    (Constants.BusinessConfigAttributes.CUMM_GERIA_FAERS)  : prevFaersAlertMap.cumGeriatricCountFaers,
                    (Constants.BusinessConfigAttributes.NEW_PAED_FAERS)  : prevFaersAlertMap.newPediatricCountFaers,
                    (Constants.BusinessConfigAttributes.CUMM_PAED_FAERS) : prevFaersAlertMap.cummPediatricCountFaers,
                    (Constants.BusinessConfigAttributes.NEW_INTER_FAERS)   : prevFaersAlertMap.newInteractingCountFaers,
                    (Constants.BusinessConfigAttributes.CUMM_INTER_FAERS)  : prevFaersAlertMap.cummInteractingCountFaers,
                    (Constants.BusinessConfigAttributes.NEW_NON_SERIOUS_FAERS)   : prevFaersAlertMap.newNonSeriousFaers,
                    (Constants.BusinessConfigAttributes.CUM_NON_SERIOUS_FAERS)  : prevFaersAlertMap.cumNonSeriousFaers,

            ]
            StratificationScoreDTO stratificationScoreDTO = new StratificationScoreDTO()
            stratificationScoreDTO = stratificationScoreDTO.addStratificationScore(prevAggregateCaseAlert.eb05GenderFaers?.toString(),'eb05')
            stratificationScoreDTO = stratificationScoreDTO.addStratificationScore(prevAggregateCaseAlert.eb95GenderFaers?.toString(),'eb95')
            stratificationScoreDTO = stratificationScoreDTO.addStratificationScore(prevAggregateCaseAlert.eb05AgeFaers?.toString(),'eb05')
            stratificationScoreDTO = stratificationScoreDTO.addStratificationScore(prevAggregateCaseAlert.eb95AgeFaers?.toString(),'eb95')
            stratificationScoreDTO = stratificationScoreDTO.addStratificationScore(prevAggregateCaseAlert.ebgmAgeFaers?.toString(),'ebgm')
            stratificationScoreDTO = stratificationScoreDTO.addStratificationScore(prevAggregateCaseAlert.ebgmGenderFaers?.toString(),'ebgm')
            result << stratificationScoreDTO.mapData()
        }
        result
    }

    Map getPreviousDataOfVaersIntegratedReview(AggregateCaseAlert prevAggregateCaseAlert, Long prevAlertId) {
        Map result = [:]
        if (prevAggregateCaseAlert) {
            Map prevVaersAlertMap = prevAggregateCaseAlert.populateVaersColumnMap()
            result = [
                    id                              : prevAlertId,
                    (Constants.BusinessConfigAttributes.PRR_SCORE_VAERS)                        : prevVaersAlertMap.prrValueVaers,
                    (Constants.BusinessConfigAttributes.PRRLCI_SCORE_VAERS)                          : prevVaersAlertMap.prrLCIVaers,
                    (Constants.BusinessConfigAttributes.PRRUCI_SCORE_VAERS)                           : prevVaersAlertMap.prrUCIVaers,
                    (Constants.BusinessConfigAttributes.ROR_SCORE_VAERS)                        : prevVaersAlertMap.rorValueVaers,
                    (Constants.BusinessConfigAttributes.RORLCI_SCORE_VAERS)                           : prevVaersAlertMap.rorLCIVaers,
                    (Constants.BusinessConfigAttributes.RORUCI_SCORE_VAERS)                           : prevVaersAlertMap.rorUCIVaers,
                    (Constants.BusinessConfigAttributes.EBGM_SCORE_VAERS)                           : prevVaersAlertMap.ebgmVaers,
                    (Constants.BusinessConfigAttributes.EB05_SCORE_VAERS)                           : prevVaersAlertMap.eb05Vaers,
                    (Constants.BusinessConfigAttributes.EB95_SCORE_VAERS)                           : prevVaersAlertMap.eb95Vaers,
                    (Constants.BusinessConfigAttributes.CHI_SQUARE_VAERS)                      : prevVaersAlertMap.chiSquareVaers,
                    (Constants.BusinessConfigAttributes.NEW_COUNT_VAERS)                       : prevVaersAlertMap.newCountVaers,
                    (Constants.BusinessConfigAttributes.CUMM_COUNT_VAERS)                      : prevVaersAlertMap.cummCountVaers,
                    (Constants.BusinessConfigAttributes.NEW_FATAL_COUNT_VAERS)                  : prevVaersAlertMap.newFatalCountVaers,
                    (Constants.BusinessConfigAttributes.CUM_FATAL_COUNT_VAERS)                  : prevVaersAlertMap.cumFatalCountVaers,
                    (Constants.BusinessConfigAttributes.NEW_SERIOUS_COUNT_VAERS)                : prevVaersAlertMap.newSeriousCountVaers,
                    (Constants.BusinessConfigAttributes.CUM_SERIOUS_COUNT_VAERS)                : prevVaersAlertMap.cumSeriousCountVaers,
                    (Constants.BusinessConfigAttributes.NEW_GERIATRIC_COUNT_VAERS)              : prevVaersAlertMap.newGeriatricCountVaers,
                    (Constants.BusinessConfigAttributes.CUM_GERIATRIC_COUNT_VAERS)              : prevVaersAlertMap.cumGeriatricCountVaers,
                    (Constants.BusinessConfigAttributes.NEW_PAED_VAERS)              : prevVaersAlertMap.newPediatricCountVaers,
                    (Constants.BusinessConfigAttributes.CUM_PAED_VAERS)             : prevVaersAlertMap.cummPediatricCountVaers,
                    (Constants.BusinessConfigAttributes.E_VALUE_VAERS)                         : prevVaersAlertMap.eValueVaers,
                    (Constants.BusinessConfigAttributes.RR_VALUE_VAERS)                        : prevVaersAlertMap.rrValueVaers
            ]
        }
        result
    }

    Map getPreviousDataOfVigibaseIntegratedReview(AggregateCaseAlert prevAggregateCaseAlert, Long prevAlertId) {
        Map result = [:]
        if (prevAggregateCaseAlert) {
            Map prevVigibaseAlertMap = prevAggregateCaseAlert.populateVigibaseColumnMap()
            result = [
                    id                              : prevAlertId,
                    (Constants.BusinessConfigAttributes.PRR_SCORE_VIGIBASE)                        : prevVigibaseAlertMap.prrValueVigibase,
                    (Constants.BusinessConfigAttributes.PRRLCI_SCORE_VIGIBASE)                          : prevVigibaseAlertMap.prrLCIVigibase,
                    (Constants.BusinessConfigAttributes.PRRUCI_SCORE_VIGIBASE)                           : prevVigibaseAlertMap.prrUCIVigibase,
                    (Constants.BusinessConfigAttributes.ROR_SCORE_VIGIBASE)                        : prevVigibaseAlertMap.rorValueVigibase,
                    (Constants.BusinessConfigAttributes.RORLCI_SCORE_VIGIBASE)                           : prevVigibaseAlertMap.rorLCIVigibase,
                    (Constants.BusinessConfigAttributes.RORUCI_SCORE_VIGIBASE)                           : prevVigibaseAlertMap.rorUCIVigibase,
                    (Constants.BusinessConfigAttributes.EBGM_SCORE_VIGIBASE)                           : prevVigibaseAlertMap.ebgmVigibase,
                    (Constants.BusinessConfigAttributes.EB05_SCORE_VIGIBASE)                           : prevVigibaseAlertMap.eb05Vigibase,
                    (Constants.BusinessConfigAttributes.EB95_SCORE_VIGIBASE)                           : prevVigibaseAlertMap.eb95Vigibase,
                    (Constants.BusinessConfigAttributes.CHI_SQUARE_VIGIBASE)                      : prevVigibaseAlertMap.chiSquareVigibase,
                    (Constants.BusinessConfigAttributes.NEW_COUNT_VIGIBASE)                       : prevVigibaseAlertMap.newCountVigibase,
                    (Constants.BusinessConfigAttributes.CUMM_COUNT_VIGIBASE)                      : prevVigibaseAlertMap.cummCountVigibase,
                    (Constants.BusinessConfigAttributes.NEW_FATAL_COUNT_VIGIBASE)                  : prevVigibaseAlertMap.newFatalCountVigibase,
                    (Constants.BusinessConfigAttributes.CUM_FATAL_COUNT_VIGIBASE)                  : prevVigibaseAlertMap.cumFatalCountVigibase,
                    (Constants.BusinessConfigAttributes.NEW_SERIOUS_COUNT_VIGIBASE)                : prevVigibaseAlertMap.newSeriousCountVigibase,
                    (Constants.BusinessConfigAttributes.CUM_SERIOUS_COUNT_VIGIBASE)                : prevVigibaseAlertMap.cumSeriousCountVigibase,
                    (Constants.BusinessConfigAttributes.NEW_GERIATRIC_COUNT_VIGIBASE)              : prevVigibaseAlertMap.newGeriatricCountVigibase,
                    (Constants.BusinessConfigAttributes.CUM_GERIATRIC_COUNT_VIGIBASE)              : prevVigibaseAlertMap.cumGeriatricCountVigibase,
                    (Constants.BusinessConfigAttributes.NEW_PAED_VIGIBASE)              : prevVigibaseAlertMap.newPediatricCountVigibase,
                    (Constants.BusinessConfigAttributes.CUM_PAED_VIGIBASE)             : prevVigibaseAlertMap.cummPediatricCountVigibase,
                    (Constants.BusinessConfigAttributes.E_VALUE_VIGIBASE)                         : prevVigibaseAlertMap.eValueVigibase,
                    (Constants.BusinessConfigAttributes.RR_VALUE_VIGIBASE)                        : prevVigibaseAlertMap.rrValueVigibase
            ]
        }
        result
    }
    Map getPreviousDataOfJaderIntegratedReview(AggregateCaseAlert prevAggregateCaseAlert, Long prevAlertId) {
        Map result = [:]
        if (prevAggregateCaseAlert) {
            Map prevJaderAlertMap = prevAggregateCaseAlert.populateJaderColumnMap()
            result = [
                    id                              : prevAlertId,
                    prrValueJader                        : prevJaderAlertMap.prrValueJader,
                    prrLCIJader                          : prevJaderAlertMap.prrLCIJader,
                    prrUCIJader                           : prevJaderAlertMap.prrUCIJader,
                    rorValueJader                        : prevJaderAlertMap.rorValueJader,
                    rorLCIJader                           : prevJaderAlertMap.rorLCIJader,
                    rorUCIJader                           : prevJaderAlertMap.rorUCIJader,
                    ebgmJader                            : prevJaderAlertMap.ebgmJader,
                    eb05Jader                            : prevJaderAlertMap.eb05Jader,
                    eb95Jader                            : prevJaderAlertMap.eb95Jader,
                    chiSquareJader                       : prevJaderAlertMap.chiSquareJader,
                    newCountJader                        : prevJaderAlertMap.newCountJader,
                    cumCountJader                       : prevJaderAlertMap.cumCountJader,
                    newFatalCountJader                   : prevJaderAlertMap.newFatalCountJader,
                    cumFatalCountJader                   : prevJaderAlertMap.cumFatalCountJader,
                    newSeriousCountJader                 : prevJaderAlertMap.newSeriousCountJader,
                    cumSeriousCountJader                 : prevJaderAlertMap.cumSeriousCountJader,
                    newGeriatricCountJader               : prevJaderAlertMap.newGeriatricCountJader,
                    cumGeriatricCountJader               : prevJaderAlertMap.cumGeriatricCountJader,
                    newPediatricCountJader               : prevJaderAlertMap.newPediatricCountJader,
                    cumPediatricCountJader              : prevJaderAlertMap.cumPediatricCountJader,
                    eValueJader                          : prevJaderAlertMap.eValueJader,
                    rrValueJader                         : prevJaderAlertMap.rrValueJader
            ]
        }
        result
    }

    RuleDataDTO populateStratificationScoreFaers(AggregateCaseAlert aggregateCaseAlert, RuleDataDTO ruleDataDTO) {
        StratificationScoreDTO stratificationScoreDTO = new StratificationScoreDTO()
        ruleDataDTO.stratificationScoreDTO = stratificationScoreDTO.addStratificationScore(aggregateCaseAlert.eb05GenderFaers?.toString(), 'eb05')
        ruleDataDTO.stratificationScoreDTO = stratificationScoreDTO.addStratificationScore(aggregateCaseAlert.eb95GenderFaers?.toString(), 'eb95')
        ruleDataDTO.stratificationScoreDTO = stratificationScoreDTO.addStratificationScore(aggregateCaseAlert.eb05AgeFaers?.toString(), 'eb05')
        ruleDataDTO.stratificationScoreDTO = stratificationScoreDTO.addStratificationScore(aggregateCaseAlert.eb95AgeFaers?.toString(), 'eb95')
        ruleDataDTO.stratificationScoreDTO = stratificationScoreDTO.addStratificationScore(aggregateCaseAlert.ebgmAgeFaers?.toString(), 'ebgm')
        ruleDataDTO.stratificationScoreDTO = stratificationScoreDTO.addStratificationScore(aggregateCaseAlert.ebgmGenderFaers?.toString(), 'ebgm')
        ruleDataDTO
    }

    String fetchIdFromSignalName(String name = '') {
        int lastIndex = name.lastIndexOf("(")
        String signalId = ''
        if (lastIndex != -1) {
            signalId = name.substring(lastIndex + 1, name.length() - 1)
        }
        return signalId
    }

}
