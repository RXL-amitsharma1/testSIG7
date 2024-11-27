package com.rxlogix.signal

class AlertPreExecutionCheckHistory {

    Boolean isPvrCheckFlagUpdated = false
    Boolean newPvrCheckValue

    Boolean isVersionAsOfCheckUpdated = false
    Boolean newVersionAsOfCheckValue

    Boolean isEtlFailureCheckUpdated = false
    Boolean newEtlFailureCheckValue

    Boolean isEtlInProgressCheckUpdated = false
    Boolean newEtlInProgressCheckValue

    String createdBy
    Date dateCreated
    Date lastUpdated
    String modifiedBy

    static belongsTo = [alertPreExecutionCheck: AlertPreExecutionCheck]

    static mapping = {
        table name: "ALERT_PRE_CHECK_HISTORY"
        isPvrCheckFlagUpdated column: "pvr_check_updated"
        newPvrCheckValue column: "new_pvr_check_val"
        isVersionAsOfCheckUpdated column: "ver_asof_check_updated"
        newVersionAsOfCheckValue column: "new_ver_asof_check_val"
        isEtlFailureCheckUpdated column: "etl_fail_check_updated"
        newEtlFailureCheckValue column: "new_etl_fail_check_val"
        isEtlInProgressCheckUpdated column: "etl_in_prog_check_updated"
        newEtlInProgressCheckValue column: "new_etl_in_prog_check"
    }

    static constraints = {
        newPvrCheckValue nullable: true
        newVersionAsOfCheckValue nullable: true
        newEtlFailureCheckValue nullable: true
        newEtlInProgressCheckValue nullable: true
    }

}
