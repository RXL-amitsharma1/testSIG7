package unit.com.rxlogix.config

import com.rxlogix.cache.CacheService
import com.rxlogix.config.ActionType
import com.rxlogix.config.ActionTypeController
import com.rxlogix.config.ActionTypeService
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class ActionTypeControllerSpec extends Specification implements ControllerUnitTest<ActionTypeController> {
    ActionType actionType1
    ActionType actionType2
    def setup(){
        actionType1=new ActionType(value: "value1",displayName: "action1",description: "description1")
        actionType1.save(flush:true,failOnError:true)
        actionType2=new ActionType(value: "value2",displayName: "action2",description: "description2")
        actionType2.save(flush:true,failOnError:true)
        CacheService cacheService=Mock(CacheService)
        cacheService.setActionTypeCache(_)>>{
            return actionType1
        }
        cacheService.removeActionType(_)>>{
            return  true
        }
        controller.cacheService=cacheService
    }
    def cleanup(){

    }
    void "test index"(){
        when:
        controller.index()
        then:
        response.status==302
        response.redirectedUrl=="/actionType/list"
    }
    void "test list where are some ActionType"(){
        when:
        Map result=controller.list()
        then:
        response.status==200
        result.actionTypeInstanceList.get(0)==actionType1
        result.actionTypeInstanceList.size()==2
        result.actionTypeInstanceList.get(1)==actionType2
        result.actionTypeInstanceTotal==2
    }
    void "test list where are some ActionType and params.max is given"(){
        given:
        params.max=1
        when:
        Map result=controller.list()
        then:
        response.status==200
        result.actionTypeInstanceList.get(0)==actionType1
        result.actionTypeInstanceList.size()==1
        result.actionTypeInstanceTotal==2
    }
    void "test list where is no ActionType"(){
        setup:
        ActionType.list().each{
            it.delete()
        }
        when:
        Map result=controller.list()
        then:
        response.status==200
        result.actionTypeInstanceList==[]
        result.actionTypeInstanceTotal==0
    }
    void "test create action"(){
        setup:
        params.value="value3"
        params.displayName="action3"
        params.description="description3"
        when:
        Map result=controller.create()
        then:
        response.status==200
        result.actionTypeInstance.getClass()==ActionType
        result.actionTypeInstance.value=="value3"
    }
    void "test save action on success"(){
        setup:
        params.value="value3"
        params.displayName="action3"
        params.description="description3"
        request.method="POST"
        when:
        controller.save()
        then:
        response.status==302
        response.redirectedUrl=="/actionType/list"
        flash.message == "default.created.message"
        flash.args == [3]
        flash.defaultMessage == "Action Type value3 created"
        ActionType.get(3)!=null
    }
    void "test save action when exception occurs"(){
        setup:
        params.value="value3"
        params.displayName="action3"
        request.method="POST"
        when:
        controller.save()
        then:
        response.status==200
        view=="/actionType/create"
        ActionType.get(3)==null
    }
    void "test show action when ActionType is found"(){
        setup:
        params.id=1L
        when:
        Map result=controller.show()
        then:
        response.status==200
        result.actionTypeInstance==actionType1
    }
    void "test show action when ActionType is not found"(){
        setup:
        params.id=10L
        when:
        controller.show()
        then:
        response.status==302
        flash.message == "actionType.not.found"
        flash.args == [10]
        flash.defaultMessage == "ActionType not found with id 10"
        response.redirectedUrl=="/actionType/list"
    }
    void "test action edit when ActionType is found"(){
        given:
        params.id=1L
        when:
        Map result=controller.edit()
        then:
        response.status==200
        result.actionTypeInstance==actionType1
    }
    void "test edit action when ActionType is not found"(){
        setup:
        params.id=10L
        when:
        controller.edit()
        then:
        response.status==302
        flash.message == "actionType.not.found"
        flash.args == [10]
        flash.defaultMessage == "ActionType not found with id 10"
        response.redirectedUrl=="/actionType/list"
    }
    void "test update action on success when params.version is not less than current version"(){
        setup:
        params.id=1L
        actionType1.displayName="test1"
        actionType1.save(flush:true)
        params.version=1L
        params.value="value3"
        params.displayName="action3"
        params.description="description3"
        request.method="POST"
        when:
        controller.update()
        then:
        response.status==302
        response.redirectedUrl=="/actionType/show/1"
        flash.message == "actionType.updated"
        flash.args == [1L]
        flash.defaultMessage == "ActionType 1 updated"
        actionType1.displayName=="action3"
    }
    void "test update action on success when params.version is less than current version"(){
        setup:
        params.id=1L
        actionType1.displayName="test1"
        actionType1.save(flush:true)
        actionType1.displayName="test2"
        actionType1.save(flush:true)
        params.version=1L
        params.value="value3"
        params.displayName="action3"
        params.description="description3"
        request.method="POST"
        when:
        controller.update()
        then:
        response.status==200
        view=="/actionType/edit"
        actionType1.displayName=="test2"
    }
    void "test update action on success when params.version is not given"(){
        setup:
        params.id=1L
        params.value="value3"
        params.displayName="action3"
        params.description="description3"
        request.method="POST"
        when:
        controller.update()
        then:
        response.status==302
        response.redirectedUrl=="/actionType/show/1"
        flash.message == "actionType.updated"
        flash.args == [1L]
        flash.defaultMessage == "ActionType 1 updated"
        actionType1.displayName=="action3"
    }
    void "test update when ActionType is not found"(){
        setup:
        params.id=10L
        params.value="value3"
        params.displayName="action3"
        params.description="description3"
        request.method="POST"
        when:
        controller.update()
        then:
        response.status==302
        response.redirectedUrl=="/actionType/edit/10"
        flash.message == "actionType.not.found"
        flash.args == [10]
        flash.defaultMessage == "ActionType not found with id 10"
    }
    void "test delete action on success"(){
        setup:
        params.id=1L
        request.method="POST"
        when:
        controller.delete()
        then:
        response.status==302
        response.redirectedUrl=="/actionType/list"
        flash.message == "actionType.deleted"
        flash.args == [1]
        flash.defaultMessage == "ActionType 1 deleted"
        ActionType.get(1)==null
    }
    void "test delete action when action type is not found"(){
        setup:
        params.id=10L
        request.method="POST"
        when:
        controller.delete()
        then:
        response.status==302
        response.redirectedUrl=="/actionType/list"
        flash.message == "actionType.not.found"
        flash.args == [10]
        flash.defaultMessage == "ActionType not found with id 10"
        ActionType.get(1)!=null
    }
}
