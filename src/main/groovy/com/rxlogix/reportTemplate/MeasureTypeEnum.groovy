package com.rxlogix.reportTemplate

public enum MeasureTypeEnum {

//    ROW_COUNT,
    CASE_COUNT,
    EVENT_COUNT,
    PRODUCT_EVENT_COUNT,
    REPORT_COUNT,
    CASE_LIST

    public getI18nKey() {
        return "app.measureTypeEnum.${this.name()}"
    }
}
