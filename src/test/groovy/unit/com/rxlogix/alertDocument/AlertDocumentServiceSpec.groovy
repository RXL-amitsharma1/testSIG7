package com.rxlogix.alertDocument

import com.rxlogix.config.AlertDocument
import com.rxlogix.config.Priority

import com.rxlogix.signal.AdHocAlert
import com.rxlogix.signal.Alert
import grails.converters.JSON
import grails.core.GrailsApplication
import grails.testing.services.ServiceUnitTest
import org.joda.time.DateTime
import spock.lang.Ignore
import spock.lang.Specification


@Ignore
class AlertDocumentServiceSpec extends Specification implements ServiceUnitTest<AlertDocumentService> {
    def adHocAlert
    def alertDoc
    GrailsApplication grailsApplication

    def setup() {
        adHocAlert = new AdHocAlert(id: 1L,
                productSelection: "something",
                detectedDate: new DateTime(2015, 12, 15, 0, 0, 0).toDate(),
                detectedBy: "Company",
                topic: "rash",
                initialDataSource: "test",
                name: "Test Name",
                alertVersion: 0,
                priority: new Priority(value: "High"),
                formulations: "test")
        adHocAlert.save()
        alertDoc = new AlertDocument(chronicleId: "ch-1", alert: null, documentType: 'Test', documentLink: 'http://test_link', documentStatus: "test status",
                startDate: new Date(), statusDate: new Date(), author: "Author")
        alertDoc.save()
    }

    def cleanup() {
    }


    def "test for importDocumentsFromJson without errors" () {
        given:
            def jsonString = '''[
        {
            "chronicleId": "test-chronicle-id-0",
            "documentType": "Test",
            "documentLink": "http://test_link",
            "documentStatus": "test status",
            "startDate": "03.05.2015",
            "statusDate": "03.05.2016",
            "author": "Author"
        },
        {
            "chronicleId": "test-chronicle-id-1",
            "documentType": "Test",
            "documentLink": "http://test_link",
            "documentStatus": "test status",
            "startDate": "03.05.2015",
            "statusDate": "03.05.2016",
            "author": "Author"
        }
    ]
'''
            def jsonObj = JSON.parse(jsonString)
            service.importDocumentsFromJson(jsonObj)
            def list = AlertDocument.where {
                chronicleId == 'test-chronicle-id-0' || chronicleId == 'test-chronicle-id-1'
            }.list()

        expect:
            list.size() == 2

    }

    def "test for importDocumentsFromJson with errors" () {
        given:
        def jsonString = '''[
        {
            "chronicleId": "",
            "documentType": "Test",
            "documentLink": "http://test_link",
            "documentStatus": "test status",
            "startDate": "03.05.2015",
            "statusDate": "03.05.2016",
            "author": "Author"
        },
        {
            "documentType": "Test",
            "documentLink": "http://test_link",
            "documentStatus": "test status",
            "startDate": "03.05.2015",
            "statusDate": "03.05.2016",
            "author": "Author"
        }
    ]
'''
        def jsonObj = JSON.parse(jsonString)
        service.importDocumentsFromJson(jsonObj)


        expect:
        assert AlertDocument.list().size() == 1

    }

    def "test for target date for high priority assessment"(){
        given:
        def detectedDate = DateTime.now()
        def alertDoc = new AlertDocument(chronicleId: "ch-2", alert: null,
                documentType: "Assessment Report", documentLink: 'http://test_link', documentStatus: "test status",
                startDate: new Date(), statusDate: new Date(), author: "Author")
        alertDoc.save();
        def alert =  AdHocAlert.findById(1l)
        alert.detectedDate = detectedDate.toDate()
        alert.save()

        expect:
        assert  alertDoc.updateWithAlertId(1L).targetDate == detectedDate.plusDays(15).toDate()
    }

    def "test for target date for high priority validation"(){
        given:
        def detectedDate = DateTime.now()
        def alertDoc = new AlertDocument(chronicleId: "ch-2", alert: null,
                documentType: "Validation", documentLink: 'http://test_link', documentStatus: "test status",
                startDate: new Date(), statusDate: new Date(), author: "Author")
        alertDoc.save();
        def alert =  AdHocAlert.findById(1l)
        alert.detectedDate = detectedDate.toDate()
        alert.save()

        expect:
        assert  alertDoc.updateWithAlertId(1L).targetDate == detectedDate.plusDays(15).toDate()
    }

    def "test for target date for medium priority assessment"(){
        given:
        def detectedDate = DateTime.now()
        def alertDoc = new AlertDocument(chronicleId: "ch-2", alert: null,
                documentType: "Assessment Report", documentLink: 'http://test_link', documentStatus: "test status",
                startDate: new Date(), statusDate: new Date(), author: "Author")
        alertDoc.save();
        def alert =  AdHocAlert.findById(1l)
        alert.detectedDate = detectedDate.toDate()
        alert.priority = new Priority(value: "Medium")
        alert.save()

        expect:
        assert  alertDoc.updateWithAlertId(1L).targetDate == detectedDate.plusMonths(2).toDate()
    }

    def "test for target date for medium priority validation"(){
        given:
        def detectedDate = DateTime.now()
        def alertDoc = new AlertDocument(chronicleId: "ch-2", alert: null,
                documentType: "Validation", documentLink: 'http://test_link', documentStatus: "test status",
                startDate: new Date(), statusDate: new Date(), author: "Author")
        alertDoc.save();
        def alert =  AdHocAlert.findById(1l)
        alert.detectedDate = detectedDate.toDate()
        alert.priority = new Priority(value: "Medium")
        alert.save()

        expect:
        assert  alertDoc.updateWithAlertId(1L).targetDate == detectedDate.plusDays(30).toDate()
    }

    def "test for target date for low priority assessment"(){
        given:
        def detectedDate = DateTime.now()
        def alertDoc = new AlertDocument(chronicleId: "ch-2", alert: null,
                documentType: "Assessment Report", documentLink: 'http://test_link', documentStatus: "test status",
                startDate: new Date(), statusDate: new Date(), author: "Author")
        alertDoc.save();
        def alert =  AdHocAlert.findById(1l)
        alert.detectedDate = detectedDate.toDate()
        alert.priority = new Priority(value: "Low")
        alert.save()

        expect:
        assert  alertDoc.updateWithAlertId(1L).targetDate == detectedDate.plusMonths(3).toDate()
    }

    def "test for target date for low priority validation"(){
        given:
        def detectedDate = DateTime.now()
        def alertDoc = new AlertDocument(chronicleId: "ch-2", alert: null,
                documentType: "Validation", documentLink: 'http://test_link', documentStatus: "test status",
                startDate: new Date(), statusDate: new Date(), author: "Author")
        alertDoc.save();
        def alert =  AdHocAlert.findById(1l)
        alert.detectedDate = detectedDate.toDate()
        alert.priority = new Priority(value: "Low")
        alert.save()

        expect:
        assert  alertDoc.updateWithAlertId(1L).targetDate == detectedDate.plusDays(45).toDate()
    }
}
