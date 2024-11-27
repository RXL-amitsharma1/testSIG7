package com.rxlogix.api

import com.hazelcast.core.Cluster
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.ITopic
import com.hazelcast.core.Member
import com.rxlogix.Constants
import com.rxlogix.FieldManagementNotificationService
import com.rxlogix.dto.FieldManagementResponseDTO
import grails.converters.JSON
import grails.util.Holders
import groovy.json.JsonOutput
import org.springframework.http.HttpStatus


class FieldManagementRefreshController {

    FieldManagementNotificationService fieldManagementNotificationService

    static allowedMethods = [publishNotification: "POST", rollbackNotification:"POST"]

    def publishNotification() {
        FieldManagementResponseDTO fieldManagementResponseDTO = new FieldManagementResponseDTO()
        try {
            def jsonData = request.JSON
            String token = request.getHeader(Constants.publicTokens.PVS_TOKEN)
            if (token != Holders.config.pvs.public.token) {
                log.info("Invalid token received for field modification.")
                fieldManagementResponseDTO.resultCode = HttpStatus.FORBIDDEN.value()
                fieldManagementResponseDTO.resultStatus = HttpStatus.FORBIDDEN.name()
                fieldManagementResponseDTO.resultMsg = "User is not authenticated to perform this operation."
                fieldManagementResponseDTO.result.text = "Access token does not match."
                render fieldManagementResponseDTO as JSON
                return
            }
            if (fieldManagementNotificationService.validateRequestData(jsonData)) {
                HazelcastInstance hazelcastInstance = Hazelcast.getHazelcastInstanceByName(Holders.config.hazelcast.server.instance.name)
                log.info("hazelcast instance + " + hazelcastInstance)
                if (hazelcastInstance) {
                    ITopic<String> topic = hazelcastInstance.getReliableTopic(Constants.FIELD_REFRESH_TOPIC_NAME)
                    Integer retryCount = Holders.config.hazelcast.publish.retry.count ?: 10
                    Cluster cluster = hazelcastInstance.getCluster()
                    Set<Member> members = cluster.getMembers()
                    while (retryCount > 0) {
                        try {
                            topic.publish(JsonOutput.toJson(jsonData))
                            log.info("Message published successfully.All members in the cluster:" + members)
                            break
                        } catch (Exception exception) {
                            log.error("An error occurred when publishing message: " + exception)
                            retryCount--
                            Thread.sleep(120000)
                        }
                    }
                    log.info("*** published notification successfully ***")
                    fieldManagementResponseDTO.resultCode = Constants.fieldManagementCustom.TXN_SUCCESS.resultCode
                    fieldManagementResponseDTO.resultStatus = Constants.CommonUtils.SUCCESS
                    fieldManagementResponseDTO.resultMsg = "Success"
                    fieldManagementResponseDTO.result.text = "Data refreshed Successfully."
                } else {
                    log.info("Hazel cast instance not found.")
                    fieldManagementResponseDTO.resultCode = HttpStatus.INTERNAL_SERVER_ERROR.value()
                    fieldManagementResponseDTO.resultStatus = HttpStatus.INTERNAL_SERVER_ERROR.name()
                    fieldManagementResponseDTO.resultMsg = "Failure"
                    fieldManagementResponseDTO.result.text = "Exception Occurred while processing request. Error: Hazelcast instance not found"
                }
            } else {
                log.info("Error : Validation failed for request payload.")
                fieldManagementResponseDTO.resultCode = HttpStatus.BAD_REQUEST.value()
                fieldManagementResponseDTO.resultStatus = Constants.CommonUtils.INVALID_PARAM
                fieldManagementResponseDTO.resultMsg = "Invalid or insufficient parameters in the request"
                fieldManagementResponseDTO.result.text = "Validation failed for request data."
            }
        } catch (Exception exception) {
            log.info("Exception :" + exception)
            exception.printStackTrace()
            fieldManagementResponseDTO.resultCode = HttpStatus.INTERNAL_SERVER_ERROR.value()
            fieldManagementResponseDTO.resultStatus = HttpStatus.INTERNAL_SERVER_ERROR.name()
            fieldManagementResponseDTO.resultMsg = "Failure"
            fieldManagementResponseDTO.result.text = "Exception Occurred while processing request. Error: " + exception.getMessage()
        }
        render fieldManagementResponseDTO as JSON
    }

    def rollbackNotification() {
        FieldManagementResponseDTO fieldManagementResponseDTO = new FieldManagementResponseDTO()
        try {
            def jsonData = request.JSON
            String token = request.getHeader(Constants.publicTokens.PVS_TOKEN)
            if (token != Holders.config.pvs.public.token) {
                log.info("Invalid token received for field modification rollback.")
                fieldManagementResponseDTO.resultCode = HttpStatus.FORBIDDEN.value()
                fieldManagementResponseDTO.resultStatus = HttpStatus.FORBIDDEN.name()
                fieldManagementResponseDTO.resultMsg = "User is not authenticated to perform this operation."
                fieldManagementResponseDTO.result.text = "Access token does not match."
                render fieldManagementResponseDTO as JSON
                return
            }
            if (fieldManagementNotificationService.validateRequestData(jsonData)) {
                HazelcastInstance hazelcastInstance = Hazelcast.getHazelcastInstanceByName(Holders.config.hazelcast.server.instance.name)
                if (hazelcastInstance) {
                    ITopic<String> topic = hazelcastInstance.getReliableTopic(Constants.FIELD_REFRESH_TOPIC_NAME)
                    Integer retryCount = Holders.config.hazelcast.publish.retry.count ?: 10
                    Cluster cluster = hazelcastInstance.getCluster()
                    Set<Member> members = cluster.getMembers()
                    while (retryCount > 0) {
                        try {
                            topic.publish(JsonOutput.toJson(jsonData))
                            log.info("Rollback Message published successfully.All members in the cluster:" + members)
                            break
                        } catch (Exception exception) {
                            log.error("An error occurred when publishing rollback message: " + exception)
                            retryCount--
                            Thread.sleep(120000)
                        }
                    }
                    log.info("*** published rollback notification successfully ***")
                    fieldManagementResponseDTO.resultCode = Constants.fieldManagementCustom.TXN_SUCCESS.resultCode
                    fieldManagementResponseDTO.resultStatus = Constants.CommonUtils.SUCCESS
                    fieldManagementResponseDTO.resultMsg = "Success"
                    fieldManagementResponseDTO.result.text = "Data refreshed Successfully."
                } else {
                    log.info("Hazel cast instance not found.")
                    fieldManagementResponseDTO.resultCode = HttpStatus.INTERNAL_SERVER_ERROR.value()
                    fieldManagementResponseDTO.resultStatus = HttpStatus.INTERNAL_SERVER_ERROR.name()
                    fieldManagementResponseDTO.resultMsg = "Failure"
                    fieldManagementResponseDTO.result.text = "Exception Occurred while processing request. Error: Hazelcast instance not found"
                }
            } else {
                log.info("Error : Validation failed for request payload.")
                fieldManagementResponseDTO.resultCode = HttpStatus.BAD_REQUEST.value()
                fieldManagementResponseDTO.resultStatus = Constants.CommonUtils.INVALID_PARAM
                fieldManagementResponseDTO.resultMsg = "Invalid or insufficient parameters in the request"
                fieldManagementResponseDTO.result.text = "Validation failed for request data."
            }
        } catch (Exception exception) {
            log.info("Exception :" + exception)
            exception.printStackTrace()
            fieldManagementResponseDTO.resultCode = HttpStatus.INTERNAL_SERVER_ERROR.value()
            fieldManagementResponseDTO.resultStatus = HttpStatus.INTERNAL_SERVER_ERROR.name()
            fieldManagementResponseDTO.resultMsg = "Failure"
            fieldManagementResponseDTO.result.text = "Exception Occurred while processing request. Error: " + exception.getMessage()
        }
        render fieldManagementResponseDTO as JSON
    }
}