package com.rxlogix

import com.rxlogix.config.PVSState
import com.rxlogix.config.WorkflowRule
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import groovy.time.TimeCategory

import java.text.SimpleDateFormat

@Secured(["isAuthenticated()"])
class PVSStateController {

    def PVSStateService
    def cacheService

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def index() {
    }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def list() {
        def list = PVSState.list(sort:"id", order: "desc").collect{
            it.toDto()
        }
        respond list, [formats: ['json']]
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def create() {
        def pvsStateInstance = new PVSState()
        pvsStateInstance.properties = params
        return [PVSStateInstance: pvsStateInstance]
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def save() {
        def pvsStateInstance = new PVSState(params)
        try {
            if (PVSStateService.saveState(pvsStateInstance)) {

                //Now update the cache
                cacheService.updateWorkflowCache(pvsStateInstance)

                flash.message = message(code:"default.created.message", args:['Workflow State',"${pvsStateInstance.value}"])
                flash.args = [pvsStateInstance.id]
                flash.defaultMessage = "PVSState ${pvsStateInstance.value} created"
                redirect(action: "index")

            } else {
                render(view: "create", model: [PVSStateInstance: pvsStateInstance])
            }
        }
        catch(Throwable e) {
            log.error(e.getMessage())
            render(view: "create", model: [PVSStateInstance: pvsStateInstance])
        }
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def show() {
        def pvsStateInstance = PVSState.get(params.id)
        if (!pvsStateInstance) {
            flash.message = message(code:"default.not.found.message",args:['Workflow State',"${params.id}"])
            flash.args = [params.id]
            flash.defaultMessage = "PVSState not found with id ${params.id}"
            redirect(action: "index")
        }
        else {
            return [PVSStateInstance: pvsStateInstance]
        }
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def edit() {
        def pvsStateInstance = PVSState.get(params.id)
        if (!pvsStateInstance) {
            flash.message = message(code:"default.not.found.message",args:['Workflow State',"${params.id}"])
            flash.args = [params.id]
            flash.defaultMessage = "PVSState not found with id ${params.id}"
            redirect(action: "index")
        }
        else {
            return [PVSStateInstance: pvsStateInstance]
        }
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def update() {
        def pvsStateInstance = PVSState.get(params.id)
        try {
            if (pvsStateInstance) {
                if (params.version) {
                    def version = params.version.toLong()
                    if (pvsStateInstance.version > version) {

                        pvsStateInstance.errors.rejectValue("version",
                                "PVSState.optimistic.locking.failure",
                                "Another user has updated this PVSState while you were editing")
                        render(view: "edit", model: [PVSStateInstance: pvsStateInstance])
                        return
                    }
                }
                if(!params.display){
                    def workflowRules = WorkflowRule.withCriteria {
                        projections {
                            count()
                        }
                        and{
                            eq('isDeleted', false)
                            or{
                                eq('incomeState', pvsStateInstance)
                                eq('targetState', pvsStateInstance)
                            }
                        }
                    }
                    if(workflowRules[0] > 0){
                        flash.error = message(code:"app.label.alert.workflow.rule",args:['Workflow State',"${params.id}"])
                        flash.args = [params.id]
                        render(view: "edit", model: [PVSStateInstance: pvsStateInstance])
                        return
                    }
                }

                pvsStateInstance.properties = params

                if (PVSStateService.saveState(pvsStateInstance)) {

                    //Now update the cache
                    cacheService.updateWorkflowCache(pvsStateInstance)

                    flash.message = message(code:"default.updated.message",args:['Workflow State',"${pvsStateInstance.value}"])
                    flash.args = [params.id]
                    flash.defaultMessage = "PVSState ${params.id} updated"
                    redirect(action: "index")
                }
                else {
                    render(view: "edit", model: [PVSStateInstance: pvsStateInstance])
                }
            }
            else {
                flash.message = message(code:"default.not.found.message",args:['Workflow State',"${params.id}"])
                flash.args = [params.id]
                flash.defaultMessage = "PVSState not found with id ${params.id}"
                redirect(action: "edit", id: params.id)
            }
        } catch (Throwable t) {
            log.error(t.getMessage())
            render(view: "edit", model: [PVSStateInstance: pvsStateInstance])
        }

    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def delete() {
        def pvsStateInstance = PVSState.get(params.id)
        if (pvsStateInstance) {
            try {
                pvsStateInstance.delete()
                cacheService.deleteWorkflowCache(pvsStateInstance)
                flash.message = message(code:"default.deleted.message",args:['Workflow State',"${pvsStateInstance.value}"])
                flash.args = [params.id]
                flash.defaultMessage = "PVSState ${params.id} deleted"
                redirect(action: "index")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = message(code:"default.not.deleted.message",args:['Workflow State'])
                flash.args = [params.id]
                flash.defaultMessage = "PVSState ${params.id} could not be deleted"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = message(code:"default.not.found.message",args:['Workflow State',"${params.id}"])
            flash.args = [params.id]
            flash.defaultMessage = "PVSState not found with id ${params.id}"
            redirect(action: "index")
        }
    }

    def fetchReviewDate(){
        String reviewDateStr = message(code: "com.workflow.state.review.state.notset")
        if (params.pvsState) {
            PVSState pvsState = PVSState.findByDisplayName(params.pvsState)
            if (pvsState && pvsState.reviewPeriod) {
                use(TimeCategory) {
                    Date reviewDate = new Date() + pvsState.reviewPeriod.days
                    SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
                    reviewDateStr = formatter.format(reviewDate)
                }
            }
        }
        def renderDate = ['reviewDate':reviewDateStr]
        render renderDate as JSON
    }
}
