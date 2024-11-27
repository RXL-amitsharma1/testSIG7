package com.rxlogix.util

import grails.gorm.dirty.checking.DirtyCheck
import groovy.json.JsonSlurper

@DirtyCheck
trait JsonUtil {

    def isJsonString(str) {
        try {
            def jsonSlurper = new JsonSlurper()
            jsonSlurper.parseText(str)

            return true
        } catch (Throwable t) {
        }

        return false
    }

    def parseJsonString(str) {
        try {
            def jsonSlurper = new JsonSlurper()
            jsonSlurper.parseText(str)
        } catch (all) {
            null
        }
    }
}
