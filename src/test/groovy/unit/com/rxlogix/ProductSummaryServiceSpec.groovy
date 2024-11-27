package unit.com.rxlogix

import com.rxlogix.*
import com.rxlogix.config.*
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.AlertComment
import com.rxlogix.signal.SubstanceFrequency
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.testing.services.ServiceUnitTest
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Ignore
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */

class ProductSummaryServiceSpec extends Specification implements ServiceUnitTest<ProductSummaryService> {

    def attrMapObj
    Disposition disposition
    Disposition defaultDisposition
    Disposition defaultSignalDisposition
    Disposition autoRouteDisposition
    User user
    Priority priority
    AggregateCaseAlert alert, prevAlert
    Configuration alertConfiguration
    ExecutedConfiguration executedConfiguration
    ExecutedEvdasConfiguration executedEvdasConfiguration
    EvdasAlert evdasAlert, prevEvdasAlert
    ValidatedSignal validatedSignal
    AlertComment alertComment

    def setup() {
        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, reviewCompleted: true, abbreviation: 'vs')
        disposition.save(failOnError: true)

        priority =
                new Priority([displayName: "mockPriority", value: "mockPriority", display: true, defaultPriority: true, reviewPeriod: 1])
        priority.save(failOnError: true)

        defaultSignalDisposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "VO")
        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")

        [defaultDisposition, autoRouteDisposition].collect { it.save(failOnError: true) }

        Group wfGroup = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP, createdBy: 'createdBy',
                modifiedBy: 'modifiedBy',
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

        mockDomain(Priority, [
                [value: "Low", display: true, displayName: "Low", reviewPeriod: 3, priorityOrder: 1]
        ])


        alertConfiguration = new Configuration(
                executing: false,
                alertTriggerCases: 11,
                alertTriggerDays: 11,
                selectedDatasource: "pva",
                name: "test",
                productSelection: "Test Product A",
                createdBy: user.username, modifiedBy: user.username,
                assignedTo: user,
                priority: priority,
                owner: user
        )
        alertConfiguration.save(failOnError: true)

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
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10,configId: alertConfiguration?.id)
        executedConfiguration.save(failOnError: true)

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
                assignedTo: user,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10,configId:1234)
        executedEvdasConfiguration.save(failOnError: true)

        attrMapObj = ['masterFollowupDate_5' : new Date(),
                      'masterRptTypeId_3'    : "test type",
                      'masterInitReptDate_4' : new Date(),
                      'reportersHcpFlag_2'   : "true",
                      'masterProdTypeList_6' : "test",
                      'masterPrefTermAll_7'  : "test",
                      'assessOutcome'        : "Death",
                      'assessListedness_9'   : "test",
                      'assessAgentSuspect_10': "test"]

        alert = new AggregateCaseAlert(
                alertConfiguration: alertConfiguration,
                executedAlertConfiguration: executedConfiguration,
                name: executedConfiguration.name,
                priority: priority,
                disposition: disposition,
                assignedTo: user,
                detectedDate: executedConfiguration.dateCreated,
                productName: "Test Product A",
                productId: 12321312,
                soc: "BODY_SYS1",
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
                periodStartDate: new DateTime(2017, 7, 1, 0, 0, 0, DateTimeZone.forID('UTC')).toDate(),
                periodEndDate: new DateTime(2017, 12, 31, 0, 0, 0, DateTimeZone.forID('UTC')).toDate(),
                adhocRun: false,
                positiveRechallenge: "true",
                positiveDechallenge: "false",
                listed: "false",
                pregenency: "false",
                related: "false",
                flagged: false,
                format: "test")
        alert.save(failOnError: true)

        evdasAlert = new EvdasAlert(
                alertConfiguration: config,
                executedAlertConfiguration: executedEvdasConfiguration,
                name: executedEvdasConfiguration.name,
                priority: priority,
                disposition: disposition,
                assignedTo: user,
                detectedDate: new Date(),
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
                dateCreated: executedEvdasConfiguration.dateCreated,
                lastUpdated: executedEvdasConfiguration.dateCreated,
                dueDate: new Date(),
                periodStartDate: new DateTime(2017, 7, 1, 0, 0, 0, DateTimeZone.forID('UTC')).toDate(),
                periodEndDate: new DateTime(2017, 12, 31, 0, 0, 0, DateTimeZone.forID('UTC')).toDate(),
                adhocRun: false,
                flagged: false,
                format: "test",
        )
        evdasAlert.save(failOnError: true)
        prevEvdasAlert = new EvdasAlert(
                alertConfiguration: config,
                executedAlertConfiguration: executedEvdasConfiguration,
                name: executedEvdasConfiguration.name,
                priority: priority,
                disposition: disposition,
                assignedTo: user,
                detectedDate: new Date(),
                substance: "Test Product A",
                substanceId: 12321312,
                soc: "BODY_SYS1",
                pt: 'Rash',
                ptCode: 1421,
                hlt: 'TEST',
                hglt: 'TEST',
                llt: "INC_TERM2",
                newEv: 2,
                totalEv: 1,
                createdBy: config.assignedTo.username,
                modifiedBy: config.assignedTo.username,
                dateCreated: executedEvdasConfiguration.dateCreated,
                lastUpdated: executedEvdasConfiguration.dateCreated,
                dueDate: new Date(),
                periodStartDate: new DateTime(2017, 7, 1, 0, 0, 0, DateTimeZone.forID('UTC')).toDate(),
                periodEndDate: new DateTime(2017, 12, 31, 0, 0, 0, DateTimeZone.forID('UTC')).toDate(),
                adhocRun: false,
                flagged: false,
                format: "test"

        )
        prevEvdasAlert.save(failOnError: true)

        prevAlert = new AggregateCaseAlert(
                alertConfiguration: alertConfiguration,
                executedAlertConfiguration: executedConfiguration,
                name: executedConfiguration.name,
                priority: priority,
                disposition: disposition,
                assignedTo: user,
                detectedDate: executedConfiguration.dateCreated,
                productName: "Test Product A",
                productId: 12321312,
                soc: "BODY_SYS1",
                pt: 'Rash',
                ptCode: 1421,
                hlt: 'TEST',
                hglt: 'TEST',
                llt: "INC_TERM2",
                newStudyCount: 1,
                cumStudyCount: 1,
                newSponCount: 2,
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
                eb05: new Double(2),
                eb95: new Double(3),
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
                format: "test")
        prevAlert.save(failOnError: true)

        validatedSignal = new ValidatedSignal(
                name: "test_name",
                products: "test_products",
                endDate: new Date(),
                assignedTo: user,
                assignmentType: 'USER',
                modifiedBy: user.username,
                priority: priority,
                disposition: disposition,
                createdBy: user.username,
                startDate: new Date(),
                genericComment: "Test notes",
                workflowGroup: wfGroup
        )

        alertComment = new AlertComment(productName: "Test Product A", eventName: "Rash", alertType: Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
                comments: "Test", createdBy: config.assignedTo.username,
                modifiedBy: config.assignedTo.username)
        validatedSignal.addToAggregateAlerts(alert)
        validatedSignal.addToEvdasAlerts(evdasAlert)
        validatedSignal.addToComments(alertComment)
        validatedSignal.save(failOnError: true)
        service.alertCommentService = mockService(AlertCommentService)

    }

    def cleanup() {
    }

    @Ignore
    void "test getProductSummaryIndexMap"() {
        when:
        def dispositionInstances = [new Disposition(value: "Requires Review", displayName: "Requires Review", validatedConfirmed: false),
                                    new Disposition(value: "New Potential Signal", displayName: "New Potential Signal", validatedConfirmed: false)]
        mockDomain(Disposition, dispositionInstances)
        service.dispositionService = mockService(DispositionService)
        def caseDetailMap = service.getProductSummaryIndexMap()
        then:
        caseDetailMap.size() == 3
        caseDetailMap['disposition'].size() == 3
        caseDetailMap['dataSourceMap'].size() == 3

    }

    @Ignore
    void "test getProductSummaryIndexMap when disposition is empty"() {
        setup:
        def mockDispService = Mock(DispositionService)
        mockDispService.dispositionListByDisplayName() >> []
        service.dispositionService = mockDispService
        when:
        def caseDetailMap = service.getProductSummaryIndexMap()
        then:
        caseDetailMap.size() == 3
        caseDetailMap['disposition'] == null
    }

    @Ignore
    void "test getSelectedDispositions when disposition is string"() {
        setup:
        def dispositionInstances = [new Disposition(value: "Requires Review", displayName: "Requires Review", validatedConfirmed: false),
                                    new Disposition(value: "New Potential Signal", displayName: "New Potential Signal", validatedConfirmed: false)]
        mockDomain(Disposition, dispositionInstances)
        service.dispositionService = mockService(DispositionService)
        def dispositionValue = "New Potential Signal"
        when:
        def dispositionList = service.getSelectedDispositions(dispositionValue)
        then:
        dispositionList.size() == 1

    }

    @Ignore
    void "test getSelectedDispositions when string contains ["() {
        setup:
        def dispositionInstances = [new Disposition(value: "Requires Review", displayName: "Requires Review", validatedConfirmed: false),
                                    new Disposition(value: "New Potential Signal", displayName: "New Potential Signal", validatedConfirmed: false)]
        mockDomain(Disposition, dispositionInstances)
        service.dispositionService = mockService(DispositionService)
        def dispositionValue = "[New Potential Signal,Requires Review]"
        when:
        def dispositionList = service.getSelectedDispositions(dispositionValue)
        then:
        dispositionList.size() == 2

    }

    @Ignore
    void "test getSelectedDispositions when disposition is instance of list"() {
        setup:
        def dispositionInstances = [new Disposition(value: "Requires Review", displayName: "Requires Review", validatedConfirmed: false),
                                    new Disposition(value: "New Potential Signal", displayName: "New Potential Signal", validatedConfirmed: false)]
        mockDomain(Disposition, dispositionInstances)
        service.dispositionService = mockService(DispositionService)
        def dispositionValue = ["New Potential Signal", "Requires Review"]
        when:
        def dispositionList = service.getSelectedDispositions(dispositionValue)
        then:
        dispositionList.size() == 2

    }

    @Ignore
    void "test getSelectedDispositions when disposition is null"() {
        setup:
        def dispositionInstances = [new Disposition(value: "Requires Review", displayName: "Requires Review", validatedConfirmed: false),
                                    new Disposition(value: "New Potential Signal", displayName: "New Potential Signal", validatedConfirmed: false)]
        mockDomain(Disposition, dispositionInstances)
        service.dispositionService = mockService(DispositionService)
        def dispositionValue = null
        when:
        def dispositionList = service.getSelectedDispositions(dispositionValue)
        then:
        dispositionList.size() == 0

    }

    @Ignore
    void "test createProductSummaryMapForAggAlert"() {
        setup:
        def dataSource = "PVA"
        def name = "Test"
        when:
        def dispositionList = service.createProductSummaryMapForAggAlert(alert, dataSource, prevAlert, name, [alertComment])
        then:
        dispositionList["event"] == "Rash"
        dispositionList["productName"] == "Test Product A"
        dispositionList["source"] == dataSource
        dispositionList["sponCounts"] == "1/2/1"
        dispositionList["eb05"] == 1
        dispositionList["prevEb05"] == 2
        dispositionList["prevEb95"] == 3
        dispositionList["requestedBy"] == ''
        dispositionList["comment"] == 'Test'
        dispositionList["assessmentComment"] == ''
    }

    @Ignore
    void "test createProductSummaryMapForAggAlert when there is no previous alert"() {
        setup:
        def dataSource = "PVA"
        def name = "Test"
        when:
        def dispositionList = service.createProductSummaryMapForAggAlert(alert, dataSource, null, name, [alertComment])
        then:
        dispositionList["event"] == "Rash"
        dispositionList["productName"] == "Test Product A"
        dispositionList["source"] == dataSource
        dispositionList["sponCounts"] == "1/-/1"
        dispositionList["eb05"] == 1
        dispositionList["prevEb05"] == '-'
        dispositionList["prevEb95"] == '-'
        dispositionList["requestedBy"] == ''
        dispositionList["comment"] == 'Test'
        dispositionList["assessmentComment"] == ''
    }

    @Ignore
    void "test createProductSummaryMapForAggAlertSignal"() {
        setup:
        def dataSource = "PVA"
        when:
        def dispositionList = service.createProductSummaryMapForAggAlertSignal(alert, dataSource, prevAlert, validatedSignal)
        then:
        dispositionList["event"] == "Rash"
        dispositionList["productName"] == "Test Product A"
        dispositionList["name"] == validatedSignal.name
        dispositionList["source"] == dataSource
        dispositionList["sponCounts"] == "1/2/1"
        dispositionList["eb05"] == 1
        dispositionList["prevEb05"] == 2
        dispositionList["prevEb95"] == 3
        dispositionList["requestedBy"] == ''
        dispositionList["comment"] == 'Test'
        dispositionList["assessmentComment"] == 'Test notes'
    }

    @Ignore
    void "test createProductSummaryMapForAggAlertSignal when there is no previous alert"() {
        setup:
        def dataSource = "PVA"
        when:
        def dispositionList = service.createProductSummaryMapForAggAlertSignal(alert, dataSource, null, validatedSignal)
        then:
        dispositionList["event"] == "Rash"
        dispositionList["productName"] == "Test Product A"
        dispositionList["name"] == validatedSignal.name
        dispositionList["source"] == dataSource
        dispositionList["sponCounts"] == "1/-/1"
        dispositionList["eb05"] == 1
        dispositionList["prevEb05"] == '-'
        dispositionList["prevEb95"] == '-'
        dispositionList["requestedBy"] == ''
        dispositionList["comment"] == 'Test'
        dispositionList["assessmentComment"] == 'Test notes'
    }

    @Ignore
    void "test createProductSummaryMapForEvdasAlert"() {
        setup:
        def dataSource = "EVDAS"
        def name = "Test"
        when:
        def dispositionList = service.createProductSummaryMapForEvdasAlert(evdasAlert, dataSource, prevEvdasAlert, name, [alertComment])
        then:
        dispositionList["event"] == "Rash"
        dispositionList["productName"] == "Test Product A"
        dispositionList["source"] == dataSource
        dispositionList["sponCounts"] == "1/2/1"
        dispositionList["eb05"] == '-'
        dispositionList["prevEb05"] == '-'
        dispositionList["prevEb95"] == '-'
        dispositionList["requestedBy"] == ''
        dispositionList["comment"] == 'Test'
        dispositionList["assessmentComment"] == ''
    }

    @Ignore
    void "test createProductSummaryMapForEvdasAlert when there is no previous alert"() {
        setup:
        def dataSource = "EVDAS"
        def name = "Test"
        when:
        def dispositionList = service.createProductSummaryMapForEvdasAlert(evdasAlert, dataSource, null, name, [alertComment])
        then:
        dispositionList["event"] == "Rash"
        dispositionList["productName"] == "Test Product A"
        dispositionList["source"] == dataSource
        dispositionList["sponCounts"] == "1/-/1"
        dispositionList["eb05"] == '-'
        dispositionList["prevEb05"] == '-'
        dispositionList["prevEb95"] == '-'
        dispositionList["requestedBy"] == ''
        dispositionList["comment"] == 'Test'
        dispositionList["assessmentComment"] == ''
    }

    @Ignore
    void "test createProductSummaryMapForEvdasAlertSignal"() {
        setup:
        def dataSource = "EVDAS"
        when:
        def dispositionList = service.createProductSummaryMapForEvdasAlertSignal(evdasAlert, dataSource, prevEvdasAlert, validatedSignal)
        then:
        dispositionList["event"] == "Rash"
        dispositionList["productName"] == "Test Product A"
        dispositionList["name"] == validatedSignal.name
        dispositionList["source"] == dataSource
        dispositionList["sponCounts"] == "1/2/1"
        dispositionList["eb05"] == '-'
        dispositionList["prevEb05"] == '-'
        dispositionList["prevEb95"] == '-'
        dispositionList["requestedBy"] == ''
        dispositionList["comment"] == 'Test'
        dispositionList["assessmentComment"] == 'Test notes'
    }

    @Ignore
    void "test createProductSummaryMapForEvdasAlertSignal when there is no previous alert"() {
        setup:
        def dataSource = "EVDAS"
        when:
        def dispositionList = service.createProductSummaryMapForEvdasAlertSignal(evdasAlert, dataSource, null, validatedSignal)
        then:
        dispositionList["event"] == "Rash"
        dispositionList["productName"] == "Test Product A"
        dispositionList["name"] == validatedSignal.name
        dispositionList["source"] == dataSource
        dispositionList["sponCounts"] == "1/-/1"
        dispositionList["eb05"] == '-'
        dispositionList["prevEb05"] == '-'
        dispositionList["prevEb95"] == '-'
        dispositionList["requestedBy"] == ''
        dispositionList["comment"] == 'Test'
        dispositionList["assessmentComment"] == 'Test notes'
    }

    @Ignore
    void "test createProductSummaryList for AggregateCase Alert"() {
        setup:
        def dataSource = "pva"
        def alertList = [alert]
        def prevAlertList = [prevAlert]
        when:
        def productSummaryList = service.createProductSummaryList(alertList, prevAlertList, dataSource, alertComment)
        then:
        productSummaryList.size() == 1
    }

    @Ignore
    void "test createProductSummaryList for Evdas Alert"() {
        setup:
        def dataSource = "eudra"
        def alertList = [evdasAlert]
        def prevAlertList = [prevEvdasAlert]
        when:
        def productSummaryList = service.createProductSummaryList(alertList, prevAlertList, dataSource, alertComment)
        then:
        productSummaryList.size() == 1
    }

    @Ignore
    void "test getProductSummary"() {
        setup:
        def dispositionInstances = [new Disposition(value: "Requires Review", displayName: "Requires Review", validatedConfirmed: false),
                                    new Disposition(value: "New Potential Signal", displayName: "New Potential Signal", validatedConfirmed: false)]
        mockDomain(Disposition, dispositionInstances)
        service.dispositionService = mockService(DispositionService)
        service.aggregateCaseAlertService = mockService(AggregateCaseAlertService)
        Map params = [:]
        params.selectedDatasource = "pva"
        params.disposition = "Validated Signal"
        params.startDate = "01-Jul-2017"
        params.endDate = "31-Dec-2017"
        params.productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}'
        params.frequency = "HalfYearly"
        when:
        def productSummaryList = service.getProductSummary(params)
        then:
        productSummaryList.size() == 1
    }

    @Ignore
    void "test getProductSummarySearchMap"() {
        setup:
        validatedSignal.addToAggregateAlerts(alert)
        validatedSignal.addToComments(alertComment)
        validatedSignal.save(flush: true)
        def dispositionInstances = [new Disposition(value: "Requires Review", displayName: "Requires Review", validatedConfirmed: false),
                                    new Disposition(value: "New Potential Signal", displayName: "New Potential Signal", validatedConfirmed: false)]
        mockDomain(Disposition, dispositionInstances)
        service.dispositionService = mockService(DispositionService)
        service.aggregateCaseAlertService = mockService(AggregateCaseAlertService)
        Map params = [:]
        params.selectedDatasource = "pva"
        params.disposition = "Validated Signal"
        params.startDate = "01-Jul-2017"
        params.endDate = "31-Dec-2017"
        params.productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}'
        params.frequency = "HalfYearly"
        when:
        def prodSummarySearchMap = service.getProductSummarySearchMap(params)
        then:
        prodSummarySearchMap.keySet().size() == 9
    }

    @Ignore
    void "test getFrequencyMap"() {
        setup:
        def substanceFrequencyInstance = [new SubstanceFrequency(name: "Test Product A", uploadFrequency: "HalfYearly", miningFrequency: "HalfYearly",
                frequencyName: "HalfYearly", alertType: Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
                startDate: new DateTime(2017, 7, 1, 0, 0, 0, DateTimeZone.forID('UTC')).toDate(),
                endDate: new DateTime(2017, 12, 31, 0, 0, 0, DateTimeZone.forID('UTC')).toDate())]
        mockDomain(SubstanceFrequency, substanceFrequencyInstance)
        service.evdasAlertService = mockService(EvdasAlertService)
        service.aggregateCaseAlertService = mockService(AggregateCaseAlertService)
        def prodName = '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}'
        when:
        def frequencyMap = service.getFrequencyMap(prodName, "pva")
        then:
        frequencyMap.keySet().size() == 3
    }

    @Ignore
    void "test getFrequencyMap for Evdas Alert"() {
        setup:
        def substanceFrequencyInstance = [new SubstanceFrequency(name: "Test Product A", uploadFrequency: "HalfYearly", miningFrequency: "HalfYearly",
                frequencyName: "HalfYearly", alertType: Constants.AlertConfigType.EVDAS_ALERT,
                startDate: new DateTime(2017, 7, 1, 0, 0, 0, DateTimeZone.forID('UTC')).toDate(),
                endDate: new DateTime(2017, 12, 31, 0, 0, 0, DateTimeZone.forID('UTC')).toDate())]
        mockDomain(SubstanceFrequency, substanceFrequencyInstance)
        service.evdasAlertService = mockService(EvdasAlertService)
        service.aggregateCaseAlertService = mockService(AggregateCaseAlertService)
        def prodName = '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}'
        when:
        def frequencyMap = service.getFrequencyMap(prodName, "eudra")
        then:
        frequencyMap.keySet().size() == 3
    }

    @Ignore
    void "test saveRequestByForAlert for AggregateCase Alert"() {
        setup:
        service.aggregateCaseAlertService = mockService(AggregateCaseAlertService)
        service.aggregateCaseAlertService.CRUDService = mockService(CRUDService)
        def crudServiceMock = Mock(CRUDService)
        crudServiceMock.saveWithoutAuditLog(_) >> alert
        service.aggregateCaseAlertService.CRUDService = crudServiceMock
        Map params = [:]
        params.alertType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT
        params.productName = "Test Product A"
        params.eventName = "Rash"
        params.requestedBy = "Test Requested By"
        when:
        service.saveRequestByForAlert(params)
        then:
        alert.requestedBy == "Test Requested By"
    }

    @Ignore
    void "test saveRequestByForAlert for Evdas Alert"() {
        setup:
        service.aggregateCaseAlertService = mockService(AggregateCaseAlertService)
        service.aggregateCaseAlertService.CRUDService = mockService(CRUDService)
        def crudServiceMock = Mock(CRUDService)
        crudServiceMock.saveWithoutAuditLog(_) >> evdasAlert
        service.evdasAlertService.CRUDService = crudServiceMock
        Map params = [:]
        params.alertType = Constants.AlertConfigType.EVDAS_ALERT
        params.productName = "Test Product A"
        params.eventName = "Rash"
        params.requestedBy = "Test Requested By Evdas"
        when:
        service.saveRequestByForAlert(params)
        then:
        evdasAlert.requestedBy == "Test Requested By Evdas"
    }

}