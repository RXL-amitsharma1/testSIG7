package com.rxlogix.config

class AlertDeletionData {

    Long configId
    String exConfigId
    String alertType
    String justification

    Boolean isMaster = false
    DeletionStatus deletionStatus = DeletionStatus.READY_TO_DELETE
    Boolean deletionCompleted = false

    Date dateCreated
    Date lastUpdated

    static hasMany = [alertDeleteEntries: AlertDeleteEntry]

    static constraints = {
        alertType nullable: true
    }

    static mapping = {
        configId column: '"config_Id"'
        exConfigId column: '"ex_Config_Id"'
        alertType column: '"alert_Type"'
        deletionStatus column: '"deletion_Status"'
    }

    AlertDeletionData(String exConfigId, Long configId, String alertType, String justification, Boolean isMaster = false) {
        this.exConfigId = exConfigId
        this.configId = configId
        this.alertType = alertType
        this.justification = justification
        this.isMaster = isMaster
        this.dateCreated = new Date()
        this.lastUpdated = new Date()
    }

    static AlertDeletionData getNextEntryToDelete(List currentlyRunningIds) {
        List<AlertDeletionData> alertList = AlertDeletionData.createCriteria().list {
            and {
                eq('deletionStatus', DeletionStatus.READY_TO_DELETE)
                if (currentlyRunningIds) {
                    not {
                        'in'('configId', currentlyRunningIds)
                    }
                }
            }
            order('dateCreated', 'desc')
            maxResults(1)
        } as List<AlertDeletionData>
        if (alertList) {
            return alertList[0]
        }
        return null
    }
    @Override
    String toString() {
        "${this.getClass().getSimpleName()} : ${this.id}"
    }

}
