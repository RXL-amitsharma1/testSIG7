package com.rxlogix.mapping

import com.rxlogix.SelectableList
import grails.util.Holders

class LmLicense implements SelectableList {

    BigDecimal id
    String tradeName
    String lang

    static mapping = {
        datasources(Holders.getGrailsApplication().getConfig().pvsignal.supported.datasource.call())
        table "VW_TRADE_NAME_APPROVAL_NUM_ALL"

        cache: "read-only"
        version false

        id column: "LICENSE_ID", type: "big_decimal", generator: "assigned"
        tradeName column: "TRADE_NAME_APPROVAL_NUMBER"
        lang column: "lang_id"
    }

    static constraints = {
        id(nullable:false, unique:false)
        tradeName(blank:false, maxSize:70)
    }

    @Override
    def getSelectableList() {
        LmLicense.withTransaction {
            return this.executeQuery("select distinct c.tradeName from LmLicense c order by c.tradeName asc")
        }
    }
}
