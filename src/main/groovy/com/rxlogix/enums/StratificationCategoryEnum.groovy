package com.rxlogix.enums

enum StratificationCategoryEnum {

    STR_AGE_GROUP('STR_AGE_GROUP'),
    STR_OCCURED_COUNTRY_NAME('STR_OCCURED_COUNTRY_NAME'),
    STR_GENDER('STR_GENDER'),
    STR_RECEIPT_YEAR('STR_RECEIPT_YEAR'),
    STR_REPORTER_TYPE('STR_REPORTER_TYPE'),
    STR_REGION('STR_REGION')

    private final String val

    StratificationCategoryEnum(String val) {
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.stratificationCategory.${this.name()}"
    }

}