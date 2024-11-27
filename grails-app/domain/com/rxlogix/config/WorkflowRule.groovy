package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.user.Group
import com.rxlogix.util.MiscUtil
import groovy.transform.ToString

//todo check usage and remove it.
@Deprecated
@ToString(includes = ['name'])
class WorkflowRule implements Serializable {
    static auditable = true

    String name
    String description
    PVSState incomeState
    PVSState targetState
    boolean approveRequired
    List allowedDispositions
    List allowedGroups
    List actions
    boolean isDeleted = false
    boolean notify = false
    boolean signalRule = false
    boolean display = true

    static constraints = {
        description nullable: true, validator: { value, object ->
            return MiscUtil.validator(value, "Description", Constants.SpecialCharacters.TEXTAREA_CHARS as String[])
        }
        allowedDispositions nullable: true
        allowedGroups nullable: true
        actions nullable: true
        name unique: true, blank: false, nullable: false, validator: { value, object ->
            return MiscUtil.validator(value, "Name", Constants.SpecialCharacters.DEFAULT_CHARS as String[])
        }
        targetState validator: { value, object ->
            if (value == object.incomeState) {
                return false
            }
        }
        topicCategories nullable: true
        incomeState nullable: true
    }

    static mapping = {
        table("WORK_FLOW_RULES")
        allowedGroups joinTable: "WORKFLOWRULES_GROUPS"
        allowedDispositions joinTable: 'WKFL_RUL_DISPOSITIONS'
        topicCategories joinTable: [name: "WORKFLOWRULES_SIGNAL_CATEGORY", column: "SIGNAL_CATEGORY_ID", key: "WORKFLOW_ID"]
        actionTemplates joinTable: [name: "WORKFLOWRULES_ACTION_TEMPLATES", column: "ACTION_TEMPLATE_ID", key: "WORKFLOW_ID"]
    }

    static hasMany = [actions      : Action, allowedDispositions: Disposition, actionTemplates: ActionTemplate,
                      allowedGroups: Group, topicCategories: TopicCategory]

    def toDto() {
        [
                id                 : this.id,
                name               : this.name?.trim()?.replaceAll("\\s{2,}", " "),
                description        : this.description,
                incomeState        : this.incomeState.displayName,
                targetStates       : this.targetState.displayName,
                allowedDispositions: getDispositionsAsString(this),
                display            : this.display
        ]
    }

    def getDispositionsAsString(WorkflowRule workflowRuleObj) {
        workflowRuleObj.allowedDispositions?.unique().collect { it.displayName }.join(",")
    }

    def getEntityValueForDeletion(){
        return "Name-${name}"
    }
}