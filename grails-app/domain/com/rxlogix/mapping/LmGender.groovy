package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmGender implements SelectableList {

    BigDecimal id
    String gender

    static mapping = {
        datasource "pva"
        table "VW_LG_GENDER_DSP"

        cache: "read-only"
        version false

        id column: "GENDER_ID", type: "big_decimal", generator: "assigned"
        gender column: "GENDER"
    }

    static constraints = {
        id(nullable:false, unique:true)
        gender(blank:false, maxSize:10)

    }

    @Override
    def getSelectableList() {
        LmGender.withTransaction {
            return LmGender.findAll().unique().collect { it.gender }.sort()
        }
    }
}
