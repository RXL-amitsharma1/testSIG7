package com.rxlogix.jobs

import grails.util.Holders

class EvdasCaseLineListingImportJob {
    def evdasCaseListingImportService
    static triggers = {
        simple repeatInterval: Holders.config.signal.evdas.case.line.listing.import.job.interval
    }

    def execute() {
        if(Holders.config.signal.boot.status == true){
            evdasCaseListingImportService.initiateDataImport()
        }
    }
}
