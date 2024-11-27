package com.rxlogix.config

class EvaluationReferenceType {
    static auditable = false
    String name
    boolean display = true
    String description

    static constraints = {
        name(unique: true, blank: false)
        description(nullable: true)
    }

    static mapping = {
        table name: "EVAL_REF_TYPE"
        name column: "NAME"
        display column: "DISPLAY"
        description column: "DESCRIPTION"
        sort "name"
    }

}
