package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmDoseFrequency implements SelectableList {

    BigDecimal id
    String freq

    static mapping = {
        datasource "pva"
        table "VW_LDF_FREQ_DSP"

        cache: "read-only"
        version false

        id column: "FREQ_ID", type: "big_decimal", generator: "assigned"
        freq column: "FREQ"
    }

    static constraints = {
        id(nullable:false, unique:true)
        freq(blank:false, maxSize:15)

    }

    @Override
    def getSelectableList() {
        LmDoseFrequency.withTransaction {
            return LmDoseFrequency.findAll().unique().collect { it.freq }.sort()
        }
    }
}
