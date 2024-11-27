package com.rxlogix.dto

import com.rxlogix.UserDashboardCounts
import com.rxlogix.enums.GroupType
import com.rxlogix.user.User

class DispositionCountDTO {
    List<Map> userDispCaseCountList = []
    List<Map> userDueDateCaseCountsList = []
    List<Map> groupDispCaseCountList = []
    List<Map> dueDateGroupCaseCountList = []
    List<Map> userDispPECountList = []
    List<Map> userDueDatePECountsList = []
    List<Map> groupDispPECountList = []
    List<Map> dueDateGroupPECountList = []
    Map<String, Integer> userDispCaseCountsMap = [:]
    Map<String, Integer> userDueDateCaseCountsMap = [:]
    Map<String, Map> dueDateGroupCaseCountsMap = [:]
    Map<String, Map> groupDispCaseCountsMap = [:]
    Map<String, Integer> userDispPECountsMap = [:]
    Map<String, Integer> userDueDatePECountsMap = [:]
    Map<String, Map> dueDateGroupPECountsMap = [:]
    Map<String, Map> groupDispPECountsMap = [:]
    Long workflowGroupId
    Long userId
    List<Long> groupIdList
    UserDashboardCounts userDashboardCounts

    DispositionCountDTO(User user) {
        userId = user.id
        workflowGroupId = user.workflowGroup?.id
        groupIdList = user.groups.findAll { it.groupType != GroupType.WORKFLOW_GROUP }.id
        userDashboardCounts = UserDashboardCounts.get(user.id)
    }
}
