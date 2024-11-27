package com.rxlogix.signal

import com.rxlogix.Constants
import com.rxlogix.attachments.Attachment
import grails.plugins.orm.auditable.ChildModuleAudit

@ChildModuleAudit(parentClassName = ['attachment', 'signalRMMs'])
class AttachmentDescription {
    static auditable = [ignoreEvents:['onDelete'],ignore:['id','dateCreated','createdBy','attachment']]
    static transients = ['skipAudit']
    Long id
    Attachment attachment
    String description
    Date dateCreated
    String createdBy
    Boolean skipAudit = false

    static constraints = {
        description nullable: true
    }

    static mapping = {
        table("ATTACHMENT_DESCRIPTION")
        version false
        description sqlType: "varchar2(8000 CHAR)"
    }

    def getInstanceIdentifierForAuditLog() {
        return this.attachment?.getInstanceIdentifierForAuditLog() ?: description
    }

    def getModuleNameForMultiUseDomains() {
        return this.attachment.getModuleNameForMultiUseDomains()
    }

    def getEntityValueForDeletion(){
        return this.attachment.getEntityValueForDeletion()
    }

    def getCustomIgnoreProperties(){
        List ignoreList=[]
        if(this?.attachment?.lnk?.referenceClass == "com.rxlogix.signal.SignalRMMs"){
            ignoreList= ['id','dateCreated','createdBy','attachment','description']
        }else{
            ignoreList=['id','dateCreated','createdBy','attachment']
        }
        return ignoreList
    }
}
