package com.rxlogix

import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured('Authenticated')
class WhatIsNewController {
    def getWhatIsNew() {
        def whatIsNew = []
        render whatIsNew as JSON
    }
}