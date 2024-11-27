package com.rxlogix.signal

import java.sql.Timestamp

class ProductGroupStatus implements Serializable {
    Long id
    Long version
    String uniqueIdentifier
    Long count
    Long validRecordCount
    Long invalidRecordCount
    Date uploadedAt
    String addedBy
    Boolean isApiProcessed

    String pvrError

    static transients = ['pvrError']
    List<ProductGroupData> productGroups = []

    static mapping = {
        table name: "PROD_GROUPS_STATUS"
        uploadedAt column: "UPLOADED_DATE"
        id generator: 'sequence', params: [sequence: 'PROD_GROUPS_STATUS_SEQ']
        productGroups joinTable: [name: "PROD_GROUPS_DATA", key: "PRODUCT_GROUP_STATUS_ID",  column: "ID"]
    }
    static constraints = {
        uniqueIdentifier nullable: true
        count nullable: true
        validRecordCount nullable: true
        invalidRecordCount nullable: true
        uploadedAt nullable: true
        addedBy nullable: true
        isApiProcessed nullable: true
        pvrError nullable:true
    }

    ProductGroupStatus() {
    }

    ProductGroupStatus(String uniqueIdentifier, Long count, Long validRecordCount, Long invalidRecordCount, Date uploadedAt, String addedBy, Boolean isApiProcessed, List<ProductGroupData> productGroups) {
        this.uniqueIdentifier = uniqueIdentifier
        this.count = count
        this.validRecordCount = validRecordCount
        this.invalidRecordCount = invalidRecordCount
        this.uploadedAt = uploadedAt
        this.addedBy = addedBy
        this.isApiProcessed = isApiProcessed
        this.productGroups = productGroups
    }

    ProductGroupStatus(BigDecimal id, BigDecimal version, String uniqueIdentifier, BigDecimal count, BigDecimal validRecordCount,
                       BigDecimal invalidRecordCount, Timestamp uploadedAt, String addedBy, BigDecimal isApiProcessed) {
        this.id = id
        this.version = version
        this.uniqueIdentifier = uniqueIdentifier
        this.count = count
        this.validRecordCount = validRecordCount
        this.invalidRecordCount = invalidRecordCount
        this.uploadedAt = uploadedAt
        this.addedBy = addedBy
        this.isApiProcessed = isApiProcessed
    }

}
