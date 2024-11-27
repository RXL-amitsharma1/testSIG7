package com.rxlogix

import com.rxlogix.config.Configuration
import com.rxlogix.enums.GroupType
import com.rxlogix.user.Group
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserGroupMapping
import com.rxlogix.user.UserGroupRole
import grails.gorm.transactions.Transactional
import groovy.sql.Sql

@Transactional
class UserGroupService {

    def dataSource

    void createUserGroupMappingsForUser(User user, List groupIds) {
        try {
            groupIds.each { Long id ->
                new UserGroupMapping(user: user, group: Group.load(id))?.save(failOnError: true, flush: true)
            }
        } catch (Exception e) {
            log.error("Some error is occurred while creating user group mappings", e)
        }

    }

    void deleteUserGroupMappingsForUser(User user) {
        try {
            UserGroupMapping.findAllByUser(user)?.each {
                it.delete(failOnError: true, flush: true)
            }
        } catch (Exception e) {
            log.error("Some error is occurred while deleting user group mappings", e)
        }
    }

    List<User> fetchUserListForGroup(Group group) {
            UserGroupMapping.findAllByGroup(group).collect {
                it.user
            }.findAll { it.enabled }.sort { a, b -> a.fullName?.toLowerCase() <=> b.fullName?.toLowerCase() }

    }

    List<Role> fetchRoleListForGroup( Group group) {
        UserGroupRole.findAllByUserGroup(group).collect { it.role.authority }
    }

    List<Group> fetchUserListForGroup(User user,GroupType groupType) {
        UserGroupMapping.withTransaction {

            UserGroupMapping.findAllByUser(user)?.findAll { it.group?.groupType == groupType }?.collect {
                it.group
            }?.sort { a, b -> a.name?.toLowerCase() <=> b.name?.toLowerCase() }
        }

    }


    List<Long> fetchUserListIdForGroup(Group group) {
        UserGroupMapping.findAllByGroup(group).collect { it.user }.findAll { it.enabled }.collect { it.id }
    }

    List<String> fetchUserEmailsForGroup(Group group) {
        fetchUserListForGroup(group).collect { it.email }
    }

    def assignGroupOrAssignTo(String assignedTo, Configuration configuration, def domain = '') {
        if (assignedTo) {
            if (assignedTo.startsWith(Constants.USER_GROUP_TOKEN)) {
                configuration.assignedToGroup = Group.get(Long.valueOf(assignedTo.replaceAll(Constants.USER_GROUP_TOKEN, '')))
                configuration.assignedTo = null
            } else if (assignedTo.startsWith(Constants.USER_TOKEN)) {
                configuration.assignedTo = User.get(Long.valueOf(assignedTo.replaceAll(Constants.USER_TOKEN, '')))
                configuration.assignedToGroup = null
            }
        }
        configuration
    }

    Boolean checkAssignmentGroupType(String assignedToValue) {
        Boolean isAssignmentTypeGroup = false
        if (assignedToValue.startsWith(Constants.USER_GROUP_TOKEN)) {
            isAssignmentTypeGroup = true
        }
        isAssignmentTypeGroup
    }

    Map fetchUserGroupMap(String assignedToValue) {
        Map userGroupMap = [:]
        if (assignedToValue) {
            if (assignedToValue.startsWith(Constants.USER_GROUP_TOKEN)) {
                userGroupMap.group = Group.get(Long.valueOf(assignedToValue.replaceAll(Constants.USER_GROUP_TOKEN, '')))
            } else if (assignedToValue.startsWith(Constants.USER_TOKEN)) {
                userGroupMap.user = User.get(Long.valueOf(assignedToValue.replaceAll(Constants.USER_TOKEN, '')))
            }
        }
        userGroupMap
    }

    List<User> getRecipientsList(def domain) {
        domain?.assignedTo ? [domain?.assignedTo] : fetchUserListForGroup(domain?.assignedToGroup)
    }

    List<User> getRecipientsListForUserGroupMap(Map userGroupMap) {
        userGroupMap.user ? [userGroupMap.user] : fetchUserListForGroup(userGroupMap.group)
    }

    void updateGroupDefaultParameter() {
        log.info( "updateGroupDefaultParameter execution started." )
        def sql = new Sql( dataSource)
        try {
            sql.execute( "update GROUPS set IS_DEFAULT=1 where NAME IN ('Default')" )
        } catch ( Exception exception ) {
            exception.printStackTrace()
        } finally {
            sql?.close()
            log.info( "updateGroupDefaultParameter execution finished." )
        }
    }




}
