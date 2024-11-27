/* Copyright 2013 SpringSource.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rxlogix

import com.rxlogix.signal.ClipboardCases
import com.rxlogix.user.User
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Holders
import org.springframework.security.access.annotation.Secured

@Secured('permitAll')
class LogoutController {

    def userService
    def CRUDService
    def jwtService

    /**
     * Index action. Redirects to the Spring security logout uri.
     */
    def index() {

        // TODO put any pre-logout code here
        User user = userService.getUser()
        if( user != null ) {
            ClipboardCases instance = ClipboardCases.findByUser(user)
            if(instance){
                CRUDService.delete(instance)
            }
            jwtService.invalidateCurrentToken(user.username)
        }
        redirect uri: SpringSecurityUtils.securityConfig.logout.filterProcessesUrl // '/j_spring_security_logout'
    }

    def local() {
        if (!Holders.config.grails.plugin.springsecurity.saml.active) {
            redirect(url: '/')
            return
        }
        render(view: 'local')
    }
}
