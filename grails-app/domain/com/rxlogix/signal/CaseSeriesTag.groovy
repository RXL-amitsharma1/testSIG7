package com.rxlogix.signal

class CaseSeriesTag {

    Long id
    Long tagId
    String name

    static mapping = {
        datasource "pva"
        version false
        table("CASE_SERIES_TAGS")
        id column: "ID", generator: "sequence", params:[sequence: 'CASE_SERIES_TAG_SEQ']
        tagId(column: "TAG_ID")
        name(column: "NAME")
    }

}
