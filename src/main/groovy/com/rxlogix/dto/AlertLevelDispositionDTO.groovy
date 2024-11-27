package com.rxlogix.dto

import com.rxlogix.config.ActivityType
import com.rxlogix.config.Disposition
import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.ExecutedEvdasConfiguration
import com.rxlogix.config.Priority
import com.rxlogix.user.User
import com.rxlogix.signal.LiteratureHistory

class AlertLevelDispositionDTO {

    def domainName
    Disposition targetDisposition
    Priority priority
    String justificationText
    ExecutedConfiguration execConfig
    List<Long> reviewCompletedDispIdList = []
    List<Map> alertList = []
    User loggedInUser
    String userName
    ActivityType activityType
    Long execConfigId
    String changeType
    //EVDAS Specific
    ExecutedEvdasConfiguration evdasalertConfiguration
    Long configId

    //Literature
    List<Long> assignedToGroup
    Long workflowGroupId

    List<Map> existingCaseHistoryList = []
    List<LiteratureHistory> literatureHistories = []
}
