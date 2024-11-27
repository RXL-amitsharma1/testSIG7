package com.rxlogix.ldap

import com.rxlogix.commandObjects.LdapCommand
import com.rxlogix.user.User
import grails.validation.ValidationException
import org.springframework.ldap.core.AttributesMapper

import javax.naming.AuthenticationException
import javax.naming.Context
import javax.naming.NamingEnumeration
import javax.naming.NamingException
import javax.naming.directory.Attributes
import javax.naming.directory.InitialDirContext
import javax.naming.directory.SearchControls
import javax.naming.directory.SearchResult
import grails.util.Holders

class LdapService {
    def grailsApplication
    def ldapTemplate
    def userService
    def CRUDService
    def ldapService


    boolean isLoginPasswordValid(String login, String password) {

        ConfigObject ldap = Holders.config.grails.plugin.springsecurity.ldap
        Properties serviceEnv = new Properties()
        serviceEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory")
        serviceEnv.put(Context.PROVIDER_URL, ldap.context.server)
        serviceEnv.put(Context.SECURITY_AUTHENTICATION, "simple")
        serviceEnv.put(Context.SECURITY_PRINCIPAL, ldap.context.managerDn as String)
        serviceEnv.put(Context.SECURITY_CREDENTIALS, ldap.context.managerPassword as String)
        def serviceCtx = new InitialDirContext(serviceEnv)

        String uid = ldap.uid.attribute
        SearchControls sc = new SearchControls()
        sc.setReturningAttributes([uid] as String[])
        sc.setSearchScope(SearchControls.SUBTREE_SCOPE)
        String searchFilter = "($uid=$login)"
        NamingEnumeration<SearchResult> results = serviceCtx.search(ldap.search.base as String, searchFilter, sc)

        // get the users DN (distinguishedName) from the result
        if (!results.hasMore()) return false
        SearchResult result = results.next()
        String distinguishedName = result.getNameInNamespace()

        // attempt another authentication, now with the user
        Properties authEnv = new Properties()
        authEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory")
        authEnv.put(Context.PROVIDER_URL, ldap.context.server)
        authEnv.put(Context.SECURITY_PRINCIPAL, distinguishedName)
        authEnv.put(Context.SECURITY_CREDENTIALS, password)
        try {
            new InitialDirContext(authEnv)
            //Authentication successful
            return true
        } catch (AuthenticationException e) {
            //Authentication sfault
        }
        return false
    }

    /**
     * Return an entire LDAP entry.  This is useful when you want to process multiple LDAP attributes for a single LDAP entry
     * and calling getLdapAttribute() multiple times would be more tedious.
     * @param filter (the search criteria)                          i.e. "uid=$username" or "uid=" + user.username
     * @return
     */
     List<LdapCommand> getLdapEntry(String filter) {
        String searchBase = grailsApplication.config.grails.plugin.springsecurity.ldap.search.base
        return ldapTemplate.search(searchBase, filter, new LdapCommandMapper())
    }

    /**
     * This is a convenience class to make it easier to transfer LDAP entries to the User object.
     */
    private class LdapCommandMapper implements AttributesMapper {
        public Object mapFromAttributes(Attributes attrs) throws NamingException {
            LdapCommand ldapCommand = new LdapCommand();
            String uid = grailsApplication.config.grails.plugin.springsecurity.ldap.uid.attribute
            String fullName = grailsApplication.config.grails.plugin.springsecurity.ldap.fullName.attribute
            String email = grailsApplication.config.grails.plugin.springsecurity.ldap.email.attribute
            ldapCommand.setUserName((String)attrs?.get(uid)?.get())
            ldapCommand.setFullName((String)attrs?.get(fullName)?.get())
            ldapCommand.setEmail((String)attrs?.get(email)?.get())
            return ldapCommand;
        }
    }


}
