package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmRptWorkflowStates implements SelectableList {

    BigDecimal id
    String name

    static mapping = {
            table "VW_CRWS_STATE_NAME_DSP"
        datasource "pva"

        version false
        cache usage: "read-only"

        id column: "REPORT_STATE_ID", type: "big_decimal", generator: "assigned"
        name column: "STATE_NAME"
    }

    static constraints = {
        id(nullable:false, unique:true)
        name(maxSize: 30)
    }

    @Override
    def getSelectableList() {
        LmRptWorkflowStates.withTransaction {
            return LmRptWorkflowStates.findAll().unique().collect { it.name }.sort()
        }
    }
}
