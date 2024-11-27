package com.rxlogix.config


import grails.gorm.dirty.checking.DirtyCheck

@DirtyCheck
class SystemPrecheckInfo implements Serializable{

    boolean enabled = false
    String name
    String appType


    static constraints = {
        name nullable: false
        appType nullable: true
    }
}
