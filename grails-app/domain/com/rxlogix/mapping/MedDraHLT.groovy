package com.rxlogix.mapping

import grails.util.Holders

class MedDraHLT {

    Long id
    String name
    int langId

    static belongsTo = [MedDraHLGT]
    static hasMany = [parentEvents: MedDraHLGT, childEvents: MedDraPT]

    static mapping = {
        datasources(Holders.getGrailsApplication().getConfig().pvsignal.supported.datasource.call())
        table "PVR_MD_HL_PREF_TERM_DSP"

        cache: "read-only"
        version false

        id column: "HLT_CODE", generator: "assigned"
        name column: "HLT_NAME"
        langId column: "LANG_ID"
        parentEvents joinTable: [name: "PVR_MD_HLGT_HLT_COMP_DSP", key: "HLT_CODE", column: "HLGT_CODE"]
        childEvents joinTable: [name: "PVR_MD_HLT_PREF_COMP_DSP", key: "HLT_CODE", column: "PT_CODE"]
    }

    static constraints = {
        id(nullable:false, unique:true)
        name(blank:false, maxSize:70)
    }
}
