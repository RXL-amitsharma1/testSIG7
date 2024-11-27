package com.rxlogix.config

import com.rxlogix.dto.ResponseDTO
import com.rxlogix.util.MiscUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

import javax.xml.bind.ValidationException

@Secured(["isAuthenticated()"])
class ActionConfigurationController {

    def actionConfigurationService
    def cacheService

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def index() {
        redirect(action: "list", params: params)
    }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def list() {
        params.max = Math.min(params.max ? params.max.toInteger() : 10,  100)
        [actionConfigurationInstanceList: ActionConfiguration.list(params), actionConfigurationInstanceTotal: ActionConfiguration.count()]
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def create() {
        def actionConfigurationInstance = new ActionConfiguration()

        actionConfigurationInstance.properties = params
        return [actionConfigurationInstance: actionConfigurationInstance]
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def save() {
        params.displayName = params.displayName?.trim()?.replaceAll("\\s{2,}", " ")
        params.value = params.value?.trim()?.replaceAll("\\s{2,}", " ")
        def actionConfigurationInstance = new ActionConfiguration(params)
        try{
            actionConfigurationService.saveActionConfiguration(actionConfigurationInstance)
            cacheService.setActionConfigurationCache(actionConfigurationInstance)
            flash.message = message(code: "default.created.message", args: ['Action Configuration', "${actionConfigurationInstance?.value}"])
            flash.args = [actionConfigurationInstance.id]
            flash.defaultMessage = "ActionConfiguration ${actionConfigurationInstance.value} created"
            redirect(action: "list")
        } catch( grails.validation.ValidationException vx ) {
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
            render(view: "create", model: [actionConfigurationInstance: actionConfigurationInstance])

        }
        catch(Exception e) {
            e.printStackTrace(  )
            render(view: "create", model: [actionConfigurationInstance: actionConfigurationInstance])
        }
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def show() {
        def actionConfigurationInstance = ActionConfiguration.get(params.id)
        if (!actionConfigurationInstance) {
            flash.message = "actionConfiguration.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "ActionConfiguration not found with id ${params.id}"
            redirect(action: "list")
        }
        else {
            return [actionConfigurationInstance: actionConfigurationInstance]
        }
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def edit() {
        def actionConfigurationInstance = ActionConfiguration.get(params.id)
        if (!actionConfigurationInstance) {
            flash.message = "actionConfiguration.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "ActionConfiguration not found with id ${params.id}"
            redirect(action: "list")
        }
        else {
            return [actionConfigurationInstance: actionConfigurationInstance]
        }
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def update() {
        params.displayName = params.displayName?.trim()?.replaceAll("\\s{2,}", " ")
        params.value = params.value?.trim()?.replaceAll("\\s{2,}", " ")
        def actionConfigurationInstance = ActionConfiguration.get(params.id)
        try{
        if (actionConfigurationInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (actionConfigurationInstance.version > version) {

                    actionConfigurationInstance.errors.rejectValue("version", "actionConfiguration.optimistic.locking.failure", "Another user has updated this ActionConfiguration while you were editing")
                    render(view: "edit", model: [actionConfigurationInstance: actionConfigurationInstance])
                    return
                }
            }

            if (params.isEmailEnabled) {
                params.isEmailEnabled = true
            } else {
                params.isEmailEnabled = false
            }
            actionConfigurationInstance.properties = params
            if (actionConfigurationService.saveActionConfiguration(actionConfigurationInstance)) {
                cacheService.setActionConfigurationCache(actionConfigurationInstance)
                flash.message = message(code: "default.updated.message", args: ['Action Configuration', "${actionConfigurationInstance?.value}"])
                flash.args = [params.id]
                flash.defaultMessage = "ActionConfiguration ${params.id} updated"
                redirect(action: "list")
            }
            else {
                render(view: "edit", model: [actionConfigurationInstance: actionConfigurationInstance])
            }
        } else {
            flash.message = "actionConfiguration.not.found"
            flash.args = [ params.id ]
            flash.defaultMessage = "ActionConfiguration not found with id ${ params.id }"
            redirect( action: "edit", id: params.id )
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
            render( view: "edit", model: [ actionConfigurationInstance: actionConfigurationInstance ] )
        } catch( Exception e ) {
            e.printStackTrace()
            render( view: "edit", model: [ actionConfigurationInstance: actionConfigurationInstance ] )
        }
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def delete() {
        def actionConfigurationInstance = ActionConfiguration.get(params.id)
        if (actionConfigurationInstance) {
            try {
                actionConfigurationService.deleteActionConfiguration(actionConfigurationInstance)
                cacheService.removeActionConfiguration(actionConfigurationInstance.id)
                flash.message = message(code: "default.deleted.message", args: ['Action Configuration', "${actionConfigurationInstance.value}"])
                flash.args = [params.id]
                flash.defaultMessage = "ActionConfiguration ${params.id} deleted"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                log.error("DataIntegrityViolationException exception occured......", e)
                flash.args = [params.id]
                flash.error = "Cannot Delete: ActionConfiguration "+actionConfigurationInstance.value+" (${params.id}) is used in Signals"
                redirect(action: "show", id: params.id)
            } catch (Exception ex) {
                log.error("Exception occurred......", ex)
                flash.args = [params.id]
                flash.error = "Cannot Delete: ActionConfiguration "+actionConfigurationInstance.value+" (${params.id}) is used in Signals"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "actionConfiguration.not.found"
            flash.args = [params.id]
            flash.defaultMessage = "ActionConfiguration not found with id ${params.id}"
            redirect(action: "list")
        }
    }
}
