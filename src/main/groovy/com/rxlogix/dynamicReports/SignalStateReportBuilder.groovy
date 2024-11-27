package com.rxlogix.dynamicReports

import com.rxlogix.Constants
import com.rxlogix.enums.ReportFormat
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.base.DRMargin
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.builder.style.StyleBuilder
import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.constant.PageType
import net.sf.jasperreports.engine.JRDataSource

import static net.sf.dynamicreports.report.builder.DynamicReports.type

class SignalStateReportBuilder extends ReportBuilder {
    private static final int PAGE_MARGIN_BOTTOM = 0

    void createReport(JRDataSource dataSource, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        String title = customMessageService.getMessage("jasperReports.${getClass().getSimpleName()}.title")
        if (dynamicReportService.isInPrintMode(params)) {
            params.portraitType = false
            if(params.outputFormat == ReportFormat.XLSX.name()) {
                JasperReportBuilderEntry criteriaJasperReportBuilderEntry = new JasperReportBuilderEntry()
                criteriaJasperReportBuilderEntry.jasperReportBuilder = buildCriteriaReport(params.criteriaSheetList, params)
                criteriaJasperReportBuilderEntry.excelSheetName = "Criteria"
                jasperReportBuilderEntryList.add(criteriaJasperReportBuilderEntry)
            } else {
                params.showCompanyLogo = false
            }
            JasperReportBuilder reportSheet = buildReport(dataSource, params, title, false)
            updateReportMargin(reportSheet)
            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = reportSheet
            jasperReportBuilderEntry.excelSheetName = title
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }

    /*
       This method is used to override report margin as per specific report.
     */

    private void updateReportMargin(JasperReportBuilder reportBuilder) {
        DRMargin drMargin = reportBuilder.getReport().getPage().getMargin()
        drMargin.setBottom(PAGE_MARGIN_BOTTOM)
    }

    @Override
    protected void addColumns(JasperReportBuilder report, Map params) {
        if(params.containsKey("title")&& params.get("title")== Constants.SignalReportTypes.SIGNALS_BY_STATE){
            report.addColumn(Columns.column("Signal Name", "signalName", type.stringType()))
                    .addColumn(Columns.column("Product", "productName", type.listType()))
                    .addColumn(Columns.column("Priority", "priority", type.stringType()))
                    .addColumn(Columns.column("Monitoring Status", "monitoringStatus", type.stringType()))
                    .addColumn(Columns.column("Current Assignment", "assignedTo", type.stringType()))
                    .addColumn(Columns.column("Open Actions", "actions", type.integerType()))
        }
        else {
            report.addColumn(Columns.column("Signal Name", "signalName", type.stringType()))
                    .addColumn(Columns.column("Product", "productName", type.listType()))
                    .addColumn(Columns.column("Current Status", "disposition", type.stringType()))
                    .addColumn(Columns.column("Priority", "priority", type.stringType()))
                    .addColumn(Columns.column("Monitoring Status", "monitoringStatus", type.stringType()))
                    .addColumn(Columns.column("Current Assignment", "assignedTo", type.stringType()))
                    .addColumn(Columns.column("Open Actions", "actions", type.integerType()))
        }
    }

    @Override
    protected String getReportHeader(Map params) {
        def header = """
        Reporting Interval: ${params.reportingInterval}
        Products: ${params.products}
        """
        return header
    }

    @Override
    protected StyleBuilder getReportHeaderStyle() {
        return Templates.pageHeader_HeaderStyle_left
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
