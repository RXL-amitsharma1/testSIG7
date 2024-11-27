package com.rxlogix.jobs

import com.rxlogix.ReportFieldService

/**
 * Created by Lei Gao on 7/11/15.
 */
class RetrieveSelectableValuesJob {
    /*
    static triggers = {
        simple name : "selectableValuesReader", startDelay: 300000, repeatInterval: 7200000
    }
    */
    def ReportFieldService reportFieldService

    def group = "data catcher"
    def description = "Read selectable values per 300sec"

    void execute() {
        def values = reportFieldService.retrieveValuesFromDatabase("en")
        if (values != null && values.keySet().size() > 0)
            reportFieldService.serializeValues(values)
    }
}
