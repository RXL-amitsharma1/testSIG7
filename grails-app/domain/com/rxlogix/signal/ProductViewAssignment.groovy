package com.rxlogix.signal

import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import grails.converters.JSON
import grails.gorm.dirty.checking.DirtyCheck
import grails.util.Holders

@DirtyCheck
class ProductViewAssignment {

    String hierarchy
    Long workflowGroup

    Date dateCreated
    Date lastUpdated

    String product
    List<Long> usersAssigned
    List<Long> groupsAssigned

    String primaryUserOrGroupId
    String alertGroup


    Long tenantId

    static hasMany = [usersAssigned: Long, groupsAssigned: Long]

    static constraints = {
        hierarchy nullable: false
        workflowGroup nullable: true
        product nullable: false
        groupsAssigned nullable: true
        usersAssigned nullable: true
        primaryUserOrGroupId nullable: true
        tenantId nullable: false
        alertGroup nullable: true, validator: { val, obj ->
            if (val && obj.primaryUserOrGroupId) { // Only validate if both alertGroup and primaryAssignee are not null
                if (ProductViewAssignment.existsAnotherWithDifferentAlertGroup(obj.primaryUserOrGroupId, val, obj.id)) {
                    return "Error Due to Primary Assignee has an assignment in another alert Group. Assignment Details: Product:  ${obj.product}, Alert Group ID: ${obj.alertGroup}, Primary Assignee: ${obj.primaryUserOrGroupId}, Hierarchy: ${obj.hierarchy}"
                }
                if(ProductViewAssignment.existsAnotherWithDifferentWorkflowGroup(obj.workflowGroup, val, obj.id)) {
                    return "Error due to Alert Group present in different workflow groups. Assignment Details: Product:  ${obj.product}, Alert Group ID: ${obj.alertGroup}, Primary Assignee: ${obj.primaryUserOrGroupId}, Hierarchy: ${obj.hierarchy}"
                }
            }
            return true
        }
    }

    static mapping = {
        datasource "pva"
        version false
        id generator: 'sequence', params: [sequence: 'PRODUCT_VIEW_ASSIGNMENT_SEQ']
        usersAssigned joinTable: [name: "PRODUCT_ASSIGNMENT_USERS", key: "PRODUCT_ASSIGNMENT_ID", column: "USER_ID"]
        groupsAssigned joinTable: [name: "PRODUCT_ASSIGNMENT_GROUPS", key: "PRODUCT_ASSIGNMENT_ID", column: "GROUP_ID"]
    }

    static boolean existsAnotherWithDifferentAlertGroup(String primaryAssignee, String currentAlertGroup, Long currentId) {
        def criteria = ProductViewAssignment.createCriteria()
        def count = criteria.count {
            eq('primaryUserOrGroupId', primaryAssignee) // Same Primary Assignee
            ne('alertGroup', currentAlertGroup)    // Different Alert Group
            if (currentId) {
                ne('id', currentId) // Ignore the current record during validation (for updates)
            }
        }
        return count > 0
    }

    static boolean existsAnotherWithDifferentWorkflowGroup(Long workflowGroup, String currentAlertGroup, Long currentId) {
        def criteria = ProductViewAssignment.createCriteria()
        def count = criteria.count {
            ne('workflowGroup', workflowGroup) // Different workflow group
            eq('alertGroup', currentAlertGroup)    // Same Alert Group
            if (currentId) {
                ne('id', currentId) // Ignore the current record during validation (for updates)
            }
        }
        return count > 0
    }

    Map toExportDto(Map groupsMap, Map usersMap, String timeZone) {
        String dateCreated = DateUtil.StringFromDate(this.dateCreated, DateUtil.DATEPICKER_FORMAT, timeZone)
        String lastUpdated = DateUtil.StringFromDate(this.lastUpdated,DateUtil.DATEPICKER_FORMAT_AM_PM, timeZone)
        String assignmentsString = generateAssignedUserOrGroupString(usersMap, groupsMap, this.usersAssigned, this.groupsAssigned, true)
        String userAssignmentsString = generateAssignedUserOrGroupString(usersMap, groupsMap, this.usersAssigned, this.groupsAssigned, false)
        Map map = [
                product      : JSON.parse(this.product).name,
                hierarchy    : this.hierarchy,
                assignments  : assignmentsString,
                userId       : userAssignmentsString,
                workflowGroup: this.workflowGroup ? groupsMap.get(this.workflowGroup)?.name : "",
                dateCreated  : dateCreated,
                lastUpdated  : lastUpdated,
                alertGroup   : this.alertGroup
        ]
        map
    }

    String generateAssignedUserOrGroupString(Map usersMap, Map groupsMap, List<Long> usersAssigned, List<Long> groupsAssigned, Boolean isAssignment = true) {
        List<String> assignedUserOrGroupsList = []
        usersAssigned?.each {
            User user = usersMap.get(it)
            if (user) {
                assignedUserOrGroupsList.add(isAssignment ? user.fullName : user.username)
            }
        }
        groupsAssigned?.each {
            Group group = groupsMap.get(it)
            if(group) {
                assignedUserOrGroupsList.add(group.name)
            }
        }
        assignedUserOrGroupsList.join(", ")
    }

}
