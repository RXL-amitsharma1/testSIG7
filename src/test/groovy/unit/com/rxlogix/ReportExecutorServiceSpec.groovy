package com.rxlogix

import com.google.common.collect.ImmutableList
import com.rxlogix.cache.CacheService
import com.rxlogix.config.*
import com.rxlogix.customException.ExecutionStatusException
import com.rxlogix.dto.SuperQueryDTO
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.enums.GroupType
import com.rxlogix.user.UserGroupMapping
import com.rxlogix.enums.QueryTypeEnum
import com.rxlogix.enums.ReportFormat
import com.rxlogix.enums.TemplateTypeEnum
import com.rxlogix.helper.NotificationHelper
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.SingleOnDemandAlertService
import com.rxlogix.user.Group
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import com.rxlogix.util.DateUtil
import spock.lang.Ignore
import com.rxlogix.util.ViewHelper
import grails.test.hibernate.HibernateSpec
import grails.testing.services.ServiceUnitTest
import grails.util.Holders
import grails.validation.ValidationException
import groovy.sql.Sql
import groovyx.net.http.Method
import org.grails.spring.beans.factory.InstanceFactoryBean
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.slf4j.Logger
import org.slf4j.event.LoggingEvent
import org.springframework.context.MessageSource
import org.springframework.jdbc.datasource.SimpleDriverDataSource
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges

import com.rxlogix.enums.QueryOperatorEnum

import javax.sql.DataSource
import java.sql.Connection
import java.text.SimpleDateFormat
import java.time.LocalDateTime

@ConfineMetaClassChanges([User,MessageSource,Configuration,ReportExecutorService,Sql])
class ReportExecutorServiceSpec extends Specification implements ServiceUnitTest<ReportExecutorService> {
    public static final String TEST_TZ = "UTC"
    public static final TimeZone ORIGINAL_TZ = TimeZone.getDefault()
    public static final user = "unitTest"
    @Shared Group wfGroup1
    @Shared User user1
    @Shared Configuration alertConfiguration,configuration1,configuration2
    @Shared ExecutedConfiguration executedConfiguration1
    @Shared ExecutionStatus executionStatus1
    @Shared ReportTemplate reportTemplate1
    @Shared Disposition disposition1,disposition2
    @Shared AlertDateRangeInformation alertDateRangeInformation1
    @Shared Logger logger= Mock(Logger)
    @Shared Closure successCallback,failureCallback
    @Shared String expected
    @Shared AlertCase alertCase1
    @Shared SingleCaseAlert singleCaseAlert1,singleCaseAlert2
    @Shared Priority priority1
    @Shared TemplateQuery templateQuery1
    @Shared SuperQueryDTO superQueryDTO
    @Shared AlertStopList alertStopList1
    QueryValueList queryValueList
    QueryValueList queryValueList1
    SqlGenerationService sqlGenerationService

    void setup() {
        String vaersStatement = "select ETL_VALUE AS vaers_date  from pvr_etl_constants where etl_key ='VAERS_LATEST_PROCESSED_DATE'"
        alertStopList1=new AlertStopList(productName: '{"3":[{"name":"paracetamol11"},{"name":"paracetamol12"}],"2":[{"name":"paracetamol21"},{"name":"paracetamol22"}]}',
                eventName: '{"4":[{"name":"event11"},{"name":"event12"}]}',dateCreated: new Date(),createdBy: "createdBy",
        activated: true,)
        alertStopList1.save(flush:true,failOnError:true)
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss zz yyyy")
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"))
        def dataSource=Mock(DataSource)
        service.signalDataSourceService = [getReportConnection: { String selectedDatasource -> return dataSource}]
        service.dataSource=dataSource
        service.dataSource_pva=dataSource
        priority1 = new Priority([displayName: "mockPriority", value: "mockPriority", display: true, defaultPriority: true, reviewPeriod: 1])
        priority1.save(failOnError: true,flush:true)
        AlertCase.withTransaction {
            alertCase1=new AlertCase(caseNumber: "1",caseVersion: "1",caseId: 1L,tenantId: 1,isDeleted: false)
            alertCase1.save(flush:true,failOnError:true)
        }
        disposition1 = new Disposition(value: "ValidatedSignal1", displayName: "Validated Signal1", validatedConfirmed: true,abbreviation: "C")
        disposition1.save(failOnError: true)
        disposition2 = new Disposition(value: "ValidatedSignal2", displayName: "Validated Signal2", validatedConfirmed: true,abbreviation: "A")
        disposition2.save(failOnError: true)
        wfGroup1 = new Group(name: "Default1", groupType: GroupType.WORKFLOW_GROUP, createdBy: 'createdBy', modifiedBy: 'modifiedBy',
        defaultDisposition: disposition1,defaultSignalDisposition: disposition1)
        wfGroup1.save(validate: false)
        Preference preference = new Preference(locale: new Locale("en"),createdBy: "createdBy",modifiedBy: "modifiedBy")
        user1 = new User(username: 'admin', createdBy: 'createdBy', modifiedBy: 'modifiedBy', fullName: 'Admin User',groups: [wfGroup1],preference:preference)
        user1.save(flush:true,failOnError:true)
        reportTemplate1=new ReportTemplate(id: 1L,name: "reportTemplate1",description: "desc1",owner: user1,
        createdBy: "user1",modifiedBy: "user1",templateType: TemplateTypeEnum.TEMPLATE_SET)
        reportTemplate1.save(flush:true,failOnError:true)
        ParameterValue parameterValue1=new ParameterValue(key: "1",value: "one")
        parameterValue1.save(flush:true,failOnError:true)
        QueryValueList queryValueList1=new QueryValueList(query:1L,queryName:"query1"/*,parameterValues: [parameterValue1]*/)
        queryValueList1.save(flush:true,failOnError:true)
        alertDateRangeInformation1 = new AlertDateRangeInformation(dateRangeEndAbsolute: new Date() + 4, dateRangeStartAbsolute: new Date(),
                dateRangeEndAbsoluteDelta: 13, dateRangeStartAbsoluteDelta: 10, dateRangeEnum: DateRangeEnum.CUSTOM)
        DateRangeInformation dateRangeInformation1=new DateRangeInformation(dateRangeStartAbsoluteDelta: 1,dateRangeEndAbsoluteDelta: 1,
        dateRangeEndAbsolute: new Date()+1,dateRangeStartAbsolute: new Date())
        configuration1 = new Configuration(assignedTo: user1, productSelection : """{"1":[],"2":[],"3":[{"name":"Test Product AJ Capsule 100 millimole","id":"100004"}],"4":[]}""", name: "test",
                owner: user1, createdBy: user1.username, modifiedBy: user1.username, priority: priority1,
        selectedDatasource: "faers",type: "Aggregate Case Alert",executing: false,adhocRun: false,scheduleDateJSON:
                '{"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;FREQ=HOURLY"}',nextRunDate:new Date()-2,template: reportTemplate1,
        alertDateRangeInformation: alertDateRangeInformation1,alertTriggerCases: 1,alertTriggerDays:0,isEnabled: true,
        alertQueryValueLists: [queryValueList1],asOfVersionDate: new Date(),productGroupSelection:"select ",eventSelection: '{"7":[{"name":"Narrow","id":"1"},{"name":"event12","id":"2"}]}]')
        configuration1.save(failOnError: true)
        configuration2 = new Configuration(assignedTo: user1, productSelection : "[TestProduct1]", name: "test1",
                owner: user1, createdBy: user1.username, modifiedBy: user1.username, priority: priority1,
                selectedDatasource: "faers",type: "Aggregate Case Alert",executing: false,adhocRun: false,scheduleDateJSON:
                '{"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;FREQ=HOURLY;FREQ=HOURLY"}',nextRunDate:new Date()-2,template: reportTemplate1,
                alertDateRangeInformation: alertDateRangeInformation1,alertTriggerCases: 1,alertTriggerDays:0,isEnabled: true)
        configuration2.save(failOnError: true)
        templateQuery1=new TemplateQuery(template: 1L,query: 1L,templateName: "temp1",queryName: "query1",
                dateRangeInformationForTemplateQuery: dateRangeInformation1,dateCreated: new Date(),lastUpdated: new Date(),
                createdBy: user1.username,modifiedBy: user1.username,report: configuration1)
        templateQuery1.save(flush:true,failOnError:true)
        //
        alertConfiguration =new Configuration(assignedTo: user1, productSelection : "[TestProduct]", name: "test0",
                owner: user1, createdBy: user1.username, modifiedBy: user1.username, priority: priority1,
                selectedDatasource: "faers",type: "Aggregate Case Alert",executing: false,adhocRun: false,scheduleDateJSON:
                '{"recurrencePattern":"FREQ=DAILY;INTERVAL=1;COUNT=1;FREQ=HOURLY"}',nextRunDate:new Date()-2,template: reportTemplate1,
                alertDateRangeInformation: alertDateRangeInformation1,alertTriggerCases: 1,alertTriggerDays:0,isEnabled: true,
                alertQueryValueLists: [queryValueList1],asOfVersionDate: new Date(),productGroupSelection:"select ",eventSelection: '{"7":[{"name":"Narrow","id":"1"},{"name":"event12","id":"2"}]}]')

        queryValueList = new QueryValueList(query: 2, queryName: "QueryName")
        QueryExpressionValue parameterValue = new QueryExpressionValue(key: "key", value: "value", reportField: new ReportField(name: "name"), operator: QueryOperatorEnum.EQUALS)
        queryValueList.addToParameterValues(parameterValue)
        alertConfiguration.addToAlertQueryValueLists(queryValueList)
        alertConfiguration.save(failOnError: true)
        //

        executedConfiguration1 = new ExecutedConfiguration(name: "test", isLatest: true, adhocRun: false,
                owner: user1, scheduleDateJSON: "{}", nextRunDate: new Date(), type: Constants.AlertConfigType.AGGREGATE_CASE_ALERT,
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
                createdBy: user1.username, modifiedBy: user1.username,
                assignedTo: user1,
                pvrCaseSeriesId: 1,
                pvrCumulativeCaseSeriesId: 1,
                missedCases:true , configId: configuration1.id,
                selectedDatasource: "pva",
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 1)
        executedConfiguration1.save(flush:true,failOnError:true)
        executionStatus1 = new ExecutionStatus(configId: configuration1.id,
                startTime: new Date().getTime(),executedConfigId: executedConfiguration1.id,
                endTime: new Date().getTime(),
                nextRunDate: new Date(),
                reportVersion: configuration1.numOfExecutions+1, message: 'for testing purpose',
                owner: user1, name: 'executionStatus',type: executedConfiguration1.type,
                executionStatus: ReportExecutionStatus.GENERATING)
        executionStatus1.save(flush:true, failOnError: true)
        singleCaseAlert1=new SingleCaseAlert(assignedTo: user1,priority: priority1,detectedDate: new Date(),
                disposition: disposition1,followUpExists: false,isNew: true,name: "singleCaseAlert1",
                productFamily: "family1",productId: 1,productName: "product1",pt: "preferredTerm1",createdBy: "test_user",
                modifiedBy: "test_user",dateCreated: new Date()-3,lastUpdated: new Date(),attributes: """{"masterInitReptDate_4":"2020-02-22T2020-02-24"}""",
                alertConfiguration: configuration1,executedAlertConfiguration: executedConfiguration1,comboFlag:'combo',
                malfunction: 'mal')
        singleCaseAlert1.save(flush:true,failOnError:true)
        singleCaseAlert2=new SingleCaseAlert(assignedTo: user1,priority: priority1,detectedDate: new Date(),
                disposition: disposition1,followUpExists: false,isNew: true,name: "singleCaseAlert2",
                productFamily: "family2",productId: 1,productName: "product2",pt: "preferredTerm2",createdBy: "test_user",
                modifiedBy: "test_user",dateCreated: new Date()-3,lastUpdated: new Date(),attributes: """{"masterInitReptDate_4":"2020-02-22T2020-02-24"}""",
                alertConfiguration: configuration1,executedAlertConfiguration: executedConfiguration1,comboFlag:'combo',
                malfunction: 'mal')
        singleCaseAlert2.save(flush:true,failOnError:true)
        EmailNotificationService emailNotificationService=Mock(EmailNotificationService)
        emailNotificationService.emailNotificationWithBulkUpdate(*_)>>{
            return false
        }
        service.emailNotificationService=emailNotificationService
        NotificationHelper notificationHelper=Mock(NotificationHelper)
        service.notificationHelper=notificationHelper
        Configuration.metaClass.static.lock = Configuration.&get
        CacheService cacheService=Mock(CacheService)
        service.cacheService=cacheService
        QueryService queryService=Mock(QueryService)
        service.queryService=queryService
        SingleCaseAlertService singleCaseAlertService=Mock(SingleCaseAlertService)
        singleCaseAlertService.createAlert(*_)>>{
            [1]
        }
        service.singleCaseAlertService=singleCaseAlertService
        sqlGenerationService=Mock(SqlGenerationService)
        sqlGenerationService.initializeAlertGtts(*_)>>{
            return "INSERT into gtt"
        }
        sqlGenerationService.setReassessContextForQuery(_,_)>>{
            return ["query1"]
        }
        sqlGenerationService.selectedFieldsCustomProcedures(_,_,_)>>{
            return "query2"
        }
        sqlGenerationService.getInsertStatementsToInsert(_,_,_)>>{
            return "query3"
        }
        sqlGenerationService.setReassessContextForTemplate(_,_)>>{
            return "query3"
        }
        sqlGenerationService.initializeGTTForMissedCases(*_)>>{
            return "initialize gtt missedCases"
        }
        sqlGenerationService.generateCustomReportSQL(_,_)>>{
            return """
            SELECT
                PRODUCT_NAME AS PRODUCT_NAME,
                PRODUCT_ID AS PRODUCT_ID,
            FROM PVS_APP_AGG_COUNTS_${configuration1.id} where PT_NAME IS NOT NULL
        """
        }
        sqlGenerationService.generateCaseLineListingSql(_,_)>>{
            return "generating caseline"
        }
        service.sqlGenerationService=sqlGenerationService
        SingleOnDemandAlertService singleOnDemandAlertService=Mock(SingleOnDemandAlertService)
        singleOnDemandAlertService.createAlert(_,_,_)>>{
            [1]
        }
        service.singleOnDemandAlertService=singleOnDemandAlertService
        successCallback={Long id1,Long id2->
            expected="success"
        }
        failureCallback={Long id1,Long id2=null,ExecutionStatusException ese->
            expected="fail"
        }
        ConfigurationService configurationService=Mock(ConfigurationService)
        configurationService.getNextDate(_)>>{
            return new Date()+2
        }
        configurationService.getDelta(_)>>{
            return 4
        }
        configurationService.getUpdatedStartandEndDate(_) >> {
            return [new Date(), new Date()]
        }
        service.configurationService=configurationService
        AlertService alertService=Mock(AlertService)
        service.alertService=alertService
        StatisticsService statisticsService=Mock(StatisticsService)
        service.statisticsService=statisticsService
        BusinessConfigurationService businessConfigurationService=Mock(BusinessConfigurationService)
        service.businessConfigurationService=businessConfigurationService
        service.currentFaersAlert=new Tuple2(configuration1.id, LocalDateTime.now())
        service.currentlyFaersRunning=[configuration1.id]
        UserGroupService userGroupService=Mock(UserGroupService)
        userGroupService.fetchUserListForGroup(_)>>{
            return [user1]
        }
        service.userGroupService=userGroupService
        UserService userService=Mock(UserService)
        userService.getUser()>>{
            user1
        }
        service.userService=userService
        Sql.metaClass.executeUpdate={String s->alertCase1.isDeleted=true}
        MessageSource.metaClass.getMessage={String s,Object[] l,Locale l1->
            return "message"
        }
        service.reportIntegrationService = [get:{String url, String path, Map query ->
            [status: 200, data: [result: [[ id: "id", text: "caseSeriesName"]],totalCount: 1]]}]
        Sql.metaClass.execute={String s->}
        Sql.metaClass.call={GString s->}
        Sql.metaClass.call={String s->}
        Sql.metaClass.eachRow={String query, Closure c->}
    }


    void setupSpec() {
        TimeZone.setDefault(TimeZone.getTimeZone(TEST_TZ))

    }

    def cleanup() {
        expected=""
    }

    def cleanupSpec() {
        TimeZone.setDefault(ORIGINAL_TZ)
        User.metaClass.encodePassword = null
    }

    void "test runConfigurations"(){
        when:
        service.runConfigurations(Constants.AlertConfigType.AGGREGATE_CASE_ALERT,[])
        then:
        ExecutionStatus.findAllByExecutionStatus(ReportExecutionStatus.ERROR).size()==0
    }
    void "test runConfigurations when exception occcurs"(){
        setup:
        configuration1.selectedDatasource=Constants.DataSource.PVA
        configuration1.save(flush:true,failOnError:true)
        service.metaClass.executeReportJob={Configuration configuration, Closure successCallback, Closure failureCallback->
            throw new Exception()
        }
        when:
        service.runConfigurations(Constants.AlertConfigType.AGGREGATE_CASE_ALERT,[])
        then:
        ExecutionStatus.findAllByExecutionStatus(ReportExecutionStatus.ERROR).size()==1
    }
    void "test setValuesForConfiguration when adHoc run is false"(){
        setup:
        MessageSource.metaClass.getMessage={String s,Object[] l,Locale l1->
            return "message"
        }
        when:
        service.setValuesForConfiguration(configuration1,executedConfiguration1,executionStatus1.id, executionStatus1.id)
        then:
        configuration1.executing==false
        executionStatus1.executionStatus==ReportExecutionStatus.COMPLETED
    }
    void "test setValuesForConfiguration when exception occurs"(){
        when:
        service.setValuesForConfiguration(configuration1,executedConfiguration1,executionStatus1.id, executionStatus1.id)
        then:
        configuration1.executing==false
        executionStatus1.executionStatus==ReportExecutionStatus.COMPLETED
    }
    void "test createExecutionStatus"(){
        when:
        ExecutionStatus result=service.createExecutionStatus(configuration1)
        then:
        result.owner==user1
        result.name==configuration1.name
        result.executionStatus==ReportExecutionStatus.GENERATING
    }
    void "test handleFailedExecution when adHoc run is false"(){
        setup:
        ExecutionStatusException executionStatusException=new ExecutionStatusException(errorCause: "wrong configuration")
        when:
        service.handleFailedExecution(executionStatusException,configuration1.id,executedConfiguration1.id, configuration1.type)
        then:
        configuration1.isEnabled==true
    }
    void "test handleFailedExecution when adHoc run is true"(){
        setup:
        configuration1.adhocRun=true
        configuration1.save(flush:true,failOnError:true)
        ExecutionStatusException executionStatusException=new ExecutionStatusException(errorCause: "wrong configuration")
        when:
        service.handleFailedExecution(executionStatusException,configuration1.id,null, configuration1.type,executionStatus1.id)
        then:
        configuration1.isEnabled==false
    }
    void "test getExecutionQueueSize"(){
        when:
        Integer result=service.getExecutionQueueSize()
        then:
        result==0
    }
    void "test getFaersExecutionQueueSize"(){
        when:
        Integer result=service.getFaersExecutionQueueSize()
        then:
        result==1
    }
    void "test getQuantExecutionSize"(){
        when:
        Integer result=service.getQuantExecutionSize()
        then:
        result==0
    }
    void "test executeReportJob when configuration type is not AggregateCaseAlert"(){
        setup:
        configuration1.type="Single Case Alert"
        configuration1.save(flush:true,failOnError:true)
        service.metaClass.generateAlertResultQualitative={Configuration configuration, ExecutedConfiguration executedConfiguration,
                                                          SuperQueryDTO superQueryDTO, boolean isAggregateCase->
            return [[:]]
        }
        service.metaClass.clearDataMiningTables={Long l1,Long l2->}
        when:
        service.executeReportJob(configuration1,successCallback,failureCallback)
        then:
        expected=="success"
    }
    void "test executeAlertJobQualitative when adhocRun is false"(){
        setup:
        service.metaClass.generateAlertResultQualitative={Configuration configuration, ExecutedConfiguration executedConfiguration,
                                                          SuperQueryDTO superQueryDTO, boolean isAggregateCase->
            return [[:]]
        }
        service.metaClass.clearDataMiningTables={Long l1,Long l2->}
        when:
        service.executeAlertJobQualitative(configuration1,true,successCallback,failureCallback)
        then:
        expected=="success"
    }
    void "test executeAlertJobQualitative when adhocRun is true"(){
        setup:
        configuration1.adhocRun=true
        configuration1.save(flush:true,failOnError:true)
        service.metaClass.generateAlertResultQualitative={Configuration configuration, ExecutedConfiguration executedConfiguration,
                                                          SuperQueryDTO superQueryDTO, boolean isAggregateCase->
            return [[:]]
        }
        service.metaClass.clearDataMiningTables={Long l1,Long l2->}
        when:
        service.executeAlertJobQualitative(configuration1,true,successCallback,failureCallback)
        then:
        expected=="success"
    }
    void "test executeAlertJobQualitative when exception occurs second time"(){
        setup:
        service.metaClass.generateAlertResultQualitative={Configuration configuration, ExecutedConfiguration executedConfiguration,
                                                          SuperQueryDTO superQueryDTO, boolean isAggregateCase->
            throw new Exception()
        }
        service.metaClass.clearDataMiningTables={Long l1,Long l2->}
        when:
        service.executeAlertJobQualitative(configuration1,true,successCallback,failureCallback)
        then:
        expected=="fail"
    }
    void "test executeAlertJobQualitative when exception occurs third time"(){
        setup:
        service.metaClass.generateAlertResultQualitative={Configuration configuration, ExecutedConfiguration executedConfiguration,
                                                          SuperQueryDTO superQueryDTO, boolean isAggregateCase ->
            throw new Exception()
        }
        service.metaClass.clearDataMiningTables={Long l1,Long l2->}
        when:
        service.executeAlertJobQualitative(configuration1,true,successCallback,failureCallback)
        then:
        expected=="fail"
    }
    void "test executeAlertJobQualitative when configuration is not enabled"(){
        setup:
        configuration1.isEnabled=false
        configuration1.save(flush:true,failOnError:true)
        when:
        service.executeAlertJobQualitative(configuration1,true,successCallback,failureCallback)
        then:
        logger.info("${configuration1.name} is not enabled. Skipping")
    }

    @Ignore
    void  "test triggerAlertAtThreshold when triggerCasesDays==0"(){
        setup:
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss zz yyyy")
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"))
        singleCaseAlert1.dateCreated=new Date(sdf.format(new Date()))-3
        singleCaseAlert2.dateCreated=new Date(sdf.format(new Date()))-3
        singleCaseAlert1.save(flush:true,failOnError:true)
        singleCaseAlert2.save(flush:true,failOnError:true)
        configuration1.alertTriggerDays=10
        configuration1.alertTriggerCases=1
        configuration1.save(flush:true,failOnError:true)
        Sql.metaClass.executeUpdate={String s->alertCase1.isDeleted=true}
        when:
        service.triggerAlertAtThreshold(configuration1,executedConfiguration1,[1,2])
        then:
        alertCase1.isDeleted==true
    }

    @Ignore
    void  "test triggerAlertAtThreshold when triggerCasesDays!=0"(){
        setup:
        Sql.metaClass.executeUpdate={String s->alertCase1.isDeleted=true}
        when:
        service.triggerAlertAtThreshold(configuration1,executedConfiguration1,[1,2])
        then:
        alertCase1.isDeleted==true
    }
    void "test updateCapturedCases on success"(){
        setup:
        Sql.metaClass.executeUpdate={String s->alertCase1.isDeleted=true}
        when:
        service.updateCapturedCases([1,2])
        then:
        alertCase1.isDeleted==true
    }
    void "test updateCapturedCases when exception occurs"(){
        setup:
        Sql.metaClass.executeUpdate={String s->throw new Exception()}
        when:
        service.updateCapturedCases([1,2])
        then:
        alertCase1.isDeleted==false
    }
    void "test saveExecutedConfiguration"(){
        when:
        ExecutedConfiguration result=service.saveExecutedConfiguration(configuration1)
        then:
        result.owner==user1
        result.executedAlertQueryValueLists!=null
    }
    void "test addExecutedTemplateQueryToExecutedConfiguration"(){
        when:
        service.addExecutedTemplateQueryToExecutedConfiguration(templateQuery1.id,executedConfiguration1)
        then:
        executedConfiguration1.executedTemplateQueries!=null
    }
    void "test setNextRunDateForConfiguration when adhocRun is false"(){
        when:
        service.setNextRunDateForConfiguration(configuration1)
        then:
        configuration1.nextRunDate>=new Date()
    }
    void "test setNextRunDateForConfiguration when adhocRun is true"(){
        setup:
        configuration1.adhocRun=true
        configuration1.save(flush:true,failOnError:true)
        when:
        service.setNextRunDateForConfiguration(configuration1)
        then:
        configuration1.nextRunDate==null
    }
    void "test adjustCustomDateRanges when configuration contains both run once and hourly"(){
        when:
        service.adjustCustomDateRanges(configuration1)
        then:
        configuration1.asOfVersionDate.day==new Date().day
    }
    void "test adjustCustomDateRanges when configuration doesn't contains both run once and hourly"(){
        setup:
        configuration1.scheduleDateJSON='{"recurrencePattern":""}'
        configuration1.save(flush:true,failOnError:true)
        when:
        service.adjustCustomDateRanges(configuration1)
        then:
        configuration1.asOfVersionDate.day!=new Date().day
    }
    @Ignore
    void "test addNotification when executionStatus is completed "(){
        setup:
        executionStatus1.executionStatus=ReportExecutionStatus.COMPLETED
        executionStatus1.save(flush:true,failOnError:true)
        when:
        service.addNotification(executedConfiguration1,executionStatus1,user1,wfGroup1)
        then:
        InboxLog.list()!=[]
        InboxLog.list().get(0).type=="Quantitative Alert Execution"
        InboxLog.list().get(0).level==NotificationLevel.INFO
    }
    @Ignore
    void "test addNotification when executionStatus is warning "(){
        setup:
        executedConfiguration1.type=Constants.AlertConfigType.SINGLE_CASE_ALERT
        executedConfiguration1.save(flush:true,failOnError:true)
        executionStatus1.executionStatus=ReportExecutionStatus.WARN
        executionStatus1.save(flush:true,failOnError:true)
        when:
        service.addNotification(executedConfiguration1,executionStatus1,user1,wfGroup1)
        then:
        InboxLog.list()!=[]
        InboxLog.list().get(0).type=="Qualitative Alert Execution"
        InboxLog.list().get(0).level==NotificationLevel.WARN
    }
    @Ignore
    void "test addNotification when executionStatus is generating "(){
        setup:
        executionStatus1.executionStatus=ReportExecutionStatus.SCHEDULED
        executionStatus1.save(flush:true,failOnError:true)
        when:
        service.addNotification(executedConfiguration1,executionStatus1,user1,wfGroup1)
        then:
        InboxLog.list()!=[]
        InboxLog.list().get(0).type=="Quantitative Alert Execution"
        InboxLog.list().get(0).level==NotificationLevel.ERROR
    }
    void "test debugReportSQL when isAggregate is false"(){
        when:
        List result=service.debugReportSQL(configuration1,executedConfiguration1,false)
        then:
        result[0].configurationId==configuration1.id as String
    }
    void "test debugReportSQL when isAggregate is true"(){
        when:
        List result=service.debugReportSQL(configuration1,executedConfiguration1,true)
        then:
        result[0].configurationId==configuration1.id as String
    }
    void "test debugReportSQL when exception occurs"(){
        setup:
        QueryService queryService=Mock(QueryService)
        queryService.queryDetail(_)>>{
            throw new Exception()
        }
        service.queryService=queryService
        when:
        List result=service.debugReportSQL(configuration1,executedConfiguration1,false)
        then:
        result==[]
    }
    void "test initiateAggregateDataMining when selectedDatasource is faers"(){
        when:
        String result=service.initiateAggregateDataMining(executedConfiguration1,configuration1,'faers')
        then:
        result!=""
    }
    void "test initiateAggregateDataMining when selectedDatasource is not faers"(){
        setup:
        configuration1.selectedDatasource="evdas"
        configuration1.save(flush:true,failOnError:true)
        when:
        String result=service.initiateAggregateDataMining(executedConfiguration1,configuration1,'pva')
        then:
        result!=""
    }
    void "test initializeGttTables"(){
        when:
        List result=service.initializeGttTables(configuration1,executedConfiguration1,null,null,1L,true,false)
        then:
        result!=[]
        result.size()==5
    }
    void "test stopListWithProc"(){
        when:
        String result=service.stopListWithProc(null)
        then:
        result!=""
    }
    void "test stopListWithProc when productName in alert contains only 2"(){
        setup:
        alertStopList1.productName= '{"2":[{"name":"paracetamol21"},{"name":"paracetamol22"}]}'
        alertStopList1.save(flush:true,failOnError:true)
        when:
        String result=service.stopListWithProc(null)
        then:
        result!=""
    }
    void "test processTemplate"(){
        when:
        List result=service.processTemplate(null,true,executedConfiguration1.id,false)
        then:
        result[0]=="""
            SELECT
                PRODUCT_NAME AS PRODUCT_NAME,
                PRODUCT_ID AS PRODUCT_ID,
            FROM PVS_APP_AGG_COUNTS_${configuration1.id} where PT_NAME IS NOT NULL
        """
    }
    void "test processTemplate when isAggregate is false"(){
        when:
        List result=service.processTemplate(null,false,executedConfiguration1.id)
        then:
        result[0]=="generating caseline"
    }

    void "test generateCaseSeries on success"(){
        setup:
        ReportIntegrationService reportIntegrationService=Mock(ReportIntegrationService)
        reportIntegrationService.fetchPublicToken()>>{
            return ["header":"Header"]
        }
        service.reportIntegrationService=reportIntegrationService
        when:
        Map result=service.generateCaseSeries("sName","data",true)
        then:
        result.status==200
    }
    void "test generateCaseSeries when exception occurs"(){
        when:
        Map result=service.generateCaseSeries("sName","data",true)
        then:
        result==null
    }
    void "test postData"(){
        given:
        ReportIntegrationService reportIntegrationService=Mock(ReportIntegrationService)
        reportIntegrationService.fetchPublicToken()>>{
            return ["header":"Header"]
        }
        service.reportIntegrationService=reportIntegrationService
        String baseUrl= Holders.config.pvreports.url
        String path = Holders.config.pvreports.saveCaseSeriesForSpotfire.uri
        Map query = [user       : user1.username,
                     seriesName : "seriesName",
                     caseNumbers: "caseData",
                     isTemporary: true]
        when:
        Map result=service.postData(baseUrl,path,query, Method.POST)
        then:
        result.status==200
    }
    void "test isFaers"(){
        when:
        Boolean result=service.isFaers(configuration1)
        then:
        result==true
    }
    void "test clearDataMiningTables"(){
        setup:
        Sql.metaClass.call={GString s-> executedConfiguration1.delete()}
        when:
        service.clearDataMiningTables(configuration1.id,executedConfiguration1.id)
        then:
        ExecutedConfiguration.first()==null
    }
    void "test clearDataMiningTables when exception occurs"(){
        setup:
        Sql.metaClass.call={GString s-> throw new Exception()}
        when:
        service.clearDataMiningTables(configuration1.id,executedConfiguration1.id)
        then:
        ExecutedConfiguration.first()!=null
    }
    void "test checkDataStatus"(){
        when:
        String result=service.checkDataStatus("pva",executedConfiguration1,1)
        then:
        result=="Exception occured in alert execution main DB flow."
    }
    void "test checkDataStatus when exception occurs"(){
        setup:
        Sql.metaClass.eachRow={String query, Closure c->throw new Exception()}
        when:
        String result=service.checkDataStatus("pva",executedConfiguration1,1)
        then:
        result=="Exception occured in alert execution main DB flow."
    }


     void "test startIntegratedReviewExecution"(){
         setup:
         configuration1.selectedDatasource="pva,eudra"
         configuration1.save(flush:true,failOnError:true)
         when:
         service.startIntegratedReviewExecution(configuration1,[])
         then:
         notThrown(Exception)

     }

     void "test startIntegratedReviewExecution when only pva"(){
         setup:
         String alertType = "Aggregate Case Alert"
         configuration1.selectedDatasource="pva"
         configuration1.save(flush:true,failOnError:true)
         when:
         service.startIntegratedReviewExecution(alertType,configuration1,[])
         then:
         notThrown(Exception)

     }

    void "test fetchQuantAlertDataFromMart when there is no exception"() {
        setup:
        service.queryService = [queryDetail: { Long id -> }]
        service.metaClass.generateEbgmScores = { Configuration configuration, ExecutedConfiguration executedConfiguration, SuperQueryDTO superQueryDTO -> }
        service.aggregateCaseAlertService = [createAlert: { Long scheduledConfigId, Long executedConfigId, List<Map> alertData -> }]
        service.statisticsService = [calculateStatisticalScores: { String fileNameStr, String selectedDatasource, ExecutedConfiguration executedConfig -> }]
        service.metaClass.checkDataStatus = { String datasource, ExecutedConfiguration executedConfiguration, int alertExecutionTryCount -> "Alert execution main DB call flow is completed." }
        service.metaClass.generatePRRScores = { Configuration configuration, ExecutedConfiguration executedConfiguration -> }
        service.metaClass.prepareAlertDataFromMart = { Sql sql, boolean isAggregateCase, ExecutedConfiguration executedConfiguration, Configuration configuration -> }
        service.alertService = [saveAlertDataInFile: { ArrayList<Map> alertList, String fileName -> }]
        service.alertService =  [fetchFirstExecutionDate :{ ExecutedConfiguration executedConfiguration, Configuration configuration ->  }]
        when:
        service.fetchQuantAlertDataFromMart(configuration1,executedConfiguration1,"")
        then: 'no exceptions must be thrown'
        noExceptionThrown()
    }
    void "test generateEbgmScores when configuration is null"() {
        setup:
        service.metaClass.prepareGttsAndVersionSqls = { Configuration configuration, boolean hasQuery, boolean isAggregateCase, Sql sql, SuperQueryDTO superQueryDTO, long sectionStart -> }
        service.sqlGenerationService = [persistCaseSeriesExecutionData: { Sql sql, Long executedConfigurationId -> }]
        service.metaClass.initiateAggregateDataMining = { Long executedConfigurationId, Configuration configuration -> }
        Sql.metaClass.call = { String sql -> }
        service.queryService = [queryDetail: { Long id -> }]

        service.sqlGenerationService=sqlGenerationService
        when:
        service.generateEbgmScores(configuration1, executedConfiguration1, null, "pva")
        then: 'No exception must be thrown'
        noExceptionThrown()
        where:
        config              | executedConfiguration1
        null                | null
        alertConfiguration  | null
        null                | executedConfiguration1
        alertConfiguration  | executedConfiguration1
        configuration1 | executedConfiguration1
    }

    void "test generateEbgmScores when prepareGttsAndVersionSqls method throws exception"() {
        setup:
        service.metaClass.prepareGttsAndVersionSqls = { Configuration configuration, boolean hasQuery, boolean isAggregateCase, Sql sql, SuperQueryDTO superQueryDTO, long sectionStart ->
            throw new Exception()
        }
        when:
        service.generateEbgmScores(alertConfiguration, executedConfiguration, null)
        then: 'Exception must be thrown'
        thrown Exception
    }

    void "test generateEbgmScores when persistCaseSeriesExecutionData method throws exception"() {
        setup:
        service.metaClass.prepareGttsAndVersionSqls = { Configuration configuration, boolean hasQuery, boolean isAggregateCase, Sql sql, SuperQueryDTO superQueryDTO, long sectionStart -> }
        service.sqlGenerationService = [persistCaseSeriesExecutionData: { Sql sql, Long executedConfigurationId -> throw new Exception() }]
        when:
        service.generateEbgmScores(alertConfiguration, executedConfiguration, null)
        then: 'Exception must be thrown'
        thrown Exception
    }

    void "test generateEbgmScores when sql connection throws exception"() {
        setup:
        service.dataSource_faers = null
        service.metaClass.prepareGttsAndVersionSqls = { Configuration configuration, boolean hasQuery, boolean isAggregateCase, Sql sql, SuperQueryDTO superQueryDTO, long sectionStart -> }
        service.sqlGenerationService = [persistCaseSeriesExecutionData: { Sql sql, Long executedConfigurationId -> throw new Exception() }]
        when:
        service.generateEbgmScores(alertConfiguration, executedConfiguration, null)
        then: 'Exception must be thrown'
        thrown Exception
    }
   
    void "test generatePRRScores when exception is thrown"() {
        setup:
        service.dataSource_faers = null
        when:
        service.generatePRRScores(alertConfiguration, executedConfiguration)
        then: 'Exception must be thrown'
        thrown Exception
    }

    void "test fetchQuantAlertDataFromMart when generatePRRScores throws exception"() {
        setup:
        service.queryService = [queryDetail: { Long id -> }]
        service.metaClass.generateEbgmScores = { Configuration configuration, ExecutedConfiguration executedConfiguration, SuperQueryDTO superQueryDTO -> }
        service.statisticsService = [calculateStatisticalScores: { String fileNameStr, String selectedDatasource, ExecutedConfiguration executedConfig -> }]
        service.metaClass.checkDataStatus = { String datasource, ExecutedConfiguration executedConfiguration, int alertExecutionTryCount -> }
        service.metaClass.generatePRRScores = { Configuration configuration, ExecutedConfiguration executedConfiguration -> throw new Exception() }
        service.metaClass.clearDataMiningTables = { Long scheduledConfigurationId, Long executedConfigId -> }
        service.alertService = [exceptionString: { Throwable throwable -> }]
        when:
        service.fetchQuantAlertDataFromMart(alertConfiguration,executedConfiguration)
        then: 'Exception must be thrown'
        thrown Exception
    }

    void "test getExecutedCaseSeriesId"(){
        setup:
        service.reportIntegrationService = [get:{String url, String path, Map query->
            [status: 200, data: [id:2L]]}]
        when:
        Long result = service.getExecutedCaseSeriesId(1L)
        then :
        result == 2L
    }

    void "test getVaersExecutionQueueSize"(){

        when:
        int result=service.getVaersExecutionQueueSize()
        then:
        result ==0

    }

    void "test startIntegratedReviewExecution when only Vaers"(){
        setup:
        String alertType = "Aggregate Case Alert"
        configuration1.selectedDatasource="vaers"
        configuration1.save(flush:true,failOnError:true)
        when:
        service.startIntegratedReviewExecution(alertType,configuration1,[])
        then:
        notThrown(Exception)

    }

    void "test runConfigurationsIntegratedReview when only Vaers"(){
        setup:
        String alertType = "Aggregate Case Alert"
        String dataSource = "vaers"
        when:
        service.runConfigurationsIntegratedReview(alertType,dataSource,[])
        then:
        notThrown(Exception)

    }

    void "test generatePRRScores when only Vaers"(){
        setup:
        String dataSource = "vaers"
        when:
        service.generatePRRScores(configuration1,executedConfiguration1, dataSource)
        then:
        notThrown(Exception)

    }

    void "test getVaersDateRange when multiple datasource including vaers"(){
        setup:
        def mockedSource = Mock(DataSource)
        service.dataSource_vaers = mockedSource
        String date = "[LATEST_UPLOADED_DATE_RANGE:01-01-2024/28-06-2024]"
        Sql.metaClass.eachRow = { String query, List criteria, Closure c ->
            c.call(date)
        }
        Map dateMap = [
                '01-01-2024': new DateTime(2024, 1, 1, 0, 0, DateTimeZone.forID('UTC')).toDate(),
                '28-06-2024': new DateTime(2024, 6, 28, 0, 0, DateTimeZone.forID('UTC')).toDate()
        ]
        DateUtil.metaClass.static.StringToDate = { String strDate, String format ->
            return dateMap[strDate] ?: new DateTime(1970, 1, 1, 0, 0, DateTimeZone.forID('UTC')).toDate()
        }
        when:
        Map result = service.getVaersDateRange()
        then:
        result == [startDate: "01-Jan-2024", endDate: "28-Jun-2024", vaersDate: "01-Jan-2024" + ' - ' + "28-Jun-2024"]

    }

    void "test generateProductName"(){
        setup:
        def mockedSource = Mock(DataSource)
        ViewHelper viewHelper=Mock(ViewHelper)
        String date = "[VAERS_DATE:27-08-2021]"
        service.dataSource_vaers = mockedSource
        configuration1.selectedDatasource="pva,vaers"
        configuration1.productGroupSelection = '[{"name":"test-pg (32013)","id":"32013"}]'
        configuration1.save(flush:true, failOnError:true)
        PvsProductDictionaryService pvsProductDictionaryService = Mock(PvsProductDictionaryService)
        pvsProductDictionaryService.isLevelGreaterThanProductLevel(executedConfiguration1)>>{
            return false
        }
        service.pvsProductDictionaryService = pvsProductDictionaryService
        viewHelper.metaClass.getDictionaryValues( configuration1, DictionaryTypeEnum.PRODUCT_GROUP) >> {
            return "test-pg (32013) (product Group)"
        }
        Sql.metaClass.eachRow = { String query, List criteria, Closure c ->
            c.call(date)
        }
        when:
        service.generateProductName(executedConfiguration1)
        then:
        notThrown(Exception)

    }


}
