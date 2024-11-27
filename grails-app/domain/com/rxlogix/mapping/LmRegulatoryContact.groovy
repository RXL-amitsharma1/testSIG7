package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmRegulatoryContact implements SelectableList {

    BigDecimal id
    String name

    static mapping = {
        table "VW_LRC_AGENCY_NAME_DSP"
        datasource "pva"

        version false
        cache usage: "read-only"

        id column: "AGENCY_ID", type: "big_decimal", generator: "assigned"
        name column: "AGENCY_NAME"
    }

    static constraints = {
        id(nullable:false, unique:true)
        name(maxSize: 40)
    }

    @Override
    def getSelectableList() {
        LmRegulatoryContact.withTransaction {
            return LmRegulatoryContact.findAll().unique().collect { it.name }.sort()
        }
    }
}
