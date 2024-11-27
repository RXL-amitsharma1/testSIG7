package com.rxlogix

import com.rxlogix.audit.AuditTrail
import com.rxlogix.commandObjects.LdapCommand
import com.rxlogix.config.SafetyGroup
import com.rxlogix.enums.GroupType
import com.rxlogix.enums.UserType
import com.rxlogix.json.JsonOutput
import com.rxlogix.user.*
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.Validateable
import grails.validation.ValidationException
import groovy.sql.Sql
import org.apache.http.HttpStatus
import org.hibernate.criterion.CriteriaSpecification

import java.text.MessageFormat

import static org.springframework.http.HttpStatus.*

@Secured(["isAuthenticated()"])
class UserController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE", get: 'GET']

    def ldapTemplate
    def CRUDService
    def springSecurityService
    def emailService
    def userService
    def userRoleService
    def userGroupService
    def groupService
    def ldapService
    def cacheService
    def alertService
    def dataSource
    def signalAuditLogService

    def keepAlive() {
        render([status: 'ok'] as JSON)
    }


    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def index() {
        params.max = params.max ?: Constants.Search.MAX_SEARCH_RESULTS
        params.offset = params.offset ?: Constants.Search.MIN_OFFSET
        List userList = User.createCriteria().list {
            eq('enabled', true)
        }
        List userInstanceList = User.list(sort: 'fullName'.trim())
        Map userGroupMapping = userService.groupLists(userInstanceList*.id)
        render view: "index", model: [userInstanceList: userInstanceList, userInstanceTotal: User.count(), activeUsersCount: userList.size(), userGroupMapping: userGroupMapping]
    }

    def encodePassword(String passwordToBeEncoded) {
        String encodedPassword = RxCodec.encode(passwordToBeEncoded)
        Map encodedMap = [encodedPassword: "", success: false]
        if (encodedPassword) {
            encodedMap = [encodedPassword: encodedPassword, success: true]
        }
        render encodedMap as JSON
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def show(User userInstance) {
        if (!userInstance) {
            notFound()
            return
        }
        Set<String> roles = UserRole.findAllByUser( userInstance ).collect { it.role.authority } as Set
        render view: "show", model: [userInstance: userInstance, roles:roles]
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def create() {
        def command = new UserCommand()
        command.groups = [Group.findByName('All Users')]
        render view: "create", model: [userInstance: command, authorityList: sortedRoles()]
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def save(UserCommand command) {

        command.groups = params.list('groups')
        command.safetyGroups = params.list('safetyGroups')
        String groupsJson = JsonOutput.toJson(command.groups)
        String safetyGroupsJson = JsonOutput.toJson(command.safetyGroups)
        //Creating user object from user command
        User userInstance = new User(command.properties)

        if (!userInstance.validate(["username", "preference.locale"])) {
            command.errors = userInstance.errors
            render view: 'create', model: [userInstance: command, groupsJson: groupsJson, safetyGroupsJson: safetyGroupsJson]
            return
        }

        bindGroups(userInstance,false)
        bindDepartments(userInstance)
        String uid = grailsApplication.config.grails.plugin.springsecurity.ldap.uid.attribute
        List<LdapCommand> ldapEntry = ldapService.getLdapEntry("$uid=$userInstance.username")
        if(userInstance.type == UserType.LDAP){
            userInstance.fullName = ldapEntry[0]?.getFullName()
            userInstance.email = ldapEntry[0]?.getEmail()
            userInstance.password = "{noop}" //FOR LDAP USERS from SAML2 we are adding {noop} as id is required in passwordEncoder
        } else{
            if (ldapEntry) {
                flash.error = message(code: 'com.rxlogix.user.User.name.unique.per.user') as Object
                render view: 'create', model: [userInstance: command]
                return
            }
            String newPassword = grailsApplication.config.password.defaultUserPassword
            userService.changePassword(userInstance, newPassword)
            emailService.sendPasswordChangeEmail(userInstance.username,newPassword, [userInstance.email])

        }
        try {
            userInstance.preference.timeZone = params.timeZone
            userInstance = (User) CRUDService.save(userInstance)
            userService.saveUserInfoInPvUserWebappTable(userInstance)
            userGroupService.createUserGroupMappingsForUser(userInstance, userInstance.groups.collect { it.id })
            //used all user as default grp when default gropu is not found because form UI all user comes as default always
            Group defaultGroup = Group.findByCreatedByAndIsDefault(Constants.SYSTEM_USER, true) ?: Group.findByName('All Users')
            groupService.saveUsersInUserGroup( defaultGroup?.id, Arrays.asList( userInstance.id ),"USER" )
            addRoles userInstance
            cacheService.prepareGroupAndSafetyGroupProductsCacheForUser(userInstance)
            new UserDashboardCounts(userId: userInstance.id).save(flush: true)
            userService.updateUserGroupCountsInBackground(userInstance, [])
            groupService.updateGroupUsersStringForUser(userInstance)
        } catch (ValidationException ve) {
            command.errors = ve.errors
            render view: 'create', model: [userInstance: command, groupsJson: groupsJson, safetyGroupsJson: safetyGroupsJson]
            return
        }
        request.withFormat {
            form {
                flash.message = message(code: 'default.created.message', args: [message(code: 'user.label'), userInstance.username])
                redirect userInstance
            }
            '*' { respond userInstance, [status: CREATED] }
        }
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def edit(User userInstance) {
        if (!userInstance) {
            notFound()
            return
        }
        render view: 'edit', model: buildUserModel(userInstance)
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def update(Long id) {
        User userInstance = User.get( id )

        Group workflowGroup=userInstance?.workflowGroup
        Long prevWorkflowGroupId = userInstance.workflowGroup?.id
        List<Long> prevGroupIdList = userInstance.groups.findAll {
            it.groupType != GroupType.WORKFLOW_GROUP
        }.id
        boolean isPrevAccountLocked = userInstance.accountLocked
        bindGroups(userInstance,true)
        bindDepartments(userInstance)
        try {
            if (params.accountLocked) {
                userInstance.accountLocked = true
            } else if (!params.accountLocked && isPrevAccountLocked) {
                userInstance.accountLocked = false
                userInstance.badPasswordAttempts = 0
            }
            if (params.enabled) {
                userInstance.enabled = true
            } else {
                userInstance.enabled = false
            }
            if (params.accountExpired) {
                userInstance.accountExpired = true
            } else {
                userInstance.accountExpired = false
            }
            userInstance.lastUpdated = new Date()
            userInstance.preference.timeZone = params.timeZone
            userInstance.preference.locale = params.preference.locale == "ja" ? Locale.JAPANESE : Locale.ENGLISH
            if (springSecurityService.isLoggedIn()) {
                def modifiedUser = userService.getCurrentUserName()
                if(modifiedUser){
                    userInstance.modifiedBy = modifiedUser
                }
            }
            userInstance.lastUpdated = new Date()
            userInstance?.groups?.add( Group.findById( workflowGroup?.id, [null: null] ) )
            userInstance = (User) CRUDService.update(userInstance)
            userService.updateUserInfoInPvUserWebappTable(userInstance)
            List groupIds = userInstance?.groups?.collect { it?.id }
            userGroupService.deleteUserGroupMappingsForUser( userInstance )
            userGroupService.createUserGroupMappingsForUser( userInstance, groupIds )
            userRoleService.changeUserRoles( userInstance, params )
            cacheService.prepareGroupAndSafetyGroupProductsCacheForUser(userInstance)
            if (prevWorkflowGroupId != userInstance?.workflowGroup?.id) {
                userService.updateUserGroupCountsForWorkflowGroupInBackground(userInstance)
            } else {
                userService.updateUserGroupCountsInBackground(userInstance, prevGroupIdList)
            }
            groupService.updateGroupUsersStringForUser(userInstance)
        } catch (ValidationException ve) {
            render view: 'edit', model: buildUserModel(userInstance)
            return
        }

        request.withFormat {
            form {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'user.label'),
                                                                                userInstance.username.equalsIgnoreCase(Constants.Commons.SYSTEM) ? Constants.Commons.SYSTEM : userInstance.username])
                redirect userInstance
            }
            '*' { respond userInstance, [status: OK] }
        }
    }

    private void bindGroups(User userInstance, boolean isEdit = false) {
        if(params.groups){
            if (params.groups.class == String) {
                params.groups = [params.groups]
            }
            List<String> updatedGroups=params.groups
            List<String> oldGroups=userInstance.groups.collect {it.id as String}
            if (params['groups'] && (updatedGroups-oldGroups!=[] || oldGroups-updatedGroups!=[])) {
                userInstance.groups = []
                def groups = Group.getAll(params['groups'])
                if(isEdit) {
                    groups = groups + UserGroupMapping.findAllByUser(userInstance).findAll {
                        it.group.groupType == GroupType.WORKFLOW_GROUP
                    }.collect { it.group }?.get(0)
                }
                userInstance.groups = groups
            }
        }


        if(params.safetyGroups){
            if (params.safetyGroups.class == String) {
                params.safetyGroups = [params.safetyGroups]
            }
            List<String> updatedSafetyGroups=params.safetyGroups
            List<String> oldSafetyGroups=userInstance.safetyGroups.collect {it.id as String}
            if (params['safetyGroups'] && (updatedSafetyGroups-oldSafetyGroups!=[] || oldSafetyGroups-updatedSafetyGroups!=[])) {
                userInstance.safetyGroups = []
                def safetyGroups = SafetyGroup.getAll(params['safetyGroups'])
                userInstance.safetyGroups = safetyGroups
            }
        }else if(params.safetyGroups==null && userInstance.safetyGroups!=null){
            userInstance.safetyGroups?.clear()
        }

    }
    @Transactional
    public User bindDepartments(User userInstance) {
        if (params.department) {
            if (params.department.class == String) {
                params.department = [params.department]
            }
            List updatedDepartment = params.department
            List currentList = userInstance?.userDepartments*.departmentName
            List userDeptList = []
            if (updatedDepartment - currentList != [] || currentList - updatedDepartment != []) {
                updatedDepartment.unique().each {
                    UserDepartment userDept = UserDepartment.findByDepartmentName(it)
                    if (!userDept) {
                        userDept = new UserDepartment(departmentName: it)
                        userDept = (UserDepartment) CRUDService.saveWithAuditLog(userDept)
                    }
                    userDeptList.add(userDept)
                }
                userInstance?.userDepartments?.clear()
                userDeptList?.each {
                    userInstance.addToUserDepartments(it)
                }
            }
        } else if(params.department==null && userInstance?.userDepartments!=null){
            userInstance?.userDepartments?.clear()
        }

        return userInstance
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def delete(User userInstance) {

        if (userInstance == null) {
            notFound()
            return
        }

        try {
            userService.deleteUserInfoFRomPvUserWebApp(userInstance.id)
            CRUDService.delete(userInstance)
            userGroupService.deleteUserGroupMappingsForUser(userInstance)
            request.withFormat {
                form {
                    flash.message = message(code: 'default.deleted.message', args: [message(code: 'user.label'), userInstance.username])
                    redirect action: "index", method: "GET"
                }
                '*' { render status: NO_CONTENT }
            }
        } catch (ValidationException ve) {
            request.withFormat {
                form {
                    flash.error = message(code: 'default.not.deleted.message', args: [message(code: 'user.label'), userInstance.username])
                    redirect(action: "show", id: params.id)
                }
                '*' { render status: FORBIDDEN }
            }
        }

    }

    def sharedUsers() {
        def activeUsers = userService.getActiveUsersList(params.term)
        render activeUsers as JSON
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'user.label'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    protected void addRoles(User userInstance) {
        for (String key in params.keySet()) {
            if (Role.findByAuthority(key) && 'on' == params.get(key)) {
                UserRole.create userInstance, Role.findByAuthority(key), true
            }
        }
        userInstance?.userRolesString = UserRole.findAllByUser(userInstance)?.role.toString()
        userInstance.save(flush:true)
    }

    protected List sortedRoles( ) {
        Role.list().sort { it.toString() }
    }

    protected Map buildUserModel(userInstance) {
        String authorityFieldName = SpringSecurityUtils.securityConfig.authority.nameField
        List roles = sortedRoles()
        Set<String> userRoleNames = UserRole.findAllByUser( userInstance ).collect { it.role.authority } as Set
        def granted = [:]
        def notGranted = [:]
        for (role in roles) {
            String authority = role[authorityFieldName]
            if (userRoleNames.contains(authority)) {
                granted[(role)] = userRoleNames.contains(authority)
            } else {
                notGranted[(role)] = userRoleNames.contains(authority)
            }
        }
        return [userInstance: userInstance, roleMap: granted + notGranted]
    }

    /**
     * Ajax call used by autocomplete textfield.
     */
    def ajaxLdapSearch() {
        def jsonData = []
        String usersSearchFilter = grailsApplication.config.grails.plugin.springsecurity.ldap.users.search.filter
        if (params.term?.length() > 2) {
            def results = []
            results.addAll(userService.searchLdapToAddUser(MessageFormat.format(usersSearchFilter, [params.term].toArray())))
            results = results as Set

            for (List ldapResult in results) {
                for (Map resultMap : ldapResult) {
                    resultMap.each { k, v ->
                        jsonData << [id: k, text: "${v}"]
                    }
                }
            }
        }
        render text: jsonData as JSON, contentType: 'text/plain'
    }

    def authenticate() {
        def password = params.password
        def userName = userService.getUser()?.username

        if (params.username && params.username != userName) {
            return false
        }
        def authorized = [:]

        if (userService.authenticate(userName, password)) {
            authorized.put('authorized', true)
        } else {
            authorized.put('authorized', false)
        }
        render(authorized as JSON)
    }

    def searchUserGroupList(String term, Integer page, Integer max) {
        List items = []
        try {
            if (!max) {
                max = 30
            }
            if (!page) {
                page = 1
            }
            if (term) {
                term = term?.trim()
            }
            int offset = Math.max(page - 1, 0) * max
            Set activeUsers = userService.getAllowedUsersForCurrentUser(term)
            Set activeGroups = userService.getAllowedGroupsForCurrentUser(term)
            List blindedUsersList = cacheService.getUserBlindingCache()
            List blindedUserGroupsList = cacheService.getUserGroupBlindingCache()
            List userList = activeUsers.unique { it.id }.sort { it.fullName?.toUpperCase()?.trim() }.collect {
                [id: Constants.USER_TOKEN + it.id, text: it.fullName ?: it.username, blinded: blindedUsersList?.contains(it.id)]
            }
            List groupList = activeGroups.unique { it.id }.sort { it.name?.toUpperCase()?.trim() }.collect {
                [id: Constants.USER_GROUP_TOKEN + it.id, text: it.name, blinded: blindedUserGroupsList?.contains(it.id)]
            }
            String autoAssignText = "Auto Assign"
            if (params.autoAssign && !term) {
                items.add(["text": "Auto Assign", "children": [[id: "AUTO_ASSIGN", text: autoAssignText, blinded: true]]])
            } else if (params.autoAssign && term && autoAssignText.toUpperCase().contains(term.toUpperCase())) {
                items.add(["text": "Auto Assign", "children": [[id: "AUTO_ASSIGN", text: "Auto Assign", blinded: true]]])
            }
            items = userService.splitResult(items, offset, max, groupList, userList)
        } catch (Exception e) {
            e.printStackTrace()
            log.error("Some error occurred", e)
        }
        render(items as JSON)
    }

    def searchUsersAndGroupsForFilterAlertsAndSignals(String term, Boolean isWorkflowEnabled, String alertType) {
        List items = []
        if (term) {
            term = term?.trim()
        }
        try {
            User currentUser = userService.getUser()
            List currentUserOptions = [[id:Constants.MINE + currentUser.id, text:Constants.FilterOptions.OWNER], [id:Constants.ASSIGN_TO_ME + currentUser.id, text:Constants.FilterOptions.ASSIGNED_TO_ME], [id:Constants.SHARED_WITH_ME + currentUser.id, text:Constants.FilterOptions.SHARED_WITH_ME]]
            if(alertType == "signalFilter" || alertType == "signalDashboard") {
                currentUserOptions = [[id:Constants.MINE + currentUser.id, text:Constants.FilterOptions.OWNER], [id:Constants.ASSIGN_TO_ME + currentUser.id, text:Constants.FilterOptions.ASSIGNED_TO_ME]]
            }
            String searchedItem = term?.toLowerCase()
            if (searchedItem) {
                currentUserOptions = currentUserOptions.findAll {
                    (it.text).toLowerCase().indexOf(searchedItem) > -1
                }
            }
            items << ["children": currentUserOptions]
            Set activeUsers = userService.getAllowedUsersForCurrentUser(term)
            Set activeGroups = userService.getAllowedGroupsForCurrentUser(term)
            List blindedUsersList = cacheService.getUserBlindingCache()
            List blindedUserGroupsList = cacheService.getUserGroupBlindingCache()
            List userList = activeUsers.unique { it.id }.sort { it.fullName?.toUpperCase()?.trim() }.collect {
                [id: Constants.USER_TOKEN + it.id, text: it.fullName ?: it.username, blinded: blindedUsersList?.contains(it.id)]
            }
            List groupList = activeGroups.unique { it.id }.sort { it.name?.toUpperCase().trim() }.collect {
                [id: Constants.USER_GROUP_TOKEN + it.id, text: it.name, blinded: blindedUserGroupsList?.contains(it.id)]
            }

            items = userService.splitResult(items, 0, 30, groupList, userList, true)
        } catch (Exception e) {
            e.printStackTrace()
            log.error("Some error occurred", e)
        }
        render(items as JSON)
    }

    def searchShareWithUserGroupList(String term, Integer page, Integer max, Boolean isWorkflowEnabled) {
        List items = []
        try {
            if (!max) {
                max = 30
            }
            if (!page) {
                page = 1
            }
            if (term) {
                term = term?.trim()
            }
            int offset = Math.max(page - 1, 0) * max
            Set activeUsers = userService.getShareWithUsersForCurrentUser(term, isWorkflowEnabled)
            Set activeGroups = userService.getShareWithGroupsForCurrentUser(term)
            List userList = []
            List groupList = []
            List blindedUsersList = cacheService.getUserBlindingCache()
            List blindedUserGroupsList = cacheService.getUserGroupBlindingCache()
            if (userService.hasAccessShareWith()) {
                userList = activeUsers.unique { it.id }.sort { it.fullName?.toUpperCase()?.trim() }.collect {
                    [id: Constants.USER_TOKEN + it.id, text: it.fullName ?: it.username, blinded: blindedUsersList?.contains(it.id)]
                }
                groupList = activeGroups.unique { it.id }.sort { it.name?.toUpperCase().trim() }.collect {
                    [id: Constants.USER_GROUP_TOKEN + it.id, text: it.name, blinded: blindedUserGroupsList?.contains(it.id)]
                }
            }
            String autoAssignText = "Auto Assign"
            if (params.autoAssign && !term) {
                items.add(["text": "Auto Assign", "children": [[id: "AUTO_ASSIGN", text: autoAssignText, blinded: true]]])
            } else if (params.autoAssign && term && autoAssignText.toUpperCase().contains(term.toUpperCase())) {
                items.add(["text": "Auto Assign", "children": [[id: "AUTO_ASSIGN", text: "Auto Assign", blinded: true]]])
            }
            items = userService.splitResult(items, offset, max, groupList, userList)
        } catch (Exception e) {
            e.printStackTrace()
            log.error("Some error occurred", e)
        }
        render(items as JSON)
    }

    def sharedWithValues() {
        def result = []
        params.ids?.split(";")?.each {
            if (it.startsWith(Constants.USER_GROUP_TOKEN)) {
                Group group = Group.get(Long.valueOf(it.replaceAll(Constants.USER_GROUP_TOKEN, '')))
                result << [id: it, text: group.name, blinded: true]
            } else if (it.startsWith(Constants.USER_TOKEN)) {
                User user = User.get(Long.valueOf(it.replaceAll(Constants.USER_TOKEN, '')))
                result << [id: it, text: user.fullName ?: user.username, blinded: true]
            }
        }
        render(result as JSON)
    }

    def sharedWithList(String term, Integer page, Integer max) {
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }
        int offset = Math.max(page - 1, 0) * max

        Set activeUsers = userService.getShareWithUsersForCurrentUser(term)
        Set activeGroups = userService.getShareWithGroupsForCurrentUser(term)
        List userList = activeUsers.unique { it.id }.collect {
            [id: Constants.USER_TOKEN + it.id, text: it.fullName ?: it.username]
        }
        List groupList = activeGroups.unique { it.id }.collect {
            [id: Constants.USER_GROUP_TOKEN + it.id, text: it.name]
        }
        def items = []
        userService.splitResult(items, offset, max, groupList, userList)
        render([items: items, total_count: userList.size() + groupList.size()] as JSON)
    }

    def eAuthenticate(String userName, String password) {
        render([authorized: ldapService.isLoginPasswordValid(userName, password)] as JSON)
    }

    @Secured('Authenticated')
    def getUserGroupsOfUser() {
        User user = User.findByUsername(params.userName)
        List<String> userGroupNames = user.groups?.collect { it.name }
        render([userGroupNames: userGroupNames, username: user.username] as JSON)
    }

    @Secured('Authenticated')
    def shareWithUsrGrpListProdGrp(String term, Integer page, Integer max) {
        List items = []
        try {
            if (!max) {
                max = 30
            }
            if (!page) {
                page = 1
            }
            if (term) {
                term = term?.trim()
            }
            int offset = Math.max(page - 1, 0) * max
            Set activeUsers = userService.getShareWithUsersForCurrentUser(term)
            Set activeGroups = userService.getShareWithGroupsForCurrentUser(term)
            List userList = activeUsers.unique { it.id }.sort { it.fullName?.trim() }.collect {
                [id: Constants.USER_TOKEN + it.username, text: it.fullName ?: it.username]
            }
            List groupList = activeGroups.unique { it.id }.sort { it.name.trim() }.collect {
                [id: Constants.USER_GROUP_TOKEN + it.name, text: it.name]
            }
            items = userService.splitResult(items, offset, max, groupList, userList)
        } catch (Exception e) {
            e.printStackTrace()
            log.error("Some error occurred", e)
        }
        render(items as JSON)
    }

    @Secured("Authenticated")
    def getFullNamesFromUserNames() {
        List userNames = JSON.parse(params.userList)
        List<Map> userList = User.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("username", "userName")
                property("fullName", "fullName")
            }
            'in'("username", userNames)
        }
        render([userList: userList.collect { [id: Constants.USER_TOKEN + it.userName, name: it.fullName] }] as JSON)
    }

    def usersEmailList(String term, Integer page, Integer max) {
       max=User.count()
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }
        int offset = Math.max(page - 1, 0) * max
        List<User> emailList = User.createCriteria().list {
            if (term) {
                ilike("email", "%$term%")
            }
            isNotNull("email")
            maxResults(max)
            firstResult(offset)
        }.sort {
            it.email?.toLowerCase()
        }

        int totalCount = User.createCriteria().count {
            if (term) {
                ilike("email", "%$term%")
            }
            ilike("email", "%$term%")
            isNotNull("email")
        }

        render([list: emailList.collect { [id: it.email, text: it.email] }, totalCount: totalCount] as JSON)
    }

    def addUsersToReports() {
        String status = "Failed"
        def result = alertService.copyBulkUsers()
        if (result?.status == HttpStatus.SC_OK) {
            status = "Success"
        }
        signalAuditLogService.createAuditLog([
                entityName : "user",
                moduleName : "User Management",
                category   : AuditTrail.Category.INSERT.toString(),
                entityValue: "Copy Users to PV Reports",
                description: "",
        ], [[propertyName: "Status", newValue: status]])
        render result as JSON
    }


}

class UserCommand implements Validateable {
    def userService
    Long id
    String username
    boolean enabled = true
    String email
    String fullName
    boolean accountExpired
    boolean accountLocked
    boolean passwordExpired
    Preference preference = new Preference()
    List roles = []
    List<Group> groups = []
    List safetyGroups = []
    List userDepartments = []
    UserType type = "LDAP"

    Integer badPasswordAttempts = 0

    static constraints = {
        importFrom User, exclude: ["password", "email", "fullName", "username"]
        preference validator: { val, obj ->
            obj.preference.createdBy = obj.userService.getUser().username
            obj.preference.modifiedBy = obj.userService.getUser().username
            return true
        }
        username blank: false, maxSize: 255, validator: { val, obj ->
            if (User.findByUsernameIlike(val)) {
                return "com.rxlogix.UserCommand.username.unique"
            }
        }
        email nullable: true, blank: true, email: true, validator: { val, obj ->
            if (obj.type.equals(UserType.NON_LDAP)) {
                if (!val) {
                    return "com.rxlogix.UserCommand.email.unique"
                }
            }
        }
        fullName nullable: true, blank: true, validator: { val, obj ->
            if (obj.type.equals(UserType.NON_LDAP)) {
                if (!val) {
                    return "com.rxlogix.UserCommand.fullName.nullable"
                }
            }
        }
        userService nullable: true
    }

    Group getWorkflowGroup() {
        try {
            this.groups.find { it.groupType == GroupType.WORKFLOW_GROUP }
        } catch (Exception e) {
            null
        }
    }

}
