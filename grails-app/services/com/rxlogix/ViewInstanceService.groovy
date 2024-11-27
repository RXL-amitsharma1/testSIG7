package com.rxlogix

import com.rxlogix.audit.AuditTrail
import com.rxlogix.audit.AuditTrailChild
import com.rxlogix.config.AdvancedFilter
import com.rxlogix.config.Configuration
import com.rxlogix.config.DefaultViewMapping
import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.json.JsonOutput
import com.rxlogix.signal.UserPinConfiguration
import com.rxlogix.signal.UserViewOrder
import com.rxlogix.signal.ViewInstance
import com.rxlogix.user.User
import com.rxlogix.user.UserGroupMapping
import com.rxlogix.util.RestApiResponse
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import grails.util.Holders
import groovy.sql.Sql
import org.hibernate.criterion.CriteriaSpecification
import com.rxlogix.signal.ClipboardCases
import grails.events.EventPublisher
import org.apache.commons.lang3.SerializationUtils

@Transactional
class ViewInstanceService implements EventPublisher {
    def userService
    def grailsApplication
    def cacheService
    def signalDataSourceService
    AdvancedFilterService advancedFilterService
    def dataSource
    def signalAuditLogService
    def CRUDService
    def alertFieldService

    def serviceMethod() {

    }


    /**
     * Fetches a list of views and a selected view map based on the specified alert type and view ID.
     *
     * @param alertType the type of alert to filter views by
     * @param viewId the ID of the view to be selected
     * @return a list of maps, each containing view details such as ID, name, and user ID
     */
    List<Map> fetchViewsListAndSelectedViewMap(String alertType, Long viewId, User currentUser) {
        List<Long> groupIds = currentUser.groups*.id ?: []
        Long defaultViewId = currentUser.getDefaultViewId(alertType)

        List<Map> viewsList = ViewInstance.createCriteria().list {
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("id", "id")
                property("name", "name")
                property("user.id", "userId")
                property("advancedFilter", "advancedFilter")
                property("sorting", "sorting")
                property("filters", "filters")
            }
            eq('alertType', alertType)
            or {
                eq('user.id', currentUser.id)
                sqlRestriction("id in (Select VIEW_INSTANCE_ID from SHARE_WITH_USER_VIEW where USER_ID = ${currentUser.id})")
                if(groupIds){
                    sqlRestriction("id in (Select VIEW_INSTANCE_ID from SHARE_WITH_GROUP_VIEW where GROUP_ID in (${groupIds.join(",")}))")
                }
                isNull('user.id')
                if (defaultViewId) {
                    eq("id", defaultViewId)
                }
            }
        } as List<Map>

        return fetchViewInstanceList(viewsList, currentUser, viewId, alertType, defaultViewId)
                .unique(false) { a, b -> a.id <=> b.id }
    }

    /**
     * Generates a list of view column information for the specified configuration and view instance.
     *
     * @param configId The ID of the configuration, can be null.
     * @param viewInstanceId The ID of the view instance, can be null.
     * @param selectedDatasource The selected datasource, can be null.
     * @param callingScreen The screen calling this method, used to determine dashboard-related logic, can be null.
     * @return A list of maps where each map contains the title, type, and fields for the view columns.
     */
    List viewColumnInfoList(Long configId, Long viewInstanceId, String selectedDatasource, String callingScreen) {
        Map viewInfoMap = Constants.viewMapping.VIEW_INFO
        if (selectedDatasource == Constants.DataSource.JADER) {
            viewInfoMap = Constants.viewMapping.JADER_VIEW_INFO
        }
        List columnMapList = []
        Map labelConfigDisplay = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', null, null, null).collectEntries {
            b -> [b.name, b.display]
        }
        JsonSlurper js = new JsonSlurper()
        Configuration configuration = configId ? Configuration.get(configId) : null
        ViewInstance viewInstance = viewInstanceId ? ViewInstance.get(viewInstanceId) : null
        if (viewInstance) {
            Map caseListCols = js.parseText(viewInstance.tempColumnSeq ?: viewInstance.columnSeq)
            viewInfoMap.each { index, value ->
                List columnList = []
                caseListCols.each {
                    if (viewInstance.alertType.contains(Constants.AlertType.AGGREGATE_NEW) && it.value.name == "currentDisposition") {
                        boolean labelCondition = configuration?.groupBySmq ?: isLabelChangeRequired(selectedDatasource)
                        it.value.label = labelConfigDisplay["currentDisposition"]?.split("#OR")[labelCondition ? 0 : 1]
                    }
                    Boolean isNotDashboard = callingScreen ? (callingScreen != Constants.Commons.DASHBOARD) || (callingScreen == Constants.Commons.DASHBOARD && !it.value.name.startsWith('exe')) : true
                    if (isNotDashboard) {
                        if (it.value.containerView == index) {
                            Map columnValueMap = it.value as Map
                            columnValueMap.remove("containerView")
                            if (!columnValueMap.containsKey("belongsTo")) {
                                String belongsToProperty = fetchBelongsToProperty(columnValueMap.name)
                                columnValueMap.put("belongsTo", belongsToProperty)
                            }
                            columnList.add(columnValueMap)
                        }
                    }
                }
                if (index != 1) {
                    // sorting only applicable for optional fields
                    columnList = columnList.sort { it.label?.toLowerCase() }
                }
                columnList.removeAll { it.name.endsWith("allRorEvdas") }
                if (configuration?.groupBySmq) {
                    columnList.removeAll { it.name in ['hlt', 'hlgt', 'smqNarrow'] }
                }
                String type = value.type
                String title = value.title
                Boolean isEnabled = Holders.config.signal[type].enabled
                Map viewDataMap = ["title": title, "type": type, "fields": columnList]
                if (index == 1 || index == 2 || isEnabled) {
                    // Safety is default enabled
                    columnMapList.add(viewDataMap)
                }

            }
        }
        return columnMapList
    }
    /**
     * Generates the belongsTo property required by front-end for field selection
     *
     * @param fieldName
     * @return A string field
     */
    String fetchBelongsToProperty(String fieldName) {
        List labels = [
                Constants.ViewsDataSourceLabels.FAERS,
                Constants.ViewsDataSourceLabels.VAERS,
                Constants.ViewsDataSourceLabels.EVDAS,
                Constants.ViewsDataSourceLabels.VIGIBASE,
                Constants.ViewsDataSourceLabels.JADER
        ]
        labels.find { fieldName.endsWith(it) }?.toLowerCase() ?: Constants.ViewsDataSourceLabels.SAFETY
    }


    /**
     * Saves a view instance based on the provided data map and current user. This method handles
     * various configurations related to alert types, filters, sorting, sharing, and default views.
     *
     * @param dataMap      A map containing the necessary data for creating or updating the view instance.
     * @param currentUser  The user who is currently performing the operation.
     * @return A map representing the response data, including the status and any result data such as viewId.
     */
    Map saveViewInstance(Map dataMap, User currentUser) {
        Long currentViewId = dataMap.get("currentViewId")
        String alertType = dataMap.get("alertType")
        Boolean isAdhocCaseSeries = dataMap.get("isAdhocCaseSeries")
        Map sorting = dataMap.get("sorting")
        Map filterMap = dataMap.get("filterMap")
        List viewSharedWith = dataMap.get("viewSharedWith")
        String name = dataMap.get("viewName")
        Boolean defaultView = dataMap.get("defaultView")
        Long advancedFilter = dataMap.get("advancedFilterId")
        dataMap.put("advancedFilter",advancedFilter)
        dataMap.put("name",name)
        Map response = [:]
        ViewInstance viewInstance = new ViewInstance()
        viewInstance.skipAudit = false
        ViewInstance prevViewInstance = ViewInstance.findById(currentViewId)
        Boolean isDrillDown
        Long viewId
        if (isAdhocCaseSeries && Boolean.parseBoolean(isAdhocCaseSeries) && alertType != Constants.AlertConfigType.SINGLE_CASE_ALERT_JADER) {
            alertType = Constants.AlertConfigType.SINGLE_CASE_ALERT_DRILL_DOWN_ADHOC
        } else if (alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT_DRILL_DOWN) {
            isDrillDown = true
            alertType = Constants.AlertConfigType.SINGLE_CASE_ALERT
        }
        viewInstance.name = name.trim()?.replaceAll("\\s{2,}", " ") //Extra Space is removed from name field
        viewInstance.filters = JsonOutput.toJson(filterMap)
        viewInstance.alertType = alertType
        viewInstance.sorting = JsonOutput.toJson(sorting)
        viewInstance.advancedFilter = advancedFilter ? AdvancedFilter.findById(advancedFilter) : null
        Boolean isFilterSharingAllowed = isViewFilterSharingAllowed(viewInstance.advancedFilter, viewSharedWith, Constants.FilterType.ADVANCED_FILTER)
        if (updateAdvancedFilterAllowed(viewInstance, viewSharedWith) && isFilterSharingAllowed) {
            userService.bindSharedWithConfiguration(viewInstance.advancedFilter, viewSharedWith, false, true)
        }
        viewInstance.user = currentUser
        viewInstance.columnSeq = prevViewInstance.tempColumnSeq ?: prevViewInstance.columnSeq
        viewInstance.customAuditProperties = ["defaultView": defaultView.toBoolean()]

        prevViewInstance.tempColumnSeq = null
        Boolean isViewSharingAllowed = isViewFilterSharingAllowed(viewInstance, viewSharedWith)
        if (isViewSharingAllowed) {
            userService.bindSharedWithConfiguration(viewInstance, viewSharedWith, false, true)
        }
        if (defaultView) {
            updateDefaultViewInstance(viewInstance, alertType, currentUser)
        }
        prevViewInstance.save(flush: true)
        CRUDService.saveWithAuditLog(viewInstance)
        if (!viewInstance.advancedFilter?.criteria?.contains('name')) {
            if (isViewPropogationAllowed(alertType)) {
                propagateViews(dataMap, viewInstance.columnSeq, viewSharedWith, currentUser, false, true)
            }
        }
        if (isDrillDown) {
            ViewInstance drillDownViewInstance = ViewInstance.findByNameAndAlertType(viewInstance.name, Constants.AlertConfigType.SINGLE_CASE_ALERT_DRILL_DOWN)
            viewId = drillDownViewInstance?.id
        } else if (isAdhocCaseSeries) {
            ViewInstance drillDownViewInstance = ViewInstance.findByNameAndAlertType(viewInstance.name, Constants.AlertConfigType.SINGLE_CASE_ALERT_DRILL_DOWN_ADHOC)
            viewId = drillDownViewInstance?.id
        }
        viewId = viewId ?: viewInstance.id
        String resultMessage = !isViewSharingAllowed ? message(code: 'duplicate.shared.view.exists') : null
        Map resultData = ["viewId": viewId]
        RestApiResponse.successResponseWithData(response, resultMessage, resultData)
        return response
    }

    /**
     * Updates the given view instance based on the provided data map and current user. This method handles updating the view's properties, such as name, alert type, filters, and sharing configurations, and default views.
     *
     * @param viewInstance  The view instance to be updated.
     * @param dataMap       A map containing the data for updating the view instance.
     * @param currentUser   The user who is currently performing the operation.
     *
     * @return A map representing the response, including any messages about the operation's success or
     *         warnings related to view sharing permissions.
     */
    Map updateView(ViewInstance viewInstance, Map dataMap, User currentUser) {
        Map response = [:]
        Boolean isViewSharingAllowed = true
        String oldName = viewInstance.name
        String alertType = dataMap.get("alertType")
        alertType = viewInstance?.alertType ?: alertType
        List viewSharedWith = dataMap.get("viewSharedWith")
        String name = dataMap.get("viewName")
        Boolean defaultView = dataMap.get("defaultView")
        Long advancedFilter = dataMap.get("advancedFilterId")
        dataMap.put("oldName",oldName)
        dataMap.put("advancedFilter",advancedFilter)
        dataMap.put("name",name)
        if (alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT_DRILL_DOWN) {
            List<ViewInstance> allAssociatedViews = ViewInstance.findAllByNameAndUserAndAlertTypeInList(viewInstance.name, viewInstance.user, [Constants.AlertConfigType.SINGLE_CASE_ALERT_DASHBOARD, Constants.AlertConfigType.SINGLE_CASE_ALERT])
            allAssociatedViews.each { it ->
                it = updateViewInstance(it, dataMap, viewSharedWith, isViewSharingAllowed, true, true)
                it.save(flush: true)
            }
        }
        viewInstance.customAuditProperties = ["defaultView": defaultView]
        viewInstance.markDirty("columnSeq")
        // this is done jsut to capture change in default view when noother property is changed because default view is added manualy this is not domain field
        if (viewInstance?.isViewUpdateAllowed(currentUser)) {
            isViewSharingAllowed = isViewFilterSharingAllowed(viewInstance, viewSharedWith)
            viewInstance = updateViewInstance(viewInstance, dataMap, viewSharedWith, isViewSharingAllowed, true,true)
        }
        viewInstance.save(flush: true)
        if (defaultView) {
            updateDefaultViewInstance(viewInstance, alertType, currentUser)
        } else if (defaultView && currentUser?.getDefaultViewId(alertType) == viewInstance?.id) {
            currentUser.deleteDefaultViewMapping(viewInstance)
        }
        if (!viewInstance?.advancedFilter?.criteria?.contains("'name'")) {
            if (isViewPropogationAllowed(alertType)) {
                dataMap.name = viewInstance.name
                dataMap.alertType = viewInstance.alertType
                propagateViews(dataMap, viewInstance.columnSeq, viewSharedWith, viewInstance.user, true, true)
                if (dataMap.alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT || dataMap.alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT_DASHBOARD) {
                    propagateViewstoDrillDown(dataMap, viewInstance.columnSeq, viewSharedWith, viewInstance.user, true)
                }
            }
        }
        String resultMessage = !isViewSharingAllowed ? message(code: 'duplicate.shared.view.exists') : null
        RestApiResponse.successResponse(response, resultMessage)
        return response
    }

    /**
     * Updates the column sequence for an aggregate view in the given view instance. The method processes the list of case list columns, assigns each field to a container based on its type, and then updates
     * the view instance's temporary column sequence. The new sequence is saved in the database.
     *
     * @param caseListCols  An ArrayList containing the column data maps.
     * @param viewInstance  The view instance being updated. This method sets the updated column sequence
     *                      as a temporary sequence (`tempColumnSeq`) in the view instance and saves the changes.
     */
    void updateAggregateViewColumnSeq(ArrayList caseListCols, ViewInstance viewInstance) {
        Map viewInfoMap = Constants.viewMapping.VIEW_INFO
        String alertType = viewInstance.alertType
        if (alertType in [Constants.AlertConfigType.AGGREGATE_CASE_ALERT_JADER, Constants.AlertConfigType.AGGREGATE_CASE_ALERT_SMQ_JADER]) {
            viewInfoMap = Constants.viewMapping.JADER_VIEW_INFO
        }
        Map viewInfoMapping = [:]
        viewInfoMap.each { Integer index, Map map ->
            viewInfoMapping.put(map.type, index)
        }
        HashMap columnMap = [:]

        int index = 0
        caseListCols.each { Map columnDataMap ->
            String type = columnDataMap.type
            Integer container = viewInfoMapping.get(type)
            ArrayList fieldsList = columnDataMap.fields
            fieldsList.each { Map item ->
                item.put("containerView", container)
                columnMap["${index}"] = item
                index++
            }
        }
        def columnSeqJson = columnMap as JSON
        viewInstance.tempColumnSeq = columnSeqJson
        viewInstance.save(flush: true)
    }

    List<Map> fetchViewInstanceList(List<Map> viewsList, User currentUser, Long selectedViewId, String alertType, Long defaultViewId) {
        LinkedList<Map> userViewOrderList = fetchOrderedViewOrder(viewsList, currentUser, defaultViewId, alertType)
        Map selectedUserViewOrder = userViewOrderList.find { it.viewInstanceId == selectedViewId }
        Integer indexOfSeletedOrder = userViewOrderList.findIndexOf { it.viewInstanceId == selectedViewId }
        if (indexOfSeletedOrder > 4) {
            userViewOrderList.remove(selectedUserViewOrder)
            userViewOrderList.add(4, selectedUserViewOrder)
        }
        List<Map> orderedViewInstances = []
        List<Map> updatedUserViewOrderList = []
        String shared = ''
        boolean isSharedView = true
        userViewOrderList.eachWithIndex { userViewOrder, index ->
            if(userViewOrder.viewOrder != index+1) {
                userViewOrder.viewOrder = index + 1
                updatedUserViewOrderList.add(userViewOrder)
            }
            Map viewInstance = viewsList.find{it.id == userViewOrder.viewInstanceId}
            ViewInstance viewInstanceShareWith = ViewInstance.get(viewInstance?.id as Long)
            shared = (currentUser.id == viewInstance?.userId || !viewInstance.userId) ? '' : Constants.Commons.SHARED
            isSharedView = (currentUser.id == viewInstance?.userId || !viewInstance?.userId) ? false : true
            Map advancedFilterMap  = viewInstance.advancedFilter ? [id:viewInstance.advancedFilter?.id,name:viewInstance.advancedFilter?.name] : null
            orderedViewInstances.add([id: viewInstance?.id, name: viewInstance?.name + shared, isShared: isSharedView, viewUserId: viewInstance?.userId, currentUserId: currentUser.id, isAdmin: currentUser.isAdmin(), defaultView: (viewInstance?.id == defaultViewId || !viewInstance?.userId) ? '(default)' : '', order: userViewOrder.viewOrder, sharedWithUsers: viewInstanceShareWith?.shareWithUser.collect{ [id: Constants.USER_TOKEN + it?.id, name: it?.name] }, sharedWithGroups: viewInstanceShareWith?.shareWithGroup.collect{ [id: Constants.USER_GROUP_TOKEN + it?.id, name: it?.name] }, advancedFilter: advancedFilterMap,sorting: JSON.parse(viewInstance.sorting), filters: JSON.parse(viewInstance.filters)])
        }
        if(updatedUserViewOrderList) {
            notify 'update.user.view.order', [updatedUserViewOrderList:updatedUserViewOrderList]
        }
        return orderedViewInstances
    }

    ViewInstance fetchSelectedViewInstance(List<ViewInstance> viewsList, Long viewId, Long defaultViewId) {
        ViewInstance viewInstance = null
        try {
            if (viewId) {
                viewInstance = ViewInstance.findById(viewId)
            }
            if(!viewInstance && defaultViewId) {
                viewInstance = viewsList.find{
                    it.id == defaultViewId
                }
            }
            if(!viewInstance) {
                viewInstance = viewsList.find {
                    it.defaultValue && it.user == null
                }
            }
        } catch(Throwable ex) {
            viewInstance = viewsList.find {
                it.defaultValue && it.user == null
            }
        }
        viewInstance
    }


    ViewInstance fetchSelectedViewInstance(String alertType, Long viewId = 0L) {
        ViewInstance viewInstance
        User currentUser = userService.getUserFromCacheByUsername(userService.getCurrentUserName())
        ViewInstance defaultViewInstance = DefaultViewMapping.findByAlertTypeAndUser(alertType, currentUser)?.defaultViewInstance
        try {
            if (viewId) {
                viewInstance = ViewInstance.findById(viewId)
            }
            if (!viewInstance && defaultViewInstance) {
                viewInstance = defaultViewInstance
            } else if (!viewInstance) {
                viewInstance = ViewInstance.findByAlertTypeAndUserIsNull(alertType)
            }
        } catch (Throwable ex) {
            viewInstance = ViewInstance.findByDefaultValueAndAlertTypeAndUserIsNull(true, alertType)
        }
        viewInstance
    }

    ViewInstance fetchViewInstanceLiteratureAlert(Boolean isEmbase){
        ViewInstance.findByAlertType(isEmbase ? Constants.AlertConfigType.EMBASE_LITERATURE_SEARCH_ALERT : Constants.AlertConfigType.LITERATURE_SEARCH_ALERT)
    }

    ViewInstance fetchViewInstanceSignal(String alertType){
        def currentUser = userService.getUser()
        ViewInstance defaultView = ViewInstance.findByUserAndAlertType(currentUser, alertType)
        if(!defaultView){
            ViewInstance systemDefaultView = ViewInstance.findByAlertTypeAndUserIsNull(alertType)
            defaultView = new ViewInstance(name: systemDefaultView.name,
                    alertType: alertType,
                    filters: systemDefaultView.filters,
                    sorting: systemDefaultView.sorting,
                    user: currentUser,
                    defaultValue: true,
                    columnSeq: systemDefaultView.columnSeq,
                    iconSeq: systemDefaultView.iconSeq)
            defaultView.save(flush:true)
        }
        defaultView
    }

    ViewInstance fetchViewInstanceBatLotReport(){
        def currentUser = userService.getUser()
        ViewInstance defaultView = ViewInstance.findByUserAndAlertType(currentUser, "Batch Lot Status Report")
        if(!defaultView){
            ViewInstance systemDefaultView = ViewInstance.findByAlertTypeAndUserIsNull("Batch Lot Status Report")
            defaultView = new ViewInstance(name: systemDefaultView.name,
                    alertType: "Signal Management",
                    filters: systemDefaultView.filters,
                    sorting: systemDefaultView.sorting,
                    user: currentUser,
                    defaultValue: true,
                    columnSeq: systemDefaultView.columnSeq,
                    iconSeq: systemDefaultView.iconSeq)
            //defaultView.save(flush:true)
        }
        defaultView
    }

    def updateViewColumnSeq(ArrayList caseListCols, ViewInstance viewInstance){
        if (caseListCols && viewInstance) {
            def columnMap = [:]
            caseListCols.eachWithIndex { item, index ->
                columnMap["${index}"] = item
            }
            def columnSeqJson = columnMap as JSON
            viewInstance.tempColumnSeq = columnSeqJson
            viewInstance.save(flush: true)
            true
        } else {
            false
        }
    }

    def fetchVisibleColumnList(alertType, viewId){
        ViewInstance viewInstance = fetchSelectedViewInstance(alertType, viewId)
        JsonSlurper js = new JsonSlurper()
        def caseListCols = js.parseText(viewInstance?.tempColumnSeq ?: viewInstance?.columnSeq)
        def columnList = []
        caseListCols.each{k, v ->
            if(v.containerView == 1){
                columnList.add(["label":v.label, "name":v.name])
            }
        }
        def dispositionTo = [label: 'Disposition To #OR Disposition (*-Proposed)', name: 'currentDisposition']
        if (columnList.contains(dispositionTo)) {
            columnList.remove(dispositionTo)
        }
        if( alertType.equals( "Single Case Alert" ) || alertType.equals("EVDAS Alert")) {
            columnList.remove( [ label: 'Disposition To', name: 'currentDisposition' ] )
        }
        def disposition=[label:'Disposition', name:'currentDisposition']
        def currDisposition=[label:'Current Disposition', name:'disposition']
        if(columnList.contains(disposition) && columnList.contains(currDisposition))
        {
            columnList=columnList-currDisposition
        }
        return columnList
    }

    def discardTempColumnSeq(ViewInstance viewInstance) {
        if (viewInstance) {
            viewInstance.tempColumnSeq = null
            viewInstance.save()
        }
    }

    //NOTE : DON'T USE log in this method
    def updateAllViewInstances(){
        try {
                Boolean isPriorityEnabled = Holders.config.alert.priority.enable
                List viewInstances  = Holders.config.configurations.viewInstances.collect { SerializationUtils.clone(it) }
                viewInstances.each { Map viDetailMap ->
                    // Fixed Column are updated due to flags updated due to CMT
                    if(isPriorityEnabled && viDetailMap.priorityRequired == true){
                        viDetailMap.fixedColumns +=1
                    }
                    Map columnOrderMap = addOrUpdateColumnMap(viDetailMap)
                    /**
                     * Only Added condition for jader to limit unforeseen impact as only jader views config sequence is updated to empty.
                     */
                    if (columnOrderMap || !(viDetailMap.alertType in [Constants.AlertConfigType.AGGREGATE_CASE_ALERT_JADER, Constants.AlertConfigType.AGGREGATE_CASE_ALERT_SMQ_JADER])) {
                        updateViewInstanceObjects(columnOrderMap, viDetailMap, null, null)
                    }

                }
             checkDataMiningViews()
        }
        catch (Exception ex) {
            println("######## Some error occure while updating the View Instances  ##########")
            ex.printStackTrace()
        }
    }

    Map populateColumnOrderCaseList(Map viDetailMap, Map flagsMap, boolean isFaersEnabled, boolean isEvdasEnabled, boolean isVaersEnabled,  boolean isVigibaseEnabled, boolean isJaderEnabled){

        Map columnOrderMap = [:]
        Integer counter = 0;
        Integer fixedColumns = viDetailMap.fixedColumns as Integer

        String alertType = viDetailMap.alertType
        boolean checkFaers = alertType.toLowerCase().contains("faers")
        boolean checkVaers = alertType.toLowerCase().contains("vaers")
        boolean checkVigibase = alertType.toLowerCase().contains("vigibase")
        boolean isOnDemandFaers = alertType.toLowerCase().contains("demand") && checkFaers
        boolean isOnDemandVaers = alertType.toLowerCase().contains("demand") && checkVaers
        boolean isOnDemandVigibase = alertType.toLowerCase().contains("demand") && checkVigibase
        String columnViewName = viDetailMap.columnList
        List adhocColumnOrderList = cacheService.getAdhocReviewCache()
        List columnOrderCaseList1 = grailsApplication.config.configurations."${columnViewName}"
        if (alertType.equals(Constants.AlertConfigType.ADHOC_ALERT)) {
            columnOrderCaseList1 = adhocColumnOrderList
        }
        List columnOrderCaseList = new ArrayList()
        Map populateMap = [:]
        if (viDetailMap.columnList == "agaColumnOrderListOnDemand") {
            List adhocColumnList = alertFieldService.getAggOnDemandColumnList(Constants.DataSource.PVA, false, false)
            populateMap = populateOnDemandColumns(adhocColumnList, columnOrderMap, counter, fixedColumns)
            counter  = populateMap.counter
        } else if (viDetailMap.columnList == "agaGroupBySMQColumnOrderListonDemand") {
            List adhocColumnList = alertFieldService.getAggOnDemandColumnList(Constants.DataSource.PVA, true, false)
            populateMap = populateOnDemandColumns(adhocColumnList, columnOrderMap, counter, fixedColumns)
            counter  = populateMap.counter
        } else if (viDetailMap.columnList == "agaColumnOrderListOnDemandFaers") {
            List adhocColumnList = alertFieldService.getAggOnDemandColumnList(Constants.DataSource.FAERS, false, false)
            populateMap = populateOnDemandColumns(adhocColumnList, columnOrderMap, counter, fixedColumns)
            counter  = populateMap.counter
        } else if (viDetailMap.columnList == "agaGroupBySMQColumnOrderListonDemandFaers") {
            List adhocColumnList = alertFieldService.getAggOnDemandColumnList(Constants.DataSource.FAERS, true, false)
            populateMap = populateOnDemandColumns(adhocColumnList, columnOrderMap, counter, fixedColumns)
            counter  = populateMap.counter
        } else if (viDetailMap.columnList == "agaColumnOrderListOnDemandVaers") {
            List adhocColumnList = alertFieldService.getAggOnDemandColumnList(Constants.DataSource.VAERS, false, false)
            populateMap = populateOnDemandColumns(adhocColumnList, columnOrderMap, counter, fixedColumns)
        } else if (viDetailMap.columnList == "agaGroupBySMQColumnOrderListonDemandVaers") {
            List adhocColumnList = alertFieldService.getAggOnDemandColumnList(Constants.DataSource.VAERS, true, false)
            populateMap = populateOnDemandColumns(adhocColumnList, columnOrderMap, counter, fixedColumns)
        } else if (viDetailMap.columnList == "agaColumnOrderListOnDemandVigibase") {
            List adhocColumnList = alertFieldService.getAggOnDemandColumnList(Constants.DataSource.VIGIBASE, false, false)
            populateMap = populateOnDemandColumns(adhocColumnList, columnOrderMap, counter, fixedColumns)
        } else if (viDetailMap.columnList == "agaGroupBySMQColumnOrderListonDemandVigibase") {
            List adhocColumnList = alertFieldService.getAggOnDemandColumnList(Constants.DataSource.VIGIBASE, true, false)
            populateMap = populateOnDemandColumns(adhocColumnList, columnOrderMap, counter, fixedColumns)
        }else if (viDetailMap.columnList == "agaColumnOrderListOnDemandJader") {
            List adhocColumnList = alertFieldService.getAggOnDemandColumnList(Constants.DataSource.JADER, false, false)
            populateMap = populateOnDemandColumns(adhocColumnList, columnOrderMap, counter, fixedColumns)
        } else if (viDetailMap.columnList == "agaGroupBySMQColumnOrderListonDemandJader") {
            List adhocColumnList = alertFieldService.getAggOnDemandColumnList(Constants.DataSource.JADER, true, false)
            populateMap = populateOnDemandColumns(adhocColumnList, columnOrderMap, counter, fixedColumns)
        } else if(viDetailMap.columnList == "jaderColumnOrderList" && viDetailMap.alertType == "Aggregate Case Alert - JADER"){
            List jaderColumnList = alertFieldService.getJaderColumnList(Constants.DataSource.JADER,false)
            fixedColumns = Holders.config?.alert?.priority?.enable ? 7 : 6
            populateMap = populateJaderColumns(jaderColumnList, columnOrderMap, counter, fixedColumns)
        } else if(viDetailMap.columnList == "jaderColumnOrderList" && viDetailMap.alertType == "Aggregate Case Alert - SMQ JADER"){
            fixedColumns = Holders.config?.alert?.priority?.enable ? 6 : 5
            List jaderColumnList = alertFieldService.getJaderColumnList(Constants.DataSource.JADER,true)
            populateMap = populateJaderColumns(jaderColumnList, columnOrderMap, counter, fixedColumns)
        }else{

            Map labelConfig = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', null, null, null).collectEntries {
                b -> [b.name, b.enabled]
            }
            Map labelConfigDisplay = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', null, null, null).collectEntries {
                b -> [b.name, b.display]
            }
        if (columnViewName in ['agaColumnOrderList', 'agaGroupBySMQColumnOrderList']) {
            List newFields = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', true).findAll {
                it.enabled == true
            }
            if(columnViewName.equals('agaGroupBySMQColumnOrderList')){
                newFields.removeAll{
                    it.name in ['hlt','hlgt','smqNarrow']
                }
            }

            int lastSequence = columnOrderCaseList1.size()
            columnOrderCaseList1 = columnOrderCaseList1 + newFields
            columnOrderCaseList1.each { obj ->
                if (labelConfig.get(obj["name"]) == true) {
                    obj["label"] = labelConfigDisplay.get(obj["name"])
                    columnOrderCaseList.add(obj)
                }
            }
        }else{
            columnOrderCaseList = grailsApplication.config.configurations."${columnViewName}"
            if (alertType.equals(Constants.AlertConfigType.ADHOC_ALERT)) {
                columnOrderCaseList = adhocColumnOrderList
            }
        }

        def isRor = cacheService.getRorCache()
        boolean autoProposed = Holders.config.dss.enable.autoProposed
        columnOrderCaseList.each { obj ->
             if (flagsMap.dssFlag && obj["name"] in ["rationale", "pecImpNumHigh"]) {
                 if( obj[ "name" ].equals( "rationale" ) ) {
                     if( autoProposed ) {
                         obj[ "label" ] = obj[ "label" ].toString().split( "#OR" )[ 0 ] + obj[ "label" ].toString().split( "#OR" )[ 1 ]
                     } else {
                         obj[ "label" ] = obj[ "label" ].toString().split( "#OR" )[ 2 ]
                     }
                 }
                columnOrderMap."${counter}" = ["containerView": obj["containerView"], "label": obj["label"], "name": obj["name"], "listOrder": obj["containerView"] == 1 ? counter : 9999, "seq": counter + fixedColumns]
                counter++
            }  else if (!isOnDemandFaers && !isOnDemandVaers && !isOnDemandVigibase && flagsMap.prrFlag && obj["name"] in ["prrLCI", "prrValue"]) {
                columnOrderMap."${counter}" = ["containerView": obj["containerView"], "label": obj["label"], "name": obj["name"], "listOrder": obj["containerView"] == 1 ? counter : 9999, "seq": counter + fixedColumns]
                counter++
            } else if (!isOnDemandVaers && !isOnDemandFaers && !isOnDemandVigibase && flagsMap.prrFlag && obj["name"] in ["prrLCI", "prrValue"]) {
                columnOrderMap."${counter}" = ["containerView": obj["containerView"], "label": obj["label"], "name": obj["name"], "listOrder": obj["containerView"] == 1 ? counter : 9999, "seq": counter + fixedColumns]
                counter++
            } else if (!isOnDemandVigibase && !isOnDemandVaers && !isOnDemandFaers && flagsMap.prrFlag && obj["name"] in ["prrLCI", "prrValue"]) {
                columnOrderMap."${counter}" = ["containerView": obj["containerView"], "label": obj["label"], "name": obj["name"], "listOrder": obj["containerView"] == 1 ? counter : 9999, "seq": counter + fixedColumns]
                counter++
            } else if (flagsMap.prrFlagFaers && obj["name"] in ["prrLCIFaers", "prrValueFaers"]) {
                columnOrderMap."${counter}" = ["containerView": obj["containerView"], "label": obj["label"], "name": obj["name"], "listOrder": obj["containerView"] == 1 ? counter : 9999, "seq": counter + fixedColumns]
                counter++
            } else if (flagsMap.prrFlagVaers && obj["name"] in ["prrLCIVaers", "prrValueVaers"]) {
                columnOrderMap."${counter}" = ["containerView": obj["containerView"], "label": obj["label"], "name": obj["name"], "listOrder": obj["containerView"] == 1 ? counter : 9999, "seq": counter + fixedColumns]
                counter++
            } else if (flagsMap.prrFlagVigibase && obj["name"] in ["prrLCIVigibase", "prrValueVigibase"]) {
                if (isRor == true) {
                    obj["label"] = obj["label"].toString().contains("#OR") ? obj["label"].toString().split("#OR")[0] : obj["label"].toString()
                } else {
                    obj["label"] = obj["label"].toString().contains("#OR") ? obj["label"].toString().split("#OR")[1] : obj["label"].toString()
                }
                columnOrderMap."${counter}" = ["containerView": obj["containerView"], "label": obj["label"], "name": obj["name"], "listOrder": obj["containerView"] == 1 ? counter : 9999, "seq": counter + fixedColumns]
                counter++
            }  else if ((!isOnDemandFaers && !isOnDemandVaers && !isOnDemandVigibase && flagsMap.rorFlag && obj["name"] in ["rorLCI", "rorValue"]) || (viDetailMap.columnList.equals("evdasColumnOrderList") && obj.name.equals("rorValue"))) {
                 if (obj["name"].equals("rorValue")) {
                    if (isRor == true) {
                        obj["label"] = obj["label"].toString().contains("#OR") ? obj["label"].toString().split("#OR")[0] : obj["label"].toString()
                    } else {
                        obj["label"] = obj["label"].toString().contains("#OR") ? obj["label"].toString().split("#OR")[1] : obj["label"].toString()
                    }
                } else if (obj["name"].equals("rorLCI")) {
                    if (isRor) {
                        obj["label"] = obj["label"].toString().split("#OR")[0]
                    } else {
                        obj["label"] = obj["label"].toString().split("#OR")[1]
                    }
                }


                columnOrderMap."${counter}" = ["containerView": obj["containerView"], "label": obj["label"], "name": obj["name"], "listOrder": obj["containerView"] == 1 ? counter : 9999, "seq": counter + fixedColumns]
                counter++
            } else if ((flagsMap.rorFlagFaers && obj["name"] in ["rorLCIFaers", "rorValueFaers"])) {
                columnOrderMap."${counter}" = ["containerView": obj["containerView"], "label": obj["label"], "name": obj["name"], "listOrder": obj["containerView"] == 1 ? counter : 9999, "seq": counter + fixedColumns]
                counter++
            } else if ((flagsMap.rorFlagVaers && obj["name"] in ["rorLCIVaers", "rorValueVaers"])) {
                columnOrderMap."${counter}" = ["containerView": obj["containerView"], "label": obj["label"], "name": obj["name"], "listOrder": obj["containerView"] == 1 ? counter : 9999, "seq": counter + fixedColumns]
                counter++
            } else if ((flagsMap.rorFlagVigibase && obj["name"] in ["rorLCIVigibase", "rorValueVigibase"])) {
                columnOrderMap."${counter}" = ["containerView": obj["containerView"], "label": obj["label"], "name": obj["name"], "listOrder": obj["containerView"] == 1 ? counter : 9999, "seq": counter + fixedColumns]
                counter++
            } else if (!isOnDemandFaers && !isOnDemandVaers && !isOnDemandVigibase && (flagsMap.prrFlag && flagsMap.rorFlag) && obj["name"] in ["chiSquare"]) {
                columnOrderMap."${counter}" = ["containerView": obj["containerView"], "label": obj["label"], "name": obj["name"], "listOrder": obj["containerView"] == 1 ? counter : 9999, "seq": counter + fixedColumns]
                counter++
            } else if (!isOnDemandFaers && !isOnDemandVaers && !isOnDemandVigibase && (flagsMap.prrFlag || flagsMap.rorFlag || flagsMap.ebgmFlag) && obj["name"] in ["aValue", "bValue", "cValue", "dValue"]) {
                columnOrderMap."${counter}" = ["containerView": obj["containerView"], "label": obj["label"], "name": obj["name"], "listOrder": obj["containerView"] == 1 ? counter : 9999, "seq": counter + fixedColumns]
                counter++
            }else if (!isOnDemandFaers && !isOnDemandVaers && !isOnDemandVigibase && (flagsMap.ebgmFlag) && obj["name"] in ["eValue", "rrValue"]) {
                columnOrderMap."${counter}" = ["containerView": obj["containerView"], "label": obj["label"], "name": obj["name"], "listOrder": obj["containerView"] == 1 ? counter : 9999, "seq": counter + fixedColumns]
                counter++
            } else if ((flagsMap.prrFlagFaers && flagsMap.rorFlagFaers) && obj["name"] in ["chiSquareFaers"]) {
                columnOrderMap."${counter}" = ["containerView": obj["containerView"], "label": obj["label"], "name": obj["name"], "listOrder": obj["containerView"] == 1 ? counter : 9999, "seq": counter + fixedColumns]
                counter++
            } else if ((flagsMap.prrFlagVaers && flagsMap.rorFlagVaers) && obj["name"] in ["chiSquareVaers"]) {
                columnOrderMap."${counter}" = ["containerView": obj["containerView"], "label": obj["label"], "name": obj["name"], "listOrder": obj["containerView"] == 1 ? counter : 9999, "seq": counter + fixedColumns]
                counter++
            } else if ((flagsMap.prrFlagVigibase && flagsMap.rorFlagVigibase) && obj["name"] in ["chiSquareVigibase"]) {
                columnOrderMap."${counter}" = ["containerView": obj["containerView"], "label": obj["label"], "name": obj["name"], "listOrder": obj["containerView"] == 1 ? counter : 9999, "seq": counter + fixedColumns]
                counter++
            } else if (!isOnDemandFaers && flagsMap.ebgmFlag && obj["name"] in ["ebgm", "eb05"]) {

                columnOrderMap."${counter}" = ["containerView": obj["containerView"], "label": obj["label"], "name": obj["name"], "listOrder": obj["containerView"] == 1 ? counter : 9999, "seq": counter + fixedColumns]
                counter++

            } else if (!isOnDemandVaers && flagsMap.ebgmFlag && obj["name"] in ["ebgm", "eb05"]) {

                columnOrderMap."${counter}" = ["containerView": obj["containerView"], "label": obj["label"], "name": obj["name"], "listOrder": obj["containerView"] == 1 ? counter : 9999, "seq": counter + fixedColumns]
                counter++

            } else if (!isOnDemandVigibase && flagsMap.ebgmFlag && obj["name"] in ["ebgm", "eb05"]) {
                columnOrderMap."${counter}" = ["containerView": obj["containerView"], "label": obj["label"], "name": obj["name"], "listOrder": obj["containerView"] == 1 ? counter : 9999, "seq": counter + fixedColumns]
                counter++
            }else if (flagsMap.ebgmFlag && obj["name"] in ["ebgm", "eb05"]) {
                columnOrderMap."${counter}" = ["containerView": obj["containerView"], "label": obj["label"], "name": obj["name"], "listOrder": obj["containerView"] == 1 ? counter : 9999, "seq": counter + fixedColumns]
                counter++
            } else if (flagsMap.ebgmFlagFaers && obj["name"] in ["ebgmFaers", "eb05Faers"]) {
                columnOrderMap."${counter}" = ["containerView": obj["containerView"], "label": obj["label"], "name": obj["name"], "listOrder": obj["containerView"] == 1 ? counter : 9999, "seq": counter + fixedColumns]
                counter++
            } else if (flagsMap.ebgmFlagVaers && obj["name"] in ["ebgmVaers", "eb05Vaers"]) {
                columnOrderMap."${counter}" = ["containerView": obj["containerView"], "label": obj["label"], "name": obj["name"], "listOrder": obj["containerView"] == 1 ? counter : 9999, "seq": counter + fixedColumns]
                counter++
            } else if (flagsMap.ebgmFlagVigibase && obj["name"] in ["ebgmVigibase", "eb05Vigibase"]) {
                columnOrderMap."${counter}" = ["containerView": obj["containerView"], "label": obj["label"], "name": obj["name"], "listOrder": obj["containerView"] == 1 ? counter : 9999, "seq": counter + fixedColumns]
                counter++
            } else if (obj["name"] == "name" && viDetailMap.cumulative) {
                columnOrderMap."${counter}" = ["containerView": obj["containerView"], "label": obj["label"], "name": obj["name"], "listOrder": obj["containerView"] == 1 ? counter : 9999, "seq": counter + fixedColumns]
                counter++
            } else if (flagsMap.customFieldsEnabled && obj["name"] in ["caseType", "completenessScore", "indNumber", "appTypeAndNum", "compoundingFlag", "submitter", "preAnda","medErrorsPt", "primSuspPai", "paiAll", "crossReferenceInd"]) {
                columnOrderMap."${counter}" = ["containerView": obj["containerView"], "label": obj["label"], "name": obj["name"], "listOrder": obj["containerView"] == 1 ? counter : 9999, "seq": counter + fixedColumns]
                counter++
            }
            else if (!(obj["name"] in ["name", "prrLCI", "prrValue","prrLCIFaers","prrValueFaers","prrLCIVaers","prrValueVaers","prrLCIVigibase","prrValueVigibase","pecImpHigh", "pecImpLow", "rorLCI", "rorValue","rorLCIFaers","rorValueFaers",
                                       "rorLCIVaers","rorValueVaers","rorLCIVigibase","rorValueVigibase", "ebgm", "eb05","ebgmFaers","eb05Faers","ebgmVaers","eb05Vaers","ebgmVigibase","eb05Vigibase", "chiSquare","chiSquareFaers","chiSquareVaers","chiSquareVigibase","primSuspPai", "paiAll",
                                       "caseType", "completenessScore", "indNumber",  "appTypeAndNum", "compoundingFlag", "submitter","preAnda","primSuspPai", "paiAll", "crossReferenceInd", "medErrorsPt","rationale", "pecImpNumHigh","aValue", "bValue", "cValue", "dValue",  "eValue", "rrValue"
                                       ])) {
                if((obj["name"].endsWith(Constants.ViewsDataSourceLabels.FAERS) && isFaersEnabled) || (obj["name"].endsWith(Constants.ViewsDataSourceLabels.VAERS) && isVaersEnabled) || (obj["name"].endsWith(Constants.ViewsDataSourceLabels.VIGIBASE) && isVigibaseEnabled)  || (obj["name"].endsWith(Constants.ViewsDataSourceLabels.EVDAS) && isEvdasEnabled) || !((obj["name"].endsWith(Constants.ViewsDataSourceLabels.FAERS) || (obj["name"].endsWith(Constants.ViewsDataSourceLabels.VAERS)) || (obj["name"].endsWith(Constants.ViewsDataSourceLabels.VIGIBASE)) || obj["name"].endsWith(Constants.ViewsDataSourceLabels.EVDAS) ))) {
                    columnOrderMap."${counter}" = ["containerView": obj["containerView"], "label": obj["label"], "name": obj["name"], "listOrder": obj["containerView"] == 1 ? counter : 9999, "seq": counter + fixedColumns]
                    counter++
                }
            }else{

            }
        }
        populateMap = [columnOrderMap : columnOrderMap, counter : counter]
        }

        if (viDetailMap.columnList in ["agaColumnOrderList", "agaGroupBySMQColumnOrderList"] && !viDetailMap.isDataMining) {
            populateMap = populateDynamicSubGroup(columnOrderMap, counter, fixedColumns, viDetailMap.alertType)
        } else if(viDetailMap.columnList in [ "agaColumnOrderListOnDemand" , "agaGroupBySMQColumnOrderListonDemand", "agaColumnOrderListOnDemandFaers", "agaGroupBySMQColumnOrderListonDemandFaers"] && !viDetailMap.isDataMining) {
            populateMap = populateDynamicSubGroupForOnDemand(columnOrderMap, counter, fixedColumns, !checkFaers, checkVaers, checkVigibase)
        }
        return populateMap
    }


    Map addOrUpdateColumnMap(Map viDetailMap){
        List<String> availableDataSources = grailsApplication.config.pvsignal.supported.datasource.call()
        boolean isFaersEnabled = availableDataSources.contains(Constants.DataSource.FAERS)
        boolean isVaersEnabled = availableDataSources.contains(Constants.DataSource.VAERS)
        boolean isVigibaseEnabled = availableDataSources.contains(Constants.DataSource.VIGIBASE)
        boolean isEvdasEnabled = availableDataSources.contains(Constants.DataSource.EUDRA)
        boolean isJaderEnabled = availableDataSources.contains(Constants.DataSource.JADER)
        def isRor = cacheService.getRorCache()

        Map flagsMap = [
                dssFlag            : grailsApplication.config.statistics.enable.dss,
                prrFlag            : grailsApplication.config.statistics.enable.prr,
                prrFlagFaers       : isFaersEnabled && grailsApplication.config.statistics.faers.enable.prr,
                prrFlagVaers       : isVaersEnabled && grailsApplication.config.statistics.vaers.enable.prr,
                prrFlagVigibase    : isVigibaseEnabled && grailsApplication.config.statistics.vigibase.enable.prr,
                prrFlagJader       : isJaderEnabled && grailsApplication.config.statistics.jader.enable.prr,
                rorFlag            : grailsApplication.config.statistics.enable.ror,
                rorFlagFaers       : isFaersEnabled && grailsApplication.config.statistics.faers.enable.ror,
                rorFlagVaers       : isVaersEnabled && grailsApplication.config.statistics.vaers.enable.ror,
                rorFlagVigibase    : isVigibaseEnabled && grailsApplication.config.statistics.vigibase.enable.ror,
                rorFlagJader       : isJaderEnabled && grailsApplication.config.statistics.jader.enable.ror,
                ebgmFlag           : grailsApplication.config.statistics.enable.ebgm,
                ebgmFlagFaers      : isFaersEnabled && grailsApplication.config.statistics.faers.enable.ebgm,
                ebgmFlagVaers      : isVaersEnabled && grailsApplication.config.statistics.vaers.enable.ebgm,
                ebgmFlagVigibase   : isVigibaseEnabled && grailsApplication.config.statistics.vigibase.enable.ebgm,
                ebgmFlagJader      : isJaderEnabled && grailsApplication.config.statistics.jader.enable.ebgm,
                customFieldsEnabled: grailsApplication.config.custom.qualitative.fields.enabled,
        ]

        //populating column order Map
        Map populatedMap = populateColumnOrderCaseList(viDetailMap, flagsMap, isFaersEnabled, isEvdasEnabled, isVaersEnabled, isVigibaseEnabled,isJaderEnabled)
        Map columnOrderMap = populatedMap.columnOrderMap
        Integer counter = populatedMap.counter
        Integer fixedColumns = viDetailMap.fixedColumns as Integer
        Integer prevColumnCount = 0
        Map prevColumnMap = [:]
        if (viDetailMap.alertType in [Constants.AlertConfigType.EVDAS_ALERT, Constants.AlertConfigType.EVDAS_ALERT_CUMMULATIVE]) {
            prevColumnMap = grailsApplication.config.signal.evdas.data.previous.columns
            prevColumnCount = grailsApplication.config.signal.evdas.number.previous.columns
            (0..<prevColumnCount).each { idx ->
                Integer idxLabel = idx + 1
                prevColumnMap.each { k, v ->
                    columnOrderMap."${counter}" = ["containerView": 3, "label": "Prev Period " + idxLabel + " " + v, "name": "exe" + idx + k, "listOrder": 9999, "seq": counter + fixedColumns]
                    counter++
                }
            }
        } else if (viDetailMap.alertType in [Constants.AlertConfigType.AGGREGATE_CASE_ALERT_JADER, Constants.AlertConfigType.AGGREGATE_CASE_ALERT_SMQ_JADER]) {
            prevColumnMap =  grailsApplication.config.signal.quantitative.data.prev.columns
            prevColumnCount = grailsApplication.config.signal.quantitative.number.prev.columns
            Boolean isSmq = viDetailMap.alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT_SMQ_JADER ?: true
            List jaderFieldList = alertFieldService.getJaderColumnList(Constants.DataSource.JADER,isSmq)
            Map labelConfig = jaderFieldList.collectEntries {
                b -> [b.name, b.enabled]
            }
            Map jaderColumnDisplay = jaderFieldList.collectEntries {
                b -> [b.name, b.display]
            }
            prevColumnMap = grailsApplication.config.signal.quantitative.data.prev.columns
            prevColumnCount = grailsApplication.config.signal.quantitative.number.prev.columns
            (0..<prevColumnCount).each { idx ->
                Integer idxLabel = idx + 1
                jaderFieldList.each{ it ->
                    if(it.type == "count" || it.type == "countStacked") {
                        columnOrderMap."${counter}" = ["containerView": 3, "label": "Prev Period " + idxLabel + " " + it.display, "name": "exe" + idx + it.name, "listOrder": 9999, "seq": counter + fixedColumns]
                        counter++
                    }
                }
            }
        } else if (viDetailMap.alertType in [Constants.AlertConfigType.AGGREGATE_CASE_ALERT, Constants.AlertConfigType.AGGREGATE_CASE_ALERT_CUMMULATIVE, Constants.AlertConfigType.AGGREGATE_CASE_ALERT_SMQ, Constants.AlertConfigType.AGGREGATE_CASE_ALERT_FAERS, Constants.AlertConfigType.AGGREGATE_CASE_ALERT_VAERS, Constants.AlertConfigType.AGGREGATE_CASE_ALERT_SMQ_VAERS, Constants.AlertConfigType.AGGREGATE_CASE_ALERT_VIGIBASE, Constants.AlertConfigType.AGGREGATE_CASE_ALERT_SMQ_VIGIBASE]) {
            prevColumnMap = grailsApplication.config.signal.quantitative.data.prev.columns
            prevColumnCount = grailsApplication.config.signal.quantitative.number.prev.columns
            Map labelConfig = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', null, null, null).collectEntries {
                b -> [b.name, b.enabled]
            }
            Map labelConfigDisplay = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', null, null, null).collectEntries {
                b -> [b.name, b.display]
            }
            Map subGroupMap = cacheService.getSubGroupMap()
            Map<String,List<String>> allSubGroupMap = cacheService.getAllOtherSubGroupColumnsListMap(Constants.DataSource.PVA)
            Map<String,List<String>> relativeSubGroupMap = cacheService.getRelativeSubGroupColumnsListMap(Constants.DataSource.PVA)
            (0..<prevColumnCount).each { idx ->
                Integer idxLabel = idx + 1
                prevColumnMap.count.each { k, v ->
                    if (labelConfig.get(k)) {
                        v = labelConfigDisplay.get(k)
                        columnOrderMap."${counter}" = ["containerView": 2, "label": "Prev Period " + idxLabel + " " + v, "name": "exe" + idx + k, "listOrder": 9999, "seq": counter + fixedColumns]
                        counter++
                    }
                }
                if (flagsMap.prrFlag) {
                    prevColumnMap.prr.each { k, v ->
                        if (labelConfig.get(k)) {
                            if (k.equals("prrValue")) {
                                v = labelConfigDisplay.get("prrValue")
                            } else if (k.equals("prrLCI")) {
                                v = labelConfigDisplay.get("prrLCI")
                            }
                            columnOrderMap."${counter}" = ["containerView": 2, "label": "Prev Period " + idxLabel + " " + v, "name": "exe" + idx + k, "listOrder": 9999, "seq": counter + fixedColumns]
                            counter++
                        }

                    }
                }
                if (flagsMap.rorFlag) {
                    prevColumnMap.ror.each { k, v ->
                        if (labelConfig.get(k)) {
                            String label
                            if (k.equals("rorValue")) {
                                label = labelConfigDisplay.get("rorValue")
                            } else if (k.equals("rorLCI")) {
                                label = labelConfigDisplay.get("rorLCI")
                            }
                            if (isRor == true && label.split("#OR").size() > 1) {
                                label = label.split("#OR")[0]
                            } else {
                                label = label.split("#OR")[1]
                            }
                            columnOrderMap."${counter}" = ["containerView": 2, "label": "Prev Period " + idxLabel + " " + label, "name": "exe" + idx + k, "listOrder": 9999, "seq": counter + fixedColumns]
                            counter++

                        }


                    }
                }
                if (flagsMap.ebgmFlag) {
                    prevColumnMap.ebgm.each { k, v ->
                        if (labelConfig.get(k)) {
                            v = labelConfigDisplay.get(k)
                            columnOrderMap."${counter}" = ["containerView": 2, "label": "Prev Period " + idxLabel + " " + v, "name": "exe" + idx + k, "listOrder": 9999, "seq": counter + fixedColumns]
                            counter++

                        }
                    }
                }

                if (flagsMap.prrFlag && flagsMap.rorFlag) {
                    if (labelConfig.get("chiSquare")) {
                        columnOrderMap."${counter}" = ["containerView": 2, "label": "Prev Period " + idxLabel + " " + labelConfigDisplay.get("chiSquare"), "name": "exe" + idx + "chiSquare", "listOrder": 9999, "seq": counter + fixedColumns]
                        counter++
                    }
                }

                if (flagsMap.ebgmFlag) {
                    if (labelConfig.get("rrValue")) {
                        columnOrderMap."${counter}" = ["containerView": 2, "label": "Prev Period " + idxLabel + " " + labelConfigDisplay.get("rrValue"), "name": "exe" + idx + "rrValue", "listOrder": 9999, "seq": counter + fixedColumns]
                        counter++
                    }
                }

                prevColumnMap.count.each { k, v ->
                    if (labelConfig.get( k + "Faers")) {
                        v = labelConfigDisplay.get(k + "Faers")
                        columnOrderMap."${counter}" = ["containerView": 3, "label": "Prev Period " + idxLabel + " " + v, "name": "exe" + idx + k + "Faers", "listOrder": 9999, "seq": counter + fixedColumns]
                        counter++
                    }
                }
                if (flagsMap.prrFlagFaers) {
                    prevColumnMap.prr.each { k, v ->
                        if (labelConfig.get(k + "Faers")) {
                            v = labelConfigDisplay.get(k + "Faers")
                            columnOrderMap."${counter}" = ["containerView": 3, "label": "Prev Period " + idxLabel + " " + v, "name": "exe" + idx + k + "Faers", "listOrder": 9999, "seq": counter + fixedColumns]
                            counter++
                        }
                    }
                }
                if (flagsMap.rorFlagFaers) {
                    prevColumnMap.ror.each { k, v ->
                        if (labelConfig.get(k + "Faers")) {
                            v = labelConfigDisplay.get(k + "Faers")
                            columnOrderMap."${counter}" = ["containerView": 3, "label": "Prev Period " + idxLabel + " " + v, "name": "exe" + idx + k + "Faers", "listOrder": 9999, "seq": counter + fixedColumns]
                            counter++
                        }
                    }
                }
                if (flagsMap.ebgmFlagFaers) {
                    prevColumnMap.ebgm.each { k, v ->
                        if (labelConfig.get(k + "Faers")) {
                            v = labelConfigDisplay.get(k + "Faers")
                            columnOrderMap."${counter}" = ["containerView": 3, "label": "Prev Period " + idxLabel + " " + v, "name": "exe" + idx + k + "Faers", "listOrder": 9999, "seq": counter + fixedColumns]
                            counter++
                        }
                    }
                }

                if (flagsMap.prrFlagFaers && flagsMap.rorFlagFaers) {
                    if (labelConfig.get("chiSquareFaers")) {
                        columnOrderMap."${counter}" = ["containerView": 3, "label": "Prev Period " + idxLabel + " " + labelConfigDisplay.get("chiSquareFaers"), "name": "exe" + idx + "chiSquareFaers", "listOrder": 9999, "seq": counter + fixedColumns]
                        counter++
                    }
                }

                if (isVaersEnabled) {
                    prevColumnMap.vaersCount.each { k, v ->
                        if (labelConfig.get(k + "Vaers")) {
                            v = labelConfigDisplay.get(k + "Vaers")
                            columnOrderMap."${counter}" = ["containerView": 5, "label": "Prev Period " + idxLabel + " " + v, "name": "exe" + idx + k + "Vaers", "listOrder": 9999, "seq": counter + fixedColumns]
                            counter++
                        }
                    }

                    if (flagsMap.prrFlagVaers) {
                        prevColumnMap.prr.each { k, v ->
                            if (labelConfig.get( k + "Vaers")) {
                                v = labelConfigDisplay.get(k + "Vaers")
                                columnOrderMap."${counter}" = ["containerView": 5, "label": "Prev Period " + idxLabel + " " + v, "name": "exe" + idx + k + "Vaers", "listOrder": 9999, "seq": counter + fixedColumns]
                                counter++
                            }
                        }
                    }
                    if (flagsMap.rorFlagVaers) {
                        prevColumnMap.ror.each { k, v ->
                            if (labelConfig.get( k + "Vaers")) {
                                v = labelConfigDisplay.get( "rorValueVaers" )
                                columnOrderMap."${counter}" = ["containerView": 5, "label": "Prev Period " + idxLabel + " " + v, "name": "exe" + idx + k + "Vaers", "listOrder": 9999, "seq": counter + fixedColumns]
                                counter++
                            }
                        }
                    }
                    if (flagsMap.ebgmFlagVaers) {
                        prevColumnMap.ebgm.each { k, v ->
                            if (labelConfig.get( k + "Vaers")) {
                                v = labelConfigDisplay.get(k + "Vaers")
                                columnOrderMap."${counter}" = ["containerView": 5, "label": "Prev Period " + idxLabel + " " + v, "name": "exe" + idx + k + "Vaers", "listOrder": 9999, "seq": counter + fixedColumns]
                                counter++
                            }
                        }
                    }

                    if (flagsMap.prrFlagVaers && flagsMap.rorFlagVaers) {
                        if (labelConfig.get("chiSquareVaers")) {
                            columnOrderMap."${counter}" = ["containerView": 5, "label": "Prev Period " + idxLabel + " " + labelConfigDisplay.get("chiSquareVaers"), "name": "exe" + idx + "chiSquareVaers", "listOrder": 9999, "seq": counter + fixedColumns]
                            counter++
                        }
                    }
                }

                [Holders.config.subgrouping.ageGroup.name, Holders.config.subgrouping.gender.name].each {
                    subGroupMap[it]?.each { id, category ->
                        Map populateMap = populateDynamicColumnsPrev(labelConfigDisplay, labelConfig, category, idx, idxLabel, columnOrderMap, counter, fixedColumns)
                        columnOrderMap = populateMap.columnOrderMap
                        counter = populateMap.counter
                    }
                }

                allSubGroupMap?.each { type, category ->
                    category?.each { it ->
                        Map populateMap = populateAllDynamicColumnsPrev(labelConfigDisplay, labelConfig, it, idx, idxLabel, columnOrderMap, counter, fixedColumns, type)
                        columnOrderMap = populateMap.columnOrderMap
                        counter = populateMap.counter
                    }
                }
                relativeSubGroupMap?.each { type, category ->
                    category?.each { it ->
                        Map populateMap = populateRelativeDynamicColumnsPrev(labelConfigDisplay, labelConfig, it, idx, idxLabel, columnOrderMap, counter, fixedColumns, type)
                        columnOrderMap = populateMap.columnOrderMap
                        counter = populateMap.counter
                    }
                }

                if (isFaersEnabled) {
                    [Holders.config.subgrouping.faers.ageGroup.name, Holders.config.subgrouping.faers.gender.name].each {
                        subGroupMap[it]?.each { id, category ->
                            Map populateMap = populateDynamicColumnsPrevFaers(labelConfigDisplay, labelConfig, category, idx, idxLabel, columnOrderMap, counter, fixedColumns)
                            columnOrderMap = populateMap.columnOrderMap
                            counter = populateMap.counter
                        }
                    }
                }
            }

            if (isVigibaseEnabled) {
                prevColumnMap = grailsApplication.config.signal.quantitative.data.prev.columns
                prevColumnCount = grailsApplication.config.signal.quantitative.number.prev.columns
                (0..<prevColumnCount).each { idx ->
                    Integer idxLabel = idx + 1
                    prevColumnMap.vigibaseCount.each { k, v ->
                        if(labelConfig.get(k+"Vigibase"))
                        {
                        v = labelConfigDisplay.get(k+"Vigibase")
                        columnOrderMap."${counter}" = ["containerView": 6, "label": "Prev Period " + idxLabel + " " + v, "name": "exe" + idx + k + "Vigibase", "listOrder": 9999, "seq": counter + fixedColumns]
                        counter++
                    }
                    }
                    if (flagsMap.prrFlagVigibase) {
                        prevColumnMap.prrvb.each { k, v ->
                            if(labelConfig.get(k+"Vigibase"))
                            {
                            if (k.equals("prrValue")) {
                                v = labelConfigDisplay.get("prrValueVigibase")
                            } else if (k.equals("prrLCI")) {
                                v = labelConfigDisplay.get("prrLCIVigibase")
                            }
                            columnOrderMap."${counter}" = ["containerView": 6, "label": "Prev Period " + idxLabel + " " + v, "name": "exe" + idx + k + "Vigibase", "listOrder": 9999, "seq": counter + fixedColumns]
                            counter++
                        }
                        }
                    }
                    if (flagsMap.rorFlagVigibase) {
                        prevColumnMap.rorvb.each { k, v ->
                            if( labelConfig.get( k + "Vigibase" ) ) {
                                if( k.equals( "rorValue" ) ) {
                                    v = labelConfigDisplay.get( "rorValueVigibase" )
                                } else if( k.equals( "prrLCI" ) ) {
                                    v = labelConfigDisplay.get( "rorLCIVigibase" )
                                }
                                columnOrderMap."${ counter }" = [ "containerView": 6, "label": "Prev Period " + idxLabel + " " + v, "name": "exe" + idx + k + "Vigibase", "listOrder": 9999, "seq": counter + fixedColumns ]
                                counter++
                            }
                        }
                    }
                    if (flagsMap.ebgmFlagVigibase) {
                        prevColumnMap.ebgm.each { k, v ->
                            if(labelConfig.get(k+"Vigibase"))
                            {
                            v = labelConfigDisplay.get(k+"Vigibase")
                            columnOrderMap."${counter}" = ["containerView": 6, "label": "Prev Period " + idxLabel + " " + v, "name": "exe" + idx + k + "Vigibase", "listOrder": 9999, "seq": counter + fixedColumns]
                            counter++
                        }
                        }
                    }

                    if (flagsMap.prrFlagVigibase && flagsMap.rorFlagVigibase) {
                        if(labelConfig.get("chiSquareVigibase"))
                        {
                        columnOrderMap."${counter}" = ["containerView": 6, "label": "Prev Period " + idxLabel + " " + labelConfigDisplay.get("chiSquareVigibase"), "name": "exe" + idx + "chiSquareVigibase", "listOrder": 9999, "seq": counter + fixedColumns]
                        counter++
                    }
                    }
                }
            }

            if (isEvdasEnabled) {
                prevColumnMap = grailsApplication.config.signal.evdas.data.previous.columns
                prevColumnCount = grailsApplication.config.signal.evdas.number.previous.columns
                (0..<prevColumnCount).each { idx ->
                    prevColumnMap.each { k, v ->
                        if(labelConfig.get(k+ "Evdas"))
                        {
                        v = labelConfigDisplay.get(k + "Evdas")
                        columnOrderMap."${counter}" = ["containerView": 4, "label": "Prev Period ${idx + 1} $v ", "name": "exe" + idx + k + "Evdas", "listOrder": 9999, "seq": counter + fixedColumns]
                        counter++
                    }
                    }
                }
            }
            if (viDetailMap.columnList in ['agaColumnOrderList', 'agaGroupBySMQColumnOrderList']) {
                def alertFields = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', true).collectEntries {
                    b -> [b.name, b.display]
                }
                def alertFieldsData = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', true).collectEntries {
                    b -> [b.name, b.enabled]
                }
                def alertFieldsContainerView = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', true).collectEntries {
                    b -> [b.name, b.containerView]
                }
                alertFields.remove("hlt")
                alertFields.remove("smqNarrow")
                alertFields.remove("hlgt")
                prevColumnMap = alertFields
                prevColumnCount = grailsApplication.config.signal.quantitative.number.prev.columns
                (0..<prevColumnCount).each { idx ->
                    Integer idxLabel = idx + 1
                    prevColumnMap.each { k, v ->
                        if (alertFieldsData.get(k)) {
                            columnOrderMap."${counter}" = ["containerView": alertFieldsContainerView.get(k), "label": "Prev Period " + idxLabel + " " + v, "name": "exe" + idx + k, "listOrder": 9999, "seq": counter + fixedColumns]
                            counter++
                        }

                    }
                }
            }
        }

        return columnOrderMap
    }

    void updateViewInstanceObjects(Map columnOrderMap, Map viDetailMap, Long keyId, Map miningVariable){
        List newNames = columnOrderMap.values()*.name
        List newLabels = columnOrderMap.values()*.label
        String alertType = ""
        List<ViewInstance> viewInstanceList = ViewInstance.findAllByAlertType(viDetailMap.alertType)
        Boolean isSystemViewAbsent = false
        if(viDetailMap.isDataMining){
            alertType = viDetailMap.alertType + "-" + miningVariable?.label
            viewInstanceList = ViewInstance.findAllByAlertType(alertType)
            isSystemViewAbsent = !viewInstanceList?.any { it.name == Constants.Commons.DEFAULT_VIEW }
        }
        JsonSlurper jsonSlurper = new JsonSlurper()
        int counterStart = 0
        if(viewInstanceList) {
            viewInstanceList.each { ViewInstance vi ->
                if (vi.name.equals(Constants.Commons.DEFAULT_VIEW)) {
                    Map columns = vi.tempColumnSeq ? jsonSlurper.parseText(vi.tempColumnSeq) : [:]
                    vi.columnSeq = JsonOutput.toJson(columnOrderMap)
                    if (vi.alertType == Constants.AlertConfigType.SIGNAL_MANAGEMENT && columns.size() != columnOrderMap.size()) {
                        vi.tempColumnSeq = null
                    }
                    if (vi.alertType == Constants.AlertConfigType.ADHOC_ALERT && columns.size() > 0) {
                        String jsonOutput = JsonOutput.toJson(syncAndUpdateColumns(columnOrderMap, columns))
                        vi.tempColumnSeq = (jsonOutput == '{}') ? null : jsonOutput
                    }
                } else {
                    Map viSequenceList = jsonSlurper.parseText(vi.columnSeq)
                    //Add column sequence if not present to prevent failure while updating.
                    if (viSequenceList) {
                        Map namesCount = [:]
                        viSequenceList.each { key, value ->
                            if (namesCount.containsKey(value.name)) {
                                namesCount.put(value.name, key)
                            } else {
                                namesCount.put(value.name, 0)
                            }
                        }
                        namesCount.each { key, value ->
                            if (value != 0) {
                                viSequenceList.remove(value)
                            }
                        }

                        //Handling for Fixed Columns
                        int prevFixedColumns = viSequenceList.findAll { it.value.seq != null }.min {
                            it.value.seq
                        }.value.seq

                        viSequenceList.each {
                            if (it?.value?.seq) {
                                it.value.seq = it.value.seq + ((viDetailMap.fixedColumns as Integer) - prevFixedColumns)
                            }
                        }

                        Map viSequenceListDel = jsonSlurper.parseText(vi.columnSeq)
                        List nameListTemp = viSequenceList.values()*.name
                        List labelListTemp = viSequenceList.values()*.label
                        List addedColumnsNameList = newNames.minus(nameListTemp)  //This in case new columns are added
                        List addedColumnLabelList = newLabels.minus(labelListTemp) // This in case new columns are added
//                  To check that if only labels are changed
                        List existingLabelList = []
                        if (addedColumnLabelList) {
                            addedColumnLabelList.each { exLabel ->
                                String existingName = columnOrderMap.find { it.value.label == exLabel }.value.name
                                String existingNameKey = viSequenceList.find { it.value.name == existingName }?.key
                                if (existingNameKey) {
                                    viSequenceList.get(existingNameKey).label = exLabel
                                    existingLabelList.add(exLabel)
                                }
                            }
                        }
                        addedColumnLabelList = addedColumnLabelList.minus(existingLabelList)
                        Map seqMap = [:]
                        addedColumnsNameList.each { name ->
                            seqMap.put(name, columnOrderMap.values().find { it.name == name }?.label)
                        }
                        addedColumnLabelList.each { label ->
                            seqMap.put(columnOrderMap.values().find { it.label == label }?.name, label)
                        }


                        // if column deleted, present in existing but not in new
                        List<String> deletedCols = nameListTemp - newNames
                        if (deletedCols) {
                            Integer cStart = 0
                            viSequenceList = new LinkedHashMap()
                            for (Map.Entry<?, ?> entry : viSequenceListDel.entrySet()) {
                                if (!deletedCols.contains(entry.getValue().name)) {
                                    viSequenceList."${cStart}" = ["containerView": entry.getValue().containerView, "label": entry.getValue().label, "name": entry.getValue().name, "listOrder": entry.getValue().listOrder, "seq": cStart + viDetailMap.fixedColumns]
                                    cStart++
                                }
                            }
                        }

                        counterStart = viSequenceList.size()
                        Integer tempSize = addedColumnsNameList.size()

                        seqMap.each { key, value ->
                            if (vi.alertType.startsWith(Constants.AlertConfigType.AGGREGATE_CASE_ALERT)) {
                                int containerView = columnOrderMap.values().find { columnOrder -> value == columnOrder.label }.containerView
                                if (containerView == 1) {
                                    containerView = vi.alertType.contains(Constants.AlertConfigTypeShort.ON_DEMAND) ? 3 : 2
                                    if (value.toString().endsWith("(F)")) {
                                        containerView = 3
                                    } else if (value.toString().endsWith("(E)")) {
                                        containerView = 4
                                    } else if (value.toString().endsWith("(VA)")) {
                                        containerView = 5
                                    } else if (value.toString().endsWith("(VB)")) {
                                        containerView = 6
                                    } else if (value.toString().endsWith("(J)")) {
                                        containerView = 7
                                    }
                                }
                                viSequenceList."${counterStart}" = ["containerView": containerView, "label": value, "name": key, "listOrder": 9999, "seq": counterStart + viDetailMap.fixedColumns]

                            } else {
                                viSequenceList."${counterStart}" = ["containerView": 3, "label": value, "name": key, "listOrder": 9999, "seq": counterStart + viDetailMap.fixedColumns]
                            }
                            counterStart++
                        }

                        if (deletedCols || seqMap) {
                            viSequenceList = updateColumnOrderSeq(columnOrderMap, viSequenceList)
                        }
                        vi.columnSeq = JsonOutput.toJson(viSequenceList)

                    } else {
                        vi.columnSeq = JsonOutput.toJson(columnOrderMap)
                    }

                }
                vi.save()
            }
        }
        if (viewInstanceList?.size() == 0 || isSystemViewAbsent) {
            ViewInstance vi = new ViewInstance(name: viDetailMap.name, alertType: viDetailMap.alertType, filters: viDetailMap.filters, columnSeq: JsonOutput.toJson(columnOrderMap), sorting: viDetailMap.sorting, defaultValue: viDetailMap.defaultValue)
            if(viDetailMap.isDataMining){
                vi.alertType=alertType
                vi.keyId = keyId as Long
            }
            vi.save(failOnError: true)
        }
    }

    Map updateColumnOrderSeq(Map originalMapSeq, Map currentMapSeq) {
        Map updatedColumnSeq = [:]
        Integer seq = 0
        def originalMapList = originalMapSeq.values()
        currentMapSeq.each { key, value ->
            if (value.label == "Disposition") {
                seq = currentMapSeq.find { it.value.name == value.name && it.value.label == "Disposition" }?.value.seq as Integer
                value.put('label', 'Disposition To')
                value.put('seq', seq)
                updatedColumnSeq.put(key, value)

            } else {
                seq = originalMapList.find {it.name == value.name  }?.seq as Integer
                value.put('seq', seq)
                updatedColumnSeq.put(key, value)

            }
        }
        return updatedColumnSeq
    }


    void propagateViews(Map viDetailMap, String columnSeq, List viewSharedWith, User user = null, Boolean isUpdateRequest = false, Boolean isRestCall = false) {
        JsonSlurper jsonSlurper = new JsonSlurper()
        String alertTypeUpdated = getUpdatedAlertType(viDetailMap.alertType)
        User currentUser = user ?: userService.getUser()
        ViewInstance viewInstance = null

        if (isUpdateRequest) {
            viewInstance = ViewInstance.findByNameAndAlertTypeAndUser(viDetailMap.oldName?.trim(), alertTypeUpdated, currentUser)
        }

        if (!viewInstance?.id) {
            viewInstance = new ViewInstance()
        }
        if (!viewInstance?.id || viewInstance?.isViewUpdateAllowed(currentUser)) {
            Map filterMap = isRestCall ? viDetailMap.filterMap : jsonSlurper.parseText(viDetailMap.filterMap)
            Map columnSeqMap = jsonSlurper.parseText(columnSeq)
            Map sortingMap = isRestCall ? viDetailMap.sorting : jsonSlurper.parseText(viDetailMap.sorting)
            Integer fixedColDiff = getFixedColumnDifference(alertTypeUpdated, viDetailMap.alertType)
            viewInstance.name = viDetailMap.name.trim()
            viewInstance.alertType = alertTypeUpdated
            viewInstance.filters = getUpdatedFilterMap(filterMap, fixedColDiff)
            viewInstance.sorting = getUpdatedSortingMap(sortingMap, fixedColDiff)
            viewInstance.advancedFilter = viDetailMap.advancedFilter ? AdvancedFilter.findById(viDetailMap.advancedFilter) : null
            Boolean isFilterSharingAllowed = isViewFilterSharingAllowed(viewInstance.advancedFilter, viewSharedWith, Constants.FilterType.ADVANCED_FILTER)
            if(updateAdvancedFilterAllowed(viewInstance, viewSharedWith) && isFilterSharingAllowed){
                userService.bindSharedWithConfiguration(viewInstance.advancedFilter, viewSharedWith, isUpdateRequest, true)
            }
            viewInstance.user = viewInstance?.user ?: currentUser
            viewInstance.columnSeq = getUpdatedColumnSeq(columnSeqMap, fixedColDiff)
            userService.bindSharedWithConfiguration(viewInstance, viewSharedWith, isUpdateRequest, true)
        }

        viewInstance.save(flush: true)
        if (viDetailMap.defaultView?.toBoolean()) {
            updateDefaultViewInstance(viewInstance, alertTypeUpdated,currentUser)
        } else if (!viDetailMap.defaultView?.toBoolean() && currentUser.getDefaultViewId(alertTypeUpdated) == viewInstance?.id) {
            currentUser.deleteDefaultViewMapping(viewInstance)
        }
    }

    void propagateViewstoDrillDown(Map viDetailMap, String columnSeq, List viewSharedWith, User user = null, Boolean isUpdateRequest = false) {
        JsonSlurper jsonSlurper = new JsonSlurper()
        String alertTypeUpdated = Constants.AlertConfigType.SINGLE_CASE_ALERT_DRILL_DOWN
        User currentUser = user ?: userService.getUser()
        ViewInstance viewInstance = null

        if (isUpdateRequest) {
            viewInstance = ViewInstance.findByNameAndAlertTypeAndUser(viDetailMap.oldName?.trim(), alertTypeUpdated, currentUser)
        }

        if (!viewInstance?.id) {
            viewInstance = new ViewInstance()
        }
        if (!viewInstance?.id || viewInstance?.isViewUpdateAllowed(currentUser)) {
            Map filterMap = jsonSlurper.parseText(viDetailMap.filterMap)
            Map columnSeqMap = jsonSlurper.parseText(columnSeq)
            Map sortingMap = jsonSlurper.parseText(viDetailMap.sorting)
            Integer fixedColDiff = getFixedColumnDifference(alertTypeUpdated, viDetailMap.alertType)
            viewInstance.name = viDetailMap.name.trim()
            viewInstance.alertType = alertTypeUpdated
            viewInstance.filters = getUpdatedFilterMap(filterMap, fixedColDiff)
            viewInstance.sorting = getUpdatedSortingMap(sortingMap, fixedColDiff)
            viewInstance.advancedFilter = viDetailMap.advancedFilter ? AdvancedFilter.findById(viDetailMap.advancedFilter) : null
            Boolean isFilterSharingAllowed = isViewFilterSharingAllowed(viewInstance.advancedFilter, viewSharedWith, Constants.FilterType.ADVANCED_FILTER)
            if(updateAdvancedFilterAllowed(viewInstance, viewSharedWith) && isFilterSharingAllowed){
                userService.bindSharedWithConfiguration(viewInstance.advancedFilter, viewSharedWith, isUpdateRequest, true)
            }
            viewInstance.user = viewInstance?.user ?: currentUser
            viewInstance.columnSeq = getUpdatedColumnSeq(columnSeqMap, fixedColDiff)
            userService.bindSharedWithConfiguration(viewInstance, viewSharedWith, isUpdateRequest, true)
        }

        viewInstance.save(flush: true)
        if (viDetailMap.defaultView?.toBoolean()) {
            updateDefaultViewInstance(viewInstance, alertTypeUpdated,currentUser)
        } else if (!viDetailMap.defaultView?.toBoolean() && currentUser.getDefaultViewId(alertTypeUpdated) == viewInstance?.id) {
            currentUser.deleteDefaultViewMapping(viewInstance)
        }
    }

    String getUpdatedAlertType(String alertType){
        String alertTypeValue = ""
        if (alertType.contains('Dashboard') || alertType.contains('DrillDown')) {
            alertTypeValue = alertType.split('-')[0].trim()
        } else {
            if(alertType.contains(Constants.DataSource.DATASOURCE_VIGIBASE) || alertType.contains(Constants.DataSource.DATASOURCE_VAERS) || alertType.contains(Constants.DataSource.DATASOURCE_FAERS)){
                alertType = alertType.split('-')[0].trim()
            }
            alertTypeValue = alertType + " - Dashboard"
        }
        return alertTypeValue
    }

    String getUpdatedFilterMap(Map filterMap, Integer fixedColDiff) {
        Map updatedFilterMap = [:]
        filterMap.each { key, value ->
            updatedFilterMap.put((Integer.parseInt(key) + fixedColDiff) as String, value)
        }
        return JsonOutput.toJson(updatedFilterMap)
    }

    String getUpdatedColumnSeq(Map columnSeqMap, Integer fixedColDiff) {
        Map updatedColumnSeqMap = [:]
        columnSeqMap.each { key, value ->
            updatedColumnSeqMap.put((Integer.parseInt(key) + fixedColDiff) as String, value)
        }
        return JsonOutput.toJson(updatedColumnSeqMap)
    }

    Integer getFixedColumnDifference(String alertTypeUpdated, String alertTypeOriginal){
        Integer fixedColumnOriginal = grailsApplication.config.configurations.viewInstances.find{it.alertType == alertTypeOriginal}.fixedColumns as Integer
        Integer fixedColumnUpdated = grailsApplication.config.configurations.viewInstances.find{it.alertType == alertTypeUpdated}.fixedColumns as Integer
        return fixedColumnUpdated - fixedColumnOriginal
    }

    String getUpdatedSortingMap(Map sortingMap, Integer fixedColDiff){
        Map updatedSortingMap = [:]
        sortingMap.each { key, value ->
            if(Integer.parseInt(key) > 0){
                updatedSortingMap.put((Integer.parseInt(key) + fixedColDiff) as String, value)
            } else {
                updatedSortingMap.put(key, value)
            }
        }
        return JsonOutput.toJson(updatedSortingMap)
    }

    void deletePropagatingView(String name, String alertType, User user){
        ViewInstance defaultViewInstance = ViewInstance.findByNameAndAlertTypeAndUser(name, getUpdatedAlertType(alertType), user)
        ViewInstance drillDownViewInstance = ViewInstance.findByNameAndAlertTypeAndUser(name, Constants.AlertConfigType.SINGLE_CASE_ALERT_DRILL_DOWN, user)
        List<ViewInstance> viewInstanceList = []
        viewInstanceList.addAll([defaultViewInstance, drillDownViewInstance])
        viewInstanceList.each { ViewInstance viewInstance ->
            if(viewInstance){
                AdvancedFilter advancedFilter = viewInstance.advancedFilter
                deleteDefaultViewMapping(viewInstance)
                deleteShareWithMapping(viewInstance)
                viewInstance?.delete(flush: true)
                if(advancedFilter)
                deleteLinkedAdvFilter(advancedFilter)
            }
        }
    }

    Boolean isViewPropogationAllowed(String alertType){
        alertType in [Constants.AlertConfigType.SINGLE_CASE_ALERT, Constants.AlertConfigType.AGGREGATE_CASE_ALERT_VIGIBASE, Constants.AlertConfigType.AGGREGATE_CASE_ALERT_FAERS, Constants.AlertConfigType.AGGREGATE_CASE_ALERT_VAERS,Constants.AlertConfigType.AGGREGATE_CASE_ALERT, Constants.AlertConfigType.EVDAS_ALERT,
                      Constants.AlertConfigType.SINGLE_CASE_ALERT_DASHBOARD, Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DASHBOARD, Constants.AlertConfigType.EVDAS_ALERT_DASHBOARD]
    }

    Map populateDynamicSubGroup(Map columnOrderMap, Integer counter, Integer fixedColumns, String alertType) {
        Map<String, Map> subGroupMap = cacheService.getSubGroupMap()
        Map<String,List<String>> allSubGroupMap = cacheService.getAllOtherSubGroupColumnsListMap(Constants.DataSource.PVA)
        Map<String,List<String>> relativeSubGroupMap = cacheService.getRelativeSubGroupColumnsListMap(Constants.DataSource.PVA)
        Map populateMap = [columnOrderMap: columnOrderMap, counter: counter]
        Map labelConfig = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', null, null, null).collectEntries {
            b -> [b.name, b.enabled]
        }
        Map labelConfigDisplay = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', null, null, null).collectEntries {
            b -> [b.name, b.display]
        }

        [Holders.config.subgrouping.ageGroup.name, Holders.config.subgrouping.gender.name].each {
            subGroupMap[it]?.each { id, category ->
                populateMap = populateDynamicColumns(labelConfigDisplay, labelConfig, category, columnOrderMap, counter, fixedColumns)
                columnOrderMap = populateMap.columnOrderMap
                counter = populateMap.counter
            }
        }

        allSubGroupMap?.each{type, category ->
            category?.each { it ->
                populateMap = populateAllDynamicColumns(labelConfigDisplay, labelConfig, it, columnOrderMap, counter, fixedColumns, type)
                columnOrderMap = populateMap.columnOrderMap
                counter = populateMap.counter
            }
        }
        relativeSubGroupMap?.each { type, category ->
            category?.each { it ->
                populateMap = populateRelativeDynamicColumns(labelConfigDisplay, labelConfig, it, columnOrderMap, counter, fixedColumns, type)
                columnOrderMap = populateMap.columnOrderMap
                counter = populateMap.counter
            }
        }

        if (grailsApplication.config.pvsignal.supported.datasource.call().contains(Constants.DataSource.FAERS) && !(alertType in [Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND, Constants.AlertConfigType.AGGREGATE_CASE_ALERT_SMQ_DEMAND])) {
            [Holders.config.subgrouping.faers.ageGroup.name, Holders.config.subgrouping.faers.gender.name].each {
                subGroupMap[it]?.each { id, category ->
                    populateMap = populateDynamicColumnsFaers(labelConfigDisplay, labelConfig, category, columnOrderMap, counter, fixedColumns)
                    columnOrderMap = populateMap.columnOrderMap
                    counter = populateMap.counter
                }
            }
        }
        return populateMap
    }

    Map populateDynamicSubGroupForOnDemand(Map columnOrderMap, Integer counter, Integer fixedColumns, boolean isSafetyDb, boolean isVaers, boolean isVigibase) {
        Map<String, Map> subGroupMap = cacheService.getSubGroupMap()
        Map<String,List<String>> allSubGroupMap = cacheService.getAllOtherSubGroupColumnsListMap(Constants.DataSource.PVA)
        Map<String,List<String>> relativeSubGroupMap = cacheService.getRelativeSubGroupColumnsListMap(Constants.DataSource.PVA)
        Map subGroupColumnInfo = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT').findAll{it.type=="subGroup" && it.enabled== true}.collectEntries {
            b -> [b.name, b.display]
        }
        Map populateMap = [columnOrderMap: columnOrderMap, counter: counter]

        if(!isVaers && !isVigibase){
            if(isSafetyDb) {
                [Holders.config.subgrouping.ageGroup.name, Holders.config.subgrouping.gender.name].each {
                    subGroupMap[it]?.each { id, category ->
                        populateMap = populateDynamicColumnsForOnDemand(category, columnOrderMap, counter, fixedColumns,subGroupColumnInfo)
                        columnOrderMap = populateMap.columnOrderMap
                        counter = populateMap.counter
                    }
                }
                allSubGroupMap?.each{type, category ->
                    category?.each { it ->
                        populateMap = populateAllDynamicColumnsForOnDemand(it, columnOrderMap, counter, fixedColumns, type,subGroupColumnInfo)
                        columnOrderMap = populateMap.columnOrderMap
                        counter = populateMap.counter
                    }
                }
                relativeSubGroupMap?.each { type, category ->
                    category?.each { it ->
                        populateMap = populateRelativeDynamicColumnsForOnDemand(it, columnOrderMap, counter, fixedColumns, type,subGroupColumnInfo)
                        columnOrderMap = populateMap.columnOrderMap
                        counter = populateMap.counter
                    }
                }

            } else {
                [Holders.config.subgrouping.faers.ageGroup.name, Holders.config.subgrouping.faers.gender.name].each {
                    subGroupMap[it]?.each { id, category ->
                        populateMap = populateDynamicColumnsForOnDemand(category, columnOrderMap, counter, fixedColumns,subGroupColumnInfo)
                        columnOrderMap = populateMap.columnOrderMap
                        counter = populateMap.counter
                    }
                }
            }
        }
        return populateMap
    }
    Map populateOnDemandColumns(List adhocColumnList,Map columnOrderMap, Integer counter,Integer fixedColumns){
        adhocColumnList?.each{ Map obj ->
            if(obj.optional) {
                if(obj["name"]=="comment"){
                    columnOrderMap."${counter}" = ["containerView": obj["containerView"], "label": "Comments", "name": obj["name"], "listOrder": obj["containerView"] == 1 ? counter : 9999, "seq": counter + fixedColumns]
                }else{
                    columnOrderMap."${counter}" = ["containerView": obj["containerView"], "label": obj["display"], "name": obj["name"], "listOrder": obj["containerView"] == 1 ? counter : 9999, "seq": counter + fixedColumns]
                }
                counter++
            }
        }
        return [columnOrderMap: columnOrderMap, counter: counter]
    }
    Map populateJaderColumns(List columnList,Map columnOrderMap, Integer counter,Integer fixedColumns){
        columnList?.each{ Map obj ->
            if(obj.optional) {
                columnOrderMap."${counter}" = ["containerView": obj["containerView"], "label": obj["display"], "name": obj["name"], "listOrder": obj["containerView"] == 1 ? counter : 9999, "seq": counter + fixedColumns]
                counter++
            }
        }
        return [columnOrderMap: columnOrderMap, counter: counter]
    }


    Map populateDynamicColumns(Map labelConfigDisplay, Map labelConfig, String category, Map columnOrderMap, Integer counter, Integer fixedColumns) {

        if (labelConfig.get("ebgm" + category)) {
            columnOrderMap."${counter}" = ["containerView": 2, "label": labelConfigDisplay.get("ebgm" + category), "name": "ebgm" + category, "listOrder": 3 == 1 ? counter : 9999, "seq": counter + fixedColumns]
            counter++
        }


        if (labelConfig.get("eb05" + category)) {
            columnOrderMap."${counter}" = ["containerView": 2, "label": labelConfigDisplay.get("eb05" + category), "name": "eb05" + category, "listOrder": 1 == 1 ? counter : 9999, "seq": counter + fixedColumns]
            counter++
        }

        if (labelConfig.get("eb95" + category)) {
            columnOrderMap."${counter}" = ["containerView": 2, "label": labelConfigDisplay.get("eb95" + category), "name": "eb95" + category, "listOrder": 3 == 1 ? counter : 9999, "seq": counter + fixedColumns]
            counter++
        }
        return [columnOrderMap: columnOrderMap, counter: counter]
    }

    Map populateAllDynamicColumns(Map labelConfigDisplay, Map labelConfig, String category, Map columnOrderMap, Integer counter, Integer fixedColumns, String type) {

        if (labelConfig.get(cacheService.toCamelCase(type) + category)) {
            columnOrderMap."${counter}" = ["containerView": 2, "label": labelConfigDisplay.get(cacheService.toCamelCase(type) + category), "name": cacheService.toCamelCase(type) + category, "listOrder": 3 == 1 ? counter : 9999, "seq": counter + fixedColumns]
            counter++
        }

        return [columnOrderMap: columnOrderMap, counter: counter]
    }

    Map populateRelativeDynamicColumns(Map labelConfigDisplay, Map labelConfig, String category, Map columnOrderMap, Integer counter, Integer fixedColumns, String type) {
        if (labelConfig.get(cacheService.toCamelCase(type) + "el" + category)) {
            columnOrderMap."${counter}" = ["containerView": 2, "label": labelConfigDisplay.get(cacheService.toCamelCase(type) + "el" + category), "name": cacheService.toCamelCase(type) + "el" + category, "listOrder": 3 == 1 ? counter : 9999, "seq": counter + fixedColumns]
            counter++
        }

        return [columnOrderMap: columnOrderMap, counter: counter]
    }

    Map populateDynamicColumnsForOnDemand(String category, Map columnOrderMap, Integer counter, Integer fixedColumns, Map subGroupColumnInfo) {
        String ebgmCategory =  "ebgm" + category
        String eb05Category =  "eb05" + category
        String eb95Category =  "eb95" + category
        if(ebgmCategory in subGroupColumnInfo?.keySet()) {
            columnOrderMap."${counter}" = ["containerView": 3, "label": subGroupColumnInfo.get(ebgmCategory), "name": ebgmCategory, "listOrder": 3 == 1 ? counter : 9999, "seq": counter + fixedColumns]
            counter++
        }
        if(eb05Category in subGroupColumnInfo?.keySet()) {
            columnOrderMap."${counter}" = ["containerView": 3, "label": subGroupColumnInfo.get(eb05Category), "name": eb05Category, "listOrder": 1 == 1 ? counter : 9999, "seq": counter + fixedColumns]
            counter++
        }
        if(eb95Category in subGroupColumnInfo?.keySet()) {
            columnOrderMap."${counter}" = ["containerView": 3, "label": subGroupColumnInfo.get(eb95Category), "name": eb95Category, "listOrder": 3 == 1 ? counter : 9999, "seq": counter + fixedColumns]
            counter++
        }

        return [columnOrderMap : columnOrderMap, counter : counter]
    }

    Map populateAllDynamicColumnsForOnDemand(String category, Map columnOrderMap, Integer counter, Integer fixedColumns,String type,Map subGroupColumnInfo) {
        String subGrpKey = cacheService.toCamelCase(type) + category
        if(subGrpKey in subGroupColumnInfo?.keySet()) {
            columnOrderMap."${counter}" = ["containerView": 3, "label":subGroupColumnInfo.get(subGrpKey), "name": subGrpKey, "listOrder": 3 == 1 ? counter : 9999, "seq": counter + fixedColumns]
            counter++
        }
        return [columnOrderMap : columnOrderMap, counter : counter]
    }
    Map populateRelativeDynamicColumnsForOnDemand(String category, Map columnOrderMap, Integer counter, Integer fixedColumns,String type,Map subGroupColumnInfo) {
        String subGrpKey = cacheService.toCamelCase(type) + "el" + category
        if(subGrpKey in subGroupColumnInfo?.keySet()) {
            columnOrderMap."${counter}" = ["containerView": 3, "label": subGroupColumnInfo.get(subGrpKey) , "name": subGrpKey, "listOrder": 3 == 1 ? counter : 9999, "seq": counter + fixedColumns]
            counter++
        }
        return [columnOrderMap : columnOrderMap, counter : counter]
    }

    Map populateDynamicColumnsFaers(Map labelConfigDisplay, Map labelConfig, String category, Map columnOrderMap, Integer counter, Integer fixedColumns) {
        if (labelConfig.get("ebgm" + category + "Faers")) {
            columnOrderMap."${counter}" = ["containerView": 3, "label": labelConfigDisplay.get("ebgm" + category + "Faers"), "name": "ebgm" + category + "Faers", "listOrder": 9999, "seq": counter + fixedColumns]
            counter++
        }
        if (labelConfig.get("eb05" + category + "Faers")) {
            columnOrderMap."${counter}" = ["containerView": 3, "label": labelConfigDisplay.get("eb05" + category + "Faers"), "name": "eb05" + category + "Faers", "listOrder": 9999, "seq": counter + fixedColumns]
            counter++
        }
        if (labelConfig.get("eb95" + category + "Faers")) {
            columnOrderMap."${counter}" = ["containerView": 3, "label": labelConfigDisplay.get("eb95" + category + "Faers"), "name": "eb95" + category + "Faers", "listOrder": 9999, "seq": counter + fixedColumns]
            counter++
        }
        return [columnOrderMap: columnOrderMap, counter: counter]
    }

    Map populateDynamicColumnsPrev(Map labelConfigDisplay, Map labelConfig, String category, Integer idx, Integer idxLabel, Map columnOrderMap, Integer counter, Integer fixedColumns) {
        if (labelConfig.get("ebgm" + category)) {
            columnOrderMap."${counter}" = ["containerView": 2, "label": "Prev Period " + idxLabel + " " + labelConfigDisplay.get("ebgm" + category), "name": "exe" + idx + "ebgm" + category, "listOrder": 9999, "seq": counter + fixedColumns]
            counter++
        }

        if (labelConfig.get("eb05" + category)) {
            columnOrderMap."${counter}" = ["containerView": 2, "label": "Prev Period " + idxLabel + " " + labelConfigDisplay.get("eb05" + category), "name": "exe" + idx + "eb05" + category, "listOrder": 9999, "seq": counter + fixedColumns]
            counter++
        }
        if (labelConfig.get("eb95" + category)) {
            columnOrderMap."${counter}" = ["containerView": 2, "label": "Prev Period " + idxLabel + " " + labelConfigDisplay.get("eb95" + category), "name": "exe" + idx + "eb95" + category, "listOrder": 9999, "seq": counter + fixedColumns]
            counter++
        }
        return [columnOrderMap: columnOrderMap, counter: counter]

    }

    Map populateAllDynamicColumnsPrev(Map labelConfigDisplay, Map labelConfig, String category, Integer idx, Integer idxLabel, Map columnOrderMap, Integer counter, Integer fixedColumns, String type) {

        if (labelConfig.get(cacheService.toCamelCase(type) + category)) {
            columnOrderMap."${counter}" = ["containerView": 2, "label": "Prev Period " + idxLabel + " " + labelConfigDisplay.get(cacheService.toCamelCase(type) + category), "name": "exe" + idx + cacheService.toCamelCase(type) + category, "listOrder": 9999, "seq": counter + fixedColumns]
            counter++
        }
        return [columnOrderMap: columnOrderMap, counter: counter]
    }

    Map populateRelativeDynamicColumnsPrev(Map labelConfigDisplay, Map labelConfig, String category, Integer idx, Integer idxLabel, Map columnOrderMap, Integer counter, Integer fixedColumns, String type) {
        if (labelConfig.get(cacheService.toCamelCase(type) + "el" + category)) {
            columnOrderMap."${counter}" = ["containerView": 2, "label": "Prev Period " + idxLabel + " " + labelConfigDisplay.get(cacheService.toCamelCase(type) + "el" + category), "name": "exe" + idx + cacheService.toCamelCase(type) + "el" + category, "listOrder": 9999, "seq": counter + fixedColumns]
            counter++
        }
        return [columnOrderMap: columnOrderMap, counter: counter]
    }

    Map populateDynamicColumnsPrevFaers(Map labelConfigDisplay, Map labelConfig, String category, Integer idx, Integer idxLabel, Map columnOrderMap, Integer counter, Integer fixedColumns) {
        if (labelConfig.get("ebgm" + category + "Faers")) {
            columnOrderMap."${counter}" = ["containerView": 3, "label": "Prev Period " + idxLabel + " " + labelConfigDisplay.get("ebgm" + category + "Faers"), "name": "exe" + idx + "ebgm" + category + "Faers", "listOrder": 9999, "seq": counter + fixedColumns]
            counter++
        }
        if (labelConfig.get("eb05" + category + "Faers")) {
            columnOrderMap."${counter}" = ["containerView": 3, "label": "Prev Period " + idxLabel + " " + labelConfigDisplay.get("eb05" + category + "Faers"), "name": "exe" + idx + "eb05" + category + "Faers", "listOrder": 9999, "seq": counter + fixedColumns]
            counter++
        }

        if (labelConfig.get("eb95" + category + "Faers")) {

            columnOrderMap."${counter}" = ["containerView": 3, "label": "Prev Period " + idxLabel + " " + labelConfigDisplay.get("eb95" + category + "Faers"), "name": "exe" + idx + "eb95" + category + "Faers", "listOrder": 9999, "seq": counter + fixedColumns]
            counter++
        }

        return [columnOrderMap: columnOrderMap, counter: counter]

    }
    void updateDefaultViewInstance(ViewInstance viewInstance, String alertType, User currentUser) {
        if (viewInstance && alertType) {
            List<User> usersList = [currentUser]
            if (currentUser.isAdmin()) {
                usersList.addAll(viewInstance.shareWithUser)
                viewInstance.shareWithGroups?.each { userGroup ->
                    usersList.addAll(UserGroupMapping.findAllByGroup(userGroup).collect { it.user })
                }
            }
            usersList = usersList.flatten()?.unique()
            updateDefaultViewInstancesForUserList(usersList, alertType, viewInstance)
        }
    }

    void updateDefaultViewInstancesForUserList(List<User> userList, String alertType, ViewInstance defaultViewInstance) {
        DefaultViewMapping defaultViewMapping = null
        userList.each { User user ->
            defaultViewMapping = DefaultViewMapping.findByAlertTypeAndUser(alertType, user)
            if (!defaultViewMapping) {
                defaultViewMapping = new DefaultViewMapping(user, alertType, defaultViewInstance)
            }
            defaultViewMapping.defaultViewInstance = defaultViewInstance

            defaultViewMapping.save()
        }
    }


    void deleteShareWithMapping(ViewInstance viewInstance){
        userService.bindSharedWithConfiguration(viewInstance, null, true, true)
    }

    void deleteDefaultViewMapping(ViewInstance viewInstance){
        if(viewInstance){
            List<DefaultViewMapping> defaultViewMappingList = DefaultViewMapping.findAllByDefaultViewInstance(viewInstance)
            defaultViewMappingList.each{
                it.delete()
            }
        }
    }

    ViewInstance updateViewInstance(ViewInstance viewInstance, Map params, List viewSharedWith, Boolean isSharingViewAllowed = true, Boolean isDrillDownView =false, Boolean isRestCall = false){
        if (!isDrillDownView) {
            viewInstance.skipAudit = false
        }
        viewInstance.name = params.name.trim()?.replaceAll("\\s{2,}", " ") //Extra Space is removed from name field
        User currentUser = userService.getUser()
        if (params.name.trim()?.endsWith(Constants.Commons.SHARED) && (viewInstance.user.id != currentUser.id) && viewInstance.isViewShared()) {
            viewInstance.name = params.name.trim()?.substring(0, params.name.length() - 3)
        }
        viewInstance.filters = isRestCall ? JsonOutput.toJson(params.filterMap) : params.filterMap
        viewInstance.alertType = viewInstance?.alertType ?: params.alertType
        viewInstance.sorting = isRestCall ? JsonOutput.toJson(params.sorting) : params.sorting
        viewInstance.advancedFilter = params.advancedFilter ? AdvancedFilter.findById(params.advancedFilter) : null
        Boolean isFilterSharingAllowed = isViewFilterSharingAllowed(viewInstance.advancedFilter, viewSharedWith, Constants.FilterType.ADVANCED_FILTER)

        viewInstance.columnSeq = viewInstance.tempColumnSeq ?: viewInstance.columnSeq
        viewInstance.tempColumnSeq = null
        viewInstance.save(flush:true)
        if(updateAdvancedFilterAllowed(viewInstance, viewSharedWith) && isFilterSharingAllowed){
            userService.bindSharedWithConfiguration(viewInstance.advancedFilter, viewSharedWith, true, true)
        }
        if(isSharingViewAllowed){
            userService.bindSharedWithConfiguration(viewInstance, viewSharedWith, true, true)
        }


        CRUDService.updateWithAuditLog(viewInstance)

        return viewInstance
    }

    Boolean isViewFilterSharingAllowed(def configInstance, def sharedWith,  String type = 'View') {
        def domain = type.equalsIgnoreCase('View') ? ViewInstance : AdvancedFilter
        if(sharedWith){
            def configInstanceDuplicate = null
            if (configInstance?.id) {
                configInstanceDuplicate = domain.findByAlertTypeAndNameAndIdNotEqual(configInstance?.alertType, configInstance?.name, configInstance?.id)
            } else {
                configInstanceDuplicate = domain.findByAlertTypeAndName(configInstance?.alertType, configInstance?.name)
            }
            if (configInstanceDuplicate) {
                List groupListCurrent = []
                List userListCurrent = []
                if(sharedWith instanceof String){
                    sharedWith = [sharedWith]
                }
                sharedWith.each { String shared ->
                    if (shared.startsWith(Constants.USER_GROUP_TOKEN)) {
                        groupListCurrent.add(Long.valueOf(shared.replaceAll(Constants.USER_GROUP_TOKEN, '')))
                    } else if (shared.startsWith(Constants.USER_TOKEN)) {
                        userListCurrent.add(Long.valueOf(shared.replaceAll(Constants.USER_TOKEN, '')))
                    }
                }
                List groupListDuplicate = configInstanceDuplicate.shareWithGroup*.id as List
                List userListDuplicate = configInstanceDuplicate.shareWithUser*.id as List
                List userIdsListCurrent = userService.getUserIdsFromGroups(groupListCurrent)
                List userIdsListDuplicate = userService.getUserIdsFromGroups(groupListDuplicate)
                userIdsListCurrent.addAll(userListCurrent)
                userIdsListDuplicate.addAll(userListDuplicate)
                userIdsListCurrent = userIdsListCurrent.flatten()
                userIdsListDuplicate = userIdsListDuplicate.flatten()
                return  userIdsListCurrent.disjoint(userIdsListDuplicate)
            }
        }
        return true
    }

    Boolean updateAdvancedFilterAllowed(ViewInstance viewInstance, def viewSharedWith){
        viewInstance.advancedFilter && (viewSharedWith || !(viewSharedWith && ViewInstance.findByNameAndAlertTypeAndIdNotEqual(viewInstance?.name, viewInstance?.alertType, viewInstance?.id)))
    }

    void deleteLinkedAdvFilter(AdvancedFilter advancedFilter) {
        Integer linkedViewsCount = advancedFilter ? ViewInstance.countByAdvancedFilter(advancedFilter) : Constants.Commons.UNDEFINED_NUM
        if (!linkedViewsCount) {
            advancedFilterService.deleteAdvancedFilter(advancedFilter)
        }
    }

    void handleDefaultViewMappings(List userIdsDuplicate, List groupIdsDuplicate, List userIdsCurrent, List groupIdsCurrent, ViewInstance viewInstance){
        List groupIdsRemaining = groupIdsDuplicate.minus(groupIdsCurrent)
        List userIdsRemaining = userIdsDuplicate.minus(userIdsCurrent)
        userIdsRemaining.addAll(userService.getUserIdsFromGroups(groupIdsRemaining))
        userIdsRemaining = userIdsRemaining?.flatten()
        if(userIdsRemaining){
            User.getAll(userIdsRemaining).each { User user ->
                user.deleteDefaultViewMapping(viewInstance)
            }
        }
    }

    void updateOrder(Map params) {
        List viewsList = JSON.parse(params.updatedViewsOrder)
        User user = userService.getUser()
        viewsList.each{
            ViewInstance viewInstance = ViewInstance.get(it.id as Long)
            UserViewOrder userViewOrder = UserViewOrder.findByUserAndViewInstance(user , viewInstance)
            if(!userViewOrder) {
                userViewOrder = new UserViewOrder()
                userViewOrder.user = user
                userViewOrder.viewInstance = viewInstance
            }
            userViewOrder.viewOrder = it.order as Integer
            userViewOrder.save(flush:true)
        }
    }

    LinkedList<Map> fetchOrderedViewOrder(List<Map> viewsList, User currentUser, Long defaultViewId, String alertType) {
        LinkedList<Map>  userViewOrderList = UserViewOrder.createCriteria().list{
            resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
            projections {
                property("id", "id")
                property("viewInstance.id", "viewInstanceId")
                property("user.id", "userId")
                property("viewOrder", "viewOrder")
            }
            'user'{
                eq("id" , currentUser.id)
            }
            or {
                viewsList.collate(1000).each {
                    'in'("viewInstance.id", it.id)
                }
            }
        } as LinkedList<Map>

        userViewOrderList.sort { it.viewOrder }
        ViewInstance systemDefault = ViewInstance.findByAlertTypeAndUserIsNull(alertType)
        Map userViewOrderSystem = userViewOrderList.find { it.viewInstanceId == systemDefault?.id }
        if (!userViewOrderSystem) {
            userViewOrderSystem = [id: null, viewInstanceId: systemDefault?.id, userId: currentUser.id]
            userViewOrderList.add(userViewOrderSystem)
        }
        userViewOrderList.remove(userViewOrderSystem)
        if (defaultViewId) {
            Map userViewOrderDefault = userViewOrderList.find { it.viewInstanceId == defaultViewId }
            if (!userViewOrderDefault) {
                userViewOrderDefault = [id: null, viewInstanceId: defaultViewId, userId: currentUser.id]
                userViewOrderList.add(userViewOrderDefault)
            }
            userViewOrderList.remove(userViewOrderDefault)
            userViewOrderList.addFirst(userViewOrderDefault)
        } else {
            userViewOrderList.addFirst(userViewOrderSystem)
        }

        viewsList.findAll{it.userId}.each { viewInstance ->
            Map userViewOrder = userViewOrderList.find { it.viewInstanceId == viewInstance.id }
            if (!userViewOrder) {
                userViewOrder = [id: null, viewInstanceId: viewInstance.id, userId: currentUser.id]
                userViewOrderList.addLast(userViewOrder)
            }
        }
        if (defaultViewId) {
            userViewOrderList.addLast(userViewOrderSystem)
        }
        userViewOrderList
    }

    @Transactional
    void saveOrUpdateUserViewOrder(List<Map> userViewOrderList){
        Sql sql = new Sql(dataSource)
        insertViewOrderForUser(userViewOrderList.findAll {!it.id}, sql)
        updateViewOrderForUser(userViewOrderList.findAll {it.id}, sql)
    }

    void updateViewOrderForUser(List<Map> userViewOrderList, Sql sql) {
        if (userViewOrderList) {
            sql.withBatch(100, "UPDATE user_view_order SET view_order = :viewOrder WHERE ID = :id", { preparedStatement ->
                userViewOrderList.each {
                    preparedStatement.addBatch(viewOrder: it.viewOrder, id: it.id)
                }
            })
        }
    }

    void insertViewOrderForUser(List<Map> userViewOrderList, Sql sql) {
        if (userViewOrderList) {
            log.info("User View Order Insertion Starts")
            sql.withBatch(100, "insert into user_view_order(id, version, user_id, view_instance_id, view_order) values (:val0, 0, :val1, :val2, :val3)", { preparedStatement ->
                userViewOrderList.each {
                    Long id
                    sql.eachRow("SELECT nextval('hibernate_sequence')", []) { row ->
                        id = row[0]
                    }
                    preparedStatement.addBatch(val0: id, val1: it.userId, val2: it.viewInstanceId, val3: it.viewOrder)
                }
            })
            log.info("User View Order Insertion Completed")
        }
    }

    void savePinnedIcon(String fieldName, Boolean isPinned) {
        User currentUser = userService.getUser()
        ViewInstance viewInstance = ViewInstance.findByUserAndAlertType(currentUser,Constants.AlertConfigType.SIGNAL_MANAGEMENT);
        if(!viewInstance.iconSeq) {
            viewInstance.iconSeq = '{"#ic-create-signal":false,"#ic-toggle-column-filters":false,"#ic-exportTypes":false,"#ic-configureValidatedSignalFields":false, "#ic-report":false}'
        }
        Map<String,Boolean> iconSeqMap = new JsonSlurper().parseText(viewInstance.iconSeq);
        def oldEnablePinList= iconSeqMap.findAll {
            it.value==true
        }.collect {it.key}

        iconSeqMap.put(fieldName,isPinned) //push the latest pin configs
        viewInstance.iconSeq = iconSeqMap as JSON
       def enablePinList= iconSeqMap.findAll {
            it.value==true
        }.collect {it.key}
        viewInstance.save()
        createAuditIfChanged(oldEnablePinList,enablePinList,currentUser)
        currentUser.save(flush:true)
    }

    Boolean createAuditIfChanged(def oldEnablePinList,def enablePinList,User currentUser){
        //Adding custom audit code to handle entity name as pin-unpin fields not user mangement

        def labelMapping = [
                "#ic-create-signal"                 : "Create Signal",
                "#ic-toggle-column-filters"         : "Filters",
                "#ic-exportTypes"                   : "Signal Tracker Export",
                "#ic-configureValidatedSignalFields": "Field Selection"
        ]

        def oldTempList=[]
        oldEnablePinList.each {
            oldTempList.add(labelMapping.get(it))
        }

        def tempList=[]
        enablePinList.each {
            tempList.add(labelMapping.get(it))
        }
        currentUser.signalPinConfigs=tempList?.toString()
        signalAuditLogService.createAuditLog([
                entityName : "Signal Pin-Unpin Fields",
                moduleName : "Signal Pin-Unpin Fields",
                category   : AuditTrail.Category.UPDATE.toString(),
                entityValue: currentUser.getValue(),
                description: "Edited Pin-Unpin Fields"
        ] as Map, [[oldValue: oldTempList, newValue: tempList, propertyName: "Signal Pin-Unpin Fields"]])
    }

    void savePinnedConfigAlerts(String fieldName , Boolean isPinned, User currentUser) {
        UserPinConfiguration userPinConfiguration
        if(isPinned) {
            userPinConfiguration = new UserPinConfiguration()
            userPinConfiguration.user = currentUser
            userPinConfiguration.isPinned = true
            userPinConfiguration.fieldCode = fieldName
            userPinConfiguration.save(flush:true)
        } else{
            userPinConfiguration = UserPinConfiguration.findByUserAndFieldCode(currentUser , fieldName)
            userPinConfiguration.delete(flush:true)
        }
        String oldValue = currentUser.userPinConfigs
        currentUser.userPinConfigs=UserPinConfiguration.findAllByUser(currentUser).sort{it.fieldCode}.unique{it.fieldCode}
        String newValue = currentUser.userPinConfigs
        if(oldValue != newValue){
            signalAuditLogService.createAuditLog([
                    entityName : "Alert Pin-Unpin Fields",
                    moduleName : "Alert Pin-Unpin Fields",
                    category   : AuditTrail.Category.UPDATE.toString(),
                    entityValue: currentUser.getValue(),
                    description: "Edited Pin-Unpin Fields"
            ] as Map, [[oldValue: oldValue, newValue: newValue, propertyName: "User Pin Configs"]])
        }
        currentUser.save(flush:true)
    }

    List fetchPinnedConfigs(User currentUser) {
        List <String> fieldNames = UserPinConfiguration.findAllByUserAndIsPinned(currentUser , true).collect{
            it.fieldCode
        }
        return fieldNames
    }

    void saveTempView(User user ,  String caseIds) {
        ClipboardCases instance = ClipboardCases.findByUser(user)
        if(!instance) {
           instance = new ClipboardCases(name: "Temporary View", user: user, caseIds: caseIds, isFirstUse: true, isUpdated:true, isDeleted:false)
        } else{
            instance.caseIds = caseIds
            instance.isUpdated = true
            instance.isDeleted = false
            instance.isFirstUse = true
        }
        instance.save(failOnError: true)
    }

    void checkDataMiningViews(){

        Map pvaMiningVariables = cacheService.getMiningVariables(Constants.DataSource.PVA)
        pvaMiningVariables.each{key,value ->
            try {
                grailsApplication.config.configurations.viewInstance.adhoc.dataMining.pva.each { Map viDetailMap ->
                    Map columnOrderMap = addOrUpdateColumnMap(viDetailMap)
                    updateViewInstanceObjects(columnOrderMap, viDetailMap, key as Long, value)
                }
            }
            catch (Exception ex) {
                println("######## Some error occure while updating the View Instances  ##########")
                ex.printStackTrace()
            }

        }
        Map faersMiningVariable = cacheService.getMiningVariables(Constants.DataSource.FAERS)
        faersMiningVariable.each{key,value ->
            try {
                grailsApplication.config.configurations.viewInstance.adhoc.dataMining.faers.each { Map viDetailMap ->
                    Map columnOrderMap = addOrUpdateColumnMap(viDetailMap)
                    updateViewInstanceObjects(columnOrderMap, viDetailMap, key as Long, value)
                }
            }
            catch (Exception ex) {
                println("######## Some error occure while updating the View Instances  ##########")
                ex.printStackTrace()
            }
        }
    }

    boolean isLabelChangeRequired(String datasource){
        boolean dssCondition = false
        boolean dataSourceCondition = false
        List selectedDataSourceList = datasource?.split(",")
        if(datasource){
            dssCondition = !(selectedDataSourceList?.contains(Constants.DataSource.PVA) && grailsApplication.config.dss.enable.autoProposed && grailsApplication.config.statistics.enable.dss)
            dataSourceCondition = (datasource in [Constants.DataSource.FAERS, Constants.DataSource.VAERS, Constants.DataSource.VIGIBASE] || !selectedDataSourceList?.contains(Constants.DataSource.PVA))
        }
        dssCondition || dataSourceCondition
    }


    List detectChangesMade(ViewInstance theInstance, AuditLogCategoryEnum auditLogCategoryEnum) {
        List changesMade = signalAuditLogService.detectChangesMade(theInstance, auditLogCategoryEnum)
        List fieldsToIgnore = ['version', 'defaultValue']
        if (AuditLogCategoryEnum.MODIFIED == auditLogCategoryEnum && theInstance.shareWithUser.dirty) {
            String originalValue =(theInstance.shareWithUser?.getStoredSnapshot()?.collect { it.value }?.join(", ") ?: Constants.AuditLog.EMPTY_VALUE)
            String newValue = (theInstance.shareWithUser?.collect { it.name }?.join(", ") ?: Constants.AuditLog.EMPTY_VALUE)
            if(newValue != originalValue){
                changesMade << [
                        fieldName    : "shareWithUser",
                        originalValue: originalValue,
                        newValue     : newValue
                ]
            }
        } else if (AuditLogCategoryEnum.CREATED == auditLogCategoryEnum && theInstance.shareWithUser?.size() > 0) {
            Map shareWithUserMap = changesMade.find { it.fieldName == 'shareWithUser' }
            if (shareWithUserMap) {
                changesMade -= shareWithUserMap
            }
            changesMade << [
                    fieldName    : "shareWithUser",
                    originalValue: Constants.AuditLog.EMPTY_VALUE,
                    newValue     : (theInstance.shareWithUser?.collect { it.name }?.join(", ") ?: Constants.AuditLog.EMPTY_VALUE)
            ]
        }
        if (AuditLogCategoryEnum.MODIFIED == auditLogCategoryEnum && theInstance.shareWithGroup.dirty) {
            String originalValue = (theInstance.shareWithGroup?.getStoredSnapshot()?.collect { it.name }?.join(", ") ?: Constants.AuditLog.EMPTY_VALUE)
            String newValue    = (theInstance.shareWithGroup?.collect { it.name }?.join(", ") ?: Constants.AuditLog.EMPTY_VALUE)
            if(newValue != originalValue){
                changesMade << [
                        fieldName    : "shareWithGroup",
                        originalValue: originalValue,
                        newValue     : newValue
                ]
            }
        } else if (AuditLogCategoryEnum.CREATED == auditLogCategoryEnum && theInstance.shareWithUser?.size() > 0) {
            Map shareWithGroupMap = changesMade.find { it.fieldName == 'shareWithGroup' }
            if (shareWithGroupMap) {
                changesMade -= shareWithGroupMap
            }
            changesMade << [
                    fieldName    : "shareWithGroup",
                    originalValue: Constants.AuditLog.EMPTY_VALUE,
                    newValue     : (theInstance.shareWithGroup?.collect { it.name }?.join(", ") ?: Constants.AuditLog.EMPTY_VALUE)
            ]
        }
        if (changesMade) {
            Map colSeqChangeMap = changesMade.find { it.fieldName == 'columnSeq' }
            if (colSeqChangeMap) {
                changesMade -= colSeqChangeMap
                colSeqChangeMap.newValue = generatePrimaryFields(colSeqChangeMap.newValue, theInstance.alertType)
                colSeqChangeMap.originalValue = generatePrimaryFields(colSeqChangeMap.originalValue,  theInstance.alertType)
                changesMade += colSeqChangeMap
            }
            changesMade -= changesMade.find { it.fieldName == 'tempColumnSeq' }

            Map filtersMap = changesMade.find { it.fieldName == 'filters' }
            if (filtersMap) {
                Map fixedCols = fixedColumns(theInstance)
                changesMade -= filtersMap
                filtersMap.newValue = filtersMap.newValue?generateFiltersMap(filtersMap.newValue, theInstance.columnSeq, fixedCols):Constants.AuditLog.EMPTY_VALUE
                filtersMap.originalValue = filtersMap.originalValue?generateFiltersMap(filtersMap.originalValue, theInstance.columnSeq, fixedCols):Constants.AuditLog.EMPTY_VALUE
                changesMade += filtersMap
            }
            Map sortingMap = changesMade.find { it.fieldName == 'sorting' }
            if (sortingMap) {
                Map fixedCols = fixedColumns(theInstance)
                changesMade -= sortingMap
                sortingMap.newValue = sortingMap.newValue?generateSortingMap(sortingMap.newValue, theInstance.columnSeq, fixedCols):Constants.AuditLog.EMPTY_VALUE
                sortingMap.originalValue = sortingMap.originalValue?generateSortingMap(sortingMap.originalValue, theInstance.columnSeq, fixedCols):Constants.AuditLog.EMPTY_VALUE
                changesMade += sortingMap
            }
        }
        if(AuditLogCategoryEnum.CREATED == auditLogCategoryEnum && theInstance.customAuditProperties?.get("defaultView")){
            // new view is created and added as default
            changesMade << [
                    fieldName    : "defaultView",
                    originalValue: Constants.AuditLog.EMPTY_VALUE,
                    newValue     : "Yes"
            ]
        }else if (AuditLogCategoryEnum.MODIFIED == auditLogCategoryEnum){
            User currentUser = userService.getUserFromCacheByUsername(userService.getCurrentUserName())
            Long defViewId = currentUser.getDefaultViewId(theInstance.alertType)
            if(defViewId != theInstance.id && theInstance.customAuditProperties?.get("defaultView")){
                //existing view is converted to default
                changesMade << [
                        fieldName    : "defaultView",
                        originalValue: "No",
                        newValue     : "Yes"
                ]
            }else if (defViewId == theInstance.id && !theInstance.customAuditProperties?.get("defaultView")){
                //current view removed from default
                changesMade << [
                        fieldName    : "defaultView",
                        originalValue: "Yes",
                        newValue     : "No"
                ]
            }

        }
        if(AuditLogCategoryEnum.DELETED == auditLogCategoryEnum && changesMade){
            changesMade.each{
                it.newValue = Constants.AuditLog.EMPTY_VALUE
            }
        }
        changesMade.removeAll({ it.fieldName in fieldsToIgnore })
        changesMade
    }

    Map fixedColumns(ViewInstance view) {
        Map result = [:]
        boolean isPriorityEnabled = grailsApplication.config.alert.priority.enable
        if(view.alertType.contains("Aggregate Case Alert - SMQ on Demand")) {
            int prodColumn = isPriorityEnabled ? 2 : 1
            result = [((prodColumn+1).toString()) : "SMQ/Event Group"]
        } else if(view.alertType.contains("Aggregate Case Alert on Demand")) {
            int prodColumn = isPriorityEnabled ? 2 : 1
            result = [((prodColumn+1).toString()) : "SOC", ((prodColumn+2).toString()) : "PT"]
        } else if(view.alertType.contains("Aggregate Case Alert - SMQ")) {
            int prodColumn = isPriorityEnabled ? 4 : 3
            result = [((prodColumn+1).toString()) : "SMQ/Event Group"]
        } else if(view.alertType.contains("Aggregate Case Alert - Dashboard")) {
            int prodColumn = isPriorityEnabled ? 5 : 4
            result = [((prodColumn+1).toString()) : "SOC", ((prodColumn+2).toString()) : "PT"]
        } else if(view.alertType.contains("Aggregate Case Alert")) {
            int prodColumn = isPriorityEnabled ? 4 : 3
            result = [((prodColumn+1).toString()) : "SOC", ((prodColumn+2).toString()) : "PT"]

        } else if(view.alertType.contains("Single Case Alert on Demand")) {
            int prodColumn = isPriorityEnabled ? 2 : 1
            result = [((prodColumn).toString()) : "Case Number"]
        } else if(view.alertType.contains("Single Case Alert - Dashboard")) {
            int prodColumn = isPriorityEnabled ? 5 : 4
            result = [((prodColumn).toString()) : "Case Number"]
        } else if(view.alertType.contains("Single Case Alert")) {
            int prodColumn = isPriorityEnabled ? 4 : 3
            result = [((prodColumn).toString()) : "Case Number"]
        } else if(view.alertType.contains("EVDAS Alert on Demand")) {
            int prodColumn = isPriorityEnabled ? 1 : 0
            result = [((prodColumn).toString()) : "Substance", ((prodColumn+1).toString()) : "SOC",
                      ((prodColumn+2).toString()) : "PT"]
        } else if(view.alertType.contains("EVDAS Alert - Dashboard")) {
            int prodColumn = isPriorityEnabled ? 5 : 4
            result = [((prodColumn).toString()) : "Substance", ((prodColumn+1).toString()) : "SOC",
                      ((prodColumn+2).toString()) : "PT"]
        } else if(view.alertType.contains("EVDAS Alert")) {
            int prodColumn = isPriorityEnabled ? 4 : 3
            result = [((prodColumn).toString()) : "Substance", ((prodColumn+1).toString()) : "SOC",
                      ((prodColumn+2).toString()) : "PT"]
        }
        return result
    }


    def generatePrimaryFields(def columnSequenceJson, String alertType) {
        JsonSlurper jsonSlurper = new JsonSlurper()
        try {
            if (columnSequenceJson) {
                Map colSeqMap = jsonSlurper.parseText(columnSequenceJson)
                if(alertType in ["Single Case Alert","Single Case Alert - Adhoc DrillDown","Single Case Alert - Cumulative","Single Case Alert - Dashboard","Single Case Alert - DrillDown","Single Case Alert on Demand"]){
                    Map columnLabelMap = [:]
                    Map rptToSignalFieldMap = Holders.config.icrFields.rptMap
                    Map rptToUiLabelMap = cacheService.getRptToUiLabelMapForSafety() as Map
                    rptToSignalFieldMap.each {
                        if (rptToUiLabelMap.get(it.value) != null) {
                            columnLabelMap.put(it.key, rptToUiLabelMap.get(it.value))
                        }
                    }
                    return colSeqMap.values().findAll { it.containerView == 1 }.collect { columnLabelMap.getOrDefault(it.name, it.label)}
                }
                return colSeqMap.values().findAll { it.containerView == 1 }.collect { it.label }
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        }
    }

    def generateFiltersMap(def filtersMapJson, columnSequenceJson, fixedCols) {
        JsonSlurper jsonSlurper = new JsonSlurper()
        Map filterMap = new HashMap()
        try {
            if (filtersMapJson && columnSequenceJson) {
                Map colSeqMap = new HashMap()
                Map columnSequence = jsonSlurper.parseText(columnSequenceJson)
                columnSequence?.values().findAll { it.containerView == 1 }.each {
                    colSeqMap.put(it.seq.toString(), it.label)
                }
                Map filters = jsonSlurper.parseText(filtersMapJson)
                filters.each {
                    if(colSeqMap.get(it.key.toString())) {
                        filterMap.put(colSeqMap.get(it.key.toString()), it.value)
                    } else {
                        filterMap.put(fixedCols.get(it.key.toString()), it.value)
                    }


                }
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        }
        return filterMap.isEmpty() ? "" : filterMap.toString()
    }

    def generateSortingMap(def sortingMapJson, columnSequenceJson, Map fixedColMap) {
        JsonSlurper jsonSlurper = new JsonSlurper()
        Map labelSortingMap = new HashMap()
        try {
            if (sortingMapJson && columnSequenceJson) {
                Map colSeqMap = new HashMap()
                Map columnSequence = jsonSlurper.parseText(columnSequenceJson)
                columnSequence?.values().findAll { it.containerView == 1 }.each {
                    colSeqMap.put(it.seq.toString(), it.label)
                }
                Map sortingMap = jsonSlurper.parseText(sortingMapJson)
                sortingMap.each {
                    if (it.key == "0") {
                        labelSortingMap.put("Check Box", it.value)
                    } else if(colSeqMap.get(it?.key?.toString())==null){
                        labelSortingMap.put(fixedColMap.get(it?.key?.toString()), it.value)
                    }else {
                        labelSortingMap.put(colSeqMap.get(it.key.toString()), it.value)
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace()
        }
        return labelSortingMap.collect {it->
            "Column Name : ${it.key}, Sorting ${it.value} "
        } as String
    }

    def updateICRandEvdasViews(){
        try {
            log.info("Refreshing Evdas and ICR View Instances")
            JsonSlurper jsonSlurper = new JsonSlurper()
            for(alertType in ["EVDAS Alert", "Single Case Alert"]){
                ViewInstance defaultView = ViewInstance.findByAlertTypeAndName(alertType, Constants.Commons.DEFAULT_VIEW)
                List viewsList = ViewInstance.findAllByAlertTypeAndNameNotEqual(alertType, Constants.Commons.DEFAULT_VIEW)
                Map viSequenceListDefault = jsonSlurper.parseText(defaultView.columnSeq)
                Map seqMap = [:]
                viSequenceListDefault.each { key, value ->
                    seqMap.put(value.name, value.seq)
                }
                viewsList.each{ViewInstance vi ->
                    Map viSequenceList = jsonSlurper.parseText(vi.columnSeq)
                    viSequenceList.each {
                        it.value.seq = seqMap.getOrDefault(it.value.name, it.value.seq)
                    }
                    vi.columnSeq = JsonOutput.toJson(viSequenceList)
                    vi.save()
                }
            }
        } catch(Exception ex){
            log.error("Exception occurred while refreshing View Instances")
        }
    }

    Map syncAndUpdateColumns(Map columnOrderMap, Map columns) {
        // Step 1: Create a new map to hold the updated columns
        Map<String, Map<String, Object>> updatedColumns = [:]

        // Step 2: Iterate over columnOrderMap to update or add entries in columns
        columnOrderMap.each { orderKey, orderValue ->
            String name = orderValue.name

            if (columns.find { it.value.name == name }) {
                // If the name is present in both maps, update the label
                columns.each { colKey, colValue ->
                    if (colValue.name == name) {
                        // Update the label from columnOrderMap to columns
                        colValue.label = orderValue.label
                        updatedColumns[orderKey] = colValue
                    }
                }
            } else {
                // If the name is missing in columns, add it
                updatedColumns[orderKey] = orderValue
            }
        }

        // Step 3: Remove entries from columns that are not in columnOrderMap
        columns.each { colKey, colValue ->
            if (!columnOrderMap.any { it.value.name == colValue.name }) {
                // Skip this entry, as it should not be part of the updated columns
                return
            }
        }

        return updatedColumns
    }


}
