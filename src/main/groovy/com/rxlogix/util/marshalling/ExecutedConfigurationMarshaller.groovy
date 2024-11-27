package com.rxlogix.util.marshalling

import com.rxlogix.config.ExecutedConfiguration
import grails.converters.JSON

class ExecutedConfigurationMarshaller {
    void register() {
        JSON.registerObjectMarshaller(ExecutedConfiguration
        ) { ExecutedConfiguration q ->
            def map = [:]
            map['id'] = q.id
            map['name'] = q.name
            map['description'] = q.description
            map['owner'] = q.owner.fullName
            map['dateCreated'] = q.dateCreated
            map['isPublic'] = q.isPublic
            map['isDeleted'] = q.isDeleted
            map['templateQueries'] = q.executedTemplateQueries
            map['version'] = q.numOfExecutions
            return map
        }
    }
}
