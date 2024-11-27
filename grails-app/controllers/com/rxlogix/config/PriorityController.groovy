package com.rxlogix.config

import com.rxlogix.util.MiscUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["isAuthenticated()"])
class PriorityController {

    def priorityService
    def cacheService

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def index() {
        redirect(action: "list", params: params)
    }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def list() {
        params.max = Math.min(params.max ? params.max.toInteger() : 10, 100)
        [priorityInstanceList: Priority.list(params), priorityInstanceTotal: Priority.count()]
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def create() {
        def priorityInstance = new Priority()
        priorityInstance.properties = params
        return [priorityInstance: priorityInstance]
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def save() {
        params.value = params.value?.trim()?.replaceAll("\\s{2,}", " ")
        params.displayName = params.displayName?.trim()?.replaceAll("\\s{2,}", " ")
        params.iconClass = params.iconClass?.trim()?.replaceAll("\\s{2,}", " ")
        def priorityInstance = new Priority(params)
        try {
            if(!params.reviewPeriod){
                flash.error = message(code: "com.rxlogix.config.Priority.reviewPeriod.typeMismatch.error")
                render(view: "create", model: [priorityInstance: priorityInstance ,  dispositionConfigs : dispositionList])
                return
            }
            //9999 years [oracle max year limit]
            if(params.reviewPeriod.toInteger() > 3652134){
                flash.message = message(code: "app.label.priority.change.sql.year.exceeded.error");
                render(view: "create", model: [priorityInstance: priorityInstance ,  dispositionConfigs : dispositionList])
                return
            }
            else {
                List dispositionList = JSON.parse(params.dispositions)
                String newConfig = dispositionList.collect {it->
                    "${it?.displayName}:${it?.reviewPeriod}"
                }.join(',')
                priorityInstance.customAuditProperties = ["Disposition Configs": ["oldValue":"", "newValue": newConfig]]
                priorityInstance.lastUpdated = new Date()
                priorityService.savePriority(priorityInstance)
                priorityService.saveDispositionConfig(params , priorityInstance)
                log.info(priorityInstance.dispositionConfigs.size().toString())
                cacheService.updatePriorityCache(priorityInstance)
                flash.message = message(code: "default.created.message", args: ['Priority', "${priorityInstance.value}"])
                flash.args = [priorityInstance.id]
                flash.defaultMessage = "Priority ${priorityInstance.value} created"
                redirect(action: "list")
            }
        }
        catch( grails.validation.ValidationException vx ) {
            vx.printStackTrace()
            def customErrorMessages = MiscUtil.getCustomErrorMessageList(vx)
            if(customErrorMessages)
            {
                flash.error = MiscUtil.getCustomErrorMessageList(vx);
            }
            if (vx.toString()?.contains("Priority.value.nullable") || vx.toString()?.contains("Priority.displayName.nullable"))
            {
                if(customErrorMessages) {
                    flash.error << message(code: "com.rxlogix.config.Priority.all.fields.required")
                }
                else {
                    flash.error = message(code: "com.rxlogix.config.Priority.all.fields.required")

                }
            }
            List dispositionList = JSON.parse(params.dispositions).groupBy{ it.order }.collect { key, value -> value}
            render(view: "create", model: [priorityInstance: priorityInstance ,  dispositionConfigs : dispositionList])
            return
        }
        catch (Throwable e) {
            List dispositionList = JSON.parse(params.dispositions).groupBy{ it.order }.collect { key, value -> value}
            log.error(e.getMessage())
            render(view: "create", model: [priorityInstance: priorityInstance ,  dispositionConfigs : dispositionList])
            return
        }
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def show() {
        def priorityInstance = Priority.get(params.id)
        if (!priorityInstance) {
            flash.message = message(code: "default.not.found.message", args: ['Priority', "${params.id}"])
            flash.args = [params.id]
            flash.defaultMessage = "Priority not found with id ${params.id}"
            redirect(action: "list")
        } else {
            return [priorityInstance: priorityInstance]
        }
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def edit() {
        def priorityInstance = Priority.get(params.id)
        if (!priorityInstance) {
            flash.message = message(code: "default.not.found.message", args: ['Priority', "${params.id}"])
            flash.args = [params.id]
            flash.defaultMessage = "Priority not found with id ${params.id}"
            redirect(action: "list")
        } else {
            return [priorityInstance: priorityInstance , dispositionConfigs : priorityService.fetchDispositionConfigList(priorityInstance)]
        }
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def update() {
        params.value = params.value?.trim()?.replaceAll("\\s{2,}", " ")
        params.displayName = params.displayName?.trim()?.replaceAll("\\s{2,}", " ")
        params.iconClass = params.iconClass?.trim()?.replaceAll("\\s{2,}", " ")
        Priority defaultPriority = Priority.findByDefaultPriority(true)
        def priorityInstance = Priority.get(params.id)
        try {
            if (priorityInstance) {
                if (params.version) {
                    def version = params.version.toLong()
                    if (priorityInstance.version > version) {
                        priorityInstance.errors.rejectValue("version", "priority.optimistic.locking.failure", "Another user has updated this Priority while you were editing")
                        render(view: "edit", model: [priorityInstance: priorityInstance])
                        return
                    }
                }
                if(!params.reviewPeriod){
                    flash.error = message(code: "com.rxlogix.config.Priority.reviewPeriod.typeMismatch.error")
                    redirect(action: "edit", id: priorityInstance.id)
                }
                //9999 years [oracle max year limit]
                if(params.reviewPeriod.toInteger() > 3652134){
                    flash.message = message(code: "app.label.priority.change.sql.year.exceeded.error");
                    redirect(action: "edit", id: priorityInstance.id)
                }
                else {
                    priorityInstance.properties = params
                    if (!priorityInstance.defaultPriority && priorityInstance.id == defaultPriority?.id) {
                        flash.error = message(code: "error.default.priority")
                        redirect(action: "edit", id: priorityInstance.id)
                    } else {
                        String prevConfig = priorityService.fetchDispositionConfigListForAudit(priorityInstance)
                        List dispositionList = JSON.parse(params.dispositions)
                        String newConfig = dispositionList.collect {it->
                            "${it?.displayName}:${it?.reviewPeriod}"
                        }.join(',')
                        priorityInstance.customAuditProperties = ["Disposition Configs": ["oldValue":prevConfig, "newValue": newConfig]]
                        priorityInstance.lastUpdated = new Date()
                        priorityService.savePriority(priorityInstance)
                        priorityService.saveDispositionConfig(params, priorityInstance)
                        log.info(priorityInstance.dispositionConfigs.size().toString())
                        cacheService.updatePriorityCache(priorityInstance)
                        flash.message = message(code: "default.updated.message", args: ['Priority', "${priorityInstance.value}"])
                        flash.args = [priorityInstance.id]
                        flash.defaultMessage = "Priority ${priorityInstance.value} updated"
                        redirect(action: "list")
                    }
                }
            } else {
                flash.message = message(code: "default.not.found.message", args: ['Priority', "${params.id}"])
                flash.args = [params.id]
                flash.defaultMessage = "Priority not found with id ${params.id}"
                redirect(action: "edit", id: params.id)
            }
        }
        catch( grails.validation.ValidationException vx ) {
            vx.printStackTrace()
            flash.error = message(code: "com.rxlogix.config.Priority.all.fields.required")
            List dispositionList = JSON.parse(params.dispositions).groupBy{ it.order }.collect { key, value -> value}
            render(view: "edit", model: [priorityInstance: priorityInstance , dispositionConfigs : dispositionList])

        }
        catch (Throwable t) {
            List dispositionList = JSON.parse(params.dispositions).groupBy{ it.order }.collect { key, value -> value}
            log.error(t.getMessage())
            render(view: "edit", model: [priorityInstance: priorityInstance , dispositionConfigs : dispositionList])
        }
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def delete() {
        def priorityInstance = Priority.get(params.id)
        if (priorityInstance) {
            try {
                priorityInstance.delete()
                cacheService.deletePriorityCache(priorityInstance)
                flash.message = message(code: "default.deleted.message", args: ['Priority', "${priorityInstance.value}"])
                flash.args = [params.id]
                flash.defaultMessage = "Priority ${params.id} deleted"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = message(code: "default.not.deleted.message", args: ['Priority'])
                flash.args = [params.id]
                flash.defaultMessage = "Priority ${params.id} could not be deleted"
                redirect(action: "show", id: params.id)
            }
        } else {
            flash.message = message(code: "default.not.found.message", args: ['Priority', "${params.id}"])
            flash.args = [params.id]
            flash.defaultMessage = "Priority not found with id ${params.id}"
            redirect(action: "list")
        }
    }

}
