package com.rxlogix.config

import com.rxlogix.BaseConfiguration
import com.rxlogix.Constants
import com.rxlogix.SignalAuditLogService
import com.rxlogix.dto.ExecutionStatusDTO
import com.rxlogix.enums.AdjustmentTypeEnum
import com.rxlogix.enums.AuditLogCategoryEnum
import com.rxlogix.enums.DateRangeEnum
import com.rxlogix.hibernate.EscapedILikeExpression
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.MiscUtil
import grails.gorm.dirty.checking.DirtyCheck
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugins.orm.auditable.AuditEntityIdentifier
import grails.plugins.orm.auditable.ChildModuleAudit
import grails.util.Holders
import org.hibernate.FetchMode
import grails.validation.ValidationException
import org.hibernate.criterion.CriteriaSpecification

@DirtyCheck
class Configuration extends BaseConfiguration {
    static transients = ['skipAudit', 'skipNameValidation']
    //this ignore list is modified on the basis of type flag below getCustomIgnoreProperties
    static auditable = ['ignore':[]]
    def auditLogService
    def queryService
    def configurationService
    def signalAuditLogService

    List <TemplateQuery> templateQueries = [] // why?
    int asOfVersionDateDelta
    boolean executing = false
    Priority priority
    int reviewPeriod = 5
    String type
    List<Disposition> dispositions
    Long alertQueryId
    Long alertForegroundQueryId
    List<QueryValueList> alertQueryValueLists
    List<QueryValueList> alertForegroundQueryValueLists
    AlertDateRangeInformation alertDateRangeInformation
    ReportTemplate template
    List<User> shareWithUser = []
    List<Group> shareWithGroup = []
    Boolean isAutoAssignedTo = false
//    Boolean isAutoSharedWith = false
    List<User> autoShareWithUser = []
    List<Group> autoShareWithGroup = []
    Boolean isResume = false
    Long integratedConfigurationId
    Boolean isTemplateAlert= false
    Boolean isMasterTemplateAlert= false
    Long masterTemplateId
    Long configurationTemplateId
    Long masterConfigId
    Boolean isLatestMaster= false
    String selectedDataSheet
    String datasheetType = "CORE_SHEET"
    Boolean isDatasheetChecked = false

    Boolean isAutoPaused = false
    Boolean autoPausedEmailTriggered
    Boolean isManuallyPaused = false
    AdjustmentTypeEnum adjustmentTypeEnum
    Long skippedAlertId
    String skippedAlertGroupCode
    String futureScheduleDateJSON
    String alertDisableReason
    Boolean deletionInProgress = false
    DeletionStatus deletionStatus
    Date migratedToMartDate
    Boolean skipAudit = false
    Boolean isMultiIngredient = false
    Boolean includeWHODrugs = false
    String productHierarchyMasterTmplt
    String eventHierarchyMasterTmplt
    Long alertGroupId
    Boolean skipNameValidation = false
    String masterConfigMapping

    static belongsTo = [priority: Priority]
    static hasMany = [templateQueries: TemplateQuery, alertQueryValueLists: QueryValueList, alertForegroundQueryValueLists: QueryValueList, dispositions: Disposition, autoShareWithUser: User, autoShareWithGroup: Group]

    static mapping = {
        table name: "RCONFIG"

        id generator:'sequence', params:[sequence:'config_sequence']

        // workaround to pull in mappings from super class that is not a domain
        def superMapping = BaseConfiguration.mapping.clone()
        superMapping.delegate = delegate
        superMapping.call()

        templateQueries cascade: "all-delete-orphan"
        templateQueries joinTable: [name: "TEMPLT_QUERY", column: "ID", key: "RCONFIG_ID"], indexColumn: [name:"TEMPLT_QUERY_IDX"]
        productGroups joinTable: [name: "RCONFIGS_PROD_GRP", column: "PROD_GRP_ID", key: "RCONFIG_ID"], indexColumn: [name:"PROD_GRP_IDX"]

        executing column: "EXECUTING"
        asOfVersionDateDelta column: "AS_OF_VERSION_DATE_DELTA"
        isAutoTrigger column: "IS_AUTO_TRIGGER"
        dispositions joinTable: [name: "RCONFIG_DISPOSITION", key: "CONFIGURATION_DISPOSITION_ID", column: "DISPOSITION_ID"], indexColumn: [name:"RCONFIG_DISPOSITION_IDX"]
        alertQueryId column: "ALERT_QUERY_ID"
        alertForegroundQueryId column: "ALERT_FG_QUERY_ID"
        alertQueryValueLists joinTable: [name: "ALERT_QUERY_VALUES", column: "QUERY_VALUE_ID", key: "ALERT_QUERY_ID"], indexColumn: [name: "QUERY_VALUE_IDX"]
        alertForegroundQueryValueLists joinTable: [name: "ALERT_QUERY_FG_VALUES", column: "FG_QUERY_VALUE_ID", key: "ALERT_FG_QUERY_ID"], indexColumn: [name: "FG_QUERY_VALUE_IDX"]
        alertDateRangeInformation column: "ALERT_DATA_RANGE_ID"
        template column: "TEMPLATE_ID"
        selectedDataSheet column: "SELECTED_DATASHEET"
        datasheetType column: "DATASHEET_TYPE"
        isDatasheetChecked column: "IS_DATASHEET_CHECKED"
        shareWithUser joinTable: [name:"SHARE_WITH_USER_CONFIG", column:"SHARE_WITH_USERID", key:"CONFIG_ID"]
        shareWithGroup joinTable: [name:"SHARE_WITH_GROUP_CONFIG", column:"SHARE_WITH_GROUPID", key:"CONFIG_ID"]
        autoShareWithUser joinTable: [name:"AUTO_SHARE_WITH_USER_CONFIG", column:"AUTO_SHARE_WITH_USERID", key:"CONFIG_ID"]
        autoShareWithGroup joinTable: [name:"AUTO_SHARE_WITH_GROUP_CONFIG", column:"AUTO_SHARE_WITH_GROUPID", key:"CONFIG_ID"]
        autoPausedEmailTriggered column: 'AUTO_PAUSED_EMAIL_SENT'
        futureScheduleDateJSON column: "FUTURE_SCHEDULE_DATE"
        includeWHODrugs column: 'INCLUDE_WHO_DRUGS'
        productHierarchyMasterTmplt column: "PRD_HIERARCHY_MASTER_TMPLT"
        eventHierarchyMasterTmplt column: "EVENT_HIERARCHY_MASTER_TMPLT"
        alertGroupId column: "ALERT_GROUP_ID"
    }

    static constraints = {
        name(validator: { val, obj ->
            if (obj.skipNameValidation) {
                return true // Bypass the name validation if the flag is set to true
            }
            def res = MiscUtil.validator(val, "Name", Constants.SpecialCharacters.DEFAULT_CHARS as String[])
            if (res != true) {
                return res
            }
            if (obj.isMasterTemplateAlert && !val.toLowerCase().contains("[product name]".toLowerCase())) {
                return "com.rxlogix.config.alert.name.must.contain.product.name"
            }
            //Name is unique to user
            if (!obj.id || obj.isDirty("name")) {
                long count = Configuration.createCriteria().count {
                    ilike('name', "${val}")
                    eq('owner', obj.owner)
                    if (obj.integratedConfigurationId) {
                        eq('selectedDatasource', obj.selectedDatasource)
                    }
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
        asOfVersionDateDelta(nullable: true)
        templateQueries(nullable: true)
        type nullable: true
        reviewPeriod nullable: true, blank: true, min: 0
        dispositions nullable: true
        assignedTo nullable: true, blank: true, validator: { value, obj ->
            def result = true
            if(!obj.assignedTo){
                result = obj.assignedToGroup ? true :  'assignedTo.nullable'
            }
            return result
        }
        assignedToGroup nullable: true, blank: true
        alertQueryId nullable: true
        alertForegroundQueryId nullable: true
        alertDateRangeInformation nullable: true
        template nullable: true
        alertQueryValueLists (cascade: 'all-delete-orphan', validator: { lists, obj ->
            boolean hasValues = true
            lists.each {
                if (!it.validate()) {
                    hasValues = false
                }
            }
            if (!hasValues) {
                return "com.rxlogix.config.parameterValues.valueless"
            }
            return hasValues
        })
        alertForegroundQueryValueLists (cascade: 'all-delete-orphan', validator: { lists, obj ->
            boolean hasValues = true
            lists.each {
                if (!it.validate()) {
                    hasValues = false
                }
            }
            if (!hasValues) {
                return "com.rxlogix.config.parameterValues.valueless"
            }
            return hasValues
        })
        integratedConfigurationId nullable:true
        isTemplateAlert nullable: true
        isMasterTemplateAlert nullable: true
        masterTemplateId nullable: true
        configurationTemplateId nullable: true
        masterConfigId nullable: true
        isLatestMaster nullable: true
        selectedDataSheet nullable: true, blank: true, maxSize: 32000
        datasheetType nullable: true, blank: true, maxSize: 32000
        isDatasheetChecked nullable: true, blank: true
        isAutoPaused nullable: true
        autoPausedEmailTriggered nullable: true
        isManuallyPaused nullable: true
        adjustmentTypeEnum nullable: true
        skippedAlertId nullable: true
        skippedAlertGroupCode nullable: true
        futureScheduleDateJSON nullable: true
        alertDisableReason nullable: true
        deletionStatus nullable: true
        deletionInProgress nullable: true
        migratedToMartDate nullable: true
        isMultiIngredient nullable: true
        priority nullable: true, validator: { value, obj ->
            def result = true
            if (!obj.priority) {
                result = obj.adhocRun ? true : 'com.rxlogix.config.Configuration.priority.nullable'
            }
            return result
        }
        drugType(validator: { val, obj ->
            //each datasource must contain its respective values
            // for aggregate alert
            if(obj.type=='Aggregate Case Alert'){
                Boolean productRuleExist = false
                List productRuleErrorDS = []
                if (obj.selectedDatasource.contains('pva')) {
                    List list = obj.drugType.split(',')
                    list.each {
                        if (it.isInteger()) {
                            productRuleExist = true
                        }
                    }
                    if (!productRuleExist) {
                        productRuleErrorDS << 'Safety DB'
                    }
                }
                obj.selectedDatasource.split(',').each {
                    if (it != 'pva' && it != 'eudra' && !obj.drugType.toLowerCase().contains(it)) {
                        if(it=='vigibase'){
                            productRuleErrorDS << 'VigiBase'
                        }else{
                            productRuleErrorDS << it.toUpperCase()
                        }
                        productRuleExist = false
                    }
                }
                if (!productRuleExist && !productRuleErrorDS.isEmpty()) {
                    if(obj.selectedDatasource.split(',').size()==1){
                        return "app.label.product.rule.error.stand.alone"
                    }else{
                        return ["app.label.product.rule.error", productRuleErrorDS.join(',')]
                    }
                }
                return true
            }
        })
        includeWHODrugs nullable: true
        eventHierarchyMasterTmplt nullable: true
        productHierarchyMasterTmplt nullable: true
        alertGroupId nullable: true
        masterConfigMapping nullable: true
    }

    static namedQueries = {

        //Fetch the list of configurations meant for the auto trigger alert.
        autoAlertTriggerConfiguration {
            isNotNull("nextRunDate")
            gt("alertTriggerCases", 0)
            eq('isEnabled', true)
            eq('isDeleted', false)
            eq("type", Constants.AlertConfigType.SINGLE_CASE_ALERT)
        }

        findAllScheduledForUser {ExecutionStatusDTO executionStatusDTO ->
            isNotNull('nextRunDate')
            eq('executing', false)
            eq('isEnabled', true)
            eq('isDeleted', false)
            eq('type',executionStatusDTO.alertType.value())
            eq('workflowGroup.id',executionStatusDTO.workflowGroupId)
            fetchMode("owner", FetchMode.JOIN)
            if(executionStatusDTO.alertType == AlertType.AGGREGATE_CASE_ALERT &&
                    (!SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_CONFIGURATION") && SpringSecurityUtils.ifAnyGranted("ROLE_FAERS_CONFIGURATION"))){
                ilike('selectedDatasource',"%${Constants.DataSource.FAERS}%")
            } else if(executionStatusDTO.alertType == AlertType.AGGREGATE_CASE_ALERT &&
                    (!SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_CONFIGURATION") && SpringSecurityUtils.ifAnyGranted("ROLE_VAERS_CONFIGURATION"))){
                ilike('selectedDatasource',"%${Constants.DataSource.VAERS}%")
            } else if(executionStatusDTO.alertType == AlertType.AGGREGATE_CASE_ALERT &&
                    (!SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_CONFIGURATION") && SpringSecurityUtils.ifAnyGranted("ROLE_VIGIBASE_CONFIGURATION"))){
                ilike('selectedDatasource',"%${Constants.DataSource.VIGIBASE}%")
            } else if(executionStatusDTO.alertType == AlertType.AGGREGATE_CASE_ALERT &&
                    (!SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_CONFIGURATION") && SpringSecurityUtils.ifAnyGranted("ROLE_JADER_CONFIGURATION"))){
                ilike('selectedDatasource',"%${Constants.DataSource.JADER}%")
            }  else if(executionStatusDTO.alertType == AlertType.AGGREGATE_CASE_ALERT &&
                    (SpringSecurityUtils.ifAnyGranted("ROLE_AGGREGATE_CASE_CONFIGURATION") && !SpringSecurityUtils.ifAnyGranted("ROLE_FAERS_CONFIGURATION") && !SpringSecurityUtils.ifAnyGranted("ROLE_VAERS_CONFIGURATION") && !SpringSecurityUtils.ifAnyGranted("ROLE_VIGIBASE_CONFIGURATION") && !SpringSecurityUtils.ifAnyGranted("ROLE_JADER_CONFIGURATION"))){
                ilike('selectedDatasource',"%${Constants.DataSource.PVA}%")
            }
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

    static def getNextConfigurationToExecute(List<Long> currentlyRunningIds, String dataSource, String alertType, boolean fetchAllConfig = false) {
        List<Configuration> configurationList = Configuration.createCriteria().list {
            and {
                lte('nextRunDate', new Date())
                eq('isEnabled', true)
                isNull('masterConfigId')
                eq('executing', false)
                eq('isDeleted', false)
                like('selectedDatasource', "$dataSource%")
                eq("type", alertType)
                if (currentlyRunningIds) {
                    not {
                        'in'('id', currentlyRunningIds)
                    }
                }
                isNull("masterConfigId")
                isNull("masterTemplateId")
                eq("isMasterTemplateAlert", false)
                ne("isAutoPaused", true)
                ne("isManuallyPaused", true)
                or {
                    isNull("adjustmentTypeEnum")
                    not {
                        'in'('adjustmentTypeEnum', [AdjustmentTypeEnum.FAILED_ALERT_PER_SKIPPED_EXECUTION, AdjustmentTypeEnum.FAILED_SINGLE_ALERT_FOR_ALL_SKIPPED_EXECUTION])
                    }
                }
            }
            order('nextRunDate', 'asc')
            if (!fetchAllConfig) {
                maxResults(1)
            }
        }
        if (configurationList && !fetchAllConfig) {
            return configurationList[0]
        } else if (configurationList){
            return configurationList
        } else {
            return null
        }
    }

    static Configuration getNextAlertBurstingConfigurationToExecute(List<Long> currentlyRunningIds, String dataSource, String alertType) {
        List<Configuration> configurationList = Configuration.createCriteria().list {
            and {
                lte('nextRunDate', new Date())
                eq('isEnabled', true)
                isNull('masterConfigId')
                eq('executing', false)
                eq('isDeleted', false)
                like('selectedDatasource', "$dataSource%")
                eq("type", alertType)
                eq("isMasterTemplateAlert", true)
                if (currentlyRunningIds) {
                    not {
                        'in'('id', currentlyRunningIds)
                    }
                }
                isNull("masterConfigId")
                ne("isAutoPaused", true)
                ne("isManuallyPaused", true)
                or {
                    isNull("adjustmentTypeEnum")
                    not {
                        'in'('adjustmentTypeEnum', [AdjustmentTypeEnum.FAILED_ALERT_PER_SKIPPED_EXECUTION, AdjustmentTypeEnum.FAILED_SINGLE_ALERT_FOR_ALL_SKIPPED_EXECUTION])
                    }
                }
            }
            order('nextRunDate', 'asc')
            maxResults(1)
        }
        if (configurationList) {
            return configurationList[0]
        }
        return null
    }

    static List<Configuration> fetchAutoPausedConfigurations(String alertType,List<Long> currentlyRunningIds) {

        List<Configuration> configurationList = Configuration.createCriteria().list {
            and {
                lte('nextRunDate', new Date())
                eq('isEnabled', true)
                isNull('masterConfigId')
                eq('executing', false)
                eq('isDeleted', false)
                eq("type", alertType)
                isNull("masterConfigId")
                eq("isAutoPaused", true)
                eq("isManuallyPaused", false)
                if (currentlyRunningIds) {
                    not {
                        'in'('id', currentlyRunningIds)
                    }
                }
            }
            order('nextRunDate', 'asc')
        }

        return configurationList
    }
    def getExecutionStatus() {
        return ReportExecutionStatus.SCHEDULED.value()
    }

    def getDisplayDescription() {
        description ? description : 'None'
    }

    public int isAnyCumulativeTQ() {
        templateQueries.any {
            it.dateRangeInformationForTemplateQuery?.dateRangeEnum == DateRangeEnum.CUMULATIVE
        } ? 1 : 0
    }

    String getQueriesIdsAsString() {
        queryService?.getQueriesIdsAsString(alertQueryId)
    }
    String getForegroundQueriesIdsAsString() {
        queryService.getQueriesIdsAsString(alertForegroundQueryId)
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
    String toString(){
        "$name"
    }

    def getInstanceIdentifierForAuditLog() {
        return name + ": ${alertDateRangeInformation}"
    }

    def getModuleNameForMultiUseDomains() {
        return this.type == Constants.AlertConfigType.SINGLE_CASE_ALERT ? Constants.AlertConfigType.INDIVIDUAL_CASE_CONFIGURATIONS : Constants.AlertConfigType.AGGREGATE_CASE_CONFIGURATIONS
    }

    void revertConfigurationToPreviousVersion(ExecutedConfiguration executedConfiguration){
        Constants.AlertStatus.BASE_CONFIGURATION_PROPERTIES.each { property ->
            this?."${property}" = executedConfiguration?."${property}"
        }
    }

    List<String> getCustomIgnoreProperties() {
        List<String> ignoreList = [
                'alertForegroundQueryId', 'alertForegroundQueryName', 'applyAlertStopList', 'deletionInProgress', 'asOfVersionDateDelta', 'isPublic', 'isEnabled', 'totalExecutionTime', 'executing', 'numOfExecutions',
                'productGroups', 'nextRunDate', 'lastUpdated', 'productDictionarySelection', 'repeatExecution', 'alertQueryId', 'isResume', 'delimiter', 'operator', 'version', 'lastUpdated', 'lastUpdatedBy',
                'dateCreated', 'createdBy', 'modifiedBy', 'deletionStatus', 'deletionInProgress', 'isLatestMaster', 'isPublic', 'createdBy', 'dateCreated', 'isAutoPaused', 'isAutoTrigger', 'isCaseSeries',
                'isManuallyPaused', 'isResume', 'limitPrimaryPath', 'modifiedBy', 'reviewPeriod', 'suspectProduct', 'template', 'workflowGroup', 'adjustPerScheduleFrequency', 'configSelectedTimeZone', 'alertQueryName',
                'alertCaseSeriesId', 'alertCaseSeriesName', 'isStandalone', 'templateQueries', 'isAutoAssignedTo', 'includeMedicallyConfirmedCases', 'type', 'drugClassification', 'drugRecordNumbers'
        ]

        if (this.type == Constants.AlertConfigType.AGGREGATE_CASE_ALERT) {
            ignoreList += ['alertTriggerCases', 'alertTriggerDays', 'missedCases']
            if (!this.selectedDatasource.contains(Constants.DataSource.PVA)) {
                ignoreList += ['includeWHODrugs']
            }
        } else {
            ignoreList += ['dataMiningVariable', 'dataMiningVariableValue', 'groupBySmq', 'selectedDatasource', 'datasheetType', 'isDatasheetChecked', 'foregroundSearch']
        }

        if (Holders.config.alertStopList) {
            ignoreList -= ["applyAlertStopList"]
        }

        if (Holders.config.configurations.include.locked.versions.enabled) {
            ignoreList -= ["includeLockedVersion"]
        }
        return ignoreList
    }

    def getEntityValueForDeletion(){
        String justification = signalAuditLogService.getActionJustification("com.rxlogix.config.Configuration", id)
        return "Name-${name}, Date Range: ${alertDateRangeInformation},Assigned To:${assignedToGroup ?: assignedTo}, Share With Users-${shareWithUsers ? shareWithUsers.join(', ') : ""}, Share With Groups-${shareWithGroups ? shareWithGroups.join(', ') : ""}, Owner-${owner}, Justification-${justification}"
    }

    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        if (newValues && (oldValues == null)) {
            newValues.put("dateRangeEnum", alertDateRangeInformation?.dateRangeEnum)
           if(alertDateRangeInformation?.dateRangeEnum in [DateRangeEnum.LAST_X_DAYS, DateRangeEnum.LAST_X_MONTHS, DateRangeEnum.LAST_X_WEEKS, DateRangeEnum.LAST_X_YEARS]){
               newValues.put("relativeDateRangeValue", alertDateRangeInformation?.relativeDateRangeValue)
           }
        }
        if (newValues.containsKey("alertDateRangeInformation") && alertDateRangeInformation?.dateRangeEnum == DateRangeEnum.CUMULATIVE) {
            newValues.put('alertDateRangeInformation',"Cumulative")
        }
        return [newValues: newValues, oldValues: oldValues]
    }

}
