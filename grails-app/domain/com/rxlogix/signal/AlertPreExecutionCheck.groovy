package com.rxlogix.signal

import com.rxlogix.user.User
import grails.gorm.dirty.checking.DirtyCheck

import static com.rxlogix.util.DateUtil.toDateString
import static com.rxlogix.util.DateUtil.toDateString

@DirtyCheck
class AlertPreExecutionCheck {

    static auditable = true

    Boolean isPvrCheckEnabled

    Boolean isVersionAsOfCheckEnabled

    Boolean isEtlFailureCheckEnabled
    Boolean isEtlInProgressCheckEnabled

    Boolean isEnabledForMasterConfig = true

    String createdBy
    Date dateCreated
    Date lastUpdated
    String modifiedBy
    Map<String, Object> customAuditProperties

    static transients = ['customAuditProperties']
    static hasMany = [alertPreCheckHistories: AlertPreExecutionCheckHistory]

    static mapping = {
        table name: "ALERT_PRE_EXECUTION_CHK"
        isPvrCheckEnabled column: "IS_PVR_CHK_ENABLED"
        isVersionAsOfCheckEnabled column: "IS_VER_ASOF_CHK_ENABLED"
        isEtlFailureCheckEnabled column: "IS_ETL_FAIL_CHK_ENABLED"
        isEtlInProgressCheckEnabled column: "IS_ETL_INPROG_CHK_ENABLED"
        isEnabledForMasterConfig column: "IS_ENABLED_FOR_MASTER_CFG"
    }
    static constraints = {

    }

    AlertPreExecutionCheck(boolean isPvrCheckEnabled, boolean isVersionAsOfCheckEnabled, boolean isEtlFailureCheckEnabled, boolean isEtlInProgressCheckEnabled, boolean isEnabledForMasterConfiguration, String createdBy, String modifiedBy) {
        this.isPvrCheckEnabled = isPvrCheckEnabled
        this.isVersionAsOfCheckEnabled = isVersionAsOfCheckEnabled
        this.isEtlFailureCheckEnabled = isEtlFailureCheckEnabled
        this.isEtlInProgressCheckEnabled = isEtlInProgressCheckEnabled
        this.isEnabledForMasterConfig = isEnabledForMasterConfiguration
        this.createdBy = createdBy
        this.modifiedBy = modifiedBy
    }

    def toDto(){
        [
                'isPvrCheckEnabled'                 :this.isPvrCheckEnabled,
                'isVersionAsOfCheckEnabled'         :this.isVersionAsOfCheckEnabled,
                'isEtlFailureCheckEnabled'          :this.isEtlFailureCheckEnabled,
                'isEtlInProgressCheckEnabled'       :this.isEtlInProgressCheckEnabled
        ]
    }

    def getInstanceIdentifierForAuditLog() {
        return "Alert Pre-Checks"
    }

    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        // custom audit properties added before CRUD operation
        for(Map.Entry customAuditEntry: this.customAuditProperties){
            if (customAuditEntry.getValue() != null && customAuditEntry.getValue() != "") {
                newValues.put(customAuditEntry.getKey(), customAuditEntry.getValue().newValue)
                oldValues.put(customAuditEntry.getKey(), customAuditEntry.getValue().oldValue)
            }
        }
        this.customAuditProperties=[:]
        return [newValues: newValues, oldValues: oldValues]
    }
}
