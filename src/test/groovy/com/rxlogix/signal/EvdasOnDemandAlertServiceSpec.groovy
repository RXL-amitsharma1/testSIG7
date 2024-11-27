package com.rxlogix.signal

import com.rxlogix.EvdasAlertService
import com.rxlogix.EvdasOnDemandAlertService
import com.rxlogix.config.Disposition
import com.rxlogix.config.EVDASDateRangeInformation
import com.rxlogix.config.EvdasAlert
import com.rxlogix.config.EvdasConfiguration
import com.rxlogix.config.EvdasOnDemandAlert
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.config.Priority
import com.rxlogix.config.ReportExecutionStatus
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.testing.services.ServiceUnitTest
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.slf4j.Logger
import spock.lang.*


class EvdasOnDemandAlertServiceSpec extends Specification implements ServiceUnitTest<EvdasOnDemandAlertService> {

    Disposition disposition
    Disposition defaultDisposition
    Disposition defaultSignalDisposition
    Disposition autoRouteDisposition
    ExecutedEvdasConfiguration executedAlertConfiguration
    EvdasOnDemandAlert evdasOnDemandAlert
    User user
    Priority priority
    Logger logger
    EvdasConfiguration evdasConfiguration
    EVDASDateRangeInformation evdasDateRangeInformation

    def setup() {
        logger = Mock(Logger)
        service.log = logger

        priority = new Priority([displayName: "mockPriority", value: "mockPriority", display: true, defaultPriority: true, reviewPeriod: 1])
        priority.save(failOnError: true,flush:true)
        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: false, reviewCompleted: true, abbreviation: 'vs')
        disposition.save(flush:true)

        defaultSignalDisposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "VO")
        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")

        [defaultDisposition, autoRouteDisposition].collect { it.save(failOnError: true) }

        Group wfGroup = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP, createdBy: 'createdBy', modifiedBy: 'modifiedBy',
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                defaultSignalDisposition: defaultSignalDisposition,
                autoRouteDisposition: autoRouteDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(flush: true)

        //Save the  user
        user = new User(id: '1', username: 'test_user', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.addToGroups(wfGroup)
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.save(flush:true)

        evdasDateRangeInformation = new EVDASDateRangeInformation(dateRangeStartAbsoluteDelta: 2, dateRangeEndAbsoluteDelta: 5, dateRangeEnum: DateRangeEnum.LAST_YEAR,
                dateRangeStartAbsolute: Date.parse('dd-MMM-yyyy', '01-Jan-2014'), dateRangeEndAbsolute: Date.parse('dd-MMM-yyyy', '31-Dec-2014'))

        def now = new Date()
        def recurrenceJSON = /{"startDateTime":"$now","recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}/

        evdasConfiguration = new EvdasConfiguration(name: 'test', owner: user, assignedTo: user, createdBy: 'createdBy', dateRangeInformation: evdasDateRangeInformation,
                frequency: "Yearly", modifiedBy: 'modifiedBy', priority: priority, scheduleDateJSON: recurrenceJSON, productSelection: 'productA', dateCreated: new Date(),
                lastUpdated: new Date(), numOfExecutions: 1, adhocRun: true, assignedToGroup: wfGroup, nextRunDate: new Date(), totalExecutionTime: 30L)
        evdasConfiguration.save(flush: true)

        executedAlertConfiguration = new ExecutedEvdasConfiguration(name: "test",
                owner: user, scheduleDateJSON: "{}", nextRunDate: new Date(),
                description: "test", dateCreated: new Date(), lastUpdated: new Date(),
                isPublic: true, isDeleted: true, isEnabled: true,
                productSelection: "['testproduct2']", eventSelection: "['rash']",
                configSelectedTimeZone: "UTC",
                createdBy: user.username, modifiedBy: user.username,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10, configId: 1L)
        executedAlertConfiguration.save(flush:true)

        evdasOnDemandAlert = new EvdasOnDemandAlert(
                executedAlertConfiguration: executedAlertConfiguration,
                name: executedAlertConfiguration.name,
                priority: priority,
                disposition: disposition,
                assignedTo: user,
                detectedDate: executedAlertConfiguration.dateCreated,
                substance: "Test Product A",
                substanceId: 12321312,
                soc: "BODY_SYS1",
                pt: 'Rash',
                ptCode: 1421,
                hlt: 'TEST',
                hglt: 'TEST',
                llt: "INC_TERM2",
                newEv: 1,
                totalEv: 1,
                createdBy: config.assignedTo.username,
                modifiedBy: config.assignedTo.username,
                dateCreated: executedAlertConfiguration.dateCreated,
                lastUpdated: executedAlertConfiguration.dateCreated,
                dueDate: new Date(),
                periodStartDate: new DateTime(2017, 7, 1, 0, 0, 0, DateTimeZone.forID('UTC')).toDate(),
                periodEndDate: new DateTime(2017, 12, 31, 0, 0, 0, DateTimeZone.forID('UTC')).toDate(),
                adhocRun: false,
                flagged: false,
                format: "test",
                evImpEventList: ['EVDAS_IMP_EVENTS','EVDAS_ALERT_IMP_EVENT_LIST','EVDAS_ALERT_ID'],
        )
        evdasOnDemandAlert.save(flush:true,failOnError:true)

    }

    void "test toggleEvdasOnDemandAlertFlag"(){
        when:
        boolean result = service.toggleEvdasOnDemandAlertFlag(evdasOnDemandAlert.id)

        then:
        result
    }

    void "test printExecutionMessage"(){
        when:
        service.printExecutionMessage(evdasConfiguration, executedAlertConfiguration, [evdasOnDemandAlert])

        then:
        logger.info("Alert data save flow is complete.")
    }

    void "test setFlagsForAlert"(){
        given:
        List<Map> alertData = [[substance: 'Test Product B', ptCode: '1422']]

        when:
        Map result = service.setFlagsForAlert(alertData, new Date(), executedAlertConfiguration)

        then:
        result == ['Test Product B-1422': 'New']
    }

}