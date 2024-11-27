package com.rxlogix.dynamicReports.reportTypes

import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression
import net.sf.dynamicreports.report.definition.ReportParameters

class SuppressEmptyValuePrintWhenExpression extends AbstractSimpleExpression<Boolean> {
    private String columnName

    SuppressEmptyValuePrintWhenExpression(String columnName) {
        this.columnName = columnName
    }

    @Override
    Boolean evaluate(ReportParameters reportParameters) {
        return reportParameters.getFieldValue(columnName) == null
    }
}
