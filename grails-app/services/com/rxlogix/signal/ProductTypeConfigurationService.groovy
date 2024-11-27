package com.rxlogix.signal

import com.rxlogix.Constants
import groovy.sql.Sql


class ProductTypeConfigurationService {
    def signalDataSourceService
    def alertAdministrationService

    def save(){

    }

    def update(){

    }

    def delete(){

    }

    def fetchSelectableFieldsFromMart(){
        def selectableFieldsMap = [productTypeMap: fetchProductType(), roleMap: fetchRolesForDrug()]
        selectableFieldsMap
    }

    List<Map> fetchProductType() {
        Sql sql = null
        List <Map> productTypeMap = []
        try {
            sql = new Sql(signalDataSourceService.getDataSource('pva'))
            sql.eachRow("select * from vw_product_type where lang_id = '1'" , []) { row ->
                Map rowData = ['id' : row.product_type_id , 'text' : row.product_type ]
                productTypeMap << rowData
            }
            sql.close()
        } catch (Throwable t) {
            log.error("Error on fetching product types "+t.getMessage())
        } finally {
            try {
                sql?.close()
            } catch (Throwable notableToHandle) {
                log.error("Failed to close the Sql", notableToHandle)
            }
        }
        productTypeMap.sort{ a,b-> a.text.toLowerCase() <=> b.text.toLowerCase() }
        return productTypeMap
    }

    List<Map> fetchRolesForDrug() {
        Sql sql = null
        List <Map> roleMap = []
        try {
            sql = new Sql(signalDataSourceService.getDataSource('pva'))
            sql.eachRow("select * from VW_CLP_DRUG_TYPE_DSP where therapy_type is not null and lang_id = '1'" , []) { row ->
                Map rowData = ['id' : row.id , 'text' : row.therapy_type]
                roleMap << rowData
            }
            sql.close()
        } catch (Throwable t) {
            log.error("Error on fetching roles "+t.getMessage())
        } finally {
            try {
                sql?.close()
            } catch (Throwable notableToHandle) {
                log.error("Failed to close the Sql", notableToHandle)
            }
        }
        roleMap.sort{ a,b-> a.text.toLowerCase() <=> b.text.toLowerCase() }
        return roleMap
    }

    def getAllLinkedAlerts(Long productTypeConfigId){
        def allScheduledAggAlerts = alertAdministrationService.listConfigurations([
                alertType: Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
                alertRunType: Constants.AlertStatus.SCHEDULED
        ])
        allScheduledAggAlerts.findAll{
            it.drugType.contains(productTypeConfigId as String)
        }
    }

}