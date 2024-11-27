package com.rxlogix

import com.rxlogix.cache.CacheService
import com.rxlogix.config.ArchivedEmbaseLiteratureAlert
import com.rxlogix.config.ArchivedEvdasAlert
import com.rxlogix.config.ArchivedLiteratureAlert
import com.rxlogix.config.Disposition
import com.rxlogix.config.DispositionRule
import com.rxlogix.config.EmbaseLiteratureAlert
import com.rxlogix.config.EvdasAlert
import com.rxlogix.config.LiteratureAlert
import com.rxlogix.dto.AlertLevelDispositionDTO
import com.rxlogix.dto.UpdateDispCountsDTO
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.SystemConfig
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.SignalQueryHelper
import grails.events.EventPublisher
import grails.gorm.transactions.Transactional
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.sql.Sql

@Transactional
class DispositionService implements EventPublisher {
    def userService
    def messageSource
    def CRUDService
    def notificationHelper
    CacheService cacheService
    def dataSource
    def alertService

    def saveDisposition(dispositionInstance) {
        dispositionInstance.validate()
        !dispositionInstance.hasErrors() && dispositionInstance.save()
    }

    def getDispositionListByDisplayName() {
        return Disposition.list()?.collect { it.displayName }
    }

    def getDispositionFromDisplayName(dispositionValue) {
        List dispositionList = []
        if (dispositionValue) {
            dispositionList = Disposition.findAllByDisplayNameInList(dispositionValue as List)
        }
        dispositionList
    }

    List<Map> fetchAllowedDispositionsFromCurrentDisposition(Disposition disposition) {
        DispositionRule.findAllByIncomingDispositionAndDisplay(disposition, true)*.toDto() as List<Map>
    }

    List<Map> listDispositionAdvancedFilter() {
        Map<Long,Disposition> dispositionMap = cacheService.getDispositionCacheMap()
        List<Map> dispositionList = []
        dispositionMap.each {
            if(it.value.display){
                dispositionList.add([id:it.key,text:it.value.displayName])
            }

        }
        dispositionList
    }

    List<Map> listAlertDispositions() {
        userService.user.workflowGroup?.alertDispositions?.collect {
            [id                : it.id, colorCode: it.colorCode, abbreviation: it.abbreviation, displayName: it.displayName,
             validatedConfirmed: it.validatedConfirmed]
        }
    }

    List<Map> listUserAlertDispositions(User currentUser) {
        currentUser.workflowGroup?.alertDispositions?.collect {
            [id                : it.id, colorCode: it.colorCode, abbreviation: it.abbreviation, displayName: it.displayName,
             validatedConfirmed: it.validatedConfirmed]
        }
    }
    AlertLevelDispositionDTO populateAlertLevelDispositionDTO(Disposition targetDisposition, String justificationText, def domain, def execConfig = null, def config = null) {
        AlertLevelDispositionDTO alertLevelDispositionDTO = new AlertLevelDispositionDTO()
        alertLevelDispositionDTO.domainName = domain
        alertLevelDispositionDTO.justificationText = justificationText
        if(domain in [EvdasAlert, ArchivedEvdasAlert]){
            alertLevelDispositionDTO.evdasalertConfiguration = execConfig
            alertLevelDispositionDTO.configId = config.id
        } else {
            alertLevelDispositionDTO.execConfig = execConfig
        }
        alertLevelDispositionDTO.execConfigId = execConfig?.id
        alertLevelDispositionDTO.targetDisposition = targetDisposition
        if((domain in [LiteratureAlert, ArchivedLiteratureAlert, EmbaseLiteratureAlert, ArchivedEmbaseLiteratureAlert]) && config != null)
            alertLevelDispositionDTO.execConfigId = config
        alertLevelDispositionDTO
    }

    def sendDispChangeNotification(Disposition targetDisposition, String alertName){
        InboxLog inboxLog = new InboxLog(type: "Disposition Change", createdOn: new Date(),
                subject: "Alert level dispositions applied on '${alertName}'",
                content: "All the safety observations pending for review in the '${alertName}' are moved to disposition '${targetDisposition}'",
                inboxUserId: userService.user?.id, isNotification: true)
        inboxLog.save(flush:true)
        notificationHelper.pushNotification(inboxLog)
    }

    List<String> getReviewCompletedDispositionList() {
        Disposition.withCriteria {
            projections {
                property("displayName")
            }
            'eq'("reviewCompleted", true)
        } as List<String>
    }

    void removeDispCountsInBackground(Long dispositionId) {
        List<UserDashboardCounts> userDashboardCountsList = UserDashboardCounts.createCriteria().list {
            'or'{
                sqlRestriction("JSON_EXISTS(user_disp_case_counts,'\$.\"${dispositionId.toString()}\"')")
                sqlRestriction("JSON_EXISTS(GROUP_DISP_CASE_COUNTS,'\$.*.\"${dispositionId.toString()}\"')")
                sqlRestriction("JSON_EXISTS(user_disppecounts,'\$.\"${dispositionId.toString()}\"')")
                sqlRestriction("JSON_EXISTS(group_disppecounts,'\$.*.\"${dispositionId.toString()}\"')")

            }
        } as List<UserDashboardCounts>
        if(userDashboardCountsList) {
            notify 'remove.disp.counts', [userDashboardCountsList:userDashboardCountsList,dispositionId:dispositionId]
        }
    }

    void removeDispCountsFromDashboardCounts(List<UserDashboardCounts> userDashboardCountsList, Long dispositionId) {
        log.info("Updating counts in background")
        Sql sql = new Sql(dataSource)
        try{
            UpdateDispCountsDTO dispCountDTO = new UpdateDispCountsDTO()
            log.info("Dashboard Query Starts")
            generateDueDateCountsList(sql, dispositionId, dispCountDTO)
            log.info("Dashboard Query Ends")
            userDashboardCountsList.each {
                User user = cacheService.getUserByUserId(it.userId)
                Group workflowgroup = user.workflowGroup
                //For Single Case Alert
                updateDispCountsKey(dispCountDTO,true)
                updateCaseCounts(user,workflowgroup,dispCountDTO, dispositionId, it)
                //For Aggregate Case Alert
                updateDispCountsKey(dispCountDTO,false)
                updateCaseCounts(user, workflowgroup, dispCountDTO, dispositionId, it)
                it.save()
            }
            updateReviewCountsForAllExecutedConfigurations(sql, "EX_RCONFIG")
            updateReviewCountsForAllExecutedConfigurations(sql, "EX_EVDAS_CONFIG")
            updateLiteratureConfigurations(sql)
            log.info("Updation completes")
        }catch(Exception ex){
            ex.printStackTrace()
        }finally{
            sql?.close()
        }
    }

    void generateDueDateCountsList(Sql sql, Long dispositionId, UpdateDispCountsDTO dispCountDTO) {
        sql.eachRow(SignalQueryHelper.singleCaseAlert_dashboard_due_date(true, dispositionId), []) { row ->
            dispCountDTO.userDueDateCaseCountsList.add([due_date: row[0], assignedToId: row[1], workflowGroupId: row[2], count: row[3]])
        }

        sql.eachRow(SignalQueryHelper.singleCaseAlert_dashboard_due_date(false, dispositionId), []) { row ->
            dispCountDTO.dueDateGroupCaseCountList.add([due_date: row[0], assignedToGroupId: row[1], workflowGroupId: row[2], count: row[3]])
        }

        sql.eachRow(SignalQueryHelper.aggCaseAlert_dashboard_due_date(true, dispositionId), []) { row ->
            dispCountDTO.userDueDatePECountsList.add([due_date: row[0], assignedToId: row[1], workflowGroupId: row[2], count: row[3]])
        }

        sql.eachRow(SignalQueryHelper.aggCaseAlert_dashboard_due_date(false, dispositionId), []) { row ->
            dispCountDTO.dueDateGroupPECountList.add([due_date: row[0], assignedToGroupId: row[1], workflowGroupId: row[2], count: row[3]])
        }
    }

    void addDispCountsToDashboardCounts(Long dispositionId) {
        Sql sql = new Sql(dataSource)
        try{
            UpdateDispCountsDTO dispCountDTO = new UpdateDispCountsDTO()
            log.info("Generate Dashboard Count Starts")
            generateDueDateCountsList(sql, dispositionId, dispCountDTO)
            generateDashboardCountsList(sql, dispositionId, dispCountDTO)
            log.info("Generate Dashboard Count Ends")
            List<UserDashboardCounts> userDashboardCountsList = prepareUserDashboardCountList(dispCountDTO)
            userDashboardCountsList.each {
                User user = cacheService.getUserByUserId(it.userId)
                Group workflowgroup = user.workflowGroup
                List<Long> groupIdList = user.groups.findAll { it.groupType != GroupType.WORKFLOW_GROUP }.id

                updateUserDispCounts(it, workflowgroup.id, dispCountDTO.userDispCaseCountList,Constants.UserDashboardCounts.USER_DISP_CASE_COUNTS)
                updateUserDispCounts(it, workflowgroup.id, dispCountDTO.userDispPECountList, Constants.UserDashboardCounts.USER_DISP_PECOUNTS)
                updateUserDueDateCounts(it, workflowgroup.id, dispCountDTO.userDueDateCaseCountsList, Constants.UserDashboardCounts.USER_DUE_DATE_CASE_COUNTS)
                updateUserDueDateCounts(it, workflowgroup.id, dispCountDTO.userDueDatePECountsList, Constants.UserDashboardCounts.USER_DUE_DATE_PECOUNTS)

                groupIdList.each { id ->
                    updateGroupDispCounts(it, id, workflowgroup.id, dispCountDTO.groupDispCaseCountList, Constants.UserDashboardCounts.GROUP_DISP_CASE_COUNTS)
                    updateGroupDispCounts(it, id, workflowgroup.id, dispCountDTO.groupDispPECountList, Constants.UserDashboardCounts.GROUP_DISP_PECOUNTS)
                    updateGroupDueDateCounts(it, id, workflowgroup.id, dispCountDTO.dueDateGroupCaseCountList, Constants.UserDashboardCounts.GROUP_DUE_DATE_CASE_COUNTS)
                    updateGroupDueDateCounts(it, id, workflowgroup.id, dispCountDTO.dueDateGroupPECountList, Constants.UserDashboardCounts.GROUP_DUE_DATE_PECOUNTS)
                }
                it.save()
            }
            updateReviewCountsForAllExecutedConfigurations(sql, "EX_RCONFIG")
            updateReviewCountsForAllExecutedConfigurations(sql, "EX_EVDAS_CONFIG")
            updateLiteratureConfigurations(sql)
        }catch(Exception ex){
            ex.printStackTrace()
        }finally{
            sql?.close()
        }

    }

    void updateGroupDispCounts(UserDashboardCounts userDashboardCounts, Long groupId, Long workflowGroupId, List<Map> groupDispCountList, String groupDispCountKey) {
        Map dispCountMap = [:]
        Map<String, Map> groupDispCountsMap = [:]
        groupDispCountList.findAll { it.assignedToGroupId == groupId && it.workflowGroupId == workflowGroupId }.each {
            dispCountMap.put(it.dispositionId as String, it.count)
        }
        if (dispCountMap) {
            Map prevGroupDispCountsMap = userDashboardCounts."$groupDispCountKey" ? new JsonSlurper().parseText(userDashboardCounts."$groupDispCountKey") as Map : [:]
            if (prevGroupDispCountsMap.containsKey(groupId.toString()) && dispCountMap) {
                groupDispCountsMap.put(groupId.toString(), alertService.mergeCountMaps(dispCountMap, prevGroupDispCountsMap.get(groupId.toString()) as Map))
            } else if (dispCountMap) {
                groupDispCountsMap.put(groupId.toString(), dispCountMap)
            }
        }
        userDashboardCounts."$groupDispCountKey" = groupDispCountsMap ? new JsonBuilder(groupDispCountsMap).toPrettyString() : userDashboardCounts."$groupDispCountKey"
    }

    void updateGroupDueDateCounts(UserDashboardCounts userDashboardCounts,Long groupId, Long workflowGroupId, List<Map> dueDateGroupCaseCountList, String groupDueDateCountKey) {
        Map dueDateCountMap = [:]
        Map<String, Map> dueDateGroupCountsMap = [:]
        dueDateGroupCaseCountList.findAll { it.assignedToGroupId == groupId && it.workflowGroupId == workflowGroupId }.each {
            dueDateCountMap.put(it.due_date, it.count)
        }
        if(dueDateCountMap){
            Map groupDueDateCounts = userDashboardCounts."$groupDueDateCountKey" ? new JsonSlurper().parseText(userDashboardCounts."$groupDueDateCountKey") as Map : [:]
            if (groupDueDateCounts.containsKey(groupId.toString())) {
                dueDateGroupCountsMap.put(groupId.toString(), alertService.mergeCountMaps(dueDateCountMap, groupDueDateCounts.get(groupId.toString()) as Map))
            } else {
                dueDateGroupCountsMap.put(groupId.toString(), dueDateCountMap)
            }
        }
        userDashboardCounts."$groupDueDateCountKey" = dueDateGroupCountsMap ? new JsonBuilder(dueDateGroupCountsMap).toPrettyString() : userDashboardCounts."$groupDueDateCountKey"
    }

    void updateUserDispCounts(UserDashboardCounts userDashboardCounts, Long workflowGroupId, List<Map> userDispCountList, String dispCountKey) {
        Map<String, Integer> userDispCountsMap = [:]
        if(userDashboardCounts."$dispCountKey"){
            userDispCountsMap.putAll(new JsonSlurper().parseText(userDashboardCounts."$dispCountKey") as Map)
        }
        userDispCountList.findAll { it.assignedToId == userDashboardCounts.userId && it.workflowGroupId == workflowGroupId }.each {
            userDispCountsMap.put(it.dispositionId as String, it.count)
        }
        userDashboardCounts."$dispCountKey" = userDispCountsMap ? new JsonBuilder(userDispCountsMap).toPrettyString() : userDashboardCounts."$dispCountKey"
    }

    void updateUserDueDateCounts(UserDashboardCounts userDashboardCounts, Long workflowGroupId, List<Map> userDueDateCountsList, String dueDateCountKey) {
        Map<String, Integer> userDueDateCountsMap = [:]
        if(userDashboardCounts."$dueDateCountKey"){
            userDueDateCountsMap.putAll(new JsonSlurper().parseText(userDashboardCounts."$dueDateCountKey") as Map)
        }
        userDueDateCountsList.findAll { it.assignedToId == userDashboardCounts.userId && it.workflowGroupId == workflowGroupId }.each {
            userDueDateCountsMap.put(it.due_date, (userDueDateCountsMap.get(it.due_date) ?: 0) + it.count)
        }
        userDashboardCounts."$dueDateCountKey" = userDueDateCountsMap ? new JsonBuilder(userDueDateCountsMap).toPrettyString() : userDashboardCounts."$dueDateCountKey"
    }

    List<UserDashboardCounts> prepareUserDashboardCountList(UpdateDispCountsDTO updateDispCountsDTO) {
        Set<Long> userList = updateDispCountsDTO.userDispCaseCountList.collect { it.assignedToId as Long } + updateDispCountsDTO.userDispPECountList.collect { it.assignedToId as Long }
        Set<Long> groupList = updateDispCountsDTO.groupDispCaseCountList.unique { it.assignedToGroupId }.collect { it.assignedToGroupId as Long } +
                               updateDispCountsDTO.groupDispPECountList.unique { it.assignedToGroupId }.collect { it.assignedToGroupId as Long }
        if(groupList) {
            userList += User.createCriteria().list {
                projections {
                    property("id")
                }
                'groups' {
                    'in'("id", groupList)
                }
            }
        }
        List<UserDashboardCounts> userDashboardCountsList = []
        if(userList) {
            userDashboardCountsList = UserDashboardCounts.createCriteria().list {
                'in'('userId', userList)
            } as List<UserDashboardCounts>
        }
        userDashboardCountsList
    }

    void generateDashboardCountsList(Sql sql, Long dispositionId, UpdateDispCountsDTO updateDispCountsDTO) {
        sql.eachRow(SignalQueryHelper.singleCaseAlert_dashboard_by_disposition(true, dispositionId), []) { row ->
            updateDispCountsDTO.userDispCaseCountList.add([dispositionId: row[0], assignedToId: row[1], workflowGroupId: row[2], count: row[3] as Integer])
        }

        sql.eachRow(SignalQueryHelper.singleCaseAlert_dashboard_by_disposition(false, dispositionId), []) { row ->
            updateDispCountsDTO.groupDispCaseCountList.add([dispositionId: row[0], assignedToGroupId: row[1], workflowGroupId: row[2], count: row[3] as Integer])
        }

        sql.eachRow(SignalQueryHelper.aggCaseAlert_dashboard_by_disposition(true, dispositionId), []) { row ->
            updateDispCountsDTO.userDispPECountList.add([dispositionId: row[0], assignedToId: row[1], workflowGroupId: row[2], count: row[3] as Integer])
        }

        sql.eachRow(SignalQueryHelper.aggCaseAlert_dashboard_by_disposition(false, dispositionId), []) { row ->
            updateDispCountsDTO.groupDispPECountList.add([dispositionId: row[0], assignedToGroupId: row[1], workflowGroupId: row[2], count: row[3] as Integer])
        }
    }

    void updateCaseCounts(User user, Group workflowGroup, UpdateDispCountsDTO updateDispCountsDTO, Long dispositionId, UserDashboardCounts userDashboardCounts){
        JsonSlurper jsonSlurper = new JsonSlurper()
        if (userDashboardCounts."${updateDispCountsDTO.dispCountKey}") {
            Map userDispCaseCountMap = jsonSlurper.parseText(userDashboardCounts."${updateDispCountsDTO.dispCountKey}") as Map
            userDispCaseCountMap.remove(dispositionId.toString())
            userDashboardCounts."${updateDispCountsDTO.dispCountKey}" = userDispCaseCountMap ? new JsonBuilder(userDispCaseCountMap).toPrettyString() : null
        }
        if(userDashboardCounts."${updateDispCountsDTO.dueDateCountKey}") {
            Map dueDateCountMap = [:]
            updateDispCountsDTO.userDueDateCountsList.findAll { it.assignedToId == user.id && it.workflowGroupId == workflowGroup.id }.each {
                dueDateCountMap.put(it.due_date, it.count)
            }
            Map resultDueDateMap = alertService.updateDispReviewCountMaps(dueDateCountMap, new JsonSlurper().parseText(userDashboardCounts."${updateDispCountsDTO.dueDateCountKey}") as Map)
            userDashboardCounts."${updateDispCountsDTO.dueDateCountKey}" = resultDueDateMap ? new JsonBuilder(resultDueDateMap.findAll { it.value != 0 }).toPrettyString() : null
        }

        if (userDashboardCounts."${updateDispCountsDTO.groupDispCountKey}") {
            Map groupDispCaseCountMap = jsonSlurper.parseText(userDashboardCounts."${updateDispCountsDTO.groupDispCountKey}") as Map
            groupDispCaseCountMap.each {
                if (it.value.containsKey(dispositionId.toString())) {
                    it.value.remove(dispositionId.toString())
                }
            }
            Map resultMap = groupDispCaseCountMap.findAll { it.value.size() }
            userDashboardCounts."${updateDispCountsDTO.groupDispCountKey}" = resultMap ? new JsonBuilder(resultMap).toPrettyString() : null
        }

        if(userDashboardCounts."${updateDispCountsDTO.groupDueDateCountKey}") {
            Map groupDueDateMap = new JsonSlurper().parseText(userDashboardCounts."${updateDispCountsDTO.groupDueDateCountKey}") as Map
            List<Long> groupIdList = user.groups.findAll { it.groupType != GroupType.WORKFLOW_GROUP }.id
            groupIdList.each { id ->
                Map dueDateCountMap = [:]
                updateDispCountsDTO.dueDateGroupCountList.findAll { it.assignedToGroupId == id && it.workflowGroupId == workflowGroup.id }.each {
                    dueDateCountMap.put(it.due_date, it.count)
                }
                if (groupDueDateMap.containsKey(id.toString()) && dueDateCountMap) {
                    Map resultDueDateMap = alertService.updateDispReviewCountMaps(dueDateCountMap, groupDueDateMap.get(id.toString())).findAll { it.value != 0 }
                    resultDueDateMap ? groupDueDateMap.put(id.toString(), resultDueDateMap) : groupDueDateMap.remove(id.toString())
                }
            }
            userDashboardCounts."${updateDispCountsDTO.groupDueDateCountKey}" = groupDueDateMap ? new JsonBuilder(groupDueDateMap).toPrettyString() : null
        }
    }

    void updateDispCountsKey(UpdateDispCountsDTO updateDispCountsDTO, boolean isCaseSeries){
        if(isCaseSeries) {
            updateDispCountsDTO.dispCountKey = Constants.UserDashboardCounts.USER_DISP_CASE_COUNTS
            updateDispCountsDTO.dueDateCountKey = Constants.UserDashboardCounts.USER_DUE_DATE_CASE_COUNTS
            updateDispCountsDTO.groupDispCountKey = Constants.UserDashboardCounts.GROUP_DISP_CASE_COUNTS
            updateDispCountsDTO.groupDueDateCountKey = Constants.UserDashboardCounts.GROUP_DUE_DATE_CASE_COUNTS
            updateDispCountsDTO.userDueDateCountsList = updateDispCountsDTO.userDueDateCaseCountsList
            updateDispCountsDTO.dueDateGroupCountList = updateDispCountsDTO.dueDateGroupCaseCountList
        } else {
            updateDispCountsDTO.dispCountKey = Constants.UserDashboardCounts.USER_DISP_PECOUNTS
            updateDispCountsDTO.dueDateCountKey = Constants.UserDashboardCounts.USER_DUE_DATE_PECOUNTS
            updateDispCountsDTO.groupDispCountKey = Constants.UserDashboardCounts.GROUP_DISP_PECOUNTS
            updateDispCountsDTO.groupDueDateCountKey = Constants.UserDashboardCounts.GROUP_DUE_DATE_PECOUNTS
            updateDispCountsDTO.userDueDateCountsList = updateDispCountsDTO.userDueDatePECountsList
            updateDispCountsDTO.dueDateGroupCountList = updateDispCountsDTO.dueDateGroupPECountList
        }
    }


    void updateReviewCountsForAllExecutedConfigurations(Sql sql, String tableName) {
        List<Map> execConfigDispCountList = []
        List<String> requiresReviewDispList = Disposition.createCriteria().list {
            projections {
                property("id")
            }
            eq("reviewCompleted", false)
        }.collect{it as String}
        sql.eachRow("""Select id, disp_counts from $tableName where disp_counts is not null AND is_enabled=true and is_latest=true and adhoc_run=false""", []) { row ->
            execConfigDispCountList.add(execConfigId: row[0], dispCounts: row[1])
        }

        sql.withBatch(100, "UPDATE $tableName SET requires_review_count = :requiresReviewCount WHERE ID = :id", { preparedStatement ->
            execConfigDispCountList.each { Map map ->
                int requiresReviewCount = 0
                Map dispositionCountMap = new JsonSlurper().parseText(map.dispCounts) as Map
                Map requiresReviewCountMap = dispositionCountMap?.findAll { it.key in requiresReviewDispList  }
                if(requiresReviewCountMap) {
                    requiresReviewCount = requiresReviewCountMap.values().sum()
                }
                preparedStatement.addBatch(id: map.execConfigId, requiresReviewCount: requiresReviewCount ? requiresReviewCount.toString() : '0')
            }
        })
    }

    void updateLiteratureConfigurations(Sql sql) {
        List<Map> execConfigDispCountList = []
        List dispList = []
        sql.eachRow("""
                     select ex_lit_search_config_id,disposition_id,count(id) from LITERATURE_ALERT 
                        where  ex_lit_search_config_id in (select id from EX_LITERATURE_CONFIG)
                               and ex_lit_search_config_id is not null
                        group by ex_lit_search_config_id,disposition_id""") { row ->
            execConfigDispCountList.add(execConfigId: row[0], dispositionId: row[1], count: row[2])
        }

        Map execConfigDispCountMap = execConfigDispCountList.groupBy({
            it.execConfigId
        }).collectEntries { key, val -> [(key): val.collectEntries { [it.dispositionId as Long, it.count] }] }

        execConfigDispCountMap.each { key,  val ->
            dispList.add(['execConfigId': key, 'dispCounts': val])
        }
        List<Long> requiresReviewDispList = Disposition.createCriteria().list {
            projections {
                property("id")
            }
            eq("reviewCompleted", false)
        }
        sql.withBatch(100, "UPDATE EX_LITERATURE_CONFIG SET requires_review_count = :requiresReviewCount WHERE ID = :id", { preparedStatement ->
            dispList.each { Map map ->
                int requiresReviewCount = 0
                Map requiresReviewCountMap = map.dispCounts?.findAll { it.key in requiresReviewDispList  }
                if(requiresReviewCountMap) {
                    requiresReviewCount = requiresReviewCountMap.values().sum()
                }
                preparedStatement.addBatch(id: map.execConfigId, requiresReviewCount: requiresReviewCount ? requiresReviewCount.toString() : '0')
            }
        })
    }

    void updateDateClosedBasedOnDisposition(Disposition dispositionInstance, Map params) {
        SystemConfig systemConfig = SystemConfig.first()
        if (systemConfig.dateClosedDisposition) {
            List dateClosedBasedOnDispList = systemConfig.dateClosedDisposition.split(',')
            List tempDateClosedBasedOnDispList = []
            dateClosedBasedOnDispList.each { String disposition ->
                if (disposition.equalsIgnoreCase(dispositionInstance.displayName)) {
                    disposition = params.displayName as String
                }
                tempDateClosedBasedOnDispList << disposition
            }
            String dateClosedDispositionStr = tempDateClosedBasedOnDispList.join(',')
            systemConfig.dateClosedDisposition = dateClosedDispositionStr
            CRUDService.save(systemConfig)
        }
        if (systemConfig.dateClosedDispositionWorkflow) {
            List dateClosedBasedOnWorkflowList = systemConfig.dateClosedDispositionWorkflow.split(',')
            List tempDateClosedBasedOnWorkflowList = []
            dateClosedBasedOnWorkflowList.each { String disposition ->
                if (disposition.equalsIgnoreCase(dispositionInstance.displayName)) {
                    disposition = params.displayName as String
                }
                tempDateClosedBasedOnWorkflowList << disposition
            }
            String dateClosedDispWorflowStr = tempDateClosedBasedOnWorkflowList.join(',')
            systemConfig.dateClosedDispositionWorkflow = dateClosedDispWorflowStr
            CRUDService.save(systemConfig)
        }
    }

}
