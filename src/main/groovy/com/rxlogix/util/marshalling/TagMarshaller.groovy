package com.rxlogix.util.marshalling

import com.rxlogix.config.Tag
import grails.converters.JSON

class TagMarshaller {
    void register() {
        JSON.registerObjectMarshaller(Tag) { Tag tag ->
            def map= [:]
            map['id'] = tag.id
            map['name'] = tag.name
            return map
        }
    }
}
