package com.rxlogix.config

import com.rxlogix.BaseDateRangeInformation
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.signal.References
import com.rxlogix.util.RelativeDateConverter
import grails.gorm.dirty.checking.DirtyCheck
import org.springframework.context.MessageSource

@DirtyCheck
class DateRangeInformation extends BaseDateRangeInformation {
    def customMessageService

    static auditable = false

    int dateRangeStartAbsoluteDelta
    int dateRangeEndAbsoluteDelta

    static final MIN_DATE = "Mon Jan 01 00:00:00 UTC 1900"

    static belongsTo = [templateQuery:TemplateQuery]

    static mapping = {
        table name: "DATE_RANGE"

        // workaround to pull in mappings from super class that is not a domain
        def superMapping = BaseDateRangeInformation.mapping.clone()
        superMapping.delegate = delegate
        superMapping.call()

        dateRangeStartAbsoluteDelta column: "DATE_RNG_START_DELTA"
        dateRangeEndAbsoluteDelta column: "DATE_RNG_END_DELTA"
    }

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

    def beforeValidate() {
            if(this.dateRangeEndAbsolute && this.dateRangeStartAbsolute) {
                this.dateRangeStartAbsoluteDelta = RelativeDateConverter.getDaysDifference(this.templateQuery.report.nextRunDate, this.dateRangeStartAbsolute)
                this.dateRangeEndAbsoluteDelta = RelativeDateConverter.getDaysDifference(this.templateQuery.report.nextRunDate, this.dateRangeEndAbsolute)
            }
            if(!relativeDateRangeValue && !DateRangeEnum.relativeDateOperatorsWithX.contains(dateRangeEnum) ) {
                relativeDateRangeValue = 1
            }

    }

    def  getReportStartAndEndDate() {
        if (this.dateRangeEnum == DateRangeEnum.PR_DATE_RANGE) {
            return templateQuery?.report?.alertDateRangeInformation?.reportStartAndEndDate
        }
        if(this.dateRangeEnum != DateRangeEnum.CUMULATIVE && this.dateRangeEnum != DateRangeEnum.CUSTOM) {

                DateRangeEnum relativeDateRange = this.dateRangeEnum
                if(!templateQuery?.report?.nextRunDate) {
                    templateQuery?.report?.nextRunDate = new Date()
                }
                return RelativeDateConverter.(relativeDateRange.value())(new java.util.Date(templateQuery?.report?.nextRunDate?.getTime()), this.relativeDateRangeValue ?: 1, templateQuery?.report?.configSelectedTimeZone)
        }
        else if (this.dateRangeEnum == DateRangeEnum.CUSTOM) {
            return [dateRangeStartAbsolute, dateRangeEndAbsolute]
        } else { // this is default case and  when cumulative option is selected
            return [new Date(MIN_DATE), new Date()]
        }
    }

    @Override
    String toString() {
        "${this.getClass().getSimpleName()} : ${this.id}"
    }

    def getInstanceIdentifierForAuditLog() {
        return customMessageService.getMessage("${this.dateRangeEnum.getI18nKey()}")
    }
}
