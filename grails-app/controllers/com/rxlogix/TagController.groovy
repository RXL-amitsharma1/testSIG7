package com.rxlogix

import com.rxlogix.config.Tag
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["isAuthenticated()"])
class TagController {

    def index() {
        render Tag.findAll() as JSON
    }
}
