package com.rxlogix.signal

import com.rxlogix.Constants
import com.rxlogix.util.DbUtil
import com.rxlogix.util.AlertUtil
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class EmergingIssue implements Serializable{

    String eventName
    String eventGroupSelection
    String productSelection
    String productGroupSelection
    String dataSourceDict
    String products
    String events
    boolean ime = false
    boolean dme = false
    boolean emergingIssue = false
    boolean specialMonitoring = false

    //Common db table fields
    String createdBy
    Date dateCreated
    Date lastUpdated
    String modifiedBy
    Boolean isMultiIngredient =false

    static constraints = {
        lastUpdated nullable: true
        modifiedBy nullable: true
        eventName(validator: {val, obj ->
            if (!val && !obj.eventGroupSelection) {
                return "com.rxlogix.EmergingIssue.eventName.nullable"
            }
        },  nullable: true)
        eventGroupSelection (nullable: true)
        productGroupSelection (nullable: true)
        products (nullable: true)
        events (nullable: true)
        productSelection(nullable: true)
        dataSourceDict(nullable: true)
        isMultiIngredient(nullable: true)
    }

    static mapping = {
        eventName column: "EVENT_SELECTION",sqlType: DbUtil.longStringType
        productSelection column: "PRODUCT_SELECTION", sqlType: DbUtil.longStringType
        eventGroupSelection column: "EVENT_GROUP_SELECTION",sqlType: DbUtil.longStringType
        productGroupSelection column: "PRODUCT_GROUP_SELECTION",sqlType: DbUtil.longStringType
        products column: "PRODUCTS",sqlType: DbUtil.longStringType
        events column: "EVENTS",sqlType: DbUtil.longStringType
    }

    Map toDto(){
        [
                eventName: AlertUtil.getNameFieldFromJson(this.eventName)?.tokenize(','),
                ime      : this.ime ? 'ime' : Constants.Commons.BLANK_STRING,
                dme      : this.dme ? 'dme' : Constants.Commons.BLANK_STRING,
                ei       : this.emergingIssue ? 'ei' : Constants.Commons.BLANK_STRING,
                sm       : this.specialMonitoring ? 'sm' : Constants.Commons.BLANK_STRING
        ]
    }

    def getEntityValueForDeletion(){
        return "Product-${productSelection}, Event-${eventName}, IME-${ime?'Yes':'No'}, DME-${dme?'Yes':'No'}, Emerging Issue-${emergingIssue?'Yes':'No'}, Special Monitoring-${specialMonitoring?'Yes':'No'}"
    }

}
