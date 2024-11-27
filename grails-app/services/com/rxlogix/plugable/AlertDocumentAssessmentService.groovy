package com.rxlogix.plugable

import com.rxlogix.config.Disposition
import com.rxlogix.helper.LinkHelper
import com.rxlogix.notifications.NotificationHelper
import com.rxlogix.signal.AdHocAlert
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import org.joda.time.DateTime

@Transactional
class AlertDocumentAssessmentService implements LinkHelper, NotificationHelper{

    def GrailsApplication grailsApplication
    def emailService
    def messageSource
    def userService


    def alertsWithValidatedSignalAndNotApproved() {
        AdHocAlert.createCriteria().list {
            and {
                eq("disposition", Disposition.findByValue("ValidatedSignal"))
                alertDocuments {
                    and {
                        eq("documentType", 'Assessment Report')
                        ne("documentStatus", 'Approved')
                    }
                }
            }
        }
    }

    def cond = { AdHocAlert alert, DateTime givenDate ->
        def doc = alert.alertDocuments.find { doc ->
            (alert.priority.value == "High" && _dueWithinDaysOfGivenTime(doc.targetDate, 2, givenDate))||
                    (alert.priority.value != "High" && _dueWithinDaysOfGivenTime(doc.targetDate, 7, givenDate))
        }

        doc ? alert : null
    }



    def allAlerts() {
        DateTime givenDate = DateTime.now()
        alertsWithValidatedSignalAndNotApproved().findAll { alert ->
            cond(alert, givenDate)
        }
    }

    def assessmentReminders() {
        def locale = userService?.user?.preference?.locale ?: Locale.ENGLISH

        if (Holders.config.pvsignal.document.reminder.email) {
            allAlerts()?.each {
                try {
                    def alertLink = createHref("adHocAlert", "alertDetail", ["id": it.id])
                    def doc = it.alertDocuments.find{ d -> d.documentType == 'Assessment Report' && d.documentStatus != 'Approved' }
                    emailService.sendDocumentAssessmentReminderEmail(['toAddress': [it.assignedTo?.email],
                                                                      'title'    : messageSource.getMessage("app.document.assessment.reminder.title",
                                                                              [it.buildProductNameList().toString()].toArray(), locale),
                                                                      'map'      : ['alertInstance': it, "alertLink": alertLink, 'document': doc]])
                }catch(Throwable ex){
                    log.error("Failed to send out document assessment reminder email [" + it?.id + "].", ex )
                }
            }
        }
    }
}