package com.rxlogix.enums

import grails.util.Holders

public enum DateRangeTypeCaseEnum {

    CASE_RECEIPT_DATE('dcdtDatecol1'),
    SAFTEY_RECEIPT_DATE('dcdtDatecol2'),
    CREATION_DATE('dcdtDatecol3'),
    INITIAL_DATE('dcdtDatecol4'),
    J_RECEIPT_DATE('dcdtDatecol5'),
    LATEST_RECEIPT_DATE('dcdtDatecol8'),
    CASE_LOCKED_DATE('dcdtDatecol9'),
    SUBMISSION_DATE('dcdtDatecol10'),
    EVENT_RECEIPT_DATE('dcdtDatecol14')

    private final String val

    DateRangeTypeCaseEnum(String val) {
        this.val = val
    }

    String value() { return val }

    public getI18nKey() {
        return Holders.config.dateRangeType.labels[this.name()]
    }
}