package com.rxlogix.config

import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.DbUtil

class ExecutedLiteratureConfiguration {

    def literatureAlertService

    //Configuration related
    String name
    User owner
    User assignedTo
    Group assignedToGroup
    String productSelection
    String productGroupSelection
    String eventSelection
    String eventGroupSelection
    String searchString

    ExecutedLiteratureDateRangeInformation dateRangeInformation
    Group workflowGroup

    //Common domain params.
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy
    boolean isDeleted = false
    int numOfExecutions = 0
    long totalExecutionTime = 0
    boolean isEnabled = true
    boolean isLatest = false
    Long configId
    String selectedDatasource

    String requiresReviewCount
    String removedUsers
    String productName
    Integer articleCount
    Long literatureQueryId
    String literatureQueryName

    static hasMany = [literatureActivity: LiteratureActivity]
    static transients = ['articleCount']

    static constraints = {
        name(nullable: false, maxSize: 255)
        lastUpdated(nullable: true)
        assignedTo nullable: true, blank: true, validator: { value, obj ->
            def result = true
            if(!obj.assignedTo){
                result = obj.assignedToGroup ? true :  'assignedTo.nullable'
            }
            return result
        }
        assignedToGroup nullable: true, blank: true
        createdBy(nullable: false, maxSize: 100)
        modifiedBy(nullable: false, maxSize: 100)
        productSelection( nullable: true, maxSize: 8192)
        productGroupSelection( nullable: true, maxSize: 8000)
        eventSelection(nullable: true, maxSize: 8192)
        eventGroupSelection(nullable: true, maxSize: 8000)
        searchString(nullable: true)
        configId(nullable: false)
        dateRangeInformation(nullable: true)
        selectedDatasource nullable: true
        requiresReviewCount nullable: true
        removedUsers nullable:true
        numOfExecutions( nullable: true)
        productName( nullable: true)
        literatureQueryId nullable: true
        literatureQueryName nullable: true
    }

    static mapping = {
        table name: 'EX_LITERATURE_CONFIG'
        numOfExecutions column: "NUM_OF_EXECUTIONS"
        name column: "NAME"
        owner column: "PVUSER_ID"
        configId column: "CONFIG_ID"
        productSelection column: "PRODUCT_SELECTION", sqlType: DbUtil.longStringType
        productGroupSelection column: "PRODUCT_GROUP_SELECTION",sqlType: "varchar2(8000 CHAR)"
        eventSelection column: "EVENT_SELECTION", sqlType: DbUtil.longStringType
        eventGroupSelection column: "EVENT_GROUP_SELECTION",sqlType: "varchar2(8000 CHAR)"
        selectedDatasource column: "SELECTED_DATA_SOURCE"
        removedUsers sqlType: "varchar2(2000 CHAR)"
        productName sqlType: DbUtil.longStringType
    }

    def getInstanceIdentifierForAuditLog() {
        return name + ": ${dateRangeInformation}"
    }

    Integer getArticleCount() {
        if (this.selectedDatasource == 'embase') {
            return literatureAlertService.getLiteratureAlertTotalCount(id, !isLatest, true)
        } else {
            return literatureAlertService.getLiteratureAlertTotalCount(id, !isLatest)
        }
    }
}
