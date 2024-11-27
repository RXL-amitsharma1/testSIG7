package com.rxlogix.signal

class ValidatedAlertCommentMapping implements Serializable {

    ValidatedSignal validatedSignal
    AlertComment comment

    static mapping = {
        table("VALIDATED_ALERT_COMMENTS")
        comment column: "COMMENT_ID"
        validatedSignal column: "VALIDATED_SIGNAL_ID"
        version false
        id composite:["validatedSignal","comment"]
    }

    static constraints = {
    }
}
