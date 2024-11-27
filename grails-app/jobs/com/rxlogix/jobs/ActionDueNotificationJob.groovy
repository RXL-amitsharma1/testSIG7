package com.rxlogix.jobs

import com.rxlogix.EmailNotificationService
import grails.util.Holders

class ActionDueNotificationJob {

    def actionDueNotificationService
    EmailNotificationService emailNotificationService

    static triggers = {
        simple startDelay: 480000l, repeatInterval: 86400000l // execute job once in 1 day after delay of 8 min
    }

    def execute() {
        Boolean notifyFlag = emailNotificationService.mailHandlerForReminderOverDueNotification()
        if(notifyFlag)
            actionDueNotificationService.actionDueNotification()
    }
}
