package com.rxlogix.enums

enum GroupType {
    USER_GROUP("User Group"),
    WORKFLOW_GROUP("Workflow Group")

    String value

    GroupType(value) { this.value = value }

    static List<GroupType> all () {
        [USER_GROUP, WORKFLOW_GROUP].sort({it.value.toUpperCase()})
    }
}