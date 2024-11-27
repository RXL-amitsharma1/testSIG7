package com.rxlogix.mapping

import com.rxlogix.SelectableList

class ClCtcaeGrade implements SelectableList {

    BigDecimal id
    String description

    static mapping = {
        table "CL_UDF_CTCAE_GRADE"
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
        ClCtcaeGrade.withTransaction {
            return ClCtcaeGrade.findAll().unique().collect { it.description }.sort()
        }
    }
}