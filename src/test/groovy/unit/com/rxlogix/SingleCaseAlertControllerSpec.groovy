package unit.com.rxlogix

import com.rxlogix.*
import com.rxlogix.api.ReportResultRestController
import com.rxlogix.attachments.Attachment
import com.rxlogix.audit.AuditTrail
import com.rxlogix.audit.AuditTrailChild
import com.rxlogix.attachments.AttachmentableService
import com.rxlogix.cache.CacheService
import com.rxlogix.commandObjects.TokenAuthenticationCO
import com.rxlogix.config.*
import com.rxlogix.config.metadata.SourceColumnMaster
import com.rxlogix.config.metadata.SourceTableMaster
import com.rxlogix.controllers.AlertController
import com.rxlogix.dto.AlertDataDTO
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.enums.*
import com.rxlogix.mart.MartTags
import com.rxlogix.signal.*
import com.rxlogix.spotfire.SpotfireService
import com.rxlogix.user.Group
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import com.rxlogix.user.UserGroupMapping
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.testing.web.controllers.ControllerUnitTest
import groovy.json.JsonBuilder
import groovy.transform.SourceURI
import org.apache.http.HttpStatus
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.springframework.web.multipart.MultipartFile
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import javax.sql.DataSource
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.Connection
import java.text.SimpleDateFormat

@ConfineMetaClassChanges([SpringSecurityUtils])
class SingleCaseAlertControllerSpec extends Specification implements ControllerUnitTest<SingleCaseAlertController> {

    Disposition disposition
    Disposition defaultDisposition
    Disposition autoRouteDisposition
    Configuration alertConfiguration
    Configuration configuration_a
    Configuration configuration_b
    Configuration configuration_agg
    ExecutedConfiguration executedConfiguration
    ExecutedConfiguration executedConfiguration_a
    User user, newUser
    def attrMapObj
    SingleCaseAlert alert
    SingleCaseAlert sca_a
    CaseHistory caseHistoryObj
    Priority priority
    ValidatedSignal validatedSignal
    AlertDateRangeInformation alertDateRangeInformation1
    ViewInstance viewInstance1
    Justification justification1
    ActionType actionType
    ActionConfiguration actionConfiguration
    ActivityType activityType
    Group wfGroup
    AggregateCaseAlert aggAlert
    AlertTag alert_tag
    Priority priorityNew
    File file
    Activity activity1
    AdvancedFilter advanceFilter
    def templateNew
    SignalDataSourceService signalDataSourceService

    void setup() {
        @SourceURI
        URI sourceUri
        Path scriptLocation = Paths.get(sourceUri)
        String directory = scriptLocation.toString().replace("AdHocAlertControllerSpec.groovy", "testingFiles/Details.html")
        file = new File(directory)
        activityType = new ActivityType(value: ActivityTypeValue.CaseAdded)
        activityType.save(flush: true, failOnError: true)
        actionConfiguration = new ActionConfiguration(displayName: 'test', value: 'test', description: "desc")
        actionConfiguration.save(flush: true, failOnError: true)
        actionType = new ActionType(description: 'test', displayName: 'test', value: 'test')
        justification1 = new Justification(/*caseAddition:"on",*/ name: "justification1", justification: "changed", feature:
                '{"feature":"feature1"}')
        justification1.save(flush: true, failOnError: true)
        viewInstance1 = new ViewInstance(name: "viewInstance", alertType: "Single Case Alert", user: user, columnSeq: "seq")
        viewInstance1.save(flush: true, failOnError: true)
        alertDateRangeInformation1 = new AlertDateRangeInformation(dateRangeEndAbsolute: new Date() + 4, dateRangeStartAbsolute: new Date(),
                dateRangeEndAbsoluteDelta: 13, dateRangeStartAbsoluteDelta: 10, dateRangeEnum: DateRangeEnum.CUSTOM)
        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "vs",productGroupSelection: '[{"name":"testing_AS (3)","id":"3"}]')
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
        UserGroupMapping userGroupMapping = new UserGroupMapping(user: user, group: wfGroup)
        userGroupMapping.save(flush: true, failOnError: true)
        user.save(flush: true,failOnError:true)

        newUser = new User(id: '2', username: 'username2', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        newUser.addToGroups(wfGroup)
        newUser.preference.createdBy = "createdBy"
        newUser.preference.modifiedBy = "modifiedBy"
        newUser.preference.locale = new Locale("en")
        newUser.preference.isEmailEnabled = false
        newUser.metaClass.getFullName = { 'Fake Name' }
        newUser.metaClass.getEmail = { 'fake.email@fake.com' }
        newUser.metaClass.isAdmin = { -> false }
        newUser.save(flush: true)

        priority = new Priority([displayName: "mockPriority",value: "Low", display: true, displayName: "Low", reviewPeriod: 3, priorityOrder: 1])
        priority.save(flush: true, failOnError: true)

        alertConfiguration = new Configuration(
                name: "test",
                owner: user,
                modifiedBy: "signaldev",
                assignedTo: user,
                productSelection: "aspirin",
                executing: false,
                createdBy: "test",
                priority: priority,
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

        alert_tag = new AlertTag(name: 'alert_tag', createdBy: user, dateCreated: new Date())
        alert_tag.save(flush: true)

        ActivityType activityType1 = new ActivityType(value: ActivityTypeValue.JustificationChange)
        activityType1.save(flush: true, failOnError: true)
        activity1 = new Activity(details: "activityDetails", performedBy: user, timestamp: new Date(),
                justification: "change needed", assignedTo: user, type: activityType1)
        activity1.save(validate: false)

        priority = new Priority([displayName: "mockPriority", value: "mockPriority", display: true, defaultPriority: true, reviewPeriod: 1])
        priority.save(failOnError: true)

        priorityNew = new Priority([displayName: "mockPriorityNew", value: "mockPriorityNew", display: true, defaultPriority: true, reviewPeriod: 1])
        priorityNew.save(failOnError: true)

        def now = new Date()
        def recurrenceJSON = /{"startDateTime":"$now","recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}/

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
                scheduleDateJSON: recurrenceJSON,
                comboFlag:'combo',
                malfunction: 'mal')
        alert.save(flush: true, failOnError: false)

        aggAlert = new AggregateCaseAlert(
                alertConfiguration: config,
                executedAlertConfiguration: executedConfiguration,
                name: executedConfiguration.name,
                priority: priority,
                disposition: disposition,
                assignedTo: user,
                detectedDate: executedConfiguration.dateCreated,
                productName: "Test Product",
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
                periodStartDate: new Date(),
                periodEndDate: new Date(),
                adhocRun: false,
                positiveRechallenge: "true",
                positiveDechallenge: "false",
                listed: "false",
                pregenency: "false",
                related: "false",
                flagged: false,
                format: "test",
                eb05Str: "male:0.1, female:0.2, unknown:0.3",
                eb95Str: "male:0.1, female:0.2, unknown:0.3",
                ebgmStr: "male:0.1, female:0.2, unknown:0.3",
                alertTags: alert_tag
        )
        aggAlert.save(flush: true,failOnError: false)
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
        controller.params.alertDetails = [obj1, obj2].toString()


        def singleCaseAlertServiceSpy = [
                getAlertById               : { return this.alert },
                updateSingleCaseAlertStates: { _1, _2, _3, _4 -> },
                getExecConfigurationById   : { return this.executedConfiguration },
                getActivityByType          : { _ -> }
        ] as SingleCaseAlertService

        controller.singleCaseAlertService = singleCaseAlertServiceSpy

        def userServiceSpy = [
                getUser: { this.user }
        ] as UserService
        controller.userService = userServiceSpy

        caseHistoryObj = new CaseHistory(
                "currentDisposition": alert.disposition,
                "currentAssignedTo": alert.assignedTo,
                "currentPriority": alert.priority,
                "caseNumber": alert.caseNumber,
                "caseVersion": alert.caseVersion,
                "productFamily": alert.productFamily,
                "followUpNumber": alert.followUpNumber,
                "isLatest": true)

        def caseHistoryServiceSpy = [
                saveCaseHistory     : {},
                getLatestCaseHistory: { String caseNumber, String productFamily -> return caseHistoryObj }
        ] as CaseHistoryService
        controller.caseHistoryService = caseHistoryServiceSpy

        def activityServiceSpy = [
                createActivity: { _1, _2, _3, _4, _5, _6, _7, _8, _9, _10 -> }
        ]
        controller.activityService = activityServiceSpy


        validatedSignal = new ValidatedSignal(name: "ValidatedSignal", assignedTo: user, assignmentType: "signalAssignment",
                createdBy: "username", disposition: disposition, modifiedBy: "username", priority: priority, products: "product1", workflowGroup: wfGroup)
        validatedSignal.save(flush: true, failOnError: true)

        DateRangeInformation dateRangeInformation1 = new DateRangeInformation(dateRangeStartAbsoluteDelta: 1, dateRangeEndAbsoluteDelta: 1,
                dateRangeEndAbsolute: new Date() + 1, dateRangeStartAbsolute: new Date())
        configuration_a = new Configuration(
                id: 100001,
                name: 'case_series_config',
                assignedTo: user,
                productSelection: '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}',
                productGroupSelection: '{"name":"product group","id":1}',
                eventGroupSelection: '{"name":"event group","id":1}',
                owner: user,
                createdBy: user.username,
                modifiedBy: user.username,
                priority: priority,
                isCaseSeries: true,
                isEnabled:true,
                adhocRun:true,
                scheduleDateJSON: recurrenceJSON,
                alertCaseSeriesId: 1L,
                alertCaseSeriesName: "case series a"
        )
        configuration_a.save(flush: true)
        configuration_b = new Configuration(
                id: 100004,
                name: 'case_series_config_b',
                assignedTo: user,
                productSelection: "[TestProduct]",
                owner: user,
                createdBy: user.username,
                modifiedBy: user.username,
                priority: priority,
                isCaseSeries: true
        )
        configuration_b.save(flush: true)

        configuration_agg = new Configuration(
                id: 200001,
                name: 'agg_config',
                assignedTo: user,
                productSelection: "[TestProduct]",
                owner: user,
                createdBy: user.username,
                modifiedBy: user.username,
                priority: priority,
                isCaseSeries: true
        )
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
        advanceFilter.user = user
        advanceFilter.save(validate: false)
        ReportFieldGroup fieldGroup = new ReportFieldGroup([name: "Case Information"])
        fieldGroup.save(flush: true, failOnError: true)
        SourceTableMaster argusTableMaster = new SourceTableMaster([tableName: "CASE_MASTER", caseJoinOrder: 1, caseJoinType: "E", tableAlias: "cm", tableType: "C"])
        argusTableMaster.save(flush: true, failOnError: true)
        SourceColumnMaster argusColumnMaster = new SourceColumnMaster([tableName: argusTableMaster, reportItem: "CM_CASE_NUM", columnName: "CASE_NUM", columnType: "V"])
        argusColumnMaster.save(flush: true, failOnError: true)

        ReportField field = new ReportField([dataType: String.class, transform: "test", isEudraField: false, name: "caseNumber", description: "This is the Case number", sourceColumn: argusColumnMaster, fieldGroup: fieldGroup])
        field.save(flush: true, failOnError: true)
        ReportFieldInfo reportFieldInfo = new ReportFieldInfo(reportField: field, argusName: "fakeName")
        ReportFieldInfoList reportFieldInfoList = new ReportFieldInfoList()
        reportFieldInfoList.addToReportFieldInfoList(reportFieldInfo).save(flush: true, failOnError: true)

        CaseLineListingTemplate templateNew = new CaseLineListingTemplate(id: 1L, templateType: TemplateTypeEnum.CASE_LINE, owner: user, name: 'Test template', createdBy: "normalUser", modifiedBy: "normalUser", columnList: reportFieldInfoList)

        def JSONQuery = """{ "all": { "containerGroups": [   
        { "expressions": [  { "index": "0", "field": "masterCaseNum", "op": "EQUALS", "value": "14FR000215" }  ] }  ] } }"""
        TemplateQuery templateQuery = new TemplateQuery(query: 1, template: 1L, report: configuration_a, dateRangeInformationForTemplateQuery: new DateRangeInformation(), createdBy: user.username, modifiedBy: user.username)
        templateQuery.save(flush: true, failOnError: true)
        configuration_agg.addToTemplateQueries(templateQuery)
        configuration_agg.save(flush: true)

        ExecutedAlertDateRangeInformation executedAlertDateRangeInformation = new ExecutedAlertDateRangeInformation(dateRangeStartAbsolute: new Date(),
                dateRangeEndAbsolute: new Date() + 2)
        executedAlertDateRangeInformation.save(validate: false)
        executedConfiguration_a = new ExecutedConfiguration(
                id: 100002,
                name: "case_series_config",
                executedAlertDateRangeInformation: executedAlertDateRangeInformation,
                owner: user,
                scheduleDateJSON: "{}",
                nextRunDate: new Date(),
                description: "test",
                dateCreated: new Date(),
                lastUpdated: new Date(),
                isPublic: true,
                isDeleted: false,
                isEnabled: true,
                dateRangeType: DateRangeTypeCaseEnum.CASE_LOCKED_DATE,
                productSelection: "['TestProduct']",
                eventSelection: "['rash']", studySelection: "['test']",
                configSelectedTimeZone: "UTC",
                evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION,
                limitPrimaryPath: true,
                includeMedicallyConfirmedCases: true,
                excludeFollowUp: false,
                includeLockedVersion: true,
                adjustPerScheduleFrequency: true,
                createdBy: user.username,
                modifiedBy: user.username,
                assignedTo: user,
                executionStatus: ReportExecutionStatus.COMPLETED,
                numOfExecutions: 10,
                isCaseSeries: true,
                aggExecutionId: 200002,
                aggAlertId: 200003,
                configId: 2,
                aggCountType: "NEW_SER")
        executedConfiguration_a.save(flush: true)

        sca_a = new SingleCaseAlert(
                id: 100003,
                name: 'case_series_config',
                productSelection: "['TestProduct']",
                detectedDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                caseNumber: "1S00001",
                caseVersion: 1,
                alertConfiguration: configuration_a,
                executedAlertConfiguration: executedConfiguration_a,
                reviewDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                priority: configuration_a.priority,
                disposition: disposition,
                assignedTo: user,
                productName: "Test Product A",
                pt: "Rash",
                productId: 894239,
                isNew: true,
                followUpExists: false,
                createdBy: configuration_a.assignedTo.username,
                modifiedBy: configuration_a.assignedTo.username,
                dateCreated: configuration_a.dateCreated,
                lastUpdated: configuration_a.dateCreated,
                productFamily: "Test Product A",
                attributesMap: attrMapObj,
                isCaseSeries: true,
                comboFlag:'combo',
                malfunction: 'mal')
        sca_a.save(flush: true)
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
        EmailNotificationService emailNotificationService = Mock(EmailNotificationService)
        controller.emailNotificationService = emailNotificationService
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

    void "Test Change Assign User"() {
        setup:
        controller.params.newValue = "2"
        controller.emailService = ["sendNotificationEmail": {}]
        controller.params.executedConfigId = "4112"
        def singleCaseAlertServiceSpy = [
                getAlertById               : { return this.alert },
                updateSingleCaseAlertStates: { _1, _2, _3, _4 -> },
                getExecConfigurationById   : { return this.executedConfiguration },
                getActivityByType          : { _ -> }
        ] as SingleCaseAlertService
        controller.singleCaseAlertService = singleCaseAlertServiceSpy

        when:
        controller.changeAssignedToGroup("4112", "2")

        then:
        response.status == 200
    }

    void "test revertDisposition when disposition reverted successfully"() {
        setup:
        SingleCaseAlertService singleCaseAlertService = Mock(SingleCaseAlertService)
        singleCaseAlertService.revertDisposition(_,_)>>{
            return [:]
        }
        singleCaseAlertService.persistAlertDueDate(_)>>{
            return true
        }
        controller.singleCaseAlertService= singleCaseAlertService
        AlertService alertService = Mock(AlertService)
        alertService.fetchPreviousDispositionCount(_,_,_)>>{
            return 1L
        }
        controller.alertService = alertService

        when:
        controller.revertDisposition(1,"Reverted Justification")
        then:
        response.status == 200
    }

    void "test fetchRelatedCaseSeries"() {
        setup:
        def relatedCaseSeriesList = [[alertId: "5362", name: "testCalpol", description: "-", criteria: "<b>Date Range</b> = 01-Jan-1900 - 31-May-2018", productSelection: "Calpol,Rx Calcium Test Product Name of Rx E2B-R3 Product Family exceeding 70 characters limit for the CSV file",
                                      lastExecuted: "31-May-2018"], [alertId: "6151", name: "testBcCalpol", description: "-", criteria: "<b>Date Range</b> = 01-Jan-1900 - 06-Jun-2018", productSelection: "Calpol", lastExecuted: "06-Jun-2018"]]

        SingleCaseAlertService singleCaseAlertService = Mock(SingleCaseAlertService)
        singleCaseAlertService.getRelatedCaseSeries(_,_,_,_,_) >> { String caseNumber, int offset, int max, String searchString, Map orderColumnMap ->
            return relatedCaseSeriesList
        }
        singleCaseAlertService.getRelatedCaseSeriesTotalCount(_,_) >> {
            return 1
        }
        controller.singleCaseAlertService = singleCaseAlertService

        controller.params.caseNumber = "17JP00000000001411"
        params.start = 0
        params.length = 2
        params.searchString = "searchString"

        when:
        controller.fetchRelatedCaseSeries()
        then:
        response.getJson().recordsFiltered == 1
        response.getJson().recordsTotal == 0
        assert response.getJson().aaData.size() == 2

    }

    void "test fetchRelatedCaseSeries when caseNumber is empty"() {
        setup:
        def relatedCaseSeriesList = []
        controller.params.caseNumber = ""
        params.start = 0
        params.length = 2
        controller.singleCaseAlertService = [getRelatedCaseSeries: { caseNumber -> relatedCaseSeriesList }]
        when:
        controller.fetchRelatedCaseSeries()

        then:
        response.getJson() == [recordsFiltered:0, aaData:[], recordsTotal:0]

    }
    @Ignore
    void "test create"() {
        setup:
        UserService mockUserService = Mock(UserService)
        mockUserService.hasNormalAlertExecutionAccess(_) >> {
            return true
        }
        controller.userService = mockUserService

        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.getCioms1Id(_) >> {
            return 1L
        }
        controller.reportIntegrationService = mockReportIntegrationService
        when:
        controller.create()
        then:
        response.status == 200
        model.configurationInstance.type == 'Single Case Alert'
        model.priorityList[0]["value"] == "Low"
        model.userList[0]["username"] == "username"
        model.action == "create"
    }

    void "test saveCaseSeries"() {
        setup:
        Long executedConfigId = executedConfiguration_a.id
        SingleCaseAlertService mockSignalCaseAlertService = Mock(SingleCaseAlertService)
        mockSignalCaseAlertService.generateTempCaseSeries("new_config_series_name", executedConfigId) >> {
            return true
        }
        controller.singleCaseAlertService = mockSignalCaseAlertService
        ReportExecutorService reportExecutorService = Mock(ReportExecutorService)
        reportExecutorService.generateProductName(_)>>{
            return "testProductA"
        }
        controller.reportExecutorService = reportExecutorService
        AlertService alertService = Mock(AlertService)
        alertService.generateCountsMap(_,_)>>{
            return
        }
        controller.alertService = alertService
        when:
        controller.saveCaseSeries(executedConfigId, 'new_config_series_name')

        then:
        response.json.success == true
        executedConfiguration_a.name == 'new_config_series_name'
        !executedConfiguration_a.isCaseSeries
        configuration_a.name == 'new_config_series_name'
        !configuration_a.isCaseSeries
        sca_a.name == 'new_config_series_name'
        !sca_a.isCaseSeries
    }

    void "test saveCaseSeries error scenario"() {
        when:
        controller.saveCaseSeries(123456, 'new_name')

        then:
        notThrown(Exception)
        response.json.success == false
    }
    @Ignore
    void "test caseSeriesDetails"() {
        setup:
        DataObjectService dataObjectService = Mock(DataObjectService)
        dataObjectService.getAbbreviationMap(_) >> {
            return ["SOC_ABBREV":"ss"]
        }
        controller.dataObjectService = dataObjectService
        def exConfigId = executedConfiguration_a.id
        when:
        controller.caseSeriesDetails(200002, executedConfiguration_a.aggAlertId.toInteger(), 'NEW_SER', 1234, 'New', 'Ser', 'SingleCaseAlert')
        then:
        response.status == 302
        response.redirectUrl == '/singleCaseAlert/details?callingScreen=review&configId=0&isFaers=&isCaseSeries=true&isCaseSeriesGenerating=false&productName=Test+Product&eventName=Rash'
    }
    @Ignore
    void "test fetchAllFieldValues for faers true"() {
        when:
        controller.fetchAllFieldValues(true,true,true)

        then:
        response.getJson()[0]["name"] == "productName"

    }
    @Ignore
    void "test fetchAllFieldValues for faers false"() {
        when:
        controller.fetchAllFieldValues(false,true,true)

        then:
        response.getJson()[0]["name"] == "flags"

    }
    @Ignore
    @ConfineMetaClassChanges(AlertController)
    void "test for custom columns qualitative"() {
        setup:
        Boolean cumulative = false
        Long id = 43
        Boolean isFilterRequest = false
        AlertDataDTO alertDataDTO = new AlertDataDTO()
        alertDataDTO.dispositionFilters = ["filter1", "filter2"]
        def alertServiceObj = Mock(AlertService)
        def allowedProducts = ["Calpol01", "Test Product AJ", "ALL-LIC-PROD", "Wonder Product"]
        def resultMapData = [totalCount: 1, totalFilteredCount: 1, resultList: [[alertName       : "test case alert", caseType: "PERIODIC", completenessScore: 1.234, indNumber: "2402609084",
                                                                                 appTypeAndNumber: "DASD_434", compoundingFlag: "Yes", medErrorsPt: "some pts list", patientAge: 32, submitter: "TEVA"]], configId: id, fullCaseList: []]


        alertServiceObj.fetchAllowedProductsForConfiguration() >> allowedProducts
        alertServiceObj.getAlertFilterCountAndList(_) >> resultMapData
        controller.alertService = alertServiceObj
        UserService userService = Mock(UserService)
        SingleCaseAlertService singleCaseAlertService = Mock(SingleCaseAlertService)
        singleCaseAlertService.generateAlertDataDTO(_, _) >> alertDataDTO
        Preference preference = new Preference()
        preference.timeZone = "UTC"
        userService.getCurrentUserPreference() >> preference
        userService.getUser() >> new User(id: 20)
        UserService.getCurrentUserName() >> {
            return user.username
        }
        controller.userService = userService
        controller.singleCaseAlertService = singleCaseAlertService
        AlertController.metaClass.getDispositionSet = { confId, df, df1 ->
            return null
        }
//        ViewInstanceService viewInstanceService = Mock(ViewInstanceService)
//        viewInstanceService.fetchSelectedViewInstance(_,_) >>{
//            return viewInstance1
//        }
//        controller.viewInstanceService = viewInstanceService

        when:
        controller.listByExecutedConfig(id, isFilterRequest)

        then:
        response.status == 200
        response.json.aaData[0].alertName == "test case alert"
        response.json.aaData[0].caseType == "PERIODIC"
        response.json.aaData[0].completenessScore == 1.234
        response.json.aaData[0].indNumber == "2402609084"
        response.json.aaData[0].submitter == "TEVA"
        response.json.aaData[0].patientAge == 32
        response.json.aaData[0].appTypeAndNumber == "DASD_434"
    }

    void "test index"() {
        when:
        controller.index()
        then:
        response.status == 200
        view == "/singleCaseAlert/index"
    }
    @Ignore
    void "test view on success"() {
        when:
        controller.view(configuration_a)
        then:
        response.status == 200
        view == "/singleCaseAlert/view"
        model.configurationInstance == configuration_a
        model.currentUser == user
    }
    @Ignore
    void "test view when configuration is null"() {
        when:
        controller.view(null)
        then:
        response.status == 302
        response.redirectedUrl == "/singleCaseAlert/index"
        flash.message == 'default.not.found.message'
    }
    @Ignore
    void "test viewExecutedConfig on success"() {
        when:
        controller.viewExecutedConfig(executedConfiguration)
        then:
        response.status == 200
        view == "/singleCaseAlert/view"
        model.isExecuted == true
        model.configurationInstance == executedConfiguration
    }
    @Ignore
    void "test viewExecutedConfig when executedConfiguration is null"() {
        when:
        controller.viewExecutedConfig(null)
        then:
        response.status == 302
        response.redirectedUrl == "/singleCaseAlert/index"
        flash.message == 'default.not.found.message'
    }

    void "test copy on success"() {
        setup:
        UserService mockUserService = Mock(UserService)
        mockUserService.hasNormalAlertExecutionAccess(_) >> {
            return true
        }
        controller.userService = mockUserService

        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.getCioms1Id(_) >> {
            return 1L
        }
        controller.reportIntegrationService = mockReportIntegrationService
        when:
        controller.copy(configuration_a)
        then:
        response.status == 200
        view == "/singleCaseAlert/create"
        model.clone == true
    }
    @Ignore
    void "test copy when copy is null"() {
        when:
        controller.copy(null)
        then:
        response.status == 302
        response.redirectedUrl == "/singleCaseAlert/index"
        flash.message == 'default.not.found.message'
    }

    void "test delete on success"() {
        setup:
        ConfigurationService mockConfigurationService = Mock(ConfigurationService)
        mockConfigurationService.deleteConfig(_) >> {
            return configuration_a
        }
        controller.configurationService = mockConfigurationService
        when:
        controller.delete(configuration_a)
        then:
        response.redirectedUrl == "/configuration/index"
        flash.message == 'app.configuration.delete.success'
    }

    void "test delete on fail"() {
        setup:
        UserService userService = Mock(UserService)
        userService.getUser()>>{
            return null
        }
        controller.userService = userService
        when:
        controller.delete(configuration_a)
        then:
        response.redirectedUrl == "/configuration/index"
        flash.warn == 'app.configuration.delete.fail'
    }
    @Ignore
    void "test delete when delete is null"() {
        when:
        controller.delete(null)
        then:
        response.status == 302
        response.redirectedUrl == "/singleCaseAlert/index"
        flash.message == 'default.not.found.message'
    }
    @Ignore
    void "test edit when configurationInstance is null"(){
        when:
        controller.edit(null)
        then:
        response.status == 302
        response.redirectedUrl == "/singleCaseAlert/index"
        flash.message == 'default.not.found.message'
    }
    @Ignore
    void "test edit on success"() {
        setup:
        UserService mockUserService = Mock(UserService)
        mockUserService.hasNormalAlertExecutionAccess(_) >> {
            return true
        }
        mockUserService.getUser() >> {
            return user
        }
        controller.userService = mockUserService

        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.getCioms1Id(_) >> {
            return 1L
        }
        controller.reportIntegrationService = mockReportIntegrationService

        when:
        controller.edit(configuration_a)
        then:
        response.status == 200
        view == "/singleCaseAlert/edit"
        model.configurationInstance == configuration_a
    }
    @Ignore
    void "test edit when reportExecutorService returns true"() {
        setup:
        UserService mockUserService = Mock(UserService)
        mockUserService.hasNormalAlertExecutionAccess(_) >> {
            return true
        }
        mockUserService.getUser() >> {
            return user
        }
        controller.userService = mockUserService

        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.getCioms1Id(_) >> {
            return 1L
        }
        controller.reportIntegrationService = mockReportIntegrationService
        controller.reportExecutorService.currentlyRunning = [configuration_a.id]
        when:
        controller.edit(configuration_a)
        then:
        response.status == 302
        response.redirectedUrl == "/singleCaseAlert/index"
        flash.warn == "app.configuration.running.fail"
    }

    void "test edit when configuration is not editable by current user"() {
        setup:
        SpringSecurityUtils.metaClass.static.ifAllGranted = { String role ->
            return false
        }
        UserService userService = Mock(UserService)
        userService.getUser() >> {
            return newUser
        }
        controller.userService = userService
        when:
        controller.edit(configuration_a)
        then:
        response.status == 302
        response.redirectedUrl == "/configuration/index"
        flash.warn == "app.configuration.edit.permission"
    }
    @Ignore
    void "test save on success when content type is form"() {
        setup:
        UserService mockUserService = Mock(UserService)
        mockUserService.assignGroupOrAssignTo(_,_,_) >> {
            return configuration_a
        }
        mockUserService.hasNormalAlertExecutionAccess(_) >> {
            return true
        }
        mockUserService.getUser() >> {
            return newUser
        }
        controller.userService = mockUserService
        CRUDService mockCRUDService = Mock(CRUDService)
        mockCRUDService.saveWithAuditLog(_) >> {
            return configuration_a
        }
        controller.CRUDService = mockCRUDService

        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.getCioms1Id(_) >> {
            return 1L
        }
        controller.reportIntegrationService = mockReportIntegrationService

        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.repeatExecution = "true"
        params.onOrAfterDate = sdf.format(new Date())
        params.reviewPeriod = "1"
        params.name = "config1"
        params.owner = user
        params.priority = priority
        params.productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}'
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.save()
        then:
        response.status == 302
        flash.message == 'default.created.message'
        response.redirectedUrl == "/singleCaseAlert/view/2"
        Configuration.get(4).isEnabled == true
    }
    @Ignore
    void "test save on success when content type is all"() {
        setup:
        CRUDService mockCRUDService = Mock(CRUDService)
        mockCRUDService.saveWithAuditLog(_) >> {
            return configuration_a
        }
        controller.CRUDService = mockCRUDService
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.repeatExecution = "true"
        params.signalId = "${validatedSignal.id}"
        params.onOrAfterDate = sdf.format(new Date())
        params.reviewPeriod = "1"
        params.name = "config1"
        params.owner = user
        params.priority = priority
        params.productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}'
        request.contentType = ALL_CONTENT_TYPE
        when:
        controller.save()
        then:
        response.status == 201
        Configuration.get(4).isEnabled == true
    }
    @Ignore
    void "test save on success when repeatExecution is false"() {
        setup:
        CRUDService mockCRUDService = Mock(CRUDService)
        mockCRUDService.saveWithAuditLog(_) >> {
            return configuration_a
        }
        controller.CRUDService = mockCRUDService
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.repeatExecution = "false"
        params.signalId = "${validatedSignal.id}"
        params.onOrAfterDate = sdf.format(new Date())
        params.reviewPeriod = "1"
        params.name = "config1"
        params.owner = user
        params.priority = priority
        params.productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}'
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.save()
        then:
        response.status == 302
        flash.message == 'default.created.message'
        response.redirectedUrl == "/singleCaseAlert/view/2"
        Configuration.get(4).isEnabled == true
    }
    @Ignore
    void "test save on success when review period is not given"() {
        setup:
        UserService mockUserService = Mock(UserService)
        mockUserService.assignGroupOrAssignTo(_,_,_) >> {
            return configuration_a
        }
        mockUserService.hasNormalAlertExecutionAccess(_) >> {
            return true
        }
        mockUserService.getUser() >> {
            return newUser
        }
        controller.userService = mockUserService
        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.getCioms1Id(_) >> {
            return 1L
        }
        controller.reportIntegrationService = mockReportIntegrationService
        CRUDService mockCRUDService = Mock(CRUDService)
        mockCRUDService.saveWithAuditLog(_) >> {
            return configuration_a
        }
        controller.CRUDService = mockCRUDService
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.repeatExecution = "true"
        params.onOrAfterDate = sdf.format(new Date())
        params.name = "config1"
        params.owner = user
        params.priority = priority
        params.productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}'
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.save()
        then:
        response.status == 302
        flash.message == 'default.created.message'
        response.redirectedUrl == "/singleCaseAlert/view/2"
        Configuration.get(4).isEnabled == true
    }

    void "test save when validation exception occurs"() {
        setup:
        UserService mockUserService = Mock(UserService)
        mockUserService.hasNormalAlertExecutionAccess(_) >> {
            return true
        }
        mockUserService.assignGroupOrAssignTo(_,_,_) >> {
            return configuration_a
        }
        controller.userService = mockUserService

        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.getCioms1Id(_) >> {
            return 1L
        }
        controller.reportIntegrationService = mockReportIntegrationService

        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.repeatExecution = "true"
        params.onOrAfterDate = sdf.format(new Date())
        params.reviewPeriod = "1"
        params.name = "config1"
        params.owner = user
        params.productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}'
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.save()
        then:
        response.status == 200
        view == "/singleCaseAlert/create"
    }

    void "test save when Exception occurs"() {
        setup:
        controller.metaClass.modelData = { Configuration configurationInstance, String action->
            return [:]
        }
        UserService mockUserService = Mock(UserService)
        mockUserService.hasNormalAlertExecutionAccess(_) >> {
            return true
        }
        mockUserService.assignGroupOrAssignTo(_,_,_) >> {
            return configuration_a
        }
        controller.userService = mockUserService

        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.getCioms1Id(_) >> {
            return 1L
        }
        controller.reportIntegrationService = mockReportIntegrationService

        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.repeatExecution = true
        params.onOrAfterDate = sdf.format(new Date())
        params.reviewPeriod = "1"
        params.name = "config1"
        params.owner = user
        params.priority = priority
        params.productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}'
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.save()
        then:
        response.status == 200
        flash.error == "app.label.alert.error"
        view == "/singleCaseAlert/create"
    }

    void "test save when parsing of alertTriggerDays fails"() {
        setup:
        UserService mockUserService = Mock(UserService)
        mockUserService.hasNormalAlertExecutionAccess(_) >> {
            return true
        }
        controller.userService = mockUserService

        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.getCioms1Id(_) >> {
            return 1L
        }
        controller.reportIntegrationService = mockReportIntegrationService

        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.alertTriggerDays = 1
        params.alertTriggerCases = "1"
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.repeatExecution = "true"
        params.signalId = "${validatedSignal.id}"
        params.onOrAfterDate = sdf.format(new Date())
        params.reviewPeriod = "1"
        params.name = "config1"
        params.owner = user
        params.priority = priority
        params.productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}'
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.save()
        then:
        response.status == 200
        flash.error == "app.label.threshold.trigger.days.invalid"
        view == "/singleCaseAlert/create"
    }

    void "test save when parsing of alertTriggerCases fails"() {
        setup:
        UserService mockUserService = Mock(UserService)
        mockUserService.hasNormalAlertExecutionAccess(_) >> {
            return true
        }
        controller.userService = mockUserService

        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.getCioms1Id(_) >> {
            return 1L
        }
        controller.reportIntegrationService = mockReportIntegrationService

        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.alertTriggerDays = "1"
        params.alertTriggerCases = 1
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.repeatExecution = "true"
        params.signalId = "${validatedSignal.id}"
        params.onOrAfterDate = sdf.format(new Date())
        params.reviewPeriod = "1"
        params.name = "config1"
        params.owner = user
        params.priority = priority
        params.productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}'
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.save()
        then:
        response.status == 200
        flash.error == "app.label.threshold.trigger.cases.invalid"
        view == "/singleCaseAlert/create"
    }

    @Ignore
    void "test run when content type is form"() {
        setup:
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.updateWithAuditLog(_)>>{
            return configuration_a
        }
        controller.CRUDService=mockCRUDService

        controller.alertService = [renameExecConfig: { Long configId, String configNameOld, String configNameNew, Long ownerId, String alertType ->
            configuration_a.name = "newName"
            configuration_a.save(flush: true, failOnError: true)
        }]
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.id = configuration_a.id
        params.previousAction = 'create'
        params.name = "newName"
        params.excludeNonValidCases = true
        params.repeatExecution = "true"
        params.productGroupSelection='{["name":"product group","id":1]}'
        params.eventGroupSelection='{["name":"event group","id":1]}'
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.reviewPeriod = "1"
        params.onOrAfterDate = sdf.format(new Date())
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        configuration_a.adhocRun = true
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.run()
        then:
        response.status == 302
        response.redirectedUrl == "/configuration/executionStatus?alertType=SINGLE_CASE_ALERT"
        flash.message == 'app.Configuration.RunningMessage'
    }
    @Ignore
    void "test run when content type is not form"() {
        setup:
        def mockCRUDService=Mock(CRUDService)
        mockCRUDService.updateWithAuditLog(_)>>{
            return configuration_a
        }
        controller.CRUDService=mockCRUDService
        controller.alertService = [renameExecConfig: { Long configId, String configNameOld, String configNameNew, Long ownerId, String alertType ->
            configuration_a.name = "newName"
            configuration_a.save(flush: true, failOnError: true)
        }]
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.id = configuration_a.id
        params.previousAction = 'create'
        params.name = "newName"
        params.excludeNonValidCases = true
        params.repeatExecution = "true"
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.reviewPeriod = "1"
        params.onOrAfterDate = sdf.format(new Date())
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        configuration_a.adhocRun = true
        request.contentType = ALL_CONTENT_TYPE
        when:
        controller.run()
        then:
        response.status == 201
    }

    void "test run when params.id is not given"() {
        setup:
        UserService mockUserService = Mock(UserService)
        mockUserService.assignGroupOrAssignTo(_,_,_) >> {
            return configuration_a
        }
        mockUserService.hasNormalAlertExecutionAccess(_) >> {
            return true
        }
        mockUserService.getUser() >> {
            return newUser
        }
        controller.userService = mockUserService
        CRUDService mockCRUDService = Mock(CRUDService)
        mockCRUDService.saveWithAuditLog(_) >> {
            return configuration_a
        }
        controller.CRUDService = mockCRUDService
        controller.alertService = [renameExecConfig: { Long configId, String configNameOld, String configNameNew, Long ownerId, String alertType ->
            configuration_a.name = "newName"
            configuration_a.save(flush: true, failOnError: true)
        }]
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.previousAction = 'create'
        params.name = "newName"
        params.excludeNonValidCases = true
        params.repeatExecution = "true"
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.reviewPeriod = "1"
        params.onOrAfterDate = sdf.format(new Date())
        params."templateQueries[0].template" = 1
        params.owner = user
        params.priority = priority
        params.productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}'
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        configuration_a.adhocRun = true
        request.contentType = ALL_CONTENT_TYPE
        when:
        controller.run()
        then:
        response.status == 201
    }

    void "test run when parsing of alertTriggerDays fails"() {
        setup:
        UserService mockUserService = Mock(UserService)
        mockUserService.hasNormalAlertExecutionAccess(_) >> {
            return true
        }
        controller.userService = mockUserService

        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.getCioms1Id(_) >> {
            return 1L
        }
        controller.reportIntegrationService = mockReportIntegrationService
        controller.alertService = [renameExecConfig: { Long configId, String configNameOld, String configNameNew, Long ownerId, String alertType ->
            configuration_a.name = "newName"
            configuration_a.save(flush: true, failOnError: true)
        }]
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.previousAction = 'create'
        params.name = "newName"
        params.excludeNonValidCases = true
        params.repeatExecution = "true"
        params.alertTriggerDays = 1
        params.alertTriggerCases = "1"
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.reviewPeriod = "1"
        params.onOrAfterDate = sdf.format(new Date())
        params."templateQueries[0].template" = 1
        params.owner = user
        params.priority = priority
        params.productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}'
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        configuration_a.adhocRun = true
        request.contentType = ALL_CONTENT_TYPE
        when:
        controller.run()
        then:
        response.status == 200
        flash.error == "app.label.threshold.trigger.days.invalid"
        view == "/singleCaseAlert/create"
    }

    void "test run when parsing of alertTriggerCases fails"() {
        setup:
        UserService mockUserService = Mock(UserService)
        mockUserService.hasNormalAlertExecutionAccess(_) >> {
            return true
        }
        controller.userService = mockUserService

        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.getCioms1Id(_) >> {
            return 1L
        }
        controller.reportIntegrationService = mockReportIntegrationService
        controller.alertService = [renameExecConfig: { Long configId, String configNameOld, String configNameNew, Long ownerId, String alertType ->
            configuration_a.name = "newName"
            configuration_a.save(flush: true, failOnError: true)
        }]
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.previousAction = 'create'
        params.name = "newName"
        params.excludeNonValidCases = true
        params.repeatExecution = "true"
        params.alertTriggerDays = "1"
        params.alertTriggerCases = 1
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.reviewPeriod = "1"
        params.onOrAfterDate = sdf.format(new Date())
        params."templateQueries[0].template" = 1
        params.owner = user
        params.priority = priority
        params.productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}'
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        configuration_a.adhocRun = true
        request.contentType = ALL_CONTENT_TYPE
        when:
        controller.run()
        then:
        response.status == 200
        flash.error == "app.label.threshold.trigger.cases.invalid"
        view == "/singleCaseAlert/create"
    }

    void "test run when params.name is not given"() {
        setup:
        controller.alertService = [renameExecConfig: { Long configId, String configNameOld, String configNameNew, Long ownerId, String alertType ->
            configuration_a.name = "newName"
            configuration_a.save(flush: true, failOnError: true)
        }]
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.previousAction = 'create'
        params.excludeNonValidCases = true
        params.repeatExecution = "true"
        params.alertTriggerDays = "1"
        params.alertTriggerCases = 1
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.reviewPeriod = "1"
        params.onOrAfterDate = sdf.format(new Date())
        params."templateQueries[0].template" = 1
        params.owner = user
        params.priority = priority
        params.productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product A","id":"100004"}],"4":[],"5":[]}'
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        configuration_a.adhocRun = true
        request.contentType = ALL_CONTENT_TYPE
        when:
        controller.run()
        then:
        response.status == 200
        flash.error == "app.label.alert.error"
        view == "/singleCaseAlert/create"
    }

    void "test run when validation exception occurs"() {
        setup:
        controller.alertService = [renameExecConfig: { Long configId, String configNameOld,
                                                       String configNameNew, Long ownerId, String alertType ->
            configuration_a.name = "newName"
            configuration_a.save(flush: true, failOnError: true)
        }]
        UserService mockUserService = Mock(UserService)
        mockUserService.hasNormalAlertExecutionAccess(_) >> {
            return true
        }
        controller.userService = mockUserService

        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.getCioms1Id(_) >> {
            return 1L
        }
        controller.reportIntegrationService = mockReportIntegrationService
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.id = configuration_a.id
        params.previousAction = 'create'
        params.name = "newName"
        params.excludeNonValidCases = true
        params.repeatExecution = "true"
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.reviewPeriod = "1"
        params.onOrAfterDate = sdf.format(new Date())
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        configuration_a.adhocRun = true
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.run()
        then:
        response.status == 200
        view == "/singleCaseAlert/edit"
        flash.error == 'app.label.alert.error'
    }

    void "test run when exception occurs"() {
        setup:
        controller.userService = [assignGroupOrAssignTo: { String assignedTo, def domain -> throw new Exception() }]
        controller.alertService = [renameExecConfig: { Long configId, String configNameOld, String configNameNew,
                                                       Long ownerId, String alertType ->
            configuration_a.name = "newName"
            configuration_a.save(flush: true, failOnError: true)
        }]
        UserService mockUserService = Mock(UserService)
        mockUserService.hasNormalAlertExecutionAccess(_) >> {
            return true
        }
        controller.userService = mockUserService

        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.getCioms1Id(_) >> {
            return 1L
        }
        controller.reportIntegrationService = mockReportIntegrationService
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.id = configuration_a.id
        params.previousAction = 'create'
        params.name = "newName"
        params.excludeNonValidCases = true
        params.repeatExecution = "true"
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params.assignedToValue = "User_${user.id}"
        params.sharedWith = "User_${user.id}"
        params.reviewPeriod = "1"
        params.onOrAfterDate = sdf.format(new Date())
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        configuration_a.adhocRun = true
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.run()
        then:
        response.status == 200
        view == "/singleCaseAlert/edit"
        flash.error == "app.label.alert.error"
    }
    @Ignore
    void "test runOnce when content type is form"() {
        setup:
        CRUDService mockCRUDService = Mock(CRUDService)
        mockCRUDService.updateWithAuditLog(_) >> {
            return configuration_a
        }
        controller.CRUDService = mockCRUDService
        UserService mockUserService = Mock(UserService)
        mockUserService.hasNormalAlertExecutionAccess(_) >> {
            return true
        }
        controller.userService = mockUserService

        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.getCioms1Id(_) >> {
            return 1L
        }
        controller.reportIntegrationService = mockReportIntegrationService
        params.id = configuration_a.id
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.runOnce()
        then:
        response.status == 302
        flash.message == 'app.Configuration.RunningMessage'
        response.redirectedUrl == "/configuration/executionStatus?alertType=SINGLE_CASE_ALERT"
    }

    void "test runOnce when content type is not form"() {
        setup:
        CRUDService mockCRUDService = Mock(CRUDService)
        mockCRUDService.updateWithAuditLog(_) >> {
            return configuration_a
        }
        controller.CRUDService = mockCRUDService
        UserService mockUserService = Mock(UserService)
        mockUserService.hasNormalAlertExecutionAccess(_) >> {
            return true
        }
        controller.userService = mockUserService

        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.getCioms1Id(_) >> {
            return 1L
        }
        controller.reportIntegrationService = mockReportIntegrationService
        params.id = configuration_a.id
        request.contentType = ALL_CONTENT_TYPE
        when:
        controller.runOnce()
        then:
        response.status == 201
    }
    @Ignore
    void "test runOnce when validation exception occurs"() {
        setup:
        UserService mockUserService = Mock(UserService)
        mockUserService.hasNormalAlertExecutionAccess(_) >> {
            return true
        }
        controller.userService = mockUserService

        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.getCioms1Id(_) >> {
            return 1L
        }
        controller.reportIntegrationService = mockReportIntegrationService
        configuration_a.priority = null
        params.id = configuration_a.id
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.runOnce()
        then:
        response.status == 200
        model != null
        view == "/singleCaseAlert/edit"
    }
    @Ignore
    void "test runOnce when exception occurs"() {
        setup:
        UserService mockUserService = Mock(UserService)
        mockUserService.hasNormalAlertExecutionAccess(_) >> {
            return true
        }
        controller.userService = mockUserService

        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.getCioms1Id(_) >> {
            return 1L
        }
        controller.reportIntegrationService = mockReportIntegrationService
        controller.configurationService = [getNextDate: { Configuration c -> throw new Exception() }]
        configuration_a.priority = null
        params.id = configuration_a.id
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.runOnce()
        then:
        response.status == 200
        flash.error == "app.label.alert.error"
        view == "/singleCaseAlert/edit"
    }
    @Ignore
    void "test update when content type is form"() {
        setup:
        UserService mockUserService = Mock(UserService)
        mockUserService.hasNormalAlertExecutionAccess(_) >> {
            return true
        }
        controller.userService = mockUserService

        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.getCioms1Id(_) >> {
            return 1L
        }
        controller.reportIntegrationService = mockReportIntegrationService
        SingleCaseAlertController.metaClass.static.populateModel = {Configuration configurationInstance ->
            return new Date()
        }
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.id = configuration_a.id
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params.productGroupSelection='{["name":"product group","id":1]}'
        params.eventGroupSelection='{["name":"event group","id":1]}'
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        params.assignedToValue = "User_${user.id}"
        params.repeatExecution = "true"
        params.onOrAfterDate = sdf.format(new Date())
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.update()
        then:
        response.status == 302
        response.redirectedUrl == "/singleCaseAlert/view/1"
        flash.message == 'default.updated.message'
    }

    void "test update when content type is not form"() {
        setup:
        UserService mockUserService = Mock(UserService)
        mockUserService.hasNormalAlertExecutionAccess(_) >> {
            return true
        }
        controller.userService = mockUserService

        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.getCioms1Id(_) >> {
            return 1L
        }
        controller.reportIntegrationService = mockReportIntegrationService
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.id = configuration_a.id
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        params.assignedToValue = "User_${user.id}"
        params.repeatExecution = "true"
        params.onOrAfterDate = sdf.format(new Date())
        request.contentType = ALL_CONTENT_TYPE
        when:
        controller.update()
        then:
        response.status == 200
    }

    void "test update when Exception occurs"() {
        setup:
        UserService mockUserService = Mock(UserService)
        mockUserService.hasNormalAlertExecutionAccess(_) >> {
            return true
        }
        controller.userService = mockUserService

        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.getCioms1Id(_) >> {
            return 1L
        }
        controller.reportIntegrationService = mockReportIntegrationService
        params.id = configuration_a.id
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        params.assignedToValue = "User_${user.id}"
        params.repeatExecution = "true"
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.update()
        then:
        response.status == 200
        flash.error == "app.label.alert.error"
        view == "/singleCaseAlert/edit"
    }
    @Ignore
    void "test update when validation error occurs"() {
        setup:
        UserService mockUserService = Mock(UserService)
        mockUserService.hasNormalAlertExecutionAccess(_) >> {
            return true
        }
        controller.userService = mockUserService

        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.getCioms1Id(_) >> {
            return 1L
        }
        controller.reportIntegrationService = mockReportIntegrationService
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.id = configuration_a.id
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params.assignedToValue = "User_${user.id}"
        params.repeatExecution = "true"
        params.onOrAfterDate = sdf.format(new Date())
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.update()
        then:
        response.status == 200
        view == "/singleCaseAlert/edit"
        flash.error == null
    }
    @Ignore
    void "test update when repeatExecution is false"() {
        setup:
        UserService mockUserService = Mock(UserService)
        mockUserService.hasNormalAlertExecutionAccess(_) >> {
            return true
        }
        controller.userService = mockUserService

        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.getCioms1Id(_) >> {
            return 1L
        }
        controller.reportIntegrationService = mockReportIntegrationService
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.id = configuration_a.id
        params.alertTriggerDays = "1"
        params.alertTriggerCases = "1"
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        params.assignedToValue = "User_${user.id}"
        params.repeatExecution = "false"
        params.onOrAfterDate = sdf.format(new Date())
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.update()
        then:
        response.status == 302
        response.redirectedUrl == "/singleCaseAlert/view/1"
        flash.message == 'default.updated.message'
    }

    void "test update when parsing of alertTriggerDays fails"() {
        setup:
        UserService mockUserService = Mock(UserService)
        mockUserService.hasNormalAlertExecutionAccess(_) >> {
            return true
        }
        controller.userService = mockUserService

        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.getCioms1Id(_) >> {
            return 1L
        }
        controller.reportIntegrationService = mockReportIntegrationService
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.id = configuration_a.id
        params.alertTriggerDays = 1
        params.alertTriggerCases = "1"
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        params.assignedToValue = "User_${user.id}"
        params.repeatExecution = "true"
        params.onOrAfterDate = sdf.format(new Date())
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.update()
        then:
        response.status == 200
        view == "/singleCaseAlert/edit"
        flash.error == "app.label.threshold.trigger.days.invalid"
    }

    void "test update when parsing of alertTriggerCases fails"() {
        setup:
        UserService mockUserService = Mock(UserService)
        mockUserService.hasNormalAlertExecutionAccess(_) >> {
            return true
        }
        controller.userService = mockUserService

        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.getCioms1Id(_) >> {
            return 1L
        }
        controller.reportIntegrationService = mockReportIntegrationService
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.id = configuration_a.id
        params.alertTriggerDays = "1"
        params.alertTriggerCases = 1
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        params.assignedToValue = "User_${user.id}"
        params.repeatExecution = "true"
        params.onOrAfterDate = sdf.format(new Date())
        request.contentType = FORM_CONTENT_TYPE
        when:
        controller.update()
        then:
        response.status == 200
        view == "/singleCaseAlert/edit"
        flash.error == "app.label.threshold.trigger.cases.invalid"
    }

    void "test viewConfig when params.id and from both given"() {
        given:
        params.id = executedConfiguration.id
        params.from = "result"
        when:
        controller.viewConfig()
        then:
        response.status == 302
        response.redirectedUrl == "/configuration/viewExecutedConfig/${executedConfiguration.id}"
    }

    void "test viewConfig when only params.id given"() {
        given:
        params.id = executedConfiguration.id
        when:
        controller.viewConfig()
        then:
        response.status == 302
        response.redirectedUrl == "/configuration/view/${executedConfiguration.id}"
    }

    void "test viewConfig when neither params.id nor from given"() {
        when:
        controller.viewConfig()
        then:
        response.status == 302
        flash.error == "app.configuration.id.null"
        response.redirectedUrl == "/configuration/listAllResults"
    }

    void "test populateModel"() {
        given:
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        params.owner = user
        params.priority = priority
        params.productSelection = '{"3":[{"name":"Test Product A","id":"100004"}]}'
        params.productGroupSelection='{["name":"product group","id":1]}'
        params.eventGroupSelection='{["name":"event group","id":1]}'
        params.spotfireDaterange = 'lastXWeeks'
        params.enableSpotfire = true
        params.spotfireType = "DRUG"
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.onOrAfterDate = sdf.format(new Date())
        params.alertDateRangeInformation = alertDateRangeInformation1
        when:
        controller.populateModel(configuration_a)
        then:
        response.status == 200
        configuration_a.productSelection == '{"3":[{"name":"Test Product A","id":"100004"}]}'
        configuration_a.productGroupSelection == '{["name":"product group","id":1]}'
        configuration_a.eventGroupSelection == '{["name":"event group","id":1]}'
    }
    @Ignore
    void "test enable when content type is form"() {
        given:
        CRUDService mockCRUDService = Mock(CRUDService)
        mockCRUDService.updateWithAuditLog(_) >> {
            return configuration_a
        }
        controller.CRUDService = mockCRUDService
        params.id = configuration_a.id
        request.contentType = FORM_CONTENT_TYPE
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.onOrAfterDate = sdf.format(new Date())
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        when:
        controller.enable()
        then:
        response.status == 302
        response.redirectedUrl == "/singleCaseAlert/view/${configuration_a.id}"
        flash.message == "default.enabled.message"
    }

    void "test enable when content type is not form"() {
        given:
        CRUDService mockCRUDService = Mock(CRUDService)
        mockCRUDService.updateWithAuditLog(_) >> {
            return configuration_a
        }
        controller.CRUDService = mockCRUDService
        params.id = configuration_a.id
        request.contentType = ALL_CONTENT_TYPE
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.onOrAfterDate = sdf.format(new Date())
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        when:
        controller.enable()
        then:
        response.status == 200
        model != null
        view == null
    }
    @Ignore
    void "test enable when validation exception occurs"() {
        given:
        params.id = configuration_a.id
        request.contentType = ALL_CONTENT_TYPE
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.onOrAfterDate = sdf.format(new Date())
        ConfigurationService configurationService = Mock(ConfigurationService)
        configurationService.detectChangesMade(_,_) >> {
            return []
        }
        controller.configurationService = configurationService
        CRUDService mockCRUDService = Mock(CRUDService)
//        mockCRUDService.updateWithAuditLog(_) >> {
//            return configuration_a
//        }
        mockCRUDService.hasDetectChangesForAuditLog(_) >> {
            return
        }
        controller.CRUDService = mockCRUDService
        when:
        controller.enable()
        then:
        response.status == 200
        view == "/singleCaseAlert/edit"
        model != null
    }

    void "test enable when configuration not found"() {
        given:
        params.id = 1000L
        request.contentType = ALL_CONTENT_TYPE
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.onOrAfterDate = sdf.format(new Date())
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        when:
        controller.enable()
        then:
        response.status == 404
        model == [:]
        view == null
    }
    @Ignore
    void "test disable when content type is form"() {
        given:
        CRUDService mockCRUDService = Mock(CRUDService)
        mockCRUDService.updateWithAuditLog(_) >> {
            return configuration_a
        }
        controller.CRUDService = mockCRUDService
        params.id = configuration_a.id
        request.contentType = FORM_CONTENT_TYPE
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.onOrAfterDate = sdf.format(new Date())
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        when:
        controller.disable()
        then:
        response.status == 302
        response.redirectedUrl == "/singleCaseAlert/view/${configuration_a.id}"
        flash.message == "default.disabled.message"
    }
    @Ignore
    void "test disable when content type is not form"() {
        given:
        CRUDService mockCRUDService = Mock(CRUDService)
        mockCRUDService.updateWithAuditLog(_) >> {
            return configuration_a
        }
        controller.CRUDService = mockCRUDService
        params.id = configuration_a.id
        request.contentType = ALL_CONTENT_TYPE
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.onOrAfterDate = sdf.format(new Date())
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        when:
        controller.disable()
        then:
        response.status == 200
        model != [:]
    }

    void "test disable when configuration not found"() {
        given:
        params.id = 1000L
        request.contentType = ALL_CONTENT_TYPE
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.onOrAfterDate = sdf.format(new Date())
        params."templateQueries[0].template" = 1
        params."templateQueries[0].queryLevel" = QueryLevelEnum.CASE
        when:
        controller.disable()
        then:
        response.status == 404
        model == [:]
        view == null
    }
    @Ignore
    void "test disable when validation exception occurs"() {
        given:
        ConfigurationService configurationService = Mock(ConfigurationService)
        configurationService.detectChangesMade(_,_) >> {
            return []
        }
        controller.configurationService = configurationService
        CRUDService mockCRUDService = Mock(CRUDService)
        mockCRUDService.updateWithAuditLog(_) >> {
            return configuration_a
        }
        controller.CRUDService = mockCRUDService
        params.id = configuration_a.id
        request.contentType = FORM_CONTENT_TYPE
        SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY")
        params.onOrAfterDate = sdf.format(new Date())
        when:
        controller.disable()
        then:
        response.status == 200
        configuration_a.errors != null
        view == "/singleCaseAlert/edit"
    }
    @Ignore
    void "test list"() {
        when:
        controller.list()
        then:
        response.status == 200
        JSON.parse(response.text).size() == 2
    }

    void "test previousCaseState"() {
        given:
        controller.caseHistoryService = [getSecondLatestCaseHistory: { String caseNumber, Configuration configuration ->
            return caseHistoryObj
        }, saveCaseHistory                                         : { Map caseHistoryMap ->
            CaseHistory caseHistory = new CaseHistory(configId: configuration_a.id, currentDisposition: disposition,
                    justification: "change needed", caseNumber: "1S01", caseVersion: 1, productFamily: "newFamily",
                    modifiedBy: user.username, followUpNumber: 1, currentPriority: priority)
            caseHistory.save(flush: true, failOnError: true)
        }]
        params.productFamily = configuration_a
        params.followUpNumber = "1"
        when:
        controller.previousCaseState("1S01", 1, 1, configuration_a.id)
        then:
        response.status == 200
        CaseHistory.findByProductFamily("newFamily") != null
    }
    @Ignore
    void "test details"() {
        setup:
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }
        controller.userService.cacheService = [getPreferenceByUserId: { Long userId ->
            return user.preference
        },getCurrentUserName: { -> user.name}]
        controller.viewInstanceService = [fetchViewsListAndSelectedViewMap: { String alertType, Long viewId ->
            return [viewdList: [], selectedViewInstance: viewInstance1]
        }]
        controller.viewInstanceService = [fetchSelectedViewInstance: { String alertType, Long viewId ->
            return viewInstance1
        }]
        DataObjectService dataObjectService = Mock(DataObjectService)
        dataObjectService.getDataSourceMap(_)>>{
            return true
        }
        controller.dataObjectService = dataObjectService
        CacheService cacheService = Mock(CacheService)
        cacheService.getUserByUserNameIlike(_)>>{
            return user
        }
        def myCriteria = [
                get : {
                    eq('user.id', currentUser?.id)
                    or {
                        eq('isFirstUse', true)
                        eq('isUpdated', true)
                    }
                }
        ]

        ClipboardCases.metaClass.static.createCriteria = { myCriteria }
        ClipboardCases.createCriteria()

        ValidatedSignalService validatedSignalService = Mock(ValidatedSignalService)
        controller.validatedSignalService = validatedSignalService
        WorkflowRuleService workflowRuleService = Mock(WorkflowRuleService)
        controller.workflowRuleService = workflowRuleService
        controller.cacheService = cacheService
        controller.safetyLeadSecurityService.cacheService = cacheService
        UserService userService = Mock(UserService)
        userService.getUser() >> { return user }
        controller.dispositionService.userService = userService
        controller.alertService.actionService = [actionPropertiesJSON: { List<Map> actionTypeList -> }]
        params.configId = configuration_a.id
        params.isFaers = true
        params.isCaseSeriesAlert = true
        SpringSecurityUtils.metaClass.static.ifAllGranted = { String role ->
            return true
        }
        when:
        controller.details(true)
        then:
        response.status == 200
        view == "/singleCaseAlert/details"
        model.executedConfigId == configuration_a.id
        model.name == "case_series_config"
    }
    @Ignore
    void "test changeAssignedTo"() {
        setup:
        params.alertDetails = """[{"alertId":"${alert.id}"}]"""
        params.newValue = "User_${newUser.id}"
        params.executedConfigId = "${executedConfiguration.id}"
        controller.cacheService = [getPriorityByValue: { Long id -> priority }, getDispositionByValue: { Long id -> disposition }]
        controller.singleCaseAlertService = [updateSingleCaseAlertStates: { SingleCaseAlert singleCaseAlert, Map map ->
            alert.assignedTo = newUser
        }, getExecConfigurationById                                     : { Long id -> executedConfiguration },
                                             getActivityByType          : { ActivityTypeValue type -> actionType }]
        when:
        controller.changeAssignedTo()
        then:
        response.status == 400
        alert.assignedTo == newUser
    }

    void "test changeAssignedToGroup on success"() {
        setup:
        controller.activityService = [createActivityBulkUpdate      : { ActivityType type, User loggedInUser, String details,
                                                                        String justification, def attrs, String product, String event,
                                                                        User assignedToUser, String caseNumber, Group assignToGroup ->
        },
                                      batchPersistBulkUpdateActivity: { List<Map> activityList -> }]
        params.isArchived = true
        AlertService alertService = Mock(AlertService)
        controller.alertService = alertService
        String selectedId = "[${alert.id}]"
        String assignedToValue = "UserGroup_${wfGroup.id}"
        when:
        controller.changeAssignedToGroup(selectedId, assignedToValue)
        then:
        response.status == 200
        JSON.parse(response.text).message == "app.assignedTo.changed.fail"

    }

    void "test changeAssignedToGroup when exception occurs"() {
        setup:
        String selectedId = "[${alert.id}]"
        String assignedToValue = "UserGroup_${wfGroup.id}"
        when:
        controller.changeAssignedToGroup(selectedId, assignedToValue)
        then:
        response.status == 200
        JSON.parse(response.text).message == "app.assignedTo.changed.fail"
        JSON.parse(response.text).status == false
    }
    @Ignore
    void "test listByExecutedConfig"() {
        setup:
        UserService mockUserService = Mock(UserService)
        mockUserService.getUserFromCacheByUsername(_) >> {
            return user
        }
        controller.userService = mockUserService
        controller.singleCaseAlertService = [generateAlertDataDTO: { Map params, Boolean isFilterRequest ->
            new AlertDataDTO(userId: user.id, dispositionFilters: ["filter"])
        }]
        AlertService alertService = Mock(AlertService)
        alertService.getAlertFilterCountAndList(_) >> {
            return [fullCaseList: [], resultList: [], totalCount: 10, totalFilteredCount: 2]
        }
        controller.alertService = alertService
        params.length = 10
        params.start = 1
        params.callingScreen = Constants.Commons.DASHBOARD
        params.tempViewId = 1
//        UserService userService = Mock(UserService)
//        userService.getCurrentUserName()>>{
//            return user.username
//        }
//        controller.userService = userService
        when:
        controller.listByExecutedConfig(configuration_a.id, true)
        then:
        response.status == 200
        JSON.parse(response.text).recordsFiltered == 2
        JSON.parse(response.text).recordsTotal == 10
        JSON.parse(response.text).configId == configuration_a.id
    }

    void "test listByExecutedConfig in case of exception"() {
        setup:
        params.length = 10
        params.start = 1
        params.callingScreen = Constants.Commons.DASHBOARD
        params.tempViewId = 1
        when:
        controller.listByExecutedConfig(configuration_a.id, true)
        then:
        response.status == 200
        JSON.parse(response.text).recordsFiltered == 0
        JSON.parse(response.text).recordsTotal == 0
        JSON.parse(response.text).configId == configuration_a.id
    }

    void "test listByExecutedConfig when exception occurs"() {
        setup:
        params.length = 10
        params.start = -1
        params.callingScreen = Constants.Commons.DASHBOARD
        when:
        controller.listByExecutedConfig(configuration_a.id, true)
        then:
        response.status == 200
        JSON.parse(response.text).recordsFiltered == 0
        JSON.parse(response.text).recordsTotal == 0
        JSON.parse(response.text).configId == configuration_a.id
    }
    @Ignore
    void "test fetchPossibleValues"() {
        setup:
        SingleCaseAlertService mockSingleCaseAlertService = Mock(SingleCaseAlertService)
        mockSingleCaseAlertService.getDistinctValues(_,_,_) >> {
            return []
        }
        controller.singleCaseAlertService = mockSingleCaseAlertService
        controller.alertService =[preparePossibleValuesMap:{def domainName, Map<String, List> possibleValuesMap, Long executedConfigId->}]
        controller.reportFieldService =[getSelectableValuesForFields:{->[:]}]
        controller.pvsGlobalTagService =[fetchTagsAndSubtags:{->},fetchTagsfromMart:{List<Map> codeValues->[]},fetchSubTagsFromMart:{List<Map> codeValues->[]}]
        when:
        controller.fetchPossibleValues(executedConfiguration.id)
        then:
        response.status == 200
        JSON.parse(response.text)."isSusar" == ["Yes","No"]
    }
    @Ignore
    void "test fetchPossibleValues for negative executedConfigId"() {
        setup:
        SingleCaseAlertService mockSingleCaseAlertService = Mock(SingleCaseAlertService)
        mockSingleCaseAlertService.getDistinctValues(_,_,_) >> {
            return []
        }
        controller.singleCaseAlertService = mockSingleCaseAlertService
        controller.alertService =[preparePossibleValuesMap:{def domainName, Map<String, List> possibleValuesMap, Long executedConfigId->}]
        controller.reportFieldService =[getSelectableValuesForFields:{->[:]}]
        controller.pvsGlobalTagService =[fetchTagsAndSubtags:{->},fetchTagsfromMart:{List<Map> codeValues->[]},fetchSubTagsFromMart:{List<Map> codeValues->[]}]
        when:
        controller.fetchPossibleValues(-1)
        then:
        response.status == 200
        JSON.parse(response.text)."isSusar" == ["Yes","No"]
    }

    void "test modelData"(){
        given:
        UserService mockUserService = Mock(UserService)
        mockUserService.hasNormalAlertExecutionAccess(_) >> {
            return true
        }
        controller.userService = mockUserService

        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.getCioms1Id(_) >> {
            return 1L
        }
        controller.reportIntegrationService = mockReportIntegrationService
        when:
        Map result = controller.modelData(configuration_a,"copy")
        then:
        result.configurationInstance == configuration_a
        result.selectedCaseSeriesText == "case series a"
    }

    void "test fetchCaseSeries"(){
        setup:
        controller.reportIntegrationService = [get:{String url, String path, Map query->
            [status: 200, data: [result: [[ id: "id", text: "caseSeriesName"]],totalCount: 1]]}]
        when:
        controller.fetchCaseSeries("case",1,10)
        then:
        response.status == 200
        JSON.parse(response.text).totalCount == 1
    }
    void "test setNextRunDateAndScheduleDateJSON in case of null"() {
        when:
        controller.setNextRunDateAndScheduleDateJSON(configuration_b)

        then:
        configuration_a.nextRunDate == null
    }

    void "test setNextRunDateAndScheduleDateJSON"() {
        setup:
        ConfigurationService mockConfigurationService = Mock(ConfigurationService)
        mockConfigurationService.getNextDate(configuration_a) >> {
            return new DateTime(2015, 12, 15, 0, 0, 0).toDate()
        }
        controller.configurationService = mockConfigurationService

        when:
        controller.setNextRunDateAndScheduleDateJSON(configuration_a)

        then:
        configuration_a.nextRunDate == new DateTime(2015, 12, 15, 0, 0, 0).toDate()
    }

    void "test renderOnErrorScenario"(){
        setup:
        UserService mockUserService = Mock(UserService)
        mockUserService.hasNormalAlertExecutionAccess(_) >> {
            return true
        }
        controller.userService = mockUserService

        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.getCioms1Id(_) >> {
            return 1L
        }
        controller.reportIntegrationService = mockReportIntegrationService
        params.previousAction = 'create'
        params.id = 1
        when:
        controller.renderOnErrorScenario(configuration_a)
        then:
        response.status == 200
        view == '/singleCaseAlert/edit'
    }

    void "test renderOnErrorScenario when previousAction is copy"(){
        setup:
        UserService mockUserService = Mock(UserService)
        mockUserService.hasNormalAlertExecutionAccess(_) >> {
            return true
        }
        controller.userService = mockUserService

        ReportIntegrationService mockReportIntegrationService = Mock(ReportIntegrationService)
        mockReportIntegrationService.getCioms1Id(_) >> {
            return 1L
        }
        controller.reportIntegrationService = mockReportIntegrationService
        params.previousAction = 'copy'
        params.id = 1
        when:
        controller.renderOnErrorScenario(configuration_a)
        then:
        response.status == 200
        view == '/singleCaseAlert/create'
    }

    void "test getRunOnceScheduledDateJson"() {
        when:
        String result = controller.getRunOnceScheduledDateJson()

        then:
        result.contains('{"name" :"UTC","offset" : "+00:00"}') == true
    }

    void "test review"() {
        when:
        controller.review()

        then:
        response.status == 200
        view == '/singleCaseAlert/index'
    }

    void "test viewConfig in case of null id"() {
        when:
        params.id = null
        controller.viewConfig()

        then:
        controller.flash.error == "app.configuration.id.null"
        response.status == 302
        response.redirectedUrl == '/configuration/listAllResults'
    }

    void "test viewConfig in case of other params than result"() {
        when:
        params.id = 1
        params.from = 'notResult'
        controller.viewConfig()

        then:
        response.status == 302
        response.redirectedUrl == '/configuration/view/1'
    }

    void "test viewConfig"() {
        when:
        params.id = 1
        params.from = 'result'
        controller.viewConfig()

        then:
        response.status == 302
        response.redirectedUrl == '/configuration/viewExecutedConfig/1'
    }

    void "test listAllResults"() {
        setup:
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }

        when:
        controller.listAllResults()

        then:
        response.status == 200
        view == '/singleCaseAlert/executionStatus'
    }
    @Ignore
    void "test notFound"(){
        setup:
        params.id = 1
        when:
        controller.notFound()
        then:
        response.status == 302
        controller.flash.message == 'default.not.found.message'
        response.redirectedUrl == '/singleCaseAlert/index'
    }
    @Ignore
    void "test noAlerts"(){
        when:
        controller.noAlerts()
        then:
        response.status == 302
        flash.message == 'default.no.alerts.selected'
    }

    void "test changePriorityOfAlert"() {
        setup:
        SingleCaseAlertService mockSingleCaseAlertService = Mock(SingleCaseAlertService)
        mockSingleCaseAlertService.changePriorityOfAlerts(_,_,_,_,_) >> {
            return
        }
        controller.singleCaseAlertService = mockSingleCaseAlertService
        String selectedRows = '[{"alert.id":1},{"executedConfigObj.id":1},{"configObj.id":1}]'
        when:
        controller.changePriorityOfAlert(selectedRows, priorityNew, "justification")

        then:
        response.status == 200
        response.json.status == true
    }

    void "test changePriorityOfAlert in case of Exception"() {
        setup:
        String selectedRows = '[{"alert.id":1},{"executedConfigObj.id":1},{"configObj.id":1}]'

        when:
        controller.changePriorityOfAlert(selectedRows, priorityNew, "justification")

        then:
        response.status == 200
        response.json.status == false
        response.json.message == "app.label.priority.change.error"
    }
    @Ignore
    void "test renderReportOutputType"(){
        given:
        params.outputFormat ="pdf"
        def dynamicReportService = Mock(DynamicReportService)
        dynamicReportService.getContentType(ReportFormat.PDF)
        controller.dynamicReportService = dynamicReportService
        when:
        controller.renderReportOutputType(file)
        then:
        noExceptionThrown()
    }

    void "test toggleFlag"() {
        when:
        controller.toggleFlag(1L)
        then:
        response.status == 200
        response.json.flagged == true
        response.json.success == 'ok'
    }

    void "test toggleFlag in case of null id"() {
        when:
        controller.toggleFlag(null)
        then:
        response.status == 404
    }

    void "test listConfig"() {
        when:
        controller.listConfig()

        then:
        response.status == 200
    }

    void "test deleteAttachment"(){
        setup:
        def attachmentId= 1
        def alertId = alert.id
        AttachmentableService mockattachmentableService=Mock(AttachmentableService)
        mockattachmentableService.removeAttachment(attachmentId)>>{
            Attachment.get(1L).delete()
            return true
        }
        controller.attachmentableService= mockattachmentableService
        when:
        controller.deleteAttachment(alertId,attachmentId)
        then:
        response.status==200
    }

    void "test deleteAttachment for null"(){
        setup:
        def attachmentId= null
        def alertId = null
        when:
        controller.deleteAttachment(alertId,attachmentId)
        then:
        response.json.success == false
        response.status==200
    }

    void "test getAttachmentList"(){
        when:
        Boolean result = controller.getAttachmentList(null)
        then:
        result == false
    }

    void "test getExecutedConfigurationsWithAllowedProducts"(){
        setup:
        ProductBasedSecurityService mockProductBasedSecurityService = Mock(ProductBasedSecurityService)
        mockProductBasedSecurityService.allAllowedProductForUser(user) >> {
            return ['paracetamol','brufen']
        }
        controller.productBasedSecurityService = mockProductBasedSecurityService
        when:
        List<ExecutedConfiguration> result = controller.getExecutedConfigurationsWithAllowedProducts()
        then:
        result != null
    }

    void "test archivedAlert"() {
        setup:
        params.start = 0
        params.length = 2
        when:
        controller.archivedAlert(executedConfiguration.id)

        then:
        response.status == 200
    }

    void "test downloadCaseForm"(){
        setup:
        params.id = 1L
        params.outputFormat ="pdf"
        CaseFormService caseFormService = Mock(CaseFormService)
        caseFormService.fetchFile(_,_)>>{
            return [file: file, filename: file.name]
        }
        controller.caseFormService = caseFormService
        when:
        controller.downloadCaseForm(1L)
        then:
        response.status == 200
    }

    void "test getDefaultReviewPeriod"(){
        when:
        def result = controller.getDefaultReviewPeriod(configuration_a)
        then:
        response.status == 200
        result == 1
    }

    void "test editShare when exception"() {
        setup:
        params.sharedWith = "User_${user.id}"
        params.executedConfigId = "1"
        when:
        controller.editShare()
        then:
        response.status == 302
        response.redirectedUrl == '/singleCaseAlert/review'
    }

    void "test editShare"() {
        setup:
        CRUDService mockCRUDService = Mock(CRUDService)
        mockCRUDService.updateWithAuditLog(_) >> {
            return configuration_a
        }
        controller.CRUDService = mockCRUDService
        when:
        params.sharedWith = "User_${user.id}"
        params.executedConfigId = "1"
        controller.editShare()

        then:
        response.status == 302
        response.redirectedUrl == '/singleCaseAlert/review'
    }

    void "test changeDisposition action on success"() {
        setup:
        String selectedRows = '[{"alert.id":1},{"alert.id":2}]'
        Disposition targetDisposition = Disposition.get(1)
        String justification = "change needed"
        String validatedSignalName = "ValidatedSignal2"
        String productJson = "product1"
        String incomingDisposition = 'Threshold Not Met'

        SingleCaseAlertService mockSingleCaseAlertService = Mock(SingleCaseAlertService)
        mockSingleCaseAlertService.changeDisposition([1L, 2L], targetDisposition,incomingDisposition, justification,validatedSignalName, productJson, false,1) >> {
            return [:]
        }
        controller.singleCaseAlertService = mockSingleCaseAlertService
        AlertService alertService = Mock(AlertService)
        alertService.fetchPreviousDispositionCount(_,_,_)>>{
            return 1L
        }
        controller.alertService = alertService
        when:
        controller.changeDisposition(selectedRows, targetDisposition,incomingDisposition, justification, validatedSignalName, productJson,1)

        then:
        JSON.parse(response.text).code == 200
        JSON.parse(response.text).status == false
    }

    void "test changeDisposition action when exception occurs"() {
        setup:
        String selectedRows = '[{"alert.id":1},{"alert.id":2}]'
        Disposition targetDisposition = Disposition.get(1)
        String justification = "change needed"
        String validatedSignalName = "ValidatedSignal2"
        String productJson = "product1"
        String incomingDisposition = 'Threshold Not Met'

        when:
        controller.changeDisposition(selectedRows, targetDisposition, justification,incomingDisposition,validatedSignalName, productJson,1)

        then:
        JSON.parse(response.text).code == 200
        JSON.parse(response.text).status == false
        JSON.parse(response.text).data == null
        JSON.parse(response.text).message == "app.label.disposition.change.error"
    }

    void "test alertDetailListHeaders"(){
        when:
        controller.alertDetailListHeaders()
        then:
        response.status == 200
    }

    void "test isWarningMessageInAutoRouteDisposition"(){
        when:
        controller.isWarningMessageInAutoRouteDisposition(1L,false)
        then:
        response.status == 200
    }

    void "test getAssignedToChangeMap"(){
        when:
        controller.getAssignedToChangeMap(sca_a)
        then:
        response.status == 200
    }

    void "test fetchCaseFormNames"(){
        when:
        String execConfigId = '1'
        controller.fetchCaseFormNames(execConfigId)
        then:
        response.status == 200
    }

    void "test fetchCaseForms"(){
        when:
        String execConfigId = '1'
        controller.fetchCaseForms(execConfigId)
        then:
        response.status == 200
    }

    void "test bindAsOfVersionDateOfSingleCase"(){
        setup:
        configuration_a.evaluateDateAs = 'VERSION_ASOF'
        when:
        controller.bindAsOfVersionDateOfSingleCase(configuration_a)
        then:
        response.status == 200
    }
    @Ignore
    void "test exportDetailReport when selectedCases is null"(){
        when:
        controller.exportDetailReport()
        then:
        response.status == 302
        flash.message == 'default.no.alerts.selected'
    }
    @Ignore
    void "test exportDetailReport"(){
        setup:
        SingleCaseAlertService singleCaseAlertService = Mock(SingleCaseAlertService)
        singleCaseAlertService.listSelectedAlerts(_,_) >> {
            return [sca_a]
        }
        controller.singleCaseAlertService = singleCaseAlertService
        params.isArchived = false
        when:
        controller.exportDetailReport()
        then:
        response.status == 302
        flash.message == 'default.no.alerts.selected'
    }

    void "test exportCaseForm"(){
        setup:
        UserService userService=Mock(UserService)
        userService.getUser()>>{
            return user
        }
        controller.userService=userService
        CaseFormService caseFormService = Mock(CaseFormService)
        caseFormService.fetchDataBackground(_,_)>>{
            return
        }
        controller.caseFormService = caseFormService
        when:
        controller.exportCaseForm()
        then:
        response.status == 200
    }

    void "test updateAutoRouteDisposition"(){
        setup:

        when:
        controller.updateAutoRouteDisposition(1,false)
        then:
        response.status == 200
    }

    void "test changeAlertLevelDisposition"(){
        setup:
        SingleCaseAlertService mockSingleCaseAlertService = Mock(SingleCaseAlertService)
        mockSingleCaseAlertService.changeAlertLevelDisposition(_,_) >> {
            return -1
        }
        controller.singleCaseAlertService = mockSingleCaseAlertService
        Disposition targetDisposition = Disposition.get(1)
        String justificationText = "Update Disposition"
        when:
        controller.changeAlertLevelDisposition(targetDisposition,justificationText,executedConfiguration,false)
        then:
        response.json.message == "alert.level.review.completed"
        response.status == 200
    }

    void "test changeAlertLevelDisposition for positive updatedRowsCount"(){
        setup:
        SingleCaseAlertService mockSingleCaseAlertService = Mock(SingleCaseAlertService)
        mockSingleCaseAlertService.changeAlertLevelDisposition(_,_) >> {
            return 1
        }
        controller.singleCaseAlertService = mockSingleCaseAlertService
        DispositionService dispositionService = Mock(DispositionService)
        dispositionService.sendDispChangeNotification(_,_) >> {
            return true
        }
        controller.dispositionService = dispositionService
        Disposition targetDisposition = Disposition.get(1)
        String justificationText = "Update Disposition"
        when:
        controller.changeAlertLevelDisposition(targetDisposition,justificationText,executedConfiguration,false)
        then:
        response.json.message == "alert.level.disposition.successfully.updated"
        response.status == 200
    }

    void "test changeAlertLevelDisposition for Exception"(){
        setup:
        Disposition targetDisposition = Disposition.get(1)
        String justificationText = "Update Disposition"
        when:
        controller.changeAlertLevelDisposition(targetDisposition,justificationText,executedConfiguration,false)
        then:
        response.json.message == 'app.label.disposition.change.error'
        response.status == 200
    }

    void "test getCaseHistoryMap"(){
        when:
        Map result = controller.getCaseHistoryMap(1L,1L,caseHistoryObj,sca_a,"DISPOSITION","justification3")
        then:
        result.size() == 14
        response.status == 200
    }

    void "test searchTagsList in case of Exception"(){
        when:
        controller.searchTagsList("term",1,2,true)
        then:
        response.status == 200
    }
    @Ignore
    void "test sendAssignedToEmail"(){
        setup:
        UserService userService = Mock(UserService)
        userService.getUserIdFromEmail(_)>>{
            return 1
        }
        controller.userService = userService
        when:
        controller.sendAssignedToEmail(newUser,alert,caseHistoryObj,"singleCaseAlert","messageToUser","newUserName")
        controller.cacheService = [getPriorityByValue: { Long id -> priority }, getDispositionByValue: { Long id -> disposition }]

        then:
        response.status == 200
    }

    void "test sendMailOfAssignedToAction"(){
        setup:
        UserService userService = Mock(UserService)
        userService.getUserIdFromEmail(_)>>{
            return 1
        }
        controller.userService = userService

        List<User> oldUserList = [user]
        List<User> newUserList = [newUser]

        when:
        controller.sendMailOfAssignedToAction(oldUserList,newUserList,alert,caseHistoryObj,false,"random")
        then:
        response.status == 200

    }

    void "test listCaseInfo"(){
        setup:
        SingleCaseAlertService singleCaseAlertService = Mock(SingleCaseAlertService)
        singleCaseAlertService.listCaseInfo(_,_,_,_,_)>>{
            return []
        }
        controller.singleCaseAlertService = singleCaseAlertService
        UserService userService = Mock(UserService)
        Preference preference = new Preference()
        preference.timeZone = "UTC"
        userService.getCurrentUserPreference() >> preference
        userService.getUser() >> new User(id: 20)
        controller.userService = userService
        ProductBasedSecurityService mockProductBasedSecurityService = Mock(ProductBasedSecurityService)
        mockProductBasedSecurityService.allAllowedProductForUser(user) >> {
            return ['paracetamol','brufen']
        }
        controller.productBasedSecurityService = mockProductBasedSecurityService

        when:
        controller.listCaseInfo("1","123","321")
        then:
        response.status == 200
    }

    void "test listCaseInfo in case of Exception"(){
        setup:

        when:
        def result = controller.listCaseInfo("1","123","321")
        then:
        result == null
        response.status == 200
    }

    void "test generateCaseSeries"(){
        setup:
        params.seriesName = "seriesName"
        params.id =1
        ReportExecutorService reportExecutorService = Mock(ReportExecutorService)
        reportExecutorService.generateCaseSeries(_, _) >> {
            return [status: HttpStatus.SC_OK, result: [status: true, data: [id: 10]]]
        }
        controller.reportExecutorService = reportExecutorService
        when:
        controller.generateCaseSeries()
        then:
        response.status == 200
    }

    void "test addCaseToSingleCaseAlert when nothing is passed"(){
        when:
        controller.addCaseToSingleCaseAlert()
        then:
        response.status == 200
        response.json.message == 'app.error.fill.all.required'
    }

    void "test addCaseToSingleCaseAlert in case of exception"(){
        setup:
        params.caseNumber = "17JP00000000001411"
        params.file = file
        params.justification = "changed name"
        when:
        controller.addCaseToSingleCaseAlert()
        then:
        response.status == 200
        response.json.message == 'Something unexpected happened at server'
    }

    void "test addCaseToSingleCaseAlert in case of null singleCaseAlertList"(){
        setup:
        SingleCaseAlertService singleCaseAlertService = Mock(SingleCaseAlertService)
        singleCaseAlertService.saveCaseSeriesInDB(_,_,_)>>{
            return [:]
        }
        controller.singleCaseAlertService = singleCaseAlertService
        params.executedConfigId = 1
        params.caseNumber = "17JP00000000001411"
        params.justification = "changed name"
        when:
        controller.addCaseToSingleCaseAlert()
        then:
        response.status == 200
        response.json.message == 'singleCaseAlert.invalid.caseNumber.data'
    }

    void "test addCaseToSingleCaseAlert in case of singleCaseAlertList"(){
        setup:
        List singleAlertCaseList = [sca_a]
        SingleCaseAlertService singleCaseAlertService = Mock(SingleCaseAlertService)
        singleCaseAlertService.saveCaseSeriesInDB(_,_,_)>>{
            return [singleAlertCaseList : singleAlertCaseList]
        }
        controller.singleCaseAlertService = singleCaseAlertService
        params.executedConfigId = 1
        params.caseNumber = "17JP00000000001411"
        params.justification = "changed name"
        when:
        controller.addCaseToSingleCaseAlert()
        then:
        response.status == 200
        response.json.message == 'singleCaseAlert.add.caseNumber.success'
    }

    void "test getAllEmailsUnique"(){
        setup:
        UserService userService = Mock(UserService)
        userService.getAllEmails(_) >> {
            return ["user2@gmail.com","user3@gmail.com"]
        }
        controller.userService = userService
        when:
        def result = controller.getAllEmailsUnique()
        then:
        result == ["user2@gmail.com","user3@gmail.com"]
        response.status == 200
    }
    @Ignore
    void "test updateAndExecuteAlert when configId null"(){
        setup:
        params.configId = null
        when:
        controller.updateAndExecuteAlert()
        then:
        response.status == 302
        response.redirectedUrl == "/singleCaseAlert/index"
        flash.message == 'default.not.found.message'
    }

    void "test updateAndExecuteAlert"(){
        setup:
        params.configId = 1
        ConfigurationService configurationService = Mock(ConfigurationService)
        configurationService.fetchRunningAlertList(_) >> [11l, 2l,1l]
        controller.configurationService = configurationService
        when:
        controller.updateAndExecuteAlert()
        then:
        response.status == 302
        response.redirectedUrl == "/alertAdministration/index"
        flash.error == 'app.configuration.alert.running'
    }
    @Ignore
    void "test upload"(){
        setup:
        List<MultipartFile> filesToUpload = [file]
        List<MultipartFile> uploadedFiles = [file]

        AttachmentableService mockAttachmentableService = Mock(AttachmentableService)
        mockAttachmentableService.attachUploadFileTo(_,_,_,_) >> {
            return [filesToUpload: filesToUpload, uploadedFiles: uploadedFiles]
        }
        controller.attachmentableService = mockAttachmentableService
        ActivityService activityService = Mock(ActivityService)
        activityService.createActivityBulkUpdate(_,_,_,_,_,_,_,_,_,_)>>{
            return activity1
        }
        controller.activityService = activityService
        SingleCaseAlert.metaClass.getAttachments = {[new Attachment(dateCreated: new Date())]}
        params.alertId = '1'
        params.attachments=[filename:"file"]
        params.isArchived = false
        params.description="description3"
        when:
        controller.upload()
        then:
        response.status == 200
    }

    void "test updateAndExecuteAlertBulk"(){
        setup:
        def configIdList = ["1", "2", "3"]
        params.configIdList = configIdList.join(",")
        params.configId = 1
        params.evaluateDateAs == 'LATEST_VERSION'
        ConfigurationService configurationService = Mock(ConfigurationService)
        configurationService.fetchRunningAlertList(_) >> [1l, 2l]
        controller.configurationService = configurationService
        AlertAdministrationService alertAdministrationService = Mock(AlertAdministrationService)
        alertAdministrationService.nullifyAutoAdjustmentRelatedFlow(_,_)>>{
            return
        }
        controller.alertAdministrationService = alertAdministrationService
        CRUDService mockCRUDService = Mock(CRUDService)
        mockCRUDService.updateWithAuditLog(_) >> {
            return configuration_a
        }
        controller.CRUDService = mockCRUDService
        when:
        controller.updateAndExecuteAlertBulk()
        then:
        response.status == 302
        response.redirectedUrl == "/configuration/executionStatus?alertType=SINGLE_CASE_ALERT"
    }
    @Ignore
    void "test exportSignalSummaryReport"(){
        setup:
        DynamicReportService dynamicReportService = Mock(DynamicReportService)
        dynamicReportService.createSignalDetectionReport(_,_,_,_,_,_)>>{
            return file
        }
        controller.dynamicReportService = dynamicReportService
        params.isArchived = false
        when:
        controller.exportSignalSummaryReport(1,true,"dashboard","pdf")
        then:
        response.status == 200
    }

    void "test validateTriggerParams for valid Input"(){
        setup:
        String triggerParams = "123"
        when:
        def result = controller.validateTriggerParams(triggerParams)
        then:
        result == true
    }

    void "test validateTriggerParams for non-numeric input"(){
        setup:
        String triggerParams = "abc"
        when:
        def result = controller.validateTriggerParams(triggerParams)
        then:
        result == false
    }

    void "test exportReport"(){
        setup:
        DynamicReportService dynamicReportService = Mock(DynamicReportService)
        dynamicReportService.createAlertsReport(_,_) >> {
            return file
        }
        controller.dynamicReportService = dynamicReportService
        SingleCaseAlertService singleCaseAlertService = Mock(SingleCaseAlertService)
        singleCaseAlertService.listSelectedAlerts(_,_) >> {
            return [sca_a]
        }
        controller.singleCaseAlertService = singleCaseAlertService
        params.isArchived = true
        params.cumulativeExport = false
        params.tempViewId = 1
        params.faers = true
        params.callingScreen = "dashboard"
        params.selectedCases = "selectedCases"
        when:
        controller.exportReport()
        then:
        response.status == 200
    }
    @Ignore
    void "test exportReport in case callingScreen other than dashboard"(){
        setup:
        DynamicReportService dynamicReportService = Mock(DynamicReportService)
        dynamicReportService.createAlertsReport(_,_) >> {
            return file
        }
        controller.dynamicReportService = dynamicReportService
        SingleCaseAlertService singleCaseAlertService = Mock(SingleCaseAlertService)
        singleCaseAlertService.listSelectedAlerts(_,_) >> {
            return [sca_a]
        }
        controller.singleCaseAlertService = singleCaseAlertService
        params.isArchived = true
        params.cumulativeExport = false
        params.tempViewId = 1
        params.faers = true
        params.callingScreen = "create"
        params.selectedCases = "selectedCases"
        params.isCaseSeries = false
        Map params = [viewId:viewInstance1.id as Long, advancedFilterId: advanceFilter.id as Long]
        when:
        controller.exportReport()
        then:
        response.status == 200
    }
    @Ignore
    void "test saveAlertTags"(){
        setup:
        params.justification = "changed name"
        params.isArchived = false
        Long executedConfigId = 1
        Long alertId = alert.id
        String alertTags = '["tag1", "tag2", "tag3"]'
        String globalTags = '["global1", "global2"]'
        String deletedCaseSeriesTags = '["deletedTag1"]'
        String deletedGlobalTags = '["deletedGlobal1"]'
        String addedGlobalTags = '["addedGlobal1"]'
        String addedCaseSeriesTags = '["addedTag1"]'

        AggregateCaseAlertService mockAggregateCaseAlertService = Mock(AggregateCaseAlertService)
        mockAggregateCaseAlertService.getDomainObject(_) >> {
            return AggregateCaseAlert
        }
        mockAggregateCaseAlertService.createBasePEHistoryMap(_,_,_)>>{
            return [customProperty: 'customProperty']
        }
        controller.aggregateCaseAlertService= mockAggregateCaseAlertService

        CaseHistoryService caseHistoryService = Mock(CaseHistoryService)
        caseHistoryService.getLatestCaseHistory(_,_)>>{
            return caseHistoryObj
        }
        controller.caseHistoryService = caseHistoryService
        def dataSource = Mock(DataSource)
        controller.dataSource = dataSource
        controller.dataSource_pva = dataSource
        SignalDataSourceService signalDataSourceService = Mock(SignalDataSourceService)
        signalDataSourceService.getReportConnection("pva") >> {
            return dataSource
        }
        controller.signalDataSourceService = signalDataSourceService
        when:
        controller.saveAlertTags(alertId,executedConfigId,alertTags,globalTags,deletedCaseSeriesTags,deletedGlobalTags,addedGlobalTags,addedCaseSeriesTags)
        then:
        response.status == 200
    }
}