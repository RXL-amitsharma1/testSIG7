package com.rxlogix.config

import com.rxlogix.BaseLiteratureAlert
import com.rxlogix.Constants
import com.rxlogix.signal.AlertTag
import com.rxlogix.signal.GlobalArticle
import com.rxlogix.signal.PvsAlertTag
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import grails.gorm.dirty.checking.DirtyCheck
import grails.plugins.orm.auditable.CollectionSnapshotAudit

@DirtyCheck
@CollectionSnapshotAudit
class LiteratureAlert extends BaseLiteratureAlert implements AlertUtil {

    static auditable = [ignoreEvents: ["onSave"],auditableProperties:['assignedTo','assignedToGroup','disposition','priority','justification','undoJustification']]

    def cacheService

    User assignedTo
    Priority priority
    Disposition disposition
    Group assignedToGroup

    //configurations
    LiteratureConfiguration litSearchConfig
    ExecutedLiteratureConfiguration exLitSearchConfig

    GlobalArticle globalIdentity
    String citation

    static attachmentable = true
    String undoJustification
    Map<String, Object> customAuditProperties
    static transients = ['undoJustification','customAuditProperties']

    static hasMany = [actions: Action, validatedSignals: ValidatedSignal, alertTags: AlertTag, pvsAlertTag: PvsAlertTag]

    static constraints = {
        globalIdentity nullable: true
        assignedTo nullable: true, validator: { value, obj ->
            def result = true
            if (!obj.assignedTo) {
                result = obj.assignedToGroup ? true : 'assignedTo.nullable'
            }
            return result
        }
        assignedToGroup(nullable: true)
        citation nullable: true
    }

    static mapping = {
        table name: 'LITERATURE_ALERT'
        def superMapping = BaseLiteratureAlert.mapping.clone()
        superMapping.delegate = delegate
        superMapping.call()
        validatedSignals joinTable: [name: "VALIDATED_LITERATURE_ALERTS", column: "VALIDATED_SIGNAL_ID", key: "LITERATURE_ALERT_ID"]
        alertTags joinTable: [name: "LITERATURE_ALERT_TAGS", column: "ALERT_TAG_ID", key: "LITERATURE_ALERT_ID"]
        actions joinTable: [name: "LIT_ALERT_ACTIONS", column: "ACTION_ID", key: "LITERATURE_ALERT_ID"]
        pvsAlertTag joinTable: [name: "LITERATURE_CASE_ALERT_TAGS", column: "PVS_ALERT_TAG_ID", key: "LITERATURE_ALERT_ID"]
        exLitSearchConfig index: 'IX_LITERATURE_EXEC_CONFIG_ID'
    }

    Map toDto(List<String> tagNameList = null, List validatedSignal = [], Boolean isExport = false,
              String comment = null,
              Boolean isAttachment = false, Boolean isUndoable=false, Long commentId = null) {
        Map literatureSearchAlertData = [
                id                       : this.id,
                alertName                : this.name?.trim()?.replaceAll("\\s{2,}", " "),
                priority                 : isExport ? this.getPriorityMap(this.priorityId)?.value : this.getPriorityMap(this.priorityId),
                title                    : this.articleTitle?.trim()?.replaceAll("\\s{2,}", " "),
                authors                  : this.articleAuthors,
                assignedTo               : isExport ? this.assignedToName() : this.assignedToMap(),
                publicationDate          : this.publicationDate,
                currentDisposition       : this.getDispositionById(this.dispositionId)?.displayName,
                disposition              : this.getDispositionById(this.dispositionId)?.displayName,
                currentDispositionId     : this.dispositionId,
                productName              : this.productSelection,
                eventName                : this.eventSelection,
                actionCount              : this.actionCount,
                isValidationStateAchieved: this.getDispositionById(this.dispositionId)?.validatedConfirmed,
                alertTags                : isExport ? tagNameList.join(', ') : tagNameList,
                alertConfigId            : this.litSearchConfigId,
                articleId                : this.articleId.toString(),
                articleAbstract          : this.articleAbstract,
                execConfigId             : this.exLitSearchConfigId,
                isReviewed               : this.getDispositionById(this.dispositionId)?.reviewCompleted,
                comment                  : comment,
                commentId                : commentId,
                isAttachment             : isAttachment,
                signal                   : isExport ? this.getSignalName(validatedSignal) : validatedSignal,
                dispPerformedBy          : this.dispPerformedBy,
                isUndoEnabled            : isUndoable?'true':'false',
                isDefaultState           : this.isDefaultState(),
                citation                 : this.citation?.trim(),
                publicationYear          : this.publicationDate?.substring(publicationDate.lastIndexOf('-') + 1)
        ]

        literatureSearchAlertData
    }

    def isDefaultState() {
        if (!isDispChanged && getDispositionById(this.dispositionId) == getDefaultDisp() && this.dispPerformedBy == null) {
            return 'true'
        }
        return 'false'
    }

    Disposition getDefaultDisp() {
        cacheService.getDefaultDisp(Constants.AlertType.LITERATURE)
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

    String assignedToName() {
        this.assignedToId ? getUserByUserId(this.assignedToId).fullName : getGroupByGroupId(this.assignedToGroupId).name

    }

    String getSignalName(List signals) {
        String signalName = ""
        signalName = signals.collect { it.name }?.join(",")
        return signalName
    }
    @Override
    String toString(){
        super.toString()
    }

    def getInstanceIdentifierForAuditLog() {
        return this.exLitSearchConfig.getInstanceIdentifierForAuditLog()+ ": " + this.articleId
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
