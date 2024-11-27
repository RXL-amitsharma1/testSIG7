package com.rxlogix.dynamicReports
import com.rxlogix.*
import com.rxlogix.enums.ReportFormat
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.constant.PageType

import static net.sf.dynamicreports.report.builder.DynamicReports.*

class CaseHistoryReportBuilder extends ReportBuilder {
    DataObjectService dataObjectService = Holders.applicationContext.getBean("dataObjectService")

    void createReport(List caseHistoryList, List suspectProductHistoryList, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList, boolean isLiteratureAlert) {
        if (dynamicReportService.isInPrintMode(params)) {
            params.showCompanyLogo = true
            List<ReportDetails> reportDetailsList = []
            String subTitle = getSubTitle(params, isLiteratureAlert);
            reportDetailsList.add(new ReportDetails(customMessageService.getMessage("caseDetails.review.history.current"), subTitle, caseHistoryList))
            reportDetailsList.add(new ReportDetails(customMessageService.getMessage("caseDetails.review.history.suspect"), subTitle, suspectProductHistoryList))
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

    String getSubTitle(Map params, Boolean isLiterature) {
        if (isLiterature) {
            return customMessageService.getMessage("app.label.literature.alert.article") + params.articleId
        } else {
            return customMessageService.getMessage("app.caseInfo.caseDetail.productName", params.caseVisibility, params.productFamily)
        }
    }

    protected void addColumns(JasperReportBuilder report, Map params) {
        Boolean isCaseVersion = dataObjectService.getDataSourceMap(Constants.DbDataSource.IS_ARISG_PVIP)
        report.addColumn(Columns.column(customMessageService.getMessage("app.label.alert.name"), "alertName", type.stringType()))
        if (!(params.get('alertName') == 'literatureHistory')) {
            report.addColumn(Columns.column(customMessageService.getMessage("${(params.isFaers == "true" || params.isVaers == "true" || params.isVigibase == "true") ? "app.label.qualitative.details.column.caseNumber.faers" : (isCaseVersion ? "app.label.qualitative.details.column.caseNumber.version" : "app.label.qualitative.details.column.caseNumber")}"), "caseNumber", type.stringType()))
        }
        report.addColumn(Columns.column(customMessageService.getMessage("app.label.disposition"), "disposition", type.stringType()))
                .addColumn(Columns.column(customMessageService.getMessage("app.label.case.history.justification"), "justification", type.stringType()))
        if(Holders.config.alert.priority.enable){
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
