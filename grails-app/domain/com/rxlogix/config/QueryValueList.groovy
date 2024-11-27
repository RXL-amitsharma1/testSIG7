package com.rxlogix.config

import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class QueryValueList extends ValueList {
    Long query
    String queryName

    static mapping = {
        tablePerHierarchy false

        table name: "QUERY_VALUE"
        query column: "SUPER_QUERY_ID"
    }
    @Override
    String toString() {
        //overriden this as inly used for audit purpose
        return queryName + ": " + this?.parameterValues*.toString()?.join(',')
    }
    String toLogParameterString(){
        return this?.parameterValues*.toString()?.join(',')
    }
}
