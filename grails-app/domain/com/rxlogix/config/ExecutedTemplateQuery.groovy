package com.rxlogix.config

import com.rxlogix.BaseTemplateQuery
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class ExecutedTemplateQuery extends BaseTemplateQuery implements Serializable {
    Long executedTemplate
    String executedTemplateName
    String executedQueryName
    ExecutedDateRangeInformation executedDateRangeInformationForTemplateQuery
    Long executedQuery

    static belongsTo = [reportResult: ReportResult, executedConfiguration:ExecutedConfiguration]

    static hasMany = [executedQueryValueLists: ExecutedQueryValueList, executedTemplateValueLists: ExecutedTemplateValueList]

    static mapping = {
        table name: "EX_TEMPLT_QUERY"
        // workaround to pull in mappings from super class that is not a domain
        def superMapping = BaseTemplateQuery.mapping.clone()
        superMapping.delegate = delegate
        superMapping.call()
        executedQueryValueLists joinTable: [name: "EX_TEMPLT_QRS_EX_QUERY_VALUES", column: "EX_QUERY_VALUE_ID", key: "EX_TEMPLT_QUERY_ID"]
        executedTemplateValueLists joinTable: [name: "EX_TEMPLT_QRS_EX_TEMPLT_VALUES", column: "EX_TEMPLT_VALUE_ID", key: "EX_TEMPLT_QUERY_ID"]
        executedConfiguration column: "EX_RCONFIG_ID"
        executedTemplate column: "EX_TEMPLT_ID"
        executedTemplateName column: "EX_TEMPLT_NAME"
        executedQueryName column: "EX_QUERY_NAME"
        executedDateRangeInformationForTemplateQuery column: "EX_DATE_RANGE_INFO_ID"
        executedQuery column: "EX_QUERY_ID"
    }

    static constraints = {
        executedTemplate(nullable: false)
        executedQueryValueLists(nullable:true)
        executedDateRangeInformationForTemplateQuery(nullable:false)
        executedQuery(nullable: true)
        reportResult(nullable:true)
        executedTemplateName nullable: true
        executedQueryName nullable: true
    }

    @Override
    String toString(){
        return "Template name : ${this.executedTemplateName} ";
    }
}
