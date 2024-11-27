package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmAlwaysSeriousTerm implements SelectableList {

    BigDecimal id
    String astPTCode

    static mapping = {
        datasource "pva"
        table "VW_ALWAYS_SERIOUS_TERM_PT_LLT_DSP"

        cache: "read-only"
        version false

        id column: "AST_ID", type: "big_decimal", generator: "assigned"
        astPTCode column: "AST_PT_CODE"
    }

    static constraints = {
        id(nullable:false, unique:true)
        astPTCode(blank:false, maxSize:50)

    }

    @Override
    def getSelectableList() {
        LmAlwaysSeriousTerm.withTransaction {
            return LmAlwaysSeriousTerm.findAll().unique().collect { it.astPTCode }.sort()
        }
    }
}
