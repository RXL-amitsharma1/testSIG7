package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmUnblindingStatus implements SelectableList {

    BigDecimal id
    String unblindingStatus

    static mapping = {
        datasource "pva"
        table "VW_LUS_UNBLINDING_STATUS_DSP"

        cache: "read-only"
        version false

        id column: "STATUS_ID", type: "big_decimal", generator: "assigned"
        unblindingStatus column: "UNBLINDING_STATUS"
    }

    static constraints = {
        id(nullable: false, unique: true)
        unblindingStatus(blank: false, maxSize: 30)

    }

    @Override
    def getSelectableList() {
        LmUnblindingStatus.withTransaction {
            return LmUnblindingStatus.findAll().unique().collect { it.unblindingStatus }.sort()
        }
    }
}
