package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmReporterType implements SelectableList {

    BigDecimal id
    String reporterType

    static mapping = {
        table "VW_LRET_REPORTER_TYPE_DSP"
        datasource "pva"

        version false
        cache usage: "read-only"

        id column: "RPTR_TYPE_ID", type: "big_decimal", generator: "assigned"
        reporterType column: "REPORTER_TYPE"
    }

    static constraints = {
        reporterType(maxSize: 30)
    }

    @Override
    public getSelectableList() {
        LmReporterType.withTransaction {
            return LmReporterType.findAll().unique().collect { it.reporterType }.sort()
        }
    }
}
