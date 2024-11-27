package com.rxlogix.config

import com.rxlogix.enums.EtlStatusEnum

class EtlStatus {

    static auditable = false

    EtlStatusEnum status
    Date lastRunDateTime

    static constraints = {
        lastRunDateTime nullable: true
    }

    static mapping = {
        datasource "pva"
        cache: "read-only"
        table name: "V_PVR_ETL_STATUS"
        id column: "ID", generator: "assigned"
        version column: "VERSION"
        status column: "STATUS"
        lastRunDateTime column: "FINISH_DATETIME"
    }
}
