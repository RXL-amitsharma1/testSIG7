package com.rxlogix.dto

class AuditTrailChildDTO {

    String propertyName
    String oldValue
    String newValue
    Map toMap(){
        ["propertyName":this.propertyName,"oldValue":this.oldValue,"newValue":this.newValue]
    }
}
