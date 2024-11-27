package com.rxlogix.config

import com.rxlogix.enums.FrequencyEnum
import com.rxlogix.enums.ReportFormat
import com.rxlogix.user.User
import com.rxlogix.util.DbUtil
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class ExecutionStatus {

    Long configId
    Long executedConfigId
    ReportExecutionStatus executionStatus = ReportExecutionStatus.GENERATING
    Long startTime = 0L
    Long endTime = 0L
    Long reportVersion
    String stackTrace
    Date nextRunDate
    User owner
    FrequencyEnum frequency
    String name
    String type
    ReportExecutionStatus reportExecutionStatus
    ReportExecutionStatus spotfireExecutionStatus
    Integer executionLevel = 0
    String alertFilePath

    Date dateCreated
    Date lastUpdated
    String timeStampJSON

    String spotfireFileName
    String nodeName
    Boolean isMaster = false
    Boolean isClearDataMining = false


    static mapping = {
        table name: "EX_STATUS"
        id column: "ID"
        startTime column: "START_TIME"
        endTime column: "END_TIME"
        reportVersion column: "RPT_VERSION"
        stackTrace column: "STACK_TRACE", sqlType: DbUtil.longStringType
        frequency column: "FREQUENCY"
        executionStatus column: "EX_STATUS"
        configId column: "CONFIG_ID"

    }

    static constraints = {
        startTime(nullable: true)
        endTime(nullable: true)
        frequency(nullable: true)
        stackTrace(maxSize: 32 * 1024, nullable: true)
        executedConfigId nullable: true
        reportExecutionStatus nullable: true
        spotfireExecutionStatus nullable: true
        alertFilePath nullable: true
        spotfireFileName nullable: true,maxSize: 2000
        type nullable: true
        executedConfigId nullable: true
        timeStampJSON nullable: true
        nodeName nullable: true
        isMaster nullable: true
        isClearDataMining nullable: true
    }

    Integer getExecutionTime() {
        if (endTime) {
            return endTime - startTime
        }
        if (this.type == AlertType.AGGREGATE_CASE_ALERT.value() || this.type == AlertType.SINGLE_CASE_ALERT.value()) {
            return Configuration.get(configId)?.expectedExecutionTime
        } else if (this.type == AlertType.EVDAS_ALERT.value()) {
            return EvdasConfiguration.get(configId).expectedExecutionTime
        } else {
            return LiteratureConfiguration.get(configId).expectedExecutionTime
        }
    }

}
