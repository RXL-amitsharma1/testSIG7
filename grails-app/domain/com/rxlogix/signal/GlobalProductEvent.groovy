package com.rxlogix.signal

class GlobalProductEvent {
    String productEventComb
    Long productKeyId
    Long eventKeyId

    static hasMany = [aggregateCaseAlert : AggregateCaseAlert , pvsGlobalTag : PvsGlobalTag]

    static constraints = {
        productKeyId nullable: true
        eventKeyId nullable: true
    }

    static mapping = {
        id column: '"globalProductEventId"'
        pvsGlobalTag joinTable: [name: "AGG_GLOBAL_TAGS", column: "PVS_GLOBAL_TAG_ID", key: "GLOBAL_PRODUCT_EVENT_ID"]
    }
}
