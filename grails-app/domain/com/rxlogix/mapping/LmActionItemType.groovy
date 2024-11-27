package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmActionItemType implements SelectableList {

    BigDecimal id
    String actionType

    static mapping = {
        datasource "pva"
        table "VW_LAIT_ACTION_TYPE_DSP"

        cache: "read-only"
        version false

        id column: "ACTION_TYPE_ID", type: "big_decimal", generator: "assigned"
        actionType column: "ACTION_TYPE"
    }

    static constraints = {
        id(nullable:false, unique:true)
        actionType(blank:false, maxSize:40)

    }

    @Override
    def getSelectableList() {
        LmActionItemType.withTransaction {
            return LmActionItemType.findAll().unique().collect { it.actionType }.sort()
        }
    }
}
