package com.rxlogix.dynamicReports

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.constant.PageType
import net.sf.jasperreports.engine.JRDataSource

import static net.sf.dynamicreports.report.builder.DynamicReports.type

class ProductSummaryReportBuilderService extends ReportBuilder {
    void createReport(JRDataSource dataSource, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        if (dynamicReportService.isInPrintMode(params)) {
            JasperReportBuilder reportSheet = buildReport(dataSource, params, "", false)
            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = reportSheet
            jasperReportBuilderEntry.excelSheetName = customMessageService.getMessage("jasperReports.${getClass().getSimpleName()}.title")
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }

    @Override
    protected void addColumns(JasperReportBuilder report, Map params) {
        def columnField = [[fieldName: "event", columnName: "Event"],
                           [fieldName: "disposition", columnName: "Disposition"],
                           [fieldName: "name", columnName: "Alert/Signal Name"],
                           [fieldName: "source", columnName: "Data Source"],
                           [fieldName: "sponCounts", columnName: "Spon\nN/P/C"],
                           [fieldName: "eb05Counts", columnName: "EB05\nCurr/Prev"],
                           [fieldName: "eb95Counts", columnName: "EB95\nCurr/Prev"],
                           [fieldName: "requestedBy", columnName: "Requested By/Sources"],
                           [fieldName: "comment", columnName: "Comments"],
                           [fieldName: "assessmentComment", columnName: "Assessment Comments"]
        ]

        columnField.each {
            report.addColumn(Columns.column(it.columnName, it.fieldName, type.stringType()))
        }
    }

    @Override
    protected String getReportHeader(Map params) {
        return customMessageService.getMessage("jasperReports.${getClass().getSimpleName()}.title")
    }

    @Override
    protected PageType getPageType() {
        return PageType.LEDGER
    }

    @Override
    protected PageOrientation getPageOrientation() {
        return PageOrientation.LANDSCAPE
    }
}
