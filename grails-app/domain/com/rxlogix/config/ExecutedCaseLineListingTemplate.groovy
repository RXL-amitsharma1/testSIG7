package com.rxlogix.config

class ExecutedCaseLineListingTemplate extends CaseLineListingTemplate{
    static auditable = false

    static mapping = {
        table name: "EX_CLL_TEMPLT"
    }

    static constraints = {
        originalTemplateId(nullable:false,min:1)
    }
}
