package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.enums.MeetingStatus
import com.rxlogix.signal.Topic
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.DbUtil

import java.text.SimpleDateFormat

class Meeting {
    static auditable = ['ignore':['linkingId','lastUpdated', 'lastUpdatedBy', 'dateCreated', 'createdBy','modifiedBy','validatedSignal','schedularJson','isRecurrenceMeeting']]

    transient def meetingService
    transient def userService


    String meetingTitle
    String meetingMinutes
    Date meetingDate
    User meetingOwner
    String meetingAgenda
    MeetingStatus meetingStatus = MeetingStatus.SCHEDULED
    Boolean isRecurrenceMeeting = Boolean.FALSE
    String schedularJson
    String linkingId
    String timeZone
    Long duration

    //Common db table fields
    String createdBy
    Date dateCreated
    Date lastUpdated
    String modifiedBy

    static mapping = {
        table name: "MEETING"
        attachments joinTable: [name: "MEETING_ATTACHMENTS", column: "ATTACHMENTS_ID", key: "MEETING_ID"]
        guestAttendee cascade: 'all-delete-orphan'
        schedularJson column: "SCHEDULAR_JSON", sqlType: DbUtil.longStringType
    }
    static hasMany = [activities   : Activity,
                      actions      : Action,
                      attendees    : User,
                      attachments  : Attachments,
                      guestAttendee: GuestAttendee]

    static belongsTo = [validatedSignal: ValidatedSignal, topic: Topic]

    static constraints = {
        lastUpdated nullable: true
        modifiedBy nullable: true
        createdBy nullable: true
        meetingMinutes maxSize: 8000, nullable: true
        meetingAgenda nullable: true, maxSize: 8000
        topic nullable: true
        validatedSignal nullable: true
        isRecurrenceMeeting nullable: true
        schedularJson nullable: true
        linkingId nullable: true
        timeZone nullable: true
        duration nullable: false
    }

    def toDto(timeZone = "UTC",Boolean isList = false) {
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATEPICKER_FORMAT_AM_PM)

        [
                meetingTitle       : this.meetingTitle?.trim()?.replaceAll("\\s{2,}", " "),
                id                 : this.id,
                meetingMinutes     : this.meetingMinutes,
                meetingDate        : DateUtil.toDateStringWithTime(this.meetingDate, timeZone),
                meetingOwner       : this.meetingOwner?.fullName,
                createdBy          : this.createdBy,
                dateCreated        : this.dateCreated,
                lastModified       : sdf.format(new Date(DateUtil.toDateStringWithTime(this.lastUpdated, timeZone))),
                modifiedBy         : User.findByUsername(this.modifiedBy)?.fullName,
                ownerId            : this.meetingOwner.id,
                attendees          : this.attendees,
                guestAttendees     : this.guestAttendee*.guestAttendeeEmail,
                actionStatus       : getActionStatus(this),
                meetingDateModified: DateUtil.toDateString(this.meetingDate),
                meetingAgenda      : this.meetingAgenda,
                meetingAttachments : isList ? [] : getAttachments(this),
                masterId           : this.linkingId,
                dateOfMeeting      : this.meetingDate,
                isRecurrenceMeeting: this.isRecurrenceMeeting,
                meetingStatus      : this.meetingStatus.toString(),
                duration           : this.duration,
                lastUpdatedDate    : this.lastUpdated
        ]
    }

    String createMeetingTitleText() {
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATEPICKER_FORMAT)
        String title = linkingId ? "${this.meetingTitle}(${sdf.format(this.meetingDate)})" : this.meetingTitle
        title = this.meetingStatus == MeetingStatus.CANCELLED ? "${title}(${this.meetingStatus.id})" : title
        title
    }

    def getActionStatus(Meeting meetingObj) {
        meetingService.getActionStatus(meetingObj)
    }

    def getAttachments(Meeting meetingObj) {
        def attachmentList = []
        meetingObj.attachments.each {
            def map = [:]
            map.put("name", it.name)
            map.put("sourceFile", it.sourceAttachments)
            map.put("id", it.id)
            attachmentList.add(map)
        }
        return attachmentList
    }
    @Override
    String toString() {
        "$meetingTitle"
    }

    def getInstanceIdentifierForAuditLog() {
        return this.validatedSignal.getInstanceIdentifierForAuditLog() + ": " + meetingTitle
    }

    def getModuleNameForMultiUseDomains() {
        return "Signal: Meeting"
    }

    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        String userTimeZone = userService?.getUser()?.preference?.timeZone ?: 'UTC'
        if (newValues && (oldValues == null)){
            //case of meeting create done to put meeting time manually as it is derived from meeting date
            if (newValues.containsKey("meetingDate")) {
                newValues.put('startTime', DateUtil.fromDateToStringWithTimezone(meetingDate, "hh:mm a", userTimeZone))
            }
        }
        if (newValues && oldValues && this.dirtyPropertyNames?.contains("meetingDate")) {
            newValues.put("startTime", DateUtil.fromDateToStringWithTimezone(meetingDate, "hh:mm a", userTimeZone))
            oldValues.put("startTime", DateUtil.fromDateToStringWithTimezone(this.getPersistentValue("meetingDate"), "hh:mm a", userTimeZone))
        }

        return [newValues: newValues, oldValues: oldValues]
    }

}
