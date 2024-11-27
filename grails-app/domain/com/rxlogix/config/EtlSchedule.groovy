package com.rxlogix.config

class EtlSchedule {

    static auditable = true

    String scheduleName
    String startDateTime
    String repeatInterval
    boolean isDisabled = false
    boolean isInitial = true

    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    static mapping = {
        table name: "ETL_SCHEDULE"
        scheduleName column: "SCHEDULE_NAME"
        startDateTime column: "START_DATETIME"
        repeatInterval column: "REPEAT_INTERVAL"
        isDisabled column: "DISABLED"
        isInitial column: "IS_INITIAL"

    }

    static constraints = {
        scheduleName blank: false, unique: true, maxSize: 20
        repeatInterval nullable: false
        startDateTime nullable: false
    }

    def getInstanceIdentifierForAuditLog() {
        return scheduleName
    }
}
