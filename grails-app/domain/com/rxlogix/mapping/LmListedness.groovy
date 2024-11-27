package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmListedness implements SelectableList {

    BigDecimal id
    String listedness

    static mapping = {
        table "VW_LLIST_LISTEDNESS_DSP"
        datasource "pva"

        version false
        cache usage: "read-only"

        id column: "LISTEDNESS_ID", type: "big_decimal", generator: "assigned"
        listedness column: "LISTEDNESS"
    }

    static constraints = {
        id(nullable:false, unique:true)
        listedness(maxSize: 20)
    }

    @Override
    public getSelectableList() {
        LmListedness.withTransaction {
            return LmListedness.findAll().unique().collect { it.listedness }.sort()
        }
    }
}
