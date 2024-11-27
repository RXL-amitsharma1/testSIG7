package com.rxlogix.dto

class AlertTagDTO {
    Long tagId
    Long subTagId
    String tagText
    String subTagText
    String alertId
    String globalId
    String domain
    String createdBy
    String createdAt
    String modifiedAt
    String modifiedBy
    String privateUser
    Long martId
    Boolean isMasterCategory
    Integer priority
    Boolean alertLevel
    String dmlType
    Boolean isActivity = true
    Boolean autoTagged;
    Boolean retained;
    Boolean isEdit = false;
    Integer execConfigId;
    Integer prodHirearchyId;
    Integer eventHirearchyId;
}
