package com.rxlogix

import com.rxlogix.config.Configuration
import com.rxlogix.config.PvsAppConfiguration
import com.rxlogix.enums.GroupType
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.signal.EmergingIssue
import com.rxlogix.signal.ProductViewAssignment
import com.rxlogix.signal.UserViewAssignment
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.SignalQueryHelper
import grails.converters.JSON
import grails.events.EventPublisher
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.json.JsonBuilder
import groovy.sql.Sql
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.hibernate.Session
import org.hibernate.criterion.CriteriaSpecification
import org.springframework.transaction.annotation.Propagation

import java.sql.Clob
import java.sql.ResultSet
import com.rxlogix.config.AlertGroup
import grails.validation.ValidationException
import groovy.json.JsonSlurper

@Transactional
class ProductAssignmentService implements EventPublisher {
    def signalAuditLogService
    def cacheService
    def signalDataSourceService
    def userService
    def ldapService
    def grailsApplication
    def dataSource_pva
    def messageSource
    def dataSource
    def sessionFactory_pva
    def CRUDService
    def attachmentableService

    void saveProductAssignments(Map products, List assignmentList, Map dicMap, Map params, Long workflowGroup, Boolean isWfUpdate) {
        List<Long> newCreatedUserViews = []
        UserViewAssignment."pva".withTransaction {
            saveAssignmentForProductView(products, assignmentList, dicMap, params, workflowGroup, isWfUpdate)
            saveAssignmentForUserView(products, assignmentList, dicMap, workflowGroup, isWfUpdate, newCreatedUserViews)
            Session session = sessionFactory_pva.currentSession
            session.flush()
            session.clear()
        }
        if (newCreatedUserViews) {
            callProcForUserView(newCreatedUserViews)
        }
    }

    boolean isNewUserOrGroupAdded(Map userGroupMap, List<String> selectedUserOrGroup){
        List<String> selectedGroupIdString = selectedUserOrGroup?.findAll{it.startsWith(Constants.USER_GROUP_TOKEN)}
        List<Long> selectedGroupIdLong = selectedGroupIdString?.collect{it.replace(Constants.USER_GROUP_TOKEN,"") as Long}
        List<String> selectedUserIdString = selectedUserOrGroup?.findAll{it.startsWith(Constants.USER_TOKEN)}
        List<Long> selectedUserIdLong = selectedUserIdString?.collect{it.replace(Constants.USER_TOKEN,"") as Long}
        return (userGroupMap.user && (selectedGroupIdLong.size()>0 || selectedUserIdLong.minus(userGroupMap.user).size()>0)) ||
                (userGroupMap.group && (selectedUserIdLong.size()>0 || selectedGroupIdLong.minus(userGroupMap.group).size()>0))

    }
    def saveAssignmentForProductView(Map productMap, List assignmentList, Map dicMap, Map params, Long workflowGroup,
                                     Boolean isWfUpdate , Boolean isTwoWFGrpUpdate = false, Map userUpdateMap = null,
                                     Boolean notNewUserForThisProduct = false, isUserViewUpdate = false, Map userGroupMap = null,
                                    List<BigInteger> newProductsIds = []) {
        Map usersGroupsMap = getUsersAndGroups(assignmentList)
        List users = usersGroupsMap.users
        List groups = usersGroupsMap.groups
        productMap.each { product ->
            String hierarchy = getProductHierarchy(product.key as Integer, dicMap)
            String productName
            product.value.each { selectionMap ->
                BigInteger productId = selectionMap.id as BigInteger
                if (!isUserViewUpdate || (newProductsIds.contains(productId) || isNewUserOrGroupAdded(userGroupMap,JSON.parse(params.selectedUserOrGroup) as List))) {
                    productName = (selectionMap as JSON).toString()
                    if (!isTwoWFGrpUpdate) {
                        saveMatchedAndNonMatchedAssignment(productName, users, groups, hierarchy, workflowGroup, productId, params,
                                isWfUpdate, notNewUserForThisProduct, isUserViewUpdate, userGroupMap)
                    } else {
                        saveMatchedAndNonMatchedAssignment(productName, userUpdateMap["firstUsers"], groups, hierarchy,
                                userUpdateMap["firstWFGrp"], productId, params, isWfUpdate, notNewUserForThisProduct, isUserViewUpdate, userGroupMap)
                        saveMatchedAndNonMatchedAssignment(productName, userUpdateMap["secondUsers"], [], hierarchy,
                                userUpdateMap["secondWFGrp"], productId, params, isWfUpdate, notNewUserForThisProduct, isUserViewUpdate, userGroupMap)
                    }
                }
            }
        }
    }

    Map getPrimaryUsersId(Map params, Long workflowGroup, List groups){
        Long primaryIdLong
        Boolean primaryBelongToWFGrp = false
        if (params.primaryUserOrGroup) {
            String primaryId = params.primaryUserOrGroup
            if (primaryId.contains(Constants.USER_TOKEN)) {
                primaryIdLong = primaryId.replace(Constants.USER_TOKEN, "") as Long
                Long primaryWFGrpId = cacheService.getUserByUserId(primaryIdLong)?.getWorkflowGroup()?.id
                if (primaryWFGrpId == workflowGroup) {
                    primaryBelongToWFGrp = true
                }
            } else if (primaryId.contains(Constants.USER_GROUP_TOKEN) && groups) {
                primaryBelongToWFGrp = true
                primaryIdLong = primaryId.replace(Constants.USER_GROUP_TOKEN, "") as Long
            }
        }
        return ["primaryIdLong": primaryIdLong,"primaryBelongToWFGrp":primaryBelongToWFGrp]
    }

    def saveMatchedAndNonMatchedAssignment(String productName, List users, List groups,
                                           String hierarchy, Long workflowGroup, BigInteger productId, Map params,
                                           Boolean isWfUpdate, Boolean notNewUserForThisProduct = false, isUserViewUpdate = false, Map userGroupMap = null) {
        Long tenantId = Holders.config.product.assignment.product.view.tenantId
        if(users || groups) {
            Long wfGroupToCheck
            if(users && !workflowGroup){
                wfGroupToCheck = cacheService.getUserByUserId(users[0])?.getWorkflowGroup()?.id
            } else {
                wfGroupToCheck = workflowGroup
            }
            ProductViewAssignment."pva".withTransaction {
                List <ProductViewAssignment> matchedAssignmentList = ProductViewAssignment."pva".createCriteria().list {

                    or {
                        if (wfGroupToCheck) {
                            eq("workflowGroup", wfGroupToCheck)
                        } else {
                            isNull("workflowGroup")
                        }
                        if (isUserViewUpdate) {
                            if (userGroupMap.user) {
                                sqlRestriction("id in (select PRODUCT_ASSIGNMENT_ID from PRODUCT_ASSIGNMENT_USERS where USER_ID = ${userGroupMap.user})")
                            } else if (userGroupMap.group) {
                                sqlRestriction("id in (select PRODUCT_ASSIGNMENT_ID from PRODUCT_ASSIGNMENT_GROUPS where GROUP_ID = ${userGroupMap.group})")
                            }
                        }
                    }
                    sqlRestriction("JSON_VALUE(product,'\$.id') = ${productId}")
                    eq("hierarchy", hierarchy)
                    eq("alertGroup", params.alertGroup)
                }
                if (matchedAssignmentList) {
                    matchedAssignmentList.each { ProductViewAssignment matchedAssignment ->
                        matchedAssignment.product = productName
                        matchedAssignment.tenantId = tenantId
                        Map primaryUserMap = getPrimaryUsersId(params, workflowGroup, groups)
                        Long primaryIdLong = primaryUserMap.get("primaryIdLong")
                        Boolean primaryBelongToWFGrp = primaryUserMap.get("primaryBelongToWFGrp")
                        String primaryUserGroupString = ""
                        if (primaryIdLong && primaryBelongToWFGrp) {
                            primaryUserGroupString = params.primaryUserOrGroup
                            matchedAssignment.primaryUserOrGroupId = params.primaryUserOrGroup
                        } else {
                            if (!notNewUserForThisProduct && users) {
                                primaryUserGroupString = "User_${users[0]}"
                                matchedAssignment.primaryUserOrGroupId = "User_${users[0]}"
                            } else if (!notNewUserForThisProduct && groups) {
                                primaryUserGroupString = "UserGroup_${groups[0]}"
                                matchedAssignment.primaryUserOrGroupId = "UserGroup_${groups[0]}"
                            }
                        }
                        String primaryIdString = matchedAssignment.primaryUserOrGroupId
                        if(primaryIdString.contains(Constants.USER_TOKEN)){
                            Long userId = primaryIdString.replace(Constants.USER_TOKEN,"") as Long
                            matchedAssignment.workflowGroup = cacheService.getUserByUserId(userId)?.getWorkflowGroup()?.id
                        } else {
                            matchedAssignment.workflowGroup = null
                        }

                        if (matchedAssignment.usersAssigned) {
                            List commonUsers = matchedAssignment.usersAssigned.intersect(users)
                            users.minus(commonUsers).each { Long user ->
                                matchedAssignment.addToUsersAssigned(user)
                            }
                        } else {
                            users.each { Long user ->
                                matchedAssignment.addToUsersAssigned(user)
                            }
                        }
                        if (matchedAssignment.groupsAssigned) {
                            List commonGroups = matchedAssignment.groupsAssigned.intersect(groups)
                            groups.minus(commonGroups).each { Long group ->
                                matchedAssignment.addToGroupsAssigned(group)
                            }
                        } else {
                            groups.each { Long group ->
                                matchedAssignment.addToGroupsAssigned(group)
                            }
                        }
                        matchedAssignment.hierarchy = hierarchy

                        if (primaryUserGroupString.contains(Constants.USER_TOKEN)) {
                            matchedAssignment.workflowGroup = workflowGroup
                        } else {
                            matchedAssignment.workflowGroup = null
                        }
                        matchedAssignment.alertGroup = params.alertGroup
                        signalAuditLogService.updateAuditLog(matchedAssignment)
                        matchedAssignment.save()
                    }
                } else {
                    ProductViewAssignment productViewAssignment = new ProductViewAssignment(hierarchy: hierarchy,
                            product: productName, tenantId: tenantId)
                    Map primaryUserMap = getPrimaryUsersId(params, workflowGroup, groups)
                    Long primaryIdLong = primaryUserMap.get("primaryIdLong")
                    Boolean primaryBelongToWFGrp = primaryUserMap.get("primaryBelongToWFGrp")
                    if (primaryIdLong && primaryBelongToWFGrp) {
                        productViewAssignment.primaryUserOrGroupId = params.primaryUserOrGroup
                    } else {
                        if (users) {
                            if(!groups) {
                                workflowGroup = cacheService.getUserByUserId(users[0])?.getWorkflowGroup()?.id
                            }
                            productViewAssignment.primaryUserOrGroupId = "User_${users[0]}"
                        } else if (groups) {
                            productViewAssignment.primaryUserOrGroupId = "UserGroup_${groups[0]}"
                        }
                    }
                    String primaryIdString = productViewAssignment.primaryUserOrGroupId
                    if(primaryIdString.contains(Constants.USER_TOKEN)){
                        Long userId = primaryIdString.replace(Constants.USER_TOKEN,"") as Long
                        productViewAssignment.workflowGroup = cacheService.getUserByUserId(userId)?.getWorkflowGroup()?.id
                    } else {
                        productViewAssignment.workflowGroup = null
                    }
                    users.each { Long user ->
                        productViewAssignment.addToUsersAssigned(user)
                    }
                    productViewAssignment.workflowGroup = workflowGroup
                    groups.each { Long group ->
                        productViewAssignment.addToGroupsAssigned(group)
                    }
                    productViewAssignment.alertGroup = params.alertGroup
                    productViewAssignment.save()
                    signalAuditLogService.saveAuditLog(productViewAssignment)
                }
            }
        }
    }

    void saveProductAssignmentForBulkUpdate(String productName, List users, List groups, String hierarchy, Long workflowGroup,
                                       BigInteger productId, Map params, Boolean notNewUserForThisProduct,List<ProductViewAssignment> productViewAssignmentList, String primaryUserOrGroup, String alertGroup, List<ProductViewAssignment> updateAuditBatchPVA, List<ProductViewAssignment> createAuditBatchPVA) {
        Long tenantId = Holders.config.product.assignment.product.view.tenantId
        if (users || groups) {
            List<ProductViewAssignment> matchedAssignmentList = ProductViewAssignment."pva".createCriteria().list {
                if (workflowGroup) {
                    eq("workflowGroup", workflowGroup)
                } else {
                    isNull("workflowGroup")
                }
                sqlRestriction("JSON_VALUE(product,'\$.id') = ${productId}")
                eq("hierarchy", hierarchy)
            }
            if (matchedAssignmentList) {
                matchedAssignmentList.each { ProductViewAssignment matchedAssignment ->
                    matchedAssignment.product = productName
                    matchedAssignment.tenantId = tenantId
                    matchedAssignment.primaryUserOrGroupId = primaryUserOrGroup
                    if (primaryUserOrGroup.contains(Constants.USER_TOKEN)) {
                        Long userId = primaryUserOrGroup.replace(Constants.USER_TOKEN, "") as Long
                        matchedAssignment.workflowGroup = cacheService.getUserByUserId(userId)?.getWorkflowGroup()?.id
                    } else {
                        matchedAssignment.workflowGroup = null
                    }

                    if (matchedAssignment.usersAssigned) {
                        matchedAssignment.usersAssigned.clear()
                        users.each { Long user ->
                            matchedAssignment.addToUsersAssigned(user)
                        }
                    } else {
                        users.each { Long user ->
                            matchedAssignment.addToUsersAssigned(user)
                        }
                    }
                    if (matchedAssignment.groupsAssigned) {
                        matchedAssignment.groupsAssigned.clear()
                        groups.each { Long group ->
                            matchedAssignment.addToGroupsAssigned(group)
                        }
                    } else {
                        groups.each { Long group ->
                            matchedAssignment.addToGroupsAssigned(group)
                        }
                    }
                    matchedAssignment.hierarchy = hierarchy
                    productViewAssignmentList.add(matchedAssignment)
                    updateAuditBatchPVA.add(matchedAssignment)
//                    signalAuditLogService.updateAuditLog(matchedAssignment)
                }
            } else {
                ProductViewAssignment productViewAssignment = new ProductViewAssignment(hierarchy: hierarchy,
                        product: productName, tenantId: tenantId)
                productViewAssignment.alertGroup = alertGroup
                productViewAssignment.primaryUserOrGroupId = primaryUserOrGroup
                if (primaryUserOrGroup.contains(Constants.USER_TOKEN)) {
                    Long userId = primaryUserOrGroup.replace(Constants.USER_TOKEN, "") as Long
                    productViewAssignment.workflowGroup = cacheService.getUserByUserId(userId)?.getWorkflowGroup()?.id
                } else {
                    productViewAssignment.workflowGroup = null
                }
                users.each { Long user ->
                    productViewAssignment.addToUsersAssigned(user)
                }
                groups.each { Long group ->
                    productViewAssignment.addToGroupsAssigned(group)
                }
                productViewAssignmentList.add(productViewAssignment)
                createAuditBatchPVA.add(productViewAssignment)
//                signalAuditLogService.saveAuditLog(productViewAssignment)
            }
        }
    }

    Map separateUsersOnWorkflowGroup(List<Long> usersList, Long workflowGroup, Long secondWFGrpId){
        Map<Long,List> usersMap = [:]
        List usersForFirstWFGroup = []
        List usersForSecondWFGroup = []
        usersList.each {
            User user = User.get(it)
            if(user){
                Long wfGrpId = user?.getWorkflowGroup()?.id
                if(wfGrpId == workflowGroup){
                    usersForFirstWFGroup.add(user.id)
                } else {
                    usersForSecondWFGroup.add(user.id)
                }
            }
        }
        usersMap[workflowGroup] = usersForFirstWFGroup
        usersMap[secondWFGrpId] = usersForSecondWFGroup
        return usersMap
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    def updateAssignmentForProductView(Map productMap, List assignmentList, Long assignmentId, Map dicMap, Map params,
                                       Long workflowGroup, Boolean isWfUpdate, List<Long> newCreatedUserViews) {
        Long tenantIdProduct = Holders.config.product.assignment.product.view.tenantId
        Map usersGroupsMap = getUsersAndGroups(assignmentList)
        List users = usersGroupsMap.users
        List groups = usersGroupsMap.groups
        List<BigInteger> productsId = []
        productMap.each{
            if(it.value){
                it.value.each {Map map->
                    productsId.add(map.id as BigInteger)
                }
            }
        }
        Long secondWFGrpId = userService.getUser().getWorkflowGroup()?.id
        boolean isSecondWFGrp = false
        users.each{
            if(User.get(it)?.getWorkflowGroup()?.id == secondWFGrpId){
                isSecondWFGrp = true
            }
        }
        if(!isSecondWFGrp){
            secondWFGrpId = null
        }
        newCreatedUserViews.addAll(users)
        newCreatedUserViews.addAll(groups)
        ProductViewAssignment."pva".withTransaction {
            ProductViewAssignment productViewAssignment = ProductViewAssignment."pva".get(assignmentId)
            Long firstWFgrpId
            if(productViewAssignment.usersAssigned){
                firstWFgrpId = User.get(productViewAssignment.usersAssigned[0])?.getWorkflowGroup()?.id
                workflowGroup = firstWFgrpId
            }

            if (productViewAssignment) {
                deleteProductViewAssignment(productViewAssignment.id, true, workflowGroup, isWfUpdate, newCreatedUserViews, users, groups, productsId, dicMap)
                Boolean isFirstUpdate = true
                boolean isTwoWfGrpAvail = false
                List firstUsers = []
                List secondUsers = []
                if(firstWFgrpId && secondWFGrpId && firstWFgrpId != secondWFGrpId) {
                    Map differentWFGrpUsers = separateUsersOnWorkflowGroup(users, firstWFgrpId, secondWFGrpId)
                    firstUsers = differentWFGrpUsers[firstWFgrpId]
                    secondUsers = differentWFGrpUsers[secondWFGrpId]
                    if(firstUsers && !secondUsers){
                        users = firstUsers
                        workflowGroup = firstWFgrpId
                    } else if (!firstUsers && secondUsers){
                        users = secondUsers
                        workflowGroup = secondWFGrpId
                    } else if(firstUsers && secondUsers){
                        isTwoWfGrpAvail = true
                        users = firstUsers
                        workflowGroup = firstWFgrpId
                    }
                }
                String primaryId = params.primaryUserOrGroup
                Map primaryUserMap = getPrimaryUsersId(params, workflowGroup, groups)
                Long primaryIdLong = primaryUserMap.get("primaryIdLong")
                Boolean primaryBelongToWFGrp = primaryUserMap.get("primaryBelongToWFGrp")
                if(primaryIdLong && primaryBelongToWFGrp && primaryId.contains(Constants.USER_TOKEN)){
                    isWfUpdate = true
                } else {
                    isWfUpdate = false
                }
                Map assignmentMap = ["firstUsers": firstUsers, secondUsers: secondUsers, firstWFgrpId: firstWFgrpId, secondWFGrpId: secondWFGrpId]
                Boolean isUserToGroup = productViewAssignment.primaryUserOrGroupId?.contains(Constants.USER_TOKEN) && primaryId.contains(Constants.USER_GROUP_TOKEN)
                Long wfGrpForUsr = workflowGroup
                Boolean isWfUpdateUsr = isWfUpdate
                if(isUserToGroup){
                    isWfUpdateUsr = true
                    wfGrpForUsr = null
                }
                saveAssignmentForUserView(productMap, assignmentList, dicMap, wfGrpForUsr, isWfUpdateUsr, newCreatedUserViews,
                isTwoWfGrpAvail, assignmentMap)
                productMap.each { product ->
                    String hierarchy = getProductHierarchy(product.key as Integer, dicMap)
                    String productName
                    product.value.each { selectionMap ->
                        BigInteger productId = selectionMap.id as BigInteger
                        productName = (selectionMap as JSON).toString()
                        if (isFirstUpdate) {
                            productViewAssignment.hierarchy = hierarchy
                            productViewAssignment.product = productName
                            productViewAssignment.usersAssigned = []
                            productViewAssignment.groupsAssigned = []
                            if(primaryIdLong && primaryBelongToWFGrp) {
                                productViewAssignment.primaryUserOrGroupId = params.primaryUserOrGroup
                            }
                            String primaryIdString = productViewAssignment.primaryUserOrGroupId
                            if(primaryIdString.contains(Constants.USER_TOKEN)){
                                Long userId = primaryIdString.replace(Constants.USER_TOKEN,"") as Long
                                productViewAssignment.workflowGroup = User.get(userId)?.getWorkflowGroup()?.id
                            } else {
                                productViewAssignment.workflowGroup = null
                            }
                            productViewAssignment.tenantId = tenantIdProduct
                            users.each { Long user ->
                                productViewAssignment.addToUsersAssigned(user)
                            }
                            groups.each { Long group ->
                                productViewAssignment.addToGroupsAssigned(group)
                            }
                            productViewAssignment.lastUpdated = new Date()
                            signalAuditLogService.updateAuditLog(productViewAssignment)
                            productViewAssignment.save()
                            deleteAndUpdatePrevProductView(JSON.parse(productViewAssignment.product).id as BigInteger,productViewAssignment.workflowGroup,
                            productViewAssignment.hierarchy, productViewAssignment)
                            isFirstUpdate = false
                            if(isTwoWfGrpAvail){
                                saveMatchedAndNonMatchedAssignment(productName, secondUsers, [], hierarchy, secondWFGrpId, productId, params, true)
                            }
                        } else {
                            if(!isTwoWfGrpAvail) {
                                saveMatchedAndNonMatchedAssignment(productName, users, groups, hierarchy, workflowGroup, productId, params, isWfUpdate)
                            } else {
                                saveMatchedAndNonMatchedAssignment(productName, firstUsers, groups, hierarchy, firstWFgrpId, productId, params, isWfUpdate)
                                saveMatchedAndNonMatchedAssignment(productName, secondUsers, [], hierarchy, secondWFGrpId, productId, params, true)
                            }
                        }
                    }
                }
            }
        }
    }

    void deleteAndUpdatePrevProductView(BigInteger productId, Long wfGroupToCheck, String hierarchy, ProductViewAssignment updateProductViewAssignment){
        List <ProductViewAssignment> matchedAssignmentList = ProductViewAssignment."pva".createCriteria().list {
            if(wfGroupToCheck) {
                eq("workflowGroup", wfGroupToCheck)
            } else {
                isNull("workflowGroup")
            }
            sqlRestriction("JSON_VALUE(product,'\$.id') = ${productId}")
            eq("hierarchy", hierarchy)
        }
        matchedAssignmentList.each {ProductViewAssignment productViewAssignment->
            if(updateProductViewAssignment.id != productViewAssignment.id) {
                productViewAssignment.usersAssigned.each { Long userId ->
                    if (!updateProductViewAssignment.usersAssigned.contains(userId)) {
                        updateProductViewAssignment.addToUsersAssigned(userId)
                    }
                }
                productViewAssignment.groupsAssigned.each { Long groupId ->
                    if (!updateProductViewAssignment.groupsAssigned.contains(groupId)) {
                        updateProductViewAssignment.addToGroupsAssigned(groupId)
                    }
                }

                productViewAssignment.delete()
            }
        }
    }


    String getProductHierarchy(Integer index, Map dicMap) {
        String hierarchy = ""
        if(index == 0){
            hierarchy = "Product Group"
        } else {
            hierarchy = dicMap[index-1]
        }
        hierarchy
    }

    String getProductHierarchyWithoutDicMap(Integer ind){
        List dicList = PVDictionaryConfig.ProductConfig.views.collect { messageSource.getMessage(it.code, null, Locale.default) }
        Map dicMap = [:]
        dicList.eachWithIndex{value,index->
            dicMap[index] = value
        }
        String hierarchy = ""
        if(ind == 0){
            hierarchy = "Product Group"
        } else {
            hierarchy = dicMap[ind-1]
        }
        hierarchy
    }

    Map getUsersAndGroups(List assignmentList) {
        List groups = []
        List users = []
        assignmentList.each { String assignment ->
            Integer id
            if (assignment.startsWith("UserGroup_")) {
                id = assignment.substring(10, assignment.length()) as Integer
                groups.add(id)
            } else {
                id = assignment.substring(5, assignment.length()) as Integer
                users.add(id)
            }
        }
        return ["users": users, "groups": groups]
    }

    def saveAssignmentForUserView(Map productMap, List assignmentList, Map dicMap, Long workflowGroup, Boolean isWfUpdate,
                                  List<Long> newCreatedUserViews, isTwoWFGrpAvail = false, Map updateMap = null) {
        Map usersAndGroups = getUsersAndGroups(assignmentList)
        List users = usersAndGroups.users
        List groups = usersAndGroups.groups
        newCreatedUserViews.addAll(users)
        newCreatedUserViews.addAll(groups)
        productMap.each { product ->
            String hierarchy = getProductHierarchy(product.key as Integer, dicMap)
            Long hierarchyKeyId = ((product.key as Long) + 199)
            String productName = (product.value as JSON).toString()
            if (product.value) {
                if(!isTwoWFGrpAvail) {
                    users.each { Long user ->
                        newCreatedUserViews.add(user)
                        saveUserAssignmentForUser(user, hierarchy, workflowGroup, product, productName, hierarchyKeyId, isWfUpdate)
                    }
                } else {
                    updateMap.firstUsers.each{Long user->
                        newCreatedUserViews.add(user)
                        saveUserAssignmentForUser(user, hierarchy, updateMap.firstWFgrpId, product, productName, hierarchyKeyId, isWfUpdate)
                    }
                    updateMap.secondUsers.each{Long user->
                        newCreatedUserViews.add(user)
                        saveUserAssignmentForUser(user, hierarchy, updateMap.secondWFGrpId, product, productName, hierarchyKeyId, true)
                    }
                }
                groups.each { Long group ->
                    newCreatedUserViews.add(group)
                    saveUserAssignmentForGroup(group, hierarchy, workflowGroup, product, productName, hierarchyKeyId, isWfUpdate)
                }
            }
        }

    }

    void callProcForUserView(List<Long> userViewAssignment){
        /**
         * Commented code due to creating new connection not taking from pool
        final Sql sql = new Sql(signalDataSourceService.getReportConnection("pva"))
         */
        Sql sql = null
        try {
            sql = new Sql(dataSource_pva)
            String proc = "begin pkg_user_view_resolve.p_process_bulk_user_data('${userViewAssignment.unique().join(",")}'); end; "
            sql?.call(proc)
        } catch (Exception ex) {
            log.error("Call to p_process_bulk_user_data failed.")
            ex.printStackTrace()
        } finally {
            if (Objects.nonNull(sql)) {
                sql.close()
            }
        }
    }

    def saveUserAssignmentForGroup(Long group, String hierarchy, Long workflowGroup, def product, String productName, Long hierarchyKeyId, Boolean isWfUpdate) {
        Long tenantId = Holders.config.product.assignment.user.view.tenantId
        UserViewAssignment."pva".withTransaction {
            List<UserViewAssignment> matchedUserViewList = UserViewAssignment.createCriteria().list {
                eq("groupAssigned", group)
                eq("hierarchy", hierarchy)
            }
            if (matchedUserViewList) {
                matchedUserViewList.each { UserViewAssignment matchedUserView ->
                    List existingProducts = JSON.parse(matchedUserView.getProductsClob())
                    List newProducts = product.value
                    Set products = existingProducts.plus(newProducts) as Set
                    matchedUserView.products = (products as JSON).toString()
                    matchedUserView.tenantId = tenantId
                    matchedUserView.workflowGroup = null
                    matchedUserView.isResolved = 'N' as Character
                    matchedUserView.save()

                }
            } else {
                UserViewAssignment userViewAssignment = new UserViewAssignment(hierarchy: hierarchy, hierarchyKeyId: hierarchyKeyId,
                        groupAssigned: group, products: productName, isResolved: "N" as Character, tenantId: tenantId)
                userViewAssignment.save()
            }
        }
    }

    def saveUserAssignmentForUser(Long user, String hierarchy, Long workflowGroup, def product,
                                  String productName, Long hierarchyKeyId, Boolean isWfUpdate) {
        Long tenantId = Holders.config.product.assignment.user.view.tenantId
        UserViewAssignment."pva".withTransaction {
            List<UserViewAssignment> matchedUserViewList = UserViewAssignment."pva".createCriteria().list {
                eq("userAssigned", user)
                eq("hierarchy", hierarchy)
            }
            if (matchedUserViewList) {
                matchedUserViewList.each {UserViewAssignment matchedUserView ->
                    List existingProducts = JSON.parse(matchedUserView.getProductsClob())
                    List newProducts = product.value
                    Set products = existingProducts.plus(newProducts) as Set
                    matchedUserView.products = (products as JSON).toString()
                    matchedUserView.workflowGroup = cacheService.getUserByUserId(user)?.getWorkflowGroup()?.id
                    matchedUserView.tenantId = tenantId
                    matchedUserView.isResolved = 'N' as Character
                    matchedUserView.save()
                }
            } else {
                UserViewAssignment userViewAssignment = new UserViewAssignment(hierarchy: hierarchy, hierarchyKeyId: hierarchyKeyId,
                        userAssigned: user, products: productName, isResolved:'N' as Character, tenantId: tenantId)
                userViewAssignment.workflowGroup =cacheService.getUserByUserId(user)?.getWorkflowGroup()?.id
                userViewAssignment.save()
            }
        }
    }

    void saveUserAssignmentForExcelUpload(Long user, Long group, String hierarchy, String product, Long hierarchyKeyId,List<UserViewAssignment>userViewList=[]) {
        Long tenantId = Holders.config.product.assignment.user.view.tenantId

        List<UserViewAssignment> fetchedList = []
        if (user) {
            fetchedList= userViewList.findAll {
                (it.userAssigned == user && it.hierarchy == hierarchy)
            }
        }

        if (group) {
            fetchedList= userViewList.findAll {
                (it.groupAssigned == group && it.hierarchy == hierarchy)
            }
        }

        if (fetchedList) {
            fetchedList.each { UserViewAssignment matchedUserView ->
                List existingProducts = JSON.parse(matchedUserView.getProductsClob())
                List newProducts = JSON.parse(product)
                List commonProducts = existingProducts.intersect(newProducts)
                matchedUserView.products = (existingProducts.plus(newProducts.minus(commonProducts)) as JSON).toString()
                matchedUserView.workflowGroup = user ? cacheService.getUserByUserId(user)?.getWorkflowGroup()?.id : null
                matchedUserView.tenantId = tenantId
                matchedUserView.isResolved = 'N' as Character
                matchedUserView.save()
            }
        } else {
            UserViewAssignment userViewAssignment = new UserViewAssignment(hierarchy: hierarchy, hierarchyKeyId: hierarchyKeyId,
                    userAssigned: user, groupAssigned: group, products: product, isResolved: 'N' as Character, tenantId: tenantId)
            userViewAssignment.workflowGroup = user ? cacheService.getUserByUserId(user)?.getWorkflowGroup()?.id : null
            userViewAssignment.save()
        }
    }

    void saveProductAssignmentForExcelUpload(Map productMap, List assignmentList, Map dicMap, Map params, Long workflowGroup,
                                            Boolean notNewUserForThisProduct,List<ProductViewAssignment>productViewAssignmentList, String primaryUserOrGroup, String alertGroup,List<ProductViewAssignment> updateAuditBatchPVA, List<ProductViewAssignment> createAuditBatchPVA) {
        Map usersGroupsMap = getUsersAndGroups(assignmentList)
        List users = usersGroupsMap.users
        List groups = usersGroupsMap.groups
        productMap.each { product ->
            String hierarchy = getProductHierarchy(product.key as Integer, dicMap)
            String productName
            product.value.each { selectionMap ->
                BigInteger productId = selectionMap.id as BigInteger
                productName = (selectionMap as JSON).toString()
                saveProductAssignmentForBulkUpdate(productName, users, groups, hierarchy, workflowGroup, productId, params, notNewUserForThisProduct,productViewAssignmentList, primaryUserOrGroup, alertGroup, updateAuditBatchPVA, createAuditBatchPVA)
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void deleteDuplicateUserAssignment(Map productMap, Long id, List<Long> users, List<Long> groups, Long workflowGroup, Boolean isWfUpdate,
                                       List<BigInteger> productsId, Map dicMap){
        boolean isFirstUpdate =true
        String hierarchy
        productMap.each { product ->
            product.value.each { selectionMap ->
                if (isFirstUpdate) {
                    hierarchy = getProductHierarchy(product.key as Integer, dicMap)
                    isFirstUpdate = false
                }
            }
        }
        Long userAssigned = users?users[0]:null
        Long groupAssigned = groups?groups[0]:null
        List<UserViewAssignment> matchedUserView
        UserViewAssignment."pva".withTransaction {
            matchedUserView = UserViewAssignment."pva".createCriteria().list {
                if(userAssigned) {
                    eq("userAssigned", userAssigned)
                } else {
                    eq("groupAssigned", groupAssigned)
                }
                eq("hierarchy",hierarchy)
            }
            matchedUserView.each{UserViewAssignment uva->
                if(uva.id != id){
                    deleteUserViewAssignment(uva.id, false, workflowGroup, isWfUpdate, productsId)
                }
            }
        }
    }

    Map<String, List> getDifferentProductIdsForUserViewAssignment(UserViewAssignment userViewAssignment, Map productMap, Map dicMap) {
        List<BigInteger> productsId = []
        String hierarchyOfAssignment = userViewAssignment.hierarchy
        Integer hierarchyKey = dicMap.find { it.value == hierarchyOfAssignment }?.key as Integer
        String hierarchyKeyOfAssignment = hierarchyKey?hierarchyKey + 1: '0'
        List productOfAssignment = JSON.parse(userViewAssignment.getProductsClob()) as List
        List<BigInteger> existingIdList = productOfAssignment.collect { it.id as BigInteger }
        List<BigInteger> deletedProductsIds = []
        Set<BigInteger> newProductsIds = []
        productMap.each {
            List<BigInteger> selectedIdList = it.value.collect { it.id as BigInteger }
            if (it.key == hierarchyKeyOfAssignment) {
                List deletedProductIdList = existingIdList - selectedIdList
                if (deletedProductIdList) {
                    deletedProductsIds.addAll(deletedProductIdList)
                }
            }
            List newSelectedIdList = selectedIdList - existingIdList
            if (newSelectedIdList) {
                newProductsIds.addAll(newSelectedIdList)
            }
            productsId.addAll(selectedIdList)
        }
        [productsId: productsId, newProductsIds: newProductsIds as List, deletedProductsIds: deletedProductsIds]
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    def updateAssignmentForUserView(Map productMap, List assignmentList, Long assignmentId, Map dicMap, Map params,
                                    Long workflowGroup, Boolean isWfUpdate, List<Long> newCreatedUserViews) {
        Long tenantId = Holders.config.product.assignment.user.view.tenantId
        Map usersGroupsMap = getUsersAndGroups(assignmentList)
        List users = usersGroupsMap.users
        List groups = usersGroupsMap.groups
        newCreatedUserViews.addAll(users)
        newCreatedUserViews.addAll(groups)
        UserViewAssignment."pva".withTransaction {
            UserViewAssignment userViewAssignment = UserViewAssignment."pva".get(assignmentId)
            Long secondWFGrpId = userService.getUser().getWorkflowGroup()?.id
            boolean isSecondWFGrp = false
            users.each{
                if(User.get(it)?.getWorkflowGroup()?.id == secondWFGrpId){
                    isSecondWFGrp = true
                }
            }
            if(!isSecondWFGrp){
                secondWFGrpId = null
            }
            Long firstWFgrpId
            userViewAssignment.userAssigned.each {
                firstWFgrpId = User.get(it)?.getWorkflowGroup()?.id
                workflowGroup = firstWFgrpId
            }
            if(!workflowGroup){
                workflowGroup = userViewAssignment.workflowGroup
            }
            boolean isTwoWfGrpAvail = false
            boolean isTwoWFGrp = false
            List firstUsers = []
            List secondUsers = []

            if (userViewAssignment) {
                log.info("Processing of product ids started")
                Map differentProductIds = getDifferentProductIdsForUserViewAssignment(userViewAssignment, productMap, dicMap)
                log.info("Processing of product ids ended")
                List<BigInteger> productsId = differentProductIds.productsId
                List<BigInteger> deletedProductsIds = differentProductIds.deletedProductsIds
                List<BigInteger> newProductsIds = differentProductIds.newProductsIds
                deleteDuplicateUserAssignment(productMap,assignmentId, users, groups, workflowGroup, isWfUpdate, productsId, dicMap)
                deleteUserViewAssignment(userViewAssignment.id, true, workflowGroup, isWfUpdate, productsId, deletedProductsIds)
                if(firstWFgrpId && secondWFGrpId && firstWFgrpId != secondWFGrpId) {
                    isTwoWFGrp = true
                    Map differentWFGrpUsers = separateUsersOnWorkflowGroup(users, firstWFgrpId, secondWFGrpId)
                    firstUsers = differentWFGrpUsers[firstWFgrpId]
                    secondUsers = differentWFGrpUsers[secondWFGrpId]
                    if(firstUsers && !secondUsers){
                        users = firstUsers
                        workflowGroup = firstWFgrpId
                    } else if (!firstUsers && secondUsers){
                        users = secondUsers
                        workflowGroup = secondWFGrpId
                    } else if(firstUsers && secondUsers){
                        users = firstUsers
                        isTwoWfGrpAvail = true
                        workflowGroup = firstWFgrpId
                        isWfUpdate = true
                    }
                }
                Map userGroupMap = ["user":userViewAssignment.userAssigned,"group":userViewAssignment.groupAssigned]
                if(!isTwoWFGrp) {
                    saveAssignmentForProductView(productMap, assignmentList, dicMap, params, workflowGroup, isWfUpdate,
                            false, [:], false, true, userGroupMap, newProductsIds)
                } else {
                    Map userUpdateMap = ["firstUsers": firstUsers, "secondUsers":secondUsers, "firstWFGrp":firstWFgrpId,
                                         "secondWFGrp": secondWFGrpId, "groupsExisted": userViewAssignment.groupAssigned]
                    saveAssignmentForProductView(productMap, assignmentList, dicMap, params, workflowGroup, isWfUpdate,
                            true, userUpdateMap, false, true, userGroupMap, newProductsIds)
                }
                Boolean isFirstUpdate = true
                productMap.each { product ->
                    String hierarchy = getProductHierarchy(product.key as Integer, dicMap)
                    Long hierarchyKeyId = ((product.key as Long) + 199)
                    String productName = (product.value as JSON).toString()
                    if (product.value) {
                        users.each { Long user ->
                            if (userViewAssignment.hierarchy == hierarchy) { // Updating User view assignment of older object with same hierarchy
                                userViewAssignment.userAssigned = null
                                userViewAssignment.groupAssigned = null
                                userViewAssignment.products = (product.value as JSON).toString()
                                userViewAssignment.userAssigned = user
                                userViewAssignment.hierarchy = hierarchy
                                userViewAssignment.workflowGroup = User.get(user)?.getWorkflowGroup()?.id
                                userViewAssignment.tenantId = tenantId
                                userViewAssignment.isResolved = 'N' as Character
                                userViewAssignment.hierarchyKeyId = hierarchyKeyId

                                isFirstUpdate = false
                                userViewAssignment.save()
                            } else {
                                saveUserAssignmentForUser(user, hierarchy, workflowGroup, product, productName, hierarchyKeyId, isWfUpdate)
                            }
                        }
                        if(isTwoWfGrpAvail){
                            secondUsers.each{ Long user ->
                                saveUserAssignmentForUser(user, hierarchy, secondWFGrpId, product, productName, hierarchyKeyId, isWfUpdate)
                            }
                        }
                        groups.each { Long group ->
                            if (userViewAssignment.hierarchy == hierarchy) {// Updating User view assignment of older object with same hierarchy
                                userViewAssignment.userAssigned = null
                                userViewAssignment.groupAssigned = null
                                userViewAssignment.products = (product.value as JSON).toString()
                                userViewAssignment.groupAssigned = group
                                userViewAssignment.hierarchy = hierarchy
                                userViewAssignment.tenantId = tenantId
                                userViewAssignment.isResolved = 'N' as Character
                                userViewAssignment.hierarchyKeyId = hierarchyKeyId
                                userViewAssignment.workflowGroup = null
                                isFirstUpdate = false
                                userViewAssignment.save()
                            } else {
                                saveUserAssignmentForGroup(group, hierarchy, workflowGroup, product, productName, hierarchyKeyId, isWfUpdate)
                            }
                        }
                    }
                }
            }
        }
    }

    Map getUsersAndGroupsId(List groupsList, List usersList, String assignmentNameString, Boolean isUsername, Boolean isOnlyUserName = false) {
        List matchedGroupIdsList = []
        groupsList.collect { Group grp ->
            if (grp.name.toUpperCase().contains(assignmentNameString.toUpperCase())) {
                matchedGroupIdsList.add(grp.id)
            }
        }
        String matchedGroupIds = matchedGroupIdsList.join(",")

        List matchedUserIdsList = []
        usersList.collect { User usr ->
            if (isUsername) {
                if ((usr.username && usr.username.toUpperCase().contains(assignmentNameString.toUpperCase())) ||
                        (usr.fullName && usr.fullName.toUpperCase().contains(assignmentNameString.toUpperCase()))) {
                    matchedUserIdsList.add(usr.id)
                }
            } else if(isOnlyUserName){
                if (usr.username && usr.username.toUpperCase().contains(assignmentNameString.toUpperCase())) {
                    matchedUserIdsList.add(usr.id)
                }
            } else {
                if (usr.fullName && usr.fullName.toUpperCase().contains(assignmentNameString.toUpperCase())) {
                    matchedUserIdsList.add(usr.id)
                }
            }
        }
        String matchedUserIds = matchedUserIdsList.join(",")
        return [matchedGroupIds: matchedGroupIds, matchedUserIds: matchedUserIds]
    }

    List getWorkflowGroupSorted(List<Group> groups, String dir){
        List<Group> wfGroups = groups.findAll{it.groupType == GroupType.WORKFLOW_GROUP}
        List groupIds
        if(dir == "asc"){
            groupIds = wfGroups.sort{it.name.toLowerCase()}.id
        } else {
            groupIds = wfGroups.sort{a,b->b.name.toLowerCase()<=>a.name.toLowerCase()}.id
        }
        return groupIds
    }
    List getGroupSorted(List<Group> groups, String dir){
        List<Group> groupsList = groups.findAll{it.groupType != GroupType.WORKFLOW_GROUP}
        List groupIds
        if(dir == "asc"){
            groupIds = groupsList.sort{it.name.toLowerCase()}.id
        } else {
            groupIds = groupsList.sort{a,b->b.name.toLowerCase()<=>a.name.toLowerCase()}.id
        }
        return groupIds
    }

    List getUsernameSorted(List<User> users, String dir) {
        List userIds
        if (dir == "asc") {
            userIds = users.sort { it.username.toLowerCase() }.id
        } else {
            userIds = users.sort { a, b -> b.username.toLowerCase() <=> a.username.toLowerCase() }.id
        }
        return userIds
    }

    List getFullNameSortedList(List<User> users, String dir) {
        List userIds
        if (dir == "asc") {
            userIds = users.sort { it.fullName.toLowerCase() }.id
        } else {
            userIds = users.sort { a, b -> b.fullName.toLowerCase() <=> a.fullName.toLowerCase() }.id
        }
        return userIds
    }

    List getMatchingProductViewAssignment(Map params, DataTableSearchRequest searchRequest, List groupsList, List usersList) {
        String createdById
        String workflowGroupName
        String assignmentNameString
        String hierarchyName
        String nameString
        String searchValue
        String alertGroupName
        params.columns.each { Map columnMap ->
            switch (columnMap.data) {
                case "product":
                    nameString = columnMap.search.value
                    break
                case "hierarchy":
                    hierarchyName = columnMap.search.value
                    break
                case "assignedUserOrGroup":
                    assignmentNameString = columnMap.search.value
                    break
                case "createdBy":
                    createdById = columnMap.search.value
                    break
                case "workflowGroup":
                    workflowGroupName = columnMap.search.value
                    break
                case "alertGroup":
                    alertGroupName = columnMap.search.value
                    break
            }
        }
        if(params.search.value){
            searchValue = params.search.value
        }
        searchValue = searchValue?.replaceAll("'","''")
        List assignments = []

        assignments = ProductViewAssignment."pva".createCriteria().list (offset: searchRequest?.searchParam?.start, max: searchRequest?.pageSize()){
            if(!searchValue) {
                    if (workflowGroupName) {
                        List matchedWFGroupIdsList = []
                        groupsList.collect { Group grp ->
                            if (grp.name.toUpperCase().contains(workflowGroupName.toUpperCase()) && grp.groupType as GroupType == GroupType.WORKFLOW_GROUP) {
                                matchedWFGroupIdsList.add(grp.id)
                            }
                        }
                        String matchedWFGroupIds = matchedWFGroupIdsList.join(",")
                        sqlRestriction("""{alias}.WORKFLOW_GROUP in (${matchedWFGroupIds ?: "''"})""")
                    }

                    if (assignmentNameString) {
                        Map usersAndGroupsIds = getUsersAndGroupsId(groupsList, usersList, assignmentNameString, false)
                        String matchedUserIds = usersAndGroupsIds.matchedUserIds
                        String matchedGroupIds = usersAndGroupsIds.matchedGroupIds
                        sqlRestriction(""" {alias}.id in
                        (
                            (select PRODUCT_ASSIGNMENT_ID from PRODUCT_ASSIGNMENT_USERS where USER_ID
                            in (${matchedUserIds ?: "''"}))
                            UNION
                            (select PRODUCT_ASSIGNMENT_ID from PRODUCT_ASSIGNMENT_GROUPS where GROUP_ID
                            in (${matchedGroupIds ?: "''"}))
                        )
                        """)
                    }
                    if(createdById){
                        Map usersAndGroupsIds = getUsersAndGroupsId(groupsList, usersList, createdById, false, true)
                        String matchedUserIds = usersAndGroupsIds.matchedUserIds
                        sqlRestriction(""" {alias}.id in
                        (
                            (select PRODUCT_ASSIGNMENT_ID from PRODUCT_ASSIGNMENT_USERS where USER_ID
                            in (${matchedUserIds ?: "''"}))
                        )
                        """)
                    }
                    if (hierarchyName) {
                        ilike('hierarchy', "%" + hierarchyName + "%")
                    }
                    if (nameString) {
                        sqlRestriction("UPPER(JSON_VALUE(product,'\$.name')) LIKE UPPER('%${nameString}%')")
                    }
                if (alertGroupName) {
                    List alertGroupIds = fetchAlertGroupIdsForSearch(alertGroupName)
                    String alertGroupIdsStr = alertGroupIds?.join(',')
                    sqlRestriction("""alert_group IN (${alertGroupIdsStr ?: "''"}) """)
                    }
            } else {
                    String esc_char = ""
                    if (searchValue.contains('_')) {
                        searchValue = searchValue.replaceAll("\\_", "!_")
                        esc_char = "escape '!'"
                    } else if (searchValue.contains('%')) {
                        searchValue = searchValue.replaceAll("\\%", "!%%")
                        esc_char = "escape '!'"
                    }
                    or {
                        List matchedWFGroupIdsList = []
                        groupsList.collect { Group grp ->
                            if (grp.name.toUpperCase().contains(searchValue.toUpperCase()) && grp.groupType as GroupType == GroupType.WORKFLOW_GROUP) {
                                matchedWFGroupIdsList.add(grp.id)
                            }
                        }
                        String matchedWFGroupIds = matchedWFGroupIdsList.join(",")
                        sqlRestriction("""{alias}.WORKFLOW_GROUP in (${matchedWFGroupIds ?: "''"})""")

                        Map usersAndGroupsIds = getUsersAndGroupsId(groupsList, usersList, searchValue, true)
                        String matchedUserIds = usersAndGroupsIds.matchedUserIds
                        String matchedGroupIds = usersAndGroupsIds.matchedGroupIds
                        sqlRestriction(""" {alias}.id in
                        (
                            (select PRODUCT_ASSIGNMENT_ID from PRODUCT_ASSIGNMENT_USERS where USER_ID
                            in (${matchedUserIds ?: "''"}))
                            UNION
                            (select PRODUCT_ASSIGNMENT_ID from PRODUCT_ASSIGNMENT_GROUPS where GROUP_ID
                            in (${matchedGroupIds ?: "''"}))
                        )
                        """)

                        ilike('hierarchy', "%" + searchValue + "%")
                        sqlRestriction("UPPER(JSON_VALUE(product,'\$.name')) LIKE UPPER('%${searchValue}%')  ${esc_char} ")
                        List alertGroupIds = fetchAlertGroupIdsForSearch(searchValue,esc_char )
                        if (alertGroupIds) {
                            String alertGroupIdsStr = alertGroupIds.join(',')
                            sqlRestriction("""alert_group IN (${alertGroupIdsStr ?: "''"}) """)
                        }
                    }

            }
            Map requestMap = params.order[0]
            List columns = params.columns
            String columnName = columns[requestMap['column']].data

            if(columnName == "hierarchy"){
                sqlRestriction(" 1=1 ORDER BY UPPER(HIERARCHY) ${requestMap.dir}")
            } else if(columnName == "product"){
                sqlRestriction(" 1=1 ORDER BY UPPER(PRODUCT) ${requestMap.dir}")
            } else if(columnName == "workflowGroup"){
                List matchedWFGroupIds = getWorkflowGroupSorted(groupsList,requestMap.dir)
                sqlRestriction("""1=1 ORDER BY instr('${matchedWFGroupIds.join(",")}',workflow_group)""")
            } else {
                order("lastUpdated", "desc")
            }
        }
        return assignments
    }

    List fetchAlertGroupIdsForSearch(String alertGroupName, String esc_char = '') {
        Sql sql
        try {
            sql = new Sql(dataSource)
            String query = """
            SELECT id FROM alert_group
            WHERE UPPER(name) LIKE UPPER('%${alertGroupName}%')
        """

            if (esc_char) {
                query += esc_char
            }

            def alertGroups = sql.rows(query)
            return alertGroups.collect { it.id }
        } catch (Exception ex) {
            ex.printStackTrace()
            return []
        } finally {
            sql?.close()
        }
    }


    Map fetchValuesForProductView(Map params, DataTableSearchRequest searchRequest, Map dicMap, Map groupMap, Map userMap) {
        List assignments
        List result = []
        Integer totalRecord
        Integer filteredCount
        List groupsList = []
        groupMap.each { groupsList.add(it.value) }
        List usersList = []
        userMap.each { usersList.add(it.value) }
        List <AlertGroup> alertGroupList = AlertGroup.getAll()
        ProductViewAssignment."pva".withTransaction {
            assignments = getMatchingProductViewAssignment(params, searchRequest, groupsList, usersList)
            filteredCount = assignments.totalCount
            assignments.each { ProductViewAssignment productAssignment ->
                String productGroupAssessment = ""
                String productAssessment = ""
                if (productAssignment.hierarchy == "Product Group") {
                    productGroupAssessment = getSingleProductGroupJSONFormat(productAssignment.product)
                } else {
                    productAssessment = getSingleProductJSONFormat(productAssignment.product, productAssignment.hierarchy, dicMap)
                }
                List<Map> assignedUserOrGroupList = generateAssignedUserOrGroupList(productAssignment.usersAssigned, productAssignment.groupsAssigned)
                result.add(["hierarchy"            : productAssignment.hierarchy, "workflowGroup": (productAssignment.workflowGroup ? groupMap.get(productAssignment.workflowGroup)?.name : ""), "createdBy": productAssignment.usersAssigned?.collect { userMap.get(it) }?.username?.join(", "),
                            "product"              : JSON.parse(productAssignment.product).name,
                            "productNameDictionary": JSON.parse(productAssignment.product).name + " (${productAssignment.hierarchy})",
                            "assignedUserOrGroup"  : assignedUserOrGroupList,
                            "assignmentNameString" : assignedUserOrGroupList.collect { it.name }.join(", "),
                            "action"               : productAssignment.id, "productGroupAssessment": productGroupAssessment, "productAssessment": productAssessment, primaryUserOrGroupId: productAssignment.primaryUserOrGroupId,
                            "alertGroup"           : alertGroupList.find{it.id == (productAssignment.alertGroup ? productAssignment.alertGroup as Long : null)}?.name])
            }
            totalRecord = ProductViewAssignment."pva".count()
        }
        return [aaData:result, recordsFiltered: filteredCount, recordsTotal:totalRecord]
    }

    List<Map> generateAssignedUserOrGroupList(List<Long> usersAssigned, List<Long> groupsAssigned){
        List<Map> assignedUserOrGroupsList = []
        List blindedUsersList = cacheService.getUserBlindingCache()
        List blindedUserGroupsList = cacheService.getUserGroupBlindingCache()
        usersAssigned?.each {
            User user = cacheService.getUserByUserId(it)
            if(user) {
                assignedUserOrGroupsList.add(["name": user.fullName, "id": "User_" + it, blinded: blindedUsersList?.contains(it)])
            }
        }
        groupsAssigned?.each {
            Group group = cacheService.getGroupByGroupId(it)
            if(group) {
                assignedUserOrGroupsList.add(["name": group.name, "id": "UserGroup_" + it, blinded: blindedUserGroupsList?.contains(it)])
            }
        }
        assignedUserOrGroupsList
    }

    String getSingleProductJSONFormat(String product, String hierarchy, Map dicMap) {
        String productJSON = ""
        if (product) {
            Map productNameId = JSON.parse(product)
            Map productMap = [:]
            dicMap.each{
                productMap["${it.key+1}"] = []
            }
            dicMap.each{
                if(it.value == hierarchy){
                    (productMap["${it.key+1}"]).add(productNameId)
                }
            }
            productJSON = (productMap as JSON).toString()
        }
        return productJSON
    }

    String getSingleProductGroupJSONFormat(String productGroup) {
        String groupsJSON = ""
        if (productGroup) {
            Map productMap = JSON.parse(productGroup)
            productMap.name += " (${productMap.id})"
            groupsJSON = ([productMap] as JSON).toString()
        }
        return groupsJSON
    }

    String getMultipleProductJSONFormat(String product, String hierarchy, Map dicMap) {
        String productJSON = ""
        if (product) {
            List productNameId = JSON.parse(product)
            Map productMap = [:]
            dicMap.each{
                productMap["${it.key+1}"] = []
            }
            dicMap.each{
                if(it.value == hierarchy){
                    (productMap["${it.key+1}"]) = productNameId
                }
            }
            productJSON = (productMap as JSON).toString()
        }
        return productJSON
    }

    String getMultipleProductGroupJSONFormat(String productGroup) {
        String groupsJSON = ""
        if (productGroup) {
            List productGroupMap = JSON.parse(productGroup)
            productGroupMap.each { map ->
                map.name += " (${map.id})"
            }
            groupsJSON = (productGroupMap as JSON).toString()
        }
        return groupsJSON
    }

    List getMatchingUserViewAssignment(Map params, DataTableSearchRequest searchRequest, List groupsList, List usersList) {
        String createdById
        String workflowGroupName
        String assignmentNameString
        String hierarchyName
        String nameString
        String searchValue
        params.columns.each { Map columnMap ->
            switch (columnMap.data) {
                case "product":
                    nameString = columnMap.search.value
                    break
                case "hierarchy":
                    hierarchyName = columnMap.search.value
                    break
                case "assignedUserOrGroup":
                    assignmentNameString = columnMap.search.value
                    break
                case "createdBy":
                    createdById = columnMap.search.value
                    break
                case "workflowGroup":
                    workflowGroupName = columnMap.search.value
                    break
            }
        }
        List<Long> prodIdList = []
        if(params.search.value){
            searchValue = params.search.value
            Sql sql = new Sql(dataSource_pva)
            sql.eachRow(SignalQueryHelper.user_view_product_search_sql(searchValue),[]){ row ->
                prodIdList.add(row[0] as Long)
            }
        }

        List assignments = UserViewAssignment."pva".createCriteria().list (offset: searchRequest?.searchParam?.start, max: searchRequest?.pageSize()) {
            resultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
            if (!searchValue) {
                if (workflowGroupName) {
                    List matchedWFGroupIdsList = []
                    groupsList.collect { Group grp ->
                        if (grp.name.toUpperCase().contains(workflowGroupName.toUpperCase()) && grp.groupType as GroupType == GroupType.WORKFLOW_GROUP) {
                            matchedWFGroupIdsList.add(grp.id)
                        }
                    }
                    String matchedWFGroupIds = matchedWFGroupIdsList.join(",")
                    sqlRestriction("""{alias}.WORKFLOW_GROUP in (${matchedWFGroupIds ?: "''"})""")
                }

                if (assignmentNameString) {
                    Map usersAndGroupsIds = getUsersAndGroupsId(groupsList, usersList, assignmentNameString, false)
                    String matchedUserIds = usersAndGroupsIds.matchedUserIds
                    String matchedGroupIds = usersAndGroupsIds.matchedGroupIds
                    sqlRestriction(""" {alias}.USER_ASSIGNED in (${matchedUserIds ?: "''"}) OR {alias}.GROUP_ASSIGNED in (${matchedGroupIds ?: "''"}) """)
                }
                if(createdById){
                    Map usersAndGroupsIds = getUsersAndGroupsId(groupsList, usersList, createdById, false, true)
                    String matchedUserIds = usersAndGroupsIds.matchedUserIds
                    sqlRestriction(""" {alias}.USER_ASSIGNED in (${matchedUserIds ?: "''"})""")
                }
                if (hierarchyName) {
                    ilike('hierarchy', "%" + hierarchyName + "%")
                }
                if (nameString) {
                    sqlRestriction("""{alias}.id in (select assignment.id from user_view_assignment assignment, json_table(PRODUCTS,'\$[*]'
                                     columns(name VARCHAR2 path '\$.name')) t1 where UPPER(t1.name) LIKE UPPER('%${nameString}%'))""")
                }
            } else {
                or {
                    List matchedWFGroupIdsList = []
                    groupsList.collect { Group grp ->
                        if (grp.name.toUpperCase().contains(searchValue.toUpperCase()) && grp.groupType as GroupType == GroupType.WORKFLOW_GROUP) {
                            matchedWFGroupIdsList.add(grp.id)
                        }
                    }
                    String matchedWFGroupIds = matchedWFGroupIdsList.join(",")
                    sqlRestriction("""{alias}.WORKFLOW_GROUP in (${matchedWFGroupIds ?: "''"})""")

                    Map usersAndGroupsIds = getUsersAndGroupsId(groupsList, usersList, searchValue, true)
                    String matchedUserIds = usersAndGroupsIds.matchedUserIds
                    String matchedGroupIds = usersAndGroupsIds.matchedGroupIds
                    sqlRestriction(""" {alias}.USER_ASSIGNED in (${matchedUserIds ?: "''"}) OR {alias}.GROUP_ASSIGNED in (${matchedGroupIds ?: "''"}) """)

                    ilike('hierarchy', "%" + searchValue + "%")
                    if(prodIdList) {
                        'or' {
                            prodIdList.collate(999).each {
                                'in'('id', it)
                            }
                        }
                    }
                }
            }
            Map requestMap = params.order[0]
            List columns = params.columns
            String columnName = columns[requestMap['column']].data

            if(columnName == "hierarchy"){
                sqlRestriction(" 1=1 ORDER BY UPPER(HIERARCHY) ${requestMap.dir}")
            } else if(columnName == "product"){
                sqlRestriction(" 1=1 ORDER BY dbms_lob.substr(UPPER(PRODUCTS), dbms_lob.getlength(PRODUCTS), 1) ${requestMap.dir}")
            } else if(columnName == "workflowGroup"){
                List matchedWFGroupIds = getWorkflowGroupSorted(groupsList,requestMap.dir)
                sqlRestriction("""1=1 ORDER BY instr('${matchedWFGroupIds.join(",")}',workflow_group)""")
            } else if(columnName == "createdBy"){
                List<Long> matchedUserIds = getUsernameSorted(usersList,requestMap.dir)
                sqlRestriction("""1=1 ORDER BY instr('${matchedUserIds.join(",")}',user_assigned)""")
            } else if(columnName == "assignedUserOrGroup"){
                List matchedGroupIds = getGroupSorted(groupsList,requestMap.dir)
                List<Long> matchedUserIds = getFullNameSortedList(usersList,requestMap.dir)
                sqlRestriction("""1=1 ORDER BY instr('${matchedUserIds.join(",")}',{alias}.USER_ASSIGNED), instr('${matchedGroupIds.join(",")}',{alias}.group_assigned)""")
            } else {
                order("lastUpdated", "desc")
            }
        }

        return assignments
    }


    Map fetchValuesForUserView(Map params, DataTableSearchRequest searchRequest, Map dicMap, Map groupMap, Map userMap) {
        List assignments
        Integer filteredCount
        List totalUserViewAssignment = []
        List totalUserViewAssignmentList =[]
        Integer totalRecords
        List result = []
        List groupsList = []
        groupMap.each { groupsList.add(it.value) }
        List usersList = []
        userMap.each { usersList.add(it.value) }
        UserViewAssignment."pva".withTransaction {
            assignments = getMatchingUserViewAssignment(params, searchRequest, groupsList, usersList)
            totalUserViewAssignment = UserViewAssignment."pva".list()
            totalUserViewAssignment.each{
                UserViewAssignment productAssignment ->
                    Boolean isAssignmentPresent = false
                    if (productAssignment.userAssigned && userMap.get(productAssignment.userAssigned)) {
                        isAssignmentPresent = true
                    }
                    if (productAssignment.groupAssigned && groupMap.get(productAssignment.groupAssigned)) {
                        isAssignmentPresent = true
                    }
                    if(isAssignmentPresent){
                        totalUserViewAssignmentList.add(productAssignment)
                    }
            }
            totalRecords = totalUserViewAssignmentList?.size()
            assignments.each { UserViewAssignment productAssignment ->
                Boolean isAssignmentPresent = false
                if (productAssignment.userAssigned && userMap.get(productAssignment.userAssigned)) {
                    isAssignmentPresent = true
                }
                if (productAssignment.groupAssigned && groupMap.get(productAssignment.groupAssigned)) {
                    isAssignmentPresent = true
                }
                if (isAssignmentPresent) {
                    String productName = JSON.parse(productAssignment.getProductsClob()).name.join(", ")
                    String productNameDictionary = JSON.parse(productAssignment.getProductsClob()).name.join(" (${productAssignment.hierarchy}),<br> ") + " (${productAssignment.hierarchy})"
                    String productGroupAssessment = ""
                    String productAssessment = ""
                    if (productAssignment.hierarchy == "Product Group") {
                        productGroupAssessment = getMultipleProductGroupJSONFormat(productAssignment.getProductsClob())
                    } else {
                        productAssessment = getMultipleProductJSONFormat(productAssignment.getProductsClob(), productAssignment.hierarchy, dicMap)
                    }
                    List<Map> assignedUserOrGroupList = generateAssignedUserOrGroupList(productAssignment.userAssigned ? [productAssignment.userAssigned] : [], productAssignment.groupAssigned ? [productAssignment.groupAssigned] : [])
                    result.add(["hierarchy"            : productAssignment.hierarchy, "workflowGroup": (productAssignment.workflowGroup ? groupMap.get(productAssignment.workflowGroup)?.name : ""),
                                "createdBy"            : productAssignment.userAssigned ? userMap.get(productAssignment.userAssigned)?.username : "",
                                "product"              : productName,
                                "productNameDictionary": productNameDictionary,
                                "assignedUserOrGroup"  : assignedUserOrGroupList,
                                "assignmentNameString" : assignedUserOrGroupList.collect { it.name }.join(", "),
                                "action"               : productAssignment.id, "productGroupAssessment": productGroupAssessment,
                                "productAssessment"    : productAssessment])
                }
            }
            filteredCount = result? result.size(): 0
        }
        return [aaData:result,recordsFiltered: filteredCount, recordsTotal:totalRecords]
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    def deleteProductViewAssignment(Long id, Boolean isUpdate, Long wfGroup = null, Boolean isWfUpdate = false, List<Long> newCreatedUserViews,
                                    List usersList = [], List groupsList = [], List<BigInteger> productsId = [], Map dicMap = [:]) {
        Long tenantIdUser = Holders.config.product.assignment.user.view.tenantId
        ProductViewAssignment."pva".withTransaction {
            ProductViewAssignment productViewAssignment = ProductViewAssignment."pva".get(id)
            Map productMap = JSON.parse(productViewAssignment.product)
            BigInteger productId = productMap.id as BigInteger
            List users = productViewAssignment.usersAssigned
            String hierarchy = productViewAssignment.hierarchy
            Long workflowGroup = productViewAssignment.workflowGroup
            List groups = productViewAssignment.groupsAssigned
            users.each { Long user ->
                List <UserViewAssignment> matchedUserViewList = UserViewAssignment."pva".createCriteria().list {
                    eq("userAssigned", user)
                    eq("hierarchy", hierarchy)
                }

                if (matchedUserViewList) {
                    UserViewAssignment matchedUserView = matchedUserViewList[0]
                    if(matchedUserViewList.size()>1){
                        deleteDuplicateUserAssignment(productMap,matchedUserView.id,users,groups,workflowGroup,isWfUpdate,[productId],dicMap)
                    }
                    if(isWfUpdate){
                        matchedUserView.workflowGroup = wfGroup
                    }
                    List matchedProductList = JSON.parse(matchedUserView.getProductsClob())
                    List<BigInteger> matchedProductsId = []
                    matchedProductList.each{
                        matchedProductsId.add(it.id as BigInteger)
                    }
                    if (matchedProductList.size() > 1) {
                        List newProductList = []
                        matchedProductList.each {
                            if (it.id as BigInteger != productId) {
                                newProductList.add(it)
                            }
                        }
                        matchedUserView.products =  (newProductList as JSON).toString()
                        matchedUserView.isResolved = 'N' as Character
                        matchedUserView.tenantId = tenantIdUser
                        newCreatedUserViews.add(user)
                        matchedUserView.save()
                    } else if(!usersList.contains(user as Integer) || !matchedProductsId.intersect(productsId)){
                        callProcToDeleteUserViewAssignment(matchedUserView.id)
                    }
                }
            }

            groups.each { Long group ->
                List <UserViewAssignment> matchedUserViewList = UserViewAssignment."pva".createCriteria().list {
                    eq("groupAssigned", group)
                    eq("hierarchy", hierarchy)
                }
                if (matchedUserViewList) {
                    UserViewAssignment matchedUserView = matchedUserViewList[0]
                    if(matchedUserViewList.size()>1){
                        deleteDuplicateUserAssignment(productMap,matchedUserView.id,users,groups,workflowGroup,isWfUpdate,[productId],dicMap)
                    }
                    if(isWfUpdate){
                        matchedUserView.workflowGroup = wfGroup
                    }
                    List matchedProductList = JSON.parse(matchedUserView.getProductsClob())
                    List<BigInteger> matchedProductsId = []
                    matchedProductList.each{
                        matchedProductsId.add(it.id as BigInteger)
                    }
                    if (matchedProductList.size() > 1) {
                        List newProductList = []
                        matchedProductList.each {
                            if (it.id as BigInteger != productId) {
                                newProductList.add(it)
                            }
                        }
                        matchedUserView.products = (newProductList as JSON).toString()
                        matchedUserView.isResolved = 'N' as Character
                        matchedUserView.tenantId = tenantIdUser
                        matchedUserView.save()
                        newCreatedUserViews.add(group)
                    } else if(!groupsList.contains(group as Long) || !matchedProductsId.intersect(productsId)){
                        callProcToDeleteUserViewAssignment(matchedUserView.id)
                    }
                }
            }

            if (!isUpdate) {
                signalAuditLogService.deleteAuditLog(productViewAssignment)
                productViewAssignment.delete()
            }
        }
    }

    void callProcToDeleteUserViewAssignment(Long id){
        /**
         * Commented code due to creating new connection not taking from pool
        final Sql sql = new Sql(signalDataSourceService.getReportConnection("pva"))
         */
        Sql sql = null
        try {
            sql = new Sql(dataSource_pva)
            String proc = "begin pkg_user_view_resolve.p_delete_user_asgnmt_data('${id}'); end; "
            sql?.call(proc)
        } catch (Exception ex) {
            log.error("Call to p_delete_user_asgnmt_data failed.")
            ex.printStackTrace()
        } finally {
            if (Objects.nonNull(sql)) {
                sql.close()
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    def deleteUserViewAssignment(Long id, Boolean isUpdate, Long wfGroup = null, Boolean isWfUpdate = false, List<BigInteger> productsId =[],
                                List<BigInteger> deletedProductsIds = []) {
        Long tenantIdProduct = Holders.config.product.assignment.product.view.tenantId
        UserViewAssignment."pva".withTransaction {
            UserViewAssignment userViewAssignment = UserViewAssignment."pva".get(id)
            Long workflowGroupId = userViewAssignment.workflowGroup
            List productList = JSON.parse(userViewAssignment.getProductsClob())
            String hierarchy = userViewAssignment.hierarchy
            Long userAssigned = userViewAssignment.userAssigned
            Long groupAssigned = userViewAssignment.groupAssigned
            productList.each { productMap ->
                BigInteger productId = productMap.id as BigInteger
                if(!isUpdate || (isUpdate && deletedProductsIds.contains(productId))) {
                    List matchedProductViewsList = ProductViewAssignment."pva".createCriteria().list {
                        if (userAssigned) {
                            sqlRestriction("id in (select PRODUCT_ASSIGNMENT_ID from PRODUCT_ASSIGNMENT_USERS where USER_ID = ${userAssigned})")
                        } else if (groupAssigned) {
                            sqlRestriction("id in (select PRODUCT_ASSIGNMENT_ID from PRODUCT_ASSIGNMENT_GROUPS where GROUP_ID = ${groupAssigned})")
                        }
                        eq("hierarchy", hierarchy)
                        sqlRestriction("JSON_VALUE(product,'\$.id') = ${productId}")
                    }
                    matchedProductViewsList.each { ProductViewAssignment matchedProductView ->

                        if ((userAssigned && matchedProductView.usersAssigned.contains(userAssigned)) ||
                                (groupAssigned && matchedProductView.groupsAssigned.contains(groupAssigned))) {

                            List usersList = matchedProductView.usersAssigned
                            List groupsList = matchedProductView.groupsAssigned
                            String primaryUserOrGroupIdString = matchedProductView.primaryUserOrGroupId
                            Boolean isGroup = false
                            Boolean isExistingPrimUser = false
                            Integer index = 0
                            Long primaryUserOrGroupId = 0
                            if (primaryUserOrGroupIdString) {
                                isExistingPrimUser = true
                                isGroup = primaryUserOrGroupIdString.contains("UserGroup")
                                index = primaryUserOrGroupIdString.indexOf("_")
                                primaryUserOrGroupId = primaryUserOrGroupIdString.substring(index + 1, primaryUserOrGroupIdString.size()) as Long
                            }
                            if (isWfUpdate) {
                                matchedProductView.workflowGroup = wfGroup
                            }
                            if (userAssigned) {
                                if (usersList.size() > 1 || (usersList.size() == 1 && groupsList.size() > 0)) {
                                    List finalUserList = []
                                    usersList.each {
                                        if (it != userAssigned) {
                                            finalUserList.add(it)
                                        }
                                    }
                                    matchedProductView.usersAssigned = finalUserList
                                    if (isExistingPrimUser) {
                                        if (!isGroup && primaryUserOrGroupId == userAssigned && matchedProductView.usersAssigned.size() > 0) {
                                            String newPrimaryUser = "User_${matchedProductView.usersAssigned[0]}"
                                            matchedProductView.primaryUserOrGroupId = newPrimaryUser
                                        } else if (!isGroup && primaryUserOrGroupId == userAssigned && matchedProductView.groupsAssigned.size() > 0) {
                                            String newPrimaryGroup = "UserGroup_${matchedProductView.groupsAssigned[0]}"
                                            matchedProductView.primaryUserOrGroupId = newPrimaryGroup
                                        }
                                    } else {
                                        if (!isGroup && matchedProductView.usersAssigned.size() > 0) {
                                            String newPrimaryUser = "User_${matchedProductView.usersAssigned[0]}"
                                            matchedProductView.primaryUserOrGroupId = newPrimaryUser
                                        } else if (!isGroup && matchedProductView.groupsAssigned.size() > 0) {
                                            String newPrimaryGroup = "UserGroup_${matchedProductView.groupsAssigned[0]}"
                                            matchedProductView.primaryUserOrGroupId = newPrimaryGroup
                                        }
                                    }
                                    if (!matchedProductView.primaryUserOrGroupId.contains(Constants.USER_TOKEN)) {
                                        matchedProductView.workflowGroup = null
                                    }
                                    matchedProductView.tenantId = tenantIdProduct
                                    matchedProductView.save()
                                } else if (!groupsList && !productsId.contains(productId)) {
                                    matchedProductView.delete()
                                }
                            } else {
                                if (groupsList.size() > 1 || (groupsList.size() == 1 && usersList.size() > 0)) {
                                    List finalGroupList = []
                                    groupsList.each {
                                        if (it != groupAssigned) {
                                            finalGroupList.add(it)
                                        }
                                    }
                                    matchedProductView.groupsAssigned = finalGroupList
                                    if (isExistingPrimUser) {
                                        if (isGroup && primaryUserOrGroupId == groupAssigned && matchedProductView.groupsAssigned.size() > 0) {
                                            String newPrimaryGroup = "UserGroup_${matchedProductView.groupsAssigned[0]}"
                                            matchedProductView.primaryUserOrGroupId = newPrimaryGroup
                                        } else if (isGroup && primaryUserOrGroupId == groupAssigned && matchedProductView.usersAssigned.size() > 0) {
                                            String newPrimaryUser = "User_${matchedProductView.usersAssigned[0]}"
                                            matchedProductView.primaryUserOrGroupId = newPrimaryUser
                                        }
                                    } else {
                                        if (isGroup && matchedProductView.groupsAssigned.size() > 0) {
                                            String newPrimaryGroup = "UserGroup_${matchedProductView.groupsAssigned[0]}"
                                            matchedProductView.primaryUserOrGroupId = newPrimaryGroup
                                        } else if (isGroup && matchedProductView.usersAssigned.size() > 0) {
                                            String newPrimaryUser = "User_${matchedProductView.usersAssigned[0]}"
                                            matchedProductView.primaryUserOrGroupId = newPrimaryUser
                                        }
                                    }
                                    if (!matchedProductView.primaryUserOrGroupId.contains(Constants.USER_TOKEN)) {
                                        matchedProductView.workflowGroup = null
                                    }
                                    matchedProductView.tenantId = tenantIdProduct
                                    signalAuditLogService.updateAuditLog(matchedProductView)
                                    matchedProductView.save()
                                } else if (!usersList && !productsId.contains(productId)) {
                                    matchedProductView.delete()
                                }
                            }
                        }
                    }
                }
            }
            if (!isUpdate) {
                callProcToDeleteUserViewAssignment(userViewAssignment.id)
            }
        }
    }

    List getMatchingProductViewAssignmentSearch(Map params, List groupsList, List usersList) {

        String searchValue

        if(params.search){
            searchValue = params.search
        }

        List assignments = []

        assignments = ProductViewAssignment."pva".createCriteria().list (){

            or {
                List matchedWFGroupIdsList = []
                groupsList.collect { Group grp ->
                    if (grp.name.toUpperCase().contains(searchValue.toUpperCase()) && grp.groupType as GroupType == GroupType.WORKFLOW_GROUP) {
                        matchedWFGroupIdsList.add(grp.id)
                    }
                }
                String matchedWFGroupIds = matchedWFGroupIdsList.join(",")
                sqlRestriction("""{alias}.WORKFLOW_GROUP in (${matchedWFGroupIds ?: "''"})""")

                Map usersAndGroupsIds = getUsersAndGroupsId(groupsList, usersList, searchValue, true)
                String matchedUserIds = usersAndGroupsIds.matchedUserIds
                String matchedGroupIds = usersAndGroupsIds.matchedGroupIds
                sqlRestriction(""" {alias}.id in
                        (
                            (select PRODUCT_ASSIGNMENT_ID from PRODUCT_ASSIGNMENT_USERS where USER_ID
                            in (${matchedUserIds ?: "''"}))
                            UNION
                            (select PRODUCT_ASSIGNMENT_ID from PRODUCT_ASSIGNMENT_GROUPS where GROUP_ID
                            in (${matchedGroupIds ?: "''"}))
                        )
                        """)

                ilike('hierarchy', "%" + searchValue + "%")
                sqlRestriction("UPPER(JSON_VALUE(product,'\$.name')) LIKE UPPER('%${searchValue}%')")
            }

        }

        return assignments

    }

    List getMatchingProductViewAssignmentFilter(Map params, List groupsList, List usersList) {
        String createdById = params.createdBy
        String workflowGroupName = params.workflowGroup
        String assignmentNameString = params.assignedUserOrGroup
        String hierarchyName = params.hierarchy
        String nameString = params.product

        List assignments = ProductViewAssignment."pva".createCriteria().list {
            if (workflowGroupName) {
                List matchedWFGroupIdsList = []
                groupsList.collect { Group grp ->
                    if (grp.name.toUpperCase().contains(workflowGroupName.toUpperCase()) && grp.groupType as GroupType == GroupType.WORKFLOW_GROUP) {
                        matchedWFGroupIdsList.add(grp.id)
                    }
                }
                String matchedWFGroupIds = matchedWFGroupIdsList.join(",")
                sqlRestriction("""{alias}.WORKFLOW_GROUP in (${matchedWFGroupIds ?: "''"})""")
            }
            if (assignmentNameString) {
                Map usersAndGroupsIds = getUsersAndGroupsId(groupsList, usersList, assignmentNameString, false)
                String matchedUserIds = usersAndGroupsIds.matchedUserIds
                String matchedGroupIds = usersAndGroupsIds.matchedGroupIds
                sqlRestriction(""" {alias}.id in
                        (
                            (select PRODUCT_ASSIGNMENT_ID from PRODUCT_ASSIGNMENT_USERS where USER_ID
                            in (${matchedUserIds ?: "''"}))
                            UNION
                            (select PRODUCT_ASSIGNMENT_ID from PRODUCT_ASSIGNMENT_GROUPS where GROUP_ID
                            in (${matchedGroupIds ?: "''"}))
                        )
                        """)
            }
            if (hierarchyName) {
                ilike('hierarchy', "%" + hierarchyName + "%")
            }
            if (nameString) {
                sqlRestriction("UPPER(JSON_VALUE(product,'\$.name')) LIKE UPPER('%${nameString}%')")
            }
            order("lastUpdated", "desc")
        }
        if (createdById) {
            List userId = []
            usersList.each { User user ->
                if (user.username && user.username?.toUpperCase()?.contains(createdById.toUpperCase())) {
                    userId.add(user.id)
                }
            }
            assignments = assignments.findAll { ProductViewAssignment productViewAssignment ->
                productViewAssignment.usersAssigned.intersect(userId)
            }
        }
        return assignments
    }

    List getMatchingUserViewAssignmentSearch(Map params, List groupsList, List usersList){

        String searchValue

        List<Long> prodIdList = []
        if(params.search){
            searchValue = params.search
            Sql sql = new Sql(dataSource_pva)
            sql.eachRow(SignalQueryHelper.user_view_product_search_sql(searchValue),[]){ row ->
                prodIdList.add(row[0] as Long)
            }
        }

        List assignments = UserViewAssignment."pva".createCriteria().list () {
            resultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)

            or {
                List matchedWFGroupIdsList = []
                groupsList.collect { Group grp ->
                    if (grp.name.toUpperCase().contains(searchValue.toUpperCase()) && grp.groupType as GroupType == GroupType.WORKFLOW_GROUP) {
                        matchedWFGroupIdsList.add(grp.id)
                    }
                }
                String matchedWFGroupIds = matchedWFGroupIdsList.join(",")
                sqlRestriction("""{alias}.WORKFLOW_GROUP in (${matchedWFGroupIds ?: "''"})""")

                Map usersAndGroupsIds = getUsersAndGroupsId(groupsList, usersList, searchValue, true)
                String matchedUserIds = usersAndGroupsIds.matchedUserIds
                String matchedGroupIds = usersAndGroupsIds.matchedGroupIds
                sqlRestriction(""" {alias}.USER_ASSIGNED in (${matchedUserIds ?: "''"}) OR {alias}.GROUP_ASSIGNED in (${matchedGroupIds ?: "''"}) """)

                ilike('hierarchy', "%" + searchValue + "%")
                if(prodIdList) {
                    'or' {
                        prodIdList.collate(999).each {
                            'in'('id', it)
                        }
                    }
                }
            }


        }

        return assignments
    }

    List getMatchingUserViewAssignmentFilter(Map params, List groupsList, List usersList) {
        String createdById = params.createdBy
        String workflowGroupName = params.workflowGroup
        String assignmentNameString = params.assignedUserOrGroup
        String hierarchyName = params.hierarchy
        String nameString = params.product
        List assignments = UserViewAssignment."pva".createCriteria().list {
            resultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
            or {
                isNotNull("userAssigned")
                isNotNull("groupAssigned")
            }
            if (workflowGroupName) {
                List matchedWFGroupIdsList = []
                groupsList.collect { Group grp ->
                    if (grp.name && grp.name.toUpperCase().contains(workflowGroupName.toUpperCase()) && grp.groupType as GroupType == GroupType.WORKFLOW_GROUP) {
                        matchedWFGroupIdsList.add(grp.id)
                    }
                }
                String matchedWFGroupIds = matchedWFGroupIdsList.join(",")
                sqlRestriction("""{alias}.WORKFLOW_GROUP in (${matchedWFGroupIds ?: "''"})""")
            }
            if (assignmentNameString) {
                Map usersAndGroupsIds = getUsersAndGroupsId(groupsList, usersList, assignmentNameString, false)
                String matchedUserIds = usersAndGroupsIds.matchedUserIds
                String matchedGroupIds = usersAndGroupsIds.matchedGroupIds
                sqlRestriction(""" {alias}.USER_ASSIGNED in (${matchedUserIds ?: "''"}) OR {alias}.GROUP_ASSIGNED in (${matchedGroupIds ?: "''"}) """)
            }
            if (hierarchyName) {
                ilike('hierarchy', "%" + hierarchyName + "%")
            }
            if (nameString) {
                sqlRestriction("""{alias}.id in (select assignment.id from user_view_assignment assignment, json_table(PRODUCTS,'\$[*]'
                                 columns(name VARCHAR2 path '\$.name')) t1 where UPPER(t1.name) LIKE UPPER('%${nameString}%'))""")
            }
            order("lastUpdated", "desc")
        }
        if (createdById) {
            List userId = []
            usersList.each { User user ->
                if (user.username && user.username?.toUpperCase()?.contains(createdById.toUpperCase())) {
                    userId.add(user.id)
                }
            }
            assignments = assignments.findAll { UserViewAssignment userViewAssignment ->
                userId.contains(userViewAssignment.userAssigned)
            }
        }
        return assignments
    }

    def exportAssignment(Map params, List groupsList, List usersList) {
        List<Map> result = []
        Map groupsMap = cacheService.getAllGroups()
        Map usersMap = cacheService.getAllUsers()
        String timeZone = userService.getUser()?.preference?.timeZone
        if (params.isProductView == "true") {
            ProductViewAssignment."pva".withTransaction {
                if(params.search){
                    List matchedAssignments = getMatchingProductViewAssignmentSearch(params, groupsList, usersList)
                    result = matchedAssignments.collect { ProductViewAssignment productViewAssignment -> productViewAssignment.toExportDto(groupsMap, usersMap, timeZone) }
                }
                else{
                    if (params.isFilter == "true") {
                        List matchedAssignments = getMatchingProductViewAssignmentFilter(params, groupsList, usersList)
                        result = matchedAssignments.collect { ProductViewAssignment productViewAssignment -> productViewAssignment.toExportDto(groupsMap, usersMap, timeZone) }
                    } else {
                        List productViewAssignmentList = ProductViewAssignment."pva".createCriteria().list {
                            order("lastUpdated", "desc")
                        }
                        result = productViewAssignmentList.collect { ProductViewAssignment productViewAssignment -> productViewAssignment.toExportDto(groupsMap, usersMap, timeZone) }
                    }
                }
            }
        } else {
            UserViewAssignment."pva".withTransaction {
                if(params.search){
                    List matchedAssignments = getMatchingUserViewAssignmentSearch(params, groupsList, usersList)
                    result = matchedAssignments.collect { UserViewAssignment userViewAssignment -> userViewAssignment.toExportDto(groupsMap, usersMap, timeZone) }
                }
                else {
                    if (params.isFilter == "true") {
                        List matchedAssignments = getMatchingUserViewAssignmentFilter(params, groupsList, usersList)
                        result = matchedAssignments.collect { UserViewAssignment userViewAssignment -> userViewAssignment.toExportDto(groupsMap, usersMap, timeZone) }
                    } else {
                        List productViewAssignmentList = UserViewAssignment."pva".createCriteria().list {
                            or {
                                isNotNull("userAssigned")
                                isNotNull("groupAssigned")
                            }
                            order("lastUpdated", "desc")
                        }
                        productViewAssignmentList.each { UserViewAssignment userViewAssignment ->
                            if ((userViewAssignment.userAssigned && usersMap.get(userViewAssignment.userAssigned)) ||
                                    (userViewAssignment.groupAssigned && groupsMap.get(userViewAssignment.groupAssigned))) {
                                result.add(userViewAssignment.toExportDto(groupsMap, usersMap, timeZone))
                            }
                        }
                    }
                }
            }
        }
        return result

    }

    def prepareDataForCRUDOperation(String selectedUserOrGroup, String selectedProducts, String selectedProductGroups) {
        Map products = [:]
        List assignmentList = JSON.parse(selectedUserOrGroup)
        List productGroups = []
        if (selectedProducts) {
            products = JSON.parse(selectedProducts)
        }
        if (selectedProductGroups) {
            List productGroupSelection = JSON.parse(selectedProductGroups)
            productGroupSelection.each { prdMap ->
                String prdId = prdMap.id
                prdMap.name = prdMap.name
                productGroups.add(prdMap)
            }
        }
        products.put("0", productGroups)
        return ["assignmentList":assignmentList,"products":products]
    }

    void populateUnassignedProductsInBackground(String hierarchy) {
        cacheService.setUnassignedProductsCache(hierarchy)
        notify 'populate.unassigned.products',[hierarchy: hierarchy]
    }

    void populateUnassignedProducts(String hierarchy) {
        try {
            List<Map> productNameList = fetchUnassignedProducts(hierarchy)
            List<ProductViewAssignment> productViewAssignmentList = prepareProductAssignmentList(hierarchy, productNameList)
            batchPersistProductAssignment(productViewAssignmentList, ProductViewAssignment,false)
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            cacheService.clearUnassignedProductsCache(hierarchy)
        }
    }

    List<Map> fetchUnassignedProducts(String hierarchy) {
        Sql sql = new Sql(dataSource_pva)
        List<Map> productNameList = []
        if(hierarchy == 'Product Group'){
            sql.eachRow("Select col_1, col_2 from DISP_VIEW_199 where col_1 not in (Select JSON_VALUE(product,'\$.id') from PRODUCT_VIEW_ASSIGNMENT where hierarchy = '$hierarchy') and lang_id = 'en'", []) { row ->
                productNameList.add([name: row.col_2 as String, id: row.col_1 as String])
            }
        } else {
            Integer hierarchyKey = PVDictionaryConfig.ProductConfig.views.find {
                messageSource.getMessage(it.code, null, Locale.default) == hierarchy
            }.index
            String tableName = "DISP_VIEW_${200 + hierarchyKey}"
            sql.eachRow("Select col_1, col_2 from $tableName where col_1 not in (Select JSON_VALUE(product,'\$.id') from PRODUCT_VIEW_ASSIGNMENT where hierarchy = '$hierarchy') and lang_id = 'en'", []) { row ->
                productNameList.add([name: row.col_2 as String, id: row.col_1 as String])
            }
        }
        productNameList
    }

    List<ProductViewAssignment> prepareProductAssignmentList(String hierarchy, List<Map> productNameList) {
        Long tenantId = Holders.config.product.assignment.product.view.tenantId
        List<ProductViewAssignment> productViewAssignmentList = productNameList.collect {
            new ProductViewAssignment(hierarchy: hierarchy, product: new JsonBuilder(it).toString(), tenantId: tenantId, dateCreated: new Date(),
                    lastUpdated: new Date())
        }
        productViewAssignmentList
    }

    void batchPersistProductAssignment(List domainList, Class domainClz,Boolean isExcelImport=false, Map processedRecords = [:],List<ProductViewAssignment> updateAuditBatchPVA = [], List<ProductViewAssignment> createAuditBatchPVA = []) throws Exception {
        Integer batchSize = Holders.config.signal.batch.size as Integer
        domainClz."pva".withTransaction {
            List batch = []
            domainList.eachWithIndex { def domain, Integer index ->
                batch += domain
                try{
                    domain.save(validate: true)
                    if(!isExcelImport){
                        signalAuditLogService.saveAuditLog(domain)
                    } else {
                        if(updateAuditBatchPVA.contains(domain)){
                            signalAuditLogService.updateAuditLog(domain)
                        } else if(createAuditBatchPVA.contains(domain)){
                            signalAuditLogService.saveAuditLog(domain)
                        }
                    }
                    if (index.mod(batchSize) == 0) {
                        Session session = sessionFactory_pva.currentSession
                        session.flush()
                        session.clear()
                        batch.clear()
                    }
                } catch (ValidationException ve){
                    log.error("Validation Error While Saving Product Assignment: ${domain.alertGroup}, ${domain.product}, ${domain.hierarchy}, ${domain.primaryUserOrGroupId}")
                    moveTheAssignmentToFailure(domain,processedRecords)
                    ve.printStackTrace()
                } catch (Exception ex) {
                    log.error("Batch Persist Failed")
                    ex.printStackTrace()
                    throw ex
                }
            }
            if (batch) {
                Session session = sessionFactory_pva.currentSession
                session.flush()
                session.clear()
                batch.clear()
            }
        }
        log.info("Data is persisted.")
    }

    void notifyForPGUpdate(Map productGroupSelection) {
        notify 'update.product.group', productGroupSelection
        return
    }

    void updatePGSelection(Map productGroupDetails) {
        try {
            updateDomain('EMERGING_ISSUE', productGroupDetails)
            log.info("Successfully updated EMERGING_ISSUE table after product group update")
            updateDomain('RCONFIG', productGroupDetails)
            log.info("Successfully updated RCONFIG table after product group update")
            updateDomain('VALIDATED_SIGNAL', productGroupDetails)
            log.info("Successfully updated VALIDATED_SIGNAL table after product group update")
            updateDomain('BUSINESS_CONFIGURATION', productGroupDetails)
            log.info("Successfully updated BUSINESS_CONFIGURATION table after product group update")

        } catch (Exception ex) {
            log.error(ex.getMessage())
            throw ex
        } finally {
            updatePvsAppConfigForPGUpdate(true)
        }
    }

    void updateDomain(String domain, Map productGroupDetails) {
        Sql sql = null
        try {
            sql = new Sql(dataSource)
            String pgSelection
            String updatedPGSelection
            Integer id
            Map<String, ArrayList> pgSelectionAndID = [:]
            StringBuilder executeStatement = new StringBuilder()
            executeStatement.append("BEGIN;")
            String updateStatement = " UPDATE ${domain} SET PRODUCT_GROUP_SELECTION = '"
            String selectStatement =  '%(' + productGroupDetails.id.toString() + ')%'
            String statement = "SELECT ID, PRODUCT_GROUP_SELECTION FROM ${domain} WHERE PRODUCT_GROUP_SELECTION LIKE '${selectStatement}'"
            sql.eachRow(statement) { ResultSet resultSet ->
                id = resultSet.getInt("ID")
                if (domain == "EMERGING_ISSUE") {
                    Clob clob = resultSet.getClob("PRODUCT_GROUP_SELECTION")
                    pgSelection = (clob?.getSubString(1, (int) clob.length()))
                } else {
                    pgSelection = resultSet.getString("PRODUCT_GROUP_SELECTION")
                }
                updatedPGSelection = updateProductGroupSelection(pgSelection, productGroupDetails)
                if (!pgSelectionAndID.get(updatedPGSelection)) {
                    pgSelectionAndID.put(updatedPGSelection, new ArrayList())
                }
                pgSelectionAndID.get(updatedPGSelection).add(id)
            }
            pgSelectionAndID.keySet().each {key ->
                pgSelectionAndID[key].collate(999).each {
                    executeStatement.append(updateStatement)
                    executeStatement.append(key)
                    executeStatement.append("' WHERE ID IN (")
                    executeStatement.append(it.toString().replace("[", "").replace("]", "") + "); \n")
                }
            }
            executeStatement.append(" COMMIT;")
            sql.executeUpdate(executeStatement.toString())

        } catch (Exception ex) {
            log.error(ex.getMessage())
            throw ex
        } finally {
            sql.close()
        }
    }

    String updateProductGroupSelection(String productGroupSelection, Map productGroupDetails) {
        try {
            JSONArray productSelectionArray = new JSONArray(productGroupSelection)
            productSelectionArray.eachWithIndex { JSONObject JsonObject, Integer index ->
                JSONObject productGroup = new JSONObject(JsonObject)
                if ((productGroup.get('id') as Integer) == productGroupDetails.id) {
                    String updatedName = productGroupDetails.name + ' (' + (productGroupDetails.id as String) + ')'
                    productGroup.put('name', updatedName)
                    productSelectionArray.put(index, productGroup)
                }
            }
            return productSelectionArray.toString()
        } catch (Exception ex) {
            log.error(ex.getMessage())
            throw ex
        }
    }

    void updatePvsAppConfigForPGUpdate(boolean isUpdated) {
        PvsAppConfiguration pvsAppConfiguration = PvsAppConfiguration.findByKey(Constants.PRODUCT_GROUP_UPDATE)
        pvsAppConfiguration?.booleanValue = isUpdated
        pvsAppConfiguration.skipAudit=true
        CRUDService.updateWithAuditLog(pvsAppConfiguration)
        log.info("Status of pvs app configuration for product group update is: " + pvsAppConfiguration.booleanValue)
    }

    List uniqueAlertGroups() {
        List distinctAlertGroups = AlertGroup.createCriteria().list() {
            projections{
                property("id")
                property("name")
            }
        }.collect { [id: it[0], name: it[1]] }

        return distinctAlertGroups
    }

    void moveTheAssignmentToFailure(ProductViewAssignment domain, Map processedRecords) {
        try {
            def recordsCreated = processedRecords.recordsCreated
            def recordsFailed = processedRecords.recordsFailed
            def domainProduct = new JsonSlurper().parseText(domain.product)
            println domainProduct

// Compare each entry in recordsCreated with the domain object
            recordsCreated.each { record ->
                if (record.alertGroup == AlertGroup.get(domain.alertGroup).name &&
                        record.hierarchy == domain.hierarchy &&
                        record.workflowGroupId == domain.workflowGroup &&
                        record.product == domainProduct.name) {
                    recordsFailed << record
                }
                log.warn("Following Record Failed: ${record}")
            }

// Remove matched records from recordsCreated
            processedRecords.recordsCreated = recordsCreated.findAll { record ->
                !(record.alertGroup == AlertGroup.get(domain.alertGroup as Long).name &&
                        record.hierarchy == domain.hierarchy &&
                        record.workflowGroupId == domain.workflowGroup &&
                        record.product == domainProduct.name)
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        }
    }

}
