package com.rxlogix.signal

import com.rxlogix.Constants
import com.rxlogix.config.*
import com.rxlogix.enums.DictionaryTypeEnum
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.*
import grails.util.Holders
import groovy.sql.Sql
import groovy.transform.ToString

@ToString(includes = ['name'])
class ValidatedSignal implements AlertUtil, GroovyInterceptable {
    static auditable = [ignore:[ 'allProducts' ,'allEvents','allEventsWithoutHierarchy' ,'allSmqs',
                                 'signalSummaryReportPreference','lastDispChange','productDictionarySelection',
                                 'productsAndGroupCombination','eventsAndGroupCombination','includeInAggregateReport',
                                 'isDueDateUpdated','lastUpdated','dateCreated','activities','signalRMMs', 'actions', 'meetings',
                                 'version', 'lastUpdated', 'lastUpdatedBy', 'createdBy','modifiedBy',
                                  'fieldProfile', 'signalStatusHistories', 'dispPerformedBy','actualDueDate','wsUpdated','assignmentType',
                                 'workflowGroup','udDropdown1','udDropdown2','actualDateClosed','signalMemoFlagForUndoDis']]

    def static dataSource
    def alertAttributesService
    def validatedSignalService
    def alertService
    def signalWorkflowService
    static attachmentable = true
    String name
    String products
    String productsSearch
    String events
    String eventsSearch
    Boolean isSystemUser = false
    Boolean skipAudit = false
    Disposition disposition
    User assignedTo
    Group assignedToGroup
    Priority priority
    String signalSummaryReportPreference
    String genericComment
    SignalStrategy strategy
    String initialDataSource
    String reasonForEvaluation
    String detectedBy
    String topic
    Date lastDispChange
    Date aggReportStartDate
    Date aggReportEndDate
    Date lastDecisionDate

    Disposition haSignalStatus
    String commentSignalStatus
    Date haDateClosed
    Date detectedDate
    Date dueDate
    String dispPerformedBy

    //Common db table fields
    String createdBy
    Date dateCreated
    Date lastUpdated
    String modifiedBy
    String description

    String productGroupSelection
    String eventGroupSelection
    List<String> allProducts
    String allEvents
    String allEventsWithoutHierarchy
    String allSmqs

    Group workflowGroup
    boolean includeInAggregateReport

    String productDictionarySelection
    String assignmentType

    String workflowState
    Date wsUpdated

    String productsAndGroupCombination
    String eventsAndGroupCombination

    Set<ValidatedSignal> linkedSignals
    List<SignalOutcome> signalOutcomes
    List<SignalStatusHistory> signalStatusHistories
    List<User> shareWithUser = []
    List<Group> shareWithGroup = []
    Date reviewDate
    String signalStatus
    boolean isDueDateUpdated;
    Date actualDueDate
    Date milestoneCompletionDate
    Date newDetectedDate
    Date actualDateClosed
    String udText1
    String udText2
    Date udDate1
    Date udDate2
    String udDropdown1
    String udDropdown2
    String ddValue1
    String ddValue2
    Boolean isMultiIngredient
    String justificationForAudit
    //for Adding custom values in audit log
    Map<String, Object> customAuditProperties
    Boolean signalMemoFlagForUndoDis = false

    static transients = ['productsSearch','eventsSearch','isSystemUser','skipAudit','justificationForAudit','customAuditProperties']

    static hasMany = [singleCaseAlerts        : SingleCaseAlert, aggregateAlerts: AggregateCaseAlert, literatureAlerts: LiteratureAlert,
                      adhocAlerts             : AdHocAlert, evdasAlerts: EvdasAlert, signalStatusHistories: SignalStatusHistory, sharedGroups: Group,
                      activities              : Activity, actions: Action, comments: AlertComment, meetings: Meeting, linkedSignals: ValidatedSignal,
                      signalOutcomes          : SignalOutcome, topicCategories: TopicCategory, alertDocuments: AlertDocument, actionTaken: String,
                      evaluationMethod        : String, configuration: Configuration, evdasConfiguration: EvdasConfiguration,
                      archivedSingleCaseAlerts: ArchivedSingleCaseAlert, archivedAggregateAlerts: ArchivedAggregateCaseAlert,
                      archivedLiteratureAlerts: ArchivedLiteratureAlert, archivedEvdasAlerts: ArchivedEvdasAlert,signalRMMs : SignalRMMs,
                      embaseLiteratureAlerts  : EmbaseLiteratureAlert, archivedEmbaseLiteratureAlerts : ArchivedEmbaseLiteratureAlert
    ]

    static belongsTo = [AggregateCaseAlert, SingleCaseAlert, EvdasAlert, AdHocAlert, LiteratureAlert, EmbaseLiteratureAlert, ArchivedEmbaseLiteratureAlert, ArchivedAggregateCaseAlert,
                        ArchivedSingleCaseAlert, ArchivedEvdasAlert, ArchivedLiteratureAlert]

    static constraints = {
        initialDataSource nullable: true
        topicCategories nullable: true
        singleCaseAlerts nullable: true
        aggregateAlerts nullable: true
        adhocAlerts nullable: true
        evdasAlerts nullable: true
        activities nullable: true
        strategy nullable: true
        comments nullable: true
        products nullable: true, blank: true
        actions nullable: true
        reasonForEvaluation nullable: true, maxSize: 8000
        events nullable: true
        name  nullable: false, maxSize: 255, validator: { value, object ->
            return MiscUtil.validator(value, "Signal Name", (Constants.SpecialCharacters.DEFAULT_CHARS-["#"]) as String[])
        }
        signalSummaryReportPreference nullable: true
        genericComment nullable: true
        alertDocuments nullable: true
        productDictionarySelection nullable: true
        aggReportStartDate nullable: true
        aggReportEndDate nullable: true
        lastDecisionDate nullable: true
        haSignalStatus nullable: true
        commentSignalStatus nullable: true, maxSize: 8000
        haDateClosed nullable: true
        actionTaken nullable: true
        assignedTo nullable: true, blank: true, validator: { value, obj ->
            def result = true
            if (!obj.assignedTo) {
                result = obj.assignedToGroup ? true : 'assignedTo.nullable'
            }
            return result
        }
        assignedToGroup nullable: true, blank: true
        description nullable: true, validator: { value, object ->
            return MiscUtil.validator(value, "Description", Constants.SpecialCharacters.TEXTAREA_CHARS as String[])
        }
        topic nullable: true
        evaluationMethod nullable: true
        detectedDate nullable: true
        detectedBy nullable: true
        configuration nullable: true
        evdasConfiguration nullable: true
        dueDate nullable: true
        productGroupSelection (nullable: true)
        eventGroupSelection nullable: true
        archivedSingleCaseAlerts nullable: true
        archivedAggregateAlerts nullable: true
        archivedEvdasAlerts nullable: true
        literatureAlerts nullable: true
        archivedLiteratureAlerts nullable: true
        embaseLiteratureAlerts nullable: true
        archivedEmbaseLiteratureAlerts nullable: true
        reviewDate nullable: true
        workflowState nullable: true
        wsUpdated nullable: true
        signalStatus nullable: true
        actualDueDate nullable: true
        milestoneCompletionDate nullable: true
        dispPerformedBy nullable: true
        newDetectedDate nullable: true
        lastDispChange nullable: true
        allProducts nullable: true
        allEvents nullable: true
        allEventsWithoutHierarchy nullable: true
        allSmqs nullable: true
        productsAndGroupCombination nullable: true
        eventsAndGroupCombination nullable: true
        actualDateClosed nullable: true
        udText1 column: "ud_text1", nullable: true, maxSize: 8000
        udText2 column: "ud_text2", nullable: true, maxSize: 8000
        udDate1 column: "ud_date1", nullable: true
        udDate2 column: "ud_date2", nullable: true
        udDropdown1 column: "ud_dropdown1", nullable: true,sqlType: DbUtil.longStringType
        udDropdown2 column: "ud_dropdown2",nullable: true,sqlType: DbUtil.longStringType
        ddValue1 column: "dd_value1",nullable: true,sqlType: DbUtil.longStringType
        ddValue2 column: "dd_value2",nullable: true,sqlType: DbUtil.longStringType
        isMultiIngredient nullable: true
        signalMemoFlagForUndoDis nullable: true
    }

    static mapping = {
        table name: "VALIDATED_SIGNAL"
        productGroupSelection sqlType: "varchar2(8000 CHAR)"
        eventGroupSelection sqlType: "varchar2(8000 CHAR)"
        productsAndGroupCombination sqlType: DbUtil.longStringType
        eventsAndGroupCombination sqlType: DbUtil.longStringType
        singleCaseAlerts joinTable: [name: "VALIDATED_SINGLE_ALERTS", column: "SINGLE_ALERT_ID", key: "VALIDATED_SIGNAL_ID"]
        literatureAlerts joinTable: [name: "VALIDATED_LITERATURE_ALERTS", column: "LITERATURE_ALERT_ID", key: "VALIDATED_SIGNAL_ID"]
        allProducts joinTable: [name: "VALIDATED_SIGNAL_ALL_PRODUCT", key: "VALIDATED_SIGNAL_ID", column: "SIGNAL_ALL_PRODUCTS", sqlType: "varchar2(8000 CHAR)"]
        adhocAlerts joinTable: [name: "VALIDATED_ADHOC_ALERTS", column: "ADHOC_ALERT_ID", key: "VALIDATED_SIGNAL_ID"]
        activities joinTable: [name: "VALIDATED_ALERT_ACTIVITIES", column: "ACTIVITY_ID", key: "VALIDATED_SIGNAL_ID"]
        aggregateAlerts joinTable: [name: "VALIDATED_AGG_ALERTS", column: "AGG_ALERT_ID", key: "VALIDATED_SIGNAL_ID"]
        evdasAlerts joinTable: [name: "VALIDATED_EVDAS_ALERTS", column: "EVDAS_ALERT_ID", key: "VALIDATED_SIGNAL_ID"]
        archivedSingleCaseAlerts joinTable: [name: "VALIDATED_ARCHIVED_SCA", column: "ARCHIVED_SCA_ID", key: "VALIDATED_SIGNAL_ID"]
        archivedLiteratureAlerts joinTable: [name: "VALIDATED_ARCHIVED_LIT_ALERTS", column: "ARCHIVED_LIT_ALERT_ID", key: "VALIDATED_SIGNAL_ID"]
        embaseLiteratureAlerts joinTable: [name: "VALIDATED_EMBASE_LITERATURE_ALERTS", column: "EMBASE_LITERATURE_ALERT_ID", key: "VALIDATED_SIGNAL_ID"]
        archivedEmbaseLiteratureAlerts joinTable: [name: "VALIDATED_ARCHIVED_EMBASE_LIT_ALERTS", column: "ARCHIVED_EMBASE_LIT_ALERT_ID", key: "VALIDATED_SIGNAL_ID"]
        archivedAggregateAlerts joinTable: [name: "VALIDATED_ARCHIVED_ACA", column: "ARCHIVED_ACA_ID", key: "VALIDATED_SIGNAL_ID"]
        archivedEvdasAlerts joinTable: [name: "VALIDATED_ARCH_EVDAS_ALERTS", column: "ARCHIVED_EVDAS_ALERT_ID", key: "VALIDATED_SIGNAL_ID"]
        comments joinTable: [name: "VALIDATED_ALERT_COMMENTS", column: "COMMENT_ID", key: "VALIDATED_SIGNAL_ID"]
        alertDocuments joinTable: [name: "VALIDATED_ALERT_DOCUMENTS", column: "ALERT_DOCUMENT_ID", key: "VALIDATED_SIGNAL_ID"]
        products column: "PRODUCTS", sqlType: DbUtil.longStringType
        events column: "EVENTS", sqlType: DbUtil.longStringType
        allEvents column: "ALL_EVENTS", sqlType: DbUtil.longStringType
        allEventsWithoutHierarchy column: "ALL_EVENTS_WITHOUT_HIERARCHY", sqlType: DbUtil.longStringType
        allSmqs column: "ALL_SMQS", sqlType: DbUtil.longStringType
        signalSummaryReportPreference column: "REPORT_PREFERENCE"
        topicCategories joinTable: [name: "VAL_SIGNAL_TOPIC_CATEGORY", column: "TOPIC_CATEGORY_ID", key: "VALIDATED_SIGNAL_ID"]
        sharedGroups joinTable: [name: "VALIDATED_SIGNAL_GROUP", column: "GROUP_ID", key: "VALIDATED_SIGNAL_ID"]
        genericComment type: "text", sqlType: "clob"
        description type: "text", sqlType: "clob"
        initialDataSource sqlType: "varchar(1000)"
        reasonForEvaluation column: "REASON_FOR_EVALUATION", sqlType: "varchar(8000)"
        evaluationMethod joinTable: [name: "VS_EVAL_METHOD", column: "EVALUATION_METHOD", key: "VALIDATED_SIGNAL_ID"]
        configuration joinTable: [name: "VALIDATED_SIGNAL_RCONFIG", column: "CONFIG_ID", key: "VALIDATED_SIGNAL_ID"]
        evdasConfiguration joinTable: [name: "VS_EVDAS_CONFIG", column: "EVDAS_CONFIG_ID", key: "VALIDATED_SIGNAL_ID"]
        workflowGroup column: "WORKFLOW_GROUP"
        isDueDateUpdated column: "IS_DUEDATE_UPDATED"
        linkedSignals joinTable: [name: "SIGNAL_LINKED_SIGNALS", key: "VALIDATED_SIGNAL_ID", column: "LINKED_SIGNAL_ID"]
        signalStatusHistories joinTable: [name: "SIGNAL_SIG_STATUS_HISTORY", key: "VALIDATED_SIGNAL_ID", column: "SIG_STATUS_HISTORY_ID"]
        signalOutcomes joinTable: [name: "VALIDATED_SIGNAL_OUTCOMES", key: "VALIDATED_SIGNAL_ID", column: "SIGNAL_OUTCOME_ID"]
        shareWithUser joinTable: [name:"SHARE_WITH_USER_SIGNAL", column:"SHARE_WITH_USERID", key:"VALIDATED_SIGNAL_ID"]
        shareWithGroup joinTable: [name:"SHARE_WITH_GROUP_SIGNAL", column:"SHARE_WITH_GROUPID", key:"VALIDATED_SIGNAL_ID"]
        signalRMMs joinTable: [name: "SIGNAL_SIG_RMMS", key: "VALIDATED_SIGNAL_ID", column: "SIG_RMM_ID"], indexColumn: [name: "PARAM_IDX"]
        topic type: 'text', sqlType: 'varchar(4000 char)'
        sort "name"

//      Below mapping has been added for deleting data from child table when signal is deleted
        actions cascade: "all-delete-orphan"

    }

    def beforeInsert =  {
        if (genericComment?.size()==8000) {
            List genericComments = genericComment.split(" ")
            genericComment = genericComments[0]+" "+"\0"+genericComments[1..genericComments.size()-1].join(" ")
        }
    }
    def beforeUpdate =  {
        if (genericComment?.size()==8000) {
            List genericComments = genericComment.split(" ")
            genericComment = genericComments[0]+" "+"\0"+genericComments[1..genericComments.size()-1].join(" ")
        }
    }

    Map toDto(String timeZone = "UTC") {
        Integer noOfPecs = this.aggregateAlerts?.size() + this.evdasAlerts?.size()
        [
                signalId         : this.id,
                signalName       : this.name?.trim()?.replaceAll("\\s{2,}", " "),
                productName      : getProductNameList(),
                noOfCases        : this.singleCaseAlerts?.size(),
                noOfPec          : noOfPecs,
                priority         : this.priority.displayName,
                assignedTo       : this.assignedTo?.fullName,
                actions          : this.actions?.size(),
                strategy         : this.strategy ? this.strategy.name : '-',
                newFollowUp      : 'New',
                dueDate          : '-',
                monitoringStatus : '-',
                initialDataSource: initialDataSource,
                lastSubmitted    : '-',
                dispPerformedBy  : this.dispPerformedBy?:Constants.Commons.SYSTEM,
                disposition      : this.disposition.displayName
        ]
    }

    Map toExportDto(String timeZone,workflowStates) {
        Map map = [
                signalId           : (null == this.id) ? "" : this.id.toString(),
                dateCreated        : this.dateCreated ? new Date( DateUtil.toDateStringPattern(this.dateCreated, DateUtil.DATEPICKER_FORMAT)) :null ,
                signalName         : this.name?.trim()?.replaceAll("\\s{2,}", " "),
                productName        : getProductAndGroupName(), //Added for PVS-71147
                eventName          : getEventAndGroupName(),
                detectedDate       : this.detectedDate ? new Date( DateUtil.toDateStringPattern(this.detectedDate, DateUtil.DATEPICKER_FORMAT)) :null ,
                topicInformation   : this.topic,
                detectedBy         : this.detectedBy,
                evaluationMethod   : this.evaluationMethod ? this.evaluationMethod?.sort()?.join(", ") : null,
                description        : this.description,
                reasonForEvaluation: this.reasonForEvaluation,
                comments           : this.genericComment?.split('\0')?.join(""),
                priority           : this.priority?.displayName,
                assignedTo         : this.assignedTo ? this.assignedTo?.fullName : this.assignedToGroup?.name,
                source             : this.initialDataSource?.replaceAll("##",", "),
                topicCategory      : this.topicCategories*.name?.sort({it?.toUpperCase()})?.join(", "),
                linkedSignal       : this.linkedSignals*.name?.join(", "),
                actionTaken        : this.actionTaken ? this.actionTaken?.sort()?.join(", ") : null,
                signalOutcome      : this.signalOutcomes*.name?.join(", "),
                aggReportStartDate : this.aggReportStartDate ? new Date( DateUtil.toDateStringPattern(this.aggReportStartDate, DateUtil.DATEPICKER_FORMAT)) :null ,
                aggReportEndDate   : this.aggReportEndDate ? new Date( DateUtil.toDateStringPattern(this.aggReportEndDate, DateUtil.DATEPICKER_FORMAT)) :null ,
                haSignalStatus     : this.haSignalStatus?.displayName,
                haDateClosed       : this.haDateClosed ? new Date( DateUtil.toDateStringPattern(this.haDateClosed, DateUtil.DATEPICKER_FORMAT)) :null ,
                commentSignalStatus: this.commentSignalStatus,
                disposition        : this.disposition?.displayName,
                shareWith          : (this.getShareWithGroups() + this.getShareWithUsers()).toString().replace(']', '').replace('[', ''),
                udText1            : this.udText1,
                udText2            : this.udText2,
                udDate1            : this.udDate1,
                udDate2            : this.udDate2,
                udDropdown1        : this.udDropdown1,
                udDropdown2        : this.udDropdown2,
                ddValue1            : this.ddValue1,
                ddValue2            : this.ddValue2
        ]
        List<String> statusList = alertAttributesService.get('signalHistoryStatus') + workflowStates
        List<String> statusLists=alertAttributesService.getUnsorted('signalHistoryStatus');
        if (SystemConfig.first().displayDueIn) {
            statusLists.add(Constants.WorkFlowLog.DUE_DATE)
            statusList.add(Constants.WorkFlowLog.DUE_DATE)
        }

        String validationDateStr=Holders.config.signal.defaultValidatedDate
        String defaultEnabledWorkflowValue=SignalWorkflowState.findByDefaultDisplay(true).value;
        String defaultEnabledWorkflow=defaultEnabledWorkflowValue.replaceAll(' ','');
        statusList.each { String status ->
            SignalStatusHistory signalStatusHistory = this.signalStatusHistories.find {
                it.signalStatus == status
            }
            String dateKey = "${status.trim().replace(' ', '').toLowerCase()}Date"
            String commentKey = "${status.trim().replace(' ', '').toLowerCase()}Comment"
            if(validationDateStr.equalsIgnoreCase(status)){
                map.put(dateKey, signalStatusHistory?.dateCreated)
                map.put(commentKey, signalStatusHistory?.statusComment?:"");
                map.put("validationdateDate", signalStatusHistory?.dateCreated ? new Date( DateUtil.toDateStringPattern(signalStatusHistory?.dateCreated, DateUtil.DATEPICKER_FORMAT)) :null )
                map.put("validationdateComment", signalStatusHistory?.statusComment?:"");
            }else{
             if(defaultEnabledWorkflow.concat("Date").equalsIgnoreCase(dateKey) && defaultEnabledWorkflowValue.equalsIgnoreCase(this.workflowState)){
                 Date dateCreated=(null!=signalStatusHistory)?signalStatusHistory.dateCreated:(null!=this)?this.dateCreated:null;
                    if(dateKey.equals("duedateDate")){
                        map.put(dateKey, this.actualDueDate ? new Date( DateUtil.toDateStringPattern(this.actualDueDate, DateUtil.DATEPICKER_FORMAT)) :null )
                    }else{
                        map.put(dateKey, dateCreated ? new Date( DateUtil.toDateStringPattern(dateCreated, DateUtil.DATEPICKER_FORMAT)) :null )
                    }
                 map.put(commentKey, (null!=signalStatusHistory)?signalStatusHistory.statusComment:"")
             }else{
                    if(dateKey.equals("duedateDate")){
                        map.put(dateKey, this.actualDueDate ? new Date( DateUtil.toDateStringPattern(this.actualDueDate, DateUtil.DATEPICKER_FORMAT)) :null )
                    }else{
                        map.put(dateKey, signalStatusHistory?.dateCreated ? new Date( DateUtil.toDateStringPattern(signalStatusHistory.dateCreated, DateUtil.DATEPICKER_FORMAT)) :null )
                    }
                 map.put(commentKey, signalStatusHistory?.statusComment?:"")
             }
            }
        }
        map
    }

    Map exportSignalMemoReport(String timeZone = "UTC") {
        Map map = [
                signalName         : this.name?.trim()?.replaceAll("\\s{2,}", " "),
                productName        : alertService.productSelectionSignal(this),
                eventName          : alertService.eventSelectionSignalWithSmq(this),
                detectedDate       : this.detectedDate ? DateUtil.toDateStringPattern(this.detectedDate, DateUtil.DATEPICKER_FORMAT) : null,
                detectedBy         : this.detectedBy,
                reasonForEvaluation: this.reasonForEvaluation,
                actionTaken        : this.actionTaken?.join(", "),
                status             : validatedSignalService.getSignalStatus(this),
                linkedSignal       : this.linkedSignals*.name?.join(", "),
                signalOutcome      : this.signalOutcomes*.name?.join(", "),
                evaluationMethod   : this.evaluationMethod?.join(", "),
                topicCategory      : this.topicCategories*.name?.join(", "),
                comments           : this.genericComment,
                signalSource       : this.initialDataSource,
        ]
        List<String> statusList = alertAttributesService?.get('signalHistoryStatus') as List<String>
        List<String> statusLists=alertAttributesService.getUnsorted('signalHistoryStatus');
        if(SystemConfig.first().displayDueIn)
        {
            statusLists.add(Constants.WorkFlowLog.DUE_DATE)
            statusList.add(Constants.WorkFlowLog.DUE_DATE)
        }
        String validationDateStr= Holders.config.signal.defaultValidatedDate
        statusList.each { String status ->
            SignalStatusHistory signalStatusHistory = this.signalStatusHistories.find {
                it.signalStatus == validationDateStr
            }
            map.put("dateValidated", signalStatusHistory?.dateCreated ? DateUtil.toDateStringPattern(signalStatusHistory.dateCreated, DateUtil.DATEPICKER_FORMAT) : null)
        }
        map
    }

    def getEventSelectionList() {
        String evtName = getNameFieldFromJson(this.events)
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

    def getProductNameList() {
        String prdName = getNameFieldFromJson(this.products)
        def prdList = Constants.Commons.DASH_STRING
        if (prdName) {
            prdList = prdName.tokenize(',')
        }
        return prdList
    }

    def getProductAndGroupNameList() {
        List data =[]
        if (this.productGroupSelection) {
            data.addAll(getGroupNameFieldFromJson(this.productGroupSelection).split(","))
        }
        if (this.products) {
            data.addAll(getAllProductNameFieldFromJson(this.products).split(","))
        }
        def prdList = Constants.Commons.DASH_STRING
        if (data) {
            prdList = data
        }
        return prdList
    }
    /**
     * Retrieves a concatenated string of product and group names.
     *
     * This method builds a list of names based on the current product and group selections.
     * It first adds group names extracted from the `productGroupSelection` field if it is not null.
     * Then, it appends product names obtained from the `products` field if it is not null.
     * The names are split by commas and then joined into a single string, which is returned.
     *
     * @return A comma-separated string of product and group names. If no names are available, returns an empty string.
     */

    String getProductAndGroupName() {
        List<String> namesList = []
        String data = Constants.Commons.BLANK_STRING

        if (this.productGroupSelection) {
            namesList.addAll(getGroupNameFieldFromJson(this.productGroupSelection).split(","))
        }
        if (this.products) {
            namesList.addAll(getNameFieldArrayFromJson(this.products).split(","))
        }

        if (namesList) {
            data = namesList.join(",")
        }

        return data
    }
    /**
     * Retrieves a concatenated string of event and group names.
     *
     * This method constructs a list of names based on the current event and group selections.
     * It first adds group names extracted from the `eventGroupSelection` field if it is not null.
     * It then appends event names obtained from the `events` field if it is not null. The event names
     * are retrieved using the `ViewHelper.getDictionaryValues` method with the `DictionaryTypeEnum.EVENT` type.
     *
     * The names are split by commas and then joined into a single comma-separated string, which is returned.
     * If no names are available, an empty string is returned.
     *
     * @return A comma-separated string of event and group names. If no names are available, returns an empty string.
     */
    String getEventAndGroupName() {
        List<String> namesList = []
        if (this.eventGroupSelection) {
            String groupValues = getGroupNameFieldFromJson(this.eventGroupSelection)
            if (groupValues) {
                namesList.addAll(groupValues.split(","))
            }
        }
        if (this.events) {
            String eventValues = ViewHelper.getDictionaryValues(this.events, DictionaryTypeEnum.EVENT)
            if (eventValues) {
                namesList.addAll(eventValues.split(","))
            }
        }
        return namesList.isEmpty() ? Constants.Commons.BLANK_STRING : namesList.join(",")
    }

    List<Integer> getProductIdList() {
        String prdIds = getIdFieldFromJson(this.products)
        List<Integer> prdList = []
        if (prdIds) {
            prdList = prdIds?.tokenize(',')?.collect { it as Integer }
        }
        return prdList
    }

    def getAlertsWithDataSourceName(String dataSourceName) {
        aggregateAlerts.findAll {
            it.alertConfiguration.selectedDatasource?.toLowerCase() == dataSourceName.toLowerCase()
        }
    }

    def getPvaAlerts() {
        getAlertsWithDataSourceName('PVA')
    }

    def getFaersAlerts() {
        getAlertsWithDataSourceName('FAERS')
    }

    def getDateClosed() {
        if (disposition.isClosed()) {
            lastUpdated
        } else
            null
    }

    def propertyMissing(String name) {
        if (name == "productSelection") {
            return this.products
        }
    }

    Map toStateReportExport(String timeZone = "UTC") {
        Integer noOfPecs = this.aggregateAlerts?.size() + this.evdasAlerts?.size()
        [
                signalId         : this.id,
                signalName       : this.name?.trim()?.replaceAll("\\s{2,}", " "),
                productName      : getProductAndGroupNameList(),
                priority         : this.priority.displayName,
                assignedTo       : this.assignedTo ? this.assignedTo?.fullName : (this.assignedToGroup ? this.assignedToGroup.name : "-"),
                actions          : this.actions.findAll{it.actionStatus != Constants.ActionStatus.DELETED && it.actionStatus != Constants.ActionStatus.CLOSED}?.size(),
                dueDate          : '-',
                monitoringStatus : '-',
                disposition      : this.disposition.displayName
        ]
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

    def getInstanceIdentifierForAuditLog() {
        return name
    }
    @Override
    String toString(){
        "$name"
    }

    def getEntityValueForDeletion(){
        String justification = getActionJustification()
        String owner = User.findByUsername(this.createdBy)?.fullName
        return "Signal Name-${name}, Owner-${owner}, Assigned To-${assignedTo ?: assignedToGroup}, Justification-${justification}"
    }

    def getActionJustification() {
        Sql sql = new Sql(getDataSource())
        String data = ""
        try {
            sql.eachRow(SignalQueryHelper.retrieve_justification_by_class_and_id("com.rxlogix.signal.ValidatedSignal",id) as String) { row ->
                data = (row.JUSTIFICATION as String)
            }
        }catch (Exception e){
            e.printStackTrace()
        }finally {
            sql?.close()
        }
        data
    }

    static def getDataSource() {
        if (!dataSource) {
            def app = Holders.getGrailsApplication()
            dataSource = app.getMainContext().getBean('dataSource')
        }
        dataSource
    }

    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        if (this.justificationForAudit != null && this.justificationForAudit != "") {
            newValues.put("justificationForAudit", this.justificationForAudit)
        }
        // custom audit properties added before CRUD operation
        for(Map.Entry customAuditEntry: this.customAuditProperties){
            if (customAuditEntry.getValue() != null && customAuditEntry.getValue() != "") {
                // here key will be displayed as field name in audit log with capitalized name
                // values will be displayed as new value in audit log details
                newValues.put(customAuditEntry.getKey(), customAuditEntry.getValue())
            }
            // in case old values need to be added and in case are saved in previous
            // audits, oldValues.put(fieldName of property, "N/A"), audit log automatically replaces
            // "N/A" string with previous state if audit existed in Audit log
        }
        this.customAuditProperties=[:]
        return [newValues: newValues, oldValues: oldValues]
    }
}
