package com.rxlogix

import com.rxlogix.audit.AuditTrail
import com.rxlogix.config.ActivityType
import com.rxlogix.config.ActivityTypeValue
import com.rxlogix.config.AdvancedFilter
import com.rxlogix.config.Configuration
import com.rxlogix.config.Disposition
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutionStatus
import com.rxlogix.config.Priority
import com.rxlogix.config.ReportExecutionStatus
import com.rxlogix.dto.AlertLevelDispositionDTO
import com.rxlogix.dto.DashboardCountDTO
import com.rxlogix.dto.SpotfireSettingsDTO
import com.rxlogix.enums.ProductClassification
import com.rxlogix.signal.GroupedAlertInfo
import com.rxlogix.signal.UndoableDisposition
import com.rxlogix.user.User
import com.rxlogix.util.AlertAsyncUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.RestApiResponse
import grails.converters.JSON
import grails.util.Holders
import com.rxlogix.enums.DateRangeEnum
import groovy.sql.Sql

import java.sql.Clob
import java.sql.ResultSet


class AggregateCaseAlertRestService implements AlertAsyncUtil {
    def alertFieldService
    def aggregateCaseAlertService
    def alertService
    def reportExecutorService
    def jaderExecutorService
    def activityService
    def productEventHistoryService
    def userService
    def emailNotificationService
    def dispositionService
    def messageSource


    Map prepareLabelConfigMap(){
        Integer prevColCount = Holders.config.signal.quantitative.number.prev.columns
        // Fetch label configuration data once
        Map labelConfig = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', null, null, null).collectEntries {
            b -> [b.name, [display: b.display, enabled: b.enabled, isHyperLink: b.isHyperLink, keyId: b.keyId]]
        }

        Map labelConfigDisplayNames = new HashMap()
        Map labelConfigCopy = new HashMap()
        // Process label configuration data
        labelConfig.each { key, value ->
            String displayName = value.display
            Boolean enabled = value.enabled
            labelConfigDisplayNames.put(key, displayName)
            labelConfigCopy.put(key, enabled)

            // Process for previous period count
            for (int j = 0; j < prevColCount; j++) {
                String prevKey = "exe${j}${key}"
                labelConfigCopy.put(prevKey, enabled)
                labelConfigDisplayNames.put(prevKey, "Prev Period ${j + 1} ${displayName}")
            }
        }

        List<Map> labelWithPreviousPeriod = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', true)
        List<Map> labelWithPreviousPeriodSubGroup = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT').findAll { it.type == "subGroup" }

        List<Map> labelWithPreviousPeriod1 = []

        for (int j = 0; j < prevColCount; j++) {
            labelWithPreviousPeriod.each { label ->
                if (!(label.name in ['hlt', 'hlgt', 'smqNarrow'])) {
                    labelWithPreviousPeriod1.add([
                            name                 : "exe${j}${label.name}",
                            display              : "Prev ${j + 1} ${label.display}",
                            enabled              : label.enabled,
                            visible              : labelConfigCopy.get(label.name),
                            previousPeriodCounter: j
                    ])
                }
            }

            labelWithPreviousPeriodSubGroup.each { subGroup ->
                labelWithPreviousPeriod1.add([
                        name                 : "exe${j}${subGroup.name}",
                        display              : "Prev ${j + 1} ${subGroup.display}",
                        enabled              : subGroup.enabled,
                        visible              : labelConfigCopy.get(subGroup.name),
                        previousPeriodCounter: j
                ])
            }
        }
        labelWithPreviousPeriod1 = labelWithPreviousPeriod1 + labelWithPreviousPeriod
        Map labelConfigMap = [labelConfig           : labelConfigDisplayNames,
                              labelConfigCopy       : labelConfigCopy,
                              labelConfigKeyId      : labelConfig.collectEntries { key, value -> [key, value.keyId] },
                              hyperlinkConfiguration: labelConfig.collectEntries { key, value -> [key, value.isHyperLink] },
                              labelConfigJson       : alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', true),
                              labelConfigNew        : labelWithPreviousPeriod1
        ]
        return labelConfigMap
    }

    Map prepareDateRangeData(ExecutedConfiguration executedConfiguration, Long executedConfigId, Long configId) {

        def dataMap = [:]
        List<String> prevFaersDate = []
        List<String> prevEvdasDate = []
        List<String> prevVaersDate = []
        List<String> prevVigibaseDate = []
        List listDateRange = []
        List dssDateRange = []
        String dr = Constants.Commons.BLANK_STRING
        String dateRange = Constants.Commons.BLANK_STRING

        String name = executedConfiguration?.name
        if (name) {
            List<ExecutedConfiguration> prevExecs = aggregateCaseAlertService.fetchPrevPeriodExecConfig(executedConfiguration.configId, executedConfigId)
            prevExecs.each {
                prevFaersDate.add(getEndDate(it.faersDateRange))
                prevEvdasDate.add(getEndDate(it.evdasDateRange))
                prevVaersDate.add(getEndDate(it.vaersDateRange))
                prevVigibaseDate.add(getEndDate(it.vigibaseDateRange))
            }
            prevExecs.removeIf { it.id >= executedConfiguration.id }
            prevExecs.each { it ->
                Date sdEx = it.executedAlertDateRangeInformation.dateRangeEndAbsolute
                if (sdEx) {
                    listDateRange.add(Date.parse("dd-MMM-yyyy", DateUtil.toDateString1(sdEx)).format("dd/MMM/yy"))
                }
            }
        }

        List<Long> prevExecList = alertService.fetchPrevExecConfigId(executedConfiguration, Configuration.get(configId))
        prevExecList.add(executedConfigId)
        List<ExecutedConfiguration> latestPrevExecList = prevExecList.sort { it ->
            ExecutedConfiguration prevExec = ExecutedConfiguration.get(it)
            prevExec?.dateCreated
        }.reverse()?.unique {
            [ExecutedConfiguration.get(it)?.executedAlertDateRangeInformation?.dateRangeStartAbsolute, ExecutedConfiguration.get(it)?.executedAlertDateRangeInformation?.dateRangeEndAbsolute]
        }.take(5)
        latestPrevExecList.each {
            String exDateRange = DateUtil.toDateString(ExecutedConfiguration.get(it).executedAlertDateRangeInformation.dateRangeStartAbsolute) +
                    " - " + DateUtil.toDateString(ExecutedConfiguration.get(it).executedAlertDateRangeInformation.dateRangeEndAbsolute)
            if (!dssDateRange.contains(exDateRange))
                dssDateRange.add(exDateRange)
        }

        dateRange = DateUtil.toDateString(executedConfiguration.executedAlertDateRangeInformation.dateRangeStartAbsolute) +
                " - " + DateUtil.toDateString(executedConfiguration.executedAlertDateRangeInformation.dateRangeEndAbsolute)
        String dateRangeStart = DateUtil.toDateString(executedConfiguration.executedAlertDateRangeInformation.dateRangeStartAbsolute)

        Map cumulativeDateRangeData = handleCumulativeDateRange(executedConfiguration,dateRangeStart,dateRange)

        Date sd = executedConfiguration.executedAlertDateRangeInformation.dateRangeEndAbsolute
        if (sd) {
            dr = Date.parse("dd-MMM-yyyy", DateUtil.toDateString1(sd)).format("dd/MMM/yy")
        }

        dataMap.dr = dr
        dataMap = [
                prevFaersDate                       : prevFaersDate,
                prevVaersDate                       : prevVaersDate,
                prevVigibaseDate                    : prevVigibaseDate,
                prevEvdasDate                       : prevEvdasDate,
                dr                                  : dr,
                listDr                              : listDateRange,
                dssDateRange                        : dssDateRange,
                dateRange                           : dateRange,
        ]
        dataMap << cumulativeDateRangeData

        return dataMap
    }

    Map handleCumulativeDateRange(ExecutedConfiguration executedConfiguration, String dateRangeStart, String dateRange) {
        String evdasDateRange = executedConfiguration?.evdasDateRange
        String faersDateRange = executedConfiguration?.faersDateRange
        String vaersDateRange = executedConfiguration?.vaersDateRange
        String vigibaseDateRange = executedConfiguration?.vigibaseDateRange

        if (executedConfiguration.executedAlertDateRangeInformation.dateRangeEnum == DateRangeEnum.CUMULATIVE) {
            String dateRangeEnd
            if (executedConfiguration?.selectedDatasource == Constants.DataSource.FAERS) {
                dateRangeEnd = reportExecutorService.getFaersDateRange().faersDate.substring(13)
                dateRange = dateRangeStart + " - " + dateRangeEnd
                faersDateRange = dateRange
            } else if (executedConfiguration?.selectedDatasource == Constants.DataSource.VAERS) {
                dateRangeEnd = reportExecutorService.getVaersDateRange(1).vaersDate.substring(13)
                dateRange = dateRangeStart + " - " + dateRangeEnd
                vaersDateRange = dateRange
            } else if (executedConfiguration?.selectedDatasource.contains(Constants.DataSource.FAERS)) {
                faersDateRange = dateRangeStart + " - " + reportExecutorService.getFaersDateRange().faersDate.substring(13)
            } else if (executedConfiguration?.selectedDatasource.contains(Constants.DataSource.VAERS)) {
                vaersDateRange = dateRangeStart + " - " + reportExecutorService.getVaersDateRange(1).vaersDate.substring(13)
            } else {
                dateRangeEnd = DateUtil.toDateString(executedConfiguration.executedAlertDateRangeInformation.dateRangeEndAbsolute)
                dateRange = dateRangeStart + " - " + dateRangeEnd
            }

            if (executedConfiguration?.selectedDatasource == Constants.DataSource.VIGIBASE) {
                dateRange = dateRangeStart + " - " + (reportExecutorService.getVigibaseDateRange().vigibaseDate).substring(13)
                vigibaseDateRange = dateRange
            } else if (executedConfiguration?.selectedDatasource.contains(Constants.DataSource.VIGIBASE)) {
                vigibaseDateRange = dateRangeStart + " - " + (reportExecutorService.getVigibaseDateRange().vigibaseDate).substring(13)
            } else if (executedConfiguration?.selectedDatasource.contains(Constants.DataSource.JADER)) {
                dateRange = dateRangeStart + " - " + (jaderExecutorService.getJaderDateRange().jaderDate).substring(13)
            }
        }

        return [dateRange   : dateRange, faersDateRange: faersDateRange, vaersDateRange: vaersDateRange, vigibaseDateRange: vigibaseDateRange, evdasDateRange: evdasDateRange, evdasEndDate: getEndDate(evdasDateRange),
                faersEndDate: getEndDate(faersDateRange),]
    }

    String getEndDate(String dateRange){
        if(dateRange){
            String date = dateRange.substring(dateRange.indexOf(" - ")+" - ".size(),dateRange.length()).replace('-','/')
            date.replace(date[-4..-1],date[-2..-1])
        } else {
            null
        }
    }

    Map changeAlertPriority(Map dataMap, User currentUser){
        Map response = [:]
        List<Map> selectedRows = dataMap.selectedRows
        Long priorityId = dataMap.newPriorityId as Long
        String justification = dataMap.justification
        Boolean isArchived = dataMap.isArchived
        Priority newPriority = Priority.get(priorityId)
        def domain = aggregateCaseAlertService.getDomainObject(isArchived)
        Date lastUpdated = null
        List<UndoableDisposition> undoableDispositionIdList = []
        //Create the peHistory.
        Map peHistoryMap = [
                "priority"        : newPriority,
                "justification"   : justification,
                "change"          : Constants.HistoryType.PRIORITY,
        ]
        List<Map> peHistoryMapList = []

        selectedRows?.each { Map<String, Long> selectedRow ->
            def alert = domain.get(selectedRow["alertId"])
            ExecutedConfiguration executedConfigObj = ExecutedConfiguration.get(selectedRow["executedConfigId"])
            if (alert) {
                User currUser = alert.assignedTo
                if (alert.priority != newPriority) {
                    Priority currentPriority = alert.priority
                    peHistoryMap = aggregateCaseAlertService.createBasePEHistoryMap(alert,peHistoryMap, isArchived)
                    peHistoryMapList << peHistoryMap
                    //Update the alert instance.
                    aggregateCaseAlertService.updateAggregateAlertStates(alert, peHistoryMap)
                    lastUpdated = alert.lastUpdated
                    peHistoryMap.put("createdTimestamp", lastUpdated)
                    ExecutedConfiguration executedConfig = executedConfigObj
                    activityService.createActivity(executedConfig, aggregateCaseAlertService.getActivityByType(ActivityTypeValue.PriorityChange),
                            currentUser, "Priority changed from '$currentPriority' to '$newPriority'",
                            justification, ['For Aggregate Alert'], alert.productName, alert.pt, currUser, null, alert.assignedToGroup,null, null, lastUpdated)

                    // updating the due date of undoable disposition object with latest due date
                    alertService.updateUndoDispDueDate(Constants.AlertType.AGGREGATE_NEW, alert.id as Long, undoableDispositionIdList, alert.dueDate)

                    response << [id: alert.id, dueIn: alert.dueIn()]
                }
            }
            peHistoryMap = [
                    "priority"        : newPriority,
                    "justification"   : justification,
                    "change"          : Constants.HistoryType.PRIORITY,
            ]
        }
        productEventHistoryService.batchPersistHistory(peHistoryMapList)
        if (undoableDispositionIdList) {
            aggregateCaseAlertService.notifyUndoableDisposition(undoableDispositionIdList)
        }
        return response
    }

    void changeAssignedToGroup(List<Long> selectedIds, String assignedToValue, Boolean isArchived, String assginedToLabel) {
        List<Map> peHistoryList = []
        boolean bulkUpdate = selectedIds.size() > 1
        DashboardCountDTO dashboardCountDTO = alertService.prepareDashboardCountDTO(false)
        def domain = aggregateCaseAlertService.getDomainObject(isArchived)
        selectedIds.each { Long id ->
            User loggedInUser = userService.getUser()
            def aggregateCaseAlert = domain.get(id)
            String eventName = aggregateCaseAlert.pt
            ExecutedConfiguration executedConfiguration = aggregateCaseAlert.executedAlertConfiguration
            Long configId = aggregateCaseAlertService.getAlertConfigObject(executedConfiguration)
            List peHistoryMapList = []
            if (assignedToValue != userService.getAssignToValue(aggregateCaseAlert)) {
                List<User> oldUserList = userService.getUserListFromAssignToGroup(aggregateCaseAlert)
                String oldUserName = userService.getAssignedToName(aggregateCaseAlert)
                //For Dashboard Counts
                if (alertService.isUpdateDashboardCount(isArchived, aggregateCaseAlert) && aggregateCaseAlert.assignedToId) {
                    alertService.updateDashboardCountMaps(dashboardCountDTO.userDispCountsMap, aggregateCaseAlert.assignedToId, aggregateCaseAlert.dispositionId.toString())
                    if (aggregateCaseAlert.dueDate) {
                        alertService.updateDashboardCountMaps(dashboardCountDTO.userDueDateCountsMap, aggregateCaseAlert.assignedToId, DateUtil.stringFromDate(aggregateCaseAlert.dueDate, "dd-MM-yyyy", "UTC"))
                    }

                } else if (alertService.isUpdateDashboardCount(isArchived, aggregateCaseAlert)) {
                    alertService.updateDashboardCountMaps(dashboardCountDTO.groupDispCountsMap, aggregateCaseAlert.assignedToGroupId, aggregateCaseAlert.dispositionId.toString())
                    if (aggregateCaseAlert.dueDate) {
                        alertService.updateDashboardCountMaps(dashboardCountDTO.groupDueDateCountsMap, aggregateCaseAlert.assignedToGroupId, DateUtil.stringFromDate(aggregateCaseAlert.dueDate, "dd-MM-yyyy", "UTC"))
                    }
                }
                aggregateCaseAlert = userService.assignGroupOrAssignTo(assignedToValue, aggregateCaseAlert)
                String newUserName = userService.getAssignedToName(aggregateCaseAlert)
                List<User> newUserList = userService.getUserListFromAssignToGroup(aggregateCaseAlert)
                Map peHistoryMap = aggregateCaseAlertService.createPEHistoryMapForAssignedToChange(aggregateCaseAlert, configId, isArchived)
                peHistoryMapList.add(peHistoryMap)
                aggregateCaseAlertService.updateAggregateAlertStates(aggregateCaseAlert, peHistoryMap)
                if (executedConfiguration) {
                    ActivityType activityType = aggregateCaseAlertService.getActivityByType(ActivityTypeValue.AssignedToChange)
                    String detailsText = assginedToLabel + " changed from '${oldUserName}' to '${newUserName}'"
                    if (emailNotificationService.emailNotificationWithBulkUpdate(bulkUpdate, Constants.EmailNotificationModuleKeys.ASSIGNEE_UPDATE)) {
                        String newEmailMessage = messageSource.getMessage('app.email.case.assignment.agg.message.newUser', null, Locale.default)
                        String oldEmailMessage = messageSource.getMessage('app.email.case.assignment.agg.message.oldUser', null, Locale.default)
                        List emailDataList = userService.generateEmailDataForAssignedToChange(newEmailMessage, newUserList, oldEmailMessage, oldUserList)
                        aggregateCaseAlertService.sendMailForAssignedToChange(emailDataList, aggregateCaseAlert, isArchived)
                    }
                    Map pEHistoryMap = aggregateCaseAlertService.createProductEventHistoryForDispositionChange(aggregateCaseAlert, null, isArchived)
                    pEHistoryMap?.change = Constants.HistoryType.ASSIGNED_TO
                    peHistoryList << pEHistoryMap
                    activityService.createActivity(executedConfiguration, activityType,
                            loggedInUser, detailsText, null, ['For Aggregate Alert'],
                            aggregateCaseAlert.productName, eventName, aggregateCaseAlert.assignedTo, null, aggregateCaseAlert.assignedToGroup)
                }
            }
        }
        Long userId = null
        Long groupId = null
        if (assignedToValue.startsWith(Constants.USER_GROUP_TOKEN)) {
            groupId = Long.valueOf(assignedToValue.replaceAll(Constants.USER_GROUP_TOKEN, ''))
        } else {
            userId = Long.valueOf(assignedToValue.replaceAll(Constants.USER_TOKEN, ''))
        }
        alertService.updateAssignedToDashboardCounts(dashboardCountDTO, userId, groupId)
        productEventHistoryService.batchPersistHistory(peHistoryList)
    }


    Map changeAlertLevelDisposition(Map dataMap){
        Boolean isAlertBursting = dataMap.isAlertBursting
        Long targetDispositionId = dataMap.targetDispositionId
        String justificationText = dataMap.justification
        Long executedConfigId = dataMap.executedConfigId
        Boolean isArchived = dataMap.isArchived
        Map responseMap = [:]
        def domain = aggregateCaseAlertService.getDomainObject(isArchived)
        List <ExecutedConfiguration> execConfigList
        GroupedAlertInfo groupedAlertInfo
        if(isAlertBursting){
            groupedAlertInfo = GroupedAlertInfo.get(executedConfigId)
            execConfigList = groupedAlertInfo.execConfigList
        } else {
            execConfigList = [ExecutedConfiguration.get(executedConfigId)]
        }
        Disposition targetDisposition = Disposition.get(targetDispositionId)
        String alertName = isAlertBursting ? groupedAlertInfo.name : execConfigList[0]?.name
        Integer updatedRowsCount = 0
        execConfigList.each { ExecutedConfiguration execConfig ->
            AlertLevelDispositionDTO alertLevelDispositionDTO = dispositionService.populateAlertLevelDispositionDTO(targetDisposition, justificationText, domain, execConfig)
            Integer updatedRowsCountForSingleConfig = aggregateCaseAlertService.changeAlertLevelDisposition(alertLevelDispositionDTO, isArchived)
            updatedRowsCount = updatedRowsCount + updatedRowsCountForSingleConfig
        }
        List removedDispositionNames = Disposition.findAllByReviewCompleted(false).collect {it.displayName}
        String resultMsg = null
        if(updatedRowsCount <= 0){
            resultMsg =  messageSource.getMessage("alert.level.review.completed", null, Locale.default)
        } else {
            dispositionService.sendDispChangeNotification(targetDisposition, alertName)
        }
        RestApiResponse.successResponseWithData(responseMap,resultMsg,[removedDispositionNames:removedDispositionNames])
        aggregateCaseAlertService.notifyUpdateDispCountsForExConfigAndGroupedConfig(execConfigList*.id)
        return responseMap
    }

    Map fetchAdvancedFilterDetails(AdvancedFilter advancedFilter) {
        String shared = (userService.currentUserId == advancedFilter.userId) ? '' : Constants.Commons.SHARED
        ['name': advancedFilter.name + shared, 'id': advancedFilter.id]
    }


}
