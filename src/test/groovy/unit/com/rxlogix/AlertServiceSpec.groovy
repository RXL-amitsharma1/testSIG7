package unit.com.rxlogix

import com.rxlogix.*
import com.rxlogix.cache.CacheService
import com.rxlogix.config.*
import com.rxlogix.dto.AlertDataDTO
import com.rxlogix.dto.AlertLevelDispositionDTO
import com.rxlogix.enums.*
import com.rxlogix.signal.*
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.user.UserGroupMapping
import grails.test.hibernate.HibernateSpec
import grails.testing.services.ServiceUnitTest
import groovy.json.JsonSlurper
import groovy.sql.Sql
import io.reactivex.Single
import org.hibernate.SessionFactory
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

import javax.wsdl.Service
import javax.xml.stream.XMLStreamException

@ConfineMetaClassChanges([Sql])
class AlertServiceSpec extends Specification implements ServiceUnitTest<AlertService> {
    def domain
    Map<String, String> iLikePropertiesMap
    Map<String, String> notILikePropertiesMap
    @Shared
    Map aliasPropertiesTagMap
    @Shared
    Map aliasPropertiesSignalMap
    @Shared
    Map aliasPropertiesAggSignalMap
    @Shared
    ExecutedConfiguration prevExecutedConfiguration
    ExecutedConfiguration executedConfiguration
    @Shared
    Configuration alertConfiguration
    User user
    Group wfGroup
    @Shared
    Disposition disposition
    Disposition defaultDisposition
    @Shared
    SingleCaseAlert singleCaseAlert
    @Shared
    SingleCaseAlert singleCaseAlertNew
    @Shared
    SingleCaseAlert alert
    @Shared
    ExecutedLiteratureConfiguration executedLiteratureConfiguration
    Priority priority
    ExecutedEvdasConfiguration executedEvdasConfiguration
    EvdasConfiguration evdasConfiguration
    AlertLevelDispositionDTO alertLevelDispositionDTO
    Map alertMap
    AlertComment comment1, comment2

    List<Class> getDomainClasses() {
        [ExecutedConfiguration, User, Priority, AdHocAlert, Alert, Disposition, Group, AdHocAlert,ExecutedLiteratureConfiguration,
         EvdasConfiguration, ExecutedEvdasConfiguration, SingleCaseAlert, Configuration, ExecutionStatus, EmergingIssue,ExecutedTemplateQuery]
    }

    def setup() {
        service.transactionManager = transactionManager
        service.sessionFactory = Mock(SessionFactory)
        priority = new Priority([displayName: "mockPriority", value: "mockPriority", display: true, defaultPriority: true, reviewPeriod: 1])
        priority.save(failOnError: true)
        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, reviewCompleted: true, abbreviation: "vs")
        disposition.save(failOnError: true)

        defaultDisposition = new Disposition(displayName: "New", value: "New", id: 1234, abbreviation: "RR", validatedConfirmed: false)
        defaultDisposition.save(flush: true, failOnError: true)

        iLikePropertiesMap = [
                'CONTAINS'  : "%test%",
                'START_WITH': "test%",
                'ENDS_WITH' : "%test"
        ]
        notILikePropertiesMap = [
                'DOES_NOT_CONTAIN': "%test%",
                'DOES_NOT_START'  : "test%",
                'DOES_NOT_END'    : "%test"
        ]
        aliasPropertiesTagMap = [columnName: 'TAG_TEXT', subQuery: """ SELECT Single_alert_id FROM single_global_tag_mapping sgtm 
                                                                   LEFT JOIN single_case_all_tag scat ON sgtm.single_global_id = scat.id"""]

        aliasPropertiesSignalMap = [columnName: 'NAME', subQuery: """ SELECT Single_alert_id FROM VALIDATED_SINGLE_ALERTS vsa
                                                                   LEFT JOIN Validated_Signal vs ON vsa.VALIDATED_SIGNAL_ID = vs.id"""
        ]
        aliasPropertiesAggSignalMap = [columnName: 'NAME', subQuery: """ SELECT AGG_ALERT_ID FROM VALIDATED_AGG_ALERTS vsa
                                                                   LEFT JOIN Validated_Signal vs ON vsa.VALIDATED_SIGNAL_ID = vs.id"""
        ]

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
        UserGroupMapping userGroupMapping = new UserGroupMapping(user: user, group: wfGroup)
        userGroupMapping.save(flush: true, failOnError: true)
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.save(flush: true)
        alertConfiguration = new Configuration(
                executing: false,
                alertTriggerCases: 11,
                alertTriggerDays: 11,
                selectedDatasource: "pva",
                name: "test",
                productSelection: "Test Product A",
                scheduleDateJSON: "{}",
                createdBy: user.username, modifiedBy: user.username,
                assignedTo: user,
                priority: priority,
                owner: user, type: 'Single Case Alert'
        )
        alertConfiguration.save(failOnError: true)

        executedEvdasConfiguration = new ExecutedEvdasConfiguration(name: "test",
                owner: user, scheduleDateJSON: "{}", nextRunDate: new Date(),
                description: "test", dateCreated: new Date(), lastUpdated: new Date(),
                isPublic: true, isDeleted: true, isEnabled: true,
                productSelection: "['testproduct2']", eventSelection: "['rash']",
                configSelectedTimeZone: "UTC",
                createdBy: user.username, modifiedBy: user.username,configId: alertConfiguration.id,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10)
        executedEvdasConfiguration.save(flush: true)

        EVDASDateRangeInformation evdasDateRangeInformation = new EVDASDateRangeInformation(dateRangeStartAbsoluteDelta: 2, dateRangeEndAbsoluteDelta: 5)

        evdasConfiguration = new EvdasConfiguration(name: 'test', owner: user, assignedTo: user, createdBy: 'createdBy', dateRangeInformation: evdasDateRangeInformation,
                modifiedBy: 'modifiedBy', priority: priority, productSelection: 'productA')
        evdasConfiguration.save(flush: true)

        prevExecutedConfiguration = new ExecutedConfiguration(name: "test",
                owner: user, scheduleDateJSON: "{}", nextRunDate: new Date(),
                description: "test", dateCreated: new Date(), lastUpdated: new Date(),
                isPublic: true, isDeleted: false, isEnabled: true, spotfireSettings: "{\"type\":\"VACCINCE\",\"rangeType\":[\"PR_DATE_RANGE\",\"CUMULATIVE\"]}",
                dateRangeType: DateRangeTypeCaseEnum.CASE_LOCKED_DATE,
                productSelection: "['testproduct2']", eventSelection: "['rash']", studySelection: "['test']",
                configSelectedTimeZone: "UTC",
                evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION,
                limitPrimaryPath: true,
                includeMedicallyConfirmedCases: true,
                excludeFollowUp: false, includeLockedVersion: true,
                adjustPerScheduleFrequency: true,
                createdBy: user.username, modifiedBy: user.username,configId: alertConfiguration.id,
                assignedTo: user, type: 'Single Case Alert', adhocRun: false, isLatest: true,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10, pvrCaseSeriesId: 9l)
        ExecutedAlertDateRangeInformation prevExecutedAlertDateRangeInformation = new ExecutedAlertDateRangeInformation()
        prevExecutedAlertDateRangeInformation.dateRangeEnum = DateRangeEnum.CUMULATIVE
        prevExecutedAlertDateRangeInformation.dateRangeStartAbsoluteDelta = 3
        prevExecutedAlertDateRangeInformation.dateRangeEndAbsoluteDelta = 6
        prevExecutedConfiguration.executedAlertDateRangeInformation = prevExecutedAlertDateRangeInformation
        prevExecutedConfiguration.save(failOnError: true)

        executedConfiguration = new ExecutedConfiguration(name: "test",
                owner: user, scheduleDateJSON: "{}", nextRunDate: new Date(), spotfireSettings: "{\"type\":\"VACCINCE\",\"rangeType\":[\"PR_DATE_RANGE\",\"CUMULATIVE\"]}",
                description: "test", dateCreated: new Date(), lastUpdated: new Date(),
                isPublic: true, isDeleted: false, isEnabled: true,
                dateRangeType: DateRangeTypeCaseEnum.CASE_LOCKED_DATE,
                productSelection: "['testproduct2']", eventSelection: "['rash']", studySelection: "['test']",
                configSelectedTimeZone: "UTC",
                evaluateDateAs: EvaluateCaseDateEnum.LATEST_VERSION,
                limitPrimaryPath: true,
                includeMedicallyConfirmedCases: true,
                excludeFollowUp: false, includeLockedVersion: true,configId: alertConfiguration.id,
                adjustPerScheduleFrequency: true,
                createdBy: user.username, modifiedBy: user.username,
                assignedTo: user, type: 'Single Case Alert', adhocRun: false,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10)
        ExecutedAlertDateRangeInformation executedAlertDateRangeInformation = new ExecutedAlertDateRangeInformation()
        executedAlertDateRangeInformation.dateRangeEnum = DateRangeEnum.LAST_YEAR
        executedAlertDateRangeInformation.dateRangeStartAbsoluteDelta = 2
        executedAlertDateRangeInformation.dateRangeEndAbsoluteDelta = 5
        executedConfiguration.executedAlertDateRangeInformation = executedAlertDateRangeInformation
        ExecutedDateRangeInformation executedDateRangeInfoTemplateQuery = new ExecutedDateRangeInformation(
                dateRangeEnum: DateRangeEnum.CUMULATIVE,
                dateRangeEndAbsolute: new Date(), dateRangeStartAbsolute: new Date(), executedAsOfVersionDate: new Date()
        )
        ExecutedTemplateQuery executedTemplateQuery = new ExecutedTemplateQuery(
                executedTemplate: 25l, createdBy: user.username, modifiedBy: user.username,
                executedDateRangeInformationForTemplateQuery: executedDateRangeInfoTemplateQuery, headerProductSelection: false,
                headerDateRange: false,
                blindProtected: false,
                privacyProtected: false,
                queryLevel: QueryLevelEnum.CASE
        )
        executedDateRangeInfoTemplateQuery.executedTemplateQuery = executedTemplateQuery
        executedConfiguration.addToExecutedTemplateQueries(executedTemplateQuery)
        executedConfiguration.save(failOnError: true)

        Map attrMapObj = ['masterFollowupDate_5' : new Date(),
                          'masterRptTypeId_3'    : "test type",
                          'masterInitReptDate_4' : new Date(),
                          'masterFollowupDate_5' : new Date(),
                          'reportersHcpFlag_2'   : "true",
                          'masterProdTypeList_6' : "test",
                          'masterPrefTermAll_7'  : "test",
                          'assessOutcome'        : "Death",
                          'assessListedness_9'   : "test",
                          'assessAgentSuspect_10': "test"]

        alert = new SingleCaseAlert(productSelection: "something",
                detectedDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                name: "Test Name",
                caseNumber: "1S01",
                caseVersion: 1,
                alertConfiguration: alertConfiguration,
                executedAlertConfiguration: prevExecutedConfiguration,
                reviewDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                priority: alertConfiguration.priority,
                disposition: disposition,
                assignedTo: user,
                productName: "Test Product A",
                pt: "Rash",
                createdBy: alertConfiguration.assignedTo.username,
                modifiedBy: alertConfiguration.assignedTo.username,
                dateCreated: alertConfiguration.dateCreated,
                lastUpdated: alertConfiguration.dateCreated,
                productFamily: "Test Product A",
                isNew: true,
                productId: 3982,
                comboFlag: "flag1",
                malfunction: "function1",
                followUpExists: true,
                attributesMap: attrMapObj)
        alert.save(failOnError: true)

        singleCaseAlert = new SingleCaseAlert()
        singleCaseAlertNew = new SingleCaseAlert(isNew: true)

        executedLiteratureConfiguration = new ExecutedLiteratureConfiguration(id: 1L, name: "test",
                owner: user, assignedTo: user,
                scheduleDateJSON: "{}",
                nextRunDate: new Date(),
                description: "test",
                dateCreated: new Date(),
                lastUpdated: new Date(),
                isPublic: true,
                isDeleted: true,
                isEnabled: true,
                productSelection: "['testproduct2']",
                eventSelection: "['rash']",
                searchString: "['test']",
                createdBy: user.username,
                modifiedBy: user.username,
                workflowGroup: wfGroup,
                assignedToGroup: wfGroup,
                totalExecutionTime: 0,
                isLatest: true,
                configId:111,
                requiresReviewCount: '100',
                selectedDatasource: "pubmed")
        executedLiteratureConfiguration.save(failOnError: true, flush: true)
        comment1 = new AlertComment(productId: 10l, eventName: "Rash", comments: "comm-1",createdBy:"signaldev",modifiedBy:"signaldev",
                configId: alertConfiguration.id, alertType: Constants.AlertConfigType.AGGREGATE_CASE_ALERT)
        comment1.save(flush: true, failOnError:true)
        comment2 = new AlertComment(productName: "aj", eventName: "Rash", comments: "comm-2",createdBy:"signaldev",modifiedBy:"signaldev",
                configId: alertConfiguration.id, alertType: Constants.AlertConfigType.EVDAS_ALERT)
        comment2.save(flush: true, failOnError:true)

        UserService mockUserService = Mock(UserService)
        mockUserService.getUserFromCacheByUsername(_) >> {
            return user
        }
        mockUserService.getCurrentUserName() >> {
            return user.username
        }
        mockUserService.getCurrentUserId() >> {
            return user.id
        }
        mockUserService.getUser() >> {
            return user
        }
        service.userService = mockUserService

        ActivityType activityType = new ActivityType(value: ActivityTypeValue.DispositionChange)
        activityType.save(failOnError: true)

        alertLevelDispositionDTO = new AlertLevelDispositionDTO(domainName: SingleCaseAlert, execConfigId: prevExecutedConfiguration.id, justificationText: 'text',
                execConfig: prevExecutedConfiguration, reviewCompletedDispIdList: [defaultDisposition.id], workflowGroupId: wfGroup.id, activityType: activityType, targetDisposition: disposition)

        alertMap = [caseNumber: 1213, displayName: 'display', productSelection: 'ProductA,ProductB', details: 'New Product', eventSelection: 'EventA,EventB', pt: 'Rash', productName: 'Test Product A']


        CacheService mockCacheService = Mock(CacheService)
        mockCacheService.getAllGroups() >> {
            return [id: 1L, text: 'ProductA']
        }
        mockCacheService.getAllUsers() >> {
            return [id: 1L, text: 'ValueA']
        }
        service.cacheService = mockCacheService
    }

    def "generateEventJSON"() {
        when:
        String eventJson = service.generateEventJSON("Application site rash", 1, "4")
        then:
        eventJson == '{"1":[],"2":[],"3":[],"4":[{"name":"Application site rash","id":"1"}],"5":[],"6":[],"7":[],"8":[]}'
    }

    def "generateEventJSON when pt is null"() {
        when:
        String eventJson = service.generateEventJSON(null, 1, null)
        then:
        eventJson == null
    }

    def "generateEventJSON when ptCode is null"() {
        when:
        String eventJson = service.generateEventJSON("Rash", null, null)
        then:
        eventJson == null
    }

    def "generateEventJSONForSMQ For Broad"() {
        when:
        String eventJson = service.generateEventJSONForSMQ("Hostility/aggression (SMQ) (Broad)", 20000142)
        then:
        eventJson == '{"1":[],"2":[],"3":[],"4":[],"5":[],"6":[],"7":[{"name":"Hostility/aggression (SMQ) ","id":"20000142"}],"8":[]}'
    }

    def "generateEventJSONForSMQ For Narrow"() {
        when:
        String eventJson = service.generateEventJSONForSMQ("Cardiac arrhythmia terms (incl bradyarrhythmias and tachyarrhythmias) (SMQ) (Narrow)", 20000050)
        then:
        eventJson == '{"1":[],"2":[],"3":[],"4":[],"5":[],"6":[],"7":[],"8":[{"name":"Cardiac arrhythmia terms (incl bradyarrhythmias and tachyarrhythmias) (SMQ) ","id":"20000050"}]}'
    }

    def "generateEventJSONForSMQ For when pt is null"() {
        when:
        String eventJson = service.generateEventJSONForSMQ(null, 20000050)
        then:
        eventJson == null
    }

    def "generateEventJSONForSMQ For when ptCode is null"() {
        when:
        String eventJson = service.generateEventJSONForSMQ("Rash", null)
        then:
        eventJson == null
    }

    def "generateAdvancedFilterClosure for subGroup"() {
        setup:
        Closure advancedFilterClosure
        AlertDataDTO alertDataDTO = new AlertDataDTO(params: [:])
        def advancedFilterService = Mock(AdvancedFilterService)
        advancedFilterService.createAdvancedFilterCriteria(_) >> "{ ->\n" +
                "criteriaConditionsForSubGroup('EBGM:Confident','EQUALS',\"153\")\n" +
                "}"
        service.advancedFilterService = advancedFilterService
        alertDataDTO.params.queryJSON = "{\"all\":{\"containerGroups\":[ {\"expressions\":[ {\"index\":\"0\",\"field\":\"EBGM:Confident\",\"op\":\"EQUALS\",\"value\":\"153\"} ] }  ] } }"
        when:
        Closure result = service.generateAdvancedFilterClosure(alertDataDTO, advancedFilterClosure)
        then:
        result != null
    }

    def "generateAdvancedFilterClosure for null"() {
        setup:
        Closure advancedFilterClosure
        AlertDataDTO alertDataDTO = new AlertDataDTO(params: [:])
        alertDataDTO.params.queryJSON = null
        alertDataDTO.params.advancedFilterId = null
        when:
        Closure result = service.generateAdvancedFilterClosure(alertDataDTO, advancedFilterClosure)
        then:
        result == null
    }

    def "check setImpEventValue Method"() {
        setup:
        String alertPt = 'pyrexia'
        List eiList = [[
                               eventName: ['pyrexia', 'rash', 'fever'],
                               dme      : 'dme',
                               ei       : "",
                               ime      : "",
                               sm       : 'sm'
                       ]]
        when:
        String result = service.setImpEventValue(alertPt, eiList)

        then:
        result == 'dme,sm'
    }

    def "check setImpEventValue Method when eiList is empty"() {
        setup:
        String alertPt = 'pyrexia'
        List eiList = []
        when:
        String result = service.setImpEventValue(alertPt, eiList)

        then:
        result == ''
    }

    def "check setImpEventValue Method when alertPt is null"() {
        setup:
        String alertPt = null
        List eiList = [[
                               eventName: ['pyrexia', 'rash', 'fever'],
                               dme      : 'dme',
                               ei       : "",
                               ime      : "",
                               sm       : 'sm'
                       ]]
        when:
        String result = service.setImpEventValue(alertPt, eiList)

        then:
        result == ''
    }

    void "generateSqlRestrictionSql for In clause"() {
        setup:
        List valuesList = ['Tags']
        expect:
        result == service.generateSqlRestrictionSql(AdvancedFilterOperatorEnum.EQUALS, valuesList, iLikePropertiesMap, notILikePropertiesMap, aliasPropertiesMap)
        where:
        aliasPropertiesMap          || result
        aliasPropertiesTagMap       || """{alias}.id IN (
                                             SELECT Single_alert_id FROM single_global_tag_mapping sgtm 
                                                                   LEFT JOIN single_case_all_tag scat ON sgtm.single_global_id = scat.id
                                            WHERE upper(TAG_TEXT) IN ('TAGS') )
                                   """
        aliasPropertiesSignalMap    || """{alias}.id IN (
                                             SELECT Single_alert_id FROM VALIDATED_SINGLE_ALERTS vsa
                                                                   LEFT JOIN Validated_Signal vs ON vsa.VALIDATED_SIGNAL_ID = vs.id
                                            WHERE upper(NAME) IN ('TAGS') )
                                   """
        aliasPropertiesAggSignalMap || """{alias}.id IN (
                                             SELECT AGG_ALERT_ID FROM VALIDATED_AGG_ALERTS vsa
                                                                   LEFT JOIN Validated_Signal vs ON vsa.VALIDATED_SIGNAL_ID = vs.id
                                            WHERE upper(NAME) IN ('TAGS') )
                                   """
    }

    void "generateAdvancedFilterTagSql for Not In clause"() {
        setup:
        List valuesList = ['Tags']
        expect:
        result == service.generateSqlRestrictionSql(AdvancedFilterOperatorEnum.NOT_EQUAL, valuesList, iLikePropertiesMap, notILikePropertiesMap, aliasPropertiesMap)
        where:
        aliasPropertiesMap          || result
        aliasPropertiesTagMap       || """{alias}.id NOT IN (
                                             SELECT Single_alert_id FROM single_global_tag_mapping sgtm 
                                                                   LEFT JOIN single_case_all_tag scat ON sgtm.single_global_id = scat.id
                                            WHERE upper(TAG_TEXT) IN ('TAGS') )
                                   """
        aliasPropertiesSignalMap    || """{alias}.id NOT IN (
                                             SELECT Single_alert_id FROM VALIDATED_SINGLE_ALERTS vsa
                                                                   LEFT JOIN Validated_Signal vs ON vsa.VALIDATED_SIGNAL_ID = vs.id
                                            WHERE upper(NAME) IN ('TAGS') )
                                   """
        aliasPropertiesAggSignalMap || """{alias}.id NOT IN (
                                             SELECT AGG_ALERT_ID FROM VALIDATED_AGG_ALERTS vsa
                                                                   LEFT JOIN Validated_Signal vs ON vsa.VALIDATED_SIGNAL_ID = vs.id
                                            WHERE upper(NAME) IN ('TAGS') )
                                   """
    }

    void "generateAdvancedFilterTagSql for iLike clause"() {
        expect:
        result == service.generateSqlRestrictionSql(AdvancedFilterOperatorEnum.CONTAINS, [], iLikePropertiesMap, notILikePropertiesMap, aliasPropertiesMap)
        where:
        aliasPropertiesMap          || result
        aliasPropertiesTagMap       || """{alias}.id IN (
                                             SELECT Single_alert_id FROM single_global_tag_mapping sgtm 
                                                                   LEFT JOIN single_case_all_tag scat ON sgtm.single_global_id = scat.id
                                            WHERE upper(TAG_TEXT) LIKE '%TEST%' )
                                   """
        aliasPropertiesSignalMap    || """{alias}.id IN (
                                             SELECT Single_alert_id FROM VALIDATED_SINGLE_ALERTS vsa
                                                                   LEFT JOIN Validated_Signal vs ON vsa.VALIDATED_SIGNAL_ID = vs.id
                                            WHERE upper(NAME) LIKE '%TEST%' )
                                   """
        aliasPropertiesAggSignalMap || """{alias}.id IN (
                                             SELECT AGG_ALERT_ID FROM VALIDATED_AGG_ALERTS vsa
                                                                   LEFT JOIN Validated_Signal vs ON vsa.VALIDATED_SIGNAL_ID = vs.id
                                            WHERE upper(NAME) LIKE '%TEST%' )
                                   """
    }

    void "generateAdvancedFilterTagSql for not iLike clause"() {
        expect:
        result == service.generateSqlRestrictionSql(AdvancedFilterOperatorEnum.DOES_NOT_CONTAIN, [], iLikePropertiesMap, notILikePropertiesMap, aliasPropertiesMap)
        where:
        aliasPropertiesMap          || result
        aliasPropertiesTagMap       || """{alias}.id NOT IN (
                                             SELECT Single_alert_id FROM single_global_tag_mapping sgtm 
                                                                   LEFT JOIN single_case_all_tag scat ON sgtm.single_global_id = scat.id
                                            WHERE upper(TAG_TEXT) LIKE '%TEST%' )
                                   """
        aliasPropertiesSignalMap    || """{alias}.id NOT IN (
                                             SELECT Single_alert_id FROM VALIDATED_SINGLE_ALERTS vsa
                                                                   LEFT JOIN Validated_Signal vs ON vsa.VALIDATED_SIGNAL_ID = vs.id
                                            WHERE upper(NAME) LIKE '%TEST%' )
                                   """
        aliasPropertiesAggSignalMap || """{alias}.id NOT IN (
                                             SELECT AGG_ALERT_ID FROM VALIDATED_AGG_ALERTS vsa
                                                                   LEFT JOIN Validated_Signal vs ON vsa.VALIDATED_SIGNAL_ID = vs.id
                                            WHERE upper(NAME) LIKE '%TEST%' )
                                   """
    }

    void "generateAdvancedFilterTagSql for isEmpty clause"() {
        expect:
        result == service.generateSqlRestrictionSql(AdvancedFilterOperatorEnum.IS_EMPTY, [], iLikePropertiesMap, notILikePropertiesMap, aliasPropertiesMap)
        where:
        aliasPropertiesMap          || result
        aliasPropertiesTagMap       || """{alias}.id NOT IN (
                                             SELECT Single_alert_id FROM single_global_tag_mapping sgtm 
                                                                   LEFT JOIN single_case_all_tag scat ON sgtm.single_global_id = scat.id
                                             )
                                   """
        aliasPropertiesSignalMap    || """{alias}.id NOT IN (
                                             SELECT Single_alert_id FROM VALIDATED_SINGLE_ALERTS vsa
                                                                   LEFT JOIN Validated_Signal vs ON vsa.VALIDATED_SIGNAL_ID = vs.id
                                             )
                                   """
        aliasPropertiesAggSignalMap || """{alias}.id NOT IN (
                                             SELECT AGG_ALERT_ID FROM VALIDATED_AGG_ALERTS vsa
                                                                   LEFT JOIN Validated_Signal vs ON vsa.VALIDATED_SIGNAL_ID = vs.id
                                             )
                                   """
    }

    void "generateAdvancedFilterTagSql for not Empty clause"() {
        expect:
        result == service.generateSqlRestrictionSql(AdvancedFilterOperatorEnum.IS_NOT_EMPTY, [], iLikePropertiesMap, notILikePropertiesMap, aliasPropertiesMap)
        where:
        aliasPropertiesMap          || result
        aliasPropertiesTagMap       || """{alias}.id IN (
                                             SELECT Single_alert_id FROM single_global_tag_mapping sgtm 
                                                                   LEFT JOIN single_case_all_tag scat ON sgtm.single_global_id = scat.id
                                             )
                                   """
        aliasPropertiesSignalMap    || """{alias}.id IN (
                                             SELECT Single_alert_id FROM VALIDATED_SINGLE_ALERTS vsa
                                                                   LEFT JOIN Validated_Signal vs ON vsa.VALIDATED_SIGNAL_ID = vs.id
                                             )
                                   """
        aliasPropertiesAggSignalMap || """{alias}.id IN (
                                             SELECT AGG_ALERT_ID FROM VALIDATED_AGG_ALERTS vsa
                                                                   LEFT JOIN Validated_Signal vs ON vsa.VALIDATED_SIGNAL_ID = vs.id
                                             )
                                   """
    }

    void "saveCaseSeriesInMart"() {
        expect:
        isCaseSeriesSaved == service.prepareUpdateCaseSeriesHql(isCumulativeCaseSeries)
        where:
        isCumulativeCaseSeries || isCaseSeriesSaved
        true                   || "Update ExecutedConfiguration set pvrCaseSeriesId = :pvrCaseSeriesId  ,pvrCumulativeCaseSeriesId = :pvrCaseSeriesId where id = :id"
        false                  || "Update ExecutedConfiguration set pvrCaseSeriesId = :pvrCaseSeriesId  where id = :id"
    }

    void "setBadgeValueForSCA"() {
        expect:
        service.setBadgeValueForSCA(alert, isPreviousReportingPeriod)
        where:
        alert              | isPreviousReportingPeriod || badgeValue
        singleCaseAlert    | true                      || singleCaseAlert.badge == Constants.Badges.PENDING_REVIEW
        singleCaseAlertNew | false                     || singleCaseAlertNew.badge == Constants.Badges.NEW
    }

    void "fetchPrevExecConfigId"() {
        when:
        def prevExec = service.fetchPrevExecConfigId(executedConfiguration, alertConfiguration, false, true)
        then:
        prevExec.size() == 1
    }

    void "fetchPrevExecConfigId for caseSeries"() {
        when:
        def prevExec = service.fetchPrevExecConfigId(executedConfiguration, alertConfiguration, true, true)
        then:
        prevExec[0][1] == 9
    }

    void "fetchPrevPeriodSCAlerts"() {
        given:
        service.cacheService = [getNotReviewCompletedDisposition: { -> [disposition] }]
        when:
        List<Map> prevPeriodSCAlerts = service.fetchPrevPeriodSCAlerts(SingleCaseAlert, [prevExecutedConfiguration.id])
        then:
        prevPeriodSCAlerts.size() == 1
    }

    void "generateSqlRestrictionTagSql for tags clause when equals operator"() {
        when:
        String result = service.generateSqlRestrictionTagSql("tags", "EQUALS", "hello", "tag_text", [tags:[tag_text:'tag_text']])

        then:
        result.contains("IN ('HELLO')") == true
    }

    void "generateSqlRestrictionTagSql for tags clause when contains operator"() {
        when:
        String result = service.generateSqlRestrictionTagSql("tags", "CONTAINS", "hello", "tag_text", [tags:[tag_text:'tag_text']])

        then:
        result.contains("LIKE '%HELLO%'") == true
    }


    void "fetchPrevPeriodSCAlerts if no previous execution"() {
        given:
        service.cacheService = [getNotReviewCompletedDisposition: { -> [disposition] }]
        when:
        List<Map> prevPeriodSCAlerts = service.fetchPrevPeriodSCAlerts(SingleCaseAlert, [executedConfiguration.id])
        then:
        prevPeriodSCAlerts.size() == 0
    }

    void "test sendReportingErrorNotifications"() {
        setup:
        GroovyMock(ExecutedConfiguration, global: true)
        ExecutedConfiguration.executeUpdate(_) >> 1
        service.reportIntegrationService = [saveConfigurationAndReportNotificationAndActivity: { ExecutedConfiguration executedConfiguration -> }]
        service.spotfireService = [sendErrorNotification: { ExecutedConfiguration executedConfiguration -> }]

        when:
        service.sendReportingErrorNotifications(executedConfiguration, true, true)

        then:
        noExceptionThrown()
    }

    void "test isCumCaseSeriesSpotfire"() {
        when:
        boolean result = service.isCumCaseSeriesSpotfire(executedConfiguration)

        then:
        result
    }

    void "test isCumCaseSeriesReport"() {
        when:
        boolean result = service.isCumCaseSeriesReport(executedConfiguration)

        then:
        result
    }

    void "test getEmergingIssueList"() {
        setup:
        EmergingIssue emergingIssue = new EmergingIssue(eventName: '{"4":[{"name":"event11"},{"name":"event12"}]}', createdBy: "createdBy1",
                dateCreated: new Date() - 1, dme: true, emergingIssue: true, ime: true, specialMonitoring: true)
        emergingIssue.save(failOnError: true)

        when:
        List result = service.getEmergingIssueList()

        then:
        result.size() == 1
        result[0].eventName == ["event11", "event12"]
        result[0].dme == 'dme'
        result[0].ei == 'ei'
        result[0].ime == 'ime'
        result[0].sm == 'sm'
    }

    void "test calcDueDateBulkUpdate"() {
        when:
        Date resultDate = service.calcDueDateBulkUpdate(new DateTime(2015, 1, 5, 8, 0, DateTimeZone.forID('UTC')).toDate(), priority)

        then:
        resultDate == new DateTime(2015, 1, 5, 8, 0, DateTimeZone.forID('UTC')).toDate() + priority.reviewPeriod
    }

    void "test prepareDashboardDispositionHQL"() {
        expect:
        service.prepareDashboardDispositionHQL(domain).trim() == result.trim()

        where:
        domain             || result
        SingleCaseAlert    || """
          SELECT disposition.id from com.rxlogix.signal.SingleCaseAlert alert 
            INNER JOIN alert.disposition disposition 
            INNER JOIN alert.executedAlertConfiguration executedal1_ 
            where (alert.assignedTo.id = :id or (alert.assignedToGroup.id in (:groupIdList))) 
                  and (executedal1_.adhocRun=0 and executedal1_.isDeleted=0 and executedal1_.isLatest=1 and
                       executedal1_.workflowGroup.id = :workflowGrpId) 
                  and (disposition.reviewCompleted=0)
         and alert.isCaseSeries=0
            GROUP BY disposition.id
            having count(disposition.id) > 0
        """

        AggregateCaseAlert || """ 
         SELECT disposition.id from com.rxlogix.signal.AggregateCaseAlert alert 
            INNER JOIN alert.disposition disposition 
            INNER JOIN alert.executedAlertConfiguration executedal1_ 
            where (alert.assignedTo.id = :id or (alert.assignedToGroup.id in (:groupIdList))) 
                  and (executedal1_.adhocRun=0 and executedal1_.isDeleted=0 and executedal1_.isLatest=1 and
                       executedal1_.workflowGroup.id = :workflowGrpId) 
                  and (disposition.reviewCompleted=0)
        
            GROUP BY disposition.id
            having count(disposition.id) > 0
        """
    }

    void "test getAlertConfigObject"() {
        when:
        EvdasConfiguration result = service.getAlertConfigObject(executedEvdasConfiguration)

        then:
        result == evdasConfiguration
    }

    void "test getAlertConfigObjectByType"() {
        when:
        Configuration result = service.getAlertConfigObjectByType(executedConfiguration)

        then:
        result == alertConfiguration
    }

    void "test getDistinctProductName"() {
        setup:
        AlertDataDTO alertDataDTO = new AlertDataDTO(userId: user.id, execConfigId: executedConfiguration.id)

        when:
        List<String> resultProductName = service.getDistinctProductName(SingleCaseAlert, alertDataDTO, Constants.Commons.DASHBOARD)

        then:
        resultProductName.size() == 1
        resultProductName == ['Test Product A']
    }

    void "test toggleAlertFlag"() {
        when:
        boolean result = service.toggleAlertFlag(alert.id, SingleCaseAlert)

        then:
        result
    }

    void "test isProductSecurity"() {
        when:
        boolean result = service.isProductSecurity()

        then:
        result != null
    }

    void "test getProductIdsFromProductSelection"() {
        when:
        List<String> resultProductName = service.getProductIdsFromProductSelection("Test Product A,Test Product B")

        then:
        resultProductName.size() == 2
        resultProductName == ['Test Product A', 'Test Product B']
    }

    void "test getAssignedToCountList"() {
        when:
        List<Map> result = service.getAssignedToCountList(SingleCaseAlert, [executedConfiguration.id, prevExecutedConfiguration.id], [disposition.id], user.id)

        then:
        result.size() == 1
        result[0].cnt == 1
        result[0].id == prevExecutedConfiguration.id
    }

    void "test getReviewCompletedDispositionList"() {
        when:
        List<Long> result = service.getReviewCompletedDispositionList()

        then:
        result.size() == 1
        result[0] == disposition.id
    }

    void "test getDomainPropertyMap for AggregateCaseAlert"() {
        when:
        Map<String, String> result = service.getDomainPropertyMap(AggregateCaseAlert)

        then:
        result.productName == 'productName'
        result.prrValue == 'prrValue'
        result.rorValue == 'rorValue'
    }

    void "test getDomainPropertyMap for SingleCaseAlert"() {
        when:
        Map<String, String> result = service.getDomainPropertyMap(SingleCaseAlert)

        then:
        result.caseNumber == 'caseNumber'
        result.followUpNumber == 'followUpNumber'
        result.caseVersion == 'caseVersion'
    }

    void "test getDomainPropertyMap for EvdasAlert"() {
        when:
        Map<String, String> result = service.getDomainPropertyMap(EvdasAlert)

        then:
        result.periodEndDate == 'periodEndDate'
        result.substance == 'substance'
        result.pt == 'pt'
    }

    void "test getDomainPropertyMap for LiteratureAlert"() {
        when:
        Map<String, String> result = service.getDomainPropertyMap(LiteratureAlert)

        then:
        result.searchString == 'searchString'
        result.articleId == 'articleId'
        result.priority == 'priority'
    }

    void "test getDispositionsForName"() {
        expect:
        service.getDispositionsForName(dispositionFilters) == result

        where:
        dispositionFilters   || result
        ['Validated Signal'] || [disposition]
        null                 || []
    }

    void "test findAdhocAlertById"() {
        setup:
        AdHocAlert adHocAlert = new AdHocAlert(id: 1L,
                productSelection: "something",
                detectedDate: new DateTime(2015, 12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                name: "Test Name",
                detectedBy: "Company",
                topic: "rash",
                initialDataSource: "Test",
                alertVersion: 0,
                priority: priority,
                assignedTo: user
        )
        adHocAlert.save(failOnError: true)

        when:
        AdHocAlert result = service.findAdhocAlertById(adHocAlert.id)

        then:
        result == adHocAlert
    }

    void "test findByExecutedConfiguration for null value"() {
        when:
        List result = service.findByExecutedConfiguration(null)

        then:
        result == null
    }

    void "test getValuesList"() {
        expect:
        service.getValuesList(value, 'product') == result

        where:
        value                        || result
        'productA;productB;productC' || ['productA', 'productB', 'productC']
        '1;2;3'                      || [1L, 2L, 3L]
        'true;false;true'            || [true, false, true]
        '@;#;%'                      || ['@', '#', '%']
    }

    void "test getValuesListForClobFields"() {
        when:
        List result = service.getValuesListForClobFields("productA;productB;productC")

        then:
        result == ['productA', 'productB', 'productC']
    }

    @ConfineMetaClassChanges([AlertService])
    void "test preparePossibleValuesMap"() {
        setup:
        service.priorityService = [listPriorityAdvancedFilter: { -> ['mockPriority'] }]
        service.dispositionService = [listDispositionAdvancedFilter: { -> ['mockDisposition'] }]
        service.validatedSignalService = [listSignalAdvancedFilter: { -> ['mockValidatedSignal'] }]
        service.metaClass.getAllUsers = { [id: 1L, text: 'ValueA'] }
        service.metaClass.getAllGroups = { [id: 1L, text: 'ProductA'] }

        when:
        Map<String, List> possibleValuesMap = [:]
        service.preparePossibleValuesMap(SingleCaseAlert, possibleValuesMap, executedConfiguration.id)

        then:
        possibleValuesMap.'priority.id' == ['mockPriority']
        possibleValuesMap.'aggImpEventList' == ['ime', 'dme', 'ei', 'sm']
        possibleValuesMap.'evImpEventList' == ['ime', 'dme', 'ei', 'sm']
        possibleValuesMap.'disposition.id' == ['mockDisposition']
        possibleValuesMap.'signal' == ['mockValidatedSignal']
        possibleValuesMap.'assignedTo.id' == [id: 1L, text: 'ValueA']
        possibleValuesMap.'assignedToGroup.id' == [id: 1L, text: 'ProductA']
    }

    void "test getDistinctValuesOfStringFields in case of AggregateCaseAlert"() {
        when:
        Map<String, List> possibleValuesMap = [:]
        service.getDistinctValuesOfStringFields(AggregateCaseAlert, possibleValuesMap, executedConfiguration.id)

        then:
        possibleValuesMap.flags == []
        possibleValuesMap.productName == []
        possibleValuesMap.soc == []
        possibleValuesMap.trendType == []
        possibleValuesMap.freqPriority == []
    }

    void "test getDistinctValuesOfStringFields in case of SingleCaseAlert"() {
        when:
        Map<String, List> possibleValuesMap = [:]
        service.getDistinctValuesOfStringFields(SingleCaseAlert, possibleValuesMap, executedConfiguration.id)

        then:
        possibleValuesMap.flags == []
        possibleValuesMap.productName == []
        possibleValuesMap.appTypeAndNum == []
    }


    void "test getDistinctValuesOfStringFields in case of others"() {
        when:
        Map<String, List> possibleValuesMap = [:]
        service.getDistinctValuesOfStringFields(EvdasAlert, possibleValuesMap, executedConfiguration.id)

        then:
        possibleValuesMap.flags == []
        possibleValuesMap.substance == []
        possibleValuesMap.soc == []
        possibleValuesMap.hlgt == []
        possibleValuesMap.hlt == []
    }

    void "test prepareFilterMap when assignedTo is given"() {
        setup:
        Map params = [:]
        params["columns[0][data]"] = "productName"
        params["columns[0][search][value]"] = "productA"
        params["columns[1][data]"] = "detectedDate"
        params["columns[1][search][value]"] = new DateTime(2015, 1, 5, 21, 0, 0, 0, DateTimeZone.forID('UTC'))
        params.dashboardFilter = 'assignedTo'

        when:
        Map result = service.prepareFilterMap(params, 1)

        then:
        result.productName == 'productA'
        result.detectedDate == new DateTime(2015, 1, 5, 21, 0, 0, 0, DateTimeZone.forID('UTC'))
        result.assigned == user.username
    }

    void "test prepareFilterMap when assignedToUser is given"() {
        setup:
        Map params = [:]
        params["columns[0][data]"] = "productName"
        params["columns[0][search][value]"] = "productA"
        params["columns[1][data]"] = "detectedDate"
        params["columns[1][search][value]"] = new DateTime(2015, 1, 5, 21, 0, 0, 0, DateTimeZone.forID('UTC'))
        params.dashboardFilter = 'assignedToUser'

        when:
        Map result = service.prepareFilterMap(params, 1)

        then:
        result.productName == 'productA'
        result.detectedDate == new DateTime(2015, 1, 5, 21, 0, 0, 0, DateTimeZone.forID('UTC'))
        result.assignedToUserAndGroup == user.id
    }


    void "test prepareFilterMap when new is given"() {
        setup:
        Map params = [:]
        params["columns[0][data]"] = "productName"
        params["columns[0][search][value]"] = "productA"
        params["columns[1][data]"] = "detectedDate"
        params["columns[1][search][value]"] = new DateTime(2015, 1, 5, 21, 0, 0, 0, DateTimeZone.forID('UTC'))
        params.dashboardFilter = 'new'

        when:
        Map result = service.prepareFilterMap(params, 1)

        then:
        result.productName == 'productA'
        result.detectedDate == new DateTime(2015, 1, 5, 21, 0, 0, 0, DateTimeZone.forID('UTC'))
        result.newDashboardFilter == true
    }

    void "test prepareOrderColumnMap"() {
        setup:
        Map params = [:]
        params["order[0][column]"] = "productName"
        params["columns[productName][data]"] = "productA"
        params["order[0][dir]"] = "dir"

        when:
        Map result = service.prepareOrderColumnMap(params)

        then:
        result.name == "productA"
        result.dir == "dir"
    }

    void "test alertListByExecConfig"() {
        when:
        List<Map> result = service.alertListByExecConfig(alertLevelDispositionDTO)

        then:
        result[0].assignedToId == user.id
        result[0].productFamily == 'Test Product A'
        result[0].pt == 'Rash'
        result[0].disposition == disposition
    }

    void "test createActivityForBulkDisposition"() {
        setup:
        Activity activity = new Activity(type: alertLevelDispositionDTO.activityType,
                details: alertMap.details,
                timestamp: DateTime.now(),
                justification: alertLevelDispositionDTO.justificationText,
                suspectProduct: alertLevelDispositionDTO.domainName == EvdasAlert ? alertMap.substance : alertMap.productName,
                eventName: alertMap.pt,
                caseNumber: alertMap.caseNumber
        )

        ActivityService mockActivityService = Mock(ActivityService)
        mockActivityService.createActivityAlertLevelDisposition(alertMap, alertLevelDispositionDTO) >> {
            return activity
        }
        service.activityService = mockActivityService

        when:
        Activity result = service.createActivityForBulkDisposition(alertMap, alertLevelDispositionDTO)

        then:
        result == activity
    }

    void "test createLiteratureActivityForBulkDisposition"() {
        setup:
        LiteratureActivity literatureActivity = new LiteratureActivity(
                type: alertLevelDispositionDTO.activityType,
                details: alertMap.details,
                timestamp: DateTime.now(),
                justification: alertLevelDispositionDTO.justificationText,
                productName: alertMap.productName,
                eventName: alertMap.pt,
        )

        LiteratureActivityService mockLiteratureActivityService = Mock(LiteratureActivityService)
        mockLiteratureActivityService.createLiteratureActivityAlertLevelDisposition(alertMap, alertLevelDispositionDTO) >> {
            return literatureActivity
        }
        service.literatureActivityService = mockLiteratureActivityService

        when:
        LiteratureActivity result = service.createLiteratureActivityForBulkDisposition(alertMap, alertLevelDispositionDTO)

        then:
        result == literatureActivity
    }


    void "test updateReviewCountsForLiterature"(){
        when:
        service.updateReviewCountsForLiterature(executedLiteratureConfiguration.id, 5)
        then:
        executedLiteratureConfiguration.requiresReviewCount == "95"
    }

    void "test updateOldExecutedConfigurationsLiterature"(){
        given:
        service.cacheService = [getNotReviewCompletedDisposition: { -> [disposition] }]
        when:
        service.updateOldExecutedConfigurationsLiterature(executedLiteratureConfiguration, executedLiteratureConfiguration.id,
                ExecutedLiteratureConfiguration,[(disposition.id):100])
        then:
        executedLiteratureConfiguration.requiresReviewCount == "100"
    }

    void "test maxSeqCommentListAggregate"(){
        when:
        List result = service.maxSeqCommentListAggregate(alertConfiguration.id)
        then:
        result == [[eventName:"Rash", comments:"comm-1", productId:10, configId:82]]

    }
    void "test maxSeqCommentListEvdas"(){
        when:
        List result = service.maxSeqCommentListEvdas(alertConfiguration.id)
        then:
        result == [[eventName:"Rash", comments:"comm-2", productName:"aj", configId:83]]
    }
    @ConfineMetaClassChanges([AlertService])
    void "test alertCaseCommentsFilter for single alert"(){
        setup:
        service.metaClass.maxSeqCommentList = {Boolean isFaers -> [[comments:"comm-1"]] }
        when:
        List result = service.alertCaseCommentsFilter("com", SingleCaseAlert, false, alertConfiguration.id)
        then:
        result == [[comments:"comm-1"]]
    }
    void "test alertCaseCommentsFilter for aggreagte alert"(){
        setup:
        service.metaClass.maxSeqCommentList = {Boolean isFaers -> [[comments:"comm-1"]] }
        when:
        List result = service.alertCaseCommentsFilter("com", AggregateCaseAlert, false, alertConfiguration.id)
        then:
        result == [[eventName:"Rash", comments:"comm-1", productId:10, configId:85]]

    }
    void "test alertCaseCommentsFilter for evdas alert"(){
        setup:
        service.metaClass.maxSeqCommentList = {Boolean isFaers -> [[comments:"comm-1"]] }
        when:
        List result = service.alertCaseCommentsFilter("com", EvdasAlert, false, alertConfiguration.id)
        then:
        result == [[eventName:"Rash", comments:"comm-2", productName:"aj", configId:86]]

    }
    void "test getInStringForFilters for single alert"(){
        when:
        String result = service.getInStringForFilters([[caseId: 1, versionNum: 1]], SingleCaseAlert)
        then:
        result == "((1,1))"
    }
    void "test getInStringForFilters for aggreagte alert"(){
        when:
        String result = service.getInStringForFilters([[productId: 2, eventName: "Rash", configId: alertConfiguration.id]], AggregateCaseAlert)
        then:
        result == "((2,'Rash',88))"

    }
    void "test getInStringForFilters for evdas alert"(){
        when:
        String result = service.getInStringForFilters([[productName: "aj", eventName: "Rash", configId: alertConfiguration.id]], EvdasAlert)
        then:
        result == "(('aj','Rash',89))"

    }
    @ConfineMetaClassChanges([AlertService])
    @Unroll
    void "test alertCommentsAdvanceFilter"(){
        setup:
        service.metaClass.maxSeqCommentList = {Boolean isFaers -> [[comments:"comm-1"]] }
        expect:
        service.alertCommentsAdvanceFilter(a, b, c, d, e, f) == result
        where:
        a                                       |      b         |      c        |        d          |         e |
                f       |        result
        ["comm-1","test"] | AdvancedFilterOperatorEnum.NOT_EQUAL| "comm-1"  | SingleCaseAlert |
        false | alertConfiguration.id | [[comments:"comm-1"]]
        ["comm-1","test"] | AdvancedFilterOperatorEnum.EQUALS| "comm-1"  | AggregateCaseAlert |
        false | alertConfiguration.id | []
        ["comm-1","test"] | AdvancedFilterOperatorEnum.DOES_NOT_CONTAIN| "comm-1"  | EvdasAlert |
        false | alertConfiguration.id | []
        ["comm-1","test"] | AdvancedFilterOperatorEnum.DOES_NOT_START| "comm-1"  | EvdasAlert |
        false | alertConfiguration.id | []
        ["comm-1","test"] | AdvancedFilterOperatorEnum.DOES_NOT_END| "comm-1"  | EvdasAlert |
        false | alertConfiguration.id | []
        ["comm-1","test"] | AdvancedFilterOperatorEnum.CONTAINS| "comm"  | EvdasAlert |
        false | alertConfiguration.id | []
        ["comm-1","test"] | AdvancedFilterOperatorEnum.START_WITH| "comm"  | EvdasAlert |
        false | alertConfiguration.id | []
        ["comm-1","test"] | AdvancedFilterOperatorEnum.ENDS_WITH| "comm-2"  | EvdasAlert |
        false | alertConfiguration.id | []
        ["comm-1","test"] | AdvancedFilterOperatorEnum.IS_EMPTY| "comm-2"  | EvdasAlert |
        false | alertConfiguration.id | []
        ["comm-1","test"] | AdvancedFilterOperatorEnum.IS_NOT_EMPTY| "comm-2"  | EvdasAlert |
        false | alertConfiguration.id | []
    }

    void "test addExtraValueInCriteria for evdas alert"(){
        setup:
        AlertDataDTO alertDataDTO = new AlertDataDTO(isFaers: true, configId: 10)
        String str = "{->criteriaConditions(1,2,3)}"
        when:
        String result = service.addExtraValueInCriteria(alertDataDTO, str)
        then:
        result == "{->criteriaConditions(1,2,3,true,10)}"

    }

    @ConfineMetaClassChanges([AlertService])
    void "test populateAdvancedFilterDispositions"(){
        setup:
        AlertDataDTO alertDataDTO = new AlertDataDTO(userId: user.id, execConfigId: executedConfiguration.id)
        String query = "{\"all\":{\"containerGroups\":[ {\"expressions\":[ {\"index\":\"0\",\"field\":\"listedness\",\"op\":\"EQUALS\",\"value\":\"true\"} ] }  ] } }"

        service.metaClass.parseExpressionObj = {Map expressionObj, AlertDataDTO alertDataDTO1 ->
            return
        }
        CacheService mockCacheService = Mock(CacheService)
        mockCacheService.getDispositionListById(_) >>{
            return []
        }
        service.cacheService = mockCacheService
        when:
        def result = service.populateAdvancedFilterDispositions(alertDataDTO,query)
        then:
        result == null
    }

    void "test parseExpressionObj"(){
        setup:
        JsonSlurper jsonSlurper = new JsonSlurper()
        AlertDataDTO alertDataDTO = new AlertDataDTO(userId: user.id, execConfigId: executedConfiguration.id)
        String query = "{\"all\":{\"containerGroups\":[ {\"expressions\":[ {\"index\":\"0\",\"field\":\"listedness\",\"op\":\"EQUALS\",\"value\":\"true\"} ] }  ] } }"
        Map object = jsonSlurper.parseText(query)
        Map expressionObj = object.all.containerGroups[0]
        when:
        def result = service.parseExpressionObj(expressionObj, alertDataDTO)
        then:
        result == null
    }

    def "test parseXmlWithStax"() {
        setup:
        // Create a temporary XML file
        def xmlFile = File.createTempFile("test", ".xml")
        xmlFile.text = """
        <PubmedArticleSet>
            <PubmedArticle>
                <MedlineCitation>
                    <PMID>123456</PMID>
                    <Article>
                        <ArticleTitle>Test Article</ArticleTitle>
                        <Journal>
                            <Title>Test Journal</Title>
                        </Journal>
                        <Abstract>
                            <AbstractText Label="BACKGROUND">Background text.</AbstractText>
                        </Abstract>
                        <AuthorList>
                            <Author>
                                <LastName>Smith</LastName>
                                <ForeName>John</ForeName>
                            </Author>
                        </AuthorList>
                    </Article>
                    <PubDate>
                        <Year>2020</Year>
                        <Month>12</Month>
                        <Day>31</Day>
                    </PubDate>
                </MedlineCitation>
            </PubmedArticle>
        </PubmedArticleSet>
        """

        when:
        // Call the method to parse the XML
        List<Map<String, Object>> result = service.parseXmlWithStax(xmlFile.absolutePath)

        then:
        // Verify the parsed result
        result.size() == 1
        result[0].PMID == '123456'
        result[0].ArticleTitle == 'Test Article'
        result[0].JournalTitle == 'Test Journal'
        result[0].Authors.size() == 1
        result[0].Authors[0].LastName == 'Smith'
        result[0].Authors[0].ForeName == 'John'
        result[0].AbstractText.size() == 1
        result[0].AbstractText[0].Label == null
        result[0].AbstractText[0].Text == 'Background text.'
        result[0].PubDateYear == '2020'
        result[0].PubDateMonth == '12'
        result[0].PubDateDay == '31'

        cleanup:
        // Delete the temporary file
        xmlFile.delete()
    }

    def "test parseXmlWithStax with empty XML file"() {
        setup:
        def xmlFile = File.createTempFile("test_empty", ".xml")
        xmlFile.text = ""

        when:
        def result = service.parseXmlWithStax(xmlFile.absolutePath)

        then:
        thrown(Throwable)

        cleanup:
        xmlFile.delete()
    }

    def "test parseXmlWithStax with malformed XML file"() {
        setup:
        File xmlFile = File.createTempFile("test_malformed", ".xml")
        xmlFile.text = """
    <PubmedArticleSet>
        <PubmedArticle>
            <MedlineCitation>
                <PMID>445566</PMID>
                <Article>
                    <ArticleTitle>Malformed XML</ArticleTitle>
                    <Journal>
                        <Title>Journal with Malformed XML</Title>
                    </Journal>
                    <!-- Missing closing tags for Article and MedlineCitation -->
    </PubmedArticleSet>
    """

        when:
        def result = service.parseXmlWithStax(xmlFile.absolutePath)

        then:
        thrown(XMLStreamException)

        cleanup:
        xmlFile.delete()
    }


    def "test updateProdSelectionForDrugNumber method"() {
        given:
        service.signalDataSourceService = Mock(SignalDataSourceService)
        service.log = Mock(Log)

        List<Configuration> childConfigs = [
                new Configuration(productSelection: '{"1":[{"name":"PARACETAMOL","id":"829189359","isMultiIngredient":true},{"name":"PARACETAMOL/ DICLOFENAC","id":"1513200401","isMultiIngredient":true}],"2":[],"3":[],"4":[],"100":[]}', includeWHODrugs: true),
                new Configuration(productSelection: '{"1":[{"name":"PARACETAMOL","id":"9089","isMultiIngredient":false}],"2":[],"3":[],"4":[],"100":[]}', includeWHODrugs: false)
        ]

        when:
        service.updateProdSelectionForDrugNumber(childConfigs)

        then:
        1 * service.signalDataSourceService.getReportConnection(Constants.DataSource.PVA) >> Mock(Sql)
        1 * _.execute(_) >> 1
        1 * _.call("{call pkg_pvr_app_util.P_POP_DRUG_REC_NUM}") >> null
        1 * _.rows("Select * from GTT_FILTER_KEY_VALUES") >> [
                [exec_id: "1", CODE: "123456", TEXT: "text1", isMultiIngredient: true]
        ]

        and:
        childConfigs[0].productSelection[Constants.DRUG_RECORD_NUMBER_LEVEL] == [
                [id: "123456", name: "text1", isMultiIngredient: true]
        ]
        childConfigs[1].productSelection[Constants.DRUG_RECORD_NUMBER_LEVEL] == []

    }

    def "test fetchDrugRecordNumbersAsList with valid JSON containing drugRecordNumbers"() {
        // Given
        given:
        String jsonString = '''
        {
            "1":[
                {"name":"IBUPROFEN/ PARACETAMOL","id":"1088984991953664453","isMultiIngredient":true},
                {"name":"IBUPROFEN/ PARACETAMOL/ HUMAN PAPILLOMA VACCINE","id":"676329449295620838","isMultiIngredient":true}
            ],
            "100":[
                {"name":"PARACETAMOL","id":"000200","isMultiIngredient":true,"drugRecordNumber":"000200"},
                {"name":"IBUPROFEN/ PARACETAMOL","id":"140071","isMultiIngredient":true,"drugRecordNumber":"140071"}
            ]
        }
        '''

         when:
        List<String> result = service.fetchDrugRecordNumbersAsList(jsonString)

        then:
        result.size() == 2
        result.contains("000200")
        result.contains("140071")
    }

    void "test fetchDrugRecordNumbersAsList with empty JSON"() {
        given:
        String jsonString = '{}'

        when:
        List<String> result = service.fetchDrugRecordNumbersAsList(jsonString)

        then:
        result != null
        result.isEmpty() == true
    }

    void "test fetchDrugRecordNumbersAsList with null JSON string"() {
        given:
        String jsonString = null

        when:
        List<String> result = service.fetchDrugRecordNumbersAsList(jsonString)

        then:
        result != null
        result.isEmpty() == true
    }

    void "test fetchDrugRecordNumbersAsList with invalid JSON format"() {
        given:
        String jsonString = '{"1": [invalid JSON]}'

        when:
        List<String> result = service.fetchDrugRecordNumbersAsList(jsonString)

        then:
        result != null
        result.isEmpty() == true// Should return empty list due to parsing exception
    }

    void "test fetchDrugRecordNumbersAsList with no drugRecordNumber key"() {
        given:
        String jsonString = '''
        {
            "100":[
                {"name":"PARACETAMOL","id":"000200","isMultiIngredient":true},
                {"name":"IBUPROFEN/ PARACETAMOL","id":"140071","isMultiIngredient":true}
            ]
        }
        '''

        when:
        List<String> result = service.fetchDrugRecordNumbersAsList(jsonString)

        then:
        result != null
        result.isEmpty() == true
    }
}