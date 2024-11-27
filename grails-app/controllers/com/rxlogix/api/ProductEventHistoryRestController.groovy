package com.rxlogix.api

import com.rxlogix.Constants
import com.rxlogix.ExceptionHandlingController
import com.rxlogix.config.Configuration
import com.rxlogix.security.Authorize
import com.rxlogix.signal.ProductEventHistory
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.FileNameCleaner
import com.rxlogix.util.RestApiResponse
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.springframework.security.core.context.SecurityContextHolder

@Authorize
class ProductEventHistoryRestController implements AlertUtil, ExceptionHandlingController {

    def userService
    def productEventHistoryService
    def signalAuditLogService
    def dynamicReportService

    static allowedMethods = [listProductEventHistory: "GET"]

    String listProductEventHistory(String productName, String eventName, Long configId, Long executedConfigId) {
        Map responseMap = [:]
            List productEventHistoryList = productEventHistoryService.getAlertHistoryList(productName, eventName, configId, executedConfigId,true)
            RestApiResponse.successResponseWithData(responseMap,null,productEventHistoryList)
        render(responseMap as JSON)
    }

    String listOtherAlertHistory(String productName, String eventName, Long configId) {
        Map responseMap = [:]
        List productEventHistoryList = productEventHistoryService.getAlertHistoryList(productName, eventName, configId, null,true)
        RestApiResponse.successResponseWithData(responseMap, null, productEventHistoryList)
        render(responseMap as JSON)
    }

    def exportHistoryReport(String productName, String eventName, Long configId, Long executedConfigId, String outputFormat, String searchField, String otherSearchString, String alertHistoryIds, String otherAlertHistoryIds) {
        Map dataMap = [
                outputFormat     : outputFormat,
                productName      : productName,
                configId         : configId,
                executedConfigId : executedConfigId,
                searchField      : searchField,
                otherSearchString: otherSearchString
        ]

        List productEventHistoryList = productEventHistoryService.getCurrentAlertProductEventHistoryList(productName, eventName, configId, executedConfigId, alertHistoryIds)
                .collect { ProductEventHistory productEventHistory -> (productEventHistoryService.getProductEventHistoryMap(productEventHistory)) }
        List otherProductEventHistoryList = productEventHistoryService.getOtherAlertsProductEventHistoryList(productName, eventName, configId, otherAlertHistoryIds)
                .collect { ProductEventHistory productEventHistory -> (productEventHistoryService.getProductEventHistoryMap(productEventHistory)) }
        File reportFile = dynamicReportService.createProductEventHistoryReport(productEventHistoryList, otherProductEventHistoryList, dataMap)
        renderReportOutputType(reportFile, dataMap)
        Configuration configuration = Configuration.get(configId as Long)
        String auditEntityValue = configuration?.getInstanceIdentifierForAuditLog() + ": " + productName + "-" + eventName + ": History"
        signalAuditLogService.createAuditForExport(null, auditEntityValue, Constants.AuditLog.AGGREGATE_REVIEW, dataMap, reportFile.name)
    }

    private renderReportOutputType(File reportFile, def params) {
        String reportName = "Product Event History" + DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss").print(new DateTime())
        params.reportName = reportName
        response.contentType = "${dynamicReportService.getContentType(params.outputFormat)}; charset=UTF-8"
        response.contentLength = reportFile.size()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.$params.outputFormat" + "\"")
        response.getOutputStream().write(reportFile.bytes)
        response.outputStream.flush()
    }

}

