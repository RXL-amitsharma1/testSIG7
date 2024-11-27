package com.rxlogix.util.marshalling

import com.rxlogix.user.User
import grails.converters.JSON

class UserMarshaller {
    void register() {
        JSON.registerObjectMarshaller(User) {User user ->
            def map= [:]
            map['id'] = user.id
            map['username'] = user.username
            map['fullName'] = user.fullName
            return map
        }
    }
}
