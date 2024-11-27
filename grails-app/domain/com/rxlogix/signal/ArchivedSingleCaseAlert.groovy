package com.rxlogix.signal

import com.rxlogix.BaseSingleAlert
import com.rxlogix.Constants
import com.rxlogix.config.*
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.DbUtil
import grails.gorm.dirty.checking.DirtyCheck
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import groovy.transform.EqualsAndHashCode
import org.joda.time.DateTime
import org.joda.time.Duration

import java.text.DecimalFormat
import static com.rxlogix.util.DateUtil.toDateString
import static com.rxlogix.util.DateUtil.toStandardDateString

@EqualsAndHashCode(callSuper = true)
@DirtyCheck
@CollectionSnapshotAudit
class ArchivedSingleCaseAlert extends BaseSingleAlert implements AlertUtil, GroovyInterceptable {

    static auditable = [ignoreEvents: ["onSave"],auditableProperties:['assignedTo','assignedToGroup','disposition','priority','justification','undoJustification']]
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
    boolean isDispChanged = false

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
    String jsonField
    String undoJustification
    Boolean studyBlindingStatus
    Boolean caseBlindingStatus
    String reportedProductName
    Map<String, Object> customAuditProperties


    static attachmentable = true

    static hasMany = [actions: Action, validatedSignals: ValidatedSignal, ptList: String, pvsAlertTag : PvsAlertTag]

    static transients = ['attributesMap','undoJustification','customAuditProperties']

    static mapping = {
        table("ARCHIVED_SINGLE_CASE_ALERT")
        def superMapping = BaseSingleAlert.mapping.clone()
        superMapping.delegate = delegate
        superMapping.call()
        executedAlertConfiguration(column: 'exec_config_id')
        executedAlertConfiguration lazy: false
        validatedSignals joinTable: [name: "VALIDATED_ARCHIVED_SCA", column: "VALIDATED_SIGNAL_ID", key: "ARCHIVED_SCA_ID"]
        ptList joinTable: [name: "ARCHIVED_SCA_PT", key: "ARCHIVED_SCA_ID", column: "ARCHIVED_SCA_PT", sqlType: DbUtil.longStringType]
        conComitList joinTable: [name: "ARCHIVED_SCA_CON_COMIT", key: "ARCHIVED_SCA_ID", column: "ALERT_CON_COMIT", sqlType: DbUtil.longStringType]
        suspectProductList joinTable: [name: "ARCHIVED_SCA_SUSP_PROD", key: "ARCHIVED_SCA_ID", column: "SCA_PRODUCT_NAME", sqlType: DbUtil.longStringType]
        medErrorPtList joinTable: [name: "ARCHIVED_SCA_MED_ERR_PT_LIST", key: "ARCHIVED_SCA_ID", column: "SCA_MED_ERROR", sqlType: DbUtil.longStringType]
        actions joinTable: [name: "ARCHIVED_SCA_ACTIONS", column: "ACTION_ID", key: "ARCHIVED_SCA_ID"]
        pvsAlertTag joinTable: [name: "ARCHIVED_SCA_TAGS", column: "PVS_ALERT_TAG_ID", key: "SINGLE_ALERT_ID"]
        indicationList joinTable: [name: "AR_SIN_ALERT_INDICATION_LIST", key: "ARCHIVED_SCA_ID", column: "SCA_INDICATION", sqlType: "varchar(500)"]
        causeOfDeathList joinTable: [name: "AR_SIN_ALERT_CAUSE_OF_DEATH", key: "ARCHIVED_SCA_ID", column: "SCA_CAUSE_OF_DEATH", sqlType: "varchar(1500)"]
        patientMedHistList joinTable: [name: "AR_SIN_ALERT_PAT_MED_HIST", key: "ARCHIVED_SCA_ID", column: "SCA_PAT_MED_HIST", sqlType: "varchar(3000)"]
        patientHistDrugsList joinTable: [name: "AR_SIN_ALERT_PAT_HIST_DRUGS", key: "ARCHIVED_SCA_ID", column: "SCA_PAT_HIST_DRUGS", sqlType: "varchar(500)"]
        batchLotNoList joinTable: [name: "AR_SIN_ALERT_BATCH_LOT_NO", key: "ARCHIVED_SCA_ID", column: "SCA_BATCH_LOT_NO", sqlType: "varchar(500)"]
        caseClassificationList joinTable: [name: "AR_SIN_ALERT_CASE_CLASSIFI", key: "ARCHIVED_SCA_ID", column: "SCA_CASE_CLASSIFICATION", sqlType: "varchar(3000)"]
        therapyDatesList joinTable: [name: "AR_SIN_ALERT_THERAPY_DATES", key: "ARCHIVED_SCA_ID", column: "SCA_THERAPY_DATES", sqlType: "varchar(3000)"]
        doseDetailsList joinTable: [name: "AR_SIN_ALERT_DOSE_DETAILS", key: "ARCHIVED_SCA_ID", column: "SCA_DOSE_DETAILS", sqlType: "varchar(15000)"]
        primSuspProdList joinTable: [name: "AR_SIN_ALERT_PRIM_SUSP", key: "ARCHIVED_SCA_ID", column: "SCA_PRIM_SUSP", sqlType: "varchar(15000)"]
        primSuspPaiList joinTable: [name: "AR_SIN_ALERT_PRIM_PAI", key: "ARCHIVED_SCA_ID", column: "SCA_PRIM_PAI", sqlType: "varchar(15000)"]
        paiAllList joinTable: [name: "AR_SIN_ALERT_ALL_PAI", key: "ARCHIVED_SCA_ID", column: "SCA_ALL_PAI", sqlType: "varchar(15000)"]
        allPtList joinTable: [name: "AR_SIN_ALERT_ALL_PT", key: "ARCHIVED_SCA_ID", column: "SCA_ALL_PT", sqlType: "varchar(15000)"]
        genericNameList joinTable: [name: "AR_SINGLE_ALERT_GENERIC_NAME", key: "SINGLE_ALERT_ID", column: "GENERIC_NAME", sqlType: "varchar(15000)"]
        allPTsOutcomeList joinTable: [name: "AR_SINGLE_ALERT_ALLPT_OUT_COME", key: "SINGLE_ALERT_ID", column: "ALLPTS_OUTCOME", sqlType: "varchar(15000)"]
        crossReferenceIndList joinTable: [name: "ARCHIVED_SINGLE_ALERT_CROSS_REFERENCE_IND", key: "SINGLE_ALERT_ID", column: "CROSS_REFERENCE_IND", sqlType: "varchar(15000)"]
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
        justification column: "JUSTIFICATION", length: 9000
        changes type: 'text', sqlType: 'clob'
        genericName type: 'text',sqlType: 'clob'
        allPTsOutcome type: 'text',sqlType: 'clob'
        crossReferenceInd type: 'text',sqlType: 'clob'
        region type: 'text', sqlType: 'varchar(256)'
        jsonField sqlType: DbUtil.longStringType
    }

    static constraints = {
        globalIdentity nullable: true
        dueDate nullable: true
        alertConfiguration nullable: true
        executedAlertConfiguration nullable: true
        actionCount nullable: true
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
        jsonField nullable: true
        studyBlindingStatus nullable: true
        caseBlindingStatus nullable: true
        reportedProductName nullable: true
        coPackagedProduct nullable:true
    }

    /**
     * This method composes the alert. It takes optional argument isForExport and timeZone.
     * It no timezone value is passed in the argument then timezone taken will be UTC.
     * @param isForExport
     * @param timeZone
     * @return
     */
    Map composeAlert(String timeZone = Constants.UTC, List validatedSignal = [], Boolean isExport = false, String comment = null,
                     Boolean isAttachment = false,String justification = null, Boolean isUndoable = false) {
        DecimalFormat df = new DecimalFormat('#.##')

        def signalCaseData = [
                alertConfigId            : this.alertConfigurationId,
                id                       : this.id,
                alertName                : this.name,
                caseNumber               : isExport ? getExportCaseNumber(this.caseNumber, this.followUpNumber) : this.caseNumber,
                detectedDate             : DateUtil.toDateString(this.detectedDate, timeZone),
                dueDate                  : isExport ? this.dueIn() + "" : DateUtil.toDateString(this.dueDate, timeZone),
                productName              : this.productName,
                productId                : this.getAttr(cacheService.getRptFieldIndexCache('cpiProdIdResolved')) as String,
                pt                       : this.pt,
                productFamily            : this.productFamily,
                flagged                  : this.flagged,
                caseReportType           : this.caseReportType,
                caseInitReceiptDate      : this.caseInitReceiptDate ? (this.executedAlertConfiguration.selectedDatasource == Constants.DataSource.JADER ? toStandardDateString(this.caseInitReceiptDate, Constants.UTC) : toDateString(this.caseInitReceiptDate, Constants.UTC)) : '',
                reportersHcpFlag         : this.generateReportersHcpFlagValue(),
                masterPrefTermAll        : this.masterPrefTermAll,
                assessOutcome            : this.getAttr(cacheService.getRptFieldIndexCache('assessOutcome')) ?: Constants.Commons.BLANK_STRING,
                assessListedness         : this.getAssessListedness(isExport),
                assessSeriousness        : this.getAttr(cacheService.getRptFieldIndexCache('assessSeriousness')),
                serious                  : this.serious ?: Constants.Commons.BLANK_STRING,
                primaryEvent             : this.getAttr(cacheService.getRptFieldIndexCache('masterPrimEvtPrefTerm')),
                outcome                  : (this.outcome && this.outcome != 'undefined') ? this.outcome : Constants.Commons.BLANK_STRING,
                listedness               : (this.listedness && this.listedness != 'undefined') ? this.listedness : Constants.Commons.BLANK_STRING,
                country                  : this.country ?: Constants.Commons.BLANK_STRING,
                suspProd                 : this.suspProd ?(this.executedAlertConfiguration.selectedDatasource == Constants.DataSource.PVA ? this.suspProd?.replaceAll('\r\n', ','): this.suspProd) :  Constants.Commons.BLANK_STRING,
                conComit                 : this.conComit ?: Constants.Commons.BLANK_STRING,
                gender                   : this.gender ?: Constants.Commons.BLANK_STRING,
                age                      : this.age ?: Constants.Commons.BLANK_STRING,
                rechallenge              : this.rechallenge ?: Constants.Commons.BLANK_STRING,
                lockedDate               : this.lockedDate ? this.lockedDate as String: Constants.Commons.BLANK_STRING,
                death                    : this.death ?: Constants.Commons.BLANK_STRING,
                caseVersion              : this.caseVersion,
                dueIn                    : this.dueDate != null && !this.disposition.reviewCompleted? this.dueIn() :Constants.Commons.DASH_STRING,
                followUpNumber           : this.followUpNumber ?: Constants.Commons.UNDEFINED_NUM,
                followUpExists           : this.followUpExists,
                execConfigId             : this.executedAlertConfigurationId,
                disposition              : this.getDispositionById(this.dispositionId)?.displayName,
                currentDisposition       : this.getDispositionById(this.dispositionId)?.displayName,
                dispositionCloseStatus   : this.getDispositionById(this.dispositionId)?.closed,
                currentDispositionId     : this.dispositionId,
                priority                 : isExport ? this.priorityDisplayName(this.priorityId) : this.getPriorityMap(this.priorityId),
                assignedToUser           : isExport ? this.getAssignedToName() : this.assignedToMap(),
                actionCount              : this.actionCount,
                caseType                 : this.caseType ?: Constants.Commons.BLANK_STRING,
                completenessScore        : this.completenessScore ?this.completenessScore.toString(): Constants.Commons.BLANK_STRING,
                indNumber                : this.indNumber ?: Constants.Commons.BLANK_STRING,
                appTypeAndNum            : this.appTypeAndNum ?: Constants.Commons.BLANK_STRING,
                compoundingFlag          : this.compoundingFlag ?: Constants.Commons.BLANK_STRING,
                medErrorsPt              : this.medErrorsPt ?: Constants.Commons.BLANK_STRING,
                patientAge               : this.patientAge ? df.format(this.patientAge) + " " + Constants.Commons.AGE_STANDARD_UNIT : Constants.Commons.BLANK_STRING,
                submitter                : this.submitter ?: Constants.Commons.BLANK_STRING,
                isValidationStateAchieved: this.getDispositionById(this.dispositionId)?.validatedConfirmed,
                isReviewed               : this.getDispositionById(this.dispositionId)?.reviewCompleted,
                lastUpdated              : this.lastUpdated.toString(),
                caseNarrative            : this.caseNarrative ?: Constants.Commons.BLANK_STRING,
                conMeds                  : this.conMeds ?: Constants.Commons.BLANK_STRING,
                ptList                   : this.getAttr(cacheService.getRptFieldIndexCache('ccAePt')) ?: Constants.Commons.BLANK_STRING,
                caseId                   : this.getAttr(cacheService.getRptFieldIndexCache('masterCaseId')),
                assignedTo               : this.getAssignedToName(),
                isCaseSeriesGenerated    : this.executedAlertConfiguration.pvrCaseSeriesId ? true : false,
                comments                 : comment,
                isAttachment             : isAttachment,
                globalId                 : this.globalIdentityId,
                badge                    : this.badge,
                malfunction              : this.malfunction,
                comboFlag                : this.comboFlag,
                productList              : this.getAttr(cacheService.getRptFieldIndexCache('masterSuspProdList')) ? this.getAttr(cacheService.getRptFieldIndexCache('masterSuspProdList')).replace("\n", ",").replace("\r", "")
                        : Constants.Commons.BLANK_STRING,
                indication               : this.indication ?: Constants.Commons.BLANK_STRING,
                eventOutcome             : this.eventOutcome ?: Constants.Commons.BLANK_STRING,
                causeOfDeath             : this.causeOfDeath ?: Constants.Commons.BLANK_STRING,
                seriousUnlistedRelated   : this.seriousUnlistedRelated ?: Constants.Commons.BLANK_STRING,
                patientMedHist           : this.patientMedHist ?: Constants.Commons.BLANK_STRING,
                patientHistDrugs         : this.patientHistDrugs ?: Constants.Commons.BLANK_STRING,
                batchLotNo               : this.batchLotNo ?: Constants.Commons.BLANK_STRING,
                timeToOnset              : (this.timeToOnset || this.timeToOnset == 0) ?(isExport? this.timeToOnset as String : this.timeToOnset): Constants.Commons.BLANK_STRING,
                caseClassification       : this.caseClassification ?: Constants.Commons.BLANK_STRING,
                initialFu                : this.initialFu ?: Constants.Commons.BLANK_STRING,
                protocolNo               : this.protocolNo ?: Constants.Commons.BLANK_STRING,
                isSusar                  : this.isSusar ?: Constants.Commons.BLANK_STRING,
                therapyDates             : this.therapyDates ?: Constants.Commons.BLANK_STRING,
                doseDetails              : this.doseDetails ?: Constants.Commons.BLANK_STRING,
                preAnda                  : this.preAnda ?: Constants.Commons.BLANK_STRING,
                justification            : this.justification,
                dispPerformedBy          : this.dispPerformedBy,
                dispLastChange           : this.dispLastChange?DateUtil.toDateStringWithTimeInAmFormat(this.dispLastChange, timeZone): Constants.Commons.BLANK_STRING,
                changes                  : this.changes?: Constants.Commons.BLANK_STRING,
                primSuspProd             : this.primSuspProd ?: Constants.Commons.BLANK_STRING,
                primSuspPai              : this.primSuspPai ?: Constants.Commons.BLANK_STRING,
                paiAll                   : this.paiAll ?: Constants.Commons.BLANK_STRING,
                allPt                    : this.allPt ?: Constants.Commons.BLANK_STRING,
                allPtList                : this.getAttr(cacheService.getRptFieldIndexCache('masterPrefTermAll')) ?: Constants.Commons.BLANK_STRING,
                genericName              : this.genericName?: Constants.Commons.BLANK_STRING,
                caseCreationDate         : this.caseCreationDate? DateUtil.toDateString(this.caseCreationDate, Constants.UTC) : Constants.Commons.BLANK_STRING,
                dateOfBirth              : this.dateOfBirth? this.dateOfBirth : Constants.Commons.BLANK_STRING,
                eventOnsetDate           : this.eventOnsetDate? this.eventOnsetDate : Constants.Commons.BLANK_STRING,
                pregnancy                : this.pregnancy?: Constants.Commons.BLANK_STRING,
                medicallyConfirmed       : this.medicallyConfirmed?: Constants.Commons.BLANK_STRING,
                allPTsOutcome            : this.allPTsOutcome?: Constants.Commons.BLANK_STRING,
                crossReferenceInd        : this.crossReferenceInd?: Constants.Commons.BLANK_STRING,
                coPackagedProduct         :this.coPackagedProduct ?: Constants.Commons.DASH_STRING
        ]

        signalCaseData.signalsAndTopics = isExport ? this.getSignalName(validatedSignal) : validatedSignal
        signalCaseData
    }

    def dueIn() {
        def theDueDate = new DateTime(dueDate)
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

    String getAssessListedness(Boolean isExport) {
        if (!isExport) {
            return this.getAttr(cacheService.getRptFieldIndexCache('assessListedness')) ?: Constants.Commons.BLANK_STRING
        } else {
            return (this.getAttr(cacheService.getRptFieldIndexCache('assessListedness')) ?: this.getAttr(cacheService.getRptFieldIndexCache('assessOutcome')) ?: Constants.Commons.BLANK_STRING)
        }
    }
    @Override
    String toString(){
       super.toString()
    }

    def getInstanceIdentifierForAuditLog() {
        return "${this.executedAlertConfiguration.getInstanceIdentifierForAuditLog()}: (${this.caseNumber}(${this?.followUpNumber ?: 0}))"
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
