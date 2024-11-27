package com.rxlogix.dynamicReports

import com.rxlogix.Constants
import com.rxlogix.ViewInstanceService
import com.rxlogix.cache.CacheService
import com.rxlogix.enums.ReportFormat
import com.rxlogix.enums.SensitivityLabelEnum
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder
import net.sf.dynamicreports.report.builder.component.ImageBuilder
import net.sf.dynamicreports.report.constant.HorizontalAlignment
import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.constant.PageType
import net.sf.dynamicreports.report.constant.VerticalAlignment
import net.sf.jasperreports.engine.JRDataSource

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.stl
import static net.sf.dynamicreports.report.builder.DynamicReports.stl
import static net.sf.dynamicreports.report.builder.DynamicReports.type

class AdHocAlertDetailsReportBuilder extends ReportBuilder {
    private ViewInstanceService viewInstanceService = Holders.applicationContext.getBean("viewInstanceService")

    void createReport(JRDataSource dataSource, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList, String title) {
        if (dynamicReportService.isInPrintMode(params)) {
            if(params.outputFormat == ReportFormat.XLSX.name() ) {
                JasperReportBuilderEntry criteriaJasperReportBuilderEntry = new JasperReportBuilderEntry()
                criteriaJasperReportBuilderEntry.jasperReportBuilder = buildCriteriaReport(params.criteriaSheetList, params)
                criteriaJasperReportBuilderEntry.excelSheetName = "Criteria"
                jasperReportBuilderEntryList.add(criteriaJasperReportBuilderEntry)
            }
            params.portraitType = true
            JasperReportBuilder reportSheet = buildReport(dataSource, params, title, false)
            //Removed code , extra confidential logo was coming
            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = reportSheet
            jasperReportBuilderEntry.excelSheetName = customMessageService.getMessage("jasperReports.${getClass().getSimpleName()}.title")
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }

    @Override
    protected void addColumns(JasperReportBuilder report, Map params) {
        Map nameLabelMap = params.get("labelMap")
        List<Map> fixedColumnList = [["label": nameLabelMap["name"], "name": "name"], ["label": nameLabelMap["notes"], "name": "notes"] ]
        Long viewInstance = null
        if (params["viewInstance.id"]) {
            viewInstance = params["viewInstance.id"] as Long
        }
        List<Map> columnList = viewInstanceService.fetchVisibleColumnList(Constants.AlertConfigType.ADHOC_ALERT, viewInstance)
        // removed as Disposition is not required in export report
        columnList.remove(["label": nameLabelMap["currentDisposition"], "name": "currentDisposition"])
        fixedColumnList.addAll(columnList)
        fixedColumnList.each {
                report.addColumn(Columns.column(it.label, it.name, type.stringType()))
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
