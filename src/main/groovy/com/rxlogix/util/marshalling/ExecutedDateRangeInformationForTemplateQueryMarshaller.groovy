package com.rxlogix.util.marshalling
import com.rxlogix.config.ExecutedDateRangeInformation
import grails.converters.JSON

class ExecutedDateRangeInformationForTemplateQueryMarshaller {
    void register() {
        JSON.registerObjectMarshaller(ExecutedDateRangeInformation) { ExecutedDateRangeInformation q ->
            def map = [:]
            map['id'] = q.id
            map['dateRangeStartAbsolute'] = q.dateRangeStartAbsolute
            map['dateRangeEndAbsolute'] = q.dateRangeEndAbsolute
            map['dateRangeType'] = q.dateRangeEnum
            return map
        }
    }
}
