package com.rxlogix

import com.rxlogix.config.Disposition
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.signal.SignalNotificationMemo
import com.rxlogix.signal.SignalOutcome
import grails.converters.JSON
import grails.util.Holders
import grails.plugin.springsecurity.annotation.Secured
import groovy.json.JsonBuilder
import org.hibernate.criterion.CriteriaSpecification

@Secured(["isAuthenticated()"])
class SignalMemoReportController {

    def signalMemoReportService
    def alertAttributesService
    def userService

    def index() {
        String signalSource = new JsonBuilder(alertAttributesService.get('initialDataSource')).toPrettyString()
        String triggerVariable = Holders.config.signal.autoNotification.memo.report.trigger.variable
        String signalOutcomes = new JsonBuilder(SignalOutcome.list().findAll { !it.isDeleted }.collect { it.name }).toPrettyString()
        String signalOutcomesToBeDisabled = new JsonBuilder(SignalOutcome.list().findAll { it.isDisabled }.collect { it.name }).toPrettyString()
        String actionsTaken = new JsonBuilder(alertAttributesService.get('actionsTaken')).toPrettyString()
        List dispositionList = Disposition.list().collect { [id: it.id, name: it.displayName] }
        render(view: "index", model: [signalSource: signalSource, triggerVariable: triggerVariable, signalOutcomes: signalOutcomes, actionsTaken: actionsTaken, signalOutcomesToBeDisabled: signalOutcomesToBeDisabled, dispositionList: dispositionList])
    }

    def fetchSignalMemoConfig() {
        Map configMap = signalMemoReportService.fetchSignalMemoConfig(params)
        render configMap as JSON
    }

    def saveSignalMemoConfig() {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            boolean saveResult = false
            boolean validEmailAddresses = false
            validEmailAddresses = signalMemoReportService.validateEmailAddresses(params.mailAddresses)
            if(validEmailAddresses){
                saveResult = signalMemoReportService.saveSignalMemoConfig(params)
            }
            if(!saveResult){
                responseDTO.status = false
                if (!validEmailAddresses){
                    responseDTO.message = message(code: "signal.memo.label.email.error.save")
                } else {
                    responseDTO.message = message(code: "signal.memo.label.error.save")
                }
            }
        } catch (Exception e) {
            log.error(e.printStackTrace())
            responseDTO.message = message(code: "signal.memo.label.error.save")
            responseDTO.status = false
        }
        render(responseDTO as JSON)
    }

    def deleteSignalMemoConfig() {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            SignalNotificationMemo signalNotificationMemo = SignalNotificationMemo.get(params.signalMemoId as Long)
            log.info("deleting signal memo with mail user: ${signalNotificationMemo.mailUsers} and  mail groups: ${signalNotificationMemo.mailGroups}")
            // dont remove this log as this is required to fetch lazy property for audit deletion entry
            signalNotificationMemo.delete(flush: true)
            responseDTO.data = SignalNotificationMemo.list()?.size()
        } catch (Exception e) {
            log.error(e.printStackTrace())
            responseDTO.status = false
            responseDTO.message = message(code: "signal.memo.label.error.delete")
        }
        render(responseDTO as JSON)
    }

    def checkIfConfigExistsWithConfigName(){
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            List<Map> configList = SignalNotificationMemo.createCriteria().list {
                resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
                projections {
                    property("id", "id")
                    property("triggerVariable", "triggerVariable")
                }
                eq("configName", params.configName.trim() as String, [ignoreCase: true])
            } as List<Map>
            if(configList.size() == 0){
                responseDTO.status = false
            } else {
                if(params.configId){
                    if(configList.size() == 1 && configList['id'][0] == params.configId as Long){
                        responseDTO.status = false
                    }
                    boolean checkTriggerVarExists = false
                    configList.each {
                        if(it.id == params.configId as Long && it.triggerVariable == params.triggerVariable){
                            checkTriggerVarExists = true
                        }
                    }
                    if(checkTriggerVarExists) {
                        responseDTO.data = null
                    } else if(configList['triggerVariable'].contains(params.triggerVariable as String)){
                        responseDTO.data = params.triggerVariable
                    }
                } else if(configList['triggerVariable'].contains(params.triggerVariable as String)){
                    responseDTO.data = params.triggerVariable
                }
            }
        } catch (Exception e) {
            log.error(e.printStackTrace())
            responseDTO.status = false
            responseDTO.message = message(code: "signal.memo.label.error.fetch")
        }
        render(responseDTO as JSON)
    }
}
