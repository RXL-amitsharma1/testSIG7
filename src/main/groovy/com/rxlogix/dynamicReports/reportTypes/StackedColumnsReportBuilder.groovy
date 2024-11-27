package com.rxlogix.dynamicReports.reportTypes

import com.rxlogix.config.ExecutedCaseLineListingTemplate
import com.rxlogix.config.ReportFieldInfo
import com.rxlogix.config.ReportResult
import com.rxlogix.dynamicReports.Templates
import com.rxlogix.enums.ReportFormat
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder
import net.sf.dynamicreports.report.builder.grid.ColumnGridComponentBuilder
import net.sf.dynamicreports.report.builder.grid.VerticalColumnGridListBuilder

import static net.sf.dynamicreports.report.builder.DynamicReports.grid
import static net.sf.dynamicreports.report.builder.DynamicReports.stl

class StackedColumnsReportBuilder extends CaseLineListingReportBuilder {

    public void createStackedColumnsReport(ReportResult reportResult, JasperReportBuilder report, Map params) {
        ExecutedCaseLineListingTemplate executedCaseLineListingTemplate = reportResult?.executedTemplateQuery?.executedTemplate
        List<ReportFieldInfo> selectedColumns = executedCaseLineListingTemplate.columnList.reportFieldInfoList

        processColumnGrouping(executedCaseLineListingTemplate, report, params)
        List<TextColumnBuilder> allColumns = makeSelectedColumnsList(executedCaseLineListingTemplate, selectedColumns, report, params)

        if (params.outputFormat != ReportFormat.XLSX.name()) {
            List<ColumnGridComponentBuilder> columnGrids = new ArrayList<ColumnGridComponentBuilder>()
            def currentStackId = -1
            VerticalColumnGridListBuilder verticalColumnGridListBuilder = null
            for (int i = 0; i < selectedColumns.size(); i++) {
                ReportFieldInfo fieldInfo = selectedColumns[i]
                TextColumnBuilder column = allColumns.get(i)
                if (fieldInfo.stackId > 0) {
                    // Grouping of stacked columns
                    if (fieldInfo.stackId != currentStackId) {
                        verticalColumnGridListBuilder = grid.verticalColumnGridList();
                        columnGrids.add(verticalColumnGridListBuilder)
                    }
                    verticalColumnGridListBuilder.add(column)
                } else {
                    // Adding of non-stacked column
                    columnGrids.add(grid.horizontalColumnGridList(column))
                }
                currentStackId = fieldInfo.stackId
            }
            report.columnGrid(*columnGrids)
        }
        report.columns(*allColumns)
        processColspans(executedCaseLineListingTemplate, report, params)
        processSummary(executedCaseLineListingTemplate, report, params)
        report.setColumnHeaderStyle(stl.style().setBackgroundColor(Templates.orange));
    }

}
