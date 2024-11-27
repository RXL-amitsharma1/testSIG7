package com.rxlogix

import com.rxlogix.dto.ResponseDTO
import com.rxlogix.signal.EvdasHistory
import grails.converters.JSON
import com.rxlogix.config.EvdasAlert
import com.rxlogix.config.ActivityType
import com.rxlogix.config.ActivityTypeValue
import com.rxlogix.user.User
import grails.plugin.springsecurity.annotation.Secured

@Secured(["isAuthenticated()"])
class EvdasHistoryController {

    def CRUDService
    def activityService
    def userService
    def cacheService

    def index() {}

    def listEvdasHistory(Long alertConfigId, Long alertId, String productName, String eventName) {
        String userTimezone = userService.getCurrentUserPreference()?.timeZone ?: Constants.UTC

        List evdasHistoryList = []
        try {
            List<EvdasHistory> list = []

            if(params.executionDate) {
                list = EvdasHistory.createCriteria().list {
                    eq('productName', productName)
                    eq('eventName', eventName)
                    eq('configId', alertConfigId)
                    isNotNull('executionDate')
                    order('executionDate', 'desc')
                }
            } else {
                list = EvdasHistory.createCriteria().list {
                    eq('productName', productName)
                    eq('eventName', eventName)
                    eq('configId', alertConfigId)
                    isNotNull('change')
                    order('id', 'desc')
                }
            }

            list?.collect {
                evdasHistoryList.add(it.toDto(userTimezone))
            }
        } catch (Throwable th) {
            th.printStackTrace()
            log.error(th.getMessage())
        }
        respond evdasHistoryList, [formats: ['json']]
    }

    void setEvdasAlertIdForEvdasHistory(EvdasHistory evdasHistory, Long alertId) {
        evdasHistory.evdasAlertId = alertId
        evdasHistory.save(flush: true)
    }

    def updateJustification(Long id, String newJustification,Long selectedAlertId) {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            EvdasHistory evdasHistory = EvdasHistory.get(id)
            String oldJustification = evdasHistory.justification
            evdasHistory.justification = newJustification
            CRUDService.saveWithoutAuditLog(evdasHistory)
            createActivityForJustificationChange(evdasHistory, oldJustification, selectedAlertId)
        } catch (Exception e) {
            e.printStackTrace()
            responseDTO.status = false
            responseDTO.message = message(code: "app.label.caseHistory.justification.change.error")
        }
        render(responseDTO as JSON)
    }

    void createActivityForJustificationChange(EvdasHistory evdasHistory, String oldJustification, Long selectedAlertId) {
        EvdasAlert alert = EvdasAlert.get(selectedAlertId)
        if(alert){
            User currentUser = userService.getUser()
            String textInfo = evdasHistory.change == Constants.HistoryType.DISPOSITION ? "Disposition (${evdasHistory?.disposition?.displayName})" : evdasHistory.change == Constants.HistoryType.PRIORITY ? "Priority (${evdasHistory?.priority?.displayName})" : ''
            String details = "Justification for ${textInfo} changed from '$oldJustification' to '${evdasHistory.justification}'"
            ActivityType activityType = ActivityType.findByValue(ActivityTypeValue.JustificationChange)
            activityService.createActivityForEvdas(alert.executedAlertConfiguration, activityType, currentUser, details, '', ['For EVDAS Alert'],
                    alert.substance, alert.pt, alert.assignedTo, null, alert.assignedToGroup)
        }
    }
}
