package com.rxlogix


class HealthInterceptor {
    HealthInterceptor(){
        match(controller: "health", action: "*")
    }

    boolean before() {
        String configToken = grailsApplication.config.pvs.controller.access.key
        configToken? (params.accessKey == configToken) : true
    }
}