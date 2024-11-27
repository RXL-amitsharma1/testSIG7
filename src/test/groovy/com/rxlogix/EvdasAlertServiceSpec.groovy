package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.dto.AlertLevelDispositionDTO
import com.rxlogix.enums.GroupType
import com.rxlogix.signal.EvdasHistory
import com.rxlogix.signal.ViewInstance
import com.rxlogix.user.Group
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import grails.testing.services.ServiceUnitTest
import com.rxlogix.user.UserGroupMapping
import com.rxlogix.util.SignalQueryHelper
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.slf4j.Logger
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */

class EvdasAlertServiceSpec extends Specification implements ServiceUnitTest<EvdasAlertService> {

    Disposition disposition
    Disposition defaultDisposition
    Disposition defaultSignalDisposition
    Disposition autoRouteDisposition
    ExecutedEvdasConfiguration executedAlertConfiguration
    EvdasAlert evdasAlert
    User user
    Priority priority
    @Shared
    ViewInstance viewInstance
    AdvancedFilter advanceFilter
    EvdasConfiguration evdasConfiguration
    @Shared
    Session session
    SignalQueryHelper signalQueryHelper
    Logger logger

    def setup() {
        logger = Mock(Logger)
        session = Mock(Session)
        signalQueryHelper = Mock(SignalQueryHelper)
        AlertService alertService = Mock(AlertService)
        service.alertService = alertService
        priority = new Priority([displayName: "mockPriority", value: "mockPriority", display: true, defaultPriority: true, reviewPeriod: 1])
        priority.save(failOnError: true,flush:true)
        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: false, reviewCompleted: true, abbreviation: 'vs')
        disposition.save(flush:true)

        defaultSignalDisposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "VO")
        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")

        [defaultDisposition, autoRouteDisposition].collect { it.save(failOnError: true) }

        Group wfGroup = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP, createdBy: 'createdBy', modifiedBy: 'modifiedBy',
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                defaultSignalDisposition: defaultDisposition,
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
        user.metaClass.getEmail = { 'fake.email@fake.com'}
        UserGroupMapping userGroupMapping = new UserGroupMapping(user: user, group: wfGroup)
        userGroupMapping.save(flush: true, failOnError: false)
        user.save(flush:true)

        executedAlertConfiguration = new ExecutedEvdasConfiguration(name: "test",
                owner: user, scheduleDateJSON: "{}", nextRunDate: new Date(),
                description: "test", dateCreated: new Date(), lastUpdated: new Date(),
                isPublic: true, isDeleted: true, isEnabled: true,
                productSelection: "aspirin", eventSelection: "['rash']",
                configSelectedTimeZone: "UTC",
                createdBy: user.username, modifiedBy: user.username,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10, configId: 1L)
        executedAlertConfiguration.save(flush:true, validate: false)

        evdasConfiguration = new EvdasConfiguration(name: "test")
        evdasConfiguration.save(validate:false)

        evdasAlert = new EvdasAlert(
                executedAlertConfiguration: executedAlertConfiguration,
                name: executedAlertConfiguration.name,
                priority: priority,
                disposition: disposition,
                assignedTo: user,
                detectedDate: executedAlertConfiguration.dateCreated,
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
                dateCreated: executedAlertConfiguration.dateCreated,
                lastUpdated: executedAlertConfiguration.dateCreated,
                dueDate: new Date(),
                periodStartDate: new DateTime(2017, 7, 1, 0, 0, 0, DateTimeZone.forID('UTC')).toDate(),
                periodEndDate: new DateTime(2017, 12, 31, 0, 0, 0, DateTimeZone.forID('UTC')).toDate(),
                adhocRun: false,
                flagged: false,
                format: "test"

        )
        evdasAlert.save(flush:true,failOnError:true)
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
        advanceFilter.save(validate:false)

        UserService userService = Mock(UserService)
        Preference preference = new Preference()
        preference.timeZone = "UTC"
        userService.getCurrentUserPreference() >> preference
        service.userService = userService

        String columnSeq = """
                {"1":{"containerView":1,"label":"Tags","name":"alertTags","listOrder":0,"seq":6},"2":{"containerView":1,"label":"Receipt Date","name":"caseInitReceiptDate","listOrder":1,"seq":7},"3":{"containerView":1,"label":"Product Name","name":"productName","listOrder":2,"seq":8},"4":{"containerView":1,"label":"PT","name":"pt","listOrder":3,"seq":9},"5":{"containerView":1,"label":"Listedness","name":"listedness","listOrder":4,"seq":10},"6":{"containerView":1,"label":"Outcome","name":"outcome","listOrder":5,"seq":11},"7":{"containerView":1,"label":"Signal / Topic","name":"signalsAndTopics","listOrder":6,"seq":12},
                "8":{"containerView":1,"label":"Disposition","name":"currentDisposition","listOrder":7,"seq":13},"9":{"containerView":1,"label":"Current Disposition","name":"disposition","listOrder":8,"seq":14},"10":{"containerView":1,"label":"Assigned To","name":"assignedToUser","listOrder":9,"seq":15},"11":{"containerView":1,"label":"Due In","name":"dueDate","listOrder":10,"seq":16},"12":{"containerView":3,"label":"Suspect Products","name":"suspProd","listOrder":9999,"seq":17},"13":{"containerView":3,"label":"Con Med","name":"conComit","listOrder":9999,"seq":18},
                "14":{"containerView":3,"label":"PT List","name":"masterPrefTermAll","listOrder":9999,"seq":19},"15":{"containerView":1,"label":"Serious","name":"serious","listOrder":14,"seq":20},"16":{"containerView":3,"label":"Report Type","name":"caseReportType","listOrder":9999,"seq":21},"17":{"containerView":3,"label":"HCP","name":"reportersHcpFlag","listOrder":9999,"seq":22},"18":{"containerView":3,"label":"Country","name":"country","listOrder":9999,"seq":23},"19":{"containerView":3,"label":"Age Group","name":"age","listOrder":9999,"seq":24},"20":{"containerView":3,"label":"Gender","name":"gender","listOrder":9999,"seq":25},
                "21":{"containerView":3,"label":"Positive Rechallenge","name":"rechallenge","listOrder":9999,"seq":26},"22":{"containerView":3,"label":"Locked Date","name":"lockedDate","listOrder":9999,"seq":27},"23":{"containerView":3,"label":"Death","name":"death","listOrder":9999,"seq":28},"24":{"containerView":3,"label":"Medication Error PTs","name":"medErrorsPt","listOrder":9999,"seq":37},"25":{"containerView":3,"label":"Age","name":"patientAge","listOrder":9999,"seq":29},"26":{"containerView":3,"label":"Case Type","name":"caseType","listOrder":9999,"seq":31},
                "27":{"containerView":3,"label":"Completeness Score","name":"completenessScore","listOrder":9999,"seq":32},"28":{"containerView":3,"label":"Primary IND#","name":"indNumber","listOrder":9999,"seq":33},"29":{"containerView":3,"label":"Application#","name":"appTypeAndNum","listOrder":9999,"seq":34},"30":{"containerView":3,"label":"Compounding Flag","name":"compoundingFlag","listOrder":9999,"seq":35},"31":{"containerView":3,"label":"Indications","name":"indications","listOrder":9999,"seq":30},"32":{"containerView":3,"label":"Medication Error PT Count","name":"medErrorPtCount","listOrder":9999,"seq":38}}
           """
        String filters = """
                {"1":"pyrexia"}
        """
        String sorting = """
                {"1":"asc"}
        """
        viewInstance = new ViewInstance(name: "viewInstance", alertType: "Single Case Alert", user: user, columnSeq: columnSeq, filters: filters, sorting: sorting)
        viewInstance.save(flush: true, failOnError: true)

        def mockViewInstanceService = Mock(ViewInstanceService)
        mockViewInstanceService.fetchSelectedViewInstance(_,_) >> {
            return viewInstance
        }
        service.viewInstanceService = mockViewInstanceService

    }

    def cleanup() {
    }

    def "test createEvdasHistoryForBulkDisposition()"() {
        given:
        Map alertMap = [:]
        AlertLevelDispositionDTO alertLevelDispositionDTO = new AlertLevelDispositionDTO(loggedInUser: user)

        when:
        EvdasHistory evdasHistory = service.createEvdasHistoryForBulkDisposition(alertMap,alertLevelDispositionDTO)

        then:
        evdasHistory.createdBy == user.fullName
    }

    def "test getColumnListForExcelExport" () {
        when:
        List columnList = service.getColumnListForExcelExport('test', 0l)
        then:
        assert columnList.size() == 12
    }
    def "test " () {
        given:
        grailsApplication.config.evdasColumnExcelExportMap = [['Test Count/Test Cum count' : 'testCount']]
        when:
        List tempCoulmnList = service.getLabelNameList('Test Count/Test Cum count', 'testCount')
        then:
        assert tempCoulmnList.size() == 2
    }
    @Ignore
    def "test changeDisposition" () {
        when:
        Map result = service.changeDisposition([evdasAlert] , defaultDisposition , "" , null , null , false,1)
        then:
        result.alertDueDateList.size() == 0
    }
    @Ignore
    def "test revertDisposition" () {
        when:
        Map result = service.revertDisposition(1,"justification")
        then:
        result.alertDueDateList.size() == 0
    }
    @Ignore
    void "test createActivityForUndoAction"(){
        when:
        Activity activity = service.createActivityForUndoAction(evdasAlert,"test justification")
        then:
        activity.id
    }
    @Ignore
    void "test undoneEvdasHistory"(){
        when:
        def result = service.undoneEvdasHistory(evdasAlert)
        then:
        !result
    }
    @Ignore
    def "getEvdasAlertCriteriaData()"(){
        Map params = [viewId:viewInstance.id as Long, advancedFilterId: advanceFilter.id as Long]
        when:
        def resultList = service.getEvdasAlertCriteriaData(executedAlertConfiguration, params)
        then:
        resultList.size() == 13
    }
    void "test saveAlertCaseHistory"(){
        when:
        service.saveAlertCaseHistory(evdasAlert, "just", "signaldev")
        then:
        evdasAlert.justification == "just"
        evdasAlert.dispPerformedBy == "signaldev"
    }

    void "test insertEvdasAlertActions"() {
        given:
        Long executedConfig = 1L
        Long prevExecConfigId = 2L
        String sqlStatement = "SELECT col1, col2 FROM some_table WHERE executed_config = :executedConfig AND prev_exec_config_id = :prevExecConfigId"
        List<Map<String, String>> alertIdAndActionIdList = [
                [col1: "1", col2: "2", col3: '1'],
                [col1: "3", col2: "4", col3: '1']
        ]
        String insertActionSingleAlertQuery = "INSERT INTO EVDAS_ALERT_ACTIONS(EVDAS_ALERT_ID, ACTION_ID, IS_RETAINED) VALUES(?, ?, ?)"
        String actionCountSql = "UPDATE EVDAS_ALERT SET action_count = action_count + 1 WHERE executed_config = :executedConfig"

        and:
        signalQueryHelper.evdas_alert_actions(_, _ ) >> sqlStatement

        SQLQuery queryMock = Mock(SQLQuery)
        session.createSQLQuery(_) >> queryMock
        queryMock.list() >> [
                [1, 2],
                [3, 4]
        ]

        signalQueryHelper.evdas_alert_actions_count(_) >> actionCountSql

        SQLQuery countQueryMock = Mock(SQLQuery)
        session.createSQLQuery(actionCountSql) >> countQueryMock
        countQueryMock.executeUpdate() >> 1

        when:
        service.insertEvdasAlertActions(session, executedConfig, prevExecConfigId)

        then:
        1 * service.alertService.batchPersistForStringParameters(session, alertIdAndActionIdList, insertActionSingleAlertQuery, 3)
        1 * session.createSQLQuery(actionCountSql).executeUpdate()
    }
}