package com.rxlogix.dynamicReports.charts

//import com.rxlogix.ChartOptionsUtils
import net.sf.jasperreports.engine.JRGenericPrintElement
import net.sf.jasperreports.engine.export.GenericElementHtmlHandler
import net.sf.jasperreports.engine.export.JRHtmlExporterContext
import org.apache.commons.lang.StringEscapeUtils

public class HtmlChartHandler implements GenericElementHtmlHandler {

    @Override
    public boolean toExport(JRGenericPrintElement element) {
        return true;
    }

    @Override
    public String getHtmlFragment(JRHtmlExporterContext exporterContext, JRGenericPrintElement element) {
        Object chartData = ((ChartGenerator) element.getParameterValue(ChartGenerator.PARAMETER_CHART_GENERATOR)).generateChart()

        StringBuffer result = new StringBuffer()
        result.append("<div id=\"${element.hashCode()}\" style=\"width:100%; min-height:400px;\" data=\"${/*StringEscapeUtils.escapeHtml(ChartOptionsUtils.serialize(chartData))*/}\"></div>\n")
        result.append("<script>\$(function () { \n" +
                "    var container = \$('#${element.hashCode()}'); \n" +
                "    var options = JSON.parse(container.attr(\"data\"),function(key, value) {\n" +
                "        if (key === \"formatter\") {\n" +
                "            value = eval(\"(\" + value + \")\")\n" +
                "        }\n" +
                "        return value;\n" +
                "    });\n" +
                "    container.highcharts(options);" +
                "});</script>")
        return result.toString()
    }
}
