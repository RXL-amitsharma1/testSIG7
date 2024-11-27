package com.rxlogix.config

class SpotfireJobInstances {

    String jobId
    Long executedConfigId
    String executionStatus
    Date dateCreated
    String jobContent
    String fileName
    String type

    static constraints = {
        jobId blank: false, maxSize: 50
        jobContent type: 'text', sqlType: 'clob'
        executionStatus nullable: true
        executedConfigId nullable: true
        jobContent nullable: true
        fileName nullable: true
        type nullable: true
    }

    static mapping = {
        table name: "SPOTFIRE_JOB_INSTANCES"
        jobId column: "JOB_ID"
        jobContent column: "JOB_CONTENT"
        executionStatus column: "EXECUTION_STATUS"
        fileName column: "FILE_NAME"
        executedConfigId column: "EXECUTED_CONFIG_ID"
        type column: "TYPE"

    }
}
