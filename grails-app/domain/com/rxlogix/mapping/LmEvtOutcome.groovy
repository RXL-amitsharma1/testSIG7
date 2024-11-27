package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmEvtOutcome implements SelectableList {

    BigDecimal id
    String evtOutcome

    static mapping = {
        datasource "pva"
        table "VW_LEO_EVT_OUTCOME_DSP"

        cache: "read-only"
        version false

        id column: "EVT_OUTCOME_ID", type: "big_decimal", generator: "assigned"
        evtOutcome column: "EVT_OUTCOME"
    }

    static constraints = {
        id(nullable:false, unique:true)
        evtOutcome(blank:false, maxSize:50)

    }

    @Override
    def getSelectableList() {
        LmEvtOutcome.withTransaction {
            return LmEvtOutcome.findAll().unique().collect { it.evtOutcome }.sort()
        }
    }
}
