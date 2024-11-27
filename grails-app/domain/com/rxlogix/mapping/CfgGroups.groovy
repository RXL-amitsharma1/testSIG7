package com.rxlogix.mapping

import com.rxlogix.SelectableList

class CfgGroups implements SelectableList {

    BigDecimal id
    String name

    static mapping = {
        table "VW_GROUPS_DSP"
        datasource "pva"

        version false
        cache usage: "read-only"

        id column: "GROUP_ID", type: "big_decimal", generator: "assigned"
        name column: "GROUP_NAME"
    }

    static constraints = {
        id(nullable:false, unique:true)
        name(maxSize: 40)
    }

    @Override
    def getSelectableList() {
        CfgGroups.withTransaction {
            return CfgGroups.findAll().unique().collect { it.name }.sort()
        }
    }
}
