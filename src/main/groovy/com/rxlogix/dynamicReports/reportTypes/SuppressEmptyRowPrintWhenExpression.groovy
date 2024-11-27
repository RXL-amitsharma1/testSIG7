package com.rxlogix.dynamicReports.reportTypes

import net.sf.dynamicreports.report.builder.expression.AbstractComplexExpression
import net.sf.dynamicreports.report.definition.ReportParameters
import net.sf.dynamicreports.report.definition.expression.DRISimpleExpression

class SuppressEmptyRowPrintWhenExpression extends AbstractComplexExpression<Boolean> {

    def addSuppressExpression(DRISimpleExpression<Boolean> expression) {
        addExpression(expression)
    }

    @Override
    Boolean evaluate(List<?> list, ReportParameters reportParameters) {
        def doSuppress = list.inject(true) {result, value ->
            result && value
        }
        return list.isEmpty() || !doSuppress
    }
}
