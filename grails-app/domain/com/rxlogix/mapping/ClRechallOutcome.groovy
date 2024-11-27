package com.rxlogix.mapping

import com.rxlogix.SelectableList

class ClRechallOutcome implements SelectableList {

    BigDecimal id
    String description

    static mapping = {
        table "VW_CREO_OUTCOME_DSP"
        datasource "pva"

        version false
        cache usage: "read-only"

        id column: "ID", type: "big_decimal", generator: "assigned"
        description column: "OUTCOME"
    }

    static constraints = {
        id(nullable:false, unique:true)
        description(maxSize: 16)
    }

    @Override
    def getSelectableList() {
        ClRechallOutcome.withTransaction {
            return ClRechallOutcome.findAll().unique().collect { it.description }.sort()
        }
    }
}
