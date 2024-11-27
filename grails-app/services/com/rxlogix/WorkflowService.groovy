package com.rxlogix

import com.rxlogix.config.Disposition
import com.rxlogix.config.PVSState
import com.rxlogix.config.Priority
import com.rxlogix.config.WorkflowRule
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.gorm.transactions.Transactional

@Transactional
class WorkflowService {
    def userService

    def workflowStates() {
        def states = PVSState.where {visible: true}.list()

        states
    }

    def priorities() {
        def priorities = Priority.findAllByDisplay(true)
        priorities
    }

    def dispositions() {
        def dispositions = Disposition.findAll()

        dispositions
    }

    def getAvailableWorkflowStates(PVSState currentState, Set<Group> groups) {
        def qualifiedWorkflowRules = groups ?
                WorkflowRule.findAllByDisplayAndIsDeleted(true, false).findAll {
                currentState == it.incomeState && (!it.allowedGroups || it.allowedGroups.intersect(groups))
            }
            :
                WorkflowRule.findAllByIsDeleted(false).findAll {
                currentState == it.incomeState && !it.allowedGroups
            }

        //check if user is a member of at least one group.
        qualifiedWorkflowRules.collect {
            [
                    "approvalRequired" :  it.approveRequired,
                    "value" : it.targetState?.value,
                    "displayName" : it.targetState?.displayName,
                    "dispositions" : (it.allowedDispositions)?.toSet()?.collect {dispositon ->
                        [
                                "value" : dispositon.value,
                                "displayName" : dispositon.displayName,
                                "isValidatedConfirmed" : dispositon.validatedConfirmed
                        ]
                    }
            ]
        }
    }

    def getSignalsForSignal(signal, currentState, groups) {

        def qualifiedWorkflowRules = groups ?
                WorkflowRule.findAllByIsDeleted(false).findAll {
                    it.signalRule && (it.topicCategories?.intersect(signal.topicCategories)?.size() > 0) && (currentState == it.incomeState) &&
                            (!it.allowedGroups || it.allowedGroups.intersect(groups))
                }
                :
                WorkflowRule.findAllByIsDeleted(false).findAll {
                    it.signalRule && (it.topicCategories?.intersect(signal.topicCategories)?.size() > 0) && (currentState == it.incomeState) &&
                            !it.allowedGroups
                }

        //check if user is a member of at least one group.
        qualifiedWorkflowRules.collect {
            [
                    "approvalRequired" :  it.approveRequired,
                    "value" : it.targetState?.value,
                    "displayName" : it.targetState?.displayName,
                    "dispositions" : (it.allowedDispositions)?.toSet()?.collect {dispositon ->
                        [
                                "value" : dispositon.value,
                                "displayName" : dispositon.displayName,
                                "isValidatedConfirmed" : dispositon.validatedConfirmed
                        ]
                    }
            ]
        }

    }

    def getDispositions(PVSState currentState) {
        def qualifiedWorkflowRules = WorkflowRule.findAllByIncomeStateAndIsDeleted(currentState, false)
        def dispositions = [] as Set
        qualifiedWorkflowRules.each {
            dispositions.add(it.allowedDispositions)
        }
        dispositions = dispositions.flatten()
        dispositions
    }

    def getWorkflowStates() {
        def workflowRuleCriteria = WorkflowRule.createCriteria()
        def rules = workflowRuleCriteria.list {
            sizeGt('allowedDispositions', 0)
        }
        def states = []
        rules.each {
            states.add(it.incomeState)
        }
        states = states.flatten()
        states
    }

    def allUsers() { User.findAll() }
}
