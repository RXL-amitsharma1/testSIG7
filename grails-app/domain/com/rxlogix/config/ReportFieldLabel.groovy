package com.rxlogix.config

class ReportFieldLabel {

    String code
    String loc
    String textVal

    static mapping = {
        datasource("pva")
        table "PVR_RPT_FIELD_LABEL"
        cache: "read-only"
        version false

        id column: "id", generator: "assigned"
        code column: "code"
        loc column: "loc"
        textVal column: "text"


    }
}
