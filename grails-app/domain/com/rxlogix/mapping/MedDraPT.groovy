package com.rxlogix.mapping

import grails.util.Holders

class MedDraPT {

    Long id
    String name
    int langId

    static belongsTo = [MedDraHLT]
    static hasMany = [parentEvents: MedDraHLT, childEvents: MedDraLLT]

    static mapping = {
        datasources(Holders.getGrailsApplication().getConfig().pvsignal.supported.datasource.call())

        table "PVR_MD_PREF_TERM_DSP"

        cache: "read-only"
        version false

        id column: "PT_CODE", generator: "assigned"
        name column: "PT_NAME"
        langId column: "LANG_ID"
        parentEvents joinTable: [name: "PVR_MD_HLT_PREF_COMP_DSP", key: "PT_CODE", column: "HLT_CODE"]
        childEvents joinTable: [name: "PVR_MD_PREF_TERM_LLT_DSP", key: "PT_CODE", column: "LLT_CODE"]
    }

    static constraints = {
        id(nullable:false, unique:true)
        name(blank:false, maxSize:70)
    }
}
