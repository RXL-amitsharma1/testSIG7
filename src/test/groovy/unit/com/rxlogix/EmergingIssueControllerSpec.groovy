package unit.com.rxlogix

import com.rxlogix.CRUDService
import com.rxlogix.EmailNotification
import com.rxlogix.EmergingIssueController
import com.rxlogix.EmergingIssueService
import com.rxlogix.UserService
import com.rxlogix.config.Disposition
import com.rxlogix.config.Priority
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.EmergingIssue
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.user.UserGroupMapping
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Ignore
import spock.lang.Specification
import spock.util.mop.Use

class EmergingIssueControllerSpec extends Specification implements ControllerUnitTest<EmergingIssueController> {
    Priority priority
    Disposition disposition,defaultDisposition,autoRouteDisposition
    Group group
    User user

    def setup(){
        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "vs")
        disposition.save(failOnError: true)

        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")

        [defaultDisposition, autoRouteDisposition].collect { it.save(failOnError: true) }

        // Prepare the mock Group
        group = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP, defaultDisposition: defaultDisposition,
                defaultSignalDisposition: disposition, autoRouteDisposition: autoRouteDisposition, justificationText: "Update Disposition",
                forceJustification: true, defaultQualiDisposition: disposition, defaultQuantDisposition: disposition,
                defaultAdhocDisposition: disposition, defaultEvdasDisposition: disposition, defaultLitDisposition: disposition,
                createdBy: "createdBy", modifiedBy: "modifiedBy")
        group.save(validate: false)

        Group wfGroup = new Group(name: "Default", createdBy: "createdBy", modifiedBy: "modifiedBy", groupType: GroupType.WORKFLOW_GROUP,
                defaultQualiDisposition: disposition, defaultQuantDisposition: disposition, defaultAdhocDisposition: disposition,
                defaultEvdasDisposition: disposition, defaultLitDisposition: disposition, defaultSignalDisposition: disposition,
                autoRouteDisposition: autoRouteDisposition, justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(flush: true)

        // Prepare the mock user
        user = new User(id: 1L, username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.addToGroups(group)
        UserGroupMapping userGroupMapping = new UserGroupMapping(user: user, group: wfGroup)
        userGroupMapping.save(flush: true, failOnError: false)
        user.save(validate: false)
        EmergingIssue emergingIssue1=new EmergingIssue(eventName: '{"3":[{"name":"event11"},{"name":"event12"}]}',createdBy: "createdBy1",dateCreated: new Date()-1)
        emergingIssue1.save(failOnError:true)
        EmergingIssue emergingIssue2=new EmergingIssue(eventName: '{"1":[{"name":"event21"},{"name":"event22"}]}',createdBy: "createdBy2",dateCreated: new Date())
        emergingIssue2.save(failOnError:true)
        SpringSecurityService springSecurityService=Mock(SpringSecurityService)
        springSecurityService.isLoggedIn()>>{
            return true
        }
        springSecurityService.principal>>{
            return user
        }
        controller.emergingIssueService.CRUDService.userService.springSecurityService=springSecurityService
    }
    def cleanup(){

    }
    void "test action index"(){
        when:
        controller.index()
        then:
        response.status==200
        view=="/emergingIssue/index"
        model.emergingIusseList.getClass()== EmergingIssue
        model.callingScreen=="index"
    }
    void "test list action when there are Emerging issue"(){
        when:
        controller.list()
        then:
        response.status==200
        JSON.parse(response.text).size()==2
        JSON.parse(response.text)[0].id==EmergingIssue.get(2).id
        JSON.parse(response.text)[1].id==EmergingIssue.get(1).id
    }
    void "test list action when there are no Emerging issue"(){
        setup:
        EmergingIssue.list().each{
            it.delete()
        }
        when:
        controller.list()
        then:
        response.status==200
        JSON.parse(response.text).size()==0
    }
    @Ignore
    void "test save action"(){
        given:
        String eventSelection='{"3":[{"name":"event1"},{"name":"event2"}]}'
        when:
        controller.save(null,null,null,null,eventSelection)
        then:
        response.status==302
        response.redirectedUrl=="/emergingIssue/index"
        EmergingIssue.get(3).eventName=='{"3":[{"name":"event1"},{"name":"event2"}]}'
    }
    void "test edit action"(){
        when:
        controller.edit(1L)
        then:
        response.status==200
        view=="/emergingIssue/index"
        model.callingScreen=="edit"
        model.emergingIusseList==EmergingIssue.get(1L)
    }
    void "test update action"(){
        given:
        String eventSelection='{"3":[{"name":"new event"},{"name":"new event"}]}'
        params.id=1L
        when:
        controller.update(null,null,null,null,eventSelection)
        then:
        response.status==302
        response.redirectedUrl=="/emergingIssue/index"
        EmergingIssue.get(1).eventName==eventSelection
    }
    void "test delete action"(){
        given:
        EmergingIssueService mockEmergingIssueService = Mock(EmergingIssueService)
        mockEmergingIssueService.delete(_,_) >> {
            return ;
        }
        controller.emergingIssueService = mockEmergingIssueService
        when:
        controller.delete(1L)
        then:
        response.status==200
        flash.message== "app.label.event.delete.success"
        JSON.parse(response.text).status==true
    }
}
