package com.rxlogix.dynamicReports

import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.constant.PageType
import net.sf.jasperreports.engine.JRDataSource
import static net.sf.dynamicreports.report.builder.DynamicReports.type
import com.rxlogix.CustomMessageService

class EmergingIssueReportBuilder extends ReportBuilder {
    CustomMessageService customMessageService = Holders.applicationContext.getBean("customMessageService")

    void createReport(JRDataSource dataSource, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        if (dynamicReportService.isInPrintMode(params)) {
            JasperReportBuilder reportSheet = buildReport(dataSource, params, "", false)
            reportSheet.setPageFormat(getPageType(), PageOrientation.LANDSCAPE)
            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = reportSheet
            jasperReportBuilderEntry.excelSheetName = customMessageService.getMessage("jasperReports.${getClass().getSimpleName()}.title")
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }

    @Override
    protected void addColumns(JasperReportBuilder report, Map params) {
        report.addColumn(Columns.column(customMessageService.getMessage('app.label.product.label'), "productSelection", type.stringType()))
                .addColumn(Columns.column( customMessageService.getMessage('app.reportField.eventName'),"eventName",type.stringType()))
                .addColumn(Columns.column(Holders.config.importantEvents.ime.label, "ime", type.stringType()))
                .addColumn(Columns.column(Holders.config.importantEvents.dme.label, "dme", type.stringType()))
                .addColumn(Columns.column(Holders.config.importantEvents.stopList.label, "emergingIssue", type.stringType()))
                .addColumn(Columns.column(Holders.config.importantEvents.specialMonitoring.label, "specialMonitoring", type.stringType()))
                .addColumn(Columns.column(customMessageService.getMessage('app.important.issue.lastModifiedBy'), "modifiedBy", type.stringType()))
                .addColumn(Columns.column(customMessageService.getMessage('app.important.issue.lastModified'), "lastUpdated", type.stringType()))

    }

    @Override
    protected String getReportHeader(Map params) {
        return params.firstRow
    }

    @Override
    protected PageType getPageType() {
        return PageType.LEDGER
    }

    @Override
    protected PageOrientation getPageOrientation() {
        return PageOrientation.PORTRAIT
    }
}
