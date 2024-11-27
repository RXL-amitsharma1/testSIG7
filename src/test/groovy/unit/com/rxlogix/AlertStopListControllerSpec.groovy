package unit.com.rxlogix

import com.rxlogix.AlertStopListController
import com.rxlogix.AlertStopListService
import com.rxlogix.CRUDService
import com.rxlogix.UserService
import com.rxlogix.config.AlertStopList
import com.rxlogix.config.Configuration
import com.rxlogix.user.User
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification
class AlertStopListControllerSpec extends Specification implements ControllerUnitTest<AlertStopListController> {
    def setup(){
        User user=new User(username: "user1")
        user.save(validate:false)
        AlertStopList alertStopList1=new AlertStopList(productName: '{"3":[{"name":"paracetamol11"},{"name":"paracetamol12"}]}',
                eventName: '{"1":[{"name":"event11"},{"name":"event12"}]}',dateCreated: new Date(),createdBy: "createdBy")
        alertStopList1.save(flush:true,failOnError:true)
        AlertStopList alertStopList2=new AlertStopList(productName: '{"1":[{"name":"paracetamol21"},{"name":"paracetamol22"}]}',
                eventName: '{"1":[{"name":"event21"},{"name":"event22"}]}',dateCreated: new Date()-1,createdBy: "createdBy")
        alertStopList2.save(flush:true,failOnError:true)
        SpringSecurityService springSecurityService=Mock(SpringSecurityService)
        springSecurityService.isLoggedIn()>>{
            return true
        }
        springSecurityService.principal>>{
            return user
        }
        controller.alertStopListService.CRUDService.userService.springSecurityService=springSecurityService
    }

    def clean(){

    }

    void "test index action"(){
        when:
        controller.index()
        then:
        response.status==200
        view=="/alertStopList/index"
        model.alertStopList.getClass()==AlertStopList
    }
    void "test list action when there are AlertStopList"(){
        when:
        controller.list()
        then:
        response.status==200
        JSON.parse(response.text).size()==2
    }
    void "test list action when there are no AlertStopList"(){
        setup:
        AlertStopList.list().each{
            it.delete()
        }
        when:
        controller.list()
        then:
        response.status==200
        JSON.parse(response.text).size()==0
    }
    void "test save action on sucess"(){
        setup:
        params.productSelection='{"3":[{"name":"paracetamol11"},{"name":"paracetamol12"}]}'
        params.eventSelection='{"4":[{"name":"event11"},{"name":"event12"}]}'
        when:
        controller.save()
        then:
        response.status==200
        flash.error==null
        view=="/alertStopList/index"
        AlertStopList.get(3).productName==params.productSelection
        AlertStopList.get(3).eventName==params.eventSelection
    }
    void "test save action when condition fails in preferred term"(){
        setup:
        params.productSelection='{"3":[{"name":"paracetamol11"},{"name":"paracetamol12"}]}'
        params.eventSelection='{"1":[{"name":"event11"},{"name":"event12"}]}'
        when:
        controller.save()
        then:
        response.status==200
        flash.error=="app.label.pt.error.message"
        view=="/alertStopList/index"
        model.alertStopList.productName==null
    }
    void "test save action when save fails"(){
        setup:
        params.productSelection='{"3":[{"name":"paracetamol11"},{"name":"paracetamol12"}]}'
        params.eventSelection='{"4":[{"name":"event11"},{"name":"event12"}]}'
        when:
        controller.save()
        then:
        response.status==200
        flash.error==null
        view=="/alertStopList/index"
        AlertStopList.get(3).productName==params.productSelection
        AlertStopList.get(3).eventName==params.eventSelection
    }
    void "test update when params.activated is true"(){
        given:
        params.activated="true"
        params.id
        when:
        controller.update()
        then:
        response.status==200
        AlertStopList.get(1).productName=='{"3":[{"name":"paracetamol11"},{"name":"paracetamol12"}]}'
    }
    void "test update when params.activated is false"(){
        given:
        params.activated="false"
        params.id
        when:
        controller.update()
        then:
        response.status==200
        AlertStopList.get(1).productName=='{"3":[{"name":"paracetamol11"},{"name":"paracetamol12"}]}'
    }
}
