package com.rxlogix.dynamicReports.charts

import net.sf.jasperreports.engine.JRGenericPrintElement
import net.sf.jasperreports.engine.export.JRHtmlExporterContext

public class XhtmlChartHandler extends HtmlChartHandler {

    @Override
    public String getHtmlFragment(JRHtmlExporterContext exporterContext, JRGenericPrintElement element) {
        StringBuilder result = new StringBuilder()
        result.append("<div style=\"position: absolute left: ")
        result.append(element.getX() + "px top: ")
        result.append(element.getY() + "px width: ")
        result.append(element.getWidth() + "px height: ")
        result.append(element.getHeight() + "px\">")
        result.append(Object.getHtmlFragment(exporterContext, element))
        result.append("</div>")
        return result.toString()
    }
}
