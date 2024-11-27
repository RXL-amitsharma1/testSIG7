package com.rxlogix.config

import com.rxlogix.enums.QueryOperatorEnum
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class QueryExpressionValue extends ParameterValue {

    ReportField reportField
    QueryOperatorEnum operator
    String operatorValue

    static mapping = {
        tablePerHierarchy false

        table name: "QUERY_EXP_VALUE"

        reportField column: "REPORT_FIELD_ID"
        operator column: "OPERATOR_ID"
    }
    static constraints = {
        operatorValue nullable: true
    }

    @Override
    String toString(){
        return "${reportField.toString()} ${operatorValue} ${value}"
    }

}
