package com.rxlogix.signal

class SystemConfig {
    static auditable = true

    Boolean enableSignalWorkflow = false
    Boolean displayDueIn = false
    Boolean enableAutoPopulate = false
    Boolean enableEndOfMilestone = false
    String selectedEndPoints
    Boolean firstTime = false
    String dateClosedDisposition
    String dateClosedWorkflow
    String dateClosedDispositionWorkflow
    Boolean isDisposition = true
    transient Boolean skipAudit = false

    static mapping = {
        table name: "SYSTEM_CONFIG"
        enableSignalWorkflow column: "ENABLE_SIGNAL_WORKFLOW"
        version false
    }

    static constraints = {
        enableSignalWorkflow nullable: true
        displayDueIn nullable: true
        enableAutoPopulate nullable: true
        enableEndOfMilestone nullable: true
        selectedEndPoints nullable: true
        firstTime nullable: true
        dateClosedDisposition nullable: true
        dateClosedWorkflow nullable: true
        dateClosedDispositionWorkflow nullable: true
        isDisposition nullable: true
    }

    def getInstanceIdentifierForAuditLog() {
        return "Signal Configurations"
    }
}
