package com.rxlogix.util.marshalling

import com.rxlogix.config.TemplateQuery
import grails.converters.JSON

class TemplateQueryMarshaller {
    void register() {
        JSON.registerObjectMarshaller(TemplateQuery
        ) { TemplateQuery q ->
            def map = [:]
            map['id'] = q.id
            map['dateRangeInformationForTemplateQuery'] = q.dateRangeInformationForTemplateQuery
            map['query'] = q.query
            map['template'] = q.template
            map['results'] = q.results
            map['configuration'] = q.report
            return map
        }
    }
}
