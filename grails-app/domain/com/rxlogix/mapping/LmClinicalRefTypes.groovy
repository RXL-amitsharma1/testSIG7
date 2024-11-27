package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmClinicalRefTypes implements SelectableList {

    BigDecimal id
    String clinicalRefType

    static mapping = {
        datasource "pva"
        table "VW_LCRT_REF_TYPE_DESC_DSP"

        cache: "read-only"
        version false

        id column: "REF_TYPE_ID", type: "big_decimal", generator: "assigned"
        clinicalRefType column: "REF_TYPE_DESC"
    }

    static constraints = {
        id(nullable:false, unique:true)
        clinicalRefType(blank:false, maxSize:20)

    }

    @Override
    def getSelectableList() {
        LmClinicalRefTypes.withTransaction {
            return LmClinicalRefTypes.findAll().unique().collect { it.clinicalRefType }.sort()
        }
    }
}
