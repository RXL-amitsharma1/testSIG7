package com.rxlogix.config

import com.rxlogix.user.User

class SharedWith implements Serializable {
    static auditable = true
    User user
    ReportResultStatus status = ReportResultStatus.NEW
    boolean isDeleted = false

    static belongsTo = [executedConfiguration: ExecutedConfiguration]
    static constraints = {
    }

    static mapping = {
        table name: "SHARED_WITH"

        user column: "RPT_USER_ID"
        status column: "STATUS"
        isDeleted column: "IS_DELETED"
        executedConfiguration column: "EX_RCONFIG_ID"
    }
}
