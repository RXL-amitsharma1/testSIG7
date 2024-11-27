package com.rxlogix.config

enum DateRangeValue {
RELATIVE('relative'),CUSTOM('custom'),CUMULATIVE('cumulative')
    private final String val

    DateRangeValue(String val) {
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.dateRangeType.${this.name()}"
    }
}