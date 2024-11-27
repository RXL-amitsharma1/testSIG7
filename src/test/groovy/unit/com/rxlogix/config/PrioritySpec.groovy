package com.rxlogix.config

import grails.testing.gorm.DomainUnitTest
import spock.lang.Ignore
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@Ignore
class PrioritySpec extends Specification implements DomainUnitTest<Priority> {

    void "test the priority persistance."() {
        setup:
        //Prepare the priority instance.
        def priorityInstance =
                new Priority([displayName : "testPriority", value : "testPriority", display :  true, defaultPriority : true, reviewPeriod :1])

        when:
        priorityInstance.save()

        then:
        priorityInstance.id != null
        priorityInstance.displayName == "testPriority"
        priorityInstance.value == "testPriority"
        priorityInstance.display == true
        priorityInstance.defaultPriority == true
        priorityInstance.reviewPeriod == 1

    }

    void "test the priority dynamic finder queries."() {
        setup:
        //Prepare the priority instance.
        def priorityInstance =
                new Priority([displayName : "testPriority", value : "testPriority", display :  true, defaultPriority : true, reviewPeriod :1])

        when:
        priorityInstance.save()

        then:
        Priority.findByValue("testPriority") != null
        Priority.findByDisplayName("testPriority") != null
        Priority.findByDefaultPriority(true) != null
        Priority.findByDefaultPriority(false) == null
    }

    def "test the priority reviewPeriod constraint"() {
        when:
        //Prepare the priority instance.
        def priorityInstance = new Priority()
        priorityInstance.validate()

        then:
        priorityInstance.hasErrors() != null
    }
}
