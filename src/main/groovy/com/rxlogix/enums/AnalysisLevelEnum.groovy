package com.rxlogix.enums

public enum AnalysisLevelEnum {

    PRODUCT('Product'), PT('PT')

    private final String val

    AnalysisLevelEnum(String val) {
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.analysisLevel.${this.name()}"
    }
}
