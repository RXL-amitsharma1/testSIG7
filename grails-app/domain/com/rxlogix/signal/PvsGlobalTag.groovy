package com.rxlogix.signal

import com.rxlogix.user.User

class PvsGlobalTag implements Cloneable{
    Long tagId
    Long subTagId
    String tagText
    String subTagText
    Long globalId
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
        autoTagged nullable: true
        isRetained nullable: true
        execConfigId nullable: true
    }

    static mapping = {
        globalId index: 'IX_pvs_global_tag_global_id'
        domain index: 'IX_pvs_global_tag_global_id'
    }
}
