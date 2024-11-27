package com.rxlogix.config

import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class CustomSQLValue extends ParameterValue {

    static mapping = {
        tablePerHierarchy false
        table name: "SQL_VALUE"
    }

    static constraints = {
        value (validator: {value ->
            if (value) {
                if (value.toLowerCase().contains("insert ")
                        || value.toLowerCase().contains("use ")
                        || value.toLowerCase().contains("alter ")
                        || value.toLowerCase().contains("desc ")
                        || value.toLowerCase().contains("create ")
                        || value.toLowerCase().contains("drop ")
                        || value.toLowerCase().contains("delete ")
                        || value.toLowerCase().contains("update ")
                        || value.contains(";"))
                    return "com.rxlogix.config.query.customSQLQuery.invalid"
            }
        })
    }
}
