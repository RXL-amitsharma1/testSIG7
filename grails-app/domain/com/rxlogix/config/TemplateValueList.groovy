package com.rxlogix.config

import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class TemplateValueList extends ValueList {

    Long template

    static mapping = {
        table name: "TEMPLT_VALUE"
        template column: "RPT_TEMPLT_ID"
        tablePerHierarchy false
    }
}
