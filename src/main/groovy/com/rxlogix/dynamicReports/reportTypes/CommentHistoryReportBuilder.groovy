package com.rxlogix.dynamicReports.reportTypes

import com.rxlogix.dynamicReports.JasperReportBuilderEntry
import com.rxlogix.dynamicReports.ReportBuilder
import com.rxlogix.enums.ReportFormat
import com.rxlogix.signal.AlertCommentHistory
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.constant.PageType
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource

import static net.sf.dynamicreports.report.builder.DynamicReports.type

class CommentHistoryReportBuilder extends ReportBuilder {

    void createReport(List commentHistoryList, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList, AlertCommentHistory alertComment) {
        String searchString = params.searchField ?: null
        if (dynamicReportService.isInPrintMode(params)) {
            params.showCompanyLogo = true
            params.criteriaSheetList = dynamicReportService.createCriteriaListForComments(userService.getUser(), searchString, alertComment)
            if(params.outputFormat == ReportFormat.XLSX.name()) {
                JasperReportBuilderEntry criteriaJasperReportBuilderEntry = new JasperReportBuilderEntry()
                criteriaJasperReportBuilderEntry.jasperReportBuilder = buildCriteriaSheetReport(params)
                criteriaJasperReportBuilderEntry.excelSheetName = customMessageService.getMessage('app.label.criteria', null, "Criteria", Locale.default)
                jasperReportBuilderEntryList.add(criteriaJasperReportBuilderEntry)
            }

            JasperReportBuilder reportSheet
            JRDataSource dataSource = new JRMapCollectionDataSource(commentHistoryList)
            String title = customMessageService.getMessage("app.label.comment.template.history.product")
            if (params.outputFormat == ReportFormat.XLSX.name()) {
                reportSheet = buildXlsxReport(dataSource, params, title)
            } else {
                reportSheet = buildPdfDocxReport(dataSource, null, params, title)
            }

            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = reportSheet
            jasperReportBuilderEntry.excelSheetName = getReportHeader(params)
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }

    @Override
    protected void addColumns(JasperReportBuilder report, Map params) {
        report.addColumn(Columns.column(customMessageService.getMessage("app.label.period"), "period", type.stringType()))
                .addColumn(Columns.column(customMessageService.getMessage("signal.history.label.status.comment"), "comments", type.stringType()))
                .addColumn(Columns.column(customMessageService.getMessage("app.label.modifiedBy"), "modifiedBy", type.stringType()))
                .addColumn(Columns.column(customMessageService.getMessage("signal.history.label.date"), "lastUpdated", type.stringType()))
    }

    @Override
    protected String getReportHeader(Map params) {
        return customMessageService.getMessage('app.label.comment.template.history', null, "Comment History", Locale.default)
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
