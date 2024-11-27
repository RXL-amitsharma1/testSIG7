package com.rxlogix.jobs

import com.rxlogix.CRUDService
import com.rxlogix.config.LiteratureConfiguration
import grails.util.Holders

class LiteratureAlertJob {

    def literatureExecutionService

    def concurrent = true

    static triggers = {
        simple repeatInterval: 10000l // execute job once in 10s seconds
    }

    // http://stackoverflow.com/questions/6163514/suggestions-for-simple-ways-to-do-asynchronous-processing-in-grails
    // http://quartz-scheduler.org/documentation/quartz-2.1.x/configuration/ConfigThreadPool
    def execute() {
        if (Holders.config.signal.boot.status == true) {
            int threadPoolSize = Holders.config.quartz.props.threadPool.literature.threadCount ?: 10
            int executionQueueSize = literatureExecutionService.getExecutionQueueSize()
            if (executionQueueSize && Holders.config.show.alert.execution.queue.size) {
                log.debug("Literature -: Current Execution Queue Size = ${executionQueueSize}, Total Execution Queue Size = ${threadPoolSize}")
            }

            if (literatureExecutionService.getExecutionQueueSize() < threadPoolSize) {
                try {
                    literatureExecutionService.runConfigurations()
                } catch (Throwable tr) {
                    tr.printStackTrace()
                    log.info("Exception in Job")
                    log.error(tr.getMessage(), tr)
                }
            } else if (Holders.config.show.alert.execution.queue.size) {
                log.info("Literature -: Current queue for Literature Alert exceeds max size, Not adding any more alerts to queue")
            }
        }
    }
}
