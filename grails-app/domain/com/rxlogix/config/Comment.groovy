package com.rxlogix.config

import com.rxlogix.user.User

class Comment {
    static auditable = false
    String content
    User commentedBy
    Comment parent
    Date inputDate

    static constraints = {
        content nullable: true
        parent nullable: true
        commentedBy nullable: true
        inputDate nullable: true
    }

    static mapping = {
        table('COMMENTS')
    }

    static belongsTo = [Action]
}
