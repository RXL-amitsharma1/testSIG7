package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmFetalOutcome implements SelectableList {

    BigDecimal id
    String fetalOutcome

    static mapping = {
        datasource "pva"
        table "VW_LFO_FETAL_OUTCOME_DSP"

        cache: "read-only"
        version false

        id column: "FETAL_OUTCOME_ID", type: "big_decimal", generator: "assigned"
        fetalOutcome column: "FETAL_OUTCOME"
    }

    static constraints = {
        id(nullable:false, unique:true)
        fetalOutcome(blank:false, maxSize:35)

    }

    @Override
    def getSelectableList() {
        LmFetalOutcome.withTransaction {
            return LmFetalOutcome.findAll().unique().collect { it.fetalOutcome }.sort()
        }
    }
}
