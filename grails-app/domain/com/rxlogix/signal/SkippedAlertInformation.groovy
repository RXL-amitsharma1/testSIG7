package com.rxlogix.signal

import com.rxlogix.Constants
import com.rxlogix.config.Configuration
import com.rxlogix.config.SkippedAlertDateRangeInformation
import com.rxlogix.enums.AdjustmentTypeEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.SkippedAlertStateEnum

class SkippedAlertInformation {

    Long configId
    String alertType

    SkippedAlertDateRangeInformation skippedAlertDateRangeInformation

    EvaluateCaseDateEnum evaluateDateAs
    Date asOfVersionDate
    Date nextRunDate

    Long exConfigId
    AdjustmentTypeEnum adjustmentTypeEnum
    SkippedAlertStateEnum stateEnum = SkippedAlertStateEnum.CREATED

    String groupCode
    String alertDisableReason

    String createdBy = Constants.SYSTEM_USER
    Date dateCreated
    Date lastUpdated
    String modifiedBy = Constants.SYSTEM_USER

    static mapping = {
        table name: "SKIPPED_ALERT_INFORMATION"
        skippedAlertDateRangeInformation column: "SKIPPED_ALERT_DATE_RNG_ID"
        stateEnum column: "STATE_ENUM"
    }

    static constraints = {
        evaluateDateAs nullable: true
        asOfVersionDate nullable: true
        nextRunDate nullable: true
        exConfigId nullable: true
        groupCode nullable: true
        adjustmentTypeEnum nullable: true
        alertDisableReason nullable: true
    }

    SkippedAlertInformation(Configuration configuration, String alertType, Boolean isFirstSkippedExecution = false) {
        this.configId = configuration.id
        this.alertType = alertType


        this.evaluateDateAs = configuration?.evaluateDateAs
        this.skippedAlertDateRangeInformation = new SkippedAlertDateRangeInformation(configuration.alertDateRangeInformation.properties)

        if (isFirstSkippedExecution) {
            this.nextRunDate = configuration.nextRunDate
            this.asOfVersionDate = configuration?.asOfVersionDate
        }
    }

}
