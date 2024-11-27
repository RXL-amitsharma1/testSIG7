package com.rxlogix

import com.rxlogix.outlook.IdToken
import com.rxlogix.outlook.OutlookUser
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import org.joda.time.DateTimeZone
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.util.UriComponentsBuilder

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.OK

@Secured(["isAuthenticated()"])
class OutlookController {

    def outlookService
    def userService

    def login() {
        // Save the state and nonce in the session so we can
        // verify after the auth process redirects back
        HttpSession session = request.getSession();
        UUID state = UUID.randomUUID();
        UUID nonce = UUID.randomUUID();
        session.setAttribute("expected_state", state);
        session.setAttribute("expected_nonce", nonce);
        String url = getLoginUrl(state, nonce);
        redirect url: url
    }

    private String getLoginUrl(UUID state, UUID nonce) {
        Map outlookConfig = outlookService.getOutlookConfig()
        List scopes = outlookConfig.scopes as List
        String authorizeUrl = "${outlookConfig.authorizeUrl}/common/oauth2/v2.0/authorize"
        log.info("generated authorize url : ${authorizeUrl}")
        UriComponentsBuilder urlBuilder = UriComponentsBuilder.fromHttpUrl(authorizeUrl)
        urlBuilder.queryParam("client_id", outlookConfig.appId)
                .queryParam("redirect_uri", outlookConfig.redirectUrl)
                .queryParam("response_type", "code id_token")
                .queryParam("scope", scopes.join(' '))
                .queryParam("state", state)
                .queryParam("nonce", nonce)
                .queryParam("response_mode", "form_post");
        String url = urlBuilder.toUriString()
        log.info("generated outlook url : ${url}")
        return url
    }

    def response(String code, String id_token, String state) {
        log.info("outlook redirect url called with params : ${params}")
        try {
            if (!params.error) {
                HttpSession session = request.getSession();
                UUID expectedState = (UUID) session.getAttribute("expected_state");
                UUID expectedNonce = (UUID) session.getAttribute("expected_nonce");
                log.info("expectedState : ${expectedState}")
                log.info("expectedNonce : ${expectedNonce}")
                session.removeAttribute("expected_state");
                session.removeAttribute("expected_nonce");
                UUID sateUUID = UUID.fromString(state)
                if (sateUUID.equals(expectedState)) {
                    Map responseMap = outlookService.authorize(code, id_token, expectedNonce.toString())
                    if (!responseMap.status) {
                        flash.outlookMessage = responseMap.message
                    }
                } else {
                    flash.outlookMessage = "Unexpected state returned from authority."
                }
            } else {
                log.info("Error = ${params.error} , Error Description = ${params.error_description}")
                flash.outlookMessage = "Some error occurred while login in outlook, please try again later."
            }
        } catch (Exception e) {
            e.printStackTrace()
            flash.outlookMessage = "Some error occurred while login in outlook, please try again later."
        }
        redirect(controller: "dashboard", action: "index")
    }

}
