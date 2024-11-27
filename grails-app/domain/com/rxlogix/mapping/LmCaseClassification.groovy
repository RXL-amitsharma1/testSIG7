package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmCaseClassification implements SelectableList {

    BigDecimal id
    String description

    static mapping = {
        datasource "pva"
        table "VW_LCC_CHARACTERISTICS_DSP"

        cache: "read-only"
        version false

        id column: "ID", type: "big_decimal", generator: "assigned"
        description column: "CHARACTERISTICS"
    }

    static constraints = {
        id(nullable:false, unique:true)
        description(blank:false, maxSize:30)

    }

    @Override
    def getSelectableList() {
        LmCaseClassification.withTransaction {
            return LmCaseClassification.findAll().unique().collect { it.description }.sort()
        }
    }
}
