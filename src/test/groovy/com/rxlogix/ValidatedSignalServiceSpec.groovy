package com.rxlogix

import com.rxlogix.ActivityService
import com.rxlogix.AlertAttributesService
import com.rxlogix.CRUDService
import com.rxlogix.UserService
import com.rxlogix.ValidatedSignalService
import com.rxlogix.cache.CacheService
import com.rxlogix.config.Activity
import com.rxlogix.config.ActivityType
import com.rxlogix.config.ActivityTypeValue
import com.rxlogix.config.Configuration
import com.rxlogix.config.Disposition
import com.rxlogix.config.EVDASDateRangeInformation
import com.rxlogix.config.EvdasAlert
import com.rxlogix.config.EvdasConfiguration
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.config.ExecutedLiteratureConfiguration
import com.rxlogix.config.LiteratureAlert
import com.rxlogix.config.LiteratureConfiguration
import com.rxlogix.config.Meeting
import com.rxlogix.config.PVSState
import com.rxlogix.config.Priority
import com.rxlogix.config.ReportExecutionStatus
import com.rxlogix.config.SpecialPE
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.dto.SignalChartsDTO
import com.rxlogix.enums.ActionStatus
import com.rxlogix.enums.GroupType
import com.rxlogix.enums.SignalAssessmentDateRangeEnum
import com.rxlogix.mapping.MedDraSOC
import com.rxlogix.signal.AdHocAlert
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.Alert
import com.rxlogix.signal.GlobalArticle
import org.springframework.web.multipart.MultipartFile
import com.rxlogix.signal.SignalEmailLog
import com.rxlogix.signal.SignalHistory
import com.rxlogix.signal.SignalStatusHistory
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.Topic
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.MiscUtil
import com.rxlogix.util.SignalQueryHelper
import grails.converters.JSON
import grails.test.hibernate.HibernateSpec
import grails.testing.services.ServiceUnitTest
import groovy.sql.Sql
import groovy.transform.SourceURI
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.sql.JoinType
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import spock.util.mop.ConfineMetaClassChanges
import groovy.json.JsonSlurper

import javax.sql.DataSource
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.ResultSet

@ConfineMetaClassChanges([AlertService])
class ValidatedSignalServiceSpec extends HibernateSpec implements ServiceUnitTest<ValidatedSignalService> {

    @Shared User user
    @Shared Disposition defaultSignalDisposition
    @Shared Disposition defaultDisposition
    @Shared Disposition autoRouteDisposition
    @Shared Priority priority,priority2
    @Shared Group wfGroup
    @Shared ValidatedSignal validatedSignal,validatedSignal2,validatedSignal3
    @Shared Meeting meeting
    @Shared SignalStatusHistory signalStatusHistory,signalStatusHistory2
    @Shared SignalHistory signalHistory1,signalHistory2
    @Shared SingleCaseAlert singleCaseAlert
    @Shared AggregateCaseAlert aggregateCaseAlert,aggregateCaseAlert2
    @Shared AdHocAlert adHocAlert
    @Shared EvdasAlert evdasAlert,evdasAlert2
    @Shared MedDraSOC medDraSOC1,medDraSOC2
    @Shared LiteratureAlert literatureAlert
    @Shared SignalChartsDTO signalChartsDTO1
    @Shared ActivityType activityTypeCaseAdded,activityTypeCaseAssociated,activityTypePECAssociated,activityTypeAdhocAlertAssociated
    @Shared DataTableSearchRequest searchRequest

    List<Class> getDomainClasses() {
        [Disposition, Group, Priority, ValidatedSignal, User, SignalStatusHistory, ActivityType, Activity, Alert,
         AggregateCaseAlert, AdHocAlert, EvdasAlert, SignalHistory, Meeting, EVDASDateRangeInformation, EvdasConfiguration,
         SpecialPE]
    }
    def setup() {

        priority = new Priority(value: "High", display: true, displayName: "High", reviewPeriod: 3, priorityOrder: 1)
        priority.save(flush:true)
        priority2 = new Priority(value: "Low", display: true, displayName: "Low", reviewPeriod: 10, priorityOrder: 3)
        priority2.save(flush:true)
        defaultSignalDisposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "VO")
        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")
        [defaultDisposition, defaultSignalDisposition, autoRouteDisposition].collect { it.save(failOnError: true) }

        wfGroup = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP, createdBy: 'createdBy',
                modifiedBy: 'modifiedBy',
                defaultQualiDisposition: defaultDisposition,
                defaultQuantDisposition: defaultDisposition,
                defaultAdhocDisposition: defaultDisposition,
                defaultEvdasDisposition: defaultDisposition,
                defaultLitDisposition: defaultDisposition,
                defaultSignalDisposition: defaultSignalDisposition,
                autoRouteDisposition: autoRouteDisposition,
                justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(flush:true)

        user = new User(id: '1', username: 'test_user', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.addToGroups(wfGroup)
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.save(flush:true)

        signalStatusHistory = new SignalStatusHistory(dateCreated: new Date(), statusComment: "Test Status Comment", signalStatus: "signal Status1",
                dispositionUpdated: true,performedBy: "Test User",id:1)
        signalStatusHistory.save()

        signalStatusHistory2 = new SignalStatusHistory(dateCreated: new Date(), statusComment: "Test Status Comment", signalStatus: "signalStatus2",
                dispositionUpdated: true,performedBy: "Test User",id:1)

        Configuration configuration=new Configuration(assignedTo: user, productSelection : "[TestProduct]", name: "test",
                owner: user, createdBy: user.username, modifiedBy: user.username, priority: priority)
        configuration.save(flush:true,failOnError:true)
        ExecutedConfiguration executedConfiguration = new ExecutedConfiguration(id: 2L, name: "test",
                owner: user, scheduleDateJSON: "{}", nextRunDate: new Date(),
                description: "test", dateCreated: new Date(), lastUpdated: new Date(),
                isPublic: true, isDeleted: false, isEnabled: true,adhocRun: false,
                productSelection: '{"6":[{"name":"IFOSFAMIDE","id":"-470484647"}]}',
                configSelectedTimeZone: "UTC",
                limitPrimaryPath: true,
                includeMedicallyConfirmedCases: true,
                excludeFollowUp: false, includeLockedVersion: true,
                adjustPerScheduleFrequency: true,
                createdBy: user.username, modifiedBy: user.username,
                assignedTo: user,
                pvrCaseSeriesId: 22L,
                type: "executedConfiguration",
                isLatest: true,
                configId:configuration.id,
                workflowGroup: wfGroup,)
        executedConfiguration.save(failOnError: true)
        PVSState pvsstate =new PVSState(value:"state1", displayName: "pvstate1")
        pvsstate.save(flush:true,failOnError:true)
        singleCaseAlert=new SingleCaseAlert(assignedTo: user,priority: priority,detectedDate: new Date(),
                disposition: defaultSignalDisposition,followUpExists: false,isNew: true,name: "singleCaseAlert1",
                productFamily: "family1",productId: 1,productName: "product1",pt: "preferredTerm1",createdBy: "test_user",
                modifiedBy: "test_user",dateCreated: new Date(),lastUpdated: new Date(),attributes: """{"masterInitReptDate_4":"2020-02-22T2020-02-24"}""")
        singleCaseAlert.save(flush:true,failOnError:true)
        aggregateCaseAlert=new AggregateCaseAlert(priority: priority,disposition: defaultSignalDisposition,
                assignedTo: user,assignedToGroup: wfGroup,createdBy: "test_user",modifiedBy: "test_user",dateCreated: new Date(),
                lastUpdated: new Date(),name: "aggregateCaseAlert1",productName: "product1",pt: "aggrePt",alertConfiguration: configuration,
                executedAlertConfiguration: executedConfiguration,cumFatalCount: 10,cumSeriousCount: 4,cumSponCount: 6,
                cumStudyCount: 2,detectedDate: new Date(),dueDate: new Date()+1,eb05: 2,eb95: 9,ebgm: 6,newFatalCount: 2,
                newSeriousCount: 2,newSponCount: 3,newStudyCount: 1,productId: 1,prrValue: 1,ptCode: 2,rorValue: 8,soc: "SOC")
        aggregateCaseAlert.save(flush:true,failOnError:true)
        aggregateCaseAlert2=new AggregateCaseAlert(priority: priority,disposition: defaultSignalDisposition,
                assignedTo: user,assignedToGroup: wfGroup,createdBy: "test_user",modifiedBy: "test_user",dateCreated: new Date(),
                lastUpdated: new Date(),name: "aggregateCaseAlert1",productName: "product2",pt: "aggrePt2",alertConfiguration: configuration,
                executedAlertConfiguration: executedConfiguration,cumFatalCount: 10,cumSeriousCount: 4,cumSponCount: 6,
                cumStudyCount: 2,detectedDate: new Date(),dueDate: new Date()+1,eb05: 2,eb95: 9,ebgm: 6,newFatalCount: 2,
                newSeriousCount: 2,newSponCount: 3,newStudyCount: 1,productId: 1,prrValue: 1,ptCode: 2,rorValue: 8,soc: "SOC")
        aggregateCaseAlert2.save(flush:true,failOnError:true)
        adHocAlert=new AdHocAlert(priority: priority,disposition: defaultSignalDisposition,createdBy: "test_user",
                modifiedBy: "test_user",dateCreated: new Date(),lastUpdated: new Date(),name: "adHocAlert1" ,assignedTo: user,
                detectedBy: "company",detectedDate: new Date(),initialDataSource: "companyData",productSelection: "someProduct",
                topic: "topic1")
        adHocAlert.save(flush:true,failOnError:true)
        EVDASDateRangeInformation evdasDateRangeInformation=new EVDASDateRangeInformation(dateRangeStartAbsoluteDelta: 2,dateRangeEndAbsoluteDelta: 4,
                dateRangeStartAbsolute: new Date(),dateRangeEndAbsolute: new Date()+4)
        EvdasConfiguration evdasConfiguration=new EvdasConfiguration(name:"evdasConfiguration",assignedTo: user,createdBy: "test_user",
                dateRangeInformation: evdasDateRangeInformation,modifiedBy: "test_user",owner: user,priority: priority,productSelection: "product1")
        evdasConfiguration.save(flush:true,failOnError:true)
        ExecutedEvdasConfiguration executedEvdasConfiguration=new ExecutedEvdasConfiguration(name:"executedEvdasConfiguration",
                owner: user,createdBy: "test_user",executionStatus: ReportExecutionStatus.COMPLETED,modifiedBy: "test_user",
                productSelection: '{"6":[{"name":"IFOSFAMIDE","id":"-470484647"}]}',isDeleted: false,isEnabled: true,adhocRun: false,workflowGroup: wfGroup,configId: evdasConfiguration?.id)
        executedEvdasConfiguration.save(flush:true,failOnError:true)
        evdasAlert=new EvdasAlert(priority: priority,disposition: defaultSignalDisposition,createdBy: "test_user",
                modifiedBy: "test_user",dateCreated: new Date(),lastUpdated: new Date(),substance: "Substance1",name: "evdasAlert1",
                assignedTo: user,detectedDate: new Date()+1,ptCode: 1,substanceId: 1,alertConfiguration: evdasConfiguration,
                executedAlertConfiguration: executedEvdasConfiguration,soc: "SOC",pt: "evdasPt")
        evdasAlert.save(flush:true,failOnError:true)
        evdasAlert2=new EvdasAlert(priority: priority,disposition: defaultSignalDisposition,createdBy: "test_user",
                modifiedBy: "test_user",dateCreated: new Date(),lastUpdated: new Date(),substance: "Substance1",name: "evdasAlert1",
                assignedTo: user,detectedDate: new Date()+1,ptCode: 1,substanceId: 1,alertConfiguration: evdasConfiguration,
                executedAlertConfiguration: executedEvdasConfiguration)
        evdasAlert2.save(flush:true,failOnError:true)
        literatureAlert=new LiteratureAlert(priority: priority,disposition: defaultSignalDisposition,
                assignedToGroup: wfGroup,name: "litAlert1",assignedTo: user,searchString: "search",createdBy: "test_user",
                modifiedBy: "test_user",litSearchConfig: new LiteratureConfiguration(name: "litConf",assignedTo: user,createdBy: "test_user",modifiedBy: "test_user",
                owner: user,workflowGroup: wfGroup,priority: priority),exLitSearchConfig:
                new ExecutedLiteratureConfiguration(name: "exLitConf",assignedTo: user,createdBy: "test_user",modifiedBy: "test_user",
                        owner: user,workflowGroup: wfGroup,configId : 333),dateCreated: new Date(),lastUpdated: new Date())
        literatureAlert.save(flush:true,failOnError:true)
        activityTypeCaseAdded=new ActivityType(value: ActivityTypeValue.CaseAdded)
        activityTypeCaseAdded.save(flush:true,failOnError:true)
        activityTypeCaseAssociated=new ActivityType(value: ActivityTypeValue.CaseAssociated)
        activityTypeCaseAssociated.save(flush:true,failOnError:true)
        activityTypePECAssociated=new ActivityType(value: ActivityTypeValue.PECAssociated)
        activityTypePECAssociated.save(flush:true,failOnError:true)
        activityTypeAdhocAlertAssociated=new ActivityType(value: ActivityTypeValue.AdhocAlertAssociated)
        activityTypeAdhocAlertAssociated.save(flush:true,failOnError:true)
        Activity activity1 = new Activity(type: ActivityType.findByValue(ActivityTypeValue.CaseAssociated),
                performedBy: user, timestamp: DateTime.now(), justification: "change needed", assignedTo: user,
                details: "Case has been added", attributes: "attributes", assignedToGroup: wfGroup)
        activity1.save(flush:true,fainOnError:true)
        validatedSignal = new ValidatedSignal(name: "test_name", products: "test_products", endDate: new Date(),
                assignedTo: user, assignmentType: 'USER', modifiedBy: user.username, priority: priority,
                disposition: defaultSignalDisposition, createdBy: user.username, startDate: new Date(), id:1,
                genericComment: "Test notes", workflowGroup: wfGroup,sharedGroups: wfGroup,productDictionarySelection:
                "productDictionarySelection1")
        validatedSignal.addToSignalStatusHistories(signalStatusHistory)
        validatedSignal.save(flush:true)
        validatedSignal2 = new ValidatedSignal(name: "test_name2", products: "test_products2", endDate: new Date(),
                assignedTo: user, assignmentType: 'USER', modifiedBy: user.username, priority: priority,
                disposition: defaultSignalDisposition, createdBy: user.username, startDate: new Date(), id:1,
                genericComment: "Test notes", workflowGroup: wfGroup,sharedGroups: wfGroup,singleCaseAlerts: singleCaseAlert,
                aggregateAlerts: aggregateCaseAlert,adhocAlerts: adHocAlert,evdasAlerts: evdasAlert,literatureAlerts: literatureAlert,
                events: [1:[["name":"event1"]]],actionTaken: "action1"
        )
        validatedSignal2.addToSignalStatusHistories(signalStatusHistory)
        validatedSignal2.addToActivities(activity1)
        validatedSignal2.save(flush:true)
        aggregateCaseAlert.addToValidatedSignals(validatedSignal2)
        aggregateCaseAlert.save(flush:true,failOnError:true)
        evdasAlert.addToValidatedSignals(validatedSignal2)
        evdasAlert.save(flush:true,failOnError:true)
        validatedSignal3 = new ValidatedSignal(name: "test_name3", products: "test_products3", endDate: new Date(),
                assignedTo: user, assignmentType: 'USER', modifiedBy: user.username, priority: priority,
                disposition: defaultSignalDisposition, createdBy: user.username, startDate: new Date(), id:1,
                genericComment: "Test notes", workflowGroup: wfGroup,sharedGroups: wfGroup,singleCaseAlerts: singleCaseAlert,
                aggregateAlerts: aggregateCaseAlert,adhocAlerts: adHocAlert,evdasAlerts: evdasAlert,literatureAlerts: literatureAlert
        )
        validatedSignal3.addToSignalStatusHistories(signalStatusHistory)
        validatedSignal3.addToActivities(activity1)
        validatedSignal3.save(flush:true)
        Topic topic = new Topic(id: 1, name: "test_topic", products: "test_products", endDate: new Date(),
                assignedTo: user, assignmentType: 'USER', modifiedBy: user.username, priority: priority,
                disposition: defaultSignalDisposition, createdBy: user.username, startDate: new Date(), genericComment: "Test notes",
                workflowState: pvsstate,activities: activity1)
        topic.save(flush:true,fainOnError:true)
        meeting=new Meeting(meetingTitle: "meeting1",duration: 2L,meetingDate: new Date()+2,meetingOwner: user,
                meetingAgenda: "agenda1",meetingMinutes: "120mins",modifiedBy: "test_user",validatedSignal: validatedSignal)
        meeting.save(flush:true,failOnError:true)
        signalHistory1=new SignalHistory(assignedTo: user,group: wfGroup,priority: priority,disposition: defaultSignalDisposition,
                validatedSignal: validatedSignal,createdBy: "test_user",modifiedBy: "test_user",dateCreated: new Date(),
                lastUpdated: new Date(),isLatest: false,change: "change1")
        signalHistory1.save(flush:true,failOnError:true)
        signalHistory2=new SignalHistory(assignedTo: user,group: wfGroup,priority: priority,disposition: defaultSignalDisposition,
                validatedSignal: validatedSignal,createdBy: "username",modifiedBy: "username",dateCreated: new Date(),
                lastUpdated: new Date(),isLatest: true,change: "change2")
        signalChartsDTO1=new SignalChartsDTO(signalId: 1L,groupingCode: 1)
        UserService userService=Mock(UserService)
        userService.getUser()>>{
            return user
        }
        service.userService=userService
        ActivityService activityService=Mock(ActivityService)
        activityService.createActivityForSignal(_,_,_,ActivityType.findByValue(ActivityTypeValue.CaseAssociated),user,user,_,_)>>{
            Activity activity = new Activity(type: ActivityType.findByValue(ActivityTypeValue.CaseAssociated),
                    performedBy: user, timestamp: DateTime.now(), justification: "change needed", assignedTo: user,
                    details: "Case has been added", attributes: "attributes", assignedToGroup: wfGroup)
            validatedSignal.addToActivities(activity)
            validatedSignal.save(failOnError: true)
        }
        activityService.createActivityForSignal(_,_,_,ActivityType.findByValue(ActivityTypeValue.PECAssociated),user,user,_,_)>>{
            Activity activity = new Activity(type: ActivityType.findByValue(ActivityTypeValue.PECAssociated),
                    performedBy: user, timestamp: DateTime.now(), justification: "change needed", assignedTo: user,
                    details: "Case has been added", attributes: "attributes", assignedToGroup: wfGroup)
            validatedSignal.addToActivities(activity)
            validatedSignal.save(failOnError: true)
        }
        activityService.createActivityForSignal(_,_,_,ActivityType.findByValue(ActivityTypeValue.AdhocAlertAssociated),user,user,_,_)>>{
            Activity activity = new Activity(type: ActivityType.findByValue(ActivityTypeValue.AdhocAlertAssociated),
                    performedBy: user, timestamp: DateTime.now(), justification: "change needed", assignedTo: user,
                    details: "Case has been added", attributes: "attributes", assignedToGroup: wfGroup)
            validatedSignal.addToActivities(activity)
            validatedSignal.save(failOnError: true)
        }
        activityService.createActivityForSignal(_,_,_,ActivityType.findByValue(ActivityTypeValue.CaseAdded),user,user,_,_)>>{
            Activity activity = new Activity(type: ActivityType.findByValue(ActivityTypeValue.CaseAdded),
                    performedBy: user, timestamp: DateTime.now(), justification: "change needed", assignedTo: user,
                    details: "Case has been added", attributes: "attributes", assignedToGroup: wfGroup)
            validatedSignal.addToActivities(activity)
            validatedSignal.save(failOnError: true)
        }
        service.activityService=activityService
        ValidatedSignalChartService validatedSignalChartService=Mock(ValidatedSignalChartService)
        validatedSignalChartService.fetchChartDataFromRemoteDatabase(signalChartsDTO1)>>{
            return [[["date1"],["criteria1"]],[["date2"],["criteria2"]],[["date2"],["criteria2"]]]
        }
        service.validatedSignalChartService=validatedSignalChartService
        CacheService cacheService=Mock(CacheService)
        cacheService.prepareDefaultPriority()>>{
            return priority
        }
        service.cacheService=cacheService
        @SourceURI
        URI sourceUri
        Path scriptLocation = Paths.get(sourceUri)
        String directory=scriptLocation.toString().replace("ValidatedSignalServiceSpec.groovy","testingFiles/Details.html")
        File file=new File(directory)
        DynamicReportService dynamicReportService=Mock(DynamicReportService)
        dynamicReportService.createSignalSummaryReport(*_)>>{
            return file
        }
        dynamicReportService.createSignalActionDetailReport(_,_)>>{
            return file
        }
        dynamicReportService.createPeberSignalReport(_,_,_)>>{
            return file
        }
        service.dynamicReportService=dynamicReportService
        SignalHistoryService signalHistoryService=Mock(SignalHistoryService)
        signalHistoryService.saveSignalHistory(_)>>{
            signalHistory2.save(flush:true,failOnError:true)
        }
        service.signalHistoryService=signalHistoryService
        SpecialPEService specialPEService=Mock(SpecialPEService)
        specialPEService.isSpecialPE(_,_)>>{
            return true
        }
        service.specialPEService=specialPEService
        def dataSource = Mock(DataSource)
        service.dataSource = dataSource
        service.dataSource_pva = dataSource
        SignalDataSourceService signalDataSourceService=Mock(SignalDataSourceService)
        signalDataSourceService.getReportConnection(_)>>{
            return dataSource
        }
        service.signalDataSourceService=signalDataSourceService
        CRUDService crudService=Mock(CRUDService)
        service.CRUDService=crudService
        ReportIntegrationService reportIntegrationService=Mock(ReportIntegrationService)
        service.reportIntegrationService=reportIntegrationService
        service.transactionManager = getTransactionManager()
        service.sessionFactory = sessionFactory
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
    void "test saveSignal when signal is not new"(){
        setup:
        ValidatedSignal validatedSignal=new ValidatedSignal(name: "newSignal", products: "test_products",
                endDate: new Date(), assignedTo: user, assignmentType: 'USER', modifiedBy: user.username,
                priority: priority, disposition: defaultSignalDisposition, createdBy: user.username,
                startDate: new Date(), id:1, genericComment: "Test notes", workflowGroup: wfGroup)
        CRUDService crudService=Mock(CRUDService)
        crudService.save(validatedSignal)>>{
            validatedSignal.save(flush:true,failOnError:true)
        }
        service.CRUDService=crudService
        when:
        service.saveSignal(validatedSignal,false)
        then:
        ValidatedSignal.findByName("newSignal")!=null
        ValidatedSignal.findByName("newSignal").signalStatusHistories==null
    }
    void "test saveSignal when signal is new"(){
        setup:
        ValidatedSignal validatedSignal=new ValidatedSignal(name: "newSignal", products: "test_products",
                endDate: new Date(), assignedTo: user, assignmentType: 'USER', modifiedBy: user.username,
                priority: priority, disposition: defaultSignalDisposition, createdBy: user.username,
                startDate: new Date(), id:1, genericComment: "Test notes", workflowGroup: wfGroup)
        CRUDService crudService=Mock(CRUDService)
        crudService.save(validatedSignal)>>{
            validatedSignal.save(flush:true,failOnError:true)
        }
        service.CRUDService=crudService
        service.metaClass.saveHistoryForSignal={ValidatedSignal validatedSignal1->
            signalHistory2.save(flush:true,failOnError:true)
        }
        when:
        service.saveSignal(validatedSignal,true)
        then:
        ValidatedSignal.findByName("newSignal")!=null
        SignalHistory.findByCreatedBy("username")!=null
    }
    @Unroll
    void "test attachToSignald"(){
        setup:
        service.metaClass.addAlertToSignal={ValidatedSignal validatedSignal1,def alert->
            if(alert.getClass()==SingleCaseAlert){
                validatedSignal1.addToSingleCaseAlerts(alert)
                validatedSignal1.save(flush:true,failOnError:true)
            }else if(alert.getClass()==AggregateCaseAlert){
                validatedSignal1.addToAggregateAlerts(alert)
                validatedSignal1.save(flush:true,failOnError:true)
            }else if(alert.getClass()==AdHocAlert){
                validatedSignal1.addToAdhocAlerts(alert)
                validatedSignal1.save(flush:true,failOnError:true)
            }else if(alert.getClass()==EvdasAlert){
                validatedSignal1.addToEvdasAlerts(alert)
                validatedSignal1.save(flush:true,failOnError:true)
            }
        }
        service.metaClass.saveHistoryForSignal={ValidatedSignal validatedSignal1-> }
        CRUDService crudService=Mock(CRUDService)
        crudService.save(validatedSignal,false)>>{
            validatedSignal.save(flush:true,failOnError:true)
        }
        service.CRUDService=crudService
        expect:
        service.attachToSignal(a,b,c,d,e,f,g).activities[0].type.value==result[0]
        service.attachToSignal(a,b,c,d,e,f,g).singleCaseAlerts?.name==result[1]
        service.attachToSignal(a,b,c,d,e,f,g).aggregateAlerts?.name==result[2]
        service.attachToSignal(a,b,c,d,e,f,g).adhocAlerts?.name==result[3]
        service.attachToSignal(a,b,c,d,e,f,g).evdasAlerts?.name==result[4]
        where:
        a                             |  b        |   c               |      d                |    e   |   f   |      g                 |      result
        '[{"signalName":"test_name","signalEvent":"event1"}]' |"product1" |singleCaseAlert    |"Single Case Alert"    |user    |wfGroup|defaultSignalDisposition|[ActivityTypeValue.CaseAssociated,["singleCaseAlert1"],null,null,null]
        '[{"signalName":"test_name","signalEvent":"event1"}]' |"product1" |aggregateCaseAlert |"Aggregate Case Alert" |user    |wfGroup|defaultSignalDisposition|[ActivityTypeValue.PECAssociated,null,["aggregateCaseAlert1"],null,null]
        '[{"signalName":"test_name","signalEvent":"event1"}]' |"product1" |adHocAlert         |"Ad-Hoc Alert"         |user    |wfGroup|defaultSignalDisposition|[ActivityTypeValue.AdhocAlertAssociated,null,null,["adHocAlert1"],null]
        '[{"signalName":"test_name","signalEvent":"event1"}]' |"product1" |evdasAlert         |"EVDAS Alert"          |user    |wfGroup|defaultSignalDisposition|[ActivityTypeValue.CaseAssociated,null,null,null,["evdasAlert1"]]
    }
    void "test saveHistoryForSignal"(){
        when:
        service.saveHistoryForSignal(validatedSignal)
        then:
        SignalHistory.findByCreatedBy("username")!=null
    }
    void "test addAlertToSignal when alert is SingleCaseAlert"(){
        when:
        def result=service.addAlertToSignal(validatedSignal,singleCaseAlert)
        then:
        validatedSignal.singleCaseAlerts[0]==singleCaseAlert
    }
    void "test addAlertToSignal when alert is AggregateCaseAlert"(){
        when:
        def result=service.addAlertToSignal(validatedSignal,aggregateCaseAlert)
        then:
        validatedSignal.aggregateAlerts[0]==aggregateCaseAlert
    }
    void "test addAlertToSignal when alert is AdHocAlert"(){
        when:
        def result=service.addAlertToSignal(validatedSignal,adHocAlert)
        then:
        validatedSignal.adhocAlerts[0] == adHocAlert
    }
    void "test addAlertToSignal when alert is EvdasAlert"(){
        when:
        def result=service.addAlertToSignal(validatedSignal,evdasAlert)
        then:
        validatedSignal.evdasAlerts[0]==evdasAlert
    }
    void "test addAlertToSignal when alert is LiteratureAlert"(){
        when:
        def result=service.addAlertToSignal(validatedSignal,literatureAlert)
        then:
        validatedSignal.literatureAlerts[0]==literatureAlert
    }
    void "test getMeetingDetails when id is found"(){
        when:
        List result=service.getMeetingDetails(ValidatedSignal.list().get(0).id)
        then:
        result.signalName==["test_name"]
        result.title==["meeting1"]
        result.agenda==["agenda1"]
        result.minutes==["120mins"]
        result.lastUpdatedBy==["test_user"]
    }
    void "test getMeetingDetails when id is not found"(){
        when:
        List result=service.getMeetingDetails(1000L)
        then:
        result==[]
    }
    void "test changeAssignedToUser"(){
        setup:
        user.username = 'username'
        user.metaClass.getFullName = { 'Fake Name2' }
        user.save(flush:true)

        when:
        ValidatedSignal result=service.changeAssignedToUser(validatedSignal.id,user)
        then:
        result.assignedTo==user
    }
    void "test changePriority"(){
        when:
        service.changePriority(validatedSignal,priority2,"change needed")
        then:
        validatedSignal.priority==priority2
    }
    @Unroll
    void "test getSignalsFromAlert"(){
        expect:
        Map output=service.getSignalsFromAlert(a,b)
        output.selectedSignal?.signalName==result[0]
        output.selectedSignal?.products==result[1]
        output.selectedSignal?.priorityAndUrgency==result[2]
        output.allSignals.size()==result[3]
        where:
        a                      |          b           |           result
        singleCaseAlert        |"Single Case Alert"   |     [['test_name2','test_name3'],["test_products2","test_products3"],["High","High"],3]
        aggregateCaseAlert     |"Aggregate Case Alert"|     [['test_name2','test_name3'],["test_products2","test_products3"],["High","High"],3]
        null                   |"Single Case Alert"   |     [[],[],[],0]
    }
    void "test getSignalsFromAlert for adhocAlert"(){
        when:
        Map result=service.getSignalsFromAlert(adHocAlert,"Ad-Hoc Alert")
        then:
        result.selectedSignal?.signalName==['test_name2','test_name3']
    }
    void "test getSignalsFromAlertObj when alertType is aggregate"(){
        when:
        List result=service.getSignalsFromAlertObj(aggregateCaseAlert,"Aggregate Case Alert")
        then:
        result==[validatedSignal2]
    }
    void "test getSignalsFromAlertObj when alertType is"(){
        when:
        List result=service.getSignalsFromAlertObj(evdasAlert,"EVDAS Alert")
        then:
        result==[validatedSignal2]
    }
    void "test listActivities when signal contains activities"(){
        when:
        def result=service.listActivities(validatedSignal2.id)
        then:
        result[0]==Activity.list().get(0)
    }
    void "test listActivities when signal doesn't contains activities"(){
        when:
        def result=service.listActivities(validatedSignal.id)
        then:
        result==null
    }
    void "test listActivities when passing null id"(){
        when:
        List result=service.listActivities(null)
        then:
        result==[]
    }
    void "test listTopicActivities when id matches"(){
        when:
        Set result=service.listTopicActivities(Topic.list().get(0).id)
        then:
        result==[Activity.list().get(0)] as Set
    }
    void "test listTopicActivities when id is null"(){
        when:
        def result=service.listTopicActivities(null)
        then:
        result==[]
    }
    void "test getSignalsByAggregateAlerts"(){
        setup:
        service.metaClass.getSignalList={String s->
            return [validatedSignal2]
        }
        when:
        List result=service.getSignalsByAggregateAlerts([aggregateCaseAlert])
        then:
        result[0]==validatedSignal2
    }
    void "test getSignalsByEvdasAlerts"(){
        setup:
        service.metaClass.getSignalList={String s->
            return [validatedSignal2]
        }
        when:
        List result=service.getSignalsByAggregateAlerts([evdasAlert])
        then:
        result[0]==validatedSignal2
    }
    void "test getRelevantSignals on success"(){
        setup:
        service.metaClass.getSignalList={String s->
            return [validatedSignal2,validatedSignal3]
        }
        when:
        List result=service.getRelevantSignals(validatedSignal2)
        then:
        result==[validatedSignal3,validatedSignal3]
    }
    void "test getRelevantSignals when exception occurs"(){
        setup:
        service.metaClass.getSignalList={String s->
            throw new Exception()
        }
        when:
        List result=service.getRelevantSignals(validatedSignal2)
        then:
        result==[]
    }
    void 'test fetchExportDataForDistributionBySourceOverTime'(){
        when:
        Map result=service.fetchExportDataForDistributionBySourceOverTime(signalChartsDTO1)
        then:
        result.data.Category==[["date1"],["date2"]]
        result.title=="Distribution By Source Over Time"
        result.rowNames==["Category"]
        result.columnNames==[["criteria1"], ["criteria2"]]
    }
    void 'test fetchExportDataForDistributionByAgeOverTime'(){
        when:
        Map result=service.fetchExportDataForDistributionByAgeOverTime(signalChartsDTO1)
        then:
        result.data.Category==[["date1"],["date2"]]
        result.title=="Distribution By Age Group Over Time"
        result.rowNames==["Category"]
        result.columnNames==[["criteria1"], ["criteria2"]]
    }
    void 'test fetchExportDataForDistributionByCountryOverTime'(){
        when:
        Map result=service.fetchExportDataForDistributionByCountryOverTime(signalChartsDTO1)
        then:
        result.data.Category==[["date1"],["date2"]]
        result.title=="Distribution By Country Over Time"
        result.rowNames==["Category"]
        result.columnNames==[["criteria1"], ["criteria2"]]
    }
    void 'test fetchExportDataForDistributionByGenderOverTime'(){
        when:
        Map result=service.fetchExportDataForDistributionByGenderOverTime(signalChartsDTO1)
        then:
        result.data.Category==[["date1"],["date2"]]
        result.title=="Distribution By Gender Over Time"
        result.rowNames==["Category"]
        result.columnNames==[["criteria1"], ["criteria2"]]
    }
    void 'test fetchExportDataForDistributionByCaseOutcome'(){
        when:
        Map result=service.fetchExportDataForDistributionByCaseOutcome(signalChartsDTO1)
        then:
        result.data.Category==[["date1"],["date2"]]
        result.title=="Distribution By Case Outcome"
        result.rowNames==["Category"]
        result.columnNames==[["criteria1"], ["criteria2"]]
    }
    void 'test fetchExportDataForDistributionBySeriousnessOverTime'(){
        when:
        Map result=service.fetchExportDataForDistributionBySeriousnessOverTime(signalChartsDTO1)
        then:
        result.data.Category==[["date1"],["date2"],["date2"]]
        result.title=="Distribution By Seriousness Over Time"
        result.rowNames==["Category"]
        result.columnNames==["Death", "Life-threatening", "Others", "Intervention Required", "Hospitalized", "Prolong Hospitlization", "Disability", "Congenital Anomaly", "Medically Significant"]
    }
    void "test filterData"(){
        given:
        List rawData=[[["date1"],["criteria1"]],[["date2"],["criteria2"]],[["date2"],["criteria2"]]]
        when:
        Map result=service.filterData(rawData)
        then:
        result==[["criteria1"]:[["date1"]:null, ["date2"]:0], ["criteria2"]:[["date1"]:0, ["date2"]:null]]
    }
    void "test mapFilteredDataForExport"(){
        given:
        Map filteredData=[["criteria1"]:[["date1"]:null, ["date2"]:0], ["criteria2"]:[["date1"]:0, ["date2"]:null]]
        when:
        List result=service.mapFilteredDataForExport(filteredData)
        then:
        result==[["Category":["date1"], ["criteria1"]:null, ["criteria2"]:0], [Category:["date2"], ["criteria1"]:0, ["criteria2"]:null]]
    }
    void "test mapSeriousnessCountDataForExport"(){
        given:
        List rawData=[["serious",1,10,"others","yes",12,8,3,2,9]]
        when:
        List result=service.mapSeriousnessCountDataForExport(rawData)
        then:
        result==[["Category":"serious", "Death":1, "Life-threatening":10, "Others":"others", "Intervention Required":"yes", "Hospitalized":12, "Prolong Hospitlization":8, "Disability":3, "Congenital Anomaly":2, "Medically Significant":9]]
    }
    void "test fetchDateRangeFromCaseAlerts"(){
        when:
        List result=service.fetchDateRangeFromCaseAlerts([singleCaseAlert])
        then:
        result==["01/02/2020", "29/02/2020"]
    }
    @Unroll
    void "test calculateSeriousness"(){
        expect:
        service.calculateSeriousness(a)==result
        where:
        a            |          result
        "Serious"    |            "Y"
        "Non-Serious"|            "N"
        "Unknown"    |            "U"
        "notMatching"|            "-"
    }
    @Unroll
    void "test calculateListedness"(){
        expect:
        service.calculateListedness(a)==result
        where:
        a              |        result
        "Listed"       |        "N"
        "Unlisted"     |        "Y"
        "Unknown"      |        "U"
        "notMatching"  |        "-"
    }
    void "test fetchProductAndEventFromAggList"(){
        when:
        Map result=service.fetchProductAndEventFromAggList([aggregateCaseAlert])
        then:
        result.productList[0]==[name:"product1", id:1]
        result.eventList[0]==[name:"aggrePt", id:2]
    }
    void "test getAssignedSignalList"(){
        when:
        List result=service.getAssignedSignalList()
        then:
        result==[[productDictionarySelection:"productDictionarySelection1", products:"test_products"],
                 [productDictionarySelection:null, products:"test_products2"], [productDictionarySelection:null,
                                                                                products:"test_products3"]]
    }
    void "test getProductNameList"(){
        given:
        String products='{1:[{"name":"name1"}],2:[{"name":"name2"}]}'
        when:
        List result=service.getProductNameList(products)
        then:
        result==['{1:[{"name":"name1"}]','2:[{"name":"name2"}]}']
    }
    void "test generateSignalSummaryReport"(){
        setup:
        Map params=[:]
        params.signalId= "${ validatedSignal2.id }"
        service.metaClass.combinedAndPrevAggAndEvdasAlertListMap={Long signalId->
            return [combinedList: [aggregateCaseAlert,evdasAlert], prevAggAlertList: [aggregateCaseAlert], prevEvdasAlertList: [evdasAlert]]
        }
        service.metaClass.generateAssessmentDetailsMap={Map para,ValidatedSignal vs->
            return ["characteristic1":[['category':'category1','number':'number1'],['category':'category2','number':'number2']]]
        }
        service.metaClass.createConceptsMap={ValidatedSignal vs->
            return ["concept1":['singleCaseAlerts':'10','aggregateAlerts':'12','evdasAlerts':'1']]
        }
        service.alertService = [productSelectionSignal: { -> "Test Product Aj"}]
        when:
        File result=service.generateSignalSummaryReport(params)
        then:
        result!=null
        result.getClass()==File
    }
    @Unroll
    void  "test generateAssessmentDetailsMap"(){
        setup:
        service.metaClass.generateAssessmentDataMap={SignalChartsDTO signalChartsDTO->
            [["product1"]:["event1"]]
        }
        expect:
        service.generateAssessmentDetailsMap(a,b)==result
        where:
        a                                                       |       b           | result
        [dateRange: 'LAST_3_MONTH',
         productSelection:"product1",eventSelection:"event1"]   |  validatedSignal  |   [["product1"]:["event1"]]
        [dateRange: 'LAST_6_MONTH',
         productSelection:"product1",eventSelection:"event1"]   |  validatedSignal  |   [["product1"]:["event1"]]
        [dateRange: 'LAST_3_YEAR',
         productSelection:"product1",eventSelection:"event1"]   |  validatedSignal  |   [["product1"]:["event1"]]
        [dateRange: 'LAST_5_YEAR',
         productSelection:"product1",eventSelection:"event1"]   |  validatedSignal  |   [["product1"]:["event1"]]
        [dateRange: 'SIGNAL_DATA',
         productSelection:"product1",eventSelection:"event1"]   |  validatedSignal  |   [["product1"]:["event1"]]
        [dateRange: 'LAST_1_YEAR',
         productSelection:"product1",eventSelection:"event1"]   |  validatedSignal  |   [["product1"]:["event1"]]
    }
    @Unroll
    void "test exportSignalActionDetailReport"(){
        setup:
        service.metaClass.getSignalActionDetails={Long id->
            [[signalName:"test_name", actionName : "actionName", actionType : "actionType", assignedTo :
                    user?.fullName, status : "Completed", creationDate: new Date(), dueDate : new Date()+4,
              completionDate: new Date()+3, comment : "Comments"]]
        }
        expect:
        service.exportSignalActionDetailReport(a).getClass()==result
        where:
        a                                            |                      result
        [signalId:"${validatedSignal.id}"]           |                        File
        [:]                                          |                        File
    }
    @Unroll
    void "test generateSignalReports"(){
        when:
        def result=service.generateSignalReports([signalId:validatedSignal.id])
        then:
        result!=null
        result.getClass()==File
    }
    @Unroll
    void "test getTrend"(){
        expect:
        service.getTrend(a,b)==result
        where:
        a             |             b                |          result
        13            |             10               |        "Negative"
        20            |             10               |        "Positive"
    }
    @Unroll
    void "test getEbgmTrend"(){
        expect:
        service.getEbgmTrend(a,b)==result
        where:
        a             |             b                |          result
        10            |             15               |        "Negative"
        20            |             10               |        "Positive"
    }
    @Unroll
    void "test getPrrTrend"(){
        expect:
        service.getPrrTrend(a,b)==result
        where:
        a             |             b                |          result
        10            |             15               |        "Negative"
        20            |             10               |        "Positive"
    }
    void "test getAggregateAndEvdasAlertList"(){
        when:
        List result=service.getAggregateAndEvdasAlertList(validatedSignal2.id)
        then:
        result[0].alertName=="aggregateCaseAlert1"
        result[0].productName=="product1"
        result[0].preferredTerm=="aggrePt"
        result[1].alertName=="evdasAlert1"
        result[1].productName=="Substance1"
        result[1].preferredTerm=="evdasPt"
    }
    void "test getCurrAggAlertList"(){
        when:
        List result=service.getCurrAggAlertList(validatedSignal2.id)
        then:
        result[0].alertName=="aggregateCaseAlert1"
        result[0].productName=="product1"
        result[0].preferredTerm=="aggrePt"
        result[0].productSelection=='{"6":[{"name":"IFOSFAMIDE","id":"-470484647"}]}'
        result[0].priority=="High"
    }
    void "test getPrevAggAlertList"(){
        given:
        List aggCaseAlertList=[[alertName:"aggregateCaseAlert1",id:aggregateCaseAlert.id,productId:1,soc:"SOC",ptCode:2]]
        when:
        List result=service.getPrevAggAlertList(aggCaseAlertList)
        then:
        result==[aggregateCaseAlert2]
    }
    void "test getCurrEvdasAlertList"(){
        when:
        List result=service.getCurrEvdasAlertList(validatedSignal2.id)
        then:
        result[0].alertName=="evdasAlert1"
        result[0].productName=="Substance1"
        result[0].preferredTerm=="evdasPt"
        result[0].productSelection=='{"6":[{"name":"IFOSFAMIDE","id":"-470484647"}]}'
        result[0].priority=="High"
    }
    void "test getPrevEvdasAlertList"(){
        given:
        List evdasAlertList=[[alertName:"evdasAlert1",id:evdasAlert.id,productId:1,soc:"SOC",ptCode:1]]
        when:
        List result=service.getPrevEvdasAlertList(evdasAlertList)
        then:
        result==[evdasAlert2]
    }
    void "Test saveValidatedSignal"() {
        given: "A map with required parameters"
        ValidatedSignal validatedSignal1
        Map<String, String> map = ['productSelection': '{"1":[],"2":[],"3":[{"name":"Test Product d","id":"100022"}],"4":[],"5":[]}', 'eventSelection': '{"1":[],"2":[{"name":"Body temperature conditions (J)","id":"10005908"}],"3":[{"name":"Febrile disorders","id":"10016286"}],"4":[{"name":"Pyrexia","id":"10037660"}],"5":[],"6":[]}', 'name': 'Test Signal', 'topic': '', genericComment: '', 'detectedBy': 'Sahi', 'evaluationMethod': 'Claims data mining', 'signalEvaluationMethod': '', priority: '1', description: 'Description', reasonForEvaluation: '', commentSignalStatus: '', 'assignedToValue': 'User_1', initialDataSource: "Data mining - FAERS database"]

        def mockUserService = Mock(UserService)
        service.userService = mockUserService

        mockUserService.getUser() >> user
        service.metaClass.setDatesForSignal = { ValidatedSignal validatedSignal, Map<String, String> params -> }
        service.metaClass.setNonStringFields = { Map<String, String> params, ValidatedSignal validatedSignal -> }
        service.metaClass.bindTopicCategory = { ValidatedSignal validatedSignal, def signalTypeList -> }
        service.metaClass.bindActionTaken = { ValidatedSignal validatedSignal, def signalTypeList -> }
        service.metaClass.bindEvaluationMethod = { ValidatedSignal validatedSignal, def signalTypeList -> }
        service.metaClass.bindOutcomes = { ValidatedSignal validatedSignal, def signaloutcomeList -> }
        service.metaClass.bindLinkedSignals = { ValidatedSignal validatedSignal, def linkedSignalList -> }

        when:
        validatedSignal1 = service.saveValidatedSignal(map)

        then:
        validatedSignal1.disposition == defaultSignalDisposition
    }
    void "test validateSignal with all required parameters"() {

        given: "A map with required parameters"

        UserService mockUserService = Mock(UserService)
        mockUserService.getUser() >> {
            return (User.get(1))
        }
        service.userService = mockUserService

        Map<String, String> map = ['products': '{"1":[],"2":[],"3":[{"name":"Test Product d","id":"100022"}],"4":[],"5":[]}', 'events': '{"1":[],"2":[{"name":"Body temperature conditions (J)","id":"10005908"}],"3":[{"name":"Febrile disorders","id":"10016286"}],' +
                '"4":[{"name":"Pyrexia","id":"10037660"}],"5":[],"6":[]}', 'name': 'Test Signal', 'topic': '', genericComment: '', 'detectedBy': 'Sahi', 'evaluationMethod': 'Claims data mining', 'signalEvaluationMethod': '', priority: Priority.findByValue('High'),
                                   description: 'Description', reasonForEvaluation: '', commentSignalStatus: '', assignedTo: User.findByUsername('test_user'), dueDate: new Date(), detectedDate: new Date(), initialDataSource: "Data mining - FAERS database",workflowGroup: wfGroup,assignmentType: 'USER',modifiedBy: 'modifiedBy',disposition: defaultDisposition,createdBy: 'createdBy']

        ValidatedSignal validatedSignal1 =
                new ValidatedSignal(map)
        validatedSignal1.save(flush:true)
        Boolean result = false

        when:
        result = service.validateSignal(validatedSignal1)

        then:
        result
    }
    void "test validateSignal without all required parameters"() {

        given: "A map without required parameters"

        UserService mockUserService = Mock(UserService)
        mockUserService.getUser() >> {
            return (User.get(1))
        }
        service.userService = mockUserService

        Map<String, String> map = ['products': '{"1":[],"2":[],"3":[{"name":"Test Product d","id":"100022"}],"4":[],"5":[]}', 'events': '{"1":[],"2":[{"name":"Body temperature conditions (J)","id":"10005908"}],"3":[{"name":"Febrile disorders","id":"10016286"}],"4":[{"name":"Pyrexia","id":"10037660"}],"5":[],"6":[]}', 'name': 'Test Signal', 'topic': '', genericComment: '', 'detectedBy': '', 'evaluationMethod': 'Claims data mining', 'signalEvaluationMethod': '', priority: Priority.findByValue('High'), description: 'Description', reasonForEvaluation: '', commentSignalStatus: '', assignedTo: User.findByUsername('test_user'), dueDate: new Date(), detectedDate: new Date(), initialDataSource: "Data mining - FAERS database"]

        ValidatedSignal validatedSignal = new ValidatedSignal(map)
        Boolean result = false

        when:
        result = service.validateSignal(validatedSignal)

        then:
        !result
    }
    void "test generateSignalHistory when it has StatusHistory"() {
        setup:
        AlertAttributesService mockAlertAttributeService = Mock(AlertAttributesService)
        mockAlertAttributeService.get(_) >> { return ['testA','testB'] }
        service.alertAttributesService = mockAlertAttributeService

        when:
        List<Map> signalStatusHistoryMapList = service.generateSignalHistory(validatedSignal)
        then:
        signalStatusHistoryMapList.size() == 1
        signalStatusHistoryMapList[0].signalStatus == 'signal Status1'
        signalStatusHistoryMapList[0].dispositionUpdated == true
        signalStatusHistoryMapList[0].performedBy == 'Test User'
        signalStatusHistoryMapList[0].statusComment == 'Test Status Comment'
        signalStatusHistoryMapList[0].isAddRow == true
    }
    void "test generateSignalHistory when StatusHistory is empty"() {
        setup:
        ValidatedSignal validatedSignalObj = new ValidatedSignal(
                name: "test_name",
                products: "test_products",
                endDate: new Date(),
                assignedTo: user,
                assignmentType: 'USER',
                modifiedBy: user.username,
                priority: priority,
                disposition: defaultSignalDisposition,
                createdBy: user.username,
                startDate: new Date(),
                genericComment: "Test notes"

        )
        when:
        List<Map> signalStatusHistoryMapList = service.generateSignalHistory(validatedSignalObj)
        then:
        signalStatusHistoryMapList.size() == 0
    }
    void "test generateSignalHistory when ValidatedSignal is null"() {
        setup:
        ValidatedSignal validatedSignalObj = null
        when:
        List<Map> signalStatusHistoryMapList = service.generateSignalHistory(validatedSignalObj)
        then:
        signalStatusHistoryMapList.size() == 0
    }
    void "test saveActivityForSignalHistory"() {
        setup:
        ActivityType activityType = new ActivityType(value: ActivityTypeValue.StatusDate)
        activityType.save()
        UserService userService = Mock(UserService)
        userService.getCurrentUserId() >> 1
        service.userService = userService
        String details = "Details"
        when:
        service.saveActivityForSignalHistory(Constants.SYSTEM_USER,validatedSignal,details)
        then:
        validatedSignal.activities.size() == 1

    }

    void "test checkAccessibility"() {
        setup:
        UserService userServiceMock = Mock(UserService)
        userServiceMock.getUser() >> user
        service.userService = userServiceMock
        when:
        Boolean response = service.checkAccessibility(1)
        then:
        response == false
    }

    void "test revertDisposition"(){
        when:
        def result = service.revertDisposition(1,"testified")
        then:
        result.alertDueDateList.size() == 0
    }

    void "test createActivityForUndoAction"(){
        when:
        def result = service.createActivityForUndoAction(validatedSignal,defaultDisposition,"justification")
        then:
        result
    }

    void "test getValidatedSignalList"(){
        setup:
        Map resultMap = [
                signalId         : validatedSignal.id,
                signalName       : validatedSignal.name,
                productName      : validatedSignal.products,
                eventName        : validatedSignal.events,
                noOfCases        : 1,
                noOfPec          : 2,
                priority         : priority.displayName,
                assignedTo       : user.fullName,
                actions          : 1,
                strategy         : validatedSignal.strategy ? validatedSignal.strategy.name : '-',
                topicCategory    : "topicCategory",
                initialDataSource: validatedSignal.initialDataSource,
                lastSubmitted    : '-',
        ]
        service.metaClass.createValidatedSignalDTO = { List<ValidatedSignal> validatedSignals, String timeZone -> return [resultMap] }
        when:
        Map params = [callingScreen: 'dashboard']
        Map signalMap = service.getValidatedSignalList(searchRequest,params)
        then:
        signalMap.aaData == [resultMap]
    }

    @Unroll
    void "test getDateRangeList"(){
        expect:
        service.getDateRangeList(dateRange,group,params)==result
        where:
        dateRange                                    |      group      |           params                              |          result
        SignalAssessmentDateRangeEnum.CUSTOM         |      wfGroup    | [startDate:"01/09/2020", endDate:"03/09/2020"]|  ["01/09/2020","03/09/2020"]
        SignalAssessmentDateRangeEnum.LAST_3_MONTH   |      wfGroup    | [startDate:"01/09/2020", endDate:"03/09/2020"]|  ["31/05/2020","31/08/2020"]
        SignalAssessmentDateRangeEnum.LAST_6_MONTH   |      wfGroup    | [startDate:"01/09/2020", endDate:"03/09/2020"]|  ["29/02/2020","31/08/2020"]
        SignalAssessmentDateRangeEnum.LAST_3_YEAR    |      wfGroup    | [startDate:"01/09/2020", endDate:"03/09/2020"]|  ["31/12/2016","31/12/2019"]
        SignalAssessmentDateRangeEnum.LAST_5_YEAR    |      wfGroup    | [startDate:"01/09/2020", endDate:"03/09/2020"]|  ["31/12/2014","31/12/2019"]
        SignalAssessmentDateRangeEnum.LAST_1_YEAR    |      wfGroup    | [startDate:"01/09/2020", endDate:"03/09/2020"]|  ["31/12/2018","31/12/2019"]
    }

    void "test spotfireFileExecution"(){
        setup:
        Map params = [:]
        params.dateRange = "LAST_3_MONTH"
        params.signalId = validatedSignal.id
        params.productGroupSelection = '[{"name":"test (13)","id":"13"}]'
        params.eventGroupSelection = '[{"name":"test (13)","id":"13"}]'
        params.productSelection = '{"1":[],"2":[],"3":[{"name":"Test Product AJ Capsule 100 millimole","id":"100004"}],"4":[]}'
        params.eventSelection = '{"1":[],"2":[],"3":[{"name":"Test Product AJ Capsule 100 millimole","id":"100004"}],"4":[]}'
        params.studySelection = '{"1":[],"2":[],"3":[{"name":"Test Product AJ Capsule 100 millimole","id":"100004"}],"4":[]}'
        params.productDictionarySelection = "dictionary"
        service.metaClass.getResultDataForSpotfire={Map map, List dateRangeList, Configuration configurationFaers, List resultData, Long execConfigId->}
        service.metaClass.saveCasesForSpotfire={List dateRangeList, String fileName, User currentUser, Long caseSeriesId, List resultData->}
        service.spotfireService=[prepareFamilyIds:{a-> ['1'] as Set},
                                 createNotificationsSignal:{a,b,c,d,e,f->},
                                 reserveFileName:{a->},
                                 generateReport:{a,b,c,d,e,f,g,h,i,j,k->'{"JobId":"13"}'}]
        service.reportIntegrationService=[postData:{a,b,c->[status:200,result:[status:true,data:1]]}]
        when:
        service.spotfireFileExecution(params,1L,user,wfGroup)
        then:
        noExceptionThrown()
    }

    void "test generateFileName method"() {
        given:
        String signalName = "Signal-UNICODE-1-022OQ-UNICODE signal-unicode-¡ÐÊßãÆæØøÅåß\$^(){}1234567890-Formal Run Purpose ¡ÐÊßãÆæØøÅåß\$^(){}1234567890-ÐÊßãÆæØøÅåß\$^(){}1234567890-Formal Run Purpose¡ÐÊßãÆæØøÅåß\$^-Formal Run Purpose-Formal Run Purpose(){}12377777777-Formal Run12345"
        long execConfigId = 1234L

        String expectedFileName = "Signal-UNICODE-1-022OQ-UNICODE_signal-unicode-__________________1234567890-Formal_Run_Purpose___________________1234567890-_________________1234567890-Formal_Run_Purpose______________-Formal_Run_Purpose-Formal_Run_Purpose____123777777_1234"

        when:
        String fileName = service.generateFileName(signalName, execConfigId)

        then:
        fileName == expectedFileName
    }

    void "test generateFileName onFiftyCharacter method"() {
        given:
        String signalName = "Signal Name July Spotfire File Fifty characte Test"
        long execConfigId = 1234L

        String expectedFileName = "Signal_Name_July_Spotfire_File_Fifty_characte_Test_1234"

        when:
        String fileName = service.generateFileName(signalName, execConfigId)

        then:
        fileName == expectedFileName
    }

    def "test validateRequiredFields method"() {
        setup:
        ValidatedSignal validatedSignal = new ValidatedSignal(
                name: name,
                products: products,
                productGroupSelection: productGroupSelection,
                detectedDate: detectedDate,
                initialDataSource: initialDataSource
        )

        when:
        Boolean result = service.validateRequiredFields(validatedSignal)

        then:
        result == expectedResult

        where:
        name          | products    | productGroupSelection | detectedDate   | initialDataSource | expectedResult
        "SignalName"  | ["Product"] | null                  | new Date()     | "Source"          | true
        "SignalName"  | null        | "GroupSelection"      | new Date()     | "Source"          | true
        null          | ["Product"] | null                  | new Date()     | "Source"          | false
        "SignalName"  | ["Product"] | null                  | null           | "Source"          | false
        "SignalName"  | ["Product"] | null                  | new Date()     | null              | false
        "SignalName"  | null        | null                  | new Date()     | "Source"          | false
        "SignalName"  | []          | null                  | new Date()     | "Source"          | false
        "SignalName"  | null        | ""                    | new Date()     | "Source"          | false
    }

    def "test requiredFieldsPresent method"() {
        setup:
        Map paramsMap = [
                name: name,
                detectedDate: detectedDate,
                initialDataSource: initialDataSource,
                productSelection: productSelection,
                productGroupSelection: productGroupSelection
        ]

        when:
        Boolean result = service.requiredFieldsPresent(paramsMap)

        then:
        result == expectedResult

        where:
        name          | detectedDate | initialDataSource | productSelection | productGroupSelection | expectedResult
        "SignalName"  | new Date()   | "Source"          | ["Product"]      | null                  | true
        "SignalName"  | new Date()   | "Source"          | null             | "GroupSelection"      | true
        null          | new Date()   | "Source"          | ["Product"]      | null                  | false
        "SignalName"  | null         | "Source"          | ["Product"]      | null                  | false
        "SignalName"  | new Date()   | null              | ["Product"]      | null                  | false
        "SignalName"  | new Date()   | "Source"          | []               | null                  | false
        "SignalName"  | new Date()   | "Source"          | null             | ""                    | false
    }
}