package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmLitCitations implements SelectableList {

    BigDecimal id
    String name

    static mapping = {
        table "VW_LLCI_JOURNAL_DSP"
        datasource "pva"

        version false
        cache usage: "read-only"

        id column: "LITERATURE_ID", type: "big_decimal", generator: "assigned"
        name column: "JOURNAL"
    }

    static constraints = {
        id(nullable:false, unique:true)
        name(maxSize: 250)
    }

    @Override
    def getSelectableList() {
        LmLitCitations.withTransaction {
            return LmLitCitations.findAll().unique().collect { it.name }.sort()
        }
    }
}
