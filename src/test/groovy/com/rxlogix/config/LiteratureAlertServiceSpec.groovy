package com.rxlogix.config

import com.rxlogix.AlertService
import com.rxlogix.DataTableSearchRequest
import com.rxlogix.UserService
import com.rxlogix.dto.AlertReviewDTO
import com.rxlogix.enums.GroupType
import com.rxlogix.util.DateUtil
import com.rxlogix.util.SignalQueryHelper
import com.rxlogix.util.ViewHelper
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.user.UserGroupMapping
import grails.testing.services.ServiceUnitTest
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.slf4j.Logger
import spock.lang.Shared
import spock.lang.Specification

class LiteratureAlertServiceSpec extends Specification implements ServiceUnitTest<LiteratureAlertService> {
    User user
    Group group

    Disposition disposition
    Disposition defaultDisposition
    Disposition autoRouteDisposition
    ExecutedLiteratureConfiguration executedLiteratureConfiguration

    def userService = Mock(UserService)
    def dateUtil = Mock(DateUtil)
    def viewHelper = Mock(ViewHelper)
    def literatureConfiguration = Mock(LiteratureConfiguration)
    def alertService = new LiteratureAlertService()
    @Shared
    Session session
    SignalQueryHelper signalQueryHelper
    Logger logger

    void setup() {
        logger = Mock(Logger)
        session = Mock(Session)
        signalQueryHelper = Mock(SignalQueryHelper)
        AlertService alertService = Mock(AlertService)
        service.alertService = alertService
        alertService.userService = userService
        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "vs")
        disposition.save(failOnError: true)

        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")
        [defaultDisposition, autoRouteDisposition].collect { it.save(failOnError: true) }

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
                isLatest: true, selectedDatasource: "pubmed", configId: 1L)
        executedLiteratureConfiguration.save(failOnError: true)
    }

    void "getCriteriaList"() {
        given:
        Map params = [articleId: "12", title: "dev tested"]
        userService.getCurrentUserPreference() >> [timeZone: "GMT"]
        userService.getUser() >> user
        userService.getGmtOffset("GMT") >> "+0000"
        dateUtil.toDateString(_, _) >> "2024-07-10"
        dateUtil.stringFromDate(_, _, _) >> "2024-07-10 10:00 AM"
        viewHelper.getDictionaryValues(_, _) >> "Test Product"

        when:
        def result1 = alertService.getCriteriaList(params, executedLiteratureConfiguration, true)
        def result2 = alertService.getCriteriaList(params,executedLiteratureConfiguration, false)
        then:
        // the below works because the result from getCriteriaList from activity screen does not return column level filter
        (result1.size() - result2.size()) == 1

        result1[result1.size()-1] == [label:'Column Level Filter', value:'Article ID : 12\nTitle : dev tested\n']
    }

    def "test prepareLiteratureAlertHQL with different filters and order columns"() {
        given:
        Map filterMap = [
                'alertName'      : 'test_alert',
                'articleId'      : '12345',
                'title'          : 'Test Title',
                'authors'        : 'John Doe',
                'publicationDate': '2023',
                'disposition'    : 'Published',
                'assignedTo'     : 'John Group',
                'productName'    : 'Product_1',
                'eventName'      : 'Event_1',
                'signal'         : 'Signal_1'
        ]
        Map orderColumnMap = [name: 'alertName', dir: 'asc']
        Boolean isDispFilters = true
        Map queryParameters = [:]
        def domainName = LiteratureAlert
        userService.getUser() >> user
        alertService.userService = userService

        when:
        String resultQuery = alertService.prepareLiteratureAlertHQL(filterMap, orderColumnMap, isDispFilters, queryParameters, domainName)

        then:
        resultQuery.contains("SELECT lsa FROM LiteratureAlert lsa")
    }

    void "test insertLitAlertActions"() {
        given:
        Long executedConfig = 1L
        Long prevExecConfigId = 2L
        String sqlStatement = "SELECT col1, col2 FROM some_table WHERE executed_config = :executedConfig AND prev_exec_config_id = :prevExecConfigId"
        List<Map<String, String>> alertIdAndActionIdList = [
                [col1: "1", col2: "2", col3: '1'],
                [col1: "3", col2: "4", col3: '1']
        ]
        String insertActionSingleAlertQuery = "INSERT INTO LIT_ALERT_ACTIONS(LITERATURE_ALERT_ID, ACTION_ID, IS_RETAINED) VALUES(?, ?, ?)"
        String actionCountSql = "UPDATE LITERATURE_ALERT SET action_count = action_count + 1 WHERE executed_config = :executedConfig"

        and:
        signalQueryHelper.lit_alert_actions(_, _ ) >> sqlStatement

        SQLQuery queryMock = Mock(SQLQuery)
        session.createSQLQuery(_) >> queryMock
        queryMock.list() >> [
                [1, 2],
                [3, 4]
        ]

        signalQueryHelper.lit_alert_actions_count(_) >> actionCountSql

        SQLQuery countQueryMock = Mock(SQLQuery)
        session.createSQLQuery(actionCountSql) >> countQueryMock
        countQueryMock.executeUpdate() >> 1

        when:
        service.insertLitAlertActions(session, executedConfig, prevExecConfigId)

        then:
        1 * service.alertService.batchPersistForStringParameters(session, alertIdAndActionIdList, insertActionSingleAlertQuery, 3)
        1 * session.createSQLQuery(actionCountSql).executeUpdate()
    }
}