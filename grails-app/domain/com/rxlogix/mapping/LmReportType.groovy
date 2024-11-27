package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmReportType implements SelectableList {

    String id
    String type

    static mapping = {
        table "VW_LRTY_REPORT_TYPE"
        datasource "pva"

        version false
        cache usage: "read-only"

        id column: "RPT_TYPE_ID", generator: "assigned"
        type column: "REPORT_TYPE"
    }

    static constraints = {
        type(maxSize: 30)
    }

    @Override
    public getSelectableList() {
        LmReportType.withTransaction {
            return LmReportType.findAll().unique().collect { it.type }.sort()
        }
    }
}
