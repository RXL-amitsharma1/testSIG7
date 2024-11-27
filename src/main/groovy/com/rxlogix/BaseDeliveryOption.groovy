package com.rxlogix

import com.rxlogix.enums.ReportFormat
import com.rxlogix.user.User
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
abstract class BaseDeliveryOption {
    Long id
    List<User> sharedWith = []
    List emailToUsers = []
    List<ReportFormat> attachmentFormats = []

    // @TODO emailToUsers can be stored as a JSON array, there is no real need to save them in the DB
    static hasMany = [sharedWith: User, emailToUsers: String, attachmentFormats: ReportFormat]

    @SuppressWarnings("GroovyAssignabilityCheck")
    static constraints = {
        sharedWith(nullable: true)
        emailToUsers(nullable: true)
        attachmentFormats(nullable: true)
    }
}
