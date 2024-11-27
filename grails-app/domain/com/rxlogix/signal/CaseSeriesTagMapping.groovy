package com.rxlogix.signal

//Todo Remove this
class CaseSeriesTagMapping {

    Long id
    Long tagId
    Long caseId
    Long caseSeriesExecId

    static mapping = {
        datasource "pva"
        version false
        table("CASE_SERIES_TAG_MAPPING")
        id column: "ID", generator: "sequence", params:[sequence: 'CASE_SERIES_TAG_MAPPING_SEQ']
        tagId(column: "TAG_ID")
        caseId(column: "CASE_ID")
        caseSeriesExecId(column: "CASE_SERIES_EXEC_ID")
    }

}
