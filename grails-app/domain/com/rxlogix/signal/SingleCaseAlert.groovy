package com.rxlogix.signal

import com.rxlogix.BaseSingleAlert
import com.rxlogix.Constants
import com.rxlogix.config.Action
import com.rxlogix.config.Configuration
import com.rxlogix.config.Disposition
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.MedicalConcepts
import com.rxlogix.config.Priority
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.DbUtil
import grails.gorm.dirty.checking.DirtyCheck
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.h2.schema.Constant
import org.joda.time.DateTime
import org.joda.time.Duration

import java.text.DecimalFormat

import static com.rxlogix.util.DateUtil.toStandardDateString
import static com.rxlogix.util.DateUtil.toDateString


@ToString()
@EqualsAndHashCode(callSuper = true)
@DirtyCheck
@CollectionSnapshotAudit
class SingleCaseAlert extends BaseSingleAlert implements AlertUtil, GroovyInterceptable {

    static auditable = [ignoreEvents: ["onSave"],auditableProperties:['assignedTo','assignedToGroup','disposition','priority','dueDate','justification','dispLastChange','dispPerformedBy','undoJustification']]
    def cacheService
    //Common Single Case Parameters.
    Date dueDate

    //Configurations
    Configuration alertConfiguration
    ExecutedConfiguration executedAlertConfiguration

    //Review related parameters.
    Priority priority
    Disposition disposition
    User assignedTo
    Group assignedToGroup

    //For Advanced Filter
    List<String> ptList
    List<String> suspectProductList
    List<String> conComitList
    List<String> medErrorPtList
    List<String> indicationList
    List<String> causeOfDeathList
    List<String> patientMedHistList
    List<String> patientHistDrugsList
    List<String> batchLotNoList
    List<String> caseClassificationList
    List<String> therapyDatesList
    List<String> doseDetailsList
    List<String> primSuspProdList
    List<String> primSuspPaiList
    List<String> paiAllList
    List<String> allPtList
    List<String> genericNameList
    List<String> allPTsOutcomeList
    List<String> crossReferenceIndList

    //case series drilldown
    String aggExecutionId
    String aggAlertId
    String aggCountType
    Boolean isCaseSeries = false
    String coPackagedProduct
    Integer actionCount = 0

    List<Action> action = []

    GlobalCase globalIdentity
    String supersededFlag
    Date initialDueDate
    Disposition initialDisposition
    String malfunction
    String comboFlag

    String indication
    String eventOutcome
    String causeOfDeath
    String seriousUnlistedRelated
    String patientMedHist
    String patientHistDrugs
    String batchLotNo
    Integer timeToOnset
    String caseClassification
    String initialFu
    String protocolNo
    String isSusar
    String therapyDates
    String doseDetails
    String preAnda
    String jsonField
    String justification
    String dispPerformedBy
    Date dispLastChange
    String changes
    Date previousDueDate
    String genericName
    Date caseCreationDate
    String dateOfBirth
    String eventOnsetDate
    String pregnancy
    String medicallyConfirmed
    String allPTsOutcome
    String crossReferenceInd
    String region
    String riskCategory
    String reporterQualification
    boolean isDispChanged = false
    static attachmentable = true
    String undoJustification
    Boolean studyBlindingStatus
    Boolean caseBlindingStatus
    String reportedProductName
    Map<String, Object> customAuditProperties

    Boolean skipAudit = false

    static hasMany = [actions: Action, signalConcepts: MedicalConcepts, topicConcepts: MedicalConcepts, validatedSignals: ValidatedSignal, topics: Topic, alertTags: AlertTag, ptList: String, tags: SingleGlobalTag  , pvsAlertTag : PvsAlertTag]
    static transients = ['attributesMap', 'action','undoJustification','skipAudit','customAuditProperties']

    static mapping = {
        table("SINGLE_CASE_ALERT")
        id generator: 'sequence', params: [sequence: 'sca_sequence']
        def superMapping = BaseSingleAlert.mapping.clone()
        superMapping.delegate = delegate
        superMapping.call()
        executedAlertConfiguration(column: 'exec_config_id')
        executedAlertConfiguration lazy: false
        signalConcepts joinTable: [name: "SINGLE_SIGNAL_CONCEPTS", column: "MEDICAL_CONCEPTS_ID", key: "SINGLE_CASE_ALERT_ID"]
        topicConcepts joinTable: [name: "SINGLE_TOPIC_CONCEPTS", column: "MEDICAL_CONCEPTS_ID", key: "SINGLE_CASE_ALERT_ID"]
        validatedSignals joinTable: [name: "VALIDATED_SINGLE_ALERTS", column: "VALIDATED_SIGNAL_ID", key: "SINGLE_ALERT_ID"]
        topics joinTable: [name: "TOPIC_SINGLE_ALERTS", column: "TOPIC_ID", key: "SINGLE_ALERT_ID"]
        alertTags joinTable: [name: "SINGLE_ALERT_TAGS", column: "ALERT_TAG_ID", key: "SINGLE_ALERT_ID"]
        ptList joinTable: [name: "SINGLE_ALERT_PT", key: "SINGLE_ALERT_ID", column: "SCA_PT", sqlType: DbUtil.longStringType]
        conComitList joinTable: [name: "SINGLE_ALERT_CON_COMIT", key: "SINGLE_ALERT_ID", column: "ALERT_CON_COMIT", sqlType: DbUtil.longStringType]
        suspectProductList joinTable: [name: "SINGLE_ALERT_SUSP_PROD", key: "SINGLE_ALERT_ID", column: "SCA_PRODUCT_NAME", sqlType: DbUtil.longStringType]
        medErrorPtList joinTable: [name: "SINGLE_ALERT_MED_ERR_PT_LIST", key: "SINGLE_ALERT_ID", column: "SCA_MED_ERROR", sqlType: DbUtil.longStringType]
        tags joinTable: [name: "SINGLE_GLOBAL_TAG_MAPPING", column: "SINGLE_GLOBAL_ID", key: "SINGLE_ALERT_ID"]
        actions joinTable: [name: "SINGLE_ALERT_ACTIONS", column: "ACTION_ID", key: "SINGLE_CASE_ALERT_ID"]
        pvsAlertTag joinTable: [name: "SINGLE_CASE_ALERT_TAGS", column: "PVS_ALERT_TAG_ID", key: "SINGLE_ALERT_ID"]
        executedAlertConfiguration index: 'idx_single_alert_exconfig'
        indicationList joinTable: [name: "SINGLE_ALERT_INDICATION_LIST", key: "SINGLE_ALERT_ID", column: "SCA_INDICATION", sqlType: "varchar(500)"]
        causeOfDeathList joinTable: [name: "SINGLE_ALERT_CAUSE_OF_DEATH", key: "SINGLE_ALERT_ID", column: "SCA_CAUSE_OF_DEATH", sqlType: "varchar(1500)"]
        patientMedHistList joinTable: [name: "SINGLE_ALERT_PAT_MED_HIST", key: "SINGLE_ALERT_ID", column: "SCA_PAT_MED_HIST", sqlType: "varchar(3000)"]
        patientHistDrugsList joinTable: [name: "SINGLE_ALERT_PAT_HIST_DRUGS", key: "SINGLE_ALERT_ID", column: "SCA_PAT_HIST_DRUGS", sqlType: "varchar(500)"]
        batchLotNoList joinTable: [name: "SINGLE_ALERT_BATCH_LOT_NO", key: "SINGLE_ALERT_ID", column: "SCA_BATCH_LOT_NO", sqlType: "varchar(500)"]
        caseClassificationList joinTable: [name: "SINGLE_ALERT_CASE_CLASSIFI", key: "SINGLE_ALERT_ID", column: "SCA_CASE_CLASSIFICATION", sqlType: "varchar(3000)"]
        therapyDatesList joinTable: [name: "SINGLE_ALERT_THERAPY_DATES", key: "SINGLE_ALERT_ID", column: "SCA_THERAPY_DATES", sqlType: "varchar(3000)"]
        doseDetailsList joinTable: [name: "SINGLE_ALERT_DOSE_DETAILS", key: "SINGLE_ALERT_ID", column: "SCA_DOSE_DETAILS", sqlType: "varchar(15000)"]
        primSuspProdList joinTable: [name: "SINGLE_ALERT_PRIM_SUSP", key: "SINGLE_ALERT_ID", column: "SCA_PRIM_SUSP", sqlType: "varchar(15000)"]
        primSuspPaiList joinTable: [name: "SINGLE_ALERT_PRIM_PAI", key: "SINGLE_ALERT_ID", column: "SCA_PRIM_PAI", sqlType: "varchar(15000)"]
        paiAllList joinTable: [name: "SINGLE_ALERT_ALL_PAI", key: "SINGLE_ALERT_ID", column: "SCA_ALL_PAI", sqlType: "varchar(15000)"]
        allPtList joinTable: [name: "SINGLE_ALERT_ALL_PT", key: "SINGLE_ALERT_ID", column: "SCA_ALL_PT", sqlType: "varchar(15000)"]
        genericNameList joinTable: [name: "SINGLE_ALERT_GENERIC_NAME", key: "SINGLE_ALERT_ID", column: "GENERIC_NAME", sqlType: "varchar(15000)"]
        allPTsOutcomeList joinTable: [name: "SINGLE_ALERT_ALLPT_OUT_COME", key: "SINGLE_ALERT_ID", column: "ALLPTS_OUTCOME", sqlType: "varchar(15000)"]
        crossReferenceIndList joinTable: [name: "SINGLE_ALERT_CROSS_REFERENCE_IND", key: "SINGLE_ALERT_ID", column: "CROSS_REFERENCE_IND", sqlType: "varchar(15000)"]
        causeOfDeath type: 'text', sqlType: 'clob'
        patientMedHist type: 'text', sqlType: 'clob'
        patientHistDrugs type: 'text', sqlType: 'clob'
        batchLotNo type: 'text', sqlType: 'clob'
        caseClassification type: 'text', sqlType: 'clob'
        therapyDates type: 'text', sqlType: 'clob'
        doseDetails type: 'text', sqlType: 'clob'
        indication type: 'text', sqlType: 'varchar(16000)'
        eventOutcome type: 'text', sqlType: 'varchar(2000)'
        protocolNo type: 'text', sqlType: 'varchar(500)'
        jsonField sqlType: DbUtil.longStringType
        justification column: "JUSTIFICATION", length: 9000
        changes type: 'text', sqlType: 'clob'
        genericName type: 'text',sqlType: 'clob'
        allPTsOutcome type: 'text',sqlType: 'clob'
        crossReferenceInd type: 'text',sqlType: 'clob'
        region type: 'text', sqlType: 'varchar(256)'
        riskCategory type: 'text', sqlType: 'varchar(256)'
        reporterQualification type: 'text', sqlType: 'varchar(256)'
        dynamicUpdate true //Added for PVS-64945
    }

    static constraints = {
        globalIdentity nullable: true
        dueDate nullable: true
        alertConfiguration nullable: true
        executedAlertConfiguration nullable: true
        actionCount nullable: true
        alertTags nullable: true
        aggExecutionId nullable: true
        aggAlertId nullable: true
        aggCountType nullable: true
        isCaseSeries nullable: true
        ptList nullable: true
        suspectProductList nullable: true
        conComitList nullable: true
        medErrorPtList nullable: true
        assignedTo nullable: true, validator: { value, obj ->
            def result = true
            if (!obj.assignedTo) {
                result = obj.assignedToGroup ? true : 'assignedTo.nullable'
            }
            return result
        }
        assignedToGroup(nullable: true)
        tags nullable: true
        supersededFlag nullable: true
        initialDueDate nullable:true
        initialDisposition nullable: true
        indicationList nullable: true
        causeOfDeathList nullable: true
        patientMedHistList nullable: true
        patientHistDrugsList nullable: true
        batchLotNoList nullable: true
        caseClassificationList nullable: true
        therapyDatesList nullable: true
        doseDetailsList nullable: true
        indication nullable: true, maxSize: 16000
        eventOutcome nullable: true, maxSize: 2000
        causeOfDeath nullable: true
        seriousUnlistedRelated nullable: true
        patientMedHist nullable: true
        patientHistDrugs nullable: true
        batchLotNo nullable: true
        timeToOnset nullable: true
        caseClassification nullable: true
        initialFu nullable: true
        protocolNo nullable: true, maxSize: 500
        isSusar nullable: true
        therapyDates nullable: true
        doseDetails nullable: true
        preAnda nullable: true
        justification nullable: true, maxSize: 9000
        dispPerformedBy nullable: true
        dispLastChange nullable: true
        isDispChanged nullable: true
        changes nullable: true
        previousDueDate nullable: true
        primSuspProdList nullable: true
        primSuspPaiList nullable: true
        paiAllList nullable: true
        allPtList nullable: true
        jsonField nullable: true
        allPTsOutcomeList nullable : true
        crossReferenceIndList nullable : true
        genericNameList nullable : true
        genericName nullable : true
        caseCreationDate nullable : true
        dateOfBirth nullable : true
        eventOnsetDate nullable : true
        pregnancy nullable : true
        medicallyConfirmed nullable : true
        allPTsOutcome nullable : true
        crossReferenceInd nullable : true
        region nullable: true, maxSize: 256
        riskCategory nullable: true, maxSize: 256
        reporterQualification nullable: true, maxSize: 256
        studyBlindingStatus nullable: true
        caseBlindingStatus nullable: true
        reportedProductName nullable: true
        coPackagedProduct nullable: true
    }

    /**
     * This method composes the alert. It takes optional argument isForExport and timeZone.
     * It no timezone value is passed in the argument then timezone taken will be UTC.
     * @param isForExport
     * @param timeZone
     * @return
     */
    Map composeAlert(String timeZone = Constants.UTC, List validatedSignal = [], Boolean isExport = false, String comment = null,
                     Boolean isAttachment = false,String justification = null, Boolean isUndoable=false) {
        DecimalFormat df = new DecimalFormat('#.##')
        Disposition dispositionOfCase = this.getDispositionById(this.dispositionId)

        def signalCaseData = [
                alertConfigId            : this.alertConfigurationId,
                id                       : this.id,
                alertName                : this.name,
                caseNumber               : isExport ? getExportCaseNumber(this.caseNumber, this.followUpNumber) : this.caseNumber,
                detectedDate             : toDateString(this.detectedDate, timeZone),
                dueDate                  : isExport ? (!this.disposition.reviewCompleted ? this.dueIn() + "" : "") : toDateString(this.dueDate, timeZone),
                productName              : this.productName,
                productId                : this.getAttr(cacheService.getRptFieldIndexCache('cpiProdIdResolved')) as String,
                pt                       : this.pt,
                productFamily            : this.productFamily,
                flagged                  : this.flagged,
                caseReportType           : this.caseReportType ?: Constants.Commons.DASH_STRING,
                caseInitReceiptDate      : this.caseInitReceiptDate ? (this.executedAlertConfiguration.selectedDatasource == Constants.DataSource.JADER ? toStandardDateString(this.caseInitReceiptDate, Constants.UTC) : toDateString(this.caseInitReceiptDate, Constants.UTC)) : '' ,
                reportersHcpFlag         : this.generateReportersHcpFlagValue(),
                masterPrefTermAll        : this.masterPrefTermAll,
                assessOutcome            : this.getAttr(cacheService.getRptFieldIndexCache('assessOutcome')) ?: Constants.Commons.DASH_STRING,
                assessListedness         : this.getAssessListedness(isExport),
                assessSeriousness        : this.getAttr(cacheService.getRptFieldIndexCache('assessSeriousness')),
                serious                  : this.serious ?: Constants.Commons.DASH_STRING,
                primaryEvent             : this.getAttr(cacheService.getRptFieldIndexCache('masterPrimEvtPrefTerm')),
                outcome                  : (this.outcome && this.outcome != 'undefined') ? this.outcome : Constants.Commons.DASH_STRING,
                listedness               : (this.listedness && this.listedness != 'undefined') ? this.listedness : Constants.Commons.DASH_STRING,
                country                  : this.country ?: Constants.Commons.DASH_STRING,
                suspProd                 : this.suspProd ?(this.executedAlertConfiguration.selectedDatasource == Constants.DataSource.PVA ? this.suspProd?.replaceAll('\r\n', ','): this.suspProd) :  Constants.Commons.DASH_STRING,
                conComit                 : this.conComit ?: Constants.Commons.DASH_STRING,
                gender                   : this.gender ?:  Constants.Commons.DASH_STRING,
                age                      : this.age ?: Constants.Commons.DASH_STRING,
                rechallenge              : this.rechallenge ?: Constants.Commons.DASH_STRING,
                lockedDate               : this.lockedDate ? DateUtil.toDateString(this.lockedDate, Constants.UTC) : Constants.Commons.DASH_STRING,
                death                    : this.death ?: Constants.Commons.DASH_STRING,
                caseVersion              : this.caseVersion,
                dueIn                    : this.dueDate != null && !this.disposition.reviewCompleted ? this.dueIn() : Constants.Commons.DASH_STRING,
                followUpNumber           : this.followUpNumber ?: Constants.Commons.UNDEFINED_NUM,
                followUpExists           : this.followUpExists,
                execConfigId             : this.executedAlertConfigurationId,
                disposition              : dispositionOfCase?.displayName,
                currentDisposition       : dispositionOfCase?.displayName,
                currentDispositionId     : this.dispositionId,
                dispositionCloseStatus   : dispositionOfCase?.closed,
                priority                 : isExport ? this.priorityDisplayName(this.priorityId) : this.getPriorityMap(this.priorityId),
                assignedToUser           : isExport ? this.getAssignedToName() : this.assignedToMap(),
                actionCount              : this.actionCount,
                caseType                 : this.caseType ?: Constants.Commons.DASH_STRING,
                completenessScore        : this.completenessScore ? this.completenessScore.toString() : Constants.Commons.DASH_STRING,
                indNumber                : this.indNumber ?: Constants.Commons.DASH_STRING,
                appTypeAndNum            : this.appTypeAndNum ?: Constants.Commons.DASH_STRING,
                compoundingFlag          : this.compoundingFlag ?: Constants.Commons.DASH_STRING,
                medErrorsPt              : generateMedErrorPts(),
                patientAge               : this.patientAge ? df.format(this.patientAge) + " " + Constants.Commons.AGE_STANDARD_UNIT : Constants.Commons.DASH_STRING,
                submitter                : this.submitter ?: Constants.Commons.DASH_STRING,
                isValidationStateAchieved: dispositionOfCase?.validatedConfirmed,
                isReviewed               : dispositionOfCase?.reviewCompleted,
                lastUpdated              : this.lastUpdated.toString(),
                caseNarrative            : this.caseNarrative ?: Constants.Commons.DASH_STRING,
                conMeds                  : this.conMeds ?: Constants.Commons.DASH_STRING,
                ptList                   : this.getAttr(cacheService.getRptFieldIndexCache('ccAePt')) ?: Constants.Commons.DASH_STRING,
                caseId                   : this.caseId,
                assignedTo               : this.getAssignedToName(),
                isCaseSeriesGenerated    : this.executedAlertConfiguration.pvrCaseSeriesId ? true : false,
                comments                 : comment,
                isAttachment             : isAttachment,
                globalId                 : this.globalIdentityId,
                badge                    : this.badge,
                malfunction              : this.malfunction,
                comboFlag                : this.comboFlag,
                productList              : this.getAttr(cacheService.getRptFieldIndexCache('masterSuspProdList')) ? this.getAttr(cacheService.getRptFieldIndexCache('masterSuspProdList')).replace("\n", ",").replace("\r", "")
                        : Constants.Commons.DASH_STRING,
                indication               : this.indication ?:  Constants.Commons.DASH_STRING,
                eventOutcome             : this.eventOutcome ?:  Constants.Commons.DASH_STRING,
                causeOfDeath             : this.causeOfDeath ?: Constants.Commons.DASH_STRING,
                seriousUnlistedRelated   : this.seriousUnlistedRelated ?: Constants.Commons.DASH_STRING,
                patientMedHist           : isExport?getPatientMedHist(this.patientMedHist):this.patientMedHist ?: Constants.Commons.DASH_STRING,
                patientHistDrugs         : this.patientHistDrugs ?: Constants.Commons.DASH_STRING,
                batchLotNo               : this.batchLotNo ?: Constants.Commons.DASH_STRING,
                timeToOnset              : (this.timeToOnset || this.timeToOnset == 0) ?(isExport? this.timeToOnset as String : this.timeToOnset): Constants.Commons.DASH_STRING,
                caseClassification       : this.caseClassification ?: Constants.Commons.DASH_STRING,
                initialFu                : this.initialFu ?: Constants.Commons.DASH_STRING,
                protocolNo               : this.protocolNo ?: Constants.Commons.DASH_STRING,
                isSusar                  : this.isSusar ?: Constants.Commons.DASH_STRING,
                therapyDates             : this.therapyDates ?: Constants.Commons.DASH_STRING,
                doseDetails              : this.doseDetails ?: Constants.Commons.DASH_STRING,
                preAnda                  : this.preAnda ?: Constants.Commons.DASH_STRING,
                justification            : this.justification,
                dispPerformedBy          : this.dispPerformedBy,
                dispLastChange           : this.dispLastChange?DateUtil.toDateStringWithTimeInAmFormat(this.dispLastChange, timeZone): Constants.Commons.DASH_STRING,
                changes                  : this.changes?: Constants.Commons.DASH_STRING,
                primSuspProd             : this.primSuspProd ?: Constants.Commons.DASH_STRING,
                primSuspPai              : this.primSuspPai ?: Constants.Commons.DASH_STRING,
                paiAll                   : this.paiAll ?: Constants.Commons.DASH_STRING,
                allPt                    : this.allPt ?:  Constants.Commons.DASH_STRING,
                allPtList                : this.getAttr(cacheService.getRptFieldIndexCache('masterPrefTermAll')) ?: Constants.Commons.DASH_STRING,
                genericName              : this.genericName?: Constants.Commons.DASH_STRING,
                caseCreationDate         : this.caseCreationDate? DateUtil.toDateString(this.caseCreationDate, Constants.UTC) : Constants.Commons.DASH_STRING,
                dateOfBirth              : this.dateOfBirth? DateUtil.simpleDateReformat(this.dateOfBirth, DateUtil.DEFAULT_DATE_FORMAT,DateUtil.DEFAULT_DATE_FORMAT) : Constants.Commons.DASH_STRING,
                eventOnsetDate           : this.eventOnsetDate? DateUtil.simpleDateReformat(this.eventOnsetDate, DateUtil.DEFAULT_DATE_FORMAT,DateUtil.DEFAULT_DATE_FORMAT) : Constants.Commons.DASH_STRING,
                pregnancy                : this.pregnancy?: Constants.Commons.DASH_STRING,
                medicallyConfirmed       : this.medicallyConfirmed?: Constants.Commons.DASH_STRING,
                allPTsOutcome            : this.allPTsOutcome?: Constants.Commons.DASH_STRING,
                region                   : this.region?:  Constants.Commons.DASH_STRING,
                isUndoEnabled            : isUndoable?'true':'false',
                isDefaultState           : this.isDefaultState(),
                crossReferenceInd        : this.crossReferenceInd ?: Constants.Commons.DASH_STRING,
                isDefaultState           : this.isDefaultState(),
                riskCategory             : this.riskCategory ?: Constants.Commons.DASH_STRING,
                reporterQualification    : this.reporterQualification ?: Constants.Commons.DASH_STRING,
                coPackagedProduct         :this.coPackagedProduct ?: Constants.Commons.DASH_STRING


        ]

        signalCaseData.signalsAndTopics = isExport ? this.getSignalName(validatedSignal) : validatedSignal
        signalCaseData
    }

    def isDefaultState(){
        if(!isDispChanged && getDispositionById(this.dispositionId) == getDefaultDisp() && this.dispPerformedBy == null){
            return 'true'
        }
        return 'false'
    }
    Disposition getDefaultDisp(){
        cacheService.getDefaultDisp(Constants.AlertConfigType.SINGLE_CASE_ALERT)
    }


    def dueIn() {
        def theDueDate = new DateTime(dueDate).withTimeAtStartOfDay()
        def now = DateTime.now().withTimeAtStartOfDay()
        def dur = new Duration(now, theDueDate)
        dur.getStandardDays()
    }


    def propertyMissing(name) {
        if (!attributesMap) {
            attributesMap = populateAttributesMap()
        }
        if (name && attributesMap.containsKey(name)) {
            return attributesMap[name]
        } else {
            null
        }
    }

    def propertyMissing(String name, value) {
        if (!attributesMap) {
            attributesMap = populateAttributesMap()
        }
        attributesMap[name] = value
    }

    String getAssignedToName() {
        this.assignedToId ? getUserByUserId(this.assignedToId).fullName : getGroupByGroupId(this.assignedToGroupId).name
    }


    Map getPriorityMap(Long priorityId) {
        Priority priority = cacheService.getPriorityByValue(priorityId)
        [value: priority?.value, iconClass: priority?.iconClass]
    }

    Disposition getDispositionById(Long dispositionId) {
        cacheService.getDispositionByValue(dispositionId)
    }

    User getUserByUserId(Long userId) {
        cacheService.getUserByUserId(userId)
    }

    Group getGroupByGroupId(Long groupId) {
        cacheService.getGroupByGroupId(groupId)
    }

    Map assignedToMap() {
        this.assignedToId ? getUserByUserId(this.assignedToId).toMap() : getGroupByGroupId(this.assignedToGroupId).toMap()
    }

    String getSignalName(List signals) {
        String signalName = ""
        signalName = signals.collect { it.name }?.join(",")
        return signalName
    }

    String priorityDisplayName(Long priorityId) {
        Priority priority = cacheService.getPriorityByValue(priorityId)
        priority.displayName
    }

    List<String> getProductNameList() {
        String prdName = getNameFieldFromJson(this.alertConfiguration.productSelection)
        if (prdName) {
            prdName.toLowerCase().tokenize(',')
        } else {
            []
        }
    }

    String getProductDictionarySelection() {
        this.executedAlertConfiguration.productDictionarySelection
    }

    String generateMedErrorPts(){
        if(this.medErrorsPt){
            List medErrorPtList = []
            String [] medErrorArray
            this.medErrorsPt.split('\r\n').each {
                if (it && it.length() > 2) {
                    if(it =~ Constants.MEDICATION_ERROR_PT_REGEX){
                        medErrorArray = it.split("\\.")
                        if (medErrorArray.length > 1) {
                            medErrorPtList.add(medErrorArray[1]?.trim())
                        }
                    } else {
                        medErrorPtList.add(it.substring(3, it.length()).trim())
                    }
                }
            }
            return medErrorPtList.join(",")
        }
        return Constants.Commons.DASH_STRING
    }

    String getAssessListedness(Boolean isExport) {
        if (!isExport) {
            return this.getAttr(cacheService.getRptFieldIndexCache('assessListedness')) ?: Constants.Commons.DASH_STRING
        } else {
            return (this.getAttr(cacheService.getRptFieldIndexCache('assessListedness')) ?: this.getAttr(cacheService.getRptFieldIndexCache('assessOutcome')) ?: Constants.Commons.DASH_STRING)
        }
    }

    @Override
    String toString(){
        super.toString()
    }


    def getInstanceIdentifierForAuditLog() {
        return this.executedAlertConfiguration.getInstanceIdentifierForAuditLog() + ": (${this.caseNumber}(${this.followUpNumber ?: 0}))"
    }
    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        if (this.undoJustification != null && this.undoJustification != "") {
            newValues.put("undoJustification", this.undoJustification)
        }
        // custom audit properties added before CRUD operation
        for(Map.Entry customAuditEntry: this.customAuditProperties){
            if (customAuditEntry.getValue() != null && customAuditEntry.getValue() != "") {
                newValues.put(customAuditEntry.getKey(), customAuditEntry.getValue())
                oldValues.put(customAuditEntry.getKey(), "")
            }
        }
        this.customAuditProperties=[:]
        return [newValues: newValues, oldValues: oldValues]
    }

}
