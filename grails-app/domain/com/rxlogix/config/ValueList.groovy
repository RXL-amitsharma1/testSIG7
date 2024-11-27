package com.rxlogix.config

import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class ValueList {
    static auditable = false
    List<ParameterValue> parameterValues

    static hasMany = [parameterValues: ParameterValue]

    static mapping = {
        tablePerHierarchy false
        table name: "VALUE"
        parameterValues joinTable:[name: "VALUES_PARAMS", column: "PARAM_ID", key: "VALUE_ID"], indexColumn: [name: "PARAM_IDX"]
    }

    static constraints = {
        parameterValues (cascade: 'all-delete-orphan', validator: { values ->
            boolean hasValues = true
            values.each {
                if (!it.validate()) {
                    hasValues = false
                }
            }
            return hasValues
        })
    }

}
