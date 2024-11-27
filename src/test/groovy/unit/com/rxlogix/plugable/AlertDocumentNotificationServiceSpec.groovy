package com.rxlogix.plugable

import com.rxlogix.config.AlertDocument
import com.rxlogix.config.Priority
import com.rxlogix.signal.AdHocAlert
import grails.testing.services.ServiceUnitTest
import org.joda.time.DateTime
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@Ignore
class AlertDocumentNotificationServiceSpec extends Specification implements ServiceUnitTest<AlertDocumentNotificationService> {

    def setup() {
        createTestData()

        prepareAlerts_cond()
        prepareDocs_cond()
    }

    def cleanup() {
    }

    void "test something"() {
    }

    void "test docCompleteWithinDays before the target date"(){
        when:
        def doc = new AlertDocument("documentType": "Validation",
                "documentLink": "http://ucb.com",
                "startDate": new DateTime(2015, 02, 01, 0, 0, 0).toDate(),
                "documentStatus": "Pending",
                "author": "John Doe",
                "targetDate": new DateTime(2015, 02, 03, 0, 0, 0).toDate(),
                "chronicleId": "ucb1234")


        then:
        service._dueWithinDaysOfGivenTime(doc.targetDate, 1, new DateTime(2015, 02, 01, 0, 0, 0)) == false
    }

    void "test docCompleteWithinDays will be due"(){
        when:
            def doc = new AlertDocument("documentType": "Validation",
                    "documentLink": "http://ucb.com",
                    "startDate": new DateTime(2015, 02, 01, 0, 0, 0).toDate(),
                    "documentStatus": "Pending",
                    "author": "John Doe",
                    "targetDate":new DateTime(2015, 02, 03, 0, 0, 0).toDate(),
                    "chronicleId": "ucb1234")


        then:
            service._dueWithinDaysOfGivenTime(doc.targetDate, 3, new DateTime(2015, 02, 01, 0, 0, 0)) == true
    }

    void "test docCompleteWithinDays due on same day as the target date"(){
        when:
            def doc = new AlertDocument("documentType": "Validation",
                    "documentLink": "http://ucb.com",
                    "startDate": new DateTime(2015, 02, 01, 0, 0, 0).toDate(),
                    "documentStatus": "Pending",
                    "author": "John Doe",
                    "targetDate":new DateTime(2015, 02, 03, 0, 0, 0).toDate(),
                    "chronicleId": "ucb1234")


        then:
            service._dueWithinDaysOfGivenTime(doc.targetDate, 2, new DateTime(2015, 02, 01, 0, 0, 0)) == false
    }

    def "test for cond_1 with high priority alert that has a document which will not be due in two days" () {
        setup:
            def alert = AdHocAlert.findByName('aha1')
            def doc = AlertDocument.findByChronicleId('ucb1234')
            doc.updateWithAlertId(alert.id)

            alert = AdHocAlert.findByName('aha1')
            assert alert.alertDocuments.size() == 1

            alert.alertDocuments[0].targetDate = DateTime.now().plusDays(3).toDate()
            alert.alertDocuments[0].documentType = "Validation"

        expect:
            service.cond_1(alert, DateTime.now()) == null
    }

    def "test for cond_1 with high priority alert that has a document which will be due in 1 days" () {
        setup:
            def alert = AdHocAlert.findByName('aha1')
            def doc = AlertDocument.findByChronicleId('ucb1234')
            doc.updateWithAlertId(alert.id)

            alert = AdHocAlert.findByName('aha1')
            assert alert.alertDocuments.size() == 1

            alert.alertDocuments[0].targetDate = DateTime.now().plusDays(1).toDate()
            alert.alertDocuments[0].documentType = "Validation"

        expect:
            service.cond_1(alert, DateTime.now()) != null
    }

    def "test for cond_1 with high priority alert that has a document which has a past target date, it should be picked up" () {
        setup:
            def alert = AdHocAlert.findByName('aha1')
            def doc = AlertDocument.findByChronicleId('ucb1234')
            doc.updateWithAlertId(alert.id)

            alert = AdHocAlert.findByName('aha1')
            assert alert.alertDocuments.size() == 1

            alert.alertDocuments[0].targetDate = DateTime.now().minusDays(1).toDate()
            alert.alertDocuments[0].documentType = "Validation"

        expect:
            service.cond_1(alert, DateTime.now()) != null
    }

    def "test for cond_1 non-high priority alert that has a document which will not be due in 7 days" () {
        setup:
            def alert = AdHocAlert.findByName('aha1')
            def doc = AlertDocument.findByChronicleId('ucb1234')
            doc.updateWithAlertId(alert.id)

            alert = AdHocAlert.findByName('aha1')
            assert alert.alertDocuments.size() == 1

            alert.alertDocuments[0].targetDate = DateTime.now().plusDays(8).toDate()
            alert.alertDocuments[0].documentType = "Validation"
            alert.priority = new Priority("value": 'Medium')

        expect:
            service.cond_1(alert, DateTime.now()) == null
    }

    def "test for cond_1 non-high priority alert that has a document which will be due in 7 days" () {
        setup:
            def alert = AdHocAlert.findByName('aha1')
            def doc = AlertDocument.findByChronicleId('ucb1234')
            doc.updateWithAlertId(alert.id)

            alert = AdHocAlert.findByName('aha1')
            assert alert.alertDocuments.size() == 1

            alert.alertDocuments[0].targetDate = DateTime.now().plusDays(6).toDate()
            alert.alertDocuments[0].documentType = "Validation"
            alert.priority = new Priority("value": 'Medium')

        expect:
            service.cond_1(alert, DateTime.now()) != null
    }

    def "test sending email for approved document with target date approaching"(){

        setup:
        def doc = AlertDocument.findByChronicleId('ucb1234')
        def alert = AdHocAlert.findByName('aha1')
        doc.updateWithAlertId(alert.id)

        assert alert.alertDocuments.size() == 1

        alert.alertDocuments[0].documentType = "Validation"

        def documents = []
        service.allAlerts()?.each {
            documents.add(it.alertDocuments.find{d -> d.documentType == 'Validation' && d.documentStatus != 'Approved'})
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
                "targetDate": DateTime.now().plusDays(3),
                "documentStatus": "Pending",
                "author": "Jim Jones",
                "statusDate": DateTime.now().minusDays(2),
                "documentType": "Non Validation",
                "chronicleId": "ucb1234",
                alert: AdHocAlert.get(1000L)
        ).save()

        assert AlertDocument.count() == 1
    }


}
