package com.rxlogix.config

import com.rxlogix.enums.FrequencyEnum
import com.rxlogix.user.User
import com.rxlogix.util.DbUtil
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class AppAlertProgressStatus {

    Long exStatusId
    Long executedConfigId
    String name
    Integer progressStatus
    Integer finalStatus
    Long startTime = 0L
    Long endTime = 0L
    String type
    Date dateCreated
    Date lastUpdated


    static mapping = {
        table name: "APP_ALERT_PROGRESS_STATUS"
        id column: "ID"
        exStatusId column: "EX_STATUS_ID"
        executedConfigId column: "EXECUTED_CONFIG_ID"
        name column: "NAME"
        progressStatus column: "PROGRESS_STATUS"
        finalStatus column: "FINAL_STATUS"
        startTime column: "START_TIME"
        endTime column: "END_TIME"
        type column: "TYPE"
    }

    static constraints = {
        startTime(nullable: false)
        endTime(nullable: false)
        exStatusId(nullable: false)
        executedConfigId(nullable: false)
        name(nullable: false)
        progressStatus(nullable: false)
        finalStatus(nullable: false)
        startTime(nullable: false)
        endTime(nullable: true)
        type(nullable: false)
    }

    Integer getExecutionTime() {
        if (endTime) {
            return endTime - startTime
        }
        return endTime
    }

}
