package com.rxlogix.mapping

import grails.util.Holders

class MedDraHLGT {

    Long id
    String name
    int langId

    static belongsTo = [MedDraSOC]
    static hasMany = [parentEvents: MedDraSOC, childEvents: MedDraHLT]

    static mapping = {

        datasources(Holders.getGrailsApplication().getConfig().pvsignal.supported.datasource.call())
        table "PVR_MD_HLG_PREF_TERM_DSP"

        cache: "read-only"
        version false

        id column: "HLGT_CODE", generator: "assigned"
        name column: "HLGT_NAME"
        langId column: "LANG_ID"
        parentEvents joinTable: [name: "PVR_MD_SOC_HLGT_COMP_DSP", key: "HLGT_CODE", column: "SOC_CODE"]
        childEvents joinTable: [name: "PVR_MD_HLGT_HLT_COMP_DSP", key: "HLGT_CODE", column: "HLT_CODE"]
    }

    static constraints = {
        name(blank:false, maxSize:70)
    }
}
