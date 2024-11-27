package com.rxlogix.helper

import com.rxlogix.config.EvaluationReferenceType
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import grails.util.Holders

class AlertHelper {
    def static composeDetailRowsForDisplay(alert, timezone, attribute) {
        def tmp = attribute.collect {
            it.collect { item ->

                def value = null

                if (alert.hasProperty("${item[1]}")) {
                    value = alert."${item[1]}"
                } else {
                    value = alert.getAttr("${item[1]}")
                }

                def align = "left"
                def key = "${item[1]}"
                if(key == 'assignedTo') {
                    value = alert.assignedTo ? alert.assignedTo?.fullName : alert.assignedToGroup?.name
                }
                if(key == 'shareWith') {
                    value = alert?.shareWithUser + alert?.shareWithGroup
                }
                if (key == 'productSelection') {
                    if (alert.productSelection) {
                        value = alert.getNameFieldFromJson(alert.productSelection)
                    } else if (alert?.studySelection) {  //Added for study Selection
                        value = alert.getNameFieldFromJson(alert.studySelection)
                    } else {
                        value = alert.getGroupNameFieldFromJson(alert.productGroupSelection)
                    }
                }

                if (key == 'eventSelection') {
                    if(alert.eventSelection){
                        value = alert.getNameFieldFromJson(alert.eventSelection)
                    } else {
                        value = alert.getGroupNameFieldFromJson(alert.eventGroupSelection)
                    }
                }

                if (key == 'Shared with Group') {
                    if (value) {
                        def strVal = value.toString()
                        value = strVal.substring(1, strVal.size() - 1)
                    } else {
                        value = null
                    }
                }

                if (key == 'deviceRelated') {
                    if (!value)
                        value = "No"
                }

                if (key == 'actionTaken') {
                    value = alert?.actionTaken ? alert.actionTaken.join(", ") : ""
                }

                if(key == 'refType' && alert) {
                    value = alert.refType ? EvaluationReferenceType.get(alert.refType as Long).name : "-"
                }

                if (value instanceof Date) {
                    value = DateUtil.toDateString(value, timezone)
                } else if (value instanceof User) {
                    value = value.getFullName()
                } else if (value instanceof Boolean) {
                    value = value ? 'Yes' : 'No'
                } else if (value instanceof List<String>) {
                    value = value.join(',')
                }
                [item[0], item[1], value, align, item[2]]
            }
        }
        tmp
    }

}
