package unit.com.rxlogix

import com.rxlogix.CRUDService
import com.rxlogix.UserCommand
import com.rxlogix.UserController
import com.rxlogix.UserGroupService
import com.rxlogix.UserRoleService
import com.rxlogix.UserService
import com.rxlogix.cache.CacheService
import com.rxlogix.commandObjects.LdapCommand
import com.rxlogix.config.Disposition
import com.rxlogix.config.DispositionRule
import com.rxlogix.config.SafetyGroup
import com.rxlogix.enums.GroupType
import com.rxlogix.ldap.LdapService
import com.rxlogix.user.Group
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserDepartment
import com.rxlogix.user.UserGroupMapping
import com.rxlogix.user.UserRole
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class UserControllerSpec extends Specification implements ControllerUnitTest<UserController> {

    @Shared User user1
    @Shared User user2
    @Shared Group wfGroup1
    @Shared Group wfGroup2
    @Shared SafetyGroup safetyGroup1
    @Shared SafetyGroup safetyGroup2
    @Shared UserDepartment userDepartment
    @Shared Role role1
    @Shared Role role2

    def setup() {

        Disposition disposition1=new Disposition(abbreviation: "C",value: "Requires Review", displayName: "Requires Review", validatedConfirmed: false)
        disposition1.save(flush:true,failOnError:true)
        Disposition disposition2=new Disposition(abbreviation: "C",value: "New Potential Signal", displayName: "New Potential Signal", validatedConfirmed: false)
        disposition2.save(flush:true,failOnError:true)

        wfGroup1 = new Group(name: "Default1", groupType: GroupType.WORKFLOW_GROUP,
                createdBy: 'createdBy', modifiedBy: 'modifiedBy',
                defaultQualiDisposition: disposition1,
                defaultQuantDisposition: disposition1,
                defaultAdhocDisposition: disposition1,
                defaultEvdasDisposition: disposition1,
                defaultLitDisposition: disposition1,
                defaultSignalDisposition: disposition2)
        wfGroup1.save(flush: true, failOnError: true)
        wfGroup2 = new Group(name: "Default2", groupType: GroupType.WORKFLOW_GROUP,
                createdBy: 'createdBy', modifiedBy: 'modifiedBy',
                defaultQualiDisposition: disposition1,
                defaultQuantDisposition: disposition1,
                defaultAdhocDisposition: disposition1,
                defaultEvdasDisposition: disposition1,
                defaultLitDisposition: disposition1,
                defaultSignalDisposition: disposition2)
        wfGroup2.save(flush: true, failOnError: true)

        safetyGroup1=new SafetyGroup(name: "testSafetyGroup1",allowedProd: "testProduct1",dateCreated: new Date(),
                lastUpdated: new Date(),createdBy: "createdBy",modifiedBy: "modifiedBy")
        safetyGroup1.save(flush:true,failOnError:true)
        safetyGroup2=new SafetyGroup(name: "testSafetyGroup2",allowedProd: "testProduct2",dateCreated: new Date(),
                lastUpdated: new Date(),createdBy: "createdBy",modifiedBy: "modifiedBy")
        safetyGroup2.save(flush:true,failOnError:true)
    }
    private User createUser(String username, Group wfGroup, String authority=null) {
        User.metaClass.encodePassword = { "password" }
        def preference = new Preference(locale: new Locale("en"))
        User user = new User(username: username, password: 'password', fullName: username, preference: preference, createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.addToGroups(wfGroup)
        user.save(flush:true,failOnError: true)
        if(authority) {
            Role role = new Role(authority: authority, createdBy: 'createdBy', modifiedBy: 'modifiedBy').save(flush: true)
            UserRole.create(user, role, true)
        }
        return user
    }
    void "test ajaxLdapSearch action when there are users"() {
        setup:
        user1 = createUser("user1", wfGroup1)
        user2 = createUser("user2", wfGroup1)
        controller.params.term = "ank"
        controller.userService = [searchLdapToAddUser: { a -> [[["ankur.gupta":"ankur.gupta - ankur.gupta - ankur.gupta@rxlogix.com"]]]
        }]
        when:
        controller.ajaxLdapSearch()
        then:
        response.status == 200
        response.getJson()[0]["id"] == "ankur.gupta"
        response.getJson()[0]["text"] == "ankur.gupta - ankur.gupta - ankur.gupta@rxlogix.com"
    }
    void "test ajaxLdapSearch action when there is no user"() {
        setup:
        user1 = createUser("user1", wfGroup1)
        user2 = createUser("user2", wfGroup1)
        controller.params.term = "ank"
        controller.userService = [searchLdapToAddUser: { a -> []
        }]
        when:
        controller.ajaxLdapSearch()
        then:
        response.status == 200
        response.getJson().size() == 0
    }
    void "test index action when there are users"(){
        setup:
        user1 = createUser("user1", wfGroup1)
        user2 = createUser("user2", wfGroup1)
        when:
        controller.index()
        then:
        response.status==200
        view=="/user/index"
        model.userInstanceTotal==2
        model.userInstanceList.get(0)==user1
        model.userInstanceList[1]==user2
    }
    void "test index action when there are no users"(){
        when:
        controller.index()
        then:
        response.status==200
        view=="/user/index"
        model.userInstanceTotal==0
    }
    @Unroll
    void "test show action for both when user exist and when user does not exist"(){
        when:
        controller.show(a)
        then:
        response.status==result[0]
        model.userInstance==result[1]
        if(result[0]==200) {
            view == result[2]
        }else{
            response.redirectedUrl=="/user/index"
        }
        where:
        a               ||   result
        user1           ||   [200,user1,"/user/show"]
        null            ||   [302,null]
    }
    void "test create action"(){
        when:
        controller.create()
        then:
        response.status==200
        view=="/user/create"
    }
    void "test save action when content type is form on success"(){
        setup:
        userDepartment=new UserDepartment(departmentName: "IT")
        userDepartment.save(flush:true,failOnError:true)
        role1=new Role(authority: "USER",description: "user role",dateCreated: new Date(),lastUpdated: new Date(),createdBy: "createdBy",modifiedBy:
                "modifiedBy")
        role1.save(flush:true,failOnError:true)
        role2=new Role(authority: "ADMIN",description: "admin role",dateCreated: new Date(),lastUpdated: new Date(),createdBy: "createdBy",modifiedBy:
                "modifiedBy")
        role2.save(flush:true,failOnError:true)
        user1 = createUser("test user", wfGroup1)
        UserCommand command=new UserCommand(username: "testuser",accountExpired: false,accountLocked: false,passwordExpired: false)
        params.groups=[1L]
        params.safetyGroups=[1L]
        params.ADMIN="on"
        LdapCommand ldapCommand=new LdapCommand(userName: "testuser",fullName: "test user",email: "testuser@gmail.com")
        LdapService ldapService=Mock(LdapService)
        ldapService.getLdapEntry(_)>>{
            return [ldapCommand]
        }
        UserService userService=Mock(UserService)
        userService.getUser()>>{
            return user1
        }
        SpringSecurityService springSecurityService=Mock(SpringSecurityService)
        springSecurityService.loggedIn>>{
            return user1
        }
        springSecurityService.principal>>{
            return user1
        }
        CacheService cacheService=Mock(CacheService)
        cacheService.prepareGroupAndSafetyGroupProductsCacheForUser(_)>>{
            return user1
        }
        controller.cacheService=cacheService
        controller.CRUDService.userService.springSecurityService=springSecurityService
        controller.userService=userService
        controller.ldapService=ldapService
        when:
        request.contentType=FORM_CONTENT_TYPE
        request.method="POST"
        controller.save(command)
        then:
        response.status==302
        flash.message=='default.created.message'
    }
    void "test save action when content type is all on success"(){
        setup:
        userDepartment=new UserDepartment(departmentName: "IT")
        userDepartment.save(flush:true,failOnError:true)
        role1=new Role(authority: "USER",description: "user role",dateCreated: new Date(),lastUpdated: new Date(),createdBy: "createdBy",modifiedBy:
                "modifiedBy")
        role1.save(flush:true,failOnError:true)
        role2=new Role(authority: "ADMIN",description: "admin role",dateCreated: new Date(),lastUpdated: new Date(),createdBy: "createdBy",modifiedBy:
                "modifiedBy")
        role2.save(flush:true,failOnError:true)
        user1 = createUser("test user", wfGroup1)
        UserCommand command=new UserCommand(username: "testuser",accountExpired: false,accountLocked: false,passwordExpired: false)
        params.groups=[1L]
        params.safetyGroups=[1L]
        params.ADMIN="on"
        LdapCommand ldapCommand=new LdapCommand(userName: "testuser",fullName: "test user",email: "testuser@gmail.com")
        LdapService ldapService=Mock(LdapService)
        ldapService.getLdapEntry(_)>>{
            return [ldapCommand]
        }
        UserService userService=Mock(UserService)
        userService.getUser()>>{
            return user1
        }
        SpringSecurityService springSecurityService=Mock(SpringSecurityService)
        springSecurityService.loggedIn>>{
            return user1
        }
        springSecurityService.principal>>{
            return user1
        }
        CacheService cacheService=Mock(CacheService)
        cacheService.prepareGroupAndSafetyGroupProductsCacheForUser(_)>>{
            return user1
        }
        controller.cacheService=cacheService
        controller.CRUDService.userService.springSecurityService=springSecurityService
        controller.userService=userService
        controller.ldapService=ldapService
        when:
        request.contentType=ALL_CONTENT_TYPE
        request.method="POST"
        controller.save(command)
        then:
        response.status==201
        flash.message==null
    }
    void "test save action when exception occurs"(){
        setup:
        userDepartment=new UserDepartment(departmentName: "IT")
        userDepartment.save(flush:true,failOnError:true)
        role1=new Role(authority: "USER",description: "user role",dateCreated: new Date(),lastUpdated: new Date(),createdBy: "createdBy",modifiedBy:
                "modifiedBy")
        role1.save(flush:true,failOnError:true)
        role2=new Role(authority: "ADMIN",description: "admin role",dateCreated: new Date(),lastUpdated: new Date(),createdBy: "createdBy",modifiedBy:
                "modifiedBy")
        role2.save(flush:true,failOnError:true)
        user1 = createUser("test user", wfGroup1)
        UserCommand command=new UserCommand(username: "testuser",accountExpired: false,accountLocked: false,passwordExpired: false)
        params.groups=[1L]
        params.safetyGroups=[1L]
        params.ADMIN="on"
        LdapCommand ldapCommand=new LdapCommand(userName: "testuser",fullName: "test user",email: "testuser@gmail.com")
        LdapService ldapService=Mock(LdapService)
        ldapService.getLdapEntry(_)>>{
            return [ldapCommand]
        }
        UserService userService=Mock(UserService)
        userService.getUser()>>{
            return user1
        }
        CacheService cacheService=Mock(CacheService)
        cacheService.prepareGroupAndSafetyGroupProductsCacheForUser(_)>>{
            return user1
        }
        controller.cacheService=cacheService
        controller.userService=userService
        controller.ldapService=ldapService
        when:
        request.contentType=ALL_CONTENT_TYPE
        request.method="POST"
        controller.save(command)
        then:
        response.status==200
        flash.error==null
        view=="/user/create"
    }
    void "test save action when role not found"(){
        setup:
        userDepartment=new UserDepartment(departmentName: "IT")
        userDepartment.save(flush:true,failOnError:true)
        role1=new Role(authority: "USER",description: "user role",dateCreated: new Date(),lastUpdated: new Date(),createdBy: "createdBy",modifiedBy:
                "modifiedBy")
        role1.save(flush:true,failOnError:true)
        role2=new Role(authority: "ADMIN",description: "admin role",dateCreated: new Date(),lastUpdated: new Date(),createdBy: "createdBy",modifiedBy:
                "modifiedBy")
        role2.save(flush:true,failOnError:true)
        user1 = createUser("test user", wfGroup1)
        UserCommand command=new UserCommand(username: "testuser",accountExpired: false,accountLocked: false,passwordExpired: false)
        params.groups=[1L]
        params.safetyGroups=[1L]
        params.ADMIN="off"
        LdapCommand ldapCommand=new LdapCommand(userName: "testuser",fullName: "test user",email: "testuser@gmail.com")
        LdapService ldapService=Mock(LdapService)
        ldapService.getLdapEntry(_)>>{
            return [ldapCommand]
        }
        UserService userService=Mock(UserService)
        userService.getUser()>>{
            return user1
        }
        SpringSecurityService springSecurityService=Mock(SpringSecurityService)
        springSecurityService.loggedIn>>{
            return user1
        }
        springSecurityService.principal>>{
            return user1
        }
        CacheService cacheService=Mock(CacheService)
        cacheService.prepareGroupAndSafetyGroupProductsCacheForUser(_)>>{
            return user1
        }
        controller.cacheService=cacheService
        controller.CRUDService.userService.springSecurityService=springSecurityService
        controller.userService=userService
        controller.ldapService=ldapService
        when:
        request.contentType=FORM_CONTENT_TYPE
        request.method="POST"
        controller.save(command)
        then:
        response.status==200
        flash.error=='app.role.select'
        view=="/user/create"
    }
    void "test save action when when validation fails during role check"(){
        setup:
        userDepartment=new UserDepartment(departmentName: "IT")
        userDepartment.save(flush:true,failOnError:true)
        role1=new Role(authority: "USER",description: "user role",dateCreated: new Date(),lastUpdated: new Date(),createdBy: "createdBy",modifiedBy:
                "modifiedBy")
        role1.save(flush:true,failOnError:true)
        role2=new Role(authority: "ADMIN",description: "admin role",dateCreated: new Date(),lastUpdated: new Date(),createdBy: "createdBy",modifiedBy:
                "modifiedBy")
        role2.save(flush:true,failOnError:true)
        user1 = createUser("test user", wfGroup1)
        UserCommand command=new UserCommand(username: "",accountExpired: false,accountLocked: false,passwordExpired: false)
        params.groups=[1L]
        params.safetyGroups=[1L]
        params.ADMIN="on"
        LdapCommand ldapCommand=new LdapCommand(userName: "testuser",fullName: "testuser",email: "testuser@gmail.com")
        LdapService ldapService=Mock(LdapService)
        ldapService.getLdapEntry(_)>>{
            return [ldapCommand]
        }
        UserService userService=Mock(UserService)
        userService.getUser()>>{
            return user1
        }
        SpringSecurityService springSecurityService=Mock(SpringSecurityService)
        springSecurityService.loggedIn>>{
            return user1
        }
        springSecurityService.principal>>{
            return user1
        }
        CacheService cacheService=Mock(CacheService)
        cacheService.prepareGroupAndSafetyGroupProductsCacheForUser(_)>>{
            return user1
        }
        controller.cacheService=cacheService
        controller.CRUDService.userService.springSecurityService=springSecurityService
        controller.userService=userService
        controller.ldapService=ldapService
        when:
        request.contentType=FORM_CONTENT_TYPE
        request.method="POST"
        controller.save(command)
        then:
        response.status==200
        flash.eror==null
        view=="/user/create"
    }
    @Unroll
    void "test edit action for both when user exist and when user does not exist"() {
        when:
        controller.edit(a)
        then:
        response.status==result[0]
        model.userInstance==result[1]
        if(result[0]==200) {
            view == result[2]
        }else{
            response.redirectedUrl=="/user/index"
        }
        where:
        a               ||   result
        user1           ||   [200,user1,"/user/edit"]
        null            ||   [302,null]
    }
    void "test action update when user not found"(){
        when:
        user1 = createUser("user1", wfGroup1)
        request.method="PUT"
        controller.update(user1.id)
        flash.message=="default.not.found.message"
        then:
        response.status==200
    }
    void "test action update when content type is form"(){
        setup:
        User user = createUser("test user", wfGroup1)
        role1=new Role(authority: "USER",description: "user role",dateCreated: new Date(),lastUpdated: new Date(),createdBy: "createdBy",modifiedBy:
                "modifiedBy")
        role1.save(flush:true,failOnError:true)
        userDepartment=new UserDepartment(departmentName: "IT")
        userDepartment.save(flush:true,failOnError:true)
        params.groups=[1L]
        params.safetyGroups=[1L,2L]
        params.department="IT"
        params.USER="on"
        CacheService cacheService=Mock(CacheService)
        cacheService.prepareGroupAndSafetyGroupProductsCacheForUser(_)>>{
            return user
        }
        controller.cacheService=cacheService
        when:
        request.method="PUT"
        controller.update(user.id)
        then:
        flash.message=="default.updated.message"
        response.status==302
    }
    void "test action update when content type is all"(){
        setup:
        User user = createUser("test user", wfGroup1)
        role1=new Role(authority: "USER",description: "user role",dateCreated: new Date(),lastUpdated: new Date(),createdBy: "createdBy",modifiedBy:
                "modifiedBy")
        role1.save(flush:true,failOnError:true)
        userDepartment=new UserDepartment(departmentName: "IT")
        userDepartment.save(flush:true,failOnError:true)
        params.groups=[1L]
        params.safetyGroups=[1L,2L]
        params.department="IT"
        params.USER="on"
        CacheService cacheService=Mock(CacheService)
        cacheService.prepareGroupAndSafetyGroupProductsCacheForUser(_)>>{
            return user
        }
        controller.cacheService=cacheService
        when:
        request.contentType="ALL_CONTENT_TYPE"
        request.method="PUT"
        controller.update(user.id)
        then:
        response.status==200
        view==null
    }
    void "test action update when exception occurs"(){
        setup:
        User user = createUser("test user1", wfGroup1)
        User user2 = createUser("test user2", wfGroup1)
        role1=new Role(authority: "USER",description: "user role",dateCreated: new Date(),lastUpdated: new Date(),createdBy: "createdBy",modifiedBy:
                "modifiedBy")
        role1.save(flush:true,failOnError:true)
        userDepartment=new UserDepartment(departmentName: "IT")
        userDepartment.save(flush:true,failOnError:true)
        user.username="test user2"
        params.groups=[1L]
        params.safetyGroups=[1L,2L]
        params.department="IT"
        params.USER="on"
        CacheService cacheService=Mock(CacheService)
        cacheService.prepareGroupAndSafetyGroupProductsCacheForUser(_)>>{
            return user
        }
        controller.cacheService=cacheService
        when:
        request.contentType=ALL_CONTENT_TYPE
        request.method="PUT"
        controller.update(user.id)
        then:
        view=="/user/edit"
        response.status==200
        flash.error==null
    }
    void "test action update when role not found"(){
        setup:
        User user = createUser("test user1", wfGroup1)
        User user2 = createUser("test user2", wfGroup1)
        role1=new Role(authority: "USER",description: "user role",dateCreated: new Date(),lastUpdated: new Date(),createdBy: "createdBy",modifiedBy:
                "modifiedBy")
        role1.save(flush:true,failOnError:true)
        userDepartment=new UserDepartment(departmentName: "IT")
        userDepartment.save(flush:true,failOnError:true)
        params.groups=[1L]
        params.safetyGroups=[1L,2L]
        params.department="IT"
        params.CRUD="on"
        CacheService cacheService=Mock(CacheService)
        cacheService.prepareGroupAndSafetyGroupProductsCacheForUser(_)>>{
            return user
        }
        controller.cacheService=cacheService
        when:
        request.method="PUT"
        controller.update(user.id)
        then:
        view=="/user/edit"
        response.status==200
        flash.error=="app.role.select"
    }
    void "test action bind groups"(){
        setup:
        user1 = createUser("test user", wfGroup1)
        params.groups=[1L]
        params.safetyGroups=[1L,2L]
        when:
        controller.bindGroups(user1 )
        then:
        response.status==200
    }
    void "test bindDepartments action when there are departments"(){
        setup:
        UserDepartment userDepartment1=new UserDepartment(departmentName: "department1")
        userDepartment1.save(flush:true,failOnError:true)
        UserDepartment userDepartment2=new UserDepartment(departmentName: "department2")
        userDepartment2 .save(flush:true,failOnError:true)
        params.department=["department1","department2"]
        when:
        User result=controller.bindDepartments(user1)
        then:
        response.status==200
        result==user1
        result.userDepartments.id==[userDepartment1,userDepartment2].id
    }
    void "test bindDepartments action when there are no departments"(){
        setup:
        params.department=[]
        when:
        User result=controller.bindDepartments(user1)
        then:
        response.status==200
        result==user1
        result.userDepartments.size()==0
    }
    void "test delete action when user exist and content type is form"(){
        given:
        User user = createUser("test", wfGroup1)
        user.save(flush:true,failOnError:true)
        when:
        request.contentType = FORM_CONTENT_TYPE
        request.method="DELETE"
        controller.delete(user)
        then:
        response.status==302
        flash.message=='default.deleted.message'
    }
    void "test delete action when user exist and content type is not form"(){
        given:
        User user = createUser("test", wfGroup1)
        user.save(flush:true,failOnError:true)
        when:
        request.contentType = ALL_CONTENT_TYPE
        request.method="DELETE"
        controller.delete(user)
        then:
        response.status==204
    }
    void "test delete action when user does not exist and content type is form"(){
        when:
        request.contentType = FORM_CONTENT_TYPE
        request.method="DELETE"
        controller.delete(null)
        then:
        flash.message=='default.not.found.message'
        response.status==302
        response.redirectedUrl=="/user/index"
    }
    void "test delete action when user does not exist and content type is all"(){
        when:
        request.contentType = ALL_CONTENT_TYPE
        request.method="DELETE"
        controller.delete(null)
        then:
        response.status==404
    }
    void "test sharedUsers when string matches"(){
        given:
        user1 = createUser("user1", wfGroup1)
        user2 = createUser("user2", wfGroup1)
        params.term="user"

        when:
        controller.sharedUsers()

        then:
        JSON.parse(response.text).size()==2
        JSON.parse(response.text)[0].id==1
        JSON.parse(response.text)[1].id==2
    }
    void "test sharedUsers when string does not matches"(){
        given:
        user1 = createUser("user1", wfGroup1)
        user2 = createUser("user2", wfGroup1)
        params.term="super"

        when:
        controller.sharedUsers()

        then:
        JSON.parse(response.text).size()==0
    }
    void "test notFound action when content type is form"(){
        when:
        request.contentType = FORM_CONTENT_TYPE
        controller.notFound()
        then:
        flash.message== 'default.not.found.message'
        response.status==302
        response.redirectedUrl=="/user/index"
    }
    void "test notFound action content type is all"(){
        when:
        request.contentType = ALL_CONTENT_TYPE
        controller.notFound()
        then:
        response.status==404
    }
    void "test addRoles action for success"(){
        setup:
        user1 = createUser("user1", wfGroup1)
        Role role=new Role(authority: "ADMIN",description: "admin",createdBy: "createdBy",modifiedBy: "modifiedBy")
        role.save(flush:true,failOnError:true)
        params.ADMIN="on"
        when:
        controller.addRoles(user1)
        then:
        response.status==200
        UserRole.get(1)!=null
    }
    void "test addRoles action on fail"(){
        setup:
        user1 = createUser("user1", wfGroup1)
        Role role=new Role(authority: "ADMIN",description: "admin",createdBy: "createdBy",modifiedBy: "modifiedBy")
        role.save(flush:true,failOnError:true)
        params.ADMIN="off"
        when:
        controller.addRoles(user1)
        then:
        response.status==200
        UserRole.get(1)==null
    }
    @Ignore
    void "test sortedRoles action when there are roles"(){
        setup:
        Role role1=new Role(authority: "USER",description: "user",createdBy: "createdBy",modifiedBy: "modifiedBy")
        role1.save(flush:true,failOnError:true)
        Role role2=new Role(authority: "ADMIN",description: "admin",createdBy: "createdBy",modifiedBy: "modifiedBy")
        role2.save(flush:true,failOnError:true)
        ViewHelper viewHelper=Mock(ViewHelper)
        viewHelper.metaClass.getMessage={"lol"}
        when:
        List result=controller.sortedRoles()
        then:
        result.size()==2
        result[0].authority=="ADMIN"
        result[1].authority=="USER"
    }
    void "test sortedRoles action when there are no roles"(){
        when:
        List result=controller.sortedRoles()
        then:
        result.size()==0
    }
    @Ignore
    void "test buildUserModel when there are role"(){
        setup:
        Role role1=new Role(authority: "USER",description: "user",createdBy: "createdBy",modifiedBy: "modifiedBy")
        role1.save(flush:true,failOnError:true)
        Role role2=new Role(authority: "ADMIN",description: "admin",createdBy: "createdBy",modifiedBy: "modifiedBy")
        role2.save(flush:true,failOnError:true)
        user1 = createUser("user1", wfGroup1)
        user1.save(flush:true,failOnError:true)
        ViewHelper viewHelper=Mock(ViewHelper)
        viewHelper.metaClass.getMessage={"lol"}
        when:
        Map result=controller.buildUserModel(user1)
        then:
        response.status==200
        result.userInstance==User.get(1)
        result.roleMap!=null
    }
    void "test buildUserModel when there are no role"(){
        setup:
        user1 = createUser("user1", wfGroup1)
        user1.save(flush:true,failOnError:true)
        when:
        Map result=controller.buildUserModel(user1)
        then:
        response.status==200
        result.userInstance==User.get(1)
        result.roleMap.size()==0
    }
    void "test searchUserGroupList action"(){
        setup:
        user1 = createUser("user1", wfGroup1)
        user1.save(flush:true,failOnError:true)
        user2 = createUser("user2", wfGroup1)
        user2.save(flush:true,failOnError:true)
        UserService userService=Mock(UserService)
        userService.getAllowedUsersForCurrentUser("user")>>{
            return [user1,user2]
        }
        userService.getAllowedGroupsForCurrentUser("user")>>{
            return [wfGroup1]
        }
        controller.userService=userService
        when:
        controller.searchUserGroupList("user",2,10)
        then:
        JSON.parse(response.text).size()==2
        JSON.parse(response.text)[0].children.size()==1
        JSON.parse(response.text)[0].text=="Group"
        JSON.parse(response.text)[1].children.size()==2
        JSON.parse(response.text)[1].text=="user.label"
        response.status==200
    }
    void "test searchUserGroupList action when exception occurs"(){
        when:
        controller.searchUserGroupList("user",2,10)
        then:
        response.status==200
        JSON.parse(response.text).size()==0
    }
    @Ignore
    void "test searchShareWithUserGroupList action"(){
        setup:
        user1 = createUser("user1", wfGroup1)
        user1.save(flush:true,failOnError:true)
        user2 = createUser("user2", wfGroup1)
        user2.save(flush:true,failOnError:true)
        UserService userService=Mock(UserService)
        userService.getShareWithUsersForCurrentUser("user")>>{
            return [user1,user2]
        }
        userService.getShareWithGroupsForCurrentUser("user")>>{
            return [wfGroup1]
        }
        controller.userService=userService
        when:
        controller.searchShareWithUserGroupList("user",2,10)
        then:
        JSON.parse(response.text).size()==2
        JSON.parse(response.text)[0].children.size()==1
        JSON.parse(response.text)[0].text=="Group"
        JSON.parse(response.text)[1].children.size()==2
        JSON.parse(response.text)[1].text=="user.label"
        response.status==200
    }
    void "test searchShareWithUserGroupList action when exception occurs"(){
        when:
        controller.searchUserGroupList("user",2,10)
        then:
        response.status==200
        JSON.parse(response.text).size()==0
    }
    void " test action sharedWithValues UserGroup is given"(){
        given:
        params.ids="UserGroup_1;UserGroup_2"
        when:
        controller.sharedWithValues()
        then:
        JSON.parse(response.text).size()==2
        JSON.parse(response.text)[0].id=="UserGroup_1"
        JSON.parse(response.text)[0].text=="Default1"
        JSON.parse(response.text)[1].id=="UserGroup_2"
        JSON.parse(response.text)[1].text=="Default2"
    }
    void " test action sharedWithValues User is given"(){
        setup:
        user1 = createUser("user1", wfGroup1)
        user1.save(flush:true,failOnError:true)
        user2 = createUser("user2", wfGroup1)
        user2.save(flush:true,failOnError:true)
        params.ids="User_1;User_2"
        when:
        controller.sharedWithValues()
        then:
        JSON.parse(response.text).size()==2
        JSON.parse(response.text)[0].id=="User_1"
        JSON.parse(response.text)[0].text=="user1"
        JSON.parse(response.text)[1].id=="User_2"
        JSON.parse(response.text)[1].text=="user2"
    }
    void "test sharedWithList"(){
        setup:
        user1 = createUser("user1", wfGroup1)
        user1.save(flush:true,failOnError:true)
        user2 = createUser("user2", wfGroup2)
        user2.save(flush:true,failOnError:true)
        UserService userService=Mock(UserService)
        userService.getShareWithUsersForCurrentUser("user")>>{
            return [user1,user2]
        }
        userService.getShareWithGroupsForCurrentUser("user")>>{
            return [wfGroup1,wfGroup2]
        }
        controller.userService=userService
        when:
        controller.sharedWithList("user",2,10)
        then:
        JSON.parse(response.text).total_count==4
        JSON.parse(response.text).items[0].children.size()==2
        JSON.parse(response.text).items[0].text=="Group"
        JSON.parse(response.text).items[1].children.size()==2
        JSON.parse(response.text).items[1].text=="user.label"
    }
    void "test splitResult action"(){
        setup:
        user1 = createUser("user1", wfGroup1)
        user1.save(flush:true,failOnError:true)
        user2 = createUser("user2", wfGroup1)
        user2.save(flush:true,failOnError:true)
        ViewHelper viewHelper=Mock(ViewHelper)
        viewHelper.metaClass.getMessage={"lol"}
        when:
        def result=controller.splitResult([],0,2,[wfGroup1,wfGroup2],[user1,user2])
        then:
        result[0].text=="Group"
        result[0].children.size()==2
        result[1].text=="user.label"
        result[1].children.size()==2
        response.status==200
    }
}
