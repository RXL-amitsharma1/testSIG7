package com.rxlogix

import com.rxlogix.audit.AuditTrail
import com.rxlogix.config.Disposition
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.signal.SignalWorkflowRule
import com.rxlogix.signal.SignalWorkflowState
import com.rxlogix.signal.SystemConfig
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.AuditLogConfigUtil
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders

@Secured(["isAuthenticated()"])
class SignalWorkflowController {

    def signalWorkflowService
    def signalAuditLogService

    static defaultAction = "signalWorkflowRule"

    def signalWorkflowRule() {
        Boolean isEditingAllowed = SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_CONFIGURATION_CRUD")
        boolean enableWorkflow = false
        enableWorkflow = SystemConfig.first()?.enableSignalWorkflow
        render(view: "signalWorkFlowRule",model: [enableWorkflow: enableWorkflow, isEditingAllowed: isEditingAllowed])
    }

    def createSignalWorkflowRule() {
        List signalWorkflowStates = signalWorkflowService.fetchSignalWorkflowStates()
        render(view: "createSignalWorkflowRule", model: [workflowStatesSignal: signalWorkflowStates?.sort()])
    }

    def fetchSignalWorkflowRule() {
        List signalWorkflowRulesList = SignalWorkflowRule.findAll().sort { it.dateCreated }.collect {
            [ruleName: it.ruleName, description: it.description, fromState: it.fromState, id: it.id,
             toState : it.toState, display: it.display, allowedGroups: it.allowedGroups?.collect { it.name }?.join(',')]
        }
        render([ruleList: signalWorkflowRulesList] as JSON)
    }

    def editSignalWorkflowState() {
        SignalWorkflowState signalWorkflowStateInstance = SignalWorkflowState.get(params.id)
        render(view: "editSignalWorkflowState", model: [signalWorkflowStateInstance: signalWorkflowStateInstance])
    }

    def editSignalWorkflowRule() {
        SignalWorkflowRule signalWorkflowRuleInstance = SignalWorkflowRule.get(params.id)
        List signalWorkflowStates = signalWorkflowService.fetchSignalWorkflowStates()
        render(view: "editSignalWorkflowRule", model: [workflowStatesSignal: signalWorkflowStates, signalWorkflowRuleInstance: signalWorkflowRuleInstance])
    }

    def saveWorkflowRule() {
        params.ruleName = params.ruleName?.trim()?.replaceAll("\\s{2,}", " ")
        SignalWorkflowRule signalWorkflowRule = new SignalWorkflowRule()
        try {
            bindData(signalWorkflowRule, params)
            signalWorkflowRule.dateCreated = new Date()
            signalWorkflowRule.display = params.display ? true : false

            if (SignalWorkflowRule.findByRuleName(params?.ruleName)) {
                flash.error = message(code: "signal.workflow.label.unique.error.save")
                List signalWorkflowStates = signalWorkflowService.fetchSignalWorkflowStates()
                render(view: "createSignalWorkflowRule", model: [workflowStatesSignal: signalWorkflowStates, signalWorkflowRuleInstance: signalWorkflowRule])
                return
            }
            //Added to fix bug-52913
            if (signalWorkflowRule.fromState == signalWorkflowRule.toState) {
                flash.error = message(code: "signal.workflow.label.error.save")
                List signalWorkflowStates = signalWorkflowService.fetchSignalWorkflowStates()
                render(view: "createSignalWorkflowRule", model: [workflowStatesSignal: signalWorkflowStates, signalWorkflowRuleInstance: signalWorkflowRule])
                return
            }
            List groups = []
            if (params.allowedGroups) {
                if (params.allowedGroups instanceof String) {
                    groups = [params.allowedGroups]
                } else {
                    groups = params.allowedGroups
                }
                groups?.each { def groupId ->
                    Group allowedGroup = Group.findById(groupId, [null: null])
                    signalWorkflowRule.addToAllowedGroups(allowedGroup)
                }
            }
            signalWorkflowRule.save(flush: true)
            flash.message = message(code: "default.created.message", args: ['Signal Workflow Rule', "${signalWorkflowRule.ruleName}"])
            flash.args = [signalWorkflowRule.id]
            flash.defaultMessage = message(code: "default.created.message", args: ['Signal Workflow Rule', "${signalWorkflowRule.ruleName}"])
            redirect(action: "signalWorkflowRule")
        } catch (Exception ex) {
            log.error(ex.printStackTrace())
            List signalWorkflowStates = signalWorkflowService.fetchSignalWorkflowStates()
            render(view: "createSignalWorkflowRule", model: [workflowStatesSignal: signalWorkflowStates, signalWorkflowRuleInstance: signalWorkflowRule])
        }
    }

    def updateWorkflowRule() {
        List groups = []
        params.ruleName = params.ruleName?.trim()?.replaceAll("\\s{2,}", " ")
        SignalWorkflowRule signalWorkflowRule = SignalWorkflowRule.get(params.id)
        try {
            bindData(signalWorkflowRule, params)
            def existingRule = SignalWorkflowRule.findByRuleName(params?.ruleName)
            if(existingRule && existingRule?.id != signalWorkflowRule?.id){
                if (SignalWorkflowRule.findByRuleName(params?.ruleName)) {
                    flash.error = message(code: "signal.workflow.label.unique.error.save")
                    List signalWorkflowStates = signalWorkflowService.fetchSignalWorkflowStates()
                    render(view: "editSignalWorkflowRule", model: [workflowStatesSignal: signalWorkflowStates, signalWorkflowRuleInstance: signalWorkflowRule, id: params.id])
                    return
                }
            }
            //Added to fix bug-52913
            if (signalWorkflowRule.fromState == signalWorkflowRule.toState) {
                flash.error = message(code: "signal.workflow.label.error.save")
                List signalWorkflowStates = signalWorkflowService.fetchSignalWorkflowStates()
                render(view: "editSignalWorkflowRule", model: [workflowStatesSignal: signalWorkflowStates, signalWorkflowRuleInstance: signalWorkflowRule, id: params.id])
                return
            }
            if (params.allowedGroups) {
                if (params.allowedGroups instanceof String) {
                    groups = [params.allowedGroups]
                } else {
                    groups = params.allowedGroups
                }
                groups?.each { def groupId ->
                    Group allowedGroup = Group.findById(groupId, [null: null])
                    signalWorkflowRule.addToAllowedGroups(allowedGroup)
                }
            } else {
                signalWorkflowRule.allowedGroups = null
            }
            signalWorkflowRule.display = params.display ? true : false
            signalWorkflowService.updateSignalWorkflowRule(signalWorkflowRule)
            flash.message = message(code: "default.updated.message", args: ['Signal Workflow Rule', "${signalWorkflowRule.ruleName}"])
            flash.args = [signalWorkflowRule.id]
            flash.defaultMessage = message(code: "default.updated.message", args: ['Signal Workflow Rule', "${signalWorkflowRule.ruleName}"])
            redirect(action: "signalWorkflowRule")
        } catch (Exception ex) {
            log.error(ex.printStackTrace())
            List signalWorkflowStates = signalWorkflowService.fetchSignalWorkflowStates()
            render(view: "editSignalWorkflowRule", model: [workflowStatesSignal: signalWorkflowStates, signalWorkflowRuleInstance: signalWorkflowRule, id: params.id])
        }
    }

    def deleteWorkflowRule() {
        SignalWorkflowRule signalWorkflowRule = SignalWorkflowRule.get(params.id)
        try {
            if (signalWorkflowRule) {
                if (signalWorkflowRule.allowedGroups != []) {
                    signalWorkflowRule.allowedGroups?.id?.each { Long groupId ->
                        Group allowedGroup = Group.findById(groupId, [null: null])
                        signalWorkflowRule.removeFromAllowedGroups(allowedGroup)
                    }
                }
                signalWorkflowRule.delete(failOnError: true, flush: true)
                flash.message = message(code: "default.deleted.message", args: ['Signal Workflow Rule', "${signalWorkflowRule.ruleName}"])
                flash.args = [params.id]
                flash.defaultMessage = message(code: "default.deleted.message", args: ['Signal Workflow Rule', "${signalWorkflowRule.ruleName}"])
                redirect(action: "signalWorkflowRule")
            } else {
                flash.message = message(code: "default.not.found.message", args: ['Signal Workflow Rule', "${params.id}"])
                flash.args = [params.id]
                flash.defaultMessage = message(code: "default.not.found.message", args: ['Signal Workflow Rule', "${params.id}"])
                redirect(action: "signalWorkflowRule")
            }
        } catch (Exception ex) {
            log.error(ex.printStackTrace())
            List signalWorkflowStates = signalWorkflowService.fetchSignalWorkflowStates()
            render(view: "editSignalWorkflowRule", model: [workflowStatesSignal: signalWorkflowStates, signalWorkflowRuleInstance: signalWorkflowRule, id: params.id])
        }
    }

    def signalWorkflowList() {
        Boolean isEditingAllowed = SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN,ROLE_CONFIGURATION_CRUD")
        boolean enableWorkflow = false
        enableWorkflow = SystemConfig.first()?.enableSignalWorkflow
        render(view: "signalWorkflowList",model: [enableWorkflow: enableWorkflow, isEditingAllowed: isEditingAllowed])
    }

    def fetchSignalWorkflowState() {
        List signalWorkflowStatesList = SignalWorkflowState.findAll().collect {
            [id            : it.id, value: it.value, displayName: it.displayName, allowedDispositions: it.allowedDispositions?.collect { it.displayName }?.join(','),
             defaultDisplay: it.defaultDisplay, dueInDisplay: it.dueInDisplay]
        }
        render([stateList: signalWorkflowStatesList] as JSON)
    }

    def updateWorkflowState() {
        SignalWorkflowState signalWorkflowState = SignalWorkflowState.get(params.id)
        SignalWorkflowState signalDefaultWorkflowState = SignalWorkflowState.findByDefaultDisplay(true)
        SignalWorkflowState signalDueInWorkflowState = SignalWorkflowState.findByDueInDisplay(true)
        try {
            if (params.displayName.trim() != signalWorkflowState.displayName) {
                signalWorkflowService.updateSignalWorkflowRuleStates(signalWorkflowState.displayName, params.displayName as String)
                signalWorkflowService.updateValidatedSignalWorkflowStates(signalWorkflowState.displayName, params.displayName as String)
                signalWorkflowService.updateDateClosedBasedOnDispositionWorkflow(signalWorkflowState.displayName, params.displayName)
            }
            bindData(signalWorkflowState, params)
            signalWorkflowState.defaultDisplay = params.defaultDisplay ? true : false
            signalWorkflowState.dueInDisplay = params.dueInDisplay ? true : false
            List dispositions = []
            if (params.allowedDispositions) {
                if (params.allowedDispositions instanceof String) {
                    dispositions = [params.allowedDispositions]
                } else {
                    dispositions = params.allowedDispositions
                }
                dispositions?.each { def dispositionId ->
                    Disposition allowedDisposition = Disposition.findById(dispositionId)
                    signalWorkflowState.addToAllowedDispositions(allowedDisposition)
                }
            } else {
                signalWorkflowState.allowedDispositions = null
            }
            if (signalWorkflowState.defaultDisplay && signalDefaultWorkflowState && signalWorkflowState?.id != signalDefaultWorkflowState?.id) {
                signalDefaultWorkflowState.defaultDisplay = false
                signalWorkflowService.updateSignalWorkflowState(signalDefaultWorkflowState)
            } else {
                if (!signalWorkflowState.defaultDisplay && signalWorkflowState?.id == signalDefaultWorkflowState?.id) {
                    flash.error = message(code: "error.default.state")
                    redirect(action: "editSignalWorkflowState",id: params.id)
                }
            }
            if (signalWorkflowState.dueInDisplay && signalDueInWorkflowState && signalWorkflowState?.id != signalDueInWorkflowState?.id) {
                signalDueInWorkflowState.dueInDisplay = false
                signalWorkflowService.updateSignalWorkflowState(signalDueInWorkflowState)
            }
            if (!(!signalWorkflowState.defaultDisplay && signalWorkflowState?.id == signalDefaultWorkflowState?.id)) {
                signalWorkflowService.updateSignalWorkflowState(signalWorkflowState)
                flash.message = message(code: "default.updated.message", args: ['Signal Workflow State', "${signalWorkflowState.value}"])
                flash.args = [signalWorkflowState.id]
                flash.defaultMessage = message(code: "default.updated.message", args: ['Signal Workflow State', "${signalWorkflowState.value}"])
                redirect(action: "signalWorkflowList")
            }
        } catch (Exception ex) {
            log.error(ex.printStackTrace())
            render(view: "editSignalWorkflowState", model: [signalWorkflowStateInstance: signalWorkflowState])
        }
    }

    def enableSignalWorkflow() {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            SystemConfig systemConfig = SystemConfig.first()
            def oldValue = systemConfig?.enableSignalWorkflow ? "Yes" : "No"
            if(systemConfig.enableEndOfMilestone){
                responseDTO.status= false
                responseDTO.message = "enableEndOfMilestone"
            }else if (params.enableWorkflow?.toBoolean()) {
                systemConfig.enableSignalWorkflow = true
            } else {
                systemConfig.enableSignalWorkflow = false
            }
            responseDTO.data = systemConfig.enableSignalWorkflow
            systemConfig.skipAudit=true
            systemConfig.save(flush: true)
            signalAuditLogService.createAuditLog([
                    entityName : "systemConfig",
                    moduleName : "Signal Workflow Rule",
                    category   : AuditTrail.Category.UPDATE.toString(),
                    entityValue: "Enable Signal Workflow Configuration",
                    description: "Enable Signal Workflow Status"
            ] as Map, [[propertyName: "Enable", oldValue: oldValue, newValue: systemConfig?.enableSignalWorkflow ? "Yes" : "No"]] as List)
            AuditLogConfigUtil.populateLabelConfigForAudit("statusHistoryStatusLabel")

        } catch (Exception ex) {
            log.error(ex.printStackTrace())
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.error.enable.signal.workflow")
        }
        render(responseDTO as JSON)
    }
}
