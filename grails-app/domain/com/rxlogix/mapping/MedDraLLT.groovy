package com.rxlogix.mapping

import grails.util.Holders

class MedDraLLT {

    Long id
    String name
    int langId

    static belongsTo = [parentEvents:MedDraPT]

    static mapping = {
        datasources(Holders.getGrailsApplication().getConfig().pvsignal.supported.datasource.call())
        table "PVR_MD_PREF_TERM_LLT_DSP"
        cache: "read-only"
        version false

        id column: "LLT_CODE", generator: "assigned"
        name column: "LLT_NAME"
        parentEvents column: "PT_CODE"
        langId column: "LANG_ID"
    }

    static constraints = {
        id(nullable:false, unique:true)
        name(blank:false, maxSize:70)
    }
}
