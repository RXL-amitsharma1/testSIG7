package com.rxlogix.mapping

import com.rxlogix.SelectableList

class ClMedicationType implements SelectableList {

    BigDecimal id
    String prod_type

    static mapping = {
        table "VW_CSPT_MED_PROD_TYPE"
        datasource "pva"

        version false
        cache usage: "read-only"

        id column: "PROD_TYPE_ID", type: "big_decimal", generator: "assigned"
        prod_type column: "MED_PROD_TYPE"
    }

    static constraints = {
        id(nullable:true, unique:true)
        prod_type(maxSize: 50)
    }

    @Override
    def getSelectableList() {
        ClMedicationType.withTransaction {
            return ClMedicationType.findAll().unique().collect { it.prod_type }.sort()
        }
    }
}
