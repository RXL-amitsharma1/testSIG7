package com.rxlogix.user

import org.grails.plugin.springsecurity.saml.SamlUserDetails
import org.springframework.security.core.GrantedAuthority
import com.rxlogix.enums.AuthType
import com.rxlogix.enums.UserType

import java.text.SimpleDateFormat

class CustomUserDetails extends SamlUserDetails {
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    final String fullName
    final String email
    UserType type
    AuthType authType
    transient Date passwordModifiedTime

    CustomUserDetails(String username, Collection<GrantedAuthority> authorities, long id, String fullName) {
        super(username, '', true, true, true, true, authorities, id, new HashMap<String, List<Object>>([:]))
        this.fullName = fullName
    }

    CustomUserDetails(String username, String password, boolean enabled,
                        boolean accountNonExpired, boolean credentialsNonExpired,
                        boolean accountNonLocked,
                        Collection<GrantedAuthority> authorities,
                        long id, String fullName, String email, Map<String, List<Object>> attributes) {

        super(username, password?:'', enabled, accountNonExpired,
              credentialsNonExpired, accountNonLocked, authorities, id, attributes)

        this.fullName = fullName
        this.email = email
    }

    CustomUserDetails(String username, String password, boolean enabled,
                      boolean accountNonExpired, boolean credentialsNonExpired,
                      boolean accountNonLocked,
                      Collection<GrantedAuthority> authorities,
                      long id, String fullName, String email,
                      UserType userType,
                      AuthType authType,
                      Date passwordModifiedTime, Map<String, List<Object>> attributes) {

        this(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities,
                id, fullName, email, attributes)
        this.type = userType
        this.authType = authType
        this.passwordModifiedTime = passwordModifiedTime
        sdf.setTimeZone(TimeZone.default)
    }

}
