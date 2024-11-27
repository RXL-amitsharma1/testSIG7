package unit.com.rxlogix.config

import com.rxlogix.action.ActionService
import com.rxlogix.config.ActionType
import com.rxlogix.config.ActionTypeService
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class ActionTypeServiceSpec extends Specification implements ServiceUnitTest<ActionTypeService> {
    def setup(){
        ActionType actionType1=new ActionType(value: "value1",displayName: "action1",description: "description1")
        actionType1.save(flush:true,failOnError:true)
        ActionType actionType2=new ActionType(value: "value2",displayName: "action2",description: "description2")
        actionType2.save(flush:true,failOnError:true)
    }
    def cleanup(){

    }
    void "test saveActionType"(){
        given:
        ActionType actionType=new ActionType(value: "value1",displayName: "action1",description: "description1")
        when:
        boolean result=service.saveActionType(actionType)
        then:
        result==true
    }
    void "test saveActionType when same action type is already available"(){
        given:
        ActionType actionType=new ActionType(value: "value1",displayName: "action1",description: "description1")
        when:
        boolean result=service.saveActionType(actionType)
        then:
        result==true
    }
    void "test delete"(){
        setup:
        when:
        service.deleteActionType(ActionType.get(1))
        then:
        ActionType.get(1)==null
    }
}
