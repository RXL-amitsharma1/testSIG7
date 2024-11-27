package unit.com.rxlogix

import com.rxlogix.CRUDService
import com.rxlogix.Constants
import com.rxlogix.GroupService
import com.rxlogix.commandObjects.GroupCO
import com.rxlogix.config.Disposition
import com.rxlogix.enums.GroupType
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.test.hibernate.HibernateSpec
import grails.testing.services.ServiceUnitTest

class GroupServiceSpec extends HibernateSpec implements ServiceUnitTest<GroupService>{

    GroupCO workflowGroupCOInstance
    GroupCO userGroupCOInstance
    Group groupInstance
    Disposition defaultDisposition
    Disposition defaultSignalDisposition

    List<Class> getDomainClasses() { [User,Group, Disposition] }

    def setup() {
        service.transactionManager = transactionManager

        defaultDisposition = new Disposition(displayName: "New", value: "New", id: 1234,abbreviation: "RR",validatedConfirmed: false)
        defaultDisposition.save(flush:true,failOnError:true)

        defaultSignalDisposition =
                new Disposition(displayName: "Validated Signal", value: "Validated Signal", id: 1256,abbreviation: "CS",validatedConfirmed: true)
        defaultSignalDisposition.save(flush:true,failOnError:true)

        workflowGroupCOInstance = new GroupCO(name: "Workflow Group", groupType: GroupType.WORKFLOW_GROUP,
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                defaultSignalDisposition: defaultSignalDisposition)

        userGroupCOInstance = new GroupCO(name: "User Group", groupType: GroupType.USER_GROUP, defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition)

        groupInstance = new Group()

        //Mocking CRUDService
        CRUDService mockCRUDService = Mock(CRUDService)
        mockCRUDService.metaClass.save = { def groupInstance ->
            groupInstance
        }
        service.CRUDService = mockCRUDService
    }

    def cleanup() {
    }

    def "save a workflow group"() {
        when:
        Group group = service.saveGroupObject(workflowGroupCOInstance, groupInstance)

        then:
        group.defaultQualiDisposition == defaultDisposition
        group.defaultQuantDisposition == defaultDisposition
        group.defaultAdhocDisposition == defaultDisposition
        group.defaultEvdasDisposition == defaultDisposition
        group.defaultLitDisposition == defaultDisposition
        group.defaultSignalDisposition == defaultSignalDisposition

    }

    def "save a user group"() {
        when:
        Group group = service.saveGroupObject(userGroupCOInstance,groupInstance)

        then:
        group.defaultQualiDisposition == null
        group.defaultQuantDisposition == null
        group.defaultAdhocDisposition == null
        group.defaultEvdasDisposition == null
        group.defaultLitDisposition == null
        group.name == "User Group"
    }

    void "test getDispositionList"(){
        when:
        List<Map> getDispositionList = service.getDispositionList(false)

        then:
        getDispositionList[0].displayName == defaultDisposition.displayName
    }
}
