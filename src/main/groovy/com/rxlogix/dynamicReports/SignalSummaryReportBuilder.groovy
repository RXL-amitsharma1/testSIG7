package com.rxlogix.dynamicReports

import com.rxlogix.*
import com.rxlogix.config.AlertFieldService
import com.rxlogix.user.User
import com.rxlogix.cache.CacheService
import com.rxlogix.config.ExecutedDataTabulationTemplate
import com.rxlogix.config.ReportResult
import com.rxlogix.dto.SignalChartsDTO
import com.rxlogix.dynamicReports.charts.HighPieChartBuilder
import com.rxlogix.dynamicReports.charts.HighStackedBarChartBuilder
import com.rxlogix.enums.ReportFormat
import com.rxlogix.enums.SignalAssessmentDateRangeEnum
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.SystemConfig
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.util.DateUtil
import com.rxlogix.util.RelativeDateConverter
import grails.converters.JSON
import grails.util.Holders
import groovy.json.JsonSlurper
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.base.expression.AbstractSimpleExpression
import net.sf.dynamicreports.report.builder.FieldBuilder
import net.sf.dynamicreports.report.builder.MarginBuilder
import net.sf.dynamicreports.report.builder.ReportTemplateBuilder
import net.sf.dynamicreports.report.builder.chart.AbstractChartBuilder
import net.sf.dynamicreports.report.builder.chart.LineChartBuilder
import net.sf.dynamicreports.report.builder.chart.PieChartBuilder
import net.sf.dynamicreports.report.builder.chart.StackedBarChartBuilder
import net.sf.dynamicreports.report.builder.column.Columns
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder
import net.sf.dynamicreports.report.builder.component.HorizontalListBuilder
import net.sf.dynamicreports.report.builder.component.ImageBuilder
import net.sf.dynamicreports.report.builder.component.TextFieldBuilder
import net.sf.dynamicreports.report.builder.component.VerticalListBuilder
import net.sf.dynamicreports.report.builder.style.StyleBuilder
import net.sf.dynamicreports.report.constant.*
import net.sf.dynamicreports.report.definition.ReportParameters
import net.sf.dynamicreports.report.definition.chart.DRIChartCustomizer
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.CategoryAxis
import org.jfree.chart.axis.CategoryLabelPositions
import org.jfree.chart.plot.PiePlot
import com.rxlogix.util.AlertUtil

import java.awt.*
import java.sql.Date
import java.util.List

import static net.sf.dynamicreports.report.builder.DynamicReports.*

class SignalSummaryReportBuilder implements AlertUtil {

    AlertAttributesService alertAttributesService = Holders.applicationContext.getBean("alertAttributesService")
    UserService userService = Holders.applicationContext.getBean("userService")
    ImageService imageService = Holders.applicationContext.getBean("imageService")
    ConfigurationService configurationService = Holders.applicationContext.getBean("configurationService")
    DynamicReportService dynamicReportService = Holders.applicationContext.getBean("dynamicReportService")
    CustomMessageService customMessageService = Holders.applicationContext.getBean("customMessageService")
    ValidatedSignalService validatedSignalService = Holders.applicationContext.getBean("validatedSignalService")
    ValidatedSignalChartService validatedSignalChartService = Holders.applicationContext.getBean("validatedSignalChartService")
    CacheService cacheService = Holders.applicationContext.getBean("cacheService")
    private AlertFieldService alertFieldService = Holders.applicationContext.getBean("alertFieldService")

    public static final StyleBuilder columnTitleStyle
    public static final StyleBuilder sectionTitleStyle
    public static final StyleBuilder printableRootStyle
    public static final dark_blue = new Color(91, 163, 197)
    public static final light_blue = new Color(212, 233, 239)
    public static final light_black = new Color(51, 51, 51)
    public static final grey = new Color(220, 220, 220)
    public static final blue = new Color(31,78,120)
    public static final white = new Color(255, 255, 255)

    def filler = cmp.filler().setStyle(stl.style().setTopBorder(stl.penDouble())).setFixedHeight(1)

    static {
        printableRootStyle = stl.style().setFontSize(9).setBottomPadding(0)
        columnTitleStyle = stl.style(printableRootStyle)
                .setBorder(stl.pen(0.1f, LineStyle.SOLID).setLineColor(grey))
                .setForegroundColor(white)
                .setBackgroundColor(blue)
                .setAlignment(HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE)
                .setPadding(0).setFontSize(8).setForegroundColor(new Color(255, 255, 255))
        sectionTitleStyle = stl.style(printableRootStyle)
                .setBorder(stl.pen(0.1f, LineStyle.SOLID).setLineColor(grey))
                .setForegroundColor(white)
                .setBackgroundColor(blue)
                .setAlignment(HorizontalAlignment.LEFT, VerticalAlignment.BOTTOM)
                .setPadding(0)
    }
    private JasperReportBuilder buildCriteriaReport(def criteriaMap, Map params) {
        JasperReportBuilder criteriaDataReport = ReportBuilder.initializeNewReport(params.portraitType, false, true, (params.outputFormat == ReportFormat.XLSX.name()),PageType.LEDGER, PageOrientation.LANDSCAPE)
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
            if (params.callingScreen != Constants.Commons.DASHBOARD) {
                criteriaMap.each {
                    addCriteriaHeaderTable(criteriaList, it.label, it.value)
                }
            }
        }
        VerticalListBuilder verticalList = cmp.verticalList()
        verticalList.add(cmp.verticalList(criteriaList))
        JasperReportBuilder report = criteriaDataReport.summary(verticalList)
        report.setPageFormat(PageType.LEDGER, PageOrientation.LANDSCAPE)
        report
    }
    private void addCriteriaHeaderTable(HorizontalListBuilder list, String labelId, String value) {
        list.add(cmp.text(labelId).setFixedColumns(14).setStyle(Templates.criteriaNameStyle),
                cmp.text(value).setStyle(Templates.criteriaValueStyle)).newRow()
    }

    void createReport(List quantitativeCaseAlertListingData, List qualitativeCaseAlertListingData, List adhocCaseAlertListingData,
                      List literatureCaseAlertListingData, List actionsList, List meetingList,
                      def signalSummary, def signalDetails, Map params, ArrayList<JasperReportBuilderEntry> jasperReportBuilderEntryList,
                      def summaryReportPreference, List assessmentDetailsList, List medicalConceptDistributionList, Boolean isConsolidatedReport,
                      Boolean uploadAssessment=false,String addReferenceName=null,Boolean criteriaSheetCheck = false) {
        if (dynamicReportService.isInPrintMode(params)) {
            JasperReportBuilderEntry criteriaJasperReportBuilderEntry = new JasperReportBuilderEntry()
            criteriaJasperReportBuilderEntry.jasperReportBuilder = buildCriteriaReport(params.criteriaSheetList, params)
            criteriaJasperReportBuilderEntry.excelSheetName = "Criteria"
            if(!criteriaSheetCheck){
                jasperReportBuilderEntryList.add(criteriaJasperReportBuilderEntry)
            }
            JasperReportBuilder signalSummaryReport = buildReport(quantitativeCaseAlertListingData, qualitativeCaseAlertListingData, adhocCaseAlertListingData, literatureCaseAlertListingData, actionsList,
                    meetingList, signalSummary, signalDetails, params, summaryReportPreference, assessmentDetailsList, medicalConceptDistributionList, isConsolidatedReport,uploadAssessment,addReferenceName,criteriaSheetCheck)

            JasperReportBuilderEntry jasperSignalSummary = new JasperReportBuilderEntry()
            jasperSignalSummary.jasperReportBuilder = signalSummaryReport
            jasperSignalSummary.excelSheetName = "Signal Summary Details"
            jasperReportBuilderEntryList.add(jasperSignalSummary)

            if (params.outputFormat == Constants.SignalReportOutputType.XLSX) {
                summaryReportPreference.required.each { reportSection ->
                    switch (reportSection) {
                        case "Signal Information":
                            JasperReportBuilder subReport = buildSignalSummarySubReport(params, signalDetails.get('signalSummaryData'))
                            JasperReportBuilderEntry jasperSubReportBuilderEntry = new JasperReportBuilderEntry()
                            jasperSubReportBuilderEntry.excelSheetName = "Signal Summary"
                            jasperSubReportBuilderEntry.jasperReportBuilder = subReport
                            jasperReportBuilderEntryList.add(jasperSubReportBuilderEntry)

                            JasperReportBuilder subReportDetails = buildSignalDetailsSubReport(params, signalDetails.get('signalDetails'))
                            if(signalDetails.get('signalDetails')) {
                                JasperReportBuilderEntry subReportBuilderEntry = new JasperReportBuilderEntry()
                                subReportBuilderEntry.excelSheetName = "Signal Details"
                                subReportBuilderEntry.jasperReportBuilder = subReportDetails
                                jasperReportBuilderEntryList.add(subReportBuilderEntry)
                            }
                            break
                        case "WorkFlow Log":
                            JasperReportBuilder subReport = buildWorkFlowLogSubReport(params, signalDetails.get('workflowLog'))
                            if(signalDetails.get('workflowLog')) {
                                JasperReportBuilderEntry jasperSubReportBuilderEntry = new JasperReportBuilderEntry()
                                jasperSubReportBuilderEntry.excelSheetName = "WorkFlow Log"
                                jasperSubReportBuilderEntry.jasperReportBuilder = subReport
                                jasperReportBuilderEntryList.add(jasperSubReportBuilderEntry)
                            }
                            break
                        case "Safety Observations":
                            JasperReportBuilder subReport = buildCaseAndPecInfoSubReport(params, adhocCaseAlertListingData, qualitativeCaseAlertListingData, quantitativeCaseAlertListingData, literatureCaseAlertListingData, isConsolidatedReport)
                           if(adhocCaseAlertListingData || qualitativeCaseAlertListingData || quantitativeCaseAlertListingData || literatureCaseAlertListingData) {
                               JasperReportBuilderEntry jasperSubReportBuilderEntry = new JasperReportBuilderEntry()
                               jasperSubReportBuilderEntry.excelSheetName = "Safety Observations"
                               jasperSubReportBuilderEntry.jasperReportBuilder = subReport
                               jasperReportBuilderEntryList.add(jasperSubReportBuilderEntry)
                           }
                            break
                        case "References":
                            JasperReportBuilder subReport = buildReferencesSubReport(params, signalDetails.get('references'), isConsolidatedReport )
                            if(signalDetails.get('references')) {
                                JasperReportBuilderEntry jasperSubReportBuilderEntry = new JasperReportBuilderEntry()
                                jasperSubReportBuilderEntry.excelSheetName = "References"
                                jasperSubReportBuilderEntry.jasperReportBuilder = subReport
                                jasperReportBuilderEntryList.add(jasperSubReportBuilderEntry)
                            }
                            break
                        case "Actions":
                            JasperReportBuilder subReport = buildActionsSubReport(params, actionsList, isConsolidatedReport)
                            if(actionsList) {
                                JasperReportBuilderEntry jasperSubReportBuilderEntry = new JasperReportBuilderEntry()
                                jasperSubReportBuilderEntry.excelSheetName = "Actions"
                                jasperSubReportBuilderEntry.jasperReportBuilder = subReport
                                jasperReportBuilderEntryList.add(jasperSubReportBuilderEntry)
                            }
                            break
                        case "Meetings":
                            JasperReportBuilder subReport = buildMeetingsSubReport(params, meetingList, isConsolidatedReport)
                            if(meetingList) {
                                JasperReportBuilderEntry jasperSubReportBuilderEntry = new JasperReportBuilderEntry()
                                jasperSubReportBuilderEntry.excelSheetName = "Meetings"
                                jasperSubReportBuilderEntry.jasperReportBuilder = subReport
                                jasperReportBuilderEntryList.add(jasperSubReportBuilderEntry)
                            }
                            break
                        case "RMMs":
                            JasperReportBuilder subReport = buildRMMsSubReport(params, signalDetails, isConsolidatedReport)
                            if(signalDetails.get("rmmType")) {
                                JasperReportBuilderEntry jasperSubReportBuilderEntry = new JasperReportBuilderEntry()
                                jasperSubReportBuilderEntry.excelSheetName = "RMMs"
                                jasperSubReportBuilderEntry.jasperReportBuilder = subReport
                                jasperReportBuilderEntryList.add(jasperSubReportBuilderEntry)
                            }
                            break;
                        case "Communication":
                            JasperReportBuilder subReport = buildCommunicationSubReport(params, signalDetails, isConsolidatedReport)
                            if(signalDetails.get("communication")) {
                                JasperReportBuilderEntry jasperSubReportBuilderEntry = new JasperReportBuilderEntry()
                                jasperSubReportBuilderEntry.excelSheetName = "Communication"
                                jasperSubReportBuilderEntry.jasperReportBuilder = subReport
                                jasperReportBuilderEntryList.add(jasperSubReportBuilderEntry)
                            }
                            break;
                        case "Appendix":
                            JasperReportBuilder subReport
                            if (isConsolidatedReport) {
                                subReport = buildConsolidatedAppendixSubReport(params, assessmentDetailsList, medicalConceptDistributionList, isConsolidatedReport)
                            } else {
                                subReport = buildAppendixSubReport(params, assessmentDetailsList, medicalConceptDistributionList, isConsolidatedReport)
                            }
                            JasperReportBuilderEntry jasperSubReportBuilderEntry = new JasperReportBuilderEntry()
                            jasperSubReportBuilderEntry.excelSheetName = "Appendix"
                            jasperSubReportBuilderEntry.jasperReportBuilder = subReport
                            jasperReportBuilderEntryList.add(jasperSubReportBuilderEntry)
                            break
                    }
                }
            }
        }
    }

    private JasperReportBuilder buildReport(List quantitativeCaseAlertListingData, List qualitativeCaseAlertListingData,
                                            List adhocCaseAlertListingData, List literatureCaseAlertListingData, List actionsList, List meetingList,
                                            signalSummary, signalDetails, Map params,
                                            def summaryReportPreference, List assessmentDetailsList, List medicalConceptDistributionList, Boolean isConsolidatedReport,Boolean uploadAssessment=false, String addReferenceName=null,Boolean criteriaSheetCheck = false) {

        User user = userService.getUser()
        String timeZone = user?.preference?.timeZone ?: userService.getCurrentUserPreference().timeZone

        VerticalListBuilder verticalList = cmp.verticalList()

        if(params.outputFormat == Constants.SignalReportOutputType.XLSX){
            params.showCompanyLogo = false
            params.showLogo = false
        }
        params.portraitType = true
        JasperReportBuilder signalDataReport = ReportBuilder.initializeNewReport(params.portraitType, false, false, (params.outputFormat == ReportFormat.XLSX.name()), PageType.LEDGER, PageOrientation.LANDSCAPE)
        //Disable Company logo
        // Call this after adding first sub report
        def disableCompanyLogo = {
            params.showCompanyLogo = false
            params.showLogo = false

            HorizontalListBuilder signalSummaryText = cmp.horizontalList()

            verticalList.add(cmp.verticalGap(5))
            verticalList.add(cmp.verticalList(signalSummaryText))
            verticalList.add(cmp.verticalGap(5))

            params.showCompanyLogo = false
            params.showLogo = false
        }

        //Signal Summary
        def signalSummaryData = signalDetails.get('signalSummaryData')
        def overallSummarySubReport = {


            HorizontalListBuilder signalSummaryList = cmp.horizontalList()

            if (params.showCompanyLogo) {
                ImageBuilder img = cmp.image(imageService.getImage(Constants.DynamicReports.COMPANY_LOGO))
                        .setFixedDimension(80, 36).setStyle(stl.style().setHorizontalAlignment(HorizontalAlignment.LEFT).setTopPadding(5))
                signalSummaryList.add(cmp.verticalList(img)).newRow()

            }
            def filler = cmp.filler().setStyle(stl.style().setTopBorder(stl.penDouble())).setFixedHeight(1)

            String productName = params.productGroupSelection && params.productGroupSelection !='[]' ? getGroupNameFieldFromJson(params.productGroupSelection):getAllProductNameFieldFromJson(params.productSelection)
            if(uploadAssessment){
                signalSummaryList.add(cmp.text("Criteria Sheet").setStyle(Templates.pageHeader_TitleStyle)).newRow()
            } else {
                signalSummaryList.add(cmp.text(Constants.DynamicReports.SIGNAL_SUMM_RPT).setStyle(Templates.pageHeader_TitleStyle)).newRow()
            }
            signalSummaryList.add(cmp.verticalList(filler))
            if (!isConsolidatedReport) {
                if(uploadAssessment){
                    addSignalSummaryHeaderTable(signalSummaryList, 'Signal Name', signalSummaryData.signalName)
                    String eventName = params.eventGroupSelection && params.eventGroupSelection !='[]' ? getGroupNameFieldFromJson(params.eventGroupSelection):getNameFieldFromJson(params.eventSelection)
                    addSignalSummaryHeaderTable(signalSummaryList,  'Product', productName)
                    addSignalSummaryHeaderTable(signalSummaryList, 'Event', eventName)
                    if(params.dateRange != 'SIGNAL_DATA'){
                        addSignalSummaryHeaderTable(signalSummaryList, 'Date Range', DateUtil.simpleDateReformat(params.startDateCharts,"dd/MM/yyyy","dd-MMM-yyyy") + ' - ' + DateUtil.simpleDateReformat(params.endDateCharts,"dd/MM/yyyy","dd-MMM-yyyy"))
                    }
                    addSignalSummaryHeaderTable(signalSummaryList, 'Date Detected', signalSummaryData.detectedDate)
                    addSignalSummaryHeaderTable(signalSummaryList, 'Signal Source', signalSummaryData.initialDataSource)
                    addSignalSummaryHeaderTable(signalSummaryList, 'Priority', signalSummaryData.priority)
                    addSignalSummaryHeaderTable(signalSummaryList, 'Current Disposition', signalSummaryData.disposition)
                    if(criteriaSheetCheck){
                        addSignalSummaryHeaderTable(signalSummaryList, 'Report Generated By', user?.fullName ?: "")
                        addSignalSummaryHeaderTable(signalSummaryList, 'Exported Date', DateUtil.stringFromDate(new java.util.Date(), DateUtil.DATEPICKER_FORMAT_AM_PM, timeZone) + userService.getGmtOffset(timeZone))
                        addSignalSummaryHeaderTable(signalSummaryList, 'Filter', 'N/A')
                    }
                }else{
                    addSignalSummaryHeaderTable(signalSummaryList, 'Products', productName)
                    addSignalSummaryHeaderTable(signalSummaryList, 'Generated on', (DateUtil.toDateStringWithTimeInAmPmFormat(userService.getUser()) + userService.getGmtOffset(userService.getUser()?.preference?.timeZone)))
                }
              }else{
                addSignalSummaryHeaderTable(signalSummaryList, 'Products', params.products)
                addSignalSummaryHeaderTable(signalSummaryList, 'Generated on', (DateUtil.toDateStringWithTimeInAmPmFormat(userService.getUser()) + userService.getGmtOffset(userService.getUser()?.preference?.timeZone)))
                addSignalSummaryHeaderTable(signalSummaryList, 'Date Range', params.reportingInterval)
            }

            verticalList.add(cmp.verticalGap(10))
            verticalList.add(cmp.verticalList(signalSummaryList))
            verticalList.add(cmp.verticalGap(10))
        }

        //Logic to maintain the sections and their order.
        overallSummarySubReport()

        if (params.outputFormat != Constants.SignalReportOutputType.XLSX) {
            summaryReportPreference.required.each { reportSection ->
                switch (reportSection) {
                    case "Signal Information":
                        signalSummaryReport(params, signalSummaryData, verticalList)
                        signalDetailsReport(params, signalDetails.get('signalDetails'), verticalList)
                        break
                    case "WorkFlow Log":
                        signalDetailsWorkLog(params, signalDetails.get('workflowLog'), verticalList)
                        break
                    case "Safety Observations":
                        caseAndPecInfoSubReport(params, adhocCaseAlertListingData, qualitativeCaseAlertListingData, quantitativeCaseAlertListingData, literatureCaseAlertListingData, isConsolidatedReport, verticalList)
                        break
                    case "References":
                        referencesSubReport(params, signalDetails.get('references'), isConsolidatedReport,verticalList)
                        break
                    case "Actions":
                        actionsSubReport(params, actionsList, isConsolidatedReport, verticalList)
                        break
                    case "Meetings":
                        meetingsSubReport(params, meetingList, isConsolidatedReport, verticalList)
                        break
                    case "RMMs":
                        rMMsReport(params, signalDetails, isConsolidatedReport, verticalList)
                        break;
                    case "Communication":
                        communicationSubReport(params, signalDetails, isConsolidatedReport, verticalList)
                        break;
                    case "Appendix":
                        if (isConsolidatedReport) {
                            consolidatedAppendixSubReport(params, assessmentDetailsList, medicalConceptDistributionList, isConsolidatedReport, verticalList)
                        } else {
                            appendixSubReport(params, assessmentDetailsList, medicalConceptDistributionList, isConsolidatedReport, verticalList,addReferenceName)
                        }
                        break
                }
            }
        }
        //<End>
        JasperReportBuilder report = signalDataReport.summary(verticalList)
        report.setPageFormat(PageType.LEDGER, PageOrientation.LANDSCAPE)
        report
    }

    def buildSignalSummarySubReport(params, signalSummaryData) {
        VerticalListBuilder verticalList = cmp.verticalList()
        signalSummaryReport(params, signalSummaryData, verticalList)
        JasperReportBuilder report = new SignalJasperReportBuilder().title(verticalList).setPageMargin(new MarginBuilder().setBottom(0).setTop(0).setLeft(12).setRight(16))
        if (params.outputFormat == Constants.SignalReportOutputType.XLSX) {
            report.ignorePagination()
        }
        report
    }

    def buildSignalDetailsSubReport(params, signalDetails) {
        VerticalListBuilder verticalList = cmp.verticalList()
        signalDetailsReport(params, signalDetails, verticalList)
        JasperReportBuilder report = new SignalJasperReportBuilder().title(verticalList).setPageMargin(new MarginBuilder().setBottom(0).setTop(0).setLeft(12).setRight(16))
        if (params.outputFormat == Constants.SignalReportOutputType.XLSX) {
            report.ignorePagination()
        }
        report
    }

    def buildWorkFlowLogSubReport(params, workflowLog) {
        VerticalListBuilder verticalList = cmp.verticalList()
        signalDetailsWorkLog(params, workflowLog, verticalList)
        JasperReportBuilder report = new SignalJasperReportBuilder().title(verticalList).setPageMargin(new MarginBuilder().setBottom(0).setTop(0).setLeft(12).setRight(16))
        if (params.outputFormat == Constants.SignalReportOutputType.XLSX) {
            report.ignorePagination()
        }
        report
    }

    def buildReferencesSubReport(params, references, isConsolidatedReport) {
        VerticalListBuilder verticalList = cmp.verticalList()
        referencesSubReport(params, references, isConsolidatedReport, verticalList)
        JasperReportBuilder report = new SignalJasperReportBuilder().title(verticalList).setPageMargin(new MarginBuilder().setBottom(0).setTop(0).setLeft(12).setRight(16))
        if (params.outputFormat == Constants.SignalReportOutputType.XLSX) {
            report.ignorePagination()
        }
        report
    }

    def buildCaseAndPecInfoSubReport(params, adhocCaseAlertListingData, qualitativeCaseAlertListingData, quantitativeCaseAlertListingData, literatureCaseAlertListingData, isConsolidatedReport) {
        VerticalListBuilder verticalList = cmp.verticalList()
        caseAndPecInfoSubReport(params, adhocCaseAlertListingData, qualitativeCaseAlertListingData, quantitativeCaseAlertListingData, literatureCaseAlertListingData, isConsolidatedReport, verticalList)
        JasperReportBuilder report = new SignalJasperReportBuilder().title(verticalList).setPageMargin(new MarginBuilder().setBottom(0).setTop(0).setLeft(12).setRight(64))
        if (params.outputFormat == Constants.SignalReportOutputType.XLSX) {
            report.ignorePagination()
        }
        report
    }

    def buildActionsSubReport(params, actionsList, isConsolidatedReport) {
        VerticalListBuilder verticalList = cmp.verticalList()
        actionsSubReport(params, actionsList, isConsolidatedReport, verticalList)
        JasperReportBuilder report = new SignalJasperReportBuilder().title(verticalList).setPageMargin(new MarginBuilder().setBottom(0).setTop(0).setLeft(12).setRight(16))
        if (params.outputFormat == Constants.SignalReportOutputType.XLSX) {
            report.ignorePagination()
        }
        report
    }

    def buildMeetingsSubReport(params, meetingList, isConsolidatedReport) {
        VerticalListBuilder verticalList = cmp.verticalList()
        meetingsSubReport(params, meetingList, isConsolidatedReport, verticalList)
        JasperReportBuilder report = new SignalJasperReportBuilder().title(verticalList).setPageMargin(new MarginBuilder().setBottom(0).setTop(0).setLeft(12).setRight(16))
        if (params.outputFormat == Constants.SignalReportOutputType.XLSX) {
            report.ignorePagination()
        }
        report
    }

    def buildRMMsSubReport(params, rMMsData, isConsolidatedReport) {
        VerticalListBuilder verticalList = cmp.verticalList()
        rMMsReport(params, rMMsData, isConsolidatedReport,verticalList)
        JasperReportBuilder report = new SignalJasperReportBuilder().title(verticalList).setPageMargin(new MarginBuilder().setBottom(0).setTop(0).setLeft(12).setRight(16))
        if (params.outputFormat == Constants.SignalReportOutputType.XLSX) {
            report.ignorePagination()
        }
        report
    }
    def buildCommunicationSubReport(params,communicationData, isConsolidatedReport) {
        VerticalListBuilder verticalList = cmp.verticalList()
        communicationSubReport(params, communicationData, isConsolidatedReport,verticalList)
        JasperReportBuilder report = new SignalJasperReportBuilder().title(verticalList).setPageMargin(new MarginBuilder().setBottom(0).setTop(0).setLeft(12).setRight(16))
        if (params.outputFormat == Constants.SignalReportOutputType.XLSX) {
            report.ignorePagination()
        }
        report
    }

    def buildConsolidatedAppendixSubReport(params, assessmentDetailsList, medicalConceptDistributionList, isConsolidatedReport) {
        VerticalListBuilder verticalList = cmp.verticalList()
        consolidatedAppendixSubReport(params, assessmentDetailsList, medicalConceptDistributionList, isConsolidatedReport, verticalList)
        JasperReportBuilder report = new SignalJasperReportBuilder().title(verticalList).setPageMargin(new MarginBuilder().setBottom(0).setTop(0).setLeft(12).setRight(16))
        if (params.outputFormat == Constants.SignalReportOutputType.XLSX) {
            report.ignorePagination()
        }
        report
    }

    def buildAppendixSubReport(params, assessmentDetailsList, medicalConceptDistributionList, isConsolidatedReport) {
        VerticalListBuilder verticalList = cmp.verticalList()
        appendixSubReport(params, assessmentDetailsList, medicalConceptDistributionList, isConsolidatedReport, verticalList)
        JasperReportBuilder report = new SignalJasperReportBuilder().title(verticalList).setPageMargin(new MarginBuilder().setBottom(0).setTop(0).setLeft(12).setRight(16))
        if (params.outputFormat == Constants.SignalReportOutputType.XLSX) {
            report.ignorePagination()
        }
        report
    }

//Case and PEC Information (AD-HOC Review)
    def adhocReviewSubReport(params, adhocCaseAlertListingData, isConsolidatedReport, verticalList,boolean reportCheck) {
        if (adhocCaseAlertListingData) {
            JasperReportBuilder adhocCaseAlertListingReport = ReportBuilder.initializeNewReport(true, true, false, (params.outputFormat == ReportFormat.XLSX.name()), PageType.LEDGER, PageOrientation.LANDSCAPE)

            adhocCaseAlertListingReport.setDataSource(new JRMapCollectionDataSource(adhocCaseAlertListingData))
            addColumnsForAdhocCaseAlertListing(adhocCaseAlertListingReport, isConsolidatedReport)
            setValidatedObservationHeader(reportCheck, adhocCaseAlertListingReport, params, verticalList)
            setPrintablePageHeader(adhocCaseAlertListingReport, 'Adhoc Review Observations', false, params, "Signal Summary Listing")

            if (params.outputFormat != Constants.SignalReportOutputType.XLSX) {
                verticalList.add(cmp.pageBreak())
            }
            adhocCaseAlertListingReport.setColumnTitleStyle(Templates.columnStyleBold)
            verticalList.add(cmp.subreport(adhocCaseAlertListingReport))
            verticalList.add(cmp.verticalGap(20))
        }
    }
//Case and PEC Information (Quantitative Review)
    def quantitativeReviewSubReport(params, quantitativeCaseAlertListingData, isConsolidatedReport, verticalList, boolean reportCheck) {
        if (quantitativeCaseAlertListingData) {
            JasperReportBuilder quantitativeCaseAlertListingReport = ReportBuilder.initializeNewReport(true, true, false, (params.outputFormat == ReportFormat.XLSX.name()), PageType.LEDGER, PageOrientation.LANDSCAPE)

            quantitativeCaseAlertListingReport.setDataSource(new JRMapCollectionDataSource(quantitativeCaseAlertListingData))
            addColumnsForQuantitativeCaseAlertListing(quantitativeCaseAlertListingReport, isConsolidatedReport)
            setValidatedObservationHeader(reportCheck, quantitativeCaseAlertListingReport, params, verticalList)
            setPrintablePageHeader(quantitativeCaseAlertListingReport, 'Aggregate Review Observations', false, params, "Signal Summary Listing")

            if (params.outputFormat != Constants.SignalReportOutputType.XLSX) {
                verticalList.add(cmp.pageBreak())
            }
            quantitativeCaseAlertListingReport.setColumnTitleStyle(Templates.columnStyleBold)
            verticalList.add(cmp.subreport(quantitativeCaseAlertListingReport))
            verticalList.add(cmp.verticalGap(20))
        }
    }
//Case and PEC Information (Qualitative Review)
    def qualitativeReviewSubReport(params, qualitativeCaseAlertListingData, isConsolidatedReport, verticalList, boolean reportCheck) {
        if (qualitativeCaseAlertListingData) {
            JasperReportBuilder qualitativeCaseAlertListingReport = ReportBuilder.initializeNewReport(true, true, false, (params.outputFormat == ReportFormat.XLSX.name()), PageType.LEDGER, PageOrientation.LANDSCAPE)

            qualitativeCaseAlertListingReport.setDataSource(new JRMapCollectionDataSource(qualitativeCaseAlertListingData))
            addColumnsForQualitativeCaseAlertListing(qualitativeCaseAlertListingReport, isConsolidatedReport)
            setValidatedObservationHeader(reportCheck, qualitativeCaseAlertListingReport, params, verticalList)
            setPrintablePageHeader(qualitativeCaseAlertListingReport, 'Individual Case Review Observations', false, params, "Signal Summary Listing")
            if (params.outputFormat != Constants.SignalReportOutputType.XLSX) {
                verticalList.add(cmp.pageBreak())
            }
            qualitativeCaseAlertListingReport.setColumnTitleStyle(Templates.columnStyleBold)
            verticalList.add(cmp.subreport(qualitativeCaseAlertListingReport))
            verticalList.add(cmp.verticalGap(20))
        }
    }

    //Case and PEC Information (Literature Review)
    def literatureReviewSubReport(params, literatureCaseAlertListingData, isConsolidatedReport, verticalList, boolean reportCheck) {
        if (literatureCaseAlertListingData) {
            JasperReportBuilder literatureCaseAlertListingReport = null
            if(params.outputFormat == Constants.SignalReportOutputType.XLSX) {
                literatureCaseAlertListingReport = new SignalJasperReportBuilder()
                setPrintablePageHeaderWithoutLogo(literatureCaseAlertListingReport, 'Literature Review Observations', false, params, null)
            }else {
                literatureCaseAlertListingReport = ReportBuilder.initializeNewReport(true, false, false, (params.outputFormat == ReportFormat.XLSX.name()), PageType.LEDGER, PageOrientation.LANDSCAPE)
                setValidatedObservationHeader(reportCheck, literatureCaseAlertListingReport, params, verticalList)
                setPrintablePageHeader(literatureCaseAlertListingReport, 'Literature Review Observations', false, params, null)
            }

            literatureCaseAlertListingReport.setDataSource(new JRMapCollectionDataSource(literatureCaseAlertListingData))
            addColumnsForLiteratureCaseAlertListing(literatureCaseAlertListingReport, isConsolidatedReport)

            if (params.outputFormat != Constants.SignalReportOutputType.XLSX) {
                verticalList.add(cmp.pageBreak())
            }
            literatureCaseAlertListingReport.setColumnTitleStyle(Templates.columnStyleBold)
            verticalList.add(cmp.subreport(literatureCaseAlertListingReport))
            verticalList.add(cmp.verticalGap(20))
        }
    }

//Case and PEC Information - Complete
    def caseAndPecInfoSubReport(params, adhocCaseAlertListingData, qualitativeCaseAlertListingData, quantitativeCaseAlertListingData, literatureCaseAlertListingData, isConsolidatedReport, verticalList) {
        params.showCompanyLogo = false
        params.showLogo = false
        boolean reportCheck = false

        quantitativeReviewSubReport(params, quantitativeCaseAlertListingData, isConsolidatedReport, verticalList, reportCheck)
        qualitativeReviewSubReport(params, qualitativeCaseAlertListingData, isConsolidatedReport, verticalList, quantitativeCaseAlertListingData ? true : false)
        literatureReviewSubReport(params, literatureCaseAlertListingData, isConsolidatedReport, verticalList,
                (quantitativeCaseAlertListingData || qualitativeCaseAlertListingData) ? true : false)
        adhocReviewSubReport(params, adhocCaseAlertListingData, isConsolidatedReport, verticalList,
                (quantitativeCaseAlertListingData || qualitativeCaseAlertListingData || literatureCaseAlertListingData) ? true : false)

    }

    private void setValidatedObservationHeader(boolean reportCheck , report , params, verticalList){
        if(!reportCheck) {
            setPrintablePageHeaderWithoutLogo(report, "Safety Observations", false, params, null)
            verticalList.add(cmp.verticalGap(20))
        }
    }

    //signalSummary
    def signalSummaryReport(params, signalSummaryData, verticalList) {
        if (signalSummaryData) {
            JasperReportBuilder signalSummaryReport = new SignalJasperReportBuilder()

            setPrintablePageHeaderWithoutLogo(signalSummaryReport, "Signal Information", false, params, null)
            setPrintablePageHeaderWithoutLogo(signalSummaryReport, "Signal Summary", false, params, null)

            signalSummaryReport.setDataSource(new JRMapCollectionDataSource(signalSummaryData))


            signalSummaryReport.addColumn(Columns.column("Signal ID", 'signalId', type.stringType() ))
                    .addColumn(Columns.column("Signal Name", 'signalName', type.stringType() ))
                    .addColumn(Columns.column("Product", 'product', type.stringType() ))
                    .addColumn(Columns.column("Event", 'event', type.stringType() ))
                    .addColumn(Columns.column("Detected Date", 'detectedDate', type.stringType() ))
                    .addColumn(Columns.column("Status", 'status', type.stringType() ))
                    .addColumn(Columns.column("Date Closed", 'closedDate', type.stringType() ))
                    .addColumn(Columns.column("Signal Source", 'signalSource', type.stringType() ))
                    .addColumn(Columns.column("Signal Outcome", 'signalOutcome', type.stringType() ))
                    .addColumn(Columns.column("Action Taken", 'actionTaken', type.stringType() ))


            Holders.config.signal.summary.dynamic.fields.sort {it.sequence}.each {
                if (it.fieldName == "UD_Text1" && it.enabled == true) {
                    signalSummaryReport.addColumn(Columns.column(it.label, "udText1", type.stringType()))
                }
                if (it.fieldName == "UD_Text2" && it.enabled == true) {
                    signalSummaryReport.addColumn(Columns.column(it.label, "udText2", type.stringType()))
                }
                if (it.fieldName == "UD_Date1" && it.enabled == true) {
                    signalSummaryReport.addColumn(Columns.column(it.label, "udDate1", type.stringType()))
                }
                if (it.fieldName == "UD_Date2" && it.enabled == true) {
                    signalSummaryReport.addColumn(Columns.column(it.label, "udDate2", type.stringType()))
                }
                if (it.fieldName == "UD_Dropdown1" && it.enabled == true) {
                    signalSummaryReport.addColumn(Columns.column(it.label, "udDropdown1", type.stringType()))
                }
                if (it.fieldName == "UD_Dropdown2" && it.enabled == true) {
                    signalSummaryReport.addColumn(Columns.column(it.label, "udDropdown2", type.stringType()))
                }
            }

            signalSummaryReport.setColumnTitleStyle(Templates.columnStyleBold)
            verticalList.add(cmp.subreport(signalSummaryReport))
            verticalList.add(cmp.verticalGap(10))
            verticalList.add(cmp.verticalGap(20))
        }
    }

    //signalDetails
    def signalDetailsReport(params, signalDetails, verticalList) {
        if (signalDetails) {
            JasperReportBuilder signalDetailsReport = new SignalJasperReportBuilder()

            setPrintablePageHeaderWithoutLogo(signalDetailsReport, "Signal Details", false, params, null)

            signalDetailsReport.setDataSource(new JRMapCollectionDataSource(signalDetails))
            signalDetailsReport.addColumn(Columns.column("Signal Name", 'signalName', type.stringType() ))
                    .addColumn(Columns.column("Linked Signal", 'linkedSignal', type.stringType() ))
                    .addColumn(Columns.column("Evaluation Method", 'evaluationMethod', type.stringType() ))
                    .addColumn(Columns.column("Risk/Topic Category", 'risk/Topic', type.stringType() ))
                    .addColumn(Columns.column("Reason for Evaluation & Summary of Key Data", 'reasonForEvaluation', type.stringType() ))
                    .addColumn(Columns.column("Additional Information/Comments", 'comments', type.stringType() ))

            signalDetailsReport.setColumnTitleStyle(Templates.columnStyleBold)
            verticalList.add(cmp.subreport(signalDetailsReport))
            verticalList.add(cmp.verticalGap(10))
            verticalList.add(cmp.verticalGap(20))
        }
    }

    def signalDetailsWorkLog(params, signalDetailsWorkLog, verticalList) {
        ValidatedSignal  validatedSignal=ValidatedSignal.get(params.signalId)
        if (signalDetailsWorkLog) {
            JasperReportBuilder signalDetailsReport = new SignalJasperReportBuilder()

            setPrintablePageHeaderWithoutLogo(signalDetailsReport, "Workflow Log", false, params, null)

            signalDetailsReport.setDataSource(new JRMapCollectionDataSource(signalDetailsWorkLog))

            signalDetailsReport.addColumn(Columns.column("Signal Name", 'signalName', type.stringType()))
                    .addColumn(Columns.column("Priority", 'priority', type.stringType()))
                    .addColumn(Columns.column("Disposition", 'disposition', type.stringType()))
                    .addColumn(Columns.column("Assigned To", 'assignedTo', type.stringType()))
            alertAttributesService.get("signalHistoryStatus").each { status ->
                String dateField = "${status.trim().replace(' ', '').toLowerCase()}Date"
                if (status != Constants.DATE_CLOSED) {
                    signalDetailsReport.addColumn(Columns.column("${status}", dateField, type.stringType()))
                }
            }
            if (SystemConfig.first().displayDueIn) {
                signalDetailsReport.addColumn(Columns.column(Constants.WorkFlowLog.DUE_DATE, "duedateDate", type.stringType()))
            }
            signalDetailsReport.setColumnTitleStyle(Templates.columnStyleBold)
            verticalList.add(cmp.subreport(signalDetailsReport))
            verticalList.add(cmp.verticalGap(10))
            verticalList.add(cmp.verticalGap(20))
        }
    }

    def referencesSubReport(params, references, isConsolidatedReport, verticalList){
        if (references) {
            JasperReportBuilder referencesReport = ReportBuilder.initializeNewReport(true, true, false, (params.outputFormat == ReportFormat.XLSX.name()), PageType.LEDGER, PageOrientation.LANDSCAPE)
            setPageMargine(referencesReport)

            setPrintablePageHeaderWithoutLogo(referencesReport, "References", false, params, null)
            def filler = cmp.filler().setStyle(stl.style().setTopBorder(stl.penDouble())).setFixedHeight(0)
            referencesReport.pageHeader(cmp.verticalList().add(filler).add(cmp.verticalGap(5)))
            referencesReport.setDataSource(new JRMapCollectionDataSource(references))
            if (isConsolidatedReport) {
                referencesReport.addColumn(Columns.column("Signal Name", "signalName", type.stringType()))
            }
            referencesReport.addColumn(Columns.column("Reference Type", 'referenceType', type.stringType() ))
                    .addColumn(Columns.column("Description", 'description', type.stringType() ))
                    .addColumn(Columns.column("File Name", 'inputName', type.stringType() ))
                    .addColumn(Columns.column("Added By", 'modifiedBy', type.stringType() ))
                    .addColumn(Columns.column("Date", 'timeStamp', type.stringType() ))

            if (params.outputFormat != Constants.SignalReportOutputType.XLSX) {
                verticalList.add(cmp.pageBreak())
            }
            referencesReport.setColumnTitleStyle(Templates.columnStyleBold)
            verticalList.add(cmp.subreport(referencesReport))
            verticalList.add(cmp.verticalGap(20))
        }
    }

    //signalSummary
    def rMMsReport(params, signalData, isConsolidatedReport, verticalList) {
        if (signalData.get("rmmType")) {
            JasperReportBuilder signalSummaryReport = new SignalJasperReportBuilder()
            setPageMargine(signalSummaryReport)

            setPrintablePageHeaderWithoutLogo(signalSummaryReport, "RMMs & Communication", false, params, null)
            setPrintablePageHeaderWithoutLogo(signalSummaryReport, "RMMs", false, params, null)
            signalSummaryReport.setDataSource(new JRMapCollectionDataSource(signalData.get("rmmType")))
            if (isConsolidatedReport) {
                signalSummaryReport.addColumn(Columns.column("Signal Name", "signalName", type.stringType()))
            }
            signalSummaryReport.addColumn(Columns.column("Type", 'type', type.stringType()))
                    .addColumn(Columns.column("Country", 'country', type.stringType()))
                    .addColumn(Columns.column("Description", 'description', type.stringType()))
                    .addColumn(Columns.column("Status", 'status', type.stringType()))
                    .addColumn(Columns.column("File Name", 'fileName', type.stringType()))
                    .addColumn(Columns.column("Assigned To", 'assignedToFullName', type.stringType()))
                    .addColumn(Columns.column("Due Date", 'dueDate', type.stringType()))

            signalSummaryReport.setColumnTitleStyle(Templates.columnStyleBold)
            verticalList.add(cmp.subreport(signalSummaryReport))
            verticalList.add(cmp.verticalGap(20))
        }
    }

    def communicationSubReport(params, signalData, isConsolidatedReport, verticalList){
        if(signalData.get("communication")){
            JasperReportBuilder signalCommunicationReport = new SignalJasperReportBuilder()
            setPageMargine(signalCommunicationReport)

            if (!signalData.get("rmmType"))
                setPrintablePageHeaderWithoutLogo(signalCommunicationReport, "RMMs & Communication", false, params, null)
            setPrintablePageHeaderWithoutLogo(signalCommunicationReport, "Communication", false, params, null)
            signalCommunicationReport.setDataSource(new JRMapCollectionDataSource(signalData.get("communication")))

            if (isConsolidatedReport) {
                signalCommunicationReport.addColumn(Columns.column("Signal Name", "signalName", type.stringType()))
            }
            signalCommunicationReport.addColumn(Columns.column("Type", 'type', type.stringType()))
                    .addColumn(Columns.column("Country", 'country', type.stringType()))
                    .addColumn(Columns.column("Description", 'description', type.stringType()))
                    .addColumn(Columns.column("Status", 'status', type.stringType()))
                    .addColumn(Columns.column("File Name", 'fileName', type.stringType()))
                    .addColumn(Columns.column("Assigned To", 'assignedToFullName', type.stringType()))
                    .addColumn(Columns.column("Due Date", 'dueDate', type.stringType()))
                    .addColumn(Columns.column("Email sent", 'email', type.stringType()))

            signalCommunicationReport.setColumnTitleStyle(Templates.columnStyleBold)
            verticalList.add(cmp.subreport(signalCommunicationReport))
            verticalList.add(cmp.verticalGap(20))
        }
    }

    private void setPageMargine(JasperReportBuilder report){
        MarginBuilder margins = new MarginBuilder()
        margins.setTop(0)
        margins.setRight(50)
        margins.setLeft(50)
        margins.setBottom(10)
        report.setPageMargin(margins)
    }

//Actions Taken
    def actionsSubReport(params, actionsList, isConsolidatedReport, verticalList) {
        if (actionsList) {
            JasperReportBuilder actionsListReportBuilder = ReportBuilder.initializeNewReport(true, true, false, (params.outputFormat == ReportFormat.XLSX.name()), PageType.LEDGER, PageOrientation.LANDSCAPE)

            actionsListReportBuilder.setDataSource(new JRMapCollectionDataSource(actionsList))
            addColumnsForActionTakenListing(actionsListReportBuilder, isConsolidatedReport)

            setPrintablePageHeader(actionsListReportBuilder, 'Actions', false, params, "Signal Summary Listing")

            if (params.outputFormat != Constants.SignalReportOutputType.XLSX) {
                verticalList.add(cmp.pageBreak())
            }
            actionsListReportBuilder.setColumnTitleStyle(Templates.columnStyleBold)
            verticalList.add(cmp.subreport(actionsListReportBuilder))
            verticalList.add(cmp.verticalGap(20))
        }
    }

//Meetings
    def meetingsSubReport(params, meetingList, isConsolidatedReport, verticalList) {
        if (meetingList) {
            JasperReportBuilder meetingListReportBuilder = ReportBuilder.initializeNewReport(true, true, false, (params.outputFormat == ReportFormat.XLSX.name()), PageType.LEDGER, PageOrientation.LANDSCAPE)

            meetingListReportBuilder.setDataSource(new JRMapCollectionDataSource(meetingList))
            addColumnsForMeetingListing(meetingListReportBuilder, isConsolidatedReport)

            setPrintablePageHeader(meetingListReportBuilder, 'Meetings', false, params, "Signal Summary Listing")

            if (params.outputFormat != Constants.SignalReportOutputType.XLSX) {
                verticalList.add(cmp.pageBreak())
            }
            meetingListReportBuilder.setColumnTitleStyle(Templates.columnStyleBold)
            verticalList.add(cmp.subreport(meetingListReportBuilder))
            verticalList.add(cmp.verticalGap(20))
        }
    }

//Appendix
    def assessmentDetails(params, assessmentDetailsList, isConsolidatedReport, verticalList,String addReferenceName = null) {
        if (assessmentDetailsList) {
            JasperReportBuilder assessmentDetailReportBuilder = ReportBuilder.initializeNewReport(true, true, false, (params.outputFormat == ReportFormat.XLSX.name()), PageType.LEDGER, PageOrientation.LANDSCAPE)

            assessmentDetailReportBuilder.setDataSource(new JRMapCollectionDataSource(assessmentDetailsList))
            addColumnsForAssessmentDetailsListing(assessmentDetailReportBuilder, isConsolidatedReport)

            if(addReferenceName && addReferenceName == 'AssessmentDetails'){
                setPrintablePageHeader(assessmentDetailReportBuilder, 'Assessment Performed', true, params, "Signal Summary Listing")
            } else {
                setPrintablePageHeader(assessmentDetailReportBuilder, 'Appendix \n \nAssessment Performed', true, params, "Signal Summary Listing")
            }

            if (params.outputFormat != Constants.SignalReportOutputType.XLSX) {
                verticalList.add(cmp.pageBreak())
            }
            assessmentDetailReportBuilder.setColumnTitleStyle(Templates.columnStyleBold)
            verticalList.add(cmp.subreport(assessmentDetailReportBuilder))
            verticalList.add(cmp.verticalGap(20))
        }
    }

    def medicalConceptDistribution(params, medicalConceptDistributionList, isConsolidatedReport, verticalList) {
        if (medicalConceptDistributionList) {
            JasperReportBuilder medicalConceptDistributionReportBuilder = ReportBuilder.initializeNewReport(true, false, false, (params.outputFormat == ReportFormat.XLSX.name()), PageType.LEDGER, PageOrientation.LANDSCAPE)

            medicalConceptDistributionReportBuilder.setDataSource(new JRMapCollectionDataSource(medicalConceptDistributionList))
            addColumnsForMedicalConceptsDistributionListing(medicalConceptDistributionReportBuilder, isConsolidatedReport)

            setPrintablePageHeader(medicalConceptDistributionReportBuilder, 'Medical Concept Distribution', false, params, "Signal Summary Listing")

            if (params.outputFormat != Constants.SignalReportOutputType.XLSX) {
                verticalList.add(cmp.pageBreak())
            }
            verticalList.add(cmp.subreport(medicalConceptDistributionReportBuilder))
            verticalList.add(cmp.verticalGap(20))
        }
    }

    void distributionBySeriousnessCountsOverTime(Map params,VerticalListBuilder verticalList,SignalChartsDTO signalChartsDTO){
        SimpleChartBuilder chartBuilder = new SimpleChartBuilder()
        JasperReportBuilder chartReportBuilder = chartBuilder.createSubChart(params, validatedSignalService.fetchExportDataForDistributionBySeriousnessOverTime(signalChartsDTO))

        setPrintablePageHeader(chartReportBuilder, 'Seriousness Criteria Counts Over Time', false, params, "Signal Summary Listing")

        if (params.outputFormat != Constants.SignalReportOutputType.XLSX) {
            verticalList.add(cmp.pageBreak())
        }
        verticalList.add(cmp.subreport(chartReportBuilder))
        verticalList.add(cmp.verticalGap(20))
    }

    void distributionByAgeGroupOverTime(Map params,VerticalListBuilder verticalList,SignalChartsDTO signalChartsDTO){
        SimpleChartBuilder chartBuilder = new SimpleChartBuilder()
        JasperReportBuilder chartReportBuilder = chartBuilder.createSubChart(params, validatedSignalService.fetchExportDataForDistributionByAgeOverTime(signalChartsDTO))

        setPrintablePageHeader(chartReportBuilder, 'Distribution By Age Group Over Time', false, params, "Signal Summary Listing")

        if (params.outputFormat != Constants.SignalReportOutputType.XLSX) {
            verticalList.add(cmp.pageBreak())
        }
        verticalList.add(cmp.subreport(chartReportBuilder))
        verticalList.add(cmp.verticalGap(20))
    }

    void distributionByGenderOverTime(Map params,VerticalListBuilder verticalList,SignalChartsDTO signalChartsDTO){
        SimpleChartBuilder chartBuilder = new SimpleChartBuilder()
        JasperReportBuilder chartReportBuilder = chartBuilder.createSubChart(params, validatedSignalService.fetchExportDataForDistributionByGenderOverTime(signalChartsDTO))

        setPrintablePageHeader(chartReportBuilder, 'Distribution By Gender Over Time', false, params, "Signal Summary Listing")

        if (params.outputFormat != Constants.SignalReportOutputType.XLSX) {
            verticalList.add(cmp.pageBreak())
        }
        verticalList.add(cmp.subreport(chartReportBuilder))
        verticalList.add(cmp.verticalGap(20))
    }

    void distributionByCountryOverTime(Map params,VerticalListBuilder verticalList, SignalChartsDTO signalChartsDTO){
        SimpleChartBuilder chartBuilder = new SimpleChartBuilder()
        JasperReportBuilder chartReportBuilder = chartBuilder.createSubChart(params, validatedSignalService.fetchExportDataForDistributionByCountryOverTime(signalChartsDTO))

        setPrintablePageHeader(chartReportBuilder, 'Distribution By Country Over Time', false, params, "Signal Summary Listing")

        if (params.outputFormat != Constants.SignalReportOutputType.XLSX) {
            verticalList.add(cmp.pageBreak())
        }
        verticalList.add(cmp.subreport(chartReportBuilder))
        verticalList.add(cmp.verticalGap(20))
    }

    void distributionByCaseOutcome(Map params,VerticalListBuilder verticalList, SignalChartsDTO signalChartsDTO){
        SimpleChartBuilder chartBuilder = new SimpleChartBuilder()
        JasperReportBuilder chartReportBuilder = chartBuilder.createSubChart(params, validatedSignalService.fetchExportDataForDistributionByCaseOutcome(signalChartsDTO))

        setPrintablePageHeader(chartReportBuilder, 'Distribution By Case Outcome', false, params, "Signal Summary Listing")

        if (params.outputFormat != Constants.SignalReportOutputType.XLSX) {
            verticalList.add(cmp.pageBreak())
        }
        verticalList.add(cmp.subreport(chartReportBuilder))
        verticalList.add(cmp.verticalGap(20))
    }

    void distributionBySourceOverTime(Map params,VerticalListBuilder verticalList, SignalChartsDTO signalChartsDTO){
        SimpleChartBuilder chartBuilder = new SimpleChartBuilder()
        JasperReportBuilder chartReportBuilder = chartBuilder.createSubChart(params, validatedSignalService.fetchExportDataForDistributionBySourceOverTime(signalChartsDTO))

        setPrintablePageHeader(chartReportBuilder, 'Distribution By Source Over Time', false, params, "Signal Summary Listing")

        if (params.outputFormat != Constants.SignalReportOutputType.XLSX) {
            verticalList.add(cmp.pageBreak())
        }
        verticalList.add(cmp.subreport(chartReportBuilder))
        verticalList.add(cmp.verticalGap(20))
    }

    def appendixSubReport(params, assessmentDetailsList, medicalConceptDistributionList, isConsolidatedReport, verticalList,String addReferenceName=null) {
        ValidatedSignal validatedSignal = ValidatedSignal.get(Long.parseLong(params.signalId))
        def dateRange = params.dateRange as SignalAssessmentDateRangeEnum
        Integer groupingCode = dateRange.groupingCode

        String productSelection = params.productSelection
        String eventSelection = params.eventSelection
        String caseList
        String timeZone = userService.user.preference.timeZone

        switch (dateRange) {
            case SignalAssessmentDateRangeEnum.CUSTOM:
                dateRange = [params.startDate,params.endDate]
                break
            case SignalAssessmentDateRangeEnum.LAST_3_MONTH:
                dateRange = RelativeDateConverter.lastXMonths(null, 3, timeZone).collect {
                    DateUtil.stringFromDate(it, "dd/MM/yyyy", timeZone)
                }
                break
            case SignalAssessmentDateRangeEnum.LAST_6_MONTH:
                dateRange = RelativeDateConverter.lastXMonths(null, 6, timeZone).collect {
                    DateUtil.stringFromDate(it, "dd/MM/yyyy", timeZone)
                }
                break
            case SignalAssessmentDateRangeEnum.LAST_3_YEAR:
                dateRange = RelativeDateConverter.lastXYears(null, 3, timeZone).collect {
                    DateUtil.stringFromDate(it, "dd/MM/yyyy", timeZone)
                }
                break
            case SignalAssessmentDateRangeEnum.LAST_5_YEAR:
                dateRange = RelativeDateConverter.lastXYears(null, 5, timeZone).collect {
                    DateUtil.stringFromDate(it, "dd/MM/yyyy", timeZone)
                }
                break
            case SignalAssessmentDateRangeEnum.LAST_1_YEAR:
                dateRange = RelativeDateConverter.lastXYears(null, 1, timeZone).collect {
                    DateUtil.stringFromDate(it, "dd/MM/yyyy", timeZone)
                }
                break
            case SignalAssessmentDateRangeEnum.SIGNAL_DATA:
                caseList = validatedSignalChartService.mapCaseNumberFormatForProc(validatedSignal.singleCaseAlerts*.caseNumber as List<String>)
                dateRange = validatedSignalService.fetchDateRangeFromCaseAlerts(validatedSignal.singleCaseAlerts as List<SingleCaseAlert>)
                break
            default:
                dateRange = RelativeDateConverter.lastXYears(null, 1, timeZone).collect {
                    DateUtil.stringFromDate(it, "dd/MM/yyyy", timeZone)
                }
        }
        if(addReferenceName==null || addReferenceName == 'AssessmentDetails') {
            assessmentDetails(params, assessmentDetailsList, isConsolidatedReport, verticalList,addReferenceName)
        }
        medicalConceptDistribution(params, medicalConceptDistributionList, isConsolidatedReport, verticalList)
        Boolean isProductOrFamilyOrIngredient = validatedSignalService.allowedDictionarySelectionByString(productSelection)
        if ((productSelection || params.productGroupSelection) && (eventSelection || params.eventGroupSelection) && isProductOrFamilyOrIngredient) {
            SignalChartsDTO signalChartsDTO = new SignalChartsDTO()
            signalChartsDTO.productSelection = productSelection
            signalChartsDTO.productGroupSelection = params.productGroupSelection
            signalChartsDTO.eventSelection = eventSelection
            signalChartsDTO.eventGroupSelection = params.eventGroupSelection
            signalChartsDTO.dateRange = dateRange
            signalChartsDTO.caseList = caseList
            signalChartsDTO.groupingCode = groupingCode
            signalChartsDTO.signalId = validatedSignal.id
            signalChartsDTO.isMultiIngredient = validatedSignal.isMultiIngredient
            if(addReferenceName==null || addReferenceName==Constants.ReferenceName.CHARTS|| addReferenceName==Constants.ReferenceName.ASSESSMENT_REPORTS) {
                distributionBySeriousnessCountsOverTime(params, verticalList, signalChartsDTO)
                distributionByAgeGroupOverTime(params, verticalList, signalChartsDTO)
                distributionByGenderOverTime(params, verticalList, signalChartsDTO)
                distributionByCountryOverTime(params, verticalList, signalChartsDTO)
                distributionByCaseOutcome(params, verticalList, signalChartsDTO)
                distributionBySourceOverTime(params, verticalList, signalChartsDTO)
            } else if(addReferenceName==Constants.ReferenceName.SERIOUSNESS_COUNTS_OVER_TIME){
                distributionBySeriousnessCountsOverTime(params, verticalList, signalChartsDTO)
            } else if(addReferenceName==Constants.ReferenceName.AGE_GROUP_OVER_TIME){
                distributionByAgeGroupOverTime(params, verticalList, signalChartsDTO)
            } else if(addReferenceName==Constants.ReferenceName.GENDER_OVER_TIME){
                distributionByGenderOverTime(params, verticalList, signalChartsDTO)
            } else if(addReferenceName==Constants.ReferenceName.COUNTY_OVER_TIME){
                distributionByCountryOverTime(params, verticalList, signalChartsDTO)
            } else if(addReferenceName==Constants.ReferenceName.CASE_OUTCOME){
                distributionByCaseOutcome(params, verticalList, signalChartsDTO)
            } else if(addReferenceName==Constants.ReferenceName.SOURCE_OVER_TIME){
                distributionBySourceOverTime(params, verticalList, signalChartsDTO)
            }
        }
    }

    def consolidatedAppendixSubReport(params, assessmentDetailsList, medicalConceptDistributionList, isConsolidatedReport, verticalList) {
        assessmentDetails(params, assessmentDetailsList, isConsolidatedReport, verticalList)
        medicalConceptDistribution(params, medicalConceptDistributionList, isConsolidatedReport, verticalList)
    }

    public static List getMeasuresList(ReportResult reportResult) {
        List result = []
        ExecutedDataTabulationTemplate template = reportResult.executedTemplateQuery.executedTemplate
        template.columnMeasureList.each { columnMeasure ->
            columnMeasure.measures.each { measure ->
                result.add(measure.name)
            }
        }
        return result.unique()
    }

    private List parseCrosstabFields(ReportResult reportResult) {
        JSONArray tabHeaders = (JSONArray) JSON.parse(reportResult.data.crossTabHeader)

        List<FieldBuilder> rowFields = []
        List<FieldBuilder> columnFields = []
        List<TextColumnBuilder> rows = []
        List<TextColumnBuilder> columns = []

        for (JSONObject header : tabHeaders) {
            String name = header.entrySet().getAt(0).key
            String label = header.entrySet().getAt(0).value
            // Remove line breaks from column header
            label = label.replaceAll("[\\r\\n]+", "")
            def isCaseListColumn = name.startsWith("CASE_LIST")
            def isTotalCaseCount = name.startsWith("CASE_COUNT")
            def isIntervalCaseCount = name.startsWith("INTERVAL_CASE_COUNT")

            if (name.substring(0, 3).equalsIgnoreCase("ROW")) {
                FieldBuilder field = field(name, type.stringType())
                rowFields.add(field)
                rows.add(Columns.column(label, field))
            } else if (!isCaseListColumn && !isTotalCaseCount && !isIntervalCaseCount) {
                FieldBuilder field = field(name, type.integerType())
                columnFields.add(field)
                columns.add(Columns.column(label, field))
            }
        }
        [rowFields, columnFields, rows, columns]
    }

    private AbstractChartBuilder addChart(
            def options, String chartTitle, List<TextColumnBuilder> rows, List<TextColumnBuilder> columns, JasperReportBuilder report, Map params) {
        def type = options.chart.type
        switch (type) {
            case "pie":
                return addPieChart(options, chartTitle, rows, columns, report, params)
            case "line":
                return addLineChart(options, chartTitle, rows, columns, report, params)
            case "column":
            default:
                return addStackedBarChart(options, chartTitle, rows, columns, report, params)
        }
    }

    private AbstractChartBuilder addPieChart(
            def options, String chartTitle, List<TextColumnBuilder> rows, List<TextColumnBuilder> columns, JasperReportBuilder report, Map params) {
        PieChartBuilder chart = cht.pieChart()
                .setTitle(chartTitle)
                .setLabelFormat("{1}")
                .setKey(new SingleCategoryExpression(rows))
                .addCustomizer(new PieChartLabelFormatCustomizer())
                .addSerie(cht.serie(columns.head()))
        chart
    }

    private AbstractChartBuilder addLineChart(
            def options, String chartTitle, List<TextColumnBuilder> rows, List<TextColumnBuilder> columns, JasperReportBuilder report, Map params) {
        LineChartBuilder chart = cht.lineChart()
                .setTitle(chartTitle)
                .setShowLegend(true)
                .setCategory(new SingleCategoryExpression(rows))
        columns.each {
            chart.addSerie(cht.serie(it))
        }
        chart
    }

    private AbstractChartBuilder addStackedBarChart(
            def options, String chartTitle, List<TextColumnBuilder> rows, List<TextColumnBuilder> columns, JasperReportBuilder report, Map params) {
        StackedBarChartBuilder chart = cht.stackedBarChart()
                .setTitle(chartTitle)
                .setShowLegend(true)
                .setCategory(new SingleCategoryExpression(rows))
        columns.each {
            chart.addSerie(cht.serie(it))
        }
        chart.addCustomizer(new StackedBarChartCustomizer());
        chart
    }

    private class PieChartLabelFormatCustomizer implements DRIChartCustomizer, Serializable {

        @Override
        public void customize(JFreeChart jFreeChart, ReportParameters reportParameters) {
            PiePlot piePlot = (PiePlot) jFreeChart.getPlot();
            piePlot.setLabelOutlinePaint(null);
            piePlot.setLabelShadowPaint(null);
        }
    }

    private class SingleCategoryExpression extends AbstractSimpleExpression<String> {
        private List<TextColumnBuilder> rows;

        public SingleCategoryExpression(List<TextColumnBuilder> rows) {
            this.rows = rows
        }

        @Override
        public String evaluate(ReportParameters reportParameters) {
            String value = reportParameters.getFieldValue(rows.last().name)
            value == null ? "N/A" : value
        }
    }

    private class StackedBarChartCustomizer implements DRIChartCustomizer, Serializable {
        public void customize(JFreeChart chart, ReportParameters reportParameters) {
            CategoryAxis domainAxis = chart.getCategoryPlot().getDomainAxis();
            domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));
        }
    }

    private HighPieChartBuilder addHighPieChart(
            def options, String chartTitle, List<TextColumnBuilder> rows, List<TextColumnBuilder> columns, JasperReportBuilder report, Map params) {
        HighPieChartBuilder chart = new HighPieChartBuilder(options)
                .setTitle(chartTitle)
                .setShowPercentages(true)
                .setKey(new CategoryExpression(rows))
                .setChartRowsCount(rows.size())
        if (columns.size() > 0) {
            chart.addSerie(cht.serie(columns.head()))
        }
        report.scriptlets(chart.scriptlet)
        chart
    }

    private HighStackedBarChartBuilder addHighStackedBarChart(
            def options, String chartTitle, String yAxisTitle, List<TextColumnBuilder> rows,
            List<TextColumnBuilder> columns, JasperReportBuilder report, Map params) {
        HighStackedBarChartBuilder chart = new HighStackedBarChartBuilder(options)
                .setTitle(chartTitle)
                .setYAxisTitle(yAxisTitle)
                .setShowLegend(true)
                .setCategory(new CategoryExpression(rows))
                .setChartRowsCount(rows.size())
        columns.each {
            chart.addSerie(cht.serie(it))
        }
        report.scriptlets(chart.scriptlet)
        chart
    }

    private class CategoryExpression extends AbstractSimpleExpression<List<String>> {
        private List<TextColumnBuilder> rows;

        public CategoryExpression(List<TextColumnBuilder> rows) {
            this.rows = rows
        }

        @Override
        public List<String> evaluate(ReportParameters reportParameters) {
            return rows.collect {
                String value = reportParameters.getFieldValue(it.name)
                value == null ? "N/A" : value
            }
        }
    }

    private class TotalFilterExpression extends AbstractSimpleExpression<Boolean> {
        private static final long serialVersionUID = 1L;
        private List<FieldBuilder> rowFields

        def totalRowIndices = []

        public TotalFilterExpression(List<FieldBuilder> rowFields) {
            this.rowFields = rowFields
        }

        @Override
        public Boolean evaluate(ReportParameters reportParameters) {
            def isTotalRow = rowFields.find {
                Object value = reportParameters.getValue(it.name)
                value instanceof String && ("Total".equals(value) || "Subtotal".equals(value) || "Sub Total".equals(value))
            }
            if (isTotalRow) {
                totalRowIndices.add(reportParameters.columnRowNumber + totalRowIndices.size())
            }
            return !isTotalRow
        }
    }

    private void addSignalSummaryHeaderTable(HorizontalListBuilder list, def labelId, def value) {
        if (value instanceof List) {
            value = value.join(',')
        }
        list.add(
                cmp.text(labelId).setFixedColumns(15).setStyle(Templates.criteriaNameStyle),
                cmp.text(value).setValueFormatter(new CSVFormulaFormatter()).setStyle(Templates.criteriaValueStyle)
        ).newRow()
    }

    private void addSignalSummaryConsolidatedHeaderTable(HorizontalListBuilder list, def signalName,
                                                         def labelId, def value) {
        list.add(
                cmp.text(signalName).setValueFormatter(new CSVFormulaFormatter()).setFixedColumns(15).setStyle(Templates.criteriaNameStyle),
                cmp.text(customMessageService.getMessage(labelId)).setFixedColumns(15).setStyle(Templates.criteriaNameStyle),
                cmp.text(value).setValueFormatter(new CSVFormulaFormatter()).setStyle(Templates.criteriaValueStyle)
        ).newRow()
    }

    private void addColumnsForQuantitativeCaseAlertListing(JasperReportBuilder report, Boolean isConsolidatedReport) {

        Map labelConfig = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', null, null, null).collectEntries {
            b -> [b.name, b.display]  //Added for PVS-65910
        }
        String rorColumnTitle = getRorColumnTitle(labelConfig.get("rorValue"), cacheService.getRorCache())


        if (isConsolidatedReport) {
            report.addColumn(Columns.column("Signal Name", "signalName", type.stringType()))
        }
        report.addColumn(Columns.column(labelConfig.get("name"), "alertName", type.stringType()))
                .addColumn(Columns.column(labelConfig.get("productName"), "productName", type.stringType()))
                .addColumn(Columns.column(labelConfig.get("soc"), "soc", type.stringType()))
                .addColumn(Columns.column("Event PT", "eventPT", type.stringType()))
                .addColumn(Columns.column(labelConfig.get("newCount"), "newCount/cummCount", type.stringType()))
                .addColumn(Columns.column(labelConfig.get("newSeriousCount"), "newSer/cummSer", type.stringType()))
                .addColumn(Columns.column(labelConfig.get("prrValue"), "prr", type.stringType()))
                .addColumn(Columns.column(rorColumnTitle, "ror", type.stringType()))
                .addColumn(Columns.column(labelConfig.get("ebgm"), "ebgm", type.stringType()))
                .addColumn(Columns.column(labelConfig.get("eb05"), "eb05/eb95", type.stringType()))
                .addColumn(Columns.column("Data Source", "dataSource", type.stringType()))
                .addColumn(Columns.column(labelConfig.get("disposition"), "disposition", type.stringType()))
    }

/**
 * Returns a specific part of the rorLabel based on the value of isRor.
 * If the rorLabel contains "#OR", it splits the label into parts.
 * If isRor is true, the first part is returned; otherwise, the second part is returned if it exists.
 *
 * @param rorLabel The label to be split and processed. May be null or empty.
 * @param isRor A boolean flag indicating which part of the split label to return.
 * @return The appropriate part of the rorLabel based on the isRor flag, or an empty string if rorLabel is null.
 */

    private String getRorColumnTitle(String rorLabel, Boolean isRor) {
        if (rorLabel == null || rorLabel.isEmpty()) {
            return ""
        }
        String[] parts = rorLabel.split("#OR")
        return (parts.length > 1 && !isRor) ? parts[1] : parts[0]
    }

    private void addColumnsForQualitativeCaseAlertListing(JasperReportBuilder report, Boolean isConsolidatedReport) {
        if (isConsolidatedReport) {
            report.addColumn(Columns.column("Signal Name", "signalName", type.stringType()))
        }
        report.addColumn(Columns.column("Alert Name", "alertName", type.stringType()))

        if(Holders.config.alert.priority.enable){
            report.addColumn(Columns.column("Priority", "priority", type.stringType()))
        }
        Map rptToUiLabelMap = cacheService.getRptToUiLabelMapForSafety() as Map
                report.addColumn(Columns.column(rptToUiLabelMap?.get('masterCaseNum') as String, "caseNumber", type.stringType()))
                .addColumn(Columns.column(rptToUiLabelMap?.get('productProductId') as String, "productName", type.stringType()))
                .addColumn(Columns.column(rptToUiLabelMap?.get('masterPrefTermSurAll'), "eventPt", type.stringType()))
                .addColumn(Columns.column("Disposition", "disposition", type.stringType()))
    }

    private void addColumnsForLiteratureCaseAlertListing(JasperReportBuilder report, Boolean isConsolidatedReport) {
        if (isConsolidatedReport) {
            report.addColumn(Columns.column("Signal Names", "signalNames", type.stringType()))
        }
        report.addColumn(Columns.column("Alert Name", "alertName", type.stringType()))
        if(Holders.config.alert.priority.enable){
            report.addColumn(Columns.column("Priority", "priority", type.stringType()))
        }
        report.addColumn(Columns.column("Article Title", "articleTitle", type.stringType()))
                .addColumn(Columns.column("Article Authors", "articleAuthors", type.stringType()))
                .addColumn(Columns.column("Publication Date", "publicationDate", type.stringType()))
                .addColumn(Columns.column("Disposition", "disposition", type.stringType()))
    }

    private void addColumnsForAdhocCaseAlertListing(JasperReportBuilder report, Boolean isConsolidatedReport) {
        if (isConsolidatedReport) {
            report.addColumn(Columns.column("Signal Name", "signalName", type.stringType()))
        }
        report.addColumn(Columns.column("Alert Name", "name", type.stringType()))
                .addColumn(Columns.column("Product Name", "productSelection", type.stringType()))
                .addColumn(Columns.column("Event PT", "eventSelection", type.stringType()))
                .addColumn(Columns.column("Detected By", "detectedBy", type.stringType()))
                .addColumn(Columns.column("Data Source", "initDataSrc", type.stringType()))
                .addColumn(Columns.column("Disposition", "disposition", type.stringType()))
    }

    private void addColumnsForOverallSignalSummaryListing(JasperReportBuilder report) {
        report.addColumn(Columns.column("Signal Name", "signalName", type.stringType()))
                .addColumn(Columns.column("Product Name", "product", type.stringType()))
                .addColumn(Columns.column("Date Detected", "detectedDate", type.stringType()))
                .addColumn(Columns.column("Signal Source", "initialDataSource", type.stringType()))
                .addColumn(Columns.column("Method of Evaluation", "evaluationMethod", type.stringType()))
                .addColumn(Columns.column("Status", "status", type.stringType()))
                .addColumn(Columns.column("Closed Date", "closedDate", type.stringType()))
                .addColumn(Columns.column("Priority", "priority", type.stringType()))
                .addColumn(Columns.column("Disposition", "disposition", type.stringType()))
    }

    private void addColumnsForActionTakenListing(JasperReportBuilder report, Boolean isConsolidatedReport) {
        if (isConsolidatedReport) {
            report.addColumn(Columns.column("Signal Name", "signalName", type.stringType()))
        }
        report.addColumn(Columns.column("Type", "type", type.stringType()))
                .addColumn(Columns.column("Action", "action", type.stringType()))
                .addColumn(Columns.column("Details", "details", type.stringType()))
                .addColumn(Columns.column("Due Date", "dueDate", type.stringType()))
                .addColumn(Columns.column("Assigned To", "assignedTo", type.stringType()))
                .addColumn(Columns.column("Status", "status", type.stringType()))
                .addColumn(Columns.column("Completion Date", "completionDate", type.stringType()))
    }

    private void addColumnsForMeetingListing(JasperReportBuilder report, Boolean isConsolidatedReport) {
        if (isConsolidatedReport) {
            report.addColumn(Columns.column("Signal Name", "signalName", type.stringType()))
        }
        report.addColumn(Columns.column("Title", "meetingTitle", type.stringType()).setWidth(30))
                .addColumn(Columns.column("Meeting Minutes", "meetingMinutes", type.stringType()).setWidth(70))
                .addColumn(Columns.column("Meeting Date", "meetingDate", type.stringType()).setWidth(70))
                .addColumn(Columns.column("Meeting Owner", "meetingOwner", type.stringType()).setWidth(70))
                .addColumn(Columns.column("Meeting Agenda", "meetingAgenda", type.stringType()).setWidth(70))

    }

    private void addColumnsForAssessmentDetailsListing(JasperReportBuilder report, Boolean isConsolidatedReport) {
        if (isConsolidatedReport) {
            report.addColumn(Columns.column("Signal Name", "signalName", type.stringType()).setWidth(20))
        }
        report.addColumn(Columns.column("Characteristics", "characteristics", type.stringType()).setWidth(60))
                .addColumn(Columns.column("Category", "category", type.stringType()).setWidth(20))
                .addColumn(Columns.column("Count of Cases (%)", "number", type.stringType()).setWidth(20))
                .addColumn(Columns.column("Count of Cases", "counts", type.stringType()).setWidth(20))
    }

    private void addColumnsForMedicalConceptsDistributionListing(JasperReportBuilder report, Boolean isConsolidatedReport) {
        if (isConsolidatedReport) {
            report.addColumn(Columns.column("Signal Name", "signalName", type.stringType()).setWidth(20))
        }
        report.addColumn(Columns.column("Medical Concept", "medicalConcept", type.stringType()).setWidth(60))
                .addColumn(Columns.column("Case Count", "caseCount", type.stringType()).setWidth(20))
                .addColumn(Columns.column("PEC Count(PVA)", "argusCount", type.stringType()).setWidth(20))
                .addColumn(Columns.column("PEC Count(EVDAS)", "evdasCount", type.stringType()).setWidth(20))
    }

    String setPrintablePageHeader(JasperReportBuilder report, String customReportHeader, boolean isCriteriaSheetOrAppendix, Map params, String tableName) {

        if (params.showCompanyLogo || (!params.advancedOptions || params.advancedOptions == "0") && params.showLogo) {
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

    String setPrintablePageHeaderWithoutLogo(JasperReportBuilder report, String customReportHeader, boolean isCriteriaSheetOrAppendix, Map params, String tableName) {

        report.setDefaultFont(Templates.defaultFontStyle)
        report.setColumnTitleStyle(columnTitleStyle)
        report.setColumnStyle(Templates.columnStyle)

        def reportTemplate = report.getReport().getTemplate()
        reportTemplate.setGroupTitleStyle(Templates.groupTitleStyle.style)
        reportTemplate.setGroupStyle(Templates.groupStyle.style)
        TextFieldBuilder textFieldHeader = cmp.text(customReportHeader).setStyle(Templates.pageHeader_HeaderStyle_left)
        report.pageHeader(cmp.verticalList(textFieldHeader).add(cmp.verticalGap(10)))
    }

    def getGroupNameFieldFromJson(jsonString){
        def prdName = ""
        def jsonObj = null
        if (jsonString) {
            jsonObj = parseJsonString(jsonString)
            if (!jsonObj)
                prdName = jsonString
            else {
                prdName=jsonObj.collect{
                    it.name.substring(0,it.name.lastIndexOf('(') - 1)
                }.join(",")
            }
        }
        prdName
    }

    def parseJsonString(str) {
        try {
            def jsonSlurper = new JsonSlurper()
            jsonSlurper.parseText(str)
        } catch (all) {
            null
        }
    }

    def getNameFieldFromJson(jsonString) {
        def prdName = ""
        def jsonObj = null
        if (jsonString) {
            jsonObj = parseJsonString(jsonString)
            if (!jsonObj)
                prdName = jsonString
            else {
                def prdVal = jsonObj.find {k,v->
                    v.find { it.containsKey('name') || it.containsKey('genericName')}
                }?.value.findAll{
                    it.containsKey('name') || it.containsKey('genericName')
                }.collect {it.name ? it.name : it.genericName }

                prdName = prdVal ? prdVal.sort().join(',') : ""
            }
        }
        prdName
    }



}
