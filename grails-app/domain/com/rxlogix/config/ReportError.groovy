package com.rxlogix.config

class ReportError {
    static auditable = false
    ReportExecutionStatus executionStatus
    String message

    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    Configuration configuration
    ExecutedConfiguration executedConfiguration
    ExecutedTemplateQuery executedTemplateQuery
    ReportResult reportResult

    static mapping = {
        table name: "RPT_ERROR"
        id column: "ID"

        executionStatus column: "EX_STATUS"
        message column: "MESSAGE"
        configuration column: "RCONFIG_ID"
        executedConfiguration column: "EX_RCONFIG_ID"
        executedTemplateQuery column: "EX_TEMPLT_QUERY_ID"
        reportResult column: "RPT_RESULT_ID"
    }

    static constraints = {
        configuration(nullable: false)
        executedTemplateQuery(nullable: false)
        executedConfiguration(nullable: false)
        reportResult(nullable: true)
        message(nullable: false)
        createdBy(nullable: false, maxSize: 100)
        modifiedBy(nullable: false, maxSize: 100)
    }
}
