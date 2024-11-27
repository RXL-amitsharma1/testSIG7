package com.rxlogix.dynamicReports

import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression
import net.sf.dynamicreports.report.builder.group.ColumnGroupBuilder
import net.sf.dynamicreports.report.definition.ReportParameters

import static net.sf.dynamicreports.report.builder.DynamicReports.exp


class SuppressRepeatingColumnPrintWhenExpression extends AbstractSimpleExpression<Boolean> {

    ColumnGroupBuilder group;
    String columnName;
    def previousValue;


    public SuppressRepeatingColumnPrintWhenExpression(ColumnGroupBuilder group, String columnName) {
        this.group = group;
        this.columnName = columnName;
    }

    public Boolean evaluate(ReportParameters reportParameters) {
        if (group) {
            int groupNumber = exp.groupRowNumber(group)?.evaluate(reportParameters)
            def currentValue = reportParameters.getFieldValue(columnName)
            boolean suppress = groupNumber == 1 || currentValue != this.previousValue
            this.previousValue = currentValue
            return suppress
        } else {
            return true
        }
    }
}
