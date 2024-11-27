package com.rxlogix.signal

import com.rxlogix.user.User

class SpotfireNotificationQuery {
    String fileName
    String configurationName
    String type
    String runType
    Long executedConfigurationId
    String signalParameters
    Boolean isEnabled = false
    String status

    Date created = new Date()
    static hasMany = [notificationRecipients: User]

    static constraints = {
        fileName nullable: false
        configurationName nullable: false
        type nullable: false
        executedConfigurationId nullable: false
        created nullable: false
        notificationRecipients nullable: false
        runType nullable: false
        signalParameters nullable: true, maxSize: 4000
        isEnabled nullable: true
        status nullable: true
    }
    static mapping = {
        notificationRecipients joinTable: [name: "spotfire_notification_pvuser", column: "USER_ID", key: "recipients_id"]
    }
}
