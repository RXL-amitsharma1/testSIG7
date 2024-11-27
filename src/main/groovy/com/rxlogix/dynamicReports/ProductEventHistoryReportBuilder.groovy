package com.rxlogix.dynamicReports

import com.rxlogix.enums.ReportFormat
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.constant.PageType

import static net.sf.dynamicreports.report.builder.DynamicReports.type

class ProductEventHistoryReportBuilder extends ReportBuilder {

    void createReport(List caseAlertHistoryList, List otherCaseAlertsHistoryList, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        if (dynamicReportService.isInPrintMode(params)) {
            String searchField = params.searchField ?: null
            String otherSearchString = params.otherSearchString ?: null
            params.showCompanyLogo = true
            params.criteriaSheetList = dynamicReportService.createCriteriaListForAggregateProductEventHistory(userService.getUser(),searchField,otherSearchString)

            List<ReportDetails> reportDetailsList = []
            reportDetailsList.add(new ReportDetails(customMessageService.getMessage("app.label.current.alert.history"), "", caseAlertHistoryList))
            reportDetailsList.add(new ReportDetails(customMessageService.getMessage("app.label.other.alerts.history"), "", otherCaseAlertsHistoryList))
            List<Tuple2<String, JasperReportBuilder>> subReports = createSubReports(params, reportDetailsList)

            if (params.outputFormat == ReportFormat.XLSX.name()) {
                JasperReportBuilderEntry criteriaJasperReportBuilderEntry = new JasperReportBuilderEntry()
                criteriaJasperReportBuilderEntry.jasperReportBuilder = buildCriteriaSheetReport(params)
                criteriaJasperReportBuilderEntry.excelSheetName = getReportHeader(params)
                jasperReportBuilderEntryList.add(criteriaJasperReportBuilderEntry)

                subReports.forEach {
                    JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
                    jasperReportBuilderEntry.excelSheetName = it.first
                    jasperReportBuilderEntry.jasperReportBuilder = it.second
                    jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
                }
            } else {
                JasperReportBuilder reportSheet = buildPdfDocxReport(null, subReports, params, "")
                JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
                jasperReportBuilderEntry.jasperReportBuilder = reportSheet
                jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
            }
        }
    }

    protected void addColumns(JasperReportBuilder report, Map params) {
        report.addColumn(Columns.column(customMessageService.getMessage("app.label.alert.name"), "alertName", type.stringType()))
                .addColumn(Columns.column(customMessageService.getMessage("app.label.review.period.days"), "reviewPeriod", type.stringType()))
                .addColumn(Columns.column(customMessageService.getMessage("app.label.disposition"), "disposition", type.stringType()))
                .addColumn(Columns.column(customMessageService.getMessage("app.label.case.history.justification"), "justification", type.stringType()))

        if (Holders.config.alert.priority.enable) {
            report.addColumn(Columns.column(customMessageService.getMessage("app.label.evdas.details.column.priorityAll"), "priority", type.stringType()))
        }

        report.addColumn(Columns.column(customMessageService.getMessage("app.label.alert.category"), "alertTags", type.listType()))
                .addColumn(Columns.column(customMessageService.getMessage("app.label.subTag.column"), "alertSubTags", type.listType()))
                .addColumn(Columns.column(customMessageService.getMessage("signal.history.label.performedBy"), "updatedBy", type.stringType()))
                .addColumn(Columns.column(customMessageService.getMessage("signal.history.label.date"), "timestamp", type.stringType()))
    }

    @Override
    protected String getReportHeader(Map params) {
        return customMessageService.getMessage('app.label.criteria', null, "Criteria", Locale.default)
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
