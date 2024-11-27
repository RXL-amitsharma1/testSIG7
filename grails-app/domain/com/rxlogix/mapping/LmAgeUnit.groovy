package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmAgeUnit implements SelectableList {

    BigDecimal id
    String unit

    static mapping = {
        table "VW_LAU_AGE_UNIT_DSP"
        datasource "pva"

        version false
        cache usage: "read-only"

        id column: "AGE_UNIT_ID", type: "big_decimal", generator: "assigned"
        unit column: "AGE_UNIT"
    }

    static constraints = {
        unit(maxSize: 10)
    }

    @Override
    def getSelectableList() {
        LmAgeUnit.withTransaction {
            return LmAgeUnit.findAll().unique().collect { it.unit }.sort()
        }
    }
}
