package com.rxlogix.signal

import com.rxlogix.user.Group
import grails.plugins.orm.auditable.AuditEntityIdentifier

class SignalWorkflowRule implements Serializable {
    static auditable = true

    @AuditEntityIdentifier
    String ruleName
    String description
    String fromState
    String toState
    boolean display = true
    transient skipAudit=false

    Date dateCreated

    static constraints = {
        description nullable: true
    }

    static mapping = {
        table("SIGNAL_WORKFLOW_RULE")
        allowedGroups joinTable: [name: "SIGNAL_WORKFLOWRULES_GROUPS", column: "GROUP_ID", key: "SIGNAL_WORKFLOW_RULE_ID"]
    }

    static hasMany = [allowedGroups: Group]

    def getEntityValueForDeletion(){
        return "Name-${ruleName}"
    }
}
