package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmHcp implements SelectableList {

    BigDecimal id
    String hcp

    static mapping = {
        datasource "pva"
        table "VW_LH_HCP_DSP"

        cache: "read-only"
        version false

        id column: "HCP_ID", type: "big_decimal", generator: "assigned"
        hcp column: "HCP"
    }

    static constraints = {
        id(nullable:false, unique:true)
        hcp(blank:false, maxSize:30)

    }

    @Override
    def getSelectableList() {
        LmHcp.withTransaction {
            return LmHcp.findAll().unique().collect { it.hcp }.sort()
        }
    }
}
