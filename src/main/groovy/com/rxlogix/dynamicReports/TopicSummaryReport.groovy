package com.rxlogix.dynamicReports

/**
 * Created by suhailjahangir on 26/04/17.
 */
import com.rxlogix.ConfigurationService
import com.rxlogix.CustomMessageService
import com.rxlogix.DynamicReportService
import com.rxlogix.ImageService
import com.rxlogix.UserService
import com.rxlogix.enums.ReportFormat
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder
import net.sf.dynamicreports.report.builder.component.ImageBuilder
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder
import net.sf.dynamicreports.report.builder.style.StyleBuilder
import net.sf.dynamicreports.report.constant.HorizontalAlignment
import net.sf.dynamicreports.report.constant.VerticalAlignment
import net.sf.dynamicreports.report.constant.WhenNoDataType
import net.sf.jasperreports.engine.JRDataSource
import java.awt.Color
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.col
import static net.sf.dynamicreports.report.builder.DynamicReports.report
import static net.sf.dynamicreports.report.builder.DynamicReports.stl
import static net.sf.dynamicreports.report.builder.DynamicReports.type
import net.sf.dynamicreports.report.builder.FieldBuilder

class TopicSummaryReport {

    UserService userService = Holders.applicationContext.getBean("userService")
    ImageService imageService = Holders.applicationContext.getBean("imageService")
    ConfigurationService configurationService = Holders.applicationContext.getBean("configurationService")
    DynamicReportService dynamicReportService = Holders.applicationContext.getBean("dynamicReportService")
    CustomMessageService customMessageService = Holders.applicationContext.getBean("customMessageService")

    public static final StyleBuilder columnTitleStyle
    public static final StyleBuilder sectionTitleStyle
    public static final StyleBuilder printableRootStyle
    public static final dark_blue = new Color(91,163,197)
    public static final light_blue = new Color(212,233,239)
    public static final light_black = new Color(51, 51, 51)
    public static final grey = new Color(220, 220, 220)
    public static final blue = new Color(31,78,120)
    public static final white = new Color(255, 255, 255)

    static{
        printableRootStyle = stl.style().setFontSize(9).setBottomPadding(0).setTopPadding()
        columnTitleStyle = stl.style(printableRootStyle).setBold(true)
                .setBorder(stl.pen1Point().setLineColor(grey))
                .setForegroundColor(white)
                .setBackgroundColor(blue)
                .setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.BOTTOM)
                .setPadding(0)
                .setTopPadding()
        sectionTitleStyle = stl.style(printableRootStyle).setBold(true)
                .setBorder(stl.pen1Point().setLineColor(grey))
                .setForegroundColor(white)
                .setBackgroundColor(blue)
                .setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.BOTTOM)
                .setPadding(0)
                .setTopPadding()
    }

    public void createReport(JRDataSource signalDataList, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        if (dynamicReportService.isInPrintMode(params)) {
            JasperReportBuilder reportSheet = buildReport(signalDataList, params)
            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = reportSheet
            jasperReportBuilderEntry.excelSheetName = "Topic Summary Report"
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }

    private JasperReportBuilder buildReport(JRDataSource signalDataList, Map params) {

        def header = "Reporting Interval: "+ params.reportingInterval
        params.showCompanyLogo = true
        params.showLogo = true

        JasperReportBuilder signalSummaryPaberReport = ReportBuilder.initializeNewReport(true,true,false,(params.outputFormat == ReportFormat.XLSX.name()))

        signalSummaryPaberReport.setDataSource(signalDataList)
        addColumns(signalSummaryPaberReport)

        setPrintablePageHeader(signalSummaryPaberReport, header, false, params, "")

        VerticalListBuilder verticalList = cmp.verticalList()

        verticalList.add(cmp.subreport(signalSummaryPaberReport))
        verticalList.add(cmp.verticalGap(10))

        JasperReportBuilder report = report().title(verticalList)
        dynamicReportService.noDataCheck(signalDataList, report)
    }

    public String setPrintablePageHeader(JasperReportBuilder report, String customReportHeader, boolean isCriteriaSheetOrAppendix, Map params, String tableName) {

        if (params.showCompanyLogo || (!params.advancedOptions || params.advancedOptions == "0")&&params.showLogo) {
            ImageBuilder img = cmp.image(imageService.getImage("company-logo.png"))
                    .setFixedDimension(80, 36).setStyle(stl.style().setHorizontalAlignment(HorizontalAlignment.LEFT).setTopPadding(5))

            report.pageHeader(cmp.verticalList(img))
        }

        report.setDefaultFont(Templates.defaultFontStyle)
        report.setColumnTitleStyle(columnTitleStyle)
        report.setColumnStyle(Templates.columnStyle)

        def reportTemplate = report.getReport().getTemplate()
        reportTemplate.setGroupTitleStyle(Templates.groupTitleStyle.style)
        reportTemplate.setGroupStyle(Templates.groupStyle.style)
        TextFieldBuilder textFieldHeader = cmp.text(customReportHeader).setStyle(Templates.pageHeader_HeaderStyle_left)
        def filler = cmp.filler().setStyle(stl.style().setTopBorder(stl.penDouble())).setFixedHeight(0)
        report.pageHeader(cmp.verticalList(textFieldHeader).add(filler).add(cmp.verticalGap(10)))
    }

    private void addColumns(JasperReportBuilder report) {

        report.addColumn(Columns.column("Topic term", "topicTerm", type.stringType()))
                .addColumn(Columns.column("Date detected", "dateDetected", type.stringType()))
                .addColumn(Columns.column("Status (ongoing or closed) ", "status", type.stringType()))
                .addColumn(Columns.column("Date closed (for closed signals)", "dateClosed", type.stringType()))
                .addColumn(Columns.column("Source of Topic", "topicSource", type.stringType()))
                .addColumn(Columns.column("Reason for evaluation & summary of key data", "topicSummary", type.stringType()))
                .addColumn(Columns.column("Method of Topic evaluation ", "methodOfTopicEvaluation", type.stringType()))
                .addColumn(Columns.column("Action(s) taken or planned", "actionTaken", type.stringType()))
    }
}

