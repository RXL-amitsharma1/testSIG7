package com.rxlogix.config

class PriorityDispositionConfig implements Serializable{
    static auditable = false
    Disposition disposition
    Integer reviewPeriod
    Priority priority
    Integer dispositionOrder

    static belongsTo = [priority:Priority]

    static constraints = {
    }

    String getInstanceIdentifierForAuditLog() {
        "$priority.displayName: ($disposition.displayName)"
    }
    @Override
    String toString(){
        return disposition.displayName+":"+reviewPeriod
    }

}
