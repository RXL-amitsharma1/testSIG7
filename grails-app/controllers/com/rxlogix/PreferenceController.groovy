package com.rxlogix

import com.rxlogix.audit.AuditTrail
import com.rxlogix.dto.ResponseDTO
import com.rxlogix.user.Preference
import com.rxlogix.user.User
import grails.plugin.springsecurity.annotation.Secured
import grails.validation.ValidationException
import org.joda.time.DateTimeZone
import grails.converters.JSON
import com.rxlogix.util.SecurityUtil
import grails.util.Holders

import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.OK

@Secured(["isAuthenticated()"])
class PreferenceController {
    def userService
    def springSecurityService
    def CRUDService
    def cacheService
    def signalAuditLogService

    def index() {
        User user = userService.getUser()
        render(view: "index", model: [
                currentLocale: convertLocaleToMap(getSessionLocale()),
                locales: buildLocaleSelectList(),
                theUserTimezone: user?.preference?.timeZone,
                timeZones: DateTimeZone.availableIDs,
                isEmailEnabled: user?.preference?.isEmailEnabled,
                isCumulativeAlertEnabled: user?.preference?.isCumulativeAlertEnabled,
                apiToken:user?.preference?.apiToken])
    }

    def update() {
        User user = userService.getUser()

        if (user == null) {
            notFound()
            return
        }
        Preference preferenceInstance = user.preference
        if (params.isEmailEnabled) {
            params.isEmailEnabled = true
        } else {
            params.isEmailEnabled = false
        }
        if (params.isCumulativeAlertEnabled) {
            params.isCumulativeAlertEnabled = true
        } else {
            params.isCumulativeAlertEnabled = false
        }
        preferenceInstance.isEmailEnabled = params.isEmailEnabled
        preferenceInstance.isCumulativeAlertEnabled = params.isCumulativeAlertEnabled
        preferenceInstance.timeZone = params.timeZone
        preferenceInstance.locale = getLocaleFromParam(params.language)
        if (params.apiToken) {
            preferenceInstance.apiToken = params.apiToken
            preferenceInstance.tokenUpdateDate = new Date()
        }

        try {
            preferenceInstance = (Preference) CRUDService.update(preferenceInstance)
            setSession(preferenceInstance)
            cacheService.setPreferenceCache(user)

        } catch (ValidationException ve) {
            render view: "index", model: [preferenceInstance: preferenceInstance]
            return
        }


        request.withFormat {
            form {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'app.label.preference'),""])
                redirect(view: "index")
            }
            '*' { respond preferenceInstance, [status: OK] }
        }


    }

    protected void notFound() {
        request.withFormat {
            form {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'accessControlGroup.label'), params.id])
                redirect action: "index", method: "GET"
            }
            '*' { render status: NOT_FOUND }
        }
    }

    private Locale getLocaleFromParam(def languagecode) {
        if (languagecode) {
            def (language, country) = languagecode.tokenize('_')
            if (country == null) {
                return new Locale(language)
            } else {
                return new Locale(language, country)
            }
        }
    }

    private def setSession(Preference preference) {
        session['org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE'] = preference.locale
        session['user.preference.timeZone'] = TimeZone.getTimeZone(preference?.timeZone)
    }

    //todo:  this needs to be in a Helper to allow usage from User Management screen. - morett
    private def buildLocaleSelectList() {
        return getSupportedLocales().collect { availableLocale ->
            return convertLocaleToMap(availableLocale)
        }
    }

    // Converts a Locale to a map that is used by the GSP's Listbox to render language + country
    //todo:  this needs to be in a Helper to allow usage from User Management screen. - morett
    private def convertLocaleToMap(Locale locale) {
        def localeMap = [:]
        localeMap.put('lang_code', locale.toString())
        if (locale.toString().find(/_/)) {
            localeMap.put('display', locale.getDisplayLanguage(locale) + " (" + locale.getDisplayCountry(locale) + ")")
        } else {
            localeMap.put('display', locale.getDisplayLanguage(locale))
        }
        return localeMap
    }

    // Do we really need this??
    private Locale getSessionLocale() {
        return session.'org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE' ?: org.springframework.web.servlet.support.RequestContextUtils.getLocale(request)
    }

    //todo:  this needs to be in a Helper to allow usage from User Management screen. - morett
    private List<Locale> getSupportedLocales() {
        return [new Locale("en"), new Locale("ja")]
    }

    def generateAPIToken() {
        def currentUser = userService.getUser()
        String oldToken = currentUser.preference.apiToken ?: ""
        if (currentUser) {
            String token = SecurityUtil.generateAPIToken(Holders.config.pvr.publicApi.token['API_PUBLIC_TOKEN'] as String,
                    currentUser.username,
                    UUID.randomUUID().toString(),
                    new Date())
            userService.updateApiToken(currentUser.username,token)
            signalAuditLogService.createAuditLog([
                    entityName : "Control Panel",
                    moduleName : "Control Panel",
                    category   : oldToken.isEmpty() ? AuditTrail.Category.INSERT.toString() : AuditTrail.Category.UPDATE.toString(),
                    entityValue: "API Token",
                    description: "Generated Token"
            ] as Map, [[propertyName: "Token", oldValue: currentUser?.preference?.apiToken ?: "", newValue: token ?: ""]] as List)
            render text: [token: token] as JSON, contentType: 'application/json', status: OK
        }
    }

    def updateUserPreferenceAbstractView(boolean flagValue) {
        ResponseDTO responseDTO = new ResponseDTO(code: 200, status: true)
        try {
            User currentUser = userService.getUser()
            Preference preference = currentUser.preference
            if (flagValue != preference.isLiteratureAbstractView) {
                preference.isLiteratureAbstractView = flagValue
                preference.save(flush: true)
            }
        } catch(Exception ex) {
            log.error("Error occurred while update user preference for literature alert", ex)
            responseDTO.code = 500
            responseDTO.status = false
            throw ex
        }
        render(responseDTO as JSON)
    }


}
