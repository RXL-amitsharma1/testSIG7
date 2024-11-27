package com.rxlogix.enums

public enum  BusinessConfigTypeEnum {

    PRR("PRR"),
    EBGM("EBGM"),
    EB05("EB05"),
    EB95("EB95"),
    EB05CHECK("EB05 > Prev-EB95"),
    ROR("ROR");

    private final String val

    BusinessConfigTypeEnum(String val) {
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.business.config.type.${this.name()}"
    }
}
