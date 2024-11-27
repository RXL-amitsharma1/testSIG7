package com.rxlogix

import com.rxlogix.config.ActivityType
import com.rxlogix.config.ActivityTypeValue
import com.rxlogix.config.Disposition
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.PVSState
import com.rxlogix.config.Priority
import com.rxlogix.signal.AdHocAlert
import com.rxlogix.signal.AggregateCaseAlert
import com.rxlogix.signal.Alert
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil

import static com.rxlogix.util.MiscUtil.calcDueDate

trait Alertbililty implements AlertUtil {


    def getById(id) { Alert.findById(id)}

    def updateState(rowId, newValue, isAggAlert) {
        def alert = null
        if (isAggAlert) {
            alert = AggregateCaseAlert.findById(rowId)
        } else {
            alert = Alert.findById(rowId)
        }
        def newState = PVSState.findByValue(newValue)

        if (alert.workflowState != newState) {
            alert.workflowState = newState
            alert.save()
        }
    }

    def updateDisposition(rowId, Disposition newDisposition, Boolean isAggAlert) {
        def alert = null
        if (isAggAlert) {
            alert = AggregateCaseAlert.findById(rowId)
        } else {
            alert = Alert.findById(rowId)
        }

        if (newDisposition != alert.disposition?.value) {
            alert.disposition = newDisposition
            alert.save()
        }
    }

    def updatePriority(rowId, newValue, isAggAlert) {
        def alert = null
        if (isAggAlert) {
            alert = AggregateCaseAlert.findById(rowId)
        } else {
            alert = Alert.findById(rowId)
        }
        def priority = Priority.findByValue(newValue)

        if (newValue != alert.priority?.value) {
            alert.priority = priority
            calcDueDate(alert, priority, alert.disposition)
            alert.save()
        }
    }

    def toggleFlag(id) {
        def alert = Alert.findById(id)
        def orgValue = alert.flagged
        alert.flagged = !orgValue
        alert.save()

        alert.flagged
    }

    def flag(id, value) {
        def alert = Alert.findById(id)
        alert.flagged = value
        alert.save()

        value
    }

    def changeAssignedTo(alert, userId) {
        def newUser = User.findById(userId)

        if (newUser != alert.assignedTo) {
            alert.assignedTo = newUser
            alert.save()
            newUser
        } else
            null
    }

    List alertCountsByDisposition(List alertList) {
        List data = []
        Set<Disposition> dispositions = alertList.collect { it.disposition } as Set<Disposition>
        dispositions = dispositions.sort { it.id }
        dispositions.each { Disposition disposition ->
            Integer count = alertList.findAll { it.disposition == disposition && !it.disposition.closed }.size()
            if (count) {
                data << [disposition.displayName, count]
            }
        }
        data
    }

    ExecutedConfiguration getExecConfigurationById(Long configId) {
        ExecutedConfiguration.get(configId)
    }

    ActivityType getActivityByType(ActivityTypeValue type) {
        ActivityType.findByValue(type)
    }

    def getAllowedAlerts(domain, allowedProductsToUser, currentUser) {
        def alertList = domain.withCriteria {
            'in'("productName", allowedProductsToUser)
        }
        alertList
    }

    def getAllowedAlertsByDisposition(domain, allowedProductsToUser) {
        def alertList = domain.withCriteria {
            'in'("productName", allowedProductsToUser)
            'disposition'{
                'eq'('closed',false)

            }
        }
        alertList
    }

}