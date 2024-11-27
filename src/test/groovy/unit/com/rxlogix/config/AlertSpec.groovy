package com.rxlogix.config

import com.rxlogix.signal.AdHocAlert
import com.rxlogix.signal.Alert
import grails.testing.gorm.DomainUnitTest
import spock.lang.Ignore
import unit.utils.ConstraintUnitSpec

@Ignore
class AlertSpec extends ConstraintUnitSpec implements DomainUnitTest<Alert> {

    def setup() {
        new Priority(value: "High", defaultPriority: true, displayName: "High").save()
    }

    def "to test the alert persistence"() {
        setup:
        def adHocAlert = new AdHocAlert(
                productSelection: "something",
                //   detectedDate: new DateTime(2015,12, 15, 0, 0, 0).toDate(),
                name: "Test Name",
                alertVersion: 0,
                initialDataSource: "newspaper",
                topic: "test",
                priority: new Priority(value: "High", displayName: "High"),
                detectedDate: new Date(),
                detectedBy: "user"
        )
        adHocAlert.save(failOnError: true)
        expect:
        adHocAlert.id != null
    }

}
