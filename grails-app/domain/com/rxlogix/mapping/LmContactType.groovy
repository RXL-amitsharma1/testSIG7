package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmContactType implements SelectableList {

    BigDecimal id
    String contactType

    static mapping = {
        datasource "pva"
        table "VW_LCTY_CONTACT_TYPE_DSP"

        cache: "read-only"
        version false

        id column: "CONTACT_ID", type: "big_decimal", generator: "assigned"
        contactType column: "CONTACT_TYPE"
    }

    static constraints = {
        id(nullable:false, unique:true)
        contactType(blank:false, maxSize:10)

    }

    @Override
    def getSelectableList() {
        LmContactType.withTransaction {
            return LmContactType.findAll().unique().collect { it.contactType }.sort()
        }
    }
}
