package com.rxlogix.dynamicReports

import com.rxlogix.CustomMessageService
import com.rxlogix.DynamicReportService
import com.rxlogix.ImageService
import com.rxlogix.UserService
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.ViewHelper
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder
import net.sf.dynamicreports.report.builder.component.ImageBuilder
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder
import net.sf.dynamicreports.report.constant.HorizontalAlignment
import net.sf.dynamicreports.report.constant.VerticalAlignment
import com.rxlogix.config.ExecutedConfiguration


import javax.imageio.ImageIO
import java.text.SimpleDateFormat

import static net.sf.dynamicreports.report.builder.DynamicReports.*

class HeaderBuilder {

    CustomMessageService customMessageService = Holders.applicationContext.getBean("customMessageService")
    DynamicReportService dynamicReportService = Holders.applicationContext.getBean("dynamicReportService")
    UserService userService = Holders.applicationContext.getBean("userService")
    ImageService imageService = Holders.applicationContext.getBean("imageService")

    public void setHeader(ExecutedConfiguration executedConfigurationInstance, Map params, JasperReportBuilder report, String customTitle, boolean isCriteriaSheetOrAppendix) {

        setHeaderTemplate(report, params, isCriteriaSheetOrAppendix, false)
        setHeaderParameters(executedConfigurationInstance, params, report,  customTitle ,
                isCriteriaSheetOrAppendix)
    }

    public void setCapaHeader(Map params, JasperReportBuilder report, String customTitle, boolean isCriteriaSheetOrAppendix) {
        setHeaderTemplate(report, params, isCriteriaSheetOrAppendix, false)
        setCapaHeaderParameters(params, report, customTitle)
    }

    public void setHeaderTemplate(JasperReportBuilder report, Map params = null, boolean isCriteriaSheetOrAppendix = false, boolean printExtraDetails = false) {
        addParameters(report)
        setPageHeaderTemplate(report, params, isCriteriaSheetOrAppendix, printExtraDetails)
        report.setDefaultFont(Templates.defaultFontStyle)
        report.setColumnHeaderStyle(Templates.columnHeaderStyle)
        report.setColumnTitleStyle(Templates.columnTitleStyle)
        report.setColumnStyle(Templates.columnStyle)
        def reportTemplate = report.getReport().getTemplate()
        reportTemplate.setGroupTitleStyle(Templates.groupTitleStyle.style)
        reportTemplate.setGroupStyle(Templates.groupStyle.style)
    }

    private void addParameters(JasperReportBuilder report) {
        CustomReportParameters.values().each {
            report.addParameter(it.jasperName, it.jasperType)
        }
    }

    private void initParameters(JasperReportBuilder report) {
        CustomReportParameters.values().each {
            report.setParameter(it.jasperName, it.defaultValue)
        }
    }

    public void setPageHeaderTemplate(JasperReportBuilder report, Map params, boolean isCriteriaSheetOrAppendix, boolean printExtraDetails) {
        HorizontalListBuilder headerComponent = cmp.horizontalList()
        headerComponent.add(cmp.text(CustomReportParameters.RUN_DATE.jasperValue)
                .setStyle(stl.style().setHorizontalAlignment(HorizontalAlignment.RIGHT).setVerticalAlignment(VerticalAlignment.MIDDLE))
                .setRemoveLineWhenBlank(true)
                .setPrintWhenExpression(CustomReportParameters.PRINT_RUN_DATE.jasperValue))
        if(params?.outputFormat != ReportFormatEnum.XLSX.name() && !(!params.outputFormat || params.outputFormat == ReportFormatEnum.HTML.name())) {
            TextFieldBuilder<String> bookmarkHeader = cmp.text(exp.jasperSyntax("\" \"")).setWidth(2)
                    .setAnchorName(CustomReportParameters.REPORT_TITLE.jasperValue)
                    .setBookmarkLevel(1)
                    .setPrintWhenExpression(exp.jasperSyntax("\$V{PAGE_NUMBER}==1"))
            headerComponent.add(bookmarkHeader)
        }

        report.pageHeader(headerComponent)

        TextFieldBuilder header = cmp.text(CustomReportParameters.HEADER_TEXT.jasperValue)
                .setStyle(Templates.pageHeader_HeaderStyle)
                .setRemoveLineWhenBlank(true)
                .setPrintWhenExpression(CustomReportParameters.PRINT_HEADER_TEXT.jasperValue)

        TextFieldBuilder title = cmp.text(CustomReportParameters.REPORT_TITLE.jasperValue)
                .setStyle(Templates.pageHeader_HeaderStyle)
                .setRemoveLineWhenBlank(true)
                .setPrintWhenExpression(CustomReportParameters.PRINT_REPORT_TITLE.jasperValue)

        TextFieldBuilder extraDetails = cmp.text(CustomReportParameters.EXTRA_DETAILS.jasperValue)
                .setStyle(Templates.pageHeader_HeaderStyle)
                .setRemoveLineWhenBlank(true)
                .setPrintWhenExpression(CustomReportParameters.PRINT_EXTRA_DETAILS.jasperValue)

        def filler = cmp.filler()
                .setStyle(stl.style().setTopBorder(stl.penDouble()))
                .setFixedHeight(15)
                .setRemoveLineWhenBlank(true)
                .setPrintWhenExpression(CustomReportParameters.IS_CRITERIA_SHEET_OR_APPENDIX.jasperValue)

        def headerList = cmp.verticalList()
        // To change the order of printing in the exported report when  Product or Date Range selection is checked
        if(printExtraDetails){
            headerList.add(extraDetails, header, title, filler)
        }else {
            headerList.add(header, title, extraDetails, filler)
        }
        report.pageHeader(headerList)
    }

    public String setHeaderParameters(ExecutedConfiguration executedConfigurationInstance, Map params, JasperReportBuilder report, String customTitle, boolean isCriteriaSheetOrAppendix) {
        initParameters(report)
        report.setParameter(CustomReportParameters.IS_CRITERIA_SHEET_OR_APPENDIX.jasperName, isCriteriaSheetOrAppendix)
        Boolean printImage = params.showCompanyLogo || (!params.advancedOptions || params.advancedOptions == "0")
        Boolean printExtraDetails = true
        if (!params.outputFormat || params.outputFormat == ReportFormatEnum.HTML.name()) {
            printImage = false
            printExtraDetails = false
            report.setParameter(CustomReportParameters.PRINT_HEADER_TEXT.jasperName, false)
            report.setParameter(CustomReportParameters.PRINT_RUN_DATE.jasperName, false)
            report.setParameter(CustomReportParameters.PRINT_EXTRA_DETAILS.jasperName, false)
            report.ignorePageWidth()
        } else if (params.outputFormat == ReportFormatEnum.XLSX.name()) {
            report.ignorePageWidth()
            report.setPageMargin(margin(0))
        } else if (params.outputFormat == ReportFormatEnum.DOCX.name()
                || params.outputFormat == ReportFormatEnum.PDF.name()
                || params.outputFormat == ReportFormatEnum.PPTX.name()) {
            // do nothing
        }
        report.setParameter(CustomReportParameters.PRINT_HEADER_IMAGE.jasperName, printImage)

        if (params.showCompanyLogo || (!params.advancedOptions || params.advancedOptions == "0")) {
            report.setParameter(CustomReportParameters.HEADER_IMAGE.jasperName, ImageIO.read(imageService.getImage("company-logo.png")))
        }

        String header =  "Case Form"
        StringBuilder extraDetailsBuilder = new StringBuilder()
        if (!isCriteriaSheetOrAppendix && executedConfigurationInstance?.nextRunDate) {
            //def runDateObj = customMessageService.getMessage("app.label.runDateAndTime") + ":" + ViewHelper.formatRunDateAndTime(executedConfigurationInstance)
            String title = ""
            report.setParameter(CustomReportParameters.REPORT_TITLE.jasperName, title)
            boolean printProductSelection = false
            boolean printDateRange = false
            printExtraDetails = printExtraDetails && (printProductSelection || printDateRange)
            if (params.outputFormat == ReportFormatEnum.XLSX.name()) {
                report.ignorePagination()
                report.ignorePageWidth()
                report.setPageMargin(margin(0))
                report.setParameter(CustomReportParameters.PRINT_HEADER_TEXT.jasperName, false)
                report.setParameter(CustomReportParameters.PRINT_REPORT_TITLE.jasperName, false)
                if (header)
                    extraDetailsBuilder.append("\n" + header)
                if (title)
                    extraDetailsBuilder.append("\n" + title)
            }
            report.setParameter(CustomReportParameters.EXTRA_DETAILS.jasperName, extraDetailsBuilder.toString())
        } else {
            report.setParameter(CustomReportParameters.PRINT_RUN_DATE.jasperName, false)
            report.setParameter(CustomReportParameters.PRINT_REPORT_TITLE.jasperName, false)
            report.setParameter(CustomReportParameters.PRINT_EXTRA_DETAILS.jasperName, false)
        }
        report.setParameter(CustomReportParameters.HEADER_TEXT.jasperName, header)

        params.entrySet().each { param ->
            def customParam = CustomReportParameters.values().find {it.name() == param.key}
            if (customParam) {
                report.setParameter(customParam.jasperName, param.value)
            }
        }
    }

    public String setCapaHeaderParameters(Map params, JasperReportBuilder report, String customTitle) {
        initParameters(report)
        Boolean printImage = true
        if (params.outputFormat == ReportFormatEnum.XLSX.name()) {
            report.ignorePagination()
            report.ignorePageWidth()
            report.setPageMargin(margin(0))
        } else if (params.outputFormat == ReportFormatEnum.DOCX.name()
                || params.outputFormat == ReportFormatEnum.PDF.name()
                || params.outputFormat == ReportFormatEnum.PPTX.name()) {
            // do nothing
        }
        report.setParameter(CustomReportParameters.PRINT_HEADER_IMAGE.jasperName, printImage)
        report.setParameter(CustomReportParameters.HEADER_IMAGE.jasperName, ImageIO.read(imageService.getImage("company-logo.png")))
        String header =  MiscUtil.matchCSVPattern(ViewHelper.getReportHeader(null, customTitle))
        StringBuilder extraDetailsBuilder = new StringBuilder()

        User user = userService.user
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.getLongDateFormatForLocale(user?.preference?.locale, true))
        sdf.setTimeZone(TimeZone.getTimeZone(ViewHelper.getTimeZone(user)))

        if (params.outputFormat == ReportFormatEnum.XLSX.name()) {
            report.ignorePagination()
            report.ignorePageWidth()
            report.setPageMargin(margin(0))
            report.setParameter(CustomReportParameters.PRINT_HEADER_TEXT.jasperName, false)
            report.setParameter(CustomReportParameters.PRINT_REPORT_TITLE.jasperName, false)
        }
        if (header)
            extraDetailsBuilder.append("\n" + header)
        report.setParameter(CustomReportParameters.EXTRA_DETAILS.jasperName, extraDetailsBuilder.toString())

        params.entrySet().each { param ->
            def customParam = CustomReportParameters.values().find {it.name() == param.key}
            if (customParam) {
                report.setParameter(customParam.jasperName, param.value)
            }
        }
    }
}
