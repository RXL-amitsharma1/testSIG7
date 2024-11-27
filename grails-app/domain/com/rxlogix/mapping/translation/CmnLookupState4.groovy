package com.rxlogix.mapping.translation

import com.rxlogix.SelectableList

class CmnLookupState4 implements SelectableList {

    BigDecimal id
    String state

    static mapping = {
        datasource "pva"
        table "CMN_LOOKUP_STATE4"

        cache: "read-only"
        version false

        id column: "id", type: "big_decimal", generator: "assigned"
        state column: "STATE4"
    }

    static constraints = {
        id(nullable:false)
        state(blank:false, maxSize:10)

    }

    @Override
    def getSelectableList() {
        CmnLookupState4.withTransaction {
            return CmnLookupState4.findAll().unique().collect { it.state }.sort()
        }
    }
}
