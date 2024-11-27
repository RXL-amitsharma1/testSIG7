package com.rxlogix.dynamicReports;

import grails.util.Holders;
import net.sf.dynamicreports.report.base.expression.AbstractValueFormatter;
import net.sf.dynamicreports.report.definition.ReportParameters;

import java.util.regex.Pattern;

class HyperlinkColumnFormatter extends AbstractValueFormatter<Object, Object> {

    private static Pattern urlPattern = Pattern.compile(Holders.config.url.field.regex,
            Pattern.CASE_INSENSITIVE)

    @Override
    Object format(Object o, ReportParameters reportParameters) {
        if(!o){
            return o
        }
        String text = o.toString()
        text = text.replaceAll(urlPattern ) { m ->
            return "<a href='${m[0]}' target='_blank'>${m[0]}</a>"
        }
        return text
    }
}