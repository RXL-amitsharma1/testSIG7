package com.rxlogix.util.marshalling

import com.rxlogix.config.Notification
import grails.converters.JSON

class NotificationMarshaller {
    void register() {
        JSON.registerObjectMarshaller(Notification) { Notification n ->
            def map = [:]
            map['id'] = n.id
            map['message'] = n.message
            map['user'] = n.user
            map['level'] = n.level.name
            map['dateCreated'] = n.dateCreated
            map['executedConfigId'] = n.executedConfigId
            return map
        }
    }
}
