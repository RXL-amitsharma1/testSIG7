package com.rxlogix

import com.rxlogix.action.ActionService
import com.rxlogix.cache.CacheService
import com.rxlogix.config.Action
import com.rxlogix.config.Configuration
import com.rxlogix.config.Disposition
import com.rxlogix.config.DispositionRule
import com.rxlogix.config.EvdasAlert
import com.rxlogix.config.EvdasFileProcessLog
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.config.ExecutedLiteratureConfiguration
import com.rxlogix.config.LiteratureAlert
import com.rxlogix.config.Meeting
import com.rxlogix.enums.EvdasFileProcessState
import com.rxlogix.helper.LinkHelper
import com.rxlogix.signal.AdHocAlert
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.BatchLotStatus
import com.rxlogix.signal.ProductGroupStatus
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovy.sql.Sql
import oracle.jdbc.Const
import org.apache.commons.lang3.StringUtils

import java.text.SimpleDateFormat

class EmailNotificationService implements LinkHelper {
    def emailService
    def reportExecutorService
    ActionService actionService
    def messageSource
    UserService userService
    MeetingService meetingService
    SignalDataSourceService signalDataSourceService
    CacheService cacheService
    def spotfireService

    Map getEmailNotificationModules() {
        Map modules = [:]
        getKeys().each {
            modules.put(it, EmailNotification.findByKey(it))
        }
        modules
    }

    List<String> getKeys() {
        List modules = Holders.config.mail.notification.modules
        List keys = modules.collect { it.key }
        return keys
    }

    boolean isEmailNotificationEnabled(String key) {
        cacheService.isEmailNotificationEnabled(key)
    }


    Map getEmailNotificationMap(String key) {
        boolean isEnabled = cacheService.isEmailNotificationEnabled(key)
        String moduleName = cacheService.getModuleName(key)
        [isEnabled: isEnabled, moduleName: moduleName]
    }

    void bulkUpdateEmailNotifications(List enabledList, List disabledList) {
        try {
            if (enabledList) {
                enabledList.each { it ->
                    EmailNotification emailNotification = EmailNotification.get(it)
                    if (!emailNotification.isEnabled) {
                        emailNotification.isEnabled = true
                        emailNotification.save(failOnError: true, flush: true)
                    }
                }
            }
            if (disabledList) {
                disabledList.each { it ->
                    EmailNotification emailNotification = EmailNotification.get(it)
                    if (emailNotification.isEnabled) {
                        emailNotification.isEnabled = false
                        emailNotification.save(failOnError: true, flush: true)
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.printStackTrace())
        }
    }

    void emailHanlderAtAlertLevel(def executedConfiguration, def executionStatus) {
        String key = null
        if (executedConfiguration instanceof ExecutedEvdasConfiguration) {
            key = Constants.EmailNotificationModuleKeys.EVDAS_ALERT
        } else if (executedConfiguration instanceof ExecutedLiteratureConfiguration) {
            key = Constants.EmailNotificationModuleKeys.LITERATURE_ALERT
        } else if (executedConfiguration instanceof ExecutedConfiguration) {
            if (executedConfiguration.type.equalsIgnoreCase(Constants.AlertConfigType.SINGLE_CASE_ALERT)) {
                key = Constants.EmailNotificationModuleKeys.SCA_ALERT
            } else if (executedConfiguration.selectedDatasource.equalsIgnoreCase(Constants.DataSource.DATASOURCE_PVA) || (executedConfiguration.selectedDatasource.equalsIgnoreCase(Constants.DataSource.PVA))) {
                key = Constants.EmailNotificationModuleKeys.ACA_PVA
            } else if (executedConfiguration.selectedDatasource.equalsIgnoreCase(Constants.DataSource.DATASOURCE_VAERS)) {
                key = Constants.EmailNotificationModuleKeys.ACA_VAERS
            } else if (executedConfiguration.selectedDatasource.equalsIgnoreCase(Constants.DataSource.DATASOURCE_FAERS)) {
                key = Constants.EmailNotificationModuleKeys.ACA_FAERS
            } else if (executedConfiguration.selectedDatasource.equalsIgnoreCase(Constants.DataSource.DATASOURCE_VIGIBASE)) {
                key = Constants.EmailNotificationModuleKeys.ACA_VIGIBASE
            } else if (executedConfiguration.selectedDatasource.equalsIgnoreCase(Constants.DataSource.DATASOURCE_JADER)) {
                key = Constants.EmailNotificationModuleKeys.ACA_JADER
            } else {
                key = Constants.EmailNotificationModuleKeys.ACA_INTEGRATED
            }
        }

        if (emailNotificationWithBulkUpdate(null, key)) {
            emailService.sendEmailNotification(executedConfiguration, executionStatus)
        }
    }

    //Mail Handler for Action Updation
    void mailHandlerForActionAssignmentUpdate(User user, String messageToUser, Action actionToUpdate, String alertName, String productName, String timezone, String alertLink) {
        emailService.sendNotificationEmail(['toAddress': [user.email],
                                            'inboxType': "Action Updated",
                                            'title'    : messageSource.getMessage("app.email.action.assignment.update", null, Locale.default),
                                            'map'      : ["map"         : ["Alert Name "    : alertName,
                                                                           "Product Name "  : productName,
                                                                           "Action type "   : actionToUpdate.type,
                                                                           "Action "        : actionToUpdate.config,
                                                                           "Due Date "      : DateUtil.toDateString(actionToUpdate.dueDate, timezone),
                                                                           "Action Details ": actionToUpdate.details,
                                                                           "Assigned To "   : actionToUpdate.assignedTo ? actionToUpdate.assignedTo : actionToUpdate.assignedToGroup
                                            ],
                                                          "emailMessage": messageToUser,
                                                          "alertLink"   : alertLink]])
    }

    void mailHandlerForActionUpdate(Action actionToUpdate, String email, String alertName, String productName, String timezone, String alertLink) {
        if(alertName && alertName!=Constants.Commons.BLANK_STRING) {
        emailService.sendNotificationEmail(['toAddress': [email],
                                            'inboxType': "Action Assignment Updated",
                                            'title'    : messageSource.getMessage("app.email.action.update", null, Locale.default),
                                            'map'      : ["map"         : ["Alert Name "    : alertName,
                                                                           "Product Name "  : productName,
                                                                           "Action type "   : actionToUpdate.type,
                                                                           "Action "        : actionToUpdate.config,
                                                                           "Due Date "      : DateUtil.toDateString(actionToUpdate.dueDate, timezone),
                                                                           "Action Details ": actionToUpdate.details,
                                                                           "Assigned To "   : actionToUpdate.assignedTo ? actionToUpdate.assignedTo : actionToUpdate.assignedToGroup ?: actionToUpdate.guestAttendeeEmail
                                            ],
                                                          "emailMessage": messageSource.getMessage('app.email.action.update.message', [productName] as Object[], Locale.default),
                                                          "alertLink"   : alertLink]])
        }
        else{
            emailService.sendNotificationEmail(['toAddress': [email],
                                                'inboxType': "Action Assignment Updated",
                                                'title'    : messageSource.getMessage("app.email.action.update", null, Locale.default),
                                                'map'      : ["map"         : ["Action type "   : actionToUpdate.type,
                                                                               "Action "        : actionToUpdate.config,
                                                                               "Due Date "      : DateUtil.toDateString(actionToUpdate.dueDate, timezone),
                                                                               "Action Details ": actionToUpdate.details,
                                                                               "Assigned To "   : actionToUpdate.assignedTo ? actionToUpdate.assignedTo : actionToUpdate.assignedToGroup ?: actionToUpdate.guestAttendeeEmail
                                                ],
                                                              "emailMessage": messageSource.getMessage('app.email.action.update.message', [productName] as Object[], Locale.default),
                                                              "alertLink"   : alertLink]])
        }
    }


    //Mail Handler for Action Creation
    void mailHandlerForActionCreation(Action actionInstance, String email, Long articleId, String productName, String alertName, String timezone, String alertLink,String appType = null) {

        String actionName = (appType == Constants.AlertConfigType.SIGNAL_MANAGEMENT) ? 'Signal Name ' : 'Alert Name '
        List titleList = articleId ? [articleId.toString()] : [productName]
        if(alertName) {
            emailService.sendNotificationEmail(['toAddress': [email],
                                                'title'    : messageSource.getMessage("app.email.action.create.title", titleList as String[], Locale.default),
                                                'inboxType': "Action Creation",
                                                'allowNotification': true,
                                                'map'      : ["map"         : [(actionName)    : alertName,
                                                                               "Product Name "  : productName,
                                                                               "Action type "   : actionInstance.type,
                                                                               "Action "        : actionInstance.config,
                                                                               "Due Date "      : DateUtil.fromDateToStringWithTimezone(actionInstance.dueDate,'dd-MMM-yyyy',timezone),
                                                                               "Action Details ": actionInstance.details
                                                ],
                                                              "emailMessage": messageSource.getMessage('app.email.action.create.msg', titleList as Object[], Locale.default),
                                                              "alertLink"   : alertLink]])
        } else {
            emailService.sendNotificationEmail(['toAddress': [email],
                                                'title'    : messageSource.getMessage("app.email.standalone.action.create.title", [] as Object[], Locale.default),
                                                'inboxType': "Action Creation",
                                                'allowNotification': true,
                                                'map'      : ["map"         : ["Action type "   : actionInstance.type,
                                                                               "Action "        : actionInstance.config,
                                                                               "Due Date "      : DateUtil.fromDateToStringWithTimezone(actionInstance.dueDate,'dd-MMM-yyyy',timezone),
                                                                               "Action Details ": actionInstance.details
                                                ],
                                                              "emailMessage": messageSource.getMessage('app.email.standalone.action.create.title', [] as Object[], Locale.default)+":",
                                                              "alertLink"   : ""]])
        }
    }

    boolean mailHandlerForReminderOverDueNotification() {
        return emailNotificationWithBulkUpdate(null, Constants.EmailNotificationModuleKeys.ACTION_REMINDER_OVER_DUE)
    }

    void mailHandlerForDispChangeAdhoc(AdHocAlert adHocAlert, Disposition previousDisposition, Disposition targetDisposition) {
        DispositionRule dispositionRule = DispositionRule.findByIncomingDispositionAndTargetDispositionAndIsDeleted(previousDisposition, targetDisposition, false)
        if (dispositionRule?.notify) {
            String alertLink = createHref("adHocAlert", "alertDetail", ["id": adHocAlert.id])
            List<String> recipientsList = userService.getRecipientsList(adHocAlert)
            recipientsList.each {
                emailService.sendNotificationEmail(['toAddress': [it],
                                                    "inboxType": messageSource.getMessage("email.disposition.change.title", null, Locale.default),
                                                    'title'    : messageSource.getMessage("email.alert.change.title", [adHocAlert.buildProductNameList().join(', ')] as Object[], Locale.default),
                                                    'map'      : ["map"         : ['Alert Name '    : adHocAlert.name,
                                                                                   'Alert Type '    : messageSource.getMessage("adhoc.alert.label", null, Locale.default),
                                                                                   "Priority "      : adHocAlert.priority,
                                                                                   "Old Disposition": previousDisposition,
                                                                                   "New Disposition": adHocAlert.disposition,
                                                                                   "Assigned User"  : userService.getAssignedToName(adHocAlert)],
                                                                  "emailMessage": messageSource.getMessage("email.alert.disposition.change.message", [adHocAlert.buildProductNameList().join(', '), adHocAlert.name] as Object[], Locale.default),
                                                                  "alertLink"   : alertLink]
                ])
            }
        } else {
            log.info("Workflow Rule Notify is Disabled.")
        }
    }

    void mailHandlerForDispChangeLiterature(def alert, Disposition previousDisposition, Boolean isArchived = false) {
        DispositionRule dispositionRule = DispositionRule.findByIncomingDispositionAndTargetDispositionAndIsDeleted(previousDisposition, alert.disposition, false)
        if (dispositionRule?.notify) {
            String alertLink = createHref("LiteratureAlert", "details", [callingScreen: "review", configId: alert.exLitSearchConfig.id, isArchived: isArchived])
            List<String> recipientsList = userService.getRecipientsList(alert)
            recipientsList.each {
                emailService.sendNotificationEmail(['toAddress': [it],
                                                    'title'    : messageSource.getMessage("email.alert.workflow.change.title", null, Locale.default),
                                                    "inboxType": messageSource.getMessage("email.disposition.change.title", null, Locale.default),
                                                    'map'      : ["map"         : ['Alert Name '    : alert.name,
                                                                                   'Alert Type '    : messageSource.getMessage("app.new.literature.search.alert", null, Locale.default),
                                                                                   "Article Id"     : alert.articleId,
                                                                                   "Search String"  : alert.searchString,
                                                                                   "Priority "      : alert.priority,
                                                                                   "Old Disposition": previousDisposition,
                                                                                   "New Disposition": alert.disposition,
                                                                                   "Assigned User"  : userService.getAssignedToName(alert)],
                                                                  "emailMessage": messageSource.getMessage("email.alert.disposition.change.message", [alert.articleId?.toString(), alert.name] as Object[], Locale.default),
                                                                  "alertLink"   : alertLink]
                ])
            }
        } else {
            log.info("Workflow Rule Notify is Disabled.")
        }
    }

    void mailHandlerForDispChangeSCA(def singleCaseAlert, Disposition previousDisposition, DispositionRule dispositionRule, Boolean isArchived = false) {
        if (dispositionRule?.notify && (singleCaseAlert?.assignedTo || singleCaseAlert?.assignedToGroup)) {

            String alertLink = createHref("singleCaseAlert", "details", ["configId": singleCaseAlert.executedAlertConfigurationId, isArchived: isArchived])
            List<User> recipientsList = userService.getUserListFromAssignToGroup(singleCaseAlert)
            recipientsList.each {
                emailService.sendNotificationEmail(['toAddress': [it?.email],
                                                    'title'    : messageSource.getMessage("email.alert.workflow.change.title", null, Locale.default),
                                                    "inboxType": messageSource.getMessage("email.disposition.change.title", null, Locale.default),
                                                    'map'      : ["map"         : ['Alert Name '    : singleCaseAlert.name,
                                                                                   'Alert Type '    : messageSource.getMessage("app.new.single.case.alert", null, Locale.default),
                                                                                   "Product Name"   : singleCaseAlert.productName,
                                                                                   "Case Number"    : singleCaseAlert.caseNumber,
                                                                                   "Priority "      : singleCaseAlert.priority,
                                                                                   "Old Disposition": previousDisposition,
                                                                                   "New Disposition": singleCaseAlert.disposition,
                                                                                   "Assigned User"  : userService.getAssignedToName(singleCaseAlert)],
                                                                  "emailMessage": messageSource.getMessage("email.alert.disposition.change.message", [singleCaseAlert.productFamily, singleCaseAlert.name] as Object[], Locale.default),
                                                                  "alertLink"   : alertLink]
                ])
            }
        } else {
            log.info("Workflow Rule Notify is Disabled.")
        }
    }

    //TODO:Need to analyze how does email server works if sent email is enabled for bulk update.
    void mailHandlerForDispChangeACA(def aggregateCaseAlert, String selectedDataSource, Disposition previousDisposition, Disposition targetDisposition, boolean bulkUpdate, Boolean isArchived = false) {
        boolean notificationEnabled = false
        if (selectedDataSource.equalsIgnoreCase(Constants.DataSource.DATASOURCE_PVA) || selectedDataSource.equalsIgnoreCase(Constants.DataSource.PVA)) {
            notificationEnabled = isEmailNotificationEnabled(Constants.EmailNotificationModuleKeys.DISPOSITION_CHANGE_ACA_PVA)
        } else if (selectedDataSource.equalsIgnoreCase(Constants.DataSource.DATASOURCE_VAERS)) {
            notificationEnabled = isEmailNotificationEnabled(Constants.EmailNotificationModuleKeys.DISPOSITION_CHANGE_ACA_VAERS)
        } else if (selectedDataSource.equalsIgnoreCase(Constants.DataSource.DATASOURCE_FAERS)) {
            notificationEnabled = isEmailNotificationEnabled(Constants.EmailNotificationModuleKeys.DISPOSITION_CHANGE_ACA_FAERS)
        } else if (selectedDataSource.equalsIgnoreCase(Constants.DataSource.DATASOURCE_VIGIBASE)) {
            notificationEnabled = isEmailNotificationEnabled(Constants.EmailNotificationModuleKeys.DISPOSITION_CHANGE_ACA_VIGIBASE)
        } else if (selectedDataSource.equalsIgnoreCase(Constants.DataSource.DATASOURCE_JADER)) {
            notificationEnabled = isEmailNotificationEnabled(Constants.EmailNotificationModuleKeys.DISPOSITION_CHANGE_ACA_JADER)
        }else {
            notificationEnabled = isEmailNotificationEnabled(Constants.EmailNotificationModuleKeys.DISPOSITION_CHANGE_ACA_INTEGRATED)
        }
        if ((bulkUpdate && isEmailNotificationEnabled(Constants.EmailNotificationModuleKeys.BULK_UPDATE)) || (!bulkUpdate && notificationEnabled)) {
            DispositionRule dispositionRule = DispositionRule.findByIncomingDispositionAndTargetDispositionAndIsDeleted(previousDisposition, targetDisposition, false)
            if (dispositionRule?.notify) {
                bulkUpdate ? log.info("Email Notification Service Enabled for : BULK UPDATE") : log.info("Email Notification Service is Enabled for : QUANTITATIVE DISPOSITION CHANGE ${selectedDataSource?.toUpperCase()}")
                String alertLink = createHref("aggregateCaseAlert", "details", ["configId": aggregateCaseAlert.executedAlertConfiguration.id, "isArchived": isArchived])
                List<String> recipientsList = userService.getRecipientsList(aggregateCaseAlert)
                recipientsList.each {
                    emailService.sendNotificationEmail(['toAddress': [it],
                                                        "inboxType": messageSource.getMessage("email.workflow.change.title", null, Locale.default),
                                                        'title'    : messageSource.getMessage("email.alert.workflow.change.title", null, Locale.default),
                                                        'map'      : ["map"         : ['Alert Name '     : aggregateCaseAlert.name,
                                                                                       'Alert Type '     : messageSource.getMessage("app.new.aggregate.case.alert", null, Locale.default),
                                                                                       "Product Name"    : aggregateCaseAlert?.productName,
                                                                                       "Soc "            : aggregateCaseAlert.soc,
                                                                                       "PT "             : aggregateCaseAlert?.pt,
                                                                                       "Priority "       : aggregateCaseAlert?.priority,
                                                                                       "Old Disposition ": previousDisposition,
                                                                                       "New Disposition ": targetDisposition,
                                                                                       "Assigned User "  : userService.getAssignedToName(aggregateCaseAlert)
                                                        ],
                                                                      "emailMessage": messageSource.getMessage("email.alert.disposition.change.message", [aggregateCaseAlert.productName, aggregateCaseAlert.name] as Object[], Locale.default),
                                                                      "alertLink"   : alertLink
                                                        ]])
                }
            } else {
                log.info("Workflow Rule Notify is Disabled.")
            }
        } else {
            log.info("Email Notification Service is Disabled for : QUANTITATIVE DISPOSITION CHANGE ${selectedDataSource?.toUpperCase()}")
        }
    }

    void mailHandlerForDispChangeEvdas(def evdasAlert, Disposition previousDisposition, Boolean isArchived = false) {
        DispositionRule dispositionRule = DispositionRule.findByIncomingDispositionAndTargetDispositionAndIsDeleted(previousDisposition, evdasAlert.disposition, false)
        if (dispositionRule?.notify) {
            String alertLink = createHref("evdasAlert", "details", ["configId": evdasAlert.executedAlertConfiguration.id, "isArchived": isArchived])
            List<String> recipientsList = userService.getRecipientsList(evdasAlert)
            recipientsList.each {
                emailService.sendNotificationEmail(
                        ['toAddress': [it],
                         "inboxType": messageSource.getMessage("email.workflow.change.title", null, Locale.default),
                         'title'    : messageSource.getMessage("email.alert.workflow.change.title", null, Locale.default),
                         'map'      : ["map"         : ['Alert Name '    : evdasAlert.name,
                                                        'Alert Type '    : messageSource.getMessage("app.new.evdas.alert", null, Locale.default),
                                                        "Substance Name" : evdasAlert.substance,
                                                        "Priority "      : evdasAlert.priority,
                                                        "Old Disposition": previousDisposition,
                                                        "New Disposition": evdasAlert.disposition,
                                                        "Assigned User"  : userService.getAssignedToName(evdasAlert)
                         ],
                                       "emailMessage": messageSource.getMessage("email.alert.disposition.change.message", [evdasAlert?.substance, evdasAlert?.name] as Object[], Locale.default),
                                       "alertLink"   : alertLink,
                         ]])
            }
        } else {
            log.info("Workflow Rule Notify is Disabled.")
        }

    }

    void mailHandlerForDispChangeSignal(ValidatedSignal validatedSignal, Disposition previousDisposition) {
        DispositionRule dispositionRule = DispositionRule.findByIncomingDispositionAndTargetDispositionAndIsDeleted(previousDisposition, validatedSignal.disposition, false)
        if (dispositionRule?.notify) {
            String alertLink = createHref("validatedSignal", "details", ["id": validatedSignal.id])
            List<String> recipientsList = userService.getRecipientsList(validatedSignal)
            recipientsList.each {
                emailService.sendNotificationEmail(['toAddress': [it],
                                                    "inboxType": messageSource.getMessage("email.signal.workflow.change.title", null, Locale.default),
                                                    'title'    : messageSource.getMessage("email.signal.workflow.change.title", null, Locale.default),
                                                    'map'      : ["map"         : ['Signal Name '       : validatedSignal.name,
                                                                                   'Risk/Topic Category': validatedSignal.topicCategories.name.join(","),
                                                                                   "Product Name"       : validatedSignal.getProductNameList(),
                                                                                   "Priority "          : validatedSignal.priority.displayName,
                                                                                   "Old Disposition"    : previousDisposition,
                                                                                   "New Disposition"    : validatedSignal.disposition,
                                                                                   "Assigned User"      : userService.getAssignedToName(validatedSignal)],
                                                                  "emailMessage": messageSource.getMessage("email.signal.disposition.change.message", [validatedSignal.name] as Object[], Locale.default),
                                                                  "screenName"  : Constants.AlertConfigType.SIGNAL,
                                                                  "alertLink"   : alertLink]
                ])
            }
        } else {
            log.info("Workflow Rule Notify is Disabled.")
        }
    }

    void mailHandlerForSignalAssigneeChange(ValidatedSignal validatedSignal, List<String> oldUserEmailList = []) {
        String alertLink = createHref("validatedSignal", "details", ["id": validatedSignal.id])
        def productName
        if (validatedSignal.products) {
            productName = validatedSignal.getProductNameList().toString()
            productName = productName.substring(1, productName.length() - 1)
        } else {
            productName = JSON.parse(validatedSignal.productGroupSelection).collect { it.name }.join(',')
        }
        List recipientsList = userService.getRecipientsList(validatedSignal)
        recipientsList.addAll(oldUserEmailList)
        recipientsList?.unique()
        recipientsList.each {
            emailService.sendNotificationEmail(['toAddress': [it],
                                                "inboxType": messageSource.getMessage("email.signal.assignedTo.change.title", null, Locale.default),
                                                "title"    : messageSource.getMessage("email.signal.assignedTo.change.title", null, Locale.default),
                                                "map"      : ["map"         : ["Signal Name " : validatedSignal.name,
                                                                               "Product Name" : productName,
                                                                               "Disposition"  : validatedSignal.disposition?.displayName,
                                                                               "Assigned User": userService.getAssignedToName(validatedSignal)],
                                                              "emailMessage": messageSource.getMessage("email.signal.assignedTo.change.title", null, Locale.default),
                                                              "screenName"  : Constants.AlertConfigType.SIGNAL,
                                                              "alertLink"   : alertLink]
            ])
        }
    }


    void mailHandlerForSignalCreation(ValidatedSignal validatedSignal) {
        def alertLink = createHref("validatedSignal", "details", ["id": validatedSignal.id])
        Integer validatedConfigId = validatedSignal.id
        def productName
        if (validatedSignal.products) {
            productName = validatedSignal.getProductNameList().toString()
            productName = productName.substring(1, productName.length() - 1)
        } else {
            productName = JSON.parse(validatedSignal.productGroupSelection).collect { it.name }.join(',')
        }
        List recipientsList = userService.getRecipientsList(validatedSignal)
        recipientsList.each {
            emailService.sendNotificationEmail(['toAddress': [it],
                                                "inboxType": messageSource.getMessage("email.signal.creation", null, Locale.default),
                                                'title'    : messageSource.getMessage("email.signal.create.title", null, Locale.default),
                                                'map'      : ["map"              : ['Signal Name ' : validatedSignal.name,
                                                                                    "Product Name" : productName,
                                                                                    "Disposition"  : validatedSignal.disposition?.displayName,
                                                                                    "Assigned User": userService.getAssignedToName(validatedSignal)],
                                                              "emailMessage"     : messageSource.getMessage("email.signal.create.message", null, Locale.default),
                                                              "screenName"       : Constants.AlertConfigType.SIGNAL,
                                                              "alertLink"        : alertLink,
                                                              "detailUrl"        : "SIGNAL_CREATION",
                                                              "validatedConfigId": validatedConfigId,
                                                              "message"          : Constants.SignalHistory.SIGNAL_CREATED
                                                ]
            ])
        }
    }

    void mailHandlerForAssignedToACA(User user, def alert, String alertLink, String messageToUser) {
        if (alert.soc == null) {
            alert.soc = " "
        }
        emailService.sendNotificationEmail([
                'toAddress': [user.email],
                'title'    : messageSource.getMessage("app.email.case.assignment.quantitative.message", [alert.productName].toArray(), Locale.default),
                "inboxType": "Assigned To Change",
                'map'      : ["map"         : ['Alert Name ' : alert.name,
                                               "Product Name": alert.productName,
                                               "SOC "        : alert.soc,
                                               "PT "         : alert.pt,
                                               "Priority "   : alert?.priority,
                                               "Disposition ": alert?.disposition?.displayName,
                                               "Assigned To ": userService.getAssignedToName(alert)
                ],
                              "emailMessage": messageToUser,
                              "alertLink"   : alertLink]
        ])

    }

    void mailHandlerForAssignedToEvdas(User user, def alert, String alertLink, messageToUser, currentAssignedUser, String emailTitle) {
        emailService.sendNotificationEmail([
                'toAddress': [user.email],
                "inboxType": "Assigned To Change",
                'title'    : emailTitle,
                'map'      : ["map"         : ['Alert Name '   : alert?.name,
                                               "Substance Name": alert?.substance,
                                               "SOC "          : alert?.soc,
                                               "PT "           : alert?.pt,
                                               "Priority "     : alert?.priority,
                                               "Assigned To "  : currentAssignedUser
                ],
                              "emailMessage": messageToUser,
                              "alertLink"   : alertLink]])
    }

    @Transactional
    void mailHandlerForAssignedToLiterature(String email, def alert, String alertLink, messageToUser, String newUserName) {
        emailService.sendNotificationEmail([
                'toAddress': [email],
                'title'    : messageSource.getMessage("app.email.case.assignment.literature.message", [alert.name].toArray(), Locale.default),
                "inboxType": "Assigned To Change",
                'map'      : ["map"         : ['Alert Name ' : alert.name,
                                               "Product Name": alert.productSelection,
                                               "Priority "   : alert.priority,
                                               "Disposition ": alert.dispositionName,
                                               "Assigned To ": newUserName
                ],
                              "emailMessage": messageToUser,
                              "alertLink"   : alertLink]])

    }

    void mailHandlerForAssignedToNewUserAdhoc(User newUser, AdHocAlert adHocAlert, String alertLink, String productName) {
        emailService.sendAssignmentChangeEmail(['toAddress': [newUser.email],
                                                'title'    : messageSource.getMessage("app.email.alert.change.assignment.title", null, Locale.default),
                                                'inboxType': messageSource.getMessage("app.email.alert.change.assign.to.change", null, Locale.default),
                                                'map'      : ['alertInstance': adHocAlert,
                                                              "alertLink"    : alertLink,
                                                              "productName"  : productName
                                                ]])

    }

    void mailHandlerForAssignedToOldUserAdhoc(User oldUser, AdHocAlert adHocAlert, String alertLink, String productName, String newUserName) {

        emailService.sendAssignmentChangeEmail(['toAddress': [oldUser.email],
                                                'title'    : messageSource.getMessage("app.email.alert.change.assignment.prev.title", null, Locale.default),
                                                'inboxType': messageSource.getMessage("app.email.alert.change.assign.to.change", null, Locale.default),
                                                'map'      : ['alertInstance'    : adHocAlert,
                                                              "alertLink"        : alertLink,
                                                              "productName"      : productName,
                                                              "currentAssignedTo": newUserName]
        ])

    }

    boolean mailHandlerForSpotfire() {
        return isEmailNotificationEnabled(Constants.EmailNotificationModuleKeys.SPOTFIRE_GENERATION)
    }

    void mailHandlerForMeetingCRUD(Meeting meeting, msg, files, def screenName, def alertLink, String inboxType) {
        Map allRecipients = meetingService.fetchMeetingAttendeeEmails(meeting)
        allRecipients.each {
            String recipientTimezone = it.key
            List recipients = it.value
            emailService.sendNotificationEmail(['toAddress': recipients,
                                                "inboxType": inboxType,
                                                'title'    : msg,
                                                'map'      : ["map"         : ['Meeting Title '      : meeting.meetingTitle,
                                                                               "Meeting Owner "      : meeting.meetingOwner,
                                                                               "Meeting Date & Time" : "${DateUtil.toDateStringWithTime(meeting.meetingDate, recipientTimezone)}" + recipientTimezone,
                                                                               "Meeting Agenda"      : meeting.meetingAgenda ?: "-",
                                                                               "Meeting Participants": recipients?.join(',')
                                                ],
                                                              "attachments" : files,
                                                              "emailMessage": msg,
                                                              "screenName"  : screenName,
                                                              "alertLink"   : alertLink]])
        }


    }


    void mailHandlerForMinutesOfMeeting(Meeting meeting, msg, def screenName, def alertLink) {
        Map allRecipients = meetingService.fetchMeetingAttendeeEmails(meeting)
        allRecipients.each {
            String recipientTimezone = it.key
            List recipients = it.value
            emailService.sendNotificationEmail(['toAddress': recipients,
                                                'title'    : msg,
                                                "inboxType": "Meeting Minutes Added",
                                                'map'      : ["map"       : ['Meeting Title '      : meeting.meetingTitle,
                                                                             "Meeting Owner "      : meeting.meetingOwner,
                                                                             "Meeting Date & Time" : "${DateUtil.toDateStringWithTime(meeting.meetingDate, recipientTimezone)}" + recipientTimezone,
                                                                             "Meeting Agenda"      : meeting.meetingAgenda ?: "-",
                                                                             "Meeting Minutes"     : meeting.meetingMinutes,
                                                                             "Meeting Participants": recipients?.join(',')
                                                ], "emailMessage"         : msg,
                                                              "screenName": screenName,
                                                              "alertLink" : alertLink]])
        }
    }

    void mailHandlerForMeetingDelete(Meeting meeting) {
        if (isEmailNotificationEnabled(Constants.EmailNotificationModuleKeys.MEETING_CREATION_UPDATION)) {
            String msg = "Meeting Cancelled"
            Map allRecipients = meetingService.fetchMeetingAttendeeEmails(meeting)
            allRecipients.each {
                String recipientTimezone = it.key
                List recipients = it.value
                emailService.sendNotificationEmail([
                        'toAddress': recipients,
                        'inboxType': msg,
                        'title'    : msg,
                        'map'      : ["map"         : ['Meeting title '     : meeting.meetingTitle,
                                                       "Meeting Owner "     : meeting.meetingOwner,
                                                       "Meeting Date & Time": "${DateUtil.toDateStringWithTime(meeting.meetingDate, recipientTimezone)}" + recipientTimezone,
                                                       "Meeting Minutes "   : meeting.meetingMinutes ?: "-"
                        ],
                                      "emailMessage": msg]])
            }
        } else {
            log.info("Email Notification Service is Disabled for : MEETING CREATE/UPDATE/DELETE")
        }
    }


    void mailHandlerForAhocPotesntialSignal(AdHocAlert adHocAlertInstance, def alertLink, def productName) {
        List recipientList = userService.getRecipientsList(adHocAlertInstance)
        recipientList.each {
            emailService.sendNotificationEmail(['toAddress': [it],
                                                "inboxType": "Ad-Hoc Alert Creation",
                                                'title'    : messageSource.getMessage("app.email.alert.create.title",
                                                        [adHocAlertInstance.initialDataSource, productName] as Object[], Locale.default),
                                                'map'      : ["map"          : [
                                                        "Alert Name "  : adHocAlertInstance.name,
                                                        "Alert Type "  : messageSource.getMessage("adhoc.alert.label", null, Locale.default),
                                                        "Priority "    : adHocAlertInstance.priority,
                                                        "Disposition"  : adHocAlertInstance.disposition.displayName,
                                                        "Assigned User": adHocAlertInstance.assignedTo?.fullName],
                                                              'emailMessage' : messageSource.getMessage("app.email.alert.create.title", [adHocAlertInstance.initialDataSource, productName] as Object[], Locale.default),
                                                              'alertInstance': adHocAlertInstance, "alertLink": alertLink, "productName": productName]])
        }
    }

    void mailHandlerForAutoRouteDispSCA(def singleCaseAlert, Disposition previousDisposition, ExecutedConfiguration executedConfiguration, boolean isArchived = false) {
        DispositionRule dispositionRule = DispositionRule.findByIncomingDispositionAndTargetDispositionAndIsDeleted(previousDisposition, singleCaseAlert.disposition, false)
        if (dispositionRule?.notify) {
            List recipientList = userService.getRecipientsList(singleCaseAlert)
            String alertLink = createHref("singleCaseAlert", "details", ["configId": executedConfiguration.id, isArchived: isArchived])
            recipientList.each {
                emailService.sendNotificationEmail(['toAddress': [it],
                                                    'title'    : messageSource.getMessage("email.alert.workflow.change.title", null, Locale.default),
                                                    "inboxType": messageSource.getMessage("email.disposition.change.title", null, Locale.default),
                                                    'map'      : ["map"         : ['Alert Name '    : singleCaseAlert.name,
                                                                                   'Alert Type '    : messageSource.getMessage("app.new.single.case.alert", null, Locale.default),
                                                                                   "Product Name"   : singleCaseAlert.productFamily,
                                                                                   "Case Number"    : singleCaseAlert.caseNumber,
                                                                                   "Priority "      : singleCaseAlert.priority,
                                                                                   "Old Disposition": previousDisposition,
                                                                                   "New Disposition": singleCaseAlert.disposition,
                                                                                   "Assigned User"  : userService.getAssignedToName(singleCaseAlert)],
                                                                  "emailMessage": messageSource.getMessage("email.alert.disposition.change.message", [singleCaseAlert.productFamily, singleCaseAlert.name] as Object[], Locale.default),
                                                                  "alertLink"   : alertLink]
                ])
            }
        }

    }

    void mailHandlerForAutoRouteDispLA(LiteratureAlert literatureAlert, Disposition previousDisposition) {
        DispositionRule dispositionRule = DispositionRule.findByIncomingDispositionAndTargetDispositionAndIsDeleted(previousDisposition, literatureAlert.disposition, false)
        if (dispositionRule?.notify) {
            String alertLink = createHref("LiteratureAlert", "details", [])
            List<String> recipientsList = userService.getRecipientsList(literatureAlert)
            recipientsList.each {
                emailService.sendNotificationEmail(['toAddress': [it],
                                                    'title'    : messageSource.getMessage("email.alert.workflow.change.title", null, Locale.default),
                                                    "inboxType": messageSource.getMessage("email.disposition.change.title", null, Locale.default),
                                                    'map'      : ["map"         : ['Alert Name '    : literatureAlert.name,
                                                                                   'Alert Type '    : messageSource.getMessage("app.new.literature.search.alert", null, Locale.default),
                                                                                   "Article Id"     : literatureAlert.articleId,
                                                                                   "Search String"  : literatureAlert.searchString,
                                                                                   "Priority "      : literatureAlert.priority,
                                                                                   "Old Disposition": previousDisposition,
                                                                                   "New Disposition": literatureAlert.disposition,
                                                                                   "Assigned User"  : userService.getAssignedToName(literatureAlert)],
                                                                  "emailMessage": messageSource.getMessage("email.alert.disposition.change.message", [literatureAlert.articleId, literatureAlert.name] as Object[], Locale.default),
                                                                  "alertLink"   : alertLink]
                ])
            }
        }
    }

    boolean emailNotificationWithBulkUpdate(Boolean bulkUpdate, String key) {
        Map emailNotificationMap = getEmailNotificationMap(key)
        if ((bulkUpdate && isEmailNotificationEnabled(Constants.EmailNotificationModuleKeys.BULK_UPDATE)) ||
                (!bulkUpdate && emailNotificationMap.isEnabled)) {
            bulkUpdate ? log.info("Email Notification Service Enabled for : BULK UPDATE") : log.info("Email Notification Service Enabled for : ${emailNotificationMap.moduleName?.toUpperCase()}")
            return true
        } else {
            bulkUpdate ? log.info("Email Notification Service Disabled for : BULK UPDATE") : log.info("Email Notification Service Disabled for : ${emailNotificationMap.moduleName?.toUpperCase()}")
            return false
        }
    }

    void mailApiUploadStatus(BatchLotStatus batchLotStatus, String uploadedByEmail, List<String> toAddresses, List files, String errorString) {
        String alertLink = createHref("apiUploadStatusReport", "index", [])
        String mailSubject = "Batch Lot Data upload API logs"
        String mailBody = ""
        if (batchLotStatus.getId() != null && batchLotStatus.getCount() == batchLotStatus.getValidRecordCount()) {
            mailBody = "Batch Lot Data upload API process is successfully completed.<br/>" +
                    "<br/>" +
                    "The Batch Lot Data logs are attached with the email, corresponding to " + batchLotStatus.getBatchId() +
                    " processed at " + DateUtil.toDateStringPattern(batchLotStatus.getUploadedAt(), DateUtil.DATEPICKER_FORMAT_AM_PM_4) +
                    "<br>"
        } else if (batchLotStatus.getId() == null || batchLotStatus.getValidRecordCount() == 0) {
            mailBody = "Batch Lot Data upload API process failed with below error.<br>" +
                    "<br/>" +
                    "Error:<br>" + errorString +
                    "<br>"
        } else if (batchLotStatus.getId() != null && batchLotStatus.getCount() > batchLotStatus.getValidRecordCount()) {
            mailBody = "Batch Lot Data upload API process is completed with error.<br/>" +
                    "<br/>" +
                    "Error:<br>" + errorString +
                    "<br/>" +
                    "<br>The Batch Lot Data logs are attached with the email, corresponding to " + batchLotStatus.getBatchId() +
                    " processed at <Timestamp> " + DateUtil.toDateStringPattern(batchLotStatus.getUploadedAt(), DateUtil.DATEPICKER_FORMAT_AM_PM_4) +
                    "<br>"
        }
        mailBody = mailBody + "<br>You can review that using following hyperlink <a href=\"" + alertLink + "\">PV Signal</a>"
        emailService.sendApiUpdateWithAttachementEmail(['toAddress': toAddresses,
                                                        'inboxType': "Action Updated",
                                                        'title'    : mailSubject,
                                                        'map'      : ["map": [
                                                                "apiUploadFile": files.get(0),
                                                                "isAPIUpload"  : "yes",
                                                                "emailMessage" : mailBody,
                                                                "alertLink"    : alertLink
                                                        ]]
        ])

    }

    void mailApiUploadStatus(ProductGroupStatus productGroupStatus, String uploadedByEmail, List<String> toAddresses, List files, String errorString) {
        String alertLink = createHref("productGroupStatus", "index", [])
        String mailSubject = "Product Group API logs"
        String mailBody = ""
        String pvrErrorString = productGroupStatus.pvrError
        if (productGroupStatus.getId() != null && productGroupStatus.getCount() == productGroupStatus.getValidRecordCount()) {
            mailBody = "Product Group API process is successfully completed.<br/>" +
                    "<br/>" +
                    "The Product Group API logs are attached with the email, corresponding to " + productGroupStatus.getUniqueIdentifier() +
                    " processed at " + DateUtil.toDateStringPattern(productGroupStatus.getUploadedAt(), DateUtil.DATEPICKER_FORMAT_AM_PM_4) +
                    "<br>"
        } else if (productGroupStatus.getId() == null || productGroupStatus.getValidRecordCount() == 0) {
            if (pvrErrorString == null && productGroupStatus) {
                mailBody = "Product Group API process is completed with error.<br>" +
                        "<br/>" +
                        "The Product Group API logs are attached with the email, corresponding to " + productGroupStatus.getUniqueIdentifier() +
                        " processed at " + DateUtil.toDateStringPattern(productGroupStatus.getUploadedAt(), DateUtil.DATEPICKER_FORMAT_AM_PM_4) +
                        "<br>"
            } else {
                mailBody = "Product Group API process failed with below error.<br><br>Error:<br>" +
                        pvrErrorString + "<br>"
            }

        } else if (productGroupStatus.getId() != null && productGroupStatus.getCount() > productGroupStatus.getValidRecordCount()) {
            mailBody = "Product Group API process is completed with error.<br/>" +
                    "<br/>" +
                    "The Product Group API logs are attached with the email, corresponding to " + productGroupStatus.getUniqueIdentifier() +
                    " processed at <Timestamp>" + DateUtil.toDateStringPattern(productGroupStatus.getUploadedAt(), DateUtil.DATEPICKER_FORMAT_AM_PM_4) +
                    "<br>"
        }
        mailBody = mailBody + "<br>You can review that using following hyperlink <a href=\"" + alertLink + "\">PV Signal</a>"
        emailService.sendPgsUpdateWithAttachementEmail(['toAddress': toAddresses,
                                                        'inboxType': "Action Updated",
                                                        'title'    : mailSubject,
                                                        'map'      : ["map": [
                                                                "pgsUploadFile": files.get(0),
                                                                "isPgsUpload"  : (pvrErrorString == null ? "yes" : "no"),
                                                                "emailMessage" : mailBody,
                                                                "alertLink"    : alertLink
                                                        ]]
        ])

    }

    void mailBatchLotETLStatus(BatchLotStatus batchLotStatus, String uploadedByEmail, List<String> toAddresses, List files, String errorString) {
        String alertLink = createHref("apiUploadStatusReport", "index", [])
        String mailSubject = "Batch Lot Data ETL logs"
        String mailBody = ""
        if (batchLotStatus.getId() != null && batchLotStatus.getCount() == batchLotStatus.getValidRecordCount()) {
            mailBody = "Batch Lot Data ETL is successfully completed.<br/>" +
                    "<br/>" +
                    "The Batch Lot Data logs are attached with the email, for ETL executed on " +
                    DateUtil.toDateStringPattern(new Date(), DateUtil.DATEPICKER_FORMAT_AM_PM_4) +
                    "<br>"
        } else if (batchLotStatus.getId() != null && batchLotStatus.getCount() > batchLotStatus.getValidRecordCount()) {
            mailBody = "Batch Lot Data ETL is completed with error.<br/>" +
                    "<br/>" +
                    "Error:<br>" + errorString +
                    "<br/>" +
                    "The Batch Lot Data logs are attached with the email, for ETL executed on" +
                    DateUtil.toDateStringPattern(new Date(), DateUtil.DATEPICKER_FORMAT_AM_PM_4) +
                    "<br>"
        } else if (batchLotStatus.getId() == null || batchLotStatus.getValidRecordCount() == 0) {
            mailBody = "Batch Lot Data ETL is completed with error." +
                    "<br/>" +
                    "Error:<br>" + errorString +
                    "<br>"
        }
        mailBody = mailBody + "<br>You can review that using following hyperlink <a href=\"" + alertLink + "\">PV Signal</a>"
        emailService.sendApiUpdateWithAttachementEmail(['toAddress': toAddresses,
                                                        'inboxType': "Action Updated",
                                                        'title'    : mailSubject,
                                                        'map'      : ["map": [
                                                                "apiUploadFile": files.get(0),
                                                                "isAPIUpload"  : "yes",
                                                                "emailMessage" : mailBody,
                                                                "alertLink"    : alertLink
                                                        ]]
        ])

    }

    void evdasCaseListingFileUploadEmailNotification(List<EvdasFileProcessLog> evdasFileProcessLogList, List<String> toAddresses) {
        if (isEmailNotificationEnabled(Constants.EmailNotificationModuleKeys.CASE_LISTING_UPLOAD_FILE) && Objects.nonNull(evdasFileProcessLogList) && !evdasFileProcessLogList.isEmpty()) {
            evdasFileProcessLogList.each {
                evdasFileProcessLog ->
                    if (EvdasFileProcessState.IN_PROCESS != evdasFileProcessLog.status) {
                        mailEvdasFileUploadStatus(evdasFileProcessLog, toAddresses)
                    }
            }
        }
    }

    void evdasERMRFileUploadEmailNotification(List<EvdasFileProcessLog> evdasFileProcessLogList, List<String> toAddresses) {
        if (isEmailNotificationEnabled(Constants.EmailNotificationModuleKeys.ERMR_UPLOAD_FILE) && Objects.nonNull(evdasFileProcessLogList) && !evdasFileProcessLogList.isEmpty()) {
            evdasFileProcessLogList.each {
                evdasFileProcessLog ->
                    if (EvdasFileProcessState.IN_PROCESS != evdasFileProcessLog.status) {
                        mailEvdasFileUploadStatus(evdasFileProcessLog, toAddresses)
                    }
            }
        }
    }

    private void mailEvdasFileUploadStatus(EvdasFileProcessLog evdasFileProcessLog, List<String> toAddresses) {
        String alertLink = createHref("evdasData", "index", [])
        Map map = [:]
        if (Objects.nonNull(evdasFileProcessLog)) {
            if (evdasFileProcessLog.dataType.contains("eRMR")) {
                map = eRMREmailFormat(evdasFileProcessLog)
            } else {
                map = caseLineListingEmailFormat(evdasFileProcessLog)
            }
        }
        String mailBody = map.get("mailBody")+"<br/>You can review it using following hyperlink <a href=\""+alertLink+"\">PV Signal</a>"
        toAddresses.each {
            emailService.sendMail(it, map.get("mailSubject"), ["map": [
                    "emailMessage": mailBody,
                    "alertLink": alertLink
            ] ])
        }
    }

    private Map caseLineListingEmailFormat(EvdasFileProcessLog evdasFileProcessLog) {
        String mailSubject = ""
        String mailBody = ""
        String dateRange = ""
        if (formatDateRange(evdasFileProcessLog)) {
            dateRange = formatDateRange(evdasFileProcessLog)
        } else {
            dateRange = "-"
        }
        if (EvdasFileProcessState.SUCCESS == evdasFileProcessLog.status) {
            mailSubject = "Case Listing for " + evdasFileProcessLog.substances + " uploaded successfully"
            mailBody = "The following Case Listing has been uploaded successfully<br/>" +
                    "<ul>" +
                    "<li>Substance: " + evdasFileProcessLog.substances + "</li>" +
                    "<li>Date Range: " + dateRange + "</li>" +
                    "</ul>"
        } else if (EvdasFileProcessState.FAILED == evdasFileProcessLog.status) {
            mailSubject = "Case Listing for " + evdasFileProcessLog.substances + " failed to upload"
            mailBody = "The following Case Listing failed to upload<br/>" +
                    "<ul>" +
                    "<li>Substance: " + evdasFileProcessLog.substances + "</li>" +
                    "<li>Date Range: " + dateRange + "</li>" +
                    "</ul>"
        }
        [mailSubject: mailSubject, mailBody: mailBody]
    }

    private Map eRMREmailFormat(EvdasFileProcessLog evdasFileProcessLog) {
        String mailSubject = ""
        String mailBody = ""
        if (EvdasFileProcessState.SUCCESS == evdasFileProcessLog.status) {
            mailSubject = "eRMR for " + evdasFileProcessLog.substances + " uploaded successfully"
            mailBody = "The following eRMR has been uploaded successfully<br/>" +
                    "<ul>" +
                    "<li>Substance: " + evdasFileProcessLog.substances + "</li>" +
                    "<li>Date Range: " + formatDateRange(evdasFileProcessLog) + "</li>" +
                    "</ul>"
        } else if (EvdasFileProcessState.FAILED == evdasFileProcessLog.status) {
            mailSubject = "eRMR for " + evdasFileProcessLog.substances + " failed to upload"
            mailBody = "The following eRMR failed to upload<br/>" +
                    "<ul>" +
                    "<li>Substance: " + evdasFileProcessLog.substances + "</li>" +
                    "<li>Date Range: " + formatDateRange(evdasFileProcessLog) + "</li>" +
                    "</ul>"
        }
        [mailSubject: mailSubject, mailBody: mailBody]
    }

    private String formatDateRange(EvdasFileProcessLog evdasFileProcessLog) {
        String recordStartDate = ""
        String recordEndDate = ""
        String dateRange = ""
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DEFAULT_DATE_FORMAT)
        if (Objects.nonNull(evdasFileProcessLog.recordStartDate)) {
            recordStartDate = evdasFileProcessLog.recordStartDate?sdf.format(evdasFileProcessLog.recordStartDate):""
        }
        if (Objects.nonNull(evdasFileProcessLog.recordEndDate)) {
            recordEndDate = evdasFileProcessLog.recordEndDate?sdf.format(evdasFileProcessLog.recordEndDate):""
        }
        if (StringUtils.isNotBlank(recordStartDate) || StringUtils.isNotBlank(recordEndDate)) {
            dateRange = recordStartDate + " to " + recordEndDate
        }
        return dateRange
    }

    def sendLiteratureEmailList(Map emailData) {
        def alert = emailData.alert
        List emailDataList = emailData.emailDataList.unique { it.email }
        String alertLink = emailData.alertLink
        String newUserName = emailData.newUserName

        emailDataList.each { Map emailMap ->
            String email = emailMap.email
            if (email != null) {
                mailHandlerForAssignedToLiterature(email, alert, alertLink, emailMap.emailMessage, newUserName)
            }
        }
    }

}
