package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmReportForms implements SelectableList {

    BigDecimal id
    String reportFormDesc

    static mapping = {
        datasource "pva"
        table "VW_LRF_FORM_DESC_DSP"

        cache: "read-only"
        version false

        id column: "REPORT_FORM_ID", type: "big_decimal", generator: "assigned"
        reportFormDesc column: "FORM_DESC"
    }

    static constraints = {
        id(nullable:false, unique:true)
        reportFormDesc(blank:false, maxSize:200)

    }

    @Override
    def getSelectableList() {
        LmReportForms.withTransaction {
            return LmReportForms.findAll().unique().collect { it.reportFormDesc }.sort()
        }
    }
}
