package com.rxlogix

import com.rxlogix.config.Disposition
import com.rxlogix.config.Priority
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.SignalWorkflowRule
import com.rxlogix.signal.SignalWorkflowState
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.converters.JSON
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
class SignalWorkflowControllerSpec extends Specification implements ControllerUnitTest<SignalWorkflowController> {

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

        signalWorkflowRuleNew = new SignalWorkflowRule(ruleName: "Safety Observation Validation to Prioritization",fromState: "Safety Observation Validation", toState: "Signal Analysis & Prioritization",
                description: "This is Signal Workflow Rule", display: true)
        signalWorkflowRuleNew.allowedGroups = [wfGroup]
        signalWorkflowRuleNew.save(flush: true)

        workflowStates = ['Safety Observation Validation','Signal Analysis & Prioritization','Signal Assessment',
                          'Recommendation for Action','Exchange of Information','Documentation & Archiving']

        UserService mockUserService = Mock(UserService)
        mockUserService.getUser() >> { user }
        controller.userService = mockUserService

    }

    def "test signalWorkflowRule"(){
        when:
        controller.signalWorkflowRule()

        then:
        response.status == 200
        view == '/signalWorkflow/signalWorkFlowRule'
    }

    def "test createSignalWorkflowRule"(){
        when:
        controller.createSignalWorkflowRule()

        then:
        response.status == 200
        view == '/signalWorkflow/createSignalWorkflowRule'
        model.workflowStatesSignal == workflowStates
    }

    def "test editSignalWorkflowState"(){
        given:
        params.id = 1

        when:
        controller.editSignalWorkflowState()

        then:
        response.status == 200
        view == '/signalWorkflow/editSignalWorkflowState'
        model.signalWorkflowStateInstance == signalWorkflowState
    }

    def "test editSignalWorkflowRule"(){
        given:
        params.id = 1

        when:
        controller.editSignalWorkflowRule()

        then:
        response.status == 200
        view == '/signalWorkflow/editSignalWorkflowRule'
        model.workflowStatesSignal == workflowStates
        model.signalWorkflowRuleInstance == signalWorkflowRule
    }

    def "test fetchSignalWorkflowRule"(){
        when:
        controller.fetchSignalWorkflowRule()

        then:
        response.status == 200
        response.contentType == 'application/json;charset=UTF-8'
        JSON.parse(response.text).ruleList.size() == 2
    }

    def "test fetchSignalWorkflowState"(){
        when:
        controller.fetchSignalWorkflowState()

        then:
        response.status == 200
        response.contentType == 'application/json;charset=UTF-8'
        JSON.parse(response.text).stateList.size() == 2
    }

    def "test signalWorkflowList"(){
        when:
        controller.signalWorkflowList()

        then:
        response.status == 200
        view == '/signalWorkflow/signalWorkflowList'
    }

    def "test saveWorkflowRule in case of success"(){
        given:
        params.allowedGroups = [userGroup.id]
        params.display = "on"
        params.ruleName = "Prioritization to Assessment"
        params.description = "This is new rule"
        params.fromState = "Signal Analysis & Prioritization"
        params.toState = "Signal Assessment"

        when:
        controller.saveWorkflowRule()

        then:
        response.status == 302
        response.redirectedUrl == '/signalWorkflow/signalWorkflowRule'
        controller.flash.message != null
        controller.flash.defaultMessage != null
    }

    def "test saveWorkflowRule in case of Exception"(){
        given:
        params.allowedGroups = [userGroup.id]
        params.display = "on"
        params.ruleName = null
        params.description = "This is new rule"
        params.fromState = "Signal Analysis & Prioritization"
        params.toState = "Signal Assessment"

        when:
        controller.saveWorkflowRule()

        then:
        response.status == 200
        view == '/signalWorkflow/createSignalWorkflowRule'
        model.workflowStatesSignal == workflowStates
    }

    def "test updateWorkflowRule in case of updation allowed"(){
        given:
        params.id = 2
        params.allowedGroups = [wfGroup.id]
        params.display = "on"
        params.ruleName = "Prioritization to Assessment"
        params.description = "This is new rule"
        params.fromState = "Signal Analysis & Prioritization"
        params.toState = "Signal Assessment"

        when:
        controller.updateWorkflowRule()

        then:
        response.status == 302
        response.redirectedUrl == '/signalWorkflow/signalWorkflowRule'
        controller.flash.message != null
    }

    def "test updateWorkflowRule in case of updation not allowed"(){
        given:
        params.id = 1
        params.allowedGroups = [userGroup.id]
        params.display = "on"
        params.ruleName = "Prioritization to Assessment"
        params.description = "This is new rule"
        params.fromState = "Signal Analysis & Prioritization"
        params.toState = "Signal Assessment"

        when:
        controller.updateWorkflowRule()

        then:
        response.status == 302
        response.redirectedUrl == '/signalWorkflow/editSignalWorkflowRule/'+params.id
        controller.flash.error != null
    }

    def "test updateWorkflowRule in case of Exception"(){
        given:
        params.id = 2
        params.allowedGroups = [wfGroup.id]
        params.display = "on"
        params.ruleName = null
        params.description = "This is new rule"
        params.fromState = "Signal Analysis & Prioritization"
        params.toState = "Signal Assessment"

        when:
        controller.updateWorkflowRule()

        then:
        response.status == 200
        view == '/signalWorkflow/editSignalWorkflowRule'
        model.workflowStatesSignal == workflowStates
    }

    def "test deleteWorkflowRule in case signal workflow rule is found"(){
        given:
        params.id = 2

        when:
        controller.deleteWorkflowRule()

        then:
        response.status == 302
        response.redirectedUrl == "/signalWorkflow/signalWorkflowRule"
    }

    def "test deleteWorkflowRule in case signal workflow rule is not found"(){
        given:
        params.id = -1

        when:
        controller.deleteWorkflowRule()

        then:
        response.status == 302
        response.redirectedUrl == "/signalWorkflow/signalWorkflowRule"
    }

    def "test updateWorkflowState in case of Exception"(){
        given:
        params.id = 1
        params.value = null
        params.displayName = "Safety Observation Validation"
        params.allowedDispositions = [disposition.id]
        params.defaultDisplay = "on"
        params.dueInDisplay = null

        when:
        controller.updateWorkflowState()

        then:
        response.status == 200
        view == '/signalWorkflow/editSignalWorkflowState'
    }

    def "test updateWorkflowState"(){
        given:
        params.id = signalWorkflowState.id
        params.value = "Safety Observation Validation"
        params.displayName = "Safety Observation Validation"
        params.allowedDispositions = [disposition.id,defaultDisposition.id]
        params.defaultDisplay = "on"
        params.dueInDisplay = "on"

        when:
        controller.updateWorkflowState()

        then:
        response.status == 302
        response.redirectedUrl == "/signalWorkflow/signalWorkflowList"
        controller.flash.message != null
    }


    def "test updateWorkflowState in case of no default state"(){
        given:
        params.id = signalWorkflowState.id
        params.value = "Safety Observation Validation"
        params.displayName = "Safety Observation Validation"
        params.allowedDispositions = [disposition.id]
        params.defaultDisplay = null
        params.dueInDisplay = null

        when:
        controller.updateWorkflowState()

        then:
        response.status == 302
        response.redirectedUrl == "/signalWorkflow/editSignalWorkflowState/"+params.id
        controller.flash.error != null
    }

    def "test enableSignalWorkflow"(){
        given:
        params.enableWorkflow = true

        when:
        controller.enableSignalWorkflow()

        then:
        response.status == 200
        JSON.parse(response.text).data == true
    }
}
