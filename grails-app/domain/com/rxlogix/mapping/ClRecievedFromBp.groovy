package com.rxlogix.mapping

import com.rxlogix.SelectableList

class ClRecievedFromBp implements SelectableList {

    BigDecimal id
    String description

    static mapping = {
        table "CL_UDF_RECIEVED_BP"
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
        ClRecievedFromBp.withTransaction {
            return ClRecievedFromBp.findAll().unique().collect { it.description }.sort()
        }
    }
}
