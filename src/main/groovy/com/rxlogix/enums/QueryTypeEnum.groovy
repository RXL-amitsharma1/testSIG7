package com.rxlogix.enums

public enum QueryTypeEnum {
    QUERY_BUILDER,
    SET_BUILDER,
    CUSTOM_SQL

    public getI18nKey() {
        return "app.queryType.${this.name()}"
    }
}

