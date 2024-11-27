package com.rxlogix.config

import com.rxlogix.SignalAuditLogService
import com.rxlogix.UserService
import com.rxlogix.util.MiscUtil
import com.rxlogix.audit.AuditTrail
import grails.plugin.springsecurity.annotation.Secured

import javax.xml.bind.ValidationException

@Secured(["isAuthenticated()"])
class ActionTypeController {

    def actionTypeService
    def cacheService
    SignalAuditLogService signalAuditLogService
    UserService userService

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def index() {
        redirect(action: "list", params: params)
    }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def list() {
        params.max = Math.min(params.max ? params.max.toInteger() : 10, 100)
        [actionTypeInstanceList: ActionType.list(params), actionTypeInstanceTotal: ActionType.count()]
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def create() {
        def actionTypeInstance = new ActionType()
        actionTypeInstance.properties = params
        return [actionTypeInstance: actionTypeInstance]
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def save() {
        params.displayName = params.displayName?.trim()?.replaceAll("\\s{2,}", " ")
        params.value = params.value?.trim()?.replaceAll("\\s{2,}", " ")
        def actionTypeInstance = new ActionType(params)
        try {
            actionTypeService.saveActionType(actionTypeInstance)
            cacheService.setActionTypeCache(actionTypeInstance)
            flash.message = message(code: "default.created.message", args: ['Action Type', "${actionTypeInstance.value}"])
            flash.args = [actionTypeInstance.id]
            flash.defaultMessage = "Action Type ${actionTypeInstance.value} created"
            redirect(action: "list")
        }
        catch( grails.validation.ValidationException vx ) {
            def customErrorMessages = MiscUtil.getCustomErrorMessageList(vx)
            if (customErrorMessages) {
                flash.error = MiscUtil.getCustomErrorMessageList(vx);
            }
            if (vx.toString()?.contains("ActionType.displayName.nullable") || vx.toString()?.contains("ActionType.value.nullable")){
                if(customErrorMessages) {
                    flash.error << message(code: "com.rxlogix.config.ActionType.all.fields.required")
                }
                else {
                    flash.error = message(code: "com.rxlogix.config.ActionType.all.fields.required")
                }
            }
            vx.printStackTrace()
            render(view: "create", model: [actionTypeInstance: actionTypeInstance])
        }
        catch (Exception exp) {
            exp.printStackTrace(  )
            render(view: "create", model: [actionTypeInstance: actionTypeInstance])
        }
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def show() {
        def actionTypeInstance = ActionType.get(params.id)
        if (!actionTypeInstance) {
            flash.message = "actionType.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "ActionType not found with id ${params.id}"
            redirect(action: "list")
        } else {
            return [actionTypeInstance: actionTypeInstance]
        }
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def edit() {
        def actionTypeInstance = ActionType.get(params.id)
        if (!actionTypeInstance) {
            flash.message = "actionType.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "ActionType not found with id ${params.id}"
            redirect(action: "list")
        } else {
            return [actionTypeInstance: actionTypeInstance]
        }
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def update() {
        params.displayName = params.displayName?.trim()?.replaceAll("\\s{2,}", " ")
        params.value = params.value?.trim()?.replaceAll("\\s{2,}", " ")
        def actionTypeInstance = ActionType.get(params.id)
        if (actionTypeInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (actionTypeInstance.version > version) {

                    actionTypeInstance.errors.rejectValue("version", "actionType.optimistic.locking.failure", "Another user has updated this ActionType while you were editing")
                    render(view: "edit", model: [actionTypeInstance: actionTypeInstance])
                    return
                }
            }
            actionTypeInstance.properties = params
            try {
                if (actionTypeService.saveActionType(actionTypeInstance)) {
                    cacheService.setActionTypeCache(actionTypeInstance)
                    flash.message = message(code: "actionType.updated", args: [ "${actionTypeInstance.value}"])
                    flash.args = [params.id]
                    flash.defaultMessage = "ActionType ${params.id} updated"
                    redirect(action: "show", id: actionTypeInstance.id)
                } else {
                    render(view: "edit", model: [actionTypeInstance: actionTypeInstance])
                }
            }
            catch( grails.validation.ValidationException vx ) {
                vx.printStackTrace()
                def customErrorMessages = MiscUtil.getCustomErrorMessageList(vx)
                if (customErrorMessages) {
                    flash.error = MiscUtil.getCustomErrorMessageList(vx);
                }
                if (vx.toString()?.contains("ActionConfiguration.displayName.nullable") || vx.toString()?.contains("ActionConfiguration.value.nullable")){
                    if(customErrorMessages) {
                        flash.error << message(code: "com.rxlogix.config.ActionConfiguration.all.fields.required")
                    }
                    else {
                        flash.error = message(code: "com.rxlogix.config.ActionConfiguration.all.fields.required")

                    }
                }
                render(view: "edit", model: [actionTypeInstance: actionTypeInstance])
            }
            catch(Throwable th) {
                log.error(th.getMessage())
                render(view: "edit", model: [actionTypeInstance: actionTypeInstance])
            }
        } else {
            flash.message = "actionType.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "ActionType not found with id ${params.id}"
            redirect(action: "edit", id: params.id)
        }
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def delete() {
        def actionTypeInstance = ActionType.get(params.id)
        if (actionTypeInstance) {
            Boolean deleted = false
            String auditEntityName = actionTypeInstance.value
            try {
                actionTypeService.deleteActionType(actionTypeInstance)
                cacheService.removeActionType(actionTypeInstance.id)

                flash.message = message(code: "actionType.deleted", args: [ "${actionTypeInstance.value}"])
                flash.args = [params.id]
                flash.defaultMessage = "ActionType ${params.id} deleted"
                redirect(action: "list")
                deleted = true
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = message(code: "actionType.not.deleted", args: [ "${actionTypeInstance.value}"])
                flash.args = [params.id]
                flash.defaultMessage = "ActionType ${params.id} could not be deleted"
                redirect(action: "show", id: params.id)
            }
            if(deleted){
                signalAuditLogService.createAuditLog([
                        entityName: "Action Type",
                        moduleName: "Action Type",
                        category: AuditTrail.Category.DELETE.toString(),
                        entityValue: auditEntityName,
                        username: userService.getUser().username,
                        fullname: userService.getUser().fullName
                ] as Map, [[propertyName: "isDeleted", oldValue: "false", newValue: "true"]] as List)
            }
        } else {
            flash.message = "actionType.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "ActionType not found with id ${params.id}"
            redirect(action: "list")
        }
    }
}
