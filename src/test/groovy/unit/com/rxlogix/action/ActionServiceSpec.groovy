package unit.com.rxlogix.action

import com.rxlogix.Constants
import com.rxlogix.DataTableSearchRequest
import com.rxlogix.InboxLog
import com.rxlogix.UserGroupService
import com.rxlogix.UserService
import com.rxlogix.ValidatedSignalService
import com.rxlogix.action.ActionService
import com.rxlogix.config.*
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.helper.NotificationHelper
import com.rxlogix.signal.AdHocAlert
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.Group
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import grails.testing.services.ServiceUnitTest
import grails.web.mapping.LinkGenerator
import com.rxlogix.signal.Alert
import com.rxlogix.config.Priority
import com.rxlogix.ActivityService
import org.grails.datastore.gorm.bootstrap.support.InstanceFactoryBean
import org.joda.time.DateTime
import spock.lang.Ignore
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges
import org.joda.time.DateTime
import com.rxlogix.config.ActionType
import com.rxlogix.config.ActionConfiguration
import com.rxlogix.AlertService
import com.rxlogix.signal.*
import com.rxlogix.util.DateUtil
import com.rxlogix.config.PVSState
import com.rxlogix.signal.Topic
import com.rxlogix.SignalDataSourceService

import javax.sql.DataSource

@ConfineMetaClassChanges([ActionService])
class ActionServiceSpec extends Specification implements ServiceUnitTest<ActionService> {
    User user1
    User user2
    User user3
    Group wfGroup
    Disposition disposition
    Disposition defaultDisposition
    Disposition autoRouteDisposition
    def sqlResult1
    def sqlResult2
    def sqlResult3
    def sqlResult4
    ActionType actionType
    ActionConfiguration actionConfiguration
    Preference preference
    def userGroupService
    NotificationHelper notificationHelper
    ActionService service // Service under test

    static doWithSpring = {
        dataSource(InstanceFactoryBean, [:] as DataSource, DataSource)
    }

    def setup() {


        userGroupService = Mock(UserGroupService)
        notificationHelper = Mock(NotificationHelper)
        service = new ActionService(
                userGroupService: userGroupService,
                notificationHelper: notificationHelper
        )

        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "VO")
        disposition.save(failOnError: true)

        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")

        wfGroup = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP,
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                defaultSignalDisposition: disposition,
                autoRouteDisposition: autoRouteDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(validate: false)

        //Save the  user
        user1 = new User(id: '1', username: 'test_user1', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user1.addToGroups(wfGroup)
        user1.preference.createdBy = "createdBy"
        user1.preference.modifiedBy = "modifiedBy"
        user1.preference.locale = new Locale("en")
        user1.preference.isEmailEnabled = false
        user1.metaClass.getFullName = { 'Fake Name1' }
        user1.metaClass.getEmail = { 'fake.email1@fake.com' }
        user1.save(flush: true)

        user2 = new User(id: '2', username: 'test_user2', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user2.addToGroups(wfGroup)
        user2.preference.createdBy = "createdBy"
        user2.preference.modifiedBy = "modifiedBy"
        user2.preference.locale = new Locale("en")
        user2.preference.isEmailEnabled = false
        user2.metaClass.getFullName = { 'Fake Name2' }
        user2.metaClass.getEmail = { 'fake.email2@fake.com' }
        user2.save(flush: true)

        user3 = new User(id: '3', username: 'test_user3', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user3.addToGroups(wfGroup)
        user3.preference.createdBy = "createdBy"
        user3.preference.modifiedBy = "modifiedBy"
        user3.preference.locale = new Locale("en")
        user3.preference.isEmailEnabled = false
        user3.metaClass.getFullName = { 'Fake Name3' }
        user3.metaClass.getEmail = { 'fake.email3@fake.com' }
        user3.save(flush: true)

        def mockedSource = Mock(DataSource)

        sqlResult1 = """SELECT acth.id as id,aconfg.DISPLAY_NAME as config,atype.DISPLAY_NAME as type ,acth.details as details,
                       COALESCE(sca.name, asca.name, agg.name, aagg.name, lalert.name, alalert.name, ealert.name, aealert.name, adhocAlert.name,vsignal.name) as alertname ,
                       COALESCE(sca.case_number, asca.case_number, agg.pt, aagg.pt, dbms_lob.substr(lalert.article_title,dbms_lob.getlength(lalert.article_title),1), dbms_lob.substr(alalert.article_title,dbms_lob.getlength(alalert.article_title),1), ealert.pt, aealert.pt, vsignal.name,adhocAlert.name ) as entity ,
                       COALESCE(sca.id, asca.id, agg.id, aagg.id, lalert.article_id, alalert.article_id, ealert.id, aealert.id, adhocAlert.id,vsignal.id ) as ALERTID ,
                       COALESCE(sca.follow_up_number, asca.follow_up_number) as followUpNumber,
                       COALESCE(sca.case_Version, asca.case_Version) as CASEVERSION,
                       acth.DUE_DATE as dueDate ,acth.ACTION_STATUS as actionStatus,acth.ALERT_TYPE as alertType,acth.COMMENTS as comments,
                       case when acth.ACTION_STATUS='Closed' then acth.COMPLETED_DATE else acth.COMPLETED_DATE end as completedDate FROM ACTIONS acth
                       LEFT JOIN  ACTION_CONFIGURATIONS aconfg on acth.config_id = aconfg.id
                       LEFT JOIN  ACTION_TYPES atype on acth.type_id = atype.id
                       LEFT JOIN  SINGLE_ALERT_ACTIONS sacth on acth.id = sacth.action_id
                       LEFT JOIN  ARCHIVED_SCA_ACTIONS asacth on acth.id = asacth.action_id
                       LEFT JOIN  AGG_ALERT_ACTIONS aacth on acth.id = aacth.action_id
                       LEFT JOIN  ARCHIVED_ACA_ACTIONS aaacth on acth.id = aaacth.action_id
                       LEFT JOIN  LIT_ALERT_ACTIONS lacth on acth.id = lacth.action_id 
                       LEFT JOIN  ARCHIVED_LIT_ALERT_ACTIONS alacth on acth.id = alacth.action_id
                       LEFT JOIN  EVDAS_ALERT_ACTIONS eacth on acth.id = eacth.action_id
                       LEFT JOIN  ARCHIVED_EVDAS_ALERT_ACTIONS aeacth on acth.id = aeacth.action_id
                       LEFT JOIN  ADHOC_ALERT_ACTIONS ahacth on acth.id = ahacth.action_id
                       LEFT JOIN  VALIDATED_SIGNAL_ACTIONS vsth on acth.id = vsth.action_id
                       LEFT JOIN  SINGLE_CASE_ALERT sca on sacth.SINGLE_CASE_ALERT_ID = sca.id
                       LEFT JOIN  ARCHIVED_SINGLE_CASE_ALERT asca on asacth.ARCHIVED_SCA_ID = asca.id
                       LEFT JOIN  AGG_ALERT agg on aacth.AGG_ALERT_ID = agg.id
                       LEFT JOIN  ARCHIVED_AGG_ALERT aagg on aaacth.ARCHIVED_ACA_ID = aagg.id
                       LEFT JOIN  LITERATURE_ALERT lalert on lacth.LITERATURE_ALERT_ID = lalert.id
                       LEFT JOIN  ARCHIVED_LITERATURE_ALERT alalert on alacth.ARCHIVED_LIT_ALERT_ID = alalert.id
                       LEFT JOIN  ALERTS adhocAlert on ahacth.ALERT_ID = adhocAlert.id
                       LEFT JOIN  VALIDATED_SIGNAL vsignal on vsth.VALIDATED_SIGNAL_ACTIONS_ID = vsignal.id
                       LEFT JOIN  EVDAS_ALERT ealert on eacth.EVDAS_ALERT_ID = ealert.id
                       LEFT JOIN  ARCHIVED_EVDAS_ALERT aealert on aeacth.ARCHIVED_EVDAS_ALERT_ID = aealert.id WHERE (acth.ASSIGNED_TO_ID = 1 OR acth.ASSIGNED_TO_GROUP_ID IN (1) )  AND acth.ACTION_STATUS not in ('Closed','Deleted')  order by null null"""

        sqlResult2 = """SELECT acth.id as id,aconfg.DISPLAY_NAME as config,atype.DISPLAY_NAME as type ,acth.details as details,
                       COALESCE(sca.name, asca.name, agg.name, aagg.name, lalert.name, alalert.name, ealert.name, aealert.name, adhocAlert.name,vsignal.name) as alertname ,
                       COALESCE(sca.case_number, asca.case_number, agg.pt, aagg.pt, dbms_lob.substr(lalert.article_title,dbms_lob.getlength(lalert.article_title),1), dbms_lob.substr(alalert.article_title,dbms_lob.getlength(alalert.article_title),1), ealert.pt, aealert.pt, vsignal.name,adhocAlert.name ) as entity ,
                       COALESCE(sca.id, asca.id, agg.id, aagg.id, lalert.article_id, alalert.article_id, ealert.id, aealert.id, adhocAlert.id,vsignal.id ) as ALERTID ,
                       COALESCE(sca.follow_up_number, asca.follow_up_number) as followUpNumber,
                       COALESCE(sca.case_Version, asca.case_Version) as CASEVERSION,
                       acth.DUE_DATE as dueDate ,acth.ACTION_STATUS as actionStatus,acth.ALERT_TYPE as alertType,acth.COMMENTS as comments,
                       case when acth.ACTION_STATUS='Closed' then acth.COMPLETED_DATE else acth.COMPLETED_DATE end as completedDate FROM ACTIONS acth
                       LEFT JOIN  ACTION_CONFIGURATIONS aconfg on acth.config_id = aconfg.id
                       LEFT JOIN  ACTION_TYPES atype on acth.type_id = atype.id
                       LEFT JOIN  SINGLE_ALERT_ACTIONS sacth on acth.id = sacth.action_id
                       LEFT JOIN  ARCHIVED_SCA_ACTIONS asacth on acth.id = asacth.action_id
                       LEFT JOIN  AGG_ALERT_ACTIONS aacth on acth.id = aacth.action_id
                       LEFT JOIN  ARCHIVED_ACA_ACTIONS aaacth on acth.id = aaacth.action_id
                       LEFT JOIN  LIT_ALERT_ACTIONS lacth on acth.id = lacth.action_id 
                       LEFT JOIN  ARCHIVED_LIT_ALERT_ACTIONS alacth on acth.id = alacth.action_id
                       LEFT JOIN  EVDAS_ALERT_ACTIONS eacth on acth.id = eacth.action_id
                       LEFT JOIN  ARCHIVED_EVDAS_ALERT_ACTIONS aeacth on acth.id = aeacth.action_id
                       LEFT JOIN  ADHOC_ALERT_ACTIONS ahacth on acth.id = ahacth.action_id
                       LEFT JOIN  VALIDATED_SIGNAL_ACTIONS vsth on acth.id = vsth.action_id
                       LEFT JOIN  SINGLE_CASE_ALERT sca on sacth.SINGLE_CASE_ALERT_ID = sca.id
                       LEFT JOIN  ARCHIVED_SINGLE_CASE_ALERT asca on asacth.ARCHIVED_SCA_ID = asca.id
                       LEFT JOIN  AGG_ALERT agg on aacth.AGG_ALERT_ID = agg.id
                       LEFT JOIN  ARCHIVED_AGG_ALERT aagg on aaacth.ARCHIVED_ACA_ID = aagg.id
                       LEFT JOIN  LITERATURE_ALERT lalert on lacth.LITERATURE_ALERT_ID = lalert.id
                       LEFT JOIN  ARCHIVED_LITERATURE_ALERT alalert on alacth.ARCHIVED_LIT_ALERT_ID = alalert.id
                       LEFT JOIN  ALERTS adhocAlert on ahacth.ALERT_ID = adhocAlert.id
                       LEFT JOIN  VALIDATED_SIGNAL vsignal on vsth.VALIDATED_SIGNAL_ACTIONS_ID = vsignal.id
                       LEFT JOIN  EVDAS_ALERT ealert on eacth.EVDAS_ALERT_ID = ealert.id
                       LEFT JOIN  ARCHIVED_EVDAS_ALERT aealert on aeacth.ARCHIVED_EVDAS_ALERT_ID = aealert.id WHERE (acth.ASSIGNED_TO_ID = 2 OR acth.ASSIGNED_TO_GROUP_ID IN (1) OR acth.OWNER_ID = 1 )  order by null null"""

        sqlResult3 = """SELECT acth.id as id,aconfg.DISPLAY_NAME as config,atype.DISPLAY_NAME as type ,acth.details as details,
                       COALESCE(sca.name, asca.name, agg.name, aagg.name, lalert.name, alalert.name, ealert.name, aealert.name, adhocAlert.name,vsignal.name) as alertname ,
                       COALESCE(sca.case_number, asca.case_number, agg.pt, aagg.pt, dbms_lob.substr(lalert.article_title,dbms_lob.getlength(lalert.article_title),1), dbms_lob.substr(alalert.article_title,dbms_lob.getlength(alalert.article_title),1), ealert.pt, aealert.pt, vsignal.name,adhocAlert.name ) as entity ,
                       COALESCE(sca.id, asca.id, agg.id, aagg.id, lalert.article_id, alalert.article_id, ealert.id, aealert.id, adhocAlert.id,vsignal.id ) as ALERTID ,
                       COALESCE(sca.follow_up_number, asca.follow_up_number) as followUpNumber,
                       COALESCE(sca.case_Version, asca.case_Version) as CASEVERSION,
                       acth.DUE_DATE as dueDate ,acth.ACTION_STATUS as actionStatus,acth.ALERT_TYPE as alertType,acth.COMMENTS as comments,
                       case when acth.ACTION_STATUS='Closed' then acth.COMPLETED_DATE else acth.COMPLETED_DATE end as completedDate FROM ACTIONS acth
                       LEFT JOIN  ACTION_CONFIGURATIONS aconfg on acth.config_id = aconfg.id
                       LEFT JOIN  ACTION_TYPES atype on acth.type_id = atype.id
                       LEFT JOIN  SINGLE_ALERT_ACTIONS sacth on acth.id = sacth.action_id
                       LEFT JOIN  ARCHIVED_SCA_ACTIONS asacth on acth.id = asacth.action_id
                       LEFT JOIN  AGG_ALERT_ACTIONS aacth on acth.id = aacth.action_id
                       LEFT JOIN  ARCHIVED_ACA_ACTIONS aaacth on acth.id = aaacth.action_id
                       LEFT JOIN  LIT_ALERT_ACTIONS lacth on acth.id = lacth.action_id 
                       LEFT JOIN  ARCHIVED_LIT_ALERT_ACTIONS alacth on acth.id = alacth.action_id
                       LEFT JOIN  EVDAS_ALERT_ACTIONS eacth on acth.id = eacth.action_id
                       LEFT JOIN  ARCHIVED_EVDAS_ALERT_ACTIONS aeacth on acth.id = aeacth.action_id
                       LEFT JOIN  ADHOC_ALERT_ACTIONS ahacth on acth.id = ahacth.action_id
                       LEFT JOIN  VALIDATED_SIGNAL_ACTIONS vsth on acth.id = vsth.action_id
                       LEFT JOIN  SINGLE_CASE_ALERT sca on sacth.SINGLE_CASE_ALERT_ID = sca.id
                       LEFT JOIN  ARCHIVED_SINGLE_CASE_ALERT asca on asacth.ARCHIVED_SCA_ID = asca.id
                       LEFT JOIN  AGG_ALERT agg on aacth.AGG_ALERT_ID = agg.id
                       LEFT JOIN  ARCHIVED_AGG_ALERT aagg on aaacth.ARCHIVED_ACA_ID = aagg.id
                       LEFT JOIN  LITERATURE_ALERT lalert on lacth.LITERATURE_ALERT_ID = lalert.id
                       LEFT JOIN  ARCHIVED_LITERATURE_ALERT alalert on alacth.ARCHIVED_LIT_ALERT_ID = alalert.id
                       LEFT JOIN  ALERTS adhocAlert on ahacth.ALERT_ID = adhocAlert.id
                       LEFT JOIN  VALIDATED_SIGNAL vsignal on vsth.VALIDATED_SIGNAL_ACTIONS_ID = vsignal.id
                       LEFT JOIN  EVDAS_ALERT ealert on eacth.EVDAS_ALERT_ID = ealert.id
                       LEFT JOIN  ARCHIVED_EVDAS_ALERT aealert on aeacth.ARCHIVED_EVDAS_ALERT_ID = aealert.id order by null null"""


        actionType = new ActionType(description: 'test', displayName: 'test', value: 'test')
        actionConfiguration = new ActionConfiguration(displayName: 'test', value: 'test')
        preference = new Preference(locale: Locale.ENGLISH,timeZone: "UTC",createdBy: "USER1",modifiedBy: "USER2")

    }

    void "test createActionDTO()"() {
        given:
        Action action = new Action()
        action.dueDate = new DateTime(2020, 4, 15, 0, 0, 0).toDate()
        action.alertType = "alertType"
        action.completedDate = new DateTime(2021, 4, 15, 0, 0, 0).toDate()
        action.metaClass.toDTO = { [alertType: "alertType"] }
        service.grailsLinkGenerator = Mock(LinkGenerator)
        SingleCaseAlert singleCaseAlert = new SingleCaseAlert()
        singleCaseAlert.id = 1L
        singleCaseAlert.name = "singleCaseAlert"
        singleCaseAlert.caseNumber = "caseNumber"
        singleCaseAlert.followUpNumber = 1
        singleCaseAlert.caseVersion = 1
        singleCaseAlert.executedAlertConfiguration = new ExecutedConfiguration()
        singleCaseAlert.executedAlertConfiguration.id = 1L

        ArchivedSingleCaseAlert archivedSingleCaseAlert = new ArchivedSingleCaseAlert()
        archivedSingleCaseAlert.id = 1L
        archivedSingleCaseAlert.name = "singleCaseAlert"
        archivedSingleCaseAlert.caseNumber = "caseNumber"
        archivedSingleCaseAlert.followUpNumber = 1
        archivedSingleCaseAlert.caseVersion = 1
        archivedSingleCaseAlert.executedAlertConfiguration = new ExecutedConfiguration()
        archivedSingleCaseAlert.executedAlertConfiguration.id = 1L

        AggregateCaseAlert aggregateCaseAlert = new AggregateCaseAlert()
        aggregateCaseAlert.name = "aggregateCaseAlert"
        aggregateCaseAlert.executedAlertConfiguration = new ExecutedConfiguration()
        aggregateCaseAlert.executedAlertConfiguration.id = 1L
        aggregateCaseAlert.productName = "productName"
        aggregateCaseAlert.soc = "soc"
        aggregateCaseAlert.pt = "pt"
        aggregateCaseAlert.id = 1L

        ArchivedAggregateCaseAlert archivedAggregateCaseAlert = new ArchivedAggregateCaseAlert()
        archivedAggregateCaseAlert.name = "aggregateCaseAlert"
        archivedAggregateCaseAlert.executedAlertConfiguration = new ExecutedConfiguration()
        archivedAggregateCaseAlert.executedAlertConfiguration.id = 1L
        archivedAggregateCaseAlert.productName = "productName"
        archivedAggregateCaseAlert.soc = "soc"
        archivedAggregateCaseAlert.pt = "pt"
        archivedAggregateCaseAlert.id = 1L

        EvdasAlert evdasAlert = new EvdasAlert()
        evdasAlert.id = 1L
        evdasAlert.name = "evdasAlert"
        evdasAlert.executedAlertConfiguration = new ExecutedEvdasConfiguration()
        evdasAlert.executedAlertConfiguration.id = 1L
        evdasAlert.substance = "productName"
        evdasAlert.soc = "soc"
        evdasAlert.pt = "pt"

        ArchivedEvdasAlert archivedEvdasAlert = new ArchivedEvdasAlert()
        archivedEvdasAlert.id = 1L
        archivedEvdasAlert.name = "evdasAlert"
        archivedEvdasAlert.executedAlertConfiguration = new ExecutedEvdasConfiguration()
        archivedEvdasAlert.executedAlertConfiguration.id = 1L
        archivedEvdasAlert.substance = "productName"
        archivedEvdasAlert.soc = "soc"
        archivedEvdasAlert.pt = "pt"

        LiteratureAlert literatureAlert = new LiteratureAlert()
        literatureAlert.name = "literatureAlert"
        literatureAlert.articleTitle = "articleTitle"

        ArchivedLiteratureAlert archivedLiteratureAlert = new ArchivedLiteratureAlert()
        archivedLiteratureAlert.name = "literatureAlert"
        archivedLiteratureAlert.articleTitle = "articleTitle"

        AdHocAlert adHocAlert = new AdHocAlert()
        adHocAlert.name = "AdHocAlert"
        adHocAlert.id = 1L

        ExecutedConfiguration executedConfiguration = new ExecutedConfiguration(
                id: 100002,
                name: "case_series_config",
                executedAlertDateRangeInformation: "{}",
                owner: user1,
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
                createdBy: user1.username,
                modifiedBy: user1.username,
                assignedTo: user1,
                executionStatus: ReportExecutionStatus.COMPLETED,
                numOfExecutions: 10,
                isCaseSeries: true,
                aggExecutionId: 200002,
                aggAlertId: 200003,
                autoShareWithUser: [user1],
                configId: 2,
                aggCountType: "NEW_SER")
        executedConfiguration.save(flush: true)

        ValidatedSignal signal = new ValidatedSignal()
        signal.name = "signal"
        signal.id = 1L
        signal.events = "{\"1\":[{\"name\":\"Blood and lymphatic system disorders\",\"id\":\"10005329\"}],\"2\":[],\"3\":[],\"4\":[],\"5\":[],\"6\":[]}"
        signal.products = "{\"1\":[],\"2\":[],\"3\":[{\"name\":\"ALL-LIC-PROD\",\"id\":\"100016\"}],\"4\":[]}"

        ExecutedConfiguration.metaClass.static.findById = { id ->
            return executedConfiguration
        }

        def current
        service.metaClass.getAssociation = { Action a -> current }

        def mockUserService = Mock(UserService)
        mockUserService.getUser() >> user1
        service.userService = mockUserService
        user1.metaClass.isSingleReviewer = { true }

        user1.metaClass.isAggregateReviewer = { true }

        mockUserService.getCurrentUserPreference() >> preference
        action.grailsLinkGenerator = Mock(LinkGenerator)
        action.grailsLinkGenerator.link(_) >> "foo"

        def mockValidatedSignalService = Mock(ValidatedSignalService)
        service.validatedSignalService = mockValidatedSignalService
        mockValidatedSignalService.getProductNameList() >> ["ALL-LIC-PROD"]

        when: "associated with Qualitative alert"
        current = singleCaseAlert
        Map result = service.createActionDTO(action)

        then:
        result == [id                    : null,
                   createdDate           : null,
                   dueDate               : '15-Apr-2020',
                   completedDate         : '15-Apr-2021',
                   assignedTo            : null,
                   assignedToObj         : [id: null, fullName: null],
                   assignedToUser        : null,
                   owner                 : null,
                   searchUserGroupListUrl: 'foo',
                   comments              : null,
                   alertType             : 'alertType',
                   type                  : null,
                   typeObj               : null,
                   config                : null,
                   configObj             : null,
                   viewed                : false,
                   details               : null,
                   actionStatus          : null,
                   meetingId             : null,
                   configName            : 'singleCaseAlert',
                   configUrl             : 'null/singleCaseAlert/details?configId=1&callingScreen=review&isArchived=false&isCaseSeries=false&productName=null&eventName=null',
                   caseNumber            : 'caseNumber',
                   caseNumberUrl         : 'null/caseInfo/caseDetail?caseNumber=caseNumber&version=1&followUpNumber=1&isArchived=false&alertId=1&isFaers=false&isSingleAlertScreen=true&isCaseSeries=false',
                   hasAccess             : true
        ]
        when: "associated with archived Qualtitative alert"
        current = archivedSingleCaseAlert
        result = service.createActionDTO(action)

        then:
        result == [id                    : null,
                   createdDate           : null,
                   dueDate               : '15-Apr-2020',
                   completedDate         : '15-Apr-2021',
                   assignedTo            : null,
                   assignedToObj         : [id: null, fullName: null],
                   assignedToUser        : null,
                   owner                 : null,
                   searchUserGroupListUrl: 'foo',
                   comments              : null,
                   alertType             : 'alertType',
                   type                  : null,
                   typeObj               : null,
                   config                : null,
                   configObj             : null,
                   viewed                : false,
                   details               : null,
                   actionStatus          : null,
                   meetingId             : null,
                   configName            : 'singleCaseAlert',
                   configUrl             : 'null/singleCaseAlert/details?configId=1&callingScreen=review&isArchived=true&isCaseSeries=false&productName=null&eventName=null',
                   caseNumber            : 'caseNumber',
                   caseNumberUrl         : 'null/caseInfo/caseDetail?caseNumber=caseNumber&version=1&followUpNumber=1&isArchived=true&alertId=1&isFaers=false&isSingleAlertScreen=true&isCaseSeries=false',
                   hasAccess: true
        ]


        when: "associated with Quantitative alert"

        current = aggregateCaseAlert
        result = service.createActionDTO(action)

        then:
        result == [id            : null, createdDate: null, dueDate: '15-Apr-2020', completedDate: '15-Apr-2021', assignedTo: null,
                   assignedToObj : [id: null, fullName: null],
                   assignedToUser: null, owner: null, searchUserGroupListUrl: 'foo',
                   comments      : null, alertType: 'alertType', type: null, typeObj: null, config: null, configObj: null, viewed: false,
                   details       : null, actionStatus: null, meetingId: null, configName: 'aggregateCaseAlert',
                   configUrl: 'null/aggregateCaseAlert/details?configId=1&callingScreen=review&archived=false',
                   productName   : 'productName', soc: 'soc', pt: 'pt',
                   ptUrl: 'null/eventInfo/eventDetail?alertId=1&type=alertType&isArchived=false&isAlertScreen=true',
                   hasAccess: true]

        when: "associated with archived Quantitative alert"

        current = archivedAggregateCaseAlert
        result = service.createActionDTO(action)

        then:
        result == [id            : null, createdDate: null, dueDate: '15-Apr-2020', completedDate: '15-Apr-2021', assignedTo: null,
                   assignedToObj : [id: null, fullName: null],
                   assignedToUser: null, owner: null, searchUserGroupListUrl: 'foo',
                   comments      : null, alertType: 'alertType', type: null, typeObj: null, config: null, configObj: null, viewed: false,
                   details       : null, actionStatus: null, meetingId: null, configName: 'aggregateCaseAlert',
                   configUrl     : 'null/aggregateCaseAlert/details?configId=1&callingScreen=review&archived=true',
                   productName   : 'productName', soc: 'soc', pt: 'pt',
                   ptUrl         : 'null/eventInfo/eventDetail?alertId=1&type=alertType&isArchived=true&isAlertScreen=true',
                   hasAccess     : true]


//        when: "associated with Evdas alert"
//        current = evdasAlert
//        result = service.createActionDTO(action)
//
//        then:
//        result == [id            : null, createdDate: null, dueDate: '14-Apr-2020', completedDate: '-', assignedTo: null,
//                   assignedToObj : [id: null, fullName: null],
//                   assignedToUser: null, owner: null, searchUserGroupListUrl: 'foo',
//                   comments      : null, alertType: 'alertType', type: null, typeObj: null, config: null, configObj: null, viewed: false,
//                   details       : null, actionStatus: null, meetingId: null, configName: 'evdasAlert',
//                   configUrl     : 'null/evdasAlert/details?configId=1&callingScreen=review&isArchived=false',
//                   productName   : 'productName', soc: 'soc', pt: 'pt',
//                   ptUrl         : 'null/eventInfo/eventDetail?alertId=1&type=alertType&isArchived=false']
//
//        when: "associated with archived Evdas alert"
//        current = archivedEvdasAlert
//        result = service.createActionDTO(action)
//
//        then:
//        result == [id            : null, createdDate: null, dueDate: '14-Apr-2020', completedDate: '-', assignedTo: null,
//                   assignedToObj : [id: null, fullName: null],
//                   assignedToUser: null, owner: null, searchUserGroupListUrl: 'foo',
//                   comments      : null, alertType: 'alertType', type: null, typeObj: null, config: null, configObj: null, viewed: false,
//                   details       : null, actionStatus: null, meetingId: null, configName: 'evdasAlert',
//                   configUrl     : 'null/evdasAlert/details?configId=1&callingScreen=review&isArchived=true',
//                   productName   : 'productName', soc: 'soc', pt: 'pt',
//                   ptUrl         : 'null/eventInfo/eventDetail?alertId=1&type=alertType&isArchived=true']
//
//        when: "associated with Literature alert"
//        current = literatureAlert
//        result = service.createActionDTO(action)
//
//        then:
//        result == [id                    : null, createdDate: null, dueDate: '14-Apr-2020', completedDate: '-',
//                   assignedTo            : null, assignedToObj: [id: null, fullName: null], assignedToUser: null, owner: null,
//                   searchUserGroupListUrl: 'foo', comments: null, alertType: 'alertType', type: null, typeObj: null, config: null,
//                   configObj             : null, viewed: false, details: null, actionStatus: null, meetingId: null, configName: 'literatureAlert', articleTitle: 'articleTitle',
//                   articleUrl            : 'https://www.ncbi.nlm.nih.gov/pubmed/?term=0']
//
//        when: "associated with archived Literature alert"
//        current = archivedLiteratureAlert
//        result = service.createActionDTO(action)
//
//        then:
//        result == [id                    : null, createdDate: null, dueDate: '14-Apr-2020', completedDate: '-',
//                   assignedTo            : null, assignedToObj: [id: null, fullName: null], assignedToUser: null, owner: null,
//                   searchUserGroupListUrl: 'foo', comments: null, alertType: 'alertType', type: null, typeObj: null, config: null,
//                   configObj             : null, viewed: false, details: null, actionStatus: null, meetingId: null, configName: 'literatureAlert', articleTitle: 'articleTitle',
//                   articleUrl            : 'https://www.ncbi.nlm.nih.gov/pubmed/?term=0']
//
//
//        when: "associated with AdHocAlert alert"
//        current = adHocAlert
//        result = service.createActionDTO(action)
//
//        then:
//        result == [id           : null, createdDate: null, dueDate: '14-Apr-2020', completedDate: '-', assignedTo: null,
//                   assignedToObj: [id: null, fullName: null], assignedToUser: null, owner: null, searchUserGroupListUrl: 'foo', comments: null, alertType: 'alertType',
//                   type         : null, typeObj: null, config: null, configObj: null, viewed: false, details: null, actionStatus: null, meetingId: null,
//                   configName   : 'AdHocAlert', configUrl: 'null/adHocAlert/alertDetail?id=1']
//
//
//        when: "associated with Signal"
//        current = signal
//        result = service.createActionDTO(action)
//
//        then:
//        result == [id                    : null, createdDate: null, dueDate: '14-Apr-2020', completedDate: '-',
//                   assignedTo            : null, assignedToObj: [id: null, fullName: null], assignedToUser: null, owner: null,
//                   searchUserGroupListUrl: 'foo', comments: null, alertType: 'alertType', type: null, typeObj: null,
//                   config                : null, configObj: null, viewed: false, details: null, actionStatus: null, meetingId: null,
//                   configName            : 'signal', configUrl: 'null/validatedSignal/details?id=1', productName: null,
//                   eventName             : 'Blood and lymphatic system disorders (SOC)']

    }

    void "get searchQuery HQL for Open Actions"() {
        given:
        DataTableSearchRequest params = null
        String filterType = Constants.ActionItemFilterType.MY_OPEN

        when:
        String data = service.prepareActionListHQL(params, user1, filterType)

        then:
        data == sqlResult1
    }

    void "get searchQuery HQL for my All Actions"() {
        given:
        DataTableSearchRequest params = null
        String filterType = Constants.ActionItemFilterType.MY_ALL

        def mockUserService = Mock(UserService)
        service.userService = mockUserService
        mockUserService.getCurrentUserId() >> user1.id
        when:
        String data = service.prepareActionListHQL(params, user2, filterType)

        then:
        data == sqlResult2
    }

    void "get searchQuery HQL for All Actions"() {
        given:
        DataTableSearchRequest params = null
        String filterType = Constants.ActionItemFilterType.ALL

        when:
        String data = service.prepareActionListHQL(params, user3, filterType)

        then:
        data == sqlResult3
    }

    void "test populate() for guestAttendeeMail exist"() {
        given:
        Action action = new Action(details: "testing", config: actionConfiguration, type: actionType, dueDate: new Date())
        when:
        action = service.populate(action, user1, 1L, "test.test@test.com", "Aggregate Case Alert")
        then:
        action.guestAttendeeEmail == "test.test@test.com"
        Action.count() == 1
    }

    void "test action Instance for dueDate null"() {
        given:
        Action action = new Action(details: "testing", config: actionConfiguration,
                type: actionType, dueDate: null, actionStatus: "test",
                assignedTo: user1, createdDate: new Date())
        when:
        action.save(failOnError: true)
        then:
        thrown grails.validation.ValidationException
    }

    void "test action Instance for dueDate notNull"() {
        given:
        Action action = new Action(details: "testing", config: actionConfiguration,
                type: actionType, dueDate: new Date(), actionStatus: "test",
                assignedTo: user1, createdDate: new Date())
        when:
        action.save(failOnError: true)
        then:
        Action.count() == 1
    }

    void "update action"() {
        given:
        Action action = new Action(details: "testing", config: actionConfiguration,
                type: actionType, dueDate: new Date(), actionStatus: "test",
                assignedTo: user1, createdDate: new Date())
        action.save(failOnError: true)

        Alert alert = new Alert(assignedTo: user1, priority: new Priority(displayName: "priority1"))
        alert.save(flush: true, failOnError: true)

        Activity activity = new Activity(type: 'Activity Description', performedBy: user1,
                details: 'Testing', timestamp: DateTime.now(), alert: alert, justification: 'Justification')

        def mockActivityService = Mock(ActivityService)
        service.activityService = mockActivityService
        mockActivityService.create() >> activity

        String activityDescription = 'Activity Description'
        ActivityType activityTypeObj = new ActivityType(value: 'ActionChange')
        activityTypeObj.save(failOnError: true)
        def alertId = '23'

        when: 'app type is signal management'
        def response = service.updateAction(action, '', alertId, user1, '')
        then:
        response.details == "testing"

        when: 'app type is signal management'
        response = service.updateAction(action, activityDescription, alertId, user1, Constants.AlertConfigType.SIGNAL_MANAGEMENT)
        then:
        response.details == "testing"
    }

    void "populate action"() {
        given:
        Action action = new Action(details: "testing", config: actionConfiguration,
                type: actionType, dueDate: new Date(), actionStatus: "test",
                assignedTo: user1, createdDate: new Date())

        Alert alert = new Alert(id: 1, assignedTo: user1, priority: new Priority(displayName: "priority1"))
        alert.save(flush: true, failOnError: true)

        String assignedTo = "User_test_user1"

        def mockUserService = Mock(UserService)
        service.userService = mockUserService
        mockUserService.assignGroupOrAssignTo(assignedTo, action) >> action

        when: "for single case alert"
        action.alertType = Constants.AlertConfigType.SINGLE_CASE_ALERT
        String appType = Constants.AlertConfigType.SINGLE_CASE_ALERT
        def response = service.populate(action, user1, alert.id, assignedTo, appType)

        then:
        response.alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT

        when: "for archived single case alert"
        action.alertType = Constants.AlertConfigType.SINGLE_CASE_ALERT
        response = service.populate(action, user1, alert.id, assignedTo, appType, true)

        then:
        response.alertType == Constants.AlertConfigType.SINGLE_CASE_ALERT

        when: "for aggregate case alert"
        action.alertType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT
        appType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT
        response = service.populate(action, user1, alert.id, assignedTo, appType)

        then:
        response.alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT

        when: "for archived aggregate case alert"
        action.alertType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT
        appType = Constants.AlertConfigType.AGGREGATE_CASE_ALERT
        response = service.populate(action, user1, alert.id, assignedTo, appType, true)

        then:
        response.alertType == Constants.AlertConfigType.AGGREGATE_CASE_ALERT

        when: "for signal management"
        action.alertType = Constants.AlertConfigType.SIGNAL_MANAGEMENT
        response = service.populate(action, user1, alert.id, assignedTo, Constants.AlertConfigType.SIGNAL_MANAGEMENT)

        then:
        response.alertType == Constants.AlertConfigType.SIGNAL_MANAGEMENT

        when: "for evdas alert"
        action.alertType = Constants.AlertConfigType.EVDAS_ALERT
        response = service.populate(action, user1, alert.id, assignedTo, Constants.AlertConfigType.EVDAS_ALERT)

        then:
        response.alertType == Constants.AlertConfigType.EVDAS_ALERT

        when: "for archived evdas alert"
        action.alertType = Constants.AlertConfigType.EVDAS_ALERT
        response = service.populate(action, user1, alert.id, assignedTo, Constants.AlertConfigType.EVDAS_ALERT, true)

        then:
        response.alertType == Constants.AlertConfigType.EVDAS_ALERT

        when: "for literature alert"
        action.alertType = Constants.AlertConfigType.LITERATURE_SEARCH_ALERT
        response = service.populate(action, user1, alert.id, assignedTo, Constants.AlertConfigType.LITERATURE_SEARCH_ALERT)

        then:
        response.alertType == Constants.AlertConfigType.LITERATURE_SEARCH_ALERT

        when: "for archived literature alert"
        action.alertType = Constants.AlertConfigType.LITERATURE_SEARCH_ALERT
        response = service.populate(action, user1, alert.id, assignedTo, Constants.AlertConfigType.LITERATURE_SEARCH_ALERT, true)

        then:
        response.alertType == Constants.AlertConfigType.LITERATURE_SEARCH_ALERT

        when: "for adhoc alert with assigned to group"
        action.alertType = Constants.AlertConfigType.AD_HOC_ALERT
        action.assignedTo = null
        action.assignedToGroup = wfGroup
        response = service.populate(action, user1, alert.id, assignedTo, '')

        then:
        response.alertType == Constants.AlertConfigType.AD_HOC_ALERT

        when: "for adhoc alert with assigned to"
        action.alertType = Constants.AlertConfigType.AD_HOC_ALERT
        response = service.populate(action, user1, alert.id, assignedTo, '')

        then:
        response.alertType == Constants.AlertConfigType.AD_HOC_ALERT
    }

    void "return JSON map for action"() {
        given:
        ActionType actionType1 = new ActionType(id: 1, description: 'test1', displayName: 'test1', value: 'test1')
        actionType1.save(failOnError: true)
        ActionType actionType2 = new ActionType(id: 2, description: 'test2', displayName: 'test2', value: 'test2')
        actionType2.save(failOnError: true)

        List<Map> actionTypeList = ActionType.list().collect {
            [id: it.id, value: it.value, text: it.displayName]
        }

        when:
        Map response = service.actionPropertiesJSON(actionTypeList)

        then:
        response == [types    : [[id: 1, value: "test1", text: "test1"], [id: 2, value: "test2", text: "test2"]], configs: [],
                     allStatus: [[name: "InProcess", value: "In Progress"], [name: "New", value: "New"], [name: "ReOpened", value: "Re-opened"],
                                 [name: "Deleted", value: "Deleted"], [name: "Closed", value: "Closed"]]]

    }

    void "toDto"() {
        given:
        Action action = new Action(id: 1, details: "testing", config: actionConfiguration,
                type: actionType, dueDate: new Date(), actionStatus: "New",
                assignedTo: user1, createdDate: new Date())
        action.save(failOnError: true)

        when: "when completed date is null"
        def data = service.toDto(action, "UTC")

        then:
        data == [id     : 1, config: "test", type: "test", details: "testing", assignedTo: "Fake Name1", owner: null, comments: null,
                 dueDate: DateUtil.stringFromDate(new Date(), "dd-MMM-yyyy", "UTC"), passDue: true, actionStatus: "New", completedDate: "-"]

        when: "when completed date is present"
        action.completedDate = new Date()
        def mockUserService = Mock(UserService)
        service.userService = mockUserService
        mockUserService.getAssignedToName(action) >> null
        data = service.toDto(action, "UTC")

        then:
        data == [id           : 1, config: "test", type: "test", details: "testing", assignedTo: null, owner: null, comments: null,
                 dueDate      : DateUtil.stringFromDate(new Date(), "dd-MMM-yyyy", "UTC"), passDue: true, actionStatus: "New",
                 completedDate: DateUtil.toDateStringWithTime(new Date(), "UTC")]
    }

    void "list actions"() {
        given:
        Action action = new Action(id: 1, details: "testing", config: actionConfiguration,
                type: actionType, dueDate: new Date(), actionStatus: "New",
                assignedTo: user1, createdDate: new Date())
        SingleCaseAlert singleCaseAlert = new SingleCaseAlert()
        singleCaseAlert.id = 1L
        singleCaseAlert.name = "singleCaseAlert"
        singleCaseAlert.caseNumber = "caseNumber"
        singleCaseAlert.followUpNumber = 1
        singleCaseAlert.caseVersion = 1
        singleCaseAlert.executedAlertConfiguration = new ExecutedConfiguration()
        singleCaseAlert.executedAlertConfiguration.id = 1L

        ArchivedSingleCaseAlert archivedSingleCaseAlert = new ArchivedSingleCaseAlert()
        archivedSingleCaseAlert.id = 1L
        archivedSingleCaseAlert.name = "singleCaseAlert"
        archivedSingleCaseAlert.caseNumber = "caseNumber"
        archivedSingleCaseAlert.followUpNumber = 1
        archivedSingleCaseAlert.caseVersion = 1
        archivedSingleCaseAlert.executedAlertConfiguration = new ExecutedConfiguration()
        archivedSingleCaseAlert.executedAlertConfiguration.id = 1L

        AggregateCaseAlert aggregateCaseAlert = new AggregateCaseAlert()
        aggregateCaseAlert.name = "aggregateCaseAlert"
        aggregateCaseAlert.executedAlertConfiguration = new ExecutedConfiguration()
        aggregateCaseAlert.executedAlertConfiguration.id = 1L
        aggregateCaseAlert.productName = "productName"
        aggregateCaseAlert.soc = "soc"
        aggregateCaseAlert.pt = "pt"
        aggregateCaseAlert.id = 1L

        ArchivedAggregateCaseAlert archivedAggregateCaseAlert = new ArchivedAggregateCaseAlert()
        archivedAggregateCaseAlert.name = "aggregateCaseAlert"
        archivedAggregateCaseAlert.executedAlertConfiguration = new ExecutedConfiguration()
        archivedAggregateCaseAlert.executedAlertConfiguration.id = 1L
        archivedAggregateCaseAlert.productName = "productName"
        archivedAggregateCaseAlert.soc = "soc"
        archivedAggregateCaseAlert.pt = "pt"
        archivedAggregateCaseAlert.id = 1L

        EvdasAlert evdasAlert = new EvdasAlert()
        evdasAlert.id = 1L
        evdasAlert.name = "evdasAlert"
        evdasAlert.executedAlertConfiguration = new ExecutedEvdasConfiguration()
        evdasAlert.executedAlertConfiguration.id = 1L
        evdasAlert.substance = "productName"
        evdasAlert.soc = "soc"
        evdasAlert.pt = "pt"

        ArchivedEvdasAlert archivedEvdasAlert = new ArchivedEvdasAlert()
        archivedEvdasAlert.id = 1L
        archivedEvdasAlert.name = "evdasAlert"
        archivedEvdasAlert.executedAlertConfiguration = new ExecutedEvdasConfiguration()
        archivedEvdasAlert.executedAlertConfiguration.id = 1L
        archivedEvdasAlert.substance = "productName"
        archivedEvdasAlert.soc = "soc"
        archivedEvdasAlert.pt = "pt"

        LiteratureAlert literatureAlert = new LiteratureAlert()
        literatureAlert.name = "literatureAlert"
        literatureAlert.articleTitle = "articleTitle"
        literatureAlert.id = 1L

        ArchivedLiteratureAlert archivedLiteratureAlert = new ArchivedLiteratureAlert()
        archivedLiteratureAlert.name = "literatureAlert"
        archivedLiteratureAlert.articleTitle = "articleTitle"
        archivedLiteratureAlert.id = 1L

        ValidatedSignal signal = new ValidatedSignal()
        signal.name = "signal"
        signal.id = 1L
        signal.events = "{\"1\":[{\"name\":\"Blood and lymphatic system disorders\",\"id\":\"10005329\"}],\"2\":[],\"3\":[],\"4\":[],\"5\":[],\"6\":[]}"
        signal.products = "{\"1\":[],\"2\":[],\"3\":[{\"name\":\"ALL-LIC-PROD\",\"id\":\"100016\"}],\"4\":[]}"

        Alert alert = new Alert(id: 1, assignedTo: user1, priority: new Priority(displayName: "priority1"))
        PVSState workFlowState = new PVSState(id: 1L, value: "10", display: true, finalState: false, displayName: "display")
        Priority priority = new Priority([displayName: "mockPriority", value: "mockPriority", display: true, defaultPriority: true, reviewPeriod: 1])

        Topic topic = new Topic(id: 1L, name: "topic1", products: "paracetamol", disposition: defaultDisposition, createdBy: 'createdBy', modifiedBy: 'modifiedBy',
                workflowState: workFlowState, startDate: new Date(), endDate: new Date(), assignedTo: user1, priority: priority)

        def mockUserService = Mock(UserService)
        service.userService = mockUserService
        mockUserService.mockUserService() >> user1

        when: 'single case alert'
        def response = service.listActionsForAlert(singleCaseAlert.id, Constants.AlertConfigType.SINGLE_CASE_ALERT)
        then:
        response == []

        when: 'archived single case alert'
        response = service.listActionsForAlert(archivedSingleCaseAlert.id, Constants.AlertConfigType.SINGLE_CASE_ALERT, true)
        then:
        response == []

        when: 'aggregate case alert'
        response = service.listActionsForAlert(aggregateCaseAlert.id, Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
        then:
        response == []

        when: 'archived aggregate case alert'
        response = service.listActionsForAlert(archivedAggregateCaseAlert.id, Constants.AlertConfigType.AGGREGATE_CASE_ALERT, true)
        then:
        response == []

        when: 'evdas alert'
        response = service.listActionsForAlert(evdasAlert.id, Constants.AlertConfigType.EVDAS_ALERT)
        then:
        response == []

        when: 'archived evdas alert'
        response = service.listActionsForAlert(archivedEvdasAlert.id, Constants.AlertConfigType.EVDAS_ALERT, true)
        then:
        response == []

        when: 'literature alert'
        response = service.listActionsForAlert(literatureAlert.id, Constants.AlertConfigType.LITERATURE_SEARCH_ALERT)
        then:
        response == []

        when: 'literature alert'
        response = service.listActionsForAlert(archivedLiteratureAlert.id, Constants.AlertConfigType.LITERATURE_SEARCH_ALERT, true)
        then:
        response == []

        when: 'validated signal'
        response = service.listActionsForAlert(signal.id, Constants.AlertConfigType.SIGNAL_MANAGEMENT)
        then:
        response == []

        when: 'topic'
        response = service.listActionsForAlert(topic.id, Constants.AlertConfigType.TOPIC)
        then:
        response == []

        when: 'app type is empty'
        response = service.listActionsForAlert(alert.id, '')
        then:
        response == []
    }

    void "get map of actions as list"() {
        given:
        SingleCaseAlert singleCaseAlert = new SingleCaseAlert()
        singleCaseAlert.id = 1L
        singleCaseAlert.name = "singleCaseAlert"
        singleCaseAlert.caseNumber = "caseNumber"
        singleCaseAlert.followUpNumber = 1
        singleCaseAlert.caseVersion = 1
        singleCaseAlert.executedAlertConfiguration = new ExecutedConfiguration()
        singleCaseAlert.executedAlertConfiguration.id = 1L
        singleCaseAlert.assignedTo = user1

        def mockUserService = Mock(UserService)
        service.userService = mockUserService
        mockUserService.mockUserService() >> user1

        when: 'no action is present'
        def response = service.getActionListMap(singleCaseAlert.id)
        then:
        response == [[id: null, type: '', action: '', details: '', dueDate: '', assignedTo: '', status: '', completionDate: '']]
    }

    void "DTO for action"() {
        given:
        Action action = new Action(id: 1, details: "testing", config: actionConfiguration,
                type: actionType, dueDate: new Date(), actionStatus: "New",
                assignedTo: user1, createdDate: new Date())
        action.save(failOnError: true)
        List<Action> actionList = Action.list()
        ActionType actionType = new ActionType(description: 'test', displayName: 'test', value: 'test')
        ActionConfiguration actionConfiguration = new ActionConfiguration(displayName: 'test', value: 'test')
        actionConfiguration.save(failOnError: true)
        actionType.save(failOnError: true)
        def actionIdList = actionList.collect { it.id }
        def mockAlertService = Mock(AlertService)
        service.alertService = mockAlertService
        mockAlertService.getAlertNameMapForAction(new SingleCaseAlert(), actionIdList) >> [[:]]

        when:
        def response = service.actionServiceDTO(actionList)

        then:
        response == [[id: 1, config: null, type: null, details: 'testing', alertName: '', dueDate: DateUtil.toDateString(new Date(), 'UTC'), actionStatus: 'New', alertType: null, comments: null, completedDate: '-']]
    }

    void "test addNotification method"() {
        given:
        String appType = Constants.AlertConfigType.SINGLE_CASE_ALERT
        User user = new User(id: 1, username: "testuser")
        String assignedTo = "John Doe"
        String dueDate = "2024-07-15"
        String completionDate = "2024-07-20"
        String actionDetail = "Complete task X"
        String comments = "No comments"
        String actionType = "Task"
        String action = "Complete"
        String actionStatus = "In Progress"
        String productName = "Product A"
        Group group = new Group(name: "Group A")

        // Setup mock behavior
        userGroupService.fetchUserListForGroup(group) >> [user]
        // Mock InboxLog
        def inboxLog = Mock(InboxLog)
        inboxLog.metaClass.save = { -> true } // Mock save method

        when:
        service.addNotification(
                appType, user, assignedTo, dueDate, completionDate,
                actionDetail, comments, actionType, action, actionStatus, productName, group
        )

        then:
        1 * userGroupService.fetchUserListForGroup(group)

        and:
        noExceptionThrown() // Ensure no exceptions were thrown during execution
    }

}
