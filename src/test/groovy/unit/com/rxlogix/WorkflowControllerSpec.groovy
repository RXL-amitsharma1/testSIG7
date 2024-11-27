package unit.com.rxlogix

import com.rxlogix.Constants
import com.rxlogix.SafetyLeadSecurityService
import com.rxlogix.UserService
import com.rxlogix.WorkflowController
import com.rxlogix.WorkflowService
import com.rxlogix.config.Disposition
import com.rxlogix.config.EvdasAlert
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.PVSState
import com.rxlogix.config.Priority
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.Alert
import com.rxlogix.signal.Justification
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.user.Group
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.converters.JSON
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification


@Ignore
class WorkflowControllerSpec extends Specification implements ControllerUnitTest<WorkflowController> {
    @Shared User user1
    @Shared User user2
    @Shared Group wfGroup1
    @Shared SingleCaseAlert singleCaseAlert1
    @Shared SingleCaseAlert singleCaseAlert2
    def setup(){
        mockDomain(PVSState, [
                [value:"New", displayName: "New"],
                [value: "State 1", displayName: "State 1"],
                [value: "State 2", displayName: "State 2"],
                [value: "State 3", displayName: "State 3"]
        ])
        mockDomain(Priority,[
                [value: "serious",displayName: "serious",reviewPeriod:15],
                [value: "fatal",displayName: "fatal",reviewPeriod:7],
                [value: "non-serious",displayName: "non-serious",reviewPeriod: 60]
        ])
        mockDomain(Justification,[
                [name: "justification1",justification:"justification1",feature:"justification feature1",attributesMap:
                        ["alertPriority":"on"]],
                [name: "justification2",justification:"justification2",feature:"justification feature2",attributesMap:
                        ["alertWorkflow":"on"]]
        ])
        Disposition disposition1=new Disposition(abbreviation: "C",value: "Requires Review", displayName: "Requires Review", validatedConfirmed: false)
        disposition1.save(flush:true,failOnError:true)
        Disposition disposition2=new Disposition(abbreviation: "C",value: "New Potential Signal", displayName: "New Potential Signal", validatedConfirmed: false)
        disposition2.save(flush:true,failOnError:true)
        wfGroup1 = new Group(name: "Default1", groupType: GroupType.WORKFLOW_GROUP, createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        wfGroup1.save(validate: false)
        user1=createUser("user1",wfGroup1)
        user2=createUser("user2",wfGroup1)
        singleCaseAlert1 =new SingleCaseAlert(assignedTo: user1,priority: Priority.get(1),productName: "paracetamol",
                attributesMap: ["masterPrefTermAll_6":"DAN,NOR"])
        singleCaseAlert1.save(validate:false)
        singleCaseAlert2 =new SingleCaseAlert(assignedToGroup: wfGroup1,priority: Priority.get(2),productName: "paracetamol")
        singleCaseAlert2.save(validate:false)
        Alert alert1=new Alert(disposition: disposition1,assignedTo: user1,priority: Priority.get(3))
        alert1.save(validate:false)
        ExecutedConfiguration executedConfiguration1=new ExecutedConfiguration()
        executedConfiguration1.save(validate:false)
        ExecutedConfiguration executedConfiguration2=new ExecutedConfiguration(selectedDatasource: Constants.DataSource.FAERS)
        executedConfiguration2.save(validate:false)
        AggregateCaseAlert aggregateCaseAlert1=new AggregateCaseAlert(executedAlertConfiguration: executedConfiguration2,
                priority: Priority.get(2))
        aggregateCaseAlert1.save(validate:false)
        AggregateCaseAlert aggregateCaseAlert2=new AggregateCaseAlert(executedAlertConfiguration: executedConfiguration1,
                priority: Priority.get(1))
        EvdasAlert evdasAlert1=new EvdasAlert(priority: Priority.get(2))
        evdasAlert1.save(validate:false)
        aggregateCaseAlert2.save(validate:false)
        UserService userService=Mock(UserService)
        userService.getUser()>>{
            return user1
        }
        controller.userService=userService
        SafetyLeadSecurityService safetyLeadSecurityService=Mock(SafetyLeadSecurityService)
        safetyLeadSecurityService.isUserSafetyLead(_,_)>>{
            return true
        }
        controller.safetyLeadSecurityService=safetyLeadSecurityService
    }
    def cleanup(){
    }
    private User createUser(String username, Group wfGroup, String authority=null) {
        User.metaClass.encodePassword = { "password" }
        def preference = new Preference(locale: new Locale("en"),createdBy: "createdBy",modifiedBy: "modifiedBy")
        preference.save(validate:false)
        User user = new User(username: username, password: 'password', fullName: username, preference: preference, createdBy: 'createdBy', modifiedBy: 'modifiedBy',
                email: "${username}@gmail.com")
        user.addToGroups(wfGroup)
        user.save(failOnError: true)
        if(authority) {
            Role role = new Role(authority: authority, createdBy: 'createdBy', modifiedBy: 'modifiedBy').save(flush: true)
            UserRole.create(user, role, true)
        }
        return user
    }
    void "test workflowState"(){
        when:
        controller.workflowState()
        then:
        response.status==200
        JSON.parse(response.text).size()==4
        JSON.parse(response.text)[0].id==PVSState.get(1).id
        JSON.parse(response.text)[1].id==PVSState.get(2).id
        JSON.parse(response.text)[2].id==PVSState.get(3).id
        JSON.parse(response.text)[3].id==PVSState.get(4).id
    }
    void "test priorities"(){
        when:
        controller.priorities()
        then:
        response.status==200
        JSON.parse(response.text).size()==3
        JSON.parse(response.text)[0].id==Priority.get(1).id
        JSON.parse(response.text)[1].id==Priority.get(2).id
        JSON.parse(response.text)[2].id==Priority.get(3).id
    }
    void "test getPriority when you have access and aap name is single case alert"(){
        setup:
        params.appName="Single Case Alert"
        params.checkedIdList="[1,2]"
        when:
        controller.getPriority()
        then:
        response.status==200
        JSON.parse(response.text).size()==3
        JSON.parse(response.text).currentValue=="fatal"
        JSON.parse(response.text).availableValues.size()==3
        JSON.parse(response.text).justification.size()==1
    }
    void "test getPriority when you have access and aap name is aggregate case alert"(){
        setup:
        params.appName="Aggregate Case Alert"
        params.checkedIdList="[1]"
        when:
        controller.getPriority()
        then:
        response.status==200
        JSON.parse(response.text).size()==3
        JSON.parse(response.text).currentValue=="fatal"
        JSON.parse(response.text).availableValues.size()==3
        JSON.parse(response.text).justification.size()==1
    }
    void "test getPriority when you don't have access and aap name is aggregate case alert and data source is faers"(){
        setup:
        params.appName="Aggregate Case Alert"
        SafetyLeadSecurityService safetyLeadSecurityService=Mock(SafetyLeadSecurityService)
        safetyLeadSecurityService.isUserSafetyLead(_,_)>>{
            return false
        }
        controller.safetyLeadSecurityService=safetyLeadSecurityService
        params.checkedIdList="[1]"
        when:
        controller.getPriority()
        then:
        response.status==200
        JSON.parse(response.text).size()==3
        JSON.parse(response.text).currentValue=="fatal"
        JSON.parse(response.text).availableValues.size()==3
        JSON.parse(response.text).justification.size()==1
    }
    void "test getPriority when you don't have access and aap name is EVDAS Alert "(){
        setup:
        params.appName="EVDAS Alert"
        SafetyLeadSecurityService safetyLeadSecurityService=Mock(SafetyLeadSecurityService)
        safetyLeadSecurityService.isUserSafetyLead(_,_)>>{
            return false
        }
        controller.safetyLeadSecurityService=safetyLeadSecurityService
        params.checkedIdList="[1]"
        when:
        controller.getPriority()
        then:
        response.status==200
        JSON.parse(response.text).size()==3
        JSON.parse(response.text).currentValue=="fatal"
        JSON.parse(response.text).availableValues.size()==3
        JSON.parse(response.text).justification.size()==1
    }
    void "test getPriority when you have access and aap name doesn't match"(){
        setup:
        params.appName="Aggregate Case Alert"
        params.checkedIdList="[1]"
        when:
        controller.getPriority()
        then:
        response.status==200
        JSON.parse(response.text).size()==3
        JSON.parse(response.text).currentValue=="fatal"
        JSON.parse(response.text).availableValues.size()==3
        JSON.parse(response.text).justification.size()==1
    }
    @Ignore
    void "test getPriority when you don't have access "(){
        setup:
        params.appName="Single Case Alert"
        SafetyLeadSecurityService safetyLeadSecurityService=Mock(SafetyLeadSecurityService)
        safetyLeadSecurityService.isUserSafetyLead(_,_)>>{
            return false
        }
        controller.safetyLeadSecurityService=safetyLeadSecurityService
        params.checkedIdList="[1,2]"
        when:
        controller.getPriority()
        then:
        response.status==403
    }
    @Ignore
    void "test getWorkflowState"(){
        setup:
        params.appName="Single Case Alert"
        params.id=1
        when:
        controller.getWorkflowState()
        then:
        JSON.parse(response.text).size()==3
    }
    void "test getPriorityBatchUpdate when you have access and aap name is single case alert"(){
        given:
        params.appName="Single Case Alert"
        params.id=1
        when:
        controller.getPriorityBatchUpdate()
        then:
        response.status==200
        JSON.parse(response.text).size()==3
        JSON.parse(response.text).currentValue=="serious"
        JSON.parse(response.text).availableValues.size()==3
        JSON.parse(response.text).availableValues[0].displayName=="serious"
        JSON.parse(response.text).availableValues[1].displayName=="fatal"
        JSON.parse(response.text).availableValues[2].displayName=="non-serious"
        JSON.parse(response.text).justification[0].name=="justification1"
    }
    void "test getPriorityBatchUpdate when you have access and aap name is aggregate case alert"(){
        setup:
        params.appName="Aggregate Case Alert"
        params.id=1
        when:
        controller.getPriorityBatchUpdate()
        then:
        response.status==200
        JSON.parse(response.text).size()==3
        JSON.parse(response.text).currentValue=="fatal"
        JSON.parse(response.text).availableValues.size()==3
        JSON.parse(response.text).availableValues[0].displayName=="serious"
        JSON.parse(response.text).availableValues[1].displayName=="fatal"
        JSON.parse(response.text).availableValues[2].displayName=="non-serious"
        JSON.parse(response.text).justification[0].name=="justification1"
    }
    void "test getPriorityBatchUpdate when you have access and aap name doesn't match"(){
        setup:
        params.appName="Alert"
        params.id=1
        when:
        controller.getPriorityBatchUpdate()
        then:
        response.status==200
        JSON.parse(response.text).size()==3
        JSON.parse(response.text).currentValue=="non-serious"
        JSON.parse(response.text).availableValues.size()==3
        JSON.parse(response.text).availableValues[0].displayName=="serious"
        JSON.parse(response.text).availableValues[1].displayName=="fatal"
        JSON.parse(response.text).availableValues[2].displayName=="non-serious"
        JSON.parse(response.text).justification[0].name=="justification1"
    }
    void "test getPriorityBatchUpdate when you don't have access"(){
        setup:
        params.appName="Single Case Alert"
        params.id=1
        SafetyLeadSecurityService safetyLeadSecurityService=Mock(SafetyLeadSecurityService)
        safetyLeadSecurityService.isUserSafetyLead(_,_)>>{
            return false
        }
        controller.safetyLeadSecurityService=safetyLeadSecurityService
        when:
        controller.getPriorityBatchUpdate()
        then:
        response.status==403
    }
    void "test getDisposition"(){
        when:
        params.id=1
        controller.getDisposition()
        then:
        response.status==200
        JSON.parse(response.text).size()==2
        JSON.parse(response.text).currentValue==Disposition.get(1).value
        JSON.parse(response.text).availableValues.size()==2
    }
    void "test getAssignedTo"(){
        when:
        params.id=1
        controller.getAssignedTo()
        then:
        response.status==200
        JSON.parse(response.text).size()==2
        JSON.parse(response.text).currentValue==user1.fullName
        JSON.parse(response.text).availableValues.size()==2
    }
}