package com.rxlogix.jobs

import grails.plugins.mail.oauth.token.OAuthToken
import grails.util.Holders

class MailOAuthTokenRefreshJob {

    static concurrent = false
    static group = "RxLogixPVS"

    def tokenStore
    def mailOAuthService

    static triggers = {
        if (Holders.config.grails.mail.oAuth.enabled) {
            simple name: 'MailOAuthTokenRefreshJobTrigger', startDelay: 60000, repeatInterval: Holders.config.getProperty('grails.mail.oAuth.token.refresh.frequency', Integer, 1000) * 1000
        }
    }

    def execute() {
        if (!Holders.config.grails.mail.oAuth.enabled) {
            return
        }
        OAuthToken token = tokenStore.getToken()
        if (token && ((token.expireAt.time - new Date().time) < (1000 * Holders.config.getProperty('grails.mail.oAuth.token.refresh.time.difference', Integer, 2000)))) {
            MailOAuthTokenRefreshJob.log.debug('Refreshing token via JOB')
            mailOAuthService.refreshAccessToken(token)
        }
    }
}