package com.rxlogix.api

import com.rxlogix.ExceptionHandlingController
import com.rxlogix.security.Authorize
import com.rxlogix.user.User
import com.rxlogix.util.DateUtil
import com.rxlogix.util.RestApiResponse
import grails.converters.JSON
import grails.rest.RestfulController
import grails.util.Holders
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormatter
import org.springframework.http.HttpStatus
import org.springframework.session.SessionRepository

import java.util.stream.Collectors

class UserRestController extends RestfulController implements ExceptionHandlingController {
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormat.forPattern("dd-MMM-yyyy hh:mm a")

    UserRestController() {
        super(User, true);
    }

    SessionRepository sessionRepository
    def userService

    def fetchUser() {
        String token = request.getHeader("X-Auth-Token")
        log.info("Session id received: " + token)
        def status
        Map result = [:]
        if (token) {
            String decodedSessionId = com.rxlogix.RxCodec.decode(token)
            def activeSession = sessionRepository.getSession(decodedSessionId)
            if (activeSession) {
                User user = User.findByUsername(activeSession.getAttribute("javamelody.remoteUser"))
                if (user) {
                    result.username = user.username
                    result.fullName = user.fullName
                    result.email = user.email
                    result.fullNameAndUsername = user.fullName + " (" + user.username + ")"
                    if (!user.lastToLastLogin) {
                        result.lastToLastLogin = "User Never logged in."
                    } else {
                        result.lastToLastLogin = DateUtil.toDateStringWithTimeInAmPmFormat(user) + userService.getGmtOffset(user.preference.timeZone)
                    }
                    result.externalUser = true
                    result.globalUser = true
                    result.type = Holders.config.grails.plugin.springsecurity.saml.active ? "LDAP User" : ""
                    result.authType = null
                    result.role = user.getAuthorities()?.stream().map({ r -> r.getAuthority() }).collect(Collectors.toList())
                    status = HttpStatus.OK
                    log.debug("User Details: " + result)
                } else {
                    status = HttpStatus.UNAUTHORIZED
                    result.message = "No User found."
                }
            } else {
                status = HttpStatus.UNAUTHORIZED
                result.message = "No Active session found."
            }
        } else {
            status = HttpStatus.UNAUTHORIZED
            result.message = "User do not have Access"
        }
        log.info("fetch user details Completed. " + result.message)

        render(status: status, text: (result as JSON).toString(), contentType: 'application/json')
    }

//    @Authorize
    def currentUser() {
        def user = userService.getUser()
        def lastLogin = new DateTime(user.lastToLastLogin)
                .withZone(DateTimeZone.forID(user.preference.timeZone))

        def response = [id       : user.id,
                        username : user.username,
                        fullName : user.fullName,
                        theme    : 'Solid Blue',
                        timeZone : user.preference.timeZone,
                        locale   : user.preference.locale,
                        lastLogin: lastLogin.toString(DATE_TIME_FORMAT)]

        render RestApiResponse.successResponseWithData(response) as JSON
    }
}
