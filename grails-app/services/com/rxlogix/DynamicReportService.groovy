package com.rxlogix

import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedTemplateQuery
import com.rxlogix.config.ReportResult
import com.rxlogix.dynamicReports.*
import com.rxlogix.dynamicReports.reportTypes.CommentHistoryReportBuilder
import com.rxlogix.dynamicReports.reportTypes.EVDASDrillDownReportBuilder
import com.rxlogix.dynamicReports.reportTypes.ImportAssignmentReportBuilder
import com.rxlogix.dynamicReports.reportTypes.ProductRuleReportBuilder
import com.rxlogix.enums.ReportFormat
import com.rxlogix.export.common.FontDto
import com.rxlogix.signal.AlertCommentHistory
import com.rxlogix.signal.CaseForm
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.ViewHelper
import grails.gorm.transactions.Transactional
import grails.util.Holders
import io.netty.handler.codec.DecoderException
import net.sf.dynamicreports.jasper.builder.JasperConcatenatedReportBuilder
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder
import net.sf.dynamicreports.report.constant.WhenNoDataType
import net.sf.jasperreports.engine.JRDataSource
import org.apache.commons.codec.binary.Hex
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.ss.util.CellUtil
import org.apache.poi.util.IOUtils
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.*

import java.text.SimpleDateFormat

import static net.sf.dynamicreports.report.builder.DynamicReports.cmp
import static net.sf.dynamicreports.report.builder.DynamicReports.concatenatedReport

@Transactional
class DynamicReportService {
    def grailsApplication
    def configurationService
    def customMessageService
    def userService
    def imageService
    def excelService
    def configManagementService
    def cacheService

    // Report Creation ---------------------------------------------------------------------------------------------------
    File createAlertsReport(JRDataSource dataSource, Map params) {
        def name = Constants.AlertConfigType.SINGLE_CASE_ALERT
        def startTime = System.currentTimeMillis()
        ReportBuilder reportBuilder = new SingleCaseReviewAlertsReportBuilder()
        JasperConcatenatedReportBuilder mainReport = concatenatedReport()

        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()
        reportBuilder.createReport(dataSource, params, jasperReportBuilderEntryList)
        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[]);
        OutputBuilder outputBuilder = new OutputBuilder()
        File reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")
        return reportFile
    }

    File createSignalsReport(JRDataSource dataSource, Map params) {
        def name = Constants.AlertConfigType.VALIDATED_SIGNAL
        def startTime = System.currentTimeMillis()
        File reportFile = getReportFile(name, params.outputFormat)
        SignalListReportBuilderService signalListReportBuilderService = new SignalListReportBuilderService()
        JasperConcatenatedReportBuilder mainReport = concatenatedReport()

        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()
        signalListReportBuilderService.createReport(dataSource, params, jasperReportBuilderEntryList)

        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[]);

        OutputBuilder outputBuilder = new OutputBuilder()
        reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")

        return reportFile
    }

    File createSignalMemoReport(JRDataSource dataSource, Map params) {
        def name = params.inputName ?: Constants.AlertConfigType.VALIDATED_SIGNAL
        def startTime = System.currentTimeMillis()
        File reportFile = getReportFile(name, params.outputFormat)
        SignalMemoReportBuilder signalMemoReportBuilder = new SignalMemoReportBuilder()
        JasperConcatenatedReportBuilder mainReport = concatenatedReport()

        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()
        signalMemoReportBuilder.createReport(dataSource, params, jasperReportBuilderEntryList)

        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[]);

        OutputBuilder outputBuilder = new OutputBuilder()
        reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")

        return reportFile
    }

    File createEVDASDrillDownReport(JRDataSource dataSource, Map params, String fileName = null) {
        def name = fileName ?: Constants.AlertConfigType.VALIDATED_SIGNAL
        def startTime = System.currentTimeMillis()
        File reportFile = getReportFile(name, params.outputFormat)
        EVDASDrillDownReportBuilder evdasDrillDownReportBuilder = new EVDASDrillDownReportBuilder()
        JasperConcatenatedReportBuilder mainReport = concatenatedReport()

        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()
        evdasDrillDownReportBuilder.createReport(dataSource, params, jasperReportBuilderEntryList)

        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[]);

        OutputBuilder outputBuilder = new OutputBuilder()
        reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")

        return reportFile
    }

    File createProductAssignmentReport(JRDataSource dataSource, Map params, Map criteriaInfo) {
        def name = Constants.AlertConfigType.VALIDATED_SIGNAL
        def startTime = System.currentTimeMillis()
        File reportFile = getReportFile(name, params.outputFormat)
        ProductAssignmentReportBuilder productAssignmentReportBuilder = new ProductAssignmentReportBuilder()
        JasperConcatenatedReportBuilder mainReport = concatenatedReport()

        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()
        productAssignmentReportBuilder.createReport(dataSource, params, jasperReportBuilderEntryList,criteriaInfo)

        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[]);

        OutputBuilder outputBuilder = new OutputBuilder()
        reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")

        return reportFile
    }

    File createImportAssignmentReport(JRDataSource dataSource, Map params, Map importedInformation, User importedBy) {
        def name = Constants.AlertConfigType.VALIDATED_SIGNAL
        def startTime = System.currentTimeMillis()
        File reportFile = getReportFile(name, params.outputFormat)
        ImportAssignmentReportBuilder importAssignmentReportBuilder = new ImportAssignmentReportBuilder()
        JasperConcatenatedReportBuilder mainReport = concatenatedReport()

        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()
        importAssignmentReportBuilder.createReport(dataSource, params, jasperReportBuilderEntryList,importedInformation, importedBy)

        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[]);

        OutputBuilder outputBuilder = new OutputBuilder()
        reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")

        return reportFile
    }

    File createtopicsReport(JRDataSource dataSource, Map params) {
        def name = Constants.AlertConfigType.VALIDATED_SIGNAL
        def startTime = System.currentTimeMillis()
        File reportFile = getReportFile(name, params.outputFormat)
        TopicListReportBuilderService topicListReportBuilderService = new TopicListReportBuilderService()
        JasperConcatenatedReportBuilder mainReport = concatenatedReport()

        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()
        topicListReportBuilderService.createReport(dataSource, params, jasperReportBuilderEntryList)

        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[]);

        OutputBuilder outputBuilder = new OutputBuilder()
        reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")

        return reportFile
    }

    File createAggregateAlertsReport(JRDataSource dataSource, Map params) {
        def name = Constants.DynamicReports.AGG_CASE_ALERT
        def startTime = System.currentTimeMillis()
        File reportFile = getReportFile(name, params.outputFormat)
        AggregateCaseAlertReportBuilder aggregateCaseAlertAlertsReportBuilder = new AggregateCaseAlertReportBuilder()
        JasperConcatenatedReportBuilder mainReport = concatenatedReport()
        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()
        aggregateCaseAlertAlertsReportBuilder.createReport(dataSource, params, jasperReportBuilderEntryList)
        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[]);
        OutputBuilder outputBuilder = new OutputBuilder()
        reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")
        return reportFile
    }

    File createEvdasAlertsReport(JRDataSource dataSource, Map params) {
        def name = "EVDAS Alerts"
        def startTime = System.currentTimeMillis()
        File reportFile = getReportFile(name, params.outputFormat)
        EvdasAlertReportBuilder evdasAlertReportBuilder = new EvdasAlertReportBuilder()
        JasperConcatenatedReportBuilder mainReport = concatenatedReport()
        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()
        evdasAlertReportBuilder.createReport(dataSource, params, jasperReportBuilderEntryList)
        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[]);
        OutputBuilder outputBuilder = new OutputBuilder()
        reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")
        return reportFile
    }

    File createActiviesReport(JRDataSource dataSource, Map params, String name) {
        def startTime = System.currentTimeMillis()
        name = MiscUtil.getValidFileName(name) + "_Activities"
        ActivitiesReportBuilder activitiesReportBuilder = new ActivitiesReportBuilder()
        JasperConcatenatedReportBuilder mainReport = concatenatedReport()
        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()
        activitiesReportBuilder.createReport(dataSource, params, jasperReportBuilderEntryList)
        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[]);
        OutputBuilder outputBuilder = new OutputBuilder()
        File reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")
        return reportFile
    }

    File createActionsReport(JRDataSource dataSource, Map params, String name, Boolean isConsolidated) {
        def startTime = System.currentTimeMillis()
        File reportFile = getReportFile(name, params.outputFormat)
        ActionReportBuilder actionReportBuilder = new ActionReportBuilder()
        JasperConcatenatedReportBuilder mainReport = concatenatedReport()
        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()
        actionReportBuilder.createReport(dataSource, params, jasperReportBuilderEntryList)
        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[]);
        OutputBuilder outputBuilder = new OutputBuilder()
        reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")
        return reportFile
    }

    File createCaseDetailReport(Map caseMultiMap,
                                JRDataSource dataSourceCaseAttachments, JRDataSource dataSourceCaseComments,
                                JRDataSource dataSourceCaseActions, JRDataSource dataSourceCaseHistory,
                                JRDataSource dataSourceSuspectProductHistory, Map params) {
        String name = "Case Detail Report"
        def startTime = System.currentTimeMillis()
        CaseDetailReportBuilder caseDetailReportBuilder = new CaseDetailReportBuilder()
        JasperConcatenatedReportBuilder mainReport = concatenatedReport()
        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()
        params.criteriaSheetList = createCriteriaList(userService.getUser(), null, null, false, false)
        //Filter is not required PVS-53068
        caseDetailReportBuilder.createReport(caseMultiMap, dataSourceCaseAttachments, dataSourceCaseComments, dataSourceCaseActions,
                dataSourceCaseHistory, dataSourceSuspectProductHistory, params, jasperReportBuilderEntryList)
        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[]);
        OutputBuilder outputBuilder = new OutputBuilder()
        File reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")
        return reportFile
    }


    File createPeberSignalReport(JRDataSource signalDataList, Map params, Boolean isConsolidated) {
        def name = "Summary Of Signals Peber"
        def startTime = System.currentTimeMillis()

        File reportFile = getReportFile(name, params.outputFormat)

        SignalSummaryPeberReportBuilder signalSummaryReportBuilder = new SignalSummaryPeberReportBuilder()

        JasperConcatenatedReportBuilder mainReport = concatenatedReport()

        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()

        signalSummaryReportBuilder.createReport(signalDataList, params, jasperReportBuilderEntryList, isConsolidated)

        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder

        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[])

        OutputBuilder outputBuilder = new OutputBuilder()
        reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")
        return reportFile
    }

    File createSignalStateReport(JRDataSource signalDataList, Map params) {
        def name = "Signal By State"

        def startTime = System.currentTimeMillis()

        File reportFile = getReportFile(name, params.outputFormat)

        SignalStateReportBuilder signalStateReportBuilder = new SignalStateReportBuilder()

        JasperConcatenatedReportBuilder mainReport = concatenatedReport()

        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()

        signalStateReportBuilder.createReport(signalDataList, params, jasperReportBuilderEntryList)

        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder

        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[])

        OutputBuilder outputBuilder = new OutputBuilder()
        reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")
        return reportFile
    }

    File createTopicReport(JRDataSource signalDataList, Map params) {
        def name = "Summary Of Topic"
        def startTime = System.currentTimeMillis()

        File reportFile = getReportFile(name, params.outputFormat)

        SignalSummaryPeberReportBuilder signalSummaryReportBuilder = new SignalSummaryPeberReportBuilder()

        JasperConcatenatedReportBuilder mainReport = concatenatedReport()

        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()

        signalSummaryReportBuilder.createReport(signalDataList, params, jasperReportBuilderEntryList, false)

        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder

        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[])

        OutputBuilder outputBuilder = new OutputBuilder()
        reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")
        return reportFile
    }

    File createSignalDetectionReport(JRDataSource validatedSignalList, JRDataSource notStartedReviewSignalList,
                                     JRDataSource pendingReviewList, JRDataSource closedReviewList,
                                     signalData, Map params) {

        def name = "SignalDetectionSummary"
        def startTime = System.currentTimeMillis()

        File reportFile = getReportFile(name, params.outputFormat)

        SignalDetectionSummaryReportBuilder signalSummaryReportBuilder = new SignalDetectionSummaryReportBuilder()

        JasperConcatenatedReportBuilder mainReport = concatenatedReport()

        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()

        signalSummaryReportBuilder.createReport(
                validatedSignalList, notStartedReviewSignalList, pendingReviewList,
                closedReviewList, signalData, params, jasperReportBuilderEntryList)


        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder

        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[])

        OutputBuilder outputBuilder = new OutputBuilder()
        reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")
        return reportFile

    }


    File createProductMemoReport(JRDataSource singleCaseAlertsSummaryList, JRDataSource aggregateCaseAlertsSummaryList, signalData, Map params) {

        def name = "ProductMemoReport"
        def startTime = System.currentTimeMillis()

        File reportFile = getReportFile(name, params.outputFormat)

        ProductMemoReportBuilder productMemoReportBuilder = new ProductMemoReportBuilder()

        JasperConcatenatedReportBuilder mainReport = concatenatedReport()

        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()

        productMemoReportBuilder.createReport(singleCaseAlertsSummaryList, aggregateCaseAlertsSummaryList,
                signalData, params, jasperReportBuilderEntryList
        )

        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder

        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[])

        OutputBuilder outputBuilder = new OutputBuilder()
        reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile?.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")
        return reportFile
    }

    File createAssessmentReport(signalData, Map params) {

        def name = "AssessmentReportForSafetySignal"
        def startTime = System.currentTimeMillis()

        File reportFile = getReportFile(name, params.outputFormat)

        AssessmentForSignalDetection assessmentForSignalDetection = new AssessmentForSignalDetection()

        JasperConcatenatedReportBuilder mainReport = concatenatedReport()

        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()
        params.criteriaSheetList = createCriteriaList(userService.getUser())
        assessmentForSignalDetection.createReport(signalData, params, jasperReportBuilderEntryList)

        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder

        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[])

        OutputBuilder outputBuilder = new OutputBuilder()
        reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")
        return reportFile

    }

    File createSignalSummaryReport(List quantitativeCaseAlertListingData, List qualitativeCaseAlertListingData, List adhocCaseAlertListingData, List literatureCaseAlertListingData,
                                   List actionsList, List meetingList, signalSummary, signalDetails, Map params,
                                   def summaryReportPreference,
                                   List assessmentDetailsList, List medicalConceptDistributionList, Boolean isConsolidatedReport,Boolean uploadAssessment=false,String addReferenceName=null,Boolean criteriaSheetCheck = false) {

        def name = "SignalSummaryReport"
        def startTime = System.currentTimeMillis()

        File reportFile = getReportFile(name, params.outputFormat)

        SignalSummaryReportBuilder signalSummaryReportBuilder = new SignalSummaryReportBuilder()

        JasperConcatenatedReportBuilder mainReport = concatenatedReport()

        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()
        if(!criteriaSheetCheck){
            params.criteriaSheetList = createCriteriaList(userService.getUser(), null, null, false, false)
            //Filter is not required PVS-53068
        }
        signalSummaryReportBuilder.createReport(quantitativeCaseAlertListingData, qualitativeCaseAlertListingData, adhocCaseAlertListingData, literatureCaseAlertListingData, actionsList, meetingList,
                signalSummary, signalDetails, params, jasperReportBuilderEntryList, summaryReportPreference, assessmentDetailsList, medicalConceptDistributionList, isConsolidatedReport, uploadAssessment, addReferenceName,criteriaSheetCheck)

        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder

        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[])

        OutputBuilder outputBuilder = new OutputBuilder()
        reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")
        return reportFile

    }

    File createAlertHistoryReport(List caseHistoryList, List suspectProductHistoryList, Map params) {
        def isLiteratureAlert = params.get('alertName') == 'literatureHistory'
        def name = isLiteratureAlert ? Constants.AlertType.LITERATURE_ALERT : Constants.AlertConfigType.SINGLE_CASE_ALERT
        def startTime = System.currentTimeMillis()
        String searchField = params.searchField ?: null
        String otherSearchString = params.otherSearchString ?: null
        CaseHistoryReportBuilder caseHistortReportBuilder = new CaseHistoryReportBuilder()
        JasperConcatenatedReportBuilder mainReport = concatenatedReport()
        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()

        if (isLiteratureAlert) {
            params.criteriaSheetList = createCriteriaListForLiteratureAlertProductHistory(userService.getUser(), searchField, otherSearchString)
        } else {
            params.criteriaSheetList = createCriteriaListForSingleCaseProductHistory(userService.getUser(), searchField, otherSearchString)
        }

        caseHistortReportBuilder.createReport(caseHistoryList, suspectProductHistoryList, params, jasperReportBuilderEntryList, name == Constants.AlertType.LITERATURE_ALERT)
        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[]);
        OutputBuilder outputBuilder = new OutputBuilder()
        File reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")
        return reportFile
    }

    File createProductEventHistoryReport(List caseAlertHistoryList, List otherCaseAlertsHistoryList, Map params) {
        def name = Constants.AlertConfigType.AGGREGATE_CASE_ALERT
        def startTime = System.currentTimeMillis()
        File reportFile = getReportFile(name, params.outputFormat)
        ProductEventHistoryReportBuilder productEventHistoryReportBuilder= new ProductEventHistoryReportBuilder()
        JasperConcatenatedReportBuilder mainReport = concatenatedReport()
        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()
        productEventHistoryReportBuilder.createReport(caseAlertHistoryList, otherCaseAlertsHistoryList, params, jasperReportBuilderEntryList)
        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[]);
        OutputBuilder outputBuilder = new OutputBuilder()
        reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")
        return reportFile
    }

    File createCommentHistoryReport(List commentHistoryList, Map params, AlertCommentHistory alertComment) { // added alertCommentList to fix bug/PVS-60893
        def name = Constants.AlertConfigType.AGGREGATE_CASE_ALERT
        def startTime = System.currentTimeMillis()
        File reportFile = getReportFile(name, params.outputFormat)
        CommentHistoryReportBuilder commentHistoryReportBuilder= new CommentHistoryReportBuilder()
        JasperConcatenatedReportBuilder mainReport = concatenatedReport()
        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()
        commentHistoryReportBuilder.createReport(commentHistoryList, params, jasperReportBuilderEntryList, alertComment)
        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[]);
        OutputBuilder outputBuilder = new OutputBuilder()
        reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")
        return reportFile
    }
    File createProductRuleReport(List productRuleList, Map params) {
        def name = Constants.AlertConfigType.AGGREGATE_CASE_ALERT
        def startTime = System.currentTimeMillis()
        File reportFile = getReportFile(name, params.outputFormat)
        String searchString = params.searchString ?: null
        params.criteriaSheetList = createCriteriaList(userService.getUser(),null,searchString)
        ProductRuleReportBuilder productRuleReportBuilder= new ProductRuleReportBuilder()
        JasperConcatenatedReportBuilder mainReport = concatenatedReport()
        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()
        productRuleReportBuilder.createReport(productRuleList, params, jasperReportBuilderEntryList)
        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[]);
        OutputBuilder outputBuilder = new OutputBuilder()
        reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")
        return reportFile
    }

    File createAlertsDetailReport(JRDataSource dataSource, Map params, String title) {
        def name = Constants.AlertConfigType.SINGLE_CASE_ALERT + " Details"
        def startTime = System.currentTimeMillis()
        File reportFile = getReportFile(name, params.outputFormat)
        SingleCaseAlertDetailsReportBuilder singleCaseAlertDetailReportBuilder = new SingleCaseAlertDetailsReportBuilder()
        JasperConcatenatedReportBuilder mainReport = concatenatedReport()

        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()

        singleCaseAlertDetailReportBuilder.createReport(dataSource, params, jasperReportBuilderEntryList, title)

        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[]);

        OutputBuilder outputBuilder = new OutputBuilder()
        reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")

        return reportFile
    }

    File createSignalDetailReport(JRDataSource dataSource, Map params, String title) {
        def name = "SignalDetail"
        def startTime = System.currentTimeMillis()
        File reportFile = getReportFile(name, params.outputFormat)
        ValidatedSignalReportService validatedSignalReportService = new ValidatedSignalReportService()
        JasperConcatenatedReportBuilder mainReport = concatenatedReport()

        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()

        validatedSignalReportService.createReport(dataSource, params, jasperReportBuilderEntryList)

        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[]);

        OutputBuilder outputBuilder = new OutputBuilder()
        reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")

        return reportFile
    }

    File createMeetingDetailReport(JRDataSource dataSource, Map params) {
        String name = "MeetingDetails"
        def startTime = System.currentTimeMillis()
        File reportFile = getReportFile(name, params.outputFormat)
        MeetingDetailsReportService meetingDetailsReportService = new MeetingDetailsReportService()
        JasperConcatenatedReportBuilder mainReport = concatenatedReport()

        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()

        meetingDetailsReportService.createReport(dataSource, params, jasperReportBuilderEntryList)

        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[]);

        OutputBuilder outputBuilder = new OutputBuilder()

        reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")

        return reportFile
    }

    File createSignalActionDetailReport(JRDataSource dataSource, Map params) {
        def name = "SignalProductActions"
        def startTime = System.currentTimeMillis()
        File reportFile = getReportFile(name, params.outputFormat)
        SignalActionReportService actionReportService = new SignalActionReportService()
        JasperConcatenatedReportBuilder mainReport = concatenatedReport()

        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()
        params.SignalProductActions = true
        params.criteriaSheetList = createCriteriaList(userService.getUser(), null, null, false, false)
        //Filter is not required PVS-53068
        actionReportService.createReport(dataSource, params, jasperReportBuilderEntryList)

        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[]);

        OutputBuilder outputBuilder = new OutputBuilder()
        reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")

        return reportFile
    }


    File createAdhocReport(JRDataSource dataSource, Map params) {
        def name = Constants.AlertConfigType.AD_HOC_ALERT
        def startTime = System.currentTimeMillis()
        File reportFile = getReportFile(name, params.outputFormat)
        AdHocAlertReportBuilder adHocAlertReportBuilder = new AdHocAlertReportBuilder()
        JasperConcatenatedReportBuilder mainReport = concatenatedReport()

        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()

        adHocAlertReportBuilder.createReport(dataSource, params, jasperReportBuilderEntryList)

        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[]);

        OutputBuilder outputBuilder = new OutputBuilder()
        reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")

        return reportFile
    }

    File createAdHocAlertsDetailReport(JRDataSource dataSource, Map params, String title) {
        List<Map> adhocConfigurationList = cacheService.getAdhocConfigurationCache()
        Map<String, String> nameLabelMap = [:]

        // Find and update the nameLabelMap for the two specific names: 'matchingAlerts' and 'issuePreviouslyTracked'
        adhocConfigurationList.each { item ->
            if ((item.name == 'name' || item.name == 'notes' || item.name == 'currentDisposition') && item.enabled) {
                nameLabelMap[item.name] = item.label
            }
        }
        params.put("labelMap", nameLabelMap)
        def name = Constants.AlertConfigType.AD_HOC_ALERT + " Details"
        def startTime = System.currentTimeMillis()
        def searchVal = params."search[value]" ?: null
        params.criteriaSheetList = createCriteriaList(userService.getUser(),null,searchVal)
        File reportFile = getReportFile(name, params.outputFormat)
        AdHocAlertDetailsReportBuilder adHocAlertDetailReportBuilder = new AdHocAlertDetailsReportBuilder()
        JasperConcatenatedReportBuilder mainReport = concatenatedReport()

        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()

        adHocAlertDetailReportBuilder.createReport(dataSource, params, jasperReportBuilderEntryList, title)

        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[]);

        OutputBuilder outputBuilder = new OutputBuilder()
        reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")

        return reportFile
    }

    File createProductSummaryReport(JRDataSource dataSource, Map params) {
        def name = Constants.AlertConfigType.VALIDATED_SIGNAL
        def startTime = System.currentTimeMillis()
        File reportFile = getReportFile(name, params.outputFormat)
        ProductSummaryReportBuilderService productSummaryReportBuilderService = new ProductSummaryReportBuilderService()
        JasperConcatenatedReportBuilder mainReport = concatenatedReport()

        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()
        productSummaryReportBuilderService.createReport(dataSource, params, jasperReportBuilderEntryList)

        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[]);

        OutputBuilder outputBuilder = new OutputBuilder()
        reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")

        return reportFile
    }

    File createLiteratureActivityReport(JRDataSource dataSource, Map params) {
        def name = "Literature Activities"
        def startTime = System.currentTimeMillis()
        File reportFile = getReportFile(name, params.outputFormat)
        LiteratureActivityReportBuilderService literatureAlertReportBuilderService = new LiteratureActivityReportBuilderService()
        JasperConcatenatedReportBuilder mainReport = concatenatedReport()

        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()
        literatureAlertReportBuilderService.createReport(dataSource, params, jasperReportBuilderEntryList)

        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[]);

        OutputBuilder outputBuilder = new OutputBuilder()
        reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")

        return reportFile
    }

    File createLiteratureAlertReport(JRDataSource dataSource, Map params) {
        def name = Constants.AlertConfigType.LITERATURE_SEARCH_ALERT
        def startTime = System.currentTimeMillis()
        File reportFile = getReportFile(name, params.outputFormat)
        LiteratureAlertReportBuilderService literatureAlertReportBuilderService = new LiteratureAlertReportBuilderService()
        JasperConcatenatedReportBuilder mainReport = concatenatedReport()

        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()
        literatureAlertReportBuilderService.createReport(dataSource, params, jasperReportBuilderEntryList)

        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[]);

        OutputBuilder outputBuilder = new OutputBuilder()
        reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")

        return reportFile
    }

    File createCaseFormReport(Map caseFormMap , CaseForm caseForm, Map criteriaMap, Map commentsMap, caseIdCommentsMap, Map caseInfoMap,
        Map sectionRelatedInfo, Map prevCaseInfoMap, String outputFormat,Map reportDataExport){
        String name = caseForm?.formName
        def startTime = System.currentTimeMillis()
        File reportFile = getReportFile(name, outputFormat)
        CaseFormReportBuilder caseFormReportBuilder = new CaseFormReportBuilder()
        JasperConcatenatedReportBuilder mainReport = concatenatedReport()
        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()
        caseFormReportBuilder.createReport(jasperReportBuilderEntryList, caseFormMap, caseForm, criteriaMap, commentsMap,
                                            caseIdCommentsMap, caseInfoMap, sectionRelatedInfo, prevCaseInfoMap, outputFormat,reportDataExport)
        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[]);
        OutputBuilder outputBuilder = new OutputBuilder()
        Map params = [outputFormat:outputFormat,type: Constants.DynamicReports.CASE_FORM]
        reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")
        return reportFile
    }



    //Helpers ----------------------------------------------------------------------------------------------------------

    JasperReportBuilder checkIfNoData(ReportResult reportResult, JasperReportBuilder report) {
        if (!reportResult?.data?.value) {
            report.setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL)
            report.columnHeader(cmp.text("This report contains no data.").setHeight(7));
        }
        report
    }

    public String getReportsDirectory() {
        return Holders.config.tempDirectory as String
    }

    /**
     * This lets us know if we are viewing the report on screen (HTML) or printing it to any of the supported output formats.
     * This is needed to determine whether we show the criteria sheet and appendix.  It may be used to toggle
     * styles that differ between HTML and the printable output formats.
     * @param params
     * @return
     */
    boolean isInPrintMode(Map params) {
        if (!params.outputFormat || params.outputFormat == ReportFormat.HTML.name()) {
            return false
        }
        return true
    }

    String getReportFilename(String name, String outputFormat) {
        if (!outputFormat) {
            outputFormat = ReportFormat.HTML.name()
        }
        return name + "." + outputFormat.toLowerCase()
    }

    /**
     * Convenience method to get a previously created report File object
     * @param executedConfiguration
     * @return File
     */
    private File getReportFile(String name, String outputFormat) {
        String filename = getReportFilename(name, outputFormat)
        File file = new File(getReportsDirectory() + filename)
        if (file?.exists() && file?.size() > 0) {
            return file
        }
        return null
    }
    File createProductGroupBatchReport(JRDataSource dataSource, Map params) {
        def name = params.name
        def startTime = System.currentTimeMillis()
        File reportFile = getReportFile(name, params.outputFormat)
        ProductGroupsBatchReportBuilder pgBatchReportBuilder = new ProductGroupsBatchReportBuilder()
        JasperConcatenatedReportBuilder mainReport = concatenatedReport()

        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()
        params.criteriaSheetList = createCriteriaList(userService.getUser())
        pgBatchReportBuilder.createReport(dataSource, params, jasperReportBuilderEntryList)
        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[]);

        OutputBuilder outputBuilder = new OutputBuilder()
        reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")

        return reportFile
    }
    // Remove index from field name. Return original report field name.
    private String getFieldName(String colName) {
        int lastIndex = colName.lastIndexOf('_')
        return colName.substring(0, lastIndex)
    }

    String getContentType(ReportFormat reportFormat) {
        return Holders.config.grails.mime.types[reportFormat?.name()?.toLowerCase()]?.first()
    }

    String getContentType(String extension) {
        return Holders.config.grails.mime.types[extension?.toLowerCase()]
    }

    String getNameAsTitle(ExecutedConfiguration executedConfiguration, ExecutedTemplateQuery executedTemplateQuery) {

        String sectionName

        if (executedConfiguration?.executedTemplateQueries?.size() > 1) {
            if (executedTemplateQuery.title == executedConfiguration.name) {
                sectionName = executedConfiguration.name + ": " + executedTemplateQuery.executedTemplate.name
            } else {
                sectionName = executedTemplateQuery.title
            }
        } else {
            sectionName = executedTemplateQuery.title
        }

        sectionName
    }

    String getName(ExecutedConfiguration executedConfigurationInstance, Map params) {
        def suffix = ""

        if (params.advancedOptions == "1") {
            //Suffix added to report name if choosing to Save As from Advanced Options
            suffix = Constants.DynamicReports.ADVANCED_OPTIONS_SUFFIX
        }

        return "E$executedConfigurationInstance.id$suffix"
    }

    String getName(ReportResult reportResult, Map params) {
        def suffix = ""

        if (params.advancedOptions == "1") {
            //Suffix added to report name if choosing to Save As from Advanced Options
            suffix = Constants.DynamicReports.ADVANCED_OPTIONS_SUFFIX
        }

        return "R$reportResult.id$suffix"
    }

    private boolean isCached(File reportFile, Map params) {
        reportFile && (!params.advancedOptions || params?.advancedOptions == "0")
    }

    def noDataCheck(dataSource, report){
        if (!dataSource?.data?.value) {
            report.setWhenNoDataType(WhenNoDataType.ALL_SECTIONS_NO_DETAIL)
            report.columnHeader(cmp.text(customMessageService.getMessage("app.label.report.blank")).setHeight(7));
        }
        report
    }

    private void writeCriteriaSheet(Workbook wb, Sheet sheet, User importedBy, def metaData = null, boolean isImportConfExport = false, boolean includeFilters = true) {

        int xoffset = 0
        int yoffset = 4
        int rowNumber = 5
        List data = createCriteriaList(importedBy, metaData, null, isImportConfExport, includeFilters)
        Map criteriaMap = [:]
        data.each {
            criteriaMap[it['label']] = it['value']
        }
        FontDto fontDto = getFontDtoObj(false)
        Font font = getFont(wb, fontDto)
        CellStyle styleRs = wb.createCellStyle()
        styleRs.setFont(font)
        FontDto italicFontDto = getItalicFontDtoObj()
        Font ItalicFont = getFont(wb, italicFontDto)
        CellStyle noteStyleRs = wb.createCellStyle()
        noteStyleRs.setFont(ItalicFont)
        if (data != null) {
            Row row = sheet.createRow(rowNumber++)
            Cell cell = row.createCell(0)
            cell.setCellValue("Criteria Sheet")
            CellUtil.setAlignment(cell,HorizontalAlignment.CENTER.CENTER)
            Cell noteCell

            criteriaMap.each { k, v ->
                row = sheet.createRow(rowNumber++)
                cell = row.createCell(0)
                if(k==Constants.CriteriaSheetLabels.IMPORT_SHEET_NOTE){
                    noteCell = cell
                }
                cell.setCellValue(k)
                cell.setCellType(CellType.STRING)
                cell = row.createCell(1)
                cell.setCellValue(v)
                cell.setCellType(CellType.STRING)
                sheet.setColumnWidth(0,30 * 256)
                sheet.setColumnWidth(1,30 * 256)
            }
            FontDto fontHeaderDto = getFontDtoObj(true)
            Font fontHeaders = getFont(wb, fontHeaderDto)
            fontHeaders.setItalic(false)
            CellStyle styleHeader = wb.createCellStyle()
            styleHeader.setFont(fontHeaders)
            // Add Criteria Sheet Header and merging 2 columns
            Row headerRow = sheet.createRow(yoffset)
            Cell cellData1 = headerRow.createCell(xoffset)
            cellData1.setCellType(CellType.STRING)
            CellUtil.setAlignment(cellData1, HorizontalAlignment.CENTER)
            noteCell?.setCellStyle(noteStyleRs)
        }
    }


    private void writeImage(Workbook wb, Sheet sheet) {
        try {
            int rowSpan = 3
            int colSpan = 2
            Row row
            //create the cells with certain heights
            for (int r = 0; r < rowSpan; r++) {
                row = sheet.createRow(r);
                row.setHeightInPoints(15);
            }
            //merge cells
            sheet.addMergedRegion(new CellRangeAddress(0,rowSpan-1,0,colSpan-1));
            //set column width of column in default character widths
            sheet.setColumnWidth(0, 30 * 256);
            sheet.setColumnWidth(1, 30 * 256);
            //FileInputStream obtains input bytes from the image file
            InputStream inputStream = imageService.getImage("company-logo.png")
            //Get the contents of an InputStream as a byte[].
            byte[] bytes = IOUtils.toByteArray(inputStream)
            //Adds a picture to the workbook
            int pictureIdx = wb.addPicture(bytes, Workbook.PICTURE_TYPE_PNG)
            //close the input stream
            inputStream.close()
            //Returns an object that handles instantiating concrete classes
            CreationHelper helper = wb.getCreationHelper()
            //Creates the top-level drawing patriarch.
            Drawing drawing = sheet.createDrawingPatriarch()
            //Create an anchor that is attached to the worksheet
            ClientAnchor anchor = helper.createClientAnchor()
            //set top-left corner for the image
            anchor.col1 = 0
            anchor.row1 = 0
            //Creates a picture
            Picture pict = drawing.createPicture(anchor, pictureIdx)
            //get the picture width in px
            int pictWidthPx = (int) pict.getImageDimension().width;
            //get the picture height in px
            int pictHeightPx = (int) pict.getImageDimension().height;
            //get the width of all merged cols in px
            float columnWidthPx = sheet.getColumnWidthInPixels(0);

            //get the heights of all merged rows in px
            float[] rowHeightsPx = new float[anchor.row1+rowSpan];
            float rowsHeightPx = 0f;
            for (int r = anchor.row1; r < anchor.row1+rowSpan; r++) {
                Row row1 = sheet.getRow(r);
                float rowHeightPt = row1.getHeightInPoints();
                rowsHeightPx += (float) (rowHeightPt * Units.PIXEL_DPI / Units.POINT_DPI);
            }
            //setting-up scale on behalf of either height or widht(whichever is minimum)
            float scale = 1;
            if (pictHeightPx > rowsHeightPx) {
                float tmpscale = rowsHeightPx / (float)pictHeightPx;
                if (tmpscale < scale) scale = tmpscale;
            }
            if (pictWidthPx > columnWidthPx) {
                float tmpscale = columnWidthPx / (float)pictWidthPx;
                if (tmpscale < scale) scale = tmpscale;
            }

            anchor.setDx1(0); //setting image in left-most side
            anchor.setDy1(0); //setting image in top-most up

            //resize the picture to it's native size
            pict.resize()
            //if it must scaled down, then scale
            if (scale < 1) {
                pict.resize(scale);
            }

        } catch (Exception e) {
            System.out.println(e)
        }
    }

    FontDto getFontDtoObj(Boolean isHeader = false) {
        FontDto fontObj
            fontObj = new FontDto()
            fontObj.setItalics(false)
            fontObj.setBold(false)
            fontObj.setFontName("Arial Unicode")
            fontObj.setCellWidth(16)
            fontObj.setFontSize((short)9)
            if(isHeader){
                fontObj.setBold(true)
            }
        return fontObj
    }
    FontDto getItalicFontDtoObj() {
        FontDto fontObj
        fontObj = new FontDto()
        fontObj.setItalics(true)
        fontObj.setBold(false)
        fontObj.setFontName("Arial Unicode")
        fontObj.setCellWidth(30)
        fontObj.setFontSize((short)9)
        return fontObj
    }
    private Font getFont(Workbook wb, FontDto fontObj) {
        Font font = wb.createFont()
        font.setFontHeightInPoints(fontObj.getFontSize())
        font.setFontName(fontObj.getFontName())
        font.setItalic(fontObj.isItalics())
        font.setBold(fontObj.isBold())
        return font
    }

    byte[] exportToExcelImportConfigurationList(Map data, Map metadata, Map params, boolean isImportConfExport = false) {
        XSSFWorkbook workbook = new XSSFWorkbook()
        XSSFSheet criteriaWorksheet = workbook.createSheet("Criteria")
        writeImage(workbook, criteriaWorksheet)
        writeCriteriaSheet(workbook, criteriaWorksheet, userService.getUser(), null, isImportConfExport)
        XSSFSheet worksheet = workbook.createSheet(metadata?.sheetName1 ?: "Data");

        XSSFFont defaultFont = workbook.createFont();
        defaultFont.setFontHeightInPoints((short) 10);
        defaultFont.setFontName("Arial Unicode");
        defaultFont.setColor(IndexedColors.BLACK.getIndex());
        defaultFont.setBold(false);
        defaultFont.setItalic(false);

        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Arial Unicode");
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        font.setItalic(false);

        XSSFRow row = worksheet.createRow((short) 0)
        XSSFCell cell

        if (params.alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            metadata.columns1 << [title: Constants.importConfigSheet.UNSCHEDULE_LOG, width: 25]
        }
        XSSFColor color = new XSSFColor(new java.awt.Color(0, 113, 156));
        metadata.columns1.eachWithIndex { it, i ->
            cell = row.createCell((short) i)
            worksheet.setColumnWidth(i, 256 * (it.width as Integer))
            cell.setCellValue(it.title as String)
            XSSFCellStyle style = workbook.createCellStyle()

            style.setAlignment(HorizontalAlignment.CENTER);
            style.setFont(font);
            style.setFillForegroundColor(color)
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setWrapText(true)
            cell.setCellStyle(style)
        }

        data.resultList.eachWithIndex { dataRow, j ->
            row = worksheet.createRow((short) 1 + j)
            dataRow.eachWithIndex { it, i ->
                cell = row.createCell((short) i)
                cell.setCellValue(sanitize(it as String))
            }
        }
        if(params.alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            XSSFSheet worksheet2 = workbook.createSheet(metadata?.sheetName2 ?: "Sheet2");

            XSSFRow row2 = worksheet2.createRow((short) 0)
            XSSFCell cell2

            metadata.columns2.eachWithIndex { it, i ->
                cell2 = row2.createCell((short) i)
                worksheet2.setColumnWidth(i, 256 * (it.width as Integer))
                cell2.setCellValue(it.title as String)
                XSSFCellStyle style = workbook.createCellStyle()

                style.setAlignment(HorizontalAlignment.CENTER);
                style.setFont(font);
                style.setFillForegroundColor(color)
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                style.setWrapText(true)
                cell2.setCellStyle(style)
            }

            data.masterList.eachWithIndex { dataRow, j ->
                row2 = worksheet2.createRow((short) 1 + j)
                dataRow.eachWithIndex { it, i ->
                    cell2 = row2.createCell((short) i)
                    cell2.setCellValue(sanitize(it as String))
                }
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream()
        workbook.write(out)
        out.toByteArray()
    }

    String sanitize(String cellValue){
        if(cellValue?.size() > Constants.ExcelConstants.MAX_CELL_LENGTH_XLSX){
            cellValue = cellValue.replace(cellValue.substring(Constants.ExcelConstants.MAX_CELL_LENGTH_XLSX - Constants.ExcelConstants.TRUNCATE_TEXT_XLSX.size()), Constants.ExcelConstants.TRUNCATE_TEXT_XLSX)
        }
        return cellValue
    }

    byte[] createLogFileForImportConfiguration(Map data, Map metadata, Map metaDataForUpdatedRecords, User importedBy, String importConfType){
        List createdRecords = data.get('configurationsCreated')
        List updatedRecords = data.get('configurationsUpdated')
        int totalRecordsProcessed = createdRecords.size()
        int indexForRecordsStatus = importConfType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT ? Constants.IMPORT_AGG_MESSAGE_INDEX : Constants.IMPORT_ICR_MESSAGE_INDEX
        int successfullyImported = Collections.frequency(createdRecords*.getAt(indexForRecordsStatus), 'Success')
        int FailedImports = Collections.frequency(createdRecords*.getAt(indexForRecordsStatus), 'Fail')
        log.info("Successfully Imported: ${successfullyImported}  records, Failed : ${FailedImports} records, Out of Total: ${totalRecordsProcessed} records.")

        XSSFWorkbook workbook = new XSSFWorkbook()

        // preparing criteria sheet
        XSSFSheet criteriaWorksheet = workbook.createSheet("Criteria")
        writeImage(workbook, criteriaWorksheet)
        writeCriteriaSheet(workbook, criteriaWorksheet, importedBy, null, true, false) //Filter not required PVS-53068

        // preparing created configurations sheet
        XSSFSheet worksheet = workbook.createSheet(metadata?.sheetName ?: "Data")

        XSSFFont defaultFont = workbook.createFont()
        defaultFont.setFontHeightInPoints((short) 10)
        defaultFont.setFontName("Arial")
        defaultFont.setColor(IndexedColors.BLACK.getIndex())
        defaultFont.setBold(false)
        defaultFont.setItalic(false)

        XSSFFont font = workbook.createFont()
        font.setFontHeightInPoints((short) 10)
        font.setFontName("Arial")
        font.setColor(IndexedColors.WHITE.getIndex())
        font.setBold(true)
        font.setItalic(false)

        XSSFRow row1 = worksheet.createRow((short) 0)
        XSSFCell cellA2 = row1.createCell((short) 0)
        XSSFCell cellB2 = row1.createCell((short) 1)
        cellA2.setCellValue("Total number of records")
        cellB2.setCellValue(totalRecordsProcessed)

        XSSFRow row2 = worksheet.createRow((short) 1)
        XSSFCell cellA3 = row2.createCell((short) 0)
        XSSFCell cellB3 = row2.createCell((short) 1)
        cellA3.setCellValue("Total number of records imported")
        cellB3.setCellValue(successfullyImported)

        XSSFRow row3 = worksheet.createRow((short) 2)
        XSSFCell cellA4 = row3.createCell((short) 0)
        XSSFCell cellB4 = row3.createCell((short) 1)
        cellA4.setCellValue("Total number of records failed")
        cellB4.setCellValue(FailedImports)

        XSSFRow row = worksheet.createRow((short) 3)
        row = worksheet.createRow((short) 4)
        XSSFCell cell

        if (importConfType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            metadata.columns.add(Constants.IMPORT_AGG_UNSCHEDULE_INDEX, [title: Constants.importConfigSheet.UNSCHEDULE_LOG, width: 25])
        }

        XSSFColor color = new XSSFColor(new byte[]{0, 113, 156}, null)
        metadata.columns.eachWithIndex { it, i ->
            cell = row.createCell((short) i)
            worksheet.setColumnWidth(i, 256 * (it.width as int))
            cell.setCellValue(it.title as String)
            XSSFCellStyle style = workbook.createCellStyle()

            style.setAlignment(HorizontalAlignment.CENTER)
            style.setFont(font)
            style.setFillForegroundColor(color)
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND)
            style.setWrapText(true)
            cell.setCellStyle(style)
        }

        createdRecords.eachWithIndex { dataRow, j ->
            row = worksheet.createRow((short) 5 + j)
            dataRow.eachWithIndex { it, i ->
                cell = row.createCell((short) i)
                cell.setCellValue(sanitize(it as String))
            }
        }

        // preparing updated configurations sheet
        if (updatedRecords) {
            log.info("Successfully updated ${updatedRecords.size()} child records.")
            XSSFSheet updatedRecordsSheet = workbook.createSheet(metaDataForUpdatedRecords?.sheetName ?: "Data")
            XSSFRow firstRow = updatedRecordsSheet.createRow((short) 0)
            XSSFCell firstCell = firstRow.createCell((short) 0)
            XSSFCell secondCell = firstRow.createCell((short) 1)
            firstCell.setCellValue("Total number of processed records")
            secondCell.setCellValue(updatedRecords.size())
            XSSFRow newRow1 = updatedRecordsSheet.createRow((short) 1)
            newRow1 = updatedRecordsSheet.createRow((short) 2)
            XSSFCell newCell1
            metaDataForUpdatedRecords.columns.eachWithIndex { it, i ->
                newCell1 = newRow1.createCell((short) i)
                updatedRecordsSheet.setColumnWidth(i, 256 * (it.width as int))
                newCell1.setCellValue(it.title as String)
                XSSFCellStyle style = workbook.createCellStyle()

                style.setAlignment(HorizontalAlignment.CENTER)
                style.setFont(font)
                style.setFillForegroundColor(color)
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND)
                style.setWrapText(true)
                newCell1.setCellStyle(style)
            }
            updatedRecords.eachWithIndex { dataRow, j ->
                newRow1 = updatedRecordsSheet.createRow((short) 3 + j)
                dataRow.eachWithIndex { it, i ->
                    cell = newRow1.createCell((short) i)
                    cell.setCellValue(sanitize(it as String))
                }
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream()
        workbook.write(out)
        out.toByteArray()
    }

    public int getSwapVirtualizerMaxSize() {
        grailsApplication.config.dynamicJasper.swapVirtualizerMaxSize
    }

    byte[] createExtractFileForAuditLog(def data, def metadata,def user){
        log.info("Started Excel preparation in createExtractFileForAuditLog")
        XSSFWorkbook workbook = new XSSFWorkbook()
        XSSFSheet criteriaWorksheet = workbook.createSheet("Criteria")
        writeImage(workbook, criteriaWorksheet)
        writeCriteriaSheet(workbook,criteriaWorksheet, user,metadata)
        XSSFSheet worksheet = workbook.createSheet(metadata?.sheetName ?: "Data");

        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Arial");
        font.setColor(IndexedColors.BLACK.getIndex());
        font.setBold(true);
        font.setItalic(false);

        XSSFCellStyle defaultCellStyle=workbook.createCellStyle();
        defaultCellStyle.setVerticalAlignment(VerticalAlignment.TOP)
        defaultCellStyle.setWrapText(true)
        defaultCellStyle.setBorderBottom(BorderStyle.THIN);
        defaultCellStyle.setBorderTop(BorderStyle.THIN);
        defaultCellStyle.setBorderRight(BorderStyle.THIN);
        defaultCellStyle.setBorderLeft(BorderStyle.THIN);

        XSSFRow row = worksheet.createRow((short) 0)
        row = worksheet.createRow((short) 0)
        XSSFCell cell

        XSSFColor color = new XSSFColor(new java.awt.Color(128, 128, 128));
        metadata.columns.eachWithIndex { it, i ->
            cell = row.createCell((short) i)
            worksheet.setColumnWidth(i, 256 * (it.width as Integer))
            cell.setCellValue(it.title as String)
            XSSFCellStyle style = workbook.createCellStyle()
            style.setAlignment(HorizontalAlignment.LEFT);
            style.setFont(font);
            style.setFillForegroundColor(color)
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setBorderBottom(BorderStyle.THIN);
            style.setBorderTop(BorderStyle.THIN);
            style.setBorderRight(BorderStyle.THIN);
            style.setBorderLeft(BorderStyle.THIN);
            style.setWrapText(true)
            cell.setCellStyle(style)
        }

        def styleRed=getColoredStyle(workbook,new XSSFColor(new java.awt.Color(239, 83, 80)))
        def styleYellow=getColoredStyle(workbook,new XSSFColor(new java.awt.Color(255, 170, 0)))
        def styleGreen=getColoredStyle(workbook,new XSSFColor(new java.awt.Color(0, 177, 157)))

        Map indexMap=[:] // this map is used for identifying merging indexes
        def start=null
        data.eachWithIndex { dataRow, j ->
            row = worksheet.createRow((short) 1 + j)
            dataRow.eachWithIndex { it, i ->
                cell = row.createCell((short) i)
                cell.setCellValue(sanitize(it as String))
                cell.setCellStyle(defaultCellStyle);
                if(i==0 && it in ["Record Created","Record Exported","Login success"]){
                    cell.setCellStyle(styleGreen);
                }else if(i==0 && it in ["Record Updated","Record Deleted"]){
                    cell.setCellStyle(styleYellow);
                }else if(i==0 && it in ["Login failed"]) {
                    cell.setCellStyle(styleRed);
                }

                if (it == '' && i == 0 && start == null) {
                    start = j
                }
                if (start != null && it != '' && i == 0) {
                    indexMap.put(start, j)
                    start = null
                }
                if (i == 5 && it != '' && it != null) {
                    //Creating Merged row for parent header in section wise modules
                    XSSFCellStyle boldStyle=workbook.createCellStyle();
                    boldStyle.setVerticalAlignment(VerticalAlignment.TOP)
                    boldStyle.setFont(font)
                    cell.setCellStyle(boldStyle)
                    createMergedRow(worksheet,j + 1, j + 1, 5, 8)
                }
            }
        }
        if (start != null) {
            indexMap.put(start, worksheet.getLastRowNum())
        }
        createMergeRegionForTrails(worksheet, indexMap)
        ByteArrayOutputStream out = new ByteArrayOutputStream()
        workbook.write(out)
        out.toByteArray()
    }

    def createMergeRegionForTrails(XSSFSheet worksheet, Map indexMap) {
        //this function will merge same trail data to single cell
        indexMap.each { k, v ->
            for (int i = 0; i < 5; i++) {
                CellRangeAddress cellMerge = new CellRangeAddress(k, v, i, i);
                worksheet.addMergedRegion(cellMerge);
            }
        }
    }

    def createMergedRow(XSSFSheet worksheet, def rowStart, def rowEnd, def colStart, def colEnd) {
        CellRangeAddress cellMerge = new CellRangeAddress(rowStart,rowEnd, colStart, colEnd);
        worksheet.addMergedRegion(cellMerge);
    }
    def getColoredStyle(XSSFWorkbook workbook,def color){
        XSSFColor colorRed = new XSSFColor(new java.awt.Color(239, 83, 80));
        XSSFColor colorYellow = new XSSFColor(new java.awt.Color(255, 170, 0));
        XSSFColor colorGreen = new XSSFColor(new java.awt.Color(0, 177, 157));

        XSSFCellStyle cellStyle=workbook.createCellStyle();
        cellStyle.setFillForegroundColor(color);
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setVerticalAlignment(VerticalAlignment.TOP)
        cellStyle.setWrapText(true)
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return cellStyle
    }


    private static void lockAll(Sheet s, XSSFWorkbook workbookx){
        String password= "abcd";
        byte[] pwdBytes = null;
        try {
            pwdBytes  = Hex.decodeHex(password.toCharArray());
        } catch (DecoderException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        XSSFSheet sheet = ((XSSFSheet)s);
        sheet.lockDeleteColumns(true);
        sheet.lockDeleteRows(true);
        sheet.lockFormatCells(true);
        sheet.lockFormatColumns(true);
        sheet.lockFormatRows(true);
        sheet.lockInsertColumns(true);
        sheet.lockInsertRows(true);
        sheet.getCTWorksheet().getSheetProtection().setPassword(pwdBytes);
        for(byte pwdChar :pwdBytes){
            System.out.println(">>> Sheet protected with '" + pwdChar + "'");
        }
        sheet.enableLocking();
        workbookx.lockStructure();
    }


    public int getBlockSize() {
        grailsApplication.config.dynamicJasper.swapFile.blockSize
    }

    public int getMinGrowCount() {
        grailsApplication.config.dynamicJasper.swapFile.minGrowCount
    }

    byte[] createEmergingIssueReport(data, metadata) {
        XSSFWorkbook workbook = new XSSFWorkbook()
        XSSFSheet criteriaWorksheet = workbook.createSheet("Criteria")
        writeImage(workbook, criteriaWorksheet)
        writeCriteriaSheet(workbook,criteriaWorksheet, userService.getUser())
        XSSFSheet worksheet = workbook.createSheet("Important Events");

        XSSFFont defaultFont = workbook.createFont();
        defaultFont.setFontHeightInPoints((short) 10);
        defaultFont.setFontName("Arial");
        defaultFont.setColor(IndexedColors.BLACK.getIndex());
        defaultFont.setBold(false);
        defaultFont.setItalic(false);

        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setFontName("Arial");
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        font.setItalic(false);

        XSSFRow row1 = worksheet.createRow((short) 0)
        XSSFCell cellA1 = row1.createCell((short) 0)
        User user = userService.user
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.getLongDateFormatForLocale(user?.preference?.locale, false))
        sdf.setTimeZone(TimeZone.getTimeZone(ViewHelper.getTimeZone(user)))
        cellA1.setCellValue((sdf.format(new Date()) + userService.getGmtOffset(user?.preference?.timeZone)))

        XSSFRow row = worksheet.createRow((short) 1)
        row = worksheet.createRow((short) 2)
        XSSFCell cell

        XSSFColor color = new XSSFColor(new java.awt.Color(0, 113, 156));

        metadata.columns.eachWithIndex { it, i ->
            cell = row.createCell((short) i)
            worksheet.setColumnWidth(i, 256 * (it.width as Integer))
            cell.setCellValue(it.title as String)
            XSSFCellStyle style = workbook.createCellStyle()

            style.setAlignment(HorizontalAlignment.CENTER);
            style.setFont(font);
            style.setFillForegroundColor(color)
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            style.setWrapText(true)
            cell.setCellStyle(style)
        }

        data.eachWithIndex { dataRow, j ->
            row = worksheet.createRow((short) 3 + j)
            dataRow.eachWithIndex { it, i ->
                cell = row.createCell((short) i)
                cell.setCellValue(sanitize(it as String))
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream()
        workbook.write(out)
        out.toByteArray()
    }

    List createCriteriaList(User user,
                            def metaData = null, String searchString = null, boolean isImportSheetNote = false, boolean includeFilters = true) {
        String filters = Constants.Commons.NA_LISTED
        if (includeFilters) {
            filters = searchString ?: (metaData ? metaData.filters : Constants.Commons.NA_LISTED)
        }
        String timeZone = user?.preference?.timeZone ?: userService.getCurrentUserPreference().timeZone
        List criteriaSheetList = []
        criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.REPORT_GENERATED_BY, 'value': user?.fullName ?: ""])
        criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.DATE_EXPORTED, 'value': (DateUtil.stringFromDate(new Date(), DateUtil.DATEPICKER_FORMAT_AM_PM, timeZone) + userService.getGmtOffset(timeZone))])
        if (includeFilters) {
            criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.FILTERS, 'value': filters])
        }
        if (isImportSheetNote) {
            criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.IMPORT_SHEET_NOTE, 'value': ''])
        }
        return criteriaSheetList
    }

/**
 * Creates a list of criteria for comments based on the provided parameters.
 *
 * @param user The user for whom the criteria list is being generated.
 * @param metaData Optional metadata object that may contain additional filters.
 * @param searchString Optional search string to filter the comments.
 * @param isImportSheetNote A boolean flag indicating whether an import sheet note should be included.
 * @param alertCommentList A list containing details of alert comments such as alert name, period, product name, and event name.
 *
 * @return A list of maps, each containing a label and value representing a criterion.
 */
    List createCriteriaListForComments(User user, String searchString = null, AlertCommentHistory alertComment, boolean isImportSheetNote = false) {
        String filters = ""
        if(searchString){
            filters = searchString
        } else {
            filters = Constants.Commons.NA_LISTED
        }
        String timeZone = user?.preference?.timeZone ?: userService.getCurrentUserPreference().timeZone
        List criteriaSheetList = []
        criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.ALERT_NAME, 'value': alertComment?.alertName ?: Constants.Commons.BLANK_STRING])
        criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.DATE_RANGE, 'value': alertComment?.period ? alertComment?.period : Constants.Commons.BLANK_STRING])
        criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.PRODUCT, 'value': alertComment?.productName ?: Constants.Commons.BLANK_STRING])
        criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.EVENT, 'value': alertComment?.eventName ?: Constants.Commons.BLANK_STRING])
        criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.REPORT_GENERATED_BY, 'value': user?.fullName ?: Constants.Commons.BLANK_STRING])
        criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.DATE_EXPORTED, 'value': (DateUtil.stringFromDate(new Date(), DateUtil.DATEPICKER_FORMAT_AM_PM, timeZone) + userService.getGmtOffset(timeZone))])
        criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.FILTERS, 'value': filters])
        if (isImportSheetNote) {
            criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.IMPORT_SHEET_NOTE, 'value': Constants.Commons.BLANK_STRING])
        }
        return criteriaSheetList
    }
    List createCriteriaListForAggregateProductEventHistory(User user,String searchString = null,String otherSearchString = null){
        String timeZone = user?.preference?.timeZone ?: userService.getCurrentUserPreference().timeZone
        List criteriaSheetList = []
        criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.REPORT_GENERATED_BY, 'value': user?.fullName ?: ""])
        criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.DATE_EXPORTED, 'value': (DateUtil.stringFromDate(new Date(), DateUtil.DATEPICKER_FORMAT_AM_PM, timeZone) + userService.getGmtOffset(timeZone))])
        criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.REVIEW_SCREEN_FOR_CURRENT_PRODUCT_EVENT_FILTER, 'value': searchString ?:'N/A'])
        criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.REVIEW_HISTORY_FOR_OTHER_ALERT_FILTER, 'value': otherSearchString ?:'N/A'])
        return criteriaSheetList
    }
    List createCriteriaListForSingleCaseProductHistory(User user,String searchString = null,String otherSearchString = null){
        String timeZone = user?.preference?.timeZone ?: userService.getCurrentUserPreference().timeZone
        List criteriaSheetList = []
        criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.REPORT_GENERATED_BY, 'value': user?.fullName ?: ""])
        criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.DATE_EXPORTED, 'value': (DateUtil.stringFromDate(new Date(), DateUtil.DATEPICKER_FORMAT_AM_PM, timeZone) + userService.getGmtOffset(timeZone))])
        criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.REVIEW_HISTORY_FOR_CURRENT_PRODUCT_FILTER, 'value': searchString ?:'N/A'])
        criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.REVIEW_HISTORY_FOR_OTHER_ALERT_FILTER, 'value': otherSearchString ?:'N/A'])
        return criteriaSheetList
    }
    List createCriteriaListForLiteratureAlertProductHistory(User user,String searchString = null,String otherSearchString = null){
        String timeZone = user?.preference?.timeZone ?: userService.getCurrentUserPreference().timeZone
        List criteriaSheetList = []
        criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.REPORT_GENERATED_BY, 'value': user?.fullName ?: ""])
        criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.DATE_EXPORTED, 'value': (DateUtil.stringFromDate(new Date(), DateUtil.DATEPICKER_FORMAT_AM_PM, timeZone) + userService.getGmtOffset(timeZone))])
        criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.REVIEW_SCREEN_FOR_CURRENT_PRODUCT_EVENT_FILTER, 'value': searchString ?:'N/A'])
        criteriaSheetList.add(['label': Constants.CriteriaSheetLabels.REVIEW_HISTORY_FOR_CURRENT_ARTICLE_FILTER, 'value': otherSearchString ?:'N/A'])
        return criteriaSheetList
    }

    File createEtlBatchReport(JRDataSource dataSource, Map params) {
        def name = params.name
        def startTime = System.currentTimeMillis()
        File reportFile = getReportFile(name, params.outputFormat)
        EtlBatchReportBuilder etlBatchReportBuilder = new EtlBatchReportBuilder()
        JasperConcatenatedReportBuilder mainReport = concatenatedReport()
        params.criteriaSheetList = createCriteriaList(userService.getUser())
        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()
        etlBatchReportBuilder.createReport(dataSource, params, jasperReportBuilderEntryList)

        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[]);

        OutputBuilder outputBuilder = new OutputBuilder()
        reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")

        return reportFile
    }

    def createTestDataReport(JRDataSource dataSource, Map params){

        def name = Constants.DynamicReports.AGG_CASE_ALERT
        def startTime = System.currentTimeMillis()
        File reportFile = getReportFile(name, params.outputFormat)
        TestSignalReportBuilder testSignalReportBuilder = new TestSignalReportBuilder()
        JasperConcatenatedReportBuilder mainReport = concatenatedReport()
        List<JasperReportBuilderEntry> jasperReportBuilderEntryList = new ArrayList<JasperReportBuilderEntry>()
        testSignalReportBuilder.createReport(dataSource, params, jasperReportBuilderEntryList)
        List<JasperReportBuilder> jasperReportBuilderList = jasperReportBuilderEntryList*.jasperReportBuilder
        mainReport.concatenate(jasperReportBuilderList.toArray() as JasperReportBuilder[]);
        OutputBuilder outputBuilder = new OutputBuilder()
        reportFile = outputBuilder.produceReportOutput(params, name, mainReport, jasperReportBuilderEntryList)
        reportFile.deleteOnExit()
        def reportTime = System.currentTimeMillis() - startTime
        log.info("Report: ${reportFile.absolutePath} took ${reportTime}ms to be created by Jasper Reports")
        return reportFile
    }

}
