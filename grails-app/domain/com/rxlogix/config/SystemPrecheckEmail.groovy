package com.rxlogix.config


import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class SystemPrecheckEmail implements Serializable {

    String name
    String reason
    Date dateCreated
    boolean emailSent = false
    String dbType
    String appType

    static constraints = {
        name nullable: false
        reason nullable: true
        dbType nullable: true
        appType nullable: true
        dateCreated nullable: false
    }
}
