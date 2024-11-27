package com.rxlogix

import com.rxlogix.config.Configuration
import com.rxlogix.config.EvdasConfiguration
import com.rxlogix.user.User
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationException
import groovy.mock.interceptor.MockFor
import spock.lang.Specification
import grails.plugin.springsecurity.SpringSecurityUtils

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
//@TestFor(ConfigurationController)
//@Mock([Configuration, User, EvdasConfiguration])
class ConfigurationControllerSpec extends Specification implements ControllerUnitTest<ConfigurationController> {

    Configuration configuration

    def setup() {
        configuration = new Configuration(id: 1, name: "TEST")
        configuration.save(validate: false)
    }

    void "test runOnce with already Running config"() {
        setup:
        controller.params.id = 1
        ConfigurationService configurationService = Mock(ConfigurationService)
        configurationService.fetchRunningAlertList(_) >> [1l, 2l]
        controller.configurationService = configurationService
        when:
        controller.runOnce()
        then:
        flash.warn == "app.configuration.running.fail"
    }

    void "test runOnce with success running"() {
        setup:
        controller.params.id = 1
        ConfigurationService configurationService = Mock(ConfigurationService)
        configurationService.fetchRunningAlertList(_) >> [11l, 2l,1l]
        controller.configurationService = configurationService
        CRUDService crudService = Mock(CRUDService)
        crudService.save(_) >> configuration
        controller.CRUDService = crudService
        when:
        controller.runOnce()
        then:
        flash.warn == "app.configuration.running.fail"
    }

    void "test runOnce for scheduled Config"() {
        setup:
        controller.params.id = 1
        configuration.nextRunDate = new Date()
        configuration.isEnabled = true
        when:
        controller.runOnce()
        then:
        flash.warn == "app.configuration.run.exists"
    }

    void "test delete when configuration found"(){
        User user = new User()
        user.save(validate:false)
        Configuration config = new Configuration(name:"config")
        config.owner = user
        config.save(validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){-> user}
        controller.userService = mockUserService.proxyInstance()
        user.metaClass.isAdmin = { -> return true }
        def mockConfigurationService = new MockFor(ConfigurationService)
        mockConfigurationService.demand.deleteConfig(0..1){Configuration config2-> config}
        controller.configurationService = mockConfigurationService.proxyInstance()
        when:
        controller.delete(config)
        then:
        flash.message == "app.configuration.alert.delete.success"
    }

    void "test copy when configuration found"(){
        User user = new User()
        user.save(validate:false)
        EvdasConfiguration config = new EvdasConfiguration(name:"config")
        config.owner = user
        config.save(validate:false)
        def mockUserService = new MockFor(UserService)
        mockUserService.demand.getUser(0..1){-> user}
        controller.userService = mockUserService.proxyInstance()
        user.metaClass.isAdmin = { -> return true }
        def mockEvdasConfigurationService = new MockFor(EvdasAlertService)
        mockEvdasConfigurationService.demand.deleteEvdasConfig(0..1){config2-> config}
        controller.evdasAlertService = mockEvdasConfigurationService.proxyInstance()
        when:
        controller.deleteEvdas(config)
        then:
        flash.message == "app.configuration.delete.success"
    }

}