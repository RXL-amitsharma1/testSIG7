package com.rxlogix.jobs

import grails.util.Holders

class ProductAssignmentImportJob {
    def productAssignmentImportService
    static triggers = {
        simple repeatInterval: Holders.config.signal.product.assignment.import.job.interval
    }

    def execute() {
        productAssignmentImportService.startProcessingUploadedFile()
    }
}
