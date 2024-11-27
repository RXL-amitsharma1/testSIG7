package com.rxlogix

import com.rxlogix.config.AdvancedFilter
import com.rxlogix.config.EvdasAlert
import com.rxlogix.config.EvdasOnDemandAlert
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.AggregateOnDemandAlert
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.SingleOnDemandAlert
import com.rxlogix.signal.ViewInstance
import com.rxlogix.user.User
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import org.springframework.web.multipart.MultipartFile
import com.rxlogix.Constants

@Secured(["isAuthenticated()"])
class AdvancedFilterController {

    def advancedFilterService
    def CRUDService
    def userService
    def singleCaseAlertService
    def reportIntegrationService
    def cacheService
    ViewInstanceService viewInstanceService


    def fetchAjaxAdvancedFilterSearch(Long executedConfigId, String term, Integer page, Integer max, String field, String alertType) {
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }

        def domainName = null
        List<Map> resultList = []
        Integer totalCount = 0
        if (alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            domainName = AggregateCaseAlert
        } else if (alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND ) {
            domainName = AggregateOnDemandAlert
        } else if (alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT ) {
            domainName = SingleCaseAlert
        } else if( alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT_DEMAND) {
            domainName = SingleOnDemandAlert
        } else if( alertType == Constants.AlertConfigType.EVDAS_ALERT_DEMAND) {
            domainName = EvdasOnDemandAlert
        } else {
            domainName = EvdasAlert
        }

        if(field == Constants.AdvancedFilter.CASE_SERIES) {
            Map resultMap = reportIntegrationService.fetchCaseSeriesList(term , Math.max(page - 1, 0) * max , max , userService.getCurrentUserName())
            resultList = resultMap?.result?.sort{it.text}.collect{[id : it.id , text: it.text + ' ( Owner:' + it.owner + ')' ]}
            totalCount = resultMap.totalCount ?: 40

        } else {
            Map filterMap = [isFaers: params.getBoolean("isFaers", false)]
            Map filteredValueMap = advancedFilterService.getAjaxFilterData(term, Math.max(page - 1, 0) * max, max, executedConfigId, field, domainName, filterMap)
            resultList = filteredValueMap.jsonData
            if(field in [Constants.AdvancedFilter.COMMENTS, Constants.AdvancedFilter.COMMENT]){
                totalCount = filteredValueMap.possibleValuesListSize
            }
        }
        resultList.each { it ->
            if (it.id == Constants.SYSTEM_USER){
                it.id = Constants.Commons.SYSTEM
                it.text= Constants.Commons.SYSTEM
            }
        }
        if(alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT){
            if(Constants.BlindingStatus.BLINDED.toUpperCase().contains(term.toUpperCase())){
                resultList.add(['id': Constants.BlindingStatus.BLINDED,text: Constants.BlindingStatus.BLINDED])
            }
            if(Constants.BlindingStatus.REDACTED.toUpperCase().contains(term.toUpperCase())) {
                resultList.add(['id': Constants.BlindingStatus.REDACTED,text: Constants.BlindingStatus.REDACTED])
            }
        }
        render([list      : resultList,
                totalCount: totalCount] as JSON)
    }

    def fetchAjaxUserSearch(String term, Integer page, Integer max, boolean isGroup) {
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }
        List<Map> resultList = []
        Integer totalCount = 0
        String currentUserId = Constants.AdvancedFilter.CURRENT_USER_ID
        String currentUserText = Constants.AdvancedFilter.CURRENT_USER_TEXT
        String currentGroupId = Constants.AdvancedFilter.CURRENT_GROUP_ID
        String currentGroupText = Constants.AdvancedFilter.CURRENT_GROUP_TEXT
        if (!isGroup) {
            resultList = advancedFilterService.getAjaxFilterUserData(term, Math.max(page - 1, 0) * max, max)
            if (currentUserText.toUpperCase().contains(term.toUpperCase().trim())) {
                resultList.add(0, [id: currentUserId, text: currentUserText])
            }
            totalCount = advancedFilterService.ajaxFilterUserCount(term)
        } else {
            resultList = advancedFilterService.getAjaxFilterGroupData(term, Math.max(page - 1, 0) * max, max)
            if (currentGroupText.toUpperCase().contains(term.toUpperCase().trim())) {
                resultList.add(0, [id: currentGroupId, text: currentGroupText])
            }
            totalCount = advancedFilterService.ajaxFilterGroupCount(term)
        }
        render([list: resultList, totalCount: totalCount] as JSON)
    }

    def save() {
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        AdvancedFilter advancedFilter = null
        try {
            User currentUser = userService.getUser()
            Boolean isUpdateRequest = false
            Boolean isNameUpdated = false
            if (params.filterId) {
                advancedFilter = AdvancedFilter.findById(params.filterId)
                isUpdateRequest = true
                if ((params.name.trim()?.endsWith(Constants.Commons.SHARED) && advancedFilter.userId != currentUser.id) && advancedFilter.isAdvancedFilterShared()) {
                    advancedFilter.name = params.name.trim()?.substring(0, params.name.length() - 3)
                    isNameUpdated = true
                }
            }
            if (!advancedFilter || advancedFilter?.isFilterUpdateAllowed(currentUser)) {
                if (!advancedFilter) {
                    advancedFilter = new AdvancedFilter()
                    advancedFilter.createdBy = currentUser?.fullName
                }
                isNameUpdated ? bindData(advancedFilter, params, [exclude: ['id', 'name']]) : bindData(advancedFilter, params, [exclude: ['id']])
                Boolean isFilterSharingAllowed = viewInstanceService.isViewFilterSharingAllowed(advancedFilter, params.advancedFilterSharedWith, Constants.FilterType.ADVANCED_FILTER)
                Long keyId
                if(params.alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT){
                    if(params.icrAlertType == Constants.AlertConfigType.SINGLE_CASE_ALERT_DRILL_DOWN){
                        advancedFilter.alertType = Constants.AlertConfigType.SINGLE_CASE_ALERT
                    }else{
                        advancedFilter.alertType = params.icrAlertType
                    }
                }
                if(params?.miningVariable){
                    Map miningVariables
                    if(params.alertType?.contains(Constants.DataSource.DATASOURCE_FAERS)) {
                        miningVariables = cacheService.getMiningVariables(Constants.DataSource.FAERS)
                    } else {
                        miningVariables = cacheService.getMiningVariables(Constants.DataSource.PVA)
                    }
                    miningVariables.each{key, value ->
                        if(value?.label.equalsIgnoreCase(params.miningVariable)){
                            keyId = key as Long
                        }
                    }
                }
                if(keyId){
                    advancedFilter.keyId = keyId
                }
                if(currentUser.isAdmin() && isFilterSharingAllowed){
                    userService.bindSharedWithConfiguration(advancedFilter, params.advancedFilterSharedWith, isUpdateRequest, true)
                } else if(!isFilterSharingAllowed) {
                    responseDTO.message = message(code: 'duplicate.shared.view.exists')
                }
                def domainName
                if(params.alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND ){
                    domainName = AggregateOnDemandAlert
                }
                advancedFilter.criteria = advancedFilterService.createAdvancedFilterCriteria(advancedFilter.JSONQuery,null,domainName)
                advancedFilter.user = advancedFilter?.user ?: currentUser
                CRUDService.saveWithFullUserName(advancedFilter)
            }
            responseDTO.data = [:]
            String shared = (userService.currentUserId == advancedFilter.userId) ? '' : Constants.Commons.SHARED
            responseDTO.data.id = advancedFilter.id
            responseDTO.data.text = advancedFilter.name + shared
        } catch (ValidationException e) {
            e.printStackTrace()
            responseDTO.status = false
            if (!params.name) {
                responseDTO.message = message(code: 'com.rxlogix.config.AdvancedFilter.name.nullable')
            } else if (advancedFilter.hasErrors()) {
                String[] errors = []
                errors = advancedFilter.errors.allErrors.collect {
                    responseDTO.message = message(code: "${it.code}")
                }
            }
        } catch (Exception ex) {
            log.error("Advnaced filter save method failed.", ex)
            responseDTO.status = false
            responseDTO.message = message(code : 'com.rxlogix.config.AdvancedFilter.mandatory')
        }
        render(responseDTO as JSON)
    }

    def delete(Long id) {
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        AdvancedFilter advancedFilter

        try {
            advancedFilter = AdvancedFilter.get(id)
            List<ViewInstance> views = ViewInstance.findAllByAdvancedFilter(advancedFilter)
            views = views.unique {
                it.name
            }
            if(views==[]){
                advancedFilterService.deleteAdvancedFilter(advancedFilter)
                responseDTO.data = [:]
                responseDTO.data.id = advancedFilter.id
                responseDTO.data.text = advancedFilter.name
            }else{
                responseDTO.status = false
                responseDTO.message = message(code: 'default.not.deleted.message.views', args: ['Advanced Filter', views*.name])
            }
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            List<ViewInstance> views = ViewInstance.findAllByAdvancedFilter(advancedFilter)
            views = views.unique {
                it.name
            }
            log.error(ex.getMessage())
            responseDTO.status = false
            if (views) {
                responseDTO.message = message(code: 'default.not.deleted.message.views', args: ['Advanced Filter', views*.name])
            } else
                responseDTO.message = message(code: 'default.not.deleted.message', args: ['Advanced Filter'])
        } catch (Exception ex) {
            log.error(ex.getMessage())
            responseDTO.status = false
            responseDTO.message = ex.getMessage()
        }
        render(responseDTO as JSON)
    }

    def fetchAdvancedFilterNameAjax(String alertType, String term, Integer page, Integer max) {
        Boolean isDashboard = false
        if (!max) {
            max = 30
        }
        if (!page) {
            page = 1
        }
        if (term) {
            term = term?.trim()
        }
        if(params.callingScreen == Constants.Commons.DASHBOARD){
            isDashboard = true
        }
        render(advancedFilterService.getAjaxAdvFilter(alertType, term, (page - 1) * 30, max, isDashboard) as JSON)
    }

    def fetchAdvancedFilterInfo(AdvancedFilter advancedFilter) {
        Boolean isFilterUpdateAllowed = advancedFilter?.isFilterUpdateAllowed(userService.user)
        String shareWithElement = g.initializeSharedWithElement(bean: advancedFilter, shareWithId: "advancedFilterSharedWith")
        String shared = (userService.currentUserId == advancedFilter.userId) ? '' : Constants.Commons.SHARED
        Map<String, String> filterInfo = ['name'     : advancedFilter.name + shared, 'description': advancedFilter.description, 'alertType': advancedFilter.alertType,
                                          'JSONQuery': advancedFilter.getJSONQuery(), 'shareWithElement': shareWithElement, 'isFilterUpdateAllowed': isFilterUpdateAllowed]
        render filterInfo as JSON
    }

    def validateValue() {
        Map map = [uploadedValues: "", message: "", success: false]
        String selectedField = params.selectedField
        List<String> list = params.values.split(";").collect { it.trim() }.findAll { it }
        if (list) {
            Map<String, List> validationResult = advancedFilterService.getValidInvalidValues(list, selectedField, params.executedConfigId as Long , userService.user?.preference?.locale?.toString(),params.alertType)
            String template = g.render(template: '/advancedFilters/includes/importValueModal', model: [validValues: validationResult.validValues, invalidValues: validationResult.invalidValues, duplicateValues: advancedFilterService.getDuplicates(list)])
            map.uploadedValues = template
            map.success = true
        }
        render map as JSON
    }

    def importExcel() {
        Map map = [uploadedValues: "", message: "", success: false]
        String selectedField = params.selectedField
        MultipartFile file = request.getFile('file')

        List list = singleCaseAlertService.processExcelFile(file)
        if (list) {
            Map<String, List> validationResult = advancedFilterService.getValidInvalidValues(list, selectedField, params.executedConfigId as Long , userService.user?.preference?.locale?.toString(),params.alertType)
            String template = g.render(template: '/advancedFilters/includes/importValueModal', model: [validValues: validationResult.validValues, invalidValues: validationResult.invalidValues, duplicateValues: advancedFilterService.getDuplicates(list)])
            map.uploadedValues = template
            map.success = true
        } else {
            map.message = "${message(code: 'app.label.no.data.excel.error')}"
        }
        render map as JSON
    }
}