package com.rxlogix.signal

import com.rxlogix.BaseSingleAlert
import com.rxlogix.Constants
import com.rxlogix.config.Configuration
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.DbUtil
import grails.gorm.dirty.checking.DirtyCheck
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import java.text.DecimalFormat
import static com.rxlogix.util.DateUtil.toDateString

@ToString()
@EqualsAndHashCode(callSuper = true)
@DirtyCheck
@CollectionSnapshotAudit
class SingleOnDemandAlert extends BaseSingleAlert implements AlertUtil, GroovyInterceptable {
    static auditable = [ignoreEvents: ["onSave"],auditableProperties:['assignedTo','assignedToGroup','disposition','priority','dueDate','justification','undoJustification']]

    def cacheService

    //Configurations
    Configuration alertConfiguration
    ExecutedConfiguration executedAlertConfiguration

    String malfunction
    String comboFlag
    GlobalCase globalIdentity

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
    String genericName
    Date caseCreationDate
    String dateOfBirth
    String eventOnsetDate
    String pregnancy
    String medicallyConfirmed
    String allPTsOutcome
    String crossReferenceInd
    String coPackagedProduct

    String jsonField
    String undoJustification
    Map<String, Object> customAuditProperties

    static transients = ['attributesMap','undoJustification','customAuditProperties']

    static hasMany = [pvsAlertTag: PvsAlertTag]

    static mapping = {
        table("SINGLE_ON_DEMAND_ALERT")
        id generator: 'sequence', params: [sequence: 'single_on_demand_sequence']
        def superMapping = BaseSingleAlert.mapping.clone()
        superMapping.delegate = delegate
        superMapping.call()
        executedAlertConfiguration(column: 'exec_config_id')
        executedAlertConfiguration lazy: false
        pvsAlertTag joinTable: [name: "SINGLE_DEMAND_ALERT_TAGS", column: "PVS_ALERT_TAG_ID", key: "SINGLE_ALERT_ID"]
        ptList joinTable: [name: "SINGLE_ALERT_OD_PT", key: "SINGLE_ALERT_OD_ID", column: "PT", sqlType: DbUtil.longStringType]
        conComitList joinTable: [name: "SINGLE_ALERT_OD_CON_COMIT", key: "SINGLE_ALERT_OD_ID", column: "CON_COMIT", sqlType: DbUtil.longStringType]
        suspectProductList joinTable: [name: "SINGLE_ALERT_OD_SUSP_PROD", key: "SINGLE_ALERT_OD_ID", column: "PRODUCT_NAME", sqlType: DbUtil.longStringType]
        medErrorPtList joinTable: [name: "SINGLE_ALERT_OD_MED_ERR", key: "SINGLE_ALERT_OD_ID", column: "MED_ERROR", sqlType: DbUtil.longStringType]
        indicationList joinTable: [name: "SINGLE_DEMAND_INDICATION_LIST", key: "SINGLE_ALERT_ID", column: "SCA_INDICATION", sqlType: "varchar(500)"]
        causeOfDeathList joinTable: [name: "SINGLE_DEMAND_CAUSE_OF_DEATH", key: "SINGLE_ALERT_ID", column: "SCA_CAUSE_OF_DEATH", sqlType: "varchar(1500)"]
        patientMedHistList joinTable: [name: "SINGLE_DEMAND_PAT_MED_HIST", key: "SINGLE_ALERT_ID", column: "SCA_PAT_MED_HIST", sqlType: "varchar(3000)"]
        patientHistDrugsList joinTable: [name: "SINGLE_DEMAND_PAT_HIST_DRUGS", key: "SINGLE_ALERT_ID", column: "SCA_PAT_HIST_DRUGS", sqlType: "varchar(500)"]
        batchLotNoList joinTable: [name: "SINGLE_DEMAND_BATCH_LOT_NO", key: "SINGLE_ALERT_ID", column: "SCA_BATCH_LOT_NO", sqlType: "varchar(500)"]
        caseClassificationList joinTable: [name: "SINGLE_DEMAND_CASE_CLASSIFI", key: "SINGLE_ALERT_ID", column: "SCA_CASE_CLASSIFICATION", sqlType: "varchar(3000)"]
        therapyDatesList joinTable: [name: "SINGLE_DEMAND_THERAPY_DATES", key: "SINGLE_ALERT_ID", column: "SCA_THERAPY_DATES", sqlType: "varchar(3000)"]
        doseDetailsList joinTable: [name: "SINGLE_DEMAND_DOSE_DETAILS", key: "SINGLE_ALERT_ID", column: "SCA_DOSE_DETAILS", sqlType: "varchar(15000)"]
        primSuspProdList joinTable: [name: "SINGLE_DEMAND_PRIM_SUSP", key: "SINGLE_ALERT_ID", column: "SCA_PRIM_SUSP", sqlType: "varchar(15000)"]
        primSuspPaiList joinTable: [name: "SINGLE_DEMAND_PRIM_PAI", key: "SINGLE_ALERT_ID", column: "SCA_PRIM_PAI", sqlType: "varchar(15000)"]
        paiAllList joinTable: [name: "SINGLE_DEMAND_ALL_PAI", key: "SINGLE_ALERT_ID", column: "SCA_ALL_PAI", sqlType: "varchar(15000)"]
        allPtList joinTable: [name: "SINGLE_DEMAND_ALL_PT", key: "SINGLE_ALERT_ID", column: "SCA_ALL_PT", sqlType: "varchar(15000)"]
        genericNameList joinTable: [name: "SIG_DMD_ALRT_GENERIC_NAME", key: "SINGLE_ALERT_ID", column: "GENERIC_NAME", sqlType: "varchar(15000)"]
        allPTsOutcomeList joinTable: [name: "SIG_DMD_ALRT_ALLPT_OUT_COME", key: "SINGLE_ALERT_ID", column: "ALLPTS_OUTCOME", sqlType: "varchar(15000)"]
        crossReferenceIndList joinTable: [name: "SIG_DMD_ALRT_CROSS_REFERENCE_IND", key: "SINGLE_ALERT_ID", column: "CROSS_REFERENCE_IND", sqlType: "varchar(15000)"]
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
        genericName type: 'text',sqlType: 'clob'
        allPTsOutcome type: 'text',sqlType: 'clob'
        crossReferenceInd type: 'text',sqlType: 'clob'
        jsonField sqlType: DbUtil.longStringType
        autowire true
    }

    static constraints = {
        alertConfiguration nullable: true
        executedAlertConfiguration nullable: true
        globalIdentity nullable: true
        ptList nullable: true
        suspectProductList nullable: true
        conComitList nullable: true
        medErrorPtList nullable: true
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
        primSuspProdList nullable: true
        primSuspPaiList nullable: true
        paiAllList nullable: true
        allPtList nullable: true
        allPTsOutcomeList nullable : true
        crossReferenceIndList nullable : true
        genericNameList nullable : true
        jsonField nullable : true
        dateOfBirth nullable: true
        pregnancy nullable: true
        eventOnsetDate nullable: true
        medicallyConfirmed nullable: true
        crossReferenceInd nullable: true
        coPackagedProduct nullable: true
    }

    /**
     * This method composes the alert. It takes optional argument isForExport and timeZone.
     * It no timezone value is passed in the argument then timezone taken will be UTC.
     * @param isForExport
     * @param timeZone
     * @return
     */
    Map composeAlert(String timeZone = Constants.UTC, Boolean isExport = false) {
        DecimalFormat df = new DecimalFormat('#.##')

        Map signalCaseData = [
                alertConfigId        : this.alertConfigurationId,
                id                   : this.id,
                alertName            : this.name,
                caseNumber           : isExport ? getExportCaseNumber(this.caseNumber, this.followUpNumber) : this.caseNumber,
                detectedDate         : toDateString(this.detectedDate, timeZone),
                productName          : this.productName,
                productId            : this.getAttr(cacheService.getRptFieldIndexCache('cpiProdIdResolved')) as String,
                pt                   : this.pt,
                productFamily        : this.productFamily,
                flagged              : this.flagged,
                caseReportType       : this.caseReportType,
                caseInitReceiptDate  : isExport ? this.caseInitReceiptDate ? DateUtil.toDateString(this.caseInitReceiptDate, Constants.UTC) : '' : this.caseInitReceiptDate,
                reportersHcpFlag     : this.generateReportersHcpFlagValue(),
                masterPrefTermAll    : this.masterPrefTermAll,
                assessOutcome        : this.getAttr(cacheService.getRptFieldIndexCache('assessOutcome')) ?: Constants.Commons.BLANK_STRING,
                assessListedness     : this.getAssessListedness(isExport),
                assessSeriousness    : this.getAttr(cacheService.getRptFieldIndexCache('assessSeriousness')),
                serious              : this.serious ?: Constants.Commons.BLANK_STRING,
                primaryEvent         : this.getAttr(cacheService.getRptFieldIndexCache('masterPrimEvtPrefTerm')),
                outcome              : (this.outcome && this.outcome != 'undefined') ? this.outcome : Constants.Commons.BLANK_STRING,
                listedness           : (this.listedness && this.listedness != 'undefined') ? this.listedness : Constants.Commons.BLANK_STRING,
                country              : this.country ?: Constants.Commons.BLANK_STRING,
                suspProd             : this.suspProd ?(this.executedAlertConfiguration.selectedDatasource == Constants.DataSource.PVA ? this.suspProd?.replaceAll('\r\n', ','): this.suspProd) :  Constants.Commons.BLANK_STRING,
                conComit             : this.conComit ?: Constants.Commons.BLANK_STRING,
                gender               : this.gender ?: Constants.Commons.BLANK_STRING,
                age                  : this.age ?: Constants.Commons.BLANK_STRING,
                rechallenge          : this.rechallenge ?: Constants.Commons.BLANK_STRING,
                lockedDate           : this.lockedDate ? DateUtil.toDateString(this.lockedDate, Constants.UTC) : Constants.Commons.BLANK_STRING,
                death                : this.death ?: Constants.Commons.BLANK_STRING,
                caseType             : this.caseType ?: Constants.Commons.BLANK_STRING,
                completenessScore    : this.completenessScore ?: Constants.Commons.BLANK_STRING,
                indNumber            : this.indNumber ?: Constants.Commons.BLANK_STRING,
                appTypeAndNum        : this.appTypeAndNum ?: Constants.Commons.BLANK_STRING,
                compoundingFlag      : this.compoundingFlag ?: Constants.Commons.BLANK_STRING,
                medErrorsPt          : generateMedErrorPts(),
                patientAge           : this.patientAge ? df.format(this.patientAge) + " " + Constants.Commons.AGE_STANDARD_UNIT : Constants.Commons.BLANK_STRING,
                submitter            : this.submitter ?: Constants.Commons.BLANK_STRING,
                caseVersion          : this.caseVersion,
                followUpNumber       : this.followUpNumber ?: Constants.Commons.UNDEFINED_NUM,
                followUpExists       : this.followUpExists,
                execConfigId         : this.executedAlertConfigurationId,
                lastUpdated          : this.lastUpdated.toString(),
                caseNarrative        : this.caseNarrative ?: Constants.Commons.BLANK_STRING,
                conMeds              : this.conMeds ?: Constants.Commons.BLANK_STRING,
                ptList               : this.getAttr(cacheService.getRptFieldIndexCache('ccAePt')) ?: Constants.Commons.BLANK_STRING,
                caseId               : this.getAttr(cacheService.getRptFieldIndexCache('masterCaseId')),
                isCaseSeriesGenerated: this.executedAlertConfiguration.pvrCaseSeriesId ? true : false,
                badge                : this.badge,
                malfunction          : this.malfunction,
                comboFlag            : this.comboFlag,
                productList          : this.getAttr(cacheService.getRptFieldIndexCache('masterSuspProdList')) ? this.getAttr(cacheService.getRptFieldIndexCache('masterSuspProdList')).replace("\n", ",").replace("\r", "")
                        : Constants.Commons.BLANK_STRING,
                indication               : this.indication ?: Constants.Commons.BLANK_STRING,
                eventOutcome             : this.eventOutcome ?: Constants.Commons.BLANK_STRING,
                causeOfDeath             : this.causeOfDeath ?: Constants.Commons.BLANK_STRING,
                seriousUnlistedRelated   : this.seriousUnlistedRelated ?: Constants.Commons.BLANK_STRING,
                patientMedHist           : isExport?getPatientMedHist(this.patientMedHist):this.patientMedHist ?: Constants.Commons.BLANK_STRING,
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
                globalId                 : this.globalIdentityId,
                primSuspProd             : this.primSuspProd ?: Constants.Commons.BLANK_STRING,
                primSuspPai              : this.primSuspPai ?: Constants.Commons.BLANK_STRING,
                paiAll                   : this.paiAll ?: Constants.Commons.BLANK_STRING,
                allPtList                : this.getAttr(cacheService.getRptFieldIndexCache('masterPrefTermAll')) ?: Constants.Commons.BLANK_STRING,
                allPt                    : this.allPt ?: Constants.Commons.BLANK_STRING,
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
        signalCaseData
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
            this.medErrorsPt.split('\r\n').each {
                if (it && it.length() > 2) {
                    medErrorPtList.add(it.substring(3, it.length()).trim())
                }
            }
            return medErrorPtList.join(",")
        }
        return Constants.Commons.BLANK_STRING
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
