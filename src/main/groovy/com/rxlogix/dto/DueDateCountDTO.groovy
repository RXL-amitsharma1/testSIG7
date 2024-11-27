package com.rxlogix.dto

import com.rxlogix.UserDashboardCounts

class DueDateCountDTO {
    Long userId
    Long workflowGroupId
    List<Long> groupIdList = []
    List pastDateList = []
    List currentDateList = []
    List futureDateList = []
    UserDashboardCounts userDashboardCounts
}
