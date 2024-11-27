package com.rxlogix.enums

enum EvaluateCaseDateEnum {

    LATEST_VERSION('LATEST_VERSION'),
    VERSION_PER_REPORTING_PERIOD('VERSION_PER_REPORTING_PERIOD'),
    VERSION_ASOF('VERSION_ASOF'),
    VERSION_ASOF_GENERATION_DATE('VERSION_ASOF_GENERATION_DATE')
    private final String val

    EvaluateCaseDateEnum(String val) {
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.evaluateCaseDate.${this.name()}"
    }
}
