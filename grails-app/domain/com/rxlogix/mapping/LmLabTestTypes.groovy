package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmLabTestTypes implements SelectableList {

    BigDecimal id
    String labTestType

    static mapping = {
        datasource "pva"
        table "VW_LLTT_LAB_TEST_DSP"

        cache: "read-only"
        version false

        id column: "LAB_TEST_ID", type: "big_decimal", generator: "assigned"
        labTestType column: "LAB_TEST"
    }

    static constraints = {
        id(nullable:false, unique:true)
        labTestType(blank:false, maxSize:250)

    }

    @Override
    def getSelectableList() {
        LmLabTestTypes.withTransaction {
            return LmLabTestTypes.findAll().unique().collect { it.labTestType }.sort()
        }
    }
}
