package com.rxlogix.jobs

import com.rxlogix.Constants
import grails.util.Holders

class JaderAlertExecutorJob {

    // http://quartz-scheduler.org/documentation/quartz-2.2.x/configuration/ConfigThreadPool

    def reportExecutorService
    def jaderExecutorService
    def concurrent = true

    static triggers = {
        simple repeatInterval: 10000l // execute job once in 10s seconds
    }

    // http://stackoverflow.com/questions/6163514/suggestions-for-simple-ways-to-do-asynchronous-processing-in-grails
    // http://quartz-scheduler.org/documentation/quartz-2.1.x/configuration/ConfigThreadPool
    def execute() {
        if (Holders.config.signal.boot.status == true) {
            int threadPoolSize = Holders.config.quartz.props.threadPool.jader.threadCount ?: 10
            int jaderExecutionQueueSize = jaderExecutorService.getJaderExecutionQueueSize()
            if (jaderExecutionQueueSize && Holders.config.show.alert.execution.queue.size) {
                log.debug("JADER -: Current Execution Queue Size = ${jaderExecutionQueueSize}, Total Execution Queue Size = ${threadPoolSize}")
            }

            if (jaderExecutorService.getJaderExecutionQueueSize() < threadPoolSize) {
                try {
                    reportExecutorService.runConfigurationsIntegratedReview(Constants.AlertConfigType.AGGREGATE_CASE_ALERT, Constants.DataSource.JADER, jaderExecutorService.currentlyJaderRunning, threadPoolSize)

                } catch (Throwable tr) {
                    log.info("Exception occured in jader alert execution job")
                    log.error(tr.getMessage(), tr)
                }
            } else if (Holders.config.show.alert.execution.queue.size) {
                log.info("JADER -: Current report queue exceeds max size, skipping adding new alerts")
            }
        }
    }
}
