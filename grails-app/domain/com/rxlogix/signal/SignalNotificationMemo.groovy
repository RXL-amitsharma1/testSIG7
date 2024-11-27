package com.rxlogix.signal

import com.rxlogix.config.Disposition
import com.rxlogix.user.Group
import com.rxlogix.user.User
import grails.plugins.orm.auditable.AuditEntityIdentifier

class SignalNotificationMemo {
    def signalMemoReportService
    def cacheService

    @AuditEntityIdentifier
    String configName
    String signalSource
    String triggerVariable
    String triggerValue
    String emailSubject
    String emailBody
    String emailAddress
    List<User> mailUsers = []
    List<Group> mailGroups = []
    List<Disposition> dispositions = []

    String updatedBy

    Date dateCreated
    static auditable = true

    static hasMany = [mailUsers: User, mailGroups: Group, dispositions: Disposition]

    static mapping = {
        table("SIGNAL_NOTIFICATION_MEMO")
        mailUsers joinTable: [name: "MAIL_USERS_MEMO", column: "USER_ID", key: "SIGNAL_NOTIFICATION_MEMO_ID"]
        mailGroups joinTable: [name: "MAIL_GROUPS_MEMO", column: "GROUP_ID", key: "SIGNAL_NOTIFICATION_MEMO_ID"]
        dispositions joinTable: [name: "DISPOSITION_IDS_MEMO", column: "DISPOSITION_IDS", key: "SIGNAL_NOTIFICATION_MEMO_ID"]
    }

    static constraints = {
        signalSource nullable: true, maxSize: 4000
        triggerVariable nullable: true
        triggerValue nullable: true, maxSize: 4000
        emailAddress nullable: true, maxSize: 4000
        emailSubject nullable: true
        emailBody nullable: true, maxSize: 8000
    }

    List<User> getMailUserList() {
        List<User> users = []
        if (this.mailUsers) {
            users.addAll(this.mailUsers)
        }
        return users.grep()
    }

    List<Group> getMailGroupList() {
        List<Group> userGroups = []
        if (this.mailGroups) {
            userGroups.addAll(this.mailGroups)
        }
        return userGroups.grep()
    }

    def getEntityValueForDeletion(){
        return "Config Name- ${configName?:""}, Signal Source- ${signalSource?:""}, Trigger Variable- ${triggerVariable?:""}, Trigger Value- ${triggerValue?:""}, Email(PVS_USER)- ${mailUsers ? mailUsers.join(",") : ""}, Email(PVS_GROUP)-${mailGroups ? mailGroups.join(",") : ""}, Email (External User Address)-${emailAddress ?: ""}, Email Subject-${emailSubject?:""}, Email Body-${emailBody?:""} "
    }
}