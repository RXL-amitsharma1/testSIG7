package com.rxlogix

import com.rxlogix.audit.AuditTrail
import com.rxlogix.audit.AuditTrailChild
import com.rxlogix.config.Configuration
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.dto.ActivityDTO
import com.rxlogix.dto.AuditTrailChildDTO
import com.rxlogix.dto.AuditTrailDTO
import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.signal.ProductViewAssignment
import com.rxlogix.signal.UserViewAssignment
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.SignalQueryHelper
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.AuditLoggingConfigUtils
import grails.plugins.orm.auditable.RxAuditLogListener
import grails.util.Holders
import grails.validation.ValidationException
import groovy.sql.Sql
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang3.reflect.FieldUtils
import org.apache.poi.util.StringUtil
import org.grails.datastore.mapping.model.PersistentEntity
import org.hibernate.SessionFactory
import org.hibernate.StatelessSession
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.sql.JoinType
import org.joda.time.DateTime

import static grails.plugins.orm.auditable.RxAuditLogListener.*

@Transactional
class SignalAuditLogService {
    def cacheService
    def userService
    def productAssignmentService
    def dataObjectService
    SessionFactory sessionFactory
    def sessionFactory_faers
    def static dataSource
    def grailsApplication



    void updateAuditLog(def theInstance) {
        List<Map> changesMade = detectChangesMade(theInstance, AuditLogCategoryEnum.MODIFIED)
        List modifiedFields=changesMade.collect{
            it.fieldName
        }
        //this is done to prevent Audit Trail creation with empty childs
        if (theInstance instanceof ProductViewAssignment && modifiedFields.intersect(["hierarchy", "workflowGroup", "product", "usersAssigned", "groupsAssigned"]).size() == 0 ) {
            return
        }
        AuditTrail auditTrail = new AuditTrail()
        auditTrail.category = AuditTrail.Category.UPDATE.toString()
        auditTrail.applicationName = "PV Signal"
        auditTrail.entityId = theInstance.id.toString()
        auditTrail.entityName = theInstance.class.getSimpleName()
        auditTrail.username = userService.currentUserName ?: "System"
        auditTrail.fullname = userService.currentUserName ? cacheService.getUserByUserNameIlike(userService.currentUserName).fullName : ""
        auditTrail.transactionId=sessionFactory?.currentSession?.getSessionIdentifier() ?:theInstance?.class.getSimpleName()
        if(theInstance instanceof ProductViewAssignment){
            if(theInstance.product!=null){
                auditTrail.entityValue= JSON.parse(theInstance.product)?."name" +"(${theInstance.hierarchy})"
            }
            auditTrail.moduleName='Product Assignment'
        }
        auditTrail.save()
        saveAuditTrailChild(theInstance, auditTrail, AuditLogCategoryEnum.MODIFIED)
    }

    void deleteAuditLog(def theInstance) {
        AuditTrail auditTrail = new AuditTrail()
        auditTrail.category = AuditTrail.Category.DELETE.toString()
        auditTrail.applicationName = "PV Signal"
        auditTrail.entityId = theInstance.id.toString()
        auditTrail.entityName = theInstance.class.getSimpleName()
        if(theInstance instanceof ProductViewAssignment){
            Map groupMap = cacheService.getAllGroups()
            def wfGroup=theInstance.workflowGroup ? groupMap.get(theInstance.workflowGroup)?.name : ""
            List<Map> assignedUserOrGroupList = productAssignmentService.generateAssignedUserOrGroupList(theInstance.usersAssigned, theInstance.groupsAssigned)
            if(theInstance.product!=null){
                auditTrail.entityValue= JSON.parse(theInstance.product)?."name" +"(${theInstance.hierarchy}), Assignment-${assignedUserOrGroupList.collect { it.name }.join(", ")}, WorkFlow Group-${wfGroup?.replace("null","")}"
            }
            auditTrail.moduleName='Product Assignment'
            auditTrail.transactionId=sessionFactory?.currentSession?.getSessionIdentifier() ?:theInstance?.class.getSimpleName()
        }
        auditTrail.username = userService.currentUserName ?: "System"
        auditTrail.fullname = userService.currentUserName ? cacheService.getUserByUserNameIlike(userService.currentUserName).fullName : ""
        auditTrail.save()
        saveAuditTrailChild(theInstance, auditTrail, AuditLogCategoryEnum.DELETED)
    }
    void saveAuditLog(def theInstance) {
        AuditTrail auditTrail = new AuditTrail()
        auditTrail.category = AuditTrail.Category.INSERT.toString()
        auditTrail.applicationName = "PV Signal"
        auditTrail.entityId = theInstance.id.toString()
        auditTrail.entityName = theInstance.class.getSimpleName()
        if(theInstance instanceof ProductViewAssignment){
            if(theInstance.product!=null){
                auditTrail.entityValue= JSON.parse(theInstance.product)?."name" +"(${theInstance.hierarchy})"
            }
            auditTrail.moduleName='Product Assignment'
            auditTrail.transactionId=sessionFactory?.currentSession?.getSessionIdentifier() ?:theInstance?.class.getSimpleName()
        }
        auditTrail.username = userService.currentUserName ?: "System"
        auditTrail.fullname = userService.currentUserName ? cacheService.getUserByUserNameIlike(userService.currentUserName).fullName : ""
        auditTrail.save()
        saveAuditTrailChild(theInstance, auditTrail, AuditLogCategoryEnum.CREATED)
    }

    void saveAuditTrailChild(def theInstance, AuditTrail auditTrail, AuditLogCategoryEnum auditLogCategoryEnum) {
        List<Map> changesMade = detectChangesMade(theInstance, auditLogCategoryEnum)
        List<String> auditLogField = (theInstance instanceof ProductViewAssignment) ? ["hierarchy", "workflowGroup", "product", "usersAssigned", "groupsAssigned"] :  ["groupAssigned", "hierarchy", "products", "userAssigned", "workflowGroup"]
        changesMade.each {
            if (it.fieldName in auditLogField) {
                AuditTrailChild auditTrailChild = new AuditTrailChild()
                if (it.fieldName == 'userAssigned') {
                    if(it.newValue.isInteger())
                        auditTrailChild.newValue = cacheService.getUserByUserId(it.newValue as Integer)?.fullName
                    if (it.originalValue.isInteger())
                        auditTrailChild.oldValue = cacheService.getUserByUserId(it.originalValue as Integer)?.fullName
                } else if (it.fieldName == 'workflowGroup') {
                    if(it.newValue.isInteger())
                        auditTrailChild.newValue = cacheService.getGroupByGroupId(it.newValue as Integer)?.name
                    if (it.originalValue.isInteger())
                        auditTrailChild.oldValue = cacheService.getGroupByGroupId(it.originalValue as Integer)?.name
                } else {
                    auditTrailChild.newValue = it.newValue
                    auditTrailChild.oldValue = it.originalValue
                }
                auditTrailChild.propertyName = it.fieldName
                auditTrailChild.auditTrail = auditTrail
                auditTrailChild.save()
            }
        }
    }

    List detectChangesMade(theInstance, AuditLogCategoryEnum auditLogCategoryEnum) {
        List changesMade = []
        PersistentEntity persistentEntity = grailsApplication.mappingContext.getPersistentEntity(theInstance.class.name)
        List associations = persistentEntity.associations*.name

        if (auditLogCategoryEnum == AuditLogCategoryEnum.DELETED){
            def value = [fieldName    : "isDeleted",
                         originalValue: "No",
                         newValue     : "Yes"]
            changesMade << value
            return changesMade
        }
            def modifiedFieldNames = null
        if (auditLogCategoryEnum == AuditLogCategoryEnum.MODIFIED) {
            if (theInstance?.id) {
                modifiedFieldNames = theInstance.getDirtyPropertyNames()
                //code written below is for associations as sometimes association is not added in getDirtyPropertyNames followed from plugin
                associations.each { String associationName ->
                    def collection = theInstance."${associationName}"
                    if (collection?.respondsTo('isDirty') && collection?.isDirty()) {
                        modifiedFieldNames << associationName
                    }
                }
            }
        }

        if (auditLogCategoryEnum in [AuditLogCategoryEnum.CREATED, AuditLogCategoryEnum.DELETED]) {
            modifiedFieldNames = MiscUtil.getPersistentProperties(theInstance)
        }
        for (modifiedFieldName in modifiedFieldNames) {
            getValues(auditLogCategoryEnum, theInstance, modifiedFieldName, changesMade, associations)
        }
        changesMade
    }

    private void getValues(AuditLogCategoryEnum auditLogCategoryEnum, theInstance, name, ArrayList changesMade, def associations) {

        String originalValue
        if(name in associations){
            originalValue = getOldValueForMappingField(name, [entityId: theInstance?.id, entityName: theInstance.class.getSimpleName()])
        }else{
            originalValue = getOriginalValue(auditLogCategoryEnum, theInstance, name)
        }
        String newValue = getNewValue(auditLogCategoryEnum, theInstance, name)
        if ((originalValue != newValue || auditLogCategoryEnum == AuditLogCategoryEnum.DELETED) && validateModifiedEmptyValues(originalValue,newValue)) {
            def value = [fieldName    : name,
                         originalValue: originalValue,
                         newValue     : newValue]

            changesMade << value
        }
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

        originalValue as String ?: Constants.AuditLog.EMPTY_VALUE
    }

    private getNewValue(auditLogCategoryEnum, theInstance, name) {
        theInstance."$name" as String ?: Constants.AuditLog.EMPTY_VALUE
    }

    def save(List changesMade, theInstance, AuditLogCategoryEnum auditLogCategoryEnum, List auditChildList = [], Boolean isImportPersist = false) {
        AuditTrail auditTrail = new AuditTrail()
        String className = theInstance.class.getSimpleName()
        def userName=userService.currentUserName
        if(userName=="__grails.anonymous.user__" || userName=="" || userName==null){
            //this is added to change the user name when save call is made from external api and spring security return
            //username as __grails.anonymous.user__
            userName="SYSTEM"
        }
        auditTrail.category = auditLogCategoryEnum == AuditLogCategoryEnum.CREATED ? AuditTrail.Category.INSERT.toString() : AuditTrail.Category.UPDATE.toString()
        auditTrail.applicationName = "PV Signal"
        if(auditLogCategoryEnum == AuditLogCategoryEnum.DELETED){
            auditTrail.category = AuditTrail.Category.DELETE.toString()
        }
        auditTrail.entityId = theInstance.id.toString()
        auditTrail.entityName = StringUtils.uncapitalise(className)
        auditTrail.username =userName
        auditTrail.isFirstEntryInTransaction=true
        auditTrail.entityValue=getEntityValue(theInstance)
        auditTrail.description=auditTrail.category +" "+className
        auditTrail.moduleName= getCustomModuleName(className,theInstance) != "" ? getCustomModuleName(className,theInstance) : className
        auditTrail.transactionId = sessionFactory?.currentSession?.getSessionIdentifier() ?:className
        auditTrail.fullname = userService.currentUserName ? cacheService.getUserByUserNameIlike(userService.currentUserName)?.fullName : "SYSTEM"
        auditTrail.save()

        changesMade.each { it ->
            AuditTrailChild auditTrailChild = new AuditTrailChild()
            auditTrailChild.newValue = it.newValue
            auditTrailChild.oldValue = it.originalValue
            auditTrailChild.propertyName = it.fieldName
            auditTrailChild.auditTrail = auditTrail
            if(isImportPersist)
                auditChildList.add(auditTrailChild)
            else
                auditTrailChild.save()
        }
        auditTrail
    }

    def save(String id, Map instanceMap, AuditLogCategoryEnum auditLogCategoryEnum, List changesMade) {
        AuditTrail auditTrail = new AuditTrail()
        def userName=userService.currentUserName
        if(userName=="__grails.anonymous.user__" || userName=="" || userName==null){
            //this is added to change the user name when save call is made from external api and spring security return
            //username as __grails.anonymous.user__
            userName="SYSTEM"
        }
        auditTrail.category = AuditTrail.Category.DELETE.toString()
        auditTrail.applicationName = "PV Signal"
        auditTrail.entityId = id
        auditTrail.entityName = instanceMap.get('entityName')
        auditTrail.username = userName
        auditTrail.isFirstEntryInTransaction=true
        auditTrail.entityValue=instanceMap.get('name')
        auditTrail.description=auditTrail.category +" "+instanceMap.get('name')
        auditTrail.moduleName= instanceMap.get('moduleName')//getCustomModuleName(name,theInstance) != "" ? getCustomModuleName(className,theInstance) : className
        auditTrail.fullname = userService.currentUserName ? cacheService.getUserByUserNameIlike(userService.currentUserName)?.fullName : "SYSTEM"
        auditTrail.save()
        changesMade.each { it ->
            AuditTrailChild auditTrailChild = new AuditTrailChild()
            auditTrailChild.newValue = it.newValue
            auditTrailChild.oldValue = it.originalValue
            auditTrailChild.propertyName = it.fieldName
            auditTrailChild.auditTrail = auditTrail
            auditTrailChild.save()
        }
        auditTrail
    }

    def getEntityValue(def instance, AuditLogCategoryEnum auditLogCategoryEnum = null) {
        String value = null
        if (auditLogCategoryEnum == AuditLogCategoryEnum.DELETED && instance.respondsTo("getEntityValueForDeletion")) {
            value = instance.getEntityValueForDeletion()
            return value
        }
        FieldUtils.getFieldsListWithAnnotation(instance.getClass(), AuditEntityIdentifier.class)?.each {
            value = instance[it.name]?.toString()
        }
        if(!value && instance.respondsTo("getInstanceIdentifierForAuditLog")){
            value = instance.getInstanceIdentifierForAuditLog()
        }
        if (!value) value = ""
        return value.replaceAll(/\[\]/, '')
    }

    String getCustomModuleName(def className,def domain){
        className= StringUtils.uncapitalise(className)
        String customName=""
        if (domain.metaClass.respondsTo(domain, "getModuleNameForMultiUseDomains")){
            customName = domain.getModuleNameForMultiUseDomains()

            return customName
        }
        Map auditLogConfigDomain = AuditLoggingConfigUtils.getAuditConfigMap()?.get(className)
        if (auditLogConfigDomain && auditLogConfigDomain.containsKey("name")) {
            customName = auditLogConfigDomain.get("name")
        }
        return customName
    }

    Boolean validateModifiedEmptyValues(def oldValue, def newValue) {
        Boolean result = !((newValue == null || newValue == "" || newValue == "[]" || newValue == [] || newValue == [:]||newValue == "[:]"||newValue == "{}") &&
                (oldValue == null || oldValue == "" || oldValue == "[]"|| oldValue == [] || oldValue == [:]||oldValue == "[:]"||oldValue == "{}"))
        return result
    }
    /**
     *
     * @param criteriaSheet
     * @param entityValue
     * @param moduleName
     * @param params
     * @param fileName
     * @param infoMap
     * @return
     */

    def createAuditForExport(def criteriaSheet = null, def entityValue, def moduleName, Map params = null, def fileName, def infoMap = null) {
        log.info("Inside createAuditForExport method")
        try{
            AuditTrail auditTrail = new AuditTrail()
            def userName = userService.currentUserName ?: "SYSTEM"
            if(infoMap && infoMap.'userName'!=null && infoMap.'userFullName'!=null){
                //this is for the audit made by job
                auditTrail.username=infoMap.'userName'
                auditTrail.fullname=infoMap.'userFullName'
            }else{
                auditTrail.username = userName
                auditTrail.fullname = userService.currentUserName ? cacheService.getUserByUserNameIlike(userService.currentUserName)?.fullName : "SYSTEM"
            }

            if (infoMap && (infoMap?.'isGenerate' == "true" || infoMap?.'isGenerate' == true)) {
                auditTrail.category = AuditTrail.Category.INSERT.toString()
                auditTrail.moduleName = moduleName + ": Generate"
            } else {
                auditTrail.category = AuditTrail.Category.EXPORT.toString()
                auditTrail.moduleName = moduleName + ": Export"
            }
            auditTrail.applicationName = "PV Signal"
            auditTrail.entityName = moduleName
            auditTrail.entityId=11224 as Long
            auditTrail.description= "Export Triggered"
            auditTrail.isFirstEntryInTransaction=true
            auditTrail.entityValue=entityValue
            auditTrail.transactionId = sessionFactory?.currentSession?.getSessionIdentifier()

            auditTrail.save()
            List overrideList = [Constants.CriteriaSheetLabels.REPORT_GENERATED_BY,Constants.CriteriaSheetLabels.DATE_EXPORTED]
            if (criteriaSheet!=null) {
                criteriaSheet.each {
                    if(it.value!=null && it.value!="" && !overrideList.contains(it.label))
                    {
                        AuditTrailChild auditTrailChild = new AuditTrailChild()
                        auditTrailChild.newValue = it.value
                        auditTrailChild.oldValue = ""
                        auditTrailChild.propertyName = it.label.charAt(it.label.length() - 1) == ":" ? StringUtils.chop(it.label) : it.label
                        auditTrailChild.auditTrail = auditTrail
                        auditTrailChild.save()
                    }
                }
            }
            def tempMap=prepareChildDataForNonCriteria(params,fileName)
            tempMap?.each {k,v->
                    AuditTrailChild auditTrailChild = new AuditTrailChild()
                    auditTrailChild.newValue = v
                    auditTrailChild.oldValue = ""
                    auditTrailChild.propertyName = k
                    auditTrailChild.auditTrail = auditTrail
                    auditTrailChild.save()
            }

            auditTrail
        }catch(Exception ex) {
            log.info("Some error occured while saving audit log for export report ${moduleName}")
            ex.printStackTrace()
        }
    }

    def prepareChildDataForNonCriteria(def params,String fileName) {
        def criteriaMap = [:]
        if (params == null) {
            return criteriaMap
        }
        if (params.containsKey('outputFormat')) {
            criteriaMap.put('Output Format', params.outputFormat?.toLowerCase()) //PVS-53135
        }
        criteriaMap.put(Constants.CriteriaSheetLabels.DATE_EXPORTED, DateUtil.stringFromDateInUserTimeZone(new Date()))
        criteriaMap.put(Constants.CriteriaSheetLabels.REPORT_GENERATED_BY, userService.getUser()?.fullName)
        // removed as per https://rxlogixdev.atlassian.net/browse/PVS-47074 comment
        return criteriaMap
    }

    def saveAuditTrailForCategories(List<Map> existingRows, List<Map> newRows, String alertType, String entityValue, String entity, User currentUser = null){
        List oldMap = existingRows.collect {
            String catName = it?.category?.name
            String subCatName = it?.subcategory ? ("["+it?.subcategory*.name.join(',')+"]") : ""
            String privateStr = it?.private ? '(P)' : ''
            String alertStr = it?.alert ? '(A)' : ''
            return catName+subCatName+privateStr+alertStr
        }
        List newMap = newRows.collect {
            String catName = it?.category?.name
            String subCatName = it?.subcategory ? ("["+it?.subcategory*.name.join(',')+"]") : ""
            String privateStr = it?.private ? '(P)' : ''
            String alertStr = it?.alert ? '(A)' : ''
            return catName+subCatName+privateStr+alertStr
        }
        String category
        if(oldMap==[]){
            category = AuditTrail.Category.INSERT.toString()
        }else if(newMap==[]){
            category = AuditTrail.Category.DELETE.toString()
        }else{
            category = AuditTrail.Category.UPDATE.toString()
        }
        AuditTrail auditTrail = new AuditTrail(entityName: entity, entityValue: entityValue, category: category, username: userService.getCurrentUserName(), transactionId: sessionFactory?.currentSession?.getSessionIdentifier())
        if (category == AuditTrail.Category.DELETE.toString()) {
            auditTrail.entityValue = auditTrail.entityValue + ", Categories: ${oldMap.toString()}"
        }
        auditTrail.fullname = currentUser?.fullName ?: (userService.currentUserName ? cacheService.getUserByUserNameIlike(userService.currentUserName).fullName : "System")
        auditTrail.applicationName = "PV Signal"
        auditTrail.moduleName = alertType
        auditTrail.save()
        AuditTrailChild auditTrailChild = new AuditTrailChild()
        auditTrailChild.propertyName = "Category"
        auditTrailChild.oldValue = oldMap
        auditTrailChild.newValue = newMap
        auditTrailChild.auditTrail = auditTrail
        auditTrailChild.save()
    }

    def saveAuditTrailForComments(String oldComment, String newComment, Long exConfigId, String category = AuditTrail.Category.UPDATE.toString(), String caseNumberAndFollowUp = ""){
        ExecutedConfiguration executedConfiguration = ExecutedConfiguration.get(exConfigId)
        String entity = executedConfiguration.isLatest ? "Individual Case Review: Comments" : "Individual Case Review: Archived Alert: Comments"
        AuditTrail auditTrail = new AuditTrail(entityName: entity, entityValue: executedConfiguration.getInstanceIdentifierForAuditLog()+": ${caseNumberAndFollowUp}", category: category, username: userService.getCurrentUserName(), transactionId: sessionFactory?.currentSession?.getSessionIdentifier())
        if (category == AuditTrail.Category.DELETE.toString()) {
            auditTrail.entityValue=auditTrail.entityValue + ", Comment-${oldComment}"
        }
        auditTrail.fullname = userService.currentUserName ? cacheService.getUserByUserNameIlike(userService.currentUserName).fullName : "System"
        auditTrail.applicationName = "PV Signal"
        auditTrail.description = category=='INSERT' ? 'Created Alert Comment' : 'Changes for Alert Comment'
        auditTrail.moduleName = entity
        auditTrail.save()
        AuditTrailChild auditTrailChild = new AuditTrailChild()
        auditTrailChild.propertyName = "Comments"
        auditTrailChild.oldValue = oldComment
        auditTrailChild.newValue = newComment
        auditTrailChild.auditTrail = auditTrail
        auditTrailChild.save()
    }

    def saveAuditTrailForEvdasComments(String oldComment, String newComment, Long exConfigId, String category = AuditTrail.Category.UPDATE.toString(), String caseNumberAndFollowUp = ""){
        ExecutedEvdasConfiguration executedConfiguration = ExecutedEvdasConfiguration.get(exConfigId)
        String entity = executedConfiguration.isLatest ? "EVDAS Review: Comment" : "EVDAS Review : Archived Alert: Comment"
        AuditTrail auditTrail = new AuditTrail(entityName: entity, entityValue: executedConfiguration.getInstanceIdentifierForAuditLog()+": ${caseNumberAndFollowUp}", category: category, username: userService.getCurrentUserName(), transactionId: sessionFactory?.currentSession?.getSessionIdentifier())
        auditTrail.fullname = userService.currentUserName ? cacheService.getUserByUserNameIlike(userService.currentUserName).fullName : "System"
        auditTrail.applicationName = "PV Signal"
        auditTrail.description = category=='INSERT' ? 'Created Alert Comment' : 'Changes for Alert Comment'
        auditTrail.moduleName = entity
        auditTrail.save()
        AuditTrailChild auditTrailChild = new AuditTrailChild()
        auditTrailChild.propertyName = "Comments"
        auditTrailChild.oldValue = oldComment
        auditTrailChild.newValue = newComment
        auditTrailChild.auditTrail = auditTrail
        auditTrailChild.save()
    }

    void saveAuditTrailForBusinessRuleActions(Long executedConfigId){
        StatelessSession statelessSession = sessionFactory.openStatelessSession()
        try {
            if (executedConfigId) {
                List<AuditTrailDTO> auditTrailDTOList = dataObjectService.getBusinessRuleAuditTrailList(executedConfigId)
                if (auditTrailDTOList) {
                    def tx = statelessSession.beginTransaction()

                    def batchSize = 1000

                    for (AuditTrailDTO auditTrailDTO : auditTrailDTOList) {
                        int index = 0
                        AuditTrail auditTrail = new AuditTrail(auditTrailDTO.toMap())
                        statelessSession.insert(auditTrail)
                        List<AuditTrailChildDTO> auditTrailChildDTOList = auditTrailDTO.auditTrailChildDTOS
                        for (AuditTrailChildDTO auditTrailChildDTO : auditTrailChildDTOList) {
                            AuditTrailChild auditTrailChild = new AuditTrailChild(auditTrailChildDTO.toMap())
                            auditTrailChild.auditTrail = auditTrail
                            statelessSession.insert(auditTrailChild)
                        }
                        if (index % batchSize == 0) {
                            // Flush and clear the session to control memory usage
                            statelessSession.flush()
                        }
                        index++
                    }
                    tx.commit()
                }
            }
            log.info("Business Rule Actions Audit Log batch persisted.")
        }catch(Exception ex){
            ex.printStackTrace()
            throw new Exception("alert got failed while persisiting audit log data of BR ")
        }finally{
            dataObjectService.clearAuditTrailMap(executedConfigId)
            statelessSession?.close()
        }
    }

    void createAuditLog(Map auditTrailMap, def auditTrailChildMap,Boolean isEmail=false){
        log.info("Manual Audit Creaion Started")
        AuditTrail auditTrail = new AuditTrail(auditTrailMap)
        if(auditTrail.username==null){
            auditTrail.username = userService.getUser()?.getUsername() ?: "SYSTEM"
        }
        if(auditTrail.fullname==null && !isEmail) {
            auditTrail.fullname = userService.getUser()?.getFullName() ?: ""
        }
        if(auditTrail.username == Constants.Commons.SYSTEM){
            auditTrail.fullname = ""
            // setting fullName as empty for system entries
        }
        auditTrail.applicationName = "PV Signal"
        auditTrail.transactionId = sessionFactory?.currentSession?.getSessionIdentifier()
        auditTrail.save()
        AuditTrailChild auditTrailChild = null
        for (Map auditChild : auditTrailChildMap) {
            if (auditChild.oldValue != auditChild.newValue) {
                auditTrailChild = new AuditTrailChild(auditChild)
                auditTrailChild.auditTrail = auditTrail
                auditTrailChild.save()
            }
        }
    }

    def getActionJustification(String domain, Long id) {
        Sql sql = new Sql(getDataSource())
        String data = ""
        try {
            sql.eachRow(SignalQueryHelper.retrieve_justification_by_class_and_id(domain,id) as String) { row ->
                data = (row.JUSTIFICATION as String)
            }
        }catch (Exception e){
            e.printStackTrace()
        }finally {
            sql?.close()
        }
        data
    }

    static def getDataSource() {
        if (!dataSource) {
            def app = Holders.getGrailsApplication()
            dataSource = app.getMainContext().getBean('dataSource')
        }
        dataSource
    }

    def getOldValueForMappingField(String key,def instanceDetails) {
        //Code taken from RXAuditlog listner for mapping fields
        def fetchedValue = ""
        AuditTrailChild.withNewSession {
                try {
                    def list = AuditTrailChild.createCriteria().list() {
                        eq("propertyName", key)
                        'auditTrail'{
                            eq("entityId", instanceDetails?.entityId?.toString())
                            eq("entityName", instanceDetails?.entityName, [ignoreCase: true])
                        }
                        maxResults(1)
                        'order'("id", "desc")
                    }

                    fetchedValue = list.size() > 0 ? list?.first()?."newValue" : null
                    return fetchedValue
                } catch (ex) {
                    ex.printStackTrace()
                    fetchedValue = null
                }
            }
        }
}
