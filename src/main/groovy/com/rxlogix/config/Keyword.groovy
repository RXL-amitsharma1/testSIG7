package com.rxlogix.config

enum Keyword {
    AND('and'), OR('or'), MINUS('minus'), INTERSECT('intersect'), UNION('union')

    private final String val

    Keyword(String val) {
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.keyword.${this.name()}"
    }
}
