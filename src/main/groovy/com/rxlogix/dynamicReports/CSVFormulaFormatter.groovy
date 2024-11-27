package com.rxlogix.dynamicReports

import com.rxlogix.Constants
import net.sf.dynamicreports.report.base.expression.AbstractValueFormatter
import net.sf.dynamicreports.report.definition.ReportParameters

class CSVFormulaFormatter extends AbstractValueFormatter<String, String> {
    @Override
    String format(String value, ReportParameters reportParameters) {
        List<String> formulaChars = ['=', '+', '-', '@']
        if(value && value instanceof String) {
            if (value.equals(Constants.Commons.DASH_STRING)) {
                return Constants.Commons.BLANK_STRING
            }
            if (formulaChars.contains(value[0])) {
                return "'$value"
            }
        }
        return value
    }
}

