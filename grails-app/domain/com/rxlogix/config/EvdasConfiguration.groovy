package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.SignalAuditLogService
import com.rxlogix.dto.ExecutionStatusDTO
import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.DbUtil
import com.rxlogix.util.MiscUtil
import grails.converters.JSON
import groovy.json.JsonSlurper
import org.hibernate.FetchMode

class EvdasConfiguration implements AlertUtil {
    static transients = ['skipAudit']
    static auditable = [ignore:['deletionInProgress', 'executing', 'isAutoPaused', 'isManuallyPaused', 'numOfExecutions'
                                , 'totalExecutionTime', 'workflowGroup', 'isEnabled', 'isResume', 'frequency','deletionStatus',
                                'nextRunDate','dateCreated','modifiedBy','isLatest','createdBy','lastUpdated','configSelectedTimeZone']]
    def signalAuditLogService
    String productSelection
    String productGroupSelection
    String eventGroupSelection
    String eventSelection

    Long query
    String queryName
    EVDASDateRangeInformation dateRangeInformation

    //Config Details Params
    User owner
    String name
    Priority priority
    SignalStrategy strategy
    String description

    //Config Schedule Params
    String scheduleDateJSON
    String blankValuesJSON
    String configSelectedTimeZone = "UTC"
    int numOfExecutions = 0
    long totalExecutionTime = 0

    Date nextRunDate
    boolean executing = false
    boolean isDeleted = false
    boolean isEnabled = true

    //Common domain params.
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy
    boolean adhocRun = false

    User assignedTo
    Group assignedToGroup

    Group workflowGroup

    String referenceNumber
    String frequency

    List<User> shareWithUser = []
    List<Group> shareWithGroup = []
    Boolean isResume = false

    Long integratedConfigurationId
    Long masterConfigId
    String selectedDataSheet
    Boolean isDatasheetChecked = false
    String datasheetType = "CORE_SHEET"

    Boolean isAutoPaused = false
    Boolean isManuallyPaused = false
    String futureScheduleDateJSON
    Boolean autoPausedEmailTriggered
    String alertDisableReason
    Boolean deletionInProgress = false
    DeletionStatus deletionStatus
    Boolean skipAudit=false

    static hasMany = [sharedGroups: Group, shareWithUser: User, shareWithGroup: Group]

    static mapping = {
        table name: "EVDAS_CONFIG"
        id generator:'sequence', params:[sequence:'evdas_config_sequence']
        sharedGroups joinTable: [name: "EVDAS_CONFIGURATION_GROUPS", column: "GROUP_ID", key: "EVDAS_CONFIG_ID"], indexColumn: [name:"GROUP_IDX"]
        productSelection column: "PRODUCT_SELECTION", sqlType: DbUtil.longStringType, nullable: false
        eventSelection column: "EVENT_SELECTION", sqlType: DbUtil.longStringType
        scheduleDateJSON column: "SCHEDULE_DATE"
        configSelectedTimeZone column: "SELECTED_TIME_ZONE"
        blankValuesJSON column: "BLANK_VALUES", sqlType: DbUtil.longStringType
        numOfExecutions column: "NUM_OF_EXECUTIONS"
        query column: "SUPER_QUERY_ID"
        queryName column: "QUERY_NAME"
        totalExecutionTime column: "TOTAL_EXECUTION_TIME"
        workflowGroup column: "WORKFLOW_GROUP"
        selectedDataSheet column: "SELECTED_DATASHEET"
        datasheetType column: "DATASHEET_TYPE"
        isDatasheetChecked column: "IS_DATASHEET_CHECKED"
        shareWithUser joinTable: [name:"SHARE_WITH_USER_EVDAS_CONFIG", column:"SHARE_WITH_USERID", key:"CONFIG_ID"]
        shareWithGroup joinTable: [name:"SHARE_WITH_GROUP_EVDAS_CONFIG", column:"SHARE_WITH_GROUPID", key:"CONFIG_ID"]
        productGroupSelection sqlType: "varchar2(8000 CHAR)"
        eventGroupSelection sqlType: "varchar2(8000 CHAR)"
        autoPausedEmailTriggered column: "AUTO_PAUSED_EMAIL_SENT"
        futureScheduleDateJSON column: "FUTURE_SCHEDULE_DATE"
    }

    static constraints = {
        name(validator: { val, obj ->
            def res = MiscUtil.validator(val, "Name", Constants.SpecialCharacters.DEFAULT_CHARS as String[])
            if (res != true) {
                return res
            }
            //Name is unique to user
            if (!obj.id || obj.isDirty("name")) {
                long count = EvdasConfiguration.createCriteria().count {
                    ilike('name', "${val}")
                    eq('owner', obj.owner)
                    eq('isDeleted', false)
                    if (obj.id) {
                        ne('id', obj.id)
                    }
                }
                if (count) {
                    return "com.rxlogix.config.alert.name.unique.per.user"
                }
            }
        })
        productSelection(validator: { val, obj ->
            if (!val && !obj.productGroupSelection) {
                return "com.rxlogix.evdas.Configuration.productSelection.nullable"
            } else if ((val && obj.productGroupSelection) || (obj.productGroupSelection && JSON.parse(obj.productGroupSelection).size() > 1)) {
                return "com.rxlogix.evdas.Configuration.productSelection.multiple"
            }
        }, nullable: true)
        priority(validator: { value, obj ->
            def result = true
            if (!obj.priority) {
                result = obj.adhocRun ? true : 'com.rxlogix.config.Configuration.priority.nullable'
            }
            return result
        }, nullable: true)
        eventSelection nullable:true
        productGroupSelection nullable: true
        eventGroupSelection nullable: true
        strategy nullable: true
        sharedGroups nullable: true
        scheduleDateJSON(nullable: true, maxSize: 1024, validator:{val, obj ->
            def result = true
            if(!obj.scheduleDateJSON){
                result = 'app.label.datetime.invalid'
            }
            return result
        })
        blankValuesJSON nullable: true
        nextRunDate nullable:true
        numOfExecutions min:0, nullable:false
        description nullable: true, maxSize: 8000
        query nullable:true
        queryName nullable:true
        totalExecutionTime nullable:true
        adhocRun nullable: true
        frequency nullable: true
        referenceNumber nullable: true
        assignedTo nullable: true, validator: { value, obj ->
            def result = true
            if(!obj.assignedTo){
                result = obj.assignedToGroup ? true :  'assignedTo.nullable'
            }
            return result
        }
        assignedToGroup nullable: true
        workflowGroup nullable: true
        integratedConfigurationId nullable: true
        dateRangeInformation nullable: true
        masterConfigId nullable: true
        selectedDataSheet nullable: true, blank: true, maxSize: 32000
        datasheetType nullable: true, blank: true, maxSize: 32000
        isDatasheetChecked nullable: true, blank: true
        isAutoPaused nullable: true
        isManuallyPaused nullable: true
        futureScheduleDateJSON nullable: true
        autoPausedEmailTriggered nullable: true
        alertDisableReason nullable: true
        deletionStatus nullable: true
        deletionInProgress nullable: true
    }

    static EvdasConfiguration getNextConfigurationToExecute(List<Long> currentlyRunningIds) {
        List<EvdasConfiguration> configurationList = EvdasConfiguration.createCriteria().list {
            and {
                lte('nextRunDate', new Date())
                eq('executing', false)
                eq('isEnabled', true)
                eq('isDeleted', false)
                if (currentlyRunningIds) {
                    not {
                        'in'('id', currentlyRunningIds)
                    }
                }
                ne("isAutoPaused", true)
                ne("isManuallyPaused", true)
            }
            order 'nextRunDate', 'asc'
            maxResults(1)
        }
        if (configurationList) {
            return configurationList[0]
        }
        return null
    }

    static List<EvdasConfiguration> fetchAutoPausedConfigurations(List<Long> currentlyRunningIds) {
        List<EvdasConfiguration> configurationList = EvdasConfiguration.createCriteria().list {
            and {
                lte('nextRunDate', new Date())
                eq('executing', false)
                eq('isEnabled', true)
                eq('isDeleted', false)
                eq("isAutoPaused", true)
                eq("isManuallyPaused", false)
                if (currentlyRunningIds) {
                    not {
                        'in'('id', currentlyRunningIds)
                    }
                }
            }
            order 'nextRunDate', 'asc'
        }

        return configurationList
    }

    List getEventDictionaryValues() {
        List result = [[],[],[],[],[],[]]
        parseDictionary(result, eventSelection)
        return result
    }

    List getProductDictionaryValues() {
        List result = [[],[],[],[]]
        parseDictionary(result, productSelection)
        return result
    }

    def getProductNameList() {
        String prdName = getNameFieldFromJson(this.productSelection)
        if (prdName) {
            prdName.toLowerCase().tokenize(',')
        } else {
            []
        }
        return prdName
    }

    def getProductGroupList() {
        getIdsForProductGroup(this.productGroupSelection)
    }

    private parseDictionary(List result, String dictionarySelection) {
        if (dictionarySelection) {
            Map values = new JsonSlurper().parseText(dictionarySelection)
            values.each { k, v ->
                if (!k.equals("isMultiIngredient")) {
                    int level = k.toInteger()
                    v.each {
                        result[level - 1].add(it["id"])
                    }
                }
            }
        }
    }

    boolean isEditableBy(User currentUser) {
        return (currentUser?.isAdmin() || owner == currentUser)
    }

    static namedQueries = {
        findAllScheduledForUser { ExecutionStatusDTO executionStatusDTO ->
            isNotNull('nextRunDate')
            eq('executing', false)
            eq('isEnabled', true)
            eq('isDeleted', false)
            eq('workflowGroup.id',executionStatusDTO.workflowGroupId)
            fetchMode("owner", FetchMode.JOIN)
            if (executionStatusDTO.searchString) {
                or {
                    iLikeWithEscape('name', "%${EscapedILikeExpression.escapeString(executionStatusDTO.searchString)}%")
                    "owner" {
                        iLikeWithEscape("fullName", "%${EscapedILikeExpression.escapeString(executionStatusDTO.searchString)}%")
                    }
                }
            }
        }
    }

    Integer getExpectedExecutionTime() {
        if (totalExecutionTime && numOfExecutions >= 1) {
            return totalExecutionTime
        }
        return 0
    }

    Set<User> getShareWithUsers() {
        Set<User> users = []
        if (this.shareWithUser) {
            users.addAll(this.shareWithUser)
        }
        return users
    }

    Set<Group> getShareWithGroups() {
        Set<Group> userGroups = []
        if (this.shareWithGroup) {
            userGroups.addAll(this.shareWithGroup)
        }
        return userGroups
    }
    @Override
    String toString(){
        "$name"
    }

    def getModuleNameForMultiUseDomains() {
        return Constants.AlertConfigType.EVDAS_ALERT_CONFIGURATIONS
    }

    def getInstanceIdentifierForAuditLog() {
        return name + ": ${dateRangeInformation}"
    }
    def getEntityValueForDeletion(){
        String justification = signalAuditLogService.getActionJustification("com.rxlogix.config.EvdasConfiguration", id)
        return "Name-${name}, Date Range: ${dateRangeInformation},Assigned To:${assignedToGroup ?: assignedTo}, Share With Users-${shareWithUsers ? shareWithUsers.join(',') : ""}, Share With Groups-${shareWithGroups ? shareWithGroups.join(',') : ""}, Owner-${owner}, Justification-${justification}"
    }

}
