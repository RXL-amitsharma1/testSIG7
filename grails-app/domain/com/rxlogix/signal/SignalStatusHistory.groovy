package com.rxlogix.signal

import grails.plugins.orm.auditable.AuditEntityIdentifier

class SignalStatusHistory {
    static transients = ['signalId','skipAudit','isSystemUser']
    static auditable = ['ignore':['dispositionUpdated','signalId','currentDispositionId','isAutoPopulate','dueDate','updateTime']]

    Long signalId
    Boolean isSystemUser
    String signalStatus
    String fromSignalStatus
    Boolean skipAudit = false

    String statusComment
    String performedBy
    Date dateCreated
    boolean dispositionUpdated = false
    Boolean isAutoPopulate = false
    Long currentDispositionId
    Date dueDate
    Long updateTime = 0L
    static mapping = {
        table("SIGNAL_STATUS_HISTORY")
        autoTimestamp false
    }

    static constraints = {
        statusComment nullable: true, maxSize: 8000
        signalStatus nullable: true
        fromSignalStatus nullable: true
        isAutoPopulate nullable: true
        currentDispositionId nullable: true
        dueDate nullable: true
        signalId nullable:true
        updateTime nullable: true
    }

    @Override
    String toString() {
        "${this.getClass().getSimpleName()} : ${this.id}"
    }

    def getInstanceIdentifierForAuditLog(){
        if(signalId!=null){
            return ValidatedSignal.get(signalId as Long).getInstanceIdentifierForAuditLog() + ": " + this.signalStatus
        }else{
            return signalStatus
        }
    }

}
