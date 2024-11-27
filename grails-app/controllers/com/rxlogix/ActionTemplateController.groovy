package com.rxlogix

import com.rxlogix.audit.AuditTrail
import com.rxlogix.config.ActionConfiguration
import com.rxlogix.config.ActionTemplate
import com.rxlogix.config.ActionType
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.user.User
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.apache.commons.lang3.StringUtils
import org.apache.http.util.TextUtils
import org.springframework.context.MessageSource

@Secured(["isAuthenticated()"])
class ActionTemplateController {

    CRUDService CRUDService
    def cacheService
    MessageSource messageSource
    SignalAuditLogService signalAuditLogService
    UserService userService

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def index() {}

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def list() {
        def actionTemplateList = ActionTemplate.list().collect { it.toDto() }
        respond actionTemplateList, [formats: ['json']]
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def save() {
        Long userId
        params.name = params.name?.trim()?.replaceAll("\\s{2,}", " ")
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        try {
            if(TextUtils.isEmpty(params.details?.trim()) || TextUtils.isEmpty(params.dueIn)){
                responseDTO.status = false
                responseDTO.message = "Please provide information for all the mandatory fields which are marked with an asterisk (*)"
                render(responseDTO as JSON)
                return
            }
            def actionTemplateInstance = ActionTemplate.findByName(params.name)
            if(actionTemplateInstance){
                responseDTO.status = false
                responseDTO.message = "Action Template name already exists"
                render(responseDTO as JSON)
                return
            }
            ActionTemplate actionTemplate = new ActionTemplate()

            if (params.long('assignedTo'))
                userId = params.long('assignedTo')
            else
                actionTemplate.guestAttendeeEmail = params.assignedTo

            if (!actionTemplate.guestAttendeeEmail && !userId) {
                responseDTO.status = false
                responseDTO.message = messageSource.getMessage("assignedTo.nullable", null, Locale.default)
                render(responseDTO as JSON)
                return
            }
            //Added to fix PVS-56065
            String actionType = ""
            if (null != params.actionType) {
                actionType = ActionType.findById(params.actionType)
                if (StringUtils.isNotBlank(actionType)) {
                    actionType = actionType.replaceAll("\"", "\\\\\"")
                }
            }
            String actionConfig = ""
            if (null != params.actionConfig) {
                actionConfig = ActionConfiguration.findById(params.actionConfig)
                if (StringUtils.isNotBlank(actionConfig)) {
                    actionConfig = actionConfig.replaceAll("\"", "\\\\\"")
                }
            }
            def actionTemplateMap = """
           {
                "actionConfig": "${actionConfig}",
                "actionConfigId":"${params.actionConfig}",
                "actionType": "${actionType}",
                "actionTypeId": "${params.actionType}",
                "assignedTo": "${User.findById(userId)?.username}",
                "assignedToId": "${userId}",
                "dueIn": "${params.dueIn}",
                "details": "${params.details}",
                "comments": "${params.comments}"
           }
        """
            actionTemplate.actionJson = actionTemplateMap
            actionTemplate.name = params.name
            actionTemplate.description = params.description
            CRUDService.save(actionTemplate)
            cacheService.setActionTemplateCache(actionTemplate)

            responseDTO.message = "Action Template added successfully"
        } catch (Exception exp) {
            responseDTO.status = false
            responseDTO.message = "Please provide information for all the mandatory fields which are marked with an asterisk (*)"
            if (exp.toString().contains('guestAttendeeEmail.email.error'))
                responseDTO.message = messageSource.getMessage("com.rxlogix.config.Action.guestAttendeeEmail.email.error.guestAttendeeEmail", null, Locale.default)
        }
        render(responseDTO as JSON)
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def edit(Long id) {
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        ActionTemplate actionTemplate = ActionTemplate.read(id)
        responseDTO.data = actionTemplate.toDto()
        render(responseDTO as JSON)
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def update(Long id) {
        Long userId
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        params.name = params.name?.trim()?.replaceAll("\\s{2,}", " ")
        try {
            if(TextUtils.isEmpty(params.details?.trim())||TextUtils.isEmpty(params.dueIn)){
                responseDTO.status = false
                responseDTO.message = "Please provide information for all the mandatory fields which are marked with an asterisk (*)"
                render(responseDTO as JSON)
                return
            }
            ActionTemplate actionTemplate = ActionTemplate.get(id)
            def actionTemplateInstance = ActionTemplate.findByName(params.name)
            if(actionTemplateInstance && params.name!=actionTemplate.name){
                responseDTO.status = false
                responseDTO.message = "Action Template name already exists"
                render(responseDTO as JSON)
                return
            }

            if (params.long('assignedTo'))
                userId = params.long('assignedTo')
            else
                actionTemplate.guestAttendeeEmail = params.assignedTo

            if (!actionTemplate.guestAttendeeEmail && !userId) {
                responseDTO.status = false
                responseDTO.message = messageSource.getMessage("assignedTo.nullable", null, Locale.default)
                render(responseDTO as JSON)
                return
            }

            def actionTemplateMap = """
           {
                "actionConfig": "${ActionConfiguration.findById(params.actionConfig)}",
                "actionConfigId":"${params.actionConfig}",
                "actionType": "${ActionType.findById(params.actionType)}",
                "actionTypeId": "${params.actionType}",
                "assignedTo": "${User.findById(userId)?.username}",
                "assignedToId": "${userId}",
                "dueIn": "${params.dueIn}",
                "details": "${params.details}",
                "comments": "${params.comments}"
           }
        """
            actionTemplate.actionJson = actionTemplateMap
            actionTemplate.name = params.name
            actionTemplate.description = params.description
            CRUDService.update(actionTemplate)
            cacheService.setActionTemplateCache(actionTemplate)
            responseDTO.message = "Action Template updated successfully"
        } catch (Exception exp) {
            responseDTO.status = false
            responseDTO.message = "Please provide information for all the mandatory fields which are marked with an asterisk (*)"
            if (exp.toString().contains('guestAttendeeEmail.email.error'))
                responseDTO.message = messageSource.getMessage("com.rxlogix.config.Action.guestAttendeeEmail.email.error.guestAttendeeEmail", null, Locale.default)

        }
        render(responseDTO as JSON)
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def delete(Long id) {
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        ActionTemplate actionTemplate = ActionTemplate.get(id)
        String auditEntityName = actionTemplate.name
        try {
            CRUDService.delete(actionTemplate)
            cacheService.clearActionTemplateCache(actionTemplate.id)
            responseDTO.message = "Action Template ${actionTemplate.name} deleted successfully"
        }
        catch (Exception e) {
            responseDTO.message = "This template can not be deleted as it is being used in some safety observations/signals"
            responseDTO.status = false
        }
        if(responseDTO.status){
            signalAuditLogService.createAuditLog([
                    entityName: "Action Template",
                    moduleName: "Action Template",
                    category: AuditTrail.Category.DELETE.toString(),
                    entityValue: auditEntityName,
                    username: userService.getUser().username,
                    fullname: userService.getUser().fullName
            ] as Map, [[propertyName: "isDeleted", oldValue: "false", newValue: "true"]] as List)
        }
        render(responseDTO as JSON)
    }
}
