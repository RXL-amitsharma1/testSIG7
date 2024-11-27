package com.rxlogix

import org.springframework.context.i18n.LocaleContextHolder

class CustomMessageService {
    static transactional = false

    def messageSource

    def getMessage(String code, Object... args = null) {
        messageSource.getMessage(code, args, '', LocaleContextHolder.getLocale())
    }

    def getMessage(String code, Object[] args = null, String defaultMessage = '', Locale locale) {
        messageSource.getMessage(code, args, defaultMessage, locale)
    }
}
