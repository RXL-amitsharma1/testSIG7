package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmManufacturer implements SelectableList {

    BigDecimal id
    String manufacturerName

    static mapping = {
        datasource "pva"
        table "VW_LM_MANU_NAME_DSP"

        cache: "read-only"
        version false

        id column: "MANUFACTURER_ID", type: "big_decimal", generator: "assigned"
        manufacturerName column: "MANU_NAME"
    }

    static constraints = {
        id(nullable:false, unique:true)
        manufacturerName(blank:false, maxSize:40)

    }

    @Override
    def getSelectableList() {
        LmManufacturer.withTransaction {
            return LmManufacturer.findAll().unique().collect { it.manufacturerName }.sort()
        }
    }
}
