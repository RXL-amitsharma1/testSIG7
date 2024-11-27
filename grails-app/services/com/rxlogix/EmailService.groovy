package com.rxlogix

import com.rxlogix.audit.AuditTrail
import com.rxlogix.cache.CacheService
import com.rxlogix.config.*
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.helper.LinkHelper
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.SingleOnDemandAlert
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.gsp.PageRenderer
import grails.plugins.mail.MailService
import grails.util.Holders

class EmailService implements LinkHelper{
    MailService mailService
    PageRenderer groovyPageRenderer
    UserService userService
    def messageSource
    ProductBasedSecurityService productBasedSecurityService
    CacheService cacheService
    AlertService alertService
    DataObjectService dataObjectService
    def notificationHelper
    def signalAuditLogService
    def pvsProductDictionaryService


    List<EmailLog> lstEmailLog = []
    List<InboxLog> lstInboxLog = []


    def methodMissing(String name, args) {
        if (name ==~ /send.*Email/ && args && [Collection, Object[]].any { it.isAssignableFrom(args.getClass()) }) {
            sendNotifyingEmails(name, args)
        } else
            throw new MissingMethodException(name, this.class, args)
    }

    def sendNotifyingEmails(methodName, args) {
        def rst = methodName =~ ~/send(.*)Email/
        if (rst.find()) {
            def type = rst[0][1]

            Long currentMS = args['currentMS']? args['currentMS'][0] as Long : 0L
            boolean rmmNotification = (args['allowNotification'] == [true]) ? false : true

            def address = args['toAddress']
            if (address) {
                def title = args['title'][0]
                def inboxType = args['inboxType'].join(",")
                address.each { it ->
                    log.info(">>>>>>>>>>>>>>> Sending email to : ${it}")
                    if(it instanceof List) {
                            it.removeAll(Collections.singleton(null))
                    }
                    try {
                        sendEmailHtml(it.flatten(), title, type, args['map'])
                        if(rmmNotification){
                            logMail(it.join(","), title, type, args['map'], inboxType, currentMS)
                        }
                    } catch (Throwable t) {
                        log.error("Sending email failed", t)
                    }
                    log.info(">>>>>>>>>>>>>>> sent email to : ${it}")
                }

                if(currentMS > 0) {
                    dataObjectService.setEmailLogMap(currentMS, lstEmailLog)
                    dataObjectService.setInboxLogMap(currentMS, lstInboxLog)
                }

            }
        }
    }

    def sendEmailHtml(targets, String title, String type, model) {
        type = (type == null || type == "") ? "" : type.substring(0, 1).toLowerCase() + type.substring(1)
        def view = "/email/${type ?: 'email/pvsEmail'}"
        def timezone = Holders.config.server.timezone
        if (Holders.config.grails.mail.enabled) {
            log.debug("Sending email")
            log.debug("-----------")
            log.debug("to: ${targets}")
            log.debug("subject: ${title}")
            log.debug("-----------")
            log.debug("model:")
            log.debug(model.toString())
            log.debug("-----------")
            try {
                def map = [type: type, title: title, timezone: timezone] << model
                if( map.containsKey('map') && map.get("map").containsKey('apiUploadFile')) {
                    sendApiUpdateWithAttachementsMail(targets, title, view, map)
                } else if( map.containsKey('map') && map.get("map").containsKey('pgsUploadFile')) {
                        sendPGsUpdateWithAttachementsMail(targets, title, view, map)
                } else {
                    sendMail(targets, title, view, map)
                }

            } catch (Exception e) {
                log.debug("Could not send email: ${e.class} - ${e.message}")
                log.debug("Email content: ${targets} -- ${title} -- $model")
                e.printStackTrace()
            }
        }
    }

    def sendMail(targets, String title, String view, Map map) throws Exception {
        mailService.sendMail {
            multipart true
            async true
            from "${Holders.config.grails.mail.displayName} <${Holders.config.grails.mail.default.from}>"
            to targets
            subject title
            body(view: view, model: map)
            if( map.containsKey('map') && map.get("map").containsKey('apiUploadFile')) {
                attachBytes map.get("map").get("apiUploadFile").get("name") , 'text/csv', map.get("map").get("apiUploadFile").get("file")
            }
            if (map.containsKey('attachments')) {
                map['attachments'].each { attach(it.name,it.file) }
            }
        }
    }
    def sendPGsUpdateWithAttachementsMail(targets, String title, String view, Map map) throws Exception {
        if(map.get("map").get("isPgsUpload")!=null && "no".equals(String.valueOf(map.get("map").get("isPgsUpload")))  ) {
            mailService.sendMail {
                multipart true
                async true
                from "${Holders.config.grails.mail.displayName} <${Holders.config.grails.mail.default.from}>"
                to targets
                subject title
                html map.get("map").get("emailMessage")
            }
        } else {
            mailService.sendMail {
                multipart true
                async true
                from "${Holders.config.grails.mail.displayName} <${Holders.config.grails.mail.default.from}>"
                to targets
                subject title
                html map.get("map").get("emailMessage")
                attach(map.get("map").get("pgsUploadFile").get("name") ,map.get("map").get("pgsUploadFile").get("file") )
            }
        }
    }
    def sendApiUpdateWithAttachementsMail(targets, String title, String view, Map map) throws Exception {
        mailService.sendMail {
            multipart true
            async true
            from "${Holders.config.grails.mail.displayName} <${Holders.config.grails.mail.default.from}>"
            to targets
            subject title
            html map.get("map").get("emailMessage")
            attach(map.get("map").get("apiUploadFile").get("name") ,map.get("map").get("apiUploadFile").get("file") )
        }
    }

    def sendMail(targets, String title, Map map) throws Exception {
        mailService.sendMail {
            multipart true
            async true
            from "${Holders.config.grails.mail.displayName} <${Holders.config.grails.mail.default.from}>"
            to targets
            subject title
            html map.get("map").get("emailMessage")
        }
    }

    void logMail(String sentTo, String title, String type, model, String inboxType = "Notification", Long currentMS) {
        type = (type == null || type == "") ? "" : type
        String view = "/email/text/log${type ?: 'email/text/pvsEmail'}"
        String message = model.message[0]
        String detailUrl=model.detailUrl[0]
        String validatedConfigId=model.validatedConfigId[0]
        if (currentMS > 0) {
            lstEmailLog.add(new EmailLog(subject: title, sentTo: sentTo, sentOn: new Date(), message: message))
        } else {
            new EmailLog(subject: title, sentTo: sentTo, sentOn: new Date(), message: message).save()
        }

        List<String> userEmail = sentTo.split(',')
        userEmail = userEmail.findAll { it }?.unique()
        if (!(inboxType.trim() in [Constants.AlertType.QUALITATIVE_ALERT, Constants.AlertType.QUANTITATIVE_ALERT,
                                   Constants.AlertType.EVDAS_ALERT, Constants.AlertType.LITERATURE_ALERT,Constants.Commons.TRIGGERED_ALERT])) {
            userEmail.each {
                if (it) {
                    Long id = userService.getUserIdFromEmail(it)
                    if (currentMS > 0) {
                        if (id) {
                            lstInboxLog.add(new InboxLog(type: inboxType, subject: title, content: message, createdOn: new Date(),detailUrl:detailUrl,
                                    inboxUserId: id, isNotification: true,executedConfigId: validatedConfigId,message:message))
                        }
                    } else {
                        if (id) {
                            if (!(title.contains(Constants.DynamicReports.CASE_FORM_EXPORT) && inboxType == Constants.DynamicReports.ASSIGNED_TO_CHANGE)) {
                                InboxLog inboxLog = new InboxLog(type: inboxType, subject: title, content: message, createdOn: new Date(), detailUrl: detailUrl,
                                        inboxUserId: id, isNotification: true, executedConfigId: validatedConfigId, message: message)
                                inboxLog.save(flush: true)
                                notificationHelper.pushNotification(inboxLog)
                            }
                        }
                    }
                }
            }
        }
    }


    void sendEmailNotification(def executedConfiguration, ExecutionStatus executionStatus) {
        if ((executedConfiguration instanceof ExecutedConfiguration) && (executedConfiguration.type == Constants.AlertConfigType.SINGLE_CASE_ALERT) && (executionStatus.executionStatus == ReportExecutionStatus.COMPLETED)) {
            userService.getUserListFromAssignToGroup(executedConfiguration).each { User user ->
                Integer totalCases = 0
                String userName = executedConfiguration.modifiedBy
                List<Map> totalCountList = alertService.getTotalCountList(executedConfiguration.adhocRun ? SingleOnDemandAlert : SingleCaseAlert, [executedConfiguration.id])
                if (totalCountList?.size() > 0) {
                    totalCases = totalCountList.get(0)?.cnt ?: 0
                }
                if (user.email) {
                    sendEmailSingleNotification(executedConfiguration, executionStatus, [user.email], totalCases)
                }
            }

        } else {
            List<String> toAddressList = userService.getRecipientsList(executedConfiguration)
            if (toAddressList) {
                sendEmailSingleNotification(executedConfiguration, executionStatus, toAddressList, null)
            }
        }
    }

    void sendEmailSingleNotification(def executedConfiguration, ExecutionStatus executionStatus, List<String> toAddress, Integer totalCases) {
        try {
            String alertLink
            String query = ""
            String dateRange = getDateRangeValue(executedConfiguration)
            Locale locale = executedConfiguration?.assignedTo?.preference?.locale ?: Locale.ENGLISH
            String productName = ''
            String productSelection = (messageSource.getMessage('app.label.signal.product.name', null, locale))
            String productNameSubList = ''
            String productNameValue = ''
            if (executedConfiguration instanceof ExecutedConfiguration) {
                if(executedConfiguration.productNameList){
                    productNameValue = executedConfiguration.productNameList?.join(", ")
                } else if (executedConfiguration.productGroupSelection) {
                    productNameValue = JSON.parse(executedConfiguration.productGroupSelection).collect{it.name.substring(0,it.name.lastIndexOf('(') - 1)}.join(', ')
                } else if (executedConfiguration.studySelection) {
                    productNameValue = MiscUtil.getStudyDictionaryValues(executedConfiguration.studySelection).collect {it.values()}.flatten().join(', ')
                    productSelection = (messageSource.getMessage('app.reportField.studyBlindName', null, locale))
                }
                productName = pvsProductDictionaryService.isLevelGreaterThanProductLevel(executedConfiguration) ? cacheService.getUpperHierarchyProductDictionaryCache(executedConfiguration.id) : productNameValue
            } else {
                productName = ViewHelper.getDictionaryValues(executedConfiguration, DictionaryTypeEnum.PRODUCT)
            }
            productNameSubList = productName
            Integer maxProdSize = Holders.config.signal.email.productName.size as Integer
            if (productNameSubList.length() > maxProdSize) {
                productNameSubList = productNameSubList.substring(0, maxProdSize) + '...'
            }
            String typeLabel
            if (executedConfiguration instanceof ExecutedEvdasConfiguration) {
                alertLink = executedConfiguration.adhocRun? createHref("evdasOnDemandAlert", "adhocDetails", ["configId": executedConfiguration.id, "callingScreen": "review"]) : createHref("evdasAlert", "details", ["configId": executedConfiguration.id, "callingScreen": "review"])
                typeLabel = messageSource.getMessage('app.label.agg.evdas.rule', null, locale)
                query = executedConfiguration.executedQueryName ?: ""
            } else if (executedConfiguration instanceof ExecutedLiteratureConfiguration) {
                alertLink = createHref("literatureAlert", "details", ["configId": executedConfiguration.id, "callingScreen": "review"])
                typeLabel = messageSource.getMessage('app.label.literature.alert', null, locale)
            } else if (executedConfiguration instanceof ExecutedConfiguration) {
                if (executedConfiguration.type == Constants.AlertConfigType.SINGLE_CASE_ALERT) {
                    alertLink = executedConfiguration.adhocRun? createHref("singleOnDemandAlert", "adhocDetails", ["configId": executedConfiguration.id, "callingScreen": "review"]) : createHref("singleCaseAlert", "details", ["configId": executedConfiguration.id, "callingScreen": "review"])
                    typeLabel = messageSource.getMessage('app.label.single.case.alert.rule', null, locale)
                } else {
                    alertLink = executedConfiguration.adhocRun? createHref("aggregateOnDemandAlert", "adhocDetails", ["configId": executedConfiguration.id, "callingScreen": "review"]) : createHref("aggregateCaseAlert", "details", ["configId": executedConfiguration.id, "callingScreen": "review"])
                    typeLabel = messageSource.getMessage('app.label.agg.alert.rule', null, locale)
                }
                query = executedConfiguration.alertQueryName ?: ""
                if (executedConfiguration.executedAlertQueryValueLists?.size() > 0) {
                    query += ",  " + messageSource.getMessage('app.label.criteria', null, locale) + ": ["
                    executedConfiguration.executedAlertQueryValueLists.each { q ->
                        query += messageSource.getMessage('app.label.queryName', null, locale) + ": " + q.queryName + ", " + messageSource.getMessage('app.label.parameters', null, locale) + ": ["
                        q.parameterValues?.each {
                            if (it.hasProperty('reportField')) {
                                query += messageSource.getMessage("app.reportField.${it.reportField.name}", null, locale) + " "
                                query += messageSource.getMessage(it.operator.getI18nKey(), null, locale) + " " + it.value
                            } else {
                                query += it.key + " : " + it.value
                            }
                        }
                        query += "]"
                    }
                    query += "]"
                }

            }


            String subject = typeLabel + " " + messageSource.getMessage('app.label.email.message.configurationExecution.executedSuccess', [productNameSubList] as Object[], locale)
            if(executedConfiguration instanceof ExecutedLiteratureConfiguration){
                subject = messageSource.getMessage('app.label.email.message.literature.execution.executedSuccess', [executedConfiguration.name] as Object[], locale)
            }

            String message = messageSource.getMessage('app.label.email.message.configurationExecution.message', null, locale) +
                    (totalCases != null ? messageSource.getMessage('app.label.email.message.configurationExecution.total', [totalCases] as Object[], locale) : "")

            if (executionStatus.executionStatus == ReportExecutionStatus.ERROR) {
                String alertType = executionStatus.type.trim().toUpperCase().replace(" ", "_")
                alertLink = createHref("configuration", "executionStatus", ["configId": executedConfiguration.id, "callingScreen": "review", alertType: alertType,alertStatus:"ERROR"])
                message = messageSource.getMessage('app.label.email.message.configurationExecution.fail', null, locale)

                subject = typeLabel + " " + messageSource.getMessage('app.label.email.message.configurationExecution.executedFail', [productNameSubList] as Object[], locale)
                if(executedConfiguration instanceof ExecutedLiteratureConfiguration){
                    subject = messageSource.getMessage('app.label.email.message.literature.execution.executedFail', [executedConfiguration.name] as Object[], locale)
                }

            }

            Map emailContentMap = [(messageSource.getMessage('app.label.alert.name', null, locale)): executedConfiguration.name,
                                   (messageSource.getMessage('app.label.alert.type', null, locale)): typeLabel,
                                   "${productSelection}" : productName,
                                   (messageSource.getMessage('app.label.email.message.configurationExecution.selectedEvent', null, locale)): ViewHelper.getEventDictionaryValues(executedConfiguration),
                                   (messageSource.getMessage('app.label.DateRange', null, locale)): dateRange,
                                   (messageSource.getMessage('app.label.assigned.to', null, locale)) : userService.getAssignedToName(executedConfiguration)
            ]

            if (!(executedConfiguration instanceof ExecutedLiteratureConfiguration)) {
                emailContentMap.put((messageSource.getMessage('label.description', null, locale)), executedConfiguration.description)
                emailContentMap.put((messageSource.getMessage('app.label.queryCriteria', null, locale)), query)
                emailContentMap.put((messageSource.getMessage('app.label.priority', null, locale)), executedConfiguration.priority?.displayName)
            } else {
                emailContentMap.put((messageSource.getMessage('app.label.searchString', null, locale)), executedConfiguration.searchString ?: " ")
            }


            sendNotificationEmail([
                    'toAddress': toAddress,
                    'inboxType': typeLabel,
                    'title'    : subject,
                    'allowNotification': true,
                    'map'      : ["map"         : emailContentMap,
                                  "emailMessage": message,
                                  "alertLink"   : alertLink]])
        } catch (Throwable e) {
            log.error("Error sending Email Notification: ${e.message}", e)
            e.printStackTrace()
        }
    }

    String getDateRangeValue(def executedConfiguration){
        List dateRangeList = []
        String dateRange = ""
        if(executedConfiguration instanceof ExecutedConfiguration){
            dateRangeList = executedConfiguration.executedAlertDateRangeInformation.getReportStartAndEndDate()
        }else{
            dateRangeList = executedConfiguration.dateRangeInformation.getReportStartAndEndDate()
        }
        dateRange = dateRangeList[0]?.format(DateUtil.DEFAULT_DATE_FORMAT) + " - " + dateRangeList[1]?.format(DateUtil.DEFAULT_DATE_FORMAT)
    }

    void sendPasswordChangeEmail(String username, String newPassword, def recipients) {
        try {
            String emailSubject = ViewHelper.getMessage("app.label.email.password")
            String emailBody = ViewHelper.getMessage("app.label.hi") + "<br>"
            emailBody += ViewHelper.getMessage("app.label.email.defaulBody" , [username , newPassword] as Object[])
            sendMail(recipients,emailSubject,["map":["emailMessage": emailBody]])
        } catch (Throwable t) {
            log.error('Error occurred when sending emails', t)
        }
    }


}
