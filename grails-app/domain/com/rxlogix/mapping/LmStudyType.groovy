package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmStudyType implements SelectableList {

    BigDecimal id
    String studyType

    static mapping = {
        datasource "pva"
        table "VW_LSTY_STUDY_TYPE_DSP"

        cache: "read-only"
        version false

        id column: "STUDY_TYPE_ID", type: "big_decimal", generator: "assigned"
        studyType column: "STUDY_TYPE"
    }

    static constraints = {
        id(nullable: false, unique: true)
        studyType(blank: false, maxSize: 15)

    }

    @Override
    def getSelectableList() {
        LmStudyType.withTransaction {
            return LmStudyType.findAll().unique().collect { it.studyType }.sort()
        }
    }
}
