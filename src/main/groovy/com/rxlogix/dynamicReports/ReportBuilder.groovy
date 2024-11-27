package com.rxlogix.dynamicReports

import com.rxlogix.Constants
import com.rxlogix.CustomMessageService
import com.rxlogix.DynamicReportService
import com.rxlogix.ImageService
import com.rxlogix.UserService
import com.rxlogix.enums.ReportFormat
import com.rxlogix.enums.SensitivityLabelEnum
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.MarginBuilder
import net.sf.dynamicreports.report.builder.ReportTemplateBuilder
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder
import net.sf.dynamicreports.report.builder.component.ImageBuilder
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder
import net.sf.dynamicreports.report.builder.style.StyleBuilder
import net.sf.dynamicreports.report.constant.ComponentPositionType
import net.sf.dynamicreports.report.constant.HorizontalAlignment
import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.constant.PageType
import net.sf.dynamicreports.report.constant.SplitType
import net.sf.dynamicreports.report.constant.VerticalAlignment
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer
import net.sf.jasperreports.engine.util.JRSwapFile
import org.springframework.context.i18n.LocaleContextHolder


import static net.sf.dynamicreports.report.builder.DynamicReports.*
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder
abstract class ReportBuilder {
    DynamicReportService dynamicReportService = Holders.applicationContext.getBean("dynamicReportService")
    static ImageService imageService = Holders.applicationContext.getBean("imageService")
    CustomMessageService customMessageService = Holders.applicationContext.getBean("customMessageService")
    UserService userService = Holders.applicationContext.getBean("userService")

    static JasperReportBuilder initializeNewReport(Boolean portraitType = true, Boolean isSubreport = true , Boolean isSignalDetectionReport = false, Boolean isExcelReport = false, PageType pageType = PageType.LEDGER, PageOrientation pageOrientation = PageOrientation.PORTRAIT, int margin = -1, Boolean signalProductActions = false) {
        JasperReportBuilder report = new SignalJasperReportBuilder()
        report.setLocale(LocaleContextHolder.getLocale())
        ReportTemplateBuilder reportTemplateBuilder = Templates.reportTemplate
        VerticalListBuilder verticalList = cmp.verticalList()
        verticalList.add(cmp.line())
        if(Holders.config.signal.confidential.logo.enable && !isExcelReport){
            ImageBuilder img = cmp.image(imageService.getImage(SensitivityLabelEnum.CONFIDENTIAL.imageName)).setFixedDimension(80, 36)
            HorizontalListBuilder content = cmp.horizontalList(Templates.pageNumberingComponent, img)
                    .setPositionType(ComponentPositionType.FLOAT)
                    .setStyle(stl.style().setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM))
            verticalList.add(content)
        }
        if(Holders.config.signal.confidential.logo.enable && !isExcelReport){
            pageOrientation = !signalProductActions ? pageOrientation: PageOrientation.PORTRAIT
            Map pageFormat = calculatePageWidth(pageOrientation, pageType)
            pageFormat.width = margin != -1 ? pageFormat.width - margin : pageFormat.width - 50
        }
        report.setTemplate(reportTemplateBuilder)
        MarginBuilder margins = new MarginBuilder()
        margins.setTop(0)
        margins.setRight(25)
        margins.setLeft(25)
        margins.setBottom(25)
        report.addPageHeader(cmp.gap(10, 5))
        reportTemplateBuilder.setSummarySplitType(SplitType.IMMEDIATE)
        reportTemplateBuilder.setSummaryWithPageHeaderAndFooter(true)

        if(!isSubreport){
            report.pageFooter(verticalList)
            report.setPageFooterStyle(stl.style().setBottomPadding(10).setHorizontalAlignment(HorizontalAlignment.CENTER))
        }

        report.setPageMargin(margins)
        if(isExcelReport) {
            report.setIgnorePagination(true)
        }

        return report
    }

    static Map calculatePageWidth(PageOrientation pageOrientation = PageOrientation.PORTRAIT, PageType pageType){
        if (pageOrientation == PageOrientation.PORTRAIT){
            return [width: pageType.getWidth() , height: pageType.getHeight()]
        } else {
            return  [width: pageType.getHeight(), height: pageType.getWidth()]
        }
    }
    static JasperReportBuilder initializeNewCaseDetailReport(Boolean portraitType = true, Boolean isSubreport = true , Boolean isSignalDetectionReport = false, Boolean isExcelReport = false) {
        JasperReportBuilder report = new SignalJasperReportBuilder()
        report.setLocale(LocaleContextHolder.getLocale())
        ReportTemplateBuilder reportTemplateBuilder = Templates.reportTemplate
        VerticalListBuilder verticalList = cmp.verticalList()
        verticalList.add(cmp.line())
        report.setTemplate(reportTemplateBuilder)
        MarginBuilder margins = new MarginBuilder()
        margins.setTop(0)
        margins.setRight(25)
        margins.setLeft(25)
        margins.setBottom(25)
        report.addPageHeader(cmp.gap(10, 5))
        reportTemplateBuilder.setSummarySplitType(SplitType.IMMEDIATE)
        reportTemplateBuilder.setSummaryWithPageHeaderAndFooter(true)

        if(!isSubreport){
            report.pageFooter(verticalList)
            report.setPageFooterStyle(stl.style().setBottomPadding(10).setHorizontalAlignment(HorizontalAlignment.CENTER))
        }

        report.setPageMargin(margins)
        if(isExcelReport) {
            report.setIgnorePagination(true)
        }

        return report
    }

    static JasperReportBuilder initializeMemoReport(Boolean portraitType = true, Boolean isSubreport = true , Boolean isSignalDetectionReport = false, Boolean isExcelReport = false) {

        JasperReportBuilder report = new SignalJasperReportBuilder()
        report.setLocale(LocaleContextHolder.getLocale())
        ReportTemplateBuilder reportTemplateBuilder = Templates.reportTemplate
        VerticalListBuilder verticalList = cmp.verticalList()
        verticalList.add(cmp.line())
        if(Holders.config.signal.confidential.logo.enable && !isExcelReport) {
            ImageBuilder img = cmp.image(imageService.getImage(SensitivityLabelEnum.CONFIDENTIAL.imageName)).setFixedDimension(80, 36)
            HorizontalListBuilder content = cmp.horizontalList(Templates.pageNumberingComponent, img)
                    .setStyle(stl.style().setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM).setTopPadding(50))
            verticalList.add(content)
        }
        report.setTemplate(reportTemplateBuilder)
        MarginBuilder margins = new MarginBuilder()
        margins.setTop(0)
        margins.setRight(25)
        margins.setLeft(25)
        margins.setBottom(0)
        report.addPageHeader(cmp.gap(10, 5))
        reportTemplateBuilder.setSummarySplitType(SplitType.IMMEDIATE)
        reportTemplateBuilder.setSummaryWithPageHeaderAndFooter(true)

        if(!isSubreport){
            report.pageFooter(verticalList)
            report.setPageFooterStyle(stl.style().setBottomPadding(10).setHorizontalAlignment(HorizontalAlignment.CENTER))
        }

        report.setPageMargin(margins)
        if(isExcelReport) {
            report.setIgnorePagination(true)
        }

        return report
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
            //Criteria sheet is added when calling screen is dashBoard
                criteriaMap.each {
                    addCriteriaSheetHeaderTable(criteriaList, it.label, it.value)
                }
        }
        VerticalListBuilder verticalList = cmp.verticalList()
        verticalList.add(cmp.verticalList(criteriaList))
        JasperReportBuilder report = criteriaDataReport.summary(verticalList)
        report.setPageFormat(getPageType(), PageOrientation.LANDSCAPE)
        report
    }

    protected JasperReportBuilder buildCriteriaSheetReport(Map params, String title = null) {
        String criteriaTitle = title ? title : customMessageService.getMessage('jasperReports.criteriaSheet', null, "Criteria Sheet", Locale.default)
        JasperReportBuilder criteriaDataReport = initializeNewReport(params.portraitType as Boolean, false, true,
                (params.outputFormat == ReportFormat.XLSX.name()), getPageType(), getPageOrientation())
        HorizontalListBuilder criteriaList = cmp.horizontalList()
        def filler = cmp.filler().setStyle(stl.style().setTopBorder(stl.penDouble())).setFixedHeight(1)
        if (params.criteriaSheetList) {
            if (params.showCompanyLogo) {
                ImageBuilder img = cmp.image(imageService.getImage(Constants.DynamicReports.COMPANY_LOGO))
                        .setFixedDimension(80, 36).setStyle(stl.style().setHorizontalAlignment(HorizontalAlignment.LEFT).setTopPadding(5))
                criteriaList.add(cmp.verticalList(img)).newRow()
            }
            criteriaList.add(cmp.text(criteriaTitle).setStyle(Templates.pageHeader_TitleStyle)).newRow()
            criteriaList.add(cmp.verticalList(filler))
            params.criteriaSheetList.each {
                addCriteriaSheetHeaderTable(criteriaList, it.label, it.value)
            }
        }
        VerticalListBuilder verticalList = cmp.verticalList()
        verticalList.add(cmp.verticalList(criteriaList))
        JasperReportBuilder report = criteriaDataReport.summary(verticalList)
        report.setPageFormat(getPageType(), getPageOrientation())
        report
    }

    protected JasperReportBuilder buildReport(JRDataSource dataSource, Map params, String title = "", Boolean isSubreport = true, int margin = 50) {

        VerticalListBuilder verticalList = cmp.verticalList()
        JasperReportBuilder signalDataReport
        //for excel report, need to set excel printable page header
        if (params.outputFormat == ReportFormat.XLSX.name()) {
            signalDataReport = new SignalJasperReportBuilder()
            setPrintablePageHeaderExcel(signalDataReport)
            signalDataReport.setPageFormat(getPageType(), getPageOrientation())
        } else {
            signalDataReport = initializeNewReport(params.portraitType, false, false, (params.outputFormat == ReportFormat.XLSX.name()), getPageType(), PageOrientation.LANDSCAPE, margin,params.SignalProductActions)
            signalDataReport.setPageFormat(getPageType(), getPageOrientation())
        }

        def criteraSheet = {
            HorizontalListBuilder signalSummaryList = cmp.horizontalList()
            //excel report check
            if (params.outputFormat != ReportFormat.XLSX.name()) {
                ImageBuilder img = cmp.image(imageService.getImage(Constants.DynamicReports.COMPANY_LOGO))
                        .setFixedDimension(80, 36).setStyle(stl.style().setHorizontalAlignment(HorizontalAlignment.LEFT).setTopPadding(5))
                signalSummaryList.add(cmp.verticalList(img)).newRow()
            }
            def filler = cmp.filler().setStyle(stl.style().setTopBorder(stl.penDouble())).setFixedHeight(10)

            signalSummaryList.add(cmp.text("Criteria Sheet").setStyle(Templates.pageHeader_TitleStyle)).newRow()

            signalSummaryList.add(cmp.verticalList(filler))
            params.criteriaSheetList.each {
                if(!(params.adhocRun?.toBoolean() && it.label == Constants.CriteriaSheetLabels.DISPOSITIONS)) {
                    addCriteriaSheetHeaderTable(signalSummaryList, it.label, it.value)
                }
            }
            verticalList.add(cmp.verticalGap(10))
            verticalList.add(cmp.verticalList(signalSummaryList))
            verticalList.add(cmp.verticalGap(10))
            verticalList.add(cmp.pageBreak())
        }
        //criteria sheet shall be added in pdf and doc only from this action
        if (params.criteriaSheetList && params.outputFormat != ReportFormat.XLSX.name()) {
            criteraSheet()
        }
        //creating subreport for alert trigger data
        JasperReportBuilder report
        if (params.outputFormat == ReportFormat.XLSX.name()) {
            report = new SignalJasperReportBuilder()
        } else {
            report = initializeNewReport(params.portraitType, true, false, (params.outputFormat == ReportFormat.XLSX.name()), getPageType(), getPageOrientation())
        }
        // this code is for Batch ETL export Logs
        if(params.summaryList) {
            List summaryList = params.summaryList
            HorizontalListBuilder aggregateList = cmp.horizontalList()
            if(params.etlLogExport) {
                aggregateList.add(cmp.text("ETL Log").setStyle(Templates.pageHeader_TitleStyle)).newRow()
            } else {
                aggregateList.add(cmp.text("Import Log").setStyle(Templates.pageHeader_TitleStyle)).newRow()
            }
            summaryList.each {
                aggregateList.add(cmp.text(it.label).setFixedColumns(20).setStyle(Templates.criteriaNameStyle),
                        cmp.text(it.value).setFixedColumns(20).setStyle(Templates.criteriaValueStyle)).newRow()
            }
            verticalList.add(cmp.verticalList(aggregateList))
        }
        def virtualizer = new JRSwapFileVirtualizer(dynamicReportService.getSwapVirtualizerMaxSize(),
                new JRSwapFile(
                        dynamicReportService.getReportsDirectory(),
                        dynamicReportService.getBlockSize(),
                        dynamicReportService.getMinGrowCount()
                )
        )
        report.setDataSource(dataSource).setVirtualizer(virtualizer)
        report.setPageFormat(getPageType(), getPageOrientation())
        if (title.equalsIgnoreCase(Constants.SignalReportTypes.SIGNALS_BY_STATE)) {
            params.put("title", title)
        }
        addColumns(report, params)
        params.showCompanyLogo = true
        if (params.outputFormat == ReportFormat.XLSX.name()) {
            if (params.signalActionReport) {
                setPrintablePageHeader(report, getReportHeader(params), false, params, title)
            } else {
                setPrintablePageHeaderExcel(report)
                setPageHeaderText(report, getReportHeader(params), false, title)
            }
        } else {
            params.showCompanyLogo = true
            setPrintablePageHeader(report, getReportHeader(params), false, params, title)
        }
        dynamicReportService.noDataCheck(dataSource, report)
        //for pdf and doc reports
        verticalList.add(cmp.subreport(report))
        JasperReportBuilder newReport = signalDataReport.summary(verticalList)
        virtualizer.cleanup()
        newReport
    }

    protected JasperReportBuilder buildXlsxReport(JRDataSource dataSource, Map params, String title = "") {
        VerticalListBuilder verticalList = cmp.verticalList()
        JasperReportBuilder signalDataReport = new SignalJasperReportBuilder()
        setPrintablePageHeaderExcel(signalDataReport)
        signalDataReport.setPageFormat(getPageType(), getPageOrientation())

        // this code is for Batch ETL export Logs
        if (params.summaryList) {
            List summaryList = params.summaryList
            HorizontalListBuilder aggregateList = cmp.horizontalList()
            if (params.etlLogExport) {
                aggregateList.add(cmp.text("ETL Log").setStyle(Templates.pageHeader_TitleStyle)).newRow()
            } else {
                aggregateList.add(cmp.text("Import Log").setStyle(Templates.pageHeader_TitleStyle)).newRow()
            }
            summaryList.each {
                aggregateList.add(cmp.text(it.label).setFixedColumns(20).setStyle(Templates.criteriaNameStyle),
                        cmp.text(it.value).setFixedColumns(20).setStyle(Templates.criteriaValueStyle)).newRow()
            }
            verticalList.add(cmp.verticalList(aggregateList))
        }

        JasperReportBuilder report = new SignalJasperReportBuilder()

        def virtualizer = new JRSwapFileVirtualizer(dynamicReportService.getSwapVirtualizerMaxSize(),
                new JRSwapFile(
                        dynamicReportService.getReportsDirectory(),
                        dynamicReportService.getBlockSize(),
                        dynamicReportService.getMinGrowCount()
                )
        )
        report.setDataSource(dataSource).setVirtualizer(virtualizer)
        report.setPageFormat(getPageType(), getPageOrientation())
        if (title.equalsIgnoreCase(Constants.SignalReportTypes.SIGNALS_BY_STATE)) {
            params.put("title", title)
        }
        addColumns(report, params)

        if (params.signalActionReport) {
            setPrintablePageHeader(report, getReportHeader(params), false, params, title)
        } else {
            setPrintablePageHeaderExcel(report)
            setPageHeaderText(report, getReportHeader(params), false, title)
        }

        dynamicReportService.noDataCheck(dataSource, report)

        verticalList.add(cmp.subreport(report))

        JasperReportBuilder newReport = signalDataReport.summary(verticalList)
        virtualizer.cleanup()
        newReport
    }

    protected JasperReportBuilder buildPdfDocxReport(JRDataSource dataSource, List<Tuple2<String, JasperReportBuilder>> subreports, Map params, String title = "", Boolean showCriteria = true) {

        VerticalListBuilder verticalList = cmp.verticalList()
        JasperReportBuilder signalDataReport = initializeNewReport(params.portraitType as Boolean, false, false, false,
                getPageType(), getPageOrientation(), 50, params.SignalProductActions as Boolean)
        signalDataReport.setPageFormat(getPageType(), getPageOrientation())


        def criteriaSheet = {
            HorizontalListBuilder criteriaList = cmp.horizontalList()

            ImageBuilder img = cmp.image(imageService.getImage(Constants.DynamicReports.COMPANY_LOGO))
                    .setFixedDimension(80, 36).setStyle(stl.style().setHorizontalAlignment(HorizontalAlignment.LEFT).setTopPadding(5))
            criteriaList.add(cmp.verticalList(img)).newRow()

            def filler = cmp.filler().setStyle(stl.style().setTopBorder(stl.penDouble())).setFixedHeight(10)

            String criteriaSheetTitle = params.criteriaSheetTitle ? params.criteriaSheetTitle
                    : customMessageService.getMessage('jasperReports.criteriaSheet', null, "Criteria Sheet", Locale.default)
            criteriaList.add(cmp.text(criteriaSheetTitle).setStyle(Templates.pageHeader_TitleStyle)).newRow()

            criteriaList.add(cmp.verticalList(filler))
            params.criteriaSheetList.each {
                if(!(params.adhocRun?.toBoolean() && it.label == Constants.CriteriaSheetLabels.DISPOSITIONS)) {
                    addCriteriaSheetHeaderTable(criteriaList, it.label, it.value)
                }
            }
            verticalList.add(cmp.verticalGap(10))
            verticalList.add(cmp.verticalList(criteriaList))
            verticalList.add(cmp.verticalGap(10))
            verticalList.add(cmp.pageBreak())
        }
        //criteria sheet shall be added in pdf and doc only from this action
        if (params.criteriaSheetList && showCriteria) {
            criteriaSheet()
        }

        if (dataSource) {
            JasperReportBuilder report = initializeNewReport(params.portraitType as Boolean, true, false, false,
                    getPageType(), getPageOrientation())
            // this code is for Batch ETL export Logs
            if (params.summaryList) {
                List summaryList = params.summaryList
                HorizontalListBuilder aggregateList = cmp.horizontalList()
                if (params.etlLogExport) {
                    aggregateList.add(cmp.text("ETL Log").setStyle(Templates.pageHeader_TitleStyle)).newRow()
                } else {
                    aggregateList.add(cmp.text("Import Log").setStyle(Templates.pageHeader_TitleStyle)).newRow()
                }
                summaryList.each {
                    aggregateList.add(cmp.text(it.label).setFixedColumns(20).setStyle(Templates.criteriaNameStyle),
                            cmp.text(it.value).setFixedColumns(20).setStyle(Templates.criteriaValueStyle)).newRow()
                }
                verticalList.add(cmp.verticalList(aggregateList))
            }
            report.setDataSource(dataSource)
                    .setVirtualizer(new JRSwapFileVirtualizer(dynamicReportService.getSwapVirtualizerMaxSize(),
                            new JRSwapFile(dynamicReportService.getReportsDirectory(), dynamicReportService.getBlockSize(), dynamicReportService.getMinGrowCount())))
            report.setPageFormat(getPageType(), getPageOrientation())
            if (title.equalsIgnoreCase(Constants.SignalReportTypes.SIGNALS_BY_STATE)) {
                params.put("title", title)
            }
            addColumns(report, params)

            setPrintablePageHeader(report, getReportHeader(params), false, params, title)

            dynamicReportService.noDataCheck(dataSource, report)

            verticalList.add(cmp.subreport(report))
        }

        if (subreports) {
            verticalList.add(cmp.verticalGap(10))
            subreports.forEach {
                verticalList.add(cmp.subreport(it.second))
                verticalList.add(cmp.verticalGap(10))
            }
        }

        JasperReportBuilder newReport = signalDataReport.summary(verticalList)
        newReport
    }

    private void addCriteriaSheetHeaderTable(HorizontalListBuilder list, def labelId, def value) {
        if (value instanceof List) {
            value = value.join(',')
        }
        list.add(cmp.text(labelId).setFixedColumns(25).setStyle(Templates.criteriaNameStyle),
                cmp.text(value).setStyle(Templates.criteriaValueStyle)).newRow()
    }
    protected abstract void addColumns(JasperReportBuilder report, Map params)

    protected abstract String getReportHeader(Map params);

    protected StyleBuilder getReportHeaderStyle() {
        return Templates.pageHeader_HeaderStyle
    }

    protected abstract PageType getPageType();

    protected abstract PageOrientation getPageOrientation();

    protected String setPrintablePageHeader(JasperReportBuilder report, String customReportHeader, boolean isCriteriaSheetOrAppendix, Map params, String title) {
        if (params.showCompanyLogo && !params.signalActionReport) {
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
        return setPageHeaderText(report, customReportHeader, isCriteriaSheetOrAppendix, title)
    }

    protected void setPrintablePageHeaderExcel(JasperReportBuilder report) {
        report.ignorePagination()
        report.ignorePageWidth()
        report.setDefaultFont(Templates.defaultFontStyle)
        report.setColumnTitleStyle(Templates.columnTitleStyle)
        report.setPageMargin(margin(0))
        report.setColumnStyle(Templates.columnStyle)
    }

    private String setPageHeaderText(JasperReportBuilder report, String customReportHeader, boolean isCriteriaSheetOrAppendix, String title) {
        String header = customReportHeader
        String printDateRange = "false"

        TextFieldBuilder textFieldTitle = cmp.text(!isCriteriaSheetOrAppendix ? title : "").setStyle(Templates.pageHeader_TitleStyle)
        TextFieldBuilder textFieldDateRange = null

        def filler = cmp.filler().setStyle(stl.style().setTopBorder(stl.penDouble())).setFixedHeight(10)

        if (header) {
            TextFieldBuilder textFieldHeader = cmp.text(header).setStyle(getReportHeaderStyle())
            if (printDateRange == "true" && !isCriteriaSheetOrAppendix) {
                report.pageHeader(cmp.verticalList(textFieldTitle, textFieldHeader, textFieldDateRange).add(filler))
            } else {
                report.pageHeader(cmp.verticalList(textFieldTitle, textFieldHeader).add(filler))
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

    protected List<Tuple2<String, JasperReportBuilder>> createSubReports(Map params, List<ReportDetails> reportDetails) {
        List<Tuple2<String, JasperReportBuilder>> reports = []

        reportDetails.forEach {
            if (it.data) {
                JasperReportBuilder subReport = createSubReportFromList(params, it)
                reports.add(new Tuple2<>(it.title, subReport))
            }
        }

        return reports
    }

    private JasperReportBuilder createSubReportFromList(Map params, ReportDetails details) {
        JasperReportBuilder subReport = initializeNewReport(params.portraitType as Boolean, true, false, (params.outputFormat == ReportFormat.XLSX.name()), getPageType(), getPageOrientation())
        subReport.setDataSource(new JRMapCollectionDataSource(details.data))
        addColumns(subReport, params)
        if (params.outputFormat != ReportFormat.XLSX.name()) {
            setPrintablePageHeader(subReport, details.subTitle, false, params, details.title)
        } else {
            setPrintablePageHeaderExcel(subReport)
            setPageHeaderText(subReport, details.subTitle, false, details.title)
        }
        subReport.setPageFormat(getPageType(), getPageOrientation())
        return subReport
    }
}
