package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmLicenseNumber implements SelectableList {

    BigDecimal id
    String licenseNumber

    static mapping = {
        datasource "pva"
        table "VW_TRADE_NAME_DSP"

        cache: "read-only"
        version false

        id column: "LICENSE_ID", type: "big_decimal", generator: "assigned"
        licenseNumber column: "LIC_NUMBER"
    }

    static constraints = {
        id(nullable:false, unique:true)
        licenseNumber(maxSize:40)
    }

    @Override
    def getSelectableList() {
        LmLicenseNumber.withTransaction {
            return LmLicenseNumber.findAll().unique().collect{it.licenseNumber? it.licenseNumber:'' }.sort()
        }
    }
}
