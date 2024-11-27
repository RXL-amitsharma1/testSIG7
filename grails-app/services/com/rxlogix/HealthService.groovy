package com.rxlogix

import groovy.sql.Sql
import grails.gorm.transactions.Transactional
import groovyx.net.http.Method
import org.springframework.ldap.core.LdapTemplate
import org.springframework.ldap.core.support.DefaultDirObjectFactory
import org.springframework.ldap.core.support.LdapContextSource
import org.apache.http.HttpStatus
import javax.sql.DataSource


@Transactional
class HealthService {
    DataSource dataSource
    DataSource dataSource_pva
    DataSource dataSource_spotfire
    DataSource dataSource_eudra
    DataSource dataSource_faers
    DataSource dataSource_vaers
    DataSource dataSource_vigibase
    def reportIntegrationService

    boolean isDatabaseRunning(String ds) {
        Sql sqlMart = null
        Sql sqlWebapp = null

        try {
            DataSource dataSourceWebapp = null;
            DataSource dataSourceMart = null;

            if (ds.equals(Constants.SystemPrecheck.SAFETY)) {
                dataSourceMart = dataSource_pva
            } else if (ds.equals(Constants.SystemPrecheck.FAERS)) {
                dataSourceMart = dataSource_faers
            } else if (ds.equals(Constants.SystemPrecheck.PVS)) {
                dataSourceWebapp = dataSource
            } else if (ds.equals(Constants.SystemPrecheck.VAERS)) {
                dataSourceMart = dataSource_vaers
            } else if (ds.equals(Constants.SystemPrecheck.VIGIBASE)) {
                dataSourceMart = dataSource_vigibase
            } else if (ds.equals(Constants.SystemPrecheck.SPOTFIRE)) {
                dataSourceMart = dataSource_spotfire
            } else if (ds.equals(Constants.SystemPrecheck.EUDRA) || ds.equals(Constants.SystemPrecheck.EVDAS)) {
                dataSourceMart = dataSource_eudra
            }
            if(dataSourceWebapp) {
                sqlWebapp = new Sql(dataSourceWebapp)
                sqlWebapp?.execute('SELECT 1')
            }
            if(dataSourceMart) {
                sqlMart = new Sql(dataSourceMart)
                sqlMart?.execute('SELECT 1 FROM DUAL')
            }
        } catch (Exception ex) {
            log.error(ex.stackTrace.toString())
            return Boolean.FALSE
        }finally {
            sqlWebapp?.close()
            sqlMart?.close()

        }
    }


    boolean isRunning(String url) throws IOException {
        boolean isRunning = false;
        HttpURLConnection con = null;
        try {
            URL urlObj = new URL(url);
            con = (HttpURLConnection) urlObj.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(3000);
            con.connect();
            int code = con.getResponseCode();
            if (code == 200) {
                isRunning = true
            }
        } catch (Exception e) {
            isRunning = false
        } finally {
            if (con != null)
                try {
                    con.disconnect()
                }
                catch (Exception e) {
                    log.error(e.message)
                    log.error("Unable to close opened socket connection to server $url")
                }
        }
        return isRunning;
    }


    boolean isRunning(String url, String path, String urlName) throws IOException {
        boolean isRunning = false
        try {
            Map response = reportIntegrationService.postData(url, path, null)
            log.info("Health Check API Response from ${urlName} : ${response}")
            if (response.status == HttpStatus.SC_OK) {
                isRunning = true
            } else {
                isRunning = false
            }
        } catch (Exception ex) {
            isRunning = false log.error(ex.printStackTrace())
        }
        log.info("${urlName} connectivity status: " + isRunning)
        return isRunning
    }

     boolean pingSpotfireServer() {
        try {
            String host = config.spotfire.server + ":" + config.spotfire.port
            String url = "/spotfire/login.html"
            log.info("spotfire host " + host + " url" + url)
            return isRunning(host+url)
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
        int timeout = 20
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
