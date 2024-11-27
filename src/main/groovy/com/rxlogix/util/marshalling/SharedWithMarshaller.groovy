package com.rxlogix.util.marshalling

import com.rxlogix.config.SharedWith
import grails.converters.JSON

class SharedWithMarshaller {
    void register() {
        JSON.registerObjectMarshaller(SharedWith) { SharedWith status ->
            def map = [:]
            map['id'] = status.id
            map['status'] = status.status.value()
            map['user'] = status.user.fullName
            return map
        }
    }
}