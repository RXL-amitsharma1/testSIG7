package com.rxlogix

import com.rxlogix.audit.AuditTrail
import com.rxlogix.audit.AuditTrailChild
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.enums.DrugClassificationTypeEnum
import com.rxlogix.signal.DrugClassification
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import org.apache.http.util.TextUtils
import org.hibernate.criterion.Criterion
import org.hibernate.criterion.Restrictions
import org.apache.commons.lang3.time.DateUtils
import java.text.ParseException
import java.text.SimpleDateFormat

@Secured(["isAuthenticated()"])
class DrugClassificationController implements AlertUtil{

    def drugClassificationService
    def auditTrailService
    def dynamicReportService
    def userService

    def index() {
        DrugClassification drugClassification = new DrugClassification()
        def classificationTypeEnums = DrugClassificationTypeEnum.values()

        [drugClassificationInstance: drugClassification, classificationTypeEnums: classificationTypeEnums,dataSourceMap: getDataSourceMap()]
    }

    def save() {
        params.className = params.className?.trim()?.replaceAll("\\s{2,}", " ")
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        DrugClassification drugClassification = null
        DrugClassification.withTransaction {
            drugClassification = DrugClassification.findByClassName(params.className)
        }
        if (drugClassification) {
            responseDTO.status = false
            responseDTO.message = message(code: "app.drug.classification.className")
        } else {
            try {
                def classificationList = JSON.parse(params.classificationList)
                Boolean isFirstEntry = true // this variable is used to prevent the audit log for multiple object created for multiple classifications
                if (classificationList) {
                    classificationList = classificationList.unique {
                        [it.classificationType, it.classificationName]
                    }
                    List classificationString = []
                    classificationString = classificationList?.collect {
                        it.classificationName + "(" + DrugClassificationTypeEnum.valueOf(it.classificationType).value() + ")"
                    }
                    def valueMap = [:]
                    classificationList.each {
                        valueMap = prepareValueMap(params, it)
                        isFirstEntry = drugClassificationService.save(valueMap, classificationString as String, isFirstEntry)
                    }
                    Map drugClassificationMap = [:]
                    drugClassificationMap.productDictionarySelection = valueMap.productDictionarySelection
                    drugClassificationMap.className = valueMap.className
                    drugClassificationMap.classification = valueMap.productNames
                    drugClassificationMap.classificationType = valueMap.product
                    drugClassificationMap.productIds = valueMap.productIds
                    drugClassificationMap.productNames = valueMap.productNames
                    isFirstEntry = drugClassificationService.save(drugClassificationMap, classificationString as String, isFirstEntry)
                    responseDTO.message = message(code: "app.drug.classification.created")
                } else {
                    responseDTO.status = false
                    responseDTO.message = message(code: "app.drug.classification.all.fields.required")
                }
            } catch (grails.validation.ValidationException vx) {
                responseDTO.message = MiscUtil.getCustomErrorMessage(vx)
                responseDTO.status = false
                log.error("Exception is : ${vx}")
            }
            catch (Exception ex) {
                log.error(ex.getMessage())
                responseDTO.status = false
                responseDTO.message = message(code: "app.drug.classification.error")
            }
        }
        render responseDTO as JSON
    }

    def update() {
        String dataSource = Holders.config.signal.drugClassification
        params.className = params.className?.trim()?.replaceAll("\\s{2,}", " ")
        DrugClassification."$dataSource".withNewSession {
            ResponseDTO responseDTO = new ResponseDTO(status: true)
            DrugClassification drugClassification = null
            if (TextUtils.isEmpty(params.className)) {
                responseDTO.status = false
                responseDTO.message = message(code: "app.drug.classification.required.fields")
            } else {
                if (params.className != params.prevClassName) {
                    DrugClassification.withTransaction {
                        drugClassification = DrugClassification.findByClassName(params.className)
                    }
                }
                if (drugClassification) {
                    responseDTO.status = false
                    responseDTO.message = message(code: "app.drug.classification.className")
                } else {
                    try {
                        List<DrugClassification> drugClassificationList = []
                        Boolean isFirstEntry = true
                        DrugClassification.withTransaction {
                            drugClassificationList = DrugClassification.findAllByClassName(params.prevClassName, [sort: "id", orderBy: "desc"])
                        }
                        DrugClassification prevInstance = drugClassificationList.find {
                            it.classification == it.productNames
                        }

                        drugClassificationList.removeIf {
                            it.classification == it.productNames
                        }
                        def classificationList = JSON.parse(params.classificationList)
                        if (classificationList) {
                            classificationList = classificationList.unique {
                                [it.classificationType, it.classificationName]
                            }
                            def valueMap = [:]

                            classificationList.each { classification ->
                                DrugClassification drugClassificationObj = null
                                DrugClassification.withTransaction {
                                    drugClassificationObj = drugClassificationList.find {
                                        classification.classificationType == it.classificationType.name() && classification.classificationName == it.classification
                                    }
                                }

                                valueMap = prepareValueMap(params, classification)
                                if (drugClassificationObj) {
                                    isFirstEntry = drugClassificationService.update(drugClassificationObj, valueMap, dataSource,isFirstEntry)
                                } else {
                                    isFirstEntry = drugClassificationService.save(valueMap,"",isFirstEntry)
                                }
                            }
                            valueMap.classification = valueMap.productNames
                            valueMap.classificationType = valueMap.product
                            if (prevInstance)
                                drugClassificationService.update(prevInstance, valueMap,dataSource,isFirstEntry)
                            responseDTO.message = "Drug Classification updated"
                        }

                    } catch (grails.validation.ValidationException vx) {
                        responseDTO.message = message(code: "app.drug.classification.all.fields.required")
                        responseDTO.status = false
                        log.error("Exception is : ${vx}")
                    }
                    catch (Exception ex) {
                        ex.printStackTrace()
                        log.error(ex.getMessage())
                        responseDTO.status = false
                        responseDTO.message = message(code: "app.drug.classification.required.fields")
                    }
                }
            }
            render responseDTO as JSON
        }
    }

    def delete() {
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        try {
            Boolean isFirstEntry = true // this variable is used to prevent the audit log for multiple object created for multiple classifications
            List<DrugClassification> drugClassificationList = []
            DrugClassification.withTransaction {
                drugClassificationList = DrugClassification.findAllByClassName(params.className)
            }
            drugClassificationList.each {drugClassification->
                isFirstEntry = drugClassificationService.delete(drugClassification, isFirstEntry)
            }
            responseDTO.message = message(code: "app.drug.classification.deleted")
        } catch (Exception ex) {
            ex.printStackTrace()
            log.error(ex.getMessage())
            responseDTO.status = false
            responseDTO.message = message(code: "app.drug.classification.cannot.be.deleted")
        }
        render responseDTO as JSON
    }

    def prepareValueMap(params, classification) {
        def paramsMap = [:]
        def products = []
        def productNames = []
        def productIds = []
        def productDictionarySelection = JSON.parse(params.productSelection)
        def product = ''

        //extract product names and ids
        (1..5).each {
            if (productDictionarySelection["${it}"]) {
                products = productDictionarySelection["${it}"]
                if (it == 1) {
                    product = "SUBS"
                } else if (it == 2) {
                    product = "Family"
                } else {
                    product = "DRUG"
                }
            }
        }
        products.each {
            productNames << it.name
            productIds << it.id
        }

        //preparing param map for drug classification
        paramsMap['productDictionarySelection'] = params.productSelection
        paramsMap['className'] = params.className
        paramsMap['classification'] = classification.classificationName
        paramsMap['classificationType'] = classification.classificationType
        paramsMap['product'] = product
        paramsMap['productIds'] = productIds.join(",")
        paramsMap['productNames'] = productNames.join(",")

        return paramsMap
    }

    def list() {
        def drugClassificationList = drugClassificationService.drugClassificationList()
        render drugClassificationList as JSON
    }

    def fetchDrugClassification() {
        def response = [success: true]
        List<DrugClassification> drugClassification = []
        DrugClassification.withTransaction {
            drugClassification = DrugClassification.findAllByClassName(params.className)
        }
        drugClassification.removeIf{
            it.classification == it.productNames
        }
        if (drugClassification) {
            def addedClassification = drugClassification.collect {
                [classification     : it.classification,
                 classificationType : it.classificationType.name(),
                 addedClassification: it.classification + '(' + it.classificationType.value() + ')']
            }
            def valMap = [
                    productSelection   : drugClassification[0].productDictionarySelection,
                    className          : params.className,
                    addedClassification: addedClassification,
                    classificationType : drugClassification[0].classificationType.name(),
                    classification     : drugClassification[0].classification
            ]
            response['valMap'] = valMap
        } else {
            response = [success: false]
        }
        render response as JSON
    }


    //Below is the dummy code of audit details export of audit log event controller in plugin pushed code here for performance bug fixes will remove later
    def generateAuditLogReportFile(params) {
        log.info("Generating Audit Log Extract with params:${params}")
        def startTime = System.currentTimeSeconds()
        def locale = Locale.ENGLISH
        User user = userService.getUser()
        def metadata = [sheetName: "Audit Log Details Export",
                        columns  : [
                                [title: "Category", width: 15],
                                [title: "Entity", width: 21],
                                [title: "Entity Value", width: 25],
                                [title: "User Name (Full Name)", width: 20],
                                [title: "Log Entry Date${userService.getGmtOffset(user?.preference?.timeZone)}", width: 23],
                                [title: "Sub-Entity", width: 20],
                                [title: "Field Name", width: 20],
                                [title: "Old Value", width: 25],
                                [title: "New Value", width: 25],
                        ]]
        List<AuditTrailChild> auditTrailChildren
        List<AuditTrail> auditTrailList = AuditTrail.list([max: 50, offset: 0, sort: 'dateCreated', order: 'desc'])
        String dateTimeFormat = Holders.config.getProperty('grails.application.server.format.dateTime', String)
        String dateFormat = Holders.config.getProperty('grails.application.server.format.date', String)
        Date toDate = null
        Date fromDate = null
        if (params.toDate) {
            toDate = DateUtils.truncate(Date.parse(dateFormat, params.toDate), Calendar.DAY_OF_MONTH) + 1
        }
        if (params.fromDate) {
            fromDate = DateUtils.truncate(Date.parse(dateFormat, params.fromDate), Calendar.DAY_OF_MONTH)
        }
        String sort = params.sort ?: 'dateCreated'
        String direction = params.direction ?: 'desc'
        Map auditLogEventInstances = AuditTrail.search([applicationName: params.applicationName,
                                                        username       : params.username,
                                                        category       : params.category,
                                                        toDate         : toDate,
                                                        fromDate       : fromDate,
                                                        entityName     : params.entityName,
                                                        entityValue    : params.entityValue,
                                                        fulltext       : params.fulltext,
                                                        sort           : sort,
                                                        order          : direction,
                                                        currentUserTimezone: user.preference.timeZone?:'UTC',
                                                        max            :Holders.config.signal.audit.maxExtract])

        String filters = auditLogEventInstances?.filters ?: 'N/A'
        metadata.filters = filters
        def finalData = []

        Map<Long, List<AuditTrailChild>> auditTrailChildrenMap = fetchAuditTrailChildren(auditLogEventInstances.auditTrailList)
        auditLogEventInstances.auditTrailList.each {AuditTrail it->
            if (it.sectionChildModule == false && it.categoryChild) {
                auditTrailChildren = auditTrailChildrenMap.get(it.id) ?: []
            } else {
                auditTrailChildren = AuditTrailChild.findAllByAuditTrail(it)
            }
            String moduleName = it.moduleName
            Map data=[:]
            try{

                data= auditTrailService.fetchAuditTrailChildrenTableData("", auditTrailChildren, it)
            }catch(Exception exception){
                log.info("Exception occure while fetching detail for trail ${it.properties}")
                exception.printStackTrace()
            }
            data?.resultPropertyMapList?.eachWithIndex { Map it2, index ->
                if (index == 0) {
                    String entityValue = it.entityValue
                    if (it.category == "LOGIN_SUCCESS" || it.category == "LOGIN_FAILED") {
                        entityValue = "IP Address:${it.userIpAddress ?: ""}, TimeZone:${it.timeZone ?: ""}, Web Browser:${it.browser ?: ""}, Operating System: ${it.browser ?: ""}"
                    }else if(it.entityValue == null){
                        entityValue = message(code: "audit.legacy.entity.${it.entityName}")+ " (ID:${it.entityId})"
                    }
                    it2.put("category", it.getCategoryName())
                    it2.put("moduleName", moduleName != "" ? moduleName : "default")
                    it2.put("entityValue", entityValue)
                    it2.put("userName", it.username + " (${it.fullname ?: ""})")
                    it2.put("dateCreated", getFormattedDateTime(it.dateCreated, dateTimeFormat, userService.getCurrentUserPreference().timeZone))
                } else {
                    it2.put("category", "")
                    it2.put("moduleName", "")
                    it2.put("entityValue", "")
                    it2.put("userName", "")
                    it2.put("dateCreated", "")
                }

            }
            if (data.resultPropertyMapList) {
                finalData.addAll(data.resultPropertyMapList)
            }
        }
        finalData = finalData?.collect { Map it ->
            [it.category, it.moduleName, it.entityValue, it.userName, it.dateCreated, it.parent, it.field, it.oldValue, it.newValue]
        }
        log.info("time taken in fetching data is ${System.currentTimeSeconds() - startTime}")
        byte[] file = dynamicReportService.createExtractFileForAuditLog(finalData, metadata,user)
        log.info("total time taken  is ${System.currentTimeSeconds() - startTime}")
        String fileName="Audit Log Details-" + DateUtil.stringFromDateInUserTimeZone(new Date(),"yyyy-MM-dd_hh-mm-ss")+ ".xlsx"
        render(file: file, contentType: grailsApplication.config.grails.mime.types.xlsx, fileName: fileName)
        auditTrailService.createAuditForExport(null,"Audit Log Details","Audit Log Details",[outputFormat:"xlsx",reportName:fileName],fileName,null)
    }

    // Function to fetch audit trail children for all audit trail instances in bulk
    def fetchAuditTrailChildren(List<AuditTrail> auditTrailList) {
        Map<Long, List<AuditTrailChild>> auditTrailChildrenMap = [:]

        // Group audit trail IDs into chunks to avoid hitting the database limit
        List<List<AuditTrail>> chunkedAuditTrailLists = chunkAuditTrailList(auditTrailList)

        // Fetch audit trail children in chunks
        chunkedAuditTrailLists.each { chunkedAuditTrailList ->
            // Build the Criterion for the chunk
            Criterion chunkCriterion = buildInCriterion('auditTrail', chunkedAuditTrailList)

            // Fetch audit trail children for the chunk(direct in list can also be used here as chunk size is 1000 hibernate limitation )
            List<AuditTrailChild> chunkAuditTrailChildren = AuditTrailChild.createCriteria().list {
                'and' {
                    add(chunkCriterion)
                }
            }

            // Group audit trail children by their respective parent audit trail instances
            chunkAuditTrailChildren.each { auditTrailChild ->
                List<AuditTrailChild> children = auditTrailChildrenMap.get(auditTrailChild.auditTrail.id)
                if (!children) {
                    children = []
                    auditTrailChildrenMap.put(auditTrailChild.auditTrail.id, children)
                }
                children.add(auditTrailChild)
            }
        }

        return auditTrailChildrenMap
    }

// Function to chunk the audit trail list into smaller chunks
    List<List<AuditTrail>> chunkAuditTrailList(List<AuditTrail> auditTrailList) {
        List<List<AuditTrail>> chunkedLists = []
        int chunkSize = 1000 // Define your chunk size here

        for (int i = 0; i < auditTrailList.size(); i += chunkSize) {
            chunkedLists.add(auditTrailList.subList(i, Math.min(i + chunkSize, auditTrailList.size())))
        }

        return chunkedLists
    }



    Criterion buildInCriterion(String propertyName, List values) {
        org.hibernate.criterion.Criterion criterion = null;
        int PARAMETER_LIMIT = 999;
        int listSize = values.size();
        for (int i = 0; i < listSize; i += PARAMETER_LIMIT) {
            List subList;
            if (listSize > i + PARAMETER_LIMIT) {
                subList = values.subList(i, (i + PARAMETER_LIMIT));
            } else {
                subList = values.subList(i, listSize);
            }
            if (criterion != null) {
                criterion = Restrictions.or(criterion, Restrictions.in(propertyName, subList));
            } else {
                criterion = Restrictions.in(propertyName, subList);
            }
        }
        return criterion;
    }

    private String getFormattedDateTime(Date date, String dateFormat, String timezone) {
        String out;
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat)
        sdf.setTimeZone(TimeZone.getTimeZone(timezone))
        try {
            out = sdf.format(date)
        } catch (ParseException e) {
            return date?.toString()
        }
        return out
    }
}
