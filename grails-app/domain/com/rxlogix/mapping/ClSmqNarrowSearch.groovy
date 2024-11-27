package com.rxlogix.mapping

import com.rxlogix.SelectableList

class ClSmqNarrowSearch implements SelectableList {

    BigDecimal id
    String name

    static mapping = {
        datasource "pva"
        table "SMQ_NARROW_SEARCH"

        cache: "read-only"
        version false

        id column: "SMQ_CODE", type: "big_decimal", generator: "assigned"
        name column: "SMQ_NAME"
    }

    static constraints = {
        id(nullable:false, unique:true)
        name(blank:false, maxSize:100)

    }

    @Override
    def getSelectableList() {
        ClSmqNarrowSearch.withTransaction {
            return ClSmqNarrowSearch.findAll().unique().collect { it.name }.sort()
        }
    }
}

