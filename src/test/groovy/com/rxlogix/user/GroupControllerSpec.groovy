package com.rxlogix.user

import com.rxlogix.AlertService
import com.rxlogix.CRUDService
import com.rxlogix.Constants
import com.rxlogix.GroupService
import com.rxlogix.ProductDictionaryCacheService
import com.rxlogix.PvsProductDictionaryService
import com.rxlogix.cache.CacheService
import com.rxlogix.commandObjects.GroupCO
import com.rxlogix.config.Disposition
import com.rxlogix.enums.GroupType
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class GroupControllerSpec extends Specification implements ControllerUnitTest<GroupController> {

    GroupCO workflowGroupCOInstance
    GroupCO userGroupCOInstance
    Group groupInstance
    Disposition defaultDisposition
    Disposition defaultSignalDisposition

    def setup() {

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

        userGroupCOInstance = new GroupCO(name: "User Group", groupType: GroupType.USER_GROUP,
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition)

        groupInstance =
                new Group(name: "GroupInstance", createdBy: "user", modifiedBy: "user", groupType: GroupType.WORKFLOW_GROUP, defaultSignalDisposition: defaultSignalDisposition,
                        defaultQualiDisposition: defaultDisposition,
                        defaultQuantDisposition: defaultDisposition,
                        defaultAdhocDisposition: defaultDisposition,
                        defaultEvdasDisposition: defaultDisposition,
                        defaultLitDisposition: defaultDisposition
                )
        groupInstance.save(flush: true)

        CRUDService mockCRUDService = Mock(CRUDService)
        mockCRUDService.metaClass.save = { def groupInstance ->
            groupInstance
        }
        controller.groupService.CRUDService = mockCRUDService

        Group groupInstanceUserGroup =
                new Group(name: "GroupInstanceUserGroup",createdBy: "user",modifiedBy: "user",groupType: GroupType.USER_GROUP)
        Group groupInstanceWorkflowGroup =
                new Group(name: "GroupInstanceWorkflowGroup",createdBy: "user",modifiedBy: "user",groupType: GroupType.WORKFLOW_GROUP)
        CacheService mockCacheService = Mock(CacheService)
        mockCacheService.setGroupCache(groupInstanceUserGroup) >> {
            return groupInstanceUserGroup.id
        }
        mockCacheService.setGroupCache(groupInstanceWorkflowGroup) >> {
            return groupInstanceWorkflowGroup.id
        }
        controller.cacheService = mockCacheService

        GroupService mockGroupService = Mock(GroupService)
        mockGroupService.getDispositionList(false) >> {
            return [[displayName: "New", id: 1234]]
        }
        mockGroupService.getDispositionList(true) >> {
            return [[displayName: "Validated Signal", id: 1256]]
        }
        mockGroupService.saveGroupObject(_,_)>>{
            return groupInstance
        }
        controller.groupService = mockGroupService
    }

    def cleanup() {
    }

    def "test index action"() {
        given:
        Group group = new Group(name: "Group 1")
        Group group2 = new Group(name: "Group 2")
        group.save(validate:false)
        group2.save(validate:false)

        when:
        controller.index()

        then:
        response.status == 200
        view == '/group/index'
        model.groupInstanceList.size() == 3  //One GroupInstance saved in setUp
        model.groupInstanceTotal == 3
    }

    void "test create action"(){
        when:
        params.name = "GroupInstance"
        controller.create()

        then:
        response.status == 200
        view == '/group/create'
        groupInstance.name == params.name
        model.defaultDispositionList[0][1] == Disposition.findById(1234)
        model.defaultSignalDispositionList[0][1] == Disposition.findById(1256)
    }

    void "test groupDispositionList action based on validatedConfirmed"() {
        when:
        Map groupDispositionLists = controller.groupDispositionLists(groupInstance)

        then:
        groupDispositionLists.defaultDispositionList[0][1] == Disposition.findById(1234)
        groupDispositionLists.defaultSignalDispositionList[0][1] == Disposition.findById(1256)
    }

    void "test save action when Group already exists"(){
        when:
        request.method = 'POST'
        params.name = "GroupInstance"
        controller.save(workflowGroupCOInstance)

        then:
        response.status == 200
        view == '/group/create'
        controller.flash.error !=null
        model.defaultDispositionList[0][1] == Disposition.findById(1234)
        model.defaultSignalDispositionList[0][1] == Disposition.findById(1256)
    }

    void "test save action when GroupType is WorkFlow Group"(){
        when:
        request.method = 'POST'
        params.id = 1
        params.name = "GroupInstanceNew"
        params.createdBy = "user"
        params.modifiedBy = "user"
        params.groupType = GroupType.WORKFLOW_GROUP
        controller.save(workflowGroupCOInstance)

        then:
        response.status == 302
        response.redirectedUrl == '/group/show/'+params.id
        controller.flash.message != null
        controller.flash.defaultMessage.endsWith("created") == true
    }


    void "test save action when GroupType is User Group"(){
        when:
        request.method = 'POST'
        params.id = 1
        params.name = "GroupInstanceNew"
        params.createdBy = "user"
        params.modifiedBy = "user"
        params.groupType = GroupType.USER_GROUP
        controller.save(userGroupCOInstance)

        then:
        response.status == 302
        response.redirectedUrl == '/group/show/'+params.id
        controller.flash.message != null
        controller.flash.defaultMessage.endsWith("created") == true
    }

    void "test save action when validation error occurs"(){
        when:
        request.method = 'POST'
        params.id = 2
        params.name = "GroupInstanceNew"
        params.createdBy = "user"
        params.modifiedBy = "user"
        params.groupType = GroupType.WORKFLOW_GROUP
        controller.save(null)

        then:
        response.status == 200
        view == '/group/create'
        model.defaultDispositionList[0][1] == Disposition.findById(1234)
        model.defaultSignalDispositionList[0][1] == Disposition.findById(1256)
    }

    void "test setAllowedProducts when productParams is instanceOf Collection"(){
        when:
        params['allowedProductList'] = [[productName: "Paracetamol"]]
        controller.setAllowedProducts(groupInstance)

        then:
        groupInstance.allowedProductList == params['allowedProductList']
    }

    void "test setAllowedProducts when productParams is null"(){
        when:
        params['allowedProductList'] = null
        controller.setAllowedProducts(groupInstance)

        then:
        groupInstance.allowedProductList == null
    }

    void "test setAllowedProducts when productParams is not an instanceOf Collection"(){
        when:
        params['allowedProductList'] = [productName: "Paracetamol"]
        controller.setAllowedProducts(groupInstance)

        then:
        groupInstance.allowedProductList == [params['allowedProductList']]
    }

    void "test show action"(){
        when:
        params.id = 1
        controller.show(params.id)

        then:
        response.status == 200
        view == '/group/show'
        model.groupInstance.id == params.id
    }

    void "test show action for which groupInstance does not exist"(){
        when:
        params.id = -1L
        controller.show(params.id)

        then:
        response.status == 302
        response.redirectedUrl == '/group/list'
        controller.flash.message != null
    }

    void "test edit action"(){
        when:
        params.id = 1
        controller.edit()

        then:
        response.status == 200
        view == '/group/edit'
        model.defaultDispositionList[0][1] == Disposition.findById(1234)
    }

    void "test edit action for which groupInstance does not exist"(){
        when:
        params.id = -1L
        controller.edit()

        then:
        response.status == 302
        response.redirectedUrl == '/group/list'
        controller.flash.message != null
    }

    void "test update action when group already exists"(){
        setup:
        Group groupInstanceNew =
                new Group(id: 2L, name: "GroupInstanceNew", createdBy: "user", modifiedBy: "user", groupType: GroupType.WORKFLOW_GROUP,
                        defaultSignalDisposition: defaultSignalDisposition,
                        defaultQualiDisposition: defaultDisposition,
                        defaultQuantDisposition: defaultDisposition,
                        defaultAdhocDisposition: defaultDisposition,
                        defaultEvdasDisposition: defaultDisposition,
                        defaultLitDisposition: defaultDisposition)
        groupInstanceNew.save(flush: true)

        when:
        request.method = 'POST'
        params.id = 1
        params.name = "GroupInstanceNew"
        params.createdBy = "user"
        params.modifiedBy = "user"
        params.groupType = GroupType.WORKFLOW_GROUP
        controller.update(workflowGroupCOInstance)

        then:
        response.status == 200
        view == '/group/edit'
        controller.flash.error !=null
        model.defaultDispositionList[0][1] == Disposition.findById(1234)
        model.defaultSignalDispositionList[0][1] == Disposition.findById(1256)
    }

    void "test update based on version"(){
        setup:
        Group groupInstanceNew = Group.get(1)
        groupInstanceNew.createdBy = "newUser"
        groupInstanceNew.save(flush:true)
        Group groupInstanceNew2 = Group.get(1)
        groupInstanceNew2.createdBy = "newUser"
        groupInstanceNew2.save(flush:true)

        when:
        request.method = 'POST'
        params.version = 1
        params.id = 1
        params.name = "GroupInstance"
        params.createdBy = "user"
        params.modifiedBy = "user"
        params.groupType = GroupType.WORKFLOW_GROUP
        controller.update(workflowGroupCOInstance)

        then:
        response.status == 200
        view == '/group/edit'
        model.defaultDispositionList[0][1] == Disposition.findById(1234)
        model.defaultSignalDispositionList[0][1] == Disposition.findById(1256)
    }

    void "test update action when GroupType is WorkFlow Group"(){
        when:
        request.method = 'POST'
        params.id = 1
        params.name = "GroupInstance"
        params.createdBy = "user"
        params.modifiedBy = "user"
        params.groupType = GroupType.WORKFLOW_GROUP
        controller.update(workflowGroupCOInstance)

        then:
        response.status == 302
        response.redirectedUrl == '/group/show/'+params.id
        controller.flash.message != null
        controller.flash.defaultMessage.endsWith("updated") == true
    }


    void "test update action when GroupType is User Group"(){
        when:
        request.method = 'POST'
        params.id = 1
        params.name = "GroupInstance"
        params.createdBy = "user"
        params.modifiedBy = "user"
        params.groupType = GroupType.USER_GROUP
        controller.update(userGroupCOInstance)

        then:
        response.status == 302
        response.redirectedUrl == '/group/show/'+params.id
        controller.flash.message != null
        controller.flash.defaultMessage.endsWith("updated") == true
    }

    void "test update action when validation error occurs"(){
        when:
        request.method = 'POST'
        params.id = 1
        params.name = "GroupInstance"
        params.createdBy = "user"
        params.modifiedBy = "user"
        params.groupType = GroupType.WORKFLOW_GROUP
        controller.update(null)

        then:
        response.status == 200
        view == '/group/edit'
        model.defaultDispositionList[0][1] == Disposition.findById(1234)
        model.defaultSignalDispositionList[0][1] == Disposition.findById(1256)
    }

    void "test update action when group instance is not found"(){
        when:
        request.method = 'POST'
        params.id = 2
        controller.update()

        then:
        response.status == 302
        response.redirectedUrl == '/group/edit/'+params.id
        controller.flash.message != null
        controller.flash.defaultMessage == "Group not found with id "+params.id
    }

    void "test delete action when group instance is found"(){
        when:
        request.method = 'POST'
        params.id = 1
        controller.delete()

        then:
        response.status == 302
        response.redirectedUrl == '/group/list'
        controller.flash.message != null
        controller.flash.defaultMessage == "Group "+groupInstance.name+" deleted"
    }

    void "test delete action when group instance is not found"(){
        when:
        request.method = 'POST'
        params.id = 2
        controller.delete()

        then:
        response.status == 302
        response.redirectedUrl == '/group/list'
        controller.flash.message != null
        controller.flash.defaultMessage == "Group not found with id "+params.id
    }

    void "test getProducts"(){
        setup:
        String searchTerm = "para"
        PvsProductDictionaryService mockPvsProductDictionaryService = Mock(PvsProductDictionaryService)
        mockPvsProductDictionaryService.getProducts(searchTerm) >> {
            return ['TestParaProduct', 'Paracetamol', 'Paracetamol M']
        }
        controller.pvsProductDictionaryService = mockPvsProductDictionaryService

        when:
        request.contentType = 'application/json'
        params.searchProduct = "para"
        controller.getProducts(params)

        then:
        response.status == 200
    }
}
