package com.rxlogix.util.marshalling
import com.rxlogix.config.ExecutedTemplateQuery
import grails.converters.JSON

class ExecutedTemplateQueryMarshaller {
    void register() {
        JSON.registerObjectMarshaller(ExecutedTemplateQuery
        ) { ExecutedTemplateQuery q ->
            def map = [:]
            map['id'] = q.id
            map['dateRangeInformationForTemplateQuery'] = q.executedDateRangeInformationForTemplateQuery
            map['queries'] = q.executedQuery
            map['queryValueLists'] = q.executedQueryValueLists
            map['template'] = q.executedTemplate
            map['results'] = q.reportResult
            map['executedConfiguration'] = q.executedConfiguration
            return map
        }
    }
}
