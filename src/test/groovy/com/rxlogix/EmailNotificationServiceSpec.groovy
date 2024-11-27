package com.rxlogix

import com.rxlogix.cache.CacheService
import com.rxlogix.config.Configuration
import com.rxlogix.config.Disposition
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.Meeting
import com.rxlogix.config.ReportExecutionStatus
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.user.Group
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import grails.testing.services.ServiceUnitTest
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Specification
import com.rxlogix.user.UserGroupMapping
import com.rxlogix.enums.GroupType

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
//@TestFor(EmailNotificationService)
//@Mock([EmailNotification,User,ExecutedConfiguration,AggregateCaseAlert,Disposition,Configuration])
class EmailNotificationServiceSpec extends Specification implements ServiceUnitTest<EmailNotificationService> {
    EmailNotification emailNotification
    ExecutedConfiguration executedConfiguration
    User user
    AggregateCaseAlert aggregateCaseAlert_a
    Disposition disposition
    Configuration alertConfiguration
    Group wfGroup
    Disposition DefaultDisposition

    def setup() {

        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "vs")
        disposition.save(failOnError: true)

        DefaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")

        wfGroup = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP,
                createdBy: 'createdBy', modifiedBy: 'modifiedBy',
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                defaultSignalDisposition: defaultDisposition,
                autoRouteDisposition: defaultDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(flush: true)
        emailNotification = new EmailNotification(key: 'Test', isEnabled: true, defaultValue: true, moduleName: 'For test').save(failOnError: true)
        user = new User(username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = {"Fake Namer"}
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        UserGroupMapping userGroupMapping = new UserGroupMapping(user: user, group: wfGroup)
        userGroupMapping.save(flush: true, failOnError: true)
        user.save(validate:false)
        executedConfiguration = new ExecutedConfiguration(name: "test",
                scheduleDateJSON: "{}", nextRunDate: new Date(),
                description: "test", dateCreated: new Date(), lastUpdated: new Date(),
                isPublic: true, isDeleted: true, isEnabled: true,
                dateRangeType: DateRangeTypeCaseEnum.CASE_LOCKED_DATE,
                productSelection: "['testproduct2']", eventSelection: "['rash']", studySelection: "['test']",
                configSelectedTimeZone: "UTC",
                evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION,
                limitPrimaryPath: true,
                includeMedicallyConfirmedCases: true,
                excludeFollowUp: false, includeLockedVersion: true,
                adjustPerScheduleFrequency: true,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10)
        executedConfiguration.save(validate:false)
        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, reviewCompleted: true, abbreviation: "vs")
        disposition.save(validate:false)
        alertConfiguration = new Configuration(
                executing: false,
                alertTriggerCases: 11,
                alertTriggerDays: 11,
                selectedDatasource: "pva",
                name: "test",
                productSelection: "Test Product A",
                createdBy: user.username, modifiedBy: user.username,
                assignedTo: user,
                owner: user
        )
        alertConfiguration.save(validate:false)
        aggregateCaseAlert_a = new AggregateCaseAlert(
                alertConfiguration: alertConfiguration,
                executedAlertConfiguration: executedConfiguration,
                name: executedConfiguration.name,
                disposition: disposition,
                assignedTo: user,
                detectedDate: executedConfiguration.dateCreated,
                productName: "Test Product A",
                soc: "BODY_SYS1",
                pt: 'Rash',
                hlt: 'TEST',
                hglt: 'TEST',
                llt: "INC_TERM2",
                newStudyCount: 1,
                cumStudyCount: 1,
                cumSponCount: 1,
                newSeriousCount: 1,
                cumSeriousCount: 1,
                newFatalCount: 1,
                cumFatalCount: 1,
                prrValue: "1",
                prrLCI: "1",
                prrUCI: "1",
                prrStr: "1",
                prrStrLCI: "1",
                prrStrUCI: "1",
                prrMh: "1",
                rorValue: "1",
                rorLCI: "1",
                rorUCI: "1",
                rorStr: "1",
                rorStrLCI: "1",
                rorStrUCI: "1",
                rorMh: "1",
                pecImpHigh: "1",
                pecImpLow: "1",
                createdBy: config.assignedTo.username,
                modifiedBy: config.assignedTo.username,
                dateCreated: executedConfiguration.dateCreated,
                lastUpdated: executedConfiguration.dateCreated,
                eb05: new Double(1),
                eb95: new Double(1),
                ebgm: new Double(2),
                dueDate: new Date(),
                periodStartDate: new DateTime(2017, 7, 1, 0, 0, 0, DateTimeZone.forID('UTC')).toDate(),
                periodEndDate: new DateTime(2017, 12, 31, 0, 0, 0, DateTimeZone.forID('UTC')).toDate(),
                adhocRun: false,
                positiveRechallenge: "true",
                positiveDechallenge: "false",
                listed: "false",
                pregenency: "false",
                related: "false",
                flagged: false,
                format: "test",
                frequency: "Yearly",
                productId: 100083,
                ptCode: 10029404,
                newSponCount: 2,
                smqCode: 1213,
                eb05Str: "male:0.1, female:0.2, unknown:0.3",
                eb95Str: "male:0.1, female:0.2, unknown:0.3")
        aggregateCaseAlert_a.save(validate:false)
    }

    void 'test getKeys'() {
        given:
        grailsApplication.config.mail.notification.modules = [[key: 'Email'], [key: 'Notification'], [key: 'Test']]
        when:
        List keys = service.getKeys()
        then:
        keys.size() > 0

    }

    def 'test getEmailNotificationModules'() {
        when:
        Map moduleMap = service.getEmailNotificationModules()
        then:
        moduleMap.size() > 0

    }

    def 'test isEmailNotificationEnabled'() {
        when:
        def mockCacheService = Mock(CacheService)
        service.cacheService = mockCacheService
        service.cacheService.isEmailNotificationEnabled('test') >> true
        then:
        assert true == service.isEmailNotificationEnabled('test')
    }

    def 'test getEmailNotificationMap'() {
        when:
        def mockCacheService = Mock(CacheService)
        service.cacheService = mockCacheService
        service.cacheService.isEmailNotificationEnabled('test') >> true
        service.cacheService.getModuleName('test') >> 'testModule'
        Map mockNotificationMap = service.getEmailNotificationMap('test')
        then:
        assert true == mockNotificationMap.isEnabled
        assert mockNotificationMap.moduleName.equals('testModule')
    }

    def 'test emailNotificationWithBulkUpdate' (){
        when:
        def mockCacheService = Mock(CacheService)
        service.cacheService = mockCacheService
        service.cacheService.isEmailNotificationEnabled('test') >> false
        service.cacheService.getModuleName('test') >> 'testModule'
        boolean bulkUpdate = service.emailNotificationWithBulkUpdate(false, 'test')
        then:
        assert false == bulkUpdate
    }
    def 'test emailHanlderAtAlertLevel for single case alert' (){
        setup:
        service.metaClass.emailNotificationWithBulkUpdate = {Boolean bulkUpdate, String key ->
                return false
        }
        executedConfiguration.type = Constants.AlertConfigType.SINGLE_CASE_ALERT
        executedConfiguration.save(validate:false)
        when:
        service.emailHanlderAtAlertLevel(executedConfiguration,executedConfiguration.executionStatus)
        then:
        executedConfiguration.type == Constants.AlertConfigType.SINGLE_CASE_ALERT
    }
    def 'test emailHanlderAtAlertLevel for selected datasource pva' (){
        setup:
        service.metaClass.emailNotificationWithBulkUpdate = {Boolean bulkUpdate, String key ->
            return false
        }
        executedConfiguration.type = Constants.AlertConfigType.AGGREGATE_CASE_ALERT
        executedConfiguration.selectedDatasource = Constants.DataSource.DATASOURCE_PVA
        executedConfiguration.save(validate:false)
        when:
        service.emailHanlderAtAlertLevel(executedConfiguration,executedConfiguration.executionStatus)
        then:
        executedConfiguration.selectedDatasource == Constants.DataSource.DATASOURCE_PVA
    }
    def 'test emailHanlderAtAlertLevel for selected datasource VAERS' (){
        setup:
        service.metaClass.emailNotificationWithBulkUpdate = {Boolean bulkUpdate, String key ->
            return false
        }
        executedConfiguration.type = Constants.AlertConfigType.AGGREGATE_CASE_ALERT
        executedConfiguration.selectedDatasource = Constants.DataSource.DATASOURCE_VAERS
        executedConfiguration.save(validate:false)
        when:
        service.emailHanlderAtAlertLevel(executedConfiguration,executedConfiguration.executionStatus)
        then:
        executedConfiguration.selectedDatasource == Constants.DataSource.DATASOURCE_VAERS
    }
    def 'test emailHanlderAtAlertLevel for selected datasource FAERS ' (){
        setup:
        service.metaClass.emailNotificationWithBulkUpdate = {Boolean bulkUpdate, String key ->
            return false
        }
        executedConfiguration.type = Constants.AlertConfigType.AGGREGATE_CASE_ALERT
        executedConfiguration.selectedDatasource = Constants.DataSource.DATASOURCE_FAERS
        executedConfiguration.save(validate:false)
        when:
        service.emailHanlderAtAlertLevel(executedConfiguration,executedConfiguration.executionStatus)
        then:
        executedConfiguration.selectedDatasource == Constants.DataSource.DATASOURCE_FAERS
    }
    def 'mailHandlerForDispChangeACA for dataSource PVA'(){
        setup:
        boolean notificationEnabled = false
        service.metaClass.isEmailNotificationEnabled = { String key->
            return false
        }
        when:
        service.mailHandlerForDispChangeACA(aggregateCaseAlert_a,Constants.DataSource.DATASOURCE_PVA,disposition,disposition,false,false)
        then:
        notificationEnabled ==false
    }
    def 'mailHandlerForDispChangeACA for dataSource VAERS'(){
        setup:
        boolean notificationEnabled = false
        service.metaClass.isEmailNotificationEnabled = { String key->
            return false
        }
        when:
        service.mailHandlerForDispChangeACA(aggregateCaseAlert_a,Constants.DataSource.DATASOURCE_VAERS,disposition,disposition,false,false)
        then:
        notificationEnabled ==false
    }
    def 'mailHandlerForDispChangeACA for dataSource FAERS'(){
        setup:
        boolean notificationEnabled = false
        service.metaClass.isEmailNotificationEnabled = { String key->
            return false
        }
        when:
        service.mailHandlerForDispChangeACA(aggregateCaseAlert_a,Constants.DataSource.DATASOURCE_FAERS,disposition,disposition,false,false)
        then:
        notificationEnabled ==false
    }

    def 'mailHandlerForMeetingDelete '() {
        setup:
        service.metaClass.isEmailNotificationEnabled = { String key ->
            return false
        }
        Preference preference = new Preference(timeZone: 'UTC')
        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> {
            new User(username: 'ownerUser', preference: preference)
        }
        service.userService = mockUserService
        User owner = new User(username: 'ownerUser')
        User attendee = new User(username: 'attendingUser')
        Meeting meeting = new Meeting(meetingTitle: 'test Meeting', meetingDate: new Date() + 1, meetingOwner: owner, attendees: attendee)
        when:
        service.mailHandlerForMeetingDelete(meeting)
        then:
        noExceptionThrown()
    }
}
