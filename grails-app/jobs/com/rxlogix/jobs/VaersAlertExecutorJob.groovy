package com.rxlogix.jobs

import com.rxlogix.Constants
import grails.util.Holders

class VaersAlertExecutorJob {

    // http://quartz-scheduler.org/documentation/quartz-2.2.x/configuration/ConfigThreadPool

    def reportExecutorService
    def concurrent = true

    static triggers = {
        simple repeatInterval: 10000l // execute job once in 10s seconds
    }

    // http://stackoverflow.com/questions/6163514/suggestions-for-simple-ways-to-do-asynchronous-processing-in-grails
    // http://quartz-scheduler.org/documentation/quartz-2.1.x/configuration/ConfigThreadPool
    def execute() {
        if (Holders.config.signal.boot.status == true) {
            int threadPoolSize = Holders.config.quartz.props.threadPool.vaers.threadCount ?: 10
            int vaersExecutionQueueSize = reportExecutorService.getVaersExecutionQueueSize()
            if (vaersExecutionQueueSize && Holders.config.show.alert.execution.queue.size) {
                log.debug("VAERS -: Current Execution Queue Size = ${vaersExecutionQueueSize}, Total Execution Queue Size = ${threadPoolSize}")
            }

            if (reportExecutorService.getVaersExecutionQueueSize() < threadPoolSize) {
                try {
                    reportExecutorService.runConfigurationsIntegratedReview(Constants.AlertConfigType.AGGREGATE_CASE_ALERT, Constants.DataSource.VAERS, reportExecutorService.currentlyVaersRunning, threadPoolSize)

                } catch (Throwable tr) {
                    log.info("Exception occured in vaers alert execution job")
                    log.error(tr.getMessage(), tr)
                }
            } else if (Holders.config.show.alert.execution.queue.size) {
                log.info("VAERS -: Current report queue exceeds max size, skipping adding new alerts")
            }
        }
    }
}
