package com.rxlogix.enums

enum PageSizeEnum {

    LETTER("Letter"),
    A4("A4"),
    LEGAL("Legal"),
    _11X17("11x17")

    final String value

    PageSizeEnum(String value){
        this.value = value
    }

    //Used to get to key for dropdown lists
    String getKey() {
        name()
    }

    public getI18nKey() {
        return "app.pageSize.${this.name()}"
    }
}