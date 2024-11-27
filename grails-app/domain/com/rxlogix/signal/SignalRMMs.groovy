package com.rxlogix.signal

import com.rxlogix.Constants
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.attachments.AttachmentLink
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.DateUtil
import grails.plugins.orm.auditable.ChildModuleAudit

import java.text.SimpleDateFormat

@ChildModuleAudit(parentClassName = ['attachment','signalRMMs'])
class SignalRMMs {
    static transients = ['isSystemUser'] //isSystemUser is used for mark the audit entry on system name if required
    def validatedSignalService

    Boolean isSystemUser=false
    Long signalId
    String type
    String description
    User assignedTo
    Group assignedToGroup
    String status
    Date dueDate
    String communicationType
    SignalEmailLog signalEmailLog
    Date dateCreated
    String country
    String emailAddress
    Date emailSent
    String criteria
    Long communicationRmmId
    Boolean isDeleted = false

    static auditable = [ignore:['signalEmailLog','communicationType','signalId','communicationRmmId','emailSent','criteria']]
    static attachmentable = true
    static mapping = {
        table("SIGNAL_RMMS")
        autoTimestamp false
    }

    static constraints = {
        description maxSize: 8000
        assignedTo nullable: true, blank: true
        type nullable: true, blank: true
        assignedToGroup nullable: true, blank: true
        description nullable: true
        signalEmailLog nullable: true
        country nullable: true
        emailAddress email: true, nullable: true
        emailSent nullable: true
        criteria nullable: true, maxSize: 8000
        status nullable: true
        dueDate nullable: true
        dateCreated nullable: true
        communicationRmmId nullable: true
        isDeleted nullable: true
        signalId nullable: true
    }
    @Override
    String toString() {
        "${this.getClass().getSimpleName()} : ${this.id}"
    }

    def getInstanceIdentifierForAuditLog() {
        if (signalId != null) {
            return ValidatedSignal.get(signalId as Long).getInstanceIdentifierForAuditLog() + ": " + this.type
        } else {
            return "${this.getClass().getSimpleName()}: ${this.id}"
        }
    }

    def getModuleNameForMultiUseDomains() {
        String rmmType = ""
        if (this.communicationType == "rmmType") {
            rmmType = "RMMs"
        } else {
            rmmType = "Communication"
        }
        return Constants.AlertConfigType.SIGNAL + ": " + rmmType
    }

    def getEntityValueForDeletion() {
        String resp = ""
        String signalName = ValidatedSignal.get(signalId as Long).getInstanceIdentifierForAuditLog()
        String attachmentNames = this.attachments.collect { it.inputName != 'attachments' ? it.inputName : it.name }?.join(',')
        String referenceLinks = AttachmentLink.findByReferenceClassAndReferenceId("com.rxlogix.signal.SignalRMMs", this.id)?.attachments?.collect{it.inputName}?.join(',')
        if (this.assignedTo) {
            resp = this.assignedTo
        } else {
            resp = this.assignedToGroup
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATEPICKER_FORMAT)
        if (this.country != null && this.country != "null") {
            return "${signalName}:Type- ${this.type},Country- ${validatedSignalService.country(this.country)},Description- ${this.description},Attachments-${attachmentNames?:referenceLinks},Resp-${resp},Status:${this.status},Due Date:${this.dueDate?sdf.format(this.dueDate):''},Date Created: ${AuditLogConfigUtil.getCreatedDateInGmt(dateCreated)}".replaceAll("null", "")
        } else {
            return "${signalName}:Type- ${this.type},Description- ${this.description},Attachments-${attachmentNames?:referenceLinks},Resp-${resp},Status:${this.status},Due Date:${this.dueDate?sdf.format(this.dueDate):''},Date Created :${AuditLogConfigUtil.getCreatedDateInGmt(dateCreated)}".replaceAll("null", "")
        }
    }
}

