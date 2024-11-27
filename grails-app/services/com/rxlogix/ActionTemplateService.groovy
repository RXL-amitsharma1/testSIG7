package com.rxlogix

import com.rxlogix.config.Action
import com.rxlogix.config.ActionTemplate
import com.rxlogix.enums.ActionStatus
import com.rxlogix.user.User
import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import groovy.time.TimeCategory

@Transactional
class ActionTemplateService {

    def cacheService

    def addActionsFromTemplate(Long actionTemplateId,String alertType,def domainObj, Boolean isBusinessConfiguration = false) {

        if (actionTemplateId) {
            ActionTemplate actionTemplate = cacheService.getActionTemplateCache(actionTemplateId)

            if (actionTemplate) {
                Action action = new Action()
                if (alertType in [Constants.AlertConfigType.AGGREGATE_CASE_ALERT, Constants.AlertConfigType.EVDAS_ALERT, Constants.AlertConfigType.SINGLE_CASE_ALERT]) {
                    action.execConfigId = domainObj?.executedAlertConfiguration?.id // call for alerts
                } else {
                    action.execConfigId = domainObj.id //block for signals
                }

                JsonSlurper jsonSlurper = new JsonSlurper()

                def actionProperties = jsonSlurper.parseText(actionTemplate.actionJson)

                User assignedUser = cacheService.getUserByUserNameIlike(actionProperties.assignedTo)

                action.config = cacheService.getActionConfigurationCache(Long.parseLong(actionProperties.actionConfigId))
                action.type = cacheService.getActionTypeCache(Long.parseLong(actionProperties.actionTypeId))
                action.comments = actionProperties.comments
                action.details = actionProperties.details
                action.createdDate = new Date()
                if (assignedUser) {
                    action.assignedTo = assignedUser
                    action.owner = assignedUser
                }else{
                    action.guestAttendeeEmail = actionTemplate.guestAttendeeEmail
                }

                if (actionProperties.dueIn) {
                    use(TimeCategory) {
                        Date start = new Date()
                        action.dueDate = start + Integer.parseInt(actionProperties.dueIn)
                    }
                } else {
                    action.dueDate = null
                }

                action.alertType = alertType
                action.createdDate = new Date()
                action.actionStatus = ActionStatus.New
                action.skipAudit = true
                domainObj.action.add(action)
            }
        }
        domainObj
    }

}
