package com.rxlogix.signal

import com.rxlogix.config.Disposition
import grails.plugins.orm.auditable.AuditEntityIdentifier

class SignalWorkflowState implements Serializable{
    static auditable = true

    String value
    @AuditEntityIdentifier
    String displayName
    boolean defaultDisplay = false
    boolean dueInDisplay = false

    static constraints = {
        value unique: true
        displayName unique: true
    }

    static mapping = {
        table("SIGNAL_WORKFLOW_STATE")
        allowedDispositions joinTable: [name: 'SIGNAL_WKFL_STATE_DISPOSITIONS', column: "DISPOSITION_ID", key: "SIGNAL_WORKFLOW_STATE_ID"]
    }

    static hasMany = [allowedDispositions : Disposition]
}
