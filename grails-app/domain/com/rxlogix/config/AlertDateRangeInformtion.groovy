package com.rxlogix.config

import com.rxlogix.BaseDateRangeInformation
import com.rxlogix.Constants
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.RelativeDateConverter
import grails.gorm.dirty.checking.DirtyCheck
import grails.plugins.orm.auditable.ChildModuleAudit

@DirtyCheck
@ChildModuleAudit(parentClassName = ['configuration'])
class AlertDateRangeInformation extends BaseDateRangeInformation {

    static auditable = ['ignoreEvents': ["onSave"],'ignore':['dateRangeStartAbsoluteDelta','dateRangeEndAbsoluteDelta','dateRangeStartAbsolute','dateRangeEndAbsolute','alertConfiguration']]
    int dateRangeStartAbsoluteDelta
    int dateRangeEndAbsoluteDelta

    static final MIN_DATE = "Mon Jan 01 00:00:00 UTC 1900"

    static belongsTo = [alertConfiguration: Configuration]

    static mapping = {
        table name: "ALERT_DATE_RANGE"

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
            if(obj.dateRangeStartAbsolute > obj.dateRangeEndAbsolute){
                return "com.rxlogix.config.enddate.greater.than.startdate.required"
            }
        })
    }

    def beforeValidate() {
        if (this.dateRangeEndAbsolute && this.dateRangeStartAbsolute) {
            this.dateRangeStartAbsoluteDelta = RelativeDateConverter.getDaysDifference(this.alertConfiguration.nextRunDate, this.dateRangeStartAbsolute)
            this.dateRangeEndAbsoluteDelta = RelativeDateConverter.getDaysDifference(this.alertConfiguration.nextRunDate, this.dateRangeEndAbsolute)
        }
        if (!DateRangeEnum.relativeDateOperatorsWithX.contains(dateRangeEnum)) {
            relativeDateRangeValue = 1
        }

    }

    List<Date> getReportStartAndEndDate(boolean isReportCumulative = false) {
        if (isReportCumulative) {
            if (this.dateRangeEnum != DateRangeEnum.CUMULATIVE && this.dateRangeEnum != DateRangeEnum.CUSTOM) {
                DateRangeEnum relativeDateRange = this.dateRangeEnum
                Date nextRunDate = alertConfiguration?.nextRunDate ?: new Date()
                return RelativeDateConverter.(relativeDateRange.value())(new Date(nextRunDate?.getTime()), this.relativeDateRangeValue ?: 1, alertConfiguration?.configSelectedTimeZone)
            } else if (this.dateRangeEnum == DateRangeEnum.CUSTOM) {
                return [new Date(MIN_DATE), dateRangeEndAbsolute]
            }
        } else if (this.dateRangeEnum != DateRangeEnum.CUMULATIVE && this.dateRangeEnum != DateRangeEnum.CUSTOM) {
            DateRangeEnum relativeDateRange = this.dateRangeEnum
            Date nextRunDate = alertConfiguration?.nextRunDate ?: new Date()
            return RelativeDateConverter.(relativeDateRange.value())(new Date(nextRunDate?.getTime()), this.relativeDateRangeValue ?: 1, alertConfiguration?.configSelectedTimeZone)
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
            Date nextRunDate = alertConfiguration?.nextRunDate ?: new Date()
            dateRange =  RelativeDateConverter.(relativeDateRange.value())(new Date(nextRunDate?.getTime()), this.relativeDateRangeValue ?: 1, alertConfiguration?.configSelectedTimeZone)
        } else if (this.dateRangeEnum == DateRangeEnum.CUSTOM) {
            dateRange = [dateRangeStartAbsolute, dateRangeEndAbsolute]
        } else { // this is default case and  when cumulative option is selected
            dateRange = [new Date(MIN_DATE), new Date()]
        }
        return AuditLogConfigUtil.getDateListFormated(dateRange,"dd-MMM-yyyy")
    }

    def getModuleNameForMultiUseDomains() {
        return this.alertConfiguration.getModuleNameForMultiUseDomains()
    }

}
