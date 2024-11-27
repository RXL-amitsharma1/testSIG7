package com.rxlogix.signal

import com.rxlogix.config.AdvancedFilter
import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.MiscUtil
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class ViewInstance {
    static auditable = [ignore:['iconSeq','keyId','tempColumnSeq','defaultValue','alertType']]

    def viewInstanceService
    Boolean skipAudit = true
    String name
    String alertType
    String filters
    String sorting
    User user
    Boolean defaultValue = false //this field cannot be used for audit purpose dur to complex backend logic
    String columnSeq
    String tempColumnSeq
    AdvancedFilter advancedFilter
    List<User> shareWithUser = []
    List<Group> shareWithGroup = []
    String iconSeq
    Long keyId
    Map<String, Object> customAuditProperties
    String selectedDispositions

    static transients = ['customAuditProperties','skipAudit']
    static mapping = {
        columnSeq type: "text", sqlType: "clob"
        tempColumnSeq type: "text", sqlType: "clob"
        shareWithUser joinTable: [name: "SHARE_WITH_USER_VIEW", column: "USER_ID", key: "VIEW_INSTANCE_ID"]
        shareWithGroup joinTable: [name: "SHARE_WITH_GROUP_VIEW", column: "GROUP_ID", key: "VIEW_INSTANCE_ID"]
    }

    static constraints = {
        name unique: ['user', 'alertType']
        filters nullable: true
        sorting nullable: true
        user nullable: true
        tempColumnSeq nullable: true
        advancedFilter nullable: true
        iconSeq nullable: true
        keyId nullable: true
        selectedDispositions nullable: true
    }

    Set<User> getShareWithUsers() {
        Set<User> users = ViewInstance.executeQuery("select shareWithUser from ViewInstance v where v.id = :id", [id: this.id])?.sort {
            it.name
        } as Set
        return users.grep()
    }

    Set<Group> getShareWithGroups() {
        Set<Group> userGroups = []
        if (this.shareWithGroup) {
            userGroups.addAll(this.shareWithGroup)
        }
        return userGroups
    }

    Boolean isViewShared() {
        this.shareWithUsers || this.shareWithGroups
    }

    Boolean isViewUpdateAllowed(User currentUser) {
        currentUser.isAdmin() || (!isViewShared() && this.userId == currentUser.id)
    }

//    List detectChangesForAuditLog(theInstance, AuditLogCategoryEnum auditLogCategoryEnum) {
//        List changesMade = viewInstanceService.detectChangesMade(theInstance, auditLogCategoryEnum)
//        changesMade.flatten()
//    }

    String toString() {
        "${this.name} (${this.alertType})"
    }

    def changeAlertTypeAsUI(String inputString) {
        return inputString?.
                replaceAll(com.rxlogix.Constants.AlertConfigType.AGGREGATE_CASE_ALERT, com.rxlogix.Constants.AlertType.AGGREGATE_ALERT).
                replaceAll("on Demand", "(Adhoc)").
                replaceAll(com.rxlogix.Constants.AlertConfigType.SINGLE_CASE_ALERT, com.rxlogix.Constants.AlertConfigType.INDIVIDUAL_CASE_ALERT)
    }

    def getInstanceIdentifierForAuditLog() {
        return name + " (${changeAlertTypeAsUI(alertType)})"
    }

    def getEntityValueForDeletion(){
        return "View Name-${name}, Share With User-${shareWithUser}, Share With Groups-${shareWithGroup}, Owner-${user}, Default View-${customAuditProperties.defaultView?"Yes":"No"}"
    }

    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        if (newValues && (oldValues == null)) {
            if (newValues.containsKey("columnSeq")) {
                newValues.put('columnSeq', viewInstanceService.generatePrimaryFields(newValues.get('columnSeq'), alertType))
            }
            if (newValues.containsKey("sorting")) {
                newValues.put('sorting', viewInstanceService.generateSortingMap(newValues.get('sorting'), this.columnSeq, viewInstanceService.fixedColumns(this)))
            }
            if (newValues.containsKey("filters")) {
                newValues.put('filters', viewInstanceService.generateFiltersMap(newValues.get('filters'), this.columnSeq, viewInstanceService.fixedColumns(this)))
            }
        }
        if (newValues && oldValues) {
            if (this.dirtyPropertyNames?.contains("columnSeq")) {
                newValues.put('columnSeq', viewInstanceService.generatePrimaryFields(newValues.get('columnSeq'), alertType))
                oldValues.put("columnSeq", viewInstanceService.generatePrimaryFields(this.getPersistentValue("columnSeq"), alertType))
            }
            if (this.dirtyPropertyNames?.contains("sorting")) {
                newValues.put('sorting', viewInstanceService.generateSortingMap(newValues.get('sorting'), this.columnSeq, viewInstanceService.fixedColumns(this)))
                oldValues.put("sorting", viewInstanceService.generateSortingMap(this.getPersistentValue("sorting"), this.columnSeq, viewInstanceService.fixedColumns(this)))
            }
            if (this.dirtyPropertyNames?.contains("filters")) {
                newValues.put('filters', viewInstanceService.generateFiltersMap(newValues.get('filters'), this.columnSeq, viewInstanceService.fixedColumns(this)))
                oldValues.put("filters", viewInstanceService.generateFiltersMap(this.getPersistentValue("filters"), this.columnSeq, viewInstanceService.fixedColumns(this)))
            }

        }


        // custom audit properties added before CRUD operation
        for(Map.Entry customAuditEntry: this.customAuditProperties){
            if (customAuditEntry.getValue() != null && customAuditEntry.getValue() != "") {
                newValues.put(customAuditEntry.getKey(), customAuditEntry.getValue())
                if(!oldValues?.isEmpty()){
                    oldValues?.put(customAuditEntry.getKey(), "N/A")
                }
            }
        }
        this.customAuditProperties=[:]
        return [newValues: newValues, oldValues: oldValues]
    }
}
