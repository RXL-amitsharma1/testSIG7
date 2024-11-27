package com.rxlogix

import com.rxlogix.config.Action
import com.rxlogix.config.Configuration
import com.rxlogix.config.Meeting
import com.rxlogix.enums.GroupType
import com.rxlogix.enums.MeetingStatus
import com.rxlogix.user.User
import com.rxlogix.user.Group
import grails.gorm.transactions.Transactional
import org.hibernate.criterion.CriteriaSpecification
import org.hibernate.sql.JoinType

@Transactional(readOnly = true)
class CalendarService {

    def configurationService
    def userService
    def productBasedSecurityService

    /**
     * The method to render the events.
     * @return event json.
     */
    List<CalendarEventDTO> events(User user, Date start, Date end) {
        Set<Group> allGroups = user.allGroups(GroupType.USER_GROUP)
        List<CalendarEventDTO> calendarEventDTOs = []

        calendarEventDTOs.addAll(getAlerts(user, start, end,
                Constants.AlertConfigType.AGGREGATE_CASE_ALERT, CalendarEventDTO.EventType.AGGREGATE_CASE_ALERT,
                CalendarEventDTO.ColorCodeEnum.AGGREGATE_CASE_ALERT_COLOR))
        calendarEventDTOs.addAll(getAlerts(user, start, end,
                Constants.AlertConfigType.SINGLE_CASE_ALERT, CalendarEventDTO.EventType.SINGLE_CASE_ALERT,
                CalendarEventDTO.ColorCodeEnum.SINGLE_CASE_ALERT_COLOR))
        calendarEventDTOs.addAll(getActionItems(user, start, end,
                CalendarEventDTO.EventType.ACTION_ITEM,
                CalendarEventDTO.ColorCodeEnum.ACTION_ITEM))

        calendarEventDTOs.addAll(getMeetings(user, start, end, CalendarEventDTO.EventType.MEETING,
                CalendarEventDTO.ColorCodeEnum.MEETING_ITEM))

        allGroups.each {
            calendarEventDTOs.addAll(getAlertsForGroup(it, start, end,
                    Constants.AlertConfigType.AGGREGATE_CASE_ALERT, CalendarEventDTO.EventType.AGGREGATE_CASE_ALERT,
                    CalendarEventDTO.ColorCodeEnum.AGGREGATE_CASE_ALERT_COLOR))

            calendarEventDTOs.addAll(getAlertsForGroup(it, start, end,
                    Constants.AlertConfigType.SINGLE_CASE_ALERT, CalendarEventDTO.EventType.SINGLE_CASE_ALERT,
                    CalendarEventDTO.ColorCodeEnum.SINGLE_CASE_ALERT_COLOR))

            calendarEventDTOs.addAll(getActionItemsForGroup(it, start, end,
                    CalendarEventDTO.EventType.ACTION_ITEM,
                    CalendarEventDTO.ColorCodeEnum.ACTION_ITEM))
        }
        return calendarEventDTOs
    }

    private List<CalendarEventDTO> getAlerts(User user, Date startDate, Date endDate, domainName, alertType, color) {
        List<CalendarEventDTO> events = []

        def result = Configuration.createCriteria().list {
            eq("assignedTo", user)
            eq("type", domainName)
            gte('nextRunDate', startDate)
            le('nextRunDate', endDate)
        }
        result.each {
            if (!it.isDeleted) {
                events.add(CalendarEventDTO.getEvent(it, alertType, color))
            }
        }
        return events
    }

    private List<CalendarEventDTO> getActionItems(user, Date startDate, Date endDate, alertType, color) {
        List<CalendarEventDTO> events = []
        def result = Action.createCriteria().list {
            eq("assignedTo", user)
            gte('dueDate', startDate)
            le('dueDate', endDate)
            inList('actionStatus', ['New', 'InProgress', 'ReOpened'])
        }
        result.each {
            events.add(CalendarEventDTO.getActionEvent(it, alertType, color))
        }
        return events
    }

    private List<CalendarEventDTO> getMeetings(user, Date startDate, Date endDate, alertType, color) {
        List<CalendarEventDTO> events = []

        Set result = Meeting.createCriteria().list {
            or {
                eq("meetingOwner", user)
                'attendees'(CriteriaSpecification.LEFT_JOIN) {
                    eq "id", user.id
                }
            }
            gte('meetingDate', startDate)
            le('meetingDate', endDate)
            eq('meetingStatus', MeetingStatus.SCHEDULED)
        }
        result.each {
            events.add(CalendarEventDTO.getMeetingEvent(it, alertType, color))
        }
        return events
    }

    private List<CalendarEventDTO> getAlertsForGroup(Group group, Date startDate, Date endDate, domainName, alertType, color) {
        List<CalendarEventDTO> events = []
        def result = Configuration.createCriteria().list {
            eq("assignedToGroup", group)
            eq("type", domainName)
            gte('nextRunDate', startDate)
            le('nextRunDate', endDate)
        }
        result.each {
            if (!it.isDeleted) {
                events.add(CalendarEventDTO.getEvent(it, alertType, color))
            }
        }
        return events
    }

    private List<CalendarEventDTO> getActionItemsForGroup(Group group, Date startDate, Date endDate, alertType, color) {

        List<CalendarEventDTO> events = []
        def result = Action.createCriteria().list {
            eq("assignedToGroup", group)
            gte('dueDate', startDate)
            le('dueDate', endDate)
            inList('actionStatus', ['New', 'InProgress', 'ReOpened'])
        }
        result.each {
            events.add(CalendarEventDTO.getActionEvent(it, alertType, color))
        }
        return events
    }
}
