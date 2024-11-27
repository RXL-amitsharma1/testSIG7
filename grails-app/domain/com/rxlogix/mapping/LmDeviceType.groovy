package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmDeviceType implements SelectableList {

    BigDecimal id
    String deviceTypeDesc

    static mapping = {
        datasource "pva"
        table "VW_LDT_DEVICE_TYPE_DESC_DSP"

        cache: "read-only"
        version false

        id column: "DEVICE_TYPE_ID", type: "big_decimal", generator: "assigned"
        deviceTypeDesc column: "DEVICE_TYPE_DESC"
    }

    static constraints = {
        id(nullable:false, unique:true)
        deviceTypeDesc(blank:false, maxSize:50)

    }

    @Override
    def getSelectableList() {
        LmDeviceType.withTransaction {
            return LmDeviceType.findAll().unique().collect { it.deviceTypeDesc }.sort()
        }
    }
}
