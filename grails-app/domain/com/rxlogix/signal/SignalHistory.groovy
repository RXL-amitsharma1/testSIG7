package com.rxlogix.signal

import com.rxlogix.Constants
import com.rxlogix.config.Disposition
import com.rxlogix.config.PVSState
import com.rxlogix.config.Priority
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil

class SignalHistory {

    //Workflow management related params.
    Disposition disposition
    User assignedTo
    Priority priority
    Group group

    //Common db table fields
    String createdBy
    Date dateCreated
    Date lastUpdated
    String modifiedBy

    String justification
    boolean isLatest
    String change
    Date dueDate
    Boolean isUndo = false
    Date createdTimestamp

    ValidatedSignal validatedSignal

    static constraints = {
        justification nullable: true, maxSize: 8000
        createdBy nullable: true
        dateCreated nullable: true
        lastUpdated nullable: true
        modifiedBy nullable: true
        group nullable: true
        assignedTo nullable: true
        dueDate nullable: true
        isUndo nullable: true
        createdTimestamp nullable: true
    }

    def toDto() {
        Date timeStamp = null
        if (Objects.nonNull(this.createdTimestamp)) {
            timeStamp = this.createdTimestamp
        } else {
            timeStamp = this.dateCreated
        }
        [
            id : this.id,
            disposition : this.disposition.displayName,
            priority : this.priority.displayName,
            assignedTo : this.assignedTo.fullName,
            createdBy : this.createdBy,
            updatedBy : this.modifiedBy == Constants.Commons.SYSTEM ? Constants.Commons.SYSTEM :User.findByUsername(this.modifiedBy).fullName,
            timestamp : DateUtil.toDateTimeString(timeStamp),
            justification : this.justification,
            group : this.group ? this.group.name : '-',
        ]
    }
}
