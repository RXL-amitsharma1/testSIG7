package com.rxlogix.util.marshalling

import com.rxlogix.Constants
import com.rxlogix.config.ReportResult
import grails.converters.JSON

class ReportResultMarshaller {
    void register() {
        JSON.registerObjectMarshaller(ReportResult) {
            ReportResult result ->
            def map = [:]
            map['id'] = result.id
            map['name'] = result.executedTemplateQuery.executedConfiguration.name
            map['isPublic'] = result.executedTemplateQuery.executedConfiguration.isPublic
            map['description'] = result.executedTemplateQuery.executedConfiguration.description
            map['owner'] = result.executedTemplateQuery.executedConfiguration.owner.fullName
            map['dateCreated'] = result.dateCreated
            map['lastUpdated'] = result.lastUpdated
            map['tags'] = Constants.Commons.BLANK_STRING
            map['version'] = result.sequenceNo
            map['executionStatus'] = result.executionStatus.value()
//            map['status'] = result.getStatusForUser()?.getStatus()?.value()
            map['runDate'] = result.runDate
            map['executionTime'] = result.totalTime
                map['configId'] = result.executedTemplateQuery.executedConfiguration.id
            return map
        }
    }
}
