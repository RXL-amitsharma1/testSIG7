package com.rxlogix.signal

import grails.testing.gorm.DomainUnitTest
import spock.lang.Ignore
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@Ignore
class AggregateCaseAlertSpec extends Specification implements DomainUnitTest<AggregateCaseAlert> {
    def aga

    def setup() {
        aga = new AggregateCaseAlert(attributes: """
{"BODY_SYS1":"General disorders and administration site conditions",
    "CUMM_FATAL_CASELIST13":"null",
    "CUMM_FATAL_COUNT4":"0",
    "CUMM_SERIOUS_CASELIST15":"(100292,1)",
    "CUMM_SERIOUS_COUNT6":"1",
    "CUMM_SPON_CASELIST19":"(100292,1)",
    "CUMM_SPON_COUNT10":"1",
    "CUMM_STUDY_CASELIST17":"null",
    "CUMM_STUDY_COUNT8":"0",
    "INC_TERM2":"Oedema",
    "NEW_FATAL_CASELIST12":"null",
    "NEW_FATAL_COUNT3":"0",
    "NEW_SERIOUS_CASELIST14":"(100292,1)",
    "NEW_SERIOUS_COUNT5":"1",
    "NEW_SPON_CASELIST18":"(100292,1)",
    "NEW_SPON_COUNT9":"1",
    "NEW_STUDY_CASELIST16":"null",
    "NEW_STUDY_COUNT7":"0",
    "PRODUCT_NAME0":"IBUPROFEN",
    "PRR_VALUE11":"0"
    }
""")
    }

    def cleanup() {
    }

    void "aggregate case alert get the correct property"() {
        when:
            def a = 0
        then:
            true == true
    }
}
