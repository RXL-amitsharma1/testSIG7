package com.rxlogix.config
import com.rxlogix.user.User
import com.rxlogix.util.Strings;

class ReportResult {
    static auditable = true

    ReportResultData data
    Date dateCreated
    Date lastUpdated
    User scheduledBy
    int sequenceNo = 0
    ReportExecutionStatus executionStatus
    Date runDate = dateCreated
    String frequency
    // timing in ms for each SQL stage and cumulative
    Long totalTime = 0L
    Long versionTime = 0L
    Long filterVersionTime = 0L
    Long queryTime = 0L
    Long reportTime = 0L
    Long reAssessTime =0L

    // row counts for each SQL stage and cumulative
    Long versionRows = 0L
    Long versionRowsFilter = 0L
    Long queryRows = 0L
    Long reportRows = 0L

    ExecutedTemplateQuery executedTemplateQuery

    static belongsTo = [templateQuery: TemplateQuery]

    static mapping = {
        table name: "RPT_RESULT"

        data column: "RPT_RESULT_DATA_ID"
        scheduledBy column: "SCHEDULED_PVUSER_ID"
        sequenceNo column: "SEQUENCE"
        executionStatus column: "EX_STATUS"
        runDate column: "RUN_DATE"
        frequency column: "FREQUENCY"
        totalTime column: "TOTAL_TIME"
        versionTime column: "VERSION_TIME"
        filterVersionTime column: "FILTER_VERSION_TIME"
        reAssessTime column: "REASSESS_TIME"
        queryTime column: "QUERY_TIME"
        reportTime column: "REPORT_TIME"
        versionRows column: "VERSION_ROWS"
        versionRowsFilter column: "FILTERED_VERSION_ROWS"
        queryRows column: "QUERY_ROWS"
        reportRows column: "REPORT_ROWS"
        executedTemplateQuery column: "EX_TEMPLT_QUERY_ID"
        templateQuery column: "TEMPLT_QUERY_ID"
    }

    static constraints = {
        data(nullable:true)
        runDate(nullable: true)
        frequency(nullable: true)
        executedTemplateQuery(nullable: true)
    }

    def beforeInsert = {
        sequenceNo ++
    }

    boolean isViewableBy(User currentUser) {
        return (currentUser.isAdmin() || scheduledBy == currentUser)
    }

    String toString() {
        "[ReportResult = " +
                " data->${data}" +
                " dateCreated->${dateCreated}" +
                " lastUpdated->${lastUpdated}" +
                " scheduledBy->${scheduledBy}" +
                " sequenceNo->${sequenceNo}" +
                " runDate->${runDate}" +
                " frequency->${frequency}" +
                " totalTime->${totalTime} \n" +
                " executionStatus->${ Strings.trunc(executionStatus.toString(),555) } \n" +
                " executedTemplateQuery->${ Strings.trunc(executedTemplateQuery.toString(),555) }" +
                "]"
    }
}
