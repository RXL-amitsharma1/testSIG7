package com.rxlogix.config

import com.rxlogix.BaseConfiguration
import com.rxlogix.Constants
import com.rxlogix.enums.AdjustmentTypeEnum
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.DbUtil
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class ExecutedConfiguration extends BaseConfiguration implements Serializable {
    static auditable=false

    ReportExecutionStatus executionStatus // this doesn't actually get created because of the getExectionStatus() method at bottom!!
    Date reviewDueDate
    ExecutedAlertDateRangeInformation executedAlertDateRangeInformation
    Long executedAlertQueryId
    Long executedAlertForegroundQueryId
    List<ExecutedQueryValueList> executedAlertQueryValueLists
    List<ExecutedQueryValueList> executedAlertForegroundQueryValueLists
    Long pvrCaseSeriesId
    Long pvrCumulativeCaseSeriesId
    Long faersCaseSeriesId
    Long faersCumCaseSeriesId
    Long vigibaseCaseSeriesId
    Long vigibaseCumCaseSeriesId
    Long vaersCaseSeriesId
    Long vaersCumCaseSeriesId
    Long reportId
    Long configId
    ReportExecutionStatus caseSeriesExecutionStatus = ReportExecutionStatus.GENERATING
    ReportExecutionStatus cumCaseSeriesExecutionStatus = ReportExecutionStatus.GENERATING
    ReportExecutionStatus reportExecutionStatus = ReportExecutionStatus.GENERATING
    Priority priority
    String evdasDateRange
    String faersDateRange
    String vaersDateRange
    String vigibaseDateRange
    String dispCounts
    Integer newCounts = 0
    String requiresReviewCount
    String removedUsers
    String productName
    String caseSeriesSpotfireFile

    Boolean isAutoAssignedTo = false
//    Boolean isAutoSharedWith = false
    List<User> autoShareWithUser = []
    List<Group> autoShareWithGroup = []
    Boolean isTemplateAlert = false
    Long masterExConfigId
    Integer runCount = 0
    String disabledDssNodes
    String stratificationColumns
    String stratificationColumnsDataMining
    String selectedDataSheet
    String datasheetType = "CORE_SHEET"
    Boolean isDatasheetChecked = false
    AdjustmentTypeEnum adjustmentTypeEnum
    Long skippedAlertId
    String skippedAlertGroupCode
    String allProducts
    String drugTypeName
    Boolean isStandalone = false
    String criteriaCounts
    Boolean isMultiIngredient = false
    Boolean isMasterTemplateAlert = false
    Long exMasterTemplateId
    String productHierarchyMasterTmplt
    String eventHierarchyMasterTmplt
    String studyDrugs
    Long alertGroupId

    static hasMany = [executedTemplateQueries: ExecutedTemplateQuery, executedAlertQueryValueLists: ExecutedQueryValueList, executedAlertForegroundQueryValueLists: ExecutedQueryValueList, sharedWith: SharedWith, activities: Activity,
                      autoShareWithUser: User, autoShareWithGroup: Group]
    boolean isLatest = false

    static mapping = {
        table name: "EX_RCONFIG"

        id generator: 'sequence', params: [sequence: 'exec_config_sequence']
        //Workaround to pull in mappings from super class that is not a domain.
        def superMapping = BaseConfiguration.mapping.clone()
        superMapping.delegate = delegate
        superMapping.call()
        activities joinTable: [name: "ex_rconfig_activities", column: "ACTIVITY_ID", key: "EX_CONFIG_ACTIVITIES_ID"], indexColumn: [name: "ACTIVITIES_IDX"]
        executionStatus column: "EX_STATUS"
        productGroups joinTable: [name: "EX_RCONFIGS_PROD_GRP", column: "PROD_GRP_ID", key: "EXCONFIG_ID"], indexColumn: [name: "PROD_GRP_IDX"]
        executedAlertQueryId column: "EX_ALERT_QUERY_ID"
        executedAlertForegroundQueryId column: "EX_ALERT_FG_QUERY_ID"
        executedAlertQueryValueLists joinTable: [name: "EX_ALERT_QUERY_VALUES", column: "EX_QUERY_VALUE_ID", key: "EX_ALERT_QUERY_ID"], indexColumn: [name: "EX_QUERY_VALUE_IDX"]
        executedAlertForegroundQueryValueLists joinTable: [name: "EX_FG_ALERT_QUERY_VALUES", column: "EX_FG_QUERY_VALUE_ID", key: "EX_ALERT_FG_QUERY_ID"], indexColumn: [name: "EX_FG_QUERY_VALUE_IDX"]
        executedAlertDateRangeInformation column: "EX_ALERT_DATE_RANGE_ID"
        reportId column: "REPORT_ID"
        configId column: "CONFIG_ID"
        selectedDataSheet column: "SELECTED_DATASHEET"
        datasheetType column: "DATASHEET_TYPE"
        isDatasheetChecked column: "IS_DATASHEET_CHECKED"
        pvrCumulativeCaseSeriesId column: "CUMULATIVE_CASE_SERIES_ID"
        reportExecutionStatus column: "REPORT_EXECUTION_STATUS"
        cumCaseSeriesExecutionStatus column: "CUM_CASE_SERIES_EXEC_STATUS"
        autoShareWithUser joinTable: [name: "AUTO_SHARE_WITH_USER_ECONFIG", column: "AUTO_SHARE_WITH_USERID", key: "CONFIG_ID"]
        autoShareWithGroup joinTable: [name: "AUTO_SHARE_WITH_GROUP_ECONFIG", column: "AUTO_SHARE_WITH_GROUPID", key: "CONFIG_ID"]
        removedUsers sqlType: "varchar2(2000 CHAR)"
        productName sqlType: DbUtil.longStringType
        allProducts sqlType: DbUtil.longStringType
        caseSeriesSpotfireFile sqlType: "varchar2(2000 CHAR)"
        stratificationColumnsDataMining column: "STRAT_COL_DATA_MINING"
        productHierarchyMasterTmplt column: "PRD_HIERARCHY_MASTER_TMPLT"
        eventHierarchyMasterTmplt column: "EVENT_HIERARCHY_MASTER_TMPLT"
        alertGroupId column: "ALERT_GROUP_ID"
    }

    static constraints = {
        reviewDueDate(nullable:true)
        assignedTo nullable: true, blank: true, validator: { value, obj ->
            def result = true
            if(!obj.assignedTo){
                result = obj.assignedToGroup ? true :  'assignedTo.nullable'
            }
            return result
        }
        assignedToGroup nullable: true, blank: true
        executedAlertQueryId nullable: true
        executedAlertForegroundQueryId nullable: true
        executedAlertDateRangeInformation nullable: true
        pvrCaseSeriesId(nullable: true)
        pvrCumulativeCaseSeriesId(nullable: true)
        faersCaseSeriesId(nullable: true)
        faersCumCaseSeriesId(nullable: true)
        reportId(nullable: true)
        caseSeriesExecutionStatus(nullable:true)
        cumCaseSeriesExecutionStatus(nullable:true)
        reportExecutionStatus(nullable:true)
        priority nullable: true
        configId(nullable: false)
        evdasDateRange nullable : true
        faersDateRange nullable : true
        vaersDateRange nullable : true
        vigibaseDateRange nullable : true
        dispCounts nullable: true
        requiresReviewCount nullable: true
        removedUsers nullable: true
        productName nullable: true
        caseSeriesSpotfireFile nullable: true
        isTemplateAlert nullable: true
        masterExConfigId nullable: true
        runCount nullable: true
        disabledDssNodes (nullable: true, maxSize: 4000)
        stratificationColumns nullable: true, blank: true
        stratificationColumnsDataMining nullable: true, blank: true
        vigibaseCaseSeriesId(nullable: true)
        vigibaseCumCaseSeriesId(nullable: true)
        vaersCaseSeriesId(nullable: true)
        vaersCumCaseSeriesId(nullable: true)
        selectedDataSheet nullable: true, blank: true, maxSize: 32000
        datasheetType nullable: true, blank: true, maxSize: 32000
        isDatasheetChecked nullable: true, blank: true
        adjustmentTypeEnum(nullable: true)
        skippedAlertId(nullable: true)
        skippedAlertGroupCode(nullable: true)
        allProducts nullable: true
        drugTypeName nullable: true
        isStandalone nullable: true
        criteriaCounts nullable: true
        isMultiIngredient nullable: true
        priority nullable: true
        eventHierarchyMasterTmplt nullable: true
        productHierarchyMasterTmplt nullable: true
        exMasterTemplateId nullable: true
        isMasterTemplateAlert nullable: true
        studyDrugs nullable: true
        alertGroupId nullable: true
    }

    static namedQueries = {
        viewableByUser { currentUser ->
            sharedWith {
                eq('isDeleted', false)
                eq('user', currentUser)
            }
        }

        viewableByUserAndNotReviewed { currentUser ->
            viewableByUser(currentUser)
            sharedWith {
                eq('status', ReportResultStatus.NON_REVIEWED)
            }
        }

        viewableByUserAndReviewed { currentUser ->
            viewableByUser(currentUser)
            sharedWith {
                eq('status', ReportResultStatus.REVIEWED)
            }
        }

        viewableByUserAndNew { currentUser ->
            viewableByUser(currentUser)
            sharedWith {
                eq('status', ReportResultStatus.NEW)
            }
        }

    }

    // To check the execution status of each result, if anyone has error returns error as status
    def getExecutionStatus() {
        def val = ReportExecutionStatus.COMPLETED.value()
        for (it in executedTemplateQueries) {
            if (it.reportResult.executionStatus == ReportExecutionStatus.ERROR) {
                val = ReportExecutionStatus.ERROR.value()
                break
            } else if (it.reportResult.executionStatus != ReportExecutionStatus.COMPLETED) {
                val = it.reportResult.executionStatus.value()
                break
            }
        }
        return val
    }

    public Set<ReportField> getSelectedColumnsForSection() {
        Set columnsList = []
        for (it in executedTemplateQueries) {
            if (it.executedTemplate instanceof ExecutedCaseLineListingTemplate) {
                columnsList.add(it.executedTemplate.getAllSelectedFieldsInfo().reportField as Set)
            }
        }
        return columnsList.flatten()
    }
    @Override
    String toString() {
        "${this.getClass().getSimpleName()} : ${this.id}"
    }

    def getModuleNameForMultiUseDomains() {
        return this.type == Constants.AlertConfigType.SINGLE_CASE_ALERT ? Constants.AlertConfigType.INDIVIDUAL_CASE_CONFIGURATIONS : Constants.AlertConfigType.AGGREGATE_CASE_CONFIGURATIONS
    }

    def getInstanceIdentifierForAuditLog() {
        return name + ": ${executedAlertDateRangeInformation}"
    }
}
