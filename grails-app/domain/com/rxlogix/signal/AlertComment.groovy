package com.rxlogix.signal

import com.rxlogix.Constants
import com.rxlogix.config.CommentTemplate
import com.rxlogix.config.Configuration
import com.rxlogix.config.EvdasConfiguration
import com.rxlogix.PVCachable
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil

import static com.rxlogix.util.DateUtil.toDateTimeString

class AlertComment implements GroovyInterceptable, Serializable {
    static auditable = [auditableProperties:['comments','isDeleted']]

    String comments
    String productName
    String eventName
    String caseNumber
    String productFamily
    String alertType

    //Common db table fields
    String createdBy
    Date dateCreated
    Date lastUpdated
    String modifiedBy
    Integer ptCode
    BigInteger productId
    String articleId

    Long configId


    Long exConfigId
    Long caseId
    Integer versionNum
    Boolean syncFlag = false
    Integer followUpNum
    String dataSource
    String alertName
    String commentTemplateId

    static mapping = {
        table("ALERT_COMMENT")
        id generator:'sequence', params:[sequence:'alert_comment_sequence']
        comments column: "COMMENTS", type: "text", sqlType: "clob"
    }
    static constraints = {
        productName nullable: true
        eventName nullable: true
        caseNumber nullable: true
        productFamily nullable: true
        ptCode nullable: true
        productId nullable: true
        configId nullable: true
        articleId nullable: true
        exConfigId nullable: true
        caseId nullable: true
        versionNum nullable: true
        syncFlag nullable: true
        followUpNum nullable: true
        dataSource nullable: true
        alertName nullable: true
        commentTemplateId nullable: true
    }

    def toDto(currentUser, timeZone = "UTC") {
        [
            id                 : this.id,
            configId           : this.configId,
            comments           : this.comments,
            productName        : this.productName,
            eventName          : this.eventName,
            caseNumber         : this.caseNumber,
            productFamily      : this.productFamily,
            alertType          : this.alertType,
            dateCreated        : toDateTimeString(this.dateCreated),
            dateUpdated        : DateUtil.toDateStringWithTime(this.lastUpdated, timeZone),
            createdBy          : User.findByUsername(this.createdBy)?.fullName ?: "-",
            editable           : currentUser == this.createdBy,
            modifiedBy         : User.findByUsername(this.modifiedBy)?.fullName ?: "-",
            exConfigId         : this.exConfigId,
            caseId             : this.caseId,
            versionNum         : this.versionNum,
            syncFlag           : this.syncFlag,
            followUpNum        : this.followUpNum,
            dataSource         : this.dataSource,
            commentTemplateId  : this.commentTemplateId,
            commentTemplateName : CommentTemplate.findById(this.commentTemplateId as Long)?.name ?: null
        ]
    }


    Long getCacheKey() {
        Long key
        if (productName && eventName)
            key = productName.hashCode() + eventName.hashCode()
        else if (productName) {
            key = productName.hashCode()
        } else if (eventName) {
            key = eventName.hashCode()
        } else {
            key = Long.MAX_VALUE
        }
        key
    }

    @Override
    String toString() {
        "$comments"
    }

    def getInstanceIdentifierForAuditLog() {
        def classMap = [
                "Aggregate Case Alert"   : "com.rxlogix.config.ExecutedConfiguration",
                "Literature Search Alert": "com.rxlogix.config.ExecutedLiteratureConfiguration",
                "EVDAS Alert"            : "com.rxlogix.config.ExecutedEvdasConfiguration"]
        String entityValue = Class.forName(classMap.get(this.alertType)).get(this.exConfigId).getInstanceIdentifierForAuditLog() ?: this.caseNumber
        if (this.alertType in [Constants.AlertConfigType.AGGREGATE_CASE_ALERT,Constants.AlertConfigType.EVDAS_ALERT]) {
            entityValue += ": (${this.productName}-${this.eventName})"
        }else {
            entityValue += ": ${this.articleId}"
        }
        return entityValue
    }

    def getEntityValueForDeletion(){
        return getInstanceIdentifierForAuditLog()+", Comment-${comments}"
    }

    def getModuleNameForMultiUseDomains() {
        String moduleName = ""
        def classMap = [
                "Aggregate Case Alert"   : "com.rxlogix.config.ExecutedConfiguration",
                "Literature Search Alert": "com.rxlogix.config.ExecutedLiteratureConfiguration",
                "EVDAS Alert"            : "com.rxlogix.config.ExecutedEvdasConfiguration"]
        moduleName = Constants.AuditLog.typeToEntityMap.get(alertType) ? Constants.AuditLog.typeToEntityMap.get(alertType) : Constants.CaseDetailFields.COMMENTS
        def isLatest = Class.forName(classMap.get(this.alertType)).get(this.exConfigId).getIsLatest()
        if (isLatest == true) {
            return moduleName + ": Comment"
        } else {
            return moduleName + ": Archived Alert: Comment"
        }
    }

}
