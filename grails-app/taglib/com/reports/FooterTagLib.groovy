package com.reports
import grails.util.Holders

class FooterTagLib {
    static  namespace = "rx"

    /**
     * Render page load statistics and build version number
     */

    def pageInfo = { attrs, body ->

        def now = System.currentTimeMillis()
        def afterView = request.afterView ?: now
        def startTime = request.startTime ?: now

        out << "<p class='right'>Build: "
        out << rx.buildVersion() + " "
        out << g.formatDate(date: lookupDate.call(), type: "datetime", style: "LONG", timeZone: attrs.timeZone)
        out << " - Loaded: ${now -afterView}/${now-startTime}ms</p>"
    }

    String buildVersion = { attrs ->
        out << lookupVersion.call()
    }

    String buildDate = { attrs ->
        out << lookupDate.call()
    }

    def renderSecurityPolicyLink = { attrs ->
        if (!Holders.config.pvsignal.privacy.policy.link) {
            out << ""
            return
        }
        attrs.url = Holders.config.pvsignal.privacy.policy.link
        out << link(attrs) {
            message(code: 'app.security.privacy.policy.link.label')
        }
    }

    private def lookupDate = { ->
        def buildDate = g.meta(name: 'build.date')
        if ( buildDate ) {
            return Date.parse("MM/dd/yyyy HH:mm:ss zzz", buildDate)
        }
        return new Date()
    }.memoize()

    private def lookupVersion = { ->
        return g.meta(name:'build.version') ?: "UNKNOWN"
    }.memoize()
}
