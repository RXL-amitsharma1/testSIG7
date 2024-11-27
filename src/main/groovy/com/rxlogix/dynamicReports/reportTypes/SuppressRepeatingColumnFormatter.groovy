package com.rxlogix.dynamicReports.reportTypes

import net.sf.dynamicreports.report.base.expression.AbstractValueFormatter
import net.sf.dynamicreports.report.builder.group.ColumnGroupBuilder
import net.sf.dynamicreports.report.definition.ReportParameters
import net.sf.dynamicreports.report.definition.expression.DRISimpleExpression

/**
 * Created by gologuzov on 04.05.16.
 */
class SuppressRepeatingColumnFormatter extends AbstractValueFormatter<Object, Object> {

    DRISimpleExpression<Boolean> expression

    public SuppressRepeatingColumnFormatter(ColumnGroupBuilder group, String columnName) {
        this.expression = new SuppressRepeatingColumnPrintWhenExpression(group, columnName)
    }

    public SuppressRepeatingColumnFormatter(DRISimpleExpression<Boolean> expression) {
        this.expression = expression
    }

    @Override
    Object format(Object currentValue, ReportParameters reportParameters) {
        if (expression.evaluate(reportParameters)) {
            return ""
        }
        return currentValue
    }
}
