package com.rxlogix.mapping.translation

import com.rxlogix.SelectableList

class TrLmDrugType implements SelectableList {

    BigDecimal id
    String drugType

    static mapping = {
        datasource "pva"
        table "LM_DRUG_TYPE"

        cache: "read-only"
        version false

        id column: "ID", type: "big_decimal", generator: "assigned"
        drugType column: "DRUG_TYPE"
    }

    static constraints = {
        id(nullable:false, unique:true)
        drugType(blank:false, maxSize:20)

    }

    @Override
    def getSelectableList() {
        TrLmDrugType.withTransaction {
            return TrLmDrugType.findAll().unique().collect { it.drugType }.sort()
        }
    }
}
