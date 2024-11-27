package com.rxlogix

import com.rxlogix.config.Configuration
import com.rxlogix.config.Disposition
import com.rxlogix.config.EmailLog
import com.rxlogix.config.ExecutedAlertDateRangeInformation
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutionStatus
import com.rxlogix.config.ReportExecutionStatus
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.gsp.PageRenderer
import grails.plugins.mail.MailService
import grails.testing.services.ServiceUnitTest
import grails.web.mapping.LinkGenerator
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification


class EmailServiceSpec extends Specification implements ServiceUnitTest<EmailService> {
    @Shared
    User user
    Group wfGroup
    Disposition disposition
    Disposition defaultSignalDisposition
    Disposition autoRouteDisposition
    Disposition defaultDisposition
    ExecutedConfiguration executedConfiguration
    ExecutionStatus executionStatus
    UserService userService
    ExecutedAlertDateRangeInformation executedAlertDateRangeInformation
    Configuration configuration

    def setup() {
        disposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        defaultSignalDisposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "VO")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")

        [disposition, defaultDisposition, defaultSignalDisposition, autoRouteDisposition].collect {
            it.save(failOnError: true)
        }

        wfGroup = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP,
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                defaultSignalDisposition: defaultSignalDisposition,
                autoRouteDisposition: autoRouteDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(validate: false)

        user = new User(id: '1', username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.addToGroups(wfGroup)
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = Locale.ENGLISH
        user.preference.isEmailEnabled = true
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.save(failOnError: true)

        defineBeans {
            customerMessageService(CustomMessageService) {bean->
                bean.autowire = true
            }
        }
    }

    void "Test mail notification"(){
        given:
        messageSource.addMessage 'app.label.email.message.configurationExecution.message', Locale.ENGLISH, 'The following alert has been executed successfully and requires your review.'
        messageSource.addMessage 'app.label.email.message.configurationExecution.total', Locale.ENGLISH, 'There are {0} number of cases returned.'
        messageSource.addMessage 'app.label.agg.alert.rule', Locale.ENGLISH, 'Quantitative Alert'
        messageSource.addMessage 'app.label.email.message.configurationExecution.executedSuccess', Locale.ENGLISH, 'for Product {0} executed successfully'
        messageSource.addMessage 'app.label.alert.name', Locale.ENGLISH, 'Alert Name'
        messageSource.addMessage 'app.label.alert.type', Locale.ENGLISH, 'Alert Type'
        messageSource.addMessage 'label.description', Locale.ENGLISH, 'Description'
        messageSource.addMessage 'app.label.signal.product.name', Locale.ENGLISH, 'Product Name'
        messageSource.addMessage 'app.label.email.message.configurationExecution.selectedEvent', Locale.ENGLISH, 'Selected Event'
        messageSource.addMessage 'app.label.DateRange', Locale.ENGLISH, 'Date Range'
        messageSource.addMessage 'app.label.queryCriteria', Locale.ENGLISH, 'Query Criteria'
        messageSource.addMessage 'app.label.assigned.to', Locale.ENGLISH, 'Assigned To'
        messageSource.addMessage 'app.label.priority', Locale.ENGLISH, 'Priority'

        executedAlertDateRangeInformation = new ExecutedAlertDateRangeInformation()
        executedConfiguration = new ExecutedConfiguration(name: "test",
                owner: user, scheduleDateJSON: "{}", nextRunDate: new Date(),
                description: "test", dateCreated: new Date(), lastUpdated: new Date(),
                isPublic: true, isDeleted: true, isEnabled: true,
                dateRangeType: DateRangeTypeCaseEnum.CASE_LOCKED_DATE,
                configSelectedTimeZone: "UTC",
                evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION,
                limitPrimaryPath: true,
                includeMedicallyConfirmedCases: true,
                excludeFollowUp: false, includeLockedVersion: true,
                adjustPerScheduleFrequency: true,
                createdBy: user.username, modifiedBy: user.username,
                assignedTo: user,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10,
                executedAlertDateRangeInformation: executedAlertDateRangeInformation)
        configuration = new Configuration(owner: user, createdBy: user.username, modifiedBy: user.username)
        executionStatus = new ExecutionStatus(configId: configuration.id,
                startTime: new Date().getTime(),
                endTime: new Date().getTime(),
                reportVersion: 1, message: 'for testing purpose',
                owner: user, name: 'executionStatus')

        when:"Generating notification"

        userService = Mock(UserService)
        userService.metaClass.getUserIdFromEmail = {1232}
        service.grailsLinkGenerator = Mock(LinkGenerator)
        service.groovyPageRenderer = Mock(PageRenderer)
        service.metaClass.logMail = {}
        service.messageSource = messageSource
        service.userService = userService
        service.sendEmailSingleNotification(executedConfiguration, executionStatus, [user.email], 10)

        then:
        notThrown(Exception)
    }
}
