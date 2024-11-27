package com.rxlogix.security

import com.rxlogix.Constants
import com.rxlogix.audit.AuditTrail
import com.rxlogix.user.User
import grails.util.Holders
import net.bull.javamelody.internal.common.LOG
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.event.AbstractAuthenticationEvent
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import org.springframework.security.web.authentication.WebAuthenticationDetails
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import ua_parser.Client
import ua_parser.Parser

import javax.servlet.http.HttpServletRequest

class SecurityEventListener implements ApplicationListener<ApplicationEvent> {

    def CRUDService

    @Override
    void onApplicationEvent(ApplicationEvent event) {
        if (!(event instanceof AbstractAuthenticationEvent)) {
            return
        }

        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.
                currentRequestAttributes()).
                getRequest();
        final String userAgentHeader = httpServletRequest.getHeader("user-agent")
        List browserAndDeviceDetails = getBrowserAndDeviceDetails(userAgentHeader)
        String timeZone=""
        String userIpAddress = ""

        if (httpServletRequest.getHeader("X-Forwarded-For") != null) {
            //for Environmens with load balancers
            userIpAddress = httpServletRequest.getHeader("X-Forwarded-For")
        } else {
            userIpAddress = httpServletRequest.getRemoteAddr()
        }

        if (isAuthenticationFailure(event)) {
            LOG.info("inside authentication failure")
            try {
                if (event.exception instanceof BadCredentialsException) {
                    User.withNewTransaction {
                        User user = User.findByUsernameIlike(event?.source?.principal)
                        if (user) {
                            user.badPasswordAttempts++
                            if (user.badPasswordAttempts >= 3) {
                                user.accountLocked = true
                            }
                            user.save(flush:true,failOnError:true)
                        }
                    }
                }

                String username = event?.source?.principal instanceof String ?
                        event?.source?.principal : event?.source?.principal?.username

                if(username){
                    AuditTrail auditLog = new AuditTrail(category: AuditTrail.Category.LOGIN_FAILED.toString(),
                            applicationName: Holders.config.grails.plugin.auditLog.applicationName,
                            username: username,entityName: AuditTrail.Category.LOGIN_FAILED.displayName,
                            description: event?.exception?.message,userIpAddress:userIpAddress,moduleName: Constants.OTHER_STRING,
                            browser: browserAndDeviceDetails[0], device: browserAndDeviceDetails[1],timeZone: timeZone, entityValue: "Bad credentials")
                    AuditTrail.withNewTransaction {
                        auditLog.save(flush:true,failOnError:true)
                    }
                }
            } catch (Throwable th) {
                th.printStackTrace()
            }
        }

        if (event instanceof AuthenticationSuccessEvent) {
            LOG.info("inside authentication success")
            try {
                String fullname

                String username = event?.source?.principal instanceof String ?
                        event?.source?.principal as String : event?.source?.principal?.username

                User.withNewTransaction {
                    User user = User.findByUsernameIlike(username)
                    fullname = user.fullName
                    user.badPasswordAttempts = 0
                    //We need to capture last login not current login that's why added this.
                    user.lastToLastLogin = user.lastLogin
                    user.lastLogin = new Date()
                    user.colOrder = null
                    user.colUserOrder = null
                    user.save(flush:true,failOnError:true)
                    timeZone=user.preference.timeZone
                }


                AuditTrail auditLog = new AuditTrail(category: AuditTrail.Category.LOGIN_SUCCESS.toString(),
                        username: username, fullname: fullname, applicationName: Holders.config.grails.plugin.auditLog.applicationName,
                        description: "Login Successful",userIpAddress:userIpAddress,
                        entityName:AuditTrail.Category.LOGIN_SUCCESS.displayName,moduleName: Constants.OTHER_STRING,
                        browser: browserAndDeviceDetails[0], device: browserAndDeviceDetails[1],timeZone: timeZone, entityValue: "Login Successful")

                AuditTrail.withNewTransaction {
                    auditLog.save(flush:true,failOnError:true)
                }
            } catch (Throwable th) {
                th.printStackTrace()
            }
        }
    }

    private boolean isAuthenticationFailure(ApplicationEvent event) {
        if (event instanceof AbstractAuthenticationEvent) {
            if (event instanceof AbstractAuthenticationFailureEvent) {
                return true
            }
        }
        return false
    }

    private List getBrowserAndDeviceDetails(String userAgent) {
        String browserDetails = null
        String deviceDetails = null
        List details = []
        Parser uaParser = new Parser()
        String majorVersion = ""
        String minorVersion = ""
        Boolean isMicrosoftEdge = false
        if(userAgent.contains("Edg/")){
            isMicrosoftEdge = true
            int index = userAgent.indexOf("Edg/")
            if (index != -1) {
                def versionMatch = userAgent =~ /Edg\/(\d+)\.(\d+)/
                if (versionMatch.find()) {
                    majorVersion = versionMatch.group(1)
                    minorVersion = versionMatch.group(2)
                }
            }
        }
        Client client = uaParser.parse(userAgent);
        if (client != null) {
            browserDetails = isMicrosoftEdge? "Microsoft Edge" + " " + majorVersion + "." + minorVersion:client.userAgent.family + " " + client.userAgent.major + "." + client.userAgent.minor
            deviceDetails = client.os.family
            details.add(browserDetails)
            details.add(deviceDetails)
        }
        return details
    }

}
