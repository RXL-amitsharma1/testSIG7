package com.rxlogix.mapping.translation

import com.rxlogix.SelectableList

class CmnLookupState2 implements SelectableList {

    BigDecimal id
    String state

    static mapping = {
        datasource "pva"
        table "CMN_LOOKUP_STATE2"

        cache: "read-only"
        version false

        id column: "id", type: "big_decimal", generator: "assigned"
        state column: "STATE2"
    }

    static constraints = {
        id(nullable:false)
        state(blank:false, maxSize:10)

    }

    @Override
    def getSelectableList() {
        CmnLookupState2.withTransaction {
            return CmnLookupState2.findAll().unique().collect { it.state }.sort()
        }
    }
}
