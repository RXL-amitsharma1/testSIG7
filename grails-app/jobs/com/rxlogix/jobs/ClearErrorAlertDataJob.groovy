package com.rxlogix.jobs

import grails.util.Holders

class ClearErrorAlertDataJob {

    def reportExecutorService
    def concurrent = true
    def alertService
    static triggers = {
        simple startDelay: 600000l, repeatInterval: 3600000l // execute job once in 1 hr after delay of 10 mints
    }

    def execute() {
        if (Holders.config.signal.boot.status == true) {
                try {
                    reportExecutorService.clearErrorAlertData()
                } catch (Throwable tr) {
                    log.info("Exception occured in alert execution job")
                    log.error(tr.getMessage(), tr)
                }
        }
    }
}
