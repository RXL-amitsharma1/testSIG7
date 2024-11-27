package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmSeverity implements SelectableList {

    BigDecimal id
    String severity

    static mapping = {
        datasource "pva"
        table "VW_LS_SEVERITY_DSP"

        cache: "read-only"
        version false

        id column: "SEVERITY_ID", type: "big_decimal", generator: "assigned"
        severity column: "SEVERITY"
    }

    static constraints = {
        id(nullable:false, unique:true)
        severity(blank:false, maxSize:30)

    }

    @Override
    def getSelectableList() {
        LmSeverity.withTransaction {
            return LmSeverity.findAll().unique().collect { it.severity }.sort()
        }
    }
}
