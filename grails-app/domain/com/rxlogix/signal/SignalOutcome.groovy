package com.rxlogix.signal

class SignalOutcome {

    static auditable = false

    String name
    boolean isDisabled = false
    boolean isDeleted = false
    Long dispositionId
    static constraints = {
        dispositionId nullable: true
    }
    static mapping = {
        table("SIGNAL_OUTCOME")
        sort "name"
        isDeleted column: "IS_DELETED"
        isDisabled column: "IS_DISABLED"
        disposition column: "DISPOSITION_ID"
    }

    @Override
    String toString(){
        "$name"
    }

    def getInstanceIdentifierForAuditLog() {
        return name;
    }

}
