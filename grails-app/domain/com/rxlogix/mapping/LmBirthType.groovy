package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmBirthType implements SelectableList {

    BigDecimal id
    String birthType

    static mapping = {
        datasource "pva"
        table "VW_LBT_BIRTH_TYPE_DSP"

        cache: "read-only"
        version false

        id column: "BIRTH_TYPE_ID", type: "big_decimal", generator: "assigned"
        birthType column: "BIRTH_TYPE"
    }

    static constraints = {
        id(nullable:false, unique:true)
        birthType(blank:false, maxSize:35)

    }

    @Override
    def getSelectableList() {
        LmBirthType.withTransaction {
            return LmBirthType.findAll().unique().collect { it.birthType }.sort()
        }
    }
}
