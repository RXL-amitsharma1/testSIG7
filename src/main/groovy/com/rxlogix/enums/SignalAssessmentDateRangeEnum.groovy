package com.rxlogix.enums

public enum SignalAssessmentDateRangeEnum {

    SIGNAL_DATA('signalData', 1),
    LAST_3_MONTH('last3Months', 1),
    LAST_6_MONTH('last6Months', 2),
    LAST_1_YEAR('last1Year', 3),
    LAST_3_YEAR('last3Year', 5),
    LAST_5_YEAR('last5Year', 4),
    CUSTOM('custom', 1)

    final String val
    final Integer groupingCode

    SignalAssessmentDateRangeEnum(String val, Integer groupingCode) {
        this.val = val
        this.groupingCode = groupingCode
    }

    String value() { return val }

    String groupingCode() { return groupingCode }


    static SignalAssessmentDateRangeEnum[] getDateRangeForFiltering() {
        return [SIGNAL_DATA, LAST_3_MONTH, LAST_6_MONTH, LAST_1_YEAR, LAST_3_YEAR, LAST_5_YEAR]
    }

    public getI18nKey() {
        return "app.queryOperator.${this.name()}"
    }
}