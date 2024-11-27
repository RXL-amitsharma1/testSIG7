package com.rxlogix.helper

import com.rxlogix.config.Action
import com.rxlogix.util.DateUtil

trait ActivityHelper {
    def composeDescription(oldInst, fieldName, newInst, desc, timeZone) {
        def oldValue = oldInst?."$fieldName" ?: ''
        def newValue = newInst?."$fieldName" ?: ''
        if (fieldName == 'dueDate' || fieldName == 'completedDate') {
            oldValue = oldValue? DateUtil.toDateString(oldValue,timeZone) : ''
            newValue = newValue? DateUtil.toDateString(newValue,timeZone) : ''
        }



        def newDesc = desc
        if(fieldName == 'completedDate' && oldValue!=newValue){
            newDesc = (desc ? desc + ", " : "") +  "Completion Date changed from '$oldValue' to '$newValue'"
        } else if (((oldValue && newValue) || (fieldName in ["assignedTo", "assignedToGroup"])) && oldValue != newValue) {
            String fieldValue=fieldName.split("(?=\\p{Upper})").join(' ')
            fieldValue=fieldValue.substring(0,1).toUpperCase() + fieldValue.substring(1)
            newDesc = (desc ? desc + ", " : "") +  "$fieldValue changed from '$oldValue' to '$newValue'"
        }else if(fieldName == 'comments'){
            if(oldValue && !newValue || !oldValue && newValue){
                newDesc = (desc ? desc + ", " : "") +  "$fieldName changed from '${oldValue ? oldValue : ""}' to '${newValue ? newValue : ""}'"
            }

        }

        newDesc
    }

    /**
     * Method to prepare the activity description.
     * @param oldAction
     * @param newAction
     * @return
     */
    def prepareActivityDescription(Action oldAction, Action newAction, String timeZone) {
        def changes = ["type", "assignedTo", "assignedToGroup", "dueDate", "details", "comments", "actionStatus", "config", "completedDate"].inject("", { rst, next ->
            composeDescription(oldAction, next, newAction, rst, timeZone)
        })
        changes ? "Action [$oldAction.id] " + changes : ""
    }
}
