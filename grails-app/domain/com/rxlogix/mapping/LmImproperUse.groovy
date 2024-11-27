package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmImproperUse implements SelectableList {

    BigDecimal id
    String gender

    static mapping = {
        datasource "pva"
        table "VW_LIMU_IMPROPER_USE_DSP"

        cache: "read-only"
        version false

        id column: "IMPROPER_USE_ID", type: "big_decimal", generator: "assigned"
        gender column: "IMPROPER_USE"
    }

    static constraints = {
        id(nullable:false, unique:true)
        gender(blank:false, maxSize:100)

    }

    @Override
    def getSelectableList() {
        LmImproperUse.withTransaction {
            return LmImproperUse.findAll().unique().collect { it.gender }.sort()
        }
    }
}