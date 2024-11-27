package com.rxlogix

import com.rxlogix.commandObjects.LdapCommand
import com.rxlogix.commandObjects.TokenAuthenticationCO
import com.rxlogix.config.Configuration
import com.rxlogix.config.EvdasConfiguration
import com.rxlogix.config.LiteratureConfiguration
import com.rxlogix.dto.DispositionCountDTO
import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.enums.TimeZoneEnum
import com.rxlogix.enums.UserType
import com.rxlogix.signal.AdHocAlert
import com.rxlogix.signal.ProductViewAssignment
import com.rxlogix.signal.UserGroupFieldMapping
import com.rxlogix.signal.UserPinConfiguration
import com.rxlogix.signal.UserReferencesMapping
import com.rxlogix.signal.ViewInstance
import com.rxlogix.user.*
import com.rxlogix.util.DateUtil
import com.rxlogix.util.SignalQueryHelper
import grails.converters.JSON
import grails.events.EventPublisher
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.sql.Sql
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.sql.JoinType
import org.hibernate.transform.Transformers
import org.hibernate.type.LongType
import org.springframework.ldap.NamingException
import org.springframework.ldap.core.AttributesMapper
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.apache.commons.codec.digest.DigestUtils
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.util.StringUtils

import javax.naming.directory.Attributes
import javax.naming.directory.SearchControls

import com.rxlogix.util.SecurityUtil

class UserService implements EventPublisher {

    static transactional = false

    def springSecurityService
    def signalAuditLogService
    def authenticationManager
    def grailsApplication
    def ldapTemplate
    def userGroupService
    def cacheService
    def ldapService
    SeedDataService seedDataService
    ViewInstanceService viewInstanceService
    def dataSource
    def productAssignmentService
    def signalDataSourceService
    def sessionFactory
    def CRUDService
    def utilService
    def dataSource_pva
    def groupService
    def messageSource

    //todo:  this shouldn't be here; refactor - morett
    @Transactional
    def updateUserPreference(Preference preference, Locale userLocale) {
        preference.locale = userLocale
        return preference.save()
    }

    /*
    ==============================================================================================
    Keep methods below non-transactional
    ==============================================================================================
    */

    User getUser() {
        return springSecurityService.loggedIn && springSecurityService?.principal?.id ? User.get(springSecurityService.principal.id) : null
    }

    Long getUserId() {
        if(springSecurityService.loggedIn){
            return springSecurityService?.principal?.id as Long
        }
        return 0l
    }

    User getUserByUsername(String username) {
        if(username) {
            User.findByUsernameIlikeAndEnabled(username, true)
        }
    }

    def getGmtOffset(String timezoneId = "UTC") {
        return " (GMT " + DateUtil.getOffsetString(timezoneId) + ")"
    }

    def getFullNameFromLdap(String username) {

        def usernameAttr = grailsApplication.config.grails.plugin.springsecurity.ldap.username.attribute
        def fullnameAttr = grailsApplication.config.grails.plugin.springsecurity.ldap.fullName.attribute

        def details = getLdapDetails("${usernameAttr}=$username", fullnameAttr)
        if (details)
            details.get(0) ?: username
        else
            username
    }

    def getLdapDetails(String filter, String attribute) {
        String searchBase = grailsApplication.config.grails.plugin.springsecurity.ldap.search.base
        try {
            return ldapTemplate.search(searchBase, filter,
                    new AttributesMapper() {
                        public Object mapFromAttributes(Attributes attrs) throws NamingException {
                            return attrs?.get(attribute)?.get()?.toString()
                        }
                    })
        } catch (Throwable t) {
            log.error(" ================================== ", t)
            return null
        }
    }


    def setOwnershipAndModifier(Object object) {
        if (springSecurityService.isLoggedIn()) {
            def user = getUser()
            //Set one time only
            if (object.hasProperty("createdBy")) {
                if (!object.createdBy) {
                    object.createdBy = user.username
                }
            }
            if (object.hasProperty("modifiedBy")) {
                object.modifiedBy = user.username
            }
        }
        return object
    }
    def setOwnershipAndModifierFullName(Object object) {
        if (springSecurityService.isLoggedIn()) {
            def user = getUser()
            //Set one time only
            if (object.hasProperty("createdBy")) {
                if (!object.createdBy) {
                    object.createdBy = user.username
                }
            }
            if (object.hasProperty("modifiedBy")) {
                object.modifiedBy = user.fullName
            }
        }
        return object
    }

    def getAllEmails(Configuration config) {
        if (config && config?.owner)
            getActiveUsers().email - config.owner.email
        else
            (getActiveUsers() - getUser()).collect { it?.email }.findAll { it != null }

    }

    def getActiveUsers() {
        User.findAllByEnabled(true)
    }

    def getAdminUsers() {
        def adminUsers = UserRole.findAllByRoleInList([Role.findAllByAuthority("ROLE_ADMIN"), Role.findAllByAuthority("ROLE_DEV")])*.user
        return adminUsers
    }

    @Transactional
    void createUser(String username, Preference pref, List<String> roles, String createdBy, List<Group> groupsList, UserType userType = UserType.NON_LDAP,String emailNonLdap = null,String fullNameNonLdap = null) {
        User user = User.findByUsernameIlike(username)
        if (!user) {
            String uid = grailsApplication.config.grails.plugin.springsecurity.ldap.uid.attribute
            List<LdapCommand> ldapEntry = ldapService.getLdapEntry("$uid=$username")
            String fullName = ldapEntry[0]?.getFullName()
            String email = ldapEntry[0]?.getEmail()
            user = new User(username: username, preference: pref, createdBy: createdBy, modifiedBy: createdBy, fullName: fullName?:fullNameNonLdap, email: email?:emailNonLdap,  type: userType)
            groupsList?.each{
                if(it && it != null){
                    user.addToGroups(it)
                }
            }
            if(user.type == UserType.NON_LDAP && user.password != null){
                user.password = springSecurityService.encodePassword(Holders.config?.password?.defaultUserPassword ?: 'changeit')
            }
            else{
                user.password = null
            }
            user.save(flush:true)
            new UserDashboardCounts(userId: user.id).save(failOnError: true)
            roles.each {
                if(Role.findByAuthority(it))
                     UserRole.create(user, Role.findByAuthority(it))
            }
            //TODO: Need to change two save call as this is required because userRole cannt be created on unsaved user
            user.userRolesString=UserRole.findAllByUser(user)?.role.toString()
            user.save(flush:true)
        }
    }

    def getAllUsers() {
        User.list()
    }

    /**
     * A method to authenticate against username and password
     * @param username
     * @param password
     * @return
     */
    def authenticate(username, password) {

        try {
            //Get user name and password authentication token.
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);

            //Authentication object created from the current user and passed password.
            Authentication lateAuthenticatication = authenticationManager.authenticate(token);

            //Authentication object fetched from current authentication.
            Authentication loggedInAuthentication = SecurityContextHolder.getContext().getAuthentication()

            //Check for authentication
            if (lateAuthenticatication?.isAuthenticated() && loggedInAuthentication.getPrincipal().equals(lateAuthenticatication?.getPrincipal())) {
                groupService.updateMenuVisibilityInSession(request);
                return true
            } else {
                return false
            }
        } catch (Throwable ex) {
            log.error(ex)
            return false
        }
    }

    User saveOutlookData(String accessToken, String refreshToken) {
        log.info("saving outlook data to user")
        log.info("count of char: [accessToken : ${accessToken?.length()}, refreshToken : ${refreshToken?.length()}]")
        User user = getUser()
        user.outlookAccessToken = accessToken
        user.outlookRefreshToken = refreshToken
        user.save(failOnError: true, flush: true)
    }

    //Overridding the dev user email with the configured external email. This is controlled by external config file.
    def getDevUserEmail(userName) {

        if (Holders.config.signal.enable.default.email && userName == 'dev') {
            return Holders.config.signal.default.email
        } else {
            return null
        }
    }


    def getUserIdFromEmail(String email) {
        User.findByEmail(email)?.getId()
    }

    def getEmailFromLdap(String username) {
        def email = getDevUserEmail(username)
        if (email) {
            return email
        }

        def usernameAttr = grailsApplication.config.grails.plugin.springsecurity.ldap.username.attribute
        def emailAttr = grailsApplication.config.grails.plugin.springsecurity.ldap.email.attribute

        def details = getLdapDetails("${usernameAttr}=$username", emailAttr)
        if (details)
            details.get(0) ?: ""
        else
            ""
    }

    def searchLdapToAddUser(String filter) {
        String searchBase = grailsApplication.config.grails.plugin.springsecurity.ldap.search.base
        String uid = grailsApplication.config.grails.plugin.springsecurity.ldap.uid.attribute
        String fullName = grailsApplication.config.grails.plugin.springsecurity.ldap.fullName.attribute
        String email = grailsApplication.config.grails.plugin.springsecurity.ldap.email.attribute
        //We are trying to get only those attributes which we would need to display.
        return ldapTemplate.search(searchBase, filter, SearchControls.SUBTREE_SCOPE, (String[]) [uid, fullName, email],
                new AttributesMapper() {
                    public Object mapFromAttributes(Attributes attrs) throws javax.naming.NamingException {
                        List ldapResults = []
                        Map ldapResultsMap = [:]
                        String key = attrs?.get(uid)?.get()?.toString()
                        String value =
                                attrs?.get(uid)?.get()?.toString() + " - " +
                                        attrs?.get(fullName)?.get()?.toString() + " - " +
                                        attrs?.get(email)?.get()?.toString()
                        if (Objects.nonNull(key)) {
                            key = key.trim()  //Added for PVS-72203
                        }
                        if ( !User.createCriteria().count { ilike('username', key )}) { //Added for PVS-65719
                            ldapResultsMap.put(key, value)
                            ldapResults.add(ldapResultsMap)
                        }
                        return ldapResults
                    }
                })
    }

    def getActiveUsersList(String search) {
        String _search = search?.toLowerCase()
        def activeUsersList = User.list()
        if (search)
            activeUsersList = activeUsersList.findAll {
                (it.fullName ?: it.username).toLowerCase().indexOf(_search) > -1
            }
        activeUsersList = activeUsersList.collect {
            [id: it.id, text: it.fullName ?: it.username]
        }
        activeUsersList
    }

    def getAllowedUsersForCurrentUser(String search = null, Boolean isWorkflowEnabled = true) {
        String _search = search?.toLowerCase()
        Long currentUserId = getUserId()
        def userInfoList = enabledUserInformation(currentUserId)
        def users = userInfoList.userList
        Long currentUserWorkflowGroupId = userInfoList.currentUserWorkflowGroupId
        if (search) {
            return users.findAll {
                (it.fullName ?: it.username).toLowerCase().indexOf(_search) > -1 && (isWorkflowEnabled ? (currentUserWorkflowGroupId == it.workflowGroupId) : true)
            }
        } else {
            return users.findAll {
                isWorkflowEnabled ? (currentUserWorkflowGroupId == it.workflowGroupId) : true
            }
        }
    }

    def enabledUserInformation(Long currentUserId) {
        Sql sql = new Sql(dataSource)
        try {
            def result
            Long currentUserWorkflowGroupId
            String sqlQuery = """
            SELECT
            PVUSER.ID AS id,
            PVUSER.username AS username,
            PVUSER.full_name AS fullName,
            Groups.ID AS workflowGroupId
            FROM
            PVUSER
            INNER JOIN
            User_group_mapping ON PVUSER.ID = User_group_mapping.USER_ID
            INNER JOIN
            Groups ON User_group_mapping.GROUP_ID = Groups.ID
            WHERE
            PVUSER.enabled = true
            AND Groups.Group_type = 'WORKFLOW_GROUP'
            ORDER BY
            PVUSER.username ASC
            """

            result = sql.rows(sqlQuery).collect { row ->
                if (row.id == currentUserId) {
                    currentUserWorkflowGroupId = row.workflowGroupId as Long
                }
                [id: (row.id as Long), username: row.username, fullName: row.fullName, workflowGroupId: row.workflowGroupId as Long]
            }
            return [userList: result, currentUserWorkflowGroupId: currentUserWorkflowGroupId]

        } catch (Exception ex) {
            ex.printStackTrace()
        } finally {
            sql?.close()
        }
    }

    def enabledUserInformationGroups(Long currentUserId) {
        Sql sql = new Sql(dataSource)
        try {
            def result
            Long currentUserWorkflowGroupId
            String sqlQuery = """
            SELECT
            PVUSER.ID AS id,
            PVUSER.username AS username,
            PVUSER.full_name AS fullName,
            Groups.ID AS workflowGroupId
            FROM
            PVUSER
            INNER JOIN
            User_group_mapping ON PVUSER.ID = User_group_mapping.USER_ID
            INNER JOIN
            Groups ON User_group_mapping.GROUP_ID = Groups.ID
            WHERE
            PVUSER.enabled = 1
            AND Groups.Group_type <> 'WORKFLOW_GROUP'
            ORDER BY
            PVUSER.username ASC
            """

            result = sql.rows(sqlQuery).collect { row ->
                if (row.id == currentUserId) {
                    currentUserWorkflowGroupId = row.workflowGroupId as Long
                }
                [id: (row.id as Long), username: row.username, fullName: row.fullName, workflowGroupId: row.workflowGroupId as Long]
            }
            return [userList: result, currentUserWorkflowGroupId: currentUserWorkflowGroupId]

        } catch (Exception ex) {
            ex.printStackTrace()
        } finally {
            sql?.close()
        }
    }


    List getAllowedGroupsForCurrentUser(String search = null) {
        String _search = search?.toLowerCase()
        def groups
        groups = getActiveGroupsFromSql()
        if (search)
            return groups.findAll {
                it.name.toLowerCase().indexOf(_search) > -1
            }
        else
            return groups
    }

    def getShareWithUsersForCurrentUser(String search = null, Boolean isWorkflowEnabled = true, User currentUser = null) {
        String _search = search?.toLowerCase()
        def userInfoList
        if(!currentUser){
            currentUser = getUser()
        }
        List users = []
        if (SpringSecurityUtils.ifAnyGranted("ROLE_SHARE_ALL")|| ["ROLE_ADMIN","ROLE_CONFIGURE_TEMPLATE_ALERT"].any{currentUser.getAuthorities().authority?.contains(it)}) {
            userInfoList = enabledUserInformation(currentUser.id)
            users = userInfoList.userList
        } else if (SpringSecurityUtils.ifAnyGranted("ROLE_SHARE_GROUP")) {
            userInfoList = enabledUserInformationGroups(currentUser.id)
            users = userInfoList.userList
        }

        if (search) {
            return users.findAll {
                (it.fullName ?: it.username).toLowerCase().indexOf(_search) > -1 && (isWorkflowEnabled ? (userInfoList.currentUserWorkflowGroupId == it.workflowGroupId) : true)
            }
        } else {
            return users.findAll {
                isWorkflowEnabled ? (userInfoList.currentUserWorkflowGroupId == it.workflowGroupId) : true
            }
        }
    }

    def getShareWithGroupsForCurrentUser(String search = null,User currentUser = null) {
        String _search = search?.toLowerCase()
        if(!currentUser){
            currentUser = getUser()
        }
        List groups = []
        if (SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN") || ["ROLE_ADMIN","ROLE_CONFIGURE_TEMPLATE_ALERT", "ROLE_EXECUTE_SHARED_ALERTS"].any{currentUser.getAuthorities().authority?.contains(it)} ) {
            groups = getActiveGroupsFromSql()
        } else if (SpringSecurityUtils.ifAnyGranted("ROLE_SHARE_GROUP")) {
            groups = getUserActiveGroupsFromSql(currentUser.id)
        }
        if (search)
            return groups.findAll {
                it.name.toLowerCase().indexOf(_search) > -1
            }
        else
            return groups
    }

    List searchUserGroupList(String term, Integer page, Integer max, Boolean autoAssign){
        List items = []
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
        Set activeUsers = getAllowedUsersForCurrentUser(term)
        Set activeGroups = getAllowedGroupsForCurrentUser(term)
        List blindedUsersList = cacheService.getUserBlindingCache()
        List blindedUserGroupsList = cacheService.getUserGroupBlindingCache()
        List userList = activeUsers.unique { it.id }.sort { it.fullName?.toUpperCase()?.trim() }.collect {
            [id: Constants.USER_TOKEN + it.id, text: it.fullName ?: it.username, blinded: blindedUsersList?.contains(it.id)]
        }
        List groupList = activeGroups.unique { it.id }.sort { it.name?.toUpperCase()?.trim() }.collect {
            [id: Constants.USER_GROUP_TOKEN + it.id, text: it.name, blinded: blindedUserGroupsList?.contains(it.id)]
        }
        String autoAssignText = "Auto Assign"
        if (autoAssign && !term) {
            items.add(["text": "Auto Assign", "children": [[id: "AUTO_ASSIGN", text: autoAssignText, blinded: true]]])
        } else if (autoAssign && term && autoAssignText.toUpperCase().contains(term.toUpperCase())) {
            items.add(["text": "Auto Assign", "children": [[id: "AUTO_ASSIGN", text: "Auto Assign", blinded: true]]])
        }
        items = splitResult(items, offset, max, groupList, userList)
        return items
    }

    List searchShareWithUserGroupList(String term, Integer page, Integer max, Boolean isWorkflowEnabled, Boolean autoAssign){
        List items = []
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
        Set activeUsers = getShareWithUsersForCurrentUser(term, isWorkflowEnabled)
        Set activeGroups = getShareWithGroupsForCurrentUser(term)
        List userList = []
        List groupList = []
        List blindedUsersList = cacheService.getUserBlindingCache()
        List blindedUserGroupsList = cacheService.getUserGroupBlindingCache()
        if (hasAccessShareWith()) {
            userList = activeUsers.unique { it.id }.sort { it.fullName?.toUpperCase()?.trim() }.collect {
                [id: Constants.USER_TOKEN + it.id, text: it.fullName ?: it.username, blinded: blindedUsersList?.contains(it.id)]
            }
            groupList = activeGroups.unique { it.id }.sort { it.name?.toUpperCase().trim() }.collect {
                [id: Constants.USER_GROUP_TOKEN + it.id, text: it.name, blinded: blindedUserGroupsList?.contains(it.id)]
            }
        }
        String autoAssignText = "Auto Assign"
        if (autoAssign && !term) {
            items.add(["text": "Auto Assign", "children": [[id: "AUTO_ASSIGN", text: autoAssignText, blinded: true]]])
        } else if (autoAssign && term && autoAssignText.toUpperCase().contains(term.toUpperCase())) {
            items.add(["text": "Auto Assign", "children": [[id: "AUTO_ASSIGN", text: "Auto Assign", blinded: true]]])
        }
        items = splitResult(items, offset, max, groupList, userList)
        return items
    }



    List splitResult(items, offset, max, groupList, userList, filtering = false) {
        String groupLabel = "Group"
        String userLabel =  messageSource.getMessage("user.label", null, Locale.default)
        if(filtering) {
            groupLabel = Constants.FilterHeadings.USER_GROUP
            userLabel = Constants.FilterHeadings.USER
        }
        List selectedGroupItems = groupList
        List selectedUserItems = userList
        if (selectedGroupItems.size() > 0)
            items << ["text": groupLabel, "children": selectedGroupItems]
        if (selectedUserItems.size() > 0)
            items << ["text": userLabel, "children": selectedUserItems]
        items
    }

    def getActiveGroupsFromSql() {
        Sql sql = new Sql(dataSource)
        def result
        try{
            String sqlQuery = """
            Select ID as id,
            NAME as name,
            group_type as groupType
            from groups
            where group_type <> 'WORKFLOW_GROUP'
            Order by name asc
         """
            result = sql.rows(sqlQuery).collect {
                [id: it.id as Long, name: it.name]
            }
            return result
        } catch (Exception ex){
            ex.printStackTrace()
        } finally {
            sql?.close()
        }
    }

    def getUserActiveGroupsFromSql(Long currentUserId) {
        Sql sql = new Sql(dataSource)
        def result
        try{
            String sqlQuery = """
            Select ID as id,
            NAME as name,
            group_type as groupType
            from groups
            where group_type <> 'WORKFLOW_GROUP'
            AND ID IN (SELECT GROUP_ID FROM USER_GROUP_MAPPING WHERE USER_ID = ${currentUserId})
            Order by name asc
         """
            result = sql.rows(sqlQuery).collect {
                [id: it.id as Long, name: it.name]
            }
            return result
        } catch (Exception ex){
            ex.printStackTrace()
        } finally {
            sql?.close()
        }
    }

    def assignGroupOrAssignTo(String assignedTo, def domain = '', Map productMap = [:], User user= null, Map autoAssignStatus=[:]) {
        if (assignedTo) {
            if (assignedTo.startsWith(Constants.USER_GROUP_TOKEN)) {
                domain.assignedToGroup = Group.get(Long.valueOf(assignedTo.replaceAll(Constants.USER_GROUP_TOKEN, '')))
                domain.assignedTo = null
                if(domain.hasProperty("isAutoAssignedTo"))
                    domain.isAutoAssignedTo = false
            } else if (assignedTo.startsWith(Constants.USER_TOKEN)) {
                domain.assignedTo = User.get(Long.valueOf(assignedTo.replaceAll(Constants.USER_TOKEN, '')))
                domain.assignedToGroup = null
                if(domain.hasProperty("isAutoAssignedTo"))
                    domain.isAutoAssignedTo = false
            } else if(assignedTo == "AUTO_ASSIGN"){
                String hierarchy = ""
                List productAssignment = []
                if(productMap.product){
                    Map productAssignmentMap = JSON.parse(productMap.product)
                    productAssignmentMap.each{
                        if(it.value){
                            hierarchy = productAssignmentService.getProductHierarchyWithoutDicMap(it.key as Integer)
                            productAssignment = it.value
                        }
                    }
                } else {
                    hierarchy = "Product Group"
                    productAssignment = JSON.parse(productMap.productGroup)
                }
                Long workflowGroup = user?user.workflowGroup?.id:getUser().getWorkflowGroup().id
                ProductViewAssignment matchedAssignment = ProductViewAssignment."pva".createCriteria().get {
                    eq("workflowGroup",workflowGroup)
                    eq("hierarchy",hierarchy)
                    sqlRestriction("JSON_VALUE(product,'\$.id') = ${productAssignment[0].id as BigInteger}")
                    maxResults(1)
                }
                if(!matchedAssignment){
                    matchedAssignment = ProductViewAssignment."pva".createCriteria().get {
                        isNull('workflowGroup')
                        eq("hierarchy",hierarchy)
                        sqlRestriction("JSON_VALUE(product,'\$.id') = ${productAssignment[0].id as BigInteger}")
                        maxResults(1)
                    }
                }
                String primaryUserOrGroup = matchedAssignment?.primaryUserOrGroupId
                if(primaryUserOrGroup) {
                    if (primaryUserOrGroup.startsWith(Constants.USER_GROUP_TOKEN) && Group.get(Long.valueOf(primaryUserOrGroup.replaceAll(Constants.USER_GROUP_TOKEN, '')))) {
                        domain.assignedToGroup = Group.get(Long.valueOf(primaryUserOrGroup.replaceAll(Constants.USER_GROUP_TOKEN, '')))
                        domain.assignedTo = null
                        domain.isAutoAssignedTo = true
                    } else if (primaryUserOrGroup.startsWith(Constants.USER_TOKEN) && User.get(Long.valueOf(primaryUserOrGroup.replaceAll(Constants.USER_TOKEN, '')))) {
                        domain.assignedTo = User.get(Long.valueOf(primaryUserOrGroup.replaceAll(Constants.USER_TOKEN, '')))
                        domain.assignedToGroup = null
                        domain.isAutoAssignedTo = true
                    }
                } else {
                    autoAssignStatus.put("status", false)
                }
            } else {
                domain.assignedTo = null
                domain.assignedToGroup = null
            }
        } else {
            domain.assignedTo = null
            domain.assignedToGroup = null
        }
        domain
    }

    def assignGroupOrAssignToForMasterTemplate(String assignedTo, def domain = '', Map productMap = [:], User user= null, Map autoAssignStatus=[:]) {
        domain.assignedTo = domain.owner
        domain
    }


    List<String> getRecipientsList(def domain) {
        domain?.assignedTo ? [domain?.assignedTo?.email] : userGroupService.fetchUserEmailsForGroup(domain?.assignedToGroup)
    }

    List<User> getUserListFromAssignToGroup(def domain) {
        domain?.assignedTo ? [domain?.assignedTo] : userGroupService.fetchUserListForGroup(domain?.assignedToGroup)
    }

    Long getIdFromAssignTo(def domain) {
        domain?.assignedTo ? domain?.assignedTo?.id : domain?.assignedToGroup?.id
    }

    String getAssignToValue(def domain) {
        String assignedToPrefix = domain?.assignedTo ? Constants.USER_TOKEN : Constants.USER_GROUP_TOKEN
        assignedToPrefix += getIdFromAssignTo(domain)
        assignedToPrefix
    }

    String getAssignedToName(def domain) {
        domain?.assignedTo ? domain?.assignedTo?.fullName : domain?.assignedToGroup?.name
    }

    String getAssignedToNameFromCache(Long userId, Long groupId) {
        userId ? cacheService.getUserByUserId(userId)?.fullName : cacheService.getGroupByGroupId(groupId)?.name
    }

    String getAssignedToNameAction(def alert) {
        alert.assignedToId ? cacheService.getUserByUserId(alert.assignedToId)?.fullName : cacheService.getGroupByGroupId(alert.assignedToGroupId)?.name
    }

    List generateEmailDataForAssignedToChange(String newEmailMessage, List<User> newUserList, String oldEmailMessage, List<User> oldUserList) {
        List emailDataList = []
        newUserList.each { User newUser ->
            emailDataList << [user: newUser, emailMessage: newEmailMessage]
        }

        oldUserList.each { User oldUser ->
            emailDataList << [user: oldUser, emailMessage: oldEmailMessage]
        }
        emailDataList
    }

    List generateLiteratureEmailDataForAssignedToChange(String newEmailMessage, List<User> newUserList, String oldEmailMessage, List<User> oldUserList) {
        List emailDataList = []
        newUserList.each { User newUser ->
            emailDataList << [email: newUser.email, emailMessage: newEmailMessage]
        }

        oldUserList.each { User oldUser ->
            emailDataList << [email: oldUser.email, emailMessage: oldEmailMessage]
        }
        emailDataList
    }

    Long getCurrentUserId() {
        return springSecurityService?.principal?.id
    }

    String getCurrentUserName() {
        return springSecurityService?.principal?.username
    }

    Preference getCurrentUserPreference() {
        cacheService.getPreferenceByUserId(springSecurityService?.principal?.id)
    }

    User getUserFromCacheByUsername(String userName) {
        cacheService.getUserByUserNameIlike(userName)
    }

    boolean checkIfEligibleForProductAssignmentOrNot(Configuration configuration){
        boolean isEligible = true
        List productGroupAssignment = []
        if(configuration.productGroupSelection) {
            productGroupAssignment = JSON.parse(configuration.productGroupSelection)
            if (productGroupAssignment.size() > 1) {
                isEligible = false
            }
        }
        Map productAssignment = [:]
        if(configuration.productSelection) {
            productAssignment = JSON.parse(configuration.productSelection)
            productAssignment.each { assignmentMap ->
                if (assignmentMap.value) {
                    if (assignmentMap.value.size() > 1) {
                        isEligible = false
                    } else if (productGroupAssignment.size() == 1 && assignmentMap.value.size() == 1) {
                        isEligible = false
                    }
                }
            }
        }
        if(!configuration.productSelection && !configuration.productGroupSelection){
            isEligible = false
        }

        return isEligible

    }

    List getUserIdFromGroup(Long groupId){
        String sql =  SignalQueryHelper.get_user_id_from_group_sql(groupId)
        Session session = sessionFactory.currentSession
        SQLQuery sqlQuery = session.createSQLQuery(sql)
        sqlQuery.addScalar("userId", new LongType())
        sqlQuery.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
        sqlQuery.list()
    }
    void addReferencesToUsers(Long objectId, Long referenceId, Boolean isGroup){
        List members = []
        if(isGroup){
            members = getUserIdFromGroup(objectId)
        } else {
            members = [[userId: objectId]]
        }
        members.each { member ->
            Integer priority = UserReferencesMapping.findAllByUserIdAndIsDeleted(member.userId as Long,false)?.size()
            UserReferencesMapping userReferencesMapping = UserReferencesMapping.findByUserIdAndReferenceId(member.userId as Long, referenceId as Long)
            if(!userReferencesMapping){
                userReferencesMapping = new UserReferencesMapping()
                userReferencesMapping.userId = member.userId as Long
                userReferencesMapping.isPinned = false
                userReferencesMapping.referenceId = referenceId as Long
                userReferencesMapping.isDeleted = false
                userReferencesMapping.priority = priority ? priority+1 : 1
                userReferencesMapping.save()
            } else if(userReferencesMapping && userReferencesMapping.isDeleted == true){
                userReferencesMapping.isPinned = false
                userReferencesMapping.isDeleted = false
                userReferencesMapping.priority = priority ? priority+1 : 1
                userReferencesMapping.save()
            }
        }
    }

    void bindSharedWithConfiguration(def configurationInstance, def sharedWith, Boolean isUpdate = false, Boolean isShareWithFilterView = false,
                                        Map productMap = [:],User currentUser = null, Boolean isShareReference = false, Map autoShareStatus=[:], Boolean isMasterTemplateAlert = false) {
        List shareWithUsers = isShareWithFilterView ? getAllowedUsersForCurrentUser(null, false) : getShareWithUsersForCurrentUser(null,true,currentUser);
        List shareWithGroups = isShareWithFilterView ? getAllowedGroupsForCurrentUser() : getShareWithGroupsForCurrentUser(null,currentUser);
        List userIdsDuplicate = []
        List groupIdsDuplicate = []
        List userIdsCurrent = []
        List groupIdsCurrent = []
        Session session = sessionFactory.currentSession
        //this session scope is made global to prevent flushing of session in between transaction to prevent multiple audit entries PVS-48760 comment
        try{
            boolean isSingleOrAggregate = false
            if(configurationInstance.hasProperty("type")) {
                isSingleOrAggregate = configurationInstance.type in [Constants.AlertConfigType.SINGLE_CASE_ALERT, Constants.AlertConfigType.AGGREGATE_CASE_ALERT]
            }
            if (isUpdate) {
                if(isSingleOrAggregate){
                    boolean isEligible = checkIfEligibleForProductAssignmentOrNot(configurationInstance)
                    if(isEligible){
                        productMap = ["product":configurationInstance.productSelection, "productGroup": configurationInstance.productGroupSelection]
                    }
                }
                if (configurationInstance?.getShareWithUser()) {
                    shareWithUsers.addAll(configurationInstance.getShareWithUser())
                    shareWithUsers.unique { it.id }
                }
                if(isSingleOrAggregate && configurationInstance?.autoShareWithUser){
                    shareWithUsers.addAll(configurationInstance.autoShareWithUser)
                    shareWithUsers.unique { it.id }
                    configurationInstance?.autoShareWithUser?.clear()
                }
                if (configurationInstance?.getShareWithGroup()) {
                    shareWithGroups.addAll(configurationInstance.getShareWithGroup())
                    shareWithGroups.unique { it.id }
                }
                if(isSingleOrAggregate && configurationInstance?.autoShareWithGroup){
                    shareWithGroups.addAll(configurationInstance.autoShareWithGroup)
                    shareWithGroups.unique { it.id }
                    configurationInstance?.autoShareWithGroup?.clear()
                }

                    configurationInstance?.shareWithUser?.clear()
                    configurationInstance?.shareWithGroup?.clear()

                if (configurationInstance.hasProperty("autoShareWithUser")) {
                    configurationInstance?.autoShareWithUser?.clear()
                    configurationInstance?.autoShareWithGroup?.clear()
                }
            }

            if (sharedWith) {
                def shareWithValues = []
                shareWithValues << sharedWith
                List<Long> totalSharedUsers = []
                // for reference remove all mapping for current reference to all user and group
                if(isShareReference){
                    shareWithValues.flatten().each { String shared ->
                        shared = shared.replaceAll("_readOnly", "")
                        if (shared.startsWith(Constants.USER_GROUP_TOKEN)) {
                            // If shared with group, create list of all users in Group
                            Long groupId = Long.valueOf(shared.replaceAll(Constants.USER_GROUP_TOKEN, ''))
                            if (groupId) {
                                List groupUserId = getUserIdFromGroup(groupId)
                                groupUserId.each {it ->
                                    if(!totalSharedUsers.contains(it['userId'])){
                                        totalSharedUsers.add(it['userId'] as Long)
                                    }
                                }
                            }
                        } else if (shared.startsWith(Constants.USER_TOKEN)) {
                            Long userId = Long.valueOf(shared.replaceAll(Constants.USER_TOKEN, ''))
                            if (userId && !totalSharedUsers.contains(userId)) {
                                totalSharedUsers.add(userId)
                            }
                        }
                    }
                    if (totalSharedUsers.size() > 0) {
                        SQLQuery sql = null
                        String deleteUnMappedUsers = SignalQueryHelper.remove_reference_mapping_to_users_and_groups(configurationInstance.id, totalSharedUsers)
                        sql = session.createSQLQuery(deleteUnMappedUsers)
                        sql.executeUpdate()
                       // session.flush() commented this to prevent session flush in a transacation PVS-48760
                    }
                }

                shareWithValues.flatten().each { String shared ->
                    shared = shared.replaceAll("_readOnly", "")
                    if (shared.startsWith(Constants.USER_GROUP_TOKEN)) {
                        Group group = Group.get(Long.valueOf(shared.replaceAll(Constants.USER_GROUP_TOKEN, '')))
                        if (group) {
                            configurationInstance.addToShareWithGroup(group)
                            if(shareWithGroups.find { it.id == group.id }) {
                                groupIdsCurrent.add(group.id)
                            }
                            if(isShareReference){
                                addReferencesToUsers(group.id, configurationInstance.id as Long, true)
                            }
                        }
                    } else if (shared.startsWith(Constants.USER_TOKEN)) {
                        User user = User.get(Long.valueOf(shared.replaceAll(Constants.USER_TOKEN, '')))
                        if (user) {
                            configurationInstance.addToShareWithUser(user)
                            if(shareWithUsers.find { it.id == user.id }) {
                                userIdsCurrent.add(user.id)
                            }
                            if(isShareReference){
                                addReferencesToUsers(user.id, configurationInstance.id as Long, false)
                            }
                        }
                    } else if(shared == "AUTO_ASSIGN" && !isMasterTemplateAlert){
                        String hierarchy = ""
                        List productAssignment = []
                        if(productMap.product){
                            Map productAssignmentMap = JSON.parse(productMap.product)
                            productAssignmentMap.each{
                                if(it.value){
                                    hierarchy = productAssignmentService.getProductHierarchyWithoutDicMap(it.key as Integer)
                                    productAssignment = it.value
                                }
                            }
                        } else if(productMap.productGroup){
                            hierarchy = "Product Group"
                            productAssignment = JSON.parse(productMap.productGroup)
                        }
                        Long workflowGroup = currentUser?currentUser.workflowGroup?.id:cacheService.getUserByUserId(getCurrentUserId())?.workflowGroup?.id
                        ProductViewAssignment matchedAssignment = ProductViewAssignment."pva".createCriteria().get {
                            eq("workflowGroup",workflowGroup)
                            eq("hierarchy",hierarchy)
                            sqlRestriction("JSON_VALUE(product,'\$.id') = ${productAssignment[0].id as BigInteger}")
                            maxResults(1)
                        }
                        if(!matchedAssignment){
                            matchedAssignment = ProductViewAssignment."pva".createCriteria().get {
                                isNull('workflowGroup')
                                eq("hierarchy",hierarchy)
                                sqlRestriction("JSON_VALUE(product,'\$.id') = ${productAssignment[0].id as BigInteger}")
                                maxResults(1)
                            }
                        }
                        if(matchedAssignment) {
                            matchedAssignment.usersAssigned.each {
                                User user = User.get(it)
                                if (user) {
                                    configurationInstance.addToAutoShareWithUser(user)
                                }
                            }
                            matchedAssignment.groupsAssigned.each {
                                Group group = Group.get(it)
                                if (group) {
                                    configurationInstance.addToAutoShareWithGroup(group)
                                }
                            }
                        } else {
                            autoShareStatus.put("status",false)
                        }
                    }
                }
            }
            if (configurationInstance instanceof ViewInstance) {
                viewInstanceService.handleDefaultViewMappings(userIdsDuplicate, groupIdsDuplicate, userIdsCurrent, groupIdsCurrent, configurationInstance)
            }
        }catch(Exception ex){
            ex.printStackTrace()
        }finally{
            try {
                if (session?.isOpen()) {
                    session.flush()
                }
            } catch (Exception e) {
                log.error("Error flushing session: ${e.message}")
            }
        }

    }
    boolean hasAccessShareWith() {
        if (SpringSecurityUtils.ifAnyGranted("ROLE_SHARE_ALL, ROLE_SHARE_GROUP, ROLE_ADMIN"))
            return true
        return false
    }

    List<String> getUserConfigurations(String alertType, String currentUserName = null) {
        User user = getUser() ?: User.findByUsername(currentUserName)
        List<Long> groupIds = user.groups?.collect { it.id }
        List<String> configs = []
        configs = Configuration.createCriteria().list {
            getUserConfigurations.delegate = delegate
            getUserConfigurations(user, groupIds, alertType?alertType in [Constants.AlertConfigType.SINGLE_CASE_ALERT, Constants.AlertConfigType.AGGREGATE_CASE_ALERT]:true)
            if (alertType)
                eq("type", alertType)
        }
        return configs
    }
    List<String> getUserConfigurationsForCurrentUser(String alertType) {
        User user = getUser()
        List<Long> groupIds = user.groups?.collect { it.id }
        List<String> configs = Configuration.createCriteria().list {
            getUserConfigurationsForCurrentUser.delegate = delegate
            getUserConfigurationsForCurrentUser(user, groupIds, alertType?alertType in [Constants.AlertConfigType.SINGLE_CASE_ALERT, Constants.AlertConfigType.AGGREGATE_CASE_ALERT]:true)
            if (alertType)
                eq("type", alertType)
        }
        return configs
    }

    List<String> getUserLitConfigurations() {
        User user = getUser() ?: User.findByUsername(currentUserName)
        List<Long> groupIds = user.groups?.collect { it.id }
        List<String> configs = []
        configs = LiteratureConfiguration.createCriteria().list {
            getUserConfigurations.delegate = delegate
            getUserConfigurations(user, groupIds)
        }
        return configs
    }

    List<String> getUserEvdasConfigurations() {
        User user = getUser()
        List<Long> groupIds = user.groups?.collect { it.id }
        List<String> configs = []
        configs = EvdasConfiguration.createCriteria().list {
            getUserConfigurations.delegate = delegate
            getUserConfigurations(user, groupIds)
        }
        return configs
    }

    Closure getUserConfigurations = { User user, List<Long> groupIds , Boolean isShare = false->

        createAlias("shareWithUser", "shareWithUser", JoinType.LEFT_OUTER_JOIN)
        createAlias("shareWithGroup", "shareWithGroup", JoinType.LEFT_OUTER_JOIN)
        if(isShare) {
            createAlias("autoShareWithUser", "autoShareWithUser", JoinType.LEFT_OUTER_JOIN)
            createAlias("autoShareWithGroup", "autoShareWithGroup", JoinType.LEFT_OUTER_JOIN)
        }
        projections {
            distinct("name")
        }
        or {
            eq("shareWithUser.id", user.id)
            if(isShare) {
                eq("autoShareWithUser.id", user.id)
            }
            if (groupIds) {
                or {
                    groupIds.collate(1000).each {
                        'in'("shareWithGroup.id", groupIds)
                    }
                }
                if(isShare) {
                    or {
                        groupIds.collate(1000).each {
                            'in'("autoShareWithGroup.id", groupIds)
                        }
                    }
                }
            }
        }
        'eq'('workflowGroup.id', user.workflowGroup.id)
    }

    Closure getUserConfigurationsForCurrentUser = { User user, List<Long> groupIds , Boolean isShare = false->

        createAlias("shareWithUser", "shareWithUser", JoinType.LEFT_OUTER_JOIN)
        createAlias("shareWithGroup", "shareWithGroup", JoinType.LEFT_OUTER_JOIN)
        if(isShare) {
            createAlias("autoShareWithUser", "autoShareWithUser", JoinType.LEFT_OUTER_JOIN)
            createAlias("autoShareWithGroup", "autoShareWithGroup", JoinType.LEFT_OUTER_JOIN)
        }
        projections {
            property("id","id")
        }
        or {
            eq("shareWithUser.id", user.id)
            if(isShare) {
                eq("autoShareWithUser.id", user.id)
            }
            if (groupIds) {
                or {
                    groupIds.collate(1000).each {
                        'in'("shareWithGroup.id", groupIds)
                    }
                }
                if(isShare) {
                    or {
                        groupIds.collate(1000).each {
                            'in'("autoShareWithGroup.id", groupIds)
                        }
                    }
                }
            }
        }
        'eq'('workflowGroup.id', user.workflowGroup.id)
    }

    List<String> getUserAdhocConfigurations() {
        User user = getUser()
        List<Long> groupIds = user.groups?.collect { it.id }
        List<String> configs = []
        configs = AdHocAlert.createCriteria().list {
            createAlias("shareWithUser", "shareWithUser", JoinType.LEFT_OUTER_JOIN)
            createAlias("shareWithGroup", "shareWithGroup", JoinType.LEFT_OUTER_JOIN)
            projections {
                distinct("id")
            }
            or {
                eq("shareWithUser.id", user.id)
                if (groupIds) {
                    or {
                        groupIds.collate(1000).each {
                            'in'("shareWithGroup.id", it)
                        }
                    }
                }
                eq("owner.id", user.id)
            }
        }
        return configs
    }

    List<Long> getShareWithConfigurations() {
        User user = getUser()
        List<Long> groupIds = user.groups?.collect { it.id }
        List sharedConfigs = AdHocAlert.createCriteria().list {
            createAlias("shareWithUser", "shareWithUser", JoinType.LEFT_OUTER_JOIN)
            createAlias("shareWithGroup", "shareWithGroup", JoinType.LEFT_OUTER_JOIN)
            projections {
                distinct("id")
            }
            or {
                eq("shareWithUser.id", user.id)
                if (groupIds) {
                    or {
                        groupIds.collate(1000).each {
                            'in'("shareWithGroup.id", it)
                        }
                    }
                }
            }
        }
        return sharedConfigs
    }

    def setSystemUser(Object object) {
        if (springSecurityService.isLoggedIn()) {
            def user = User.findByUsername(Constants.SYSTEM_USER)
            if (!user) {
                seedDataService.seedSystemUser()
                user = User.findByUsername(Constants.SYSTEM_USER)
            }

            //Set one time only
            if (!object.createdBy) {
                object.createdBy = user.username
            }
            object.modifiedBy = user.username
        }
        return object
    }

    List getUserIdsFromGroups(List groupIds) {
        List userIdsList = []
        if (groupIds) {
            userIdsList = UserGroupMapping.createCriteria().list {
                projections {
                    distinct('user.id')
                }
                groupIds.collate(1000).each { List grpIdsList ->
                    inList('group.id', grpIdsList)
                }
            }
        }
        userIdsList
    }

    void updateUserGroupCountsInBackground(User userInstance, List<Long> prevGroupIdList) {
        notify 'update.user.group.counts', [userInstance: userInstance, prevGroupIdList: prevGroupIdList]
    }

    @Transactional
    void updateUserGroupCounts(User userInstance, List<Long> prevGroupIdList) {
        Long workflowGroupId = userInstance.workflowGroup?.id
        List<Long> removedGroups = prevGroupIdList - userInstance.groups.findAll {
            it.groupType != GroupType.WORKFLOW_GROUP
        }.id

        List<Long> addedGroups = userInstance.groups.findAll {
            it.groupType != GroupType.WORKFLOW_GROUP
        }.id - prevGroupIdList

        if (removedGroups || addedGroups) {
            UserDashboardCounts userDashboardCounts = UserDashboardCounts.get(userInstance.id)
            JsonSlurper jsonSlurper = new JsonSlurper()
            Map userGroupCounts = userDashboardCounts.groupDispCaseCounts ? jsonSlurper.parseText(userDashboardCounts.groupDispCaseCounts) as Map : [:]
            Map groupDueDateCountsMap = userDashboardCounts.groupDueDateCaseCounts ? jsonSlurper.parseText(userDashboardCounts.groupDueDateCaseCounts) as Map : [:]
            Map userPEGroupCounts = userDashboardCounts.groupDispPECounts ? jsonSlurper.parseText(userDashboardCounts.groupDispPECounts) as Map : [:]
            Map groupDueDatePECountsMap = userDashboardCounts.groupDueDatePECounts ? jsonSlurper.parseText(userDashboardCounts.groupDueDatePECounts) as Map : [:]
            removedGroups.each {
                userGroupCounts.remove(it.toString())
                userPEGroupCounts.remove(it.toString())
                groupDueDateCountsMap.remove(it.toString())
                groupDueDatePECountsMap.remove(it.toString())
            }
            updateAddedGroupCounts(addedGroups, workflowGroupId, userGroupCounts, groupDueDateCountsMap, true)
            updateAddedGroupCounts(addedGroups, workflowGroupId, userPEGroupCounts, groupDueDatePECountsMap, false)
            userDashboardCounts.groupDispCaseCounts = userGroupCounts ? new JsonBuilder(userGroupCounts).toPrettyString() : null
            userDashboardCounts.groupDueDateCaseCounts = groupDueDateCountsMap ? new JsonBuilder(groupDueDateCountsMap).toPrettyString() : null
            userDashboardCounts.groupDispPECounts = userPEGroupCounts ? new JsonBuilder(userPEGroupCounts).toPrettyString() : null
            userDashboardCounts.groupDueDatePECounts = groupDueDatePECountsMap ? new JsonBuilder(groupDueDatePECountsMap).toPrettyString() : null
            userDashboardCounts.save()
        }
    }

    void updateUserGroupCountsForWorkflowGroupInBackground(User userInstance) {
        notify 'update.user.group.counts.workflow.group', [userInstance: userInstance]
    }

    void updateAddedGroupCounts(List<Long> addedGroups, Long workflowGroupId, Map userGroupCounts, Map groupDueDateCountsMap, boolean isCaseCounts) {
        if (addedGroups) {
            Sql sql = new Sql(dataSource)
            List<Map> groupDispCountList = []
            List<Map> dueDateGroupCountList = []
            log.info("Generate Counts for newly added groups")
            String dispCountsSql = isCaseCounts ? SignalQueryHelper.singleCaseAlert_dashboard_by_disposition(false, null, addedGroups) :
                                    SignalQueryHelper.aggCaseAlert_dashboard_by_disposition(false, null, addedGroups)
            sql.eachRow(dispCountsSql, []) { row ->
                groupDispCountList.add([dispositionId: row[0], assignedToGroupId: row[1], workflowGroupId: row[2], count: row[3] as Integer])
            }

            String dueDateSql = isCaseCounts ? SignalQueryHelper.singleCaseAlert_dashboard_due_date(false, null, addedGroups) : SignalQueryHelper.aggCaseAlert_dashboard_due_date(false, null, addedGroups)
            sql.eachRow(dueDateSql, []) { row ->
                dueDateGroupCountList.add([due_date: row[0], assignedToGroupId: row[1], workflowGroupId: row[2], count: row[3] as Integer])
            }
            log.info("Counts for newly added groups generated")

            addedGroups.each { groupId ->
                Map dispCountMap = [:]
                groupDispCountList.findAll { it.assignedToGroupId == groupId && it.workflowGroupId == workflowGroupId }.each {
                    dispCountMap.put(it.dispositionId as String, it.count)
                }
                if (dispCountMap) {
                    userGroupCounts.put(groupId.toString(), dispCountMap)
                }

                Map dueDateCountMap = [:]
                dueDateGroupCountList.findAll { it.assignedToGroupId == groupId && it.workflowGroupId == workflowGroupId }.each {
                    dueDateCountMap.put(it.due_date, it.count)
                }
                if (dueDateCountMap) {
                    groupDueDateCountsMap.put(groupId.toString(), dueDateCountMap)
                }
            }
        }
    }

    @Transactional
    void updateUserGroupCountsForWorkflowGroup(User user) {
        DispositionCountDTO dispositionCountDTO = new DispositionCountDTO(user)
        log.info("Generate Dashboard Count Starts For Workflow Group")
        generateDashboardCountsListForUser(dispositionCountDTO)
        log.info("Generate Dashboard Count End For Workflow Group")
        dispositionCountDTO.userDashboardCounts.save()
    }

    void generateDashboardCountsListForUser(DispositionCountDTO dispositionCountDTO) {
        Sql sql = new Sql(dataSource)
        generateDashoardCaseCounts(sql, dispositionCountDTO)
        generateDashboardPECounts(sql, dispositionCountDTO)
    }

    void generateDashoardCaseCounts(Sql sql, DispositionCountDTO dispositionCountDTO) {
        sql.eachRow(SignalQueryHelper.singleCaseAlert_dashboard_by_disposition(true, null, [], dispositionCountDTO.userId), []) { row ->
            dispositionCountDTO.userDispCaseCountList.add([dispositionId: row[0], assignedToId: row[1], workflowGroupId: row[2], count: row[3] as Integer])
        }

        sql.eachRow(SignalQueryHelper.singleCaseAlert_dashboard_by_disposition(false, null, dispositionCountDTO.groupIdList), []) { row ->
            dispositionCountDTO.groupDispCaseCountList.add([dispositionId: row[0], assignedToGroupId: row[1], workflowGroupId: row[2], count: row[3] as Integer])
        }

        sql.eachRow(SignalQueryHelper.singleCaseAlert_dashboard_due_date(true, null, [], dispositionCountDTO.userId), []) { row ->
            dispositionCountDTO.userDueDateCaseCountsList.add([due_date: row[0], assignedToId: row[1], workflowGroupId: row[2], count: row[3] as Integer])
        }

        sql.eachRow(SignalQueryHelper.singleCaseAlert_dashboard_due_date(false, null, dispositionCountDTO.groupIdList), []) { row ->
            dispositionCountDTO.dueDateGroupCaseCountList.add([due_date: row[0], assignedToGroupId: row[1], workflowGroupId: row[2], count: row[3] as Integer])
        }

        dispositionCountDTO.userDispCaseCountList.findAll { it.assignedToId == dispositionCountDTO.userId && it.workflowGroupId == dispositionCountDTO.workflowGroupId }.each {
            dispositionCountDTO.userDispCaseCountsMap.put(it.dispositionId as String, it.count)
        }

        dispositionCountDTO.userDueDateCaseCountsList.findAll { it.assignedToId == dispositionCountDTO.userId && it.workflowGroupId == dispositionCountDTO.workflowGroupId }.each {
            dispositionCountDTO.userDueDateCaseCountsMap.put(it.due_date, it.count)
        }

        dispositionCountDTO.groupIdList.each { id ->
            prepareGroupDispCountsMap(id, dispositionCountDTO.workflowGroupId, dispositionCountDTO.groupDispCaseCountList,dispositionCountDTO.groupDispCaseCountsMap,"dispositionId")
            prepareGroupDispCountsMap(id, dispositionCountDTO.workflowGroupId, dispositionCountDTO.dueDateGroupCaseCountList,dispositionCountDTO.dueDateGroupCaseCountsMap,"due_date")
        }

        dispositionCountDTO.userDashboardCounts.userDispCaseCounts = dispositionCountDTO.userDispCaseCountsMap ? new JsonBuilder(dispositionCountDTO.userDispCaseCountsMap).toPrettyString() : null
        dispositionCountDTO.userDashboardCounts.userDueDateCaseCounts = dispositionCountDTO.userDueDateCaseCountsMap ? new JsonBuilder(dispositionCountDTO.userDueDateCaseCountsMap).toPrettyString() : null
        dispositionCountDTO.userDashboardCounts.groupDispCaseCounts = dispositionCountDTO.groupDispCaseCountsMap ? new JsonBuilder(dispositionCountDTO.groupDispCaseCountsMap).toPrettyString() : null
        dispositionCountDTO.userDashboardCounts.groupDueDateCaseCounts = dispositionCountDTO.dueDateGroupCaseCountsMap ? new JsonBuilder(dispositionCountDTO.dueDateGroupCaseCountsMap).toPrettyString() : null
    }

    void generateDashboardPECounts(Sql sql, DispositionCountDTO dispositionCountDTO) {
        sql.eachRow(SignalQueryHelper.aggCaseAlert_dashboard_by_disposition(true, null, [], dispositionCountDTO.userId), []) { row ->
            dispositionCountDTO.userDispPECountList.add([dispositionId: row[0], assignedToId: row[1], workflowGroupId: row[2], count: row[3] as Integer])
        }

        sql.eachRow(SignalQueryHelper.aggCaseAlert_dashboard_by_disposition(false, null, dispositionCountDTO.groupIdList), []) { row ->
            dispositionCountDTO.groupDispPECountList.add([dispositionId: row[0], assignedToGroupId: row[1], workflowGroupId: row[2], count: row[3] as Integer])
        }

        sql.eachRow(SignalQueryHelper.aggCaseAlert_dashboard_due_date(true, null, [], dispositionCountDTO.userId), []) { row ->
            dispositionCountDTO.userDueDatePECountsList.add([due_date: row[0], assignedToId: row[1], workflowGroupId: row[2], count: row[3] as Integer])
        }

        sql.eachRow(SignalQueryHelper.aggCaseAlert_dashboard_due_date(false, null, dispositionCountDTO.groupIdList), []) { row ->
            dispositionCountDTO.dueDateGroupPECountList.add([due_date: row[0], assignedToGroupId: row[1], workflowGroupId: row[2], count: row[3] as Integer])
        }

        dispositionCountDTO.userDispPECountList.findAll { it.assignedToId == dispositionCountDTO.userId && it.workflowGroupId == dispositionCountDTO.workflowGroupId }.each {
            dispositionCountDTO.userDispPECountsMap.put(it.dispositionId as String, it.count)
        }

        dispositionCountDTO.userDueDatePECountsList.findAll { it.assignedToId == dispositionCountDTO.userId && it.workflowGroupId == dispositionCountDTO.workflowGroupId }.each {
            dispositionCountDTO.userDueDatePECountsMap.put(it.due_date, it.count)
        }

        dispositionCountDTO.groupIdList.each { id ->
            prepareGroupDispCountsMap(id, dispositionCountDTO.workflowGroupId, dispositionCountDTO.groupDispPECountList,dispositionCountDTO.groupDispPECountsMap,"dispositionId")
            prepareGroupDispCountsMap(id, dispositionCountDTO.workflowGroupId, dispositionCountDTO.dueDateGroupPECountList,dispositionCountDTO.dueDateGroupPECountsMap,"due_date")
        }

        dispositionCountDTO.userDashboardCounts.userDispPECounts = dispositionCountDTO.userDispPECountsMap ? new JsonBuilder(dispositionCountDTO.userDispPECountsMap).toPrettyString() : null
        dispositionCountDTO.userDashboardCounts.userDueDatePECounts = dispositionCountDTO.userDueDatePECountsMap ? new JsonBuilder(dispositionCountDTO.userDueDatePECountsMap).toPrettyString() : null
        dispositionCountDTO.userDashboardCounts.groupDispPECounts = dispositionCountDTO.groupDispPECountsMap ? new JsonBuilder(dispositionCountDTO.groupDispPECountsMap).toPrettyString() : null
        dispositionCountDTO.userDashboardCounts.groupDueDatePECounts = dispositionCountDTO.dueDateGroupPECountsMap ? new JsonBuilder(dispositionCountDTO.dueDateGroupPECountsMap).toPrettyString() : null

    }

    void prepareGroupDispCountsMap(Long groupId, Long workflowGroupId, List<Map> countList, Map countsMap, String key) {
        Map dispCountMap = [:]
        countList.findAll { it.assignedToGroupId == groupId && it.workflowGroupId == workflowGroupId }.each {
            dispCountMap.put(it."$key" as String, it.count)
        }
        if (dispCountMap) {
            countsMap.put(groupId.toString(), dispCountMap)
        }
    }

    void saveUserInfoInPvUserWebappTable(User user){
        final Sql sql
        User loggedInUSer = getUser() ?: User.findByFullName("System")
        Date timeStamp = new Date()
        try {
            sql = new Sql(signalDataSourceService.getReportConnection("pva"))
            String sqlStatement = SignalQueryHelper.saveUserInfoInPvUserWebappTable(user.id, user.email, user.fullName, user.username, loggedInUSer.username, timeStamp)
            sql?.call(sqlStatement)
            log.info("User's data successfully saved in PVUSER_WEBAPP table.")
        } catch (Exception ex) {
            log.error("User's data could not be saved in PVUSER_WEBAPP table.")
            ex.printStackTrace()
        } finally {
            sql?.close()
        }
    }

    void updateUserInfoInPvUserWebappTable(User user){
        final Sql sql
        User loggedInUSer = getUser()
        Date timeStamp = new Date()
        try {
            sql = new Sql(signalDataSourceService.getReportConnection("pva"))
            String sqlStatement = SignalQueryHelper.updateUserInfoInPvUserWebappTable(user.id, user.email, user.fullName, user.username, loggedInUSer.username, timeStamp)
            sql?.call(sqlStatement)
            log.info("User's data successfully updated in PVUSER_WEBAPP table.")
        } catch (Exception ex) {
            log.error("User's data could not be updated in PVUSER_WEBAPP table.")
            ex.printStackTrace()
        } finally {
            sql?.close()
        }
    }

    void deleteUserInfoFRomPvUserWebApp(Long id) {
        final Sql sql
        try {
            sql = new Sql(signalDataSourceService.getReportConnection("pva"))
            String sqlStatement = SignalQueryHelper.deleteUserInfoFRomPvUserWebApp(id)
            sql?.execute(sqlStatement)
            log.info("All User's data successfully updated in PVUSER_WEBAPP table.")
        } catch (Exception ex) {
            log.error("All User's data could not be updated in PVUSER_WEBAPP table.")
            ex.printStackTrace()
        } finally {
            sql?.close()
        }
    }

    void migrateAllUsersToPvUserWebappTable() {

        final Sql sql
        List<User> userList = User.list()
        Date timeStamp = new Date()
        try {
            sql = new Sql(signalDataSourceService.getReportConnection("pva"))
            String sqlStatement = SignalQueryHelper.migrateAllUsersToPvUserWebappTable(userList, timeStamp)
            sql?.execute(sqlStatement)
            log.info("All User's data successfully updated in PVUSER_WEBAPP table.")
        } catch (Exception ex) {
            log.error("All User's data could not be updated in PVUSER_WEBAPP table.")
            ex.printStackTrace()
        } finally {
            sql?.close()
        }
    }
    User getUserByToken(TokenAuthenticationCO commandObject) {
        User user = null;
        try {
            if (!StringUtils.isEmpty(commandObject.apiToken)) {
                String decryptText = SecurityUtil.decodeAPIToken(Holders.config.pvr.publicApi.token['API_PUBLIC_TOKEN'] as String, commandObject.apiToken)
                Map<String, String> tokenResult = new JsonSlurper().parseText(decryptText) as Map<String, String>
                user = User.findByUsernameAndEnabled(tokenResult.get("content"), true)
                commandObject.username=user.username
            }

        } catch (Exception ex) {
            log.error("User not found from apiToken")
        }
        return user;

    }

    Boolean authenticateToken(User user, String apiToken) {
        try {
            if (!StringUtils.isEmpty(apiToken)) {
                String decryptText = SecurityUtil.decodeAPIToken(Holders.config.pvr.publicApi.token['API_PUBLIC_TOKEN'] as String, apiToken)
                Map<String, String> tokenResult = new JsonSlurper().parseText(decryptText) as Map<String, String>
                Preference preference = user.preference
                Date currentDate = new Date()
                Long diffTime = currentDate.getTime() - preference?.tokenUpdateDate?.getTime()
                if(user==null) {
                    user = User.findByUsernameAndEnabled(tokenResult.get("content"), true)
                }
                if (user.username == tokenResult.content && preference.apiToken == apiToken
                        && (Holders.config.signal.batchSignal.api.token.expiration == -1 || diffTime < Holders.config.signal.batchSignal.api.token.expiration) ) {
                    return true
                } else {
                    return false
                }
            }
        }catch(Exception ex){
            return false
        }
    }

    def updateApiToken(String username, String apiToken) {
        Session session = sessionFactory.currentSession
        try {
            String updateApiTokenQuery = "update preference set api_token = :apiToken , token_update_date = CURRENT_TIMESTAMP " +
                    "where id= (select preference_id from pvuser where username= :username) "
            SQLQuery sqlQuery = session.createSQLQuery(updateApiTokenQuery)
            sqlQuery.setParameter("apiToken", apiToken)
            sqlQuery.setParameter("username", username)
            sqlQuery.executeUpdate()
        } finally {
            session.flush()
            session.clear()
        }

    }

    void saveUserRolesString(List<User> userList) {
        userList.each {
            it.userRolesString = getUserRoles(it)
            it.save(flush: true)
        }
    }

    boolean hasNormalAlertExecutionAccess(String alertType){
        switch(alertType){
            case Constants.AlertConfigType.AGGREGATE_CASE_ALERT:
                   SpringSecurityUtils.ifAllGranted("ROLE_AGGREGATE_CASE_CONFIGURATION, ROLE_DATA_MINING")
                break
            case Constants.AlertConfigType.AGGREGATE_CASE_ALERT_FAERS:
                SpringSecurityUtils.ifAllGranted("ROLE_FAERS_CONFIGURATION, ROLE_DATA_MINING")
                break
            case Constants.AlertConfigType.AGGREGATE_CASE_ALERT_VAERS:
                SpringSecurityUtils.ifAllGranted("ROLE_VAERS_CONFIGURATION, ROLE_DATA_MINING")
                break
            case Constants.AlertConfigType.AGGREGATE_CASE_ALERT_VIGIBASE:
                SpringSecurityUtils.ifAllGranted("ROLE_VIGIBASE_CONFIGURATION, ROLE_DATA_MINING")
                break
            case Constants.AlertConfigType.SINGLE_CASE_ALERT:
                SpringSecurityUtils.ifAllGranted("ROLE_SINGLE_CASE_CONFIGURATION, ROLE_DATA_MINING")
                break
            case Constants.AlertConfigType.EVDAS_ALERT:
                SpringSecurityUtils.ifAllGranted("ROLE_EVDAS_CASE_CONFIGURATION, ROLE_DATA_MINING")
                break
            case Constants.AlertConfigType.AGGREGATE_CASE_ALERT_JADER:
                SpringSecurityUtils.ifAllGranted("ROLE_JADER_CONFIGURATION, ROLE_DATA_MINING")
                break

            default:
                false
        }

    }
    void syncPvsUsernameWithLdap(List<User> userList = []){
        try{
            List<User> activeUsers = []
            if(userList){
                activeUsers = userList
            } else {
                activeUsers = User.findAllByEnabled(true)
            }
            activeUsers.each{
                String username = it.username
                String uid = Holders.config.grails.plugin.springsecurity.ldap.uid.attribute
                List<LdapCommand> ldapEntry = ldapService.getLdapEntry("$uid=$username")
                if(ldapEntry.size()==1){
                    if(ldapEntry[0].userName != username){
                        it.username = ldapEntry[0].userName
                        it.save(flush:true, failOnError:true)
                        log.info("Username updated from ${username} to ${it.username}.")
                    }
                } else {
                    log.error("Either there are multiple entries in LDAP for the username: ${username} or no entry is present.")
                }
            }
        } catch(Exception ex){
            log.error("Error occurred while syncing usernames from LDAP", ex.printStackTrace())
        }
    }
    User changePassword(User user, String newPassword) {
        user.password = springSecurityService.encodePassword(newPassword)
        user.accountLocked = false
        user.passwordModifiedTime = new Date()
        user.passwordDigests.clear()
        user.addToPasswordDigests(digestPassword(newPassword))
        user
    }
    String digestPassword(String password) {
        DigestUtils.md5Hex(password)
    }

    def getUserRoles(User user){
        return UserRole.findAllByUser(user)?.role?.toString()
    }

    def getUserPinConfigs(User user){
        return UserPinConfiguration.findAllByUser(user)
    }

    Map groupLists(List<Long> userIds) {
        Sql sql
        Map result
        try{
            String whereClause = ""
            userIds.collate(999).each{
                if(whereClause == ""){
                    whereClause += "WHERE User_ID IN (${it.join(',')})"
                }
                else{
                    whereClause += " OR User_ID IN  (${it.join(',')})"
                }
            }
        sql = new Sql(dataSource)
        String sqlQuery = """SELECT 
    u.User_ID,
    ug.User_Groups AS User_Groups,
    wg.Workflow_Groups AS Workflow_Groups
FROM 
    (
        SELECT 
            User_ID
        FROM 
            User_Group_mapping """+whereClause+ """ GROUP BY 
            User_ID
    ) u
LEFT JOIN 
(
        SELECT 
            User_ID,
            STRING_AGG(Name, '# ' ORDER BY Name) AS User_Groups
        FROM 
            User_Group_mapping ugm
        JOIN 
            Groups g ON ugm.Group_ID = g.ID
        WHERE 
            g.Group_type = 'USER_GROUP'
        GROUP BY 
            User_ID
    ) ug ON u.User_ID = ug.User_ID
LEFT JOIN 
(
        SELECT 
            User_ID,
            STRING_AGG(Name, '# ' ORDER BY Name) AS Workflow_Groups
        FROM 
            User_Group_mapping ugm
        JOIN 
            Groups g ON ugm.Group_ID = g.ID
        WHERE 
            g.Group_type = 'WORKFLOW_GROUP'
        GROUP BY 
            User_ID
    ) wg ON u.User_ID = wg.User_ID
"""

        result = sql.rows(sqlQuery).collectEntries { row ->
            [(row.User_ID as Long): [userGroups: row.User_Groups?.split('#').collect {
                it?.trim()
            }, workFlowGroup                   : row.Workflow_Groups?.split('#').collect { it?.trim() }]]
        }


        } catch (Exception ex){
            ex.printStackTrace()
        } finally {
            sql?.close()
        }
        return result
    }

    /**
     * Fetches the combined fieldMapping data for all the groups
     * @param groupIds - List of the group ids whose fields are required to be fetched
     * @return - A map containing the blinded, redacted and available fields
     *           list will be returned
     */
    Map getBlindingFieldsForGroup(List<Long> groupIds) {
        Set<String> blindedFieldsSet = new HashSet<>()
        Set<String> redactedFieldsSet = new HashSet<>()
        Set<String> availableFieldsSet = new HashSet<>()
        int isAllFieldsRedacted = 0
        boolean isUserMappingAvailable = false

        groupIds.each { groupId ->
            UserGroupFieldMapping fieldMapping = UserGroupFieldMapping.findByGroupId(groupId)
            if (fieldMapping) {
                isUserMappingAvailable = true
                addFieldsToSet(fieldMapping.blindedFields, blindedFieldsSet)
                addFieldsToSet(fieldMapping.redactedFields, redactedFieldsSet)
                if (fieldMapping.availableFields) {
                    addFieldsToSet(fieldMapping.availableFields, availableFieldsSet)
                }
            }
        }
        if (isUserMappingAvailable) {
            isAllFieldsRedacted = availableFieldsSet ? 0 : 1
        }
        return [
                blindedFields      : blindedFieldsSet.toList(),
                redactedFields     : redactedFieldsSet.toList(),
                availableFields    : availableFieldsSet.toList(),
                isAllFieldsRedacted: isAllFieldsRedacted,
                isUserMappingAvailable : isUserMappingAvailable
        ]
    }

    /**
     * Transforms a string having values as comma separated into a Set of the string values
     * @param fields - String value which is having comma separated fields
     * @param set - Set which is containing each string value of 'fields'
     */
    void addFieldsToSet(String fields, Set<String> set) {
        if (fields) {
            fields.split(",").each { set.add(it.trim()) }
        }
    }

    /**
     * Provides the blinded, available and redacted field list for a user according
     * to the associated groups of the user
     * @param groupBlindingFields - the combined fieldMapping data for all the groups
     * @param isRptFieldNeeded - flag to provide the field list with rpt field
     * @return - status of all the fields mapping of the user will be returned
     */
    Map getBlindingFieldsForUser(Map groupBlindingFields, boolean isRptFieldNeeded = false) {
        Map finalStatusForFields = [:]
        List availableFields = groupBlindingFields.availableFields
        Map uniqueFieldToRpt = cacheService.getCacheUniqueFieldId()

        availableFields.each { field ->
            if (field) {
                String uniqueField = isRptFieldNeeded ? uniqueFieldToRpt.get(field) : field
                if (groupBlindingFields.redactedFields?.contains(field)) {
                    finalStatusForFields[uniqueField] = Constants.BlindingStatus.REDACTED
                } else if (groupBlindingFields.blindedFields?.contains(field)) {
                    finalStatusForFields[uniqueField] = Constants.BlindingStatus.BLINDED
                } else {
                    finalStatusForFields[uniqueField] = Constants.BlindingStatus.AVAILABLE
                }
            }
        }

        return finalStatusForFields
    }

    /**
     * Transforms the blinded, available and redacted field data for SQL query
     * @param blindingFieldsForUser - list of the fields of the user
     * @return - map with blinded, available and redacted fields,
     *           formatted for SQL query will return
     */
    Map getBlindedDataForSQL(Map blindingFieldsForUser) {
        List<String> blindedFields = []
        List<String> redactedFields = []
        List<String> availableFields = []

        blindingFieldsForUser.each { field, status ->
            String quotedField = "'${field}'"
            if (status == Constants.BlindingStatus.BLINDED) {
                blindedFields.add(quotedField)
            } else if (status == Constants.BlindingStatus.REDACTED) {
                redactedFields.add(quotedField)
            } else {
                availableFields.add(quotedField)
            }
        }

        return [
                blindedData  : blindedFields ? blindedFields.join(', ').trim() : "''",
                redactedData : redactedFields ? redactedFields.join(', ').trim() : "''",
                availableData: availableFields ? availableFields.join(', ').trim() : "''"
        ]
    }

    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User.withTransaction { status ->
            User user = User.findByUsernameIlike(username)
            def authorities = user.authorities.collect {
                new SimpleGrantedAuthority(it.authority)
            }
            return new CustomUserDetails(user.username, user.password ?: '', user.enabled,
                    !user.accountExpired, !user.passwordExpired,
                    !user.accountLocked, authorities ?: [new SimpleGrantedAuthority(SpringSecurityUtils.NO_ROLE)], user.id,
                    user.fullName, user.email,user.type, user.authType, user.passwordModifiedTime ?: user.dateCreated)
        }

    }

    void setUserContext(){
        UserDetails userDetails = loadUserByUsername(Holders.config.username.for.ui.api)
        def authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
        SecurityContextHolder.context.authentication = authentication
    }

}
