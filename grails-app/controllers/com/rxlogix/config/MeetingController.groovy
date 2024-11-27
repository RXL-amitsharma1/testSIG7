package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.EmailNotificationService
import com.rxlogix.controllers.SignalController
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.signal.Topic
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.User
import com.rxlogix.util.AttachmentableUtil
import com.rxlogix.util.DateUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import net.fortuna.ical4j.model.property.RRule
import org.grails.web.json.JSONObject

import java.text.SimpleDateFormat

@Secured(["isAuthenticated()"])
class MeetingController implements SignalController {

    def CRUDService
    def emailService
    def userService
    def activityService
    def meetingService
    def outlookService
    EmailNotificationService emailNotificationService
    public final static String JSON_DATE = "yyyy-MM-dd'T'HH:mmXXX"


    def index() {}

    def list() {
        def meetings = [:]
        def timeZone = userService.getUser().preference.timeZone
        def alertId = params.alertId
        if (alertId) {
            def assignedMeetings = null
            if (params.appType == Constants.AlertConfigType.TOPIC) {
                assignedMeetings = Topic.findById(alertId)?.meetings
            } else {
                assignedMeetings = ValidatedSignal.findById(alertId)?.meetings
            }

            if (assignedMeetings) {
                meetings = assignedMeetings.collect {
                    it.toDto(timeZone,true)
                }
                meetings = meetingService.applyRecurrenceLimit(meetings)
                meetings = meetingService.checkIfMeetingDatePassed(meetings)
                meetings = meetings.sort { it.lastUpdatedDate }.reverse()
            }
            render(meetings as JSON)
        } else {
            response.status = 200
            render(text: "[]", contentType: 'application/json')
        }
    }

    def save(Boolean isRecurringMeeting, Boolean downloadICSFile) {
        ResponseDTO responseDTO = new ResponseDTO(status: true, message: "Meeting saved successfully.")
        validateMeetingParams(params, isRecurringMeeting)
        def topicOrValidatedObj
        String userTimeZone = userService?.getUser()?.preference?.timeZone?:'UTC'
        String screenName
        String alertLink
        if (params.appType == Constants.AlertConfigType.TOPIC) {
            topicOrValidatedObj = Topic.findById(Long.parseLong(params.alertId))
            screenName = Constants.AlertConfigType.TOPIC
            alertLink = createHref("topic", "details", ["id": params.alertId])

        } else {
            topicOrValidatedObj = ValidatedSignal.findById(Long.parseLong(params.alertId))
            screenName = Constants.AlertConfigType.SIGNAL
            alertLink = createHref("validatedSignal", "details", ["id": params.alertId])
        }

        try {

            List<Meeting> meetingList = meetingService.createMeeting(params, isRecurringMeeting)
            meetingList.each {
                topicOrValidatedObj.addToMeetings(it)
            }

            if (isRecurringMeeting && (params.scheduleDateJSON)) {
                Date endDate
                JSONObject timeObject = JSON.parse(params.scheduleDateJSON)
                Date startDate = Date.parse(JSON_DATE, timeObject.startDateTime)
                if(timeObject.recurrencePattern) {
                    RRule recurRule = new RRule(timeObject?.recurrencePattern)
                     endDate = recurRule?.recur?.getUntil()
                }
                String errorMsg = null

            }
            CRUDService.update(topicOrValidatedObj)
           // Removed for PVS-60038
            if (downloadICSFile) {
                responseDTO.data = file.text
            }
            List uploads = []
            uploads = meetingService.fetchMeetingAttachments(meetingList.first())
            if(emailNotificationService.emailNotificationWithBulkUpdate(null, Constants.EmailNotificationModuleKeys.MEETING_CREATION_UPDATION)) {
                def msg
                if (isRecurringMeeting) {
                    msg = message(code: "app.email.meeting.create.title.recurrent")
                } else {
                    msg = message(code: "app.email.meeting.create.title")
                }
                emailNotificationService.mailHandlerForMeetingCRUD(meetingList.first(), msg, uploads, screenName, alertLink, "Meeting Creation")
            }
            if (params.appType == Constants.AlertConfigType.TOPIC) {
                meetingList.each {
                    String recurrencePattern = ''
                    String recurrenceDetails = ''
                    if (it?.isRecurrenceMeeting) {
                        recurrencePattern = new JSONObject(it?.schedularJson)['recurrencePattern'].toString()
                        recurrenceDetails = meetingService.generateRecurrenceDetailsForActivity(recurrencePattern)
                    }
                    def attr = [meeting: it.meetingTitle, topic: topicOrValidatedObj.name]
                    activityService.createActivityForTopic(topicOrValidatedObj, '', "Meeting created with Meeting Title '${it?.meetingTitle?:''}', Meeting Owner '${it?.meetingOwner?.fullName?:''}', Meeting Attendees '${it?.attendees?it?.attendees*.fullName.toString():''}', Meeting Date '${DateUtil.toDateString(it?.meetingDate, userTimeZone)}', Start Time '${DateUtil.stringFromDate(it?.meetingDate,DateUtil.DATETIME_FMT, userTimeZone).substring(Math.max(DateUtil.stringFromDate(it?.meetingDate,DateUtil.DATETIME_FMT, userTimeZone).size() - 8,0))}', Duration '${it?.duration} Minutes', Recurrence '${it?.isRecurrenceMeeting?recurrenceDetails:'False'}', Agenda= '${it?.meetingAgenda?:''}', Attachment= '${it?.attachments?it?.attachments*.name.toString():''}'", ActivityType.findByValue(ActivityTypeValue.MeetingCreated), topicOrValidatedObj.assignedTo, userService.getUser(), attr)

                }
            } else {
                meetingList.each {
                    String recurrencePattern = ''
                    String recurrenceDetails = ''
                    if (it?.isRecurrenceMeeting) {
                        recurrencePattern = new JSONObject(it?.schedularJson)['recurrencePattern'].toString()
                        recurrenceDetails = meetingService.generateRecurrenceDetailsForActivity(recurrencePattern)
                    }
                    def attr = [meeting: it.meetingTitle, signal: topicOrValidatedObj.name]
                    activityService.createActivityForSignal(topicOrValidatedObj, '', "Meeting created with Meeting Title '${it?.meetingTitle?:''}', Meeting Owner '${it?.meetingOwner?.fullName?:''}', Meeting Attendees '${it?.attendees?it?.attendees*.fullName.toString():''}', Meeting Date '${DateUtil.toDateString(it?.meetingDate, userTimeZone)}', Start Time '${DateUtil.stringFromDate(it?.meetingDate,DateUtil.DATETIME_FMT, userTimeZone).substring(Math.max(DateUtil.stringFromDate(it?.meetingDate,DateUtil.DATETIME_FMT, userTimeZone).size() - 8,0))}', Duration '${it?.duration} Minutes', Recurrence '${it?.isRecurrenceMeeting?recurrenceDetails:'False'}', Agenda= '${it?.meetingAgenda?:''}', Attachment= '${it?.attachments?it?.attachments*.name.toString():''}'", ActivityType.findByValue(ActivityTypeValue.MeetingCreated), topicOrValidatedObj.assignedTo, userService.getUser(), attr)
                }
            }

        } catch (Exception ve) {
            ve.printStackTrace()
            if (ve.toString().contains('guestAttendeeEmail.email.error'))
                responseDTO.code = 400
            responseDTO.status = false
        }
        render(responseDTO as JSON)
    }

    def handleIllegalArgumentException(IllegalArgumentException e) {
        render(new ResponseDTO(status: false, message: e.message) as JSON)
    }

    private void addAttachment(Meeting meeting) {
        meetingService.addAttachment(params, meeting)
    }

    def getById() {
        def timeZone = userService.getUser().preference.timeZone
        def meetingId = params.meetingId
        Map meeting = Meeting.findById(meetingId).toDto(timeZone,false)
        render meeting as JSON
    }

    def getByMasterId(String masterId) {
        Map meetingResponse = meetingService.getByMasterId(masterId)
        render meetingResponse as JSON
    }

    def fetchMeetingMinutes(String meetingId) {
        log.debug("fetchMeetingMinutes called")
        Meeting meeting = Meeting.read(meetingId)
        ResponseDTO responseDTO = new ResponseDTO()
        responseDTO.status = true
        responseDTO.data = meeting.meetingMinutes
        render responseDTO as JSON
    }

    def saveMeetingMinutes(String appType, String alertId, String meetingId, String meetingMinutes) {
        log.debug("saveMeetingMinutes called")
        ResponseDTO responseDTO = new ResponseDTO(status: true, message: "Meeting Minutes Saved Successfully.")
        Meeting meeting = Meeting.get(meetingId)
        String prevMeetingMinutes = meeting?.meetingMinutes?:''
        meeting.meetingMinutes = meetingMinutes
        meeting.meetingOwner = User.findById(Long.parseLong(params.meetingOwner))
        meeting.attendees = null
        Map attendeeMap = meetingService.getMeetingAttendeeList(params.meetingAttendees)
        meeting = meetingService.addAttendeesToMeeting(meeting, attendeeMap.attendeeList)
        meeting = meetingService.addGuestAttendeesToMeeting(meeting, attendeeMap.guestList)
        def topicOrValidatedObj
        try {
            addAttachment(meeting)
            CRUDService.update(meeting)
            String screenName
            String alertLink
            if (appType != Constants.AlertConfigType.TOPIC) {
                topicOrValidatedObj = ValidatedSignal.findById(Long.parseLong(alertId))
                screenName = Constants.AlertConfigType.SIGNAL
                alertLink = createHref("validatedSignal", "details", ["id": alertId])
                def attr = [meeting: meeting.meetingTitle, signal: topicOrValidatedObj.name]
                activityService.createActivityForSignal(topicOrValidatedObj, '', "Meeting '${meeting?.meetingTitle?:''}' Updated, Meeting Minutes changed from '${prevMeetingMinutes}' to '${meeting?.meetingMinutes?:''}'", ActivityType.findByValue(ActivityTypeValue.MeetingUpdated), topicOrValidatedObj.assignedTo, userService.getUser(), attr,topicOrValidatedObj.assignedToGroup)
            } else {
                topicOrValidatedObj = Topic.findById(Long.parseLong(alertId))
                screenName = Constants.AlertConfigType.TOPIC
                alertLink = createHref("topic", "details", ["id": alertId])
                def attr = [meeting: meeting.meetingTitle, topic: topicOrValidatedObj.name]
                activityService.createActivityForTopic(topicOrValidatedObj, '', "Meeting '${meeting?.meetingTitle?:''}' Updated, Meeting Minutes changed from '${prevMeetingMinutes}' to '${meeting?.meetingMinutes?:''}'", ActivityType.findByValue(ActivityTypeValue.MeetingUpdated), topicOrValidatedObj.assignedTo, userService.getUser(), attr)
            }
            if(emailNotificationService.emailNotificationWithBulkUpdate(null, Constants.EmailNotificationModuleKeys.MINUTES_CREATION_UPDATION)) {
                def msg = message(code: "app.email.meeting.minutes.added.title")
                emailNotificationService.mailHandlerForMinutesOfMeeting(meeting, msg, screenName, alertLink)
            }
        } catch (Exception e) {
            responseDTO.status = false
            responseDTO.message = e.message
        }
        render responseDTO as JSON
    }

    def updateMeeting(Boolean downloadICSFile) {
        ResponseDTO responseDTO = new ResponseDTO(status: true, message: "meeting updated successfully.")
        validateMeetingParams(params, false)
        def topicOrValidatedObj
        String screenName
        String alertLink
        def meetingId = params.meetingId
        def date = meetingService.parseDate(params.meetingDate)
        Meeting oldMeeting = Meeting.findById(meetingId)
        Meeting newMeeting = new Meeting()
        bindData(newMeeting, params)
        newMeeting.meetingDate = date
        Map attendeeMap = meetingService.getMeetingAttendeeList(params.meetingAttendees)
        newMeeting = meetingService.addAttendeesToMeeting(newMeeting, attendeeMap.attendeeList)
        newMeeting = meetingService.addGuestAttendeesToMeeting(newMeeting, attendeeMap.guestList)
        addAttachment(newMeeting)
        String description = composeMeetingDescription(oldMeeting, newMeeting)
        oldMeeting.meetingDate = date
        oldMeeting.meetingTitle = params.meetingTitle
        oldMeeting.meetingOwner = User.findById(Long.parseLong(params.meetingOwner))
        oldMeeting.meetingAgenda = params.meetingAgenda
        oldMeeting.isRecurrenceMeeting = false
        oldMeeting.attendees = null
        oldMeeting.duration = params.duration as Long
        oldMeeting = meetingService.addAttendeesToMeeting(oldMeeting, attendeeMap.attendeeList)
        oldMeeting = meetingService.addGuestAttendeesToMeeting(oldMeeting, attendeeMap.guestList)
        if (params.appType == Constants.AlertConfigType.TOPIC) {
            topicOrValidatedObj = Topic.findById(Long.parseLong(params.alertId))
            oldMeeting.topic = topicOrValidatedObj
            screenName = 'Topic'
            alertLink = createHref("topic", "details", ["id": params.alertId])

        } else {
            topicOrValidatedObj = ValidatedSignal.findById(Long.parseLong(params.alertId))
            oldMeeting.validatedSignal = topicOrValidatedObj
            screenName = 'Signal'
            alertLink = createHref("validatedSignal", "details", ["id": params.alertId])
        }
        addAttachment(oldMeeting)

        try {
            CRUDService.update(oldMeeting)
            if (params.appType == Constants.AlertConfigType.TOPIC) {
                def attr = [meeting: params.meetingTitle, topic: topicOrValidatedObj.name]
                activityService.createActivityForTopic(topicOrValidatedObj, '', "Meeting '" + params.meetingTitle + "' is updated", ActivityType.findByValue(ActivityTypeValue.MeetingUpdated), topicOrValidatedObj.assignedTo, userService.getUser(), attr)
            } else {
                def attr = [meeting: params.meetingTitle, signal: topicOrValidatedObj.name]
                if(description)
                   activityService.createActivityForSignal(topicOrValidatedObj, '', description, ActivityType.findByValue(ActivityTypeValue.MeetingUpdated), topicOrValidatedObj.assignedTo, userService.getUser(), attr)

            }
            File file = meetingService.generateICSFile(oldMeeting)
            List<Map> attachments = []
            if (downloadICSFile) {
                responseDTO.data = file.text
                attachments.add([name: file.name, file: file])
            }

            oldMeeting.attachments.each {
                attachments.add([name: it.name, file: AttachmentableUtil.generateFileFromBytes(it.sourceAttachments, it.name)])
            }
            if (emailNotificationService.emailNotificationWithBulkUpdate(null, Constants.EmailNotificationModuleKeys.MEETING_CREATION_UPDATION)) {
                String msg = message(code: "app.email.meeting.update.title")
                emailNotificationService.mailHandlerForMeetingCRUD(oldMeeting, msg, attachments, screenName, alertLink, "Meeting Updated")
            }

        } catch (Exception ex) {
            responseDTO.status = false
            responseDTO.message = ex.message
            if (ex.toString().contains('guestAttendeeEmail.email.error'))
                responseDTO.code = 400
        }
        response.status = 200
        render(responseDTO as JSON)
    }

    def composeMeetingDescription(Meeting oldMeeting, Meeting updatedMeeting){
        String description = ''
        [Constants.MeetingUpdate.MEETING_TITLE, Constants.MeetingUpdate.MEETING_OWNER, Constants.MeetingUpdate.DURATION, Constants.MeetingUpdate.MEETING_AGENDA, Constants.MeetingUpdate.MEETING_DATE, Constants.MeetingUpdate.ATTENDEES, Constants.MeetingUpdate.GUEST_ATTENDEE].each{column ->
            def oldVal = oldMeeting."${column}" ?: ''
            def newVal = updatedMeeting."${column}" ?: ''
            if(column == "duration"){
                    oldVal = (oldVal.toInteger() < 60) ? oldVal.toString() + " min" : (oldVal.toInteger()/60).toString() + " hour"
                    newVal = (newVal.toInteger() < 60) ? newVal.toString() + " min" : (newVal.toInteger()/60).toString() + " hour"
            }

            if (oldVal != newVal) {

                if (column == Constants.MeetingUpdate.MEETING_DATE) {
                    oldVal = DateUtil.StringFromDate(oldVal, DateUtil.DATEPICKER_FORMAT_AM_PM, userService.getUser()?.preference?.timeZone)
                    newVal = DateUtil.StringFromDate(newVal, DateUtil.DATEPICKER_FORMAT_AM_PM, userService.getUser()?.preference?.timeZone)
                }
                description += ",<br>'" + column + "' is changed from '" + oldVal + "' to '" + newVal + "'"
            }
        }

        def attachmentList = updatedMeeting?.attachments?.name
        if( attachmentList  && attachmentList != "null"){
            description += ", new attachment added= " + attachmentList
        }
        if(description != '')
            return "Meeting '" + oldMeeting.meetingTitle + "' is updated" + description
    }


    def updateMeetingSeries(Boolean isRecurringMeeting, Boolean downloadICSFile) {
        isRecurringMeeting = true
        ResponseDTO responseDTO = new ResponseDTO(status: true, message: "meeting updated successfully.")
        validateMeetingParams(params, isRecurringMeeting)
        def topicOrValidatedObj
        String screenName
        String alertLink
        List<Meeting> meetingList = meetingService.updateMeetingSeries(params, isRecurringMeeting)

        if (params.appType == Constants.AlertConfigType.TOPIC) {
            topicOrValidatedObj = Topic.findById(Long.parseLong(params.alertId))
            screenName = 'Topic'
            alertLink = createHref("topic", "details", ["id": params.alertId])
            meetingList.each {
                it.topic = topicOrValidatedObj
            }
        } else {
            topicOrValidatedObj = ValidatedSignal.findById(Long.parseLong(params.alertId))
            screenName = 'Signal'
            alertLink = createHref("validatedSignal", "details", ["id": params.alertId])
            meetingList.each {
                it.validatedSignal = topicOrValidatedObj
            }
        }

        try {
            meetingList.each {
                CRUDService.save(it)
            }
            if(emailNotificationService.emailNotificationWithBulkUpdate(null, Constants.EmailNotificationModuleKeys.MEETING_CREATION_UPDATION)) {
                Meeting meeting = meetingList.first()
                File file = meetingService.generateICSFile(meeting)
                if (downloadICSFile) {
                    responseDTO.data = file.text
                }
                def msg = message(code: "app.email.meeting.update.title")
                emailNotificationService.mailHandlerForMeetingCRUD(meeting, msg, [file], screenName, alertLink, "Meeting Updated")
            }
        } catch (Exception ex) {
            responseDTO.status = false
            responseDTO.message = ex.message
        }
        response.status = 200
        render(responseDTO as JSON)
    }

    def viewAttachments() {
        def attachmentId = Long.parseLong(params.attachmentId)
        def meetingId = Long.parseLong(params.meetingId)

        def attachmentBytes
        def attachmentName
        for (def attachment : Meeting.findById(meetingId).attachments) {
            if (attachment.id == attachmentId) {
                attachmentBytes = attachment.sourceAttachments
                attachmentName = attachment.name
                break;
            }
        }
        response.getOutputStream().write(attachmentBytes);
        response.setContentType("application/force-download");
        response.setHeader("Content-Transfer-Encoding", "binary");
        response.outputStream.flush()
    }

    def viewMeeting() {
        def meetingInstance = Meeting.get(params.id)
        render(view: "viewMeeting", model: [meetingInstance: meetingInstance])
    }

    private void validateMeetingParams(params, Boolean isRecurringMeeting) throws IllegalArgumentException {
        if (params.meetingDate == '' && !isRecurringMeeting) {
            throw new IllegalArgumentException("meeting date is required")
        }
        if (!params.duration?.isInteger()) {
            throw new IllegalArgumentException("duration is required")
        }

        if (!params.meetingTitle) {
            throw new IllegalArgumentException("meeting title is required")
        }
        try {
            if (!isRecurringMeeting) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm")
                dateFormat.parse(params.meetingDate)
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid meeting date")
        }
    }

    def findMeetingTimes() {
        render(outlookService.findMeetingTimes(params) as JSON)
    }

    def fetchMeetingTitles(String alertId, Boolean isTopicFlow) {
        List meetingTitleList = meetingService.fetchMeetingTitles(alertId, isTopicFlow)
        render(meetingTitleList as JSON)
    }

    def cancelMeeting() {
        ResponseDTO responseDTO = meetingService.cancelMeeting(params)
        render(responseDTO as JSON)
    }

    def cancelMeetingSeries(String masterId) {
        ResponseDTO responseDTO = meetingService.cancelMeetingSeries(masterId)
        render(responseDTO as JSON)
    }
}
