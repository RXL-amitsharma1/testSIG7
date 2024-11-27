package com.rxlogix.util

import com.rxlogix.user.User
import groovy.time.TimeCategory

import java.text.SimpleDateFormat

class RelativeDateConverter {
    public static final longDateFormat = "yyyy-MM-dd HH:mm:ss"
    public static final reportDateFormat = "dd-MMM-yyyy"

    static yesterday(Date nextRunDate, String selectedTimeZone) {
        // prevent nulls
        if (!nextRunDate) {
            nextRunDate = new Date()
        }
        if (!selectedTimeZone) {
            selectedTimeZone = TimeZone.getDefault().ID
        }
        nextRunDate = parseNextRunDate(nextRunDate, selectedTimeZone)
        use(TimeCategory) {
            Date start = nextRunDate - 1.day
            Date end = start + 1.day - 1.second
            return [start, end]
        }
    }

   static Date getNextDate(){
       Date date = new Date()
       Date nextDay
       use(TimeCategory) {
           nextDay = date + 1.day - 1.second
       }
       return nextDay
   }

    static Date parseNextRunDate(Date date, String s) {
        Calendar cal = Calendar.getInstance()
        cal.setTime(date)
        SimpleDateFormat sdf = new SimpleDateFormat(longDateFormat)
        sdf.setTimeZone(TimeZone.getTimeZone(cal.getTimeZone().ID))
        String dateValue = sdf.format(date) // gives string date original value
        def val = dateValue.tokenize(" ")
        String finalValue = val[0] + " 00:00:00"
        return sdf.parse(finalValue)
    }

    static lastXWeeks(Date nextRunDate, int numOfWeeks, String selectedTimeZone) {
        // prevent nulls
        if (!nextRunDate) {
            nextRunDate = new Date()
        }
        if (!selectedTimeZone) {
            selectedTimeZone = TimeZone.getDefault().ID
        }
        if (numOfWeeks < 1) {
            return null
        }
        nextRunDate = parseNextRunDate(nextRunDate, selectedTimeZone)

        use(TimeCategory) {

            def startDate, endDate

            //Calculate the last week.
            def lastWeek = nextRunDate - (numOfWeeks).week

            Calendar calendar = Calendar.getInstance()
            calendar.setTime(lastWeek)
            calendar.set(Calendar.DAY_OF_WEEK, 1) //Set the sunday as the start of the week.
            startDate = calendar.time

            //Calculate the end week date
            def endWeekDate = startDate + (numOfWeeks - 1).week

            //Now set the end week time
            calendar.setTime(endWeekDate)
            calendar.set(Calendar.DAY_OF_WEEK, 7) //Set the sunday as the start of the week.
            setTimeOnCalendar(calendar) //Set hour as 23, minutes as 59 and seconds as 59

            endDate = calendar.time

            return [startDate, endDate]
        }
    }

    /**
     * Method to set the time on the calendar.
     * It sets the hour as 23, minute as 59 and seconds as 59.
     * @param calendar
     * @return
     */
    static setTimeOnCalendar(calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
    }

    static lastXMonths(Date nextRunDate, int numOfMonths, String selectedTimeZone) {
        // prevent nulls
        if (!nextRunDate) {
            nextRunDate = new Date()
        }
        if (numOfMonths < 1) {
            return null
        }
        if (!selectedTimeZone) {
            selectedTimeZone = TimeZone.getDefault().ID
        }
        nextRunDate = parseNextRunDate(nextRunDate, selectedTimeZone)

        use(TimeCategory) {

            def lastMonth = nextRunDate - (numOfMonths).months

            Calendar cal = Calendar.getInstance()
            cal.setTime(lastMonth)
            cal.set(Calendar.DATE, 1)//Set the first date of the month.

            def startDate = cal.time

            def endDateMonth = startDate + (numOfMonths - 1).month

            cal.setTime(endDateMonth)
            cal.set(Calendar.DATE, cal.getActualMaximum(Calendar.DATE)) //Set the last date of the month.

            setTimeOnCalendar(cal) //Set hour as 23, minutes as 59 and seconds as 59

            def endDate = cal.time

            return [startDate, endDate]
        }
    }

    static lastXDays(Date nextRunDate, int numOfDays, String selectedTimeZone) {

        // prevent nulls
        if (!nextRunDate) {
            nextRunDate = new Date()
        }
        if (numOfDays < 1) {
            return null
        }
        if (!selectedTimeZone) {
            selectedTimeZone = TimeZone.getDefault().ID
        }

        nextRunDate = parseNextRunDate(nextRunDate, selectedTimeZone)

        use(TimeCategory) {

            Date start = (nextRunDate - (numOfDays).day)

            Calendar cal = Calendar.getInstance();
            cal.setTime(start);
            def startDate = cal.time

            //One day will be removed from the calculated date as we don't want to include the current day.
            Date end = start + numOfDays.day - 1.second
            cal.setTime(end)
            setTimeOnCalendar(cal) //Set hour as 23, minutes as 59 and seconds as 59
            def endDate = cal.time

            return [startDate, endDate]
        }
    }

    static lastXDaysDates(Date nextRunDate, int numOfDays, String selectedTimeZone) {

        // prevent nulls
        if (!nextRunDate) {
            nextRunDate = new Date()
        }
        if (numOfDays < 1) {
            return null
        }
        if (!selectedTimeZone) {
            selectedTimeZone = TimeZone.getDefault().ID
        }

        nextRunDate = parseNextRunDate(nextRunDate, selectedTimeZone)

        use(TimeCategory) {

            Date start = (nextRunDate - (numOfDays).day)

            Calendar cal = Calendar.getInstance();
            cal.setTime(start);
            def startDate = cal.time

            //One day will be removed from the calculated date as we don't want to include the current day.
            Date end = start + numOfDays.day - 1.second
            cal.setTime(end)
            setTimeOnCalendar(cal) //Set hour as 23, minutes as 59 and seconds as 59
            def endDate = cal.time

            return [startDate, endDate]
        }
    }

    static lastXYears(Date nextRunDate, int numOfYears, String selectedTimeZone) {
        // prevent nulls
        if (!nextRunDate) {
            nextRunDate = new Date()
        }
        if (numOfYears < 1) {
            return null
        }
        if (!selectedTimeZone) {
            selectedTimeZone = TimeZone.getDefault().ID
        }
        nextRunDate = parseNextRunDate(nextRunDate, selectedTimeZone)

        use(TimeCategory) {

            def lastYear = nextRunDate - (numOfYears).years

            Calendar cal = Calendar.getInstance();
            cal.setTime(lastYear);
            cal.set(Calendar.MONTH, 0)
            cal.set(Calendar.DAY_OF_MONTH, 1)

            def startDate = cal.time

            def endDate = startDate + (numOfYears - 1).years

            cal.setTime(endDate)
            cal.set(Calendar.MONTH, 11)
            cal.set(Calendar.DAY_OF_MONTH, 31)
            setTimeOnCalendar(cal) //Set hour as 23, minutes as 59 and seconds as 59

            endDate = cal.time

            return [startDate, endDate]
        }
    }

    static findDay(Date nextRunDate, String selectedTimeZone, def timeZoneNotRequired = null) {
        if (!nextRunDate) {
            nextRunDate = new Date()
        }
        if (!selectedTimeZone) {
            selectedTimeZone = TimeZone.getDefault().ID
        }

        if (!timeZoneNotRequired) {
            nextRunDate = parseNextRunDate(nextRunDate, selectedTimeZone)
        }

        use(TimeCategory) {
            Date start = nextRunDate
            Date end = start + 1.day - 1.second
            return [start, end]
        }
    }

    static def getDaysDifference(Date nextRunDate, Date selectedDate) {
        if (!nextRunDate) {
            nextRunDate = new Date()
        }
        if (!selectedDate) {
            selectedDate = new Date()
        }
        use(TimeCategory) {
            return (nextRunDate - selectedDate).days

        }
    }

    static Date getUpdatedDate(Date previousDate, int delta) {
        if (!previousDate) {
            previousDate = new Date()
        }
        if (!delta) {
            delta = 0
        }
        use(TimeCategory) {
            return (previousDate + delta.day)
        }
    }

    static Date calculateLatestVersion() {
        use(TimeCategory) {
            return (new Date() + 1.year)
        }
    }

    static getDateRangeString(Date date, String tz) {
        SimpleDateFormat sdf = new SimpleDateFormat(reportDateFormat)
        sdf.setTimeZone(TimeZone.getTimeZone(tz))
        String dateValue = sdf.format(date) // gives string date original value
        def val = dateValue.tokenize(" ")
        return dateValue
    }

    static getCurrentTimeWRTTimeZone(User user) {
        SimpleDateFormat sdf = new SimpleDateFormat(longDateFormat)
        sdf.setTimeZone(TimeZone.getTimeZone(user.preference.getTimeZone()))
        String dateValue = sdf.format(new Date()) // gives string date original value
        return dateValue.toString()
    }

    static getCurrentTimeInMilliSeconds(User user) {
        SimpleDateFormat sdf = new SimpleDateFormat(longDateFormat)
        sdf.setTimeZone(TimeZone.getTimeZone(user.preference.getTimeZone()))
        String dateValue = sdf.format(new Date())
        return sdf.parse(dateValue).getTime()
    }
}