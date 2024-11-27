package com.rxlogix.jobs

import com.rxlogix.Constants
import com.rxlogix.config.AlertType
import grails.util.Holders

class ScheduledReportJob {
    // http://quartz-scheduler.org/documentation/quartz-2.2.x/configuration/ConfigThreadPool

    def reportExecutorService
    def concurrent = true
    def alertService

    static triggers = {
        simple startDelay: 300000l, repeatInterval: Holders.config.signal.qualitative.report.job.interval // execute job once in 20s seconds
    }

    // http://stackoverflow.com/questions/6163514/suggestions-for-simple-ways-to-do-asynchronous-processing-in-grails
    // http://quartz-scheduler.org/documentation/quartz-2.1.x/configuration/ConfigThreadPool
    def execute() {
        if (Holders.config.signal.boot.status == true) {
            int threadPoolSize = Holders.config.quartz.props.threadPool.qual.threadCount ?: 10
            int executionQueueSize = reportExecutorService.getExecutionQueueSize()
            if (executionQueueSize && Holders.config.show.alert.execution.queue.size) {
                log.debug("Qualitative -: Current Execution Queue Size = ${executionQueueSize}, Total Execution Queue Size = ${threadPoolSize}")
            }

            if (reportExecutorService.getExecutionQueueSize() < threadPoolSize) {
                try {
                    reportExecutorService.runConfigurations(Constants.AlertConfigType.SINGLE_CASE_ALERT, reportExecutorService.currentlyRunning,threadPoolSize)
                } catch (Throwable tr) {
                    log.info("Exception occured in alert execution job")
                    log.error(tr.getMessage(), tr)
                }
            } else if (Holders.config.show.alert.execution.queue.size) {
                log.info("Qualitative -: Current report queue exceeds max size, skipping adding new alerts")
            }
        }
    }
}
