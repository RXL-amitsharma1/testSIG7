package com.rxlogix.config

import com.rxlogix.Constants
import com.rxlogix.SignalAuditLogService
import com.rxlogix.dto.ExecutionStatusDTO
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.DbUtil
import com.rxlogix.util.MiscUtil
import org.hibernate.FetchMode

class LiteratureConfiguration implements AlertUtil {
    static auditable = [ignore:['deletionInProgress', 'executing', 'isManuallyPaused', 'numOfExecutions', 'lastUpdated', 'lastUpdatedBy', 'dateCreated', 'createdBy','modifiedBy','updatedBy'
                                , 'totalExecutionTime', 'workflowGroup', 'isEnabled', 'isResume','nextRunDate','repeatExecution','isLatest','deletionStatus','literatureQueryId']]
    def signalAuditLogService
    String name
    User owner
    String scheduleDateJSON
    Date nextRunDate
    String productSelection
    String eventSelection
    String searchString
    int numOfExecutions = 0
    boolean isLatest = false
    String configSelectedTimeZone = "UTC"
    String productGroupSelection
    String eventGroupSelection

    LiteratureDateRangeInformation dateRangeInformation
    Group workflowGroup
    //Common domain params.
    Date dateCreated
    Date lastUpdated
    String createdBy
    String modifiedBy

    Priority priority
    User assignedTo
    Group assignedToGroup
    boolean isEnabled = true
    boolean repeatExecution = false
    boolean isDeleted = false
    boolean executing = false
    String selectedDatasource
    Boolean isResume = false


    long totalExecutionTime = 0 // this will be in milliseconds

    List<User> shareWithUser = []
    List<Group> shareWithGroup = []
    Boolean isManuallyPaused = false
    String futureScheduleDateJSON
    Boolean deletionInProgress = false
    DeletionStatus deletionStatus
    Long literatureQueryId
    String literatureQueryName

    static hasMany = [shareWithUser: User, shareWithGroup: Group]

    static constraints = {
        name(validator: { val, obj ->
            def res = MiscUtil.validator(val, "Name", Constants.SpecialCharacters.DEFAULT_CHARS as String[])
            if (res != true) {
                return res
            }
            //Name is unique to user
            if (!obj.id || obj.isDirty("name")) {
                long count = LiteratureConfiguration.createCriteria().count {
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
        lastUpdated(nullable: true)
        nextRunDate(nullable: true)
        scheduleDateJSON(nullable: true, maxSize: 1024, validator:{val, obj ->
            def result = true
            if(!obj.scheduleDateJSON){
                result = 'app.label.datetime.invalid'
            }
            return result
        })
        assignedTo nullable: true, blank: true, validator: { value, obj ->
            def result = true
            if(!obj.assignedTo){
                result = obj.assignedToGroup ? true :  'assignedTo.nullable'
            }
            return result
        }
        assignedToGroup nullable: true, blank: true
        numOfExecutions(min: 0, nullable: false)
        createdBy(nullable: false, maxSize: 100)
        modifiedBy(nullable: false, maxSize: 100)
        productSelection(nullable: true, maxSize: 8192)
        eventSelection(nullable: true, maxSize: 8192)
        searchString(nullable: true)
        dateRangeInformation(nullable: true)
        selectedDatasource nullable: true
        productGroupSelection nullable: true,validator: {value,obj->
            if(value && obj.productSelection){
                return ["com.rxlogix.literature.productAndGroup.notNull"]
            }
        }
        eventGroupSelection nullable: true, validator: {value,obj->
            if(value && obj.eventSelection){
                return ["com.rxlogix.literature.eventAndGroup.notNull"]
            }

        }
        isManuallyPaused nullable: true
        futureScheduleDateJSON nullable: true
        deletionStatus nullable: true
        deletionInProgress nullable: true
        literatureQueryId nullable: true
        literatureQueryName nullable: true
    }

    static mapping = {
        table name: "LITERATURE_CONFIG"
        productGroupSelection sqlType: "varchar2(8000 CHAR)"
        eventGroupSelection sqlType: "varchar2(8000 CHAR)"
        name column: "NAME"
        owner column: "PVUSER_ID"
        scheduleDateJSON column: "SCHEDULE_DATE"
        nextRunDate column: "NEXT_RUN_DATE"
        selectedDatasource column: "SELECTED_DATA_SOURCE"
        productSelection column: "PRODUCT_SELECTION", sqlType: DbUtil.longStringType
        eventSelection column: "EVENT_SELECTION", sqlType: DbUtil.longStringType
        shareWithUser joinTable: [name:"SHARE_WITH_USER_LITR_CONFIG", column:"SHARE_WITH_USERID", key:"CONFIG_ID"]
        shareWithGroup joinTable: [name:"SHARE_WITH_GROUP_LITR_CONFIG", column:"SHARE_WITH_GROUPID", key:"CONFIG_ID"]
        futureScheduleDateJSON column: "FUTURE_SCHEDULE_DATE"
    }

    boolean isEditableBy(User currentUser) {
        return (currentUser?.isAdmin() || owner == currentUser)
    }

    static LiteratureConfiguration getNextConfigurationToExecute(List<Long> currentlyRunningIds) {
        List<LiteratureConfiguration> configurationList = LiteratureConfiguration.createCriteria().list {
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
            }
            order 'nextRunDate', 'asc'
            maxResults(1)
        }
        if (configurationList) {
            return configurationList[0]
        }
        return null
    }

    def getProductNameList() {
        String prdName = getNameFieldFromJson(this.productSelection)
        def prdList = []
        if (prdName) {
            prdList = prdName.tokenize(',')
        }
        return prdList
    }

    def getEventSelectionList() {
        String evtName = getNameFieldFromJson(this.eventSelection)
        if (evtName) {
            evtName.toLowerCase().tokenize(',')
        } else {
            []
        }
    }
    def getIdsForProductGroup(){
        String prdGroupIdString=getIdsForProductGroup(this.productGroupSelection)
        if(prdGroupIdString) {
            prdGroupIdString.tokenize(',').collect{it as Integer}
        } else {
            []
        }
    }
    def getIdsForEventGroup(){
        String prdGroupIdString=getIdsForProductGroup(this.eventGroupSelection)
        if(prdGroupIdString) {
            prdGroupIdString.tokenize(',').collect{it as Integer}
        } else {
            []
        }
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

    public Set<User> getShareWithUsers() {
        Set<User> users = []
        if (this.shareWithUser) {
            users.addAll(this.shareWithUser)
        }
        return users
    }

    public Set<Group> getShareWithGroups() {
        Set<Group> userGroups = []
        if (this.shareWithGroup) {
            userGroups.addAll(this.shareWithGroup)
        }
        return userGroups
    }

    @Override
    String toString() {
        "${this.getClass().getSimpleName()} : ${this.id}"
    }

    def getModuleNameForMultiUseDomains() {
        return Constants.AlertConfigType.LITERATURE_ALERT_CONFIGURATIONS
    }

    def getInstanceIdentifierForAuditLog() {
        return name + ": ${dateRangeInformation}"
    }
    def getEntityValueForDeletion(){
        String justification = signalAuditLogService.getActionJustification("com.rxlogix.config.LiteratureConfiguration", id)
        return "Name: ${name}, Date Range: ${dateRangeInformation},Assigned To: ${assignedToGroup ?: assignedTo}, Share With Users: ${shareWithUsers ? shareWithUsers.join(', ') : ""}, Share With Groups :${shareWithGroups ? shareWithUsers.join(', ') : ""}, Owner:${owner}, Justification:${justification}"
    }

}
