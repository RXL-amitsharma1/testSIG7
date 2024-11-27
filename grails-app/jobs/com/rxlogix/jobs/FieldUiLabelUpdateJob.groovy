package com.rxlogix.jobs

class FieldUiLabelUpdateJob {

    def cacheService
    def reportFieldService

    static triggers = {
        simple startDelay: 600000l, repeatInterval: 600000l // execute job once in 10 min, delay of 10 min
    }

    def execute() {
        // Updating Ui labels for alertQueries according to reports
        cacheService.prepareRptToUiLabelInfoPvr()
        reportFieldService.retrieveValuesFromDatabase("en")
    }
}
