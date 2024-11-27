package com.rxlogix.signal

import com.rxlogix.*
import com.rxlogix.cache.CacheService
import com.rxlogix.config.*
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.spotfire.SpotfireService
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([SpringSecurityUtils])
class EvdasOnDemandAlertControllerSpec extends Specification implements ControllerUnitTest<EvdasOnDemandAlertController> {

    Disposition disposition
    Disposition defaultDisposition
    Disposition autoRouteDisposition
    Configuration alertConfiguration
    ExecutedConfiguration executedConfiguration
    Priority priority
    EvdasOnDemandAlert evdasOnDemandAlert
    ViewInstance viewInstance
    Group wfGroup
    User user
    EvdasConfiguration evdasConfiguration
    EVDASDateRangeInformation evdasDateRangeInformation
    ExecutedEvdasConfiguration executedEvdasConfiguration

    def setup() {
        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "vs")
        disposition.save(flush: true, failOnError: true)

        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")

        [defaultDisposition, autoRouteDisposition].collect { it.save(failOnError: true) }

        wfGroup = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP, defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                createdBy: 'createdBy', modifiedBy: 'modifiedBy',
                defaultSignalDisposition: disposition,
                autoRouteDisposition: autoRouteDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(flush: true)

        user = new User(id: '1', username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.addToGroups(wfGroup);
        user.save(flush: true)

        viewInstance = new ViewInstance(name: "viewInstance", alertType: "EVDAS Alert on Demand", user: user, columnSeq: "seq")
        viewInstance.save(flush: true, failOnError: true)

        priority = new Priority(value: "Low", display: true, displayName: "Low", reviewPeriod: 3, priorityOrder: 1)
        priority.save(flush: true, failOnError: true)
        alertConfiguration = new Configuration(
                executing: false,
                priority: "High",
                alertTriggerCases: "11",
                alertTriggerDays: "11")

        evdasDateRangeInformation = new EVDASDateRangeInformation(dateRangeStartAbsoluteDelta: 2, dateRangeEndAbsoluteDelta: 5, dateRangeEnum: DateRangeEnum.LAST_YEAR,
                dateRangeStartAbsolute: Date.parse('dd-MMM-yyyy', '01-Jan-2014'), dateRangeEndAbsolute: Date.parse('dd-MMM-yyyy', '31-Dec-2014'))

        def now = new Date()
        def recurrenceJSON = /{"startDateTime":"$now","recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}/

        evdasConfiguration = new EvdasConfiguration(name: 'test', owner: user, assignedTo: user, createdBy: 'createdBy', dateRangeInformation: evdasDateRangeInformation,
                frequency: "Yearly", modifiedBy: 'modifiedBy', priority: priority, scheduleDateJSON: recurrenceJSON, productSelection: 'productA', dateCreated: new Date(),
                lastUpdated: new Date(), numOfExecutions: 1, adhocRun: true, assignedToGroup: wfGroup, nextRunDate: new Date(), totalExecutionTime: 30L)
        evdasConfiguration.save(flush: true)

        executedConfiguration = new ExecutedConfiguration(name: "test",
                owner: user, scheduleDateJSON: "{}", nextRunDate: new Date(),
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
                createdBy: user.username, modifiedBy: user.username,
                assignedTo: user, configId: 1,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10)
        executedConfiguration.save(flush: true, failOnError: true)

        executedEvdasConfiguration = new ExecutedEvdasConfiguration(name: "test",
                owner: user, scheduleDateJSON: "{}", nextRunDate: new Date(),
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
                createdBy: user.username, modifiedBy: user.username,
                assignedTo: user, configId: 1, frequency: 'Monthly',
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10)
        executedEvdasConfiguration.save(failOnError: true)

        evdasOnDemandAlert = new EvdasOnDemandAlert(
                alertConfiguration: evdasConfiguration,
                executedAlertConfiguration: executedEvdasConfiguration,
                name: user.name,
                createdBy: config.createdBy,
                modifiedBy: config.modifiedBy,
                dateCreated: evdasConfiguration.dateCreated,
                lastUpdated: evdasConfiguration.dateCreated,
                frequency: 'monthly',
                rorValue: "1",
                allRor: "1",
                totalFatal: 1,
                newFatal: 1,
                totalSerious: 1,
                newSerious: 1,
                totalEv: 1,
                newEv: 1,
                totalEvLink: "1",
                newEvLink: "1",
                dmeIme: "1",
                pt: 'pyrexia',
                ptCode: 1,
                soc: 'body_sys',
                newLit: "1",
                totalLit: "1",
                sdr: '1',
                smqNarrow: 'smq',
                substance: 'apremilast',
                substanceId: 12,
                hlgt: 'hlgt',
                hlt: 'hlt',
                newEea: '1',
                totEea: '1',
                newHcp: '1',
                totHcp: '1',
                newMedErr: '1',
                totMedErr: '1',
                newObs: '1',
                totObs: '1',
                newRc: '1',
                totRc: '1',
                newPaed: '1',
                totPaed: '1',
                newGeria: '1',
                totGeria: '1',
                listedness: false,
                impEvents: "imp",
        )
        evdasOnDemandAlert.save(flush: true)

        SubstanceFrequency substanceFrequency = new SubstanceFrequency(name: 'Test Product', startDate: Date.parse('dd-MMM-yyyy', '01-Jan-2014'), endDate: Date.parse('dd-MMM-yyyy', '31-Dec-2014'),
                uploadFrequency: 'Yearly', miningFrequency: 'Yearly', frequencyName: "Yearly", alertType: Constants.AlertConfigType.EVDAS_ALERT_DEMAND)
        substanceFrequency.save(flush: true)

        SpringSecurityService springSecurityService = Mock(SpringSecurityService)
        springSecurityService.loggedIn >> {
            return true
        }
        springSecurityService.principal >> {
            return user
        }
        controller.userService.springSecurityService = springSecurityService
        controller.CRUDService.userService.springSecurityService = springSecurityService
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String s -> return true }
        Configuration.metaClass.static.lock = Configuration.&get
        CacheService cacheService = Mock(CacheService)
        cacheService.getUserByUserId(_) >> {
            return user
        }
        cacheService.getGroupByGroupId(_) >> {
            return wfGroup
        }
        controller.cacheService = cacheService
        controller.userService.cacheService = cacheService

        UserService mockUserService = Mock(UserService)
        mockUserService.getUserEvdasConfigurations() >> { return [user.name] }
        mockUserService.getUser() >> { return user }
        mockUserService.getCurrentUserId() >> { return user.id }
        controller.userService = mockUserService
    }

    void "test listByExecutedConfig"(){
        when:
        controller.listByExecutedConfig(false, executedEvdasConfiguration.id)

        then:
        response.status == 200
        JSON.parse(response.text).size() == 5
    }

    void "test adhocDetails"(){
        when:
        controller.adhocDetails()

        then:
        response.status == 200
        view == '/evdasOnDemandAlert/adhocDetails'
    }


    void "test toggleFlag"(){
        when:
        params.id = evdasOnDemandAlert.id
        controller.toggleFlag()

        then:
        response.status == 200
        JSON.parse(response.text).success == 'ok'
    }

    void "test toggleFlag when page is not found"(){
        when:
        params.id = 0
        controller.toggleFlag()

        then:
        response.status == 404
    }

    void "test adhocReview"(){
        when:
        controller.adhocReview()

        then:
        response.status == 200
        view == '/evdasOnDemandAlert/adhocReview'
    }

    void "test fetchSubstanceFreqNames"(){
        when:
        List result = controller.fetchSubstanceFreqNames()

        then:
        result.size() == 1
        result == ['Monthly']
    }

    void "test deleteOnDemandAlert"(){
        setup:
        EvdasOnDemandAlertService mockEvdasOnDemandAlertService = Mock(EvdasOnDemandAlertService)
        mockEvdasOnDemandAlertService.deleteOnDemandAlert(executedEvdasConfiguration) >> { return true }
        controller.evdasOnDemandAlertService = mockEvdasOnDemandAlertService

        when:
        params.id = executedConfiguration.id
        controller.delete()

        then:
        response.status == 200
        JSON.parse(response.text).status == true
        JSON.parse(response.text).message != null
    }

    void "test deleteOnDemandAlert in case of Exception"(){
        setup:
        EvdasOnDemandAlertService mockEvdasOnDemandAlertService = Mock(EvdasOnDemandAlertService)
        mockEvdasOnDemandAlertService.deleteOnDemandAlert(executedEvdasConfiguration) >> { throw new Exception() }
        controller.evdasOnDemandAlertService = mockEvdasOnDemandAlertService

        when:
        params.id = executedEvdasConfiguration.id
        controller.delete()

        then:
        response.status == 200
        JSON.parse(response.text).status == false
        JSON.parse(response.text).message != null
    }
}