package com.rxlogix.dynamicReports

import com.rxlogix.DynamicReportService
import com.rxlogix.ImageService
import com.rxlogix.enums.ReportFormat
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.builder.component.ImageBuilder
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder
import net.sf.dynamicreports.report.constant.HorizontalAlignment
import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.constant.PageType
import net.sf.jasperreports.engine.JRDataSource

import static net.sf.dynamicreports.report.builder.DynamicReports.*

/**
 * Created by amanadhikari on 10/10/15.
 */
class AdHocAlertReportBuilder {
    DynamicReportService dynamicReportService = Holders.applicationContext.getBean("dynamicReportService")
    ImageService imageService = Holders.applicationContext.getBean("imageService")

    public void createReport(JRDataSource dataSource, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        if (dynamicReportService.isInPrintMode(params)) {
            JasperReportBuilder reportSheet = buildReport(dataSource, params)

            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = reportSheet
            jasperReportBuilderEntry.excelSheetName = "Ad-Hoc Alert"
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }

    private JasperReportBuilder buildReport(JRDataSource dataSource, Map params) {
        JasperReportBuilder report = ReportBuilder.initializeNewReport(true,true,false,(params.outputFormat == ReportFormat.XLSX.name()),PageType._11X17, PageOrientation.LANDSCAPE)
        report.setDataSource(dataSource)
        report.setPageFormat(PageType._11X17, PageOrientation.LANDSCAPE)
        addColumns(report)
        params.showCompanyLogo = true
        setPrintablePageHeader(report, "Ad-Hoc Alert", false, params)
        dynamicReportService.noDataCheck(dataSource, report)
    }

    private void addColumns(JasperReportBuilder report) {
        report.addColumn(Columns.column("Alert Name", "alertName", type.stringType()))
                .addColumn(Columns.column("Version", "alertVersion", type.stringType()))
                .addColumn(Columns.column("Description", "description", type.stringType()))
                .addColumn(Columns.column("Frequency", "frequency", type.stringType()))
                .addColumn(Columns.column("Assigned To", "assignedTo", type.stringType()))
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
            title = "Ad-Hoc Alert"
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
