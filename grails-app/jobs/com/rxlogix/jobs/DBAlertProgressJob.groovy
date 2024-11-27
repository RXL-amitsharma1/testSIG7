package com.rxlogix.jobs

import grails.util.Holders

class DBAlertProgressJob {
    // http://quartz-scheduler.org/documentation/quartz-2.2.x/configuration/ConfigThreadPool

    def reportExecutorService
    def concurrent = true
    def alertService
    static triggers = {
        simple startDelay: 300000l, repeatInterval: 30000l // execute job once in 30s seconds
    }

    def execute() {
        if (Holders.config.signal.boot.status == true) {
            try {
                    reportExecutorService.fetchUpdateAlertDBProgress()
                } catch (Throwable tr) {
                    log.info("Exception occured in DBAlertProgressJob execution job")
                    log.error(tr.getMessage(), tr)
                }
        }
    }
}
