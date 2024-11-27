package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmMfrEvalReason implements SelectableList {

    BigDecimal id
    String evaluation

    static mapping = {
        table "VW_LMER_MFR_EVAL_REASON_DSP"
        datasource "pva"

        version false
        cache usage: "read-only"

        id column: "MFR_EVAL_ID", type: "big_decimal", generator: "assigned"
        evaluation column: "MFR_EVAL_REASON"
    }

    static constraints = {
        id(nullable:true, unique:true)
        evaluation(maxSize: 100)
    }

    @Override
    def getSelectableList() {
        LmMfrEvalReason.withTransaction {
            return LmMfrEvalReason.findAll().unique().collect { it.evaluation }.sort()
        }
    }
}