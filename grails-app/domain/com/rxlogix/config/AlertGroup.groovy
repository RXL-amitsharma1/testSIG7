package com.rxlogix.config


class AlertGroup {
    Long id
    String name
    Boolean isGrouping = false

    static mapping = {
        table name: "ALERT_GROUP"
        id column: "ID"
        name column: "NAME"
        isGrouping column: "IS_GROUPING"
    }
    static constraints = {
        name unique: true
    }

}