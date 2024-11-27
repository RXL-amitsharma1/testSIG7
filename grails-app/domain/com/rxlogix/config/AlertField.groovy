package com.rxlogix.config


import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class AlertField implements Serializable {
    boolean optional = false
    boolean enabled = false
    boolean isAutocomplete = false
    boolean isFilter = true
    boolean isVisible = true
    boolean isSmqVisible = false
    boolean isAdvancedFilterField = true
    boolean isBusinessRuleField = true
    boolean isNewColumn = false
    boolean isHyperLink = true
    boolean isAdhocVisible = false
    String name
    String display
    String oldDisplay
    String dataType
    String keyId
    String type
    String dbType
    String dbKey
    String alertType
    int containerView
    int seq
    int listOrder
    String secondaryName
    String cssClass
    String flagEnabled

}