package com.rxlogix

import com.rxlogix.cache.CacheService
import com.rxlogix.exception.ResourceNotFoundException
import com.rxlogix.user.User
import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import org.springframework.context.MessageSource

@Slf4j
@Transactional
class InboxLogService {
    MessageSource messageSource
    CacheService cacheService

    List<InboxLog> getInboxLogs(Long userId, Date fromDate) {
        log.info('Retrieving Inbox Logs for user {} from {}', userId, fromDate)
        return InboxLog.findAllByInboxUserIdAndIsDeletedAndCreatedOnGreaterThanEquals(userId, false, fromDate)
                .sort { it.createdOn }
    }

    List<Map<String, Object>> getUnreadInboxLogs(Long userId, int limit) {
        return InboxLog.createCriteria().list {
            eq("inboxUserId", userId)
            eq("isRead", false)
            eq("isDeleted", false)
            order("createdOn", "desc")
            maxResults(limit)
        }.collect {
            def val = [it.messageArgs]
            User user = cacheService.getUserByUserId(userId)
            Locale locale = cacheService.getPreferenceByUserId(user.id)?.locale
            String message
            if (it.type == Constants.SignalHistory.SIGNAL_TYPE)
                message = Constants.SignalHistory.SIGNAL_CREATED
            else message = (it?.executedConfigId) ? messageSource.getMessage(it.message, val.toArray(), locale) : it.subject
            [id  : it.id, message: message, type: it.type, content: it.content,
             user: user, createdOn: it.createdOn, level: it.level?.name, executedConfigId: it.executedConfigId, detailUrl: it.detailUrl]
        }
    }

    void changeIsReadStatus(Long id, boolean isRead) {
        def inboxLog = fetchInboxLog(id)
        inboxLog.isRead = isRead
        inboxLog.isNotification = false
        inboxLog.save(flush: true)
    }

    void markNotificationsAsRead(Long userId) {
        if (!User.exists(userId)) {
            throw ResourceNotFoundException.withMessageCode('restApi.error.notFound.user',
                    "User with id ${userId} not found")
        }
        def query = "update InboxLog set isRead=:isRead where inboxUserId=:id"
        InboxLog.executeUpdate(query, [isRead: true, id: userId])
    }

    void delete(Long id) {
        def inboxLog = fetchInboxLog(id)
        inboxLog.isDeleted = true
        inboxLog.isNotification = false
        inboxLog.save(flush: true)
    }

    private InboxLog fetchInboxLog(Long id) {
        return Optional.ofNullable(InboxLog.findById(id))
                .orElseThrow { ->
                    throw ResourceNotFoundException.withMessageCode('restApi.error.notFound.inboxLog',
                            "InboxLog entry with id ${id} not found")
                }
    }
}
