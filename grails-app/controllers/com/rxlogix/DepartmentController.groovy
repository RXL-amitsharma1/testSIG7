package com.rxlogix

import com.rxlogix.user.UserDepartment
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured(["isAuthenticated()"])
class DepartmentController {

    def index() {
        render UserDepartment.findAll() as JSON
    }
}
