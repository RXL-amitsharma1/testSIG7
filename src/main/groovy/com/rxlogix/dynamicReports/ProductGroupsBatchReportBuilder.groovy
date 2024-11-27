package com.rxlogix.dynamicReports

import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.constant.PageType
import net.sf.jasperreports.engine.JRDataSource

import static net.sf.dynamicreports.report.builder.DynamicReports.type

class ProductGroupsBatchReportBuilder extends ReportBuilder {

    void createReport(JRDataSource dataSource, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        JasperReportBuilderEntry criteriaJasperReportBuilderEntry = new JasperReportBuilderEntry()
        criteriaJasperReportBuilderEntry.jasperReportBuilder = buildCriteriaReport(params.criteriaSheetList, params)
        criteriaJasperReportBuilderEntry.excelSheetName = "Criteria"
        jasperReportBuilderEntryList.add(criteriaJasperReportBuilderEntry)
        JasperReportBuilder reportSheet = buildReport(dataSource, params)
        JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
        jasperReportBuilderEntry.jasperReportBuilder = reportSheet
        jasperReportBuilderEntry.excelSheetName = customMessageService.getMessage("jasperReports.${getClass().getSimpleName()}.title")
        jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
    }

    @Override
    protected void addColumns(JasperReportBuilder report, Map params) {
        Map columns = params.columns
        columns.each {
            report.addColumn(Columns.column(it.value, it.key, type.stringType()))
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
