package com.rxlogix.jobs

import grails.util.Holders

class EVDASAlertJob {

    def evdasAlertExecutionService

    def concurrent = true

    static triggers = {
        simple repeatInterval: 10000l // execute job once in 10s seconds
    }

    // http://stackoverflow.com/questions/6163514/suggestions-for-simple-ways-to-do-asynchronous-processing-in-grails
    // http://quartz-scheduler.org/documentation/quartz-2.1.x/configuration/ConfigThreadPool
    def execute() {
        if (Holders.config.signal.boot.status == true) {
            int threadPoolSize = Holders.config.quartz.props.threadPool.evdas.threadCount ?: 10
            int executionQueueSize = evdasAlertExecutionService.getExecutionQueueSize()
            if (executionQueueSize && Holders.config.show.alert.execution.queue.size) {
                log.debug("EVDAS -: Current Execution Queue Size = ${executionQueueSize}, Total Execution Queue Size = ${threadPoolSize}")
            }

            if (evdasAlertExecutionService.getExecutionQueueSize() < threadPoolSize) {
                try {
                    evdasAlertExecutionService.runConfigurations()
                } catch (Throwable tr) {
                    tr.printStackTrace()
                    log.info("Exception in Job")
                    log.error(tr.getMessage(), tr)
                }
            } else if (Holders.config.show.alert.execution.queue.size) {
                log.info("EVDAS -: Current report queue exceeds max size, skipping adding new reports")
            }
        }
    }
}
