package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.util.MiscUtil

import grails.plugins.orm.auditable.AuditEntityIdentifier

class Priority implements Serializable {
    static auditable = [ignore:['dispositionConfigs','lastUpdated']]

    Long id
    String value
    @AuditEntityIdentifier
    String displayName
    String description
    String iconClass
    Boolean display = true
    Boolean defaultPriority = false
    int reviewPeriod
    int priorityOrder = 5
    List<PriorityDispositionConfig> dispositionConfigs = []
    Date lastUpdated
    Map<String, Object> customAuditProperties

    static transients = ['customAuditProperties']

    static hasMany = [dispositionConfigs: PriorityDispositionConfig]

    static constraints = {
        value unique: true, validator: { value, object ->
            return MiscUtil.validator(value, "Value", Constants.SpecialCharacters.DEFAULT_CHARS as String[])
        }
        displayName unique: true, validator: { value, object ->
            return MiscUtil.validator(value, "Display Name", Constants.SpecialCharacters.DEFAULT_CHARS as String[])
        }
        iconClass nullable: true, validator: { value, object ->
            return MiscUtil.validator(value, "Icon", Constants.SpecialCharacters.DEFAULT_CHARS as String[])
        }
        description nullable: true, validator: { value, object ->
            return MiscUtil.validator(value, "Description", Constants.SpecialCharacters.TEXTAREA_CHARS as String[])
        }
        reviewPeriod nullable: false
        priorityOrder nullable: false
        lastUpdated nullable: true
    }

    static mapping = {
        table name: "PRIORITY"
        sort "value"
    }

    Priority(String pv) {
        this.value = pv
    }

    @Override
    String toString() { value }

    def getEntityValueForDeletion(){
        return "Name-${displayName}"
    }

    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        // custom audit properties added before CRUD operation
        for(Map.Entry customAuditEntry: this.customAuditProperties){
            if (customAuditEntry.getValue() != null && customAuditEntry.getValue() != "") {
                newValues?.put(customAuditEntry.getKey(), customAuditEntry.getValue().newValue)
                oldValues?.put(customAuditEntry.getKey(), customAuditEntry.getValue().oldValue)
            }
        }
        this.customAuditProperties=[:]
        return [newValues: newValues, oldValues: oldValues]
    }
}
