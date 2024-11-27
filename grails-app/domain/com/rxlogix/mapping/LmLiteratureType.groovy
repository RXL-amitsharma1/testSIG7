package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmLiteratureType implements SelectableList {

    BigDecimal id
    String literatureType

    static mapping = {
        datasource "pva"
        table "VW_LLTP_LIT_TYPE_DSP"

        cache: "read-only"
        version false

        id column: "LITERATURE_ID", type: "big_decimal", generator: "assigned"
        literatureType column: "LIT_TYPE"
    }

    static constraints = {
        id(nullable:false, unique:true)
        literatureType(blank:false, maxSize:40)

    }

    @Override
    def getSelectableList() {
        LmLiteratureType.withTransaction {
            return LmLiteratureType.findAll().unique().collect { it.literatureType }.sort()
        }
    }
}
