package com.rxlogix.dto

class AlertReviewDTO {

    List<Long> execConfigIds = []
    String alertType
    Boolean adhocRun
    Long workflowGrpId
    Integer max
    Integer offset
    String searchValue
    String orderProperty
    String direction
    String groupIds
    List<String> shareWithConfigs
    List<String> filterWithUsersAndGroups = []
}
