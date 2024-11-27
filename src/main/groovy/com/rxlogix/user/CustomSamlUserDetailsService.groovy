package com.rxlogix.user

import com.rxlogix.enums.AuthType
import com.rxlogix.user.sso.exception.SSOUserLockedException
import com.rxlogix.user.sso.exception.SSOUserNotConfiguredException
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugin.springsecurity.userdetails.NoStackUsernameNotFoundException
import groovy.util.logging.Slf4j
import com.rxlogix.user.sso.exception.SSOUserAccountExpiredException
import org.grails.plugin.springsecurity.saml.SpringSamlUserDetailsService
import org.opensaml.core.xml.XMLObject
import org.opensaml.core.xml.schema.XSString
import org.opensaml.core.xml.schema.impl.XSAnyImpl
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal


@Slf4j
class CustomSamlUserDetailsService extends SpringSamlUserDetailsService {
    String samlAttrName
    boolean isSamlActive

    /**
     * Some Spring Security classes (e.g. RoleHierarchyVoter) expect at least
     * one role, so we give a user with no granted roles this one which gets
     * past that restriction but doesn't grant anything.
     */



    @Override
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String[] methodNames = Thread.currentThread().stackTrace*.methodName
        if (!methodNames.contains('loadUserBySAML')) {
            this.loadUserByUsername(username, true)
        }
        else{
            User.withTransaction { status ->
                User user = User.findByUsernameIlike(username)
                try {
                    validateUserLocally(user,username)
                } catch (AuthenticationException e) {
                    log.error("AuthenticationException: ${e.message} for ${username}")
                    throw e
                }
                def authorities = user.authorities.collect {
                    new SimpleGrantedAuthority(it.authority)
                }
                Map<String, List<Object>> attributes = [:]
                return new CustomUserDetails(user.username, user.password ?: '', user.enabled,
                        !user.accountExpired, !user.passwordExpired,
                        !user.accountLocked, authorities ?: [new SimpleGrantedAuthority(SpringSecurityUtils.NO_ROLE)], user.id,
                        user.fullName, user.email,user.type, user.authType, user.passwordModifiedTime ?: user.dateCreated, attributes)
            }
        }
    }

//    @Override //updated in grails upgrade as classes deprecated
//    Object loadUserBySAML(SAMLCredential samlCredential) throws UsernameNotFoundException {
//        if (samlCredential) {
//            def usernameObj = samlCredential.getAttribute(samlAttrName)
//            if (usernameObj instanceof Attribute) {
//                List<XMLObject> values = ((Attribute) usernameObj).getAttributeValues()
//                def username = getAttributeValue(values[0])
//                return loadUserByUsername(username)
//            }
//        }
//        return null
//    }

    @Override
    CustomUserDetails loadUserBySAML(Saml2AuthenticatedPrincipal principal) throws UsernameNotFoundException {
        if (principal) {
            String username = getSamlUsername(principal)
            return loadUserByUsername(username)
        }
        return null
    }


    private void validateUserLocally(User user,String username) throws AuthenticationException {
        if (!isSamlActive) {
            if (!user) {
                throw new NoStackUsernameNotFoundException()
            }
            return
        }
        if (!user || !user.enabled) {
            throw new SSOUserNotConfiguredException(username +': SSO User not in local database')
        }
        if (user.accountLocked) {
            throw new SSOUserLockedException(username +": SSO User Account is locked")
        }
        if(user.accountExpired){
            throw new SSOUserAccountExpiredException(username +': SSO User Account has expired')
        }

    }

    private String getAttributeValue(XMLObject attributeValue) {
        attributeValue == null ?
                null :
                attributeValue instanceof XSString ?
                        getStringAttributeValue((XSString) attributeValue) :
                        attributeValue instanceof XSAnyImpl ?
                                getAnyAttributeValue((XSAnyImpl) attributeValue) :
                                attributeValue.toString()
    }

    private String getStringAttributeValue(XSString attributeValue) {
        attributeValue.getValue()
    }

    private String getAnyAttributeValue(XSAnyImpl attributeValue) {
        attributeValue.getTextContent()
    }

    UserDetails loadUserByUsername(String username, boolean loadRoles) throws UsernameNotFoundException {
        User.withTransaction {
            def conf = SpringSecurityUtils.securityConfig
            String userClassName = conf.userLookup.userDomainClassName
            def dc = grailsApplication.getDomainClass(userClassName)
            if (!dc) {
                throw new IllegalArgumentException("The specified user domain class '$userClassName' is not a domain class")
            }

            Class<?> User = dc.clazz

            def user = User.createCriteria().get {
                eq((conf.userLookup.usernamePropertyName), username, [ignoreCase: true])
            }

            if (!user) {
                log.warn "User not found: {$username}"
                throw new NoStackUsernameNotFoundException()
            }

            Collection<GrantedAuthority> authorities = loadAuthorities(user, username, loadRoles)
            createUserDetails user, authorities
        }
    }

    protected UserDetails createUserDetails(user, Collection<GrantedAuthority> authorities) {

        def conf = SpringSecurityUtils.securityConfig

        String usernamePropertyName = conf.userLookup.usernamePropertyName
        String passwordPropertyName = conf.userLookup.passwordPropertyName
        String enabledPropertyName = conf.userLookup.enabledPropertyName
        String accountExpiredPropertyName = conf.userLookup.accountExpiredPropertyName
        String accountLockedPropertyName = conf.userLookup.accountLockedPropertyName
        String passwordExpiredPropertyName = conf.userLookup.passwordExpiredPropertyName

        String username = user."$usernamePropertyName"
        String password = user."$passwordPropertyName"
        boolean enabled = enabledPropertyName ? user."$enabledPropertyName" : true
        boolean accountExpired = accountExpiredPropertyName ? user."$accountExpiredPropertyName" : false
        boolean accountLocked = accountLockedPropertyName ? user."$accountLockedPropertyName" : false
        boolean passwordExpired = passwordExpiredPropertyName ? user."$passwordExpiredPropertyName" : false
        Date passwordModifiedTime = user.passwordModifiedTime ?: ((User)user).dateCreated
        Map<String, List<Object>> attributes = [:]

        return new CustomUserDetails(username, password, enabled, !accountExpired, !passwordExpired, !accountLocked, authorities ?: [NO_ROLE], user.id, user.fullName, user.email, user.type, AuthType.Database, passwordModifiedTime,attributes)
    }

}
