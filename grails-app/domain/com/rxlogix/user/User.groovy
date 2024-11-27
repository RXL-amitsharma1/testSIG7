package com.rxlogix.user

import com.rxlogix.Constants
import com.rxlogix.SignalAuditLogService
import com.rxlogix.SignalDataSourceService
import com.rxlogix.UserService
import com.rxlogix.config.DefaultViewMapping
import com.rxlogix.config.ReportTemplate
import com.rxlogix.config.SafetyGroup
import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.enums.AuthType
import com.rxlogix.enums.GroupType
import com.rxlogix.enums.UserType
import com.rxlogix.signal.ViewInstance
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.SignalQueryHelper
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugins.orm.auditable.ChildModuleAudit
import org.apache.commons.logging.LogFactory


class User implements Serializable {
    static auditable = [ignore:['passwordDigests','colUserOrder','colOrder','version', 'lastUpdated', 'lastUpdatedBy', 'dateCreated', 'createdBy',
                                'modifiedBy','badPasswordAttempts','userPinConfigs','signalPinConfigs','lastToLastLogin','lastLogin','preference','type','groups','scimId',
                                'outlookAccessToken','outlookRefreshToken']]

    String username
    String password = ""
    boolean enabled = true
    boolean accountExpired
    boolean accountLocked
    boolean passwordExpired
    Preference preference = new Preference()
    String fullName
    String email
    String userRolesString
    String userPinConfigs
    String signalPinConfigs

    Date lastToLastLogin
    Date lastLogin
    String scimId

    Integer badPasswordAttempts = 0

    //Standard fields
    Date dateCreated = new Date()
    Date lastUpdated = new Date()
    String createdBy
    String modifiedBy
    Set<Group> groups

    String outlookAccessToken
    String outlookRefreshToken

    String colOrder
    String colUserOrder

    UserType type = UserType.LDAP
    Date passwordModifiedTime
    Set<String> passwordDigests = new HashSet<>()
    AuthType authType = null
    static hasMany = [reportTemplates: ReportTemplate, groups: Group, safetyGroups: SafetyGroup, userDepartments: UserDepartment, passwordDigests:String]
    static mappedBy = [groups: "members", safetyGroups: "members"]

    static transients = ['workflowGroup','authType']

    @SuppressWarnings("GroovyAssignabilityCheck")
    static mapping = {
        preference lazy: false
        table name: "PVUSER"

        username column: "USERNAME"
        enabled column: "ENABLED"
        accountExpired column: "ACCOUNT_EXPIRED"
        accountLocked column: "ACCOUNT_LOCKED"
        passwordExpired column: "PASSWORD_EXPIRED"
        preference column: "PREFERENCE_ID"
        badPasswordAttempts column: "BAD_PASSWORD_ATTEMPTS"
        outlookAccessToken column: 'OUTLOOK_ACCESS_TOKEN', maxSize: 2000
        outlookRefreshToken column: 'OUTLOOK_REFRESH_TOKEN',maxSize: 2000
        scimId column: 'SCIM_ID'
        colOrder column: 'COL_ORDER',maxSize: 2000
        colUserOrder column: 'COL_USER_ORDER',maxSize: 2000
        groups joinTable: "user_group_s"
        sort "fullName"
        type column: 'USER_TYPE'
        lastToLastLogin column: 'LAST_TO_LAST_LOGIN'
        lastLogin column: 'LAST_LOGIN'
    }

    static constraints = {
        username blank: false, unique: true, maxSize: 100
        fullName nullable: true
        email nullable: true
        createdBy(nullable: false, maxSize: 100)
        modifiedBy(nullable: false, maxSize: 100)
        outlookAccessToken(nullable: true)
        outlookRefreshToken(nullable: true)
        colOrder(nullable: true)
        userRolesString(nullable: true)
        userPinConfigs(nullable: true)
        signalPinConfigs(nullable: true)
        colUserOrder(nullable: true)
        scimId(nullable: true,unique: true)
        password nullable: true, validator: { val, obj ->
            if (obj.type.equals(UserType.NON_LDAP)) {
                if (!val) {
                    return false
                }
            }
            return true
        }
        passwordModifiedTime nullable: true
        lastToLastLogin nullable: true
        lastLogin nullable: true

    }

    Set<Role> getAuthorities( Boolean showAll = null ) {
        Set<Role> roles = UserRole.findAllByUser( this ).collect { it.role } as Set
        if( showAll == null || showAll != false ) {
            List userGroupMappingList = UserGroupMapping.findAllByUser( this ).collect { it.group.id }
            userGroupMappingList?.each {
                roles = roles + UserGroupRole.findAllByUserGroup( Group.get( it ) )?.collect { it?.role }
            }
        }
        roles?.sort { a, b -> a.authorityDisplay?.toLowerCase() <=> b.authorityDisplay?.toLowerCase() }
    }

    Set<Role> getEnabledAuthorities( Boolean showAll = null ) {
        Set<Role> roles = UserRole.findAllByUser( this ).collect { it.role } as Set
        if( showAll == null || showAll != false ) {
            List userGroupMappingList = UserGroupMapping.findAllByUser( this ).collect { it.group.id }
            userGroupMappingList?.each {
                roles = roles + UserGroupRole.findAllByUserGroup( Group.get( it ) )?.collect { it?.role }
            }
        }
        roles?.sort { a, b -> a.authority?.toLowerCase() <=> b.authority?.toLowerCase() }
    }

    boolean isAdmin() {
        return SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN")
    }

    boolean isHealthStatusReviewer() {
        return SpringSecurityUtils.ifAnyGranted("ROLE_HEALTH_CONFIGURATION")
    }

    boolean isAggregateReviewer() {
        return SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_CONFIGURATION, ROLE_AGGREGATE_CASE_REVIEWER, ROLE_AGGREGATE_CASE_VIEWER, ROLE_VIEW_ALL, ROLE_FAERS_CONFIGURATION, ROLE_VAERS_CONFIGURATION, ROLE_VIGIBASE_CONFIGURATION,ROLE_JADER_CONFIGURATION")
    }

    boolean isSingleReviewer() {
        return SpringSecurityUtils.ifAnyGranted("ROLE_SINGLE_CASE_CONFIGURATION, ROLE_SINGLE_CASE_REVIEWER, ROLE_SINGLE_CASE_VIEWER, ROLE_VIEW_ALL")
    }

    boolean isEvdasReviewer() {
        return SpringSecurityUtils.ifAnyGranted("ROLE_EVDAS_CASE_CONFIGURATION, ROLE_EVDAS_CASE_REVIEWER, ROLE_EVDAS_CASE_VIEWER, ROLE_VIEW_ALL")
    }

    boolean isLiteratureReviewer() {
        return SpringSecurityUtils.ifAnyGranted("ROLE_LITERATURE_CASE_CONFIGURATION, ROLE_LITERATURE_CASE_REVIEWER, ROLE_LITERATURE_CASE_VIEWER, ROLE_VIEW_ALL")
    }

    boolean isSignalManagement() {
        return SpringSecurityUtils.ifAnyGranted("ROLE_SIGNAL_MANAGEMENT_CONFIGURATION, ROLE_SIGNAL_MANAGEMENT_REVIEWER, ROLE_SIGNAL_MANAGEMENT_VIEWER, ROLE_VIEW_ALL")
    }

    boolean isAdhocEvaluator() {
        return SpringSecurityUtils.ifAnyGranted("ROLE_AD_HOC_CRUD, ROLE_VIEW_ALL")
    }


    String getFullNameAndUserName () {
        return getFullName()+" ("+username+")"
    }

    private getRolesToBeAdded(userInstance, Map params) {
        def rolesToBeAdded = []

        User.withNewSession {
            for (String key in params.keySet()) {
                if (Role.findByAuthority(key) && 'on' == params.get(key)) {
                    rolesToBeAdded << Role.findByAuthority(key)
                }
            }

            def existingRoles = []
            if (userInstance?.id) {
                existingRoles = userInstance.getAuthorities()
            }

            rolesToBeAdded.removeAll(existingRoles)
        }
        rolesToBeAdded
    }

    private getRolesToBeRemoved(userInstance, Map params) {
        def rolesToBeRemoved = []
        def rolesToBeAdded = []

        User.withNewSession {
            for (String key in params.keySet()) {
                if (Role.findByAuthority(key) && 'on' == params.get(key)) {
                    rolesToBeAdded << Role.findByAuthority(key)
                }
            }

            def existingRoles = []
            if (userInstance?.id) {
                existingRoles = userInstance.getAuthorities()
            }

            for (Role role : existingRoles) {
                if (!rolesToBeAdded.contains(role)) {
                    rolesToBeRemoved.add(role)
                }
            }
        }

        rolesToBeRemoved
    }

    def getInstanceIdentifierForAuditLog() {
        return getValue()
    }

    def getValue() {
        fullName ?: username
    }

    @Override
    boolean equals(def obj) {
        if (obj instanceof User) {
             this.username?.toLowerCase() == (obj as User).getUsername()?.toLowerCase()
        } else {
            false
        }
    }

    @Override
    String toString() {
        getValue()
    }

    Map toMap() {
        [
                id: this.id,
                fullName: this.fullName,
                username: this.username,
                email: this.email
        ]
    }

    Map getAssignedToMap() {
        [
                id  : this.id,
                name: this.fullName
        ]
    }

    String getName() {
        this.fullName ?: this.username
    }

    Group getWorkflowGroup() {
        return allGroups(GroupType.WORKFLOW_GROUP)?.getAt(0)
    }

    Set<Group> getGroups() {
        return allGroups(GroupType.USER_GROUP)
    }

    Set<Group> allGroups(GroupType groupType) {
        // Collect all group associated with this object
        Set<Group> userGroups = this.groups != null ? new HashSet<>(this.groups) : new HashSet<>();

        // Find all group from UserGroupMapping for this user
        def userGroupMappings = this.id ? UserGroupMapping.findAllByUser(this) : []
        if (userGroupMappings != null && userGroupMappings != []) {
            userGroups.addAll(userGroupMappings.collect { it.group })
        }

        // Filter groups based on the group type if required
        if (groupType == GroupType.WORKFLOW_GROUP) {
            userGroups = userGroups.findAll { it.groupType != GroupType.USER_GROUP }
        }

        return userGroups
    }

    Long getDefaultViewId(String alertType){
        Long defaultViewId = null
        if(alertType){
            defaultViewId = DefaultViewMapping.findByAlertTypeAndUser(alertType, this)?.defaultViewInstanceId
        }
        return defaultViewId
    }

    Void deleteDefaultViewMapping(ViewInstance viewInstance){
        if(viewInstance){
            DefaultViewMapping.findByDefaultViewInstanceAndUser(viewInstance, this)?.delete(flush: true)
        }
    }


}
