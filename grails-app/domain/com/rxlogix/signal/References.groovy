package com.rxlogix.signal

import com.rxlogix.attachments.Attachment
import com.rxlogix.attachments.AttachmentReference
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.AuditLogConfigUtil
import grails.plugins.orm.auditable.ChildModuleAudit
import grails.plugins.orm.auditable.CollectionSnapshotAudit

@ChildModuleAudit(parentClassName = ['attachmentReference'])
@CollectionSnapshotAudit
class References {
    static auditable = ['ignore':['favIconUrl','dateCreated','lastUpdated','createdBy','modifiedBy','attachment']]
    Long id
    AttachmentReference attachment
    String description
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy
    Set<User> shareWithUser=[]
    List<User> deletedByUser=[]
    Set<Group> shareWithGroup=[]
    String favIconUrl
    boolean isDeleted = false
    static hasMany = [ shareWithUser: User, shareWithGroup: Group]
    static constraints = {
        description nullable: true, maxSize: 8000
        favIconUrl nullable: true
    }

    static mapping = {
        table("REFERENCE_DETAILS")
        version false
        isDeleted column: "IS_DELETED"
        description sqlType: "varchar2(8000 CHAR)"
        shareWithUser joinTable: [name:"SHARE_WITH_USER_REFERENCES", column:"SHARE_WITH_USER_ID", key:"REFERENCE_ID"]
        shareWithGroup joinTable: [name:"SHARE_WITH_GROUP_REFERENCES", column:"SHARE_WITH_GROUP_ID", key:"REFERENCE_ID"]
        deletedByUser joinTable: [name:"REFERENCE_DELETED_FOR_USER", column:"DELETED_USER_ID", key:"REFERENCE_ID"]
    }
    public Set<User> getShareWithUsers() {
        Set<User> users = []
        if (this.shareWithUser) {
            users.addAll(this.shareWithUser)
        }
        return users
    }

    public Set<Group> getShareWithGroups() {
        Set<Group> userGroups = []
        if (this.shareWithGroup) {
            userGroups.addAll(this.shareWithGroup)
        }
        return userGroups
    }

    @Override
    boolean equals(def obj) {
        if (obj instanceof References) {
            this?.id == (obj as User)?.id
        } else {
            false
        }
    }

    @Override
    int hashCode() {
        return (this.id)?this.id.hashCode():0
    }

    def getInstanceIdentifierForAuditLog() {
        return this?.attachment?.inputName ?: this?.description
    }

    def getEntityValueForDeletion(){
        if(attachment.referenceLink != null){
            return "Reference Name-${attachment.inputName}, URL-${attachment.referenceLink}, Owner-${createdBy}, Created Date-${AuditLogConfigUtil.getCreatedDateInGmt(dateCreated)}"
        }else{
            return "Attachment Name-${attachment.inputName}, URL-, Owner-${createdBy}, Created Date-${AuditLogConfigUtil.getCreatedDateInGmt(dateCreated)}"
        }
    }
}
