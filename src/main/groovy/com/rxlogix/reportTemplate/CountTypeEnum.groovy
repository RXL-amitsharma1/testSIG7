package com.rxlogix.reportTemplate

public enum CountTypeEnum {
    PERIOD_COUNT,
    CUMULATIVE_COUNT,
    CUSTOM_PERIOD_COUNT

    public getI18nKey() {
        return "app.countTypeEnum.${this.name()}"
    }
}