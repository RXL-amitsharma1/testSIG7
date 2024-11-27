package com.rxlogix.api

import com.rxlogix.InboxLog
import com.rxlogix.InboxLogController
import com.rxlogix.config.Notification
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.user.User
import grails.converters.JSON
import grails.rest.RestfulController


class NotificationRestController extends RestfulController {

    def messageSource

    NotificationRestController() {
        super(Notification)

    }



    /**
     * Get notifications for a given User
     * @param User id
     * @return list of notifications for a given user
     */
    def forUser() {
        def id = params.id
        if (id) {
            def notificationList = Notification.where { notificationUserId == id }.list().collect() {
                def val = [it.messageArgs]
                def user = User.get(id)
                [id  : it.id, message: messageSource.getMessage(it.message, val.toArray(), user.preference.locale),

                 user: user, dateCreated: it.dateCreated, level: it.level.name, executedConfigId: it.executedConfigId, detailUrl: it.detailUrl]
                }
            respond notificationList, [formats: ['json']]
        }
    }

    /**
     * Delete notification by id
     * @param Notification id
     * @return boolean whether or not deletion was successful
     */
    def deleteNotificationById() {
        def id = params.id
        if (id) {
            try {
                def notification = Notification.get(id.toInteger())
                notification.delete(flush: true)
                render true
            } catch (Exception e) {
                log.info("Could not delete notification! $e.localizedMessage")
                render false
            }
        }
    }

    /**
     * Delete all notifications for user by id
     * @param User id
     * @return boolean whether or not deletion was successful
     */
    def deleteNotificationsForUserId() {
        def id = params.id
        if (id) {
            User user
            try {
                user = User.get(id)
                Notification.findAllByNotificationUserId(id)?.each {
                    it.delete(flush: true)
                }
                render true
            } catch (Exception e) {
                log.info("Could not delete notifications for user $user! $e.localizedMessage")
                render false
            }
        }
    }
}

