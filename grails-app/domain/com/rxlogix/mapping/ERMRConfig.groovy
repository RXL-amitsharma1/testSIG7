package com.rxlogix.mapping

import com.rxlogix.SelectableList
import grails.util.Holders

class ERMRConfig implements SelectableList {

    Long id
    String sourceColumnName
    String targetColumnName
    String sourceDataType
    String targetDataType
    String excluded

    static mapping = {
        datasource Holders.getGrailsApplication().getConfig().signal.evdasconfig
        table "ERMR_CONFIG_MAPPING"

        cache: "read-only"
        version false

        id column: "ID"
        sourceColumnName column: "SRC_COLUMN_NAME"
        targetColumnName column: "TGT_COLUMN_NAME"
        sourceDataType column: "SRC_DATA_TYPE"
        targetDataType column: "TGT_DATA_TYPE"
        excluded column: "EXCLUDE"
    }

    static constraints = {
        id(nullable: false, unique: true)
    }

    @Override
    def getSelectableList() {
        withTransaction {
            return executeQuery("select * from ERMRConfig ec order by ec.id asc")
        }
    }
}
