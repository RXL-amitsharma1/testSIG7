package com.rxlogix.dynamicReports


import com.rxlogix.Constants
import com.rxlogix.enums.ReportFormat
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.builder.style.StyleBuilder
import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.constant.PageType
import net.sf.jasperreports.engine.JRDataSource

import static net.sf.dynamicreports.report.builder.DynamicReports.*

class SignalSummaryPeberReportBuilder extends ReportBuilder {

    void createReport(JRDataSource dataSource, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList, Boolean isConsolidated) {
        if (dynamicReportService.isInPrintMode(params)) {
            params.isConsolidated = isConsolidated
            params.showCompanyLogo = true
            if(params.outputFormat == ReportFormat.XLSX.name() && params.callingScreen != Constants.Commons.DASHBOARD && params.callingScreen != Constants.Commons.TAGS) {
                JasperReportBuilderEntry criteriaJasperReportBuilderEntry = new JasperReportBuilderEntry()
                criteriaJasperReportBuilderEntry.jasperReportBuilder = buildCriteriaSheetReport(params)
                criteriaJasperReportBuilderEntry.excelSheetName = customMessageService.getMessage('app.label.criteria', null, "Criteria", Locale.default)
                jasperReportBuilderEntryList.add(criteriaJasperReportBuilderEntry)
            }

            String title = customMessageService.getMessage("jasperReports.${getClass().getSimpleName()}.title")

            JasperReportBuilder reportSheet;
            if (params.outputFormat == ReportFormat.XLSX.name()) {
                reportSheet = buildXlsxReport(dataSource, params, title)
            } else {
                reportSheet = buildPdfDocxReport(dataSource, null, params, title)
            }

            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = reportSheet
            jasperReportBuilderEntry.excelSheetName = title
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }

    @Override
    protected void addColumns(JasperReportBuilder report, Map params) {
        report.addColumn(Columns.column("${Holders.config.pvsignal.pbrerReport.columnNames.signalName}", "signalName", type.stringType()))
                .addColumn(Columns.column("${Holders.config.pvsignal.pbrerReport.columnNames.dateDetected}", "dateDetected", type.stringType()))
                .addColumn(Columns.column("${Holders.config.pvsignal.pbrerReport.columnNames.signalStatus}", "status", type.stringType()))
                .addColumn(Columns.column("${Holders.config.pvsignal.pbrerReport.columnNames.dateClosed}", "dateClosed", type.stringType()))
                .addColumn(Columns.column("${Holders.config.pvsignal.pbrerReport.columnNames.signalSource}", "signalSource", type.stringType()))
                .addColumn(Columns.column("${Holders.config.pvsignal.pbrerReport.columnNames.reasonForEvaluation}", "reasonForEvaluation", type.stringType()))
                .addColumn(Columns.column("${Holders.config.pvsignal.pbrerReport.columnNames.evaluationMethod}", "evaluationMethod", type.stringType()))
                .addColumn(Columns.column("${Holders.config.pvsignal.pbrerReport.columnNames.actionTaken}", "actionTaken", type.stringType()))
    }

    @Override
    protected String getReportHeader(Map params) {
        String header
        if (params.isConsolidated) {
            header = "Reporting Interval: ${params.reportingInterval}\nGenerated On: ${params.generatedOn}\nProduct Name: ${params.products}"
        } else {
            header = "Signal Name: ${params.signalName}\nProduct Name: ${params.products}"
        }
        return header
    }

    @Override
    protected StyleBuilder getReportHeaderStyle() {
        return Templates.pageHeader_HeaderStyle
    }

    @Override
    protected PageType getPageType() {
        return PageType.A4
    }

    @Override
    protected PageOrientation getPageOrientation() {
        return PageOrientation.LANDSCAPE
    }
}
