package com.rxlogix

import com.rxlogix.cache.CacheService
import grails.plugin.springsecurity.annotation.Secured


@Secured(["isAuthenticated()"])
class EmailNotificationController {
    EmailNotificationService emailNotificationService
    CacheService cacheService

    @Secured(['ROLE_ADMIN','ROLE_CONFIGURATION_CRUD','ROLE_CONFIGURATION_VIEW'])
    def edit() {
        Map modules = emailNotificationService.getEmailNotificationModules()
        List<String> keys = emailNotificationService.getKeys()
        render(view: 'edit', model: [modules : modules, keys : keys])
    }

    @Secured(['ROLE_ADMIN','ROLE_CONFIGURATION_CRUD',' ROLE_CONFIGURATION_VIEW'])
    def update() {
        List enabledList = []
        List disabledList = []
        List<String> keys = emailNotificationService.getKeys()
        EmailNotification emailNotification = null

        keys.each {
            emailNotification = EmailNotification.findByKey(it)
            if(emailNotification) {
                if (params.get(it) == 'on') {
                    enabledList.add(emailNotification.id)
                    cacheService.setEmaiNotificationCache(it, true)
                } else {
                    disabledList.add(emailNotification.id)
                    cacheService.setEmaiNotificationCache(it, false)
                }
            }
        }
        emailNotificationService.bulkUpdateEmailNotifications(enabledList, disabledList)
        flash.message = message(code: 'app.email.notification.success')
        redirect(action: 'edit')
    }

}
