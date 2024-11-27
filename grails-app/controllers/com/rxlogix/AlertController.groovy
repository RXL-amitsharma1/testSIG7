package com.rxlogix

import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.Alert
import com.rxlogix.signal.SingleCaseAlert
import grails.plugin.springsecurity.annotation.Secured

@Secured(["isAuthenticated()"])
class AlertController {

    def dueIn(Long alertId) {

        def appName = params.appName

        def result = null

        if (Constants.AlertConfigType.SINGLE_CASE_ALERT == appName) {
            result = SingleCaseAlert.findById(alertId)?.dueIn()
        } else if (Constants.AlertConfigType.AGGREGATE_CASE_ALERT == appName) {
            result = AggregateCaseAlert.findById(alertId)?.dueIn()
        } else {
            result =  Alert.findById(alertId)?.dueIn()
        }

        render(contentType: 'text/json') {[
                'result': result,
                'status': result ? "OK" : "Nothing present"
        ]}
    }
}
