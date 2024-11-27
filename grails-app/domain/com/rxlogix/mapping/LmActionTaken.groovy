package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmActionTaken implements SelectableList {

    BigDecimal id
    String actionTaken

    static mapping = {
        datasource "pva"
        table "VW_LAT_ACTION_TAKEN_DSP"

        cache: "read-only"
        version false

        id column: "ACT_TAKEN_ID", type: "big_decimal", generator: "assigned"
        actionTaken column: "ACTION_TAKEN"
    }

    static constraints = {
        id(nullable:false, unique:true)
        actionTaken(blank:false, maxSize:30)

    }

    @Override
    def getSelectableList() {
        LmActionTaken.withTransaction {
            return LmActionTaken.findAll().unique().collect { it.actionTaken }.sort()
        }
    }
}
