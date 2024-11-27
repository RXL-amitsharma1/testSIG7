package com.rxlogix.config

import com.rxlogix.enums.AuditLogCategoryEnum

class CognosReport {
    static auditable = false

    def auditLogService

    String name
    String url
    String description
    boolean isDeleted = false

    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    static mapping = {
        table name: "COGNOS_REPORT"

        name column: "NAME"
        url column: "URL"
        description column: "DESCRIPTION"
        isDeleted column: "IS_DELETED"
    }

    static constraints = {
        description (nullable: true, maxSize: 1000)
        url (maxSize: 1000, url: true)
        createdBy(nullable: false, maxSize: 100)
        modifiedBy(nullable: false, maxSize: 100)
    }

    def detectChangesForAuditLog(theInstance, Map params, AuditLogCategoryEnum auditLogCategoryEnum) {
        List changesMade = auditLogService.detectChangesMade(theInstance, auditLogCategoryEnum)
        changesMade.flatten()
    }
}
