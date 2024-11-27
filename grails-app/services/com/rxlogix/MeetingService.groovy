package com.rxlogix

import com.rxlogix.config.Action
import com.rxlogix.config.ActivityType
import com.rxlogix.config.ActivityTypeValue
import com.rxlogix.config.Attachments
import com.rxlogix.config.GuestAttendee
import com.rxlogix.config.Meeting
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.enums.ActionStatus
import com.rxlogix.enums.MeetingStatus
import com.rxlogix.signal.Topic
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.MeetingAttendee
import grails.converters.JSON
import grails.util.Holders
import grails.web.servlet.mvc.GrailsParameterMap
import net.fortuna.ical4j.data.CalendarOutputter
import net.fortuna.ical4j.model.*
import net.fortuna.ical4j.model.component.VEvent
import net.fortuna.ical4j.model.component.VTimeZone
import net.fortuna.ical4j.model.parameter.Cn
import net.fortuna.ical4j.model.parameter.Role
import net.fortuna.ical4j.model.parameter.Rsvp
import net.fortuna.ical4j.model.property.*
import net.fortuna.ical4j.util.UidGenerator
import org.grails.web.json.JSONObject
import org.joda.time.LocalDateTime

import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class MeetingService {

    def userService
    def grailsApplication
    def emailService
    def CRUDService
    def messageSource
    def activityService
    EmailNotificationService emailNotificationService

    public final static String JSON_DATE = "yyyy-MM-dd'T'HH:mmXXX"

    List applyRecurrenceLimit(def meetings) {
        Map map = meetings.groupBy({ meeting -> meeting.linkingId })
        List newList = []
        map.each { key, value ->
            if (key) {
                int limit = Holders.config.meeting.showRecurrenceMeetingLimit
                newList << value.findAll { it.dateOfMeeting < new LocalDateTime().plusMonths(limit).toDate() }
            } else {
                newList << value
            }
        }
        newList.flatten()
    }

    List checkIfMeetingDatePassed(def meetings) {
        meetings = meetings.collect { Map map ->
            map['isMeetingEditable'] = isMeetingEditable(map)
            map
        }
        meetings
    }

    Boolean isMeetingEditable(Map map) {
        (map.meetingStatus == MeetingStatus.CANCELLED.toString()) ? false : isMeetingDateNotPassed(map.dateOfMeeting)
    }

    List<Date> calculateDates(JSONObject jsonObject) {
        log.info("Time object : ${jsonObject}")
        List nextRunDateList = []
        Date nextRunDate = calculateNextDate(jsonObject)
        while (nextRunDate) {
            nextRunDateList << nextRunDate
            nextRunDate = calculateNextDate(jsonObject, nextRunDate)
        }
        log.info("nextRunDateList is :" + nextRunDateList)
        nextRunDateList
    }

    JSONObject generateJSONObjectFromString(String jsonString) {
        JSON.parse(jsonString) as JSONObject
    }

    Date calculateNextDate(JSONObject timeObject, Date lastRunDate = null) {
        Date nextRunDate = null
        int limit = Holders.config.meeting.recurrenceMeetingLimit
        Date dateAfterSomeMonth = new LocalDateTime().plusMonths(limit).toDate()
        if (timeObject.startDateTime && timeObject.timeZone && timeObject.recurrencePattern) {
            Date now = new Date()
            Date inputDate = Date.parse(JSON_DATE, timeObject.startDateTime)
            TimeZone utcTimeZone = TimeZone.getTimeZone("UTC")
            TimeZone userTimeZone = TimeZone.getTimeZone(timeObject.timeZone.name)
//            TimeZone userTimeZone = TimeZone.getTimeZone("Asia/Kolkata")
            long newMilliSeconds = inputDate.time - utcTimeZone.rawOffset
            Date startDate = new Date(newMilliSeconds)
            DateTime from = new DateTime(startDate)
            DateTime to = new DateTime(dateAfterSomeMonth)

            RRule recurRule = new RRule(timeObject.recurrencePattern)

            if (!recurRule.recur.until && !recurRule.recur.count) {
                recurRule.recur.setUntil(new net.fortuna.ical4j.model.Date(dateAfterSomeMonth))
            }

            //Check if the scheduler will never end
            if (recurRule.recur.count == -1 && !recurRule.recur.until) {
                //We temporarily set the recurrence count to 2 because we only need the next recur date
                recurRule.recur.setCount(2)
                if (lastRunDate) {
                    from = new DateTime(lastRunDate)
                }
            }

            //Check if the recurrence is run once/now
            if (recurRule?.recur?.count == 1 && startDate.before(now)) {
                //Do not return a nextRunDate if we have already run this configuration once
                if (lastRunDate) {
                    return null
                }

                //Run once anytime in the past is generated with today's date
                from = new DateTime(startDate)
            }

            VEvent event = new VEvent(from, "event")
            event.getProperties().add(recurRule)
            Period period = new Period(from, to)
            PeriodList periodList = event.calculateRecurrenceSet(period)

            //Check if the start date matches recurrence pattern
            boolean excludeStartDate = checkStartDate(startDate, recurRule)

            def futureRunDates = []
            if (periodList) {
                if (excludeStartDate) {
                    periodList.remove(periodList.first())
                }
                if (!lastRunDate) {
                    lastRunDate = startDate - 1
                }
                futureRunDates = periodList.findAll {
                    new DateTime(it.toString().split("/").first()).after(lastRunDate)
                }
            }
            if (futureRunDates) {
                DateTime nextRun = new DateTime(futureRunDates?.first()?.toString()?.split("/")?.first())
                nextRunDate = new Date(nextRun.time)
                return nextRunDate
            }
        }
        nextRunDate
    }

    boolean checkStartDate(Date startDate, RRule originalRule) {
        def interval = originalRule.recur.getInterval()
        if (interval < 0) interval = 1
        DateTime fromBefore = new DateTime(startDate - interval)
        DateTime toLater = new DateTime(startDate + interval)
        VEvent eventTest = new VEvent(fromBefore, "event test")
        RRule ruleTest = originalRule
        ruleTest.recur.setCount(2) //deal with run once
        eventTest.getProperties().add(ruleTest)
        Period periodTest = new Period(fromBefore, toLater)
        PeriodList testPeriodList = eventTest.calculateRecurrenceSet(periodTest)

        DateTime from = new DateTime(startDate)
        Period start = new Period(from, new Dur(0, 0, 0, 0))
        if (testPeriodList.contains(start) || originalRule.recur.frequency == "HOURLY") {
            return false
        }
        return true
    }

    Map getMeetingAttendeeList(String meetingAttendees) {
        def toLongOrNull = { it?.isLong() ? it.toLong() : null }
        List attendeeList = []
        List guestList = []
        meetingAttendees?.split(",").each {
            if (toLongOrNull(it)) {
                if (it != null && it != "null") {
                    attendeeList << it
                }
            } else {
                if (it != null && it != "null") {
                    guestList << it
                }
            }
        }
        [attendeeList: attendeeList?.toSet()?.toList(), guestList: guestList?.toSet()?.toList()]
    }

    Meeting addAttendeesToMeeting(Meeting meeting, List attendeeList) {
        meeting.attendees?.collect()?.each {
            meeting.removeFromAttendees(it)
        }
        attendeeList.each {
            meeting.addToAttendees(User.load(Long.parseLong(it)))
        }
        meeting
    }

    Meeting addGuestAttendeesToMeeting(Meeting meeting, List guestList) {
        meeting.guestAttendee?.clear()
        guestList.each {
            meeting.addToGuestAttendee(new GuestAttendee(guestAttendeeEmail: it))
        }
        meeting
    }

    List<Meeting> createMeeting(GrailsParameterMap params, Boolean isRecurringMeeting) {
        List<Meeting> meetingList
        Meeting meeting = new Meeting(
                meetingTitle: params.meetingTitle,
                meetingOwner: User.findById(Long.parseLong(params.meetingOwner)),
                meetingMinutes: params.meetingMinutes,
                duration: params.duration,
                isRecurrenceMeeting: Boolean.FALSE,
                meetingAgenda: params.meetingAgenda,
                modifiedBy: userService.getUser().username)
        Map attendeeMap = getMeetingAttendeeList(params.meetingAttendees)
        meeting = addAttendeesToMeeting(meeting, attendeeMap.attendeeList)
        meeting = addGuestAttendeesToMeeting(meeting, attendeeMap.guestList)

        addAttachment(params, meeting)
        if (isRecurringMeeting) {
            meetingList = createRecurringMeeting(meeting, params.scheduleDateJSON)
        } else {
            def date = parseDate(params.meetingDate)
            meeting.meetingDate = date
            meetingList = [meeting]
        }
        meetingList
    }

    Map<String, String> parseKeyValuePairs(String inputString) {
        Map<String, String> keyValuePairs = new HashMap<>()
        List<String> pairs = inputString.split(";")
        for (String pair in pairs) {
            List<String> keyValue = pair.split("=")
            String key = keyValue.get(0).trim()
            String value = (keyValue.size() > 1) ? keyValue.get(1).trim() : ""
            keyValuePairs.put(key, value)
        }
        return keyValuePairs
    }

    String addRepeatingDetails(Map recurrenceProperties, String recurrenceDetails) {
        String result = recurrenceDetails
        if ('COUNT' in recurrenceProperties.keySet()) {
            result += ", recurring process ending after ${recurrenceProperties.COUNT} occurrences."
        } else if ('UNTIL' in recurrenceProperties.keySet()) {
            String formattedOnDate = new SimpleDateFormat('dd-MMM-YYYY').format(Date.parse('yyyyMMdd', recurrenceProperties.UNTIL))
            result += ", recurring process ending on ${formattedOnDate}."
        } else {
            result += ", recurrence process set to never ending."
        }
        return result
    }

    String generateRecurrenceDetailsForActivity(String recurrencePattern) {
        String month = ""
        String dayOfMonth = ""
        String dayType = ""
        Map<String, String> recurrenceProperties = parseKeyValuePairs(recurrencePattern)
        String recurrenceDetails = ""
        if (recurrenceProperties.BYSETPOS) {
            switch (recurrenceProperties.BYSETPOS) {
                case '1':
                    dayOfMonth = 'First'
                    break
                case '2':
                    dayOfMonth = 'Second'
                    break
                case '3':
                    dayOfMonth = 'Third'
                    break
                case '4':
                    dayOfMonth = 'Fourth'
                    break
                case '5':
                    dayOfMonth = 'Last'
                    break
                default:
                    dayOfMonth = 'Last'
            }
        }
        if (recurrenceProperties.BYDAY) {
            switch (recurrenceProperties.BYDAY) {
                case 'SU,SA':
                    dayType = 'weekend-day'
                    break
                case 'MO,TU,WE,TH,FR':
                    dayType = 'week-day'
                    break
                case 'SU,MO,TU,WE,TH,FR,SA':
                    dayType = 'day'
                    break
                default:
                    switch (recurrenceProperties.BYDAY) {
                        case 'SU':
                            dayType = 'Sunday'
                            break
                        case 'MO':
                            dayType = 'Monday'
                            break
                        case 'TU':
                            dayType = 'Tuesday'
                            break
                        case 'WE':
                            dayType = 'Wednesday'
                            break
                        case 'TH':
                            dayType = 'Thursday'
                            break
                        case 'FR':
                            dayType = 'Friday'
                            break
                        case 'SA':
                            dayType = 'Saturday'
                            break
                    }
            }
        }
        if (recurrenceProperties.BYMONTH) {
            switch (recurrenceProperties.BYMONTH) {
                case '1':
                    month = 'January'
                    break
                case '2':
                    month = 'February'
                    break
                case '3':
                    month = 'March'
                    break
                case '4':
                    month = 'April'
                    break
                case '5':
                    month = 'May'
                    break
                case '6':
                    month = 'June'
                    break
                case '7':
                    month = 'July'
                    break
                case '8':
                    month = 'August'
                    break
                case '9':
                    month = 'September'
                    break
                case '10':
                    month = 'October'
                    break
                case '11':
                    month = 'November'
                    break
                case '12':
                    month = 'December'
                    break
            }
        }
        if (recurrenceProperties.FREQ == 'HOURLY' || recurrenceProperties.FREQ == 'WEEKLY' || (recurrenceProperties.FREQ == 'DAILY' && !('BYDAY' in recurrenceProperties.keySet()))) {
            recurrenceDetails += "${recurrenceProperties.FREQ} meeting with ${recurrenceProperties.INTERVAL} intervals"
            recurrenceDetails = addRepeatingDetails(recurrenceProperties, recurrenceDetails)
        } else {
            switch (recurrenceProperties.FREQ) {
            //Weekdays
                case 'DAILY':
                    if ('BYDAY' in recurrenceProperties.keySet()) {
                        recurrenceDetails += "WEEKDAYS meeting with ${recurrenceProperties.INTERVAL} intervals"
                        recurrenceDetails = addRepeatingDetails(recurrenceProperties, recurrenceDetails)
                    }
                    break
                case 'MONTHLY':
                    if ('BYMONTHDAY' in recurrenceProperties) {
                        recurrenceDetails += "${recurrenceProperties.FREQ} meeting on date ${recurrenceProperties.BYMONTHDAY} of the month with ${recurrenceProperties.INTERVAL} intervals"
                        recurrenceDetails = addRepeatingDetails(recurrenceProperties, recurrenceDetails)
                    } else {
                        recurrenceDetails += "${recurrenceProperties.FREQ} meeting on ${dayOfMonth} ${dayType} of the month with ${recurrenceProperties.INTERVAL} intervals"
                        recurrenceDetails = addRepeatingDetails(recurrenceProperties, recurrenceDetails)
                    }
                    break
                case 'YEARLY':
                    if ('BYMONTHDAY' in recurrenceProperties) {
                        recurrenceDetails += "${recurrenceProperties.FREQ} meeting on date ${recurrenceProperties.BYMONTHDAY} of ${month} with ${recurrenceProperties.INTERVAL ?: "1"} intervals"
                        recurrenceDetails = addRepeatingDetails(recurrenceProperties, recurrenceDetails)
                    } else {
                        recurrenceDetails += "${recurrenceProperties.FREQ} meeting on ${dayOfMonth} ${dayType} of the month with ${recurrenceProperties.INTERVAL ?: "1"} intervals"
                        recurrenceDetails = addRepeatingDetails(recurrenceProperties, recurrenceDetails)
                    }
                    break
                default:
                    recurrenceDetails = "-"
            }
        }
        recurrenceDetails = recurrenceDetails + " (" + recurrencePattern + ")"
        return recurrenceDetails
    }

    List<Meeting> createRecurringMeeting(Meeting meeting, String scheduleDateJSON) {
        List<Meeting> meetingList
        JSONObject jsonObject = generateJSONObjectFromString(scheduleDateJSON)
        List<Date> nextRunDates = calculateDates(jsonObject)
        log.info("next run date list count : ${nextRunDates.size()}")
        if (nextRunDates) {
            String masterId = UUID.randomUUID()
            Meeting newMeeting
            meetingList = nextRunDates.collect {
                newMeeting = new Meeting(meetingDate: it,
                        meetingTitle: meeting.meetingTitle,
                        meetingOwner: meeting.meetingOwner,
                        meetingMinutes: meeting.meetingMinutes,
                        duration: meeting.duration,
                        isRecurrenceMeeting: Boolean.TRUE,
                        meetingAgenda: meeting.meetingAgenda,
                        linkingId: masterId,
                        schedularJson: scheduleDateJSON,
                        timeZone: jsonObject.timeZone.name,
                        topic: meeting.topic,
                        validatedSignal: meeting.validatedSignal
                )
                meeting.attendees.each {
                    newMeeting.addToAttendees(it)
                }
                meeting.attachments.each {
                    newMeeting.addToAttachments(it)
                }
                meeting.guestAttendee.each {
                    newMeeting.addToGuestAttendee(it)
                }
                newMeeting
            }
        } else {
            throw new IllegalArgumentException("No date found for given recurrence")
        }

        meetingList
    }

    def addAttachment(Map params, Meeting meeting) {
        def attachmentSize = Integer.parseInt(params.attachmentSize)
        for (int i = 0; i < attachmentSize; i++) {
            def attachment = "attachments" + i
            def attachedFile = params."$attachment"

            Attachments file = new Attachments(sourceAttachments: attachedFile.bytes,
                    appType: params.appType,
                    name: attachedFile.getOriginalFilename())
            meeting.addToAttachments(file)
        }
    }

    Map getByMasterId(String masterId) {
        def timeZone = userService.getUser().preference.timeZone
        Meeting meeting = Meeting.findByLinkingId(masterId)
        Map meetingMap = meeting.toDto(timeZone, false)
        meetingMap.schedularJson = meeting.schedularJson
        meetingMap
    }

    List<Meeting> updateMeetingSeries(GrailsParameterMap params, Boolean isRecurringMeeting) throws IllegalArgumentException {
        log.debug("updateMeetingSeries method called")
        String masterId = params.masterId
        List<Meeting> meetingList = []
        Meeting meeting
        def oldMeeting = Meeting.findByLinkingId(masterId)
        if (!oldMeeting) {
            throw new IllegalArgumentException("invalid master id.")
        }
        oldMeeting.meetingTitle = params.meetingTitle
        oldMeeting.meetingOwner = User.findById(Long.parseLong(params.meetingOwner))
//        oldMeeting.meetingMinutes = params.meetingMinutes
        oldMeeting.meetingAgenda = params.meetingAgenda
        oldMeeting.attendees = null

        meeting = oldMeeting
        Map attendeeMap = getMeetingAttendeeList(params.meetingAttendees)
        oldMeeting = addAttendeesToMeeting(oldMeeting, attendeeMap.attendeeList)
        oldMeeting = addGuestAttendeesToMeeting(oldMeeting, attendeeMap.guestList)

        addAttachment(params, oldMeeting)
        if (Boolean.parseBoolean(params.isRecurringMeeting)) {
            deleteRecurrenceMeetings(masterId, oldMeeting)
            meetingList = createRecurringMeeting(meeting, params.scheduleDateJSON)
            CRUDService.delete(oldMeeting)
        } else {
            deleteRecurrenceMeetings(masterId, oldMeeting)
            meeting.meetingDate = parseDate(params.meetingDate)
            meetingList = [meeting]
        }
        meetingList
    }

    def deleteRecurrenceMeetings(String masterId, Meeting meeting = null) {
        List<Meeting> meetingsList = Meeting.findAllByLinkingIdAndMeetingDateGreaterThanAndMeetingStatusNotEqual(masterId, new Date(), MeetingStatus.CANCELLED)
        meetingsList?.remove(meeting)
        meetingsList.each {
            log.info("deleting meeting : ${it.id}")
            CRUDService.delete(it)
        }
    }

    Date parseDate(String meetingDate) {
        Date date = DateUtil.stringToDate(meetingDate, DateUtil.MEETING_DATE_FORMAT, getTimeZoneOfUser())
        date
    }

    String getTimeZoneOfUser() {
        userService.getUser().preference.timeZone
    }

    Boolean isMeetingDateNotPassed(Date meetingDate) {
        Date currentDate = new Date()
        currentDate.time < meetingDate.time
    }

    List fetchMeetingTitles(String alertId, Boolean isTopicFlow) {
        List<Meeting> meetingList
        if (isTopicFlow) {
            meetingList = Meeting.findAllByTopicAndMeetingStatusNotEqual(Topic.load(alertId), MeetingStatus.CANCELLED)
        } else {
            meetingList = Meeting.findAllByValidatedSignalAndMeetingStatusNotEqual(ValidatedSignal.load(alertId), MeetingStatus.CANCELLED)
        }
        def timeZone = userService.getUser()?.preference?.getTimeZone()
        List meetingTitleList = meetingList.collect {
            [id: it.id, title: "${it.meetingTitle}(${DateUtil.toDateString(it.meetingDate, timeZone)})"]
        }
        meetingTitleList
    }

    List<Meeting> findMeetingsByActionId(Long actionId) {
        Meeting.where {
            actions {
                id == actionId
            }
        }.list()
    }

    int getActionStatus(Meeting meetingObj) {
        int returnCode = 0;
        //Assigning values 1,2,3 for green,yellow and red color.
        Set<Action> actionList = meetingObj.actions
        Date currentDate = new Date()
        long todayStartTime = currentDate.clearTime().time
        if (actionList) {
            Set<Action> openAction = actionList.findAll {
                it.actionStatus in [ActionStatus.New.name(), ActionStatus.InProgress.name()]
            }
            if (openAction) {
                returnCode = openAction.any { it.dueDate?.time < todayStartTime } ? 3 : 2
            } else {
                returnCode = 1
            }
        }
        return returnCode
    }

    ResponseDTO cancelMeeting(Map params) {
        ResponseDTO responseDTO = new ResponseDTO(status: true, message: "Meeting cancelled successfully.")
        try {
            Long meetingId = params?.id as Long
            Meeting meeting = Meeting.findById(meetingId)
            String meetingTitle = meeting.meetingTitle
            if (meeting.meetingDate.after(new Date()) && meeting.meetingStatus != MeetingStatus.CANCELLED) {
                meeting.meetingStatus = MeetingStatus.CANCELLED
                CRUDService.save(meeting)
                emailNotificationService.mailHandlerForMeetingDelete(meeting)
            } else {
                responseDTO.message = "Meeting date has passed."
            }
            if (params.appType == Constants.AlertConfigType.TOPIC) {
                Topic topicObj = Topic.findById(Long.parseLong(params.alertId))
                Map attr = [meeting: meetingTitle, topic: topicObj.name]
                activityService.createActivityForTopic(topicObj, '', "Meeting '" + meetingTitle + "' is cancelled", ActivityType.findByValue(ActivityTypeValue.MeetingCancelled), topicObj.assignedTo, userService.getUser(), attr)
            } else {
                ValidatedSignal validatedObj = ValidatedSignal.findById(Long.parseLong(params.alertId))
                Map attr = [meeting: meetingTitle, signal: validatedObj.name]
                activityService.createActivityForSignal(validatedObj, '', "Meeting '" + meetingTitle + "' is cancelled", ActivityType.findByValue(ActivityTypeValue.MeetingCancelled), validatedObj.assignedTo, userService.getUser(), attr)

            }
        } catch (Exception ex) {
            log.error(ex.printStackTrace())
            responseDTO.status = false
            responseDTO.message = "Some error occurred while cancelling meeting"
        }
        responseDTO
    }

    ResponseDTO cancelMeetingSeries(String masterId) {
        ResponseDTO responseDTO = new ResponseDTO(status: true, message: "meeting cancelled successfully.")
        try {
            Meeting meeting
            Meeting oldMeeting = Meeting.findByLinkingIdAndMeetingDateGreaterThanAndMeetingStatusNotEqual(masterId, new Date(), MeetingStatus.CANCELLED)
            log.info("oldMeeting found : ${oldMeeting}")
            if (oldMeeting) {
                meeting = new Meeting()
                meeting.meetingStatus = MeetingStatus.CANCELLED
                meeting.meetingTitle = oldMeeting.meetingTitle
                meeting.duration = oldMeeting.duration
                meeting.meetingOwner = oldMeeting.meetingOwner
                meeting.meetingDate = oldMeeting.meetingDate
                oldMeeting.attendees.each {
                    meeting.addToAttendees(it)
                }
                meeting.validatedSignal = oldMeeting.validatedSignal
                meeting.topic = oldMeeting.topic
                CRUDService.save(meeting)
                meeting.save(failOnError: true)
                deleteRecurrenceMeetings(masterId)
                emailNotificationService.mailHandlerForMeetingDelete(meeting)
                String meetingTitle = oldMeeting.meetingTitle
                if (oldMeeting.topic) {
                    Topic topicObj = oldMeeting.topic
                    Map attr = [meeting: meetingTitle, topic: topicObj.name]
                    activityService.createActivityForTopic(topicObj, '', "Meeting series '" + meetingTitle + "' is cancelled", ActivityType.findByValue(ActivityTypeValue.MeetingCancelled), topicObj.assignedTo, userService.getUser(), attr)
                } else if (oldMeeting.validatedSignal) {
                    ValidatedSignal validatedObj = oldMeeting.validatedSignal
                    Map attr = [meeting: meetingTitle, signal: validatedObj.name]
                    activityService.createActivityForSignal(validatedObj, '', "Meeting series '" + meetingTitle + "' is cancelled", ActivityType.findByValue(ActivityTypeValue.MeetingCancelled), validatedObj.assignedTo, userService.getUser(), attr)
                }

            } else {
                responseDTO.status = false
                def locale = userService?.user?.preference?.locale ?: Locale.ENGLISH
                responseDTO.message = messageSource.getMessage("app.meeting.masterId.invalid", [], locale)
            }
        } catch (Exception e) {
            e.printStackTrace()
            responseDTO.status = false
            responseDTO.message = "Some error occurred while cancelling series."
        }
        responseDTO
    }

    Map fetchMeetingAttendeeEmails(Meeting meeting) {
        List<MeetingAttendee> attendee = []
        meeting.attendees.each {
            if (it.enabled)
                attendee.add(new MeetingAttendee(email: it.email, timeZone: it.preference.timeZone))
        }
        if (!(attendee.email?.contains(meeting.meetingOwner.email))) {
            attendee.add(new MeetingAttendee(email: meeting.meetingOwner.email, timeZone: meeting.meetingOwner.preference.timeZone))
        }
        merge(attendee.groupBy({ recipient -> recipient.timeZone }), meeting.guestAttendee?.guestAttendeeEmail?.groupBy({ recipient -> 'UTC' }))
    }

    Map merge(Map lhs, Map rhs) {
        return rhs.inject(lhs.clone()) { map, entry ->
            if (map[entry.key] instanceof Map && entry.value instanceof Map) {
                map[entry.key] = merge(map[entry.key], entry.value)
            } else if (map[entry.key] instanceof Collection && entry.value instanceof Collection) {
                map[entry.key] += entry.value
            } else {
                map[entry.key] = entry.value
            }
            return map
        }
    }

    File generateICSFile(Meeting meeting) {
        log.info("generating .ics file for meeting : ${meeting.id}")
        File file = new File("mycalendar.ics")
        try {
            User owner = meeting.meetingOwner
            String userTimeZone = userService.getUser()?.preference?.timeZone
            Map<String, String> abbreviatedTimeZoneMap = grailsApplication.config.signal.customTimeZoneMap.abbreviatedTimeZoneMap as Map<String, String>
            if (abbreviatedTimeZoneMap.containsKey(userTimeZone)) {
                userTimeZone = abbreviatedTimeZoneMap.get(userTimeZone)
            }
            String dateFormat = "yyyy-MM-dd'T'HH:mmXXX"

            // Creating a new calendar
            net.fortuna.ical4j.model.Calendar calendar = new net.fortuna.ical4j.model.Calendar()
            calendar.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"))
            calendar.getProperties().add(Version.VERSION_2_0)
            calendar.getProperties().add(CalScale.GREGORIAN)

            Dur dur = new Dur(0, 0, meeting.duration as Integer, 0)
            def jb
            Date meetingDateParam
            if (meeting.isRecurrenceMeeting) {
                String jsonString = meeting.schedularJson
                jb = generateJSONObjectFromString(jsonString)
                meetingDateParam = Date.parse(dateFormat, jb.startDateTime)
            } else {
                meetingDateParam = meeting.meetingDate
            }
            if (!meetingDateParam.timezoneOffset) {
                userTimeZone = 'UTC'
            }

            TimeZoneRegistry registry = TimeZoneRegistryFactory.getInstance().createRegistry();
            TimeZone timezone = registry.getTimeZone(userTimeZone);

            net.fortuna.ical4j.model.DateTime dt = new net.fortuna.ical4j.model.DateTime(meetingDateParam.time)
            VEvent event = new VEvent(dt, dur, meeting.meetingTitle);

            // Create a TimeZone
            VTimeZone tz = timezone.getVTimeZone();

            // adding rrule
            if (meeting.isRecurrenceMeeting) {
                event.getProperties().add(new RRule(jb.recurrencePattern));
            }
            event.getProperties().add(new Description())
            event.getProperties().getProperty(Property.DESCRIPTION).setValue(meeting.meetingAgenda);

            // Generate a UID for the event..
            UidGenerator ug = new UidGenerator("1");
            event.getProperties().add(ug.generateUid());

            // add attendees..
            List<Map> attendeeList = []
            meeting.attendees.each {
                attendeeList << [name: it.fullName, email: it.email]
            }
            meeting.guestAttendee.each {
                attendeeList << [name: it.guestAttendeeEmail, email: it.guestAttendeeEmail]
            }

            Attendee attendee
            attendeeList.each {
                attendee = new Attendee(URI.create("mailto:${it.email}"))
                attendee.getParameters().add(Role.REQ_PARTICIPANT);
                attendee.getParameters().add(new Cn(it.name));
                attendee.getParameters().add(new Rsvp(true));
                event.getProperties().add(attendee);
            }

            if (owner.email && owner.fullName) {
                //add organizer
                Organizer organizer = new Organizer(URI.create(owner.email))
                organizer.getParameters().add(new Cn(owner.fullName))
                organizer.getParameters().add(new Rsvp(true));
                event.getProperties().add(organizer);

                calendar.getComponents().add(event)
                calendar.getComponents().add(tz)
                FileOutputStream fout = new FileOutputStream(file)
                CalendarOutputter outputter = new CalendarOutputter()
                outputter.output(calendar, fout)
            } else {
                log.info("Owner's email and full name are not set in the user table. Owner is -: " + owner.fullName)
            }
        } catch (Exception e) {
            e.printStackTrace()
        }
        file
    }

    void sendAddMinutesNotificationMail(Meeting meeting, String msg, String screenName, String alertLink, Boolean forceSendEmail = false) {
        List recipients = fetchMeetingAttendeeEmails(meeting)
        emailService.sendNotificationEmail(['toAddress': recipients, 'forceSendEmail': forceSendEmail,
                                            'title'    : msg,
                                            "inboxType": "Meeting Minutes Added",
                                            'map'      : ["map"       : ['Meeting Title '      : meeting.meetingTitle,
                                                                         "Meeting Owner "      : meeting.meetingOwner,
                                                                         "Meeting Date & Time" : "${DateUtil.toDateStringWithTime(meeting.meetingDate, "UTC")} GMT",
                                                                         "Meeting Agenda"      : meeting.meetingAgenda ?: "-",
                                                                         "Meeting Minutes"     : meeting.meetingMinutes,
                                                                         "Meeting Participants": recipients?.join(',')
                                            ], "emailMessage"         : msg,
                                                          "screenName": screenName,
                                                          "alertLink" : alertLink]])
    }

    void sendMeetingNotificationMail(Meeting meeting, String msg, List<File> files, String screenName, String alertLink, String inboxType, Boolean forceSendEmail = false) {
        List recipients = fetchMeetingAttendeeEmails(meeting)
        emailService.sendNotificationEmail(['toAddress': recipients, 'forceSendEmail': forceSendEmail,
                                            "inboxType": inboxType,
                                            'title'    : msg,
                                            'map'      : ["map"         : ['Meeting Title '      : meeting.meetingTitle,
                                                                           "Meeting Owner "      : meeting.meetingOwner,
                                                                           "Meeting Date & Time" : "${DateUtil.toDateStringWithTime(meeting.meetingDate, "UTC")} GMT",
                                                                           "Meeting Agenda"      : meeting.meetingAgenda ?: "-",
                                                                           "Meeting Participants": recipients?.join(',')
                                            ],
                                                          "attachments" : files,
                                                          "emailMessage": msg,
                                                          "screenName"  : screenName,
                                                          "alertLink"   : alertLink]])
    }

    def fetchMeetingAttachments(Meeting oldMeeting) {
        List uploads = []
        oldMeeting.attachments.each {
            File uploadedAttachment = new File(Holders.config.grails.attachmentable.uploadDir + '/' + it.name)
            uploadedAttachment.createNewFile()
            OutputStream os = new FileOutputStream(uploadedAttachment)
            os.write(it.sourceAttachments)
            os.close()
            uploads << [name: uploadedAttachment.name, file: uploadedAttachment]
        }
        uploads
    }
}
