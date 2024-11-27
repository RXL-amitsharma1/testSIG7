package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.enums.GroupType
import com.rxlogix.user.Group
import com.rxlogix.util.MiscUtil
import groovy.transform.ToString

@ToString(includes = ['name'])
class DispositionRule {

    static auditable = true

    String name
    String description

    Disposition incomingDisposition
    Disposition targetDisposition

    Boolean approvalRequired = false
    Boolean isDeleted = false
    Boolean notify = false
    Boolean signalRule = false
    Boolean display = true

    static hasMany = [workflowGroups: Group, allowedUserGroups: Group, topicCategories: TopicCategory]

    static mapping = {
        table("DISPOSITION_RULES")
        topicCategories joinTable: [name: "DISPO_RULES_TOPIC_CATEGORY", column: "TOPIC_CATEGORY_ID", key: "DISPOSITION_ID"]
        workflowGroups joinTable: [name: "DISPO_RULES_WORKFLOW_GROUP", column: "WORKFLOW_GROUP_ID", key: "DISPOSITION_ID"]
        allowedUserGroups joinTable: [name: "DISPO_RULES_USER_GROUP", column: "USER_GROUP_ID", key: "DISPOSITION_ID"]
    }

    static constraints = {
        name  blank: false, nullable: false, validator: { value, object ->
            return MiscUtil.validator(value, "Display Name", Constants.SpecialCharacters.DEFAULT_CHARS as String[])
        }
        description nullable: true
        topicCategories nullable: true
        targetDisposition(validator: { value, object ->
            def retValue = true
            if(value == object.incomingDisposition) {
                retValue = 'com.rxlogix.config.DispositionRule.targetDisposition.validator.invalid'
            }
            return retValue
        })
        workflowGroups(validator: { val, obj ->
            def retval = true
            Boolean hasAnyOtherGroupTypeAssigned = val?.findAll {
                it.groupType != GroupType.WORKFLOW_GROUP
            } as Boolean
            if (hasAnyOtherGroupTypeAssigned) {
                retval = 'com.rxlogix.signal.DispositionRule.workflowGroups.otherGroup'
            }
            return retval
        })
        allowedUserGroups(validator: { val, obj ->
            def retval = true
            Boolean hasAnyOtherGroupTypeAssigned = val?.findAll {
                it.groupType != GroupType.USER_GROUP
            } as Boolean
            if (hasAnyOtherGroupTypeAssigned) {
                retval = 'com.rxlogix.signal.DispositionRule.userGroups.otherGroup'
            }
            return retval
        })
    };

    Map toDto() {
        [
                id                  : this.id,
                name                : this.name?.trim()?.replaceAll("\\s{2,}", " "),
                description         : this.description,
                incomingDisposition : this.incomingDisposition.displayName,
                targetDisposition   : this.targetDisposition.displayName,
                allowedUserGroups   : this.allowedUserGroups?.join(','),
                workflowGroups      : this.workflowGroups?.join(','),
                display             : this.display
        ]
    }

    def getInstanceIdentifierForAuditLog() {
        if(isDeleted && name.contains("_deleted_")){
            return name.substring(0, name.lastIndexOf("_deleted_"))
        }else{
            return name
        }
    }

    def getEntityValueForDeletion(){
        if(isDeleted && name.contains("_deleted_")){
            return "Name- ${name.substring(0, name.lastIndexOf("_deleted_"))}"
        }else{
            return "Name-${name}"
        }
    }
}
