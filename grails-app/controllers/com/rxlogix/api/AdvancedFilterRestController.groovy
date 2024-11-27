package com.rxlogix.api

import com.rxlogix.Constants
import com.rxlogix.config.AdvancedFilter
import com.rxlogix.config.EvdasAlert
import com.rxlogix.security.Authorize
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.AggregateOnDemandAlert
import com.rxlogix.signal.ViewInstance
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.RestApiResponse
import grails.converters.JSON
import grails.validation.ValidationException
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.web.multipart.MultipartFile

@Authorize
class AdvancedFilterRestController implements AlertUtil {
    def advancedFilterService
    def userService
    def viewInstanceService
    def cacheService
    def CRUDService
    def alertFieldService
    def reportIntegrationService
    def singleCaseAlertService

    static allowedMethods = [delete: "DELETE", save: "POST", fetchAjaxAdvancedFilterSearch: "GET"]
    private
    static List<String> countColumn = ["newSponCount", "cumSponCount", "newStudyCount", "cumStudyCount", "newSeriousCount",
                                       "cumSeriousCount", "newGeriatricCount", "cumGeriatricCount", "newNonSerious",
                                       "cumNonSerious", "newFatalCount", "cumFatalCount", "eb05", "eb95", "ebgm", "prrValue",
                                       "prrLCI", "prrUCI", "rorValue", "rorLCI", "rorUCI", "newEv", "totalEv", "newSerious",
                                       "totalSerious", "newFatal", "totalFatal", "newRc", "totRc", "newLit", "totalLit",
                                       "newPaed", "totPaed", "newGeria", "totGeria", "newEea", "totEea", "newMedErr", "totMedErr",
                                       "newHcp", "totHcp", "newSpont", "totSpont", "newObs", "totObs", "ratioRorPaedVsOthers",
                                       "ratioRorGeriatrVsOthers", "totSpontRest", "totSpontAsia", "totSpontJapan",
                                       "totSpontNAmerica", "totSpontEurope", "asiaRor", "restRor", "japanRor", "europeRor",
                                       "northAmericaRor", "newCount", "cummCount", "newPediatricCount", "cummPediatricCount",
                                       "newInteractingCount", "cummInteractingCount", "chiSquare", "completenessScore",
                                       "patientAge", "medErrorPtCount","timeToOnset","newProdCount","cumProdCount",
                                       "freqPeriod", "cumFreqPeriod","reviewedFreqPeriod","reviewedCumFreqPeriod","pecImpNumHigh",
                                       "aValue", "bValue", "cValue", "dValue", "eValue", "rrValue"]
    private
    static List<String> clobColumn = ["caseNarrative", "conComitList", "ptList", "suspectProductList", "medErrorPtList",
                                      "aggImpEventList", "evImpEventList","indication","causeOfDeath","patientMedHist","patientHistDrugs",
                                      "batchLotNo","caseClassification","therapyDates","doseDetails","primSuspProdList","primSuspPaiList","paiAllList","allPtList", "genericName","allPTsOutcome","crossReferenceInd"]

    private static List<String> integratedReviewColumn = ["newCountFaers", "cummCountFaers", "newPediatricCountFaers", "cummPediatricCountFaers",
                                                          "newInteractingCountFaers", "cummInteractingCountFaers", "eb05Faers", "eb95Faers", "ebgmFaers", "prrValueFaers",
                                                          "prrLCIFaers", "prrUCIFaers", "rorValueFaers", "rorLCIFaers", "rorUCIFaers","newSeriousCountFaers",
                                                          "cumSeriousCountFaers","newSponCountFaers","cumSponCountFaers","newStudyCountFaers",
                                                          "cumStudyCountFaers","newFatalCountFaers","cumFatalCountFaers","chiSquareFaers",
                                                          "impEventsFaers","freqPriorityFaers","positiveRechallengeFaers","positiveDechallengeFaers",
                                                          "listedFaers","relatedFaers","pregenencyFaers","trendTypeFaers",
                                                          "newEeaEvdas","totEeaEvdas", "newHcpEvdas","totHcpEvdas", "newSeriousEvdas","totalSeriousEvdas", "newMedErrEvdas","totMedErrEvdas",
                                                          "newObsEvdas","totObsEvdas", "newFatalEvdas", "totalFatalEvdas","newRcEvdas","totRcEvdas", "newLitEvdas","totalLitEvdas", "newPaedEvdas", "totPaedEvdas","ratioRorPaedVsOthersEvdas", "newGeriaEvdas","totGeriaEvdas", "ratioRorGeriatrVsOthersEvdas",
                                                          "sdrGeratrEvdas", "newSpontEvdas","totSpontEvdas", "totSpontEuropeEvdas", "totSpontNAmericaEvdas", "totSpontJapanEvdas", "totSpontAsiaEvdas", "totSpontRestEvdas",
                                                          "sdrPaedEvdas", "europeRorEvdas", "northAmericaRorEvdas", "japanRorEvdas", "asiaRorEvdas", "restRorEvdas", "newEvEvdas","totalEvEvdas",
                                                          "dmeImeEvdas","sdrEvdas","hlgtEvdas","hltEvdas","smqNarrowEvdas","impEventsEvdas",
                                                          "changesEvdas","listedEvdas" , "allRorEvdas", "newCountVaers", "cummCountVaers", "newFatalCountVaers", "cumFatalCountVaers", "newSeriousCountVaers", "cumSeriousCountVaers",
                                                          "newGeriatricCountVaers", "cumGeriatricCountVaers", "newPediatricCountVaers", "cummPediatricCountVaers", "eb05Vaers", "eb95Vaers", "ebgmVaers", "prrValueVaers", "prrLCIVaers",
                                                          "prrUCIVaers", "rorValueVaers", "rorLCIVaers", "rorUCIVaers", "chiSquareVaers",
                                                          "newCountVigibase", "cummCountVigibase", "newFatalCountVigibase", "cumFatalCountVigibase", "newSeriousCountVigibase", "cumSeriousCountVigibase",
                                                          "newGeriatricCountVigibase", "cumGeriatricCountVigibase", "newPediatricCountVigibase", "cummPediatricCountVigibase", "eb05Vigibase", "eb95Vigibase", "ebgmVigibase", "prrValueVigibase", "prrLCIVigibase",
                                                          "prrUCIVigibase", "rorValueVigibase", "rorLCIVigibase", "rorUCIVigibase", "chiSquareVigibase",
                                                          "newCountJader", "cumCountJader", "newFatalCountJader", "cumFatalCountJader", "newSeriousCountJader", "cumSeriousCountJader",
                                                          "newGeriatricCountJader", "cumGeriatricCountJader", "newPediatricCountJader", "cumPediatricCountJader", "eb05Jader", "eb95Jader", "ebgmJader", "prrValueJader", "prrLCIJader",
                                                          "prrUCIJader", "rorValueJader", "rorLCIJader", "rorUCIJader", "chiSquareJader",
                                                          "aValueJader", "bValueJader", "cValueJader", "dValueJader", "eValueJader", "rrValueJader"]


    String save() {
        Map responseMap = [:]
        Map params = request.JSON
        params.JSONQuery = JsonOutput.prettyPrint(JsonOutput.toJson(params.filterQuery))
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
                    userService.bindSharedWithConfiguration(advancedFilter, params.shareWith*.id, isUpdateRequest, true)
                } else if(!isFilterSharingAllowed) {
                    message = message(code: 'duplicate.shared.view.exists')
                }
                def domainName
                if(params.alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND ){
                    domainName = AggregateOnDemandAlert
                }
                advancedFilter.criteria = createAdvancedFilterCriteria(advancedFilter.JSONQuery,null,domainName)
                advancedFilter.user = advancedFilter?.user ?: currentUser
                CRUDService.saveWithFullUserName(advancedFilter)
            }
            String shared = (userService.currentUserId == advancedFilter.userId) ? '' : Constants.Commons.SHARED
            RestApiResponse.successResponseWithData(responseMap, null, ['id': advancedFilter.id, 'text': advancedFilter.name+ shared])
        } catch (ValidationException e) {
            e.printStackTrace()
            String msg = ""
            if (!params.name) {
                msg = message(code: 'com.rxlogix.config.AdvancedFilter.name.nullable')
            } else if (advancedFilter.hasErrors()) {
                String[] errors = []
                errors = advancedFilter.errors.allErrors.collect {
                    msg = message(code: "${it.code}")
                }
            }
            RestApiResponse.serverErrorResponse(responseMap, msg)
        } catch (Exception ex) {
            log.error("Unexpected error occurred while saving Advanced Filter: ${ex.stackTrace}")
            RestApiResponse.serverErrorResponse(responseMap, message(code : 'com.rxlogix.config.AdvancedFilter.mandatory'))
        }
        render(responseMap as JSON)
    }

    /**
     * Deletes an AdvancedFilter by its ID, ensuring no associated views exist before deletion.
     *
     * The method first attempts to retrieve the AdvancedFilter using the provided ID. If found,
     * it checks if any associated `ViewInstance`s are linked to the filter. If no views are
     * found, the AdvancedFilter is deleted; otherwise, a failure response is returned.
     *
     * The method handles cases of:
     * - No AdvancedFilter found with the given ID
     * - Associated views preventing deletion
     * - Data integrity violations during deletion
     * - General exceptions, with appropriate error logging and responses
     *
     * @param id The ID of the AdvancedFilter to be deleted
     * @return A JSON response indicating success or failure of the delete operation
     */
    def delete() {
        Map responseMap = [:]
        AdvancedFilter advancedFilter
        Map params = request.JSON
        try {
            // Retrieve the AdvancedFilter by id
            advancedFilter = AdvancedFilter.get(params.id as Long)
            if (!advancedFilter) {
                RestApiResponse.failureResponse(responseMap, message(code: 'default.not.found.message', args: ['Advanced Filter']))
                render(responseMap as JSON)
                return
            }

            // Fetch and ensure unique views associated with the AdvancedFilter
            List<ViewInstance> views = ViewInstance.findAllByAdvancedFilter(advancedFilter).unique { it.name }

            if (views.isEmpty()) {
                // No views, safe to delete the AdvancedFilter
                advancedFilterService.deleteAdvancedFilter(advancedFilter)
                RestApiResponse.successResponseWithData(responseMap, null, ['id': advancedFilter.id, 'text': advancedFilter.name])
            } else {
                // Views exist, return failure message with the list of view names
                RestApiResponse.failureResponse(responseMap, message(code: 'default.not.deleted.message.views', args: ['Advanced Filter', views*.name]))
            }
        } catch (DataIntegrityViolationException ex) {
            log.error("Data integrity violation while deleting Advanced Filter: ${ex.message}")

            // Handle specific case where views are associated with the AdvancedFilter
            List<ViewInstance> views = ViewInstance.findAllByAdvancedFilter(advancedFilter)?.unique { it.name }
            String errorMessage = views ?
                    message(code: 'default.not.deleted.message.views', args: ['Advanced Filter', views*.name]) :
                    message(code: 'default.not.deleted.message', args: ['Advanced Filter'])

            RestApiResponse.failureResponse(responseMap, errorMessage)
        } catch (Exception ex) {
            // Catch any other exceptions and respond with a server error
            log.error("Unexpected error occurred: ${ex.message}")
            RestApiResponse.serverErrorResponse(responseMap, ex.message)
        }

        // Render the final response as JSON
        render(responseMap as JSON)
    }

    def fetchAdvancedFilterNameAjax(String alertType, String term, Integer page, Integer max, String callingScreen) {
        Map responseMap = [:]
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
        if(callingScreen == Constants.Commons.DASHBOARD){
            isDashboard = true
        }
        Map data = advancedFilterService.getAjaxAdvFilter(alertType, term, (page - 1) * max, max, isDashboard)
        RestApiResponse.successResponseWithData(responseMap,null,data)
        render(responseMap as JSON)

    }


    String createAdvancedFilterCriteria(String JSONQuery, Long exConfigId, def domainName) {
        JsonSlurper jsonSlurper = new JsonSlurper()
        Map object = jsonSlurper.parseText(JSONQuery)
        Map expressionObj = object.all.containerGroups[0]
        StringBuilder criteria = new StringBuilder()
        criteria.append('{ ->\n')
        generateCriteria(expressionObj, criteria, exConfigId, domainName)
        criteria.append('}')
        return criteria.toString()
    }

    def formatValue = { value ->
        if (value instanceof List || value instanceof Object[]) {
            return value.join(';')
        } else {
            return value.toString()
        }
    }

    private void generateCriteria(Map expressionObj, StringBuilder criteria, Long exConfigId, def domainName) {
        boolean isCumCount = false
        if (expressionObj.containsKey('keyword') && expressionObj.keyword!="") {
            criteria.append(expressionObj.keyword + " {\n")
            for (int i = 0; i < expressionObj.expressions.size(); i++) {
                generateCriteria(expressionObj.expressions[i], criteria, exConfigId, domainName)
            }
            criteria.append("}\n")
        } else {
            if (expressionObj.containsKey('expressions')) {
                for (int i = 0; i < expressionObj.expressions.size(); i++) {
                    generateCriteria(expressionObj.expressions[i], criteria, exConfigId, domainName)
                }
            } else {
                List newFields = alertFieldService.getAlertFields('AGGREGATE_CASE_ALERT', true).collect { it.name }

                // Incorporate specific criteria conditions based on field type
                if (expressionObj.field in countColumn) {
                    criteria.append("criteriaConditionsCount('${expressionObj.field}','${expressionObj.op}','${formatValue(expressionObj.value)}')\n")
                } else if (expressionObj.field in ['caseInitReceiptDate', 'lockedDate', 'dueDate', 'dispLastChange', 'caseCreationDate']) {
                    criteria.append("criteriaConditionsDate('${expressionObj.field}','${expressionObj.op}','${formatValue(expressionObj.value)}')\n")
                } else if (expressionObj.field in clobColumn) {
                    String value = formatValue(expressionObj.value)
                    value = (expressionObj.field == 'caseClassification') ? value?.replaceAll('\n', '\\\\n') : value
                    String escapedValue = escapeSpecialCharactersGroovyCriteria(value)
                    criteria.append("criteriaConditionsForCLOB('${expressionObj.field}','${expressionObj.op}',\"${escapedValue}\",${exConfigId ?: "exConfigId"})\n")
                } else if (expressionObj.field in ['tags', 'subTags', 'currentRun']) {
                    criteria.append("criteriaConditionsTags('${expressionObj.field}','${expressionObj.op}',\"${formatValue(expressionObj.value)}\",${exConfigId})\n")
                } else if (expressionObj.field.toString().startsWith("EBGM:") ||
                        expressionObj.field.toString().startsWith("EB95:") ||
                        expressionObj.field.toString().startsWith("EB05:") ||
                        expressionObj.field.toString().startsWith("ROR:") ||
                        expressionObj.field.toString().startsWith("ROR_LCI:") ||
                        expressionObj.field.toString().startsWith("ROR_UCI:") ||
                        expressionObj.field.toString().startsWith("PRR:") ||
                        expressionObj.field.toString().startsWith("PRR_LCI:") ||
                        expressionObj.field.toString().startsWith("PRR_UCI:") ||
                        expressionObj.field.toString().startsWith("Chi-Square:")) {
                    criteria.append("criteriaConditionsForSubGroup('${expressionObj.field}','${expressionObj.op}',\"${expressionObj.value}\")\n")
                } else if (expressionObj.field.toString().startsWith("ROR-R:") ||
                        expressionObj.field.toString().startsWith("ROR_LCI-R:") ||
                        expressionObj.field.toString().startsWith("ROR_UCI-R:")) {
                    criteria.append("criteriaConditionsForRelSubGroup('${expressionObj.field}','${expressionObj.op}',\"${expressionObj.value}\")\n")
                } else if (expressionObj.field.toString().startsWith("EBGMFAERS:") ||
                        expressionObj.field.toString().startsWith("EB95FAERS:") ||
                        expressionObj.field.toString().startsWith("EB05FAERS:")) {
                    criteria.append("criteriaConditionsForSubGroupFaers('${expressionObj.field}','${expressionObj.op}',\"${expressionObj.value}\")\n")
                } else if (expressionObj.field in integratedReviewColumn) {
                    criteria.append("criteriaConditionsForIntegratedReview('${expressionObj.field}','${expressionObj.op}',\"${formatValue(expressionObj.value)}\")\n")
                }else if ((expressionObj.field in newFields || expressionObj.field.toString().replace("_cum", "") in newFields) && domainName != EvdasAlert) {
                    criteria.append("criteriaConditionsForNewColumns(${isCumCount},'${expressionObj.field}','${expressionObj.op}',\"${expressionObj.value}\")\n")
                } else {
                    String value = formatValue(expressionObj.value)
                    // Handle special case for "dispPerformedBy"
                    if (expressionObj.field in ["dispPerformedBy"] && value?.contains(Constants.Commons.SYSTEM)) {
                        value = value.replaceAll(Constants.Commons.SYSTEM, Constants.SYSTEM_USER)
                    }
                    String escapedValue = escapeSpecialCharactersGroovyCriteria(value)
                    criteria.append("criteriaConditions('${expressionObj.field}','${expressionObj.op}',\"${escapedValue}\")\n")
                }
            }
        }
    }

    String escapeSpecialCharactersGroovyCriteria(String name) {
        // regular expression pattern to match special characters
        def specialCharacters = /["'$]/
        // Replace special characters and newline characters with escaped versions
        return name.replaceAll(specialCharacters) { match -> '\\' + match}.replaceAll(/\n/, /\\r\\n/)
    }

    def fetchAjaxAdvancedFilterSearch(Long executedConfigId, String term, Integer page, Integer max, String field, String alertType, Boolean isFaers) {
        // Trim the term if it is not null
        term = term?.trim()
        max = max ?: 30
        page = page ?: 1

        // Initialize variables
        def domainName = alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT ? AggregateCaseAlert : null
        List<Map> resultList = []
        Integer totalCount = 0
        String currentUserId = Constants.AdvancedFilter.CURRENT_USER_ID
        String currentUserText = Constants.AdvancedFilter.CURRENT_USER_TEXT
        String currentGroupId = Constants.AdvancedFilter.CURRENT_GROUP_ID
        String currentGroupText = Constants.AdvancedFilter.CURRENT_GROUP_TEXT

        // Fetch case series list if the field is CASE_SERIES
        if (field == Constants.AdvancedFilter.CASE_SERIES) {
            resultList = fetchCaseSeriesList(term, page, max)
            totalCount = resultList.size() ?: 40 // Default to 40 if size is null
        }else if (field == "user") {
            resultList = advancedFilterService.getAjaxFilterUserData(term, Math.max(page - 1, 0) * max, max)
            if (currentUserText.toUpperCase().contains(term.toUpperCase().trim())) {
                resultList.add(0, [id: currentUserId, text: currentUserText])
            }
            totalCount = advancedFilterService.ajaxFilterUserCount(term)
        } else if(field =="group"){
            resultList = advancedFilterService.getAjaxFilterGroupData(term, Math.max(page - 1, 0) * max, max)
            if (currentGroupText.toUpperCase().contains(term.toUpperCase().trim())) {
                resultList.add(0, [id: currentGroupId, text: currentGroupText])
            }
            totalCount = advancedFilterService.ajaxFilterGroupCount(term)
        } else {
            // Prepare filter parameters and fetch filtered data
            Map filterMap = [isFaers: isFaers]
            Map filteredValueMap = advancedFilterService.getAjaxFilterData(term, calculateOffset(page, max), max, executedConfigId, field, domainName, filterMap)

            resultList = filteredValueMap.jsonData ?: []
            if (field in [Constants.AdvancedFilter.COMMENTS, Constants.AdvancedFilter.COMMENT]) {
                totalCount = filteredValueMap.possibleValuesListSize
            }
        }

        // Adjust IDs and text for system users
        adjustSystemUser(resultList)

        // Render the response as JSON
        render([list: resultList, totalCount: totalCount] as JSON)
    }

    // Helper method to fetch case series list
    private List<Map> fetchCaseSeriesList(String term, Integer page, Integer max) {
        Map resultMap = reportIntegrationService.fetchCaseSeriesList(term, calculateOffset(page, max), max, userService.getCurrentUserName())
        return resultMap?.result?.sort { it.text }?.collect { [id: it.id, text: "${it.text} (Owner: ${it.owner})"] } ?: []
    }

    // Helper method to calculate offset for pagination
    private static Integer calculateOffset(Integer page, Integer max) {
        return Math.max(page - 1, 0) * max
    }

    // Helper method to adjust system user details in the result list
    private static void adjustSystemUser(List<Map> resultList) {
        resultList.each { item ->
            if (item.id == Constants.SYSTEM_USER) {
                item.id = Constants.Commons.SYSTEM
                item.text = Constants.Commons.SYSTEM
            }
        }
    }

    def fetchAdvancedFilterInfo(AdvancedFilter advancedFilter) {
        def isFilterUpdateAllowed = advancedFilter?.isFilterUpdateAllowed(userService.user)
        def isShared = userService.currentUserId != advancedFilter.userId
        def sharedSuffix = isShared ? Constants.Commons.SHARED : ''

        def filterInfo = [
                name: advancedFilter.name + sharedSuffix,
                description: advancedFilter.description,
                alertType: advancedFilter.alertType,
                filterQuery: JSON.parse(advancedFilter.getJSONQuery()),
                isFilterUpdateAllowed: isFilterUpdateAllowed
        ]

        render filterInfo as JSON
    }

    def validateValue() {
        Map map = [:]
        String selectedField = params.selectedField
        List<String> list = params.values.split(";").collect { it.trim() }.findAll { it }
        if (list) {
            Map<String, List> validationResult = advancedFilterService.getValidInvalidValues(list, selectedField, params.executedConfigId as Long , userService.user?.preference?.locale?.toString(),params.alertType)
            map.put("successList", validationResult?.validValues)
            def duplicates = advancedFilterService.getDuplicates(list)
            map.put("warnings", ["invalidValues" : validationResult?.invalidValues,
                                 "duplicateValues" : duplicates])
        }
        render map as JSON
    }

    def importExcel() {
        Map map = [:]
        String selectedField = params.selectedField
        MultipartFile file = request.getFile('file')
        List list = singleCaseAlertService.processExcelFile(file)
        if (list) {
            Map<String, List> validationResult = advancedFilterService.getValidInvalidValues(list, selectedField, params.executedConfigId as Long , userService.user?.preference?.locale?.toString(),params.alertType)
            map.put("importedValues", validationResult.validValues)
            def duplicates = advancedFilterService.getDuplicates(list)
            map.put("warnings", ["invalidValues" : validationResult?.invalidValues,
                                 "duplicateValues" : duplicates])
        } else {
            map.message = "${message(code: 'app.label.no.data.excel.error')}"
        }
        render map as JSON
    }

}