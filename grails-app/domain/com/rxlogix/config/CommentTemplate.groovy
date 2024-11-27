package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.user.User
import com.rxlogix.util.MiscUtil

import static com.rxlogix.util.DateUtil.toDateString

class CommentTemplate implements Serializable {
    static auditable = true

    String name
    String comments


    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    static constraints = {
        name unique: true, nullable: false, validator: { value, object ->
            return MiscUtil.validator(value, "Template Name", Constants.SpecialCharacters.DEFAULT_CHARS  as String[])
        }
    }

    static mapping = {
        table("COMMENT_TEMPLATE")
        comments  type: 'text', sqlType: 'clob'
    }

    def toDto(){
        [
                'id'                :this.id,
                'name'              :this.name?.trim()?.replaceAll("\\s{2,}", " "),
                'comments'          :this.comments,
                'dateCreated'       :toDateString(this.dateCreated),
                'lastUpdated'       :toDateString(this.lastUpdated),
                'createdBy'         :User.findByUsername(this.createdBy)?.fullName,
                'modifiedBy'        :User.findByUsername(this.modifiedBy)?.fullName
        ]
    }

    def getInstanceIdentifierForAuditLog() {
        return name;
    }

    def getEntityValueForDeletion(){
        return "Name-${name}"
    }
}
