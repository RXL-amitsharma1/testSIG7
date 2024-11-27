package com.rxlogix

import com.rxlogix.config.AdvancedFilter
import com.rxlogix.config.Configuration
import com.rxlogix.config.DefaultViewMapping
import com.rxlogix.signal.ViewInstance
import com.rxlogix.user.User
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import groovy.json.JsonSlurper
import com.rxlogix.signal.ClipboardCases
import org.hibernate.exception.ConstraintViolationException

@Secured(["isAuthenticated()"])
class ViewInstanceController {

    def userService
    def viewInstanceService
    def cacheService
    def CRUDService
    def alertFieldService

    def index() { }

    def saveView(){
        def response
        try {
            ViewInstance viewInstance = new ViewInstance()
            viewInstance.skipAudit=false
            ViewInstance prevViewInstance = ViewInstance.findById(params.currentViewId)
            Boolean isDrillDown
            Long viewId
            User currentUser = userService.getUser()
            if(params.isAdhocCaseSeries && Boolean.parseBoolean(params.isAdhocCaseSeries) && params?.alertType != Constants.AlertConfigType.SINGLE_CASE_ALERT_JADER){
                params.alertType = Constants.AlertConfigType.SINGLE_CASE_ALERT_DRILL_DOWN_ADHOC
            } else if (params.alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT_DRILL_DOWN) {
                isDrillDown = true
                params.alertType = Constants.AlertConfigType.SINGLE_CASE_ALERT
            }

            viewInstance.name = params.name.trim()?.replaceAll("\\s{2,}", " ") //Extra Space is removed from name field
            viewInstance.filters = params.filterMap
            viewInstance.alertType = params.alertType
            viewInstance.sorting = params.sorting
            List viewSharedWith = params.viewSharedWith ? JSON.parse(params.viewSharedWith) : []
            viewInstance.advancedFilter = params.advancedFilter ? AdvancedFilter.findById(params.advancedFilter) : null
            Boolean isFilterSharingAllowed = viewInstanceService.isViewFilterSharingAllowed(viewInstance.advancedFilter, viewSharedWith, Constants.FilterType.ADVANCED_FILTER)
            if(viewInstanceService.updateAdvancedFilterAllowed(viewInstance, viewSharedWith) && isFilterSharingAllowed){
                userService.bindSharedWithConfiguration(viewInstance.advancedFilter, viewSharedWith, false, true)
            }
            viewInstance.user = userService.getUser()
            viewInstance.columnSeq = prevViewInstance.tempColumnSeq ?: prevViewInstance.columnSeq
            viewInstance.customAuditProperties = ["defaultView": params.defaultView.toBoolean()]

            prevViewInstance.tempColumnSeq = null
            Boolean isViewSharingAllowed = viewInstanceService.isViewFilterSharingAllowed(viewInstance, viewSharedWith)
            if (isViewSharingAllowed) {
                userService.bindSharedWithConfiguration(viewInstance, viewSharedWith, false, true)
            }
            if (params.defaultView.toBoolean()) {
                viewInstanceService.updateDefaultViewInstance(viewInstance, params.alertType, currentUser)
            }
            if (params.name.trim() != "System View") {
                prevViewInstance.save(flush: true)
                CRUDService.saveWithAuditLog(viewInstance)
                if (!viewInstance.advancedFilter?.criteria?.contains('name')) {
                    if (viewInstanceService.isViewPropogationAllowed(params.alertType)) {
                        viewInstanceService.propagateViews(params, viewInstance.columnSeq, viewSharedWith)
                        if(params.alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT || params.alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT_DASHBOARD) {
                            viewInstanceService.propagateViewstoDrillDown(params, viewInstance.columnSeq, viewSharedWith)
                        }
                    }
                }
                if (isDrillDown) {
                    ViewInstance drillDownViewInstance = ViewInstance.findByNameAndAlertType(viewInstance.name, Constants.AlertConfigType.SINGLE_CASE_ALERT_DRILL_DOWN)
                    viewId = drillDownViewInstance?.id
                } else if(params.isAdhocCaseSeries){
                    ViewInstance drillDownViewInstance = ViewInstance.findByNameAndAlertType(viewInstance.name, Constants.AlertConfigType.SINGLE_CASE_ALERT_DRILL_DOWN_ADHOC)
                    viewId = drillDownViewInstance?.id
                }
                viewId = viewId ?: viewInstance.id
                response = [success: true, viewId: viewId, errorMessage: !isViewSharingAllowed ? message(code: 'duplicate.shared.view.exists') : '']
            } else {
                response = [success: false, errorMessage: message(code: 'app.label.view.name.exists')]
            }

        } catch (grails.validation.ValidationException | ConstraintViolationException vex) {
            log.error(vex.message)
            response = [success: false, errorMessage: message(code: 'app.label.view.name.exists')]
        } catch (Exception ex) {
            log.error(ex.message)
            response = [success: false, errorMessage: "Fill in the View Name correctly"]
        }
        render(response as JSON)
    }

    def updateView() {
        def response
        try     {
            User currentUser = userService.getUser()
            Boolean isViewSharingAllowed = true
            ViewInstance viewInstance = ViewInstance.findById(params.id)
            params.oldName = viewInstance.name
            String alertType = viewInstance?.alertType ?: params.alertType
            List viewSharedWith = params.viewSharedWith ? JSON.parse(params.viewSharedWith) : []
            if(alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT_DRILL_DOWN) {
                List <ViewInstance> allAssociatedViews = ViewInstance.findAllByNameAndUserAndAlertTypeInList(viewInstance.name, viewInstance.user , [Constants.AlertConfigType.SINGLE_CASE_ALERT_DASHBOARD, Constants.AlertConfigType.SINGLE_CASE_ALERT])
                allAssociatedViews.each {it ->
                        it = viewInstanceService.updateViewInstance(it, params, viewSharedWith, isViewSharingAllowed, true)
                        it.save(flush: true)
                }
            }
            viewInstance.customAuditProperties = ["defaultView": params.defaultView.toBoolean()]
            viewInstance.markDirty("columnSeq")// this is done jsut to capture change in default view when noother property is changed because default view is added manualy this is not domain field
            if (viewInstance?.isViewUpdateAllowed(currentUser)) {
                isViewSharingAllowed = viewInstanceService.isViewFilterSharingAllowed(viewInstance, viewSharedWith)
                viewInstance = viewInstanceService.updateViewInstance(viewInstance, params, viewSharedWith, isViewSharingAllowed)
            }
            viewInstance.save(flush: true)
            if (params?.defaultView?.toBoolean()) {
                viewInstanceService.updateDefaultViewInstance(viewInstance, alertType, currentUser)
            } else if (!params.defaultView?.toBoolean() && currentUser?.getDefaultViewId(alertType) == viewInstance?.id) {
                currentUser.deleteDefaultViewMapping(viewInstance)
            }
            if (!viewInstance?.advancedFilter?.criteria?.contains("'name'")) {
                if (viewInstanceService.isViewPropogationAllowed(alertType)) {
                    params.name = viewInstance.name
                    params.alertType = viewInstance.alertType
                    viewInstanceService.propagateViews(params, viewInstance.columnSeq, viewSharedWith, viewInstance.user, true)
                    if(params.alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT || params.alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT_DASHBOARD) {
                        viewInstanceService.propagateViewstoDrillDown(params, viewInstance.columnSeq, viewSharedWith, viewInstance.user, true)
                    }
                }
            }
            response = [success: true, viewId: viewInstance.id, errorMessage: !isViewSharingAllowed ? message(code: 'duplicate.shared.view.exists') : '']

        } catch (grails.validation.ValidationException vex) {
            log.error(vex.message)
            response = [success: false, errorMessage: message(code: 'app.label.view.name.exists')]
        } catch (Exception ex) {
            log.error(ex.printStackTrace())
            response = [success: false, errorMessage: "Fill in the View Name correctly"]
        }
        render(response as JSON)
    }

    def deleteView() {
        Map response = [success: false]
        try {
            ViewInstance viewInstance = ViewInstance.findById(params.id)
            viewInstance.skipAudit = false
            if (viewInstance.alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT_DRILL_DOWN) {
                viewInstance.skipAudit = true
                String alertTypeUpdated = viewInstanceService.getUpdatedAlertType(viewInstance.alertType)
                viewInstance = ViewInstance.findByNameAndAlertTypeAndUser(viewInstance.name, alertTypeUpdated, viewInstance.user)
            }
            if(viewInstance){
                AdvancedFilter advancedFilter = viewInstance.advancedFilter
                viewInstance.customAuditProperties = ['defaultView': DefaultViewMapping.findByDefaultViewInstance(viewInstance)]
                viewInstanceService.deletePropagatingView(viewInstance.name, viewInstance.alertType, viewInstance.user)
                viewInstanceService.deleteDefaultViewMapping(viewInstance)
                log.info("deleting view with users ${viewInstance?.shareWithUser?.toString()} and groups ${viewInstance?.shareWithGroup?.toString()}")
                //dont remove this log info as this was used as corrective action to fetch lazyproperties in predelete event in audit
                viewInstance.delete(flush: true)
                response.success = true
            }
        } catch (Exception ex) {
            log.error(ex.getMessage())
            response = [success: false, errorMessage: ex.getMessage()]
        }
        render(response as JSON)
    }

    def viewColumnInfo(ViewInstance viewInstance) {
        String callingScreen = params.callingScreen ?: ''
        def columnMapList = []
        List primaryList = []
        List secondaryList = []
        Map labelConfigDisplay = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', null, null, null).collectEntries {
            b -> [b.name, b.display]
        }
        Configuration configuration
        boolean labelCondition
        if (params.configId) {
            configuration = Configuration.get(params.configId)
        }
        if (viewInstance) {

            JsonSlurper js = new JsonSlurper()
            def caseListCols = js.parseText(viewInstance.tempColumnSeq ?: viewInstance.columnSeq)
            // if alertType is icr or adhoc icr or archived icr then change mapping of columns according to DB with given datasource
            // currently only develop for safety
            if (viewInstance.alertType.contains("Single Case Alert") && !viewInstance.alertType.contains('Vaers') && !viewInstance.alertType.contains('Faers') && !viewInstance.alertType.contains('Vigibase') && !viewInstance.alertType.contains('Jader')) {
                // change column labels according to DB
                // only change in case of safety
                Map signalFieldToRptMap = Holders.config.icrFields.rptMap
                Map rptToUiLabelMap = cacheService.getRptToUiLabelMapForSafety() as Map
                caseListCols.each {
                    if (signalFieldToRptMap.containsKey(it?.value?.name) && rptToUiLabelMap.get(signalFieldToRptMap.get(it?.value?.name)) != null) {
                        it.value.label = rptToUiLabelMap.get(signalFieldToRptMap.get(it?.value?.name))
                    }
                }
            }

            caseListCols.each {
                if (viewInstance.alertType.contains("Aggregate")) {
                    labelCondition = configuration?.groupBySmq ? true : viewInstanceService.isLabelChangeRequired(params.selectedDatasource)
                    if (labelCondition && it.value.name.equals("currentDisposition")) {
                        it.value.label = labelConfigDisplay.get("currentDisposition")?.split("#OR")[0]
                    } else if (!labelCondition && it.value.name.equals("currentDisposition")) {
                        it.value.label = labelConfigDisplay.get("currentDisposition").split("#OR")[1]
                    }
                }
                if (callingScreen) {
                    if ((callingScreen != Constants.Commons.DASHBOARD) || (callingScreen == Constants.Commons.DASHBOARD && !it.value.name.startsWith('exe'))) {
                        if (it.value.containerView == 1) {
                            primaryList.add(it.value)
                        } else {
                            if (!secondaryList.contains(it.value))
                                secondaryList.add(it.value)
                        }
                    }
                } else {
                    if (it.value.containerView == 1) {
                        primaryList.add(it.value)
                    } else {
                        if (!secondaryList.contains(it.value))
                            secondaryList.add(it.value)
                    }
                }
            }

            columnMapList = primaryList + secondaryList.sort { it.label?.toLowerCase() }
        }

        columnMapList.removeAll {
            it.name.endsWith("allRorEvdas")
        }

        if (!Holders.config.icr.comments.enable && viewInstance.alertType.startsWith(Constants.AlertConfigType.SINGLE_CASE_ALERT)) { // added check of flag for PVS-63960
            columnMapList.removeIf { it.name == Constants.AdvancedFilter.COMMENTS }
        }
        if (configuration && configuration.groupBySmq) {
            columnMapList.removeAll {
                (it.name.equals("hlt") || it.name.equals("hlgt") || it.name.equals("smqNarrow"))
            }
        }
        columnMapList = columnMapList.unique(false) { a, b ->
            a.name <=> b.name
        }

        render columnMapList as JSON
    }

    def updateViewColumnInfo(ViewInstance viewInstance, String columnList){
        JsonSlurper js = new JsonSlurper()
        def caseListCols = js.parseText(columnList)
        viewInstanceService.updateViewColumnSeq(caseListCols, viewInstance)
    }

    def discardTempChanges(ViewInstance viewInstance){
        viewInstanceService.discardTempColumnSeq(viewInstance)
    }

    def saveBookmarkPositions() {
        viewInstanceService.updateOrder(params)
        render "success"
    }

    def fetchViewInstances() {
        Long viewId

        Boolean isShared = false
        if(!params.viewId.equals("")){
            viewId=params.viewId as Long
        }
        User currentUser = userService.getUser()
        List viewsList = viewInstanceService.fetchViewsListAndSelectedViewMap(params.alertType , viewId, currentUser)
        Map viewsMap = [viewsList : viewsList]
        render viewsMap as JSON
    }

    def savePinnedIcon(){
        Boolean isPinned = Boolean.parseBoolean(params.isPinned)
        String fieldName = params.fieldName
        if(fieldName)
            viewInstanceService.savePinnedIcon(fieldName,isPinned)
        render (["Success"] as JSON)
    }

    def savePinConfigurationAlert() {
        Boolean isPinned = Boolean.parseBoolean(params.isPinned)
        String fieldName = params.fieldName
        if (fieldName) {
            User currentUser = userService.getUser()
            viewInstanceService.savePinnedConfigAlerts(fieldName, isPinned, currentUser)
        }
        render (["Success"] as JSON)
    }

    def fetchPinnedConfs() {
        User currentUser = userService.getUser()
        render(["pinnedConfs" : viewInstanceService.fetchPinnedConfigs(currentUser)] as JSON)
    }

    def deleteTempView() {
        try {
            ClipboardCases instance = ClipboardCases.findByUser(userService.getUser())
            instance.isFirstUse = false
            instance?.isDeleted = true
            instance?.isUpdated = false
            instance?.tempCaseIds = null
            instance?.save(flush: true)
            render (["Success"] as JSON)
        } catch(Exception ex) {
            log.error(ex)
            render (["Failed"] as JSON)
        }

    }

    def updateTempView(Boolean isDeleted) {
        try {
            ClipboardCases instance = ClipboardCases.findByUser(userService.getUser())
            instance?.isDeleted = isDeleted
            instance?.tempCaseIds = null
            instance?.save(flush:true)
            render (["Success"] as JSON)
        } catch(Exception ex) {
            ex.printStackTrace()
            render (["Failed"] as JSON)
        }
    }

    def fetchIfTempAvailable() {
        try {
            ClipboardCases clipboardCase = ClipboardCases.createCriteria().get{
                eq('user.id' , userService.getCurrentUserId())
                'or'{
                    eq('isFirstUse' , true)
                    eq('isUpdated', true)
                }
                eq('isDeleted' , false)

            }
            render([status: 200, instanceId: clipboardCase?.id] as JSON)
        } catch (Exception ex) {
            ex.printStackTrace()
            render([status: 500] as JSON)
        }
    }

}

