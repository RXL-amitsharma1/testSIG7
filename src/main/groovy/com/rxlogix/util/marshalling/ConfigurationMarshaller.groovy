package com.rxlogix.util.marshalling

import com.rxlogix.config.Configuration
import grails.converters.JSON

class ConfigurationMarshaller {
    void register() {
        JSON.registerObjectMarshaller(Configuration
        ) { Configuration q ->
            def map = [:]
            map['id'] = q.id
            map['name'] = q.name
            map['description'] = q.description
            map['createdBy'] = q.owner.fullName
            map['dateCreated'] = q.dateCreated
            map['isPublic'] = q.isPublic
            map['isDeleted'] = q.isDeleted
            map['templateQueries'] = q.templateQueries
            map['noOfExecution'] = q.numOfExecutions
            map['configId'] = q.id
            return map
        }
    }
}
