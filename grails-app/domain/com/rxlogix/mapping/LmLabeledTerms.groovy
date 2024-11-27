package com.rxlogix.mapping

import com.rxlogix.SelectableList

class LmLabeledTerms implements SelectableList {

    BigDecimal id
    String labeledTerm

    static mapping = {
        datasource "pva"
        table "VW_SHEET_LISTED_TERMS_DSP"

        cache: "read-only"
        version false

        id column: "LABEL_TERM_ID", type: "big_decimal", generator: "assigned"
        labeledTerm column: "LABELED_TERM"
    }

    static constraints = {
        id(nullable:false, unique:true)
        labeledTerm(blank:false, maxSize:250)

    }

    @Override
    def getSelectableList() {
        LmLabeledTerms.withTransaction {
            return LmLabeledTerms.findAll().unique().collect { it.labeledTerm }.sort()
        }
    }
}
