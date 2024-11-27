package com.rxlogix.api

import com.rxlogix.Constants
import com.rxlogix.attachments.Attachment
import com.rxlogix.ExceptionHandlingController
import com.rxlogix.config.*
import com.rxlogix.controllers.AlertController
import com.rxlogix.dto.AlertDataDTO
import com.rxlogix.enums.*
import com.rxlogix.security.Authorize
import com.rxlogix.signal.*
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.*
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Holders
import grails.validation.ValidationException
import groovy.json.JsonOutput
import org.apache.commons.io.FilenameUtils
import groovy.json.JsonSlurper
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource
import org.apache.commons.lang.StringEscapeUtils
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import javax.servlet.http.HttpServletResponse
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import org.apache.http.util.TextUtils


import static org.springframework.http.HttpStatus.NOT_FOUND

@Authorize
class AggregateCaseAlertRestController implements AlertAsyncUtil, ExceptionHandlingController, AlertController {

    def aggregateCaseAlertService
    def validatedSignalService
    def userService
    def viewInstanceService
    def cacheService
    def reportIntegrationService
    def workflowRuleService
    def priorityService
    def safetyLeadSecurityService
    def alertService
    def dispositionService
    def spotfireService
    def alertFieldService
    def aggregateCaseAlertRestService
    def configurationService
    def attachmentableService
    def activityService
    def dynamicReportService
    def signalAuditLogService
    def pvsGlobalTagService
    def dataObjectService
    def jaderAlertService
    def dataSheetService

    String fetchAlertDetails(Long executedConfigId, String callingScreen, Long tempViewId, Boolean archived, Integer version, String dashboardFilter, Long viewId, Boolean isAlertBursting) {

        // this method is originated from aggregate case alert controller method
        // this can be refactored according to api data requirements
        Map responseMap = [:]
        Boolean alertDeletionObject = false
        Integer prevColCount = Holders.config.signal.quantitative.number.prev.columns
        Boolean isTempViewSelected = tempViewId ? true : false
        try {
            User currentUser = userService.getUser()
            /*if (params.masterConfigProduct && params.masterConfigProduct != null) {
                flash.message = message(code: "master.config.product.success.message", args: [params.masterConfigProduct])
            }*/

            if (!isAlertBursting && callingScreen != Constants.Commons.DASHBOARD && executedConfigId == -1) {
                alertDeletionInProgress()
                return
            }

            ExecutedConfiguration executedConfiguration
            GroupedAlertInfo groupedAlertInfo
            if(isAlertBursting) {
                groupedAlertInfo = GroupedAlertInfo.get(executedConfigId)
                executedConfiguration = groupedAlertInfo.execConfigList[0]

            } else {
                executedConfiguration = executedConfigId ? ExecutedConfiguration.findById(executedConfigId) : null
            }

            // Check if deletion is in progress for executed configuration
            if (!isAlertBursting && executedConfiguration) {
                alertDeletionObject = alertService.isDeleteInProgress(executedConfiguration.configId as Long, executedConfiguration?.type) ?: false
            }

            // Initialize variables
            Long configId = 0L

            if (executedConfiguration || callingScreen == Constants.Commons.DASHBOARD) {
                /* if (callingScreen != Constants.Commons.DASHBOARD && !aggregateCaseAlertService.detailsAccessPermission(executedConfiguration.selectedDatasource.split(","))) {
                     notFound()
                     return
                 }*/
                configId = executedConfiguration ? aggregateCaseAlertService.getAlertConfigObject(executedConfiguration) : 0L
            } else {
                notFound()
                return
            }

            if (isAlertBursting && callingScreen != Constants.Commons.DASHBOARD && alertDeletionObject) {
                alertDeletionInProgress()
                return
            }

            Boolean cumulative = cacheService.getPreferenceByUserId(currentUser.id)?.isCumulativeAlertEnabled ?: false
            String name = Constants.Commons.BLANK_STRING
            String dssNetworkUrl = [grailsLinkGenerator.serverBaseURL, "aggregateCaseAlert", "dssScores"].join("/")
            String backUrl = request.getHeader('referer')
            List freqNames = []
            Boolean groupBySmq = false
            List actionConfigList = validatedSignalService.getActionConfigurationList(Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
            List alertDispositionList = dispositionService.listUserAlertDispositions(currentUser)
            String alertType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT
            String timezone = cacheService.getPreferenceByUserId(currentUser.id)?.timeZone
            Map<String, Map> statusMap = [:]
            String currentDateRangeDss = ""
            Map dateRangeDataMap = [:]

            // Check permission for accessing alert
            /*if (callingScreen == Constants.Commons.REVIEW && (alertService.checkAlertSharedToCurrentUser(executedConfiguration) || !alertService.roleAuthorised(Constants.AlertConfigType.AGGREGATE_CASE_ALERT, executedConfiguration?.selectedDatasource))) {
                log.info("${user?.username} does not have access to alert")
                RestApiResponse.accessDeniedResponse(responseMap, "Permission Error Page")
                render(responseMap as JSON)
                return
            }*/

            if (callingScreen != Constants.Commons.DASHBOARD && !cumulative) {
                name = isAlertBursting ? groupedAlertInfo.name : executedConfiguration?.name
                statusMap = isAlertBursting ? [:] : spotfireService.fetchAnalysisFileUrlCounts(executedConfiguration)
                if(isAlertBursting){
                    dateRangeDataMap = aggregateCaseAlertRestService.prepareDateRangeData(executedConfiguration, executedConfiguration.id, configId)
                } else {
                    dateRangeDataMap = aggregateCaseAlertRestService.prepareDateRangeData(executedConfiguration, executedConfigId, configId)
                }
                groupBySmq = executedConfiguration.groupBySmq
                if (groupBySmq) {
                    alertType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT_SMQ
                }
            } else if (callingScreen != Constants.Commons.TAGS) {
                backUrl = createLink(controller: 'dashboard', action: 'index')
            }

            if (params.callingScreen == Constants.Commons.DASHBOARD) {
                alertType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DASHBOARD
            } else if (executedConfiguration) {
                String selectedDatasource = executedConfiguration.selectedDatasource
                if (selectedDatasource && !selectedDatasource.contains(Constants.DataSource.EUDRA)) {
                    if (selectedDatasource.startsWith(Constants.DataSource.FAERS) && !selectedDatasource.contains(Constants.DataSource.VIGIBASE)) {
                        alertType = groupBySmq ? Constants.AlertConfigType.AGGREGATE_CASE_ALERT_SMQ_FAERS : Constants.AlertConfigType.AGGREGATE_CASE_ALERT_FAERS
                    } else if (selectedDatasource.startsWith(Constants.DataSource.VAERS) && !selectedDatasource.contains(Constants.DataSource.VIGIBASE)) {
                        alertType = groupBySmq ? Constants.AlertConfigType.AGGREGATE_CASE_ALERT_SMQ_VAERS : Constants.AlertConfigType.AGGREGATE_CASE_ALERT_VAERS
                    } else if (selectedDatasource.startsWith(Constants.DataSource.VIGIBASE)) {
                        alertType = groupBySmq ? Constants.AlertConfigType.AGGREGATE_CASE_ALERT_SMQ_VIGIBASE : Constants.AlertConfigType.AGGREGATE_CASE_ALERT_VIGIBASE
                    } else if (selectedDatasource.startsWith(Constants.DataSource.JADER)) {
                        alertType = groupBySmq ? Constants.AlertConfigType.AGGREGATE_CASE_ALERT_SMQ_JADER : Constants.AlertConfigType.AGGREGATE_CASE_ALERT_JADER
                    }
                }
            }

            ViewInstance viewInstance = viewInstanceService.fetchSelectedViewInstance(alertType, viewId as Long)

            String dssUrl = grailsApplication.config.dss.url
            Configuration configurationInstance = Configuration.get(configId)
            String selectedDatasource = configurationInstance?.selectedDatasource
            List<String> availableDataSources = grailsApplication.config.pvsignal.supported.datasource.call()
            boolean isFaersEnabled = availableDataSources.contains(Constants.DataSource.FAERS)
            boolean isEvdasEnabled = availableDataSources.contains(Constants.DataSource.EUDRA)
            boolean isPVAEnabled = selectedDatasource ? availableDataSources.contains(Constants.DataSource.PVA) && selectedDatasource.contains(Constants.DataSource.PVA) : false
            boolean isFaersAvailable = selectedDatasource ? availableDataSources.contains(Constants.DataSource.FAERS) && selectedDatasource.contains(Constants.DataSource.FAERS) : false
            boolean isVaersAvailable = selectedDatasource ? availableDataSources.contains(Constants.DataSource.VAERS) && selectedDatasource.contains(Constants.DataSource.VAERS) : false
            boolean isJaderAvailable = selectedDatasource ? availableDataSources.contains(Constants.DataSource.JADER) && selectedDatasource.contains(Constants.DataSource.JADER) : false
            boolean isVaersEnabled = availableDataSources.contains(Constants.DataSource.VAERS)
            boolean isVigibaseEnabled = availableDataSources.contains(Constants.DataSource.VIGIBASE)
            boolean isJaderEnabled = availableDataSources.contains(Constants.DataSource.JADER)
            List<Map> availableAlertPriorityJustifications = Justification.fetchByAnyFeatureOn([JustificationFeatureEnum.alertPriority], false)*.toDto(timezone)
            Map dispositionIncomingOutgoingMap = workflowRuleService.fetchDispositionIncomingOutgoingMap()
            Boolean forceJustification = currentUser.workflowGroup?.forceJustification
            //List availableSignals = validatedSignalService.fetchSignalsNotInAlertObj(currentUser)
            List<Map> availablePriorities = priorityService.listPriorityOrder()
            List allowedProductsAsSafetyLead = alertService.isProductSecurity() ? safetyLeadSecurityService.allAllowedProductsForUser(currentUser.id) : []
            Map actionTypeAndActionMap = alertService.getActionTypeAndActionMap()
            List<String> reviewCompletedDispostionList = dispositionService.getReviewCompletedDispositionList()
            List subGroupsColumnsList = []
            List faersSubGroupsColumnsList = []
            Map subGroupMap = cacheService.getSubGroupMap()
            subGroupMap[Holders.config.subgrouping.ageGroup.name]?.each { id, value ->
                subGroupsColumnsList.add(value)
            }
            subGroupMap[Holders.config.subgrouping.gender.name]?.each { id, value ->
                subGroupsColumnsList.add(value)
            }
            Map<String, List<String>> prrRorSubGroupMap = cacheService.allOtherSubGroupColumnUIList(Constants.DataSource.PVA)
            Map<String, List<String>> relativeSubGroupMap = cacheService.relativeSubGroupColumnUIList(Constants.DataSource.PVA)
            Boolean isShareFilterViewAllowed = currentUser.isAdmin()
            Boolean isViewUpdateAllowed = viewInstance?.isViewUpdateAllowed(currentUser)
            subGroupMap[Holders.config.subgrouping.faers.ageGroup.name]?.each { id, value ->
                faersSubGroupsColumnsList.add(value)
            }
            subGroupMap[Holders.config.subgrouping.faers.gender.name]?.each { id, value ->
                faersSubGroupsColumnsList.add(value)
            }
            def prevColMap = Holders.config.signal.evdas.data.previous.columns.clone()
            def prevColumns = groovy.json.JsonOutput.toJson(prevColMap)
            Boolean hasAggReviewerAccess = hasReviewerAccess(Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
            List readOnlyUsers = alertService.getReadOnlyUsersForEx(executedConfiguration)
            List readOnlyGroups = alertService.getReadOnlyGroupsForEx(executedConfiguration)
            def readOnlyUser = readOnlyUsers.find { it.id == currentUser.id }
            if (readOnlyUser) {
                if(readOnlyUser?.readOnly)
                    hasAggReviewerAccess = false
            } else if (readOnlyGroups.find { currentUser.groups*.id.contains(it.id) && it.readOnly }) {
                hasAggReviewerAccess = false
            }
            Boolean showArchivedAlert = Configuration.get(configId).shareWithUsers.contains(currentUser) || Configuration.get(configId).shareWithGroups.find{currentUser.groups*.id.contains(it.id)}
            if(currentUser.isAdmin() || currentUser.id == executedConfiguration.owner.id){
                hasAggReviewerAccess = true
                showArchivedAlert = true
            }
            String buttonClass = hasAggReviewerAccess ? "" : "hidden"
            if (dateRangeDataMap?.dssDateRange) {
                currentDateRangeDss = dateRangeDataMap.dssDateRange.first()
            }
            def latestVersion
            if (callingScreen != Constants.Commons.DASHBOARD) {
                Configuration configuration = Configuration.get(executedConfiguration?.configId)
                latestVersion = ExecutionStatus.findAllByConfigIdAndExecutionStatusAndType(configuration?.id, ReportExecutionStatus.COMPLETED, Constants.AlertConfigType.AGGREGATE_CASE_ALERT).size()
            }
            Map dispositionData = workflowRuleService.fetchDispositionData()
            boolean labelCondition = executedConfiguration?.groupBySmq ? true : viewInstanceService.isLabelChangeRequired(selectedDatasource)
            if (callingScreen == Constants.Commons.DASHBOARD) {
                labelCondition = !(Holders.config.dss.enable.autoProposed && Holders.config.statistics.enable.dss)
            }
            List searchableColumnList = []
            List jaderColumnList = []
            List<Map> fieldList = []
            if (isJaderAvailable) {
                jaderColumnList = alertFieldService.getJaderColumnList(selectedDatasource, groupBySmq)
                searchableColumnList = alertService.generateSearchableColumnsJader(groupBySmq)
                fieldList = aggregateCaseAlertService.fieldListAdvanceFilterJader(groupBySmq)
            } else {
                searchableColumnList = alertService.generateSearchableColumns(groupBySmq, callingScreen, selectedDatasource)
                fieldList = aggregateCaseAlertService.fieldListAdvanceFilter(selectedDatasource, groupBySmq, callingScreen, 'AGGREGATE_CASE_ALERT').sort()

                if (groupBySmq) {
                    fieldList.removeAll {
                        (it.name.toString().equals("smqNarrow") || it.name.toString().equals("hlt") || it.name.toString().equals("hlgt"))
                    }
                }
            }

            String encodedExConfigId = configurationService?.encodeId(executedConfigId)
            String titleNameUrl = createHref('aggregateCaseAlert', 'review',null)
            String infoIconUrl = createHref('aggregateCaseAlert', 'viewExecutedConfig',[ "id": encodedExConfigId])
            Map breadCrumbsData = [
                    "titleName"     : Constants.AuditLog.AGGREGATE_REVIEW,
                    "titleNameUrl" : titleNameUrl,
                    "alertName"     : name,
                    "dateRange"     : dateRangeDataMap.dateRange,
                    "infoIconUrl"   : infoIconUrl
            ]

            Map dataMap = [
                    executedConfigId                    : executedConfigId,
                    labelCondition                      : labelCondition,
                    isAdmin                             : SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN"),
                    adhocRun                            : executedConfiguration ? executedConfiguration.adhocRun : false,
                    selectedDatasource                  : selectedDatasource,
                    isSaftyDb                           : executedConfiguration ? executedConfiguration.selectedDatasource == Constants.DataSource.PVA : false,
                    configId                            : configId,
                    prevColumns                         : prevColMap,
                    prevColumnsJson                     : prevColumns,
                    callingScreen                       : callingScreen,
                    name                                : name,
                    showPrr                             : grailsApplication.config.statistics.enable.prr,
                    showRor                             : grailsApplication.config.statistics.enable.ror,
                    showEbgm                            : grailsApplication.config.statistics.enable.ebgm,
                    showPrrFaers                        : isFaersEnabled && grailsApplication.config.statistics.faers.enable.prr,
                    showRorFaers                        : isFaersEnabled && grailsApplication.config.statistics.faers.enable.ror,
                    showEbgmFaers                       : isFaersEnabled && grailsApplication.config.statistics.faers.enable.ebgm,
                    showPrrVaers                        : isVaersEnabled && grailsApplication.config.statistics.vaers.enable.prr,
                    showRorVaers                        : isVaersEnabled && grailsApplication.config.statistics.vaers.enable.ror,
                    showEbgmVaers                       : isVaersEnabled && grailsApplication.config.statistics.vaers.enable.ebgm,
                    showPrrVigibase                     : isVigibaseEnabled && grailsApplication.config.statistics.vigibase.enable.prr,
                    showRorVigibase                     : isVigibaseEnabled && grailsApplication.config.statistics.vigibase.enable.ror,
                    showEbgmVigibase                    : isVigibaseEnabled && grailsApplication.config.statistics.vigibase.enable.ebgm,
                    showPrrJader                        : isJaderEnabled && grailsApplication.config.statistics.jader.enable.prr,
                    showRorJader                        : isJaderEnabled && grailsApplication.config.statistics.jader.enable.ror,
                    showEbgmJader                       : isJaderEnabled && grailsApplication.config.statistics.jader.enable.ebgm,
                    showDss                             : grailsApplication.config.statistics.enable.dss,
                    showDssScores                       : grailsApplication.config.statistics.enable.dssScores,
                    isAutoProposed                      : grailsApplication.config.dss.enable.autoProposed,
                    aggregateRules                      : getAggregateRuleJson(),
                    groupBySmq                          : groupBySmq,
                    backUrl                             : backUrl,
                    currentDateRangeDss                 : currentDateRangeDss,
                    dssUrl                              : dssUrl,
                    cumulative                          : cumulative,
                    freqNames                           : freqNames,
                    isShareFilterViewAllowed            : isShareFilterViewAllowed,
                    isViewUpdateAllowed                 : isViewUpdateAllowed,
                    filterMap                           : viewInstance ? viewInstance.filters : "",
                    columnIndex                         : "",
                    sortedColumn                        : viewInstance ? viewInstance.sorting : "",
                    advancedFilterView                  : viewInstance?.advancedFilter ? aggregateCaseAlertRestService.fetchAdvancedFilterDetails(viewInstance.advancedFilter) : "",
                    viewId                              : viewInstance ? viewInstance.id : "",
                    actionConfigList                    : actionConfigList,
                    isFaers                             : (executedConfiguration && executedConfiguration?.selectedDatasource == Constants.DataSource.FAERS) ?: false,
                    isVaers                             : (executedConfiguration && executedConfiguration?.selectedDatasource == Constants.DataSource.VAERS) ?: false,
                    isVigibase                          : (executedConfiguration && executedConfiguration?.selectedDatasource == Constants.DataSource.VIGIBASE) ?: false,
                    isJader                             : (executedConfiguration && executedConfiguration?.selectedDatasource == Constants.DataSource.JADER) ?: false,
                    dashboardFilter                     : dashboardFilter ? dashboardFilter : '',
                    reportUrl                           : reportIntegrationService.fetchReportUrl(executedConfiguration, isAlertBursting),
                    reportName                          : name,
                    analysisFileUrl                     : spotfireService.fetchAnalysisFileUrlIntegratedReview(executedConfiguration, isAlertBursting),
                    dispositionIncomingOutgoingMap      : dispositionIncomingOutgoingMap,
                    dispositionData                     : dispositionData,
                    forceJustification                  : forceJustification,
                    availableAlertPriorityJustifications: availableAlertPriorityJustifications,
                    availablePriorities                 : availablePriorities,
                    allowedProductsAsSafetyLead         : allowedProductsAsSafetyLead?.join(","),
                    prevColCount                        : prevColCount,
                    isLatest                            : isAlertBursting ? groupedAlertInfo.isLatest : executedConfiguration?.isLatest,
                    appType                             : Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
                    fieldList                           : fieldList.sort({ it?.display?.toUpperCase() }),
                    reviewCompletedDispostionList       : JsonOutput.toJson(reviewCompletedDispostionList),
                    actionPropertiesMap                 : JsonOutput.toJson(actionTypeAndActionMap.actionPropertiesMap),
                    actionTypeList                      : actionTypeAndActionMap.actionTypeList,
                    alertDispositionList                : alertDispositionList,
                    isProductSecurity                   : alertService.isProductSecurity(),
                    subGroupsColumnList                 : subGroupsColumnsList,
                    faersSubGroupsColumnList            : faersSubGroupsColumnsList,
                    relativeSubGroupMap                 : relativeSubGroupMap,
                    prrRorSubGroupMap                   : prrRorSubGroupMap,
                    filterIndex                         : JsonOutput.toJson(searchableColumnList[0]),
                    filterIndexMap                      : JsonOutput.toJson(searchableColumnList[1]),
                    isCaseSeriesAccess                  : SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_VIEWER, ROLE_VIEW_ALL, ROLE_FAERS_CONFIGURATION, ROLE_VAERS_CONFIGURATION, ROLE_VIGIBASE_CONFIGURATION,ROLE_JADER_CONFIGURATION"),
                    alertType                           : alertType,
                    isPVAEnabled                        : isPVAEnabled,
                    isJaderAvailable                    : isJaderAvailable,
                    isFaersEnabled                      : isFaersEnabled,
                    isVaersEnabled                      : isVaersEnabled,
                    isVigibaseEnabled                   : isVigibaseEnabled,
                    isJaderEnabled                      : isJaderEnabled,
                    isEvdasEnabled                      : isEvdasEnabled,
                    allFourEnabled                      : isVigibaseEnabled && isVaersEnabled && isFaersEnabled && isEvdasEnabled,
                    allThreeEnabled                     : (isVaersEnabled && isFaersEnabled && isEvdasEnabled && !isVigibaseEnabled) || (isVigibaseEnabled && isFaersEnabled && isEvdasEnabled && !isVaersEnabled) || (isVigibaseEnabled && isVaersEnabled && isEvdasEnabled && !isFaersEnabled) || (isVigibaseEnabled && isFaersEnabled && isVaersEnabled && !isEvdasEnabled),
                    anyTwoEnabled                       : (isVaersEnabled && isFaersEnabled && !isEvdasEnabled && !isVigibaseEnabled) || (isEvdasEnabled && isVaersEnabled && !isFaersEnabled && !isVigibaseEnabled) || (isEvdasEnabled && isFaersEnabled && !isVaersEnabled && !isVigibaseEnabled) || (isVaersEnabled && !isFaersEnabled && !isEvdasEnabled && isVigibaseEnabled) || (isEvdasEnabled && !isVaersEnabled && !isFaersEnabled && isVigibaseEnabled) || (isEvdasEnabled && !isFaersEnabled && !isVaersEnabled && isVigibaseEnabled),
                    anyOneEnabled                       : (isVaersEnabled && !isFaersEnabled && !isEvdasEnabled && !isVigibaseEnabled) || (isEvdasEnabled && !isVaersEnabled && !isFaersEnabled && !isVigibaseEnabled) || (isFaersEnabled && !isEvdasEnabled && !isVaersEnabled && !isVigibaseEnabled) || (isVigibaseEnabled && !isFaersEnabled && !isEvdasEnabled && !isVaersEnabled),
                    isArchived                          : archived ?: false,
                    isRor                               : cacheService.getRorCache(),
                    isPriorityEnabled                   : grailsApplication.config.alert.priority.enable,
                    analysisStatus                      : statusMap,
                    analysisStatusJson                  : statusMap,
                    saveCategoryAccess                  : checkAccessForCategorySave(Constants.AlertConfigType.AGGREGATE_CASE_ALERT),
                    hasAggReviewerAccess                : hasAggReviewerAccess,
                    hasSignalCreationAccessAccess       : hasSignalCreationAccessAccess(),
                    hasSignalViewAccessAccess           : hasSignalViewAccessAccess(),
                    buttonClass                         : buttonClass,
                    isTempViewSelected                  : isTempViewSelected,
                    dssNetworkUrl                       : dssNetworkUrl,
                    isFaersAvailable                    : isFaersAvailable,
                    isVaersAvailable                    : isVaersAvailable,
                    version                             : version ?: latestVersion ?: "null",
                    currUserName                        : currentUser.fullName,
                    jaderColumnList                     : jaderColumnList,
                    isDispositionDropdownEnabled        : grailsApplication.config.alert.disposition.useDropdown,
                    showArchivedAlert                   : showArchivedAlert
            ]
            dataMap << dateRangeDataMap
            //dataMap << aggregateCaseAlertRestService.prepareLabelConfigMap()
            dataMap.put("breadCrumbsData",breadCrumbsData)
            RestApiResponse.successResponseWithData(responseMap, null, dataMap)
        } catch (Exception ex) {
            log.error("Error fetching alert detail parameters", ex)
            RestApiResponse.serverErrorResponse(responseMap)
        }
        render(responseMap as JSON)
    }

    def caseSeriesDetails() {
        String jsonContent = request.JSON
        log.info("json content received is := " + jsonContent)
        try {
            Map dataMap = jsonContent ? jsonToMap(jsonContent) : null
            Long aggExecutionId = dataMap.aggExecutionId
            Long aggAlertId = dataMap.aggAlertId
            String aggCountType = dataMap.aggCountType
            Integer ptCode = dataMap.ptCode
            String type = dataMap.type
            String typeFlag = dataMap.typeFlag
            redirect(action: "caseSeriesDetails", controller: "singleCaseAlert", aggExecutionId: aggExecutionId, aggAlertId: aggAlertId, aggCountType: aggCountType, ptCode: ptCode, type: type, typeFlag: typeFlag, params: dataMap)

        } catch (Throwable ex) {
            log.error("Error redirecting case series details", ex)
        }
    }

    def viewExecutedConfig() {
        def jsonContent = request.JSON
        log.info("json content received is := " + jsonContent)
        try {
            Long exConfigId = jsonContent.exConfigId
            String decodedId = configurationService.encodeId(exConfigId)
            redirect(action: "viewExecutedConfig", controller: "aggregateCaseAlert", id: decodedId)
        } catch (Exception ex) {
            log.error("Error redirecting view config", ex)
        }
    }

    def aggregateReportTemplate() {
        String jsonContent = request.JSON
        log.info("json content received is := " + jsonContent)
        try {
            Map dataMap = jsonContent ? jsonToMap(jsonContent) : null
            redirect(action: "index", controller: "template", params: dataMap)
        } catch (Exception ex) {
            log.error("Error redirecting template index", ex)
        }
    }

    String searchUserGroupList(String term, Integer page, Integer max, Boolean isAutoAssigned) {
        Map responseMap = [:]
            List items = userService.searchUserGroupList(term,page,max,isAutoAssigned)
            RestApiResponse.successResponseWithData(responseMap, null, items)
        render(responseMap as JSON)
    }

    String searchShareWithUserGroupList(String term, Integer page, Integer max, Boolean isWorkflowEnabled,Boolean isAutoAssigned) {
        Map responseMap = [:]
            List items = userService.searchShareWithUserGroupList(term,page,max,isWorkflowEnabled,isAutoAssigned)
            RestApiResponse.successResponseWithData(responseMap,null,items)
        render(responseMap as JSON)
    }

    //@Secured(['ROLE_AGGREGATE_CASE_REVIEWER', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    String changeAssignedToGroup() {
        String jsonContent = request.JSON
        log.info("json content received is := " + jsonContent)
        Map responseMap = [:]
        try {
            Map dataMap = jsonContent ? jsonToMap(jsonContent) : null
            List selectedId = dataMap.selectedId as List
            String assignedToValue = dataMap.assignedToValue
            Boolean isArchived = dataMap.isArchived ?: false
            if (selectedId && assignedToValue) {
                Map labelConfig = alertFieldService.getLabelConfigMap(Constants.AlertConfigType.UNDERSCORE_AGGREGATE_CASE_ALERT)
                String assignedToLabel = labelConfig.get("assignedTo")
                aggregateCaseAlertRestService.changeAssignedToGroup(selectedId, assignedToValue, isArchived, assignedToLabel)
                String resultMsg = message(code: 'app.assigned.changed.success', args: [assignedToLabel])
                RestApiResponse.successResponse(responseMap, resultMsg)
            } else {
                RestApiResponse.invalidParametersResponse(responseMap)
                render(status: HttpStatus.INTERNAL_SERVER_ERROR.value(),text:responseMap as JSON)
                return
            }
        } catch (ValidationException vx) {
            String errorMessage = MiscUtil.getCustomErrorMessage(vx)
            RestApiResponse.failureResponse(responseMap, errorMessage)
            render(status: HttpStatus.INTERNAL_SERVER_ERROR.value(),text:responseMap as JSON)
            return
        } catch (Exception ex) {
            log.error("Error changing assigned to group", ex)
            RestApiResponse.serverErrorResponse(responseMap)
            render(status: HttpStatus.INTERNAL_SERVER_ERROR.value(),text:responseMap as JSON)
            return
        }
        render(responseMap as JSON)
    }

    private getAggregateRuleJson() {
        def aggregateRules = grailsApplication.config.aggregateRules
        def aggregateArray = new JSONArray()
        aggregateRules.each {
            def eudraRulesObj = new JSONObject()
            eudraRulesObj.put("parameterName", it.parameterName)
            eudraRulesObj.put("value", it.value)
            eudraRulesObj.put("change", it.change)
            eudraRulesObj.put("color", it.color)
            aggregateArray.add(eudraRulesObj)
        }
        aggregateArray.toString()
    }

    private void alertDeletionInProgress() {
        redirect(action: "alertInProgressError", controller: 'errors')
    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message',
                        args: [message(code: 'configuration.label'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    String changePriorityOfAlert() {
        Map responseMap = [:]
        String jsonContent = request.JSON
        log.info("json content received is := " + jsonContent )
        User currentUser = userService.getUser()
        Map dataMap = jsonContent ? jsonToMap(jsonContent) : null
        Map data = aggregateCaseAlertRestService.changeAlertPriority(dataMap, currentUser)
        RestApiResponse.successResponseWithData(responseMap,null, data)
        render(responseMap as JSON)
    }

    String changeDisposition() {
        def responseData = [:]
        def data

        def requestBody = request.JSON
        List<Map> selectedRows = requestBody.selectedRows ?: []
        String justification = requestBody.justification
        String validatedSignalName = requestBody.validatedSignalName
        Long signalId = requestBody.signalId
        String productJson = requestBody.productJson
        Long targetDispositionId = requestBody.targetDispositionId
        String incomingDisposition = requestBody.incomingDisposition
        boolean isArchived = requestBody.isArchived ?: false
        String callingScreen = requestBody.callingScreen

        Disposition targetDisposition = Disposition.get(targetDispositionId)

        if (targetDisposition == null) {
            RestApiResponse.recordAbsentResponse(responseData, "Target disposition with id: ${targetDispositionId}, not found")
            render (status: 404, contentType: 'application/json', text: (responseData as JSON))
            return
        }

        List<Long> multipleExecutedConfigIdList = selectedRows.collect { Map selectedRow ->
            def id = selectedRow["exConfigId"]
            return id ? id as Long : null
        }.findAll { it != null }.unique()

//        if (multipleExecutedConfigIdList.size() > 1) {
//            RestApiResponse.failureResponse(responseData, message(code: "multiple.alert.disposition.change.error.PECs"))
//            render (status: 404, contentType: 'application/json', text: (responseData as JSON))
//            return
//        }

        try {
            validatedSignalName = validatedSignalName?.trim() ? StringEscapeUtils.unescapeHtml(validatedSignalName) : ''

            List<Long> aggCaseAlertIdList = selectedRows.collect { Map selectedRow ->
                def alertId = selectedRow["alertId"]
                return alertId ? alertId as Long : null
            }.findAll { it != null }.unique()

            def jsonSlurper = new JsonSlurper()
            def productJsonMap = [:]
            if (productJson) {
                productJsonMap = jsonSlurper.parseText(productJson) as Map<String, Object>
            }

            data = aggregateCaseAlertService.changeDisposition(aggCaseAlertIdList, targetDisposition,
                    justification, validatedSignalName, productJsonMap, isArchived, signalId, incomingDisposition)
            def domain = aggregateCaseAlertService.getDomainObject(isArchived)
            Long configId = domain?.get(aggCaseAlertIdList[0])?.executedAlertConfiguration?.id
            Long countOfPreviousDisposition
            if (callingScreen != Constants.Commons.DASHBOARD) {
                countOfPreviousDisposition = alertService.fetchPreviousDispositionCount(configId, incomingDisposition, domain)
            } else {
                countOfPreviousDisposition = alertService.fetchPreviousDispositionCountDashboard(incomingDisposition, domain)
            }
            data << [incomingDisposition : incomingDisposition, countOfPreviousDisposition : countOfPreviousDisposition]
            if (targetDispositionId) {
                aggregateCaseAlertService.persistAlertDueDate(data?.alertDueDateList)
            }
            if (!data.dispositionChanged) {
                RestApiResponse.failureResponse(responseData, message(code: "app.label.disposition.change.error.refresh"))
                render (status: 409, contentType: 'application/json', text: (responseData as JSON))
                return
            }
            RestApiResponse.successResponseWithData(responseData, message(code: "app.label.disposition.change.error.refresh"), data)
            aggregateCaseAlertService.notifyUpdateDispCountsForExConfigAndGroupedConfig(multipleExecutedConfigIdList)
        } catch (ValidationException vx) {
            vx.printStackTrace()
            RestApiResponse.failureResponse(responseData, MiscUtil.getCustomErrorMessageList(vx)[0])
            render (status: 500, contentType: 'application/json', text: (responseData as JSON))
            return
        }
        catch (Exception e) {
            e.printStackTrace()
            RestApiResponse.failureResponse(responseData, message(code: "app.label.disposition.change.error"))
            render (status: 500, contentType: 'application/json', text: (responseData as JSON))
            return
        }
        render (status: 200, contentType: 'application/json', text: (responseData as JSON))
    }

    def exportSignalSummaryReport() {
        def isArchived = params.boolean('isArchived')
        def selectedCases = params.selectedCases
        def filterList = params.filterList
        def cumulative = params.boolean('cumulative')
        def outputFormat = params.outputFormat
        def callingScreen = params.callingScreen
        List validatedSignalList = []
        List notStartedReviewSignalList = []
        List pendingReviewList = []
        List closedReviewList = []
        Map signalData = [:]
        ExecutedConfiguration ec = ExecutedConfiguration.findByIdAndIsEnabled(params.id, true)
        List criteriaSheetList = []
        Boolean isDashboard = false
        def entityValueForExport = ""
        def domainName = aggregateCaseAlertService.getDomainObject(isArchived)
        def user = userService.getUser()

        if (!user) {
            def resp = [:]
            RestApiResponse.accessDeniedResponse(resp)
            render(resp as JSON)
            return
        }

        Group workflowGroup = user.getWorkflowGroup()
        String defaultDispositionValue = workflowGroup?.defaultQuantDisposition?.value

        if (cumulative && (callingScreen == Constants.Commons.DASHBOARD || callingScreen == Constants.Commons.TAGS)) {
            isDashboard = true
            List<ExecutedConfiguration> executedConfigurationList = getExecutedConfigurationList(workflowGroup)
            List aggregateCaseAlertList = getAggregateCaseAlertList(domainName, executedConfigurationList, "executedAlertConfiguration")
            entityValueForExport = Constants.Commons.DASHBOARD

            aggregateCaseAlertList.each { def aga ->
                categorizeAggregateCaseAlert(aga, defaultDispositionValue, validatedSignalList,
                        closedReviewList, notStartedReviewSignalList, pendingReviewList)
            }

            signalData = [alertName: "", productName: "", dateRange: "", referenceNumber: "", cumulative: true]
        } else {
            List agaList = []
            entityValueForExport = ec?.getInstanceIdentifierForAuditLog()
            params.adhocRun = false
            AlertDataDTO alertDataDTO = setupAlertDataDTO(params, user, ec, cumulative, isArchived)

            if (selectedCases) {
                List resultList = aggregateCaseAlertService.listSelectedAlerts(selectedCases, alertDataDTO.domainName)
                agaList = resultList*.id
            } else {
                Map filterMap = [:]
                if (filterList) {
                    def jsonSlurper = new JsonSlurper()
                    filterMap = jsonSlurper.parseText(filterList)
                    if (filterMap.productName == "-1") filterMap.remove("productName")
                }
                if (ec?.masterExConfigId) {
                    filterMap.remove("productName")
                }
                List dispositionFilters = getFiltersFromParams(params.isFilterRequest?.toBoolean(), params)

                List<String> allowedProductsToUser = []
                if (alertService.isProductSecurity()) {
                    allowedProductsToUser = alertService.fetchAllowedProductsForConfiguration()
                }
                alertDataDTO.allowedProductsToUser = allowedProductsToUser
                alertDataDTO.filterMap = filterMap
                alertDataDTO.executedConfiguration = ec
                alertDataDTO.execConfigId = ec?.id
                alertDataDTO.configId = ec?.configId
                alertDataDTO.orderColumnMap = [name: params.column, dir: params.sorting]
                alertDataDTO.userId = user?.id
                alertDataDTO.cumulative = cumulative
                alertDataDTO.dispositionFilters = dispositionFilters

                agaList = alertService.getAlertFilterIdList(alertDataDTO)
            }

            List currentDispositionList = []
            List aggregateCaseAlertList = []
            if (agaList?.isEmpty() == false) {
                aggregateCaseAlertList = getAggregateCaseAlertList(domainName, agaList, "id")
            }
            if (aggregateCaseAlertList) {
                aggregateCaseAlertList.each { def aga ->
                    currentDispositionList.add(aga?.disposition?.displayName)
                    categorizeAggregateCaseAlert(aga, defaultDispositionValue, validatedSignalList,
                            closedReviewList, notStartedReviewSignalList, pendingReviewList)
                }
            }
            params.totalCount = agaList?.size()?.toString()
            def uniqueDispositions = currentDispositionList.toSet()
            String quickFilterDisposition = uniqueDispositions?.join(", ")
            params.quickFilterDisposition = quickFilterDisposition

            Configuration config = Configuration.findByName(ec?.name)
            def productName = ec.getProductNameList().size() < 1
                    ? getGroupNameFieldFromJson(config.getProductGroupSelection())
                    : ec.getProductNameList()
            List<Date> dateRange = ec?.executedAlertDateRangeInformation?.getReportStartAndEndDate()
            def reportDateRange = (dateRange && dateRange.size() == 2)
                    ? "${DateUtil.toDateString1(dateRange[0])} to ${DateUtil.toDateString1(dateRange[1])}"
                    : "-"
            def referenceNumber = ec?.referenceNumber ?: "-"
            signalData = [alertName      : ec?.name, productName: productName, dateRange: reportDateRange,
                          referenceNumber: referenceNumber, cumulative: false]
        }

        validatedSignalList = aggregateCaseAlertService.getSignalDetectionSummaryMap(validatedSignalList)
        notStartedReviewSignalList = aggregateCaseAlertService.getSignalDetectionSummaryMap(notStartedReviewSignalList)
        pendingReviewList = aggregateCaseAlertService.getSignalDetectionSummaryMap(pendingReviewList)
        closedReviewList = aggregateCaseAlertService.getSignalDetectionSummaryMap(closedReviewList)

        if (ec?.selectedDatasource == Constants.DataSource.JADER) {
            criteriaSheetList = aggregateCaseAlertService.getJaderAggregateCaseAlertCriteriaData(
                    ec, params, Constants.AlertConfigType.AGGREGATE_CASE_ALERT_JADER)
        } else {
            criteriaSheetList = aggregateCaseAlertService.getAggregateCaseAlertCriteriaData(ec, params)
        }

        params.criteriaSheetList = criteriaSheetList

        Map reportParamsMap = ["showCompanyLogo"  : true,
                               "showLogo"         : true,
                               "header"           : "Signal Detection Summary for Aggregate Alert",
                               "outputFormat"     : outputFormat,
                               'criteriaSheetList': criteriaSheetList]

        File reportFile = dynamicReportService.createSignalDetectionReport(validatedSignalList
                ? new JRMapCollectionDataSource(validatedSignalList) : null,
                notStartedReviewSignalList ? new JRMapCollectionDataSource(notStartedReviewSignalList) : null,
                pendingReviewList ? new JRMapCollectionDataSource(pendingReviewList) : null,
                closedReviewList ? new JRMapCollectionDataSource(closedReviewList) : null,
                signalData, reportParamsMap)

        renderReportOutputType(reportFile, params)

        signalAuditLogService.createAuditForExport(
                criteriaSheetList,
                entityValueForExport + ": Detection Summary",
                isDashboard == true
                        ? "Aggregate Review Dashboard"
                        : (Constants.AuditLog.AGGREGATE_REVIEW + (ec.isLatest ? "" : ": Archived Alert")),
                params,
                reportFile.name)
    }

    String listByExecutedConfig() {
        def respData = [:]
        def requestBody = request.JSON
        def selectedRows = requestBody.columns ?: []
        Map requestParams  = [
                "cumulative"            : requestBody.cumulative,
                "id"                    : requestBody.id,
                "isFilterRequest"       : requestBody.isFilterRequest,
                "isViewInstance"        : requestBody.isViewInstance,
                "filters"               : requestBody.filters,
                "dashboardFilter"       : requestBody.dashboardFilter,
                "alertType"             : requestBody.alertType,
                "viewId"                : requestBody.viewId,
                "callingScreen"         : requestBody.callingScreen,
                "isArchived"            : requestBody.isArchived,
                "length"                : requestBody.length,
                "start"                 : requestBody.start,
                "order"                 : requestBody.order,
                "advancedFilterId"      : requestBody.advancedFilterId,
                "adhocRun"              : requestBody.adhocRun,
                "tagName"               : requestBody.tagName,
                "frequency"             : requestBody.frequency,
                "advancedFilterChanged" : requestBody.advancedFilterChanged.toString(),
                "draw"                  : requestBody.draw,
                "dir"                   : requestBody.dir,
                "sortName"              : requestBody.sortName,
                "queryJSON"             : requestBody.queryJson,
                ]

        List selectedFields = selectedRows.collectMany { row ->
            def item = row.name
            item.contains("/") ? item.split("/") as List : [item]
        }

        Boolean cumulative = requestBody.cumulative
        Long configId = requestBody.configId
        Boolean isFilterRequest = requestBody.isFilterRequest
        Boolean isGroupedAlert = requestBody.isAlertBursting
        GroupedAlertInfo groupedAlertInfo = null
        if(isGroupedAlert){
            groupedAlertInfo = GroupedAlertInfo.get(configId as Long)
        }

        def startTime = System.currentTimeSeconds()
        String viewInstanceCheck = requestBody.isViewInstance

        ConcurrentHashMap finalMap = [recordsTotal: 0, recordsFiltered: 0, data: [], filters: [], configId: configId, visibleIdList: []]

        try {
            List filters = getFiltersFromParamsList(requestParams.isFilterRequest?.toBoolean(), requestParams)
            Map filterMap = alertService.prepareFilterMapForRest(selectedRows)
            if (!isGroupedAlert && ExecutedConfiguration.get(configId)?.masterExConfigId) {
                filterMap.remove("productName")
            }
            ViewInstance viewInstance = viewInstanceService.fetchSelectedViewInstance(requestParams.alertType,
                    requestParams.viewId as Long)
            Map sort =  JSON.parse(viewInstance.sorting)
            Map orderColumnMap = alertService.prepareOrderColumnMapForRest(requestParams)
            if (sort && viewInstanceCheck == "1") {
                orderColumnMap = [name: requestParams.sortName, dir: sort.values()[0]]
            }
            if (filterMap.assignedTo != null) {
                String name = filterMap.remove("assignedTo")
                filterMap.put("assignedToUser", name)
            }
            List<String> allowedProductsToUser = []
            if (alertService.isProductSecurity()) {
                allowedProductsToUser = alertService.fetchAllowedProductsForConfiguration()
            }
            ExecutedConfiguration executedConfig = null

            //Faers related check added to by-pass the product security.
            requestParams.isFaers = false
            if (!cumulative && requestParams.callingScreen != Constants.Commons.DASHBOARD
                    && requestParams.callingScreen != Constants.Commons.TAGS && !isGroupedAlert) {
                executedConfig = ExecutedConfiguration.get(configId)
                def selectedDataSource = executedConfig?.selectedDatasource ?: Constants.DataSource.PVA
                if (selectedDataSource == Constants.DataSource.FAERS) {
                    requestParams.isFaers = true
                }
                if (selectedDataSource == Constants.DataSource.JADER) {
                    requestParams.isJader = true
                }
            }

            User user = userService.getUser()
            String timeZone = userService.getCurrentUserPreference()?.timeZone
            AlertDataDTO alertDataDTO = new AlertDataDTO()
            alertDataDTO.params = requestParams
            alertDataDTO.allowedProductsToUser = allowedProductsToUser
            alertDataDTO.domainName = requestParams.isArchived ? ArchivedAggregateCaseAlert : AggregateCaseAlert
            alertDataDTO.executedConfiguration = executedConfig
            alertDataDTO.execConfigId = executedConfig?.id
            alertDataDTO.masterExConfigId = executedConfig?.masterExConfigId
            alertDataDTO.configId = executedConfig?.configId
            alertDataDTO.filterMap = filterMap
            alertDataDTO.timeZone = timeZone
            alertDataDTO.orderColumnMap = orderColumnMap
            alertDataDTO.userId = user.id
            alertDataDTO.workflowGroupId = user.workflowGroup.id
            alertDataDTO.groupIdList = user.groups.collect { it.id }
            alertDataDTO.cumulative = cumulative
            alertDataDTO.dispositionFilters = filters
            alertDataDTO.isJader = requestParams.isJader
            alertDataDTO.length = requestParams.length
            alertDataDTO.start = requestParams.start
            alertDataDTO.analysisStatusJson = requestBody.analysisStatusJson as Map

            Set dispositionSet
            if (requestParams.callingScreen == Constants.Commons.DASHBOARD) {
                dispositionSet = alertService.getDispositionSetDashboard(isFilterRequest, alertDataDTO)
            } else {
                dispositionSet = alertService.getDispositionSet(alertDataDTO.executedConfiguration,
                        alertDataDTO.domainName, isFilterRequest, requestParams, groupedAlertInfo)
            }
            long time1 = System.currentTimeMillis()
            Map filterCountAndList = alertService.getAlertFilterCountAndListForRest(alertDataDTO,
                    requestParams.callingScreen, false, selectedFields,groupedAlertInfo)
            long time2 = System.currentTimeMillis()
            log.info(((time2 - time1) / 1000) + " Secs were taken in getAlertFilterCountAndListForRest method")
            List<String> productNames = alertService.getDistinctProductName(alertDataDTO.domainName,
                    alertDataDTO, requestParams.callingScreen, groupedAlertInfo)
            boolean isMasterConfig = requestParams.callingScreen != Constants.Commons.DASHBOARD
                    ? alertDataDTO.masterExConfigId && productNames.size() >= 1 : false
            List<Map> productIdList = []
            if (isMasterConfig && requestParams.callingScreen != Constants.Commons.DASHBOARD) {
                productIdList = alertService.getProductIdMapForMasterConfig(alertDataDTO, requestParams.callingScreen)
            }
            if (alertService.isProductSecurity()) {
                List productList = productIdList ? productIdList.collect { it[0] } : []
                List commonList = allowedProductsToUser.intersect(productList)
                productIdList = productIdList.findAll { it[0] in commonList }
            }
            String currentProduct = productIdList.find { it[1] == executedConfig?.id }
                    ? productIdList.find { it[1] == executedConfig?.id }[0]
                    : productIdList != [] ? productIdList.find { it[1] }[0] : ''
            if (!filters?.isEmpty() || alertDataDTO.advancedFilterDispositions) {
                List visibleIdList = filterCountAndList?.resultList*.alertId
                finalMap = [recordsTotal: filterCountAndList.totalCount,
                            recordsFiltered: filterCountAndList.totalFilteredCount,
                            data: filterCountAndList.resultList,
                            filters: dispositionSet,
                            configId: configId,
                            productNameList :productNames.sort({it?.toUpperCase()}),
                            productIdList: productIdList.sort { it[0]?.toUpperCase() },
                            isMasterConfig: isMasterConfig,
                            visibleIdList: visibleIdList]
                if (currentProduct?:executedConfig?.productName) {
                    finalMap.put("currentProduct", currentProduct?:executedConfig?.productName)
                }
            } else {
                finalMap = [recordsTotal: filterCountAndList.totalCount, recordsFiltered: 0,
                            data: [], filters: dispositionSet, configId: configId, visibleIdList : []]
            }
            finalMap.put("advancedFilterDispName", alertDataDTO.advancedFilterDispName)
            finalMap.put("orderColumnMap", alertDataDTO.orderColumnMap)

        } catch (Throwable th) {
            log.error("Error changing assigned to group", th)
            RestApiResponse.serverErrorResponse(respData, th.getMessage())
            render(status: 500, contentType: 'application/json', text: (respData as JSON))
            return
        }

        def endTime=System.currentTimeSeconds()

        log.info("it took ${endTime-startTime} to load listByExecutedConfig")
//        RestApiResponse.successResponseWithData(respData, null, finalMap)
//        render(status: 200, contentType: 'application/json', text: (respData as JSON))
        render (finalMap as JSON)
    }

    String fetchFieldsSpecification() {
        def respData = [:]
        Long viewId = params.viewId as Long
        def callingScreen = params.callingScreen
        def configId = params.configId
        def isSmq = false

        if (!callingScreen || !configId) {
            RestApiResponse.invalidParametersResponse(respData, "parameter callingScreen and configId is missing")
            render(status: 400, contentType: 'application/json', text: (respData as JSON))
        }

        ViewInstance viewInstance = ViewInstance.get(viewId)

        if (!viewInstance || !viewInstance.columnSeq) {
            RestApiResponse.invalidParametersResponse(respData)
            render(status: HttpStatus.BAD_REQUEST.value(), text: respData as JSON)
            return
        }

        JsonSlurper js = new JsonSlurper()
        Map dataColumn = [:]
        if (viewInstance.tempColumnSeq) {
            js.parseText(viewInstance.tempColumnSeq).each { key, value ->
                if (value.get("containerView") == 1) {
                    dataColumn << [(value.get("name")): value.get("label")]
                }
            }
        } else {
            js.parseText(viewInstance.columnSeq).each { key, value ->
                if (value.get("containerView") == 1) {
                    dataColumn << [(value.get("name")): value.get("label")]
                }
            }
        }

        List fields = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT',
                null, null, null)
        boolean isPriorityEnabled = grailsApplication.config.alert.priority.enable

        List fieldsWithContainerView = fields.findAll { it.containerView == 1 }
        Map display = [:]
        fieldsWithContainerView.each {
            if (isPriorityEnabled && it.name.equals("priority")) {
                display.put(it.name, it.display)
            }
            if (!it.optional || it.name.equals("actions")) {
                display.put(it.name, it.display)
            }
        }
        int fixedCount = display.size()
        display.putAll(dataColumn)

        Map allFields = fields.collectEntries { map ->
            [(map.name): map]
        }
        List resultList = []

        def executedConfiguration = ExecutedConfiguration.findByIdAndIsEnabled(configId, true)
        Configuration configurationInstance = Configuration.get(executedConfiguration?.configId)
        String selectedDatasource = configurationInstance?.selectedDatasource

        isSmq = executedConfiguration?.groupBySmq
        def labelCondition = executedConfiguration?.groupBySmq ? true : viewInstanceService.isLabelChangeRequired(selectedDatasource)
        if (callingScreen == Constants.Commons.DASHBOARD) {
            labelCondition = !(Holders.config.dss.enable.autoProposed && Holders.config.statistics.enable.dss)
        }
        def isRor = cacheService.getRorCache()
        def c1 = Holders.config.dss.enable.autoProposed
        def c2 = callingScreen.equals("")
        boolean isDashboard = callingScreen.toLowerCase().contains("dashboard")

        display.each { k, v ->
            def matcher = k =~ /(exe\d+)(.*)/

            if (matcher.matches()) {
                def prefix = matcher[0][1]
                def field = matcher[0][2]
                if (allFields.containsKey(field)) {
                    resultList.add(getCustomFields(allFields[field], prefix, isSmq, labelCondition, isRor, c1, c2, isDashboard))
                }
            } else if (allFields.containsKey(k)) {
                resultList.add(getCustomFields(allFields[k], null, isSmq, labelCondition, isRor, c1, c2, isDashboard))
            }
        }

        def result = [
                columns: resultList,
                fixedCount: fixedCount
        ]

        RestApiResponse.successResponseWithData(respData, null, result)
        render(status: 200, contentType: 'application/json', text: (respData as JSON))
    }

    //@Secured(['ROLE_AGGREGATE_CASE_VIEWER', 'ROLE_VIEW_ALL', 'ROLE_FAERS_CONFIGURATION', 'ROLE_VAERS_CONFIGURATION', 'ROLE_VIGIBASE_CONFIGURATION','ROLE_JADER_CONFIGURATION'])
    def exportReport() {
        def startTime=System.currentTimeSeconds()

        List agaList = []
        List newCountAgaList = []
        List listDateRange = []
        Map dataSourceMap = [:]
        List newFields = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', true).collect { it.name }
        Boolean cumulative = userService.getUser()?.preference?.isCumulativeAlertEnabled ?: false
        boolean isExcelExport = isExcelExportFormat(params.outputFormat)
        params.adhocRun = false
        def adhocRun = params.adhocRun
        Integer previousExecutionsToConsider = Holders.config.signal.quantitative.number.prev.columns
        Boolean groupBySmq = false
        ExecutedConfiguration executedConfiguration = null
        Map criteriaData
        params.isFaers = params.boolean("isFaers")
        params.isVaers = params.boolean("isVaers")
        params.isVigibase = params.boolean("isVigibase")
        Boolean isDashboard = params.callingScreen == Constants.Commons.DASHBOARD

        if (!cumulative && !adhocRun?.toBoolean() && params.callingScreen != Constants.Commons.DASHBOARD && params.callingScreen != Constants.Commons.TAGS) {
            executedConfiguration = ExecutedConfiguration.findByIdAndIsEnabled(params.id, true)
            groupBySmq = executedConfiguration.groupBySmq
            def prevExecs = ExecutedConfiguration.findAllByName(executedConfiguration.name, [max: previousExecutionsToConsider])
            prevExecs.remove(executedConfiguration)
            prevExecs.each { it ->
                Date sdEx = it.executedAlertDateRangeInformation.dateRangeEndAbsolute
                listDateRange.add(Date.parse("dd-MMM-yyyy", DateUtil.toDateString1(sdEx)).format("dd/MMM/yy"))
            }
            def selectedDataSource = executedConfiguration?.selectedDatasource ?: Constants.DataSource.PVA
            if (selectedDataSource == Constants.DataSource.FAERS) {
                params.isFaers = true
            }
            if (selectedDataSource == Constants.DataSource.VAERS) {
                params.isVaers = true
            }
            if (selectedDataSource == Constants.DataSource.VIGIBASE) {
                params.isVigibase = true
            }
            if (selectedDataSource == Constants.DataSource.JADER) {
                params.isJader = true
            }
        } else if (cumulative && !adhocRun.toBoolean()) {
            //TODO check if needed
            Map freqDateRange = fetchDateRangeMap(params.frequency)
            freqDateRange.each { key, value ->
                if (key != 'exeRecent') {
                    listDateRange.add(value['startDate'].replaceAll("-", "/") + "-" + value['endDate'].replaceAll("-", "/"))
                }
            }
        }
        User user = userService.getUser()
        String timeZone = user?.preference?.timeZone
        params['listDateRange'] = listDateRange
        params['groupBySmq'] = groupBySmq
        AlertDataDTO alertDataDTO = new AlertDataDTO()
        alertDataDTO.params = params
        alertDataDTO.userId = user.id
        alertDataDTO.executedConfiguration = executedConfiguration
        alertDataDTO.execConfigId = executedConfiguration?.id
        alertDataDTO.cumulative = cumulative
        alertDataDTO.timeZone = timeZone
        alertDataDTO.isFromExport = true
        alertDataDTO.isJader = params.isJader
        alertDataDTO.workflowGroupId = user.workflowGroup.id
        alertDataDTO.groupIdList = user.groups.collect { it.id }

        alertDataDTO.domainName = aggregateCaseAlertService.getDomainObject(params.boolean('isArchived'))

        if (params.selectedCases) {
            List resultList = aggregateCaseAlertService.listSelectedAlerts(params.selectedCases, alertDataDTO.domainName)
            if (params.isJader) {
                agaList = jaderAlertService.fetchResultAlertListJader(resultList, alertDataDTO, params.callingScreen)
            } else {
                agaList = aggregateCaseAlertService.fetchResultAlertList(resultList, alertDataDTO, params.callingScreen)
            }
        } else {
            Map filterMap = [:]
            if (params.filterList) {
                def jsonSlurper = new JsonSlurper()
                filterMap = jsonSlurper.parseText(params.filterList)
                if (filterMap.productName=="-1") filterMap.remove("productName")
            }
            if (executedConfiguration?.masterExConfigId) {
                filterMap.remove("productName")
            }
            List dispositionFilters = getFiltersFromParams(params.isFilterRequest?.toBoolean(), params)

            if ((!cumulative && params.callingScreen != Constants.Commons.DASHBOARD) || params.adhocRun.toBoolean()) {
                executedConfiguration = ExecutedConfiguration.findByIdAndIsEnabled(params.id, true)
            }

            List<String> allowedProductsToUser = []
            if (alertService.isProductSecurity()) {
                allowedProductsToUser = alertService.fetchAllowedProductsForConfiguration()
            }
            alertDataDTO.allowedProductsToUser = allowedProductsToUser
            alertDataDTO.filterMap = filterMap
            alertDataDTO.executedConfiguration = executedConfiguration
            alertDataDTO.execConfigId = executedConfiguration?.id
            alertDataDTO.configId = executedConfiguration?.configId
            alertDataDTO.orderColumnMap = [name: params.column, dir: params.sorting]
            alertDataDTO.userId = userService.getUser().id
            alertDataDTO.cumulative = cumulative
            alertDataDTO.dispositionFilters = dispositionFilters
            alertDataDTO.length = isDashboard ? 5000 : 10000  //As per PO suggestion applied max limit as 10000 for export from details screen
            Map filterCountAndList = alertService.getAlertFilterCountAndList(alertDataDTO, params.callingScreen)
            agaList = filterCountAndList.resultList

        }
        if (alertDataDTO.dispositionFilters?.isEmpty()) {
            agaList = []
        }
        ConcurrentLinkedQueue currentDispositionQueue = new ConcurrentLinkedQueue()
        ExecutorService executorService = Executors.newFixedThreadPool(6)
        List subGroupingFields = Holders.config.subgrouping.pva.subGroupColumnsList?.keySet() as List
        List ebgmSubGroupingFields = ["ebgm","eb05","eb95"]
        List relativeSubGroupingFields = ["ror","rorLci","rorUci"]
        Integer maxPrevExeFieldNum = aggregateCaseAlertService.getTheMaxExeNumber(params)
        maxPrevExeFieldNum = Math.min(maxPrevExeFieldNum, previousExecutionsToConsider)
        List<Future> futureList = []
        if (executedConfiguration?.selectedDatasource == Constants.DataSource.JADER) {
            futureList = jaderAlertService.prepareJaderAlertExportData(agaList,isExcelExport,previousExecutionsToConsider,executorService)
        } else {
            boolean selectedDatasourceContainsFaers = executedConfiguration?.selectedDatasource?.contains("faers")
            boolean statisticsEbgmEnabled = grailsApplication.config.statistics.enable.ebgm
            boolean statisticsFaersEbgmEnabled = grailsApplication.config.statistics.faers.enable.ebgm
            boolean statisticsRrrOrRorEnabled = grailsApplication.config.statistics.enable.prr || grailsApplication.config.statistics.enable.ror
            Map subGroupColumnsCamelCaseMap = cacheService.getAllOtherSubGroupColumnsCamelCase(Constants.DataSource.PVA)
            Map subGroupMap = cacheService.getSubGroupMap()
            futureList = agaList.collect { it ->
                executorService.submit({ ->
                    it.name = it?.alertName
                    it.pt = it.preferredTerm
                    it.newStudyCount = isExcelExport ? "" + it.newStudyCount : "    " + it.newStudyCount + "\n    " + it.cumStudyCount
                    it.newCount = isExcelExport ? "" + it.newCount : "    " + it.newCount + "\n    " + it.cummCount
                    it.newPediatricCount = isExcelExport ? "" + it.newPediatricCount : "    " + it.newPediatricCount + "\n    " + it.cummPediatricCount
                    it.newInteractingCount = isExcelExport ? "" + it.newInteractingCount : "    " + it.newInteractingCount + "\n    " + it.cummInteractingCount
                    it.newSponCount = isExcelExport ? "" + it.newSponCount : "    " + it.newSponCount + "\n   " + it.cumSponCount
                    it.newSeriousCount = isExcelExport ? "" + it.newSeriousCount : "    " + it.newSeriousCount + "\n    " + it.cumSeriousCount
                    it.newFatalCount = isExcelExport ? "" + it.newFatalCount : "    " + it.newFatalCount + "\n    " + it.cumFatalCount
                    it.newGeriatricCount = isExcelExport ? "" + it.newGeriatricCount : "    " + it.newGeriatricCount + "\n    " + it.cumGeriatricCount
                    it.newNonSerious = isExcelExport ? "" + it.newNonSerious : "    " + it.newNonSerious + "\n    " + it.cumNonSerious
                    it.prrLCI = isExcelExport ? "" + it.prrLCI : "    " + it.prrLCI + "\n    " + it.prrUCI
                    it.rorLCI = isExcelExport ? "" + it.rorLCI : "    " + it.rorLCI + "\n    " + it.rorUCI
                    it.eb05 = isExcelExport ? "" + it.eb05 + "" : "    " + it.eb05 + "" + "\n    " + it.eb95 + ""
                    it.newProdCount = isExcelExport ? "" + it.newProdCount + "" : "    " + it.newProdCount + "" + "\n    " + it.cumProdCount + ""
                    it.freqPeriod = isExcelExport ? "" + it.freqPeriod + "" : "    " + it.freqPeriod + "" + "\n    " + it.cumFreqPeriod + ""
                    it.reviewedFreqPeriod = isExcelExport ? "" + it.reviewedFreqPeriod + "" : "    " + it.reviewedFreqPeriod + "" + "\n    " + it.reviewedCumFreqPeriod + ""
                    it.trendFlag = "" + it.trendFlag

                    it.ebgm = "" + it.ebgm
                    it.chiSquare = (it.chiSquare as String != '-' && it.chiSquare != -1) ? ("" + (it.chiSquare)) : it.chiSquare != -1 ? it.chiSquare : '-'
                    it.aValue = (it.aValue as String != '-' && it.aValue != -1) ? ("" + (it.aValue as Integer)) : it.aValue != -1 ? it.aValue : '-'
                    it.bValue = (it.bValue as String != '-' && it.bValue != -1) ? ("" + (it.bValue as Integer)) : it.bValue != -1 ? it.bValue : '-'
                    it.cValue = (it.cValue as String != '-' && it.cValue != -1) ? ("" + (it.cValue as Integer)) : it.cValue != -1 ? it.cValue : '-'
                    it.dValue = (it.dValue as String != '-' && it.dValue != -1) ? ("" + (it.dValue as Integer)) : it.dValue != -1 ? it.dValue : '-'
                    it.eValue = "" + it.eValue
                    it.rrValue = "" + it.rrValue

                    it.pecImpHigh = "" + it.highPecImp
                    it.lowPecImp = "" + it.pecImpLow
                    it.dueDate = it.dueIn + ""
                    it.soc = dataObjectService.getAbbreviationMap(it.soc) + ""

                    it.newCountFaers = isExcelExport ? "" + it.newCountFaers : "    " + it.newCountFaers + "\n    " + it.cummCountFaers
                    it.newSeriousCountFaers = isExcelExport ? "" + it.newSeriousCountFaers : "    " + it.newSeriousCountFaers + "\n    " + it.cumSeriousCountFaers
                    it.newStudyCountFaers = isExcelExport ? "" + it.newStudyCountFaers : "    " + it.newStudyCountFaers + "\n    " + it.cumStudyCountFaers
                    it.newPediatricCountFaers = isExcelExport ? "" + it.newPediatricCountFaers : "    " + it.newPediatricCountFaers + "\n    " + it.cummPediatricCountFaers
                    it.newInteractingCountFaers = isExcelExport ? "" + it.newInteractingCountFaers : "    " + it.newInteractingCountFaers + "\n    " + it.cummInteractingCountFaers
                    it.newSponCountFaers = isExcelExport ? "" + it.newSponCountFaers : "    " + it.newSponCountFaers + "\n    " + it.cumSponCountFaers
                    it.newGeriatricCountFaers = isExcelExport ? "" + it.newGeriatricCountFaers : "    " + it.newGeriatricCountFaers + "\n    " + it.cumGeriatricCountFaers
                    it.newNonSeriousFaers = isExcelExport ? "" + it.newNonSeriousFaers : "    " + it.newNonSeriousFaers + "\n    " + it.cumNonSeriousFaers
                    it.newFatalCountFaers = isExcelExport ? "" + it.newFatalCountFaers : "    " + it.newFatalCountFaers + "\n    " + it.cumFatalCountFaers
                    it.prrLCIFaers = isExcelExport ? "" + it.prrLCIFaers : "    " + it.prrLCIFaers + "\n    " + it.prrUCIFaers
                    it.prrValueFaers = "" + it.prrValueFaers
                    it.rorValueFaers = "" + it.rorValueFaers
                    it.rorLCIFaers = isExcelExport ? "" + it.rorLCIFaers : "    " + it.rorLCIFaers + "\n    " + it.rorUCIFaers
                    it.eb05Faers = isExcelExport ? "" + it.eb05Faers : "    " + it.eb05Faers + "" + "\n    " + it.eb95Faers + ""
                    it.ebgmFaers = "" + it.ebgmFaers
                    it.chiSquareFaers = "" + it.chiSquareFaers

                    it.newCountVaers = isExcelExport ? "" + it.newCountVaers : "    " + it.newCountVaers + "\n    " + it.cummCountVaers
                    it.newSeriousCountVaers = isExcelExport ? "" + it.newSeriousCountVaers : "    " + it.newSeriousCountVaers + "\n    " + it.cumSeriousCountVaers
                    it.newPediatricCountVaers = isExcelExport ? "" + it.newPediatricCountVaers : "    " + it.newPediatricCountVaers + "\n    " + it.cummPediatricCountVaers
                    it.newGeriatricCountVaers = isExcelExport ? "" + it.newGeriatricCountVaers : "    " + it.newGeriatricCountVaers + "\n    " + it.cumGeriatricCountVaers
                    it.newFatalCountVaers = isExcelExport ? "" + it.newFatalCountVaers : "    " + it.newFatalCountVaers + "\n    " + it.cumFatalCountVaers
                    it.prrLCIVaers = isExcelExport ? "" + it.prrLCIVaers : "    " + it.prrLCIVaers + "\n    " + it.prrUCIVaers
                    it.prrValueVaers = "" + it.prrValueVaers
                    it.rorValueVaers = "" + it.rorValueVaers
                    it.rorLCIVaers = isExcelExport ? "" + it.rorLCIVaers : "    " + it.rorLCIVaers + "\n    " + it.rorUCIVaers
                    it.eb05Vaers = isExcelExport ? "" + it.eb05Vaers : "    " + it.eb05Vaers + "" + "\n    " + it.eb95Vaers + ""
                    it.ebgmVaers = "" + it.ebgmVaers
                    it.chiSquareVaers = "" + it.chiSquareVaers
                    it.newCountVigibase = isExcelExport ? "" + it.newCountVigibase : "    " + it.newCountVigibase + "\n    " + it.cummCountVigibase
                    it.newSeriousCountVigibase = isExcelExport ? "" + it.newSeriousCountVigibase : "    " + it.newSeriousCountVigibase + "\n    " + it.cumSeriousCountVigibase
                    it.newPediatricCountVigibase = isExcelExport ? "" + it.newPediatricCountVigibase : "    " + it.newPediatricCountVigibase + "\n    " + it.cummPediatricCountVigibase
                    it.newGeriatricCountVigibase = isExcelExport ? "" + it.newGeriatricCountVigibase : "    " + it.newGeriatricCountVigibase + "\n    " + it.cumGeriatricCountVigibase
                    it.newFatalCountVigibase = isExcelExport ? "" + it.newFatalCountVigibase : "    " + it.newFatalCountVigibase + "\n    " + it.cumFatalCountVigibase
                    it.prrLCIVigibase = isExcelExport ? "" + it.prrLCIVigibase : "    " + it.prrLCIVigibase + "\n    " + it.prrUCIVigibase
                    it.prrValueVigibase = "" + it.prrValueVigibase
                    it.rorValueVigibase = "" + it.rorValueVigibase
                    it.rorLCIVigibase = isExcelExport ? "" + it.rorLCIVigibase : "    " + it.rorLCIVigibase + "\n    " + it.rorUCIVigibase
                    it.eb05Vigibase = isExcelExport ? "" + it.eb05Vigibase : "    " + it.eb05Vigibase + "" + "\n    " + it.eb95Vigibase + ""
                    it.ebgmVigibase = "" + it.ebgmVigibase
                    it.chiSquareVigibase = "" + it.chiSquareVigibase
                    it.newFatalEvdas = isExcelExport ? "" + it.newFatalEvdas : "    " + it.newFatalEvdas + "\n    " + it.totalFatalEvdas
                    it.newSeriousEvdas = isExcelExport ? "" + it.newSeriousEvdas : "    " + it.newSeriousEvdas + "\n    " + it.totalSeriousEvdas
                    it.newEvEvdas = isExcelExport ? "" + it.newEvEvdas : "    " + it.newEvEvdas + "\n    " + it.totalEvEvdas
                    it.newLitEvdas = isExcelExport ? "" + it.newLitEvdas : "    " + it.newLitEvdas + "\n    " + it.totalLitEvdas
                    it.newEeaEvdas = isExcelExport ? "" + it.newEeaEvdas : "    " + it.newEeaEvdas + "\n    " + it.totEeaEvdas
                    it.newHcpEvdas = isExcelExport ? "" + it.newHcpEvdas : "    " + it.newHcpEvdas + "\n    " + it.totHcpEvdas
                    it.newMedErrEvdas = isExcelExport ? "" + it.newMedErrEvdas : "    " + it.newMedErrEvdas + "\n    " + it.totMedErrEvdas
                    it.newObsEvdas = isExcelExport ? "" + it.newObsEvdas : "    " + it.newObsEvdas + "\n    " + it.totObsEvdas
                    it.newRcEvdas = isExcelExport ? "" + it.newRcEvdas : "    " + it.newRcEvdas + "\n    " + it.totRcEvdas
                    it.newPaedEvdas = isExcelExport ? "" + it.newPaedEvdas : "    " + it.newPaedEvdas + "\n    " + it.totPaedEvdas
                    it.newGeriaEvdas = isExcelExport ? "" + it.newGeriaEvdas : "    " + it.newGeriaEvdas + "\n    " + it.totGeriaEvdas
                    it.newSpontEvdas = isExcelExport ? "" + it.newSpontEvdas : "    " + it.newSpontEvdas + "\n    " + it.totSpontEvdas
                    Map countMap = new HashMap()
                    for (int i = 0; i < newFields.size(); i++) {
                        String newCount = '', cumCount = ''
                        String fieldName = newFields.get(i)
                        String fieldData = it[fieldName]

                        if (fieldName in ['hlt', 'hlgt', 'smqNarrow']) {
                            newCount = fieldData?.toString()?.replaceAll(/<br>|<BR>/, "\n") ?: Constants.Commons.DASH_STRING
                            countMap.put(fieldName, newCount)
                        } else {
                            if (fieldData&& !fieldData.equals(Constants.Commons.DASH_STRING)) {
                                Map fieldDataMap = JSON.parse(fieldData) as Map
                                newCount = fieldDataMap.new
                                cumCount = fieldDataMap.cum
                            } else {
                                newCount = Constants.Commons.DASH_STRING
                                cumCount = Constants.Commons.DASH_STRING
                            }
                            String newCountString = fieldName.toString()
                            String cumCountString = newCountString.replace("new", "cum")

                            countMap.put(newCountString, isExcelExport ? "" + newCount : "    " + newCount + "\n    " + cumCount)
                            if (isExcelExport) {
                                countMap.put(cumCountString, isExcelExport ? "" + cumCount : "    " + newCount + "\n    " + cumCount)
                            }
                        }

                    }
                    it << countMap

                    if (isExcelExport) {
                        it.cumGeriatricCount = "" + it.cumGeriatricCount
                        it.cumNonSerious = "" + it.cumNonSerious
                        it.cumStudyCount = "" + it.cumStudyCount
                        it.cummCount = "" + it.cummCount
                        it.cummPediatricCount = "" + it.cummPediatricCount
                        it.cummInteractingCount = "" + it.cummInteractingCount
                        it.cumSponCount = "" + it.cumSponCount
                        it.cumSeriousCount = "" + it.cumSeriousCount
                        it.cumFatalCount = "" + it.cumFatalCount
                        it.prrUCI = "" + it.prrUCI
                        it.rorUCI = "" + it.rorUCI
                        it.eb95 = "" + it.eb95
                        it.cumProdCount = "" + it.cumProdCount
                        it.cumFreqPeriod = "" + it.cumFreqPeriod
                        it.reviewedCumFreqPeriod = "" + it.reviewedCumFreqPeriod
                        it.cummCountFaers = "" + it.cummCountFaers
                        it.cumSeriousCountFaers = "" + it.cumSeriousCountFaers
                        it.cumStudyCountFaers = "" + it.cumStudyCountFaers
                        it.cummPediatricCountFaers = "" + it.cummPediatricCountFaers
                        it.cummInteractingCountFaers = "" + it.cummInteractingCountFaers
                        it.cumSponCountFaers = "" + it.cumSponCountFaers
                        it.cumFatalCountFaers = "" + it.cumFatalCountFaers
                        it.cumGeriatricCountFaers = "" + it.cumGeriatricCountFaers
                        it.cumNonSeriousFaers = "" + it.cumNonSeriousFaers
                        it.prrUCIFaers = "" + it.prrUCIFaers
                        it.rorUCIFaers = "" + it.rorUCIFaers
                        it.eb95Faers = "" + it.eb95Faers
                        it.cummCountVaers = "" + it.cummCountVaers
                        it.cumSeriousCountVaers = "" + it.cumSeriousCountVaers
                        it.cummPediatricCountVaers = "" + it.cummPediatricCountVaers
                        it.cumGeriatricCountVaers = "" + it.cumGeriatricCountVaers
                        it.cumFatalCountVaers = "" + it.cumFatalCountVaers
                        it.prrUCIVaers = "" + it.prrUCIVaers
                        it.rorUCIVaers = "" + it.rorUCIVaers
                        it.eb95Vaers = "" + it.eb95Vaers
                        it.cummCountVigibase = "" + it.cummCountVigibase
                        it.cumSeriousCountVigibase = "" + it.cumSeriousCountVigibase
                        it.cummPediatricCountVigibase = "" + it.cummPediatricCountVigibase
                        it.cumGeriatricCountVigibase = "" + it.cumGeriatricCountVigibase
                        it.cumFatalCountVigibase = "" + it.cumFatalCountVigibase
                        it.prrUCIVigibase = "" + it.prrUCIVigibase
                        it.rorUCIVigibase = "" + it.rorUCIVigibase
                        it.eb95Vigibase = "" + it.eb95Vigibase
                        it.totalFatalEvdas = "" + it.totalFatalEvdas
                        it.totalSeriousEvdas = "" + it.totalSeriousEvdas
                        it.totalEvEvdas = "" + it.totalEvEvdas
                        it.totalLitEvdas = "" + it.totalLitEvdas
                        it.totEeaEvdas = "" + it.totEeaEvdas
                        it.totHcpEvdas = "" + it.totHcpEvdas
                        it.totMedErrEvdas = "" + it.totMedErrEvdas
                        it.totObsEvdas = "" + it.totObsEvdas
                        it.totRcEvdas = "" + it.totRcEvdas
                        it.totPaedEvdas = "" + it.totPaedEvdas
                        it.totGeriaEvdas = "" + it.totGeriaEvdas
                        it.totSpontEvdas = "" + it.totSpontEvdas
                    }
                    if (it.pecImpNumHigh && it.pecImpNumHigh != '-') {
                        if (it.rationale && it.rationale != '-') {
                            it.rationale = it.rationale + " " + it.pecImpNumHigh
                        } else {
                            it.rationale = it.pecImpNumHigh
                        }
                    } else {
                        it.rationale = Constants.Commons.BLANK_STRING
                    }

                    if (groupBySmq) {
                        it.rationale = ""
                    }
                    String signalTopics = ""
                    signalTopics = it.signalsAndTopics.collect { it.name }?.join(",")
                    it.signalsAndTopics = signalTopics
                    it.assignedTo = userService.getAssignedToName(it)
                    List<String> tagsList = []
                    it.alertTags.each { tag ->
                        String tagString = ""
                        if (tag.subTagText == null) {
                            tagString = tagString + tag.tagText + tag.privateUser + tag.tagType
                        } else {
                            String subTags = tag.subTagText.split(";").join("(S);")
                            tagString = tagString + tag.tagText + tag.privateUser + tag.tagType + " : " + subTags + "(S)"
                        }
                        tagsList.add(tagString)

                    }
                    it.alertTags = tagsList.join(", ")
                    it.currentDisposition = it.disposition
                    if (statisticsEbgmEnabled || statisticsFaersEbgmEnabled) {
                        if (selectedDatasourceContainsFaers) {
                            it << fetchEachSubCategory(subGroupMap, Holders.config.subgrouping.faers.ageGroup.name, '', it, "Age")
                            it << fetchEachSubCategory(subGroupMap, Holders.config.subgrouping.faers.gender.name, '', it, "Gender")
                            if (executedConfiguration?.selectedDatasource?.contains("pva")) {
                                it << fetchEachSubCategory(subGroupMap, Holders.config.subgrouping.ageGroup.name, '', it, "Age")
                                it << fetchEachSubCategory(subGroupMap, Holders.config.subgrouping.gender.name, '', it, "Gender")
                                ebgmSubGroupingFields?.each { category ->
                                    it << fetchAllSubCategoryColumn(subGroupColumnsCamelCaseMap, category, null, '', it)
                                }
                            }
                        } else {
                            it << fetchEachSubCategory(subGroupMap, Holders.config.subgrouping.ageGroup.name, '', it, "Age")
                            it << fetchEachSubCategory(subGroupMap, Holders.config.subgrouping.gender.name, '', it, "Gender")
                            ebgmSubGroupingFields?.each { category ->
                                it << fetchAllSubCategoryColumn(subGroupColumnsCamelCaseMap as Map, category, null, '', it)
                            }
                        }
                    }
                    if ((statisticsRrrOrRorEnabled)) {
                        subGroupingFields?.each { category ->
                            it << fetchAllSubCategoryColumn(subGroupColumnsCamelCaseMap, category, null, '', it)
                        }
                        relativeSubGroupingFields?.each { category ->
                            it << fetchAllSubCategoryColumn(subGroupColumnsCamelCaseMap, category, null, "Rel", it)
                        }
                    }
                    currentDispositionQueue.add(it?.currentDisposition)

                    if (maxPrevExeFieldNum >= 0) {
                        (0..maxPrevExeFieldNum).each { exeNum ->
                            String exeName = 'exe' + exeNum
                            it.put(exeName + 'newSponCount', isExcelExport ? "" + it[exeName]?.newSponCount : "    " + it[exeName]?.newSponCount + "\n    " + it[exeName]?.cumSponCount)
                            it.put(exeName + 'newSeriousCount', isExcelExport ? "" + it[exeName]?.newSeriousCount : "    " + it[exeName]?.newSeriousCount + "\n    " + it[exeName]?.cumSeriousCount)
                            it.put(exeName + 'newFatalCount', isExcelExport ? "" + it[exeName]?.newFatalCount : "    " + it[exeName]?.newFatalCount + "\n    " + it[exeName]?.cumFatalCount)
                            it.put(exeName + 'newStudyCount', isExcelExport ? "" + it[exeName]?.newStudyCount : "    " + it[exeName]?.newStudyCount + "\n    " + it[exeName]?.cumStudyCount)
                            it.put(exeName + 'newCount', isExcelExport ? "" + it[exeName]?.newCount : "    " + it[exeName]?.newCount + "\n    " + it[exeName]?.cummCount)
                            it.put(exeName + 'newInteractingCount', isExcelExport ? "" + it[exeName]?.newInteractingCount : "    " + it[exeName]?.newInteractingCount + "\n    " + it[exeName]?.cummInteractingCount)
                            it.put(exeName + 'newPediatricCount', isExcelExport ? "" + it[exeName]?.newPediatricCount : "    " + it[exeName]?.newPediatricCount + "\n    " + it[exeName]?.cummPediatricCount)
                            it.put(exeName + 'newNonSerious', isExcelExport ? "" + it[exeName]?.newNonSerious : "    " + it[exeName]?.newNonSerious + "\n    " + it[exeName]?.cumNonSerious)
                            it.put(exeName + 'newGeriatricCount', isExcelExport ? "" + it[exeName]?.newGeriatricCount : "    " + it[exeName]?.newGeriatricCount + "\n    " + it[exeName]?.cumGeriatricCount)
                            it.put(exeName + 'prrLCI', isExcelExport ? "" + it[exeName]?.prrLCI : "    " + it[exeName]?.prrLCI + "\n    " + it[exeName]?.prrUCI)
                            it.put(exeName + 'prrValue', "" + it[exeName]?.prrValue)
                            it.put(exeName + 'rorLCI', isExcelExport ? "" + it[exeName]?.rorLCI : "    " + it[exeName]?.rorLCI + "\n    " + it[exeName]?.rorUCI)
                            it.put(exeName + 'rorValue', "" + it[exeName]?.rorValue)
                            it.put(exeName + 'ebgm', "" + it[exeName]?.ebgm)
                            it.put(exeName + 'eb05', isExcelExport ? "" + it[exeName]?.eb05 : "    " + it[exeName]?.eb05 + "\n    " + it[exeName]?.eb95)
                            it.put(exeName + 'eb05Age', "    " + it[exeName]?.eb05Age)
                            it.put(exeName + 'eb05Gender', "    " + it[exeName]?.eb05Gender)
                            it.put(exeName + 'rrValue', "    " + it[exeName]?.rrValue)
                            it.put(exeName + 'newCountFaers', isExcelExport ? "" + it[exeName]?.newCountFaers : "    " + it[exeName]?.newCountFaers + "\n    " + it[exeName]?.cummCountFaers)
                            it.put(exeName + 'newSeriousCountFaers', isExcelExport ? "" + it[exeName]?.newSeriousCountFaers : "    " + it[exeName]?.newSeriousCountFaers + "\n    " + it[exeName]?.cumSeriousCountFaers)
                            it.put(exeName + 'eb05Faers', isExcelExport ? "" + it[exeName]?.eb05Faers : "    " + it[exeName]?.eb05Faers + "\n    " + it[exeName]?.eb95Faers)
                            it.put(exeName + 'newSponCountFaers', isExcelExport ? "" + it[exeName]?.newSponCountFaers : "    " + it[exeName]?.newSponCountFaers + "\n    " + it[exeName]?.cumSponCountFaers)
                            it.put(exeName + 'newStudyCountFaers', isExcelExport ? "" + it[exeName]?.newStudyCountFaers : "    " + it[exeName]?.newStudyCountFaers + "\n    " + it[exeName]?.cumStudyCountFaers)
                            it.put(exeName + 'prrLCIFaers', isExcelExport ? "" + it[exeName]?.prrLCIFaers : "    " + it[exeName]?.prrLCIFaers + "\n    " + it[exeName]?.prrUCIFaers)
                            it.put(exeName + 'newPediatricCountFaers', isExcelExport ? "" + it[exeName]?.newPediatricCountFaers : "    " + it[exeName]?.newPediatricCountFaers + "\n    " + it[exeName]?.cummPediatricCountFaers)
                            it.put(exeName + 'newInteractingCountFaers', isExcelExport ? "" + it[exeName]?.newInteractingCountFaers : "    " + it[exeName]?.newInteractingCountFaers + "\n    " + it[exeName]?.cummInteractingCountFaers)
                            it.put(exeName + 'newFatalCountFaers', isExcelExport ? "" + it[exeName]?.newFatalCountFaers : "    " + it[exeName]?.newFatalCountFaers + "\n    " + it[exeName]?.cumFatalCountFaers)
                            it.put(exeName + 'newNonSeriousFaers', isExcelExport ? "" + it[exeName]?.newNonSeriousFaers : "    " + it[exeName]?.newNonSeriousFaers + "\n    " + it[exeName]?.cumNonSeriousFaers)
                            it.put(exeName + 'newGeriatricCountFaers', isExcelExport ? "" + it[exeName]?.newGeriatricCountFaers : "    " + it[exeName]?.newGeriatricCountFaers + "\n    " + it[exeName]?.cumGeriatricCountFaers)
                            it.put(exeName + 'rorLCIFaers', isExcelExport ? "" + it[exeName]?.rorLCIFaers : "    " + it[exeName]?.rorLCIFaers + "\n    " + it[exeName]?.rorUCIFaers)
                            it.put(exeName + 'newCountVaers', isExcelExport ? "" + it[exeName]?.newCountVaers : "    " + it[exeName]?.newCountVaers + "\n    " + it[exeName]?.cummCountVaers)
                            it.put(exeName + 'newSeriousCountVaers', isExcelExport ? "" + it[exeName]?.newSeriousCountVaers : "    " + it[exeName]?.newSeriousCountVaers + "\n    " + it[exeName]?.cumSeriousCountVaers)
                            it.put(exeName + 'eb05Vaers', isExcelExport ? "" + it[exeName]?.eb05Vaers : "    " + it[exeName]?.eb05Vaers + "\n    " + it[exeName]?.eb95Vaers)
                            it.put(exeName + 'prrLCIVaers', isExcelExport ? "" + it[exeName]?.prrLCIVaers : "    " + it[exeName]?.prrLCIVaers + "\n    " + it[exeName]?.prrUCIVaers)
                            it.put(exeName + 'newPediatricCountVaers', isExcelExport ? "" + it[exeName]?.newPediatricCountVaers : "    " + it[exeName]?.newPediatricCountVaers + "\n    " + it[exeName]?.cummPediatricCountVaers)
                            it.put(exeName + 'newFatalCountVaers', isExcelExport ? "" + it[exeName]?.newFatalCountVaers : "    " + it[exeName]?.newFatalCountVaers + "\n    " + it[exeName]?.cumFatalCountVaers)
                            it.put(exeName + 'newGeriatricCountVaers', isExcelExport ? "" + it[exeName]?.newGeriatricCountVaers : "    " + it[exeName]?.newGeriatricCountVaers + "\n    " + it[exeName]?.cumGeriatricCountVaers)
                            it.put(exeName + 'rorLCIVaers', isExcelExport ? "" + it[exeName]?.rorLCIVaers : "    " + it[exeName]?.rorLCIVaers + "\n    " + it[exeName]?.rorUCIVaers)
                            it.put(exeName + 'newCountVigibase', isExcelExport ? "" + it[exeName]?.newCountVigibase : "    " + it[exeName]?.newCountVigibase + "\n    " + it[exeName]?.cummCountVigibase)
                            it.put(exeName + 'newSeriousCountVigibase', isExcelExport ? "" + it[exeName]?.newSeriousCountVigibase : "    " + it[exeName]?.newSeriousCountVigibase + "\n    " + it[exeName]?.cumSeriousCountVigibase)
                            it.put(exeName + 'eb05Vigibase', isExcelExport ? "" + it[exeName]?.eb05Vigibase : "    " + it[exeName]?.eb05Vigibase + "\n    " + it[exeName]?.eb95Vigibase)
                            it.put(exeName + 'prrLCIVigibase', isExcelExport ? "" + it[exeName]?.prrLCIVigibase : "    " + it[exeName]?.prrLCIVigibase + "\n    " + it[exeName]?.prrUCIVigibase)
                            it.put(exeName + 'newPediatricCountVigibase', isExcelExport ? "" + it[exeName]?.newPediatricCountVigibase : "    " + it[exeName]?.newPediatricCountVigibase + "\n    " + it[exeName]?.cummPediatricCountVigibase)
                            it.put(exeName + 'newFatalCountVigibase', isExcelExport ? "" + it[exeName]?.newFatalCountVigibase : "    " + it[exeName]?.newFatalCountVigibase + "\n    " + it[exeName]?.cumFatalCountVigibase)
                            it.put(exeName + 'newGeriatricCountVigibase', isExcelExport ? "" + it[exeName]?.newGeriatricCountVigibase : "    " + it[exeName]?.newGeriatricCountVigibase + "\n    " + it[exeName]?.cumGeriatricCountVigibase)
                            it.put(exeName + 'rorLCIVigibase', isExcelExport ? "" + it[exeName]?.rorLCIVigibase : "    " + it[exeName]?.rorLCIVigibase + "\n    " + it[exeName]?.rorUCIVigibase)
                            it.put(exeName + 'newEeaEvdas', isExcelExport ? "" + it[exeName]?.newEeaEvdas : "    " + it[exeName]?.newEeaEvdas + "\n    " + it[exeName]?.totEeaEvdas)
                            it.put(exeName + 'newHcpEvdas', isExcelExport ? "" + it[exeName]?.newHcpEvdas : "    " + it[exeName]?.newHcpEvdas + "\n    " + it[exeName]?.totHcpEvdas)
                            it.put(exeName + 'newSeriousEvdas', isExcelExport ? "" + it[exeName]?.newSeriousEvdas : "    " + it[exeName]?.newSeriousEvdas + "\n    " + it[exeName]?.totalSeriousEvdas)
                            it.put(exeName + 'newMedErrEvdas', isExcelExport ? "" + it[exeName]?.newMedErrEvdas : "    " + it[exeName]?.newMedErrEvdas + "\n    " + it[exeName]?.totMedErrEvdas)
                            it.put(exeName + 'newObsEvdas', isExcelExport ? "" + it[exeName]?.newObsEvdas : "    " + it[exeName]?.newObsEvdas + "\n    " + it[exeName]?.totObsEvdas)
                            it.put(exeName + 'newFatalEvdas', isExcelExport ? "" + it[exeName]?.newFatalEvdas : "    " + it[exeName]?.newFatalEvdas + "\n    " + it[exeName]?.totalFatalEvdas)
                            it.put(exeName + 'newRcEvdas', isExcelExport ? "" + it[exeName]?.newRcEvdas : "    " + it[exeName]?.newRcEvdas + "\n    " + it[exeName]?.totRcEvdas)
                            it.put(exeName + 'newLitEvdas', isExcelExport ? "" + it[exeName]?.newLitEvdas : "    " + it[exeName]?.newLitEvdas + "\n    " + it[exeName]?.totalLitEvdas)
                            it.put(exeName + 'newPaedEvdas', isExcelExport ? "" + it[exeName]?.newPaedEvdas : "    " + it[exeName]?.newPaedEvdas + "\n    " + it[exeName]?.totPaedEvdas)
                            it.put(exeName + 'newGeriaEvdas', isExcelExport ? "" + it[exeName]?.newGeriaEvdas : "    " + it[exeName]?.newGeriaEvdas + "\n    " + it[exeName]?.totGeriaEvdas)
                            it.put(exeName + 'newSpontEvdas', isExcelExport ? "" + it[exeName]?.newSpontEvdas : "    " + it[exeName]?.newSpontEvdas + "\n    " + it[exeName]?.totSpontEvdas)
                            it.put(exeName + 'newEvEvdas', isExcelExport ? "" + it[exeName]?.newEvEvdas : "    " + it[exeName]?.newEvEvdas + "\n    " + it[exeName]?.totalEvEvdas)
                            it.put(exeName + 'ebgmFaers', "" + it[exeName]?.ebgmFaers)
                            it.put(exeName + 'ebgmVaers', "" + it[exeName]?.ebgmVaers)
                            it.put(exeName + 'prrValueVaers', "" + it[exeName]?.prrValueVaers)
                            it.put(exeName + 'rorValueVaers', "" + it[exeName]?.rorValueVaers)
                            it.put(exeName + 'chiSquareVaers', "" + it[exeName]?.chiSquareVaers)
                            it.put(exeName + 'chiSquare', "" + it[exeName]?.chiSquare)
                            it.put(exeName + 'chiSquareFaers', "" + it[exeName]?.chiSquareFaers)
                            it.put(exeName + 'ebgmVigibase', "" + it[exeName]?.ebgmVigibase)
                            it.put(exeName + 'prrValueVigibase', "" + it[exeName]?.prrValueVigibase)
                            it.put(exeName + 'rorValueVigibase', "" + it[exeName]?.rorValueVigibase)
                            it.put(exeName + 'chiSquareVigibase', "" + it[exeName]?.chiSquareVigibase)
                            it.put(exeName + 'prrValueFaers', "" + it[exeName]?.prrValueFaers)
                            it.put(exeName + 'rorValueFaers', "" + it[exeName]?.rorValueFaers)

                            if (it[exeName]) {
                                Map m2 = new HashMap()
                                for (int i = 0; i < newFields.size(); i++) {
                                    String newCount = '', cumCount = ''

                                    if (it[newFields.get(i)]) {
                                        if (newFields.get(i) in ['hlt', 'hlgt', 'smqNarrow']) {
                                            newCount = it[exeName][newFields.get(i)]
                                            if (!newCount) {
                                                newCount = Constants.Commons.DASH_STRING
                                            }
                                        } else {
                                            if (it[exeName][newFields.get(i)] && !it[exeName][newFields.get(i)].equals(Constants.Commons.DASH_STRING)) {
                                                newCount = it[exeName][newFields.get(i)]
                                                cumCount = it[exeName][newFields.get(i).replace("new", "cum")]
                                            }
                                        }
                                    } else {
                                        newCount = Constants.Commons.DASH_STRING
                                        cumCount = Constants.Commons.DASH_STRING
                                    }
                                    if (newFields.get(i) in ['hlt', 'hlgt', 'smqNarrow']) {
                                        m2.put(exeName + newFields.get(i), newCount)
                                    } else {
                                        m2.put(exeName + newFields.get(i), isExcelExport ? "" + newCount : "    " + newCount + "\n    " + cumCount)
                                        m2.put(exeName + newFields.get(i).replace("new", "cum"), cumCount)
                                    }
                                }
                                it << m2
                            }

                            if (isExcelExport) {
                                it.put(exeName + 'cummCount', "" + it[exeName]?.cummCount)
                                it.put(exeName + 'cummPediatricCount', "" + it[exeName]?.cummPediatricCount)
                                it.put(exeName + 'cummInteractingCount', "" + it[exeName]?.cummInteractingCount)
                                it.put(exeName + 'cumNonSerious', "" + it[exeName]?.cumNonSerious)
                                it.put(exeName + 'cumGeriatricCount', "" + it[exeName]?.cumGeriatricCount)
                                it.put(exeName + 'cumSponCount', "" + it[exeName]?.cumSponCount)
                                it.put(exeName + 'cumSeriousCount', "" + it[exeName]?.cumSeriousCount)
                                it.put(exeName + 'cumFatalCount', "" + it[exeName]?.cumFatalCount)
                                it.put(exeName + 'cumStudyCount', "" + it[exeName]?.cumStudyCount)
                                it.put(exeName + 'prrUCI', "" + it[exeName]?.prrUCI)
                                it.put(exeName + 'rorUCI', "" + it[exeName]?.rorUCI)
                                it.put(exeName + 'eb95', "" + it[exeName]?.eb95)
                                it.put(exeName + 'cummCountFaers', "" + it[exeName]?.cummCountFaers)
                                it.put(exeName + 'cumSeriousCountFaers', "" + it[exeName]?.cumSeriousCountFaers)
                                it.put(exeName + 'eb95Faers', "" + it[exeName]?.eb95Faers)
                                it.put(exeName + 'cumSponCountFaers', "" + it[exeName]?.cumSponCountFaers)
                                it.put(exeName + 'cumStudyCountFaers', "" + it[exeName]?.cumStudyCountFaers)
                                it.put(exeName + 'prrUCIFaers', "" + it[exeName]?.prrUCIFaers)
                                it.put(exeName + 'cummPediatricCountFaers', "" + it[exeName]?.cummPediatricCountFaers)
                                it.put(exeName + 'cummInteractingCountFaers', "" + it[exeName]?.cummInteractingCountFaers)
                                it.put(exeName + 'cumFatalCountFaers', "" + it[exeName]?.cumFatalCountFaers)
                                it.put(exeName + 'cumGeriatricCountFaers', "" + it[exeName]?.cumGeriatricCountFaers)
                                it.put(exeName + 'cumNonSeriousFaers', "" + it[exeName]?.cumNonSeriousFaers)
                                it.put(exeName + 'rorUCIFaers', "" + it[exeName]?.rorUCIFaers)
                                it.put(exeName + 'cummCountVaers', "" + it[exeName]?.cummCountVaers)
                                it.put(exeName + 'cumSeriousCountVaers', "" + it[exeName]?.cumSeriousCountVaers)
                                it.put(exeName + 'eb95Vaers', "" + it[exeName]?.eb95Vaers)
                                it.put(exeName + 'prrUCIVaers', "" + it[exeName]?.prrUCIVaers)
                                it.put(exeName + 'cummPediatricCountVaers', "" + it[exeName]?.cummPediatricCountVaers)
                                it.put(exeName + 'cumFatalCountVaers', "" + it[exeName]?.cumFatalCountVaers)
                                it.put(exeName + 'cumGeriatricCountVaers', "" + it[exeName]?.cumGeriatricCountVaers)
                                it.put(exeName + 'rorUCIVaers', "" + it[exeName]?.rorUCIVaers)
                                it.put(exeName + 'cummCountVigibase', "" + it[exeName]?.cummCountVigibase)
                                it.put(exeName + 'cumSeriousCountVigibase', "" + it[exeName]?.cumSeriousCountVigibase)
                                it.put(exeName + 'eb95Vigibase', "" + it[exeName]?.eb95Vigibase)
                                it.put(exeName + 'prrUCIVigibase', "" + it[exeName]?.prrUCIVigibase)
                                it.put(exeName + 'cummPediatricCountVigibase', "" + it[exeName]?.cummPediatricCountVigibase)
                                it.put(exeName + 'cumFatalCountVigibase', "" + it[exeName]?.cumFatalCountVigibase)
                                it.put(exeName + 'cumGeriatricCountVigibase', "" + it[exeName]?.cumGeriatricCountVigibase)
                                it.put(exeName + 'rorUCIVigibase', "" + it[exeName]?.rorUCIVigibase)
                                it.put(exeName + 'totEeaEvdas', "" + it[exeName]?.totEeaEvdas)
                                it.put(exeName + 'totHcpEvdas', "" + it[exeName]?.totHcpEvdas)
                                it.put(exeName + 'totalSeriousEvdas', "" + it[exeName]?.totalSeriousEvdas)
                                it.put(exeName + 'totMedErrEvdas', "" + it[exeName]?.totMedErrEvdas)
                                it.put(exeName + 'totObsEvdas', "" + it[exeName]?.totObsEvdas)
                                it.put(exeName + 'totalFatalEvdas', "" + it[exeName]?.totalFatalEvdas)
                                it.put(exeName + 'totRcEvdas', "" + it[exeName]?.totRcEvdas)
                                it.put(exeName + 'totalLitEvdas', "" + it[exeName]?.totalLitEvdas)
                                it.put(exeName + 'totPaedEvdas', "" + it[exeName]?.totPaedEvdas)
                                it.put(exeName + 'totGeriaEvdas', "" + it[exeName]?.totGeriaEvdas)
                                it.put(exeName + 'totSpontEvdas', "" + it[exeName]?.totSpontEvdas)
                                it.put(exeName + 'totalEvEvdas', "" + it[exeName]?.totalEvEvdas)

                                if (it[exeName]) {
                                    Map m1 = new HashMap()
                                    for (int i = 0; i < newFields.size(); i++) {
                                        String newCountString = newFields.get(i).toString()
                                        String cumCountString = newCountString.replace("new", "cum")
                                        if (it[exeName][cumCountString]) {
                                            m1.put(exeName + cumCountString, it[exeName][cumCountString])
                                        } else {
                                            m1.put(exeName + cumCountString, Constants.Commons.DASH_STRING)
                                        }
                                    }
                                    it << m1
                                }
                            }
                            if (statisticsEbgmEnabled || statisticsFaersEbgmEnabled) {
                                if (selectedDatasourceContainsFaers) {
                                    it << fetchEachSubCategory(subGroupMap, Holders.config.subgrouping.faers.ageGroup.name, exeName, it, "Age")
                                    it << fetchEachSubCategory(subGroupMap, Holders.config.subgrouping.faers.gender.name, exeName, it, "Gender")
                                    if (executedConfiguration?.selectedDatasource?.contains("pva")) {
                                        it << fetchEachSubCategory(subGroupMap, Holders.config.subgrouping.ageGroup.name, '', it, "Age")
                                        it << fetchEachSubCategory(subGroupMap, Holders.config.subgrouping.gender.name, '', it, "Gender")
                                        ebgmSubGroupingFields?.each { category ->
                                            it << fetchAllSubCategoryColumn(subGroupColumnsCamelCaseMap, category, exeName, '', it)
                                        }
                                    }
                                } else {
                                    it << fetchEachSubCategory(subGroupMap, Holders.config.subgrouping.ageGroup.name, exeName, it, "Age")
                                    it << fetchEachSubCategory(subGroupMap, Holders.config.subgrouping.gender.name, exeName, it, "Gender")
                                    ebgmSubGroupingFields?.each { category ->
                                        it << fetchAllSubCategoryColumn(subGroupColumnsCamelCaseMap, category, exeName, '', it)
                                    }
                                }
                            }
                            if (statisticsRrrOrRorEnabled) {
                                subGroupingFields?.each { category ->
                                    it << fetchAllSubCategoryColumn(subGroupColumnsCamelCaseMap, category, exeName, '', it)
                                }
                                relativeSubGroupingFields?.each { category ->
                                    it << fetchAllSubCategoryColumn(subGroupColumnsCamelCaseMap, category, exeName, Constants.SubGroups.REL, it)
                                }
                            }
                        }
                    }
                } as Runnable)
            }
        }
        futureList?.each {
            it.get()
        }
        executorService.shutdown()
        Map reportParamsMap
        String datasheets = ""
        String enabledSheet = Constants.DatasheetOptions.CORE_SHEET
        if (!TextUtils.isEmpty(executedConfiguration?.selectedDataSheet)) {
            datasheets = dataSheetService.formatDatasheetMap(executedConfiguration)?.text?.join(',')
        } else if (executedConfiguration?.selectedDatasource?.contains(Constants.DataSource.PVA)) {
            if (!TextUtils.isEmpty(executedConfiguration.productGroupSelection)
                    || !TextUtils.isEmpty(executedConfiguration.productSelection)) {
                Boolean isProductGroup = !TextUtils.isEmpty(executedConfiguration.productGroupSelection)
                String products = executedConfiguration.productGroupSelection ?: executedConfiguration.productSelection
                datasheets = dataSheetService.fetchDataSheets(products, enabledSheet, isProductGroup,
                        executedConfiguration.isMultiIngredient)?.dispName?.join(',')
            }
        }
        if (isExcelExport) {
            String reportDateRange = DateUtil.toDateString1(executedConfiguration?.executedAlertDateRangeInformation?.dateRangeStartAbsolute)
            + " - " + DateUtil.toDateString1(executedConfiguration?.executedAlertDateRangeInformation?.dateRangeEndAbsolute)
            criteriaData = [alertName                     : executedConfiguration?.name,
                            dataSource                    : executedConfiguration?.getDataSource(executedConfiguration?.selectedDatasource),
                            productName                   : executedConfiguration?.productSelection ?
                                    ViewHelper.getDictionaryValues(executedConfiguration, DictionaryTypeEnum.PRODUCT)
                                    : ViewHelper.getDictionaryValues(executedConfiguration, DictionaryTypeEnum.PRODUCT_GROUP),
                            selectedDatasheets            : datasheets?:"",
                            dateRange                     : reportDateRange,
                            cumulative                    : false,
                            advncedFilter                 : params.advancedFilterId ? advancedFilterService
                                    .getAvdFilterCriteriaExcelExport(params.advancedFilterId as Long) : '-',
                            excludeFollowUp               : executedConfiguration?.excludeFollowUp ? "Yes" : "No",
                            missedcase                    : executedConfiguration?.missedCases ? "Yes" : "No",
                            groupBySmq                    : executedConfiguration?.groupBySmq ? "Yes" : "No",
                            includeMedicallyConfirmedCases: executedConfiguration?.includeMedicallyConfirmedCases ? "Yes" : "No",
                            limitToPrimaryPath            : executedConfiguration?.limitPrimaryPath ? "Yes" : "No",
                            rmpReference                  : executedConfiguration?.alertRmpRemsRef ?: "-",
                            scheduledBy                   : executedConfiguration?.owner?.fullName ?: "-",
                            evdasDateRange                : executedConfiguration?.evdasDateRange ?: "-",
                            faersDateRange                : executedConfiguration?.faersDateRange ?: "-",
                            onAndAfterDate                : executedConfiguration?.onOrAfterDate
                                    ? DateUtil.toDateString1(executedConfiguration?.onOrAfterDate) : "-"
            ]
            reportParamsMap = ["showCompanyLogo": true, "showLogo": true, "header": "Quantitative Review Report"]
            params << reportParamsMap
            params.criteriaData = criteriaData
        }
        def uniqueDispositions = currentDispositionQueue.toSet()
        String quickFilterDisposition = uniqueDispositions?.join(", ")
        params.quickFilterDisposition = quickFilterDisposition
        List criteriaSheetList
        if (!cumulative && !adhocRun?.toBoolean() && params.callingScreen != Constants.Commons.DASHBOARD
                && params.callingScreen != Constants.Commons.TAGS) {
            params.totalCount = agaList?.size()?.toString()
            if (executedConfiguration.selectedDatasource == Constants.DataSource.JADER) {
                criteriaSheetList = aggregateCaseAlertService.getJaderAggregateCaseAlertCriteriaData(executedConfiguration,
                        params,Constants.AlertConfigType.AGGREGATE_CASE_ALERT_JADER)
            } else {
                criteriaSheetList = aggregateCaseAlertService.getAggregateCaseAlertCriteriaData(executedConfiguration,
                        params, Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
            }
            params.criteriaSheetList = criteriaSheetList
        }
        Boolean isLongComment = agaList?.any({x -> x.comment?.size() > 100})
        params.isLongComment = isLongComment?: false
        def reportFile = dynamicReportService.createAggregateAlertsReport(new JRMapCollectionDataSource(agaList), params)
        log.info("total time taken in output report generation is ${System.currentTimeSeconds()-startTime}")
        renderReportOutputType(reportFile,params)
        signalAuditLogService.createAuditForExport(criteriaSheetList, isDashboard == true
                ? Constants.AuditLog.AGGREGATE_REVIEW_DASHBOARD
                : executedConfiguration.getInstanceIdentifierForAuditLog() + ": Alert Details",
                isDashboard == true ? Constants.AuditLog.AGGREGATE_REVIEW_DASHBOARD
                        : (Constants.AuditLog.AGGREGATE_REVIEW + (executedConfiguration.isLatest ? "" : ": Archived Alert")),
                params, reportFile.name)
    }

    private boolean isExcelExportFormat(String outputFormat) {
        return outputFormat == ReportFormat.XLSX.name() ? true : false
    }

    private static Map getCustomFields(def b, def prefix, def isSmq, def labelCondition,
                                       def isRor, def c1, def c2, boolean isDashboard) {

        def display = b.display
        def name = prefix ? prefix + b.name : b.name
        def data = prefix ? prefix + "." + b.name : b.name


        if (b.display.contains("PT#ORSMQ")) {
            def labeles = b.display.split("\\s*#OR\\s*")
            def label = isSmq ? labeles[1] : labeles[0]
            display = prefix ? "Prev period " + prefix[-1] + " " + label : label
        } else if (b.display.contains("#ORDisposition")) {
            def labeles = b.display.split("\\s*#OR\\s*")
            def label = labelCondition ? labeles[0] : labeles[1]
            display = prefix ? "Prev period " + prefix[-1] + " " + label : label
        } else if (b.display.contains("#ORiROR")) {
            def labeles = b.display.split("\\s*#OR\\s*")
            def label = isRor ? labeles[0] : labeles[1]
            display = prefix ? "Prev period " + prefix[-1] + " " + label : label
        } else if (b.display.contains("#ORiROR05/iROR95")) {
            def labeles = b.display.split("\\s*#OR\\s*")
            def label = isRor ? labeles[0] : labeles[1]
            display = prefix ? "Prev period " + prefix[-1] + " " + label : label
        } else if (b.display.contains("#OR(Prior Review/DSS)")) {
            def label
            if (c1 && c2) {
                label = "Rationale #OR (Prior Review/DSS)"
            } else if (c1) {
                label = "Rational"
            } else {
                label = "DSS"
            }
            display = prefix ? "Prev period " + prefix[-1] + " " + label : label
        }

        Map result = [:]

        if (b.display.contains("/") && b.display != "IME/DME (E)" && !b.display.contains("PT#ORSMQ")
            && !b.display.contains("#OR")) {
            def parts = display.split("\\s*/\\s*")
            def firstItem = parts[0]
            def secondItem = parts[1]
            def firstData = prefix ? "${prefix}.${b.name}" : "${b.name}"
            def secondName = getSecondNameField(b.name)
            def secondData = prefix ? prefix + "." + secondName : secondName


            def composedName = name + "/" + (prefix ? prefix + secondName : secondName)

            result = [
                    "name"            : composedName,
                    "data"            : null,
                    "title"           : null,
                    "visible"         : true,
                    "showDefault"     : true,
                    "customComponent" : "StackedCellComponent",
                    "headerItems"     : [firstItem, secondItem],
                    "dataProperties"  : [firstData, secondData],
                    "hyperLinkEnabled": b.isHyperLink,
                    "className"       : "text-center",
                    "filterType"      : ["type": b.type]
            ]
        } else {
            switch (b.name) {
                case "priority":
                    result = ["name"           : name,
                              "data"           : data,
                              "title"          : display,
                              "visible"        : true,
                              "showDefault"    : true,
                              "className"      : "col-min-50 text-center priority",
                              "customComponent": "PriorityComponent"]
                    break
                case "actions":
                    result = ["name"           : name,
                              "data"           : null,
                              "title"          : display,
                              "visible"        : true,
                              "showDefault"    : true,
                              "className"      : "col-min-50 text-center actions",
                              "customComponent": "ActionCountDropdownComponent"]
                    break
                case "productName":
                    result = ["name"       : name,
                              "data"       : data,
                              "title"      : display,
                              "visible"    : true,
                              "showDefault": true,
                              "filterType" : [
                                      "type"   : "select",
                                      "options": []
                              ],
                              "className"  : "col-min-100 col-max-150"]
                    break
                case "alertTags":
                    result = ["name"           : name,
                              "data"           : data,
                              "title"          : display,
                              "visible"        : true,
                              "showDefault"    : true,
                              "customComponent": "AlertTagsComponent",
                              "className"      : "col-max-300 col-min-300"]
                    break
                case ["listed", "chiSquare", "positiveRechallenge", "positiveDechallenge", "dmeImeEvdas", "sdrEvdas", "cValue", "dValue", "bValue", "eValue", "aValue", "ebgm", "prrValue", "rorValue", "rorValueEvdas"]:
                    result = ["name"       : name,
                              "data"       : data,
                              "title"      : display,
                              "visible"    : true,
                              "showDefault": true,
                              "className"  : "col-max-90"]
                    break
                case "signalsAndTopics":
                    result = ["name"          : name,
                              "data"          : data,
                              "title"         : display,
                              "visible"       : true,
                              "showDefault"   : true,
                              "className"     : "col-min-150 col-max-200",
                              "orderable"     : true,
                              "renderType"    : "html",
                              "renderFunction": "customSignalRenderer"]
                    break
                case "disposition":
                    result = ["name"       : name,
                              "data"       : data,
                              "title"      : display,
                              "visible"    : true,
                              "showDefault": true]
                    break
                case "currentDisposition":
                    result = ["name"           : name,
                              "data"           : null,
                              "title"          : display,
                              "visible"        : true,
                              "showDefault"    : true,
                              "customComponent": "DispositionComponent",
                              "className"      : "col-max-300 col-min-300"]
                    break
                case "assignedTo":
                    result = ["name"           : name,
                              "data"           : data,
                              "title"          : display,
                              "visible"        : true,
                              "showDefault"    : true,
                              "customComponent": "AssignedToComponent",
                              "className"      : "col-min-180"]
                    break
                case ["dueIn", "comment"]:
                    result = ["name"       : name,
                              "data"       : data,
                              "title"      : display,
                              "visible"    : true,
                              "showDefault": true,
                              "className"  : "text-center"]
                    break
                default:
                    result = ["name"       : name,
                              "data"       : data,
                              "title"      : display,
                              "visible"    : true,
                              "showDefault": true]
                    if (name == "soc") {
                        result.renderFunction = 'socRenderer'
                    }
                    break
            }
        }
        if (b.isFilter && b.name != "productName") {
            result.put("filterType", ["type": b.type])
        }
        if (b.name == "dispPerformedBy" || b.name == "justification" || b.name == "comment" || b.name == "productName"
                || b.name == "alertTags" || (b.name == "alertName" && isDashboard)) {
            result.put("isElipsis", true)
        }

        return result
    }

    private renderReportOutputType(File reportFile, def params) {
        String reportName = "Aggregate Case Alert" + DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss").print(new DateTime())
        response.contentType = "${dynamicReportService.getContentType(params.outputFormat)}; charset=UTF-8"
        response.contentLength = reportFile.size()
        response.setCharacterEncoding("UTF-8")
        response.setHeader("Content-disposition", "Attachment; filename=\""
                + "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.$params.outputFormat" + "\"")
        response.getOutputStream().write(reportFile.bytes)
        response.outputStream.flush()
        params?.reportName = "${URLEncoder.encode(FileNameCleaner.cleanFileName(reportName), "UTF-8")}.$params.outputFormat"
    }

    private static void categorizeAggregateCaseAlert(def aga, String defaultDispositionValue,
                                              List validatedSignalList, List closedReviewList,
                                              List notStartedReviewSignalList, List pendingReviewList) {
        if (aga.disposition.isValidatedConfirmed()) {
            validatedSignalList << aga
        } else if (aga.disposition.isClosed()) {
            closedReviewList << aga
        } else if (aga.disposition.value == defaultDispositionValue) {
            notStartedReviewSignalList << aga
        } else {
            pendingReviewList << aga
        }
    }

    private static List<ExecutedConfiguration> getExecutedConfigurationList(Group workflowGroup) {
        return ExecutedConfiguration.createCriteria().list {
            eq('workflowGroup', workflowGroup)
            eq("adhocRun", false)
            eq('type', Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
        }
    }

    private List getAggregateCaseAlertList(domainName, executedConfigurationList, String propertyName) {
        return domainName.createCriteria().list {
            or {
                executedConfigurationList.collate(1000).each {
                    'in'(propertyName, it)
                }
            }
        }
    }

    private AlertDataDTO setupAlertDataDTO(def params, User user, ExecutedConfiguration ec,
                                           Boolean cumulative, Boolean isArchived) {
        AlertDataDTO alertDataDTO = new AlertDataDTO()
        alertDataDTO.params = params
        alertDataDTO.userId = user?.id
        alertDataDTO.executedConfiguration = ec
        alertDataDTO.execConfigId = ec?.id
        alertDataDTO.cumulative = cumulative
        alertDataDTO.timeZone = user?.preference?.timeZone
        alertDataDTO.isFromExport = true
        alertDataDTO.workflowGroupId = user?.workflowGroup?.id
        alertDataDTO.groupIdList = user?.groups?.collect { it.id }
        alertDataDTO.domainName = aggregateCaseAlertService.getDomainObject(isArchived)
        return alertDataDTO
    }

    private static List getFiltersFromParamsList(Boolean isFilterRequest, def params) {
        List filters = []
        def escapedFilters = null

        if (params.filters) {
            escapedFilters = params.filters
        }
        if(escapedFilters) {
            filters = new ArrayList(escapedFilters)
        }

        if (params.dashboardFilter && (params.dashboardFilter == 'total' || params.dashboardFilter == 'new') && !isFilterRequest) {
            filters = Disposition.list().collect { it.displayName }
        } else if (params.dashboardFilter && params.dashboardFilter == 'underReview' && !isFilterRequest) {
            filters = Disposition.findAllByClosedNotEqualAndValidatedConfirmedNotEqual(true, true).collect {
                it.displayName
            }
        } else if (!isFilterRequest) {
            filters = Disposition.findAllByClosedAndReviewCompleted(false , false).collect { it.displayName }
        }
        filters
    }

    String changeAlertLevelDisposition(){
        Map responseMap = [:]
        String jsonContent = request.JSON
        log.info("json content received is := " + jsonContent)
        try {
            Map dataMap = jsonContent ? jsonToMap(jsonContent) : null
            responseMap = aggregateCaseAlertRestService.changeAlertLevelDisposition(dataMap)
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex)
            String errorMsg = message(code: "app.label.disposition.change.error")
            RestApiResponse.serverErrorResponse(responseMap,errorMsg)
            render(status: HttpStatus.INTERNAL_SERVER_ERROR.value(),text:responseMap as JSON)
            return
        }
        render(responseMap as JSON)
    }

    def fetchPossibleValues(Long executedConfigId) {
        Map responseMap = [:]
        try {
            Map<String, List> possibleValuesMap = [:]
            possibleValuesMap.put("alertTags", AlertTag.list()?.collect { [id: it.name, text: it.name] })
            alertService.preparePossibleValuesMap(AggregateCaseAlert, possibleValuesMap, executedConfigId)
            String fullName = ""
            for (int i = 0; i < (possibleValuesMap?.soc)?.size(); i++) {
                if (possibleValuesMap.soc[i].id == Constants.Commons.SMQ_ADVANCEDFILTER_LABEL) {
                    fullName = Constants.Commons.SMQ_ADVANCEDFILTER_LABEL
                } else {
                    fullName = dataObjectService.getAbbreviationMap(possibleValuesMap.soc[i].id)
                    if (!fullName) {
                        fullName = possibleValuesMap.soc[i].text
                    }
                }
                possibleValuesMap.soc[i].text = fullName
            }
            possibleValuesMap.soc = possibleValuesMap?.soc?.sort { it?.text }
            List<String> yesNoFieldsList = ["listed", "positiveRechallenge", "positiveDechallenge", "related", "pregenency",
                                            "listedFaers", "positiveRechallengeFaers", "positiveDechallengeFaers", "relatedFaers", "pregenencyFaers",
                                            "sdrEvdas", "sdrPaedEvdas", "sdrGeratrEvdas", "currentRun"]
            List<Map<String, String>> yesNoMapList = [[id: "Yes", text: "Yes"], [id: "No", text: "No"]]
            possibleValuesMap.put(Constants.AggregateAlertFields.TREND_FLAG, [Constants.Commons.NEW_UPPERCASE, Constants.Commons.NO_UPPERCASE, Constants.Commons.YES_UPPERCASE])
            yesNoFieldsList.each {
                possibleValuesMap.put(it, yesNoMapList)
            }
            List<Map> codeValues = pvsGlobalTagService.fetchTagsAndSubtags()
            codeValues.each {
                it.id = it.text
            }
            List<Map> tags = pvsGlobalTagService.fetchTagsfromMart(codeValues)
            List<Map> subTags = pvsGlobalTagService.fetchSubTagsFromMart(codeValues)
            possibleValuesMap.put("tags", tags.unique { it.text.toUpperCase() })
            possibleValuesMap.put("subTags", subTags.unique { it.text.toUpperCase() })
            RestApiResponse.successResponseWithData(responseMap, null , possibleValuesMap)
        } catch(Exception ex){
            // Catch any other exceptions and respond with a server error
            log.error("Unexpected error occurred: ${ex.message}")
            RestApiResponse.serverErrorResponse(responseMap, ex.message)
        }
        render(responseMap as JSON)
    }

    def fetchAllFieldValues(Long executedConfigId, String callingScreen) {
        List<Map> fieldList = []
        try {
            ExecutedConfiguration executedConfiguration
            if (executedConfigId) {
                executedConfiguration = ExecutedConfiguration.get(executedConfigId)
            }
            if (executedConfiguration?.selectedDatasource == Constants.DataSource.JADER) {
                fieldList = aggregateCaseAlertService.fieldListAdvanceFilterJader(executedConfiguration?.groupBySmq as boolean)
            } else {
                fieldList = aggregateCaseAlertService.fieldListAdvanceFilter(executedConfiguration?.selectedDatasource, executedConfiguration?.groupBySmq as boolean, callingScreen, 'AGGREGATE_CASE_ALERT')
            }
            fieldList = fieldList.collect { field ->
                field + [dataType: simplifyDataType(field.dataType)]
            }
        } catch(Exception ex){
            // Catch any other exceptions and respond with a server error
            log.error("Unexpected error occurred: ${ex.message}")
        }
        render fieldList as JSON
    }

    def showCharts(Long alertId, boolean isArchived) {
        Map responseMap = [:]
        try {
            def domain = isArchived ? ArchivedAggregateCaseAlert : AggregateCaseAlert
            def aga = domain.findById(alertId)
            if (aga) {
                Map chartsData = aggregateCaseAlertService.showCharts(aga)
                RestApiResponse.successResponseWithData(responseMap, null, chartsData)
            } else {
                RestApiResponse.recordAbsentResponse(responseMap)
            }
        } catch (Exception ex) {
            log.error("Error fetch charts", ex)
            RestApiResponse.serverErrorResponse(responseMap)
        }
        render(responseMap as JSON)
    }

    def fetchAcceptedFileFormats() {
        List formatsSupported = Holders.config.signal.file.accepted.formats
        List formatsNotSupported = Holders.config.signal.file.not.accepted.formats
        formatsSupported = formatsSupported.minus(formatsNotSupported)
        render formatsSupported as JSON
    }

    def fetchAttachment(final Long alertId, boolean isArchived) {
        Map responseMap = [:]
        List<Map> data = []
        try {
            List<Long> aggAlertList = aggregateCaseAlertService.getAlertIdsForAttachments(alertId, isArchived)
            String timezone = userService.getUser().preference.timeZone
            if (aggAlertList) {
                aggAlertList.each { Long aggAlertId ->
                    def aggAlert = ArchivedAggregateCaseAlert.get(aggAlertId) ?: AggregateCaseAlert.get(aggAlertId)
                    data += aggAlert.attachments.collect {
                        [
                                id         : it.id,
                                name       :  it.inputName ?: it.name,
                                description: AttachmentDescription.findByAttachment(it)?.description,
                                timeStamp  : DateUtil.stringFromDate(it.dateCreated, DateUtil.DEFAULT_DATE_TIME_FORMAT, timezone),
                                modifiedBy : AttachmentDescription.findByAttachment(it)?.createdBy
                        ]
                    }
                }
                RestApiResponse.successResponseWithData(responseMap, null, data)
            } else {
                RestApiResponse.recordAbsentResponse(responseMap)
            }
        } catch (Exception ex) {
            log.error("Error fetch attachments", ex)
            RestApiResponse.serverErrorResponse(responseMap)
        }
        render(responseMap as JSON)
    }

    def upload(Long alertId, boolean isArchived) {
        Map responseMap = [:]
        try {
            def domain = isArchived ? ArchivedAggregateCaseAlert : AggregateCaseAlert
            def aggCaseAlert = domain.findById(alertId.toInteger())
            def fileName = params?.attachments?.filename
            if (fileName instanceof String) {
                fileName = [fileName]
            }
            params?.isAlertDomain = true   //this is added to check whether attachment is added from alert details screen PVS-49054
            User currentUser = User.findByUsername('signaldev')
            Map filesStatusMap = attachmentableService.attachUploadFileTo(currentUser, fileName, aggCaseAlert, request)
            String fileDescription = params.description
            List<Attachment> attachments = aggCaseAlert.getAttachments().sort { it.dateCreated }
            if (attachments) {
                List<Integer> bulkAttachmentIndex = 1..filesStatusMap?.uploadedFiles?.size()
                bulkAttachmentIndex.each {
                    Attachment attachment = attachments[-it]
                    AttachmentDescription attachmentDescription = new AttachmentDescription()
                    attachmentDescription.attachment = attachment
                    attachmentDescription.createdBy = currentUser.fullName
                    attachmentDescription.description = fileDescription
                    attachmentDescription.skipAudit = true
                    attachmentDescription.save(flush: true)
                }
            }
            if (filesStatusMap?.uploadedFiles) {
                def filenames = filesStatusMap?.uploadedFiles*.originalFilename.join(', ')
                activityService.createActivity(aggCaseAlert.executedAlertConfiguration, ActivityType.findByValue(ActivityTypeValue.AttachmentAdded),
                        currentUser, "Attachment ${filenames} is added with description '${fileDescription}'", null,
                        [product: getNameFieldFromJson(aggCaseAlert.alertConfiguration.productSelection), event: getNameFieldFromJson(aggCaseAlert.alertConfiguration.eventSelection)],
                        aggCaseAlert.productName, aggCaseAlert.pt, aggCaseAlert.assignedTo, null, aggCaseAlert.assignedToGroup)
            }
            RestApiResponse.successResponse(responseMap)
        } catch (Exception ex) {
            log.error("Error occurred while uploading attachment ", ex)
            RestApiResponse.serverErrorResponse(responseMap)
        }

        render(responseMap as JSON)
    }

    def downloadAttachment(Long id) {
        Attachment attachment = Attachment.get(id)

        if (attachment) {
            File file = AttachmentableUtil.getFile(grailsApplication.config, attachment)

            if (file.exists()) {
                String filename
                if(params.type == "assessment" || params.type == "rmm"){
                    filename = attachment.filename
                } else {
                    String extension = FilenameUtils.getExtension(attachment.filename)
                    if(attachment.inputName && !attachment.inputName.contains(extension))
                        filename = attachment.inputName + "." + extension ?: attachment.filename
                    else
                        filename = attachment.inputName ?: attachment.filename
                }

                ['Content-disposition': "${params.containsKey('inline') ? 'inline' : 'attachment'};filename=\"$filename\"",
                 'Cache-Control': 'private',
                 'Pragma': ''].each {k, v ->
                    response.setHeader(k, v)
                }

                if (params.containsKey('withContentType')) {
                    response.contentType = attachment.contentType
                } else {
                    response.contentType = 'application/octet-stream'
                }
                file.withInputStream{fis->
                    response.outputStream << fis
                }
                signalAuditLogService.createAuditForExport(null, attachment.getInstanceIdentifierForAuditLog(),attachment.getModuleNameForMultiUseDomains(), [:], filename)
                // response.contentLength = file.length()
                // response.outputStream << file.readBytes()
                // response.outputStream.flush()
                // response.outputStream.close()
                return
            }
        }

        response.status = HttpServletResponse.SC_NOT_FOUND
    }

    def deleteAttachment() {
        Map responseMap = [:]
        String jsonContent = request.JSON
        log.info("json content received is := " + jsonContent)
        try {
            Map dataMap = jsonContent ? jsonToMap(jsonContent) : null
            def domain = aggregateCaseAlertService.getDomainObject(dataMap.isArchived)
            Attachment attachment = Attachment.findById(dataMap.attachmentId)
            String fileName = attachment.getFilename()
            def aggCaseAlert = domain.get(dataMap.alertId)
            if (attachment) {
                if (AttachmentDescription.findByAttachment(attachment)) {
                    AttachmentDescription.findByAttachment(attachment).delete()
                }
                attachmentableService.removeAttachment(dataMap.attachmentId)
                activityService.createActivity(aggCaseAlert.executedAlertConfiguration, ActivityType.findByValue(ActivityTypeValue.AttachmentRemoved),
                        User.findByUsername('signaldev'), "Attachment " + fileName + " is removed", null,
                        [product: getNameFieldFromJson(aggCaseAlert.alertConfiguration.productSelection), event: getNameFieldFromJson(aggCaseAlert.alertConfiguration.eventSelection)],
                        aggCaseAlert.productName, aggCaseAlert.pt, aggCaseAlert.assignedTo, null, aggCaseAlert.assignedToGroup)
                RestApiResponse.successResponse(responseMap)
            } else {
                RestApiResponse.recordAbsentResponse(responseMap)
            }
        } catch (Exception ex) {
            log.error("Error deleting attachment", ex)
            RestApiResponse.serverErrorResponse(responseMap)
        }
        render(responseMap as JSON)
    }

    def revertDisposition() {
        Map responseMap = [:]
        String jsonContent = request.JSON
        log.info("json content received is := " + jsonContent)
        try {
            Map requestData = jsonContent ? jsonToMap(jsonContent) : null
            Map data = aggregateCaseAlertService.revertDisposition(requestData.id, requestData.justification)
            if (data) {
                Long configId = AggregateCaseAlert.get(requestData.id)?.executedAlertConfiguration?.id
                Long countOfPreviousDisposition
                if (requestData.callingScreen != Constants.Commons.DASHBOARD) {
                    countOfPreviousDisposition = alertService.fetchPreviousDispositionCount(configId, data.oldDispName, AggregateCaseAlert)
                } else {
                    countOfPreviousDisposition = alertService.fetchPreviousDispositionCountDashboard(data.oldDispName, AggregateCaseAlert)
                }
                data << [incomingDisposition: data.oldDispName, targetDisposition: data.newDispName, countOfPreviousDisposition: countOfPreviousDisposition]
                aggregateCaseAlertService.persistAlertDueDate(data.alertDueDateList)
                if (data.dispositionReverted) {
                    RestApiResponse.successResponseWithData(responseMap, null, data)
                } else {
                    RestApiResponse.failureResponse(responseMap, message(code: "app.label.undo.disposition.change.error.refresh"))
                }
            }
        } catch (Exception e) {
            log.error("Error occurred while reverting disposition : ", e)
            e.printStackTrace()
            RestApiResponse.serverErrorResponse(responseMap, message(code: "app.label.undo.disposition.change.error"))
        }
        render(responseMap as JSON)
    }

    def archivedAlert() {
        Map responseMap = [:]
        String jsonContent = request.JSON
        log.info("json content received is := " + jsonContent)
        Map alertList = [:]
        try {
            Map requestData = jsonContent ? jsonToMap(jsonContent) : null
            if (requestData.id) {
                alertList = archivedAlertList(requestData.id as Long, requestData, true)
                RestApiResponse.successResponseWithData(responseMap, null, alertList)
            } else {
                RestApiResponse.recordAbsentResponse(responseMap)
            }
        } catch (Exception ex) {
            log.error("Error occurred while fetching archived alert list", ex)
            RestApiResponse.serverErrorResponse(responseMap)
        }

        render(responseMap as JSON)
    }

    String generateDataAnalysis() {
        Map responseMap = [:]
        String jsonContent = request.JSON
        log.info("json content received is := " + jsonContent)
        Map dataMap = jsonContent ? jsonToMap(jsonContent) : null
        if (dataMap.executedConfigId && dataMap.spotfireDateRange && dataMap.analysisPeriod) {
            Boolean status = spotfireService.generateDataAnalysis(dataMap)
            if (status) {
                RestApiResponse.successResponse(responseMap)
            } else {
                RestApiResponse.failureResponse(responseMap)
                render(status: HttpStatus.INTERNAL_SERVER_ERROR.value(), text: responseMap as JSON)
                return
            }
        } else {
            RestApiResponse.invalidParametersResponse(responseMap)
            render(status: HttpStatus.BAD_REQUEST.value(), text: responseMap as JSON)
            return
        }
        render(responseMap as JSON)
    }

    def viewDataAnalysis() {
        String jsonContent = request.JSON
        log.info("json content received is := " + jsonContent)
        Map dataMap = jsonContent ? jsonToMap(jsonContent) : null
        String fileName = dataMap.fileName
        redirect(controller: "dataAnalysis", action: "view",fileName: fileName, params:dataMap)
    }

    private String simplifyDataType(String dataType) {
        switch (dataType) {
            case ~/^java\.lang\.String$/:
                return 'string'
            case ~/^java\.lang\.(Integer|Long|Float|Double|BigDecimal|Number)$/:
                return 'number'
            case ~/^java\.lang\.Boolean$/:
                return 'boolean'
            case ~/^java\.util\.Date$/:
                return 'date'
            default:
                return dataType.toLowerCase()
        }
    }

    private static String getSecondNameField(String firstName) {
        Map pairs = [
             "newCount" : "cummCount",
             "newCountFaers" : "cummCountFaers",
             "newEvEvdas" : "totalEvEvdas",
             "newSeriousCount" : "cumSeriousCount",
             "newSeriousCountFaers" : "cumSeriousCountFaers",
             "newFatalCount" : "cumFatalCount",
             "eb05" : "eb95",
             "eb05Faers" : "eb95Faers",
             "eb05Vaers" : "eb95Vaers",
             "eb05Vigibase" : "eb95Vigibase",
             "freqPeriod" : "cumFreqPeriod",
             "newRcEvdas" : "totRcEvdas",
             "newCountVaers" : "cummCountVaers",
             "newCountVigibase" : "cummCountVigibase",
             "newEeaEvdas" : "totEeaEvdas",
             "newFatalCountFaers" : "cumFatalCountFaers",
             "newFatalCountVigibase" : "cumFatalCountVigibase",
             "newGeriaEvdas" : "totGeriaEvdas",
             "newGeriatricCountFaers" : "cumGeriatricCountFaers",
             "newGeriatricCountVaers" : "cumGeriatricCountVaers",
             "newGeriatricCountVigibase" : "cumGeriatricCountVigibase",
             "newGeriatricCount" : "cumGeriatricCount",
             "newHcpEvdas" : "totHcpEvdas",
             "newInteractingCountFaers" : "cummInteractingCountFaers",
             "newInteractingCount" : "cummInteractingCount",
             "newLitEvdas" : "totalLitEvdas",
             "newNonSeriousFaers" : "cumNonSeriousFaers",
             "newNonSerious" : "cumNonSerious",
             "newObsEvdas" : "totObsEvdas",
             "newPaedEvdas" : "totPaedEvdas",
             "newPediatricCountFaers" : "cummPediatricCountFaers",
             "newPediatricCountVaers" : "cummPediatricCountVaers",
             "newPediatricCountVigibase" : "cummPediatricCountVigibase",
             "newPediatricCount" : "cummPediatricCount",
             "newProdCount" : "cumProdCount",
             "newSeriousEvdas" : "totalSeriousEvdas",
             "newSeriousCountVaers" : "cumSeriousCountVaers",
             "newSeriousCountVigibase" : "cumSeriousCountVigibase",
             "newSponCountFaers" : "cumSponCountFaers",
             "newSponCount" : "cumSponCount",
             "newSpontEvdas" : "totSpontEvdas",
             "newStudyCountFaers" : "cumStudyCountFaers",
             "newStudyCount" : "cumStudyCount",
             "prrLCIVaers" : "prrUCIVaers",
             "prrLCIVigibase" : "prrUCIVigibase",
             "prrLCI" : "prrUCI",
             "prrLCIFaers" : "prrUCIFaers",
             "reviewedFreqPeriod" : "reviewedCumFreqPeriod",
             "rorLCIVaers" : "rorUCIVaers",
             "rorLCIVigibase" : "rorUCIVigibase",
             "rorLCI" : "rorUCI",
             "rorLciSubGroup" : "rorUciSubGroup",
             "rorLciRelSubGroup" : "rorUciRelSubGroup",
             "rorLCIJader" : "rorUCIJader",
             "newCountJader" : "cumCountJader",
             "newSeriousCountJader" : "cumSeriousCountJader",
             "eb05Jader" : "eb95Jader",
             "newFatalCountJader" : "cumFatalCountJader",
             "newGeriatricCountJader" : "cumGeriatricCountJader",
             "newPediatricCountJader" : "cumPediatricCountJader",
             "prrLCIJader" : "prrUCIJader",
             "rorLCIJader" : "rorUCIJader"
        ]
        return pairs.get(firstName)
    }

}
