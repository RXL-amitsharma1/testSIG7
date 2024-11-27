package com.rxlogix

import com.rxlogix.util.FileNameCleaner
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.apache.http.util.TextUtils
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

@Secured(["isAuthenticated()"])
class ProductSummaryController {
    def alertAttributesService
    def evdasAlertService
    def productSummaryService
    def userService
    def dynamicReportService


    @Secured(['ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_AGGREGATE_CASE_VIEWER',
            'ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION'])
    def index() {
        Map prodSummaryIndexMap = productSummaryService.getProductSummaryIndexMap()
        render(view: "index", model: prodSummaryIndexMap)
    }

    @Secured(['ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_AGGREGATE_CASE_VIEWER',
            'ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION'])
    def search() {
        Map prodSummarySearchMap = productSummaryService.getProductSummarySearchMap(params)
        if (TextUtils.isEmpty(params.productSelection) || params.startDate == "null") {
            flash.error = "Please fill all required fields"
        }
        render(view: 'index', model: prodSummarySearchMap)
    }

    def listProductSummaryResult(){
        def resultMap = productSummaryService.getProductSummary(params)
        def finalMap = [recordsTotal: resultMap.resultCount, recordsFiltered: resultMap.filteredCount, aaData: resultMap.resultList]
        render(finalMap as JSON)
    }

    @Secured(['ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_AGGREGATE_CASE_VIEWER',
            'ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION'])
    def fetchSubstanceFrequency() {
        def frequencyMap = productSummaryService.getFrequencyMap(params.productName, params.selectedDatasource)
        render(frequencyMap as JSON)
    }

    @Secured(['ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_AGGREGATE_CASE_VIEWER',
            'ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION'])
    def requestByForAlert(){
        productSummaryService.saveRequestByForAlert(params)
        render([success:true] as JSON)
    }

    @Secured(['ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_AGGREGATE_CASE_VIEWER',
            'ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION'])
    def exportReport() {
        def reportFile = productSummaryService.getExportedFile(params)
        renderReportOutputType(reportFile, "ProductSummary")
    }


    @Secured(['ROLE_AGGREGATE_CASE_CONFIGURATION', 'ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_AGGREGATE_CASE_VIEWER',
            'ROLE_EVDAS_CASE_CONFIGURATION', 'ROLE_EVDAS_CASE_REVIEWER', 'ROLE_EVDAS_CASE_VIEWER', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION'])
    void renderReportOutputType(File reportFile, String name) {
        String reportName = name + DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss").print(new DateTime())
        response.contentType = "${dynamicReportService.getContentType(params.outputFormat)}; charset=UTF-8"
        response.contentLength = reportFile.size()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.$params.outputFormat" + "\"")
        response.getOutputStream().write(reportFile.bytes)
        response.outputStream.flush()
    }
}
