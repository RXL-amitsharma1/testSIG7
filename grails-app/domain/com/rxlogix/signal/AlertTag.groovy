package com.rxlogix.signal

import com.rxlogix.user.User

class AlertTag {
    static auditable = true

    String name
    User createdBy
    Date dateCreated

    static constraints = {
        name unique: true
    }

    static mapping = {
        table name: "ALERT_TAG"
        name column: "NAME"
        createdBy nullable: true
        dateCreated nullable: true
        sort "name"
    }

    def toDto(){
        [
            id          : this.id,
            name        : this.name,
            createdBy   : this.createdBy?.fullName ?: '',
            dateCreated : this.dateCreated
        ]
    }
}
