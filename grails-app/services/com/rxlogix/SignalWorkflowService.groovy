package com.rxlogix

import com.rxlogix.signal.SignalWorkflowRule
import com.rxlogix.signal.SignalWorkflowState
import com.rxlogix.signal.SystemConfig
import com.rxlogix.signal.ValidatedSignal
import grails.converters.JSON
import grails.gorm.transactions.Transactional

@Transactional
class SignalWorkflowService {

    def userService

    Map fetchAllPossibleTransitionsFromCurrentState(){
        Map states = [:]
        List workflowStates= fetchSignalWorkflowStates()
        workflowStates?.each{workflowState ->
            states.put(workflowState,findTargetStateFromIncomingState(workflowState).sort({it.toUpperCase()}))
        }
        return states
    }

    List findTargetStateFromIncomingState(String fromState){
        List result = []
        List<SignalWorkflowRule> signalWorkflowRuleList = SignalWorkflowRule.findAllByFromStateAndDisplay(fromState,true)
        signalWorkflowRuleList?.each {
            userService.getUser()?.groups?.each { group ->
                it.allowedGroups?.each { allowedGroup ->
                    if (allowedGroup.name.equalsIgnoreCase(group.name)) {
                        result.add(it.toState)
                    }
                }
            }
        }
        return result.unique { a, b -> a <=> b }
    }

    String defaultSignalWorkflowState(){
        SignalWorkflowState signalWorkflowState = SignalWorkflowState.findByDefaultDisplay(true)
        return signalWorkflowState?.displayName
    }

    String calculateDueInSignalWorkflowState(){
        SignalWorkflowState signalWorkflowState = SignalWorkflowState.findByDueInDisplay(true)
        return signalWorkflowState?.displayName
    }

    List fetchDispositionAllowedForWorkflowState(String workflowState){
        List<String> abbreviationList = []
        SignalWorkflowState signalWorkflowState = SignalWorkflowState.findByDisplayName(workflowState)
        signalWorkflowState?.allowedDispositions.each {
            abbreviationList.add(it.id as String)
        }
        return abbreviationList
    }

    Map fetchAllPossibleDispositionsForWorkflowState(){
        Map states = [:]
        List workflowStates= fetchSignalWorkflowStates()
        workflowStates?.each{workflowState ->
            states.put(workflowState,fetchDispositionAllowedForWorkflowState(workflowState))
        }
        return states
    }

    boolean isCalculateDueInChecked(String workflowState){
        SignalWorkflowState signalWorkflowState = SignalWorkflowState.findByDueInDisplay(true)
        if(workflowState == signalWorkflowState?.displayName){
            return true
        } else {
            return false
        }
    }

    void updateSignalWorkflowRule(SignalWorkflowRule signalWorkflowRule) {
        signalWorkflowRule.save()
    }

    void updateSignalWorkflowState(SignalWorkflowState signalWorkflowState) {
        signalWorkflowState.save()
    }

    def fetchSignalWorkflowStates() {
        def signalWorkflowStates

        signalWorkflowStates = SignalWorkflowState.findAll().collect() { it.displayName }
        signalWorkflowStates = JSON.parse(signalWorkflowStates.toString())

        return signalWorkflowStates
    }

    void updateSignalWorkflowRuleStates(String oldDisplayName, String newDisplayName) {

        List<SignalWorkflowRule> signalWorkflowRuleList = SignalWorkflowRule.findAllByFromState(oldDisplayName)
        signalWorkflowRuleList.each { signalWorkflowRule ->
            signalWorkflowRule.fromState = newDisplayName
            signalWorkflowRule.skipAudit=true
            signalWorkflowRule.save()
            log.info("Updated From state: (" + oldDisplayName + " --> " + newDisplayName + ") for rule: " + signalWorkflowRule.ruleName)
        }
        signalWorkflowRuleList.clear()

        signalWorkflowRuleList = SignalWorkflowRule.findAllByToState(oldDisplayName)
        signalWorkflowRuleList.each { signalWorkflowRule ->
            signalWorkflowRule.toState = newDisplayName
            signalWorkflowRule.save()
            log.info("Updated To state: (" + oldDisplayName + " --> " + newDisplayName + ") for rule: " + signalWorkflowRule.ruleName)
        }
    }

    void updateValidatedSignalWorkflowStates(String oldWorkflowStateName, String newWorkflowStateName) {

        List<ValidatedSignal> validatedSignalList = ValidatedSignal.findAllByWorkflowState(oldWorkflowStateName)

        if (validatedSignalList) {
            log.info("Found " + validatedSignalList.size() + " validated signals with workflow state " + oldWorkflowStateName)

            validatedSignalList.each { validatedSignal ->
                validatedSignal.workflowState = newWorkflowStateName
                validatedSignal.skipAudit=true
                validatedSignal.save()
            }

            List validatedSignalNames = validatedSignalList*.name
            log.info("Workflow state of Validated Signals  :" + validatedSignalNames.toString() + " updated from " + oldWorkflowStateName + " to " + newWorkflowStateName)
        } else {
            log.info("No validated signals with workflow state " + oldWorkflowStateName)
        }
    }

    void updateDateClosedBasedOnDispositionWorkflow(String initialDisplayName, String finalDisplayName) {
        SystemConfig systemConfig = SystemConfig.first()
        if (systemConfig.dateClosedWorkflow) {
            List dateClosedBasedOnWorkflowList = systemConfig.dateClosedWorkflow.split(',')
            List tempDateClosedBasedOnWorkflowList = []
            dateClosedBasedOnWorkflowList.each { String workflow ->
                if (workflow.equalsIgnoreCase(initialDisplayName)) {
                    workflow = finalDisplayName as String
                }
                tempDateClosedBasedOnWorkflowList << workflow
            }
            String dateClosedWorkflowStr = tempDateClosedBasedOnWorkflowList.join(',')
            systemConfig.dateClosedWorkflow = dateClosedWorkflowStr
            systemConfig.skipAudit = true
            systemConfig.save(flush: true)
        }
    }
}
