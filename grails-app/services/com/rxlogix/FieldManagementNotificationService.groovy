package com.rxlogix


import grails.async.Promise
import grails.gorm.transactions.Transactional
import com.hazelcast.core.Hazelcast
import com.hazelcast.core.HazelcastInstance
import com.hazelcast.core.ITopic
import com.hazelcast.core.Message
import com.hazelcast.core.MessageListener
import grails.util.Holders
import com.rxlogix.json.JsonOutput
import groovy.json.JsonSlurper
import org.springframework.util.StringUtils

import static grails.async.Promises.task


@Transactional
class FieldManagementNotificationService implements MessageListener<String> {

    FieldManagementRefreshService fieldManagementRefreshService


    @Override
    void onMessage(Message<String> message) {
        log.info("Received message: ${message.getMessageObject()}")
        // Handle the notification
        String msg = message.getMessageObject()
        List uidDataList = convertJsonToList(msg)
        Promise promise = task {
            try {
                fieldManagementRefreshService.processLabelMessage(uidDataList)
            } catch (Exception exception) {
                exception.printStackTrace()
                throw exception
            }
        }
        promise.onComplete { log.info("ICR label Refresh successfully Completed. ") }

        promise.onError { log.error("An Error occurred while refreshing labels ") }
    }

    def subscribeHazelcastTopic() {
        try {
            HazelcastInstance hazelcastInstance = Hazelcast.getHazelcastInstanceByName(Holders.config.hazelcast.server.instance.name)
            if (hazelcastInstance) {
                // Get the topic
                ITopic<String> icrLabelTopic = hazelcastInstance.getReliableTopic(Constants.FIELD_REFRESH_TOPIC_NAME)
                // Add this service as a message listener to the topic
                icrLabelTopic.addMessageListener(this)
                log.info("Successfully subscribed to hazelcast topic: " + Constants.FIELD_REFRESH_TOPIC_NAME)
            } else {
                log.info("Hazelcast instance not found.")
            }
        } catch (Exception exception) {
            log.error("An exception occurred while subscribing to hazelcast: ")
            exception.printStackTrace()
        }
    }

    List convertJsonToList(String jsonString) {
        def slurper = new JsonSlurper()
        def data = slurper.parseText(jsonString)
        return data
    }

    Boolean validateRequestData(def requestList) {
        if (requestList && !requestList.isEmpty()) {
            for (it in requestList) {
                if (StringUtils.isEmpty(it["fieldId"]) || StringUtils.isEmpty(it["langId"]) || StringUtils.isEmpty(it["tenantId"])) {
                    return false
                }
            }
        } else {
            return false
        }
        return true
    }
}