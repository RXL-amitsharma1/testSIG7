package com.rxlogix.config

import com.rxlogix.util.DbUtil

class EmailLog implements Serializable {
    Long id
    String subject
    String sentTo
    Date sentOn
    String message

    static mapping = {
        table('EMAIL_LOG')
    }

    static constraints = {
        subject nullable: true, maxSize: 4000
        sentTo nullable: true, maxSize: 4000
        message nullable: true, sqlType: DbUtil.longStringType
    }
}
