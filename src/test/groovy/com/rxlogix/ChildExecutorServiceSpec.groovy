package com.rxlogix

import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
//@TestFor(ChildExecutorService)
class ChildExecutorServiceSpec extends Specification implements ServiceUnitTest<ChildExecutorService> {

    def setup() {
    }

    def cleanup() {
    }

    void "test something"() {
        expect:"fix me"
            true == false
    }
}
