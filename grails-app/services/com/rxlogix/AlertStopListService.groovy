package com.rxlogix

import com.rxlogix.config.AlertStopList
import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import grails.validation.ValidationException
import static com.rxlogix.util.DateUtil.*

@Transactional
class AlertStopListService {
    def CRUDService

    def getAlertStopList() {
        def productList
        def eventList
        def alertStopList = AlertStopList.list().collect { AlertStopList stopList ->
            def productMap = [:]
            def eventMap = [:]
            productMap = prepareProductMap(stopList.productName, productList)
            eventMap = prepareEventMap(stopList.eventName, eventList)
            [
                    productName : productMap,
                    eventName : eventMap,
                    dateCreated : toDateTimeString(stopList.dateCreated),
                    dateDeactivated : toDateTimeString(stopList.dateDeactivated),
                    activated : stopList.activated,
                    id:stopList.id
            ]
        }
        return alertStopList
    }

    def prepareEventMap(eventName, eventList) {
        def eventMap = [:]
        def eventFamilyMap = [
                '1':'SOC','2':'HLGT','3':'HLT','4':'PT','5':'LLT'
        ]
        def jsonSlurper = new JsonSlurper()
        def list = jsonSlurper.parseText(eventName)
        list.each { k, v ->
            eventList = []
            v.each {
                eventList = eventList + it.name
            }
            if(!eventList.isEmpty()) {
                eventMap.put(eventFamilyMap[k],eventList)
            }
        }
        return eventMap
    }

    def prepareProductMap(productName, productList) {
        def productMap = [:]
        def jsonSlurper = new JsonSlurper()
        def productFamilyMap = [
                '1':'Ingredient','2':'Family','3':'Product Name','4':'Trade Name'
        ]
        def list = jsonSlurper.parseText(productName)
        list.each { key, value ->
            productList = []
            value.each {
                productList = productList + it.name
            }
            if(!productList.isEmpty()) {
                productMap.put(productFamilyMap[key],productList)
            }
        }
        return productMap
    }

    def saveList(alertStopList) {
        try {
            CRUDService.save(alertStopList)
            alertStopList = null
            return alertStopList
        } catch(ValidationException ve) {
            alertStopList.errors = ve.errors
            return alertStopList
        }
    }

    def updateList(alertStopList) {
        try{
            CRUDService.update(alertStopList)
        } catch(Exception e){
        }
    }

}
