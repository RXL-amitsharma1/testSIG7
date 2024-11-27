package com.rxlogix.dynamicReports

import com.rxlogix.enums.ReportFormat
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.constant.PageType
import net.sf.jasperreports.engine.JRDataSource

import static net.sf.dynamicreports.report.builder.DynamicReports.*

class SignalDetectionSummaryReportBuilder extends ReportBuilder {
    void createReport(JRDataSource validatedSignalList, JRDataSource notStartedReviewSignalList, JRDataSource pendingReviewList,
                      JRDataSource closedReviewList, signalData, Map params, List<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        if (dynamicReportService.isInPrintMode(params)) {
            params.showCompanyLogo = true
            params.criteriaSheetTitle = getReportHeader(params)
            if(params.outputFormat == ReportFormat.XLSX.name() && !signalData.cumulative) {
                JasperReportBuilderEntry criteriaJasperReportBuilderEntry = new JasperReportBuilderEntry()
                criteriaJasperReportBuilderEntry.jasperReportBuilder = buildCriteriaSheetReport(params, getReportHeader(params))
                criteriaJasperReportBuilderEntry.excelSheetName = customMessageService.getMessage('app.label.criteria', null, "Criteria", Locale.default)
                jasperReportBuilderEntryList.add(criteriaJasperReportBuilderEntry)
            } else {
                List<Tuple2<String, JasperReportBuilder>> subreports = createAlertGrids(params, validatedSignalList,
                        notStartedReviewSignalList, pendingReviewList, closedReviewList)
                JasperReportBuilder reportSheet = buildPdfDocxReport(null, subreports, params, "", !signalData.cumulative)

                JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
                jasperReportBuilderEntry.jasperReportBuilder = reportSheet
                jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
            }

            if (params.outputFormat == ReportFormat.XLSX.name()) {
                buildAlertReport(jasperReportBuilderEntryList, validatedSignalList,
                        notStartedReviewSignalList, pendingReviewList, closedReviewList, params)
            }
        }
    }

    private void buildAlertReport(List<JasperReportBuilderEntry> jasperReportBuilderEntryList,
                                  JRDataSource validatedSignalList, JRDataSource notStartedReviewSignalList,
                                  JRDataSource pendingReviewList, JRDataSource closedReviewList, Map params) {
        List<Tuple2<String, JasperReportBuilder>> subreports = createAlertGrids(params, validatedSignalList,
                notStartedReviewSignalList, pendingReviewList, closedReviewList)
        subreports.forEach {
            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.excelSheetName = it.first
            jasperReportBuilderEntry.jasperReportBuilder = it.second
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }

    //TODO: In this method we are using the string literals as values. We should read those from messages.properties file.
    private List<Tuple2<String, JasperReportBuilder>> createAlertGrids(Map params, JRDataSource validatedSignalList,
                                                                       JRDataSource notStartedReviewSignalList,
                                                                       JRDataSource pendingReviewList,
                                                                       JRDataSource closedReviewList) {
        List<Tuple2<String, JasperReportBuilder>> reports = []
        params.showCompanyLogo = true
        if (validatedSignalList) {
            String title = "Validated Observation"
            JasperReportBuilder report = createGridSubreport(validatedSignalList, params, title)
            reports.add(new Tuple2<>(title, report))
        }
        if (notStartedReviewSignalList) {
            String title = "Pending Review"
            JasperReportBuilder report = createGridSubreport(notStartedReviewSignalList, params, title)
            reports.add(new Tuple2<>(title, report))
        }
        if (pendingReviewList) {
            String title = "Review In Progress"
            JasperReportBuilder report = createGridSubreport(pendingReviewList, params, title)
            reports.add(new Tuple2<>(title, report))
        }
        if (closedReviewList) {
            String title = "Closed Observations"
            JasperReportBuilder report = createGridSubreport(closedReviewList, params, title)
            reports.add(new Tuple2<>(title, report))
        }
        return reports
    }

    private JasperReportBuilder createGridSubreport(JRDataSource dataSource, Map params, String title) {
        JasperReportBuilder subReport = ReportBuilder.initializeNewReport(params.portraitType, true, false, (params.outputFormat == ReportFormat.XLSX.name()), getPageType(), getPageOrientation())
        subReport.setDataSource(dataSource)
        addColumns(subReport, params)
        if (params.outputFormat != ReportFormat.XLSX.name()) {
            setPrintablePageHeader(subReport, '', false, params, title)
        } else {
            setPrintablePageHeaderExcel(subReport)
        }
        subReport.setPageFormat(getPageType(), getPageOrientation())
        return subReport
    }

    @Override
    protected void addColumns(JasperReportBuilder report, Map params) {
        if (params.isSingleCaseAlert) {
            report.addColumn(Columns.column("Case Number(f/u#)", "caseNumber", type.stringType()))
        }
        if (params.isEvdasCaseAlert) {
            report.addColumn(Columns.column("Substance", "product", type.stringType()))
        } else {
            report.addColumn(Columns.column("Product Name", "product", type.stringType()))
        }
        report.addColumn(Columns.column("PT", "event", type.stringType()))
                .addColumn(Columns.column("Justification", "justification", type.stringType()))
                .addColumn(Columns.column("Current Disposition", "currentDisposition", type.stringType()))
        if(Holders.config.alert.priority.enable){
            report.addColumn(Columns.column("Priority", "priority", type.stringType()))
                    .addColumn(Columns.column("Priority Justification", "priorityJustification", type.stringType()))
        }
    }

    @Override
    protected String getReportHeader(Map params) {
        return params.header
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