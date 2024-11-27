package com.rxlogix.signal

import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.json.JsonOutput
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.DbUtil
import groovy.json.JsonSlurper

class GroupedAlertInfo {

    String name
    String productGroupSelection
    String productSelection
    String dispCounts
    String productDictionarySelection
    boolean isLatest
    boolean isEnabled
    boolean isDeleted = false
    Long assignedToId
    List<ExecutedConfiguration> execConfigList = []
    Long masterTemplateConfigId
    Long exMasterTemplateConfigId
    Long alertGroupId
    Date dateCreated
    Date lastUpdated

    static hasMany = [execConfigList: ExecutedConfiguration]
    static mapping = {
        execConfigList  joinTable: [name: "GROUP_ALERT_EXEC_CONFIG", column: "EXEC_CONFIG_ID", key: "ALERT_GROUP_INFO_ID"], indexColumn: [name: "GROUPED_ALERT_EX_RCONFIG_IDX"]
    }
    static constraints = {
        assignedToId nullable: true
        masterTemplateConfigId nullable: true
        exMasterTemplateConfigId nullable: true
        productDictionarySelection nullable: true
        productGroupSelection nullable: true
        productSelection nullable: true
        dispCounts nullable: true
        alertGroupId nullable: true
    }

    public List<GroupedAlertInfo> getAssignedToAlerts(Long userId){
        Set<Group> groups = User.get(userId).groups
        List<Long> groupIds = groups*.id
        List<GroupedAlertInfo> groupedAlertInfo = GroupedAlertInfo.createCriteria().list{
            eq("assignedToId",userId)
            groupIds.collate(1000).each{ id->
                'in'('assignedToGroupId',id)
            }
            eq('isLatest', true)
            eq('isEnabled',true)
        }
    }
}
