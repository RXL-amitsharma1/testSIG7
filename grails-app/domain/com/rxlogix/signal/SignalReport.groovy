package com.rxlogix.signal

import com.rxlogix.config.ReportExecutionStatus

class SignalReport {

    String reportName
    String userName
    long userId
    String linkUrl
    ReportExecutionStatus reportExecutionStatus = ReportExecutionStatus.GENERATING

    String type
    String typeFlag
    Long alertId
    Long executedAlertId
    //The reports
    Long reportId       //Executed Report Id in PVR DB
    byte[] pdfReport
    byte[] wordReport
    byte[] excelReport

    //Common parameters
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    Boolean isAlertReport = Boolean.FALSE
    boolean isGenerating

    static mapping = {
        table name: "SIGNAL_REPORT"
        pdfReport sqlType: 'blob'
        wordReport sqlType: 'blob'
        excelReport sqlType: 'blob'
        reportId column: "REPORT_ID"
        executedAlertId column: "EXECUTED_ALERT_ID"
        isAlertReport column: "IS_ALERT_REPORT"
    }

    static constraints = {
        pdfReport nullable: true
        wordReport nullable: true
        excelReport nullable: true
        type nullable: true
        typeFlag nullable: true
        alertId nullable: true
        reportId nullable: true
        executedAlertId nullable: true
        reportExecutionStatus nullable: true
    }

}
