package com.rxlogix.mapping

import com.rxlogix.SelectableList

class ClMfrEvaluation implements SelectableList {

    BigDecimal id
    String evaluation

    static mapping = {
        table "VW_CMFR_EVALUATION_DSP"
        datasource "pva"

        version false
        cache usage: "read-only"

        id column: "ID", type: "big_decimal", generator: "assigned"
        evaluation column: "EVALUATION"
    }

    static constraints = {
        id(nullable:true, unique:true)
        evaluation(maxSize: 100)
    }

    @Override
    def getSelectableList() {
        ClMfrEvaluation.withTransaction {
            return ClMfrEvaluation.findAll().unique().collect { it.evaluation }.sort()
        }
    }
}