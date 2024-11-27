package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmEvtIntensity implements SelectableList {

    BigDecimal id
    String evtIntensity

    static mapping = {
        datasource "pva"
        table "VW_LEI_EVT_INTENSITY_DSP"

        cache: "read-only"
        version false

        id column: "EVT_INTENSITY_ID", type: "big_decimal", generator: "assigned"
        evtIntensity column: "EVT_INTENSITY"
    }

    static constraints = {
        id(nullable:false, unique:true)
        evtIntensity(blank:false, maxSize:30)

    }

    @Override
    def getSelectableList() {
        LmEvtIntensity.withTransaction {
            return LmEvtIntensity.findAll().unique().collect { it.evtIntensity }.sort()
        }
    }
}
