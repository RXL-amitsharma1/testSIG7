package com.rxlogix.config

class AlertCase {
    static auditable = false

    String caseNumber
    String caseVersion
    Long caseId
    Integer tenantId
    boolean isDeleted

    static mapping = {
        datasource("pva")
        table("PVS_AUTO_ALERT_INCR_CASES")
        caseVersion column: "VERSION_NUM"
        isDeleted column: "DELETED_FLAG"
    }

}
