package com.rxlogix.jobs

import grails.util.Holders

class EvdasCaseLineListingUploadJob {
    def evdasCaseListingImportService
    def applicationSettingsService
    def config = Holders.config

    static triggers = {
        simple repeatInterval: Holders.config.signal.evdas.case.line.listing.upload.job.interval
    }

    def execute() {
        try {
            if (!applicationSettingsService.fetchEvdasCaseListingUploadLocked() && Holders.config.signal.boot.status == true) {
                evdasCaseListingImportService.processWaitingFiles()
            } else {
                log.info("Case Listing file process lock is not available.")
            }
        } catch (Exception e) {
            log.error('EvdasApplicationSettings table is not yet created in domain.')
        }
    }
}
