package com.rxlogix

import com.rxlogix.config.Disposition
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.SignalWorkflowRule
import com.rxlogix.signal.SignalWorkflowState
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
class SignalWorkflowServiceSpec extends Specification implements ServiceUnitTest<SignalWorkflowService> {
    User user
    Group wfGroup
    Group userGroup
    Disposition disposition
    Disposition defaultDisposition
    Disposition defaultSignalDisposition
    Disposition autoRouteDisposition
    SignalWorkflowRule signalWorkflowRule
    SignalWorkflowRule signalWorkflowRuleNew
    SignalWorkflowState signalWorkflowState
    SignalWorkflowState signalWorkflowStateNew
    List<String> workflowStates

    def setup() {
        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "VO")
        disposition.save(flush: true, failOnError: true)
        defaultSignalDisposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "VO")
        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")

        [defaultDisposition, autoRouteDisposition].collect { it.save(failOnError: true) }

        wfGroup = new Group(name: "Default",
                groupType: GroupType.WORKFLOW_GROUP,
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                defaultSignalDisposition: disposition,
                autoRouteDisposition: autoRouteDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(validate: false)

        //Save the  user
        user = new User(id: '1', username: 'test_user', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.addToGroups(wfGroup)
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.save(flush: true)

        signalWorkflowState = new SignalWorkflowState(value: "Safety Observation Validation",displayName: "Safety Observation Validation",defaultDisplay: true,dueInDisplay: false)
        signalWorkflowState.allowedDispositions = [disposition]
        signalWorkflowState.save(flush: true)

        signalWorkflowStateNew = new SignalWorkflowState(value: "Signal Analysis & Prioritization",displayName: "Signal Analysis & Prioritization",defaultDisplay: false,dueInDisplay: true)
        signalWorkflowStateNew.allowedDispositions = [defaultDisposition]
        signalWorkflowStateNew.save(flush: true)

        userGroup = new Group(name: "GroupInstance", createdBy: "user", modifiedBy: "user", groupType: GroupType.USER_GROUP, defaultSignalDisposition: defaultSignalDisposition,
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition
        )
        userGroup.save(flush: true)

        signalWorkflowRule = new SignalWorkflowRule(ruleName: "Safety Observation Validation to Prioritization",fromState: "Safety Observation Validation", toState: "Signal Analysis & Prioritization",
                description: "This is Signal Workflow Rule", display: true)
        signalWorkflowRule.allowedGroups = [userGroup]
        signalWorkflowRule.save(flush: true)

        signalWorkflowRuleNew = new SignalWorkflowRule(ruleName: "New Rule",fromState: "Safety Observation Validation", toState: "Documentation & Archiving",
                description: "This is Signal Workflow Rule", display: true)
        signalWorkflowRuleNew.allowedGroups = [wfGroup]
        signalWorkflowRuleNew.save(flush: true)

        workflowStates = ['Safety Observation Validation','Signal Analysis & Prioritization','Signal Assessment',
                          'Recommendation for Action','Exchange of Information','Documentation & Archiving']
    }

    void "test fetchAllPossibleTransitionsFromCurrentState"(){
        when:
        Map result = service.fetchAllPossibleTransitionsFromCurrentState()

        then:
        result == ['Safety Observation Validation':['Signal Analysis & Prioritization','Documentation & Archiving'],'Signal Analysis & Prioritization':[], 'Signal Assessment':[], 'Recommendation for Action':[], 'Exchange of Information': [],'Documentation & Archiving':[]]
    }

    void 'test findTargetStateFromIncomingState'(){
        when:
        List result = service.findTargetStateFromIncomingState('Safety Observation Validation')

        then:
        result.size == 2
        result == ['Signal Analysis & Prioritization','Documentation & Archiving']
    }

    void 'test defaultSignalWorkflowState'(){
        when:
        String result = service.defaultSignalWorkflowState()

        then:
        result == 'Safety Observation Validation'
    }
}
