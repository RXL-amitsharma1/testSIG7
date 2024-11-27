package com.rxlogix.signal

import com.rxlogix.Constants
import com.rxlogix.config.*
import com.rxlogix.user.Group
import com.rxlogix.user.User
import com.rxlogix.util.AlertUtil
import com.rxlogix.util.DbUtil
import groovy.transform.ToString

@ToString(includes=['name'])
class Topic implements AlertUtil, GroovyInterceptable {

    static auditable = true

    static attachmentable = true

    String name
    String products
    String initialDataSource
    SignalStrategy strategy

    PVSState workflowState
    Disposition disposition
    User assignedTo
    Priority priority

    Date startDate
    Date endDate

    //Common db table fields
    String createdBy
    Date dateCreated
    Date lastUpdated
    String modifiedBy

    String assignmentType="USER"
    String productDictionarySelection

    static mapping= {
        table name: "TOPIC"
        singleCaseAlerts joinTable: [name: "TOPIC_SINGLE_ALERTS", column: "SINGLE_ALERT_ID", key: "TOPIC_ID"]
        adhocAlerts joinTable: [name: "TOPIC_ADHOC_ALERTS", column: "ADHOC_ALERT_ID", key: "TOPIC_ID"]
        activities joinTable: [name: "TOPIC_ACTIVITIES", column: "ACTIVITY_ID", key: "TOPIC_ID"]
        aggregateAlerts joinTable: [name: "TOPIC_AGG_ALERTS", column: "AGG_ALERT_ID", key: "TOPIC_ID"]
        comments joinTable: [name: "TOPIC_COMMENTS", column: "COMMENT_ID", key: "TOPIC_ID"]
        sharedGroups joinTable: [name: "TOPIC_GROUP", column: "GROUP_ID", key: "TOPIC_ID"]
        products column: "PRODUCTS", sqlType: DbUtil.longStringType
    }

    static hasMany = [singleCaseAlerts: SingleCaseAlert, aggregateAlerts: AggregateCaseAlert, adhocAlerts: AdHocAlert,
                      activities      : Activity, actions: Action, comments: AlertComment, meetings: Meeting,sharedGroups: Group]

    static belongsTo = [AggregateCaseAlert,SingleCaseAlert,AdHocAlert]

    static constraints = {
        productDictionarySelection nullable: true
        products nullable: false, blank: false
        initialDataSource nullable: true
        singleCaseAlerts nullable: true
        aggregateAlerts nullable: true
        adhocAlerts nullable: true
        activities nullable: true
        strategy nullable: true
        comments nullable: true
        actions nullable: true
        name nullable: false
    }

    def toDto() {
        [
                topicId          : this.id,
                topicName        : this.name,
                productName      : this.getProductNameForList(),
                noOfCases        : this.singleCaseAlerts?.size(),
                noOfPec          : this.aggregateAlerts?.size(),
                priority         : this.priority.displayName,
                assignedTo       : this.assignedTo?.fullName,
                actions          : this.actions?.size(),
                strategy         : this.strategy ? this.strategy.name : '-',
                initialDataSource: this.initialDataSource,
                disposition      : this.disposition.displayName
        ]
    }

    def getProductNameForList() {
        String prdName = getNameFieldFromJson(this.products)
        if (prdName) {
            prdName.tokenize(',')
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

    def propertyMissing(String name) {
        if (name == "productSelection") {
            return this.products
        }
    }
    
}
