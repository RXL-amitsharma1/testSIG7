package com.rxlogix.helper

import grails.web.mapping.LinkGenerator

trait LinkHelper {
    LinkGenerator grailsLinkGenerator

    String createHref(String controller, String action, params){
        def url = [grailsLinkGenerator.serverBaseURL, controller, action].join("/")
        if(params){
            return url + "?" + params.collect { k,v -> "$k=$v" }.join('&')
        }
        return url
    }

    String createCustomHref(String baseUrl, Map params) {
        return baseUrl + "?" + params.collect { k, v -> "$k=$v" }.join('&')
    }
}