package com.rxlogix

import com.rxlogix.signal.SignalHistory
import com.rxlogix.signal.ValidatedSignal
import grails.gorm.transactions.Transactional

@Transactional
class SignalHistoryService {

    def CRUDService

    def saveSignalHistory(signalHistoryMap) {
            def change = signalHistoryMap.change
            def existingEvdasHistory = getLatestSignalHistory(signalHistoryMap.validatedSignal)
            def signalHistory = new SignalHistory(signalHistoryMap)
            signalHistory.dateCreated = new Date()
            signalHistory.lastUpdated = new Date()
        if (existingEvdasHistory) {
                signalHistory.properties = existingEvdasHistory.properties
                if (change == Constants.HistoryType.DISPOSITION) {
                    signalHistory.disposition = signalHistoryMap.disposition
                } else if (change == Constants.HistoryType.PRIORITY) {
                    signalHistory.priority = signalHistoryMap.priority
                } else if (change == Constants.HistoryType.ASSIGNED_TO) {
                    signalHistory.assignedTo = signalHistoryMap.assignedTo
                } else if (change == Constants.HistoryType.GROUP) {
                    signalHistory.group = signalHistoryMap.group
                }
                signalHistory.change = existingEvdasHistory.change
                signalHistory.dueDate = signalHistoryMap.dueDate
                //Set the status of existing signal history false and persist it.
                existingEvdasHistory.isLatest = false
                if (signalHistory.change) {
                    CRUDService.saveWithoutAuditLog(signalHistory)
                }
            } else {
                signalHistory.change = Constants.HistoryType.FIRST_EXECUTION
            }
            signalHistory.justification = signalHistoryMap.justification
            signalHistory.validatedSignal = signalHistoryMap.validatedSignal
            signalHistory.isLatest = true
            CRUDService.saveWithoutAuditLog(signalHistory)
    }

    def getLatestSignalHistory(ValidatedSignal validatedSignal) {
        SignalHistory.findByValidatedSignalAndIsLatest(validatedSignal, true)
    }
}
