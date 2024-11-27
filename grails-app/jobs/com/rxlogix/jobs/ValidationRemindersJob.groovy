package com.rxlogix.jobs

class ValidationRemindersJob {

    def alertDocumentNotificationService

    static triggers = {
      //simple repeatInterval: 5000l // execute job once in 5 seconds
    }

    def execute() {
        alertDocumentNotificationService.validationReminders()
    }
}
