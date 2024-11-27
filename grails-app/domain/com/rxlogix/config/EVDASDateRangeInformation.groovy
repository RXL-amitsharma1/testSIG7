package com.rxlogix.config

import com.rxlogix.BaseDateRangeInformation
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.RelativeDateConverter
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class EVDASDateRangeInformation extends BaseDateRangeInformation {
    static auditable = false

    int dateRangeStartAbsoluteDelta
    int dateRangeEndAbsoluteDelta

    static final MIN_DATE = "Mon Jan 01 00:00:00 UTC 1900"

    static mapping = {
        table name: "EVDAS_DATE_RANGE"

        // workaround to pull in mappings from super class that is not a domain
        def superMapping = BaseDateRangeInformation.mapping.clone()
        superMapping.delegate = delegate
        superMapping.call()

        dateRangeStartAbsoluteDelta column: "DATE_RNG_START_DELTA"
        dateRangeEndAbsoluteDelta column: "DATE_RNG_END_DELTA"
    }

    static belongsTo = [evdasConfiguration : EvdasConfiguration]

    static constraints = {
        dateRangeStartAbsoluteDelta(nullable:true, validator: { val, obj ->
            if (obj.dateRangeEnum == DateRangeEnum.CUSTOM) {
                if(obj.dateRangeStartAbsoluteDelta == null)
                {
                    return "com.rxlogix.config.enddate.required"
                }
                if(obj.dateRangeEndAbsoluteDelta == null)
                {
                    return "com.rxlogix.config.startdate.required"
                }
            }
        })
        dateRangeEndAbsoluteDelta(nullable:true)
    }

    def  getReportStartAndEndDate() {

        if(this.dateRangeEnum != DateRangeEnum.CUMULATIVE && this.dateRangeEnum != DateRangeEnum.CUSTOM) {

            DateRangeEnum relativeDateRange = this.dateRangeEnum
            Date nextRunDate = evdasConfiguration?.nextRunDate ?: new Date()
            return RelativeDateConverter.(relativeDateRange.value())(new java.util.Date(nextRunDate?.getTime()), this.relativeDateRangeValue ?: 1, this.evdasConfiguration?.configSelectedTimeZone)
        } else if (this.dateRangeEnum == DateRangeEnum.CUSTOM) {
            return [dateRangeStartAbsolute, dateRangeEndAbsolute]
        } else { // this is default case and  when cumulative option is selected
            return [new Date(MIN_DATE), new Date()]
        }
    }

    def beforeValidate() {
        if(this.dateRangeEndAbsolute && this.dateRangeStartAbsolute) {
            this.dateRangeStartAbsoluteDelta = RelativeDateConverter.getDaysDifference(this.evdasConfiguration?.nextRunDate, this.dateRangeStartAbsolute)
            this.dateRangeEndAbsoluteDelta = RelativeDateConverter.getDaysDifference(this.evdasConfiguration?.nextRunDate, this.dateRangeEndAbsolute)
        }
        if(!relativeDateRangeValue && !DateRangeEnum.getEudraDateOperators().contains(dateRangeEnum) ) {
            relativeDateRangeValue = 1
        }

    }

    @Override
    String toString() {
        List dateRange = []
        if(this.dateRangeEnum != DateRangeEnum.CUMULATIVE && this.dateRangeEnum != DateRangeEnum.CUSTOM) {
            DateRangeEnum relativeDateRange = this.dateRangeEnum
            Date nextRunDate = evdasConfiguration?.nextRunDate ?: new Date()
            dateRange = RelativeDateConverter.(relativeDateRange.value())(new java.util.Date(nextRunDate?.getTime()), this.relativeDateRangeValue ?: 1, this.evdasConfiguration?.configSelectedTimeZone)
        } else if (this.dateRangeEnum == DateRangeEnum.CUSTOM) {
            dateRange = [dateRangeStartAbsolute, dateRangeEndAbsolute]
        } else { // this is default case and  when cumulative option is selected
            dateRange = [new Date(MIN_DATE), new Date()]
        }
        return AuditLogConfigUtil.getDateListFormated(dateRange,"dd-MMM-yyyy")
    }
}
