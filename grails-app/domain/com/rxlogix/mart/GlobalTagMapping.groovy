package com.rxlogix.mart

class GlobalTagMapping {

    String id
    Long caseId
    Date lastUpdated
    MartTags tags


    static mapping = {
        datasource "pva"
        table name: "CASE_GLOBAL_TAG_LIST_MAPPING"
        tags column: "TAG_ID"
        caseId column: "CASE_ID"
        id column: 'ROWID'
        lastUpdated column: "LAST_UPDATED"
        cache: "read-only"
        version false
    }
}
