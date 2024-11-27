package com.rxlogix

import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.*
import com.rxlogix.config.DispositionRule
import com.rxlogix.config.Disposition
import com.rxlogix.config.WorkflowRule
import grails.plugin.springsecurity.SpringSecurityUtils
import com.rxlogix.config.PVSState

class WorkflowRuleControllerSpec extends Specification implements ControllerUnitTest<WorkflowRuleController> {

    Disposition initialDisposition
    Disposition targetDisposition
    DispositionRule dispositionRule
    WorkflowRule workflowRule

    def setup() {
        initialDisposition = new Disposition(value: 'testValue1', displayName: 'testInit', abbreviation: 'tv1').save(failOnError: true)
        targetDisposition = new Disposition(value: 'testValue2', displayName: 'testTarget', abbreviation: 'tv2').save(failOnError: true)
        dispositionRule = new DispositionRule(name: 'testRule1', incomingDisposition: initialDisposition, targetDisposition: targetDisposition)
        dispositionRule.save(failOnError: true)
        PVSState state1 = new PVSState(value: 'testState1', displayName: 'st1')
        state1.save(failOnError: true)
        PVSState state2 = new PVSState(value: 'testState2', displayName: 'st2')
        state2.save(failOnError: true)
        workflowRule = new WorkflowRule(name: 'testWorkflowRule1', incomeState: state1, targetState: state2)
        workflowRule.save(failOnError: true)
    }

    def cleanup() {
    }

    void "test list"() {
        when:
        controller.list()
        then:
        response.status == 200
        response.json[0].name == 'testRule1'
        response.json[0].incomingDisposition == 'testInit'
        response.json[0].targetDisposition == 'testTarget'
    }

    void "test create"() {
        given:
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return false
        }
        params.name = 'testRule2'
        params.incomingDisposition = initialDisposition
        params.targetDisposition = targetDisposition
        when:
        def model = controller.create()
        then:
        response.status == 200
        model.availableDispositions == [initialDisposition, targetDisposition]
        model.workflowRuleInstance.name == 'testRule2'

    }

    void "test save, When saveWorkflowRule return true -- Success"() {
        given:

        def mockWorkflowRuleService = Mock(WorkflowRuleService)
        mockWorkflowRuleService.saveWorkflowRule(_) >> {
            return true
        }
        controller.workflowRuleService = mockWorkflowRuleService
        params.name = 'testRule3'
        params.incomingDisposition = initialDisposition
        params.targetDisposition = targetDisposition
        request.method = 'POST'
        when:
        controller.save()
        then:
        response.status == 302
        flash.message != null
        flash.args != null
        response.redirectedUrl == '/workflowRule/index'

    }

    void "test save, saveWorkflowRule return false -- Success"() {
        given:

        def mockWorkflowRuleService = Mock(WorkflowRuleService)
        mockWorkflowRuleService.saveWorkflowRule(_) >> {
            return false
        }
        controller.workflowRuleService = mockWorkflowRuleService
        params.name = 'testRule3'
        params.incomingDisposition = initialDisposition
        params.targetDisposition = targetDisposition
        request.method = 'POST'
        when:
        controller.save()
        then:
        response.status == 200
        view == '/workflowRule/create'
        model.workflowRuleInstance.name == 'testRule3'
        model.availableDispositions == [initialDisposition, targetDisposition]

    }

    void "test create "() {
        given:

        def mockWorkflowRuleService = Mock(WorkflowRuleService)
        mockWorkflowRuleService.saveWorkflowRule(_) >> {
            return false
        }
        controller.workflowRuleService = mockWorkflowRuleService
        params.name = 'testRule3'
        params.incomingDisposition = initialDisposition
        params.targetDisposition = targetDisposition
        request.method = 'POST'
        when:
        controller.save()
        then:
        response.status == 200
        view == '/workflowRule/create'
        model.workflowRuleInstance.name == 'testRule3'
        model.availableDispositions == [initialDisposition, targetDisposition]

    }

    void "test show, When workflowRuleInstance exists "() {
        given:
        params.id = 1
        when:
        def model = controller.show()
        then:
        response.status == 200
        model.workflowRuleInstance.name == 'testWorkflowRule1'

    }

    void "test show, When workflowRuleInstance doesn't exist "() {
        given:
        params.id = 40
        when:
        def model = controller.show()
        then:
        response.status == 302
        flash.message != null
        flash.args == [40]
        response.redirectedUrl == '/workflowRule/list'

    }


    void "test edit, When dispositionRuleInstance exists "() {
        given:
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return false
        }
        params.id = 1
        when:
        def model = controller.edit()
        then:
        response.status == 200
        model.workflowRuleInstance.name == 'testRule1'
        model.availableDispositions == [initialDisposition, targetDisposition]
        model.isAdmin == false

    }

    void "test edit, When dispositionRuleInstance doesn't exist "() {
        given:
        params.id = 50
        when:
        def model = controller.edit()
        then:
        response.status == 302
        flash.message != null
        flash.args == [50]
        response.redirectedUrl == '/workflowRule/index'

    }

    void "test update, When dispositionRuleInstance doesn't exist"() {
        given:
        params.id = 51
        request.method = 'POST'
        when:
        controller.update()
        then:
        response.status == 302
        flash.message != null
        flash.args == [51]
        response.redirectedUrl == '/workflowRule/edit/51'
    }

    void "test update, When dispositionRuleInstance exist and saveWorkflowRule returns true"() {
        given:
        def mockWorkflowRuleService = Mock(WorkflowRuleService)
        mockWorkflowRuleService.saveWorkflowRule(_) >> {
            return true
        }
        controller.workflowRuleService = mockWorkflowRuleService
        params.id = 1
        params.name = 'newTestRule1'
        params.version = 2
        request.method = 'POST'
        when:
        controller.update()
        then:
        response.status == 302
        flash.message != null
        flash.args == [1]
        response.redirectedUrl == '/workflowRule/index'
    }

    void "test update, When dispositionRuleInstance exist and saveWorkflowRule returns false"() {
        given:
        def mockWorkflowRuleService = Mock(WorkflowRuleService)
        mockWorkflowRuleService.saveWorkflowRule(_) >> {
            return false
        }
        controller.workflowRuleService = mockWorkflowRuleService
        params.id = 1
        params.name = 'newTestRule1'
        params.version = 2
        request.method = 'POST'
        when:
        controller.update()
        then:
        response.status == 200
        view == '/workflowRule/edit'
        model.workflowRuleInstance.name == 'newTestRule1'
        model.availableDispositions == [initialDisposition, targetDisposition]
    }

}