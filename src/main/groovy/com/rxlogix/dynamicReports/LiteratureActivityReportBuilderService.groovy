package com.rxlogix.dynamicReports

import com.rxlogix.Constants
import com.rxlogix.DynamicReportService
import com.rxlogix.ImageService
import com.rxlogix.enums.ReportFormat
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
import net.sf.jasperreports.engine.JRDataSource
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer
import net.sf.jasperreports.engine.util.JRSwapFile

import static net.sf.dynamicreports.report.builder.DynamicReports.*

class LiteratureActivityReportBuilderService {

    DynamicReportService dynamicReportService = Holders.applicationContext.getBean("dynamicReportService")
    ImageService imageService = Holders.applicationContext.getBean("imageService")

    void createReport(JRDataSource dataSource, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        if (dynamicReportService.isInPrintMode(params)) {
            if(params.outputFormat == ReportFormat.XLSX.name() ) {
                JasperReportBuilderEntry criteriaJasperReportBuilderEntry = new JasperReportBuilderEntry()
                criteriaJasperReportBuilderEntry.jasperReportBuilder = buildCriteriaReport(params.criteriaSheetList, params)
                criteriaJasperReportBuilderEntry.excelSheetName = "Criteria"
                jasperReportBuilderEntryList.add(criteriaJasperReportBuilderEntry)
            }
            JasperReportBuilder reportSheet = buildReport(dataSource, params)
            reportSheet.setPageFormat(PageType.LEDGER, PageOrientation.LANDSCAPE)
            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = reportSheet
            jasperReportBuilderEntry.excelSheetName = "Literature Alert"
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }
    protected void setPrintablePageHeaderExcel(JasperReportBuilder report) {
        report.ignorePagination()
        report.ignorePageWidth()
        report.setDefaultFont(Templates.defaultFontStyle)
        report.setColumnTitleStyle(Templates.columnTitleStyle)
        report.setPageMargin(margin(0))
        report.setColumnStyle(Templates.columnStyle)
    }
    protected void setPrintablePageHeaderExcel(SignalJasperReportBuilder report) {
        report.ignorePagination()
        report.ignorePageWidth()
        report.setDefaultFont(Templates.defaultFontStyle)
        report.setColumnTitleStyle(Templates.columnTitleStyle)
        report.setPageMargin(margin(0))
        report.setColumnStyle(Templates.columnStyle)
    }

        private JasperReportBuilder buildReport(JRDataSource dataSource, Map params, String title = "", Boolean isSubreport = true, int margin = 50) {
            VerticalListBuilder verticalList = cmp.verticalList()
            JasperReportBuilder signalDataReport
            //for excel report, need to set excel printable page header
            if (params.outputFormat == ReportFormat.XLSX.name()) {
                signalDataReport = new SignalJasperReportBuilder()
                setPrintablePageHeaderExcel(signalDataReport)
                signalDataReport.setPageFormat(PageType.LEDGER, PageOrientation.LANDSCAPE)
            } else {
                signalDataReport = ReportBuilder.initializeNewReport(params.portraitType, false, false, (params.outputFormat == ReportFormat.XLSX.name()), PageType.LEDGER, PageOrientation.LANDSCAPE, margin,params.SignalProductActions)
                signalDataReport.setPageFormat(PageType.LEDGER, PageOrientation.LANDSCAPE)
            }

            def criteraSheet = {
                HorizontalListBuilder signalSummaryList = cmp.horizontalList()
                // excel report check
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
                report = ReportBuilder.initializeNewReport(params.portraitType, true, false, (params.outputFormat == ReportFormat.XLSX.name()), PageType.LEDGER, PageOrientation.LANDSCAPE)
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
            report.setDataSource(dataSource)
                    .setVirtualizer(new JRSwapFileVirtualizer(dynamicReportService.getSwapVirtualizerMaxSize(), new JRSwapFile(dynamicReportService.getReportsDirectory(), dynamicReportService.getBlockSize(), dynamicReportService.getMinGrowCount())))
            report.setPageFormat(PageType.LEDGER, PageOrientation.LANDSCAPE)
            if (title.equalsIgnoreCase(Constants.SignalReportTypes.SIGNALS_BY_STATE)) {
                params.put("title", title)
            }
            addColumns(report, params)
            params.showCompanyLogo = true
            if (params.outputFormat == ReportFormat.XLSX.name()) {
                if (params.signalActionReport) {
                    setPrintablePageHeader(report, "Literature Alert", false, params)
                } else {
                    setPrintablePageHeaderExcel(report)
                    setPageHeaderText(report, "Literature Alert", false, title)
                }
            } else {
                params.showCompanyLogo = true
                setPrintablePageHeader(report, "Literature Alert", false, params)
            }
            dynamicReportService.noDataCheck(dataSource, report)
            //for pdf and doc reports
            verticalList.add(cmp.subreport(report))
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

    private JasperReportBuilder buildCriteriaReport(def criteriaMap, Map params) {
        JasperReportBuilder literatureCaseDataReport = ReportBuilder.initializeNewReport(params.portraitType, false , true,(params.outputFormat == ReportFormat.XLSX.name()), PageType.LEDGER, PageOrientation.LANDSCAPE)
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
        report.setPageFormat(PageType.LEDGER, PageOrientation.LANDSCAPE)
        report
    }
    private void addLiteratureCaseCriteriaHeaderTable(HorizontalListBuilder list, String labelId, String value) {
        list.add(cmp.text(labelId).setFixedColumns(10).setStyle(Templates.criteriaNameStyle),
                cmp.text(value).setStyle(Templates.criteriaValueStyle)).newRow()
    }

    private void addColumns(JasperReportBuilder report, Map params) {
        report.addColumn(Columns.column("Article Id", "articleId", type.stringType()))
                .addColumn(Columns.column("Activity Type", "type", type.stringType()))
                .addColumn(Columns.column("Search String", "searchString", type.stringType()))
                .addColumn(Columns.column("Product", "productName", type.stringType()))
                .addColumn(Columns.column("Event", "eventName", type.stringType()))
                .addColumn(Columns.column("Description", "details", type.stringType()))
                .addColumn(Columns.column("Current Assignment", "currentAssignment", type.stringType()))
                .addColumn(Columns.column("Performed By", "performedBy", type.stringType()))
                .addColumn(Columns.column("Timestamp", "timestamp", type.stringType()))
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

    private String setPageHeaderText(JasperReportBuilder report, String customReportHeader, boolean isCriteriaSheetOrAppendix,String title = "") {
        String header = customReportHeader
//        String title = ""

        String printDateRange = "false"

        TextFieldBuilder textFieldTitle = cmp.text(title).setStyle(Templates.pageHeader_TitleStyle)
        TextFieldBuilder textFieldDateRange = null

        def filler = cmp.filler().setStyle(stl.style().setTopBorder(stl.penDouble())).setFixedHeight(10)

        if (header) {
            TextFieldBuilder textFieldHeader = cmp.text(header).setStyle(Templates.pageHeader_HeaderStyle)

            //todo:  Strings like this shouldn't be used; introduce a constant (actually, this should be a boolean and not even a String at all) - morett
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
