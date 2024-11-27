package com.rxlogix.config

class Notification {

    String message
    String messageArgs
    long notificationUserId
    Date dateCreated
    NotificationLevel level
    long executedConfigId
    String detailUrl

    static constraints = {
        message(blank:false, maxSize: 255)
        messageArgs(nullable:true, maxSize: 255)
        detailUrl nullable:true
    }

    static mapping = {
        table name: "NOTIFICATION"
        executedConfigId column: "EC_ID"
        message column: "MESSAGE"
        messageArgs column: "MSG_ARGS"
        notificationUserId column: "NOTIFICATION_USER_ID"
        dateCreated column: "DATE_CREATED"
        level column: "LVL"
    }
}

