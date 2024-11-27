package com.rxlogix.jobs

import com.rxlogix.config.MasterConfiguration
import grails.util.Holders

class AlertAdministrationJob {


    def concurrent = false
    def alertAdministrationService


    static triggers = {
        simple startDelay: 300000l, repeatInterval: Holders.config.alertManagement.job.sync.interval
    }

    def execute() {
        if (Holders.config.signal.boot.status == true) {

            Map<String, List<Object>> pausedConfigurations = alertAdministrationService.fetchAutoPausedConfigurations()
            List<MasterConfiguration> pausedMasterConfigurations = alertAdministrationService.fetchAutoPausedMasterConfigurations()

            try {
                if (Holders.config.alertManagement.AlertFailureNotification.enabled) {
                    alertAdministrationService.triggerBulkPausedAlertNotification(pausedConfigurations, pausedMasterConfigurations)
                }
            } catch (Throwable tr) {
                log.info("Exception occurred in sending email for paused alerts")
                log.error(tr.getMessage(), tr)
            }

            try {

                if (!pausedConfigurations.isEmpty()) {
                    log.info("Updating auto-paused configurations...")
                    alertAdministrationService.updateAutoPausedConfigurations(pausedConfigurations)
                }
                if (!pausedMasterConfigurations.isEmpty()) {
                    log.info("Updating auto-paused master configurations...")
                    alertAdministrationService.updateAutoPausedMasterConfigurations(pausedMasterConfigurations)
                }

            } catch (Throwable tr) {
                log.info("Exception occurred in alert administration job")
                log.error(tr.getMessage(), tr)
            }

        }
    }
}
