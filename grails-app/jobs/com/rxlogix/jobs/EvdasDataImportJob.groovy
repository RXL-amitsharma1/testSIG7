package com.rxlogix.jobs

import grails.util.Holders

class EvdasDataImportJob {
    def evdasDataImportService
    static triggers = {
        simple repeatInterval: Holders.config.signal.evdas.data.import.job.interval
    }

    def execute() {
        if(Holders.config.signal.evdas.enabled && Holders.config.signal.boot.status == true){
            evdasDataImportService.initiateDataImport()
        }
    }
}
