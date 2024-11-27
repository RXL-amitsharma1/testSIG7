package com.rxlogix.user

import com.rxlogix.Constants
import com.rxlogix.SeedDataService
import com.rxlogix.UserDashboardCounts
import com.rxlogix.UserGroupService
import com.rxlogix.UserService
import com.rxlogix.cache.CacheService
import com.rxlogix.commandObjects.LdapCommand
import com.rxlogix.config.Configuration
import com.rxlogix.config.Disposition
import com.rxlogix.config.Priority
import com.rxlogix.signal.UserGroupFieldMapping
import com.rxlogix.enums.GroupType
import com.rxlogix.ldap.LdapService
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.user.UserGroupRole
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.testing.services.ServiceUnitTest
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([UserService])
class UserServiceSpec extends Specification implements ServiceUnitTest<UserService>  {
    @Shared
    SingleCaseAlert singleCaseAlert1
    @Shared
    SingleCaseAlert singleCaseAlert2
    @Shared
    Configuration configuration
    @Shared
    Configuration configuration2
    @Shared
    User user1
    @Shared
    User user2
    @Shared
    User user3
    @Shared
    User systemUser
    @Shared
    Group wfGroup1
    @Shared
    Group wfGroup
    Group wfGroup2

    def setup() {
        Disposition disposition1 = new Disposition(value: "ValidatedSignal2", displayName: "ValidatedSignal2", closed: false,
                validatedConfirmed: false, abbreviation: "C")
        disposition1.save(failOnError: true)
        Group wfGroup = new Group(name: "Default1", groupType: GroupType.WORKFLOW_GROUP,
                createdBy: 'createdBy',
                modifiedBy: 'modifiedBy',
                defaultQualiDisposition: disposition1,
                defaultQuantDisposition: disposition1,
                defaultAdhocDisposition: disposition1,
                defaultEvdasDisposition: disposition1,
                defaultLitDisposition: disposition1,
                defaultSignalDisposition: disposition1)
        wfGroup.save(flush: true, failOnError: true)
        singleCaseAlert1 =new SingleCaseAlert(assignedTo: user2)
        singleCaseAlert1.save(validate:false)
        singleCaseAlert2 =new SingleCaseAlert(assignedToGroup: wfGroup)
        singleCaseAlert2.save(validate:false)
        Group userGroup = new Group(name: "Default3", groupType: GroupType.USER_GROUP, createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        userGroup.save(validate: false)
        user1=createUser("user1", wfGroup)
        user2=createUser("user2", wfGroup)
        user3 =createUser("user3", wfGroup)
        UserGroupMapping userGroupMapping = new UserGroupMapping(user: user1, group: wfGroup)
        userGroupMapping.save(flush: true, failOnError: true)

        UserGroupMapping userGroupMapping1 = new UserGroupMapping(user: user2, group: wfGroup)
        userGroupMapping1.save(flush: true, failOnError: true)
        configuration=new Configuration(assignedTo:user2 ,assignedToGroup: wfGroup,shareWithUser: [user1,user2],shareWithGroup: [wfGroup],
        type: "SERIOUS")
        configuration.save(validate:false)
        configuration2=new Configuration(assignedToGroup: wfGroup)
        configuration2.save(validate:false)
        SpringSecurityService springSecurityService=Mock(SpringSecurityService)
        springSecurityService.loggedIn>>{
            return user1
        }
        springSecurityService.principal>>{
            return user1
        }
        service.springSecurityService=springSecurityService
    }

    def cleanup() {
    }

    private User createUser(String username, Group wfGroup, String authority=null) {
        User.metaClass.encodePassword = { "password" }
        def preference = new Preference(locale: new Locale("en"),createdBy: "createdBy",modifiedBy: "modifiedBy")
        preference.save(validate:false)
        User user = new User(username: username, password: 'password', fullName: username, preference: preference, createdBy: 'createdBy', modifiedBy: 'modifiedBy',
        email: "${username}@gmail.com")
        user.addToGroups(wfGroup)
        UserGroupMapping userGroupMapping = new UserGroupMapping(user: user, group: wfGroup)
        userGroupMapping.save(flush: true, failOnError: true)
        user.save(failOnError: true)
        if(authority) {
            Role role = new Role(authority: authority, createdBy: 'createdBy', modifiedBy: 'modifiedBy').save(flush: true)
            UserRole.create(user, role, true)
        }
        return user
    }
    //TODO required to update this test case file to run properly.
    void "test getLdapDetails"() {
        setup:
        def filter = "(&(|(uid=*ank*)(cn=*ank*)(mail=*ank*)))"
        service.ldapTemplate = [search: { a, b, c, d, e -> [["Ankit1.Kumar":"Ankit1.Kumar - Ankit1.Kumar - ankit.kumar@rxlogix.com"]]
        }]
        when:
        def resultList = service.searchLdapToAddUser(filter)
        then:
        resultList[0] == ["Ankit1.Kumar":"Ankit1.Kumar - Ankit1.Kumar - ankit.kumar@rxlogix.com"]
    }
    void "test getLdapDetails when there is no user"() {
        setup:
        def filter = "(&(|(uid=*ank*)(cn=*ank*)(mail=*ank*)))"
        service.ldapTemplate = [search: { a, b, c, d, e -> []
        }]
        when:
        def resultList = service.searchLdapToAddUser(filter)
        then:
        resultList.size() == 0
    }
    void "test getShareWithUsersForCurrentUser when no share with role"() {
        given:
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return false
        }
        service.metaClass.getUser = { -> user1 }
        when:
        List<User> users = service.getShareWithUsersForCurrentUser()
        then:
        users.size() == 1
        users[0].username == 'user1'
    }
    void "test getShareWithUsersForCurrentUser when share with group role"() {
        setup:
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }
        User userWithShareGroupRole = createUser("userWithShareGroupRole", wfGroup2, "ROLE_SHARE_GROUP")
        Group grp1 = new Group(name: 'group1', groupType: GroupType.USER_GROUP, createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        grp1.addToMembers(userWithShareGroupRole)
        grp1.addToMembers(user2)
        grp1.save(flush: true)
        UserGroupMapping userGroupMapping = new UserGroupMapping(user: user, group: wfGroup)
        userGroupMapping.save(flush: true, failOnError: true)
        service.metaClass.getUser = { -> userWithShareGroupRole }
        when:
        List<User> users = service.getShareWithUsersForCurrentUser()
        then:
        users.size() == 1
    }
    void "test getShareWithUsersForCurrentUser when share with all role"() {
        setup:
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }
        User userWithShareAllRole = createUser("userWithShareAllRole", wfGroup1, "ROLE_SHARE_ALL")
        Group grp1 = new Group(name: 'group1', groupType: GroupType.USER_GROUP, createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        grp1.addToMembers(userWithShareAllRole)
        grp1.addToMembers(user2)
        grp1.save(flush: true)
        Group grp2 = new Group(name: 'group2', groupType: GroupType.USER_GROUP, createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        grp2.addToMembers(user3)
        grp2.save(flush: true)
        UserGroupMapping userGroupMapping = new UserGroupMapping(user: user, group: wfGroup)
        userGroupMapping.save(flush: true, failOnError: true)
        service.metaClass.getUser = { -> userWithShareAllRole }
        when:
        List<User> users = service.getShareWithUsersForCurrentUser()
        then:
        users.size() == 4
    }
    void "test getShareWithGroupsForCurrentUser with no share with role"() {
        setup:
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return false
        }
        service.metaClass.getUser = { -> user1 }
        Group grp1 = new Group(name: 'group1', groupType: GroupType.USER_GROUP, createdBy: 'createdBy', modifiedBy: 'modifiedBy')

        grp1.addToMembers(user1)
        grp1.addToMembers(user2)
        grp1.save(flush: true)
        when:
        List<Group> groups = service.getShareWithGroupsForCurrentUser()
        then:
        groups.size() == 0
    }
    void "test getShareWithGroupsForCurrentUser with share with group role"() {
        setup:
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }
        User userWithShareGroupRole = createUser("user1", wfGroup2, "ROLE_SHARE_GROUP")
        service.metaClass.getUser = { -> user1 }
        Group grp1 = new Group(name: 'group1', groupType: GroupType.USER_GROUP, createdBy: 'createdBy', modifiedBy: 'modifiedBy')

        grp1.addToMembers(userWithShareGroupRole)
        grp1.addToMembers(user2)
        grp1.save(flush: true)
        when:
        List<Group> groups = service.getShareWithGroupsForCurrentUser()
        then:
        groups.size() == 2
        groups[0].name == 'Default3'
    }
    void "test getUser action"(){
        when:
        User result=service.getUser()
        then:
        result==user1
    }
    @Unroll
    void "test getUserByUsername"(){
        expect:
        service.getUserByUsername(a)?.id==result
        where:
        a         |    result
        "user1"   |    1
        "new user"|    null
    }
    void "test setOwnershipAndModifier "(){
        setup:
        User user=createUser("user",wfGroup1)
        when:
        User result=service.setOwnershipAndModifier(user)
        then:
        result.modifiedBy=="user1"
    }
    void "test getAllEmails when there is configuration"(){
        when:
        user1.email="user1@gmail.com"
        user2.email="user2@gmail.com"
        user3.email="user3@gmail.com"
        Configuration configuration=new Configuration(owner:user2)
        configuration.save(validate:false)
        def result=service.getAllEmails(configuration)
        then:
        result==["user1@gmail.com","user3@gmail.com"]
    }
    void "test getAllEmails when there is no configuration"(){
        when:
        user1.email="user1@gmail.com"
        user2.email="user2@gmail.com"
        user3.email="user3@gmail.com"
        Configuration configuration=new Configuration(owner:user2)
        configuration.save(validate:false)
        def result=service.getAllEmails(null)
        then:
        result==["user2@gmail.com","user3@gmail.com"]
    }
    void "test getActiveUsers "(){
        when:
        List<User> result=service.getActiveUsers()
        then:
        result.size()==3
        result[0]==user1
        result[1]==user2
        result[2]==user3
    }
    void "test createUser"(){
        setup:
        Preference preference = new Preference(locale: new Locale("en"),createdBy: "createdBy",modifiedBy: "modifiedBy")
        preference.save(validate:false)
        LdapCommand ldapCommand=new LdapCommand(userName: "user",fullName: "user",email: "user@gaml.com")
        LdapService ldapService=Mock(LdapService)
        ldapService.getLdapEntry(_)>>{
            return [ldapCommand]
        }
        service.ldapService=ldapService
        Role role1=new Role(authority: "USER",description: "user role",dateCreated: new Date(),lastUpdated: new Date(),createdBy: "createdBy",modifiedBy:
                "modifiedBy")
        role1.save(flush:true,failOnError:true)
        when:
        def result=service.createUser("user",preference,["USER"],"createdBy",wfGroup1)
        then:
        "user"==User.findByUsername("user").username
    }
    @Unroll
    void "test getDevUserEmail"(){
        expect:
        service.getDevUserEmail(a)==result
        where:
        a          |        result
        "dev"      |   "signaldev@rxlogix.com"
        "new user" |    null
    }
    @Unroll
    void "test getUserIdFromEmail"(){
        expect:
        service.getUserIdFromEmail(a)==result
        where:
        a                   |        result
        "user1@gmail.com"   |        1
        "new@gmail.com"     |      null
    }
    void "test getActiveUsersList"(){
        expect:
        service.getActiveUsersList(a).size()==result[0]
        service.getActiveUsersList(a)[0]?.id==result[1]
        where:
        a        |         result
        "user"   |          [3,1]
        "new"    |          [0,null]
    }
    void "test getAllowedUsersForCurrentUser when search is not null"(){
        when:
        List result= service.getAllowedUsersForCurrentUser("user2")
        then:
        result.size()==1
        result[0]==User.get(2)
    }
    void "test getAllowedUsersForCurrentUser when search is null"(){
        when:
        List result= service.getAllowedUsersForCurrentUser()
        then:
        result.size()==3
        result[0]==User.get(1)
        result[1]==User.get(2)
        result[2]==User.get(3)
    }
    void "test getAllowedGroupsForCurrentUser when search is not null"(){
        when:
        List result=service.getAllowedGroupsForCurrentUser("Default")
        then:
        result.size()==1
        result[0]==Group.get(3)
    }
    void "test getAllowedGroupsForCurrentUser when serach is null"(){
        when:
        List result=service.getAllowedGroupsForCurrentUser()
        then:
        result.size()==1
        result[0]==Group.get(3)
    }
    void "test getActiveGroups"(){
        when:
        List result=service.getActiveGroups()
        then:
        result.size()==3
        result[0]==Group.get(1)
    }
    @Unroll
    void "test assignGroupOrAssignTo"(){
        expect:
        User
        service.assignGroupOrAssignTo(a,b).assignedToGroup?.id==result[0]
        service.assignGroupOrAssignTo(a,b).assignedTo?.id==result[1]
        where:
        a               |     b               |     result
        "UserGroup_1"   |   configuration     |    [1,null]
        "User_1"        |   configuration     |    [null,1]
        "Other_1"       |   configuration     |    [null,null]
        ""              |   configuration     |    [null,null]
    }
    @Unroll
    void "test getRecipientsList"(){
        setup:
        UserGroupService userGroupService=Mock(UserGroupService)
        userGroupService.fetchUserEmailsForGroup(_)>>{
            ["user1@gmail.com","user2@gmail.com","user3@gmail.com"]
        }
        service.userGroupService=userGroupService
        expect:
        service.getRecipientsList(a)==result
        where:
        a                     |     result
        configuration         |    ["user2@gmail.com"]
        new Configuration()   |    ["user1@gmail.com","user2@gmail.com","user3@gmail.com"]
    }
    @Unroll
    void "test getUserListFromAssignToGroup"(){
        setup:
        UserGroupService userGroupService=Mock(UserGroupService)
        userGroupService.fetchUserListForGroup(_)>>{
            ["user1","user2","user3"]
        }
        service.userGroupService=userGroupService
        expect:
        service.getUserListFromAssignToGroup(a)==result
        where:
        a                     |     result
        configuration         |    [user2]
        new Configuration()   |    ["user1","user2","user3"]
    }
    @Unroll
    void "test getIdFromAssignTo"(){
        expect:
        service.getIdFromAssignTo(a)==result
        where:
        a                                            |        result
        configuration                                |          2
        new Configuration(assignedToGroup: wfGroup1) |          1
    }
    @Unroll
    void "test getAssignToValue"(){
        expect:
        service.getAssignToValue(a)==result
        where:
          a                 |       result
        configuration       |     "User_2"
        configuration2      |     "UserGroup_1"
    }
    @Unroll
    void "test getAssignedToName"(){
        expect:
        service.getAssignedToName(a)==result
        where:
        a                 |           result
        configuration     |           "user2"
        configuration2    |           "Default1"
    }
    @Unroll
    void "test getAssignedToNameFromCache"(){
        setup:
        CacheService cacheService=Mock(CacheService)
        cacheService.getUserByUserId(_)>>{
            return user1
        }
        cacheService.getGroupByGroupId(1)>>{
            return wfGroup1
        }
        service.cacheService=cacheService
        expect:
        service.getAssignedToNameFromCache(a,b)==result
        where:
        a     |    b     |       result
        1     |    1     |       "user1"
        null  |    1     |       "Default1"
    }
    void "test getAssignedToNameAction"(){
        setup:
        CacheService cacheService=Mock(CacheService)
        cacheService.getUserByUserId(_)>>{
            return user1
        }
        cacheService.getGroupByGroupId(_)>>{
            return wfGroup1
        }
        service.cacheService=cacheService
        expect:
        service.getAssignedToNameAction(a)==result
        where:
        a                 |           result
        singleCaseAlert1  |           "user1"
        singleCaseAlert2  |            "Default1"
    }
    void "test generateEmailDataForAssignedToChange when there are users"(){
        when:
        def result=service.generateEmailDataForAssignedToChange("new message",[user3],"old message",[user1,user2] )
        then:
        result.size()==3
        result[0].user==user3
    }
    void "test generateEmailDataForAssignedToChange when there are no users"(){
        when:
        def result=service.generateEmailDataForAssignedToChange("new message",[],"old message",[] )
        then:
        result.size()==0
    }
    void "test getCurrentUserId"(){
        when:
        Integer result=service.getCurrentUserId()
        then:
        result==user1.id
    }
    void "test getCurrentUserName"(){
        when:
        String result=service.getCurrentUserName()
        then:
        result==user1.username
    }
    void "test getCurrentUserPreference"(){
        setup:
        CacheService cacheService=Mock(CacheService)
        cacheService.getPreferenceByUserId(1)>>{
            return user1.preference
        }
        service.cacheService=cacheService
        when:
        Preference result=service.getCurrentUserPreference()
        then:
        result==Preference.get(1)
    }
    void "test getUserFromCacheByUsername"(){
        setup:
        CacheService cacheService=Mock(CacheService)
        cacheService.getUserByUserName(user1.username)>>{
            return user1
        }
        service.cacheService=cacheService
        when:
        User result=service.getUserFromCacheByUsername(user1.username)
        then:
        result==user1
    }
    void "test bindSharedWithConfiguration when isUpdate is false"(){
        when:
        def result=service.bindSharedWithConfiguration(configuration,["UserGroup_3","User_1"])
        then:
        configuration.shareWithGroups==[wfGroup1,Group.get(3)] as Set
        configuration.shareWithUsers==[user1,user2] as Set
    }
    void "test bindSharedWithConfiguration when isUpdate is true"(){
        when:
        def result=service.bindSharedWithConfiguration(configuration,["UserGroup_3","User_2"],true)
        then:
        configuration.shareWithGroups==[Group.get(3)] as Set
        configuration.shareWithUsers==[user2] as Set
    }
    void "test hasAccessShareWith"(){
        when:
        boolean  result=service.hasAccessShareWith()
        then:
        result==true
    }
    void "test getUserConfigurations"(){
        given:
        service.metaClass.getUserConfigurations = { User user, List<Long> groupIds , Boolean isShare ->

        }
        expect:
        service.getUserConfigurations(a,b)==result
        where:
        a                                           | b       | result
        Constants.AlertConfigType.SINGLE_CASE_ALERT | "user1" | []
    }
    void "test setSystemUser when there is system user"(){
        setup:
        systemUser =createUser("System", wfGroup1)
        when:
        User result=service.setSystemUser(user1)
        then:
        result==user1
        result.modifiedBy=="System"
    }
    void "test setSystemUser when there is no system user"(){
        setup:
        SeedDataService seedDataService=Mock(SeedDataService)
        service.seedDataService=seedDataService
        systemUser =createUser("System", wfGroup1)
        when:
        User result=service.setSystemUser(user1)
        then:
        result==user1
        result.modifiedBy=="System"
    }
    void "test getBlindingFieldsForGroup"() {
        setup:
        // Create and save mock UserGroupFieldMapping instances
        def group1 = new UserGroupFieldMapping(
                groupId: 1L,
                blindedFields: "field1,field2",
                redactedFields: "field3",
                availableFields: "field4,field5"
        ).save(flush: true, failOnError: true)

        def group2 = new UserGroupFieldMapping(
                groupId: 2L,
                blindedFields: "field2,field6",
                redactedFields: "field3,field7",
                availableFields: "field8"
        ).save(flush: true, failOnError: true)

        def group3 = new UserGroupFieldMapping(
                groupId: 3L,
                blindedFields: "field1",
                redactedFields: "field3",
                availableFields: ""
        ).save(flush: true, failOnError: true)

        // Mocking the addFieldsToSet method
        service.metaClass.addFieldsToSet = { String fields, Set<String> fieldSet ->
            if (fields) {
                fields.split(",").each { fieldSet.add(it.trim()) }
            }
        }

        // Mocking the processSet method
        service.metaClass.processSet = { Set<String> fieldSet ->
            fieldSet.toList()
        }

        // Mocking the findByGroupId method
        UserGroupFieldMapping.metaClass.static.findByGroupId = { Long groupId ->
            switch (groupId) {
                case 1L:
                    return group1
                case 2L:
                    return group2
                case 3L:
                    return group3
                default:
                    return null
            }
        }

        when:
        Map result = service.getBlindingFieldsForGroup([1L, 2L, 3L])

        then:
        result.blindedFields.sort() == ["field1", "field2", "field6"].sort()
        result.redactedFields.sort() == ["field3", "field7"].sort()
        result.availableFields.sort() == ["field4", "field5", "field8"].sort()
        result.isAllFieldsRedacted == false
    }
    void "test getBlindingFieldsForGroup with empty groupIds"() {
        setup:
        // No setup needed as groupIds is empty

        when:
        Map result = service.getBlindingFieldsForGroup([])

        then:
        result.blindedFields == []
        result.redactedFields == []
        result.availableFields == []
        result.isAllFieldsRedacted == true
    }

    void "test getBlindingFieldsForUser with various field statuses"() {
        setup:
        // Mocked data for groupBlindingFields and uniqueFieldToRpt
        Map groupBlindingFields = [
                availableFields: ["field1", "field2", "field3", "field4", "field5"],
                redactedFields : ["field3", "field6"],
                blindedFields  : ["field1", "field4"]
        ]

        Map uniqueFieldToRpt = [
                "field1": "uniqueField1",
                "field2": "uniqueField2",
                "field3": "uniqueField3",
                "field4": "uniqueField4",
                "field5": "uniqueField5"
        ]

        // Mock the cacheService.getCacheUniqueFieldId method
        service.cacheService = Mock(CacheService)
        service.cacheService.getCacheUniqueFieldId() >> uniqueFieldToRpt

        when:
        Map result = service.getBlindingFieldsForUser(groupBlindingFields, true)

        then:
        result == [
                "uniqueField1": Constants.BlindingStatus.BLINDED,
                "uniqueField2": Constants.BlindingStatus.AVAILABLE,
                "uniqueField3": Constants.BlindingStatus.REDACTED,
                "uniqueField4": Constants.BlindingStatus.BLINDED,
                "uniqueField5": Constants.BlindingStatus.AVAILABLE
        ]
    }

    void "test getBlindingFieldsForUser without unique field IDs"() {
        setup:
        // Mocked data for groupBlindingFields
        Map groupBlindingFields = [
                availableFields: ["field1", "field2", "field3", "field4", "field5"],
                redactedFields : ["field3", "field6"],
                blindedFields  : ["field1", "field4"]
        ]

        def cacheServiceMock = Mock(CacheService)
        cacheServiceMock.getCacheUniqueFieldId() >> [:] // Return an empty map for this test
        service.cacheService = cacheServiceMock
        when:
        Map result = service.getBlindingFieldsForUser(groupBlindingFields, false)

        then:
        result == [
                "field1": Constants.BlindingStatus.BLINDED,
                "field2": Constants.BlindingStatus.AVAILABLE,
                "field3": Constants.BlindingStatus.REDACTED,
                "field4": Constants.BlindingStatus.BLINDED,
                "field5": Constants.BlindingStatus.AVAILABLE
        ]
    }

    void "test getBlindingFieldsForUser with empty available fields"() {
        setup:
        // Mocked data for groupBlindingFields
        Map groupBlindingFields = [
                availableFields: [],
                redactedFields : ["field3", "field6"],
                blindedFields  : ["field1", "field4"]
        ]
        // Mock the cacheService and its getCacheUniqueFieldId method
        def cacheServiceMock = Mock(CacheService)
        cacheServiceMock.getCacheUniqueFieldId() >> [:]
        service.cacheService = cacheServiceMock

        when:
        Map result = service.getBlindingFieldsForUser(groupBlindingFields, false)

        then:
        result == [:]
    }

    void "test getBlindedDataForSQL with various blinding statuses"() {
        setup:
        Map blindingFieldsForUser = [
                "field1": Constants.BlindingStatus.BLINDED,
                "field2": Constants.BlindingStatus.REDACTED,
                "field3": Constants.BlindingStatus.AVAILABLE,
                "field4": Constants.BlindingStatus.BLINDED,
                "field5": Constants.BlindingStatus.REDACTED
        ]

        when:
        Map result = service.getBlindedDataForSQL(blindingFieldsForUser)

        then:
        result.blindedData == "'field1', 'field4'"
        result.redactedData == "'field2', 'field5'"
        result.availableData == "'field3'"
    }
    void "test getBlindedDataForSQL with empty map"() {
        setup:
        Map blindingFieldsForUser = [:]

        when:
        Map result = service.getBlindedDataForSQL(blindingFieldsForUser)

        then:
        result.blindedData == "''"
        result.redactedData == "''"
        result.availableData == "''"
    }
}
