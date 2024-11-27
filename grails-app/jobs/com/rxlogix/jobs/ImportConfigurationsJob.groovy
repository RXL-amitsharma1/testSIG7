package com.rxlogix.jobs

import grails.util.Holders

class ImportConfigurationsJob {
    def importConfigurationService
    def masterExecutorService

    static triggers = {
        simple repeatInterval: Holders.config.signal.configuration.import.job.interval
    }

    def execute() {
        importConfigurationService.startProcessingUploadedFile()
    }

}
