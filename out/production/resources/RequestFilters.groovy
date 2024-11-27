import org.apache.log4j.MDC

class RequestFilters {

    def springSecurityService

    def filters = {
        /**
         * Log performance metrics & setup a LOCALE if not set
         */
        all(controller:'*', action:'*') {
            before = {
                request.startTime = System.currentTimeMillis()

                /**
                 * Lookup user timezone and locale for first time and set
                 */
                def currentUser = springSecurityService.currentUser
                if (!session.'user.preference.timeZone' && currentUser) {
                    if(currentUser?.preference?.timeZone) {
                        //@TODO translate the string .timeZone into format TimeZone understands
                        session['user.preference.timeZone'] = TimeZone.getTimeZone(currentUser.preference.timeZone)
                    }
                }

                if (!session.'org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE' && currentUser) {
                    if(currentUser?.preference?.locale) {
                        session['org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'] = currentUser.preference.locale
                    }
                }
                MDC.put('user', currentUser?.username ?: "")
                request.nav = controllerName
            }
            after = { Map model ->
                request.afterView = System.currentTimeMillis()
            }
            afterView = { Exception e ->
                def now = System.currentTimeMillis()
                if ( !e ) {
                    log.info("[${response.status}] ${request.forwardURI} : ${now - request.afterView}/${now - request.startTime}ms")
                }
                MDC.remove('user')
            }
        }
    }
}
