package com.rxlogix.config

import com.rxlogix.signal.ViewInstance
import com.rxlogix.user.User

class DefaultViewMapping {

    static auditable = false

    User user
    String alertType
    ViewInstance defaultViewInstance

    DefaultViewMapping(User user, String alertType, ViewInstance defaultViewInstance) {
        this.user = user
        this.alertType = alertType
        this.defaultViewInstance = defaultViewInstance
    }

    static constraints = {
        defaultViewInstance unique: ['alertType', 'user']
    }

    def getInstanceIdentifierForAuditLog() {
        return user.getValue() + "(" + alertType + ")"
    }
}