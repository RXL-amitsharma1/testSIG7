package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmEvtFrequency implements SelectableList {

    BigDecimal id
    String evtFreq

    static mapping = {
        datasource "pva"
        table "VW_LEF_EVT_FREQ_DSP"

        cache: "read-only"
        version false

        id column: "EVT_FREQ_ID", type: "big_decimal", generator: "assigned"
        evtFreq column: "EVT_FREQ"
    }

    static constraints = {
        id(nullable:false, unique:true)
        evtFreq(blank:false, maxSize:15)

    }

    @Override
    def getSelectableList() {
        LmEvtFrequency.withTransaction {
            return LmEvtFrequency.findAll().unique().collect { it.evtFreq }.sort()
        }
    }
}
