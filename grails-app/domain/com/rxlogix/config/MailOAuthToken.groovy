package com.rxlogix.config

class MailOAuthToken {

    String name = 'mail token'
    String accessToken
    String refreshToken
    Integer expiresIn
    Date expireAt
    Date dateCreated
    Date lastUpdated

    static constraints = {
        name unique: true
    }

    static mapping = {
        table name: "MAIL_OAUTH_TOKEN"
        accessToken sqlType: 'varchar2(4000)'
        refreshToken sqlType: 'varchar2(4000)'
    }
}