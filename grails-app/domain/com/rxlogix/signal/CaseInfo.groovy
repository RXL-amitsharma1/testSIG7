package com.rxlogix.mapping

class CaseInfo {
    Long id
    String caseNumber
    Long version

    static constraints = {
        id bindable: true
        caseNumber nullable: true
    }

    static mapping = {
        datasource "pva"
        table "C_IDENTIFICATION"
        id generator: 'assigned', column: 'CASE_ID'
        caseNumber column: "CASE_NUM"
        version column: "VERSION_NUM"
    }
}
