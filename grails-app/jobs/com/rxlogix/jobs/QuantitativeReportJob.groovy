package com.rxlogix.jobs

import com.rxlogix.Constants
import com.rxlogix.config.AlertType
import grails.util.Holders

class QuantitativeReportJob {
    // http://quartz-scheduler.org/documentation/quartz-2.2.x/configuration/ConfigThreadPool

    def reportExecutorService
    def concurrent = true
    def alertService
    static triggers = {
        simple startDelay: 300000l, repeatInterval: 20000l // execute job once in 20s seconds
    }

    // http://stackoverflow.com/questions/6163514/suggestions-for-simple-ways-to-do-asynchronous-processing-in-grails
    // http://quartz-scheduler.org/documentation/quartz-2.1.x/configuration/ConfigThreadPool
    def execute() {
        if (Holders.config.signal.boot.status == true) {
            int threadPoolSize = Holders.config.quartz.props.threadPool.quant.threadCount ?: 10
            int executionQueueSize = reportExecutorService.getQuantExecutionSize()
            if (executionQueueSize && Holders.config.show.alert.execution.queue.size) {
                log.debug("Quant -: Current Execution Queue Size = ${executionQueueSize}, Total Execution Queue Size = ${threadPoolSize}")
            }

            if (reportExecutorService.getQuantExecutionSize() < threadPoolSize && reportExecutorService.checkProductGroupUpdateStatus()) {
                try {
                    reportExecutorService.runConfigurationsIntegratedReview(Constants.AlertConfigType.AGGREGATE_CASE_ALERT, Constants.DataSource.PVA, reportExecutorService.currentlyQuantRunning,threadPoolSize)
                } catch (Throwable tr) {
                    log.info("Exception occured in alert execution job")
                    log.error(tr.getMessage(), tr)
                }
            } else if (Holders.config.show.alert.execution.queue.size) {
                log.info("Quant -: Current report queue exceeds max size, skipping adding new alerts")
            }
        }
    }
}
