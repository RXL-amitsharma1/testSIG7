package com.rxlogix.notifications

import org.joda.time.DateTime
import org.joda.time.DateTimeComparator


trait NotificationHelper {

    def _dueExactlyNDaysFromGivenTime(Date targetDate, int days, DateTime givenTime)   {
        DateTimeComparator.getDateOnlyInstance().compare(givenTime.plusDays(days),
                new DateTime(targetDate.getTime())) == 0
    }

    def _dueWithinDaysOfGivenTime(Date targetDate, int days, DateTime givenTime) {
        givenTime.plusDays(days).isAfter(new DateTime(targetDate.getTime()))
    }

    def _dueDateExceeded(Date targetDate){
        new DateTime(targetDate.getTime()).isBeforeNow()
    }

}