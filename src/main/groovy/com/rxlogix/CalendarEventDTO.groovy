package com.rxlogix

import com.rxlogix.config.Action
import com.rxlogix.config.Configuration
import com.rxlogix.config.Meeting
import com.rxlogix.util.DateUtil

import java.text.SimpleDateFormat

class CalendarEventDTO {

    Long id
    String title
    ColorCodeEnum colorCode
    boolean allDay = false
    ColorCodeEnum textColorCode
    EventType eventType
    Date startDate
    Date endDate

    Map toMap() {
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATEPICKER_UTC_FORMAT)
        Map map = [
                id: id, title: title, color: colorCode.code,
                allDay: allDay, textColor: textColorCode.code,
                eventType: eventType.name(), startDate: sdf.format(startDate),
                endDate: sdf.format(endDate)
        ]
        return map
    }

    static CalendarEventDTO getEvent(Configuration configuration, alertType, color) {
        CalendarEventDTO eventDTO = new CalendarEventDTO()
        eventDTO.with {
            id = configuration.id
            title = configuration.name
            eventType = alertType
            colorCode = color
            allDay = false
            textColorCode = ColorCodeEnum.TEXT_COLOR_WHITE
            startDate = configuration.nextRunDate
            endDate = configuration.nextRunDate
        }
        return eventDTO
    }

    static CalendarEventDTO getActionEvent(Action action, alertType, color) {
        CalendarEventDTO eventDTO = new CalendarEventDTO()
        eventDTO.with {
            id = action.id
            title = action.type
            eventType = alertType
            colorCode = color
            allDay = false
            textColorCode = ColorCodeEnum.TEXT_COLOR_WHITE
            startDate = action.dueDate ? new Date(DateUtil.toDateString(action.dueDate,Constants.UTC)) :null
            endDate = action.dueDate ? new Date(DateUtil.toDateString(action.dueDate,Constants.UTC)) :null

        }
        return eventDTO
    }

    static CalendarEventDTO getMeetingEvent(Meeting meeting, alertType, color) {
        CalendarEventDTO eventDTO = new CalendarEventDTO()
        eventDTO.with {
            id = meeting.id
            title = meeting.meetingTitle
            eventType = alertType
            colorCode = color
            allDay = false
            textColorCode = ColorCodeEnum.TEXT_COLOR_WHITE
            startDate = meeting.meetingDate
            endDate = meeting.meetingDate
        }
        return eventDTO
    }

    static enum EventType {
        SINGLE_CASE_ALERT,
        MEETING,
        AGGREGATE_CASE_ALERT,
        EXECUTED_PERIODIC_REPORT,
        SCHEDULED_PERIODIC_REPORT,
        REPORT_REQUEST,
        ACTION_ITEM
    }


    static enum ColorCodeEnum {
        SINGLE_CASE_ALERT_COLOR("#3b789e"),
        AGGREGATE_CASE_ALERT_COLOR("#acd3e1"),
        ACTION_ITEM("#FF99F0"),
        MEETING_ITEM("#45b25f"),
        TEXT_COLOR_DARK("#000000"),
        TEXT_COLOR_LIGHT("#333333"),
        TEXT_COLOR_WHITE("#ffffff")

        String code

        ColorCodeEnum(String code) {
            this.code = code
        }
    }

}
