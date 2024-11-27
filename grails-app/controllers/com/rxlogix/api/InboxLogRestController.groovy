package com.rxlogix.api

import com.rxlogix.ExceptionHandlingController
import com.rxlogix.InboxLogService
import com.rxlogix.UserService
import com.rxlogix.exception.InvalidPayloadException
import com.rxlogix.security.Authorize
import com.rxlogix.util.RestApiResponse
import grails.converters.JSON

import java.time.Instant
import java.util.function.Supplier

import static java.util.Calendar.MONTH

@Authorize
class InboxLogRestController implements ExceptionHandlingController {
    private static final int DEFAULT_LIMIT = 100
    UserService userService
    InboxLogService inboxLogService

    def getInboxLogs() {
        def user = userService.getUser()
        def timezone = user.preference?.getTimeZone()
        def activeFilter = InboxLogFilter.fromValue(params.period)
                .orElse(InboxLogFilter.TODAY)
        def inboxLogs = inboxLogService.getInboxLogs(user.id, activeFilter.fromDate())
                .collect { it.toDTO(timezone) }
                .reverse()

        render RestApiResponse.successResponseWithData([inboxList: inboxLogs, activeFilter: activeFilter.value]) as JSON
    }

    def getUnreadInboxLogs() {
        def userId = params.long('userId')
        if (!userId) {
            throw new InvalidPayloadException('userId must not be null')
        }
        def limit = params.int('limit') ?: DEFAULT_LIMIT
        def notifications = inboxLogService.getUnreadInboxLogs(userId, limit)
        render RestApiResponse.successResponseWithData(notifications) as JSON
    }

    def changeIsReadStatus(Long id) {
        def isRead = request.JSON.isRead as Boolean
        if (isRead == null) {
            throw new InvalidPayloadException('isRead must not be null')
        }
        inboxLogService.changeIsReadStatus(id, isRead)
        render RestApiResponse.successResponse() as JSON
    }

    def markAllAsRead(Long userId) {
        inboxLogService.markNotificationsAsRead(userId)
        render RestApiResponse.successResponse() as JSON
    }

    def deleteInboxLog(Long id) {
        inboxLogService.delete(id)
        render RestApiResponse.successResponse() as JSON
    }

    private enum InboxLogFilter {
        TODAY('today', { -> new Date().clearTime() }),
        LAST_WEEK('lastWeek', { -> new Date().clearTime() - 7 }),
        LAST_MONTH('lastMonth', { ->
            def date = new Date().clearTime()
            date.set(month: date[MONTH] - 1)
            date
        }),
        ALL('all', { -> Date.from(Instant.EPOCH) })

        InboxLogFilter(String value, Supplier dateSupplier) {
            this.value = value
            this.dateSupplier = dateSupplier
        }

        private String value
        private Supplier<Date> dateSupplier

        Date fromDate() {
            dateSupplier.get()
        }

        static Optional<InboxLogFilter> fromValue(String value) {
            Optional.ofNullable(values().find { it.value == value })
        }
    }
}
