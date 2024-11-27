package com.rxlogix.signal

import com.rxlogix.AggregateCaseAlertService
import com.rxlogix.AlertService
import com.rxlogix.CRUDService
import com.rxlogix.Constants
import com.rxlogix.ReportIntegrationService
import com.rxlogix.SafetyLeadSecurityService
import com.rxlogix.UserService
import com.rxlogix.ViewInstanceService
import com.rxlogix.cache.CacheService
import com.rxlogix.config.Configuration
import com.rxlogix.config.DateRangeInformation
import com.rxlogix.config.Disposition
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.Priority
import com.rxlogix.config.ReportExecutionStatus
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.enums.ReportFormat
import com.rxlogix.spotfire.SpotfireService
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.testing.web.controllers.ControllerUnitTest
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.*
import spock.util.mop.ConfineMetaClassChanges

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@ConfineMetaClassChanges([SpringSecurityUtils])
class AggregateOnDemandAlertControllerSpec extends Specification implements ControllerUnitTest<AggregateOnDemandAlertController> {

    Disposition disposition
    Disposition defaultDisposition
    Disposition autoRouteDisposition
    Configuration alertConfiguration
    ExecutedConfiguration executedConfiguration
    ExecutedConfiguration executedConfigurationNew
    Priority priority
    AggregateOnDemandAlert aggregateOnDemandAlert
    ViewInstance viewInstance
    Group wfGroup
    User user

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

        viewInstance = new ViewInstance(name: "viewInstance", alertType: "Aggregate Case Alert on Demand", user: user, columnSeq: "seq")
        viewInstance.save(flush: true, failOnError: true)

        priority = new Priority(value: "Low", display: true, displayName: "Low", reviewPeriod: 3, priorityOrder: 1)
        priority.save(flush: true, failOnError: true)
        alertConfiguration = new Configuration(
                executing: false,
                priority: "High",
                alertTriggerCases: "11",
                alertTriggerDays: "11")

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

        executedConfigurationNew = new ExecutedConfiguration(name: "test",
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
        executedConfigurationNew.save(flush: true, failOnError: true)

        aggregateOnDemandAlert = new AggregateOnDemandAlert(
                alertConfiguration: alertConfiguration, executedAlertConfiguration: executedConfiguration,
                name: executedConfiguration.name,
                productName: Constants.Commons.UNDEFINED, productId: Constants.Commons.UNDEFINED_NUM,
                soc: Constants.Commons.UNDEFINED, pt: 'pyrexia',
                ptCode: 0,
                newStudyCount: 0, cumStudyCount: 0,
                newSponCount:  0, cumSponCount:  0,
                newSeriousCount:  0, cumSeriousCount: 0,
                newFatalCount:  0, cumFatalCount:  0,
                createdBy: config.createdBy, modifiedBy: config.modifiedBy,
                dateCreated: executedConfiguration.dateCreated, lastUpdated: executedConfiguration.dateCreated,
                positiveRechallenge: Constants.Commons.BLANK_STRING,
                periodStartDate: new Date(),
                periodEndDate: new Date(),
                positiveDechallenge: Constants.Commons.BLANK_STRING,
                listed: Constants.Commons.BLANK_STRING, pregnancy: Constants.Commons.BLANK_STRING,
                related: Constants.Commons.BLANK_STRING,
                pecImpNumHigh: 0, pecImpNumLow: 0,
                ebgm: 0.0, eb95: 0.0, eb05: 0.0, rorValue: 0, rorLCI: 0, rorUCI: 0, rorStr: 0, rorStrLCI: 0,
                rorStr95: 0, rorMh: 0, prrValue: 0, prrLCI: 0, prrUCI: 0, prrStr: 0, prrStrLCI: 0, prrStrUCI: 0,
                prrMh: 0, pecImpHigh: 0, pecImpLow: 0
        )
        aggregateOnDemandAlert.save(flush: true)


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
        cacheService.getRorCache() >> { return true }
        controller.cacheService = cacheService
        controller.userService.cacheService = cacheService

        AlertService mockAlertService = Mock(AlertService)
        mockAlertService.isProductSecurity() >> { return true }
        controller.alertService = mockAlertService
    }

    void "test listConfig"(){
        when:
        controller.listConfig()

        then:
        response.status == 200
        JSON.parse(response.text).size() == 3
    }

    void "test listByExecutedConfig"(){
        when:
        controller.listByExecutedConfig()

        then:
        response.status == 200
        JSON.parse(response.text).size() == 5
    }

    void "test toggleFlag"(){
        when:
        controller.toggleFlag(aggregateOnDemandAlert.id)

        then:
        response.status == 200
        JSON.parse(response.text).success == 'ok'
    }

    void "test toggleFlag when page is not found"(){
        when:
        controller.toggleFlag(0L)

        then:
        response.status == 404
    }

    void "test adhocReview"(){
        when:
        controller.adhocReview()

        then:
        response.status == 200
        view == '/aggregateOnDemandAlert/adhocReview'
    }

    void "test editShare"(){
        when:
        params.sharedWith = "User_${user.id}"
        params.executedConfigId = executedConfiguration.id
        controller.editShare()

        then:
        response.status == 302
        response.redirectedUrl == '/aggregateOnDemandAlert/adhocReview'
    }

    void "test editShare in case of no execConfigId"(){
        when:
        params.sharedWith = "User_${user.id}"
        params.executedConfigId = executedConfiguration.id
        controller.editShare()

        then:
        response.status == 302
        response.redirectedUrl == '/aggregateOnDemandAlert/adhocReview'
    }

    void "test deleteOnDemandAlert"(){
        setup:
        AggregateOnDemandAlertService mockAggregateOnDemandAlertService = Mock(AggregateOnDemandAlertService)
        mockAggregateOnDemandAlertService.deleteOnDemandAlert(executedConfiguration) >> { return true }
        controller.aggregateOnDemandAlertService = mockAggregateOnDemandAlertService

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
        AggregateOnDemandAlertService mockAggregateOnDemandAlertService = Mock(AggregateOnDemandAlertService)
        mockAggregateOnDemandAlertService.deleteOnDemandAlert(executedConfiguration) >> { throw new Exception() }
        controller.aggregateOnDemandAlertService = mockAggregateOnDemandAlertService

        when:
        params.id = executedConfiguration.id
        controller.delete()

        then:
        response.status == 200
        JSON.parse(response.text).status == false
        JSON.parse(response.text).message != null
    }

    void "test fetchAllfieldValues"(){
        setup:
        AggregateOnDemandAlertService mockAggregateOnDemandAlertService = Mock(AggregateOnDemandAlertService)

        mockAggregateOnDemandAlertService.fieldListAdvanceFilter(false, true,executedConfiguration?.dataMiningVariable) >> { [[name: 'chisquare', display: 'ChiSquare', dataType: 'java.lang.Number']] }

        controller.aggregateOnDemandAlertService = mockAggregateOnDemandAlertService

        when:
        params.executedConfigId = executedConfiguration.id
        controller.fetchAllFieldValues()

        then:
        response.status == 200
        JSON.parse(response.text) == [[name: 'chisquare', display: 'ChiSquare', dataType: 'java.lang.Number']]
        JSON.parse(response.text).size() == 1
    }

    void "test isExcelExportFormat"(){
        when:
        boolean result = controller.isExcelExportFormat(ReportFormat.XLSX.name())

        then:
        result
    }

    void "test isExcelExportFormat if not in excel format"(){
        when:
        boolean result = controller.isExcelExportFormat(ReportFormat.PDF.name())

        then:
        !result
    }
}