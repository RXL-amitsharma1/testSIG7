package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.customException.ExecutionStatusException
import com.rxlogix.enums.*
import com.rxlogix.mapping.ERMRConfig
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.SubstanceFrequency
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.testing.services.ServiceUnitTest
import groovy.sql.Sql
import org.slf4j.Logger
import spock.lang.Ignore
import spock.lang.Specification
import spock.util.mop.ConfineMetaClassChanges

import javax.sql.DataSource
import java.sql.ResultSet

//@TestFor(EvdasAlertExecutionService)
//@Mock([ExecutedConfiguration, User, Priority, Disposition, Group, EvdasConfiguration, SubstanceFrequency, ExecutedEvdasConfiguration, ExecutedEvdasConfiguration, EvdasOnDemandAlertService,
//        ConfigurationService, CRUDService, SignalDataSourceService, SingleCaseAlert, Configuration, ExecutionStatus, ExecutedTemplateQuery, ExecutedEVDASDateRangeInformation,
//        EVDASDateRangeInformation, EvdasAlertService, UserService, EvdasSqlGenerationService, EmailNotificationService, ERMRConfig])
@ConfineMetaClassChanges([EvdasConfiguration, EvdasAlertExecutionService])
@Ignore
class EvdasAlertExecutionServiceSpec extends Specification implements ServiceUnitTest<EvdasAlertExecutionService> {

    def resultSet
    ExecutedConfiguration executedConfiguration
    Configuration alertConfiguration
    User user
    Group wfGroup
    Disposition disposition
    Disposition defaultDisposition
    Priority priority
    EvdasConfiguration evdasConfiguration
    ExecutionStatus executionStatus
    EVDASDateRangeInformation evdasDateRangeInformation
    ExecutedEvdasConfiguration executedEvdasConfiguration
    Logger logger

    def setup() {
        logger = Mock(Logger)
        service.log = logger

        priority = new Priority([displayName: "mockPriority", value: "mockPriority", display: true, defaultPriority: true, reviewPeriod: 1])
        priority.save(failOnError: true)
        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, reviewCompleted: true, abbreviation: "vs")
        disposition.save(failOnError: true)

        defaultDisposition = new Disposition(displayName: "New", value: "New", id: 1234, abbreviation: "RR", validatedConfirmed: false)
        defaultDisposition.save(flush: true, failOnError: true)

        wfGroup = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP,
                defaultQualiDisposition: disposition,
                defaultQuantDisposition: disposition,
                defaultAdhocDisposition: disposition,
                defaultEvdasDisposition: disposition,
                defaultLitDisposition: disposition,
                defaultSignalDisposition: disposition,
                autoRouteDisposition: disposition, justificationText: "Update Disposition",
                forceJustification: true, createdBy: "user", modifiedBy: "user")
        wfGroup.save(flush: true)
        //Prepare the mock user
        user = new User(username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.addToGroups(wfGroup)
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.save(flush: true)
        alertConfiguration = new Configuration(id :1,
                executing: false,
                alertTriggerCases: 11,
                alertTriggerDays: 11,
                selectedDatasource: "pva",
                name: "test",
                productSelection: "Test Product A",
                createdBy: user.username, modifiedBy: user.username,
                assignedTo: user,
                priority: priority,
                owner: user, type: 'Single Case Alert'
        )
        alertConfiguration.save(failOnError: true)

        evdasDateRangeInformation = new EVDASDateRangeInformation(dateRangeStartAbsoluteDelta: 2, dateRangeEndAbsoluteDelta: 5, dateRangeEnum: DateRangeEnum.LAST_YEAR,
                dateRangeStartAbsolute: Date.parse('dd-MMM-yyyy', '01-Jan-2014'), dateRangeEndAbsolute: Date.parse('dd-MMM-yyyy', '31-Dec-2014'))

        def now = new Date()
        def recurrenceJSON = /{"startDateTime":"$now","recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;"}/

        evdasConfiguration = new EvdasConfiguration(name: 'test', owner: user, assignedTo: user, createdBy: 'createdBy', dateRangeInformation: evdasDateRangeInformation,
                frequency: "Yearly", modifiedBy: 'modifiedBy', priority: priority, scheduleDateJSON: recurrenceJSON, productSelection: 'productA', dateCreated: new Date(),
                lastUpdated: new Date(), numOfExecutions: 1, adhocRun: true, assignedToGroup: wfGroup, nextRunDate: new Date(), totalExecutionTime: 30L)
        evdasConfiguration.save(flush: true)

        executionStatus = new ExecutionStatus(configId: evdasConfiguration.id,
                startTime: new Date().getTime(),
                endTime: new Date().getTime(), executionStatus: ReportExecutionStatus.COMPLETED,
                reportVersion: evdasConfiguration.numOfExecutions + 1, message: 'for testing purpose',
                owner: user, name: 'executionStatus', type: Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
                nextRunDate: new Date())
        executionStatus.save(flush: true)

        executedConfiguration = new ExecutedConfiguration(name: "test", isLatest: true, adhocRun: false,
                owner: user, scheduleDateJSON: "{}", nextRunDate: new Date(), type: Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
                description: "test", dateCreated: new Date(), lastUpdated: new Date(),
                isPublic: true, isDeleted: false, isEnabled: false, totalExecutionTime: 10,
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
                pvrCaseSeriesId: 1,
                pvrCumulativeCaseSeriesId: 1,
                selectedDatasource: "pva", configId : 1,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 1)
        ExecutedAlertDateRangeInformation executedAlertDateRangeInformation = new ExecutedAlertDateRangeInformation()
        executedAlertDateRangeInformation.dateRangeEnum = DateRangeEnum.CUMULATIVE
        executedAlertDateRangeInformation.dateRangeStartAbsolute = new Date()
        executedAlertDateRangeInformation.dateRangeEndAbsolute = new Date()
        executedConfiguration.executedAlertDateRangeInformation = executedAlertDateRangeInformation
        ExecutedDateRangeInformation executedDateRangeInfoTemplateQuery = new ExecutedDateRangeInformation(
                dateRangeEnum: DateRangeEnum.CUMULATIVE,
                dateRangeEndAbsolute: new Date(), dateRangeStartAbsolute: new Date()
        )
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(
                executedTemplate: 25l, createdBy: user.username, modifiedBy: user.username,
                executedDateRangeInformationForTemplateQuery: executedDateRangeInfoTemplateQuery, headerProductSelection: false,
                headerDateRange: false,
                blindProtected: false,
                privacyProtected: false,
                queryLevel: QueryLevelEnum.CASE
        )
        executedConfiguration.addToExecutedTemplateQueries(executedTemplateQuery)
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
                assignedTo: user,configId: 1,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10)
        executedEvdasConfiguration.save(failOnError: true)

        SubstanceFrequency substanceFrequency = new SubstanceFrequency(name: 'Test Product', startDate: Date.parse('dd-MMM-yyyy', '01-Jan-2014'), endDate: Date.parse('dd-MMM-yyyy', '31-Dec-2014'),
                uploadFrequency: 'Yearly', miningFrequency: 'Yearly', frequencyName: "Yearly", alertType: Constants.AlertConfigType.EVDAS_ALERT)
        substanceFrequency.save(flush: true)

        UserGroupService mockUserGroupService = Mock(UserGroupService)
        mockUserGroupService.fetchUserListForGroup(_) >> {
            return [user]
        }
        service.userGroupService = mockUserGroupService

        def mockResultSet = ['ACTIVE_SUBSTANCES_ID': '1L', 'ACTIVE_SUBSTANCES': 'SubstanceA',
                             'SOCS'                : 'soc', 'HLGTS': 'hlgt', 'hlts': 'hlt', 'SMQ_NARROW': 'SMQ_NARROW',
                             'PTS'                 : 'pts', 'PT_CODE': 'PT', 'IME_DME': 'IME', 'NEW_EV': 'NEW_EV', 'TOT_EV': 'TOT_EV',
                             'NEW_EVPM_LINK'       : 'NEW_EVPM_LINK', 'TOT_EVPM_LINK': 'TOT_EVPM_LINK', 'NEW_EEA': 'NEW_EEA',
                             'TOT_EEA'             : 'TOT_EEA', 'NEW_HCP': 'NEW_HCP', 'TOT_HCP': 'TOT_HCP', 'TOT_SERIOUS': 'TOT_SERIOUS',
                             'NEW_SERIOUS'         : 'NEW_SERIOUS', 'NEW_MED_ERR': 'NEW_MED_ERR', 'TOT_MED_ERR': 'TOT_MED_ERR',
                             'TOT_FATAL'           : 'TOT_FATAL', 'NEW_FATAL': 'NEW_FATAL', 'NEW_LIT': 'NEW_LIT', 'TOT_LIT': 'TOT_LIT',
                             'NEW_PAED'            : 'NEW_PAED', 'TOT_PAED': 'TOT_PAED', 'NEW_OBS': 'NEW_OBS', 'TOT_OBS': 'TOT_OBS', 'NEW_RC': 'NEW_RC',
                             'TOT_RC'              : 'TOT_RC', 'NEW_GERIATR': 'NEW_GERIATR', 'TOT_GERIATR': 'TOT_GERIATR', 'ROR_EUROPE': 'ROR_EUROPE',
                             'ROR_NORTH_AMERICA'   : 'ROR_NORTH_AMERICA', 'ROR_JAPAN': 'ROR_JAPAN', 'ROR_ASIA': 'ROR_ASIA', 'ROR_REST': 'ROR_REST',
                             'ROR_ALL'             : 'ROR_ALL', 'SDR': 'SDR', 'RATIO_ROR_PAED_VS_OTHERS': 'RATIO_ROR_PAED_VS_OTHERS', 'RATIO_ROR_GERIATR_VS_OTHERS': 'RATIO_ROR_GERIATR_VS_OTHERS',
                             'CHANGES'             : 'CHANGES', 'SDR_PAED': 'SDR_PAED', 'SDR_GERIATR': 'SDR_GERIATR', 'NEW_SPONT': 'NEW_SPONT', 'TOT_SPONT': 'TOT_SPONT',
                             'TOT_SPONT_EUROPE'    : 'TOT_SPONT_EUROPE', 'TOT_SPONT_N_AMERICA': 'TOT_SPONT_N_AMERICA', 'TOT_SPONT_JAPAN': 'TOT_SPONT_JAPAN',
                             'TOT_SPONT_ASIA'      : 'TOT_SPONT_ASIA', 'TOT_SPONT_REST': 'TOT_SPONT_REST', 'LISTEDNESS': false]
        def values = mockResultSet.values()
        def rows = []
        rows.add(values)
        resultSet = makeResultSet(mockResultSet.keySet().toList(), rows)
    }

    def cleanup() {
    }

    void "test getExecutionQueueSize"() {
        when:
        int result = service.getExecutionQueueSize()

        then:
        result == 0
    }

    void "test enableExecutedConfiguration in case of Exception"() {
        when:
        service.enableExecutedConfiguration(-1L)

        then:
        _ * logger.error("Error on update rconfig")
    }

    void "test getDetailsUrlMap"() {
        expect:
        service.getDetailsUrlMap(adhocRun) == result

        where:
        adhocRun || result
        true     || "evdas_adhoc_reportRedirectURL"
        false    || "evdas_reportRedirectURL"
    }

    void "test getExecutedDateRangeInformation"() {
        setup:
        ExecutedEVDASDateRangeInformation executedEVDASDateRangeInformation = new ExecutedEVDASDateRangeInformation(dateRangeStartAbsoluteDelta: 2, dateRangeEndAbsoluteDelta: 5, dateRangeEnum: DateRangeEnum.LAST_YEAR)

        when:
        ExecutedEVDASDateRangeInformation result = service.getExecutedDateRangeInformation(evdasDateRangeInformation)

        then:
        result.dateRangeStartAbsoluteDelta == executedEVDASDateRangeInformation.dateRangeStartAbsoluteDelta
        result.dateRangeEndAbsoluteDelta == executedEVDASDateRangeInformation.dateRangeEndAbsoluteDelta
        result.dateRangeEnum == executedEVDASDateRangeInformation.dateRangeEnum
    }

    void "test saveExecutedConfiguration"() {
        when:
        ExecutedEvdasConfiguration result = service.saveExecutedConfiguration(evdasConfiguration)

        then:
        result.name == evdasConfiguration.name
        result.owner == evdasConfiguration.owner
        result.scheduleDateJSON == evdasConfiguration.scheduleDateJSON
        result.frequency == evdasConfiguration.frequency
        result.isEnabled == false
        result.priority == evdasConfiguration.priority
    }

    void "test createExecutedConfiguration"() {
        when:
        ExecutedEvdasConfiguration result = service.createExecutedConfiguration(evdasConfiguration)

        then:
        result.name == evdasConfiguration.name
        result.frequency == evdasConfiguration.frequency
        result.isEnabled == false
        result.priority == evdasConfiguration.priority
    }

    void "test calculateNextDateRangeFrequency when startDate is null"() {
        setup:
        EVDASDateRangeInformation evdasDateRangeInformationNew = new EVDASDateRangeInformation(dateRangeStartAbsoluteDelta: 2, dateRangeEndAbsoluteDelta: 5, dateRangeEnum: DateRangeEnum.CUMULATIVE)
        evdasDateRangeInformationNew.dateRangeStartAbsolute = null
        evdasDateRangeInformationNew.dateRangeEndAbsolute = null
        EvdasConfiguration evdasConfigurationNew = new EvdasConfiguration(name: 'testA', owner: user, assignedTo: user, createdBy: 'createdBy', dateRangeInformation: evdasDateRangeInformationNew,
                frequency: "Yearly", modifiedBy: 'modifiedBy', priority: priority, scheduleDateJSON: "{}", productSelection: 'productA', dateCreated: new Date(),
                lastUpdated: new Date(), numOfExecutions: 1, adhocRun: true, assignedToGroup: wfGroup)
        evdasConfigurationNew.save(flush: true)

        when:
        service.calculateNextDateRangeFrequency(evdasConfigurationNew)

        then:
        evdasConfigurationNew.dateRangeInformation.dateRangeStartAbsolute == Date.parse('dd-MMM-yyyy', '01-Jan-2014')
        evdasConfigurationNew.dateRangeInformation.dateRangeEndAbsolute == Date.parse('dd-MMM-yyyy', '31-Dec-2014')
    }

    void "test calculateNextDateRangeFrequency"() {
        when:
        service.calculateNextDateRangeFrequency(evdasConfiguration)

        then:
        evdasConfiguration.dateRangeInformation.dateRangeStartAbsolute == Date.parse('dd-MMM-yyyy', '01-Jan-2015')
        evdasConfiguration.dateRangeInformation.dateRangeEndAbsolute == Date.parse('dd-MMM-yyyy', '31-Dec-2015')
    }

    void "test createExecutionStatus"() {
        setup:
        EvdasConfiguration.metaClass.static.lock = EvdasConfiguration.&get
        CRUDService mockCRUDService = Mock(CRUDService)
        mockCRUDService.update(evdasConfiguration) >> {
            return evdasConfiguration
        }
        service.CRUDService = mockCRUDService

        when:
        ExecutionStatus result = service.createExecutionStatus(evdasConfiguration)

        then:
        result.reportVersion == evdasConfiguration.numOfExecutions + 1
        result.type == Constants.AlertConfigType.EVDAS_ALERT
        result.frequency == FrequencyEnum.RUN_ONCE
    }

    void "test setNextRunDateForConfiguration when adhocRun set to true"() {
        when:
        service.setNextRunDateForConfiguration(evdasConfiguration)

        then:
        evdasConfiguration.nextRunDate == null
        evdasConfiguration.isEnabled == false
    }


    void "test setNextRunDateForConfiguration when adhocRun set to false"() {
        setup:
        EvdasConfiguration evdasConfiguration = new EvdasConfiguration(name: 'testA', owner: user, assignedTo: user, createdBy: 'createdBy', dateRangeInformation: evdasDateRangeInformation,
                frequency: "Yearly", modifiedBy: 'modifiedBy', priority: priority, scheduleDateJSON: "{}", productSelection: 'productA', dateCreated: new Date(),
                lastUpdated: new Date(), numOfExecutions: 1, adhocRun: false, assignedToGroup: wfGroup)
        evdasConfiguration.save(flush: true)

        ConfigurationService mockConfigurationService = Mock(ConfigurationService)
        mockConfigurationService.getNextDate(evdasConfiguration) >> {
            return Date.parse('dd-MMM-yyyy', '01-Jan-2015')
        }
        service.configurationService = mockConfigurationService

        when:
        service.setNextRunDateForConfiguration(evdasConfiguration)

        then:
        evdasConfiguration.nextRunDate == Date.parse('dd-MMM-yyyy', '01-Jan-2015')
    }

    void "test setTotalExecutionTimeForConfiguration"() {
        setup:
        Long executionTime = 10L

        CRUDService mockCRUDService = Mock(CRUDService)
        mockCRUDService.update(evdasConfiguration) >> {
            return evdasConfiguration
        }
        service.CRUDService = mockCRUDService

        when:
        service.setTotalExecutionTimeForConfiguration(evdasConfiguration, executionTime)

        then:
        evdasConfiguration.totalExecutionTime == 40L
    }

    void "test addNotification for completed status"() {
        when:
        service.addNotification(executedEvdasConfiguration, evdasConfiguration, executionStatus, user)

        then:
        noExceptionThrown()
    }

    void "test addNotification for Warn Status"() {
        setup:
        ExecutionStatus executionStatus1 = new ExecutionStatus(configId: alertConfiguration.id,
                startTime: new Date().getTime(),
                endTime: new Date().getTime(), executionStatus: ReportExecutionStatus.WARN,
                reportVersion: 1, message: 'for testing purpose',
                owner: user, name: 'executionStatus', type: Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
                nextRunDate: new Date())
        executionStatus1.save(flush: true)

        when:
        service.addNotification(executedEvdasConfiguration, evdasConfiguration, executionStatus1, user)

        then:
        noExceptionThrown()
    }

    void "test addNotification for Exception thrown"() {
        when:
        service.addNotification(executedEvdasConfiguration, null, executionStatus, user)

        then:
        logger.info("""Error creating Notification: """)
    }

    void "test addNotification for Other Status"() {
        setup:
        ExecutionStatus executionStatus2 = new ExecutionStatus(configId: alertConfiguration.id,
                startTime: new Date().getTime(),
                endTime: new Date().getTime(), executionStatus: ReportExecutionStatus.ERROR,
                reportVersion: 1, message: 'for testing purpose',
                owner: user, name: 'executionStatus', type: Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
                nextRunDate: new Date())
        executionStatus2.save(flush: true)

        when:
        service.addNotification(executedEvdasConfiguration, evdasConfiguration, executionStatus2, user)

        then:
        noExceptionThrown()
    }

    void "test debugReportSQL in case of Exception"() {
        setup:
        SignalDataSourceService signalDataSourceService = Mock(SignalDataSourceService)
        signalDataSourceService.getReportConnection(_) >> {
            throw new Exception()
        }
        service.signalDataSourceService = signalDataSourceService

        when:
        service.debugReportSQL(evdasConfiguration)

        then:
        thrown Exception
    }

    void "test debugReportSQL"() {
        setup:
        def dataSource = Mock(DataSource)
        service.signalDataSourceService = [getReportConnection: { String selectedDatasource -> return dataSource }]
        EvdasSqlGenerationService mockService = Mock(EvdasSqlGenerationService)
        mockService.initializeInsertGtts(evdasConfiguration) >> {
            return "Begin execute immediate('delete from gtt_report_input_params'); "
        }
        mockService.initializeQuerySql(evdasConfiguration) >> {
            return """ BEGIN delete from GTT_QUERY_DETAIL delete from GTT_QUERY_SETS delete from GTT_REPORT_VAR_INPUT; """
        }
        mockService.getEvdasQuerySql() >> {
            return "{call pkg_create_report_sql_evd.p_main_query(?,?)}"
        }
        service.evdasSqlGenerationService = mockService

        when:
        Map result = service.debugReportSQL(evdasConfiguration)

        then:
        result.gttInserts.trim() == "Begin execute immediate('delete from gtt_report_input_params'); ".trim()
        result.queryInserts.trim() == """ BEGIN delete from GTT_QUERY_DETAIL delete from GTT_QUERY_SETS delete from GTT_REPORT_VAR_INPUT; """.trim()
        result.evdasSql.trim() == "{call pkg_create_report_sql_evd.p_main_query(?,?)}".trim()
    }

    void "test setValuesForConfiguration"() {
        setup:
        EvdasConfiguration evdasConfiguration = new EvdasConfiguration(name: 'testA', owner: user, assignedTo: user, createdBy: 'createdBy', dateRangeInformation: evdasDateRangeInformation,
                frequency: "Yearly", modifiedBy: 'modifiedBy', priority: priority, scheduleDateJSON: "{}", productSelection: 'productA', dateCreated: new Date(),
                lastUpdated: new Date(), numOfExecutions: 1, adhocRun: false, assignedToGroup: wfGroup)
        evdasConfiguration.save(flush: true)
        EvdasConfiguration.metaClass.static.lock = EvdasConfiguration.&get

        when:
        service.setValuesForConfiguration(evdasConfiguration, executedEvdasConfiguration)

        then:
        evdasConfiguration.totalExecutionTime == executionStatus.startTime - executionStatus.endTime
        executionStatus.executionStatus == ReportExecutionStatus.COMPLETED
        evdasConfiguration.executing == false
    }

    void "test adjustCustomDateRanges"() {
        when:
        service.adjustCustomDateRanges(evdasConfiguration)

        then:
        evdasConfiguration.dateRangeInformation.dateRangeStartAbsolute == Date.parse('dd-MMM-yyyy', '01-Jan-2015')
        evdasConfiguration.dateRangeInformation.dateRangeEndAbsolute == Date.parse('dd-MMM-yyyy', '31-Dec-2015')
    }

    void "test adjustCustomDateRanges for no frequency"() {
        setup:
        def now = new Date()
        def recurrenceJSON = /{"startDateTime":"$now","recurrencePattern":"FREQ=WEEKLY;INTERVAL=1;COUNT=1;"}/
        EvdasConfiguration evdasConfiguration = new EvdasConfiguration(name: 'testA', owner: user, assignedTo: user, createdBy: 'createdBy', dateRangeInformation: evdasDateRangeInformation,
                modifiedBy: 'modifiedBy', priority: priority, scheduleDateJSON: recurrenceJSON, productSelection: 'productA', dateCreated: new Date(), isEnabled: true,
                lastUpdated: new Date(), numOfExecutions: 1, adhocRun: false, assignedToGroup: wfGroup)
        evdasConfiguration.save(flush: true)

        when:
        service.adjustCustomDateRanges(evdasConfiguration)

        then:
        evdasConfiguration.dateRangeInformation.dateRangeStartAbsolute == Date.parse('dd-MMM-yyyy', '01-Jan-2014')
        evdasConfiguration.dateRangeInformation.dateRangeEndAbsolute == Date.parse('dd-MMM-yyyy', '31-Dec-2014')
    }

    void "test executeAlertJob in case of Exception"() {
        setup:
        EvdasConfiguration.metaClass.static.lock = EvdasConfiguration.&get
        String expectedErrorMsg = "No bean named 'dataSource_eudra' available"
        ExecutionStatusException executionStatusException = new ExecutionStatusException(errorCause: 'Unavailability of datasource')

        when:
        service.executeAlertJob(evdasConfiguration, {}, { Long id1 = 1L, Long id2 = 1L, ExecutionStatusException ese -> executionStatusException })

        then:
        _ * logger.error("Error occured while running EvdasAlertConfiguration.", expectedErrorMsg)
    }


    void "test executeAlertJob"() {
        setup:
        EvdasConfiguration.metaClass.static.lock = EvdasConfiguration.&get
        ExecutionStatusException executionStatusException = new ExecutionStatusException(errorCause: 'Unavailability of datasource')
        service.signalDataSourceService = [getDataSource: { String selectedDatasource -> }]
        Sql.metaClass.call = { String sql -> }
        EvdasSqlGenerationService mockService = Mock(EvdasSqlGenerationService)
        mockService.initializeInsertGtts(evdasConfiguration) >> {
            return "Begin execute immediate('delete from gtt_report_input_params'); "
        }
        mockService.initializeQuerySql(evdasConfiguration) >> {
            return """ BEGIN delete from GTT_QUERY_DETAIL delete from GTT_QUERY_SETS delete from GTT_REPORT_VAR_INPUT; """
        }
        mockService.getEvdasQuerySql() >> {
            return "{call pkg_create_report_sql_evd.p_main_query(?,?)}"
        }
        service.evdasSqlGenerationService = mockService

        when:
        service.executeAlertJob(evdasConfiguration, {}, { Long id1 = 1L, Long id2 = 1L, ExecutionStatusException ese -> executionStatusException })

        then:
        _ * logger.info("GTT Inserts executed.")
        _ * logger.info("Query Inserts executed.")
        _ * logger.info("Evdas SQLs executed.")
    }

    void "test handleFailedExecution"() {
        setup:
        ExecutionStatus executionStatus = new ExecutionStatus(configId: evdasConfiguration.id,
                startTime: new Date().getTime(),
                endTime: new Date().getTime(), executionStatus: ReportExecutionStatus.COMPLETED,
                reportVersion: evdasConfiguration.numOfExecutions + 1, message: 'for testing purpose',
                owner: user, name: 'executionStatus', type: Constants.AlertConfigType.EVDAS_ALERT,
                nextRunDate: new Date())
        executionStatus.save(flush: true)
        ExecutionStatusException executionStatusException = new ExecutionStatusException(errorCause: 'Unavailability of datasource')

        when:
        service.handleFailedExecution(evdasConfiguration.id, executedEvdasConfiguration.id, executionStatusException)

        then:
        executionStatus.message == 'No bean named Datasource available'
        executionStatus.queryId == 1L
        executionStatus.executionStatus == ReportExecutionStatus.ERROR
        evdasConfiguration.nextRunDate == null
        evdasConfiguration.executing == false
        executedEvdasConfiguration.isEnabled == false
    }

    void "test handleFailedException in case of no Execution Status"() {
        setup:
        ExecutionStatusException executionStatusException = new ExecutionStatusException(errorCause: 'Unavailability of datasource')
        EmailNotificationService mockEmailNotificationService = Mock(EmailNotificationService)
        mockEmailNotificationService.emailHanlderAtAlertLevel(executedEvdasConfiguration, executionStatus) >> {
            return true
        }
        service.emailNotificationService = mockEmailNotificationService

        when:
        service.handleFailedExecution(evdasConfiguration.id, executedEvdasConfiguration.id, executionStatusException)

        then:
        1 * logger.error("Cannot find the execution status. [handleFailedExecution]")
    }

    void "test handleFailedException in case of Exception"() {
        setup:
        ExecutionStatus executionStatus = new ExecutionStatus(configId: evdasConfiguration.id,
                startTime: new Date().getTime(),
                endTime: new Date().getTime(), executionStatus: ReportExecutionStatus.COMPLETED,
                reportVersion: evdasConfiguration.numOfExecutions + 1, message: 'for testing purpose',
                owner: user, name: 'executionStatus', type: Constants.AlertConfigType.EVDAS_ALERT,
                nextRunDate: new Date())
        executionStatus.save(flush: true)
        ExecutionStatusException executionStatusException = new ExecutionStatusException(errorCause: 'Unavailability of datasource')

        when:
        service.handleFailedExecution(evdasConfiguration.id, executedEvdasConfiguration.id, executionStatusException)

        then:
        _ * logger.error('Error happened when handling failed Configurations [1]', 'java.lang.NullPointerException: Cannot invoke method isEmailNotificationEnabled() on null object')
        _ * logger.info('Error creating Notification: No message found under code \'app.notification.failed\' for locale \'en\'.')
    }

    void "test runConfigurations in case of Exception"() {
        setup:
        EvdasConfiguration.metaClass.static.getNextConfigurationToExecute = { evdasConfiguration }

        when:
        service.runConfigurations()

        then:
        logger.info("Found test 1L to execute.")
        evdasConfiguration.executing == false
        executionStatus.executionStatus != ReportExecutionStatus.ERROR
        service.currentlyRunning == []
        logger.error("Exception in Executor Service")
        logger.info("Execution is Done")
    }

    void "test runConfigurations"() {
        setup:
        EvdasConfiguration.metaClass.static.getNextConfigurationToExecute = { evdasConfiguration }
        EvdasConfiguration.metaClass.static.lock = EvdasConfiguration.&get
        CRUDService mockCRUDService = Mock(CRUDService)
        mockCRUDService.update(evdasConfiguration) >> {
            return evdasConfiguration
        }
        service.CRUDService = mockCRUDService

        when:
        service.runConfigurations()

        then:
        service.currentlyRunning == []
        logger.info("Execution is Done")
    }

    void "test readEvdasData"() {
        setup:
        ERMRConfig ermrConfig1 = new ERMRConfig(targetColumnName: 'ACTIVE_SUBSTANCES')
        ERMRConfig ermrConfig2 = new ERMRConfig(targetColumnName: 'SMQ_NARROW')
        ERMRConfig ermrConfig3 = new ERMRConfig(targetColumnName: 'PT_CODE')
        List<ERMRConfig> eRMRConfigMapping = [ermrConfig1, ermrConfig2, ermrConfig3]

        when:
        Map result = service.readEvdasData(resultSet, eRMRConfigMapping)

        then:
        result.substanceId == '1L'
        result.substance == 'SubstanceA'
        result.soc == 'soc'
        result.hlgt == 'hlgt'
        result.smqNarrow == 'SMQ_NARROW'
        result.listedness == false
        result.attributes == [:]
    }


    void "test readEvdasData in case of Empty eRMRConfigMapping List"() {
        setup:
        List<ERMRConfig> eRMRConfigMapping = []

        when:
        Map result = service.readEvdasData(resultSet, eRMRConfigMapping)

        then:
        result.substanceId == '1L'
        result.substance == 'SubstanceA'
        result.soc == 'soc'
        result.hlgt == 'hlgt'
        result.smqNarrow == 'SMQ_NARROW'
        result.listedness == false
        result.attributes == [:]
    }

    private ResultSet makeResultSet(List<String> aColumns, List rows) {
        ResultSet result = Mock()
        int currentIndex = -1
        result.next() >> { ++currentIndex < rows.size() }
        result./get(String|Short|Date|Int|Timestamp)/(_) >> { String argument ->
            rows[currentIndex][aColumns.indexOf(argument)]
        }
        return result
    }
}