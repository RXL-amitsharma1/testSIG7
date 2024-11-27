package com.rxlogix.mapping

import com.rxlogix.SelectableList

class ClDeviceUsage implements SelectableList {

    BigDecimal id
    String usage

    static mapping = {
        table "VW_CDEU_DEV_USAGE_DSP"
        datasource "pva"

        version false
        cache usage: "read-only"

        id column: "ID", type: "big_decimal", generator: "assigned"
        usage column: "DEV_USAGE"
    }

    static constraints = {
        id(nullable:true, unique:true)
        usage(maxSize: 100)
    }

    @Override
    def getSelectableList() {
        ClDeviceUsage.withTransaction {
            return ClDeviceUsage.findAll().unique().collect { it.usage }.sort()
        }
    }
}