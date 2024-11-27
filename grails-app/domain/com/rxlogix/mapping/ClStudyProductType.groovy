package com.rxlogix.mapping

import com.rxlogix.SelectableList

class ClStudyProductType implements SelectableList {

    BigDecimal id
    String name

    static mapping = {
        table "VW_CSPT_MED_PROD_TYPE"
        datasource "pva"

        version false
        cache usage: "read-only"

        id column: "PROD_TYPE_ID", type: "big_decimal", generator: "assigned"
        name column: "MED_PROD_TYPE"
    }

    static constraints = {
        id(nullable:false, unique:true)
        name(maxSize: 50)
    }

    @Override
    def getSelectableList() {
        ClStudyProductType.withTransaction {
            return ClStudyProductType.findAll().unique().collect { it.name }.sort()
        }
    }
}
