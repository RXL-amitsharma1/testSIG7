package com.rxlogix.jobs

import grails.util.Holders

class ClearCompletedAppAlertProgressStatusJob {

    def reportExecutorService
    def concurrent = true
    def alertService
    static triggers = {
        simple startDelay: 480000l, repeatInterval: 86400000l // execute job once in 1 day after delay of 8 min
    }

    def execute() {
        if (Holders.config.signal.boot.status == true) {
                try {
                    reportExecutorService.clearCompletedAlertProgressData()
                } catch (Throwable tr) {
                    log.info("Exception occured in clear completed app alert progress status execution job")
                    log.error(tr.getMessage(), tr)
                }
        }
    }
}
