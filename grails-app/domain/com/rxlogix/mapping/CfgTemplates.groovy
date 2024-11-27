package com.rxlogix.mapping

import com.rxlogix.SelectableList

class CfgTemplates implements SelectableList {

    BigDecimal id
    String name

    static mapping = {
        table "CFG_TEMPLATES_DSP"
        datasource "pva"

        version false
        cache usage: "read-only"

        id column: "TEMPLATE_ID", type: "big_decimal", generator: "assigned"
        name column: "NAME"
    }

    static constraints = {
        id(nullable:false, unique:true)
        name(maxSize: 100)
    }

    @Override
    def getSelectableList() {
        CfgTemplates.withTransaction {
            return CfgTemplates.findAll().unique().collect { it.name }.sort()
        }
    }
}
