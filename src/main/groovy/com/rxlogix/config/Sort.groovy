package com.rxlogix.config

public enum Sort {
    ASCENDING("asc"),
    DESCENDING("desc")

    private final String val

    Sort(String val) {
        this.val = val
    }

    String value() { return val }

    static Sort valueOfName( String name ) {
        values().find { it.val == name }
    }
}
