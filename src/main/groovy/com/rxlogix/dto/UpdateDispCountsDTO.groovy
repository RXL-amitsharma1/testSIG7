package com.rxlogix.dto

class UpdateDispCountsDTO {
    List<Map> userDueDateCaseCountsList = []
    List<Map> dueDateGroupCaseCountList = []
    List<Map> userDueDatePECountsList = []
    List<Map> dueDateGroupPECountList = []
    List<Map> userDispCaseCountList = []
    List<Map> groupDispCaseCountList = []
    List<Map> userDispPECountList = []
    List<Map> groupDispPECountList = []
    List<Map> userDueDateCountsList = []
    List<Map> dueDateGroupCountList = []
    String dispCountKey
    String dueDateCountKey
    String groupDispCountKey
    String groupDueDateCountKey
}
