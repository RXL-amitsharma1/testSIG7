package com.rxlogix.config

import com.rxlogix.enums.AdjustmentTypeEnum
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.DbUtil

class ExecutedEvdasConfiguration implements AlertUtil {
    static auditable = false

    String name
    User owner
    String scheduleDateJSON
    Date nextRunDate
    String description
    boolean isPublic = false
    boolean isDeleted = false
    boolean isEnabled = true
    String productSelection
    String productGroupSelection
    String eventGroupSelection
    String eventSelection
    int numOfExecutions = 0
    boolean isLatest = false

    ReportExecutionStatus executionStatus
    String configSelectedTimeZone = "UTC"
    String blankValuesJSON

    ExecutedEVDASDateRangeInformation dateRangeInformation

    //Common domain params.
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy
    String frequency

    long totalExecutionTime = 0 // this will be in milliseconds

    Group workflowGroup

    boolean adhocRun = false
    Long executedQuery
    String executedQueryName
    User assignedTo
    Group assignedToGroup
    Priority priority
    Long configId
    String dispCounts
    String removedUsers
    String requiresReviewCount
    String productName
    Date reviewDueDate
    Long masterExConfigId
    String selectedDataSheet
    String datasheetType = "CORE_SHEET"
    Boolean isDatasheetChecked = false
    String allProducts


    static hasMany = [activities: Activity]

    static mapping = {
        table name: "EX_EVDAS_CONFIG"
        id generator:'sequence', params:[sequence:'evdas_exec_config_sequence']
        productSelection column: "PRODUCT_SELECTION", sqlType: DbUtil.longStringType
        eventSelection column: "EVENT_SELECTION", sqlType: DbUtil.longStringType
        scheduleDateJSON column: "SCHEDULE_DATE"
        configSelectedTimeZone column: "SELECTED_TIME_ZONE"
        blankValuesJSON column: "BLANK_VALUES", sqlType: DbUtil.longStringType
        numOfExecutions column: "NUM_OF_EXECUTIONS"
        activities joinTable: [name: "EX_EVDAS_CONFIG_ACTIVITIES", column: "ACTIVITY_ID", key: "EX_EVDAS_CONFIG_ID"], indexColumn: [name: "ACTIVITIES_IDX"]
        isLatest column: "IS_LATEST"
        workflowGroup column: "WORKFLOW_GROUP"
        configId column: "CONFIG_ID"
        selectedDataSheet column: "SELECTED_DATASHEET"
        datasheetType column: "DATASHEET_TYPE"
        isDatasheetChecked column: "IS_DATASHEET_CHECKED"
        productGroupSelection sqlType: "varchar2(8000 CHAR)"
        eventGroupSelection sqlType: "varchar2(8000 CHAR)"
        removedUsers sqlType: "varchar2(2000 CHAR)"
        productName sqlType: "varchar2(1000 CHAR)"
        allProducts sqlType: DbUtil.longStringType
    }

    static constraints = {
        productSelection(validator: { val, obj ->
            if (!val && !obj.productGroupSelection) {
                return "com.rxlogix.Configuration.productSelection.nullable"
            }
        }, nullable: true, maxSize: 8192)
        productGroupSelection nullable: true
        eventGroupSelection nullable: true
        eventSelection nullable:true
        scheduleDateJSON(nullable: true, maxSize: 1024)
        blankValuesJSON(nullable: true, maxSize: 8192)
        nextRunDate(nullable: true)
        numOfExecutions(min: 0, nullable: false)
        description nullable: true, maxSize: 8000
        dateRangeInformation nullable: true
        adhocRun nullable: true
        frequency nullable: true
        workflowGroup nullable: true
        executedQuery nullable: true
        executedQueryName nullable: true
        assignedTo nullable: true
        assignedToGroup nullable: true
        priority nullable: true
        configId nullable: false
        dispCounts nullable: true
        requiresReviewCount nullable: true
        reviewDueDate(nullable:true)
        removedUsers nullable:true
        productName nullable:true
        masterExConfigId nullable: true
        selectedDataSheet nullable: true, blank: true, maxSize: 32000
        datasheetType nullable: true, blank: true, maxSize: 32000
        isDatasheetChecked nullable: true, blank: true
        allProducts nullable: true
    }

    def getEventSelectionList() {
        String evtName = getAllProductNameFieldFromJson(this.eventSelection)
        if (evtName) {
            evtName.toLowerCase().tokenize(',')
        } else {
            []
        }
    }
    String getEventGroupSelectionList() {
        getEventGroupFromJson(this.eventGroupSelection)
    }

    def getProductNameList() {
        String prdName = getNameFieldFromJson(this.productSelection)
        def prdList = []
        if(prdName) {
            prdList = prdName.tokenize(',')
        }
        return prdList
    }

    def getInstanceIdentifierForAuditLog() {
        return name + ": ${dateRangeInformation}"
    }

}
