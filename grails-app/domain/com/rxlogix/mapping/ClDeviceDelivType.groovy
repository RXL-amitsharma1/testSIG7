package com.rxlogix.mapping

import com.rxlogix.SelectableList

class ClDeviceDelivType implements SelectableList {

    BigDecimal id
    String type

    static mapping = {
        table "VW_CDDT_DELIV_TYPE_DSP"
        datasource "pva"

        version false
        cache usage: "read-only"

        id column: "ID", type: "big_decimal", generator: "assigned"
        type column: "DELIV_TYPE"
    }

    static constraints = {
        id(nullable:true, unique:true)
        type(maxSize: 100)
    }

    @Override
    def getSelectableList() {
        ClDeviceDelivType.withTransaction {
            return ClDeviceDelivType.findAll().unique().collect { it.type }.sort()
        }
    }
}