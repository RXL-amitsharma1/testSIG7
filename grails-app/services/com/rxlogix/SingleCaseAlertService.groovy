package com.rxlogix

import com.rxlogix.CategoryDTO
import com.rxlogix.audit.AuditTrail
import com.rxlogix.config.*
import com.rxlogix.dto.*
import com.rxlogix.dto.caseSeries.integration.ExecutedCaseSeriesDTO
import com.rxlogix.dto.caseSeries.integration.ExecutedDateRangeInfoDTO
import com.rxlogix.dto.caseSeries.integration.ParameterValueDTO
import com.rxlogix.dto.caseSeries.integration.QueryValueListDTO
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.enums.DispositionEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.QueryTypeEnum
import com.rxlogix.helper.LinkHelper
import com.rxlogix.mart.CaseSeriesTagMapping
import com.rxlogix.signal.*
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.*
import grails.async.Promise
import grails.converters.JSON
import grails.events.EventPublisher
import grails.gorm.transactions.Transactional
import grails.util.Holders
import grails.validation.ValidationException
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.sql.GroovyResultSet
import groovy.sql.Sql
import org.apache.commons.lang.math.NumberUtils
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.exception.ExceptionUtils
import org.apache.http.HttpStatus
import org.apache.http.util.TextUtils
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.Transaction
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.criterion.Order
import org.hibernate.jdbc.Work
import org.hibernate.sql.JoinType
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.Duration
import org.joda.time.format.DateTimeFormat
import org.springframework.context.MessageSource
import org.springframework.transaction.annotation.Propagation
import org.springframework.web.multipart.MultipartFile
import com.rxlogix.Constants

import java.sql.Clob
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.concurrent.*
import java.util.regex.Pattern

import static com.rxlogix.Constants.AlertConfigType.SINGLE_CASE_ALERT
import static com.rxlogix.util.DateUtil.DEFAULT_DATE_FORMAT
import static com.rxlogix.util.DateUtil.toDateString
import static com.rxlogix.util.MiscUtil.calcDueDate
import static grails.async.Promises.task

class SingleCaseAlertService implements Alertbililty, AlertAsyncUtil, LinkHelper, EventPublisher {

    def dataSource_pva
    def alertService
    def caseHistoryService
    def CRUDService
    MessageSource messageSource
    def productBasedSecurityService
    def userService
    def validatedSignalService
    def productEventHistoryService
    def businessConfigurationService
    def activityService
    def sqlGenerationService
    def signalDataSourceService
    def medicalConceptsService
    def actionTemplateService
    def sessionFactory
    def emailService
    def queryService
    def reportIntegrationService
    def reportExecutorService
    def configurationService
    def spotfireService
    def actionService
    def cacheService
    def alertCommentService
    def grailsApplication
    def dataObjectService
    def springSecurityService
    def singleAlertTagService
    EmailNotificationService emailNotificationService
    def signalExecutorService
    def alertTagService
    def pvsGlobalTagService
    def pvsAlertTagService
    def archiveService
    def dataSource
    def viewInstanceService
    CustomMessageService customMessageService
    def undoableDispositionService
    def appAlertProgressStatusService
    def signalAuditLogService
    static Map columnNames = [
            "initialfu"         : "initialFu", "serious": "serious", "indication": "indication", "eventoutcome": "eventOutcome",
            "causeofdeath"      : "causeOfDeath", "seriousunlistedrelated": "seriousUnlistedRelated", "concomit": "conComit",
            "age"               : "age", "country": "country", "gender": "gender", "rechallenge": "rechallenge", "death": "death",
            "casetype"          : "caseType", "indnumber": "indNumber", "apptypeandnum": "appTypeAndNum", "compoundingflag": "compoundingFlag",
            "submitter"         : "submitter", "region": "region", "patienthistdrugs": "patientHistDrugs", "batchlotno": "batchLotNo",
            "caseclassification": "caseClassification", "protocolno": "protocolNo", "issusar": "isSusar",
            "therapydates"      : "therapyDates", "preanda": "preAnda", "casereporttype": "caseReportType", "changes": "changes",
            "genericname"       : "genericName", "eventonsetdate": "eventOnsetDate", "pregnancy": "pregnancy", "medicallyconfirmed": "medicallyConfirmed",
            "allptsoutcome"     : "allPTsOutcome", "dateofbirth": "dateOfBirth", "dosedetails": "doseDetails", "outcome": "outcome",
            "listedness"        : "listedness", "caseinitreceiptdate": "caseInitReceiptDate", "lockeddate": "lockedDate", "casecreationdate": "caseCreationDate",
            "patientage"        : "patientAge", "duedate": "dueDate", "completenessscore": "completenessScore", "patientmedhist": "patientMedHist",
            "alertname"         : "alertName", "casenumber": "caseNumber", "productname": "productName", "pt": "pt", "masterpreftermall": "masterPrefTermAll",
            "malfunction"       : "malfunction", "comboflag": "comboFlag", "badge": "badge", "reportershcpflag": "reportersHcpFlag",
            "timetoonset"       : "timeToOnset", "mederrorspt": "medErrorsPt", "displastchange": "dispLastChange", "suspprod": "suspProd",
            "copackagedproduct" : "coPackagedProduct"
    ]


    def listAll() {
        SingleCaseAlert.findAll()
    }

    def getAlertById(id) {
        SingleCaseAlert.findById(id)
    }

    /**
     * Method to filter cases for the configuration and passed alert data.
     * @param configuration
     * @param alertData
     * @return : Filtered cases.
     */
    List<Map> filterCases(Configuration configuration, List<Map> alertData) {

        Map filteredData = [:]
        Map duplicateData = [:]
        Map primaryIndData = [:]

        alertData.reverse().each { Map data ->
            def caseNumber = data[cacheService.getRptFieldIndexCache('masterCaseNum')]
            def obj = duplicateData.get(caseNumber)
            String productFamily = data[cacheService.getRptFieldIndexCache('productFamilyId')]
            def familyList = configuration.getProductNameList()
            def selectionType = configuration.getProductType()
            def csiSponsorStudyNumber
            def studyClassificationId

            if (Holders.config.custom.qualitative.fields.enabled) {
                csiSponsorStudyNumber = cacheService.getRptFieldIndexCache('vwcsiSponsorStudyNumber')
                studyClassificationId = cacheService.getRptFieldIndexCache('vwstudyClassificationId')
            } else {
                csiSponsorStudyNumber = cacheService.getRptFieldIndexCache('crepoud_text_11')
                studyClassificationId = cacheService.getRptFieldIndexCache('crepoud_text_12')
            }

            if (data[studyClassificationId] && getIsPrimaryInd(data[studyClassificationId].toString().toUpperCase())){
                primaryIndData.put(caseNumber, data[csiSponsorStudyNumber])
            }
            else if(primaryIndData.get(caseNumber)){
                data[csiSponsorStudyNumber] = primaryIndData.get(caseNumber)
            }
            if (!obj) {
                filteredData.put(caseNumber, data)
            } else {
                if (obj[studyClassificationId] && getIsPrimaryInd(obj[studyClassificationId].toString().toUpperCase()))
                    primaryIndData.put(caseNumber, obj[csiSponsorStudyNumber])
                else
                    obj[csiSponsorStudyNumber] = primaryIndData.get(caseNumber)
                if (selectionType == Constants.ProductSelectionType.FAMILY) {
                    if (familyList.contains(productFamily?.toLowerCase())) {
                        filteredData.remove(caseNumber)
                        filteredData.put(caseNumber, obj)
                    }
                } else {
                    filteredData.remove(caseNumber)
                    if (obj[cacheService.getRptFieldIndexCache('masterPrimProdName')] == obj[cacheService.getRptFieldIndexCache('productProductId')]) {
                        filteredData.put(caseNumber, obj)
                    } else {
                        filteredData.put(caseNumber, data)
                    }
                }
            }
            duplicateData.put(caseNumber, data)
        }
        duplicateData = null
        filteredData.values() as List<Map>
    }


    private List<NullablePair<SingleCaseAlert, Activity>> processAlertData(List<Map> alertData,
                                                                     Configuration config, ExecutedConfiguration executedConfig, List dateRangeStartEndDate,
                                                                     String justification, Boolean isAddCase, List<Map> queryCaseMaps,
                                                                     boolean isTempCaseSeriesFlow, List supersededAlertIds, String type) {
        List<String> newCaseIds = fetchNewCases(alertData)
        pvsGlobalTagService.batchPersistGlobalCase(newCaseIds)
        List<GlobalCase> globalCaseList = fetchGlobalCases(alertData)
        List<GlobalCase> existingGlobalCaseList = dataObjectService.getGlobalCaseList(executedConfig.id)
        dataObjectService.setGlobalCaseList(executedConfig.id, globalCaseList + existingGlobalCaseList)
        List<NullablePair<SingleCaseAlert, Activity>> allAlertsAndActivities = []
        ExecutorService executorService = signalExecutorService.threadPoolForQualAlertExec()
        User currentUser = userService.getUser()
        String productSelection = executedConfig.productSelection
        log.info("Thread Starts")
        StringBuffer logStringBuilder = new StringBuffer()
        logStringBuilder.append("\nApplying business rule for ${executedConfig.id}")
        List<Future<NullablePair<SingleCaseAlert, Activity>>> futureList = alertData.collect { Map data ->
            executorService.submit({ ->
                createSingleCaseAlert(data, config, executedConfig, dateRangeStartEndDate, justification, isAddCase, currentUser, queryCaseMaps, isTempCaseSeriesFlow, productSelection, supersededAlertIds, type,logStringBuilder)
            } as Callable)
        }
        futureList.each {
            allAlertsAndActivities.add(it.get())
        }
        if(!isAddCase){
            Promise promise = task {
                alertService.saveLogsInFile(logStringBuilder,"${config.id}_${executedConfig.type}")  //Preventing log file creation in case of add case #Dev finding
            }
            promise.onError {
                log.error("BR Log File Processing Failed.")
            }
            promise.onComplete {
                log.info("BR Log File Processing Finished.")
            }
        }
        log.info("Thread Ends")
        allAlertsAndActivities
    }

    protected NullablePair<SingleCaseAlert, Activity> createSingleCaseAlert(Map data, Configuration config, ExecutedConfiguration executedConfig, List dateRangeStartEndDate,
                                                                      String justification, Boolean isAddCase, User currentUser, List<Map> queryCaseMaps,
                                                                      boolean isTempCaseSeriesFlow, String productSelection, List<Long> supersededAlertIds, String type, StringBuffer logStringBuilder) {

        boolean isPVCM = dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM)
        String caseNumber = data[cacheService.getRptFieldIndexCache('masterCaseNum')]
        String versionNumber = data[cacheService.getRptFieldIndexCache('masterVersionNum')]
        String productFamily = data[cacheService.getRptFieldIndexCache('productFamilyId')] ?: Constants.Commons.UNDEFINED
        Integer followUpNumber = (data[cacheService.getRptFieldIndexCache('masterFupNum')]) as Integer
        String productName
        String reportedProductName
        if (isPVCM) {
            productName = data[cacheService.getRptFieldIndexCache('productProductId')] ?: (data[cacheService.getRptFieldIndexCache('productProductName')] ?: Constants.Commons.UNCODED)
            reportedProductName = data[cacheService.getRptFieldIndexCache('productProductName')] ?: Constants.Commons.UNCODED
        } else {
            productName = data[cacheService.getRptFieldIndexCache('productProductId')] ?: Constants.Commons.UNCODED
            // java variable needs to be updated in case of non siab
            reportedProductName = data[cacheService.getRptFieldIndexCache('productProductName')] ?: Constants.Commons.UNCODED
        }
        BigInteger productId = data[cacheService.getRptFieldIndexCache('cpiProdIdResolved')] ?: Constants.Commons.UNDEFINED_NUM as BigInteger
        String pt = data[cacheService.getRptFieldIndexCache('masterPrimEvtPrefTerm')] ?: (data[cacheService.getRptFieldIndexCache('eventDescReptd')] ?: Constants.Commons.UNDEFINED)
        String listedness = data[cacheService.getRptFieldIndexCache('assessListedness')] ?: Constants.Commons.DASH_STRING
        String outcome = data[cacheService.getRptFieldIndexCache('assessOutcome')] ?: Constants.Commons.BLANK_STRING
        String initialFu
        Disposition defaultDisposition = dataObjectService.getDefaultDisposition(config.id)
        String appTypeAndNum = getAppTypeAndNumValue(data)
        boolean isNew
        boolean isPrevReportingPeriod
        boolean isFollowUpExists
        Boolean isCaseReset = false
        Date dueDate
        Long priorityId
        Long dispositionId
        Long assignedToId
        Long assignedToGroupId
        Long singleAlertId
        Double patientAge

        if (data.containsKey('isNew')) {
            isNew = true
            data.remove('isNew')
        }

        if (data.containsKey('isPreviousReportingPeriod')) {
            isPrevReportingPeriod = true
            data.remove('isPreviousReportingPeriod')
        }

        if (data.containsKey('isFollowUpExists')) {
            isFollowUpExists = true
            data.remove('isFollowUpExists')
        }

        if (data.containsKey('dueDate')) {
            dueDate = data["dueDate"]
            data.remove('dueDate')
        }

        if (data.containsKey('dispositionId')) {
            dispositionId = data["dispositionId"]
            data.remove('dispositionId')
        }
        if(dispositionId == null && config.isStandalone == true) {
            Disposition.withTransaction {
                defaultDisposition = Disposition.findByValidatedConfirmed(true)
                dispositionId = defaultDisposition?.id
            }
        }

        if (data.containsKey('priorityId')) {
            priorityId = data["priorityId"]
            data.remove('priorityId')
        }

        if (data.containsKey('assignedToId')) {
            assignedToId = data["assignedToId"]
            data.remove('assignedToId')
        }

        if (data.containsKey('assignedToGroupId')) {
            assignedToGroupId = data["assignedToGroupId"]
            data.remove('assignedToGroupId')
        }

        if (data.containsKey('singleAlertId')) {
            singleAlertId = data["singleAlertId"]
            data.remove('singleAlertId')
        }

        if (data.containsKey('initialFu')) {
            initialFu = data["initialFu"]
            data.remove('initialFu')
        }

        if (Holders.config.custom.qualitative.fields.enabled && data[cacheService.getRptFieldIndexCache('casePatInfoPvrUdNumber2')] != null) {
            patientAge = data[cacheService.getRptFieldIndexCache('casePatInfoPvrUdNumber2')] as Double
        } else if (data[cacheService.getRptFieldIndexCache('patInfoPatientAgeYears')] != null) {
            patientAge = data[cacheService.getRptFieldIndexCache('patInfoPatientAgeYears')] as Double
        }

        //Fetch the latest case history for the case and product family.
        CaseHistory existingCaseHistory = dataObjectService.getExistingCaseHistory(config.id, caseNumber)
        Map scaData = [caseNumber           : caseNumber, versionNumber: versionNumber, productFamily: productFamily,
                       productName          : productName, productId: productId, pt: pt, followUpNumber: followUpNumber,
                       dateRangeStartEndDate: dateRangeStartEndDate, listedness : listedness, outcome: outcome,
                       patientAge: patientAge, appTypeAndNum: appTypeAndNum, isNew: isNew, initialFu: initialFu, reportedProductName : reportedProductName]
        Map SCAJsonFieldMap = grailsApplication.config.advancedFilter.singleCaseAlert.jsonFieldMap
        Map jsonField = [:]
        SCAJsonFieldMap.each { k, v ->
            jsonField.put("${v}", [])
        }

        SingleCaseAlert singleCaseAlert = saveFieldsInSingleCaseAlert( config, executedConfig, scaData, data, isTempCaseSeriesFlow, type)
        splitDataForAdvanceFilter(singleCaseAlert, jsonField)

        singleCaseAlert.patientMedHist = addNewLineInField(singleCaseAlert.patientMedHist)

        if (singleCaseAlert.conComit != Constants.Commons.BLANK_STRING) {
            String[] conComitList = singleCaseAlert.conComit?.split('\r\n')
            singleCaseAlert.conComitList = []
            conComitList.each {
                if (it && it.length() > 2) {
                    singleCaseAlert.conComitList.add(it.substring(3, it.length()).trim())
                }
            }
            jsonField.put("${SCAJsonFieldMap.get("conComit")}", singleCaseAlert.conComitList)
        }
        if (singleCaseAlert.patientMedHist != Constants.Commons.BLANK_STRING) {
            if(singleCaseAlert.executedAlertConfiguration.selectedDatasource == Constants.DataSource.JADER){
                String[] patientMedHistList = singleCaseAlert.patientMedHist?.split('\r\n')
                singleCaseAlert.patientMedHistList = []
                patientMedHistList.each {
                    if (it && it.length() > 2) {
                        singleCaseAlert.patientMedHistList.add(it.substring(3, it.length()).trim())
                    }
                }
            } else {
                singleCaseAlert.patientMedHistList = removeKeysFromData(singleCaseAlert.patientMedHist).split(", ")

            }

            jsonField.put("${SCAJsonFieldMap.get("patientMedHist")}", singleCaseAlert.patientMedHistList)
        }
        if (singleCaseAlert.medErrorsPt != Constants.Commons.BLANK_STRING) {
            String[] medErrorPtList = singleCaseAlert.medErrorsPt?.split('\r\n')
            singleCaseAlert.medErrorPtList = []
            medErrorPtList.each {
                if (it && it.length() > 2) {
                    singleCaseAlert.medErrorPtList.add(it.substring(3, it.length()).trim())
                }
            }
            jsonField.put("${SCAJsonFieldMap.get("medErrorsPt")}", singleCaseAlert.medErrorPtList)
        }
        if (singleCaseAlert.suspProd != Constants.Commons.BLANK_STRING) {
            String[] suspProdList = singleCaseAlert.suspProd?.split('\r\n')
            singleCaseAlert.suspectProductList = []
            suspProdList.each {
                if (it && it.length() > 2) {
                    singleCaseAlert.suspectProductList.add(it.trim())
                }
            }
            jsonField.put("${SCAJsonFieldMap.get("suspProd")}", singleCaseAlert.suspectProductList)
        }
        if (singleCaseAlert.allPt != Constants.Commons.BLANK_STRING) {
            String[] allPtList = singleCaseAlert.allPt?.split('\r\n')
            singleCaseAlert.allPtList = []
            allPtList.each {
                if (it && it.length() > 2) {
                    singleCaseAlert.allPtList.add(it.substring(3, it.length()).trim())
                }
            }
            jsonField.put("${SCAJsonFieldMap.get("allPt")}", singleCaseAlert.allPtList)
        }
        if (singleCaseAlert.masterPrefTermAll != Constants.Commons.BLANK_STRING) {
            String[] allSuspPtList = singleCaseAlert.masterPrefTermAll?.split('\r\n')
            singleCaseAlert.ptList = []
            allSuspPtList.each {
                if (it && it.length() > 2) {
                    singleCaseAlert.ptList.add(it.substring(3, it.length()).trim())
                }
            }
            jsonField.put("${SCAJsonFieldMap.get("masterPrefTermAll")}", singleCaseAlert.ptList)
        }
        if (singleCaseAlert.primSuspProd != Constants.Commons.BLANK_STRING) {
            String[] primSuspProdRows = singleCaseAlert.primSuspProd?.split('\r\n')
            singleCaseAlert.primSuspProdList = []
            primSuspProdRows.each {
                singleCaseAlert.primSuspProdList.add(it.trim())
            }
            jsonField.put("${SCAJsonFieldMap.get("primSuspProd")}", singleCaseAlert.primSuspProdList)
        }
        if (singleCaseAlert.primSuspPai != Constants.Commons.BLANK_STRING) {
            String[] primSuspPaiRows = singleCaseAlert.primSuspPai?.split('\r\n')
            singleCaseAlert.primSuspPaiList = []
            primSuspPaiRows.each {
                singleCaseAlert.primSuspPaiList.add(it.trim())
            }
            jsonField.put("${SCAJsonFieldMap.get("primSuspPai")}", singleCaseAlert.primSuspPaiList)
        }
        if (singleCaseAlert.paiAll != Constants.Commons.BLANK_STRING) {
            String[] paiAllRows = singleCaseAlert.paiAll?.split('\r\n')
            singleCaseAlert.paiAllList = []
            paiAllRows.each {
                if (it && it.length() > 2) {
                    singleCaseAlert.paiAllList.add(it.substring(3, it.length()).trim())
                }
            }
            jsonField.put("${SCAJsonFieldMap.get("paiAll")}", singleCaseAlert.paiAllList)
        }
        if (singleCaseAlert.allPTsOutcome != Constants.Commons.BLANK_STRING) {
            String[] allPTsOutcomeRows = singleCaseAlert.allPTsOutcome?.split('\r\n')
            singleCaseAlert.allPTsOutcomeList = []
            allPTsOutcomeRows.each {
                if (it && it.length() > 2) {
                    singleCaseAlert.allPTsOutcomeList.add(it.substring(3, it.length()).trim())
                }
            }
            jsonField.put("${SCAJsonFieldMap.get("allPTsOutcome")}", singleCaseAlert.allPTsOutcomeList)
        }
        if (singleCaseAlert.batchLotNo != Constants.Commons.BLANK_STRING) {
            String[] batchLotNoList = singleCaseAlert.batchLotNo?.split('\r\n')
            singleCaseAlert.batchLotNoList = []
            batchLotNoList.each {
                boolean trimString = stringStartsWithNumber(it)
                if (it && it.length() > 2 && trimString) {
                    singleCaseAlert.batchLotNoList.add(it.substring(3, it.length()).trim())
                } else {
                    singleCaseAlert.batchLotNoList.add(it.trim())
                }
            }
            jsonField.put("${SCAJsonFieldMap.get("batchLotNo")}", singleCaseAlert.batchLotNoList)
        }
        if (singleCaseAlert.therapyDates != Constants.Commons.BLANK_STRING) {
            String[] therapyDateList = singleCaseAlert.therapyDates?.split('\r\n')
            singleCaseAlert.therapyDatesList = []
            therapyDateList?.each {
                if (it && it.length() > 2) {
                    if(singleCaseAlert.executedAlertConfiguration.selectedDatasource == Constants.DataSource.JADER) {
                        singleCaseAlert.therapyDatesList.add(it.trim())
                    }else{
                        singleCaseAlert.therapyDatesList.add(it.substring(3, it.length()).trim())
                    }
                }
            }
            jsonField.put("${SCAJsonFieldMap.get("therapyDates")}", singleCaseAlert.therapyDatesList)
        }
        if (singleCaseAlert.crossReferenceInd != Constants.Commons.BLANK_STRING) {
            String[] crossReferenceIndRows = singleCaseAlert.crossReferenceInd?.split('\r\n')
            singleCaseAlert.crossReferenceIndList = []
            crossReferenceIndRows.each {
                if (it && it.length() > 2) {
                    singleCaseAlert.crossReferenceIndList.add(it.substring(3, it.length()).trim())
                }
            }
            jsonField.put("${SCAJsonFieldMap.get("crossReferenceInd")}", singleCaseAlert.crossReferenceIndList)
        }
        if (singleCaseAlert.genericName != Constants.Commons.BLANK_STRING) {
            String[] genericNameRows = singleCaseAlert.genericName?.split('\r\n')
            singleCaseAlert.genericNameList = []
            genericNameRows.each {
                if (it && it.length() > 2) {
                    singleCaseAlert.genericNameList.add(it.substring(3, it.length()).trim())
                }
            }
            jsonField.put("${SCAJsonFieldMap.get("genericName")}", singleCaseAlert.genericNameList)
        }
        singleCaseAlert.jsonField = new JsonBuilder(jsonField).toPrettyString()
        //If case history exists for running configuration  then workflow items will be set from previous case history.
        Disposition prevCaseDisposition = existingCaseHistory?.currentDispositionId ?
                cacheService.getDispositionByValue(existingCaseHistory?.currentDispositionId) : null
        CaseHistory existingDispositionCaseHistory = dataObjectService.getLatestDispositionCaseHistory(config.id, caseNumber)
        if(existingDispositionCaseHistory){
            saveDispositionAlertCaseHistory(singleCaseAlert,existingDispositionCaseHistory)
        }
        Boolean resetCaseOnFollowUp = existingCaseHistory && prevCaseDisposition?.resetReviewProcess && isFollowUpExists
        if (!resetCaseOnFollowUp && existingCaseHistory && (isFollowUpExists || isPrevReportingPeriod) && !(prevCaseDisposition?.resetReviewProcess && prevCaseDisposition?.reviewCompleted)) {
            //Set the fresh workflow management values from history.
            setHistoryForAlert(existingCaseHistory, singleCaseAlert, assignedToId, assignedToGroupId)

            if (isPrevReportingPeriod) {
                singleCaseAlert.dueDate = dueDate
            }
            if (isFollowUpExists) {
                supersededAlertIds.add(existingCaseHistory.singleAlertId)
            }
        } else if (!resetCaseOnFollowUp && dispositionId && priorityId) {
            // Changes done for bug PVS-70051 as these conditions already maintained using resetCaseOnFollowUp flag
            singleCaseAlert.disposition = cacheService.getDispositionByValue(dispositionId)
            if (assignedToId) {
                singleCaseAlert.assignedTo = cacheService.getUserByUserId(assignedToId)
            } else {
                singleCaseAlert.assignedToGroup = cacheService.getGroupByGroupId(assignedToGroupId)
            }
            singleCaseAlert.priority = cacheService.getPriorityByValue(priorityId)
            singleCaseAlert.dueDate = dueDate
            if (isFollowUpExists) {
                supersededAlertIds.add(singleAlertId)
            }
        } else {
            //Set the fresh workflow management values.
            setValuesFromFreshData(singleCaseAlert, config, defaultDisposition, executedConfig)
            if (singleAlertId && prevCaseDisposition != null && resetCaseOnFollowUp) {
                saveAlertCaseHistory(singleCaseAlert,"Case moved to initial state, as new follow-up information is available"
                        ,Constants.SYSTEM_USER)
                Map caseHistoryMap = getCaseHistoryMap(singleCaseAlert)
                caseHistoryService.saveCaseHistoryForCaseReset(caseHistoryMap)
                ActivityType activityType = cacheService.getActivityTypeByValue(ActivityTypeValue.DispositionChange.value)
                User user = cacheService.getUserByUserNameIlike(Constants.SYSTEM_USER)
                Activity activity = activityService.createActivityForSingleCaseAlert(activityType, user,
                        "Disposition changed from ${prevCaseDisposition} to ${singleCaseAlert.disposition}",
                        "Case moved to initial state, as new follow-up information is available",
                        null, singleCaseAlert.productName, singleCaseAlert.pt, singleCaseAlert.assignedTo, singleCaseAlert.caseNumber,
                        singleCaseAlert.assignedToGroup)
                dataObjectService.setSCAActivityToMap(singleCaseAlert.executedAlertConfigurationId, activity)
                // PVS-59850 : Case reset scenario audit log
                signalAuditLogService.createAuditLog([
                        entityName: "Individual Case Review",
                        moduleName: "Individual Case Review",
                        category: AuditTrail.Category.UPDATE.toString(),
                        entityValue: singleCaseAlert.getInstanceIdentifierForAuditLog(),
                        username: userService.getCurrentUserName(),
                        fullname: userService.getUserFromCacheByUsername(userService.getCurrentUserName())?.fullName
                ] as Map, [[propertyName: "Disposition", oldValue: prevCaseDisposition?.displayName, newValue: singleCaseAlert?.disposition?.displayName],
                           [propertyName: "Justification", oldValue: "", newValue: "Case moved to initial state, as new follow-up information is available"]])
                isCaseReset = true
            }
        }
        if (isCaseReset) {
            singleCaseAlert.initialDisposition = prevCaseDisposition
            singleCaseAlert.initialDueDate = dueDate

        } else {
            singleCaseAlert.initialDisposition = singleCaseAlert.disposition
            singleCaseAlert.initialDueDate = singleCaseAlert.dueDate
        }

        singleCaseAlert.globalIdentity = dataObjectService.getGlobalCase(executedConfig.id, singleCaseAlert.caseId, singleCaseAlert.caseVersion)

        //set Badges for Single Case Alert
        alertService.setBadgeValueForSCA(singleCaseAlert, isPrevReportingPeriod)
        if (!config.adhocRun && !isTempCaseSeriesFlow && !isAddCase) {
            businessConfigurationService.executeRulesForSingleCaseAlert(singleCaseAlert, queryCaseMaps,
                    config.productDictionarySelection, defaultDisposition.id, productSelection,logStringBuilder)
        }

        //If the flow is for the add case then we create activity for the same as well.
        Activity activity = null
        if (isAddCase) {
            String details = "Case " + singleCaseAlert.caseNumber + " added to alert " + executedConfig.name
            activity = new Activity(
                    type: cacheService.getActivityTypeByValue(ActivityTypeValue.CaseAdded.value),
                    performedBy: currentUser,
                    details: details,
                    timestamp: new Date(),
                    justification: justification,
                    attributes: (['For Case Number': singleCaseAlert.caseNumber] as JSON).toString(),
                    suspectProduct: productName,
                    eventName: singleCaseAlert.pt,
                    assignedTo: currentUser,
                    caseNumber: singleCaseAlert.caseNumber
            )
        }

        return new NullablePair<SingleCaseAlert, Activity>(singleCaseAlert, activity)
    }

    /**
     * prepare single case alert object at the time
     * of alert creation
     * @param config
     * @param executedConfig
     * @param scaData
     * @param data
     * @param isTempCaseSeriesFlow
     * @param type
     * @return single case alert object
     */
    SingleCaseAlert saveFieldsInSingleCaseAlert(Configuration config, ExecutedConfiguration executedConfig, Map scaData, Map data,
                                                Boolean isTempCaseSeriesFlow, String type) {
        Integer timeToOnset = data[cacheService.getRptFieldIndexCache('dvProdEventTimeOnsetDays')]
        def csiSponsorStudyNumber = Holders.config.custom.qualitative.fields.enabled ? cacheService.getRptFieldIndexCache('vwcsiSponsorStudyNumber') : cacheService.getRptFieldIndexCache('crepoud_text_11')
        String parsedAttributes = new JsonBuilder(data)
        String countryColumn = Holders.config.custom.qualitative.fields.enabled ? cacheService.getRptFieldIndexCache('csiderived_country') : cacheService.getRptFieldIndexCache('masterCountryId')
        if (parsedAttributes.size() == 8000) {
            data << [extraField: ""]
            parsedAttributes = new JsonBuilder(data)
        }
        String suspProd = config?.aggAlertId ? data[cacheService.getRptFieldIndexCache('masterSuspProdList')] : data[cacheService.getRptFieldIndexCache('masterSuspProdAgg')]
        SingleCaseAlert singleCaseAlert = new SingleCaseAlert(
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
                 periodStartDate           : scaData.dateRangeStartEndDate ? scaData.dateRangeStartEndDate.get(0) : null,
                 periodEndDate             : scaData.dateRangeStartEndDate ? scaData.dateRangeStartEndDate.get(1) : null,
                 listedness                : scaData.listedness,
                 outcome                   : scaData.outcome,
                 lockedDate                : data[cacheService.getRptFieldIndexCache('masterDateLocked')] ?: data[cacheService.getRptFieldIndexCache('masterCloseDate')],
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
                 completenessScore         : generateCompletenessScore(data),
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
                 timeToOnset               : (timeToOnset || timeToOnset == 0) ? timeToOnset : Constants.Commons.BLANK_STRING,
                 caseClassification        : data[cacheService.getRptFieldIndexCache('masterCharactersticAllcs')] ?: Constants.Commons.BLANK_STRING,
                 initialFu                 : scaData.initialFu ?: checkIfInitialOrFu(data, isTempCaseSeriesFlow, scaData, type),
                 protocolNo                : Holders.config.custom.qualitative.fields.enabled? (data[cacheService.getRptFieldIndexCache('vwstudyProtocolNum')] ?: Constants.Commons.BLANK_STRING) : getProtocolNo(data),
                 isSusar                   : data[cacheService.getRptFieldIndexCache('masterSusar')] ?: Constants.Commons.BLANK_STRING,
                 therapyDates              : data[cacheService.getRptFieldIndexCache('productStartStopDateAllcs')] ?: Constants.Commons.BLANK_STRING,
                 doseDetails               : data[cacheService.getRptFieldIndexCache('productDoseDetailAllcs')] ?: Constants.Commons.BLANK_STRING,
                 preAnda                   : data[cacheService.getRptFieldIndexCache('PreAndastudyStudyNum')] ?: Constants.Commons.BLANK_STRING,
                 changes                   : data[cacheService.getRptFieldIndexCache('caseChangesConcat')] ?: Constants.Commons.BLANK_STRING,
                 primSuspProd              : data[cacheService.getRptFieldIndexCache('masterPrimProdName')] ?: Constants.Commons.BLANK_STRING,
                 primSuspPai               : data[cacheService.getRptFieldIndexCache('dciPrimSuspectPai')] ?: Constants.Commons.BLANK_STRING,
                 paiAll                    : data[cacheService.getRptFieldIndexCache('dciPaiAll')] ?: Constants.Commons.BLANK_STRING,
                 allPt                     : data[cacheService.getRptFieldIndexCache('masterPrefTermAll')] ?: Constants.Commons.BLANK_STRING,
                 genericName               : data[cacheService.getRptFieldIndexCache('masterSuspProdList')] ?: Constants.Commons.BLANK_STRING,
                 caseCreationDate          : data[cacheService.getRptFieldIndexCache('masterCreateTime')],
                 dateOfBirth               : data[cacheService.getRptFieldIndexCache('patInfoPatDobPartial')] ?: Constants.Commons.BLANK_STRING,
                 eventOnsetDate            : data[cacheService.getRptFieldIndexCache('eventPrimaryOnsetDatePartial')] ?: Constants.Commons.BLANK_STRING,
                 pregnancy                 : data[cacheService.getRptFieldIndexCache('masterPregnancyFlag')] ?: Constants.Commons.BLANK_STRING,
                 medicallyConfirmed        : data[cacheService.getRptFieldIndexCache('masterHcpFlag')] ?: Constants.Commons.BLANK_STRING,
                 allPTsOutcome             : data[cacheService.getRptFieldIndexCache('masterPrefTermList')] ?: Constants.Commons.BLANK_STRING,
                 crossReferenceInd         : data[cacheService.getRptFieldIndexCache('CrossRefIndstudyStudyNum')] ?: Constants.Commons.BLANK_STRING,
                 region                    : data[cacheService.getRptFieldIndexCache('masterCloseNotes')] ?: Constants.Commons.BLANK_STRING,
                 riskCategory              : data[cacheService.getRptFieldIndexCache('riskCategory')] ?: Constants.Commons.BLANK_STRING,
                 reporterQualification     : data[cacheService.getRptFieldIndexCache('reportersReporterType')] ?: Constants.Commons.BLANK_STRING,
                 studyBlindingStatus       : data[cacheService.getRptFieldIndexCache('ciFlagUnblind')] && data[cacheService.getRptFieldIndexCache('ciFlagUnblind')].equalsIgnoreCase(Constants.Commons.NO) ? true: false,
                 caseBlindingStatus        : data[cacheService.getRptFieldIndexCache('studyCodeUnBroken')] == 0,
                 coPackagedProduct         : data[cacheService.getRptFieldIndexCache('analytic.caseInformation.cmflagcopackproduct')] ?: Constants.Commons.BLANK_STRING,
                 reportedProductName       : scaData.reportedProductName
                ]
        )

        return singleCaseAlert
    }

    void splitDataForAdvanceFilter(def domain, Map jsonField = null){
        Map fieldAndListMap = ["indication":"indicationList","causeOfDeath":"causeOfDeathList",
                               "patientHistDrugs":"patientHistDrugsList","caseClassification":"caseClassificationList", "doseDetails":"doseDetailsList","genericName":"genericNameList"]
        Map SCAJsonFieldMap = grailsApplication.config.advancedFilter.singleCaseAlert.jsonFieldMap
        fieldAndListMap.each { k, v ->
            if (domain."${k}") {
                    domain."${v}" = domain."${k}".split(", ")
                    jsonField?.put("${SCAJsonFieldMap.get(k)}", domain."${v}")
            }
        }
    }

    String removeKeysFromData(String data){
        String result =""
        if(data) {
            List keyValList = data.split("\r\n")
            keyValList.eachWithIndex{ val, idx ->
                List list=val.split("&lt;/b&gt;")
                if(list.size() > 1)
                    result += ","+list[1]
            }
            if(result.size() >=2) {
                result = result.substring(1, result.size())
            }
        }
        result.trim()
    }

    String addNewLineInField(String data){
        if(data) {
            data = data.replaceAll("&lt;b&gt;", "&lt;/br&gt;&lt;b&gt;")?.replaceFirst("&lt;/br&gt;","")
        }
        return data
    }

    String getProtocolNo(Map data){
        String protocol = "${data[cacheService.getRptFieldIndexCache('vwstudyProtocolNum')]?data[cacheService.getRptFieldIndexCache('vwstudyProtocolNum')] +"/": Constants.Commons.BLANK_STRING}" +
                "${data[cacheService.getRptFieldIndexCache('PreAndastudyStudyNum')] ?: Constants.Commons.BLANK_STRING}"
        if(protocol != "/"){
            return protocol
        } else {
            return Constants.Commons.BLANK_STRING
        }
    }

    String checkIfInitialOrFu(Map data, Boolean isTempCaseSeriesFlow, Map scaData, String type = null) {

        if (!(isTempCaseSeriesFlow && type?.equals("CUMM"))) {
            Date initialDate = data[cacheService.getRptFieldIndexCache('ciTxtDateReceiptInitial')] ?
                    DateUtil.parseDate(data[cacheService.getRptFieldIndexCache('ciTxtDateReceiptInitial')], "dd-MMM-yyyy") :
                    DateUtil.parseDate(data[cacheService.getRptFieldIndexCache('masterInitReptDate')].toString(), "yyyy-MM-dd")
            Date lockedDate = data[cacheService.getRptFieldIndexCache('cifInitialDateLocked')] ?
                    DateUtil.parseDate(data[cacheService.getRptFieldIndexCache('cifInitialDateLocked')], "dd-MMM-yyyy") : null


            Date startDate = scaData.dateRangeStartEndDate ? scaData.dateRangeStartEndDate?.get(0) : null
            Date endDate = scaData.dateRangeStartEndDate ? scaData.dateRangeStartEndDate?.get(1) : null
            if ((initialDate <= endDate && initialDate >= startDate) || (lockedDate <= endDate && lockedDate >= startDate)) {
                return "Initial"
            } else {
                return "f/u"
            }
        }
    }

    Map getCaseHistoryMap(def singleCaseAlert) {
        [
                "configId"              : singleCaseAlert.alertConfigurationId,
                "currentDisposition"    : singleCaseAlert.disposition,
                "currentPriority"       : singleCaseAlert.priority,
                "productFamily"         : singleCaseAlert.productFamily,
                "caseNumber"            : singleCaseAlert.caseNumber,
                "caseVersion"           : singleCaseAlert.caseVersion,
                "currentAssignedTo"     : singleCaseAlert.assignedTo,
                "currentAssignedToGroup": singleCaseAlert.assignedToGroup,
                "justification"         : "Case moved to initial state, as new follow-up information is available",
                "createdBy"             : Constants.SYSTEM_USER,
                "modifiedBy"            : Constants.SYSTEM_USER,
                "updatedBy"             : Constants.SYSTEM_USER,
                "dateCreated"           : singleCaseAlert.dateCreated,
                "lastUpdated"           : singleCaseAlert.dateCreated,
                "followUpNumber"        : singleCaseAlert.followUpNumber,
                "execConfigId"          : singleCaseAlert.executedAlertConfigurationId,
                "dueDate"               : singleCaseAlert.dueDate,
                "change"                : Constants.HistoryType.DISPOSITION,
                "createdBySystem"       : Constants.SYSTEM_USER
        ]
    }

    void setPreviousActions(List<ActionDTO> actionSet, singleCaseAlert) {
        actionSet.each {
            Action action = new Action()
            User assignedUser = it.assignedToId ? cacheService.getUserByUserId(it.assignedToId) : null
            Group assignedGroup = it.assignedToGroupId ? cacheService.getGroupByGroupId(it.assignedToGroupId) : null
            action.config = cacheService.getActionConfigurationCache(it.configId)
            action.type = cacheService.getActionTypeCache(it.typeId)
            action.comments = it.comments
            action.details = it.details
            action.createdDate = it.createdDate
            if (assignedUser) {
                action.assignedTo = assignedUser
            } else if (assignedGroup) {
                action.assignedToGroup = assignedGroup
            } else {
                action.guestAttendeeEmail = it.guestAttendeeEmail
            }
            action.owner = cacheService.getUserByUserId(it.ownerId)
            action.dueDate = it.dueDate
            action.completedDate = it.completedDate
            action.alertType = it.alertType
            action.actionStatus = it.actionStatus
            action.viewed = it.viewed
            singleCaseAlert.action.add(action)
        }
    }

    Boolean getIsPrimaryInd(String primaryInd) {
        (primaryInd.contains("CLINICAL") && primaryInd.contains("TRIALS")) ||
                (primaryInd.contains("INDIVIDUAL") && primaryInd.contains("PATIENT") && primaryInd.contains("USE")) ||
                (primaryInd.contains("OTHER") && primaryInd.contains("STUDIES")) ||
                (primaryInd.contains("REPORT") && primaryInd.contains("FROM") && primaryInd.contains("ANALYSIS") && primaryInd.contains("AGGREGATE"))
    }

    String getAppTypeAndNumValue(Map data) {
        def ndaType = data[cacheService.getRptFieldIndexCache('caseProdDrugsPvrUdText20')]
        def blaType = data[cacheService.getRptFieldIndexCache('caseProdDrugsPvrUdNumber10')]
        def andaType = data[cacheService.getRptFieldIndexCache('caseProdDrugsPvrUdNumber11')]
        String appTypeAndNum = ""
        if (ndaType) {
            appTypeAndNum = ndaType
        } else if (blaType) {
            appTypeAndNum = blaType
        } else if (andaType) {
            appTypeAndNum = andaType
        }
        return appTypeAndNum
    }

/**
 * Method to create the alert based on passed alert data.
 * @param config
 * @param executedConfig
 * @param alertData
 * @param justification
 * @param isAddCase
 * @return
 */
    def createAlert(Configuration config, ExecutedConfiguration executedConfig,
                    List<Map> alertData, String justification = '', boolean isAddCase = false,
                    boolean isTempCaseSeriesFlow = false, String type = null, ExecutionStatus executionStatus = null) {
        List<String> caseMapperList = []
        ExecutedConfiguration.withNewSession {
            executedConfig = ExecutedConfiguration.get(executedConfig.id)
        }
        try {
            List<Date> dateRangeStartEndDate = executedConfig.executedAlertDateRangeInformation?.getReportStartAndEndDate()

            List<Map> caseVersionList = []
            Set productSet = []
            Map caseVersionMap = [:]
            Map advancedFilterMap = [:]
            Map<String, String> advancedFilterRptMap = [:]
            grailsApplication.config.advancedFilter.rpt.field.map.each{key, val ->
                advancedFilterRptMap.put(key,cacheService.getRptFieldIndexCache(val))
            }
            List<Long> notReviewCompletedDispositionList = cacheService.getNotReviewCompletedDisposition()*.id
            List<Long> reviewCompletedDispositionList = cacheService.getDispositionByReviewCompleted()*.id
            List<Long> prevExecConfigId = !executedConfig?.isStandalone? alertService.fetchPrevExecConfigId(executedConfig, config, false, true):[]
            List<Map> prevSingleCaseAlertList = alertService.fetchPrevPeriodSCAlerts(SingleCaseAlert, prevExecConfigId)
            List<Map> archiveSingleCaseAlertList = alertService.fetchPrevPeriodSCAlerts(ArchivedSingleCaseAlert, prevExecConfigId)

            Session session = sessionFactory.currentSession
            String sql
            List<ScaLastReviewDurationDTO> lastReviewDurationDTOList
            List<Long> prevExecConfigIdForAll = prevExecConfigId ?: alertService.fetchPrevExecConfigId(executedConfig, config, false, true)
            List<Map> alertToBeRemoved = []

            if (prevExecConfigIdForAll) {
                log.info("Fetching LastReviewDurationDTOs")
                sql = SignalQueryHelper.qual_last_review_sql(prevExecConfigIdForAll)
                lastReviewDurationDTOList = alertService.getTransformedResultList(ScaLastReviewDurationDTO.class.name, sql, session)
                log.info("Fetched LastReviewDurationDTOs")

                dataObjectService.setLastReviewDurationMap(executedConfig.id, lastReviewDurationDTOList)
                dataObjectService.setCurrentEndDateMap(executedConfig.id, executedConfig.executedAlertDateRangeInformation.dateRangeEndAbsolute)
            }

            log.info("Previous Single Case Alert List Size : ${prevSingleCaseAlertList.size()}")
            log.info("Previous Archived Single Case Alert List Size : ${archiveSingleCaseAlertList.size()}")
            List<Date> dateRange = executedConfig?.executedAlertDateRangeInformation?.getReportStartAndEndDate()
            List<Date> previousDateRange = []
            ExecutedConfiguration previousExecutedConfiguration
            if(prevExecConfigId){
                previousExecutedConfiguration = ExecutedConfiguration.get(prevExecConfigId[0])
                previousDateRange = previousExecutedConfiguration?.executedAlertDateRangeInformation?.getReportStartAndEndDate()
            }
            alertData.each {
                //For adding badges
                Integer followUpNumber = (it[cacheService.getRptFieldIndexCache('masterFupNum')]) as Integer
                followUpNumber = followUpNumber ? (followUpNumber - 1) : followUpNumber

                Map prevalertMap = prevSingleCaseAlertList.find { map ->
                    map.caseNumber == it[cacheService.getRptFieldIndexCache('masterCaseNum')]
                }

                if (!prevalertMap) {
                    prevalertMap = archiveSingleCaseAlertList.find { map ->
                        map.caseNumber == it[cacheService.getRptFieldIndexCache('masterCaseNum')]
                    }
                }
                if(prevalertMap && prevalertMap.followUpNumber == followUpNumber && (prevalertMap.dispositionId in reviewCompletedDispositionList) && (dateRange != previousDateRange)){
                    alertToBeRemoved.add(it)
                }
                if (prevalertMap && prevalertMap.followUpNumber != followUpNumber) {
                    it.put("isFollowUpExists", true)
                    prevalertMap.put("isFollowUpExists", true)
                    it.put("dueDate", prevalertMap.dueDate)
                    it.put("dispositionId", prevalertMap.dispositionId)
                    it.put("priorityId", prevalertMap.priorityId)
                    it.put("assignedToId", prevalertMap.assignedToId)
                    it.put("assignedToGroupId", prevalertMap.assignedToGroupId)
                    it.put("singleAlertId", prevalertMap.id)

                } else if (!prevalertMap) {
                    it.put("isNew", true)
                }

                if (prevalertMap && executedConfig.missedCases && prevalertMap.dispositionId in notReviewCompletedDispositionList) {
                    it.put("isPreviousReportingPeriod", true)
                    it.put("initialFu", prevalertMap.initialFu)
                }

                if (prevalertMap) {
                    it.put("dueDate", prevalertMap.dueDate)
                    it.put("dispositionId", prevalertMap.dispositionId)
                    it.put("priorityId", prevalertMap.priorityId)
                    it.put("assignedToId", prevalertMap.assignedToId)
                    it.put("assignedToGroupId", prevalertMap.assignedToGroupId)
                    it.put("singleAlertId", prevalertMap.id)
                }

                caseMapperList.add(it[cacheService.getRptFieldIndexCache('masterCaseNum')] as String)
                caseVersionMap = [caseNumber: it[cacheService.getRptFieldIndexCache('masterCaseNum')], versionNumber: it[cacheService.getRptFieldIndexCache('masterVersionNum')]]
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
                caseVersionList.add(caseVersionMap)
                productSet.add(executedConfig.productDictionarySelection == Constants.ProductSelectionTypeValue.FAMILY ? it[cacheService.getRptFieldIndexCache('productFamilyId')] : it[cacheService.getRptFieldIndexCache('productProductId')])
            }
            if(alertToBeRemoved) {
                log.info("Number of Cases review completed to be removed are : ${alertToBeRemoved.size()}")
                alertData = alertData - alertToBeRemoved
            }

            if (executedConfig.missedCases && prevSingleCaseAlertList.size()) {
                prevSingleCaseAlertList.removeAll { !(it.dispositionId in notReviewCompletedDispositionList) || it.containsKey("isFollowUpExists") }
                if (prevSingleCaseAlertList.size()) {
                    prevSingleCaseAlertList.each {
                        Map existingAlertMap = alertData.find { map ->
                            it.caseNumber == map[cacheService.getRptFieldIndexCache('masterCaseNum')]
                        }
                        if (existingAlertMap && existingAlertMap.containsKey("isPreviousReportingPeriod") && !existingAlertMap.containsKey("isFollowUpExists")) {
                            existingAlertMap.put("actionSet", null)
                        } else {
                            Map alertMap = new JsonSlurper().parseText(it.attributes) as Map
                            if(alertMap) {
                                alertMap = getTransformedMap(alertMap)
                            }
                            alertMap.put("dueDate", it.dueDate)
                            alertMap.put("dispositionId", it.dispositionId)
                            alertMap.put("priorityId", it.priorityId)
                            alertMap.put("assignedToId", it.assignedToId)
                            alertMap.put("assignedToGroupId", it.assignedToGroupId)
                            alertMap.put("singleAlertId", it.id)
                            alertMap.put('isPreviousReportingPeriod', true)
                            alertMap.put("initialFu", it.initialFu)
                            alertData.add(alertMap)
                            caseVersionList.add([caseNumber: alertMap[cacheService.getRptFieldIndexCache('masterCaseNum')], versionNumber: alertMap[cacheService.getRptFieldIndexCache('masterVersionNum')]])
                            productSet.add(executedConfig.productDictionarySelection == Constants.ProductSelectionTypeValue.FAMILY ? alertMap[cacheService.getRptFieldIndexCache('productFamilyId')] : alertMap[cacheService.getRptFieldIndexCache('productProductId')])
                        }
                    }
                }
            }
            //br started
            List<Map> queryCaseMaps = []
            boolean isCaseSeriesGenerated = false
            DashboardCountDTO dashboardCountDTO = null
            DashboardCountDTO dashboardCountDTOAddCase = null
            Integer newCounts = 0
            try {
                    if (Objects.nonNull(executionStatus)) {
                        appAlertProgressStatusService.createAppAlertProgressStatus(executionStatus, executedConfig
                                .id, Constants.AlertProgress.BUSINESS_RULES, 2, 2, System.currentTimeMillis(), AlertProgressExecutionType.BR)
                    }
                    List<BusinessConfiguration> businessConfigurationList = BusinessConfiguration.findAllByDataSourceAndIsGlobalRuleAndEnabled(Constants.DataSource.PVA, true, true)
                    dataObjectService.setEnabledBusinessConfigList(Constants.DataSource.PVA, businessConfigurationList)
                    boolean isProductGroup = config.productGroupSelection != null
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

                        dataObjectService.setEnabledBusinessConfigProductGrpList(executedConfig.id, prodGrpBusinessConfigList)
                    }

                    dataObjectService.setDefaultDispositionMap(config.id, config.getWorkflowGroup()?.defaultQualiDisposition)
                    if (!config.adhocRun && !isTempCaseSeriesFlow) {
                        log.info("Now saving the case series in mart for reporting purposes.")
                        isCaseSeriesGenerated = invokeReportingForQualAlert(executedConfig, caseVersionList, config)
                        if (isCaseSeriesGenerated) {
                            queryCaseMaps = generateCaseListQueryMetaData(config, executedConfig)
                        }
                    }

                    log.info("Fetching existing case History List")
                    List<CaseHistory> existingCaseHistoryList = CaseHistory.createCriteria().list {
                        eq("configId", config.id)
                        order("lastUpdated", "desc")
                    } as List<CaseHistory>
                    if(!config?.isStandalone) {
                        dataObjectService.setAlertExistingCaseHistoryList(config.id, existingCaseHistoryList)

                    log.info("Existing case History List fetched")
                    dashboardCountDTO = alertService.prepareDashboardCountDTO(true)
                    dashboardCountDTOAddCase = alertService.prepareDashboardCountDTO(true)
                    }

                    alertData.collate(5000).each { List<Map> alertDataMapList ->
                        CopyOnWriteArrayList<Long> supersededAlertIds = []
                        List<NullablePair<SingleCaseAlert, Activity>> singleCaseAlertAndActitvityList =
                                processAlertData(alertDataMapList, config, executedConfig,
                                        dateRangeStartEndDate, justification, isAddCase, queryCaseMaps, isTempCaseSeriesFlow, supersededAlertIds, type)

                        List<SingleCaseAlert> singleCaseAlertList = singleCaseAlertAndActitvityList.collect {
                            it.getT1()
                        }
                        List<Activity> activityList = singleCaseAlertAndActitvityList.collect { it.getT2() }
                        singleCaseAlertList.removeAll([null])
                        activityList.removeAll([null])
                        log.info("Save Single Case Alert")
                        List<SingleCaseAlert> singleCaseAlertListAddCase
                        if (isAddCase) {
                            singleCaseAlertListAddCase = SingleCaseAlert.findAllByExecutedAlertConfiguration(executedConfig)
                        }
                        if (Objects.nonNull(executionStatus)) {
                            appAlertProgressStatusService.updateAppAlertProgressStatus(executionStatus.id, executedConfig.id, Constants.AlertProgress.BUSINESS_RULES, 3, 3, System.currentTimeMillis())
                        }
                        //br ended

                        //persist started
                        try {
                            if (Objects.nonNull(executionStatus)) {
                                appAlertProgressStatusService.createAppAlertProgressStatus(executionStatus, executedConfig
                                        .id, Constants.AlertProgress.PERSIST, 2, 2, System.currentTimeMillis(), AlertProgressExecutionType.PERSIST)
                            }
                            batchPersistForAlert(singleCaseAlertList, Constants.AlertConfigType.SINGLE_CASE_ALERT)
                            List<Map> caseAlertMapping = getCaseAlertMapping(singleCaseAlertList)
                            List<Map> existingCaseAlertMapping = dataObjectService.getGlobalCaseAlertList(executedConfig.id)
                            dataObjectService.setGlobalCaseAlertMap(executedConfig?.id, caseAlertMapping + existingCaseAlertMapping)

                            if (supersededAlertIds) {
                                alertService.bulkUpdateSuperseded(supersededAlertIds)
                            }
                            if (prevExecConfigId) {
                                log.info("Now Saving the Actions")
                                insertSingleAlertActions(session, executedConfig.id, prevExecConfigId[0])
                            }
                            log.info("Now Saving the Audit log for Business Rule Actions")
                            signalAuditLogService.saveAuditTrailForBusinessRuleActions(executedConfig.id)
                            log.info("Saving the Audit log for Business Rule Actions Completed")

                            if (!config?.isStandalone) {
                                //update the existing caseHistory
                                updateExistingCaseHistoryList(config.id)

                                //persist the caseHistory
                                log.info("Save Case History")
                                persistCaseHistory(config.id)

                                //persist the activities
                                log.info("Save Activities")
                                persistSCAActivity(executedConfig.id, activityList)

                                //Prepare Dashboard Counts Map
                                if (isAddCase) {
                                    alertService.generateCountsMap(singleCaseAlertList, dashboardCountDTOAddCase)
                                    singleCaseAlertList = singleCaseAlertList + singleCaseAlertListAddCase
                                }
                                alertService.generateCountsMap(singleCaseAlertList, dashboardCountDTO)
                                newCounts += singleCaseAlertList.count { it.isNew }
                            }
                        } catch (Throwable throwable) {

                            if (Objects.nonNull(executionStatus)) {
                                appAlertProgressStatusService.updateAppAlertProgressStatus(executionStatus.id, executedConfig.id, Constants.AlertProgress.PERSIST, 1, 1, System.currentTimeMillis())
                            }
                            log.error("Alert got failed while persisting data in app schema. " + ExceptionUtils.getStackTrace(throwable))
                            throw new Exception("Alert got failed while persisting data in app schema. " + ExceptionUtils.getStackTrace(throwable))
                        }
                    }
                        try {
                            if (isTempCaseSeriesFlow && !isAddCase && !config?.isStandalone) {
                                alertService.updateOldExecutedConfigurationWithDispCounts(config, executedConfig.id, ExecutedConfiguration, dashboardCountDTO.execDispCountMap.get(executedConfig.id), newCounts)
                            }

                            if (isTempCaseSeriesFlow && isAddCase && !config?.isStandalone && dashboardCountDTO.execDispCountMap) {
                                alertService.updateExecutedConfigurationWithAddCaseDispCounts(config, executedConfig.id, ExecutedConfiguration, dashboardCountDTO.execDispCountMap.get(executedConfig.id), newCounts)
                            }

                            if (!config.adhocRun && !isTempCaseSeriesFlow) {
                                //For attaching the signal if dispostions is validatedConfirmed
                                persistValidatedSignalWithSingleCaseAlert(executedConfig.id, config.id)
                            } else {
                                log.info("Since the alert execution was on demand/temp case series thus business configuration will not be applied.")
                            }
                            dataObjectService.clearDefaultDispostion(config.id)
                            dataObjectService.clearAlertExistingCaseHistoryList(config.id)

                            //Clearing last review maps
                            dataObjectService.clearCurrentEndDateMap(executedConfig.id)
                            dataObjectService.clearLastReviewDurationMap(executedConfig.id)

                            if (alertService.isProductSecurity()) {
                                alertService.addProductInCacheForSingleAlerts(executedConfig.id, SingleCaseAlert)
                            }
                            advancedFilterMap.each { String key, List<String> value ->
                                cacheService.saveAdvancedFilterPossibleValues(key, value)
                            }
                            if (isAddCase && !config?.isStandalone) {
                                alertService.updateDashboardCounts(dashboardCountDTOAddCase, alertService.mergeCountMaps)
                            }
                            if (Objects.nonNull(executionStatus)) {
                                appAlertProgressStatusService.updateAppAlertProgressStatus(executionStatus.id, executedConfig.id, Constants.AlertProgress.PERSIST, 3, 3, System.currentTimeMillis())
                            }
                        } catch (Throwable throwable) {
                            if (Objects.nonNull(executionStatus)) {
                                appAlertProgressStatusService.updateAppAlertProgressStatus(executionStatus.id, executedConfig.id, Constants.AlertProgress.PERSIST, 1, 1, System.currentTimeMillis())
                            }
                            log.error("Alert got failed while persisting data in app schema. "+ ExceptionUtils.getStackTrace(throwable))
                            throw new Exception("Alert got failed while persisting data in app schema. "+ ExceptionUtils.getStackTrace(throwable))
                        }

            } catch (Throwable throwable) {
                if (Objects.nonNull(executionStatus)) {
                    appAlertProgressStatusService.updateAppAlertProgressStatus(executionStatus.id, executedConfig.id, Constants.AlertProgress.BUSINESS_RULES, 2, 1, System.currentTimeMillis())
                }
                List<String> errorMessages = Arrays.asList("archiving", "business", "persisting")
                if (StringUtils.isBlank(throwable.getMessage()) || !isMatchedFromException(throwable, errorMessages)) {
                    log.error("Alert got failed while saving data"+ ExceptionUtils.getStackTrace(throwable))
                    throw new Exception("Alert got failed while saving data"+throwable.toString())
                }
                log.error(throwable.getMessage() +" "+ ExceptionUtils.getStackTrace(throwable))
                throw throwable
            }
//persist end
            if (!isTempCaseSeriesFlow && !isAddCase) {
                if (isCaseSeriesGenerated) {

                    if (!config.adhocRun && !isTempCaseSeriesFlow) {
                        //saving tags for Business Configuration
                        saveTagsForBusinessConfig(executedConfig.id, executedConfig.pvrCaseSeriesId as Long)
                        dataObjectService.clearCaseTagMap(executedConfig?.id)
                        dataObjectService.clearGlobalCaseAlertMap(executedConfig?.id)
                    }
                    try {
                        log.info("Move Data to Archive Start")
                        //archieve started
                        if (Objects.nonNull(executionStatus)) {
                            appAlertProgressStatusService.createAppAlertProgressStatus(executionStatus, executedConfig
                                    .id, Constants.AlertProgress.ARCHIEVE, 2, 2, System.currentTimeMillis(), AlertProgressExecutionType.ARCHIEVE)
                        }
                        if (!config.adhocRun) {
                            archiveService.moveDatatoArchive(executedConfig, SingleCaseAlert, prevExecConfigId)
                        }

                        log.info("Move Data to Archive End")
                        alertService.updateOldExecutedConfigurationWithDispCounts(config, executedConfig.id, ExecutedConfiguration,
                                dashboardCountDTO.execDispCountMap.get(executedConfig.id), newCounts)
                    } catch (Throwable archiveEx) {
                        log.error("Alert got failed while archiving data. " + ExceptionUtils.getStackTrace(archiveEx))
                        throw new Exception("Alert got failed while archiving data. "+ archiveEx.toString())
                    }

                    //Update Dashboard Count Map
                    alertService.updateDashboardCountsForPrevAlert(prevSingleCaseAlertList, dashboardCountDTO)
                    alertService.updateDashboardCounts(dashboardCountDTO, alertService.mergeCountMaps)
                    boolean isCumCaseSeries = alertService.isCumCaseSeriesReport(executedConfig) || alertService.isCumCaseSeriesSpotfire(executedConfig)
                    boolean isGenerateReport = executedConfig.executedTemplateQueries?.size() > 0
                    boolean isSpotfire = executedConfig.spotfireSettings != null
                    Promise promise = task {
                        reportingInBackground(executedConfig.id, isCumCaseSeries)
                    }
                    promise.onError { Throwable th ->
                        updateReportStatusForError(executedConfig)
                        alertService.sendReportingErrorNotifications(executedConfig, isGenerateReport, isSpotfire)
                        dataObjectService.clearfirstVersionExecMap(executedConfig.id)
                        if(isCumCaseSeries) {
                            dataObjectService.clearCummCaseSeriesGeneratedMap(config.id)
                            dataObjectService.removeCumCaseSeriesThread(config.id)
                        }
                    }
                    promise.onComplete { boolean isReportingCompleted ->
                        if (!isReportingCompleted) {
                            updateReportStatusForError(executedConfig)
                            alertService.sendReportingErrorNotifications(executedConfig, isGenerateReport, isSpotfire)
                        }
                        dataObjectService.clearfirstVersionExecMap(executedConfig.id)
                        if(isCumCaseSeries) {
                            dataObjectService.clearCummCaseSeriesGeneratedMap(config.id)
                            dataObjectService.removeCumCaseSeriesThread(config.id)
                        }
                    }
                }
                if (Objects.nonNull(executionStatus)) {
                    appAlertProgressStatusService.updateAppAlertProgressStatus(executionStatus.id, executedConfig.id, Constants.AlertProgress.ARCHIEVE, 3, 3, System.currentTimeMillis())
                }
                updateExecutionStatus(config, isCaseSeriesGenerated)
            } else {
                log.info("Since the alert execution was temp case series thus case series will not be saved.")
            }

            String executionMessage = "Execution of Configuration took ${executedConfig.totalExecutionTime} ms for configuration ${config.name} [C:${config.id}, EC: ${executedConfig.id}]. It gave ${alertData ? alertData.size() : 0} Cases"
            log.info(executionMessage)
            log.info("Alert data save is complete.")

        } catch (ValidationException vex) {
            vex.printStackTrace()
            log.error(vex.getMessage())
            throw vex
        } catch (Throwable ex) {
            List<String> errorMessages = Arrays.asList("archiving", "business", "persisting")
            if (StringUtils.isBlank(ex.getMessage()) || !isMatchedFromException(ex, errorMessages)) {
                log.error("Alert got failed while creating single case alert. "+ ExceptionUtils.getStackTrace(ex))
                throw new Exception("Alert got failed while creating single case alert. "+ex.toString())
            }
            log.error(ExceptionUtils.getStackTrace(ex))
            throw ex
        }
        return caseMapperList
    }

    void insertSingleAlertActions(Session session, Long executedConfigId, Long prevExecConfigId) {
        // Retrieve the alert actions using a helper method
        String sqlStatement = SignalQueryHelper.single_alert_actions(executedConfigId, prevExecConfigId)
        List<Map<String, String>> alertIdAndActionIdList = session.createSQLQuery(sqlStatement)
                .list()
                .collect { row -> [col1: row[0].toString(), col2: row[1].toString(), col3: '1'] }

        // Log the start of the batch execution
        log.info("Batch execution of TABLE SINGLE_ALERT_ACTIONS started")

        // Insert the alert actions into the database
        String insertActionSingleAlertQuery = SignalQueryHelper.insert_icr_actions_sql()
        alertService.batchPersistForStringParameters(session, alertIdAndActionIdList, insertActionSingleAlertQuery, 3)

        // Log the end of the batch execution
        log.info("Batch execution of TABLE SINGLE_ALERT_ACTIONS ended")

        // Update the actions count in the alert
        log.info("Now updating the Actions Count in Alert")
        String actionCountSql = SignalQueryHelper.single_alert_actions_count(executedConfigId)
        session.createSQLQuery(actionCountSql).executeUpdate()
        log.info("Action Count For Alert updated")
    }


    void parallelyCreateSCAlerts(Map caseInfoMap) {
        log.info(">> Saving rest of the Case Series data in a Thread.")
        caseInfoMap.put('currentUser', userService.getUser())
        notify 'case.drilldown.data.prepared', caseInfoMap
    }

    def createAuditForCaseSeries(ExecutedConfiguration executedConfiguration, def alert, String aggCountType, def dataSource, def caseListSize) {
        try {
            def countMap = [
                    (com.rxlogix.Constants.BusinessConfigAttributesEvdas.NEW_EV_EVDAS)           : 'New EV',
                    (com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_EV_EVDAS)         : 'Total EV',
                    (com.rxlogix.Constants.BusinessConfigAttributesEvdas.NEW_EEA_EVDAS)          : 'New EEA',
                    (com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_EEA_EVDAS)        : 'Total EEA',
                    (com.rxlogix.Constants.BusinessConfigAttributesEvdas.NEW_HCP_EVDAS)          : 'New HCP',
                    (com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_HCP_EVDAS)        : 'Total HCP',
                    (com.rxlogix.Constants.BusinessConfigAttributesEvdas.NEW_SERIOUS_EVDAS)      : 'New Serious',
                    (com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_SERIOUS_EVDAS)    : 'Total Serious',
                    (com.rxlogix.Constants.BusinessConfigAttributesEvdas.NEW_OBS_EVDAS)          : 'New Obs',
                    (com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_OBS_EVDAS)        : 'Total Obs',
                    (com.rxlogix.Constants.BusinessConfigAttributesEvdas.NEW_FATAL_EVDAS)        : 'New Fatal',
                    (com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_FATAL_EVDAS)      : 'Total Fatal',
                    (com.rxlogix.Constants.BusinessConfigAttributesEvdas.NEW_MED_ERR_EVDAS)      : 'New Med Err',
                    (com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_MED_ERR_EVDAS)    : 'Total Med Err',
                    (com.rxlogix.Constants.BusinessConfigAttributesEvdas.NEW_PLUS_RC_EVDAS)      : 'New +RC',
                    (com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_PLUS_RC_EVDAS)    : 'Total +RC',
                    (com.rxlogix.Constants.BusinessConfigAttributesEvdas.NEW_LITERATURE_EVDAS)   : 'New Lit',
                    (com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_LITERATURE_EVDAS) : 'Total Lit',
                    (com.rxlogix.Constants.BusinessConfigAttributesEvdas.NEW_PAED_EVDAS)         : 'New Paed',
                    (com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_PAED_EVDAS)       : 'Total Paed',
                    (com.rxlogix.Constants.BusinessConfigAttributesEvdas.NEW_GERIAT_EVDAS)       : 'New Geriatr',
                    (com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_GERIAT_EVDAS)     : 'Total Geriatr',
                    (com.rxlogix.Constants.BusinessConfigAttributesEvdas.NEW_SPON_EVDAS)         : 'New Spont',
                    (com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_SPON_EVDAS)       : 'Total Spont',
                    (com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_SPON_EUROPE)      : 'Tot Spont Europe',
                    (com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_SPON_N_AMERICA)   : 'Tot Spont N America',
                    (com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_SPON_JAPAN)       : 'Tot Spont Japan',
                    (com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_SPON_ASIA)        : 'Tot Spont Asia',
                    (com.rxlogix.Constants.BusinessConfigAttributesEvdas.TOTAL_SPON_REST)        : 'Tot Spont Rest',
                    (com.rxlogix.Constants.BusinessConfigAttributes.NEW_COUNT_VAERS)             : 'New Count Vaers',
                    (com.rxlogix.Constants.BusinessConfigAttributes.CUMM_COUNT_VAERS)            : 'Cum Count Vaers',
                    (com.rxlogix.Constants.BusinessConfigAttributes.NEW_FATAL_COUNT_VAERS)       : 'New Fatal Vaers',
                    (com.rxlogix.Constants.BusinessConfigAttributes.CUM_FATAL_COUNT_VAERS)       : 'Cum Fatal Vaers',
                    (com.rxlogix.Constants.BusinessConfigAttributes.NEW_SERIOUS_COUNT_VAERS)     : 'New Ser Vaers',
                    (com.rxlogix.Constants.BusinessConfigAttributes.CUM_SERIOUS_COUNT_VAERS)     : 'Cum Ser Vaers',
                    (com.rxlogix.Constants.BusinessConfigAttributes.NEW_GERIATRIC_COUNT_VAERS)   : 'New Geria Vaers',
                    (com.rxlogix.Constants.BusinessConfigAttributes.CUM_GERIATRIC_COUNT_VAERS)   : 'Cum Geria Vaers',
                    (com.rxlogix.Constants.BusinessConfigAttributes.NEW_PAED_VAERS)              : 'New Paed Vigibase',
                    (com.rxlogix.Constants.BusinessConfigAttributes.CUM_PAED_VAERS)              : 'Cum Paed Vigibase',
                    (com.rxlogix.Constants.BusinessConfigAttributes.NEW_COUNT_VIGIBASE)          : 'New Count Vigibase',
                    (com.rxlogix.Constants.BusinessConfigAttributes.CUMM_COUNT_VIGIBASE)         : 'Cum Count Vigibase',
                    (com.rxlogix.Constants.BusinessConfigAttributes.NEW_FATAL_COUNT_VIGIBASE)    : 'New Fatal Vigibase',
                    (com.rxlogix.Constants.BusinessConfigAttributes.CUM_FATAL_COUNT_VIGIBASE)    : 'Cum Fatal Vigibase',
                    (com.rxlogix.Constants.BusinessConfigAttributes.NEW_SERIOUS_COUNT_VIGIBASE)  : 'New Ser Vigibase',
                    (com.rxlogix.Constants.BusinessConfigAttributes.CUM_SERIOUS_COUNT_VIGIBASE)  : 'Cum Ser Vigibase',
                    (com.rxlogix.Constants.BusinessConfigAttributes.NEW_GERIATRIC_COUNT_VIGIBASE): 'New Geria Vigibase',
                    (com.rxlogix.Constants.BusinessConfigAttributes.CUM_GERIATRIC_COUNT_VIGIBASE): 'Cum Geria Vigibase',
                    (com.rxlogix.Constants.BusinessConfigAttributes.NEW_COUNT_FAERS)             : 'New Count Faers',
                    (com.rxlogix.Constants.BusinessConfigAttributes.CUMM_COUNT_FAERS)            : 'Cum Count Faers',
                    (com.rxlogix.Constants.BusinessConfigAttributes.NEW_SPON_COUNT_FAERS)        : 'New Spon Faers',
                    (com.rxlogix.Constants.BusinessConfigAttributes.CUM_SPON_COUNT_FAERS)        : 'Cum Spon Faers',
                    (com.rxlogix.Constants.BusinessConfigAttributes.NEW_SERIOUS_COUNT_FAERS)     : 'New Ser Faers',
                    (com.rxlogix.Constants.BusinessConfigAttributes.CUM_SERIOUS_COUNT_FAERS)     : 'Cum Ser Faers',
                    (com.rxlogix.Constants.BusinessConfigAttributes.NEW_FATAL_COUNT_FAERS)       : 'New Fatal Faers',
                    (com.rxlogix.Constants.BusinessConfigAttributes.CUM_FATAL_COUNT_FAERS)       : 'Cum Fatal Faers',
                    (com.rxlogix.Constants.BusinessConfigAttributes.NEW_STUDY_COUNT_FAERS)       : 'New Study Faers',
                    (com.rxlogix.Constants.BusinessConfigAttributes.CUM_STUDY_COUNT_FAERS)       : 'Cum Study Faers',
                    (com.rxlogix.Constants.BusinessConfigAttributes.NEW_COUNT)                   : 'New Count',
                    (com.rxlogix.Constants.BusinessConfigAttributes.CUMM_COUNT)                  : 'Cum Count',
                    (com.rxlogix.Constants.BusinessConfigAttributes.NEW_SPON_COUNT)              : 'New Spon',
                    (com.rxlogix.Constants.BusinessConfigAttributes.CUM_SPON_COUNT)              : 'Cum Spon',
                    (com.rxlogix.Constants.BusinessConfigAttributes.NEW_SERIOUS_COUNT)           : 'New Ser',
                    (com.rxlogix.Constants.BusinessConfigAttributes.CUM_SERIOUS_COUNT)           : 'Cum Ser',
                    (com.rxlogix.Constants.BusinessConfigAttributes.NEW_FATAL_COUNT)             : 'New Fatal',
                    (com.rxlogix.Constants.BusinessConfigAttributes.CUM_FATAL_COUNT)             : 'Cum Fatal',
                    (com.rxlogix.Constants.BusinessConfigAttributes.NEW_STUDY_COUNT)             : 'New Study',
                    (com.rxlogix.Constants.BusinessConfigAttributes.CUM_STUDY_COUNT)             : 'Cum Study',
                    (com.rxlogix.Constants.BusinessConfigAttributes.POSITIVE_RE_CHALLENGE)       : 'Positive Re-challenge',
                    'newInteractingCount'                                                        : 'New Inter',
                    'cummInteractingCount'                                                       : 'Cum Inter',
                    'newGeriatricCount'                                                          : 'New Geria',
                    'cumGeriatricCount'                                                          : 'Cum Geria',
                    'newPediatricCount'                                                          : 'New Paed',
                    'cummPediatricCount'                                                         : 'Cum Paed',
                    "NEW_COUNT"                                                                  : "New Count",
                    "CUM_COUNT"                                                                  : "Cum Count",
                    "NEW_FATAL"                                                                  : "New Fatal",
                    "CUM_FATAL"                                                                  : "Cum Fatal",
                    "NEW_GER"                                                                    : "New Geria",
                    "CUM_GER"                                                                    : "Cum Geria",
                    "NEW_PEDIA"                                                                  : "New Paed",
                    "CUM_PEDIA"                                                                  : "Cum Paed",
                    'newNonSerious'                                                              : "${-> grails.util.Holders?.config?.signal?.new?.serious?.column?.header}",
                    'cumNonSerious'                                                              : "${-> grails.util.Holders?.config?.signal?.cum?.serious?.column?.header}",
                    (com.rxlogix.Constants.BusinessConfigAttributes.NEW_PROD_COUNT)              : 'New Prod Count',
                    (com.rxlogix.Constants.BusinessConfigAttributes.NEW_CUM_COUNT)               : 'Cum Prod Count']

            def auditTrailMap = [
                    entityName : 'aggregateCaseAlert',
                    moduleName : Constants.AuditLog.AGGREGATE_REVIEW + ": Case Series",
                    category   : AuditTrail.Category.INSERT.toString(),
                    entityValue: alert?.getInstanceIdentifierForAuditLog(),
                    description: "Case Series Generated for ${alert.getInstanceIdentifierForAuditLog()}",
            ]
            List<Map> auditChildMap = []
            def childEntry = [:]
            childEntry = [
                    propertyName: "Product",
                    newValue    : alert.productName]
            auditChildMap << childEntry
            childEntry = [
                    propertyName: "Event",
                    newValue    : alert.pt]
            auditChildMap << childEntry
            childEntry = [
                    propertyName: "Count Type",
                    newValue    : countMap.get(aggCountType) ?: aggCountType]
            auditChildMap << childEntry
            childEntry = [
                    propertyName: "Count",
                    newValue    : caseListSize]
            auditChildMap << childEntry
            signalAuditLogService.createAuditLog(auditTrailMap, auditChildMap)
        }catch(Exception ex){
            log.info("Some error occured while creaitng manual audit for case series generation")
            ex.printStackTrace()
        }
    }


    boolean invokeReportingForQualAlert(ExecutedConfiguration executedConfig, ArrayList<Map> caseVersionList, Configuration config) {
        reportExecutorService.clearDataMiningTables(config.id, executedConfig.id)
        alertService.saveCaseSeriesInMart(caseVersionList, executedConfig, executedConfig.pvrCaseSeriesId)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    boolean generateCaseSeriesForQualAlert(ExecutedConfiguration executedConfig, Configuration config, boolean isCumCaseSeries) {
        boolean isCaseSeriesGenerated = true
        Long seriesId = generateExecutedCaseSeries(executedConfig, false)
        if (seriesId) {
            String updateCaseSeriesHql = alertService.prepareUpdateCaseSeriesHql(executedConfig.executedAlertDateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE)
            ExecutedConfiguration.executeUpdate(updateCaseSeriesHql, [pvrCaseSeriesId: seriesId, id: executedConfig.id])
            if (isCumCaseSeries) {
                Long cumCaseSeriesId = generateExecutedCaseSeries(executedConfig, true)
                if (cumCaseSeriesId) {
                    ExecutedConfiguration.executeUpdate("Update ExecutedConfiguration set pvrCumulativeCaseSeriesId = :pvrCumulativeCaseSeriesId where id = :id", [pvrCumulativeCaseSeriesId: cumCaseSeriesId, id: executedConfig.id])
                } else {
                    isCaseSeriesGenerated = false
                }
            }
        } else {
            isCaseSeriesGenerated = false
        }
        isCaseSeriesGenerated
    }

    void saveSingleCaseAlertGlobalTags(Long executedConfigId) {
        log.info("Saving saveSingleCaseAlertGlobal Tags...")
        List<Map> scaList = getSCAListByExecId(executedConfigId)
        if (scaList) {
            List<Long> caseIdList = scaList.collect {
                it.caseId
            }
            List<Map> sgtList = getTagList(caseIdList, null, false)

            List<Map> scaTagList = []

            if (sgtList) {
                scaList.each({ Map sca ->
                    sgtList.findAll { it.caseId == sca.caseId }.each { sgt ->
                        Map duplicateTag = scaTagList.find {
                            it.alertId == sca.id.toString() && it.tagId == sgt.tagId
                        }
                        if (!duplicateTag) {
                            scaTagList.add([alertId: sca.id.toString(), id: sgt.id.toString(), tagId: sgt.tagId])
                        }
                    }
                })
                if (scaTagList) {
                    Session session = sessionFactory.currentSession
                    saveSCATag(scaTagList, session, true)
                }
            }
        }
    }

    private Map prevExecutedAlert(ExecutedConfiguration executedConfiguration) {
        Map previousExConfig = [:]
        previousExConfig = ExecutedConfiguration.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("id", "id")
                property("pvrCaseSeriesId", "pvrCaseSeriesId")
            }
            eq('name', executedConfiguration.name)
            eq('owner.id', executedConfiguration.owner.id)
            eq('type', executedConfiguration.type)
            isNotNull('pvrCaseSeriesId')
            not {
                eq('id', executedConfiguration.id)
            }
            if (reportVersions.size() > 0) {
                or {
                    reportVersions.collate(999).each {
                        'in'('numOfExecutions', it)
                    }
                }
            }
            le('id', executedConfiguration.id)
            order('id', 'desc')
            maxResults(1)
        }[0] as Map
        previousExConfig
    }

    List<Map> getTagList(List<Long> caseIdList, Long caseSeriesId, Boolean isAlertLevelTag = false) {
        SingleGlobalTag.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("id", "id")
                property("caseId", "caseId")
                property("tagId", "tagId")
                if (isAlertLevelTag) {
                    property("tagText", "tagText")
                    property("caseSeriesId", "caseSeriesId")
                    property("owner", "owner")
                    property("lastUpdated", "lastUpdated")
                }
            }
            if (caseIdList) {
                'or' {
                    caseIdList.collate(1000).each {
                        'in'('caseId', it)
                    }
                }
            }
            caseSeriesId ? eq('caseSeriesId', caseSeriesId) : isNull('caseSeriesId')
        } as List<Map>
    }

    List<Map> getSCAListByExecId(Long executedConfigId) {
        SingleCaseAlert.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("id", "id")
                property("caseId", "caseId")
            }
            eq('executedAlertConfiguration.id', executedConfigId)
        } as List<Map>
    }

    void saveSCATag(List<Map> scaAlertTagList, Session session, Boolean IsCurrentSession = false) {
        String insertAlertTagQuery = "INSERT INTO SINGLE_GLOBAL_TAG_MAPPING(SINGLE_ALERT_ID,SINGLE_GLOBAL_ID) VALUES(?,?)"

        session.doWork(new Work() {
            public void execute(Connection connection) throws SQLException {
                PreparedStatement preparedStatement = connection.prepareStatement(insertAlertTagQuery)
                def batchSize = 100
                int count = 0
                try {
                    scaAlertTagList.each {
                        preparedStatement.setString(1, it.alertId)
                        preparedStatement.setString(2, it.id)
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
                    if (!IsCurrentSession)
                        session.close()
                }
            }
        })
    }

    void saveRecords(Long pvrCaseSeriesId) {
        List<CaseSeriesTagMapping> caseSeriesTags = []
        caseSeriesTags = CaseSeriesTagMapping.withTransaction {
            CaseSeriesTagMapping.findAllByCaseSeriesExecId(pvrCaseSeriesId)
        }

        Session session
        try {
            session = sessionFactory.openSession()
            Transaction tx = session.beginTransaction()

            SingleGlobalTag singleGlobalTag
            caseSeriesTags.eachWithIndex { CaseSeriesTagMapping caseSeriesTagMapping, counter ->
                singleGlobalTag = new SingleGlobalTag(caseSeriesTagMapping)
                session.save(singleGlobalTag)
                if (counter.mod(100) == 0) {
                    //clear session and save records after every 100 records
                    session.flush()
                    session.clear()
                }
            }
            tx.commit()
        } catch (SQLException e) {
            log.error(e.getMessage())
            log.info("Exception occour while saving the Single Case Alert Tags.")
        } finally {
            session.close()
        }
    }

    void saveTagsForBusinessConfig(Long execConfigId, Long caseSeriesId) {
        Map<Long, Integer> caseIdList = dataObjectService.getCaseIdList(execConfigId)
        List<Map> tagsAndSubTags = pvsGlobalTagService.fetchTagsAndSubtags()
        List<PvsGlobalTag> pvsGlobalTagList = []
        List<PvsAlertTag> pvsAlertTagList = []
        List<CategoryDTO> categoryDtoList = []
        List tagsList = []
        List tags = []
        String dataSource = ExecutedConfiguration.get(execConfigId)?.selectedDatasource
        JsonSlurper jsonSlurper = new JsonSlurper()
        log.info("Now Saving the Tags")
        Map caseTagMap = dataObjectService.getCaseTags(execConfigId)
        caseIdList.each { caseId, versionNum ->

            tags = caseTagMap?.get(caseId as Long)

            if (tags) {
                tags.each {
                    tagsList.add(JSON.parse((jsonSlurper.parseText(it) as Map).tags))
                }
            }

            tagsList = tagsList.flatten()
            tagsList.each { tag ->

                buildTagList(tag, tagsAndSubTags, categoryDtoList, pvsGlobalTagList, pvsAlertTagList, versionNum, execConfigId, caseId, caseSeriesId)
            }

            tagsList.clear()
        }
        log.info("Category DTO Size ${categoryDtoList.size()}")
        categoryDtoList.collate(20000).each {
            CategoryUtil.saveCategories(it)
        }
        cacheService.prepareCommonTagCache()
        log.info("Category DTO completed")
        Session session = sessionFactory.currentSession
        if (pvsGlobalTagList) {
            log.info("Global Tag List Size ${pvsGlobalTagList.size()}")
            List<Map> tagCaseMap = pvsGlobalTagService.batchPersistGlobalTags(pvsGlobalTagList)
            String insertValidatedQuery = "INSERT INTO SINGLE_GLOBAL_TAGS(PVS_GLOBAL_TAG_ID,GLOBAL_CASE_ID) VALUES(?,?)"
            alertService.batchPersistForMapping(session, tagCaseMap, insertValidatedQuery)
            log.info("Global Tag list completed")

        }
        if (pvsAlertTagList) {
            log.info("Alert Tag List Size ${pvsAlertTagList.size()}")
            List<Map> tagAlertMap = pvsAlertTagService.batchPersistAlertTags(pvsAlertTagList)
            String insertValidatedQuery = "INSERT INTO SINGLE_CASE_ALERT_TAGS(PVS_ALERT_TAG_ID,SINGLE_ALERT_ID) VALUES(?,?)"
            alertService.batchPersistForMapping(session, tagAlertMap, insertValidatedQuery)
            log.info("Alert Tag list completed")
        }
        log.info("Tags are saved across the system.")

        if (caseIdList) {
            dataObjectService.clearTagsFromMap(execConfigId)
            dataObjectService.clearCaseIdList(execConfigId)
            dataObjectService.clearGlobalCaseMap(execConfigId)
            log.info("Tags Updated")
        }
    }

    void persistCaseHistory(Long configId) {
        List<CaseHistory> scaCaseHistoryList = dataObjectService.getCaseHistoryList(configId)
        if (scaCaseHistoryList) {
            alertService.batchPersistForDomain(scaCaseHistoryList, CaseHistory.class.name)
        }
        dataObjectService.clearCaseHistoryMap(configId)

    }

    void updateExistingCaseHistoryList(Long configId) {
        List<Long> existingCaseHistoryList = dataObjectService.getExistingCaseHistoryList(configId)
        if (existingCaseHistoryList) {
            existingCaseHistoryList.collate(1000).each {
                CaseHistory.executeUpdate("update CaseHistory set isLatest = false where id in (:idList)", [idList: it])
            }
        }
        dataObjectService.clearExistingCaseHistoryMap(configId)

    }

    void persistSCAActivity(Long executedConfigId, List<Activity> activityList) {
        List<Activity> scaActivityList = dataObjectService.getSCAActivityList(executedConfigId)
        if (scaActivityList) {
            activityList.addAll(scaActivityList)
        }
        if (activityList && activityList.get(0)?.typeId) {
            List<Long> activityIdList = activityService.batchPersistAlertLevelActivity(activityList)
            String insertExConfigActivityQuery = "INSERT INTO ex_rconfig_activities(EX_CONFIG_ACTIVITIES_ID,ACTIVITY_ID) VALUES(?,?)"
            alertService.batchPersistExecConfigActivityMapping(executedConfigId, activityIdList, insertExConfigActivityQuery)
        }
        dataObjectService.clearSCAActivityMap(executedConfigId)
    }

    def saveCaseSeriesToMart(alertData, ExecutedConfiguration executedConfiguration) {
        if (alertData) {
            final Sql sql = new Sql(dataSource_pva)
            String statement = "Begin "
            SimpleDateFormat sdf = new SimpleDateFormat(SqlGenerationService.DATE_FMT)
            alertData.each { data ->
                def caseId = data[cacheService.getRptFieldIndexCache('masterCaseId')]
                def versionNumber = data[cacheService.getRptFieldIndexCache('masterVersionNum')]
                statement += "INSERT INTO pvr_query_case_list (CASE_ID,CASE_SERIES_EXEC_ID,DELETED_FLAG,UPDATED_TIME, VERSION_NUM) " +
                        "VALUES (${caseId},${executedConfiguration.id}, ${0}," +
                        "TO_DATE('${executedConfiguration.lastUpdated?sdf.format(executedConfiguration.lastUpdated):''}', '${SqlGenerationService.DATETIME_FMT_ORA}')," +
                        "${versionNumber});"
            }
            statement += " END;"
            sql.execute(statement)

            String queryStatement = "Begin "
            def reportName = executedConfiguration.name
            def user = User.findByUsername(executedConfiguration.createdBy)?.id ?: 0
            queryStatement += "INSERT INTO pvr_query_info (CASE_SERIES_EXEC_ID, CASE_SERIES_ID, CASE_SERIES_NAME, CREATE_TIME, CREATE_USER_ID, CUMULATIVE_FLAG, REPORT_NAME, REVISION_NUMBER, TENANT_ID) " +
                    "VALUES (${executedConfiguration.id}, ${executedConfiguration.id}, '${reportName}'," +
                    "TO_DATE('${executedConfiguration.lastUpdated?sdf.format(executedConfiguration.lastUpdated):''}', '${SqlGenerationService.DATETIME_FMT_ORA}')," +
                    "${user}, ${1}, '${reportName}', ${0}, ${1});"
            queryStatement += " END;"
            sql.execute(queryStatement)

            sql.close()
        }
    }

    @Transactional
    boolean reportingInBackground(Long execConfigId, boolean isCumCaseSeries) {
        log.info("Reporting Started")
        boolean isReporting = true
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(execConfigId)
        if (isCumCaseSeries) {
            while (!dataObjectService.containsCummCaseSeries(executedConfiguration.configId)) {
                Thread.sleep(30000)
            }
            isReporting = dataObjectService.getCummCaseSeriesGeneratedMap(executedConfiguration.configId)
        }
        if (isReporting) {
            alertService.generateReport(executedConfiguration)
            alertService.generateSpotfireReport(executedConfiguration)
        }
        log.info("Reporting Completed")
        isReporting
    }

    void updateExecutionStatus(Configuration configuration, boolean isCaseSeriesGenerated) {
        ExecutionStatus executionStatus = ExecutionStatus.findByConfigIdAndReportVersionAndType(configuration.id,
                configuration.isResume ? configuration.numOfExecutions : configuration.numOfExecutions + 1, configuration.type)
        if (!isCaseSeriesGenerated) {
            executionStatus?.executionStatus = ReportExecutionStatus.ERROR
            executionStatus?.stackTrace = "Error occurred while saving the case series"
            if (configuration.templateQueries.size() > 0)
                executionStatus?.reportExecutionStatus = ReportExecutionStatus.ERROR
            if (configuration.spotfireSettings)
                executionStatus?.spotfireExecutionStatus = ReportExecutionStatus.ERROR
        } else {
            executionStatus?.executionStatus = ReportExecutionStatus.COMPLETED
            executionStatus?.executionLevel = executionStatus.executionLevel + 1
        }
        executionStatus?.save()
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void updateReportStatusForError(ExecutedConfiguration executedConfiguration) {
        ExecutionStatus executionStatus = ExecutionStatus.findByExecutedConfigIdAndType(executedConfiguration.id, executedConfiguration.type)
        SQLQuery sql = null
        Boolean isReport = false
        Boolean isSpotfire = false

        Configuration configuration = Configuration.get(executedConfiguration.configId)
        String updateQuery = "update ex_status set"

        if (configuration.templateQueries.size() > 0 && executionStatus?.reportExecutionStatus != ReportExecutionStatus.COMPLETED) {
            isReport = true
            updateQuery = updateQuery + " REPORT_EXECUTION_STATUS = '" + ReportExecutionStatus.ERROR
        }
        if (configuration.spotfireSettings && executionStatus?.spotfireExecutionStatus != ReportExecutionStatus.COMPLETED) {
            isSpotfire = true
            updateQuery = updateQuery + (isReport ? "', " : " ") + "SPOTFIRE_EXECUTION_STATUS = '" + ReportExecutionStatus.ERROR

        }

        if (isReport || isSpotfire) {
            updateQuery = updateQuery + "' where id = " + executionStatus.id
            log.info(updateQuery)
            Session session = sessionFactory.currentSession
            try {
                sql = session.createSQLQuery(updateQuery)
                sql.executeUpdate()
                session.clear()
                session.flush()
            } catch (Exception ex) {
                log.info(ex.printStackTrace())
            }
        }
    }

    Long generateExecutedCaseSeries(ExecutedConfiguration executedConfiguration, boolean isTemporary, boolean isCumulative = false, String selectedDatasource='pva') {
        Long caseSeriesId
        ExecutedCaseSeriesDTO executedCaseSeriesDTO = populateCSDTO(executedConfiguration, isTemporary, isCumulative, selectedDatasource)
        String url = Holders.config.pvreports.url
        String path = Holders.config.pvreports.caseSeries.generation.uri
        Map response = reportIntegrationService.postData(url, path, executedCaseSeriesDTO)
        if(response.status == 500){
            throw new Exception("Alert failed due to PV Reports inaccessibility.")
        }
        log.info("Case Series API Response from PVR : ${response}")
        if (response.status == HttpStatus.SC_OK) {
            if (response.result.status) {
                caseSeriesId = response.result.data as Long
            }
        }
        caseSeriesId
    }

    boolean generateCummCaseSeries(Long configId, Long execConfigId) {
        boolean isCaseSeriesGenerated = false
        Configuration.withNewSession {
            Configuration configuration = Configuration.get(configId)
            ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(execConfigId)
            isCaseSeriesGenerated = reportExecutorService.generateAlertResultQualitative(configuration, executedConfiguration, null, false, true)
        }
        isCaseSeriesGenerated
    }

    @Transactional
    ExecutedCaseSeriesDTO populateCSDTO(ExecutedConfiguration executedConfiguration, boolean isTemporaryCS = false, boolean isCumulative = false, String selectedDatasource = 'pva') {
        ExecutedCaseSeriesDTO executedCaseSeriesDTO = new ExecutedCaseSeriesDTO()
        Configuration config = Configuration.get(executedConfiguration.configId)
        Set<String> users = config.getShareWithUsers()?.collect{it?.username}.findAll {it != null}
        Set<String> groups= config.getShareWithGroups()?.collect{it?.name}.findAll {it != null}
        Set<String> autoSharedUsers = []
        Set<String> autoSharedGroups = []
        selectedDatasource = (executedConfiguration.type == SINGLE_CASE_ALERT) ? config?.selectedDatasource : selectedDatasource
        Boolean isDeletedCases = selectedDatasource == Constants.DataSource.PVA ? sqlGenerationService.isDeletedCasesFlagEnabled(selectedDatasource) : false
        if(config.autoShareWithGroup || config.autoShareWithUser){
           autoSharedUsers = config.autoShareWithUser?.collect{it?.username}.findAll {it != null}
           autoSharedGroups = config.autoShareWithGroup?.collect{it?.name}.findAll {it != null}
        }
        if(autoSharedUsers && autoSharedUsers.size() > 0){
            users += autoSharedUsers
        }
        if(autoSharedGroups && autoSharedGroups.size() > 0) {
            groups += autoSharedGroups
        }
        ExecutedDateRangeInfoDTO executedCaseSeriesDateRangeInformationDTO = new ExecutedDateRangeInfoDTO()
        executedCaseSeriesDateRangeInformationDTO.dateRangeStartAbsolute = executedConfiguration.executedAlertDateRangeInformation.dateRangeStartAbsolute
        executedCaseSeriesDateRangeInformationDTO.dateRangeEndAbsolute = executedConfiguration.executedAlertDateRangeInformation.dateRangeEndAbsolute
        executedCaseSeriesDateRangeInformationDTO.dateRangeEnum = isCumulative ? DateRangeEnum.CUMULATIVE : executedConfiguration.executedAlertDateRangeInformation.dateRangeEnum
        executedCaseSeriesDateRangeInformationDTO.relativeDateRangeValue = isCumulative ? 1 : executedConfiguration.executedAlertDateRangeInformation.relativeDateRangeValue
        List<QueryValueListDTO> executedGlobalQueryValueDTOLists = []
        executedCaseSeriesDTO.with {
            seriesName = executedConfiguration.name
            description = executedConfiguration.description
            dateRangeType = executedConfiguration.dateRangeType.value()
            //Setting asOfVersionDate as null bcoz PVR report engine with expect asOfversionDate as null when Evaluate Case date on is selected as LATEST_VERSION. -PS
            asOfVersionDate = executedConfiguration.evaluateDateAs == EvaluateCaseDateEnum.LATEST_VERSION ? null : executedConfiguration.asOfVersionDate
            evaluateDateAs = executedConfiguration.evaluateDateAs
            excludeFollowUp = executedConfiguration.excludeFollowUp
            includeLockedVersion = executedConfiguration.includeLockedVersion
            excludeNonValidCases = false
            productSelection = executedConfiguration.productSelection
            studySelection = executedConfiguration.studySelection
            eventSelection = executedConfiguration.eventSelection
            ownerName = executedConfiguration.createdBy
            globalQueryId = executedConfiguration.executedAlertQueryId
            suspectProduct = executedConfiguration.suspectProduct
            executedCaseSeriesDateRangeInformation = executedCaseSeriesDateRangeInformationDTO
            executedGlobalQueryValueLists = executedGlobalQueryValueDTOLists
            callbackURL = grailsLinkGenerator?.link(base: grailsApplication.config.signal.serverURL, controller: 'singleCaseAlert', action: 'caseSeriesCallback', params: [id: executedConfiguration.id])
            callbackURL += "/${isCumulative}"
            isTemporary = isTemporaryCS
            isMultiIngredient = executedConfiguration.isMultiIngredient
            includeWHODrugs = executedConfiguration.includeWHODrugs
        }
        if(users.size()>0)
            executedCaseSeriesDTO.sharedWithUsers = users as List
        if(groups.size()>0)
            executedCaseSeriesDTO.sharedWithGroups = groups as List
        List<ParameterValueDTO> parameterValueDTOList = []
        executedConfiguration.executedAlertQueryValueLists.each {
            parameterValueDTOList = []
            it.parameterValues.each {
                if (it.hasProperty('reportField')) {
                    parameterValueDTOList << new ParameterValueDTO(key: it.key,
                            reportFieldName: it.reportField.name, operator: it.operator, value: it.value)
                } else {
                    parameterValueDTOList << new ParameterValueDTO(key: it.key, value: it.value)
                }
            }
            executedGlobalQueryValueDTOLists << new QueryValueListDTO(queryId: it.query, parameterValues: parameterValueDTOList)
        }
        if (executedConfiguration.excludeNonValidCases) {
            String nonValidQueryName = Holders.config.pvreports.nonValidQueryName.quan
            if (executedConfiguration.type == SINGLE_CASE_ALERT) {
                nonValidQueryName = Holders.config.pvreports.nonValidQueryName.qual
            }
            SuperQueryDTO nonValidQuery = queryService.queryDetailByName(nonValidQueryName)
            if (nonValidQuery) {
                executedGlobalQueryValueDTOLists << new QueryValueListDTO(queryId: nonValidQuery.id, parameterValues: [])
            }
        } else if(isDeletedCases){
            String nonValidQueryName = Holders.config.pvreports.nonValidQueryName.latestVersion
            if(executedConfiguration.evaluateDateAs ==  EvaluateCaseDateEnum.VERSION_ASOF){
                nonValidQueryName =  Holders.config.pvreports.nonValidQueryName.versionAsOf
            }
            SuperQueryDTO nonValidQuery = queryService.queryDetailByName(nonValidQueryName)
            if(nonValidQuery){
                executedGlobalQueryValueDTOLists << new QueryValueListDTO(queryId: nonValidQuery.id, parameterValues: [])
            }
        }
        executedCaseSeriesDTO
    }

    List<Map> generateCaseListQueryMetaData(config, ExecutedConfiguration execConfig) {
        List<Map> productQueryList = []
        Map queryMap = businessConfigurationService.getQueryFromRules(execConfig.id, config)
        List<Long> queryList = queryMap.queryList
        List<RuleInformation> ruleList = queryMap.ruleList
        try {
            if (queryList) {
                List<SuperQueryDTO> superQueryDTOS = queryService.queryListDetail(queryList)
                log.info("Fetching data for query from business Rule")
                superQueryDTOS.each { SuperQueryDTO superQuery ->
                    reportExecutorService.clearDataMiningTables(config.id, execConfig.id)
                    List<Long> resultCaseList = reportExecutorService.generateAlertResultQualitative(config, execConfig, superQuery, false, false, execConfig.pvrCaseSeriesId)
                    Map queryResultMap = [query: superQuery.id, caseList: resultCaseList]
                    productQueryList.add(queryResultMap)
                }
            }
            ruleList.each { rule ->
                reportExecutorService.clearDataMiningTables(config.id, execConfig.id)
                SuperQueryDTO superQuery = sqlGenerationService.prepareSuperQueryDTO(rule.ruleJSON, QueryTypeEnum.SET_BUILDER)
                List<Long> resultCaseList = reportExecutorService.generateAlertResultQualitative(config, execConfig, superQuery, false, false, execConfig.pvrCaseSeriesId)
                Map queryResultMap = [rule: rule.id, caseList: resultCaseList]
                productQueryList.add(queryResultMap)
            }
           reportExecutorService.clearDataMiningTables(config.id, execConfig.id)
            log.info("Data Fetched for business Rule Query")
        } catch (Exception ex) {
            ex.printStackTrace()
            log.error("Error while fetching the business configuration query")
        }
        productQueryList
    }

    private void setHistoryForAlert(CaseHistory existingCaseHistory, SingleCaseAlert singleCaseAlert, def assignedToId, def assignedToGroupId) {

        singleCaseAlert.disposition = cacheService.getDispositionByValue(existingCaseHistory.currentDispositionId)
        if (assignedToId) {
            singleCaseAlert.assignedTo = cacheService.getUserByUserId(assignedToId)
        } else {
            singleCaseAlert.assignedToGroup = cacheService.getGroupByGroupId(assignedToGroupId)
        }
        singleCaseAlert.priority = cacheService.getPriorityByValue(existingCaseHistory.currentPriorityId)
        singleCaseAlert.dueDate = existingCaseHistory.dueDate
    }

/**
 * Sets the value to the single case alert as fresh data.
 * @param sca
 * @param config
 */
    @Transactional
    private void setValuesFromFreshData(SingleCaseAlert sca, Configuration config, Disposition disposition, ExecutedConfiguration execConfig) {
        sca.disposition = disposition
        if (!execConfig.isAutoAssignedTo) {
            sca.assignedTo = config.assignedTo
            sca.assignedToGroup = config.assignedToGroup
        } else {
            sca.assignedTo = execConfig.assignedTo
            sca.assignedToGroup = execConfig.assignedToGroup
        }
        sca.priority = config.priority ?: Priority.findByDefaultPriority(true) //done for adhoc alert due to null priority (PVS-54684 priority removal from adhoc alerts)
        calcDueDate(sca, config.priority, sca.disposition, false,
                cacheService.getDispositionConfigsByPriority(sca.priority.id))
    }


//This will update the states on Alert Level
    def updateSingleCaseAlertStates(def singleCaseAlert, Map map) {
        switch (map.change) {
            case Constants.HistoryType.DISPOSITION:
                singleCaseAlert.disposition = map.currentDisposition
                break
            case Constants.HistoryType.ASSIGNED_TO:
                singleCaseAlert.assignedTo = map.currentAssignedTo
                singleCaseAlert.assignedToGroup = map.currentAssignedToGroup
                break
            case Constants.HistoryType.PRIORITY:
                singleCaseAlert.priority = map.currentPriority
                calcDueDate(singleCaseAlert, singleCaseAlert.priority, singleCaseAlert.disposition, false,
                        cacheService.getDispositionConfigsByPriority(singleCaseAlert.priority.id))
                break
        }
        singleCaseAlert.followUpExists = false
        singleCaseAlert.save(flush: true)
    }

//Return List of JSON Objects with Tag Names
    def getAlertTagNames(def alert) {
        List tagNames = alert?.alertTags?.name?.unique()
        List globalTagNames = alert?.tags?.tagText?.unique()
        List tagList = []
        Map tagObj = [:]
        tagNames.each { tagName ->
            tagObj = ["name": tagName]
            tagList.add(tagObj as JSON)
        }
        globalTagNames.each { globalTagName ->
            tagObj = ['name': globalTagName]
            tagList.add(tagObj as JSON)
        }
        tagList as String
    }

    def overdueGroup() {
        def rawData = groupData()
        ['Priority Low', 'Priority Medium', 'Priority High'].collect {
            if (it.toLowerCase().contains('low')) {
                [name                  : it, data: rawData['Low'] ? rawData['Low'].values()?.collect {
                    it.values()
                }.flatten() : [], color: '#6fbe44']
            } else if (it.toLowerCase().contains('high')) {
                [name                  : it, data: rawData['High'] ? rawData['High'].values()?.collect {
                    it.values()
                }.flatten() : [], color: '#ed561b']
            } else {
                [name                  : it, data: rawData['Medium'] ? rawData['Medium'].values()?.collect {
                    it.values()
                }.flatten() : [], color: '#FFCC00']
            }
        }
    }

    def groupData() {
        def data = [:]

        SingleCaseAlert.overdueGroups().collect {
            def value = data[it[1]]
            def grpData = null
            if (!value) {
                value = [:]
                data[it[1]] = value
            }
            grpData = value[it[0]]
            if (!grpData) {
                grpData = [:]
                value[it[0]] = grpData
            }
            def v = grpData[it[0]]
            if (!v) v = 0
            grpData[it[0]] = v + it[2]
        }

        data
    }

    def listSelectedAlerts(String alerts, def domain, Boolean isCaseFormExport = false) {
        //Added code for pii policy
        DbUtil.piiPolicy(sessionFactory.currentSession)
        //end
        String[] alertList = alerts.split(",")
        List<Long> alertLisId = alertList.collect{it as Long}
        domain.createCriteria().list{
            if(isCaseFormExport) {
                resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                projections {
                    property('caseNumber', 'caseNumber')
                    property('caseVersion', 'caseVersion')
                    property('id', 'alertId')
                    property('followUpNumber', 'followUpNumber')
                    property('isDuplicate', 'isDuplicate')
                    property('caseId', 'caseId')
                }
            }
            or {
                alertLisId.collate(999).each {
                    'in'('id', it)
                }
            }
        }
    }

    def getDomainObject(Boolean isArchived) {
        isArchived ? ArchivedSingleCaseAlert : SingleCaseAlert
    }


    List<Map> getSingleCaseAlertList(List list, AlertDataDTO alertDataDTO, String callingScreen = null, Boolean isArchived = false) {

        List scaList = []
        if (list) {
                Disposition defaultQualiDisposition = userService.getUser().workflowGroup.defaultQualiDisposition
                cacheService.setDefaultDisp(Constants.AlertConfigType.SINGLE_CASE_ALERT, defaultQualiDisposition.id as Long)
                List<Long> alertIdList = list.collect { it.id }
                List<Long> configIds = list.alertConfiguration.id.unique()
                List<Long> execConfigIdList = list.collect { it.executedAlertConfigurationId }
                List<Map> caseAndversionList = list.collect { [caseId: it.caseId, versionNum: it.caseVersion] }
                List<Map> alertValidatedSignalList = validatedSignalService.getAlertValidatedSignalList(alertIdList, alertDataDTO.domainName)
                alertDataDTO.isFaers = Boolean.valueOf(alertDataDTO.params.isFaers)?:false
                List alertCommentList = []
                if(Holders.config.icr.comments.enable){
                    alertCommentList = alertCommentService.getGlobalCommentByCaseList(caseAndversionList, alertDataDTO.isFaers, true)
                }
                List<Map> allAlertTagsList = pvsAlertTagService.getAllAlertSpecificTags(alertIdList, Constants.AlertConfigType.SINGLE_CASE_ALERT)
                List<Map> allGlobalTagsList = pvsGlobalTagService.getAllGlobalTags(list.collect {
                    it.globalIdentityId
                }, Constants.AlertConfigType.SINGLE_CASE_ALERT, callingScreen)
                List<Long> undoableAlertIdList =  isArchived? [] : (undoableDispositionService.getUndoableAlertList(alertIdList, Constants.AlertConfigType.SINGLE_CASE_ALERT))

            ExecutorService executorService = signalExecutorService.threadPoolForQualListExec()
                List<Callable<Map>> callables = new ArrayList<Callable<Map>>()

                list.each { def sca ->
                    callables.add(new Callable<Map>() {
                        @Override
                        Map call() throws Exception {
                            List<Map> validatedSignals = alertValidatedSignalList.findAll {
                                it.id == sca.id
                            }?.collect { [name: it.name + "(S)", signalId: it.signalId,disposition: it.disposition] }

                            String comment = alertCommentList.find {
                                sca.caseId == it.caseId
                            }?.comments ?: null

                            Boolean isAttached = sca?.attachments as boolean

                            Map caseData = sca?.composeAlert(alertDataDTO.timeZone, validatedSignals, alertDataDTO.isFromExport, comment, isAttached, null, undoableAlertIdList.contains(sca?.id) ?: false)
                            List<Map> caseSeriesTags = allAlertTagsList.findAll { it.alertId == caseData.id }
                            List<Map> globalTags = allGlobalTagsList.findAll { it.globalId == caseData.globalId }
                            globalTags = globalTags.unique(false) { a, b ->
                                a.tagText <=> b.tagText
                            }
                            List<Map> caseSeriesAndGlobalTags = caseSeriesTags + globalTags
                            caseData.alertTags = caseSeriesAndGlobalTags.sort{ it.tagText?.toLowerCase()}
                            return caseData
                        }
                    })
                }

                List<Future<Map>> futures = executorService.invokeAll(callables)
                futures.each {

                    try {

                        if (it.isDone() && !it.get().isEmpty()) {

                            scaList.add(it.get())
                        }
                    } catch (Exception e) {

                        e.printStackTrace()
                    }
                }

            }
        cacheService.removeDefaultDisp(Constants.AlertConfigType.SINGLE_CASE_ALERT)
        scaList
    }

    /**
     * converts single case alert data fetched from sql into
     * the fromat that is required for the UI
     * @param list
     * @param alertDataDTO
     * @param callingScreen
     * @param isArchived
     * @return a list of map containg single case alert data
     */

    List<Map> getSingleCaseAlertListSql(List list, AlertDataDTO alertDataDTO, String callingScreen = null, Boolean isArchived = false) {

        List scaList = []
        if (list) {
            Disposition defaultQualiDisposition = userService.getUser().workflowGroup.defaultQualiDisposition
            cacheService.setDefaultDisp(Constants.AlertConfigType.SINGLE_CASE_ALERT, defaultQualiDisposition.id as Long)
            List<Long> alertIdList = list.collect { it.ids as Long }
            List<Map> caseAndversionList = list.collect {
                [caseId: it.caseid as Long, versionNum: it.caseversion as Integer]
            }
            List<Map> alertValidatedSignalList = validatedSignalService.getAlertValidatedSignalList(alertIdList, alertDataDTO.domainName)
            alertDataDTO.isFaers = Boolean.valueOf(alertDataDTO.params.isFaers) ?: false
            def alertCommentList = alertCommentService.getGlobalCommentByCaseList(caseAndversionList, alertDataDTO.isFaers, true)
            List<Map> allAlertTagsList = pvsAlertTagService.getAllAlertSpecificTags(alertIdList, Constants.AlertConfigType.SINGLE_CASE_ALERT)
            List<Map> allGlobalTagsList = pvsGlobalTagService.getAllGlobalTags(list.collect {
                it.globalidentity as Long
            }, Constants.AlertConfigType.SINGLE_CASE_ALERT, callingScreen)
            println '----------------------------------------------------------------------------------------------------------------------------------'
            println 'allGlobalTagsList'
            println allGlobalTagsList
            println '----------------------------------------------------------------------------------------------------------------------------------'
            List<Long> undoableAlertIdList = isArchived ? [] : (undoableDispositionService.getUndoableAlertList(alertIdList, Constants.AlertConfigType.SINGLE_CASE_ALERT))
            ExecutorService executorService = signalExecutorService.threadPoolForQualListExec()
            List<Callable<Map>> callables = new ArrayList<Callable<Map>>()
            list.each { def sca ->
                callables.add(new Callable<Map>() {
                    @Override
                    Map call() throws Exception {
                        List<Map> validatedSignals = alertValidatedSignalList.findAll {
                            it.id == sca.ids
                        }?.collect { [name: it.name + "(S)", signalId: it.signalId, disposition: it.disposition] }
                        String comment = alertCommentList.find {
                            sca.caseid as Long == it.caseId
                        }?.comments ?: null
                        Boolean isAttached = sca?.attachments as boolean
                        Map caseData = composeAlert(alertDataDTO.timeZone, validatedSignals, alertDataDTO.isFromExport, comment, isAttached, null, undoableAlertIdList.contains(sca?.ids as Long) ?: false, sca)
                        List<Map> caseSeriesTags = allAlertTagsList.findAll { it.alertId == caseData.id }
                        List<Map> globalTags = allGlobalTagsList.findAll { it.globalId == caseData.globalId }
                        globalTags = globalTags.unique(false) { a, b ->
                            a.tagText <=> b.tagText
                        }
                        List<Map> caseSeriesAndGlobalTags = caseSeriesTags + globalTags
                        caseData.alertTags = caseSeriesAndGlobalTags.sort { it.tagText?.toLowerCase() }
                        return caseData
                    }
                })
            }
            List<Future<Map>> futures = executorService.invokeAll(callables)
            futures.each {
                try {
                    if (it.isDone() && !it.get().isEmpty()) {
                        scaList.add(it.get())
                    }
                } catch (Exception e) {
                    e.printStackTrace()
                }
            }
        }
        cacheService.removeDefaultDisp(Constants.AlertConfigType.SINGLE_CASE_ALERT)
        scaList
    }

    List<Long> getAlertIdsForAttachments(Long alertId, boolean isArchived = false){
        def domain = alertService.generateDomainName(isArchived)
        List<Long> scaList = []
        List<Long> archivedAlertIds = []
        def scaAlert
        ArchivedSingleCaseAlert.withTransaction {
            scaAlert = domain.findById(alertId.toInteger())
            archivedAlertIds = ExecutedConfiguration.findAllByConfigId(scaAlert.alertConfiguration.id).collect {
                it.id
            }
            scaList = ArchivedSingleCaseAlert.createCriteria().list {
                projections {
                    property('id')
                }
                eq('caseNumber', scaAlert?.caseNumber)
                if (archivedAlertIds) {
                    or {
                        archivedAlertIds.collate(999).each { alertIds ->
                            'in'('executedAlertConfiguration.id', alertIds)
                        }
                    }
                }
            } as List<Long>

            archivedAlertIds = scaList.findAll {
                ArchivedSingleCaseAlert.get(it).executedAlertConfiguration.id < scaAlert.executedAlertConfiguration.id
            }
        }
        archivedAlertIds + scaAlert.id
    }

    def fetchPreviousAlertsListSCA(List prevExecConfigIdList){
        def prevAlertsList
        ArchivedSingleCaseAlert.withTransaction {
            prevAlertsList = ArchivedSingleCaseAlert.createCriteria().list {
                resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                projections {
                    property('id','id')
                    property('caseNumber','caseNumber')
                }
                if (prevExecConfigIdList){
                    or {
                        prevExecConfigIdList.collate(1000).each {
                            'in'("executedAlertConfiguration.id", it)
                        }
                    }
                }
            } as List<Long>
        }
        return prevAlertsList
    }

    boolean checkAttachmentsForAlert(List<Long> alertIds){
        boolean  isAttached = false
        ArchivedSingleCaseAlert.withTransaction {
            alertIds.each { Long scaAlertId ->
                def scaAlert = ArchivedSingleCaseAlert.get(scaAlertId) ?: SingleCaseAlert.get(scaAlertId)
                if (scaAlert.attachments)
                    isAttached = true
            }
        }
        isAttached
    }

    /**
     * converts single case alert data fetched from sql into
     * the fromat that is required for the export
     * @param list
     * @param alertDataDTO
     * @param callingScreen
     * @param isArchived
     * @return a list of map containg single case alert data
     */

    List<Map> getSingleCaseAlertListForExport(List<Map> list, AlertDataDTO alertDataDTO, String callingScreen = null) {
        List scaList = []
        String dateFormat = "yyyy-MM-dd HH:mm:ss.SSS"
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat)
        if (list) {
            List<Long> alertIdList = list.collect { it.ids as Long }
            List<Map> caseAndversionList = list.collect {
                [caseId: it.caseid as Long, versionNum: it.caseversion as Integer]
            }
            List<Map> alertValidatedSignalList = validatedSignalService.getAlertValidatedSignalList(alertIdList, alertDataDTO.domainName)
            alertDataDTO.isFaers = Boolean.valueOf(alertDataDTO.params.isFaers) ?: false
            def alertCommentList = alertCommentService.getGlobalCommentByCaseList(caseAndversionList, alertDataDTO.isFaers, false)
            List<Map> allAlertTagsList = pvsAlertTagService.getAllAlertSpecificTags(alertIdList, Constants.AlertConfigType.SINGLE_CASE_ALERT)
            List<Map> allGlobalTagsList = pvsGlobalTagService.getAllGlobalTags(list.collect {
                it.globalidentity as Long
            }, Constants.AlertConfigType.SINGLE_CASE_ALERT, callingScreen)
            String userTimeZone = userService.getCurrentUserPreference()?.timeZone
            ExecutorService executorService = Executors.newFixedThreadPool(10)
            List<Future> futureList = list.collect { sca ->
                executorService.submit({ ->

                    List<Map> validatedSignals = alertValidatedSignalList.findAll {
                        it.id == sca.ids
                    }?.collect { [name: it.name + "(S)", signalId: it.signalId] }

                    String comment = alertCommentList.find {
                        sca.caseid == it.caseId
                    }?.comments ?: null

                    Map caseData = [:]
                    Boolean isJader = alertDataDTO.params?.isJader == "true" ? true : false
                    sca.each { key, value ->
                        Boolean isReviewCompleted = cacheService.getDispositionByValue(sca.disposition as Long)?.isReviewCompleted()
                        caseData.put(columnNames[key], unescapeHtml(generateValueForExport(isJader, columnNames[key], value, sdf,userTimeZone)))
                        if (isReviewCompleted)
                            caseData.put("dueDate", "")
                    }
                    //Corrected code below.
                    Boolean isSafety = alertDataDTO.params?.isSafety == "true" ? true : false
                    Integer followupNumber = 0
                    //updated for PVS-56935
                    if (!isSafety) {
                        caseData.caseNumber = sca.caseNumber
                    } else {
                        followupNumber = sca?.followupnumber != null ? sca?.followupnumber as Integer : 0
                        caseData.caseNumber = getExportCaseNumber(sca.casenumber as String, followupNumber)
                    }
                    caseData.allPt = sca.allpt
                    caseData.assignedToUser = sca.assignedto ? cacheService.getUserByUserId(sca.assignedto as Long).fullName : cacheService.getGroupByGroupId(sca.assignedtogroup as Long).name
                    caseData.signalsAndTopics = validatedSignals.collect { it.name }?.join(",")
                    caseData.disposition = cacheService.getDispositionByValue(sca.disposition as Long)?.displayName
                    caseData.priority = cacheService.getPriorityByValue(sca.priority as Long).displayName
                    caseData.comments = comment
                    caseData.currentDisposition = cacheService.getDispositionByValue(sca.disposition as Long)?.displayName
                    caseData.primSuspProd = sca.primsuspprod
                    List<Map> caseSeriesTags = allAlertTagsList.findAll { it.alertId == sca.ids as Long }
                    List<Map> globalTags = allGlobalTagsList.findAll { it.globalId == sca.globalidentity as Long }
                    globalTags = globalTags.unique(false) { a, b ->
                        a.tagText <=> b.tagText
                    }
                    List<Map> caseSeriesAndGlobalTags = caseSeriesTags + globalTags
                    caseData.alertTags = caseSeriesAndGlobalTags.sort { it.tagText?.toLowerCase() }
                    List<String> tagsList = []
                    caseData.alertTags.each { tag ->
                        String tagString = ""
                        if (tag.subTagText == null) {
                            tagString = tagString + tag.tagText + tag.privateUser + tag.tagType
                        } else {
                            String subTags = tag.subTagText.split(";").join("(S);")
                            tagString = tagString + tag.tagText + tag.privateUser + tag.tagType + " : " + subTags + "(S)"
                        }
                        tagsList.add(tagString)

                    }
                    caseData.alertTags = tagsList.join(", ")
                    caseData.performedBy = sca.dispperfdrmedby
                    alertDataDTO.uniqueDispositions.add(caseData.disposition)
                    if (alertDataDTO.exportCaseNarrative) {
                        caseData.caseNarrative = sca.casenarrative
                    }
                    caseData
                } as Callable)
            }
            futureList.each {
                scaList.add(it.get())
            }
            executorService.shutdown()
        }
        scaList
    }

    /**
     * Converts value for few of the columns into desired format
     * for the export of single case alert data
     * @param isJader
     * @param key
     * @param value
     * @param sdf
     * @return value required for export
     */
    String generateValueForExport(Boolean isJader,String key, def value, SimpleDateFormat sdf,String userTimeZone='UTC') {
        if(key in ["initialFu"]){
            return value ?: Constants.Commons.DASH_STRING
        } else if (key in ["serious", "indication", "eventOutcome", "causeOfDeath", "seriousUnlistedRelated", "conComit", "age",
                    "country", "gender", "rechallenge", "death", "caseType", "indNumber","appTypeAndNum",
                    "compoundingFlag", "submitter", "region", "patientHistDrugs", "batchLotNo",
                    "caseClassification", "protocolNo", "isSusar", "therapyDates", "preAnda",
                    "caseReportType", "changes", "genericName", "eventOnsetDate"
                    , "pregnancy", "medicallyConfirmed", "allPTsOutcome"]) {
            return value ?: Constants.Commons.BLANK_STRING
        } else if (key in ["dateOfBirth"]) {
            if(Holders.config.enable.show.dob==true){
                return value ?: Constants.Commons.BLANK_STRING
            }else{
                return "#########"
            }
        } else if (key in ["doseDetails"]) {
            return value ? (value == '-' ? ' -' : value) : Constants.Commons.BLANK_STRING
        } else if (key in ["outcome", "listedness"]) {
            return value && value != 'undefined' ? value : Constants.Commons.BLANK_STRING
        } else if (key in ["caseInitReceiptDate", "lockedDate","caseCreationDate"]) {
            return (value != Constants.BlindingStatus.BLINDED && value != Constants.BlindingStatus.REDACTED) ? (value ? convertDate(value) : ''): value
        } else if (key.equals("patientAge")) {
            DecimalFormat df = new DecimalFormat('#.##')
           return (value && value != Constants.BlindingStatus.REDACTED && value != Constants.BlindingStatus.BLINDED && value.isDouble()) ? (df.format(value.toBigDecimal()) + " " + Constants.Commons.AGE_STANDARD_UNIT) : value ?:Constants.Commons.DASH_STRING
        } else if (key in["dueDate"]) {
            return value ? dueIn(value as String) : Constants.Commons.BLANK_STRING
        } else if (key.equals("completenessScore")) {
            return value ?(value.toString()): Constants.Commons.BLANK_STRING
        } else if (key.equals("patientMedHist")) {
            return value ?(getPatientMedHist(value as String)): Constants.Commons.BLANK_STRING
        } else if (key in ["alertName", "caseNumber", "productName", "pt", "masterPrefTermAll", "malfunction",
                           "comboFlag", "badge"]) {
            return value
        } else if (key.equals("timeToOnset")) {
            return (value) ?(value as String): Constants.Commons.BLANK_STRING
        } else if (key.equals("reportersHcpFlag")) {
            return generateReportersHcpFlagValue(value)
        } else if (key.equals("medErrorsPt")) {
            return generateMedErrorPts(value)
        } else if(key.equals("dispLastChange")){
            return value?DateUtil.toDateStringWithTimeInAmFormat(sdf.parse(value as String), userTimeZone): Constants.Commons.BLANK_STRING
        } else if(key.equals("suspProd")){
            return value?value.replaceAll('\r\n', ',') : Constants.Commons.BLANK_STRING
        }
        return value
    }

    String unescapeHtml(String data){
        String[] htmlSymbol = ["'","©","Û","®","ž","Ü","Ÿ","Ý",'$',"Þ","%","¡","ß","¢","à","£","á","À","¤","â","Á","¥","ã","Â","¦","ä","Ã","§","å","Ä","¨","æ","Å","©","ç","Æ","ª","è","Ç","«","é","È","¬","ê","É","­","ë","Ê","®","ì","Ë","¯","í","Ì","°","î","Í","±","ï","Î","²","ð","Ï","³","ñ","Ð","´","ò","Ñ","µ","ó","Õ","¶","ô","Ö","·","õ","Ø","¸","ö","Ù","¹","÷","Ú","º","ø","Û","»","ù","Ü","@","¼","ú","Ý","½","û","Þ","€","¾","ü","ß","¿","ý","à","‚","À","þ","á","ƒ","Á","ÿ","å","„","Â","æ","…","Ã","ç","†","Ä","è","‡","Å","é","ˆ","Æ","ê","‰","Ç","ë","Š","È","ì","‹","É","í","Œ","Ê","î","Ë","ï","Ž","Ì","ð","Í","ñ","Î","ò","‘","Ï","ó","’","Ð","ô","“","Ñ","õ","”","Ò","ö","•","Ó","ø","–","Ô","ù","—","Õ","ú","˜","Ö","û","™","×","ý","š","Ø","þ","›","Ù","ÿ","œ","Ú"];
        String[] codes = ["&apos;","&copy;","&#219;","&reg;","&#158;","&#220;","&#159;","&#221;","&#36;","&#222;","&#37;","&#161;","&#223;","&#162;","&#224;","&#163;","&#225;","&Agrave;","&#164;","&#226;","&Aacute;","&#165;","&#227;","&Acirc;","&#166;","&#228;","&Atilde;","&#167;","&#229;","&Auml;","&#168;","&#230;","&Aring;","&#169;","&#231;","&AElig;","&#170;","&#232;","&Ccedil;","&#171;","&#233;","&Egrave;","&#172;","&#234;","&Eacute;","&#173;","&#235;","&Ecirc;","&#174;","&#236;","&Euml;","&#175;","&#237;","&Igrave;","&#176;","&#238;","&Iacute;","&#177;","&#239;","&Icirc;","&#178;","&#240;","&Iuml;","&#179;","&#241;","&ETH;","&#180;","&#242;","&Ntilde;","&#181;","&#243;","&Otilde;","&#182;","&#244;","&Ouml;","&#183;","&#245;","&Oslash;","&#184;","&#246;","&Ugrave;","&#185;","&#247;","&Uacute;","&#186;","&#248;","&Ucirc;","&#187;","&#249;","&Uuml;","&#64;","&#188;","&#250;","&Yacute;","&#189;","&#251;","&THORN;","&#128;","&#190;","&#252","&szlig;","&#191;","&#253;","&agrave;","&#130;","&#192;","&#254;","&aacute;","&#131;","&#193;","&#255;","&aring;","&#132;","&#194;","&aelig;","&#133;","&#195;","&ccedil;","&#134;","&#196;","&egrave;","&#135;","&#197;","&eacute;","&#136;","&#198;","&ecirc;","&#137;","&#199;","&euml;","&#138;","&#200;","&igrave;","&#139;","&#201;","&iacute;","&#140;","&#202;","&icirc;","&#203;","&iuml;","&#142;","&#204;","&eth;","&#205;","&ntilde;","&#206;","&ograve;","&#145;","&#207;","&oacute;","&#146;","&#208;","&ocirc;","&#147;","&#209;","&otilde;","&#148;","&#210;","&ouml;","&#149;","&#211;","&oslash;","&#150;","&#212;","&ugrave;","&#151;","&#213;","&uacute;","&#152;","&#214;","&ucirc;","&#153;","&#215;","&yacute;","&#154;","&#216;","&thorn;","&#155;","&#217;","&yuml;","&#156;","&#218;"];
        for(int i=0;i<codes.size();i++){
            if(data?.contains(codes[i])){
               data=data.replaceAll(codes[i],htmlSymbol[i])
            }
        }
        return data
    }

    String generateMedErrorPts(String value){
        if(value){
            List medErrorPtList = []
            value.split('\r\n').each {
                if (it && it.length() > 2) {
                    medErrorPtList.add(it.substring(3, it.length()).trim())
                }
            }
            return medErrorPtList.join(",")
        }
        return Constants.Commons.BLANK_STRING
    }

    String generateReportersHcpFlagValue(String value) {
        try {
            if (value == null || value.trim().equalsIgnoreCase('null'))
                return '-'
        } catch (Exception e) {
            return '-'
        }
        return value
    }


    def generateDueIn(def dueDate) {
        def theDueDate = new DateTime(dueDate).withTimeAtStartOfDay()
        def now = DateTime.now().withTimeAtStartOfDay()
        def dur = new Duration(now, theDueDate)
        dur.getStandardDays()
    }

    List listCaseInfo(String executedConfigId, String eventCode, String eventCodeVal, List allowedProductsToUser, Boolean cumulative) {

        Sql sql = new Sql(dataSource_pva)
        List data = []
        try {

            def singleCaseAlerts = SingleCaseAlert.withCriteria {
                or {
                    allowedProductsToUser.collate(1000).each{
                        'in'("productName", it)
                    }
                }
                if (cumulative) {
                    'executedAlertConfiguration' {
                        eq('adhocRun', false)
                    }
                } else {
                    ExecutedConfiguration executedConfig = getExecConfigurationById(Long.parseLong(executedConfigId))
                    eq("executedAlertConfiguration", executedConfig)
                }
            }
            if (singleCaseAlerts) {

                String caseVersionString = ""

                String primaryPt = null
                singleCaseAlerts.each {
                    Integer caseVersion = it.caseVersion
                    String caseNumber = it.caseNumber
                    caseVersionString = caseVersionString + "(" + "'" + caseNumber + "'" + "," + caseVersion + "),"
                }

                //Remove the last comma.
                caseVersionString = caseVersionString.substring(0, caseVersionString.length() - 1)

                def sql_statement = SignalQueryHelper.case_info_sql(caseVersionString, eventCode, eventCodeVal)

                sql.eachRow(sql_statement, []) { resultSet ->

                    String disposition
                    String priority
                    String assignedTo
                    Long alertId

                    def caseNumber = resultSet.getString('CASE_NUMBER')
                    def caseVersion = resultSet.getString('CASE_VERSION')

                    //Fetch the single cae alert from case number and version number.
                    //As the workflow mgmt states will be same for the all the single case alerts with
                    //case number and case version thus we'll consult single case instance to fetch the
                    //workflow mgmt values.
                    SingleCaseAlert singleCaseAlert = SingleCaseAlert.findByCaseNumberAndCaseVersion(caseNumber, caseVersion)

                    if (singleCaseAlert) {
                        primaryPt = singleCaseAlert.pt
                        disposition = singleCaseAlert.disposition?.displayName
                        priority = singleCaseAlert.priority?.displayName
                        assignedTo = singleCaseAlert.assignedTo ? singleCaseAlert.assignedTo.fullName : singleCaseAlert.assignedToGroup.name
                        alertId = singleCaseAlert.id
                    }

                    data << [
                            caseVersion        : caseVersion,
                            caseNumber         : caseNumber,
                            productName        : resultSet.getString('PRODUCT_NAME'),
                            pt                 : primaryPt ? primaryPt : resultSet.getString('PT'),
                            listedness         : resultSet.getString('LISTEDNESS'),
                            seriousness        : resultSet.getString('SERIOUSNESS'),
                            outcome            : resultSet.getString('CASE_OUTCOME_DESC'),
                            determinedCausality: resultSet.getString('DETERMINED_CAUSALITY'),
                            reportedCausality  : resultSet.getString('REPORTED_CAUSALITY'),
                            hcp                : resultSet.getString('HCP_FLAG'),
                            disposition        : disposition,
                            priority           : priority,
                            assignedTo         : assignedTo,
                            alertId            : alertId,
                            followUpNumber     : resultSet.getString('FOLLOW_UP') ?: "-"
                    ]
                }
            }
        } catch (Exception e) {
            log.error(e.printStackTrace())
        } finally {
            sql.close()
        }
        data
    }


    List<String> processExcelFile(MultipartFile fileToBeProcessed) {
        List<String> list = ExcelDataImporter.readFromExcelForAddCases(fileToBeProcessed)
        list
    }

    Map<String, List<String>> validCaseNumbersFromExcel(List<String> caseNumberList, Set<String> warnings, ExecutedConfiguration executedConfig) {
        List<String> validCaseNumber = []
        List<String> invalidCaseNumber = []
        caseNumberList.each { caseNumber ->
            if (!SingleCaseAlert.findByExecutedAlertConfigurationAndCaseNumber(executedConfig, caseNumber)) {
                validCaseNumber.add("'" + caseNumber + "'")
            } else {
                invalidCaseNumber.add(caseNumber)
            }
        }
        [validCaseNumber: validCaseNumber, invalidCaseNumber: invalidCaseNumber + warnings]
    }


    List getRelatedCaseSeries(String caseNumber, int offset, int max, def searchString = null, Map orderColumnMap = null) {
        User user = userService.getUser()
        boolean isAlertVisible
        List<Long> groupIdList = user.groups.collect { it.id }
        String timeZone = user.preference.timeZone
        Group workflowGroup = user.workflowGroup
        List relatedCaseSeriesList = []
        if (!TextUtils.isEmpty(caseNumber)) {
                relatedCaseSeriesList = SingleCaseAlert.createCriteria().list(offset:offset,max:max) {
                    if (searchString) {
                        or {
                            ilike("name", "%${searchString}%")
                        }
                    }
                    eq("caseNumber", caseNumber)
                    eq("isCaseSeries", false)
                    'executedAlertConfiguration' {
                        eq("isLatest", true)
                        eq("workflowGroup", workflowGroup)
                    }
                    if (orderColumnMap.name.equals("description")) {
                        'alertConfiguration' {
                            order('description', orderColumnMap.dir)
                        }
                    } else if (orderColumnMap.name.equals("lastExecuted")) {
                        'alertConfiguration' {
                            order('dateCreated', orderColumnMap.dir)
                        }
                    } else {
                        order(Order.(orderColumnMap.dir)(orderColumnMap.name).ignoreCase())
                    }
                }

            relatedCaseSeriesList = relatedCaseSeriesList.collect {
                String query = ""
                if (it.alertConfiguration.alertQueryId && it.alertConfiguration.alertQueryName) {
                    query = it.alertConfiguration.alertQueryName
                }
                isAlertVisible = false
                if (it.assignedTo?.id == user.id || (it.assignedToGroup?.id in groupIdList) || (it.alertConfiguration.shareWithUser?.id.contains(user.id)) || (it.alertConfiguration.shareWithGroup?.id.intersect(groupIdList)) ) {
                    isAlertVisible = true
                }
                [
                        alertId         : it.executedAlertConfiguration?.id,
                        name            : it.alertConfiguration?.name,
                        description     : it.alertConfiguration?.description ?: '-',
                        dateRange       : getDateRangeAlert(it.executedAlertConfiguration?.id),
                        query           : query,
                        productSelection: it.alertConfiguration?.productSelection ? getNameFieldFromJson(it.alertConfiguration.productSelection) : getGroupNameFieldFromJson(it.alertConfiguration.productGroupSelection),
                        lastExecuted    : DateUtil.stringFromDate(it.alertConfiguration?.dateCreated, DateUtil.DATEPICKER_FORMAT, timeZone),
                        isAlertVisible  : isAlertVisible
                ]
            }
        }
        relatedCaseSeriesList
    }
    int getRelatedCaseSeriesTotalCount(String caseNumber, def searchString = null) {
        User user = userService.getUser()
        String timeZone = user.preference.timeZone
        Group workflowGroup = user.workflowGroup
        int relatedCaseSeriesListCount
        if (!TextUtils.isEmpty(caseNumber)) {
            relatedCaseSeriesListCount = SingleCaseAlert.createCriteria().list() {
                if (searchString) {
                    or {
                        ilike("name", "%${searchString}%")
                    }
                }
                eq("caseNumber", caseNumber)
                eq("isCaseSeries", false)
                'executedAlertConfiguration' {
                    eq("isLatest", true)
                    eq("workflowGroup", workflowGroup)
                }
            }.size()
            return relatedCaseSeriesListCount
        }
    }

    def getDateRangeAlert(Long id) {
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.findById(id)
        if (executedConfiguration) {
            Date startDate = executedConfiguration.executedAlertDateRangeInformation?.dateRangeStartAbsolute
            Date endDate = executedConfiguration.executedAlertDateRangeInformation?.dateRangeEndAbsolute
            if (startDate && endDate) {
                return "${DateUtil.toDateString1(startDate)} - ${DateUtil.toDateString1(endDate)}"
            }
        }
        return ""
    }

    @Transactional
    def persistValidatedSignalWithSingleCaseAlert(Long executedConfigId, Long configId) {
        List<SingleCaseAlert> attachSignalAlertList = getAttachSignalAlertList(executedConfigId)
        if (attachSignalAlertList) {
            log.info("Now saving the signal across the PE.")
            List<Map<String, String>> alertIdAndSignalIdList = getAlertIdAndSignalIdForBusinessConfig(executedConfigId, configId, attachSignalAlertList)

            List<String> caseNoAndProductFamilyList = attachSignalAlertList.collect {
                "(" + "'" + it.caseNumber + "'" + "," + "'" + it.productFamily + "'" + ")"
            }

            Session session = sessionFactory.currentSession
            String sql_statement = SignalQueryHelper.signal_alert_ids_single(caseNoAndProductFamilyList.join(","), executedConfigId, configId)
            SQLQuery sqlQuery = session.createSQLQuery(sql_statement)
            sqlQuery.list().each { row ->
                alertIdAndSignalIdList.add([col2: row[0].toString(), col1: row[1].toString(), col3: '1'])
            }

            alertIdAndSignalIdList = alertIdAndSignalIdList.unique {
                [it.col2, it.col1]
            }
            log.info("Batch execution of TABLE validated_single_alerts started")
            String insertValidatedQuery = "INSERT INTO validated_single_alerts(VALIDATED_SIGNAL_ID,SINGLE_ALERT_ID,IS_CARRY_FORWARD,DATE_CREATED) VALUES(?,?,?,?)"
            session.doWork(new Work() {
                public void execute(Connection connection) throws SQLException {
                    PreparedStatement preparedStatement = connection.prepareStatement(insertValidatedQuery)
                    def batchSize = Holders.config.signal.batch.size
                    int count = 0
                    try {
                        alertIdAndSignalIdList.each {
                            preparedStatement.setString(1, it.col1)
                            preparedStatement.setString(2, it.col2)
                            preparedStatement.setString(3, it.col3?:'0')
                            preparedStatement.setTimestamp(4, new Timestamp(System.currentTimeMillis()))
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
            log.info("Signal are saved across the system.")
        }

    }

    List<SingleCaseAlert> getAttachSignalAlertList(long executedConfigId) {
        List<SingleCaseAlert> attachSignalAlertList = SingleCaseAlert.createCriteria().list {
            eq("executedAlertConfiguration.id", executedConfigId)
            'disposition' {
                eq("validatedConfirmed", true)
            }
            createAlias("validatedSignals", "vs", JoinType.LEFT_OUTER_JOIN)
            isNull('vs.id')
        } as List<SingleCaseAlert>
        attachSignalAlertList
    }

    List<Map<String, String>> getAlertIdAndSignalIdForBusinessConfig(Long executedConfigId, Long configId, List<SingleCaseAlert> attachSignalAlertList) {
        List<Map> signalAlertMap = dataObjectService.getSignalAlertMap(executedConfigId)
        List<Map<String, String>> alertIdAndSignalIdList = []
        List<Activity> signalActivityList = []
        if (signalAlertMap) {
            List<String> validatedDateDispositions=Holders.config.alert.validatedDateDispositions
            Disposition defaultSignalDisposition = attachSignalAlertList[0].alertConfiguration.getWorkflowGroup()?.defaultSignalDisposition
            boolean isValidatedDate=validatedDateDispositions.contains(defaultSignalDisposition.value)
            ValidatedSignal validatedSignal = validatedSignalService.createSignalForBusinessConfiguration(signalAlertMap[0].signalName, signalAlertMap[0].alert, Constants.AlertConfigType.SINGLE_CASE_ALERT, defaultSignalDisposition, signalAlertMap[0].signalId ? signalAlertMap[0].signalId as Long : null, isValidatedDate)
            signalAlertMap.each { Map map ->
                Long alertId = attachSignalAlertList.find {
                    map.alert.caseNumber == it.caseNumber && map.alert.productFamily == it.productFamily
                }?.id
                if (alertId) {
                    alertIdAndSignalIdList.add([col2: alertId.toString(), col1: validatedSignal.id.toString(), col3: '0'])
                    signalActivityList.add(activityService.createActivityForSignalBusinessConfig(map.alert.caseNumber, validatedSignal.name, map.alert.name))
                }
            }
            dataObjectService.clearSignalAlertMap(executedConfigId)
            alertService.persistActivitesForSignal(validatedSignal.id, signalActivityList)
        }
        alertIdAndSignalIdList
    }

    @Transactional
    Map changeDisposition(List<Long> singleCaseAlertIdList, Disposition newDisposition, String incomingDisposition,
                          String justification, String validatedSignalName, String productJson, boolean isArchived,Long signalId) {
        Boolean attachedSignalData = false
        Boolean dispositionChanged = false
        Boolean adhocSignalCheck = true
        Date lastChangeDate = null
        List<Map> activityList = []
        List<Long> existingCaseHistoryList = []
        List<CaseHistory> caseHistoryList = []
        ValidatedSignal validatedSignal
        def domain = alertService.generateDomainName(isArchived)
        List singleCaseAlertList = fetchSingleCaseAlertsForAssignedTo(singleCaseAlertIdList, domain)
        if (signalId) {
            ValidatedSignal validatedSignalDuplicate = ValidatedSignal.findById(signalId)
            if (validatedSignalDuplicate) {
                adhocSignalCheck = false
                singleCaseAlertList = singleCaseAlertList.findAll {
                    !it.validatedSignals.contains(validatedSignalDuplicate)
                }
            }
        }
        List<UndoableDisposition> undoableDispositionList =[]
        if (singleCaseAlertList.size() > 0) {
            Map<Long, String> tagsNameMap = getTagsNameListBulkOperations(singleCaseAlertIdList)
            AlertLevelDispositionDTO alertLevelDispositionDTO = new AlertLevelDispositionDTO()

            boolean bulkUpdate = singleCaseAlertIdList.size() > 1
            Disposition previousDisposition = singleCaseAlertList[0].disposition
            Disposition defaultSignalDisposition = singleCaseAlertList[0].alertConfiguration.getWorkflowGroup()?.defaultSignalDisposition
            DispositionRule dispositionRule
            Long configId = singleCaseAlertList[0].alertConfiguration.id
            Long exConfigId = singleCaseAlertList[0].executedAlertConfiguration.id
            List<Map<String, String>> alertIdAndSignalIdList = []
            List<Activity> signalActivityList = []
            List<String> validatedDateDispositions=Holders.config.alert.validatedDateDispositions;
            boolean isValidatedDate=validatedDateDispositions.contains(defaultSignalDisposition.value);
            if (validatedSignalName) {
                String dictionarySelectionType = dataObjectService.getDictionaryProductLevel()
                productJson = SignalUtil.generateProductDictionaryJson(singleCaseAlertList[0]?.getAttr(cacheService.getRptFieldIndexCache('cpiProdIdResolved')) as String, singleCaseAlertList[0]?.productName, dictionarySelectionType, singleCaseAlertList[0].executedAlertConfiguration.isMultiIngredient)
                Boolean isNonCompany = singleCaseAlertList[0]?.getAttr(cacheService.getRptFieldIndexCache('cpiProdIdResolved')) == null
                validatedSignal = validatedSignalService.createSignalForBusinessConfiguration(validatedSignalName, singleCaseAlertList[0], Constants.AlertConfigType.SINGLE_CASE_ALERT, defaultSignalDisposition, productJson,null, null,signalId,isValidatedDate, isNonCompany, adhocSignalCheck)
                //removed activity from here and added inside createSignalForBusinessConfiguration to maintain the order
            }

            if (newDisposition?.id) {
                dispositionRule = DispositionRule.findByIncomingDispositionAndTargetDispositionAndIsDeleted(previousDisposition, newDisposition, false)
            }
            alertLevelDispositionDTO.existingCaseHistoryList = caseHistoryService.existingCaseHistoryMap(singleCaseAlertIdList)
            List<Map> alertDueDateList = []
            boolean isCaseSeries = false
            DashboardCountDTO dashboardCountDTO = alertService.prepareDashboardCountDTO(true)
            boolean isTargetDispReviewed = newDisposition.reviewCompleted
            singleCaseAlertList.each { def singleCaseAlert ->
                if (singleCaseAlert.isCaseSeries) {
                    isCaseSeries = true
                }
                if (incomingDisposition.equals(singleCaseAlert.disposition.displayName)) {
                    String prevDueDate = singleCaseAlert.dueDate ? DateUtil.stringFromDate(singleCaseAlert.dueDate, "dd-MM-yyyy", "UTC") : null
                    singleCaseAlert.customAuditProperties = ["justification": justification]
                    if (!singleCaseAlert?.disposition?.isValidatedConfirmed()) {
                        Disposition oldDisp = singleCaseAlert?.disposition

                        Map dispDataMap = [objectId          : singleCaseAlert.id, objectType: Constants.AlertConfigType.SINGLE_CASE_ALERT, prevDispositionId: oldDisp.id,
                                           currDispositionId   : newDisposition.id, prevJustification: singleCaseAlert.justification, prevDispPerformedBy: singleCaseAlert.dispPerformedBy,
                                           prevDispChangeDate: singleCaseAlert.dispLastChange, prevDueDate: singleCaseAlert.dueDate, pastPrevDueDate:singleCaseAlert.previousDueDate]

                        UndoableDisposition undoableDisposition = undoableDispositionService.createUndoableObject(dispDataMap)
                        undoableDispositionList.add(undoableDisposition)

                        alertLevelDispositionDTO.justificationText = justification
                        alertLevelDispositionDTO.changeType = Constants.HistoryType.DISPOSITION
                        alertLevelDispositionDTO.targetDisposition = newDisposition
                        alertLevelDispositionDTO.userName = userService.getCurrentUserName()
                        singleCaseAlert.disposition = newDisposition
                        saveAlertCaseHistory(singleCaseAlert,justification,alertLevelDispositionDTO.userName)
                        lastChangeDate = singleCaseAlert.dispLastChange
                        if((newDisposition.closed || newDisposition.reviewCompleted) && singleCaseAlert.dueDate!=null) {
                            singleCaseAlert.previousDueDate = singleCaseAlert.dueDate
                        }
                        calcDueDate(singleCaseAlert, singleCaseAlert.priority, singleCaseAlert.disposition, true,
                                cacheService.getDispositionConfigsByPriority(singleCaseAlert.priority.id))
                        def customReviewPeriod = cacheService.getDispositionConfigsByPriority(singleCaseAlert.priority.id)?.find {it -> it.disposition == newDisposition}?.reviewPeriod
                        if(newDisposition.closed==false && newDisposition.reviewCompleted==false && singleCaseAlert?.previousDueDate!=null && !customReviewPeriod){
                            singleCaseAlert.dueDate = singleCaseAlert.previousDueDate
                        }

                        alertDueDateList << [id           : singleCaseAlert.id, dueDate: singleCaseAlert.dueDate,
                                             dispositionId: singleCaseAlert.dispositionId, reviewDate: singleCaseAlert.reviewDate?new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(singleCaseAlert.reviewDate):null, caseNumber: singleCaseAlert.caseNumber, configId: configId, execConfigId: exConfigId]
                        if (emailNotificationService.emailNotificationWithBulkUpdate(bulkUpdate, Constants.EmailNotificationModuleKeys.DISPOSITION_CHANGE_SCA)) {
                            emailNotificationService.mailHandlerForDispChangeSCA(singleCaseAlert, previousDisposition, dispositionRule, isArchived)
                        }
                        Map caseHistoryAndExistingCaseHistoryMap = createCaseAndExistingCaseHistory(singleCaseAlert, tagsNameMap, alertLevelDispositionDTO, isArchived)
                        caseHistoryList.add(caseHistoryAndExistingCaseHistoryMap.caseHistory)
                        Long existingCaseHistoryId = caseHistoryAndExistingCaseHistoryMap.existingCaseHistory
                        if (existingCaseHistoryId) {
                            existingCaseHistoryList.add(existingCaseHistoryId)
                        }
                        dispositionChanged = true
                    } else if (previousDisposition.isValidatedConfirmed()) {
                        if (justification)
                            alertLevelDispositionDTO.justificationText = justification.replace('.', ' ') + "-- " + customMessageService.getMessage("validatedObservation.justification.case", "${validatedSignalName}")
                        else
                            alertLevelDispositionDTO.justificationText = customMessageService.getMessage("validatedObservation.justification.case", "${validatedSignalName}")
                        alertLevelDispositionDTO.changeType = Constants.HistoryType.DISPOSITION
                        alertLevelDispositionDTO.targetDisposition = singleCaseAlert?.disposition
                        alertLevelDispositionDTO.userName = userService.getCurrentUserName()
                        saveAlertCaseHistory(singleCaseAlert,alertLevelDispositionDTO.justificationText,alertLevelDispositionDTO.userName)
                        lastChangeDate = singleCaseAlert.dispLastChange
                        calcDueDate(singleCaseAlert, singleCaseAlert.priority, singleCaseAlert.disposition, true,
                                cacheService.getDispositionConfigsByPriority(singleCaseAlert.priority.id))
                        if(newDisposition.closed==false && newDisposition.reviewCompleted==false && singleCaseAlert.previousDueDate != null) {
                            singleCaseAlert.dueDate = singleCaseAlert.previousDueDate
                        }
                        alertDueDateList << [id           : singleCaseAlert.id, dueDate: singleCaseAlert.dueDate,
                                             dispositionId: singleCaseAlert.dispositionId, reviewDate: singleCaseAlert.reviewDate?new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(singleCaseAlert.reviewDate):null, caseNumber: singleCaseAlert.caseNumber, configId: configId, execConfigId: exConfigId]
                        Map caseHistoryAndExistingCaseHistoryMap = createCaseAndExistingCaseHistory(singleCaseAlert, tagsNameMap, alertLevelDispositionDTO, isArchived)
                        caseHistoryList.add(caseHistoryAndExistingCaseHistoryMap.caseHistory)
                        Long existingCaseHistoryId = caseHistoryAndExistingCaseHistoryMap.existingCaseHistory
                        if (existingCaseHistoryId) {
                            existingCaseHistoryList.add(existingCaseHistoryId)
                        }

                    }

                    Activity activity = createActivityForBulkUpdate(singleCaseAlert, newDisposition, previousDisposition, justification, validatedSignalName)
                    activityList.add([execConfigId: singleCaseAlert.executedAlertConfigurationId, activity: activity])

                    //For Dashboard Counts
                    if (newDisposition?.id) {
                        if (singleCaseAlert.assignedToId) {
                            alertService.updateDashboardCountMaps(dashboardCountDTO.prevDispCountMap, singleCaseAlert.assignedToId, previousDisposition.id.toString())
                            alertService.updateDashboardCountMaps(dashboardCountDTO.userDispCountsMap, singleCaseAlert.assignedToId, newDisposition.id.toString())
                            if (prevDueDate && isTargetDispReviewed) {
                                alertService.updateDashboardCountMaps(dashboardCountDTO.userDueDateCountsMap, singleCaseAlert.assignedToId, prevDueDate)
                            }
                        } else {
                            alertService.updateDashboardCountMaps(dashboardCountDTO.prevGroupDispCountMap, singleCaseAlert.assignedToGroupId, previousDisposition.id.toString())
                            alertService.updateDashboardCountMaps(dashboardCountDTO.groupDispCountsMap, singleCaseAlert.assignedToGroupId, newDisposition.id.toString())
                            if (prevDueDate && isTargetDispReviewed) {
                                alertService.updateDashboardCountMaps(dashboardCountDTO.groupDueDateCountsMap, singleCaseAlert.assignedToGroupId, prevDueDate)
                            }
                        }

                        //For Top 50 alert widget
                        alertService.updateDashboardCountMaps(dashboardCountDTO.execDispCountMap, singleCaseAlert.executedAlertConfigurationId, newDisposition.id.toString())
                    }
                    if (validatedSignal) {
                        alertIdAndSignalIdList.add([col2: singleCaseAlert.id.toString(), col1: validatedSignal.id.toString()])
                        signalActivityList.add(activityService.createActivityForSignalBusinessConfig(singleCaseAlert.caseNumber, validatedSignal.name, singleCaseAlert.name))
                        signalAuditLogService.createAuditLog([
                                entityName: "Signal: Individual Case Review Observations",
                                moduleName: "Signal: Individual Case Review Observations",
                                category: AuditTrail.Category.INSERT.toString(),
                                entityValue: "${validatedSignal.name}: Case Associated",
                                username: userService.getUser().username,
                                fullname: userService.getUser().fullName
                        ] as Map, [[propertyName: "Case Associated", oldValue: "", newValue: "${singleCaseAlert.caseNumber}"]] as List)
                        attachedSignalData = true
                    }

                }
            }

            if (singleCaseAlertIdList) {
                if (!isArchived && !isCaseSeries) {
//                    alertService.updateDashboardCountsForDispChange(dashboardCountDTO, isTargetDispReviewed)
//                    alertService.updateDashboardGroupCountsForDispChange(dashboardCountDTO, isTargetDispReviewed)
                }
                if(!isArchived){
                    notify 'push.disposition.changes', [undoableDispositionList:undoableDispositionList]
                }
                //Creating caseHistories in background
                def totalPrevDispCount = dashboardCountDTO.prevDispCountMap ?: [:]
                totalPrevDispCount = totalPrevDispCount + dashboardCountDTO.prevGroupDispCountMap ?: [:]

                notify 'activity.case.history.event.published', [existingCaseHistoryList: existingCaseHistoryList, caseHistoryList: caseHistoryList,
                                                                 activityList           : activityList, isBulkUpdate: true,
                                                                 execDispCountMap       : dashboardCountDTO.execDispCountMap,
                                                                 prevDispCountMap       : totalPrevDispCount, lastChangeDate        : lastChangeDate]
            }

            if (validatedSignal) {
                notify 'activity.add.signal.event.published', [signalId: validatedSignal.id, signalActivityList: signalActivityList]
                String insertValidatedQuery
                if (isArchived)
                    insertValidatedQuery = "INSERT INTO VALIDATED_ARCHIVED_SCA(VALIDATED_SIGNAL_ID,ARCHIVED_SCA_ID) VALUES(?,?)"
                else
                    insertValidatedQuery = "INSERT INTO validated_single_alerts(VALIDATED_SIGNAL_ID,SINGLE_ALERT_ID, DATE_CREATED) VALUES(?,?,?)"
                alertService.batchPersistForMapping(sessionFactory.currentSession, alertIdAndSignalIdList, insertValidatedQuery, !isArchived)
            }
            Map signal=validatedSignalService.fetchSignalDataFromSignal(validatedSignal,productJson,null);
            [attachedSignalData: attachedSignalData, dispositionChanged: dispositionChanged,
             alertDueDateList: alertDueDateList, domain: domain,signal:signal]
        } else
            [attachedSignalData: true, dispositionChanged: true, alertDueDateList: [], domain: domain]
    }

    Activity createActivityForUndoAction(def alert, String justification, String activityChanges){
        ActivityType activityType = cacheService.getActivityTypeByValue(ActivityTypeValue.UndoAction.value)
        String changeDetails = Constants.ChangeDetailsUndo.UNDO_DISPOSITION_CHANGE + " with " + activityChanges
        User loggedInUser = cacheService.getUserByUserId(userService.getCurrentUserId())

        Activity activity = activityService.createActivityBulkUpdate(activityType, loggedInUser, changeDetails, justification,
                ['For Case Number': alert.caseNumber], alert.productName, alert.pt, alert.assignedToId ? cacheService.getUserByUserId(alert.assignedToId) : null, alert.caseNumber,
                alert.assignedToGroupId ? cacheService.getGroupByGroupId(alert.assignedToGroupId) : null)

        activity
    }
    def undoneCaseHistory(SingleCaseAlert singleCaseAlert){
        log.info("Marking previous Case history as Undone")

        CaseHistory caseHistory = CaseHistory.createCriteria().get{
            eq("caseNumber", singleCaseAlert.caseNumber)
            eq("singleAlertId", singleCaseAlert.id as Long)
            eq("execConfigId", singleCaseAlert.executedAlertConfigurationId as Long)
            eq("change", Constants.HistoryType.DISPOSITION)
            order("lastUpdated", "desc")
            maxResults(1)
        }

        if (caseHistory) {
            caseHistory.isUndo = true
            CRUDService.save(caseHistory)
            log.info("Successfully marked previous Case History as Undone for singleCaseAlert alert: ${singleCaseAlert?.id}")
        }

    }
    @Transactional
    def revertDisposition(Long id, String justification) {
        log.info("Reverting Dispostion Started for Single Case Alert")
        Boolean dispositionReverted = false
        List<Map> activityList = []
        List<Map> alertDueDateList = []
        List<Long> existingCaseHistoryList = []
        List<CaseHistory> caseHistoryList = []
        String oldDispName = ""
        String newDispName = ""

        SingleCaseAlert singleCaseAlert = SingleCaseAlert.get(id)
        UndoableDisposition undoableDisposition = UndoableDisposition.createCriteria().get {
            eq('objectId', id as Long)
            eq('objectType', Constants.AlertConfigType.SINGLE_CASE_ALERT)
            order('dateCreated', 'desc')
            maxResults(1)
        }
        if(singleCaseAlert && undoableDisposition?.isEnabled){
            try{
                DashboardCountDTO dashboardCountDTO = alertService.prepareDashboardCountDTO(false)

                AlertLevelDispositionDTO alertLevelDispositionDTO = new AlertLevelDispositionDTO()

                alertLevelDispositionDTO.existingCaseHistoryList = caseHistoryService.existingCaseHistoryMap([id])

                Map<Long, String> tagsNameMap = getTagsNameListBulkOperations([id])

                Disposition previousDisposition = cacheService.getDispositionByValue(singleCaseAlert.disposition?.id)
                oldDispName=previousDisposition?.displayName

                String prevDueDate = singleCaseAlert.dueDate ? DateUtil.stringFromDate(singleCaseAlert.dueDate, "dd-MM-yyyy", "UTC") : null


                Disposition newDisposition = cacheService.getDispositionByValue(undoableDisposition.prevDispositionId)
                newDispName = newDisposition?.displayName

                boolean isTargetDispReviewed = newDisposition.reviewCompleted

                undoableDisposition.isUsed = true

                alertLevelDispositionDTO.justificationText = justification
                alertLevelDispositionDTO.changeType = Constants.HistoryType.UNDO_ACTION
                alertLevelDispositionDTO.targetDisposition = newDisposition
                alertLevelDispositionDTO.userName = userService.getCurrentUserName()
                // saving state before undo for activity: 60067
                def prevUndoDisposition = singleCaseAlert.disposition
                def prevUndoJustification = singleCaseAlert.justification
                def prevUndoDispPerformedBy = singleCaseAlert.dispPerformedBy
                def prevUndoDispChangeDate = singleCaseAlert.dispLastChange
                def prevUndoDueDate = singleCaseAlert.dueDate

                singleCaseAlert.disposition = newDisposition
                singleCaseAlert.justification = undoableDisposition.prevJustification
                singleCaseAlert.dispPerformedBy = undoableDisposition.prevDispPerformedBy
                singleCaseAlert.dispLastChange = undoableDisposition.prevDispChangeDate
                singleCaseAlert.dueDate = undoableDisposition.prevDueDate
                singleCaseAlert.undoJustification = justification

                def activityMap = [
                        'Disposition': [
                                'previous': prevUndoDisposition ?: "",
                                'current': singleCaseAlert.disposition ?: ""
                        ],
                        'Justification': [
                                'previous': prevUndoJustification ?: "",
                                'current': singleCaseAlert.justification ?: ""
                        ],
                        'Performed By': [
                                'previous': prevUndoDispPerformedBy ?: "",
                                'current': singleCaseAlert.dispPerformedBy ?: ""
                        ],
                        'Last Disposition Date': [
                                'previous': prevUndoDispChangeDate ? new SimpleDateFormat("yyyy-MM-dd").format(prevUndoDispChangeDate) : "",
                                'current': singleCaseAlert.dispLastChange ? new SimpleDateFormat("yyyy-MM-dd").format(singleCaseAlert.dispLastChange) : ""
                        ],
                        'Due Date': [
                                'previous': prevUndoDueDate ? new SimpleDateFormat("yyyy-MM-dd").format(prevUndoDueDate) : "",
                                'current': singleCaseAlert.dueDate ? new SimpleDateFormat("yyyy-MM-dd").format(singleCaseAlert.dueDate) : ""
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


                CRUDService.update(singleCaseAlert)
                CRUDService.update(undoableDisposition)

                UndoableDisposition.executeUpdate("Update UndoableDisposition set isEnabled=:isEnabled where objectId=:id and objectType=:type", [isEnabled: false, id: id, type: Constants.AlertConfigType.SINGLE_CASE_ALERT])

                alertDueDateList << [id        : singleCaseAlert.id, dueDate: singleCaseAlert.dueDate ? new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(singleCaseAlert.dueDate) : null,
                                     dispositionId: singleCaseAlert.disposition.id,
                                     reviewDate: singleCaseAlert.reviewDate ? new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(singleCaseAlert.reviewDate) : null]
                undoneCaseHistory(singleCaseAlert)

                Map caseHistoryAndExistingCaseHistoryMap = createCaseAndExistingCaseHistory(singleCaseAlert, tagsNameMap, alertLevelDispositionDTO, false,[],true)
                caseHistoryList.add(caseHistoryAndExistingCaseHistoryMap.caseHistory)
                Long existingCaseHistoryId = caseHistoryAndExistingCaseHistoryMap.existingCaseHistory
                if (existingCaseHistoryId) {
                    existingCaseHistoryList.add(existingCaseHistoryId)
                }


                Activity activity = createActivityForUndoAction(singleCaseAlert, justification, activityChanges)
                activityList.add([execConfigId: singleCaseAlert.executedAlertConfigurationId, activity: activity])

                if (singleCaseAlert.assignedToId) {
                    alertService.updateDashboardCountMaps(dashboardCountDTO.prevDispCountMap, singleCaseAlert.assignedToId, previousDisposition.id.toString())
                    alertService.updateDashboardCountMaps(dashboardCountDTO.userDispCountsMap, singleCaseAlert.assignedToId, newDisposition.id.toString())
                    if (prevDueDate && isTargetDispReviewed) {
                        alertService.updateDashboardCountMaps(dashboardCountDTO.userDueDateCountsMap, singleCaseAlert.assignedToId, prevDueDate)
                    }
                } else {
                    alertService.updateDashboardCountMaps(dashboardCountDTO.prevGroupDispCountMap, singleCaseAlert.assignedToGroupId, previousDisposition.id.toString())
                    alertService.updateDashboardCountMaps(dashboardCountDTO.groupDispCountsMap, singleCaseAlert.assignedToGroupId, newDisposition.id.toString())
                    if (prevDueDate && isTargetDispReviewed) {
                        alertService.updateDashboardCountMaps(dashboardCountDTO.groupDueDateCountsMap, singleCaseAlert.assignedToGroupId, prevDueDate)
                    }
                }
                //For Top 50 alert widget
                alertService.updateDashboardCountMaps(dashboardCountDTO.execDispCountMap, singleCaseAlert.executedAlertConfigurationId, newDisposition.id.toString())

                alertService.updateDashboardCountsForDispChange(dashboardCountDTO, isTargetDispReviewed)

                alertService.updateDashboardGroupCountsForDispChange(dashboardCountDTO, isTargetDispReviewed)

                def totalPrevDispCount = dashboardCountDTO.prevDispCountMap ?: [:]
                totalPrevDispCount = totalPrevDispCount + dashboardCountDTO.prevGroupDispCountMap ?: [:]

                notify 'activity.case.history.event.published', [existingCaseHistoryList: existingCaseHistoryList, caseHistoryList: caseHistoryList,
                                                                 activityList           : activityList, isBulkUpdate: true,
                                                                 execDispCountMap       : dashboardCountDTO.execDispCountMap,
                                                                 prevDispCountMap       : totalPrevDispCount]
                dispositionReverted = true
                log.info("Dispostion reverted successfully for single case alert Id: " + id)
            } catch(Exception ex){
                ex.printStackTrace()
            }
        }
        [alertDueDateList: alertDueDateList, domain: SingleCaseAlert, oldDispName: oldDispName, newDispName: newDispName, dispositionReverted:dispositionReverted]
    }

    void saveAlertCaseHistory(def alert, String justification, String userName = null){
        alert.justification = justification
        alert.dispLastChange = new Date()
        alert.dispPerformedBy = userName?cacheService.getUserByUserNameIlike(userName)?.fullName:userService.getUser()?.fullName
        alert.isDispChanged = true
    }
    void saveDispositionAlertCaseHistory(SingleCaseAlert alert, CaseHistory caseHistory){
        alert.justification = caseHistory.justification
        alert.dispLastChange = Objects.nonNull(caseHistory.createdTimestamp) ? caseHistory.createdTimestamp : caseHistory.lastUpdated
        String userName = caseHistory.modifiedBy? (caseHistory.modifiedBy == Constants.Commons.SYSTEM ? Constants.SYSTEM_USER: caseHistory.modifiedBy)
                : caseHistory.createdBy == Constants.Commons.SYSTEM ? Constants.SYSTEM_USER: caseHistory.createdBy
        alert.dispPerformedBy = cacheService.getUserByUserNameIlike(userName)?.fullName?:userName
    }

    void createCaseHistoryForDispositionChange(def alert, String justification, ExecutedConfiguration executedConfiguration,
                                               Configuration configuration, String systemUser, Boolean isArchived = false, Date lastDispChange = null) {
        //Create the caseHistory.
        Date timestamp = null
        if (Objects.nonNull(lastDispChange)) {
            timestamp = lastDispChange
        }
        Map caseHistoryMap = [
                "configId"              : configuration?.id,
                "singleAlertId"         : !isArchived ? alert?.id : null,
                "archivedSingleAlertId" : isArchived ? alert?.id : null,
                "currentDisposition"    : alert.disposition,
                "currentPriority"       : alert.priority,
                "caseNumber"            : alert.caseNumber,
                "caseVersion"           : alert.caseVersion,
                "productFamily"         : alert.productFamily,
                "currentAssignedTo"     : alert.assignedTo,
                "currentAssignedToGroup": alert.assignedToGroup,
                "followUpNumber"        : alert.followUpNumber,
                "justification"         : justification,
                "execConfigId"          : executedConfiguration.id,
                "change"                : Constants.HistoryType.DISPOSITION,
                "createdBySystem"       : systemUser,
                "dueDate"               : alert.dueDate,
                "createdTimestamp"      : timestamp
        ]
        caseHistoryService.saveCaseHistory(caseHistoryMap)
    }

    void createActivityForDispositionChange(def singleCaseAlert, Disposition previousDisposition, Disposition targetDisposition,
                                            String justification, ExecutedConfiguration executedConfiguration, User loggedInUser, String validatedSignalName = null, Date dispLastChange = null) {

        ActivityType activityType = null
        String changeDetails = null

        //if current disposition is VO and targetDisposition==null, it means case is signal attach
        if (null == targetDisposition.value && previousDisposition.isValidatedConfirmed()) {
            activityType = ActivityType.findByValue(ActivityTypeValue.SignalAdded)

            changeDetails = "Case attached with Signal '$validatedSignalName'"
        } else {
            activityType = ActivityType.findByValue(ActivityTypeValue.DispositionChange)

            changeDetails = "Disposition changed from '$previousDisposition' to '$targetDisposition'"
        }

        Map attrs = [product: getNameFieldFromJson(singleCaseAlert.alertConfiguration.productSelection),
                     event  : getNameFieldFromJson(singleCaseAlert.alertConfiguration.eventSelection)]

        activityService.createActivity(executedConfiguration, activityType, loggedInUser, changeDetails, justification,
                attrs, singleCaseAlert.productName, singleCaseAlert.pt, singleCaseAlert.assignedTo, singleCaseAlert.caseNumber,singleCaseAlert.assignedToGroup, null, null, dispLastChange)
    }


    void saveTags(String caseLevelTags, String globalTags, Long caseId, Long executedConfigurationId, String owner, String insertStatement) {
        log.info("caseLevelTags=${caseLevelTags},globalTags=${globalTags},caseId=${caseId},executedConfigurationId=${executedConfigurationId}, owner=${owner}")
        Sql sql = new Sql(signalDataSourceService.getReportConnection("pva"))
        try {
            if (insertStatement) {
                sql.execute(insertStatement)
            }
            sql.call("{ call PKG_TAG_MAPPING.p_tag_mapping(?,?,?,?,?)}", [caseLevelTags, globalTags, caseId, executedConfigurationId, owner])
        } finally {
            sql.close()
        }
    }

    Configuration getAlertConfigObject(ExecutedConfiguration executedConfiguration) {
        Configuration.findById(executedConfiguration?.configId)
    }

    Configuration saveCaseSeriesConfiguration(Long configId, String configName, aggExecutionId, aggAlertId, aggCountType, selectedDatasource, User user = null) {
        User currentUser = user ?: userService.getUser()
        Date currentDate = new Date()
        Configuration aggConfig = Configuration.get(configId)
        if(aggConfig.integratedConfigurationId){
            // PVS-58898: Main configuration should be used,
            // configuration of other datasources are not updated
            aggConfig = Configuration.get(aggConfig.integratedConfigurationId)
        }
        ExecutedConfiguration aggExecutedConfig = ExecutedConfiguration.get(aggExecutionId)
        Date aggConfigNextRunDate = aggConfig?.nextRunDate
        Configuration configurationInstance = new Configuration()
        configurationInstance.type = SINGLE_CASE_ALERT
        configurationInstance.setIsEnabled(false)
        configurationInstance.nextRunDate = null
        configurationInstance.priority = aggConfig?.priority
        configurationInstance.reviewPeriod = aggConfig?.reviewPeriod
        configurationInstance.productDictionarySelection = aggConfig?.productDictionarySelection
        configurationInstance.productSelection = aggConfig?.productSelection
        configurationInstance.productGroupSelection = aggConfig?.productGroupSelection
        configurationInstance.eventSelection = aggConfig?.eventSelection
        configurationInstance.studySelection = aggConfig?.studySelection
        configurationInstance.owner = aggConfig?.owner
        configurationInstance.dataMiningVariable = aggConfig?.dataMiningVariable
        configurationInstance.adhocRun = aggConfig?.adhocRun
        configurationInstance.missedCases = aggConfig?.missedCases
        configurationInstance.excludeNonValidCases = aggConfig?.excludeNonValidCases
        configurationInstance.includeLockedVersion = aggConfig?.includeLockedVersion
        configurationInstance.evaluateDateAs = aggConfig?.evaluateDateAs ?: EvaluateCaseDateEnum.LATEST_VERSION
        if(configurationInstance && configurationInstance.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF ) {
            configurationInstance.asOfVersionDate = aggConfig?.asOfVersionDate as Date
        }
        List<User> autoShareWithUserList = []
        List<Group> autoShareWithGroupList = []
        if (aggConfig.isAutoAssignedTo) {
            configurationInstance.assignedTo = aggExecutedConfig.assignedTo
            configurationInstance.assignedToGroup = aggExecutedConfig.assignedToGroup
            aggExecutedConfig.autoShareWithUser.each { User usr ->
                autoShareWithUserList.add(usr)
            }
            aggExecutedConfig.autoShareWithGroup.each { Group grp ->
                autoShareWithGroupList.add(grp)
            }
            configurationInstance.autoShareWithUser = autoShareWithUserList
            configurationInstance.autoShareWithGroup = autoShareWithGroupList
        } else {
            configurationInstance.assignedTo = aggConfig.assignedTo
            configurationInstance.assignedToGroup = aggConfig.assignedToGroup
            aggConfig?.autoShareWithUser.each { User usr ->
                autoShareWithUserList.add(usr)
            }
            aggConfig?.autoShareWithGroup.each { Group grp ->
                autoShareWithGroupList.add(grp)
            }
            configurationInstance.autoShareWithUser = autoShareWithUserList
            configurationInstance.autoShareWithGroup = autoShareWithGroupList
        }
        List<User> shareWithUserList = []
        List<Group> shareWithGroupList = []
        aggConfig?.shareWithUser.each { User usr ->
            shareWithUserList.add(usr)
        }
        aggConfig?.shareWithGroup.each { Group grp ->
            shareWithGroupList.add(grp)
        }
        configName = configName.replaceAll("/","")
        configurationInstance.shareWithUser = shareWithUserList
        configurationInstance.shareWithGroup = shareWithGroupList
        configurationInstance.dateCreated = currentDate
        configurationInstance.lastUpdated = currentDate
        configurationInstance.createdBy = currentUser
        configurationInstance.modifiedBy = currentUser
        configurationInstance.repeatExecution = false
        configurationInstance.groupBySmq = false
        configurationInstance.name = configName.length() > 512 ? configName.substring(0, 496) + "..." + configName[-13..-1] : configName
        configurationInstance.aggExecutionId = aggExecutionId
        configurationInstance.aggAlertId = aggAlertId
        configurationInstance.aggCountType = aggCountType
        configurationInstance.isCaseSeries = true
        configurationInstance.workflowGroup = currentUser?.workflowGroup
        configurationInstance.selectedDatasource = selectedDatasource
        configurationInstance.skipAudit=true  //to skip the audit of configuration while drill down manual entry to be created
        configurationInstance.isMultiIngredient = aggConfig?.isMultiIngredient ?: false
        configurationInstance.includeWHODrugs = aggConfig?.includeWHODrugs ?: false
        configurationInstance.studyCases = aggConfig?.studyCases ?: false
        configurationInstance.ignoreStudyType = aggConfig?.ignoreStudyType ?: false
        configurationInstance.onlyPoi = aggConfig?.onlyPoi ?: false   //As confirmed this should be same as corresponding agg alert and need not to be fetched from mart again
        bindNewTemplateQueryForCaseSeries(configurationInstance, aggConfig)
        aggConfig.nextRunDate = aggConfigNextRunDate
        aggConfig.save(flush: true, failOnError: true)
        configurationInstance = (Configuration) CRUDService.saveWithAuditLog(configurationInstance)
        configurationInstance
    }

    def bindNewTemplateQueryForCaseSeries(Configuration config, Configuration aggConfig) {

        AlertDateRangeInformation aggDateRange = aggConfig?.alertDateRangeInformation
        config.template = aggConfig.template

        AlertDateRangeInformation alertDateRangeInformation = new AlertDateRangeInformation()

        alertDateRangeInformation.properties = aggDateRange.properties

        //new date range information instance
        alertDateRangeInformation.dateRangeStartAbsoluteDelta = aggDateRange?.dateRangeStartAbsoluteDelta ?: 0
        alertDateRangeInformation.dateRangeEndAbsoluteDelta = aggDateRange?.dateRangeEndAbsoluteDelta ?: 0
        alertDateRangeInformation.dateRangeStartAbsolute = aggDateRange?.dateRangeStartAbsolute ?: null
        alertDateRangeInformation.dateRangeEndAbsolute = aggDateRange?.dateRangeEndAbsolute ?: null
        alertDateRangeInformation.dateRangeEnum = aggDateRange?.dateRangeEnum ?: DateRangeEnum.CUMULATIVE
        alertDateRangeInformation.alertConfiguration = config

        config.alertDateRangeInformation = alertDateRangeInformation

        aggConfig.alertQueryValueLists.each {
            QueryValueList queryValueList = new QueryValueList(query: it.query, queryName: it.queryName)
            it.parameterValues.each {
                ParameterValue parameterValue
                if (it.hasProperty('reportField')) {
                    parameterValue = new QueryExpressionValue(key: it.key,
                            reportField: it.reportField, operator: it.operator, value: it.value,
                            operatorValue: messageSource.getMessage("app.queryOperator.$it.operator", null, Locale.ENGLISH))
                } else {
                    parameterValue = new CustomSQLValue(key: it.key, value: it.value)
                }
                queryValueList.addToParameterValues(parameterValue)
            }
            config.addToAlertQueryValueLists(queryValueList)
        }
        aggConfig.alertForegroundQueryValueLists.each {
            QueryValueList queryValueList = new QueryValueList(query: it.query, queryName: it.queryName)
            it.parameterValues.each {
                ParameterValue parameterValue
                if (it.hasProperty('reportField')) {
                    parameterValue = new QueryExpressionValue(key: it.key,
                            reportField: it.reportField, operator: it.operator, value: it.value)
                } else {
                    parameterValue = new CustomSQLValue(key: it.key, value: it.value)
                }
                queryValueList.addToParameterValues(parameterValue)
            }
            config.addToAlertForegroundQueryValueLists(queryValueList)
        }

    }
    ExecutedConfiguration saveCaseSeriesExecutedConfig(Long aggExecutedId, Configuration configuration, String selectedDatasource) {
        ExecutedConfiguration aggExConfig = ExecutedConfiguration.findById(aggExecutedId)
        ExecutedConfiguration executedConfiguration = new ExecutedConfiguration(name: configuration.name,
                owner: User.get(configuration.owner.id), scheduleDateJSON: configuration.scheduleDateJSON, nextRunDate: configuration.nextRunDate ?: aggExConfig.nextRunDate ?: new Date(),
                description: configuration.description, dateCreated: configuration.dateCreated, lastUpdated: configuration.lastUpdated,
                isPublic: configuration.isPublic, isDeleted: configuration.isDeleted, isEnabled: true,
                dateRangeType: configuration.dateRangeType, productSelection: configuration.productSelection,
                eventSelection: configuration.eventSelection, studySelection: configuration.studySelection,
                configSelectedTimeZone: configuration.configSelectedTimeZone, asOfVersionDate: configuration.asOfVersionDate,
                evaluateDateAs: configuration.evaluateDateAs, productDictionarySelection: configuration.productDictionarySelection,
                limitPrimaryPath: configuration.limitPrimaryPath, includeMedicallyConfirmedCases: configuration.includeMedicallyConfirmedCases,
                excludeFollowUp: configuration.excludeFollowUp, includeLockedVersion: configuration.includeLockedVersion,missedCases:configuration.missedCases,excludeNonValidCases:configuration.excludeNonValidCases,
                adjustPerScheduleFrequency: configuration.adjustPerScheduleFrequency,
                assignedToGroup: configuration.assignedToGroup,
                isAutoTrigger: configuration.isAutoTrigger, adhocRun: configuration.adhocRun,
                dataMiningVariable: configuration.dataMiningVariable,
                dataMiningVariableValue: configuration.dataMiningVariableValue,
                frequency: null, reviewDueDate: aggExConfig?.reviewDueDate ?: new Date(),
                createdBy: configuration.getOwner().username, modifiedBy: configuration.modifiedBy,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: configuration.numOfExecutions + 1,
                aggExecutionId: configuration.aggExecutionId, aggAlertId: configuration.aggAlertId, aggCountType: configuration.aggCountType, isCaseSeries: configuration.isCaseSeries,
                priority: configuration.priority, productGroupSelection: configuration.productGroupSelection, configId: configuration.id,
                selectedDatasource: selectedDatasource, workflowGroup: configuration.workflowGroup)
        ExecutedAlertDateRangeInformation executedAlertDateRangeInformation = new ExecutedAlertDateRangeInformation(
                dateRangeStartAbsoluteDelta: aggExConfig.executedAlertDateRangeInformation.dateRangeStartAbsoluteDelta,
                dateRangeEndAbsoluteDelta: aggExConfig.executedAlertDateRangeInformation.dateRangeEndAbsoluteDelta,
                dateRangeStartAbsolute: aggExConfig.executedAlertDateRangeInformation.dateRangeStartAbsolute,
                dateRangeEndAbsolute: aggExConfig.executedAlertDateRangeInformation.dateRangeEndAbsolute,
                dateRangeEnum: aggExConfig.executedAlertDateRangeInformation.dateRangeEnum,
                relativeDateRangeValue: aggExConfig.executedAlertDateRangeInformation.relativeDateRangeValue
        )
        executedConfiguration.executedAlertDateRangeInformation = executedAlertDateRangeInformation
        executedConfiguration.type = configuration.type
        executedConfiguration.groupBySmq = configuration.groupBySmq
        executedConfiguration.assignedTo = configuration.assignedTo
        executedConfiguration.workflowGroup = configuration.workflowGroup
        executedConfiguration.isMultiIngredient = configuration.isMultiIngredient
        executedConfiguration.includeWHODrugs = configuration.includeWHODrugs
        executedConfiguration.studyCases = configuration?.studyCases ?: false
        executedConfiguration.ignoreStudyType = configuration?.ignoreStudyType ?: false
        executedConfiguration.onlyPoi = configuration?.onlyPoi ?: false
        executedConfiguration.studyDrugs = aggExConfig?.studyDrugs != "" ?: Constants.Commons.BLANK_STRING
        List<User> autoShareWithUserList = []
        List<Group> autoShareWithGroupList = []
        configuration.autoShareWithUser.each { User usr ->
            autoShareWithUserList.add(usr)
        }
        configuration.autoShareWithGroup.each { Group grp ->
            autoShareWithGroupList.add(grp)
        }
        executedConfiguration.autoShareWithUser = autoShareWithUserList
        executedConfiguration.autoShareWithGroup = autoShareWithGroupList
        executedConfiguration.save(failOnError: true)
        executedConfiguration
    }

    def generateTempCaseSeries(String configurationName, Long executedConfigId) {
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(executedConfigId)
        List singleCaseAlertList = SingleCaseAlert.findAllByExecutedAlertConfiguration(executedConfiguration)
        def caseData = []

        String caseSeriesDelimiter = Holders.config.caseSeries.bulk.addCase.delimiter ?: ":"
        singleCaseAlertList.each { sca ->
            caseData.add(sca.caseNumber + caseSeriesDelimiter + sca.caseVersion)
        }
        caseData = caseData.join(",")
        Map response = reportExecutorService.generateCaseSeries(configurationName, caseData)
        if (response.status == HttpStatus.SC_OK) {
            if (response.result.status) {
                Long seriesId = response.result.data.id as Long
                executedConfiguration.pvrCaseSeriesId = seriesId
                executedConfiguration.isLatest = true
                executedConfiguration.workflowGroup = userService.getUser().workflowGroup
                if ((executedConfiguration.executedAlertDateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE)) {
                    executedConfiguration.pvrCumulativeCaseSeriesId = seriesId
                }
                executedConfiguration.save(flush: true, failOnError: true)
            }
        }
        response.status = response.status
        response as JSON
    }

    Integer changeAlertLevelDisposition(AlertLevelDispositionDTO alertLevelDispositionDTO, Boolean isArchived = false) {
        alertService.changeAlertLevelDisposition(saveActivityAndHistory, alertLevelDispositionDTO, isArchived)
    }

    Closure saveActivityAndHistory = { AlertLevelDispositionDTO alertLevelDispositionDTO, Boolean isArchived , List bulkUpdateDueDateDataList = [] ->
        def startTime= System.currentTimeSeconds()
        List<Activity> activityList = []
        List<CaseHistory> caseHistoryList = []
        List<UndoableDisposition> undoableDispositionList =[]
        List<Long> existingCaseHistoryList = []
        DashboardCountDTO dashboardCountDTO = alertService.prepareDashboardCountDTO(true)
        dashboardCountDTO.prevDispCountMap = alertLevelDispositionDTO.alertList.findAll { it.assignedToId }.countBy { it.dispositionId as String }
        dashboardCountDTO.prevGroupDispCountMap = alertLevelDispositionDTO.alertList.findAll { !it.assignedToId }.countBy { it.dispositionId as String }
        dashboardCountDTO.execDispCountMap = [(alertLevelDispositionDTO.execConfigId): [(alertLevelDispositionDTO.targetDisposition.id.toString()): alertLevelDispositionDTO.alertList.size()]]
        boolean isTargetDispReviewed = alertLevelDispositionDTO.targetDisposition.reviewCompleted
        alertLevelDispositionDTO.activityType = ActivityType.findByValue(ActivityTypeValue.DispositionChange)
        alertLevelDispositionDTO.loggedInUser = userService.getUser()
        alertLevelDispositionDTO.userName = userService.getCurrentUserName()
        alertLevelDispositionDTO.changeType = Constants.HistoryType.DISPOSITION
        List<Long> singleCaseAlertIdList = alertLevelDispositionDTO.alertList.collect { it.id }
        alertLevelDispositionDTO.existingCaseHistoryList = caseHistoryService.existingCaseHistoryMap(singleCaseAlertIdList)
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
                Activity activity = alertService.createActivityForBulkDisposition(alertMap, alertLevelDispositionDTO)
                Map caseHistoryAndExistingCaseHistoryMap = createCaseAndExistingCaseHistory(alertMap, tagsNameMap, alertLevelDispositionDTO, isArchived, bulkUpdateDueDateDataList)
                UndoableDisposition undoableDisposition = null
                if(!isArchived){
                    Map dispDataMap = [objectId          : alertMap.id, objectType: Constants.AlertConfigType.SINGLE_CASE_ALERT, prevDispositionId: alertMap.disposition.id,
                                       currDispositionId   : alertLevelDispositionDTO.targetDisposition.id, prevJustification: alertMap.justification, prevDispPerformedBy: alertMap.dispPerformedBy,
                                       prevDispChangeDate: alertMap.dispLastChange, prevDueDate: alertMap.dueDate]

                    undoableDisposition = undoableDispositionService.createUndoableObject(dispDataMap)

                }
                [activity: activity, caseHistoryAndExistingCaseHistoryMap: caseHistoryAndExistingCaseHistoryMap, undoableDisposition: undoableDisposition?:null]
            } as Callable)
        }
        futureList.each {
            activityList.add(it.get()['activity'])
            caseHistoryList.add(it.get()['caseHistoryAndExistingCaseHistoryMap'].caseHistory)
            Long existingCaseHistoryId = it.get()['caseHistoryAndExistingCaseHistoryMap'].existingCaseHistory
            if (existingCaseHistoryId) {
                existingCaseHistoryList.add(existingCaseHistoryId)
            }
            undoableDispositionList.add(it.get()['undoableDisposition'])
        }
        executorService.shutdown()
        def totalPrevDispCount = dashboardCountDTO.prevDispCountMap ?: [:]
        totalPrevDispCount = totalPrevDispCount + dashboardCountDTO.prevGroupDispCountMap ?: [:]

        Map activityAndHistoryMap = [activityList           : activityList, caseHistoryList: caseHistoryList,
                                     existingCaseHistoryList: existingCaseHistoryList, id: alertLevelDispositionDTO.execConfigId,
                                     execDispCountMap       : dashboardCountDTO.execDispCountMap, prevDispCountMap: totalPrevDispCount]
        notify 'activity.case.history.event.published', activityAndHistoryMap
        undoableDispositionList.removeAll([null])
        log.info("Time taken in closure before notify call : ${System.currentTimeSeconds() - startTime}")
        if(undoableDispositionList.size()>0){
            notify 'push.disposition.changes', [undoableDispositionList:undoableDispositionList]
        }
    }

    Map<Long, String> getTagsNameList(Long execConfigId, List<Long> reviewCompletedDispIdList) {
        Map<Long, String> tagsNameMap = [:]
        if (reviewCompletedDispIdList) {
            Session session = sessionFactory.currentSession
            String sql_statement = SignalQueryHelper.list_single_case_tag_name(execConfigId, reviewCompletedDispIdList)
            SQLQuery sqlQuery = session.createSQLQuery(sql_statement)
            sqlQuery.list()?.each { row ->
                tagsNameMap.put(row[0] as Long, row[1])
            }
        }
        tagsNameMap
    }

    Map<Long, String> getTagsNameListBulkOperations(List<Long> singleCaseAlertIdList) {
        Map<Long, String> tagsNameMap = [:]
        if (singleCaseAlertIdList) {
            Session session = sessionFactory.currentSession
            String sql_statement = SignalQueryHelper.list_single_case_tag_name_bulk(singleCaseAlertIdList)
            SQLQuery sqlQuery = session.createSQLQuery(sql_statement)
            sqlQuery.list()?.each { row ->
                tagsNameMap.put(row[0] as Long, row[1])
            }
        }
        tagsNameMap
    }


    void getExistingCaseHistoryList(List alertList, Long execConfigId) {
        List<String> caseNumberList = alertList.collect { it.caseNumber }
        List<CaseHistory> existingCaseHistoryList = []
        existingCaseHistoryList = CaseHistory.createCriteria().list {
            eq('configId', execConfigId)
            or {
                caseNumberList.collate(1000).each {
                    'in'("caseNumber", it)
                }
            }
            eq('isLatest', true)
        } as List<CaseHistory>
        existingCaseHistoryList
    }

    Map createCaseAndExistingCaseHistory(
            def alertMap, Map<Long, String> tagsNameMap, AlertLevelDispositionDTO alertLevelDispositionDTO, boolean isArchived = false, List<Map> bulkUpdateDueDateDataList = [], boolean isUndo=false, boolean isDispositionChange = true) {
        //Create the caseHistory.
        Map caseHistoryMap = caseHistoryMap(alertMap, tagsNameMap, alertLevelDispositionDTO, isArchived, isUndo, isDispositionChange)
        if (bulkUpdateDueDateDataList) {
            Map dueDateMap = bulkUpdateDueDateDataList.find { it.priorityId == alertMap?.priority?.id }
            if (dueDateMap) {
                caseHistoryMap.dueDate = !dueDateMap.isClosed ? (dueDateMap.dueDateChange ? dueDateMap.dueDate : caseHistoryMap.dueDate) : null
            }
        }
        Long existingCaseHistoryId = alertLevelDispositionDTO.existingCaseHistoryList.find {
            it.caseNumber == caseHistoryMap.caseNumber && it.configId == caseHistoryMap.configId
        }?.id
        CaseHistory caseHistory = new CaseHistory(caseHistoryMap)
        [caseHistory: caseHistory, existingCaseHistory: existingCaseHistoryId]
    }

    Map caseHistoryMap(def alertMap, Map<Long, String> tagsNameMap, AlertLevelDispositionDTO alertLevelDispositionDTO, boolean isArchived = false, boolean isUndo=false, boolean isDispositionChange = true) {

        Map caseHistoryMap = [
                "configId"              : alertMap.alertConfigurationId,
                "singleAlertId"         : !isArchived ? alertMap?.id : null,
                "archivedSingleAlertId" : isArchived ? alertMap?.id : null,
                "currentDisposition"    : alertLevelDispositionDTO.targetDisposition ?: cacheService.getDispositionByValue(alertMap.dispositionId),
                "currentPriority"       : alertLevelDispositionDTO.priority ?: cacheService.getPriorityByValue(alertMap.priorityId),
                "caseNumber"            : alertMap.caseNumber,
                "caseVersion"           : alertMap.caseVersion,
                "productFamily"         : alertMap.productFamily,
                "currentAssignedTo"     : alertMap.assignedToId ? cacheService.getUserByUserId(alertMap.assignedToId) : null,
                "currentAssignedToGroup": alertMap.assignedToGroupId ? cacheService.getGroupByGroupId(alertMap.assignedToGroupId) : null,
                "justification"         : alertLevelDispositionDTO.justificationText,
                "execConfigId"          : alertLevelDispositionDTO.execConfigId ?: alertMap.executedAlertConfigurationId,
                "change"                : alertLevelDispositionDTO.changeType,
                "followUpNumber"        : alertMap.followUpNumber,
                "isLatest"              : true,
                "createdBy"             : alertLevelDispositionDTO.userName,
                "modifiedBy"            : alertLevelDispositionDTO.userName,
                "dueDate"               : alertMap.dueDate,
                "isUndo"                : isUndo,
                "createdTimestamp"      : isDispositionChange ? alertMap?.dispLastChange : null
        ]
        caseHistoryMap
    }

    void batchPersistForAlert(List domainList, String alertType, Boolean dssPvaFlag = false, List productEventHistoryList = [], List qualifiedSignalList = []) throws Exception {
        Integer batchSize = Holders.config.signal.batch.less.size as Integer
         def domainClz;
         if(alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT){
             domainClz = AggregateCaseAlert
         }
        else if(alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT){
             domainClz = SingleCaseAlert
         }
        domainClz.withTransaction {
            List batch = []
            domainList.eachWithIndex { def domain, Integer index ->
                if(domain instanceof AggregateCaseAlert && (grailsApplication.config.statistics.enable.dssScores || grailsApplication.config.pvs.autoAssign.flag)){
                    AggregateCaseAlert alert = (AggregateCaseAlert)domain
                    if(grailsApplication.config.statistics.enable.dssScores && grailsApplication.config.dss.enable.autoProposed && dssPvaFlag)
                        proposedDispositionRule(alert, productEventHistoryList, qualifiedSignalList)
                    if(grailsApplication.config.pvs.autoAssign.flag)
                        autoAssignUser(alert)
                }
                batch += domain
                domain.save(validate: false)
                if (index.mod(batchSize) == 0) {
                    Session session = sessionFactory.currentSession
                    session.flush()
                    session.clear()
                    batch.clear()
                }
            }
            if (batch) {
                Session session = sessionFactory.currentSession
                session.flush()
                session.clear()
                batch.clear()
            }
        }
        log.info("Data is persisted, pec count is ${domainList?.size()}")
    }

    void proposedDispositionRule(AggregateCaseAlert aggregateCaseAlert, List<ProductEventHistory> productEventHistoryList, List<ValidatedSignal> qualifiedSignalList) {
        boolean isAutoProposed = grailsApplication.config.dss.enable.autoProposed
        List<ProductEventHistory> alertPEHistoryList = productEventHistoryList.findAll {
            it.productName == aggregateCaseAlert.getProductName() &&
            it.eventName == aggregateCaseAlert.getPt()
        }?.takeRight(6)
        if(aggregateCaseAlert.isNew && isAutoProposed){
                proposeDispositionOnNewPEC(aggregateCaseAlert, isAutoProposed)
        } else if (!aggregateCaseAlert.disposition.reviewCompleted && isAutoProposed && !alertPEHistoryList){
            aggregateCaseAlert.proposedDisposition = ''
            aggregateCaseAlert.rationale = ''
        } else if(alertPEHistoryList) {
            proposeDispositionBasedOnPEHistory(alertPEHistoryList, aggregateCaseAlert, isAutoProposed, qualifiedSignalList)
        }
    }

    /**
     * This method calculates Proposed Disposition based on PEHistory
     * @param productEventHistoryList
     * @param aggregateCaseAlert
     */
    private void proposeDispositionBasedOnPEHistory(List<ProductEventHistory> productEventHistoryList, AggregateCaseAlert aggregateCaseAlert, boolean isAutoProposed = true, List<ValidatedSignal> qualifiedSignalList=[]) {
        List<String> dispFromAlertHistory = []
        List<ValidatedSignal> qualifiedSignal = []
        List<ValidatedSignal> qualifiedSignalPEC = []
        List<String> dispFromAlertHistoryPEC = []
        List mapping = grailsApplication.config.dss.table.calculation

        if(qualifiedSignalList){
            qualifiedSignal = qualifiedSignalList.findAll {
                it.getProductAndGroupNameList()[0]?.equalsIgnoreCase(aggregateCaseAlert.getProductName()) &&
                        it.getEventSelectionList()[0]?.equalsIgnoreCase(aggregateCaseAlert.getPt()) && it.disposition.reviewCompleted
            }
        }
        if(qualifiedSignal){
            qualifiedSignalPEC  = [qualifiedSignal[0]]
        }
        List<String> dispFromQualSignal = qualifiedSignalPEC?.collect { it.disposition.value }?.unique() as List<String>
        productEventHistoryList?.each { pe ->
            dispFromAlertHistory.add(pe.getDisposition().getValue())
        }
        dispFromAlertHistory.unique()
        dispFromQualSignal.removeAll([null])
        dispFromAlertHistory.removeAll([null])
        if(dispFromAlertHistory) {
            dispFromAlertHistoryPEC = [dispFromAlertHistory[0]]
        }
        boolean isDispositionReviewCompleted  = productEventHistoryList[0].disposition.reviewCompleted
        if(!isDispositionReviewCompleted && isAutoProposed){
            aggregateCaseAlert.proposedDisposition = ''
            aggregateCaseAlert.rationale = ''
        }else{
            mapping.each { Map dssConfig ->
                List<String> previouslyReviewedStateArray = dssConfig.previouslyReviewedState.split(",")
                dispFromAlertHistoryPEC.each { String alertDisp ->
                    if (previouslyReviewedStateArray.size() == Constants.Commons.ONE) {
                        if(dssConfig.previouslyReviewedState == alertDisp) {
                            proposeDispositionOnExistingPEC(dssConfig, aggregateCaseAlert, isAutoProposed)
                        }
                    } else {
                        dispFromQualSignal.each { String signalDisp ->
                            List<String> combinedDisp = [signalDisp, alertDisp]
                            if (combinedDisp.intersect(previouslyReviewedStateArray).size() == combinedDisp.size()) {
                                proposeDispositionOnExistingPEC(dssConfig, aggregateCaseAlert, isAutoProposed)
                            }
                        }
                    }
                }
            }
        }
    }

    private void proposeDispositionOnNewPEC(AggregateCaseAlert aggregateCaseAlert, boolean isAutoProposed = true) {
        List mapping = grailsApplication.config.dss.new.pec.table.calculation
        mapping.each {
            String rationale = it.rationale
            boolean isNewPec = it.isNewPec
            String operator = it.operator
            Double dssScoreExpression = it.dssScore as Double
            String proposedDisposition = it.proposedDisposition
            boolean dssScoreMatched = dssComparisionOperation(aggregateCaseAlert.getPecImpNumHigh(),dssScoreExpression, operator)
            if(aggregateCaseAlert.isNew && isNewPec && dssScoreMatched && isAutoProposed && aggregateCaseAlert.isSafety && aggregateCaseAlert.pecImpNumHigh) {
                aggregateCaseAlert.proposedDisposition = proposedDisposition
                aggregateCaseAlert.rationale = rationale
            }
        }
    }

    private void proposeDispositionOnExistingPEC(Map dssConfig, AggregateCaseAlert aggregateCaseAlert, boolean isAutoProposed = true) {
        boolean dssScoreMatched = dssComparisionOperation(aggregateCaseAlert.getPecImpNumHigh(), dssConfig.dssScore, dssConfig.operator)
        if(dssScoreMatched && isAutoProposed && aggregateCaseAlert.isSafety && aggregateCaseAlert.pecImpNumHigh){
            aggregateCaseAlert.proposedDisposition = dssConfig.proposedDisposition
            aggregateCaseAlert.rationale = dssConfig.rationale
        }
    }


    void calculateDssScore(AggregateCaseAlert aca, boolean dssPvaFlag){
        if ((grailsApplication.config.statistics.enable.dssScores || grailsApplication.config.pvs.autoAssign.flag)&&
                grailsApplication.config.statistics.enable.dssScores && dssPvaFlag) {
            setDssScoresValues(aca)
        }
    }

    private setDssScoresValues(AggregateCaseAlert aca) {
        Map dssScoreData = dataObjectService.getDssScoresDataMap(aca.executedAlertConfiguration.id, aca.productName, aca.pt)
        if (dssScoreData && aca.isSafety && !aca.executedAlertConfiguration.groupBySmq) {
            aca.pecImpNumLow = dssScoreData.pecImpNumLow as Double
            aca.pecImpNumHigh = dssScoreData.pecImpNumHigh as Double
            aca.dssScore = dssScoreData.dssScore
        }
    }

    Map saveCaseSeriesInDB(List<String> caseNumberList, ExecutedConfiguration executedConfiguration, String justification = null) {
        Sql sql
        Set<String> warnings = []
        Integer result = 0
        try {
            sql = new Sql(signalDataSourceService.getReportConnection("pva"))
            Long startTime = System.currentTimeMillis()
            sql.withBatch(1000) { stmt ->
                caseNumberList.each { casenum ->
                    //We are passing Version number as -1 if uploaded values has case number only.
                    // In GTT_FILTER_KEY_VALUES(CODE,TEXT), CODE = "Version Number" and TEXT = "Case Number"
                    stmt.addBatch("insert into GTT_FILTER_KEY_VALUES(CODE,TEXT) values(-1,'${casenum}')")
                }
            }

            String endDate = executedConfiguration?.executedAlertDateRangeInformation?.reportStartAndEndDate[1]?.format(SqlGenerationService.DATE_FMT)
            String dateRangeType = executedConfiguration.dateRangeType.value()
            String evaluateDateAs = executedConfiguration.evaluateDateAs
            String versionAsOfDate = executedConfiguration.evaluateDateAs == EvaluateCaseDateEnum.VERSION_ASOF ? executedConfiguration?.asOfVersionDate?.format(SqlGenerationService.DATE_FMT) : null
            Integer includeLockedVersion = executedConfiguration?.includeLockedVersion ? 1 : 0

            sql.call("{?= call PKG_QUERY_HANDLER.f_save_cstm_case_series(?,?,?,?,?,?,?,?,?,?,?,?)}",
                    [Sql.NUMERIC, executedConfiguration.name, executedConfiguration.version, executedConfiguration.owner.id, executedConfiguration.pvrCaseSeriesId, justification, justification ? 1 : 1, endDate, dateRangeType, evaluateDateAs, versionAsOfDate, includeLockedVersion, Constants.PVS_CASE_SERIES_OWNER]) { res ->
                result = res
            }
            if (result != 0) {
                warnings = sql.rows("SELECT * from GTT_REPORT_INPUT_PARAMS").collect { it.PARAM_KEY }
            }
            Long endTime = System.currentTimeMillis()
            log.info("Time taken to save the Case Series \'${executedConfiguration.name}\' in DB : " + ((endTime - startTime) / 1000) + "secs")
            log.info("Out of ${caseNumberList.size()} cases ${(caseNumberList - warnings).size()} are saved in DB")
            log.info("Cases not saved in DB : ${warnings}")
            List<String> casesToSaved = caseNumberList - warnings
            Map<String, List<String>> caseNumbersMap = validCaseNumbersFromExcel(casesToSaved, warnings, executedConfiguration)
            log.info("caseNumbersMap : " + caseNumbersMap)
            Boolean isAddCase = 1
            List<Map> singleAlertCaseList = []
            if(Holders.config.custom.qualitative.fields.enabled) {
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
                        'TEXT_41 "eventPrimaryOnsetDatePartial", TEXT_42 "masterPregnancyFlag",TEXT_43 "masterHcpFlag",CLOB_17  "masterPrefTermList" FROM GTT_CLL_REPORT_DATA'
                sql.call("{call pkg_pvs_case_drill_down.p_case_drill_down(?,?,?,?,?,?,?,?,?,?)}",
                        [executedConfiguration.id, null, null, null, null, null, null,null,isAddCase,
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
                // used same sql that we use in case drill down
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
                        'TEXT_42 "masterPregnancyFlag",TEXT_43 "masterHcpFlag",CLOB_13 "masterSuspProdList",CLOB_14 "masterPrefTermList", TEXT_44 "cifInitialDateLocked", TEXT_45 "ciFlagUnblind", TEXT_46 "studyCodeBroken", TEXT_47 "studyCodeUnBroken", TEXT_48 "analytic.caseInformation.cmflagcopackproduct" FROM GTT_CLL_REPORT_DATA'

                sql.call("{call pkg_pvs_case_drill_down.p_case_drill_down(?,?,?,?,?,?,?,?,?,?)}",
                        [executedConfiguration.id, null, null, null, null, null, null,null,isAddCase,
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
            return [singleAlertCaseList : singleAlertCaseList, caseNumbersMap : caseNumbersMap]
        } catch (SQLException e) {
            e.printStackTrace()
        } finally {
            sql?.close()
        }

    }
    List removeDuplicateCases(List singleAlertCaseList,ExecutedConfiguration ec){
        List singleCaseAlertList = []
        singleAlertCaseList.each { sca->
            String caseNumber = sca[cacheService.getRptFieldIndexCache('masterCaseNum')]
            if (!SingleCaseAlert.findByExecutedAlertConfigurationAndCaseNumber(ec, caseNumber)) {
                singleCaseAlertList.add(sca)
            }
        }
        return singleCaseAlertList
    }

    /**
     * generate detection summary export data
     * @param singleCaseAlertList
     * @return list of map
     */
    List<Map> getSignalDetectionSummaryMap(List singleCaseAlertList) {
        List<Map> signalList = []
        Integer workerCnt = Holders.config.signal.worker.count as Integer
        if (singleCaseAlertList) {
            List<Map> caseHistoryMap = []
            List<String> caseNumberAndConfigIdList = singleCaseAlertList.collect {
                "('" + it.caseNumber + "','" + it.alertConfigurationId + "')"
            }
            String sql_statement = SignalQueryHelper.case_history_change(caseNumberAndConfigIdList.join(","))
            Session session = sessionFactory.currentSession
            SQLQuery sqlQuery = session.createSQLQuery(sql_statement)
            sqlQuery.list().each { row ->
                caseHistoryMap.add([caseNumber: row[0].toString(), configId: row[1], justification: row[2].toString(), change: row[3].toString()])
            }

            ExecutorService executorService = Executors.newFixedThreadPool(workerCnt)
            User user = User.get(userService.getCurrentUserId())
            List<Long> userGroupIdList = user.getGroups().collect { it.id }
            Map blindingFieldsMap = userService.getBlindingFieldsForGroup(userGroupIdList)
            boolean isAllFieldsRedacted = blindingFieldsMap.isAllFieldsRedacted == 1
            boolean isUserMappingAvailable = blindingFieldsMap.isUserMappingAvailable
            Map<String, Constants.BlindingStatus> blindedStatusMap = userService.getBlindingFieldsForUser(blindingFieldsMap, true)
            List<String> blindedFields = []
            List<String> fieldsAvailable = []
            List<String> redactedFields = []
            Map<String, String> conversionMap = Holders.config.icrFields.rptMap

            alertService.categorizeFields(conversionMap, blindedStatusMap, blindedFields, fieldsAvailable, redactedFields, isAllFieldsRedacted,isUserMappingAvailable)
            List<Future<Map<String, String>>> futureList = singleCaseAlertList.collect { def singleCaseAlert ->
                executorService.submit({ ->

                    String dispositionJustification = caseHistoryMap.find {
                        it.caseNumber == singleCaseAlert.caseNumber && it.configId == singleCaseAlert.alertConfigurationId && it.change == Constants.HistoryType.DISPOSITION
                    }?.justification

                    String priorityJustification = caseHistoryMap.find {
                        it.caseNumber == singleCaseAlert.caseNumber && it.configId == singleCaseAlert.alertConfigurationId && it.change == Constants.HistoryType.PRIORITY
                    }?.justification
                    String productName = (singleCaseAlert.caseBlindingStatus && singleCaseAlert.studyBlindingStatus) ? singleCaseAlert.reportedProductName: singleCaseAlert.productName

                    [
                            "caseNumber"           : redactedFields.contains(Constants.AdvancedFilter.CASE_NUMBER) ? Constants.BlindingStatus.REDACTED : (blindedFields.contains(Constants.AdvancedFilter.CASE_NUMBER) && singleCaseAlert.studyBlindingStatus ? Constants.BlindingStatus.BLINDED : getExportCaseNumber(singleCaseAlert.caseNumber, singleCaseAlert.followUpNumber)),
                            "product"              : redactedFields.contains(Constants.ICRFields.PRODUCT_NAME) ? Constants.BlindingStatus.REDACTED : (blindedFields.contains(Constants.ICRFields.PRODUCT_NAME) && singleCaseAlert.studyBlindingStatus ? Constants.BlindingStatus.BLINDED : productName),
                            "event"                : redactedFields.contains(Constants.ICRFields.PT) ? Constants.BlindingStatus.REDACTED : (blindedFields.contains(Constants.ICRFields.PT) && singleCaseAlert.studyBlindingStatus ? Constants.BlindingStatus.BLINDED : singleCaseAlert.pt),
                            "justification"        : (dispositionJustification != null && dispositionJustification != "" && dispositionJustification != 'null') ? dispositionJustification : '-',
                            "currentDisposition"   : singleCaseAlert.disposition.displayName,
                            "priority"             : cacheService.getPriorityByValue(singleCaseAlert.priorityId).displayName,
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

    List<Map> fetchNextXFullCaseList(Map params, Boolean isFaers = false, Boolean isVaers = false, Boolean isVigibase = false,Boolean isJader = false) {
        AlertDataDTO alertDataDTO = generateAlertDataDTO(params, true)
        alertDataDTO.length = params.length
        alertDataDTO.start = params.start
        alertDataDTO.isCaseFormProjection = true
        alertDataDTO.isFaers = isFaers
        alertDataDTO.isVaers = isVaers
        alertDataDTO.isVigibase = isVigibase
        alertDataDTO.isJader = isJader
        if(Objects.isNull(alertDataDTO.orderColumnMap)){
            alertDataDTO.orderColumnMap = [:]
        }
        List<Disposition> openDispositions = alertService.getDispositionsForName(alertDataDTO.dispositionFilters)
        Closure advancedFilterClosure
        advancedFilterClosure = alertService.generateAdvancedFilterClosure(alertDataDTO, advancedFilterClosure)
        List<Map> fullCaseList = alertService.generateAlertList(advancedFilterClosure, alertDataDTO, openDispositions)
        fullCaseList
    }

    AlertDataDTO generateAlertDataDTO(Map params, Boolean isFilterRequest) {
        List filters = getFiltersFromParams(isFilterRequest, params)
        Integer totalColumns = 0
        params.each { k, v ->
            if (k.contains("columns")) {
                totalColumns++
            }
        }
        totalColumns = totalColumns / 6
        Map filterMap = alertService.prepareFilterMap(params, totalColumns)
        List<String> allowedProductsToUser = alertService.fetchAllowedProductsForConfiguration()
        ExecutedConfiguration ec = null
        if (params.callingScreen != Constants.Commons.DASHBOARD) {
            ec = ExecutedConfiguration.findByIdAndIsEnabled(params.id, true)
        }
        User user = userService.getUserFromCacheByUsername(userService.getCurrentUserName())

        String timeZone = user?.preference?.timeZone
        AlertDataDTO alertDataDTO = new AlertDataDTO()
        alertDataDTO.params = params
        alertDataDTO.allowedProductsToUser = allowedProductsToUser
        alertDataDTO.domainName = alertService.generateDomainName(Boolean.parseBoolean(params.isArchived))
        alertDataDTO.executedConfiguration = ec
        alertDataDTO.execConfigId = ec?.id
        alertDataDTO.filterMap = filterMap
        alertDataDTO.timeZone = timeZone
        alertDataDTO.userId = user.id
        alertDataDTO.dispositionFilters = filters
        alertDataDTO.isFullCaseList = true
        alertDataDTO.workflowGroupId = user.workflowGroup.id
        alertDataDTO.groupIdList = user.groups.collect { it.id }
        alertDataDTO
    }

    List getFiltersFromParams(Boolean isFilterRequest, Map params) {
        List filters = []
        def escapedFilters = null
        if (params.filters) {
            def slurper = new JsonSlurper()
            escapedFilters = slurper.parseText(params.filters)
        }
        if(escapedFilters) {
            filters = new ArrayList(escapedFilters)
        }
        if (params.dashboardFilter && (params.dashboardFilter == 'total' || params.dashboardFilter == 'new') && !isFilterRequest) {
            filters = Disposition.list().collect { it.displayName }
        } else if (params.dashboardFilter && params.dashboardFilter == 'underReview' && !isFilterRequest) {
            filters = Disposition.findAllByClosedNotEqualAndValidatedConfirmedNotEqual(true, true).collect {
                it.displayName
            }
        } else if (!isFilterRequest) {
            filters = cacheService.getNotReviewCompletedAndClosedDisposition().collect { it.displayName }
        }
        filters
    }

    void changePriorityOfAlerts(List<Long> singleCaseAlertIdList, String justification, Priority newPriority, ResponseDTO responseDTO, Boolean isArchived) {
        def domain = alertService.generateDomainName(isArchived)
        List singleCaseAlertList = fetchSingleCaseAlertsForAssignedTo(singleCaseAlertIdList, domain)
        List<Map> activityList = []
        User currUser = cacheService.getUserByUserId(userService.getCurrentUserId())
        List<Long> existingCaseHistoryList = []
        List<CaseHistory> caseHistoryList = []
        List<UndoableDisposition> undoableDispositionIdList = []
        Map<Long, String> tagsNameMap = getTagsNameListBulkOperations(singleCaseAlertIdList)
        AlertLevelDispositionDTO alertLevelDispositionDTO = new AlertLevelDispositionDTO()
        alertLevelDispositionDTO.justificationText = justification
        alertLevelDispositionDTO.changeType = Constants.HistoryType.PRIORITY
        alertLevelDispositionDTO.priority = newPriority
        alertLevelDispositionDTO.userName = userService.getCurrentUserName()
        alertLevelDispositionDTO.existingCaseHistoryList = caseHistoryService.existingCaseHistoryMap(singleCaseAlertIdList)

        ExecutorService executorService = Executors.newFixedThreadPool(20)
        List<Future> futureList = singleCaseAlertList.collect { alert ->
            executorService.submit({ ->
                Priority currentPriority = cacheService.getPriorityByValue(alert.priorityId)
                if (newPriority && (currentPriority.id != newPriority.id)) {
//                    Create an activity
                    Activity activity = activityService.createActivityBulkUpdate(cacheService.getActivityTypeByValue(ActivityTypeValue.PriorityChange.value),
                            currUser, "Priority changed from '$currentPriority.displayName' to '$newPriority.displayName'", justification,
                            ['For Case Number': alert.caseNumber], alert.productName, alert.pt, alert.assignedToId ? cacheService.getUserByUserId(alert.assignedToId) : null, alert.caseNumber,
                            alert.assignedToGroupId ? cacheService.getGroupByGroupId(alert.assignedToGroupId) : null)
                    calcDueDate(alert, newPriority, cacheService.getDispositionByValue(alert.dispositionId), false,
                            cacheService.getDispositionConfigsByPriority(newPriority.id))
                    signalAuditLogService.createAuditLog([
                            entityName: "Individual Case Review",
                            moduleName: "Individual Case Review",
                            category: AuditTrail.Category.UPDATE.toString(),
                            entityValue: alert.getInstanceIdentifierForAuditLog(),
                            username: currUser.username,
                            fullname: currUser.fullName
                    ] as Map, [[propertyName: "Priority", oldValue: currentPriority.displayName, newValue: newPriority.displayName]] as List<Map>)

                    // updating the due date of undoable disposition object with latest due date
                    alertService.updateUndoDispDueDate(Constants.AlertConfigType.SINGLE_CASE_ALERT, alert.id as Long, undoableDispositionIdList, alert.dueDate)

                    Map caseHistoryAndExistingCaseHistoryMap = createCaseAndExistingCaseHistory(alert, tagsNameMap, alertLevelDispositionDTO, isArchived, [], false, false)
                    [activity                            : [execConfigId: alert.executedAlertConfigurationId, activity: activity],
                     caseHistoryAndExistingCaseHistoryMap: caseHistoryAndExistingCaseHistoryMap,
                     dueInResponse                       : [id: alert.id, dueIn: alert.dueIn(), dueDate: new SimpleDateFormat("yyyy-MM-dd").format(alert.dueDate), priorityId: newPriority.id]]

                }
            } as Callable)
        }
        futureList.each {
            if (it.get()) {
                activityList.add(it.get()['activity'])
                caseHistoryList.add(it.get()['caseHistoryAndExistingCaseHistoryMap'].caseHistory)
                Long existingCaseHistoryId = it.get()['caseHistoryAndExistingCaseHistoryMap'].existingCaseHistory
                if (existingCaseHistoryId) {
                    existingCaseHistoryList.add(existingCaseHistoryId)
                }
                responseDTO.data << (it.get()['dueInResponse'])
            }
        }
        executorService.shutdown()
        //Creating caseHistories in background
        notify 'activity.case.history.event.published', [existingCaseHistoryList: existingCaseHistoryList, caseHistoryList: caseHistoryList,
                                                         activityList           : activityList, isBulkUpdate: true]
        updatePriorityDueDate(responseDTO.data,domain)
        if (undoableDispositionIdList) {
            notify 'push.disposition.changes', [undoableDispositionList:undoableDispositionIdList]
        }
    }

    private Activity createActivityForBulkUpdate(def singleCaseAlert, Disposition newDisposition, Disposition previousDisposition,
                                                 String justification, String validatedSignalName) {
        ActivityType activityType
        String changeDetails
        User loggedInUser = cacheService.getUserByUserId(userService.getCurrentUserId())
        if (previousDisposition.isValidatedConfirmed()) {
            activityType = cacheService.getActivityTypeByValue(ActivityTypeValue.SignalAdded.value)
            changeDetails = "Case attached with Signal '$validatedSignalName'"
        } else {
            activityType = cacheService.getActivityTypeByValue(ActivityTypeValue.DispositionChange.value)
            if (validatedSignalName) {
                changeDetails = "Disposition changed from '$previousDisposition.displayName' to '${newDisposition?.displayName}' and attached with signal '${validatedSignalName}'"
            } else {
                changeDetails = "Disposition changed from '$previousDisposition.displayName' to '${newDisposition?.displayName}'"
            }
        }

        Activity activity = activityService.createActivityBulkUpdate(activityType, loggedInUser, changeDetails, justification,
                ['For Case Number': singleCaseAlert.caseNumber], singleCaseAlert.productName, singleCaseAlert.pt, singleCaseAlert.assignedToId ? cacheService.getUserByUserId(singleCaseAlert.assignedToId) : null, singleCaseAlert.caseNumber,
                singleCaseAlert.assignedToGroupId ? cacheService.getGroupByGroupId(singleCaseAlert.assignedToGroupId) : null, singleCaseAlert.dispLastChange)
        activity
    }

    List fetchSingleCaseAlertsForAssignedTo(List<Long> singleCaseAlertIdList, def domain) {
        List singleCaseAlertList = domain.createCriteria().list {
            'or' {
                singleCaseAlertIdList.collate(1000).each {
                    'in'("id", it)
                }
            }
        }
        singleCaseAlertList
    }

    List<String> fetchNewCases(List<Map> alertData) {
        Map<Long, Integer> caseIds = fetchCasesFromData(alertData)
        List<String> caseIdVersionList = caseIds.collect {
            "(" + it.key + "," + it.value + ")"
        }
        List<GlobalCase> oldCaseIds = GlobalCase.createCriteria().list {
            sqlRestriction(" (CASE_ID, VERSION_NUM) IN (${caseIdVersionList.join(",")})")
        }
        Map oldCaseIdsMap = [:]
        oldCaseIds.each {
            oldCaseIdsMap.put(it.caseId, it.versionNum)
        }
        Map newCases = caseIds - oldCaseIdsMap
        List<String> caseIdVersion = newCases.collect { it.key?.toString() + "-" + it.value?.toString() }
        return caseIdVersion
    }

    List fetchGlobalCases(List<Map> alertData) {
        Map<Long, Integer> caseIds = fetchCasesFromData(alertData)
        List<String> caseIdVersionList = caseIds.collect {
            "(" + it.key + "," + it.value + ")"
        }
        List<GlobalCase> globalCasesList = GlobalCase.createCriteria().list {
            sqlRestriction(" (CASE_ID, VERSION_NUM) IN (${caseIdVersionList.join(",")})")
        }
        return globalCasesList

    }

    Map<Long, Integer> fetchCasesFromData(List<Map> alertData) {
        Map<Long, Integer> caseIds = new HashMap<>()
        alertData.each { data ->
            caseIds.put(data[cacheService.getRptFieldIndexCache('masterCaseId')] ? data[cacheService.getRptFieldIndexCache('masterCaseId')] as Long : 0L, data[cacheService.getRptFieldIndexCache('masterVersionNum')] ? data[cacheService.getRptFieldIndexCache('masterVersionNum')] as Integer : 0)
        }
        return caseIds
    }

    Double generateCompletenessScore(Map data) {
        String completenessScore = data[cacheService.getRptFieldIndexCache('caseMasterPvrUdText12')]
        if (completenessScore && NumberUtils.isNumber(completenessScore)) {
            return Double.parseDouble(completenessScore)
        }
        return null
    }

    private void updatePriorityDueDate(List alertDueDateList, def domainName) {
        def domain = domainName == SingleCaseAlert ? 'single_case_alert' : 'ARCHIVED_SINGLE_CASE_ALERT'
        def size = Holders.config.signal.batch.size
        Sql sql = null
        try {
            sql = new Sql(dataSource)
            DateFormat dtFmt = new SimpleDateFormat("yyyy-MM-dd");
            sql.withBatch(size, "update ${domain} set priority_id = :val0, due_date = :val1 " +
                    "where id = :val2".toString(), { preparedStatement ->
                alertDueDateList.each { def obj ->
                    preparedStatement.addBatch(val0: obj.priorityId, val1: new Timestamp(dtFmt.parse(obj.dueDate).getTime()), val2: obj.id)
                }
            })
        } catch (Exception e) {
            log.error(e.printStackTrace())
        } finally {
            sql?.close()
        }
    }

    @Transactional
    void persistDispositionDueDate(List alertDueDateList, def domainName) {
        def domain = domainName == SingleCaseAlert ? 'SINGLE_CASE_ALERT' : 'ARCHIVED_SINGLE_CASE_ALERT'
        def size = Holders.config.signal.batch.size
        List<Long> reviewedDispositions = cacheService.getDispositionByReviewCompleted()*.id
        Sql sql = null
        try {
            sql = new Sql(dataSource)
            DateFormat dtFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
            sql.withBatch(size, "update ${domain} set disposition_id = :val0, due_date = :val1, review_date = :val2 " +
                    "where id = :val3".toString(), { preparedStatement ->
                alertDueDateList.each { def obj ->
                    preparedStatement.addBatch(val0: obj.dispositionId, val1: obj.dueDate ? new Timestamp(obj.dueDate.getTime()) : null,
                            val2: obj.reviewDate ? new Timestamp(dtFmt.parse(obj.reviewDate).getTime()): null, val3: obj.id)
                }
            })
            if (domainName == SingleCaseAlert) {
                sql.withBatch(size, "update ARCHIVED_SINGLE_CASE_ALERT set due_date = :val0 where case_number = :val1 and " +
                        "alert_configuration_id = :val2 and  exec_config_id != :val3".toString(), { preparedStatement ->
                    alertDueDateList.each { def obj ->
                        if (obj.dispositionId in reviewedDispositions) {
                            preparedStatement.addBatch(val0: null, val1: obj.caseNumber, val2: obj.configId, val3: obj.execConfigId)
                        }
                    }
                })
            }
        } catch (Exception e) {
            log.error(e.printStackTrace())
        } finally {
            sql?.close()
        }
    }

    void buildTagList(def tag, List tagsAndSubTags, List categoryDtoList, List pvsGlobalTagList, List pvsAlertTagList, Integer versionNum, Long execConfigId, Long caseId, Long caseSeriesId) {
        Boolean isSubTagPresent = false
        Long tagId = tagsAndSubTags.find { it.text == tag.tagText }?.id
        String tagName = tag.tagText
        Integer tagIndex = tag.alert ? Holders.config.category.singleCase.alertSpecific : Holders.config.category.singleCase.global
        String globalCaseId = caseId.toString() + '-' + versionNum.toString()
        if (tag.alert) {
            globalCaseId = caseSeriesId + '-' + caseId + '-' + versionNum
        }
        tag.subTags.each { subTagName ->
            isSubTagPresent = true
            Map subTag = tagsAndSubTags.find { it.text == subTagName && it.parentId == tagId }
            Long subTagId = subTag?.id

            CategoryDTO category = pvsGlobalTagService.fetchCategoryDto((Constants.DataSource.PVA).toUpperCase(), tagName, tagId, globalCaseId, tagIndex, subTagName, subTagId, 0,
                    true, false, execConfigId)
            categoryDtoList.add(category)
            if (tag.alert) {
                PvsAlertTag pvsAlertTag = pvsAlertTagService.fetchPvsAlertTagObject(tagName, tagId, dataObjectService.getGlobalCaseAlertMap(execConfigId, caseId)?.id, Constants.AlertConfigType.SINGLE_CASE_ALERT, subTagName, subTagId,
                        true, false, execConfigId)
                pvsAlertTagList.add(pvsAlertTag)
            } else {
                PvsGlobalTag pvsGlobalTag = pvsGlobalTagService.fetchPvsGlobalTagObject(tagName, tagId, dataObjectService.getGlobalCase(execConfigId, caseId, versionNum)?.id, Constants.AlertConfigType.SINGLE_CASE_ALERT, subTagName, subTagId,
                        true, false, execConfigId)
                pvsGlobalTagList.add(pvsGlobalTag)
            }
        }
        if (!isSubTagPresent) {
            CategoryDTO category = pvsGlobalTagService.fetchCategoryDto((Constants.DataSource.PVA).toUpperCase(), tagName, tagId, globalCaseId, tagIndex, null, null,
                    0, true, false, execConfigId)
            categoryDtoList.add(category)
            if (tag.alert) {
                PvsAlertTag pvsAlertTag = pvsAlertTagService.fetchPvsAlertTagObject(tagName, tagId, dataObjectService.getGlobalCaseAlertMap(execConfigId, caseId)?.id, Constants.AlertConfigType.SINGLE_CASE_ALERT,
                        null, null, true, false, execConfigId)
                pvsAlertTagList.add(pvsAlertTag)
            } else {
                PvsGlobalTag pvsGlobalTag = pvsGlobalTagService.fetchPvsGlobalTagObject(tagName, tagId, dataObjectService.getGlobalCase(execConfigId, caseId, versionNum)?.id, Constants.AlertConfigType.SINGLE_CASE_ALERT,
                        null, null, true, false, execConfigId)
                pvsGlobalTagList.add(pvsGlobalTag)
            }
        }
    }

    List getCaseAlertMapping(List alertData) {
        List<Map> caseAlertMapping = []
        alertData.each {
            caseAlertMapping.add([id: it.id, caseId: it.globalIdentity?.caseId])
        }
        caseAlertMapping
    }

    void dissociateSingleCaseAlertFromSignal(def alert, Disposition targetDisposition, String justification, ValidatedSignal signal,
                                             Boolean isArchived) {
        if(alert?.executedAlertConfiguration?.isStandalone){
            // skipping audit for manually added case which has Validated Signal as configuration
            alert.skipAudit = true
        }
        Disposition previousDisposition = alert.disposition
        DashboardCountDTO dashboardCountDTO = alertService.prepareDashboardCountDTO(true)
        String prevDueDate = alert.dueDate ? DateUtil.stringFromDate(alert.dueDate, Constants.DateFormat.STANDARD_DATE, "UTC") : null
        boolean isTargetDispReviewed = targetDisposition.reviewCompleted
        validatedSignalService.changeToInitialDisposition(alert, signal, targetDisposition)

        createCaseHistoryForDispositionChange(alert, justification, alert.executedAlertConfiguration, alert.alertConfiguration, null, isArchived)
        saveAlertCaseHistory(alert, justification)
        String changeDetails = "Disposition changed from '$previousDisposition' to '$targetDisposition' and dissociated from signal '$signal.name'"
        Map attrs = [product: getNameFieldFromJson(alert.alertConfiguration.productSelection),
                     event  : getNameFieldFromJson(alert.alertConfiguration.eventSelection)]
        activityService.createActivity(alert.executedAlertConfiguration, ActivityType.findByValue(ActivityTypeValue.DispositionChange), userService.getUser(),
                changeDetails, justification, attrs, alert.productName, alert.pt, alert.assignedTo, alert.caseNumber)

        //For Dashboard Counts
        if (targetDisposition?.id && !(alert.executedAlertConfiguration?.isStandalone)) {
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
        }
        if (!isArchived && !(alert.isCaseSeries) && !(alert.executedAlertConfiguration?.isStandalone)) {
            alertService.updateDashboardCountsForDispChange(dashboardCountDTO, isTargetDispReviewed)
            alertService.updateDashboardGroupCountsForDispChange(dashboardCountDTO, isTargetDispReviewed)
            def totalPrevDispCount = dashboardCountDTO.prevDispCountMap ?: [:]
            totalPrevDispCount = totalPrevDispCount + dashboardCountDTO.prevGroupDispCountMap ?: [:]
            alertService.updateDispCountsForExecutedConfiguration(dashboardCountDTO.execDispCountMap, totalPrevDispCount)
        }
        if (isArchived) { // this block is introduced to update disp count for archived alerts
            def storePrevDispCountMap = dashboardCountDTO.prevDispCountMap
            alertService.updateDashboardCountMaps(dashboardCountDTO.prevDispCountMap, alert.assignedToId, previousDisposition.id.toString())
            def totalPrevDispCount = dashboardCountDTO.prevDispCountMap ?: [:]
            totalPrevDispCount = totalPrevDispCount + dashboardCountDTO.prevGroupDispCountMap ?: [:]
            alertService.updateDispCountsForExecutedConfiguration(dashboardCountDTO.execDispCountMap, totalPrevDispCount)
            dashboardCountDTO.prevDispCountMap = storePrevDispCountMap
        }
    }
    //generate the criteria sheet data
    List getSingleCaseAlertCriteriaData(ExecutedConfiguration ec, Map params, String alertType = null,Boolean checkPecCount = false,Boolean isJader = false) {
        String all = Constants.Commons.ALL.toLowerCase().substring(0, 1).toUpperCase() + Constants.Commons.ALL.toLowerCase().substring(1)
        String none = Constants.Commons.NONE.toLowerCase().substring(0, 1).toUpperCase() + Constants.Commons.NONE.toLowerCase().substring(1)
        String timeZone = userService.getCurrentUserPreference()?.timeZone
        ViewInstance viewInstance = viewInstanceService.fetchSelectedViewInstance(alertType ? alertType : SINGLE_CASE_ALERT, params?.viewId as Long)
        //current advancedFilter in view
        AdvancedFilter advancedFilter = AdvancedFilter.findById(params?.advancedFilterId)

        List dispositionFilters = getFiltersFromParams(params.isFilterRequest?.toBoolean(), params)

        String dateRange = DateUtil.toDateString(ec.executedAlertDateRangeInformation.dateRangeStartAbsolute) +
                " - " + DateUtil.toDateString(ec.executedAlertDateRangeInformation.dateRangeEndAbsolute)
        List<String> queryParameters = []
        String queryName = ''
        if (ec.executedAlertQueryId) {
            queryName = ec.alertQueryName
            if (ec.executedAlertQueryValueLists.size() > 0) {
                ec.executedAlertQueryValueLists.each { eaqvl ->
                    StringBuilder queryParameter = new StringBuilder()
                    eaqvl.parameterValues.each { parameter ->
                        if (parameter.hasProperty('reportField')) {
                            queryParameter.append(messageSource.getMessage("app.reportField.${parameter.reportField.name}", null, Locale.default))
                            queryParameter.append(" ")
                            queryParameter.append(messageSource.getMessage("${parameter.operator.getI18nKey()}", null, Locale.default))
                            queryParameter.append(" ")
                            if(parameter.value)
                            {
                                queryParameter.append(parameter.value)
                            }else{
                                if(queryName){
                                    queryParameter.append(all)
                                }else{
                                    queryParameter.append(none)
                                }
                            }
                            queryParameters.add(queryParameter.toString())
                        } else {
                            queryParameters.add("${parameter.key} : ${parameter.value}")
                        }
                        queryParameter.setLength(0);
                    }
                }
            }
        }

        JsonSlurper js = new JsonSlurper()
        def caseListCols = js.parseText(viewInstance.columnSeq)

        def sorting = js.parseText(viewInstance?.sorting)

        String sortColoum = ""
        Map fixedColSeq = [
                3: "Case(f/u#)",
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
            List<Map> filterColumns = grailsApplication.config.configurations.agaColumnOrderList.clone() as List<Map>
            filterColumns.each{
                if(it.name == params.column){
                    sortColoum = it.label + ": " + params.sorting
                }
            }
        }
        Map filterMap = [:]
        if (params.filterList && params.filterList != "{}") {
            def jsonSlurper = new JsonSlurper()
            filterMap = jsonSlurper.parseText(params.filterList)
        }

        List filtersList = []
        String filter = ""

        Map rptToUiLabelMap = cacheService.getRptToUiLabelMapForSafety() as Map
        Map rptToSignalFieldMap = Holders.config.icrFields.rptMap
        if (filterMap.containsKey("caseNumber")) {
            def object =  filterMap.find {it.key == "caseNumber"}
            if (Objects.nonNull(ec) && Constants.DataSource.PVA.equals(ec.selectedDatasource)) {
                filtersList.add((rptToUiLabelMap.get('masterCaseNum') as String) + ": " + object.value)
            } else {
                filtersList.add("Case: " + object.value)
            }
        }
        // Introduced a map 'updatedFilterMap' to map specific keys to their corresponding updated values as part of PVS-64884
        Map updatedFilterMap = ['assessListedness': 'listedness', 'assessSeriousness': 'serious']

        filterMap.each { filterValue ->
            Set keySet = rptToSignalFieldMap.keySet()
            String filterValKey = filterValue.key
            String updatedFilterValKey = updatedFilterMap.get(filterValKey)
            if ((filterValKey in keySet || updatedFilterValKey in keySet) && filterValKey != "caseNumber" && ec.selectedDatasource == Constants.DataSource.PVA) {
                [filterValKey, updatedFilterValKey].each { key ->
                    if (key in keySet) {
                        String rptField = rptToSignalFieldMap.get(key)
                        String uiLabel = rptToUiLabelMap.get(rptField)
                        if (uiLabel != null) {
                            filtersList.add(uiLabel + ": " + filterValue.value)
                        }
                    }
                }
            } else {
                caseListCols.each {
                    if (filterValue.key == it.value.name) {
                        filtersList.add(it.value.label + ": " + filterValue.value)
                    }
                }
            }
        }

        filter = filtersList.join("\n")
        String productSelection = ec?.productSelection ? ViewHelper.getDictionaryValues(ec, DictionaryTypeEnum.PRODUCT) : ViewHelper.getDictionaryValues(ec, DictionaryTypeEnum.PRODUCT_GROUP)
        String productName = ""
        if(ec.dataMiningVariable && ec.adhocRun){
            productName = ec.dataMiningVariable
            if(productSelection){
                productName = productName + "(" + productSelection + ")"
            }
        } else {
            productName = productSelection
        }
        String drugRecordSelection = ec?.productSelection ? ViewHelper.getDictionaryValues(ec, DictionaryTypeEnum.PRODUCT, true) : ""

        ExecutedConfiguration ec1 = ExecutedConfiguration.get(params?.aggExecutionId)
        String dateRangeType = ""
        String dateRangeType1 = ""
        ViewHelper.getDateRangeTypeI18n().each {
            if (it.name == ec.dateRangeType) {
                dateRangeType = it.display
            }
        }
        ViewHelper.getDateRangeTypeI18n().each {
            if (it.name == ec1?.dateRangeType) {
                dateRangeType1 = it.display
            }
        }
        String date_range_type,evaluate_on,as_of_date = ""
        if(params.isCaseSeries != "true" && params.isCaseSeries != true){
            date_range_type = ec.dateRangeType ? dateRangeType : Constants.Commons.BLANK_STRING
            evaluate_on = ec.evaluateDateAs == EvaluateCaseDateEnum.LATEST_VERSION ? "Latest Version" : "Version As Of"
            as_of_date = ec.evaluateDateAs == EvaluateCaseDateEnum.LATEST_VERSION ? "" : DateUtil.fromDateToString(ec.asOfVersionDate, DateUtil.DATEPICKER_FORMAT)
        }else{
            date_range_type = ec1?.dateRangeType ? dateRangeType1 : Constants.Commons.BLANK_STRING
            evaluate_on = ec1?.evaluateDateAs == EvaluateCaseDateEnum.LATEST_VERSION ? "Latest Version" : "Version As Of"
            as_of_date = ec1?.evaluateDateAs == EvaluateCaseDateEnum.LATEST_VERSION ? "" : DateUtil.fromDateToString(ec1?.asOfVersionDate, DateUtil.DATEPICKER_FORMAT)
        }

        String substanceLabel = Constants.CriteriaSheetLabels.MULTI_SUBSTANCE
        if (!dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM)) {
            substanceLabel = Constants.CriteriaSheetLabels.MULTI_INGREDIENT
        }

        Boolean isPva = false
        if (Objects.nonNull(ec) && Constants.DataSource.PVA.equals(ec.selectedDatasource)) {
            isPva = true
        }


    List criteriaSheetList = [
            ['label': Constants.CriteriaSheetLabels.ALERT_NAME, 'value': ec?.name],
            ['label': Constants.CriteriaSheetLabels.DESCRIPTION, 'value': ec?.description ? ec?.description : Constants.Commons.BLANK_STRING],
            ['label': Constants.CriteriaSheetLabels.PRODUCT, 'value': productName],
            (isPva ? ['label': substanceLabel, 'value': ec?.isMultiIngredient ? Constants.Commons.YES : Constants.Commons.NO]: null),
            (isPva ? ['label': Constants.CriteriaSheetLabels.INCLUDE_WHO_DRUGS, 'value': ec?.includeWHODrugs ? Constants.Commons.YES : Constants.Commons.NO]: null),
            (isPva && ec && ec.includeWHODrugs && ec.productSelection ? ['label': Constants.CriteriaSheetLabels.DRUG_RECORD_NUMBER, 'value': drugRecordSelection]: null),

            (!isJader ? ['label': Constants.CriteriaSheetLabels.SUSPECT_PRODUCT, 'value': ec?.suspectProduct ? Constants.Commons.YES : Constants.Commons.NO]: null),
            ['label': Constants.CriteriaSheetLabels.EVENT_SELECTION, 'value': ec.eventSelection ? getAllEventNameFieldFromJson(ec.eventSelection) : (getGroupNameFieldFromJson(ec.eventGroupSelection) ?: Constants.Commons.BLANK_STRING)], //Added for PVS-55056
            (!isJader ? ['label': Constants.CriteriaSheetLabels.QUERY_NAME, 'value': ec.alertQueryName ? queryName : none]:null),
            (!isJader ? ['label': Constants.CriteriaSheetLabels.QUERY_PARAMETERS, 'value': ec.executedAlertQueryValueLists ? queryParameters.join(', ') : none]:null),     //TO do it while reports is on
            ['label': Constants.CriteriaSheetLabels.DATE_RANGE_TYPE, 'value': date_range_type] ,
            ['label': Constants.CriteriaSheetLabels.DATE_RANGE, 'value': dateRange ? dateRange : Constants.Commons.BLANK_STRING],
            ['label': Constants.CriteriaSheetLabels.EVAULATE_ON, 'value': evaluate_on ?: Constants.Commons.BLANK_STRING],
            (!isJader ? ['label': Constants.CriteriaSheetLabels.LIMIT_TO_CASE_SERIES, 'value': ec.alertCaseSeriesName ?: Constants.Commons.BLANK_STRING]: null),
            (!isJader ? ['label': Constants.CriteriaSheetLabels.AS_OF_DATE, 'value': as_of_date ?: Constants.Commons.BLANK_STRING] :null),
            (!isJader ? ['label': Constants.CriteriaSheetLabels.INCLUDE_LOCKED_VERSION, 'value': (ec.selectedDatasource == "vaers" || ec.selectedDatasource == "vigibase") ? "No" : ec?.includeLockedVersion ? "Yes" : "No"]:null),
            (!isJader ? ['label': Constants.CriteriaSheetLabels.EXCLUDE_NON_VALID_CASE, 'value': (ec.selectedDatasource == "vaers" || ec.selectedDatasource == "vigibase") ? "No" :  ec?.excludeNonValidCases ? "Yes" : "No"]:null),
            ['label': Constants.CriteriaSheetLabels.EXCLUDE_FOLLOW_UP, 'value': ec?.excludeFollowUp ? "Yes" : "No"],
            (!isJader ?['label': Constants.CriteriaSheetLabels.MISSED_CASE, 'value': (ec.selectedDatasource == "vaers" || ec.selectedDatasource == "vigibase") ? "No" :  ec?.missedCases ? "Yes" : "No"]:null),
            ['label': Constants.CriteriaSheetLabels.INCLUDE_STUDY_CASES, 'value': (ec.studyCases ? (ec?.studyDrugs?:Constants.Commons.BLANK_STRING) : 'No')],
            ['label': Constants.CriteriaSheetLabels.CONSIDER_ONLY_POI, 'value': ec.onlyPoi? "Yes": "No"],
            ['label': Constants.CriteriaSheetLabels.IGNORE_STUDY_MEDICATION, 'value': ec?.ignoreStudyType ? "Yes": "No"],
            ['label': Constants.CriteriaSheetLabels.VIEW, 'value': viewInstance ? viewInstance.name : Constants.Commons.BLANK_STRING],
            ['label': Constants.CriteriaSheetLabels.FILTER, 'value': advancedFilter ? advancedFilter?.name : Constants.Commons.NA_LISTED],
            ['label': Constants.CriteriaSheetLabels.COLUMN_LEVEL_FILTER, value: filter?:""],
            (!isJader ?['label': Constants.CriteriaSheetLabels.DISPOSITIONS, 'value': params.quickFilterDisposition?:Constants.Commons.BLANK_STRING]:null),
            ['label': Constants.CriteriaSheetLabels.SORT_ORDER, 'value': sortColoum?:Constants.Commons.BLANK_STRING],
            (!checkPecCount ? ['label': Constants.CriteriaSheetLabels.CASE_COUNT, 'value': params.totalCount ?: Constants.Commons.BLANK_STRING] : null),
            ['label': Constants.CriteriaSheetLabels.DATE_CREATED, 'value': DateUtil.stringFromDate(ec?.dateCreated, DateUtil.DATEPICKER_FORMAT_AM_PM, timeZone)],
            ['label': Constants.CriteriaSheetLabels.REPORT_GENERATED_BY, 'value': userService.getUser().fullName?:""],
            ['label': Constants.CriteriaSheetLabels.DATE_EXPORTED, 'value': (DateUtil.stringFromDate(new Date(), DateUtil.DATEPICKER_FORMAT_AM_PM, timeZone) + userService.getGmtOffset(timeZone))]
    ].findAll { it != null }
    return criteriaSheetList

    }

    private Map getTransformedMap(Map data) {
        Map rptMapping = [:]
        data.keySet().each { it ->
            int index = it.lastIndexOf("_");
            if (index >= 0) {
                String newKey = it.substring(0, index);
                rptMapping.put(newKey, it)
            } else {
                rptMapping.put(it, it)
            }
        }
        Map currentMap = cacheService.getCache("rptFieldIndexCache")
        Map result = [:]
        Map newOldRptMapping = Holders.config.newOldRptMapping
        rptMapping.keySet().each { it ->
            String newKey = newOldRptMapping.get(it)
            if(newKey) {
                result.put(newKey + "_" +currentMap.get(newKey), data.get(rptMapping.get(it)))
            } else {
                result.put(it + "_" +currentMap.get(it), data.get(rptMapping.get(it)))
            }
        }
        result
    }

    /**
     * get distict values while preparing
     * auto populated advanced filter values
     * @param domain
     * @param exConfigid
     * @param field
     * @return
     */
    List<String> getDistinctValues(def domain, Long exConfigid, String field) {
        List<String> outcomes = domain.createCriteria().list {
            projections {
                property(field)
            }
            isNotNull(field)
            'ne'(field, "undefined")
            if (exConfigid > 0)
                'eq'("executedAlertConfiguration.id", exConfigid)
        } as List<String>
        if (domain == SingleCaseAlert) {
            List blindedList = [Constants.BlindingStatus.REDACTED, Constants.BlindingStatus.BLINDED]
            outcomes.addAll(blindedList)
        }
        outcomes ? outcomes.sort() : []

    }

    def getDistinctOutcomeValuesSql(String field) {
        def startTime = System.currentTimeSeconds()
        def finalList = []
        User currentUser = userService.getUser()
        List<Long> groupIdList = currentUser.groups.collect { it.id as Long }
        def sql_statement = "select distinct  gender from SINGLE_CASE_ALERT where gender is not null" +
                " and gender<>'undefined' and ASSIGNED_TO_ID=${currentUser.id} or ASSIGNED_TO_GROUP_ID in (${groupIdList.join(",")})"
        try {
            switch (field) {
                case "gender":
                    sql_statement = "select distinct  gender from SINGLE_CASE_ALERT where gender is not null" +
                            " and gender<>'undefined' and ASSIGNED_TO_ID=${currentUser.id} or ASSIGNED_TO_GROUP_ID in (${groupIdList.join(",")})"
                    break;
                case "outcome":
                    sql_statement = "select distinct  outcome from SINGLE_CASE_ALERT where outcome is not null" +
                            " and outcome<>'undefined' and ASSIGNED_TO_ID=${currentUser.id} or ASSIGNED_TO_GROUP_ID in (${groupIdList.join(",")})"
                    break;
                case "region":
                    sql_statement = "select distinct  region from SINGLE_CASE_ALERT where region is not null" +
                            " and region<>'undefined' and ASSIGNED_TO_ID=${currentUser.id} or ASSIGNED_TO_GROUP_ID in (${groupIdList.join(",")})"
                    break;
                case "death":
                    sql_statement = "select distinct  death from SINGLE_CASE_ALERT where death is not null" +
                            " and death<>'undefined' and ASSIGNED_TO_ID=${currentUser.id} or ASSIGNED_TO_GROUP_ID in (${groupIdList.join(",")})"
                    break;
            }
            def data
            def sql = new Sql(signalDataSourceService.getReportConnection("dataSource"))
            data = sql.rows(sql_statement)
            data?.each {
                if (it.get(field.toUpperCase()) != null) {
                    finalList.add(it.get(field.toUpperCase()))
                }
            }
            finalList += [Constants.BlindingStatus.REDACTED,Constants.BlindingStatus.BLINDED]
            log.info("time taken to fetch advance filter data for ${field} is ${System.currentTimeSeconds() - startTime}")
            return finalList.sort()
        } catch (Exception ex) {
            return []
            ex.printStackTrace()
        }

    }
    def getListListDataSql()
    {
        Map<String, HashSet<String>> fieldsData = [flags: new HashSet<String>(), productName: new HashSet<String>(), appTypeNum: new HashSet<String>()]
        User currentUser = userService.getUser()
        List<Long> groupIdList = currentUser.groups.collect { it.id as Long }
        Sql sql=null
        try {
            def sql_statement = "select sca.product_name,sca.badge,sca.APP_TYPE_AND_NUM from SINGLE_CASE_ALERT sca " +
                    "left outer join EX_RCONFIG ex on sca.EXEC_CONFIG_ID = ex.ID where sca.ASSIGNED_TO_ID=${currentUser.id} " +
                    "or sca.ASSIGNED_TO_GROUP_ID in (${groupIdList.join(",").replaceAll(" ", "")}) and ex.ADHOC_RUN=0"

            def data
            sql = new Sql(signalDataSourceService.getReportConnection("dataSource"))
            data = sql.rows(sql_statement)
            data?.each {
                if (it.PRODUCT_NAME != null)
                    fieldsData.get("productName").add(it.PRODUCT_NAME)

                if (it.BADGE != null)
                    fieldsData.get("flags").add(it.BADGE)

                if (it.APP_TYPE_AND_NUM != null)
                    fieldsData.get("appTypeNum").add(it.APP_TYPE_AND_NUM)
            }
            return fieldsData
        }catch(Exception ex){
            log.info("Some error occured while fetching prefilled values for advanced filter")
            return fieldsData
        }finally{
            sql?.close()
        }
    }

    @Transactional
    void generateExecutedCaseSeriesAsync(ExecutedConfiguration executedConfiguration, boolean isTemporary, boolean isCumulative = false) {
        log.info("generateExecutedCaseSeriesAsync started..............")
        ExecutorService executorService = signalExecutorService.threadPoolForCaseSeriesExec()
        executorService.submit(new Callable<Long>() {
            @Override
            Long call() throws Exception {
                Long seriesId = null
                seriesId = generateExecutedCaseSeries(executedConfiguration, isTemporary, isCumulative)
                if (Objects.nonNull(seriesId)) {
                    log.info("seriesId.............." + seriesId)
                    String updateCaseSeriesHql = alertService.prepareUpdateCaseSeriesHql(executedConfiguration.executedAlertDateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE)
                    log.debug("prepareUpdateCaseSeriesHql executed with returning updateCaseSeriesHql :" + updateCaseSeriesHql)
                    ExecutedConfiguration.withTransaction{ExecutedConfiguration.executeUpdate(updateCaseSeriesHql, [pvrCaseSeriesId: seriesId, id: executedConfiguration.id])}
                    log.debug("executeUpdate executed.")
                    log.info("generateExecutedCaseSeriesAsync finished.......")
                }
                return seriesId
            }
        })
    }

    Boolean dssComparisionOperation(Double value, Double threshold, String operator) {
        switch(operator) {
            case ">":
                return value > threshold
            case ">=":
                return value >= threshold
            case "<":
                return value < threshold
            case "<=":
                return value <= threshold
            case "==":
                return value == threshold
            default:
                return false
        }

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

    void syncAssignedAndShareWithFieldsParentAgg(ExecutedConfiguration icrExConfig,ExecutedConfiguration aggExConfig) {
        try {
            /* scenario is -> user create agg alert and open a case series afterthen it share the same agg
            alert to another user/group and then shared user open the same case series then it will show access issue.
            To handle this situation on each open of case series we must sync the shareWith and assignedTo fields with parent agg */

            Configuration icrConfig = Configuration.get(icrExConfig.configId)
            Configuration aggConfig = Configuration.get(aggExConfig.configId)

            //Updating Configuration for Icr from Agg Configuration
            icrConfig.assignedTo = aggConfig.assignedTo
            icrConfig.assignedToGroup = aggConfig.assignedToGroup

            List<User> autoShareWithUserList = []
            List<Group> autoShareWithGroupList = []
            List<User> shareWithUserList = []
            List<Group> shareWithGroupList = []
            aggConfig?.autoShareWithUser.each { User usr ->
                autoShareWithUserList.add(usr)
            }
            aggConfig?.autoShareWithGroup.each { Group grp ->
                autoShareWithGroupList.add(grp)
            }
            aggConfig?.shareWithUser.each { User usr ->
                shareWithUserList.add(usr)
            }
            aggConfig?.shareWithGroup.each { Group grp ->
                shareWithGroupList.add(grp)
            }
            icrConfig.autoShareWithUser = autoShareWithUserList
            icrConfig.shareWithUser = shareWithUserList
            icrConfig.autoShareWithGroup = autoShareWithGroupList
            icrConfig.shareWithGroup = shareWithGroupList
            icrConfig.skipAudit = true
            icrConfig.save(failOnError: true, flush:true)

            //Updating ExConfiguration for Icr from Icr Configuration
            icrExConfig.assignedTo = icrConfig.assignedTo
            icrExConfig.assignedToGroup = icrConfig.assignedToGroup
            autoShareWithUserList = []
            autoShareWithGroupList = []
            icrConfig.autoShareWithUser.each { User usr ->
                autoShareWithUserList.add(usr)
            }
            icrConfig.autoShareWithGroup.each { Group grp ->
                autoShareWithGroupList.add(grp)
            }
            icrExConfig.autoShareWithUser = autoShareWithUserList
            icrExConfig.autoShareWithGroup = autoShareWithGroupList
            icrExConfig.save(failOnError: true, flush:true)

        }
        catch (Exception ex) {
            log.error("Some error occured while syncing case series shareWith and assignedTo fields with parent aggregate alert")
            ex.printStackTrace()
        }
    }

    boolean stringStartsWithNumber(String str){
        def pattern = Pattern.compile(/^\d+\)/)
        return pattern.matcher(str).find()
    }

    /**
     * helper function to populate attribute map
     * while creating single case alert map for details screen
     * @param attributes
     * @return map
     */
    Map populateAttributesMap(String attributes) {
        JsonSlurper jsonSlurper = new JsonSlurper()
        Map transformedMap = (attributes ? jsonSlurper.parseText(attributes) ?: [:] : [:]) as Map
        if (transformedMap) {
            transformedMap = getTransformedMap(transformedMap as Map)
        }
        transformedMap
    }

    /**
     * Modifies data for fields which are fetched using report fields in single case alert
     * @param attrName
     * @param attributes
     * @return map or null
     */
    def getAttr(String attrName,String attributes) {

        Map attributesMap = populateAttributesMap(attributes)

        if (attributesMap) {
            attributesMap[attrName]
        } else {
            null
        }
    }

    /**
     * Helper function to generate hcp flag value while
     * creating map for single case alert
     * @param map
     * @return value of reportersHcpFlag
     */
    String generateReportersHcpFlagValueSql(Map map) {
        try {
            if (map.reportershcpflag == null || map.reportershcpflag.trim().equalsIgnoreCase('null'))
                return '-'
        } catch (Exception e) {
            return '-'
        }
        return map.reportershcpflag
    }

    /**
     * Converts disposition id into disposition object via cache CACHE_NAME_DISPOSITION
     * @param dispositionId
     * @return disposition object
     */
    Disposition getDispositionById(Long dispositionId) {
        cacheService.getDispositionByValue(dispositionId)
    }

    /**
     * Generate String for listedness while creating map
     * for single case alert details screen
     * @param isExport
     * @param map
     * @return string value of listedness
     */
    String getAssessListedness(Boolean isExport,Map map) {
        if (!isExport) {
            return getAttr(cacheService.getRptFieldIndexCache('assessListedness'),map.attributes) ?: Constants.Commons.DASH_STRING
        } else {
            return (getAttr(cacheService.getRptFieldIndexCache('assessListedness'), map.attributes) ?: getAttr(cacheService.getRptFieldIndexCache('assessOutcome'),map.attributes) ?: Constants.Commons.DASH_STRING)
        }
    }

    /**
     * Accepts priority id and return a map containing information
     * about priority value and priority icon class
     * @param priorityId
     * @return mao of priority value and priority icon class
     */
    Map getPriorityMap(Long priorityId) {
        Priority priority = cacheService.getPriorityByValue(priorityId)
        [value: priority?.value, iconClass: priority?.iconClass]
    }

    /**
     * Accepts priority id and converts it into its display name
     * @param priorityId
     * @return prority name
     */
    String priorityDisplayName(Long priorityId) {
        Priority priority = cacheService.getPriorityByValue(priorityId)
        priority.displayName
    }

    /**
     * Converts assinged to id into its full name / group name
     * @param map
     * @return full name/ group name
     */
    String getAssignedToName(Map map) {
        map.assignedto ? getUserByUserId(map.assignedto as Long).fullName : getGroupByGroupId(map.assignedtogroup as Long).name
    }

    /**
     * Accepts userId and returns user object
     * @param userId
     * @return user object
     */
    User getUserByUserId(Long userId) {
        cacheService.getUserByUserId(userId)
    }


    /**
     * Accepts group id and retrurns group object
     * @param groupId
     * @return user group object
     */
    Group getGroupByGroupId(Long groupId) {

        cacheService.getGroupByGroupId(groupId)
    }


    /**
     * Converts assigned to id in user/group object map
     * @param map
     * @return user/group object map
     */
    Map assignedToMap(Map map) {
        map.assignedto ? getUserByUserId(map.assignedto as Long).toMap() : getGroupByGroupId(map.assignedtogroup as Long).toMap()
    }


    /**
     * Checks if the case is in its default state or not
     * @param map
     * @return
     */
    String isDefaultState(Map map){
        if(!map.isdispchanged && getDispositionById(map.disposition as Long) == getDefaultDisp() && map.dispperformedby == null){
            return 'true'
        }
        return 'false'
    }


    /**
     * function returns default disposition from cache
     * @return default disposition
     */
    Disposition getDefaultDisp(){
        cacheService.getDefaultDisp(Constants.AlertConfigType.SINGLE_CASE_ALERT)
    }

    /**
     * get signal name from list of signals
     * @param signals
     * @return signal name
     */
    String getSignalName(List signals) {
        String signalName = ""
        signalName = signals.collect { it.name }?.join(",")
        return signalName
    }

    /**
     * Convert due in into days
     * @param date
     * @return due in
     */

    int dueIn(String date) {
        String dateFormat = "yyyy-MM-dd HH:mm:ss.SSS"
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat)
        Date dueDate = sdf.parse(date)

        DateTime theDueDate = new DateTime(dueDate).withTimeAtStartOfDay()
        DateTime now = DateTime.now().withTimeAtStartOfDay()
        Duration dur = new Duration(now, theDueDate)
        return dur.getStandardDays().intValue()
    }

    /**
     * convert single case alert object to format required on UI.
     * @param timeZone
     * @param validatedSignal
     * @param isExport
     * @param comment
     * @param isAttachment
     * @param justification
     * @param isUndoable
     * @param scaMap
     * @return Map of single case alert data
     */

    @Transactional
    Map composeAlert(String timeZone = Constants.UTC, List validatedSignal = [], Boolean isExport = false, String comment = null,
                     Boolean isAttachment = false, String justification = null, Boolean isUndoable = false, Map scaMap) {

        DecimalFormat df = new DecimalFormat('#.##')
        Disposition dispositionOfCase = getDispositionById(scaMap.disposition as Long)
        String dateFormat = "yyyy-MM-dd HH:mm:ss.SSS"
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat)
        Date detectedDate = scaMap.detecteddate ? sdf.parse(scaMap.detecteddate as String) : null
        Date dueDate = scaMap.duedate ? sdf.parse(scaMap.duedate as String) : null

        Map signalCaseData = [
                alertConfigId            : scaMap.alertconfiguration as Long,
                id                       : scaMap.ids as Long,
                alertName                : scaMap.name,
                caseNumber               : isExport ? getExportCaseNumber(scaMap.actualcasenumber as String, scaMap.followupnumber as Integer) : scaMap.casenumber,
                detectedDate             : toDateString(detectedDate, timeZone),
                dueDate                  : isExport ? (!Disposition.get(scaMap.dispostion as Long).reviewCompleted ? dueIn(scaMap.duedate as String) + "" : "") : toDateString(dueDate, timeZone),
                productName              : scaMap.productname,
                productId                : getAttr(cacheService.getRptFieldIndexCache('cpiProdIdResolved'), scaMap.attributes),
                pt                       : scaMap.pt,
                productFamily            : scaMap.productfamily,
                flagged                  : scaMap.flagged,
                caseReportType           : scaMap.casereporttype ?: Constants.Commons.DASH_STRING,
                caseInitReceiptDate      : getCaseInitReceiptDate(scaMap),
                reportersHcpFlag         : generateReportersHcpFlagValueSql(scaMap),
                masterPrefTermAll        : scaMap.masterpreftermall,
                assessOutcome            : getAttr(cacheService.getRptFieldIndexCache('assessOutcome'), scaMap.attributes) ?: Constants.Commons.DASH_STRING,
                assessListedness         : getAssessListedness(isExport, scaMap),
                assessSeriousness        : getAttr(cacheService.getRptFieldIndexCache('assessSeriousness'), scaMap.attributes),
                serious                  : scaMap.serious ?: Constants.Commons.DASH_STRING,
                primaryEvent             : getAttr(cacheService.getRptFieldIndexCache('masterPrimEvtPrefTerm'), scaMap.attributes),
                outcome                  : scaMap.outcome ?: Constants.Commons.DASH_STRING,
                listedness               : scaMap.listedness ?: Constants.Commons.DASH_STRING,
                country                  : scaMap.country ?: Constants.Commons.DASH_STRING,
                suspProd                 : getSuspProd(scaMap),
                conComit                 : scaMap.concomit ?: Constants.Commons.DASH_STRING,
                gender                   : scaMap.gender ?: Constants.Commons.DASH_STRING,
                age                      : scaMap.age ?: Constants.Commons.DASH_STRING,
                rechallenge              : scaMap.rechallenge ?: Constants.Commons.DASH_STRING,
                lockedDate               : getLockedDate(scaMap),
                death                    : scaMap.death ?: Constants.Commons.DASH_STRING,
                caseVersion              : scaMap.caseversion,
                dueIn                    : getDueIn(scaMap),
                followUpNumber           : scaMap.followupnumber ?: Constants.Commons.UNDEFINED_NUM,
                followUpExists           : scaMap.followupexists,
                execConfigId             : scaMap.executedalertconfiguration,
                disposition              : dispositionOfCase?.displayName,
                currentDisposition       : dispositionOfCase?.displayName,
                currentDispositionId     : scaMap.disposition,
                dispositionCloseStatus   : dispositionOfCase?.closed,
                priority                 : getPriority(isExport, scaMap),
                assignedToUser           : getAssignedToUser(isExport, scaMap),
                actionCount              : scaMap.actioncount,
                caseType                 : scaMap.casetype ?: Constants.Commons.DASH_STRING,
                completenessScore        : scaMap.completenessscore ?: Constants.Commons.DASH_STRING,
                indNumber                : scaMap.indnumber ?: Constants.Commons.DASH_STRING,
                appTypeAndNum            : scaMap.apptypeandnum ?: Constants.Commons.DASH_STRING,
                compoundingFlag          : scaMap.compoundingflag ?: Constants.Commons.DASH_STRING,
                medErrorsPt              : generateMedErrorPts(scaMap.mederrorspt),
                patientAge               : getPatientAge(scaMap, df),
                submitter                : scaMap.submitter ?: Constants.Commons.DASH_STRING,
                isValidationStateAchieved: dispositionOfCase?.validatedConfirmed,
                isReviewed               : dispositionOfCase?.reviewCompleted,
                lastUpdated              : scaMap.lastupdated.toString(),
                caseNarrative            : scaMap.casenarrative ?: Constants.Commons.DASH_STRING,
                conMeds                  : scaMap.conmeds ?: Constants.Commons.DASH_STRING,
                ptList                   : getAttr(cacheService.getRptFieldIndexCache('ccAePt'), scaMap.attributes) ?: Constants.Commons.DASH_STRING,
                caseId                   : scaMap.caseid,
                assignedTo               : getAssignedToName(scaMap),
                isCaseSeriesGenerated    : ExecutedConfiguration.get(scaMap.executedalertconfiguration as Long).pvrCaseSeriesId ? true : false,
                comments                 : comment,
                isAttachment             : isAttachment,
                globalId                 : scaMap.globalidentity as Long,
                badge                    : scaMap.badge,
                malfunction              : scaMap.malfunction,
                comboFlag                : scaMap.comboflag,
                productList              : getProductList(scaMap),
                indication               : scaMap.indication ?: Constants.Commons.DASH_STRING,
                eventOutcome             : scaMap.eventoutcome ?: Constants.Commons.DASH_STRING,
                causeOfDeath             : scaMap.causeofdeath ?: Constants.Commons.DASH_STRING,
                seriousUnlistedRelated   : scaMap.seriousunlistedrelated ?: Constants.Commons.DASH_STRING,
                patientMedHist           : isExport ? getPatientMedHist(scaMap.patientmedhist) : scaMap.patientmedhist ?: Constants.Commons.DASH_STRING,
                patientHistDrugs         : scaMap.patienthistdrugs ?: Constants.Commons.DASH_STRING,
                batchLotNo               : scaMap.batchlotno ?: Constants.Commons.DASH_STRING,
                timeToOnset              : scaMap.timetoonset?.toString() ?: Constants.Commons.DASH_STRING,
                caseClassification       : scaMap.caseclassification ?: Constants.Commons.DASH_STRING,
                initialFu                : scaMap.initialfu ?: Constants.Commons.DASH_STRING,
                protocolNo               : scaMap.protocolno ?: Constants.Commons.DASH_STRING,
                isSusar                  : scaMap.issusar ?: Constants.Commons.DASH_STRING,
                therapyDates             : scaMap.therapydates ?: Constants.Commons.DASH_STRING,
                doseDetails              : scaMap.dosedetails ?: Constants.Commons.DASH_STRING,
                preAnda                  : scaMap.preanda ?: Constants.Commons.DASH_STRING,
                justification            : scaMap.justification,
                dispPerformedBy          : scaMap.dispperformedby,
                dispLastChange           : getDispLastChange(scaMap, sdf, timeZone),
                changes                  : scaMap.changes ?: Constants.Commons.DASH_STRING,
                primSuspProd             : scaMap.primsuspprod ?: Constants.Commons.DASH_STRING,
                primSuspPai              : scaMap.primsusppai ?: Constants.Commons.DASH_STRING,
                paiAll                   : scaMap.paiall ?: Constants.Commons.DASH_STRING,
                allPt                    : scaMap.allpt ?: Constants.Commons.DASH_STRING,
                allPtList                : getAttr(cacheService.getRptFieldIndexCache('masterPrefTermAll'), scaMap.attributes) ?: Constants.Commons.DASH_STRING,
                genericName              : scaMap.genericname ?: Constants.Commons.DASH_STRING,
                caseCreationDate         : getCaseCreationDate(scaMap),
                dateOfBirth              : scaMap.dateofbirth ? DateUtil.simpleDateReformat(scaMap.dateofbirth as String, DateUtil.DEFAULT_DATE_FORMAT, DateUtil.DEFAULT_DATE_FORMAT) : Constants.Commons.DASH_STRING,
                eventOnsetDate           : getEventOnsetDate(scaMap),
                pregnancy                : scaMap.pregnancy ?: Constants.Commons.DASH_STRING,
                medicallyConfirmed       : scaMap.medicallyconfirmed ?: Constants.Commons.DASH_STRING,
                allPTsOutcome            : scaMap.allptsoutcome ?: Constants.Commons.DASH_STRING,
                region                   : scaMap.region ?: Constants.Commons.DASH_STRING,
                isUndoEnabled            : isUndoable ? 'true' : 'false',
                crossReferenceInd        : scaMap.crossreferenceind ?: Constants.Commons.DASH_STRING,
                coPackagedProduct        : scaMap.copackagedproduct ?: Constants.Commons.DASH_STRING,
                riskCategory             : scaMap.riskcategory ?: Constants.Commons.DASH_STRING,
                reporterQualification    : scaMap.reporterqualification ?: Constants.Commons.DASH_STRING,
                actualCaseNumber         : scaMap.actualcasenumber,
                actualProductName        : scaMap.actualproductname,
                actualPt                 : scaMap.actualpt,
                isDefaultState           : isDefaultState(scaMap)
        ]

        signalCaseData.signalsAndTopics = isExport ? getSignalName(validatedSignal) : validatedSignal
        return signalCaseData
    }

    /**
     * Helper function to get case init receipt date while creating
     * map for single case alert details screen
     * @param scaMap
     * @return case init reciept date
     */
    private getCaseInitReceiptDate(scaMap) {
        return (scaMap.caseinitreceiptdate != Constants.BlindingStatus.REDACTED && scaMap.caseinitreceiptdate != Constants.BlindingStatus.BLINDED) ?
                (scaMap.caseinitreceiptdate ? (ExecutedConfiguration.get(scaMap.executedalertconfiguration as Long).selectedDatasource == Constants.DataSource.JADER ?
                        convertDate(scaMap.casecreationdate) : convertDate(scaMap.caseinitreceiptdate)) : '') : scaMap.caseinitreceiptdate
    }

    /**
     * Helper function to get susp prod while creating
     * map for single case alert details screen
     * @param scaMap
     * @return susop prod string
     */
    private getSuspProd(scaMap) {
        return scaMap.SUSPPROD ?
                (ExecutedConfiguration.get(scaMap.EXECUTEDALERTCONFIGURATION as Long).selectedDatasource == Constants.DataSource.PVA ?
                        scaMap.SUSPPROD?.replaceAll('\r\n', ',') : scaMap.SUSPPROD) : Constants.Commons.DASH_STRING
    }

    /**
     * Helper function to get locked date while creating
     * map for single case alert details screen
     * @param scaMap
     * @return locked date
     */
    private getLockedDate(scaMap) {
        return scaMap.lockeddate != Constants.BlindingStatus.REDACTED && scaMap.lockeddate != Constants.BlindingStatus.BLINDED ?
                (scaMap.lockeddate ? convertDate(scaMap.lockeddate as String) : Constants.Commons.DASH_STRING) : scaMap.lockeddate
    }

    /**
     * Helper function to get due in while creating
     * map for single case alert details screen
     * @param scaMap
     * @return due in
     */
    private getDueIn(scaMap) {
        return scaMap.duedate != null && !Disposition.get(scaMap.disposition as Long).reviewCompleted ? dueIn(scaMap.duedate as String) : Constants.Commons.DASH_STRING
    }

    /**
     * Helper function to get priority while creating
     * map for single case alert details screen
     * @param isExport
     * @param scaMap
     * @return priority
     */
    private getPriority(isExport, scaMap) {
        return isExport ? priorityDisplayName(scaMap.priority as Long) : getPriorityMap(scaMap.priority as Long)
    }

    /**
     * Helper function to get assigned to user while creating
     * map for single case alert details screen
     * @param isExport
     * @param scaMap
     * @return assigned to user
     */
    private getAssignedToUser(isExport, scaMap) {
        return isExport ? getAssignedToName(scaMap) : assignedToMap(scaMap)
    }

    /**
     * Helper function to get patient age while creating
     * map for single case alert details screen
     * @param scaMap
     * @param df
     * @return patient age
     */
    private getPatientAge(scaMap, df) {
        return (scaMap.patientage && scaMap.patientage != Constants.BlindingStatus.REDACTED && scaMap.patientage != Constants.BlindingStatus.BLINDED && scaMap.patientage.isDouble()) ?
                (df.format(scaMap.patientage.toBigDecimal()) + " " + Constants.Commons.AGE_STANDARD_UNIT) : scaMap.patientage ?: Constants.Commons.DASH_STRING
    }

    /**
     * Helper function to get product list while creating
     * map for single case alert details screen
     * @param scaMap
     * @return Product List
     */
    private getProductList(scaMap) {
        return getAttr(cacheService.getRptFieldIndexCache('masterSuspProdList'), scaMap.attributes) ?
                getAttr(cacheService.getRptFieldIndexCache('masterSuspProdList'), scaMap.attributes).replace("\n", ",").replace("\r", "") : Constants.Commons.DASH_STRING
    }

    /**
     * Helper function to get dipsositon last change date while creating
     * map for single case alert details screen
     * @param scaMap
     * @param sdf
     * @param timeZone
     * @return disoisition last change date
     */
    private getDispLastChange(scaMap, sdf, timeZone) {
        return scaMap.displastchange ? DateUtil.toDateStringWithTimeInAmFormat(sdf.parse(scaMap.displastchange as String), timeZone) : Constants.Commons.DASH_STRING
    }

    /**
     * Helper function to case creation date while creation
     * map for single case alert details screen
     * @param scaMap
     * @return case creation date
     */
    private getCaseCreationDate(scaMap) {
        return (scaMap.casecreationdate != Constants.BlindingStatus.REDACTED && scaMap.casecreationdate != Constants.BlindingStatus.BLINDED) ?
                (scaMap.casecreationdate ? convertDate(scaMap.casecreationdate) : Constants.Commons.DASH_STRING) : scaMap.casecreationdate ?: Constants.Commons.DASH_STRING
    }

    /**
     * Heloer function to get event onset date while creating
     * map for single case alert details screen
     * @param scaMap
     * @return event onset date
     */
    private getEventOnsetDate(scaMap) {
        return scaMap.eventonsetdate != Constants.BlindingStatus.REDACTED && scaMap.eventonsetdate != Constants.BlindingStatus.BLINDED ?
                (scaMap.eventonsetdate ? convertDate(scaMap.eventonsetdate) : Constants.Commons.DASH_STRING) : scaMap.eventonsetdate ?: Constants.Commons.DASH_STRING
    }

    /**
     * Converts a date string in the format 'day-MONTH-year' to 'day-Month-year'
     * where the month first letter is capitalized.
     *
     * @param date the date string to convert
     * @return the formatted date string
     */
    String convertDate(String date){

        String[] parts = date.split('-')
        String day =  parts.size() > 2 ? parts[0] : parts[0].toUpperCase().capitalize()
        String month = parts.size() > 1 ? parts[1]?.toLowerCase().capitalize() : null // capitalize the month
        String year = parts.size() > 2 ? parts[2] : null

        String formattedDate = "${day}"+ (month?"-${month}":"") + (year?"-${year}":"")
        formattedDate
    }
}
