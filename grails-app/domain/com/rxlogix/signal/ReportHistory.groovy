package com.rxlogix.signal

import com.rxlogix.util.AlertUtil
import com.rxlogix.Constants
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil

class ReportHistory implements AlertUtil{

    static auditable = [ignore:['memoReport','isReportGenerated','version', 'lastUpdated', 'lastUpdatedBy', 'dateCreated', 'createdBy','modifiedBy','updatedBy']]

    String reportName
    String productName
    String productGroup
    Date startDate
    Date endDate
    String reportType
    String dataSource

    Date dateCreated = new Date()
    User updatedBy

    byte[] memoReport

    Boolean isReportGenerated = false

    static mapping = {
        memoReport sqlType: 'blob'
    }
    static constraints = {
        memoReport nullable: true
        isReportGenerated nullable: true
        reportName unique: true,nullable: true, maxSize: 8000, validator: { value, object ->
            validateRequiredField(value)
            return MiscUtil.validator(value, "Report Name", Constants.SpecialCharacters.DEFAULT_CHARS as String[])
        }
        productName nullable: true, maxSize: 8000
        productGroup nullable: true, maxSize: 8000
        startDate(nullable: false, validator: { val, obj ->
            if (obj.startDate > obj.endDate) {
                return "com.rxlogix.config.startdate.greater.than.enddate.required"
            }
        })
        endDate(nullable: false, validator: { val, obj ->
            if (obj.endDate < obj.startDate) {
                return "com.rxlogix.config.enddate.less.than.startdate.required"
            }
        })
    }

    private static validateRequiredField(value) {
        if (!value) {
            return "app.label.reporting.all.fields.required"
        }
    }

    Map toDto(String timezone) {
        String dataSourceLabel = getDataSource(this.dataSource)
        [
                reportName      : this.reportName,
                productName     : this.productGroup ? this.productGroup : this.productName,
                productGroup    : this.productGroup,
                summaryDateRange: "${DateUtil.toDateString1(this.startDate)} to ${DateUtil.toDateString1(this.endDate)}",
                generatedBy     : this.updatedBy.fullName,
                generatedOn     : DateUtil.stringFromDate(this.dateCreated, Constants.DateFormat.STANDARD_DATE_WITH_TIME, timezone),
                downloadId      : this.id,
                reportType      : this.reportType,
                dataSource      : dataSourceLabel,
                reportGenerated : this.isReportGenerated != null ? this.isReportGenerated : null
        ]
    }

    def getInstanceIdentifierForAuditLog() {
        return this.reportName + " (" + this.reportType + ")"
    }
}
