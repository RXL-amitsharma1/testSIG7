package com.rxlogix.config

class AllowedDictionaryDataCache implements Serializable {

    Integer fieldLevelId
    String label
    Boolean isProduct
    String allowedData
    String allowedDataIds

    static belongsTo = [productDictionaryCache : ProductDictionaryCache]

    static mapping = {
        table("ALLOWED_DICTIONARY_CACHE")
        allowedData type: "text", sqlType: "clob"
        allowedDataIds type: "text", sqlType: "clob"
    }
}
