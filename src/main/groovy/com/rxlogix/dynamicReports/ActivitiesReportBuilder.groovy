package com.rxlogix.dynamicReports

import com.rxlogix.Constants
import com.rxlogix.enums.ReportFormat
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.base.DRMargin
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
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.stl
import static net.sf.dynamicreports.report.builder.DynamicReports.stl
import static net.sf.dynamicreports.report.builder.DynamicReports.type

class ActivitiesReportBuilder extends ReportBuilder {
    private static final int PAGE_MARGIN_BOTTOM = 0

    void createReport(JRDataSource dataSource, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        if (dynamicReportService.isInPrintMode(params)) {
            if(params.outputFormat    == ReportFormat.XLSX.name()) {
                JasperReportBuilderEntry criteriaJasperReportBuilderEntry = new JasperReportBuilderEntry()
                criteriaJasperReportBuilderEntry.jasperReportBuilder = buildCriteriaReport(params.criteriaSheetList, params)
                criteriaJasperReportBuilderEntry.excelSheetName = "Criteria"
                jasperReportBuilderEntryList.add(criteriaJasperReportBuilderEntry)
            }
            JasperReportBuilder reportSheet = buildReport(dataSource, params)
            updateReportMargin(reportSheet)
            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = reportSheet
            jasperReportBuilderEntry.excelSheetName = customMessageService.getMessage("jasperReports.${getClass().getSimpleName()}.title")
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }
    private JasperReportBuilder buildCriteriaReport(def criteriaMap, Map params) {
        JasperReportBuilder singleCaseDataReport = ReportBuilder.initializeNewReport(params.portraitType, false , true,(params.outputFormat == ReportFormat.XLSX.name()), getPageType(), PageOrientation.LANDSCAPE)

        HorizontalListBuilder singleCaseList = cmp.horizontalList()
        def filler = cmp.filler().setStyle(stl.style().setTopBorder(stl.penDouble())).setFixedHeight(1)
        if (criteriaMap) {
            ImageBuilder img = cmp.image(imageService.getImage(Constants.DynamicReports.COMPANY_LOGO))
                    .setFixedDimension(80, 36).setStyle(stl.style().setHorizontalAlignment(HorizontalAlignment.LEFT).setTopPadding(5))
            singleCaseList.add(cmp.verticalList(img)).newRow()
            singleCaseList.add(cmp.text("Criteria Sheet").setStyle(Templates.pageHeader_TitleStyle)).newRow()
            singleCaseList.add(cmp.verticalList(filler))
                criteriaMap.each{
                        addCaseCriteriaHeaderTable(singleCaseList, it.label, it.value)
                }
        }

        VerticalListBuilder verticalList = cmp.verticalList()
        verticalList.add(cmp.verticalList(singleCaseList))
        JasperReportBuilder report = singleCaseDataReport.summary(verticalList)

        report.setPageFormat(getPageType(), PageOrientation.LANDSCAPE)
        report
    }
    private void addCaseCriteriaHeaderTable(HorizontalListBuilder list, String labelId, String value) {
        list.add(cmp.text(labelId).setFixedColumns(10).setStyle(Templates.criteriaNameStyle),
                cmp.text(value).setStyle(Templates.criteriaValueStyle)).newRow()
    }
    @Override
    protected void addColumns(JasperReportBuilder report, Map params) {
        if (params.appType == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
            addSingleCaseColumns(report)
        } else if (params.appType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            addAggregateCaseColumns(report)
        } else if (params.appType == Constants.AlertConfigType.EVDAS_ALERT) {
            addEvdasCaseColumns(report)
        } else {
            addAdHocColumns(report, params)
        }
    }

    private void addSingleCaseColumns(JasperReportBuilder report) {
        report.addColumn(Columns.column("Case Number", "caseNumber", type.stringType()))
                .addColumn(Columns.column("Activity Type", "type", type.stringType()))
                .addColumn(Columns.column("Suspect Product", "suspect", type.stringType()))
                .addColumn(Columns.column("Event", "eventName", type.stringType()))
                .addColumn(Columns.column("Description", "details", type.stringType()))
                .addColumn(Columns.column("Current Assignment", "currentAssignment", type.stringType()))
                .addColumn(Columns.column("Performed By", "performedBy", type.stringType()))
                .addColumn(Columns.column("Timestamp", "timestamp", type.stringType()))
    }

    private void addAggregateCaseColumns(JasperReportBuilder report) {
        report.addColumn(Columns.column("Activity Type", "type", type.stringType()))
                .addColumn(Columns.column("Suspect Product", "suspect", type.stringType()))
                .addColumn(Columns.column("Event", "eventName", type.stringType()))
                .addColumn(Columns.column("Description", "details", type.stringType()))
                .addColumn(Columns.column("Current Assignment", "currentAssignment", type.stringType()))
                .addColumn(Columns.column("Performed By", "performedBy", type.stringType()))
                .addColumn(Columns.column("Timestamp", "timestamp", type.stringType()))
    }

    private void addEvdasCaseColumns(JasperReportBuilder report) {
        report.addColumn(Columns.column("Activity Type", "type", type.stringType()))
                .addColumn(Columns.column("Suspect Product", "suspect", type.stringType()))
                .addColumn(Columns.column("Event", "eventName", type.stringType()))
                .addColumn(Columns.column("Description", "details", type.stringType()))
                .addColumn(Columns.column("Current Assignment", "currentAssignment", type.stringType()))
                .addColumn(Columns.column("Performed By", "performedBy", type.stringType()))
                .addColumn(Columns.column("Timestamp", "timestamp", type.stringType()))
    }

    private void addAdHocColumns(JasperReportBuilder report, Map params) {
        report.addColumn(Columns.column("Activity Type", "type", type.stringType()))
        // split data for Description field

        if(params.maxSize==null){
            report.addColumn(Columns.column("Description", "details", type.stringType()))
        }
        else{
            for (Integer i = 1; i <= params.maxSize; i++) {
                 report.addColumn(Columns.column("Description","details${i}",type.stringType()))
            }
        }
        report.addColumn(Columns.column("Performed By", "performedBy", type.stringType()))
                .addColumn(Columns.column("Timestamp", "timestamp", type.stringType()))
    }

    @Override
    protected String getReportHeader(Map params) {
        String header = customMessageService.getMessage("jasperReports.${getClass().getSimpleName()}.title")

        if (params.appType == Constants.AlertConfigType.AD_HOC_ALERT) {
            header += """
                Alert Name: ${params.alertName}
                Product Name: ${params.productName}
                Event Name: ${params.eventName}
                Topic Name: ${params.topicName}
                """
        }
        return header
    }

    @Override
    protected PageType getPageType() {
        return PageType.LEDGER
    }

    @Override
    protected PageOrientation getPageOrientation() {
        return PageOrientation.LANDSCAPE
    }
    /*
       This method is used to override report margin as per specific report.
     */
    private void updateReportMargin(JasperReportBuilder reportBuilder) {
        DRMargin drMargin = reportBuilder.getReport().getPage().getMargin()
        drMargin.setBottom(PAGE_MARGIN_BOTTOM)
    }
}
