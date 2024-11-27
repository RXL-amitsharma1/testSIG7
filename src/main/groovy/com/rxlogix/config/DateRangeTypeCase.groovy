package com.rxlogix.config

public enum DateRangeTypeCase {
    CaseReceiptDate('CM_INIT_REPT_DATE'), CaseLockedDate('CM_DATE_LOCKED'),  SafetyReceiptDate('CM_SAFETY_DATE'), CreationDate('CM_CREATE_TIME'),submissionDate('CMR_DATE_SUBMITTED')

    private final String val

    DateRangeTypeCase(String val) {
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return "app.dateRangeType.${this.name()}"
    }
}