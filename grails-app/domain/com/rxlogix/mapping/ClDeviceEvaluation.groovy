package com.rxlogix.mapping

import com.rxlogix.SelectableList

class ClDeviceEvaluation implements SelectableList {

    BigDecimal id
    String evaluation

    static mapping = {
        table "VW_CDEE_DEV_EVAL_DSP"
        datasource "pva"

        version false
        cache usage: "read-only"

        id column: "ID", type: "big_decimal", generator: "assigned"
        evaluation column: "DEV_EVAL"
    }

    static constraints = {
        id(nullable:true, unique:true)
        evaluation(maxSize: 100)
    }

    @Override
    def getSelectableList() {
        ClDeviceEvaluation.withTransaction {
            return ClDeviceEvaluation.findAll().unique().collect { it.evaluation }.sort()
        }
    }
}