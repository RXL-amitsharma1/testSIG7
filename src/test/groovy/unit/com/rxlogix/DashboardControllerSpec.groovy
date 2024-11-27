package com.rxlogix

import com.rxlogix.action.ActionService
import com.rxlogix.attachments.Attachment
import com.rxlogix.cache.CacheService
import com.rxlogix.config.*
import com.rxlogix.dto.TriggeredReviewDTO
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.AdHocAlert
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.SignalStatusHistory
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.Topic
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.Group
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import grails.testing.web.controllers.ControllerUnitTest
import org.grails.datastore.gorm.bootstrap.support.InstanceFactoryBean
import org.hibernate.Session
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Ignore
import spock.lang.Specification

import javax.sql.DataSource

/**
 * See the API for {@link grails.test.mixin.web.ControllerUnitTestMixin} for usage instructions
 */
class DashboardControllerSpec extends Specification implements ControllerUnitTest<DashboardController> {

    User user
    Group wfGroup
    def adHocAlertObj
    def attrMapObj
    Session session1
    Action action
    Disposition disposition
    UserService userService
    SingleCaseAlert alert
    AggregateCaseAlert aggAlert
    Disposition dispositionObj2
    ValidatedSignal validatedSignal
    SignalStatusHistory signalStatusHistory
    Disposition defaultDisposition
    Disposition autoRouteDisposition
    Configuration alertConfiguration
    ExecutedConfiguration executedConfiguration
    Topic topic
    PVSState workFlowState
    ActionConfiguration actionConfiguration
    ActionType actionType

    static doWithSpring = {
        dataSource(InstanceFactoryBean, [:] as DataSource, DataSource)
    }

    def setup() {
        Priority priority =
                new Priority([displayName: "mockPriority", value: "mockPriority", display: true, defaultPriority: true, reviewPeriod: 1])
        priority.save(failOnError: true)

        disposition = new Disposition(value: "ValidatedSignal", displayName: "ValidatedSignal",
                validatedConfirmed: true, reviewCompleted: true, abbreviation: "vs")
        disposition.save(failOnError: true)
        dispositionObj2 = new Disposition(value: "ValidatedSignal2", displayName: "ValidatedSignal2",
                validatedConfirmed: true, reviewCompleted: true, abbreviation: "vs")
        dispositionObj2.save(failOnError: true)

        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")

        wfGroup = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP, createdBy: 'createdBy',
                modifiedBy: 'modifiedBy',
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                defaultSignalDisposition: disposition,
                autoRouteDisposition: autoRouteDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(flush: true, failOnError: true)

        user = new User(id: '1', username: 'test_user1', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.addToGroups(wfGroup)
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name1' }
        user.metaClass.getEmail = { 'fake.email1@fake.com' }
        user.save(flush:true)

        action = new Action([details: "no comments", createdDate: new Date(),
                             dueDate: new Date(), assignedTo: user, actionStatus: "Closed"])

        mockDomain(Priority, [
                [value: "High", display: true, displayName: "High", reviewPeriod: 3, priorityOrder: 1]
        ])

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
                configId: 111,
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
                createdBy: config.assignedTo.username,
                modifiedBy: config.assignedTo.username,
                dateCreated: config.dateCreated,
                lastUpdated: config.dateCreated,
                productFamily: "Test Product A",
                isNew: true,
                productId: 3982,
                followUpExists: true,
                attributesMap: attrMapObj,
                malfunction: "true",
                comboFlag: "true")
        alert.save(failOnError: true)

        aggAlert = new AggregateCaseAlert(
                alertConfiguration: config,
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
                prr: "1",
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
                ebgmStr: "male:0.1, female:0.2, unknown:0.3"

        )
        aggAlert.save(failOnError: true)

        adHocAlertObj = new AdHocAlert(
                productSelection: "something",
                detectedDate: new DateTime(2015, 12, 15, 0, 0, 0).toDate(),
                detectedBy: "Company",
                initialDataSource: "test",
                name: "Test Name2",
                alertVersion: 0,
                priority: new Priority(value: "High"),
                topic: "rash",
                disposition: disposition,
                assignedTo: user
        )

        signalStatusHistory = new SignalStatusHistory(dateCreated: new Date(), statusComment: "Test Status Comment", signalStatus: "Signal Status",
                dispositionUpdated: true, performedBy: "Test User", id: 1)
        signalStatusHistory.save(flush:true)

        validatedSignal = new ValidatedSignal(
                name: "test_name",
                products: "test_products",
                endDate: new Date(),
                assignedTo: user,
                assignmentType: 'USER',
                modifiedBy: user.username,
                priority: priority,
                workflowGroup: wfGroup,
                disposition: defaultDisposition,
                createdBy: user.username,
                startDate: new Date(),
                id: 1,
                genericComment: "Test notes"

        )
        validatedSignal.addToSignalStatusHistories(signalStatusHistory)
        validatedSignal.save(flush:true)

        adHocAlertObj.save(failOnError: true)

        workFlowState = new PVSState(id:1L,value: "10",display: true,finalState: false,displayName: "display")
        workFlowState.save(flush:true)

        topic = new Topic(name: "topic1",products: "paracetamol",disposition: defaultDisposition, createdBy: 'createdBy', modifiedBy: 'modifiedBy',
                workflowState: workFlowState, startDate: new Date(),endDate: new Date(), assignedTo: user,priority:priority)
        topic.save(flush:true)

        actionType= new ActionType(description: 'test', displayName: 'test', value: 'test')
        actionConfiguration = new ActionConfiguration(displayName: 'test', value: 'test')
        Action action = new Action(dueDate: new Date(),createdDate: new Date(),assignedTo: user,assignedToGroup: wfGroup,
                type: actionType,config: actionConfiguration,details: "action",actionStatus: "actionStatus")
        action.save(flush:true)

        userService = Mock(UserService)
        Preference preference = new Preference()
        preference.timeZone = "UTC"
        userService.getCurrentUserPreference() >> preference
        userService.getUser() >> user
        userService.getUserFromCacheByUsername(_) >> user
        controller.userService = userService

        def mockedSource = Mock(DataSource)
        session1 = Mock(Session)

        TriggeredReviewDTO triggeredReviewDTO =
                new TriggeredReviewDTO(userId: 1L,reviewedList: [disposition,dispositionObj2] , isProductSecurity: false,
                        requiresReviewedList: [disposition,dispositionObj2])

        DashboardService mockDashboardService = Mock(DashboardService)
        mockDashboardService.saveDashboardConfiguration(_) >> {
            return true
        }
        mockDashboardService.getFirstXExecutedConfigurationList(_) >> {
            return [executedConfiguration]
        }
        mockDashboardService.alertByDueDate() >> { return [[name: "Due Date in Future", data: [userId : 1,workflowGroupId : 1]],
                                                           [name: "Due Today", data: [userId : 2,workflowGroupId : 2]],
                                                           [name: "Passed Due Date", data: [userId : 3,workflowGroupId : 3]]]
        }
        mockDashboardService.generateTriggeredReview(_) >> {
            return triggeredReviewDTO
        }
        controller.dashboardService = mockDashboardService

    }

    def cleanup() {
    }

    void "test sidebar"() {

        setup:
        controller.userService = userService
        controller.dashboardService.userService = userService
        def adHocAlertService = Mock(AdHocAlertService)
        def actionService = Mock(ActionService)
        def validatedSignalService = Mock(ValidatedSignalService)
        def dashBoardService = Mock(DashboardService)


        adHocAlertService.getAssignedAdhocAlertList(_) >> [user]
        dashBoardService.adHocAlertService = adHocAlertService
        actionService.getActionDashboardCount(_) >> user.count()
        dashBoardService.actionService = actionService
        validatedSignalService.getAssignedSignalList() >> [user]
        validatedSignalService.userService = userService
        dashBoardService.validatedSignalService = validatedSignalService
        dashBoardService.getAlertCounts(_, _) >> ['test': 1]
        dashBoardService.dashboardCounts() >> ['test': 1]
        controller.dashboardService = dashBoardService

        when:
        controller.sideBar()

        then:
        response.getJson() == [test: 1]

    }

    void "test ahaByStatus"() {
        setup:
        controller.adHocAlertService.userService =userService
        def adHocService = Mock(AdHocAlertService)
        adHocService.getAssignedAdhocAlertList(_) >> [adHocAlertObj]
        adHocService.alertCountsByDisposition(_) >> ['test', 1]
        adHocService.byDisposition() >> ['test', 1]
        controller.adHocAlertService = adHocService

        when:
        controller.ahaByStatus()

        then:
        response.getJson() == ['test', 1]
    }

    void "test caseByStatus"(){
        setup:
        DashboardService mockDashboardService = Mock(DashboardService)
        mockDashboardService.generateReviewByStateChart(_) >> {
            return [[id : alert.id]]
        }
        controller.dashboardService = mockDashboardService

        when:
        controller.caseByStatus()

        then:
        response.status == 200
        response.getJson().id == [alert.id]
    }


    void "test actionList"(){
        setup:
        ActionService mockActionService = Mock(ActionService)
        mockActionService.listFirstXRowsByAssignedTo(_,_,_) >> {
            return [[id:1,config: actionConfiguration],[id:2,type:actionType]]
        }
        controller.actionService = mockActionService

        when:
        controller.actionList()

        then:
        response.status == 200
        response.getJson()[0].id == 1
        response.getJson()[1].id == 2
    }

    void "test alertByDueDate"(){
        when:
        controller.alertByDueDate()

        then:
        response.status == 200
        response.getJson()[0].name == "Due Date in Future"
        response.getJson()[0].data.userId == 1
    }

    void "test aggAlertByStatus"(){
        setup:
        DashboardService mockDashboardService = Mock(DashboardService)
        mockDashboardService.generateReviewByStateChart(_) >> {
            return [[id : aggAlert.id]]
        }
        controller.dashboardService = mockDashboardService

        when:
        controller.aggAlertByStatus()

        then:
        response.status == 200
        response.getJson().id == [aggAlert.id]
    }


    void "test SignalList"(){
        setup:
        ValidatedSignalService mockValidatedSignalService = Mock(ValidatedSignalService)
        mockValidatedSignalService.getValidatedSignalList() >> {
            return [ValidatedSignal]
        }
        mockValidatedSignalService.createValidatedSignalDTO(_,_,_) >> {
            return [[id:validatedSignal.id],[name: validatedSignal.name]]
        }
        controller.validatedSignalService = mockValidatedSignalService

        when:
        controller.signalList()

        then:
        response.status == 200
        response.getJson()[0].id == validatedSignal.id
    }

    void "test topicList"(){
        setup:
        ProductBasedSecurityService mockProductBasedSecurityService = Mock(ProductBasedSecurityService)
        mockProductBasedSecurityService.allAllowedProductForUser(user) >> {
            return ['paracetamol','brufen']
        }
        controller.productBasedSecurityService = mockProductBasedSecurityService

        when:
        List topics = controller.topicList()

        then:
        topics == null
    }

    void "test fetchSchedulesConfigForTriggeredAlert"(){
        when:
        List scheduledConfigs = controller.fetchSchedulesConfigForTriggeredAlert()

        then:
        !scheduledConfigs
    }

    void "test getProductByStatus"(){
        setup:
        DashboardService mockDashboardService = Mock(DashboardService)
        mockDashboardService.generateProductsByStatusChart(_) >> {
            return [id : alert.id]
        }
        controller.dashboardService = mockDashboardService

        when:
        controller.getProductByStatus("Constants.AlertConfigType.SINGLE_CASE_ALERT")

        then:
        response.status == 200
        response.getJson().id == alert.id
    }

    void "test saveDashboardConfig"(){
        when:
        Boolean result = controller.saveDashboardConfig()

        then:
        response.status == 200
        !result
    }

    void "test alertList"(){
        setup:
        userService.getCurrentUserName() >> "test_user1"
        userService.getUserFromCacheByUsername(_) >> user
        when:
        Boolean result = controller.alertList()

        then:
        response.status == 200
        !result
    }

    void "test fetchTopNavAndSidePanelDetails"() {
        given:
        Map<String, Object> pvsAppInfo = [name: 'PVS', version: '1.0']
        Map<String, Object> pvrAppInfo = [name: 'PVR', version: '1.0']
        Map<String, Object> pvAdminAppInfo = [name: 'PVAdmin', version: '1.0']
        List<Map<String, Object>> settings = [[name: 'setting1', value: 'value1']]
        List<Map<String, Object>> sidePanel = [[name: 'sidePanel1', value: 'value1']]
        User user = new User(fullName: 'Signal', lastToLastLogin: new Date(), preference: new Preference(timeZone: 'UTC'))
        DashboardService mockDashboardService = Mock(DashboardService)
        UserService mockUserService = Mock(UserService)
        mockDashboardService.getApplicationInformation('PVS') >> pvsAppInfo
        mockDashboardService.getApplicationInformation('PVR') >> pvrAppInfo
        mockDashboardService.getApplicationInformation('PVAdmin') >> pvAdminAppInfo
        mockDashboardService.getSettingsData(_) >> { arg -> arg << settings }
        mockDashboardService.getSidePanelData(_) >> { arg -> arg << sidePanel }
        mockUserService.getUserFromCacheByUsername(_) >> user
        mockUserService.getCurrentUserName() >> 'Signal'

        when:
        controller.fetchTopNavAndSidePanelDetails()

        then:
        def jsonResponse = response.json

        jsonResponse.result.header.applications.current == pvsAppInfo
        jsonResponse.result.header.applications.installed.size() == 2
        jsonResponse.result.header.applications.installed.contains(pvrAppInfo)
        jsonResponse.result.header.applications.installed.contains(pvAdminAppInfo)
        jsonResponse.result.header.username == 'Signal'
        jsonResponse.result.header.lastLoggedIn == user.lastToLastLogin
        jsonResponse.result.header.userTimeZone == 'UTC'
        jsonResponse.result.header.setting == settings
        jsonResponse.result.sidePanel == sidePanel
    }

}

