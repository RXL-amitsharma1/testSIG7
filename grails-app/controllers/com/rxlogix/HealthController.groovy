package com.rxlogix

import com.rxlogix.dto.HealthDTO
import grails.plugin.springsecurity.annotation.Secured
import groovy.sql.Sql
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.core.support.DefaultDirObjectFactory
import org.springframework.ldap.core.support.LdapContextSource

import javax.sql.DataSource

@Secured('Authenticated')
class HealthController {

    static scope = "singleton"
    static allowedMethods = [index: ["GET"]]

    DataSource dataSource
    DataSource dataSource_pva
    DataSource dataSource_spotfire
    DataSource dataSource_eudra
    DataSource dataSource_faers
    def healthService

    def index() {
        HealthDTO responseDTO = new HealthDTO()
        try {
            Map healthChecks = [:]

            healthChecks.put("PVS Database", healthService.isDatabaseRunning(Constants.SystemPrecheck.PVS.toString()))
            healthChecks.put("PVA Database",  healthService.isDatabaseRunning(Constants.SystemPrecheck.SAFETY.toString()))
            grailsApplication.config.signal.evdas.enabled ? healthChecks.put("EVDAS Database",  healthService.isDatabaseRunning(Constants.SystemPrecheck.EUDRA.toString())) : ''
            grailsApplication.config.signal.faers.enabled ? healthChecks.put("FAERS Database",  healthService.isDatabaseRunning(Constants.SystemPrecheck.FAERS.toString())) : ''
            grailsApplication.config.signal.spotfire.enabled ? healthChecks.put("Spotfire Database", healthService.isDatabaseRunning(Constants.SystemPrecheck.SPOTFIRE.toString())) : ''
            grailsApplication.config.signal.spotfire.enabled ? healthChecks.put("Spotfire Server", healthService.pingSpotfireServer()) : ''
            grailsApplication.config.grails.mail.enabled ? healthChecks.put("SMTP Server", healthService.pingSMTPServer()) : ''
            !(grailsApplication.config.grails.plugin.springsecurity.saml.active) ? healthChecks.put("LDAP Server", healthService.pingLDAPServer()) : ''

            responseDTO.setSuccessResponse(healthChecks, "OK")
        } catch (Exception ex) {
            responseDTO.setFailureResponse(ex, "FAIL")
        }
        render responseDTO.toAjaxResponse()
    }

    def ping() {
        HealthDTO responseDTO = new HealthDTO()
        responseDTO.message = "App Server is UP"
        responseDTO.data = 'OK'
        render responseDTO.toAjaxResponse()
    }


    private boolean pingPVSDatabase() {
        Sql sql = new Sql(dataSource)
        try {
            sql.execute('SELECT 1')
        } catch (Exception ex) {
            log.error(ex.stackTrace.toString())
            return Boolean.FALSE
        }finally{
            sql?.close()
        }
    }

    private boolean pingPVADatabase() {
        Sql sql = new Sql(dataSource_pva)
        try {
            sql.execute('SELECT 1')
        } catch (Exception ex) {
            log.error(ex.stackTrace.toString())
            return Boolean.FALSE
        }finally{
            sql?.close()
        }

    }

    private boolean pingSpotfireDatabase() {
        Sql sql = new Sql(dataSource_spotfire)
        try {
            sql.execute('SELECT 1')
        } catch (Exception ex) {
            log.error(ex.stackTrace.toString())
            return Boolean.FALSE
        }finally{
            sql?.close()
        }

    }

    private boolean pingEVDASDatabase() {
        Sql sql = new Sql(dataSource_eudra)
        try {
            sql.execute('SELECT 1')
        } catch (Exception ex) {
            log.error(ex.stackTrace.toString())
            return Boolean.FALSE
        }finally{
            sql?.close()
        }

    }

    private boolean pingFAERSDatabase() {
        Sql sql = new Sql(dataSource_faers)
        try {
            sql.execute('SELECT 1')
        } catch (Exception ex) {
            log.error(ex.stackTrace.toString())
            return Boolean.FALSE
        } finally {
            sql?.close()
        }

    }

    private boolean pingSpotfireServer() {
        try {
            String host = grailsApplication.config.spotfire.server
            int port = Integer.parseInt(grailsApplication.config.spotfire.port) ?: 80
            return testServerPortConnectivity(host, port)
        } catch (Exception ex) {
            log.error(ex.stackTrace.toString())
            return Boolean.FALSE
        }
    }

    private boolean pingLDAPServer() {
        try {
            ConfigObject ldap = grailsApplication.config.grails.plugin.springsecurity.ldap
            LdapContextSource sourceLdapCtx = new LdapContextSource();
            sourceLdapCtx.setUrl(ldap.context.server);
            sourceLdapCtx.setUserDn(ldap.context.managerDn);
            sourceLdapCtx.setPassword(ldap.context.managerPassword);
            sourceLdapCtx.setDirObjectFactory(DefaultDirObjectFactory.class);
            sourceLdapCtx.afterPropertiesSet();
            def sourceLdapTemplate = new LdapTemplate(sourceLdapCtx);
            sourceLdapTemplate.getContextSource().getContext(ldap.context.managerDn, ldap.context.managerPassword);
        } catch (Exception ex) {
            log.error(ex.message)
            return Boolean.FALSE
        }
    }

    private boolean pingSMTPServer() {
        try {
            ConfigObject mail = grailsApplication.config.grails.mail
            return testServerPortConnectivity(mail.host, mail.port)
        } catch (Exception ex) {
            log.error(ex.message)
            return Boolean.FALSE
        }
    }

    boolean testServerPortConnectivity(String host, int port) {
        Socket socket = null
        int timeout = 2000
        try {
            SocketAddress socketAddress = new InetSocketAddress(host, port)
            socket = new Socket()
            socket.connect(socketAddress, timeout)
            return true
        } catch (Exception e) {
            log.error("Unable to open socket connection to server $host on port $port")
            log.error(e.message)
            return Boolean.FALSE
        } finally {
            if (socket != null)
                try {
                    socket.close()
                }
                catch (Exception e) {
                    log.error(e.message)
                    log.error("Unable to close opened socket connection to server $host on port $port")
                }
        }
    }
}