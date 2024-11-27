package com.rxlogix.config

class WorkflowVariable {
    static auditable = false

    String name
    String type
    String value

    static constraints = {
    }

    static mapping = {
        table("WORK_FLOW_VARIABLES")
    }
}
