package com.rxlogix.signal

class PvsAlertTag implements Cloneable{
    Long tagId
    Long subTagId
    String tagText
    String subTagText
    Long alertId
    String domain
    String createdBy
    Date createdAt
    Date modifiedAt
    String modifiedBy
    String privateUser
    Long martId
    Boolean isMasterCategory
    Integer priority
    Boolean autoTagged
    Boolean isRetained
    Long execConfigId

    static constraints = {
        subTagId nullable: true
        subTagText nullable: true
        priority nullable: true
        martId nullable: true
        privateUser nullable: true
        isMasterCategory nullable: true
        createdBy nullable: true
        modifiedBy nullable: true
        modifiedAt nullable: true
        createdAt nullable: true
        autoTagged nullable: true
        isRetained nullable: true
        execConfigId nullable: true
    }

    static mapping = {

    }
}
