package com.rxlogix

import com.rxlogix.config.*
import com.rxlogix.enums.GroupType
import com.rxlogix.mapping.LmCountry
import com.rxlogix.mapping.LmFormulation
import com.rxlogix.mapping.LmReportType
import com.rxlogix.signal.AdHocAlert
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.Group
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.services.ServiceUnitTest
import org.joda.time.DateTime
import spock.lang.Ignore
import spock.lang.Specification


class AdHocAlertServiceSpec extends Specification implements ServiceUnitTest<AdHocAlertService> {

    def setup() {

        ActivityType activityTypeObj = new ActivityType(value:  ActivityTypeValue.DispositionChange)
        activityTypeObj.save(failOnError: true)

        Disposition disposition1 = new Disposition(value: "ValidatedSignal2", displayName: "ValidatedSignal2", closed: false,
                validatedConfirmed: false,abbreviation: "C")
        disposition1.save(failOnError: true)

        Group wfGroup = new Group(name: "Default1", groupType: GroupType.WORKFLOW_GROUP, createdBy: 'createdBy',
                modifiedBy: 'modifiedBy',
                defaultQualiDisposition: disposition1,
                defaultQuantDisposition: disposition1,
                defaultAdhocDisposition: disposition1,
                defaultEvdasDisposition: disposition1,
                defaultLitDisposition: disposition1,
                defaultSignalDisposition: disposition1)
        wfGroup.save(flush: true, failOnError: true)

        Preference preference=new Preference(locale:new Locale('en'),createdBy: 'admin',modifiedBy: 'admin')

        User admin = new User(username: 'admin', createdBy: 'createdBy', modifiedBy: 'modifiedBy', fullName: 'Admin User', groups: [wfGroup], preference: preference)
        User sharedUser = new User(username: 'drno', createdBy: 'createdBy', modifiedBy: 'modifiedBy', fullName: 'Dr. No', groups: [wfGroup], preference: preference)
        User importer = new User(username: 'importer', createdBy: 'createdBy', modifiedBy: 'modifiedBy', fullName: 'Interface User', groups: [wfGroup], preference: preference)
        admin.save(flush: true, failOnError: true)
        sharedUser.save(flush: true, failOnError: true)
        importer.save(flush: true, failOnError: true)
        assert (User.count == 3)

        Priority priority1 = new Priority(value: "High", display: true, displayName: "High", reviewPeriod: 3, priorityOrder: 1)
        Priority priority2 = new Priority(value: "Medium", display: true, displayName: "Medium", reviewPeriod: 5, priorityOrder: 2, defaultPriority: true)
        Priority priority3 = new Priority(value: "Low", display: true, displayName: "Low", reviewPeriod: 7, priorityOrder: 3)
        priority1.save(flush: true, failOnError: true)
        priority2.save(flush: true, failOnError: true)
        priority3.save(flush: true, failOnError: true)
        Priority priority = new Priority(value: "high", id: 1L, reviewPeriod: 20, displayName: "high")
        priority.save(flush: true, failOnError: true)

        ValidatedSignal validatedSignal1=new ValidatedSignal(name: "validateSignal1")
        validatedSignal1.save(validate:false)

        AdHocAlert adHocAlertObj = new AdHocAlert(
                detectedDate: new DateTime(2015,12, 15, 0, 0, 0).toDate(),
                detectedBy: "Company",
                initialDataSource: "test",
                name: "Test Name2",
                alertVersion: 0,
                priority: priority,
                topic:"rash",
                assignedTo: admin,
                disposition: disposition1,
                productSelection:"IBUPROFEN",
                owner: admin,
                eventSelection: "something",
        )
        adHocAlertObj.addToValidatedSignals(validatedSignal1)
        adHocAlertObj.save(failOnError: true)
        AdHocAlert adHocAlertObj2 = new AdHocAlert(
                productSelection: "something",
                detectedDate: new DateTime(2015,12, 15, 0, 0, 0).toDate(),
                detectedBy: "Company",
                initialDataSource: "test",
                name: "Test Name3",
                alertVersion: 0,
                priority: priority,
                topic:"rash",
                disposition: disposition1,
                assignedTo: admin,
                eventSelection: "1"
        )
        adHocAlertObj2.save(failOnError: true)
        EmailNotificationService emailNotificationService=Mock(EmailNotificationService)
        emailNotificationService.emailNotificationWithBulkUpdate(_,_)>>{
            return false
        }
        service.emailNotificationService=emailNotificationService
        SpringSecurityService springSecurityService=Mock(SpringSecurityService)
        springSecurityService.isLoggedIn()>>{
            return  true
        }
        springSecurityService.principal>>{
            return admin
        }
        service.userService.springSecurityService=springSecurityService
    }

    def cleanup() {
    }

    void "test list action"(){
        when:
        List result=service.list()
        then:
        result.size()==2
        result[0]==AdHocAlert.get(1)
        result[1]==AdHocAlert.get(2)
    }

    def "test listWithFilter filter by disposition" () {
        when:
        List result=service.listWithFilter([dispositionFilter: "1"])
        then:
        result.size() == 2
    }
    def "test listWithFilter filter by priority" () {
        when:
        List result=service.listWithFilter([priorityFilter: "4"])
        then:
        result.size() == 2
    }
    def "test listWithFilter filter by product name" () {
        when:
        List result=service.listWithFilter([productSelectionFilter: 'IBUPROFEN' ])
        then:
        result.size() == 1
    }
    def "test listWithFilter filter by event" () {
        when:
        List result=service.listWithFilter([eventSelectionFilter: '1' ])
        then:
        result.size()== 1
    }
    def "test listWithFilter when filter is not valid" () {
        when:
        List result=service.listWithFilter([invalidFilter: '1' ])
        then:
        result.size()== 2
    }
    void "test listSelectedAlerts action"(){
        setup:
        String alerts="1,2"
        when:
        List result=service.listSelectedAlerts(alerts)
        then:
        result.size()==2
        result[0]==AdHocAlert.get(1)
        result[1]==AdHocAlert.get(2)
    }
    @Ignore
    def "test importAlert Create Alerts"() {
        when:
        def jsonAlerts = '[{"id":"1385437113430",\n' +
                '"detectedByUCB":"UCB",\n' +
                '"description":"Babwah Terence J, An unexpected temporary suppression of lactation after a local corticosteroid injection for tenosynovitis., The European journal of general practice, 2013",\n' +
                '"safetyObservation":"01-Mar-2016",\n' +
                '"initialDataSource":"SLIM",\n' +
                '"alertName":"Lidocaine, Injection of lidocaine supressed lactation in  apatient: SNB, 1385437113430",\n' +
                '"productName":"Lidocaine",\n' +
                '"topicName":"Injection of lidocaine supressed lactation in  apatient: SNB",\n'+
                '"Assigned To":"admin"}]'

        def alertResults = service.importAlert(jsonAlerts)
        then:
        assert alertResults[0].name == "Lidocaine, Injection of lidocaine supressed lactation in  apatient: SNB, 1385437113430"
    }
    @Ignore
    void "test importAlert setting alert attributes"() {
        when:
        def jsonAlerts = '[{"id":"1385437113430",\n' +
                '"detectedByUCB":"UCB",\n' +
                '"description":"Babwah Terence J, An unexpected temporary suppression of lactation after a local corticosteroid injection for tenosynovitis., The European journal of general practice, 2013",\n' +
                '"safetyObservation":"01-Mar-2016",\n' +
                '"initialDataSource":"SLIM",\n' +
                '"alertName":"Lidocaine, Injection of lidocaine supressed lactation in  apatient: SNB, 1385437113430",\n' +
                '"productName":"Lidocaine",\n' +
                '"topicName":"Injection of lidocaine supressed lactation in  apatient: SNB",\n' +
                '"Assigned To":"admin"}]'
        def alertResults = service.importAlert(jsonAlerts)
        assert alertResults.size() == 1
        AdHocAlert aa = alertResults[0]
        then:
        assert aa.attributesMap.slimId == "1385437113430"
    }
    @Ignore
    void "test importAlert adding generic names to AdHocAlert"(){
        when:
        def jsonAlerts = '[{"id":"1385437113430",\n' +
                '"detectedByUCB":"UCB",\n' +
                '"description":"Babwah Terence J, An unexpected temporary suppression of lactation after a local corticosteroid injection for tenosynovitis., The European journal of general practice, 2013",\n' +
                '"safetyObservation":"01-Mar-2016",\n' +
                '"initialDataSource":"SLIM",\n' +
                '"alertName":"Lidocaine, Injection of lidocaine supressed lactation in  apatient: SNB, 1385437113430",\n' +
                '"genericName":"Lidocaine",\n' +
                '"topicName":"Injection of lidocaine supressed lactation in  apatient: SNB",\n'+
                '"Assigned To":"admin"}]'
        def alertResults = service.importAlert(jsonAlerts)
        assert alertResults.size() == 1
        AdHocAlert aa = alertResults[0]
        then:
        def generic = aa.getNameFieldFromJson(aa.productSelection)
        assert generic == "Lidocaine"
    }
    @Ignore
    void "test importAlert adding product name to AdHocAlert"(){
        when:
        def jsonAlerts = '[{"id":"1385437113430",\n' +
                '"detectedByUCB":"UCB",\n' +
                '"description":"Babwah Terence J, An unexpected temporary suppression of lactation after a local corticosteroid injection for tenosynovitis., The European journal of general practice, 2013",\n' +
                '"safetyObservation":"01-Mar-2016",\n' +
                '"initialDataSource":"SLIM",\n' +
                '"alertName":"Lidocaine, Injection of lidocaine supressed lactation in  apatient: SNB, 1385437113430",\n' +
                '"productName":"Lidocaine",\n' +
                '"topicName":"Injection of lidocaine supressed lactation in  apatient: SNB",\n'+
                '"Assigned To":"admin"}]'
        def alertResults = service.importAlert(jsonAlerts)
        assert alertResults.size() == 1
        AdHocAlert aa = alertResults[0]
        then:
        def product = aa.getNameFieldFromJson(aa.productSelection)
        assert product == "Lidocaine"
    }
    @Ignore
    void "test importAlert without detected date"(){
        when:
        def jsonAlerts = '[{"id":"1385437113430",\n' +
                '"detectedByUCB":"UCB",\n' +
                '"description":"Babwah Terence J, An unexpected temporary suppression of lactation after a local corticosteroid injection for tenosynovitis., The European journal of general practice, 2013",\n' +
                '"initialDataSource":"SLIM",\n' +
                '"alertName":"Lidocaine, Injection of lidocaine supressed lactation in  apatient: SNB, 1385437113430",\n' +
                '"productName":"Lidocaine",\n' +
                '"topicName":"Injection of lidocaine supressed lactation in  apatient: SNB",' +
                '"myDetectedDate": "04/12/2016",\n'+
                '"Assigned To":"admin"}]'
        def alertResults = service.importAlert(jsonAlerts)
        then:
        assert alertResults.size() == 1
    }
    @Ignore
    void "test importAlert without detected by"(){
        when:
            def jsonAlerts = '[{"id":"1385437113430",\n' +
                    '"description":"Babwah Terence J, An unexpected temporary suppression of lactation after a local corticosteroid injection for tenosynovitis., The European journal of general practice, 2013",\n' +
                    '"detectedByUCB":"UCB",\n' +
                    '"safetyObservation":"01-Mar-2016",\n' +
                    '"initialDataSource":"SLIM",\n' +
                    '"alertName":"Lidocaine, Injection of lidocaine supressed lactation in  apatient: SNB, 1385437113430",\n' +
                    '"productName":"Lidocaine",\n' +
                    '"topicName":"Injection of lidocaine supressed lactation in  apatient: SNB",' +
                    '"myDetectedDate": "04/12/2016",\n'+
                    '"Assigned To":"admin"}]'
            def alertResults = service.importAlert(jsonAlerts)
        then:
            assert alertResults.size() == 1
    }
    @Ignore
    void "test importAlert without initial data source"(){
        when:
        def jsonAlerts = '[{"id":"1385437113430",\n' +
                '"description":"Babwah Terence J, An unexpected temporary suppression of lactation after a local corticosteroid injection for tenosynovitis., The European journal of general practice, 2013",\n' +
                '"detectedByUCB":"UCB",\n' +
                '"safetyObservation":"01-Mar-2016",\n' +
                '"initialDataSource":"SLIM",\n' +
                '"alertName":"Lidocaine, Injection of lidocaine supressed lactation in  apatient: SNB, 1385437113430",\n' +
                '"productName":"Lidocaine",\n' +
                '"myDetectedDate": "04/12/2016",\n' +
                '"topicName":"Injection of lidocaine supressed lactation in  apatient: SNB",\n'+
                '"Assigned To":"admin"}]'
        def alertResults = service.importAlert(jsonAlerts)
        then:
        assert alertResults.size() == 1
    }
    @Ignore
    void "test importAlert action without topic"(){
        when:
        def jsonAlerts = '[{"id":"1385437113430",\n' +
                '"detectedByUCB":"UCB",\n' +
                '"description":"Babwah Terence J, An unexpected temporary suppression of lactation after a local corticosteroid injection for tenosynovitis., The European journal of general practice, 2013",\n' +
                '"safetyObservation":"01-Mar-2016",\n' +
                '"initialDataSource":"SLIM",\n' +
                '"alertName":"Lidocaine, Injection of lidocaine supressed lactation in  apatient: SNB, 1385437113430",\n' +
                '"detectedDate": "04/12/2016",' +
                '"topicName":"Injection of lidocaine supressed lactation in  apatient: SNB",' +
                '"productName":"Lidocaine",\n' +
                '"Assigned To":"admin"}]'
        def alertResults = service.importAlert(jsonAlerts)
        then:
        assert alertResults.size() == 1
    }
    @Ignore
    def "test importAlert null topic"(){
        when:
        def jsonAlerts = '[{"id":"1385437113430",\n' +
                '"detectedByUCB":"UCB",\n' +
                '"description":"testing",\n' +
                '"safetyObservation":"01-Mar-2016",\n' +
                '"initialDataSource":"SLIM",\n' +
                '"alertName":"Lidocaine",\n' +
                '"productName":"Lidocaine",\n' +
                '"topicName":"Injection of lidocaine supressed lactation in  apatient: SNB",\n'+
                '"Assigned To":"admin"}]'

        def alertResults = service.importAlert(jsonAlerts)

        def alert = AdHocAlert.findByName('Lidocaine')
        assert alert != null
        alert.topic = null
        alert.validate()

        then:
        alert.errors.errorCount == 1
    }
    void "test saveAlerts action"(){
        setup:
        AdHocAlert adHocAlertObj2 = new AdHocAlert(
                productSelection: "something",
                detectedDate: new DateTime(2015,12, 15, 0, 0, 0).toDate(),
                detectedBy: "Company",
                initialDataSource: "test",
                name: "Test Name3",
                alertVersion: 0,
                priority: Priority.get(1),
                topic:"rash",
                disposition: Disposition.get(1),
                assignedTo: User.get(1),
                eventSelection: "1"
        )
        when:
        def result=service.saveAlerts([adHocAlertObj2])
        then:
        result.get(0)==AdHocAlert.get(3)
    }
    @Ignore
    void "test buildAlert action"(){
        setup:
        Map jsonAlert=["id":"1385437113430", "detector":"UCB", "description":
                "Babwah Terence J, An unexpected temporary suppression of lactation after a local " +
                        "corticosteroid injection for tenosynovitis., The European journal of general " +
                        "practice, 2013", "myDetectedDate":"01-Mar-2016", "initialDataSource":"SLIM",
                       "name":"Lidocaine", "Injection of lidocaine supressed lactation in  apatient":
                               "SNB, 1385437113430", "detectedDate":"04/12/2016",
                       "topic":"Injection of lidocaine supressed lactation in  apatient: SNB",
                       productName:"Lidocaine", assignedUser:"admin"]
        when:
        AdHocAlert result=service.buildAlert(jsonAlert)
        then:
        result.getClass()==AdHocAlert
    }
    void "test  getDefaultPriority action."() {
        when:
        Priority result = service.getDefaultPriority()
        then:
        result == Priority.findByValue('Medium')

    }
    void "test getDefaultPriority action when default priority does not exists."() {
        when:
        Priority priority=Priority.findByDefaultPriority(true)
        priority.defaultPriority=false
        priority.save(flush:true,failOnError:true)
        Priority result = service.getDefaultPriority()
        then:
        result == null
    }
    void "test buildProductSelection action when both product name and generic name is given"(){
        setup:
        Map jsonAlert=["productName":"paracetamol","genericNames":"paraGeneric"]
        when:
        String result=service.buildProductSelection(jsonAlert)
        then:
        result=='{"1":[],"2":[],"3":[{"name":"paracetamol"}],"4":[],"5":[]}'
    }
    void "test buildProductSelection action when only generic name is given"(){
        setup:
        Map jsonAlert=["genericNames":"paraGeneric"]
        when:
        String result=service.buildProductSelection(jsonAlert)
        then:
        result=='{"1":[],"2":[],"3":[],"4":[],"5":[{"genericName":"paraGeneric"}]}'
    }
    void "test buildProductSelection action when neither product name is given nor generic name is given"(){
        setup:
        Map jsonAlert=[:]
        when:
        String result=service.buildProductSelection(jsonAlert)
        then:
        result==null
    }
    def "test findAlertsByProductName action with the multiple product names" () {
        setup:
        AdHocAlert alert1 = new AdHocAlert(id: 1L,
                productSelection: '{"1":[],"2":[],"3":' +
                        '[{"name":"IBUPROFEN","id":100032},' +
                        '{"name":"MACROBID","id":100033},' +
                        '{"name":"Drug Interruption","id":100044}],"4":[],"5":[]}',
                detectedDate: new DateTime(2015,12, 15, 0, 0, 0).toDate(),
                detectedBy: "Company",
                initialDataSource: "test",
                topic: "rash",
                name: "Test Name 1",
                alertVersion: 0,
                priority: Priority.findByValue("High"),
                assignedTo: User.findByUsername('admin')
        )
        alert1.save()
        AdHocAlert alert2 = new AdHocAlert(id: 2L,
                productSelection: "something",
                detectedDate: new DateTime(2015,12, 15, 0, 0, 0).toDate(),
                detectedBy: "Company",
                initialDataSource: "test",
                topic: "rash",
                name: "Test Name 2",
                alertVersion: 0,
                priority: Priority.findByValue("High"),
                assignedTo: User.findByUsername('admin')
        )
        alert2.save()
        String productNames = "IBUPROFEN, MACROBID, Drug Interruption"
        when:
        List result=service.findAlertsByProductName(productNames.toLowerCase().tokenize(','))
        then:
        result.size() == 2
    }
    def "test findAlertsByProductName action with no product names" () {
        when:
        List result=service.findAlertsByProductName(null)
        then:
        result.size() == 0
    }
    def "test for findMatchedAlerts with a matched topic name and product name" () {
        setup:
        new AdHocAlert(id: 1L,
                productSelection: '{"1":[],"2":[],"3":[{"name":"IBUPROFEN","id":100032}],"4":[],"5":[]}',
                eventSelection: '{"1":[],"2":[],"3":[{"name":"EVENT 1","id":10001}],"4":[],"5":[]}',
                detectedDate: new DateTime(2015,12, 15, 0, 0, 0).toDate(),
                detectedBy: "Company",
                initialDataSource: "test",
                name: "Test Name 1",
                alertVersion: 0,
                priority: Priority.findByValue("High"),
                topic: "test1",
                assignedTo: User.findByUsername('admin')).save()
        new AdHocAlert(id: 2L,
                productSelection: "something else",
                detectedDate: new DateTime(2015,12, 15, 0, 0, 0).toDate(),
                detectedBy: "Company",
                initialDataSource: "test",
                name: "Test Name 2",
                alertVersion: 0,
                topic: "test2",
                priority: Priority.findByValue("Medium"),
                assignedTo: User.findByUsername('admin')).save()
        expect:
        service.findMatchedAlerts(["ibuprofen"], "test1", null).size() == 1
    }
    def "test for findMatchedAlerts with unmatched product name" () {
        setup:
        new AdHocAlert(id: 1L,
                productSelection: '{"1":[],"2":[],"3":[{"name":"IBUPROFEN","id":100032}],"4":[],"5":[]}',
                eventSelection: '{"1":[],"2":[],"3":[{"name":"EVENT 1","id":10001}],"4":[],"5":[]}',
                detectedDate: new DateTime(2015,12, 15, 0, 0, 0).toDate(),
                detectedBy: "Company",
                initialDataSource: "test",
                name: "Test Name 1",
                alertVersion: 0,
                priority: Priority.findByValue("High"),
                topic: "test1",
                assignedTo: User.findByUsername('admin')).save()
        new AdHocAlert(id: 2L,
                productSelection: "something else",
                detectedDate: new DateTime(2015,12, 15, 0, 0, 0).toDate(),
                detectedBy: "Company",
                initialDataSource: "test",
                name: "Test Name 2",
                alertVersion: 0,
                topic: "test1",
                priority: Priority.findByValue("Medium"),
                assignedTo: User.findByUsername('admin')).save()
        expect:
        service.findMatchedAlerts(["ibuprofen11"], "test1", null).size() == 0
    }
    def "test for findMatchedAlerts with unmatched topic name  and a unmatched event selection" () {
        setup:
        new AdHocAlert(id: 1L,
                productSelection: '{"1":[],"2":[],"3":[{"name":"IBUPROFEN","id":100032}],"4":[],"5":[]}',
                eventSelection: '{"1":[],"2":[],"3":[{"name":"EVENT 1","id":10001}],"4":[],"5":[]}',
                detectedDate: new DateTime(2015,12, 15, 0, 0, 0).toDate(),
                detectedBy: "Company",
                initialDataSource: "test",
                name: "Test Name 1",
                alertVersion: 0,
                priority: Priority.findByValue("High"),
                topic: "test1",
                assignedTo: User.findByUsername('admin')).save()
        new AdHocAlert(id: 2L,
                productSelection: "something else",
                detectedDate: new DateTime(2015,12, 15, 0, 0, 0).toDate(),
                detectedBy: "Company",
                initialDataSource: "test",
                name: "Test Name 2",
                alertVersion: 0,
                topic: "test1",
                priority: Priority.findByValue("Medium"),
                assignedTo: User.findByUsername('admin')).save()
        expect:
        service.findMatchedAlerts(["ibuprofen"], "test12", null).size() == 0
    }
    def "test for findMatchedAlerts with unmatched topic name  and a matched event selection" () {
        setup:
        new AdHocAlert(id: 1L,
                productSelection: '{"1":[],"2":[],"3":[{"name":"IBUPROFEN","id":100032}],"4":[],"5":[]}',
                eventSelection: '{"1":[],"2":[],"3":[{"name":"EVENT 1","id":10001}],"4":[],"5":[]}',
                detectedDate: new DateTime(2015,12, 15, 0, 0, 0).toDate(),
                detectedBy: "Company",
                initialDataSource: "test",
                name: "Test Name 1",
                alertVersion: 0,
                priority: Priority.findByValue("High"),
                topic: "test1",
                assignedTo: User.findByUsername('admin')).save()
        new AdHocAlert(id: 2L,
                productSelection: '{"1":[],"2":[],"3":[{"name":"IBUPROFEN","id":100032}],"4":[],"5":[]}',
                eventSelection: '{"1":[],"2":[],"3":[{"name":"EVENT 1","id":10001}],"4":[],"5":[]}',
                detectedDate: new DateTime(2015,12, 15, 0, 0, 0).toDate(),
                detectedBy: "Company",
                initialDataSource: "test",
                name: "Test Name 2",
                alertVersion: 0,
                priority: Priority.findByValue("Medium"),
                topic: "test1",
                assignedTo: User.findByUsername('admin')).save()
        expect:
        service.findMatchedAlerts(["ibuprofen"], "test12",
                'EVENT 1').size() == 2
    }
    def "intersectEvents test" () {
        expect:
        service.intersectEvents("evt1, evt2", "evt1, evt3") == ['evt1']
    }
    def "intersectEvents test with empty input" () {
        expect:
        service.intersectEvents("evt1, evt2", null) == []
    }
    def "intersectEvents test with non-intersacted input" () {
        expect:
        service.intersectEvents("evt1, evt2", "evt3, evt4") == []
    }
    void "test copyAlert action"(){
        setup:
        AdHocAlert adHocAlert=AdHocAlert.get(1)
        User user=User.findByUsername("admin")
        when:
        AdHocAlert result=service.copyAlert(adHocAlert,user)
        then:
        result.id==3L
    }
    void "test generateUniqueName action"(){
        setup:
        AdHocAlert adHocAlert=AdHocAlert.get(1)
        when:
        String result=service.generateUniqueName(adHocAlert)
        then:
        result=='Copy of Test Name2'
    }
    @Ignore
    void "test changeDisposition action"(){
        setup:
        String selectedRows='[{"alert.id":1}]'
        Disposition targetDisposition=Disposition.get(2)
        String justification="change needed"
        String validatedSignalName="ValidatedSignal2"
        String productJson="product1"
        ActivityService activityService=Mock(ActivityService)
        activityService.create(_,_,_,_,_,_)>>{
            ActivityType activityType = ActivityType.findByValue(ActivityTypeValue.DispositionChange)
            User user=User.findByUsername("admin")
            AdHocAlert adHocAlert=AdHocAlert.get(1)
            Disposition previousDisposition = adHocAlert.disposition
            adHocAlert.disposition = targetDisposition
            String details="Disposition changed from '$previousDisposition' to '$targetDisposition'"
            String attrs='{"product":"IBUPROFEN"," event":"something"}'
            def activity = new Activity(
                    type: activityType,
                    performedBy: user,
                    details: details,
                    timestamp: DateTime.now(),
                    alert: adHocAlert,
                    justification: justification,
                    attributes: attrs
            )
            adHocAlert.addToActivities(activity)
            activity.save()
            adHocAlert.save()
        }
        service.activityService=activityService
        when:
        boolean result=service.changeDisposition(selectedRows,targetDisposition,justification,validatedSignalName,productJson,1)
        then:
        result==true
    }

    void "test revertDisposition action"(){
        setup:
        String selectedRows='[{"alert.id":1}]'
        Disposition targetDisposition=Disposition.get(2)
        String justification="change needed"
        String validatedSignalName="ValidatedSignal2"
        String productJson="product1"
        ActivityService activityService=Mock(ActivityService)
        activityService.create(_,_,_,_,_,_)>>{
            ActivityType activityType = ActivityType.findByValue(ActivityTypeValue.DispositionChange)
            User user=User.findByUsername("admin")
            AdHocAlert adHocAlert=AdHocAlert.get(1)
            Disposition previousDisposition = adHocAlert.disposition
            adHocAlert.disposition = targetDisposition
            String details="Disposition changed from '$previousDisposition' to '$targetDisposition'"
            String attrs='{"product":"IBUPROFEN"," event":"something"}'
            def activity = new Activity(
                    type: activityType,
                    performedBy: user,
                    details: details,
                    timestamp: DateTime.now(),
                    alert: adHocAlert,
                    justification: justification,
                    attributes: attrs
            )
            adHocAlert.addToActivities(activity)
            activity.save()
            adHocAlert.save()
        }
        service.activityService=activityService
        when:
        boolean result=service.revertDisposition(1,"")
        then:
        result
    }

    void "test createActivityForUndoDisposition"() {
        setup:

        when:
        def result = service.createActivityForUndoDisposition(adHocAlert,"test justification",user)
        then:
        !result
    }

    void "test createActivityForDispositionChange"(){
        setup:
        Disposition targetDisposition=Disposition.get(2)
        AdHocAlert adHocAlert=AdHocAlert.get(1)
        Disposition previousDisposition = adHocAlert.disposition
        ActivityService activityService=Mock(ActivityService)
        String justification="new change needed"
        User user=User.findByUsername("admin")
        activityService.create(_,_,_,_,_,_)>>{
            ActivityType activityType = ActivityType.findByValue(ActivityTypeValue.DispositionChange)
            adHocAlert.disposition = targetDisposition
            String details="Disposition changed from '$previousDisposition' to '$targetDisposition'"
            String attrs='{"product":"IBUPROFEN"," event":"something"}'
            def activity = new Activity(
                    type: activityType,
                    performedBy: user,
                    details: details,
                    timestamp: DateTime.now(),
                    alert: adHocAlert,
                    justification: justification,
                    attributes: attrs
            )
            adHocAlert.addToActivities(activity)
            activity.save()
            adHocAlert.save()
        }
        service.activityService=activityService
        when:
        boolean result=service.createActivityForDispositionChange(adHocAlert,previousDisposition,targetDisposition,justification,user)
        then:
        adHocAlert.activities[0].type==ActivityType.findByValue(ActivityTypeValue.DispositionChange)
        adHocAlert.activities[0].justification=="new change needed"
    }
    void "test getDispositionsForName when dispositionFilters is given"(){
        setup:
        List dispositionFilters=["ValidatedSignal2"]
        when:
        List result=service.getDispositionsForName(dispositionFilters)
        then:
        result.size()==1
        result.get(0)==Disposition.get(1)
    }
    void "test getDispositionsForName when dispositionFilters is not given"(){
        when:
        List result=service.getDispositionsForName(null)
        then:
        result.size()==0
    }
    void "test getCountryNames when there are LmCountry"(){
        setup:
        LmCountry lmCountry1=new LmCountry(name:"secondLmCountry")
        lmCountry1.id="1"
        lmCountry1.save(flush:true,failOnError: true)
        LmCountry lmCountry2=new LmCountry(name:"firstLmCountry")
        lmCountry2.id="2"
        lmCountry2.save(flush:true,failOnError: true)
        when:
        List result=service.getCountryNames()
        then:
        result.size()==2
        result.get(0)==lmCountry2
        result.get(1)==lmCountry1
    }
    void "test getCountryNames when there are no LmCountry"(){
        when:
        List result=service.getCountryNames()
        then:
        result.size()==0
    }
    void "test getFormulations when there are LmFormulation"(){
        setup:
        BigDecimal id=new BigDecimal("1")
        LmFormulation lmFormulation1=new LmFormulation(formulation: "lmFormulation1")
        lmFormulation1.id=id
        lmFormulation1.save(flush:true)
        when:
        List result=service.getFormulations()
        then:
        result.size()==1
        result.get(0)==lmFormulation1
    }
    void "test getFormulations when there are no LmFormulation"(){
        when:
        List result=service.getFormulations()
        then:
        result.size()==0
    }
    void "test getLmRetportTypes when there are LmReportType"(){
        setup:
        LmReportType lmReportType=new LmReportType(type:"type1")
        lmReportType.id="1"
        lmReportType.save(flush:true,failOnError: true)
        when:
        List result=service.getLmRetportTypes()
        then:
        result.size()==1
        result.get(0)==lmReportType
    }
    void "test getLmRetportTypes when there are no LmReportType"(){
        when:
        List result=service.getLmRetportTypes()
        then:
        result.size()==0
    }
}
