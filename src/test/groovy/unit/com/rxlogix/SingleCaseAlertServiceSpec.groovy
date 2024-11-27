package unit.com.rxlogix

import com.rxlogix.ActivityService
import com.rxlogix.AlertCommentService
import com.rxlogix.AlertService
import com.rxlogix.CaseHistoryService
import com.rxlogix.CategoryUtil
import com.rxlogix.Constants
import com.rxlogix.DataObjectService
import com.rxlogix.EmailNotificationService
import com.rxlogix.ProductBasedSecurityService
import com.rxlogix.PvsAlertTagService
import com.rxlogix.PvsGlobalTagService
import com.rxlogix.ReportExecutorService
import com.rxlogix.ReportExecutorServiceSpec
import com.rxlogix.ReportIntegrationService
import com.rxlogix.SignalDataSourceService
import com.rxlogix.SignalExecutorService
import com.rxlogix.SingleCaseAlertService
import com.rxlogix.UserService
import com.rxlogix.ValidatedSignalService
import com.rxlogix.ViewInstanceService
import com.rxlogix.cache.CacheService
import com.rxlogix.config.*
import com.rxlogix.dto.AlertDataDTO
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.dto.caseSeries.integration.ExecutedCaseSeriesDTO
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.helper.LinkHelper
import com.rxlogix.mart.CaseSeriesTagMapping
import com.rxlogix.mart.MartTags
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.AlertComment
import com.rxlogix.signal.AlertTag
import com.rxlogix.signal.BusinessConfiguration
import com.rxlogix.signal.CaseHistory
import com.rxlogix.signal.GlobalCase
import com.rxlogix.signal.GlobalProductEvent
import com.rxlogix.signal.PvsGlobalTag
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.SingleGlobalTag
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.signal.ViewInstance
import com.rxlogix.spotfire.SpotfireService
import com.rxlogix.user.Group
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import com.rxlogix.user.UserGroupMapping
import com.rxlogix.util.SignalQueryHelper
import com.rxlogix.util.SignalUtil
import com.rxlogix.util.ViewHelper
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.test.hibernate.HibernateSpec
import grails.testing.services.ServiceUnitTest
import grails.web.mapping.LinkGenerator
import groovy.json.JsonSlurper
import groovy.sql.Sql
import org.apache.http.HttpStatus
import org.hibernate.SQLQuery
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.criterion.CriteriaSpecification
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.slf4j.Logger
import org.springframework.context.MessageSource
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

import javax.servlet.http.HttpSession
import javax.sql.DataSource
import java.sql.Connection
import java.sql.DriverManager
import java.util.concurrent.Executors

@ConfineMetaClassChanges([SingleCaseAlertService,SingleCaseAlert,CategoryUtil, SpringSecurityUtils])
class SingleCaseAlertServiceSpec extends Specification implements ServiceUnitTest<SingleCaseAlertService> {

    Disposition disposition
    Disposition disposition2,disposition3
    Configuration alertConfiguration, alertConfiguration2
    @Shared
    ExecutedConfiguration executedConfiguration, executedConfiguration2, executedConfiguration3
    @Shared
    User user, newUser
    def attrMapObj
    @Shared
    SingleCaseAlert alert
    SingleCaseAlert alert2
    AggregateCaseAlert aggregateCaseAlert_a
    @Shared
    Group wfGroup
    Logger logger
    @Shared
    Session session
    SignalQueryHelper signalQueryHelper
    @Shared
    List execIds
    Activity activity1
    Priority priority1,priority2
    @Shared AlertComment alertComment
    @Shared
    Map map
    @Shared ViewInstance viewInstance
    @Shared
    AdvancedFilter advanceFilter
    ResponseDTO responseDTO
    ValidatedSignal validatedSignal1
    GlobalCase globalCase

    void setup() {
        logger = Mock(Logger)
        session = Mock(Session)
        service.sessionFactory = Mock(SessionFactory)
        signalQueryHelper = Mock(SignalQueryHelper)
        alertComment=new AlertComment(comments: "commetn1",alertType: "Single Case Alert",createdBy: "username",
                modifiedBy: "username",caseNumber: "1S01")
        alertComment.save(flush:true,failOnError:false)
        disposition = new Disposition(value: "ValidatedSignal1", displayName: "Validated Signal1", validatedConfirmed: true, abbreviation: "vs",productGroupSelection: '[{"name":"testing_AS (3)","id":"3"}]')
        disposition.save(flush: true, failOnError: false)
        disposition2 = new Disposition(value: "ValidatedSignal2", displayName: "Validated Signal2", validatedConfirmed: true, abbreviation: "vs",productGroupSelection: '[{"name":"testing_AS (3)","id":"3"}]')
        disposition.save(flush: true, failOnError: true)
        disposition3 = new Disposition(value: "ValidatedSignal3", displayName: "Validated Signal3", validatedConfirmed: true, abbreviation: "vs",productGroupSelection: '[{"name":"testing_AS (3)","id":"3"}]')
        disposition.save(flush: true, failOnError: true)

        wfGroup = new Group(name: "Default1", groupType: GroupType.WORKFLOW_GROUP, createdBy: 'createdBy',
                modifiedBy: 'modifiedBy',
                defaultQualiDisposition: disposition,
                defaultQuantDisposition: disposition,
                defaultAdhocDisposition: disposition,
                defaultEvdasDisposition: disposition,
                defaultLitDisposition: disposition,
                defaultSignalDisposition: disposition)

        user = new User(id: '1', username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.addToGroups(wfGroup)
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        UserGroupMapping userGroupMapping = new UserGroupMapping(user: user, group: wfGroup)
        userGroupMapping.save(flush: true, failOnError: false)
        user.save(flush: false)

        newUser = new User(id: '2', username: 'username2', createdBy: 'createdBy2', modifiedBy: 'modifiedBy2')
        newUser.preference.createdBy = "createdBy"
        newUser.preference.modifiedBy = "modifiedBy"
        newUser.preference.locale = new Locale("en")
        newUser.preference.isEmailEnabled = false
        newUser.groups = [wfGroup]
        newUser.metaClass.getFullName = { 'Fake Name' }
        newUser.metaClass.getEmail = { 'fake.email@fake.com' }
        newUser.save(failOnError: false)

        priority1 = new Priority(value: "Low", display: true, displayName: "Low", reviewPeriod: 3, priorityOrder: 1)
        priority1.save(flush: true, failOnError: true)
        priority2 = new Priority(value: "medium", display: true, displayName: "medium", reviewPeriod: 2, priorityOrder: 1)
        priority2.save(flush: true, failOnError: true)
        AlertDateRangeInformation alertDateRangeInformation1 = new AlertDateRangeInformation(dateRangeEndAbsolute: new Date() + 4, dateRangeStartAbsolute: new Date(),
                dateRangeEndAbsoluteDelta: 13, dateRangeStartAbsoluteDelta: 10, dateRangeEnum: DateRangeEnum.CUSTOM)
        AlertDateRangeInformation alertDateRangeInformation2 = new AlertDateRangeInformation(dateRangeEndAbsolute: new Date() + 4, dateRangeStartAbsolute: new Date(),
                dateRangeEndAbsoluteDelta: 13, dateRangeStartAbsoluteDelta: 10, dateRangeEnum: DateRangeEnum.CUMULATIVE)
        Category category1 = new Category(name: "category1")
        category1.save(flush: true, failOnError: true)
        ReportTemplate reportTemplate1 = new ReportTemplate(name: "repTemp1", description: "repDesc1", category: category1,
                owner: user, templateType: TemplateTypeEnum.TEMPLATE_SET, dateCreated: new Date(), lastUpdated: new Date() + 4,
                createdBy: "username", modifiedBy: "username")
        reportTemplate1.save(flush: true, failOnError: false)

        alertConfiguration = new Configuration(id:1,
                productSelection: '{"3":[{"name":"product1"}]}',
                executing: false,
                template: reportTemplate1,
                priority: priority1,
                alertTriggerCases: "11",
                alertTriggerDays: "11",
                dateCreated: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                name: "test",
                assignedTo: user,
                createdBy: "username",
                modifiedBy: "username",
                owner: user,
                adhocRun: true,
                alertQueryId: 1L,
                alertQueryName: "AlertQuery1",
                alertDateRangeInformation: alertDateRangeInformation1,
                reviewPeriod: 1
        )
        alertConfiguration.metaClass.getProductType = { 'family' }
        alertConfiguration.save(flush: true, failOnError: false)
        alertConfiguration2 = new Configuration(id:2,
                productSelection: '{"3":[{"name":"product2"}]}',
                executing: false,
                priority: priority1,
                alertTriggerCases: "11",
                alertTriggerDays: "11",
                dateCreated: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                name: "test1",
                assignedTo: user,
                createdBy: "username",
                modifiedBy: "username",
                owner: user,
                adhocRun: true,
                alertQueryId: 1L,
                alertQueryName: "AlertQuery1",
                reviewPeriod: 1
        )
        alertConfiguration2.metaClass.getProductType = { 'family2' }
        alertConfiguration2.save(flush: false, failOnError: false)
        ExecutedAlertDateRangeInformation executedAlertDateRangeInformation = new ExecutedAlertDateRangeInformation(dateRangeStartAbsoluteDelta: 2, dateRangeEndAbsoluteDelta: 5, dateRangeEnum: DateRangeEnum.LAST_YEAR,
                dateRangeStartAbsolute: Date.parse('dd-MMM-yyyy', '01-Jan-2014'), dateRangeEndAbsolute: Date.parse('dd-MMM-yyyy', '31-Dec-2014'))
        executedAlertDateRangeInformation.save(validate: false)

        ExecutedAlertDateRangeInformation executedAlertDateRangeInformation2 = new ExecutedAlertDateRangeInformation(dateRangeStartAbsolute: new Date(),
                dateRangeEndAbsolute: new Date() + 2, dateRangeEnum: DateRangeEnum.CUSTOM)
        executedAlertDateRangeInformation2.save(validate: false)
        AlertTag alertTag1 = new AlertTag(name: "alertTag1", createdBy: user, dateCreated: new Date())
        alertTag1.save(flush: true, failOnError: false)
        SingleGlobalTag singleGlobalTag1 = new SingleGlobalTag(caseSeriesId: 22L, caseId: 22L, tagId: 22L, owner: "userName",
                lastUpdated: new Date(), tagText: "tagText1")
        singleGlobalTag1.save(flush: true, failOnError: true)
        SingleGlobalTag singleGlobalTag2 = new SingleGlobalTag(/*caseSeriesId: 22L,*/ caseId: 2L, tagId: 1L, owner: "userName",
                lastUpdated: new Date(), tagText: "tagText2")
        singleGlobalTag2.save(flush: true, failOnError: true)
        SingleGlobalTag singleGlobalTag3 = new SingleGlobalTag(caseSeriesId: 22L, caseId: 2L, tagId: 1L, owner: "userName",
                lastUpdated: new Date(), tagText: "tagText3")
        singleGlobalTag3.save(flush: true, failOnError: true)
        ParameterValue parameterValue1 = new ParameterValue(key: "paramKey1", value: "paramValue1")
        parameterValue1.save(flush: true, failOnError: true)
        ExecutedQueryValueList executedQueryValueList1 = new ExecutedQueryValueList(query: 1L, queryName: "qName1", parameterValues: [parameterValue1])
        executedQueryValueList1.save(flush: true, failOnError: true)
        ExecutedQueryValueList executedQueryValueList2 = new ExecutedQueryValueList(query: 2L, queryName: "qName2")
        executedQueryValueList2.save(flush: true, failOnError: true)
        executedConfiguration = new ExecutedConfiguration(id: 2L, name: "test",
                executedAlertQueryValueLists: [executedQueryValueList1, executedQueryValueList2],
                owner: user, scheduleDateJSON: "{}", nextRunDate: new Date(),
                executedAlertDateRangeInformation: executedAlertDateRangeInformation,
                description: "test", dateCreated: new Date(), lastUpdated: new Date(),
                isPublic: true, isDeleted: true, isEnabled: true,
                dateRangeType: DateRangeTypeCaseEnum.CASE_LOCKED_DATE,
                productSelection: "", eventSelection: "['rash']", studySelection: "['test']",
                configSelectedTimeZone: "UTC",
                evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION,
                limitPrimaryPath: true,
                includeMedicallyConfirmedCases: true,
                excludeFollowUp: false, includeLockedVersion: true,
                adjustPerScheduleFrequency: true,
                createdBy: user.username, modifiedBy: user.username,
                assignedTo: user,
                pvrCaseSeriesId: 22L,
                caseSeriesExecutionStatus: ReportExecutionStatus.GENERATING,
                type: "executedConfiguration",
                isLatest: true, configId: alertConfiguration.id,
                workflowGroup: wfGroup,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10)
        executedConfiguration.save(flush:true, validate: false)
        executedConfiguration2 = new ExecutedConfiguration(id: 3L, name: "test",
                owner: user, scheduleDateJSON: "{}", nextRunDate: new Date(),
                executedAlertDateRangeInformation: executedAlertDateRangeInformation2,
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
                assignedTo: user, configId: alertConfiguration.id,
                pvrCaseSeriesId: 22L,
                type: "executedConfiguration",
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10)
        executedConfiguration2.save(failOnError: false)

        executedConfiguration3 = new ExecutedConfiguration(id: 3L, name: "test2",
                owner: user, scheduleDateJSON: "{}", nextRunDate: new Date(),
                executedAlertDateRangeInformation: executedAlertDateRangeInformation,
                description: "test2", dateCreated: new Date(), lastUpdated: new Date(),
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
                assignedTo: user, configId: alertConfiguration.id,
                pvrCaseSeriesId: 22L,
                type: "executedConfiguration",
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10)
        executedConfiguration3.save(failOnError: false)


        AlertTag alertTagA = new AlertTag(name: "testAlertA", createdBy: user, dateCreated: new Date())
        alertTagA.save(failOnError: false)
        aggregateCaseAlert_a = new AggregateCaseAlert(
                alertConfiguration: alertConfiguration,
                executedAlertConfiguration: executedConfiguration,
                name: executedConfiguration.name,
                priority: priority1,
                alertTags: alertTagA,
                disposition: disposition,
                assignedTo: user,
                detectedDate: executedConfiguration.dateCreated,
                productName: "Test Product A",
                soc: "BODY_SYS1",
                pt: 'Rash',
                hlt: 'TEST',
                hglt: 'TEST',
                llt: "INC_TERM2",
                newStudyCount: 1,
                cumStudyCount: 1,
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
                format: "test",
                frequency: "Yearly",
                productId: 100083,
                ptCode: 10029404,
                newSponCount: 2,
                smqCode: 1213,
                eb05Str: "male:0.1, female:0.2, unknown:0.3",
                eb95Str: "male:0.1, female:0.2, unknown:0.3",
                ebgmStr: "male:0.1, female:0.2, unknown:0.3")
        aggregateCaseAlert_a.save(failOnError: false)
        ExecutionStatus executionStatus1 = new ExecutionStatus(configId: alertConfiguration.id, reportVersion: 1,
                type: "executedConfiguration", name: "executionStatus1", nextRunDate: new Date() + 1, owner: user)
        executionStatus1.save(flush: true, failOnError: false)
        ExecutionStatus executionStatus2 = new ExecutionStatus(configId: alertConfiguration2.id, reportVersion: 1,
                type: "executedConfiguration", name: "executionStatus2", nextRunDate: new Date() + 1, owner: user)
        executionStatus2.save(flush: true, failOnError: false)
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

        validatedSignal1 = new ValidatedSignal(name: "vSignal1", assignedTo: user, assignmentType: "signalAssignment",
                createdBy: "username", disposition: disposition, modifiedBy: "username", priority: priority1, products: "product1", workflowGroup: wfGroup)
        validatedSignal1.save(flush: true, failOnError: true)
        globalCase= new GlobalCase()
        globalCase.caseId = 10l
        globalCase.versionNum = 1
        globalCase.save(failOnError: true)

        alert = new SingleCaseAlert(id: 1L,
                productSelection: "something",
                detectedDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                name: "Test Name",
                caseNumber: "1S01",
                caseVersion: 1,
                alertConfiguration: alertConfiguration,
                executedAlertConfiguration: executedConfiguration,
                reviewDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                priority: priority2,
                disposition: disposition,
                assignedTo: user,
                productName: "Test Product A",
                pt: "Rash",
                createdBy: config.assignedTo.username,
                modifiedBy: config.assignedTo.username,
                dateCreated: config.dateCreated,
                lastUpdated: config.dateCreated,
                productFamily: "Test Product A",
                attributesMap: attrMapObj,
                followUpExists: false,
                isNew: false,
                productId: 1,
                followUpNumber: 1,
                tags: singleGlobalTag1,
                alertTags: alertTag1,
                isCaseSeries: false,
                caseId: 1,
                comboFlag: "comboFlag",
                malfunction: "malfunction",
                globalIdentity: globalCase
        )
        alert.addToValidatedSignals(validatedSignal1)
        alert.save(flush: true, failOnError: false)

        alert2 = new SingleCaseAlert(id: 2L,
                productSelection: "something",
                detectedDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                name: "Test Name2",
                caseNumber: "1S02",
                caseVersion: 1,
                alertConfiguration: alertConfiguration,
                executedAlertConfiguration: executedConfiguration2,
                reviewDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                priority: alertConfiguration.priority,
                disposition: disposition,
                assignedTo: user,
                productName: "Test Product B",
                pt: "Rash",
                createdBy: config.assignedTo.username,
                modifiedBy: config.assignedTo.username,
                dateCreated: config.dateCreated,
                lastUpdated: config.dateCreated,
                productFamily: "Test Product B",
                attributesMap: attrMapObj,
                followUpExists: true,
                isNew: true,
                followUpNumber: 2,
                productId: 2,
                comboFlag: "flag1",
                malfunction: "function1",
                globalIdentity: globalCase,
                caseId: 2)
        alert2.addToValidatedSignals(validatedSignal1)
        alert2.save(flush: true, failOnError: false)
        ActivityType activityType1 = new ActivityType(value: ActivityTypeValue.JustificationChange)
        activityType1.save(flush: true, failOnError: true)
        activity1 = new Activity(details: "activityDetails", performedBy: user, timestamp: new Date(),
                justification: "change needed", assignedTo: user, type: activityType1)
        activity1.save(validate: false)

        ViewHelper viewHelper = Mock(ViewHelper)
        viewHelper.metaClass.getDictionaryValues(_)>>{
            return "product"
        }

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

//        MartTags.withTransaction {
//            martTags1 = new MartTags(name: "martTag1", type: "mart1", isCaseSeriesTag: true)
//            martTags1.save(flush: true, failOnError: true)
//        }
//        CaseSeriesTagMapping.withTransaction {
//            CaseSeriesTagMapping caseSeriesTagMapping1 = new CaseSeriesTagMapping(caseId: 1L, caseSeriesExecId: 1L, owner: "username",
//                    tag: martTags1, lastUpdated: new Date())
//            caseSeriesTagMapping1.save(flush: true, failOnError: true)
//        }
        ValidatedSignal validatedSignal1 = new ValidatedSignal(name: "vSignal2", assignedTo: user, assignmentType: "signalAssignment",
                createdBy: "username", disposition: disposition, modifiedBy: "username", priority: priority1, products: "product1", workflowGroup: wfGroup)
        validatedSignal1.save(flush: true, failOnError: true)
        ValidatedSignalService validatedSignalService = Mock(ValidatedSignalService)
        validatedSignalService.createSignalForBusinessConfiguration(*_) >> {
            return validatedSignal1
        }
        validatedSignalService.getAlertValidatedSignalList(_,_)>>{
            [[id: alert.id,signalId:validatedSignal1.id,name:validatedSignal1.name]]
        }
        service.validatedSignalService = validatedSignalService
        AlertCommentService alertCommentService=Mock(AlertCommentService)
        alertCommentService.getAlertCommentByConfigIdList(_,_)>>{
            return [alertComment]
        }
        service.alertCommentService=alertCommentService
        CacheService cacheService = Mock(CacheService)
        cacheService.getRptFieldIndexCache('caseProdDrugsPvrUdText20') >> {
            return "20"
        }
        cacheService.getRptFieldIndexCache('caseProdDrugsPvrUdText10') >> {
            return "10"
        }
        cacheService.getRptFieldIndexCache('caseProdDrugsPvrUdText11') >> {
            return "11"
        }
        cacheService.getDispositionByValue(_) >> {
            return disposition
        }
        cacheService.getPriorityByValue(_) >> {
            return priority1
        }
        cacheService.getUserByUserId(_) >> {
            return user
        }
        cacheService.getGroupByGroupId(_) >> {
            return wfGroup
        }
        service.cacheService = cacheService

        ViewInstanceService viewInstanceService = Mock(ViewInstanceService)
        viewInstanceService.fetchSelectedViewInstance(_,_) >>{
            return viewInstance
        }
        service.viewInstanceService = viewInstanceService

        SingleCaseAlert.metaClass.composeAlert={String s,List l,Boolean b,String s2,Boolean b2,String s3->
            [id:alert.id,globalId:12]
        }
        SignalExecutorService signalExecutorService = Mock(SignalExecutorService)
        signalExecutorService.threadPoolForQualAlertExec() >> {
            return Executors.newFixedThreadPool(8)
        }
        signalExecutorService.threadPoolForQualListExec()>>{
            return Executors.newFixedThreadPool(8)
        }
        service.signalExecutorService = signalExecutorService
        UserService userService = Mock(UserService)
        Preference preference = new Preference()
        preference.timeZone = "UTC"
        userService.getCurrentUserPreference() >> preference
        userService.getUser() >> {
            return user
        }
        service.userService = userService
        SpringSecurityService springSecurityService = Mock(SpringSecurityService)
        springSecurityService.isLoggedIn() >> {
            return true
        }
        springSecurityService.principal >> {
            return user
        }
        service.userService.springSecurityService = springSecurityService
        CaseHistory caseHistory1 = new CaseHistory(currentDisposition: disposition, currentAssignedTo: user, currentAssignedToGroup:
                wfGroup, currentPriority: new Priority(value: "tooLow", displayName: "priority1"), caseNumber: "1S01", caseVersion: 1,
                productFamily: "productFamily1", isLatest: true, followUpNumber: 1)
        caseHistory1.save(flush: true, failOnError: true)
        CaseHistory caseHistory2 = new CaseHistory(currentDisposition: disposition, currentAssignedTo: user, currentAssignedToGroup:
                wfGroup, currentPriority: new Priority(value: "High", displayName: "priority2"), caseNumber: '2', caseVersion: 1,
                productFamily: "productFamily2", isLatest: true)
        Long confId = alertConfiguration.id
        PvsGlobalTagService pvsGlobalTagService=Mock(PvsGlobalTagService)
        pvsGlobalTagService.getAllGlobalTags(_,_,_)>>{
            [[globalId:12]]
        }
        pvsGlobalTagService.batchPersistGlobalTags(_)>>{
            PvsGlobalTag pvsGlobalTag=new PvsGlobalTag(tagId: 1L,subTagId: 1L,tagText: "text",subTagText: "text",
                    globalId: 1L,domain: "Single Case Alert",createdAt: new Date())
            pvsGlobalTag.save(flush:true,failOnError:true)
            [[col1:pvsGlobalTag.id.toString(),col2:pvsGlobalTag.globalId.toString()]]
        }
        pvsGlobalTagService.fetchTagsAndSubtags()>>{
            return [["text":"text","id":1L]]
        }
        service.pvsGlobalTagService=pvsGlobalTagService
        DataObjectService dataObjectService = Mock(DataObjectService)
        dataObjectService.getDefaultDisposition(_) >> {
            return disposition
        }
        dataObjectService.getCaseIdList(_)>>{
            return [(alertConfiguration.id) : 1]
        }
        dataObjectService.getCaseTags(_)>>{
            return ['{"tags":"[{tagText:text,subTags:[text]}]"}']
        }
        dataObjectService.getExistingCaseHistory(_, _) >> {
            return null
        }
        dataObjectService.getSCAActivityList(_) >> {
            return [activity1]
        }
        dataObjectService.getCaseHistoryList(confId) >> {
            return [caseHistory2]
        }
        dataObjectService.clearCaseHistoryMap(confId) >> {
            return null
        }
        dataObjectService.getExistingCaseHistoryList(confId) >> {
            CaseHistory.list().get(0).isLatest = false
            [CaseHistory.list().get(0).id]
        }
        dataObjectService.getSignalAlertMap(_) >> {
            return [[alert: [caseNumber: "1S01", productFamily: "Test Product A"]]]
        }
        dataObjectService.clearSignalAlertMap(_) >> {
            return true
        }
        service.dataObjectService = dataObjectService
        MessageSource messageSource = Mock(MessageSource)
        messageSource.getMessage(_, _, _) >> {
            return "case history has been set"
        }
        service.messageSource = messageSource
        Map caseHistoryMap = [
                "configId"          : alertConfiguration.id,
                "currentDisposition": disposition,
                "currentPriority"   : priority1,
                "caseNumber"        : CaseHistory.list().get(0).caseNumber,
                "caseVersion"       : CaseHistory.list().get(0).caseVersion,
                "productFamily"     : CaseHistory.list().get(0).productFamily,
                "currentAssignedTo" : user,
                "justification"     : "case history has been set",
                "followUpNumber"    : alert.followUpNumber,
                "createdBy"         : CaseHistory.list().get(0).createdBy,
                "modifiedBy"        : "SYSTEM",
                "change"            : Constants.Commons.BLANK_STRING
        ]
        Map caseHistoryMap2 = [
                "configId"              : alertConfiguration?.id,
                "singleAlertId"         : alert2?.id,
                "currentDisposition"    : alert2.disposition,
                "currentPriority"       : alert2.priority,
                "caseNumber"            : alert2.caseNumber,
                "caseVersion"           : alert2.caseVersion,
                "productFamily"         : alert2.productFamily,
                "currentAssignedTo"     : alert2.assignedTo,
                "currentAssignedToGroup": alert2.assignedToGroup,
                "followUpNumber"        : alert2.followUpNumber,
                "justification"         : "change needed",
                "execConfigId"          : executedConfiguration.id,
                "change"                : Constants.HistoryType.DISPOSITION,
                "tagName"               : "tag1",
                "createdBySystem"       : "systemuser"
        ]
        CaseHistoryService caseHistoryService = Mock(CaseHistoryService)
        caseHistoryService.saveCaseHistoryForBusinessConfig(_) >> {
            CaseHistory.list().get(0).configId = caseHistoryMap.configId
            CaseHistory.list().get(0).currentPriority = caseHistoryMap.currentPriority
            CaseHistory.list().get(0).justification = caseHistoryMap.justification
            CaseHistory.list().get(0).followUpNumber = caseHistoryMap.followUpNumber
            CaseHistory.list().get(0).modifiedBy = caseHistoryMap.modifiedBy
            CaseHistory.list().get(0).change = caseHistoryMap.change
            CaseHistory.list().get(0).save(flush: true, failOnError: true)
        }
        caseHistoryService.getAlertHistoryByConfigIdList(_)>>{
            return [caseHistory1]
        }
        caseHistoryService.saveCaseHistory(_) >> {
            CaseHistory caseHistory = CaseHistory.list().get(0)
            caseHistory.configId = caseHistoryMap2.configId
            caseHistory.singleAlertId = caseHistoryMap2.singleAlertId
            caseHistory.currentDisposition = caseHistoryMap2.currentDisposition
            caseHistory.currentPriority = caseHistoryMap2.currentPriority
            caseHistory.caseNumber = caseHistoryMap2.caseNumber
            caseHistory.caseVersion = caseHistoryMap2.caseVersion
            caseHistory.productFamily = caseHistoryMap2.productFamily
            caseHistory.currentAssignedTo = caseHistoryMap2.currentAssignedTo
            caseHistory.currentAssignedToGroup = caseHistoryMap2.currentAssignedToGroup
            caseHistory.followUpNumber = caseHistoryMap2.followUpNumber
            caseHistory.justification = caseHistoryMap2.justification
            caseHistory.execConfigId = caseHistoryMap2.execConfigId
            caseHistory.change = caseHistoryMap2.change
            caseHistory.tagName = caseHistoryMap2.tagName
            caseHistory.createdBy = caseHistoryMap2.createdBySystem
            caseHistory.save(flush: true, failOnError: true)
        }
        service.caseHistoryService = caseHistoryService
        Long execConfId = ExecutedConfiguration.list().get(0).id
        Long activityId = Activity.list().get(0).id
        ActivityService activityService = Mock(ActivityService)
        activityService.batchPersistAlertLevelActivity(_) >> {
            [activityId]
        }
        activityService.createActivityBulkUpdate(*_)>>{
            activity1
        }
        service.activityService = activityService
        AlertService alertService = Mock(AlertService)
        alertService.batchPersistExecConfigActivityMapping(execConfId, [activityId], _) >> {
            ExecutedConfiguration executedConfiguration1 = ExecutedConfiguration.get(execConfId)
            Activity activity = Activity.get(activityId)
            executedConfiguration1.addToActivities(activity)
            executedConfiguration1.save(flush: true, failOnError: true)
        }
        alertService.getAttachmentMap(_,_)>>{
            return [[alertId: alert.id]]
        }
        alertService.prepareFilterMap(_,_)>>{
            return ['newDashboardFilter':true]
        }
        alertService.bulkUpdatePriority(_,_,_)>>{
            alert.priority=priority2
            alert.save(flush:true,failOnError:true)
        }
        alertService.isProductSecurity() >> {
            return false
        }
        alertService.fetchAllowedProductsForConfiguration()>>{
            return ["product1"]
        }
        List caseHistoryList = [caseHistory2]
        alertService.batchPersistForDomain(caseHistoryList, CaseHistory) >> {
            CaseHistory.withTransaction {
                caseHistoryList.each {
                    it.save(validate: false)
                }
            }
        }
        alertService.batchPersistForMapping(_, _, _) >> {
            validatedSignal1.addToSingleCaseAlerts(alert)
            validatedSignal1.save(flush: true, failOnError: true)
        }
        alertService.fetchPrevPeriodSCAlerts(_,_)>>{
            return [SingleCaseAlert.list().get(0)]
        }
        service.alertService = alertService
        PvsAlertTagService pvsAlertTagService=Mock(PvsAlertTagService)
        pvsAlertTagService.getAllAlertSpecificTags(_,_)>>{
            return [[alertId:alert.id]]
        }
        service.pvsAlertTagService=pvsAlertTagService

        ReportIntegrationService reportIntegrationService = Mock(ReportIntegrationService)
        reportIntegrationService.postData(_, _, _) >> {
            return [status: HttpStatus.SC_OK, result: [status: true, data: 1]]
        }
        service.reportIntegrationService = reportIntegrationService
        ReportExecutorService reportExecutorService = Mock(ReportExecutorService)
        reportExecutorService.addNotification(_) >> {
            return true
        }
        reportExecutorService.generateCaseSeries(_, _) >> {
            return [status: HttpStatus.SC_OK, result: [status: true, data: [id: 10]]]
        }
        service.reportExecutorService = reportExecutorService
        EmailNotificationService emailNotificationService = Mock(EmailNotificationService)
        emailNotificationService.emailHanlderAtAlertLevel(_, _) >> {
            return true
        }
        emailNotificationService.emailNotificationWithBulkUpdate(*_)>>{
            true
        }
        service.emailNotificationService = emailNotificationService
        def dataSource_pva = Mock(DataSource)
        service.dataSource_pva = dataSource_pva
        SignalDataSourceService signalDataSourceService = Mock(SignalDataSourceService)
        signalDataSourceService.getReportConnection("pva") >> {
            return dataSource_pva
        }
        service.signalDataSourceService = signalDataSourceService
        execIds = ExecutedConfiguration.list().id
//        service.sessionFactory = sessionFactory
        service.transactionManager = getTransactionManager()
    }
    @Ignore
    void "test listAll when there is some SingleCaseAlert"() {
        when:
        List result = service.listAll()
        then:
        result.size() == 2
        result.get(0) == alert
        result.get(1) == alert2
    }
    void "test listAll when there is no SingleCaseAlert"() {
        setup:
        SingleCaseAlert.list().each {
            it.delete()
        }
        when:
        List result = service.listAll()
        then:
        result.size() == 0
    }
    @Ignore
    void "test getAlertById when SingleCaseAlert is found"() {
        given:
        Integer id = SingleCaseAlert.findByName("Test Name")?.id
        when:
        SingleCaseAlert result = service.getAlertById(id)
        then:
        result == alert
    }
    void "test getAlertById when SingleCaseAlert is not found"() {
        when:
        SingleCaseAlert result = service.getAlertById(100L)
        then:
        result == null
    }
    void "test filterCases"() {
        given:
        List alertData = [["masterCaseNum_0": 1, "productFamilyId_9": "product1", 12: "CLINICAL TRIALS", 11: "CLINICAL TRIALS"]]
        when:
        def result = service.filterCases(alertConfiguration, alertData)
        then:
        result[0]."masterCaseNum_0" == 1
        result[0]."productFamilyId_9" == "product1"
    }
    @Ignore
    void "test processAlertData"() {
        given:
        List alertData = [["masterCaseNum_0": 1, "productFamilyId_9": "product1", "12": "CLINICAL TRIALS", "11": "CLINICAL TRIALS"]]
        String justification = "change needed"
        Boolean isAddCase = new Boolean("False")
        boolean isTempCaseSeriesFlow = true
        List dateRangeStartEndDate = [new Date(), new Date() + 1]
        List queryCaseMaps = [[:]]
        List supersededAlertIds = []
        when:
        List result = service.processAlertData(alertData, alertConfiguration, executedConfiguration, dateRangeStartEndDate,
                justification, isAddCase, queryCaseMaps, isTempCaseSeriesFlow, supersededAlertIds,"Single Case Alert")
        then:
        result[0].t1.getClass() == SingleCaseAlert
        result[0].t1.name == "test"
        result[0].t2 == null
    }
    @Ignore
    void "test createSingleCaseAlert"() {
        given:
        Map alertData = ["masterCaseNum_0": 1, "productFamilyId_9": "product1", "12": "CLINICAL TRIALS", "11": "CLINICAL TRIALS"]
        String justification = "change needed"
        Boolean isAddCase = new Boolean("False")
        boolean isTempCaseSeriesFlow = true
        List dateRangeStartEndDate = [new Date(), new Date() + 1]
        List queryCaseMaps = []
        String productSelection = '{"3":[{"name":"product1"}]}'
        when:
        def result = service.createSingleCaseAlert(alertData, alertConfiguration, executedConfiguration, dateRangeStartEndDate,
                justification, isAddCase, user, queryCaseMaps, isTempCaseSeriesFlow, productSelection, [alert.id],"Single Case Alert")
        then:
        result.t1.getClass() == SingleCaseAlert
        result.t1.name == "test"
        result.t2 == null
    }
    void "test getIsPrimaryInd when primaryInd contains required terms"() {
        given:
        String primaryInd = "CLINICAL TRIALS"
        when:
        boolean result = service.getIsPrimaryInd(primaryInd)
        then:
        result == true
    }
    void "test getIsPrimaryInd when primaryInd does not contains required terms"() {
        given:
        String primaryInd = "FIRST CASE"
        when:
        boolean result = service.getIsPrimaryInd(primaryInd)
        then:
        result == false
    }
    void "test getAppTypeAndNumValue whentype is NDA"() {
        given:
        Map alertData = ["20": "ndaType", "10": "blaType", "11": "andaType"]
        when:
        String result = service.getAppTypeAndNumValue(alertData)
        then:
        result == "ndaType"
    }
    void "test getAppTypeAndNumValue when type is BLA"() {
        given:
        CacheService cacheService = Mock(CacheService)
        cacheService.getRptFieldIndexCache("caseProdDrugsPvrUdNumber20") >> {
            return null
        }
        cacheService.getRptFieldIndexCache("caseProdDrugsPvrUdNumber10") >> {
            return "10"
        }
        service.cacheService = cacheService
        Map alertData = ["20": "ndaType", "10": "blaType", "11": "andaType"]
        when:
        String result = service.getAppTypeAndNumValue(alertData)
        then:
        result == "blaType"
    }
    void "test getAppTypeAndNumValue whentype is ANDA"() {
        given:
        CacheService cacheService = Mock(CacheService)
        cacheService.getRptFieldIndexCache("caseProdDrugsPvrUdNumber20") >> {
            return null
        }
        cacheService.getRptFieldIndexCache("caseProdDrugsPvrUdNumber10") >> {
            return null
        }
        cacheService.getRptFieldIndexCache("caseProdDrugsPvrUdNumber11") >> {
            return "11"
        }
        service.cacheService = cacheService
        Map alertData = ["20": "ndaType", "10": "blaType", "11": "andaType"]
        when:
        String result = service.getAppTypeAndNumValue(alertData)
        then:
        result == "andaType"
    }
    @Ignore
    void "test createAlert"() {
        given:
        List alertData = [["masterCaseNum_0": 1, "productFamilyId_9": "product1", "12": "CLINICAL TRIALS", "11": "CLINICAL TRIALS"]]
        String justification = "change needed"
        Boolean isAddCase = new Boolean("False")
        boolean isTempCaseSeriesFlow = true
        when:
        List result = service.createAlert(alertConfiguration, executedConfiguration, alertData, justification, isAddCase, isTempCaseSeriesFlow)
        then:
        result.get(0) == '1'
    }
    @Ignore
    void "test saveSingleCaseAlertGlobalTags on success"() {
        when:
        service.saveSingleCaseAlertGlobalTags(ExecutedConfiguration.list().get(1).id)
        then:
        1 * logger.info("Saving saveSingleCaseAlertGlobal Tags...")
        SingleCaseAlert.list().get(1).tags[0] == SingleGlobalTag.list().get(1)
    }
    @Ignore
    void "test saveSingleCaseAlertGlobalTags when sgtList is empty"() {
        when:
        service.saveSingleCaseAlertGlobalTags(ExecutedConfiguration.list().get(0).id)
        then:
        1 * logger.info("Saving saveSingleCaseAlertGlobal Tags...")
        SingleCaseAlert.list().get(1).tags == null
    }
    @Ignore
    void "test prevExecutedAlert"() {
        when:
        def result = service.prevExecutedAlert(ExecutedConfiguration.list().get(1))
        then:
        result.pvrCaseSeriesId == 22
        result.id == ExecutedConfiguration.list().get(0).id
    }
    @Ignore
    @Unroll
    void "test getTagList"() {
        expect:
        if (service.getTagList(a, b, c).size() > 0) {
            service.getTagList(a, b, c).get(0).tagId == result[0]
            service.getTagList(a, b, c).get(0).caseId == result[1]
            if (b == true) {
                service.getTagList(a, b, c).get(0).owner == result[2]
                service.getTagList(a, b, c).get(0).caseSeriesId == result[3]
            }
        } else {
            service.getTagList(a, b, c) == []
        }
        where:
        a    |b     | c     | result
        [2L] |9L    | false | [1, 2]
        [2L] |9L    | true  | [1, 2, "userName", 22]
        [1L] |9L    | false | []
        [1L] |9L    | true  | []
    }
    @Ignore
    void "test getSCAListByExecId when id is given"() {
        when:
        List result = service.getSCAListByExecId(execIds[0])
        then:
        result == [[id: SingleCaseAlert.list().get(0).id, caseId: SingleCaseAlert.list().get(0).caseId]]
    }
    @Ignore
    @Unroll
    void "test getSCAListByExecId id is not given for invalid id"() {
        expect:
        service.getSCAListByExecId(a) == result
        where:
        a      | result
        10000L | []
        null   | []
    }
    @Ignore
    void "test saveSCATag on success"() {
        setup:
        String alertId = SingleCaseAlert.list().get(1).id
        String id = SingleGlobalTag.list().get(0).id
        String tagId = SingleGlobalTag.list().get(1).id
        List scaAlertTagList = [[alertId: alertId, id: id, tagId: tagId]]
        Session session = service.sessionFactory.currentSession
        when:
        service.saveSCATag(scaAlertTagList, session, true)
        then:
        SingleCaseAlert.list().get(1).tags[0] == SingleGlobalTag.list().get(0)
    }
    @Ignore
    void "test saveSCATag when exceptionOccurs"() {
        Integer alertId = SingleCaseAlert.list().get(1).id
        Integer id = SingleGlobalTag.list().get(0).id
        Integer tagId = SingleGlobalTag.list().get(1).id
        List scaAlertTagList = [[alertId: alertId, id: id, tagId: tagId]]
        Session session = service.sessionFactory.currentSession
        when:
        service.saveSCATag(scaAlertTagList, session, true)
        then:
        SingleCaseAlert.list().get(1).tags.size() == 0
    }
    @Ignore
    void "test persistCaseHistory"() {
        when:
        service.persistCaseHistory(Configuration.list().get(0).id)
        then:
        CaseHistory.list().get(1) != null
        CaseHistory.list().get(1).currentPriority.value == "High"
    }
    @Ignore
    void "test updateExistingCaseHistoryList"() {
        setup:
        Long confId = Configuration.list().get(0).id
        when:
        service.updateExistingCaseHistoryList(confId)
        then:
        CaseHistory.list().get(0).isLatest == false
    }
    @Ignore
    void "test persistSCAActivity"() {
        when:
        service.persistSCAActivity(ExecutedConfiguration.list().get(0).id, [Activity.list().get(0)])
        then:
        ExecutedConfiguration.list().get(0).activities[0] == Activity.list().get(0)
    }
    @Ignore
    @Unroll
    void "test populateCSDTO"() {
        setup:
        service.queryService=[queryDetailByName:{String name->}]
        expect:
        service.populateCSDTO(a, b, c).executedGlobalQueryValueLists.size() == result[0]
        service.populateCSDTO(a, b, c).ownerName == result[1]
        service.populateCSDTO(a, b, c).excludeFollowUp == result[2]
        service.populateCSDTO(a, b, c).includeAllStudyDrugsCases == result[3]
        service.populateCSDTO(a, b, c).includeLockedVersion == result[4]
        service.populateCSDTO(a, b, c).isTemporary == result[5]
        service.populateCSDTO(a, b, c).callbackURL == result[6]
        where:
        a                     | b     | c     | result
        executedConfiguration | false | false | [2, "username", false, false, true, false, "null/false"]
        executedConfiguration | true  | true  | [2, "username", false, false, true, true, "null/true"]
    }
    void "test setValuesFromFreshData"() {
        when:
        def result = service.setValuesFromFreshData(alert, alertConfiguration, disposition, executedConfiguration)
        then:
        result == null
    }
    void " test updateSingleCaseAlertStates update disposition"() {
        given:
        Map map = [change: "DISPOSITION", currentDisposition: disposition2, currentAssignedTo: user]
        when:
        service.updateSingleCaseAlertStates(alert, map)
        then:
        alert.disposition == disposition2
        alert.followUpExists == false
    }
    void " test updateSingleCaseAlertStates update assignedTo"() {
        given:
        Map map = [change: "ASSIGNED_TO", currentAssignedTo: newUser, currentAssignedToGroup: wfGroup]
        when:
        service.updateSingleCaseAlertStates(alert, map)
        then:
        alert.assignedTo == newUser
        alert.assignedToGroup == wfGroup
        alert.followUpExists == false
    }
    void " test updateSingleCaseAlertStates update PRIORITY"() {
        given:
        Map map = [change: "PRIORITY", currentPriority: Priority.list().get(1)]
        when:
        service.updateSingleCaseAlertStates(alert, map)
        then:
        alert.priority == Priority.list().get(1)
        alert.followUpExists == false
    }
    @Ignore
    void "test listSelectedAlerts on success"() {
        given:
        String alerts = "${alert.id},${alert2.id}"
        when:
        List result = service.listSelectedAlerts(alerts, SingleCaseAlert)
        then:
        result[0] == alert
        result[1] == alert2
    }
    @Ignore
    void "test validCaseNumbersFromExcel on success"() {
        given:
        List caseNumberList = ["1S01", "1S02"]
        Set warnings = ["listOfWarning"]
        when:
        Map result = service.validCaseNumbersFromExcel(caseNumberList, warnings, executedConfiguration)
        then:
        result == [validCaseNumber: ["'1S02'"], invalidCaseNumber: ["1S01", "listOfWarning"]]
    }
    void "test validCaseNumbersFromExcel when caseNumberList is empty"() {
        given:
        List caseNumberList = []
        Set warnings = ["listOfWarning"]
        when:
        Map result = service.validCaseNumbersFromExcel(caseNumberList, warnings, executedConfiguration)
        then:
        result == ["validCaseNumber": [], "invalidCaseNumber": ["listOfWarning"]]
    }
    @Ignore
    @Unroll
    void "test getRelatedCaseSeries"() {
        expect:
        List returnedValue = service.getRelatedCaseSeries(a)
        if (returnedValue.size() > 0) {
            returnedValue.get(0).alertId == result[0]
            returnedValue.get(0).name == result[1]
            returnedValue.get(0).query == result[2]
            returnedValue.get(0).productSelection == result[3]
        } else {
            returnedValue == []
        }

        where:
        a        | result
        ["1S01"] | [1L, "test", "AlertQuery1", "product1"]
        [""]     | [null, null, null, null]
    }
    void "test getDateRangeAlert on success"() {
        when:
        String result = service.getDateRangeAlert(executedConfiguration.id)
        then:
        result != null
    }
    void "test getDateRangeAlert when id doesn't match"() {
        when:
        String result = service.getDateRangeAlert(1000L)
        then:
        result == ""
    }
    @Ignore
    void "test getAttachSignalAlertList on success"() {
        setup:
        alert.validatedSignals = null
        alert.save(flush:true,failOnError:true)
        when:
        List result = service.getAttachSignalAlertList(executedConfiguration.id)
        then:
        result == [alert]
    }
    @Ignore
    void "test getAttachSignalAlertList when id doesn't match"() {
        when:
        List result = service.getAttachSignalAlertList(1000L)
        then:
        result == []
    }
    @Ignore
    void "test getAlertIdAndSignalIdForBusinessConfig on success"() {
        when:
        List result = service.getAlertIdAndSignalIdForBusinessConfig(executedConfiguration.id, alertConfiguration.id, [alert])
        then:
        result.col2 != null

    }
    void "test getAlertIdAndSignalIdForBusinessConfig when executedConfiguration id doesn't match"() {
        setup:
        DataObjectService dataObjectService = Mock(DataObjectService)
        dataObjectService.getSignalAlertMap(executedConfiguration.id) >> {
            return null
        }
        service.dataObjectService = dataObjectService
        when:
        List result = service.getAlertIdAndSignalIdForBusinessConfig(executedConfiguration.id, alertConfiguration.id, [alert])
        then:
        result == []
    }
    void "test createCaseHistoryForDispositionChange"() {
        when:
        service.createCaseHistoryForDispositionChange(alert2, "change needed", executedConfiguration, alertConfiguration, "systemuser")
        then:
        CaseHistory caseHistory = CaseHistory.list().get(0)
        caseHistory.justification == "change needed"
        caseHistory.createdBy == "systemuser"
        caseHistory.productFamily == "Test Product B"
    }
    @Ignore
    void "test getAlertConfigObject on success"() {
        when:
        Configuration result = service.getAlertConfigObject(executedConfiguration)
        then:
        result != null
    }
    void "test getAlertConfigObject when condition fails"() {
        when:
        Configuration result = service.getAlertConfigObject(null)
        then:
        result == null
    }
    @Ignore
    void "test saveCaseSeriesConfiguration"() {
        setup:
        UserService userService = Mock(UserService)
        userService.getUser() >> {
            return newUser
        }
        service.userService = userService
        when:
        Configuration result = service.saveCaseSeriesConfiguration(1l, "newName", 1l, 1l, "1","pva")
        then:
        result.type == "Single Case Alert"
        result.isEnabled == false
        result.owner == user
        result.priority == Priority.list().get(0)
        result.createdBy == newUser.username
        result.modifiedBy == newUser.username
    }
    void "test bindNewTemplateQueryForCaseSeries"() {
        when:
        service.bindNewTemplateQueryForCaseSeries(alertConfiguration2, alertConfiguration)
        then:
        alertConfiguration2.template == alertConfiguration.template
        alertConfiguration2.alertDateRangeInformation.dateRangeEnum == alertConfiguration.alertDateRangeInformation.dateRangeEnum
        alertConfiguration2.alertDateRangeInformation.dateRangeEndAbsolute == alertConfiguration.alertDateRangeInformation.dateRangeEndAbsolute
        alertConfiguration2.alertDateRangeInformation.dateRangeStartAbsolute == alertConfiguration.alertDateRangeInformation.dateRangeStartAbsolute
        alertConfiguration2.alertDateRangeInformation.dateRangeEndAbsoluteDelta == alertConfiguration.alertDateRangeInformation.dateRangeEndAbsoluteDelta
        alertConfiguration2.alertDateRangeInformation.dateRangeStartAbsoluteDelta == alertConfiguration.alertDateRangeInformation.dateRangeStartAbsoluteDelta
    }
    @Ignore
    void "test saveCaseSeriesExecutedConfig"() {
        when:
        ExecutedConfiguration result = service.saveCaseSeriesExecutedConfig(executedConfiguration.id, alertConfiguration, "pva")
        then:
        result.workflowGroup == alertConfiguration.workflowGroup
        result.assignedTo == alertConfiguration.assignedTo
    }
    @Ignore
    void "test generateTempCaseSeries when dateRangeEnum is not cumulative"() {
        when:
        service.generateTempCaseSeries("test", executedConfiguration2.id)
        then:
        executedConfiguration2.pvrCaseSeriesId == 10L
        executedConfiguration2.isLatest == true
        executedConfiguration2.pvrCumulativeCaseSeriesId != 10L
    }
    @Ignore
    void "test generateTempCaseSeries when dateRangeEnum is  cumulative"() {
        given:
        when:
        service.generateTempCaseSeries("test", executedConfiguration.id)
        then:
        executedConfiguration.pvrCaseSeriesId == 10L
        executedConfiguration.isLatest == true
        executedConfiguration.pvrCumulativeCaseSeriesId == null
    }
    void "test generateTempCaseSeries when response status is not 200"() {
        setup:
        ReportExecutorService reportExecutorService = Mock(ReportExecutorService)
        reportExecutorService.generateCaseSeries(_, _) >> {
            return [status: HttpStatus.SC_BAD_REQUEST, result: [status: true, data: [id: 10]]]
        }
        service.reportExecutorService = reportExecutorService
        when:
        service.generateTempCaseSeries("test", executedConfiguration2.id)
        then:
        executedConfiguration2.pvrCaseSeriesId != 10L
        executedConfiguration2.isLatest == false
        executedConfiguration2.pvrCumulativeCaseSeriesId != 10L
    }
    void "test createActivityForBulkUpdate when value of new Disposition is not null"(){
        when:
        Activity result=service.createActivityForBulkUpdate(alert,disposition2,disposition,"change needed","validatedSignal1")
        then:
        result==activity1
    }
    @Ignore
    void "test changePriorityOfAlerts"(){
        service.metaClass.getTagsNameListBulkOperations={List ids->
            return [:]
        }
        service.alertService.generateDomainName(_) >> SingleCaseAlert
        when:
        service.changePriorityOfAlerts([alert.id],"chege needed", priority2, responseDTO,false)
        then:
        alert.priority==priority2
    }
    @Ignore
    @Unroll
    void "test getFiltersFromParams"(){
        setup:
        service.cacheService = [getNotReviewCompletedAndClosedDisposition:{->[disposition,disposition2,disposition3]}]
        expect:
        service.getFiltersFromParams(a,b)==result
        where:
        a     |                          b                                   |          result
        true  |[filters: "[['name':'name1']]",dashboardFilter:"total"]       |      [[name:"name1"]]
        false |[filters: "[['name':'name1']]",dashboardFilter:"total"]       |      ["Validated Signal1"]
        false |[filters: "[['name':'name1']]",dashboardFilter:"underReview"] |      []
        false |[filters: "[['name':'name1']]"]                               |      ["Validated Signal1", "Validated Signal2","Validated Signal3"]
    }
    @Ignore
    @Unroll
    void "test generateAlertDataDTO"(){
        setup:
        service.userService=[getUserFromCacheByUsername:{String userName->user},getCurrentUserName:{->user.name}]
        expect:
        AlertDataDTO output=service.generateAlertDataDTO(a,b)
        output.userId!=result[1]
        output.isFullCaseList==result[2]
        output.workflowGroupId!=result[3]
        output.groupIdList!=result[4]
        where:
        a                                                          |    b    |   result
        [filters: "[['name':'name1']]",dashboardFilter:"new"]      |  false  | [SingleCaseAlert,null,true,null,null]
        [filters: "[['name':'name1']]",dashboardFilter:"new"]      |  true   | [SingleCaseAlert,null,true,null,null]
    }
    @Ignore
    void "test getSingleCaseAlertList"(){
        given:
        List list=[alert]
        AlertDataDTO alertDataDTO=new AlertDataDTO(cumulative: true,domainName : SingleCaseAlert, params: [isFaers: "true"])
        when:
        List result=service.getSingleCaseAlertList(list ,alertDataDTO)
        then:
        result.size()>0
        result[0].id!=null
        result[0].globalId==12
        result[0].alertTags[0].alertId!=null
        result[0].alertTags[1].globalId==12
    }
    void "test getSingleCaseAlertList when list is empty"(){
        given:
        List list=[]
        AlertDataDTO alertDataDTO=new AlertDataDTO(cumulative: true, domainName : SingleCaseAlert)
        when:
        List result=service.getSingleCaseAlertList(list ,alertDataDTO)
        then:
        result==[]
    }
    @Ignore
    void "test changeDisposition"(){
        setup:
        alert.disposition.validatedConfirmed=false
        alert.save(flush:true,failOnError:true)
        service.metaClass.getTagsNameListBulkOperations={List l->
            return [:]
        }
        SignalUtil.metaClass.static.generateProductDictionaryJson={String s1,String s2,String s3->
            return null
        }
        service.alertService.generateDomainName(_) >> SingleCaseAlert
        when:
        Map result = service.changeDisposition([alert.id], disposition2, alert.disposition.displayName, "changeNeeded", "vSignal1", "json", false, 1)
        then:
        result.dispositionChanged==true
        result.attachedSignalData==true
    }
    @Ignore
    void "test revertDisposition"(){
        setup:
        alert.disposition.validatedConfirmed=false
        alert.save(flush:true,failOnError:true)
        service.alertService.generateDomainName(_) >> SingleCaseAlert
        when:
        Map result=service.revertDisposition(1,"test justification")
        then:
        result.domain == SingleCaseAlert
    }
    @Ignore
    void "test undoneCaseHistory"(){
        setup:
        alert.disposition.validatedConfirmed=false
        alert.save(flush:true,failOnError:false)
        service.alertService.generateDomainName(_) >> SingleCaseAlert
        when:
        Map result=service.undoneCaseHistory(alert)
        then:
        result
    }
    @Ignore
    void "test createActivityForUndoAction"(){
        setup:
        alert.disposition.validatedConfirmed=false
        alert.save(flush:true,failOnError:false)
        service.alertService.generateDomainName(_) >> SingleCaseAlert
        when:
        Activity activity = service.createActivityForUndoAction(alert,"justification")
        then:
        activity
    }
    @Ignore
    void "test saveTagsForBusinessConfig"(){
        setup:
        CategoryUtil.metaClass.static.saveCategories={List l->}
        when:
        service.saveTagsForBusinessConfig(alertConfiguration.id, null)
        then:
        PvsGlobalTag.findByTagText("text")!=null
    }

    void "test parallelyCreateSCAlerts"(){
        when:
        def result = service.parallelyCreateSCAlerts([:])

        then:
        result == null
    }

    void "test getSingleCaseAlertCriteriaData()"(){
        Map params = [viewId:viewInstance.id as Long, advancedFilterId: advanceFilter.id as Long]
        when:
        def resultList = service.getSingleCaseAlertCriteriaData(executedConfiguration, params)
        then:
        resultList.size() == 21
    }

    void "test saveFieldsInSingleCaseAlert"(){
        setup:
        service.cacheService=[getRptFieldIndexCache:{String rptField->rptField == "dvProdEventTimeOnsetDays"?1:"value"}]
        when:
        SingleCaseAlert result = service.saveFieldsInSingleCaseAlert(alertConfiguration, executedConfiguration, [:],[:],false,"Single Case Alert")
        then:
        result.name == "test"
    }
    @Ignore
    void "test removeKeysFromData"(){
        when:
        String result = service.removeKeysFromData("Historical Condition: Application site pain")
        then:
        result == "Application site pain"
    }
    void "test saveAlertCaseHistory"(){
        setup:
        CacheService cacheService = Mock(CacheService)
        cacheService.getUserByUserNameIlike(_)>>{
            return user
        }
        service.cacheService = cacheService
        when:
        service.saveAlertCaseHistory(alert, "just", "signaldev")
        then:
        alert.justification == "just"
        alert.dispPerformedBy == user.fullName
    }

    void "test isMatchedFromException"(){
        setup:
        List<String> errorMessages = ["error message", "another error"]
        Throwable t = new RuntimeException("This is an error message.")
        when:
        def result = service.isMatchedFromException(t,errorMessages)
        then:
        result == true
    }

    void "test isMatchedFromException when Exception is null"(){
        setup:
        List<String> errorMessages = ["error message", "another error"]
        when:
        def result = service.isMatchedFromException(null,errorMessages)
        then:
        result == false
    }

    void "test isMatchedFromException when errorMessages is empty list"(){
        setup:
        Throwable t = new RuntimeException("This is an error message.")
        when:
        def result = service.isMatchedFromException(t,[])
        then:
        result == false
    }

    void "test dssComparisionOperation"() {
        when:
        boolean result = service.dssComparisionOperation(value, threshold, operator)
        then:
        result == expectedValue
        where:
        value  | threshold | operator | expectedValue
        2      | 1         | '>'      | true
        1      | 2         | '>'      | false
        2      | 2         | '>='     | true
        1      | 2         | '>='     | false
        1      | 2         | '<'      | true
        2      | 1         | '<'      | false
        2      | 2         | '<='     | true
        1      | 2         | '<='     | true
        2      | 2         | '=='     | true
        1      | 2         | '=='     | false
        1.0    | 1.0       | '=='     | true
        1.1    | 1.0       | '=='     | false
    }

    void "test getCaseAlertMapping"(){
        setup:
        List<Map> alertData = [
                [id: 1, globalIdentity: [caseId: 101]],
                [id: 2, globalIdentity: [caseId: 102]],
                [id: 3, globalIdentity: [caseId: 103]]
        ]
        when:
        def result = service.getCaseAlertMapping(alertData)
        then:
        result.size() == alertData.size()
    }

    void "test fetchCasesFromData"(){
        setup:
        CacheService cacheService = Mock(CacheService)
        cacheService.getRptFieldIndexCache(_) >> {
            return 1
        }
        service.cacheService = cacheService
        List<Map> alertData = [["masterCaseId": 1, "masterVersionNum": 3, "12": "CLINICAL TRIALS", "11": "CLINICAL TRIALS"]]
        when:
        def result =  service.fetchCasesFromData(alertData)
        then:
        println result
    }

    void "test generateCompletenessScore"(){
        setup:
        CacheService cacheService = Mock(CacheService)
        cacheService.getRptFieldIndexCache(_) >> {
            return "20"
        }
        service.cacheService = cacheService
        Map alertData = ["20": "3", "10": "5", "11": "6"]
        when:
        def result = service.generateCompletenessScore(alertData)
        then:
        result == 3.0
    }

    void "test generateCompletenessScore for empty data"(){
        when:
        def result = service.generateCompletenessScore([:])
        then:
        result == null
    }

    void "test generateExecutedCaseSeriesAsync"(){
        setup:
        SignalExecutorService signalExecutorService = Mock(SignalExecutorService)
        signalExecutorService.threadPoolForCaseSeriesExec() >> {
            return Executors.newFixedThreadPool(8)
        }
        service.signalExecutorService = signalExecutorService
        when:
        service.generateExecutedCaseSeriesAsync(executedConfiguration,true)
        then:
        logger.info("generateExecutedCaseSeriesAsync started..............")
        logger.info("executeUpdate executed.")
        logger.info("generateExecutedCaseSeriesAsync finished.......")
    }

    def "test insertSingleAlertActions generic case"() {
        given:
        Long executedConfig = 1L
        Long prevExecConfigId = 2L
        String sqlStatement = "SELECT col1, col2 FROM some_table WHERE executed_config = :executedConfig AND prev_exec_config_id = :prevExecConfigId"
        List<Map<String, String>> alertIdAndActionIdList = [
                [col1: "1", col2: "2", col3: '1'],
                [col1: "3", col2: "4", col3: '1']
        ]
        String insertActionSingleAlertQuery = "INSERT INTO SINGLE_ALERT_ACTIONS(SINGLE_CASE_ALERT_ID, ACTION_ID, IS_RETAINED) VALUES(?, ?, ?)"
        String actionCountSql = "UPDATE SINGLE_CASE_ALERT SET action_count = action_count + 1 WHERE executed_config = :executedConfig"

        and:
        signalQueryHelper.single_alert_actions(_, _ ) >> sqlStatement

        SQLQuery queryMock = Mock(SQLQuery)
        session.createSQLQuery(_) >> queryMock
        queryMock.list() >> [
                [1, 2],
                [3, 4]
        ]

        signalQueryHelper.single_alert_actions_count(_) >> actionCountSql

        SQLQuery countQueryMock = Mock(SQLQuery)
        session.createSQLQuery(actionCountSql) >> countQueryMock
        countQueryMock.executeUpdate() >> 1

        when:
        service.insertSingleAlertActions(session, executedConfig, prevExecConfigId)

        then:
        1 * service.alertService.batchPersistForStringParameters(session, alertIdAndActionIdList, insertActionSingleAlertQuery, 3)
        1 * session.createSQLQuery(actionCountSql).executeUpdate()
    }



}