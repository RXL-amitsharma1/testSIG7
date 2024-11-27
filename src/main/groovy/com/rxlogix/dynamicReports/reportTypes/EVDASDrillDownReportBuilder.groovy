package com.rxlogix.dynamicReports.reportTypes

import com.rxlogix.Constants
import com.rxlogix.dynamicReports.JasperReportBuilderEntry
import com.rxlogix.dynamicReports.ReportBuilder
import com.rxlogix.dynamicReports.Templates
import com.rxlogix.enums.ReportFormat
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.base.DRMargin
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder
import net.sf.dynamicreports.report.constant.HorizontalAlignment
import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.constant.PageType
import net.sf.jasperreports.engine.JRDataSource

import static net.sf.dynamicreports.report.builder.DynamicReports.*

class EVDASDrillDownReportBuilder extends ReportBuilder {
    private static final int PAGE_MARGIN_BOTTOM = 0

    void createReport(JRDataSource dataSource, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        if (dynamicReportService.isInPrintMode(params)) {
            if (params.outputFormat == ReportFormat.XLSX.name()) {
                JasperReportBuilderEntry criteriaJasperReportBuilderEntry = new JasperReportBuilderEntry()
                criteriaJasperReportBuilderEntry.jasperReportBuilder = buildCriteriaReport(params.criteriaSheetList, params)
                criteriaJasperReportBuilderEntry.excelSheetName = "Criteria"
                jasperReportBuilderEntryList.add(criteriaJasperReportBuilderEntry)
                params.showCompanyLogo = false
                params.showLogo = false
            }
            JasperReportBuilder reportSheet = buildReport(dataSource, params)
            updateReportMargin(reportSheet)
            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = reportSheet
            jasperReportBuilderEntry.excelSheetName = customMessageService.getMessage("jasperReports.${getClass().getSimpleName()}.title")
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }


    private void addEvdasDrillDownCriteriaHeaderTable(HorizontalListBuilder list, String labelId, String value) {
        list.add(cmp.text(labelId).setFixedColumns(10).setStyle(Templates.criteriaNameStyle),
                cmp.text(value).setStyle(Templates.criteriaValueStyle)).newRow()
    }
    /*
       This method is used to override report margin as per specific report.
     */

    private void updateReportMargin(JasperReportBuilder reportBuilder) {
        DRMargin drMargin = reportBuilder.getReport().getPage().getMargin()
        drMargin.setBottom(PAGE_MARGIN_BOTTOM)
    }

    @Override
    protected void addColumns(JasperReportBuilder report, Map params) {
        try {
            report.addColumn(Columns.column("Case Number", "caseNum", type.stringType()))
            report.addColumn(Columns.column("WWID", "caseIdentifier", type.stringType()))
            report.addColumn(Columns.column("HCP", "hcp", type.stringType()))
            report.addColumn(Columns.column("Age Group", "ageGroup", type.stringType()))
            report.addColumn(Columns.column("Gender", "gender", type.stringType()))
            report.addColumn(Columns.column("Receipt Date", "dateFirstReceipt", type.stringType()))
            report.addColumn(Columns.column("Source Country", "primarySourceCountryDesc", type.stringType()))
            report.addColumn(Columns.column("Suspected Drug", "suspList", type.stringType()))
            report.addColumn(Columns.column("Suspect Drugs w/Additional Info", "suspAddInfoList", type.stringType()))
            report.addColumn(Columns.column("Con-Med", "concList", type.stringType()))
            report.addColumn(Columns.column("ConMed Drugs w/Additional Info", "conMedInfoList", type.stringType()))
            report.addColumn(Columns.column("Event Terms", "reactList", type.stringType()))
            report.addColumn(Columns.column("Event Terms w/Additional Info", "eventAddInfoList", type.stringType()))
            report.addColumn(Columns.column("ICSR Forms", "icsr", type.stringType()))
        } catch (Exception e) {
            println e.printStackTrace()
        }
    }

    private JasperReportBuilder buildCriteriaReport(def criteriaList, Map params) {
        JasperReportBuilder evdasDataReport = ReportBuilder.initializeNewReport(params.portraitType, false, true, (params.outputFormat == ReportFormat.XLSX.name()), getPageType(), getPageOrientation())

        HorizontalListBuilder evdasList = cmp.horizontalList()
        def filler = cmp.filler().setStyle(stl.style().setTopBorder(stl.penDouble())).setFixedHeight(1)
        if (criteriaList) {
            evdasList.add(cmp.text("Criteria Sheet").setStyle(Templates.pageHeader_TitleStyle)).newRow()
            evdasList.add(cmp.verticalList(filler))
                criteriaList.each {
                    addEvdasDrillDownCriteriaHeaderTable(evdasList, it.label, it.value)
            }
        }

        VerticalListBuilder verticalList = cmp.verticalList()
        verticalList.add(cmp.verticalGap(10))
        verticalList.add(cmp.verticalList(evdasList))

        if (params.outputFormat != ReportFormat.XLSX.name()) {
            verticalList.add(cmp.verticalList(filler))
            verticalList.add(cmp.verticalGap(10))
        }
        JasperReportBuilder report = evdasDataReport.summary(verticalList)

        report.setPageFormat(getPageType(), getPageOrientation())
        report
    }

    @Override
    protected String getReportHeader(Map params) {
        String excelSheetName = customMessageService.getMessage("jasperReports.${getClass().getSimpleName()}.title")
        return excelSheetName
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
