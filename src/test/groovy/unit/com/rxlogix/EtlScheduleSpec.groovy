package com.rxlogix

import com.rxlogix.config.EtlSchedule
import grails.testing.gorm.DomainUnitTest
import spock.lang.Ignore
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@Ignore
class EtlScheduleSpec extends Specification implements DomainUnitTest<EtlSchedule> {

    def setup() {
    }

    def cleanup() {
    }

    void "test create schedule"() {
        given:
        def eTLSchedule = new EtlSchedule(scheduleName: "ETL", startDateTime: "20115-03-31T03:23+02:00", repeatInterval: "FREQ=MONTHLY;INTERVAL=6;BYDAY=WE;BYSETPOS=3;UNTIL=20140919;", createdBy: "bootstrap", modifiedBy: "bootstrap")
        eTLSchedule.save();

        when:
        def eTLScheduleInstance = EtlSchedule.findByScheduleName("ETL")

        then:
        eTLScheduleInstance.scheduleName.equals("ETL")
        eTLScheduleInstance.id == 1
    }
}
