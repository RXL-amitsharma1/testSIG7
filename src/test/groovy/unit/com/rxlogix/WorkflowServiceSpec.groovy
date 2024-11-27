package unit.com.rxlogix

import com.rxlogix.WorkflowService
import com.rxlogix.config.Disposition
import com.rxlogix.config.PVSState
import com.rxlogix.config.Priority
import com.rxlogix.config.TopicCategory
import com.rxlogix.config.WorkflowRule
import com.rxlogix.enums.GroupType
import com.rxlogix.user.Group
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification


class WorkflowServiceSpec extends Specification implements ServiceUnitTest<WorkflowService> {
    def setup() {
        TopicCategory topicCategory1=new TopicCategory(name: "topic category1")
        topicCategory1.save(flush:true,failOnError:true)
        TopicCategory topicCategory2=new TopicCategory(name: "topic category2")
        topicCategory2.save(flush:true,failOnError:true)
        mockDomain(PVSState, [
                [value:"New", displayName: "New"],
                [value: "State 1", displayName: "State 1"],
                [value: "State 2", displayName: "State 2"],
                [value: "State 3", displayName: "State 3"]
        ])
        Disposition disposition1=new Disposition(abbreviation: "C",value: "Requires Review", displayName: "Requires Review", validatedConfirmed: false)
        disposition1.save(flush:true,failOnError:true)
        Disposition disposition2=new Disposition(abbreviation: "C",value: "New Potential Signal", displayName: "New Potential Signal", validatedConfirmed: false)
        disposition2.save(flush:true,failOnError:true)
        Group wfGroup1 = new Group(name: "Default1", groupType: GroupType.WORKFLOW_GROUP, createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        wfGroup1.save(validate: false)
        Group wfGroup2 = new Group(name: "Default2", groupType: GroupType.WORKFLOW_GROUP, createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        wfGroup2.save(validate: false)
        mockDomain(User, [
                [username: 'admin', createdBy:'tester', modifiedBy: "tester", groups: [wfGroup1]],
                [username: 'glenn', createdBy:'tester', modifiedBy: "tester", groups: wfGroup2],
                [username: 'anon', createdBy:'tester', modifiedBy: "tester", groups: [wfGroup1]]
        ])
        List<PVSState> states = PVSState.findAll()
        mockDomain(WorkflowRule,[
                [name: "Rule New", incomeState: states.get(0), targetState: states.get(1), allowedGroups: Group.getAll(),
                 allowedDispositions:Disposition.getAll(),approveRequired:true],
                [name: "Rule 1", incomeState: states.get(1), targetState: states.get(2), allowedGroups: [wfGroup1],
                 allowedDispositions:Disposition.getAll()],
                [name: "Rule 2", incomeState: states.get(1), targetState: states.get(2),signalRule:true,
                 allowedDispositions:Disposition.getAll(),topicCategories:TopicCategory.getAll()],
                [name: "Rule 3", incomeState: states.get(2), targetState: states.get(3)],
                [name: "Rule 4", incomeState: states.get(2), targetState: states.get(1),allowedDispositions:Disposition.get(1)],
                [name: "Rule 5", incomeState: states.get(1), targetState: states.get(2),signalRule:true,
                 allowedDispositions:Disposition.getAll(),topicCategories:TopicCategory.getAll(),approveRequired:true]
        ])
        mockDomain(Priority,[
                [value: "serious",displayName: "serious",reviewPerid:15],
                [value: "fatal",displayName: "fatal",reviewPerid:7],
                [value: "non-serious",displayName: "non-serious",reviewPerid:60]
        ])
    }

    def cleanup() {
    }

    void "test workflowStates"(){
        when:
        List result=service.workflowStates()
        then:
        result.size()==4
        result[0]==PVSState.get(1)
        result[1]==PVSState.get(2)
        result[2]==PVSState.get(3)
        result[3]==PVSState.get(4)
    }
    void "test priorities"(){
        when:
        List result=service.priorities()
        then:
        result.size()==3
        result[0]==Priority.get(1)
        result[1]==Priority.get(2)
        result[2]==Priority.get(3)
    }
    void "test dispositions"(){
        when:
        List result=service.dispositions()
        then:
        result.size()==2
        result[0]==Disposition.get(1)
        result[1]==Disposition.get(2)
    }
    void "test getAvailableWorkflowStates when groups is given"(){
        when:
        List result=service.getAvailableWorkflowStates(PVSState.get(1),Group.getAll() as Set)
        then:
        result.approvalRequired==[true]
        result.value==[WorkflowRule.get(1).targetState.value]
        result.displayName==[WorkflowRule.get(1).targetState.displayName]
        result.dispositions[0].size()==2
    }
    void "test getAvailableWorkflowStates when groups is not given"(){
        when:
        List result=service.getAvailableWorkflowStates(PVSState.get(3),null)
        then:
        result*.approvalRequired==[false,false]
        result.value==["State 3","State 1"]
        result.dispositions.value==[[Disposition.get(1).value]]
    }
    void "test getSignalsForSignal"(){
        when:
        List result=service.getSignalsForSignal(WorkflowRule.get(6),PVSState.get(2),Group.getAll())
        then:
        result.size()==2
        result.approvalRequired==[false,true]
        result.value==["State 2","State 2"]
    }
    void "test getDispositions"(){
        when:
        Set result=service.getDispositions(PVSState.get(1))
        then:
        result.size()==2
        result[0]==Disposition.get(1)
        result[1]==Disposition.get(2)
    }
    void "test allUsers"(){
        when:
        List result=service.allUsers()
        then:
        result.size()==3
        result[0]==User.get(1)
        result[1]==User.get(2)
        result[2]==User.get(3)
    }
}