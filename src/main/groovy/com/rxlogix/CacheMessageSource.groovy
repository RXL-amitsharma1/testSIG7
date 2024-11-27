package com.rxlogix

import org.grails.spring.context.support.PluginAwareResourceBundleMessageSource

import java.text.MessageFormat

class CacheMessageSource extends PluginAwareResourceBundleMessageSource {

    def cacheService

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        String text = getTextFromCache(code, locale)
        if (text) return new MessageFormat(text, locale)
        super.resolveCode(code, locale)
    }

    @Override
    protected String resolveCodeWithoutArguments(String code, Locale locale) {
        getTextFromCache(code, locale) ?: super.resolveCodeWithoutArguments(code, locale)
    }

    private String getTextFromCache(String code, Locale locale) {
        String text = cacheService.getPvDictionaryLocalizedString(locale.getLanguage(), code)
        if (!text)
            text = cacheService.getPvDictionaryLocalizedString("en", code) //get for default lang
        return text
    }
}
