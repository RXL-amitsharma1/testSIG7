package com.rxlogix.jobs

import grails.util.Holders

class OnDemandAlertDeletionJob {
    def onDemandAlertDeletionService
    static triggers = {
        simple repeatInterval: Holders.config.signal.delete.ondemand.alert.job.interval, startDelay: 900000l
    }

    def execute() {
        if (Holders.config.signal.boot.status == true) {
            onDemandAlertDeletionService.startDeletingOnDemandAlert()
        }
    }
}
