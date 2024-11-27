package com.rxlogix.api

import com.rxlogix.Constants
import com.rxlogix.config.Activity
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.security.Authorize
import com.rxlogix.signal.AdHocAlert
import com.rxlogix.signal.GroupedAlertInfo
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.FileNameCleaner
import com.rxlogix.util.RestApiResponse
import com.rxlogix.util.SignalQueryHelper
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import org.hibernate.Session

import java.text.SimpleDateFormat

@Authorize
class ActivityRestController implements AlertUtil {

    def activityService
    def sessionFactory
    def signalAuditLogService
    def userService
    def dynamicReportService

    String exportActivitiesReport() {
        Map resp = [:]
        def acaList
        def exeAlert = null
        List criteriaSheetList = []
        Session session = sessionFactory.currentSession
        String currentUserName = userService.getUser()?.username
        def auditEntityValue = ""
        def auditEntityModule = ""

        try {
            if (params.callingScreen == Constants.Commons.DASHBOARD) {
                acaList = handleDashboardCases(currentUserName)
                params.criteriaSheetList = dynamicReportService.createCriteriaList(userService.getUser())
            } else {
                log.info("1: " + params.alertType + ", 2: " + params.executedConfigId)
                Set <Long> execConfigIdList
                if(params.isAlertBursting){
                    exeAlert = GroupedAlertInfo.get(params.executedConfigId)
                    execConfigIdList = exeAlert.execConfigList*.id
                } else {
                    exeAlert = handleExecutedAlert(params.executedConfigId as Long)
                    if(exeAlert){
                        execConfigIdList = [exeAlert.id]
                    }
                }
                if (!execConfigIdList) {
                    resp.message = "Invalid executed config ID or type"
                    RestApiResponse.invalidParametersResponse(resp, "Invalid config ID or type: ${params.executedConfigId}")
                    render(resp as JSON)
                    return
                }
                acaList = activityService.listActivitiesByExConfig(execConfigIdList, params.alertType as String)
            }

            def dateFormat = new SimpleDateFormat('dd-MMM-yyyy hh:mm:ss a')
            sortAcaList(acaList, dateFormat)
            formatDates(acaList, dateFormat)

            def reportFile = dynamicReportService.createActiviesReport(
                    new JRMapCollectionDataSource(acaList), params, exeAlert?.name ?: params.alertType)
            renderReportOutputType(reportFile, params)
            signalAuditLogService.createAuditForExport(criteriaSheetList, auditEntityValue + " : Activities", auditEntityModule, params, reportFile.name)
        } catch (Exception e) {
            log.error("Error occurred while exporting activities report: ", e)
            RestApiResponse.failureResponse(resp, "Error occurred while exporting activities report")
            render(resp as JSON)
        } finally {
            session.flush()
            session.clear()
        }
    }

    def handleDashboardCases(String currentUserName) {
        def acaList = null
        if (params.appType == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
            auditEntityValue = Constants.AuditLog.SINGLE_REVIEW_DASHBOARD
            auditEntityModule = Constants.AuditLog.SINGLE_REVIEW_DASHBOARD
            acaList = Activity.findAllByCaseNumberIsNotNullAndAssignedTo(userService.getUser()).findAll {
                !it.privateUserName || (it.privateUserName && it.privateUserName == currentUserName)
            }.collect { it.toDto() }
        } else if (params.appType in [Constants.AlertConfigType.AGGREGATE_CASE_ALERT, Constants.AlertConfigType.EVDAS_ALERT]) {
            if (params.appType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
                auditEntityValue = Constants.AuditLog.AGGREGATE_REVIEW_DASHBOARD
                auditEntityModule = Constants.AuditLog.AGGREGATE_REVIEW_DASHBOARD
            } else if (params.appType == Constants.AlertConfigType.EVDAS_ALERT) {
                auditEntityValue = Constants.AuditLog.EVDAS_REVIEW_DASHBOARD
                auditEntityModule = Constants.AuditLog.EVDAS_REVIEW_DASHBOARD
            }
            String searchAlertQuery = (params.appType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
                    ? (SignalQueryHelper.agg_activity_from_dashboard(userService.getUser().id))
                    : (SignalQueryHelper.evdas_activity_from_dashboard(userService.getUser().id))
            List<Activity> activities = getResultList(Activity.class, searchAlertQuery)
            acaList = activities.findAll {
                !it.privateUserName || (it.privateUserName && it.privateUserName == currentUserName)
            }.collect { it.toDto() }
        }
        return acaList
    }

    def handleAdHocAlert(Integer alertId) {
        return AdHocAlert.findById(alertId)
    }

    def processAdHocActivities(AdHocAlert alert) {
        def acaList = Activity.findAllByAlert(alert).collect {
            it.toDto()
        }
        acaList.each {
            if (!it['performedByDept'].isEmpty()) {
                it['performedByDept'] = it['performedByDept'].toString()
                        .replace(Constants.ActivityExportRegex.OPEN_SQUARE_BRACKET, Constants.ActivityExportRegex.OPEN_CURLY_BRACKET)
                        .replace(Constants.ActivityExportRegex.CLOSED_SQUARE_BRACKET, Constants.ActivityExportRegex.CLOSED_CURLY_BRACKET)
            }
            it['type'] = activityService.breakActivityType(it['type'] as String)
        }
        return acaList
    }

    def createCriteriaSheet(AdHocAlert alert, String productName) {
        String timeZone = userService.getCurrentUserPreference()?.timeZone
        productName = alert.productSelection ? ViewHelper.getDictionaryValues(alert, DictionaryTypeEnum.PRODUCT)
                : ViewHelper.getDictionaryValues(alert, DictionaryTypeEnum.PRODUCT_GROUP)
        if (!productName) {
            productName = params.productName
        }
        if (!alert.productSelection) {
            params.productName = productName
        }
        auditEntityValue = alert.name
        auditEntityModule = Constants.AuditLog.ADHOC_REVIEW

        return [
                ['label': Constants.CriteriaSheetLabels.ALERT_NAME, 'value': alert?.name],
                ['label': Constants.CriteriaSheetLabels.DESCRIPTION, 'value': alert?.description ?: Constants.Commons.BLANK_STRING],
                ['label': Constants.CriteriaSheetLabels.PRODUCT, 'value': productName],
                ['label': Constants.CriteriaSheetLabels.EVENT_SELECTION, 'value': alert.eventSelection
                        ? getNameFieldFromJson(alert.eventSelection)
                        : (getGroupNameFieldFromJson(alert.eventGroupSelection) ?: Constants.Commons.BLANK_STRING)],
                ['label': Constants.CriteriaSheetLabels.DISPOSITIONS, 'value': alert?.disposition?.displayName ?: Constants.Commons.BLANK_STRING],
                ['label': Constants.CriteriaSheetLabels.CREATED_DATE, 'value':
                        DateUtil.stringFromDate(alert?.dateCreated, DateUtil.DATEPICKER_FORMAT_AM_PM, timeZone)],
                ['label': Constants.CriteriaSheetLabels.REPORT_GENERATED_BY, 'value': userService.getUser().fullName ?: ""],
                ['label': Constants.CriteriaSheetLabels.DATE_EXPORTED, 'value':
                        (DateUtil.stringFromDate(new Date(), DateUtil.DATEPICKER_FORMAT_AM_PM, timeZone) + userService.getGmtOffset(timeZone))]
        ]
    }

    def handleExecutedAlert(Long exConfigId) {
        return ExecutedConfiguration.findById(exConfigId)
    }

    def sortAcaList(def acaList, def dateFormat) {
        acaList.sort { a, b ->
            dateFormat.parse(b.timestamp) <=> dateFormat.parse(a.timestamp)
        }
    }

    def formatDates(def acaList, def dateFormat) {
        acaList.each {
            it.timestamp = dateFormat.format(dateFormat.parse(it.timestamp))
            it.performedBy = it.performedByDept ? "${it.performedBy} (${it.performedByDept})" : it.performedBy
            it.currentAssignment = it.currentAssignmentDept ? "${it.currentAssignment} (${it.currentAssignmentDept})" : it.currentAssignment
            it.details = it.justification ? "${it.details} -- with Justification '${it.justification}'" : it.details
        }
    }

    private renderReportOutputType(File reportFile, def params) {
        response.contentType = "${dynamicReportService.getContentType(params.outputFormat)}; charset=UTF-8"
        response.contentLength = reportFile.size()
        response.setCharacterEncoding("UTF-8")
        response.setHeader ("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportFile.name), "UTF-8")}" + "\"")
        response.outputStream << reportFile.bytes
        response.outputStream.flush()
        params?.reportName = URLEncoder.encode(FileNameCleaner.cleanFileName(reportFile.name), "UTF-8")
    }

    def listByExeConfig(Long executedConfigId, String alertType, Boolean isAlertBursting) {
        Map responseMap = [:]
        try {
            Set executedIds = []
            if(isAlertBursting){
                executedIds = GroupedAlertInfo.get(executedConfigId).execConfigList as Set
            } else {
                executedIds = [executedConfigId] as Set
            }
            List acaList = []
            if (executedIds) {
                acaList = activityService.listActivitiesByExConfig(executedIds, alertType as String)
                RestApiResponse.successResponseWithData(responseMap, null, acaList)
            } else {
                RestApiResponse.recordAbsentResponse(responseMap)
            }
        } catch (Exception e) {
            log.error("Error occurred while fetching activities : ", e)
            e.printStackTrace()
            RestApiResponse.serverErrorResponse(responseMap)
        }
        render(responseMap as JSON)
    }
}
