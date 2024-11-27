package com.rxlogix.enums

enum ReportTypeEnum {

    MULTI_REPORT("multi"),
    REPORT("single")

    final String key

    ReportTypeEnum(String key) {
        this.key = key
    }
}