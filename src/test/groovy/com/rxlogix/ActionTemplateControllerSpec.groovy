package com.rxlogix

import com.rxlogix.ActionTemplateController
import com.rxlogix.cache.CacheService
import com.rxlogix.config.ActionConfiguration
import com.rxlogix.config.ActionTemplate
import com.rxlogix.config.ActionType
import com.rxlogix.config.Disposition
import com.rxlogix.config.WorkflowRule
import com.rxlogix.enums.GroupType
import com.rxlogix.user.Group
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class ActionTemplateControllerSpec extends Specification implements ControllerUnitTest<ActionTemplateController> {

    def setup() {

        Disposition disposition1 = new Disposition(value: "ValidatedSignal2", displayName: "ValidatedSignal2", closed: false,
                validatedConfirmed: false, abbreviation: "C")
        disposition1.save(failOnError: true)
        Group wfGroup = new Group(name: "Default1", groupType: GroupType.WORKFLOW_GROUP,
                createdBy: 'createdBy',
                modifiedBy: 'modifiedBy',
                defaultQualiDisposition: disposition1,
                defaultQuantDisposition: disposition1,
                defaultAdhocDisposition: disposition1,
                defaultEvdasDisposition: disposition1,
                defaultLitDisposition: disposition1,
                defaultSignalDisposition: disposition1)
        wfGroup.save(flush: true, failOnError: true)

        Preference preference=new Preference(locale:new Locale('en'),createdBy: 'admin',modifiedBy: 'admin')
        preference.save(validate:false)

        User admin = new User(username: 'admin', createdBy: 'createdBy', modifiedBy: 'modifiedBy', fullName: 'Admin User',groups: [wfGroup],preference:preference)
        admin.save(flush:true,failOnError:true)
        ActionTemplate actionTemplate1=new ActionTemplate(name: "template1",description: "description1",actionJson: "action1",
        dateCreated: new Date()-3,lastUpdated: new Date()-2,createdBy: "admin",modifiedBy: "admin")
        actionTemplate1.save(flush:true,failOnError: true)
        ActionTemplate actionTemplate2=new ActionTemplate(name: "template2",description: "description2",actionJson: "action2",
                dateCreated: new Date()-1,lastUpdated: new Date(),createdBy: "admin",modifiedBy: "admin")
        actionTemplate2.save(flush:true,failOnError: true)
        WorkflowRule workflowRule=new WorkflowRule(name: "workFlowRule")
        workflowRule.addToActionTemplates(actionTemplate2)
        workflowRule.save(validate:false)

        ActionConfiguration actionConfiguration1=new ActionConfiguration(isEmailEnabled: true,value: "actionConfiguration1")
        actionConfiguration1.save(validate:false)
        ActionType actionType1=new ActionType(displayName: "action1",value: "action1",description: "actionDescription1")
        actionType1.save(flush:true,failOnError:true)
        CacheService cacheService=Mock(CacheService)
        cacheService.setActionTemplateCache(_)>>{
            return true
        }
        controller.cacheService=cacheService
        SpringSecurityService springSecurityService=Mock(SpringSecurityService)
        springSecurityService.isLoggedIn()>>{
            return  true
        }
        springSecurityService.principal>>{
            return admin
        }
        controller.CRUDService.userService.springSecurityService=springSecurityService
    }

    def cleanup(){

    }

    void "test list action when there are ActionTemplates"(){
        when:
        controller.list()
        then:
        response.status==200
        JSON.parse(response.text).size()==2
        JSON.parse(response.text).get(0).id==1
        JSON.parse(response.text).get(1).id==2
    }
    void "test list action when there are no ActionTemplates"(){
        setup:
        ActionTemplate.list().each{
            it.delete()
        }
        when:
        controller.list()
        then:
        response.status==200
        JSON.parse(response.text).size()==0
    }
    void "test save action on success"(){
        setup:
        params.name="template3"
        params.assignedTo=1
        params.actionConfig=1L
        params.actiontype=1L
        params.dueIn=(new Date()+1).toString()
        params.details="details"
        params.comments="comment"
        params.description="description3"
        when:
        controller.save()
        then:
        response.status==200
        JSON.parse(response.text).message=="Action Template added successfully"
        JSON.parse(response.text).status==true
        ActionTemplate.get(3)!=null
        ActionTemplate.get(3).name=="template3"
    }
    void "test save action when details are not given"(){
        setup:
        params.name="template3"
        params.assignedTo=1
        params.actionConfig=1L
        params.actiontype=1L
        params.dueIn=(new Date()+1).toString()
        params.comments="comment"
        params.description="description3"
        when:
        controller.save()
        then:
        response.status==200
        JSON.parse(response.text).message=="Please fill the required fields."
        JSON.parse(response.text).status==false
        ActionTemplate.get(3)==null
    }
    void "test save action when dueIn is not given"(){
        setup:
        params.details="details"
        params.name="template3"
        params.assignedTo=1
        params.actionConfig=1L
        params.actiontype=1L
        params.comments="comment"
        params.description="description3"
        when:
        controller.save()
        then:
        response.status==200
        JSON.parse(response.text).message=="Please fill the required fields."
        JSON.parse(response.text).status==false
        ActionTemplate.get(3)==null
    }
    void "test save action when already exist with given name"(){
        setup:
        params.details="details"
        params.name="template1"
        params.assignedTo=1
        params.actionConfig=1L
        params.actiontype=1L
        params.comments="comment"
        params.description="description3"
        params.dueIn=(new Date()+1).toString()
        when:
        controller.save()
        then:
        response.status==200
        JSON.parse(response.text).message=="Action Template name already exists"
        JSON.parse(response.text).status==false
        ActionTemplate.get(3)==null
    }
    void "test save action when assignedTo is not given"(){
        setup:
        params.name="template3"
        params.actionConfig=1L
        params.actiontype=1L
        params.dueIn=(new Date()+1).toString()
        params.details="details"
        params.comments="comment"
        params.description="description3"
        when:
        controller.save()
        then:
        response.status==200
        JSON.parse(response.text).message=="Please fill the required fields."
        JSON.parse(response.text).status==false
        ActionTemplate.get(3)==null
    }
    void "test save action when exception occurs"(){
        setup:
        params.details="details"
        params.assignedTo=1
        params.actionConfig=1L
        params.actiontype=1L
        params.comments="comment"
        params.description="description3"
        params.dueIn=(new Date()+1).toString()
        when:
        controller.save()
        then:
        response.status==200
        JSON.parse(response.text).message=="Please fill the required fields."
        JSON.parse(response.text).status==false
        ActionTemplate.get(3)==null
    }
    void "test edit action"(){
        when:
        controller.edit(1L)
        then:
        response.status==200
        JSON.parse(response.text).status==true
        JSON.parse(response.text).data.name=="template1"
    }
    void "test update action on success"(){
        setup:
        params.name="template3"
        params.assignedTo=1
        params.dueIn=(new Date()+1).toString()
        params.details="details"
        params.description="description3"
        when:
        controller.update(1L)
        then:
        response.status==200
        JSON.parse(response.text).status==true
        JSON.parse(response.text).message=="Action Template updated successfully"
        ActionTemplate.get(1).name=="template3"
    }
    void "test update action when details are not given"(){
        setup:
        params.name="template3"
        params.assignedTo=1
        params.dueIn=(new Date()+1).toString()
        params.description="description3"
        when:
        controller.update(1L)
        then:
        response.status==200
        JSON.parse(response.text).status==false
        JSON.parse(response.text).message=="Please fill the required fields."
        ActionTemplate.get(1).name=="template1"
    }
    void "test update action when dueIn is not given"(){
        setup:
        params.details="details"
        params.name="template3"
        params.assignedTo=1
        params.description="description3"
        when:
        controller.update(1L)
        then:
        response.status==200
        JSON.parse(response.text).status==false
        JSON.parse(response.text).message=="Please fill the required fields."
        ActionTemplate.get(1).name=="template1"
    }
    void "test update action when given name exist and given name doesn't match to it's name"(){
        setup:
        params.details="details"
        params.name="template2"
        params.assignedTo=1
        params.dueIn=(new Date()+1).toString()
        params.description="description3"
        params.dueIn=(new Date()+1).toString()
        when:
        controller.update(1L)
        then:
        response.status==200
        JSON.parse(response.text).status==false
        JSON.parse(response.text).message=="Action Template name already exists"
        ActionTemplate.get(1).name=="template1"
    }
    void "test update action when assignedTo is not given"(){
        setup:
        params.details="details"
        params.name="template3"
        params.dueIn=(new Date()+1).toString()
        params.description="description3"
        when:
        controller.update(1L)
        then:
        response.status==200
        JSON.parse(response.text).status==false
        JSON.parse(response.text).message=="Please fill the required fields."
        ActionTemplate.get(1).name=="template1"
    }
    void "test update action when exception occurs"(){
        setup:
        SpringSecurityService springSecurityService=Mock(SpringSecurityService)
        springSecurityService.isLoggedIn()>>{
            return  true
        }
        springSecurityService.principal>>{
            return null
        }
        controller.CRUDService.userService.springSecurityService=springSecurityService
        params.details="details"
        params.name="template3"
        params.assignedTo=1
        params.dueIn=(new Date()+1).toString()
        params.description="description3"
        when:
        controller.update(1L)
        then:
        response.status==200
        JSON.parse(response.text).status==false
        JSON.parse(response.text).message=="Please fill the required fields."
        ActionTemplate.get(1).name=="template1"
    }
    void "test delete action"(){
        when:
        controller.delete(1L)
        then:
        response.status==200
        JSON.parse(response.text).status==true
        JSON.parse(response.text).message=="Action Template template1 deleted successfully"
        ActionTemplate.get(1)==null
    }
}
