package com.rxlogix.dynamicReports.reportTypes

import com.rxlogix.Constants
import com.rxlogix.dynamicReports.CSVFormulaFormatter
import com.rxlogix.dynamicReports.JasperReportBuilderEntry
import com.rxlogix.dynamicReports.ReportBuilder
import com.rxlogix.dynamicReports.Templates
import com.rxlogix.enums.ReportFormat
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder
import net.sf.dynamicreports.report.builder.component.ImageBuilder
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder
import net.sf.dynamicreports.report.constant.HorizontalAlignment
import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.constant.PageType
import net.sf.jasperreports.engine.JRDataSource

import static net.sf.dynamicreports.report.builder.DynamicReports.*

class ImportAssignmentReportBuilder extends ReportBuilder{
    void createReport(JRDataSource dataSource, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList, Map importedInformation, User importedBy) {
        if (dynamicReportService.isInPrintMode(params)) {
            List criteriaSheetList = dynamicReportService.createCriteriaList(importedBy, null, null, false, false) //Filter not required PVS-53068
            JasperReportBuilderEntry criteriaJasperReportBuilderEntry = new JasperReportBuilderEntry()
            criteriaJasperReportBuilderEntry.jasperReportBuilder = buildCriteriaReport(criteriaSheetList, params)
            criteriaJasperReportBuilderEntry.excelSheetName = "Criteria"
            jasperReportBuilderEntryList.add(criteriaJasperReportBuilderEntry)
            JasperReportBuilder criteriaPage =importedInformationOfFile(importedInformation, params)
            JasperReportBuilderEntry jasperSignalSummary = new JasperReportBuilderEntry()
            jasperSignalSummary.jasperReportBuilder = criteriaPage
            jasperSignalSummary.excelSheetName = "Import Log Details"
            jasperReportBuilderEntryList.add(jasperSignalSummary)

            JasperReportBuilder reportSheet = buildReport(dataSource, params)
            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = reportSheet
            jasperReportBuilderEntry.excelSheetName = "Failed Records"
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }

    def importedInformationOfFile(Map importedInformation, Map params){
        VerticalListBuilder verticalList = cmp.verticalList()
        HorizontalListBuilder signalSummaryList = cmp.horizontalList()
        signalSummaryList.add(cmp.text("Import Log").setStyle(Templates.pageHeader_TitleStyle)).newRow()
        def filler = cmp.filler().setStyle(stl.style().setTopBorder(stl.penDouble())).setFixedHeight(1)
        signalSummaryList.add(cmp.verticalList(filler))

        addSignalSummaryHeaderTable(signalSummaryList, 'Imported Date', importedInformation.importedDate.toString())
        addSignalSummaryHeaderTable(signalSummaryList, 'Total Number of records', importedInformation.totalRecords.toString())
        addSignalSummaryHeaderTable(signalSummaryList, 'Total Number of records Imported', importedInformation.recordsImported.toString())
        addSignalSummaryHeaderTable(signalSummaryList, 'Total Number of records Discarded', importedInformation.recordsDiscarded.toString())
        addSignalSummaryHeaderTable(signalSummaryList, 'Total Number of records Failed', importedInformation.recordsFailed.toString())
        verticalList.add(cmp.verticalGap(10))
        verticalList.add(cmp.verticalList(signalSummaryList))
        verticalList.add(cmp.verticalGap(10))

        HorizontalListBuilder signalSummaryText = cmp.horizontalList()

        verticalList.add(cmp.verticalGap(5))
        verticalList.add(cmp.verticalList(signalSummaryText))
        verticalList.add(cmp.verticalGap(5))
        JasperReportBuilder signalDataReport = ReportBuilder.initializeNewReport(true,true,false,(params.outputFormat == ReportFormat.XLSX.name()), PageType.LEDGER, PageOrientation.LANDSCAPE)
        JasperReportBuilder report = signalDataReport.summary(verticalList)
        report.setPageFormat(PageType.LEDGER, PageOrientation.LANDSCAPE)
        report
    }
    private JasperReportBuilder buildCriteriaReport(def criteriaMap, Map params) {
        JasperReportBuilder criteriaDataReport = ReportBuilder.initializeNewReport(params.portraitType, false, true, (params.outputFormat == ReportFormat.XLSX.name()), getPageType(), PageOrientation.LANDSCAPE)
        HorizontalListBuilder criteriaList = cmp.horizontalList()
        params.showCompanyLogo = true
        params.showLogo = true
        def filler = cmp.filler().setStyle(stl.style().setTopBorder(stl.penDouble())).setFixedHeight(1)
        if (criteriaMap) {
            ImageBuilder img = cmp.image(imageService.getImage(Constants.DynamicReports.COMPANY_LOGO))
                    .setFixedDimension(80, 36).setStyle(stl.style().setHorizontalAlignment(HorizontalAlignment.LEFT).setTopPadding(5))
            criteriaList.add(cmp.verticalList(img)).newRow()
            criteriaList.add(cmp.text("Criteria Sheet").setStyle(Templates.pageHeader_TitleStyle)).newRow()
            criteriaList.add(cmp.verticalList(filler))
            if (params.callingScreen != Constants.Commons.DASHBOARD) {
                criteriaMap.each {
                    addCriteriaHeaderTable(criteriaList, it.label, it.value)
                }
            }
        }
        VerticalListBuilder verticalList = cmp.verticalList()
        verticalList.add(cmp.verticalList(criteriaList))
        JasperReportBuilder report = criteriaDataReport.summary(verticalList)
        report.setPageFormat(getPageType(), PageOrientation.LANDSCAPE)
        report
    }
    private void addCriteriaHeaderTable(HorizontalListBuilder list, String labelId, String value) {
        list.add(cmp.text(labelId).setFixedColumns(14).setStyle(Templates.criteriaNameStyle),
                cmp.text(value).setStyle(Templates.criteriaValueStyle)).newRow()
    }
    private void addSignalSummaryHeaderTable(HorizontalListBuilder list, def labelId, def value) {
        if (value instanceof List) {
            value = value.join(',')
        }
        list.add(
                cmp.text(labelId).setFixedColumns(15).setStyle(Templates.criteriaNameStyle),
                cmp.text(value).setValueFormatter(new CSVFormulaFormatter()).setStyle(Templates.criteriaValueStyle)
        ).newRow()
    }

    @Override
    protected void addColumns(JasperReportBuilder report, Map params) {
        report.addColumn(Columns.column("Product", "product", type.stringType()))
        report.addColumn(Columns.column("Product Hierarchy", "product hierarchy", type.stringType()))
        report.addColumn(Columns.column("Assignment", "assignment", type.stringType()))
        report.addColumn(Columns.column("Workflow Group", "workflow group", type.stringType()))
        report.addColumn(Columns.column("Import", "import", type.stringType()))
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
