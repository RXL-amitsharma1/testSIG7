package com.rxlogix.dynamicReports

import com.rxlogix.UserService
import com.rxlogix.ViewInstanceService
import com.rxlogix.signal.SystemConfig
import com.rxlogix.util.DateUtil
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.base.DRMargin
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.constant.PageType
import net.sf.jasperreports.engine.JRDataSource

import static net.sf.dynamicreports.report.builder.DynamicReports.type

class SignalMemoReportBuilder extends ReportBuilder {
    private static final int PAGE_MARGIN_BOTTOM = 0
    void createReport(JRDataSource dataSource, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        if (dynamicReportService.isInPrintMode(params)) {
            JasperReportBuilder reportSheet = buildReport(dataSource, params)
            updateReportMargin(reportSheet)
            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = reportSheet
            jasperReportBuilderEntry.excelSheetName = customMessageService.getMessage("jasperReports.${getClass().getSimpleName()}.title")
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
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
            report.addColumn(Columns.column("Signal Name", "signalName", type.stringType()))
            report.addColumn(Columns.column("Product", "productName", type.stringType()))
            report.addColumn(Columns.column("Signal Term", "eventName", type.stringType()))
            report.addColumn(Columns.column("Date Detected", "detectedDate", type.stringType()))
            report.addColumn(Columns.column("Date Validated", "dateValidated", type.stringType()))
            report.addColumn(Columns.column("Reason For Evaluation", "reasonForEvaluation", type.stringType()))
            report.addColumn(Columns.column("Action Taken/Planned", "actionTaken", type.stringType()))
            report.addColumn(Columns.column("Signal Status", "status", type.stringType()))
            report.addColumn(Columns.column("Signal Source", "signalSource", type.stringType()))
            if(Holders.config.signal.autoNotification.show.linked.signal)
                report.addColumn(Columns.column("Linked Signal", "linkedSignal", type.stringType()))
            if(Holders.config.signal.autoNotification.show.signal.outcome)
                report.addColumn(Columns.column("Signal Outcome", "signalOutcome", type.stringType()))
            if(Holders.config.signal.autoNotification.show.evaluation.method)
                report.addColumn(Columns.column("Evaluation Method", "evaluationMethod", type.stringType()))
            if(Holders.config.signal.autoNotification.show.topic.category)
                report.addColumn(Columns.column("Risk/Topic Category", "topicCategory", type.stringType()))
            if(Holders.config.signal.autoNotification.show.comments)
                report.addColumn(Columns.column("Additional Information/Comments", "comments", type.stringType()))
            if(Holders.config.signal.autoNotification.show.detected.by)
                report.addColumn(Columns.column("Detected By", "detectedBy", type.stringType()))
            if(Holders.config.signal.autoNotification.show.date.closed)
                report.addColumn(Columns.column("Date Closed", "dateClosed", type.stringType()))
        } catch (Exception e) {
            println e.printStackTrace()
        }
    }

    @Override
    protected String getReportHeader(Map params) {
        String excelSheetName = customMessageService.getMessage("jasperReports.${getClass().getSimpleName()}.title") + "\n" + "Generated on: " + "${ (DateUtil.stringFromDate(new Date(), DateUtil.DATEPICKER_FORMAT_AM_PM, params.timezone as String) + userService.getGmtOffset(params.timezone as String)) }"
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
