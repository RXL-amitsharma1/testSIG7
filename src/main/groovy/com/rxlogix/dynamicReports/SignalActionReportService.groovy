package com.rxlogix.dynamicReports

import com.rxlogix.Constants
import com.rxlogix.enums.ReportFormatEnum
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.MarginBuilder
import net.sf.dynamicreports.report.base.DRMargin
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.builder.style.StyleBuilder
import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.constant.PageType
import net.sf.jasperreports.engine.JRDataSource

import static net.sf.dynamicreports.report.builder.DynamicReports.*

class SignalActionReportService extends ReportBuilder {
    private static final int PAGE_MARGIN_BOTTOM = 0

    void createReport(JRDataSource dataSource, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        String title = customMessageService.getMessage("jasperReports.${getClass().getSimpleName()}.title")
        if (dynamicReportService.isInPrintMode(params)) {
            params.portraitType = false
            params.showCompanyLogo = true
            if(params.outputFormat == ReportFormatEnum.XLSX.name()){
                JasperReportBuilderEntry criteriaJasperReportBuilderEntry = new JasperReportBuilderEntry()
                criteriaJasperReportBuilderEntry.jasperReportBuilder = buildCriteriaReport(params.criteriaSheetList, params)
                criteriaJasperReportBuilderEntry.excelSheetName = "Criteria"
                jasperReportBuilderEntryList.add(criteriaJasperReportBuilderEntry)
            }
            JasperReportBuilder reportSheet = buildReport(dataSource, params, title, false, 50)
            overridePageMargins(reportSheet)
            updateReportMargin(reportSheet)
            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = reportSheet
            jasperReportBuilderEntry.excelSheetName = title
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }

    static JasperReportBuilder overridePageMargins(JasperReportBuilder reportSheet) {
        MarginBuilder margins = new MarginBuilder()
        margins.setTop(0)
        margins.setRight(25)
        margins.setLeft(25)
        margins.setBottom(0)
        reportSheet.setPageMargin(margins)
    }

    /*
       This method is used to override report margin as per specific report.
     */

    private void updateReportMargin(JasperReportBuilder reportBuilder) {
        DRMargin drMargin = reportBuilder.getReport().getPage().getMargin()
        drMargin.setBottom(PAGE_MARGIN_BOTTOM)
    }

    @Override
    void addColumns(JasperReportBuilder report, Map params) {
        report.addColumn(Columns.column("Signal Name", "signalName", type.stringType()))
                .addColumn(Columns.column("Action Name", "actionName", type.stringType()))
                .addColumn(Columns.column("Action Type", "actionType", type.stringType()))
                .addColumn(Columns.column("Assigned To", "assignedTo", type.stringType()))
                .addColumn(Columns.column("Status", "status", type.stringType()))
                .addColumn(Columns.column("Creation Date", "creationDate", type.stringType()))
                .addColumn(Columns.column("Completion Date", "completionDate", type.stringType()))
                .addColumn(Columns.column("Details", "details", type.stringType()))
                .addColumn(Columns.column("Comments", "comments", type.stringType()))
    }

    @Override
    protected String getReportHeader(Map params) {
        def header = Constants.Commons.BLANK_STRING

        if (params.showProductAndInterval) {
            header = "Reporting Interval: ${params.reportingInterval}\nProducts: ${params.products}"
        }
        return header
    }

    @Override
    protected StyleBuilder getReportHeaderStyle() {
        return Templates.pageHeader_HeaderStyle_left
    }

    @Override
    protected PageType getPageType() {
        return PageType._11X17
    }

    @Override
    protected PageOrientation getPageOrientation() {
        return PageOrientation.PORTRAIT
    }
}
