package com.rxlogix.dto

import com.rxlogix.config.AlertType
import com.rxlogix.config.ReportExecutionStatus
import com.rxlogix.user.User

class ExecutionStatusDTO {

    AlertType alertType
    ReportExecutionStatus executionStatus
    String searchString
    def configurationDomain
    User currentUser
    Integer max
    Integer offset
    String sort
    String direction
    Long workflowGroupId
}
