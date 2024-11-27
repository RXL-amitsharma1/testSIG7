package com.rxlogix.enums

enum ReportNameEnum {

    ICSR_BY_CASE_CHARACTERISTICS('ICSRs by Case Characteristics'),
    PBRER_SIGNAL_SUMMARY('PBRER Signal Summary'),
    SIGNAL_SUMMARY_REPORT('Signal Summary Report'),
    SIGNALS_BY_STATE('Signals by State'),
    SIGNAL_PRODUCT_ACTIONS('Signal Product Actions'),
    MEMO_REPORT('Memo Reports')

    final String value

    ReportNameEnum(String value) {
        this.value = value
    }

    static ReportNameEnum[] getAllReportNames() {
        return [ICSR_BY_CASE_CHARACTERISTICS, PBRER_SIGNAL_SUMMARY, SIGNAL_SUMMARY_REPORT, SIGNALS_BY_STATE, SIGNAL_PRODUCT_ACTIONS, MEMO_REPORT].sort({it.value.toUpperCase()})
    }
}