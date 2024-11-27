package com.rxlogix.mapping

import com.rxlogix.SelectableList

class ClUsageOfDevice implements SelectableList {

    BigDecimal id
    String usage

    static mapping = {
        table "VW_CUOD_USAGE_OF_DEVICE_DSP"
        datasource "pva"

        version false
        cache usage: "read-only"

        id column: "ID", type: "big_decimal", generator: "assigned"
        usage column: "USAGE_OF_DEVICE"
    }

    static constraints = {
        id(nullable:true, unique:true)
        usage(maxSize: 100)
    }

    @Override
    def getSelectableList() {
        ClUsageOfDevice.withTransaction {
            return ClUsageOfDevice.findAll().unique().collect { it.usage }.sort()
        }
    }
}