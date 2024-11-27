package com.rxlogix.util

import com.rxlogix.Constants
import com.rxlogix.config.ActionType
import com.rxlogix.config.Configuration
import com.rxlogix.config.Priority
import com.rxlogix.enums.DataSourceEnum
import com.rxlogix.pvdictionary.config.PVDictionaryConfig
import com.rxlogix.signal.AdHocAlert
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.converters.JSON
import grails.gorm.dirty.checking.DirtyCheck
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Holders
import groovy.json.JsonSlurper
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils

import java.text.ParseException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@DirtyCheck
trait AlertUtil implements JsonUtil {
    def getNameFieldFromJson(jsonString) {
        def prdName = ""
        def jsonObj = null
        def prd = []
        if (jsonString) {
            jsonObj = parseJsonString(jsonString)
            if (!jsonObj)
                prdName = jsonString
            else {
                jsonObj.each { key, value ->
                    if (key != Constants.DRUG_RECORD_NUMBER_LEVEL && key != Constants.IS_MULTI_INGREDIENT_FIELD) { // added check to fix PVS-71186
                        if (value?.name) {
                            prd.add(value.name)
                        } else if (value?.genericName) {
                            prd.add(value.genericName)
                        }
                    }
                }
                prdName = prd ? prd.flatten().sort().join(', ') : ""
            }
        }
        prdName
    }

    String getAllEventNameFieldFromJson(jsonString) {
        String eventName = ""
        List events = []
        def jsonObj = null
        if (jsonString) {
            jsonObj = parseJsonString(jsonString)
            if (!jsonObj)
                eventName = jsonString
            else {
                jsonObj.each { k, v ->
                    if (v?.name) {
                        events.add(v.name)
                    } else if (v?.genericName) {
                        events.add(v.genericName)
                    }
                }
                eventName = events ? events.flatten().sort().join(',') : ""
            }
        }
        eventName
    }

    String replaceImpEventsLabels(String impEvents) {
        List<String> impEventsList = impEvents.split(',')
        impEvents = impEventsList.unique().join(',')
        String specialMonitoringAbbreviation = Holders.config.importantEvents.specialMonitoring.abbreviation
        String stopListAbbreviation = Holders.config.importantEvents.stopList.abbreviation
        String imeAbbreviation = Holders.config.importantEvents.ime.abbreviation
        String dmeAbbreviation = Holders.config.importantEvents.dme.abbreviation
        impEvents = impEvents?.replace('sm', specialMonitoringAbbreviation)
        impEvents = impEvents?.replace('ei', stopListAbbreviation)
        impEvents = impEvents?.replace('ime', imeAbbreviation)
        impEvents = impEvents?.replace('dme', dmeAbbreviation)
        impEvents
    }

    def getEventGroupFromJson(jsonString) {
        JsonSlurper jsonSlurper = new JsonSlurper()
        def object = jsonSlurper.parseText(jsonString)
        def events = ''
        def size = object.size()
        object.each {
            map ->
                events = events + map.name.substring(0, map.name.lastIndexOf('(') - 1)
                size--
                if (size >= 1) {
                    events += ", "
                }
        }
        return events
    }
    // This functions returns all the products.
    def getAllProductNameFieldFromJson(jsonString) {
        def prdName = ""
        def jsonObj = null
        def prd = []
        if (jsonString) {
            jsonObj = parseJsonString(jsonString)
            if (!jsonObj)
                prdName = jsonString
            else {
                jsonObj.each { key, value ->
                    if (key != Constants.DRUG_RECORD_NUMBER_LEVEL) {
                        if (value?.name) {
                            prd.add(value.name)
                        } else if (value?.genericName) {
                            prd.add(value.genericName)
                        }
                    }
                }
                prdName = prd ? prd.flatten().sort().join(',') : ""
            }
        }
        prdName
    }

    /**
     * Get drug number with sustance as comma separated string for product selection
     * @param productSelection
     * @return
     */
    String getAllDrugRecNumberFromJson(String productSelection) {
        String prdName = ""
        def jsonObj = null
        List prd = []
        if (productSelection) {
            jsonObj = parseJsonString(productSelection)
            if (!jsonObj)
                prdName = productSelection
            else {
                jsonObj.each { key, value ->
                    if (key == Constants.DRUG_RECORD_NUMBER_LEVEL) {
                        value.each { it ->
                            prd.add(it.id + ' (' + it.name + ')')
                        }
                    }
                }
                prdName = prd ? prd.flatten().sort().join(', ') : ""
            }
        }
        prdName
    }

    Boolean isIngredient(jsonString,prodString){
        def jsonObject = parseJsonString(jsonString)
        def ingLevel = PVDictionaryConfig.ingredientColumnIndex
        if(jsonObject){
            jsonObject[ingLevel]?.each { it->
                if(it.name == prodString){
                    return true
                }
            }
        }
        return false
    }
    // This function returns all the Product Group Selection.
    def getAllProductGroupSelectionFieldFromJson(String jsonString) {
        def prdName = ""
        def jsonObj = null
        def prd = []
        if (jsonString) {
            jsonObj = parseJsonString(jsonString)
            if (!jsonObj)
                prdName = jsonString
            else {
                jsonObj.each {it ->
                    if(it?.name) {
                        prd.add(it?.name)
                    }
                }
                prdName = prd ? prd.sort().join(',') : ""
            }
        }
        prdName
    }

    def getGroupNameFieldFromJson(jsonString){
        def prdName = ""
        def jsonObj = null
        if (jsonString) {
            jsonObj = parseJsonString(jsonString)
            if (!jsonObj)
                prdName = jsonString
            else {
                prdName=jsonObj.collect{
                    it.name.substring(0,it.name.lastIndexOf('(') - 1)
                }.join(",")
            }
        }
        prdName
    }

    def getIdFieldFromJson(jsonString) {
        def prdId = ""
        def jsonObj = null
        if (jsonString) {
            jsonObj = parseJsonString(jsonString)
            if (!jsonObj)
                prdId = jsonString
            else {
                def prdVal = jsonObj.find { k,v->
                    v.find {
                        it.containsKey('id')
                    }
                }?.value.findAll {
                    it.containsKey('id')
                }.collect {
                    it.id
                }
                prdId = prdVal ? prdVal.sort().join(',') : ""
            }
        }
        prdId
    }

    List getAllIdFieldFromJson(jsonString) {
        List prdId = []
        def jsonObj = null
        if (jsonString) {
            jsonObj = parseJsonString(jsonString)
            if(jsonObj){
               jsonObj.each { k,v->
                    v.each{ it ->
                        if(it.containsKey('id')){
                            prdId.add(it['id'])
                        }
                    }

                }
            }
        }
        prdId
    }


    List<String> getProductIdsWithName(String productSelectionJsonAsString){

        List<String> productList = []
        JsonSlurper jason =  new JsonSlurper()
        if(productSelectionJsonAsString != "" && productSelectionJsonAsString) {
            jason.parseText(productSelectionJsonAsString).each { k, List v ->
                if (!v.isEmpty()) {
                    v.findAll {
                        if (it.get('name') != null)
                            productList.add(it.get('name'))
                    }
                }
            }
        }
        productList
    }

    def getProductSelectionType(jsonString) {
        def jsonSlurper = new JsonSlurper()

        def prodSelType
        if (jsonString) {
            if (isJsonString(jsonString)) {
                def jsonObj = jsonSlurper.parseText(jsonString ?: "")
                jsonObj.find { k, v ->
                    v.find {
                        if (it.containsKey('name'))
                            prodSelType = "product"
                        else if (it.containsKey('genericName'))
                            prodSelType = "generic"
                    }

                }
            }
        }
        prodSelType
    }

    def getRecurrencePattern(jsonString) {
        def prdName = ""
        def jsonObj = null
        if (jsonString) {
            jsonObj = parseJsonString(jsonString)
            if (!jsonObj)
                prdName = jsonString
            else {
                def prdVal = jsonObj.find {k,v->
                    if (k == "recurrencePattern") {
                        prdName = (v.split(";")[0]).split("=")[1]
                    }
                }
            }
        }
        prdName
    }

    def buildAttributes(Map attributeNames, AdHocAlert inst) {
        return attributeNames.inject([:]) { memo, key, value ->
            def result
            if (inst.hasProperty(key)) {
                result = inst.getProperty(key)
            } else {
                result = inst.getAttr(key)
            }

            if (result != null && result.class == User.class) {
                result = result.getFullName()
            } else if (result != null && key == 'productSelection') {
                result = getNameFieldFromJson(result)
            }
            memo[key] = [value, result]
            memo
        }
    }

    def revertMap(map) {
        map.inject([:], {memo, pair-> memo[pair.value] = pair.key; memo})
    }

    def processStringForComparison(str) {
        str = str.trim()
        str = str.toLowerCase()
        str
    }

    def zipFiles(String zipFileName, List files) {
        def zf = new File(Holders.config.tempDirectory as String, zipFileName)
        def zo = new ZipOutputStream(new FileOutputStream(zf))
        files.each { f ->
            zipFile(f, zo)
        }
        zo.close()
        zf
    }

    private zipFile(File f, ZipOutputStream zo) {
        String name = f.getName()
        ZipEntry zipEntry = new ZipEntry(name)
        zo.putNextEntry(zipEntry)
        IOUtils.copy(new FileInputStream(f), zo)
        zo.closeEntry()
    }

    //Breaks the list object into the list of lists till the index is reached.
    def breakListObject(list, finalBrokenList, index) {
        def remainingList = []
        if (list.size() > index) {
            def subList = list.subList(0, index)
            finalBrokenList.add(subList)
            remainingList = list - subList
        }
        if (remainingList.size() > index) {
            breakListObject(remainingList, finalBrokenList, index)
        }
    }

    def allowedDictionarySelection(configuration) {
        def productSelection = configuration.productSelection
        def jsonSlurper = new JsonSlurper()
        if (productSelection) {
            def productSelectionObj = jsonSlurper.parseText(productSelection)
            //Determine if user has selected multiple dictionary values.
            //In case of the same, flow is returned back
            int selectionCheck = 0
            productSelectionObj.each { k, v->
                if (!v.isEmpty() && k != Constants.DRUG_RECORD_NUMBER_LEVEL) {
                    selectionCheck = selectionCheck + 1
                }
            }

            if (configuration instanceof Configuration && configuration?.type != Constants.AlertConfigType.SINGLE_CASE_ALERT && selectionCheck > 1) {
                return false
            }
            //Again find the selection value from the json.
            productSelectionObj.find { k, v ->
                if (!v.isEmpty()  && k != Constants.DRUG_RECORD_NUMBER_LEVEL) {
                    selectionCheck = selectionCheck + 1
                    configuration.productDictionarySelection = k
                }
            }
            return true
        } else {
            return true
        }
    }

    Map getDataSourceMap() {
        Holders.config.pvsignal.supported.datasource.call().collectEntries {
            String value = it.toString().toUpperCase()
            def enumVal = DataSourceEnum."$value"
            if(enumVal) {
                [(it): enumVal.value()]
            }
        }
    }

    List listFromJsonString(String jsonString) {
        List res = []
        try {
            if (jsonString)
                res = JSON.parse(jsonString).collect { k, v -> v*.id }.flatten().unique()
        } catch (Exception ex) {
            ex.printStackTrace()
        }
        res
    }

    public Set<User> getShareWithUsers() {
        Set<User> users = this.shareWithUser ?: []
        return users
    }

    public Set<Group> getShareWithGroups() {
        Set<Group> userGroups = this.shareWithGroup ?: []
        return userGroups
    }

    def fetchEachSubCategory(String flag, String prefix, LinkedHashMap map, String subGroup, Boolean adhocRun = false) {
        Map subGroupsMap = cacheService.getSubGroupMap()
        return fetchEachSubCategory(subGroupsMap, flag, prefix, map, subGroup, adhocRun);
    }

    def fetchEachSubCategory(Map subGroupsMap, String flag, String prefix, LinkedHashMap map, String subGroup, Boolean adhocRun = false) {
        if (!subGroupsMap[flag]) {
            return [:]
        }

        boolean isFaersFlag = (flag == Holders.config.subgrouping.faers.ageGroup.name || flag == Holders.config.subgrouping.faers.gender.name)

        if (!adhocRun && isFaersFlag) {
            subGroup += "Faers"
        }

        Map map05 = SignalUtil.getMapFromClob(map["eb05" + subGroup])
        Map mapEbgm = SignalUtil.getMapFromClob(map["ebgm" + subGroup])
        Map map95 = SignalUtil.getMapFromClob(map["eb95" + subGroup])
        Map finalMap = [:]

        subGroupsMap[flag].each { id, category ->
            String suffix = isFaersFlag ? category + "Faers" : category
            finalMap[prefix + "eb05" + suffix] = map05?.containsKey(category) ? map05[category] : "0"
            finalMap[prefix + "ebgm" + suffix] = mapEbgm?.containsKey(category) ? mapEbgm[category] : "0"
            finalMap[prefix + "eb95" + suffix] = map95?.containsKey(category) ? map95[category] : "0"
        }
        return finalMap
    }

    def fetchAllSubCategoryColumn(String flag, String prefix,String rel, LinkedHashMap map) {
        Map subGroupColumnsCamelCaseMap = cacheService.getAllOtherSubGroupColumnsCamelCase(Constants.DataSource.PVA)
        return fetchAllSubCategoryColumn(subGroupColumnsCamelCaseMap, flag, prefix, rel, map)
    }

    def fetchAllSubCategoryColumn(Map subGroupsMap, String flag, String prefix,String rel, LinkedHashMap map) {
        Map dataMap = [:]
        Map finalMap = [:]
        if (subGroupsMap[flag]) {
            String dataJson
            String dataColumnName = flag + rel + "SubGroup"
            if (prefix) {
                dataJson = map[prefix]?.get(dataColumnName)
            } else {
                dataJson = map[dataColumnName]
            }
            if (dataJson && dataJson != "-" && dataJson != "" && dataJson != null) {
                dataMap = JSON.parse(dataJson)
                subGroupsMap[flag].each { category, columns ->
                    if (dataMap?.containsKey(category)) {
                        def categoryDataMap = dataMap?.get(category)
                        columns.each { column ->
                            String key = prefix ? "${prefix + flag + rel + column}" : "${flag + rel + column}"
                            finalMap[key] = categoryDataMap?.containsKey(column) ? categoryDataMap[column] as String : "-"
                        }
                    }
                }
            }
        }
        return finalMap
    }

    public String getDataSource(String dataSourceValue) {
        Map dataSourceMap = getDataSourceMap()
        List<String> dataSourceList = []
        dataSourceValue.split(',').each {
              dataSourceList.add(dataSourceMap.containsKey(it) ? dataSourceMap[it] : it)
        }
        dataSourceList.join(',')
    }

    String getIdsForProductGroup(String productGroupSelection){
        if(productGroupSelection){
            return JSON.parse(productGroupSelection).collect {it.id}.join(',')
        }
        return null
    }

    String getIdsForEventGroup(def eventGroupSelection){
        if(eventGroupSelection){
            return JSON.parse(eventGroupSelection).collect {it.id}.join(',')
        }
        return null
    }

    def getPatientMedHist(String data){
        String result = Constants.Commons.BLANK_STRING
        if(data){
            result = data.replace("&lt;b&gt;","")?.replace("&lt;/b&gt;","")
                    ?.replace("&lt;/br&gt;","")?.replace("&amp;apos;","'")
        }
        return result
    }
    def replaceTagsToChar(String data){
        String result = Constants.Commons.BLANK_STRING
        if(data){
            result = data.replace("&amp;apos;","'")
        }
        return result
    }

    String checkAccessForCategorySave(String appType){
        String result = ""
        if(appType in [Constants.AlertConfigType.SINGLE_CASE_ALERT, Constants.AlertConfigType.SINGLE_CASE_ALERT_DEMAND] &&
                !(SpringSecurityUtils.ifAllGranted("ROLE_SINGLE_CASE_REVIEWER, ROLE_CATEGORY_SUBCATEGORY_MANAGEMENT") ||
                        SpringSecurityUtils.ifAllGranted("ROLE_VAERS_CONFIGURATION, ROLE_CATEGORY_SUBCATEGORY_MANAGEMENT") ||
                        SpringSecurityUtils.ifAllGranted("ROLE_VIGIBASE_CONFIGURATION, ROLE_CATEGORY_SUBCATEGORY_MANAGEMENT") ||
                        SpringSecurityUtils.ifAllGranted("ROLE_JADER_CONFIGURATION, ROLE_CATEGORY_SUBCATEGORY_MANAGEMENT"))) {
            result = "hidden"
        } else if(appType in [Constants.AlertConfigType.AGGREGATE_CASE_ALERT, Constants.AlertConfigType.AGGREGATE_CASE_ALERT_DEMAND] &&
                !(SpringSecurityUtils.ifAllGranted("ROLE_AGGREGATE_CASE_REVIEWER, ROLE_CATEGORY_SUBCATEGORY_MANAGEMENT") ||
                 (SpringSecurityUtils.ifAllGranted("ROLE_FAERS_CONFIGURATION, ROLE_CATEGORY_SUBCATEGORY_MANAGEMENT") ||
                  SpringSecurityUtils.ifAllGranted("ROLE_VAERS_CONFIGURATION, ROLE_CATEGORY_SUBCATEGORY_MANAGEMENT") ||
                  SpringSecurityUtils.ifAllGranted("ROLE_VIGIBASE_CONFIGURATION, ROLE_CATEGORY_SUBCATEGORY_MANAGEMENT") ||
                         SpringSecurityUtils.ifAllGranted("ROLE_JADER_CONFIGURATION, ROLE_CATEGORY_SUBCATEGORY_MANAGEMENT"))
                )){
            result = "hidden"
        } else if(appType == Constants.AlertConfigType.LITERATURE_SEARCH_ALERT &&
                !SpringSecurityUtils.ifAllGranted("ROLE_LITERATURE_CASE_REVIEWER, ROLE_CATEGORY_SUBCATEGORY_MANAGEMENT")){
            result = "hidden"
        }
        return result
    }

    Boolean hasSignalViewAccessAccess(){
        Boolean result = false
        if(SpringSecurityUtils.ifAnyGranted("ROLE_SIGNAL_MANAGEMENT_VIEWER, ROLE_VIEW_ALL")){
            result = true
        }
        return result
    }

    Boolean hasSignalCreationAccessAccess(){
        Boolean result = false
        if(SpringSecurityUtils.ifAnyGranted("ROLE_SIGNAL_MANAGEMENT_CONFIGURATION")){
            result = true
        }
        return result
    }

    Boolean hasReviewerAccess(String alertType, Boolean isCaseSeries = false){
        Boolean result = false
        switch (alertType){
            case Constants.AlertConfigType.SINGLE_CASE_ALERT:
                if(!isCaseSeries && SpringSecurityUtils.ifAnyGranted("ROLE_SINGLE_CASE_REVIEWER")){
                    result = true
                } else if(isCaseSeries && SpringSecurityUtils.ifAnyGranted("ROLE_SINGLE_CASE_REVIEWER, ROLE_AGGREGATE_CASE_REVIEWER, ROLE_FAERS_CONFIGURATION")){
                    result = true
                }
                break
            case Constants.AlertConfigType.AGGREGATE_CASE_ALERT:
                if(SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_REVIEWER, ROLE_FAERS_CONFIGURATION, ROLE_VAERS_CONFIGURATION, ROLE_VIGIBASE_CONFIGURATION,ROLE_JADER_CONFIGURATION")){
                    result = true
                }
                break
            case Constants.AlertConfigType.EVDAS_ALERT:
                if(SpringSecurityUtils.ifAnyGranted("ROLE_EVDAS_CASE_REVIEWER")){
                    result = true
                }
                break
            case Constants.AlertConfigType.LITERATURE_SEARCH_ALERT:
                if(SpringSecurityUtils.ifAnyGranted("ROLE_LITERATURE_CASE_REVIEWER")){
                    result = true
                }
                break
            case Constants.AlertConfigType.VALIDATED_SIGNAL:
                if(SpringSecurityUtils.ifAnyGranted("ROLE_SIGNAL_MANAGEMENT_REVIEWER")){
                    result = true
                }
                break
            case Constants.AlertConfigType.AD_HOC_ALERT:
                if(SpringSecurityUtils.ifAnyGranted("ROLE_AD_HOC_CRUD")){
                    result = true
                }
                break
            case Constants.AuditLog.ACTION:
                if(SpringSecurityUtils.ifAnyGranted("ROLE_ADMIN, ROLE_DEV,ROLE_CONFIGURATION_CRUD,ROLE_SINGLE_CASE_CONFIGURATION,ROLE_AGGREGATE_CASE_CONFIGURATION, ROLE_LITERATURE_CASE_CONFIGURATION,ROLE_SIGNAL_MANAGEMENT_CONFIGURATION,ROLE_SIGNAL_MANAGEMENT_REVIEWER,ROLE_AD_HOC_CRUD, ROLE_SINGLE_CASE_CONFIGURATION,ROLE_SINGLE_CASE_REVIEWER,ROLE_EVDAS_CASE_CONFIGURATION, ROLE_VAERS_CONFIGURATION,ROLE_FAERS_CONFIGURATION, ROLE_VIGIBASE_CONFIGURATION,ROLE_JADER_CONFIGURATION")){
                    result = true
                }
                break
        }
        return result
    }

    Boolean hasViewerRoleAccess(String alertType, User user){
        Boolean result = false
        List authorisedRoles = []
        switch (alertType) {
            case Constants.AlertConfigType.SINGLE_CASE_ALERT:
                authorisedRoles = ["ROLE_SINGLE_CASE_CONFIGURATION", "ROLE_ADMIN", "ROLE_SINGLE_CASE_REVIEWER", "ROLE_SINGLE_CASE_VIEWER", "ROLE_VIEW_ALL"]
                break
            case Constants.AlertConfigType.AGGREGATE_CASE_ALERT:
                authorisedRoles = ["ROLE_AGGREGATE_CASE_CONFIGURATION", "ROLE_ADMIN", "ROLE_AGGREGATE_CASE_REVIEWER", "ROLE_AGGREGATE_CASE_VIEWER", "ROLE_VIEW_ALL"]
                break
            case Constants.AlertConfigType.EVDAS_ALERT:
                authorisedRoles = ["ROLE_EVDAS_CASE_CONFIGURATION", "ROLE_ADMIN", "ROLE_EVDAS_CASE_REVIEWER", "ROLE_EVDAS_CASE_VIEWER", "ROLE_VIEW_ALL"]
                break
            case Constants.AlertConfigType.LITERATURE_SEARCH_ALERT:
                authorisedRoles = ["ROLE_LITERATURE_CASE_CONFIGURATION", "ROLE_ADMIN", "ROLE_LITERATURE_CASE_REVIEWER", "ROLE_LITERATURE_CASE_VIEWER", "ROLE_VIEW_ALL"]
                break
        }
        Set<String> roles = user?.getEnabledAuthorities()?.collect { it?.authority }
        for (int i = 0; i < authorisedRoles?.size(); i++) {
            if (roles?.contains(authorisedRoles?.get(i))) {
                result = true
                break
            }
        }
        return result
    }

    /* Function to replace value in nested Map and List
     * @Params root - Map/List
     * @Params replaceWhat
     * @Params replaceWith
     */

    def replaceValueInNestedMapOrList(def root, def replaceWhat, def replaceWith) {
        if (root instanceof List) {
            root.collect {
                if (it instanceof Map) {
                    replaceValueInNestedMapOrList(it, replaceWhat, replaceWith)
                } else if (it instanceof List) {
                    replaceValueInNestedMapOrList(it, replaceWhat, replaceWith)
                } else {
                    it == replaceWhat ? replaceWith : it
                }
            }
        } else if (root instanceof Map) {
            root.each {
                if (it.value instanceof Map) {
                    replaceValueInNestedMapOrList(it.value, replaceWhat, replaceWith)
                } else if (it.value instanceof List) {
                    it.value = replaceValueInNestedMapOrList(it.value, replaceWhat, replaceWith)
                } else if (it.value == replaceWhat) {
                    it.value = replaceWith
                }
            }
        }
    }

    String getExportCaseNumber(String caseNumber, Integer followUpNumber){
        caseNumber in [Constants.BlindingStatus.BLINDED, Constants.BlindingStatus.REDACTED] ? caseNumber : (caseNumber + (followUpNumber != null ? "(" + followUpNumber + ")" : ""))
    }

    def getDmvData(String dmv = null, String value = null, String operator = null) {
        String all = Constants.Commons.ALL.toLowerCase().substring(0, 1).toUpperCase() + Constants.Commons.ALL.toLowerCase().substring(1)
        String equals = Constants.AdvancedFilter.EQUALS.toLowerCase().substring(0, 1).toUpperCase() + Constants.AdvancedFilter.EQUALS.toLowerCase().substring(1)
        if (dmv != null && !dmv.equals(Constants.Commons.BLANK_STRING)) {
            if ((value == null || value.equals(Constants.Commons.BLANK_STRING)) && (operator != null && !operator.equals(Constants.Commons.BLANK_STRING) )) {
                dmv + Constants.Commons.SPACE + operator + Constants.Commons.SPACE + all
            } else if ((value != null && !value.equals(Constants.Commons.BLANK_STRING))&& (operator != null && !operator.equals(Constants.Commons.BLANK_STRING) )) {
                dmv + Constants.Commons.SPACE + operator + Constants.Commons.SPACE + value
            } else if (operator == null || operator.equals(Constants.Commons.BLANK_STRING)) {
                dmv + Constants.Commons.SPACE + equals + Constants.Commons.SPACE + all
            } else {
                dmv + Constants.Commons.SPACE + equals + Constants.Commons.SPACE + all
            }
        }
    }

    static def getListOfShareWith(def shareWithUser, def shareWithGroups) {
        def userString = shareWithUser?.join(', ')
        def groupString = shareWithGroups?.join(', ')

        if (userString && groupString) {
            return "${userString}, ${groupString}"
        } else if (userString) {
            return userString
        } else if (groupString) {
            return groupString
        } else {
            return ""  // Return empty string if both user and group are empty
        }
    }

    def getNameFieldArrayFromJson(jsonString) {
        def prdName = ""
        def jsonObj = null
        if (jsonString) {
            jsonObj = parseJsonString(jsonString)
            if (!jsonObj)
                prdName = jsonString
            else {
                def prdVal = jsonObj.collect { k, v ->
                    v.findAll { it.containsKey('name') || it.containsKey('genericName') }
                            .collect { it.name ?: it.genericName }
                }.flatten().unique().sort()
                prdName = prdVal ? prdVal.join(',') : ""
            }
        }
        prdName
    }

    /**
     * Get drug number substance as comma separated string for product selection
     * @param productSelection
     * @return
     */
    String getDrugNameFieldFromJson(String productSelection) {
        String prdName = ""
        def jsonObj = null
        List prd = []
        if (productSelection) {
            jsonObj = parseJsonString(productSelection)
            if (!jsonObj)
                prdName = productSelection
            else {
                jsonObj.each { key, value ->
                    if (key == Constants.DRUG_RECORD_NUMBER_LEVEL) {
                        if (value?.name) {
                            prd.add(value.name)
                        } else if (value?.genericName) {
                            prd.add(value.genericName)
                        }
                    }
                }
                prdName = prd ? prd.flatten().join(', ') : ""
            }
        }
        prdName
    }

    /**
     * Get drug number ids as comma separated string for product selection
     * @param productSelection
     * @return
     */
    String getDrugIdFieldFromJson(String productSelection) {
        String prdName = ""
        def jsonObj = null
        List prd = []
        if (productSelection) {
            jsonObj = parseJsonString(productSelection)
            if (!jsonObj)
                prdName = productSelection
            else {
                jsonObj.each { key, value ->
                    if (key == Constants.DRUG_RECORD_NUMBER_LEVEL) {
                        if (value?.id) {
                            prd.add(value.id)
                        }
                    }
                }
                prdName = prd ? prd.flatten().join(', ') : ""
            }
        }
        prdName
    }

    /**
     * Converts a JSON string into a Map.
     *
     * @param jsonString The JSON string to be converted.
     * @return A Map representing the JSON data, or null if the input is invalid.
     */
    Map<String, Object> jsonToMap(String jsonString) {
        if (!jsonString) {
            return null
        }

        def jsonSlurper = new JsonSlurper()

        try {
            return jsonSlurper.parseText(jsonString) as Map<String, Object>
        } catch (Exception e) {
            e.printStackTrace()
            throw new ParseException(e.getMessage())
        }
    }

}
