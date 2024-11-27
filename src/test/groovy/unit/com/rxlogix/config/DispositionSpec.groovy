package com.rxlogix.config

import grails.testing.gorm.DomainUnitTest
import spock.lang.Ignore
import spock.lang.Specification


@Ignore
class DispositionSpec extends Specification implements DomainUnitTest<Disposition> {

    @Ignore
    void "test Disposition persistance" () {
        setup:

        def domainObj = new Disposition([value      : "testValue", displayName: "New Validated Signal",
                                         description: "test",
                                         display    : true, validatedConfirmed: true, notify: true, closed: true])

        when:
        domainObj.save()

        then:
        "disposition.validated.and.closed"
    }
    //test for Validated Confirmed and Closed values
    void "test Disposition persistance with both false"() {
        setup:

        def domainObj = new Disposition([value             : "testValue", displayName: "New Validated Signal",
                                         description       : "test",
                                         validatedConfirmed: false, notify: true, closed: false])

        when:
        domainObj.save()

        then:
        domainObj.closed == false
        domainObj.validatedConfirmed == false
    }
}
