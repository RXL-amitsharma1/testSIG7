package com.rxlogix

import com.rxlogix.signal.AdHocAlert
import grails.testing.gorm.DomainUnitTest
import spock.lang.Ignore
import spock.lang.Specification
import com.rxlogix.config.AlertDocument
import org.joda.time.DateTime

import com.rxlogix.config.Priority

@Ignore
class AlertDocumentSpec extends Specification implements DomainUnitTest<AlertDocument> {

    def setup() {}

    void "test for AlertDocument persistence"() {

        setup:
        def adHocAlert = new AdHocAlert(id: 1L,
                productSelection: "something",
                detectedDate: new DateTime(2015,12, 15, 0, 0, 0).toDate(),
                name: "Test Name",
                alertVersion: 0,
                priority: new Priority(value: "High"),
        )

        when:

        def alertDocument = new AlertDocument([documentType  : "Validation",
                                               documentLink: "http://gmail.com",
                                               startDate     : new DateTime(2015, 12, 15, 0, 0, 0).toDate(),
                                               targetDate    : null,
                                               statusDate    : new DateTime(2015, 12, 15, 0, 0, 0).toDate(),
                                               documentStatus: "", author: "Chetan", comments: "Test comments",
                                               chronicleId   : "ucb1234",
                                               author        : "test author",
                                               documentStatus: 'completed'

        ])
        alertDocument.alert = adHocAlert
        alertDocument.save()

        then:
        alertDocument.id != null
    }

    void "test for AlertDocument persistence with validation error"() {

        setup:
        def adHocAlert = new AdHocAlert(id: 1L,
                productSelection: "something",
                detectedDate: new DateTime(2015,12, 15, 0, 0, 0).toDate(),
                name: "Test Name",
                alertVersion: 0,
                priority: new Priority(value: "High"),
        )

        when:

        def alertDocument = new AlertDocument([documentType:"Validation",documentLink:"http://gmail.com",
            startDate: null, targetDate: null, statusDate: null, documentStatus:"", author:"Chetan",comments:"Test comments"
        ])
        alertDocument.alert = adHocAlert
        alertDocument.save(failOnError: false)

        then:
        alertDocument.id == null
    }

    //TODO the use case is not covered enough for this test
    def "test for updateWithAlertId" () {
        setup:
            def adHocAlert = new AdHocAlert(
                    productSelection: "something",
                    detectedDate: new DateTime(2015,12, 15, 0, 0, 0).toDate(),
                    name: "Test Name",
                    detectedBy: "Company",
                    topic: "rash",
                    initialDataSource: "Test",
                    alertVersion: 0,
                    priority: new Priority(value: "High"),
            )

            adHocAlert.save();
            assert AdHocAlert.count == 1

            def alertDoc = new AlertDocument([documentType  : "Validation",
                                                   documentLink: "http://gmail.com",
                                                   startDate     : new DateTime(2015, 12, 15, 0, 0, 0).toDate(),
                                                   targetDate    : null,
                                                   statusDate    : new DateTime(2015, 12, 15, 0, 0, 0).toDate(),
                                                   documentStatus: "", author: "Chetan", comments: "Test comments",
                                                   chronicleId   : "ch-1",
                                                   author        : "test author",
                                                   documentStatus: 'completed'

            ])
            alertDoc.save()

            AlertDocument.findByChronicleId('ch-1').updateWithAlertId(adHocAlert.id)
            alertDoc = AlertDocument.findByChronicleId('ch-1')

        expect:
            alertDoc.alertId == adHocAlert.id
    }
}