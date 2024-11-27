package com.rxlogix

import com.rxlogix.config.Configuration
import com.rxlogix.config.LiteratureConfiguration
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.signal.CaseHistory
import com.rxlogix.signal.LiteratureHistory
import com.rxlogix.util.FileNameCleaner
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

@Secured(["isAuthenticated()"])
class LiteratureHistoryController {

    def literatureHistoryService
    def signalAuditLogService
    DynamicReportService dynamicReportService

    def listCaseHistory(Long articleId, Long litConfigId) {
        List history = literatureHistoryService.listLiteratureHistory(articleId, litConfigId)
        render(history as JSON)
    }

    def listArticleHistoryInOtherAlerts(Long articleId, Long litConfigId) {
        List history = literatureHistoryService.getArticleHistoriesInOtherAlerts(articleId, litConfigId)
        render(history as JSON)
    }

    def exportReport(Long articleId, Long litConfigId) {
        String searchField = params.searchField ?: null
        String otherSearchString = params.otherSearchString ?: null
        def otherAlertConfigIds = params?.otherAlertConfigIds
        def alertConfigIds = params?.alertConfigIds
        List<Map> caseHistoryList
        List suspectProductHistoryList
        if(searchField != "undefined" && searchField  && alertConfigIds != ""){
            String[] ids = alertConfigIds.split(',')
            List searchCaseAlertHistoryList = literatureHistoryService.getLiteratureHistory(articleId, litConfigId)
            if (ids) {
                ids.each { it ->
                    caseHistoryList = searchCaseAlertHistoryList.findAll { it2 -> it2.id == it as Long }?.collect { LiteratureHistory history ->
                        literatureHistoryService.getHistoryMap(history)
                    }
                }
            }
        }else if(searchField != "undefined" && searchField){
            caseHistoryList = []
        }else{
            caseHistoryList = literatureHistoryService.getLiteratureHistory(articleId, litConfigId).collect { LiteratureHistory history ->
                literatureHistoryService.getHistoryMap(history)
            }
        }
        if(otherSearchString != "undefined" && otherSearchString  && otherAlertConfigIds != "" ){
            String[] ids = otherAlertConfigIds.split(',')
            List searchOtherCaseAlertHistoryList = literatureHistoryService.getLiteratureHistoryForOtherAlerts(articleId, litConfigId)
            if (ids) {
                ids.each { it ->
                    suspectProductHistoryList = searchOtherCaseAlertHistoryList.findAll { it2 -> it2.id == it as Long }.collect { LiteratureHistory history ->
                        literatureHistoryService.getHistoryMap(history)
                    }
                }
            }
        } else if(otherSearchString != "undefined" && otherSearchString){
            suspectProductHistoryList = []
        } else{
            suspectProductHistoryList = literatureHistoryService.getLiteratureHistoryForOtherAlerts(articleId, litConfigId).collect { LiteratureHistory history ->
                literatureHistoryService.getHistoryMap(history)
            }
        }
        File reportFile = dynamicReportService.createAlertHistoryReport(caseHistoryList, suspectProductHistoryList, params)
        renderReportOutputType(reportFile,params)
        LiteratureConfiguration configuration = LiteratureConfiguration.get(litConfigId as Long)
        signalAuditLogService.createAuditForExport(params?.containsKey('criteriaSheetList') ? params.criteriaSheetList : null, configuration.getInstanceIdentifierForAuditLog() + ": ${articleId}: History", Constants.AuditLog.LITERATURE_REVIEW, params, reportFile.name)
    }

    private renderReportOutputType(File reportFile,params) {
        String reportName = "Literature History" + DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss").print(new DateTime())
        params.reportName = reportName
        response.contentType = "${dynamicReportService.getContentType(params.outputFormat)}; charset=UTF-8"
        response.contentLength = reportFile.size()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.$params.outputFormat" + "\"")
        response.getOutputStream().write(reportFile.bytes)
        response.outputStream.flush()
    }

    def updateJustification(Long id, String newJustification) {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            literatureHistoryService.updateJustification(id, newJustification)
        } catch (Exception e) {
            log.error(e.getMessage(), e)
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.caseHistory.justification.change.error")
        }
        render(responseDTO as JSON)
    }

}