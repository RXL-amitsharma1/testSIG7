package com.rxlogix

import com.rxlogix.cache.CacheService
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class FieldManagementRefreshServiceSpec extends Specification implements ServiceUnitTest<FieldManagementRefreshService> {

    def setup() {
    }

    def cleanup() {
    }

    def "test processLabelMessage method with successful cache update"() {
        given:
        def cacheServiceMock = Mock(CacheService)
        service.cacheService = cacheServiceMock

        when:
        service.processLabelMessage([])

        then:
        1 * cacheServiceMock.updateUiLabelCacheForSafety(_) >> true
    }

//    def "test processLabelMessage method with unsuccessful cache update"() {
//        given:
//        def cacheServiceMock = Mock(CacheService)
//        service.cacheService = cacheServiceMock
//
//        when:
//        service.processLabelMessage([])
//
//        then:
//        10 * cacheServiceMock.updateUiLabelCacheForSafety(_) >> false
//    }

    def "Test processLabelMessage method"() {
        given:
        def cacheServiceMock = Mock(CacheService)
        cacheServiceMock.updateUiLabelCacheForSafety(_) >> false >> false >> true
        service.cacheService = cacheServiceMock


        when:
        service.processLabelMessage(null)

        then:
        1 * cacheServiceMock.updateUiLabelCacheForSafety(_) >> false
        1 * cacheServiceMock.updateUiLabelCacheForSafety(_) >> false
        1 * cacheServiceMock.updateUiLabelCacheForSafety(_) >> true
    }
}