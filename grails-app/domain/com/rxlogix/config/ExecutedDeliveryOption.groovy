package com.rxlogix.config

import com.rxlogix.BaseDeliveryOption
import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class ExecutedDeliveryOption extends BaseDeliveryOption {
    static auditable = false

    static mapping = {
        table name: "EX_DELIVERY"

        sharedWith joinTable: [name: "EX_DELIVERIES_SHARED_WITHS", column: "SHARED_WITH_ID", key: "EX_DELIVERY_ID"], indexColumn: [name:"SHARED_WITH_IDX"]
        emailToUsers joinTable: [name: "EX_DELIVERIES_EMAIL_USERS", column: "EMAIL_USER", key: "EX_DELIVERY_ID"], indexColumn: [name:"EMAIL_USER_IDX"]
        attachmentFormats lazy: false, joinTable: [name: "EX_DELIVERIES_RPT_FORMATS", column: "RPT_FORMAT", key: "EX_DELIVERY_ID"], indexColumn: [name:"RPT_FORMAT_IDX"]
    }
}
