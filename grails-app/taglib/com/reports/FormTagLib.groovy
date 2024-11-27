package com.reports

import com.rxlogix.helper.LocaleHelper
import org.springframework.web.servlet.support.RequestContextUtils

class FormTagLib {

    static  namespace = "rx"

    Closure localeSelect = { attrs ->
        attrs.from = LocaleHelper.getSupportedLocales()
        attrs.value = (attrs.value ?: RequestContextUtils.getLocale(request))?.toString()
        // set the key as a closure that formats the locale
        attrs.optionKey = { it.country ? "${it.language}_${it.country}" : it.language }
        // set the option value as a closure that formats the locale for display
        attrs.optionValue = {it.displayName}

        // use generic select
        out << select(attrs)
    }

}
