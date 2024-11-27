package com.rxlogix.enums

public enum PvqTypeEnum {
    SAMPLING('Quality Sampling'),
    CASE_QUALITY('Case Quality Monitoring'),
    SUBMISSION_QUALITY('Submission Quality')

    private final String val

    PvqTypeEnum(String val) {
        this.val = val
    }

    String value() { return val }

    static List<String> toStringList() {
        [SAMPLING.value(), CASE_QUALITY.value(), SUBMISSION_QUALITY.value()]
    }
}
