package com.rxlogix.plugable

import com.rxlogix.AlertService
import com.rxlogix.InboxLog
import com.rxlogix.config.Action
import com.rxlogix.config.EmailLog
import com.rxlogix.helper.LinkHelper
import com.rxlogix.notifications.NotificationHelper
import com.rxlogix.signal.AdHocAlert
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.util.DateUtil
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import com.rxlogix.Constants
import grails.util.Holders
import org.joda.time.DateTime
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean

@Transactional
class ActionDueNotificationService implements LinkHelper, NotificationHelper {
    GrailsApplication grailsApplication
    def emailService
    def userService
    MessageSource messageSource
    def dataObjectService
    def sessionFactory
    AlertService alertService
    def notificationHelper


    def actionDueCondition = { AdHocAlert alert, DateTime givenDate ->
        def actions = alert.actions?.find { action ->
            (_dueExactlyNDaysFromGivenTime(action.dueDate, 0, givenDate) ||
                    (_dueDateExceeded(action.dueDate)) && !(Constants.ActionStatus.CLOSED.equalsIgnoreCase(action.actionStatus)))

        }
        actions ? alert : null
    }

    def allAlerts() {
        def alerts = alertsWithActions()
        DateTime givenDate = DateTime.now()
        alerts.findAll { alert ->
            actionDueCondition(alert, givenDate)
        }

    }

    def allActionsWithDueDate() {
        DateTime givenDate = DateTime.now()
        String dateString = new Date().dateString
        def actions = []
        Action.getAll().each { action ->
            if (!(Constants.ActionStatus.CLOSED.equalsIgnoreCase(action.actionStatus) || Constants.ActionStatus.DELETED.equalsIgnoreCase(action.actionStatus) || Constants.ActionStatus.COMPLETED.equalsIgnoreCase(action.actionStatus) || dateString.equalsIgnoreCase(action.notificationDate) ) &&
                    (_dueExactlyNDaysFromGivenTime(action.dueDate, 0, givenDate) || _dueDateExceeded(action.dueDate))) {
                actions.add(action)
            }
        }
        actions ? actions : null
    }
    def saveNotificationDate(Action action){
        String dateString = new Date().dateString
        try {
            action.notificationDate = dateString
            action.save(failOnError: true, flush: true)
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    def alertsWithActions() {
        AdHocAlert.createCriteria().list {
            actions {
               isNotNull("id")
            }
        }
    }

    def actionDueNotification() {
        def locale = userService?.user?.preference?.locale ?: Locale.ENGLISH
        String timezone = grailsApplication.config.server.timezone

        if (Holders.config.pvsignal.document.reminder.email) {


            Date date = new Date()
            Long currentMS = date.getTime()
            String currentDate = date.dateString

            try {
                //Flow for adhoc alert
                allAlerts()?.each {
                    def alertLink = createHref("action", "list", null)
                    emailService.sendActionDueReminderAdhocEmail(['toAddress': [it.assignedTo?.email],
                                                                  'inboxType': "Action Due Date Reminder",
                                                                  'title'    : messageSource.getMessage("app.action.due.reminder.title",
                                                                          [it.buildProductNameList().toString()].toArray(), locale),
                                                                  'map'      : ['alertInstance': it, "alertLink": alertLink], 'currentMS': currentMS])
                }
                //Flow for aggregate and single case alert
                List<Action> actionList = allActionsWithDueDate()
                if (actionList) {
                    List<Long> actionIdList = actionList.collect { it.id }
                    List singleCaseAlertList = alertService.getAlertForAction(SingleCaseAlert, actionIdList)
                    List aggCaseAlertList = alertService.getAlertForAction(AggregateCaseAlert, actionIdList)
                    actionList.each { Action action ->
                        saveNotificationDate(action)
                        def alert
                        def productName = Constants.Commons.BLANK_STRING
                        if (action.alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
                            alert = singleCaseAlertList.find { it.actionId == action.id }
                            productName = alert?.productName
                        } else if (action.alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
                            alert = aggCaseAlertList.find { it.actionId == action.id }
                            productName = alert?.productName
                        }

                        if (alert) {
                            String emailAddress = action.assignedTo ? action.assignedTo?.email : action.guestAttendeeEmail
                            def alertLink = createHref("action", "list", null)
                            emailService.sendNotificationEmail(['toAddress': [emailAddress],
                                                                'inboxType': "Action Due Date Reminder",
                                                                'title'    : messageSource.getMessage("app.action.due.reminder.title", [productName].toArray(), locale),
                                                                'map':["map":['Alert Name ':alert?.name,
                                                                              "Product Name " : productName,
                                                                              "Priority " : alert?.priorityDisplayName,
                                                                              "Disposition " : alert?.dispositionDisplayName,
                                                                              "Assigned User " : userService.getAssignedToNameAction(alert),
                                                                              "Action type " : action.type,
                                                                              "Action " : action.config,
                                                                              "Due Date " : DateUtil.fromDateToStringWithTimezone(action.dueDate,'dd-MMM-yyyy',timezone),
                                                                              "Action Details " : action.details],
                                                                       "emailMessage" : messageSource.getMessage("app.action.due.reminder.msg", null, locale),
                                                                       "alertLink": alertLink], 'currentMS': currentMS])
                        } else {
                            String emailAddress = action.assignedTo ? action.assignedTo?.email : action.guestAttendeeEmail
                            def alertLink = createHref("action", "list", null)
                            emailService.sendNotificationEmail(['toAddress': [emailAddress],
                                                                'inboxType': "Action Due Date Reminder",
                                                                'title'    : alert ? messageSource.getMessage("app.action.due.reminder.title", [productName].toArray(), locale):
                                                                        "Action item is due",
                                                                'map':["map":["Assigned User " : alert ? userService.getAssignedToNameAction(alert) : action.assignedTo ? action.assignedTo?.fullName : "",
                                                                              "Action type " : action.type,
                                                                              "Action " : action.config,
                                                                              "Due Date " : DateUtil.fromDateToStringWithTimezone(action.dueDate,'dd-MMM-yyyy',timezone),
                                                                              "Action Details " : action.details],
                                                                       "emailMessage" : messageSource.getMessage("app.action.due.reminder.msg", null, locale),
                                                                       "alertLink": ""], 'currentMS': currentMS])
                        }
                    }
                }
                batchPersistEmailLog(currentMS)
                batchPersistInboxlog(currentMS)
            } catch (Throwable t) {
                t.printStackTrace()
                log.error(t.getMessage())
            }
            finally {
                emailService.lstEmailLog = []
                emailService.lstInboxLog = []
            }
        }
    }

    void batchPersistEmailLog(Long currentMS) {
        List<EmailLog> lstEmailLog = dataObjectService.getEmailLogMap(currentMS)
        try {
            alertService.batchPersistForDomain(lstEmailLog, EmailLog.class.name)
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            dataObjectService.clearEmailLogMap(currentMS)
        }
    }

    void batchPersistInboxlog(Long currentMS) {
        List<InboxLog> lstInboxLog = dataObjectService.getInboxLogMap(currentMS)
        try {
            alertService.batchPersistForDomain(lstInboxLog, InboxLog.class.name)
            sendActionDueBulkNotification(lstInboxLog)
        } catch (Exception e) {
            e.printStackTrace()
        } finally {
            dataObjectService.clearInboxLogMap(currentMS)
        }
    }

    void sendActionDueBulkNotification(List<InboxLog> inboxLogList )
    {
        for (InboxLog inboxLog : inboxLogList) {
            notificationHelper.pushNotification(inboxLog)
        }
    }

}