package com.rxlogix.reactorService

import com.rxlogix.Constants
import com.rxlogix.EmailNotificationService
import com.rxlogix.PvsGlobalTagService
import com.rxlogix.ViewInstanceService
import com.rxlogix.config.ExecutedConfiguration
import grails.events.annotation.Subscriber

class ConsumerService {

    def alertService
    def reportService
    PvsGlobalTagService pvsGlobalTagService
    def pvsAlertTagService
    def singleCaseAlertService
    def aggregateCaseAlertService
    def userService
    def dispositionService
    def productAssignmentService
    ViewInstanceService viewInstanceService
    def undoableDispositionService
    EmailNotificationService emailNotificationService

    @Subscriber("activity.product.event.history.published")
    def persistActivityAndPEHistory(Map peHistoryActivityMap){
        alertService.persistActivityAndPEHistory(peHistoryActivityMap)
    }

    @Subscriber("activity.case.history.event.published")
    def persistActivityAndCaseHistories(Map activityAndHistoryMap){
        alertService.persistActivityAndCaseHistories(activityAndHistoryMap)
    }

    @Subscriber("evdas.activity.history.event.published")
    def persistActivityAndEVDASHistories(Map activityAndHistoryMap){
        alertService.persistActivityAndEvdasHistory(activityAndHistoryMap)
    }

    @Subscriber("literature.activity.event.published")
    def persistActivity(Map activityAndHistoryMap){
        alertService.persistLiteratureActivityAndHistories(activityAndHistoryMap)
    }

    @Subscriber("literature.send.email.batch.published")
    def sendEmailNotifications(Map emailMap){
        emailNotificationService.sendLiteratureEmailList(emailMap)
    }

    @Subscriber("generate.memo.report.event.published")
    def generateMemoReportInBackground(Map memoReportMap){
        reportService.generateMemoReport(memoReportMap)
    }

    @Subscriber("activity.add.signal.event.published")
    def addActivityForSignal(Map activityAndSignalMap){
        alertService.persistActivitesForSignal(activityAndSignalMap.signalId, activityAndSignalMap.signalActivityList)
    }

    @Subscriber("propagate.categories.versions.published")
    def propagateCategoriesHigherVersions(Map caseIdVersionNumMap) {
        pvsGlobalTagService.refreshGlobalTagsForCaseId(caseIdVersionNumMap.caseId, caseIdVersionNumMap.caseVersion)
    }

    @Subscriber("categories.populate.version.published")
    def populateAlertCategoriesVersion(Map categoriesMap) {
        pvsAlertTagService.populateAlertTagsForVersions(categoriesMap.categories,categoriesMap.execConfigId)
    }

    @Subscriber("update.user.group.counts")
    def updateUserGroupCountsInBackground(Map userGroupMap) {
        userService.updateUserGroupCounts(userGroupMap.userInstance, userGroupMap.prevGroupIdList)
    }

    @Subscriber("update.user.group.counts.workflow.group")
    def updateUserGroupCountsForWorkflowGroupInBackground(Map userGroupMap) {
        userService.updateUserGroupCountsForWorkflowGroup(userGroupMap.userInstance)
    }

    @Subscriber("remove.disp.counts")
    def removeDispCountsInBackground(Map userDispMap) {
        dispositionService.removeDispCountsFromDashboardCounts(userDispMap.userDashboardCountsList, userDispMap.dispositionId)
    }

    @Subscriber("case.drilldown.data.prepared")
    def generateCaseSeriesData(Map caseInfoMap) {
        List<Map> caseInfo = aggregateCaseAlertService.caseDrillDown(caseInfoMap.type, caseInfoMap.typeFlag, caseInfoMap.aggExecutionId, caseInfoMap.productId, caseInfoMap.ptCode, caseInfoMap.selectedDataSource, caseInfoMap.groupBySmq, caseInfoMap.aggAlert, caseInfoMap.isEventGroup, null)
        if (caseInfo) {
            List<Map> remainingCaseInfo = caseInfo - caseInfoMap.prevData
            ExecutedConfiguration.withNewSession {
                singleCaseAlertService.createAlert(caseInfoMap.configId, caseInfoMap.exeConfigId, remainingCaseInfo, Constants.Commons.DASH_STRING, false, true, caseInfoMap.type)
            }
        }
        alertService.addTaskCompletionNotification(caseInfoMap.exeConfigId, caseInfoMap.currentUser,caseInfoMap)
    }

    @Subscriber("update.user.view.order")
    def updateUserViewOrder(Map userViewOrderMap) {
        viewInstanceService.saveOrUpdateUserViewOrder(userViewOrderMap.updatedUserViewOrderList)
    }

    @Subscriber("populate.unassigned.products")
    def populateUnassignedProductsInBackground(Map map) {
        productAssignmentService.populateUnassignedProducts(map.hierarchy)
    }

    @Subscriber("update.product.group")
    def updateProductGroup(Map productGroupDetails){
        productAssignmentService.updatePGSelection(productGroupDetails)
    }
    @Subscriber("push.disposition.changes")
    def pushDispositionChanges(Map map) {
        undoableDispositionService.persistUndoableDisposition(map)
    }

    @Subscriber("sync.alert.config.to.mart")
    def syncAlertConfigToMart(Map data) {
        aggregateCaseAlertService.syncConfigToMartCacheConfig(data)
    }
    @Subscriber("update.disp.count")
    def updateDispCount(Map data) {
        aggregateCaseAlertService.updateDispCountsForExConfigAndGroupedConfig(data)
    }
}
