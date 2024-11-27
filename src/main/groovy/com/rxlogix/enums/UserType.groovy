package com.rxlogix.enums

public enum UserType {
    LDAP("LDAP User"),
    NON_LDAP("Non-LDAP User")

    private final String name

    UserType(String name) {
        this.name = name
    }
}
