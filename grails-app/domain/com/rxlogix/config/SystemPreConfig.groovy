package com.rxlogix.config


import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class SystemPreConfig implements Serializable{

    boolean running = false
    boolean optional = false
    boolean enabled = false
    boolean warning = false
    boolean previousRunningStatus = true
    String name
    String displayName
    String reason
    String appType
    String dbType
    String entityType
    String entityKey
    String validationLevel
    int orderSeq
    String alertType
    String tableSpaceTime

    static constraints = {
        name nullable: false
        displayName nullable: false
        reason nullable: true
        appType nullable: true
        dbType nullable: true
        entityType nullable: true
        entityKey nullable: true
        validationLevel nullable: true
        alertType nullable: true
        tableSpaceTime nullable: true
    }
}
