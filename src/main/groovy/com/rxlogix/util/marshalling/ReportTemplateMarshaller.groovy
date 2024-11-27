package com.rxlogix.util.marshalling

import com.rxlogix.Constants
import com.rxlogix.config.ReportTemplate
import grails.converters.JSON

class ReportTemplateMarshaller {
    void register() {
        JSON.registerObjectMarshaller(ReportTemplate) { ReportTemplate template ->
            def map= [:]
            map['id'] = template.id
            map['category'] = template.category?.name?:" ";
            map['isPublic'] = template.isPublic
            map['name'] = template.name
            map['description'] = template.description
            map['dateCreated'] = template.dateCreated
//            map['selectedFieldsColumns'] = template.selectedFieldsColumns
            map['lastUpdated'] = template.lastUpdated
            map['tags'] = Constants.Commons.BLANK_STRING
            map['owner'] = template.owner
            map['createdBy'] = template.createdBy
            map['isDeleted'] = template.isDeleted
            map['checkUsage'] = template.countUsage()
            return map
        }
    }
}
