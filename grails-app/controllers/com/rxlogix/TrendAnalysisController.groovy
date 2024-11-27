package com.rxlogix

import com.rxlogix.config.EvdasAlert
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.ValidatedSignal
import grails.plugin.springsecurity.annotation.Secured

@Secured(["isAuthenticated()"])
class TrendAnalysisController {

    def trendAnalysisService
    def productEventHistoryService

    def index() {}

    def showTrendAnalysis(Boolean isFaers) {
        [id: params.id, type: params.type, isFaers: isFaers]
    }

    def getTrendData(Boolean isFaers) {
        def alertId = params.alertId
        def alertType = params.alertType
        def chartType = params.chartType
        def frequency = params.frequency
        def trendData = trendAnalysisService.getTrendData(alertId, alertType, chartType, frequency, isFaers)
        render(contentType: 'text/csv', text: trendData)
    }

    def alertList() {
        def type = params.type
        def result = []
        if (type == Constants.AlertConfigType.VALIDATED_SIGNAL) {
            def signal = ValidatedSignal.get(Long.parseLong(params.id))
            result = signal.aggregateAlerts
            result = result + signal.evdasAlerts
        } else if (type == Constants.AlertConfigType.EVDAS_ALERT) {
            result = [EvdasAlert.get(Long.parseLong(params.id))]
        } else if (type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            result = [AggregateCaseAlert.get(Long.parseLong(params.id))]
        }
        def alertList = result.collect {
            it.toDto()
        }
        respond alertList, [formats: ['json']]
    }
}
