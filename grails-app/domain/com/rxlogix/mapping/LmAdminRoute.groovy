package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmAdminRoute implements SelectableList {

    BigDecimal id
    String adminRoute

    static mapping = {
        datasource "pva"
        table "VW_LAR_ROUTE_DSP"

        cache: "read-only"
        version false

        id column: "ADMIN_ROUTE_ID", type: "big_decimal", generator: "assigned"
        adminRoute column: "ROUTE"
    }

    static constraints = {
        id(nullable:false, unique:true)
        adminRoute(blank:false, maxSize:15)

    }

    @Override
    def getSelectableList() {
        LmAdminRoute.withTransaction {
            return LmAdminRoute.findAll().unique().collect { it.adminRoute }.sort()
        }
    }
}
