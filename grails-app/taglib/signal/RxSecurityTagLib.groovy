package signal

import grails.util.Holders
import org.grails.plugin.springsecurity.saml.SamlTagLib


class RxSecurityTagLib extends SamlTagLib {
    /**
     * {@inheritDocs}
     */
    def loggedInUserInfo = { attrs, body ->
        if (Holders.config.grails.plugin.springsecurity.saml.active) {
            String field = assertAttribute('field', attrs, 'loggedInUserInfo')

            def source = springSecurityService.authentication.details."${field}"

            if (source) {
                out << source.encodeAsHTML()
            }
            else {
                out << body()
            }
        } else {
            // TODO support 'var' and 'scope' and set the result instead of writing it

            String field = assertAttribute('field', attrs, 'loggedInUserInfo')

            def source
            if (springSecurityService.isLoggedIn()) {
                source = determineSource()
                for (pathElement in field.split('\\.')) {
                    source = source."$pathElement"
                    if (source == null) {
                        break
                    }
                }
            }

            if (source) {
                out << source.encodeAsHTML()
            } else {
                out << body()
            }
        }
    }
}