package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmDevPhase implements SelectableList {

    BigDecimal id
    String devPhase

    static mapping = {
        datasource "pva"
        table "VW_LDP_DEV_PHASE_DSP"

        cache: "read-only"
        version false

        id column: "DEV_PHASE_ID", type: "big_decimal", generator: "assigned"
        devPhase column: "DEV_PHASE"
    }

    static constraints = {
        id(nullable:false, unique:true)
        devPhase(blank:false, maxSize:50)

    }

    @Override
    def getSelectableList() {
        LmDevPhase.withTransaction {
            return LmDevPhase.findAll().unique().collect { it.devPhase }.sort()
        }
    }
}
