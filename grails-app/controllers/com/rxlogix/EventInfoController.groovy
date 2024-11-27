package com.rxlogix

import com.rxlogix.util.AlertUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.apache.http.util.TextUtils

@Secured(["isAuthenticated()"])
class EventInfoController implements AlertUtil {

    def eventInfoService
    def dataSource_pva
    def dynamicReportService
    def userService
    def workflowRuleService
    def priorityService
    def validatedSignalService
    def safetyLeadSecurityService
    def alertService

    def eventDetail(Long alertId, String type, Boolean isArchived) {

        Boolean isAlertScreen = false
        if(params.isAlertScreen && params.boolean("isAlertScreen")){
            isAlertScreen = true
        }
        if (!TextUtils.isEmpty(type) && alertId) {
            Map eventDetailMap = eventInfoService.fetchEventDetailMap(alertId, type, isArchived, isAlertScreen)
            render(view: 'eventDetail', model: eventDetailMap)
        } else {
            render view: '/errors/error404'
        }
    }


    def showCharts(Long alertId, String alertType, Boolean isArchived) {

        def alertObj = eventInfoService.getAlertObject(alertType, alertId, isArchived)

        Map countData = [newFatal_pva: [], cummFatal_pva: [], prr_pva: [], ror_pva: [], newCount_pva: [], cummCount_pva: [], xAxisTitle: [], xAxisTitle_ev: [],
                         newEvpm_ev  : [], totalEvpm_ev: [], ime_ev: [], dme_ev: [], newFatal_ev: [], totalFatal_ev: [], newPaed_ev: [], totalPaed_ev: [],
                         sdrPaed_ev  : [], changes_ev: [], rorAll_ev: [], newCounts_faers: [], cummCounts_faers: [], eb05_faers: [], eb95_faers: [], xAxisTitle_faers : []
        ]

        if (alertObj) {
            def configuration = alertObj.alertConfiguration
            String productName = alertObj.productName
            String event = alertObj.pt
            String dataSource = (alertType == Constants.AlertConfigType.EVDAS_ALERT) ? Constants.DataSource.EUDRA : configuration.selectedDatasource

            Map resultMap = eventInfoService.getStandardAlertsList(productName)
            resultMap[dataSource] = configuration.name

            resultMap.each { key, value ->
                key.split(',').each { it ->
                    if (it.equals(Constants.DataSource.EUDRA)) {
                        if (dataSource.split(',').size() == 1) {
                            countData = eventInfoService.getDetailsMapForEvdas(countData, value, productName, event)
                        } else {
                            countData = eventInfoService.getDetailsMapForEvdasIntegratedReview(countData, value, productName, event)
                        }
                    } else if (it.equals(Constants.DataSource.FAERS)) {
                        countData = eventInfoService.getDetailsMapForFaers(countData, value, productName, event, dataSource)
                    } else {
                        countData = eventInfoService.getDetailsMapForPVA(countData, value, productName, event)
                    }
                }
            }
        }

        render(countData as JSON)
    }


    def listPreviousCounts(Long alertId, String alertType, Boolean isArchived) {

        Map finalMap = [aaData: []]
        try {
            List resultMapList = eventInfoService.countMapData(alertId, alertType, isArchived)
            finalMap = [aaData: resultMapList]
        } catch (Throwable th) {
            th.printStackTrace()
        }

        render(finalMap as JSON)
    }

    def listHistoryOfOtherSources(String alertType, Long alertId, Boolean isArchived) {
        List otherAlertsHistory = eventInfoService.fetchOtherSourceHistory(alertType, alertId, isArchived)
        render([aaData: otherAlertsHistory] as JSON)
    }
}
