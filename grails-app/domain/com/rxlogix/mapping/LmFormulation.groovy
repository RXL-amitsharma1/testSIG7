package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmFormulation implements SelectableList {

    BigDecimal id
    String formulation

    static mapping = {
        datasource "pva"
        table "VW_LFOR_FORMULATION_DSP"

        cache: "read-only"
        version false

        id column: "FORMULATION_ID", type: "big_decimal", generator: "assigned"
        formulation column: "FORMULATION"
    }

    static constraints = {
        id(nullable:false, unique:true)
        formulation(blank:false, maxSize:100)

    }

    @Override
    def getSelectableList() {
        LmFormulation.withTransaction {
            return LmFormulation.findAll().unique().collect { it.formulation }.sort()
        }
    }
}
