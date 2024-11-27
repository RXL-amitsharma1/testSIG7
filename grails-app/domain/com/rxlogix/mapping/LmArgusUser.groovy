package com.rxlogix.mapping

import com.rxlogix.SelectableList
import grails.gorm.transactions.Transactional

class LmArgusUser implements SelectableList {
    BigDecimal id
    String fullname
    String name

    static mapping = {
        table "VW_USERNAME_DSP"
        datasource "pva"

        version false
        cache usage: "read-only"

        id column: "USER_ID", type: "big_decimal", generator: "assigned"
        fullname column: "USER_FULLNAME"
        name column: "USER_NAME"
    }

    static constraints = {
        id(nullable:false)
        fullname(nullable:true, maxSize: 64)
        name(nullable:true, maxSize: 64)
    }

    @Override
    def getSelectableList() {
        LmArgusUser.withTransaction {
            return LmArgusUser.findAll().unique().collect { it.fullname }.sort()
        }
    }
}
