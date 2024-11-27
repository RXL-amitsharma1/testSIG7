package com.rxlogix.util

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Specification

/**
 * Created by Lei Gao on 7/17/15.
 */
class DateUtilSpec extends Specification {
    def setup() {
    }

    def "test stringToDate"() {
        when:
            def theDate = DateUtil.stringToDate("2015-01-05", "yyyy-MM-DD", 'America/Los_Angeles')
        then:
            theDate == new DateTime(2015, 1, 5, 0, 0, DateTimeZone.forID('America/Los_Angeles')).toDate()
    }

    def "test stringToDate for UTC"() {
        when:
            def theDate = DateUtil.stringToDate("2015-01-05", "yyyy-MM-DD", 'America/Los_Angeles')
        then:
            def theNewDate = new DateTime(2015, 1, 5, 8, 0, DateTimeZone.forID('UTC')).toDate()
            theDate ==  theNewDate
    }

    def "test toDateString" () {
        when:
            def dt = new DateTime(2015, 1, 5, 0, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles'))
        then:
            DateUtil.toDateString(dt.toDate(), "America/Los_Angeles") == "05-Jan-2015"
    }

    def "test toDateString format in UTC time" () {
        when:
            def dt = new DateTime(2015, 1, 5, 21, 0, 0, 0, DateTimeZone.forID('America/Los_Angeles'))
        then:
            DateUtil.toDateString(dt.toDate(), "UTC") == "06-Jan-2015"
    }
}
