package com.rxlogix.signal

import com.rxlogix.AggregateCaseAlertService
import com.rxlogix.AlertService
import com.rxlogix.CaseHistoryService
import com.rxlogix.Constants
import com.rxlogix.DataObjectService
import com.rxlogix.EvdasAlertService
import com.rxlogix.ProductBasedSecurityService
import com.rxlogix.ReportIntegrationService
import com.rxlogix.cache.CacheService
import com.rxlogix.config.Configuration
import com.rxlogix.config.Disposition
import com.rxlogix.config.ExecutedAlertDateRangeInformation
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedDateRangeInformation
import com.rxlogix.config.ExecutedTemplateQuery
import com.rxlogix.config.ExecutionStatus
import com.rxlogix.config.Priority
import com.rxlogix.config.ReportExecutionStatus
import com.rxlogix.config.WorkflowRule
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.enums.QueryLevelEnum
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.testing.services.ServiceUnitTest
import org.hibernate.SessionFactory
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.slf4j.Logger
import spock.lang.*
import spock.util.mop.ConfineMetaClassChanges


@ConfineMetaClassChanges([ExecutedConfiguration])
class AggregateOnDemandAlertServiceSpec extends Specification implements ServiceUnitTest<AggregateOnDemandAlertService> {


    Disposition disposition
    Configuration alertConfiguration
    ExecutedConfiguration executedConfiguration
    User user
    def mockData
    def mockDateRange
    EvdasAlertService evdasAlertService
    @Shared
    AggregateOnDemandAlert aggregateOnDemandAlert_a
    @Shared
    AggregateOnDemandAlert aggregateOnDemandAlert_b
    GlobalProductEvent globalProductEvent
    List<Map> alertData = []
    Logger logger
    ExecutionStatus executionStatus

    def setup() {
        logger = Mock(Logger)
        service.log = logger

        service.sessionFactory = Mock(SessionFactory)

        Priority priority = new Priority([displayName: "mockPriority", value: "mockPriority", display: true, defaultPriority: true, reviewPeriod: 1])
        priority.save(failOnError: true)
        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, reviewCompleted: true, abbreviation: "vs")
        disposition.save(failOnError: true)


        Group wfGroup = new Group(name: "Default", createdBy: 'createdBy', modifiedBy: 'modifiedBy',
                groupType: GroupType.WORKFLOW_GROUP,
                defaultQualiDisposition: disposition,
                defaultQuantDisposition: disposition,
                defaultAdhocDisposition: disposition,
                defaultEvdasDisposition: disposition,
                defaultLitDisposition: disposition,
                defaultSignalDisposition: disposition,
                autoRouteDisposition: disposition,
                justificationText: "Update Disposition",
                forceJustification: true)

        wfGroup.save(flush: true)

        //Prepare the mock user
        user = new User(id: '1', username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.addToGroups(wfGroup)
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
                createdBy: user.username, modifiedBy: user.username,
                assignedTo: user,
                priority: priority,
                owner: user
        )
        alertConfiguration.save(failOnError: true)

        executionStatus = new ExecutionStatus(configId: alertConfiguration.id,
                startTime: new Date().getTime(),
                endTime: new Date().getTime(), executionStatus: ReportExecutionStatus.COMPLETED,
                reportVersion: 1, message: 'for testing purpose',
                owner: user, name: 'executionStatus', type: Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
                nextRunDate: new Date())
        executionStatus.save(flush: true)

        executedConfiguration = new ExecutedConfiguration(name: "test", isLatest: true, adhocRun: false,
                owner: user, scheduleDateJSON: "{}", nextRunDate: new Date(), type: Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
                description: "test", dateCreated: new Date(), lastUpdated: new Date(),
                isPublic: true, isDeleted: false, isEnabled: true, totalExecutionTime: 10,
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
                pvrCaseSeriesId: 1,
                pvrCumulativeCaseSeriesId: 1,
                selectedDatasource: "pva",
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 1,dataMiningVariable: "SOC")
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

        SubstanceFrequency frequency = new SubstanceFrequency(name: 'Test Product', startDate: Date.parse('dd-MMM-yyyy', '01-Jan-2014'), endDate: Date.parse('dd-MMM-yyyy', '31-Dec-2014'),
                uploadFrequency: 'Yearly', miningFrequency: 'Yearly', frequencyName: "Yearly", alertType: "Aggregate Case Alert")
        frequency.save(flush: true)

        AlertTag alertTagA = new AlertTag(name: "testAlertA", createdBy: user, dateCreated: new Date())
        alertTagA.save(flush: true)

        AlertTag alertTagB = new AlertTag(name: "testAlertB", createdBy: user, dateCreated: new Date())
        alertTagB.save(flush: true)

        aggregateOnDemandAlert_a = new AggregateOnDemandAlert(
                alertConfiguration: alertConfiguration,
                executedAlertConfiguration: executedConfiguration,
                name: executedConfiguration.name,
                priority: priority,
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
                aggImpEventList: ['AGA_IMP_EVENTS','AGG_ALERT_IMP_EVENT_LIST','AGG_ALERT_ID'],
                eb05Str: "male:0.1, female:0.2, unknown:0.3",
                eb95Str: "male:0.1, female:0.2, unknown:0.3",
                ebgmStr: "male:0.1, female:0.2, unknown:0.3")
        globalProductEvent = new GlobalProductEvent()
        globalProductEvent.productEventComb = "100083-10029404"
        globalProductEvent.save(flush: true)
        aggregateOnDemandAlert_a.globalIdentity = globalProductEvent
        aggregateOnDemandAlert_a.save(failOnError: true)
        aggregateOnDemandAlert_b = new AggregateOnDemandAlert(
                alertConfiguration: alertConfiguration,
                executedAlertConfiguration: executedConfiguration,
                name: executedConfiguration.name,
                priority: priority,
                disposition: disposition,
                assignedTo: user,
                alertTags: alertTagB,
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
                eb05Str: "male:0.1, female:0.2, unknown:0.3",
                eb95Str: "male:0.1, female:0.2, unknown:0.3",
                ebgmStr: "male:0.1, female:0.2, unknown:0.3")
        aggregateOnDemandAlert_b.save(flush: true)


        mockData = ["PRODUCT_NAME": "Test Product", "PRODUCT_ID": 100083, "PT_CODE": 10029404, "NEW_SPON_COUNT": BigInteger.valueOf(5), "CUMM_SPON_COUNT": BigInteger.valueOf(13)]
        mockDateRange = [Date.parse('dd-MMM-yyyy', '01-Jan-2018'), Date.parse('dd-MMM-yyyy', '31-Dec-2018')]

        alertData.add(['PRODUCT_ID': 100083, "PT_CODE": "10029404"])
        alertData.add(['PRODUCT_ID': 100083, "PT_CODE": "10029405"])
        alertData.add(['PRODUCT_ID': 100083, "PT_CODE": "10029406"])

        DataObjectService mockDataObjectService = Mock(DataObjectService)
        mockDataObjectService.getStatsDataMap(_, _, _) >> {
            return [eb05: new Double(1), eb95: new Double(1), ebgm: new Double(2)]
        }
        mockDataObjectService.getStatsDataMapSubgrouping(_, _, _) >> {
            return [ebgmAge   : 'Adolescent : 3.99 ,Adult : 3.99 ,Child : 3.99 ,Elderly : 3.99 ', eb05Age: 'Adolescent : 5.99 ,Adult : 5.99 ,Child : 5.99 ,Elderly : 5.99 '
                    , eb95Age : 'Adolescent : 7.99 ,Adult : 7.99 ,Child : 7.99 ,Elderly : 7.99 ', eb05Gender: 'Confident : 3.99,CopyConf : 3.99,Female : 0.3361',
                    eb95Gender: 'Confident : 3.99,CopyConf : 3.99,Male : 0.2891', ebgmGender: 'Confident : 3.99,CopyConf : 3.99,Female : 0.2361']
        }
        mockDataObjectService.getProbDataMap(_, _, _) >> {
            return [prrValue: "1", prrLCI: "1", prrUCI: "1", prrStr: "1", prrStrLCI: "1", prrStrUCI: "1", prrMh: "1", rorValue: "1", rorLCI: "1",
                    ror95   : "1", rorStr: "1", rorStrLCI: "1", rorStrUCI: "1", rorMh: "1", chiSquare: 15]
        }
        service.dataObjectService = mockDataObjectService

        def cacheServiceMocked = Mock(CacheService)
        cacheServiceMocked.getRorCache() >> false
        cacheServiceMocked.getSubGroupColumns() >>[['Adolescent','Adult','Child','Elderly','Foetus','Infant','Neonate'],['AddedNew','Confident','Female','MALE_NEW','Male','Transege','UNK']]
        cacheServiceMocked.getSubGroupColumnsFaers() >>[['Adolescent','Adult','Child','Elderly','Foetus','Infant','Neonate'],['AddedNew','Confident','Female','MALE_NEW','Male','Transege','UNK']]
        service.cacheService = cacheServiceMocked
    }

    void "test fieldListAdvanceFilter when pva is enabled"(){
        when:
        List<Map> result = service.fieldListAdvanceFilter(false, true,executedConfiguration.dataMiningVariable)

        then:
        result.size() == 83
        result[0] == [name:'EBGM:Adolescent', display:'EBGM(Adolescent)', dataType:'java.lang.Number']
        result[1] == [name:'EBGM:Adult', display:'EBGM(Adult)', dataType:'java.lang.Number']
    }

    void "test fieldListAdvanceFilter when faers is enabled"(){
        when:
        List<Map> result = service.fieldListAdvanceFilter(true, false,executedConfiguration.dataMiningVariable)

        then:
        result.size() == 41
        result[0] == [name:'soc', display:'SOC', dataType:'java.lang.String']
        result[1] == [name:'listed', display:'Listed', dataType:'java.lang.String']
        result[40] == [name:'productName', display:'null', dataType:'java.lang.String']
    }

    void "test printExecutionMessage"(){
        when:
        service.printExecutionMessage(alertConfiguration, executedConfiguration, [aggregateOnDemandAlert_a])

        then:
        logger.info("Alert data save flow is complete.")
    }

    void "test setStatsSubgroupingScoresValues when smqCode is present"() {
        when:
        service.setStatsSubgroupingScoresValues(aggregateOnDemandAlert_a,null,false)

        then:
        aggregateOnDemandAlert_a.ebgmAge == 'Adolescent : 3.99 ,Adult : 3.99 ,Child : 3.99 ,Elderly : 3.99 '
        aggregateOnDemandAlert_a.eb05Age == 'Adolescent : 5.99 ,Adult : 5.99 ,Child : 5.99 ,Elderly : 5.99 '
        aggregateOnDemandAlert_a.eb95Age == 'Adolescent : 7.99 ,Adult : 7.99 ,Child : 7.99 ,Elderly : 7.99 '
        aggregateOnDemandAlert_a.ebgmGender == 'Confident : 3.99,CopyConf : 3.99,Female : 0.2361'
        aggregateOnDemandAlert_a.eb05Gender == 'Confident : 3.99,CopyConf : 3.99,Female : 0.3361'
        aggregateOnDemandAlert_a.eb95Gender == 'Confident : 3.99,CopyConf : 3.99,Male : 0.2891'
    }

    void "test setStatsSubgroupingScoresValues when smqCode is absent"() {
        when:
        service.setStatsSubgroupingScoresValues(aggregateOnDemandAlert_b,null,false)

        then:
        aggregateOnDemandAlert_b.ebgmAge == 'Adolescent : 3.99 ,Adult : 3.99 ,Child : 3.99 ,Elderly : 3.99 '
        aggregateOnDemandAlert_b.eb05Age == 'Adolescent : 5.99 ,Adult : 5.99 ,Child : 5.99 ,Elderly : 5.99 '
        aggregateOnDemandAlert_b.eb95Age == 'Adolescent : 7.99 ,Adult : 7.99 ,Child : 7.99 ,Elderly : 7.99 '
        aggregateOnDemandAlert_b.ebgmGender == 'Confident : 3.99,CopyConf : 3.99,Female : 0.2361'
        aggregateOnDemandAlert_b.eb05Gender == 'Confident : 3.99,CopyConf : 3.99,Female : 0.3361'
        aggregateOnDemandAlert_b.eb95Gender == 'Confident : 3.99,CopyConf : 3.99,Male : 0.2891'
    }

    void "test setPrrRorScoresValues when smqCode is present"() {
        when:
        service.setPrrRorScoresValues(aggregateOnDemandAlert_a,null,false)

        then:
        aggregateOnDemandAlert_a.prrValue == 1.0
        aggregateOnDemandAlert_a.prrUCI == 1.0
        aggregateOnDemandAlert_a.prrLCI == 1.0
    }

    void "test setPrrRorScoresValues when smqCode is absent"() {
        when:
        service.setPrrRorScoresValues(aggregateOnDemandAlert_b,null,false)

        then:
        aggregateOnDemandAlert_b.prrValue == 1.0
        aggregateOnDemandAlert_b.prrUCI == 1.0
        aggregateOnDemandAlert_b.prrLCI == 1.0
    }

    void "test setStatisticsScoresValues when smqCode is present"() {
        when:
        service.setStatisticsScoresValues(aggregateOnDemandAlert_a,null,false)

        then:
        aggregateOnDemandAlert_a.ebgm == new Double(2)
        aggregateOnDemandAlert_a.eb05 == new Double(1)
        aggregateOnDemandAlert_a.eb95 == new Double(1)
    }

    void "test setStatisticsScoresValues when smqCode is absent"() {
        when:
        service.setStatisticsScoresValues(aggregateOnDemandAlert_b,null,false)

        then:
        aggregateOnDemandAlert_b.ebgm == new Double(2)
        aggregateOnDemandAlert_b.eb05 == new Double(1)
        aggregateOnDemandAlert_b.eb95 == new Double(1)
    }
}