package com.rxlogix

import com.rxlogix.config.Disposition
import com.rxlogix.config.EvdasAlert
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ReportExecutionStatus
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.config.AlertType
import com.rxlogix.config.EVDASDateRangeInformation
import com.rxlogix.enums.DateRangeTypeCaseEnum
import com.rxlogix.enums.EvaluateCaseDateEnum
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.user.UserGroupMapping
import com.rxlogix.util.DateUtil
import grails.util.Holders
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationException
import grails.plugin.springsecurity.SpringSecurityUtils
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.config.EvdasConfiguration
import com.rxlogix.signal.SubstanceFrequency
import com.rxlogix.config.ArchivedEvdasAlert
import com.rxlogix.config.ExecutionStatus
import com.rxlogix.enums.GroupType
import com.rxlogix.user.Group
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.User
import com.rxlogix.config.Priority
import net.fortuna.ical4j.model.DateTime
import spock.lang.*

import javax.xml.bind.ValidationException
import java.text.ParseException

class EvdasAlertControllerSpec extends Specification implements ControllerUnitTest<EvdasAlertController> {
    ExecutedEvdasConfiguration executedAlertConfiguration
    EvdasAlert evdasAlert
    Priority priority
    Disposition disposition,defaultSignalDisposition,defaultDisposition,autoRouteDisposition
    User user
    Group group
    EvdasConfiguration evdasConfiguration

    def setup() {
        priority = new Priority([displayName: "mockPriority", value: "mockPriority", display: true, defaultPriority: true, reviewPeriod: 1])
        priority.save(failOnError: true, flush: true)
        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: false, reviewCompleted: true, abbreviation: 'vs')
        disposition.save(flush: true)

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
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        UserGroupMapping userGroupMapping = new UserGroupMapping(user: user, group: wfGroup)
        userGroupMapping.save(flush: true, failOnError: false)
        user.save(flush: true)
        executedAlertConfiguration = new ExecutedEvdasConfiguration(name: "test",
                owner: user, scheduleDateJSON: "{}", nextRunDate: new Date(),
                description: "test", dateCreated: new Date(), lastUpdated: new Date(),
                isPublic: true, isDeleted: true, isEnabled: true,
                productSelection: "aspirin", eventSelection: "['rash']",
                configSelectedTimeZone: "UTC",
                createdBy: user.username, modifiedBy: user.username,
                executionStatus: ReportExecutionStatus.COMPLETED, numOfExecutions: 10, configId: 1L)
        executedAlertConfiguration.save(flush: true, validate: false)

        evdasConfiguration = new EvdasConfiguration(name: "test")
        evdasConfiguration.save(validate: false)

        evdasAlert = new EvdasAlert(executedAlertConfiguration: executedAlertConfiguration,
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
                litCount: 0,
                seriousCount: 0,
                fatalCount: 0,
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
        evdasAlert.save(flush: true, failOnError: true)
    }

    def cleanup() {
    }

    void "test index action "() {

        when:"call index action"
            controller.index()
        then:"It renders review view"
            response.status==200
            view=='/evdasAlert/review'
    }

    void "test showCharts"() {
        setup:
        Holders.config.previous.alerts.count.evdas.charts = 4

        def listExe = ExecutedEvdasConfiguration.metaClass.static.findAllByConfigId = { Long, Map ->
            return [executedAlertConfiguration]
        }
        EvdasAlertService evdasAlertService = Mock(EvdasAlertService)
        evdasAlertService.getDomainObject(_) >> {
            return evdasAlert
        }
        controller.evdasAlertService = evdasAlertService
        evdasAlert.alertConfiguration = evdasConfiguration
        evdasAlert.save(validate: false)
        params.alertId = evdasAlert.id
        when:
        controller.showCharts()
        then:
        response.status == 200
        response.json.fatalCount == [0]
        response.json.litCount == [0]
        response.json.seriousCount == [0]
        response.json.evCount == [1]
    }
    @Ignore
    @Unroll
    void "test create action"(){
        given:
            def mockEvdasAlertService=Mock(EvdasAlertService)
            mockEvdasAlertService.checkProductExistsForEvdas(_,_)>>{
                true
            }
            controller.evdasAlertService=mockEvdasAlertService
        when:"call create "
            params.signalId=signalIdValue
            controller.create()
        then:"It renders create view"
            response.status==200
            view=='/evdasAlert/create'
            model.action==Constants.AlertActions.CREATE
            model.userList[0].fullName=='testFullName'
            model.priorityList[0].displayName=='testDisplayName'
            model.configurationInstance!=null
            model.signalId==signalIdValue
        where:
            signalIdValue<<[null,'1']

    }
    @Ignore
    @Unroll
    void "test save --Success"(){
        given:
            Group group1=new Group(name: 'testGroup',modifiedBy: 'user',createdBy: 'user',groupType:GroupType.WORKFLOW_GROUP)
            Priority priority2=new Priority( value:'testValue2',displayName:'testDisplayName2',reviewPeriod:2)
            EvdasConfiguration evdasConfiguration1=new EvdasConfiguration(name:'newTestName',priority:priority2,createdBy:'user',modifiedBy:'user')
            def mockUserService=Mock(UserService)
            2*mockUserService.getUser() >> {
                User user = new User(username:'test',fullName:'testFullName',groups: [group1])
                return user
            }
            mockUserService.bindSharedWithConfiguration(_,_,_)>>{

            }
            mockUserService.assignGroupOrAssignTo(_,_)>>{
                evdasConfiguration1
            }
            controller.userService=mockUserService
            def mockCRUDService=Mock(CRUDService)
            mockCRUDService.save(_)>>{
                return evdasConfiguration1
            }
            controller.CRUDService=mockCRUDService
        when:"call save method"
            params.id=idValue
            params.name='testName'
            params.priority=priority2
            params.productGroupSelection='{["name":"product group","id":1]}'
            params.eventGroupSelection='{["name":"event group","id":1]}'
            params.createdBy='user'
            params.modifiedBy='user'
            params.sharedWith=null
            params.signalId=null
            controller.save()
        then:"It redirects to view action"
            response.status==302
            response.redirectedUrl=='/evdasAlert/view'

        where:
        idValue<<[null,1]

    }
    @Unroll
    void "test save --Failed"(){
        given:
            Group group1=new Group(name: 'testGroup',modifiedBy: 'user',createdBy: 'user',groupType:GroupType.WORKFLOW_GROUP)
            Priority priority2=new Priority( value:'testValue2',displayName:'testDisplayName2',reviewPeriod:2)
            EvdasConfiguration evdasConfiguration1=new EvdasConfiguration(name:'newTestName',priority:priority2,createdBy:'user',modifiedBy:'user')
            def mockUserService=Mock(UserService)
            2*mockUserService.getUser() >> {
                User user = new User(username:'test',fullName:'testFullName',groups: [group1])
                return user
            }
            mockUserService.bindSharedWithConfiguration(_,_,_)>>{

            }
            mockUserService.assignGroupOrAssignTo(_,_)>>{
                evdasConfiguration1
            }
            controller.userService=mockUserService
            def mockCRUDService=Mock(CRUDService)
            mockCRUDService.save(_)>>{
                throw new ValidationException("validation failed")
            }
            controller.CRUDService=mockCRUDService
        when:
            params.id=1
            params.productGroupSelection='{["name":"product group","id":1]}'
            params.eventGroupSelection='{["name":"event group","id":1]}'
            params.name='testName'
            params.priority=priority2
            params.createdBy='user'
            params.modifiedBy='user'
            params.sharedWith=null
            params.signalId=null
            params.repeatExecution=repeatExecutionValue
            params.action=actionValue
            params.dateRangeEnum=DateRangeEnum.CUSTOM.name()
            params.dateRangeStartAbsolute=dateValue
            params.dateRangeEndAbsolute=dateValue
            controller.save()
        then:
            response.status==200
            view=='/evdasAlert/'+resultValue
            model.userList[0].fullName=='testFullName'
            model.priorityList[0].displayName=='testDisplayName'
            model.configurationInstance!=null
            model.startDateAbsoluteCustomFreq==null

        where:
            repeatExecutionValue | resultValue | actionValue | dateValue
            true                 | 'create'    | 'copy'      | null
            false                | 'create'    | 'copy'      | null
            true                 | 'edit'      | ''          | null
            false                | 'edit'      | ''          | null


    }
    @Ignore
    void "test view,When EvdasConfiguration is not found "(){
        given:
            EvdasConfiguration evdasConfiguration=null
        when:
            controller.view(evdasConfiguration)
        then:
            response.status==302
            response.redirectedUrl=='/evdasAlert/index'

    }
    @Ignore
    void "test view,When EvdasConfiguration found "(){
       given:
            User user1=new User()
            Priority priority1=new Priority()
            EvdasConfiguration evdasConfiguration=new EvdasConfiguration(owner:user1,name:'testName',priority:priority1,createdBy:'user',modifiedBy:'user')
            def mockUserService=Mock(UserService)
            mockUserService.getAssignedToName(_)>>{
            return 'assignedToUserName'
            }
            mockUserService.getCurrentUserId() >>{
                user1.id
            }
            mockUserService.getUser() >> {
                User user = new User(username:'test',fullName:'testFullName')
                return user
            }
            controller.userService=mockUserService
           SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
               return true
           }

        when:
            params.viewSql=false
            controller.view(evdasConfiguration)
        then:
            response.status==200
            view=='/evdasAlert/view'


    }
    @Ignore
    void "test viewExecutedConfig, When ExecutedEvdasConfiguration not found"(){
        given:
            ExecutedEvdasConfiguration executedConfiguration1= null
        when:
            controller.viewExecutedConfig(executedConfiguration1)
        then:
            response.status==302
            response.redirectedUrl=='/evdasAlert/index'

    }
    @Ignore
    void "test changeDisposition , when result found changed disposition successfully"() {
        given:

        when:
            controller.changeDisposition("", disposition, "","","{\"3\":[{\"name\":\"test\",\"id\":\"1\"}]}",false,1)
        then:
            response.status==200
    }

    void "test revertDisposition , when result found disposition reverted successfully"() {
        given:

        when:
        controller.revertDisposition(1,"TEST JUSTIFICATION")
        then:
        response.status==200
    }
    @Ignore
    void "test viewExecutedConfig, When ExecutedEvdasConfiguration found"(){
        given:
            ExecutedEvdasConfiguration executedConfiguration1= new ExecutedEvdasConfiguration()
            User user = new User(username:'test',fullName:'testFullName')
            def mockUserService=Mock(UserService)
            mockUserService.getUser() >> {

                return user
            }
            controller.userService=mockUserService
        when:
            controller.viewExecutedConfig(executedConfiguration1)
        then:
            response.status==200
            view=='/evdasAlert/view'
            model.configurationInstance==executedConfiguration1
            model.currentUser==user
            model.isExecuted==true

    }
    @Ignore
    void "test edit, When ExecutedEvdasConfiguration not found"(){
        given:
            EvdasConfiguration evdasConfiguration= null
        when:
            controller.edit(evdasConfiguration)
        then:
            response.status==302
            response.redirectedUrl=='/evdasAlert/index'

    }
    @Ignore
    Void"test copy, When configuration not found "(){
        given:
        EvdasConfiguration config=null
        when:
        controller.copy(config)
        then:
        response.status==302
        response.redirectedUrl=='/evdasAlert/index'
    }
    @Ignore
    @Unroll
    Void"test copy, When configuration found "(){
        given:
            Priority priority=new Priority()
            User user=new User()
            EVDASDateRangeInformation evdasDateRangeInformation=new EVDASDateRangeInformation(dateRangeStartAbsolute:new Date(2020,06,03),dateRangeEndAbsolute:new Date(2020,06,07),dateRangeEnum:dateRangeType)
            EvdasConfiguration config=new EvdasConfiguration(owner:user,name:'testName',priority:priority,createdBy:'user',modifiedBy:'user',dateRangeInformation:evdasDateRangeInformation)

        when:
            controller.copy(config)
        then:
            response.status==200
            view=='/evdasAlert/create'
            model.action==Constants.AlertActions.COPY
            model.userList[0].fullName=='testFullName'
            model.priorityList[0].displayName=='testDisplayName'
            model.configurationInstance==config
            model.startDateAbsoluteCustomFreq==startDateAbsoluteCustomFreqResult
            model.endDateAbsoluteCustomFreq==endDateAbsoluteCustomFreqResult

        where:
        dateRangeType            | startDateAbsoluteCustomFreqResult              | endDateAbsoluteCustomFreqResult
        DateRangeEnum.CUMULATIVE | ''                                             | ''
        DateRangeEnum.CUSTOM     | DateUtil.toDateString1(new Date(2020, 06, 03)) | DateUtil.toDateString1(new Date(2020, 06, 07))
    }
    @Ignore
    Void"test runOnce, When configuration not found "(){
        when:
            params.id=instanceId
            controller.runOnce()
        then:
            response.status==302
            flash.message!=null
            response.redirectedUrl=='/evdasAlert/index'
        where:
            instanceId<<[null,10]
    }

    Void"test runOnce, When configuration found and nextRunDate is not null "(){
        when:
            params.id=2
            controller.runOnce()
        then:
            response.status==302
            flash.warn!=null
            response.redirectedUrl=='/configuration/index'
    }
    @Ignore
    Void"test runOnce, When configuration found and nextRunDate is null -- Success "(){
        given:
            EvdasConfiguration evdasConfiguration=EvdasConfiguration.get(1)
            def mockConfigurationService=Mock(ConfigurationService)
            mockConfigurationService.getNextDate(_)>>{
                return evdasConfiguration.nextRunDate=new Date()

            }
            controller.configurationService=mockConfigurationService
            def mockCRUDService=Mock(CRUDService)
            mockCRUDService.save(_)>>{
                return EvdasConfiguration.get(1)
            }
            controller.CRUDService=mockCRUDService
            def mockUserService=Mock(UserService)
            mockUserService.getUser()>>{
                new User()
            }
            controller.userService=mockUserService

        when:
            params.id=1
            controller.runOnce()
        then:
            response.status==302
            flash.message!=null
            response.redirectedUrl=='/configuration/executionStatus?alertType='+ AlertType.EVDAS_ALERT
    }
    @Ignore
    Void"test delete, When configuration not found "(){
        when:
            EvdasConfiguration evdasConfiguration=null
            controller.delete(evdasConfiguration)
        then:
            response.status==302
            flash.message!=null
            response.redirectedUrl=='/evdasAlert/index'
    }
    



}