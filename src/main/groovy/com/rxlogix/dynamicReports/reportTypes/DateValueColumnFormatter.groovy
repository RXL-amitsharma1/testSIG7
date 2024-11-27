package com.rxlogix.dynamicReports.reportTypes

import net.sf.dynamicreports.report.base.expression.AbstractValueFormatter
import net.sf.dynamicreports.report.definition.ReportParameters

import java.text.DateFormat
import java.text.SimpleDateFormat

/**
 * The formatter formats and then parses a date against a given string format.
 * It will remove timestamp from date object if formatter does not contains the timestamp
 * See the https://rxlogixdev.atlassian.net/browse/PVR-2809 for more details
 */
class DateValueColumnFormatter extends AbstractValueFormatter<Date, Date> {
    private DateFormat dateFormat;

    public DateValueColumnFormatter(String pattern) {
        this.dateFormat = new SimpleDateFormat(pattern)
    }

    public Date format(Date value, ReportParameters reportParameters) {
        if (value != null) {
            return dateFormat.parse(dateFormat.format(value))
        }
        return value;
    }
}
