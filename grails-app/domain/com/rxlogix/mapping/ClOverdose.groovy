package com.rxlogix.mapping

import com.rxlogix.SelectableList

class ClOverdose implements SelectableList {

    BigDecimal id
    String description

    static mapping = {
        table "CL_UDF_OVERDOSE"
        datasource "pva"

        version false
        cache usage: "read-only"

        id column: "ID", type: "big_decimal", generator: "assigned"
        description column: "DESCRIPTION"
    }

    static constraints = {
        id(nullable:true, unique:true)
        description(maxSize: 100)
    }

    @Override
    def getSelectableList() {
        ClOverdose.withTransaction {
            return ClOverdose.findAll().unique().collect { it.description }.sort()
        }
    }
}
