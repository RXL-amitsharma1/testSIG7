package com.rxlogix.config
import com.rxlogix.BaseDeliveryOption
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class DeliveryOption extends BaseDeliveryOption {
    static auditable = false

    static mapping = {
        table name: "DELIVERY"
        sharedWith joinTable: [name: "DELIVERIES_SHARED_WITHS", column: "SHARED_WITH_ID", key: "DELIVERY_ID"], indexColumn: [name:"SHARED_WITH_IDX"]
        emailToUsers joinTable: [name: "DELIVERIES_EMAIL_USERS", column: "EMAIL_USER", key: "DELIVERY_ID"], indexColumn: [name: "EMAIL_USER_IDX"]
        attachmentFormats joinTable: [name: "DELIVERIES_RPT_FORMATS", column: "RPT_FORMAT", key: "DELIVERY_ID"], indexColumn: [name:"RPT_FORMAT_IDX"]
    }

}
