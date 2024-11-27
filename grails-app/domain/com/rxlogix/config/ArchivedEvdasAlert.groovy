package com.rxlogix.config

import com.rxlogix.BaseEvdasAlert
import com.rxlogix.Constants
import com.rxlogix.json.JsonOutput
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import grails.gorm.dirty.checking.DirtyCheck
import grails.plugins.orm.auditable.CollectionSnapshotAudit
import groovy.json.JsonSlurper
import org.joda.time.DateTime
import org.joda.time.Duration

@DirtyCheck
@CollectionSnapshotAudit
class ArchivedEvdasAlert extends BaseEvdasAlert {
    static auditable = [ignoreEvents: ["onSave"],auditableProperties:['assignedTo','assignedToGroup','disposition','priority','justification','undoJustification']]

    //Configuration related information
    EvdasConfiguration alertConfiguration
    ExecutedEvdasConfiguration executedAlertConfiguration

    //Evdas related parameters.
    String totalEvLink
    String newEvLink
    String dmeIme
    String attributes
    String ratioRorPaedVsOthers
    String ratioRorGeriatrVsOthers
    String changes
    String sdrPaed
    String sdrGeratr
    String totSpontEurope
    String totSpontNAmerica
    String totSpontJapan
    String totSpontAsia
    String totSpontRest
    Map<String, Object> attributesMap

    //Work flow related fields
    Priority priority
    Disposition disposition
    User assignedTo
    Group assignedToGroup
    Date detectedDate
    Date dueDate

    String format
    String frequency
    String flags
    String requestedBy

    Integer actionCount = 0

    boolean adhocRun
    boolean isDispChanged = false

    static attachmentable = true

    Boolean listedness = false

    List<Action> action = []
    String impEvents
    List<String> evImpEventList = []

    Boolean isNew = true
    Date reviewDate
    Date initialDueDate
    Disposition initialDisposition
    String justification
    String dispPerformedBy
    Date dispLastChange
    Date previousDueDate
    String undoJustification
    Map<String, Object> customAuditProperties

    static transients = ['attributesMap', 'action','undoJustification','customAuditProperties']
    static hasMany = [actions         : Action,
                      validatedSignals: ValidatedSignal]

    static mapping = {
        table("ARCHIVED_EVDAS_ALERT")
        executedAlertConfiguration(column: 'exec_configuration_id')
        alertConfiguration(column: 'alert_configuration_id')
        dmeIme(column: 'DME_IME')
        validatedSignals joinTable: [name: "VALIDATED_ARCH_EVDAS_ALERTS", column: "VALIDATED_SIGNAL_ID", key: "ARCHIVED_EVDAS_ALERT_ID"]
        actions joinTable: [name: "ARCHIVED_EVDAS_ALERT_ACTIONS", column: "ACTION_ID", key: "ARCHIVED_EVDAS_ALERT_ID"]
        evImpEventList joinTable: [name: "ARCHIVED_EA_IMP_EVENT_LIST", key: "ARCHIVED_EVDAS_ALERT_ID", column: "EVDAS_IMP_EVENTS"]
        attributes type: 'text', sqlType: 'clob'
        justification column: "JUSTIFICATION", length: 9000
    }

    static constraints = {
        dueDate nullable: true
        dmeIme nullable: true, blank: true
        format nullable: true, blank: true, maxSize: 8000
        attributes nullable: true
        attributes nullable: true
        frequency nullable: true
        actionCount nullable: true
        totalEvLink nullable: true, size: 0..600
        newEvLink nullable: true, size: 0..600
        ratioRorPaedVsOthers nullable: true
        ratioRorGeriatrVsOthers nullable: true
        changes nullable: true
        sdrPaed nullable: true
        sdrGeratr nullable: true
        totSpontEurope nullable: true
        totSpontNAmerica nullable: true
        totSpontJapan nullable: true
        totSpontAsia nullable: true
        totSpontRest nullable: true
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
        impEvents nullable: true
        evImpEventList nullable: true
        reviewDate nullable: true
        initialDueDate nullable:true
        initialDisposition nullable: true
        listedness nullable: true
        justification nullable: true, maxSize: 9000
        dispPerformedBy nullable: true
        isDispChanged nullable: true
        dispLastChange nullable: true
        previousDueDate nullable: true
    }

    def toDto(timeZone = "UTC", isSpecialPE = false, String trend = Constants.Commons.LOW,
              Map ptMap = [isIme: "", isDme: "", isEi: "", isSm: ""],
              def pvsState = null, List validatedSignal = [], String comment = null,
              Boolean isAttachment = false,
              Boolean isExport = false,Boolean isUndoable = false, Long commentId = null) {
        def alertData = [
                id                       : this.id,
                alertName                : this.name?.trim()?.replaceAll("\\s{2,}", " "),
                detectedDate             : DateUtil.toDateString(this.detectedDate, timeZone),
                flagged                  : this.flagged,
                dueIn                    : this.dueDate != null && !this.disposition.reviewCompleted? this.dueIn() :Constants.Commons.DASH_STRING,
                alertConfigId            : this.alertConfigurationId,
                execConfigId             : this.executedAlertConfigurationId,
                disposition              : this.getDispositionById(this.dispositionId)?.displayName,
                dispositionCloseStatus   : this.getDispositionById(this.dispositionId)?.closed,
                currentDispositionId     : this.dispositionId,
                priority                 : this.getPriorityMap(this.priorityId),
                actionCount              : this.actionCount,
                productName              : this.substance,
                substanceId              : this.substanceId,
                soc                      : this.soc,
                preferredTerm            : this.pt,
                dmeIme                   : this.dmeIme ?: "",
                newEv                    : this.newEv ?: 0,
                totalEv                  : this.totalEv ?: 0,
                newSerious               : this.newSerious ?: 0,
                totalSerious             : this.totalSerious ?: 0,
                newFatal                 : this.newFatal ?: 0,
                totalFatal               : this.totalFatal ?: 0,
                //Spontaneous counts are mapped with the 'Ev' counts.
                newSponCount             : this.newEv ?: 0,
                cumSponCount             : this.totalEv ?: 0,
                newSeriousCount          : this.newSerious ?: 0,
                cumSeriousCount          : this.totalSerious ?: 0,
                rorValue                 : this.rorValue ?: "",
                newLit                   : this.newLit ?: "",
                totalLit                 : this.totalLit ?: "",
                sdr                      : this.sdr ?: "",
                smqNarrow                : this.smqNarrow ?: "",
                trend                    : trend ?: "",
                isSpecialPE              : isSpecialPE,
                dataSource               : Constants.DataSource.EVDAS.toUpperCase(),
                eb05                     : '-',
                eb95                     : '-',
                ebgm                     : '-',
                impEvents                : (isExport && this.impEvents)?replaceStopListLabel(this.impEvents).toUpperCase():this.impEvents ? this.impEvents.toUpperCase() : Constants.Commons.BLANK_STRING,
                ime                      : ptMap.isIme,
                dme                      : ptMap.isDme,
                ei                       : ptMap.isEi,
                isSm                     : ptMap.isSm,
                lastUpdated              : this.lastUpdated,
                format                   : this.format,
                evdasRuleFormat          : generateEvdasRuleFormat(),
                hlgt                     : this.hlgt ?: "",
                hlt                      : this.hlt ?: "",
                newEea                   : this.newEea ?: "",
                totEea                   : this.totEea ?: "",
                newHcp                   : this.newHcp ?: "",
                totHcp                   : this.totHcp ?: "",
                newMedErr                : this.newMedErr ?: "",
                totMedErr                : this.totMedErr ?: "",
                newObs                   : this.newObs ?: "",
                totObs                   : this.totObs ?: "",
                newRc                    : this.newRc ?: "",
                totRc                    : this.totRc ?: "",
                newPaed                  : this.newPaed ?: "",
                totPaed                  : this.totPaed ?: "",
                newGeria                 : this.newGeria ?: "",
                totGeria                 : this.totGeria ?: "",
                europeRor                : this.europeRor ?: "",
                northAmericaRor          : this.northAmericaRor ?: "",
                japanRor                 : this.japanRor ?: "",
                asiaRor                  : this.asiaRor ?: "",
                restRor                  : this.restRor ?: "",
                allRor                   : this.allRor ?: "",
                priorityAll              : this.getAttr('PRIORITY_ALL') != null ? this.getAttr('PRIORITY_ALL') : '-',
                newEvLink                : this.newEvLink ?: "",
                totalEvLink              : this.totalEvLink ?: "",
                ratioRorPaedVsOthers     : this.ratioRorPaedVsOthers ?: "",
                ratioRorGeriatrVsOthers  : this.ratioRorGeriatrVsOthers ?: "",
                changes                  : this.changes ?: "",
                sdrPaed                  : this.sdrPaed ?: "",
                sdrGeratr                : this.sdrGeratr ?: "",
                newSpont                 : this.newSpont ?: "",
                totSpont                 : this.totSpont ?: "",
                totSpontEurope           : this.totSpontEurope ?: "",
                totSpontNAmerica         : this.totSpontNAmerica ?: "",
                totSpontJapan            : this.totSpontJapan ?: "",
                totSpontAsia             : this.totSpontAsia ?: "",
                totSpontRest             : this.totSpontRest ?: "",
                ptCode                   : this.ptCode ?: "",
                flags                    : this.flags ?: "",
                asOfDate                 : DateUtil.toDateString(this.periodEndDate),
                executionDate            : DateUtil.toDateString(this.dateCreated),
                prrValue                 : '-',
                isValidationStateAchieved: this.getDispositionById(this.dispositionId)?.validatedConfirmed,
                isReviewed               : this.getDispositionById(this.dispositionId)?.reviewCompleted,
                assignedToUser           : this.getAssignedToMap(),
                assignedTo               : this.assignedToName(),
                comment                  : comment,
                commentId                : commentId,
                isAttachment             : isAttachment,
                listed                   : getEvdasListednessValue(this.listedness),
                justification            : this.justification,
                dispPerformedBy          : this.dispPerformedBy?.equalsIgnoreCase(Constants.Commons.SYSTEM)?Constants.Commons.SYSTEM:this.dispPerformedBy,
                dispLastChange           : this.dispLastChange? DateUtil.toDateStringWithTimeInAmFormat(this.dispLastChange, timeZone): Constants.Commons.BLANK_STRING,

        ]
        alertData.signalsAndTopics = validatedSignal
        alertData.padiatric = (int) ((this.id % 2 == 0) ? (this.totalEv) * 12 / 100 : (this.totalEv) * 25 / 100)
        alertData.geriatric = (int) ((this.id % 2 == 0) ? (this.totalEv) * 60 / 100 : (this.totalEv) * 70 / 100)
        alertData
    }

    def dueIn() {
        def theDueDate = new DateTime(dueDate)
        def now = DateTime.now().withTimeAtStartOfDay()
        def dur = new Duration(now, theDueDate)
        dur.getStandardDays()
    }

    def getProductName() {
        substance
    }

    String generateEvdasRuleFormat() {
        List ruleFormat = []
        if (this.totalFatal >= 1) {
            ruleFormat << [key: 'TOTAL_FATAL', color: 'red', isFontColor: true]
        }
        if ((this.newLit as int) >= 1) {
            ruleFormat << [key: 'NEW_LITERATURE', color: 'blue', isFontColor: true]
        }

        JsonOutput.toJson(ruleFormat)
    }

    def getAttr(attrName) {
        if (!attributesMap) {
            attributesMap = populateAttributesMap()
        }
        if (attributesMap) {
            attributesMap[attrName]
        } else {
            null
        }
    }

    def populateAttributesMap() {
        def jsonSlurper = new JsonSlurper()
        attributes ? jsonSlurper.parseText(attributes) ?: [:] : [:]
    }

    Map getAssignedToMap() {
        this.assignedToId ? getUserByUserId(this.assignedToId).toMap() : getGroupByGroupId(this.assignedToGroupId).toMap()
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

    String assignedToName() {
        this.assignedToId ? getUserByUserId(this.assignedToId).fullName : getGroupByGroupId(this.assignedToGroupId).name

    }

    @Override
    String toString() {
        "Archived ${name}"
    }

    def getInstanceIdentifierForAuditLog() {
        return "${this.executedAlertConfiguration.getInstanceIdentifierForAuditLog()}: ${this.productName} - ${this.pt}"
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
