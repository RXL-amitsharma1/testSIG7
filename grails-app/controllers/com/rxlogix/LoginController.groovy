/* Copyright 2013 SpringSource.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rxlogix

import grails.converters.JSON
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Holders
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.web.authentication.session.SessionAuthenticationException
import org.springframework.security.web.csrf.InvalidCsrfTokenException
import org.springframework.security.web.csrf.MissingCsrfTokenException

import javax.servlet.http.HttpServletResponse

import org.springframework.security.access.annotation.Secured
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.springframework.security.web.WebAttributes
import com.rxlogix.user.sso.exception.SSOConfigurationException
import com.rxlogix.user.sso.exception.SSOUserDisabledException
import com.rxlogix.user.sso.exception.SSOUserLockedException
import com.rxlogix.user.sso.exception.SSOUserNotConfiguredException

import java.text.SimpleDateFormat


@Secured('permitAll')
class LoginController {

    /**
     * Dependency injection for the authenticationTrustResolver.
     */
    def authenticationTrustResolver

    /**
     * Dependency injection for the springSecurityService.
     */
    def springSecurityService

    /**
     * Default action; redirects to 'defaultTargetUrl' if logged in, /login/auth otherwise.
     */
    def index() {
        if (springSecurityService.isLoggedIn()) {
            redirect uri: SpringSecurityUtils.securityConfig.successHandler.defaultTargetUrl
        } else {
            redirect action: 'auth', params: params
        }
    }
    def securityAndPrivacyPolicy() {
        render(view: 'policy')
    }
    /**
     * Show the login page.
     */
    def auth() {

        def config = SpringSecurityUtils.securityConfig

        if (springSecurityService.isLoggedIn()) {
            redirect uri: config.successHandler.defaultTargetUrl
            return
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        String message = (message(code: "app.copyright.message", args: [ sdf.format(new Date())])).replaceAll('Â','')
        String view = 'auth'
        String postUrl = "${request.contextPath}${config.apf.filterProcessesUrl}"
        render view: view, model: [postUrl            : postUrl,
                                   rememberMeParameter: config.rememberMe.parameter,
                                    message: message]
    }

    /**
     * The redirect action for Ajax requests.
     */
    def authAjax() {
        if (request.xhr) {  // For any AJAX request or any condition you want
            session["SPRING_SECURITY_SAVED_REQUEST"] = null
        }
        response.setHeader 'Location', SpringSecurityUtils.securityConfig.auth.ajaxLoginFormUrl
        response.sendError HttpServletResponse.SC_UNAUTHORIZED
    }

    /**
     * Show denied page.
     */
    def denied() {
        if (springSecurityService.isLoggedIn() &&
                authenticationTrustResolver.isRememberMe(SCH.context?.authentication)) {
            // have cookie but the page is guarded with IS_AUTHENTICATED_FULLY
            redirect action: 'full', params: params
        }
    }

    /**
     * Login page for users with a remember-me cookie but accessing a IS_AUTHENTICATED_FULLY page.
     */
    def full() {
        def config = SpringSecurityUtils.securityConfig
        render view: 'auth', params: params,
                model: [hasCookie: authenticationTrustResolver.isRememberMe(SCH.context?.authentication),
                        postUrl  : "${request.contextPath}${config.apf.filterProcessesUrl}"]
    }

    /**
     * Callback after a failed login. Redirects to the auth page with a warning message.
     */
    def authfail() {
        def exception = session[WebAttributes.AUTHENTICATION_EXCEPTION]
        if (Holders.config.grails?.plugin?.springsecurity?.saml?.active) {
            forward(action: 'ssoAuthFail')
            return
        }
        String msg = getFailMessage(exception, params.boolean('sessionInvalidated'))
        if (springSecurityService.isAjax(request)) {
            render([error: msg] as JSON)
        } else {
            flash.warn = msg
            redirect action: 'auth', params: params
        }
    }

    private getFailMessage(exception, Boolean sessionInvalidated) {
        String msg = ''
        if (exception instanceof AccountExpiredException) {
            msg = message(code: "springSecurity.errors.login.expired")
        } else if (exception instanceof CredentialsExpiredException) {
            msg = message(code: "springSecurity.errors.login.passwordExpired")
        } else if (exception instanceof DisabledException) {
            msg = message(code: "springSecurity.errors.login.disabled")
        } else if (exception instanceof LockedException) {
            msg = message(code: "springSecurity.errors.login.locked")
        } else if (exception instanceof UsernameNotFoundException) {
            msg = message(code: "springSecurity.errors.login.fail")
        } else if (exception instanceof InvalidCsrfTokenException) {
            msg = message(code: "springSecurity.errors.login.csrf.token.invalid")
        } else if (exception instanceof MissingCsrfTokenException) {
            msg = message(code: "springSecurity.errors.login.csrf.token.missing")
        } else if (exception instanceof SessionAuthenticationException) {
            msg = message(code: "springSecurity.errors.login.multiple")
        } else if (sessionInvalidated) {
            msg = message(code: "springSecurity.errors.login.sessionInvalidated")
        } else {
            msg = message(code: "springSecurity.errors.login.fail")
        }
        return msg
    }


    /**
     * The Ajax success redirect url.
     */
    def ajaxSuccess() {
        render([success: true, username: springSecurityService.authentication.name] as JSON)
    }

    /**
     * The Ajax denied redirect url.
     */
    def ajaxDenied() {
        render([error: 'access denied'] as JSON)
    }

    def ssoAuthFail() {
        def exception = session[WebAttributes.AUTHENTICATION_EXCEPTION]
        String msg = getSSOFailMessage(exception, params.boolean('sessionInvalidated'))
        if (springSecurityService.isAjax(request)) {
            render([error: msg] as JSON)
            return
        }
        render(view: '/errors/errorSSOAuth', model: [msg: msg])
    }

    private String getSSOFailMessage(exception, Boolean sessionInvalidated) {
        String msg = 'Error Occurred during SSO Authentication'
        if (exception instanceof AccountExpiredException) {
            msg = message(code: "springSecurity.errors.login.expired")
        } else if (exception instanceof CredentialsExpiredException) {
            msg = message(code: "springSecurity.errors.login.passwordExpired")
        } else if (exception instanceof InvalidCsrfTokenException) {
            msg = message(code: "springSecurity.errors.login.csrf.token.invalid")
        } else if (exception instanceof MissingCsrfTokenException) {
            msg = message(code: "springSecurity.errors.login.csrf.token.missing")
        } else if (exception instanceof SSOUserDisabledException) {
            msg = message(code: 'springSecurity.errors.login.sso.disabled')
        } else if (exception instanceof SSOUserLockedException) {
            msg = message(code: 'springSecurity.errors.login.sso.locked')
        } else if (exception instanceof SSOUserNotConfiguredException) {
            msg = message(code: 'springSecurity.errors.login.sso.notfound')
        } else if (exception instanceof SSOConfigurationException) {
            msg = message(code: 'springSecurity.errors.login.sso.config')
        } else if (exception instanceof SessionAuthenticationException) {
            msg = message(code: "springSecurity.errors.login.multiple")
        } else if (sessionInvalidated) {
            msg = message(code: "springSecurity.errors.login.sessionInvalidated")
        }
        return msg
    }
}
