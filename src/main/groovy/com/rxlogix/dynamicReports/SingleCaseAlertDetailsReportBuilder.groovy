package com.rxlogix.dynamicReports

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.constant.PageType
import net.sf.jasperreports.engine.JRDataSource

import static net.sf.dynamicreports.report.builder.DynamicReports.type

/**
 * Created by amanadhikari on 10/9/15.
 */
class SingleCaseAlertDetailsReportBuilder extends ReportBuilder {
    void createReport(JRDataSource dataSource, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList, String title) {
        if (dynamicReportService.isInPrintMode(params)) {
            JasperReportBuilder reportSheet = buildReport(dataSource, params, title)

            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = reportSheet
            jasperReportBuilderEntry.excelSheetName = customMessageService.getMessage("jasperReports.${getClass().getSimpleName()}.title")
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }

    @Override
    protected void addColumns(JasperReportBuilder report, Map params) {
        report.addColumn(Columns.column("Priority", "priority", type.stringType()))
                .addColumn(Columns.column("Case Number", "caseNumber", type.stringType()))
                .addColumn(Columns.column("Report Type", "caseReportType", type.stringType()))
                .addColumn(Columns.column("Receipt Date", "caseInitReceiptDate", type.stringType()))
                .addColumn(Columns.column("HCP", "reportersHcpFlag", type.stringType()))
                .addColumn(Columns.column("Product Name", "productName", type.stringType()))
                .addColumn(Columns.column("Preferred Term", "masterPrefTermAll", type.stringType()))
                .addColumn(Columns.column("Listedness", "assessListedness", type.stringType()))
                .addColumn(Columns.column("Causality", "causality", type.stringType()))
                .addColumn(Columns.column("Workflow State", "state", type.stringType()))
                .addColumn(Columns.column("Disposition", "disposition", type.stringType()))
                .addColumn(Columns.column("Assigned To", "assignedTo", type.stringType()))
                .addColumn(Columns.column("Detected Date", "detectedDate", type.stringType()))
                .addColumn(Columns.column("Due In", "dueDate", type.stringType()))
    }

    @Override
    protected String getReportHeader(Map params) {
        return customMessageService.getMessage("jasperReports.${getClass().getSimpleName()}.title")
    }

    @Override
    protected PageType getPageType() {
        return PageType._11X17
    }

    @Override
    protected PageOrientation getPageOrientation() {
        return PageOrientation.LANDSCAPE
    }
}
