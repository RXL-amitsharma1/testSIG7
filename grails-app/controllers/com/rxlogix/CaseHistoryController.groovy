package com.rxlogix

import com.rxlogix.config.Configuration
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.signal.CaseHistory
import com.rxlogix.signal.ProductEventHistory
import com.rxlogix.util.FileNameCleaner
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import java.text.SimpleDateFormat

@Secured(["isAuthenticated()"])
class CaseHistoryController {

    def caseHistoryService
    def dynamicReportService
    def CRUDService
    def userService
    def signalAuditLogService

    def listCaseHistory(Long alertConfigId, String caseNumber, Boolean isArchived, Long exeConfigId, String caseVisibility) {
        List<Map> caseHistoryList = []
        String userTimezone = userService.getCurrentUserPreference()?.timeZone ?: Constants.UTC
        caseHistoryService.listCaseHistory(caseNumber, alertConfigId, isArchived, exeConfigId)?.collect {
            caseHistoryList.add(it.toDto(userTimezone, [], caseVisibility))
        }
        respond caseHistoryList, [formats: ['json']]
    }

    def exportReport(Long alertConfigId, String caseNumber, String productFamily, Boolean isArchived, Long exeConfigId , String sorted , String order, String caseVisibility) {
        String searchField = params.searchField ?: null
        String otherSearchString = params.otherSearchString ?: null
        String caseVersion = params.caseVersion
        def otherAlertConfigIds = params?.otherAlertConfigIds
        def alertConfigIds =  params?.alertConfigIds
        List caseHistoryList
        List suspectProductHistoryList = []
        if(searchField != "undefined" && searchField  && alertConfigIds != "" ) {
            String[] ids = alertConfigIds.split(',')
            List searchCaseAlertHistoryList  = caseHistoryService.listCaseHistory(caseNumber, alertConfigId, isArchived, exeConfigId)
            if (ids) {
                ids.each { it ->
                    caseHistoryList = searchCaseAlertHistoryList.findAll { it2 -> it2.id == it as Long }?.collect { CaseHistory ch ->
                        caseHistoryService.getCaseHistoryMap(ch, caseVisibility)
                    }
                }
            }
        } else if(searchField != "undefined" && searchField ){
            caseHistoryList = []
        } else{
            caseHistoryList = caseHistoryService.listCaseHistory(caseNumber, alertConfigId, isArchived, exeConfigId)?.collect { CaseHistory ch ->
                caseHistoryService.getCaseHistoryMap(ch, caseVisibility)
            }
        }

        List columnOrder = Holders.config.alert.case.history.table.columns.order
        caseHistoryList?.sort{hist1 , hist2->
            String sortedColumn = columnOrder[sorted as int]
            if(order == "desc") {
                return  hist2[sortedColumn]?.toString()?.toLowerCase()<=>hist1[sortedColumn]?.toString()?.toLowerCase()
            } else {
                return  hist1[sortedColumn]?.toString()?.toLowerCase()<=>hist2[sortedColumn]?.toString()?.toLowerCase()
            }
        }
        if(otherSearchString != "undefined" && otherSearchString  && otherAlertConfigIds != "" ) {
            String[] ids = otherAlertConfigIds.split(',')
            List searchOtherCaseAlertHistoryList = caseHistoryService.listSuspectCaseHistory(caseNumber, alertConfigId)?.flatten()
            if (ids) {
                ids.each { it ->
                    suspectProductHistoryList = searchOtherCaseAlertHistoryList.findAll { it2 -> it2.id == it as Long }.collect { CaseHistory ch ->
                        caseHistoryService.getCaseHistoryMap(ch, caseVisibility)
                    }
                }
            }
        } else if(otherSearchString != "undefined" && otherSearchString){
            suspectProductHistoryList = []
        } else{
            suspectProductHistoryList = caseHistoryService.listSuspectCaseHistory(caseNumber, alertConfigId)?.flatten()?.collect { CaseHistory ch ->
                caseHistoryService.getCaseHistoryMap(ch, caseVisibility)
            }
        }
        suspectProductHistoryList?.sort{hist1 , hist2->
            String sortedColumn = columnOrder[params.sorted2 as int]
            if(params.order2 == "desc") {
                return  hist2[sortedColumn]?.toString()?.toLowerCase()<=>hist1[sortedColumn]?.toString()?.toLowerCase()
            } else {
                return  hist1[sortedColumn]?.toString()?.toLowerCase()<=>hist2[sortedColumn]?.toString()?.toLowerCase()
            }
        }
        def dateFormat = new SimpleDateFormat('dd-MMM-yyyy hh:mm:ss a')

        suspectProductHistoryList.sort { a, b ->
            def dateA = dateFormat.parse(a.timestamp)
            def dateB = dateFormat.parse(b.timestamp)
            dateB <=> dateA
        }
        suspectProductHistoryList.each { it ->
            it.timestamp = dateFormat.format(dateFormat.parse(it.timestamp))
        }

        caseHistoryList.sort { a, b ->
            def dateA = dateFormat.parse(a.timestamp)
            def dateB = dateFormat.parse(b.timestamp)
            dateB <=> dateA
        }
        caseHistoryList.each { it ->
            it.timestamp = dateFormat.format(dateFormat.parse(it.timestamp))
        }

        File reportFile = dynamicReportService.createAlertHistoryReport(caseHistoryList, suspectProductHistoryList, params)
        renderReportOutputType(reportFile, params)
        Configuration configuration = Configuration.get(alertConfigId as Long)
        signalAuditLogService.createAuditForExport(params?.containsKey('criteriaSheetList') ? params.criteriaSheetList : null, configuration?.getInstanceIdentifierForAuditLog() + ": ${caseNumber}: History", Constants.AuditLog.SINGLE_REVIEW, params, reportFile.name)
    }

    private renderReportOutputType(File reportFile,params) {
        String reportName = "Case History" + DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss").print(new DateTime())
        response.contentType = "${dynamicReportService.getContentType(params.outputFormat)}; charset=UTF-8"
        response.contentLength = reportFile.size()
        response.setCharacterEncoding("UTF-8")
        response.setHeader ("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.$params.outputFormat" + "\"")
        response.getOutputStream().write(reportFile.bytes)
        response.outputStream.flush()
        params?.reportName = "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.$params.outputFormat"
    }

    def listSuspectProdCaseHistory(String caseNumber, Long alertConfigId, String caseVisibility) {
        String userTimezone = userService.getCurrentUserPreference()?.timeZone ?: Constants.UTC
        List configIds = userService.getUserConfigurationsForCurrentUser(Constants.AlertConfigType.SINGLE_CASE_ALERT)
        List caseHistoryList = caseHistoryService.listSuspectCaseHistory(caseNumber, alertConfigId)?.flatten()?.collect { it.toDto(userTimezone, configIds, caseVisibility) } ?: []
        respond caseHistoryList, [formats: ['json']]
    }

    def updateJustification(Long id, String newJustification) {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            CaseHistory caseHistory = CaseHistory.get(id)
            if (caseHistory.justification?.trim() != newJustification?.trim()) {
                String oldJustification = caseHistory.justification
                caseHistory.justification = newJustification?.trim()
                CRUDService.saveWithoutAuditLog(caseHistory)
                caseHistoryService.createActivityForJustificationChange(caseHistory, oldJustification)
            }
        } catch (Exception e) {
            e.printStackTrace()
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.caseHistory.justification.change.error")
        }
        render(responseDTO as JSON)
    }

}
