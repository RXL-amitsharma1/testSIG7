package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmLabTestGroup implements SelectableList {

    BigDecimal id
    String labTestGroup

    static mapping = {
        datasource "pva"
        table "VW_LLTG_LAB_TEST_GROUP_DSP"

        cache: "read-only"
        version false

        id column: "LAB_TEST_GROUP_ID", type: "big_decimal", generator: "assigned"
        labTestGroup column: "LAB_TEST_GROUP"
    }

    static constraints = {
        id(nullable:false, unique:true)
        labTestGroup(blank:false, maxSize:100)

    }

    @Override
    def getSelectableList() {
        LmLabTestGroup.withTransaction {
            return LmLabTestGroup.findAll().unique().collect { it.labTestGroup }.sort()
        }
    }
}
