package com.rxlogix.config

import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.AuditLogConfigUtil
import com.rxlogix.util.DbUtil

class AdvancedFilter {
    static auditable = ['ignore':['user','criteria','keyId','JSONQuery','modifiedBy','lastUpdated','createdBy','dateCreated']]
    def auditLogService

    String name
    String description
    String alertType
    String JSONQuery
    String criteria

    //Standard fields
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy
    User user
    List<User> shareWithUser = []  //reverted the type to list as set doesnt preserve order PVS-55058
    List<Group> shareWithGroup = []
    Long keyId

    static constraints = {
        name(validator: { val, obj ->
            //Name is unique to alertType
            if (!obj.id || obj.isDirty("name")) {
                long count = AdvancedFilter.createCriteria().count {
                    ilike('name', "${val}")
                    eq('alertType', obj.alertType)
                    eq('user.id', obj.user?.id)
                    if (obj.id) {
                        ne('id', obj.id)
                    }
                }
                if (count) {
                    return "com.rxlogix.config.AdvancedFilter.name.unique.per.type"
                }
            }
        }, nullable: false)
        description nullable: true, maxSize: 8000
        'JSONQuery' column: "QUERY", sqlType: DbUtil.longStringType
        'criteria' column: "criteria", sqlType: DbUtil.longStringType
        keyId nullable: true
    }

    static mapping = {
        table name: "ADVANCED_FILTER"
        JSONQuery(maxSize: 8388608)
        criteria(maxSize: 8388608)
        shareWithUser joinTable: [name:"SHARE_WITH_USER_FILTER", column:"USER_ID", key:"ADVANCED_FILTER_ID"]
        shareWithGroup joinTable: [name:"SHARE_WITH_GROUP_FILTER", column:"GROUP_ID", key:"ADVANCED_FILTER_ID"]
    }

    Set<User> getShareWithUsers() {
        Set<User> users = []
        if (this.shareWithUser) {
            users.addAll(this.shareWithUser)
        }
        return users.grep()
    }

    Set<Group> getShareWithGroups() {
        Set<Group> userGroups = []
        if (this.shareWithGroup) {
            userGroups.addAll(this.shareWithGroup)
        }
        return userGroups.grep()
    }

    Boolean isAdvancedFilterShared() {
        (this.shareWithUsers || this.shareWithGroups)
    }

    Boolean isFilterUpdateAllowed(User currentUser) {
        currentUser.isAdmin() || (!isAdvancedFilterShared() && this.userId == currentUser.id)
    }

    @Override
    String toString() {
        "${this.name}"
    }

    def getInstanceIdentifierForAuditLog() {
        return name
    }

    def getEntityValueForDeletion(){
        return "Filter Name-${name}, Share With Users-${shareWithUser?.join(', ')}, Share With Groups-${shareWithGroup?.join(', ')}, Description-${description?:""}, Owner-${user}, Created Date-${AuditLogConfigUtil.getCreatedDateInGmt(dateCreated)}"
    }
}
