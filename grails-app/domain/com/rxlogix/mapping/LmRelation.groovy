package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmRelation implements SelectableList {

    BigDecimal id
    String relation

    static mapping = {
        datasource "pva"
        table "VW_LR_DESC_DSP"

        cache: "read-only"
        version false

        id column: "ID", type: "big_decimal", generator: "assigned"
        relation column: "DESCRIPTION"
    }

    static constraints = {
        id(nullable:false, unique:true)
        relation(blank:false, maxSize:50)

    }

    @Override
    def getSelectableList() {
        LmRelation.withTransaction {
            return LmRelation.findAll().unique().collect { it.relation }.sort()
        }
    }
}
