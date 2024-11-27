package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmReported implements SelectableList {

    BigDecimal id
    String desc

    static mapping = {
        datasource "pva"
        table "VW_LRETD_DESC_DSP"

        cache: "read-only"
        version false

        id column: "ID", type: "big_decimal", generator: "assigned"
        desc column: "DESCRIPTION"
    }

    static constraints = {
        id(nullable:false, unique:true)
        desc(blank:false, maxSize:50)

    }

    @Override
    def getSelectableList() {
        return LmReported.findAll().unique().collect { it.desc }.sort()
    }
}