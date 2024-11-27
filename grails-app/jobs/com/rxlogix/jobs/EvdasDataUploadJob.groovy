package com.rxlogix.jobs

import grails.util.Holders

class EvdasDataUploadJob {
    def evdasDataImportService
    def applicationSettingsService
    def config = Holders.config

    static triggers = {
        simple repeatInterval: Holders.config.signal.evdas.data.upload.job.interval
    }

    def execute() {
        try {
            if (!applicationSettingsService.fetchEvdasErmrUploadLocked() && Holders.config.signal.boot.status == true) {
                evdasDataImportService.processWaitingFiles()
            } else {
                log.info("eRMR file process lock is not available.")
            }
        } catch (Exception e) {
            log.error('EvdasApplicationSettings table is not yet created in domain.')
        }
    }
}
