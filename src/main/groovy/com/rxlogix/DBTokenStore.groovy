package com.rxlogix

import com.rxlogix.config.MailOAuthToken
import grails.plugins.mail.oauth.token.OAuthToken
import grails.plugins.mail.oauth.token.TokenStore

class DBTokenStore implements TokenStore {

    @Override
    void saveToken(OAuthToken token) {

        MailOAuthToken.withNewSession {
            MailOAuthToken existingToken = MailOAuthToken.first()?.lock()
            if (!existingToken) {
                existingToken = new MailOAuthToken()
            }
            existingToken.with {
                accessToken = token.accessToken
                refreshToken = token.refreshToken
                expiresIn = token.expiresIn
                expireAt = token.expireAt
            }
            existingToken.save(flush: true, failOnError: true)
        }

    }

    @Override
    OAuthToken getToken() {
        MailOAuthToken existingToken
        MailOAuthToken.withNewSession {
            existingToken = MailOAuthToken.first()
        }
        if (!existingToken) {
            return null
        }
        OAuthToken oAuthToken = new OAuthToken()
        oAuthToken.with {
            accessToken = existingToken.accessToken
            refreshToken = existingToken.refreshToken
            expiresIn = existingToken.expiresIn
            expireAt = existingToken.expireAt
        }
        return oAuthToken
    }

    @Override
    void revokeToken() {

        MailOAuthToken.withNewSession {
            MailOAuthToken existingToken = MailOAuthToken.first()
            if (existingToken) {
                existingToken.delete(flush: true)
            }
        }

    }
}