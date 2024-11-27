/**
 * Helper class to implement Push-Notification using Spring-Websockets.
 * @author Aryal N
 * @version 1.0* @since 2019-09-17
 */
package com.rxlogix.helper

import com.rxlogix.Constants
import com.rxlogix.InboxLog
import grails.converters.JSON
import org.springframework.context.MessageSource
import org.springframework.context.NoSuchMessageException

import org.springframework.messaging.simp.SimpMessagingTemplate
//org.springframework.messaging.simp.SimpMessageSendingOperations

import org.springframework.stereotype.Service
import org.springframework.util.StringUtils

import java.util.logging.Logger

class NotificationHelper {

    SimpMessagingTemplate brokerMessagingTemplate
    MessageSource messageSource
    def cacheService

    Logger log = Logger.getLogger("NotificationHelper")


    /**
     * Prepares the Notification-Message and push the notification using Websocket Connection.
     * The connection is specific to users and is established using user-id. Ex: "/topic/{user.id}"
     * <p>
     * The InboxLog argument is reference to newly saved notification.
     * @param inboxLog
     * @return
     */
    def pushNotification(InboxLog inboxLog) {
        log.info(">> NotificationHelper.pushNotification()")
        try {
            prepareNotificationMessage(inboxLog)
        } catch (NoSuchMessageException ne) {
        }
        String queueName = Constants.NOTIFICATION_QUEUE + Long.toString(inboxLog.inboxUserId)
        brokerMessagingTemplate.convertAndSend queueName, (inboxLog as JSON).toString()
        inboxLog.discard()
        log.info(" message posted to :" + queueName)
        log.info("<< NotificationHelper.pushNotification()")
    }

    /**
     * Prepares the Notification-Message.
     * If the message-code is present, prepare message using message-code, arguments and locale.
     * If not, set subject as message.
     *
     * @param inboxLog
     * @return
     */
    private def prepareNotificationMessage(InboxLog inboxLog) throws NoSuchMessageException {
        Locale locale = cacheService.getPreferenceByUserId(inboxLog.inboxUserId)?.locale
        if (!StringUtils.isEmpty(inboxLog.getMessage())) {
            inboxLog.setMessage(messageSource.getMessage(inboxLog.getMessage(), [inboxLog.getMessageArgs()].toArray(),
                    locale))
        } else {
            inboxLog.setMessage(inboxLog.getSubject())
        }
    }

}