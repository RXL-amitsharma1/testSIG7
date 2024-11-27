package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.user.User
import com.rxlogix.util.MiscUtil
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.JSONAudit

import static com.rxlogix.util.DateUtil.toDateString

class ActionTemplate implements Serializable {
    static auditable = [ignoreEvents: ["onDelete"]]

    //Action template properties.
    @AuditEntityIdentifier
    String name
    String description
    @JSONAudit
    String actionJson

    //Standard fields.
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy
    String guestAttendeeEmail

    static constraints = {
        name unique: true, nullable: false, validator: { value, object ->
            return MiscUtil.validator(value, "Name", Constants.SpecialCharacters.DEFAULT_CHARS as String[])
        }
        description nullable: true, validator: { value, object ->
            return MiscUtil.validator(value, "Description", Constants.SpecialCharacters.TEXTAREA_CHARS as String[])
        }
        guestAttendeeEmail nullable: true, email: true
    }

    static mapping = {
        table("ACTION_TEMPLATE")
        actionJson type: 'text', sqlType: 'clob'
    }

    def toDto(timeZone) {
        [
                'id'                : this.id,
                'name'              : this.name?.trim()?.replaceAll("\\s{2,}", " "),
                'description'       : this.description,
                'actionProperties'  : this.actionJson,
                'lastUpdated'       : toDateString(this.lastUpdated, timeZone),
                'modifiedBy'        : User.findByUsername(this.modifiedBy)?.fullName,
                'guestAttendeeEmail': this.guestAttendeeEmail
        ]
    }

    @Override
    String toString() {
        this.name
    }

    def getEntityValueForDeletion(){
        return "Name-${name}"
    }
}
