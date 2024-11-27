package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmDeviceSubcomponents implements SelectableList {

    BigDecimal id
    String name

    static mapping = {
        datasource "pva"
        table "VW_LDES_SUBCOMPONENT_NAME_DSP"

        cache: "read-only"
        version false

        id column: "SUBCOMPONENT_ID", type: "big_decimal", generator: "assigned"
        name column: "SUBCOMPONENT_NAME"
    }

    static constraints = {
        id(nullable:false, unique:true)
        name(blank:false, maxSize:50)

    }

    @Override
    def getSelectableList() {
        LmDeviceSubcomponents.withTransaction {
            return LmDeviceSubcomponents.findAll().unique().collect { it.name }.sort()
        }
    }
}