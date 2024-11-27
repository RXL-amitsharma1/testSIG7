package com.rxlogix.cache

import com.rxlogix.mapping.LmProduct
import grails.testing.services.ServiceUnitTest
import spock.lang.Ignore
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@Ignore
class GenericNameCacheServiceSpec extends Specification implements ServiceUnitTest<GenericNameCacheService> {
    def lmProducts

    def setup() {
       lmProducts = [new LmProduct(id: new BigDecimal("1000"), name:'IBUPROFEN', genericName: 'IBUPROFEN(200 mg)'),
            new LmProduct(id: new BigDecimal("1001"), name:'Med1', genericName: 'Generic1'),
            new LmProduct(id: new BigDecimal("1002"), name:'Med2', genericName: 'Generic2'),
            new LmProduct(id: new BigDecimal("1003"), name:'Med3', genericName: 'Generic2')]

    }

    def cleanup() {
    }

    void "test create cache" () {
        when:
        assert service.set(lmProducts)

        then:
        assert service.get().size() == 3

    }

    void "test get product names" () {
        when:
        assert service.set(lmProducts)
        def cache = service.get()

        then:
        assert cache.'Generic2'.size == 2
        assert cache.'Generic2'.collect{it.name} == ["Med2","Med3"]
    }
}
