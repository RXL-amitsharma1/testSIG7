package com.rxlogix

import com.rxlogix.*
import com.rxlogix.alertDocument.AlertDocumentService
import com.rxlogix.attachments.Attachment
import com.rxlogix.attachments.AttachmentLink
import com.rxlogix.attachments.AttachmentableService
import com.rxlogix.config.*
import com.rxlogix.dto.SignalChartsDTO
import com.rxlogix.enums.AttachmentType
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.enums.SignalAssessmentDateRangeEnum
import com.rxlogix.signal.*
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.user.UserGroupMapping
import grails.converters.JSON
import grails.testing.web.controllers.ControllerUnitTest
import groovy.transform.SourceURI
import com.rxlogix.exception.FileFormatException
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.springframework.web.multipart.MultipartFile
import spock.lang.Ignore
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import javax.xml.bind.ValidationException
import java.nio.file.Path
import java.nio.file.Paths

import static com.rxlogix.util.DateUtil.toDateStringPattern

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
class ValidatedSignalControllerSpec extends Specification implements ControllerUnitTest<ValidatedSignalController> {

    User user
    Group wfGroup
    Priority priority
    Map resultMap
    def userServiceSpy
    ExecutedConfiguration executedConfiguration
    Configuration configuration
    Disposition disposition
    Disposition defaultDisposition
    Disposition defaultSignalDisposition
    Disposition autoRouteDisposition
    ValidatedSignal validatedSignal
    AdHocAlert adHocAlert
    DataTableSearchRequest searchRequest
    SignalStatusHistory signalStatusHistory
    EmergingIssue emergingIssue
    SpecialPE specialPE
    SignalChart signalChart
    Justification justification
    ActionConfiguration actionConfiguration
    SignalRMMs signalRMMs
    SignalEmailLog signalEmailLog


    Attachment attachment
    File file
    //def userService
    def setup() {

        priority = new Priority([displayName    : "mockPriority",
                                 value          : "mockPriority",
                                 display        : true,
                                 defaultPriority: true,
                                 reviewPeriod   : 1])
        priority.save(failOnError: true)

        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "VO")
        disposition.save(flush: true, failOnError: true)

        defaultSignalDisposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "VO")
        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")

        [defaultDisposition, autoRouteDisposition].collect { it.save(failOnError: true) }

        wfGroup = new Group(name: "Default",
                groupType: GroupType.WORKFLOW_GROUP,
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                defaultSignalDisposition: disposition,
                autoRouteDisposition: autoRouteDisposition,
                modifiedBy: 'defaultModifiedBy',
                createdBy: 'defaultCreatedBy',
                justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(validate: false)

        //Save the  user
        user = new User(id: '1', username: 'test_user', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.addToGroups(wfGroup)
        user.preference.createdBy = "test"
        user.preference.modifiedBy = "test"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.save(flush: true, failOnError: true)
        UserGroupMapping userGroupMapping = new UserGroupMapping(user: user, group: wfGroup)
        userGroupMapping.save(flush: true, failOnError: true)
        signalStatusHistory = new SignalStatusHistory(dateCreated: new Date(),
                statusComment: "Test Status Comment", signalStatus: "Signal Status",
                dispositionUpdated: true, performedBy: "Test User", id: 1)
        signalStatusHistory.save()

        def now = new Date()
        def recurrenceJSON = /{"startDateTime":"$now","recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}/
        configuration = new Configuration(assignedTo: user, productSelection: "[TestProduct]", name: "test", scheduleDateJSON: recurrenceJSON,
                owner: user, createdBy: user.username, modifiedBy: user.username, priority: priority)
        configuration.save(flush: true, failOnError: true)

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

        def today = new Date()

        mockDomain(Disposition, [
                [validatedConfirmed: false, displayName: "Validated Signal1", value: 'ValidatedSignal1', abbreviation: 'VO1'],
                [validatedConfirmed: true, displayName: "Validated Signal2", value: 'ValidatedSignal2', abbreviation: 'VO2']

        ])

        assert (Disposition.count == 5)

        def configSCA = new Configuration(priority: priority,
                owner: user,
                productSelection: "Test Product A",
                assignedTo: user,
                type: 'Single Case Alert', isPublic: false,
                name: 'SAE - Clinical Reconciliation Death Case1111',
                description: 'Config to identify SAE - Clinical Reconciliation death cases',
                scheduleDateJSON: recurrenceJSON,
                isEnabled: false, createdBy: 'test', modifiedBy: 'test')
        configSCA.save(failOnError: true)

        def configACA = new Configuration(priority: priority,
                owner: user,
                productSelection: "Test Product A",
                assignedTo: user,
                type: 'Aggregate Case Alert', isPublic: false,
                name: 'SAE - Clinical Reconciliation Death Case',
                description: 'Config to identify SAE - Clinical Reconciliation death cases',
                drugType: '6267,6219,6283',
                scheduleDateJSON: recurrenceJSON,
                isEnabled: false, createdBy: 'test', modifiedBy: 'test',)
        configACA.save(failOnError: true)

        assert Configuration.count == 3

        def aca = new AggregateCaseAlert(
                alertConfiguration: configuration,
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
                version : 0,
                pecImpHigh: "1",
                pecImpLow: "1",
                createdBy: configuration.assignedTo.username,
                modifiedBy: configuration.assignedTo.username,
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
        aca.save(flush: true, failOnError: true)

        def attrMapObj = ['masterFollowupDate_5' : new Date(),
                          'masterRptTypeId_3'    : "test type",
                          'masterInitReptDate_4' : new Date(),
                          'masterFollowupDate_5' : new Date(),
                          'reportersHcpFlag_2'   : "true",
                          'masterProdTypeList_6' : "test",
                          'masterPrefTermAll_7'  : "test",
                          'assessOutcome'        : "Death",
                          'assessListedness_9'   : "test",
                          'assessAgentSuspect_10': "test"]

        def alert = new SingleCaseAlert(id: 1L,
                productId: 101,
                productSelection: "something",
                detectedDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                name: "Test Name",
                caseNumber: "1S01",
                caseVersion: 1,
                priority: configuration.priority,
                productFamily: "Test Product A",
                disposition: disposition,
                assignedTo: user,
                productName: "Test Product A",
                isNew: true,
                followUpExists: true,
                pt: "Rash",
                createdBy: configuration.assignedTo.username,
                modifiedBy: configuration.assignedTo.username,
                dateCreated: configuration.dateCreated,
                lastUpdated: configuration.dateCreated,
                reviewDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                attributesMap: attrMapObj,
                comboFlag: "comboFlag",
                malfunction: "malfunction"
        )
        alert.save(flush: true, failOnError: true)


        adHocAlert = new AdHocAlert([
                alertId            : 13,
                assignedTo         : user,
                detectedDate       : today,
                reviewDate         : today,
                detectedBy         : 'Company',
                topic              : "rash",
                initialDataSource  : "test",
                priority           : priority,
                alertVersion       : 0,
                disposition        : disposition,
                eventSelection     : 'Test Event',
                productSelection   : 'Test Product',
                name               : 'AdHoc Alert',
                aggReportStartDate : new Date(),
                aggReportEndDate   : new Date(),
                lastDecisionDate   : new Date(),
                haSignalStatus     : disposition,
                commentSignalStatus: 'test comments',
                haDateClosed       : new Date()
        ])
        adHocAlert.save(flush: true, failOnError: true)

        assert (AggregateCaseAlert.count == 1)
        assert (SingleCaseAlert.count == 1)
        assert (AdHocAlert.count == 1)

        def caseHistory = new CaseHistory([
                currentDisposition: disposition,
                currentAssignedTo : user,
                currentPriority   : priority,
                productFamily     : "",
                caseNumber        : "11S01",
                caseVersion       : 1,
                isLatest          : true,
                createdBy         : "admin",
                dateCreated       : today,
                lastUpdated       : new Date(),
                modifiedBy        : new Date(),
                productFamily     : "Test Product A",
                justification     : "Test"
        ])
        caseHistory.save(flush: true, failOnError: true)
        assert (CaseHistory.count == 1)

        attachment = new Attachment(
                name: "Reference Link",
                length: 0L,
                posterClass: User.class ?: "",
                posterId: user.id,
                inputName: "Reference Link",
                referenceLink: "https://www.google.com/",
                attachmentType: AttachmentType.Reference)

        signalEmailLog = new SignalEmailLog(assignedTo: user.toString(), subject: "Well-being", body: "About Employees")
        signalEmailLog.save(flush: true, failOnError: true)
        signalRMMs = new SignalRMMs(type: "LARMM", description: "First LARMM Record", assignedTo: user, assignedToGroup: wfGroup, status: "Approved", dueDate: new Date(), communicationType: "rmmType",
                signalEmailLog: signalEmailLog, dateCreated: new Date(), country: "India", emailAddress: "devops@rxlogix.com", emailSent: new Date(), colorDueDate: "Red")
        signalRMMs.save(flush: true, failOnError: true)

        validatedSignal = new ValidatedSignal(
                id: 1L,
                name: "test_name",
                products: "test_products",
                events: "cardiac arrest",
                detectedDate: new Date(),
                signalRMMs: signalRMMs,
                detectedBy: "Sahi",
                assignedTo: user,
                assignmentType: 'USER',
                modifiedBy: user.username,
                priority: priority,
                disposition: disposition,
                createdBy: user.username,
                description: "Description",
                initialDataSource: 'Data mining - FAERS database',
                workflowGroup: wfGroup
        )
        validatedSignal.save(flush: true, failOnError: true)
        emergingIssue = new EmergingIssue(eventName: "testEvent", createdBy: "Test", dateCreated: new Date(), modifiedBy: "Test", lastUpdated: new Date())
        emergingIssue.save(failOnError: true)
        specialPE = new SpecialPE(specialEvents: "TestEvent", specialProducts: "TestProduct", createdBy: "Test", dateCreated: new Date(), modifiedBy: "Test", lastUpdated: new Date())
        specialPE.save(failOnError: true)
        signalChart = new SignalChart(chartData: "Test", chartName: 'Test', execId: 1L, validatedSignal: validatedSignal)
        signalChart.save(failOnError: true)
        justification = new Justification(name: "Test", justification: "test", feature: "test", attributesMap: [:])
        justification.save(failOnError: true)
        actionConfiguration = new ActionConfiguration(displayName_local: "Test", description_local: "Test", description: "true", displayName: "Test", value: "Test")
        actionConfiguration.save(failOnError: true)
        assert (ValidatedSignal.count == 1)


        userServiceSpy = [
                getUser                 : { this.user },
                getUserLitConfigurations: { ["test"] },
                getCurrentUserPreference: { this.user.preference },
                getCurrentUserId        : { 1L }
        ] as UserService

        controller.userService = userServiceSpy

        searchRequest = new DataTableSearchRequest();
        searchRequest.searchParam = new DataTableSearchRequest.DataTableSearchParam();
        searchRequest.searchParam.start = 0;
        searchRequest.searchParam.length = 50;
        searchRequest.searchParam.draw = 1;
        searchRequest.searchParam.search = new DataTableSearchRequest.Search();
        searchRequest.searchParam.search.value = "";
        searchRequest.searchParam.search.regex = false;
        List<DataTableSearchRequest.Columns> column = new ArrayList<DataTableSearchRequest.Columns>()
        column.add(new DataTableSearchRequest.Columns())
        column.add(new DataTableSearchRequest.Columns())
        searchRequest.searchParam.columns = column;
        List<DataTableSearchRequest.Order> order = new ArrayList<>()
        order.add(new DataTableSearchRequest.Order())
        searchRequest.searchParam.order = order
        searchRequest.searchParam.order[0].setColumn(1);
        searchRequest.searchParam.order[0].setDir("desc");

        resultMap = [
                signalId         : validatedSignal.id,
                signalName       : validatedSignal.name,
                productName      : validatedSignal.products,
                eventName        : validatedSignal.events,
                noOfCases        : 1,
                noOfPec          : 2,
                priority         : priority.displayName,
                assignedTo       : user.fullName,
                actions          : 1,
                strategy         : validatedSignal.strategy ? validatedSignal.strategy.name : '-',
                topicCategory    : "topicCategory",
                monitoringStatus : disposition.displayName,
                initialDataSource: validatedSignal.initialDataSource,
                lastSubmitted    : '-',
                disposition      : disposition.displayName
        ]

        controller.activityService = [createActivityForSignal: { a, b, c, d, e, f, g, h -> }]

        @SourceURI
        URI sourceUri
        Path scriptLocation = Paths.get(sourceUri)
        String directory = scriptLocation.toString().replace("ValidatedSignalControllerSpec.groovy", "testingFiles/Details.html")
        file = new File(directory)

        UserService mockUserService = Mock(UserService)
        mockUserService.assignGroupOrAssignTo(_, _) >> {
            return signalRMMs
        }
        controller.userService = mockUserService

        def attachmentableService = Mock(AttachmentableService)
        controller.attachmentableService = attachmentableService
        attachmentableService.doAddReference(user, validatedSignal, "https://www.google.com/")

        validatedSignal.metaClass.getReferences = { [attachment] }
    }

    def cleanup() {
    }

    void "test list()"() {
        setup:
        controller.validatedSignalService.userService = userServiceSpy
        def validatedSignalService = Mock(ValidatedSignalService)
        validatedSignalService.createValidatedSignalDTO(_, _, _) >> [resultMap]
        validatedSignalService.getValidatedSignalList(_, _) >> [aaData: [resultMap], recordsTotal: 1, recordsFiltered: 1]
        controller.validatedSignalService = validatedSignalService
        when:
        params.callingScreen = 'dashboard'
        controller.list(searchRequest)
        then:
        response.json.aaData == [resultMap]
    }

    void "test fetchSignalStatus"() {
        setup:
        validatedSignal.addToAdhocAlerts(adHocAlert)
        validatedSignal.save(flush: true)
        def signalId = validatedSignal.id
        def date = toDateStringPattern(new Date(), 'dd-MMM-yyyy')
        controller.params.id = signalId

        when:
        controller.fetchSignalStatus()

        then:
        controller.response.json['aggReportEndDate'] == date
        controller.response.json['aggReportStartDate'] == date
        controller.response.json['haDateClosed'] == date
        controller.response.json['commentsSignalStatus'] == 'test comments'
        controller.response.json['haSignalStatus'] == 'Validated Signal'
        controller.response.json['lastDecisionDate'] == date
    }


    void "test fetchSignalStatus error scenario"() {
        when:
        controller.fetchSignalStatus()
        then:
        notThrown(Exception)
        controller.response.json['aggReportEndDate'] == ''
        controller.response.json['aggReportStartDate'] == ''
        controller.response.json['haDateClosed'] == ''
        controller.response.json['commentsSignalStatus'] == ''
        controller.response.json['haSignalStatus'] == ''
        controller.response.json['lastDecisionDate'] == ''
    }


    @ConfineMetaClassChanges([ValidatedSignalController])
    def "test for export meeting details"() {

        given:
        Boolean renderReportOutputTypeCalled = false
        File tempFile = new File("abc.pdf")
        def validatedSignalServiceMocked = Mock(ValidatedSignalService)
        validatedSignalServiceMocked.getMeetingDetails(_) >> [['name': 'test'], ['name': 'test1']]

        def dynamicReportServiceMocked = Mock(DynamicReportService)
        dynamicReportServiceMocked.createMeetingDetailReport(_, _) >> tempFile.createNewFile()

        controller.validatedSignalService = validatedSignalServiceMocked
        controller.dynamicReportService = dynamicReportServiceMocked
        controller.metaClass.renderReportOutputType = { File reportFile, String name ->
            renderReportOutputTypeCalled = true
        }

        when:
        controller.exportMeetingDetailReport()

        then:
        1 * controller.validatedSignalService.getMeetingDetails(_)
        1 * controller.dynamicReportService.createMeetingDetailReport(_, _)
        renderReportOutputTypeCalled
        tempFile.delete()
    }

    def "addReference"() {
        setup:
        controller.params.description = "Reference added"
        controller.params.referenceLink = "https://www.google.com/"

        def mockUserService = Mock(UserService)
        controller.userService = mockUserService
        mockUserService.getUser() >> user

        def attachmentableService = Mock(AttachmentableService)
        controller.attachmentableService = attachmentableService
        attachmentableService.doAddReference(user, validatedSignal, "https://www.google.com/")

        def activityService = Mock(ActivityService)
        controller.activityService = activityService
        activityService.createActivityForSignal(_, _, _, _, _, _, _, _) >> 1

        validatedSignal.metaClass.getReferences = { [attachment] }

        AttachmentDescription attachmentDescription = new AttachmentDescription()
        attachmentDescription.attachment = attachment
        attachmentDescription.createdBy = user.getFullName()
        attachmentDescription.description = controller.params.description
        attachmentDescription.save(flush: true, failOnError: true)

        when:
        controller.addReference(validatedSignal.id)

        then:
        notThrown(ValidationException)
        flash.message == "reference.add.success"
        attachmentDescription.description == "Reference added"
        response.status == 200
        response.text == "{\"success\":true}"
    }

    def "test checkNotificationStatus"(){
        setup:
        Map params = [statusComment: "Test Status Comment", signalStatus: "Signal Status", createdDate: new Date()]
        when:
        def result = controller.checkNotificationStatus(signalStatusHistory, params)
        then:
        result == false //since there are no difference between params and signalStatusHistory
    }

    def "test checkNotificationStatus with different comment"(){
        setup:
        Map params = [statusComment: "Test Status Comment2", signalStatus: "Signal Status", createdDate: new Date()]
        when:
        def result = controller.checkNotificationStatus(signalStatusHistory, params)
        then:
        result == true //since comment in params is different
    }


    def "test saveSignalStatusHistory"() {
        setup:
        // Mocking the ValidatedSignalService and ensuring it returns a list with expected values
        ValidatedSignalService validatedSignalServiceMock = Mock(ValidatedSignalService)
        validatedSignalServiceMock.saveSignalStatusHistory(_, _, _, _, _) >> 1

        controller.validatedSignalService = validatedSignalServiceMock
        controller.userService = Mock(UserService)
        controller.params.signalId = "1"
        controller.params.signalHistoryId = "1"
        controller.params.enableSignalWorkflow = "true"

        when:
        controller.saveSignalStatusHistory()
        then:
        notThrown(Exception)
        response.status == 200

        def jsonResponse = response.json
        jsonResponse != null
        jsonResponse.code == 200
    }


    def "test saveSignalStatusHistory for error scenario"() {
        setup:
        ValidatedSignalService validatedSignalServiceMock = Mock(ValidatedSignalService)
        controller.validatedSignalService = validatedSignalServiceMock

        when:
        controller.params.signalId = -1L
        controller.saveSignalStatusHistory()
        then:
        controller.response.json.status == false
    }


    def "test refreshSignalHistory"() {
        ValidatedSignalService validatedSignalServiceMock = Mock(ValidatedSignalService)
        controller.validatedSignalService = validatedSignalServiceMock
        controller.userService = Mock(UserService)
        when:
        controller.refreshSignalHistory(1)
        then:
        notThrown(Exception)
        response.status == 200
    }


    def "test refreshSignalHistory for errorScenario"() {
        when:
        controller.refreshSignalHistory(1)
        then:
        controller.response.json.status == false
    }


    def "generateSignalHistoryHtml"() {
        controller.validatedSignalService = Mock(ValidatedSignalService)
        controller.userService = Mock(UserService)
        AlertAttributesService mockAlertAttributeService = Mock(AlertAttributesService)
        mockAlertAttributeService.get(_) >> { return ['testA', 'testB'] }
        controller.alertAttributesService = mockAlertAttributeService

        when:
        String signalHistoryHtml = controller.generateSignalHistoryHtml(1L)

        then:
        signalHistoryHtml != null
    }

    def "test details"() {
        ValidatedSignalService validatedSignalService = Mock(ValidatedSignalService)
        validatedSignalService.pecAndCaseCounts(_) >> [:]
        validatedSignalService.heatMapData(_) >> [:]
        validatedSignalService.isProductAllowAsSafetyLead(_) >> true
        validatedSignalService.prepareCreateMap(_) >> [:]
        controller.validatedSignalService = validatedSignalService
        AlertDocumentService alertDocumentService = Mock(AlertDocumentService)
        alertDocumentService.getSignalUnlikedDocuments(_) >> []
        controller.alertDocumentService = alertDocumentService
        WorkflowRuleService workflowRuleService = Mock(WorkflowRuleService)
        workflowRuleService.fetchDispositionIncomingOutgoingMap() >> [:]
        controller.workflowRuleService = workflowRuleService
        PriorityService priorityService = Mock(PriorityService)
        priorityService.listPriorityOrder() >> []
        controller.priorityService = priorityService
        AlertAttributesService alertAttributesService = Mock(AlertAttributesService)
        alertAttributesService.get(_) >> ["TEST"]
        controller.alertAttributesService = alertAttributesService
        AlertService alertService = Mock(AlertService)
        alertService.getActionTypeAndActionMap() >> [:]
        controller.alertService = alertService
        controller.userService = Mock(UserService)
        DispositionService dispositionService = Mock(DispositionService)
        dispositionService.getDispositionListByDisplayName() >> []
        controller.dispositionService = dispositionService
        when:
        controller.details()
        then:
        response.status == 200
    }

    def "test isSignalAccessible"() {
        ValidatedSignalService validatedSignalService = Mock(ValidatedSignalService)
        validatedSignalService.checkAccessibility(_) >> true
        controller.validatedSignalService = validatedSignalService
        when:
        controller.isSignalAccessible(1)
        then:
        JSON.parse(response.text).success == true
    }

    def "test uploadSignalAssessmentReport"() {
        setup:
        controller.validatedSignalService = [generateSignalSummaryReport           : { a, b, c -> file },
                                             isFileSavedNameAlreadyAttachedToSignal: { a, b -> ["file1", "file2"] },
                                             generateNewFileName                   : { a, b -> "file.pdf" },
                                             getNextSequence                       : { -> "123" }]
        controller.attachmentableService = [uploadAssessmentReport: { a, b, c, d -> }]
        ValidatedSignal.metaClass.getAttachments = { [new Attachment(dateCreated: new Date())] }
        params.addReferenceName = "SignalDetails"
        params.description = "description"
        params.signalId = 1L
        when:
        controller.uploadSignalAssessmentReport()
        then:
        response.status == 200
    }

    def "test uploadSignalAssessmentReport when exception occurs"() {
        setup:
        controller.validatedSignalService = [generateSignalSummaryReport           : { a, b, c -> file },
                                             isFileSavedNameAlreadyAttachedToSignal: { a, b -> ["file1", "file2"] },
                                             generateNewFileName                   : { a, b -> "file.pdf" },
                                             getNextSequence                       : { -> "123" }]
        controller.attachmentableService = [uploadAssessmentReport: { a, b, c, d -> throw new FileFormatException() }]
        ValidatedSignal.metaClass.getAttachments = { [new Attachment(dateCreated: new Date())] }
        params.addReferenceName = "SignalDetails"
        params.description = "description"
        params.signalId = 1L
        when:
        controller.uploadSignalAssessmentReport()
        then:
        response.status == 400
    }

    def "test upload when exception occurs"() {
        setup:
        controller.validatedSignalService = [isFileSavedNameAlreadyAttachedToSignal: { a, b -> ["file1", "file2"] },
                                             generateNewFileName                   : { a, b -> "file.pdf" }]
        controller.attachmentableService = [attachUploadFileTo: { a, b, c, d, e, f, g -> throw new FileFormatException() },
                                            removeAttachment  : { a -> }]
        params.attachments = [filename: "file"]
        params.fileName = "file.pdf"
        params.inputName = "file"
        params.attachmentType = "Reference"
        params.description = "description"
        when:
        controller.upload()
        then:
        response.status == 400
    }

    def "test updateAttachment when exception occurs"() {
        setup:
        controller.validatedSignalService = [isFileSavedNameAlreadyAttachedToSignal: { a, b -> ["file1", "file2"] },
                                             generateNewFileName                   : { a, b -> "file.pdf" }]
        controller.attachmentableService = [attachUploadFileTo: { a, b, c, d, e, f, g -> throw new FileFormatException() },
                                            removeAttachment  : { a -> }]
        attachment.save(flush: true, validate: false)
        AttachmentLink link = new AttachmentLink()
        link.addToAttachments(attachment)
        link.save(flush: true, validate: false)
        AttachmentDescription attachmentDescription = new AttachmentDescription()
        attachmentDescription.attachment = attachment
        attachmentDescription.createdBy = user.getFullName()
        attachmentDescription.description = controller.params.description
        attachmentDescription.save(flush: true, failOnError: true)
        params.attachments = [filename: "file"]
        params.fileName = "file.pdf"
        params.inputName = "file"
        params.attachmentType = "Reference"
        params.description = "description"
        params.attachmentId = "1"
        println "lol"
        println params?.attachments
        println params?.attachments?.filename
        when:
        controller.updateAttachment()
        then:
        response.status == 400
    }

    def "test generateSpotfireReportForSignal"() {
        setup:
        controller.validatedSignalService = [generateSpotfireReportForSignal: { a -> }]
        when:
        controller.generateSpotfireReportForSignal()
        then:
        response.status == 200
    }

    def "test revertDisposition"() {
        setup:
        controller.validatedSignalService = [generateSpotfireReportForSignal: { a -> }]
        when:
        controller.revertDisposition(1, "test justification")
        then:
        response.status == 200
    }

    def "test fetchAnalysisData"() {
        setup:
        SpotfireNotificationQuery spotfireNotificationQuery = new SpotfireNotificationQuery(executedConfigurationId: 1L, isEnabled: true)
        spotfireNotificationQuery.save(flush: true, validate: false)
        when:
        controller.fetchAnalysisData()
        then:
        response.status == 200
    }

    def "test prepareSignalChartsDTO"() {
        setup:
        params.startDate = new Date()
        params.endDate = new Date() + 1
        params.productGroupSelection = '[{"name":"test (13)","id":"13"}]'
        params.eventGroupSelection = '[{"name":"test (13)","id":"13"}]'
        params.dateRange = SignalAssessmentDateRangeEnum.CUSTOM
        String selection = '{"1":[],"2":[],"3":[{"name":"Test Product AJ Capsule 100 millimole","id":"100004"}],"4":[]}'
        when:
        SignalChartsDTO signalChartsDTO = controller.prepareSignalChartsDTO(validatedSignal, selection, selection, params)
        then:
        response.status == 200
        signalChartsDTO.productGroupSelection == '[{"name":"test (13)","id":"13"}]'
        signalChartsDTO.eventGroupSelection == '[{"name":"test (13)","id":"13"}]'

    }

    def "test saveSignalRMMs in case of saving the record"() {
        when:
        params.signalId = 1
        params.rmmResp = "User"
        params.dueDate = new Date()
        params.inputName = "FileName"
        params.referenceLink = "http://www.google.com"
        controller.saveSignalRMMs()

        then:
        notThrown(Exception)
        response.status == 200
    }

    def "test saveSignalRMMs in case of updating the record"() {
        when:
        params.signalId = 1
        params.signalRmmId = 1
        params.rmmResp = "User"
        params.dueDate = new Date()
        params.inputName = "FileName"
        params.referenceLink = "http://www.google.com"
        controller.saveSignalRMMs()

        then:
        notThrown(Exception)
        response.status == 200
    }

    def "test saveSignalRMMs in case of error while saving the record"() {
        when:
        params.signalId = -1L
        params.signalRmmId = -1L
        controller.saveSignalRMMs()

        then:
        controller.response.json.status == false
    }

    def "test deleteSignalRMMs"() {
        when:
        params.signalId = 1
        params.signalRmmId = 1
        controller.deleteSignalRMMs()

        then:
        notThrown(Exception)
        response.status == 200
    }


    def "test deleteSignalRMMs in case of Exception"() {
        when:
        params.signalId = 1
        params.signalRmmId = -1
        controller.deleteSignalRMMs()

        then:
        controller.response.json.status == false
    }

    def "test bindReferences"() {
        when:
        controller.bindReferences(signalRMMs)

        then:
        noExceptionThrown()
    }

    def "test bindReferencesForSignalEmailLog"() {
        when:
        controller.bindReferencesForSignalEmailLog(signalEmailLog)

        then:
        noExceptionThrown()
    }

    def "test fetchRmms"() {
        when:
        params.signalId = validatedSignal.id
        controller.fetchRmms()

        then:
        JSON.parse(response.text).aaData == []
        JSON.parse(response.text).recordsTotal == 0
    }

    def "test fetchSignalNotInAlertObj"() {
        setup:
        ValidatedSignalService validatedSignalService = Mock(ValidatedSignalService)
        controller.validatedSignalService = validatedSignalService

        String searchTerm = "Test"
        int page = 1
        int pageSize = 20

        Map signals = [
                items: [
                        [
                                name: "Test Signal",
                                id: 1,
                                isClosed: false,
                                products: "(PARACETAMOL)",
                                detectedDate: "05-Aug-2024",
                                disposition: "Disposition",
                                forceJustification: false
                        ]
                ],
                total_count: 1
        ]

        validatedSignalService.fetchSignalsNotInAlertObj(searchTerm, page, pageSize) >> signals

        when:
        params.page = page
        params.searchTerm = searchTerm

        controller.fetchSignalNotInAlertObj()

        then:
        JSON.parse(response.text) == signals
    }
}