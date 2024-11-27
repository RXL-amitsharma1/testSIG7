package com.rxlogix.signal

import com.rxlogix.BaseAggregateAlert
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
import grails.converters.JSON
import grails.gorm.dirty.checking.DirtyCheck
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import groovy.json.JsonSlurper
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import org.joda.time.DateTime
import org.joda.time.Duration

import java.text.DecimalFormat
import static com.rxlogix.util.DateUtil.toDateString

@ToString()
@EqualsAndHashCode(callSuper = true)
@DirtyCheck
@CollectionSnapshotAudit
class AggregateCaseAlert extends BaseAggregateAlert implements AlertUtil, GroovyInterceptable {

    static auditable = [ignoreEvents: ["onSave"],auditableProperties:['assignedTo','assignedToGroup','disposition','priority','dueDate','justification','dispLastChange','dispPerformedBy','undoJustification']]
    def cacheService
    def dataObjectService

    //Configuration related information
    Configuration alertConfiguration
    ExecutedConfiguration executedAlertConfiguration

    //Work flow related fields
    Priority priority
    Disposition disposition
    User assignedTo
    Group assignedToGroup
    Date detectedDate
    Date dueDate
    Date previousDueDate

    String pregenency

    //PRR values
    Double prrValue
    Double prrLCI
    Double prrUCI
    String prrStr
    String prrStrLCI
    String prrStrUCI
    String prrMh

    //ROR values
    Double rorValue
    Double rorLCI
    Double rorUCI
    String rorStr
    String rorStrLCI
    String rorStrUCI
    String rorMh

    //PEC Importance values
    String pecImpHigh
    String pecImpLow
//DSS properties
    String dssScore
    String dssComments
    String dssConfirmation
    Double pecImpNumHigh
    Double pecImpNumLow
    String rationale
    String proposedDisposition

    String format
    String freqPriority
    String trendType
    String flags
    String requestedBy

    boolean adhocRun = false
    boolean isNew
    boolean isDispChanged = false
    List<Action> action = []

    Integer actionCount = 0

    //Updates as per PVS-2792
    Integer newCount = 0
    Integer cummCount = 0
    Integer newPediatricCount = 0
    Integer cummPediatricCount = 0
    Integer newInteractingCount = 0
    Integer cummInteractingCount = 0

    Integer newGeriatricCount = 0
    Integer cumGeriatricCount = 0
    Integer newNonSerious = 0
    Integer cumNonSerious = 0

    String ebgmStr
    String eb05Str
    String eb95Str

    //Faers Columns
    String eb95AgeFaers
    String eb05AgeFaers
    String ebgmAgeFaers
    String eb05GenderFaers
    String eb95GenderFaers
    String ebgmGenderFaers
    String prrStrFaers
    String prrStrLCIFaers
    String prrStrUCIFaers
    String rorStrFaers
    String rorStrLCIFaers
    String rorStrUCIFaers


    String impEvents
    List<String> aggImpEventList = []
    GlobalProductEvent globalIdentity

    //Integrated Review Fields
    String faersColumns
    String evdasColumns
    String vaersColumns
    String vigibaseColumns
    String jaderColumns
    Map<String, Object> evdasColumnMap
    Map<String, Object> faersColumnMap
    Map<String, Object> vaersColumnMap
    Map<String, Object> vigibaseColumnMap
    Map<String, Object> jaderColumnMap


    Date reviewDate
    Date initialDueDate
    Disposition initialDisposition
    String justification
    String dispPerformedBy
    Date dispLastChange
    //Update for 11059 story
    Integer newCountFreqCalc =0

    String trendFlag
    Integer newProdCount
    Integer cumProdCount
    Double freqPeriod
    Double cumFreqPeriod
    Double reviewedFreqPeriod
    Double reviewedCumFreqPeriod

    Integer prodHierarchyId
    Integer eventHierarchyId
    Long version

    boolean isSafety = true

    Double aValue
    Double bValue
    Double cValue
    Double dValue
    Double eValue
    Double rrValue
    String newCountsJson
    String undoJustification
    Map<String, Object> customAuditProperties


    static attachmentable = true

    static hasMany = [actions: Action, signalConcepts: MedicalConcepts, topicConcepts: MedicalConcepts, validatedSignals: ValidatedSignal, topics: Topic, alertTags: AlertTag , pvsAlertTag : PvsAlertTag, alertCommentHistory: AlertCommentHistory]


    def dataSource_pva

    static constraints = {
        globalIdentity nullable: true
        prrStr nullable: true
        rorStr nullable: true
        format nullable: true, blank: true, maxSize: 8000
        pecImpLow nullable: true
        pecImpHigh nullable: true
        prrLCI nullable: true
        prrUCI nullable: true
        rorLCI nullable: true
        rorUCI nullable: true
        prrStrLCI nullable: true
        prrStrUCI nullable: true
        prrMh nullable: true
        rorStrLCI nullable: true
        rorStrUCI nullable: true
        rorMh nullable: true
        actionCount nullable: true
        isNew nullable: false
        pregenency nullable: true
        alertTags nullable: true
        freqPriority nullable: true
        trendType nullable: true
        flags nullable: true
        requestedBy nullable: true
        assignedTo nullable: true, validator: { value, obj ->
            def result = true
            if (!obj.assignedTo) {
                result = obj.assignedToGroup ? true : 'assignedTo.nullable'
            }
            return result
        }
        assignedToGroup nullable: true
        ebgmStr nullable: true
        eb05Str nullable: true
        eb95Str nullable: true
        impEvents nullable: true
        aggImpEventList nullable: true
        reviewDate nullable: true
        dueDate nullable: true
        previousDueDate nullable: true
        faersColumns nullable: true, blank: true, maxSize: 4000
        evdasColumns nullable: true, blank: true, maxSize: 4000
        eb95AgeFaers nullable: true, blank: true, maxSize: 4000
        eb05AgeFaers nullable: true, blank: true, maxSize: 4000
        ebgmAgeFaers nullable: true, blank: true, maxSize: 4000
        eb05GenderFaers nullable: true, blank: true, maxSize: 4000
        eb95GenderFaers nullable: true, blank: true, maxSize: 4000
        ebgmGenderFaers nullable: true, blank: true, maxSize: 4000
        prrStrFaers nullable: true, blank: true, maxSize: 4000
        prrStrLCIFaers nullable: true, blank: true, maxSize: 4000
        prrStrUCIFaers nullable: true, blank: true, maxSize: 4000
        rorStrFaers nullable: true, blank: true, maxSize: 4000
        rorStrLCIFaers nullable: true, blank: true, maxSize: 4000
        rorStrUCIFaers nullable: true, blank: true, maxSize: 4000
        initialDueDate nullable:true
        initialDisposition nullable: true
        justification nullable: true, maxSize: 9000
        dispPerformedBy nullable: true
        dispLastChange nullable: true
        isDispChanged nullable: true
        dssScore nullable: true
        dssComments nullable: true
        dssConfirmation nullable: true
        pecImpNumHigh nullable: true
        pecImpNumLow nullable: true
        rationale nullable: true
        proposedDisposition nullable: true
        newCountFreqCalc nullable:true
        vaersColumns nullable: true, blank: true, maxSize: 4000
        trendFlag nullable:true
        newProdCount nullable:true
        cumProdCount nullable:true
        freqPeriod nullable:true
        cumFreqPeriod nullable:true
        reviewedFreqPeriod nullable:true
        reviewedCumFreqPeriod nullable:true
        prodHierarchyId nullable: true
        eventHierarchyId nullable: true
        vigibaseColumns nullable: true, blank: true, maxSize: 4000
        jaderColumns nullable: true, blank: true, maxSize: 4000
        aValue nullable: true
        bValue nullable: true
        cValue nullable: true
        dValue nullable: true
        eValue nullable: true
        rrValue nullable: true
        newCountsJson nullable: true
        version nullable: false
    }

    static transients = ['action','evdasColumnMap','faersColumnMap','vaersColumnMap', 'vigibaseColumnMap','jaderColumnMap','undoJustification','customAuditProperties']

    static mapping = {
        table("AGG_ALERT")
        id generator: 'sequence', params: [sequence: 'agg_sequence']
        executedAlertConfiguration(column: 'exec_configuration_id')
        alertConfiguration(column: 'alert_configuration_id')
        prrLCI(column: 'PRR05')
        prrUCI(column: 'PRR95')
        rorLCI(column: 'ROR05')
        rorUCI(column: 'ROR95')
        prrStrLCI(column: 'PRR_STR05')
        prrStrUCI(column: 'PRR_STR95')
        rorStrLCI(column: 'ROR_STR05')
        rorStrUCI(column: 'ROR_STR95')
        prrStrLCIFaers(column: 'PRR_STR05FAERS')
        prrStrUCIFaers(column: 'PRR_STR95FAERS')
        rorStrLCIFaers(column: 'ROR_STR05FAERS')
        rorStrUCIFaers(column: 'ROR_STR95FAERS')
        ebgmSubGroup(column: 'EBGM_SUB_GROUP')
        eb95SubGroup(column: 'EB95_SUB_GROUP')
        eb05SubGroup(column: 'EB05_SUB_GROUP')
        signalConcepts joinTable: [name: "AGG_SIGNAL_CONCEPTS", column: "MEDICAL_CONCEPTS_ID", key: "AGG_ALERT_ID"]
        validatedSignals joinTable: [name: "VALIDATED_AGG_ALERTS", column: "VALIDATED_SIGNAL_ID", key: "AGG_ALERT_ID"]
        topics joinTable: [name: "TOPIC_AGG_ALERTS", column: "TOPIC_ID", key: "AGG_ALERT_ID"]
        topicConcepts joinTable: [name: "AGG_TOPIC_CONCEPTS", column: "MEDICAL_CONCEPTS_ID", key: "AGG_ALERT_ID"]
        alertTags joinTable: [name: "AGG_ALERT_TAGS", column: "ALERT_TAG_ID", key: "AGG_ALERT_ID"]
        prrStr column: 'PRR_STR', sqlType: DbUtil.stringType
        rorStr column: 'ROR_STR', sqlType: DbUtil.stringType
        alertConfiguration lazy: false
        executedAlertConfiguration lazy: false
        ebgmStr type: 'text', sqlType: 'clob'
        eb05Str type: 'text', sqlType: 'clob'
        eb95Str type: 'text', sqlType: 'clob'
        actions joinTable: [name: "AGG_ALERT_ACTIONS", column: "ACTION_ID", key: "AGG_ALERT_ID"]
        aggImpEventList joinTable: [name: "AGG_ALERT_IMP_EVENT_LIST", key: "AGG_ALERT_ID", column: "AGA_IMP_EVENTS"]
        pvsAlertTag joinTable: [name: "AGG_CASE_ALERT_TAGS", column: "PVS_ALERT_TAG_ID", key: "AGG_ALERT_ID"]
        alertCommentHistory joinTable: [name:'ALERT_COMMENT_HISTORY_MAP', column:'COMMENT_HISTORY_ID', key:'AGG_ALERT_ID']
        eb95Age type: 'text', sqlType: 'clob'
        eb05Age type: 'text', sqlType: 'clob'
        ebgmAge type: 'text', sqlType: 'clob'
        eb05Gender type: 'text', sqlType: 'clob'
        eb95Gender type: 'text', sqlType: 'clob'
        ebgmGender type: 'text', sqlType: 'clob'
        executedAlertConfiguration index: 'idx_agg_alert_exconfig'
        justification column: "JUSTIFICATION", length: 9000
        newProdCount column: "PROD_N_PERIOD"
        cumProdCount column: "PROD_N_CUMUL"
        freqPeriod column: "FREQ_PERIOD"
        cumFreqPeriod column: "FREQ_CUMUL"
        reviewedFreqPeriod column: "SAVE_FREQ_PERIOD"
        reviewedCumFreqPeriod column: "SAVE_FREQ_CUMUL"
        dssScore type: 'text', sqlType: 'clob'
        dssComments column: "DSS_COMMENTS", length: 4000
        newCountsJson type: 'text', sqlType: 'clob'
        autowire true
        version column: "version"
    }

    Map toDto(String timeZone = "UTC",
              isSpecialPE = false,
              String trend = Constants.Commons.EVEN,
              smqMap = null,
              Map ptMap = [isIme: "", isDme: "", isEi: "", isSm: ""], List<String> tagNameList = null, List validatedSignal = [],
              String comments = null,
              Boolean isAttachment = false,
              Boolean isExport = false,
              Boolean isUndoable=false, Long commentId = null) {
        DecimalFormat df = new DecimalFormat('#.#####')
        Double chisquare = Double.valueOf(df.format(this.chiSquare))
        DecimalFormat df1 = new DecimalFormat('#.###')
        Double eVal = this.eValue? Double.valueOf(df1.format(this?.eValue)):-1
        Double rrVal = this.rrValue? Double.valueOf(df1.format(this?.rrValue)):-1
        Map aggAlertData = [
                id                          : this.id,
                alertName                   : this.name,
                priority                    : getPriorityMap(this.priorityId),
                exConfigId                  : this.executedAlertConfigurationId,
                isArchived                  : false,
                assignedTo                  : this.assignedToId ? getUserByUserId(this.assignedToId).toMap() : this.assignedToGroupId ? getGroupByGroupId(this.assignedToGroupId).toMap() : [:],
                detectedDate                : toDateString(this.detectedDate, timeZone),
                dueDate                     : toDateString(this.dueDate, timeZone),
                disposition                 : getDispositionById(this.dispositionId)?.displayName,
                proposedDisposition         : this.proposedDisposition,
                dispositionCloseStatus      : this.getDispositionById(this.dispositionId)?.closed,
                currentDispositionId        : this.dispositionId,
                productName                 : this.productName,
                flagged                     : this.flagged,
                newStudyCount               : this.newStudyCount!=-1 ? this.newStudyCount: '-',
                cumStudyCount               : this.cumStudyCount!=-1 ? this.cumStudyCount: '-',
                newSponCount                : this.newSponCount!=-1 ? this.newSponCount: '-',
                cumSponCount                : this.cumSponCount!=-1 ? this.cumSponCount: '-',
                newSeriousCount             : this.newSeriousCount!=-1 ? this.newSeriousCount: '-',
                cumSeriousCount             : this.cumSeriousCount!=-1 ? this.cumSeriousCount: '-',
                newFatalCount               : this.newFatalCount!=-1 ? this.newFatalCount: '-',
                cumFatalCount               : this.cumFatalCount!=-1 ? this.cumFatalCount: '-',
                positiveRechallenge         : this.positiveRechallenge ?: '-',
                positiveDechallenge         : this.positiveDechallenge ?: '-',
                listed                      : this.listed ?: '-',
                related                     : this.related ?: '-',
                pregenency                  : this.pregenency ?: '-',
                prrValue                    : this.prrValue==0 ? '0' : this.prrValue != -1 ? this.prrValue.toString() : '-',
                prrLCI                       : this.prrLCI==0 ? '0' :  this.prrLCI != -1.0 ? this.prrLCI?.toString() : '-',
                prrUCI                       : this.prrUCI==0 ? '0' :  this.prrUCI != -1.0 ? this.prrUCI?.toString() : '-',
                rorValue                    : this.rorValue==0 ? '0' :  this.rorValue != -1.0 ? this.rorValue.toString() : '-',
                rorLCI                       : this.rorLCI==0 ? '0' :  this.rorLCI != -1.0 ? this.rorLCI?.toString() : '-',
                rorUCI                       : this.rorUCI==0 ? '0' :  this.rorUCI!=-1.0 ? this.rorUCI?.toString() : '-',
                eb05                        : this.eb05==0 ? '0' :  this.eb05 !=-1 ? this.eb05: '-' ,
                eb95                        : this.eb95==0 ? '0' :  this.eb95 !=-1 ? this.eb95: '-',
                ebgm                        : this.ebgm==0 ? '0' :  this.ebgm != -1.0 ? this.ebgm : '-',
                highPecImp                  : this.pecImpHigh,
                lowPecImp                   : this.pecImpLow,
                dueIn                       : this.dueDate != null && !this.disposition.reviewCompleted? this.dueIn() :Constants.Commons.DASH_STRING,
                preferredTerm               : this.pt,
                primaryEvent                : this.pt,
                soc                         : this.soc,
                actionCount                 : this.actionCount,
                systemOrganClass            : null,
                execConfigId                : this.executedAlertConfigurationId,
                alertConfigId               : this.alertConfigurationId,
                configId                    : this.alertConfigurationId,
                productId                   : this.productId as String ,
                level                       : getLevel(this.executedAlertConfiguration.productSelection),
                ptCode                      : this.ptCode,
                dataSource                  : this.alertConfiguration.selectedDatasource.toUpperCase(),
                trend                       : trend,
                isSpecialPE                 : isSpecialPE,
                groupBySmq                  : this.alertConfiguration.groupBySmq,
                smqValue                    : getSmqValue(this, smqMap),
                isValidationStateAchieved   : getDispositionById(this.dispositionId)?.validatedConfirmed,
                lastUpdated                 : this.lastUpdated,
                impEvents                   : (isExport && this.impEvents)?replaceImpEventsLabels(this.impEvents).toUpperCase():this.impEvents ? this.impEvents.toUpperCase() : Constants.Commons.BLANK_STRING,
                ime                         : ptMap.isIme,
                dme                         : ptMap.isDme,
                ei                          : ptMap.isEi,
                isSm                        : ptMap.isSm,
                format                      : this.format,
                alertTags                   : tagNameList,
                trendType                   : this.trendType,
                freqPriority                : this.freqPriority,
                asOfDate                    : toDateString(this.periodEndDate),
                executionDate               : toDateString(this.dateCreated),
                isReviewed                  : getDispositionById(this.dispositionId)?.reviewCompleted,
                flags                       : this.flags ?: "",
                eb05Str                     : this.eb05Str ?: '-',
                eb95Str                     : this.eb95Str ?: '-',
                ebgmStr                     : this.ebgmStr ?: '-',
                eb95Age                     : this.eb95Age ?: '-',
                eb05Age                     : this.eb05Age ?: '-',
                ebgmAge                     : this.ebgmAge ?: '-',
                eb05Gender                  : this.eb05Gender ?: '-',
                eb95Gender                  : this.eb95Gender ?: '-',
                ebgmGender                  : this.ebgmGender ?: '-',
                ebgmSubGroup                : this.ebgmSubGroup ?: '-',
                eb05SubGroup                : this.eb05SubGroup ?: '-',
                eb95SubGroup                : this.eb95SubGroup ?: '-',
                rorSubGroup                 : this.rorSubGroup ?: '-',
                rorLciSubGroup              : this.rorLciSubGroup ?: '-',
                rorUciSubGroup              : this.rorUciSubGroup ?: '-',
                prrSubGroup                 : this.prrSubGroup ?: '-',
                prrLciSubGroup              : this.prrLciSubGroup ?: '-',
                prrUciSubGroup              : this.prrUciSubGroup ?: '-',
                chiSquareSubGroup           : this.chiSquareSubGroup ?: '-',
                rorRelSubGroup             : this.rorRelSubGroup ?: '-',
                rorLciRelSubGroup            : this.rorLciRelSubGroup ?: '-',
                rorUciRelSubGroup          : this.rorUciRelSubGroup ?: '-',
                newCount                    : this.newCount!=-1 ? this.newCount: '-',
                cummCount                   : this.cummCount!=-1 ? this.cummCount: '-',
                newPediatricCount           : this.newPediatricCount!=-1 ? this.newPediatricCount: '-',
                cummPediatricCount          : this.cummPediatricCount!=-1 ? this.cummPediatricCount: '-',
                newInteractingCount         : this.newInteractingCount!=-1 ? this.newInteractingCount: '-',
                cummInteractingCount        : this.cummInteractingCount!=-1 ? this.cummInteractingCount: '-',
                comment                     : comments,
                commentId                   : commentId,
                isAttachment                : isAttachment,
                aValue                      : this.aValue!=-1 ? this.aValue: '-',
                bValue                      : this.bValue!=-1 ? this.bValue: '-',
                cValue                      : this.cValue!=-1 ? this.cValue: '-',
                dValue                      : this.dValue!=-1 ? this.dValue: '-',
                eValue                      : eVal!=-1? eVal : '-',
                rrValue                     : rrVal!=-1? rrVal: '-',
                chiSquare                   : !(this.cummCount ==0 && this.newCount ==0) ? chisquare : '0',
                evdasExecConfigId           : this.getEvdasColumnValue("execConfigId") ?: '-',
                newEvLink                   : this.getEvdasColumnValue("newEvLink") ?: '-',
                totalEvLink                 : this.getEvdasColumnValue("totalEvLink") ?: '-',
                newEvEvdas                  : this.getEvdasColumnValue("newEvEvdas") ?: '-',
                totalEvEvdas                : this.getEvdasColumnValue("totalEvEvdas") ?: '-',
                dmeImeEvdas                 : this.getEvdasColumnValue("dmeImeEvdas") ?: '-',
                sdrEvdas                    : this.getEvdasColumnValue("sdrEvdas") ?: '-',
                rorValueEvdas               : this.getEvdasColumnValue("rorValueEvdas") ?: '-',
                newPaedEvdas                : this.getEvdasColumnValue("newPaedEvdas") ?: '-',
                totPaedEvdas                : this.getEvdasColumnValue("totPaedEvdas") ?: '-',
                totalFatalEvdas             : this.getEvdasColumnValue("totalFatalEvdas") ?: '-',
                newFatalEvdas               : this.getEvdasColumnValue("newFatalEvdas") ?: '-',
                sdrPaedEvdas                : this.getEvdasColumnValue("sdrPaedEvdas") ?: '-',
                changesEvdas                : this.getEvdasColumnValue("changesEvdas") ?: '-',
                hlgtEvdas                   : this.getEvdasColumnValue("hlgtEvdas") ?: '-',
                hltEvdas                    : this.getEvdasColumnValue("hltEvdas") ?: '-',
                smqNarrowEvdas              : this.getEvdasColumnValue("smqNarrowEvdas") ?: '-',
                impEventsEvdas              : this.getEvdasColumnValue("impEventsEvdas") ?: '-',
                newEeaEvdas                 : this.getEvdasColumnValue("newEeaEvdas") ?: '-',
                totEeaEvdas                 : this.getEvdasColumnValue("totEeaEvdas") ?: '-',
                newHcpEvdas                 : this.getEvdasColumnValue("newHcpEvdas") ?: '-',
                totHcpEvdas                 : this.getEvdasColumnValue("totHcpEvdas") ?: '-',
                newSeriousEvdas             : this.getEvdasColumnValue("newSeriousEvdas") ?: '-',
                totalSeriousEvdas           : this.getEvdasColumnValue("totalSeriousEvdas") ?: '-',
                newMedErrEvdas              : this.getEvdasColumnValue("newMedErrEvdas") ?: '-',
                totMedErrEvdas              : this.getEvdasColumnValue("totMedErrEvdas") ?: '-',
                newObsEvdas                 : this.getEvdasColumnValue("newObsEvdas") ?: '-',
                totObsEvdas                 : this.getEvdasColumnValue("totObsEvdas") ?: '-',
                newRcEvdas                  : this.getEvdasColumnValue("newRcEvdas") ?: '-',
                totRcEvdas                  : this.getEvdasColumnValue("totRcEvdas") ?: '-',
                newLitEvdas                 : this.getEvdasColumnValue("newLitEvdas") ?: '-',
                allRorEvdas                 : this.getEvdasColumnValue("allRorEvdas") ?: '-',
                totalLitEvdas               : this.getEvdasColumnValue("totalLitEvdas") ?: '-',
                ratioRorPaedVsOthersEvdas   : this.getEvdasColumnValue("ratioRorPaedVsOthersEvdas") ?: '-',
                newGeriaEvdas               : this.getEvdasColumnValue("newGeriaEvdas") ?: '-',
                totGeriaEvdas               : this.getEvdasColumnValue("totGeriaEvdas") ?: '-',
                ratioRorGeriatrVsOthersEvdas: this.getEvdasColumnValue("ratioRorGeriatrVsOthersEvdas") ?: '-',
                sdrGeratrEvdas              : this.getEvdasColumnValue("sdrGeratrEvdas") ?: '-',
                newSpontEvdas               : this.getEvdasColumnValue("newSpontEvdas") ?: '-',
                totSpontEvdas               : this.getEvdasColumnValue("totSpontEvdas") ?: '-',
                totSpontEuropeEvdas         : this.getEvdasColumnValue("totSpontEuropeEvdas") ?: '-',
                totSpontNAmericaEvdas       : this.getEvdasColumnValue("totSpontNAmericaEvdas") ?: '-',
                totSpontJapanEvdas          : this.getEvdasColumnValue("totSpontJapanEvdas") ?: '-',
                totSpontAsiaEvdas           : this.getEvdasColumnValue("totSpontAsiaEvdas") ?: '-',
                totSpontRestEvdas           : this.getEvdasColumnValue("totSpontRestEvdas") ?: '-',
                europeRorEvdas              : this.getEvdasColumnValue("europeRorEvdas") ?: '-',
                northAmericaRorEvdas        : this.getEvdasColumnValue("northAmericaRorEvdas") ?: '-',
                japanRorEvdas               : this.getEvdasColumnValue("japanRorEvdas") ?: '-',
                asiaRorEvdas                : this.getEvdasColumnValue("asiaRorEvdas") ?: '-',
                restRorEvdas                : this.getEvdasColumnValue("restRorEvdas") ?: '-',
                listedEvdas                 : getEvdasListednessValue(this.getEvdasColumnValue("listedEvdas")),
                faersExecConfigId           : this.getFaersColumnValue("executedAlertConfigurationId") ?: '-',
                newCountFaers               : this.getFaersColumnValue("newCountFaers") ?: '-',
                cummCountFaers              : this.getFaersColumnValue("cummCountFaers") ?: '-',
                newSeriousCountFaers        : this.getFaersColumnValue("newSeriousCountFaers") ?: '-',
                cumSeriousCountFaers        : this.getFaersColumnValue("cumSeriousCountFaers") ?: '-',
                eb05Faers                   : this.getFaersColumnValue("eb05Faers") ?: '-',
                eb95Faers                   : this.getFaersColumnValue("eb95Faers") ?: '-',
                newSponCountFaers           : this.getFaersColumnValue("newSponCountFaers") ?: '-',
                cumSponCountFaers           : this.getFaersColumnValue("cumSponCountFaers") ?: '-',
                newStudyCountFaers          : this.getFaersColumnValue("newStudyCountFaers") ?: '-',
                cumStudyCountFaers          : this.getFaersColumnValue("cumStudyCountFaers") ?: '-',
                newInteractingCountFaers    : this.getFaersColumnValue("newInteractingCountFaers") ?: '-',
                cummInteractingCountFaers   : this.getFaersColumnValue("cummInteractingCountFaers") ?: '-',
                newFatalCountFaers          : this.getFaersColumnValue("newFatalCountFaers") ?: '-',
                cumFatalCountFaers          : this.getFaersColumnValue("cumFatalCountFaers") ?: '-',
                newGeriatricCountFaers      : this.getFaersColumnValue("newGeriatricCountFaers") ?: '-',
                cumGeriatricCountFaers      : this.getFaersColumnValue("cumGeriatricCountFaers") ?: '-',
                newNonSeriousFaers          : this.getFaersColumnValue("newNonSeriousFaers") ?: '-',
                cumNonSeriousFaers          : this.getFaersColumnValue("cumNonSeriousFaers") ?: '-',
                ebgmFaers                   : this.getFaersColumnValue("ebgmFaers") ?: '-',
                prrValueFaers               : this.getFaersColumnValue("prrValueFaers") ?: '-',
                rorValueFaers               : this.getFaersColumnValue("rorValueFaers") ?: '-',
                chiSquareFaers              : this.getFaersColumnValue("chiSquareFaers") ?: '-',
                listedFaers                 : this.getFaersColumnValue("listedFaers") ?: '-',
                trendTypeFaers              : this.getFaersColumnValue("trendTypeFaers") ?: '-',
                freqPriorityFaers           : this.getFaersColumnValue("freqPriorityFaers") ?: '-',
                impEventsFaers              : this.getFaersColumnValue("impEventsFaers") ?: '-',
                positiveRechallengeFaers    : this.getFaersColumnValue("positiveRechallengeFaers") ?: '-',
                positiveDechallengeFaers    : this.getFaersColumnValue("positiveDechallengeFaers") ?: '-',
                prrLCIFaers                  : this.getFaersColumnValue("prrLCIFaers") ?: '-',
                prrUCIFaers                  : this.getFaersColumnValue("prrUCIFaers") ?: '-',
                rorUCIFaers                  : this.getFaersColumnValue("rorUCIFaers") ?: '-',
                rorLCIFaers                  : this.getFaersColumnValue("rorLCIFaers") ?: '-',
                newPediatricCountFaers      : this.getFaersColumnValue("newPediatricCountFaers") ?: '-',
                cummPediatricCountFaers     : this.getFaersColumnValue("cummPediatricCountFaers") ?: '-',
                relatedFaers                : this.getFaersColumnValue("relatedFaers") ?: '-',
                pregenencyFaers             : this.getFaersColumnValue("pregenencyFaers") ?: '-',
                aValueFaers                 : this.getFaersColumnValue("aValueFaers") ?: '-',
                bValueFaers                 : this.getFaersColumnValue("bValueFaers") ?: '-',
                cValueFaers                 : this.getFaersColumnValue("cValueFaers") ?: '-',
                dValueFaers                 : this.getFaersColumnValue("dValueFaers") ?: '-',
                eValueFaers                 : this.getFaersColumnValue("eValueFaers") ?: '-',
                rrValueFaers                : this.getFaersColumnValue("rrValueFaers") ?: '-',
                eb95AgeFaers                : this.eb95AgeFaers ?: '-',
                eb05AgeFaers                : this.eb05AgeFaers ?: '-',
                ebgmAgeFaers                : this.ebgmAgeFaers ?: '-',
                eb05GenderFaers             : this.eb05GenderFaers ?: '-',
                eb95GenderFaers             : this.eb95GenderFaers ?: '-',
                ebgmGenderFaers             : this.ebgmGenderFaers ?: '-',
                isFaersOnly                 : this.newCount == -1,
                newGeriatricCount           : this.newGeriatricCount!=-1 ? this.newGeriatricCount: '-',
                cumGeriatricCount           : this.cumGeriatricCount!=-1 ? this.cumGeriatricCount: '-',
                newNonSerious               : this.newNonSerious!=-1 ? this.newNonSerious: '-',
                cumNonSerious               : this.cumNonSerious!=-1 ? this.cumNonSerious: '-',
                fullSoc                     : dataObjectService.getAbbreviationMap(this.soc),
                productSelection            : this.executedAlertConfiguration?.productSelection,
                justification               : this.justification,
                dispPerformedBy             : this.dispPerformedBy?.equalsIgnoreCase(Constants.Commons.SYSTEM)?Constants.Commons.SYSTEM:this.dispPerformedBy,
                dispLastChange              : this.dispLastChange? DateUtil.toDateStringWithTimeInAmFormat(this.dispLastChange, timeZone): Constants.Commons.BLANK_STRING,
                newCountVaers               : this.getVaersColumnValue("newCountVaers") ?: '-',
                cummCountVaers              : this.getVaersColumnValue("cummCountVaers") ?: '-',
                newSeriousCountVaers        : this.getVaersColumnValue("newSeriousCountVaers") ?: '-',
                cumSeriousCountVaers        : this.getVaersColumnValue("cumSeriousCountVaers") ?: '-',
                eb05Vaers                   : this.getVaersColumnValue("eb05Vaers") ?: '-',
                eb95Vaers                   : this.getVaersColumnValue("eb95Vaers") ?: '-',
                newFatalCountVaers          : this.getVaersColumnValue("newFatalCountVaers") ?: '-',
                cumFatalCountVaers          : this.getVaersColumnValue("cumFatalCountVaers") ?: '-',
                newGeriatricCountVaers      : this.getVaersColumnValue("newGeriatricCountVaers") ?: '-',
                cumGeriatricCountVaers      : this.getVaersColumnValue("cumGeriatricCountVaers") ?: '-',
                newPediatricCountVaers      : this.getVaersColumnValue("newPediatricCountVaers") ?: '-',
                cummPediatricCountVaers     : this.getVaersColumnValue("cummPediatricCountVaers") ?: '-',
                vaersExecConfigId           : this.getVaersColumnValue("executedAlertConfigurationId") ?: '-',
                ebgmVaers                   : this.getVaersColumnValue("ebgmVaers") ?: '-',
                prrValueVaers               : this.getVaersColumnValue("prrValueVaers") ?: '-',
                rorValueVaers               : this.getVaersColumnValue("rorValueVaers") ?: '-',
                chiSquareVaers              : this.getVaersColumnValue("chiSquareVaers") ?: '-',
                prrLCIVaers                  : this.getVaersColumnValue("prrLCIVaers") ?: '-',
                prrUCIVaers                  : this.getVaersColumnValue("prrUCIVaers") ?: '-',
                rorUCIVaers                  : this.getVaersColumnValue("rorUCIVaers") ?: '-',
                rorLCIVaers                  : this.getVaersColumnValue("rorLCIVaers") ?: '-',
                aValueVaers                 : this.getVaersColumnValue("aValueVaers") ?: '-',
                bValueVaers                 : this.getVaersColumnValue("bValueVaers") ?: '-',
                cValueVaers                 : this.getVaersColumnValue("cValueVaers") ?: '-',
                dValueVaers                 : this.getVaersColumnValue("dValueVaers") ?: '-',
                eValueVaers                 : this.getVaersColumnValue("eValueVaers") ?: '-',
                rrValueVaers                : this.getVaersColumnValue("rrValueVaers") ?: '-',
                trendFlag                   : this.trendFlag?:Constants.Commons.DASH_STRING,
                isSafety                    : this.isSafety,
                newProdCount                : this.newProdCount || this.newProdCount == 0 ? this.newProdCount : Constants.Commons.DASH_STRING,
                cumProdCount                : this.cumProdCount || this.cumProdCount == 0 ? this.cumProdCount : Constants.Commons.DASH_STRING,
                freqPeriod                  : this.freqPeriod || this.freqPeriod == 0 ? this.freqPeriod : Constants.Commons.DASH_STRING,
                cumFreqPeriod               : this.cumFreqPeriod || this.cumFreqPeriod == 0 ? this.cumFreqPeriod : Constants.Commons.DASH_STRING,
                reviewedFreqPeriod          : this.reviewedFreqPeriod || this.reviewedFreqPeriod == 0 ? this.reviewedFreqPeriod : Constants.Commons.DASH_STRING,
                reviewedCumFreqPeriod       : this.reviewedCumFreqPeriod || this.reviewedCumFreqPeriod == 0 ? this.reviewedCumFreqPeriod : Constants.Commons.DASH_STRING,
                pecImpNumLow                : this.pecImpNumLow ? this.pecImpNumLow.toString() : Constants.Commons.DASH_STRING,
                pecImpNumHigh               : this.pecImpNumHigh ? this.pecImpNumHigh.toString() : Constants.Commons.DASH_STRING,
                dssScore                    : this.dssScore?: Constants.Commons.DASH_STRING,
                rationale                   : this.rationale? this.rationale : Constants.Commons.DASH_STRING,
                newCountVigibase               : this.getVigibaseColumnValue("newCountVigibase") ?: '-',
                cummCountVigibase              : this.getVigibaseColumnValue("cummCountVigibase") ?: '-',
                newSeriousCountVigibase        : this.getVigibaseColumnValue("newSeriousCountVigibase") ?: '-',
                cumSeriousCountVigibase        : this.getVigibaseColumnValue("cumSeriousCountVigibase") ?: '-',
                eb05Vigibase                   : this.getVigibaseColumnValue("eb05Vigibase") ?: '-',
                eb95Vigibase                   : this.getVigibaseColumnValue("eb95Vigibase") ?: '-',
                newFatalCountVigibase          : this.getVigibaseColumnValue("newFatalCountVigibase") ?: '-',
                cumFatalCountVigibase          : this.getVigibaseColumnValue("cumFatalCountVigibase") ?: '-',
                newGeriatricCountVigibase      : this.getVigibaseColumnValue("newGeriatricCountVigibase") ?: '-',
                cumGeriatricCountVigibase      : this.getVigibaseColumnValue("cumGeriatricCountVigibase") ?: '-',
                newPediatricCountVigibase      : this.getVigibaseColumnValue("newPediatricCountVigibase") ?: '-',
                cummPediatricCountVigibase     : this.getVigibaseColumnValue("cummPediatricCountVigibase") ?: '-',
                vigibaseExecConfigId           : this.getVigibaseColumnValue("executedAlertConfigurationId") ?: '-',
                ebgmVigibase                   : this.getVigibaseColumnValue("ebgmVigibase") ?: '-',
                prrValueVigibase               : this.getVigibaseColumnValue("prrValueVigibase") ?: '-',
                rorValueVigibase               : this.getVigibaseColumnValue("rorValueVigibase") ?: '-',
                chiSquareVigibase              : this.getVigibaseColumnValue("chiSquareVigibase") ?: '-',
                prrLCIVigibase                  : this.getVigibaseColumnValue("prrLCIVigibase") ?: '-',
                prrUCIVigibase                  : this.getVigibaseColumnValue("prrUCIVigibase") ?: '-',
                rorUCIVigibase                  : this.getVigibaseColumnValue("rorUCIVigibase") ?: '-',
                rorLCIVigibase                  : this.getVigibaseColumnValue("rorLCIVigibase") ?: '-',
                aValueVigibase                 : this.getVigibaseColumnValue("aValueVigibase") ?: '-',
                bValueVigibase                 : this.getVigibaseColumnValue("bValueVigibase") ?: '-',
                cValueVigibase                 : this.getVigibaseColumnValue("cValueVigibase") ?: '-',
                dValueVigibase                 : this.getVigibaseColumnValue("dValueVigibase") ?: '-',
                eValueVigibase                 : this.getVigibaseColumnValue("eValueVigibase") ?: '-',
                rrValueVigibase                : this.getVigibaseColumnValue("rrValueVigibase") ?: '-',
                newCountJader              : this.getJaderColumnValue("newCountJader") ?: '-',
                cumCountJader             : this.getJaderColumnValue("cumCountJader") ?: '-',
                newSeriousCountJader       : this.getJaderColumnValue("newSeriousCountJader") ?: '-',
                cumSeriousCountJader       : this.getJaderColumnValue("cumSeriousCountJader") ?: '-',
                eb05Jader                  : this.getJaderColumnValue("eb05Jader") ?: '-',
                eb95Jader                  : this.getJaderColumnValue("eb95Jader") ?: '-',
                newFatalCountJader         : this.getJaderColumnValue("newFatalCountJader") ?: '-',
                cumFatalCountJader         : this.getJaderColumnValue("cumFatalCountJader") ?: '-',
                newGeriatricCountJader     : this.getJaderColumnValue("newGeriatricCountJader") ?: '-',
                cumGeriatricCountJader     : this.getJaderColumnValue("cumGeriatricCountJader") ?: '-',
                newPediatricCountJader     : this.getJaderColumnValue("newPediatricCountJader") ?: '-',
                cumPediatricCountJader    : this.getJaderColumnValue("cumPediatricCountJader") ?: '-',
                ebgmJader                  : this.getJaderColumnValue("ebgmJader") ?: '-',
                prrValueJader              : this.getJaderColumnValue("prrValueJader") ?: '-',
                rorValueJader              : this.getJaderColumnValue("rorValueJader") ?: '-',
                chiSquareJader             : this.getJaderColumnValue("chiSquareJader") ?: '-',
                prrLCIJader                : this.getJaderColumnValue("prrLCIJader") ?: '-',
                prrUCIJader                : this.getJaderColumnValue("prrUCIJader") ?: '-',
                rorUCIJader                : this.getJaderColumnValue("rorUCIJader") ?: '-',
                rorLCIJader                : this.getJaderColumnValue("rorLCIJader") ?: '-',
                aValueJader                : this.getJaderColumnValue("aValueJader") ?: '-',
                bValueJader                : this.getJaderColumnValue("bValueJader") ?: '-',
                cValueJader                : this.getJaderColumnValue("cValueJader") ?: '-',
                dValueJader                : this.getJaderColumnValue("dValueJader") ?: '-',
                eValueJader                : this.getJaderColumnValue("eValueJader") ?: '-',
                rrValueJader               : this.getJaderColumnValue("rrValueJader") ?: '-',
                isUndoEnabled                  : isUndoable?'true':'false',
                isDefaultState                 : this.isDefaultState()
        ]
        if (this.newCountsJson) {
            Map data = JSON.parse(this.newCountsJson)
            for (String k: data.keySet()) {
                String key = k
                String value = data.get(k)
                aggAlertData.put(key, value)
            }
        }
        aggAlertData.signalsAndTopics = validatedSignal
        aggAlertData
    }

    Map toRestDto(String timeZone = "UTC", isSpecialPE = false, String trend = Constants.Commons.EVEN,
                  smqMap = null, Map ptMap = [isIme: "", isDme: "", isEi: "", isSm: ""],
                  List<String> tagNameList = null, List validatedSignal = [], String comments = null,
                  Boolean isAttachment = false, Boolean isExport = false, Boolean isUndoable=false,
                  Long commentId = null, List<String> requiredFields, Map parameter,
                  String dataAnalysisLabel, Map analysisStatusJson, Boolean isArchived = false) {

        DecimalFormat df = new DecimalFormat('#.#####')
        Double chisquare = Double.valueOf(df.format(this.chiSquare))
        DecimalFormat df1 = new DecimalFormat('#.###')
        Double eVal = this.eValue? Double.valueOf(df1.format(this?.eValue)):-1
        Double rrVal = this.rrValue? Double.valueOf(df1.format(this?.rrValue)):-1
        Map<String, Object> aggAlertData = [:]
        Map fieldLoaderMap = [
                priority                    : { getPriorityMap(this.priorityId) },
                assignedTo                  : { getAssignedToObj() },
                detectedDate                : { toDateString(this.detectedDate, timeZone) },
                dueDate                     : { this.dueDate != null && !this.disposition.reviewCompleted? this.dueIn() :Constants.Commons.DASH_STRING },
                proposedDisposition         : { this.proposedDisposition },
                dispositionCloseStatus      : { this.getDispositionById(this.dispositionId)?.closed },
                flagged                     : { this.flagged },
                newStudyCount               : { this.newStudyCount!=-1 ? this.newStudyCount: '-' },
                cumStudyCount               : { this.cumStudyCount!=-1 ? this.cumStudyCount: '-' },
                newSponCount                : { this.newSponCount!=-1 ? this.newSponCount: '-' },
                cumSponCount                : { this.cumSponCount!=-1 ? this.cumSponCount: '-' },
                newSeriousCount             : { this.newSeriousCount!=-1 ? this.newSeriousCount: '-' },
                cumSeriousCount             : { this.cumSeriousCount!=-1 ? this.cumSeriousCount: '-' },
                newFatalCount               : { this.newFatalCount!=-1 ? this.newFatalCount: '-' },
                cumFatalCount               : { this.cumFatalCount!=-1 ? this.cumFatalCount: '-' },
                positiveRechallenge         : { this.positiveRechallenge ?: '-' },
                positiveDechallenge         : { this.positiveDechallenge ?: '-' },
                listed                      : { this.listed ?: '-' },
                related                     : { this.related ?: '-' },
                pregenency                  : { this.pregenency ?: '-' },
                prrValue                    : { this.prrValue==0 ? '0' : this.prrValue != -1 ? this.prrValue.toString() : '-' },
                prrLCI                      : { this.prrLCI==0 ? '0' :  this.prrLCI != -1.0 ? this.prrLCI?.toString() : '-' },
                prrUCI                      : { this.prrUCI==0 ? '0' :  this.prrUCI != -1.0 ? this.prrUCI?.toString() : '-' },
                rorValue                    : { this.rorValue==0 ? '0' :  this.rorValue != -1.0 ? this.rorValue.toString() : '-' },
                rorLCI                      : { this.rorLCI==0 ? '0' :  this.rorLCI != -1.0 ? this.rorLCI?.toString() : '-' },
                rorUCI                      : { this.rorUCI==0 ? '0' :  this.rorUCI!=-1.0 ? this.rorUCI?.toString() : '-' },
                eb05                        : { this.eb05==0 ? '0' :  this.eb05 !=-1 ? this.eb05: '-'  },
                eb95                        : { this.eb95==0 ? '0' :  this.eb95 !=-1 ? this.eb95: '-' },
                ebgm                        : { this.ebgm==0 ? '0' :  this.ebgm != -1.0 ? this.ebgm : '-' },
                highPecImp                  : { this.pecImpHigh },
                lowPecImp                   : { this.pecImpLow },
                dueIn                       : { this.dueDate != null && !this.disposition.reviewCompleted? this.dueIn() :Constants.Commons.DASH_STRING },
                pt                          : { this.pt },
                primaryEvent                : { this.pt },
                soc                         : { this.soc },
                systemOrganClass            : { null },
                execConfigId                : { this.executedAlertConfigurationId },
                alertConfigId               : { this.alertConfigurationId },
                level                       : { getLevel(this.executedAlertConfiguration.productSelection) },
                trend                       : { trend },
                isSpecialPE                 : { isSpecialPE },
                groupBySmq                  : { this.alertConfiguration.groupBySmq },
                smqValue                    : { getSmqValue(this , smqMap) },
                lastUpdated                 : { this.lastUpdated },
                impEvents                   : { (isExport && this.impEvents)?replaceImpEventsLabels(this.impEvents).toUpperCase():this.impEvents ? this.impEvents.toUpperCase() : Constants.Commons.BLANK_STRING },
                ime                         : { ptMap.isIme },
                dme                         : { ptMap.isDme },
                ei                          : { ptMap.isEi },
                isSm                        : { ptMap.isSm },
                alertTags                   : { tagNameList },
                trendType                   : { this.trendType },
                freqPriority                : { this.freqPriority },
                asOfDate                    : { toDateString(this.periodEndDate) },
                executionDate               : { toDateString(this.dateCreated) },
                isReviewed                  : { getDispositionById(this.dispositionId)?.reviewCompleted },
                flags                       : { this.flags ?: "" },
                eb05Str                     : { this.eb05Str ?: '-' },
                eb95Str                     : { this.eb95Str ?: '-' },
                ebgmStr                     : { this.ebgmStr ?: '-' },
                eb95Age                     : { this.eb95Age ?: '-' },
                eb05Age                     : { this.eb05Age ?: '-' },
                ebgmAge                     : { this.ebgmAge ?: '-' },
                eb05Gender                  : { this.eb05Gender ?: '-' },
                eb95Gender                  : { this.eb95Gender ?: '-' },
                ebgmGender                  : { this.ebgmGender ?: '-' },
                ebgmSubGroup                : { this.ebgmSubGroup ?: '-' },
                eb05SubGroup                : { this.eb05SubGroup ?: '-' },
                eb95SubGroup                : { this.eb95SubGroup ?: '-' },
                rorSubGroup                 : { this.rorSubGroup ?: '-' },
                rorLciSubGroup              : { this.rorLciSubGroup ?: '-' },
                rorUciSubGroup              : { this.rorUciSubGroup ?: '-' },
                prrSubGroup                 : { this.prrSubGroup ?: '-' },
                prrLciSubGroup              : { this.prrLciSubGroup ?: '-' },
                prrUciSubGroup              : { this.prrUciSubGroup ?: '-' },
                chiSquareSubGroup           : { this.chiSquareSubGroup ?: '-' },
                rorRelSubGroup              : { this.rorRelSubGroup ?: '-' },
                rorLciRelSubGroup           : { this.rorLciRelSubGroup ?: '-' },
                rorUciRelSubGroup           : { this.rorUciRelSubGroup ?: '-' },
                newCount                    : { this.newCount!=-1 ? this.newCount: '-' },
                cummCount                   : { this.cummCount!=-1 ? this.cummCount: '-' },
                newPediatricCount           : { this.newPediatricCount!=-1 ? this.newPediatricCount: '-' },
                cummPediatricCount          : { this.cummPediatricCount!=-1 ? this.cummPediatricCount: '-' },
                newInteractingCount         : { this.newInteractingCount!=-1 ? this.newInteractingCount: '-' },
                cummInteractingCount        : { this.cummInteractingCount!=-1 ? this.cummInteractingCount: '-' },
                aValue                      : { this.aValue!=-1 ? this.aValue: '-' },
                bValue                      : { this.bValue!=-1 ? this.bValue: '-' },
                cValue                      : { this.cValue!=-1 ? this.cValue: '-' },
                dValue                      : { this.dValue!=-1 ? this.dValue: '-' },
                eValue                      : { eVal!=-1? eVal : '-' },
                rrValue                     : { rrVal!=-1? rrVal: '-' },
                chiSquare                   : { !(this.cummCount ==0 && this.newCount ==0) ? chisquare : '0' },
                evdasExecConfigId           : { this.getEvdasColumnValue("execConfigId") ?: '-' },
                newEvLink                   : { this.getEvdasColumnValue("newEvLink") ?: '-' },
                totalEvLink                 : { this.getEvdasColumnValue("totalEvLink") ?: '-' },
                newEvEvdas                  : { this.getEvdasColumnValue("newEvEvdas") ?: '-' },
                totalEvEvdas                : { this.getEvdasColumnValue("totalEvEvdas") ?: '-' },
                dmeImeEvdas                 : { this.getEvdasColumnValue("dmeImeEvdas") ?: '-' },
                sdrEvdas                    : { this.getEvdasColumnValue("sdrEvdas") ?: '-' },
                rorValueEvdas               : { this.getEvdasColumnValue("rorValueEvdas") ?: '-' },
                newPaedEvdas                : { this.getEvdasColumnValue("newPaedEvdas") ?: '-' },
                totPaedEvdas                : { this.getEvdasColumnValue("totPaedEvdas") ?: '-' },
                totalFatalEvdas             : { this.getEvdasColumnValue("totalFatalEvdas") ?: '-' },
                newFatalEvdas               : { this.getEvdasColumnValue("newFatalEvdas") ?: '-' },
                sdrPaedEvdas                : { this.getEvdasColumnValue("sdrPaedEvdas") ?: '-' },
                changesEvdas                : { this.getEvdasColumnValue("changesEvdas") ?: '-' },
                hlgtEvdas                   : { this.getEvdasColumnValue("hlgtEvdas") ?: '-' },
                hltEvdas                    : { this.getEvdasColumnValue("hltEvdas") ?: '-' },
                smqNarrowEvdas              : { this.getEvdasColumnValue("smqNarrowEvdas") ?: '-' },
                impEventsEvdas              : { this.getEvdasColumnValue("impEventsEvdas") ?: '-' },
                newEeaEvdas                 : { this.getEvdasColumnValue("newEeaEvdas") ?: '-' },
                totEeaEvdas                 : { this.getEvdasColumnValue("totEeaEvdas") ?: '-' },
                newHcpEvdas                 : { this.getEvdasColumnValue("newHcpEvdas") ?: '-' },
                totHcpEvdas                 : { this.getEvdasColumnValue("totHcpEvdas") ?: '-' },
                newSeriousEvdas             : { this.getEvdasColumnValue("newSeriousEvdas") ?: '-' },
                totalSeriousEvdas           : { this.getEvdasColumnValue("totalSeriousEvdas") ?: '-' },
                newMedErrEvdas              : { this.getEvdasColumnValue("newMedErrEvdas") ?: '-' },
                totMedErrEvdas              : { this.getEvdasColumnValue("totMedErrEvdas") ?: '-' },
                newObsEvdas                 : { this.getEvdasColumnValue("newObsEvdas") ?: '-' },
                totObsEvdas                 : { this.getEvdasColumnValue("totObsEvdas") ?: '-' },
                newRcEvdas                  : { this.getEvdasColumnValue("newRcEvdas") ?: '-' },
                totRcEvdas                  : { this.getEvdasColumnValue("totRcEvdas") ?: '-' },
                newLitEvdas                 : { this.getEvdasColumnValue("newLitEvdas") ?: '-' },
                allRorEvdas                 : { this.getEvdasColumnValue("allRorEvdas") ?: '-' },
                totalLitEvdas               : { this.getEvdasColumnValue("totalLitEvdas") ?: '-' },
                ratioRorPaedVsOthersEvdas   : { this.getEvdasColumnValue("ratioRorPaedVsOthersEvdas") ?: '-' },
                newGeriaEvdas               : { this.getEvdasColumnValue("newGeriaEvdas") ?: '-' },
                totGeriaEvdas               : { this.getEvdasColumnValue("totGeriaEvdas") ?: '-' },
                ratioRorGeriatrVsOthersEvdas: { this.getEvdasColumnValue("ratioRorGeriatrVsOthersEvdas") ?: '-' },
                sdrGeratrEvdas              : { this.getEvdasColumnValue("sdrGeratrEvdas") ?: '-' },
                newSpontEvdas               : { this.getEvdasColumnValue("newSpontEvdas") ?: '-' },
                totSpontEvdas               : { this.getEvdasColumnValue("totSpontEvdas") ?: '-' },
                totSpontEuropeEvdas         : { this.getEvdasColumnValue("totSpontEuropeEvdas") ?: '-' },
                totSpontNAmericaEvdas       : { this.getEvdasColumnValue("totSpontNAmericaEvdas") ?: '-' },
                totSpontJapanEvdas          : { this.getEvdasColumnValue("totSpontJapanEvdas") ?: '-' },
                totSpontAsiaEvdas           : { this.getEvdasColumnValue("totSpontAsiaEvdas") ?: '-' },
                totSpontRestEvdas           : { this.getEvdasColumnValue("totSpontRestEvdas") ?: '-' },
                europeRorEvdas              : { this.getEvdasColumnValue("europeRorEvdas") ?: '-' },
                northAmericaRorEvdas        : { this.getEvdasColumnValue("northAmericaRorEvdas") ?: '-' },
                japanRorEvdas               : { this.getEvdasColumnValue("japanRorEvdas") ?: '-' },
                asiaRorEvdas                : { this.getEvdasColumnValue("asiaRorEvdas") ?: '-' },
                restRorEvdas                : { this.getEvdasColumnValue("restRorEvdas") ?: '-' },
                listedEvdas                 : { getEvdasListednessValue(this.getEvdasColumnValue("listedEvdas")) },
                faersExecConfigId           : { this.getFaersColumnValue("executedAlertConfigurationId") ?: '-' },
                newCountFaers               : { this.getFaersColumnValue("newCountFaers") ?: '-' },
                cummCountFaers              : { this.getFaersColumnValue("cummCountFaers") ?: '-' },
                newSeriousCountFaers        : { this.getFaersColumnValue("newSeriousCountFaers") ?: '-' },
                cumSeriousCountFaers        : { this.getFaersColumnValue("cumSeriousCountFaers") ?: '-' },
                eb05Faers                   : { this.getFaersColumnValue("eb05Faers") ?: '-' },
                eb95Faers                   : { this.getFaersColumnValue("eb95Faers") ?: '-' },
                newSponCountFaers           : { this.getFaersColumnValue("newSponCountFaers") ?: '-' },
                cumSponCountFaers           : { this.getFaersColumnValue("cumSponCountFaers") ?: '-' },
                newStudyCountFaers          : { this.getFaersColumnValue("newStudyCountFaers") ?: '-' },
                cumStudyCountFaers          : { this.getFaersColumnValue("cumStudyCountFaers") ?: '-' },
                newInteractingCountFaers    : { this.getFaersColumnValue("newInteractingCountFaers") ?: '-' },
                cummInteractingCountFaers   : { this.getFaersColumnValue("cummInteractingCountFaers") ?: '-' },
                newFatalCountFaers          : { this.getFaersColumnValue("newFatalCountFaers") ?: '-' },
                cumFatalCountFaers          : { this.getFaersColumnValue("cumFatalCountFaers") ?: '-' },
                newGeriatricCountFaers      : { this.getFaersColumnValue("newGeriatricCountFaers") ?: '-' },
                cumGeriatricCountFaers      : { this.getFaersColumnValue("cumGeriatricCountFaers") ?: '-' },
                newNonSeriousFaers          : { this.getFaersColumnValue("newNonSeriousFaers") ?: '-' },
                cumNonSeriousFaers          : { this.getFaersColumnValue("cumNonSeriousFaers") ?: '-' },
                ebgmFaers                   : { this.getFaersColumnValue("ebgmFaers") ?: '-' },
                prrValueFaers               : { this.getFaersColumnValue("prrValueFaers") ?: '-' },
                rorValueFaers               : { this.getFaersColumnValue("rorValueFaers") ?: '-' },
                chiSquareFaers              : { this.getFaersColumnValue("chiSquareFaers") ?: '-' },
                listedFaers                 : { this.getFaersColumnValue("listedFaers") ?: '-' },
                trendTypeFaers              : { this.getFaersColumnValue("trendTypeFaers") ?: '-' },
                freqPriorityFaers           : { this.getFaersColumnValue("freqPriorityFaers") ?: '-' },
                impEventsFaers              : { this.getFaersColumnValue("impEventsFaers") ?: '-' },
                positiveRechallengeFaers    : { this.getFaersColumnValue("positiveRechallengeFaers") ?: '-' },
                positiveDechallengeFaers    : { this.getFaersColumnValue("positiveDechallengeFaers") ?: '-' },
                prrLCIFaers                 : { this.getFaersColumnValue("prrLCIFaers") ?: '-' },
                prrUCIFaers                 : { this.getFaersColumnValue("prrUCIFaers") ?: '-' },
                rorUCIFaers                 : { this.getFaersColumnValue("rorUCIFaers") ?: '-' },
                rorLCIFaers                 : { this.getFaersColumnValue("rorLCIFaers") ?: '-' },
                newPediatricCountFaers      : { this.getFaersColumnValue("newPediatricCountFaers") ?: '-' },
                cummPediatricCountFaers     : { this.getFaersColumnValue("cummPediatricCountFaers") ?: '-' },
                relatedFaers                : { this.getFaersColumnValue("relatedFaers") ?: '-' },
                pregenencyFaers             : { this.getFaersColumnValue("pregenencyFaers") ?: '-' },
                aValueFaers                 : { this.getFaersColumnValue("aValueFaers") ?: '-' },
                bValueFaers                 : { this.getFaersColumnValue("bValueFaers") ?: '-' },
                cValueFaers                 : { this.getFaersColumnValue("cValueFaers") ?: '-' },
                dValueFaers                 : { this.getFaersColumnValue("dValueFaers") ?: '-' },
                eValueFaers                 : { this.getFaersColumnValue("eValueFaers") ?: '-' },
                rrValueFaers                : { this.getFaersColumnValue("rrValueFaers") ?: '-' },
                eb95AgeFaers                : { this.eb95AgeFaers ?: '-' },
                eb05AgeFaers                : { this.eb05AgeFaers ?: '-' },
                ebgmAgeFaers                : { this.ebgmAgeFaers ?: '-' },
                eb05GenderFaers             : { this.eb05GenderFaers ?: '-' },
                eb95GenderFaers             : { this.eb95GenderFaers ?: '-' },
                ebgmGenderFaers             : { this.ebgmGenderFaers ?: '-' },
                isFaersOnly                 : { this.newCount == -1 },
                newGeriatricCount           : { this.newGeriatricCount!=-1 ? this.newGeriatricCount: '-' },
                cumGeriatricCount           : { this.cumGeriatricCount!=-1 ? this.cumGeriatricCount: '-' },
                newNonSerious               : { this.newNonSerious!=-1 ? this.newNonSerious: '-' },
                cumNonSerious               : { this.cumNonSerious!=-1 ? this.cumNonSerious: '-' },
                productSelection            : { this.executedAlertConfiguration?.productSelection },
                justification               : { this.justification },
                dispPerformedBy             : { this.dispPerformedBy?.equalsIgnoreCase(Constants.Commons.SYSTEM)?Constants.Commons.SYSTEM:this.dispPerformedBy },
                dispLastChange              : { this.dispLastChange? DateUtil.toDateStringWithTimeInAmFormat(this.dispLastChange, timeZone): Constants.Commons.BLANK_STRING },
                newCountVaers               : { this.getVaersColumnValue("newCountVaers") ?: '-' },
                cummCountVaers              : { this.getVaersColumnValue("cummCountVaers") ?: '-' },
                newSeriousCountVaers        : { this.getVaersColumnValue("newSeriousCountVaers") ?: '-' },
                cumSeriousCountVaers        : { this.getVaersColumnValue("cumSeriousCountVaers") ?: '-' },
                eb05Vaers                   : { this.getVaersColumnValue("eb05Vaers") ?: '-' },
                eb95Vaers                   : { this.getVaersColumnValue("eb95Vaers") ?: '-' },
                newFatalCountVaers          : { this.getVaersColumnValue("newFatalCountVaers") ?: '-' },
                cumFatalCountVaers          : { this.getVaersColumnValue("cumFatalCountVaers") ?: '-' },
                newGeriatricCountVaers      : { this.getVaersColumnValue("newGeriatricCountVaers") ?: '-' },
                cumGeriatricCountVaers      : { this.getVaersColumnValue("cumGeriatricCountVaers") ?: '-' },
                newPediatricCountVaers      : { this.getVaersColumnValue("newPediatricCountVaers") ?: '-' },
                cummPediatricCountVaers     : { this.getVaersColumnValue("cummPediatricCountVaers") ?: '-' },
                vaersExecConfigId           : { this.getVaersColumnValue("executedAlertConfigurationId") ?: '-' },
                ebgmVaers                   : { this.getVaersColumnValue("ebgmVaers") ?: '-' },
                prrValueVaers               : { this.getVaersColumnValue("prrValueVaers") ?: '-' },
                rorValueVaers               : { this.getVaersColumnValue("rorValueVaers") ?: '-' },
                chiSquareVaers              : { this.getVaersColumnValue("chiSquareVaers") ?: '-' },
                prrLCIVaers                 : { this.getVaersColumnValue("prrLCIVaers") ?: '-' },
                prrUCIVaers                 : { this.getVaersColumnValue("prrUCIVaers") ?: '-' },
                rorUCIVaers                 : { this.getVaersColumnValue("rorUCIVaers") ?: '-' },
                rorLCIVaers                 : { this.getVaersColumnValue("rorLCIVaers") ?: '-' },
                aValueVaers                 : { this.getVaersColumnValue("aValueVaers") ?: '-' },
                bValueVaers                 : { this.getVaersColumnValue("bValueVaers") ?: '-' },
                cValueVaers                 : { this.getVaersColumnValue("cValueVaers") ?: '-' },
                dValueVaers                 : { this.getVaersColumnValue("dValueVaers") ?: '-' },
                eValueVaers                 : { this.getVaersColumnValue("eValueVaers") ?: '-' },
                rrValueVaers                : { this.getVaersColumnValue("rrValueVaers") ?: '-' },
                trendFlag                   : { this.trendFlag?:Constants.Commons.DASH_STRING },
                isSafety                    : { this.isSafety },
                newProdCount                : { this.newProdCount || this.newProdCount == 0 ? this.newProdCount : Constants.Commons.DASH_STRING },
                cumProdCount                : { this.cumProdCount || this.cumProdCount == 0 ? this.cumProdCount : Constants.Commons.DASH_STRING },
                freqPeriod                  : { this.freqPeriod || this.freqPeriod == 0 ? this.freqPeriod : Constants.Commons.DASH_STRING },
                cumFreqPeriod               : { this.cumFreqPeriod || this.cumFreqPeriod == 0 ? this.cumFreqPeriod : Constants.Commons.DASH_STRING },
                reviewedFreqPeriod          : { this.reviewedFreqPeriod || this.reviewedFreqPeriod == 0 ? this.reviewedFreqPeriod : Constants.Commons.DASH_STRING },
                reviewedCumFreqPeriod       : { this.reviewedCumFreqPeriod || this.reviewedCumFreqPeriod == 0 ? this.reviewedCumFreqPeriod : Constants.Commons.DASH_STRING },
                pecImpNumLow                : { this.pecImpNumLow ? this.pecImpNumLow.toString() : Constants.Commons.DASH_STRING },
                pecImpNumHigh               : { this.pecImpNumHigh ? this.pecImpNumHigh.toString() : Constants.Commons.DASH_STRING },
                dssScore                    : { this.dssScore?: Constants.Commons.DASH_STRING },
                rationale                   : { this.rationale? this.rationale : Constants.Commons.DASH_STRING },
                newCountVigibase               : { this.getVigibaseColumnValue("newCountVigibase") ?: '-' },
                cummCountVigibase              : { this.getVigibaseColumnValue("cummCountVigibase") ?: '-' },
                newSeriousCountVigibase        : { this.getVigibaseColumnValue("newSeriousCountVigibase") ?: '-' },
                cumSeriousCountVigibase        : { this.getVigibaseColumnValue("cumSeriousCountVigibase") ?: '-' },
                eb05Vigibase                   : { this.getVigibaseColumnValue("eb05Vigibase") ?: '-' },
                eb95Vigibase                   : { this.getVigibaseColumnValue("eb95Vigibase") ?: '-' },
                newFatalCountVigibase          : { this.getVigibaseColumnValue("newFatalCountVigibase") ?: '-' },
                cumFatalCountVigibase          : { this.getVigibaseColumnValue("cumFatalCountVigibase") ?: '-' },
                newGeriatricCountVigibase      : { this.getVigibaseColumnValue("newGeriatricCountVigibase") ?: '-' },
                cumGeriatricCountVigibase      : { this.getVigibaseColumnValue("cumGeriatricCountVigibase") ?: '-' },
                newPediatricCountVigibase      : { this.getVigibaseColumnValue("newPediatricCountVigibase") ?: '-' },
                cummPediatricCountVigibase     : { this.getVigibaseColumnValue("cummPediatricCountVigibase") ?: '-' },
                vigibaseExecConfigId           : { this.getVigibaseColumnValue("executedAlertConfigurationId") ?: '-' },
                ebgmVigibase                   : { this.getVigibaseColumnValue("ebgmVigibase") ?: '-' },
                prrValueVigibase               : { this.getVigibaseColumnValue("prrValueVigibase") ?: '-' },
                rorValueVigibase               : { this.getVigibaseColumnValue("rorValueVigibase") ?: '-' },
                chiSquareVigibase              : { this.getVigibaseColumnValue("chiSquareVigibase") ?: '-' },
                prrLCIVigibase                 : { this.getVigibaseColumnValue("prrLCIVigibase") ?: '-' },
                prrUCIVigibase                 : { this.getVigibaseColumnValue("prrUCIVigibase") ?: '-' },
                rorUCIVigibase                 : { this.getVigibaseColumnValue("rorUCIVigibase") ?: '-' },
                rorLCIVigibase                 : { this.getVigibaseColumnValue("rorLCIVigibase") ?: '-' },
                aValueVigibase                 : { this.getVigibaseColumnValue("aValueVigibase") ?: '-' },
                bValueVigibase                 : { this.getVigibaseColumnValue("bValueVigibase") ?: '-' },
                cValueVigibase                 : { this.getVigibaseColumnValue("cValueVigibase") ?: '-' },
                dValueVigibase                 : { this.getVigibaseColumnValue("dValueVigibase") ?: '-' },
                eValueVigibase                 : { this.getVigibaseColumnValue("eValueVigibase") ?: '-' },
                rrValueVigibase                : { this.getVigibaseColumnValue("rrValueVigibase") ?: '-' },
                newCountJader              : { this.getJaderColumnValue("newCountJader") ?: '-' },
                cumCountJader              : { this.getJaderColumnValue("cumCountJader") ?: '-' },
                newSeriousCountJader       : { this.getJaderColumnValue("newSeriousCountJader") ?: '-' },
                cumSeriousCountJader       : { this.getJaderColumnValue("cumSeriousCountJader") ?: '-' },
                eb05Jader                  : { this.getJaderColumnValue("eb05Jader") ?: '-' },
                eb95Jader                  : { this.getJaderColumnValue("eb95Jader") ?: '-' },
                newFatalCountJader         : { this.getJaderColumnValue("newFatalCountJader") ?: '-' },
                cumFatalCountJader         : { this.getJaderColumnValue("cumFatalCountJader") ?: '-' },
                newGeriatricCountJader     : { this.getJaderColumnValue("newGeriatricCountJader") ?: '-' },
                cumGeriatricCountJader     : { this.getJaderColumnValue("cumGeriatricCountJader") ?: '-' },
                newPediatricCountJader     : { this.getJaderColumnValue("newPediatricCountJader") ?: '-' },
                cumPediatricCountJader     : { this.getJaderColumnValue("cumPediatricCountJader") ?: '-' },
                ebgmJader                  : { this.getJaderColumnValue("ebgmJader") ?: '-' },
                prrValueJader              : { this.getJaderColumnValue("prrValueJader") ?: '-' },
                rorValueJader              : { this.getJaderColumnValue("rorValueJader") ?: '-' },
                chiSquareJader             : { this.getJaderColumnValue("chiSquareJader") ?: '-' },
                prrLCIJader                : { this.getJaderColumnValue("prrLCIJader") ?: '-' },
                prrUCIJader                : { this.getJaderColumnValue("prrUCIJader") ?: '-' },
                rorUCIJader                : { this.getJaderColumnValue("rorUCIJader") ?: '-' },
                rorLCIJader                : { this.getJaderColumnValue("rorLCIJader") ?: '-' },
                aValueJader                : { this.getJaderColumnValue("aValueJader") ?: '-' },
                bValueJader                : { this.getJaderColumnValue("bValueJader") ?: '-' },
                cValueJader                : { this.getJaderColumnValue("cValueJader") ?: '-' },
                dValueJader                : { this.getJaderColumnValue("dValueJader") ?: '-' },
                eValueJader                : { this.getJaderColumnValue("eValueJader") ?: '-' },
                rrValueJader               : { this.getJaderColumnValue("rrValueJader") ?: '-' }
        ]

        aggAlertData.put("alertId", this.id)
        aggAlertData.put("alertName", this.name)
        aggAlertData.put("configId", this.alertConfigurationId)
        aggAlertData.put("exConfigId", this.executedAlertConfigurationId)
        aggAlertData.put("disposition", getDispositionById(this.dispositionId)?.displayName)
        aggAlertData.put("isArchived", false)
        aggAlertData.put("isAttachment", isAttachment)
        aggAlertData.put("currentDispositionId", this.dispositionId)
        aggAlertData.put("isUndoEnabled",  isUndoable?'true':'false')
        aggAlertData.put("isDefaultState", this.isDefaultState())
        aggAlertData.put("isValidationStateAchieved", getDispositionById(this.dispositionId)?.validatedConfirmed)
        aggAlertData.put("productName", this.productName)
        aggAlertData.put("actionCount", this.actionCount)
        aggAlertData.put("dataSource", this.alertConfiguration.selectedDatasource.toUpperCase())
        def jsonSlurper = new JsonSlurper()
        if (this.format) {
            aggAlertData.put("format", jsonSlurper.parseText(this.format))
        } else {
            aggAlertData.put("format", null)
        }
        aggAlertData.put("comment", comments)
        aggAlertData.put("commentId", commentId)
        aggAlertData.put("ptCode", this.ptCode)
        aggAlertData.put("productId", this.productId as String)

        requiredFields.each { fieldName ->
            if (fieldLoaderMap.containsKey(fieldName)) {
                aggAlertData.put(fieldName, fieldLoaderMap[fieldName]())
            }
        }

        aggAlertData.put("productJson", this.executedAlertConfiguration.productSelection)

        if (aggAlertData.containsKey("soc")) {
            aggAlertData.put("fullSoc", dataObjectService.getAbbreviationMap(this.soc))
        }

        Map keyId = [
                "newCount" : "REPORT_TOTAL_FLG",
                "newSponCount": "REPORT_SPON_FLG",
                "newSeriousCount" : "REPORT_SERIOUS_FLG",
                "newFatalCount" : "REPORT_FATAL_FLG",
                "newStudyCount" : "REPORT_STUDY_FLG",
                "newPediatricCount" : "REPORT_PEDIA_FLG",
                "newInteractingCount" : "REPORT_SUSP_INT_DRUG_FLG",
                "newGeriatricCount" : "REPORT_GERIA_FLG",
                "newNonSerious" : "REPORT_NON_SERIOUS_FLG"
        ]

        def isFaers = this.alertConfiguration.selectedDatasource == "faers"
        Map urls = [:]

        parameter << ["dataAnalysisLabel" : dataAnalysisLabel]

        if (aggAlertData.containsKey("newCount")) {
            urls.put("newCountUrl", getSingleCaseDetailsUrl(this.newCount, "NEW", "CUMM_FLAG",
                    keyId.get("newCount"), this.executedAlertConfigurationId, this.id, this.productId, this.ptCode,
                    this.alertConfiguration.groupBySmq, this.pt, "NEW_COUNT", parameter, isFaers,
                    analysisStatusJson,isArchived))
            urls.put("cummCountUrl", getSingleCaseDetailsUrl(this.cummCount, "CUMM", "CUMM_FLAG",
                    keyId.get("newCount"), this.executedAlertConfigurationId, this.id, this.productId, this.ptCode,
                    this.alertConfiguration.groupBySmq, this.pt, "CUMM_COUNT", parameter, isFaers,
                    analysisStatusJson,isArchived))
        }

        if (aggAlertData.containsKey("newSponCount")) {
            urls.put("newSponCountUrl", getSingleCaseDetailsUrl(this.newSponCount, "NEW", "SPONT_FLAG",
                    keyId.get("newSponCount"), this.executedAlertConfigurationId, this.id, this.productId, this.ptCode,
                    this.alertConfiguration.groupBySmq, this.pt, "NEW_SPON", parameter, isFaers,
                    analysisStatusJson,isArchived))
            urls.put("cumSponCountUrl", getSingleCaseDetailsUrl(this.cumSponCount, "CUMM", "SPONT_FLAG",
                    keyId.get("newSponCount"), this.executedAlertConfigurationId, this.id, this.productId, this.ptCode,
                    this.alertConfiguration.groupBySmq, this.pt, "CUMM_SPON", parameter, isFaers,
                    analysisStatusJson,isArchived))
        }

        if (aggAlertData.containsKey("newSeriousCount")) {
            urls.put("newSeriousCountUrl", getSingleCaseDetailsUrl(this.newSeriousCount, "NEW", "SERIOUS_FLAG",
                        keyId.get("newSeriousCount"), this.executedAlertConfigurationId, this.id, this.productId, this.ptCode,
                        this.alertConfiguration.groupBySmq, this.pt, "NEW_SER", parameter, isFaers,
                        analysisStatusJson,isArchived))
            urls.put("cumSeriousCountUrl", getSingleCaseDetailsUrl(this.cumSeriousCount, "CUMM", "SERIOUS_FLAG",
                        keyId.get("newSeriousCount"), this.executedAlertConfigurationId, this.id, this.productId, this.ptCode,
                        this.alertConfiguration.groupBySmq, this.pt, "CUMM_SER", parameter, isFaers,
                        analysisStatusJson,isArchived))
        }

        if (aggAlertData.containsKey("newFatalCount")) {
            urls.put("newFatalCountUrl", getSingleCaseDetailsUrl(this.newFatalCount, "NEW", "FATAL_FLAG",
                        keyId.get("newFatalCount"), this.executedAlertConfigurationId, this.id, this.productId, this.ptCode,
                        this.alertConfiguration.groupBySmq, this.pt, "NEW_FATAL", parameter, isFaers,
                        analysisStatusJson,isArchived))
            urls.put("cumFatalCountUrl", getSingleCaseDetailsUrl(this.cumFatalCount, "CUMM", "FATAL_FLAG",
                        keyId.get("newFatalCount"), this.executedAlertConfigurationId, this.id, this.productId, this.ptCode,
                        this.alertConfiguration.groupBySmq, this.pt, "CUMM_FATAL", parameter, isFaers,
                        analysisStatusJson,isArchived))
        }

        if (aggAlertData.containsKey("newStudyCount")) {
            urls.put("newStudyCountUrl", getSingleCaseDetailsUrl(this.newStudyCount, "NEW", "STUDY_FLAG",
                        keyId.get("newStudyCount"), this.executedAlertConfigurationId, this.id, this.productId, this.ptCode,
                        this.alertConfiguration.groupBySmq, this.pt, "NEW_STUDY", parameter, isFaers,
                        analysisStatusJson,isArchived))
            urls.put("cumStudyCountUrl", getSingleCaseDetailsUrl(this.cumStudyCount, "CUMM", "STUDY_FLAG",
                        keyId.get("newStudyCount"), this.executedAlertConfigurationId, this.id, this.productId, this.ptCode,
                        this.alertConfiguration.groupBySmq, this.pt, "CUMM_STUDY", parameter, isFaers,
                        analysisStatusJson,isArchived))
        }

        if (aggAlertData.containsKey("newPediatricCount")) {
            urls.put("newPediatricCountUrl", getSingleCaseDetailsUrl(this.newPediatricCount, "NEW", "PEDI_FLAG",
                        keyId.get("newPediatricCount"), this.executedAlertConfigurationId, this.id, this.productId, this.ptCode,
                        this.alertConfiguration.groupBySmq, this.pt, "NEW_PEDI", parameter, isFaers,
                        analysisStatusJson,isArchived))
            urls.put("cumPediatricCountUrl", getSingleCaseDetailsUrl(this.cummPediatricCount, "CUMM", "PEDI_FLAG",
                        keyId.get("newPediatricCount"), this.executedAlertConfigurationId, this.id, this.productId, this.ptCode,
                        this.alertConfiguration.groupBySmq, this.pt, "CUMM_PEDI", parameter, isFaers,
                        analysisStatusJson,isArchived))
        }

        if (aggAlertData.containsKey("newInteractingCount")) {
            urls.put("newInteractingCountUrl", getSingleCaseDetailsUrl(this.newInteractingCount, "NEW", "INTERACTING_FLAG",
                        keyId.get("newInteractingCount"), this.executedAlertConfigurationId, this.id, this.productId, this.ptCode,
                        this.alertConfiguration.groupBySmq, this.pt, "NEW_INTERACTING", parameter, isFaers,
                        analysisStatusJson,isArchived))
            urls.put("cummInteractingCountUrl", getSingleCaseDetailsUrl(this.cummInteractingCount, "CUMM", "INTERACTING_FLAG",
                        keyId.get("newInteractingCount"), this.executedAlertConfigurationId, this.id, this.productId, this.ptCode,
                        this.alertConfiguration.groupBySmq, this.pt, "CUMM_INTERACTING", parameter, isFaers,
                        analysisStatusJson,isArchived))
        }

        if (aggAlertData.containsKey("newGeriatricCount")) {
            urls.put("newGeriatricCountUrl", getSingleCaseDetailsUrl(this.newGeriatricCount, "NEW", "GERI_FLAG",
                        keyId.get("newGeriatricCount"), this.executedAlertConfigurationId, this.id, this.productId, this.ptCode,
                        this.alertConfiguration.groupBySmq, this.pt, "NEW_GERI", parameter, isFaers,
                        analysisStatusJson,isArchived))
            urls.put("cumGeriatricCountUrl", getSingleCaseDetailsUrl(this.cumGeriatricCount, "CUMM", "GERI_FLAG",
                        keyId.get("newGeriatricCount"), this.executedAlertConfigurationId, this.id, this.productId, this.ptCode,
                        this.alertConfiguration.groupBySmq, this.pt, "CUMM_GERI", parameter, isFaers,
                        analysisStatusJson,isArchived))
        }

        if (aggAlertData.containsKey("newNonSerious")) {
            urls.put("newNonSeriousUrl", getSingleCaseDetailsUrl(this.newNonSerious, "NEW", "NON_SERIOUS_FLAG",
                        keyId.get("newNonSerious"), this.executedAlertConfigurationId, this.id, this.productId, this.ptCode,
                        this.alertConfiguration.groupBySmq, this.pt, "NEW_NON_SERIOUS", parameter, isFaers,
                        analysisStatusJson,isArchived))
            urls.put("cumNonSeriousUrl", getSingleCaseDetailsUrl(this.cumNonSerious, "CUMM", "NON_SERIOUS_FLAG",
                        keyId.get("NonSerious"), this.executedAlertConfigurationId, this.id, this.productId, this.ptCode,
                        this.alertConfiguration.groupBySmq, this.pt, "CUMM_NON_SERIOUS", parameter, isFaers,
                        analysisStatusJson,isArchived))
        }

        aggAlertData.putAll("urls" : urls)

        if (this.newCountsJson) {
            Map data = JSON.parse(this.newCountsJson)
            for (String k: data.keySet()) {
                String key = k
                String value = data.get(k)
                aggAlertData.put(key, value)
            }
        }
        aggAlertData.signalsAndTopics = validatedSignal
        aggAlertData
    }

    def getSingleCaseDetailsUrl(int value, String type, String typeFlag, String keyId, long executedConfigId,
                                long alertId, BigInteger productId, int ptCode, boolean groupBySmq, String preferredTerm,
                                String countType, Map parameter, boolean isFaers, def analysisStatusJson,boolean isArchived) {

        def links = [:]
        if (value <= 0) {
            return
        }
        def singleCaseDetailsUrl = "${parameter.singleCaseUrl}?aggExecutionId=${executedConfigId}&aggAlertId=${alertId}&aggCountType=${countType}&productId=${productId}&ptCode=${ptCode}&type=${type}&typeFlag=${keyId}&isArchived=${isArchived}&version=${this.version}&numberOfCount=${value}"

        // Set term scope based on preferredTerm
        def pTermScope = 0
        def termScopeStr = preferredTerm

        if (termScopeStr == 'Broad') {
            pTermScope = 1
        } else if (termScopeStr == 'Narrow') {
            pTermScope = 2
        }

        // Construct customAnalysisUrl
        def customAnalysisUrl = "${parameter.customAnalysisThirdPartyUrl}${parameter.queryParam}${parameter.executedConfigId}/${executedConfigId}/${parameter.queryParam}${parameter.baseId}/${productId}/${parameter.queryParam}${parameter.meddraPtCode}/${ptCode}/"
        if (type == 'NEW') {
            customAnalysisUrl += "${parameter.queryParam}${parameter.dateRangeTypeFlag}/1/"
        }
        if (groupBySmq) {
            customAnalysisUrl += "${parameter.queryParam}${parameter.termScope}/${pTermScope}/"
        }
        if (typeFlag) {
            customAnalysisUrl += "${parameter.queryParam}${parameter.flag}${typeFlag}/1"
        }

        if (parameter.pvrIntegrate) {
            def hasReviewerAccessVar = parameter.hasReviewerAccess
            if (parameter.isCaseSeriesAccess) {
                links.put("signalCase", singleCaseDetailsUrl)
            }
            if (!isFaers) {
                def reportUrl = "${parameter.templateListUrl}?configId=${executedConfigId}&type=${type}&alertId=${alertId}&typeFlag=${keyId}&keyId=${keyId}&isAggScreen=true&hasReviewerAccess=${hasReviewerAccessVar}&preferredTerm=${URLEncoder.encode(preferredTerm, 'UTF-8')}&version=${this.version}"
                links.put("report", reportUrl)
            }
            if (parameter.isCustomAnalysisEnabled) {
                links.put(parameter.customAnalysisLabel, customAnalysisUrl)
            } else if (parameter.spotfireEnabled && this.alertConfiguration.selectedDatasource == "pva") {
                if (type == "NEW" && analysisStatusJson?.PR_DATE_RANGE?.status == 2) {
                    links.put(parameter.dataAnalysisLabel, analysisStatusJson.PR_DATE_RANGE.url)
                }
                else if (type == "CUMM" && analysisStatusJson?.CUMULATIVE?.status == 2) {
                    links.put(parameter.dataAnalysisLabel, analysisStatusJson.CUMULATIVE.url)
                }
            }
        } else {
            links.put("signalCase", singleCaseDetailsUrl)
        }

        return links
    }

    def getFaersUrl(int value, String type, String typeFlag, String keyId, long executedConfigId,
                    long alertId, BigInteger productId, int ptCode, boolean groupBySmq, String preferredTerm,
                    String countType, Map parameter, boolean isFaers, def analysisStatusJson,boolean isArchived) {
        def links = [:]
        if (value <= 0) {
            return
        }

        def singleCaseDetailsUrl = "${parameter.singleCaseUrl}?aggExecutionId=${executedConfigId}&aggAlertId=${alertId}&aggCountType=${countType}&productId=${productId}&ptCode=${ptCode}&type=${type}&typeFlag=${keyId}&isArchived=${isArchived}&numberOfCount=${value}"
        // Set term scope based on preferredTerm
        def pTermScope = 0
        def termScopeStr = preferredTerm

        if (termScopeStr == 'Broad') {
            pTermScope = 1
        } else if (termScopeStr == 'Narrow') {
            pTermScope = 2
        }

        // Construct customAnalysisUrl
        def customAnalysisUrl = "${parameter.customAnalysisThirdPartyUrl}${parameter.queryParam}${parameter.executedConfigId}/${executedConfigId}/${parameter.queryParam}${parameter.baseId}/${productId}/${parameter.queryParam}${parameter.meddraPtCode}/${ptCode}/"
        if (type == 'NEW') {
            customAnalysisUrl += "${parameter.queryParam}${parameter.dateRangeTypeFlag}/1/"
        }
        if (groupBySmq) {
            customAnalysisUrl += "${parameter.queryParam}${parameter.termScope}/${pTermScope}/"
        }
        if (typeFlag) {
            customAnalysisUrl += "${parameter.queryParam}${parameter.flag}${typeFlag}/1"
        }

        if (parameter.pvrIntegrate) {
            def hasReviewerAccessVar = parameter.hasReviewerAccess
            if (parameter.isCaseSeriesAccess) {
                links.put("signalCase", singleCaseDetailsUrl)
            }
            if (parameter.isCustomAnalysisEnabled) {
                links.put(parameter.customAnalysisLabel, customAnalysisUrl)
            } else if (parameter.spotfireEnabled && this.alertConfiguration.selectedDatasource == "pva") {
                if (type == "NEW" && analysisStatusJson.PR_DATE_RANGE_FAERS.status == 2) {
                    links.put(parameter.dataAnalysisLabel, analysisStatusJson.PR_DATE_RANGE_FAERS.url)
                }
                else if (type == "CUMM" && analysisStatusJson.CUMULATIVE_FAERS.status == 2) {
                    links.put(parameter.dataAnalysisLabel, analysisStatusJson.CUMULATIVE_FAERS.url)
                }
            }
        } else {
            links.put("signalCase", singleCaseDetailsUrl)
        }

        return links

    }

    def getVaersUrl(int value, String type, String typeFlag, String keyId, long executedConfigId,
                    long alertId, BigInteger productId, int ptCode, boolean groupBySmq, String preferredTerm,
                    String countType, Map parameter, boolean isFaers, def analysisStatusJson,boolean isArchived) {
        def links = [:]
        if (value <= 0) {
            return
        }

        def singleCaseDetailsUrl = "${parameter.singleCaseUrl}?aggExecutionId=${executedConfigId}&aggAlertId=${alertId}&aggCountType=${countType}&productId=${productId}&ptCode=${ptCode}&type=${type}&typeFlag=${keyId}&isArchived=${isArchived}&numberOfCount=${value}"
        // Set term scope based on preferredTerm
        def pTermScope = 0
        def termScopeStr = preferredTerm

        if (termScopeStr == 'Broad') {
            pTermScope = 1
        } else if (termScopeStr == 'Narrow') {
            pTermScope = 2
        }

        // Construct customAnalysisUrl
        def customAnalysisUrl = "${parameter.customAnalysisThirdPartyUrl}${parameter.queryParam}${parameter.executedConfigId}/${executedConfigId}/${parameter.queryParam}${parameter.baseId}/${productId}/${parameter.queryParam}${parameter.meddraPtCode}/${ptCode}/"
        if (type == 'NEW') {
            customAnalysisUrl += "${parameter.queryParam}${parameter.dateRangeTypeFlag}/1/"
        }
        if (groupBySmq) {
            customAnalysisUrl += "${parameter.queryParam}${parameter.termScope}/${pTermScope}/"
        }
        if (typeFlag) {
            customAnalysisUrl += "${parameter.queryParam}${parameter.flag}${typeFlag}/1"
        }

        if (parameter.pvrIntegrate) {
            def hasReviewerAccessVar = parameter.hasReviewerAccess
            if (parameter.isCaseSeriesAccess) {
                links.put("signalCase", singleCaseDetailsUrl)
            }
            if (parameter.isCustomAnalysisEnabled) {
                links.put(parameter.customAnalysisLabel, customAnalysisUrl)
            } else if (parameter.spotfireEnabled && this.alertConfiguration.selectedDatasource == "vaers") {
                if (type == "NEW" && analysisStatusJson.PR_DATE_RANGE_VAERS.status == 2) {
                    links.put(parameter.dataAnalysisLabel, analysisStatusJson.PR_DATE_RANGE_VAERS.url)
                }
                else if (type == "CUMM" && analysisStatusJson.CUMULATIVE_VAERS.status == 2) {
                    links.put(parameter.dataAnalysisLabel, analysisStatusJson.CUMULATIVE_VAERS.url)
                }
            }
        } else {
            links.put("signalCase", singleCaseDetailsUrl)
        }

        return links
    }

    def getJaderUrl(int value, String type, String typeFlag, String keyId, long executedConfigId,
                    long alertId, BigInteger productId, int ptCode, boolean groupBySmq, String preferredTerm,
                    String countType, Map parameter, boolean isFaers, def analysisStatusJson, boolean isArchived) {
        def links = [:]
        if (value <= 0) {
            return
        }

        def singleCaseDetailsUrl = "${parameter.singleCaseUrl}?aggExecutionId=${executedConfigId}&aggAlertId=${alertId}&aggCountType=${countType}&productId=${productId}&ptCode=${ptCode}&type=${type}&typeFlag=${keyId}&isArchived=${isArchived}&numberOfCount=${value}"
        // Set term scope based on preferredTerm

        if (parameter.pvrIntegrate) {
            if (parameter.isCaseSeriesAccess) {
                links.put("signalCase", singleCaseDetailsUrl)
            }
        } else {
            links.put("signalCase", singleCaseDetailsUrl)
        }

        return links
    }

    def isDefaultState(){
        if(!this.isDispChanged && getDispositionById(this.dispositionId) == getDefaultDisp() && this.dispPerformedBy == null){
            return 'true'
        }
        return 'false'
    }

    def getSmqValue(AggregateCaseAlert aggObj, TreeMap smqMap) {
        if (smqMap && aggObj.alertConfiguration.groupBySmq && smqMap.containsKey(aggObj.pt)) {
            return smqMap.get(aggObj.pt)
        } else {
            return "NA"
        }
    }

    def getAssignedToObj() {
        return this.assignedToId ? getUserForDtoByUserId(this.assignedToId)
                : this.assignedToGroupId ? getGroupForDtoByGroupId(this.assignedToGroupId) : [:]
    }

    def dueIn() {
        def theDueDate = new DateTime(dueDate).withTimeAtStartOfDay()
        def now = DateTime.now().withTimeAtStartOfDay()
        def dur = new Duration(now, theDueDate)
        dur.getStandardDays()
    }

    def getProductNameList() {
        def String prdName = getNameFieldFromJson(this.executedAlertConfiguration.productSelection)
        if (prdName) {
            prdName.toLowerCase().tokenize(',')
        } else {
            []
        }
    }

    int getLevel(String productJson) {
        if (productJson) {
            Map jsonMap = parseJsonString(productJson)
            def level = jsonMap.find { it.value }?.key
            level as int
        }
        0
    }

    def getProductDictionarySelection() {
        this.executedAlertConfiguration.productDictionarySelection
    }

    Map getPriorityMap(Long priorityId) {
        Priority priority = cacheService.getPriorityByValue(priorityId)
        [value: priority?.value, iconClass: priority?.iconClass]
    }

    Disposition getDispositionById(Long dispositionId) {
        cacheService.getDispositionByValue(dispositionId)
    }
    Disposition getDefaultDisp(){
        cacheService.getDefaultDisp(Constants.AlertType.AGGREGATE_NEW)
    }

    User getUserByUserId(Long userId) {
        cacheService.getUserByUserId(userId)
    }

    Group getGroupByGroupId(Long groupId) {
        cacheService.getGroupByGroupId(groupId)
    }

    def getUserForDtoByUserId(Long userId) {
        User user = cacheService.getUserByUserId(userId)
        if (user) {
            return ["id" : "User_" + userId,
                    "name" : user.fullName]
        }
        return [:]
    }

    def getGroupForDtoByGroupId(Long groupId) {
        Group group = cacheService.getGroupByGroupId(groupId)
        if (group) {
            return ["id" : "UserGroup_" + groupId,
                    "name": group.name]
        }
        return [:]
    }

    def getEvdasColumnValue(String columnName) {
        if (!evdasColumnMap) {
            evdasColumnMap = populateEvdasColumnMap()
        }
        if (evdasColumnMap) {
            def value = evdasColumnMap[columnName]
            if (value == 0)
                return "0"
            return value
        } else {
            null
        }
    }

    def populateEvdasColumnMap() {
        JsonSlurper jsonSlurper = new JsonSlurper()
        evdasColumns ? jsonSlurper.parseText(this.evdasColumns) ?: [:] : [:]
    }

    def getFaersColumnValue(String columnName) {
        if (!faersColumnMap) {
            faersColumnMap = populateFaersColumnMap()
        }
        if (faersColumnMap) {
            def value = faersColumnMap[columnName]
            if (value == 0)
                return "0"
            return value
        } else {
            null
        }
    }

    def populateFaersColumnMap() {
        JsonSlurper jsonSlurper = new JsonSlurper()
        faersColumns ? jsonSlurper.parseText(this.faersColumns) ?: [:] : [:]
    }
    def populateVaersColumnMap() {
        JsonSlurper jsonSlurper = new JsonSlurper()
        vaersColumns ? jsonSlurper.parseText(this.vaersColumns) ?: [:] : [:]
    }
    def getVaersColumnValue(String columnName) {
        if (!vaersColumnMap) {
            vaersColumnMap = populateVaersColumnMap()
        }
        if (vaersColumnMap) {
            def value = vaersColumnMap[columnName]
            if (value == 0)
                return "0"
            return value
        } else {
            null
        }
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
    def populateVigibaseColumnMap() {
        JsonSlurper jsonSlurper = new JsonSlurper()
        vigibaseColumns ? jsonSlurper.parseText(this.vigibaseColumns) ?: [:] : [:]
    }
    def getVigibaseColumnValue(String columnName) {
        if (!vigibaseColumnMap) {
            vigibaseColumnMap = populateVigibaseColumnMap()
        }
        if (vigibaseColumnMap) {
            def value = vigibaseColumnMap[columnName]
            if (value == 0)
                return "0"
            return value
        } else {
            null
        }
    }
    def populateJaderColumnMap() {
        JsonSlurper jsonSlurper = new JsonSlurper()
        jaderColumns ? jsonSlurper.parseText(this.jaderColumns) ?: [:] : [:]
    }
    def getJaderColumnValue(String columnName) {
        if (!jaderColumnMap) {
            jaderColumnMap = populateJaderColumnMap()
        }
        if (jaderColumnMap) {
            def value = jaderColumnMap[columnName]
            if (value == 0)
                return "0"
            return value
        } else {
            null
        }
    }

    @Override
    String toString(){
        super.toString()
    }

    def getInstanceIdentifierForAuditLog() {
        return this.executedAlertConfiguration.getInstanceIdentifierForAuditLog()+": ("+this.productName+"-"+this.pt+")"
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
