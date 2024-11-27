package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmDatasheet implements SelectableList {

    BigDecimal id
    String sheetName

    static mapping = {
        datasource "pva"
        table "VW_DATASHEET_NAME_DSP"

        cache: "read-only"
        version false

        id column: "DATASHEET_ID", type: "big_decimal", generator: "assigned"
        sheetName column: "SHEET_NAME"
    }

    static constraints = {
        id(nullable:false) //not unique, can have revisions
        sheetName(blank:false, maxSize:40)

    }

    @Override
    def getSelectableList() {
        LmDatasheet.withTransaction {
            return LmDatasheet.findAll().unique().collect { it.sheetName }.sort()
        }
    }
}
