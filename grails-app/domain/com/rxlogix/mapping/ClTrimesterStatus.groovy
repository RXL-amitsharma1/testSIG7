package com.rxlogix.mapping

import com.rxlogix.SelectableList

class ClTrimesterStatus implements SelectableList {

    BigDecimal id
    String description

    static mapping = {
        table "VW_CLTS_STATUS"
        datasource "pva"

        version false
        cache usage: "read-only"

        id column: "ID", type: "big_decimal", generator: "assigned"
        description column: "STATUS"
    }

    static constraints = {
        id(nullable:false, unique:true)
        description(maxSize: 100)
    }

    @Override
    def getSelectableList() {
        ClTrimesterStatus.withTransaction {
            return ClTrimesterStatus.findAll().unique().collect { it.description }.sort()
        }
    }
}
