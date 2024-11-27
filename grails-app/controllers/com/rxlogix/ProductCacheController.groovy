package com.rxlogix

import grails.plugin.springsecurity.annotation.Secured

@Secured(["isAuthenticated()"])
class ProductCacheController {
    def show() {
    }
}
