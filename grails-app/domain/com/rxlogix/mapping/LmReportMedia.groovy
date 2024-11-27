package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmReportMedia implements SelectableList {

    BigDecimal id
    String reportMedia

    static mapping = {
        datasource "pva"
        table "VW_LRM_MEDIA_DSP"

        cache: "read-only"
        version false

        id column: "MEDIA_ID", type: "big_decimal", generator: "assigned"
        reportMedia column: "MEDIA"
    }

    static constraints = {
        id(nullable:false, unique:true)
        reportMedia(blank:false, maxSize:30)

    }

    @Override
    def getSelectableList() {
        LmReportMedia.withTransaction {
            return LmReportMedia.findAll().unique().collect { it.reportMedia }.sort()
        }
    }
}
