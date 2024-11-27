package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.signal.Alert
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.DbUtil
import grails.util.Holders
import groovy.json.JsonSlurper
import org.apache.commons.lang3.StringUtils

import java.text.SimpleDateFormat


class Activity implements Serializable {

    def userService

    static auditable = false
    Long id

    ActivityType type
    String details
    User performedBy
    Date timestamp
    String ipAddress
    String clientName
    Alert alert
    Boolean display = Boolean.TRUE
    String justification
    String attributes
    String caseNumber
    String suspectProduct
    String eventName
    User assignedTo
    Group assignedToGroup
    String guestAttendeeEmail
    String privateUserName
    def attributesMap

    static mapping = {
        table("ACTIVITIES")
        details column: "DETAILS", sqlType: DbUtil.longStringType
        justification column: "JUSTIFICATION", length: 9000
        attributes sqlType: DbUtil.longStringType
    }

    static constraints = {
        ipAddress nullable: true
        clientName nullable: true
        alert nullable: true
        justification nullable: true, maxSize: 9000
        attributes nullable: true
        caseNumber nullable: true
        suspectProduct nullable: true
        eventName nullable: true
        assignedTo nullable: true, blank: true
        assignedToGroup nullable: true, blank: true
        guestAttendeeEmail nullable: true, blank: true
        privateUserName nullable: true
    }

    static belongsTo = [alert: Alert, performedBy: User]

    static transients = [ "attributesMap" ]

    def getAttr(attrName) {
        if (!attributesMap && attributes) {
            def jsonSlurper = new JsonSlurper()
            attributesMap = jsonSlurper.parseText(attributes)
        }

        if (attributesMap)
            attributesMap[attrName]
        else
            null
    }

    def getAttrString() {
        if (!attributesMap && attributes) {
            def jsonSlurper = new JsonSlurper()
            attributesMap = jsonSlurper.parseText(attributes)
        }
        if (attributesMap) {
            attributesMap.inject("", { memo, k, v ->
                memo += "$k: $v,"
            }) + " "
        } else {
            ""
        }
    }

    def toDto() {
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtil.DATEPICKER_FORMAT_AM_PM)
        [
                alertName            : alert?.name?.trim()?.replaceAll("\\s{2,}", " "),
                alertId              : alert?.id,
                activity_id          : id,
                type                 : type.value.toString(),
                details              : replaceNullCharacter(details),
                performedBy          : performedBy.getFullName()?.equalsIgnoreCase(Constants.Commons.SYSTEM)?Constants.Commons.SYSTEM: performedBy.getFullName(),
                timestamp            : sdf.format(new Date(DateUtil.toDateStringWithTime(this.timestamp, userService.getCurrentUserPreference().timeZone))),
                justification        : justification,
                caseNumber           : caseNumber,
                suspect              : suspectProduct,
                eventName            : eventName,
                currentAssignment    : assignedTo ? assignedTo?.getFullName() : assignedToGroup ? assignedToGroup?.name : guestAttendeeEmail,
                currentAssignmentDept: assignedTo?.userDepartments?.departmentName,
                performedByDept      : performedBy?.userDepartments?.departmentName
        ]
    }

    @Override
    String toString() {
        return details?.replaceAll("<br>","")
    }

    private String replaceNullCharacter(String value) {
        if (StringUtils.isNotBlank(value) && value.contains(Character.toString((char) 0x00))) {
            value = value.replace(Character.toString((char) 0x00), "")
        }
        return value?.replaceAll("<br>", "\n")
    }

}
