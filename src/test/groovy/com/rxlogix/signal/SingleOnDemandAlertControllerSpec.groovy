package com.rxlogix.signal

import com.rxlogix.*
import com.rxlogix.cache.CacheService
import com.rxlogix.config.*
import com.rxlogix.config.metadata.SourceColumnMaster
import com.rxlogix.config.metadata.SourceTableMaster
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.*
import com.rxlogix.spotfire.SpotfireService
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.testing.web.controllers.ControllerUnitTest
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

@ConfineMetaClassChanges([SpringSecurityUtils])
class SingleOnDemandAlertControllerSpec extends Specification implements ControllerUnitTest<SingleOnDemandAlertController> {

    Disposition disposition
    Disposition defaultDisposition
    Disposition autoRouteDisposition
    Configuration alertConfiguration
    ExecutedConfiguration executedConfiguration
    ExecutedConfiguration executedConfigurationNew
    def attrMapObj
    Priority priority
    SingleOnDemandAlert singleOnDemandAlert
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

        viewInstance = new ViewInstance(name: "viewInstance", alertType: "Single Case Alert on Demand", user: user, columnSeq: "seq")
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

        singleOnDemandAlert = new SingleOnDemandAlert(id: 1L,
                productSelection: "something",
                detectedDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                name: "Test Name",
                caseNumber: "1S01",
                caseVersion: 1,
                alertConfiguration: alertConfiguration,
                executedAlertConfiguration: executedConfiguration,
                reviewDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                priority: alertConfiguration.priority,
                disposition: disposition,
                assignedTo: user,
                productName: "Test Product A",
                pt: "Rash",
                createdBy: config.assignedTo.username,
                modifiedBy: config.assignedTo.username,
                dateCreated: config.dateCreated,
                lastUpdated: config.dateCreated,
                productFamily: "Test Product A",
                isNew: true,
                productId: 3982,
                assignedToGroup: wfGroup,
                followUpExists: true,
                attributesMap: attrMapObj,
                comboFlag: 'combo',
                malfunction: 'mal')
        singleOnDemandAlert.save(flush: true, failOnError: true)
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
    }

    void "test listConfig"(){
        when:
        controller.listConfig()

        then:
        response.status == 200
        JSON.parse(response.text).size() == 3
    }

    void "test adhocReview"(){
        when:
        controller.adhocReview()

        then:
        response.status == 200
        view == '/singleOnDemandAlert/adhocReview'
    }

    void "test toggleFlag"(){
        when:
        controller.toggleFlag(singleOnDemandAlert.id)

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

    void "test fetchAllFieldValues"(){
        setup:
        List<Map> fieldValues = grailsApplication.config.signal.scaOnDemandColumnList as List<Map>

        when:
        controller.fetchAllFieldValues()

        then:
        response.status == 200
        JSON.parse(response.text).size() == fieldValues.size()
    }

    void "test deleteOnDemandAlert"(){
        setup:
        SingleOnDemandAlertService mockSingleOnDemandAlertService = Mock(SingleOnDemandAlertService)
        mockSingleOnDemandAlertService.deleteOnDemandAlert(executedConfiguration) >> { return true }
        controller.singleOnDemandAlertService = mockSingleOnDemandAlertService

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
        SingleOnDemandAlertService mockSingleOnDemandAlertService = Mock(SingleOnDemandAlertService)
        mockSingleOnDemandAlertService.deleteOnDemandAlert(executedConfiguration) >> { throw new Exception() }
        controller.singleOnDemandAlertService = mockSingleOnDemandAlertService

        when:
        params.id = executedConfiguration.id
        controller.delete()

        then:
        response.status == 200
        JSON.parse(response.text).status == false
        JSON.parse(response.text).message != null
    }

    void "test editShare"(){
        when:
        params.sharedWith = "User_${user.id}"
        params.executedConfigId = executedConfiguration.id
        controller.editShare()

        then:
        response.status == 302
        response.redirectedUrl == '/singleOnDemandAlert/adhocReview'
    }

    void "test editShare in case of no execConfigId"(){
        when:
        params.sharedWith = "User_${user.id}"
        params.executedConfigId = executedConfiguration.id
        controller.editShare()

        then:
        response.status == 302
        response.redirectedUrl == '/singleOnDemandAlert/adhocReview'
    }
}