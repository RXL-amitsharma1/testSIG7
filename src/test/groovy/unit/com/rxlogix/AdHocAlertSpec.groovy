package com.rxlogix

import com.rxlogix.config.Priority

import com.rxlogix.signal.AdHocAlert
import com.rxlogix.user.User
import grails.testing.gorm.DomainUnitTest
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Ignore
import spock.lang.Specification
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject

@Ignore
class AdHocAlertSpec extends Specification implements DomainUnitTest<AdHocAlert> {

    def formulations
    User userObj
    def setup() {

        formulations = new JSONArray()
        def formulation = new JSONObject()
        formulation.put("name", 'Table')
        formulation.put("id", 1)
        formulations.add(formulation)


        userObj = new User()
        userObj.username = 'username'
        userObj.createdBy = 'createdBy'
        userObj.modifiedBy = 'modifiedBy'
        userObj.grailsApplication = grailsApplication
        userObj.preference.createdBy = "createdBy"
        userObj.preference.modifiedBy = "modifiedBy"
        userObj.preference.locale = new Locale("en")
        userObj.preference.isEmailEnabled = false
        userObj.metaClass.getFullName = { "Fake Namer" }
        userObj.metaClass.getEmail = { 'fake.email@fake.com' }
        userObj.groups = []
        userObj.save(validate: false)
    }

    def "test toDetailJson" () {
        setup:
            def adHocAlert = new AdHocAlert(id: 1L,
                    productSelection: "something",
                    detectedDate: new DateTime(2015,12, 15, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate(),
                    name: "Test Name",
                    detectedBy: "Company",
                    topic: "rash",
                    initialDataSource: "Test",
                    alertVersion: 0,
                    priority: new Priority(value: "High"),
                    formulations:formulations.toString()
            )
            adHocAlert.save(failOnError: true)

        expect:
              adHocAlert.details() != [id :1, name:'Test Name',  version:0,
                             priority:'High', description:null, assignedTo:null, detectedDate:'15-Dec-2015',
                             dueDate:null, notes:null, disposition:null, detectedBy: "Company", topic: "rash", initialDataSource: "Test",
                             productSelection:'something', eventSelection:'', flagged:false,
                             followupDate:null, formulations:formulations.toString(), indication:null, reportType:'', issueTracked:'No',
                             numOfIcsrs:null, initialDataSource:null, dueIn:0, actionCount:null, alertRmpRemsRef:null]

    }

    void "test the adhoc alert persistence"() {
        setup:

        def adHocAlert = new AdHocAlert(id: 1L,
                productSelection: "something",
                detectedDate: new DateTime(2015,12, 15, 0, 0, 0).toDate(),
                name: "Test Name",
                detectedBy: "Company",
                topic: "rash",
                initialDataSource: "Test",
                alertVersion: 0,
                priority: new Priority(value: "High"),
                formulations: formulations.toString())
        adHocAlert.save(failOnError: true)

        expect:
        adHocAlert.id != null
    }

    void "test the adhoc alert failed persistence"() {
        setup:

        def adHocAlert = new AdHocAlert(
                productSelection: "something",
                detectedDate: new DateTime(2015,12, 15, 0, 0, 0).toDate(),
                alertVersion: 0,
                formulations: formulations.toString())

        adHocAlert.save(failOnError: false)

        expect:
        adHocAlert.id == null
    }

    void "test the adhoc alert topic persistence"() {
        setup:

        def adHocAlert = new AdHocAlert(id: 1L,
                productSelection: "something",
                detectedDate: new DateTime(2015,12, 15, 0, 0, 0).toDate(),
                name: "Test Name",
                detectedBy: "Company",
                initialDataSource: "Test",
                alertVersion: 0,
                priority: new Priority(value: "High"),
                topic: "Test topic",
                formulations:formulations.toString())

        adHocAlert.save(failOnError: true)

        expect:
        adHocAlert.id != null
        adHocAlert.topic == "Test topic"
    }

    void "test the adhoc alert topic null persistence"() {
        setup:

        def adHocAlert = new AdHocAlert(
                productSelection: "something",
                studySelection: "anything",
                detectedDate: new DateTime(2015,12, 15, 0, 0, 0).toDate(),
                name: "Test Name",
                detectedBy: "Company",
                initialDataSource: "Test",
                alertVersion: 0,
                countryOfIncidence: "usa",
                reportType: "pader",
                refType: "sys",
                indication: "test",
                sharedWith: userObj,
                owner: userObj,
                issuePreviouslyTracked: false,
                numberOfICSRs: 1,
                priority: new Priority(value: "High"),
                formulations: formulations.toString())

        adHocAlert.save(failOnError: false)

        expect:
        adHocAlert.id == null
    }

    void "test AdHocAlert buildProductNameList with one product name" () {
        setup:
            def aha = new AdHocAlert(productSelection:
                    '{"1":[],"2":[],"3":[{"name":"IBUPROFEN","id":100032}],"4":[],"5":[]}')
        expect:
            aha.buildProductNameList() == ['IBUPROFEN']
    }

    void "test AdHocAlert buildProductNameList with multiple product name" () {
        setup:
        def aha = new AdHocAlert(productSelection:
            '{"1":[],"2":[],"3":[{"name":"IBUPROFEN","id":100032},' +
                    '{"name":"MACROBID","id":100033},{"name":"Drug Interruption","id":100044}],' +
                    '"4":[],"5":[]}')
        expect:
            aha.buildProductNameList().sort() == ["Drug Interruption", 'IBUPROFEN', 'MACROBID']
    }

    void "test validation is true"(){
        setup:
        def badMockedAdHocAlert =  new AdHocAlert(
                productSelection: "something",
                detectedDate: new DateTime(2015,12, 15, 0, 0, 0).toDate(),
                detectedBy: "Company",
                initialDataSource: "Test",
                name: "Test Name",
                alertVersion: 0,
                priority: new Priority(value: "High"),
                formulations: formulations.toString(),
                topic: "rash")


        expect:
        assert badMockedAdHocAlert.validate()

    }

    void "test topic is null"(){
        setup:
        def badMockedAdHocAlert =  new AdHocAlert(
                productSelection: "something",
                detectedDate: new DateTime(2015,12, 15, 0, 0, 0).toDate(),
                detectedBy: "Company",
                initialDataSource: "Test",
                name: "Test Name",
                alertVersion: 0,
                priority: new Priority(value: "High"),
                formulations: formulations.toString(),
                topic: null)

        badMockedAdHocAlert.validate()
        expect:
        assert 'com.rxlogix.signal.AdHocAlert.topic.nullable' == badMockedAdHocAlert.errors['topic'].code

    }

    void "test detectedDate is null"() {
        setup:
        def badMockedAdHocAlert = new AdHocAlert(
                productSelection: "something",
                detectedDate: null,
                detectedBy: "Company",
                initialDataSource: "Test",
                name: "Test Name",
                alertVersion: 0,
                priority: new Priority(value: "High"),
                formulations: formulations.toString(),
                topic: "rash")

        badMockedAdHocAlert.validate()
        expect:

        assert 'com.rxlogix.signal.AdHocAlert.detectedDate.nullable' == badMockedAdHocAlert.errors['detectedDate'].code
    }

    void "test productSelection is null"() {
        setup:
        def badMockedAdHocAlert = new AdHocAlert(
                productSelect: null,
                detectedDate: new DateTime(2015,12, 15, 0, 0, 0).toDate(),
                detectedBy: "Company",
                initialDataSource: "Test",
                name: "Test Name",
                alertVersion: 0,
                priority: new Priority(value: "High"),
                formulations: formulations.toString(),
                topic: "rash")

        badMockedAdHocAlert.validate()
        expect:

        assert 'com.rxlogix.signal.AdHocAlert.productSelection.nullable' == badMockedAdHocAlert.errors['productSelection'].code
    }

    void "test detectedBy is null"() {
        setup:
        def badMockedAdHocAlert = new AdHocAlert(
                productSelect: "something",
                detectedDate: new DateTime(2015,12, 15, 0, 0, 0).toDate(),
                initialDataSource: "Test",
                name: "Test Name",
                alertVersion: 0,
                priority: new Priority(value: "High"),
                formulations: formulations.toString(),
                topic: "rash")

        badMockedAdHocAlert.validate()
        expect:

        assert 'com.rxlogix.signal.AdHocAlert.detectedBy.nullable' == badMockedAdHocAlert.errors['detectedBy'].code
    }
}
