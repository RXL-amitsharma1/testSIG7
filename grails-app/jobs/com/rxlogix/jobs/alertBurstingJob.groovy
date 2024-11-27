package com.rxlogix.jobs

import com.rxlogix.Constants
import grails.util.Holders

class alertBurstingJob {
    def reportExecutorService
    def concurrent = true
    static triggers = {
        simple startDelay: 300000l, repeatInterval: 20000l // execute job once in 20s seconds
    }

    // http://stackoverflow.com/questions/6163514/suggestions-for-simple-ways-to-do-asynchronous-processing-in-grails
    // http://quartz-scheduler.org/documentation/quartz-2.1.x/configuration/ConfigThreadPool


    /**
    *JOB STOPPED FOR DEVELOPMENT .
     * REMOVE THE && false after development or for testing.
     */
    def execute() {
        if (Holders.config.signal.boot.status == true ) {
            int threadPoolSize = 1

            if (reportExecutorService.getAlertBurstingQueueSize() < threadPoolSize) {
                try {
                    reportExecutorService.runConfigurationsAlertBursting(Constants.AlertConfigType.AGGREGATE_CASE_ALERT, Constants.DataSource.PVA, reportExecutorService.currentlyAlertBursting,threadPoolSize)
                } catch (Throwable tr) {
                    log.info("Exception occured in alert bursting execution job")
                    log.error(tr.getMessage(), tr)
                }
            } else if (Holders.config.show.alert.execution.queue.size) {
                log.info("Quant -: Current report queue exceeds max size, skipping adding new alerts")
            }
        }
    }
}
