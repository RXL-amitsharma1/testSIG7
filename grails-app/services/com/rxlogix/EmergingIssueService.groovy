package com.rxlogix

import com.rxlogix.audit.AuditTrail
import com.rxlogix.audit.AuditTrailChild
import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.signal.EmergingIssue
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import grails.async.Promise
import static grails.async.Promises.task
import grails.gorm.dirty.checking.DirtyCheck
import grails.gorm.transactions.Transactional
import grails.util.Holders
import grails.validation.ValidationException
import groovy.json.JsonSlurper

import static com.rxlogix.util.DateUtil.toDateTimeString

@Transactional
@DirtyCheck
class EmergingIssueService implements AlertUtil{

    def CRUDService
    def userService
    def cacheService
    def dataObjectService
    def sessionFactory
    def signalAuditLogService

    def getEmergingIssues() {
        def finalEventList = []
        def emergingIssueList = EmergingIssue.list().collect { EmergingIssue emergingIssue ->
            def eventList
            def eventMap = prepareEventMap(emergingIssue.eventName, eventList)
            finalEventList.add(eventMap.'PT')
        }
        return finalEventList.flatten()
    }

    def getEmergingIssueList(Map params) {
        String timezone = userService?.getCurrentUserPreference()?.timeZone
        def eventList
        String esc_char = ""
        String searchString = params["search[value]"]
        List emergingIssues = EmergingIssue.createCriteria().list(max: params.length, offset: params.start){
            if(searchString){
                searchString = searchString.toLowerCase()
                if (searchString.contains('_')) {
                    searchString = searchString.replaceAll("\\_", "!_")
                    esc_char = "!"
                } else if (searchString.contains('%')) {
                    searchString = searchString.replaceAll("\\%", "!%%")
                    esc_char = "!"
                }
                if(esc_char){
                    or {
                        sqlRestriction("""lower(products) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'""")
                        sqlRestriction("""lower(events) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'""")
                        sqlRestriction("""lower(modified_by) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'""")
                        sqlRestriction("""lower(REPLACE(data_source_dict,'pva','safetydb')) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'""")
                        sqlRestriction("""lower(REPLACE(data_source_dict,'eudra','evdas')) like '%${searchString.replaceAll("'", "''")}%' escape '${esc_char}'""")
                    }
                } else {
                    or {
                        sqlRestriction("""lower(products) like '%${searchString.replaceAll("'", "''")}%'""")
                        sqlRestriction("""lower(events) like '%${searchString.replaceAll("'", "''")}%'""")
                        sqlRestriction("""lower(modified_by) like '%${searchString.replaceAll("'", "''")}%'""")
                        sqlRestriction("""lower(REPLACE(data_source_dict,'pva','safetydb')) like '%${searchString.replaceAll("'", "''")}%'""")
                        sqlRestriction("""lower(REPLACE(data_source_dict,'eudra','evdas')) like '%${searchString.replaceAll("'", "''")}%'""")
                    }
                }
            }
            order("lastUpdated","desc")
        }
        Integer filteredCount = emergingIssues.totalCount
        def emergingIssueList = emergingIssues.collect { EmergingIssue emergingIssue ->
            def eventMap = prepareEventMap(emergingIssue.eventName, eventList)
            [
                    eventName : eventMap,
                    eventGroupSelection :getGroupNameFieldFromJson(emergingIssue.eventGroupSelection),
                    lastUpdated : DateUtil.toDateStringWithTimeInAmPmFormat(emergingIssue.lastUpdated,timezone),
                    id : emergingIssue.id,
                    ime : emergingIssue.ime,
                    dme : emergingIssue.dme,
                    modifiedBy : User.findByUsername(emergingIssue.modifiedBy)?.fullName?.equalsIgnoreCase(Constants.Commons.SYSTEM)?Constants.Commons.SYSTEM:User.findByUsername(emergingIssue.modifiedBy)?.fullName,
                    emergingIssue : emergingIssue.emergingIssue,
                    specialMonitoring : emergingIssue.specialMonitoring,
                    productSelection : prepareProductMap(emergingIssue.productSelection, emergingIssue.dataSourceDict),
                    productGroupSelection : getGroupNameFieldFromJsonProduct(emergingIssue.productGroupSelection, emergingIssue.dataSourceDict)
            ]
        }
        Integer totalCount = EmergingIssue.createCriteria().get {
            projections {
                count('id')
            }
        }

        return [recordsTotal: totalCount, recordsFiltered: filteredCount, aaData: emergingIssueList]
    }

    def prepareEventMap(eventName, eventList) {
        def eventMap = [:]
        def eventFamilyMap = ['1': "SOC", '2': "HLGT", '3': "HLT", '4': "PT", '5': "LLT", '6': "Synonyms", '7': "SMQ Broad", '8': "SMQ Narrow"]

        def jsonSlurper = new JsonSlurper()
        if(eventName) {
            def list = jsonSlurper.parseText(eventName)
            list.each { k, v ->
                eventList = []
                v.each {
                    eventList = eventList + it.name
                }
                if (!eventList.isEmpty()) {
                    eventMap.put(eventFamilyMap[k], eventList)
                }
            }
        }
        return eventMap
    }
    def getGroupNameFieldFromJson(String jsonString){
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
    def parseJsonString(str) {
        try {
            def jsonSlurper = new JsonSlurper()
            jsonSlurper.parseText(str)
        } catch (all) {
            null
        }
    }

    def save(EmergingIssue emergingIssueList) {
        try {
            CRUDService.save(emergingIssueList)
            AuditTrail auditTrail = new AuditTrail()
            auditTrail.category = AuditTrail.Category.INSERT.toString()
            auditTrail.applicationName = "PV Signal"
            auditTrail.entityId = emergingIssueList.id.toString()
            auditTrail.entityName = "Stop List"
            auditTrail.moduleName = "Important Events"
            String description = prepareProductName(emergingIssueList.productSelection, emergingIssueList.productGroupSelection, emergingIssueList.dataSourceDict)
            if(description!=""){
                description += ("-" + prepareEventName(emergingIssueList.eventName, emergingIssueList.eventGroupSelection))
            }else{
                description = prepareEventName(emergingIssueList.eventName, emergingIssueList.eventGroupSelection)
            }
            auditTrail.entityValue = description?.replaceAll("\n"," ")
            auditTrail.description = description
            auditTrail.transactionId = sessionFactory?.currentSession?.getSessionIdentifier()
            auditTrail.username = userService.currentUserName ?: "System"
            auditTrail.fullname = userService.currentUserName ? cacheService.getUserByUserNameIlike(userService.currentUserName).fullName : "System"
            auditTrail.save()
            saveAuditTrailChild(emergingIssueList, auditTrail, AuditLogCategoryEnum.CREATED)
            return emergingIssueList
        } catch (ValidationException ve) {
            emergingIssueList.errors = ve.errors
            return emergingIssueList
        }
    }

    def update(EmergingIssue emergingIssueList,Map emergingIssueObjectClone) {
        try{
            emergingIssueList = userService.setOwnershipAndModifier(emergingIssueList)
            List products = []
            if (emergingIssueList.productSelection) {
                Map productMap = prepareProductMap(emergingIssueList.productSelection, emergingIssueList.dataSourceDict)
                products += productMap.keySet()
                products += productMap.values().flatten()
            }
            if (emergingIssueList.productGroupSelection) {
                products.add(getGroupNameFieldFromJsonProduct(emergingIssueList.productGroupSelection, emergingIssueList.dataSourceDict))
            }
            emergingIssueList.products = products.join(',')

            List events = []
            if (emergingIssueList.eventName) {
                Map eventMap = prepareEventMap(emergingIssueList.eventName, [])
                events += eventMap.keySet()
                events += eventMap.values().flatten()
            }
            if (emergingIssueList.eventGroupSelection) {
                events.add(getGroupNameFieldFromJson(emergingIssueList.eventGroupSelection))
            }
            emergingIssueList.events = events.join(',')

            if (!emergingIssueList.save(failOnError: true) || emergingIssueList.hasErrors()) {
                throw new ValidationException("Validation Exception", emergingIssueList.errors)
            }
            AuditTrail auditTrail = new AuditTrail()
            auditTrail.category = AuditTrail.Category.UPDATE.toString()
            auditTrail.applicationName = "PV Signal"
            auditTrail.entityId = emergingIssueList.id.toString()
            auditTrail.entityName = "Stop List"
            auditTrail.moduleName = "Important Events"
            String description = prepareProductName(emergingIssueList.productSelection, emergingIssueList.productGroupSelection, emergingIssueList.dataSourceDict)
            if(description!=""){
                description += ("-" + prepareEventName(emergingIssueList.eventName, emergingIssueList.eventGroupSelection))
            }else{
                description = prepareEventName(emergingIssueList.eventName, emergingIssueList.eventGroupSelection)
            }
            auditTrail.entityValue = description?.replaceAll("\n"," ")
            auditTrail.description = description
            auditTrail.transactionId = sessionFactory?.currentSession?.getSessionIdentifier()
            auditTrail.username = userService.currentUserName ?: "System"
            auditTrail.fullname = userService.currentUserName ? cacheService.getUserByUserNameIlike(userService.currentUserName).fullName : "System"
            auditTrail.save()
            saveAuditTrailChild(emergingIssueList, auditTrail, AuditLogCategoryEnum.MODIFIED,emergingIssueObjectClone)
        } catch(Exception e){
            e.printStackTrace()
        }
    }

    def delete(emergingIssueList,Map emergingIssueObjectClone) {
        try{
            CRUDService.delete(emergingIssueList)
            AuditTrail auditTrail = new AuditTrail()
            auditTrail.category = AuditTrail.Category.DELETE.toString()
            auditTrail.applicationName = "PV Signal"
            auditTrail.entityId = emergingIssueList.id.toString()
            auditTrail.entityName = "Stop List"
            auditTrail.moduleName = "Important Events"
            String description = prepareProductName(emergingIssueList.productSelection, emergingIssueList.productGroupSelection, emergingIssueList.dataSourceDict)
            if(description!=""){
                description += ("-" + prepareEventName(emergingIssueList.eventName, emergingIssueList.eventGroupSelection))
            }else{
                description = prepareEventName(emergingIssueList.eventName, emergingIssueList.eventGroupSelection)
            }
            auditTrail.entityValue = description?.replaceAll("\n"," ") + ", " + getFeatureString(emergingIssueObjectClone)
            auditTrail.description = description
            auditTrail.transactionId = sessionFactory?.currentSession?.getSessionIdentifier()
            auditTrail.username = userService.currentUserName ?: "System"
            auditTrail.fullname = userService.currentUserName ? cacheService.getUserByUserNameIlike(userService.currentUserName).fullName : "System"
            auditTrail.save()
            saveAuditTrailChild(emergingIssueList, auditTrail, AuditLogCategoryEnum.DELETED,emergingIssueObjectClone)
        } catch(Exception e){
        }
    }

    def getFeatureString(def emergingIssueobject) {
        return "IME: ${emergingIssueobject?.ime ? "Yes" : "No"}, DME: ${emergingIssueobject?.dme ? "Yes" : "No"}, Stop List: ${emergingIssueobject?.emergingIssue ? "Yes" : "No"}, Special Monitoring: ${emergingIssueobject?.specialMonitoring ? "Yes" : "No"}"
    }

    List getEmergingIssueListReport() {
        List<EmergingIssue> eiList = EmergingIssue.list().sort{ it.lastUpdated }
        Promise promise = task {
            preparingListData(eiList)
        }
        List finalData = promise.get()
        return finalData
    }

    @Transactional
    List preparingListData(List<EmergingIssue> list) {
        String timezone = userService?.getCurrentUserPreference()?.timeZone
        // timezone as displayed on UI
        List emergingIssueList = list.collect { EmergingIssue emergingIssue ->
            [prepareProductName(emergingIssue.productSelection, emergingIssue.productGroupSelection, emergingIssue.dataSourceDict), prepareEventName(emergingIssue.eventName, emergingIssue.eventGroupSelection),
                    emergingIssue.ime ? "Yes" : "No", emergingIssue.dme ? "Yes" : "No", emergingIssue.emergingIssue ? "Yes" : "No", emergingIssue.specialMonitoring ? "Yes" : "No",
                    User.findByUsername(emergingIssue.modifiedBy)?.fullName, DateUtil.toDateStringWithTimeInAmPmFormat(emergingIssue.lastUpdated, timezone) ]
        }
        Collections.reverse(emergingIssueList)
        return emergingIssueList
    }

    String prepareProductName(String productSelection, String productGroupSelection, String dataSourceDict) {
        String dataSource = dataSourceDict?.split(';')?.getAt(0)
        JsonSlurper jsonSlurper = new JsonSlurper()
        Map dataSourceMap = getDataSourceMap()
        dataSource = dataSourceMap?.get(dataSource)
        String products = ""
        Map productTypeMap = [:]
        if (dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM)) {
            productTypeMap = Holders.config.pvsignal.pvcm.dictionary.list
        } else {
            productTypeMap = Holders.config.custom.caseInfoMap.Enabled ? Holders.config.custom.dictionary.list : Holders.config.pvsignal.pva.dictionary.list
        }
        if (productSelection) {
            Map productSelectionMap = jsonSlurper.parseText(productSelection) as Map
            productSelectionMap?.each { key, val ->
                if (val) {
                    products += prepareProductData(key, val, dataSource, productTypeMap)
                }
            }
        } else if (productGroupSelection) {
            products += "Product Group: " + getGroupNameFieldFromJsonProduct(productGroupSelection, dataSourceDict) ?: ""
        }
        return products
    }

    String prepareEventName(String eventName, String eventGroupSelection) {
        JsonSlurper jsonSlurper = new JsonSlurper()
        String events = ''
        if (eventName) {
            Map eventSelection = jsonSlurper.parseText(eventName) as Map
            eventSelection?.each { key, val ->
                if (val) {
                    events += prepareEventData(key, val)
                }
            }
        } else if (eventGroupSelection) {
            events += 'Event Group: ' + getGroupNameFieldFromJson(eventGroupSelection) ?: ""
        }
        return events
    }

    String prepareProductData(def key, def val, String dataSource, Map productTypeMap) {
        String dataString = ''
        dataString += productTypeMap.get(key) + ": "
        val?.collect {
            if (dataSource) {
                dataString += it.name ? it.name + '(' + dataSource + '), ' : it.genericName + ' (' + dataSource + '), '
            } else {
                dataString += it.name ? it.name : it.genericName
            }
        }
        dataString = dataString.substring(0, dataString.length() - 2)
        dataString += '\n'
        return dataString
    }

    String prepareEventData(def key, def val) {
        String eventString = ''
        Map eventFamilyMap = ['1': "SOC", '2': "HLGT", '3': "HLT", '4': "PT", '5': "LLT", '6': "Synonyms", '7': "SMQ Broad", '8': "SMQ Narrow"]
        eventString += eventFamilyMap.get(key) + ': '
        val.collect {
            eventString += it.name + ', '
        }
        eventString = eventString.substring(0, eventString.length() - 2)
        eventString += '\n'
        return eventString
    }

    def prepareProductMap(jsonString, String dataSourceDict) {
        String dataSource = dataSourceDict?.split(';')?.getAt(0)
        Map dataSourceMap = getDataSourceMap()
        dataSource = dataSourceMap.get(dataSource)
        def prdName = ""
        def jsonObj = null
        Map productMap = [:]
        Map productTypeMap
        if(dataObjectService.getDataSourceMap(Constants.DbDataSource.PVCM)){
            productTypeMap =  Holders.config.pvsignal.pvcm.dictionary.list
        }else{
            productTypeMap = Holders.config.custom.caseInfoMap.Enabled ? Holders.config.custom.dictionary.list : Holders.config.pvsignal.pva.dictionary.list
        }
        if (jsonString) {
            jsonObj = parseJsonString(jsonString)
            if (!jsonObj)
                prdName = jsonString
            else {
                def prdVal = jsonObj.findAll{k,v->
                    v.find { it.containsKey('name') || it.containsKey('genericName')}
                }
                prdVal.each{heirarchy->
                    List productName = []
                    heirarchy.value.each{
                        if(dataSource){
                            productName += it.name ? it.name +  ' (' + dataSource + ')' : it.genericName + ' (' + dataSource + ')'
                        }else{
                            productName += it.name ? it.name : it.genericName
                        }
                    }
                    productMap.put(productTypeMap.get(heirarchy.key), productName )
                }
            }
        }
        return productMap
    }

    def getGroupNameFieldFromJsonProduct(jsonString, String dataSourceDict){
        List dataSourceList = dataSourceDict?.split(';')
        def prdName = ""
        def jsonObj = null
        if (jsonString) {
            jsonObj = parseJsonString(jsonString)
            if (!jsonObj)
                prdName = jsonString
            else {
                jsonObj.eachWithIndex{value,ind->
                    if(ind>0) {
                        prdName += ', ' + value.name.substring(0, value.name.lastIndexOf('(') - 1) + ' (' + dataSourceList[ind + 1] + ')'     //dataSourceList contains dataSource for products at 0 index
                    } else{
                        prdName += value.name.substring(0, value.name.lastIndexOf('(') - 1) + ' (' + dataSourceList[ind + 1] + ')'
                    }
                }
            }
        }
        prdName
    }

    String prepareEventStringReport(String jsonStringEvent, String jsonStringGroup){
        def eventList
        Map eventMap = prepareEventMap(jsonStringEvent, eventList)
        String eventGroupString = getGroupNameFieldFromJson(jsonStringGroup)
        String eventString = ""
        eventMap.each{key,value->
            eventString += key + ': '
            eventString += value.join(', ')
            eventString += '\n'
        }
        if(eventGroupString) {
            eventString += 'Event Group: ' + eventGroupString
        }

        return eventString
    }

    String prepareProductStringReport(String jsonStringProduct, String jsonStringGroup, String dataSourceDict){
        def eventList
        Map productMap = prepareProductMap(jsonStringProduct, dataSourceDict)
        String productGroupString = getGroupNameFieldFromJsonProduct(jsonStringGroup, dataSourceDict)
        String productString = ""
        productMap.each{key,value->
            productString += key + ': '
            productString += value.join(', ')
            productString += '\n'
        }
        if(productGroupString) {
            productString += 'Product Group: ' + productGroupString
        }
        return productString
    }

    List fetchProductNameForMatching(String jsonString){
        def prdName = ""
        def jsonObj = null
        List products = []
        if (jsonString) {
            jsonObj = parseJsonString(jsonString)
            if (!jsonObj)
                prdName = jsonString
            else {
                def prdVal = jsonObj.findAll{k,v->
                    v.find { it.containsKey('name') || it.containsKey('genericName')}
                }
                prdVal.each{heirarchy->
                    heirarchy.value.each{
                        products.add([name: it.name?it.name:it.genericName , id:it.id])
                    }
                }
            }
        }
        return products
    }

    List fetchPGNameForMatching(String jsonString){
        List productGroups = []
        def prdName = ""
        def jsonObj = null
        if (jsonString) {
            jsonObj = parseJsonString(jsonString)
            if (!jsonObj)
                prdName = jsonString
            else {
                jsonObj.eachWithIndex{value,ind->
                    productGroups.add([name:value.name.substring(0, value.name.lastIndexOf('(') - 1), id:value.name.substring(value.name.lastIndexOf('(')+1,value.name.lastIndexOf(')'))])
                }
            }
        }
        productGroups
    }

    def getEventNameFieldJson(jsonString) {
        def eventFamilyMap = ['1': "SOC", '2': "HLGT", '3': "HLT", '4': "PT", '5': "LLT", '6': "Synonyms", '7': "SMQ Broad", '8': "SMQ Narrow"]
        def jsonSlurper = new JsonSlurper()

        List eventList = []
        if(jsonString) {
            def list = jsonSlurper.parseText(jsonString)
            list.each { k, v ->
                v.each {
                    if(eventFamilyMap[k] == "SMQ Broad"){
                        eventList.add(it.name + "(Broad)")
                    }else if(eventFamilyMap[k] == "SMQ Narrow"){
                        eventList.add(it.name + "(Narrow)")
                    }else{
                        eventList.add(it.name)
                    }
                }
            }
        }
        return eventList
    }

    void saveAuditTrailChild(def theInstance, AuditTrail auditTrail, AuditLogCategoryEnum auditLogCategoryEnum,Map emergingIssueObjectClone=null) {
        List<Map> changesMade = detectChangesMade(theInstance, auditLogCategoryEnum)
        List<String> auditLogField = ["dme", "ime", "emergingIssue", "specialMonitoring", "eventName", "eventGroupSelection", "productSelection", "productGroupSelection"]
        changesMade.each {
            String oldVal=getEmergencyValue(emergingIssueObjectClone,it.fieldName)
            if (it.fieldName in auditLogField && signalAuditLogService.validateModifiedEmptyValues(it.newValue,oldVal)) {
                AuditTrailChild auditTrailChild = new AuditTrailChild()
                auditTrailChild.newValue = it.newValue
                auditTrailChild.propertyName = it.fieldName
                if(it.fieldName == "emergingIssue"){
                    auditTrailChild.propertyName = "stopList"
                }
                auditTrailChild.oldValue = oldVal
                auditTrailChild.auditTrail = auditTrail
                if(auditTrailChild.oldValue!=auditTrailChild.newValue)
                {
                    auditTrailChild.save()
                }
            }
        }
    }

    String getEmergencyValue(Map emergingIssueObjectClone, String fieldName) {
        String str;
        if (emergingIssueObjectClone?.get("id")) {
            for (Map.Entry<?, ?> entry : emergingIssueObjectClone.entrySet()) {
                if (entry.getKey() == fieldName) {
                    if (entry.getValue() instanceof Boolean) {
                        str = entry.getValue() == true ? "Yes" : "No"
                    } else {
                        str = entry.getValue()
                    }
                }
            }
        }
        return str;
    }

    List detectChangesMade(theInstance, AuditLogCategoryEnum auditLogCategoryEnum) {
        List changesMade = []
        def modifiedFieldNames = null
        List data=[]
        if (auditLogCategoryEnum == AuditLogCategoryEnum.MODIFIED) {
            if (theInstance?.id) {
                theInstance.getProperties().each { property ->
                    data.add(property.key)
                }
                modifiedFieldNames = data
            }
        }


        if (auditLogCategoryEnum in [AuditLogCategoryEnum.CREATED, AuditLogCategoryEnum.DELETED]) {
            modifiedFieldNames = MiscUtil.getPersistentProperties(theInstance)
        }
        for (modifiedFieldName in modifiedFieldNames) {
            getValues(auditLogCategoryEnum, theInstance, modifiedFieldName, changesMade)
        }
        changesMade
    }

    private void getValues(AuditLogCategoryEnum auditLogCategoryEnum, theInstance, name, ArrayList changesMade) {

        def originalValue = getOriginalValue(auditLogCategoryEnum, theInstance, name)
        def newValue = getNewValue(auditLogCategoryEnum, theInstance, name)
        if(auditLogCategoryEnum == AuditLogCategoryEnum.DELETED){
            def value = originalValue
            originalValue = newValue
            newValue = value
        }

        def value = [fieldName    : name,
                     originalValue: originalValue,
                     newValue     : newValue]

        changesMade << value
    }

    private getOriginalValue(AuditLogCategoryEnum auditLogCategoryEnum, theInstance, name) {
        def originalValue

        if (auditLogCategoryEnum == AuditLogCategoryEnum.CREATED) {
            originalValue = Constants.AuditLog.EMPTY_VALUE
        } else {
            originalValue = theInstance.getPersistentValue(name)

            if (originalValue?.metaClass?.respondsTo(theInstance, "getInstanceIdentifierForAuditLog")) {
                originalValue = originalValue?.getInstanceIdentifierForAuditLog()
            }
        }
        if(auditLogCategoryEnum == AuditLogCategoryEnum.MODIFIED && name in ["ime","dme","specialMonitoring","emergingIssue"] && !originalValue){
            originalValue = "No"
        }
        originalValue!=null ? originalValue: Constants.AuditLog.EMPTY_VALUE
    }

    private getNewValue(auditLogCategoryEnum, theInstance, name) {
        theInstance."$name" ?(theInstance."$name" instanceof Boolean ? "Yes":theInstance."$name"): (name in ["ime","dme","specialMonitoring","emergingIssue"] ? "No" : Constants.AuditLog.EMPTY_VALUE)
    }

}
