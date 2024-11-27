package com.rxlogix.api

import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.NotificationLevel
import com.rxlogix.signal.SignalReport
import grails.converters.JSON
import grails.rest.RestfulController
import groovy.json.JsonSlurper

import static org.apache.http.HttpStatus.SC_BAD_REQUEST
import static org.apache.http.HttpStatus.SC_OK

class ReportTemplateRestController extends RestfulController {

    //The call the super constructor is required as it takes the entity
    //In the below super call the ExecutedConfiguration means nothing and added to make it work.
    ReportTemplateRestController() {
        super(ExecutedConfiguration, true);
    }

    def springSecurityService
    def templateService
    def userService
    def reportIntegrationService
    def reportTemplateRestService

    def index() {
        Map data = [:]
        data.username = userService.getUser().username
        data.offset = params.start
        data.max = params.length
        data.searchString = params."search[value]"
        data.columnName = params."columns[${params."order[0][column]"}][data]"
        data.dir = params."order[0][dir]"
        Map res = reportIntegrationService.getTemplateListForAdHocreport(data)
        render res as JSON
    }

    def runReport(Long selectedTemplate, String templateName, Long configId, Long alertId, String type, String typeFlag) {
        Map result = reportTemplateRestService.exportConfiguration(selectedTemplate, templateName, configId, type, alertId, typeFlag, params)
        if (result.status && result.isDataAvailable ) {
            flash.message = message(code: "report.builder.ok")
        } else if(!result.isDataAvailable){
            flash.error = message(code: "configuration.noDataAvailable")
        } else{
            flash.error = message(code: "configuration.import.failed")
        }


        redirect controller: "template", params: [configId: configId, type: type, alertId: alertId, typeFlag: typeFlag, isAggScreen:params.isAggScreen, selectedCases: params.selectedCases, productName: params.productName, preferredTerm: params.preferredTerm,eventName: params.eventName, filterList: params.filterList,
                                                  filters: params.filters, isFilterRequest: params.isFilterRequest,advancedFilterId: params.advancedFilterId, aggExecutionId: params.aggExecutionId, version: params.version]
    }

    def getTemplateList() {
        respond(templateService.getTemplateList().collect() {
            [id: it.id, text: it.name + " (" + it.description + ")"]
        }, [formats: ['json']])
    }

    def saveSignalReport() {
        def jsonContent = request.JSON
        if (!jsonContent) throw new Throwable("Empty JSON content")
        Long signalReportId
        jsonContent = jsonToMap(jsonContent.toString())
        Boolean isReportGenerated = jsonContent.isReportGenerated.asBoolean()
        String subject = ""
        Long assignedToId
        ExecutedConfiguration executedConfiguration
        try {
            String linkUrl = jsonContent.linkUrl
            List linkUrlParts = linkUrl.split("\\?")[1]?.split("&")
            Long executedAlertId = (linkUrlParts[0].split("=")[1]) as Long
            executedConfiguration = ExecutedConfiguration.findById(executedAlertId)
            assignedToId = executedConfiguration.assignedTo?.id
            if (!isReportGenerated) {
                subject = "Report \"${jsonContent.reportName}\" associated with \"${executedConfiguration.name}\" is failed."
                reportIntegrationService.saveSignalReportNotification(jsonContent.linkUrl, jsonContent.reportName, assignedToId, NotificationLevel.ERROR, message(code: "app.signal.report.failed"), subject)
            } else {
                SignalReport signalReport = reportTemplateRestService.saveSignalReport(jsonContent, executedAlertId, linkUrlParts)
                signalReportId = signalReport.id
                subject = "Report \"${signalReport.reportName}\" associated with \"${executedConfiguration.name}\" is completed."
                reportIntegrationService.saveSignalReportNotification(reportIntegrationService.getExecutedReportUrl(signalReport.reportId), jsonContent.reportName, assignedToId, NotificationLevel.INFO, message(code: "app.signal.report.generated"), subject)
            }
            log.info("Notification created.")
        } catch (Throwable th) {
            subject = "Report \"${jsonContent.reportName}\" associated with \"${executedConfiguration.name}\" is failed"
            reportIntegrationService.saveSignalReportNotification(jsonContent.linkUrl, jsonContent.reportName, assignedToId, NotificationLevel.ERROR, message(code: "app.signal.report.failed"), subject)
            log.error(th.printStackTrace())
            response.status = SC_BAD_REQUEST
            respond([id: null])
        }
        response.status = SC_OK
        respond([id: signalReportId])
    }

    private def jsonToMap(String jsonContent) {
        JsonSlurper jsonSlurper = new JsonSlurper();
        def json = jsonSlurper.parseText(jsonContent);
        Map jsonResult = (Map) json
        jsonResult.get("reportBuilder")
    }
}