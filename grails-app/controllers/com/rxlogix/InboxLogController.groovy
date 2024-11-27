package com.rxlogix

import com.rxlogix.dto.ResponseDTO
import com.rxlogix.exception.ResourceNotFoundException
import com.rxlogix.user.User
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import org.apache.commons.lang3.time.DateUtils

import static java.util.Calendar.MONTH

@Secured(["isAuthenticated()"])
class InboxLogController {
    def userService
    def inboxLogService

    def index() {

        def list = InboxLog.findAllByInboxUserIdAndIsDeleted(userService.getUser().getId(), false)
        def user = userService.getUser()
        String timezone = user?.preference?.getTimeZone()

        def activeFilter = "today"
        Date compareTo = new Date()
        if(params.dueIn == "lastWeek"){
            activeFilter = "lastWeek"
            compareTo = compareTo - 7
        }else if(params.dueIn == "lastMonth"){
            activeFilter = "lastMonth"
            def prevMonth = compareTo[MONTH] - 1
            compareTo.set(month:prevMonth)
        }else if(params.dueIn == "all"){
            activeFilter = "all"
        }
        def  inboxlist= list.findAll() {
            if (params.dueIn) {
                if(params.dueIn == "all"){
                    return it
                }
                if ((DateUtils.truncate(it.createdOn.clone(), Calendar.DAY_OF_MONTH).after(DateUtils.truncate(compareTo, Calendar.DAY_OF_MONTH)) || DateUtils.truncate(it.createdOn.clone(), Calendar.DAY_OF_MONTH) == DateUtils.truncate(compareTo, Calendar.DAY_OF_MONTH)) && !it.isDeleted) {
                    it
                }
            } else {
                if (DateUtils.truncate(it.createdOn.clone(), Calendar.DAY_OF_MONTH) == DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH) && !it.isDeleted) {
                    it
                }
            }
        }.sort{it.createdOn}.collect {
            it.toDTO(timezone)
        }

        inboxlist = inboxlist.reverse()
        [inboxList: inboxlist,activeFilter:activeFilter]
    }

    def list(){
        String timezone = userService.getCurrentUserPreference()?.getTimeZone()
        def list = InboxLog.findAll().collect{
            it.toDTO(timezone)
        }


        render(list as JSON)
    }

    def forUser() {
        Long id = params.long("id")
        if (id) {
            def notificationList = inboxLogService.getUnreadInboxLogs(id, 100)
            respond notificationList, [formats: ['json']]
        }
    }

    def markAsRead() {
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        try {
            def id = params.long('id')
            inboxLogService.changeIsReadStatus(id, true)
        } catch (ResourceNotFoundException e) {
            responseDTO.status = false
            flash.params = "Some error has occured"
        }
        render(responseDTO as JSON)
    }

    def markAsUnread() {
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        try {
            def id = params.long('id')
            inboxLogService.changeIsReadStatus(id, false)
        } catch (ResourceNotFoundException e) {
            responseDTO.status = false
            flash.params = "Some error has occured"
        }
        render(responseDTO as JSON)
    }

    def deleteInboxLog() {
        ResponseDTO responseDTO = new ResponseDTO(status: true)
        try {
            def id = params.long('id')
            inboxLogService.delete(id)
        } catch (ResourceNotFoundException e) {
            responseDTO.status = false
            flash.params = "Some error has occurred"
        }
        render(responseDTO as JSON)
    }

    def deleteNotificationsForUserId() {
        def id = params.id
        if (id) {
            User user
            try {
                user = User.get(id)
                InboxLog.findAllByInboxUserId(id)?.each {
                    it.isDeleted = true
                    it.save(flush: true)
                }
                render true
            } catch (Exception e) {
                log.info("Could not delete notifications for user $user! $e.localizedMessage")
                render false
            }
        }
    }

    def markAsReadNotificationsForUserId(Long id) {
        if (id) {
            User user
            try {
                InboxLog.executeUpdate("update InboxLog set isRead=:isRead where inboxUserId=:id",[isRead:true,id: id])
                render true
            } catch (Exception e) {
                log.info("Could not delete notifications for user $user! $e.localizedMessage")
                render false
            }
        }
    }

    def deleteNotificationById() {
        def id = params.id
        if (id) {
            try {
                def inboxLog = InboxLog.get(id.toInteger())
                inboxLog.isDeleted = true
                inboxLog.save(flush: true)
                render true
            } catch (Exception e) {
                log.info("Could not delete notification! $e.localizedMessage")
                render false
            }
        }
    }

}