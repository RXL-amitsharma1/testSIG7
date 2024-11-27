package com.rxlogix.config

import com.rxlogix.BaseDateRangeInformation
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.signal.SkippedAlertInformation
import com.rxlogix.util.RelativeDateConverter

class SkippedAlertDateRangeInformation extends BaseDateRangeInformation {

    int dateRangeStartAbsoluteDelta
    int dateRangeEndAbsoluteDelta

    static final MIN_DATE = "Mon Jan 01 00:00:00 UTC 1900"

    static belongsTo = [skippedAlertInformation: SkippedAlertInformation]

    Date dateCreated
    Date lastUpdated

    static mapping = {
        table name: "SKIPPED_ALERT_DATE_RANGE"

        // workaround to pull in mappings from super class that is not a domain
        def superMapping = BaseDateRangeInformation.mapping.clone()
        superMapping.delegate = delegate
        superMapping.call()

        dateRangeStartAbsoluteDelta column: "DATE_RNG_START_DELTA"
        dateRangeEndAbsoluteDelta column: "DATE_RNG_END_DELTA"
    }

    static constraints = {
        dateRangeStartAbsoluteDelta(nullable: true, validator: { val, obj ->
            if (obj.dateRangeEnum == DateRangeEnum.CUSTOM) {
                if (obj.dateRangeStartAbsoluteDelta == null) {
                    return "com.rxlogix.config.enddate.required"
                }
                if (obj.dateRangeEndAbsoluteDelta == null) {
                    return "com.rxlogix.config.startdate.required"
                }
            }
        })
        dateRangeEndAbsoluteDelta(nullable: true)
        dateRangeStartAbsolute(validator: { val, obj ->
            if (obj.dateRangeStartAbsolute > obj.dateRangeEndAbsolute) {
                return "com.rxlogix.config.enddate.greater.than.startdate.required"
            }
        })
        skippedAlertInformation nullable: true
    }

    def beforeValidate() {
        if (this.dateRangeEndAbsolute && this.dateRangeStartAbsolute && this.skippedAlertInformation) {
            this.dateRangeStartAbsoluteDelta = RelativeDateConverter.getDaysDifference(this.skippedAlertInformation.nextRunDate, this.dateRangeStartAbsolute)
            this.dateRangeEndAbsoluteDelta = RelativeDateConverter.getDaysDifference(this.skippedAlertInformation.nextRunDate, this.dateRangeEndAbsolute)
        }
        if (!DateRangeEnum.relativeDateOperatorsWithX.contains(dateRangeEnum)) {
            relativeDateRangeValue = 1
        }

    }

    List<Date> getReportStartAndEndDate(def configuration, boolean isReportCumulative = false) {
        if (isReportCumulative) {
            if (this.dateRangeEnum != DateRangeEnum.CUMULATIVE && this.dateRangeEnum != DateRangeEnum.CUSTOM) {
                DateRangeEnum relativeDateRange = this.dateRangeEnum
                if (!this.skippedAlertInformation?.nextRunDate) {
                    this.skippedAlertInformation?.nextRunDate = new Date()
                }
                return RelativeDateConverter.(relativeDateRange.value())(new Date(this.skippedAlertInformation?.nextRunDate?.getTime()), this.relativeDateRangeValue ?: 1, configuration?.configSelectedTimeZone)
            } else if (this.dateRangeEnum == DateRangeEnum.CUSTOM) {
                return [new Date(MIN_DATE), dateRangeEndAbsolute]
            }
        } else if (this.dateRangeEnum != DateRangeEnum.CUMULATIVE && this.dateRangeEnum != DateRangeEnum.CUSTOM) {
            DateRangeEnum relativeDateRange = this.dateRangeEnum
            if (!this.skippedAlertInformation?.nextRunDate) {
                this.skippedAlertInformation?.nextRunDate = new Date()
            }
            return RelativeDateConverter.(relativeDateRange.value())(new Date(this.skippedAlertInformation?.nextRunDate?.getTime()), this.relativeDateRangeValue ?: 1, configuration?.configSelectedTimeZone)
        } else if (this.dateRangeEnum == DateRangeEnum.CUSTOM) {
            return [dateRangeStartAbsolute, dateRangeEndAbsolute]
        } else { // this is default case and  when cumulative option is selected
            return [new Date(MIN_DATE), new Date()]
        }
    }
}
