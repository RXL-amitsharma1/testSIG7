package com.rxlogix

import com.rxlogix.signal.DrugClassification
import grails.gorm.transactions.Transactional

@Transactional
class DrugClassificationService {
    def sessionFactory_faers
    def CRUDService

    def save(paramsMap, def classificationString, Boolean isFirstEntry = false) {
        def customEntryList = [["fieldName": "Classifications", "originalValue": "", "newValue": classificationString]]
        DrugClassification drugClassification = null
        DrugClassification.withTransaction {
            drugClassification = new DrugClassification(paramsMap)
            if (isFirstEntry) {
                CRUDService.saveWithAuditLog(drugClassification,null,false,customEntryList)
                isFirstEntry = false
            } else {
                drugClassification.save(flush: true)
            }
        }
        return isFirstEntry
    }

    def update(drugClassification, valueMap, dataSource, isFirstEntry) {
        valueMap.each { key, value ->
            drugClassification."${key}" = value
        }
        def customEntryList = []
        if (isFirstEntry) {
            DrugClassification."$dataSource".withSession {
                CRUDService.updateWithAuditLog(drugClassification, customEntryList)
            }
            isFirstEntry = false
        } else {
            drugClassification.save(flush: true)
        }

        return isFirstEntry
    }

    def delete(drugClassification,Boolean isFirstEntry=false) {
        DrugClassification.withTransaction {
            if(isFirstEntry==true){
                CRUDService.deleteWithAuditLog(drugClassification)
                isFirstEntry=false
            }else{
                drugClassification.delete(flush: true)
            }
        }
        return isFirstEntry
    }

    def drugClassificationList(){
        List drugClassificationList = []
        List<DrugClassification> drugClassifications = []
        DrugClassification.withTransaction {
            drugClassifications = DrugClassification.list()

        }

        Map drugClassificationMap = drugClassifications.groupBy {
            it.className
        }

        drugClassificationMap.each{className,values ->
            def productName = values[0].productNames
            values.removeIf{
                it.classification == it.productNames

            }
            def addedClassification =  values.collect {
                it.classification + '(' +  it.classificationType.value + ')'
            }.join(',')
            drugClassificationList.add([
                    product : productName ,
                    className : className ,
                    addedClassification : addedClassification
            ])
        }
        drugClassificationList
    }
}
