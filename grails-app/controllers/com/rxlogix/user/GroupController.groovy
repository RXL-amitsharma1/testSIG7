package com.rxlogix.user

import com.rxlogix.Constants
import com.rxlogix.commandObjects.GroupCO
import com.rxlogix.enums.GroupType
import com.rxlogix.util.MiscUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.hibernate.criterion.CriteriaSpecification

@Secured(["isAuthenticated()"])
class GroupController {
    def CRUDService
    def pvsProductDictionaryService
    def productDictionaryCacheService
    def groupService
    def cacheService
    def alertService
    def userService
    def userGroupService
    def userRoleService

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def index() {
        flash.error = null
        params.max = Constants.Search.MAX_SEARCH_RESULTS
        List groupList = Group.createCriteria().list{
            eq('isActive',true)
            ne("groupType", GroupType.WORKFLOW_GROUP)
        }
        User user=userService.getUser(  )
        boolean editGroup=false;
        Set<Role> roles = UserRole.findAllByUser(user  ).collect { it.role?.authority } as Set
        List userGroupMappingList = UserGroupMapping.findAllByUser( user ).collect { it.group?.id }
        userGroupMappingList?.each {
            roles = roles + UserGroupRole.findAllByUserGroup( Group.get( it ) )?.collect { it?.role?.authority }

        }
        if( roles.contains( "ROLE_DEV" ) || roles.contains( "ROLE_ADMIN" )) {
            editGroup = true;
        }
        render view: "index", model: [groupInstanceList: Group.list(), groupInstanceTotal: Group.count(), activeGroupsCount: groupList.size() , editGroup:editGroup]
    }

    @Secured( [ 'ROLE_CONFIGURATION_CRUD' ] )
    def create( String type ) {
        def groupInstance = new Group()
        groupInstance.properties = params
        Map createModelMap = groupDispositionLists( groupInstance )
        createModelMap << [ type: type, groupUsersList: userGroupService.fetchUserListForGroup( Group.get( groupInstance.id ) ), allUserList: User.list( sort: 'fullName'.trim() ) ]
        render( view: "create", model: createModelMap )
    }

    private Map groupDispositionLists(Group groupInstance) {
        List<Map> defaultDispositionList = groupService.getDispositionList(false)
        List<Map> defaultSignalDispositionList = groupService.getDispositionList(true)
        [groupInstance : groupInstance, defaultDispositionList : defaultDispositionList.sort({it?.displayName?.toUpperCase()}),
         allowedProductsList : groupInstance.allowedLmProductList, defaultSignalDispositionList: defaultSignalDispositionList.sort({it?.displayName?.toUpperCase()}),
         alertLevelDispositionList: defaultDispositionList.sort({it?.displayName?.toUpperCase()})]
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def save(GroupCO groupCO) {
        params.name = params.name?.trim()?.replaceAll("\\s{2,}", " ")
        if(params.name){
            groupCO.name=params.name
        }
        Group groupInstance = new Group()
        flash.error = null
        try {
            List selectedUsers = params.selectedUsers ? Arrays.asList( params.selectedUsers ):[]
            List<User> usersToUpdateCache
            List selectedUsersObject = new ArrayList()
            selectedUsers.each {
                selectedUsersObject.add( User.get( it ) )
            }
            if(selectedUsers && params.type == "workflow" && !selectedUsers.isEmpty(  )) {
                List existingUsers = UserGroupMapping.findAllByUserInList( selectedUsersObject ).findAll {
                    it.group.groupType == GroupType.WORKFLOW_GROUP
                }.collect { it.user.fullName }
                if( existingUsers ) {
                    flash.error = message( code: 'app.workflow.group.not.assigned', args: [ String.join( " , ", existingUsers ) ] )
                    bindData( groupInstance, groupCO )
                    Map createModelMap = groupDispositionLists( groupInstance )
                    createModelMap << [ type: params.type, groupUsersList: userGroupService.fetchUserListForGroup( Group.get( groupInstance.id ) ), allUserList: User.list( sort: 'fullName'.trim() ) ]
                    render( view: "create", model: createModelMap )
                    return
                }
            }


            groupCO.setAlertLevelDispositions(params.alertLevelDispositions)
            bindData(groupInstance, params)
            setAllowedProducts(groupInstance)
            if( Group.findByNameAndGroupType( params.name, params.type == "workflow" ? GroupType.WORKFLOW_GROUP : GroupType.USER_GROUP ) ) {
                flash.error = (params.type == "workflow" ? "Workflow " : "User " )+ "Group Name already exists!!!"
                Map createModelMap = groupDispositionLists( groupInstance )
                createModelMap << [ type: params.type, groupUsersList: userGroupService.fetchUserListForGroup( Group.get( groupInstance.id ) ), allUserList: User.list( sort: 'fullName'.trim() ) ]
                render( view: "create", model: createModelMap )
                return
            }
            groupInstance = groupService.saveGroupObject(groupCO, groupInstance)
            groupService.saveGroupInfoInGroupsWebappTable(groupInstance)

            if (!groupInstance.hasErrors() && groupInstance) {
                //Update the product group cache
                if(alertService.isProductSecurity()){
                    log.info("Updating Product Dictionary Cache")
                    productDictionaryCacheService.updateProductDictionaryCache(groupInstance, groupInstance.allowedProductList, true)
                    log.info("Product Dictionary Cache Completed")
                    cacheService.setGroupCache(groupInstance)
                    List<Long> groupList = []
                    groupList.add(groupInstance.id)
                    if(groupInstance.groupType != GroupType.WORKFLOW_GROUP){
                        cacheService.updateProductsCacheForGroup(groupList)
                    }
                }else{
                    cacheService.setGroupCache(groupInstance)
                }
                groupService.saveUsersInUserGroup(groupInstance.id,selectedUsers)
                groupService.updateGroupUsersString(groupInstance)
                if( params.type == "user" ) {
                    List<Role> selectedRoles = userRoleService.getSelectedRolesList( params, [ ] )
                    groupService.saveRolesInUserGroup( groupInstance.id, selectedRoles as List )
                }
                groupService.updateMenuVisibilityInSession(request)
                flash.message = (params.type == "workflow" ? "Workflow " : "User " )+ message(code: 'group.created', args: [groupInstance.name])
                flash.args = [groupInstance.id]
                flash.defaultMessage = (params.type == "workflow" ? "Workflow " : "User " )+ "Group ${groupInstance.name} created"
                redirect(action: "show", id: groupInstance.id , params:[type : params.type])
            }
        }
        catch( grails.validation.ValidationException vx ) {
            vx.printStackTrace()
            def customErrorMessages = MiscUtil.getCustomErrorMessageList(vx)
            if (customErrorMessages) {
                flash.error = MiscUtil.getCustomErrorMessageList(vx);
            }
            if(flash.error == null){
                flash.error = []
            }
            if (vx.toString()?.contains("signal.group.justification.required")){
                flash.error << message(code: "com.rxlogix.signal.group.justification.required")
            }

            if (vx.toString()?.contains("group.name.nullable")|| vx.toString()?.contains("Literature.disposition.required") || vx.toString()?.contains("default.Adhoc.disposition.required")|| vx.toString()?.contains("default.EVDAS.disposition.required")|| vx.toString()?.contains("default.Quantitative.disposition.required") || vx.toString()?.contains("default.Qualitative.disposition.required") || vx.toString()?.contains("default.signal.disposition.required")) {
                if (flash.error) {
                    flash.error << message(code: "com.rxlogix.signal.group.default.signal.all.fields.required")
                } else {
                    flash.error = message(code: "com.rxlogix.signal.group.default.signal.all.fields.required")
                }
            }

            Map createModelMap = groupDispositionLists(groupInstance)
            createModelMap << [ type: params.type, groupUsersList: userGroupService.fetchUserListForGroup( Group.get( groupInstance.id ) ), allUserList: User.list( sort: 'fullName'.trim() ) ]
            render(view: "create", model: createModelMap)
        }
        catch (Exception e) {
            e.printStackTrace()
            Map createModelMap = groupDispositionLists(groupInstance)
            createModelMap << [ type: params.type, groupUsersList: userGroupService.fetchUserListForGroup( Group.get( groupInstance.id ) ), allUserList: User.list( sort: 'fullName'.trim() ) ]
            render(view: "create", model: createModelMap)
        }
    }

    def setAllowedProducts(groupInstance) {
        def productParams = params['allowedProductList']
        if (productParams instanceof Collection || productParams.getClass().isArray() ||
                productParams == null) {
            groupInstance.allowedProductList = productParams
        } else {
            groupInstance.allowedProductList = [productParams]
        }
    }

    @Secured(['ROLE_CONFIGURATION_VIEW'])
    def show(Long id) {
        Group groupInstance = Group.get(id)
        if (!groupInstance) {
            flash.message = message(code: 'group.not.found')
            flash.args = [params.id]
            flash.defaultMessage = "Group not found with id ${id}"
            redirect(action: "list")
        } else {
            render(view: 'show', model: [ type: params.type,groupInstance: groupInstance,groupUsersList: userGroupService.fetchUserListForGroup( Group.get( groupInstance.id ) ),
                                         groupRoleList : userGroupService.fetchRoleListForGroup( Group.get( groupInstance.id ) )])
        }
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def edit(String type) {
        def groupInstance = Group.get(params.id)
        groupInstance.name = groupInstance.name?.trim()?.replaceAll("\\s{2,}", " ")
        if (!groupInstance) {
            flash.message = message(code: 'group.not.found')
            flash.args = [params.id]
            flash.defaultMessage = "Group not found with id ${params.id}"
            redirect(action: "list")
        } else {
            Map createModelMap = groupDispositionLists(groupInstance)
            createModelMap << [ type: type, groupUsersList: userGroupService.fetchUserListForGroup( Group.get( groupInstance.id ) ), allUserList: User.list( sort: 'fullName'.trim() ),
            groupRoleList : userGroupService.fetchRoleListForGroup( Group.get( groupInstance.id ) )
            ]
            render(view: "edit", model: createModelMap)
        }
    }

    @Secured( [ 'ROLE_CONFIGURATION_CRUD' ] )
    def update( GroupCO groupCO ) {
        params.name = params.name?.trim()?.replaceAll("\\s{2,}", " ")
        Group groupInstance = Group.get( params.id )
        try {
            if( groupInstance ) {
                List selectedUsers = params.selectedUsers ? Arrays.asList( params.selectedUsers ) : [ ]
                List<User> usersToUpdateCache
                List<User> currentMappedUsers = UserGroupMapping.findAllByGroup(groupInstance).collect{
                    it.user
                }
                List selectedUsersObject = new ArrayList()
                selectedUsers.each {
                    selectedUsersObject.add( User.get( it ) )
                }
                usersToUpdateCache = (selectedUsersObject - currentMappedUsers) + (currentMappedUsers - selectedUsersObject)
                if( selectedUsers && params.type == "workflow" && !selectedUsers.isEmpty() ) {
                    List existingUsers = UserGroupMapping.findAllByUserInList( selectedUsersObject ).findAll {
                        it.group.groupType == GroupType.WORKFLOW_GROUP && groupInstance != it.group
                    }.collect { it.user.fullName }

                    if( existingUsers ) {
                        flash.error = message( code: 'app.workflow.group.not.assigned', args: [ String.join( " , ", existingUsers ) ] )
                        bindData( groupInstance, groupCO )
                        Map createModelMap = groupDispositionLists( groupInstance )
                        createModelMap << [ type: params.type, groupUsersList: userGroupService.fetchUserListForGroup( Group.get( groupInstance.id ) ), allUserList: User.list( sort: 'fullName'.trim() ) ]
                        render( view: "edit", model: createModelMap )
                        return
                    }
                }
                groupInstance.lastUpdated = new Date()

                if (params.alertLevelDispositions) {
                    if (params.alertLevelDispositions.class == String) {
                        params.alertLevelDispositions = [params.alertLevelDispositions]
                    }
                    List<String> updatedALD = params.alertLevelDispositions
                    List<String> oldALD = groupInstance.alertDispositions.collect { it.id as String }
                    if (params['alertLevelDispositions'] && (updatedALD - oldALD != [] || oldALD - updatedALD != [])) {
                        groupInstance.alertDispositions?.clear()
                        groupCO.setAlertLevelDispositions(params.alertLevelDispositions)
                    }
                } else {
                    groupInstance.alertDispositions?.clear()
                }

                String name = groupInstance.name
                bindData( groupInstance, params )
                setAllowedProducts( groupInstance )
                if( params.name != name && Group.findByNameAndGroupType( params.name, params.type == "workflow" ? GroupType.WORKFLOW_GROUP : GroupType.USER_GROUP ) ) {
                    Map createModelMap = groupDispositionLists( groupInstance )
                    flash.error = (params.type == "workflow" ? "Workflow " : "User " )+ "Group Name already exists!!!"
                    createModelMap << [ type: params.type, groupUsersList: userGroupService.fetchUserListForGroup( Group.get( groupInstance.id ) ), allUserList: User.list( sort: 'fullName'.trim() ) ]
                    render( view: "edit", model: createModelMap )
                    return
                }

                if( params.version ) {
                    def version = params.version.toLong()
                    if( groupInstance.version > version ) {
                        groupInstance.errors.rejectValue( "version", "group.optimistic.locking.failure", "Another user has updated this Group while you were editing" )
                        Map createModelMap = groupDispositionLists( groupInstance )
                        createModelMap << [ type: params.type, groupUsersList: userGroupService.fetchUserListForGroup( Group.get( groupInstance.id ) ), allUserList: User.list( sort: 'fullName'.trim() ) ]
                        render( view: "edit", model: createModelMap )
                        return
                    }
                }
                groupInstance = groupService.saveGroupObject( groupCO, groupInstance )
                groupService.updateGroupInfoInGroupsWebappTable( groupInstance )
                if( !groupInstance.hasErrors() && groupInstance ) {
                    //Update the product group cache
                    if( alertService.isProductSecurity() ) {
                        log.info( "Updating Product Dictionary Cache" )
                        productDictionaryCacheService.updateProductDictionaryCache( groupInstance, groupInstance.allowedProductList, true )
                        log.info( "Product Dictionary Cache Completed" )
                        cacheService.setGroupCache( groupInstance )
                        List<Long> groupList = [ ]
                        groupList.add( groupInstance.id )
                        if( groupInstance.groupType != GroupType.WORKFLOW_GROUP ) {
                            cacheService.updateProductsCacheForGroup( groupList )
                        }
                    } else {
                        cacheService.setGroupCache( groupInstance )
                    }
                    groupService.saveUsersInUserGroup( groupInstance.id, selectedUsers )
                    groupService.updateGroupUsersString(groupInstance)
                    if( params.type == "user" ) {
                        List<Role> selectedRoles = userRoleService.getSelectedRolesList( params, [ ] )
                        groupService.saveRolesInUserGroup( groupInstance.id, selectedRoles as List )
                    }
                    groupService.updateMenuVisibilityInSession(request)
                    if (usersToUpdateCache) {
                        // Refreshing user cache after updating the group
                        usersToUpdateCache.each { User user ->
                            List groupIds = user?.groups?.collect { it?.id }
                            userGroupService.deleteUserGroupMappingsForUser( user )
                            userGroupService.createUserGroupMappingsForUser( user, groupIds )
                            cacheService.setUserCacheByUserName(user)
                            cacheService.setUserCacheByUserId(user)
                        }
                    }
                    // Refreshing the blinded users cache after new user getting added to group
                    if (usersToUpdateCache && cacheService.getUserGroupBlindingCache()?.contains(groupInstance.id)) {
                        groupService.refreshBlindedUserCacheForGroup()
                    }
                    flash.message = (params.type == "workflow" ? "Workflow " : "User ") + message( code: 'group.updated', args: [ groupInstance.name ] )
                    flash.args = [ params.id ]
                    flash.defaultMessage = (params.type == "workflow" ? "Workflow " : "User " )+ "Group ${ groupInstance.name } updated"
                    redirect( action: "show", id: groupInstance.id, params: [ type: params.type ] )
                }
            } else {
                flash.message = (params.type == "workflow" ? "Workflow" : "User " )+ message( code: 'group.not.found' )
                flash.args = [ params.id ]
                flash.defaultMessage = (params.type == "workflow" ? "Workflow " : "User " )+ "Group not found with id ${ params.id }"
                redirect( action: "edit", id: params.id )
            }
        }
        catch( grails.validation.ValidationException vx ) {
            vx.printStackTrace()
            def customErrorMessages = MiscUtil.getCustomErrorMessageList(vx)
            if (customErrorMessages) {
                flash.error = MiscUtil.getCustomErrorMessageList(vx);
            }
            if(flash.error == null){
                flash.error = []
            }
            if (vx.toString()?.contains("signal.group.justification.required")){
                flash.error << message(code: "com.rxlogix.signal.group.justification.required")
            }
            if (vx.toString()?.contains("group.name.nullable")|| vx.toString()?.contains("Literature.disposition.required") || vx.toString()?.contains("default.Adhoc.disposition.required")|| vx.toString()?.contains("default.EVDAS.disposition.required")|| vx.toString()?.contains("default.Quantitative.disposition.required") || vx.toString()?.contains("default.Qualitative.disposition.required") || vx.toString()?.contains("default.signal.disposition.required")) {
                if (flash.error) {
                    flash.error << message(code: "com.rxlogix.signal.group.default.signal.all.fields.required")
                } else {
                    flash.error = message(code: "com.rxlogix.signal.group.default.signal.all.fields.required")
                }
            }
            Map createModelMap = groupDispositionLists( groupInstance )
            createModelMap << [ type: params.type, groupUsersList: userGroupService.fetchUserListForGroup( Group.get( groupInstance.id ) ), allUserList: User.list( sort: 'fullName'.trim() ) ]
            render( view: "edit", model: createModelMap )
        }
        catch( Exception e ) {
            e.printStackTrace()
            Map createModelMap = groupDispositionLists( groupInstance )
            createModelMap << [ type: params.type, groupUsersList: userGroupService.fetchUserListForGroup( Group.get( groupInstance.id ) ), allUserList: User.list( sort: 'fullName'.trim() ) ]

            render( view: "edit", model: createModelMap )
        }
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def delete() {
        def groupInstance = Group.get(params.id)
        if (groupInstance) {
            try {
                groupService.deleteGroupInfoFromGroupsWebappTable(groupInstance.id)
                CRUDService.delete(groupInstance)
                flash.message = message(code: 'group.deleted', args: [groupInstance.name])
                flash.args = [params.id]
                flash.defaultMessage = "Group ${groupInstance.name} deleted"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "group.not.deleted"
                flash.args = [params.id]
                flash.defaultMessage = "Group ${groupInstance.name} could not be deleted"
                redirect(action: "show", id: params.id)
            }
        } else {
            flash.message = message(code: 'group.not.found')
            flash.args = [params.id]
            flash.defaultMessage = "Group not found with id ${params.id}"
            redirect(action: "list")
        }
    }

    @Secured(['ROLE_CONFIGURATION_CRUD'])
    def getProducts(params){
        String searchProduct = params.searchProduct
        List products = pvsProductDictionaryService.getProducts(searchProduct)?.sort {
            prod1, prod2 -> prod1.compareToIgnoreCase(prod2)
        }
        render products as JSON
    }


    def addGroupsToReports(){
        def result = alertService.copyBulkGroups()
        render result as JSON
    }

}
