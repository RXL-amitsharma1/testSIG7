package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmSites implements SelectableList {

    BigDecimal id
    String siteDesc

    static mapping = {
        datasource "pva"
        table "VW_LSI_SITE_DESC_DSP"

        cache: "read-only"
        version false

        id column: "SITE_ID", type: "big_decimal", generator: "assigned"
        siteDesc column: "SITE_DESC"
    }

    static constraints = {
        id(nullable:false, unique:true)
        siteDesc(blank:false, maxSize:40)

    }

    @Override
    def getSelectableList() {
        LmSites.withTransaction {
            return LmSites.findAll().unique().collect { it.siteDesc }.sort()
        }
    }
}
