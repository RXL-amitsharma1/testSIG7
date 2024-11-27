package com.rxlogix.dynamicReports

import com.rxlogix.Constants
import com.rxlogix.TestSignalService
import com.rxlogix.enums.ReportFormat
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder
import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.constant.PageType
import net.sf.jasperreports.engine.JRDataSource


import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.type

class TestSignalReportBuilder extends ReportBuilder {

    TestSignalService testSignalService = Holders.applicationContext.getBean("testSignalService")

    void createReport(JRDataSource dataSource, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        if (dynamicReportService.isInPrintMode(params)) {
            JasperReportBuilder reportSheet = buildReport(dataSource, params, "", false)
            reportSheet.setPageFormat(getPageType(), PageOrientation.LANDSCAPE)
            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = reportSheet
            jasperReportBuilderEntry.excelSheetName = customMessageService.getMessage("jasperReports.${getClass().getSimpleName()}.title")
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }

    protected JasperReportBuilder buildReport(JRDataSource dataSource, Map params, String title = "", Boolean isSubreport = true){

        VerticalListBuilder verticalList = cmp.verticalList()
        JasperReportBuilder signalDataReport = ReportBuilder.initializeNewReport(params.portraitType, false, false, (params.outputFormat == ReportFormat.XLSX.name()), getPageType(), getPageOrientation())
        signalDataReport.setPageFormat(getPageType(), getPageOrientation())

        createChartSubReport(verticalList, params)

        JasperReportBuilder subReport = ReportBuilder.initializeNewReport(params.portraitType, true, false, (params.outputFormat == ReportFormat.XLSX.name()), getPageType(), getPageOrientation())

        subReport.setDataSource(dataSource)
        addColumns(subReport, params)
        subReport.setColumnTitleStyle(Templates.columnStyleBold)
        verticalList.add(cmp.subreport(subReport))
        verticalList.add(cmp.verticalGap(20))


        JasperReportBuilder report = signalDataReport.summary(verticalList)
        report.setPageFormat(PageType.LEDGER, PageOrientation.LANDSCAPE)
        report

    }

    void createChartSubReport(VerticalListBuilder verticalList, Map params){
        SimpleChartBuilder chartBuilder = new SimpleChartBuilder()

        JasperReportBuilder chartReportBuilder = chartBuilder.createSubChart(params, testSignalService.getDataForChartBuilder())

        setPrintablePageHeader(chartReportBuilder, 'Test Case Stats', false, params, "Test Case Stats")
        verticalList.add(cmp.pageBreak())

        verticalList.add(cmp.subreport(chartReportBuilder))
        verticalList.add(cmp.verticalGap(20))
    }




    @Override
    protected void addColumns(JasperReportBuilder report, Map params) {
        List<Map> fixedColumnList = [
                ["label": "Name", "name": "name"],
                ["label": "Alert Type", "name": "alertType"],
                ["label": "Product", "name": "product"],
                //["label": "Run Duration", "name": "executionTime"],
                ["label": "Owner", "name": 'owner'],
                ["label": "Execution Status", "name": 'executionStatus'],
                ["label": "PEC Count", "name": 'peCount']
        ]
        fixedColumnList.each {
            if(it.name == "peCount"){
                report.addColumn(Columns.column(it.label, it.name, type.integerType()))
            } else if(it.name == 'executionTime'){
                report.addColumn(Columns.column(it.label, it.name, type.integerType()))
            } else {
                report.addColumn(Columns.column(it.label, it.name, type.stringType()))

            }
            }
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
        return PageOrientation.PORTRAIT
    }
}
