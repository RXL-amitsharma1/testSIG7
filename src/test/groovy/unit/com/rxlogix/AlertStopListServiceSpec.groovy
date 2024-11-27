package unit.com.rxlogix

import com.rxlogix.AlertStopListService
import com.rxlogix.config.AlertStopList
import com.rxlogix.user.User
import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import spock.lang.Unroll

class AlertStopListServiceSpec extends Specification implements ServiceUnitTest<AlertStopListService> {
    def setup(){
        AlertStopList alertStopList1=new AlertStopList(productName: '{"3":[{"name":"paracetamol11"},{"name":"paracetamol12"}]}',
                eventName: '{"1":[{"name":"event11"},{"name":"event12"}]}',dateCreated: new Date(),createdBy: "createdBy")
        alertStopList1.save(flush:true,failOnError:true)
        AlertStopList alertStopList2=new AlertStopList(productName: '{"1":[{"name":"paracetamol21"},{"name":"paracetamol22"}]}',
                eventName: '{"1":[{"name":"event21"},{"name":"event22"}]}',dateCreated: new Date()-1,createdBy: "createdBy")
        alertStopList2.save(flush:true,failOnError:true)
        User user=new User(username: "user1")
        user.save(validate:false)
        SpringSecurityService springSecurityService=Mock(SpringSecurityService)
        springSecurityService.isLoggedIn()>>{
            return true
        }
        springSecurityService.principal>>{
            return user
        }
        service.CRUDService.userService.springSecurityService=springSecurityService
    }

    def clean(){

    }

    void "test getAlertStopList action when there are alert stop list"(){
        when:
        List result=service.getAlertStopList()
        then:
        result.size()==2
        result[0].id==1
        result[1].id==2
    }
    void "test getAlertStopList when there are no alert stop list"(){
        setup:
        AlertStopList.list().each {
            it.delete()
        }
        when:
        List result=service.getAlertStopList()
        then:
        result.size()==0
    }
    @Unroll
    void "test prepareEveneMap"(){
        setup:
        String eventName='{"3":[{"name":"paracetamol11"},{"name":"paracetamol12"}]}'
        when:
        Map result=service.prepareProductMap(eventName,[])
        then:
        result.get("Product Name")==["paracetamol11", "paracetamol12"]
    }
    void "test prepareProductMap"(){
        setup:
        String productName='{"1":[{"name":"paracetamol"},{"name":"paracetamol2"}]}'
        when:
        def result=service.prepareProductMap(productName,[])
        then:
        result.Ingredient==["paracetamol", "paracetamol2"]
    }
    void "test saveList on success"(){
        given:
        AlertStopList alertStopList=new AlertStopList(productName: '{"3":[{"name":"paracetamol"},{"name":"paracetamol"}]}',
                eventName: '{"1":[{"name":"event"},{"name":"event"}]}',dateCreated: new Date(),createdBy: "createdBy")
        when:
        String result=service.saveList(alertStopList)
        then:
        AlertStopList.get(3).productName=='{"3":[{"name":"paracetamol"},{"name":"paracetamol"}]}'
        result==null
    }
    void "test saveList when exception occurs"(){
        setup:
        AlertStopList alertStopList=new AlertStopList(productName: '{"3":[{"name":"paracetamol"},{"name":"paracetamol"}]}',
                eventName: '{"1":[{"name":"event"},{"name":"event"}]}')
        SpringSecurityService springSecurityService=Mock(SpringSecurityService)
        springSecurityService.isLoggedIn()>>{
            return false
        }
        service.CRUDService.userService.springSecurityService=springSecurityService
        when:
        AlertStopList result=service.saveList(alertStopList)
        then:
        AlertStopList.get(3)==null
        result.errors!=null
    }
    void "test updateList on success"(){
        given:
        AlertStopList alertStopList=new AlertStopList(productName: '{"3":[{"name":"paracetamol"},{"name":"paracetamol"}]}',
                eventName: '{"1":[{"name":"event"},{"name":"event"}]}',dateCreated: new Date(),createdBy: "createdBy")
        when:
        AlertStopList result=service.updateList(alertStopList)
        then:
        result.productName=='{"3":[{"name":"paracetamol"},{"name":"paracetamol"}]}'
    }
    void "test updateList when exception occurs"(){
        given:
        AlertStopList alertStopList=new AlertStopList(productName: '{"3":[{"name":"paracetamol"},{"name":"paracetamol"}]}',
                eventName: '{"1":[{"name":"event"},{"name":"event"}]}',dateCreated: new Date())
        SpringSecurityService springSecurityService=Mock(SpringSecurityService)
        springSecurityService.isLoggedIn()>>{
            return false
        }
        service.CRUDService.userService.springSecurityService=springSecurityService
        when:
        AlertStopList result=service.updateList(alertStopList)
        then:
        result==null
    }
}
