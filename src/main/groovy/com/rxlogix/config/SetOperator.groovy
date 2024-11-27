package com.rxlogix.config

public enum SetOperator {
   INTERSECT('Intersect'), UNION('Union'), EXCEPT('Except')

    private final String val

    SetOperator(String val) {
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.setOperator.${this.name()}"
    }

}
