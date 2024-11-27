package com.rxlogix

public enum ReportCategory {
    CASE_LISTING("Case Listing"),
    TABULATION("Tabulation"),
    SUBMISSION("Submission"),
    PREGNANCY("Pregnancy"),
    DEVICE_VACCINE("Device/Vaccine"),
    MISCELLANEOUS("Miscellaneous"),
    IT_CONFIGURATION("IT Configuration"),
    IT_MONITORING("IT Monitoring"),
    PMDA("PMDA (Japan Only)")

    String name

    ReportCategory(String name) {
        this.name = name
    }

}