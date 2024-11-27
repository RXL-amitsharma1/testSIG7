package com.rxlogix.api

import com.rxlogix.Constants
import com.rxlogix.ExceptionHandlingController
import com.rxlogix.config.CommentTemplate
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.security.Authorize
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.AlertComment
import com.rxlogix.signal.AlertCommentHistory
import com.rxlogix.signal.ArchivedAggregateCaseAlert
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.FileNameCleaner
import com.rxlogix.util.RestApiResponse
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import com.rxlogix.util.DateUtil


@Authorize
class AlertCommentRestController implements AlertUtil, ExceptionHandlingController {

    def userService
    def alertCommentService
    def dynamicReportService
    def signalAuditLogService

    static allowedMethods = [commentTemplateList: "GET",saveAlertComment:"POST",updateAlertComment:"POST",deleteAlertComment:"POST",deleteBulkComments:"POST",getBulkCheck:"POST"]

    String commentTemplateList() {
        Map responseMap = [:]
            List commentTemplateList = CommentTemplate.list().collect { it.toDto() }.sort({ it.name.toUpperCase()})
            Map dataMap = ["template":commentTemplateList]
            RestApiResponse.successResponseWithData(responseMap,null,dataMap)
        render(responseMap as JSON)
    }

    String listAlertComments(String alertType, String eventName, Long productId, Long executedConfigId, Long configId) {
        Map responseMap = [:]
            Map paramsMap = [
                    alertType       : alertType,
                    eventName       : eventName,
                    productId       : productId,
                    executedConfigId: executedConfigId,
                    configId        : configId
            ]
            if (alertType && productId && configId && executedConfigId && eventName) {
                Map commentInfo = alertCommentService.getComment(paramsMap)
                RestApiResponse.successResponseWithData(responseMap, null, commentInfo)
            } else {
                RestApiResponse.invalidParametersResponse(responseMap)
                render(status: HttpStatus.INTERNAL_SERVER_ERROR.value(),text:responseMap as JSON)
                return
            }
        render(responseMap as JSON)
    }

    String saveAlertComment() {
        Map responseMap = [:]
        String jsonContent = request.JSON
        log.info("json content received is := " + jsonContent)
        Map resultMap = alertCommentService.createAlertComment(["caseJsonObjArray": jsonContent])
        if (resultMap.isSuccess) {
            String resultMsg = message(code: "app.label.comments.success")
            RestApiResponse.successResponse(responseMap, resultMsg)
        } else {
            String errorMsg = message(code: "app.label.comments.error")
            RestApiResponse.failureResponse(responseMap, errorMsg)
            render(status: HttpStatus.INTERNAL_SERVER_ERROR.value(),text:responseMap as JSON)
            return
        }
        render(responseMap as JSON)
    }

    String updateAlertComment() {
        Map responseMap = [:]
        String jsonContent = request.JSON
        log.info("json content received is := " + jsonContent)
        Map dataMap = jsonContent ? jsonToMap(jsonContent) : null
        Boolean isSuccess = alertCommentService.updateSignalComment(dataMap)
        if (isSuccess) {
            String resultMsg = message(code: "app.label.comments.update")
            RestApiResponse.successResponse(responseMap, resultMsg)
        } else {
            String errorMsg = message(code: "app.label.comments.error")
            RestApiResponse.failureResponse(responseMap, errorMsg)
            render(status: HttpStatus.INTERNAL_SERVER_ERROR.value(),text:responseMap as JSON)
            return
        }
        render(responseMap as JSON)

    }

    String deleteAlertComment() {
        Map responseMap = [:]
        String jsonContent = request.JSON
        log.info("json content received is := " + jsonContent)
        Map dataMap = jsonContent ? jsonToMap(jsonContent) : null
        Long alertId = dataMap.caseId
        AlertComment comment = AlertComment.findByCaseId(alertId)
        Boolean isSuccess = alertCommentService.deleteComment(comment)
        if (isSuccess) {
            alertCommentService.deleteAlertComment(dataMap, comment)
            RestApiResponse.successResponse(responseMap)
        } else {
            String errorMsg = message(code: "app.label.comments.error")
            RestApiResponse.failureResponse(responseMap, errorMsg)
            render(status: HttpStatus.INTERNAL_SERVER_ERROR.value(),text:responseMap as JSON)
            return
        }
        render(responseMap as JSON)

    }

    String deleteBulkComments() {
        Map responseMap = [:]
        String jsonContent = request.JSON
        log.info("json content received is := " + jsonContent)
        Map resultMap = alertCommentService.deleteBulkComments(["caseJsonObjArray": jsonContent])
        if (resultMap.isSuccess) {
            String resultMsg = message(code: "app.label.comments.delete")
            RestApiResponse.successResponse(responseMap, resultMsg)
        } else {
            String errorMsg = message(code: "app.label.comments.error")
            RestApiResponse.failureResponse(responseMap, errorMsg)
            render(status: HttpStatus.INTERNAL_SERVER_ERROR.value(),text:responseMap as JSON)
            return
        }
        render(responseMap as JSON)
    }

    String getBulkCheck() {
        Map responseMap = [:]
        String jsonContent = request.JSON
        log.info("json content received is := " + jsonContent)
        Map dataMap = jsonContent ? jsonToMap(jsonContent) : null
        List alertListObj = dataMap.alertIdList as List
        Boolean bulkUpdateAllowed = alertCommentService.isBulkUpdateAllowed(alertListObj)
        RestApiResponse.successResponseWithData(responseMap, null, ["bulkUpdateAllowed": bulkUpdateAllowed])
        render(responseMap as JSON)
    }


    String createCommentFromTemplate(Long templateId, Long alertId){
        Map responseMap = [:]
        String comment  = alertCommentService.createCommentFromTemplate(templateId, alertId)
        RestApiResponse.successResponseWithData(responseMap,null,comment)
        render(responseMap as JSON)
    }

    def exportCommentHistory(String outputFormat, Boolean isArchived, Long caseId, String searchField) {
        String userTimezone = userService.getCurrentUserPreference()?.timeZone ?: Constants.UTC
        List<AlertCommentHistory> alertCommentList = alertCommentService.fetchExportHistoryData(caseId, searchField)
        Map dataMap = [
                outputFormat: outputFormat,
                isArchived  : isArchived,
                caseId      : caseId,
                searchField : searchField
        ]

        List list = alertCommentList.collect {
            [
                    id         : it.id,
                    aggAlertId : it.aggAlertId,
                    comments   : it.comments,
                    alertName  : it.alertName,
                    period     : it.period,
                    modifiedBy : User.findByUsername(it.modifiedBy)?.fullName,
                    lastUpdated: DateUtil.stringFromDate(it.lastUpdated, DateUtil.DEFAULT_DATE_TIME_FORMAT, userTimezone)
            ]
        }.sort { -it.id }

        File report = dynamicReportService.createCommentHistoryReport(list, dataMap, alertCommentList?.first())
        renderReportOutputType(report, dataMap)

        def domain = isArchived ? ArchivedAggregateCaseAlert.get(caseId) : AggregateCaseAlert.get(caseId)
        signalAuditLogService.createAuditForExport(null, domain.getInstanceIdentifierForAuditLog(), Constants.AuditLog.AGGREGATE_REVIEW + ": Comments", dataMap, report.name)

    }

    private renderReportOutputType(File reportFile,def params) {
        String reportName = "Comment History" + DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss").print(new DateTime())
        response.contentType = "${dynamicReportService.getContentType(params.outputFormat)}; charset=UTF-8"
        response.contentLength = reportFile.size()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.$params.outputFormat" + "\"")
        response.getOutputStream().write(reportFile.bytes)
        response.outputStream.flush()
        params?.reportName = "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.$params.outputFormat"
    }

}
