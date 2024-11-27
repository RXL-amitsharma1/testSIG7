package com.rxlogix.dynamicReports.reportTypes

import net.sf.dynamicreports.report.base.expression.AbstractValueFormatter
import net.sf.dynamicreports.report.definition.ReportParameters

/**
 * The formatter truncates data if length is more than 32 767 characters
 * https://support.office.com/en-us/article/Excel-specifications-and-limits-1672b34d-7043-467e-8e27-269d656771c3
 * See the https://rxlogixdev.atlassian.net/browse/PVR-5828 for more details
 */
class MaxLengthColumnFormatter extends AbstractValueFormatter<String, String> {
    private int maxLength
    private String truncateText

    MaxLengthColumnFormatter(int maxLength, truncateText = null) {
        this.maxLength = maxLength ?: Integer.MAX_VALUE
        this.truncateText = truncateText ?: "..."
    }

    String format(String value, ReportParameters reportParameters) {
        if (value != null && value.length() > maxLength) {
            return value.substring(0, maxLength - truncateText.length()) + truncateText
        }
        return value;
    }
}
