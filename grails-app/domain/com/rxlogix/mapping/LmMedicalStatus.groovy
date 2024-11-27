package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmMedicalStatus implements SelectableList {

    BigDecimal id
    String medicalStatus

    static mapping = {
        datasource "pva"
        table "VW_LMS_SEQ_NUM_DSP"

        cache: "read-only"
        version false

        id column: "MED_STATUS_ID", type: "big_decimal", generator: "assigned"
        medicalStatus column: "LABEL"
    }

    static constraints = {
        id(nullable:false, unique:true)
        medicalStatus(blank:false, maxSize:40)

    }

    @Override
    def getSelectableList() {
        LmMedicalStatus.withTransaction {
            return LmMedicalStatus.findAll().unique().collect { it.medicalStatus }.sort()
        }
    }
}
