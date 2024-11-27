package com.rxlogix.util

import com.rxlogix.Constants
import com.rxlogix.user.User
import grails.util.Holders
import groovy.time.TimeCategory
import oracle.sql.TIMESTAMP
import org.apache.commons.lang3.time.DateUtils
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

import java.sql.Timestamp
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

public class DateUtil {
    //-----------------spotfire-------------//
    public static final DATEPICKER_UTC_FORMAT = "MM/dd/yyyy HH:mm:ss"
    public static final DATEPICKER_FORMAT_AM_PM = "dd-MMM-yyyy hh:mm:ss a"
    public static final DATEPICKER_FORMAT_AM_PM_2 = "dd-MMM-yyyy hh:mm a"
    public static final DATEPICKER_FORMAT_AM_PM_3 = "dd-MMM-yyyy hh:mm:ss a"
    public static final DATEPICKER_FORMAT_AM_PM_4 = "dd-MMM-yyyy HH:mm:ss a"
    public static final DATEPICKER_FORMAT = "dd-MMM-yyyy"
    public static final DATEPICKER_FORMAT_FILE_UPLOAD = "dd/MM/yyyy"
    public static final DATEPICKER_EMA_FORMAT = "yyyy-MMM-dd"
    public static final DATEPICKER_STANDARD_FORMAT = "yyyy/MM/dd"
    static final String DATETIME_FMT = "dd-MM-yyyy HH:mm:ss"
    static final String PRECHECK_DATETIME_FMT_AM_PM = "yyyy-MM-dd HH:mm:ss.S"

    //----------------spotfire-------//
    public static final String DEFAULT_DATE_FORMAT = "dd-MMM-yyyy"
    public static final String DEFAULT_DATE_FORMAT_WITHOUT_SEPARATOR = "ddMMMyyyy"

    public static final String MEETING_DATE_FORMAT = "MM/dd/yyyy HH:mm"

    public static final String US_DATE_FORMAT = "MM/dd/yyyy"

    public static final String DEFAULT_DATE_TIME_FORMAT = "dd-MMM-yyyy HH:mm:ss"

    public static final String START_TIME = " 00:00:00"

    public static final String END_TIME = " 11:59:59"

    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss"

    def static dtf = DateTimeFormat.forPattern('ZZ')

    static String stringFromDate(Date date, String format, String timeZone) {
        String out
        SimpleDateFormat sdf = new SimpleDateFormat(format)
        sdf.setTimeZone(TimeZone.getTimeZone(timeZone))
        try {
            out = sdf.format(date)
        } catch (ParseException e) {
            out = date?.toString()
        }
        out
    }

    static String stringFromDateInUserTimeZone(Date date, String format = DATEPICKER_FORMAT_AM_PM) {
        //This will return date in 26-Feb-2024 01:21:57 PM (GMT +05:30) format
        String out
        def userService = MiscUtil.getBean('userService')
        String timeZone = userService?.getUser()?.preference?.timeZone ?: 'UTC'
        SimpleDateFormat sdf = new SimpleDateFormat(format)
        sdf.setTimeZone(TimeZone.getTimeZone(timeZone))
        try {
            out = sdf.format(date)
        } catch (ParseException e) {
            out = date?.toString()
        }
        out + "${userService?.getGmtOffset(timeZone)}"
    }

    /**
     * The date comparison util where the seconds and minutes are discarded so that the dates are compared properly.
     * TODO: Need to find better way
     * @param date
     * @return
     */
    static getDateWithoutSeconds(Date date) {
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        instance.clear(Calendar.SECOND);
        instance.clear(Calendar.MINUTE);
        date = instance.getTime();
    }

    def static getTimezone(User currentUser) {

        String tz = currentUser?.preference?.timeZone
        return "name : ${tz},offset : ${getOffsetString(tz)} "
    }

    def static getOffsetString(timeZoneId) {
        dtf.print(DateTime.now().withZone(DateTimeZone.forID(timeZoneId)))
    }

    def static getTimezoneForRunOnce(User currentUser) {
        String tz = currentUser?.preference?.timeZone
        return """name" :"${tz}","offset" : "${getOffsetString(tz)}"""
    }

    def static getDateObj(String dateFromDatePicker, timeString) {
        def date = null
        if (dateFromDatePicker) {
            def completeDate = dateFromDatePicker + timeString
            date = new Date().parse(RelativeDateConverter.longDateFormat, completeDate)
        }
        date
    }

    // changing the end time from 11;59;59 to 00:00:00 because it doesn't  do time conversion from mysql needs a proper fix
    def static getEndDate(String dateToDatePicker, def timezone) {
        Date end = null
        SimpleDateFormat sdf = new SimpleDateFormat(RelativeDateConverter.longDateFormat)
        sdf.setTimeZone(TimeZone.getTimeZone(timezone))
        def endDate = dateToDatePicker + " 11:59:59"
        // TODO: need to check invalid date
        if (dateToDatePicker && dateToDatePicker != "Invalid date") {
            end = sdf.parse(endDate)
        }
        return end
    }

    static Date getStartDate(String dateFromDatePicker, def timezone) {
        Date start
        SimpleDateFormat sdf = new SimpleDateFormat(DATEPICKER_FORMAT)
        sdf.setLenient(false)
        sdf.setTimeZone(TimeZone.getTimeZone(timezone))
        try {
            start = sdf.parse(dateFromDatePicker)
        } catch (ParseException pe) {
            start = null
        }
        return start
    }

    static String StringFromDate(Date date, String format, String timeZone) {
        String out;
        SimpleDateFormat sdf = new SimpleDateFormat(format)
        sdf.setTimeZone(TimeZone.getTimeZone(timeZone))
        try {
            out = sdf.format(date)
        } catch (ParseException e) {
            return date?.toString();
        }
        return out;

    }

    static Date getEndDateForCalendar(String dateToDatePicker, def timezone) {
        Date end = null
        SimpleDateFormat sdf = new SimpleDateFormat(DATEPICKER_FORMAT)
        sdf.setLenient(false)
        sdf.setTimeZone(TimeZone.getTimeZone(timezone))
        try {
            use(TimeCategory) {
                end = sdf.parse(dateToDatePicker) + 1.day - 1.second
            }
        } catch (ParseException pe) {
            end = null
        }
        return end
    }

    static Date getStartDateWithMinutes(String dateFromDatePicker, def timezone) {
        Date start = null
        SimpleDateFormat sdf = new SimpleDateFormat(RelativeDateConverter.longDateFormat)
        sdf.setTimeZone(TimeZone.getTimeZone(timezone))

        // TODO: need to check invalid date
        if (dateFromDatePicker && dateFromDatePicker != "Invalid date") {
            start = sdf.parse(dateFromDatePicker)
        }
        return start
    }

    static Date getAsOfVersion(String dateToDatePicker, def timezone) {
        Date end = null
        SimpleDateFormat sdf = new SimpleDateFormat(DEFAULT_DATE_FORMAT)
        sdf.setTimeZone(TimeZone.getTimeZone(timezone))
        def endDate = dateToDatePicker + " 00:00:00"
        // TODO: need to check invalid date
        if (dateToDatePicker && dateToDatePicker != "Invalid date") {
            end = sdf.parse(endDate)
        }
        return end
    }

    static Date parseDateWithTimeZone(String dateFrom, String dateTo, String format, String timeZone) {
        Date date = null
        SimpleDateFormat sdf = new SimpleDateFormat(format)
        sdf.setTimeZone(TimeZone.getTimeZone(timeZone))
        if (dateFrom) {
            date = sdf.parse(dateFrom)
        }
        if (dateTo) {
            date = sdf.parse(dateTo)
        }
        return date
    }

    def static parseDateWithTimeZone(String date, String format, String timeZone) {
        SimpleDateFormat sdf = new SimpleDateFormat(format)
        sdf.setTimeZone(TimeZone.getTimeZone(timeZone))
        sdf.parse(date)
    }

    def static parseEndDateWithTimeZone(String dateString, String format, String timeZone) {
        SimpleDateFormat sdf = new SimpleDateFormat(format)
        sdf.setTimeZone(TimeZone.getTimeZone(timeZone))
        new Date((sdf.parse(dateString) + 1).time - 1000)
    }

    static Date parseDate(String date, String dateFormat) {

        Date dateAsString = null
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(dateFormat)
            dateAsString = sdf.parse(date)
        } catch (ParseException) {
            // Do nothing
        }

        return dateAsString

    }

    def static parseDate(String dateString) {
        List<String> formats = ["yyyy/MM/dd", "dd-mm-yyyy", "MM/dd/yyyy, DEFAULT_DATE_FORMAT"]

        DateTimeZone dtz = DateTimeZone.getDefault()
        def DateTime dt
        for (String fmt : formats) {
            DateTimeFormatter dtf = DateTimeFormat.forPattern(fmt)
            try {
                dt = dtf.parseDateTime(dateString).withZone(dtz)
            } catch (all) {
            }
        }
        dt ? dt.toDate() : null
    }

    def static stringToDate(String strDate, String format, String timeZoneId) {
        if (!strDate)
            return null

        DateTimeFormatter formatter =
                DateTimeFormat.forPattern(format).withZone(DateTimeZone.forID(timeZoneId))

        DateTime dateTime = formatter.parseDateTime(strDate)
            return dateTime.toDate()

    }

    def static displayStringToDate(String strDate) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(Constants.DateFormat.DISPLAY_DATE);
        DateTime dt = formatter.parseDateTime(strDate)
        return dt.toDate()
    }

    def static StringToDate(String strDate, String format) {

        DateTimeFormatter formatter =
                DateTimeFormat.forPattern(format).withOffsetParsed();

        DateTime dateTime = formatter.parseDateTime(strDate);
        GregorianCalendar cal = dateTime.toGregorianCalendar();

        return cal.getTime()
    }

    def static simpleDateReformat(String strDate, String fromFormat, String toFormat) {
        SimpleDateFormat from = new SimpleDateFormat(fromFormat);
        SimpleDateFormat to = new SimpleDateFormat(toFormat);
        String reformattedDate
        try {

            reformattedDate = to.format(from.parse(strDate));
        } catch (ParseException e) {
            return strDate
        }

        return reformattedDate

    }

    def static toDateStringWithoutTimezone(Date date) {
        def formatter = DateTimeFormat.forPattern(DEFAULT_DATE_FORMAT)
        if (date) {
            new DateTime(date).toString(formatter)
        } else {
            new DateTime(new Date()).toString(formatter)
        }
    }

    def static toDateStringWithoutTimezoneForAnyFormat(Date date, String format) {
        def formatter = DateTimeFormat.forPattern(format)
        if (date) {
            new DateTime(date).toString(formatter)
        } else {
            new DateTime(new Date()).toString(formatter)
        }
    }

    def static toDateString(Date date, String timezone = "UTC") {
        def formatter = DateTimeFormat.forPattern(DEFAULT_DATE_FORMAT).withZone(DateTimeZone.forID(timezone))
        if (date) {
            new DateTime(date).toString(formatter)
        } else {
            new DateTime(new Date()).toString(formatter)
        }
    }
    def static toStandardDateString(Date date, String timezone = "UTC") {
        def formatter = DateTimeFormat.forPattern(DATEPICKER_STANDARD_FORMAT).withZone(DateTimeZone.forID(timezone))
        if (date) {
            new DateTime(date).toString(formatter)
        } else {
            new DateTime(new Date()).toString(formatter)
        }
    }

    def static toDateStringWithTime(Date date, String timezone = "UTC") {
        def formatter = DateTimeFormat.forPattern(DEFAULT_DATE_TIME_FORMAT).withZone(DateTimeZone.forID(timezone))
        if (date) {
            new DateTime(date).toString(formatter)
        } else {
            new DateTime(new Date()).toString(formatter)
        }
    }
    def static String fromStringDateTimeZone(String inputDate, String timezone){
        if(inputDate) {
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(inputDate)
            return StringFromDate(date, DATEPICKER_FORMAT_AM_PM_3, timezone)
        }
    }
    def static toDateStringWithTimeInAmPmFormat(Date date, String timezone = "UTC") {
        def formatter = DateTimeFormat.forPattern(DATEPICKER_FORMAT_AM_PM_3).withZone(DateTimeZone.forID(timezone))
        if (date) {
            new DateTime(date).toString(formatter)
        } else {
            new DateTime(new Date()).toString(formatter)
        }
    }

    def static toDateStringWithTimeInAmPmFormat(User currentUser) {
        def formatter = DateTimeFormat.forPattern(DATEPICKER_FORMAT_AM_PM_3).withZone(DateTimeZone.forID(currentUser?.preference?.timeZone))
            new DateTime(new Date()).toString(formatter)
    }

    def static toUSDateString(Date date, String timezone = "UTC") {
        def formatter = DateTimeFormat.forPattern(US_DATE_FORMAT).withZone(DateTimeZone.forID(timezone))
        if (date) {
            new DateTime(date).toString(formatter)
        } else {
            new DateTime(new Date()).toString(formatter)
        }
    }
    def static fromDateToStringWithTimezone (Date date, String pattern, String timeZone = "UTC") {
        def formatter = DateTimeFormat.forPattern(pattern).withZone(DateTimeZone.forID(timeZone))
        if (date) {
            new DateTime(date).toString(formatter)
        }
    }

    def static toDateTimeString(Date date) {
        def formatter = DateTimeFormat.forPattern(DEFAULT_DATE_TIME_FORMAT)
        if (date) {
            new DateTime(date).toString(formatter)
        } else {
            new DateTime(new Date()).toString(formatter)
        }
    }

    def static toDateStringPattern(Date date, String pattern) {
        def formatter = DateTimeFormat.forPattern(pattern)
        if (date) {
            new DateTime(date).toString(formatter)
        } else {
            new DateTime(new Date()).toString(formatter)
        }

    }

    def static fromDateToString (Date date, String pattern) {
        def formatter = DateTimeFormat.forPattern(pattern)
        if (date) {
            new DateTime(date).toString(formatter)
        }
    }

    def static toDateStringEMA(Date date) {
        def formatter = DateTimeFormat.forPattern(DATEPICKER_EMA_FORMAT)
        if (date) {
            new DateTime(date).toString(formatter)
        } else {
            new DateTime(new Date()).toString(formatter)
        }
    }


    def static toDateString1(dateObj) {
        if (!dateObj)
            ""
        else {
            if (dateObj instanceof String) {
                String date1 = dateObj.substring(0, 10)
                SimpleDateFormat df = new SimpleDateFormat(DEFAULT_DATE_FORMAT)
                Date date = df.parse(date1)
                df = new SimpleDateFormat(DEFAULT_DATE_FORMAT)
                return df.format(date)
            } else if (dateObj instanceof Date) {
                if (dateObj) {
                    def theDate = dateObj as Date
                    def formatter = DateTimeFormat.forPattern(DEFAULT_DATE_FORMAT)
                    new DateTime(theDate).toString(formatter)
                } else {
                    ""
                }
            }
        }

    }

    def static getDate(String dateString, formats, timezone) {
        Date dt = null;
        for (String format : formats) {
            try {
                dt = stringToDate(dateString, format, timezone)
                break
            } catch (Exception e) {

            }
        }
        return dt
    }

    def static getCurrentTimeWithUserTimeZone() {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss")
        Date date = format.parse(DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss").print(new DateTime()))
        return date
    }

    static int getQuarter() {
        Date date = new Date()
        (date.getMonth() / 3) + 1
    }

    static Date getFirstDateOfQuarter(int quarter) {
        Date startDate
        DateTime dt = new DateTime()
        switch (quarter) {
            case 1:
                startDate = dt.withMonthOfYear(1).withDayOfMonth(1).withTimeAtStartOfDay().toDate()
                break
            case 2:
                startDate = dt.withMonthOfYear(4).withDayOfMonth(1).withTimeAtStartOfDay().toDate()
                break
            case 3:
                startDate = dt.withMonthOfYear(7).withDayOfMonth(1).withTimeAtStartOfDay().toDate()
                break
            case 4:
                startDate = dt.withMonthOfYear(10).withDayOfMonth(1).withTimeAtStartOfDay().toDate()
                break
            default:
                startDate = dt.withMonthOfYear(1).withDayOfMonth(1).withTimeAtStartOfDay().toDate()
        }
        startDate
    }

    static Date getEndDateOfQuarter(int quarter) {
        Date endDate
        DateTime dt = new DateTime()
        switch (quarter) {
            case 1:
                endDate = dt.withMonthOfYear(4).withDayOfMonth(1).withTimeAtStartOfDay().minusSeconds(1).toDate()
                break
            case 2:
                endDate = dt.withMonthOfYear(7).withDayOfMonth(1).withTimeAtStartOfDay().minusSeconds(1).toDate()
                break
            case 3:
                endDate = dt.withMonthOfYear(10).withDayOfMonth(1).withTimeAtStartOfDay().minusSeconds(1).toDate()
                break
            case 4:
                endDate = dt.plusYears(1).withMonthOfYear(1).withDayOfMonth(1).withTimeAtStartOfDay().minusSeconds(1).toDate()
                break
            default:
                endDate = dt.withMonthOfYear(4).withDayOfMonth(1).withTimeAtStartOfDay().minusSeconds(1).toDate()
        }
        endDate
    }

    static Date endOfDay(Date date) {
        return endOfGivenDate(new DateTime(date).withZone(DateTimeZone.forID(Holders.config.server.timezone))).toDate()
    }

    static DateTime endOfGivenDate(DateTime dateTime) {
        return dateTime.millisOfDay().withMaximumValue()
    }

    static String getDateStringFromOracleTimestamp(TIMESTAMP timestamp, String timeZone) {
        Date date
        if (timestamp) {
            date = Date.from(timestamp.timestampValue().toLocalDateTime().atZone(ZoneId.systemDefault()).toInstant())
            return toDateStringWithTime(date, timeZone)
        }
        return Constants.Commons.DASH_STRING
    }

    static String getAmPmDateFromOracleTimestamp(TIMESTAMP timestamp, String timeZone) {
        Date date
        if (timestamp) {
            date = Date.from(timestamp.timestampValue().toLocalDateTime().atZone(ZoneId.systemDefault()).toInstant())
            return new SimpleDateFormat(DateUtil.DATEPICKER_FORMAT_AM_PM).format(new Date(DateUtil.toDateStringWithTime(date, timeZone))).toString()
        }
        return Constants.Commons.DASH_STRING
    }

    static String getDateStringFromTimestampWithoutTimezone(Timestamp timestamp) {

        Date date
        if (timestamp) {
            date = Date.from(timestamp.toLocalDateTime().atZone(ZoneId.systemDefault()).toInstant())
            return toDateStringWithoutTimezone(DateUtils.truncate(date, Calendar.DAY_OF_MONTH))
        }
        return Constants.Commons.DASH_STRING
    }

    static LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }


    static String getLongDateFormatForLocale(Locale locale, boolean withTimeZone = false) {
        String format
        try {
            def messageSource = MiscUtil.getBean("messageSource")
            format = messageSource.getMessage(withTimeZone? "default.date.format.long.tz" : "default.date.format.long", null, locale)
        }
        catch (Exception e) {
            format = DATEPICKER_FORMAT_AM_PM
        }
        return format
    }

    def static toDateStringWithTimeInAmFormat(Date date, String timezone = "UTC") {
        def formatter = DateTimeFormat.forPattern(DATEPICKER_FORMAT_AM_PM).withZone(DateTimeZone.forID(timezone))
        if (date) {
            new DateTime(date).toString(formatter)
        } else {
            new DateTime(new Date()).toString(formatter)
        }
    }
    def static toDateStringExportWithTimeInAmFormat(Date date, String timezone = "UTC") {
        def formatter = DateTimeFormat.forPattern(DATEPICKER_FORMAT_AM_PM).withZone(DateTimeZone.forID(timezone))
        String strDate
        if (date) {
            strDate = new DateTime(date).toString(formatter)
            return stringToDate(strDate, DATEPICKER_FORMAT_AM_PM, "UTC")
        }
        return null
    }

    static String getLongDateStringForTimeZone(Date date, String timeZone, Boolean showTimeZone = false) {
        if (!date) return ""
        SimpleDateFormat sdf = new SimpleDateFormat(DATEPICKER_FORMAT_AM_PM)
        sdf.setTimeZone(TimeZone.getTimeZone(timeZone))
        return sdf.format(date) + (showTimeZone ? " " + timeZone : "")
    }

    static Boolean matchDate(Date date1, Date date2) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd")
        return fmt.format(date1).equals(fmt.format(date2))
    }

    static int getDifferenceDays(Date d1, Date d2) {
        LocalDateTime localDateTime1 = convertToLocalDateTimeViaSqlTimestamp(d1)
        LocalDateTime localDateTime2 = convertToLocalDateTimeViaSqlTimestamp(d2)

        int diff = (int) localDateTime1.until(localDateTime2, ChronoUnit.DAYS)

        return diff
    }

    static LocalDateTime convertToLocalDateTimeViaSqlTimestamp(Date dateToConvert) {
        return new java.sql.Timestamp(dateToConvert.getTime()).toLocalDateTime()
    }

    static long getCustomDifferenceBetweenDates(Date date1, Date date2, String unit = null) {
        long difference = Math.abs(date2.getTime() - date1.getTime())
        if (unit == Constants.TimeUnits.Hours || unit == Constants.TimeUnits.Minutes || unit == Constants.TimeUnits.Seconds) {
            difference = (difference / (60 * 60 * 1000)) % 24
            if (unit == Constants.TimeUnits.Minutes || unit == Constants.TimeUnits.Seconds) {
                difference = (difference / (60 * 1000)) % 60
                if (unit == Constants.TimeUnits.Seconds) {
                    difference = (difference / 1000) % 60
                }
            }
        }
        return difference
    }

    static String convertDateStringToFormat(String date, String fromFormat, String toFormat){
        SimpleDateFormat format1 = new SimpleDateFormat(fromFormat)
        SimpleDateFormat format2 = new SimpleDateFormat(toFormat)
        Date date1 = format1.parse(date)
        return format2.format(date1)
    }

    /**
     * This method is used to check if date has valid year or not and returns true or false
     * @param date
     * @return
     */
    static boolean checkValidDateYear(Date date) {
        boolean isValid = true
        if (null != date) {
            LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            isValid = localDate.getYear() != 0
        }
        return isValid
    }

    public static Date parseDate(String dateString, String format, String timezone) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format)
        dateFormat.setTimeZone(TimeZone.getTimeZone(timezone))
        return dateFormat.parse(dateString)
    }

}
