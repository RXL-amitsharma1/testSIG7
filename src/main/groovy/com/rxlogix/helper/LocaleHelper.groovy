package com.rxlogix.helper

class LocaleHelper {
    //todo:  this is a duplicate method with one found in PreferenceController -- refactor PreferenceController to centralize here - morett
    static List<Locale> getSupportedLocales() {
        return [new Locale("en"), new Locale("ja")]
    }

}
