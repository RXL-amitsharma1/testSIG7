package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmDoseUnits implements SelectableList {

    BigDecimal id
    String unit

    static mapping = {
        datasource "pva"
        table "VW_LDU_UNIT_DSP"

        cache: "read-only"
        version false

        id column: "UNIT_ID", type: "big_decimal", generator: "assigned"
        unit column: "UNIT"
    }

    static constraints = {
        id(nullable:false, unique:true)
        unit(blank:false, maxSize:25)

    }

    @Override
    def getSelectableList() {
        LmDoseUnits.withTransaction {
            return LmDoseUnits.findAll().unique().collect { it.unit }.sort()            
        }
    }
}
