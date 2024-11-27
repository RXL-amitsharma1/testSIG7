package com.rxlogix.config

import com.rxlogix.enums.AuditLogCategoryEnum
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class PvsAppConfiguration {
    static transients = ['skipAudit']

   def configurationService
    static auditable = true

    String key
    Boolean booleanValue
    String stringValue
    Boolean skipAudit = false

    static mapping = {
        table name: "PVS_APP_CONFIGURATION"

        version false
    }


    static constraints = {
        booleanValue nullable: true
        stringValue nullable: true
    }

    def getInstanceIdentifierForAuditLog() {
        switch (key){
            case "EnableAlertExecutionPreChecks":
                return "Alert Execution Pre Check Configurations"

            case "EnableSignalCharts":
                return "Signal Assessment Charts Configuration"

            case "UpdateDomainsAfterProductGroupUpdate":
                return "Product Group Update Configurations"

            default:
                return key;
        }
    }
}
