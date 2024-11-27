package com.rxlogix

import com.rxlogix.cache.CacheService
import com.rxlogix.config.Disposition
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.ProductAssignmentLog
import com.rxlogix.signal.ProductViewAssignment
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.hibernate.HibernateSpec
import grails.testing.services.ServiceUnitTest
import groovy.transform.SourceURI

import java.nio.file.Path
import java.nio.file.Paths

class ProductAssignmentServiceSpec extends HibernateSpec implements ServiceUnitTest<ProductAssignmentService> {

    User user
    User newUser
    Group wfGroup
    Group newWfGroup
    Group userGroup
    Group newUserGroup
    Disposition disposition
    Disposition defaultDisposition
    Disposition defaultSignalDisposition
    Disposition autoRouteDisposition
    Map dicMap

    File fileToRead

    List<Class> getDomainClasses() {
        [User, UserService, Disposition, Group, ProductAssignmentService, CacheService, ProductViewAssignment, ProductAssignmentLog]
    }

    def setup() {
        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "VO")
        disposition.save(flush: true, failOnError: true)
        defaultSignalDisposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "VO")
        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")

        [defaultDisposition, autoRouteDisposition].collect { it.save(failOnError: true) }

        wfGroup = new Group(name: "Default",
                groupType: GroupType.WORKFLOW_GROUP,
                createdBy: "user",
                modifiedBy: "user",
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                defaultSignalDisposition: disposition,
                autoRouteDisposition: autoRouteDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(flush: true)

        newWfGroup = new Group(name: "Test_Group",
                groupType: GroupType.WORKFLOW_GROUP,
                createdBy: "user",
                modifiedBy: "user",
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                defaultSignalDisposition: disposition,
                autoRouteDisposition: autoRouteDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        newWfGroup.save(flush: true)

        userGroup = new Group(name: "user_group",
                groupType: GroupType.USER_GROUP,
                createdBy: "user",
                modifiedBy: "user",
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                defaultSignalDisposition: disposition,
                autoRouteDisposition: autoRouteDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        userGroup.save(flush: true)

        newUserGroup = new Group(name: "new_user_group",
                groupType: GroupType.USER_GROUP,
                createdBy: "user",
                modifiedBy: "user",
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                defaultSignalDisposition: disposition,
                autoRouteDisposition: autoRouteDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        newUserGroup.save(flush: true)

        //Save the  user
        user = new User(id: '1', username: 'test_user', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.addToGroups(wfGroup)
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.timeZone = "UTC"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.save(flush: true)

        //Save the  user
        newUser = new User(id: '2', username: 'new_user', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        newUser.addToGroups(newWfGroup)
        newUser.preference.createdBy = "createdBy"
        newUser.preference.modifiedBy = "modifiedBy"
        newUser.preference.timeZone = "UTC"
        newUser.preference.locale = new Locale("en")
        newUser.preference.isEmailEnabled = false
        newUser.metaClass.getFullName = { ' New Fake Name' }
        newUser.metaClass.getEmail = { 'fake.email@fake.com' }
        newUser.save(flush: true)

        dicMap = [1: "Ingredient", 2: "Family", 3: "Product Name", 4: "Trade Name"]

        @SourceURI
        URI sourceUri
        Path scriptLocation = Paths.get(sourceUri)
        String directory = scriptLocation.toString().replace("ProductAssignmentControllerSpec.groovy", "import_assignment_file/upload/import-assignment.xlsx")
        fileToRead = new File(directory)

        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }

        service.transactionManager = getTransactionManager()
        service.sessionFactory_pva = sessionFactory

        UserService mockUserService = Mock(UserService)
        mockUserService.getUser() >> { user }
        service.userService = mockUserService

        CacheService mockCacheService = Mock(CacheService)
        mockCacheService.getUserByUserId(_) >> {
            return user
        }
        mockCacheService.getGroupByGroupId(_) >> {
            return wfGroup
        }
        service.cacheService = mockCacheService
    }

    void "test getUsersAndGroups"() {
        when:
        Map result = service.getUsersAndGroups(['User_1114', 'UserGroup_1'])

        then:
        result.users == [1114]
        result.groups == [1]
    }

    void "test getProductHierarchy"() {
        when:
        String result = service.getProductHierarchy(0, dicMap)

        then:
        result == "Product Group"
    }

    void "test generateAssignedUserOrGroupList"() {
        when:
        List<Map> result = service.generateAssignedUserOrGroupList([1114], [11])

        then:
        result == [["name": "Fake Name", "id": "User_1114"], ["name": "Default", "id": "UserGroup_11"]]
    }

    void "test getWorkflowGroupSorted when order is ascending"(){
        when:
        List result = service.getWorkflowGroupSorted([wfGroup,newWfGroup],"asc")

        then:
        result == [wfGroup.id,newWfGroup.id]
    }


    void "test getWorkflowGroupSorted when order is descending"(){
        when:
        List result = service.getWorkflowGroupSorted([wfGroup,newWfGroup],"desc")

        then:
        result == [newWfGroup.id,wfGroup.id]
    }

    void "test getGroupSorted when order is ascending"(){
        when:
        List result = service.getGroupSorted([userGroup,newUserGroup],"asc")

        then:
        result == [newUserGroup.id,userGroup.id]
    }

    void "test getGroupSorted when order is descending"(){
        when:
        List result = service.getGroupSorted([userGroup,newUserGroup],"desc")

        then:
        result == [userGroup.id,newUserGroup.id]
    }

    void "test getUsernameSorted when order is ascending"(){
        when:
        List result = service.getUsernameSorted([user,newUser],"asc")

        then:
        result == [newUser.id,user.id]
    }

    void "test getUsernameSorted when order is descending"(){
        when:
        List result = service.getUsernameSorted([user,newUser],"desc")

        then:
        result == [user.id,newUser.id]
    }

    void "test getFullNameSortedList when order is ascending"(){
        when:
        List result = service.getFullNameSortedList([user,newUser],"asc")

        then:
        result == [newUser.id,user.id]
    }

    void "test getFullNameSortedList when order is descending"(){
        when:
        List result = service.getFullNameSortedList([user,newUser],"desc")

        then:
        result == [user.id,newUser.id]
    }

    void "test getUsersAndGroupsId in case assignment name is group"(){
        when:
        Map result = service.getUsersAndGroupsId([userGroup,newUserGroup,wfGroup],[user,newUser],"Default",false,false)

        then:
        result.matchedGroupIds == "${wfGroup.id}"
    }

    void "test getUsersAndGroupsId in case assignment name is username"(){
        when:
        Map result = service.getUsersAndGroupsId([userGroup,newUserGroup,wfGroup],[user,newUser],"test_user",true,false)

        then:
        result.matchedUserIds == "${user.id}"
    }

    void "test getUsersAndGroupsId in case assignment name is fullName"(){
        when:
        Map result = service.getUsersAndGroupsId([userGroup,newUserGroup,wfGroup],[user,newUser],"New Fake Name",false,false)

        then:
        result.matchedUserIds == "${newUser.id}"
    }
}
