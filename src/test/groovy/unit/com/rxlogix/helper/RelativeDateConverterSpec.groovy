package com.rxlogix.helper

import com.rxlogix.util.RelativeDateConverter
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

class RelativeDateConverterSpec extends Specification {
    public static final String TEST_TZ = "UTC"
    public static final TimeZone ORIGINAL_TZ = TimeZone.getDefault()

    def setupSpec() {
        // force the tests to run in the TEST_TZ
        TimeZone.setDefault(TimeZone.getTimeZone(TEST_TZ))
    }

    def cleanupSpec() {
        // set the TZ back to what it was
        TimeZone.setDefault(ORIGINAL_TZ)
    }

    @Unroll("convert null date with #methodName ")
    void "test null as parameter "() {
        given: "A null next Run Date"
        when:
        "we call ${methodName} and pass in a null nextRunDate we are supposed to get valid start time and end time  "
        def result = RelativeDateConverter.newInstance().invokeMethod(methodName, params)
        then: "We have some values in result" // not going to focus on their content here rather the sanity check of null param
        result.size() == 2
        result.get(0) != null
        result.get(0).class == Date.class
        result.get(1) != null
        result.get(1).class == Date.class
        where:
        methodName    | params
        "yesterday"   | [null, "UTC"]
        "lastXWeeks"  | [null, 4, "UTC"]
        "lastXMonths" | [null, 5, "UTC"]
        "lastXYears"  | [null, 7, "UTC"]
        "lastXDays"   | [null, 3, "UTC"]
    }

    @Unroll("convert null date with #methodName ")
    void "test for more Inputs parameter "() {
        given: "A next Run date"
        def nextRunDate = new Date("Thu Nov 20 14:19:22 $TEST_TZ 2014")
        when:
        "we call ${methodName} and pass next run date and pass negative parameter or zero we are supose to return null because 0 or negative numbers are not valid for dates "
        def result = RelativeDateConverter.newInstance().invokeMethod(methodName, params)
        then: "We have Results as "
        result == expected
        where:
        methodName    | params                                                     | expected
        "yesterday"   | [new Date("Thu Nov 20 14:19:22 $TEST_TZ 2014"), "UTC"]     | [new Date("Wed Nov 19 00:00:00 $TEST_TZ 2014"), new Date("Wed Nov 19 23:59:59 $TEST_TZ 2014")]
        "lastXWeeks"  | [new Date("Thu Nov 20 14:19:22 $TEST_TZ 2014"), 0, "UTC"]  | null
        "lastXMonths" | [new Date("Thu Nov 20 14:19:22 $TEST_TZ 2014"), 12, "UTC"] | [new Date("Fri Nov 01 00:00:00 $TEST_TZ 2013"), new Date("Fri Oct 31 23:59:59 $TEST_TZ 2014")]
        "lastXYears"  | [new Date("Thu Nov 20 14:19:22 $TEST_TZ 2014"), 7, "UTC"]  | [new Date("Mon Jan 01 00:00:00 $TEST_TZ 2007"), new Date("Tue Dec 31 23:59:59 $TEST_TZ 2013")]
        "lastXDays"   | [new Date("Thu Nov 20 14:19:22 $TEST_TZ 2014"), 99, "UTC"] | [new Date("Thu Aug 13 00:00:00 $TEST_TZ 2014"), new Date("Wed Nov 19 23:59:59 $TEST_TZ 2014")]
        "lastXDays"   | [new Date("Thu Nov 20 14:19:22 $TEST_TZ 2014"), -1, "UTC"] | null
    }


    void "test for Yesterday "() {
        given: "A next Run Date"
        Date nxtRunDate = new Date("Thu Nov 20 14:19:22 $TEST_TZ 2014")
        when: "we call yesterday and pass in nextRunDate we are supposed to get yesterday start time and end time  "
        def result = RelativeDateConverter.yesterday(nxtRunDate, "UTC")
        then: "We set the value in result"
        result == [new Date("Wed Nov 19 00:00:00 $TEST_TZ 2014"), new Date("Wed Nov 19 23:59:59 $TEST_TZ 2014")]
    }

    void "test for lastXWeeks "() {
        given: "A next Run Date"
        def dateConverter = new RelativeDateConverter()
        Date nxtRunDate = new Date("Mon Nov 24 09:40:38 $TEST_TZ 2014")
        when: "we call lastXWeek and pass in nextRunDate we are supposed to get x weeks earlier start time and end time  "
        def result = dateConverter.lastXWeeks(nxtRunDate, 2, "UTC")
        then: "We set the value result"
        result == [new Date("Sun Nov 09 00:00:00 $TEST_TZ 2014"), new Date("Sat Nov 22 23:59:59 $TEST_TZ 2014")]
    }

    void "test for lastXMonths "() {
        given: "A next Run Date"
        Date nxtRunDate = new Date("Mon Nov 24 09:40:38 $TEST_TZ 2014")
        when: "we call lastXMonths and pass in nextRunDate we are supposed to get x months earlier start time and end time  "
        def result = RelativeDateConverter.lastXMonths(nxtRunDate, 3, "UTC")
        then: "We set the value in result"
        result == [new Date("Fri Aug 01 00:00:00 $TEST_TZ 2014"), new Date("Fri Oct 31 23:59:59 $TEST_TZ 2014")]

    }

    void "test for lastXYears "() {
        given: "A next Run Date"
        Date nxtRunDate = new Date("Mon Nov 24 09:40:38 $TEST_TZ 2014")
        when: "we call lastXYears and pass in nextRunDate we are supposed to get x years earlier start time and end time  "
        def result = RelativeDateConverter.lastXYears(nxtRunDate, 9, "UTC")
        then: "We set the value in result"
        result == [new Date("Sat Jan 01 00:00:00 $TEST_TZ 2005"), new Date("Tue Dec 31 23:59:59 $TEST_TZ 2013")]

    }

    void "test for lastXDays "() {
        given: "A next Run Date"
        Date nxtRunDate = new Date("Mon Nov 24 09:40:38 ${TEST_TZ} 2014")
        when: "we call lastXDays and pass in nextRunDate we are supposed to get x days earlier start time and end time  "
        def result = RelativeDateConverter.lastXDays(nxtRunDate, 2, "UTC")
        then: "We set the value in result"
        result == [new Date("Sat Nov 22 00:00:00 UTC 2014"), new Date("Sun Nov 23 23:59:59 UTC 2014")]

    }
    void "test for getting number Of Days difference " () {
        given: "A next Run Date and a selected date"
        Date nxtRunDate = new Date("sun Feb 26 02:29:38 UTC 2015")
        Date selectedDate = new Date("sun Feb 01 00:29:38 UTC 2015")
        when: "we call lastXDays and pass in nextRunDate we are supposed to get x days earlier start time and end time  "
        def result = RelativeDateConverter.getDaysDifference(nxtRunDate,selectedDate)
        then: "We set the value in result"
        result == 25
    }

    void "test for getting number Of Days difference when selected Date is null" () {
        given: "A next Run Date and a selected date"
        Date nxtRunDate = new Date("sun Feb 26 02:29:38 UTC 2015")
        when: "we call lastXDays and pass in nextRunDate we are supposed to get x days earlier start time and end time  "
        def resultForNull = RelativeDateConverter.getDaysDifference(nxtRunDate,null)
        def resultForToday = RelativeDateConverter.getDaysDifference(nxtRunDate,new Date())
        then: "We set the value in result"
        assert(resultForNull == resultForToday)
    }

    void "test for getting date with Delta" () {
        given: "A next Run Date and a delta which is no of days"
        Date nxtRunDate = new Date("Fri Feb 05 02:30:00 UTC 2016")
        int delta = 1
        when: "Next Run Date + delta equals what will be the new Next Run Date"
        def result = RelativeDateConverter.getUpdatedDate(nxtRunDate,delta)
        then: "We use that value to then set asOfVersionDate, dateRangeAbsoluteStart and dateRangeAbsoluteEnd"
        assert(result == new Date("Sat Feb 06 02:30:00 UTC 2016"))
    }

    void "test for getting date with Delta when delta is 0 or null" () {
        given: "A next Run Date and a delta which is no of days"
        Date nxtRunDate = new Date("sun Feb 26 02:29:38 UTC 2015")

        when: "we call lastXDays and pass in nextRunDate we are supposed to get x days earlier start time and end time  "
        def result = RelativeDateConverter.getUpdatedDate(nxtRunDate,0)
        then: "We set the value in result"
        assert(result == nxtRunDate)
    }

    void "test for calculating latest Case version" () {
        given: "We need future date a year apart"
        Date nxtRunDate = new Date()
        when: "we call calculate latestVersion "
        def result = RelativeDateConverter.calculateLatestVersion()
        then: "We set the value in result"
        assert(result>=nxtRunDate)
    }
}
