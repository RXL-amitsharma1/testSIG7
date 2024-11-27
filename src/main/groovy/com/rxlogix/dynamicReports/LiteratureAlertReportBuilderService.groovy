package com.rxlogix.dynamicReports

import com.rxlogix.Constants
import com.rxlogix.ViewInstanceService
import com.rxlogix.enums.ReportFormat
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder
import net.sf.dynamicreports.report.builder.component.ImageBuilder
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder
import net.sf.dynamicreports.report.constant.HorizontalAlignment
import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.constant.PageType
import net.sf.jasperreports.engine.JRDataSource

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.stl
import static net.sf.dynamicreports.report.builder.DynamicReports.stl
import static net.sf.dynamicreports.report.builder.DynamicReports.type

class LiteratureAlertReportBuilderService extends ReportBuilder {
    ViewInstanceService viewInstanceService = Holders.applicationContext.getBean("viewInstanceService")

    void createReport(JRDataSource dataSource, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        String title = customMessageService.getMessage("jasperReports.${getClass().getSimpleName()}.title")
        if (dynamicReportService.isInPrintMode(params)) {

            if(params.outputFormat == ReportFormat.XLSX.name() ) {
                JasperReportBuilderEntry criteriaJasperReportBuilderEntry = new JasperReportBuilderEntry()
                criteriaJasperReportBuilderEntry.jasperReportBuilder = buildCriteriaReport(params.criteriaSheetList, params)
                criteriaJasperReportBuilderEntry.excelSheetName = "Criteria"
                jasperReportBuilderEntryList.add(criteriaJasperReportBuilderEntry)
            }

            params.portraitType = true
            JasperReportBuilder reportSheet = buildReport(dataSource, params, "", false)
            reportSheet.setPageFormat(getPageType(), PageOrientation.LANDSCAPE)
            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = reportSheet
            jasperReportBuilderEntry.excelSheetName = title
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }

    private JasperReportBuilder buildCriteriaReport(def criteriaMap, Map params) {
        JasperReportBuilder literatureCaseDataReport = ReportBuilder.initializeNewReport(params.portraitType, false , true,(params.outputFormat == ReportFormat.XLSX.name()), getPageType(), PageOrientation.LANDSCAPE)

        HorizontalListBuilder literatureCaseList = cmp.horizontalList()
        def filler = cmp.filler().setStyle(stl.style().setTopBorder(stl.penDouble())).setFixedHeight(1)
        if (criteriaMap) {
            ImageBuilder img = cmp.image(imageService.getImage(Constants.DynamicReports.COMPANY_LOGO))
                    .setFixedDimension(80, 36).setStyle(stl.style().setHorizontalAlignment(HorizontalAlignment.LEFT).setTopPadding(5))
            literatureCaseList.add(cmp.verticalList(img)).newRow()
            literatureCaseList.add(cmp.text("Criteria").setStyle(Templates.pageHeader_TitleStyle)).newRow()
            literatureCaseList.add(cmp.verticalList(filler))
            if(params.callingScreen != Constants.Commons.DASHBOARD) {
                criteriaMap.each{
                    addLiteratureCaseCriteriaHeaderTable(literatureCaseList, it.label, it.value)
                }
            }
        }

        VerticalListBuilder verticalList = cmp.verticalList()
        verticalList.add(cmp.verticalList(literatureCaseList))
        JasperReportBuilder report = literatureCaseDataReport.summary(verticalList)
        report.setPageFormat(getPageType(), PageOrientation.LANDSCAPE)
        report
    }

    private void addLiteratureCaseCriteriaHeaderTable(HorizontalListBuilder list, String labelId, String value) {
        list.add(cmp.text(labelId).setFixedColumns(10).setStyle(Templates.criteriaNameStyle),
                cmp.text(value).setStyle(Templates.criteriaValueStyle)).newRow()
    }

    @Override
    protected void addColumns(JasperReportBuilder report, Map params) {
        String alertType = params.boolean("isEmbase") ? Constants.AlertConfigType.EMBASE_LITERATURE_SEARCH_ALERT : Constants.AlertConfigType.LITERATURE_SEARCH_ALERT
        List<Map> columnList = viewInstanceService.fetchVisibleColumnList(alertType, null)
        if(params.boolean("isAbstractEnabled")){
            columnList.push(["label": 'Abstract', "name":'articleAbstract'])
        }
        Map<String, Object> dispositionToRemove = ["label": 'Disposition To', "name":'currentDisposition'] //Fix for PVS-53084
        columnList.removeIf { it == dispositionToRemove }  //Fix for PVS-53084
        columnList << ["label": "Comment", "name": "comment"]
        columnList << ["label": "Priority", "name": "priority"]
        columnList.each {
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
