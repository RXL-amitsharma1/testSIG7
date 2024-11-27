package com.rxlogix.config

import com.rxlogix.dto.AlertLevelDispositionDTO
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.events.EventPublisher
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.hibernate.Session
import org.joda.time.DateTime

@Transactional
class LiteratureActivityService implements  EventPublisher{
    def sessionFactory

    @Transactional
    def createLiteratureActivity(ExecutedLiteratureConfiguration executedLiteratureConfiguration, ActivityType type, User loggedInUser,
                                 String details, String justification, String product, String event,
                                 User assignedToUser, String searchString, Integer articleId, Group assignToGroup = null, String guestAttendeeEmail = null , String privateUserName = null) {
        if(executedLiteratureConfiguration) {
            LiteratureActivity literatureActivity = new LiteratureActivity(
                    type: type,
                    details: details,
                    timestamp: DateTime.now(),
                    justification: justification,
                    productName: product,
                    eventName: event,
                    articleId: articleId,
                    searchString : searchString,
                    privateUserName : privateUserName
            )
            if (loggedInUser) {
                literatureActivity.performedBy = loggedInUser
            }
            if (assignedToUser) {
                literatureActivity.assignedTo = assignedToUser
            } else if (assignToGroup) {
                literatureActivity.assignedToGroup = assignToGroup
            } else
                literatureActivity.guestAttendeeEmail = guestAttendeeEmail
            literatureActivity.setExecutedConfiguration(executedLiteratureConfiguration)
            literatureActivity.save(flush: true)
        }
    }

    LiteratureActivity createLiteratureActivityAlertLevelDisposition(Map alertMap, AlertLevelDispositionDTO alertLevelDispositionDTO){
        LiteratureActivity literatureActivity = new LiteratureActivity(
                type: alertLevelDispositionDTO.activityType,
                details: alertMap.details,
                timestamp: DateTime.now(),
                justification: alertLevelDispositionDTO.justificationText,
                productName: alertMap.attrs.productName,
                eventName: alertMap.attrs.pt,
                articleId: alertMap.articleId,
                searchString : alertMap.searchString
        )
        if (alertLevelDispositionDTO.loggedInUser) {
            literatureActivity.performedBy =  alertLevelDispositionDTO.loggedInUser
        }
        if (alertMap.assignedTo) {
            literatureActivity.assignedTo = alertMap.assignedTo
        } else {
            literatureActivity.assignedToGroup = alertMap.assignedToGroup
        }
        literatureActivity.setExecutedConfiguration(alertMap.exLitSearchConfig)
        literatureActivity
    }

    @Transactional
    def createLiteratureActivityForSignal(ExecutedLiteratureConfiguration executedLiteratureConfiguration, ActivityType activityType,
                                          User loggedInUser, String details, String justification, User assignedToUser, String searchString,
                                          Integer articleId, Group assignedToGroup = null,String productName,String eventName) {
        LiteratureActivity literatureActivity = new LiteratureActivity(
                type: activityType,
                timestamp: DateTime.now(),
                justification: justification,
                details: details,
                articleId: articleId,
                searchString: searchString,
                productName:productName,
                eventName:eventName
        )
        if (loggedInUser) {
            literatureActivity.performedBy = loggedInUser
        }
        if (assignedToUser) {
            literatureActivity.assignedTo = assignedToUser
        } else {
            literatureActivity.assignedToGroup = assignedToGroup
        }
        literatureActivity.setExecutedConfiguration(executedLiteratureConfiguration)
        literatureActivity.save(flush: true)
    }

    void batchPersistAlertLevelActivity(activityList) {

        LiteratureActivity.withTransaction {
            def batch = []
            for (LiteratureActivity activity : activityList) {
                batch += activity
                Session session = sessionFactory?.currentSession?:sessionFactory?.openSession()
                if (batch.size() > Holders.config.signal.batch.size) {
                    for (LiteratureActivity act in batch) {
                        act.save(validate: false)
                    }
                    session.flush()
                    session.clear()
                    batch.clear()
                }
            }

            if (batch) {
                try {
                    Session session = sessionFactory?.currentSession?:sessionFactory?.openSession()
                    for (LiteratureActivity act in batch) {
                        act.save(validate: false)
                    }
                    session.flush()
                    session.clear()
                } catch (Throwable th) {
                    th.printStackTrace()
                }
            }
        }
    }
}
