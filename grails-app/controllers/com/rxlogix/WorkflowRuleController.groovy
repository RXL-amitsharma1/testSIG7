package com.rxlogix

import com.rxlogix.config.Disposition
import com.rxlogix.config.DispositionRule
import com.rxlogix.config.EvdasAlert
import com.rxlogix.config.PVSState
import com.rxlogix.config.WorkflowRule
import com.rxlogix.signal.AdHocAlert
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.user.Group
import com.rxlogix.util.MiscUtil
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import org.springframework.dao.DataIntegrityViolationException

import javax.xml.bind.ValidationException

@Secured(["isAuthenticated()"])
class WorkflowRuleController {

    def workflowRuleService

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def index() {
    }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]


    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def list() {
        List<Map> list = DispositionRule.findAllByIsDeleted(false, [sort: "id", order: "desc"]).collect {
            it.toDto()
        }
        respond list, [formats: ['json']]
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def create() {
        DispositionRule dispositionRuleInstance = new DispositionRule()
        dispositionRuleInstance.properties = params
        Boolean isAdmin = SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_CONFIGURATION_CRUD")
        List<Disposition> availableDispositions = Disposition.findAllByDisplay(true)
        [workflowRuleInstance: dispositionRuleInstance, availableDispositions: availableDispositions, isAdmin: isAdmin]
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def save() {
        DispositionRule dispositionRuleInstance = new DispositionRule()

        try {
            params.name = params.name?.trim()?.replaceAll("\\s{2,}", " ")
            def matchingInstances = DispositionRule.findAllByNameAndIsDeleted(params.name, false)
            if (matchingInstances) {
                flash.error = message(code: 'com.rxlogix.config.DispositionRule.name.unique', args: ['Workflow Rule', "${params.name}"])
                List<Disposition> availableDispositions = Disposition.findAllByDisplay(true).sort({it.displayName})
                render(view: "create", model: [workflowRuleInstance: dispositionRuleInstance, availableDispositions: availableDispositions])
            }
            else {
                bindData(dispositionRuleInstance, params)
                if (workflowRuleService.saveWorkflowRule(dispositionRuleInstance)) {
                    flash.message = message(code: "default.created.message", args: ['Workflow Rule', "${dispositionRuleInstance.name}"])
                    flash.args = [dispositionRuleInstance.id]
                    flash.defaultMessage = message(code: "default.created.message", args: ['Workflow Rule', "${dispositionRuleInstance.name}"])
                    redirect(action: "index")
                } else {
                    List<Disposition> availableDispositions = Disposition.findAllByDisplay(true).sort({it.displayName})
                    render(view: "create", model: [workflowRuleInstance: dispositionRuleInstance, availableDispositions: availableDispositions])
                }
            }
        }
        catch( grails.validation.ValidationException vx ) {
            vx.printStackTrace()
            def customErrorMessages = MiscUtil.getCustomErrorMessageList(vx)
            if (customErrorMessages) {
                flash.error = MiscUtil.getCustomErrorMessageList(vx);
            }
            if (vx.toString()?.contains("targetDisposition.validator.invalid"))
                    {
                        if(flash.error){
                            flash.error << message(code: "com.rxlogix.config.DispositionRule.targetDisposition.validator.invalid")
                        } else {
                            flash.error = [message(code: "com.rxlogix.config.DispositionRule.targetDisposition.validator.invalid")]
                        }
                    }
            if (vx.toString()?.contains("DispositionRule.name.nullable"))
            {
                if(flash.error){
                    flash.error << message(code: "com.rxlogix.config.WorkflowRule.all.fields.required")
                }
                else{
                    flash.error = message(code: "com.rxlogix.config.WorkflowRule.all.fields.required")
                }
            }
            List<Disposition> availableDispositions = Disposition.findAllByDisplay(true)
            render(view: "create", model: [workflowRuleInstance: dispositionRuleInstance, availableDispositions: availableDispositions])
        }
        catch (Throwable e) {
            log.error(e.getMessage())
            List<Disposition> availableDispositions = Disposition.findAllByDisplay(true)
            render(view: "create", model: [workflowRuleInstance: dispositionRuleInstance, availableDispositions: availableDispositions])
        }
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def show() {
        def workflowRuleInstance = WorkflowRule.get(params.id)
        if (!workflowRuleInstance) {
            flash.message = message(code: "default.not.found.message", args: ['Workflow Rule', "${params.id}"])
            flash.args = [params.id]
            flash.defaultMessage = message(code: "default.not.found.message", args: ['Workflow Rule', "${params.id}"])
            redirect(action: "list")
        } else {
            return [workflowRuleInstance: workflowRuleInstance]
        }
    }


    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def edit(Long id) {
        DispositionRule dispositionRuleInstance = DispositionRule.get(id)
        if (!dispositionRuleInstance) {
            flash.message = message(code: "default.not.found.message", args: ['Workflow Rule', "${params.id}"])
            flash.args = [params.id]
            flash.defaultMessage = message(code: "default.not.found.message", args: ['Workflow Rule', "${params.id}"])
            redirect(action: "index")
        } else {
            List<Disposition> availableDispositions = Disposition.findAllByDisplay(true)
            Boolean isAdmin = SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_CONFIGURATION_CRUD")
            return [workflowRuleInstance: dispositionRuleInstance, availableDispositions: availableDispositions, isAdmin: isAdmin]
        }
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def update(Long id) {
        DispositionRule dispositionRuleInstance = DispositionRule.get(id)
        params.name = params.name?.trim()?.replaceAll("\\s{2,}", " ")
        List<Group> workflowGroups = params.workflowGroups ? Group.getAll(params.workflowGroups) : null
        List<Group> allowedUserGroups = params.allowedUserGroups ? Group.getAll(params.allowedUserGroups) : null
        try {
            if (dispositionRuleInstance) {
                dispositionRuleInstance.workflowGroups = []
                dispositionRuleInstance.allowedUserGroups = []
                bindData(dispositionRuleInstance, params)
                allowedUserGroups ? dispositionRuleInstance.addToAllowedUserGroups(allowedUserGroups) : false
                workflowGroups ? dispositionRuleInstance.addToWorkflowGroups(workflowGroups) : false
                if (params.version) {
                    def version = params.version.toLong()
                    if (dispositionRuleInstance.version > version) {

                        dispositionRuleInstance.errors.rejectValue("version", "workflowRule.optimistic.locking.failure",
                                "Another user has updated this WorkflowRule while you were editing")
                        render(view: "edit", model: [workflowRuleInstance: dispositionRuleInstance, pvsStateList: PVSState.findAllByDisplay(true)])
                        return
                    }
                }
                if (workflowRuleService.saveWorkflowRule(dispositionRuleInstance)) {
                    flash.message = message(code: "default.updated.message", args: ['Workflow Rule', "${dispositionRuleInstance.name}"])
                    flash.args = [params.id]
                    flash.defaultMessage = message(code: "default.updated.message", args: ['Workflow Rule', "${dispositionRuleInstance.name}"])
                    redirect(action: "index")
                } else {
                    List<Disposition> availableDispositions = Disposition.findAllByDisplay(true)
                    render(view: "edit", model: [workflowRuleInstance: dispositionRuleInstance, availableDispositions: availableDispositions])
                }
            } else {
                flash.message = message(code: "default.not.found.message", args: ['Workflow Rule', "${params.id}"])
                flash.args = [params.id]
                flash.defaultMessage = message(code: "default.not.found.message", args: ['Workflow Rule', "${params.id}"])
                redirect(action: "edit", id: params.id)
            }

        }
        catch( grails.validation.ValidationException vx ) {
            vx.printStackTrace()
            def customErrorMessages = MiscUtil.getCustomErrorMessageList(vx)
            if (customErrorMessages) {
                flash.error = MiscUtil.getCustomErrorMessageList(vx);
            }
            if (vx.toString()?.contains("targetDisposition.validator.invalid"))
            {
                if(flash.error){
                    flash.error << message(code: "com.rxlogix.config.DispositionRule.targetDisposition.validator.invalid")
                } else {
                    flash.error = [message(code: "com.rxlogix.config.DispositionRule.targetDisposition.validator.invalid")]
                }
            }
            if (vx.toString()?.contains("DispositionRule.name.nullable"))
            {
                if(flash.error){
                    flash.error << message(code: "com.rxlogix.config.WorkflowRule.all.fields.required")
                }
                else{
                    flash.error = message(code: "com.rxlogix.config.WorkflowRule.all.fields.required")
                }
            }
            List<Disposition> availableDispositions = Disposition.findAllByDisplay(true)
            render(view: "edit", model: [workflowRuleInstance: dispositionRuleInstance, pvsStateList: PVSState.findAllByDisplay(true),availableDispositions: availableDispositions])
        }
        catch (Throwable t) {
//            flash.error= message(code: "com.rxlogix.config.WorkflowRule.name.empty")
            log.error(t.getMessage())
            render(view: "edit", model: [workflowRuleInstance: dispositionRuleInstance, pvsStateList: PVSState.findAllByDisplay(true)])
        }
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def delete(Long id) {
        DispositionRule dispositionRuleInstance = DispositionRule.get(id)
        String currentWorkflowRuleName = dispositionRuleInstance.name
        Long currentSystemTime = System.currentTimeMillis()
        if (dispositionRuleInstance) {
            try {
                dispositionRuleInstance.isDeleted = true
                dispositionRuleInstance.save(failOnError: true, flush: true)
                flash.message = message(code: "default.deleted.message", args: ['Workflow Rule', "${currentWorkflowRuleName}"])
                flash.args = [params.id]
                flash.defaultMessage = message(code: "default.deleted.message", args: ['Workflow Rule', "${currentWorkflowRuleName}"])
                redirect(action: "index")
            } catch (DataIntegrityViolationException e) {
                flash.message = message(code: "default.not.deleted.message", args: ['Workflow Rule'])
                flash.args = [params.id]
                flash.defaultMessage = message(code: "default.not.deleted.message.simple", args: ['Workflow Rule'])
                redirect(action: "show", id: params.id)
            }
        } else {
            flash.message = message(code: "default.not.found.message", args: ['Workflow Rule', "${params.id}"])
            flash.args = [params.id]
            flash.defaultMessage = message(code: "default.not.found.message", args: ['Workflow Rule', "${params.id}"])
            redirect(action: "index")
        }
    }

    def disableCheck(Long id) {
        DispositionRule dispositionRule = DispositionRule.findById(id)
        Boolean alertsIncomingDisposition

        if (dispositionRule) {
            Disposition incomingDisposition = dispositionRule.incomingDisposition
            Disposition targetDisposition = dispositionRule.targetDisposition

            alertsIncomingDisposition = AggregateCaseAlert.countByDisposition(incomingDisposition) as Boolean

            if (!alertsIncomingDisposition) {
                alertsIncomingDisposition = SingleCaseAlert.countByDisposition(incomingDisposition) as Boolean
            }
            if (!alertsIncomingDisposition) {
                alertsIncomingDisposition = EvdasAlert.countByDisposition(incomingDisposition) as Boolean
            }
            if (!alertsIncomingDisposition) {
                alertsIncomingDisposition = AdHocAlert.countByDisposition(incomingDisposition) as Boolean
            }

            Boolean rulesIncomeState = DispositionRule.countByIncomingDisposition(incomingDisposition) > 0
            Boolean rulesTargetState = DispositionRule.countByTargetDisposition(targetDisposition) > 0

            render(['alertsIncomeState': alertsIncomingDisposition, 'rulesIncomeState': rulesIncomeState, 'rulesTargetState': rulesTargetState] as JSON)
        }
    }
}
