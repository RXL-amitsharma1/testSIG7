package com.rxlogix.jobs

import com.rxlogix.config.PreCheckService
import grails.util.Holders

class SystemPrecheckExecutorJob {


    PreCheckService preCheckService

    static triggers = {
        simple repeatInterval: Holders.config.system.precheck.configuration.job.interval * 60000
    }

    def execute() {
        if (Holders.config.signal.boot.status == true && Holders.config.system.precheck.configuration.enable == true) {
            preCheckService.executeSystemConfigurationPrecheck()
        }
    }
}
