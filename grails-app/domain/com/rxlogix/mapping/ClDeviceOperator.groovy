package com.rxlogix.mapping

import com.rxlogix.SelectableList

class ClDeviceOperator implements SelectableList {

    BigDecimal id
    String operator

    static mapping = {
        table "VW_CDOP_DEV_OPER_DSP"
        datasource "pva"

        version false
        cache usage: "read-only"

        id column: "ID", type: "big_decimal", generator: "assigned"
        operator column: "DEV_OPER"
    }

    static constraints = {
        id(nullable:true, unique:true)
        operator(maxSize: 100)
    }

    @Override
    def getSelectableList() {
        ClDeviceOperator.withTransaction {
            return ClDeviceOperator.findAll().unique().collect { it.operator }.sort()
        }
    }
}