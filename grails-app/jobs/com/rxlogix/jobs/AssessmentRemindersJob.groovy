package com.rxlogix.jobs

class AssessmentRemindersJob {
    def alertDocumentAssessmentService

    static triggers = {
      //simple name: "assessmentReminder", startDelay: 50000, repeatInterval: 50000, repeatCount: -1

    }

    def execute() {
        alertDocumentAssessmentService.assessmentReminders()

    }
}
