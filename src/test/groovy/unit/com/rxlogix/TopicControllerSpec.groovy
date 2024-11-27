package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.signal.*
import com.rxlogix.user.User
import grails.testing.web.controllers.ControllerUnitTest
import groovy.json.JsonBuilder
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Ignore
import spock.lang.Specification

@Ignore
class TopicControllerSpec extends Specification implements ControllerUnitTest<TopicController> {

    Disposition disposition
    Configuration alertConfiguration
    ExecutedConfiguration executedConfiguration
    User user
    def attrMapObj
    SingleCaseAlert alert
    Topic topic
    MedicalConcepts medicalConcepts

    def setup() {

        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true)
        disposition.save(failOnError: true)

        Priority priority =
                new Priority([displayName : "mockPriority", value : "mockPriority", display :  true, defaultPriority : true, reviewPeriod :1])
        priority.save(failOnError:true)

        //Prepare the mock user
        user = new User(id:'1', username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.grailsApplication = grailsApplication
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.save(failOnError: true)


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
                assignedTo: user,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10)
        executedConfiguration.save(failOnError: true)

        attrMapObj = ['masterFollowupDate_5' : new Date(),
                      'masterRptTypeId_3'    : "test type",
                      'masterInitReptDate_4' : new Date(),
                      'masterFollowupDate_5' : new Date(),
                      'reportersHcpFlag_2'   : "true",
                      'masterProdTypeList_6' : "test",
                      'masterPrefTermAll_7'  : "test",
                      'assessOutcome'        : "Death",
                      'assessListedness_9'   : "test",
                      'assessAgentSuspect_10': "test"]

        alert = new SingleCaseAlert(id: 1L,
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
                createdBy: user.username,
                modifiedBy: user.username,
                dateCreated: user.dateCreated,
                lastUpdated: user.dateCreated,
                productFamily: "Test Product A",
                attributesMap: attrMapObj)
        alert.save(failOnError: true)

        def adHocAlertObj = new AdHocAlert(
                productSelection: "something",
                detectedDate: new DateTime(2015,12, 15, 0, 0, 0).toDate(),
                detectedBy: "Company",
                initialDataSource: "test",
                name: "Test Name2",
                alertVersion: 0,
                priority: new Priority(value: "High"),
                topic:"rash",
                assignedTo: user
        )
        adHocAlertObj.save(failOnError: true)


        def aca = new AggregateCaseAlert(
                alertConfiguration: config,
                executedAlertConfiguration: executedConfiguration,
                name: executedConfiguration.name,
                priority: priority,
                disposition: disposition,
                assignedTo: user,
                detectedDate: executedConfiguration.dateCreated,
                productName: "Test Product A",
                productId: 12321312,
                soc:  "BODY_SYS1",
                pt: 'Rash',
                ptCode: 1421,
                hlt: 'TEST',
                hglt: 'TEST',
                llt: "INC_TERM2",
                newStudyCount: 1,
                cumStudyCount: 1,
                newSponCount: 1,
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
                periodStartDate: new Date(),
                periodEndDate: new Date(),
                adhocRun: false,
                positiveRechallenge: "true",
                positiveDechallenge: "false",
                listed: "false",
                pregenency: "false",
                related: "false",
                flagged: false,
                format: "test"

        )
        aca.save(failOnError: true)

        def obj = ["caseNumber" : "16US000684",
                   "caseVersion": "1",
                   "alertId"    : "1",
                   "event"      : "1.Rash",
                   "currentUser": "1"]

        def obj1 = new JsonBuilder(obj)
        def obj0 = ["caseNumber" : "16US000684",
                    "caseVersion": "1",
                    "alertId"    : "1",
                    "event"      : "1.Rash",
                    "currentUser": "1"]
        def obj2 = new JsonBuilder(obj0)
        controller.params.alertArray = [obj1, obj2].toString()

        medicalConcepts = new MedicalConcepts(name: "New Concept")
        medicalConcepts.save(failOnError: true)

        topic = new Topic(
                name: "test_topic_1",
                products: "Test Product A",
                attachmentable: true,
                initialDataSource: "Test DS",
                disposition: disposition,
                assignedTo: user,
                priority: alert.priority,
                startDate: alert.detectedDate,
                endDate: alert.detectedDate,
                createdBy: alert.createdBy,
                dateCreated: alert.dateCreated,
                lastUpdated: alert.lastUpdated,
                modifiedBy: alert.modifiedBy
        )
        topic.save(failOnError: true)

        controller.CRUDService = ["update": {}]
    }

    def "Test the addBatchAlertToTopic action for SingleCaseAlerts"(){
        setup:
        controller.params.topicName = "test_topic_1"
        controller.params.appType = Constants.AlertConfigType.SINGLE_CASE_ALERT
        controller.params.medicalConcepts = "1"
        controller.params.alertId = "1"

        when:
        controller.addBatchAlertToTopic()

        then:
        response.status == 200

    }

    def "Test the addBatchAlertToTopic action for AggregateCaseAlerts"(){
        setup:
        controller.params.topicName = "test_topic_1"
        controller.params.appType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT
        controller.params.medicalConcepts = "1"
        controller.params.alertId = "1"

        when:
        controller.addBatchAlertToTopic()

        then:
        response.status == 200

    }

    def "Test the addBatchAlertToTopic action for AdHocAlerts"(){
        setup:
        controller.params.topicName = "test_topic_1"
        controller.params.appType = Constants.AlertConfigType.AD_HOC_ALERT
        controller.params.medicalConcepts = "1"
        controller.params.alertId = "1"

        when:
        controller.addBatchAlertToTopic()

        then:
        response.status == 200

    }
}
