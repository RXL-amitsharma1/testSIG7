package com.rxlogix

import com.rxlogix.AdHocAlertController
import com.rxlogix.AlertAttributesService
import com.rxlogix.attachments.Attachment
import com.rxlogix.attachments.AttachmentLink
import com.rxlogix.attachments.AttachmentableService
import com.rxlogix.cache.CacheService
import com.rxlogix.config.*
import com.rxlogix.enums.GroupType
import com.rxlogix.mapping.LmCountry
import com.rxlogix.mapping.LmFormulation
import com.rxlogix.mapping.LmReportType
import com.rxlogix.signal.AdHocAlert
import com.rxlogix.signal.Alert
import com.rxlogix.signal.AttachmentDescription
import com.rxlogix.signal.Justification
import com.rxlogix.signal.UndoableDisposition
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.signal.ViewInstance
import com.rxlogix.user.Group
import com.rxlogix.user.Preference
import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole
import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.testing.web.controllers.ControllerUnitTest
import groovy.json.JsonBuilder
import groovy.transform.SourceURI
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.joda.time.DateTime
import spock.lang.Ignore
import spock.lang.Specification

import java.nio.file.Path
import java.nio.file.Paths

class AdHocAlertControllerSpec extends Specification implements ControllerUnitTest<AdHocAlertController> {

    User userObj
    AdHocAlert adHocAlertObj
    AdHocAlert adHocAlertObj2
    Disposition disposition
    Group wfGroup
    def formulations
    File file

    def setup() {
        LmFormulation dataStore = new LmFormulation()
        formulations = new JSONArray()
        def formulation = new JSONObject()
        formulation.put("name", 'Table')
        formulation.put("id", 1)
        formulations.add(formulation)
        grailsApplication.config.grails.plugin.springsecurity.ldap.search.base = 'ou=users,dc=eng,dc=rxlogix,dc=com'
        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", closed: false,
                validatedConfirmed: true,abbreviation: "C")
        disposition.save(failOnError: true)
        Disposition disposition1 = new Disposition(value: "ValidatedSignal2", displayName: "ValidatedSignal2", closed: false,
                validatedConfirmed: false,abbreviation: "C")
        disposition1.save(failOnError: true)

        wfGroup = new Group(name: "Default1", groupType: GroupType.WORKFLOW_GROUP, createdBy: 'createdBy',
                modifiedBy: 'modifiedBy',
                defaultQualiDisposition: disposition,
                defaultQuantDisposition: disposition,
                defaultAdhocDisposition: disposition,
                defaultEvdasDisposition: disposition,
                defaultLitDisposition: disposition,
                defaultSignalDisposition: disposition)
        wfGroup.save(flush:true,failOnError:true)

        userObj = new User()
        userObj.username = 'username'
        userObj.createdBy = 'createdBy'
        userObj.modifiedBy = 'modifiedBy'
        userObj.preference.createdBy = "createdBy"
        userObj.preference.modifiedBy = "modifiedBy"
        userObj.preference.locale = new Locale("en")
        userObj.preference.isEmailEnabled = false
        userObj.metaClass.getFullName = { "Fake Namer" }
        userObj.metaClass.getEmail = { 'fake.email@fake.com' }
        userObj.groups = [wfGroup]
        userObj.save(validate: false)

        User userObj2 = new User()
        userObj2.username = 'username2'
        userObj2.createdBy = 'createdBy2'
        userObj2.modifiedBy = 'modifiedBy2'
        userObj2.preference.createdBy = "createdBy2"
        userObj2.preference.modifiedBy = "modifiedBy2"
        userObj2.preference.locale = new Locale("en")
        userObj2.preference.isEmailEnabled = false
        userObj2.metaClass.getFullName = { "Fake Namer" }
        userObj2.metaClass.getEmail = { 'fake.email@fake.com' }
        userObj2.groups = [wfGroup]
        userObj2.save(validate: false)
        
        ActionConfiguration actionConfiguration1=new ActionConfiguration(isEmailEnabled: true,value: "actionConfiguration1")
        actionConfiguration1.save(validate:false)
        Role role = new Role(authority: "ROLE_SHARE_GROUP", createdBy: 'createdBy', modifiedBy: 'modifiedBy').save(flush: true)
        UserRole.create(userObj, role, true)
        UserRole.create(userObj2, role, true)
        Priority priority=new Priority(value:"High")
        priority.save(validate:false)
        
        adHocAlertObj = new AdHocAlert(
                productSelection: "something",
                detectedDate: new DateTime(2015,12, 15, 0, 0, 0).toDate(),
                detectedBy: "Company",
                initialDataSource: "test",
                name: "Test Name2",
                productGroupSelection: '{["name":"product group","id":1]}',
                eventGroupSelection: '{["name":"event group","id":1]}',
                alertVersion: 0,
                priority: priority,
                topic:"rash",
                assignedTo: userObj,
                disposition: disposition
        )
        adHocAlertObj.save(failOnError: true)
        adHocAlertObj2 = new AdHocAlert(
                productSelection: "something",
                detectedDate: new DateTime(2015,12, 15, 0, 0, 0).toDate(),
                detectedBy: "Company",
                initialDataSource: "test",
                productGroupSelection: '{["name":"product group","id":1]}',
                eventGroupSelection: '{["name":"event group","id":1]}',
                name: "Test Name3",
                alertVersion: 0,
                priority: new Priority(value:"High"),
                topic:"rash",
                disposition: disposition,
                assignedTo: userObj
        )
        adHocAlertObj2.save(failOnError: true)
        def obj = ["caseNumber" : "16US000684",
                   "caseVersion": "1",
                   "alertId"    : "1",
                   "event"      : "1.Rash",
                   "currentUser": "1"]
        def obj1 = new JsonBuilder(obj)
        def obj0 = ["caseNumber" : "16US000684",
                    "caseVersion": "1",
                    "alertId"    : "1",
                    "event"      : "1.Rash",
                    "currentUser": "1"]
        def obj2 = new JsonBuilder(obj0)
        controller.params.alertDetails = [obj1, obj2].toString()
      
        ActivityType activityTypeObj = new ActivityType(value: ActivityTypeValue.DispositionChange)
        activityTypeObj.save(failOnError: true)
        AttachmentLink attachmentLink1 = new AttachmentLink(referenceClass: "test class", referenceId: 1L)
        attachmentLink1.save(flush: true, failOnError: true)
        Attachment attachment1 = new Attachment(name: "attachment1", ext: "txt")
        attachment1.save(validate: false)
        attachmentLink1.addToAttachments(attachment1)

        AttachmentDescription attachmentDescription1 = new AttachmentDescription(attachment: attachment1, description: "test" +
                "attachment", createdBy: "username", dateCreated: new Date(), id: 1L)
        attachmentDescription1.save(flush: true, failOnError: true)

        Alert alert1 = new Alert(assignedTo: userObj, priority: new Priority(displayName: "priority1"))
        alert1.save(flush: true, failOnError: true)

        ActivityType activityType1 = new ActivityType(value: "PriorityChange")
        activityType1.save(validate: false)
        ActivityType activityType2 = new ActivityType(value: 'AssignedToChange')
        activityType2.save(validate: false)
        ActivityType activityType3 = new ActivityType(value: 'AttachmentRemoved')
        activityType3.save(validate: false)

        ExecutedConfiguration executedConfiguration1 = new ExecutedConfiguration(name: "ExecutedConfiguration")
        executedConfiguration1.save(validate: false)
        ExecutedConfiguration executedConfiguration2 = new ExecutedConfiguration(name: "ExecutedConfiguration", isPublic: true)
        executedConfiguration2.save(validate: false)

        SignalStrategy signalStrategy1 = new SignalStrategy(name: "signalStrategy1")
        signalStrategy1.save(validate: false)

        ValidatedSignal validatedSignal1 = new ValidatedSignal(name: "validatedSgnal1", adhocAlerts: adHocAlertObj)
        validatedSignal1.save(validate: false)
        ValidatedSignal validatedSignal2 = new ValidatedSignal(name: "validatedSgnal2")
        validatedSignal2.save(validate: false)

        ValidatedSignalService validatedSignalService = Mock(ValidatedSignalService)
        validatedSignalService.fetchSignalsNotInAlertObj("", 1, 10) >> {
            [[name: "validatedSgnal1", id: "1"], [id: "validatedSgnal2", id: "2"]]
        }
        controller.validatedSignalService = validatedSignalService
        SpringSecurityService springSecurityService = Mock(SpringSecurityService)
        springSecurityService.isLoggedIn() >> {
            return true
        }

        springSecurityService.principal >> {
            return userObj
        }

        controller.userService.springSecurityService = springSecurityService
        CacheService cacheService = Mock(CacheService)
        cacheService.getPreferenceByUserId(_) >> {
            return userObj.preference
        }

        cacheService.getUserByUserName(_) >> {
            return userObj
        }
        controller.cacheService=cacheService
        controller.userService.cacheService = cacheService
        controller.workflowRuleService.cacheService = cacheService
        ProductBasedSecurityService productBasedSecurityService = Mock(ProductBasedSecurityService)
        productBasedSecurityService.allAllowedProductIdsForUser(_) >> {
            ["product1", "product2"]
        }
        controller.productBasedSecurityService = productBasedSecurityService
        controller.adHocAlertService.userService.springSecurityService = springSecurityService
        EmailNotificationService emailNotificationService = Mock(EmailNotificationService)
        emailNotificationService.emailNotificationWithBulkUpdate(_, _) >> {
            return false
        }
        controller.emailNotificationService = emailNotificationService
        @SourceURI
        URI sourceUri
        Path scriptLocation = Paths.get(sourceUri)
        String directory = scriptLocation.toString().replace("AdHocAlertControllerSpec.groovy", "testingFiles/Details.html")
        file = new File(directory)
    }

    void "test view action"() {
        setup:
        def adHocAlert = new AdHocAlert(id: 1L, productSelection: "something",detectedDate: new DateTime(2015, 12, 15, 0, 0, 0).toDate(),
                name: "Test Name", detectedBy: "Company", topic: "rash", initialDataSource: "Test", alertVersion: 0,
                priority: new Priority(value: "High"), formulations: formulations.toString())
        adHocAlert.save(validate: false)
        controller.params.id = adHocAlert.id
        when:
        controller.view()
        then:
        response.status == 200
        view=="/adHocAlert/view"
        model.isPublic==true
        model.alertInstance.getClass()==AdHocAlert
        model.isExecuted==false
    }
    void "test view action when AdHocAlert not found"() {
        given:
        params.id=10
        when:
        controller.view()
        then:
        response.status == 302
        response.redirectedUrl=="/adHocAlert/index"
        flash.message=='default.not.found.message'
    }
    void "test create action"(){
        setup:
        AlertAttributesService alertAttributesService=Mock(AlertAttributesService)
        controller.alertAttributesService=alertAttributesService
        when:
        controller.create()
        then:
        response.status==200
        view=="/adHocAlert/create"
        model.alertInstance.getClass()==AdHocAlert
        model.isAdhocAlert==true
    }
    @Ignore
    void "test index action"(){
        when:
        controller.index()
        then:
        view=="/adHocAlert/index"
        model.isProductSecurity==true
        model.allowedProductsAsSafetyLead==["product1","product2"]
    }
    void "test copy action on successful copy"(){
        given:
        AdHocAlert adHocAlert=AdHocAlert.get(1)
        when:
        controller.copy(adHocAlert)
        then:
        response.status==302
        response.redirectedUrl=="/adHocAlert/view/4"
        flash.message=="app.copy.success"
    }
    void "test copy action when AdHocAlert not found"(){
        when:
        controller.copy(null)
        then:
        response.status==302
        response.redirectedUrl=="/adHocAlert/index"
    }
    @Ignore
    void "test delete action when user has permission"(){
        setup:
        Configuration configuration=new Configuration(owner: userObj,name: "configuration1",productSelection: "product",
        createdBy: "username",modifiedBy: "username",assignedTo: userObj,priority: new Priority(displayName: "priority1"))
        configuration.save(validate:false)
        userObj.metaClass.isAdmin={true}
        when:
        controller.delete(configuration)
        then:
        response.status==302
        response.redirectedUrl=="/adHocAlert/index"
        flash.message=="app.configuration.delete.success"
    }
    void "test delete action when user don't have access"(){
        setup:
        Configuration configuration=new Configuration(name: "configuration1", createdBy: "username",
                modifiedBy: "username",assignedTo: userObj,priority: new Priority(displayName: "priority1"))
        configuration.save(validate:false)
        userObj.metaClass.isAdmin={false}
        when:
        controller.delete(configuration)
        then:
        response.status==302
        response.redirectedUrl=="/adHocAlert/index"
        flash.warn=="app.configuration.delete.fail"
    }
    void "test edit action when AdHocAlert found"(){
        setup:
        params.id=1L
        AlertAttributesService alertAttributesService=Mock(AlertAttributesService)
        alertAttributesService.addMissingProperties(_,_)>>{
            return true
        }
        controller.alertAttributesService=alertAttributesService
        when:
        controller.edit()
        then:
        response.status==200
        view=="/adHocAlert/edit"
        model.alertInstance.getClass()==AdHocAlert
        model.isAdhocAlert==true
    }
    void "test edit action when AdHocAlert is not found"(){
        setup:
        params.id=10L
        AlertAttributesService alertAttributesService=Mock(AlertAttributesService)
        alertAttributesService.addMissingProperties(_,_)>>{
            return true
        }
        controller.alertAttributesService=alertAttributesService
        when:
        controller.edit()
        then:
        response.status==302
        response.redirectedUrl=="/adHocAlert/index"
        flash.message =='default.not.found.message'
    }
    void "test the save action when request content type is form " () {
        setup:
        params.detectedBy="Company"
        params.sharedWith="UserGroup_1"
        params.initialDataSource="test"
        params.productGroupSelection='{["name":"product group","id":1]}'
        params.eventGroupSelection='{["name":"event group","id":1]}'
        params.name= "TestAdHocAlert"
        params.alertVersion= 0
        params.priority=new Priority(value: "High")
        params.topic="rash"
        params.disposition=disposition
        params.detectedDate = '12-MAR-2015'
        params.assignedToValue="UserGroup_1"
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }
        AlertAttributesService alertAttributesService = new AlertAttributesService()
        alertAttributesService.slurp()
        controller.alertAttributesService = alertAttributesService
        when:
        request.contentType=FORM_CONTENT_TYPE
        controller.save()
        then:
        response.status == 302
        response.redirectedUrl=="/adHocAlert/view/4"
        flash.message=='default.created.message'
    }
    void "test the save action when productSelection and productGroupSelection both given " () {
        setup:
        params.productSelection= '{"3":[{"name":"product1"}]}'
        params.detectedBy="Company"
        params.sharedWith="UserGroup_1"
        params.initialDataSource="test"
        params.productGroupSelection='{["name":"product group","id":1]}'
        params.eventGroupSelection='{["name":"event group","id":1]}'
        params.name= "TestAdHocAlert"
        params.alertVersion= 0
        params.priority=new Priority(value: "High")
        params.topic="rash"
        params.disposition=disposition
        params.detectedDate = '12-MAR-2015'
        params.assignedToValue="UserGroup_1"
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }
        AlertAttributesService alertAttributesService = new AlertAttributesService()
        alertAttributesService.slurp()
        controller.alertAttributesService = alertAttributesService
        when:
        controller.save()
        then:
        response.status == 200
        view=="/adHocAlert/create"
    }
    void "test the save action when request content type is all" () {
        setup:
        params.detectedBy="Company"
        params.sharedWith="UserGroup_1"
        params.productGroupSelection='{["name":"product group","id":1]}'
        params.eventGroupSelection='{["name":"event group","id":1]}'
        params.initialDataSource="test"
        params.name= "TestAdHocAlert"
        params.alertVersion= 0
        params.priority=new Priority(value: "High")
        params.topic="rash"
        params.disposition=disposition
        params.detectedDate = '12-MAR-2015'
        params.assignedToValue="UserGroup_1"
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }
        AlertAttributesService alertAttributesService = new AlertAttributesService()
        alertAttributesService.slurp()
        controller.alertAttributesService = alertAttributesService
        when:
        request.contentType=ALL_CONTENT_TYPE
        controller.save()
        then:
        response.status == 201
        flash.message==null
    }
    void "test the save action when validation fails" () {
        setup:
        params.productSelection= '{"3":[{"name":"product1"}]}'
        params.detectedBy="Company"
        params.initialDataSource="test"
        params.name= "TestAdHocAlert"
        params.productGroupSelection='{["name":"product group","id":1]}'
        params.eventGroupSelection='{["name":"event group","id":1]}'
        params.alertVersion= 0
        params.priority=new Priority(value: "High")
        params.topic="rash"
        params.disposition=disposition
        params.detectedDate = '12-MAR-2015'
        params.sharedWith="UserGroup_1"
        params.assignedToValue=" "
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }
        AlertAttributesService alertAttributesService = new AlertAttributesService()
        alertAttributesService.slurp()
        controller.alertAttributesService = alertAttributesService
        when:
        request.contentType=ALL_CONTENT_TYPE
        controller.save()
        then:
        response.status == 200
        view=="/adHocAlert/create"
        model.checked=="product"
    }
    void "test the save action when exception occurs" () {
        setup:
        params.productSelection= '{"3":[{"name":"product1"}]}'
        params.detectedBy="Company"
        params.initialDataSource="test"
        params.name= "TestAdHocAlert"
        params.productGroupSelection='{["name":"product group","id":1]}'
        params.eventGroupSelection='{["name":"event group","id":1]}'
        params.alertVersion= 0
        params.priority=new Priority(value: "High")
        params.topic="rash"
        params.disposition=disposition
        params.detectedDate = '12/MAR/2015'
        params.sharedWith="UserGroup_1"
        params.assignedToValue=" "
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }
        AlertAttributesService alertAttributesService = new AlertAttributesService()
        alertAttributesService.slurp()
        controller.alertAttributesService = alertAttributesService
        when:
        request.contentType=ALL_CONTENT_TYPE
        controller.save()
        then:
        response.status == 200
        view=="/adHocAlert/create"
        model.checked=="product"
    }
    void "test action update action when request content type is form"(){
        setup:
        params.detectedBy="Individual"
        params.sharedWith="UserGroup_1"
        params.detectedDate = '12-MAR-2015'
        params.productGroupSelection='{["name":"product group","id":1]}'
        params.eventGroupSelection='{["name":"event group","id":1]}'
        params.id=1L
        params.assignedToValue="UserGroup_1"
        AlertAttributesService alertAttributesService = new AlertAttributesService()
        alertAttributesService.slurp()
        controller.alertAttributesService = alertAttributesService
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }
        when:
        request.contentType=FORM_CONTENT_TYPE
        controller.update()
        then:
        response.status==302
        response.redirectedUrl=="/adHocAlert/view/1"
        flash.message=='default.updated.message'
    }
    void "test action update action when request content type is all"(){
        setup:
        params.detectedBy="Individual"
        params.sharedWith="UserGroup_1"
        params.detectedDate = '12-MAR-2015'
        params.productGroupSelection='{["name":"product group","id":1]}'
        params.eventGroupSelection='{["name":"event group","id":1]}'
        params.id=1L
        params.assignedToValue="UserGroup_1"
        AlertAttributesService alertAttributesService = new AlertAttributesService()
        alertAttributesService.slurp()
        controller.alertAttributesService = alertAttributesService
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }
        when:
        request.contentType=ALL_CONTENT_TYPE
        controller.update()
        then:
        response.status==200
    }
    void "test action update action when exception occurs"(){
        setup:
        params.detectedBy="Individual"
        params.sharedWith="UserGroup_1"
        params.detectedDate = '12/MAR/2015'
        params.productGroupSelection='{["name":"product group","id":1]}'
        params.eventGroupSelection='{["name":"event group","id":1]}'
        params.id=1L
        params.assignedToValue="UserGroup_1"
        AlertAttributesService alertAttributesService = new AlertAttributesService()
        alertAttributesService.slurp()
        controller.alertAttributesService = alertAttributesService
        SpringSecurityUtils.metaClass.static.ifAnyGranted = { String role ->
            return true
        }
        when:
        request.contentType=ALL_CONTENT_TYPE
        controller.update()
        then:
        response.status==200
        view=="/adHocAlert/edit"
        model.alertInstance.getClass()==AdHocAlert
    }
    void "test review action"(){
        when:
        controller.review()
        then:
        view=="/adHocAlert/index"
    }
    void "test executionStatus action"(){
        when:
        controller.executionStatus()
        then:
        response.status==200
        view=="/adHocAlert/executionStatus"
        model.related=="executionStatusPage"
        model.isAdmin==true
    }
    void "test listAllResults action"(){
        when:
        controller.listAllResults()
        then:
        response.status==200
        view=="/adHocAlert/executionStatus"
        model.related=="listAllResultsPage"
        model.isAdmin==true
    }
    void "test getPublicForExecutedConfig action when ExecutedConfiguration is not public"(){
        setup:
        params.id=1L
        when:
        String result=controller.getPublicForExecutedConfig()
        then:
        result=="app.label.private"
    }
    void "test getPublicForExecutedConfig action when ExecutedConfiguration is public"(){
        setup:
        params.id=2L
        when:
        String result=controller.getPublicForExecutedConfig()
        then:
        result=="app.label.public"
    }
    void "test notFound when request type is form"(){
        setup:
        request.contentType=FORM_CONTENT_TYPE
        when:
        controller.notFound()
        then:
        response.status==302
        response.redirectedUrl=="/adHocAlert/index"
        flash.message=='default.not.found.message'
    }
    void "test notFound when request type is all"(){
        setup:
        request.contentType=ALL_CONTENT_TYPE
        when:
        controller.notFound()
        then:
        response.status==404
    }
    void "test list action"(){
        setup:
        AdHocAlertService adHocAlertService=Mock(AdHocAlertService)
        adHocAlertService.getAllowedAdHocAlerts(_,_,_,_)>>{
            return [adHocAlertObj]
        }
        controller.adHocAlertService=adHocAlertService
        params.dashboardFilter = 'total'
        when:
        controller.list(false)
        then:
        response.status==200
        JSON.parse(response.text).filters.value==["Validated Signal"]
        JSON.parse(response.text).filters.closed==[false]
    }
    void "test createAdhocList action on success"(){
        setup:
        AdHocAlertService adHocAlertService=Mock(AdHocAlertService)
        adHocAlertService.getAllowedAdHocAlerts(_,_,_,_)>>{
            return [adHocAlertObj]
        }
        controller.adHocAlertService=adHocAlertService
        params.dashboardFilter = 'total'
        when:
        List result=controller.createAdhocList([],params)
        then:
        result.get(0).id==1
        result.get(0).assignedTo=="username"
        result.get(0).disposition=="Validated Signal"
    }
    void "test createAdhocList action when exception occurs"(){
        setup:
        AdHocAlertService adHocAlertService=Mock(AdHocAlertService)
        adHocAlertService.getAllowedAdHocAlerts(_,_,_,_)>>{
            return []
        }
        controller.adHocAlertService=adHocAlertService
        params.dashboardFilter = 'total'
        when:
        List result=controller.createAdhocList([],params)
        then:
        response.status==200
        result.size()==0
    }
    void "test fetchDispositionSet action when filter is total"(){
        setup:
        params.dashboardFilter = 'total'
        when:
        Set result=controller.fetchDispositionSet(true,[])
        then:
        response.status==200
        result.closed==[false]
        result.value==["Validated Signal"]
    }
    void "test fetchDispositionSet action when filter is underReview"(){
        setup:
        params.dashboardFilter = 'underReview'
        when:
        Set result=controller.fetchDispositionSet(true,[])
        then:
        response.status==200
        result.closed==[true]
        result.value==["Validated Signal"]
    }
    void "test fetchDispositionSet action when filter is not given"(){
        when:
        Set result=controller.fetchDispositionSet(true,[])
        then:
        response.status==200
        result.closed==[true]
        result.value==["Validated Signal"]
    }
    void "test getFiltersFromParamsAdhoc action when dashboard filter is total"(){
        setup:
        params.dashboardFilter = 'total'
        when:
        List result=controller.getFiltersFromParamsAdhoc(false,params)
        then:
        response.status==200
        result[0]=="Validated Signal"
    }
    void "test getFiltersFromParamsAdhoc action when dashboard filter is underReview"(){
        setup:
        params.dashboardFilter = 'underReview'
        when:
        List result=controller.getFiltersFromParamsAdhoc(false,params)
        then:
        response.status==200
        result[0]=="ValidatedSignal2"
    }
    void "test getFiltersFromParamsAdhoc action when filters is empty"(){
        setup:
        params.filters = ''
        when:
        List result=controller.getFiltersFromParamsAdhoc(false,params)
        then:
        response.status==200
        result==["Validated Signal", "ValidatedSignal2"]
    }
    void "test action changePriorityOfAlert on success"(){
        setup:
        String selectedRows='[{"alert.id":1},{"alert.id":2}]'
        Priority priority=new Priority(displayName: "priority")
        priority.save(validate:false)
        String justification="Need changed"
        when:
        controller.changePriorityOfAlert(selectedRows,priority,justification)
        then:
        response.status==200
        JSON.parse(response.text).code==200
        JSON.parse(response.text).status==true
    }
    void "test action changePriorityOfAlert when exception occurs"(){
        setup:
        String selectedRows='[["alert.id":1],["alert.id":2]]'
        Priority priority=new Priority(displayName: "priority")
        priority.save(validate:false)
        String justification="Need changed"
        when:
        controller.changePriorityOfAlert(selectedRows,priority,justification)
        then:
        response.status==200
        JSON.parse(response.text).code==200
        JSON.parse(response.text).status==false
        JSON.parse(response.text).message=="app.label.priority.change.error"
    }
    void "test changeDisposition action on success"(){
        setup:
        String selectedRows='[{"alert.id":1},{"alert.id":2}]'
        Disposition targetDisposition=Disposition.get(2)
        String justification="change needed"
        String validatedSignalName="ValidatedSignal2"
        String productJson="product1"
        AdHocAlertService adHocAlertService=Mock(AdHocAlertService)
        adHocAlertService.changeDisposition(selectedRows,targetDisposition,justification,validatedSignalName,productJson,1,true)>>{
            return true
        }
        controller.adHocAlertService=adHocAlertService
        when:
        controller.changeDisposition(selectedRows,targetDisposition,justification,validatedSignalName,productJson,1)
        then:
        JSON.parse(response.text).code==200
        JSON.parse(response.text).status==true
        JSON.parse(response.text).data.attachedSignalData==true
    }
    void "test changeDisposition action when exception occurs"(){
        setup:
        String selectedRows='[["alert.id":1],["alert.id":2]]'
        Disposition targetDisposition=Disposition.get(2)
        String justification="change needed"
        String validatedSignalName="ValidatedSignal2"
        String productJson="product1"
        when:
        controller.changeDisposition(selectedRows,targetDisposition,justification,validatedSignalName,productJson,1)
        then:
        JSON.parse(response.text).code==200
        JSON.parse(response.text).status==false
        JSON.parse(response.text).data==null
        JSON.parse(response.text).message=="app.label.disposition.change.error"
    }
    void "test revertDisposition action on success"(){
        setup:
        String selectedRows='[{"alert.id":1},{"alert.id":2}]'
        Disposition targetDisposition=Disposition.get(2)
        String justification="change needed"
        String validatedSignalName="ValidatedSignal2"
        String productJson="product1"
        AdHocAlertService adHocAlertService=Mock(AdHocAlertService)
        LmFormulation lmFormulation = Mock(LmFormulation)
        UndoableDisposition undoableDisposition=Mock(UndoableDisposition)
        adHocAlertService.revertDisposition(1,justification)>>{
            return [attachedSignalData: null]
        }
        controller.adHocAlertService=adHocAlertService
        when:
        controller.revertDisposition(1,justification)
        then:
        JSON.parse(response.text).code==200
        JSON.parse(response.text).status==true
        JSON.parse(response.text).data.attachedSignalData==null
    }
    void "test revertDisposition action when exception occurs"(){
        setup:
        String selectedRows='[["alert.id":1],["alert.id":2]]'
        Disposition targetDisposition=Disposition.get(2)
        String justification="change needed"
        String validatedSignalName="ValidatedSignal2"
        String productJson="product1"
        when:
        controller.revertDisposition(1,"")
        then:
        JSON.parse(response.text).code==200
        JSON.parse(response.text).status==false
        JSON.parse(response.text).data==null
        JSON.parse(response.text).message=="app.label.disposition.change.error"
    }
    void "test changeAssignedToGroup action on success"(){
        setup:
        String selectedId="[1]"
        String assignedToValue="User_2"
        ActivityService activityService=Mock(ActivityService)
        activityService.create(*_)>>{
            return true
        }
        controller.activityService=activityService
        when:
        controller.changeAssignedToGroup(selectedId,assignedToValue)
        then:
        response.status==200
        JSON.parse(response.text).message=="app.assignedTo.changed.success"
        JSON.parse(response.text).status==true
    }
    void "test changeAssignedToGroup action when exception occurs"(){
        setup:
        String selectedId="[2]"
        String assignedToValue="User_2"
        when:
        controller.changeAssignedToGroup(selectedId,assignedToValue)
        then:
        response.status==200
        JSON.parse(response.text).message=="app.assignedTo.changed.fail"
        JSON.parse(response.text).status==false
    }

    void "test exportReport action"(){
        setup:
        params.ids="1"
        AlertService alertService=Mock(AlertService)
        alertService.filterByProductSecurity>>{
            [adHocAlertObj]
        }
        controller.alertService=alertService
        ValidatedSignalService validatedSignalService=Mock(ValidatedSignalService)
        validatedSignalService.getSignalsFromAlertObj(_,_)>>{
            return [ValidatedSignal.get(1)]
        }
        controller.validatedSignalService=validatedSignalService
        TopicService topicService=Mock(TopicService)
        topicService.getTopicsFromAlertObj(_,_)>>{
            return []
        }
        controller.topicService=topicService
        DynamicReportService dynamicReportService=Mock(DynamicReportService)
        dynamicReportService.createAdHocAlertsDetailReport(_,_,_)>>{
            return file
        }
        controller.dynamicReportService=dynamicReportService
        when:
        controller.exportReport()
        then:
        response.status==200
        response.text!=null
    }
    void "test exportDetailReport action "(){
        setup:
        params.ids="1"
        AdHocAlertService adHocAlertService=Mock(AdHocAlertService)
        adHocAlertService.listWithFilter(_)>>{
            return []
        }
        adHocAlertService.list()>>{
            [adHocAlertObj]
        }
        controller.adHocAlertService=adHocAlertService
        DynamicReportService dynamicReportService=Mock(DynamicReportService)
        dynamicReportService.createAdHocAlertsDetailReport(_,_,_)>>{
            return file
        }
        controller.dynamicReportService=dynamicReportService
        when:
        controller.exportDetailReport()
        then:
        response.status==200
        response.text!=null
    }
    void "test renderReportOutputType action"(){
        setup:
        DynamicReportService dynamicReportService=Mock(DynamicReportService)
        controller.dynamicReportService=dynamicReportService
        when:
        controller.renderReportOutputType(file)
        then:
        response.status==200
        response.text!=null
    }
    void "test toggleFlag action when alert is found"(){
        setup:
        params.id=1L
        when:
        controller.toggleFlag()
        then:
        response.status==200
        JSON.parse(response.text).success=="ok"
        JSON.parse(response.text).flagged==true
    }
    void "test toggleFlag action when params.id is not given"(){
        when:
        controller.toggleFlag()
        then:
        response.status==404
    }
    void "test importFile action"(){
        setup:
        params.jsonContent="some JSON text"
        AdHocAlertService adHocAlertService=Mock(AdHocAlertService)
        adHocAlertService.importAlert(params.jsonContent)>>{
            return params.jsonContent
        }
        controller.adHocAlertService=adHocAlertService
        when:
        controller.importFile()
        then:
        response.status==302
        response.redirectedUrl=="/adHocAlert/index"
    }
    void "test deleteAttachment action"(){
        setup:
        params.attachmentId="1"
        params.id=1L
        AttachmentableService attachmentableService=Mock(AttachmentableService)
        attachmentableService.removeAttachment(Long.parseLong(params.attachmentId))>>{
            Attachment.get(1L).delete()
            return true
        }
        controller.attachmentableService=attachmentableService
        when:
        controller.deleteAttachment()
        then:
        response.status==302
        response.redirectedUrl=="/adHocAlert/alertDetail/1"
    }
    void "test findMatchedAlerts action when productName is given"(){
        setup:
        String productName="product"
        String genericName=""
        String topic="topic1"
        String event ="fatality"
        Long alertId=1L
        AdHocAlertService adHocAlertService=Mock(AdHocAlertService)
        adHocAlertService.findMatchedAlerts(_,_,_)>>{
            [adHocAlertObj2]
        }
        controller.adHocAlertService=adHocAlertService
        when:
        controller.findMatchedAlerts(productName,genericName,topic,event,alertId)
        then:
        response.status==200
        JSON.parse(response.text).get(0).assignedTo=="Fake Namer"
        JSON.parse(response.text).get(0).id==2
        JSON.parse(response.text).get(0).priority.value=="High"
        JSON.parse(response.text).get(0).name=="Test Name3"
    }
    void "test findMatchedAlerts action when productName is not given"(){
        setup:
        String topic="topic1"
        String event ="fatality"
        Long alertId=1L
        AdHocAlertService adHocAlertService=Mock(AdHocAlertService)
        adHocAlertService.findMatchedAlerts(_,_,_)>>{
            [adHocAlertObj2]
        }
        controller.adHocAlertService=adHocAlertService
        when:
        controller.findMatchedAlerts(null,null,topic,event,alertId)
        then:
        response.status==200
        JSON.parse(response.text).size()==0
    }
    void "test buildAlert"(){
        setup:
        params.detectedBy="company"
        AlertAttributesService alertAttributesService = new AlertAttributesService()
        alertAttributesService.slurp()
        controller.alertAttributesService = alertAttributesService
        when:
        AdHocAlert result=controller.buildAlert()
        then:
        response.status==200
        result.getClass()==AdHocAlert
    }
    void "test saveDelayReason action"(){
        setup:
        AdHocAlert adHocAlert=AdHocAlert.get(1)
        String reason="Due to an emergency"
        when:
        controller.saveDelayReason(adHocAlert,reason)
        then:
        response.status==200
        JSON.parse(response.text).success==true
    }
    void "test saveComment action"(){
        setup:
        String selectedAdhocAlertIds='[1]'
        String comment="new comment"
        when:
        controller.saveComment(selectedAdhocAlertIds,comment)
        then:
        response.status==200
        JSON.parse(response.text).success==true
        JSON.parse(response.text).comment==comment
    }
    void "test fetchComment action"(){
        setup:
        AdHocAlert adHocAlert = AdHocAlert.get(1)
        adHocAlert.notes="new comment"
        adHocAlert.noteModifiedBy="username"
        adHocAlert.save(flush:true,failOnError:true)
        when:
        controller.fetchComment(adHocAlert)
        then:
        JSON.parse(response.text).comment=="new comment"
        JSON.parse(response.text).createdBy=="username"
    }

    def "test mandatoryCheck with grouped fields present and other fields invalid"() {
        given:
        List<Map> adhocConfiguration = [
                [name: 'study', mandatory: true],
                [name: 'studySelection', mandatory: true],
                [name: 'productSelection', mandatory: true],
                [name: 'otherField', mandatory: true]
        ]
        Map params = [
                study: 'Some Study', // grouped field present
                studySelection: '',   // grouped field empty
                productSelection: 'Product A', // grouped field present
                otherField: ''       // invalid other field
        ]

        when:
        boolean result = controller.mandatoryCheck(adhocConfiguration, params)

        then:
        result == true // Should return true because 'otherField' is invalid
    }

    def "test mandatoryCheck with all mandatory fields valid"() {
        given:
        List<Map> adhocConfiguration = [
                [name: 'study', mandatory: true],
                [name: 'studySelection', mandatory: true],
                [name: 'productSelection', mandatory: true],
                [name: 'otherField', mandatory: true]
        ]
        Map params = [
                study: 'Some Study', // grouped field present
                studySelection: 'Selection A', // grouped field valid
                productSelection: 'Product A', // grouped field valid
                otherField: 'Valid Value' // valid other field
        ]

        when:
        boolean result = controller.mandatoryCheck(adhocConfiguration, params)

        then:
        result == false // Should return false because all mandatory fields are valid
    }
}
