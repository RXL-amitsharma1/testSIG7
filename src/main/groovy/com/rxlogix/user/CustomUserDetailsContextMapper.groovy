package com.rxlogix.user

import com.rxlogix.Constants
import grails.gorm.transactions.Transactional
import org.apache.commons.logging.LogFactory
import org.springframework.ldap.core.DirContextAdapter
import org.springframework.ldap.core.DirContextOperations
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper

import com.rxlogix.enums.AuthType
import com.rxlogix.enums.UserType

class CustomUserDetailsContextMapper implements UserDetailsContextMapper {
    private static final log = LogFactory.getLog(this)

    def grailsApplication

    @Override
    @Transactional
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection authorities) {
        String fullnameAttr = grailsApplication.config.grails.plugin.springsecurity.ldap.fullName.attribute
        String emailAttr = grailsApplication.config.grails.plugin.springsecurity.ldap.email.attribute

        String fullName = Constants.Commons.BLANK_STRING
        try {
            fullName = ctx?.originalAttrs?.attributes[fullnameAttr]?.values[0]
            log.info("The user $username 'full name is : $fullName")
            if (!fullName) {
                log.error(" ================================== User, ${username}, full name not found with attribute, ${fullnameAttr}")
            }
        } catch (Exception ex) {
            log.error(ex.getMessage())
            log.error("Error came while fetching the fullname attribute.")
        }

        String email = Constants.Commons.BLANK_STRING

        try {
            email = ctx?.originalAttrs?.attributes[emailAttr]?.values[0]?.toString()?.toLowerCase()
            log.info("The user $username ' email is : $email")
            if (!email) {
                log.error(" ==================================, User, ${username}, email not found with attribute, ${emailAttr}")
            }
        } catch (Exception ex) {
            log.error(ex.getMessage())
            log.error("Error came while fetching the email attribute.")
        }

        User user = User.findByUsernameIlike(username)

        if (!user) {
            throw new UsernameNotFoundException('User not in local database table')
        }

        if (!user.enabled) throw new DisabledException("Account Disabled")
        if (user.accountExpired) throw new AccountExpiredException("Account Expired")
        if (user.passwordExpired) throw new CredentialsExpiredException("Password Expired")
        if (user.accountLocked) throw new LockedException("Account is locked")
        if(user.type == UserType.NON_LDAP && user.passwordExpired){
            throw new CredentialsExpiredException("Password Expired")
        }
        user.authType = AuthType.Database
        Map<String, List<Object>> attributes = [:]

        def userDetails = new CustomUserDetails(user.username, user.password, user.enabled, !user.accountExpired,
                !user.passwordExpired, !user.accountLocked, authorities, user.id, user.fullName, user.email,user.type,
                AuthType.Database,
                user.passwordModifiedTime?:user.dateCreated,attributes)
        return userDetails
    }

    @Override
    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
        throw new IllegalStateException("Only retrieving data from LDAP is currently supported")
    }

}
