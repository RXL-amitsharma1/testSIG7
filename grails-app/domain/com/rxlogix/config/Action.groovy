package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.audit.AuditTrail
import com.rxlogix.enums.ActionStatus
import com.rxlogix.signal.AdHocAlert
import com.rxlogix.signal.Alert
import com.rxlogix.signal.SingleCaseAlert
import com.rxlogix.signal.ValidatedSignal
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import grails.util.Holders
import org.apache.commons.lang3.time.DateUtils

import java.text.SimpleDateFormat

import static com.rxlogix.util.DateUtil.toDateTimeString

class Action {
    static transients = ['skipAudit']
    static auditable = ['ignore':['execConfigId','notificationDate','viewed', 'createdDate', 'owner']]

    def auditLogService
    def grailsApplication
    def userService
    def grailsLinkGenerator
    def actionService

    String details
    Date createdDate
    Date dueDate
    Date completedDate
    User assignedTo
    Group assignedToGroup
    User owner
    String comments
    Boolean viewed = false
    String actionStatus
    String alertType
    String meetingId
    ActionConfiguration config
    ActionType type
    String guestAttendeeEmail
    String notificationDate
    Long execConfigId
    Boolean skipAudit = false

    static constraints = {
        createdDate nullable: false
        dueDate nullable: false
        completedDate nullable: true, validator: { value, obj ->
            def result = true
            if (!obj.completedDate) {
                result = obj.actionStatus != ActionStatus.Closed.name() ? true : 'com.rxlogix.config.Action.completedDate.nullable'
            } else {
                Date today = new Date()
                Date tomorrow = new Date(today.getTime() + (1000 * 60 * 60 * 24));
                result = (DateUtils.truncate(obj.completedDate, Calendar.DAY_OF_MONTH)) < (DateUtils.truncate(tomorrow, Calendar.DAY_OF_MONTH)) ? true : 'com.rxlogix.config.Action.completedDate.future'
            }
            return result
        }
        owner nullable: true
        comments nullable: true, maxSize: 8000
        type nullable: false
        config nullable: false
        viewed nullable: true
        details maxSize: 8000
        alertType nullable: true
        meetingId nullable: true
        assignedTo nullable: true, validator: { value, obj ->
            def result = true
            if (!obj.assignedTo) {
                result = obj.assignedToGroup ? true : 'assignedTo.nullable'
                if (!obj.assignedToGroup) {
                    result = obj.guestAttendeeEmail ? true : 'assignedTo.nullable'
                }
            }
            return result
        }
        assignedToGroup nullable: true
        guestAttendeeEmail nullable: true, email: true
        notificationDate nullable: true
        execConfigId nullable: true
    }

    static mapping = {
        table('ACTIONS')
    }

    def brief() {
        if (details && details.length() > 30) {
            details.substring(0, 30) + '...'
        } else
            details
    }


    def toDTO(String timezone = "UTC") {
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATEPICKER_FORMAT)

        [
                id                    : this.id,
                createdDate           : this.createdDate ? DateUtil.toDateStringWithTimeInAmFormat(this.createdDate, timezone) : null,
                dueDate               : this.dueDate ?sdf.format(this.dueDate)  : "",
                completedDate : this.completedDate ? sdf.format(this.completedDate): "",                assignedTo            : this.assignedTo?.fullName,
                assignedToObj         : this.assignedTo ? this.assignedTo.toMap() : this.assignedToGroup ? this.assignedToGroup.toMap() : [id: this.guestAttendeeEmail, fullName: this.guestAttendeeEmail],
                assignedToUser        : userService.getAssignToValue(this) == 'UserGroup_null' ? this.guestAttendeeEmail : userService.getAssignToValue(this),
                owner                 : this.owner,
                searchUserGroupListUrl: grailsLinkGenerator.link(controller: 'user', action: 'searchUserGroupList'),
                comments              : this.comments,
                alertType             : this.alertType,
                type                  : this.type?.displayName,
                typeObj               : this.type?.toMap(),
                config                : this.config?.displayName,
                configObj             : this.config?.toMap(),
                viewed                : this.viewed,
                details               : this.details,
                actionStatus          : this.actionStatus,
                meetingId             : this.meetingId
        ]
    }

    @Override
    String toString() {
        "$details"
    }

    def getInstanceIdentifierForAuditLog() {
        String identifier = ""
        switch (alertType) {
            case Constants.AlertConfigType.AGGREGATE_CASE_ALERT:
            case Constants.AlertConfigType.SINGLE_CASE_ALERT:
                identifier = ExecutedConfiguration.get(execConfigId)?.getInstanceIdentifierForAuditLog()
                break

            case Constants.AlertConfigType.EVDAS_ALERT:
                identifier = ExecutedEvdasConfiguration.get(execConfigId)?.getInstanceIdentifierForAuditLog()
                break

            case Constants.AlertConfigType.LITERATURE_SEARCH_ALERT:
                identifier = ExecutedLiteratureConfiguration.get(execConfigId)?.getInstanceIdentifierForAuditLog()
                break

            case Constants.AlertConfigType.AD_HOC_ALERT:
                identifier = AdHocAlert.get(execConfigId)?.getInstanceIdentifierForAuditLog()
                break

            case Constants.AlertConfigType.SIGNAL_MANAGEMENT:
                if (execConfigId != null) {
                    identifier = ValidatedSignal.get(execConfigId as Long)?.getInstanceIdentifierForAuditLog()
                }
                break
            default:
                return this.id
        }
        identifier = identifier + ": ${this.id}"
        return identifier
    }

    def getModuleNameForMultiUseDomains() {
        switch (alertType) {
            case Constants.AlertConfigType.AGGREGATE_CASE_ALERT:
                if(ExecutedConfiguration.get(execConfigId).isLatest){
                    return Constants.AuditLog.AGGREGATE_REVIEW + ": " + Constants.AuditLog.ACTION
                }else{
                    return Constants.AuditLog.AGGREGATE_REVIEW + ": Archived Alert: " + Constants.AuditLog.ACTION
                }
                break

            case Constants.AlertConfigType.SINGLE_CASE_ALERT:
                if(ExecutedConfiguration.get(execConfigId).isLatest){
                    return Constants.AuditLog.SINGLE_REVIEW + ": " + Constants.AuditLog.ACTION
                }else{
                    return Constants.AuditLog.SINGLE_REVIEW + ": Archived Alert: " + Constants.AuditLog.ACTION
                }
                break

            case Constants.AlertConfigType.EVDAS_ALERT:
                if( ExecutedEvdasConfiguration.get(execConfigId).isLatest){
                    return Constants.AuditLog.EVDAS_REVIEW + ": " + Constants.AuditLog.ACTION
                }else{
                    return Constants.AuditLog.EVDAS_REVIEW + ": Archived Alert: " + Constants.AuditLog.ACTION
                }
                break

            case Constants.AlertConfigType.LITERATURE_SEARCH_ALERT:
                if( ExecutedLiteratureConfiguration.get(execConfigId).isLatest){
                    return Constants.AuditLog.LITERATURE_REVIEW + ": " + Constants.AuditLog.ACTION
                }else{
                    return Constants.AuditLog.LITERATURE_REVIEW + ": Archived Alert: " + Constants.AuditLog.ACTION
                }
                break

            case Constants.AlertConfigType.SIGNAL_MANAGEMENT:
                return Constants.AlertConfigType.SIGNAL + ": " + Constants.AuditLog.ACTION
                break
            case Constants.AlertConfigType.AD_HOC_ALERT:
                return Constants.AuditLog.ADHOC_REVIEW + ": " + Constants.AuditLog.ACTION
                break
            default:
                return Constants.AuditLog.ACTION
        }
    }


    String assignedToValue(){
        return userService.getAssignToValue(this) == 'UserGroup_null' ? this.guestAttendeeEmail : userService.getAssignToValue(this)
    }


}