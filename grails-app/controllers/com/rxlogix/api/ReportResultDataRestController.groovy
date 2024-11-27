package com.rxlogix.api
import com.rxlogix.config.ReportResult
import com.rxlogix.config.ReportResultData
import grails.rest.RestfulController

class ReportResultDataRestController extends RestfulController {
    def springSecurityService

    ReportResultDataRestController() {
        super(ReportResultData)
    }

    def show() {
        render(text: "{\"data\":${ReportResult.get(params.id).data?.value}}", contentType: "application/json" ) // this will produce gzip'ed output
    }
}
