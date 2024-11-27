package com.rxlogix.mapping

import com.rxlogix.SelectableList

class ClAutopsyResults implements SelectableList {

    BigDecimal id
    String description

    static mapping = {
        table "VW_CAUR_AUTOPSY_RESULTS_DSP"
        datasource "pva"

        version false
        cache usage: "read-only"

        id column: "ID", type: "big_decimal", generator: "assigned"
        description column: "AUTOPSY_RESULTS"
    }

    static constraints = {
        id(nullable:false, unique:true)
        description(maxSize: 35)
    }

    @Override
    def getSelectableList() {
        ClAutopsyResults.withTransaction {
            return ClAutopsyResults.findAll().unique().collect { it.description }.sort()
        }
    }
}
