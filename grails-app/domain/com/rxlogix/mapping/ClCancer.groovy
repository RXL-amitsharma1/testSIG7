package com.rxlogix.mapping

import com.rxlogix.SelectableList

class ClCancer implements SelectableList {

    BigDecimal id
    String description

    static mapping = {
        table "CL_UDF_CANCER"
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
        ClCancer.withTransaction {
            return ClCancer.findAll().unique().collect { it.description }.sort()
        }
    }
}
