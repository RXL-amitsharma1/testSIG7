package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmCondition implements SelectableList {

    BigDecimal id
    String conditionType

    static mapping = {
        datasource "pva"
        table "VW_LCT_CONDITION_TYPE_DSP"

        cache: "read-only"
        version false

        id column: "CONDITION_TYPE_ID", type: "big_decimal", generator: "assigned"
        conditionType column: "CONDITION_TYPE"
    }

    static constraints = {
        id(nullable:false, unique:true)
        conditionType(blank:false, maxSize:20)

    }

    @Override
    def getSelectableList() {
        LmCondition.withTransaction {
            return LmCondition.findAll().unique().collect { it.conditionType }.sort()
        }
    }
}
