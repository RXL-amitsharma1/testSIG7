package com.rxlogix.config

import com.rxlogix.BaseLiteratureAlert
import com.rxlogix.config.Action
import com.rxlogix.config.Disposition
import com.rxlogix.config.Priority
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
class ArchivedEmbaseLiteratureAlert extends BaseLiteratureAlert implements AlertUtil {

    static auditable = [ignoreEvents: ["onSave"],auditableProperties:['assignedTo','assignedToGroup','disposition','priority','justification','undoJustification']]

    String affiliationOrganization
    String sourceTitle
    String issn
    String sourceLink
    String publicationYear
    String chemicalName
    String casRegistryNumber
    String manufacturer
    String tradeName
    String tradeItemManufacturer
    String externalSource
    String publisherEmail
    String enzymeCommissionNumber
    String digitalObjectIdentifier

    String explosionGroupsJson
    String descriptorGroupsJson

    def cacheService
    String undoJustification

    User assignedTo
    Priority priority
    Disposition disposition
    Group assignedToGroup

    //configurations
    LiteratureConfiguration litSearchConfig
    ExecutedLiteratureConfiguration exLitSearchConfig

    GlobalArticle globalIdentity

    Map<String, Object> customAuditProperties

    static transients = ['undoJustification','customAuditProperties']

    static hasMany = [actions: Action, validatedSignals: ValidatedSignal, alertTags: AlertTag, pvsAlertTag: PvsAlertTag]

    static attachmentable = true

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

        affiliationOrganization nullable: true
        sourceTitle nullable: true
        issn nullable: true
        sourceLink nullable: true
        publicationYear nullable: true
        chemicalName nullable: true
        casRegistryNumber nullable: true
        manufacturer nullable: true
        tradeName nullable: true
        tradeItemManufacturer nullable: true
        externalSource nullable: true
        publisherEmail nullable: true
        enzymeCommissionNumber nullable: true
        digitalObjectIdentifier nullable: true
        explosionGroupsJson nullable: true
        descriptorGroupsJson nullable: true
    }

    static mapping = {
        table name: 'ARCHIVED_EMBASE_LITERATURE_ALERT'
        def superMapping = BaseLiteratureAlert.mapping.clone()
        superMapping.delegate = delegate
        superMapping.call()

        validatedSignals joinTable: [name: "VALIDATED_ARCHIVED_EMBASE_LIT_ALERTS", column: "VALIDATED_SIGNAL_ID", key: "ARCHIVED_EMBASE_LIT_ALERT_ID"]
        actions joinTable: [name: "ARCHIVED_EMBASE_LIT_ALERT_ACTIONS", column: "ACTION_ID", key: "ARCHIVED_EMBASE_LIT_ALERT_ID"]
        pvsAlertTag joinTable: [name: "ARCHIVED_EMBASE_LIT_CASE_ALERT_TAGS", column: "PVS_ALERT_TAG_ID", key: "ARCHIVED_EMBASE_LIT_ALERT_ID"]
    }

    Map toDto(List<String> tagNameList = null, List validatedSignal = [], Boolean isExport = false, String comment = null,
              Boolean isAttachment = false, Boolean isUndoable = false, Long commentId = null) {
        Map embaseLiteratureAlertData = [
                id                       : this.id,
                alertName                : this.name?.trim()?.replaceAll("\\s{2,}", " "),
                priority                 : isExport ? this.getPriorityMap(this.priority.id)?.value : this.getPriorityMap(this.priority.id),
                title                    : this.articleTitle,
                authors                  : this.articleAuthors,
                assignedTo               : isExport ? this.assignedToName() : this.assignedToMap(),
                publicationDate          : this.publicationDate,
                disposition              : this.getDispositionById(this.disposition.id)?.displayName,
                currentDisposition       : this.getDispositionById(this.disposition.id)?.displayName,
                currentDispositionId     : this.disposition.id,
                productName              : this.productSelection,
                eventName                : this.eventSelection,
                actionCount              : this.actionCount,
                isValidationStateAchieved: this.getDispositionById(this.disposition.id)?.validatedConfirmed,
                alertTags                : isExport ? tagNameList.join(', ') : tagNameList,
                alertConfigId            : this.litSearchConfig.id,
                articleId                : this.articleId?.toString(),
                articleAbstract          : this.articleAbstract,
                execConfigId             : this.exLitSearchConfig.id,
                isReviewed               : this.getDispositionById(this.disposition.id)?.reviewCompleted,
                comment                  : comment,
                commentId                : commentId,
                isAttachment             : isAttachment,
                dispPerformedBy          : this.dispPerformedBy,
                signal                   : isExport ? this.getSignalName(validatedSignal) : validatedSignal,
                affiliationOrganization  : this.affiliationOrganization,
                sourceTitle              : this.sourceTitle,
                issn                     : this.issn,
                sourceLink               : this.sourceLink,
                publicationYear          : this.publicationYear,
                chemicalName             : this.chemicalName,
                casRegistryNumber        : this.casRegistryNumber,
                manufacturer             : this.manufacturer,
                tradeName                : this.tradeName,
                tradeItemManufacturer    : this.tradeItemManufacturer,
                externalSource           : this.externalSource,
                publisherEmail           : this.publisherEmail,
                enzymeCommissionNumber   : this.enzymeCommissionNumber,
                digitalObjectIdentifier  : this.digitalObjectIdentifier,
                explosionGroups          : this.explosionGroupsJson,
                descriptorGroups         : this.descriptorGroupsJson
        ]

        return embaseLiteratureAlertData
    }

    // move to parent
    Map getPriorityMap(Long priorityId) {
        Priority priority = cacheService.getPriorityByValue(priorityId)
        [value: priority?.value, iconClass: priority?.iconClass]
    }

    Disposition getDispositionById(Long dispositionId) {
        cacheService.getDispositionByValue(dispositionId)
    }

    String assignedToName() {
        this.assignedToId ? getUserByUserId(this.assignedToId).fullName : getGroupByGroupId(this.assignedToGroupId).name
    }

    Map assignedToMap() {
        this.assignedTo?.id ? getUserByUserId(this.assignedTo.id).toMap() : getGroupByGroupId(this.assignedToGroup.id).toMap()
    }

    User getUserByUserId(Long userId) {
        cacheService.getUserByUserId(userId)
    }

    Group getGroupByGroupId(Long groupId) {
        cacheService.getGroupByGroupId(groupId)

    }

    def getInstanceIdentifierForAuditLog() {
        return this.exLitSearchConfig.getInstanceIdentifierForAuditLog()+ ": " + this.articleId
    }

    String getSignalName(List signals) {
        String signalName = ""
        signalName = signals.collect { it.name }?.join(",")
        return signalName
    }
}
