package com.rxlogix

import com.rxlogix.config.Disposition
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.signal.Justification
import com.rxlogix.util.MiscUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import groovy.json.JsonBuilder

@Secured(["isAuthenticated()"])
class JustificationController {

    def userService
    def CRUDService
    JustificationService justificationService
    def checkBoxList = ["alertWorkflow", "signalWorkflow", "signalPriority", "alertPriority", "caseAddition"]

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def index() {
        List<Map<Long, String>> dispositions = Disposition.list()?.collect {
            [id: it.id, name: it.displayName]
        }
        [dispositions: dispositions]
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def save() {
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        StringBuilder errorMessage = new StringBuilder()
        try {
            Map featureMap = createFeatureJson()
            Justification justification = params.id ? Justification.get(params.id) : new Justification()
            justification.name = params.justificationName?.trim().replaceAll("\\s{2,}", " ")
            justification.justification = params.justificationText
            justification.feature = new JsonBuilder(featureMap).toPrettyString()
            justificationService.bindDispositions(justification, params.linkedDisposition)
            responseDTO.message = "Justification added successfully"
            CRUDService.save(justification)
        } catch (grails.validation.ValidationException vx) {
            def customErrorMessages = MiscUtil.getCustomErrorMessageList(vx)
            if(customErrorMessages) {
                customErrorMessages?.each {
                    errorMessage.append(it)
                    errorMessage.append('</br>')
                }
                responseDTO.message = errorMessage.toString()
            }

            if (vx.toString()?.contains("Justification.name.blank") || vx.toString()?.contains("justification.justification.blank.error")){
                if(customErrorMessages) {
                    errorMessage.append("Please provide information for all the mandatory fields which are marked with an asterisk (*)")
                    responseDTO.message = errorMessage.toString()
                }
                else {
                    responseDTO.message = "Please provide information for all the mandatory fields which are marked with an asterisk (*)"
                }
            }
            responseDTO.status = false
            log.error("Exception is : ${vx}")
        } catch (ValidationException e) {
            e.printStackTrace()
            responseDTO.status = false
            responseDTO.message = "Please provide information for all the mandatory fields which are marked with an asterisk (*)"
        }
        render(responseDTO as JSON)
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def edit(Long id) {
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        Justification justification = Justification.read(id)
        responseDTO.data = justification.toDto()
        render(responseDTO as JSON)
    }

    def handleException(Exception e) {
        return ResponseDTO.getErrorResponseDTO(e)
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def delete(Long id) {
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        Justification justification = Justification.get(id)
        CRUDService.delete(justification)
        responseDTO.message = "Justification ${justification.name} deleted successfully"
        render(responseDTO as JSON)
    }

    def createFeatureJson() {
        def map = [:]
        checkBoxList.each {
            map.put(it, params."$it" ?: "off")
        }
        map
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def list() {
        String timezone = userService.getUser()?.preference?.timeZone
        List<Map> list = Justification.list(sort: "id", order: "desc").collect {
            it.toDto(timezone)
        }
        respond list, [formats: ['json']]
    }

    def fetchJustificationsForDisposition(Long id) {
        List<String> justifications = justificationService.fetchJustificationsForDisposition(id as Long, Boolean.parseBoolean(params.signalWorkFlow))
        render(justifications as JSON)
    }

    def fetchJustificationsForDispositionForBR(Long id) {
        List<Map> justifications = justificationService.fetchJustificationsForDispositionForBR(id as Long)
        render(justifications as JSON)
    }
}
