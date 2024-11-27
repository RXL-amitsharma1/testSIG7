package unit.com.rxlogix

import com.rxlogix.AdvancedFilterService
import com.rxlogix.Constants
import com.rxlogix.UserService
import com.rxlogix.config.AdvancedFilter
import com.rxlogix.config.Comment
import com.rxlogix.config.Configuration
import com.rxlogix.config.Disposition
import com.rxlogix.config.EvdasAlert
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.Priority
import com.rxlogix.config.ReportExecutionStatus
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.AlertComment
import com.rxlogix.signal.CaseHistory
import com.rxlogix.signal.GlobalCaseCommentMapping
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.test.hibernate.HibernateSpec
import grails.testing.services.ServiceUnitTest
import grails.util.Holders
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Ignore
import spock.lang.Specification

class AdvancedFilterServiceSpec extends HibernateSpec implements ServiceUnitTest<AdvancedFilterService> {

    AdvancedFilter advanceFilter
    Disposition defaultDisposition
    Disposition defaultSignalDisposition
    Group wfGroup
    User userObj
    Disposition disposition
    ExecutedConfiguration executedConfiguration
    CaseHistory caseHistory
    SingleCaseAlert singleCaseAlert
    Configuration con
    Priority priority
    AlertComment comment1, comment2

    List<Class> getDomainClasses() {
        [User, AdvancedFilter,Group,ExecutedConfiguration,CaseHistory,SingleCaseAlert, UserService, AlertComment]
    }

    def setup() {
        defaultSignalDisposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "VO")
        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")

        wfGroup = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP, createdBy: 'createdBy', modifiedBy: 'modifiedBy',
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                defaultSignalDisposition: defaultSignalDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(flush:true)

        userObj = new User()
        userObj.addToGroups(wfGroup)
        userObj.username = 'username'
        userObj.createdBy = 'createdBy'
        userObj.modifiedBy = 'modifiedBy'
        userObj.preference.createdBy = "createdBy"
        userObj.preference.modifiedBy = "modifiedBy"
        userObj.preference.locale = new Locale("en")
        userObj.preference.isEmailEnabled = false
        userObj.metaClass.getFullName = { "Fake Namer" }
        userObj.metaClass.getEmail = { 'fake.email@fake.com' }

        userObj.save(flush:true)

        advanceFilter = new AdvancedFilter()
        advanceFilter.alertType = "Single Case Alert"
        advanceFilter.criteria = "{ ->\n" +
                "criteriaConditions('listedness','EQUALS','true')\n" +
                "}"
        advanceFilter.createdBy = "fakeuser"
        advanceFilter.dateCreated = new Date()
        advanceFilter.description = "test advanced filter"
        advanceFilter.JSONQuery = "{\"all\":{\"containerGroups\":[ {\"expressions\":[ {\"index\":\"0\",\"field\":\"listedness\",\"op\":\"EQUALS\",\"value\":\"true\"} ] }  ] } }"
        advanceFilter.lastUpdated = new Date()
        advanceFilter.modifiedBy = "fakeuser"
        advanceFilter.name = "ad listed 1"
        advanceFilter.user = userObj
        advanceFilter.save(validate:false)
        priority = new Priority([displayName: "mockPriority", value: "mockPriority", display: true, defaultPriority: true, reviewPeriod: 1])
        priority.save(flush: true, failOnError: false)
        con = new Configuration(assignedTo: userObj, productSelection: "[TestProduct]", name: "test",
                owner: userObj, createdBy: userObj.username, modifiedBy: userObj.username, selectedDatasource: "safety", priority: priority)
        con.save(flush: true)

        disposition = new Disposition(value: "ValidatedSignal1", displayName: "Validated Signal1", validatedConfirmed: true,
                abbreviation: "C")
        disposition.save(flush: true, failOnError: true)

        executedConfiguration = new ExecutedConfiguration(name: "test", configId: con.id,
                owner: userObj, scheduleDateJSON: "{}", nextRunDate: new Date(),
                description: "test", dateCreated: new Date(), lastUpdated: new Date(),
                isPublic: true, isDeleted: false, isEnabled: true, spotfireSettings: "{\"type\":\"VACCINCE\",\"rangeType\":[\"PR_DATE_RANGE\",\"CUMULATIVE\"]}",
                dateRangeType: DateRangeTypeCaseEnum.CASE_LOCKED_DATE,
                productSelection: "['testproduct2']", eventSelection: "['rash']", studySelection: "['test']",
                configSelectedTimeZone: "UTC",
                evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION,
                limitPrimaryPath: true,
                includeMedicallyConfirmedCases: true,
                excludeFollowUp: false, includeLockedVersion: true,
                adjustPerScheduleFrequency: true,
                createdBy: userObj.username, modifiedBy: userObj.username,
                assignedTo: userObj, type: 'Single Case Alert', adhocRun: false, isLatest: true,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10, pvrCaseSeriesId: 9l)
        executedConfiguration.save(flush: true, failOnError: true)

        singleCaseAlert = new SingleCaseAlert(id: 903,
                productSelection: "something",
                detectedDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                name: "Test Name",
                caseId: 1212,
                caseNumber: "1S01",
                caseVersion: 1,
                alertConfiguration: con,
                executedAlertConfiguration: executedConfiguration,
                reviewDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                priority: con.priority,
                disposition: disposition,
                assignedTo: userObj,
                productName: "Test Product A",
                pt: "Rash",
                productFamily: "Test Product A",
                isNew: true,
                productId: 100004,
                followUpExists: true,
                comboFlag: "flag1",
                malfunction: "function1",
                justification: "just"
        )
        singleCaseAlert.save(flush: true, failOnError: true)

        caseHistory = new CaseHistory(currentDisposition: disposition, currentAssignedTo: userObj, currentAssignedToGroup:
                wfGroup, currentPriority: new Priority(value: "tooLow", displayName: "priority1"), caseNumber: "1S01", caseVersion: 1,
                productFamily: "productFamily1", isLatest: true, followUpNumber: 1, justification: "JUST-1",execConfigId: executedConfiguration.id)
        caseHistory.save(flush: true, failOnError: true)


            comment1 = new AlertComment(productId: 10l, eventName: "Rash", comments: "comm-1",createdBy:"signaldev",modifiedBy:"signaldev",
                    configId: con.id, alertType: Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
            comment1.save(flush: true, failOnError:true)
            comment2 = new AlertComment(productName: "aj", eventName: "Rash", comments: "comm-2",createdBy:"signaldev",modifiedBy:"signaldev",
                    configId: con.id, alertType: Constants.AlertConfigType.EVDAS_ALERT)
            comment2.save(flush: true, failOnError:true)
        service.transactionManager = getTransactionManager()
    }

    def cleanup() {
    }
    @Ignore
    void "test getAjaxAdvFilter"() {
        setup:
        service.userService = [getUser: { return userObj }]

        when:
        def result = service.getAjaxAdvFilter("Single Case Alert", "", 0, 10)

        then:
        result.size() == 1 //returns 2 view instances with current user
        result[0].name == "ad listed 1"
    }

    void "test createAdvancedFilterCriteria for SUBGROUP"() {
        setup:
        when:
        String result = service.createAdvancedFilterCriteria("{\"all\":{\"containerGroups\":[ {\"expressions\":[ {\"index\":\"0\",\"field\":\"EBGM:Confident\",\"op\":\"EQUALS\",\"value\":\"153\"} ] }  ] } }")
        then:
        result == "{ ->\n" +
                "criteriaConditionsForSubGroup('EBGM:Confident','EQUALS',\"153\")\n" +
                "}"
    }


    void "test getAjaxFilterData for justification"(){
        when:
        Map result = service.getAjaxFilterData("USt",0,10,executedConfiguration.id,"justification", SingleCaseAlert, [isFaers:false])
        then:
        result == [jsonData:[["id":"just","text":"just"]], possibleValuesListSize:0]
    }

    void "test getAjaxFilterData when no condition matches"(){
        when:
        Map result = service.getAjaxFilterData("pro",0,10,executedConfiguration.id,"productName", SingleCaseAlert, [isFaers:false])
        then:
        result == [jsonData:[["id":"Test Product A","text":"Test Product A"]], possibleValuesListSize:0]
    }

    void "test getAjaxFilterData when no condition matches and executedConfig id is not given"(){
        when:
        Map result = service.getAjaxFilterData("pro",0,10,null,"productName", SingleCaseAlert, [isFaers:false])
        then:
        result == [jsonData:[["id":"Test Product A","text":"Test Product A"]], possibleValuesListSize:0]
    }
    void "test getAjaxFilterData for comment in aggregate alert"(){
        when:
        Map result = service.getAjaxFilterData("com",0,10,executedConfiguration.id,Constants.AdvancedFilter.COMMENT, AggregateCaseAlert, [isFaers:false])
        then:
        result == [jsonData:[[id:"comm-1", text:"comm-1"]], possibleValuesListSize:1]
    }
    void "test getAjaxFilterData for comment in evdas alert"(){
        when:
        Map result = service.getAjaxFilterData("com",0,10,executedConfiguration.id,Constants.AdvancedFilter.COMMENT, EvdasAlert, [isFaers:false])
        then:
        result == [jsonData:[[id:"comm-2", text:"comm-2"]], possibleValuesListSize:1]
    }
    void "test getAjaxFilterDataTotalCount for justification"(){
        when:
        Long result = service.getAjaxFilterDataTotalCount("ust",executedConfiguration.id,"justification", SingleCaseAlert)
        then:
        result == 1
    }
    void "test getAjaxFilterDataTotalCount for justification when executed configuration is not given"(){
        when:
        Long result = service.getAjaxFilterDataTotalCount("ust",executedConfiguration.id,"justification", SingleCaseAlert)
        then:
        result == 1
    }

    void "test getAjaxFilterDataTotalCount when no condition matches and executedConfig id is given"(){
        when:
        Long result = service.getAjaxFilterDataTotalCount("pro",executedConfiguration.id,"productName", SingleCaseAlert)
        then:
        result == 1
    }
    void "test getAjaxFilterDataTotalCount when no condition matches and executedConfig id is not given"(){
        when:
        Long result = service.getAjaxFilterDataTotalCount("pro",executedConfiguration.id,"productName", SingleCaseAlert)
        then:
        result == 1
    }
    void "test maxSeqCommentList for AggregateCaseAlert"(){
        when:
        List result = service.maxSeqCommentList(AggregateCaseAlert, executedConfiguration.id,false)
        then:
        result == [[comments:"comm-1"]]
    }
    void "test maxSeqCommentList for EvdasAlert"(){
        when:
        List result = service.maxSeqCommentList(EvdasAlert, executedConfiguration.id,false)
        then:
        result == [[comments:"comm-2"]]
    }
}
