package com.rxlogix.config

import com.rxlogix.BaseDateRangeInformation
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.RelativeDateConverter
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class ExecutedLiteratureDateRangeInformation extends BaseDateRangeInformation {

    static auditable = false

    static final MIN_DATE = "Mon Jan 01 00:00:00 UTC 1900"

    static mapping = {
        table name: "EX_LITERATURE_DATE_RANGE"

        // workaround to pull in mappings from super class that is not a domain
        def superMapping = BaseDateRangeInformation.mapping.clone()
        superMapping.delegate = delegate
        superMapping.call()
    }

    static belongsTo = [literatureConfiguration : LiteratureConfiguration]

    static constraints = {
    }

    def  getReportStartAndEndDate() {

        if(this.dateRangeEnum != DateRangeEnum.CUMULATIVE && this.dateRangeEnum != DateRangeEnum.CUSTOM) {

            DateRangeEnum relativeDateRange = this.dateRangeEnum
            Date nextRunDate = literatureConfiguration?.nextRunDate ?: new Date()
            return RelativeDateConverter.(relativeDateRange.value())(new java.util.Date(nextRunDate?.getTime()), this.relativeDateRangeValue ?: 1, this.literatureConfiguration?.configSelectedTimeZone)
        } else if (this.dateRangeEnum == DateRangeEnum.CUSTOM) {
            return [dateRangeStartAbsolute, dateRangeEndAbsolute]
        } else { // this is default case and  when cumulative option is selected
            return [new Date(MIN_DATE), new Date()]
        }
    }

    @Override
    String toString() {
        List dateRange = []
        if (this.dateRangeEnum != DateRangeEnum.CUMULATIVE && this.dateRangeEnum != DateRangeEnum.CUSTOM) {
            DateRangeEnum relativeDateRange = this.dateRangeEnum
            Date nextRunDate = literatureConfiguration?.nextRunDate ?: new Date()
            dateRange =  RelativeDateConverter.(relativeDateRange.value())(new Date(nextRunDate?.getTime()), this.relativeDateRangeValue ?: 1, literatureConfiguration?.configSelectedTimeZone)
        } else if (this.dateRangeEnum == DateRangeEnum.CUSTOM) {
            dateRange = [dateRangeStartAbsolute, dateRangeEndAbsolute]
        } else { // this is default case and  when cumulative option is selected
            dateRange = [new Date(MIN_DATE),dateRangeEndAbsolute]
        }
        return AuditLogConfigUtil.getDateListFormated(dateRange,"dd-MMM-yyyy")
    }
}
