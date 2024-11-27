package com.rxlogix.signal

class ProductGroupData implements Serializable {
    Long id
    Long productGroupStatusId
    Long version
    String uniqueIdentifier
    String groupName
    String groupOldName
    Integer type
    String description
    String copyGroups
    String sharedWith
    String owner
    Integer tenantId
    String includeSources
    String data
    String validationError
    String status
    static mapping = {
        table name: "PROD_GROUPS_DATA"
        id generator: 'sequence', params: [sequence: 'PROD_GROUPS_DATA_SEQ']
    }

    static constraints = {
        productGroupStatusId nullable: true
        version nullable: false
        uniqueIdentifier nullable: true
        description nullable: true
        groupName nullable: true
        groupOldName nullable: true
        type nullable: true
        copyGroups nullable: true
        sharedWith nullable: true
        owner nullable: true
        includeSources nullable: true
        tenantId nullable: true
        data nullable: true, type: 'text', sqlType: 'clob'
        validationError nullable: true
        status nullable: true
    }

}
