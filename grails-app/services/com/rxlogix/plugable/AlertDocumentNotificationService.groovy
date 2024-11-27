package com.rxlogix.plugable

import com.rxlogix.config.AlertDocument
import com.rxlogix.helper.LinkHelper
import com.rxlogix.notifications.NotificationHelper
import com.rxlogix.signal.AdHocAlert
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import org.joda.time.DateTime
import grails.util.Holders

@Transactional
class AlertDocumentNotificationService implements LinkHelper, NotificationHelper {
    GrailsApplication grailsApplication
    def emailService
    def messageSource
    def userService

    def alertsWithUnapprovedValidationReport() {
        AdHocAlert.createCriteria().list {
            alertDocuments {
                and {
                    eq("documentType", 'Validation')
                    ne("documentStatus", 'Approved')
                }
            }
        }
    }

    def docDueWithinDaysAtGivenTime(AdHocAlert alert, AlertDocument document, DateTime givenTime){
        alert.priority.value == "High" && _dueWithinDaysOfGivenTime ( document.targetDate, 2, givenTime ) ||
        alert.priority.value != "High" && _dueWithinDaysOfGivenTime ( document.targetDate, 7, givenTime )
    }

    def cond_1 = { AdHocAlert alert, DateTime givenTime ->
        def doc = alert.alertDocuments.find { doc ->
            docDueWithinDaysAtGivenTime(alert, doc, givenTime)
        }

        doc ? alert : null
    }

    def cond_2 = { AdHocAlert alert, DateTime givenTime ->
        if (!alert.disposition.reviewCompleted &&
                alert.alertDocuments.find {it.documentType == 'Validation'} == null) {
            if (alert.alertDocuments) {
                def docs = alert.alertDocuments.sort {-it.id}
                if (docDueWithinDaysAtGivenTime(alert, docs[0], givenTime)){
                   return alert
                }
            }
        }
        null
    }

    def allAlerts() {
        def alerts = alertsWithUnapprovedValidationReport()
        DateTime givenTime = DateTime.now()
        alerts.findAll { alert ->
            cond_1(alert, givenTime) || cond_2(alert, givenTime)
        }
    }

    def validationReminders() {
        def locale = userService?.user?.preference?.locale ?: Locale.ENGLISH

        if (Holders.config.pvsignal.document.reminder.email) {
            allAlerts()?.each {
                try {
                    def alertLink = createHref("adHocAlert", "alertDetail", ["id": it.id])
                    def doc = it.alertDocuments.find { d -> d.documentType == 'Validation' && d.documentStatus != 'Approved' }
                    emailService.sendDocumentValidationReminderEmail(['toAddress': [it.assignedTo?.email],
                                                                      'title'    : messageSource.getMessage("app.document.validation.reminder.title",
                                                                              [it.buildProductNameList().toString()].toArray(), locale),
                                                                      'map'      : ['alertInstance': it, "alertLink": alertLink, "doc": doc]])
                } catch (Throwable ex) {
                    log.error("Failed to send out document validation reminder email [" + it?.id + "].", ex)
                }
            }
        }
    }
}