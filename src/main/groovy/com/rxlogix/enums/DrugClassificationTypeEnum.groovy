package com.rxlogix.enums

public enum DrugClassificationTypeEnum {
    ATC('ATC Code'),
    SUBS('Ingredient'),
    DRUG('Product'),
    Family('Family')

    final String value

    DrugClassificationTypeEnum(String value) {
        this.value = value
    }

    String value() { return value }

    public getI18nKey() {
        return "app.drugClassificationType.${this.name()}"
    }

    public getValue() {
        value
    }

    public getKey() {
        name()
    }
}