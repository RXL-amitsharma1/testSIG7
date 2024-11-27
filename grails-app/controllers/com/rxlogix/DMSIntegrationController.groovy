package com.rxlogix

import com.rxlogix.signal.SignalReport
import com.rxlogix.signal.ValidatedSignal
import grails.core.GrailsApplication
import grails.plugin.springsecurity.annotation.Secured

@Secured(["isAuthenticated()"])
class DMSIntegrationController {

    GrailsApplication grailsApplication
    def dmsService
    def validatedSignalService
    def dynamicReportService

    def sendToDms(String docTypeValue) {
        log.info("params: ${params}")
        params.outputFormat = params.dmsConfiguration.format
        String reportType = params.dmsConfiguration.format
        File reportFile
        switch (docTypeValue) {
            case Constants.DMSDocTypes.SIGNAL_SUMMARY_REPORT:
                reportFile = validatedSignalService.generateSignalSummaryReport(params)
                break
            case Constants.DMSDocTypes.SIGNAL_ASSESSMENT_REPORT:
                ValidatedSignal validatedSignal = ValidatedSignal.get(params.long('signalId'))
                Map assessmentData = validatedSignalService.generateAssessmentDetailsMap(params,validatedSignal)
                reportFile = dynamicReportService.createAssessmentReport(assessmentData, params)
                break
            case Constants.DMSDocTypes.PBRER_SIGNAL_SUMMARY_REPORT:
                reportFile = validatedSignalService.generateSignalReports(params)
                break
            case Constants.DMSDocTypes.SIGNAL_ACTION_DETAIL_REPORT:
                reportFile = validatedSignalService.exportSignalActionDetailReport(params)
                break
            case Constants.DMSDocTypes.GENERATED_REPORT:
                def reportId = params.reportId
                def report = SignalReport.get(reportId)
                def reportBytes = null
                if (Constants.SignalReportOutputType.PDF == reportType) {
                    reportBytes = report.pdfReport
                } else if (Constants.SignalReportOutputType.DOCX == reportType) {
                    reportBytes = report.wordReport
                } else if (Constants.SignalReportOutputType.XLSX == reportType) {
                    reportBytes = report.excelReport
                }
                reportFile = File.createTempFile("Generated Report", ".${reportType}", null)
                FileOutputStream fos = new FileOutputStream(reportFile);
                fos.write(reportBytes);
                break
            default:
                reportFile = validatedSignalService.generateSignalSummaryReport(params)
                break
        }
        String subfolder = params.dmsConfiguration.folder
        String name = params.dmsConfiguration.name ? params.dmsConfiguration.name : reportFile.name
        String description = params.dmsConfiguration.description
        String tag = params.dmsConfiguration.tag ?: params.dmsConfiguration.tags?.name?.join((","))
        String sensitivity = ""
        String author = "tushar"
        dmsService.upload(reportFile, subfolder, name, description, tag, sensitivity, author)
        redirect(url: request.getHeader('referer'))
        //redirect to the page from where the request comes: to dashboard, to report/index page or periodicReport/reports.
    }

}
