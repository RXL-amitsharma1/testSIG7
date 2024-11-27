package com.rxlogix.jobs

import com.rxlogix.config.AlertDeletionData
import com.rxlogix.config.AlertDeletionDataService
import com.rxlogix.config.PvsAppConfiguration
import grails.util.Holders

class AlertDeleteExecutorJob {

    def concurrent = false
    AlertDeletionDataService alertDeletionDataService

    static triggers = {
        simple startDelay: 60000l, repeatInterval: 20000l // execute job once in 20 seconds after the delay of 5 mins.
    }

    def execute() {
        String serverHostName = "hostname".execute().text.trim()
        PvsAppConfiguration serverHostnameConfig = PvsAppConfiguration.findByKey('serverHostname')
        if (Holders.config.signal.boot.status == true && serverHostName == serverHostnameConfig.stringValue) {
            ArrayList<Long> currentlyRunningIds = alertDeletionDataService.currentlyRunningIds
            alertDeletionDataService.executeAlertDeletion(currentlyRunningIds)
        }
    }
}
