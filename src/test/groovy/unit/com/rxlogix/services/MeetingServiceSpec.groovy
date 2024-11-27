package com.rxlogix.services

import com.rxlogix.CRUDService
import com.rxlogix.EmailNotificationService
import com.rxlogix.MeetingService
import com.rxlogix.UserService
import com.rxlogix.config.Disposition
import com.rxlogix.config.Meeting
import com.rxlogix.config.Priority
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.enums.GroupType
import com.rxlogix.user.Group
import org.springframework.context.MessageSource
import grails.testing.services.ServiceUnitTest
import org.joda.time.DateTime
import spock.lang.Ignore
import spock.lang.Specification
import com.rxlogix.user.User
import com.rxlogix.user.Preference
import com.rxlogix.enums.MeetingStatus
import com.rxlogix.user.UserGroupMapping

@Ignore
class MeetingServiceSpec extends Specification implements ServiceUnitTest<MeetingService> {

    UserService userService
    CRUDService crudService
    EmailNotificationService emailNotificationService
    MessageSource messageSource
    Priority priority
    Disposition disposition,defaultDisposition,autoRouteDisposition
    Group group
    User user

    def setup() {
        disposition = new Disposition(value: "ValidatedSignal", displayName: "Validated Signal", validatedConfirmed: true, abbreviation: "vs")
        disposition.save(failOnError: true)

        defaultDisposition = new Disposition(value: "New", displayName: "New", validatedConfirmed: false, abbreviation: "NEW")
        autoRouteDisposition = new Disposition(value: "Required Review", displayName: "Required Review", validatedConfirmed: false, abbreviation: "RR")

        [defaultDisposition, autoRouteDisposition].collect { it.save(failOnError: true) }

        // Prepare the mock Group
        group = new Group(name: "Default", groupType: GroupType.WORKFLOW_GROUP, defaultDisposition: defaultDisposition,
                defaultSignalDisposition: disposition, autoRouteDisposition: autoRouteDisposition, justificationText: "Update Disposition",
                forceJustification: true, defaultQualiDisposition: disposition, defaultQuantDisposition: disposition,
                defaultAdhocDisposition: disposition, defaultEvdasDisposition: disposition, defaultLitDisposition: disposition,
                createdBy: "ujjwal", modifiedBy: "ujjwal")
        group.save(validate: false)

        Group wfGroup = new Group(name: "Default", createdBy: "ujjwal", modifiedBy: "ujjwal", groupType: GroupType.WORKFLOW_GROUP,
                defaultQualiDisposition: disposition, defaultQuantDisposition: disposition, defaultAdhocDisposition: disposition,
                defaultEvdasDisposition: disposition, defaultLitDisposition: disposition, defaultSignalDisposition: disposition,
                autoRouteDisposition: autoRouteDisposition, justificationText: "Update Disposition", forceJustification: true)
        wfGroup.save(flush: true)

        // Prepare the mock user
        user = new User(id: 1L, username: 'username', createdBy: 'createdBy', modifiedBy: 'modifiedBy')
        user.preference.createdBy = "createdBy"
        user.preference.modifiedBy = "modifiedBy"
        user.preference.locale = new Locale("en")
        user.preference.isEmailEnabled = false
        user.metaClass.getFullName = { 'Fake Name' }
        user.metaClass.getEmail = { 'fake.email@fake.com' }
        user.addToGroups(group)
        UserGroupMapping userGroupMapping = new UserGroupMapping(user: user, group: wfGroup)
        userGroupMapping.save(flush: true, failOnError: false)
        user.save(validate: false)

        priority = new Priority(value: "Low", display: true, displayName: "Low", reviewPeriod: 3, priorityOrder: 1)
        priority.save(validate: false)
        crudService = Mock(CRUDService)
        service.CRUDService=crudService


        emailNotificationService = Mock(EmailNotificationService)
        service.emailNotificationService = emailNotificationService

        messageSource = Mock(MessageSource)
        service.messageSource = messageSource
    }

    def "test isMeetingDateNotPassed method"() {
        when:
        Boolean isMeetingDateNotPassed = service.isMeetingDateNotPassed(meetingDate)

        then:
        isMeetingDateNotPassed == result

        where:
        sno | meetingDate                          | result
        1   | new DateTime().minusDays(5).toDate() | false
        2   | new DateTime().plusDays(5).toDate()  | true
    }

    def "test deleteRecurrenceMeetings method"() {
        given:
        Meeting.findAllByLinkingId(_) >> [new Meeting(id: 1, meetingTitle: 'abc')]
        String masterId = 5

        when:
        service.deleteRecurrenceMeetings(masterId)

        then:
        notThrown(Exception)
    }

    def "test attendee logic"() {
        when:
        Map map = service.getMeetingAttendeeList(meetingAttendees)

        then:
        map.attendeeList.sort().join(',') == result1.sort().join(',')
        map.guestList.sort().join(',') == result2.sort().join(',')

        where:
        sno | meetingAttendees        | result1      | result2
        1   | "123,tushar,345,saxena" | [123, 345]   | ["tushar", "saxena"]
    }

    @Ignore
    def "test generateICSFile"() {
        given:
        Preference preference = new Preference(timeZone: 'UTC')
        userService.getUser() >> new User(username: 'ownerUser', preference: preference)
        User owner = new User(username: 'ownerUser')
        User attendee = new User(username: 'attendingUser')
        Meeting meeting = new Meeting(meetingTitle: 'test Meeting', meetingDate: new Date() + 1, meetingOwner: owner, attendees: [attendee])

        when:
        File generatedFile = service.generateICSFile(meeting)

        then:
        generatedFile.text != null
    }
    def "test cancelMeetingSeries"() {
        given:
        String masterId = "123"


        User attendee1 = new User(username: "attendee1", createdBy: "system", modifiedBy: "system")
        attendee1.save(failOnError: false)

        Preference attendeePreference = new Preference(locale: Locale.ENGLISH, user: attendee1, createdBy: "system", modifiedBy: "system")
        attendeePreference.save(failOnError: true)
        attendee1.preference = attendeePreference
        attendee1.save(failOnError: true)

        Preference ownerPreference = new Preference(locale: Locale.ENGLISH, user: user, createdBy: "system", modifiedBy: "system")
        ownerPreference.save(failOnError: true)
        user.preference = ownerPreference

        Meeting oldMeeting = new Meeting(meetingTitle: "Test Meeting", duration: 60, meetingOwner: user, meetingDate: new Date() + 1)
        oldMeeting.addToAttendees(attendee1)
        oldMeeting.save(failOnError: false)

        Meeting.metaClass.static.findByLinkingIdAndMeetingDateGreaterThanAndMeetingStatusNotEqual = { String id, Date date, MeetingStatus status ->
            return oldMeeting
        }
        when:
        ResponseDTO response = service.cancelMeetingSeries(masterId)

        then:
        response.status == true
    }
}