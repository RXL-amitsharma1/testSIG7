package com.rxlogix.reportTemplate

public enum ReassessListednessEnum {
    BEGINNING_OF_THE_REPORTING,
    END_OF_THE_REPORTING_PERIOD,
    LATEST_DATA_SHEET

    public getI18nKey() {
        return "app.reassessListednessEnum.${this.name()}"
    }
}