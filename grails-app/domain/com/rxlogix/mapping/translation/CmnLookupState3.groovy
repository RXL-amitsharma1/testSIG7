package com.rxlogix.mapping.translation

import com.rxlogix.SelectableList

class CmnLookupState3 implements SelectableList {

    BigDecimal id
    String state

    static mapping = {
        datasource "pva"
        table "CMN_LOOKUP_STATE3"

        cache: "read-only"
        version false

        id column: "id", type: "big_decimal", generator: "assigned"
        state column: "STATE3"
    }

    static constraints = {
        id(nullable:false)
        state(blank:false, maxSize:10)

    }

    @Override
    def getSelectableList() {
        CmnLookupState3.withTransaction {
            return CmnLookupState3.findAll().unique().collect { it.state }.sort()
        }
    }
}
