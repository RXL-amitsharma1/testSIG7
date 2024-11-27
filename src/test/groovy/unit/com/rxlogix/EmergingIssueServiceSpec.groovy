package unit.com.rxlogix

import com.rxlogix.CRUDService
import com.rxlogix.EmergingIssueService
import com.rxlogix.UserService
import com.rxlogix.signal.EmergingIssue
import com.rxlogix.user.User
import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.services.ServiceUnitTest
import spock.lang.Ignore
import spock.lang.Specification

class EmergingIssueServiceSpec extends Specification implements ServiceUnitTest<EmergingIssueService> {
    def setup(){
        User user=new User(username: "user1",fullName: "user1")
        user.save(validate:false)
        EmergingIssue emergingIssue1=new EmergingIssue(eventName: '{"4":[{"name":"event11"},{"name":"event12"}]}',createdBy: "createdBy1",dateCreated: new Date()-1)
        emergingIssue1.save(failOnError:true)
        EmergingIssue emergingIssue2=new EmergingIssue(eventName: '{"1":[{"name":"event21"},{"name":"event22"}]}',createdBy: "createdBy2",dateCreated: new Date())
        emergingIssue2.save(failOnError:true)
        EmergingIssue emergingIssue3=new EmergingIssue(productSelection: '{"1":[],"2":[],"3":[{"name":"Test Product d","id":"100022"}],"4":[],"5":[]}',eventName: '{"1":[{"name":"event21"},{"name":"event22"}]}',createdBy: "createdBy2",dateCreated: new Date())
        emergingIssue2.save(failOnError:true)
        EmergingIssue emergingIssue4=new EmergingIssue(productSelection: null,eventName: '{"1":[{"name":"event21"},{"name":"event22"}]}',createdBy: "createdBy2",dateCreated: new Date(), productGroupSelection: '[{"name":"testing_AS (3)","id":"3"}]')
        emergingIssue2.save(failOnError:true)
        SpringSecurityService springSecurityService=Mock(SpringSecurityService)
        springSecurityService.isLoggedIn()>>{
            return true
        }
        springSecurityService.principal>>{
            return user
        }
        service.CRUDService.userService.springSecurityService=springSecurityService
    }
    def cleanup(){

    }

    void "test getEmergingIssues when there are emerging issue"(){
        when:
        List result = service.getEmergingIssues()
        then:
        result==["event11", "event12", null]
    }
    @Ignore
    void "test getEmergingIssues when there are no emerging issue"(){
        setup:
        EmergingIssue.list().each{
            it.delete()
        }
        when:
        List result=service.getEmergingIssues()
        then:
        result.size()==0
    }
    @Ignore
    void "test getEmergingIssueList when there are emerging issue"(){
        when:
        List result=service.getEmergingIssueList(params)
        then:
        result.get(0).id==2
        result.get(1).id==1
    }
    @Ignore
    void "test getEmergingIssueList when there are no emerging issue"(){
        setup:
        EmergingIssue.list().each{
            it.delete()
        }
        when:
        List result=service.getEmergingIssueList(params)
        then:
        result.size()==0
    }
    void "test prepareEventMap"(){
        given:
        String eventName='{"2":[{"name":"event1"},{"name":"event2"}]}'
        when:
        Map result=service.prepareEventMap(eventName,[])
        then:
        result==["HLGT":["event1","event2"]]
    }
    void "test save on success"(){
        given:
        EmergingIssue emergingIssue=new EmergingIssue(eventName: '{"4":[{"name":"new event1"},{"name":"new event2"}]}',dateCreated: new Date())
        when:
        EmergingIssue result=service.save(emergingIssue)
        then:
        EmergingIssue newEmergingIssue=EmergingIssue.get(3)
        result==null
        newEmergingIssue.get(3)!=null
        newEmergingIssue.get(3).eventName=='{"4":[{"name":"new event1"},{"name":"new event2"}]}'
    }
    void "test save when exception occurs"(){
        setup:
        EmergingIssue emergingIssue=new EmergingIssue(eventName: '{"4":[{"name":"new event1"},{"name":"new event2"}]}',dateCreated: new Date())
        SpringSecurityService springSecurityService=Mock(SpringSecurityService)
        springSecurityService.isLoggedIn()>>{
            return false
        }
        service.CRUDService.userService.springSecurityService=springSecurityService
        when:
        EmergingIssue result=service.save(emergingIssue)
        then:
        result.errors!=null
    }
    void "test update on success"(){
        given:
        EmergingIssue emergingIssue=EmergingIssue.get(1)
        emergingIssue.eventName='{"1":[{"name":"new event1"},{"name":"new event2"}]}'
        when:
        service.update(emergingIssue)
        then:
        EmergingIssue.get(1).eventName=='{"1":[{"name":"new event1"},{"name":"new event2"}]}'
    }
    void "test update when exception occurs"(){
        setup:
        EmergingIssue emergingIssue=EmergingIssue.get(1)
        emergingIssue.eventName='{"1":[{"name":"new event1"},{"name":"new event2"}]}'
        emergingIssue.createdBy=null
        SpringSecurityService springSecurityService=Mock(SpringSecurityService)
        springSecurityService.isLoggedIn()>>{
            return false
        }
        service.CRUDService.userService.springSecurityService=springSecurityService
        when:
        service.update(emergingIssue)
        then:
        EmergingIssue.get(1)==null
    }
    void "test getEmergingIssueListReport()"(){
        setup:

        when:
        List emergingIssueList = service.getEmergingIssueListReport()
        then:
        emergingIssueList.size() == 2
    }
    void "test prepareProductMap()"(){
        setup:
        String jsonString = '{"1":[],"2":[],"3":[{"name":"Test Product d","id":"100022"}],"4":[],"5":[]}'
        String dataSourceDict = "pva"
        when:
        Map emergingIssueMap = service.prepareProductMap(jsonString, dataSourceDict)
        then:
        emergingIssueMap['Product Name'] == ["Test Product d(Safety DB)"]
    }
    void "test getGroupNameFieldFromJsonProduct()"(){
        setup:
        String jsonString = '[{"name":"testing_AS (3)","id":"3"}]'
        String dataSourceDict = "pva;pva,faers"
        when:
        String groupName = service.getGroupNameFieldFromJsonProduct(jsonString, dataSourceDict)
        then:
        groupName == "testing_AS(pva,faers)"
    }
    void "test prepareProductStringReport()"(){
        setup:
        String jsonString = '{"1":[],"2":[],"3":[{"name":"Test Product d","id":"100022"}],"4":[],"5":[]}'
        String dataSourceDict = "pva;pva,faers"
        when:
        String groupName = service.prepareProductStringReport(jsonString, null, dataSourceDict)
        then:
        groupName == "Product Name : Test Product d(Safety DB)"
    }
    void "test fetchProductNameForMatching()"(){
        setup:
        String jsonString = '{"1":[],"2":[],"3":[{"name":"Test Product d","id":"100022"}],"4":[],"5":[]}'
        when:
        List products = service.fetchProductNameForMatching(jsonString)
        then:
        products.size() == 1
    }
    void "test fetchPGNameForMatching()"(){
        setup:
        String jsonString = '[{"name":"testing_AS (3)","id":"3"}]'
        when:
        List productGroups = service.fetchPGNameForMatching(jsonString)
        then:
        productGroups.size() == 1
    }


}
