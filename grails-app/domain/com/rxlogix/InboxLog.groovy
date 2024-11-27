package com.rxlogix

import com.rxlogix.config.NotificationLevel

import static com.rxlogix.util.DateUtil.toDateString

//import static com.rxlogix.util.DateUtil.toDateTimeString

class InboxLog implements Serializable {

    String type
    String subject
    String content
    Date createdOn
    Boolean isRead = false
    Boolean isDeleted = false
    Boolean isNotification = true
    Long inboxUserId
    Long executedConfigId
    String detailUrl

    NotificationLevel level
    String message
    String messageArgs
    Long notificationUserId

    static constraints = {
        message(nullable:true, maxSize: 1500)
        subject maxSize: 4000
        messageArgs(nullable:true, maxSize: 1500)
        content nullable: true, maxSize: 32000
        detailUrl nullable:true, maxSixe: 1000
        executedConfigId nullable:true
        level nullable:true
        notificationUserId nullable:true
    }

    static mapping = {
        level column: "LVL"
    }

    Map toDTO(String timeZone = "UTC") {
        [
                "notificationUserId": this.notificationUserId,
                "level":this.level,
                "message": this.message,
                "messageArgs" :this.messageArgs,
                "id"          : this.id,
                "type"        : this.type.replace("Quantitative" , "Aggregate Review").replace("Qualitative" , "Individual Case Review"),
                "subject"     : this.subject,
                "content"     : this.content,
                "createdOn"   : this.createdOn,
                "isRead"      : this.isRead,
                "isDeleted"   : this.isDeleted,
                "execConfigId": this.executedConfigId,
                "detailUrl"   : this.detailUrl
        ]
    }
}
