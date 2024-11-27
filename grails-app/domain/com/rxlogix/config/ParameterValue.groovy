package com.rxlogix.config
import com.rxlogix.util.DbUtil
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class ParameterValue {
    static auditable = false
    String value
    String key

    static mapping = {
        tablePerHierarchy false
        table name: "PARAM"
        id column: "ID"

        key column : "LOOKUP" // "KEY" is a reserved word DB's
        value column: "VALUE", sqlType: DbUtil.longStringType
    }

    static constraints = {
        value(nullable: true)
    }
}
