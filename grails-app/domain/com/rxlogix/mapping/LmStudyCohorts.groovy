package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmStudyCohorts implements SelectableList {

    BigDecimal id
    String blindName

    static mapping = {
        datasource "pva"
        table "VW_STUDY_COHORTS_DSP"

        cache: "read-only"
        version false

        id column: "COHORT_ID", type: "big_decimal", generator: "assigned"
        blindName column: "COHORT_NAME"
    }

    static constraints = {
        id(nullable:false, unique:true)
        blindName(blank:false, maxSize:70)

    }

    @Override
    def getSelectableList() {
        LmStudyCohorts.withTransaction {
            return LmStudyCohorts.findAll().unique().collect { it.blindName }.sort()
        }
    }
}
