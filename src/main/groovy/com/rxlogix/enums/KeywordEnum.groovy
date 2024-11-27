package com.rxlogix.enums

enum KeywordEnum {
    AND('and'), OR('or'), MINUS('minus'), INTERSECT('intersect'), UNION('union')

    private final String val

    KeywordEnum(String val) {
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.keyword.${this.name()}"
    }
}
