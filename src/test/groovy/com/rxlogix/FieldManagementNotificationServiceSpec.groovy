package com.rxlogix

import com.hazelcast.config.Config
import com.hazelcast.config.NetworkConfig
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.Message
import grails.testing.services.ServiceUnitTest
import grails.util.Holders
import spock.lang.Specification
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance


class FieldManagementNotificationServiceSpec extends Specification implements ServiceUnitTest<FieldManagementNotificationService>  {

    HazelcastInstance hazelcastInstance

    def setup() {

//        Config config = new Config()
//        config.setInstanceName("myHazelcastInstance")
//        // Configure network settings
//        NetworkConfig networkConfig = config.getNetworkConfig()
//        networkConfig.setPortAutoIncrement(true) // Enable auto-incrementation of ports
//        networkConfig.setPort(5701) // Set the starting port
//        // Add localhost as a member
//        networkConfig.getInterfaces().setEnabled(true).addInterface("127.0.0.1")
//
//        hazelcastInstance = Hazelcast.newHazelcastInstance(config)
    }

    def cleanup() {
//        if (hazelcastInstance != null) {
//            hazelcastInstance.shutdown()
//        }
    }

    def "Test onMessage with valid message"() {
        given:
        def service = new FieldManagementNotificationService()
        service.fieldManagementRefreshService = Mock(FieldManagementRefreshService)

        def message = Mock(Message)
        message.getMessageObject() >> '[{"fieldId": "caseNum", "tenantId": 1, "langId": "en"}]'

        when:
        service.onMessage(message)

        then:
        1 * service.fieldManagementRefreshService.processLabelMessage(_)
    }


    def "Test validateRequestData with valid request data"() {
        when:
        def result = service.validateRequestData([[fieldId: "caseNum", tenantId: 1, langId: "en"], [fieldId: "testUID", tenantId: 1, langId: "en"]])

        then:
        result == true
    }

    def "Test validateRequestData with invalid request data missing fieldId"() {
        when:
        def result = service.validateRequestData([[tenantId: 1, langId: "en"], [fieldId: "testUID", tenantId: 1, langId: "en"]])

        then:
        result == false
    }
    def "Test validateRequestData with invalid request data missing tenantId"() {
        when:
        def result = service.validateRequestData([[tenantId: null, langId: "en", fieldId:"tstUID"], [fieldId: "testUID", tenantId: 1, langId: "en"]])

        then:
        result == false
    }

    def "Test validateRequestData with invalid request data missing langId"() {
        when:
        def result = service.validateRequestData([[fieldId: "caseNum", tenantId: 1], [fieldId: "testUID", tenantId: 1, langId: "en"]])

        then:
        result == false
    }

    def "Test validateRequestData with empty request list"() {
        when:
        def result = service.validateRequestData([])

        then:
        result == false
    }

    def "Test validateRequestData with null request list"() {
        when:
        def result = service.validateRequestData(null)

        then:
        result == false
    }
}