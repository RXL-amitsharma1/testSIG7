package com.rxlogix.signal

import com.rxlogix.Constants
import com.rxlogix.config.AlertDocument
import com.rxlogix.config.Disposition
import com.rxlogix.config.MedicalConcepts
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.SignalUtil

import static com.rxlogix.util.DateUtil.toDateString
import static com.rxlogix.util.DateUtil.toDateStringPattern

class AdHocAlert extends Alert implements AlertUtil, GroovyInterceptable {
    def grailsApplication
    def cacheService

    static TYPE = "ad-hoc"
    static auditable = ['ignore':['initialDisposition','initialDueDate','isDispChanged','lastUpdatedNote','noteModifiedBy', 'productDictionarySelection','publicAlert',
                                  'flagged','workflowGroup','activities','lastUpdated','alertId','actions','dispPerformedBy','modifiedBy','createdBy','reviewDate','dateCreated']]
    String countryOfIncidence
    String reportType
    String formulations
    String refType
    String productSelection
    String studySelection
    String productDictionarySelection
    String productGroupSelection
    String eventGroupSelection
    String indication
    String detectedBy
    User sharedWith
    User owner
    Boolean issuePreviouslyTracked
    Integer numberOfICSRs
    String initialDataSource
    String topic
    String reasonForDelay
    Group workflowGroup
    List<User> shareWithUser = []
    List<Group> shareWithGroup = []
    String undoJustification
    Map<String, Object> customAuditProperties

    static hasMany = [alertDocuments: AlertDocument, signalConcepts: MedicalConcepts, topicConcepts: MedicalConcepts, validatedSignals: ValidatedSignal,
                      topics        : Topic, comments: AlertComment, actionTaken: String, shareWithUser: User, shareWithGroup: Group]

    String description
    String name
    String alertRmpRemsRef
    String referenceNumber
    Boolean publicAlert = false

    Date aggReportStartDate
    Date aggReportEndDate
    Date lastDecisionDate


    Disposition haSignalStatus
    String commentSignalStatus
    Date haDateClosed
    Date lastUpdatedNote
    String noteModifiedBy
    Date initialDueDate
    Disposition initialDisposition
    String dispPerformedBy
    Boolean isDispChanged = false

    static mapping = {
        discriminator value: "ad-hoc"
        productGroupSelection sqlType: "varchar2(8000 CHAR)"
        eventGroupSelection sqlType: "varchar2(8000 CHAR)"
        signalConcepts joinTable: [name: "AD_HOC_SIGNAL_CONCEPTS", column: "MEDICAL_CONCEPTS_ID", key: "AD_HOC_ALERT_ID"]
        topicConcepts joinTable: [name: "AD_HOC_TOPIC_CONCEPTS", column: "MEDICAL_CONCEPTS_ID", key: "AD_HOC_ALERT_ID"]
        validatedSignals joinTable: [name: "VALIDATED_ADHOC_ALERTS", column: "VALIDATED_SIGNAL_ID", key: "ADHOC_ALERT_ID"]
        topics joinTable: [name: "TOPIC_ADHOC_ALERTS", column: "TOPIC_ID", key: "ADHOC_ALERT_ID"]
        comments joinTable: [name: "ADHOC_ALERT_COMMENTS", column: "COMMENT_ID", key: "ADHOC_ALERT_ID"]
        aggReportEndDate(column: 'AGG_END_DATE')
        aggReportStartDate(column: 'AGG_START_DATE')
        actionTaken joinTable: [name: "ADHOC_ALERT_ACTION_TAKEN", column: "ACTION_TAKEN", key: "ADHOC_ALERT_ID"]
        workflowGroup column: "WORKFLOW_GROUP"
        shareWithUser joinTable: [name:"SHARE_WITH_USER_ADHOC", column:"SHARE_WITH_USERID", key:"CONFIG_ID"]
        shareWithGroup joinTable: [name:"SHARE_WITH_GROUP_ADHOC", column:"SHARE_WITH_GROUPID", key:"CONFIG_ID"]
    }

    static constraints = {

        productSelection nullable: true, maxSize: 4000
        countryOfIncidence nullable: true
        reportType nullable: true
        refType nullable: true
        studySelection nullable: true, maxSize: 4000
        formulations nullable: true
        indication nullable: true
        sharedWith nullable: true
        detectedBy nullable: true
        issuePreviouslyTracked nullable: true
        numberOfICSRs nullable: true
        initialDataSource nullable: true
        description nullable: true, maxSize: 4000
        alertRmpRemsRef nullable: true
        referenceNumber nullable: true
        owner nullable: true
        topic nullable: true, maxSize: 4000
        name(validator: { val, obj ->
            //Name is unique to user
            if (!obj.id || obj.isDirty("name")) {
                long count = AdHocAlert.createCriteria().count {
                    ilike('name', "${val}")
                    eq('owner', obj.owner)
                    if (obj.id) {
                        ne('id', obj.id)
                    }
                }
                if (count) {
                    return "com.rxlogix.config.alert.name.unique.per.user"
                }
            }
        }, nullable: false, maxSize: 4000)
        eventSelection nullable: true
        comments nullable: true
        reasonForDelay nullable: true
        productDictionarySelection nullable: true
        aggReportStartDate nullable: true
        aggReportEndDate nullable: true
        lastDecisionDate nullable: true
        haSignalStatus nullable: true
        commentSignalStatus nullable: true, maxSize: 4000
        haDateClosed nullable: true
        actionTaken nullable: true
        workflowGroup nullable: true
        lastUpdatedNote nullable: true
        noteModifiedBy nullable: true
        productGroupSelection nullable: true
        eventGroupSelection nullable: true
        initialDueDate nullable:true
        initialDisposition nullable: true
        dispPerformedBy nullable: true
        isDispChanged nullable: true
    }

    static transients = ['productNameList','undoJustification','customAuditProperties']

    static overdueGroups() {
        overdueGroups(TYPE)
    }

    def isPublic() { publicAlert }

    def setIsPublic(boolean pub) { this.publicAlert = pub } //Added for PVS-56093

    //This is used for the list view.
    def briefs(isProductSafetyLead = true, def signals = null,  Boolean isUndoable=false) {
        def prdName = getNameFieldFromJson(this.productSelection)?:getGroupNameFieldFromJson(this.productGroupSelection)
        def productId = getIdFieldFromJson(this.productSelection)
        def evtName = getNameFieldFromJson(this.eventSelection)?:getGroupNameFieldFromJson(this.eventGroupSelection)
        def timezone = grailsApplication?.config?.pvsignal?.server?.timezone ?: 'America/Los_Angeles'

        def alertData = [
                id                       : this.id,
                name                     : this.name ?: "",
                version                  : this.alertVersion,
                priority                 : [value: this.priority?.value, iconClass: this.priority?.iconClass],
                assignedTo               : this.assignedTo ? this.assignedTo?.username : this.assignedToGroup?.name,
                detectedDate             : toDateStringPattern(this.detectedDate, 'dd-MMM-yyyy'),
                dueDate                  : this.dueDate,
                disposition              : this.disposition?.displayName,
                dispositionCloseStatus   : this.disposition?.closed,
                currentDispositionId     : this.dispositionId,
                productSelection         : prdName,
                productId                : productId,
                productSelectionJson     : this.productSelection,
                eventSelection           : evtName,
                flagged                  : this.flagged,
                //TODO rename the toDateString1 to regular name
                followupDate             : this.getAttr('masterFollowupDate_4'),
                formulation              : this.formulations,
                indication               : this.indication,
                reportType               : this.reportType,
                //TODO donot hard code yes or no here
                issueTracked             : this.issuePreviouslyTracked ? "Yes" : "No",
                numOfIcsrs               : this.numberOfICSRs,
                initDataSrc              : this.initialDataSource,
                dueIn                    : this.dueDate != null ? this.dueIn() as String:Constants.Commons.DASH_STRING,
                alertRmpRemsRef          : this.alertRmpRemsRef,
                topic                    : this.topic,
                isProductSafetyLead      : isProductSafetyLead,
                assignedToFullName       : this.assignedTo ? this.assignedTo?.fullName : this.assignedToGroup?.name,
                canAddToTopic            : canAddToTopic(),
                isValidationStateAchieved: this.disposition.validatedConfirmed,
                lastUpdated              : this.lastUpdated,
                notes                    : this.notes ? this.notes : "",
                assignedToValue          : this.getAssignedToMap(),
                dispPerformedBy          : this.dispPerformedBy,
                isUndoEnabled            : isUndoable?'true':'false',
                isDefaultState           : this.isDefaultState()
        ]
        alertData.signalsAndTopics = SignalUtil.joinSignalNames(this.validatedSignals as List)
        alertData
    }

    def isDefaultState() {
        if (!isDispChanged && getDispositionById(this.dispositionId) == getDefaultDisp() && this.dispPerformedBy == null) {
            return 'true'
        }
        return 'false'
    }

    Disposition getDispositionById(Long dispositionId) {
        cacheService.getDispositionByValue(dispositionId)
    }

    Disposition getDefaultDisp() {
        cacheService.getDefaultDisp(Constants.AlertConfigType.AD_HOC_ALERT)
    }

    Boolean canAddToTopic() {
        return (this.disposition.validatedConfirmed) ? false : true
    }

    def details(isProductSafetyLead) {
        def prdName = this.productSelection==null?getGroupNameFieldFromJson(this.productGroupSelection):(getProdNameList()?.join(", ")?:Constants.Commons.BLANK_STRING)
        def productId = getIdFieldFromJson(this.productSelection)
        def evtName = this.eventSelection? getNameFieldFromJson(this.eventSelection):(getGroupNameFieldFromJson(this.eventGroupSelection)?:Constants.Commons.BLANK_STRING)
        def timezone = grailsApplication?.config?.pvsignal?.server?.timezone ?: 'America/Los_Angeles'

        [
                id                 : this.id,
                name               : this.name ?: "",
                version            : this.alertVersion,
                priority           : [value: this.priority?.value, iconClass: this.priority?.iconClass],
                description        : this.description,
                assignedTo         : this.assignedTo ? this.assignedTo?.fullName : this.assignedToGroup?.name,
                detectedDate       : toDateString(this.detectedDate, timezone),
                detectedBy         : this.detectedBy,
                dueDate            : this.dueDate,
                notes              : this.notes,
                disposition        : this.disposition?.displayName,
                productSelection   : prdName,
                productId          : productId,
                eventSelection     : evtName,
                flagged            : this.flagged,
                //TODO rename the toDateString1 to regular name
                followupDate       : this.getAttr('masterFollowupDate_4'),
                formulation        : this.formulations,
                indication         : this.indication,
                reportType         : this.reportType,
                //TODO donot hard code yes or no here
                issueTracked       : this.issuePreviouslyTracked ? "Yes" : "No",
                numOfIcsrs         : this.numberOfICSRs,
                initDataSrc        : this.initialDataSource,
                dueIn              : this.dueIn(),
                actionCount        : this.actions?.size(),
                alertRmpRemsRef    : this.alertRmpRemsRef,
                topic              : this.topic,
                isProductSafetyLead: isProductSafetyLead,
                assignedToValue    : this.getAssignedToMap()
        ]
    }

    def buildProductNameList() {
        getNameFieldFromJson(productSelection).split(',')
    }

    def propertyMissing(propName) {
        if (propName == 'productNames')
            return buildProductNameList()

        super.propertyMissing(propName)
    }

    def getProductNameList() {
        String prdName = getNameFieldFromJson(this.productSelection)

        if (prdName) {
            prdName.toLowerCase().tokenize(',')
        } else {
            []
        }
    }

    def getProdNameList() {
        String prdName = getNameFieldFromJson(this.productSelection)

        if (prdName) {
            prdName.tokenize(',')
        } else {
            []
        }
    }

    def getPrdNameList() {
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

    Map getAssignedToMap() {
        this.assignedTo ? this.assignedTo.getAssignedToMap() : this.assignedToGroup.getAssignedToMap()
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
        return name
    }
    Map appendAuditLogCustomProperties(Map newValues, Map oldValues) {
        if (this.undoJustification != null && this.undoJustification != "") {
            newValues.put("undoJustification", this.undoJustification)
        }
        // custom audit properties added before CRUD operation
        for(Map.Entry customAuditEntry: this.customAuditProperties){
            if (customAuditEntry.getValue() != null && customAuditEntry.getValue() != "") {
                newValues.put(customAuditEntry.getKey(), customAuditEntry.getValue())
                oldValues.put(customAuditEntry.getKey(), "")
            }
        }
        this.customAuditProperties=[:]
        return [newValues: newValues, oldValues: oldValues]
    }
}
