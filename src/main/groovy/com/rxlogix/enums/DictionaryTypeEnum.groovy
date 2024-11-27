package com.rxlogix.enums

enum DictionaryTypeEnum {

    PRODUCT("productSelection"),
    PRODUCT_GROUP("productGroupSelection"),
    STUDY("studySelection"),
    EVENT("eventSelection"),
    EVENT_GROUP("eventGroupSelection")

    final String val

    DictionaryTypeEnum(String val){
        this.val = val
    }

    String value() { return val }
}