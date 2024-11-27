package com.rxlogix

import com.rxlogix.cache.CacheService
import com.rxlogix.config.Disposition
import com.rxlogix.signal.BusinessConfiguration
import com.rxlogix.signal.RuleInformation
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
//@TestFor(BusinessConfigurationController)
//@TestMixin(GrailsUnitTestMixin)
//@Mock([CacheService, BusinessConfiguration, QueryService, Disposition, BusinessConfigurationService, RuleInformation])
class BusinessConfigurationControllerSpec extends Specification implements ControllerUnitTest<BusinessConfigurationController> {

    BusinessConfiguration businessConfiguration
    Disposition disposition
    RuleInformation ruleInformation


    def setup() {
        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, reviewCompleted: true, abbreviation: "vs")
        disposition.save(failOnError: true)
        businessConfiguration = new BusinessConfiguration(id: 123L, ruleName: 'testRule', dataSource: 'pva', modifiedBy: 'username', createdBy: 'username', productSelection: '{}')
        businessConfiguration.save(failOnError: true)
        ruleInformation = new RuleInformation(ruleName: "TEST", ruleJSON: "{}")

        CRUDService service = Mock(CRUDService)
        controller.CRUDService = service

        BusinessConfigurationService configurationService = Mock(BusinessConfigurationService)
        configurationService.getSelectBoxValues(_) >> ["categories": ["type": "ALGORITHM", "text": "EBGM(Male)", "value": "EBGM_MALE"], "formatOptions": ["key": "EBGM", "value": "EBGM"], "textFields": ["LISTEDNESS", "DME"]]
        configurationService.getBusinessConfiguration(_) >> businessConfiguration
        configurationService.isOtherRuleEnabled(_) >> true
        controller.businessConfigurationService = configurationService

        CacheService cacheService = Mock(CacheService)
        cacheService.setRuleInformationCache(_) >> null
        controller.cacheService = cacheService
    }

    def cleanup() {
    }

    void "test fetchSelectBoxValues() for SUB_GROUP ALGORITHM"() {
        when:
        controller.fetchSelectBoxValues(123L)
        then:
        response.json.toString().contains('EBGM')
        response.json.toString().contains('EBGM(Male)')
    }

    void "test fetchSelectBoxValues() for SUB_GROUP AGE disabled"() {
        when:
        controller.fetchSelectBoxValues(123L)
        then:
        !response.json.toString().contains('EB05(Adult)')
        !response.json.toString().contains('EB05_CHILD')
    }

    void "test toggleEnableBusinessConfiguration() for Attribute true"() {
        when:
        controller.toggleEnableBusinessConfiguration(123L, true)
        then:
        response.redirectedUrl == '/businessConfiguration/index'
    }

    void "test toggleEnableBusinessConfiguration() for Attribute false"() {
        when:
        controller.toggleEnableBusinessConfiguration(123L, false)
        then:
        response.redirectedUrl == '/businessConfiguration/index'
    }

    void "test toggleEnableBusinessConfiguration() for OtherRule Enabled and attribute is false"() {
        when:
        controller.toggleEnableBusinessConfiguration(123L, false)
        then:
        response.redirectedUrl == '/businessConfiguration/index'
    }

    void "test toggleEnableBusinessConfiguration() for OtherRule Enabled and attribute is true"() {
        when:
        controller.toggleEnableBusinessConfiguration(123L, true)
        then:
        response.redirectedUrl == '/businessConfiguration/index'
    }

    void "test updateRule() success"() {
        setup:
        CRUDService crudServiceMock = Mock(CRUDService)
        crudServiceMock.update(_) >> ruleInformation
        controller.CRUDService = crudServiceMock
        when:
        controller.updateRule(123)
        then:
        response.getHeader('Location') == 'http://localhost:7171/signal/businessConfiguration/index'
    }

    void "test updateRule() failure"() {
        setup:
        CRUDService crudServiceMock = Mock(CRUDService)
        crudServiceMock.update(_) >> { throw new Exception() }
        controller.CRUDService = crudServiceMock
        when:
        controller.updateRule(123)
        then:
        view == '/businessConfiguration/editRule'
    }

    void "test saveBusinessConfiguration()"() {
        setup:
        businessConfiguration = new BusinessConfiguration(id: 127L, ruleName: 'testRule2', dataSource: 'pva', modifiedBy: 'username', createdBy: 'username', productSelection: '{"1":[],"2":[],"3":[{"name":"Calpol","id":"100060"}],"4":[],"5":[]}')
        businessConfiguration.save(failOnError: true)
        controller.params.productSelection = '{"1":[],"2":[],"3":[{"name":"Calpol","id":"100060"}],"4":[],"5":[]}'
        controller.params.dataSource = 'pva'
        controller.params.description = ""
        when:
        controller.saveBusinessConfiguration(businessConfiguration.id,businessConfiguration.ruleName)
        then:
        response.status == 302
        response.redirectedUrl == '/businessConfiguration/index'
    }

    void "test cloneRule"(){
        given:
            def mockCRUDService = Mock(CRUDService)
            mockCRUDService.save(_) >> {

            }
            controller.CRUDService = mockCRUDService
            def mockCacheService = Mock(CacheService)
            mockCacheService.setRuleInformationCache(_) >> {

            }
            controller.cacheService = mockCacheService
        when:
            controller.cloneRule(ruleInformation.id)
        then:
            response.status == 302
            response.redirectedUrl == '/businessConfiguration/index'

    }

}