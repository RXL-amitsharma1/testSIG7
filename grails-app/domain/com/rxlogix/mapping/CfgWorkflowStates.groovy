package com.rxlogix.mapping

import com.rxlogix.SelectableList

class CfgWorkflowStates implements SelectableList {

    BigDecimal id
    String name

    static mapping = {
        table "VW_CWS_STATE_NAME_DSP"
        datasource "pva"

        version false
        cache usage: "read-only"

        id column: "STATE_ID", type: "big_decimal", generator: "assigned"
        name column: "STATE_NAME"
    }

    static constraints = {
        id(nullable:false, unique:true)
        name(maxSize: 30)
    }

    @Override
    def getSelectableList() {
        CfgWorkflowStates.withTransaction {
            return CfgWorkflowStates.findAll().unique().collect { it.name }.sort()
        }
    }
}
