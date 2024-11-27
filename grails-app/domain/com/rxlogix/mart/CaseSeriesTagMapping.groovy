package com.rxlogix.mart

class CaseSeriesTagMapping {

    String id
    Long caseId
    Long caseSeriesExecId
    String owner
    MartTags tag
    Date lastUpdated

    static constraints = {

    }

    static mapping = {
        datasource "pva"
        autoImport false
        table name: "CASE_SERIES_TAG_LIST_MAPPING"
        tag column: "TAG_ID"
        caseId column: "CASE_ID"
        id column: 'ROWID'
        caseSeriesExecId column: "CASE_SERIES_ID"
        cache: "read-only"
        version false
        lastUpdated column: "LAST_UPDATED"
    }
}
