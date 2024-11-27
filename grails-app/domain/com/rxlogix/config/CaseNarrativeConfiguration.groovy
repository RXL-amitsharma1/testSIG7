package com.rxlogix.config

import grails.gorm.dirty.checking.DirtyCheck

/**
 This class implements singleton design pattern. It holds configuration data for Case Narrative export.
 */
@DirtyCheck
class CaseNarrativeConfiguration {

    static auditable = true

    // static instance variable to hold the singleton instance
    static CaseNarrativeConfiguration instance
    Boolean exportAlways = false
    Boolean promptUser = false

    static constraints = {
        exportAlways validator: { val, obj ->
            if (val && obj.promptUser)
                return "case.narrative.configuration.validator"
        }
        promptUser validator: { val, obj ->
            if (val && obj.exportAlways)
                return "case.narrative.configuration.validator"
        }
    }

    static mapping = {
        table name: "CASE_NARRATIVE_CONFIG"
        version false
    }
    /**
     * Private constructor to restrict instantiation from outside the class
     */
    private CaseNarrativeConfiguration() {
    }

    /**
     * Public static method to retrieve the singleton instance
     */
    static CaseNarrativeConfiguration getInstance() {
        if (instance == null && first() == null) {
            instance = new CaseNarrativeConfiguration().save(flush: true)
        } else {
            instance = first()
        }
        return instance
    }

    @Override
    String toString() {
        return "Case Narrative Configuration: \n exportAlways: " + this.exportAlways + "\n prompt: " + this.promptUser
    }

    def getInstanceIdentifierForAuditLog() {
        return "Case Narrative Configuration"
    }
}
