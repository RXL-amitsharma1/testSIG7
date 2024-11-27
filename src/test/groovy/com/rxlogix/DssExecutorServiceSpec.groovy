package com.rxlogix

import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
//@TestFor(DssExecutorService)
class DssExecutorServiceSpec extends Specification implements ServiceUnitTest<DssExecutorService> {

    def setup() {
    }

    def cleanup() {
    }

    void "test something"() {
        expect:"fix me"
            true == false
    }
}
