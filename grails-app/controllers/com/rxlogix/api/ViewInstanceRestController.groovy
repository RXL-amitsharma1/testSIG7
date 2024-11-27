package com.rxlogix.api

import com.rxlogix.Constants
import com.rxlogix.ExceptionHandlingController
import com.rxlogix.config.DefaultViewMapping
import com.rxlogix.security.Authorize
import com.rxlogix.signal.ViewInstance
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.RestApiResponse
import grails.converters.JSON
import grails.validation.ValidationException
import org.springframework.http.HttpStatus
import org.hibernate.exception.ConstraintViolationException

@Authorize
class ViewInstanceRestController implements AlertUtil, ExceptionHandlingController {

    def userService
    def viewInstanceService

    static allowedMethods = [fetchPinnedConfs: "GET", fetchViewInstances: "GET", viewColumnInfo: "GET", saveView: "POST", updateViewColumnInfo: "POST", deleteView: "POST",savePinConfigurationAlert:"POST"]

    String saveView() {
        Map responseMap = [:]
        String jsonContent = request.JSON
        log.info("json content received is := " + jsonContent )
        try {
            Map dataMap = jsonContent ? jsonToMap(jsonContent) : null
            String viewName = dataMap.get("viewName")
            if (viewName.trim() != Constants.Commons.DEFAULT_VIEW) {
                User currentUser = userService.getUser()
                responseMap = viewInstanceService.saveViewInstance(dataMap,currentUser)
            } else {
                String resultMessage = message(code: 'app.label.view.name.exists')
                RestApiResponse.failureResponse(responseMap, resultMessage)
                render(status: HttpStatus.INTERNAL_SERVER_ERROR.value(),text:responseMap as JSON)
                return
            }
        } catch (ValidationException | ConstraintViolationException vex) {
            log.error(vex.message)
            String errorMessage = message(code: 'app.label.view.name.exists')
            RestApiResponse.failureResponse(responseMap, errorMessage)
            render(status: HttpStatus.INTERNAL_SERVER_ERROR.value(),text:responseMap as JSON)
            return
        } catch (Exception ex) {
            log.error("Error saving view instance", ex)
            RestApiResponse.serverErrorResponse(responseMap)
            render(status: HttpStatus.INTERNAL_SERVER_ERROR.value(),text:responseMap as JSON)
            return
        }
        render(responseMap as JSON)
    }

    String updateView() {
        Map responseMap = [:]
        String jsonContent = request.JSON
        log.info("json content received is := " + jsonContent)
        try {
            Map dataMap = jsonContent ? jsonToMap(jsonContent) : null
            User currentUser = userService.getUser()
            Long viewId = dataMap.get("viewId") as Long
            ViewInstance viewInstance = ViewInstance.findById(viewId)
            if (viewInstance) {
                responseMap = viewInstanceService.updateView(viewInstance, dataMap, currentUser)
            } else {
                RestApiResponse.recordAbsentResponse(responseMap)
                render(status: HttpStatus.INTERNAL_SERVER_ERROR.value(),text:responseMap as JSON)
                return
            }
        } catch (ValidationException vex) {
            log.error(vex.message)
            String errorMessage = message(code: 'app.label.view.name.exists')
            RestApiResponse.failureResponse(responseMap, errorMessage)
            render(status: HttpStatus.INTERNAL_SERVER_ERROR.value(),text:responseMap as JSON)
            return
        } catch (Exception ex) {
            log.error("Error saving view instance", ex)
            RestApiResponse.serverErrorResponse(responseMap)
            render(status: HttpStatus.INTERNAL_SERVER_ERROR.value(),text:responseMap as JSON)
            return
        }
        render(responseMap as JSON)
    }

    String deleteView() {
        Map responseMap = [:]
        String jsonContent = request.JSON
        log.info("json content received is := " + jsonContent)
        Map dataMap = jsonContent ? jsonToMap(jsonContent) : null
        Long viewId = dataMap.get("viewId") as Long
        ViewInstance viewInstance = ViewInstance.findById(viewId)
        if (viewInstance) {
            if (viewInstance.name.trim() != Constants.Commons.DEFAULT_VIEW) {
                viewInstance.skipAudit = false
                String alertType = viewInstance.alertType
                if (viewInstance.alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT_DRILL_DOWN) {
                    viewInstance.skipAudit = true
                    String alertTypeUpdated = viewInstanceService.getUpdatedAlertType(viewInstance.alertType)
                    viewInstance = ViewInstance.findByNameAndAlertTypeAndUser(viewInstance.name, alertTypeUpdated, viewInstance.user)
                }
                if (viewInstance) {
                    viewInstance.customAuditProperties = ['defaultView': DefaultViewMapping.findByDefaultViewInstance(viewInstance)]
                    viewInstanceService.deletePropagatingView(viewInstance.name, viewInstance.alertType, viewInstance.user)
                    viewInstanceService.deleteDefaultViewMapping(viewInstance)
                    log.info("deleting view with users ${viewInstance?.shareWithUser?.toString()} and groups ${viewInstance?.shareWithGroup?.toString()}")
                    //dont remove this log info as this was used as corrective action to fetch lazyproperties in predelete event in audit
                    viewInstance.delete(flush: true)
                    ViewInstance defaultViewInstance = viewInstanceService.fetchSelectedViewInstance(alertType)
                    Map resultData = ["viewId": defaultViewInstance?.id]
                    RestApiResponse.successResponseWithData(responseMap, null, resultData)
                }
            } else{
                RestApiResponse.failureResponse(responseMap,"System view deletion not allowed")
                render(status: HttpStatus.INTERNAL_SERVER_ERROR.value(), text: responseMap as JSON)
                return
            }
        } else {
            RestApiResponse.recordAbsentResponse(responseMap)
            render(status: HttpStatus.INTERNAL_SERVER_ERROR.value(), text: responseMap as JSON)
            return
        }
        render(responseMap as JSON)
    }

    String viewColumnInfo(Long configId, Long viewId, String selectedDatasource, String callingScreen) {
        Map responseMap = [:]
            if (viewId) {
                List columnMapList = viewInstanceService.viewColumnInfoList(configId, viewId, selectedDatasource, callingScreen)
                RestApiResponse.successResponseWithData(responseMap, null, columnMapList)
            } else {
                RestApiResponse.recordAbsentResponse(responseMap)
                render(status: HttpStatus.INTERNAL_SERVER_ERROR.value(), text: responseMap as JSON)
                return
            }
        render(responseMap as JSON)
    }

    String updateViewColumnInfo() {
        Map responseMap = [:]
        String jsonContent = request.JSON
            Map dataMap = jsonContent ? jsonToMap(jsonContent) : null
            Long viewId = dataMap.get("viewId") as Long
            log.info("view id to update view column info is := " + viewId)
            ArrayList columnList = dataMap.get("columnList") as ArrayList
            println columnList
            if (viewId && columnList) {
                ViewInstance viewInstance = ViewInstance.get(viewId)
                viewInstanceService.updateAggregateViewColumnSeq(columnList, viewInstance)
                RestApiResponse.successResponse(responseMap)
            } else {
                RestApiResponse.invalidParametersResponse(responseMap)
                render(status: HttpStatus.INTERNAL_SERVER_ERROR.value(), text: responseMap as JSON)
                return
            }

        render(responseMap as JSON)
    }

    String discardTempChanges() {
        Map responseMap = [:]
        String jsonContent = request.JSON
            Map dataMap = jsonContent ? jsonToMap(jsonContent) : null
            Long viewId = dataMap.get("viewId") as Long
            log.info("view id to discard view temp changes is := " + viewId)
            if (viewId) {
                ViewInstance viewInstance = ViewInstance.get(viewId)
                viewInstanceService.discardTempColumnSeq(viewInstance)
                RestApiResponse.successResponse(responseMap)
            } else {
                RestApiResponse.invalidParametersResponse(responseMap)
                render(status: HttpStatus.INTERNAL_SERVER_ERROR.value(), text: responseMap as JSON)
                return
            }
        render(responseMap as JSON)
    }

    String fetchViewInstances(String alertType, Long viewId) {
        Map responseMap = [:]
        if (viewId && alertType) {
            User currentUser = userService.getUser()
            List viewsList = viewInstanceService.fetchViewsListAndSelectedViewMap(alertType, viewId, currentUser)
            Map viewsMap = [viewsList: viewsList]
            RestApiResponse.successResponseWithData(responseMap, null, viewsMap)
        } else {
            RestApiResponse.invalidParametersResponse(responseMap)
            render(status: HttpStatus.INTERNAL_SERVER_ERROR.value(), text: responseMap as JSON)
            return
        }
        render(responseMap as JSON)
    }

    String savePinConfigurationAlert() {
        Map responseMap = [:]
        String jsonContent = request.JSON
        log.info("json content received is := " + jsonContent)
        Map dataMap = jsonContent ? jsonToMap(jsonContent) : null
        Boolean isPinned = dataMap.get("isPinned")
        String fieldName = dataMap.get("fieldName")
        if (fieldName) {
            User currentUser = userService.getUser()
            viewInstanceService.savePinnedConfigAlerts(fieldName, isPinned, currentUser)
            RestApiResponse.successResponse(responseMap)
        }
        render(responseMap as JSON)
    }

    String fetchPinnedConfigs() {
        Map responseMap = [:]
        User currentUser = userService.getUser()
        List<String> pinnedConfigs = viewInstanceService.fetchPinnedConfigs(currentUser)
        RestApiResponse.successResponseWithData(responseMap, null, pinnedConfigs)
        render(responseMap as JSON)
    }

}

