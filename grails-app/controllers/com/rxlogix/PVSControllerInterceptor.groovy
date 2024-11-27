package com.rxlogix

import grails.util.Holders

class PVSControllerInterceptor {

    PVSControllerInterceptor() {
        match(controller: ~/(console)/)
    }

    boolean before() {
        if((params.accessKey && params.accessKey.equals(Holders.config.pvs.controller.access.key2)) || actionName) {
            return true
        }
        false
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
