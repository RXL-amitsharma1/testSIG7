package com.rxlogix.dynamicReports.reportTypes

import com.rxlogix.DynamicReportService
import com.rxlogix.config.ReportResult
import com.rxlogix.dynamicReports.SuppressRepeatingColumnPrintWhenExpression
import com.rxlogix.dynamicReports.Templates
import com.rxlogix.enums.ReportFormat
import grails.converters.JSON
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.builder.group.ColumnGroupBuilder
import net.sf.dynamicreports.report.builder.style.ConditionalStyleBuilder
import net.sf.dynamicreports.report.builder.style.StyleBuilder
import net.sf.dynamicreports.report.constant.GroupHeaderLayout
import net.sf.dynamicreports.report.definition.ReportParameters
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject

import static net.sf.dynamicreports.report.builder.DynamicReports.*

class CrosstabReportBuilder {

    DynamicReportService dynamicReportService = Holders.applicationContext.getBean("dynamicReportService")

    public void createCrosstabReport(ReportResult reportResult, JasperReportBuilder report, Map params) {
        //Note: this is a manually created crosstab report that is closer in structure to a case line listing report
        addCrosstabColumns(reportResult, report, params)
    }

    private void addCrosstabColumns(ReportResult reportResult, JasperReportBuilder report, Map params) {
        JSONArray tabHeaders = (JSONArray) JSON.parse(reportResult.data.crossTabHeader)

        ColumnGroupBuilder singleColumnGroup

        List<String> fields = new ArrayList<String>()
        for (JSONObject header : tabHeaders) {
            String columnLabel = header.entrySet().getAt(0).value
            String columnValue = header.entrySet().getAt(0).key
            fields.add(columnValue)

            if (params.outputFormat == ReportFormat.XLSX.name()) {
                report.addColumn(Columns.column(columnLabel, columnValue, type.stringType()))
            } else {

                ConditionalStyleBuilder totalOrSubtotalStyle = stl.conditionalStyle(new TotalOrSubtotalRow(fields)).bold().setBackgroundColor(Templates.subTotalOrange)
                def columnTemplateStyle = dynamicReportService.isInPrintMode(params)? Templates.columnStyle : Templates.columnStyleHTML
                StyleBuilder totalOrSubtotalStyleColumnStyle = stl.style(columnTemplateStyle).conditionalStyles(totalOrSubtotalStyle);

                if (columnValue.substring(0, 3).equalsIgnoreCase("ROW")) {
                    singleColumnGroup = grp.group(col.column(columnLabel, columnValue, type.stringType()))
                            .setHeaderLayout(GroupHeaderLayout.EMPTY).setPadding(0)

                    report.groupBy(singleColumnGroup)

                    report.addColumn(Columns.column(columnLabel, columnValue, type.stringType())
                            .setPrintWhenExpression(new SuppressRepeatingColumnPrintWhenExpression(singleColumnGroup, columnValue))
                            .setStyle(totalOrSubtotalStyleColumnStyle)

                    )
                } else {
                    report.addColumn(Columns.column(columnLabel, columnValue, type.stringType())
                            .setStyle(totalOrSubtotalStyleColumnStyle)
                    )
                }
            }
        }
    }

    private class TotalOrSubtotalRow extends AbstractSimpleExpression<Boolean> {
        private static final long serialVersionUID = 1L
        List<String> fields

        public TotalOrSubtotalRow(List<String> fields) {
            this.fields = fields
        }

        @Override
        public Boolean evaluate(ReportParameters reportParameters) {
            for (String field : fields) {
                Object value = reportParameters.getValue(field)
                if (value instanceof String && ("Total".equals(value) || "Subtotal".equals(value) || "Sub Total".equals(value))) {
                    return true
                }
            }
            return false
        }
    }

}
