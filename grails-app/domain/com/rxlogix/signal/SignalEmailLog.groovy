package com.rxlogix.signal

import com.rxlogix.util.DbUtil

class SignalEmailLog {

    static auditable = false

    String assignedTo
    String subject
    String body
    static attachmentable = true
    static mapping = {
        autoTimestamp false
    }

    static constraints = {
        body sqlType: DbUtil.longStringType
        assignedTo maxSize: 8000
    }
}