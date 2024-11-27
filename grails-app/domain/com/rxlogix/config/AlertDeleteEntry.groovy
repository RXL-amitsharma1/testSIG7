package com.rxlogix.config

class AlertDeleteEntry {

    Long exConfigId
    Boolean pvrCompleted = false
    Boolean spotfireCompleted = false
    Boolean dbCompleted = false
    Boolean isDeletionCompleted = false
    Boolean dataMigrationCompleted =false

    AlertDeletionStatus alertDeletionStatus = AlertDeletionStatus.CREATED

    Date dateCreated
    Date lastUpdated

    static belongsTo = [alertDeletionData:AlertDeletionData]

    static constraints = {
    }
    static mapping = {
        alertDeletionData(column: 'ALERT_DELETION_DATA_ID')
    }

    AlertDeleteEntry(Long exConfigId, AlertDeletionData alertDeletionData) {
        this.exConfigId = exConfigId
        this.dateCreated = new Date()
        this.lastUpdated = new Date()
        this.alertDeletionData = alertDeletionData
    }
    @Override
    String toString() {
        "${this.getClass().getSimpleName()} : ${this.id}"
    }
}
