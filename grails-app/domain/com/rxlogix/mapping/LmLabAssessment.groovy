package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmLabAssessment implements SelectableList {

    BigDecimal id
    String labAssessment

    static mapping = {
        datasource "pva"
        table "VW_LLA_ASSESSMENT_DSP"

        cache: "read-only"
        version false

        id column: "ASSESSMENT_ID", type: "big_decimal", generator: "assigned"
        labAssessment column: "ASSESSMENT"
    }

    static constraints = {
        id(nullable:false, unique:true)
        labAssessment(blank:false, maxSize:20)

    }

    @Override
    def getSelectableList() {
        LmLabAssessment.withTransaction {
            return LmLabAssessment.findAll().unique().collect { it.labAssessment }.sort()
        }
    }
}
