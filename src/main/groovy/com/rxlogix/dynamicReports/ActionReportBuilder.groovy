package com.rxlogix.dynamicReports

import com.rxlogix.*
import com.rxlogix.enums.ReportFormat
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.builder.component.ImageBuilder
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder
import net.sf.dynamicreports.report.constant.HorizontalAlignment
import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.constant.PageType
import net.sf.jasperreports.engine.JRDataSource

import static net.sf.dynamicreports.report.builder.DynamicReports.*

class ActionReportBuilder {

    UserService userService = Holders.applicationContext.getBean("userService")
    ImageService imageService = Holders.applicationContext.getBean("imageService")
    ConfigurationService configurationService = Holders.applicationContext.getBean("configurationService")
    DynamicReportService dynamicReportService = Holders.applicationContext.getBean("dynamicReportService")
    CustomMessageService customMessageService = Holders.applicationContext.getBean("customMessageService")

    public void createReport(JRDataSource dataSource, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {

        if (dynamicReportService.isInPrintMode(params)) {
            JasperReportBuilder reportSheet = buildReport(dataSource, params, true)
            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = reportSheet
            jasperReportBuilderEntry.excelSheetName = "Actions"
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }
    private JasperReportBuilder buildReport(JRDataSource dataSource, Map params, Boolean isConsolidated) {
        def header = isConsolidated? "":"""
        """

        header += """
        Alert Name: ${params.alertName}
        Product Name: ${params.productName}
        Event Name: ${params.eventName}
        Topic Name: ${params.topicName}
        """
        JasperReportBuilder report = ReportBuilder.initializeNewReport(true,false,false,(params.outputFormat == ReportFormat.XLSX.name()), PageType.LEDGER, PageOrientation.LANDSCAPE)
        report.setDataSource(dataSource)
        report.setPageFormat(PageType.LEDGER, PageOrientation.LANDSCAPE)
        addActionsReportColumns(report)
        params.showCompanyLogo = true
        setPrintablePageHeader(report, header ,false, params)

        dynamicReportService.noDataCheck(dataSource, report)

    }

    private void addActionsReportColumns(JasperReportBuilder report) {
        report.addColumn(Columns.column("Action Type", "type", type.stringType()))
                .addColumn(Columns.column("Action", "config", type.stringType()))
                .addColumn(Columns.column("Details", "details", type.stringType()))
                .addColumn(Columns.column("Due Date", "dueDate", type.stringType()))
                .addColumn(Columns.column("Assigned To", "assignedTo", type.stringType()))
                .addColumn(Columns.column("Status", "actionStatus", type.stringType()))
                .addColumn(Columns.column("Completion Date", "completedDate", type.stringType()))
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
        TextFieldBuilder textFieldHeader = cmp.text(customReportHeader).setStyle(Templates.pageHeader_HeaderStyle_left)

        def filler = cmp.filler().setStyle(stl.style().setTopBorder(stl.penDouble())).setFixedHeight(0)
        report.pageHeader(cmp.verticalList(textFieldHeader).add(filler).add(cmp.verticalGap(10)))

        }

    private String setPageHeaderText(JasperReportBuilder report, String customReportHeader, boolean isCriteriaSheetOrAppendix) {
        String header = customReportHeader
        String title

        if (!isCriteriaSheetOrAppendix) {
            title = ""
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
