package com.rxlogix.enums

enum TrendFrequencyEnum {

    Monthly('Monthly'),
    Quarterly('Quarterly'),
    Yearly('Yearly')

    final String value

    TrendFrequencyEnum(String value) {
        this.value = value
    }

    final String val

    String value() { return val }

    public getI18nKey() {
        return "app.label.trendfrequency.${this.name()}"
    }

}