package com.rxlogix

import com.rxlogix.config.Configuration
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.signal.ProductEventHistory
import com.rxlogix.util.FileNameCleaner
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

@Secured(["isAuthenticated()"])
class ProductEventHistoryController {

    def productEventHistoryService
    def dynamicReportService
    def CRUDService
    def signalAuditLogService

    def index() { }

    def listProductEventHistory(String productName, String eventName, Long configId, Long executedConfigId) {
        respond productEventHistoryService.getAlertHistoryList(productName, eventName, configId, executedConfigId), [formats: ['json']]
    }

    def exportReport(String productName, String eventName, Long configId){
        String searchField = params.searchField ?: null
        String otherSearchString = params.otherSearchString ?: null
        def otherAlertConfigIds = params?.otherAlertConfigIds
        def alertConfigIds =  params?.alertConfigIds
        List otherCaseAlertsHistoryList = []
        List caseAlertHistoryList = []
        if(searchField != "undefined" && searchField  && alertConfigIds != "" ) {
            String[] ids = alertConfigIds.split(',')
            List searchCaseAlertHistoryList = productEventHistoryService.getCurrentAlertProductEventHistoryList(productName, eventName, configId)
            if (ids) {
                ids.each { it ->
                    caseAlertHistoryList = searchCaseAlertHistoryList.findAll { it2 -> it2.id == it as Long }.collect { ProductEventHistory productEventHistory -> (productEventHistoryService.getProductEventHistoryMap(productEventHistory)) }
                }
            }
        } else if(searchField != "undefined" && searchField ){
            caseAlertHistoryList = []
        } else{
            caseAlertHistoryList = productEventHistoryService.getCurrentAlertProductEventHistoryList(productName, eventName, configId)
                    .collect { ProductEventHistory productEventHistory -> (productEventHistoryService.getProductEventHistoryMap(productEventHistory)) }
        }
        if(otherSearchString != "undefined" && otherSearchString  && otherAlertConfigIds != "" ) {
            String[] ids = otherAlertConfigIds.split(',')
            List searchOtherCaseAlertHistoryList = productEventHistoryService.getOtherAlertsProductEventHistoryList(productName, eventName, configId)
            if (ids) {
                ids.each { it ->
                    otherCaseAlertsHistoryList = searchOtherCaseAlertHistoryList.findAll { it2 -> it2.id == it as Long }.collect { ProductEventHistory productEventHistory -> (productEventHistoryService.getProductEventHistoryMap(productEventHistory)) }
                }
            }
        } else if(otherSearchString != "undefined" && otherSearchString){
            otherCaseAlertsHistoryList = []
        } else{
           otherCaseAlertsHistoryList = productEventHistoryService.getOtherAlertsProductEventHistoryList(productName, eventName, configId)
                    .collect { ProductEventHistory productEventHistory -> (productEventHistoryService.getProductEventHistoryMap(productEventHistory)) }
        }
        File reportFile = dynamicReportService.createProductEventHistoryReport(caseAlertHistoryList, otherCaseAlertsHistoryList, params)
        renderReportOutputType(reportFile,params)
        Configuration configuration=Configuration.get(configId as Long)
        String auditEntityValue=configuration?.getInstanceIdentifierForAuditLog()+": "+productName+"-"+eventName+": History"
        signalAuditLogService.createAuditForExport(params?.containsKey('criteriaSheetList') ? params.criteriaSheetList : null,auditEntityValue,Constants.AuditLog.AGGREGATE_REVIEW,params,reportFile.name)
    }

    private renderReportOutputType(File reportFile,def params) {
        String reportName = "Product Event History" + DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss").print(new DateTime())
        params.reportName=reportName
        response.contentType = "${dynamicReportService.getContentType(params.outputFormat)}; charset=UTF-8"
        response.contentLength = reportFile.size()
        response.setCharacterEncoding("UTF-8")
        response.setHeader ("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.$params.outputFormat" + "\"")
        response.getOutputStream().write(reportFile.bytes)
        response.outputStream.flush()
    }

    def updateJustification(Long id, String newJustification) {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            ProductEventHistory productEventHistory = ProductEventHistory.get(id)
            if (productEventHistory.justification?.trim() != newJustification?.trim()) {
                String oldJustification = productEventHistory.justification
                productEventHistory.justification = newJustification?.trim()
                CRUDService.saveWithoutAuditLog(productEventHistory)
                productEventHistoryService.createActivityForJustificationChange(productEventHistory, oldJustification)
            }
        } catch (Exception e) {
            e.printStackTrace()
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.caseHistory.justification.change.error")
        }
        render(responseDTO as JSON)
    }

}
