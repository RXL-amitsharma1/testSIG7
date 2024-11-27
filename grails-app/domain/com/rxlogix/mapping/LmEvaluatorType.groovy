package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmEvaluatorType implements SelectableList {

    BigDecimal id
    String name

    static mapping = {
        table "VW_LET_EVALUATOR_TYPE_DSP"
        datasource "pva"

        version false
        cache usage: "read-only"

        id column: "EVALUATOR_TYPE_ID", type: "big_decimal", generator: "assigned"
        name column: "EVALUATOR_TYPE"
    }

    static constraints = {
        id(nullable:false, unique:true)
        name(maxSize: 25)
    }

    @Override
    def getSelectableList() {
        LmEvaluatorType.withTransaction {
            return LmEvaluatorType.findAll().unique().collect { it.name }.sort()
        }
    }
}
