package com.rxlogix

import grails.plugin.springsecurity.annotation.Secured

@Secured('Authenticated')
class HelpController {

    def index() {}
}
