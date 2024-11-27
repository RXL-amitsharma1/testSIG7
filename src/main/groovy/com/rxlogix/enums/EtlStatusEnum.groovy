package com.rxlogix.enums

enum EtlStatusEnum {
    FAILED("Failed"),
    RUNNING("Running"),
    SUCCESS("Success"),
    ETL_INITIATED("Initiated"),
    ETL_PAUSED("Paused"),
    ETL_STOPPED("Stopped"),
    NOT_STARTED("Not Started");

    private final String val

    EtlStatusEnum(String val) {
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.etlStatus.${this.name()}"
    }
}