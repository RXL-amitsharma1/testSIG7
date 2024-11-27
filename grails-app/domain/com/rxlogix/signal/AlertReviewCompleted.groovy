package com.rxlogix.signal

class AlertReviewCompleted {

    String caseNumber
    String alertType
    Long configId
    Long exeConfigId

    static constraints = {
        caseNumber nullable: true
        alertType nullable: true
    }
    static mapping={
        table("ALERT_REVIEW_COMPLETED")
        caseNumber column: "CASE_NUMBER"
        alertType column: "ALERT_TYPE"
        configId column: "CONFIG_ID"
        exeConfigId column: "EXE_CONFIG_ID"
    }
}
