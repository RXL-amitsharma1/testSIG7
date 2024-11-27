package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmOccupations implements SelectableList {

    BigDecimal id
    String occupation

    static mapping = {
        datasource "pva"
        table "VW_LO_OCCUPATION_DSP"

        cache: "read-only"
        version false

        id column: "OCCUPATION_ID", type: "big_decimal", generator: "assigned"
        occupation column: "OCCUPATION"
    }

    static constraints = {
        id(nullable:false, unique:true)
        occupation(blank:false, maxSize:40)

    }

    @Override
    def getSelectableList() {
        LmOccupations.withTransaction {
            return LmOccupations.findAll().unique().collect { it.occupation }.sort()
        }
    }
}
