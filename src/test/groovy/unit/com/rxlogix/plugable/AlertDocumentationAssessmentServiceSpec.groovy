package com.rxlogix.plugable

import com.rxlogix.alertDocument.AlertDocumentService
import com.rxlogix.config.AlertDocument
import com.rxlogix.config.Disposition
import com.rxlogix.config.Priority
import com.rxlogix.signal.AdHocAlert
import com.rxlogix.signal.Alert
import com.rxlogix.user.Role
import com.rxlogix.user.User
import grails.testing.services.ServiceUnitTest
import org.joda.time.DateTime
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

@Ignore
class AlertDocumentationAssessmentServiceSpec extends Specification implements ServiceUnitTest<AlertDocumentAssessmentService> {

    @Shared
    def AlertDocumentService alertDocumentService

    def setup() {
        createTestData()
        prepareDispositions()

        prepareAlerts_cond()
        prepareDocs_cond()
    }

    def cleanup() {
    }

    def "test for alertsWithValidatedSignalAndNotApproved" () {
        setup:
        def alert = AdHocAlert.findByName('aha1')
        alert.disposition = Disposition.findByValue("ValidatedSignal")
        alert.alertDocuments = AlertDocument.list()
        alert.save()

        expect:
        assert alert.alertDocuments.size() == 2
        service.alertsWithValidatedSignalAndNotApproved().size() == 1
    }

    def "test for cond with alert with high priority and assessment document due in 2 days"(){
        setup:
        def alert = AdHocAlert.findByName('aha1')
        alert.disposition = Disposition.findByValue("ValidatedSignal")

        def doc = AlertDocument.findByChronicleId('ucb12345')
        doc.targetDate = DateTime.now().plusDays(2).toDate()
        alert.addToAlertDocuments(doc)
        alert.save()

        expect:
        assert service.cond(alert, DateTime.now()) != null

    }

    def "test for cond with alert with high priority and assessment document due in 3 days"(){
        setup:
        def alert = AdHocAlert.findByName('aha1')
        alert.disposition = Disposition.findByValue("ValidatedSignal")

        def doc = AlertDocument.findByChronicleId('ucb12345')
        doc.targetDate = DateTime.now().plusDays(3).toDate()
        alert.addToAlertDocuments(doc)
        alert.save()

        expect:
        assert service.cond(alert, DateTime.now()) == null

    }

    def "test for cond with alert with medium priority and assessment document due in 7 days"(){
        setup:
        def alert = AdHocAlert.findByName('aha1')
        alert.disposition = Disposition.findByValue("ValidatedSignal")
        alert.priority = new Priority("value": 'Medium')

        def doc = AlertDocument.findByChronicleId('ucb12345')
        doc.targetDate = DateTime.now().plusDays(7).toDate()

        alert.addToAlertDocuments(doc)
        alert.save()

        expect:
        assert service.cond(alert, DateTime.now()) != null

    }

    def "test for cond with alert with medium priority and assessment document due in 8 days"(){
        setup:
        def alert = AdHocAlert.findByName('aha1')
        alert.disposition = Disposition.findByValue("ValidatedSignal")
        alert.priority = new Priority("value": 'Medium')

        def doc = AlertDocument.findByChronicleId('ucb12345')
        doc.targetDate = DateTime.now().plusDays(8).toDate()

        alert.addToAlertDocuments(doc)
        alert.save()

        expect:
        assert service.cond(alert, DateTime.now()) == null

    }

    def "test for allAlerts with one alert assessment notification"() {
        setup:
        def alert = AdHocAlert.findByName('aha1')
        alert.disposition = Disposition.findByValue("ValidatedSignal")
        alert.priority = new Priority("value": 'Medium')

        def doc = AlertDocument.findByChronicleId('ucb12345')
        doc.targetDate = DateTime.now().plusDays(7).toDate()

        alert.addToAlertDocuments(doc)
        alert.save()

        expect:
        assert service.allAlerts().size() == 1
    }

    def "test for allAlerts with no alert assessment notification"() {
        setup:
        def alert = AdHocAlert.findByName('aha1')
        alert.disposition = Disposition.findByValue("ValidatedSignal")
        alert.priority = new Priority("value": 'Medium')

        def doc = AlertDocument.findByChronicleId('ucb12345')
        doc.targetDate = DateTime.now().plusDays(8).toDate()

        alert.addToAlertDocuments(doc)
        alert.save()

        expect:
        assert service.allAlerts().size() == 0
    }

    def "test sending email for assessment notification with target date approaching"(){

        setup:
        def alert = AdHocAlert.findByName('aha1')
        alert.disposition = Disposition.findByValue("ValidatedSignal")
        def doc = AlertDocument.findByChronicleId('ucb12345')
        doc.updateWithAlertId(alert.id)

        assert alert.alertDocuments.size() == 1

        def documents = []
        def alerts = service.allAlerts()

        service.allAlerts()?.each {
            documents.add(it.alertDocuments.find{d -> d.documentType == 'Assessment Report' && d.documentStatus != 'Approved'})
        }


        expect:

        documents.size() == 1
    }


    //Setup Alerts and Documents *****************************************************************
    def prepareAlerts_cond() {
        new AdHocAlert(
                productSelection: "something",
                detectedDate: new DateTime(2016, 02, 03, 0, 0, 0).toDate(),
                detectedBy: "Company",
                topic:  "rash",
                initialDataSource: "test",
                name: "aha1",
                alertVersion: 0,
                priority: Priority.findByValue("High")).save(flush: true)

        new AdHocAlert(
                productSelection: "something",
                detectedDate: new DateTime(2016, 02, 03, 0, 0, 0).toDate(),
                detectedBy: "Company",
                topic:  "rash",
                initialDataSource: "test",
                name: "aha2",
                alertVersion: 0,
                priority: Priority.findByValue("High")).save(flush: true)

        assert AdHocAlert.count() == 2
    }

    def prepareDocs_cond() {
        new AlertDocument(
                "documentLink": "http://ucb.com",
                "startDate": DateTime.now().minusDays(2),
                targetDate: DateTime.now().plusDays(3),
                "documentStatus": "Approved",
                "author": "Jim Jones",
                "statusDate": DateTime.now().minusDays(2),
                "documentType": "Validation",
                "chronicleId": "ucb1234",
                alert: AdHocAlert.get(1000L)
        ).save()

        new AlertDocument(
                "documentLink": "http://ucb.com",
                "startDate": DateTime.now().minusDays(2),
                targetDate: DateTime.now().plusDays(3),
                "documentStatus": "Pending",
                "author": "Jim Jones",
                "statusDate": DateTime.now().minusDays(2),
                "documentType": "Assessment Report",
                "chronicleId": "ucb12345",
                alert: AdHocAlert.get(1000L)
        ).save()

        assert AlertDocument.count() == 2
    }

    def prepareDispositions(){
        def disp1 = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal")
        def disp2 = new Disposition(value: "Non-ValidatedSignal", displayName: "Non-Validated Signal")
        disp1.save()
        disp2.save()
        assert (Disposition.count == 2)
    }
}
