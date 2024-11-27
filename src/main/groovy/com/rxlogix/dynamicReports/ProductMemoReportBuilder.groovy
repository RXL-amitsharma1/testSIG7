package com.rxlogix.dynamicReports

import com.rxlogix.Constants
import com.rxlogix.CustomMessageService
import com.rxlogix.enums.ReportFormat
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.MarginBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder
import net.sf.dynamicreports.report.builder.component.ImageBuilder
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder
import net.sf.dynamicreports.report.constant.HorizontalAlignment
import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.constant.PageType
import net.sf.dynamicreports.report.constant.WhenNoDataType
import net.sf.jasperreports.engine.JRDataSource
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.stl
import static net.sf.dynamicreports.report.builder.DynamicReports.type

class ProductMemoReportBuilder extends ReportBuilder {
    CustomMessageService customMessageService = Holders.applicationContext.getBean("customMessageService")

    void createReport(JRDataSource singleCaseAlertsSummaryList, JRDataSource aggregateCaseAlertsSummaryList,
                          signalData, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        Boolean isNoDataInReport = false
        // Added for PVS-55067
        if(!singleCaseAlertsSummaryList?.data?.value && !aggregateCaseAlertsSummaryList.data?.value){
            isNoDataInReport = true
        }
        params.isNoDataInReport = isNoDataInReport

        if (params.outputFormat == ReportFormat.XLSX.name()) {
            JasperReportBuilderEntry criteriaJasperReportBuilderEntry = new JasperReportBuilderEntry()
            criteriaJasperReportBuilderEntry.jasperReportBuilder = buildCriteriaReport(params.criteriaSheetList, params)
            criteriaJasperReportBuilderEntry.excelSheetName = "Criteria"
            jasperReportBuilderEntryList.add(criteriaJasperReportBuilderEntry)
            JasperReportBuilder reportSheet0 = buildReport1(singleCaseAlertsSummaryList, signalData, params)
            JasperReportBuilderEntry jasperReportBuilderEntry0 = new JasperReportBuilderEntry()
            jasperReportBuilderEntry0.jasperReportBuilder = reportSheet0
            jasperReportBuilderEntry0.excelSheetName = "Product Memo Report"
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry0)
        }
        if (dynamicReportService.isInPrintMode(params)) {
            String header
            params.showCriteria = true
            if (singleCaseAlertsSummaryList) {
                header = customMessageService.getMessage("jasperReports.${getClass().getSimpleName()}.qualitative.title")
                params.isSingleCaseAlert = true
                JasperReportBuilder reportSheet1 = buildReport(header, singleCaseAlertsSummaryList, signalData, params)
                JasperReportBuilderEntry jasperReportBuilderEntry1 = new JasperReportBuilderEntry()
                jasperReportBuilderEntry1.jasperReportBuilder = reportSheet1
                jasperReportBuilderEntry1.excelSheetName = header
                jasperReportBuilderEntryList.add(jasperReportBuilderEntry1)
            }
            if (aggregateCaseAlertsSummaryList) {
                header = customMessageService.getMessage("jasperReports.${getClass().getSimpleName()}.quantitative.title")
                params.isSingleCaseAlert = false
                JasperReportBuilder reportSheet2 = buildReport(header, aggregateCaseAlertsSummaryList, signalData, params)
                JasperReportBuilderEntry jasperReportBuilderEntry2 = new JasperReportBuilderEntry()
                jasperReportBuilderEntry2.jasperReportBuilder = reportSheet2
                jasperReportBuilderEntry2.excelSheetName = header
                jasperReportBuilderEntryList.add(jasperReportBuilderEntry2)
            }
        }
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
                criteriaMap.each {
                    addSingleCaseCriteriaHeaderTable(criteriaList, it.label, it.value)
                }
        }
        VerticalListBuilder verticalList = cmp.verticalList()
        verticalList.add(cmp.verticalList(criteriaList))
        JasperReportBuilder report = criteriaDataReport.summary(verticalList)
        report.setPageFormat(getPageType(), PageOrientation.LANDSCAPE)
        report
    }
        private void addSingleCaseCriteriaHeaderTable(HorizontalListBuilder list, String labelId, String value) {
            list.add(cmp.text(labelId).setFixedColumns(10).setStyle(Templates.criteriaNameStyle),
                    cmp.text(value).setStyle(Templates.criteriaValueStyle)).newRow()
        }

    private JasperReportBuilder buildReport1(JRDataSource signalList, signalData, Map params) {
        JasperReportBuilder mainReport = initializeNewReport(true, false, false, (params.outputFormat == ReportFormat.XLSX.name()), getPageType(), getPageOrientation())
        JasperReportBuilder signalDataReport = initializeNewReport(true, true, false, (params.outputFormat == ReportFormat.XLSX.name()), getPageType(), getPageOrientation())
        VerticalListBuilder verticalList = cmp.verticalList()

            HorizontalListBuilder signalSummaryList = cmp.horizontalList()
            def filler = cmp.filler().setStyle(stl.style().setTopBorder(stl.penDouble())).setFixedHeight(1)
            verticalList.add(cmp.text(getReportHeader(params)).setStyle(Templates.pageHeader_TitleStyle))
            addSignalSummaryHeaderTable(signalSummaryList, 'Product Name', signalData.productName)
            addSignalSummaryHeaderTable(signalSummaryList, 'Date Range', signalData.dateRange)
            verticalList.add(cmp.verticalList(signalSummaryList))
            verticalList.add(cmp.subreport(signalDataReport))


        params.showCompanyLogo = false
        params.showLogo = false
        Boolean isNoDataInReport = params.isNoDataInReport ? params.isNoDataInReport.asBoolean() : false
        mainReport.setDefaultFont(Templates.defaultFontStyle)
        mainReport.setColumnTitleStyle(Templates.columnTitleStyle)
        mainReport.setColumnStyle(Templates.columnStyle)
        mainReport.summary(verticalList)
        mainReport.setPageMargin(new MarginBuilder().setBottom(0).setTop(0).setRight(16).setLeft(12) )
        mainReport.setPageFormat(getPageType(), getPageOrientation())
        if (params.outputFormat == ReportFormat.XLSX.name()) {
            mainReport.ignorePagination()
        }
        noDataCheck(signalList, mainReport,isNoDataInReport)
    }

    private JasperReportBuilder buildReport(String header, JRDataSource signalList, signalData, Map params) {
        JasperReportBuilder mainReport = initializeNewReport(true, false, false, (params.outputFormat == ReportFormat.XLSX.name()), getPageType(), getPageOrientation())
        JasperReportBuilder signalDataReport = ReportBuilder.initializeMemoReport(true, true)
        VerticalListBuilder verticalList = cmp.verticalList()
        ImageBuilder img = cmp.image(imageService.getImage(Constants.DynamicReports.COMPANY_LOGO))
                .setFixedDimension(80, 36).setStyle(stl.style().setHorizontalAlignment(HorizontalAlignment.LEFT).setTopPadding(5))

        def criteraSheet = {
            HorizontalListBuilder signalSummaryList = cmp.horizontalList()
            //excel report check
            signalSummaryList.add(cmp.verticalList(img)).newRow()
            def filler = cmp.filler().setStyle(stl.style().setTopBorder(stl.penDouble())).setFixedHeight(10)
            signalSummaryList.add(cmp.text("Criteria Sheet").setStyle(Templates.pageHeader_TitleStyle)).newRow()
            signalSummaryList.add(cmp.verticalList(filler))
            params.criteriaSheetList.each {
                    addCriteriaSheetHeaderTable(signalSummaryList, it.label, it.value)
            }
            verticalList.add(cmp.verticalGap(10))
            verticalList.add(cmp.verticalList(signalSummaryList))
            verticalList.add(cmp.verticalGap(10))
            verticalList.add(cmp.pageBreak())
        }
        //criteria sheet shall be added in pdf and doc only from this action
        if (params.criteriaSheetList && params.outputFormat != ReportFormat.XLSX.name() && params.showCriteria) {
            criteraSheet()
            params.showCriteria = false
        }
        if (params.outputFormat == ReportFormat.XLSX.name()) {
            setPrintablePageHeaderExcel(signalDataReport)
            verticalList.add(cmp.text(getReportHeader(params)).setStyle(Templates.pageHeader_TitleStyle))
            verticalList.add(cmp.text(header).setStyle(Templates.pageHeader_TitleStyle))
            verticalList.add(cmp.subreport(signalDataReport))
        } else {
            HorizontalListBuilder signalSummaryList = cmp.horizontalList()
            signalSummaryList.add(cmp.verticalList(img)).newRow()
            signalSummaryList.add(cmp.text(getReportHeader(params)).setStyle(Templates.pageHeader_TitleStyle)).newRow()
            signalSummaryList.add(cmp.text(header).setStyle(Templates.pageHeader_TitleStyle)).newRow()
            def filler = cmp.filler().setStyle(stl.style().setTopBorder(stl.penDouble())).setFixedHeight(1)
            addSignalSummaryHeaderTable(signalSummaryList, 'Product Name', signalData.productName)
            addSignalSummaryHeaderTable(signalSummaryList, 'Date Range', signalData.dateRange)
            verticalList.add(cmp.verticalList(signalSummaryList))
            verticalList.add(cmp.horizontalGap(2))
            verticalList.add(cmp.subreport(signalDataReport))
            verticalList.add(cmp.verticalList(filler))
        }
        if (params.outputFormat == ReportFormat.XLSX.name()) {
            params.showCompanyLogo = false
            params.showLogo = false
        }
        Boolean isNoDataInReport = params.isNoDataInReport ? params.isNoDataInReport.asBoolean() : false
        if (signalList) {
            JasperReportBuilder dataSourceValidatedReport = ReportBuilder.initializeNewReport(true,true,false,(params.outputFormat == ReportFormat.XLSX.name()), getPageType(), getPageOrientation())
            dataSourceValidatedReport.setDataSource(signalList)
            addColumns(dataSourceValidatedReport, params)
            dataSourceValidatedReport.setColumnTitleStyle(Templates.columnTitleStyle)
            dataSourceValidatedReport.setColumnStyle(Templates.columnStyle)
            verticalList.add(cmp.subreport(dataSourceValidatedReport))
            dataSourceValidatedReport.ignorePagination()
            dataSourceValidatedReport.setColumnHeaderStyle(Templates.columnTitleStyleMemo)
        }

        mainReport.summary(verticalList)
        mainReport.setPageMargin(new MarginBuilder().setBottom(25).setTop(0).setRight(16).setLeft(12) )
        mainReport.setPageFormat(getPageType(), getPageOrientation())
        if (params.outputFormat == ReportFormat.XLSX.name()) {
            mainReport.ignorePagination()
        }
        noDataCheck(signalList, mainReport,isNoDataInReport)
    }
    def noDataCheck(dataSource, report,Boolean isNoDataInReport = false){
        if (isNoDataInReport) {
            report.setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL)
            report.columnHeader(cmp.text(customMessageService.getMessage("app.label.report.blank")).setHeight(7));
        }
        report
    }

    private void addSignalSummaryHeaderTable(HorizontalListBuilder list, String labelId, String value) {
        list.add(cmp.text(labelId + ":").setFixedColumns(10).setStyle(Templates.criteriaNameStyle),
                cmp.text(value).setStyle(Templates.criteriaValueStyle)).newRow()
    }

    @Override
    protected void addColumns(JasperReportBuilder report, Map params) {
        if(params.isSingleCaseAlert) {
            report.addColumn(Columns.column("Case Number", "caseNumber", type.stringType()))
        }
                report.addColumn(Columns.column("Product", "product", type.stringType()))
                .addColumn(Columns.column("Event", "event", type.stringType()))
                .addColumn(Columns.column("Disposition", "disposition", type.stringType()))
                .addColumn(Columns.column("Disposition Justification", "justification", type.stringType()))
                .addColumn(Columns.column("Priority", "priority", type.stringType()))
                .addColumn(Columns.column("Priority Justification", "priorityJustification", type.stringType()))
                .addColumn(Columns.column("Signal Name", "signalName", type.stringType()))
                .addColumn(Columns.column("Last Updated Timestamp", "lastUpdateTimestamp", type.stringType()))
                .addColumn(Columns.column("Due/Overdue", "due/overdue", type.stringType()))
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
