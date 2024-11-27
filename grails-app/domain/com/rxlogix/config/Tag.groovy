package com.rxlogix.config

import com.rxlogix.user.User

class Tag implements Serializable {
    static auditable = true
    String name
    User createdBy
    Date dateCreated

    static constraints = {
        name(unique: true, blank: false)
        createdBy nullable: true
        dateCreated nullable: true
    }

    static mapping = {
        table name: "TAG"
        name column: "NAME"
    }

    def getInstanceIdentifierForAuditLog() {
        return name
    }

    def toDto(){
        [
                id          : this.id,
                name        : this.name?.trim()?.replaceAll("\\s{2,}", " "),
                createdBy   : this.createdBy?.fullName ?: '',
                dateCreated : this.dateCreated
        ]
    }
}