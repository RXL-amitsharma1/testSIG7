package com.rxlogix.config

import com.rxlogix.user.Group

class ProductDictionaryCache implements Serializable {

    Group group
    SafetyGroup safetyGroup

    static hasMany = [allowedDictionaryData : AllowedDictionaryDataCache]

    static mapping = {
        table("PRODUCT_DICTIONARY_CACHE")
    }

    static constraints = {
        safetyGroup nullable: true
        group nullable: true
        allowedDictionaryData cascade: 'all-delete-orphan'
    }
}
