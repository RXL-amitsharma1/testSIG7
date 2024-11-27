package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmAgeGroups implements SelectableList {

    BigDecimal id
    String groupName

    static mapping = {
        datasource "pva"
        table "VW_LAG_AGE_GROUP_DSP"

        cache: "read-only"
        version false

        id column: "AGE_GROUP_ID", type: "big_decimal", generator: "assigned"
        groupName column: "AGE_GROUP"
    }

    static constraints = {
        id(nullable:false, unique:true)
        groupName(blank:false, maxSize:20)

    }

    @Override
    def getSelectableList() {
        LmAgeGroups.withTransaction {
            return LmAgeGroups.findAll().unique().collect { it.groupName }.sort()
        }
    }
}
