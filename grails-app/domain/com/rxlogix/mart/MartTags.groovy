package com.rxlogix.mart

class MartTags {

    String name
    String type
    Boolean isCaseSeriesTag

    static mapping = {
        datasource "pva"
        table name: "TAG_LIST"
        cache: "read-only"
        version false

        id column: "TAG_ID"
        name column: "TAG_TEXT"
        type column: "TAG_DESC"
        isCaseSeriesTag column: "TAG_TYPE"
    }

    static constraints = {
        name nullable: false
    }
}
