package com.rxlogix.api

import com.hazelcast.config.NetworkConfig
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.ITopic
import com.rxlogix.Constants
import com.rxlogix.FieldManagementNotificationService
import grails.converters.JSON
import grails.testing.web.controllers.ControllerUnitTest
import groovy.json.JsonSlurper
import org.junit.Ignore
import org.springframework.http.HttpStatus
import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Specification
import grails.util.Holders
import com.hazelcast.config.Config

class FieldManagementRefreshControllerSpec extends Specification implements ControllerUnitTest<FieldManagementRefreshController> {


    HazelcastInstance hazelcastInstance

    def setup() {
        Config config = new Config()
        config.setInstanceName("myHazelcastInstance")
        // Configure network settings
        NetworkConfig networkConfig = config.getNetworkConfig()
        networkConfig.setPortAutoIncrement(true) // Enable auto-incrementation of ports
        networkConfig.setPort(5701) // Set the starting port

        // Add localhost as a member
        networkConfig.getInterfaces().setEnabled(true).addInterface("127.0.0.1")

        hazelcastInstance = Hazelcast.newHazelcastInstance(config)
    }

    def cleanup() {
        if (hazelcastInstance != null) {
            hazelcastInstance.shutdown()
        }
    }

    def "Test publishNotification with valid token and request"() {
        given:
        controller.fieldManagementNotificationService = Mock(FieldManagementNotificationService)
        def jsonData = [
                [fieldId: "caseNum", tenantId: 1, langId: "*"],
                [fieldId: "testUID", tenantId: 1, langId: "*"]
        ]
        request.method="POST"
        request.json = jsonData as JSON
        Holders.config.pvs.public.token = "testToken"
        request.addHeader(Constants.publicTokens.PVS_TOKEN, Holders.config.pvs.public.token)
        def mockFieldManagementNotificationService = Mock(FieldManagementNotificationService)
        mockFieldManagementNotificationService.validateRequestData(_) >> {true}

        controller.fieldManagementNotificationService = mockFieldManagementNotificationService
        Holders.config.hazelcast.server.instance.name = "myHazelcastInstance"

        when:
        def res = controller.publishNotification()

        then:
        println response
        println response.properties
        println res
        response.status == HttpStatus.OK.value()
        def jsonResponse = new JsonSlurper().parseText(response.text)
        jsonResponse.resultCode == HttpStatus.OK.value()
        jsonResponse.resultStatus == Constants.CommonUtils.SUCCESS
        jsonResponse.resultMsg == "Success"
        jsonResponse.result.text == "Data refreshed Successfully."
    }

    def "Test publishNotification with invalid token"() {
        given:
        controller.fieldManagementNotificationService = Mock(FieldManagementNotificationService)
        def jsonData = [
                [fieldId: "caseNum", tenantId: 1, langId: "*"],
                [fieldId: "testUID", tenantId: 1, langId: "*"]
        ]
        request.method="POST"
        request.json = jsonData as JSON
        Holders.config.pvs.public.token = "testToken"
        request.addHeader(Constants.publicTokens.PVS_TOKEN, "testToken2")
        def mockFieldManagementNotificationService = Mock(FieldManagementNotificationService)
        mockFieldManagementNotificationService.validateRequestData(_) >> {true}
        controller.fieldManagementNotificationService = mockFieldManagementNotificationService
        when:
        controller.publishNotification()

        then:
        response.status == HttpStatus.OK.value()
        def jsonResponse = new JsonSlurper().parseText(response.text)
        jsonResponse.resultCode == HttpStatus.FORBIDDEN.value()
        jsonResponse.resultStatus == HttpStatus.FORBIDDEN.name()
        jsonResponse.resultMsg == "User is not authenticated to perform this operation."
        jsonResponse.result.text == "Access token does not match."
    }

    def "Test publishNotification with invalid parameter"() {
        given:
        controller.fieldManagementNotificationService = Mock(FieldManagementNotificationService)
        def jsonData = [
                [fieldId: "caseNum", tenantId: 1, langId: "*"],
                [fieldId: "testUID", tenantId: 1, langId: "*"]
        ]
        request.method="POST"
        request.json = jsonData as JSON
        Holders.config.pvs.public.token = "testToken"
        request.addHeader(Constants.publicTokens.PVS_TOKEN, "testToken")
        def mockFieldManagementNotificationService = Mock(FieldManagementNotificationService)
        mockFieldManagementNotificationService.validateRequestData(_) >> {false}
        controller.fieldManagementNotificationService = mockFieldManagementNotificationService
        when:
        controller.publishNotification()

        then:
        response.status == HttpStatus.OK.value()
        def jsonResponse = new JsonSlurper().parseText(response.text)
        jsonResponse.resultCode == HttpStatus.BAD_REQUEST.value()
        jsonResponse.resultStatus == Constants.CommonUtils.INVALID_PARAM
        jsonResponse.resultMsg == "Invalid or insufficient parameters in the request"
        jsonResponse.result.text == "Validation failed for request data."
    }

    def "Test publishNotification when Hazelcast instance is not found"() {
        given:
        controller.fieldManagementNotificationService = Mock(FieldManagementNotificationService)
        def jsonData = [
                [fieldId: "caseNum", tenantId: 1, langId: "*"],
                [fieldId: "testUID", tenantId: 1, langId: "*"]
        ]
        request.method="POST"
        request.json = jsonData as JSON
        Holders.config.pvs.public.token = "testToken"
        request.addHeader(Constants.publicTokens.PVS_TOKEN, "testToken")
        Holders.config.hazelcast.server.instance.name = "test"
        def mockFieldManagementNotificationService = Mock(FieldManagementNotificationService)
        mockFieldManagementNotificationService.validateRequestData(_) >> {true}
        controller.fieldManagementNotificationService = mockFieldManagementNotificationService

        when:
        controller.publishNotification()

        then:
        response.status == HttpStatus.OK.value()
        def jsonResponse = new JsonSlurper().parseText(response.text)
        jsonResponse.resultCode == HttpStatus.INTERNAL_SERVER_ERROR.value()
        jsonResponse.resultStatus == HttpStatus.INTERNAL_SERVER_ERROR.name()
        jsonResponse.resultMsg == "Failure"
        jsonResponse.result.text == "Exception Occurred while processing request. Error: Hazelcast instance not found"
    }

    def "Test rollbackNotification with valid token and request"() {
        given:
        controller.fieldManagementNotificationService = Mock(FieldManagementNotificationService)
        def jsonData = [
                [fieldId: "caseNum", tenantId: 1, langId: "*"],
                [fieldId: "testUID", tenantId: 1, langId: "*"]
        ]
        request.method="POST"
        request.json = jsonData as JSON
        Holders.config.pvs.public.token = "testToken"
        request.addHeader(Constants.publicTokens.PVS_TOKEN, Holders.config.pvs.public.token)
        def mockFieldManagementNotificationService = Mock(FieldManagementNotificationService)
        mockFieldManagementNotificationService.validateRequestData(_) >> {true}

        controller.fieldManagementNotificationService = mockFieldManagementNotificationService
        Holders.config.hazelcast.server.instance.name = "myHazelcastInstance"

        when:
        def res = controller.rollbackNotification()

        then:
        println response
        println response.properties
        println res
        response.status == HttpStatus.OK.value()
        def jsonResponse = new JsonSlurper().parseText(response.text)
        jsonResponse.resultCode == HttpStatus.OK.value()
        jsonResponse.resultStatus == Constants.CommonUtils.SUCCESS
        jsonResponse.resultMsg == "Success"
        jsonResponse.result.text == "Data refreshed Successfully."
    }

    def "Test rollbackNotification with invalid token"() {
        given:
        controller.fieldManagementNotificationService = Mock(FieldManagementNotificationService)
        def jsonData = [
                [fieldId: "caseNum", tenantId: 1, langId: "*"],
                [fieldId: "testUID", tenantId: 1, langId: "*"]
        ]
        request.method="POST"
        request.json = jsonData as JSON
        Holders.config.pvs.public.token = "testToken"
        request.addHeader(Constants.publicTokens.PVS_TOKEN, "testToken2")
        def mockFieldManagementNotificationService = Mock(FieldManagementNotificationService)
        mockFieldManagementNotificationService.validateRequestData(_) >> {true}
        controller.fieldManagementNotificationService = mockFieldManagementNotificationService
        when:
        controller.rollbackNotification()

        then:
        response.status == HttpStatus.OK.value()
        def jsonResponse = new JsonSlurper().parseText(response.text)
        jsonResponse.resultCode == HttpStatus.FORBIDDEN.value()
        jsonResponse.resultStatus == HttpStatus.FORBIDDEN.name()
        jsonResponse.resultMsg == "User is not authenticated to perform this operation."
        jsonResponse.result.text == "Access token does not match."
    }

    def "Test rollbackNotification with invalid parameter"() {
        given:
        controller.fieldManagementNotificationService = Mock(FieldManagementNotificationService)
        def jsonData = [
                [fieldId: "caseNum", tenantId: 1, langId: "*"],
                [fieldId: "testUID", tenantId: 1, langId: "*"]
        ]
        request.method="POST"
        request.json = jsonData as JSON
        Holders.config.pvs.public.token = "testToken"
        request.addHeader(Constants.publicTokens.PVS_TOKEN, "testToken")
        def mockFieldManagementNotificationService = Mock(FieldManagementNotificationService)
        mockFieldManagementNotificationService.validateRequestData(_) >> {false}
        controller.fieldManagementNotificationService = mockFieldManagementNotificationService
        when:
        controller.rollbackNotification()

        then:
        response.status == HttpStatus.OK.value()
        def jsonResponse = new JsonSlurper().parseText(response.text)
        jsonResponse.resultCode == HttpStatus.BAD_REQUEST.value()
        jsonResponse.resultStatus == Constants.CommonUtils.INVALID_PARAM
        jsonResponse.resultMsg == "Invalid or insufficient parameters in the request"
        jsonResponse.result.text == "Validation failed for request data."
    }

    def "Test rollbackNotification when Hazelcast instance is not found"() {
        given:
        controller.fieldManagementNotificationService = Mock(FieldManagementNotificationService)
        def jsonData = [
                [fieldId: "caseNum", tenantId: 1, langId: "*"],
                [fieldId: "testUID", tenantId: 1, langId: "*"]
        ]
        request.method="POST"
        request.json = jsonData as JSON
        Holders.config.pvs.public.token = "testToken"
        request.addHeader(Constants.publicTokens.PVS_TOKEN, "testToken")
        def mockFieldManagementNotificationService = Mock(FieldManagementNotificationService)
        mockFieldManagementNotificationService.validateRequestData(_) >> {true}
        Holders.config.hazelcast.server.instance.name = "test"
        controller.fieldManagementNotificationService = mockFieldManagementNotificationService

        when:
        controller.rollbackNotification()

        then:
        response.status == HttpStatus.OK.value()
        def jsonResponse = new JsonSlurper().parseText(response.text)
        jsonResponse.resultCode == HttpStatus.INTERNAL_SERVER_ERROR.value()
        jsonResponse.resultStatus == HttpStatus.INTERNAL_SERVER_ERROR.name()
        jsonResponse.resultMsg == "Failure"
        jsonResponse.result.text == "Exception Occurred while processing request. Error: Hazelcast instance not found"
    }

}