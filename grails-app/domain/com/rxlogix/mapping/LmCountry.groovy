package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmCountry implements SelectableList {

    String id
    String name

    static mapping = {
        datasource "pva"
        table "VW_LCO_COUNTRY_DSP"

        cache: "read-only"
        version false

        id column: "COUNTRY_ID", generator: "assigned"
        name column: "COUNTRY"
    }

    static constraints = {
        id(nullable:false, unique:true)
        name(blank:false, maxSize:50)

    }

    @Override
    def getSelectableList() {
        LmCountry.withTransaction {
            return LmCountry.findAll().unique().collect { it.name }.sort()
        }
    }
}
