package com.rxlogix

import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import grails.util.Holders
import groovy.json.JsonOutput

@Secured(["isAuthenticated()"])
class CalendarController {

    def calendarService
    def userService
    def alertService

    def index() {
        Map actionTypeAndActionMap = alertService.getActionTypeAndActionMap()
        List userList = User.list().sort { it.fullName?.toLowerCase() }.collect {
            [id: it.id, fullName: it.fullName]
        }
        [userList           : userList,
         actionPropertiesMap: JsonOutput.toJson(actionTypeAndActionMap.actionPropertiesMap),
         actionConfigList: actionTypeAndActionMap.actionPropertiesMap.configs,
         actionTypeList : actionTypeAndActionMap.actionTypeList]
    }

    def events() {
        User user = userService.getUser()
        String userTimeZone = user.preference?.timeZone
        Date startDate = DateUtil.getStartDate(params.start.toString(), userTimeZone)
        Date endDate = DateUtil.getEndDateForCalendar(params.end.toString(), userTimeZone)
        List<CalendarEventDTO> eventsArray = calendarService.events(user, startDate, endDate)
        render eventsArray*.toMap() as JSON
    }

}
