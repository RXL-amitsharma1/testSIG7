package com.rxlogix

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class DataBaseConnectionCheckInterceptorSpec extends Specification implements InterceptorUnitTest<DataBaseConnectionCheckInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test dataBaseConnectionCheck interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"dataBaseConnectionCheck")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
