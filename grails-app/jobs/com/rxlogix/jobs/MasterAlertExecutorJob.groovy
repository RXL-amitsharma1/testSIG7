package com.rxlogix.jobs

import com.rxlogix.Constants
import com.rxlogix.config.PvsAppConfiguration
import grails.util.Holders

class MasterAlertExecutorJob {

    def masterExecutorService
    def concurrent = false
    def cacheService
    def childExecutorService

    static triggers = {
        simple repeatInterval: 10000l // execute job once in 10s seconds
    }

    def execute() {
        String serverHostName = "hostname".execute().text.trim()
        PvsAppConfiguration serverHostnameConfig = PvsAppConfiguration.findByKey('serverHostname')
        if (Holders.config.signal.boot.status == true && serverHostName == serverHostnameConfig.stringValue) {
            int threadPoolSize = Holders.config.quartz.props.threadPool.master.threadCount ?: 1
            int masterExecutionQueueSize = masterExecutorService.manageMasterExecutionQueue(false, true, false)
            if (masterExecutionQueueSize < threadPoolSize &&
                    childExecutorService.manageMasterChildExecutionQueue(false, true, false) < 1 &&
                    childExecutorService.manageRunningChildExecutionQueue(false, true, false) < 1) {
                try {
                    if (!cacheService.masterUploadRunningStatus()) {
                        masterExecutorService.runConfigurations(Constants.AlertConfigType.AGGREGATE_CASE_ALERT, masterExecutorService.manageMasterExecutionQueue(true, false, false))
                    } else
                        log.info("MASTER -: Configuration Upload is in progress")

                } catch (Throwable tr) {
                    log.error("Exception occurred in master alert execution job.", tr)
                    tr.printStackTrace()
                } finally {
                    masterExecutorService.manageMasterExecutionQueue()
                }
            } else if (Holders.config.show.alert.execution.queue.size) {
                log.info("MASTER -: Current report queue exceeds max size, skipping adding new alerts")
            }
        }
    }
}
