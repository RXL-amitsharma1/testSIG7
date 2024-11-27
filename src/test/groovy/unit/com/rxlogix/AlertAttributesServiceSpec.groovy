package com.rxlogix

import grails.testing.services.ServiceUnitTest
import spock.lang.Ignore
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@Ignore
class AlertAttributesServiceSpec extends Specification implements ServiceUnitTest<AlertAttributesService> {

    def setup() {}

    def cleanup() {}

    void "test menu slurping"() {
        when:
        service.slurp()

        then:
        assert service.attributesMap instanceof Map
        assert service.attributesMap.size() == 9
    }

    void "test menu values"() {
        when:
        service.slurp()

        then:
        assert service.get('publicHealthImpact').size() == 3
        assert service.get('populationSpecific').size() == 6
    }

    void "test menu keys"() {
        when:
        service.slurp()

        then:
        assert service.attributeNames().size() == 9
    }

    void "test for null default menu value"(){
        when:
        service.slurp()

        then:
        assert service.getDefault("publicHealthImpact") == null
    }
}
