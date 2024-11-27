package com.rxlogix.ReportTemplateRestService

import com.rxlogix.InboxLog
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.NotificationLevel
import com.rxlogix.config.ReportExecutionStatus
import com.rxlogix.enums.ReportFormatEnum
import com.rxlogix.signal.SignalReport
import com.rxlogix.user.User
import com.rxlogix.util.ViewHelper
import grails.async.PromiseList
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.apache.http.HttpStatus
import org.apache.http.util.TextUtils
import com.rxlogix.helper.NotificationHelper

import static grails.async.Promises.task
import static grails.async.Promises.waitAll

@Transactional
class ReportTemplateRestService {

    def reportIntegrationService
    def userService
    def grailsApplication
    def reportExecutorService
    def notificationHelper

    def exportConfiguration(Long selectedTemplate, String templateName, Long configId, String type, Long alertId, String typeFlag, def params) {
        log.info("Calling exportConfiguration..")
        Map result = [status: false, isDataAvailable: false]
        try {
            ExecutedConfiguration config = ExecutedConfiguration.findById(configId)
            ExecutedConfiguration aggExeConfig
            User user = userService.getUser()
            List<Map> caseData = reportIntegrationService.caseDataForExport(config, type, false, alertId, typeFlag, params,true)
            String caseSeriesDelimiter = Holders.config.caseSeries.bulk.addCase.delimiter ?: ":"
            List<String> caseNumAndVersionList = caseData.collect { it.case_number + caseSeriesDelimiter + it.version }
            if(caseNumAndVersionList && !caseNumAndVersionList?.isEmpty()){
                result.isDataAvailable = true
            }
            //To do
            //log.info("Generate Temporary Case Series: $caseNumAndVersionList")
            if(params.aggExecutionId && !TextUtils.isEmpty(params.aggExecutionId)) {
                aggExeConfig = ExecutedConfiguration.findById(params.aggExecutionId)
            }
            String seriesName =(aggExeConfig?aggExeConfig.name:config.name) + ":::" + System.currentTimeMillis()
            Map response = reportExecutorService.generateCaseSeries(seriesName, caseNumAndVersionList?.join(","), true)
            if (response.result.status) {
                Long seriesId = response.result.data.id as Long
                String linkUrl = "/signal/template/index?configId=" + configId + "&type=" + type + "&typeFlag=" + typeFlag + "&alertId=" + alertId
                String reportName = "${aggExeConfig?aggExeConfig.name:config.name}: ${templateName}"
                SignalReport signalReport = new SignalReport(reportName: reportName, linkUrl: linkUrl, userName: user.username, userId: user.id, type: type,
                        typeFlag: typeFlag, alertId: alertId, executedAlertId: configId, createdBy: user.username, modifiedBy: user.username)
                if(params.aggExecutionId && params.aggExecutionId!=null){
                    signalReport.executedAlertId = params.aggExecutionId as Long
                }
                signalReport.save(flush: true)
                Map res = reportIntegrationService.executeAdhocReport(aggExeConfig?:config, selectedTemplate, seriesId, signalReport, type, params.preferredTerm)
                result.status = res.status
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        }
        return result
    }

    void saveExportedFiles(Long executedConfigurationId, SignalReport signalReport) {
        log.info("Calling saveExportedFiles...")
        String url = Holders.config.pvreports.url
        String path = Holders.config.pvreports.export.adhoc.report.uri

        def pList = new PromiseList()

        log.info("Creating the tasks for report files.")

        [ReportFormatEnum.PDF, ReportFormatEnum.XLSX, ReportFormatEnum.DOCX].each { reportFormat ->
            pList << task {
                try {
                    Map response = reportIntegrationService.get(url, path, [executedConfigurationId: executedConfigurationId, format: reportFormat.name()])
                    //To do
                    //log.info("Exported Report Files API Response from PVR for ${reportFormat?.name()}: " + response)
                    return response
                } catch (ConnectException ce) {
                    signalReport.reportExecutionStatus = ReportExecutionStatus.ERROR
                    log.error(ce.printStackTrace())
                }
            }
        }

        //On complete callback is called when all the tasks are complete.
        pList.onComplete { List result ->
            try {
                result.each { response ->
                    if (response.status == HttpStatus.SC_OK) {
                        if (response.data.status && response.data.data.bytes) {
                            ReportFormatEnum reportFormatEnum = ReportFormatEnum.valueOf(response.data.data.format)
                            switch (reportFormatEnum) {
                                case ReportFormatEnum.PDF:
                                    signalReport.pdfReport = response.data.data.bytes
                                    break;
                                case ReportFormatEnum.DOCX:
                                    signalReport.wordReport = response.data.data.bytes
                                    break;
                                case ReportFormatEnum.XLSX:
                                    signalReport.excelReport = response.data.data.bytes
                            }
                        }
                        if(signalReport.reportExecutionStatus != ReportExecutionStatus.ERROR)
                        {
                            signalReport.reportExecutionStatus = ReportExecutionStatus.COMPLETED
                        }
                        saveSignalReport(signalReport)
                    } else {
                        throw new Exception("Something unexpected happen in PVR" + response.status)
                    }
                }
                saveSignalReportNotification(signalReport)
            } catch (Exception ex) {
                log.error("Error occured while saveExportedFiles in onComplete" + ex.printStackTrace())
                signalReport.reportExecutionStatus = ReportExecutionStatus.ERROR
                saveSignalReport(signalReport)
            }
        }

        pList.onError { errorData ->
            log.error("Error occured while saveExportedFiles in onError" + errorData)
            signalReport.reportExecutionStatus = ReportExecutionStatus.ERROR
            saveSignalReport(signalReport)
            saveSignalReportNotification(signalReport)
        }
    }

    void saveSignalReport(SignalReport signalReport) {
        SignalReport.withTransaction {
            SignalReport.withNewSession {
                signalReport.attach()
                signalReport.save()
            }
            signalReport.save()
        }
    }

    void saveSignalReportNotification(SignalReport signalReport) {
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(signalReport?.executedAlertId)
        String subject = (signalReport.reportExecutionStatus == ReportExecutionStatus.COMPLETED) ? "Report \"${signalReport.reportName}\" associated with \"${executedConfiguration.name}\" is completed." : "Report \"${signalReport.reportName}\" associated with \"${executedConfiguration.name}\" is failed."
        NotificationLevel notificationLevel = (signalReport.reportExecutionStatus == ReportExecutionStatus.COMPLETED) ? NotificationLevel.INFO : NotificationLevel.ERROR
        String typeCode = (signalReport.reportExecutionStatus == ReportExecutionStatus.COMPLETED) ? "app.signal.report.generated" : "app.signal.report.failed"
        String type = ViewHelper.getMessage(typeCode)

        InboxLog signalNotification = new InboxLog()
        signalNotification.createdOn = new Date()
        signalNotification.detailUrl = reportIntegrationService.getExecutedReportUrl(signalReport.reportId)
        signalNotification.messageArgs = signalReport?.reportName
        signalNotification.message = subject
        signalNotification.type = type
        signalNotification.subject = subject
        signalNotification.level = notificationLevel
        signalNotification.isRead = false
        signalNotification.notificationUserId = signalReport.userId
        signalNotification.inboxUserId = signalReport.userId
        signalNotification.content = "<span>${type}</span>"
        signalNotification.save(failOnError: true, flush: true)
        notificationHelper.pushNotification(signalNotification)
    }

    def saveSignalReport(def jsonContent, Long executedAlertId, List linkUrlParts) {
        SignalReport signalReport = new SignalReport()
        signalReport.reportName = jsonContent.reportName
        signalReport.userName = jsonContent.userName
        signalReport.userId = jsonContent.userId
        signalReport.linkUrl = jsonContent.linkUrl
        signalReport.executedAlertId = executedAlertId
        if (!signalReport.isAlertReport && (linkUrlParts[1].split("=").size() > 1)) {
            signalReport.type = linkUrlParts[1].split("=")[1]
            signalReport.typeFlag = linkUrlParts[2].split("=")[1]
            signalReport.alertId = (linkUrlParts[3].split("=")[1]) as Long
        }

        //The reports
        signalReport.reportId = jsonContent.exConfigId
        signalReport.pdfReport = jsonContent.pdfReport
        signalReport.wordReport = jsonContent.wordReport
        signalReport.excelReport = jsonContent.excelReport
        signalReport.dateCreated = new Date()
        signalReport.lastUpdated = new Date()
        signalReport.createdBy = jsonContent.userName
        signalReport.modifiedBy = jsonContent.userName
        signalReport.save(failOnError: true, flush: true)
    }
}
