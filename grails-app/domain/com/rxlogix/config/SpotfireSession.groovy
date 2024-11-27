package com.rxlogix.config

class SpotfireSession {
    Long id
    String token
    boolean deleted = false
    Date timestamp
    String username
    String email
    String fullName

    static constraints = {
        token nullable: true, unique: true
        timestamp nullable: false
        username nullable: true
        email nullable: true
        fullName nullable: true
    }

    static mapping = {
        table "spotfire_session"
        token nullable: true
        deleted defaultValue: false
    }

    String toJson() {
        """{
    \"token\": \"$token\",
    \"username\": \"$username\",
    \"fullName\": \"$fullName\",
    \"email\": \"$email\",
    \"timestamp\": \"${timestamp.toString()}\"
}"""
    }
}
