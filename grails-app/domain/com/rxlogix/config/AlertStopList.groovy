package com.rxlogix.config

import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.util.DbUtil
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class AlertStopList {
    static auditable = true

    def auditLogService

    String productName
    String eventName
    Date dateDeactivated
    Boolean activated = false

    //Common db table fields
    String createdBy
    Date dateCreated
    Date lastUpdated
    String modifiedBy

    def static TABLE_NAME = "ALERT_STOP_LIST"

    static mapping = {
        productName column: "PRODUCT_SELECTION", sqlType: DbUtil.longStringType
        eventName column: "EVENT_SELECTION", sqlType: DbUtil.longStringType
    }

    static constraints = {
        lastUpdated nullable: true
        modifiedBy nullable: true
        activated nullable: true
        dateDeactivated nullable: true
        productName(validator: {val, obj ->
            if (!val) {
                return "com.rxlogix.AlertStopList.productName.nullable"
            }
        },  nullable: true, maxSize:8192)
        eventName(validator: {val, obj ->
            if (!val) {
                return "com.rxlogix.AlertStopList.eventName.nullable"
            }
        },  nullable: true, maxSize:8192)
    }

    def detectChangesForAuditLog(theInstance, Map params, AuditLogCategoryEnum auditLogCategoryEnum) {
        List changesMade = auditLogService.detectChangesMade(theInstance, auditLogCategoryEnum)

        changesMade.flatten()
    }

}
