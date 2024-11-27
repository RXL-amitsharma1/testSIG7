package com.rxlogix

import com.rxlogix.config.Configuration
import com.rxlogix.config.EvdasConfiguration
import com.rxlogix.enums.AuditLogCategoryEnum
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import org.apache.commons.lang.StringUtils
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.web.context.request.RequestContextHolder

@Transactional
class CRUDService {

    def userService
    def signalAuditLogService

    def save(theInstance) {
        def params = RequestContextHolder?.requestAttributes?.params
        theInstance = userService.setOwnershipAndModifier(theInstance)
        //Support for adding pre-validation errors to errors stack
        def existingErrors = theInstance.errors
        if (theInstance.hasErrors() || !theInstance.save(failOnError: true, flush: true)) {
            theInstance.errors.addAllErrors(existingErrors)
            throw new ValidationException("Validation Exception", theInstance.errors)
        }
        theInstance
    }
    def saveWithFullUserName(theInstance) {
        theInstance = userService.setOwnershipAndModifierFullName(theInstance)
        //Support for adding pre-validation errors to errors stack
        def existingErrors = theInstance.errors
        if (theInstance.hasErrors() || !theInstance.save(failOnError: true, flush: true)) {
            theInstance.errors.addAllErrors(existingErrors)
            throw new ValidationException("Validation Exception", theInstance.errors)
        }
        theInstance
    }

    def update(theInstance,Boolean isDueDateUpdate = false, String modifiedBy="") {
        def params = RequestContextHolder?.requestAttributes?.params
        theInstance = userService.setOwnershipAndModifier(theInstance)
        if(isDueDateUpdate){
            theInstance.modifiedBy = modifiedBy
        }
        if (!theInstance.save(failOnError: true, flush: true) || theInstance.hasErrors()) {
            throw new ValidationException("Validation Exception", theInstance.errors)
        }

        return theInstance
    }
    def updateWithFullUserName(theInstance) {
        theInstance = userService.setOwnershipAndModifierFullName(theInstance)
        if (!theInstance.save(failOnError: true, flush: true) || theInstance.hasErrors()) {
            throw new ValidationException("Validation Exception", theInstance.errors)
        }

        return theInstance
    }

    def delete(theInstance)  {
        try {
            //Must flush session to force constraint violations, if any, and then respond with appropriate validation message
            theInstance.delete (failOnError: true, flush: true)
        } catch (DataIntegrityViolationException e) {
            throw new ValidationException("Validation Exception", theInstance.errors)
        }
    }

    def softDelete(theInstance) {
        def params = RequestContextHolder?.requestAttributes?.params
        theInstance = userService.setOwnershipAndModifier(theInstance)

        theInstance.isDeleted = true

        if (!theInstance.save(failOnError: true, flush: true)) {
            throw new ValidationException("Validation Exception", theInstance.errors)
        }

        theInstance
    }

    def saveWithoutAuditLog(theInstance) throws ValidationException {
        theInstance = userService.setOwnershipAndModifier(theInstance)
        if (!theInstance.save()) {
            throw new ValidationException("Validation Exception", theInstance.errors)
        }
    }

    def updateWithoutAuditLog(theInstance) throws ValidationException {
        theInstance = userService.setOwnershipAndModifier(theInstance)
        if (!theInstance.save(failOnError: true, flush: true)) {
            log.error("errors:" + theInstance.errors)
            throw new ValidationException("Validation Exception", theInstance.errors)
        }
    }


    def instanceSaveWithSystemUser(theInstance) throws ValidationException {
        theInstance = userService.setSystemUser(theInstance)
        if (!theInstance.save()) {
            throw new ValidationException("Validation Exception", theInstance.errors)
        }
    }

    def saveWithAuditLog(theInstance, List auditChildList = [], Boolean isImportPersist = false, def customEntriesList = []) {
        def params = RequestContextHolder?.requestAttributes?.params
        theInstance = userService.setOwnershipAndModifier(theInstance)
        def existingErrors = theInstance.errors
        if (theInstance.hasErrors() || !theInstance.save(failOnError: true, flush: true)) {
            theInstance.errors.addAllErrors(existingErrors)
            throw new ValidationException("Validation Exception", theInstance.errors)
        }

        List changesMade = []

        /* Note:  For new records, there is never a previous value so only the persisted values matter,
                  so capturing any changes made is done after a successful save.  */
        if (theInstance.metaClass.respondsTo(theInstance, "detectChangesForAuditLog", [theInstance.class, AuditLogCategoryEnum.class].toArray())) {
            changesMade = theInstance.detectChangesForAuditLog(theInstance, AuditLogCategoryEnum.CREATED)
        }
        if(customEntriesList!=null && customEntriesList.size()>0){
            changesMade.addAll(customEntriesList)
        }

        if (theInstance.metaClass.respondsTo(theInstance, "detectChangesForAuditLog", [theInstance.class, AuditLogCategoryEnum.class].toArray())) {
            signalAuditLogService.save(changesMade, theInstance, AuditLogCategoryEnum.CREATED, auditChildList, isImportPersist)
        }

        theInstance
    }

    def updateWithAuditLog(theInstance,def customEntries=[]) {
        def params = RequestContextHolder?.requestAttributes?.params

        List changesMade = []
        if (hasDetectChangesForAuditLog(theInstance)) {
            changesMade = theInstance.detectChangesForAuditLog(theInstance, AuditLogCategoryEnum.MODIFIED)
        }
        if (!theInstance.save(failOnError: true, flush: true) || theInstance.hasErrors()) {
            throw new ValidationException("Validation Exception", theInstance.errors)
        }
        if (hasDetectChangesForAuditLog(theInstance) && changesMade.size()>0) {
            if(customEntries!=null && customEntries.size()>0){
                changesMade.addAll(customEntries)
            }
            signalAuditLogService.save(changesMade, theInstance, AuditLogCategoryEnum.MODIFIED)
        }
        return theInstance
    }


    private boolean hasDetectChangesForAuditLog(theInstance, params = null) {
        return (theInstance.metaClass.respondsTo(theInstance, "detectChangesForAuditLog", [theInstance.class, AuditLogCategoryEnum.class].toArray())
                || (theInstance.metaClass.respondsTo(theInstance, "getTarget") && theInstance.getTarget().metaClass.respondsTo(theInstance, "detectChangesForAuditLog", [theInstance.class, AuditLogCategoryEnum.class].toArray())))
    }

    def deleteWithAuditLog(theInstance) {
        try {
            String id = theInstance.id.toString()
            String objName = ""
            String entityName= StringUtils.uncapitalise(theInstance.getClass().getSimpleName())
            Map instanceMap=[name:signalAuditLogService.getEntityValue(theInstance,AuditLogCategoryEnum.DELETED),entityName:entityName,moduleName:signalAuditLogService.getCustomModuleName(entityName, theInstance)]

            List changesMade = []
            Boolean isAuditable = hasDetectChangesForAuditLog(theInstance)
            if (isAuditable) {
                changesMade = theInstance.detectChangesForAuditLog(theInstance, AuditLogCategoryEnum.DELETED)
            }
            theInstance.delete(flush: true)
            if (isAuditable) {
                signalAuditLogService.save(id, instanceMap, AuditLogCategoryEnum.DELETED, changesMade)
            }
        } catch (DataIntegrityViolationException e) {
            e.printStackTrace()
            throw new ValidationException("Validation Exception", theInstance.errors)
        }
    }
    def softDeleteWithAuditLog(theInstance) {
        def params = RequestContextHolder?.requestAttributes?.params

        List changesMade = []
        if (hasDetectChangesForAuditLog(theInstance)) {
            changesMade = theInstance.detectChangesForAuditLog(theInstance, AuditLogCategoryEnum.MODIFIED)
        }
        if (!theInstance.save(failOnError: true, flush: true) || theInstance.hasErrors()) {
            throw new ValidationException("Validation Exception", theInstance.errors)
        }
        if (hasDetectChangesForAuditLog(theInstance) && changesMade.size()>0) {
            signalAuditLogService.save(changesMade, theInstance, AuditLogCategoryEnum.DELETED)
        }
        return theInstance
    }
}
