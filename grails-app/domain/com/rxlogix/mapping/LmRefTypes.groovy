package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmRefTypes implements SelectableList {

    BigDecimal id
    String refType

    static mapping = {
        datasource "pva"
        table "VW_LRT_TYPE_DESC_DSP"

        cache: "read-only"
        version false

        id column: "REF_TYPE_ID", type: "big_decimal", generator: "assigned"
        refType column: "TYPE_DESC"
    }

    static constraints = {
        id(nullable:false, unique:true)
        refType(blank:false, maxSize:25)

    }

    @Override
    def getSelectableList() {
        LmReportType.withTransaction {
            return LmRefTypes.findAll().unique().collect { it.refType }.sort()
        }
    }
}
