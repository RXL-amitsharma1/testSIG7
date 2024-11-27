package com.rxlogix


import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["isAuthenticated()"])
class AlertFieldController {


    def alertFieldService

    def alertFields() {
        render(alertFieldService.alertFields('') as JSON)
    }
}
