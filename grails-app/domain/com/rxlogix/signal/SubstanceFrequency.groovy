package com.rxlogix.signal

import grails.plugins.orm.auditable.AuditEntityIdentifier

import static com.rxlogix.util.DateUtil.toDateString



class SubstanceFrequency {
    static auditable = true

    @AuditEntityIdentifier
    String name
    Date startDate
    Date endDate
    String uploadFrequency
    String miningFrequency
    String alertType
    String frequencyName

    static constraints = {
        frequencyName maxSize: 255 //this is added because in DB table it is already 255 char
        name maxSize: 255         // this is added because in DB table it is already 255 char
    }

    def toDto() {
        [
            id : this.id,
            name : this.name,
            startDate : toDateString(this.startDate),
            endDate : toDateString(this.endDate),
            uploadFrequency : this.uploadFrequency,
            miningFrequency : this.miningFrequency,
            alertType : this.alertType,
            frequencyName : this.frequencyName
        ]
    }

    def getEntityValueForDeletion(){
        return "Frequency Label-${frequencyName}"
    }
}
