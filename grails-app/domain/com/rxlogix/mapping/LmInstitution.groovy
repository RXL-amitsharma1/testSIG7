package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmInstitution implements SelectableList {

    BigDecimal id
    String institution

    static mapping = {
        datasource "pva"
        table "VW_LINS_INSTITUTION_DSP"

        cache: "read-only"
        version false

        id column: "INST_ID", type: "big_decimal", generator: "assigned"
        institution column: "INSTITUTION"
    }

    static constraints = {
        id(nullable:false, unique:true)
        institution(blank:false, maxSize:60)

    }

    @Override
    def getSelectableList() {
        LmInstitution.withTransaction {
            return LmInstitution.findAll().unique().collect { it.institution }.sort()
        }
    }
}
