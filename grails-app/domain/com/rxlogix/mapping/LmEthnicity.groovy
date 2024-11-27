package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmEthnicity implements SelectableList {

    BigDecimal id
    String ethnicity

    static mapping = {
        datasource "pva"
        table "VW_LETH_ETHNICITY_DSP"

        cache: "read-only"
        version false

        id column: "ETHNICITY_ID", type: "big_decimal", generator: "assigned"
        ethnicity column: "ETHNICITY"
    }

    static constraints = {
        id(nullable:false, unique:true)
        ethnicity(blank:false, maxSize:40)

    }

    @Override
    def getSelectableList() {
        LmEthnicity.withTransaction {
            return LmEthnicity.findAll().unique().collect { it.ethnicity }.sort()
        }
    }
}
