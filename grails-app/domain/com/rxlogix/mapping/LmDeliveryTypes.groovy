package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmDeliveryTypes implements SelectableList {

    BigDecimal id
    String deliveryType

    static mapping = {
        datasource "pva"
        table "VW_LDTY_DELIVERY_TYPE_DSP"

        cache: "read-only"
        version false

        id column: "DELIVERY_TYPE_ID", type: "big_decimal", generator: "assigned"
        deliveryType column: "DELIVERY_TYPE"
    }

    static constraints = {
        id(nullable:false, unique:true)
        deliveryType(blank:false, maxSize:30)

    }

    @Override
    def getSelectableList() {
        LmDeliveryTypes.withTransaction {
            return LmDeliveryTypes.findAll().unique().collect { it.deliveryType }.sort()
        }
    }
}
