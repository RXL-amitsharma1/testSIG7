package com.rxlogix.enums

enum FrequencyEnum {

    HOURLY('Hourly'),
    DAILY ('Daily'),
    WEEKLY('Weekly'),
    MONTHLY('Monthly'),
    QUATERLY('Quarterly'),
    BIMONTHLY("Bi-Monthly"),
    YEARLY('Yearly'),
    RUN_ONCE('Run Once')

    final String val
    FrequencyEnum(String val){
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.frequency.${this.name()}"
    }
}