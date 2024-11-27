package com.rxlogix.dynamicReports

import com.rxlogix.Constants
import com.rxlogix.DynamicReportService
import com.rxlogix.ImageService
import com.rxlogix.enums.ReportFormat
import com.rxlogix.enums.SensitivityLabelEnum
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder
import net.sf.dynamicreports.report.builder.component.ImageBuilder
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder
import net.sf.dynamicreports.report.constant.HorizontalAlignment
import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.constant.PageType
import net.sf.dynamicreports.report.constant.VerticalAlignment
import net.sf.jasperreports.engine.JRDataSource

import static net.sf.dynamicreports.report.builder.DynamicReports.*

class MeetingDetailsReportService {

    DynamicReportService dynamicReportService = Holders.applicationContext.getBean("dynamicReportService")
    ImageService imageService = Holders.applicationContext.getBean("imageService")

    public void createReport(JRDataSource dataSource, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        if (dynamicReportService.isInPrintMode(params)) {
                JasperReportBuilderEntry criteriaJasperReportBuilderEntry = new JasperReportBuilderEntry()
                criteriaJasperReportBuilderEntry.jasperReportBuilder = buildCriteriaReport(params.criteriaSheetList, params)
                criteriaJasperReportBuilderEntry.excelSheetName = "Criteria"
                jasperReportBuilderEntryList.add(criteriaJasperReportBuilderEntry)
            JasperReportBuilder reportSheet = buildReport(dataSource, params)
            if(Holders.config.signal.confidential.logo.enable && !(params.outputFormat == ReportFormat.XLSX.name())){
                ImageBuilder img = cmp.image(imageService.getImage(SensitivityLabelEnum.CONFIDENTIAL.imageName)).setFixedDimension(80, 36)
                HorizontalListBuilder confidentialImage = cmp.horizontalList(cmp.horizontalGap((PageType.LEDGER.getHeight() - 150)),img)
                        .setStyle(stl.style().setAlignment(HorizontalAlignment.RIGHT, VerticalAlignment.BOTTOM).setTopPadding(50))
                reportSheet.pageFooter(confidentialImage)
                reportSheet.setPageFooterStyle(stl.style().setBottomPadding(10).setHorizontalAlignment(HorizontalAlignment.CENTER))
            }
            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = reportSheet
            jasperReportBuilderEntry.excelSheetName = "Meeting Details Report"
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }

    private JasperReportBuilder buildCriteriaReport(def criteriaMap, Map params) {
        JasperReportBuilder criteriaDataReport = ReportBuilder.initializeNewReport(params.portraitType, false, true, (params.outputFormat == ReportFormat.XLSX.name()), PageType.LEDGER, PageOrientation.LANDSCAPE)
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
        report.setPageFormat(PageType.LEDGER, PageOrientation.LANDSCAPE)
        report
    }

    private void addCriteriaHeaderTable(HorizontalListBuilder list, String labelId, String value) {
        list.add(cmp.text(labelId).setFixedColumns(14).setStyle(Templates.criteriaNameStyle),
                cmp.text(value).setStyle(Templates.criteriaValueStyle)).newRow()
    }

    private JasperReportBuilder buildReport(JRDataSource dataSource, Map params) {
        JasperReportBuilder report = ReportBuilder.initializeNewReport(true, true, false, (params.outputFormat == ReportFormat.XLSX.name()), PageType.LEDGER, PageOrientation.LANDSCAPE)
        report.setDataSource(dataSource)
        report.setPageFormat(PageType.LEDGER, PageOrientation.LANDSCAPE)
        addColumns(report)
        if (params.outputFormat == Constants.SignalReportOutputType.XLSX) {
            report.ignorePagination()
            setPrintablePageHeaderExcel(report)
        } else {
            params.showCompanyLogo = true
            setPrintablePageHeader(report, "Meeting Details Report", false, params)
        }
        dynamicReportService.noDataCheck(dataSource, report)
    }

    protected void setPrintablePageHeaderExcel(JasperReportBuilder report) {
        report.ignorePagination()
        report.ignorePageWidth()
        report.setDefaultFont(Templates.defaultFontStyle)
        report.setColumnTitleStyle(Templates.columnTitleStyle)
        report.setPageMargin(margin(0))
        report.setColumnStyle(Templates.columnStyle)
    }


    private void addColumns(JasperReportBuilder report) {
        report
                .addColumn(Columns.column("Signal Name", "signalName", type.stringType()))
                .addColumn(Columns.column("Title", "title", type.stringType()))
                .addColumn(Columns.column("Meeting Date/Time", "meetingDate", type.stringType()))
                .addColumn(Columns.column("Agenda", "agenda", type.stringType()))
                .addColumn(Columns.column("Minutes", "minutes", type.stringType()))
                .addColumn(Columns.column("Last Modified By", "lastUpdatedBy", type.stringType()))
                .addColumn(Columns.column("Last Modified", "lastUpdated", type.stringType()))
    }

    public String setPrintablePageHeader(JasperReportBuilder report, String customReportHeader, boolean isCriteriaSheetOrAppendix, Map params) {

        if (params.showCompanyLogo || (!params.advancedOptions || params.advancedOptions == "0")) {
            ImageBuilder img = cmp.image(imageService.getImage("company-logo.png"))
                    .setFixedDimension(80, 36).setStyle(stl.style().setHorizontalAlignment(HorizontalAlignment.LEFT).setTopPadding(5))

            report.pageHeader(cmp.verticalList(img))
        }

        report.setDefaultFont(Templates.defaultFontStyle)
        report.setColumnTitleStyle(Templates.columnTitleStyle)
        report.setColumnStyle(Templates.columnStyle)

        def reportTemplate = report.getReport().getTemplate()
        reportTemplate.setGroupTitleStyle(Templates.groupTitleStyle.style)
        reportTemplate.setGroupStyle(Templates.groupStyle.style)

        return setPageHeaderText(report, customReportHeader, isCriteriaSheetOrAppendix)
    }

    private String setPageHeaderText(JasperReportBuilder report, String customReportHeader, boolean isCriteriaSheetOrAppendix) {
        String header = customReportHeader
        String title

        if (!isCriteriaSheetOrAppendix) {
            title = "Meeting Details"
        } else {
            title = ""
        }

        String printDateRange = "false"

        TextFieldBuilder textFieldTitle = cmp.text(title).setStyle(Templates.pageHeader_TitleStyle)
        TextFieldBuilder textFieldDateRange = null

        def filler = cmp.filler().setStyle(stl.style().setTopBorder(stl.penDouble())).setFixedHeight(10)

        if (header) {
            TextFieldBuilder textFieldHeader = cmp.text(header).setStyle(Templates.pageHeader_HeaderStyle)
            if (printDateRange == "true" && !isCriteriaSheetOrAppendix) {
                report.pageHeader(cmp.verticalList(textFieldHeader, textFieldTitle, textFieldDateRange).add(filler))
            } else {
                report.pageHeader(cmp.verticalList(textFieldHeader, textFieldTitle).add(filler))
            }
        } else {
            if (printDateRange == "true" && !isCriteriaSheetOrAppendix) {
                report.pageHeader(cmp.verticalList(textFieldTitle, textFieldDateRange).add(filler))
            } else {
                report.pageHeader(cmp.verticalList(textFieldTitle).add(filler))
            }
        }
        return title
    }
}
