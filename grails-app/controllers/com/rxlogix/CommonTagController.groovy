package com.rxlogix

import com.rxlogix.signal.ArchivedSingleCaseAlert
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.SingleOnDemandAlert
import com.rxlogix.config.Configuration
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import com.rxlogix.Constants

@Secured(["isAuthenticated()"])
class CommonTagController {

    def commonTagService
    def caseInfoService
    def cacheService

    def getQuanAlertCategories(){
        List<Map> result = commonTagService.getQuanAlertCategories(params)
        render(result as JSON)
    }

    def getQualAlertCategories(){
        List<Map> result = commonTagService.getQualAlertCategories(params)
        render(result as JSON)
    }

    def getLitAlertCategories(){
        List<Map> result = commonTagService.getLitAlertCategories(params)
        render(result as JSON)
    }

    def saveAlertCategories(){
        Map params = request.JSON
        List<Map> existingRows = JSON.parse(params.existingRows)
        List<Map> newRows = JSON.parse(params.newRows)

        Map result = [:]
        if (params.type == "Quantitative")
            result = commonTagService.saveQuanAlertCategories(params.alertId as Long, existingRows, newRows, Boolean.parseBoolean(params.isArchived))
        else if (params.type == "Qualitative")
            result = commonTagService.saveQualAlertCategories(params.alertId as Long, existingRows, newRows, Boolean.parseBoolean(params.isArchived))
        else if (params.type == "Literature")
            result = commonTagService.saveLitAlertCategories(params.alertId as Long, existingRows, newRows, Boolean.parseBoolean(params.isArchived),
                    Constants.LiteratureFields.EMBASE.equalsIgnoreCase(params.datasource))
        else if (params.type == "Qualitative on demand")
            result = commonTagService.saveQualOnDemandAlertCategories(params.alertId as Long, existingRows, newRows)
        else if (params.type == "Quantitative on demand")
            result = commonTagService.saveQuanOnDemandAlertCategories(params.alertId as Long, existingRows, newRows)
        render(result as JSON)
    }

    def commonTagDetails() {
        List<Map> result = cacheService.getCommonTagCache()
        render([commonTagList: result] as JSON)
    }

    def fetchCategoriesByVersion() {
        Long alertId = params.alertId as Long
        def sca

        if (params.domain == Constants.AlertConfigType.INDIVIDUAL_ON_DEMAND) {
            sca = SingleOnDemandAlert.findById(alertId)
        } else if (Boolean.parseBoolean(params.isArchived)) {
            sca = ArchivedSingleCaseAlert.findById(alertId)
        } else {
            sca = SingleCaseAlert.findById(alertId)
        }
        if (sca) {
            Long caseId = sca.caseId
            Long caseSeriesId = sca.executedAlertConfiguration.pvrCaseSeriesId
            if (params.prevAlertId && params.domain !=Constants.AlertConfigType.INDIVIDUAL_ON_DEMAND) {
                Long prevAlertId = params.prevAlertId as Long
                def alert = params.isPrevAlertArchived == "true" ? ArchivedSingleCaseAlert.get(prevAlertId) : SingleCaseAlert.get(prevAlertId)
                caseSeriesId = alert ? alert.executedAlertConfiguration.pvrCaseSeriesId : caseSeriesId

            }
            String tenantId = Holders.config.categories.tenantId
            String alertLevelParams = tenantId + "," + caseSeriesId + "," + caseId + "," + params.caseVersion
            String globalLevelParams = tenantId + "," + caseId + "," + params.caseVersion

            Map inputMap = [alertLevelParams: alertLevelParams, globalLevelParams: globalLevelParams,
                            alertLevelId    : Holders.config.category.singleCase.alertSpecific,
                            globalLevelId   : Holders.config.category.singleCase.global]

            render commonTagService.getCategories(inputMap) as JSON

        }

    }


    def fetchCategoriesMapByCase(String isAdhoc){
        Boolean isArchived = params.boolean('archived')
        Boolean isAdhocRun = isAdhoc.toBoolean()
        Map categoryMap = caseInfoService.getGobalAndAlertSpecificCategoriesList(params.alertId, params.version, isArchived, isAdhocRun)
        render categoryMap.categoryList as JSON
    }

    def fetchCommonCategories() {
        render(commonTagService.fetchCommonCategories(params) as JSON)

    }

    def bulkUpdateCategory() {
        Map params = request.JSON
        render(commonTagService.saveCommonCategories(params) as JSON)
    }


}
