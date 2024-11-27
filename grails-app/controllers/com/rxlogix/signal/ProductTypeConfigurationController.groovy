package com.rxlogix.signal

import com.rxlogix.Constants
import com.rxlogix.config.Configuration
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.FileNameCleaner
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import groovy.sql.Sql
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import static org.springframework.http.HttpStatus.*
@Secured(["isAuthenticated()"])
class ProductTypeConfigurationController {

    def productTypeConfigurationService
    def dynamicReportService
    def dataSource
    def signalAuditLogService

//    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index() {
        render(view: "index", model: [:])
    }

    def list() {
        def productTypeConfigList = ProductTypeConfiguration.list().collect {
            it.toDto()
        }.sort{it.lastUpdated}.reverse()

        def model = [aaData: productTypeConfigList]
        // fetching product names and role values from mart table
        model << productTypeConfigurationService.fetchSelectableFieldsFromMart()
        render(model as JSON)
    }

    def show(Long id) {
        respond productTypeConfigurationService.get(id)
    }

    def create() {
        def response
        try {
            ProductTypeConfiguration productTypeConfiguration = new ProductTypeConfiguration(params)
            productTypeConfiguration.lastUpdated = new Date()
            productTypeConfiguration.dateCreated = new Date()
            productTypeConfiguration.save(flush: true, failOnError: true)
            response = [success: true]
        }catch(Exception ex){
            response = [success: false, errorMessage: "Exception occurred in saving of Product Rule", exception: ex.getMessage()]
        }
        render(response as JSON)
    }


    def edit(Long id) {
//        respond productTypeConfigurationService.get(id)
    }

    def update() {
        ProductTypeConfiguration productTypeConfiguration = ProductTypeConfiguration.get(params.id)
        def response
        try {
            productTypeConfiguration.setProperties(params)
            productTypeConfiguration.lastUpdated = new Date()
            productTypeConfiguration.save(flush: true, failOnError: true)
            response = [success: true]
        }catch(Exception ex){
            log.error(ex.getMessage())
            response = [success: false, errorMessage: "Exception occurred in updating of Product Rule", exception: ex.getMessage()]
        }
        render(response as JSON)
    }

    def delete(Long id) {
        ProductTypeConfiguration productTypeConfiguration = ProductTypeConfiguration.get(params.id)
        def response
        List aggList = productTypeConfigurationService.getAllLinkedAlerts(productTypeConfiguration.id)
        if(aggList){
            response = [success: false, errorMessage: "Cannot delete product rule because of linked Alerts", data: aggList.size()]
            render(response as JSON)
            return
        }
        try {
            batchUpdateConfigurationForSimilarProductType(productTypeConfiguration)
            productTypeConfiguration.delete(flush: true)
            response = [success: true]
        }catch(Exception ex){
            log.error(ex.getMessage())
            response = [success: false, errorMessage: "Exception occurred while deleting of Product Rule", exception: ex.getMessage()]
        }
        render(response as JSON)
    }


    def linkedAggAlerts(Long productConfigId) {
        try {
            List aggData = productTypeConfigurationService.getAllLinkedAlerts(productConfigId)
            render view: "linkedAggAlerts", model: [productConfigName: ProductTypeConfiguration.get(productConfigId)?.name, aggData: aggData]
        } catch (Exception e) {
            log.error(e.printStackTrace())
            redirect(controller: "productTypeConfiguration", action: "index")
            return
        }
    }

    def exportProductRule(Long caseId){
        List selectedCases = []
        if (params.selectedCases != "") {
            selectedCases = params.selectedCases?.split(',') as List
        }
        def list = []
        List productTypeConfigurationList = ProductTypeConfiguration.list().collect { it.toDto() }
        selectedCases?.each { selectedCase ->
            list.add(productTypeConfigurationList.find {
                it.id == selectedCase.toLong()
            })
        }
        def report = dynamicReportService.createProductRuleReport(list, params)
        renderReportOutputType(report,params)
        signalAuditLogService.createAuditForExport(params?.containsKey('criteriaSheetList') ? params.criteriaSheetList : null,"Product Type Configuration" , "Product Type Configuration", params, report.name)
    }

    private renderReportOutputType(File reportFile,def params) {
        String reportName = "Aggregate Alert Product Type Configuration" + DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss").print(new DateTime())
        params.reportName=reportName.replaceAll(" ","+")
        response.contentType = "${dynamicReportService.getContentType(params.outputFormat)}; charset=UTF-8"
        response.contentLength = reportFile.size()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\"" + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.$params.outputFormat" + "\"")
        response.getOutputStream().write(reportFile.bytes)
        response.outputStream.flush()
    }

    def batchUpdateConfigurationForSimilarProductType(ProductTypeConfiguration productTypeConfiguration){
        ProductTypeConfiguration similarProductTypeConfiguration = ProductTypeConfiguration.createCriteria().get {
            and{
                ilike("name", "${productTypeConfiguration.name}")
                ne("id", productTypeConfiguration.id)
            }
            order("name","asc")
            maxResults(1)
        } as ProductTypeConfiguration
        if(!similarProductTypeConfiguration){
            return
        }
        List<Map> configurationProductTypeList = []
        def configList = Configuration.list().findAll {
            (it.type == 'Aggregate Case Alert')
        }
        Sql sql = new Sql(dataSource)
        String productType = ""
        configList.each { configuration ->
            if(configuration.drugType.contains(productTypeConfiguration.id as String)){
                productType = ""
                productType = configuration.drugType.replace(productTypeConfiguration.id as String, similarProductTypeConfiguration.id as String)
                configurationProductTypeList.add(id: configuration.id, drugType: productType)
            }
        }
        try {
            sql.withBatch(100, "UPDATE RCONFIG SET DRUG_TYPE = :drugType WHERE ID = :id", { preparedStatement ->
                configurationProductTypeList.each {
                    preparedStatement.addBatch(id: it.id, drugType: it.drugType)
                }
            })
        } catch (Exception e) {
            println("########## Some error occurred in Updating ProductTypeConfiguration for Configuration #############")
            e.printStackTrace()
        } finally {
            sql?.close()
        }
    }
}
