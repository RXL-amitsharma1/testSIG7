package com.rxlogix.dynamicReports

import com.rxlogix.AlertAttributesService
import com.rxlogix.Constants
import com.rxlogix.ViewInstanceService
import com.rxlogix.enums.ReportFormat
import com.rxlogix.signal.SystemConfig
import grails.util.Holders
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.constant.HorizontalAlignment
import net.sf.dynamicreports.report.constant.PageOrientation
import net.sf.dynamicreports.report.constant.PageType
import net.sf.jasperreports.engine.JRDataSource

import static net.sf.dynamicreports.report.builder.DynamicReports.type

class SignalListReportBuilderService extends ReportBuilder {

    ViewInstanceService viewInstanceService = Holders.applicationContext.getBean("viewInstanceService")
    AlertAttributesService alertAttributesService = Holders.applicationContext.getBean("alertAttributesService")
    void createReport(JRDataSource dataSource, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList) {
        if (dynamicReportService.isInPrintMode(params)) {
            if(params.outputFormat == ReportFormat.XLSX.name()){
                JasperReportBuilderEntry criteriaJasperReportBuilderEntry = new JasperReportBuilderEntry()
                criteriaJasperReportBuilderEntry.jasperReportBuilder = buildCriteriaReport(params.criteriaSheetList, params)
                criteriaJasperReportBuilderEntry.excelSheetName = "Criteria"
                jasperReportBuilderEntryList.add(criteriaJasperReportBuilderEntry)
            }
            JasperReportBuilder reportSheet = buildReport(dataSource, params)
            JasperReportBuilderEntry jasperReportBuilderEntry = new JasperReportBuilderEntry()
            jasperReportBuilderEntry.jasperReportBuilder = reportSheet
            jasperReportBuilderEntry.excelSheetName = customMessageService.getMessage("jasperReports.${getClass().getSimpleName()}.title")
            jasperReportBuilderEntryList.add(jasperReportBuilderEntry)
        }
    }

    @Override
    protected void addColumns(JasperReportBuilder report, Map params) {
        try {
            // FIRST ALL DEFAULT COLUMNS WILL BE DISPLAYED
            boolean enableSignalWorkflow = SystemConfig.first()?.enableSignalWorkflow
            report.addColumn(Columns.column("Signal ID", "signalId", type.stringType()))
            report.addColumn(Columns.column("Creation Date", "dateCreated", type.dateType()).setPattern("dd-MMM-yyyy").setHorizontalAlignment(HorizontalAlignment.LEFT))
            report.addColumn(Columns.column("Signal Name", "signalName", type.stringType()))
            report.addColumn(Columns.column("Product Name", "productName", type.stringType()))
            report.addColumn(Columns.column("Event Name", "eventName", type.stringType()))
            report.addColumn(Columns.column("Detected Date", "detectedDate", type.dateType()).setPattern("dd-MMM-yyyy").setHorizontalAlignment(HorizontalAlignment.LEFT))
            report.addColumn(Columns.column("Risk/Topic Category", "topicCategory", type.stringType()))
            report.addColumn(Columns.column("Linked Signal", "linkedSignal", type.stringType()))
            report.addColumn(Columns.column("Signal Source", "source", type.stringType()))
            report.addColumn(Columns.column("Evaluation Method", "evaluationMethod", type.stringType()))
            report.addColumn(Columns.column("Reason For Evaluation & Summary of Key Data", "reasonForEvaluation", type.stringType()))
            report.addColumn(Columns.column("Priority", "priority", type.stringType()))
            report.addColumn(Columns.column("Assigned To", "assignedTo", type.stringType()))
            report.addColumn(Columns.column("Action Taken", "actionTaken", type.stringType()))
            report.addColumn(Columns.column("Signal Outcome", "signalOutcome", type.stringType()))
            // split data for Additional Information/Comments field
            for (Integer i = 1; i <= params.maxSize; i++) {
                report.addColumn(Columns.column("Additional Information/Comments","comments${i}",type.stringType()))
            }

            // AFTER DEFAULT COLUMNS, DYNAMIC FILEDS WILL BE DISPLAYED

            Holders.config.signal.summary.dynamic.fields.sort {it.sequence}.each {
                if (it.fieldName == "UD_Text1" && it.enabled == true) {
                    report.addColumn(Columns.column(it.label, "udText1", type.stringType()))
                }
                if (it.fieldName == "UD_Text2" && it.enabled == true) {
                    report.addColumn(Columns.column(it.label, "udText2", type.stringType()))
                }
                if (it.fieldName == "UD_Date1" && it.enabled == true) {
                    report.addColumn(Columns.column(it.label, "udDate1", type.dateType()))
                }
                if (it.fieldName == "UD_Date2" && it.enabled == true) {
                    report.addColumn(Columns.column(it.label, "udDate2", type.dateType()))
                }
                if (it.fieldName == "UD_Dropdown1" && it.enabled == true) {
                    report.addColumn(Columns.column(it.label, "ddValue1", type.stringType()))
                }
                if (it.fieldName == "UD_Dropdown2" && it.enabled == true) {
                    report.addColumn(Columns.column(it.label, "ddValue2", type.stringType()))
                }
            }
            report.addColumn(Columns.column("Disposition", "disposition", type.stringType()))

            // AFTER DYNAMIC FIELDS, ALL CONFIGURABLE FIELDS WILL BE DISPLAYED
            if(Holders.config.validatedSignal.aggregateDate.enabled) {
                report.addColumn(Columns.column("Aggregate Report Start Date", "aggReportStartDate", type.dateType()).setPattern("dd-MMM-yyyy").setHorizontalAlignment(HorizontalAlignment.LEFT))
                report.addColumn(Columns.column("Aggregate Report End Date", "aggReportEndDate", type.dateType()).setPattern("dd-MMM-yyyy").setHorizontalAlignment(HorizontalAlignment.LEFT))
            }
            if(Holders.config.validatedSignal.show.detected.by)
                report.addColumn(Columns.column("Detected By", "detectedBy", type.stringType()))
            if(SystemConfig.first().displayDueIn){
                report.addColumn(Columns.column("Due Date", "duedateDate", type.dateType()).setPattern("dd-MMM-yyyy").setHorizontalAlignment(HorizontalAlignment.LEFT))
                report.addColumn(Columns.column("Due Date Comment", "duedateComment", type.stringType()))
            }
            if(Holders.config.validatedSignal.show.health.authority) {
                report.addColumn(Columns.column("Health Authority Signal Status", "haSignalStatus", type.stringType()))
                report.addColumn(Columns.column("Health Authority Date Closed", "haDateClosed", type.dateType()).setPattern("dd-MMM-yyyy").setHorizontalAlignment(HorizontalAlignment.LEFT))
                report.addColumn(Columns.column("Health Authority Comments on Signal Status", "commentSignalStatus", type.stringType()))
            }
            if (Holders.config.validatedSignal.shareWith.enabled) {
                report.addColumn(Columns.column("Share With", "shareWith", type.stringType()))
            }
            if(Holders.config.validatedSignal.show.topic.information)
                report.addColumn(Columns.column("Topic Information", "topicInformation", type.stringType()))

            // AFTER CONFIGURABLE FIELDS, ALL THE STATUS HISTORIES AND WORKFLOWS WILL BE DISPLAYED
            if(enableSignalWorkflow){
                List signalWorkflowStateList=[]
                Holders.config.pvsignal.signalHistoryStatusWithWorkflowState.each {
                    String status ->
                        signalWorkflowStateList.add(status)
                }
                List signalStatusList = alertAttributesService.getSignalStatusList()
                signalWorkflowStateList = (signalWorkflowStateList - signalStatusList).sort()
                signalStatusList += signalWorkflowStateList
                signalStatusList.each { String status ->
                    String dateField = "${status.trim().replace(' ', '').toLowerCase()}Date"
                    String commentField = "${status.trim().replace(' ', '').toLowerCase()}Comment"
                    if (status == 'Validation Date') {
                        report.addColumn(Columns.column("Validation Date", dateField, type.dateType()).setPattern("dd-MMM-yyyy").setHorizontalAlignment(HorizontalAlignment.LEFT))
                        report.addColumn(Columns.column("${status} Comment", commentField, type.stringType()))
                    } else if (status == 'Assessment Date') {
                        report.addColumn(Columns.column("Assessment Date", dateField, type.dateType()).setPattern("dd-MMM-yyyy").setHorizontalAlignment(HorizontalAlignment.LEFT))
                        report.addColumn(Columns.column("${status} Comment", commentField, type.stringType()))
                    } else {
                        report.addColumn(Columns.column("${status == Constants.DATE_CLOSED ? Constants.DATE_CLOSED : status + ' Date'}", dateField, type.dateType()).setPattern(Constants.DateFormat.STANDARD_DATE).setHorizontalAlignment(HorizontalAlignment.LEFT))
                        report.addColumn(Columns.column("${status} Comment", commentField, type.stringType()))
                    }
                }
            } else {
                alertAttributesService.getSignalStatusList().each { String status ->
                    String dateField = "${status.trim().replace(' ', '').toLowerCase()}Date"
                    String commentField = "${status.trim().replace(' ', '').toLowerCase()}Comment"
                    if (status == 'Validation Date') {
                        report.addColumn(Columns.column("Validation Date", dateField, type.dateType()).setPattern("dd-MMM-yyyy").setHorizontalAlignment(HorizontalAlignment.LEFT))
                        report.addColumn(Columns.column("${status} Comment", commentField, type.stringType()))
                    } else if (status == 'Assessment Date') {
                        report.addColumn(Columns.column("Assessment Date", dateField, type.dateType()).setPattern("dd-MMM-yyyy").setHorizontalAlignment(HorizontalAlignment.LEFT))
                        report.addColumn(Columns.column("${status} Comment", commentField, type.stringType()))
                    } else {
                        report.addColumn(Columns.column("${status == Constants.DATE_CLOSED ? Constants.DATE_CLOSED : status}", dateField, type.dateType()).setPattern(Constants.DateFormat.STANDARD_DATE).setHorizontalAlignment(HorizontalAlignment.LEFT))
                        report.addColumn(Columns.column("${status} Comment", commentField, type.stringType()))
                    }
                }
            }
        } catch (Exception e) {
            println e.printStackTrace()
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
        return PageOrientation.LANDSCAPE
    }
}
