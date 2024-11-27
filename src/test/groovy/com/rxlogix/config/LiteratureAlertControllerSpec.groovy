package com.rxlogix.config

import com.rxlogix.DataTableSearchRequest
import com.rxlogix.UserService
import com.rxlogix.dto.AlertReviewDTO
import com.rxlogix.enums.GroupType
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.user.UserGroupMapping
import com.rxlogix.util.DateUtil
import grails.converters.JSON
import grails.testing.web.controllers.ControllerUnitTest
import org.joda.time.DateTimeZone
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Ignore

@Ignore
class LiteratureAlertControllerSpec extends Specification implements ControllerUnitTest<LiteratureAlertController> {

    User user
    Group group
    Map resultMap
    Disposition disposition
    List literatureExeConfig
    Disposition defaultDisposition
    Disposition autoRouteDisposition
    AlertReviewDTO alertReviewDTO
    DataTableSearchRequest searchRequest
    LiteratureConfiguration configuration
    ExecutedLiteratureConfiguration executedLiteratureConfiguration
    LiteratureAlert literatureAlert
    Priority priority

    void setup() {

        // Prepare the mock dispositions
        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "vs")
        disposition.save(failOnError: true)

        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")

        [defaultDisposition, autoRouteDisposition].collect { it.save(failOnError: true) }

        // Prepare the mock Group
        group = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP, defaultDisposition: defaultDisposition,
                defaultSignalDisposition: disposition, autoRouteDisposition: autoRouteDisposition, justificationText: "Update Disposition",
                forceJustification: true, defaultQualiDisposition: disposition, defaultQuantDisposition: disposition,
                defaultAdhocDisposition: disposition, defaultEvdasDisposition: disposition, defaultLitDisposition: disposition,
                createdBy: "ujjwal", modifiedBy: "ujjwal")
        group.save(validate: false)

        Group wfGroup = new Group(name: "Default", createdBy: "ujjwal", modifiedBy: "ujjwal", groupType: GroupType.WORKFLOW_GROUP,
                defaultQualiDisposition: disposition, defaultQuantDisposition: disposition, defaultAdhocDisposition: disposition,
                defaultEvdasDisposition: disposition, defaultLitDisposition: disposition, defaultSignalDisposition: disposition,
                autoRouteDisposition: autoRouteDisposition, justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(flush: true)

        // Prepare the mock user
        user = new User(id: 1L, username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.addToGroups(group)
        UserGroupMapping userGroupMapping = new UserGroupMapping(user: user, group: wfGroup)
        userGroupMapping.save(flush: true, failOnError: true)
        user.save(validate: false)

        priority = new Priority(value: "Low", display: true, displayName: "Low", reviewPeriod: 3, priorityOrder: 1)

        //Prepare the mock configuration
        configuration = new LiteratureConfiguration(id: 1L, name: "test",
                createdBy: user.username, modifiedBy: user.username, numOfExecutions: 0,
                assignedTo: user)

        //Prepare the mock Executed Literature Configuration
        executedLiteratureConfiguration = new ExecutedLiteratureConfiguration(id: 1L, name: "test",
                owner: user, assignedTo: user,
                scheduleDateJSON: "{}", nextRunDate: new Date(),
                description: "test", dateCreated: new Date(),
                lastUpdated: new Date(), isPublic: true,
                isDeleted: true, isEnabled: true,
                productSelection: "['testproduct2']", eventSelection: "['rash']",
                searchString: "['test']", createdBy: user.username,
                modifiedBy: user.username, workflowGroup: group,
                assignedToGroup: group, totalExecutionTime: 0,
                isLatest: true, selectedDatasource: "pubmed" , configId: 1l)
        executedLiteratureConfiguration.save(failOnError: true)

        alertReviewDTO = new AlertReviewDTO()
        alertReviewDTO.workflowGrpId = 1L
        alertReviewDTO.shareWithConfigs = ["test"]

        //prepare the mock result set
        List literatureAlerts = new ArrayList();
        Map result = [
                id                : executedLiteratureConfiguration.id,
                name              : executedLiteratureConfiguration.name,
                searchString      : executedLiteratureConfiguration.searchString,
                dateRange         : "12-Dec-1993 To 12-Dec-2020",
                selectedDatasource: "Pubmed",
                dateCreated       : DateUtil.stringFromDate(new Date(), DateUtil.DATEPICKER_FORMAT, DateTimeZone.UTC.ID),
                lastUpdated       : DateUtil.stringFromDate(new Date(), DateUtil.DATEPICKER_FORMAT, DateTimeZone.UTC.ID),
                IsShareWithAccess : true
        ]
        literatureAlerts.add(result)
        resultMap = [aaData: literatureAlerts as Set, recordsTotal: 1, recordsFiltered: 1]

        literatureExeConfig = new ArrayList()
        literatureExeConfig.add(executedLiteratureConfiguration)

        def now = new Date()
        def recurrenceJSON = /{"startDateTime":"$now","recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}/

        literatureAlert = new LiteratureAlert(
                litSearchConfig: configuration,
                exLitSearchConfig: executedLiteratureConfiguration,
                dateCreated: new Date(),
                lastUpdated: new Date(),
                name: 'test',
                assignedTo: user,
                priority: priority,
                assignedToGroup: group,
                disposition: defaultDisposition,
                productSelection: 'test',
                eventSelection: 'test',
                searchString: 'Pubmed',
                articleId: 1L,
                articleTitle: 'test',
                articleAbstract: 'test',
                articleAuthors: 'test',
                scheduleDateJSON: recurrenceJSON,
                publicationDate: new Date()
        )
        literatureAlert.save(failOnError: false)

        def userServiceSpy = [
                getUser                 : { this.user },
                getUserLitConfigurations: { ["test"] },
                getCurrentUserPreference: { this.user.preference },
                getCurrentUserId        : { 1L }
        ] as UserService
        controller.literatureAlertService.userService = userServiceSpy

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

    }

    def cleanup() {
    }
    @Ignore
    void "test listByLiteratureConfiguration"() {
        given:
        def alertService = Mock(LiteratureAlertService)
        alertService.getDateRangeFromExecutedConfiguration(executedLiteratureConfiguration) >> "12-Dec-1993 To 12-Dec-2020"
        alertService.generateAlertReviewMaps(_,_,_) >> literatureExeConfig
        controller.literatureAlertService = alertService
        controller.literatureAlertService.createAlertReviewDTO() >> alertReviewDTO
        controller.literatureAlertService.generateResultMap(_,_) >> [aaData: [], recordsTotal: 0, recordsFiltered: 0]
        when:
        controller.listByLiteratureConfiguration(searchRequest)
        then:
        response.getJson()["aaData"] == []
    }
    @Ignore
    void "test listByLiteratureConfiguration if executionConfig is null"() {
        given:
        def alertService = Mock(LiteratureAlertService)
        alertService.generateAlertReviewMaps(_,_,_) >> new ExecutedLiteratureConfiguration()
        controller.literatureAlertService = alertService
        controller.literatureAlertService.createAlertReviewDTO() >> alertReviewDTO
        controller.literatureAlertService.generateResultMap(_,_) >> resultMap
        when:
        controller.listByLiteratureConfiguration(searchRequest)
        then:
        response.getJson()["aaData"][0]["name"] == "test"
    }

    void "test getSharedWithUserAndGroups"() {
        given:
        List<Map> users = [[id:user.id,name:user.fullName]]
        List<Map> groups = [[id:group.id,name: group.name]]
        Map sharedWithUser = [user:users,groups:groups,all:users+groups]
        def alertService = Mock(LiteratureAlertService)
        alertService.getShareWithUserAndGroup(_)>>sharedWithUser
        controller.literatureAlertService = alertService

        when:
        controller.getSharedWithUserAndGroups()
        then:
        response.getJson()["user"][0]["name"] == "Fake Name"
    }
    @Ignore
    void "test editShare"() {
        given:
        def alertService = Mock(LiteratureAlertService)
        alertService.editShareWith(_) >> executedLiteratureConfiguration
        when:
        controller.editShare()
        then:
        response.status == 302
    }

    void "test revertDisposition for controller"() {
        given:

        when:
        controller.revertDisposition(1,"")
        then:
        response.status == 200
    }

    void "test revertDisposition for service"() {
        given:
        def alertService = Mock(LiteratureAlertService)
        when:
        alertService.revertDisposition(1,"")
        then:
        response.status == 200
    }

    void "test changeAssignedToGroup on success"() {
        setup:
        params.isArchived = false
        LiteratureActivityService mockLiteratureActivityService = Mock(LiteratureActivityService)
        ActivityType activityType = new ActivityType(value: ActivityTypeValue.AssignedToChange)
        activityType.save(flush: true, failOnError: true)
        mockLiteratureActivityService.createLiteratureActivity(literatureAlert.exLitSearchConfig, activityType,
                user, "Assigned To changed from 'x' to 'y'", null,
                literatureAlert.productSelection, null, literatureAlert.assignedTo, literatureAlert.searchString, literatureAlert.articleId, literatureAlert.assignedToGroup) >> { }
        String selectedId = "[${literatureAlert.id}]"
        String assignedToValue = "UserGroup_${group.id}"
        when:
        controller.changeAssignedToGroup(selectedId, assignedToValue , false)
        then:
        response.status == 200
        println literatureAlert.assignedToGroup

    }

    void "test changeAssignedToGroup when exception occurs"() {
        setup:
        String selectedId = "[${literatureAlert.id}]"
        String assignedToValue = "UserGroup_${group.id}"
        when:
        controller.changeAssignedToGroup(selectedId, assignedToValue , false)
        then:
        response.status == 200
        JSON.parse(response.text).message == "app.assignedTo.changed.fail"
        JSON.parse(response.text).status == false
    }
}