package com.rxlogix

import com.rxlogix.commandObjects.GroupCO
import com.rxlogix.config.Disposition
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.UserGroupFieldMapping
import com.rxlogix.user.Group
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserGroupMapping
import com.rxlogix.user.UserGroupRole
import com.rxlogix.util.SignalQueryHelper
import com.rxlogix.util.ViewHelper
import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import org.hibernate.criterion.CriteriaSpecification
import javax.servlet.http.HttpSession
import javax.servlet.http.HttpServletRequest

@Transactional
class GroupService {
    def CRUDService
    def signalDataSourceService
    def userService
    def dataSource
    def dataSource_pva
    def utilService
    def cacheService
    def userGroupService

    List<Map> getDispositionList(Boolean validatedConfirmed) {
        Disposition.withCriteria {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("id", "id")
                property("displayName", "displayName")
            }
            'eq'("validatedConfirmed", validatedConfirmed)
        } as List<Map>
    }

    Group saveGroupObject(GroupCO groupCO, Group groupInstance) {
        groupInstance.name = groupCO.name
        groupInstance.description = groupCO.description
        groupInstance.groupType = groupCO.groupType
        if (groupCO.groupType == GroupType.WORKFLOW_GROUP) {
            groupInstance.justificationText = groupCO.justificationText
            groupInstance.forceJustification = groupCO.forceJustification
            groupInstance.defaultAdhocDisposition = groupCO.defaultAdhocDisposition
            groupInstance.defaultEvdasDisposition = groupCO.defaultEvdasDisposition
            groupInstance.defaultLitDisposition = groupCO.defaultLitDisposition
            groupInstance.defaultQuantDisposition = groupCO.defaultQuantDisposition
            groupInstance.defaultQualiDisposition = groupCO.defaultQualiDisposition
            groupInstance.defaultSignalDisposition = groupCO.defaultSignalDisposition
            groupInstance.autoRouteDisposition = groupCO.autoRouteDisposition
            groupCO.alertLevelDispositions.each {
                groupInstance.addToAlertDispositions(Disposition.get(it))
            }
        } else {
            groupInstance.justificationText = null
            groupInstance.forceJustification = null
            groupInstance.defaultAdhocDisposition = null
            groupInstance.defaultEvdasDisposition = null
            groupInstance.defaultLitDisposition = null
            groupInstance.defaultQuantDisposition = null
            groupInstance.defaultQualiDisposition = null
            groupInstance.defaultSignalDisposition = null
            groupInstance.autoRouteDisposition = null
            groupInstance.alertDispositions?.clear()
        }
        CRUDService.save(groupInstance)
        return groupInstance
    }

    void saveGroupInfoInGroupsWebappTable(Group group) {
        final Sql sql
        User loggedInUSer = userService.getUser() ?: User.findByFullName("System")
        Date timeStamp = new Date()
        try {
            /**
             * Commented code due to creating new connection not taking from pool
            sql = new Sql(signalDataSourceService.getReportConnection("pva"))
             */
            sql = new Sql(dataSource_pva)
            String sqlStatement = SignalQueryHelper.saveGroupInfoInGroupsWebappTable(group.id, group.name, group.description, group.groupType.value, loggedInUSer.username, group.isActive, timeStamp)
            sql?.call(sqlStatement)
            log.info("Group's data successfully saved in GROUPS_WEBAPP table.")
        } catch (Exception ex) {
            log.error("Group's data could not be saved in GROUPS_WEBAPP table.")
            ex.printStackTrace()
        } finally {
            sql?.close()
        }
    }


    void saveUsersInUserGroup( Long groupId, List users, String fromMethod=null ) {
        /**
         * Commented code due to creating new connection not taking from pool
        def sql= new Sql(signalDataSourceService.getReportConnection("dataSource"))
         */
        Sql sql = new Sql(dataSource)
        Group userGroup = Group.findById( groupId, [null: null] )
        String insertUserQuery='DO $$ '
        List<UserGroupMapping> oldUsersMapping = UserGroupMapping.findAllByGroup( userGroup )
        List oldUserIdList = oldUsersMapping?.collect { it.user.id }
        if( !fromMethod ) {
            UserGroupMapping.deleteAll( oldUsersMapping )
        }
        List<UserGroupMapping> userGroupMappings = [ ]
        insertUserQuery +="Begin execute ('DELETE FROM USER_GROUP_S WHERE GROUP_ID=${groupId}');"
        oldUserIdList?.each {
            insertUserQuery +=" execute ('DELETE FROM USER_GROUP_S WHERE USER_ID=${it} and GROUP_ID=${groupId}'); "+"\n"
        }
        for( int i = 0; i < users?.size(); i++ ) {
            User user = User.get( users.get( i ) )
            if( userGroup && user ) {
                if (!oldUserIdList.contains(users.get(i))) {
                    UserGroupMapping userGroupMapping = new UserGroupMapping()
                    userGroupMapping.group = userGroup
                    userGroupMapping.user = user
                    userGroupMappings.add(userGroupMapping)
                }
                if(userGroup.groupType==GroupType.WORKFLOW_GROUP)
                {
                    insertUserQuery +=" execute ('DELETE FROM USER_GROUP_S WHERE USER_ID=${user.id}'); "+"\n"

                }
                insertUserQuery += " INSERT INTO USER_GROUP_S(USER_ID,GROUP_ID)VALUES(${user.id},${groupId}); "+"\n"
            }
        }
        UserGroupMapping.saveAll( userGroupMappings )
        try {
            insertUserQuery += 'END $$;'
            sql.execute( insertUserQuery  )
        } catch( Exception exception ) {
            exception.printStackTrace()
        } finally {
            sql?.close()
            log.info( "insertUserQuery execution finished." )
        }

    }


    void saveRolesInUserGroup( Long groupId, List roles ) {
        Group userGroup = Group.findById( groupId, [null: null] )
        List tempRoles = [ ]
        UserGroupRole.deleteAll( UserGroupRole.findAllByUserGroup( userGroup ) )
        List<UserGroupRole> userGroupRoles = [ ]
        for( int i = 0; i < roles?.size(); i++ ) {
            Role role = Role.findByAuthority( roles.get( i ) )
            if( userGroup && role) {
                UserGroupRole userGroupRole = new UserGroupRole()
                userGroupRole.userGroup = userGroup
                userGroupRole.role = role
                userGroupRoles.add( userGroupRole )
            }
        }
        UserGroupRole.saveAll( userGroupRoles )
        roles.each {
            tempRoles.add( ViewHelper.getMessage( "app.role." + it.toString() ) )
        }
        if( userGroup ) {
            userGroup.groupRoles = tempRoles.toString()
            userGroup.save()
        }

    }


    void updateGroupInfoInGroupsWebappTable(Group group) {
        final Sql sql
        User loggedInUSer = userService.getUser()
        Date timeStamp = new Date()
        try {
            /**
             * Commented code due to creating new connection not taking from pool
            sql = new Sql(signalDataSourceService.getReportConnection("pva"))
             */
            sql = new Sql(dataSource_pva)
            String sqlStatement = SignalQueryHelper.updateGroupInfoInGroupsWebappTable(group.id, group.name, group.description, group.groupType.value, loggedInUSer.username, group.isActive, timeStamp)
            sql?.call(sqlStatement)
            log.info("Group's data successfully updated in GROUPS_WEBAPP table.")
        } catch (Exception ex) {
            log.error("Group's data could not be updated in GROUPS_WEBAPP table.")
            ex.printStackTrace()
        } finally {
            sql?.close()
        }
    }

    void deleteGroupInfoFromGroupsWebappTable(Long id) {
        final Sql sql
        try {
            /**
             * Commented code due to creating new connection not taking from pool
            sql = new Sql(signalDataSourceService.getReportConnection("pva"))
             */
            sql = new Sql(dataSource_pva)
            String sqlStatement = SignalQueryHelper.deleteGroupInfoFRomGroupsWebApp(id)
            sql?.call(sqlStatement)
            log.info("Group's data successfully deleted from GROUPS_WEBAPP table.")
        } catch (Exception ex) {
            log.error("Group's data could not be deleted from GROUPS_WEBAPP table.")
            ex.printStackTrace()
        } finally {
            sql?.close()
        }
    }

    void migrateAllGroupsToGroupsWebappTable() {
        final Sql sql
        List<Group> groupList = Group.list()
        Date timeStamp = new Date()
        try {
            /**
             * Commented code due to creating new connection not taking from pool
            sql = new Sql(signalDataSourceService.getReportConnection("pva"))
             */
            sql = new Sql(dataSource_pva)
            String sqlStatement = SignalQueryHelper.migrateAllGroupsToGroupsWebappTable(groupList, timeStamp)
            sql?.call(sqlStatement)
            log.info("All Group's data successfully saved in GROUPS_WEBAPP table.")
        } catch (Exception ex) {
            log.error("All Group's data could not be updated in GROUPS_WEBAPP table.")
            ex.printStackTrace()
        } finally {
            sql?.close()
        }
    }


    void updateMenuVisibilityInSession(HttpServletRequest request){
        boolean hideMenu = utilService.fetchUserListForGroup(userService.getUser(), GroupType.WORKFLOW_GROUP)?.size() > 0 ? false : true
        HttpSession session = request.getSession()
        session.setAttribute("hideMenu",hideMenu)
    }

    void updateGroupUsersString(Group group = null) {
        group.markDirty('groupUsers')
        try{
            if (group) {
                def currentGroupUsers = UserGroupMapping.findAllByGroup(group)*.user.sort { it.fullName }
                group.groupUsers = currentGroupUsers.toString()
                group.save(flush: true)
            } else {
                Group.list().each { grp ->
                    def currentGroupUsers = UserGroupMapping.findAllByGroup(grp)*.user.sort { it.username?.toLowerCase() }
                    grp.groupUsers = currentGroupUsers.toString()
                    grp.save(flush: true)
                }
            }
        }catch(Exception exception){
            exception.printStackTrace()
            log.info("Error occure while updating groupUser string for group: ${group?.name}")
        }
    }

    void updateGroupUsersStringForUser(User userInstance) {
        try {
            if (userInstance) {
                userInstance.groups.each {
                    it.groupUsers = UserGroupMapping.findAllByGroup(it)*.user.sort() {
                        it.username?.toLowerCase()
                    }.toString()
                    it.save(flush: true)
                }
            }
        } catch (Exception exception) {
            log.info("Error occure while updating groupUser string for user ${userInstance.properties}")

        }
    }

    /**
     * Refreshing the list of blinded users after any update happening to any group's users
     */

    void refreshBlindedUserCacheForGroup() {
        List<Long> groupIds = cacheService.getUserGroupBlindingCache()
        Set<Long> blindedUsers = new HashSet<>()

        if (groupIds) {
            List<UserGroupFieldMapping> mappings = UserGroupFieldMapping.findAllByGroupIdInList(groupIds)

            mappings.each { mapping ->
                if (mapping.blindedFields) {
                    Group group = Group.read(mapping.groupId)
                    blindedUsers.addAll(userGroupService.fetchUserListIdForGroup(group))
                }
            }
        }

        cacheService.clearUserBlindingCache()
        cacheService.prepareUserBlindingCache(blindedUsers as List)
    }

}
