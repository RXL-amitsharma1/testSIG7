package com.rxlogix.jobs

import grails.util.Holders

class CaseFormJob {
    def caseFormService
    static triggers = {
        simple repeatInterval: Holders.config.signal.case.form.job.interval
    }

    def execute() {
        caseFormService.generateCaseFormExport()
    }
}
