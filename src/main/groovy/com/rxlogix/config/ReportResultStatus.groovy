package com.rxlogix.config

public enum ReportResultStatus {
    NEW("NEW"), NON_REVIEWED("NON_REVIEWED"), REVIEWED("REVIEWED")

    private final String val

    ReportResultStatus(String val) {
        this.val = val
    }

    String value() { return val }
}
