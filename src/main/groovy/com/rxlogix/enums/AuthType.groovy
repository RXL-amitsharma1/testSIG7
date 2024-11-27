package com.rxlogix.enums

enum AuthType {
    Database("Database"),
    SAML('SAML'),
    LDAP('LDAP')

    private String value

    AuthType(String value) {
        this.value = value
    }

    static boolean isExternalUser(String authType) {
        return authType == Database.value
    }

    @Override
    String toString() { value }
}
