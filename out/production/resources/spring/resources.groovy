import com.rxlogix.ApplicationNotificationService
import com.rxlogix.CacheMessageSource
import com.rxlogix.CustomHttpSessionRequestCache
import com.rxlogix.DBTokenStore
import com.rxlogix.cache.HazelcastService
import com.rxlogix.helper.NotificationHelper
import com.rxlogix.security.DummyFilter
import com.rxlogix.security.SecurityEventListener
import com.rxlogix.security.csrf.PVAccessDeniedHandler
import com.rxlogix.security.csrf.PVRequireCsrfProtectionMatcher
import com.rxlogix.session.HttpSessionSynchronizer
import com.rxlogix.session.PVHazelcastInstanceInitializer
import com.rxlogix.session.PVHttpSessionServletListener
import com.rxlogix.session.SpringSessionConfig
import com.rxlogix.session.HazelcastStoreSessionConfig
import com.rxlogix.session.SpringSessionConfigProperties
import com.rxlogix.user.CustomSamlUserDetailsService
import com.rxlogix.user.CustomUserDetailsContextMapper
import com.rxlogix.util.marshalling.CustomMarshallerRegistry
import grails.util.Holders
import org.springframework.security.ldap.SpringSecurityLdapTemplate
import org.springframework.security.web.csrf.CsrfFilter
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository
import org.springframework.security.web.session.ConcurrentSessionFilter
import org.springframework.session.MapSessionRepository
import org.springframework.session.hazelcast.config.annotation.web.http.HazelcastHttpSessionConfiguration
import org.springframework.security.saml.websso.WebSSOProfileConsumerImpl
import org.springframework.session.security.SpringSessionBackedSessionRegistry
import org.springframework.session.web.http.CookieHttpSessionStrategy
import org.springframework.session.web.http.DefaultCookieSerializer
import org.springframework.session.web.http.SessionRepositoryFilter
import com.rxlogix.scim.ScimGroupRepositoryImpl
import com.rxlogix.scim.ScimUserRepositoryImpl
import org.springframework.security.core.session.SessionRegistryImpl
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy
import org.springframework.security.web.authentication.session.CompositeSessionAuthenticationStrategy
import com.rxlogix.security.CustomSessionLogoutHandler

import com.rxlogix.HazelcastNotificationService
import com.rxlogix.ApplicationConfigService

// Place your Spring DSL code here
beans = {
    customMarshallerRegistry(CustomMarshallerRegistry)
    sessionRegistry(SessionRegistryImpl)
    ldapUserDetailsMapper(CustomUserDetailsContextMapper) {
        grailsApplication = ref('grailsApplication')
    }

    if (grailsApplication.config.grails?.plugin?.springsecurity?.saml?.active &&
            grailsApplication.config.saml?.lb?.enabled) {
        contextProvider(org.springframework.security.saml.context.SAMLContextProviderLB) {
            scheme = grailsApplication.config.saml.lb.scheme
            serverName = grailsApplication.config.saml.lb.serverName
            serverPort = grailsApplication.config.saml.lb.port as Integer
            contextPath = grailsApplication.config.saml.lb.contextPath
        }
    }
    customSessionLogoutHandler(CustomSessionLogoutHandler) {
        sessionRegistry = ref('sessionRegistry')
    }
    customSessionLogoutHandler(CustomSessionLogoutHandler, ref('sessionRegistry'))
    if (Holders.config.springsession.enabled) {
        log.info("Initializing spring session...")
        springSessionConfigProperties(SpringSessionConfigProperties, Holders.config.springsession)
        if (Holders.config.hazelcast.enabled) {
            log.info("Hazelcast is enabled via config, starting Hazelcast Instance...")
            hazelcastInstance(PVHazelcastInstanceInitializer) {
                grailsApplication = ref('grailsApplication')
                hazelcastConfig = Holders.config.hazelcast as ConfigObject
                springSessionConfigProperties = ref('springSessionConfigProperties')
            }
            sessionStoreConfiguration(HazelcastStoreSessionConfig, ref('grailsApplication'), ref('springSessionConfigProperties'))
        } else {
            sessionRepository(MapSessionRepository) {
                defaultMaxInactiveInterval = Holders.config.springsession.timeout.interval as Integer
            }
            sessionStoreConfiguration(SpringSessionConfig)
        }
        httpSessionSynchronizer(HttpSessionSynchronizer, ref('springSessionConfigProperties'))
        springSessionRepositoryFilter(SessionRepositoryFilter, ref('sessionRepository'))
        log.info("...spring session initialization done.")
    }
    if (Holders.config.singleUserSession.enabled) {
        concurrentSingleSessionAuthenticationStrategy(ConcurrentSessionControlAuthenticationStrategy, ref('sessionRegistry')) {
            exceptionIfMaximumExceeded = !Holders.config.singleUserSession.invalidateOld
        }
        sessionFixationProtectionStrategy(SessionFixationProtectionStrategy) {
            migrateSessionAttributes = true
            alwaysCreateSession = true
        }
        registerSessionAuthenticationStrategy(RegisterSessionAuthenticationStrategy, ref('sessionRegistry'))
        sessionAuthenticationStrategy(CompositeSessionAuthenticationStrategy, [ref('concurrentSingleSessionAuthenticationStrategy'), ref('sessionFixationProtectionStrategy'), ref('registerSessionAuthenticationStrategy')])
        concurrentSessionFilter(ConcurrentSessionFilter, ref('sessionRegistry'),'/login/authfail?sessionInvalidated=true'){
            redirectStrategy = ref('redirectStrategy')
        }
    } else {
        concurrentSessionFilter(DummyFilter)
    }


    userDetailsService(CustomSamlUserDetailsService) {
        isSamlActive = grailsApplication.config.grails?.plugin?.springsecurity?.saml?.active ?: false
        samlAttrName = grailsApplication.config.grails?.plugin?.springsecurity?.saml?.userAttributeMappings?.username ?: 'username'
        grailsApplication = ref('grailsApplication')
    }

    securityEventListener(SecurityEventListener)
    ldapTemplate(SpringSecurityLdapTemplate, ref('contextSource')) {
        ignorePartialResultException = true
    }

    if (grailsApplication.config.pvsignal.csrfProtection.enabled) {
        pvAccessDeniedHandler(PVAccessDeniedHandler)
        pvRequireCsrfProtectionMatcher(PVRequireCsrfProtectionMatcher)
        csrfFilter(CsrfFilter, new HttpSessionCsrfTokenRepository()) {
            requireCsrfProtectionMatcher = ref('pvRequireCsrfProtectionMatcher')
        }
    }

    ConfigObject springSessionConfig = grailsApplication.config.springsession
    ConfigObject hzConfig = grailsApplication.config.hazelcast
    springSessionConfigProperties(SpringSessionConfigProperties, hzConfig)


    hazelcastHttpSessionConfiguration(HazelcastHttpSessionConfiguration) {
    }

    hazelcastService(HazelcastService) {
        hazelcastInstance = ref('hazelcastInstance')
    }

    messageSource(CacheMessageSource) {
        basenames = "WEB-INF/grails-app/i18n/messages"
        cacheService = ref("cacheService")
        pluginManager = ref('pluginManager')
    }

    httpSessionSynchronizer(HttpSessionSynchronizer) {
        persistMutable = springSessionConfig.allow.persist.mutable as Boolean
    }

    pvSessionListener(PVHttpSessionServletListener) {
    }

    requestCache(CustomHttpSessionRequestCache) {
        portResolver = ref('portResolver')
        createSessionAllowed = grailsApplication.config.requestCache.createSession // true
        requestMatcher = ref('requestMatcher')
    }

    notificationHelper(NotificationHelper) { bean ->
        bean.autowire = 'byName'
    }

    if (Holders.config.grails.mail.oAuth.enabled) {
        tokenStore(DBTokenStore)
    }

    cookieSerializer(DefaultCookieSerializer) {
        if (Holders.config.pvreports.cookieName)
            cookieName = Holders.config.pvsignal.cookieName
    }
    
    httpSessionStrategy(CookieHttpSessionStrategy) {
        cookieSerializer = ref('cookieSerializer')
    }
    
    springSessionRepositoryFilter(SessionRepositoryFilter, ref('sessionRepository')) {
        httpSessionStrategy = ref('httpSessionStrategy')
    }
    
    if (Holders.config.grails?.plugin?.springsecurity?.saml?.active) {
        //Overridden for adding maxAuthenticationAge
        webSSOprofileConsumer(WebSSOProfileConsumerImpl) {
            responseSkew = Holders.config.grails.plugin.springsecurity.saml.responseSkew
            maxAuthenticationAge = Holders.config.grails.plugin.springsecurity.saml.maxAuthenticationAge
        }
    }

    scimUserRepository(ScimUserRepositoryImpl) {
        CRUDService = ref('CRUDService')
        allowExistingUserMigrate = grailsApplication.config.getProperty('grails.scim.migrate.existing', Boolean, false)
    }

    scimGroupRepository(ScimGroupRepositoryImpl) {
        CRUDService = ref('CRUDService')
        allowExistingGroupMigrate = grailsApplication.config.getProperty('grails.scim.migrate.existing', Boolean, false)
    }

    hazelcastNotificationService(HazelcastNotificationService) {
        configurationDataSource = ref('dataSource_pva') //pva data source
        configuredAppName = "PVS"
    }

    applicationConfigService(ApplicationConfigService) {
        refreshDependenciesService = ref('refreshConfigurationService')
        refreshDbConfigService = ref('refreshDbConfigService')
    }

    applicationNotificationService(ApplicationNotificationService){
        configuredAppName = "PVS"
    }
}
