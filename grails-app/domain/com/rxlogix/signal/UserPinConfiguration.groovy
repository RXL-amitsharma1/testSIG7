package com.rxlogix.signal

import com.rxlogix.user.User

class UserPinConfiguration {
    static auditable=false

    User user
    String fieldCode
    Boolean isPinned

    static belongsTo = [user : User]

    static constraints = {
        isPinned(nullable: true)
    }
    def getInstanceIdentifierForAuditLog() {
        return this.user?.fullName
    }

    @Override
    String toString(){
        return this.fieldCode
    }

}
